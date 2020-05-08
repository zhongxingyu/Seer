 package org.jboss.tools.jst.jsp.test.ca;
 
 import junit.framework.Test;
 import junit.framework.TestSuite;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
 import org.jboss.tools.common.test.util.TestProjectProvider;
 import org.jboss.tools.jst.jsp.contentassist.AutoContentAssistantProposal;
 import org.jboss.tools.jst.jsp.test.TestUtil;
 import org.jboss.tools.test.util.JobUtils;
 
 public class JstJspJbide1641Test extends ContentAssistantTestCase {
 	TestProjectProvider provider = null;
 	boolean makeCopy = false;
 	private static final String PROJECT_NAME = "JsfJbide1641Test";
 	private static final String PAGE_NAME = "/WebContent/pages/greeting.xhtml";
 	private static final String PREFIX_STRING = "<h:commandButton a";
 	private static final String PROPOSAL_TO_APPLY_STRING = "action=\"\"";
 	private static final String ATTRIBUTE_TO_INSERT_STRING = "ction=\"\"";
 	private static final String POSTFIX_STRING = "></h:commandButton>";
 	private static final String INSERT_BEFORE_STRING = "<ui:composition";
 	private static final String INSERTION_STRING = PREFIX_STRING + POSTFIX_STRING;
 	private static final String COMPARE_STRING = PREFIX_STRING + ATTRIBUTE_TO_INSERT_STRING + POSTFIX_STRING;
 	
 	public static Test suite() {
 		return new TestSuite(JstJspJbide1641Test.class);
 	}
 
 	public void setUp() throws Exception {
 		provider = new TestProjectProvider("org.jboss.tools.jst.jsp.test", null, PROJECT_NAME, makeCopy); 
 		project = provider.getProject();
 		Throwable exception = null;
 		try {
 			project.refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (Exception x) {
 			exception = x;
 			x.printStackTrace();
 		}
 		assertNull("An exception caught: " + (exception != null? exception.getMessage() : ""), exception);
 	}
 
 	protected void tearDown() throws Exception {
 		if(provider != null) {
 			provider.dispose();
 		}
 	}
 
 	public void testJstJspJbide1641() {
 		
 		openEditor(PAGE_NAME);
 		
 		// Find start of <ui:composition> tag
 		String documentContent = document.get();
 		int start = (documentContent == null ? -1 : documentContent.indexOf(INSERT_BEFORE_STRING));
 		int offsetToTest = start + PREFIX_STRING.length();
 		
 		assertTrue("Cannot find the starting point in the test file  \"" + PAGE_NAME + "\"", (start != -1));
 		
 		String documentContentModified = documentContent.substring(0, start) +
 			INSERTION_STRING + documentContent.substring(start);
 		String documentContentToCompare = documentContent.substring(0, start) +
 			COMPARE_STRING + documentContent.substring(start);
 		
 		jspTextEditor.setText(documentContentModified);
 		
 		ICompletionProposal[] result= null;
 		String errorMessage = null;
 
 		IContentAssistProcessor p= TestUtil.getProcessor(viewer, offsetToTest, contentAssistant);
 		if (p != null) {
 			try {
 				result= p.computeCompletionProposals(viewer, offsetToTest);
 			} catch (Throwable x) {
 				x.printStackTrace();
 			}
 			errorMessage= p.getErrorMessage();
 		}
 		
 
		assertTrue("Content Assistant returned no proposals", (result != null && result.length > 0));
 
 		boolean bPropoosalToApplyFound = false;
 		for (int i = 0; i < result.length; i++) {
 			if (!(result[i] instanceof AutoContentAssistantProposal)) 
 				continue;
 			AutoContentAssistantProposal proposal = (AutoContentAssistantProposal)result[i];
 			String proposalString = proposal.getReplacementString();
 
 			if (PROPOSAL_TO_APPLY_STRING.equals(proposalString)) {
 				bPropoosalToApplyFound = true;
 				proposal.apply(document);
 				break;
 			}
 		}
 		assertTrue("The proposal to apply not found.", bPropoosalToApplyFound);
 
 		try {
 			JobUtils.waitForIdle();
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue("Waiting for the jobs to complete has failed.", false);
 		} 
 
 		String documentUpdatedContent = document.get();
 		assertTrue("The proposal replacement is failed.", documentContentToCompare.equals(documentUpdatedContent));
 		
 		closeEditor();
 	}
 
 }
