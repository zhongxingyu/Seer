 package fi.helsinki.acmcrawler.domain.support;
 
 import fi.helsinki.acmcrawler.Magic;
 import fi.helsinki.acmcrawler.domain.support.AuthorNode;
 import fi.helsinki.acmcrawler.domain.SeedFactory;
 import fi.helsinki.acmcrawler.storage.CollaborationGraphDB;
 import java.util.ArrayList;
 import java.util.List;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 /**
  *
  * @author Rodion Efremov
  * @version I
  */
 public class DefaultSeedFactory implements SeedFactory<AuthorNode>{
 
     private static final String XPATH_JOURNAL_A =
             "//a[contains(@href,'pub.cfm?id=')]";
 
     private static final String XPATH_AUTHOR_LINK_A =
             "//a[contains(@href,'author_page.cfm?id=')]";
 
     private static final String XPATH_PAPER_LINK_A =
             "//a[starts-with(@href,'citation.cfm?id=')]";
 
     private static final String LINKTEXT_BIBTEX = "BibTeX";
 
     private CollaborationGraphDB<AuthorNode> db;
     private List<AuthorNode> list;
     private int max;
 
     public DefaultSeedFactory(CollaborationGraphDB<AuthorNode> db) {
         this.db = db;
     }
 
     @Override
     public List<AuthorNode> get(int max) {
         if (this.list != null) {
             return new ArrayList<AuthorNode>(this.list);
         }
 
         this.max = max;
         this.list = new ArrayList<AuthorNode>();
         populate(Magic.DEFAULT_JAVASCRIPT_WAIT);
         return new ArrayList<AuthorNode>(this.list);
     }
 
     private void populate(int timeoutSeconds) {
         WebDriver driver = new HtmlUnitDriver();
         navigateToJournalListPage(driver, timeoutSeconds);
         List<WebElement> aList = getLinkElementsFromJournalListPage(driver);
 
         for (WebElement e : aList) {
             System.out.println(e.getText());
             populateFromJournal(e.getAttribute("href"), timeoutSeconds);
 
             if (max <= 0) {
                 return;
             }
         }
     }
 
     private void navigateToJournalListPage(WebDriver driver, int timeoutSeconds) {
         driver.get(Magic.URL_BASE + "/" + Magic.URL_JOURNAL_LIST_PAGE);
     }
 
     private List<WebElement> getLinkElementsFromJournalListPage(WebDriver driver) {
         return driver.findElements(By.xpath(XPATH_JOURNAL_A));
     }
 
     private void populateFromJournal(String url, int timeoutSeconds) {
         WebDriver driver = new HtmlUnitDriver(true);
         driver.get(url);
 
         WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
         wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.xpath(XPATH_AUTHOR_LINK_A)));
 
         List<WebElement> aList =
                 driver.findElements(By.xpath(XPATH_AUTHOR_LINK_A));
 
         for (WebElement e : aList) {
             System.out.println("  " + e.getText());
             String raw = e.getAttribute("href");
             int i1 = raw.indexOf("id=");
 
             if (i1 < 0) {
                 continue;
             }
 
             int i2 = raw.indexOf("&");
 
             if (i2 < 0 || i2 < i1) {
                 continue;
             }
 
            String id = raw.substring(i1 + "id=".length(), i2).trim();
             AuthorNode authorNode = new AuthorNode(id);
             authorNode.setName(e.getText().trim());
             authorNode.setDb(db);
 
             if (db != null) {
                 db.addAuthor(authorNode.getId(), authorNode.getName());
                 processAuthor(id, timeoutSeconds);
             }
 
             this.list.add(authorNode);
             --max;
 
             if (max <= 0) {
                 return;
             }
         }
     }
 
     /**
      * Load all the papers written by author id <code>id</code> and their
      * BibTex-references.
      *
      * @param id the ACM DL id of an author.
      */
     private void processAuthor(String id, int timeoutSeconds) {
         String url = Magic.URL_BASE + "/"
                 + Magic.URL_AUTHOR_PAGE_SCRIPT_NAME
                 + "?id=" + id + Magic.URL_GET_ALL_ARGS;
         WebDriver driver = new HtmlUnitDriver(true);
         driver.get(url);
 
         WebDriverWait wait = new WebDriverWait(driver, timeoutSeconds);
         wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.xpath(XPATH_PAPER_LINK_A)));
 
         List<WebElement> aPapers =
                 driver.findElements(By.xpath(XPATH_PAPER_LINK_A));
 
         for (WebElement e : aPapers) {
             System.out.println("      > " + e.getText());
             savePaper(id, e);
         }
 
         downloadAllBibtexOfAuthor(driver);
     }
 
     private void downloadAllBibtexOfAuthor(WebDriver driver) {
         WebElement link = driver.findElement(By.linkText(LINKTEXT_BIBTEX));
 
         if (link == null) {
             return;
         }
 
         link.click();
 
         List<WebElement> pres = driver.findElements(By.tagName("pre"));
 
         if (pres == null) {
             return;
         }
 
         for (WebElement e : pres) {
             db.addBibtexToPaper(e.getAttribute("id").trim(),
                                 e.getText().trim());
         }
     }
 
     private void savePaper(String authorId, WebElement a) {
         String url = a.getAttribute("href");
         int i1 = url.indexOf("id=");
 
         if (i1 < 0) {
             return;
         }
 
         int i2 = url.indexOf("&");
 
         if (i2 < 0 || i2 < i1) {
             return;
         }
 
         String paperId = url.substring(i1 + "id=".length(), i2).trim();
         db.addPaper(paperId, a.getText().trim());
         db.associate(authorId, paperId);
     }
 }
