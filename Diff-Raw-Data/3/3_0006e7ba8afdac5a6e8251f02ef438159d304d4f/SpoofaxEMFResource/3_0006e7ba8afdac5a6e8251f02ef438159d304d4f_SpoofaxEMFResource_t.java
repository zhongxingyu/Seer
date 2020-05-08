 package org.spoofax.modelware.emf.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.CommonPlugin;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.spoofax.interpreter.terms.IStrategoAppl;
 import org.spoofax.interpreter.terms.IStrategoTerm;
 import org.spoofax.interpreter.terms.ITermFactory;
 import org.spoofax.modelware.emf.Language;
 import org.spoofax.modelware.emf.LanguageRegistry;
 import org.spoofax.modelware.emf.trans.Constants;
 import org.spoofax.modelware.emf.trans.Tree2modelConverter;
 import org.spoofax.modelware.emf.tree2model.Model2Term;
 import org.spoofax.modelware.emf.utils.Utils;
 import org.spoofax.terms.TermFactory;
 import org.strategoxt.imp.runtime.Environment;
 import org.strategoxt.imp.runtime.FileState;
 import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
 
 /**
  * An EMF resource implementation for Spoofax, which provides generic functionality for serializing and deserializing EObjects by means of a
  * user-defined syntax. One can use this resource implementation by extending `org.eclipse.emf.ecore.extension_parser` by means of an Eclipse
  * extension.
  * 
  * @author oskarvanrest
  */
 public class SpoofaxEMFResource extends ResourceImpl {
 
 	protected IPath path;
 	protected ITermFactory termFactory;
 
 	public SpoofaxEMFResource(URI uri) {
 		super(uri);
 
 		URI resolvedFile = CommonPlugin.resolve(uri);
 		this.path = new Path(resolvedFile.toFileString());
 		this.termFactory = new TermFactory();
 	}
 
 	/**
 	 * @override
 	 */
 	protected void doLoad(InputStream inputStream, Map<?, ?> options) {
 		FileState editorOrFileState = Utils.getEditorOrFileState(path);
 		IStrategoTerm ASTgraph = Utils.getASTgraph(editorOrFileState);
 		
 		String textFileExtension = null;
 		try {
 			textFileExtension = editorOrFileState.getDescriptor().getLanguage().getFilenameExtensions().iterator().next();
 		}
 		catch (BadDescriptorException e) {
 			e.printStackTrace();
 		}
 		Language language = LanguageRegistry.getInstance().get(textFileExtension);
		if (language == null) {
			Environment.logException("No language found for file extension '" + textFileExtension + "'. Most likely, you forgot to add an 'org.spoofax.modelware.gmf.synchronizer' extension. Note that this extension can be added to a random plugin.");
		}
 
 		EPackage pack = EPackageRegistryImpl.INSTANCE.getEPackage(language.getNsURI());
 		if (pack == null) {
 			Environment.logException("Cannot find EPackage " + textFileExtension + ".");
 		}
 
 		EObject eObject = null;
 
 		if (ASTgraph instanceof IStrategoAppl) {
 			eObject = new Tree2modelConverter(pack).convert(ASTgraph);
 		}
 		else {
 			EAnnotation rootElementAnnotation = pack.getEAnnotation(Constants.ANNO_SPOOFAX_CONFIG);
 			if (rootElementAnnotation != null) {
 				String rootClass_String = rootElementAnnotation.getDetails().get(Constants.ANNO_SPOOFAX_CONFIG_ROOT);
 				if (rootClass_String != null) {
 					EClass rootClass_EClass = (EClass) pack.getEClassifier(rootClass_String);
 					if (rootClass_EClass != null) {
 						eObject = pack.getEFactoryInstance().create(rootClass_EClass);
 					}
 				}
 			}
 			if (eObject == null) {
 				Environment.logException("Unknown root class.");
 				return;
 			}
 		}
 
 		getContents().add(0, eObject);
 	}
 
 	protected void doSave(OutputStream outputStream, Map<?, ?> options) {
 		FileState editorOrFileState = Utils.getEditorOrFileState(path);
 
 		if (editorOrFileState.getCurrentAst() == null) {
 			Environment.logException("Can't parse file, see Spoofax.modelware/7");
 			// TODO: pretty-print newTree
 		}
 
 		EObject object = getContents().get(0);
 		Model2Term model2term = new Model2Term(new TermFactory());
 		IStrategoTerm newTree = model2term.convert(object);
 		newTree = Utils.getASTtext(newTree, editorOrFileState);
 		
 		try {
 			String result = Utils.calculateTextReplacement(editorOrFileState.getCurrentAnalyzedAst(), newTree, editorOrFileState);
 			outputStream.write(result.getBytes());
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		} catch (BadDescriptorException e) {
 			e.printStackTrace();
 		}
 	}
 }
