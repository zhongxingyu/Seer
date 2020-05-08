 package org.easysoa.registry.test;
 
 
 import org.nuxeo.ecm.webengine.test.WebEngineFeature;
 import org.nuxeo.runtime.test.runner.Jetty;
 
 @Jetty(port = EasySOAWebEngineFeature.PORT)
 public class EasySOAWebEngineFeature extends WebEngineFeature {
     public static final int PORT = 8082;
 
     public static final String NUXEO_URL = "http://localhost:" + PORT + "/";
 
 }
