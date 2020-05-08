 package hudson.plugins.cpptest.parser;
 
 import hudson.plugins.analysis.core.AbstractAnnotationParser;
 import hudson.plugins.analysis.util.JavaPackageDetector;
 import hudson.plugins.analysis.util.model.FileAnnotation;
 import hudson.plugins.analysis.util.model.Priority;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.SAXException;
 
 /**
  * A parser for Cpptest XML files.
  *
  * @author Ulli Hafner
  * @author Aurelien Hebert
  * 
  * NQH: adapt for Cpptest
  */
 public class CpptestParser extends AbstractAnnotationParser {
     /** Unique identifier of this class. */
     private static final long serialVersionUID = -8705621867291182458L;
 
     /**
      * Creates a new instance of {@link CpptestParser}.
      */
     public CpptestParser() {
         super(StringUtils.EMPTY);
     }
 
     /**
      * Creates a new instance of {@link CpptestParser}.
      *
      * @param defaultEncoding
      *            the default encoding to be used when reading and parsing files
      */
     public CpptestParser(final String defaultEncoding) {
         super(defaultEncoding);
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<FileAnnotation> parse(final InputStream file, final String moduleName) throws InvocationTargetException {
         try {
             Digester digester = new Digester();
             digester.setValidating(false);
             digester.setClassLoader(CpptestParser.class.getClassLoader());
 
             String rootXPath = "ResultsSession";
             digester.addObjectCreate(rootXPath, Cpptest.class);
             digester.addSetProperties(rootXPath);
 
             String fileXPath = "ResultsSession/CodingStandards/StdViols/StdViol";
             digester.addObjectCreate(fileXPath, hudson.plugins.cpptest.parser.StdViol.class);
             digester.addSetProperties(fileXPath);
             digester.addSetNext(fileXPath, "addFile", hudson.plugins.cpptest.parser.StdViol.class.getName());
 
 	    //Change for update 0.10 : To use FlowViol
 	    fileXPath = "ResultsSession/CodingStandards/StdViols/FlowViol";
             digester.addObjectCreate(fileXPath, hudson.plugins.cpptest.parser.FlowViol.class);
             digester.addSetProperties(fileXPath);
             digester.addSetNext(fileXPath, "addFile", hudson.plugins.cpptest.parser.FlowViol.class.getName());
             
             //Change for the detection of Metrics Violations :
             fileXPath = "ResultsSession/CodingStandards/StdViols/MetViol";
             digester.addObjectCreate(fileXPath, hudson.plugins.cpptest.parser.MetViol.class);
             digester.addSetProperties(fileXPath);
             digester.addSetNext(fileXPath, "addFile", hudson.plugins.cpptest.parser.MetViol.class.getName());
 
             String ruleXPath = "ResultsSession/CodingStandards/Rules/RulesList/Rule";
             digester.addObjectCreate(ruleXPath, hudson.plugins.cpptest.parser.RuleDesc.class);
             digester.addSetProperties(ruleXPath);
             digester.addSetNext(ruleXPath, "addRuleDesc", hudson.plugins.cpptest.parser.RuleDesc.class.getName());
             
             String categoryXPath = "ResultsSession/CodingStandards/Rules/CategoriesList/Category";
             digester.addObjectCreate(categoryXPath, hudson.plugins.cpptest.parser.Category.class);
             digester.addSetProperties(categoryXPath);
             digester.addSetNext(categoryXPath, "addCategory", hudson.plugins.cpptest.parser.Category.class.getName());
     
             String locXPath = "ResultsSession/Locations/Loc";
             digester.addObjectCreate(locXPath, hudson.plugins.cpptest.parser.Location.class);
             digester.addSetProperties(locXPath);
             digester.addSetNext(locXPath, "addLocation", hudson.plugins.cpptest.parser.Location.class.getName());
             
             Cpptest module;
             module = (Cpptest)digester.parse(new InputStreamReader(file, "UTF-8"));
             if (module == null) {
                 throw new SAXException("Input stream is not a Cpptest file.");
             }
 
             return convert(module, moduleName);
         }
         catch (IOException exception) {
             throw new InvocationTargetException(exception);
         }
         catch (SAXException exception) {
             throw new InvocationTargetException(exception);
         }
     }
 
     /**
      * Converts the internal structure to the annotations API.
      *
      * @param collection
      *            the internal maven module
      * @param moduleName
      *            name of the maven module
      * @return a maven module of the annotations API
      */
     private Collection<FileAnnotation> convert(final Cpptest collection, final String moduleName) {
         ArrayList<FileAnnotation> annotations = new ArrayList<FileAnnotation>();
 
         for (hudson.plugins.cpptest.parser.StdViol viol : collection.getFiles()) {
            if (isValidWarning(viol)) {
                 String packageName = new JavaPackageDetector().detectPackageName(viol.getRule());
                 Priority priority;
                     if ("1".equalsIgnoreCase(viol.getSev())) {
                         priority = Priority.HIGH;
                     }
                     else if ("2".equalsIgnoreCase(viol.getSev())) {
                         priority = Priority.HIGH;
                     }
                     else if ("3".equalsIgnoreCase(viol.getSev())) {
                         priority = Priority.NORMAL;
                     }
                     else if ("4".equalsIgnoreCase(viol.getSev())) {
                         priority = Priority.NORMAL;
                     }
                     else if ("5".equalsIgnoreCase(viol.getSev())) {
                         priority = Priority.LOW;
                     }
                     else {
                         continue; // ignore
                     }
                     
                     String type = viol.getRule();
                     String category = viol.getCat();
                     
                     for (hudson.plugins.cpptest.parser.Category categ : collection.getCategories())
                     {
                     	if (categ.getName().equals(category)) 
                     	{
                     		category=categ.getDesc();break;
                     	}
                     }
                     
                     Warning warning = new Warning(priority, viol.getMsg(), StringUtils.capitalize(category),
                             type, viol.getLn(), viol.getLn());
                     
                     warning.setFileName(viol.getLocFile());
                     
                     for (hudson.plugins.cpptest.parser.RuleDesc rule : collection.getRuleDescs())
                     {
                     	if (rule.getId().equals(viol.getRule())) 
                     	{
                     		warning.setDesc(rule.getDesc());break;
                     	}
                     }
 
                     for (hudson.plugins.cpptest.parser.Location loc : collection.getLocations())
                     {
                     	if (loc.getLoc().equals(viol.getLocFile()))
                     	{
                     		warning.setFileName(loc.getFsPath());break;
                     	}
                     }
                                         
                     //TODO: module and package settings need to be modify to work properly for C++Test purpose
                     warning.setModuleName(moduleName);                    
                     warning.setPackageName(packageName);
 
                     try {
                         warning.setContextHashCode(createContextHashCode(viol.getRule(), viol.getLn()));
                     }
                     catch (IOException exception) {
                         // ignore and continue
                     }
                     annotations.add(warning);
                 
             }
         }
         return annotations;
     }
 
     /**
      * Returns <code>true</code> if this warning is valid or <code>false</code>
      * if the warning can't be processed by the Cpptest plug-in.
      *
      * @param file the file to check
      * @return <code>true</code> if this warning is valid
      */
     private boolean isValidWarning(final hudson.plugins.cpptest.parser.StdViol viol) {
         return !viol.getRule().endsWith("package.html");
     }
 }
 
