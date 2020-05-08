 package ast.tools.core;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.FileUtils;
 import org.eclipse.core.filebuffers.FileBuffers;
 import org.eclipse.core.filebuffers.ITextFileBuffer;
 import org.eclipse.core.filebuffers.ITextFileBufferManager;
 import org.eclipse.core.filebuffers.LocationKind;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.text.edits.MalformedTreeException;
 import org.eclipse.text.edits.TextEdit;
 
 import ast.tools.context.ASTContext;
 import ast.tools.context.ASTWriter;
 import ast.tools.helper.CompilationUnitHelper;
 import ast.tools.internal.context.impl.ASTContextImpl;
 import ast.tools.internal.model.impl.TClassImpl;
 import ast.tools.internal.visitor.ASTVisitor;
 import ast.tools.model.TClass;
 import ast.tools.observable.ASTObservable;
 
 public class ASTProcessor extends ASTObservable {
 	private ICompilationUnit iunit;
 	private CompilationUnit unit;
 	private TClass tClass;
 
 	public ASTProcessor(ICompilationUnit iunit) {
 		super();
 		this.iunit = iunit;
 		this.init();
 	}
 
 	public ASTProcessor(File file) {
 		super();
 		try {
 			this.init(FileUtils.readFileToString(file).toCharArray());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public ASTProcessor(String source) {
 		super();
 		this.init(source.toCharArray());
 	}
 
 	private void init() {
 		ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(iunit);
 		parser.setResolveBindings(true);
 		this.unit = (CompilationUnit) parser.createAST(null);
 	}
 
 	private void init(char[] source) {
 		ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(source);
 		parser.setResolveBindings(true);
 		this.unit = (CompilationUnit) parser.createAST(null);
 	}
 
 	public ASTContext visit() {
 		ASTContext context = new ASTContextImpl();
 		if (unit != null) {
 			ASTVisitor visitor = new ASTVisitor(this);
 			unit.accept(visitor);
 			context = visitor.getContext();
 			tClass = visitor.getTClass();
 		}
 		return context;
 	}
 
 	public TClass getTClass() {
 		return this.tClass != null ? this.tClass : new TClassImpl();
 	}
 
 	public void write(ASTWriter writer) {
		this.unit.recordModifications();
 		writer.write(new CompilationUnitHelper(this.unit), this.unit.getAST());
 	}
 
 	public void commit() {
 		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
 		IPath path = this.unit.getJavaElement().getPath();
 		try {
 			bufferManager.connect(path, LocationKind.IFILE, null);
 			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
 			IDocument document = textFileBuffer.getDocument();
 			TextEdit edit = this.unit.rewrite(document, null);
 			edit.apply(document);
 			textFileBuffer.commit(null, false);
 			bufferManager.disconnect(path, LocationKind.IFILE, null);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		} catch (MalformedTreeException e) {
 			e.printStackTrace();
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		}
 	}
 }
