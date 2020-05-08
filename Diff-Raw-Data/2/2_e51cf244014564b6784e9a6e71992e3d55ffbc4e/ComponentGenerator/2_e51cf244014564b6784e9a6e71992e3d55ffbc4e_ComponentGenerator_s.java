 package com.seitenbau.micgwaf.generator.component;
 
 import java.util.List;
 
 import com.seitenbau.micgwaf.component.Component;
 import com.seitenbau.micgwaf.component.RefComponent;
 import com.seitenbau.micgwaf.component.SnippetListComponent;
 import com.seitenbau.micgwaf.generator.Generator;
 import com.seitenbau.micgwaf.generator.JavaClassName;
 
 public abstract class ComponentGenerator
 {
   /**
    * Returns the class name of the component which will represent the parsed component
    * in the generated code. This can either be a generated component, or a component
    * supplied in some Library.
    * Also there may be an extension class containing user modifications to the generating class,
    * in which case the extension class should be used in references instead of this class.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name, not null.
    */
   public abstract JavaClassName getClassName(
       Component component,
       String targetPackage);
 
   /**
    * Returns the class name of the  extension class containing user modifications to the generating class.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name of the extension class. 
    *         May return null if generateExtensionClass() returns null for the component.
    */
   public JavaClassName getExtensionClassName(
       Component component,
       String targetPackage)
   {
     return toExtensionClassName(component.getId(), targetPackage);
   }
   
   /**
    * Returns whether an extension class should be generated for the component.
    * 
    * @param component the component to check, not null.
    * 
    * @return true if an extension class should be generated, false otherwise.
    */
   public abstract boolean generateExtensionClass(Component component);
   
   /**
    * Returns the class name of the class by which this component can be referenced in generated code.
    * 
    * @param component the component to be represented, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the class name of the extension class. 
    *         May return null if generateExtensionClass() returns null for the component.
    */
   public JavaClassName getReferencableClassName(
       Component component,
       String targetPackage)
   {
     if (generateExtensionClass(component))
     {
       return getExtensionClassName(component, targetPackage);
     }
     return getClassName(component, targetPackage);
   }
 
   /**
    * Generates the component class representing the component.
    * 
    * @param component the component for which code should be generated, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the code for the generated class, or null if no class should be generated.
    */
   public abstract String generate(
       Component component,
       String targetPackage);
   
   /**
    * Generates the component extension class representing the component.
    * The extension class contains user-specific extensions to the generated class.
    * 
    * @param component the component for which code should be generated, not null.
    * @param targetPackage the base target package for the generated classes, not null.
    * 
    * @return the code for the generated extension class, or null if no extension class should be generated.
    */
   public abstract String generateExtension(
       Component component,
       String targetPackage);
 
   /**
    * Generates an initializer which initializes this component if this component is constructed
    * and assigned to a field.
    * If this component does not need to be initialized, the empty string can be returned.
    * 
    * @param componentField the name of the field the component is assigned to, not null.
    * @param component the component to generate the initializer for, not null.
    * @param targetPackage the target package of the component, not null.
    * @param indent how many spaces to indent, not null.
    * 
    * @return the initializer java code, of the empty string if no initilaizer code is needed; not null.
    */
   public abstract String generateInitializer(
       String componentField,
       Component component,
       String targetPackage,
       int indent);
   
   /**
    * Converts a component and a package into a java class name following the base class naming pattern.
    * The method first checks if an extension class is generated, and if yes and the configuration
    * say that base classes without extension should follow the extension naming pattern, it forwards
    * to the toExtensionClassName method.
    * If this is not the case, any loop part suffixes (starting with a colon :) are removed from the 
    * component id, and the first character of the component id is converted to upper case.
    * This modified id is then prefixed by the baseClassPrefix and baseClassSuffix 
    * from the generator configuration.
    * 
    * @param component the component to generate the class name for, not null.
    * @param packageName TODO
    * 
    * @return the java class name for the componentid/package pair.
    */
   public JavaClassName toBaseClassName(Component component, String packageName)
   {
     if (!generateExtensionClass(component) 
         && !Generator.getGeneratorConfiguration().isBaseClassWithoutExtensionNamedLikeBaseClasses())
     {
       return toExtensionClassName(component.getId(), packageName);
     }
     String normalizedId = removeLoopPart(component.getId());
     String simpleName = Generator.getGeneratorConfiguration().getBaseClassPrefix() 
         + normalizedId.substring(0, 1).toUpperCase()
         + normalizedId.substring(1)
         + Generator.getGeneratorConfiguration().getBaseClassSuffix();
     return new JavaClassName(simpleName, packageName);
   }
   
   /**
    * Converts a component id and a package into a java class name following the extension class 
    * naming pattern.
    * Any loop part suffixes (starting with a colon :) are removed from the 
    * component id, and the first character of the component id is converted to upper case.
    * This modified id is then prefixed by the extensionClassPrefix and extensionClassSuffix 
    * from the generator configuration.
    * 
    * @param componentId the id of the component to generate the class name for, not null.
    * @param packageName TODO
    * 
    * @return the java class name for the componentid/package pair.
    */
   public JavaClassName toExtensionClassName(String componentId, String packageName)
   {
     String normalizedId = removeLoopPart(componentId);
     String simpleName = Generator.getGeneratorConfiguration().getExtensionClassPrefix() 
         + normalizedId.substring(0, 1).toUpperCase()
         + normalizedId.substring(1)
         + Generator.getGeneratorConfiguration().getExtensionClassSuffix();
     return new JavaClassName(simpleName, packageName);
   }
   
   /**
    * Returns a string in a form which can be used as string constant in a java source file.
    * The String is surrounded with double quotes, and backslashes, carriage returns and newlines
    * are replaced with the appropriate escape sequences.
    *  
    * @param string the string to be converted to a string constant, not null.
    * 
    * @return the string constant, not null.
    */
   public String asConstant(String string)
   {
     String result = string.replace("\\", "\\\\");
     result = result.replace("\r", "\\r");
     result = result.replace("\n", "\\n");
     result = result.replace("\"", "\\\"");
     return "\"" + result + "\"";
   }
 
   /**
    * Generates a field or a local variable from a component. 
    * If the component is not of the general Type Component, 
    * the component is constructed using the constructor with the parent argument, assigned to
    * the local variable and field.
    * Afterwards, the component is initialized using the initializer.
    * 
    * @param component The component to create the package name for, not null.
    * @param targetPackage TODO
    * @param toAppendTo the String builder to which the generated code should be appended, not null.
    * @param modifier any modifier to the variable or field, e.g "public " or "final ".
    * @param fieldName the name of the field or variable
    * @param indent how many spaces the generated code should be indented.
    */
   public void generateFieldOrVariableFromComponent(
       Component component,
       String targetPackage, 
       StringBuilder toAppendTo,
       String modifier,
       String fieldName,
       int indent)
   {
     String indentString = getIndentString(indent);
     ComponentGenerator generator = Generator.getGenerator(component);
     JavaClassName componentClassName = generator.getReferencableClassName(component, targetPackage);
     if (componentClassName.isNameFor(Component.class))
     {
       toAppendTo.append(indentString).append(modifier).append(componentClassName.getSimpleName())
       .append(" ").append(fieldName).append(";\n");
     }
     else
     {
       toAppendTo.append(indentString).append(modifier).append(componentClassName.getSimpleName())
           .append(" ").append(fieldName)
           .append(" = new ").append(componentClassName.getSimpleName()).append("(this);\n");
     }
     toAppendTo.append(generator.generateInitializer(fieldName, component, targetPackage, indent));
   }
 
   public void generateInitChildren(
       Component component, 
       String targetPackage,
       StringBuilder result,
       String componentField,
       int indent)
   {
     List<? extends Component> children = component.getChildren();
     if (children.isEmpty())
     {
       return;
     }
     String indentString = getIndentString(indent);
     result.append(indentString).append("{\n");
     int counter = 1;
     for (Component child : children)
     {
       String fieldName;
       if (child.getId() != null)
       {
         fieldName = removeLoopPart(child.getId());
       }
       else
       {
         fieldName = getChildName(componentField, counter);
       }
       generateFieldOrVariableFromComponent(child, targetPackage, result, "", fieldName, indent + 2);
       result.append(indentString).append("  ").append(componentField).append(".children.add(")
           .append(fieldName).append(");\n");
 
       counter++;
     }
     result.append(indentString).append("}\n");
   }
   
   /**
    * Generates an empty constructor for a component with a parent argument.
    * 
    * @param className the unqualified class name of the class for which the constructor is generated.
    * @param toAppendTo the content to which the constructor code should be appended.
    */
   public void generateComponentConstructorWithParent(String className, StringBuilder toAppendTo)
   {
     // Constructor
     toAppendTo.append("\n  /**\n");
     toAppendTo.append("  * Constructor. \n");
     toAppendTo.append("  *\n");
     toAppendTo .append("  * @param parent the parent component,")
         .append(" or null if this is a standalone component (e.g. a page)\n");
     toAppendTo.append("  */\n");
    toAppendTo.append("  public " + className + "(Component parent)");
     toAppendTo.append("  {\n");
     toAppendTo.append("    super(parent);\n");
     toAppendTo.append("  }\n");
   }
   
   /**
    * Generates the class javadoc for component classes.
    * 
    * @param component the component for which the javadoc should be generated
    * @param toAppendTo the content to which the constructor code should be appended.
    * @param forExtension if the javadoc is generated for an extension class.
    */
   protected void generateClassJavadoc(Component component, StringBuilder toAppendTo, boolean forExtension)
   {
     toAppendTo.append("/**\n");
     toAppendTo.append(" * This class represents the HTML element with m:id ")
             .append(removeLoopPart(component.getId())).append(".\n");
     toAppendTo.append(" * Instances of this class are used whenever these elements are rendered\n");
     toAppendTo.append(" * or when form date from a page containing these elements is processed.\n");
     if (!forExtension)
     {
       ComponentGenerator generator = Generator.getGenerator(component);
       if (generator.generateExtensionClass(component))
       {
         toAppendTo.append(" *\n");
         toAppendTo.append(" * NOTE: This class should not be referenced; instead, the class\n");
         toAppendTo.append(" * ").append(generator.getExtensionClassName(component, "dummy").getSimpleName())
             .append(" should be used.").append("\n");
       }
     }
     toAppendTo.append(" **/\n");
   }
 
   public String removeLoopPart(String id)
   {
     if (id == null)
     {
       return null;
     }
     int indexOfColon = id.indexOf(':');
     if (indexOfColon == -1)
     {
       return id;
     }
     return id.substring(0, indexOfColon);
   }
   
   public String getIndentString(int indent)
   {
     StringBuilder result = new StringBuilder(indent);
     for (int i = 0; i < indent; ++i)
     {
       result.append(" ");
     }
     return result.toString();
   }
   
   public String getChildName(String baseName, int counter)
   {
     if (baseName == null || "".equals(baseName))
     {
       return "child" + counter;
     }
     return baseName + "Child" + counter;
   }
   
 
   public String getComponentFieldName(Component component, int componentCounter)
   {
     if (component != null && component.getId() != null)
     {
       return removeLoopPart(component.getId());
     }
     else if (component instanceof RefComponent)
     {
       RefComponent refComponent = (RefComponent) component;
       return refComponent.refid;
     }
     return  "component" + componentCounter;
   }
 
   public String getComponentFieldName(SnippetListComponent.ComponentPart part, int componentCounter)
   {
     if (part.component != null)
     {
       return getComponentFieldName(part.component, componentCounter);
     }
     return  "snippet" + componentCounter;
   }
 
   /**
    * Generates code for the serialVersionUID constant for serializable classes.
    * 
    * @param fileContent the StringBuilder to which the code should be appended.
    */
   public void generateSerialVersionUid(StringBuilder toAppendTo)
   {
     // SerialversionUID
     toAppendTo.append("  /** Serial Version UID. */\n");
     toAppendTo.append("  private static final long serialVersionUID = 1L;\n");
   }
 }
