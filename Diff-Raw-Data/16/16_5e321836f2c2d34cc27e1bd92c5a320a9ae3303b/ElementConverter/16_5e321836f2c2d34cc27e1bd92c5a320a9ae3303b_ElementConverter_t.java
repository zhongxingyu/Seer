 package com.rockwellautomation.verification.converter;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.rockwellautomation.verification.Performance.Element;
 import com.rockwellautomation.verification.element.VisitableElement;
 import com.rockwellautomation.verification.element.BooleanElement;
 import com.rockwellautomation.verification.element.IntegerElement;
 import com.rockwellautomation.verification.element.StringElement;
 
 /**
  * Converts an {@link Element} object tree to List of {@link VisitableElement} objects. The root element of the input
  * tree is disregarded as it is used simply as a container to hold the first level children which will become the roots
  * of the many small data trees that are returned.
  * 
  * @author JCase
  */
 public class ElementConverter {
 
   /**
    * Iterate over all the children of the root element of the data set and produce a List of VisitableElements
    * representing the same data.
    * 
    * @param root
    * @return
    */
   public static List<VisitableElement> convertMessageToElement(Element root) {
	List<VisitableElement> ret = new ArrayList<VisitableElement>(root.getChildrenCount());
 
     for (Element e : root.getChildrenList()) {
       ret.add(convertSingleElement(e));
     }
 
     return ret;
   }
 
   /**
    * Recursively iterate over the Element and its children and build up a mirroring VisitableElement
    * 
    * @param element
    *          The {@link Element} to convert
    * @return An {@link VisitableElement} representing the same data
    */
   private static VisitableElement convertSingleElement(Element element) {
     VisitableElement ret;
 
     if (element.hasExtension(Element.stringValue)) {
       ret = new StringElement(element.getExtension(Element.stringValue));
     }
     else if (element.hasExtension(Element.numericValue)) {
       ret = new IntegerElement(element.getExtension(Element.numericValue));
     }
     else if (element.hasExtension(Element.booleanValue)) {
       ret = new BooleanElement(element.getExtension(Element.booleanValue));
     }
     else {
       // ret = new BooleanElement(false); // DELETE THIS
       throw new IllegalArgumentException("Unknown Element type: " + element.toString());
     }
 
     for (Element child : element.getChildrenList()) {
       ret.addChild(ElementConverter.convertSingleElement(child));
     }
 
     return ret;
   }
 
 }
