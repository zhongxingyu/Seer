 package cs4120.der34dlc287lg342.xi.ir.context;
 
 import cs4120.der34dlc287lg342.xi.ir.Binop;
 import cs4120.der34dlc287lg342.xi.ir.Call;
 import cs4120.der34dlc287lg342.xi.ir.Const;
 import cs4120.der34dlc287lg342.xi.ir.Expr;
 import cs4120.der34dlc287lg342.xi.ir.Mem;
 import cs4120.der34dlc287lg342.xi.ir.Move;
 import cs4120.der34dlc287lg342.xi.ir.Name;
 import cs4120.der34dlc287lg342.xi.ir.Seq;
 import cs4120.der34dlc287lg342.xi.ir.Temp;
 
 public class TempRegister {
 	static public int global = 0;
 	public int value;
 	public String name;
 	
 	static public TempRegister Null = new TempRegister("null");
 	static public TempRegister FP = new TempRegister("fp"); // frame pointer, rsp (dereferences to location on stack)
 	static public TempRegister RV = new TempRegister("rv"); // return value register, rax
 	static public TempRegister RDI = new TempRegister("rdi"); 
 	static public TempRegister RSI = new TempRegister("rsi"); 
 	static public TempRegister RDX = new TempRegister("rdx"); 
 	static public TempRegister RCX = new TempRegister("rcx"); 
 	static public TempRegister R8 = new TempRegister("r8"); 
 	static public TempRegister R9 = new TempRegister("r9"); 
 	
 	static public TempRegister[] free_registers = new TempRegister[]{RDI, RSI, RDX, RCX, R8, R9};
 	
 	public TempRegister(){
 		value = global++;
 		this.name = "" + value;
 	}
 	
 	public TempRegister(String name){
 		this();
 		this.name = name;
 	}
 	
 	public int value(){
 		return value;
 	}
 	
 	public String toString(){
		return "%r("+value+")";
 	}
 	
 	public static Seq init_array(Expr base, Expr n){
 		Temp p = new Temp(new TempRegister());
 		Call ptr = new Call(new Name(Label.alloc), new Binop(Binop.LSH, new Binop(Binop.PLUS, n, new Const(1)), new Const(3)));
 		
 		return new Seq(new Move(p, ptr), new Move(new Mem(p), n), new Move(base, new Binop(Binop.PLUS, p, new Const(8))));
 	}
 	
 	public static Expr size_of(Expr base_expr){
 		/*
 		 * Cjump[cond:(base == null)][iftrue:8][iffalse:9]
 		 * LabelNode[label:8]
 		 * Exp[expr:call[error=[]]]
 		 * LabelNode[label:9]
 		 * Move[dest:r(#)][src:Mem[(base - 8)]]
 		 */
 		//Label iftrue = new Label(), iffalse = new Label();
 //		Temp size = new Temp(new Register()), base = new Temp(new Register());
 //		Seq seq = new Seq(
 //			new Move(base, base_expr),
 //			new Cjump(new Binop(Binop.EQ, base, new Temp(Register.Null)), iftrue, iffalse),
 //			new LabelNode(iffalse),
 //			new Move(size, new Mem(new Binop(Binop.MINUS, base_expr, new Const(8))))
 //			new LabelNode(iftrue),
 //			new Exp(new Call(new Name(Label.outOfBounds)))
 //		);
 		
 		//return new Eseq(size, seq);
 		return new Mem(new Binop(Binop.MINUS, base_expr, new Const(8)));
 	}
 	
 	public boolean equals(Object that){
 		if (that instanceof TempRegister){
 			return ((TempRegister)that).value == value;
 		}
 		return false;
 	}
 }
