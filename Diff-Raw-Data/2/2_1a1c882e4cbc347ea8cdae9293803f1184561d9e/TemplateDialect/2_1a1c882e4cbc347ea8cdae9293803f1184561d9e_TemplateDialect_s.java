 /*
  * Copyright 2012 Robert W. Vawter III <bob@vawter.org>
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jsonddl.generator;
 
 import static org.jsonddl.generator.industrial.IndustrialDialect.getterName;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.jsonddl.model.Kind;
 import org.jsonddl.model.Model;
 import org.jsonddl.model.ModelVisitor;
 import org.jsonddl.model.Property;
 import org.jsonddl.model.Schema;
 import org.jsonddl.model.Type;
 import org.stringtemplate.v4.AttributeRenderer;
 import org.stringtemplate.v4.AutoIndentWriter;
 import org.stringtemplate.v4.Interpreter;
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.STGroupFile;
 import org.stringtemplate.v4.misc.ObjectModelAdaptor;
 import org.stringtemplate.v4.misc.STNoSuchPropertyException;
 
 /**
  * A utility base class for generators that use StringTemplate.
  * <p>
  * Defines:
  * <ul>
  * <li>A {@value #NOW_TEMPLATE_NAME} template with a stable timestamp.</li>
  * <li>A {@value #NAMES_DICTIONARY_NAME} dictionary with class names returned from
  * {@link #getTemplateClasses()}.</li>
  * </ul>
  */
 public abstract class TemplateDialect implements Dialect {
   private static final String DIALECT_PROPERTIES_KEY = "dialectProperties";
   private static final String GENERATED_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
   private static final int LINE_WIDTH = 80;
   private static final String MODEL_KEY = "model";
   private static final String NAMES_DICTIONARY_NAME = "names";
   private static final String NOW_TEMPLATE_NAME = "now";
   private STGroup templates;
 
   @Override
   public void generate(Options options, Collector output, Schema s) throws IOException {
     templates = loadTemplateGroup();
     // Redefine now()
     templates.undefineTemplate(NOW_TEMPLATE_NAME);
     templates.defineTemplate(NOW_TEMPLATE_NAME,
         new SimpleDateFormat(GENERATED_DATE_FORMAT).format(new Date()));
 
     // Provide the collection of well-known classes to the template
     Map<String, Object> classMap = new HashMap<String, Object>();
     for (Class<?> clazz : getTemplateClasses()) {
       classMap.put(clazz.getSimpleName(), clazz.getCanonicalName());
     }
     for (String name : getTemplateClassNames()) {
       int idx = name.lastIndexOf('.');
       if (idx == -1) {
         classMap.put(name, name);
       } else {
         classMap.put(name.substring(idx + 1), name);
       }
     }
    classMap.put(Dialect.class.getCanonicalName(), getClass().getCanonicalName());
     templates.defineDictionary(NAMES_DICTIONARY_NAME, classMap);
 
     // Stringifies a Type as its parameterized, qualified source name
     templates.registerRenderer(Type.class, new AttributeRenderer() {
       @Override
       public String toString(Object o, String formatString, Locale locale) {
         return TypeAnswers.getParameterizedQualifiedSourceName((Type) o);
       }
     });
 
     // Add a magic "getterName" property to Property objects for use by the templates
     templates.registerModelAdaptor(Property.class, new ObjectModelAdaptor() {
       @Override
       public Object getProperty(Interpreter interp, ST self, Object o, Object property,
           String propertyName) throws STNoSuchPropertyException {
         if ("getterName".equals(propertyName)) {
           return getterName(((Property) o).getName());
         }
         return super.getProperty(interp, self, o, property, propertyName);
       }
     });
 
     // Map TypeAnswers methods onto Type objects
     templates.registerModelAdaptor(Type.class, new ObjectModelAdaptor() {
       @Override
       public Object getProperty(Interpreter interp, ST self, Object o, Object property,
           String propertyName) throws STNoSuchPropertyException {
         if ("nestedKinds".equals(propertyName)) {
           // A list of the inner kind parameterizations
           final List<Kind> kindReferences = new ArrayList<Kind>();
           ((Type) o).accept(new ModelVisitor() {
             @Override
             public boolean visit(Type t, Context<Type> ctx) {
               kindReferences.add(t.getKind());
               return true;
             }
           });
           kindReferences.remove(0);
           return kindReferences;
         }
         if ("shouldProtect".equals(propertyName)) {
           return TypeAnswers.shouldProtect((Type) o);
         }
         if (propertyName.startsWith("isKind")) {
           String kindName = propertyName.substring("isKind".length());
           Kind kind = Kind.valueOf(kindName.toUpperCase());
           return kind.equals(((Type) o).getKind());
         }
         return super.getProperty(interp, self, o, property, propertyName);
       }
     });
 
     doGenerate(options, output, s);
   }
 
   protected abstract void doGenerate(Options options, Collector output, Schema s)
       throws IOException;
 
   /**
    * Configure the given template with {@code model} and {@code dialectProperties} attributes. If
    * the model sets a dialect-specific {@code inspect} property to {@code true}, then
    * {@link ST#inspect()} will be called.
    */
   protected ST forModel(ST template, Model model) {
     Set<String> attributeNames = template.getAttributes().keySet();
     boolean inspect = false;
 
     if (attributeNames.contains(DIALECT_PROPERTIES_KEY)) {
       template.remove(DIALECT_PROPERTIES_KEY);
       Map<String, Map<String, String>> dialectProperties = model.getDialectProperties();
       if (dialectProperties != null) {
         Map<String, String> properties = dialectProperties.get(getName());
         if (properties != null) {
           template.add(DIALECT_PROPERTIES_KEY, properties);
           inspect = Boolean.parseBoolean(properties.get("inspect"));
         }
       }
     }
 
     if (attributeNames.contains(MODEL_KEY)) {
       template.remove(MODEL_KEY);
       template.add(MODEL_KEY, model);
     }
 
     if (inspect) {
       template.inspect(LINE_WIDTH);
     }
     return template;
   }
 
   /**
    * Returns the named template, attaching the {@code options} attribute.
    */
   protected ST getTemplate(String name, Options options) {
     return templates.getInstanceOf(name).add("options", options);
   }
 
   /**
    * Subclasses may return a list of classes that will be defined in a {@code names} dictionary in
    * the templates returned from {@link #getTemplate}. The simple name of the class will be mapped
    * to the canonical name of the class, allowing templates to write {@code <names.JsonDdlObject>}
    * instead of the fully-qualified type name or having to rely on import statements.
    */
   protected List<Class<?>> getTemplateClasses() {
     return Collections.emptyList();
   }
 
   /**
    * Similar to {@link #getTemplateClasses()}, but uses string values to avoid generator
    * dependencies on external toolkits.
    */
   protected List<String> getTemplateClassNames() {
     return Collections.emptyList();
   }
 
   protected STGroup getTemplateGroup() {
     return templates;
   }
 
   /**
    * Loads a template group based on dialect name.
    */
   protected STGroup loadTemplateGroup() {
     String location = "org/jsonddl/generator/templates/" + getName() + ".stg";
     URL resource = Thread.currentThread().getContextClassLoader().getResource(location);
     if (resource == null) {
       // Try this class's classloader, using an absolute resource path
       resource = getClass().getResource("/" + location);
     }
     if (resource == null) {
       throw new RuntimeException("Could not locate template at " + location);
     }
     return new STGroupFile(resource, "UTF8", '<', '>');
   }
 
   /**
    * Render the fully-initialized template into the given Writer. The line width will be set to
    * {@value #LINE_WIDTH}. The Writer will be closed by this method.
    */
   protected void renderTemplate(ST template, Writer out) throws IOException {
     AutoIndentWriter writer = new AutoIndentWriter(out);
     writer.setLineWidth(LINE_WIDTH);
     template.write(writer);
     out.close();
   }
 
 }
