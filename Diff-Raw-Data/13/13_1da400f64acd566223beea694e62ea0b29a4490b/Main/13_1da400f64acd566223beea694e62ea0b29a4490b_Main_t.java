 package main;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 
 import parser.gene.ParseException;
 import parser.gene.SimpleSQLParser;
 import parser.syntaxtree.CompilationUnit;
 import parser.visitor.ObjectDepthFirst;
 import relationenalgebra.ITreeNode;
 import relationenalgebra.TableOperation;
 import database.FileSystemDatabase;
 
 public class Main {
 
 	// Verzeichnis der Buchversandsdatenbank
 	public static final String KUNDENDB = "db";
 
 	public static void main(String[] args) throws IOException,
 			ClassNotFoundException {
 		Logger.debug = true;
 		Logger.debug("DEBUGGING IS ENABLED");
 		Logger.debug("load database");
 		FileSystemDatabase.getInstance().setDbDirectory(KUNDENDB);
 		//Main.createKundenDB();
 		Logger.debug("execute sql");
 		Main.execute("select B.Titel, BA.id from Buch_Autor as BA, Buch as B where BA.Autorenname=\"Frank Schätzing\" and BA.B_ID=B.ID");
 		// Main.readFile("sql.txt");
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
				if (strLine != "\n")
					Main.execute(strLine);
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			throw new RuntimeException(e);
 		}
 	}
 
 }
