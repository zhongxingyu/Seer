 package main;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 
 import optimization.CascadeSelects;
 import optimization.DetectJoins;
 import optimization.IOptimization;
 import optimization.MoveProjection;
 import optimization.MoveSelection;
 import parser.gene.ParseException;
 import parser.gene.SimpleSQLParser;
 import parser.syntaxtree.CompilationUnit;
 import parser.visitor.ObjectDepthFirst;
 import relationenalgebra.CrossProduct;
 import relationenalgebra.IOneChildNode;
 import relationenalgebra.ITreeNode;
 import relationenalgebra.ITwoChildNode;
 import relationenalgebra.Join;
 import relationenalgebra.Projection;
 import relationenalgebra.Relation;
 import relationenalgebra.Selection;
 import relationenalgebra.TableOperation;
 import database.FileSystemDatabase;
 import database.Table;
 
 public class Main {
 
     // Verzeichnis der Buchversandsdatenbank
     public static final String KUNDENDB = "db";
 
     public static void main(String[] args) throws IOException,
             ClassNotFoundException {
         //Logger.debug = true;
         //Logger.debug("DEBUGGING IS ENABLED");
         Logger.debug("load database");
         FileSystemDatabase.getInstance().setDbDirectory(KUNDENDB);
         Main.createKundenDB();
         Logger.debug("execute sql");
         //Main.execute("select B.Titel from Buch_Autor as BA, Buch as B where BA.Autorenname=\"Christian Ullenboom\" and BA.B_ID=B.ID");
         //Main.execute("select B.Titel from Buch_Autor as BA, Buch as B where BA.Autorenname=\"Henning Mankell\" and BA.B_ID=B.ID");
         //Main.execute("select B.Titel from Buch as B, Kunde as K, Buch_Bestellung as BB, Kunde_Bestellung as KB where K.Name=\"KName1\" and K.ID=KB.K_ID and KB.B_ID=BB.Be_ID and BB.Bu_ID=B.ID");
 
         //Main.readFile("sql.txt");
         Main.blockTwoOptimizationDemo();
 
         //Main.printKundenDB();
         //FileSystemDatabase.getInstance().persistDb();
     }
 
     public static void blockTwoOptimizationDemo() {
         String[] queries = new String[]{
             "select B.Titel \n" +
             "from \n" +
             "	Buch as B, \n" +
             "	Kunde as K, \n" +
             "	Buch_Bestellung as BB,\n" +
             "	Kunde_Bestellung as KB \n" +
             "where \n" +
             "	K.Name=\"KName1\" and \n" +
             "	K.ID=KB.K_ID and \n" +
             "	KB.B_ID=BB.Be_ID and \n" +
             "	BB.Bu_ID=B.ID",
 
             "select B.ID, K.Name \n" +
             "from\n" +
             "	Bestellung as B, \n" +
             "	Kunde as K, \n" +
            "	Kunde_Bestellung as KB, \n" +
             "where \n" +
             "	KB.K_ID=K.ID and \n" +
             "	KB.B_ID=B.ID and \n" +
             "	B.ID=\"Bestellung5\"",
 
             "select Name \n" +
             "from \n" +
             "	Kunde, \n" +
             "	Kunde_Bestellung \n" +
             "where \n" +
             "	ID=K_ID and \n" +
             "	Name=\"KName1\"",
         };
 
         IOptimization cascadeSelects = new CascadeSelects();
         IOptimization detectJoins = new DetectJoins();
         IOptimization moveSelection = new MoveSelection();
         IOptimization moveProjection = new MoveProjection();
 
         String[] titles = new String[]{
                 "Result with no optimizations: ",
                 "Result with cascaded and moved selections: ",
                 "Result with cascaded and moved selections and detected joins: ",
                 "Result with cascaded and moved selections, detected joins and moved projections: ",
         };
 
         IOptimization[][] optimizationLists = new IOptimization[][]{
                 new IOptimization[0],
                 new IOptimization[]{ cascadeSelects, moveSelection },
                 new IOptimization[]{ cascadeSelects, moveSelection, detectJoins },
                 new IOptimization[]{ cascadeSelects, moveSelection, detectJoins, moveProjection },
         };
 
         for (String query : queries) {
             System.out.println("Next query: ");
             System.out.println(query);
             System.out.println();
 
             for (int i = 0; i < titles.length; i++) {
                 System.out.println(titles[i]);
                 Table result = executeOptimized(query, optimizationLists[i]);
                 if (result == null) {
                     System.out.println("(no result, optimization failed)");
                 } else {
                     System.out.println(result.toString());
                 }
                 System.out.println();
             }
         }
     }
 
     private static Table executeOptimized(String query, IOptimization[] optimizations) {
         ITreeNode plan = sqlToRelationenAlgebra(query);
         if (plan == null) {
             System.err.println("failed to parse query");
             return null;
         }
 
         System.out.println("parsed plan: ");
         System.out.println(plan.toString());
         for (IOptimization optimization : optimizations) {
             try {
                 plan = optimization.optimize(plan);
             } catch (Exception e) {
                 System.err.println("failed to optimize query using "+optimization);
                 System.err.println(e.getMessage());
                 e.printStackTrace();
                 return null;
             }
         }
         System.out.println("optimized plan:");
         System.out.println(plan);
         try {
             return executeQuery(plan);
         } catch (Exception e) {
             System.err.println("failed to execute query");
             System.err.println(e.getMessage());
             e.printStackTrace();
             return null;
         }
     }
 
 
     public static void printKundenDB() throws IOException,
             ClassNotFoundException {
         FileSystemDatabase.getInstance().printDb();
     }
 
     public static void createKundenDB() {
         Logger.debug("create kunden db");
         Main.readFile("kundendb.txt");
     }
 
     public static void execute(String simpleSQL) {
         ITreeNode plan = Main.sqlToRelationenAlgebra(simpleSQL);
         Main.executePlan(plan);
     }
 
     public static ITreeNode sqlToRelationenAlgebra(String simpleSQL) {
         SimpleSQLParser parser = new SimpleSQLParser(
                 new StringReader(simpleSQL));
         parser.setDebugALL(Logger.debug);
         Logger.debug("parsing: "+simpleSQL);
         CompilationUnit cu = null;
         try {
             cu = parser.CompilationUnit();
             ObjectDepthFirst v = new ObjectDepthFirst();
             cu.accept(v, null);
         } catch (ParseException e) {
             System.err.println(e.getMessage());
             return null;
         }
 
         return (ITreeNode) cu.accept(new AlgebraVisitor(), null);
     }
 
     private static void executePlan(ITreeNode plan) {
         if (plan instanceof TableOperation)
             ((TableOperation) plan).execute();
         else {
             Logger.debug("QUERY: "+plan);
             Table result = executeQuery(plan);
             Logger.debug("QUERY RESULT: ");
             Logger.debug(result.toString());
         }
     }
 
     private static Table executeQuery(ITreeNode query) {
         if (query instanceof ITwoChildNode) {
             Table child1Result = executeQuery(((ITwoChildNode)query).getChild());
             Table child2Result = executeQuery(((ITwoChildNode)query).getSecondChild());
             if (query instanceof CrossProduct)
                 return child1Result.cross(child2Result);
             if (query instanceof Join)
                 return child1Result.join(child2Result, ((Join)query).getExpr());
         } else if (query instanceof IOneChildNode) {
             Table childResult = executeQuery(((IOneChildNode)query).getChild());
             if (query instanceof Projection)
                 return childResult.projectTo(((Projection)query).getColumnnames());
             if (query instanceof Selection)
                 return childResult.select(((Selection)query).getExpr());
         } else if (query instanceof Relation) {
             Relation r = (Relation)query;
             Table t = FileSystemDatabase.getInstance().getTable(r.getName());
             t.setAlias(r.getAlias());
             return t;
         }
         throw new IllegalArgumentException("unknown node type: "+query);
     }
 
     private static void readFile(String filename) {
         File f = new File(filename);
         if (!f.isFile())
             return;
         try {
             // Open the file that is the first
             // command line parameter
             FileInputStream fstream = new FileInputStream(filename);
             // Get the object of DataInputStream
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             // Read File Line By Line
             while ((strLine = br.readLine()) != null) {
                 // Print the content on the console
                 if (!strLine.equals("\n") && !strLine.equals(""))
                     Main.execute(strLine);
             }
             // Close the input stream
             in.close();
         } catch (Exception e) {// Catch exception if any
             throw new RuntimeException(e);
         }
     }
 
 }
