 package com.pyxis.matchers.dom;
 
 import org.hamcrest.Matcher;
 import org.hamcrest.Matchers;
 import org.w3c.dom.Element;
 
 import static org.hamcrest.Matchers.allOf;
 import static org.hamcrest.Matchers.anything;
 import static org.hamcrest.Matchers.equalTo;
 
 public class DomMatchers {
 
     private DomMatchers() {}
 
     public static Matcher<Element> hasSelector(String selector, Matcher<? super Element>... elementsMatchers) {
         return HasSelector.hasSelector(selector, elementsMatchers);
     }
 
     public static Matcher<Element> hasSelector(String selector, Matcher<Iterable<Element>> elementsMatcher) {
         return HasSelector.hasSelector(selector, elementsMatcher);
     }
 
     public static Matcher<Element> hasUniqueSelector(String selector) {
         return HasUniqueSelector.hasUniqueSelector(selector);
     }
 
     public static Matcher<Element> hasUniqueSelector(String selector, Matcher<Element> elementMatcher) {
         return HasUniqueSelector.hasUniqueSelector(selector, elementMatcher);
     }
 
     public static Matcher<Element> hasUniqueSelector(String selector, Matcher<Element>... elementMatchers) {
         return HasUniqueSelector.hasUniqueSelector(selector, allOf(elementMatchers));
     }
 
     public static Matcher<Element> hasNoSelector(String selector) {
     	return HasNoSelector.hasNoSelector(selector);
     }
 
     public static Matcher<Iterable<Element>> inOrder(Matcher<Element>... elementMatchers) {
         return Matchers.contains(elementMatchers);
     }
 
     public static Matcher<Iterable<Element>> hasElement(Matcher<? super Element> elementMatcher) {
         return Matchers.hasItems(elementMatcher);
     }
 
     public static Matcher<Iterable<Element>> hasElements(Matcher<? super Element>... elementMatchers) {
         return Matchers.hasItems(elementMatchers);
     }
 
     public static Matcher<Element> withTag(String tagName) {
         return WithTag.withTag(tagName);
     }
 
     public static Matcher<Element> withText(String contentText) {
         return WithContentText.withContent(contentText);
     }
     
     public static Matcher<Element> withBlankText() {
     	return WithContentText.withBlankContent();
     }
 
     public static Matcher<Element> withText(Matcher<? super String> contentMatcher) {
         return WithContentText.withContent(contentMatcher);
     }
 
     public static Matcher<Element> withAttribute(String name, Matcher<? super String> valueMatcher) {
         return WithAttribute.withAttribute(name, valueMatcher);
     }
 
     public static Matcher<Element> withAttribute(String name, String value) {
         return WithAttribute.withAttribute(name, value);
     }
 
     public static Matcher<Element> withName(String name) {
         return WithAttribute.withName(name);
     }
 
     public static Matcher<Element> withId(String id) {
         return WithAttribute.withId(id);
     }
 
     public static Matcher<Element> withClassName(String className) {
         return WithAttribute.withClassName(className);
     }
 
     public static Matcher<Element> hasChildren(Matcher<Iterable<Element>> childrenMatcher) {
         return HasChildren.hasChildren(childrenMatcher);
     }
 
     public static Matcher<Element> hasChildren(Matcher<Element>... childrenMatchers) {
         return HasChildren.hasChildren(childrenMatchers);
     }
 
     public static Matcher<Element> hasChild(Matcher<Element> childMatcher) {
        return HasChildren.hasChild(childMatcher);
     }
 
     public static Matcher<Iterable<Element>> withSize(int size) {
         return Matchers.iterableWithSize(size);
     }
 }
