 package edu.northwestern.bioinformatics.studycalendar.xml.writers;
 
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
 import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
 import org.apache.commons.lang.StringUtils;
 import org.dom4j.Element;
 
 public class PropertyChangeXmlSerializer extends AbstractChangeXmlSerializer {
     public static final String PROPERTY_CHANGE = "property-change";
     private static final String OLD_VALUE = "old-value";
     private static final String NEW_VALUE = "new-value";
     private static final String PROPERTY_NAME = "property-name";
 
     protected Change changeInstance() {
         return new PropertyChange();
     }
 
     protected String elementName() {
         return PROPERTY_CHANGE;
     }
 
 
     protected void addAdditionalAttributes(final Change change, Element element) {
        String oldValue = ((PropertyChange) change).getOldValue();
        String newValue = ((PropertyChange) change).getNewValue();
        if (oldValue == null) {
            oldValue = "";
        }
        if (newValue == null) {
            newValue = "";
        }
         element.addAttribute(PROPERTY_NAME, ((PropertyChange) change).getPropertyName());
        element.addAttribute(OLD_VALUE, oldValue);
        element.addAttribute(NEW_VALUE, newValue);
     }
 
     protected void setAdditionalProperties(final Element element, Change change) {
         ((PropertyChange) change).setPropertyName(element.attributeValue(PROPERTY_NAME));
         ((PropertyChange) change).setOldValue(element.attributeValue(OLD_VALUE));
         ((PropertyChange) change).setNewValue(element.attributeValue(NEW_VALUE));
     }
 
     @Override
     public StringBuffer validateElement(Change change, Element eChange) {
         if (change == null && eChange == null) {
             return new StringBuffer("");
         } else if ((change == null && eChange != null) || (change != null && eChange == null)) {
             return new StringBuffer("either change or element is null");
         }
 
         StringBuffer errorMessageStringBuffer = super.validateElement(change, eChange);
 
 
         String expectedPropertyName = ((PropertyChange) change).getPropertyName();
         String oldValue = ((PropertyChange) change).getOldValue();
         String newValue = ((PropertyChange) change).getNewValue();
         if (!StringUtils.equals(expectedPropertyName, eChange.attributeValue(PROPERTY_NAME))) {
             errorMessageStringBuffer.append(String.format("property name is different. expected:%s , found (in imported document) :%s \n",
                     expectedPropertyName, eChange.attributeValue(PROPERTY_NAME)));
         } else if (!StringUtils.equals(oldValue, eChange.attributeValue(OLD_VALUE))) {
             errorMessageStringBuffer.append(String.format("old value  is different. expected:%s , found (in imported document) :%s \n",
                     oldValue, eChange.attributeValue(OLD_VALUE)));
         } else if (!StringUtils.equals(newValue, eChange.attributeValue(NEW_VALUE))) {
             errorMessageStringBuffer.append(String.format("new value is different. expected:%s , found (in imported document) :%s \n",
                     newValue, eChange.attributeValue(NEW_VALUE)));
         }
 
 
         return errorMessageStringBuffer;
     }
 
 }
