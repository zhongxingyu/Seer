 package main;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 
 import optimization.CascadeSelects;
 
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
 import test.TreeNodeTester;
 import database.FileSystemDatabase;
 import database.Table;
 
 public class Main {
 
 	// Verzeichnis der Buchversandsdatenbank
 	public static final String KUNDENDB = "db";
 
 	public static void main(String[] args) throws IOException,
 			ClassNotFoundException {
 		Logger.debug = true;
 		Logger.debug("DEBUGGING IS ENABLED");
 		Logger.debug("load database");
 		FileSystemDatabase.getInstance().setDbDirectory(KUNDENDB);
 		Main.createKundenDB();
 		Logger.debug("execute sql");
 		//Main.execute("select B.Titel from Buch_Autor as BA, Buch as B where BA.Autorenname=\"Christian Ullenboom\" and BA.B_ID=B.ID");
 		//Main.execute("select B.Titel from Buch_Autor as BA, Buch as B where BA.Autorenname=\"Henning Mankell\" and BA.B_ID=B.ID");
 		Main.execute("select B.Titel from Buch as B, Kunde as K, Buch_Bestellung as BB, Kunde_Bestellung as KB where K.Name=\"KName1\" and K.ID=KB.K_ID and KB.B_ID=BB.Be_ID and BB.Bu_ID=B.ID");
 		
 		//Main.readFile("sql.txt");
 		
 		
		Main.printKundenDB();
		FileSystemDatabase.getInstance().persistDb();
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
