 /*
  * Conventions:
  *  -the root package name is "Default Package", not "" as before
  */
 
 package xtc.oop;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.FileReader;
 
 import java.util.*;
 
 import xtc.parser.ParseException;
 import xtc.parser.Result;
 
 import xtc.tree.GNode;
 import xtc.tree.Node;
 import xtc.tree.Visitor;
 import xtc.tree.Location;
 
 import xtc.tree.Printer;
 
 import xtc.lang.JavaFiveParser;
 
 //our imports
 import xtc.oop.helper.Bubble;
 import xtc.oop.helper.Mubble;
 import xtc.oop.helper.Pubble;
 import xtc.oop.helper.Field;
 
 import xtc.oop.StructureParser;
 import xtc.oop.ImplementationParser;
 //
 import java.util.regex.*;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 
 public class NewTranslator extends xtc.util.Tool{
 
 
   public static ArrayList<Bubble> bubbleList; // Classes
   public static ArrayList<Pubble> pubbleList; // Packages
   public static ArrayList<Mubble> mubbleList; // Methods
   public static ArrayList<Mubble> langList;   // java_lang Methods (don't want to print them)
 
   public String getName()
   {
     return "LSD: Language Switching Device";
   }
 
   public String getCopy()
   {
 
     return "Ninja assassins: DK, Calvin, Andrew*2";
   }
 
   public void init(){
     super.init();
   }
 
   public Node parse(Reader in, File file) throws IOException, ParseException
   {
     JavaFiveParser parser = new JavaFiveParser(in, file.toString(), (int)file.length());
     Result result = parser.pCompilationUnit(0);
 
     return (Node)parser.value(result);
   }
 
   public static String findFile(String query) {//{{{
 
     String sep = System.getProperty("file.separator");
     String cp = System.getProperty("java.class.path");
     //Hardcoded as the working directory, otherwise real classpath
     cp = ".";
 
     query = query.replace(".",sep).concat(".java");
     //System.out.println("+++++"+query);
     return findFile(cp, query);
   }//}}}
 
   public static String findFile(String cp, String query) {//{{{
     String sep = System.getProperty("file.separator");
     File f = new File(cp);
     File [] files = f.listFiles();
     for(int i = 0; i < files.length; i++) {
       //System.out.println(sep+(cp.equals(".") ? "\\\\" : "")+cp+sep);
       //////////////////////////////////////
       //Hardcoding that sep is / and cp is .
       //////////////////////////////////////
       //System.out.println(query);
       if(files[i].isDirectory()) {
         String a = findFile(files[i].getAbsolutePath(), query);
         if(!a.equals(""))
           return a;
       }
       else if(files[i].getAbsolutePath().replaceAll("/\\./",sep).endsWith(query))
         return files[i].getAbsolutePath();
     }
     return "";
   }//}}}
 
 
   public void process(Node node){
 
     StructureParser s = new StructureParser(this, pubbleList, mubbleList, bubbleList, langList);
     s.dispatch(node);
 
     //Not sure if we need this:
     ArrayList<String> parsed = new ArrayList<String>();
     ImplementationParser i = new ImplementationParser(this, pubbleList, mubbleList, bubbleList, langList, parsed);
     i.dispatch(node);
 
 
   }
 
   public static void main (String [] args)
   {
     /* new Translator
      * new StructParser //was Decl
      * new ImplParser   //was Impl
      */
 
     NewTranslator t = new NewTranslator();
     t.prepStructures();
     t.init();
     t.prepare();
     for(int i = 0; i< args.length; i++){
       try{ //TODO put in flag to not system exit also maybe change to run
         t.process(args[i]);
       } catch (Exception e) {System.out.println(e);}
     }
 
     //At this point, pubbleList contains all the packages but they aren't linked together
     t.constructPackageTree();
 
 
     //Print structure <- use for testing dependencies, inheritance
     if(false)
       for(Pubble p : pubbleList){
         System.out.println("^||^||^||^||^||^||^||^||^||^||^");
         System.out.println("Package: " + p.getName());
         if(p.getParent() == null)
           System.out.println("No parent");
         else
           System.out.println("Parent: " + p.getParent().getName());
         System.out.println("Children: ");
         for(Pubble c : p.getChildren()){
           System.out.println("\t-" + c.getName());
         }
         System.out.println("Classes: ");
         for(Bubble b : p.getBubbles()){
           System.out.println("\t- " + b.getName());
 
         }
       }
 
 
     //Printing Contents of Everything
     if(false)
       for(Pubble p : pubbleList)
         {
           System.out.println("Package: " + p.getName());
           for(Bubble b : p.getBubbles())
             {
               System.out.println("\tClass: " + b.getName());
               for(Mubble m : b.getMubbles())
                 {
                   System.out.println("\t\tMethod: " + m.getName());
                   System.out.println("\t\t{\n \t\t" + m.getCode() + "\n\t\t}");
                 }
             }
 
 
         }
         /* Lets Print a .H!!! */
        System.out.println("\n=====================  .h  =====================\n");
         Pubble root = new Pubble();
         for(Pubble p : pubbleList){
             if(p.getName().equals("Default Package"))
                 root = p;
         }
         String doth = root.getH();
         System.out.println(doth);
        System.out.println("\n================================================\n");
        System.out.println("\n=====================  .cc  =====================\n");
         String dotc = root.getCC();
         System.out.println(dotc);
        System.out.println("\n=================================================\n");

     //-before printing, call setParameters on each Bubble so that DK's
     //  previous printing methods work
 
   }
 
 
   //**********************TO IMPLEMENT***************************//
 
   //putting all of java_lang methods into the langList
   public void populateLangList()
   {
     //to implement with new mubble structure
     //see Decl line 716 for old implementation
   }
 
   //************************HELPER METHODS***********************//
   public void constructPackageTree()
   //constructs package tree from pubblelist
   {
     boolean rootFound = false;
     Pubble root = new Pubble();
     //set root to Default Package
 
     for(Pubble p : pubbleList){
       //System.out.println(p.getName());
       if(p.getName().equals(null)){//{{{
         //what is this strange animal
         System.out.println("V_V_V_ inspect null package _V_V_V_V");
         for(Bubble b : p.getBubbles())
           {
             System.out.println("\tClass: " + b.getName());
             for(Mubble m : b.getMubbles())
               {
                 System.out.println("\t\tMethod: " + m.getName());
                 System.out.println("\t\t{\n \t\t" + m.getCode() + "\n\t\t}");
 
               }
           }
 
       }//}}}
       if(p.getName().equals("Default Package")){
         root = p;
         rootFound = true;
       }
     }
     if(!rootFound){ //this should never happen... just in case, create it
       root = new Pubble("Default Package", null);
       pubbleList.add(root);
     }
 
 
     //for all pubbles, link children to it. (search on parent name)
     //also, if your parent doesn't exist, create it
     //we create a copy of the list to iterate through to avoid concurrentModError
     ArrayList<Pubble> pubbleListCopy = new ArrayList<Pubble>(pubbleList);
     for(Pubble p : pubbleListCopy){
       String name = p.getName();
       if((p.getParent()==null || p.getParent().equals(""))
          && !name.equals("Default Package")){
         //get lineage
         String[] lineage = name.split(" ");
 
         //remove " " (~_~;)
         int scount = 0;
         for(String s : lineage){
           if(s.equals(" "))
             scount++;
         }
         String[] nlineage = new String[lineage.length - scount];
         int ind = 0;
         for(int i = 0; i< lineage.length; i ++){
           if(!(lineage[i].equals(" "))){
             nlineage[ind] = lineage[i];
             ind++;
           }
         }
         //" " are now removed ^_^
 
         if(nlineage.length == 1){
           p.setParent(root);
           root.addChild(p);
         }
         else{
           Pubble parent = findParent(lineage);
           parent.addChild(p);
         }
       }
     }
   }
 
 
   public Pubble findParent(String[] lineage){
     /* Say we are given:
      * xtc oop helper
      * And want to find/create the parent, then return it.
      * Special case for "Default Package"
      */
     //find root
     Pubble root = new Pubble();
     boolean rootFound = false;
     //set root to Default Package
     for(Pubble p : pubbleList){
       if(p.getName().equals("Default Package")){
         root = p;
         rootFound = true;
       }
     }
     if(!rootFound)//serious problem
       System.out.println("Watch out buddy, we don't have a root package");
 
     String parentName = "";
     for(int i = 0; i < lineage.length-1; i++){
       parentName += lineage[i] + " ";
     }
     parentName = parentName.trim();
     boolean parentFound = false;
     Pubble parent = new Pubble();
     for(Pubble p : pubbleList){
       if(p.getName().trim().equals(parentName)) {
         parent = p;
         parentFound = true;
       }
     }
 
     if(!parentFound){//create it
       String[] parentLineage = parentName.split(" ");
       if(parentLineage.length==1){
         parent = new Pubble(parentName, root);
         root.addChild(parent);
       }
       else{
         Pubble gparent = findParent(parentLineage);
         parent = new Pubble(parentName, gparent);
         gparent.addChild(parent);
       }
     }
     pubbleList.add(parent);
     return parent;
   }
 
   public void prepStructures(){
 
     /*
      * create root Pubble
      * create empty BubbleList
      * create empty MubbleList
      * create langList(?)
      *
      * create Object and String and Array (exception too!) Bubbles
      */
     pubbleList = new ArrayList<Pubble>();
     bubbleList = new ArrayList<Bubble>();
     mubbleList = new ArrayList<Mubble>();
     langList = new ArrayList<Mubble>();
     this.populateLangList();//putting all of java_lang methods into the langList
 
 
     pubbleList.add(new Pubble("Default Package", null));
     //pre-load Object Bubble
     Bubble object = new Bubble("Object");
 
     object.setIsFilled(true);
     object.setIsBuilt(true);
     /*DO WE NEED THIS? HOW ARE WE CONSTRUCTING A VTABLE NOW? */
     //Creating Object's Vtable
     //NEED TO IMPLEMENT IN NEW FASHION
     /*object.add2Vtable("Class __isa;");
       object.add2Vtable("int32_t (*hashCode)(Object);");
       object.add2Vtable("bool (*equals)(Object, Object);");
       object.add2Vtable("Class (*getClass)(Object);");
       object.add2Vtable("String (*toString)(Object);"); */
     bubbleList.add(object);
 
 
     //pre-load String Bubble
     Bubble string = new Bubble("String");
     string.setIsFilled(true);
     string.setIsBuilt(true);
     //Creating Object's Vtable
     /*
       string.add2Vtable("Class __isa;");
       string.add2Vtable("int32_t (*hashCode)(String);");
       string.add2Vtable("bool (*equals)(String, Object);");
       string.add2Vtable("Class (*getClass)(String);");
       string.add2Vtable("String (*toString)(String);");
       string.add2Vtable("int32_t (*length)(String);");
       string.add2Vtable("char (*charAt)(String, int_32_t);"); */
     bubbleList.add(string);
   }
 }
 
