 package de.saumya.gwt.session.client.model;
 
 import de.saumya.gwt.datamapper.client.AbstractResourceTestGwt;
 import de.saumya.gwt.datamapper.client.Resource;
 import de.saumya.gwt.datamapper.client.ResourceFactory;
import de.saumya.gwt.session.client.model.LocaleFactory;
import de.saumya.gwt.session.client.model.Role;
import de.saumya.gwt.session.client.model.RoleFactory;
import de.saumya.gwt.session.client.model.VenueFactory;
 
 /**
  * GWT JUnit tests must extend GWTTestCase.
  */
 public class RoleTestGwt extends AbstractResourceTestGwt<Role> {
 
     /**
      * Must refer to a valid module that sources this class.
      */
     @Override
     public String getModuleName() {
         return "de.saumya.gwt.session.Session";
     }
 
     private Role                resource;
 
     private static final String RESOURCE_XML = "<role>"
                                                      + "<name>root</name>"
                                                      + "<locales></locales>"
                                                      + "<venues></venues>"
                                                      + "<created_at>2009-07-09 17:14:48.0</created_at>"
                                                      + "</role>";
 
     @Override
     protected String resourceNewXml() {
         return RESOURCE_XML.replaceFirst("<created_at>[0-9-:. ]*</created_at>",
                                         "");
     }
 
     @Override
     protected String resource1Xml() {
         return RESOURCE_XML;
     }
 
     @Override
     protected String resource2Xml() {
        return RESOURCE_XML.replace(">root<", ">admin<");
     }
 
     @Override
     protected String resourcesXml() {
         return "<roles>" + resource1Xml() + resource2Xml() + "</roles>";
     }
 
     @Override
     protected ResourceFactory<Role> factorySetUp() {
         return new RoleFactory(this.repository,
                 new LocaleFactory(this.repository),
                 new VenueFactory(this.repository));
     }
 
     @Override
     protected Resource<Role> resourceSetUp() {
         this.resource = this.factory.newResource();
 
         this.resource.name = "root";
 
         this.repository.addXmlResponse(RESOURCE_XML);
 
         this.resource.save();
 
         return this.resource;
     }
 
     @Override
     public void doTestCreate() {
         assertEquals("root", this.resource.name);
     }
 
     @Override
     public void doTestUpdate() {
         this.resource.name = changedValue();
         this.resource.save();
         assertEquals(this.resource.name, changedValue());
     }
 
     private final static String XML = "<role>"
                                             + "<name>root</name>"
                                             + "<locales>"
                                             + "<locale>"
                                             + "<code>de</code>"
                                             + "<created_at>2009-07-09 17:14:48.0</created_at>"
                                             + "</locale>"
                                             + "</locales>"
                                             + "<venues>"
                                             + "<venue>"
                                             + "<id>dvara</id>"
                                             + "<created_at>2007-07-09 17:14:48.0</created_at>"
                                             + "</venue>"
                                             + "</venues>"
                                             + "<created_at>2005-07-09 17:14:48.0</created_at>"
                                             + "</role>";
 
     @Override
     protected String changedValue() {
         return "superuser";
     }
 
     @Override
     protected String keyValue() {
         return "root";
     }
 
     @Override
     protected String marshallingXml() {
         return XML;
     }
 
     @Override
     protected String value() {
         return "root";
     }
 }
