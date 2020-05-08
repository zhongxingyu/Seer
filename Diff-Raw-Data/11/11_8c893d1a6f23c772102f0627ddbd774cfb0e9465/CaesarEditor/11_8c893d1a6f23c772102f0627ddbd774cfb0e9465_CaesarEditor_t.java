 package org.caesarj.ui.editor;
 
 import org.apache.log4j.Logger;
import org.caesarj.ui.CaesarPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
 import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
 
 /**
  * CaesarEditor
  * 
  * TODO [feature] solve the problem with annotations, at the moment user has to
  * disable "Analyse Annotations while typing" TODO [feature] completer for
  * caesar keywords TODO [feature] add new file type (e.g.: .cj) in order to
  * avoid conflicts with coexisting pure java projects TODO [feature] add new
  * Wizardtype for Caesar Files
  * 
  * @author Ivica Aracic <ivica.aracic@bytelords.de>
  */
 
 public class CaesarEditor extends CompilationUnitEditor {
 
 	/**
 	 * @author Jochen
 	 * 
 	 * To change the template for this generated type comment go to
 	 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 	 */
 
 	private static Logger log = Logger.getLogger(CaesarEditor.class);
 
 	private CaesarOutlineView outlineView;
 
 	//private CompositeRuler caesarVerticalRuler;
 
 	public CaesarEditor() {
 		super();
		/* initialize auto annotation on/off switching */
		CaesarPlugin.getDefault().initPluginUI();
 	}
 	
 	public IJavaElement getInputJavaElement() {
 		return super.getInputJavaElement();
 	}
 
 	protected void initializeEditor() {
 		super.initializeEditor();
 
 		log.debug("Initializing CaesarJ Editor."); //$NON-NLS-1$
 		try {
 			
 			IPreferenceStore store = this.getPreferenceStore();
 			CaesarTextTools textTools = new CaesarTextTools(store);
 			JavaSourceViewerConfiguration svConfig = new CaesarSourceViewerConfiguration(
 					textTools, this, store);
 			setSourceViewerConfiguration(svConfig);
 			log.debug("CaesarJ Editor Initialized."); //$NON-NLS-1$
 		} catch (Exception e) {
 			log.error("Initalizing CaesarJ Editor.", e); //$NON-NLS-1$
 		}
 	}
 
 	/* overriden to create correct source viewer configuration */
 	protected void setPreferenceStore(IPreferenceStore store) {
 		super.setPreferenceStore(store);
 
 		CaesarTextTools textTools = new CaesarTextTools(store);
 		JavaSourceViewerConfiguration svConfig = new CaesarSourceViewerConfiguration(
 				textTools, this, store);
 		setSourceViewerConfiguration(svConfig);
 	}
 	
 	public Object getAdapter(Class key) {
 		if (key.equals(IContentOutlinePage.class)) {
 			if (this.outlineView == null) {
 				this.outlineView = new CaesarOutlineView(this);
 				this.outlineView.setEnabled(true);
 			}
 			return this.outlineView;
 		}
 		return super.getAdapter(key);
 	}
 
 	public void doSetInput(IEditorInput input) throws CoreException {
 		super.doSetInput(input);
 	}
 
 	public void dispose() {
		/* Fix TR35: must call super to free reference to the file */
		super.dispose(); 
		
 		log.debug("dispose"); //$NON-NLS-1$
 		this.outlineView.setEnabled(false);
 	}
 }
