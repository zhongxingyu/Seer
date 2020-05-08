 /**
  * Error Marker Class to mark error in an AlgorithmEditor
  */
 package de.unisiegen.informatik.bs.alvis.editors;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.runtime.MismatchedTreeNodeException;
 import org.antlr.runtime.NoViableAltException;
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.ui.texteditor.MarkerUtilities;
 
 import de.uni_siegen.informatik.bs.alvic.*;
 import de.unisiegen.informatik.bs.alvis.compiler.CompilerAccess;
 
 /**
  * This class is for marking the given Document and File with Errors that occurs parsing the document.
  * @author Eduard Boos
  * 
  */
 public class AlgorithmErrorMarker {
 	private IFile file;
 	private IDocument document;
 	public static final String ERROR_MARKER_ID = "de.unisiegen.informatik.bs.alvis.markers.AlgorithmErrorMarker";
 
 	/**
 	 * 
 	 * @param file
 	 *            the IFile to mark
 	 * @param document
 	 *            the IDocument to mark
 	 */
 	public AlgorithmErrorMarker(IFile file, IDocument document) {
 		this.file = file;
 		this.document = document;
 	}
 
 	/**
 	 * mark the errors in the file/document, this method will parse the document
 	 * text for errors. First delete all markers and then repaint the errors
 	 * again.
 	 */
 	public void markErrors() {
 		/** delete old errorMarkers */
 		try {
 			file.deleteMarkers(ERROR_MARKER_ID, false, IResource.DEPTH_ZERO);
 		} catch (CoreException e) {
 			// not critical, will be deleted next time called
 		}
 		/** remark all errorMarkers */
 		// test mark the current Document, from start to position 50
 		// TODO implement method when Compiler front-end is created
 
 		Map<String, Object> map = new HashMap<String, Object>();
 		List<RecognitionException> errors = CompilerAccess.getDefault().getExceptions();
 		for(RecognitionException error : errors) {
 			String errorMessage = "";
 			System.err.println(error.toString());
 			if (error instanceof MismatchedTreeNodeException) {
 				MismatchedTreeNodeException re = (MismatchedTreeNodeException)error;
 				//TODO specific Exception Stuff here like, Error Message
 				System.out.println(error);
 				errorMessage = "MismatchedTreeNode at " + re.line + ":" + re.charPositionInLine;
 			} else if (error instanceof NoViableAltException) {
 				NoViableAltException re = (NoViableAltException)error;
 				//TODO specific Exception Stuff here like, Error Message
 		 		errorMessage = "No alternative given at " + re.line + ":" + re.charPositionInLine;
 			} else if (error instanceof ArgumentNumberException) {
 				ArgumentNumberException re = (ArgumentNumberException)error;
 				errorMessage = "Function called at " + re.line + ":" + re.charPositionInLine + " with the wrong number of arguments (" + " instead of the expected " + ")";
 			} else if (error instanceof ArgumentTypeException) {
 				ArgumentTypeException re = (ArgumentTypeException)error;
 				errorMessage = "Function called at " + re.line + ":" + re.charPositionInLine + " got an argument with the wrong type: argument #" + re.getArgumentNumber() + " is of type " + re.getGiven() + " but should be of type " + re.getExpected() + ".";
 			} else if (error instanceof InvalidAssignmentException) {
 				InvalidAssignmentException re = (InvalidAssignmentException)error;
 				errorMessage = "Tried to assign to " + re.getLeft() + " at " + re.line + ":" + re.charPositionInLine + " which is a function call and can thus not be assigned to.";
 			} else if (error instanceof InvalidReturnException) {
 				InvalidReturnException re = (InvalidReturnException)error;
 				if (re.getExpected().toString().equalsIgnoreCase("void"))
 					errorMessage = "Trying to return non-void result " + re.getStat() + " at " + re.line + ":" + re.charPositionInLine + " from void function " + re.getFunction() + ".";
 				else if (null == re.getGiven())
 					errorMessage = "Trying to return from function " + re.getFunction() + " at " + re.line + ":" + re.charPositionInLine + " without a return value where a return value of type " + re.getExpected() + " was expected.";
 				else
 					errorMessage = "Trying to return value " + re.getStat() + " from function " + re.getFunction() + " at " + re.line + ":" + re.charPositionInLine + " which has type " + re.getGiven() + " where an expression of type " + re.getExpected() + " was expected.";
 			} else if (error instanceof InvalidStatementException) {
 				InvalidStatementException re = (InvalidStatementException)error;
 				errorMessage = "Tried to use expression " + re.getExpr() + " where a statement was expected at " + re.line + ":" + re.charPositionInLine + ".";
 			} else if (error instanceof NoSuchOperatorException) {
 				NoSuchOperatorException re = (NoSuchOperatorException)error;
 				errorMessage = "Using non-existant operator " + re.getMember() +  "(" + re.getObject() + (re.getArguments() == 0 ? ")." : ", " + re.getArgType()+ ").");
 			} else if (error instanceof NotAFunctionException) {
 				NotAFunctionException re = (NotAFunctionException)error;
 				errorMessage = "Trying to call " + re.getExpr() + " at " + re.line + ":" + re.charPositionInLine + " which has type " + re.getType() + "and therefore is not a function.";
 			} else if (error instanceof NotAnArrayException) {
 				NotAnArrayException re = (NotAnArrayException)error;
 				errorMessage = "Trying to index " + re.getExpr() + " at " + re.line + ":" + re.charPositionInLine + " which has type " + re.getType() + " and therefore is not an array.";
 			} else if (error instanceof TypeMismatchException) {
 				TypeMismatchException re = (TypeMismatchException)error;
 				errorMessage = "Found expression " + re.getExpr() + " at " + re.line + ":" + re.charPositionInLine + " which has type " + re.getGiven() + " where an expression of type" + re.getExpected() + " was expected.";
 			} else if (error instanceof UnknownIdentifierException) {
 				UnknownIdentifierException re = (UnknownIdentifierException)error;
 				errorMessage = "Found identifier " + re.getIdentifier() + " at " + re.line + ":" + re.charPositionInLine + " which was not declared before being used.";
 			} else if (error instanceof UnknownOperatorException) {
 				UnknownOperatorException re = (UnknownOperatorException)error;
 				errorMessage = "Found operator " + re.getMember() + " at " + re.line + ":" + re.charPositionInLine + " with argument of type " + re.getObject() + " which is not defined.";
 			} else if (error instanceof UnknownMemberException) {
 				UnknownMemberException re = (UnknownMemberException)error;
 				errorMessage = "Found access to member " + re.getMember() + " of class " + re.getObject() + " at " + re.line + ":" + re.charPositionInLine + ". Such a member does not exist.";
 			} else if (error instanceof TypeException) {
 				errorMessage = error.toString();
 			} else {
 				System.out.println("UNKNOWN ERROR at " + error.line + ":" + error.charPositionInLine + ": " + error);
 				continue;
 			}
			TypeException re = (TypeException) error;
 			MarkerUtilities.setLineNumber(map, error.line);
 			MarkerUtilities.setMessage(map, errorMessage);
//			map.put(IMarker.MESSAGE, errorMessage);
 			int lineOffset;
 			try {
 				lineOffset = document.getLineOffset(error.line - 1);
 			} catch (BadLocationException e1) {
 				continue;
 			}
 
			MarkerUtilities.setLineNumber(map, ((RecognitionException)error).line-1);
			MarkerUtilities.setCharStart(map, lineOffset + re.charPositionInLine);
			MarkerUtilities.setCharEnd(map, lineOffset + re.endCharPositionInLine + 1);
 			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
 			map.put(IMarker.LOCATION, file.getFullPath().toString());
 			try {
 				MarkerUtilities.createMarker(file, map, ERROR_MARKER_ID);
 			} catch (CoreException e) {
				//ignore Error
 			}
			
 		}
		
 	}
 
 }
