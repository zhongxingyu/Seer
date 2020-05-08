 
 public class BuiltIn_BAdd extends BuiltIn {
 	
 	public BuiltIn_BAdd(ReferenceFrame ref) {
 		super(ref);
 		String[] argNames = {"arg1", "arg2"};
 		args = createArgs(argNames);
 	}
 	
 	@Override
 	public Node execute(Node params, ReferenceFrame ref) {
 		Node arg1 = params.getCar();
		arg1.print(0);
 		Node arg2 = params.getCdr().getCar();
		//arg2.print(0);
 		arg1 = interpreter.eval(arg1, ref);
 		arg2 = interpreter.eval(arg2, ref);
 		if (!(arg1 instanceof IntLit && arg2 instanceof IntLit)) {
 			System.err.println("Invalid arguments to b+");
 			return null;
 		}
		IntLit foo = new IntLit(((IntLit)arg1).getVal() + ((IntLit)arg2).getVal());
		foo.print(0);
		return foo;
 	}
 }
