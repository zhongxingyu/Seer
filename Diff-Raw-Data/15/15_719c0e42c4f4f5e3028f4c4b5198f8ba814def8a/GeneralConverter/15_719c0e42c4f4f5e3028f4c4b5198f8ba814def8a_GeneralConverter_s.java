 /*
  * Created on Nov 17, 2005
  */
 package uk.org.ponder.conversion;
 
 import uk.org.ponder.mapping.DataAlterationRequest;
 
 /**
  * Converts objects to and from strings using one of a repertoire of strategies
  * (leaf conversion, XML or JSON)
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class GeneralConverter {
   private SerializationProvider XMLProvider;
   private SerializationProvider JSONProvider;
   private GeneralLeafParser leafParser;
   
   /**
    * @param provider the XMLProvider to set
    */
   public void setXMLProvider(SerializationProvider provider) {
     XMLProvider = provider;
   }
   /**
    * @param provider the JSONProvider to set
    */
   public void setJSONProvider(SerializationProvider provider) {
     JSONProvider = provider;
   }
   /**
    * @param leafParser the leafParser to set
    */
   public void setLeafParser(GeneralLeafParser leafParser) {
     this.leafParser = leafParser;
   }
   
   
   public String render(Object torender, String encoding) {
     if (encoding.equals(DataAlterationRequest.JSON_ENCODING)) {
       return JSONProvider.toString(torender);
     }
     else if (encoding.equals(DataAlterationRequest.XML_ENCODING)) {
         return XMLProvider.toString(torender);
       }
      else return leafParser.render(torender);
   }
 
   public Object parse(String toparse, Class targetclass, String encoding) {
    if (DataAlterationRequest.JSON_ENCODING.equals(encoding)) {
       return JSONProvider.fromString(toparse);
     }
     else if (DataAlterationRequest.XML_ENCODING.equals(encoding)) {
         return XMLProvider.fromString(toparse);
       }
    else return leafParser.parse(targetclass, toparse);
   }
 
 }
