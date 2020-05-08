 package cn.shenyanchao.ut;
 
 import cn.shenyanchao.builder.ClassTypeBuilder;
 import cn.shenyanchao.builder.CompilationUnitBuilder;
 import cn.shenyanchao.common.Consts;
 import cn.shenyanchao.common.FileComments;
 import cn.shenyanchao.filter.JavaFileFilter;
 import cn.shenyanchao.generator.TestGenerator;
 import cn.shenyanchao.utils.JavaParserFactory;
 import cn.shenyanchao.utils.MembersFilter;
 import cn.shenyanchao.utils.PackageUtils;
 import japa.parser.ast.CompilationUnit;
 import japa.parser.ast.PackageDeclaration;
 import japa.parser.ast.body.MethodDeclaration;
 import japa.parser.ast.body.TypeDeclaration;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author shenyanchao
  *         Date:  6/13/13
  *         Time:  5:13 PM
  */
 @Mojo(name = "source2test", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
 public class AstGenerator extends AbstractMojo {
 
     @Parameter(defaultValue = Consts.DEFAULT_ENCODE, property = "sourceEncode", required = false)
     private String sourceEncode;
 
     @Parameter(defaultValue = "${project.build.sourceDirectory}", property = "sourceDir", required = false)
     private String sourceDir;
 
     @Parameter(defaultValue = "${project.build.testSourceDirectory}", property = "testDir", required = false)
     private String testDir;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         getLog().info("############################################################");
         getLog().info(sourceDir);
         getLog().info(testDir);
         getLog().info("############################################################");
 
         File sourceDirectory = new File(sourceDir);
         File testDirectory = new File(testDir);
         makeDirIfNotExist(sourceDirectory);
         makeDirIfNotExist(testDirectory);
 
         Iterator<File> fileItr = FileUtils.iterateFiles(sourceDirectory, new JavaFileFilter(), TrueFileFilter.INSTANCE);
         while (fileItr.hasNext()) {
             File javaFile = fileItr.next();
             getLog().info("start process file:" + javaFile.getAbsolutePath());
             //todo
             //process java file
             convertJavaFile2Test(javaFile);
         }
     }
 
 
     private void makeDirIfNotExist(File dir) {
         if (!dir.isDirectory()) {
             getLog().error(dir.getAbsolutePath() + "is not a directory!");
         }
         if (!dir.exists()) {
             boolean success = false;
             while (!success) {
                 success = dir.mkdirs();
             }
         }
     }
 
 
     private void convertJavaFile2Test(File javaFile) {
 
         CompilationUnit compilationUnit = JavaParserFactory.getCompilationUnit(javaFile, sourceEncode);
 
         CompilationUnitBuilder compilationUnitBuilder = new CompilationUnitBuilder();
         compilationUnitBuilder.buildComment(FileComments.GENERATOR_COMMENT);
         //process package
         PackageDeclaration packageDeclaration = compilationUnit.getPackage();
         String testPackageName = PackageUtils.getTestPackageNameFrom(packageDeclaration);
         compilationUnitBuilder.buildPackage(testPackageName);
         //process import
 
         //process type
         List typeList = compilationUnit.getTypes();
         for (Object type : typeList) {
             TypeDeclaration typeDeclaration = (TypeDeclaration) type;
            String className = typeDeclaration.getName().toString();
             getLog().info("className:" + className);
             ClassTypeBuilder classTypeBuilder = new ClassTypeBuilder(className + Consts.TEST_SUFFIX);
             //process methods
             List<MethodDeclaration> methodDeclarations = MembersFilter.findMethodsFrom(typeDeclaration);
             for (MethodDeclaration methodDeclaration : methodDeclarations) {
                 //has method and add import
                 compilationUnitBuilder.buildImports(null);
 
                 String methodName = methodDeclaration.getName();
                 getLog().info("methodName:" + methodDeclaration.getName());
                 classTypeBuilder.buildMethod(methodName + Consts.TEST_SUFFIX, methodDeclaration);
             }
 
             compilationUnitBuilder.buildClass(classTypeBuilder.build());
         }
 
         CompilationUnit testCompilationUnit = compilationUnitBuilder.build();
 
         //写入测试代码文件
         TestGenerator.writeJavaTest(testDir, testCompilationUnit);
     }
 
 }
