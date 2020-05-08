 package es.upm.dit.gsi.beast.reader;
 
 import jadex.base.fipa.SFipa;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.apache.ws.jaxme.js.JavaComment;
 import org.apache.ws.jaxme.js.JavaMethod;
 import org.apache.ws.jaxme.js.JavaQName;
 import org.apache.ws.jaxme.js.JavaQNameImpl;
 import org.apache.ws.jaxme.js.JavaSource;
 import org.apache.ws.jaxme.js.JavaSourceFactory;
 
 import es.upm.dit.gsi.beast.mocks.common.MockConfiguration;
 import es.upm.dit.gsi.beast.story.testCase.Scenario;
 
 /**
  * Main class to create the setup of each Test.
  * The evaluation represents the GIVEN part of our BDD Test.
  * 
  * @author Jorge Solitario
  */
 public class CreateScenario {
 
 	/**
 	 * Method to create the scenario of each test.
 	 * IT does not check if the file is already created, overwriting it.
 	 * 
 	 * @param scenario_name The name given by the client
 	 * @param package_path The package of the test
 	 * @param client_description The plain text given by the client in the THEN part
 	 * @param dest_dir the working directory (typically src/main/java)
 	 */
 	public static void createScenario(String scenario_name, String package_path, String client_description, String dest_dir){
 		
 		JavaSourceFactory factory = new JavaSourceFactory();
 		
 		JavaQName className = JavaQNameImpl.getInstance(package_path,"Scenario"+scenario_name);
 		JavaSource js = factory.newJavaSource(className, "public");
 		js.addExtends(Scenario.class);
 		js.addImport(SFipa.class);
 
 		js.newComment();
 		JavaComment classComment = js.getComment();
 		classComment.addLine(" ");
 		classComment.addLine("This is the class that must create the Scenario.");
 		classComment.addLine("It is related with the GIVEN part.");
 		classComment.addLine(" ");
 		classComment.addLine("In startAgents method the following method must be used");
 		classComment.addLine("   super.startAgent(agent_name,agent_path)");
 		classComment.addLine("   startMock(mock_name,mock_type,mock_configuration)");
 		classComment.addAuthor("Jorge Solitario");
 		
 		JavaMethod jm = js.newJavaMethod("startAgents","void","public");
 		jm.newComment();
		JavaComment methodComment = js.getComment();
 		methodComment.addLine(" ");
 		methodComment.addLine("Here the description given by the client must be written,");
 		methodComment.addLine("which is: "+client_description.toUpperCase());
 				
 		JavaMethod jm2 = js.newJavaMethod("startMock","void","private");
 		jm2.addParam(String.class,"mock_name");
 		jm2.addParam(String.class,"mock_type");
 		jm2.addParam(MockConfiguration.class,"mock_configuration");
 		jm2.addLine("startAgent(mock_name, mock_type);");
 		jm2.addLine("// The message to changes mock agent's df_service_name and its behaviour");
 		jm2.addLine("sendMessageToAgent(mock_name, SFipa.INFORM, mock_configuration);");
 		
 		jm2.newComment();
 		JavaComment methodComment2 = jm2.getComment();
 		methodComment2.addLine("This method is used to launch mock agents. First it creates them, with");
 		methodComment2.addLine("the generic df_service_name \"mock_agent\", and then the method sends to the");
 		methodComment2.addLine("agent a message with the new df_service_name and its behaviour.");
 		methodComment2.addLine(" ");
 		methodComment2.addLine("@param mock_name The name of the mock in the platform");
 		methodComment2.addLine("@param mock_type The type of the mock, taken from mocks.common.Definitions.[bridge,listener,repository]");
 		methodComment2.addLine("@param mock_configuration The behaviour of the mock and its df_service_name");
 		
 		try {
 			factory.write(new File(dest_dir));
 		} catch (IOException e) {
 			Logger logger = Logger.getLogger("CreateScenario.java");
 			logger.severe("ERROR writing the scenario of "+scenario_name);
 		}	
 	}
 }
