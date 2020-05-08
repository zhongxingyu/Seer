 package com.computas.sublima.app.index;
 
 import com.computas.sublima.app.service.URLActions;
 import com.computas.sublima.query.impl.DefaultSparqlDispatcher;
 import com.computas.sublima.query.service.MappingService;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import java.io.ByteArrayInputStream;
 import java.util.HashSet;
 
 /**
  * This class generates triples to be inserted for all resources that represent a media type.
  * The triple, sub:literals, is a concatenation of all configured literals that will make up
  * the content of which we do freetext search against in Mediasone.
  * <p/>
  * The class uses a configured SPARQL/SPARUL end point to perform all getting and inserting of data.
  *
  * @author: mha
  * Date: 03.des.2008
  */
 public class FreetextTriples {
 
   private DefaultSparqlDispatcher sparqlQuery = new DefaultSparqlDispatcher();
   private GenerateUtils gu = new GenerateUtils();
   private MappingService ms = new MappingService();
 
   public FreetextTriples() {
   }
 
 
   /**
    * This method takes the resource URI as input and returns a String representation of the sub:literals triple
    * to be inserted in the graph.
    *
    * @param uri                  String representation of the URI for the resource to generate sub:literals for
    * @param searchableProperties A string array with the properties to index
    * @param prefixes             A string array with the prefixes for the properties to index
    * @param graphs               graphname
    * @return String with sub:literals N3-triple
    */
   public String generateFreetextTripleForURI(String uri, String[] searchableProperties, String[] prefixes, String[] graphs, boolean indexExternalContent) {
     if (!uri.startsWith("<") && !uri.endsWith(">")) {
       uri = "<" + uri + ">";
     }
 
     String concatenatedSearchableText = getConcatenatedTextFromURI(uri, searchableProperties, prefixes, graphs);
 
     if (indexExternalContent) {
       String externalContent = getResourceExternalLiteralsAsString(uri);
       return uri + " <http://xmlns.computas.com/sublima#literals> \"" + concatenatedSearchableText + "\" .\n" + uri + " <http://xmlns.computas.com/sublima#externalliterals> \"" + concatenatedSearchableText + " " + externalContent + "\" .\n";
     } else {
       return concatenatedSearchableText.isEmpty() ? null : uri + " <http://xmlns.computas.com/sublima#literals> \"" + concatenatedSearchableText + "\" .";
     }
   }
 
   /**
    * This method queries the end point and returns a concatenated String with the searchable text
    *
    * @param uri           String representation of the URI for the resource to generate sub:literals for
    * @param fieldsToIndex String array representation of the fields of the resource to query
    * @param prefixes      Prefixes
    * @param graphs        graphname
    * @return String with contatenated text
    */
   public String getConcatenatedTextFromURI(String uri, String[] fieldsToIndex, String[] prefixes, String[] graphs) {
 
     if (!uri.startsWith("<") && !uri.endsWith(">")) {
       uri = "<" + uri + ">";
     }
 
     String query = gu.createSelectQueryToGetFields(uri, fieldsToIndex, prefixes, graphs);
     String xml = (String) sparqlQuery.query(query);
 
     StringBuilder results = new StringBuilder();
     HashSet<String> set = new HashSet<String>();
 
     try {
       DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
       Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
      XPathExpression expr = XPathFactory.newInstance().newXPath().compile("//td");
       NodeList nodes = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
 
       // I use set here to avoid getting the same results over and over. Seems like the query gives me a result that is repeated per n(Agent)
       // todo Check the SPARQL with Kjetil or David to check if we can get rid of the repeating results
       for (int i = 0; i < nodes.getLength(); i++) {
         if (!nodes.item(i).getTextContent().trim().isEmpty()) {
           set.add(nodes.item(i).getTextContent().trim());
         }
       }
 
       for (String text : set) {
         results.append(ms.charactermapping(text)).append(". ");
       }
 
     } catch (Exception e) {
       System.out.println("Could not get concatenated text of URI from XML result");
       e.printStackTrace();
     }
 
     return escapeString(results.toString());
   }
 
   /**
    * Method to get the external content as an triple for a specific resource including the internal literals
    *
    * @param uri
    * @return a String ie. "<http://theresource.net> sub:externalliterals """ This is the resource . net external content including internal content""""
    */
   public String getResourceExternalLiteralsAsString(String uri) {
     StringBuilder externalContent = new StringBuilder();
 
     URLActions urlAction = new URLActions(uri);
     String code = urlAction.getCode();
 
     if ("302".equals(code) ||
             "303".equals(code) ||
             "304".equals(code) ||
             "305".equals(code) ||
             "307".equals(code) ||
             code.startsWith("2")) {
       try {
 
         externalContent.append(urlAction.strippedContent(null).replace("\\", "\\\\"));
 
       } catch (Exception e) {
         System.out.println(e);
       }
     }
     return escapeString(externalContent.toString());
   }
 
   private String escapeString(String s) {
     StringBuilder sb = new StringBuilder();
     int i;
     char c;
     for (i = 0; i < s.length(); i++) {
       c = s.charAt(i);
       if (c >= 32 && c <= 127) { //<http://www.w3.org/2001/sw/RDFCore/ntriples/#character>
         if (c == 92 || c == 34 || c == 10 || c == 13 || c == 9) { //<http://www.w3.org/2001/sw/RDFCore/ntriples/#sec-string1>
           sb.append('\\');
           sb.append(c);
         } else {
           sb.append(c);
         }
       } else {
         String hexstr = Integer.toHexString(c).toUpperCase();
         int pad = 4 - hexstr.length();
         sb.append("\\u");
         for (; pad > 0; pad--)
           sb.append('0');
         sb.append(hexstr);
       }
     }
     return sb.toString();
   }
 
 }
