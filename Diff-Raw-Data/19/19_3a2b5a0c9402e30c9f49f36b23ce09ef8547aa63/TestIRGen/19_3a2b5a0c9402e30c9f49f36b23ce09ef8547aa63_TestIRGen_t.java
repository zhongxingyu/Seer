 package cs4120.der34dlc287lg342.xi.tests;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 
 import cs4120.der34dlc287lg342.xi.XiParser;
 import cs4120.der34dlc287lg342.xi.ast.AbstractSyntaxTree;
 import cs4120.der34dlc287lg342.xi.ir.Binop;
 import cs4120.der34dlc287lg342.xi.ir.Call;
 import cs4120.der34dlc287lg342.xi.ir.Cjump;
 import cs4120.der34dlc287lg342.xi.ir.Const;
 import cs4120.der34dlc287lg342.xi.ir.Exp;
 import cs4120.der34dlc287lg342.xi.ir.Expr;
 import cs4120.der34dlc287lg342.xi.ir.Func;
 import cs4120.der34dlc287lg342.xi.ir.Jump;
 import cs4120.der34dlc287lg342.xi.ir.LabelNode;
 import cs4120.der34dlc287lg342.xi.ir.Mem;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Name;
 import cs4120.der34dlc287lg342.xi.ir.Return;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.Stmt;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 import cs4120.der34dlc287lg342.xi.ir.context.IRContextStack;
 import cs4120.der34dlc287lg342.xi.ir.context.InvalidIRContextException;
 import cs4120.der34dlc287lg342.xi.ir.context.Label;
 import cs4120.der34dlc287lg342.xi.ir.context.TempRegister;
 import cs4120.der34dlc287lg342.xi.ir.translate.ConstantFolding;
 import cs4120.der34dlc287lg342.xi.ir.translate.IRTranslation;
 import cs4120.der34dlc287lg342.xi.ir.translate.LowerCjump;
 import cs4120.der34dlc287lg342.xi.typechecker.InvalidXiTypeException;
 import cs4120.der34dlc287lg342.xi.typechecker.XiTypechecker;
 import edu.cornell.cs.cs4120.util.VisualizableTreeNode;
 import edu.cornell.cs.cs4120.xi.AbstractSyntaxNode;
 import edu.cornell.cs.cs4120.xi.parser.Parser;
 import junit.framework.TestCase;
 
 public class TestIRGen extends TestCase {
 	public Label l = new Label();
 	public LabelNode label = new LabelNode(l);
 	public TempRegister r = new TempRegister();
 	public Temp temp = new Temp(r); 
 	public Return ret = new Return();
 	public Const c = new Const(0);
 	public Name name = new Name(l);
 	public Temp reg = temp;
 	public Temp reg(String s){
 		return new Temp(new TempRegister(s));
 	}
 	public Temp reg(int s){
 		return temp;
 	}
 	public Label label(String s){
 		return l;
 	}
 	
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
 	
 	public void lookslike(Seq stmt, Seq expected){
 		int i = 0, n = ((ArrayList<VisualizableTreeNode>)expected.children()).size();
 		for (VisualizableTreeNode child : stmt.children()){
 			try{
 				lookslike((Stmt)child, (Stmt)((ArrayList<VisualizableTreeNode>)expected.children()).get(i++));
 			} catch (IndexOutOfBoundsException e){
 				fail(e.getMessage());
 			}
 		}
 		if (i<n-1)
 			fail("Too many expected stmts");
 	}
 	
 	public void lookslike(Stmt stmt, Stmt expected){
 		Field[] fields = stmt.getClass().getDeclaredFields();
 		assertEquals(""+stmt, (expected).getClass().getSimpleName(), stmt.getClass().getSimpleName());
 		for (Field field : fields){
 			field.setAccessible(true);
 			Object o = null;
 			try {
 				o = field.get(stmt);
 			} catch (Exception e) {
 				System.out.println(field.getName());
 				continue;
 			}
 			if (o instanceof Stmt){
 				Object o2 = null;
 				try {
 					o2 = field.get(expected);
 				} catch (Exception e) {
 					fail("??");
 				}
 				
 				if (o2 == null)
 					fail("The impossible happened");
 				
 				lookslike((Stmt)o, (Stmt)o2);
 			} else if (o instanceof Expr){
 				Object o2 = null;
 				try {
 					o2 = field.get(expected);
 				} catch (Exception e) {
 					fail("??");
 				}
 				
 				if (o2 == null)
 					fail("The impossible happened");
 				
 				lookslike((Expr)o, (Expr)o2);
 			}
 		}
 	}
 	
 	private void lookslike(Expr expr, Expr expected) {
 		Field[] fields = expr.getClass().getDeclaredFields();
 		assertEquals(expr.toString(), (expected).getClass().getSimpleName(), expr.getClass().getSimpleName());
 		for (Field field : fields){
 			field.setAccessible(true);
 			Object o = null;
 			try {
 				o = field.get(expr);
 			} catch (Exception e) {
 				System.out.println(field.getName());
 				continue;
 			}
 			if (o instanceof Stmt){
 				Object o2 = null;
 				try {
 					o2 = field.get(expected);
 				} catch (Exception e) {
 					fail("??");
 				}
 				
 				if (o2 == null)
 					fail("The impossible happened");
 				
 				lookslike((Stmt)o, (Stmt)o2);
 			} else if (o instanceof Expr){
 				Object o2 = null;
 				try {
 					o2 = field.get(expected);
 				} catch (Exception e) {
 					fail("??");
 				}
 				
 				if (o2 == null)
 					fail("The impossible happened");
 				
 				lookslike((Expr)o, (Expr)o2);
 			} else if (o instanceof ArrayList<?>){
 				Object o2 = null;
 				try {
 					o2 = field.get(expected);
 				} catch (Exception e) {
 					fail("??");
 				}
 				
 				if (o2 == null)
 					fail("The impossible happened");
 				
 				ArrayList<?> a1 = (ArrayList<?>)o, a2 = (ArrayList<?>)o2;
 				
 				int i = 0, n = a2.size();
 				for (Object child : a1){
 					try{
 						lookslike((Expr)child, (Expr)a2.get(i++));
 					} catch (IndexOutOfBoundsException e){
 						fail(e.getMessage());
 					}
 				}
 				if (i<n-1)
 					fail("Too many expected stmts");
 			}
 		}
 	}
 
 	public String islike(Seq stmt){
 		String seq = "new Seq(\n";
 		for (VisualizableTreeNode child : stmt.children()){
 			try{
 				if (child instanceof Func)
 					seq += "\t"+islike((Func)child)+",\n";
 				else
 					seq += "\t"+islike((Stmt)child)+",\n";
 			} catch (IndexOutOfBoundsException e){
 				fail(e.getMessage());
 			}
 		}
 		return seq.substring(0, seq.length()-2)+"\n)";
 	}
 	
 	public String islike(Func stmt){
 		String seq = "new Func(new Label(\""+stmt.name+"\"),\n";
 		for (VisualizableTreeNode child : stmt.children()){
 			try{
 				seq += "\t\t"+islike((Stmt)child)+",\n";
 			} catch (IndexOutOfBoundsException e){
 				fail(e.getMessage());
 			}
 		}
 		return seq.substring(0, seq.length()-2)+"\n\t)";
 	}
 	
 	public String islike(Stmt stmt){
 		if (stmt instanceof Return)
 			return "ret";
 		Field[] f = stmt.getClass().getDeclaredFields();
 		ArrayList<Field> fields = new ArrayList<Field>();
 		for (Field f_ : f) fields.add(f_);
 		Constructor<?>[] cs = stmt.getClass().getConstructors();
 		Class<?>[] args = {};
 		for (Constructor<?> c : cs){
 			Class<?>[] args_ = c.getParameterTypes();
 			if (args_.length > args.length)
 				args = args_;
 		}
 		//System.out.println(stmt);
 		String s = "new "+stmt.getClass().getSimpleName()+"(";
 		for (Class<?> arg : args){
 			for (Field field : fields){
 				if (field.getName().equals("primitive"))
 					continue;
 				if (arg == field.getType()){
 					//System.out.println(arg.getSimpleName() +": "+ field.getName());
 					fields.remove(field);
 					field.setAccessible(true);
 					Object o = null;
 					try {
 						o = field.get(stmt);
 					} catch (Exception e) {}
 					if (o instanceof Stmt){
 						s += islike((Stmt)o);
 					} else if (o instanceof Expr){
 						s += islike((Expr)o);
 					} else if (o instanceof Integer || o instanceof Boolean) {
 						s += o;
 					} else if (o instanceof Label) {
 						Label l = ((Label)o);
 						s += "label(\""+l.name+"\")";
 					} else if (o instanceof TempRegister) {
 						s += "new Register(\""+((TempRegister)o).name+"\")";
 					} else{
 						System.out.println(arg.getSimpleName() +": "+ field.getName());
 					}
 					s += ",";
 					break;
 				}
 			}
 			
 		}
 		return s.substring(0,s.length()-1)+")";
 	}
 	
 	private String islike(Binop op){
 		String s = "new Binop(Binop.";
 		Field[] fields = op.getClass().getDeclaredFields();
 		for (Field f : fields){
 			if (Modifier.isStatic(f.getModifiers())){
 				try {
 					Object o = f.get(op);
 					if (o.equals(op.op)){
 						s += f.getName()+",";
 					}
 				} catch (Exception e) {}
 			}
 		}
 		s += islike(op.left)+","+islike(op.right)+")";
 		return s;
 	}
 	
 	private String islike(Expr expr) {
 		if (expr instanceof Binop)
 			return islike((Binop)expr);
 		
 		Field[] f = expr.getClass().getDeclaredFields();
 		ArrayList<Field> fields = new ArrayList<Field>();
 		for (Field f_ : f) fields.add(f_);
 		Constructor<?>[] cs = expr.getClass().getConstructors();
 		Class<?>[] args = {};
 		for (Constructor<?> c : cs){
 			Class<?>[] args_ = c.getParameterTypes();
 			if (args_.length > args.length && !c.isVarArgs())
 				args = args_;
 		}
 		//System.out.println(expr);
 		String s = "new "+expr.getClass().getSimpleName()+"(";
 		if (expr instanceof Temp)
 			s = "reg";
 		
 		for (Class<?> arg : args){
 			for (Field field : fields){
 				//System.out.println(arg.getSimpleName() +": "+ field.getName());
 				if (arg == field.getType()){
 					//System.out.println(arg.getSimpleName() +": "+ field.getName());
 					fields.remove(field);
 					field.setAccessible(true);
 					Object o = null;
 					try {
 						o = field.get(expr);
 					} catch (Exception e) {e.printStackTrace();}
 					if (o instanceof Stmt){
 						s += islike((Stmt)o);
 					} else if (o instanceof Expr){
 						s += islike((Expr)o);
 					} else if (o instanceof Integer || o instanceof Boolean) {
 						s += o;
 					} else if (o instanceof Label) {
 						Label l = ((Label)o);
 						s += "label(\""+l.name+"\")";
 					} else if (o instanceof TempRegister) {
 						TempRegister r = ((TempRegister)o);
 						s += r.name.equals(""+r.value)? "("+r.name+")" : "(\""+r.name+"\")";
 						return s;
 					} else if (o instanceof ArrayList<?>) {
 						ArrayList<?> r = (ArrayList<?>)o;
 						for (Object e : r)
 							s += islike((Expr)e)+",";
 						s = s.substring(0, s.length()-1);
 					} else{
 						System.out.println(arg.getSimpleName() +": "+ field.getName());
 					}
 					s += ",";
 					break;
 				}
 			}
 			
 		}
 		
 		return s.substring(0,s.length()-1)+")";
 	}
 	
 	public void testIRConstruction(){
 		Seq stmt = gen("use io main(){j:int[][] = ((0,0,0), (0,0,0)) a:int, b:int, c:int, d:int, e:int, f:int, g:int, h:bool = func(1)} func(a:int):int,int,int,int,int,int,int,bool{return 1,2,3,4,5,6,7,(true)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Func(new Label("_Imain_p"),
 				new Move(reg(56),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(2),new Const(1)),new Const(3)))),
 				new Move(reg(42),reg(56)),
 				new Move(new Mem(reg(42)),new Const(2)),
 				new Move(reg(41),new Binop(Binop.PLUS,reg(42),new Const(8))),
 				new Move(reg(57),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(3),new Const(1)),new Const(3)))),
 				new Move(reg(44),reg(57)),
 				new Move(new Mem(reg(44)),new Const(3)),
 				new Move(reg(43),new Binop(Binop.PLUS,reg(44),new Const(8))),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(43),new Const(0))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(43),new Const(8))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(43),new Const(16))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(41),new Const(0))),reg(43)),
 				new Move(reg(58),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(3),new Const(1)),new Const(3)))),
 				new Move(reg(46),reg(58)),
 				new Move(new Mem(reg(46)),new Const(3)),
 				new Move(reg(45),new Binop(Binop.PLUS,reg(46),new Const(8))),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(45),new Const(0))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(45),new Const(8))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(45),new Const(16))),new Const(0)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg(41),new Const(8))),reg(45)),
 				new Move(reg("j"),reg(41)),
 				new Move(reg(59),new Call(new Name(label("_Ifunc_t8iiiiiiibi")),new Const(1))),
 				new Move(reg("a"),reg(59)),
 				new Move(reg("b"),reg("rdi")),
 				new Move(reg("c"),reg("rsi")),
 				new Move(reg("d"),reg("rdx")),
 				new Move(reg("e"),reg("rcx")),
 				new Move(reg("f"),reg("r8")),
 				new Move(reg("g"),new Mem(new Binop(Binop.PLUS,reg("r9"),new Const(0)))),
 				new Move(reg("h"),new Mem(new Binop(Binop.PLUS,reg("r9"),new Const(8)))),
 				new LabelNode(label("41"))
 			),
 			new Func(new Label("_Ifunc_t8iiiiiiibi"),
 				new Move(reg("a"),reg("rdi")),
 				new Move(reg("rv"),new Const(1)),
 				new Move(reg("rdi"),new Const(2)),
 				new Move(reg("rsi"),new Const(3)),
 				new Move(reg("rdx"),new Const(4)),
 				new Move(reg("rcx"),new Const(5)),
 				new Move(reg("r8"),new Const(6)),
 				new Move(reg(60),new Call(new Name(label("_I_alloc_i")),new Const(16))),
 				new Move(reg("r9"),reg(60)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg("r9"),new Const(0))),new Const(7)),
 				new Move(new Mem(new Binop(Binop.PLUS,reg("r9"),new Const(8))),new Const(1)),
 				new Jump(label("42")),
 				new LabelNode(label("42"))
 			)
 		));
 	}
 	
 	public void testIRCjumpLowering(){
 		Seq stmt = gen("use io main(){a:bool b:bool; if (a | b){a = true} else {a = false}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Func(new Label("_Imain_p"),
 				new Cjump(reg("a"),label("52"),label("54")),
 				new LabelNode(label("54")),
 				new Cjump(reg("b"),label("52"),label("53")),
 				new LabelNode(label("52")),
 				new Move(reg("a"),new Const(1)),
 				new Jump(label("55")),
 				new LabelNode(label("53")),
 				new Move(reg("a"),new Const(0)),
 				new LabelNode(label("55")),
 				new LabelNode(label("51"))
 			)
 		));
 	}
 	
 	public void testIRAssignmentPrimitive(){
 		Seq stmt = gen("use io main(){a:int; a = 3}");
 		lookslike(stmt, new Seq(
 			new Func(new Label("_Imain_p"),
 				new Move(reg("a"),new Const(3)),
 				new LabelNode(label("63"))
 			)
 		));
 		
 		stmt = gen("use io main(){a:bool; a = true}");
 		System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(label, new Move(temp, c), ret));
 		
 		stmt = gen("use io main(){a:int[]; a = (1,2,3)}");
 		lookslike(stmt, new Seq(
 			label, new Move(temp, temp), // init a
 			new Move(temp, new Call(name, new Binop(Binop.LSH, new Binop(0, c, c), c))), // call alloc
 			new Move(temp, temp),
 			new Move(new Mem(temp), c), // put size into base-1
 			new Move(temp, new Binop(Binop.PLUS, temp, c)), // put base-addr of the array into temp
 			// add the elements into the array
 			new Move(new Mem(new Binop(Binop.PLUS, temp, c)), c),
 			new Move(new Mem(new Binop(Binop.PLUS, temp, c)), c),
 			new Move(new Mem(new Binop(Binop.PLUS, temp, c)), c),
 			new Move(temp, temp),
 			ret
 		));
 		
 		stmt = gen("use io main(){a:int[] b:int b = a[1]}");
 		lookslike(stmt, new Seq(label,
 			new Move(temp, temp), // decl a
 			new Move(temp, temp), // rebase a to a temporary
 			new Move(temp, c), // rebase index 1 to a new temp
 			new Cjump(new Binop(Binop.GE, temp, new Mem(new Binop(Binop.MINUS, temp, c))), l, l),
 			label, new Exp(new Call(name)), // out of bounds
 			label, new Move(temp, new Mem(new Binop(Binop.PLUS, temp, new Binop(Binop.LSH, temp, c)))),
 			ret
 		));
 		
 		stmt = gen("use io main(){a:int; a = f()} f():int{return 1}");
 		lookslike(stmt, new Seq(
 			label, new Move(temp, new Call(name)), new Move(temp, temp), ret, // main(){a - f()}
 			label, new Move(temp, c), ret // f(){return 1}
 		));
 	}
 	
 	public void testIRGenTupleAssignment(){
 		Seq stmt = gen("main(){a:int, b:int = f(1,2)} f(a:int, b:int):int,int{return a+b, a-b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(89),new Call(new Name(label("_If_t2iiii")),new Const(1),new Const(2))),
 			new Move(reg("a"),reg(89)),
 			new Move(reg("b"),reg("rdi")),
 			ret,
 			new LabelNode(label("_If_t2iiii")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Move(reg("rv"),new Binop(Binop.PLUS,reg("a"),reg("b"))),
 			new Move(reg("rdi"),new Binop(Binop.MINUS,reg("a"),reg("b"))),
 			ret
 		));
 	}
 	
 	public void testIRGenTupleAssignmentUnderscore(){
 		Seq stmt = gen("main(){a:int, _, b:int = f(1,2)} f(a:int, b:int):int,int,int{return a+b, a-b, 1}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(111),new Call(new Name(label("_If_t3iiiii")),new Const(1),new Const(2))),
 			new Move(reg("a"),reg(111)),
 			new Move(reg("b"),reg("rsi")),
 			ret,
 			new LabelNode(label("_If_t3iiiii")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Move(reg("rv"),new Binop(Binop.PLUS,reg("a"),reg("b"))),
 			new Move(reg("rdi"),new Binop(Binop.MINUS,reg("a"),reg("b"))),
 			new Move(reg("rsi"),new Const(1)),
 			ret
 		));
 		
 		stmt = gen("main(){_, a:int, b:int = f(1,2)} f(a:int, b:int):int,int,int{return a+b, a-b, 1}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Exp(new Call(new Name(label("_If_t3iiiii")),new Const(1),new Const(2))),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			ret,
 			new LabelNode(label("_If_t3iiiii")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Move(reg("rv"),new Binop(Binop.PLUS,reg("a"),reg("b"))),
 			new Move(reg("rdi"),new Binop(Binop.MINUS,reg("a"),reg("b"))),
 			new Move(reg("rsi"),new Const(1)),
 			ret
 		));
 	}
 	
 	public void testIRGenIndexAssignment(){
 		Seq stmt = gen("main(){a:int[] b:int  a[b] = 3}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg(109),reg("a")),
 			new Move(reg(110),reg("b")),
 			new Cjump(new Binop(Binop.GE,reg(110),new Mem(new Binop(Binop.MINUS,reg(109),new Const(8)))),label("65"),label("66")),
 			new LabelNode(label("66")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("65")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(109),new Binop(Binop.LSH,reg(110),new Const(3)))),new Const(3)),
 			ret
 		));
 		
 		stmt = gen("main(){a:int[][] b:int  a[b][1] = 3}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg(162),reg("a")),
 			new Move(reg(163),reg("b")),
 			new Cjump(new Binop(Binop.GE,reg(163),new Mem(new Binop(Binop.MINUS,reg(162),new Const(8)))),label("75"),label("76")),
 			new LabelNode(label("76")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("75")),
 			new Move(reg(166),new Mem(new Binop(Binop.PLUS,reg(162),new Binop(Binop.LSH,reg(163),new Const(3))))),
 			new Move(reg(167),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(167),new Mem(new Binop(Binop.MINUS,reg(166),new Const(8)))),label("79"),label("80")),
 			new LabelNode(label("80")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("79")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(166),new Binop(Binop.LSH,reg(167),new Const(3)))),new Const(3)),
 			ret
 		));
 	}
 	
 	public void testIRGenIndexAssignmentList(){
 		Seq stmt = gen("main(){a:int[][] b:int  a[b] = ()}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg(189),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(0),new Const(1)),new Const(3)))),
 			new Move(reg(188),reg(189)),
 			new Move(new Mem(reg(188)),new Const(0)),
 			new Move(reg(187),new Binop(Binop.PLUS,reg(188),new Const(8))),
 			new Move(reg(183),reg("a")),
 			new Move(reg(184),reg("b")),
 			new Cjump(new Binop(Binop.GE,reg(184),new Mem(new Binop(Binop.MINUS,reg(183),new Const(8)))),label("85"),label("86")),
 			new LabelNode(label("86")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("85")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(183),new Binop(Binop.LSH,reg(184),new Const(3)))),reg(187)),
 			ret
 		));
 	}
 	
 	public void testIRGenBinop(){
 		Seq stmt = gen("main(){a:int b:int  a = a + b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),new Binop(Binop.PLUS,reg("a"),reg("b"))),
 			ret
 		));
 		
 		stmt = gen("main(){a:int b:int  a = a * b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),new Binop(Binop.MUL,reg("a"),reg("b"))),
 			ret
 		));
 		
 		stmt = gen("main(){a:int b:int  a = -b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),new Binop(Binop.PLUS,new Const(0),reg("b"))),
 			ret
 		));
 		
 		stmt = gen("main(){a:int b:int c:bool  c = a > b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("c"),new Binop(Binop.GT,reg("a"),reg("b"))),
 			ret
 		));
 		
 		stmt = gen("main(){a:bool b:bool c:bool  c = a & b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq( // short circuited behavior
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(221),new Const(1)),
 			new Cjump(new Binop(Binop.XOR,reg("a"),new Const(1)),label("95"),label("96")),
 			new LabelNode(label("96")),
 			new Cjump(reg("b"),label("94"),label("95")),
 			new LabelNode(label("95")),
 			new Move(reg(221),new Const(0)),
 			new LabelNode(label("94")),
 			new Move(reg("c"),reg(221)),
 			ret
 		));
 		
 		stmt = gen("main(){a:bool c:bool  c = !a}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("c"),new Binop(Binop.XOR,reg("a"),new Const(1))),
 			ret
 		));
 	}
 	
 	public void testIRGenRecurGCD(){
 		Seq stmt = gen("gcd(a:int, b:int):int{if (b == 0){return a} return gcd(b, a % b)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Igcd_iii")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Cjump(new Binop(Binop.NE,reg("b"),new Const(0)),label("90"),label("89")),
 			new LabelNode(label("89")),
 			new Move(reg("rv"),reg("a")),
 			ret,
 			new LabelNode(label("90")),
 			new Move(reg(199),new Call(new Name(label("_Igcd_iii")),reg("b"),new Binop(Binop.MOD,reg("a"),reg("b")))),
 			new Move(reg("rv"),reg(199)),
 			ret
 		));
 	}
 	
 	public void testIRGenNestedBlocks(){
 		Seq stmt = gen("gcd(a:int, b:int):int{{{{a:int = 3 return a}}}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Igcd_iii")),
 			new Move(reg("a(245)"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Move(reg("a(249)"),new Const(3)), // this reg has a diff register id
 			new Move(reg("rv"),reg("a(249)")),
 			ret
 		));
 	}
 	
 	public void testIRGenBoolOps(){
 		Seq stmt = gen("main(){a:bool b:bool a = a|b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(257),new Const(1)),
 			new Cjump(reg("a"),label("105"),label("107")),
 			new LabelNode(label("107")),
 			new Cjump(reg("b"),label("105"),label("106")),
 			new LabelNode(label("106")),
 			new Move(reg(257),new Const(0)),
 			new LabelNode(label("105")),
 			new Move(reg("a"),reg(257)),
 			ret
 		));
 		
 		stmt = gen("main(){a:bool b:bool if(a|b){a = true}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Cjump(reg("a"),label("109"),label("111")),
 			new LabelNode(label("111")),
 			new Cjump(new Binop(Binop.XOR,reg("b"),new Const(1)),label("110"),label("109")),
 			new LabelNode(label("109")),
 			new Move(reg("a"),new Const(1)),
 			new LabelNode(label("110")),
 			ret
 		));
 		
 		stmt = gen("main(){a:bool if(a|f()){a = true}} f():bool{return true}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Cjump(reg("a"),label("114"),label("116")),
 			new LabelNode(label("116")),
 			new Move(reg(267),new Call(new Name(label("_If_b")))),
 			new Cjump(new Binop(Binop.XOR,reg(267),new Const(1)),label("115"),label("114")),
 			new LabelNode(label("114")),
 			new Move(reg("a"),new Const(1)),
 			new LabelNode(label("115")),
 			ret,
 			new LabelNode(label("_If_b")),
 			new Move(reg("rv"),new Const(1)),
 			ret
 		));
 		
 		stmt = gen("main(){a:bool b:bool[] if(a|b[1]){a = true}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("b"),reg("null")),
 			new Cjump(reg("a"),label("122"),label("124")),
 			new LabelNode(label("124")),
 			new Move(reg(274),reg("b")),
 			new Move(reg(275),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(275),new Mem(new Binop(Binop.MINUS,reg(274),new Const(8)))),label("120"),label("121")),
 			new LabelNode(label("121")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("120")),
 			new Cjump(new Binop(Binop.XOR,new Mem(new Binop(Binop.PLUS,reg(274),new Binop(Binop.LSH,reg(275),new Const(3)))),new Const(1)),label("123"),label("122")),
 			new LabelNode(label("122")),
 			new Move(reg("a"),new Const(1)),
 			new LabelNode(label("123")),
 			ret
 		));
 	}
 	
 	public void testIRGenListEq(){
 		Seq stmt = gen("use io main(){a:int[]; b:int[] c:bool = a == b}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg("b"),reg("null")),
 			new Move(reg("c"),new Binop(Binop.EQ,reg("a"),reg("b"))),
 			ret
 		));
 		
 		stmt = gen("use io main(){a:int[] c:bool = a == f()} f():int[]{return ()}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg(303),new Call(new Name(label("_If_ai")))),
 			new Move(reg("c"),new Binop(Binop.EQ,reg("a"),reg(303))),
 			ret,
 			new LabelNode(label("_If_ai")),
 			new Move(reg(304),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(0),new Const(1)),new Const(3)))),
 			new Move(reg(302),reg(304)),
 			new Move(new Mem(reg(302)),new Const(0)),
 			new Move(reg(301),new Binop(Binop.PLUS,reg(302),new Const(8))),
 			new Move(reg("rv"),reg(301)),
 			ret
 		));
 	}
 	
 	public void testIRGenListAdd(){
 		Seq stmt = gen("use io main(){a:int[]; b:int[] c:int[] = a + b}");
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null"),false),
 			new Move(reg("b"),reg("null"),false),
 			new Move(reg(60),new Const(0),false),
 			new Move(reg, reg), new Move(reg, reg),
 			new Move(reg(61),new Mem(new Binop(Binop.MINUS,reg("a"),new Const(8))),false),
 			new Move(reg(62),new Binop(Binop.PLUS,reg(61),new Mem(new Binop(Binop.MINUS,reg("b"),new Const(8)))),false),
 			new Move(reg(68),new Call(new Name(label("_I_alloc_i")),reg(62)),true),
 			new Move(reg(63),reg(68),false),
 			new LabelNode(label("64")),
 			new Cjump(new Binop(Binop.GE,reg(60),reg(61)),label("66"),label("65")),
 			new LabelNode(label("65")),
 			new Move(
 				new Mem(new Binop(Binop.PLUS,reg(63),new Binop(Binop.LSH,reg(60),new Const(3)))),
 				new Mem(new Binop(Binop.PLUS,reg("a"),new Binop(Binop.LSH,reg(60),new Const(3))))),
 			new Move(reg(60),new Binop(Binop.PLUS,reg(60),new Const(1)),false),
 			new Jump(label("64")),
 			new LabelNode(label("66")),
 			new LabelNode(label("67")),
 			new Cjump(new Binop(Binop.GE,reg(60),reg(62)),label("69"),label("68")),
 			new LabelNode(label("68")),
 			new Move(
 				new Mem(new Binop(Binop.PLUS,reg(63),new Binop(Binop.LSH,reg(60),new Const(3)))),
 				new Mem(
 					new Binop(Binop.PLUS,reg("b"),
 						new Binop(Binop.LSH,new Binop(Binop.MINUS,reg(60),reg(61)),new Const(3))))),
 			new Move(reg(60),new Binop(Binop.PLUS,reg(60),new Const(1)),false),
 			new Jump(label("67")),
 			new LabelNode(label("69")),
 			new Move(reg("c"),reg(63),false),
 			ret
 		));
 	}
 	
 	public void testIRGenWhile(){
 		Seq stmt = gen("main(){while(true){a:int = 3}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new LabelNode(label("157")),
 			new Cjump(new Binop(Binop.XOR,new Const(1),new Const(1)),label("159"),label("158")),
 			new LabelNode(label("158")),
 			new Move(reg("a"),new Const(3)),
 			new Jump(label("157")),
 			new LabelNode(label("159")),
 			ret
 		));
 	}
 	
 	public void testIRGenWhileBreak(){
 		Seq stmt = gen("main(){while(true){a:int = 3 if (a == 3) break}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new LabelNode(label("162")),
 			new Cjump(new Binop(Binop.XOR,new Const(1),new Const(1)),label("164"),label("163")),
 			new LabelNode(label("163")),
 			new Move(reg("a"),new Const(3)),
 			new Cjump(new Binop(Binop.NE,reg("a"),new Const(3)),label("166"),label("165")),
 			new LabelNode(label("165")),
 			new Jump(label("164")),
 			new LabelNode(label("166")),
 			new Jump(label("162")),
 			new LabelNode(label("164")),
 			ret
 		));
 	}
 	
 	public void testIRGenMean(){
 		Seq stmt = gen("mean(list:int[]):int{sum:int = 0 i:int = 0 while (i < length(list)) sum = sum + list[i]; return sum/length(list)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imean_iai")),
 			new Move(reg("list"),reg("rdi")),
 			new Move(reg("sum"),new Const(0)),
 			new Move(reg("i"),new Const(0)),
 			new LabelNode(label("169")),
 			new Cjump(new Binop(Binop.GE,reg("i"),new Mem(new Binop(Binop.MINUS,reg("list"),new Const(8)))),label("171"),label("170")),
 			new LabelNode(label("170")),
 			new Move(reg(347),reg("list")),
 			new Move(reg(348),reg("i")),
 			new Cjump(new Binop(Binop.GE,reg(348),new Mem(new Binop(Binop.MINUS,reg(347),new Const(8)))),label("174"),label("175")),
 			new LabelNode(label("175")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("174")),
 			new Move(reg("sum"),new Binop(Binop.PLUS,reg("sum"),new Mem(new Binop(Binop.PLUS,reg(347),new Binop(Binop.LSH,reg(348),new Const(3)))))),
 			new Jump(label("169")),
 			new LabelNode(label("171")),
 			new Move(reg("rv"),new Binop(Binop.DIV,reg("sum"),new Mem(new Binop(Binop.MINUS,reg("list"),new Const(8))))),
 			ret
 		));
 	}
 	
 	public void testIRGenIfStmts(){
 		Seq stmt = gen("f():int{if (true) {return 1} else {return 2}}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_If_i")),
 			new Cjump(new Binop(Binop.XOR,new Const(1),new Const(1)),label("179"),label("178")),
 			new LabelNode(label("178")),
 			new Move(reg("rv"),new Const(1)),
 			ret,
 			new Jump(label("180")),
 			new LabelNode(label("179")),
 			new Move(reg("rv"),new Const(2)),
 			ret,
 			new LabelNode(label("180"))
 		));
 		
 		stmt = gen("f():int{if (true) {return 1} return 2}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_If_i")),
 			new Cjump(new Binop(Binop.XOR,new Const(1),new Const(1)),label("179"),label("178")),
 			new LabelNode(label("178")),
 			new Move(reg("rv"),new Const(1)),
 			ret,
 			new LabelNode(label("179")),
 			new Move(reg("rv"),new Const(2)),
 			ret
 		));
 	}
 	
 	public void testIRGenLength(){
 		Seq stmt = gen("main(b:int[]){a:int = length(b)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_pai")),
 			new Move(reg("b"),reg("rdi")),
 			new Move(reg("a"),new Mem(new Binop(Binop.MINUS,reg("b"),new Const(8)))),
 			ret
 		));
 		
 		stmt = gen("main(b:int[]){b[0] = length(b)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_pai")),
 			new Move(reg("b"),reg("rdi")),
 			new Move(reg(380),reg("b")),
 			new Move(reg(381),new Const(0)),
 			new Cjump(new Binop(Binop.GE,reg(381),new Mem(new Binop(Binop.MINUS,reg(380),new Const(8)))),label("189"),label("190")),
 			new LabelNode(label("190")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("189")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(380),new Binop(Binop.LSH,reg(381),new Const(3)))),new Mem(new Binop(Binop.MINUS,reg("b"),new Const(8)))),
 			ret
 		));
 	}
 	
 	public void testIRGenList(){
 		Seq stmt = gen("main(){a:int[] = (1, 2, 3, 1+4)}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(395),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(4),new Const(1)),new Const(3)))),
 			new Move(reg(394),reg(395)),
 			new Move(new Mem(reg(394)),new Const(4)),
 			new Move(reg(393),new Binop(Binop.PLUS,reg(394),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(393),new Const(0))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(393),new Const(8))),new Const(2)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(393),new Const(16))),new Const(3)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(393),new Const(24))),new Const(5)),
 			new Move(reg("a"),reg(393)),
 			ret
 		));
 		
 		stmt = gen("main(a:int[][]){a = ((1,2),(3,4,5),(1,a[1][1]))}"); // moves list into a at the very end
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_paai")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg(414),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(3),new Const(1)),new Const(3)))),
 			new Move(reg(399),reg(414)),
 			new Move(new Mem(reg(399)),new Const(3)),
 			new Move(reg(398),new Binop(Binop.PLUS,reg(399),new Const(8))),
 			new Move(reg(415),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(2),new Const(1)),new Const(3)))),
 			new Move(reg(401),reg(415)),
 			new Move(new Mem(reg(401)),new Const(2)),
 			new Move(reg(400),new Binop(Binop.PLUS,reg(401),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(400),new Const(0))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(400),new Const(8))),new Const(2)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(398),new Const(0))),reg(400)),
 			new Move(reg(416),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(3),new Const(1)),new Const(3)))),
 			new Move(reg(403),reg(416)),
 			new Move(new Mem(reg(403)),new Const(3)),
 			new Move(reg(402),new Binop(Binop.PLUS,reg(403),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(402),new Const(0))),new Const(3)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(402),new Const(8))),new Const(4)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(402),new Const(16))),new Const(5)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(398),new Const(8))),reg(402)),
 			new Move(reg(417),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(2),new Const(1)),new Const(3)))),
 			new Move(reg(405),reg(417)),
 			new Move(new Mem(reg(405)),new Const(2)),
 			new Move(reg(404),new Binop(Binop.PLUS,reg(405),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(404),new Const(0))),new Const(1)),
 			new Move(reg(406),reg("a")),
 			new Move(reg(407),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(407),new Mem(new Binop(Binop.MINUS,reg(406),new Const(8)))),label("196"),label("197")),
 			new LabelNode(label("197")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("196")),
 			new Move(reg(410),new Mem(new Binop(Binop.PLUS,reg(406),new Binop(Binop.LSH,reg(407),new Const(3))))),
 			new Move(reg(411),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(411),new Mem(new Binop(Binop.MINUS,reg(410),new Const(8)))),label("200"),label("201")),
 			new LabelNode(label("201")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("200")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(404),new Const(8))),new Mem(new Binop(Binop.PLUS,reg(410),new Binop(Binop.LSH,reg(411),new Const(3))))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(398),new Const(16))),reg(404)),
 			new Move(reg("a"),reg(398)),
 			ret
 		));
 	}
 
 	public void testIRGenArguments(){
 		Seq stmt = gen("func(a:int, b:int, c:int, d:int, e:int, f:int, g:int, h:int):int,int,int,int,int,int,int{return a,b,c,d,e,a+b,f}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Ifunc_t7iiiiiiiiiiiiiii")),
 			new Move(reg("a"),reg("rdi")),
 			new Move(reg("b"),reg("rsi")),
 			new Move(reg("c"),reg("rdx")),
 			new Move(reg("d"),reg("rcx")),
 			new Move(reg("e"),reg("r8")),
 			new Move(reg("f"),reg("r9")),
 			new Move(reg("g"),new Mem(new Binop(Binop.PLUS,reg("fp"),new Const(24)))),
 			new Move(reg("h"),new Mem(new Binop(Binop.PLUS,reg("fp"),new Const(16)))),
 			new Move(reg("rv"),reg("a")),
 			new Move(reg("rdi"),reg("b")),
 			new Move(reg("rsi"),reg("c")),
 			new Move(reg("rdx"),reg("d")),
 			new Move(reg("rcx"),reg("e")),
 			new Move(reg("r8"),new Binop(Binop.PLUS,reg("a"),reg("b"))),
 			new Move(reg(440),new Call(new Name(label("_I_alloc_i")),new Const(8))),
 			new Move(reg("r9"),reg(440)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg("r9"),new Const(0))),reg("f")),
 			ret
 		));
 	}
 	
 	public void testIRGenFuncCalls(){
 		Seq stmt = gen("main(){a:int = f()} f():int{return 3}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(451),new Call(new Name(label("_If_i")))),
 			new Move(reg("a"),reg(451)),
 			ret,
 			new LabelNode(label("_If_i")),
 			new Move(reg("rv"),new Const(3)),
 			ret
 		));
 	}
 	
 	public void testIRGenProcedures(){
 		Seq stmt = gen("main(){f()} f(){}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Exp(new Call(new Name(label("_If_p")))),
 			ret,
 			new LabelNode(label("_If_p")),
 			ret
 		));
 	}
 	
 	public void testIRGenString(){
 		Seq stmt = gen("use io main(){print(\"Hello World!\")}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(459),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(12),new Const(1)),new Const(3)))),
 			new Move(reg(458),reg(459)),
 			new Move(new Mem(reg(458)),new Const(12)),
 			new Move(reg(457),new Binop(Binop.PLUS,reg(458),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(0))),new Const(72)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(8))),new Const(101)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(16))),new Const(108)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(24))),new Const(108)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(32))),new Const(111)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(40))),new Const(32)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(48))),new Const(87)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(56))),new Const(111)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(64))),new Const(114)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(72))),new Const(108)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(80))),new Const(100)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(457),new Const(88))),new Const(33)),
 			new Exp(new Call(new Name(label("_Iprint_pai")),reg(457))),
 			ret
 		));
 	}
 	
 	public void testIRGenFuncCallIndexAssign(){
 		Seq stmt = gen("main(){f()[1] = 2} f():int[]{return ()}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(466),new Call(new Name(label("_If_ai")))),
 			new Move(reg(462),reg(466)),
 			new Move(reg(463),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(463),new Mem(new Binop(Binop.MINUS,reg(462),new Const(8)))),label("196"),label("197")),
 			new LabelNode(label("197")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("196")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(462),new Binop(Binop.LSH,reg(463),new Const(3)))),new Const(2)),
 			ret,
 			new LabelNode(label("_If_ai")),
 			new Move(reg(469),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.LSH,new Binop(Binop.PLUS,new Const(0),new Const(1)),new Const(3)))),
 			new Move(reg(465),reg(469)),
 			new Move(new Mem(reg(465)),new Const(0)),
 			new Move(reg(464),new Binop(Binop.PLUS,reg(465),new Const(8))),
 			new Move(reg("rv"),reg(464)),
 			ret
 		));
 	}
 	
 	public void testIRGenConstantFold(){
 		Seq stmt = gen("main(){a:int[] a = (1,2,3)}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			new Move(reg(476),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(475),reg(476)),
 			new Move(new Mem(reg(475)),new Const(3)),
 			new Move(reg(474),new Binop(Binop.PLUS,reg(475),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(474),new Const(0))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(474),new Const(8))),new Const(2)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(474),new Const(16))),new Const(3)),
 			new Move(reg("a"),reg(474)),
 			ret
 		));
 	}
 	
 	public void testIRGenConstantFoldPlus(){
 		// operators that commute will also be folded
 		Seq stmt = new Seq(new Move(temp, 
 			new Binop(Binop.PLUS, new Const(1), new Binop(Binop.PLUS, temp, new Const(3)))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.PLUS,new Const(4),reg(33)))
 		));
 		
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.PLUS, new Binop(Binop.PLUS, temp, new Const(3)), new Const(1))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.PLUS,new Const(4),reg(33)))
 		));
 		
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.PLUS, new Binop(Binop.PLUS, new Const(3), temp), new Const(1))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.PLUS,new Const(4),reg(33)))
 		));
 		
 		// plus and multiplication do not commute
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.PLUS, new Binop(Binop.MUL, new Const(3), temp), new Const(1))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.PLUS,new Binop(Binop.MUL,new Const(3),reg(33)),new Const(1)))
 		));
 	}
 	
 	public void testIRGenConstantFoldMinus(){
 		// minus
 		// 1-(r-3) -> 4-r
 		Seq stmt = new Seq(new Move(temp, 
 			new Binop(Binop.MINUS, new Const(1), new Binop(Binop.MINUS, temp, new Const(3)))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.MINUS,new Const(4),reg(33)))
 		));
 		// (r-3)-1 -> r-4
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.MINUS, new Binop(Binop.MINUS, temp, new Const(3)), new Const(1))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.MINUS,reg(33),new Const(4)))
 		));
 		
 		// (3-r)-1 -> 2-r
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.MINUS, new Binop(Binop.MINUS, new Const(3), temp), new Const(1))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.MINUS,new Const(2),reg(33)))
 		));
 		
 		// 1-(3-r) -> r-2
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.MINUS, new Const(1), new Binop(Binop.MINUS, new Const(3), temp))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.MINUS,reg(33),new Const(2)))
 		));
 	}
 	
 	
 	public void testIRGenConstantFoldDiv(){	
 		// div
 		// (r/2)/10 -> r/20
 		Seq stmt = new Seq(new Move(temp, 
 			new Binop(Binop.DIV, new Binop(Binop.DIV, temp, new Const(2)), new Const(10))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.DIV,reg(33),new Const(20)))
 		));
 		
 		// 10/(r/2) -> 20/r
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.DIV, new Const(10), new Binop(Binop.DIV, temp, new Const(2)))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.DIV,new Const(20),reg(33)))
 		));
 		
 		// 10/(2/r) -? 5r
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.DIV, new Const(10), new Binop(Binop.DIV, new Const(2), temp))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.MUL,new Const(5),reg(33)))
 		));
 		
 		// (10/r)/2 -> 5/r
 		stmt = new Seq(new Move(temp, 
 			new Binop(Binop.DIV, new Binop(Binop.DIV, new Const(10), temp), new Const(2))));
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new Move(reg(33),new Binop(Binop.DIV,new Const(5),reg(33)))
 		));
 	}
 	
 	public void testIRGenArrayDecl(){
 		Seq stmt = gen("main(){a:int[][][]}");
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("a"),reg("null")),
 			ret
 		));
 		
 		stmt = gen("main(){a:int[1][2][]}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(477),new Call(new Name(label("_I_alloc_i")),new Const(16))),
 			new Move(reg(474),reg(477)),
 			new Move(new Mem(reg(474)),new Const(1)),
 			new Move(reg(473),new Binop(Binop.PLUS,reg(474),new Const(8))),
 			new Move(reg(478),new Call(new Name(label("_I_alloc_i")),new Const(24))),
 			new Move(reg(476),reg(478)),
 			new Move(new Mem(reg(476)),new Const(2)),
 			new Move(reg(475),new Binop(Binop.PLUS,reg(476),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(475),new Const(0))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(475),new Const(8))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(473),new Const(0))),reg(475)),
 			new Move(reg("a"),reg(473)),
 			ret
 		));
 		
 		stmt = gen("main(){a:int[2][2][3]}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(497),new Call(new Name(label("_I_alloc_i")),new Const(24))),
 			new Move(reg(484),reg(497)),
 			new Move(new Mem(reg(484)),new Const(2)),
 			new Move(reg(483),new Binop(Binop.PLUS,reg(484),new Const(8))),
 			new Move(reg(498),new Call(new Name(label("_I_alloc_i")),new Const(24))),
 			new Move(reg(486),reg(498)),
 			new Move(new Mem(reg(486)),new Const(2)),
 			new Move(reg(485),new Binop(Binop.PLUS,reg(486),new Const(8))),
 			new Move(reg(499),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(488),reg(499)),
 			new Move(new Mem(reg(488)),new Const(3)),
 			new Move(reg(487),new Binop(Binop.PLUS,reg(488),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(487),new Const(0))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(487),new Const(8))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(487),new Const(16))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(485),new Const(0))),reg(487)),
 			new Move(reg(500),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(490),reg(500)),
 			new Move(new Mem(reg(490)),new Const(3)),
 			new Move(reg(489),new Binop(Binop.PLUS,reg(490),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(489),new Const(0))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(489),new Const(8))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(489),new Const(16))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(485),new Const(8))),reg(489)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(483),new Const(0))),reg(485)),
 			new Move(reg(501),new Call(new Name(label("_I_alloc_i")),new Const(24))),
 			new Move(reg(492),reg(501)),
 			new Move(new Mem(reg(492)),new Const(2)),
 			new Move(reg(491),new Binop(Binop.PLUS,reg(492),new Const(8))),
 			new Move(reg(502),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(494),reg(502)),
 			new Move(new Mem(reg(494)),new Const(3)),
 			new Move(reg(493),new Binop(Binop.PLUS,reg(494),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(493),new Const(0))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(493),new Const(8))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(493),new Const(16))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(491),new Const(0))),reg(493)),
 			new Move(reg(503),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(496),reg(503)),
 			new Move(new Mem(reg(496)),new Const(3)),
 			new Move(reg(495),new Binop(Binop.PLUS,reg(496),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(495),new Const(0))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(495),new Const(8))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(495),new Const(16))),reg("null")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(491),new Const(8))),reg(495)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(483),new Const(8))),reg(491)),
 			new Move(reg("a"),reg(483)),
 			ret
 		));
 	}
 	
 	public void testIRGenArrayDeclAmbiguous(){
 		Seq stmt = gen("main(){n:int a:int[1][n][]}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg(543),new Call(new Name(label("_I_alloc_i")),new Const(24))),
 			new Move(reg(521),reg(543)),
 			new Move(new Mem(reg(521)),new Const(2)),
 			new Move(reg(520),new Binop(Binop.PLUS,reg(521),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(520),new Const(0))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(520),new Const(8))),reg("n")),
 			new Move(reg(544),new Call(new Name(label("_I_c_dynamalloc_aiai")),reg(520))),
 			new Move(reg("a"),reg(544)),
 			ret,
 			new LabelNode(label("_I_c_dynamalloc_aiai")),
 			new Move(reg("arr"),reg("rdi")),
 			new Move(reg("n"),new Mem(new Binop(Binop.MINUS,reg("arr"),new Const(8)))),
 			new Move(reg("i"),new Const(1)),
 			new Cjump(new Binop(Binop.NE,reg("n"),new Const(0)),label("213"),label("212")),
 			new LabelNode(label("212")),
 			new Move(reg("rv"),new Const(0)),
 			ret,
 			new LabelNode(label("213")),
 			new Move(reg(545),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.PLUS,reg("n"),new Const(1)))),
 			new Move(reg(526),reg(545)),
 			new Move(new Mem(reg(526)),reg("n")),
 			new Move(reg(527),new Binop(Binop.PLUS,reg(526),new Const(8))),
 			new Move(reg("tl"),reg(527)),
 			new LabelNode(label("214")),
 			new Cjump(new Binop(Binop.GE,reg("i"),reg("n")),label("216"),label("215")),
 			new LabelNode(label("215")),
 			new Move(reg(529),reg("arr")),
 			new Move(reg(530),reg("i")),
 			new Cjump(new Binop(Binop.GE,reg(530),new Mem(new Binop(Binop.MINUS,reg(529),new Const(8)))),label("219"),label("220")),
 			new LabelNode(label("220")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("219")),
 			new Move(reg(531),reg("tl")),
 			new Move(reg(532),new Binop(Binop.MINUS,reg("i"),new Const(1))),
 			new Cjump(new Binop(Binop.GE,reg(532),new Mem(new Binop(Binop.MINUS,reg(531),new Const(8)))),label("217"),label("218")),
 			new LabelNode(label("218")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("217")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(531),new Binop(Binop.LSH,reg(532),new Const(3)))),new Mem(new Binop(Binop.PLUS,reg(529),new Binop(Binop.LSH,reg(530),new Const(3))))),
 			new Jump(label("214")),
 			new LabelNode(label("216")),
 			new Move(reg(533),reg("arr")),
 			new Move(reg(534),new Const(0)),
 			new Cjump(new Binop(Binop.GE,reg(534),new Mem(new Binop(Binop.MINUS,reg(533),new Const(8)))),label("221"),label("222")),
 			new LabelNode(label("222")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("221")),
 			new Move(reg("i"),new Mem(new Binop(Binop.PLUS,reg(533),new Binop(Binop.LSH,reg(534),new Const(3))))),
 			new Move(reg(549),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.PLUS,reg("i"),new Const(1)))),
 			new Move(reg(535),reg(549)),
 			new Move(new Mem(reg(535)),reg("i")),
 			new Move(reg(536),new Binop(Binop.PLUS,reg(535),new Const(8))),
 			new Move(reg("list"),reg(536)),
 			new LabelNode(label("223")),
 			new Cjump(new Binop(Binop.LE,reg("i"),new Const(0)),label("225"),label("224")),
 			new LabelNode(label("224")),
 			new Move(reg("i"),new Binop(Binop.MINUS,reg("i"),new Const(1))),
 			new Move(reg(550),new Call(new Name(label("_I_c_dynamalloc_aiai")),reg("tl"))),
 			new Move(reg(538),reg(550)),
 			new Move(reg("a"),reg(538)),
 			new Move(reg(540),reg("list")),
 			new Move(reg(541),reg("i")),
 			new Cjump(new Binop(Binop.GE,reg(541),new Mem(new Binop(Binop.MINUS,reg(540),new Const(8)))),label("226"),label("227")),
 			new LabelNode(label("227")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("226")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(540),new Binop(Binop.LSH,reg(541),new Const(3)))),reg("a'")),
 			new Jump(label("223")),
 			new LabelNode(label("225")),
 			new Move(reg("rv"),reg("list")),
 			ret
 		));
 	}
 	
 	public void testIRGenArrayDeclAmbiguousAssign(){
 		Seq stmt = gen("main(){n:int = 2 a:int[1][n][3] a[1][2] = (3,1,2)}");
 		stmt = ConstantFolding.foldConstants(stmt);
 		//System.out.println(islike(stmt));
 		lookslike(stmt, new Seq(
 			new LabelNode(label("_Imain_p")),
 			new Move(reg("n"),new Const(2)),
 			new Move(reg(596),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(589),reg(596)),
 			new Move(new Mem(reg(589)),new Const(3)),
 			new Move(reg(588),new Binop(Binop.PLUS,reg(589),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(588),new Const(0))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(588),new Const(8))),reg("n")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(588),new Const(16))),new Const(3)),
 			new Move(reg(597),new Call(new Name(label("_I_c_dynamalloc_aiai")),reg(588))),
 			new Move(reg("a"),reg(597)),
 			new Move(reg(598),new Call(new Name(label("_I_alloc_i")),new Const(32))),
 			new Move(reg(595),reg(598)),
 			new Move(new Mem(reg(595)),new Const(3)),
 			new Move(reg(594),new Binop(Binop.PLUS,reg(595),new Const(8))),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(594),new Const(0))),new Const(3)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(594),new Const(8))),new Const(1)),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(594),new Const(16))),new Const(2)),
 			new Move(reg(590),reg("a")),
 			new Move(reg(591),new Const(1)),
 			new Cjump(new Binop(Binop.GE,reg(591),new Mem(new Binop(Binop.MINUS,reg(590),new Const(8)))),label("230"),label("231")),
 			new LabelNode(label("231")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("230")),
 			new Move(reg(592),new Mem(new Binop(Binop.PLUS,reg(590),new Binop(Binop.LSH,reg(591),new Const(3))))),
 			new Move(reg(593),new Const(2)),
 			new Cjump(new Binop(Binop.GE,reg(593),new Mem(new Binop(Binop.MINUS,reg(592),new Const(8)))),label("232"),label("233")),
 			new LabelNode(label("233")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("232")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(592),new Binop(Binop.LSH,reg(593),new Const(3)))),reg(594)),
 			ret,
 			new LabelNode(label("_I_c_dynamalloc_aiai")),
 			new Move(reg("arr"),reg("rdi")),
 			new Move(reg("n"),new Mem(new Binop(Binop.MINUS,reg("arr"),new Const(8)))),
 			new Move(reg("i"),new Const(1)),
 			new Cjump(new Binop(Binop.NE,reg("n"),new Const(0)),label("213"),label("212")),
 			new LabelNode(label("212")),
 			new Move(reg("rv"),new Const(0)),
 			ret,
 			new LabelNode(label("213")),
 			new Move(reg(604),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.PLUS,reg("n"),new Const(1)))),
 			new Move(reg(527),reg(604)),
 			new Move(new Mem(reg(527)),reg("n")),
 			new Move(reg(528),new Binop(Binop.PLUS,reg(527),new Const(8))),
 			new Move(reg("tl"),reg(528)),
 			new LabelNode(label("214")),
 			new Cjump(new Binop(Binop.GE,reg("i"),reg("n")),label("216"),label("215")),
 			new LabelNode(label("215")),
 			new Move(reg(530),reg("arr")),
 			new Move(reg(531),reg("i")),
 			new Cjump(new Binop(Binop.GE,reg(531),new Mem(new Binop(Binop.MINUS,reg(530),new Const(8)))),label("219"),label("220")),
 			new LabelNode(label("220")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("219")),
 			new Move(reg(532),reg("tl")),
 			new Move(reg(533),new Binop(Binop.MINUS,reg("i"),new Const(1))),
 			new Cjump(new Binop(Binop.GE,reg(533),new Mem(new Binop(Binop.MINUS,reg(532),new Const(8)))),label("217"),label("218")),
 			new LabelNode(label("218")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("217")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(532),new Binop(Binop.LSH,reg(533),new Const(3)))),new Mem(new Binop(Binop.PLUS,reg(530),new Binop(Binop.LSH,reg(531),new Const(3))))),
 			new Jump(label("214")),
 			new LabelNode(label("216")),
 			new Move(reg(534),reg("arr")),
 			new Move(reg(535),new Const(0)),
 			new Cjump(new Binop(Binop.GE,reg(535),new Mem(new Binop(Binop.MINUS,reg(534),new Const(8)))),label("221"),label("222")),
 			new LabelNode(label("222")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("221")),
 			new Move(reg("i"),new Mem(new Binop(Binop.PLUS,reg(534),new Binop(Binop.LSH,reg(535),new Const(3))))),
 			new Move(reg(608),new Call(new Name(label("_I_alloc_i")),new Binop(Binop.PLUS,reg("i"),new Const(1)))),
 			new Move(reg(536),reg(608)),
 			new Move(new Mem(reg(536)),reg("i")),
 			new Move(reg(537),new Binop(Binop.PLUS,reg(536),new Const(8))),
 			new Move(reg("list"),reg(537)),
 			new LabelNode(label("223")),
 			new Cjump(new Binop(Binop.LE,reg("i"),new Const(0)),label("225"),label("224")),
 			new LabelNode(label("224")),
 			new Move(reg("i"),new Binop(Binop.MINUS,reg("i"),new Const(1))),
 			new Move(reg(609),new Call(new Name(label("_I_c_dynamalloc_aiai")),reg("tl"))),
 			new Move(reg(539),reg(609)),
 			new Move(reg("a"),reg(539)),
 			new Move(reg(541),reg("list")),
 			new Move(reg(542),reg("i")),
 			new Cjump(new Binop(Binop.GE,reg(542),new Mem(new Binop(Binop.MINUS,reg(541),new Const(8)))),label("226"),label("227")),
 			new LabelNode(label("227")),
 			new Exp(new Call(new Name(label("_I_outOfBounds_p")))),
 			new LabelNode(label("226")),
 			new Move(new Mem(new Binop(Binop.PLUS,reg(541),new Binop(Binop.LSH,reg(542),new Const(3)))),reg("a'")),
 			new Jump(label("223")),
 			new LabelNode(label("225")),
 			new Move(reg("rv"),reg("list")),
 			ret
 		));
 	}
	
	public void testConditionalConstantFolding(){
		Seq stmt = gen("main(){a:int; if(1==1) {a = 1 } if(true) { a = 2}  while(false) {a = 3 }}");
		stmt = ConstantFolding.foldConstants(stmt);
		//System.out.println(islike(stmt));
		System.out.println(stmt.prettyPrint());
	}
 }
