 package mmrnmhrm.core.model;
 
 import melnorme.miscutil.ExceptionAdapter;
 import melnorme.miscutil.tree.IElement;
 import mmrnmhrm.core.CorePreferenceInitializer;
 import mmrnmhrm.core.DeeCore;
 import mmrnmhrm.core.ILangModelConstants;
 import mmrnmhrm.core.LangCoreMessages;
 import mmrnmhrm.core.model.lang.ELangElementTypes;
 import mmrnmhrm.core.model.lang.LangModuleUnit;
 import mmrnmhrm.core.model.lang.LangPackageFragment;
 
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBuffer;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 
 import descent.core.compiler.IProblem;
 import descent.internal.core.dom.ParserFacade;
 import dtool.descentadapter.DescentASTConverter;
 import dtool.dom.ast.ASTNode;
 import dtool.dom.definitions.Module;
 import dtool.refmodel.pluginadapters.IGenericCompilationUnit;
 
 /**
  * Compilation Unit. Bridges the Lang resource elements with ASTNode elements.
  * The content/structure of this element is allways created when the object
  * is instantiated.
  */
 public class CompilationUnit extends LangModuleUnit implements IGenericCompilationUnit, IDeeElement {
 
 	
 	private descent.internal.core.dom.Module oldModule;
 	private Module module;
 	//private boolean astUpdated;
 
 	public IProblem[] problems;
 	public int parseStatus;
 	
 
 	public CompilationUnit(LangPackageFragment parent, IFile file) {
 		super(parent);
 		this.file = file;
 		createStructure();
 	}
 	
 	public CompilationUnit(IFile file) {
 		this(null, file);
 	}
 
 	public ASTNode[] getChildren() {
 		try {
 			getElementInfo();
 		} catch (CoreException e) {
 			ExceptionAdapter.unchecked(e); // Should not happen
 		}
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
 
 	public boolean hasErrors() {
 		return problems.length > 0;
 	}
 	
 	/** Updates this CompilationUnit's AST according to the underlying text. */
 	public void reconcile() {
 		module = null;
 		
 		clearErrorMarkers();
		preParseCompilationUnit();
 
 		if (hasErrors()) {
 			createErrorMarkers(getDocument());
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
 		} catch (CoreException ce) {
 			throw ExceptionAdapter.unchecked(ce);
 		}
 	}
 	
 	protected void createErrorMarkers(IDocument doc) {
 		boolean reportSyntaxErrors;
 		reportSyntaxErrors = DeeCore.getInstance().getPluginPreferences()
 				.getBoolean(CorePreferenceInitializer.REPORT_SYNTAX_ERRORS);
 
 		for (IProblem problem : getOldModule().getProblems()) {
 			try {
 				createMarker(doc, problem, reportSyntaxErrors);
 			} catch (CoreException e) {
 				throw ExceptionAdapter.unchecked(e);
 			}
 		}
 	}
 	
 	private void createMarker(IDocument doc, IProblem problem,
 			boolean reportSyntaxErrors) throws CoreException {
 		IMarker marker = file.createMarker(IMarker.PROBLEM);
 		
 		int lineNum = 0;
 		try {
 			lineNum = doc.getLineOfOffset(problem.getOffset());
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNum);
 		} catch (BadLocationException e) {
 			DeeCore.log(e);
 		}
 		
 		if(reportSyntaxErrors) {
 			marker.setAttribute(IMarker.LOCATION, "Line "+ lineNum);
 			marker.setAttribute(IMarker.CHAR_START, problem.getOffset());
 			marker.setAttribute(IMarker.CHAR_END, problem.getOffset() + problem.getLength());
 		}
 		marker.setAttribute(IMarker.MESSAGE, problem.getMessage());
 		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
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
