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
		proj.add("A");
		proj.add("B");
		proj.add("C");
		proj.add("D");
 		
 		System.out.println("\n\n");
 		printer.printFunctionalDependencies(Projecter.projDependenciesByAttributes(proj, fdList));
 	}
 	
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		new Main();
 	}
 
 }
