 package org.tigus.conversion;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import org.tigus.core.Answer;
 import org.tigus.core.Question;
 import org.tigus.core.QuestionSet;
 
 public class PlainTextQuestionConverter {
 
     /*
      * Class that transforms plain text questions into usable objects
      */
 
     private int state;
     private static final int NOQUESTIONSTATE = 0;
     private static final int TAGSTATE = 1;
     private static final int QUESTIONTEXTSTATE = 2;
     private static final int ANSWERSTATE = 3;
     private File inputFile;
     private QuestionSet qSet;
     private Question q;
 
     // no-question -> tags -> question-text -> answers -> begin
 
     public PlainTextQuestionConverter(File input) {
         inputFile = new File(input.getPath());
         qSet = new QuestionSet();
         q = new Question();
     }
 
     public PlainTextQuestionConverter(String path) {
         inputFile = new File(path);
         qSet = new QuestionSet();
         q = new Question();
     }
 
     public void parseFile() {
         FileReader inputReader;
 
         try {
             inputReader = new FileReader(inputFile);
             BufferedReader inputBuffer = new BufferedReader(inputReader);
 
             String line;
 
             state = NOQUESTIONSTATE;
             q = new Question();
 
             while ((line = inputBuffer.readLine()) != null) {
                 /*
                  * if the line is empty and it is not part of the question text
                  * or we are not in the answer state (where it separates the
                  * questions) we ignore it
                  */
 
                 if ((state != QUESTIONTEXTSTATE) && (state != ANSWERSTATE)
                         && (line.trim().length() == 0)) {
                     continue;
                 }
 
                 /*
                 * if we are in tag state or in no qustion state and the line
                  * starts with '@' we have tags to parse
                  */
                 if (((state == TAGSTATE) || (state == NOQUESTIONSTATE))
                         && line.startsWith("@")) {
                    state = TAGSTATE;
                     parseTags(line);
                 }
 
                 /*
                  * if we are in tag state or in question text state and the line
                 * doesn't start with '@' nor '+' or'-' we have question text to
                 * parse
                  */
 
                 if (((state == TAGSTATE) || (state == QUESTIONTEXTSTATE))
                         && (!line.startsWith("@")) && (!line.startsWith("+"))
                         && (!line.startsWith("-"))) {
                     state = QUESTIONTEXTSTATE;
                     parseQuestionText(line);
                 }
 
                 /*
                  * if we are in question text state or in answer state and the
                  * line starts with '+' or '-' we have answers to parse
                  */
 
                 if (((state == QUESTIONTEXTSTATE) || (state == ANSWERSTATE))
                         && (line.startsWith("+") || line.startsWith("-"))) {
                     state = ANSWERSTATE;
                     parseAnswer(line);
                 }
 
                 /*
                  * if we are in answer state and we have an empty line we jump
                  * to the next question (no question state)
                  */
 
                 if ((state == ANSWERSTATE) && (line.trim().length() == 0)) {
                     state = NOQUESTIONSTATE;
 
                     printQuestion();
                     qSet.add(q);
                     q = new Question();
                 }
 
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
     }
 
     public QuestionSet getQuestionSet() {
         return qSet;
     }
 
     private void printQuestion() {
         // question printing function for debug purposes only
 
         System.out.print("\n------------NEW QUESTION-------------\n");
         System.out.print("----------------TAGS-----------------\n");
 
         if (q.getTags().containsKey("chapter")) {
             for (String value : q.getTags().get("chapter")) {
                 System.out.print("chapter-" + value + " ,");
             }
         }
 
         if (q.getTags().containsKey("difficulty")) {
             for (String value : q.getTags().get("difficulty")) {
                 System.out.print("difficulty-" + value + " ,");
             }
         }
 
         if (q.getTags().containsKey("desc")) {
             for (String value : q.getTags().get("desc")) {
                 System.out.print("desc-" + value + " ,");
             }
         }
 
         System.out.print("\n----------------TEXT-----------------\n");
         System.out.print(q.getText());
 
         System.out.print("\n---------------ANSWER----------------\n");
         for (Answer ans : q.getAnswers()) {
             if (ans.isCorrect()) {
                 System.out.print("Correct:" + ans.getText() + "\n");
             }
             if (!ans.isCorrect()) {
                 System.out.print("Incorrect:" + ans.getText() + "\n");
             }
         }
     }
 
     private void parseAnswer(String line) {
 
         // we add the answer according to its true/false value
         if (line.charAt(0) == '+') {
             q.addAnswer(true, line.substring(1));
         }
         if (line.charAt(0) == '-') {
             q.addAnswer(false, line.substring(1));
         }
     }
 
     private void parseQuestionText(String line) {
 
         // if this is the first line we just add it otherwise we concatenate it
         if (q.getText().isEmpty()) {
             q.setText(line);
         } else {
             q.setText(q.getText().concat("\n"));
             q.setText(q.getText().concat(line));
         }
     }
 
     private void parseTags(String line) {
         // TODO Auto-generated method stub
         String tagName;
 
         // we delete the @ character marking the tag line
         line = line.substring(1);
 
         // we identify the tag name
         int i = 0;
         while (line.charAt(i) != ' ') {
             i++;
         }
 
         // we extract the tag name
         tagName = new String();
         tagName = line.substring(0, i);
 
         // we extract the tag attributes
         line = line.substring(i + 1);
 
         // we add the tag name and the tag attributes in the List form
         q.setTagValueList(tagName, line);
     }
 }
