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
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.ui.texteditor.MarkerUtilities;
 
 import de.uni_siegen.informatik.bs.alvic.*;
 import de.unisiegen.informatik.bs.alvis.compiler.CompilerAccess;
 import de.unisiegen.informatik.bs.alvis.io.logger.Logger;
 
 /**
 * This class is for marking the given Document and File with Errors that occurs
 * parsing the document.
 * 
  * @author Eduard Boos
  * 
  */
 public class AlgorithmErrorMarker {
 	private IFile file;
 	private IDocument document;
 	public static final String ERROR_MARKER_ID = "de.unisiegen.informatik.bs.alvis.markers.AlgorithmErrorMarker"; //$NON-NLS-1$
 
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
			Logger.getInstance()
					.log("Editor->AlgorithmErrorMarker", Logger.ERROR, "Error marking caused an CoreException: \n" + e.getLocalizedMessage()); //$NON-NLS-1$
 		}
 		/* remark all errorMarkers */
 		Map<String, Object> map = new HashMap<String, Object>();
 		List<RecognitionException> errors = CompilerAccess.getDefault()
 				.getExceptions();
 		for (RecognitionException error : errors) {
 			String errorMessage;
			// System.err.println(error.toString());
 			if (error instanceof MismatchedTreeNodeException) {
 				// This case should not happen. If control flow gets here
 				// something is wrong with the compiler.
 				MismatchedTreeNodeException re = (MismatchedTreeNodeException) error;
 				error.printStackTrace();
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_MismatcheTreeNode,
 						new Object[] { re.line, re.charPositionInLine, error });
 			} else if (error instanceof NoViableAltException) {
 				NoViableAltException re = (NoViableAltException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_NoViableAlt,
 						(Object) re.line, (Object) re.charPositionInLine);
 			} else if (error instanceof ArgumentNumberException) {
 				ArgumentNumberException re = (ArgumentNumberException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_ArgumentNumberEx,
 						new Object[] { re.getMethod(), re.line,
 								re.charPositionInLine, re.getGiven(),
 								re.getExpected() });
 			} else if (error instanceof ArgumentTypeException) {
 				ArgumentTypeException re = (ArgumentTypeException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_ArgumentTypeEx,
 						new Object[] { re.getMethod(), re.line,
 								re.charPositionInLine, re.getArgumentNumber(),
 								re.getGiven(), re.getExpected() });
 			} else if (error instanceof InvalidAssignmentException) {
 				InvalidAssignmentException re = (InvalidAssignmentException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_InvalidAssignment,
 						new Object[] { re.getLeft(), re.line,
 								re.charPositionInLine });
 			} else if (error instanceof InvalidReturnException) {
 				InvalidReturnException re = (InvalidReturnException) error;
 				if (re.getExpected().toString().equalsIgnoreCase("void")) //$NON-NLS-1$
 					errorMessage = NLS.bind(
 							Messages.AlgorithmErrorMarker_InvalidNonVoidReturn,
 							new Object[] { re.getStat(), re.line,
 									re.charPositionInLine, re.getFunction() });
 				else if (null == re.getGiven())
 					errorMessage = NLS.bind(
 							Messages.AlgorithmErrorMarker_InvalidVoidReturn,
 							new Object[] { re.getFunction(), re.line,
 									re.charPositionInLine, re.getExpected() });
 				else
 					errorMessage = NLS.bind(
 							Messages.AlgorithmErrorMarker_InvalidReturnType,
 							new Object[] { re.getStat(), re.getFunction(),
 									re.line, re.charPositionInLine,
 									re.getGiven(), re.getExpected() });
 			} else if (error instanceof InvalidStatementException) {
 				InvalidStatementException re = (InvalidStatementException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_InvalidStatement,
 						new Object[] { re.getExpr(), re.line,
 								re.charPositionInLine });
 			} else if (error instanceof NoSuchOperatorException) {
 				NoSuchOperatorException re = (NoSuchOperatorException) error;
 				if (re.getArguments() == 0)
 					errorMessage = NLS.bind(
 							Messages.AlgorithmErrorMarker_NoUnarySuchOperator,
 							new Object[] { re.getMember(), re.getObject(),
 									re.line, re.charPositionInLine });
 				else
 					errorMessage = NLS.bind(
 							Messages.AlgorithmErrorMarker_NoSuchBinaryOperator,
 							new Object[] { re.getMember(), re.getObject(),
 									re.getArgType(), re.line,
 									re.charPositionInLine });
 			} else if (error instanceof NotAFunctionException) {
 				NotAFunctionException re = (NotAFunctionException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_NotAFunction,
 						new Object[] { re.getExpr(), re.line,
 								re.charPositionInLine, re.getType() });
 			} else if (error instanceof NotAnArrayException) {
 				NotAnArrayException re = (NotAnArrayException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_NotAnArray, new Object[] {
 								re.getExpr(), re.line, re.charPositionInLine,
 								re.getType() });
 			} else if (error instanceof TypeMismatchException) {
 				TypeMismatchException re = (TypeMismatchException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_TypeMismatch,
 						new Object[] { re.getExpr(), re.line,
 								re.charPositionInLine, re.getGiven(),
 								re.getExpected() });
 			} else if (error instanceof UnknownIdentifierException) {
 				UnknownIdentifierException re = (UnknownIdentifierException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_UnknownIdentifier,
 						new Object[] { re.getIdentifier(), re.line,
 								re.charPositionInLine });
 			} else if (error instanceof UnknownOperatorException) {
 				UnknownOperatorException re = (UnknownOperatorException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_UnknownOperator,
 						new Object[] { re.getMember(), re.line,
 								re.charPositionInLine, re.getObject() });
 			} else if (error instanceof UnknownMemberException) {
 				UnknownMemberException re = (UnknownMemberException) error;
 				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_UnknownMember,
 						new Object[] { re.getMember(), re.getObject(), re.line,
 								re.charPositionInLine });
 			} else if (error instanceof TypeException) {
 				errorMessage = error.toString();
 			} else {
				errorMessage = NLS.bind(
 						Messages.AlgorithmErrorMarker_UnknownError,
 						new Object[] { error.line, error.charPositionInLine,
								error });
 			}
 
 			TypeException re = null;
 			if (error instanceof TypeException) {
 				re = (TypeException) error;
 			}
 
 			MarkerUtilities.setLineNumber(map, error.line);
 			MarkerUtilities.setMessage(map, errorMessage);
 			// map.put(IMarker.MESSAGE, errorMessage);
 			int lineOffset;
 			try {
 				lineOffset = document.getLineOffset(error.line - 1);
 			} catch (BadLocationException e1) {
 				continue;
 			}
 
 			MarkerUtilities.setLineNumber(map, error.line - 1);
 			MarkerUtilities.setCharStart(map, lineOffset
 					+ error.charPositionInLine);
 			MarkerUtilities.setCharEnd(map, lineOffset
 					+ (null != re ? re.endCharPositionInLine : 0) + 1);
 			map.put(IMarker.SEVERITY, new Integer(IMarker.SEVERITY_ERROR));
 			map.put(IMarker.LOCATION, file.getFullPath().toString());
 			try {
 				MarkerUtilities.createMarker(file, map, ERROR_MARKER_ID);
 			} catch (CoreException e) {
				Logger.getInstance()
						.log("Editor->AlgorithmErrorMarker", Logger.ERROR, "Error marking caused an CoreException: \n" + e.getLocalizedMessage()); //$NON-NLS-1$
 			}
 		}
 	}
 
 }
