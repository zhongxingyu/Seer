 package org.jboss.tools.jst.jsp.test.ca;
 
 import org.jboss.tools.common.test.util.TestProjectProvider;
 
 public class CAForIDTest extends ContentAssistantTestCase{
 	TestProjectProvider provider = null;
	boolean makeCopy = false;
 	private static final String PROJECT_NAME = "CAForIDTest";
 	private static final String PAGE_NAME = "/WebContent/pages/inputUserName.jsp";
 	
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.jsp.test", null, PROJECT_NAME, makeCopy); 
 		project = provider.getProject();
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 	
 	public void testCAForIDTest(){
 		String[] proposals = {
 			"greetingForm",
 		};
 
 		checkProposals(PAGE_NAME, "<a4j:commandButton focus=\"\"/>", 26, proposals, false);
 	}
 
 	public void testCAForConverterIDTest(){
 		String[] proposals = {
 			"converter1",
 		};
 
 		checkProposals(PAGE_NAME, "<h:inputText value=\"#{user.name}\" required=\"true\" converter=\"\">", 61, proposals, false);
 	}
 
 	public void testCAForValidatorIDTest(){
 		String[] proposals = {
 			"validator1",
 		};
 
 		checkProposals(PAGE_NAME, "<f:validator validatorId=\"\" />", 26, proposals, false);
 	}
 }
