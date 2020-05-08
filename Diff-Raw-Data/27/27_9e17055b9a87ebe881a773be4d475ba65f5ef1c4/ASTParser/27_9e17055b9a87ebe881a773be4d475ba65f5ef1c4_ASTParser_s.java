 package org.ita.testrefactoring.astparser;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTRequestor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
 import org.ita.testrefactoring.metacode.AbstractParser;
 import org.ita.testrefactoring.metacode.ParserException;
 
 public class ASTParser extends AbstractParser {
 
 	private ASTEnvironment environment;
 
 	/**
 	 * Parser master, chama os demais parsers. Popula a lista de packages do
 	 * environment e chama os parsers para os source files encontrados.
 	 */
 	@Override
 	public void parse() throws ParserException {
 		List<IPackageFragment> packageList;
 
 		try {
 			packageList = Utils.getAllPackagesInWorkspace();
 		} catch (CoreException e) {
 			throw new ParserException(e);
 		}
 
 		environment = new ASTEnvironment();
 
 		setEnvironment(environment);
 
 		List<ICompilationUnit> compilationUnitList = getAllCompilationUnits(packageList);
 
 		ICompilationUnit activeCompilationUnit = getActiveCompilationUnit(compilationUnitList);
 
 		// Significa que não há nenhuma compilation unit no projeto, não será
 		// necessário continuar o parsing
 		if (activeCompilationUnit == null) {
 			return;
 		}
 
 		doASTParsing(compilationUnitList, activeCompilationUnit);
 
 		for (ASTPackage pack : environment.getPackageList().values()) {
			for (ASTSourceFile sourceFile : pack.getSourceFileList().values()) {
				SourceFileParser parser = new SourceFileParser();
 
				parser.setSourceFile(sourceFile);
 
				parser.parse();
			}
 		}
 
 		List<ASTSourceFile> allSourceFiles = new ArrayList<ASTSourceFile>();
 
 		for (ASTPackage pack : environment.getPackageList().values()) {
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
 
 	/**
 	 * Popula a lista de packages do environment a partir da lista de packages
 	 * bruta fornecida pelo Eclipse. Devolve uma lista de compilation unit's do
 	 * ambiente, a partir da qual o parsing continuará.
 	 * 
 	 * @param packageList
 	 * @param environment
 	 * @return
 	 * @throws ParserException
 	 */
 	private List<ICompilationUnit> getAllCompilationUnits(List<IPackageFragment> packageList) throws ParserException {
 		List<ICompilationUnit> compilationUnitList = new ArrayList<ICompilationUnit>();
 
 		for (IPackageFragment _package : packageList) {
 
 			try {
 				compilationUnitList.addAll(Arrays.asList(_package.getCompilationUnits()));
 			} catch (JavaModelException e) {
 				throw new ParserException(e);
 			}
 		}
 
 		return compilationUnitList;
 	}
 
 	/**
 	 * Chama o parsing do AST e popula as listas de source file de cada package.
 	 * 
 	 * @param compilationUnitList
 	 * @param activeCompilationUnit
 	 */
 	private void doASTParsing(List<ICompilationUnit> compilationUnitList, ICompilationUnit activeCompilationUnit) {
 		org.eclipse.jdt.core.dom.ASTParser parser = org.eclipse.jdt.core.dom.ASTParser.newParser(AST.JLS3);
 		parser.setKind(org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT);
 		parser.setSource(activeCompilationUnit);
 		parser.setResolveBindings(true);
 
 		// Projeto java que será usado para resolver os bindings
 		parser.setProject(activeCompilationUnit.getJavaProject());
 
 		parser.createASTs(compilationUnitList.toArray(new ICompilationUnit[0]), new String[0], new ASTRequestor() {
 			@Override
 			/**
 			 * Roda uma vez para cada compilation unit parseada. Uso para popular as listas de source file existentes em cada pacote.
 			 */
 			public void acceptAST(ICompilationUnit jdtObject, CompilationUnit astObject) {
 				PackageDeclaration pack = astObject.getPackage();
 
 				ASTPackage parsedPackage = environment.createPackage(pack.getName().toString());
 
 				parsedPackage.setASTObject(pack);
 
 				ASTSourceFile sourceFile = parsedPackage.createSourceFile(jdtObject.getPath().toFile().getName());
 
 				ASTSourceFile.ASTContainer container = sourceFile.new ASTContainer();
 
 				container.setICompilationUnit(jdtObject);
 				container.setCompilationUnit(astObject);
 				container.setRewrite(ASTRewrite.create(astObject.getAST()));
 
 				sourceFile.setASTObject(container);
 
 				super.acceptAST(jdtObject, astObject);
 			}
 		}, new NullProgressMonitor());
 	}
 
 	private ICompilationUnit getActiveCompilationUnit(List<ICompilationUnit> compilationUnitList) {
 		ICompilationUnit activeCompilationUnit = Utils.getActiveICompilationUnit();
 		if (activeCompilationUnit == null) {
 			if (compilationUnitList.size() > 0) {
 				// Se não tem nenhum arquivo aberto no editor, considero o
 				// primeiro arquivo que foi encontrado como ativo
 				activeCompilationUnit = compilationUnitList.get(0);
 			}
 		}
 		return activeCompilationUnit;
 	}
 
 	@Override
 	public ASTEnvironment getEnvironment() {
 		return (ASTEnvironment) super.getEnvironment();
 	}
 }
