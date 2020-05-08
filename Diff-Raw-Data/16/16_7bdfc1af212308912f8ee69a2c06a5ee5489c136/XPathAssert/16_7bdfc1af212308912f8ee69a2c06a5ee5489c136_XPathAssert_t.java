 package org.codehaus.xfire.test;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import junit.framework.Assert;
 
 import org.codehaus.xfire.soap.Soap11;
 import org.codehaus.xfire.soap.Soap12;
 import org.codehaus.yom.Node;
 import org.codehaus.yom.xpath.YOMXPath;
 import org.jaxen.XPath;
 
 /**
  * WebService assertions.
  * 
  * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
  */
 public class XPathAssert
 {
     /**
      * Assert that the following XPath query selects one or more nodes.
      * 
      * @param xpath
      */
     public static List assertValid(String xpath, Node node, Map namespaces)
         throws Exception
     {
         List nodes = createXPath(xpath, namespaces).selectNodes(node);
 
         if (nodes.size() == 0)
         {
             throw new Exception("Failed to select any nodes for expression:.\n" + xpath + "\n"
                     + node.toXML());
         }
 
         return nodes;
     }
 
     /**
      * Assert that the following XPath query selects no nodes.
      * 
      * @param xpath
      */
     public static List assertInvalid(String xpath, Node node, Map namespaces)
         throws Exception
     {
         List nodes = createXPath(xpath, namespaces).selectNodes(node);
 
         if (nodes.size() > 0)
         {
             throw new Exception("Found multiple nodes for expression:\n" + xpath + "\n"
                     + node.toXML());
         }
         
         return nodes;
     }
 
     /**
      * Asser that the text of the xpath node retrieved is equal to the value
      * specified.
      * 
      * @param xpath
      * @param value
      * @param node
      */
     public static void assertXPathEquals(String xpath, String value, Node node, Map namespaces)
         throws Exception
     {
         String value2 = ((Node) createXPath( xpath, namespaces ).selectSingleNode( node )).getValue().trim();
         
         Assert.assertEquals( value, value2 );
     }
 
     public static void assertNoFault(Node node)
         throws Exception
     {
         Map namespaces = new HashMap();
         namespaces.put("s", Soap11.getInstance().getNamespace());
         namespaces.put("s12", Soap12.getInstance().getNamespace());
        
         assertInvalid("/s:Envelope/s:Body/s:Fault", node, namespaces);
        assertInvalid("/s12:Envelope/s12:Body/s12:Fault", node, namespaces);
     }
 
    public static void assertFault(Node node)
        throws Exception
    {
        Map namespaces = new HashMap();
        namespaces.put("s", Soap11.getInstance().getNamespace());
        namespaces.put("s12", Soap12.getInstance().getNamespace());
        
        assertValid("/s:Envelope/s:Body/s:Fault", node, namespaces);
        assertValid("/s12:Envelope/s12:Body/s12:Fault", node, namespaces);
    }
    
     /**
      * Create the specified XPath expression with the namespaces added via
      * addNamespace().
      */
     public static XPath createXPath( String xpathString, Map namespaces ) 
         throws Exception
     {
         XPath xpath = new YOMXPath( xpathString );
         
         for ( Iterator itr = namespaces.keySet().iterator(); itr.hasNext(); )
         {
             String ns = (String) itr.next();
             xpath.addNamespace(ns, (String) namespaces.get(ns));
         }
     
         return xpath;
     }
 }
