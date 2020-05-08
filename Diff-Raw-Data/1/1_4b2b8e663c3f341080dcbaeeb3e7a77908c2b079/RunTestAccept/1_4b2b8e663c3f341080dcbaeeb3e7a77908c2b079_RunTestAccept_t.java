 package AcceptTests.Simulator;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import easyaccept.EasyAcceptFacade;
 
 public class RunTestAccept implements Serializable {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static void main(String[] args) throws Exception {
 		testCreateAcount();
 	}
 
	
 	private static void testCreateAcount() {
 		List<String> files = new ArrayList<String>();
 
 		files.add("src/AcceptTests/CreateAcount/US01.txt");
 		files.add("src/AcceptTests/CreateAcount/US02.txt");
 		files.add("src/AcceptTests/CreateAcount/US03.txt");
 		files.add("src/AcceptTests/CreateAcount/US04.txt");
 		files.add("src/AcceptTests/CreateAcount/US05.txt");
 		files.add("src/AcceptTests/CreateAcount/US06.txt");
 		files.add("src/AcceptTests/CreateAcount/US07.txt");
 		files.add("src/AcceptTests/CreateAcount/US08.txt");
 		files.add("src/AcceptTests/CreateAcount/US09.txt");
 
 		CreateAcountSimulator fachada = new CreateAcountSimulator();
 
 		EasyAcceptFacade eaFacade = new EasyAcceptFacade(fachada, files);
 
 		eaFacade.executeTests();
 
 		System.out.println(eaFacade.getCompleteResults());
 	}
 }
