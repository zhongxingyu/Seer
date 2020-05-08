 package org.geoserver.wps;
 
 import junit.framework.Test;
 
 import org.w3c.dom.Document;
import org.w3c.dom.Node;
 
 public class GetCapabilitiesTest extends WPSTestSupport {
 
     //read-only test
     public static Test suite() {
         return new OneTimeTestSetup(new GetCapabilitiesTest());
     }
     
     
     public void testGetBasic() throws Exception {
         Document d = getAsDOM( "wps?service=wps&request=getcapabilities" );
         print(d);
         basicCapabilitiesTest(d);
     }
 
     public void testPostBasic() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
                 + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
                 + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/>";
         Document d = postAsDOM(root(), request);
         basicCapabilitiesTest(d);
     }
     
     private void basicCapabilitiesTest(Document d) throws Exception {
         print(d);
         checkValidationErrors(d);
         
         assertEquals( "wps:Capabilities", d.getDocumentElement().getNodeName() );
         int np = d.getElementsByTagName( "wps:Process" ).getLength();
         assertTrue( np > 0 );
     }
 
     public void testUnsupportedVersionPost() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
                 + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
                 + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                 + "  <ows:AcceptVersions>" // 
                 + "    <ows:Version>9.9.9</ows:Version>" //
                 + "  </ows:AcceptVersions>" // 
                 + "</wps:GetCapabilities>";
         Document dom = postAsDOM(root(), request);
         
         checkValidationErrors(dom);
         checkOws11Exception(dom, "VersionNegotiationFailed");
     }
     
     public void testUnsupportedVersionGet() throws Exception {
         Document dom = getAsDOM(root() + "request=GetCapabilities&service=WPS&acceptVersions=9.9.9,8.8.8");
         
         checkValidationErrors(dom);
         checkOws11Exception(dom, "VersionNegotiationFailed");
     }
     
     public void testSupportedVersionGet() throws Exception {
         Document dom = getAsDOM(root() + "request=GetCapabilities&service=WPS&acceptVersions=0.5.0,1.0.0");
         assertEquals("wps:Capabilities", dom.getFirstChild().getNodeName());
     }
     
     public void testSupportedVersionPost() throws Exception {
         String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                 + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
                 + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
                 + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                 + "  <ows:AcceptVersions>" // 
                 + "    <ows:Version>0.5.0</ows:Version>" //
                 + "    <ows:Version>1.0.0</ows:Version>" //
                 + "  </ows:AcceptVersions>" // 
                 + "</wps:GetCapabilities>";
         Document dom = postAsDOM(root(), request);
         print( dom );
         assertEquals("wps:Capabilities", dom.getFirstChild().getNodeName());
     }
     
//    public void testUpdateSequenceInferiorGet() throws Exception {
//        Document dom = getAsDOM(root() + "request=GetCapabilities&service=WPS&updateSequence=-1");
//        basicCapabilitiesTest(dom);
//    }
//    
 //    public void testUpdateSequenceInferiorPost() throws Exception {
 //        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
 //                + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
 //                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 //                + " updateSequence=\"-1\"/>";
 //        Document dom = postAsDOM(root(), request);
 //        basicCapabilitiesTest(dom);
 //    }
 //    
 //    public void testUpdateSequenceEqualsGet() throws Exception {
//        Document dom = getAsDOM(root() + "request=GetCapabilities&service=WPS&updateSequence=0");
//        checkValidationErrors(dom);
 //        final Node root = dom.getFirstChild();
 //        assertEquals("wps:Capabilities", root.getNodeName());
 //        assertEquals(0, root.getChildNodes().getLength());
 //    }
 //    
 //    public void testUpdateSequenceEqualsPost() throws Exception {
 //        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
 //                + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
 //                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 //                + " updateSequence=\"0\"/>";
 //        Document dom = postAsDOM(root(), request);
//        checkValidationErrors(dom);
 //        final Node root = dom.getFirstChild();
 //        assertEquals("wcs:Capabilities", root.getNodeName());
 //        assertEquals(0, root.getChildNodes().getLength());
 //    }
 //    
 //    public void testUpdateSequenceSuperiorGet() throws Exception {
 //        Document dom = getAsDOM(root() + "request=GetCapabilities&service=WPS&updateSequence=1");
//        checkValidationErrors(dom);
 ////        print(dom);
 //        checkOws11Exception(dom);
 //    }
 //    
 //    public void testUpdateSequenceSuperiorPost() throws Exception {
 //        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
 //                + "<wps:GetCapabilities service=\"WPS\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" "
 //                + "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" "
 //                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
 //                + " updateSequence=\"1\"/>";
 //        Document dom = postAsDOM(root(), request);
//        checkValidationErrors(dom);
 ////        print(dom);
 //        checkOws11Exception(dom);
 //    }
 }
