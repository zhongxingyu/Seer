 package mmrnmhrm.core.model;
 
 import melnorme.miscutil.ExceptionAdapter;
 import melnorme.miscutil.tree.IElement;
 import mmrnmhrm.core.model.lang.ELangElementTypes;
 import mmrnmhrm.core.model.lang.LangElement;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBuffer;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.text.IDocument;
 
 import descent.core.compiler.IProblem;
 import descent.internal.core.dom.ParserFacade;
 import dtool.descentadapter.DescentASTConverter;
 import dtool.dom.ast.ASTNode;
 import dtool.dom.definitions.Module;
 import dtool.refmodel.pluginadapters.IGenericCompilationUnit;
 
 /**
  * Module Wrapper 
  */
 public class CompilationUnit extends LangElement implements IGenericCompilationUnit, IDeeElement {
 
 	public IFile file;
 	private IDocument document;
 	
 	private descent.internal.core.dom.Module oldModule;
 	private Module module;
 	//private boolean astUpdated;
 
 	public IProblem[] problems;
 	public int parseStatus;
 	
 	
 	public CompilationUnit(IFile file) {
 		this(null, file);
 	}
 
 	public CompilationUnit(PackageFragment parent, IFile file) {
 		super(parent);
 		this.file = file;
 	}
 
	public IFile getFile() {
		return file;
	}

 	public IResource getUnderlyingResource() {
 		return file;
 	}
 
 
 	public String getElementName() {
 //		int extStart = file.getName().lastIndexOf('.'); 
 //		return file.getName().substring(0, extStart);
 		return file.getName();
 	}
 
 	public int getElementType() {
 		return ELangElementTypes.COMPILATION_UNIT;
 	}
 	
 	public IDocument getDocument() {
 		return document;
 	}
 	
 	public String getSource() {
 		return document.get();
 	}
 	
 	/** Returns the D project of this compilation unit, null if none. */
 	public DeeProject getProject() {
 		return DeeModelManager.getLangProject(file.getProject().getName());
 	}
 	
 	public IElement[] getChildren() {
 		return getModule().getChildren();
 	}
 	
 
 	public descent.internal.core.dom.Module getOldModule() {
 		return oldModule;
 	}
 	
 	public Module getNeoModule() {
 		return module;
 	}
 	
 	public ASTNode getModule() {
 		if(module == null || parseStatus == EModelStatus.PARSER_SYNTAX_ERRORS)
 			return oldModule;
 		return module;
 	}
 	
 	public boolean isOutOfModel() {
 		return parent == null;
 	}
 
 
 	public void updateElement() {
 		openBuffer();
 	}
 
 	
 	private void openBuffer()  {
 		if(document != null)
 			return;
 		
 		ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
 		IPath loc = file.getFullPath();
 		LocationKind fLocationKind = LocationKind.IFILE;
 
 		ITextFileBuffer textFileBuffer;
 		try {
 			manager.connect(loc, fLocationKind, null);
 			//manager.connect(loc, fLocationKind, new NullProgressMonitor());
 			textFileBuffer = manager.getTextFileBuffer(loc, fLocationKind);
 			document = textFileBuffer.getDocument();
 		} catch (CoreException ce) {
 			//fStatus= x.getStatus();
 			document = manager.createEmptyDocument(loc, fLocationKind);
 		}
 	}
 	
 	public void updateElementRecursive() throws CoreException {
 		updateElement();
 		reconcile();
 	}
 
 	public boolean hasErrors() {
 		return problems.length > 0;
 	}
 	
 	/**
 	 *  Updates this CompilationUnit's AST according to the underlying text. 
 	 */
 	public void reconcile() {
 		module = null;
 		
 		clearErrorMarkers();
 		preParseCompilationUnit();
 
 		if (hasErrors()) {
 			createErrorMarkers();
 			parseStatus = EModelStatus.PARSER_SYNTAX_ERRORS;
 			return;
 		}
 
 		try {
 			convertAST();
 			parseStatus = EModelStatus.OK;
 		} catch (UnsupportedOperationException uoe) {
 			parseStatus = EModelStatus.PARSER_AST_UNSUPPORTED_NODE;
 		} catch (RuntimeException re) {
 			parseStatus = EModelStatus.PARSER_INTERNAL_ERROR;
 			throw re;
 		}
 	}
 
 	protected void clearErrorMarkers() {
 		try {
 			file.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		} catch (CoreException e) {
 			throw ExceptionAdapter.unchecked(e);
 		}
 	}
 	
 	protected void createErrorMarkers() {
 		for (IProblem problem : getOldModule().getProblems()) {
 			try {
 				IMarker marker = file.createMarker(IMarker.PROBLEM);
 				marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
 				marker.setAttribute(IMarker.SEVERITY,
 						IMarker.SEVERITY_ERROR);
 			} catch (CoreException e) {
 				throw ExceptionAdapter.unchecked(e);
 			}
 		}
 	}
 
 	private void preParseCompilationUnit() {
 		this.module = null;
 		this.problems = null;
 		ParserFacade parser = new descent.internal.core.dom.ParserFacade();
 		this.oldModule = parser.parseCompilationUnit(getSource()).mod;
 		this.problems = getOldModule().getProblems();
 	}
 	
 	
 	private void convertAST() {
 		DescentASTConverter domadapter = new DescentASTConverter();
 		Module neoModule = domadapter.convertModule(oldModule);
 		neoModule.setCUnit(this);
 		module = neoModule;
 	}
 	
 
 
 	public String toStringParseStatus() {
 		if(parseStatus == EModelStatus.PARSER_INTERNAL_ERROR) {
 			return "Parser Internal Error";
 		} else if(parseStatus == EModelStatus.PARSER_SYNTAX_ERRORS) {
 			return "Document Syntax Error";
 		} else if(parseStatus == EModelStatus.PARSER_AST_UNSUPPORTED_NODE) {
 			return "Unsupported AST Node configuration.";
 		} else
 			return "Status OK";
 	}
 
 }
