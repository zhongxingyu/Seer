 package x10cpp.postcompiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import polyglot.util.DiffWriter;
 import polyglot.util.ErrorQueue;
 import polyglot.util.QuotedStringTokenizer;
 import x10.X10CompilerOptions;
 
 public class CXXMakeBuilder extends CXXCommandBuilder {
     CXXCommandBuilder ccb;
 
     public CXXMakeBuilder(CXXCommandBuilder ccb)
     {
         this.ccb = ccb;
     }
 
     /** Construct the C++ compilation command */
     public String[] buildCXXCommandLine(Collection<String> outputFiles) 
     {
         String post_compiler = options.post_compiler;
         if (post_compiler.contains("javac")) {
             post_compiler = defaultPostCompiler();
         }
 
         QuotedStringTokenizer st = new QuotedStringTokenizer(post_compiler);
         int pc_size = st.countTokens();
         ArrayList<String> firstTokens = new ArrayList<String>();
         String token = "";
         for (int i = 0; i < pc_size; i++) {
             token = st.nextToken();
             // A # as the first token signifies that the default postcompiler for this platform be used
             if (i==0 && token.equals("#")) {
             	firstTokens.add(defaultPostCompiler());
             	continue;
             }
 
         	// consume all tokens up until the next # (or %) whereupon we will insert (or not)
         	// default CXXFLAGS parameters and generated compilation units
             if (token.equals("#") || token.equals("%")) {
                 break;
             }
             firstTokens.add(token);
         }
 
         ArrayList<String> preArgs = new ArrayList<String>();
         boolean skipArgs = token.equals("%");
         if (!skipArgs) {
             ccb.addPreArgs(preArgs);
            if (options.buildX10Lib != null && sharedLibProps.staticLib) {
                preArgs.add("-c");
            } else {
                addExecutablePath(preArgs);
            }
         }
 
         ArrayList<String> srcFiles = new ArrayList<String>();
         for (String file : outputFiles) {
             file = file.replace(File.separatorChar,'/');
             if (file.endsWith(".cu")) continue;
             srcFiles.add(file);
         }
 
         ArrayList<String> secondTokens = new ArrayList<String>();
         while (st.hasMoreTokens()) {
             token = st.nextToken();
             // The second '#' delimits the libraries that have to go at the end
             if (token.equals("#") || token.equals("%")) {
                 break;
             }
             secondTokens.add(token);
         }
 
         ArrayList<String> postArgs = new ArrayList<String>();
         boolean skipLibs = token.equals("%");
         if (!skipLibs) {
             ccb.addPostArgs(postArgs);
         }
 
         ArrayList<String> thirdTokens = new ArrayList<String>();
         while (st.hasMoreTokens()) {
             thirdTokens.add(st.nextToken());
         }
 
 
         try {
             DiffWriter dw = new DiffWriter(new File(options.output_directory, "Makefile"));
 
             println(dw, "SRC=", srcFiles);
             dw.println("OBJS=$(patsubst %.cc,%.o,$(SRC))");
             dw.println("DEPENDS=$(patsubst %.cc,%.d,$(SRC))");
             println(dw, "FIRST=", firstTokens);
             println(dw, "PREARGS=", preArgs);
             println(dw, "SECOND=", secondTokens);
             println(dw, "POSTARGS=", postArgs);
             println(dw, "THIRD=", thirdTokens);
 
             dw.println("all: $(OBJS)");
            dw.println("\t$(FIRST) $(PREARGS) $(OBJS) $(SECOND) $(POSTARGS) $(THIRD)");
             dw.println(".cc.o:");
             dw.println("\t$(FIRST) $(PREARGS) -MMD -MF $(patsubst %.cc,%.d,$<) -c $< -o $@ $(SECOND) $(POSTARGS) $(THIRD)");
             dw.println("-include $(DEPENDS)");
 
             dw.flush();
             dw.close();
         } catch (IOException e) {
 			System.out.println(e);
         }
 
         ArrayList<String> cxxCmd = new ArrayList<String>();
         cxxCmd.add("make");
         cxxCmd.addAll(options.makeOptions);
         cxxCmd.add("all");
         return cxxCmd.toArray(new String[cxxCmd.size()]);
     }
 
     private void println(DiffWriter dw, String preToken, Collection<String> tokenArray) throws IOException
     {
         StringBuilder str = new StringBuilder();
         str.append(preToken);
         for (String token : tokenArray) {
             str.append(token+" ");
         }
         dw.println(str.toString());
     }
 
 }
