 package nl.wiggertloonstra.bible.ui.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import nl.wiggertloonstra.bible.hibernate.BibleTextRepository;
 import nl.wiggertloonstra.bible.hibernate.CategoryRepository;
 import nl.wiggertloonstra.bible.hibernate.domain.BibleTextDo;
 import nl.wiggertloonstra.bible.hibernate.domain.CategoryDo;
 import nl.wiggertloonstra.bible.ui.view.BibleTextView;
 import nl.wiggertloonstra.bible.ui.view.CategoryView;
 import nl.wiggertloonstra.bible.util.BiblePointerFormatter;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.client.ClientProtocolException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 @Controller
 public class OverviewController {
 
     private static final Function<BibleTextDo, BibleTextView> TO_BIBLETEXT_VIEW_AND = new Function<BibleTextDo, BibleTextView>() {
         @Override
         public BibleTextView apply(BibleTextDo from) {
             return new BibleTextView(from);
         }
         
     };
     
     private static final String DEFAULT_BIBLIJA_URL = "http://www.biblija.net/biblija.cgi?l=nl&m=";
     List<String> texts = TextOverviewHelper.defaultTexts();
     private WebDriver driver;
     private final BibleTextRepository bibleTextRepository;
     private final CategoryRepository categoryRepository;
     
     @Autowired
     public OverviewController(BibleTextRepository bibleTextRepository,
                               CategoryRepository categoryRepository) {
         this.bibleTextRepository = bibleTextRepository;
         this.categoryRepository = categoryRepository;
         driver = new HtmlUnitDriver();
     }
     
     @RequestMapping("/overzicht.html")
     public String overview(Model model,
                            @RequestParam(required = false, defaultValue = "0") int categoryId) throws ClientProtocolException, IOException {
         model.addAttribute("bibleTexts", getBibleTextsFor(categoryId));
         model.addAttribute("categoriesHeader", getHeaderFor(categoryId));
         model.addAttribute("categories", getCategories());
         
         //model.addAttribute("liveBibleText", seleniumBibleText());
         
         return "overview";
     }
 
     private String getHeaderFor(int categoryId) {
         if (categoryId > 0) {
             return categoryRepository.getCategoryFor(categoryId).getName();
         } else {
            return "Alle categorien";
         }
     }
 
     private List<CategoryView> getCategories() {
         return Lists.transform(categoryRepository.getAllCategories(), new Function<CategoryDo, CategoryView>() {
             @Override
             public CategoryView apply(CategoryDo from) {
                 return new CategoryView(from);
             }
         });
     }
 
     private List<BibleTextView> getBibleTextsFor(int categoryId) {
         List<BibleTextDo> bibleTextDos;
         
         if (categoryId > 0) {
             bibleTextDos = bibleTextRepository.getBibleTextsForCategory(categoryId);
         } else {
             bibleTextDos = bibleTextRepository.getLatestBibleTexts(10);
         }
         
         makeSureTextsAreAvailableFor(bibleTextDos);
         
         return Lists.transform(bibleTextDos, TO_BIBLETEXT_VIEW_AND);
     }
 
     private void makeSureTextsAreAvailableFor(List<BibleTextDo> bibleTextDos) {
         for (BibleTextDo bibleTextDo : bibleTextDos) {
             if (StringUtils.isBlank(bibleTextDo.getText())) {
                 String text = seleniumBibleText(bibleTextDo);
                 bibleTextDo.setText(text);
                 bibleTextRepository.store(bibleTextDo);
             }
         }
     }
 
     private String seleniumBibleText(BibleTextDo bibleTextDo) {
         driver.get(DEFAULT_BIBLIJA_URL + BiblePointerFormatter.format(bibleTextDo));
         return driver.findElement(By.xpath("//table/tbody/tr/td/table/tbody/tr/td[@class = 'text']")).getText();
     }
 }
