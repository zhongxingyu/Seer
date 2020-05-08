 import Interpreter.*;
 import viz.*;
 import java.io.*;
 
 public class ByName
 {
 	public static void main(String[] args)
 	{
 		System.out.println("Starting");
 		Global.InterpreterType = InterpreterTypes.BY_NAME;
 		BufferedReader br = null;
 		try
 		{
			br = new BufferedReader(new FileReader("Samples/break.src"));
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 		VizParser parser = new VizParser(br);
 		try
 		{
 			ASTProgram program = (ASTProgram)parser.program();
 
 			RandomizingVisitor2<ByNameVariable> rv = new RandomizingVisitor2<ByNameVariable>(ByNameVariable.class);
 				
			//	program.jjtAccept(rv, null);
 		
 			System.out.println("Successfully Parsed");
 			System.out.println("________________\n");
 			
 			program.buildCode();
 			String[] initial = {"1. The Arguments are copied into the function.", "2. The program is run (click next to continue)."};
 			XAALConnector xc = new XAALConnector(initial, "Call by Name", true);
 			String p0 = xc.addCodePage(program.getPseudocode());
 			Global.currentPage = p0;
 							xc.showCodePage(p0);
 			xc.startSnap(1);
 				xc.startPar();
 				xc.showCodePage(p0);
 				xc.endPar();
 			xc.endSnap();
 			xc.startSnap(2);
 			xc.startPar();
 				xc.showCodePage(p0);
 			xc.endPar();			
 			xc.startPar();
 			QuestionFactory questionFactory = new QuestionFactory();
 			
 			
 			ByNameVisitor0 bm = new ByNameVisitor0();
 			bm.setXAALConnector(xc);
 
 			program.jjtAccept(bm, null);
 			Global.lineNumber = 1;
 			program.codeBuilt = false;
 			program.buildCode();
 			String p2 = xc.addCodePage(program.getPseudocode());
 			xc.swapCodePage(p0, p2);
 			Global.executing = true;
 			xc.endPar();
 			xc.endSnap();
 			
 			
 			Global.lineNumber = 1;
 			program.codeBuilt = false;
 			program.buildCode();
 			xc.startSnap(1,program.getPseudocode());
 			xc.startPar();
 			xc.hideCodePage(p2);
 			xc.endPar();
 
 			
 			
 			
 			ByNameInterpretVisitor iv = new ByNameInterpretVisitor();
 			iv.setXAALConnector(xc);
 			iv.setQuestionFactory(questionFactory);
 			program.jjtAccept(iv, null);
 			xc.draw(args[0]);
 			System.out.println("Visualization file created");
 
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 	}
 }
