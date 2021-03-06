 package org.exigencecorp.bindgen.processor;
 
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.lang.model.type.TypeMirror;
 
 import org.exigencecorp.gen.GClass;
 import org.exigencecorp.gen.GMethod;
 import org.exigencecorp.util.Inflector;
 
 public class FieldPropertyGenerator implements PropertyGenerator {
 
     private final GenerationQueue queue;
     private final GClass bindingClass;
     private final Element enclosed;
     private final ClassName propertyType;
     private final String propertyName;
     private TypeElement propertyTypeElement;
 
     public FieldPropertyGenerator(GenerationQueue queue, GClass bindingClass, Element enclosed) {
         this.queue = queue;
         this.bindingClass = bindingClass;
         this.enclosed = enclosed;
        TypeMirror boxed = this.queue.boxIfNeededOrNull(this.enclosed.asType());
        if (boxed != null) {
            this.propertyType = new ClassName(boxed);
        } else {
            this.propertyType = null;
        }
         this.propertyName = this.enclosed.getSimpleName().toString();
     }
 
     public boolean shouldGenerate() {
         if (this.propertyType == null || this.propertyType.getWithoutGenericPart().endsWith("Binding")) {
             return false;
         }
 
         if (this.shouldSkipAttribute(this.propertyName)) {
             return false;
         }
 
         TypeMirror fieldType = this.queue.boxIfNeededOrNull(this.enclosed.asType());
         if (fieldType == null) {
             return false; // Skip methods we (javac) could not box appropriately
         }
 
         this.propertyTypeElement = (TypeElement) this.getProcessingEnv().getTypeUtils().asElement(fieldType);
         if (this.propertyTypeElement == null) {
             return false;
         }
 
         return true;
     }
 
     public void generate() {
         this.bindingClass.getField(this.propertyName).type(this.propertyType.getBindingType());
         GClass fieldClass = this.bindingClass.getInnerClass("My{}Binding", Inflector.capitalize(this.propertyName)).notStatic();
         fieldClass.baseClassName(this.propertyType.getBindingType());
 
         GMethod fieldClassName = fieldClass.getMethod("getName").returnType(String.class);
         fieldClassName.body.line("return \"{}\";", this.propertyName);
 
         GMethod fieldClassGetParent = fieldClass.getMethod("getParentBinding").returnType("Binding<?>");
         fieldClassGetParent.body.line("return {}.this;", this.bindingClass.getSimpleClassNameWithoutGeneric());
 
         GMethod fieldClassGet = fieldClass.getMethod("get").returnType(this.propertyType.get());
         fieldClassGet.body.line("return {}.this.get().{};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName);
 
         GMethod fieldClassSet = fieldClass.getMethod("set").argument(this.propertyType.get(), this.propertyName);
         fieldClassSet.body.line("{}.this.get().{} = {};", this.bindingClass.getSimpleClassNameWithoutGeneric(), this.propertyName, this.propertyName);
 
         GMethod fieldGet = this.bindingClass.getMethod(this.propertyName).returnType(this.propertyType.getBindingType());
         fieldGet.body.line("if (this.{} == null) {", this.propertyName);
         fieldGet.body.line("    this.{} = new My{}Binding();", this.propertyName, Inflector.capitalize(this.propertyName));
         fieldGet.body.line("}");
         fieldGet.body.line("return this.{};", this.propertyName);
     }
 
     private ProcessingEnvironment getProcessingEnv() {
         return this.queue.getProcessingEnv();
     }
 
     private boolean shouldSkipAttribute(String name) {
         String configKey = "skipAttribute." + this.enclosed.getEnclosingElement().toString() + "." + name;
         String configValue = this.queue.getProperties().getProperty(configKey);
         return "true".equals(configValue);
     }
 
     public TypeElement getPropertyTypeElement() {
         return this.propertyTypeElement;
     }
 
     public String getPropertyName() {
         return this.propertyName;
     }
 
 }
