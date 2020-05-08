 package org.eglipse.lang.glsl.generator;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.xtend2.lib.StringConcatenation;
 import org.eclipse.xtext.generator.IFileSystemAccess;
 import org.eclipse.xtext.generator.IGenerator;
 import org.eclipse.xtext.xbase.lib.BooleanExtensions;
 import org.eclipse.xtext.xbase.lib.InputOutput;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.StringExtensions;
 import org.eglipse.lang.glsl.glsl.SourceFile;
 
 @SuppressWarnings("all")
 public class GlslGenerator implements IGenerator {
   public void doGenerate(final Resource resource, final IFileSystemAccess fsa) {
       InputOutput.<String>println("FUCK FUCK FUCK");
       URI _uRI = resource.getURI();
       String _javaFileName = this.getJavaFileName(_uRI);
       CharSequence _generateAccessor = this.generateAccessor(resource);
       fsa.generateFile(_javaFileName, _generateAccessor);
   }
   
   protected CharSequence _generateAccessor(final Resource resource) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("package ");
     URI _uRI = resource.getURI();
     String _package = this.getPackage(_uRI);
     _builder.append(_package, "");
     _builder.append(";");
     _builder.newLineIfNotEmpty();
     _builder.newLine();
    _builder.append("import java.net.URI;");
     _builder.newLine();
     _builder.append("import java.io.InputStream;");
     _builder.newLine();
     _builder.newLine();
     _builder.append("public class ");
     URI _uRI_1 = resource.getURI();
     String _javaName = this.getJavaName(_uRI_1);
     _builder.append(_javaName, "");
     _builder.append(" {");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("public static InputStream asInputStream() {");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("return ");
     URI _uRI_2 = resource.getURI();
     String _javaName_1 = this.getJavaName(_uRI_2);
     _builder.append(_javaName_1, "		");
     _builder.append(".class.getClassLoader().getResourceAsStream(\"");
     URI _uRI_3 = resource.getURI();
     String _fileName = this.getFileName(_uRI_3);
     _builder.append(_fileName, "		");
     _builder.append("\");");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("}");
     _builder.newLine();
     _builder.append("\t");
    _builder.append("public static URI asURI() {");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("return ");
     URI _uRI_4 = resource.getURI();
     String _javaName_2 = this.getJavaName(_uRI_4);
     _builder.append(_javaName_2, "		");
     _builder.append(".class.getClassLoader().getResource(\"");
     URI _uRI_5 = resource.getURI();
     String _fileName_1 = this.getFileName(_uRI_5);
     _builder.append(_fileName_1, "		");
     _builder.append("\");");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("}");
     _builder.newLine();
     _builder.append("\t");
     _builder.newLine();
     _builder.append("}");
     _builder.newLine();
     return _builder;
   }
   
   protected CharSequence _generateContent(final Resource resource) {
     CharSequence _xblockexpression = null;
     {
       EList<EObject> _contents = resource.getContents();
       Iterable<SourceFile> _filter = IterableExtensions.<SourceFile>filter(_contents, org.eglipse.lang.glsl.glsl.SourceFile.class);
       SourceFile _head = IterableExtensions.<SourceFile>head(_filter);
       final SourceFile sourceFile = _head;
       StringConcatenation _builder = new StringConcatenation();
       {
         URI _uRI = resource.getURI();
         String _package = this.getPackage(_uRI);
         boolean _equals = _package.equals("");
         boolean _operator_not = BooleanExtensions.operator_not(_equals);
         if (_operator_not) {
           _builder.append("package ");
           URI _uRI_1 = resource.getURI();
           String _package_1 = this.getPackage(_uRI_1);
           _builder.append(_package_1, "");
           _builder.append(";");
           _builder.newLineIfNotEmpty();
           _builder.newLine();
         }
       }
       _builder.append("public class ");
       URI _uRI_2 = resource.getURI();
       String _javaName = this.getJavaName(_uRI_2);
       _builder.append(_javaName, "");
       _builder.append(" {");
       _builder.newLineIfNotEmpty();
       _builder.append("\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private int shaderId;");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private String file = ");
       URI _uRI_3 = resource.getURI();
       _builder.append(_uRI_3, "	");
       _builder.newLineIfNotEmpty();
       _builder.append("\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("public ");
       URI _uRI_4 = resource.getURI();
       String _javaName_1 = this.getJavaName(_uRI_4);
       _builder.append(_javaName_1, "	");
       _builder.append("() {");
       _builder.newLineIfNotEmpty();
       _builder.append("\t\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private void createShader() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.append("shaderId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("private void loadShader() {");
       _builder.newLine();
       _builder.append("\t\t");
       _builder.newLine();
       _builder.append("\t");
       _builder.append("}");
       _builder.newLine();
       _builder.append("}");
       _builder.newLine();
       _xblockexpression = (_builder);
     }
     return _xblockexpression;
   }
   
   public String getFileName(final URI uri) {
     String _package = this.getPackage(uri);
     String _replace = _package.replace(".", "/");
    String _operator_plus = StringExtensions.operator_plus(_replace, "/");
     String _lastSegment = uri.lastSegment();
    String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _lastSegment);
     return _operator_plus_1;
   }
   
   public String getJavaFileName(final URI uri) {
     String _javaNameWithPackage = this.getJavaNameWithPackage(uri);
     String _replace = _javaNameWithPackage.replace(".", "/");
    String _operator_plus = StringExtensions.operator_plus(_replace, ".java");
     return _operator_plus;
   }
   
   public String getJavaName(final URI uri) {
     String _lastSegment = uri.lastSegment();
     String _replaceAll = _lastSegment.replaceAll(".glsl", "");
    String _operator_plus = StringExtensions.operator_plus(_replaceAll, "Shader");
     return _operator_plus;
   }
   
   public String getJavaNameWithPackage(final URI uri) {
     String _package = this.getPackage(uri);
    String _operator_plus = StringExtensions.operator_plus(_package, ".");
     String _javaName = this.getJavaName(uri);
    String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _javaName);
     return _operator_plus_1;
   }
   
   public String getSrcRelativePath(final URI uri) {
     String _path = uri.path();
     String _replaceAll = _path.replaceAll("/resource/[^/]+/src", "");
     return _replaceAll;
   }
   
   public String getPackage(final URI uri) {
     String _srcRelativePath = this.getSrcRelativePath(uri);
     String _replaceAll = _srcRelativePath.replaceAll("/[^/]+$", "");
     String _replaceAll_1 = _replaceAll.replaceAll("/", ".");
     String _replaceFirst = _replaceAll_1.replaceFirst(".", "");
     return _replaceFirst;
   }
   
   public CharSequence generateAccessor(final Resource resource) {
     {
       return _generateAccessor(resource);
     }
   }
   
   public CharSequence generateContent(final Resource resource) {
     {
       return _generateContent(resource);
     }
   }
 }
