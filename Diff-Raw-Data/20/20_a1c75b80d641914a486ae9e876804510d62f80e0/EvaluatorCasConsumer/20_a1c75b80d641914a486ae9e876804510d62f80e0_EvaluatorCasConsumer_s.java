 package edu.cmu.deiis.types;
 
 import java.util.Iterator;
 
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIndex;
 
 import edu.cmu.deiis.types.AnswerScore;
 
 public class EvaluatorCasConsumer extends CasConsumer_ImplBase{
   public void processCas(CAS aCAS) throws ResourceProcessException {
     JCas aJcas;
     try {
       aJcas = aCAS.getJCas();
     } catch (CASException e) {
       throw new ResourceProcessException(e);
     }
     FSIndex answerScoreIndex=aJcas.getAnnotationIndex(edu.cmu.deiis.types.AnswerScore.type);
     Iterator answerScoreIter=answerScoreIndex.iterator();
     int totalCorrectNum=0;
     int totalNum=0;
     double threshold=0.33; // can tune this parameter
     while(answerScoreIter.hasNext()){
       // for each answer, print the gold standard score (0 or 1) and the
       // given scorer's score (map to {0,1} using a threshold), then
       // count the match percentage. This is done according Alkesh Patel's email.
       AnswerScore answerScore=(AnswerScore)answerScoreIter.next();
       totalNum++;
       if(answerScore.getAnswer().getIsCorrect()){
         System.out.print("1 ");
       }
       else{
         System.out.print("0 ");
       }
       if(answerScore.getScore()>=threshold){
         System.out.print("1 ");
         if(answerScore.getAnswer().getIsCorrect()){
           totalCorrectNum++;
         }
       }
       else{
         System.out.print("0 ");
         if(!answerScore.getAnswer().getIsCorrect()){
           totalCorrectNum++;
         }
       }
       System.out.println(answerScore.getAnswer().getCoveredText());
     }
    System.out.println("Accuracy: "+totalCorrectNum*1.0/totalNum + "\n");
   }
 }
