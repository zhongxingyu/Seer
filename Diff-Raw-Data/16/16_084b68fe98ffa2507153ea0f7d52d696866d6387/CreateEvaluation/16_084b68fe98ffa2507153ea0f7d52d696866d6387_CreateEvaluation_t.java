 package es.upm.dit.gsi.beast.reader;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.ws.jaxme.js.JavaComment;
 import org.apache.ws.jaxme.js.JavaMethod;
 import org.apache.ws.jaxme.js.JavaQName;
 import org.apache.ws.jaxme.js.JavaQNameImpl;
 import org.apache.ws.jaxme.js.JavaSource;
 import org.apache.ws.jaxme.js.JavaSourceFactory;
 
 
 import es.upm.dit.gsi.beast.story.testCase.Evaluation;
 
 /**
  * Main class to create the evaluation of each Test.
  * The evaluation represents the THEN part of our BDD Test.
  * 
  * @author Jorge Solitario
  */
 public class CreateEvaluation {
 
 	/**
 	 * Method to create the evaluation of each test.
 	 * IT does not check if the file is already created, overwriting it.
 	 * 
 	 * @param scenario_name The name given by the client
 	 * @param path The package of the test
 	 * @param client_description The plain text given by the client in the THEN part
 	 * @param dest_dir the working directory (typically src/main/java)
 	 */
 	public static void createEvaluation(String scenario_name, String path, String client_description, String dest_dir){
 		JavaSourceFactory factory = new JavaSourceFactory();
 		
 		JavaQName className = JavaQNameImpl.getInstance(path,"Evaluation"+scenario_name);
 		JavaSource js = factory.newJavaSource(className, "public");
 		
 		js.newComment();
 		JavaComment classComment = js.getComment();
 		classComment.addLine(" ");
 		classComment.addLine("This is the class that must create the Evaluation.");
 		classComment.addLine("It is related with the THEN part.");
 		classComment.addLine(" ");
 		classComment.addLine("In checkStates method the following method must be used");
 		classComment.addLine("   super.checkAgentsBeliefEquealsTo(agent_name,belief_name,expected_belief_value)");
 		classComment.addAuthor("Jorge Solitario");
 		
 		JavaMethod jm = js.newJavaMethod("checkStates","void","public");
 		jm.newComment();
		JavaComment methodComment = jm.getComment();
 		methodComment.addLine(" ");
 		methodComment.addLine("Here the description given by the client must be written,");
 		methodComment.addLine("which is: "+client_description.toUpperCase());
 		
 		js.addExtends(Evaluation.class);
 		try {
 			factory.write(new File(dest_dir));
 		} catch (IOException e) {
 			Logger logger = Logger.getLogger("CreateEvaluation");
 			logger.severe("ERROR writing the evaluation of "+scenario_name);
 		}		
 	}
 }
