 package com.certh.iti.cloud4all.inference;
 /**
  *
  * @author nkak
  */
 
 import com.certh.iti.cloud4all.feedback.FeedbackManager;
 import com.certh.iti.cloud4all.instantiation.InstantiationManager;
 import com.hp.hpl.jena.rdf.model.InfModel;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
 import com.hp.hpl.jena.reasoner.rulesys.Rule;
 import com.certh.iti.cloud4all.ontology.*;
 import com.certh.iti.cloud4all.prevayler.PrevaylerManager;
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RulesManager 
 {
     public static final int GET_FIRST_INSTANCE_BETWEEN_SOLUTIONS_OF_THE_SAME_TYPE = 0;
     public static final int GET_RANDOM_INSTANCE_BEWTWEEN_SOLUTIONS_OF_THE_SAME_TYPE = 1;
     
     public Model m;
     public BufferedReader br;
     
     String uri_MatchMaker_rules;
     
     private static RulesManager instance = null;
     
     private RulesManager() 
     {
         m = ModelFactory.createDefaultModel();
         
         String currentDir = System.getProperty("user.dir");
         uri_MatchMaker_rules = currentDir + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/MatchMakerRules.rules";
     }
     
     public static RulesManager getInstance() 
     {
         if(instance == null) 
             instance = new RulesManager();
         return instance;
     }
     
     public String executeMyCloudRulesForFindingHandicapSituations(boolean writeInfModelForDebug)
     {
         String resultForNodeJs = "";
         
         //RULES EXECUTION - START
         List<Rule> rules = Rule.rulesFromURL(uri_MatchMaker_rules);
         GenericRuleReasoner r = new GenericRuleReasoner(rules);
         r.setOWLTranslation(true);           
         r.setTransitiveClosureCaching(true);
         InfModel infmodel = ModelFactory.createInfModel(r, OntologyManager.getInstance().model);
         Model deductionsModel = infmodel.getDeductionsModel();
         OntologyManager.getInstance().model.add(deductionsModel);
         //RULES EXECUTION - END
         
         OntologyManager.getInstance().getInstancesAfterRulesExecution();
       
         //get TempHandicapSituations
         if(OntologyManager.getInstance().allInstances_TempHandicapSituation != null)
         {
             for(int i=0; i<OntologyManager.getInstance().allInstances_TempHandicapSituation.size(); i++)
             {
                 if(OntologyManager.getInstance().allInstances_TempHandicapSituation.get(i).problemWithMagnifierFullScreen)
                 {
                     resultForNodeJs = "ENABLE_DEFAULT_THEME ENABLE_MAGNIFIER_WITH_INVERSE_COLOURS";
                     //break;
                 }
                 else if(OntologyManager.getInstance().allInstances_TempHandicapSituation.get(i).problemWithHighContrast)
                 {
                     resultForNodeJs = "ENABLE_DEFAULT_THEME ENABLE_MAGNIFIER_WITH_INVERSE_COLOURS";
                     //break;
                 }
             }
         }
         
         boolean aScreenReaderHasBeenSelectedForLaunching = false;
         
         //get TempSolutionsToBeLaunched
         if(OntologyManager.getInstance().allInstances_TempSolutionsToBeLaunched != null)
         {
             for(int i=0; i<OntologyManager.getInstance().allInstances_TempSolutionsToBeLaunched.size(); i++)
             {
                 String[] tmpIDsToBeLaunched_Str = OntologyManager.getInstance().allInstances_TempSolutionsToBeLaunched.get(i).IDs.split(" ");
                 for(int tmpCnt=0; tmpCnt<tmpIDsToBeLaunched_Str.length; tmpCnt++)
                 {
                     String tempID = tmpIDsToBeLaunched_Str[tmpCnt].trim();
                     if(tempID.equals("org.gnome.desktop.a11y.magnifier"))
                         resultForNodeJs = resultForNodeJs + " LAUNCH_GNOME_MAGNIFIER";
                     else if(tempID.equals("ISO24751.screenMagnifier"))
                         resultForNodeJs = resultForNodeJs + " LAUNCH_ISO24751_MAGNIFIER";
                     else if(tempID.equals("com.microsoft.windows.magnifier"))
                         resultForNodeJs = resultForNodeJs + " LAUNCH_WINDOWS_MAGNIFIER";
                     else if(tempID.equals("ZoomText.screenMagnifier"))
                         resultForNodeJs = resultForNodeJs + " LAUNCH_ZOOMTEXT_MAGNIFIER";
                     else if(tempID.equals("ISO24751.screenReader"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_ISO24751_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("jaws.screenReader"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_JAWS_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("org.nvda-project"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_NVDA_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("org.chrome.cloud4chrome"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_CLOUD4CHROME";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("org.gnome.orca"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_ORCA_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("com.serotek.satogo"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_SATOGO_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("com.android.talkback"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_TALKBACK_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("webinsight.webAnywhere.windows"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_WEBANYWHERE_WINDOWS_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("webinsight.webAnywhere.linux"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_WEBANYWHERE_LINUX_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                     else if(tempID.equals("Win7BuiltInNarrator.screenReader"))
                     {
                         resultForNodeJs = resultForNodeJs + " LAUNCH_WIN7BUILTINNARRATOR_SCREENREADER";
                         aScreenReaderHasBeenSelectedForLaunching = true;
                     }
                 }
             }
         }
         
         if(InstantiationManager.getInstance().DEVICE_REPORTER_OS_id.toLowerCase().equals("linux"))
         {
             //if screenReaderTTSEnabled==true, THEN configure volume
             for(int tempComTermsCounter=0; tempComTermsCounter<InstantiationManager.getInstance().USER_CommonTermsIDs.size(); tempComTermsCounter++)
             {
                 CommonPref tmpCommonPref = InstantiationManager.getInstance().USER_CommonTermsIDs.get(tempComTermsCounter);
                 if(tmpCommonPref.commonTermID.equals("display.screenReader.-provisional-screenReaderTTSEnabled"))
                 {
                     resultForNodeJs = resultForNodeJs + " OPEN_VOLUME";
                     break;
                 }
             }
         }
         else if(InstantiationManager.getInstance().DEVICE_REPORTER_OS_id.toLowerCase().equals("android"))
         {
             for(int tmpCnter=0; tmpCnter<InstantiationManager.getInstance().USER_CommonTermsIDs.size(); tmpCnter++)
             {
                 CommonPref tmpCommonPref = InstantiationManager.getInstance().USER_CommonTermsIDs.get(tmpCnter);
                 if(tmpCommonPref.commonTermID.equals("display.screenEnhancement.-provisional-magnifierEnabled")
                      || tmpCommonPref.commonTermID.equals("display.screenEnhancement.magnification")
                      || tmpCommonPref.commonTermID.equals("display.screenEnhancement.tracking")
                      || tmpCommonPref.commonTermID.equals("display.screenEnhancement.-provisional-magnifierPosition")
                      || tmpCommonPref.commonTermID.equals("display.screenEnhancement.-provisional-invertColours")
                      || tmpCommonPref.commonTermID.equals("display.screenEnhancement.-provisional-showCrosshairs") )
                 {
                     resultForNodeJs = resultForNodeJs + " ANDROID_INCREASE_FONT_SIZE";
                     break;
                 }
             }
         }
         else //windows
         {
             //in case that NO screen reader has been selected for launching
             //AND user has app-specific preferences for a screen reader that is NOT installed
             //THEN add a commom term that defines that user wants a screen reader (in general)
             //because this common term may lead to the launching (by the flat MM) of another screen reader 
             //that IS installed
             if(aScreenReaderHasBeenSelectedForLaunching == false)
             {
                 if( (OntologyManager.getInstance().getIndexInUserPrefs("ISO24751.screenReader")!=-1 && OntologyManager.getInstance().solutionIsInstalled("ISO24751.screenReader")==false) 
                         || (OntologyManager.getInstance().getIndexInUserPrefs("jaws.screenReader")!=-1 && OntologyManager.getInstance().solutionIsInstalled("jaws.screenReader")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("org.nvda-project")!=-1 && OntologyManager.getInstance().solutionIsInstalled("org.nvda-project")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("org.chrome.cloud4chrome")!=-1 && OntologyManager.getInstance().solutionIsInstalled("org.chrome.cloud4chrome")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("org.gnome.orca")!=-1 && OntologyManager.getInstance().solutionIsInstalled("org.gnome.orca")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("com.serotek.satogo")!=-1 && OntologyManager.getInstance().solutionIsInstalled("com.serotek.satogo")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("com.android.talkback")!=-1 && OntologyManager.getInstance().solutionIsInstalled("com.android.talkback")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("webinsight.webAnywhere.windows")!=-1 && OntologyManager.getInstance().solutionIsInstalled("webinsight.webAnywhere.windows")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("webinsight.webAnywhere.linux")!=-1 && OntologyManager.getInstance().solutionIsInstalled("webinsight.webAnywhere.linux")==false)
                         || (OntologyManager.getInstance().getIndexInUserPrefs("Win7BuiltInNarrator.screenReader")!=-1 && OntologyManager.getInstance().solutionIsInstalled("Win7BuiltInNarrator.screenReader")==false)
                   )
                 {
                     resultForNodeJs = resultForNodeJs + " ADD_COMMON_PREF_SCREENREADER_TTS_ENABLED";
                     resultForNodeJs = resultForNodeJs + " LAUNCH_SATOGO_SCREENREADER";
                     FeedbackManager.getInstance().solutionsToBeLaunched.add(OntologyManager.getInstance().getSolutionByID("com.serotek.satogo"));
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }    
             }
 
             //in case that NO screen reader has been selected for launching
             //AND user has COMMON PREFERENCE "screenReaderTTSEnabled"
             //examine all the installed screen readers and select the one with the most RDF statements
             //(it means that is supports more adjustments)
             if(aScreenReaderHasBeenSelectedForLaunching == false)
             {
                String tempSelectedScrReaderID = "webinsight.webAnywhere.linux"; //OntologyManager.getInstance().findTheMostSuitableInstalledScreenReaderFromNumberOfRDFStatements();
                 FeedbackManager.getInstance().solutionsToBeLaunched.add(OntologyManager.getInstance().getSolutionByID(tempSelectedScrReaderID));
 
 
                 if(tempSelectedScrReaderID.equals("ISO24751.screenReader"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_ISO24751_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("jaws.screenReader"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_JAWS_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("org.nvda-project"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_NVDA_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("org.chrome.cloud4chrome"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_CLOUD4CHROME";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("org.gnome.orca"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_ORCA_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("com.serotek.satogo"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_SATOGO_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("com.android.talkback"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_TALKBACK_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("webinsight.webAnywhere.windows"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_WEBANYWHERE_WINDOWS_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("webinsight.webAnywhere.linux"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_WEBANYWHERE_LINUX_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
                 else if(tempSelectedScrReaderID.equals("Win7BuiltInNarrator.screenReader"))
                 {
                     resultForNodeJs = resultForNodeJs + " LAUNCH_WIN7BUILTINNARRATOR_SCREENREADER";
                     aScreenReaderHasBeenSelectedForLaunching = true;
                 }
             }
         }
         
         FeedbackManager.getInstance().findSolutionsToBeExcluded();
         
         //solutions to be excluded
         for(int j=0; j<FeedbackManager.getInstance().solutionsToBeExcluded.size(); j++)
         {
             Solution tempSolution = FeedbackManager.getInstance().solutionsToBeExcluded.get(j);
             if(tempSolution.id.equals("org.gnome.desktop.a11y.magnifier"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_GNOME_MAGNIFIER";
             else if(tempSolution.id.equals("ISO24751.screenMagnifier"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_ISO24751_MAGNIFIER";
             else if(tempSolution.id.equals("com.microsoft.windows.magnifier"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_WINDOWS_MAGNIFIER";
             else if(tempSolution.id.equals("ZoomText.screenMagnifier"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_ZOOMTEXT_MAGNIFIER";
             else if(tempSolution.id.equals("ISO24751.screenReader"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_ISO24751_SCREENREADER";
             else if(tempSolution.id.equals("jaws.screenReader"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_JAWS_SCREENREADER";
             else if(tempSolution.id.equals("org.nvda-project"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_NVDA_SCREENREADER";
             else if(tempSolution.id.equals("org.chrome.cloud4chrome"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_CLOUD4CHROME";
             else if(tempSolution.id.equals("org.gnome.orca"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_ORCA_SCREENREADER";
             else if(tempSolution.id.equals("com.serotek.satogo"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_SATOGO_SCREENREADER";
             else if(tempSolution.id.equals("com.android.talkback"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_TALKBACK_SCREENREADER";
             else if(tempSolution.id.equals("webinsight.webAnywhere.windows"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_WEBANYWHERE_WINDOWS_SCREENREADER";
             else if(tempSolution.id.equals("webinsight.webAnywhere.linux"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_WEBANYWHERE_LINUX_SCREENREADER";
             else if(tempSolution.id.equals("Win7BuiltInNarrator.screenReader"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_WIN7BUILTINNARRATOR_SCREENREADER";
             else if(tempSolution.id.equals("com.yourdolphin.supernova-as"))
                 resultForNodeJs = resultForNodeJs + " EXCLUDE_SUPERNOVA";
         }
         
         //HTML output test - NVDA
         if(FeedbackManager.getInstance().TEST_GENERATE_FEEDBACK_FOR_NVDA)
         {
             resultForNodeJs = " LAUNCH_NVDA_SCREENREADER";
             FeedbackManager.getInstance().allAvailableSolutions.clear();
             FeedbackManager.getInstance().allAvailableSolutions.add(OntologyManager.getInstance().getSolutionByID("com.serotek.satogo"));
             FeedbackManager.getInstance().allAvailableSolutions.add(OntologyManager.getInstance().getSolutionByID("org.nvda-project"));
             FeedbackManager.getInstance().solutionsToBeLaunched.clear();
             FeedbackManager.getInstance().solutionsToBeLaunched.add(OntologyManager.getInstance().getSolutionByID("org.nvda-project"));
         }
         
         //DEBUG
         if(writeInfModelForDebug)
         {
             try
             {
                 OutputStream out = new FileOutputStream("./lib/generatedOntologyWithTestUserAndEnvironmement.owl");
                 OntologyManager.getInstance().model.write(out, "RDF/XML-ABBREV"); // readable rdf/xml
                 out.close();            
             }
             catch(Exception e)
             {
                 e.printStackTrace();
             }
         }
         //-DEBUG
         
         //initialize again the model
         OntologyManager.getInstance().model.remove(deductionsModel); //remove statements created by the rules
         InstantiationManager.getInstance().removeTempInstances();    //remove temp instances for user and environment
         
         return resultForNodeJs;
     }
 }
