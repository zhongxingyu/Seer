 package com.tehbeard.annotations;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.regex.MatchResult;
 
 import javax.annotation.processing.*;
 import javax.lang.model.element.*;
 import javax.tools.StandardLocation;
 import javax.tools.Diagnostic.Kind;
 
 import com.tehbeard.utils.CallbackMatcher;
 import com.tehbeard.utils.CallbackMatcher.Callback;
 
 import me.tehbeard.BeardAch.dataSource.json.editor.EditorField;
 import me.tehbeard.BeardAch.dataSource.json.help.ComponentHelpDescription;
 import me.tehbeard.BeardAch.dataSource.json.help.ComponentType;
 import me.tehbeard.BeardAch.dataSource.json.help.ComponentValueDescription;
 
 
 
 @SuppressWarnings("restriction")
 @SupportedAnnotationTypes("me.tehbeard.BeardAch.dataSource.json.help.ComponentHelpDescription")
 public class BeardAchHelpProcessor extends AbstractProcessor {
 
     CallbackMatcher matcher = new CallbackMatcher("\\$\\{([A-Za-z0-9_]*)\\}");
 
     String html = "";
 
     String paramFragment = "";
 
 
     String tContents = "";
     String rContents = "";
 
     public void init(ProcessingEnvironment processingEnv) {
         super.init(processingEnv);
         html = new Scanner(this.getClass().getClassLoader().getResourceAsStream("ach-help-template.html")).useDelimiter("\\Z").next().replaceAll("\\Z", "");
         paramFragment = new Scanner(this.getClass().getClassLoader().getResourceAsStream("ach-params.html")).useDelimiter("\\Z").next().replaceAll("\\Z", "");
     }
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations,
             RoundEnvironment roundEnv) {
         Set<? extends Element> eles = roundEnv.getElementsAnnotatedWith(ComponentHelpDescription.class);
         for(Element ele  : eles){
             ComponentHelpDescription c = ele.getAnnotation(ComponentHelpDescription.class);
             processingEnv.getMessager().printMessage(Kind.NOTE, "Creating help file for " + getPackage(ele));
 
 
 
             Writer output = null;
             try {
                 final Map<String,String> data = new HashMap<String,String>();
                 data.put("name",c.name());
                 data.put("description",c.description());
                 data.put("dependencies", c.dependencies().length > 0 ? Arrays.toString(c.dependencies()) : "None");
 
                 String tmpParams = "";
                 for(Element field : ele.getEnclosedElements()){
                     if(!field.getKind().isField()){continue;}
 
                     EditorField ef = field.getAnnotation(EditorField.class);
                     if(ef == null){continue;}
 
                     final Map<String,String> pData = new HashMap<String, String>();
                     pData.put("alias", ef.alias());
                     pData.put("type", ef.type().label);
 
                     ComponentValueDescription vd = field.getAnnotation(ComponentValueDescription.class);
                     if(vd!=null){
                         pData.put("description", vd.description());
                         String example = "";
                         for(String e : vd.examples()){
                             example += e + "\n";
                         }
                         pData.put("examples", example);
                     }
                     tmpParams += processTemplate(pData, paramFragment) + "\n";
                 }
 
                 data.put("params", tmpParams);
 
 
                 output = openFile("editor/help/" + c.type().toString().toLowerCase() + "/" + ele.getSimpleName() + ".html");
 
 
                 output.write(processTemplate(data, html));
                output.flush();
 
                 if(c.type() == ComponentType.TRIGGER){
                     tContents += "<br><a target='main' href='" + c.type().toString().toLowerCase() + "/" + ele.getSimpleName() + ".html" + "'>" + c.name() + "</a>\n";
                 }
                 
                 if(c.type() == ComponentType.REWARD){
                     rContents += "<br><a target='main' href='" + c.type().toString().toLowerCase() + "/" + ele.getSimpleName() + ".html" + "'>" + c.name() + "</a>\n";
                 }
 
                 /*output.write("Name: " + c.name());
 				 output.write("\n");
 				 output.write("Description:\n");
 				 output.write(c.description());*/
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }finally{
                 try {
                     output.close();
                 } catch (IOException e) {
                 }
             }
         }
 
         if(roundEnv.processingOver()){
             Writer contentsFile;
             try {
                 contentsFile = openFile("editor/help/contents.html");
 
                 contentsFile.write("<h4>Triggers</h4>");
                 contentsFile.write(tContents);
                 contentsFile.write("<h4>Rewards</h4>");
                 contentsFile.write(rContents);
                 contentsFile.flush();
                 contentsFile.close();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
         }
         return false;
     }
 
 
     private String getPackage(Element ele){
         return ((PackageElement)ele.getEnclosingElement()).getQualifiedName() + "." + ele.getSimpleName();
     }
 
     private Writer openFile(String fileName) throws IOException{
         return processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT,"",fileName).openWriter();
     }
 
     private String processTemplate(final Map<String,String> data,String rawhtml){
         return matcher.replaceMatches(rawhtml, new Callback() {
 
             public String foundMatch(MatchResult result) {
                 if (data.containsKey(result.group(1))) {
                     return data.get(result.group(1));
                 }
                 return "";
             }
         });
     }
 
 }
