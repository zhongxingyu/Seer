 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.chtijbug.drools.runtime.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import org.chtijbug.drools.runtime.RuleBasePackage;
 import org.chtijbug.drools.runtime.RuleBaseSession;
 import org.drools.KnowledgeBase;
 import org.drools.KnowledgeBaseFactory;
 import org.drools.builder.KnowledgeBuilder;
 import org.drools.builder.KnowledgeBuilderFactory;
 import org.drools.builder.ResourceType;
 import org.drools.io.Resource;
 import org.drools.io.ResourceFactory;
 import org.drools.runtime.StatefulKnowledgeSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author nheron
  */
 public class RuleBaseSingleton implements RuleBasePackage {
 
     private KnowledgeBase kbase = null;
     static final Logger LOGGER = LoggerFactory.getLogger(RuleBaseSingleton.class);
     private String guvnor_url;
     private String guvnor_appName = "drools-guvnor";
     private String guvnor_packageName;
     private String guvnor_packageVersion = "/LATEST";
     private String guvnor_username;
     private String guvnor_password;
 
     public RuleBaseSingleton(String guvnor_url, String guvnor_packageName, String guvnor_username, String guvnor_password) {
         this.guvnor_url = guvnor_url;
         this.guvnor_packageName = guvnor_packageName;
         this.guvnor_username = guvnor_username;
         this.guvnor_password = guvnor_password;
         loadKAgent();
     }
 
     public RuleBaseSingleton(String guvnor_url, String guvnor_appName, String guvnor_packageName, String guvnor_packageVersion, String guvnor_username, String guvnor_password) {
         this.guvnor_url = guvnor_url;
         this.guvnor_appName = guvnor_appName;
         this.guvnor_packageName = guvnor_packageName;
         this.guvnor_packageVersion = guvnor_packageVersion;
         this.guvnor_username = guvnor_username;
         this.guvnor_password = guvnor_password;
         loadKAgent();
     }
 
     public RuleBaseSingleton(String guvnor_url, String guvnor_packageName, String guvnor_packageVersion, String guvnor_username, String guvnor_password) {
         this.guvnor_url = guvnor_url;
         this.guvnor_packageName = guvnor_packageName;
         this.guvnor_packageVersion = guvnor_packageVersion;
         this.guvnor_username = guvnor_username;
         this.guvnor_password = guvnor_password;
         loadKAgent();
     }
 
     private void loadKAgent() {
        loadKAgent();
         StringBuffer changesetxml = null;
         try {
 
             StringBuilder buff = new StringBuilder();
             buff.append(guvnor_url);
             buff.append(guvnor_appName);
             buff.append("/org.drools.guvnor.Guvnor/package/");
             buff.append(guvnor_packageName);
             buff.append(guvnor_packageVersion);
 
 
             changesetxml = new StringBuffer();
             changesetxml.append("<change-set xmlns='http://drools.org/drools-5.0/change-set' \n xmlns:xs='http://www.w3.org/2001/XMLSchema-instance'> \n  \n <add> \n");//xs:schemaLocation='http://drools.org/drools-5.0/change-set http://anonsvn.jboss.org/repos/labs/labs/jbossrules/trunk/drools-api/src/main/resources/change-set-1.0.0.xsd' 
             changesetxml.append("<resource source='");
             changesetxml.append(buff.toString());
             changesetxml.append("' type='PKG' basicAuthentication=\"enabled\" username=\"");
             changesetxml.append(guvnor_username);
             changesetxml.append("\" password=\"");
             changesetxml.append(guvnor_password);
             changesetxml.append("\" /> \n </add> \n </change-set>\n");
             File fxml = null;
             try {
 
                 fxml = new File("ChangeSet.xml");
                 BufferedWriter output = new BufferedWriter(new FileWriter(fxml));
                 output.write(changesetxml.toString());
                 output.close();
             } catch (Exception e) {
             }
 
             KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
             //Resource res = ResourceFactory.newClassPathResource("ChangeSet.xml");
             Resource res = ResourceFactory.newFileResource(fxml);
             kbuilder.add(res, ResourceType.CHANGE_SET);
             kbase = KnowledgeBaseFactory.newKnowledgeBase();
             kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
             // kagent.applyChangeSet(rr);
         } catch (Exception e) {
             // e.printStackTrace();
             LOGGER.error("URL incorrect", changesetxml, e);
         }
     }
 
     @Override
     public RuleBaseSession createRuleBaseSession() {
         RuleBaseSession newRuleBaseSession = null;
         if (kbase != null) {
             StatefulKnowledgeSession newDroolsSession = kbase.newStatefulKnowledgeSession();
             newRuleBaseSession = new RuleBaseStatefullSession(newDroolsSession);
         }else{
           throw new UnsupportedOperationException("Kbase not initialized");
         }
         return newRuleBaseSession;
     }
 }
