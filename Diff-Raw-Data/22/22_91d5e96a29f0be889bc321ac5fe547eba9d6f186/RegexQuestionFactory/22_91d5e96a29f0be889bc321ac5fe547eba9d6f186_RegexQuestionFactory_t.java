 package models.question;
 
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import models.data.Language;
 
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * A factory where MultipleChoiceQuestion can be created
  * @author Ruben Taelman
  *
  */
 public class RegexQuestionFactory extends QuestionFactory<RegexQuestion> {
 
     /**
      * Creates a new RegexQuestionFactory
      */
     public RegexQuestionFactory() {
         super();
     }
 
     @Override
     public Question newQuestion(Node node) throws QuestionBuilderException {
         this.nodeActions.put("input", new InputNodeAction());
         RegexQuestion question = new RegexQuestion();
         this.processCommonElements(question, node);
         return question;
     }
     
     @Override
     public Question newQuestion() {
         return new RegexQuestion();
     }
 
     /**
      * Check if a certain regex has a valid structure
      * @param regex regex to check
      * @return is the given regex valid
      */
     public static boolean isValidRegex(String regex) {
         try {
             Pattern.compile(regex);
         } catch (PatternSyntaxException exception) {
             return false;
         }
         return true;
     }
 
     /**
      * A NodeAction for the Input elements that are present in Regex Questions
      */
     protected class InputNodeAction extends NodeAction{
 
         public static final String ATTRIBUTE_REGEX="regex";
 
         @Override
         public void processValue(RegexQuestion question,
                 Language language, String value, NodeList nodeList,
                 NamedNodeMap attributes) throws QuestionBuilderException {
             // Get the regex value and save it in the question
             Node regexAttribute = attributes.getNamedItem(ATTRIBUTE_REGEX);
             String regex = regexAttribute.getNodeValue();
             if(isValidRegex(regex))
                 question.setRegex(language, regex);
             else
                throw new QuestionBuilderException("Invalid regular expression found in language "+language.getName());
         }
 
     }
 
 }
