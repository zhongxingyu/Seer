 package org.eclipse.imp.formatting;
 
 import java.io.IOException;
 
 import lpg.runtime.IMessageHandler;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.imp.box.builders.BoxFactory;
 import org.eclipse.imp.editor.UniversalEditor;
 import org.eclipse.imp.formatting.spec.ExtensionPointBinder;
 import org.eclipse.imp.formatting.spec.ParseException;
 import org.eclipse.imp.formatting.spec.Parser;
 import org.eclipse.imp.formatting.spec.Specification;
 import org.eclipse.imp.formatting.spec.Transformer;
 import org.eclipse.imp.language.ILanguageService;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.lpg.parser.ParseController;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.model.ModelFactory;
 import org.eclipse.imp.model.ModelFactory.ModelException;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.services.ISourceFormatter;
 import org.eclipse.imp.xform.pattern.matching.IASTAdapter;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IFileEditorInput;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 
 public class SourceFormatter implements ISourceFormatter, ILanguageService {
 	private IASTAdapter adapter;
 
 	private AbstractTextEditor fActiveEditor;
 
 	private Language fLanguage;
 
 	private Transformer transformer;
 	
 	private IMessageHandler handler;
 
 	public void formatterStarts(String initialIndentation) {
 		initialize();
 	}
 
 	private void initialize() {
 		UniversalEditor ue = (UniversalEditor) getActiveEditor();
 		fLanguage = LanguageRegistry.findLanguage(ue.getEditorInput());
 		ExtensionPointBinder b = new ExtensionPointBinder(fLanguage);
 
 		try {
 			adapter = b.getASTAdapter();
 			IPath fsp = b.getSpecificationPath();
 			Parser parser = new Parser(fsp, getActiveProject(), handler);
 			Specification spec = parser.parse(fsp);
 			transformer = new Transformer(spec, adapter);
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private AbstractTextEditor getActiveEditor() {
 		fActiveEditor = (AbstractTextEditor) PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 		return fActiveEditor;
 	}
 
 	private ISourceProject getActiveProject() throws ModelException {
 		return ModelFactory.open(extractResource(getActiveEditor())
 				.getProject());
 	}
 
 	private IResource extractResource(IEditorPart editor) {
 		IEditorInput input = editor.getEditorInput();
 		if (!(input instanceof IFileEditorInput))
 			return null;
 		return ((IFileEditorInput) input).getFile();
 	}
 
 	public String format(IParseController parseController, String content,
 			boolean isLineStart, String indentation, int[] positions) {
 		Object ast = parseController.getCurrentAst();
 
 		if (ast != null) {
 			String box = transformer.transformToBox(content, ast);
 			try {
 				return BoxFactory.box2text(box);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return content;
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return content;
 			}
 		}
 		else {
 			return content;
 		}
 	}
 
 	public void formatterStops() {
 	}
 }
