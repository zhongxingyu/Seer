 package core;
 
 import java.util.HashSet;
 
 public class Main {
 	private HashSet<FunctionalDependency> fdList;
 	private HashSet<Relation> rList;
 	private Printer printer;
 	
 	public Main() {
 		fdList = FileParser.parseFunctionalDependencies("./files/test.txt");
 		printer = new Printer();
 	
 		printer.printFunctionalDependencies(fdList);
 		
 		
 		HashSet<String> proj = new HashSet<String>();
		proj.add("X");
		proj.add("Y");
 		
 		System.out.println("\n\n");
 		printer.printFunctionalDependencies(Projecter.projDependenciesByAttributes(proj, fdList));
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		new Main();
 	}
 
 }
