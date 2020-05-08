 package cs444.codegen;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import cs444.codegen.instructions.Comment;
 import cs444.codegen.instructions.Dd;
 import cs444.codegen.instructions.Extern;
 import cs444.codegen.instructions.Global;
 import cs444.codegen.instructions.Instruction;
 import cs444.codegen.instructions.Label;
 import cs444.codegen.instructions.Section;
 import cs444.codegen.instructions.Section.SectionType;
 import cs444.types.APkgClassResolver;
 
 public class SelectorIndexedTable {
     private final Map<String, Map<String, String>> sit = new HashMap<String, Map<String,String>>();;
     private final Map<String, Integer> offset = new LinkedHashMap<String, Integer>();
     private int offsetCounter = 0;
 
     public void addIndex(String className, String selector,
             String methodImplLabel) {
 
         Map<String, String> column = this.addClass(className);
 
         column.put(selector, methodImplLabel);
        offset.put(selector, offsetCounter);
        offsetCounter += 4;
     }
 
     public int getOffset(String selector){
         return offset.get(selector);
     }
 
     public void genCodeAndPrint(PrintStream printer) {
         List<Instruction> instructions = new LinkedList<Instruction>();
 
         instructions.add(new Section(SectionType.DATA));
 
         for (String classLabel : sit.keySet()) {
             instructions.add(new Global(classLabel));
             Map<String, String> column = sit.get(classLabel);
 
             for (String implLabel : column.values()) {
                 instructions.add(new Extern(implLabel));
             }
             instructions.add(new Label(classLabel));
 
             if (column.isEmpty()){
                 instructions.add(new Comment(classLabel + " does not have non-static method, so add NULL"));
                 instructions.add(new Dd(Immediate.NULL));
             }
 
             for (String selector : offset.keySet()) {
                 String implLabel = column.get(selector);
                 if (implLabel == null){
                     instructions.add(new Comment(classLabel + " does not have access to " + selector + ":"));
                     instructions.add(new Dd(Immediate.NULL));
                 }else{
                     instructions.add(new Dd(new Immediate(implLabel)));
                 }
             }
         }
 
         for(Instruction instruction : instructions) printer.println(instruction.generate());
     }
 
     public static SelectorIndexedTable generateSIT(List<APkgClassResolver> resolvers,
             boolean outputFile, String directory)
                     throws IOException, FileNotFoundException {
         SelectorIndexedTable sit = new SelectorIndexedTable();
 
         for(APkgClassResolver resolver : resolvers){
             resolver.addToSelectorIndexedTable(sit);
         }
 
         if(outputFile){
             File file = new File(directory + "_joos.sit.s");
             file.createNewFile();
             PrintStream printer = new PrintStream(file);
             sit.genCodeAndPrint(printer);
             printer.close();
         }
 
         return sit;
     }
 
     public Map<String, String> addClass(String fullName) {
         Map<String, String> column = sit.get(fullName);
         if (column == null){
             column = new HashMap<String, String>();
             sit.put(fullName, column);
         }
         return column;
     }
 
 }
