 package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;
 
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.uima.UimaContext;
 import org.apache.uima.resource.ResourceInitializationException;
 
 import banner.eval.uima.BANNERWrapper;
 import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 
 public class BannerKeytermUpdater extends AbstractKeytermUpdater {
 
   private BANNERWrapper banw;
 
   @Override
   public void initialize(UimaContext aContext) throws ResourceInitializationException {
     super.initialize(aContext);
     String configFilePathString = (String) aContext.getConfigParameterValue("configFile");
     String modelFilePathString = (String) aContext.getConfigParameterValue("modelFile");
 
     URL configFilePath = Thread.currentThread().getContextClassLoader().getResource("config/" + configFilePathString);
     String modelFilePath = "/output/" + modelFilePathString;
     
     banw = new BANNERWrapper();
     banw.initialize(configFilePath, modelFilePath);
   }
 
   @Override
   protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
     Map<String, String> annots = banw.getAnnotations(question+" ."); // XXX added extra content to force complete processing (??)
     for (String k : annots.keySet()) {
       String mentionText = k, type = annots.get(k);
       Keyterm keyterm = new Keyterm(mentionText);
       keyterm.setComponentId(type);
       keyterms.add(keyterm);
       log("BANNER keyterm: " + mentionText + " type: " + type);
     }
     return keyterms;
   }
 
   public void testIntialize() {
     URL configFilePath = getClass().getClassLoader().getResource("config/banner_AZDC.xml");
     String modelFilePath = "/output/model_AZDC.bin";
     if(configFilePath != null)
       System.out.println(configFilePath);
     if(modelFilePath != null)
       System.out.println(configFilePath);
     banw = new BANNERWrapper();
     banw.initialize(configFilePath, modelFilePath);
   }
 
   public static void main(String[] args){
     BannerKeytermUpdater mkt = new BannerKeytermUpdater();
     mkt.testIntialize();
     String q = "What is the role of IDE in Alzheimer's disease ?";
     for(Keyterm term: mkt.updateKeyterms(q, new LinkedList<Keyterm>()))
       System.out.println(term);
   }
 }
