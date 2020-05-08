 package viz;
 import Interpreter.*;
 
 public class NewTest
 {
 	public static void main(String[] args)
 	{
 		VizParser parser = new VizParser(System.in);
 		try
 		{
 			ASTProgram program = (ASTProgram)parser.program();
 
 			RandomizingVisitor rv = new RandomizingVisitor();
 				
 			program.jjtAccept(rv, null);
 		
 			System.out.println("Successfully Parsed");
 			System.out.println("________________\n");
 
 			program.buildCode();
 			System.out.println("Built code");
 			
 			//program.dump("");
 			XAALConnector xc = new XAALConnector(program.getPseudocode(), "foo");
 		
 			for (String s : program.getPseudocode())
 			{
 				System.out.println(s);
 			}
 			System.out.println("\n\n Testing Interpret Visitor");
 			
 			QuestionFactory questionFactory = new QuestionFactory();
 			
 			InterpretVisitor iv = new InterpretVisitor();
 			iv.setXAALConnector(xc);
 			iv.setQuestionFactory(questionFactory);
 			program.jjtAccept(iv, null);
 			System.out.println(Global.getFunction("foo").getParameters().size());
 			System.out.println(Global.getFunction("foo").getSymbolTable().getLocalVariables().size());
			xc.draw("/home/fairfieldt/Documents/!real.xaal");
 						for (String line: program.getPseudocode())
 						{
 							System.out.println(line);
 						}
 
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 	}
 }
