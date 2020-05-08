 package uk.co.mtford.jalp;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.logic.instance.constraints.IConstraintInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IEqualityInstance;
 import uk.co.mtford.jalp.abduction.parse.program.JALPParser;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 import uk.co.mtford.jalp.abduction.parse.program.TokenMgrError;
 import uk.co.mtford.jalp.abduction.parse.query.JALPQueryParser;
 import uk.co.mtford.jalp.abduction.rules.LeafRuleNode;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 import uk.co.mtford.jalp.abduction.rules.visitor.FifoRuleNodeVisitor;
 import uk.co.mtford.jalp.abduction.rules.visitor.RuleNodeVisitor;
 
 import java.io.BufferedWriter;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * Main entry point. Provides a terminal like interface.
  *
  * @author mtford
  */
 public class Main {
 
     private static final Logger LOGGER = Logger.getLogger(Main.class);
     private static final Scanner sc = new Scanner(System.in);
 
     // Command line options.
     private static final String DEBUG_OPTION = "-d";
     private static final String JSON_OPTION = "-j";
     private static final String REDUCE_OPTION = "-r";
     private static final String FILE_OPTION = "-f";
     private static final String QUERY_OPTION = "-q";
 
     private static final String JS_FILE_NAME = "../visualizer2/output.js";
 
     private static boolean debugMode = false;
     private static boolean jsonMode = false;
     private static boolean reduceMode = false;
 
     private static JALPSystem system = new JALPSystem();
 
     private static void fatalError(String error) {
         System.err.println(error);
         System.exit(-1);
     }
 
     private static void fatalError(String error, Throwable t) {
         System.err.println(error);
         t.printStackTrace(System.out);
         System.exit(-1);
     }
 
     private static void printMessage(String str) {
         System.out.println(str);
     }
 
     private static void processQuery(String query) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, JALPException, IOException {
         List<IInferableInstance> predicates = new LinkedList<IInferableInstance>(JALPQueryParser.readFromString(query));
         List<IInferableInstance> goals = new LinkedList<IInferableInstance>();
         Set<VariableInstance> queryVariables = new HashSet<VariableInstance>();
         goals.addAll(predicates);
         for (IInferableInstance inferable:goals) {
             queryVariables.addAll(inferable.getVariables());
         }
 
         if (debugMode) debugMode(predicates);
         else if (jsonMode) jsonMode(predicates);
         else standardMode(predicates, queryVariables);
 
         printMessage("Exiting...");
     }
 
     private static void standardMode(List<IInferableInstance> predicates, Set<VariableInstance> queryVariables) throws JALPException {
         List<Result> results = system.processQuery(predicates, JALPSystem.Heuristic.NONE);
         printResults(queryVariables, results);
     }
 
     private static void jsonMode(List<IInferableInstance> predicates) throws IOException, JALPException {
         RuleNode rootNode = system.getDerivationTree(predicates, JALPSystem.Heuristic.NONE);
         printJSON(JS_FILE_NAME, rootNode);
     }
 
     private static void debugMode(List<IInferableInstance> predicates) throws JALPException {
         printMessage("Framework is as follows:\n\n"+system.getFramework()+"\n");
         printMessage("Query is as follows:\n\n"+predicates+"\n");
         JALPSystem.RuleNodeIterator iterator = system.getRuleNodeIterator(new LinkedList<IInferableInstance>(predicates), JALPSystem.Heuristic.NONE);
         while (iterator.hasNext()) {
             RuleNode currentNode = iterator.next();
             printMessage("\nCurrent state for query " + predicates + " is");
             printMessage("==============================================================");
             printMessage(currentNode.toString());
             printMessage("==============================================================");
             printMessage("Enter c to continue or anything else to quit.");
             String s = sc.nextLine();
             if (s.trim().equals("c")||s.trim().equals("cc")||s.trim().equals("ccc")) {
                 continue;
             }
             else {
                 break;
             }
         }
     }
 
     private static void printResults(Set<VariableInstance> queryVariables, List<Result> leafRuleNodes) {
        printMessage("There are "+leafRuleNodes.size()+" explanations for the query "+leafRuleNodes.get(0).getQuery());
         for (int i=0;i< leafRuleNodes.size();i++) {
             printMessage("Enter c to see next explanation or anything else to quit.");
             String s = sc.nextLine();
             if (s.trim().equals("c")||s.trim().equals("cc")||s.trim().equals("ccc")) {
                 Map<VariableInstance,IUnifiableAtomInstance> assignments = leafRuleNodes.get(i).getAssignments();
                 List<PredicateInstance> abducibles = leafRuleNodes.get(i).getStore().abducibles;
                 List<DenialInstance> denials = leafRuleNodes.get(i).getStore().denials;
                 List<IEqualityInstance> equalities = leafRuleNodes.get(i).getStore().equalities;
                 List<IConstraintInstance> constraints = leafRuleNodes.get(i).getStore().constraints;
 
                 if (reduceMode) {   // TODO: Use the result in JALP System.
                     List<PredicateInstance> substAbducibles = new LinkedList<PredicateInstance>();
                     List<DenialInstance> substDenials = new LinkedList<DenialInstance>();
                     List<IEqualityInstance> substEqualities = new LinkedList<IEqualityInstance>();
                     List<IConstraintInstance> substConstraints = new LinkedList<IConstraintInstance>();
 
                     Set<VariableInstance> relevantVariables = new HashSet<VariableInstance>(queryVariables);
                     HashMap<VariableInstance,IUnifiableAtomInstance> relevantAssignments = new HashMap<VariableInstance, IUnifiableAtomInstance>();
 
 
                     for (PredicateInstance a:abducibles) {
                         substAbducibles.add((PredicateInstance)a.performSubstitutions(assignments));
                     }
 
                     for (DenialInstance d:denials) {
                         substDenials.add((DenialInstance) d.performSubstitutions(assignments));
                     }
 
                     for (IEqualityInstance e:equalities) {
                         substEqualities.add((IEqualityInstance) e.performSubstitutions(assignments));
                     }
 
                     for (IConstraintInstance c:constraints) {
                         substConstraints.add((IConstraintInstance)c.performSubstitutions(assignments));
                     }
 
                     Set<IUnifiableAtomInstance> keySet = new HashSet<IUnifiableAtomInstance>(assignments.keySet());
 
                     for (IUnifiableAtomInstance key:keySet) {
 
 
                         if (queryVariables.contains(key)) {
                             IUnifiableAtomInstance value = assignments.get(key);
 
                             while (keySet.contains(value)) value = assignments.get(value);
                             relevantAssignments.put((VariableInstance) key,value);
                         }
                     }
 
                     assignments=relevantAssignments;
                     abducibles=substAbducibles;
                     denials = substDenials;
                     equalities=substEqualities;
                     constraints=substConstraints;
                 }
 
                 printMessage("==============================================================");
                 printMessage("Abducibles: "+abducibles);
                 printMessage("Assignments: "+assignments);
                 printMessage("Denials: "+denials);
                 printMessage("Equalities: "+equalities);
                 printMessage("Finite-Domain Constraints: "+constraints);
                 printMessage("==============================================================");
                 printMessage("There are "+(leafRuleNodes.size()-1-i)+" explanations remaining.");
                 continue;
             }
             else {
                 break;
             }
         }
     }
 
     private static void printJSON(String fileName, RuleNode root) throws IOException {
         FileWriter fstream = new FileWriter(fileName);
         BufferedWriter out = new BufferedWriter(fstream);
         String js = "var data=\""+root.toJSON()+"\"";
         out.write(js);
         out.close();
     }
 
     public static void main(String[] args) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, DefinitionException, IOException {
         boolean console = false;
         boolean file = false;
         boolean query = false;
         String fileName = null;
         String queryString = null;
         AbductiveFramework f = new AbductiveFramework();
 
         for (int i = 0; i < args.length; i++) {
             String arg = args[i];
 
             if (arg.equals(FILE_OPTION)) {
                 if (console) {
                     fatalError("Can choose either console or file.");
                 }
                 if (file) {
                     fatalError("Already specified a file.");
                 }
                 file = true;
                 i++;
                 fileName = args[i];
             }
             else if (arg.equals(QUERY_OPTION)) {
                 query = true;
                 i++;
                 queryString = "";
                 if (args.length>i)
                     queryString = args[i];
             }
             else if (arg.equals(DEBUG_OPTION)) {
                 debugMode = true;
 
             }
             else if (arg.equals(JSON_OPTION)) {
                 if (debugMode) {
                     fatalError("JSON mode not available during debug.");
                 }
                 jsonMode = true;
             }
             else if (arg.equals(REDUCE_OPTION)) {
                 reduceMode = true;
             }
             else {
                 fatalError("Argument error.");
             }
         }
 
         if (file) {
             System.out.println("Reading from " + fileName);
 
             try {
                 f = JALPParser.readFromFile(fileName);
                 System.out.println("Successfully read " + fileName);
             } catch (FileNotFoundException ex) {
                 LOGGER.error("Cannot find file.");
                 file = false;
             } catch (ParseException ex) {
                 LOGGER.error("Syntax error in " + fileName, ex);
                 file = false;
             }
 
         }
 
         if (!query) {
             fatalError("No query specified.");
         }
 
         if (!file) {
             fatalError("No file specified.");
         }
 
         system.mergeFramework(f);
         try {
             processQuery(queryString);
         } catch (JALPException e) {
             fatalError("Implementation error",e);
         }
 
     }
 
 }
