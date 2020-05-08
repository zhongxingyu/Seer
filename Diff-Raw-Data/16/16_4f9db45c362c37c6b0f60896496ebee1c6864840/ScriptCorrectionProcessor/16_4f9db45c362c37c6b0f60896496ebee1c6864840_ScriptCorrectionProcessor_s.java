 package org.eclipse.dltk.ui.text;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.internal.ui.editor.EditorUtility;
 import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
 import org.eclipse.dltk.ui.editor.IScriptAnnotation;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
 import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
 import org.eclipse.jface.text.source.Annotation;
 import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;
 
 public class ScriptCorrectionProcessor implements IQuickAssistProcessor {
 	private final ScriptCorrectionAssistant fAssistant;
 
 	public ScriptCorrectionProcessor(
 			ScriptCorrectionAssistant tclCorrectionAssistant) {
 		this.fAssistant = tclCorrectionAssistant;
 	}
 
 	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
 		return true;
 	}
 
 	public boolean canFix(Annotation annotation) {
 		return ScriptCorrectionProcessorManager.canFix(getNatureId(),
 				annotation);
 	}
 
 	private String getNatureId() {
 		final ScriptEditor editor = (ScriptEditor) this.fAssistant.getEditor();
 		return editor.getLanguageToolkit().getNatureId();
 	}
 
 	public ICompletionProposal[] computeQuickAssistProposals(
 			IQuickAssistInvocationContext invocationContext) {
 		final ScriptEditor editor = (ScriptEditor) this.fAssistant.getEditor();
 		final IScriptCorrectionProcessor[] processors = ScriptCorrectionProcessorManager
 				.getProcessors(editor.getLanguageToolkit().getNatureId());
 		if (processors == null) {
 			return null;
 		}
 		final ISourceModule sourceModule = EditorUtility
 				.getEditorInputModelElement(editor, false);
 		final IScriptCorrectionContext context = new ScriptCorrectionContext(
 				invocationContext, editor, sourceModule);
 		final Annotation[] annotations = fAssistant.getAnnotationsAtOffset();
 		for (int i = 0; i < annotations.length; i++) {
 			final Annotation annotation = annotations[i];
 			if (annotation instanceof IScriptAnnotation) {
 				for (int j = 0; j < processors.length; ++j) {
 					processors[j].computeQuickAssistProposals(
 							(IScriptAnnotation) annotation, context);
 				}
 			} else if (annotation instanceof SimpleMarkerAnnotation) {
 				final IMarker marker = ((SimpleMarkerAnnotation) annotation)
 						.getMarker();
 				for (int j = 0; j < processors.length; ++j) {
 					processors[j].computeQuickAssistProposals(marker, context);
 				}
 			}
 
 		}
 		return context.getProposals();
 	}
 
 	public String getErrorMessage() {
 		return null;
 	}
 
 }
