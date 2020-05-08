 /*
  *	Copyright Technophobia Ltd 2012
  *
  *   This file is part of Substeps.
  *
  *    Substeps is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU Lesser General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    Substeps is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU Lesser General Public License for more details.
  *
  *    You should have received a copy of the GNU Lesser General Public License
  *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.technophobia.webdriver.substeps.impl;
 
 import static com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown.getThreadLocalWebDriver;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.pagefactory.ByChained;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Supplier;
 import com.technophobia.substeps.model.SubSteps.Step;
 import com.technophobia.substeps.model.SubSteps.StepImplementations;
 import com.technophobia.webdriver.substeps.runner.DefaultExecutionSetupTearDown;
 import com.technophobia.webdriver.util.WebDriverContext;
 import com.technophobia.webdriver.util.WebDriverSubstepsBy;
 import com.technophobia.webdriver.util.WebElementPredicate;
 
 @StepImplementations(requiredInitialisationClasses = DefaultExecutionSetupTearDown.class)
 public class FinderWebDriverSubStepImplementations extends AbstractWebDriverSubStepImplementations {
 
     private static final Logger logger = LoggerFactory.getLogger(FinderWebDriverSubStepImplementations.class);
 
     public FinderWebDriverSubStepImplementations() {
         super();
     }
 
     public FinderWebDriverSubStepImplementations(final Supplier<WebDriverContext> webDriverContextSupplier) {
         super(webDriverContextSupplier);
     }
 
     /**
      * Find an element by it's ID
      * 
      * @example FindById username
      * @section Location
      * 
      * @param id
      *            the id
      * @return the web element
      */
     @Step("FindById ([^\"]*)")
     public WebElement findById(final String id) {
 
         logger.debug("Looking for item with id " + id);
         webDriverContext().setCurrentElement(null);
 
         final WebElement elem = webDriverContext().waitForElement(By.id(id));
         Assert.assertNotNull("expecting an element with id " + id, elem);
 
         webDriverContext().setCurrentElement(elem);
         return elem;
     }
 
     /**
      * Find an element by it's ID with the specified timeout
      * 
      * @example FindByIdTimeout username timeout = 15 secs
      * @section Location
      * 
      * @param id
      *            the id
      * @param timeout
      *            the timeout
      * @return the web element
      */
     @Step("FindByIdTimeout ([^\"]*) timeout = ([^\"]*) secs")
     public WebElement findById(final String id, final String timeout) {
         logger.debug("Looking for item with id " + id + "within " + timeout + " seconds");
         final long t = Long.parseLong(timeout);
 
         webDriverContext().setCurrentElement(null);
         final WebElement elem = webDriverContext().waitForElement(By.id(id), t);
         Assert.assertNotNull("expecting an element with id " + id, elem);
         webDriverContext().setCurrentElement(elem);
         return elem;
     }
 
     /**
      * Find an id by xpath
      * 
      * @example FindByXpath
      * @section Location
      * 
      * @param xpath
      *            the xpath
      */
     @Step("FindByXpath ([^\"]*)")
     public void findByXpath(final String xpath) {
         logger.debug("Looking for item with xpath " + xpath);
         webDriverContext().setCurrentElement(null);
         final WebElement elem = webDriver().findElement(By.xpath(xpath));
         Assert.assertNotNull("expecting an element with xpath " + xpath, elem);
         webDriverContext().setCurrentElement(elem);
     }
 
     /**
      * Find an element using the name attribute of the element
      * 
      * @example FindByName "named field"
      * @section Location
      * 
      * @param name
      *            the name
      * @return the web element
      */
     @Step("FindByName \"?([^\"]*)\"?")
     public WebElement findByName(final String name) {
         logger.debug("Looking for item with name " + name);
         webDriverContext().setCurrentElement(null);
         final WebElement elem = webDriverContext().waitForElement(By.name(name));
         Assert.assertNotNull("expecting an element with name " + name, elem);
         webDriverContext().setCurrentElement(elem);
         return elem;
     }
 
     /**
      * Find element by predicate.
      * 
      * @example
      * @param predicate
      *            the predicate
      * @return the web element
      */
     public WebElement findElementByPredicate(final WebElementPredicate predicate) {
         logger.debug("About to find element by predicate " + predicate);
         WebElement rtn = null;
 
         final List<WebElement> elems = webDriver().findElements(By.tagName(predicate.getTagname()));
 
         for (final WebElement e : elems) {
 
             if (predicate.apply(e)) {
                 rtn = e;
                 break;
             }
         }
         if (rtn == null) {
             throw new IllegalStateException("Failed to find element by predicate: " + predicate.getDescription());
         }
 
         return rtn;
     }
 
     /**
      * Finds an element on the page with the specified tag and text
      * 
      * @example FindTagElementContainingText tag="ul" text="list item itext"
      * @section Location
      * @param tag
      *            the tag
      * @param text
      *            the text
      */
     @Step("FindTagElementContainingText tag=\"([^\"]*)\" text=\"([^\"]*)\"")
     public void findTagElementContainingText(final String tag, final String text) {
         logger.debug("Finding tag element " + tag + "and asserting has text " + text);
 
         webDriverContext().setCurrentElement(null);
         final List<WebElement> elementsWithTagName = webDriver().findElements(By.tagName(tag));
 
         WebElement matchingElement = null;
         for (final WebElement element : elementsWithTagName) {
 
            if (element.getText().contains(text)) {
 
                 if (matchingElement == null) {
                     matchingElement = element;
                 } else {
                     Assert.fail("expected one element with tag " + tag + " and text " + text + " but found multiple");
                 }
             }
         }
 
         Assert.assertNotNull("expecting element with tag " + tag + " and text " + text, matchingElement);
         webDriverContext().setCurrentElement(matchingElement);
     }
 
     /**
      * Finds an element that is a child of the current element using the name
      * attribute, another Find method should be used first
      * 
      * @example FindChild ByName name="child name"
      * @section Location
      * 
      * @param name
      *            the name
      * @return the web element
      */
     @Step("FindChild ByName name=\"?([^\"]*)\"?")
     public WebElement findChildByName(final String name) {
         logger.debug("Looking for child with name " + name);
         Assert.assertNotNull("expecting a current element", webDriverContext().getCurrentElement());
         final WebElement elem = webDriverContext().getCurrentElement().findElement(By.name(name));
 
         Assert.assertNotNull("expecting an element with name " + name, elem);
         webDriverContext().setCurrentElement(elem);
         return elem;
     }
 
     /**
      * Finds an element that is a child of the current element using the tag
      * name and specified attributes, another Find method should be used first
      * 
      * @example FindChild ByTagAndAttributes tag="input"
      *          attributes=[type="submit",value="Search"]
      * @section Location
      * 
      * @param tag
      *            the tag
      * @param attributeString
      *            the attribute string
      * @return the web element
      */
     @Step("FindChild ByTagAndAttributes tag=\"?([^\"]*)\"? attributes=\\[(.*)\\]")
     public WebElement findChildByTagAndAttributes(final String tag, final String attributeString) {
         logger.debug("Looking for child with tag " + tag + " and attributes " + attributeString);
         Assert.assertNotNull("expecting a current element", webDriverContext().getCurrentElement());
 
         final WebElement currentElement = webDriverContext().getCurrentElement();
 
         final Map<String, String> expectedAttributes = convertToMap(attributeString);
 
         final By byTagAndAttributes = WebDriverSubstepsBy.ByTagAndAttributes(tag, expectedAttributes);
 
         final By byCurrentElement = WebDriverSubstepsBy.ByCurrentWebElement(currentElement);
 
         final By chained = new ByChained(byCurrentElement, byTagAndAttributes);
 
         final String msg = "failed to locate a child element with tag: " + tag + " and attributes: " + attributeString;
 
         final WebElement elem = lookForManyWaitForOne(chained, msg);
 
         webDriverContext().setCurrentElement(elem);
 
         return elem;
     }
 
     /**
      * Finds a checkbox that is a child of the specified tag, that contains the
      * specified text; eg.
      * 
      * <pre>
      * <label>
      *  <input type="checkbox" name="checkbox_name" value="yeah"/>a checkbox <span>label</span>
      * </label>
      * </pre>
      * 
      * @example FindCheckbox inside tag="label" with label="a checkbox label>"
      * 
      * @section Location
      * 
      * @param tag
      *            the tag
      * @param label
      *            the checkbox label
      * @return the web element
      */
     @Step("FindCheckbox inside tag=\"?([^\"]*)\"? with label=\"([^\"]*)\"")
     public WebElement findCheckBox(final String tag, final String label) {
 
         return findInputInsideTag(label, tag, "checkbox");
 
     }
 
     // todo variant that also has attributes for the tag
 
     /**
      * Finds a radiobutton that is a child of the specified tag, that contains
      * the specified text; eg.
      * 
      * <pre>
      * <label>
      *  <input type="radio" name="radio_name" value="yeah"/>a radio <span>label</span>
      * </label>
      * </pre>
      * 
      * @example FindRadioButton inside tag="label" with label="a radio label>"
      * 
      * @section Location
      * 
      * @param tag
      *            the tag
      * @param label
      *            the radio button label
      * @return the web element
      */
     @Step("FindRadioButton inside tag=\"?([^\"]*)\"? with label=\"([^\"]*)\"")
     public WebElement findRadioButton(final String tag, final String label) {
 
         return findInputInsideTag(label, tag, "radio");
     }
 
     /**
      * @param label
      * @param tag
      * @param inputType
      * @return
      */
     public WebElement findInputInsideTag(final String label, final String tag, final String inputType) {
         WebElement elem = null;
         webDriverContext().setCurrentElement(null);
 
         // TODO - turn this into a function
 
         // look for elems with the right tag
         final List<WebElement> tagElems = getThreadLocalWebDriver().findElements(By.tagName(tag));
 
         checkElements("expecting some elements of tag: " + tag, tagElems);
 
         List<WebElement> matchingElems = null;
 
         for (final WebElement tagElem : tagElems) {
 
             // does this tag contain the right text ?
             if (label.compareTo(tagElem.getText()) == 0) {
                 // yes
                 // is there a radio button inside ?
 
                 final List<WebElement> inputElements = tagElem.findElements(By.tagName("input"));
 
                 if (inputElements != null && !inputElements.isEmpty()) {
                     // are they radio buttons ?
 
                     for (final WebElement inputElement : inputElements) {
                         final String type = inputElement.getAttribute("type");
 
                         if (type != null && type.compareToIgnoreCase(inputType) == 0) {
                             // bingo
                             if (matchingElems == null) {
                                 matchingElems = new ArrayList<WebElement>();
                             }
                             matchingElems.add(inputElement);
                         }
                     }
                 }
             }
         }
 
         elem = checkForOneMatchingElement("expecting an input of type " + inputType + " inside tag [" + tag
                 + "] with label [" + label + "]", matchingElems);
 
         webDriverContext().setCurrentElement(elem);
         return elem;
     }
 
     /**
      * @param tag
      * @param tagElems
      */
     public void checkElements(final String msg, final List<WebElement> tagElems) {
         Assert.assertNotNull(msg, tagElems);
         Assert.assertTrue(msg, !tagElems.isEmpty());
     }
 
     /**
      * Find an element by tag name and a set of attributes and corresponding
      * values
      * 
      * @param tag
      *            the tag
      * @param attributeString
      *            the attribute string
      * @return the web element
      * @example FindByTagAndAttributes tag="input"
      *          attributes=[type="submit",value="Search"]
      * @section Location
      */
     @Step("FindByTagAndAttributes tag=\"?([^\"]*)\"? attributes=\\[(.*)\\]")
     public WebElement findByTagAndAttributes(final String tag, final String attributeString) {
         logger.debug("Looking for item with tag " + tag + " and attributes " + attributeString);
         webDriverContext().setCurrentElement(null);
         final Map<String, String> expectedAttributes = convertToMap(attributeString);
 
         WebElement rtn = null;
 
         final By by = WebDriverSubstepsBy.ByTagAndAttributes(tag, expectedAttributes);
 
         final String msg = "failed to locate an element with tag: " + tag + " and attributes: " + attributeString;
 
         rtn = lookForManyWaitForOne(by, msg);
 
         webDriverContext().setCurrentElement(rtn);
 
         return rtn;
     }
 
     /**
      * This method will attempt to find many elements using the supplied By, if
      * no elements are found, it will wait until one matching element is found.
      * finding multiple elements will result in failure
      * 
      * @param by
      * @param matchingElems
      * @param msg
      * @return
      */
     private WebElement lookForManyWaitForOne(final By by, final String msg) {
 
         WebElement rtn = null;
 
         List<WebElement> matchingElems = webDriverContext().getWebDriver().findElements(by);
 
         if (matchingElems == null || matchingElems.isEmpty()) {
 
             // wait for at least one - if we need to wait, we will only find one
             final WebElement elementWaitedFor = webDriverContext().waitForElement(by);
             if (matchingElems == null) {
                 matchingElems = new ArrayList<WebElement>();
             }
             matchingElems.add(elementWaitedFor);
         }
 
         rtn = checkForOneMatchingElement(msg, matchingElems);
         return rtn;
     }
 
     /**
      * Checks that a list of WebElements only contains one (not empty and not
      * too many).
      * 
      * @param msg
      * @param matchingElems
      * @return
      */
     public WebElement checkForOneMatchingElement(final String msg, final List<WebElement> matchingElems) {
         WebElement rtn = null;
         if (matchingElems != null && matchingElems.size() > 1) {
             // ambiguous
             Assert.fail("Found too many elements that meet this criteria");
             // TODO - need some more debug here
         }
 
         else if (matchingElems != null) {
             rtn = matchingElems.get(0);
         }
 
         Assert.assertNotNull(msg, rtn);
         return rtn;
     }
 
     /**
      * Gets the element with text.
      * 
      * @param type
      *            the type
      * @param text
      *            the text
      * @return the element with text
      */
     public WebElement findElementWithText(final String type, final String text) {
         WebElement elem = null;
         final List<WebElement> elems = webDriver().findElements(By.tagName(type));
         if (elems != null) {
             for (final WebElement e : elems) {
 
                 if (text.equalsIgnoreCase(e.getText())) {
                     elem = e;
                     break;
                 }
             }
         }
         return elem;
     }
 
 }
