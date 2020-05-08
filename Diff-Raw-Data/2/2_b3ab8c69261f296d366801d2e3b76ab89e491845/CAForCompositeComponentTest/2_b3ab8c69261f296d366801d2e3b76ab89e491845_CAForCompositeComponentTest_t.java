 package org.jboss.tools.jst.jsp.test.ca;
 
 import org.jboss.tools.common.test.util.TestProjectProvider;
 
 public class CAForCompositeComponentTest extends ContentAssistantTestCase{
 	TestProjectProvider provider = null;
	boolean makeCopy = true;
 	private static final String PROJECT_NAME = "CAForCompositeComponentTest";
 	private static final String PAGE_NAME = "/WebContent/pages/greeting.xhtml";
 	
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.jsp.test", null, PROJECT_NAME, makeCopy); 
 		project = provider.getProject();
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 	
 	public void testCAForCompositeComponent(){
 		String[] proposals = {
 			"sample:tag", "sample:tag2", "sample:tag3"
 		};
 
 		checkProposals(PAGE_NAME, "<sample:tag />", 8, proposals, false);
 
 		proposals = new String[]{
 			"aaa"
 		};
 		checkProposals(PAGE_NAME, "<sample:tag />", 12, proposals, false);
 	}
 }
