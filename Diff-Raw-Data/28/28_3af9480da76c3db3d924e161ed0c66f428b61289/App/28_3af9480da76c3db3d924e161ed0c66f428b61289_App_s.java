 package dcll.grp3.morphoQuizz;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * Hello world!
  *
  */
 public class App 
 {
     public static void main( String[] args )
     {
         try {
			QuizzConverter.XMLQuizzToJson(new File("quiz-moodle-exemple.xml"), new File("test.json"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 }
