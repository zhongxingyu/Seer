 package jp.long_long_float.cuick.compiler;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import jp.long_long_float.cuick.ast.AST;
 import jp.long_long_float.cuick.ast.AtTestCase;
 import jp.long_long_float.cuick.ast.IntegerRangeNode;
 import jp.long_long_float.cuick.exception.CompileException;
 import jp.long_long_float.cuick.exception.ConfigException;
 import jp.long_long_float.cuick.exception.FileException;
 import jp.long_long_float.cuick.exception.SemanticException;
 import jp.long_long_float.cuick.exception.SyntaxException;
 import jp.long_long_float.cuick.parser.Parser;
 import jp.long_long_float.cuick.utility.ErrorHandler;
 import jp.long_long_float.cuick.utility.FileUtils;
 import jp.long_long_float.cuick.utility.InputThread;
 import jp.long_long_float.cuick.utility.Pair;
 import jp.long_long_float.cuick.utility.Tuple3;
 
 import org.apache.commons.lang3.StringUtils;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.ExampleMode;
 
 public class Compiler {
     static public void main(String[] args) throws FileException {
         new Compiler("cuick").commandMain(args);
     }
     
     private final ErrorHandler errorHandler;
     
     public Compiler(String programName) {
         this.errorHandler = new ErrorHandler(programName);
     }
     
     public void commandMain(String[] args) throws FileException {
         Options opts = null;
         try {
             opts = Options.parse(args);
         }
         catch (CmdLineException err) {
             errorHandler.error(err.getMessage());
             errorHandler.error("cuickc --help");
             System.exit(1);
         }
         
         if(opts.isHelpFlag()) {
             System.out.println("cuickc " + opts.getParser().printExample(ExampleMode.ALL) + " source...");
             System.out.println();
             opts.getParser().printUsage(System.out);
             System.exit(0);
         }
         
         if(opts.isVarsionFlag()) {
             System.out.println("cuick 1.0");
             System.out.println("created by long_long_float");
             System.exit(0);
         }
         
         if(opts.getSourceFiles().isEmpty() && !opts.isStdinFlag()) {
             errorHandler.error("source file is empty!");
             System.exit(1);
         }
         
         ConfigLoader cl = null;
         try {
             cl = new ConfigLoader(errorHandler);
         } catch (FileNotFoundException e) {
             errorHandler.error(Config.FILE_NAME + " is not existed!");
             throw new FileException("file error");
         } catch (IOException e) {
             errorHandler.error("IO error" + e.getMessage());
             throw new FileException("file error");
         }
         
         if(opts.isStdinFlag()) {
         	try {
 				build("out.cuick", opts, cl);
 			} catch (CompileException e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
         }
         for(String sourceFile : opts.getSourceFiles()) {
             try {
                 build(sourceFile, opts, cl);
             }
             catch (ConfigException ex) {
                 errorHandler.error(ex.getMessage());
                 System.exit(1);
             }
             catch (CompileException ex) {
                 errorHandler.error(ex.getMessage());
                 System.exit(1);
             }
         }
         System.exit(0);
     }
     
     private Tuple3<String, String, Integer> exec(String[] commands, String input) {
         ProcessBuilder pb = new ProcessBuilder(commands);
         
         Process process = null;
         try {
             process = pb.start();
         } catch(IOException ex) {
             ex.printStackTrace();
             return null;
         }
         
         OutputStream pos = process.getOutputStream();
         PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(pos)));
         
         InputThread outThread = new InputThread(process.getInputStream());
         InputThread errThread = new InputThread(process.getErrorStream());
         outThread.start();
         errThread.start();
         
         if(input != null) {
             pw.println(input);
             pw.close();
         }
 
         try {
             process.waitFor();
             outThread.join();
             errThread.join();
         } catch (InterruptedException e) {
             errorHandler.error("interrupted!");
             return null;
         }
         
         String sepa = System.getProperty("line.separator");
         String out = StringUtils.join(outThread.getBuffer(), sepa) + sepa;
         String err = StringUtils.join(errThread.getBuffer(), sepa) + sepa;
         int code = process.exitValue();
         return new Tuple3<String, String, Integer>(out, err, code);
     }
     
     public boolean isEmpty(String str) {
     	for(char c : str.toCharArray()) {
     		if(c != '\r' && c != '\n' && c != ' ') {
     			return false;
     		}
     	}
     	return true;
     }
     
     private void build(String sourceFile, Options opts, ConfigLoader cl) throws CompileException {
         String destFile = sourceFile.replaceFirst(".cuick$", ".cpp");
         if(opts.getOutputFile() != null) {
             destFile = opts.getOutputFile().getPath();
         }
         
         compile(sourceFile, destFile, opts);
         
         Map<String, String> constants = new HashMap<String, String>();
         constants.put("FILE_BASENAME", sourceFile.replaceFirst(".cuick$", ""));
         constants.put("DEST_FILE", sourceFile.replaceFirst(".cuick$", ".cpp"));
         
         Config config = cl.load(constants);
         
         if(opts.isWithCompileFlag()) {
             Tuple3<String, String, Integer> ret = exec(config.compiler, null);
             if(ret == null) return;
             if(!isEmpty(ret.get_1())) System.out.println(ret.get_1());
             if(!isEmpty(ret.get_2())) System.err.println(ret.get_2());
             if(ret.get_3() != 0) {
                 errorHandler.error(config.compiler[0] + " returned " + ret.get_3());
                 return;
             }
         }
         
         //auto test
         Table table = Table.getInstance();
         if(table.isEnabledTest() && table.isDebugMode()) {
         	IntegerRangeNode range = table.getAtTestNode().getRange();
             int id = 1;
             for(Pair<AtTestCase, AtTestCase> testCase : table.getTestCases()) {
                 int begin = 0, end = 0;
                 if(range != null) {
                 	begin = range.getBegin();
                 	end = range.getEnd();
                   }
                 for(int i = begin;i <= end;i++) {
 	                String input = "", output = "";
 	                try {
 	                		String inFile = testCase.getFirst().fileName();
 	                		String outFile = testCase.getSecond().fileName();
 	                		if(range != null) {
 	                			inFile = inFile.replace("@{id}", Integer.toString(i));
 	                			outFile = outFile.replace("@{id}", Integer.toString(i));
 	                		}
 	                    input = FileUtils.readFromFile(inFile);
 	                    output = FileUtils.readFromFile(outFile);
 	                } catch (IOException e) {
 	                    // TODO 自動生成された catch ブロック
 	                    e.printStackTrace();
 	                    continue;
 	                }
 	                Tuple3<String, String, Integer> ret = exec(new String[] {config.test.exefile}, input);
 	                if(ret == null) continue;
 	                if(ret.get_1().equals(output)) {
 	                    System.out.println("test case" + id + ": ok!");
 	                }
 	                else {
 	                    System.err.println("test case" + id + ": fail! expected");
 	                    System.err.println(output);
 	                    System.err.println("but");
 	                    System.err.println(ret.get_1());
 	                }
 	                id++;
                 }
             }
         }
     }
 
     private void compile(String srcPath, String destPath, Options opts) throws CompileException{
         AST ast = parseFile(srcPath, opts);
        //ast.dump();
         //System.out.println("===============localResolve================");
         ast = localResolve(ast, opts);
         //ast.dump();
         //System.out.println("===============typeResolve================");
         ast = typeResolve(ast, opts);
         //ast.dump();
         new ParentSetter(errorHandler).parentSet(ast);
         //System.out.println("===============ASTTranslator================");
         new ASTTranslator(errorHandler).translate(ast);
         //ast.dump();
         /*System.out.println("===============localResolve================");
         ast = localResolve(ast, opts);
         ast.dump();
         System.out.println("===============typeResolve================");
         ast = typeResolve(ast, opts);
         ast.dump();*/
         //System.out.println("===============rename================");
         ast = rename(ast, opts);
         //ast.dump();
         
         
         writeFile(destPath, ast, opts);
     }
 
     private void writeFile(String path, AST ast, Options opts) throws FileException {
         BufferedWriter writer;
         try {
             writer = new BufferedWriter(new FileWriter(new File(path)));
             try {
                 //writer.write(cb.toString());
             	  if(opts.isStdoutFlag()) {
             		  System.out.println(codeGenerate(ast, opts));
             	  }
             	  else {
                     writer.write(codeGenerate(ast, opts));
             	  }
             }
             finally {
                 writer.close();
             }
         }
         catch (FileNotFoundException ex) {
             errorHandler.error("file not found: " + path);
             throw new FileException("file error");
         }
         catch (IOException ex) {
             errorHandler.error("IO error" + ex.getMessage());
             throw new FileException("file error");
         }
     }
     
     public AST parseFile(String path, Options opts) throws SyntaxException, FileException {
     	if(opts.isStdinFlag()) {
     		return new Parser(new BufferedReader(new InputStreamReader(System.in)), "stdin", errorHandler).parse();
     	}
     	else {
     		return Parser.parseFile(new File(path), errorHandler);
     	}
     }
     
     public AST localResolve(AST ast, /*TypeTable types, */Options opts) throws SemanticException {
         new LocalResolver(errorHandler).resolve(ast);
         return ast;
     }
     
     public AST rename(AST ast, Options opts) {
         new Renamer(errorHandler).rename(ast);
         return ast;
     }
     
     public AST typeResolve(AST ast, Options opts) {
         new TypeResolver(errorHandler).resolve(ast);
         return ast;
     }
     
     public String codeGenerate(AST ast, Options opts) {
         return new CodeGenerator(errorHandler).generate(ast);
     }
 }
