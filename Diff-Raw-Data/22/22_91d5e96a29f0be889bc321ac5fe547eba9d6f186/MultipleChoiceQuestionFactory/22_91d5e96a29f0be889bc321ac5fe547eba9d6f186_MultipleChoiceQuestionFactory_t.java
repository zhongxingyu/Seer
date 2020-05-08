 package models.question;
 
 import models.data.Language;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A factory where MultipleChoiceQuestion can be created
  * @author Ruben Taelman
  *
  */
 public class MultipleChoiceQuestionFactory extends QuestionFactory<MultipleChoiceQuestion>{
 
     /**
      * Creates a new MultipleChoiceQuestionFactory
      */
     public MultipleChoiceQuestionFactory() {
         super();
     }
 
     @Override
     public Question newQuestion(Node node) throws QuestionBuilderException {
         this.nodeActions.put("answers", new AnswersNodeAction());
         MultipleChoiceQuestion question = new MultipleChoiceQuestion();
         this.processCommonElements(question, node);
         return question;
     }
     
     @Override
     public Question newQuestion() {
         return new MultipleChoiceQuestion();
     }
 
     /**
      * A NodeAction for the Answer elements that are present in MultipleChoice Questions
      */
     protected class AnswersNodeAction extends NodeAction{
 
         public static final String ELEMENT_ANSWER="answer";
         public static final String ATTRIBUTE_ANSWER_CORRECT="correct";
 
         @Override
         public void processValue(MultipleChoiceQuestion question,
                 Language language, String value, NodeList nodeList,
                 NamedNodeMap attributes) throws QuestionBuilderException {
             boolean containsOneCorrect = false;
             // Loop over the answer-nodes
             for(int i=0;i<nodeList.getLength();i++) {
                 Node answerNode = nodeList.item(i);
 
                 // Only check valid xml-elements
                 if(answerNode.getNodeName().equals(ELEMENT_ANSWER)) {
                     // Add the current element to the Question
                     MultipleChoiceElement element = new MultipleChoiceElement(getNodeValue(answerNode));
                     question.addElement(language, element);
 
                     // Check if the element is the correct one, and add those one to the Question
                     Node attribute = answerNode.getAttributes().getNamedItem(ATTRIBUTE_ANSWER_CORRECT);
 
                     if(attribute!=null && attribute.getNodeValue().equals("true")) {
                         question.setCorrectElement(language, element);
                         // Throw exception if there already was a correct answer
                         if(containsOneCorrect) {
                            throw new QuestionBuilderException("The answers for language "+language.getName()+" contain more than one correct answers.");
                         }
                         containsOneCorrect = true;
                     }
                 }
             }
 
             // Throw exception if there are no correct answers in the answer list
             if(!containsOneCorrect) {
                throw new QuestionBuilderException("The answers for language "+language.getName()+" contain no correct answers.");
             }
         }
 
     }
 
 }
