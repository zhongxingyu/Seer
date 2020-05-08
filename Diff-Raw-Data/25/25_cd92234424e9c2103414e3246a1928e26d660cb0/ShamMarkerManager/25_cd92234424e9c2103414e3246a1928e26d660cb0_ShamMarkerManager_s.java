 /**
  * 
  */
 package org.rubypeople.rdt.internal.core.builder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.eclipse.shams.resources.ShamFile;
 import org.rubypeople.rdt.core.compiler.IProblem;
 import org.rubypeople.rdt.internal.core.parser.RdtPosition;
 import org.rubypeople.rdt.internal.core.parser.TaskTag;
 import org.rubypeople.rdt.internal.core.util.ListUtil;
 
 public class ShamMarkerManager implements IMarkerManager {
 
     private IFile fileArg;
     private List tasksArg;
     private String messageArg;
     private int lineArg;
     private int startOffsetArg;
     private int endOffsetArg;
     private SyntaxException syntaxExceptionArg;
     private List resourcesRemoved = new ArrayList();
 
     public void removeProblemsAndTasksFor(IResource resource) {
         resourcesRemoved.add(resource);
     }
 
     public void assertMarkersRemovedFor(IResource expectedResource) {
         assertMarkersRemovedFor(ListUtil.create(expectedResource));
     }
 
 
     public void assertMarkersRemovedFor(List expectedFiles) {
         Assert.assertEquals(expectedFiles, resourcesRemoved);
         
     }
     public void createSyntaxError(IFile file, SyntaxException syntaxException) {
         fileArg = file;
         syntaxExceptionArg = syntaxException;
     }
 
    public void assertErrorCreated(ShamFile expectedFile, SyntaxException expectedSyntaxException) {
        Assert.assertEquals("file", expectedFile, fileArg);
        Assert.assertEquals("syntaxException", expectedSyntaxException, syntaxExceptionArg);
    }

     public void createTasks(IFile file, List<TaskTag> tasks) throws CoreException {
         fileArg = file;
         tasksArg = tasks;
     }
 
     public void assertTasksCreated(IFile expectedFile, List expectedTasks) {
         Assert.assertEquals("file", expectedFile, fileArg);
         Assert.assertEquals("tasks", expectedTasks, tasksArg);
     }
 
     public void assertWarningAdded(IFile file, String message) {
         Assert.assertEquals("File", file, fileArg);
         Assert.assertEquals("Warning Message", message, messageArg);
     }
 
     public void addWarning(IFile file, String message) {
         fileArg = file;
         messageArg = message;
     }
     
     public void assertWarningAdded(IFile file, String message, int line, int startOffset, int endOffset) {
         Assert.assertEquals("File", file, fileArg);
         Assert.assertEquals("Warning Message", message, messageArg);
         Assert.assertEquals("line", line, lineArg);
         Assert.assertEquals("startOffset", startOffset, startOffsetArg);
         Assert.assertEquals("endOffset", endOffset, endOffsetArg);
     }
 
     public void addWarning(IFile file, String message, int line, int startOffset, int endOffset) {
         fileArg = file;
         messageArg = message;
         lineArg = line;
         startOffsetArg = startOffset;
         endOffsetArg = endOffset;
     }
 
	public void createError(IFile file, String message, int startLine, int startOffset, int endOffset) {
		 fileArg = file;
		 ISourcePosition position = new RdtPosition(startLine, startOffset, endOffset);
		 SyntaxException e = new SyntaxException(position, message);
	     syntaxExceptionArg = e;
	}

 	public void addProblem(IFile file, IProblem problem) {
 		fileArg = file;
 		messageArg = problem.getMessage();
 		lineArg = problem.getSourceLineNumber();
 		startOffsetArg = problem.getSourceStart();
 		endOffsetArg = problem.getSourceEnd();
 	}
 }
