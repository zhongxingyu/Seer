 package jivko.brain.speech;
 
 import java.io.File;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import jivko.brain.movement.Command;
 import jivko.brain.movement.CommandsCenter;
 import jivko.util.Tree;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class UtterancesManager {
   static private final String XML_DOM_NODE_UTTERANCE = "utterance";
   static private final String XML_DOM_NODE_UTTERANCES = "utterances";
   static private final String XML_DOM_NODE_QUESTION = "question";
   static private final String XML_DOM_NODE_ANSWER = "answer";
   
   static private final String FAIL_ANSWER = "Ниче страшного";
   
   private Utterance rootUtterance = new Utterance();
   private Utterance currentUtterance = null;
 
   public Utterance getRootUtterance() {
     return rootUtterance;
   }
 
   private void setRootUtterance(Utterance rootUtterance) {
     this.rootUtterance = rootUtterance;
   }
 
   public Utterance getCurrentUtterance() {
     return currentUtterance;
   }
 
   public void setCurrentUtterance(Utterance currentUtterance) {
     this.currentUtterance = currentUtterance;
   }  
   
   public void initialize(String path) throws Exception {    
     readDialogList(path);    
     print();
     currentUtterance = rootUtterance;
   }
   
   public void reset() {
     currentUtterance = rootUtterance;
   }
 
   private void readDialogList(String path) throws Exception {
     
     DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
     f.setValidating(false);
     DocumentBuilder builder = f.newDocumentBuilder();
     Document doc = builder.parse(new File(path));
     
     Node firstUtterancesNode = doc.getFirstChild();
 
     while (firstUtterancesNode.getNodeType() != Document.ELEMENT_NODE 
             && firstUtterancesNode.getNextSibling() != null)
       firstUtterancesNode = firstUtterancesNode.getNextSibling();
     
     assert XML_DOM_NODE_UTTERANCE.equals(firstUtterancesNode.getNodeName());
     
     readUtterance(firstUtterancesNode, rootUtterance);
   }
   
   private void readUtterance(Node node, Utterance utterance) throws Exception {
     assert XML_DOM_NODE_UTTERANCE.equals(node.getNodeName());
 
     List<Command> commands = CommandsCenter.getInstance().getCommandsFromXmlElement(node);
     utterance.setCommands(commands);
     
     NodeList nl = node.getChildNodes();
     for (int i = 0; i < nl.getLength(); ++i) {
       Node n = nl.item(i);
       
       if (n.getNodeType() != Document.ELEMENT_NODE) 
         continue;
         
       String nodeName = n.getNodeName();            
       switch (nodeName) {
         case XML_DOM_NODE_QUESTION:
           {
             String nodeVal = n.getTextContent();
             utterance.setQuestion(nodeVal);
             break;
           }
         case XML_DOM_NODE_ANSWER:
           {
             String nodeVal = n.getTextContent();
             utterance.setAnswer(nodeVal);
             break;
           }
         case XML_DOM_NODE_UTTERANCES:
           NodeList nnl = n.getChildNodes();
           for (int j = 0; j < nnl.getLength(); ++j) {
             Node chNode = nnl.item(j);
             
             if (chNode.getNodeType() != Document.ELEMENT_NODE 
                    || XML_DOM_NODE_UTTERANCE.equals(chNode.getNodeName()))
               continue;
             
             Utterance chUtterance = new Utterance();      
             readUtterance(chNode, chUtterance);
             
             utterance.addNode(chUtterance);
           }
           break;
       }
     }
   }
 
   static public String getFailAnswer() {
     return FAIL_ANSWER;
   }
 
   public String findAnswer(String question) throws Exception {
     String answer = null;
 
     if (question == null) {
       return getFailAnswer();
     }
 
     answer = findAnswer(currentUtterance, question);
     if (answer == null)
       answer = findAnswer(rootUtterance, question);        
 
     return answer;
   }
   
   public String findAnswer(Utterance utterance, String question) throws Exception {
     String answer = null;
     Utterance uWithAnswer = utterance.findUtteranceWithAnswer(question);
     
     if (uWithAnswer != null) {
       answer = uWithAnswer.getRandomValue();
       CommandsCenter.getInstance().executeCommandList(uWithAnswer.getCommands());
       
       //update current pointer if this is a dialog
       if (uWithAnswer.getNodes() != null)
         currentUtterance = uWithAnswer;
     }
 
     return answer;
   }
   
   public void print() {
     String identity = "";
     printNode(rootUtterance, identity);
   }
   
   public void printNode(Utterance u, String identity) {
     String q = u.getQuestion();
     String a = u.getRandomValue();
     System.out.println(identity + "Question:" + q);
     System.out.println(identity + "Answer:" + a);
     
     identity = identity + "  ";
     
     List<Tree> chNodes = u.getNodes();
     for (Tree t : chNodes) {
       printNode((Utterance)t, identity);
     }
   }
 }
