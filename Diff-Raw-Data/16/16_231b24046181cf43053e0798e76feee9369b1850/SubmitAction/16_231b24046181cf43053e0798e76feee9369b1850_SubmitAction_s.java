 package org.abratuhi.acmtimus.actions;
 
 import java.io.File;
 
 import org.abratuhi.acmtimus.Activator;
 import org.abratuhi.acmtimus.preferences.PreferenceConstants;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.URI;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 
 /**
  * This class represents a submit pop-up for the Package Explorer View in Eclipse.
  * It gives the user the opportunity to submit the solution directly from the Eclipse IDE.
  * 
  * @author Alexei Bratuhin
  * 
  */
 public class SubmitAction extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		try {
 			Shell shell = HandlerUtil.getActiveShell(event);
 			ISelection sel = HandlerUtil.getActiveMenuSelection(event);
 			IStructuredSelection selection = (IStructuredSelection) sel;
 			Object firstElement = selection.getFirstElement();
 			if (firstElement instanceof ICompilationUnit) {
 				submitSolution(shell, firstElement);
 
 			} else {
 				MessageDialog.openInformation(shell, "Info",
 						"Please select a Java source file");
 			}
 		} catch (Exception e) {
 			throw new ExecutionException("", e);
 		}
 		return null;
 	}
 
 	private void submitSolution(Shell shell, Object firstElement)
 			throws CoreException {
 		try {
 			ICompilationUnit cu = (ICompilationUnit) firstElement;
 			IResource res = cu.getResource();
 			File file = res.getLocation().toFile();
 
 			IPreferenceStore store = Activator.getDefault()
 					.getPreferenceStore();
 
 			String judgeId = store.getString(PreferenceConstants.PREF_JUDGEID);
 			String problemNum = file.getName().replaceAll("Problem", "")
 					.replace(".java", "");
 
 			HttpClient hc = new HttpClient();
 			hc.getHostConfiguration().setHost(
 					new URI("http://acm.timus.ru", true));
 			PostMethod post = new PostMethod("/submit.aspx");
 			Part[] parts = new Part[] { new StringPart("Action", "submit"),
 					new StringPart("SpaceID", "1"),
 					new StringPart("JudgeID", judgeId),
					new StringPart("Language", "7"),
 					new StringPart("ProblemNum", problemNum),
 					new StringPart("Source", ""),
 					new FilePart("SourceFile", file) };
 			MultipartRequestEntity mre = new MultipartRequestEntity(parts,
 					post.getParams());
 			post.setRequestEntity(mre);
 			int result = hc.executeMethod(post);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
