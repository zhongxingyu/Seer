 /**
  * (C) 2009 jolira (http://www.jolira.com). Licensed under the GNU General Public License, Version 3.0 (the "License");
  * you may not use this file except in compliance with the License. You may obtain a copy of the License at
  * http://www.gnu.org/licenses/gpl-3.0-standalone.html Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the specific language governing permissions and limitations
  * under the License.
  */
 
 package com.google.code.joliratools.bind.apt;
 
 import static com.google.code.joliratools.bind.apt.SelectedProcessType.ADAPTERS_ONLY;
 import static com.google.code.joliratools.bind.apt.SelectedProcessType.COMPLETE;
 import static com.google.code.joliratools.bind.apt.SelectedProcessType.NONE;
 import static com.google.code.joliratools.bind.apt.SelectedProcessType.SCHEMA_ONLY;
 import static javax.lang.model.SourceVersion.RELEASE_6;
 import static javax.tools.Diagnostic.Kind.ERROR;
 import static javax.tools.Diagnostic.Kind.WARNING;
 import static javax.tools.StandardLocation.SOURCE_OUTPUT;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Filer;
 import javax.annotation.processing.Messager;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedOptions;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.tools.FileObject;
 
 import com.google.code.joliratools.bind.annotation.RoRootElement;
 import com.google.code.joliratools.bind.annotation.RoTransient;
 import com.google.code.joliratools.bind.annotation.RoType;
 import com.google.code.joliratools.bind.generate.JSONAdapterClassGenerator;
 import com.google.code.joliratools.bind.generate.SchemaGenerator;
 import com.google.code.joliratools.bind.generate.XMLAdapterClassGenerator;
 import com.google.code.joliratools.bind.model.Class;
 import com.google.code.joliratools.bind.schema.Schema;
 
 /**
  * The JAX-Read-Only process generates a schema and XML adapters used for
  * serialization from a small set of annotations. The goal is to enable users
  * that have an existing code-base to quickly generate XML-bases services (using
  * SOAP or REST).
  * 
  * @see "http://code.google.com/p/jolira-tools/wiki/jaxro"
  * @see RoRootElement
  * @see RoType
  * @see RoTransient
  * @author Joachim F. Kainz
  */
 @SupportedSourceVersion(RELEASE_6)
 @SupportedAnnotationTypes("com.google.code.joliratools.bind.annotation.*")
 @SupportedOptions( { "dense", "schema", "adapters", "jaxroproc",
     "jaxrodisabled", "jsonstringonly" })
 public class JAXROProcessor extends AbstractProcessor {
     private static final Logger LOG = Logger.getLogger(JAXROProcessor.class
             .getName());
     private static final String DEFAULT_XSD_FILENAME = "jaxro.xsd";
 
     private static PrintWriter createFile(final String schema)
             throws IOException {
         final File file = new File(schema);
 
         makeParentDirs(file);
 
         return new PrintWriter(file);
     }
 
     private static PrintWriter createSourceFile(final String adaptersDir,
             final String classname) throws IOException {
         final File dir = new File(adaptersDir);
         final String relativeFileName = classname.replace('.', '/') + ".java";
         final File file = new File(dir, relativeFileName);
 
         makeParentDirs(file);
 
         return new PrintWriter(file);
     }
 
     private static void makeParentDirs(final File _file) throws IOException,
     Error {
         final File file = _file.getCanonicalFile();
         final File parentFile = file.getParentFile();
 
         if (parentFile == null) {
             throw new Error("Not directory found for file " + file);
         }
 
         final boolean success = parentFile.mkdirs();
 
         LOG.fine("mkdirs returned " + success);
     }
 
     /**
      * @param classname
      * @return the print writer to write the new source file
      */
     protected PrintWriter createSourceFile(final String classname) {
         final String adaptersDir = getOption("adapters");
 
         if (adaptersDir != null && !adaptersDir.isEmpty()) {
             try {
                 return createSourceFile(adaptersDir, classname);
             } catch (final IOException e) {
                 throw new Error(e);
             }
         }
 
         final Filer filer = processingEnv.getFiler();
 
         try {
             final FileObject file = filer.createSourceFile(classname);
             final Writer writer = file.openWriter();
 
             return new PrintWriter(writer);
         } catch (final IOException e) {
             throw new Error(e);
         }
     }
 
     /**
      * @param pkg
      * @param filename
      * @return a new text file
      */
     protected PrintWriter createTextFile(final String pkg, final String filename) {
         final String schema = getOption("schema");
 
         if (schema != null && !schema.isEmpty()) {
             try {
                 return createFile(schema);
             } catch (final IOException e) {
                 throw new Error(e);
             }
         }
 
         final Filer filer = processingEnv.getFiler();
 
         try {
             final FileObject file = filer.createResource(SOURCE_OUTPUT, pkg,
                     filename);
             final Writer writer = file.openWriter();
 
             return new PrintWriter(writer);
         } catch (final IOException e) {
            throw new Error("error creating text file for '" + pkg + "' and '" + filename + "'", e);
         }
     }
 
     private void generateAdapterClasses(final Schema schema) {
         // FIXME: need to specify adapters to run.
 
         final XMLAdapterClassGenerator generator = new XMLAdapterClassGenerator(
                 schema) {
             @Override
             protected PrintWriter createSourceFile(final String classname) {
                 return JAXROProcessor.this.createSourceFile(classname);
             }
         };
 
         generator.generate();
 
         final String jsonstringonly = getOption("jsonstringonly");
         final boolean stringOnly = jsonstringonly != null && Boolean.parseBoolean(jsonstringonly);
         final JSONAdapterClassGenerator generatorJSON = new JSONAdapterClassGenerator(schema, stringOnly) {
             @Override
             protected PrintWriter createSourceFile(final String classname) {
                 return JAXROProcessor.this.createSourceFile(classname);
             }
         };
 
         generatorJSON.generate();
     }
 
     private void generateSchema(final Schema schema) {
         final SchemaGenerator generator = new SchemaGenerator(schema) {
             @Override
             protected PrintWriter createTextFile(final String pkg) {
                 return JAXROProcessor.this.createTextFile(pkg,
                         DEFAULT_XSD_FILENAME);
             }
         };
 
         generator.generate();
     }
 
     private String getOption(final String option) {
         final Map<String, String> options = processingEnv.getOptions();
 
         return options.get(option);
     }
 
     private Collection<Element> getRoots(final RoundEnvironment env) {
         final Set<? extends Element> roTypes = env
                 .getElementsAnnotatedWith(RoType.class);
         final Set<? extends Element> roElements = env
                 .getElementsAnnotatedWith(RoRootElement.class);
         final Collection<Element> roots = new HashSet<Element>();
 
         roots.addAll(roTypes);
         roots.addAll(roElements);
 
         return roots;
     }
 
     private Schema getSchema(final RoundEnvironment env) {
         final AdaptorFactory factory = new AdaptorFactoryImpl();
         final boolean isDense = isDense();
         final Collection<Element> roots = getRoots(env);
         final Class[] classes = getSpecifiedClassDeclarations(factory, roots);
 
         if (classes == null || classes.length < 1) {
             return null;
         }
 
         return new Schema(
                 new com.google.code.joliratools.bind.schema.Messager() {
                     @Override
                     public void warning(final Class clazz, final String message) {
                         printWarning(clazz.toString() + ": " + message);
                     }
                 }, classes, isDense);
     }
 
     private SelectedProcessType getSelectedProcessType() {
         final String disabled = getOption("jaxrodisabled");
 
         if (Boolean.parseBoolean(disabled)) {
             return NONE;
         }
 
         final String proc = getOption("jaxroproc");
 
         if (proc == null || proc.isEmpty()) {
             return COMPLETE;
         }
 
         if ("none".equalsIgnoreCase(proc)) {
             return NONE;
         }
 
         if ("complete".equalsIgnoreCase(proc)) {
             return COMPLETE;
         }
 
         if ("schemaonly".equalsIgnoreCase(proc)) {
             return SCHEMA_ONLY;
         }
 
         if ("adaptersonly".equalsIgnoreCase(proc)) {
             return ADAPTERS_ONLY;
         }
 
         printError("not a valid value for jaxprox: " + proc);
 
         return NONE;
     }
 
     private Class[] getSpecifiedClassDeclarations(final AdaptorFactory factory,
             final Collection<? extends Element> roots) {
         final int rootSize = roots.size();
         final Collection<Class> adapters = new ArrayList<Class>(rootSize);
 
         for (final Element e : roots) {
             final Class adapted = factory.getAdapter(e);
 
             if (adapted != null) {
                 adapters.add(adapted);
             }
         }
 
         final int size = adapters.size();
 
         if (rootSize > 1 && size < 1) {
             printError("no appropriate annotation found");
         }
 
         return adapters.toArray(new Class[size]);
     }
 
     private boolean isDense() {
         final String dense = getOption("dense");
 
         return dense != null && Boolean.parseBoolean(dense);
     }
 
     private void printError(final String message) {
         final Messager messager = processingEnv.getMessager();
 
         messager.printMessage(ERROR, message);
     }
 
     /**
      * Print an error message.
      * 
      * @param message
      *            the error message
      */
     protected void printWarning(final String message) {
         final Messager messager = processingEnv.getMessager();
 
         messager.printMessage(WARNING, message);
     }
 
     @Override
     public boolean process(final Set<? extends TypeElement> annotations,
             final RoundEnvironment env) {
         final SelectedProcessType type = getSelectedProcessType();
 
         if (NONE.equals(type)) {
             return true;
         }
 
         final Schema schema = getSchema(env);
 
         if (schema == null) {
             return true;
         }
 
         if (!ADAPTERS_ONLY.equals(type)) {
             generateSchema(schema);
         }
 
         if (!SCHEMA_ONLY.equals(type)) {
             generateAdapterClasses(schema);
         }
 
         return true;
     }
 }
