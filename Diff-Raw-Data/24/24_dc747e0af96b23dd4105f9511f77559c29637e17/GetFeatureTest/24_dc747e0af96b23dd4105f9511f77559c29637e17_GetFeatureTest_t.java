 package org.geoserver.wfs;
 
 import junit.framework.Test;
 
import org.custommonkey.xmlunit.XMLAssert;
 import org.geoserver.data.test.MockData;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 public class GetFeatureTest extends WFSTestSupport {
     
     /**
      * This is a READ ONLY TEST so we can use one time setup
      */
     public static Test suite() {
         return new OneTimeTestSetup(new GetFeatureTest());
     }
     
     public void testGet() throws Exception {
     	testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=1.0.0&service=wfs");
     }
     
     public void testGetPropertyNameEmpty() throws Exception {
     	testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=1.0.0&service=wfs&propertyname=");
     }
     
     public void testGetPropertyNameStar() throws Exception {
     	testGetFifteenAll("wfs?request=GetFeature&typename=cdf:Fifteen&version=1.0.0&service=wfs&propertyname=*");
     }
     
     private void testGetFifteenAll(String request) throws Exception {
         Document doc = getAsDOM(request);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList featureMembers = doc.getElementsByTagName("gml:featureMember");
         assertFalse(featureMembers.getLength() == 0);
     }
     
    // see GEOS-1893
    public void testGetMissingParams() throws Exception {
        Document doc = getAsDOM("wfs?request=GetFeature&typeNameWrongParam=cdf:Fifteen&version=1.0.0&service=wfs");
        // trick: the document specifies a namespace with schema reference, as a result xpath expressions
        // do work only if fully qualified
        XMLAssert.assertXpathEvaluatesTo("1", "count(//ogc:ServiceException)", doc);
        XMLAssert.assertXpathEvaluatesTo("MissingParameterValue", "//ogc:ServiceException/@code", doc);
    }
    
     public void testAlienNamespace() throws Exception {
         // if the namespace is not known, complain with a service exception
         Document doc = getAsDOM("wfs?request=GetFeature&typename=youdontknowme:Fifteen&version=1.0.0&service=wfs");
         assertEquals("ServiceExceptionReport", doc.getDocumentElement()
                 .getNodeName());
     }
     
     public void testPost() throws Exception {
 
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.0.0\" "
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
                 + "<wfs:Query typeName=\"cdf:Other\"> "
                 + "<ogc:PropertyName>cdf:string2</ogc:PropertyName> "
                 + "</wfs:Query> " + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
 
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList featureMembers = doc.getElementsByTagName("gml:featureMember");
         assertFalse(featureMembers.getLength() == 0);
 
     }
 
     public void testPostWithFilter() throws Exception {
 
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.0.0\" " + "outputFormat=\"GML2\" "
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" > "
                 + "<wfs:Query typeName=\"cdf:Other\"> "
                 + "<ogc:PropertyName>cdf:string2</ogc:PropertyName> "
                 + "<ogc:Filter> " + "<ogc:PropertyIsEqualTo> "
                 + "<ogc:PropertyName>cdf:integers</ogc:PropertyName> "
                 + "<ogc:Add> " + "<ogc:Literal>4</ogc:Literal> "
                 + "<ogc:Literal>3</ogc:Literal> " + "</ogc:Add> "
                 + "</ogc:PropertyIsEqualTo> " + "</ogc:Filter> "
                 + "</wfs:Query> " + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
 
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList featureMembers = doc.getElementsByTagName("gml:featureMember");
         assertFalse(featureMembers.getLength() == 0);
     }
     
     public void testLax() throws Exception {
         String xml = 
             "<GetFeature xmlns:gml=\"http://www.opengis.net/gml\">" +  
             " <Query typeName=\"" + MockData.BUILDINGS.getLocalPart() + "\">" + 
             "   <PropertyName>ADDRESS</PropertyName>" + 
             "   <Filter>" + 
             "     <PropertyIsEqualTo>" + 
             "       <PropertyName>ADDRESS</PropertyName>" + 
             "       <Literal>123 Main Street</Literal>" + 
             "     </PropertyIsEqualTo>" + 
             "   </Filter>" + 
             " </Query>" + 
             "</GetFeature>";
         
         Document doc = postAsDOM( "wfs", xml );
         //print( doc );
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList featureMembers = doc.getElementsByTagName("cite:Buildings");
         assertEquals(1,featureMembers.getLength());
     }
 
 }
