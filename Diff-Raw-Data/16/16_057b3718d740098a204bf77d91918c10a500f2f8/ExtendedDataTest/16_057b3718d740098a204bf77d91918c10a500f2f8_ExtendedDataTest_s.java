 package org.geoserver.wms.responses.map.kml;
 
 import javax.xml.namespace.QName;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.wms.WMSTestSupport;
 import org.vfny.geoserver.wms.responses.map.kml.OWS5MapProducerFactory;
 import org.w3c.dom.Document;
 
 public class ExtendedDataTest extends WMSTestSupport {
 
     private static final QName ONLYGEOM = new QName(MockData.CITE_URI, "OnlyGeom",
                     MockData.CITE_PREFIX);
 
     @Override
     protected void populateDataDirectory(MockData dataDirectory) throws Exception {
         dataDirectory.addWellKnownTypes(new QName[] { MockData.BUILDINGS });
        dataDirectory.addPropertiesType(ONLYGEOM, getClass().getResource("OnlyGeom.properties"), null, null);
     }
 
     public void testDefaultOutput() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + MockData.BUILDINGS.getPrefix()
                 + ":" + MockData.BUILDINGS.getLocalPart()
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&featureid=Buildings.1107531701010");
         print(doc);
 
         // we should have just one feature
         assertEquals(1, doc.getElementsByTagName("Placemark").getLength());
         // gml3 encoding uses poslist instead of coordinates
 //        assertEquals(1, doc.getElementsByTagName("posList").getLength());
         assertEquals(2, doc.getElementsByTagName("coordinates").getLength());
         // by default we have style, but no extended data
         assertEquals(1, doc.getElementsByTagName("Style").getLength());
         assertEquals(0, doc.getElementsByTagName("Schema").getLength());
     }
 
     public void testExtendedDataNoStyle() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + MockData.BUILDINGS.getPrefix()
                 + ":" + MockData.BUILDINGS.getLocalPart()
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&featureid=Buildings.1107531701010"
                 + "&format_options=extendedData:true;style:false");
 
         // by default we have style, but no extended data
         assertEquals(0, doc.getElementsByTagName("Style").getLength());
         assertEquals(1, doc.getElementsByTagName("Schema").getLength());
         assertEquals(2, doc.getElementsByTagName("SimpleField").getLength());
         assertEquals(1, doc.getElementsByTagName("ExtendedData").getLength());
         assertEquals(2, doc.getElementsByTagName("Data").getLength());
     }
 
     public void testExtendedDataStyle() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + MockData.BUILDINGS.getPrefix()
                 + ":" + MockData.BUILDINGS.getLocalPart()
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&featureid=Buildings.1107531701010"
                 + "&format_options=extendedData:true;style:true");
 
         // by default we have style, but no extended data
         assertEquals(1, doc.getElementsByTagName("Style").getLength());
         assertEquals(1, doc.getElementsByTagName("Schema").getLength());
         assertEquals(2, doc.getElementsByTagName("SimpleField").getLength());
         assertEquals(1, doc.getElementsByTagName("ExtendedData").getLength());
         assertEquals(2, doc.getElementsByTagName("Data").getLength());
     }
     
     public void testExtendedDataNoAttributes() throws Exception {
         Document doc = getAsDOM("wms?request=getmap&service=wms&version=1.1.1" + "&format="
                 + OWS5MapProducerFactory.FORMAT + "&layers=" + ONLYGEOM.getPrefix()
                 + ":" + ONLYGEOM.getLocalPart()
                 + "&height=1024&width=1024&bbox=-180,-90,180,90&featureid=OnlyGeom.1107531493630"
                 + "&format_options=extendedData:true");
 
         // by default we have style, but no extended data
         assertEquals(1, doc.getElementsByTagName("Schema").getLength());
         assertEquals(0, doc.getElementsByTagName("SimpleField").getLength());
         assertEquals(1, doc.getElementsByTagName("ExtendedData").getLength());
         assertEquals(0, doc.getElementsByTagName("Data").getLength());
     }
 
 }
