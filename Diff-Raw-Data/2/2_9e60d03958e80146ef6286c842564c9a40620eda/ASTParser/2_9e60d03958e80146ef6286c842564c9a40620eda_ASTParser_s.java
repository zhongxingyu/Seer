 package org.ita.testrefactoring.astparser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTRequestor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.ita.testrefactoring.codeparser.CodeParser;
 import org.ita.testrefactoring.codeparser.CodeSelection;
 import org.ita.testrefactoring.codeparser.Constructor;
 import org.ita.testrefactoring.codeparser.Method;
 import org.ita.testrefactoring.codeparser.ParserException;
 import org.ita.testrefactoring.codeparser.SourceFile;
 import org.ita.testrefactoring.codeparser.Type;
 import org.ita.testrefactoring.codeparser.TypeKind;
 
public class ASTParser extends CodeParser {
 
 	private ICompilationUnit activeCompilationUnit;
 	private ICompilationUnit[] compilationUnits;
 	private ASTSelection selection = new ASTSelection();
 	private ASTEnvironment environment;
 	
 	public void setActiveCompilationUnit(ICompilationUnit activeCompilationUnit) {
 		this.activeCompilationUnit = activeCompilationUnit;
 	}
 	
 	public void setCompilationUnits(ICompilationUnit[] compilationUnits) {
 		this.compilationUnits = compilationUnits;
 	}
 
 	@Override
 	public ASTEnvironment getEnvironment() {
 		return environment;
 	}
 
 	/**
 	 * Parser master, chama os demais parsers. Popula a lista de packages do
 	 * environment e chama os parsers para os source files encontrados.
 	 */
 	@Override
 	public void parse() throws ParserException {
 		environment = new ASTEnvironment();
 		
 		environment.setSelection(selection);
 
 		// Significa que não há nenhuma compilation unit no projeto, não será
 		// necessário continuar o parsing
 		if (compilationUnits.length == 0) {
 			return;
 		}
 		
 		if (activeCompilationUnit == null) {
 			throw new ParserException("Active compilation unit not specified.");
 		}
 		
 		List<ICompilationUnit> compilationUnitList = Arrays.asList(compilationUnits);
 		
 		if (!compilationUnitList.contains(activeCompilationUnit)) {
 			throw new ParserException("Active compilation unit not in compilation unit list.");
 		}
 
 		doASTParsing();
 
 		parseAllSourceFilesInWorkspace();
 
 		parseAllClassesInWorkspace();
 
 		parseAllCodeBlocksInWorkspace();
 	}
 
 
 	/**
 	 * Chama o parsing do AST e popula as listas de source file de cada package.
 	 * Roda uma vez para cada compilation unit parseada. Uso para popular as
 	 * listas de source file existentes em cada pacote.
 	 * 
 	 * @param compilationUnitList
 	 * @param activeCompilationUnit
 	 */
 	private void doASTParsing() {
 		org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS3);
 		parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(activeCompilationUnit);
 		parser.setResolveBindings(true);
 
 		// Projeto java que será usado para resolver os bindings
 		parser.setProject(activeCompilationUnit.getJavaProject());
 
 		parser.createASTs(compilationUnits, new String[0], new ASTRequestor() {
 			@Override
 			public void acceptAST(ICompilationUnit jdtObject, CompilationUnit astObject) {
 				PackageDeclaration pack = astObject.getPackage();
 
 				ASTPackage parsedPackage = getEnvironment().createPackage(pack.getName().toString());
 
 				parsedPackage.setASTObject(pack);
 
 				String sourceFileName = jdtObject.getPath().toFile().getName();
 
 				ASTSourceFile sourceFile = parsedPackage.createSourceFile(sourceFileName);
 
 				ASTSourceFile.ASTContainer container = sourceFile.new ASTContainer();
 
 				container.setICompilationUnit(jdtObject);
 				container.setCompilationUnit(astObject);
 				container.setRewrite(ASTRewrite.create(astObject.getAST()));
 
 				sourceFile.setASTObject(container);
 
 				super.acceptAST(jdtObject, astObject);
 			}
 		}, new NullProgressMonitor());
 	}
 
 	private void parseAllSourceFilesInWorkspace() {
 		List<ASTSourceFile> allSourceFiles = new ArrayList<ASTSourceFile>();
 
 		for (ASTPackage pack : getEnvironment().getPackageList().values()) {
 			allSourceFiles.addAll(pack.getSourceFileList().values());
 		}
 
 		for (ASTSourceFile sourceFile : allSourceFiles) {
 			SourceFileParser parser = new SourceFileParser();
 
 			parser.setSourceFile(sourceFile);
 
 			parser.parse();
 		}
 
 	}
 
 	private void parseAllClassesInWorkspace() throws ParserException {
 		List<ASTSourceFile> allSourceFiles = new ArrayList<ASTSourceFile>();
 
 		for (ASTPackage pack : getEnvironment().getPackageList().values()) {
 			allSourceFiles.addAll(pack.getSourceFileList().values());
 		}
 
 		for (ASTSourceFile sourceFile : allSourceFiles) {
 			for (ASTType type : sourceFile.getTypeList().values()) {
 				switch (type.getKind()) {
 				case CLASS: {
 					ClassParser parser = new ClassParser();
 
 					parser.setType((ASTClass) type);
 
 					parser.parse();
 
 					break;
 				}
 
 					// case INTERFACE: {
 					// parser = new InterfaceParser();
 					//
 					// break;
 					// }
 					//
 					// case ENUM: {
 					// parser = new EnumParser();
 					//
 					// break;
 					// }
 					//
 					// case ANNOTATION: {
 					// parser = new AnnotationParser();
 					//
 					// break;
 					// }
 
 				default:
 					assert false : "Should never happen.";
 				} // switch
 			}
 		}
 	}
 
 	private void parseAllCodeBlocksInWorkspace() throws ParserException {
 		List<ASTBlock> allCodeBlocksInWorkspace = new ArrayList<ASTBlock>();
 
 		for (ASTPackage pack : getEnvironment().getPackageList().values()) {
 			for (SourceFile sourceFile : pack.getSourceFileList().values()) {
 				for (Type type : sourceFile.getTypeList().values()) {
 
 					if (type.getKind() != TypeKind.UNKNOWN) {
 						for (Constructor constructor : type.getConstructorList().values()) {
 							ASTConstructor astConstructor = (ASTConstructor) constructor;
 							allCodeBlocksInWorkspace.add(astConstructor.getBody());
 						}
 
 						for (Method method : type.getMethodList().values()) {
 							ASTMethod astMethod = (ASTMethod) method;
 							
 							if (!astMethod.getNonAccessModifier().isAbstract()) {
 								allCodeBlocksInWorkspace.add(astMethod.getBody());
 							}
 						}
 					}
 				}
 			}
 		}
 
 		for (ASTBlock block : allCodeBlocksInWorkspace) {
 			BlockParser parser = new BlockParser();
 
 			parser.setBlock(block);
 
 			parser.parse();
 		}
 	}
 
 	@Override
 	public CodeSelection getSelection() {
 		return selection;
 	}
 }
