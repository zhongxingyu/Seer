 package cn.shenyanchao.generator;
 
 import cn.shenyanchao.common.Consts;
 import cn.shenyanchao.utils.PathUtils;
 import japa.parser.ast.CompilationUnit;
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author shenyanchao
  *         Date:  6/18/13
  *         Time:  1:16 PM
  */
 public class TestGenerator {
 
     private static final Logger LOG = LoggerFactory.getLogger(TestGenerator.class);
 
     public static void writeJavaTest(String testDir, CompilationUnit testCompilationUnit) {
         String packageName = testCompilationUnit.getPackage().getName().toString();
         StringBuilder sb = new StringBuilder();
         sb.append(testDir).append(File.separator);
         sb.append(PathUtils.packageToPath(packageName)).append(File.separator);
         sb.append(testCompilationUnit.getTypes().get(0).getName()).append(Consts.JAVA_SUFFIX);
         String testJavaFile = sb.toString();
         try {
             FileUtils.writeStringToFile(new File(testJavaFile), testCompilationUnit.toString(),
                     Charset.forName(Consts.DEFAULT_ENCODE));
         } catch (IOException e) {
             LOG.error(e.getMessage());
         }
         LOG.debug(testJavaFile);
         LOG.debug(testCompilationUnit.toString());
     }
 
    private static void ifExistDonnotOverride() {

    }
 }
