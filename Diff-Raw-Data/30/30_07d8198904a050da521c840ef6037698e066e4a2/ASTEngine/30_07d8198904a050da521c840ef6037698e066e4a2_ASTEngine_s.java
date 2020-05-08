 package archimate.codegen;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.text.edits.TextEdit;
 
 import archimate.uml.UMLUpdater;
 import archimate.util.FileHandler;
 import archimate.util.TagNode;
 import archimate.util.JavaClass;
 import archimate.validation.JavaValidator;
 
 /**
  * Utility class providing methods for editing Java source files
  * 
  * @author Samuel Esposito
  * 
  */
 public class ASTEngine {
 
 	// the mode of the engine
 	private String mode;
 	// the file to edit
 	private IFile targetFile;
 	// the SourceInspector leading the editing
 	private SourceInspector inspector;
 	// the current pattern
 	private String pattern;
 
 	/**
 	 * Creates new {@link ASTEngine} and sets the {@link SourceInspector}
 	 * 
 	 * @param inspector
 	 *            {@link SourceInspector} handling code traversing and
 	 *            modifications
 	 * @param mode
 	 *            the engine mode: either {@link SourceInspector#GENERATE} or
 	 *            {@link SourceInspector#VALIDATE}
 	 * @param pattern
 	 *            the pattern currently processed
 	 */
 	public ASTEngine(SourceInspector inspector, String mode, String pattern) {
 		this.mode = mode;
 		this.inspector = inspector;
 		this.pattern = pattern;
 	}
 
 	/**
 	 * Creates new {@link ASTEngine} and sets the target file and
 	 * {@link SourceInspector}
 	 * 
 	 * @param targetFile
 	 *            the targetFile where the engine reads and writes
 	 * @param inspector
 	 *            {@link SourceInspector} handling code traversing and
 	 *            modifications
 	 * @param mode
 	 *            the engine mode: either {@link SourceInspector#GENERATE} or
 	 *            {@link SourceInspector#VALIDATE}
 	 * @param pattern
 	 *            the pattern currently processed
 	 */
 	public ASTEngine(IFile targetFile, SourceInspector inspector, String mode,
 			String pattern) {
 		this.mode = mode;
 		this.inspector = inspector;
 		this.pattern = pattern;
 		this.targetFile = targetFile;
 	}
 
 	/**
 	 * Parses the source in the file, lets a {@link JavaInspector} visit it and
 	 * saves the changes
 	 */
 	public void traverseSource() {
 		FileHandler handler = new FileHandler();
 		ICompilationUnit compilationUnit = JavaCore
 				.createCompilationUnitFrom(targetFile);
		String text = handler.getSource(targetFile);
 		ASTParser parser = ASTParser.newParser(AST.JLS3);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(compilationUnit);
 		// Enable binding resolution when code validation is intended
 		if (mode.equals(SourceInspector.VALIDATE)) {
 			parser.setResolveBindings(true);
 		}
 		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
 		unit.recordModifications();
 		ASTVisitor visitor = null;
 		// Select the right ASTVisitor
 		if (mode.equals(SourceInspector.GENERATE)) {
 			visitor = new JavaInspector(inspector, pattern);
 		} else if (mode.equals(SourceInspector.VALIDATE)) {
 			visitor = new JavaValidator(inspector, pattern);
 		} else if (mode.equals(SourceInspector.UPDATE)) {
 			visitor = new UMLUpdater(inspector, pattern);
 		}
 		if (visitor != null) {
 			unit.accept(visitor);
 			String sourceCode = "";
			Document doc = new Document(text);
			TextEdit edits = unit.rewrite(doc, null);
			if (edits.hasChildren()) {
				try {
 					edits.apply(doc);
				} catch (BadLocationException e) {
					System.out.println("Unable to apply changes to source.");
					e.printStackTrace();
 				}
				sourceCode += doc.get();
				handler.save(sourceCode, targetFile);
				handler.selectAndReveal(targetFile);
 			}
 		}
 	}
 
 	/**
 	 * Creates a new source file for the given archiMateTag
 	 * 
 	 * @param node
 	 *            The {@link TagNode} to generate code for
 	 * @param status
 	 *            The status
 	 */
 	public void createSourceFile(TagNode node, MultiStatus status) {
 		for (Iterator<ICodeElement> iter = node.source().iterator(); iter
 				.hasNext();) {
 			ICodeElement element = iter.next();
 			if (element instanceof JavaClass) {
 				JavaClass javaClass = (JavaClass) element;
 				FileHandler handler = new FileHandler();
 				ASTParser parser = ASTParser.newParser(AST.JLS3);
 				parser.setKind(ASTParser.K_COMPILATION_UNIT);
 				parser.setSource("".toCharArray());
 				CompilationUnit unit = (CompilationUnit) parser.createAST(null);
 				unit.recordModifications();
 				JavaHelper helper = new JavaHelper(status, pattern);
 				helper.addClass(unit, javaClass);
 				String sourceCode = "";
 				Document doc = new Document("");
 				TextEdit edits = unit.rewrite(doc, null);
 				if (edits.hasChildren()) {
 					try {
 						edits.apply(doc);
 					} catch (BadLocationException e) {
 						System.out
 								.println("Unable to apply changes to source.");
 						e.printStackTrace();
 					}
 					sourceCode += doc.get();
 					targetFile = handler.save(sourceCode, javaClass
 							.packageName(), javaClass.targetFile());
 				}
 				status.add(new Status(IStatus.INFO, status.getPlugin(), 1,
 						pattern
 								+ ": Sourcefile for \""
 								+ javaClass.className()
 								+ "\" "
 								+ (javaClass.isInterface() ? "interface"
 										: "class") + " added."
 								+ "                             ", null));
 				traverseSource();
 			}
 		}
 	}
 }
