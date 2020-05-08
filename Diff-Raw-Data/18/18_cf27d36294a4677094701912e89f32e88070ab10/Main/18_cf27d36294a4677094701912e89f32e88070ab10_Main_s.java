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
 		Logger.debug("sql -> relational algebra");
		Main.sqlToRelationenAlgebra("select mycol1, mycol2 "
				+ "from table1, table2, table3 " + "where table1.ID = true "
				+ "and table1.ID > table3.fkTwo "
				+ "or table1.ID != table3.fkTwo");
 		// Main.readFile(args[1]);
 	}
 
 	public static void printKundenDB() throws IOException,
 			ClassNotFoundException {
 		File dir = new File(Main.KUNDENDB);
 		FileSystemDatabase.getInstance().setDbDirectory(KUNDENDB);
 		FileSystemDatabase.getInstance().printDb();
 	}
 
 	public static void createKundenDB() {
 		Main.readFile("kundendb.txt");
 	}
 
 	public static void execute(String simpleSQL) {
 		// TODO Anfrage �bersetzen
 		// TODO Anfrage ausf�hren
 	}
 
 	public static ITreeNode sqlToRelationenAlgebra(String simpleSQL) {
 		SimpleSQLParser parser = new SimpleSQLParser(
 				new StringReader(simpleSQL));
 		parser.setDebugALL(true);
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
 		// TODO
 
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
 				Main.execute(strLine);
 			}
 			// Close the input stream
 			in.close();
 		} catch (Exception e) {// Catch exception if any
 			System.err.println("Error: " + e.getMessage());
 		}
 	}
 
 }
