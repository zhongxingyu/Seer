 package org.geoserver.web.admin;
 
import org.geoserver.jai.JAIInfo;
import org.geoserver.web.GeoServerWicketTestSupport;

 import org.apache.wicket.markup.html.form.TextField;
 
 public class JAIPageTest extends GeoServerWicketTestSupport {
     public void testValues() {
         JAIInfo info = (JAIInfo) getGeoServerApplication()
             .getGeoServer()
            .getGlobal()
            .getMetadata()
            .get(JAIInfo.KEY);
 
         login();
 
         tester.startPage(JAIPage.class);
         tester.assertComponent("form:tileThreads", TextField.class);
         tester.assertModelValue("form:tileThreads", info.getTileThreads());
     }
 }
