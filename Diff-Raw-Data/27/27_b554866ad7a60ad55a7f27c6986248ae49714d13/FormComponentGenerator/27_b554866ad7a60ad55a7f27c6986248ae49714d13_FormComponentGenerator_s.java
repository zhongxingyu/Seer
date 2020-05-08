 package com.seitenbau.micgwaf.generator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.seitenbau.micgwaf.component.Component;
 import com.seitenbau.micgwaf.component.FormComponent;
 import com.seitenbau.micgwaf.component.HtmlElementComponent;
 import com.seitenbau.micgwaf.component.InputComponent;
 
 public class FormComponentGenerator extends HtmlElementComponentGenerator
 {
   @Override
   public JavaClassName getClassName(
       Component component,
       String targetPackage)
   {
     return toJavaClassName(component.id, targetPackage);
   }
   
   @Override
   public String generate(
       Component component,
       String targetPackage)
   {
     String rootContent = super.generate(component, targetPackage);
 
     StringBuilder fileContent = new StringBuilder();
     
     // replace inheritance
     rootContent = rootContent.replace(" extends " + HtmlElementComponent.class.getSimpleName(), 
         " extends " + FormComponent.class.getSimpleName());
 
     // remove last "}"
     rootContent = rootContent.substring(0,  rootContent.lastIndexOf("}") -1);
     
     // add import
     int indexOfImport = rootContent.indexOf("\nimport");
     fileContent.append(rootContent.substring(0, indexOfImport))
         .append("import ").append(HttpServletRequest.class.getName()).append(";\n")
         .append("import ").append(FormComponent.class.getName()).append(";\n")
         .append(rootContent.substring(indexOfImport));
 
     List<InputComponent> buttons = new ArrayList<>();
     getButtons(component, buttons);
     List<InputComponent> inputs = new ArrayList<>();
     getInputs(component, inputs);
     
     for (InputComponent button : buttons)
     {
       fileContent.append("\n\n");
      fileContent.append("  public Component ").append(button.id).append("Pressed()\n");
           fileContent.append("  {\n");
           fileContent.append("    return null;\n");
           fileContent.append("  }\n");
     }
     
     for (InputComponent input : inputs)
     {
       fileContent.append("\n\n");
      fileContent.append("  public String get").append(input.id.substring(0, 1).toUpperCase())
          .append(input.id.substring(1)).append("()\n");
       fileContent.append("  {\n");
      String pathToComponent = input.id;
       Component parent = input.parent;
       while (parent != component)
       {
         if (parent.id != null)
         {
          pathToComponent = parent.id + "." + pathToComponent;
         }
         parent = parent.parent;
       }
       fileContent.append("    return ").append(pathToComponent).append(".value;\n");
       fileContent.append("  }\n");
     }
     
     fileContent.append("\n");
     fileContent.append("  public void onSubmit()\n");
     fileContent.append("  {\n");
     fileContent.append("  }\n\n");
     fileContent.append("  @Override\n");
     fileContent.append("  public Component processRequest(HttpServletRequest request)\n");
     fileContent.append("  {\n");
     fileContent.append("    Component result = super.processRequest(request);\n");
     fileContent.append("    if (submitted)\n");
     fileContent.append("    {\n");
     fileContent.append("      onSubmit();\n");
     for (InputComponent button : buttons)
     {
      fileContent.append("      if (").append(button.id).append(".submitted)\n");
       fileContent.append("      {\n");
      fileContent.append("        result = ").append(button.id).append("Pressed();\n");
       fileContent.append("      }\n");
     }
     fileContent.append("    }\n");
     fileContent.append("    return result;\n");
     fileContent.append("  }\n");
     fileContent.append("}\n");
     
     return fileContent.toString();
   }
 
   @Override
   public String generateExtension(
       Component component,
       String targetPackage)
   {
     String className = getClassName(component, targetPackage).getSimpleName();
     String extensionClassName = getExtensionClassName(component, targetPackage).getSimpleName();
 
     StringBuilder fileContent = new StringBuilder();
     fileContent.append("package ").append(targetPackage).append(";\n\n");
     fileContent.append("\n");
     fileContent.append("import ").append(Component.class.getName()).append(";\n");
     fileContent.append("\n");
     fileContent.append("public class ").append(extensionClassName)
         .append(" extends ").append(className)
         .append("\n");
     fileContent.append("{\n");
     fileContent.append("  public " + extensionClassName + "(Component parent)\n");
     fileContent.append("  {\n");
     fileContent.append("    super(parent);\n");
     fileContent.append("  }\n");
     fileContent.append("}\n");
     return fileContent.toString();
   }
   
   public void getButtons(Component component, List<InputComponent> buttons)
   {
     if (component instanceof InputComponent)
     {
       InputComponent inputComponent = (InputComponent) component;
       if (inputComponent.isButton())
       {
         buttons.add(inputComponent);
       }
     }
     for (Component child : component.getChildren())
     {
       getButtons(child, buttons);
     }
   }
 
   public void getInputs(Component component, List<InputComponent> inputs)
   {
     if (component instanceof InputComponent)
     {
       InputComponent inputComponent = (InputComponent) component;
       if (!inputComponent.isButton())
       {
         inputs.add(inputComponent);
       }
     }
     for (Component child : component.getChildren())
     {
       getInputs(child, inputs);
     }
   }
 }
