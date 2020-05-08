 package uk.co.mtford.jalp;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.*;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.DefinitionException;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.*;
 import uk.co.mtford.jalp.abduction.parse.program.JALPParser;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 import uk.co.mtford.jalp.abduction.parse.query.JALPQueryParser;
 import uk.co.mtford.jalp.abduction.rules.RuleNode;
 import uk.co.mtford.jalp.abduction.rules.visitor.FifoRuleNodeVisitor;
 import uk.co.mtford.jalp.abduction.rules.visitor.RuleNodeVisitor;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mtford
  * Date: 27/05/2012
  * Time: 08:33
  * To change this template use File | Settings | File Templates.
  */
 public class JALPSystem {
 
     private static final Logger LOGGER = Logger.getLogger(JALPSystem.class);
     private static final int MAX_EXPANSIONS = Integer.MAX_VALUE;
 
     private AbductiveFramework framework;
 
     public JALPSystem(AbductiveFramework framework) {
         this.framework = framework;
     }
 
     public JALPSystem(String fileName) throws FileNotFoundException, ParseException {
         framework = JALPParser.readFromFile(fileName);
     }
 
     public JALPSystem(String[] fileNames) {
         try {
             setFramework(fileNames);
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     public JALPSystem() {
         framework = new AbductiveFramework();
     }
 
     public void mergeFramework(AbductiveFramework newFramework) {
         if (framework == null) {
             framework = newFramework;
         } else {
             framework.getP().addAll(newFramework.getP());
             framework.getIC().addAll(newFramework.getIC());
             framework.getA().putAll(newFramework.getA());
         }
     }
 
     public void mergeFramework(File file) throws FileNotFoundException, ParseException {
         mergeFramework(JALPParser.readFromFile(file.getPath()));
     }
 
     public void mergeFramework(String query) throws ParseException {
         mergeFramework(JALPParser.readFromString(query));
     }
 
     private void reset() {
         framework = new AbductiveFramework();
     }
 
     public AbductiveFramework getFramework() {
         return framework;
     }
 
     public void setFramework(AbductiveFramework framework) {
         this.framework = framework;
     }
 
     public void setFramework(String fileName) throws FileNotFoundException, ParseException {
         framework = JALPParser.readFromFile(fileName);
     }
 
     public void setFramework(String[] fileName) throws FileNotFoundException, ParseException {
         for (String f:fileName) {
             mergeFramework(JALPParser.readFromFile(f));
         }
     }
 
     public List<Result> processQuery(List<IInferableInstance> query, Heuristic heuristic) throws Exception {
         RuleNodeIterator iterator = new RuleNodeIterator(new LinkedList<IInferableInstance>(query),heuristic);
         return performDerivation(query, iterator);
     }
 
     public List<Result> processQuery(String query, Heuristic heuristic) throws Exception, uk.co.mtford.jalp.abduction.parse.query.ParseException {
        LinkedList<IInferableInstance> queryList =  new
                 LinkedList<IInferableInstance>(JALPQueryParser.readFromString(query));
         RuleNodeIterator iterator = new RuleNodeIterator(new LinkedList<IInferableInstance>(queryList),heuristic);
         return performDerivation(queryList,iterator);
     }
 
     public RuleNode processQuery(List<IInferableInstance> query, Heuristic heuristic, List<Result> results) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, Exception {
         RuleNodeIterator iterator = new RuleNodeIterator(new LinkedList<IInferableInstance>(query),heuristic);
         RuleNode root = iterator.getCurrentNode();
         results.addAll(performDerivation(query,iterator));
         return root;
     }
 
     private List<Result> performDerivation(List<IInferableInstance> query, RuleNodeIterator iterator) {
         LinkedList<Result> resultList = new LinkedList<Result>();
         RuleNode currentNode = iterator.getCurrentNode();
         RuleNode rootNode = currentNode;
 
         int expansions = 0;
 
         while (iterator.hasNext()) {
             expansions++;
             if (expansions>=MAX_EXPANSIONS) {
                 System.err.println("ERROR: Reached max number of expansions.");
                 System.err.flush();
                 break;
             }
             if (currentNode.getNodeMark()==RuleNode.NodeMark.SUCCEEDED) {
                 Result result = new Result(currentNode.getStore(),currentNode.getAssignments(),query,rootNode,currentNode.getConstraintSolver());
                 resultList.add(result);
             }
             try {
                 currentNode = iterator.next();
             } catch (Exception e) {
                System.err.println("Encounted an exception. Returning results collected so far...");
                e.printStackTrace();
             }
 
         }
 
         return resultList;
     }
 
     public List<Result> generateDebugFiles(List<IInferableInstance> query, String folderName) throws Exception, JALPException, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         File folder = new File(folderName);
         FileUtils.touch(folder);
         FileUtils.forceDelete(folder);
         folder = new File(folderName);
         Appender R = Logger.getRootLogger().getAppender("R");
         Level previousLevel = Logger.getRootLogger().getLevel();
         Logger.getRootLogger().removeAppender("R");
         Logger.getRootLogger().setLevel(Level.DEBUG);
         FileAppender newAppender = new DailyRollingFileAppender(new PatternLayout("%d{dd-MM-yyyy HH:mm:ss} %C %L %-5p: %m%n"), folderName+"/log.txt", "'.'dd-MM-yyyy");
         newAppender.setName("R");
         Logger.getRootLogger().addAppender(newAppender);
         LOGGER.info("Abductive framework is:\n"+framework);
         LOGGER.info("Query is:" + query);
         List<Result> results = new LinkedList<Result>();
         RuleNode root = processQuery(query,Heuristic.NONE,results);
         JALP.getVisualizer(folderName + "/visualizer", root);
         int rNum = 1;
         LOGGER.info("Found "+results.size()+" results");
         for (Result r:results) {
             LOGGER.info("Result "+rNum+" is\n"+r);
             rNum++;
         }
         Logger.getRootLogger().removeAppender("R");
         Logger.getRootLogger().addAppender(R);
         Logger.getRootLogger().setLevel(previousLevel);
         return results;
     }
 
     public List<Result> generateDebugFiles(String query, String folderName) throws IOException, Exception, uk.co.mtford.jalp.abduction.parse.query.ParseException {
         return generateDebugFiles(new LinkedList<IInferableInstance>(JALPQueryParser.readFromString(query)),folderName);
     }
 
 
         public RuleNodeIterator getRuleNodeIterator(List<IInferableInstance> query, Heuristic heuristic) throws Exception {
         return new RuleNodeIterator(query,heuristic);
     }
 
     public class RuleNodeIterator implements Iterator<RuleNode> {
 
         private RuleNode currentNode;
         private RuleNodeVisitor nodeVisitor;
 
         public RuleNode getCurrentNode() {
             return currentNode;
         }
 
         public void setCurrentNode(RuleNode currentNode) {
             this.currentNode = currentNode;
         }
 
         private RuleNodeIterator(List<IInferableInstance> goals, Heuristic heuristic) throws Exception {
             switch (heuristic) {
                 case NONE:
                     currentNode = goals.remove(0).getPositiveRootRuleNode(framework,goals);
                     nodeVisitor = new FifoRuleNodeVisitor(currentNode);
                     break;
                 default: throw new JALPException("No such heuristic exists.");
             }
         }
 
         @Override
         public boolean hasNext() {
             return nodeVisitor.hasNextNode();
         }
 
         @Override
         public RuleNode next() {
             try {
                 currentNode=nodeVisitor.stateRewrite();
             } catch (Exception e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
             return currentNode;
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     public enum Heuristic { // TODO
         NONE
     }
 }
