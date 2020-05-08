 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.apkc.archtype.processors;
 
 import com.apkc.archtype.AlloyTest;
 import com.apkc.archtype.quals.Component;
 import com.apkc.archtype.quals.Pattern;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.annotation.processing.AbstractProcessor;
 import javax.annotation.processing.Messager;
 import javax.annotation.processing.RoundEnvironment;
 import javax.annotation.processing.SupportedAnnotationTypes;
 import javax.annotation.processing.SupportedSourceVersion;
 import javax.lang.model.SourceVersion;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic;
 
 /**
  * Component processor. Process @Component annotations and writes alloy models to files
  * @author asger
  */
 @SupportedSourceVersion(SourceVersion.RELEASE_7)
 @SupportedAnnotationTypes({"com.apkc.archtype.quals.Component"})
 public class ComponentProcessor extends AbstractProcessor {
 
     /**
      *
      * @param annotations annotations - contains the set of annotations that we are interested in processing. The annotations in the set should correspond with the list of annotations that we specify in the @SupportedAnnotationTypes annotation
      * @param roundEnv - this is the processing environment
      * @return boolean -  a boolean value is to indicate whether we have claimed ownership of the set of annotations passed by the processing environment in elements. "Claiming ownership" means that the set of annotations in elements are ours, and we have processed it. Return true if you claim ownership of them; false otherwise.
      */
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
         //processingEnv is a predefined member in AbstractProcessor class
         //Messager allows the processor to output messages to the environment
         Messager messager = processingEnv.getMessager();
         messager.printMessage(Diagnostic.Kind.NOTE, "Starting processor");
         HashMap components = new HashMap();
         boolean claimed = false;
         for (TypeElement te: annotations) {
 
             //Get the members that are annotated with Option
             for (Element e: roundEnv.getElementsAnnotatedWith(te)){
                 claimed = true;
                 processAnnotation(e, messager, components);
             }
         }
 
         //If there are any annotations, we will proceed to generate the annotation
         //processor in generateOptionProcessor method
         ArrayList<String> files = generateAlloyModels(components);
         for(String file: files){
             AlloyTest.passToAlloy(file);
         }
         return claimed;
     }
     /**
      *
      * @param e - the element currently being worked on
      * @param m - the messager used for errors, notes and the like
      * @param c - a hashmap containing all components, stored under the pattern(s) they are part of
      */
     private void processAnnotation(Element e, Messager m, HashMap c){
         Component com = e.getAnnotation(Component.class);
 
         // TODO do some analisys of enclosed contra p.references, do they
         List<? extends Element> enclosedElements = e.getEnclosedElements();
 
         ArrayList<ComponentRepresentation> cr;
         System.out.println("{");
         System.out.println("Component: ");
         System.out.println("\tname: " + com.name());
         System.out.println( "\tfileName: " + e.getSimpleName());
         for(Pattern p : com.patterns()){
             ComponentRepresentation comRep = new ComponentRepresentation(com.name(), p.kind(), p.role(), p.references());
             // check if the hashmap contains pattern p, if is does check if the list allready
             // contains the the component and extend the components references
             // else just add component
             if(c.containsKey(p.name())){
                 cr =  (ArrayList<ComponentRepresentation>) c.get(p.name());
                 boolean wasThere = false;
                 for (ComponentRepresentation crIns : cr) {
                     if(crIns.getComponentName().equals(comRep.getComponentName())){
                         crIns.extendReferences(comRep.getRefreferences());
                         wasThere = true;
                     }
                 }
                 if(!wasThere){
                     cr.add(comRep);
                 }
             } else{
                 cr = new ArrayList<>();
                 c.put(p.name(), cr);
                 cr.add(comRep);
             }
 
 
             String[] refs = p.references();
             System.out.println("\t\t{");
             System.out.println("\t\t\tPattern:");
             System.out.println("\t\t\t\tname: " + p.name());
             System.out.println("\t\t\t\tkind: " + p.kind());
             System.out.println("\t\t\t\trole: " + p.role());
             System.out.println("\t\t\t\trefs: {" );
             for(String ref: p.references()){
                 System.out.println("\t\t\t\t\tref: " + ref + " ");
             }
             /*for(Element ee: enclosedElements){
              * System.out.println(ee.+ " " + ee.asType().toString());
              * if(ee.getKind().isClass()){
              *
              * String actualRef = ee.asType().toString();
              * System.out.println(actualRef);
              * actualRef = actualRef.substring(actualRef.lastIndexOf("."));
              * if(Arrays.asList(refs).contains(actualRef)){
              * System.out.println("\t\t\t\t\t"+actualRef);
              * }else{
              * m.printMessage(Diagnostic.Kind.ERROR, "illegal reference", e);
              * }
              * }
              * }*/
             System.out.println("\t\t\t\t}");
             System.out.println("\t\t}");
         }
         System.out.println("}");
     }
     /**
      *
      * @param coms
      */
     private ArrayList<String> generateAlloyModels(HashMap coms ) {
         ArrayList<String> fileNames = new ArrayList<>();
         if(coms.isEmpty()){
             return fileNames;
         }
 
         Iterator i = coms.entrySet().iterator();
         // Iterate over each pattern and write alloy model
         while(i.hasNext()){
             Map.Entry next = (Map.Entry) i.next();
             String patternName = (String) next.getKey();
             ArrayList<ComponentRepresentation> comRe = (ArrayList<ComponentRepresentation>) next.getValue();
 
             String generatedFilename = patternName + "_configuration.als";
             try {
                 BufferedWriter out = new BufferedWriter(new FileWriter(generatedFilename), 32768);
 
                 String pat = "";
                 StringBuilder contains = new StringBuilder("\telements = ");
                 if(comRe != null){
                     Iterator<ComponentRepresentation> it = comRe.iterator();
                     while( it.hasNext()) {
                         ComponentRepresentation c = it.next();
                         contains.append(c.getComponentName());
                         if (it.hasNext()) {
                             pat = c.getPattern();
                             contains.append(" + ");
                         } else{
                             contains.append("\n}\n");
                         }
                     }
                 }
                 out.write("open " + pat.toLowerCase() + "\n");
                 out.write("one sig " + patternName + " extends Configuration { } {\n");
                 out.write(contains.toString());
                 if(comRe != null){
                     Iterator<ComponentRepresentation> ite = comRe.iterator();
                     while( ite.hasNext()) {
                         ComponentRepresentation c = ite.next();
                         out.write(c.toString());
                     }
                 }
                writeAsserts(out,pat,patternName);
                writeCommands(out, pat);
                
                 out.close();
                 fileNames.add(generatedFilename);
             } catch (IOException ex) {
                 Logger.getLogger(ComponentProcessor.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return fileNames;
     }

    /**
     * Should analyze which pattern is used and write the appropriate commands for that pattern, using the writer.
     * @param bw
     * @param pattern
     */
    private void writeCommands(BufferedWriter bw, String pattern) throws IOException{
        if("mvc".equals(pattern.toLowerCase())){
            bw.write("check models for 8\n");
            bw.write("check views for 8\n");
        }
    }

    private void writeAsserts(BufferedWriter bw, String pattern, String patternName) throws IOException{
        if("mvc".equals(pattern.toLowerCase())){
            bw.write("assert models {\n");
            bw.write("\t" + pattern.toLowerCase() + "_view" +"_style["+ patternName+"]\n");
            bw.write("}\n");
            bw.write("assert views {\n");
            bw.write("\t" + pattern.toLowerCase() + "_model" +"_style["+ patternName+"]\n");
            bw.write("}\n");
        }
    }
 }
