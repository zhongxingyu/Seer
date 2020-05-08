 package com.lolcode;
 
 
 import com.lolcode.checker.ErrorHandler;
 import com.lolcode.checker.ScopeChecker;
 import com.lolcode.checker.TypeGenerator;
 import com.lolcode.checker.VariableBinder;
 import com.lolcode.jgenerator.CodeGenerator;
 import com.lolcode.tree.AstBuilder;
 import com.lolcode.tree.TreeModule;
 import com.lolcode.tree.exception.BaseAstException;
 import org.antlr.v4.runtime.ANTLRFileStream;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.CommonTokenStream;
 import org.antlr.v4.runtime.LexerNoViableAltException;
 import org.antlr.v4.runtime.tree.ParseTree;
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: miha
  * Date: 6/28/13
  * Time: 10:43 PM
  */
 public class Runner {
     private static Logger log = Logger.getLogger(AstBuilder.class.getName());
     private String filename = "";
     private CompilerSettings settings;
     private String runtimeLib = "runtime.jar";
 
     private ArrayList<File> classFiles = new ArrayList<>();
 
     public Runner(CompilerSettings settings) {
         this.settings = settings;
         ErrorHandler.disableWarnings = settings.isDisableWarnings();
     }
 
     public Runner() {
         this.settings = new BaseCompilerSettings();
     }
 
     //Lexer which exits on error insted of recovering
     public static class BailLolcodeLexer extends lolcodeLexer {
         public BailLolcodeLexer(CharStream input) {
             super(input);
         }
 
         public void recover(LexerNoViableAltException e) {
             throw e; //bail
         }
     }
 
     public void setInputFile(String file) {
         filename = file;
     }
 
     private String pathToName(String path) {
         int pos = path.lastIndexOf('/');
         if (pos == -1)
             pos = 0;
         return path.substring(pos).replace(".lol", "").replace(".", "_");
     }
 
     public void run() {
         new File(settings.getOutputDir() + settings.getOutputClassPath()).mkdirs();
         for (String unit : settings.getInputFiles()) {
             try {
                 String unitName = pathToName(unit);
                 byte[] result = compileUnit(unit);
                 writeClassFile(result, unitName);
                 if (settings.isCreateJar())
                     packJarFile();
             } catch (IOException | BaseAstException e) {
                 log.severe(e.getMessage());
            } catch (CompilerException e) {
                log.severe("Failed co compile " + unit + " due to errors");
             }
         }
     }
 
     private void writeClassFile(byte[] data, String name) throws IOException {
         File f = new File(settings.getOutputDir() + settings.getOutputClassPath() + name + ".class");
         classFiles.add(f);
         BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
         out.write(data);
         out.flush();
         out.close();
     }
 
 
     private void packJarFile() throws IOException {
         if (new File(runtimeLib).exists()) {
             //copy runtime jar as a template
             FileChannel source = new FileInputStream(runtimeLib).getChannel();
             FileChannel destination = new FileOutputStream(settings.getOutputDir() + settings.getOutputJarFile()).getChannel();
             destination.transferFrom(source, 0, source.size());
             ArrayList<String> args = new ArrayList<>();
             args.add("jar");
             args.add("ufe");
             args.add(settings.getOutputJarFile());
             args.add(settings.getJarMainClass());
             for (File f : classFiles) {
                 args.add(settings.getOutputClassPath() + f.getName());
             }
             ProcessBuilder builder = new ProcessBuilder(args);
             builder.directory(new File(settings.getOutputDir()).getAbsoluteFile());
             builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
             builder.redirectError(ProcessBuilder.Redirect.INHERIT);
             builder.start();
         } else {
             log.severe("Runtime library " + runtimeLib + " not found, unable to pack jar file!");
         }
     }
 
    private byte[] compileUnit(String filename) throws IOException, BaseAstException, CompilerException {
         lolcodeLexer lexer = new BailLolcodeLexer(new ANTLRFileStream(filename)); //use BailLexer
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         lolcodeParser parser = new lolcodeParser(tokens);
         parser.setErrorHandler(new BailErrorStrategy()); //yse bail error strategy
         ParseTree tree = parser.file(); //builds parse tree, do syntax check
         AstBuilder visitor = new AstBuilder(filename);
         TreeModule ast = (TreeModule) visitor.visit(tree); //builds ast
         ScopeChecker scopeChecker = new ScopeChecker();
         scopeChecker.visit(ast);
         VariableBinder binder = new VariableBinder();
         binder.visit(ast);
         TypeGenerator infer = new TypeGenerator();
         infer.visit(ast);
        if (!ErrorHandler.clean)
            throw new CompilerException();
         CodeGenerator gen = new CodeGenerator(settings);
         gen.visit(ast);
         return gen.getCompiledBytecode();
     }
 
    @Deprecated
     public void test() {
         try {
             CharStream cs = new ANTLRFileStream(filename);
 
             //use BailLexer
             lolcodeLexer lexer = new BailLolcodeLexer(cs);
             CommonTokenStream tokens = new CommonTokenStream(lexer);
             lolcodeParser parser = new lolcodeParser(tokens);
             parser.setErrorHandler(new BailErrorStrategy()); //yse bail error strategy
 
             /*
                 parser.file() throws RuntimeException if something wrong now, do we expand on it?
                 like catching stuff and printing our errors...
              */
             ParseTree tree = parser.file(); //builds parse tree, do syntax check
             AstBuilder visitor = new AstBuilder(filename);
             TreeModule ast = (TreeModule) visitor.visit(tree); //builds ast
             ScopeChecker scopeChecker = new ScopeChecker();
             scopeChecker.visit(ast);
             VariableBinder binder = new VariableBinder();
             binder.visit(ast);
             TypeGenerator infer = new TypeGenerator();
             infer.visit(ast);
             CodeGenerator gen = new CodeGenerator(settings);
             gen.visit(ast);
             if (!ErrorHandler.clean) {
                 System.err.println("Compilation failed");
                 System.exit(-1);
             }
             System.out.println("Compilation finished successfully");
         } catch (IOException | LexerNoViableAltException e) {
             log.severe(e.toString());
         } catch (BaseAstException e) {
             log.severe(String.valueOf(e));
             System.exit(-1);
         }
     }
 }
