 package wdcore.selenium.elementLocatorFactory.elements.impl;
 
 import java.util.List;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.Select;
 
 import wdcore.selenium.elementLocatorFactory.elements.Selector;
 
 class SelectorImpl extends AbstractElement implements Selector {
 
    Select select;
 
     protected SelectorImpl(final WebElement wrappedElement) {
         super(wrappedElement);
        select = new Select(wrappedElement);
 
     }
 
     @Override
     public void select(String value) {
         LOG.debug("Select '" + value + "' in " + wrappedElement);
 
 
//			Select select = new Select(wrappedElement);
         if (value.startsWith("value=")) {
             select.selectByValue(value.replace("value=", ""));
         } else if (value.startsWith("label=")) {
             select.selectByVisibleText(value.replace("label=", ""));
         } else if (value.startsWith("index=")) {
             int index = Integer.parseInt(value.replace("index=", ""));
             select.selectByIndex(index);
         } else {
             select.selectByVisibleText(value);
         }
 
     }
 
     @Override
     public StringBuilder getOptionsText() {
         StringBuilder result = new StringBuilder();
         List<WebElement> options = select.getOptions();
 
         for (WebElement option : options) {
 //            result = result + option.getText() + ";";
             result.append(option.getText());
         }
 
         return result;
 
     }
 }
