 package tests.jdolly;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.apache.tools.ant.XmlLogger;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Modifier;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.testorrery.IGenerator;
 
 import saferefactor.core.AbstractSafeRefactor;
 import saferefactor.core.Parameters;
 import saferefactor.core.Report;
 import saferefactor.core.SafeRefactor;
 import saferefactor.core.util.Constants;
 
 import AST.Program;
 import AST.RawCU;
 import AST.RefactoringException;
 
 public abstract class Test {
 
 	private static final String WARNING_STATUS = "REFACTORING_INAPPLICABLE";
 	private static final String OK = "SUCCESSFUL_REFACTORING";
 	private static final String BEHAVIORAL_CHANGE = "BEHAVIORCHANGE_FAILURE";
 	private static final String BEHAVIORAL_CHANGE2 = "BEHAVIORCHANGE_FAILURE2";
 	private static final String COMPILATION_ERROR = "POST_REFACTOR_NOT_COMPILE";
 	private static final String INPUT_COMPILATION_ERROR = "PRE_REFACTOR_NOT_COMPILE";
 	private static final String ENGINE_CRASH = "ENGINE_CRASH";
 	public static final String FILE_SEPARATOR = System
 			.getProperty("file.separator");
 
 	private Map<String, Integer> problems = new HashMap<String, Integer>();
 	private int nmCompilationError = 0;
 	private int nmInputCompilationError = 0;
 	private int nmBehaviorChange = 0;
 	private int nmBehaviorChange2 = 0;
 
 	private int nmWarning = 0;
 	private int engineCrash = 0;
 	private String ce;
 	public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
 
 	// time metrics
 	long totalInputCompileTime = 0;
 	long outputCompileTime = 0;
 	long refactoringTime = 0;
 	long saferefactorTime = 0;
 	long instrumentTime = 0;
 	long closedWorldTime = 0;
 	private long tempBefore;
 	private long tempAfter;
 	private long tempTotal;
 	private SafeRefactorLogger srLogger = new SafeRefactorLogger();
 	private long timeStrategy2 = 0;
 
 	public static String getSystemTempDir() {
 		String tempdir = System.getProperty("java.io.tmpdir");
 		if (tempdir == null) {
 			throw new IllegalArgumentException("Temp dir is not specified");
 		}
 		String separator = System.getProperty("file.separator");
 		if (!tempdir.endsWith(separator)) {
 			return tempdir + separator;
 		}
 		return tempdir;
 	}
 
 	public void run() {
 
 		long before = System.currentTimeMillis();
 
 		String refactoringName = this.getClass().getSimpleName()
 				.replace("Test", "");
 		;
 
 		TestLogger logger = new TestLogger(refactoringName, getSystemTempDir());
 
 		logger.start();
 		int i = 0;
 		// for (File testFolder : tests) {
 		Program program = null;
 		Program target = null;
 
 		for (List<org.eclipse.jdt.core.dom.CompilationUnit> classes : getCUGen()) {
 			i++;
		
 			List<RawCU> compilationUnits = getCompilationUnits(classes);
 
 			RawCU[] rawCUs = new RawCU[compilationUnits.size()];
 			compilationUnits.toArray(rawCUs);
 
 			logger.logGenerated(classes);
 			File inDir = new File(logger.getTestDirectory(), "in");
 			inDir.mkdir();
 			File outDir = new File(logger.getTestDirectory(), "out/jrrt");
 			outDir.mkdirs();
 
 			// check whether input compiles
 			tempBefore = System.currentTimeMillis();
 			String compileMessage = compile(inDir.toString());
 			tempAfter = System.currentTimeMillis();
 			tempTotal = tempAfter - tempBefore;
 			totalInputCompileTime = totalInputCompileTime + tempTotal;
 
 			if (compileMessage.contains("ERROR")) {
 				System.out.println("Input nao compila");
 				String fileLog = outDir + FILE_SEPARATOR
 						+ INPUT_COMPILATION_ERROR;
 				FileUtil.gravaArquivo(fileLog, compileMessage.toString());
 				nmInputCompilationError++;
 				continue;
 			}
 
 			// if (i > 209)
 			// break;
 			// if (i != 209)
 			// continue;
 
 			try {
 
 				program = Program.fromCompilationUnits(rawCUs);
 				// System.out.println(program);
 
 				// aqui aplica o refatoramento
 				tempBefore = System.currentTimeMillis();
 				target = refactoring(program);
 				tempAfter = System.currentTimeMillis();
 				tempTotal = tempAfter - tempBefore;
 				refactoringTime = refactoringTime + tempTotal;
 
 				logger.logPostRefactoring(target);
 
 				// checa se compila
 				tempBefore = System.currentTimeMillis();
 				compileMessage = compile(outDir.toString());
 				tempAfter = System.currentTimeMillis();
 				tempTotal = tempAfter - tempBefore;
 				outputCompileTime = refactoringTime + tempTotal;
 
 				if (!compileMessage.contains("ERROR")) {
 
 					if (checkBehaviorConditions(inDir.toString(),
 							outDir.toString())) {
 						// logger.logCoverage();
 						String fileLog = outDir + FILE_SEPARATOR
 								+ BEHAVIORAL_CHANGE;
 						FileUtil.gravaArquivo(fileLog, BEHAVIORAL_CHANGE);
 						nmBehaviorChange++;
 						logger.logBehavioralChange();
 
 						// MODIFICACAO PARA APLICAR O REFACTORING COM OS TESTES
 						// INCLUIDOS
 						// aplica denovo com testes para serem refatorados
 						// tempBefore = System.currentTimeMillis();
 						// String testPath = Constants.TEST;
 						// File testFolder = new File(testPath);
 						// File[] listOfFiles = testFolder
 						// .listFiles(new FileFilter() {
 						//
 						// public boolean accept(File arg0) {
 						// if (arg0.getName().endsWith(".java"))
 						// return true;
 						// return false;
 						// }
 						// });
 						//
 						// for (File string : listOfFiles) {
 						// System.out.println("testes: " + string);
 						// }
 						// // finaliza antigo
 						// program.end();
 						//
 						// target.end();
 						//
 						// // AST.Program.
 						// program = Program.fromCompilationUnitsAndTests(
 						// listOfFiles, rawCUs);
 						// logger.logPreRefactoring(program);
 						// target = refactoring(program);
 						// logger.logPostRefactoring2(target);
 						//
 						// if (checkBehaviorConditions2(inDir.toString(),
 						// outDir.toString())) {
 						// String fileLog2 = outDir + FILE_SEPARATOR
 						// + BEHAVIORAL_CHANGE2;
 						// FileUtil.gravaArquivo(fileLog2, BEHAVIORAL_CHANGE2);
 						// nmBehaviorChange2++;
 						// }
 						// //check whether tests were changed
 						// logger.checkTestRefactoring();
 						// tempAfter = System.currentTimeMillis();
 						// tempTotal = tempAfter - tempBefore;
 						// timeStrategy2 = timeStrategy2 + tempTotal;
 
 					} else {
 						// logger.logCoverage();
 						String fileLog = outDir + FILE_SEPARATOR + OK;
 						FileUtil.gravaArquivo(fileLog, OK);
 					}
 
 				} else {
 					String fileLog = outDir + FILE_SEPARATOR
 							+ COMPILATION_ERROR;
 					FileUtil.gravaArquivo(fileLog, compileMessage.toString());
 					nmCompilationError++;
 
 					ce = compileMessage;
 					jrrtOracle(ce);
 					logger.logProgramCE(ce);
 				}
 				program.end();
 				target.end();
 				target = null;
 			} catch (RefactoringException e) {
 				String fileLog = outDir + FILE_SEPARATOR + WARNING_STATUS;
 				FileUtil.gravaArquivo(fileLog, e.getMessage());
 				nmWarning++;
 			} catch (Error e) {
 				String fileLog = outDir + FILE_SEPARATOR + ENGINE_CRASH;
 				FileUtil.gravaArquivo(fileLog, e.getMessage());
 				engineCrash++;
 				System.out.println("Crash Engine Fail: " + e.toString());
 				break;
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			rawCUs = null;
 
 			program = null;
 
 			// break;
 		}
 		long after = System.currentTimeMillis();
 		long tempo = (after - before) / 1000;
 
 		double somaCoberturas = 0;
 		for (Double cobertura : logger.getCoberturas()) {
 			somaCoberturas = somaCoberturas + cobertura;
 		}
 		double porcentagemCobertura = somaCoberturas
 				/ logger.getCoberturas().size();
 
 		StringBuffer sb = new StringBuffer();
 		sb.append("===================================\n");
 		sb.append("TIME RESULTS\n");
 		sb.append("===================================");
 		sb.append("\nTempo para compilar source: " + totalInputCompileTime
 				/ 1000);
 		sb.append("\nTempo para compilar target " + outputCompileTime / 1000);
 		sb.append("\nTempo para aplicar o refactoring: " + refactoringTime
 				/ 1000);
 		sb.append(srLogger.getResults());
 		sb.append("\nTempo para aplicar a abordagem 2: " + timeStrategy2 / 1000);
 		long tempoTotal = tempo - timeStrategy2 / 1000;
 		sb.append("\nTempo total sem segunda abordagem: " + tempoTotal);
 		long totalMinutes = tempoTotal / 60;
 		// System.out.println("Total minutos:" + totalMinutes);
 		double totalHours = (double) tempoTotal / 60 / 60;
 		long hours = totalMinutes / 60;
 		// System.out.println("Total horas:" + hours);
 		long fraction = totalMinutes % 60;
 		// System.out.println("Fracao:" + fraction);
 		long minutes = fraction * 60 / 100;
 		// System.out.println("minutos:" + minutes);
 
 		sb.append("\nTempo total em HH:mm " + hours + ":" + minutes);
 		sb.append("\nTempo total em hours " + totalHours);
 
 		sb.append("\n===================================\n");
 		sb.append("GENERATION RESULTS\n");
 		sb.append("===================================");
 		sb.append("\nTotal de testes:  " + i);
 		sb.append("\nTotal de inputs que n�o compilam:  "
 				+ nmInputCompilationError);
 		int compilableInputs = i - nmInputCompilationError;
 		sb.append("\nTotal de inputs que compilam:  " + compilableInputs);
 		sb.append("\n===================================\n");
 		sb.append("FAILURE RESULTS\n");
 		sb.append("===================================");
 		sb.append("\nTotal de warning status: " + nmWarning);
 		sb.append("\nTotal de falhas de engine crash: " + engineCrash);
 		sb.append("\nTotal de falhas de compilacao: " + nmCompilationError);
 		sb.append("\nTotal de mudancas comportamentais (mundo aberto): "
 				+ nmBehaviorChange);
 		sb.append("\nTotal de mudancas comportamentais (mundo fechado): "
 				+ nmBehaviorChange);
 		sb.append("\n===================================\n");
 		sb.append("FAULT RESULTS\n");
 		sb.append("===================================");
 		sb.append("\nBugs de compilacao: " + problems.size());
 		sb.append("\n===================================\n");
 		sb.append("COVERAGE RESULTS\n");
 		sb.append("===================================");
 		sb.append("\nMenor Cobertura: "
 				+ String.valueOf(logger.getMenorCobertura()));
 		sb.append("\nMaior Cobertura: "
 				+ String.valueOf(logger.getMaiorCobertura()));
 		sb.append("\nTest coverage: " + String.valueOf(porcentagemCobertura));
 		sb.append("\nTotal de testes refactorados: "
 				+ String.valueOf(logger.getRefactoredTests().size()));
 
 		System.out.println(sb.toString());
 
 		// String resumo = "Resumo:" + "Total de testes: " + i + "\n"
 		// + "Total de erros de inputs do not compile: " +
 		// nmInputCompilationError+ "\n"
 		// + "Total de erros de engine crash: " + engineCrash + "\n"
 		// + "Total de erros de compilacao: " + nmCompilationError + "\n"
 		// + "Total de mudancas comportamentais: " + nmBehaviorChange
 		// + "\n Total de mudancas comportamentais2: " + nmBehaviorChange2
 		// + "\n" + "Total de warning status: " + nmWarning + "\n"
 		// + "\nTempo total: " + tempo + "\nBugs de compilacao: "
 		// + problems.size() + "Menor Cobertura: "
 		// + String.valueOf(logger.getMenorCobertura())
 		// + "Maior Cobertura: "
 		// + String.valueOf(logger.getMaiorCobertura())
 		// + "Test coverage: " + String.valueOf(porcentagemCobertura)
 		// + "Total de testes refactorados: " +
 		// String.valueOf(logger.getRefactoredTests().size());
 
 		logger.logProgramsBC();
 		logger.logCEs(problems);
 		logger.logProgramsCE();
 		logger.logRefactoredTests();
 		logger.logUnderCoveragePrograms();
 		FileUtil.gravaArquivo(logger.getLogDir().toString() + "/SUMARRY_JRRT",
 				sb.toString());
 		// System.out.println(resumo);
 
 	}
 
 	private List<RawCU> getCompilationUnits(
 			List<org.eclipse.jdt.core.dom.CompilationUnit> classes) {
 
 		List<RawCU> results = new ArrayList<RawCU>();
 		for (org.eclipse.jdt.core.dom.CompilationUnit cu : classes) {
 
 			String cuName = getPrimaryTypeName(cu);
 			System.out.println(cuName);
 			// String cuSource = cu.toString();
 			// String packageName = cu.getPackage().getName()
 			// .getFullyQualifiedName();
 
 			results.add(new RawCU(cuName + ".java", cu.toString()));
 		}
 		return results;
 	}
 
 	protected String getPrimaryTypeName(
 			org.eclipse.jdt.core.dom.CompilationUnit cu) {
 		String first = null;
 		for (Object type : cu.types()) {
 			if (type instanceof TypeDeclaration) {
 				String typeName = ((TypeDeclaration) type).getName().toString();
 				if (first == null) {
 					first = typeName;
 				}
 				for (Object modifier : ((TypeDeclaration) type).modifiers()) {
 					if (((Modifier) modifier).getKeyword() == Modifier.ModifierKeyword.PUBLIC_KEYWORD) {
 						return typeName;
 					}
 				}
 			}
 		}
 		return first;
 	}
 
 	protected abstract IGenerator<List<CompilationUnit>> getCUGen();
 
 	protected abstract Program refactoring(Program program)
 			throws RefactoringException;
 
 	private String compile(String path) {
 
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ByteArrayOutputStream outError = new ByteArrayOutputStream();
 
 		PrintWriter pw = new PrintWriter(out);
 		PrintWriter pwerror = new PrintWriter(outError);
 
 		org.eclipse.jdt.internal.compiler.batch.Main.compile(
 				"-classpath rt.jar \"" + path + "\"", pw, pwerror);
 
 		// System.out.println(out.toString());
 
 		// System.out.println(outError.toString());
 		// result = !outError.toString().contains("ERROR");
 
 		return outError.toString();
 	}
 
 	private void jrrtOracle(String error) {
 
 		String[] lines = error.split("\n");
 
 		String x = "";
 		boolean inclueAproxima = true;
 		boolean limit = false;
 
 		for (String s : lines) {
 			if (s.contains("^"))
 				continue;
 			// so conta o 1o. erro
 			if (s.contains("----------") /* && achouErro */) {
 				if (limit)
 					break;
 				limit = true;
 			}
 			if (s.contains("ERROR")) {
 				inclueAproxima = true;
 				// achouErro = true;
 			}
 
 			if (inclueAproxima) {
 				if (s.contains("WARNING"))
 					inclueAproxima = false;
 				else {
 					if (!s.contains("ERROR") && !s.contains("problem")) {
 						// para remover nome de classes e metodos
 						// s = s
 						// .replaceAll(
 						// "[(]?[(]?[a-zA-Z0-9]+_[0-9][(]?[(]?[\\w]*[)]?[\\w]*[)]?",
 						// " ");
 						// para remover o codigo que ocorre o problmea, deixar
 						// so a mensagem
 						if (s.contains(";"))
 							s = "";
 						else
 							s = s.replaceAll(
 									"[(]?[(]?[a-zA-Z0-9]+_[0-9][(]?[(]?[\\w]*[)]?[\\w]*[)]?",
 									" ");
 
 					} else
 						s = "ERROR";
 					x = x + "\n" + s;
 
 				}
 
 			}
 		}
 		if (problems.containsKey(x)) {
 			Integer integer = problems.get(x);
 			integer = integer + 1;
 			problems.put(x, integer);
 		} else {
 			problems.put(x, 1);
 
 		}
 
 	}
 
 	public void setNmCompilationError(int nmCompilationError) {
 		this.nmCompilationError = nmCompilationError;
 	}
 
 	public int getNmCompilationError() {
 		return nmCompilationError;
 	}
 
 	// public boolean checkBehaviorConditions(String in, String out) {
 	//
 	//
 	//
 	// Boolean result = false;
 	//
 	// AnalyzerRunner.execute(in, out);
 	//
 	// File file = new File("/tmp/result/source/TEST-RandoopTest.xml");
 	// if (file.exists()) {
 	// result = !Mediator.hasSameBehavior("/tmp/result/source",
 	// "/tmp/result/target");
 	// }
 	//
 	// return result;
 	// }
 
 	private saferefactor.core.util.Project createSourceProject(
 			String testDirectory) {
 
 		saferefactor.core.util.Project source;
 
 		source = new saferefactor.core.util.Project();
 		source.setProjectFolder(new File(testDirectory));
 		source.setBuildFolder(new File(testDirectory));
 		source.setSrcFolder(new File(testDirectory));
 
 		return source;
 
 	}
 
 	private AbstractSafeRefactor configureSafeRefactor(
 			saferefactor.core.util.Project source,
 			saferefactor.core.util.Project target) throws Exception {
 		AbstractSafeRefactor result;
 
 		File saferefactorJar = new File("lib/saferefactor-1.jar");
 		System.setProperty("extra.jars", saferefactorJar.getAbsolutePath());
 
 		Parameters parameters = new Parameters();
 		parameters.setTimeLimit(1);
 		// parameters.setCheckCoverage(true);
 		result = new SafeRefactor(source, target, parameters);
 
 		return result;
 	}
 
 	public boolean checkBehaviorConditions(String in, String out)
 			throws Exception {
 
 		saferefactor.core.util.Project source = createSourceProject(in);
 		saferefactor.core.util.Project target = createSourceProject(out);
 
 		AbstractSafeRefactor saferefactor = configureSafeRefactor(source,
 				target);
 		saferefactor.checkTransformation();
 		Report report = saferefactor.getReport();
 		return !report.isRefactoring();
 		// getLogger().logCoverage(report.getCoverage().getLineRate());
 
 		// FileUtil.createFolders();
 		// File buildFile = new File("ant/"
 		// + "build.xml");
 		// Project p = new Project();
 		//
 		// // propriedades
 		// p.setProperty("source", in);
 		// p.setProperty("target", out);
 		// // p.setProperty("methodlist", methodList.toString());
 		// p.setProperty("timeout", "1");
 		// p.setProperty("bin", "");
 		// p.setProperty("lib", "");
 		// p.setProperty("src", "");
 		// p.setProperty("tests.folder", Constants.);
 		//
 		// // log do ant no console
 		// // TODO logar tamb�m em arquivo
 		// DefaultLogger consoleLogger = new DefaultLogger();
 		// consoleLogger.setErrorPrintStream(System.err);
 		// consoleLogger.setOutputPrintStream(System.out);
 		// consoleLogger.setMessageOutputLevel(Project.MSG_INFO);
 		// p.addBuildListener(consoleLogger);
 		// // logger = new SafeRefactorLogger();
 		// // logger.setErrorPrintStream(System.err);
 		// // logger.setOutputPrintStream(System.out);
 		// // logger.setMessageOutputLevel(Project.MSG_INFO);
 		//
 		// p.addBuildListener(srLogger);
 		//
 		// // FileOutputStream outXml;
 		// // PrintStream ps;
 		// // try {
 		// // outXml = new FileOutputStream(out + Constants.FILE_SEPARATOR +
 		// "antLogger.xml");
 		// // ps = new PrintStream(outXml);
 		// // XmlLogger xmlLogger = new XmlLogger();
 		// //// xmlLogger.setOutputPrintStream(ps);
 		// //// xmlLogger.setErrorPrintStream(ps);
 		// //// xmlLogger.setMessageOutputLevel(Project.MSG_INFO);
 		// // p.addBuildListener(xmlLogger);
 		// // } catch (FileNotFoundException e) {
 		// // // TODO Auto-generated catch block
 		// // e.printStackTrace();
 		// // }
 		//
 		// p.init();
 		// ProjectHelper helper = ProjectHelper.getProjectHelper();
 		// p.addReference("ant.projectHelper", helper);
 		// helper.parse(p, buildFile);
 		// p.executeTarget(p.getDefaultTarget());
 		//
 		// ResultComparator rc = new ResultComparator(Constants.TESTSRC,
 		// Constants.TESTTGT);
 		// Report report = rc.generateReport();
 		// return !report.isSameBehavior();
 		//
 		// // Saferefactor sr = new Saferefactor(in, out, "", "", "");
 		// // return !sr.isRefactoring("1",true);
 	}
 
 	public static void main(String[] args) {
 		Test test = new Test() {
 
 			@Override
 			protected IGenerator<List<CompilationUnit>> getCUGen() {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			protected Program refactoring(Program program)
 					throws RefactoringException {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 		};
 
 		try {
 			System.out.println("Inicio");
 			boolean checkBehaviorConditions = test.checkBehaviorConditions(
 					"/Users/gustavoas/workspaces/csmr2012/source/bin",
 					"/Users/gustavoas/workspaces/csmr2012/target/bin");
 			System.out.println("Refactoring? " + !checkBehaviorConditions);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
