 package yapl;
 
 import java.util.List;
 
 import yapl.exceptions.*;
 import yapl.impl.BackendMJ;
 import yapl.interfaces.Attrib;
 import yapl.interfaces.BackendBinSM;
 import yapl.interfaces.CodeGen;
 import yapl.interfaces.Symbol;
 import yapl.interfaces.YAPLToken;
 import yapl.interfaces.Attrib.AttribKind;
 import yapl.lib.ArrayType;
 import yapl.lib.YAPLException;
 import yapl.types.BooleanType;
 import yapl.types.IntegerType;
 
 public class CodeGenImpl implements CodeGen {
 
 	private BackendBinSM backend;
 	
 	public CodeGenImpl(BackendBinSM backend)
 	{
 		this.backend = backend;
 	}
 	
 	@Override
 	public Attrib allocArray(ArrayType arrayType) throws YAPLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void allocVariable(Symbol sym) throws YAPLException {
		if (sym.isGlobal()) 
			sym.setOffset(backend.allocStaticData(1));
		else	
			sym.setOffset(backend.allocStack(1));
 	}
 
 	@Override
 	public Attrib arrayLength(Attrib arr) throws YAPLException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void arrayOffset(Attrib arr, Attrib index) throws YAPLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void assign(Attrib lvalue, Attrib expr) throws YAPLException {
 		if (!lvalue.getType().isCompatible(expr.getType()))
 		{
 			throw new TypeMismatchAssignException();
 		}
 		if (lvalue.getKind() != Attrib.AttribKind.ArrayElement)
 		{
			backend.storeWord(lvalue.getOffset(), lvalue.isGlobal());
 		}
 	}
 	
 	@Override
 	public void loadVariable(Symbol symbol) {
		backend.loadWord(symbol.getOffset(), symbol.isGlobal());
 	}
 
 	@Override
 	public void branchIfFalse(Attrib condition, String label)
 			throws YAPLException {
 		if (condition.getKind() == AttribKind.None)
 			backend.branchIf(false, label);
 	}
 
 	@Override
 	public Attrib callProc(Symbol proc, Attrib[] args) throws YAPLException {
 		backend.callProc(proc.getName());
 		return new AttribImpl(proc.getType());
 	}
 
 	@Override
 	public void enterProc(Symbol proc) throws YAPLException {
 		backend.enterProc(proc.getName(), proc.getParameters().size(), proc.isGlobal());
 	}
 	
 	@Override
 	public void exitProc(Symbol proc) throws YAPLException {
 		backend.exitProc(proc.getName());
 	}
 
 	@Override
 	public Attrib equalOp(Attrib x, Token op, Attrib y)
 			throws YAPLException {
 		if (!x.getType().isCompatible(y.getType()))
 			throw new IllegalEqualOpTypeException(op);
 		
 		if (op.image == "==")
 			backend.isEqual();
 		if (op.image == "")
 			backend.isNotEqual();
 		return new AttribImpl(new BooleanType());
 		
 	}
 
 	@Override
 	public void freeReg(Attrib attr) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void jump(String label) {
 		backend.jump(label);
 	}
 
 	@Override
 	public void loadReg(Attrib attr) throws YAPLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	private int labels = 0;
 	@Override
 	public String newLabel() {
 		labels++;
 		return "tempLabel" + labels;
 	}
 
 	@Override
 	public Attrib op1(Token op, Attrib x) throws YAPLException {
 		if (op != null)
 		{
 			if (!(x.getType() instanceof IntegerType))
 				throw new IllegalOp1Type(op);
 			if(op.image == "-")
 				backend.neg();
 		}
 		return x;
 	}
 
 	@Override
 	public Attrib op2(Attrib x, Token op, Attrib y) throws YAPLException {
 		if (op.kind == yapl.OR || op.kind == yapl.AND)
 		{
 			if (!(x.getType() instanceof BooleanType && y.getType() instanceof BooleanType))
 				throw new IllegalOp2Type(op);
 		}
 		if (!x.getType().isCompatible(y.getType()))
 			throw new IllegalOp2Type(op);
 		
 		if (op.image == "+")
 			backend.add();
 		if (op.image == "-")
 			backend.sub();
 		if (op.image == "*")
 			backend.mul();
 		if (op.image == "/")
 			backend.div();
 		if (op.image == "%")
 			backend.mod();
 		if (op.kind == yapl.AND)
 			backend.and();
 		if (op.kind == yapl.OR)
 			backend.or();
 		
 		
 		return x;
 	}
 
 	@Override
 	public Attrib relOp(Attrib x, Token op, Attrib y) throws YAPLException {
 		if (x.getType() instanceof IntegerType && y.getType() instanceof IntegerType) {
 			if (op.image == "<")
 				backend.isLess();
 			else if (op.image == ">")
 				backend.isGreater();
 			else if (op.image == ">=")
 				backend.isGreaterOrEqual();
 			else if (op.image == "<=")
 				backend.isLessOrEqual();
 			return new AttribImpl(new BooleanType());
 		}
 		throw new IllegalRelOpTypeException(op);
 	}
 
 	@Override
 	public void returnFromProc(Symbol proc, Attrib returnVal)
 			throws YAPLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void setParamOffset(Symbol sym, int pos) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void storeArrayDim(int dim, Attrib length) throws YAPLException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void verifyArraySelector(Attrib a, Token t) throws YAPLException {
 		if (!(a.getType() instanceof IntegerType))
 			throw new BadArraySelectorException(t);
 	}
 
 	@Override
 	public void assignLabel(String label) {
 		backend.assignLabel(label);
 	}
 
 	@Override
 	public void writeString(String string) throws YAPLException {
 		String s = string.substring(1, string.length() -1);
 		int addr = backend.allocStringConstant(s);
 		backend.writeString(addr);
 	}
 
 	@Override
 	public int storeConst(boolean value) {
 		return storeConst(backend.boolValue(value));
 	}
 
 	@Override
 	public int storeConst(int value) {
 		int addr = backend.allocStaticData(1);
 		backend.storeStaticConst(addr, value);
 		return addr;
 	}
 
 	@Override
 	public void loadConstData(int address)
 	{
 		backend.loadWord(address, true);
 	}
 }
