 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wfsv.xml.v1_1_0;
 
 import net.opengis.ows.OwsFactory;
 import net.opengis.wfs.WfsFactory;
 import net.opengis.wfsv.WfsvFactory;
 
 import org.eclipse.xsd.util.XSDSchemaLocationResolver;
 import org.geoserver.wfs.xml.FeatureTypeSchemaBuilder;
import org.geoserver.wfs.xml.v1_1_0.WFSConfiguration;
 import org.geotools.xml.BindingConfiguration;
 import org.geotools.xml.Configuration;
 import org.picocontainer.MutablePicoContainer;
 import org.vfny.geoserver.global.Data;
 
 
 /**
  * Parser configuration for the http://www.opengis.net/wfsv schema.
  *
  * @generated
  */
 public class WFSVConfiguration extends Configuration {
     /**
      * Creates a new configuration.
      *
      * @generated
      */
     public WFSVConfiguration(Data catalog, FeatureTypeSchemaBuilder schemaBuilder) {
         super();
         addDependency(new WFSConfiguration(catalog, schemaBuilder));
     }
 
     /**
      * @return the schema namespace uri: http://www.opengis.net/wfsv.
      * @generated
      */
     public String getNamespaceURI() {
         return WFSV.NAMESPACE;
     }
 
     /**
      * @return the uri to the the wfsv.xsd .
      * @generated
      */
     public String getSchemaFileURL() {
         return getSchemaLocationResolver()
                    .resolveSchemaLocation(null, getNamespaceURI(), "wfsv.xsd");
     }
     
     public XSDSchemaLocationResolver getSchemaLocationResolver() {
         return new WFSVSchemaLocationResolver();
     }
     
     /**
      * @return new instanceof {@link WFSVBindingConfiguration}.
      */
     public BindingConfiguration getBindingConfiguration() {
         return new WFSVBindingConfiguration();
     }
     
     protected void configureContext(MutablePicoContainer context) {
         super.configureContext(context);
         context.registerComponentInstance(OwsFactory.eINSTANCE);
         context.registerComponentInstance(WfsFactory.eINSTANCE);
         context.registerComponentInstance(WfsvFactory.eINSTANCE);
     }
 }
