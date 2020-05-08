 package de.twenty11.skysail.server.ext.dbviewer.itests;
 
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 
 import java.util.EnumSet;
 import java.util.List;
 
 import org.ops4j.pax.exam.Option;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.twenty11.skysail.common.testing.utils.PaxExamOptionSet;
 import de.twenty11.skysail.common.testing.utils.SkysailCommonOsgiSetup;
 import de.twenty11.skysail.server.testing.utils.SkysailServerOsgiSetup;
 
 public class SkysailServerExtDbViewerOsgiSetup extends SkysailServerOsgiSetup {
 
     private static Logger logger = LoggerFactory.getLogger(SkysailCommonOsgiSetup.class.getName());
 
     @Override
     public List<Option> getOptions(EnumSet<PaxExamOptionSet> optionSets) {
 
         List<Option> options = super.getOptions(optionSets);
 
         options.add(mavenBundle("de.twentyeleven.skysail", "skysail.server", "0.2.12-SNAPSHOT"));
 
         // restassured:
         options.add(mavenBundle("de.twentyeleven.skysail", "com.jayway.rest-assured-osgi", "1.6.2"));
         options.add(mavenBundle("de.twentyeleven.skysail", "org.hamcrest.hamcrest-all-osgi", "1.3.0.1"));
         options.add(mavenBundle("de.twentyeleven.skysail", "org.ccil.cowan.tagsoup-osgi", "1.2.1"));
         options.add(mavenBundle("commons-collections", "commons-collections", "3.2.1"));
         options.add(mavenBundle("org.apache.commons", "commons-lang3", "3.1"));
         options.add(mavenBundle("org.apache.httpcomponents", "httpcore-osgi", "4.1.4"));
         options.add(mavenBundle("org.apache.httpcomponents", "httpclient-osgi", "4.1.3"));
         options.add(mavenBundle("org.codehaus.groovy", "groovy-all", "1.8.4"));
         options.add(mavenBundle("org.codehaus.jackson", "jackson-core-lgpl", "1.9.5"));
 
         // mysql
         options.add(mavenBundle("mysql", "skysail.bundles.mysql-connector-java", "5.1.6"));
 
         // felix config admin
         // options.add(mavenBundle("org.apache.felix", "org.apache.felix.configadmin", "1.4.0"));
 
        options.add(mavenBundle("commons-dbcp", "commons-dbcp", "1.4"));
 
         // bean validation
         // options.add(mavenBundle("org.apache.bval", "org.apache.bval.bundle", "0.4"));
         // options.add(mavenBundle("org.apache.geronimo.specs", "geronimo-validation_1.0_spec", "1.1"));
         // options.add(mavenBundle("org.apache.servicemix.bundles", "org.apache.servicemix.bundles.commons-beanutils",
         // "1.8.3_1"));
         options.add(mavenBundle("org.hibernate", "hibernate-validator", "4.3.0.Final"));
         options.add(mavenBundle("javax.validation", "com.springsource.javax.validation", "1.0.0.GA"));
         options.add(mavenBundle("org.jboss.logging", "jboss-logging", "3.1.2.GA"));
 
         logger.info("using options from {} for tests", this.getClass());
 
         return options;
     }
 
 }
