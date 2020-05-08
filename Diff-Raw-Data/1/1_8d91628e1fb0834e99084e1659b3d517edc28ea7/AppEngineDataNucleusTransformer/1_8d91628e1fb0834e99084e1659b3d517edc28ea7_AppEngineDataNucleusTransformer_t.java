 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.maven.arquillian.transformer;
 
 import java.lang.reflect.Method;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import javassist.CtClass;
 import org.jboss.shrinkwrap.api.ShrinkWrap;
 import org.jboss.shrinkwrap.api.spec.WebArchive;
 import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
 
 /**
  * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
  */
 public class AppEngineDataNucleusTransformer extends ArquillianJUnitTransformer {
     protected String getDeploymentMethodBody(CtClass clazz) throws Exception {
         return "{return org.jboss.maven.arquillian.transformer.AppEngineDataNucleusTransformer.buildArchive(\"" + clazz.getName() + "\");}";
     }
 
     public static WebArchive buildArchive(String clazz) {
         WebArchive war = ShrinkWrap.create(WebArchive.class);
         addClasses(war, clazz);
         war.addPackage("com.google.appengine.datanucleus");
         if (clazz.contains(".jpa.") || clazz.contains(".query.")) {
             war.addPackage("com.google.appengine.datanucleus.test.jpa");
             war.addClass("com.google.appengine.datanucleus.jpa.JPATestCase$EntityManagerFactoryName");
         }
         if (clazz.contains(".jdo.") || clazz.contains(".query.")) {
             war.addClass("com.google.appengine.datanucleus.jdo.JDOTestCase$PersistenceManagerFactoryName");
         }
         if (clazz.contains(".query.")) {
             war.addClass("com.google.appengine.datanucleus.query.ApiConfigMatcher");
             war.addClass("com.google.appengine.datanucleus.query.BookSummary");
             war.addClass("com.google.appengine.datanucleus.query.ChunkMatcher");
             war.addClass("com.google.appengine.datanucleus.query.FailoverMsMatcher");
             war.addClass("com.google.appengine.datanucleus.query.FlightStartEnd1");
             war.addClass("com.google.appengine.datanucleus.query.FlightStartEnd2");
             war.addClass("com.google.appengine.datanucleus.query.NoQueryDelegate");
         }
         war.addPackage("com.google.appengine.datanucleus.test.jdo");
         war.setWebXML(new org.jboss.shrinkwrap.api.asset.StringAsset("<web/>"));
         war.addAsWebInfResource("appengine-web.xml");
         war.addAsWebInfResource("META-INF/persistence.xml", "classes/META-INF/persistence.xml");
         war.addAsWebInfResource("META-INF/jdoconfig.xml", "classes/META-INF/jdoconfig.xml");
 
         final PomEquippedResolveStage resolver = getResolver("pom.xml");
         final String version_dn_gae = System.getProperty("version.dn.gae", "2.1.2-SNAPSHOT"); // TODO -- better way?
         war.addAsLibraries(resolve(resolver, "com.google.appengine.orm:datanucleus-appengine:" + version_dn_gae));
         war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-api-1.0-sdk"));
         war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-testing"));
         war.addAsLibraries(resolve(resolver, "com.google.appengine:appengine-api-stubs"));
         war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-core"));
         war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-api-jdo"));
         war.addAsLibraries(resolve(resolver, "org.datanucleus:datanucleus-api-jpa"));
         war.addAsLibraries(resolve(resolver, "javax.jdo:jdo-api"));
         war.addAsLibraries(resolve(resolver, "org.apache.geronimo.specs:geronimo-jpa_2.0_spec"));
         war.addAsLibraries(resolve(resolver, "org.easymock:easymockclassextension"));
 
         System.err.println(war.toString(true));
 
         return war;
     }
 
     private static void addClasses(WebArchive war, String clazz) {
         try {
             ClassLoader cl = AppEngineDataNucleusTransformer.class.getClassLoader();
             Class<?> current = cl.loadClass(clazz);
             addClasses(war, current);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void addClasses(WebArchive war, Class<?> current) {
         while (current != null && current != Object.class && "junit.framework.TestCase".equals(current.getName()) == false) {
             war.addClass(current);
             current = current.getSuperclass();
         }
     }
 
     @Override
     protected String tearDownSrc() {
         return super.tearDownSrc() + "org.jboss.maven.arquillian.transformer.AppEngineDataNucleusTransformer.clean();";
     }
 
     // a hack to clean the DS after test
     public static void clean() {
         try {
             DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
             Method clean = ds.getClass().getDeclaredMethod("clearCache"); // impl detail
             clean.invoke(ds);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 }
