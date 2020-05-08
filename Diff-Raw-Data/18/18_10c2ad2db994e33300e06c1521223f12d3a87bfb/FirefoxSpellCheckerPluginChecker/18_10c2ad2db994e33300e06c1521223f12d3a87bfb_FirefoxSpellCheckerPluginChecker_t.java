 package org.raoxunrong.check.firefox;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebElement;
 import org.raoxunrong.check.PageChecker;
 import org.raoxunrong.domain.item.PlainTextItem;
 import org.raoxunrong.domain.page.CheckablePage;
 
 import java.util.List;
 
 import static org.raoxunrong.check.JavascriptGenerator.generateDispatchEventScript;
 import static org.raoxunrong.utils.CheckedItemStatistic.addCheckedItem;
 
 public class FirefoxSpellCheckerPluginChecker implements PageChecker {
 
     @Override
     public void doCheck(CheckablePage page) {
         doSpellCheckerActionEvent(page);
         handleSpellCheckerInfo(page);
     }
 
     private void handleSpellCheckerInfo(CheckablePage page) {
         List<WebElement> elements = page.getWebDriver().findElements(By.className("misspelling"));
         StringBuffer stringBuffer = new StringBuffer();
        String errorWord;
         for (WebElement webElement : elements) {
            errorWord = webElement.getText();
            if(errorWord.isEmpty() && !webElement.getAttribute("innerHTML").isEmpty()){
                errorWord = webElement.getAttribute("innerHTML");
            }
            stringBuffer.append(errorWord).append("\n");
         }
         addCheckedItem(new PlainTextItem(page.getPageName(), (elements.size() == 0), stringBuffer.toString()));
     }
 
     private void doSpellCheckerActionEvent(CheckablePage page) {
         ((JavascriptExecutor) page.getWebDriver()).executeScript(withCheckScript(page.getPageId()));
     }
 
     private String withCheckScript(String pageId) {
         return generateDispatchEventScript(pageId, "CheckSpellingEvent");
     }
 
 }
 
 
