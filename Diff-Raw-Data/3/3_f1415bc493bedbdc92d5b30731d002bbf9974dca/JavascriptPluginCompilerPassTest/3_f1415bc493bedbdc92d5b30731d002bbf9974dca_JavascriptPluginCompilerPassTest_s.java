 package liebenberg.closure_utilities.soy;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Multimap;
 import com.google.javascript.jscomp.Compiler;
 import com.google.javascript.jscomp.*;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class JavascriptPluginCompilerPassTest {
 
     private JavascriptPluginCompilerPass compilerPass;
 
     private InputStreamReader inputStreamReader;
 
     private InputStream inputStream;
 
     private Compiler compiler;
 
 
     @Before
     public void setUp() throws Exception {
         compiler = new Compiler();
         inputStream = getClass().getResourceAsStream("/jsplugins/SoyOptimizerPlugin.js");
         inputStreamReader = new InputStreamReader(inputStream);
         compilerPass = new JavascriptPluginCompilerPass(inputStreamReader, compiler);
     }
 
     @After
     public void tearDown() throws Exception {
         compilerPass = null;
         inputStream.close();
         inputStreamReader.close();
     }
 
    public final static String BASE_PATH = "/app/src/javascript/goog/base.js";
 
     @Test
     public void testProcess() throws Exception {
 
         final CompilerOptions compilerOptions = new CompilerOptions();
         final List<SourceFile> externs = new ArrayList<>();
         final List<SourceFile> sources = new ArrayList<>();
         sources.add(SourceFile.fromInputStream("base.js",
                 getClass().getResourceAsStream(BASE_PATH)));
         Multimap<CustomPassExecutionTime, CompilerPass> customPasses =
                 LinkedListMultimap.create();
         compilerPass.configurePasses(customPasses);
         compilerOptions.setCustomPasses(customPasses);
         Result compilerResult =
                 compiler.compile(externs, sources, compilerOptions);
 
     }
 }
