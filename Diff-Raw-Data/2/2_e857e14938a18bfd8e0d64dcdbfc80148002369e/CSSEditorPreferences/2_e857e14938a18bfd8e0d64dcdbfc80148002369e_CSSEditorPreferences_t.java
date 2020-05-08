 package org.eclipse.e4.ui.preferences;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine;
 import org.eclipse.e4.ui.css.swt.theme.ITheme;
 import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
 import org.eclipse.e4.ui.model.application.ui.basic.MPart;
 import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
 import org.eclipse.e4.ui.workbench.modeling.EModelService;
 import org.eclipse.e4.ui.workbench.modeling.EPartService;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.EditorReference;
 import org.eclipse.ui.internal.WorkbenchPage;
 import org.eclipse.ui.internal.e4.compatibility.CompatibilityEditor;
 import org.eclipse.ui.internal.tweaklets.PreferencePageEnhancer;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 
 public class CSSEditorPreferences extends PreferencePageEnhancer {
 
 	ITheme selection;
 	XtextEditor cssEditor;
 	IThemeEngine engine;
 	boolean resetCurrentTheme;
 	
 	@SuppressWarnings("restriction")
 	@Override
 	public void createContents(Composite parent) {
 		resetCurrentTheme = false;
 		IWorkbenchWindow wbw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		MWindow hostWin = (MWindow) wbw.getService(MWindow.class);
 		EPartService partService = hostWin.getContext().get(EPartService.class);
 		EModelService modelService = hostWin.getContext().get(EModelService.class);
 		MPart editor = partService.createPart(CompatibilityEditor.MODEL_ELEMENT_ID);
 		engine = hostWin.getContext().get(IThemeEngine.class);
 	
 		IFile file = updateInput();
 		
 		IEditorInput input = new FileEditorInput(file);
 
 		IWorkbenchPage wbPage = wbw.getActivePage();
 		EditorReference reference = ((WorkbenchPage) wbPage).createEditorReferenceForPart(editor,
 				input,
 				"org.eclipse.e4.CSS", null); //$NON-NLS-1$
 		IEclipseContext localContext = hostWin.getContext().createChild();
 		localContext.set(IEditorInput.class, input);
 		localContext.set(EditorReference.class, reference);
 
 		// Render it
 		Composite composite3 = new Composite(parent, SWT.BORDER);
 		composite3.setLayout(new FillLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 2);
 		data.widthHint = 500;
 		data.heightHint = 500;
 		composite3.setLayoutData(data);
 		
 		modelService.hostElement(editor, hostWin, composite3, localContext);
 		partService.activate(editor);
 		IEditorPart tmpEditor = editor.getContext().get(IEditorPart.class);
 		if (tmpEditor instanceof XtextEditor) {
 			cssEditor = (XtextEditor) tmpEditor;
 		}
 	}
 
 	@Override
 	public void setSelection(Object sel) {
 		if (sel instanceof ITheme) {
 			ITheme newTheme = (ITheme) sel;
 			ITheme oldSelection  = selection;
 			selection = newTheme;
 			if (oldSelection != null && !newTheme.getId().equals(oldSelection.getId())) {
 					IFile file = updateInput();
 					IEditorInput input = new FileEditorInput(file);
 					cssEditor.setInput(input);
 			}
 		}
 		
 	}
 
 	@Override
 	public void performOK() {
 		if (cssEditor.isDirty()) {
 			// make a copy of file
 			IDocumentProvider docProvider = cssEditor.getDocumentProvider();
 			
 			IEditorInput editorInput = cssEditor.getEditorInput();
 			IDocument doc = docProvider.getDocument(editorInput);
 			String more = doc.get();
 			//check for .e4css folder
 			String e4CSSPath = System.getProperty("user.home") + System.getProperty("file.separator") + ".e4css";
 			File e4CSS = new File(e4CSSPath);
 			if (!e4CSS.exists()) {
 				File userHome = e4CSS.getParentFile( );
 				if ( userHome.exists() && userHome.canWrite() )
 				{
 					e4CSS.mkdir();
 				}
 			}
 			IPath path = new Path(e4CSSPath + System.getProperty("file.separator") + editorInput.getName()); //$NON-NLS-1$
 			
 			byte[] bytes = more.getBytes();
 			FileOutputStream outputStream = null;
 			try {
 				outputStream = new FileOutputStream(path.toOSString());
 				outputStream.write(bytes, 0, bytes.length);
 			} catch (FileNotFoundException e) {
 			} catch (IOException e) {
 			} finally {
 				if (outputStream != null)
 					try {
 						outputStream.close();
 					} catch (IOException e) {
 					} 
 			}
 
 			if (engine instanceof ThemeEngine) {
 				ArrayList<String> styleSheets = new ArrayList<String>();
 				try {
 					URL styleSheetURL = FileLocator.toFileURL(path.toFile().toURI().toURL());
 					styleSheets.add(styleSheetURL.toString());
 					((ThemeEngine) engine).themeModified(selection, styleSheets );
 				} catch (MalformedURLException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			cssEditor.doRevertToSaved();
 		}
 		
 		if (resetCurrentTheme) {
 			((ThemeEngine) engine).resetCurrentTheme();
 			resetCurrentTheme = false;
 		}
 	}
 
 	
 	IFile updateInput() {
 
 		IProject newProject = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(".e4css"); //$NON-NLS-1$
 		URL styleSheetURL = null;
 
 		if (engine instanceof ThemeEngine) {
 			List<String> ss = ((ThemeEngine) engine).getStylesheets(selection);
 			List<String> mod = ((ThemeEngine) engine).getModifiedStylesheets(selection);
 			if (mod.size() > 0) {
 				ss = mod;
 			}
 			if (ss.size() > 0) {
 				// For now just get the first element
 				String path = ss.get(0);
 				try {
 					styleSheetURL = FileLocator.toFileURL(new URL(path));
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		IProjectDescription desc = newProject.getWorkspace()
 				.newProjectDescription(newProject.getName());
 		IFile file = null;
 		try {
 			if (!newProject.exists())
 				newProject.create(desc, null);
 			if (!newProject.isOpen()) {
 				newProject.open(null);
 			}
 			newProject.setHidden(true);
 			// currentTheme.
 			IPath location = new Path(styleSheetURL.getPath());
 			// IPath location = new Path(styleSheetURL.getPath());
 			file = newProject.getFile(location.lastSegment());
 			file.delete(true, null);
 			if (!file.exists())
 				file.createLink(location, IResource.NONE, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return file;
 
 	}
 
 	@Override
 	public void performCancel() {
 		if (cssEditor.isDirty()) cssEditor.doRevertToSaved();
 	}
 
 	@Override
 	public void performDefaults() {
 		List<String> mod = ((ThemeEngine) engine).getModifiedStylesheets(selection);
 		if (mod.size() > 0) {
 		
 			// For now just get the first element
 			String path = mod.get(0);
 			try {
 				
 				URL styleSheetURL = FileLocator.toFileURL(new URL(path));
 				File file = new File(styleSheetURL.getFile());
 				if (file.exists()) file.delete();
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		if (engine instanceof ThemeEngine) ((ThemeEngine) engine).resetModifiedStylesheets(selection);
 		IFile file = updateInput();
 		IEditorInput input = new FileEditorInput(file);
 		cssEditor.setInput(input);
 		resetCurrentTheme = true;
 	}
 
 }
