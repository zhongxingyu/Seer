 package cs4120.der34dlc287lg342.xi.tests;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import cs4120.der34dlc287lg342.xi.XiParser;
 import cs4120.der34dlc287lg342.xi.assembly.Assembler;
 import cs4120.der34dlc287lg342.xi.assembly.Assembly;
 import cs4120.der34dlc287lg342.xi.assembly.RegAlloc;
 import cs4120.der34dlc287lg342.xi.ast.AbstractSyntaxTree;
 import cs4120.der34dlc287lg342.xi.cfg.AssemblyCFG;
 import cs4120.der34dlc287lg342.xi.cfg.AvailableExpressions;
 import cs4120.der34dlc287lg342.xi.cfg.CFG;
 import cs4120.der34dlc287lg342.xi.cfg.CSE;
 import cs4120.der34dlc287lg342.xi.cfg.InterferenceGraph;
 import cs4120.der34dlc287lg342.xi.cfg.LivenessWorklist;
 import cs4120.der34dlc287lg342.xi.cfg.Rewrite;
 import cs4120.der34dlc287lg342.xi.ir.Binop;
 import cs4120.der34dlc287lg342.xi.ir.Const;
 import cs4120.der34dlc287lg342.xi.ir.Func;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.Stmt;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 import cs4120.der34dlc287lg342.xi.ir.translate.ConstantFolding;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.LowerCjump;
 import cs4120.der34dlc287lg342.xi.tiles.SeqTile;
 import cs4120.der34dlc287lg342.xi.tiles.Tile;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 import cs4120.der34dlc287lg342.xi.typechecker.XiTypechecker;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.parser.Parser;
 import junit.framework.TestCase;
 
 public class TestCFG extends TestCase {
 	
 	public Seq gen(String code){
 		Reader reader = new StringReader(code);
 		Parser p = new XiParser(reader);
 		AbstractSyntaxNode ast = p.parse();
 		XiTypechecker tc;
 		try {
 			tc = new XiTypechecker(ast, code);
 		} catch (InvalidXiTypeException e1) {
 			fail(e1.getMessage());
 			return null;
 		}
 		tc.typecheck();
 		((AbstractSyntaxTree)tc.ast).foldConstants();
 		try {
 			IRTranslation tr = ((AbstractSyntaxTree)tc.ast).to_ir(new IRContextStack());
 			return LowerCjump.translate(tr.stmt().lower());
 		} catch (InvalidIRContextException e) {
 			fail(e.getMessage());
 		}
 		return null;
 	}
 	
 	public Seq gen(Reader r){
 		String code = "";
 		try {
 			while (r.ready()){
 				char[] buf = new char[1024];
 				r.read(buf);
 				code += new String(buf);
 			}
 		} catch (IOException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 		Reader reader = new StringReader(code);
 		Parser p = new XiParser(reader);
 		AbstractSyntaxNode ast = p.parse();
 		XiTypechecker tc;
 		try {
 			tc = new XiTypechecker(ast, code);
 		} catch (InvalidXiTypeException e1) {
 			fail(e1.getMessage());
 			return null;
 		}
 		tc.typecheck();
 		((AbstractSyntaxTree)tc.ast).foldConstants();
 		try {
 			IRTranslation tr = ((AbstractSyntaxTree)tc.ast).to_ir(new IRContextStack());
 			return LowerCjump.translate(tr.stmt().lower());
 		} catch (InvalidIRContextException e) {
 			fail(e.getMessage());
 		}
 		return null;
 	}
 	
 	public void testMeh(){
 		File[] valid = new File("2009-contest").listFiles();
 		
 		for (File validFile: valid) {
 			Seq f = null;
 			Tile t = null;
 			try{
 			if (validFile.getName().contains(".xi")){
 				//System.out.println(validFile.getName());
 				System.out.println("./linkxi.sh -o "+validFile.getName().replace(".xi", "")+" "+validFile.getName().replace("xi", "s"));
 				System.out.println("./"+validFile.getName().replace(".xi", "")+" > "+validFile.getName().replace("xi", "act"));
 				System.out.println("diff "+validFile.getName().replace("xi", "exp")+" "+validFile.getName().replace("xi", "act"));
 				Reader reader = null;
 				
 				reader = new FileReader(validFile.getPath());
 				
 				Seq stmt = gen(reader);
 				stmt = ConstantFolding.foldConstants(stmt);
 				f = stmt;
 		//		System.out.println(func.prettyPrint());
 		//		CFG cfg = CFG.cfg(func);
 		//		AvailableExpressions ae = new AvailableExpressions(cfg);
 		//		ae.analyze();
 		//		CSE cse = new CSE(cfg);
 		//		cse.analyze();
 				t = stmt.munch();
 				Assembler assembler = new Assembler((SeqTile) t);
 				String att = assembler.att();
 				
 				FileWriter fstream = new FileWriter("2009-contest/"+validFile.getName().replace("xi", "s"));
 				BufferedWriter out = new BufferedWriter(fstream);
 				out.write(att);
 				out.close();
 			}
 			} catch (Exception e){
 				
 				e.printStackTrace();
 				System.out.println(f.prettyPrint());
 				System.out.println(t);
 				break;
 			}
 		}
 	}
 	
 	public void testCFG(){
 		Seq stmt = gen("use io use conv main(args:int[][]){a:int = 0 b:int = 1 c:int = 3 if (a == 3) {c = a + 3; b = (a + 3) * 4; a = b + c}}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		Func func = (Func) stmt.children.get(0);
 //		System.out.println(func.prettyPrint());
 		CFG cfg = CFG.cfg(func);
 		AvailableExpressions ae = new AvailableExpressions(cfg);
 		ae.analyze();
 		CSE cse = new CSE(cfg);
 		cse.analyze();
 		System.out.println(cfg.dot_edge());
 		
 //		TempRegister r = new TempRegister();
 //		Stmt a = new Move(new Temp(r), new Binop(Binop.MINUS, new Binop(Binop.PLUS, new Temp(r), new Const(3)), new Const(4)));
 //		System.out.println(a);
 //		a.replace(new Binop(Binop.PLUS, new Temp(r), new Const(3)), new Temp(new TempRegister("r")));
 //		System.out.println(a);
 	}
 	
 //	public void testWorklist() {
 //		Seq stmt = gen("main(a:int){b:int c:int d:int b=a+a c=a+a d=c+b if(d<b) {d=a+1} else {d=c+1}}");
 //		stmt = ConstantFolding.foldConstants(stmt);
 //		Func func = (Func) stmt.children.get(0);
 //		
 //		CFG cfg = func.cfg();
 //
 ////		System.out.println(cfg);
 //		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //		
 //		LivenessWorklist wl = new LivenessWorklist(cfg);
 //		wl.analyze();
 //		InterferenceGraph g = new InterferenceGraph(cfg);
 //		System.out.println(g.adjacent);
 //		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 ////		System.out.println(cfg);
 ////		for (CFG node : wl.worklist) {
 ////			System.out.println(node.simpleName(node));
 ////		}
 //	}
 	
 	public void testWorklist(){
		Seq stmt = gen("use io use conv main(args:int[][]){a:int,b:int = f()} f():int,int{return 1, 2}");
 		stmt = ConstantFolding.foldConstants(stmt);
		System.out.println(stmt.prettyPrint());
 		Assembler assembler = new Assembler((SeqTile) stmt.munch());
 		System.out.println(assembler.att());
 //		Func func = (Func) stmt.children.get(0);
 //		//System.out.println(func.prettyPrint());
 //		ArrayList<Assembly> instrs = func.munch().att();
 //		CFG cfg = CFG.cfg(instrs);
 //		//System.out.println(cfg);
 //		//System.out.println(cfg.asm());
 //		
 //		LivenessWorklist wl = new LivenessWorklist(cfg);
 //		wl.analyze();
 //		InterferenceGraph g = new InterferenceGraph(cfg);
 //		
 //		int n = 0;
 //		while (!g.spills.isEmpty()){
 //			
 //			Rewrite rewrite = new Rewrite(instrs, g.spills, n);
 //			n += g.spills.size();
 //			instrs = rewrite.rewrite();
 //			cfg = CFG.cfg(instrs);
 //			wl = new LivenessWorklist(cfg);
 //			wl.analyze();
 //			//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //			g = new InterferenceGraph(cfg);
 //		}
 //
 //		System.out.println(RegAlloc.allocate(instrs, g.coloring));
 		//System.out.println(cfg.dot_edge(new HashSet<CFG>(), g.coloring));
 		//System.out.println(g.dot_edge());
 		
 		//System.out.println(g.adjacent);
 		//System.out.println(cfg.dot_edge(new HashSet<CFG>()));
 //		System.out.println(cfg);
 //		for (CFG node : wl.worklist) {
 //			System.out.println(node.simpleName(node));
 //		}
 	}
 }
