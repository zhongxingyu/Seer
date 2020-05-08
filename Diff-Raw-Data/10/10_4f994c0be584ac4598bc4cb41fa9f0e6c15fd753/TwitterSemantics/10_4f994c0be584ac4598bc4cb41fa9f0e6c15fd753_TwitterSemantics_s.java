 package tuwien.aic12.server.twitter.semantics;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import tuwien.aic12.server.twitter.semantics.classifier.ClassifierBuilder;
 import tuwien.aic12.server.twitter.semantics.classifier.WekaClassifier;
 import tuwien.aic12.server.twitter.semantics.util.Options;
 import twitter4j.Tweet;
 import weka.classifiers.bayes.NaiveBayes;
 
 /**
  *
  * @author vanjalee
  */
 public class TwitterSemantics {
 
     private DecimalFormat decimalFormat = new DecimalFormat("#.##");
 
     public Double analyse(List<Tweet> tweets) {
         Double aMiddle = 0.0;
         ClassifierBuilder clb = new ClassifierBuilder();
         Options opt = new Options();
         clb.setOpt(opt);
        //seleziona solo i termini utilizzati più di una volta
         opt.setSelectedFeaturesByFrequency(true);
         // seleziona solamente 150 termini
         opt.setNumFeatures(150);
         // rimuove le emoticons
         opt.setRemoveEmoticons(true);
         List<String> results = new ArrayList<String>();
         List<Double> convertedResults = new ArrayList<Double>();
         try {
             //prepara le strutture dati per il train e il test
             clb.prepareTrain();
             clb.prepareTest();
             //classificatore Weka
             NaiveBayes nb = new NaiveBayes();
             //costruzione e memorizzazione su disco del classificatore
             WekaClassifier wc = clb.constructClassifier(nb);
             for (Tweet t : tweets) {
                 String result = wc.classify(t.getText());
                 results.add(result);
             }
             convertedResults = convertResults(results);
             for (Double d : convertedResults) {
                 aMiddle += d;
             }
             aMiddle = aMiddle / convertedResults.size();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(TwitterSemantics.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(TwitterSemantics.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(TwitterSemantics.class.getName()).log(Level.SEVERE, null, ex);
         }
         return Double.valueOf(decimalFormat.format(aMiddle));
     }
 
     private List<Double> convertResults(List<String> stringResults) {
         List<Double> returnValue = new ArrayList<Double>();
         for (String s : stringResults) {
             if (s.equals("0")) {
                 returnValue.add(0.0);
             } else if (s.equals("1")) {
                 returnValue.add(0.25);
             } else if (s.equals("2")) {
                 returnValue.add(0.5);
             } else if (s.equals("3")) {
                 returnValue.add(0.75);
             } else {
                 returnValue.add(1.0);
             }
         }
         return returnValue;
     }
 }
