 /*
  * Created on Apr 28, 2005
  *
  * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
  * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
  * 
 */
 package org.cubictest.exporters.selenium.runner.converters;
 
 import static org.cubictest.model.IdentifierType.LABEL;
 
 import org.cubictest.common.utils.Logger;
 import org.cubictest.export.converters.IPageElementConverter;
 import org.cubictest.export.exceptions.AssertionFailedException;
 import org.cubictest.export.exceptions.TestFailedException;
 import org.cubictest.exporters.selenium.runner.holders.SeleniumHolder;
 import org.cubictest.model.FormElement;
 import org.cubictest.model.PageElement;
 import org.cubictest.model.TestPartStatus;
 import org.cubictest.model.Text;
 import org.cubictest.model.Title;
 import org.cubictest.model.formElement.Option;
 
 import com.thoughtworks.selenium.SeleniumException;
 
 /**
  * This class is responsible for asserting PageElements present. 
  * 
  * @author chr_schwarz
  */
 public class PageElementConverter implements IPageElementConverter<SeleniumHolder> {	
 
 	
 	/**
 	 * Asserts that page element is present on HTML page. 
 	 * @param pe The Page element to convert to Selenese row.
 	 */
 	public void handlePageElement(SeleniumHolder seleniumHolder, PageElement pe) {
 		try {
 			if (pe instanceof Title) {
 				String actual = seleniumHolder.getSelenium().getTitle();
 				String expected = pe.getIdentifier(LABEL).getValue();
 	
 				if (actual.equals(expected)) {
 					seleniumHolder.addResult(pe, TestPartStatus.PASS, pe.isNot());
 				}
 				else {
 					seleniumHolder.addResult(pe, TestPartStatus.FAIL, pe.isNot());
 				}
 			}
 			else if (pe instanceof FormElement && !(pe instanceof Option)){
 				//html input elements: get value
 				String locator = "xpath=" + seleniumHolder.getFullContextWithAllElements(pe);
 				String value = seleniumHolder.getSelenium().getValue(locator);
 				if (value == null) {
 					seleniumHolder.addResult(pe, TestPartStatus.FAIL, pe.isNot());
 				}
 				else {
 					seleniumHolder.addResult(pe, TestPartStatus.PASS, pe.isNot());
 				}
 			}
 			else if (seleniumHolder.isInRootContext() && pe instanceof Text) {
 				//texts in root context have bug in firefox xpath, use selenium's own function:
 				boolean present = seleniumHolder.getSelenium().isTextPresent(pe.getText());
 				if (present) {
 					seleniumHolder.addResult(pe, TestPartStatus.PASS, pe.isNot());
 				}
 				else {
 					seleniumHolder.addResult(pe, TestPartStatus.FAIL, pe.isNot());
 				}
 			}
 			else {
 				//all other elements: get text
 				String locator = "xpath=" + seleniumHolder.getFullContextWithAllElements(pe);
 				String text = seleniumHolder.getSelenium().getText(locator);
 				if (text == null) {
 					seleniumHolder.addResult(pe, TestPartStatus.FAIL, pe.isNot());
 				}
 				else {
 					seleniumHolder.addResult(pe, TestPartStatus.PASS, pe.isNot());
 				}
 			}
 		}
 		catch (AssertionFailedException e) {
 			if (seleniumHolder.shouldFailOnAssertionFailure()) {
 				throw new TestFailedException(e.getMessage());
 			}
 		}
 		catch (SeleniumException e) {
 			Logger.error(e, "Test step failed");
			seleniumHolder.addResult(pe, TestPartStatus.EXCEPTION, pe.isNot());
 			throw new TestFailedException(e.getMessage());
 		}
 	}
 }
