 package de.lightful.testing.xslt;
 
 import net.sf.saxon.TransformerFactoryImpl;
 
 import javax.xml.transform.*;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Formatter;
 
 import static de.lightful.testing.xslt.ResourceResolverUtil.resolveResource;
 
 public abstract class SaxonXsltTestBase implements IHookable {
 
   public static final String TRANSFORMER_FACTORY_KEY = "javax.xml.transform.TransformerFactory";
 
   private Templates stylesheet;
 
   protected Templates getStylesheet() {
     return stylesheet;
   }
 
  @Override
   public void run(IHookCallBack iHookCallBack, ITestResult iTestResult) {
     final StylesheetPerTestCase stylesheetPerTestCase = getEffectiveAnnotation(iTestResult, StylesheetPerTestCase.class);
     final Stylesheet stylesheet = getEffectiveAnnotation(iTestResult, Stylesheet.class);
     ensureStylesheetDefinitionUnambiguous(stylesheet, stylesheetPerTestCase);
 
     final ResourceBasePaths resourceBasePaths = getEffectiveAnnotation(iTestResult, ResourceBasePaths.class);
     ensureResourceBasePathsGivenIfRequired(stylesheetPerTestCase, stylesheet, resourceBasePaths);
 
     final String oldTransformerFactory = System.getProperty(TRANSFORMER_FACTORY_KEY);
     try {
       System.setProperty(TRANSFORMER_FACTORY_KEY, TransformerFactoryImpl.class.getName());
       final TransformerFactory factory = createTransformerFactory(resourceBasePaths);
       if (useStylesheetFromResource(stylesheet)) {
         this.stylesheet = loadStylesheetFromResource(resourceBasePaths.value(), factory, stylesheet.resource());
       }
       else if (useStylesheetPerTestCase(stylesheetPerTestCase)) {
         this.stylesheet = loadStylesheetPerTestCase(stylesheetPerTestCase, factory);
       }
       else {
         this.stylesheet = loadStylesheetFromLiteral(stylesheet, factory);
       }
       iHookCallBack.runTestMethod(iTestResult);
     }
     finally {
       resetTransformerFactory(oldTransformerFactory);
     }
   }
 
   private TransformerFactory createTransformerFactory(ResourceBasePaths resourceBasePaths) {
     final TransformerFactory factory = TransformerFactory.newInstance();
     if (resourceBasePaths != null) {
       factory.setURIResolver(new BaseResourceUriResolver(resourceBasePaths.value()));
     }
     return factory;
   }
 
   private void resetTransformerFactory(String oldTransformerFactory) {
     if (oldTransformerFactory != null) {
       System.setProperty(TRANSFORMER_FACTORY_KEY, oldTransformerFactory);
     }
     else {
       System.clearProperty(TRANSFORMER_FACTORY_KEY);
     }
   }
 
   private Templates loadStylesheetPerTestCase(StylesheetPerTestCase stylesheetPerTestCase, TransformerFactory factory) {
     String stylesheetText = createStylesheetText(stylesheetPerTestCase);
     try {
       Source transformationSource = createSourceFromString(stylesheetText);
       return factory.newTemplates(transformationSource);
     }
     catch (TransformerConfigurationException e) {
       throw new TestNGException("Error compiling stylesheet template", e);
     }
   }
 
   private String createStylesheetText(StylesheetPerTestCase stylesheetPerTestCase) {
     String indent4 = indent(4);
     StringBuilder builder = new StringBuilder();
     for (ImportStylesheet importStylesheet : stylesheetPerTestCase.imports()) {
       builder.append(indent4).append("<xsl:import href='").append(importStylesheet.value()).append("'/>\n");
     }
     return PER_TEST_CASE_XSLT_TEMPLATE
         .replace("@@IMPORTS@@", builder.toString())
         .replace("@@XSLT_SNIPPLET@@", stylesheetPerTestCase.xsltSnippet());
   }
 
   private String indent(int i) {
     Formatter formatter = new Formatter();
     formatter.format("%" + i + "s", " ");
     return formatter.out().toString();
   }
 
   private final static String PER_TEST_CASE_XSLT_TEMPLATE =
       "<?xml version='1.0' encoding='UTF-8'?>" +
       "<xsl:stylesheet version='2.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'" +
       "  xmlns:em='http://www.europace.ie/xsd/'> " +
       "  @@IMPORTS@@" +
       "  <xsl:output method='xml' version='1.0' encoding='UTF-8' indent='yes'/>" +
       "  <xsl:template match='/tests'>" +
       "    <xsl:element name='results'>" +
       "      <xsl:for-each select='testcase'>" +
       "        <xsl:element name='result'>" +
       "          @@XSLT_SNIPPLET@@" +
       "        </xsl:element>" +
       "      </xsl:for-each>" +
       "    </xsl:element>" +
       "  </xsl:template>" +
       "</xsl:stylesheet>";
 
   private void ensureResourceBasePathsGivenIfRequired(StylesheetPerTestCase stylesheetPerTestCase, Stylesheet stylesheet, ResourceBasePaths resourceBasePaths) {
     if (needsResourceBasePaths(stylesheetPerTestCase, stylesheet) && resourceBasePaths == null) {
       throw new TestNGException("Test " + getClass().getSimpleName() + " must use @" + ResourceBasePaths.class.getSimpleName() + " to declare at least one test resource base path.");
     }
   }
 
   private void ensureStylesheetDefinitionUnambiguous(Stylesheet stylesheet, StylesheetPerTestCase stylesheetPerTestCase) {
     if (stylesheet != null && stylesheetPerTestCase != null) {
       throw new TestNGException("Test " + getClass().getSimpleName() + " must cannot use both @" + Stylesheet.class.getSimpleName()
                                 + " and @" + StylesheetPerTestCase.class.getSimpleName() + " to define stylesheet. Please choose " +
                                 "exactly one of them.");
     }
     if (stylesheet == null && stylesheetPerTestCase == null) {
       throw new TestNGException("Test " + getClass().getSimpleName() + ": missing stylesheet definition. Please specify one of @"
                                 + Stylesheet.class.getSimpleName() + " and @" + StylesheetPerTestCase.class.getSimpleName() + " " +
                                 "to define stylesheet.");
     }
   }
 
   private boolean needsResourceBasePaths(StylesheetPerTestCase stylesheetPerTestCase, Stylesheet stylesheet) {
     return (useStylesheetFromResource(stylesheet) || useImportsPerTestCase(stylesheetPerTestCase));
   }
 
   private boolean useStylesheetPerTestCase(StylesheetPerTestCase stylesheetPerTestCase) {
     return (stylesheetPerTestCase != null);
   }
 
   private boolean useImportsPerTestCase(StylesheetPerTestCase stylesheetPerTestCase) {
     return stylesheetPerTestCase != null && (stylesheetPerTestCase.imports().length > 0);
   }
 
   private boolean useStylesheetFromResource(Stylesheet stylesheet) {
     return stylesheet != null && !stylesheet.resource().isEmpty();
   }
 
   private Templates loadStylesheetFromLiteral(Stylesheet stylesheetAnnotation, TransformerFactory factory) {
     if (stylesheetAnnotation.literal().isEmpty()) {
       throw new TestNGException("Test " + getClass().getSimpleName() + " must use @" + Stylesheet.class.getSimpleName() + ".literal to define stylesheet to use, since no stylesheet resource is given");
     }
     try {
       Source transformationSource = createSourceFromString(stylesheetAnnotation.literal());
       return factory.newTemplates(transformationSource);
     }
     catch (TransformerConfigurationException e) {
       throw new TestNGException("Error compiling stylesheet template", e);
     }
   }
 
   private Templates loadStylesheetFromResource(String[] resourceBasePaths, TransformerFactory transformerFactory, String stylesheetName) {
     try {
       return createTemplates(stylesheetName, resourceBasePaths, transformerFactory);
     }
     catch (TransformerConfigurationException e) {
       throw new TestNGException("Cannot load stylesheet template", e);
     }
   }
 
   private <ANNOTATION_TYPE extends Annotation> ANNOTATION_TYPE getEffectiveAnnotation(ITestResult iTestResult, Class<? extends ANNOTATION_TYPE> annotationClass) {
     final Method testMethod = iTestResult.getMethod().getMethod();
 
     final ANNOTATION_TYPE annotationOnTestMethod = testMethod.getAnnotation(annotationClass);
     if (annotationOnTestMethod != null) {
       return annotationOnTestMethod;
     }
 
     final Class<?> testClass = testMethod.getDeclaringClass();
     return testClass.getAnnotation(annotationClass);
   }
 
   protected static Source createSourceFromString(String inputXml) {
     StringReader inputStringReader = new StringReader(inputXml);
     return new StreamSource(inputStringReader);
   }
 
   protected static Source createSourceFromFile(File file) throws IOException {
     FileReader inputStringReader = new FileReader(file);
     return new StreamSource(inputStringReader);
   }
 
   protected Source createSourceFromResourceUrl(URL resourcePath) throws IOException {
     InputStream inputStream = resourcePath.openStream();
     Assert.assertNotNull(inputStream, "Resource named " + resourcePath + " not found.");
     return new StreamSource(inputStream);
   }
 
   protected StylesheetRunner runStylesheet(Templates stylesheet) throws TransformerException {
     return new StylesheetRunner(stylesheet);
   }
 
   private Templates createTemplates(final String transformationFileName, String[] resourceBasePath, TransformerFactory factory) throws TransformerConfigurationException {
     final URL resourceUrl = resolveResource(transformationFileName, "", resourceBasePath);
     if (resourceUrl == null) { return null; }
 
     try {
       Source transformationSource = createSourceFromResourceUrl(resourceUrl);
       return factory.newTemplates(transformationSource);
     }
     catch (IOException e) {
       throw new TransformerConfigurationException("Cannot open transformation " + transformationFileName, e);
     }
   }
 
   protected String xmlTests(String... testCases) {
 
     StringBuilder builder = new StringBuilder();
     builder.append("<tests>");
     for (String testCase : testCases) {
       builder.append(testCase);
     }
     builder.append("</tests>");
     return builder.toString();
   }
 
   protected String xmlTestCase(String testCaseContent) {
     return "<testcase>" + testCaseContent + "</testcase>";
   }
 }
