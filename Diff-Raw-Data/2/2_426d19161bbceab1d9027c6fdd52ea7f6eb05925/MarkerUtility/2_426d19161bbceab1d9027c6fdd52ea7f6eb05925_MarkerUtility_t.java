 /*
  * Created on Feb 20, 2005
  *
  */
 package org.rubypeople.rdt.internal.core.parser;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.IRubyModelMarker;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.parser.IProblem;
 
 /**
  * @author Chris
  * 
  */
 public class MarkerUtility {
 
 	/**
 	 * @param underlyingResource
 	 * @param syntaxException
 	 * @param contentLength
 	 */
 	public static void createSyntaxError(IResource underlyingResource, SyntaxException syntaxException) {
 		try {
 			ISourcePosition pos = syntaxException.getPosition();
 			IMarker marker = underlyingResource.createMarker(IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER);
 			Map map = new HashMap();
 			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
 			map.put(IMarker.MESSAGE, syntaxException.getMessage());
 			map.put(IMarker.USER_EDITABLE, Boolean.FALSE);
 			map.put(IMarker.LINE_NUMBER, new Integer(pos.getLine()));
 			map.put(IMarker.CHAR_START, new Integer(pos.getStartOffset()));
 			map.put(IMarker.CHAR_END, new Integer(pos.getEndOffset()));
 			marker.setAttributes(map);
 		} catch (CoreException e) {
 			RubyCore.log(e);
 		}
 	}
 
 	/**
 	 * @param underlyingResource
 	 */
 	public static void removeMarkers(IResource underlyingResource) {
 		try {
 			underlyingResource.deleteMarkers(IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
 		} catch (CoreException e) {
 			RubyCore.log(e);
 		}
 	}
 
 	/**
 	 * @param resource
 	 * @param problems
 	 */
 	public static void createProblemMarkers(IResource resource, List problems) {
 		for (Iterator iter = problems.iterator(); iter.hasNext();) {
 			createProblemMarker(resource, (DefaultProblem) iter.next());
 		}
 	}
 
 	/**
 	 * @param underlyingResource
 	 * @param problem
 	 */
 	private static void createProblemMarker(IResource underlyingResource, IProblem problem) {
 		try {
 			IMarker marker = underlyingResource.createMarker(IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER);
 			Map map = new HashMap();
 			int severity;
 			if(problem.isWarning()) severity = IMarker.SEVERITY_WARNING;
 			else if(problem.isError()) severity = IMarker.SEVERITY_ERROR;
 			else severity = IMarker.SEVERITY_INFO;
 			map.put(IMarker.SEVERITY, new Integer(severity));
 			map.put(IMarker.MESSAGE, problem.getMessage());
 			map.put(IMarker.USER_EDITABLE, Boolean.FALSE);
 			map.put(IMarker.LINE_NUMBER, new Integer(problem.getSourceLineNumber()));
 			map.put(IMarker.CHAR_START, new Integer(problem.getSourceStart()));
 			map.put(IMarker.CHAR_END, new Integer(problem.getSourceEnd()));
 			marker.setAttributes(map);
 		} catch (CoreException e) {
 			RubyCore.log(e);
 		}
 	}
 
 	public static void createTasks(IResource underlyingResource, List tasks) throws CoreException {
 		for (Iterator iter = tasks.iterator(); iter.hasNext();) {
 			createTask(underlyingResource, (TaskTag) iter.next());
 		}		
 	}
 	
 	/**
 	 * @param underlyingResource
 	 * @param warning
 	 * @throws CoreException 
 	 */
 	private static void createTask(IResource resource, TaskTag task) throws CoreException {
 		int lineNumber = task.getSourceLineNumber();
 		if (lineNumber <= 0) lineNumber = 1;
		IMarker marker = markerExists(resource, task.getMessage(), lineNumber, IRubyModelMarker.TASK_MARKER);
 		if (marker == null) {
 			HashMap map = new HashMap();
 			map.put(IMarker.PRIORITY, new Integer(task.getPriority()));
 			map.put(IMarker.MESSAGE, task.getMessage());
 			map.put(IMarker.LINE_NUMBER, new Integer(lineNumber));
 			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_INFO));
 			map.put(IMarker.USER_EDITABLE, new Boolean(false));
 			map.put(IMarker.TRANSIENT, new Boolean(false));
 			map.put(IMarker.CHAR_START, new Integer(task.getSourceStart()));
 			map.put(IMarker.CHAR_END, new Integer(task.getSourceEnd()));
 			marker = resource.createMarker(IMarker.TASK);
 			marker.setAttributes(map);
 		}
 	}
 	
 	public static IMarker markerExists(IResource resource, String message, int lineNumber, String type) throws CoreException {
 		IMarker tasks[] = resource.findMarkers(type, true, 0);
 		for (int i = 0; i < tasks.length; i++) {
 			if (tasks[i].getAttribute(IMarker.LINE_NUMBER).toString().equals(String.valueOf(lineNumber)) && tasks[i].getAttribute(IMarker.MESSAGE).equals(message)) return tasks[i];
 		}
 		return null;
 	}
 
 }
