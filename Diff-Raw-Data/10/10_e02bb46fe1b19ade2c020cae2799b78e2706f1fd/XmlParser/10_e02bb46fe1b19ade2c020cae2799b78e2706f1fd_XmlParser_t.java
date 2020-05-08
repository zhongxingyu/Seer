 package org.cophm.validation;
 
 import org.apache.log4j.Logger;
 import org.cophm.util.XMLDefs;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created by
  * User: ralph
  * Date: 4/18/12
  * Time: 9:32 PM
  */
 public class XmlParser extends Parser implements IDataParser {
 
     private Document    doc;
     private Element     root;
 
     private Logger      log = Logger.getLogger(this.getClass().getName());
 
 
     public XmlParser() {
     }
 
     public void loadData(String data) throws IOException {
         SAXBuilder      builder = new SAXBuilder();
         String          xmlHeader;
 
         if(data.startsWith("<?")) {
             xmlHeader = "";
         }
         else {
             xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
         }
 
         try {
             doc = builder.build(new StringReader(xmlHeader + data));
 
             root = doc.getRootElement();
         }
         catch(JDOMException e) {
             log.error("Caught a " + e.getClass().getName() + ": " + e.toString());
             log.error("XML Data = [" + data + "]");
             throw new IOException(e.getMessage());
         }
     }
 
     public boolean  isSegmentPresent(String  segmentName) {
         Element     segmentElement;
 
         segmentElement = root.getChild(segmentName, root.getNamespace());
 
         if(segmentElement == null) {
             return false;
         }
 
         return true;
     }
 
     public String getFieldValue(List<Element> locationList) throws HL7ValidatorException {
         List<String>    valuesToReturn;
 
         valuesToReturn = getAllFieldValues(locationList);
 
         if(valuesToReturn.size() == 0) {
             return "";
         }
         else {
             return valuesToReturn.get(0);
         }
     }
 
     public List<String>  getAllFieldValues(List<Element> locationList) throws HL7ValidatorException {
         String          segmentName;
         String          fieldNumber;
         Element         location;
         Iterator        locationIterator;
         boolean         multipleValuesAllowed;
         String          singleValue;
         List<String>    allValues = new ArrayList<String>();
 
         locationIterator = locationList.iterator();
 
         while(locationIterator.hasNext()) {
             location = (Element)locationIterator.next();
             segmentName = location.getChildText(XMLDefs.HL7_SEGMENT, location.getNamespace()).trim();
             fieldNumber = location.getChild(XMLDefs.HL7_SEGMENT,
                                             location.getNamespace()).getAttributeValue(XMLDefs.FIELD_NUMBER,
                                                                                        location.getNamespace());
             multipleValuesAllowed = Boolean.parseBoolean(((Element)(location.getParent()))
                                                                  .getAttributeValue(XMLDefs.CAN_CONTAIN_MULTIPLE_VALUES,
                                                                                     location.getNamespace(), "false"));
 
             singleValue = getFieldValue(segmentName, location, fieldNumber, allValues);
             //
             // There are two ways data gets returned from getFieldValue.  If multiple values are allowed,
             // then any values found are returned in the allValues List that gets passed in.  If multiple
             // values are not allowed, then a single value is returned as a return value.  Because of this,
             // we need to add the single value to the allValues List if multiples are not allowed.
             //
             // This should be refactored and is this way only because there are only a couple of weeks left
             // before the contract ends and the refactoring (and retesting) effort will take longer than that.
             // Adding to this is the fact that our only tester, which we are sharing with another account,
             // is already over tasked by the other account.
             //
             if(multipleValuesAllowed == false && singleValue.length() > 0) {
                 allValues.add(singleValue);
             }
         }
 
         return allValues;
     }
 
     public String getFieldValue(String segmentName, String fieldNumber) {
 
         return getFieldValue(segmentName, null, fieldNumber, null);
     }
 
     public String getFieldValue(String segmentName, Element  location, String fieldNumber) {
 
         return getFieldValue(segmentName, location, fieldNumber, null);
     }
 
     public List<String>  getFieldValues(String segmentName, String fieldNumber) {
         ArrayList<String>     listOfValues = new ArrayList<String>();
 
         getFieldValue(segmentName, null, fieldNumber, listOfValues);
 
         return listOfValues;
     }
 
     protected String getFieldValue(String segmentName, Element  location,
                                    String fieldNumber, List<String>  allValues) {
         Element           segment = null;
         Element           field;
         Element           subField;
         String            fieldName;
         Iterator          identifierIterator;
         List<Element>     segmentList;
         List<Element>     identifiers;
         Iterator          segmentIterator;
         boolean           matchFound = false;
         boolean           multipleValuesAllowed;
         boolean           repeatingSegment;
         List<Element>     fields;
         Iterator          fieldsIterator;
 
         segmentList = root.getChildren(segmentName, root.getNamespace());
 
         if(segmentList.size() == 0) {
             return "";
         }
 
         if(location != null) {
             multipleValuesAllowed = Boolean.parseBoolean(((Element)(location.getParent()))
                                                                  .getAttributeValue(XMLDefs.CAN_CONTAIN_MULTIPLE_VALUES,
                                                                                     location.getNamespace(), "false"));
 
             repeatingSegment = ((Element)(location.getParent())).getAttributeValue(XMLDefs.REPEATING_ELEMENT,
                                                                                     location.getNamespace(),
                                                                                     RepeatingElement.Field.name())
                                                                  .equals(RepeatingElement.Segment.name());
 
             identifiers = location.getChildren(XMLDefs.IDENTIFIER, location.getNamespace());
         }
         else {
             //
             // Set default values.
             //
             multipleValuesAllowed = false;
             repeatingSegment = false;
             identifiers = null;
         }
 
         if(identifiers == null || identifiers.size() == 0) {
             if(multipleValuesAllowed && allValues != null && repeatingSegment) {
                 segmentIterator = segmentList.iterator();
                 while(segmentIterator.hasNext()) {
                     segment = (Element)segmentIterator.next();
 
                     allValues.add(getFieldValue(segment, fieldNumber));
                 }
 
                 return "";
             }
             else {
                 segment = segmentList.get(0);
             }
         }
         else {
             //
             //  Implementation of identifiers.
             //
             matchFound = false;
 
             segmentIterator = segmentList.iterator();
              while(segmentIterator.hasNext()) {
                  Element    segmentToCheck = (Element)segmentIterator.next();
 
                  identifierIterator = identifiers.iterator();
                  while(identifierIterator.hasNext()) {
                      Element    identifier = (Element)identifierIterator.next();
                      String     matchValue;
 //                     boolean    mustMatch;
                      String     idFieldNumberStr;
                      String     valueToCompare;
                      String     fieldValue;
                      Iterator   fieldListIterator;
 
                      matchValue = identifier.getText().trim();
                      idFieldNumberStr = identifier.getAttributeValue(XMLDefs.FIELD_NUMBER,
                                                                      identifier.getNamespace());
 //                     mustMatch = identifier.getAttributeValue(XMLDefs.MUST_MATCH,
 //                                                              identifier.getNamespace(), "true").equalsIgnoreCase("true");
 
                     if(multipleValuesAllowed) {
                         fieldListIterator = getAllFields(segmentToCheck, fieldNumber).iterator();
                         while(fieldListIterator.hasNext()) {
                             Element fieldElement = (Element)fieldListIterator.next();
 
                             fieldName = fieldElement.getName() + "." + getSubFieldNumber(idFieldNumberStr);
                             subField = fieldElement.getChild(fieldName, fieldElement.getNamespace());
                             valueToCompare = subField.getText().trim();
                             if(valueToCompare.equals(matchValue)) {
                                 fieldName = fieldElement.getName() + "." + getSubFieldNumber(fieldNumber);
                                 fieldValue = fieldElement.getChildText(fieldName, fieldElement.getNamespace());
                                 if(allValues != null) {
                                     allValues.add(fieldValue.trim());
                                 }
                                 else {
                                     segment = segmentToCheck;
                                     matchFound = true;
                                     return fieldValue.trim();
                                 }
                             }
                             else {
 //                                if(mustMatch) {
                                     matchFound = false;
 //                                    break;
 //                                }
                             }
                         }
                     }
                     else {
                         fields = getAllFields(segmentToCheck, idFieldNumberStr);
                         fieldsIterator = fields.iterator();
                         while(fieldsIterator.hasNext()) {
                             Element       fieldToCheck = (Element)fieldsIterator.next();
 
                             fieldName = fieldToCheck.getName() + "." + getSubFieldNumber(idFieldNumberStr);
                             subField = fieldToCheck.getChild(fieldName, fieldToCheck.getNamespace());
                             if(subField != null && subField.getText() != null
                                 && subField.getText().trim().equals(matchValue)) {
                                 int       subFieldNumber;
 
                                 fieldValue = getFieldValue(segmentToCheck, fieldNumber);
 
 //                                subFieldNumber = getSubFieldNumber(fieldNumber);
 //                                if(subFieldNumber > 0) {
 //                                    fieldName = fieldToCheck.getName() + "." + subFieldNumber;
 //                                    subField = fieldToCheck.getChild(fieldName, fieldToCheck.getNamespace());
 //                                }
 //                                else {
 //                                    fieldName = fieldNumber + ".1";
 //                                    subField = fieldToCheck.getChild(fieldName, fieldToCheck.getNamespace());
 //                                }
 
                                 if(multipleValuesAllowed) {
                                     allValues.add(fieldValue.trim());
                                 }
                                 else {
                                     return fieldValue.trim();
                                 }
                             }
                         }
                     }
                 }
 
                 if(matchFound) {
                     break;
                 }
 
              }
 
             if(matchFound == false) {
                 return "";
             }
         }
 
         if(segment == null) {
             return "";
         }
 
         if(multipleValuesAllowed) {
             fields = getAllFields(segment, fieldNumber);
             fieldName = segment.getName() + "." + fieldNumber;
             fieldsIterator = fields.iterator();
             while(fieldsIterator.hasNext()) {
                 Element fieldElement = (Element)fieldsIterator.next();
                String  value;
 
                value = fieldElement.getChildText(fieldName, fieldElement.getNamespace());

                if(value == null) {
                    value = fieldElement.getChildText(fieldName + ".1", fieldElement.getNamespace());
                }

                allValues.add(value == null ? "" : value.trim());
             }
 
             return "";
         }
         else {
             return getFieldValue(segment, fieldNumber).trim();
         }
 
 //        fieldNumberInt = getFieldNumber(fieldNumber);
 //
 //        fieldName = segmentName + "." + fieldNumberInt;
 //
 //        field = segment.getChild(fieldName, segment.getNamespace());
 //
 //        if(field == null) {
 //            return "";
 //        }
 //
 //        //
 //        // In order to maintain compatibility with the way HL7's Pipe Delimited rules,
 //        // we will look for a "*.1" child Element because in the Pipe Delimited format,
 //        // MSG|...|MSGID001|... is equal to MSG|...|MASGID001^|... but in XML format
 //        // it must be portrayed as <MSG.12.1>, even though it might be referenced as
 //        // MSG.12 (i.e. segment name = MSG and field number = 12).
 //        //
 //        if(getSubFieldNumber(fieldNumber) == -1) {
 //            value = field.getText().trim();
 //            if(value.length() == 0) {
 //                value = field.getChildText(fieldName + ".1", field.getNamespace());
 //                if(value == null) {
 //                    value = "";
 //                }
 //            }
 //            return value.trim();
 //        }
 //
 //        fieldName = fieldName + "." + getSubFieldNumber(fieldNumber);
 //
 //        subField = field.getChild(fieldName, field.getNamespace());
 //
 //        if(subField != null) {
 //            return subField.getText().trim();
 //        }
 //
 //        return "";
     }
 
     private List<Element> getAllFields(Element segment, String fieldNumberStr) {
         String            fieldName;
         List<Element>     fields;
 
         fieldName = segment.getName() + "." + getFieldNumber(fieldNumberStr);
 
         fields = segment.getChildren(fieldName, segment.getNamespace());
 
         return fields;
     }
 
     private String getFieldValue(Element segment, String fieldNumberStr) {
         int           subFiledNumber;
         Element       fieldElement;
         Element       subFieldElement;
         String        fieldName;
         String        fieldValue;
 
         fieldName = segment.getName() + "." + getFieldNumber(fieldNumberStr);
 
         fieldElement = segment.getChild(fieldName, segment.getNamespace());
 
         if(fieldElement == null) {
             return "";
         }
 
         subFiledNumber = getSubFieldNumber(fieldNumberStr);
 
         if(subFiledNumber == -1) {
             fieldValue = fieldElement.getText().trim();
 
             //
             // In order to maintain compatibility with the way HL7's Pipe Delimited rules,
             // we will look for a "*.1" child Element because in the Pipe Delimited format,
             // MSG|...|MSGID001|... is equal to MSG|...|MASGID001^|... but in XML format
             // it must be portrayed as <MSG.12.1>, even though it might be referenced as
             // MSG.12 (i.e. segment name = MSG and field number = 12).
             //
             if(fieldValue == null || fieldValue.length() == 0) {
                 fieldName = fieldName + ".1";
                 subFieldElement = fieldElement.getChild(fieldName, fieldElement.getNamespace());
 
                 if(subFieldElement == null) {
                     return "";
                 }
                 else {
                     return subFieldElement.getText().trim();
                 }
             }
 
             return fieldElement.getText().trim();
         }
 
 
         fieldName = fieldName + "." + subFiledNumber;
 
         subFieldElement = fieldElement.getChild(fieldName, fieldElement.getNamespace());
 
         if(subFieldElement == null) {
             if(subFiledNumber == 1) {
                 return fieldElement.getText().trim();
             }
             else {
                 return "";
             }
         }
 
         return subFieldElement.getText().trim();
     }
 }
