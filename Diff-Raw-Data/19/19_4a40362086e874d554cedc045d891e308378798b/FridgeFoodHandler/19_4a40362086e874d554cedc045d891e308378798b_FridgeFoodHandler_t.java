 package com.openfridge;
 
 import java.util.ArrayList;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.util.Log;
 
 
 public class FridgeFoodHandler extends DefaultHandler
 {
     private String createdDateTime;
     private String description;
     private String expirationDate;
     private String id;
     private String lastUpdatedDateTime;
     private String userId;
     
     private ArrayList<FridgeFood> goodFoods = new ArrayList<FridgeFood>();
     private ArrayList<FridgeFood> nearFoods = new ArrayList<FridgeFood>();
     private ArrayList<FridgeFood> expiredFoods = new ArrayList<FridgeFood>();
     private TagParseState parseState;
     private ParseState expirationState;
 
     private enum ParseState {
         NO_EXPIRATION_STATE,
         NEAR,
         GOOD,
         EXPIRED
     }
     private enum TagParseState {
         NO_TAG,
         CREATE_TIME,
         DESCRIPTION,
         EXPIRATION,
         ID,
         LAST_UPDATE_TIME,
         USER_ID
     }
 
     // ===========================================================
     // Getter & Setter
     // ===========================================================
 
     public ArrayList<FridgeFood> getGoodFoods() {
         return goodFoods;
     }
     
     public ArrayList<FridgeFood> getNearFoods() {
         return nearFoods;
     }
     
     public ArrayList<FridgeFood> getExpiredFoods() {
         return expiredFoods;
     }
 
     // ===========================================================
     // Methods
     // ===========================================================
     @Override
     public void startDocument() throws SAXException {
         goodFoods.clear();
         nearFoods.clear();
         expiredFoods.clear();
         
         parseState = TagParseState.NO_TAG;
         expirationState = ParseState.NO_EXPIRATION_STATE;
         clearBuffers();
     }
 
     @Override
     public void endDocument() throws SAXException {
         // Nothing to do
     }
     
     private void clearBuffers() {
         createdDateTime = "";
         description = "";
         expirationDate = "";
         id = "";
         lastUpdatedDateTime = "";
         userId = "";
     }
 
     /** Gets be called on opening tags like: 
      * <tag> 
      * Can provide attribute(s), when xml was like:
      * <tag attribute="attributeValue">*/
     @Override
     public void startElement(String namespaceURI, String localName,
             String qName, Attributes atts) throws SAXException {
         if (localName.equals("created-at")) {
             parseState = TagParseState.CREATE_TIME;
         } else if (localName.equals("desc")) {
             parseState = TagParseState.DESCRIPTION;
         } else if (localName.equals("expiration")) {
             parseState = TagParseState.EXPIRATION;
         } else if (localName.equals("id")) {
             parseState = TagParseState.ID;
         } else if (localName.equals("updated-at")) {
             parseState = TagParseState.LAST_UPDATE_TIME;
         } else if (localName.equals("user_id")) {
             parseState = TagParseState.USER_ID;
         } else if (localName.equals("good")) {
             expirationState = ParseState.GOOD;
         } else if (localName.equals("near")) {
             expirationState = ParseState.NEAR;
         } else if (localName.equals("expired")) {
             expirationState = ParseState.EXPIRED;
         }
     }
     
     /** Gets be called on closing tags like: 
      * </tag> */
     @Override
     public void endElement(String namespaceURI, String localName, String qName)
             throws SAXException {
         if (localName.equals("food")) {
             // Uncomment for long format FridgeFood record
             //FridgeFood newFood = new FridgeFood(createdDateTime, description, 
             //        expirationDate, id, lastUpdatedDateTime, userId);
             
             FridgeFood newFood = new FridgeFood(description, expirationDate, 
                     userId);
             
             switch (expirationState) {
             case NEAR:
                 nearFoods.add(newFood);
                 break;
             case GOOD:
                 goodFoods.add(newFood);
                 break;
             case EXPIRED:
                 expiredFoods.add(newFood);
                 break;
             default:
                 throw new RuntimeException();
             }
             
             parseState = TagParseState.NO_TAG;
             clearBuffers();
         }
     }
     
     /** Gets be called on the following structure: 
      * <tag>characters</tag> */
     @Override
     public void characters(char ch[], int start, int length) {
         switch (parseState) {
         case CREATE_TIME:
             createdDateTime += new String(ch, start, length);
             break;
         case DESCRIPTION:
             description += new String(ch, start, length);
             break;
         case EXPIRATION:
             expirationDate += new String(ch, start, length);
             break;
         case ID:
             id += new String(ch, start, length);
             break;
         case LAST_UPDATE_TIME:
             lastUpdatedDateTime += new String(ch, start, length);
             break;
         case USER_ID:
             userId += new String(ch, start, length);
             break;
         default:
         }
     }
 
 }
