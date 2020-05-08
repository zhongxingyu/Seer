 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.gen;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.dom.DOMSource;
 
 import org.w3c.dom.Document;
 
 import org.znerd.logdoc.LogDef;
 
 /**
  * Code generator.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class CodeGenerator {
 
    /**
     * Creates a new <code>CodeGenerator</code> for the specified log 
     * definition.
     *
     * @param def
     *    the {@link LogDef} object, cannot be <code>null</code>.
     *
     * @param target
     *    the target to generate code for, e.g. <code>"log4j"</code> or
     *    <code>"slf4j"</code>, cannot be <code>null</code>.
     *
     * @param targetDir
     *    the target directory to create the Java source files in,
     *    cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>def == null || target == null || targetDir == null</code>.
     */
    public CodeGenerator(LogDef def, String target, File targetDir)
    throws IllegalArgumentException {
       if (def == null) {
          throw new IllegalArgumentException("def == null");
       } else if (target == null) {
          throw new IllegalArgumentException("target == null");
       } else if (targetDir == null) {
          throw new IllegalArgumentException("targetDir == null");
       }
 
       _def       = def;
       _target    = target;
       _targetDir = targetDir;
    }
 
    private final LogDef _def;
    private final String _target;
    private final File _targetDir;
 
    /**
     * Generates the Java code.
     *
     * @throws IOException
     *    if the Java code could not be generated because of an I/O error.
     */
    public void generateCode() throws IOException {
 
      transformToJava(_target + '/', "Log");
      transformToJava("",            "TranslationBundle");
       for (Map.Entry<String,Document> entry : _def.getTranslations().entrySet()) {
          String           locale = entry.getKey();
          Document translationXML = entry.getValue();
         transformToJavaForLocale(locale, translationXML);
       }
    }
 
   private void transformToJava(String xsltSubDir, String className)
    throws IOException {
 
       Source      source = new DOMSource(_def.getXML());
       String    xsltPath = xsltSubDir + "log_to_" + className + "_java" + ".xslt";
      String  domainPath = _def.getDomainName().replace(".", "/");
      File        outDir = new File(_targetDir, domainPath);
       String outFileName = className + ".java";
 
       Map<String,String> xsltParams = new HashMap<String,String>();
       xsltParams.put("package_name", _def.getDomainName());
       xsltParams.put("accesslevel",  _def.isPublic() ? "public" : "protected");
 
       new Xformer(_def).transform(source, xsltPath, xsltParams, outDir, outFileName);
    }
 
   private void transformToJavaForLocale(String locale, Document translationXML)
    throws IOException {
 
       Source      source = new DOMSource(translationXML);
       String    xsltPath = "translation-bundle_to_java.xslt";
       String outFileName = "TranslationBundle_" + locale + ".java";
 
       Map<String,String> xsltParams = new HashMap<String,String>();
       xsltParams.put("package_name", _def.getDomainName());
       xsltParams.put("accesslevel",  _def.isPublic() ? "public" : "protected");
       xsltParams.put("locale",       locale);
 
      new Xformer(_def).transform(source, xsltPath, xsltParams, _targetDir, outFileName);
    }
 }
