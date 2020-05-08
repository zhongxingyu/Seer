 
 package pikater.gui.java;
 
 import jade.content.lang.Codec.CodecException;
 import jade.content.onto.OntologyException;
 import jade.content.onto.UngroundedException;
 import jade.content.onto.basic.Action;
 import jade.content.onto.basic.Result;
 import jade.core.AID;
 import jade.domain.DFService;
 import jade.domain.FIPAAgentManagement.DFAgentDescription;
 import jade.domain.FIPAAgentManagement.FailureException;
 import jade.domain.FIPAAgentManagement.ServiceDescription;
 import jade.domain.FIPAException;
 import jade.domain.FIPANames;
 import jade.domain.FIPAService;
 import jade.gui.GuiEvent;
 import jade.lang.acl.ACLMessage;
 import jade.util.leap.Iterator;
 import jade.util.leap.LinkedList;
 import jade.util.leap.List;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.ResourceBundle;
 import java.util.Scanner;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import java.util.regex.Pattern;
 import java.util.regex.Pattern;
 
 import pikater.Agent_GUI;
 import pikater.gui.java.improved.DataInputFrame;
 import pikater.DataManagerService;
 import pikater.gui.java.improved.AgentOptionsDialog;
 import pikater.gui.java.improved.FileBrowserFrame;
 import pikater.gui.java.improved.FileDetailsFrame;
 import pikater.gui.java.improved.FileGroup;
 import pikater.gui.java.improved.GuiConstants;
 import pikater.gui.java.improved.NewExperimentFrame;
 import pikater.gui.java.improved.ResultsBrowserFrame;
 import pikater.ontology.messages.Agent;
 import pikater.ontology.messages.DataInstances;
 import pikater.ontology.messages.Evaluation;
 import pikater.ontology.messages.Execute;
 import pikater.ontology.messages.GetData;
 import pikater.ontology.messages.GetFileInfo;
 import pikater.ontology.messages.Instance;
 import pikater.ontology.messages.LoadResults;
 import pikater.ontology.messages.Metadata;
 import pikater.ontology.messages.Option;
 import pikater.ontology.messages.Problem;
 import pikater.ontology.messages.Results;
 import pikater.ontology.messages.Task;
 
 public class Agent_GUI_Java extends Agent_GUI {
 
     /**
      *
      */
     private static final long serialVersionUID = -3678714827126048550L;
     transient protected pikater.gui.java.improved.MainWindow myGUI;
     private HashMap<Integer, Integer> experimentTasks = new HashMap<Integer, Integer>();
     private HashMap<Integer, Integer> finishedTasks = new HashMap<Integer, Integer>();
 
 
     public Agent_GUI_Java() {
 
         myGUI = new pikater.gui.java.improved.MainWindow(this);
 
         myGUI.setVisible(true);
         myGUI.showInfo(ResourceBundle.getBundle("pikater/gui/java/improved/Strings").getString("PIKATER_START"));
     }
 
     @Override
     protected void DisplayWrongOption(int problemGuiId, String agentName,
             String optionName, String errorMessage) {
         myGUI.showError("Agent " + agentName + ": " + errorMessage);
     }
 
     @Override
     protected void allOptionsReceived(int problemId) {
         try {
 			sendProblem(problemId);
 		} catch (Exception e) {
 			System.err.println("Problem hasn't been specified correctly.");
 			e.printStackTrace();
 		}
         myGUI.showInfo(ResourceBundle.getBundle("pikater/gui/java/improved/Strings").getString("STARTING_EXPERIMENT") + (problemId + 1));
     }
 
     /*@Override
     protected void displayOptions(Problem problem, int performative) {
         // TODO Auto-generated method stub
     }*/
 
     private void showResult(Task t) {
 
         int problemID = Integer.parseInt(t.getId().split("_")[1]);
         int finished = finishedTasks.get(problemID) + 1;
         finishedTasks.put(problemID, finished);
         int total = experimentTasks.get(problemID);
 
         String finTot = " (" + finished + "/" + total + ")";
 
         myGUI.showInfo(ResourceBundle.getBundle("pikater/gui/java/improved/Strings").getString("GOT_RESULTS") + (problemID + 1) + finTot);
         
         String testInternalFilename = t.getData().getTest_file_name();
                 String trainInternalFilename = t.getData().getTrain_file_name();
 
 				String[] path = testInternalFilename.split(Pattern.quote(
 						System.getProperty("file.separator")));
                 testInternalFilename = path[path.length - 1];
 
 				path = trainInternalFilename.split(Pattern.quote(
 						System.getProperty("file.separator")));
                 trainInternalFilename = path[path.length - 1];
 
                 t.getData().setTest_file_name(
                         DataManagerService.translateFilename(this, 1, null,
                         testInternalFilename));
                 t.getData().setTrain_file_name(
                         DataManagerService.translateFilename(this, 1, null,
                         trainInternalFilename));
 
                 if (t.getResult().getStatus() == null)
                     myGUI.addResult(t);
                 else {
                     myGUI.showError("Error: " + t.getResult().getStatus());
                 }
     }
 
     @Override
     protected void displayPartialResult(ACLMessage inform) {
         if (inform.getPerformative() != ACLMessage.INFORM) {
             System.err.println("Received FAILURE");
             myGUI.showError(inform.getContent());
             return;
         }
 
         try {
             Result r = (Result) getContentManager().extractContent(inform);
             Results res = (Results) r.getValue();
             List tasks = res.getResults();
 
             myGUI.showInfo(ResourceBundle.getBundle("pikater/gui/java/improved/Strings").getString("GOT_RESULTS") + ((Task)tasks.get(0)).getId().split("_")[1]);
 
             Iterator it = tasks.iterator();
 
             while (it.hasNext()) {
                 Task t = (Task) it.next();
 
                 showResult(t);
             }
         } catch (UngroundedException e) {
             e.printStackTrace();
             myGUI.showError("Ungrounded exception: " + e.getLocalizedMessage());
         } catch (CodecException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             myGUI.showError("Codec exception: " + e.getLocalizedMessage());
         } catch (OntologyException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
             myGUI.showError("Ontology exception: " + e.getLocalizedMessage());
         }
 
     }
 
     public List getAgentOptionsSynchronous(String agentType) {
 
         java.util.LinkedList<String> filterOptions = new java.util.LinkedList<String>();
 
         try {
             FileReader in = new FileReader("guiDisplayOptions");
             Scanner s = new Scanner(in);
 
             while (s.hasNextLine()) {
                 filterOptions.add(s.nextLine());
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 
         List options = null;
         
         try {
             options = getOptions(agentType);
 
             for (int i = options.size() - 1; i >= 0; i--) {
                 Option o = (Option) options.get(i);
                 if (!filterOptions.contains(agentType + "-" + o.getName())) {
                     options.remove(o);
                 }
             }
 
         } catch (CodecException ce) {
             ce.printStackTrace();
             myGUI.showError("Codec Error: " + ce.getLocalizedMessage());
         } catch (OntologyException oe) {
             oe.printStackTrace();
             myGUI.showError("Ontology Error: " + oe.getLocalizedMessage());
         } catch (FIPAException fe) {
             fe.printStackTrace();
             myGUI.showError("FIPA error: " + fe.getLocalizedMessage());
         }
 
         if (options == null) {
             options = new LinkedList();
         }
 
         return options;
     }
 
     @Override
     protected void displayResult(ACLMessage inform) {
         displayPartialResult(inform);
         //myGUI.allResultsReceived();
     }
 
     @Override
     protected String getAgentType() {
         return "Java GUI Agent";
     }
 
     @Override
     protected void mySetup() {
         // TODO Auto-generated method stub
     }
 
     @Override
     protected void onGuiEvent(GuiEvent ev) {
         switch (ev.getType()) {
             case GuiConstants.GET_FILES_INFO:
 
                 if (ev.getSource() instanceof FileBrowserFrame) {
                     FileBrowserFrame fm = (FileBrowserFrame) ev.getSource();
                     GetFileInfo gfi = (GetFileInfo)ev.getParameter(0);
                     fm.setFiles(DataManagerService.getFilesInfo(this, gfi));
                 }
                 if (ev.getSource() instanceof NewExperimentFrame) {
                     NewExperimentFrame nef = (NewExperimentFrame)ev.getSource();
                     GetFileInfo gfi = (GetFileInfo)ev.getParameter(0);
                     nef.setFiles(DataManagerService.getFilesInfo(this, gfi));
                 }
                 break;
 
             case GuiConstants.UPDATE_METADATA:
 
                 Metadata update = (Metadata) ev.getParameter(0);
                 DataManagerService.updateMetadata(this, update);
                 break;
 
             case GuiConstants.GET_DATA:
 
                 System.err.println(ev.getParameter(0).toString());
                 
                 String internalFilename = DataManagerService.translateFilename(this, 1, (String)ev.getParameter(0), null);
                 internalFilename = "data" + System.getProperty("file.separator") + "files" + System.getProperty("file.separator") + internalFilename;
 
                 ServiceDescription sd = new ServiceDescription();
                 sd.setType("ARFFReader");
 
                 DFAgentDescription dfd = new DFAgentDescription();
                 dfd.addServices(sd);
 
                 try {
                     DFAgentDescription readers[] = DFService.search(this, dfd);
 
                     if (readers.length == 0) {
                         System.err.println("No readers found");
                         break;
                     }
 
                     AID reader = readers[0].getName();
 
                     GetData gd = new GetData();
                     gd.setFile_name(internalFilename);
 
                     Action a = new Action();
                     a.setAction(gd);
                     a.setActor(this.getAID());
 
                     ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                     req.addReceiver(reader);
                     req.setLanguage(codec.getName());
                     req.setOntology(ontology.getName());
                     req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 
                     getContentManager().fillContent(req, a);
 
                     ACLMessage response = FIPAService.doFipaRequestClient(this, req);
 
                     if (response.getPerformative() != ACLMessage.INFORM) {
                         System.err.println("Error reading file");
                     }
 
                     Result res = (Result)getContentManager().extractContent(response);
                     DataInstances di = (DataInstances)res.getValue();
 
                     if (ev.getSource() instanceof FileDetailsFrame) {
                         FileDetailsFrame source = (FileDetailsFrame)ev.getSource();
                         source.setInstances(di);
                     }
 
                     if (ev.getSource() instanceof pikater.gui.java.improved.ResultsBrowserFrame) {
                         pikater.gui.java.improved.ResultsBrowserFrame source = (pikater.gui.java.improved.ResultsBrowserFrame)ev.getSource();
                         source.addTrainingFile((String)ev.getParameter(0), di);
                     }
 
                     if (ev.getSource() instanceof DataInputFrame) {
                         DataInputFrame did = (DataInputFrame)ev.getSource();
                         did.setDataInstances(di);
                     }
 
                 }
                 catch (Exception e) {
                     myGUI.showError("Error: " + e.getLocalizedMessage());
                     e.printStackTrace();
                 }
                 break;
             case GuiConstants.LOAD_RESULTS:
 
                 LoadResults lr = (LoadResults)ev.getParameter(0);
                 Action a = new Action(this.getAID(), lr);
 
                 System.err.println(lr.asText());
                 System.err.println(lr.asSQLCondition());
 
                 ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                 req.addReceiver(new AID("dataManager", false));
                 req.setLanguage(codec.getName());
                 req.setOntology(ontology.getName());
                 req.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
 
                 try {
                     getContentManager().fillContent(req, a);
                     ACLMessage response = FIPAService.doFipaRequestClient(this, req);
 
                     if (response.getPerformative() != ACLMessage.INFORM) {
                         System.err.println("Error getting results");
                     }
 
                     Result res = (Result)getContentManager().extractContent(response);
                     List l = (List)res.getValue();
 
                     if (ev.getSource() instanceof ResultsBrowserFrame) {
                         ResultsBrowserFrame source = (ResultsBrowserFrame)ev.getSource();
                         source.showResults(l);
                     }
 
                 }
                 catch (CodecException e) {
                     myGUI.showError("COdec error: " + e.getLocalizedMessage());
                     e.printStackTrace();
                 }
                 catch (OntologyException e) {
                     myGUI.showError("Ontology error: " + e.getLocalizedMessage());
                     e.printStackTrace();
                 }
                 catch (FIPAException e) {
                     myGUI.showError("FIPA error: " + e.getLocalizedMessage());
                     e.printStackTrace();
                 }
                 break;
 
             case GuiConstants.GET_AGENT_TYPES:
 
                 System.err.println("GET_AGENT_TYPES");
 
                 java.util.LinkedList<String> filterAgents = new java.util.LinkedList<String>();
 
                 try {
                     FileReader in = new FileReader("guiDisplayAgents");
                     Scanner s = new Scanner(in);
 
                     while (s.hasNextLine()) {
                         filterAgents.add(s.nextLine());
                     }
 
                 }
                 catch (FileNotFoundException e ) {
                     e.printStackTrace();
                 }
 
                 NewExperimentFrame nef = (NewExperimentFrame) ev.getSource();
 
                 Vector<String> typesBF = offerAgentTypes();
                 typesBF.add(0, "?");
                 Vector<String> types = new Vector<String>();
                 if (filterAgents.size() > 0){
                     
                     for (String s : typesBF) {
                         if (filterAgents.contains(s))
                             types.add(s);
                     }
                 }
                 String[] agentTypes = new String[types.size()];
 
                 for (int i = 0; i < agentTypes.length; i++) {
                     agentTypes[i] = types.get(i);
                 }
 
                 /*try {
                     FileOutputStream outF = new FileOutputStream("agent_strings.properties");
                     PrintStream out = new PrintStream(outF);
 
 
 
                     for (String s : agentTypes) {
 
                        out.println(s + "=" + s);
 
                        Iterator it = getAgentOptionsSynchronous(s).iterator();
                        while (it.hasNext()) {
 
                            Option o = (Option)it.next();
                            String synopsis = o.getSynopsis();
                            String description = o.getDescription();
 
                            description = description.trim();
                            description = description.replace("\n", " ");
                            out.println(s + "-" + o.getName() + "-S=" + synopsis);
                            out.println(s + "-" + o.getName() + "-D=" + description);
 
                         }
 
                        }
 
 
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                 }*/
 
 
 
                 nef.setAgentTypes(agentTypes);
                 break;
             case GuiConstants.GET_AGENT_OPTIONS:
 
                 java.util.LinkedList<String> filterOptions = new java.util.LinkedList<String>();
 
                 try {
                     FileReader in = new FileReader("guiDisplayOptions");
                     Scanner s = new Scanner(in);
 
                     while (s.hasNextLine()) {
                         filterOptions.add(s.nextLine());
                     }
 
                 }
                 catch (FileNotFoundException e ) {
                     e.printStackTrace();
                 }
 
                 AgentOptionsDialog aop = (AgentOptionsDialog)ev.getSource();
 
                 String agentType = (String)ev.getParameter(0);
 
                 try {
                     List options = getOptions(agentType);
 
                     for (int i = options.size() - 1; i >= 0; i--) {
                         Option o = (Option)options.get(i);
                         System.err.println(agentType + "-" + o.getName());
                         if (!filterOptions.contains(agentType + "-" + o.getName())) {
                             options.remove(o);
                         }
                         System.err.println("-" + o.getName() + " : " + o.getSynopsis());
                     }
 
                     if (options == null)
                         options = new LinkedList();
 
                     aop.setAgentOptions(options);
 
                     for (int i = 0; i < options.size(); i++) {
                         Option o = (Option)options.get(i);
                         System.err.println("-" + o.getName() + " : " + o.getSynopsis());
                     }
                 }
                 catch (CodecException ce) {
                     ce.printStackTrace();
                     myGUI.showError("Codec Error: " + ce.getLocalizedMessage());
                 }
                 catch (OntologyException oe) {
                     oe.printStackTrace();
                     myGUI.showError("Ontology Error: " + oe.getLocalizedMessage());
                 }
                 catch (FIPAException fe) {
                     fe.printStackTrace();
                     myGUI.showError("FIPA error: " + fe.getLocalizedMessage());
                 }
 
                 break;
 
             case GuiConstants.START_EXPERIMENT:
 
                 String optManagerType = (String) ev.getParameter(0);
                 List optManagerOptions = (List) ev.getParameter(1);
                 ArrayList<Agent> agents = (ArrayList<Agent>) ev.getParameter(2);
                 ArrayList<FileGroup> files = (ArrayList<FileGroup>) ev.getParameter(3);
                int tasks = (Integer)ev.getParameter(3);
             
                 int problemID = createNewProblem("10000", "after_each_task", "no");
                 // createNewProblem (
                 // 		timeout,
                 //		get_results - "after_each_task", "after_each_computation",
                 //		save_results - "yes", "no")
 
                 experimentTasks.put(problemID, tasks);
                 finishedTasks.put(problemID, 0);
 
                 addMethodToProblem(problemID, optManagerType);
 
                 for (int i = 0; i < optManagerOptions.size(); i++) {
                     Option o = (Option)optManagerOptions.get(i);
                     addSearchOption(problemID, o.getName(), o.getValue());
                 }
 
                 for (int i = 0; i < files.size(); i++) {
                     addDatasetToProblem(problemID, files.get(i).getTrainFile(), files.get(i).getTestFile(), null, "predictions", null);
                 }
 
                 try {
                     for (int i = 0; i < agents.size(); i++) {
 
                         if (agents.get(i).getType().equals("?")) {
                             System.err.println("? agent");
                             addAgentToProblem(problemID, null, agents.get(i).getType(), null);
                         } else {
                             int aid = addAgentToProblem(problemID, null, agents.get(i).getType(), null);
 
                             for (int j = 0; j < agents.get(i).getOptions().size(); j++) {
                                 Option o = (Option)agents.get(i).getOptions().get(j);
 
                                 String tries = o.getNumber_of_values_to_try() != 0 ? String.valueOf(o.getNumber_of_values_to_try()) : null;
 
                                 if (o.getIs_a_set()) {
                                     String set = "";
                                     for (int k = 0; k < o.getSet().size(); k++) {
                                         set += o.getSet().get(k);
                                         if (k < o.getSet().size() - 1) {
                                             set += ",";
                                         }
                                     }
                                     System.err.println("SET PARAM: " + set);
                                     addOptionToAgent(problemID, aid, o.getName(), o.getValue(), null, null, String.valueOf(o.getSet().size()), set);
                                 }
                                 else
                                     addOptionToAgent(problemID, aid, o.getName(), o.getValue(), o.getRange().getMin().toString(), o.getRange().getMax().toString(), tries, null);
                             }
 
                         }
                     }
                 } catch (FailureException e) {
                     e.printStackTrace();
                 }
 
                 
                 break;
 
             case GuiConstants.IMPORT_TEMP_FILE:
 
                 String fileContent = (String)ev.getParameter(0);
                 String fileName = (String)ev.getParameter(1);
 
                 DataManagerService.importFile(this, 1, fileName, fileContent, true);
 
                 if (ev.getSource() instanceof DataInputFrame) {
 
                     GuiEvent ge = new GuiEvent(ev.getSource(), GuiConstants.GET_DATA);
                     ge.addParameter(fileName);
                     this.postGuiEvent(ge);
 
                 }
 
                 break;
                 
             case GuiConstants.LABEL_NEW_DATA:
 
                 fileContent = (String)ev.getParameter(0);
                 fileName = (String)ev.getParameter(1);
 
                 DataManagerService.importFile(this, 1, fileName, fileContent, true);
 
                 Execute ex = (Execute)ev.getParameter(2);
 
                 try {
                     loadAgent(ex.getTask().getAgent().getName(), ex, ex.getTask().getAgent().getObject());
 
                     /*System.err.println("Extracting results");
 
                     Result res = (Result)getContentManager().extractContent(response);
 
                     System.err.println("Content extracted" + res.getValue().toString());
 
                     Evaluation eval = (Evaluation)res.getValue();
 
                     System.err.println("Values obtained");
 
                     DataInputDialog did = (DataInputDialog)ev.getSource();
 
                     did.setDataInstances(eval.getData_table());*/
                     
                 } catch (FIPAException e2) {
                     // TODO Auto-generated catch block
                     e2.printStackTrace();
                 } /*catch (CodecException ce) {
                     ce.printStackTrace();
                 } catch (UngroundedException ue) {
                     ue.printStackTrace();
                 } catch (OntologyException oe) {
                     oe.printStackTrace();
                 }*/
 
                 break;
 
 /*            case MainWindow.IMPORT_FILE:
 
                 String fileName = (String) ev.getParameter(0);
                 String fileContent = (String) ev.getParameter(1);
 
                 DataManagerService.importFile(this, 1, fileName, fileContent);
 
                 FileManagerPanel fmp = (FileManagerPanel) ev.getSource();
                 fmp.reloadFileInfo();
 
                 //myGUI.addFile(fileName);
 
                 break;*/
 
         }
     }
 
     @Override
     protected void displayResurrectedResult(ACLMessage inform) {
 
         try {
             Result r = (Result)getContentManager().extractContent(inform);
 
             Evaluation eval = (Evaluation)r.getValue();
 
             DataInputFrame did = myGUI.getDataInputDialog();
 
             if (did == null) {
                 return;
             }
 
             if (eval.getLabeled_data() == null || eval.getLabeled_data().size() == 0) {
                 System.err.println("No data instances");
             }
 
             DataInstances di = (DataInstances)eval.getLabeled_data().get(0);
             int cIdx = di.getClass_index();
 
             for (int i = 0; i < di.getInstances().size(); i++) {
                 Instance inst = (Instance)di.getInstances().get(i);
                 List newMissing = new jade.util.leap.ArrayList();
                 for (int j = 0; j < inst.getMissing().size(); j++) {
                     if (j == cIdx)
                         newMissing.add(false);
                     else
                         newMissing.add(inst.getMissing().get((j)));
                 }
                 inst.setMissing(newMissing);
             }
 
             did.setDataInstances((DataInstances)eval.getLabeled_data().get(0));
 
         }
         catch (CodecException ce) {
             ce.printStackTrace();
         }
         catch (OntologyException oe) {
             oe.printStackTrace();
         }
 
 
     }
 
     @Override
     protected void displayFileImportProgress(int completed, int all) {
         myGUI.showFileImportProgress(completed, all);
     }
 
     @Override
     protected void displayTaskResult(ACLMessage inform) {
 
         if (inform.getPerformative() != ACLMessage.INFORM) {
             myGUI.showError(ResourceBundle.getBundle("pikater/gui/java/improved/Strings").getString("TASK_FAILURE") + inform.getContent());
             return;
         }
 
         try {
             Result r = (Result) getContentManager().extractContent(inform);
             Task t = (Task) r.getValue();
             showResult(t);
         } catch (CodecException ex) {
             ex.printStackTrace();
         } catch (UngroundedException ex) {
             ex.printStackTrace();
         } catch (OntologyException ex) {
             ex.printStackTrace();
         }
     }
 }
