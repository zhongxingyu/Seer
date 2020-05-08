 package main;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import utils.Parser;
 
 public class Menu {
 	
 	KnowledgeBase knowledgeBase = null;
 	Parser parser = null;
 	Backchaining backchaining = null;
 	static boolean trace = false;
 	
 	public Menu()
 	{
 		System.out.println("Main menu");
 		String command = "";
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (command != "quit") {
 			if (!Main.DEBUG) {
 				try {
 					command = br.readLine();
 				} catch (IOException e) {
 					System.err.println("Error reading line");
 				}
 
 				if(command.contains("load"))
 					load(command);
 				else if(command.contains("show"))
 					show();
 				else if(command.contains("trace"))
 					trace();
 				else if(command.contains("why"))
 					why();
 				else if (command.contains("clear"))
 					clear();
 				else
 					query(command);
 			}
 			else {
 				
 				parser = new Parser("");
 				knowledgeBase = parser.getKnowlegeBase();
 				backchaining = new Backchaining(knowledgeBase);
 				knowledgeBase.print();
 				if (backchaining.query("Z"))
 					System.out.println("Yes");
 				else
 					System.out.println("Fail");
 				backchaining.proof();
 				break;
 				
 			}
 		}
 	}
 	
 	private void clear() {
 		parser = new Parser("");
 		knowledgeBase = parser.getKnowlegeBase();
 		backchaining = new Backchaining(knowledgeBase);
 	}
 
 	private void query(String command) {
 		if (backchaining.query(command))
 			System.out.println("Yes");
 		else
 			System.out.println("Fail");
 		trace = false;
 	}
 
 	private void why() {
 		backchaining.proof();
 	}
 
 	private void trace() {
 		trace = true;		
 	}
 
 	private void show() {
 		knowledgeBase.print();
 	}
 
 	public void load(String command) {
 		parser = new Parser(command.substring(command.indexOf(" ")+1));
 		knowledgeBase = parser.getKnowlegeBase();
 		backchaining = new Backchaining(knowledgeBase);
 	}
 	
 	
 
 }
