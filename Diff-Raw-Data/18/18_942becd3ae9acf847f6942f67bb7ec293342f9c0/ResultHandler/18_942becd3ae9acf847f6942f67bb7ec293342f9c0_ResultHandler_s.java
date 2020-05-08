 package br.org.indt.ndg.mobile.xmlhandle;
 
 import br.org.indt.ndg.lwuit.model.BoolAnswer;
 import br.org.indt.ndg.lwuit.model.CategoryAnswer;
 import br.org.indt.ndg.lwuit.model.ChoiceAnswer;
 import br.org.indt.ndg.lwuit.model.DateAnswer;
 import br.org.indt.ndg.lwuit.model.DecimalAnswer;
 import br.org.indt.ndg.lwuit.model.ImageAnswer;
 import br.org.indt.ndg.lwuit.model.ImageData;
 import br.org.indt.ndg.lwuit.model.IntegerAnswer;
 import br.org.indt.ndg.lwuit.model.NDGAnswer;
 import br.org.indt.ndg.lwuit.model.DescriptiveAnswer;
 import br.org.indt.ndg.lwuit.model.TimeAnswer;
 import br.org.indt.ndg.mobile.logging.Logger;
 import br.org.indt.ndg.mobile.multimedia.Base64Coder;
 import java.util.Hashtable;
 import java.util.Stack;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import br.org.indt.ndg.mobile.structures.ResultStructure;
 import java.util.Calendar;
 import javax.microedition.location.Coordinates;
 
 public class ResultHandler extends DefaultHandler {
     private ResultStructure result;
     private Stack tagStack = new Stack();
     private NDGAnswer currentAnswer;
     private CategoryAnswer answers=null;
 
     private String currentOtherIndex = null;
     private Coordinates currentCoordinates = null;
     private boolean binary = false;
     private int subCategory = 1;
 
     public ResultHandler() {}
 
     public void setResultStructure(ResultStructure _structure) {
         this.result = _structure;
     }
 
     public void startDocument() throws SAXException {
     }
 
     public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
         if (qName.equals("result")) {
         } else if (qName.equals("category")) {
             answers = new CategoryAnswer();
             String name = attributes.getValue(attributes.getIndex("name"));
             String id = attributes.getValue(attributes.getIndex("id"));
             answers.setName( name );
             answers.setId( id );
         } else if (qName.equals("subcategory")) {
            String id = attributes.getValue(attributes.getIndex("id"));
             subCategory = Integer.parseInt(id);
         } else if (qName.equals("answer")) {
             String _type = attributes.getValue(attributes.getIndex("type"));
             String _id = attributes.getValue(attributes.getIndex("id"));
             String _visted = attributes.getValue(attributes.getIndex("visited"));
 
             if (_type.equals("_str")) currentAnswer = new DescriptiveAnswer();
             else if (_type.equals("_date")) currentAnswer = new DateAnswer();
             else if (_type.equals("_time")){
                 currentAnswer = new TimeAnswer();
                String sconvention = attributes.getValue(attributes.getIndex("convention")) ;
                 long convention  = 0;
                 if(sconvention.equals("am")){
                     convention = 1;
                 }else if(sconvention.equals("pm")){
                     convention = 2;
                 }
 
                 ((TimeAnswer)currentAnswer).setAmPm24(convention);
 
             }
             else if (_type.equals("_int")) currentAnswer = new IntegerAnswer();
             else if (_type.equals("_decimal")) currentAnswer = new DecimalAnswer();
             else if (_type.equals("_choice")) currentAnswer = new ChoiceAnswer();
             else if(_type.equals("_img")) currentAnswer = new ImageAnswer();
 
             currentAnswer.setType(_type);
             currentAnswer.setId(Integer.parseInt(_id));
             currentAnswer.setVisited(_visted);
         } else if (qName.equals("other")) {
             currentOtherIndex = attributes.getValue(0);
             ((ChoiceAnswer) currentAnswer).addSelectedIndex(currentOtherIndex);
         } else if (qName.equals("img_data")) {
             String latitude = attributes.getValue(attributes.getIndex("latitude"));
             String longitude = attributes.getValue(attributes.getIndex("longitude"));
             if ( attributes.getIndex("type") >= 0 ) {
                 binary = attributes.getValue(attributes.getIndex("type")).equals("binary");
             } else {
                 binary = false;
             }
 
             if( latitude != null && longitude != null ) {
                 currentCoordinates = new Coordinates(Double.parseDouble(latitude), Double.parseDouble(longitude), 0);
             }
         }
 
         tagStack.push(qName);
     }
 
     private long timeStamp2Long(String time,long convention){
         int ix = time.indexOf(':');
         int hour = Integer.parseInt(time.substring(0,ix));
         int min  = Integer.parseInt(time.substring(ix+1));
         Calendar calendar = Calendar.getInstance();
         if(convention == 0 || convention == 24 ){
             calendar.set(Calendar.HOUR_OF_DAY, hour);
         }else{
             calendar.set(Calendar.HOUR, hour);
         }
         calendar.set(Calendar.MINUTE, min);
 
         return calendar.getTime().getTime();
     }
 
     public void characters(char[] ch, int start, int length) throws SAXException {
         String chars = new String(ch, start, length).trim();
         if (chars.length() > 0) {
             String qName = (String)tagStack.peek();
 
             if (qName.equals("str"))
                 ((DescriptiveAnswer) currentAnswer).setValue(chars);
             else if (qName.equals("longitude")) {
                 result.setLongitude(chars);
             } else if (qName.equals("latitude")) {
                 result.setLatitude(chars);
             }
             else if (qName.equals("date")) ((DateAnswer) currentAnswer).setDate(Long.parseLong(chars));
             else if (qName.equals("time")) ((TimeAnswer) currentAnswer).setTime(timeStamp2Long(chars,((TimeAnswer) currentAnswer).getAmPm24()));
             else if (qName.equals("int")) {
                 try
                 {
                     ((IntegerAnswer) currentAnswer).setValue(chars);
                 }
                 catch(NumberFormatException ex)
                 {}
             }
             else if (qName.equals("decimal"))
                 try
                 {
                     ((DecimalAnswer) currentAnswer).setValue(chars);
                 }
                 catch ( NumberFormatException ex )
                 {}
             else if (qName.equals("index")) ((BoolAnswer) currentAnswer).setIndex(chars);
             else if (qName.equals("item")){
                 ((ChoiceAnswer) currentAnswer).addSelectedIndex(chars);
             }
             else if (qName.equals("other")) {
                 ((ChoiceAnswer) currentAnswer).addOtherText(currentOtherIndex, chars);
             }
             else{
                 //ATTENTION
                 //This "else" means that is a new type of Answer from a new type
                 //of Question
                 //USE Answer that extends br.org.indt.ndg.lwuit.model.Answer
                 //using its setValue()
                 //This is necessary from version 2.0 on.
                 if (qName.equals("img_data")){
                     if(chars != null && chars.length() > 0 && !chars.equals(" ") )
                     {
                         if( !binary ) {
                             byte[] imgData = Base64Coder.decode(chars);
                             ((ImageAnswer) currentAnswer).getImages().addElement( new ImageData(imgData, currentCoordinates) );
                             currentCoordinates = null;
                         } else {
                             ((ImageAnswer) currentAnswer).getImages().addElement( new ImageData( chars, currentCoordinates) );
                         }
                     }
                 }
             }
         }
     }
 
     public void endElement(String uri, String localName, String qName) throws SAXException {
 
         if (qName.equals("category")) {
             result.addAnswer(answers);
         } else if (qName.equals("answer")) {
             answers.put( subCategory-1, String.valueOf(currentAnswer.getId()), currentAnswer);
         }
         tagStack.pop();
     }
 
     public void endDocument() throws SAXException {}
 }
