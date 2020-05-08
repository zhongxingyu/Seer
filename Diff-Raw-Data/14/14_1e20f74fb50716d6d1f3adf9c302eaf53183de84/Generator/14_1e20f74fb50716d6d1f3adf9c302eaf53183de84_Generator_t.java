 package com.seitenbau.micgwaf.generator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 
 import com.seitenbau.micgwaf.component.ChildListComponent;
 import com.seitenbau.micgwaf.component.Component;
 import com.seitenbau.micgwaf.component.ComponentRegistry;
 import com.seitenbau.micgwaf.component.EmptyComponent;
 import com.seitenbau.micgwaf.component.FormComponent;
 import com.seitenbau.micgwaf.component.HtmlElementComponent;
 import com.seitenbau.micgwaf.component.InputComponent;
 import com.seitenbau.micgwaf.component.RefComponent;
 import com.seitenbau.micgwaf.component.SnippetListComponent;
 import com.seitenbau.micgwaf.parser.HtmlParser;
 
 public class Generator
 {
   public static final Map<Class<? extends Component>, ComponentGenerator> componentGeneratorMap = new HashMap<>();
   
   static
   {
     componentGeneratorMap.put(SnippetListComponent.class, new SnippetListComponentGenerator());
     componentGeneratorMap.put(HtmlElementComponent.class, new HtmlElementComponentGenerator());
     componentGeneratorMap.put(InputComponent.class, new InputComponentGenerator());
     componentGeneratorMap.put(FormComponent.class, new FormComponentGenerator());
     componentGeneratorMap.put(RefComponent.class, new RefComponentGenerator());
     componentGeneratorMap.put(ChildListComponent.class, new ChildListComponentGenerator());
     componentGeneratorMap.put(EmptyComponent.class, new EmptyComponentGenerator());
   }
   
   public void generateComponent(
         File sourceDirectory,
         File targetDirectory,
         File extensionsTargetDirectory,
         String targetPackage)
       throws IOException
   {
     HtmlParser parser = new HtmlParser();
     Map<String, Component> componentMap 
         = parser.readComponents(sourceDirectory);
     for (Map.Entry<String, Component> entry : componentMap.entrySet())
     {
       Component component = entry.getValue();
       String componentPackage = targetPackage + "." + component.id;
       
       Map<JavaClassName, String> componentFilesToWrite = new HashMap<>();
       Map<JavaClassName, String> extensionFilesToWrite = new HashMap<>();
       generate(component, componentPackage, componentFilesToWrite);
       generateExtension(component, componentPackage, extensionFilesToWrite);
       for (Map.Entry<JavaClassName, String> fileToWriteEntry : componentFilesToWrite.entrySet())
       {
         File targetFile = new File(
             targetDirectory,
             fileToWriteEntry.getKey().getSourceFile());
        writeFile(targetFile, fileToWriteEntry.getValue(), true);
       }
       for (Map.Entry<JavaClassName, String> fileToWriteEntry : extensionFilesToWrite.entrySet())
       {
         File targetFile = new File(
             extensionsTargetDirectory,
             fileToWriteEntry.getKey().getSourceFile());
        writeFile(targetFile, fileToWriteEntry.getValue(), false);
       }
     }
     generateComponentRegistry(componentMap, targetDirectory, targetPackage);
   }
 
  private void writeFile(File targetFile, String content, boolean overwrite) throws IOException
   {
     File targetDir = targetFile.getParentFile();
     if (!targetDir.exists())
     {
       if (!targetDir.mkdirs())
       {
         throw new IOException("Could not create directory " + targetDir.getAbsolutePath());
       }
     }
    if (overwrite || !targetFile.exists())
     {
       FileUtils.writeStringToFile(targetFile,  content,  "ISO-8859-1");
     }
   }
   
   public void generate(Component component, String targetPackage, Map<JavaClassName, String> filesToWrite)
   {
     ComponentGenerator componentGenerator = componentGeneratorMap.get(component.getClass());
     String result = componentGenerator.generate(component, targetPackage);
     if (result != null)
     {
       filesToWrite.put(componentGenerator.getClassName(component, targetPackage), result);
     }
     for (Component child : component.getChildren())
     {
       generate(child, targetPackage, filesToWrite);
     }
   }
   
   public void generateExtension(Component component, String targetPackage, Map<JavaClassName, String> filesToWrite)
   {
     ComponentGenerator componentGenerator = componentGeneratorMap.get(component.getClass());
     if (componentGenerator.generateExtensionClass(component))
     {
       String result = componentGenerator.generateExtension(component, targetPackage);
       filesToWrite.put(componentGenerator.getExtensionClassName(component, targetPackage), result);
     }
     for (Component child : component.getChildren())
     {
       generateExtension(child, targetPackage, filesToWrite);
     }
   }
   
   public void generateComponentRegistry(
       Map<String, Component> componentMap,
       File targetDirectory,
       String targetPackage) throws IOException
   {
     JavaClassName javaClassName = new JavaClassName("ComponentRegistryImpl", targetPackage);
     String className = javaClassName.getSimpleName();
     StringBuilder content = new StringBuilder();
     content.append("package ").append(targetPackage).append(";\n\n");
     content.append("import ").append(ComponentRegistry.class.getName()).append(";\n");
     for (Map.Entry<String, Component> entry : componentMap.entrySet())
     {
       Component component = entry.getValue();
       ComponentGenerator componentGenerator = componentGeneratorMap.get(component.getClass());
       String componentPackage = targetPackage + "." + component.id;
       JavaClassName componentClassName = componentGenerator.getReferencableClassName(component, componentPackage);
       content.append("import ").append(componentClassName.getName()).append(";\n");
     }
     content.append("\n");
     content.append("public class ").append(className)
         .append(" extends ").append(ComponentRegistry.class.getSimpleName())
         .append("\n");
     content.append("{\n");
     content.append("  public ").append(className).append("()\n");
     content.append("  {\n");
     for (Map.Entry<String, Component> entry : componentMap.entrySet())
     {
       Component component = entry.getValue();
       ComponentGenerator componentGenerator = componentGeneratorMap.get(component.getClass());
       String componentPackage = targetPackage + "." + component.id;
       JavaClassName componentClassName 
           = componentGenerator.getReferencableClassName(component, componentPackage);
       content.append("    components.put(\"").append(componentClassName.getSimpleName())
           .append("\", new ").append(componentClassName.getSimpleName()).append("(null));\n");
     }
     content.append("  }\n");
     content.append("}\n");
     File targetFile = new File(targetDirectory, javaClassName.getSourceFile());
    writeFile(targetFile, content.toString(), true);
   }
   
   public static ComponentGenerator getGenerator(Component component)
   {
     return getGenerator(component.getClass());
   }
 
   public static ComponentGenerator getGenerator(Class<? extends Component> componentClass)
   {
     ComponentGenerator result = componentGeneratorMap.get(componentClass);
     if (result == null)
     {
       throw new IllegalArgumentException("Unknown component class " + componentClass);
     }
     return result;
   }
 }
