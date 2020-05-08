 package org.geoserver.web.wicket;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.util.tester.FormTester;
 import org.geoserver.web.FormTestPage;
 import org.geoserver.web.GeoServerWicketTestSupport;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 public class EnvelopePanelTest extends GeoServerWicketTestSupport {
 
     ReferencedEnvelope e;
     
     @Override
     protected void setUpInternal() throws Exception {
         super.setUpInternal();
         
         e = new ReferencedEnvelope(-180,180,-90,90, DefaultGeographicCRS.WGS84);
         tester.startPage(new FormTestPage(new FormTestPage.ComponentBuilder() {
         
             public Component buildComponent(String id) {
                 return new EnvelopePanel(id, e);
             }
         }));
     }
     
     public void test() throws Exception {
         tester.assertComponent( "form", Form.class );
         
         FormTester ft = tester.newFormTester( "form");
        assertEquals( "-180", ft.getTextComponentValue( "content:minX") );
        assertEquals( "-90", ft.getTextComponentValue( "content:minY") );
        assertEquals( "180", ft.getTextComponentValue( "content:maxX") );
        assertEquals( "90", ft.getTextComponentValue( "content:maxY") );
         
        EnvelopePanel ep = (EnvelopePanel) tester.getComponentFromLastRenderedPage("form:content");
         assertEquals( e, ep.getModelObject() );
         
        ft.setValue( "content:minX", "-2");
        ft.setValue( "content:minY", "-2");
        ft.setValue( "content:maxX", "2");
        ft.setValue( "content:maxY", "2");
         
         ft.submit();
         
         assertEquals( new Envelope(-2,2,-2,2), ep.getModelObject() );
     }
 }
