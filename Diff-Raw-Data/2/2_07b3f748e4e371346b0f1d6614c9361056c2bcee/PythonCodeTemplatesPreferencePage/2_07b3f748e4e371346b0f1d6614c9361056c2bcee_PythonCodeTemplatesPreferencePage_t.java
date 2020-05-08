 package org.eclipse.dltk.python.internal.ui.templates;
 
 import org.eclipse.dltk.python.internal.ui.PythonUI;
 import org.eclipse.dltk.python.internal.ui.text.PythonTextTools;
 import org.eclipse.dltk.python.internal.ui.text.SimplePythonSourceViewerConfiguration;
 import org.eclipse.dltk.python.ui.text.IPythonPartitions;
 import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
 import org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.jface.text.IDocument;
 
 /**
  * Python code templates preference page
  */
 public class PythonCodeTemplatesPreferencePage extends
 		ScriptTemplatePreferencePage {
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#createSourceViewerConfiguration()
 	 */
 	protected ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
 		return new SimplePythonSourceViewerConfiguration(getTextTools()
 				.getColorManager(), getPreferenceStore(), null,
 				IPythonPartitions.PYTHON_PARTITIONING, false);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setDocumentParticioner(org.eclipse.jface.text.IDocument)
 	 */
	protected void setDocumentPartitioner(IDocument document) {
 		getTextTools().setupDocumentPartitioner(document,
 				IPythonPartitions.PYTHON_PARTITIONING);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#setPreferenceStore()
 	 */
 	protected void setPreferenceStore() {
 		setPreferenceStore(PythonUI.getDefault().getPreferenceStore());
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage#getTemplateAccess()
 	 */
 	protected ScriptTemplateAccess getTemplateAccess() {
 		return PythonTemplateAccess.getInstance();
 	}
 
 	private PythonTextTools getTextTools() {
 		return PythonUI.getDefault().getTextTools();
 	}
 }
