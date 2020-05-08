 package ups.m2glre.rossf1.parser;
 
 import org.jdom.Element;
 
 import universite.toulouse.moodlexmlapi.core.InvalidQuizFormatException;
 import ups.m2glre.rossf1.question.EssayQuestion;
 
 public class EssayQuestionParser extends QuestionParser {
 
     public void parseSpecializedQuestion(Element questionXML)
             throws InvalidQuizFormatException {
         EssayQuestion q = (EssayQuestion) question;
 
         try {
           //Parse la fraction
             q.setAnswerFraction(Integer.valueOf(
                     questionXML.getChild("answer").getAttributeValue("fraction")));
             //Parse la valeur
             q.setAnswerValue(Integer.valueOf(
                    questionXML.getChild("answer").getChild("text").getValue()));
             //Parse la answer shuffle
             q.setAnswerShuffle(parseAnswerShuffle(questionXML));
         } catch (Exception e) {
             throw new InvalidQuizFormatException(e.getCause());
         }
     }
 }
