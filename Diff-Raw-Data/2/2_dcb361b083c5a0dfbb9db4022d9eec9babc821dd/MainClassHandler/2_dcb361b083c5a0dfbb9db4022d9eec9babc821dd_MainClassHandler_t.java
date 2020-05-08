 package semant;
 
 import symbol.ClassInfo;
 import symbol.Symbol;
 import symbol.VarInfo;
 import syntaxtree.IntegerLiteral;
 import syntaxtree.IntegerType;
 import syntaxtree.MainClass;
 import syntaxtree.Program;
 import syntaxtree.VisitorAdapter;
 
 public class MainClassHandler extends VisitorAdapter {
 
 	private Env env;
 
 	private MainClassHandler(Env e) {
 		super();
 
 		env = e;
 	}
 
 	static void firstPass(Env e, MainClass mainClass) {
 
 		MainClassHandler h = new MainClassHandler(e);
 
 		mainClass.accept(h);
 
 	}
 
 	public void visit(MainClass node) {
 
 		// Cria a classe Main
 		Symbol className = Symbol.symbol(node.className.s);
 		ClassInfo info = new ClassInfo(className);
 
 		// Verifica se ja existe a classe com esse nome
 		if (!env.classes.put(className, info)) {
 			env.err.Error(node, new Object[] { "Nome de classe redefinido.",
 					"Simbolo: " + className });
 		}
 
 		// adiciona var of main. Adicionado com o tipo Integer apesar de ser
 		// String[]
 		Symbol mainArgName = Symbol.symbol(node.mainArgName.s);
 		VarInfo v = new VarInfo(new IntegerType(node.mainArgName.line,
 				node.mainArgName.row), mainArgName);
 
 		if (!info.addAttribute(v)) {
 
 			VarInfo old = info.attributes.get(mainArgName);
 
 			env.err.Error(node.mainArgName, new Object[] {
 					"Atributo \'" + mainArgName
 							+ "\' redeclarado para a classe \'" + info.name
 							+ "\'",
 					"Declaracao anterior aqui: [" + old.type.line + ","
 							+ old.type.row + "]" });
 
 		}
 		
		System.out.println("oi");
		
 		//Trata o Statement
 		StatementHandler.firstPass(env, info, node.s);
 
 		
 	}
 }
