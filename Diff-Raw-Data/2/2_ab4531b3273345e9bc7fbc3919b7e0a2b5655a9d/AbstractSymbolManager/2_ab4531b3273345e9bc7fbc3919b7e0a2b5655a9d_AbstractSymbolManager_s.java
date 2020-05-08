 package hu.e.compiler.internal.symbols;
 
 import hu.e.compiler.ECompiler;
 import hu.e.compiler.ECompilerException;
 import hu.e.compiler.internal.OperationCallCompiler;
 import hu.e.compiler.internal.OperationCompiler;
 import hu.e.compiler.internal.linking.CodePlatform;
 import hu.e.compiler.internal.linking.OperationFinder;
 import hu.e.compiler.internal.model.ISymbolManager;
 import hu.e.compiler.internal.model.OPERATION;
 import hu.e.compiler.internal.model.symbols.ILiteralSymbol;
 import hu.e.compiler.internal.model.symbols.ISymbol;
 import hu.e.compiler.internal.model.symbols.IVariableSymbol;
 import hu.e.compiler.internal.model.symbols.SymbolContext;
 import hu.e.compiler.internal.model.symbols.impl.ArrayLiteralSymbol;
 import hu.e.compiler.internal.model.symbols.impl.LiteralSymbol;
 import hu.e.compiler.internal.model.symbols.impl.MemoryAssignmentValueSymbol;
 import hu.e.compiler.internal.model.symbols.impl.OperatedSymbol;
 import hu.e.compiler.internal.model.symbols.impl.OperationSymbol;
 import hu.e.compiler.internal.model.symbols.impl.StructLiteralSymbol;
 import hu.e.compiler.internal.model.symbols.impl.TypeCastedLiteralSymbol;
 import hu.e.compiler.internal.model.symbols.impl.TypeCastedVariableSymbol;
 import hu.e.compiler.internal.model.symbols.impl.VariableSymbol;
 import hu.e.compiler.list.ProgramStep;
 import hu.e.compiler.list.SequenceStep;
 import hu.e.parser.eSyntax.ADDITIVE_OPERATOR;
 import hu.e.parser.eSyntax.ArrayTypeDef;
 import hu.e.parser.eSyntax.BOOLEAN_OPERATOR;
 import hu.e.parser.eSyntax.EQUALITY_OPERATOR;
 import hu.e.parser.eSyntax.MULTIPLICATIVE_OPERATOR;
 import hu.e.parser.eSyntax.Operation;
 import hu.e.parser.eSyntax.OperationCall;
 import hu.e.parser.eSyntax.OperationRole;
 import hu.e.parser.eSyntax.RefTypeDef;
 import hu.e.parser.eSyntax.StructTypeDef;
 import hu.e.parser.eSyntax.StructTypeDefMember;
 import hu.e.parser.eSyntax.TypeDef;
 import hu.e.parser.eSyntax.UNARY_OPERATOR;
 import hu.e.parser.eSyntax.Variable;
 import hu.e.parser.eSyntax.VariableReference;
 import hu.e.parser.eSyntax.XExpression;
 import hu.e.parser.eSyntax.XExpression0;
 import hu.e.parser.eSyntax.XExpression1;
 import hu.e.parser.eSyntax.XExpression2;
 import hu.e.parser.eSyntax.XExpression3;
 import hu.e.parser.eSyntax.XExpression4;
 import hu.e.parser.eSyntax.XExpression5;
 import hu.e.parser.eSyntax.XExpression6;
 import hu.e.parser.eSyntax.XExpressionLiteral;
 import hu.e.parser.eSyntax.XExpressionM1;
 import hu.e.parser.eSyntax.XIsLiteralExpression;
 import hu.e.parser.eSyntax.XParenthesizedExpression;
 import hu.e.parser.eSyntax.XPrimaryExpression;
 import hu.e.parser.eSyntax.XSizeOfExpression;
 import hu.e.parser.eSyntax.XStructExpression;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.ecore.EObject;
 
 public abstract class AbstractSymbolManager implements ISymbolManager {
 	
 	private final CodePlatform platform;
 	
 	public AbstractSymbolManager(CodePlatform platform) {
 		this.platform = platform;
 	}
 	
 	@Override
 	public CodePlatform getCodePlatform() {
 		return platform;
 	}
 	
 	public TypeDef computeType(XExpression x){
 		return computeType((XExpression6)x);
 	}
 	
 	public TypeDef computeType(XExpression6 x){
 		if (x.getType() != null){
 			return x.getType();
 		}
 		
 		//TODO insert type inference here
 		return null;
 	}
 	
 	@Override
 	public ISymbol resolve(SequenceStep context, XExpression x) throws ECompilerException{
 		return resolve(context, (XExpression6)x);
 	}
 	
 	private ISymbol resolve(SequenceStep context, XExpression6 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		
 		for(VariableReference vr : x.getRef()){
 			a = new OperationSymbol(x, getSymbol(vr.getVar()), OPERATION.SET, a, this);
 		}
 		
 		if (x.getType() != null){
			if (a instanceof ILiteralSymbol){
 				a = new TypeCastedLiteralSymbol(x.getType(), (ILiteralSymbol)a);
 			}else if (a instanceof IVariableSymbol){
 				a = new TypeCastedVariableSymbol(x.getType(), (IVariableSymbol)a);
 			}
 		}
 		
 		return a;
 	}
 	
 	private OPERATION getOp(EObject x, BOOLEAN_OPERATOR op) throws ECompilerException{
 		switch(op){
 		case AND: return OPERATION.AND;
 		case OR: return OPERATION.OR; 
 		}
 		throw new ECompilerException(x, "Unsupported operator: "+op);
 	}
 	
 	private ISymbol resolve(SequenceStep context, XExpression5 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(int i=0;i<x.getB().size();i++){
 			a = new OperationSymbol(x, a, getOp(x,x.getOp().get(i)), resolve(context, x.getB().get(i)), this);
 		}
 		return a;
 	}
 	
 	private ISymbol resolve(SequenceStep context, XExpression4 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(int i=0;i<x.getB().size();i++){
 			a = new OperationSymbol(x, a, getOp(x,x.getOp().get(i)), resolve(context, x.getB().get(i)), this);
 		}
 		return a;
 	}
 	
 	private OPERATION getOp(EObject x, EQUALITY_OPERATOR op) throws ECompilerException {
 		switch(op){
 		case EQUALS: return OPERATION.EQUALS;
 		case NOTEQUALS: return OPERATION.NOTEQUALS;
 		case GT: return OPERATION.GT;
 		case LT: return OPERATION.LT;
 		case GTE: return OPERATION.GTE;
 		case LTE: return OPERATION.LTE;
 		}
 		throw new ECompilerException(x, "Unsupported operator: "+op);
 	}
 
 	private ISymbol resolve(SequenceStep context, XExpression3 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(int i=0;i<x.getB().size();i++){
 			a = new OperationSymbol(x, a, getOp(x,x.getOp().get(i)), resolve(context, x.getB().get(i)), this);
 		}
 		return a;
 	}
 	
 	private OPERATION getOp(EObject x, ADDITIVE_OPERATOR op) throws ECompilerException {
 		switch(op){
 		case ADD: return OPERATION.ADD;
 		case MINUS: return OPERATION.MINUS;
 		}
 		throw new ECompilerException(x, "Unsupported operator: "+op);
 	}
 
 	private ISymbol resolve(SequenceStep context, XExpression2 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(int i=0;i<x.getB().size();i++){
 			a = new OperationSymbol(x, a, getOp(x,x.getOp().get(i)), resolve(context, x.getB().get(i)), this);
 		}
 		return a;
 	}
 	
 	private OPERATION getOp(EObject x, MULTIPLICATIVE_OPERATOR op) throws ECompilerException {
 		switch(op){
 		case DIV: return OPERATION.DIV;
 		case MOD: return OPERATION.MOD;
 		case MUL: return OPERATION.MUL;
 		}
 		throw new ECompilerException(x, "Unsupported operator: "+op);
 	}
 
 	private ISymbol resolve(SequenceStep context, XExpression1 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(UNARY_OPERATOR op : x.getOperator()){
 			if (UNARY_OPERATOR.REFERENCE == op){
 				if (a instanceof IVariableSymbol){
 					a = ((IVariableSymbol) a).getAddressSymbol();
 				}else{
 					throw new ECompilerException(x, "Only variable can have a reference.");
 				}
 			}else{
 				a = new OperationSymbol(x, a, getOp(x, op), null, this);
 			}
 		}
 		return a;
 	}
 	
 	private OPERATION getOp(EObject x, UNARY_OPERATOR op) throws ECompilerException {
 		switch (op) {
 		case MINUS: return OPERATION.UNARYMINUS;
 		case NOT: return OPERATION.NOT;
 		case DEREFERENCE: return OPERATION.DEREFERENCE;
 		case REFERENCE: return OPERATION.REFERENCE;
 		}
 		throw new ECompilerException(x, "Unsupported operator: "+op);
 	}
 
 	private ISymbol resolve(SequenceStep context, XExpression0 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(String v : x.getMember()){
 			TypeDef td = a.getType();
 			while(td instanceof RefTypeDef){
 				td = ((RefTypeDef)td).getType().getDef();
 			}
 			if (td instanceof StructTypeDef){
 				boolean ok = false;
 				for(Variable member : ((StructTypeDef) td).getMembers()) if (!ok){
 					if (v.equals(member.getName())){
 						a = a.getMember(this, (StructTypeDefMember)member);
 						ok = true;
 					}
 				}
 				if (!ok){
 					throw new ECompilerException(x, "No such member: "+v);
 				}
 			}else{
 				throw new ECompilerException(x, "Struct expression expected");
 			}
 		}
 		return a;
 	}
 	
 	private ISymbol resolve(SequenceStep context, XExpressionM1 x) throws ECompilerException{
 		ISymbol a = resolve(context, x.getA());
 		for(XExpression index : x.getIndex()){
 			ISymbol i = resolve(context, index);
 			if (i.isAssignableAt(SymbolContext.COMPILETIME)){
 				a = a.getElement(this, (int)((ILiteralSymbol)i).getValue().intValue());
 			}else{
 				throw new ECompilerException(index, "TODO: Only compile-time indexing is supported for now");
 			}
 		}
 		return a;
 	}
 	
 	@Override
 	public ISymbol resolveVarRef(VariableReference vr) throws ECompilerException{
 		ISymbol s = getSymbol(vr.getVar());
 		
 		if (s == null) throw new ECompilerException(vr, "Variable cannot be accessed here: "+vr.getVar());
 		return s;
 	}
 	
 	private ISymbol resolve(SequenceStep context, XPrimaryExpression x) throws ECompilerException{
 		if(x instanceof VariableReference){
 			return resolveVarRef((VariableReference)x);
 		}
 		if (x instanceof XExpressionLiteral){
 			return new LiteralSymbol(null, ECompiler.convertLiteral(((XExpressionLiteral) x).getValue()));
 		}
 		if (x instanceof XParenthesizedExpression){
 			return resolve(context, ((XParenthesizedExpression) x).getA());
 		}
 		
 		if (x instanceof XIsLiteralExpression){
 			ISymbol s = getSymbol(((XIsLiteralExpression) x).getRef().getVar());
 			if (s == null) throw new ECompilerException(x, "Symbol cannot be resolved!");
 			return new LiteralSymbol(null, s.isAssignableAt(SymbolContext.COMPILETIME)? 1 : 0);
 		}
 		
 		if (x instanceof OperationCall){
 			OperationCall oc = (OperationCall)x;
 			OperationCallCompiler c = new OperationCallCompiler(platform, oc, this);
 			List<ProgramStep> ps = c.compile(context);
 			if (!ps.isEmpty()){
 				return new OperatedSymbol(ps, c.getReturns());
 			}
 			return c.getReturns();
 		}
 		if (x instanceof XStructExpression){
 			return resolveXStruct(context, (XStructExpression)x);
 		}
 		if (x instanceof XSizeOfExpression){
 			TypeDef t = ((XSizeOfExpression) x).getType();
 			int size = getVariableManager().getTypeResolver().getSize(this, t);
 			return new LiteralSymbol(null, size);
 		}
 		
 		throw new ECompilerException(x, "Invalid expression");
 	}
 	
 	private ISymbol resolveXStruct(SequenceStep context, XStructExpression literalStruct) throws ECompilerException{ 
 		TypeDef td = literalStruct.getType().getDef();
 		List<ISymbol> symbols = new ArrayList<ISymbol>(literalStruct.getValues().size());
 		for(XExpression value : literalStruct.getValues()){
 			symbols.add(resolve(context, value));
 		}
 		
 		if (td instanceof ArrayTypeDef){
 			ArrayTypeDef atd = (ArrayTypeDef)td;
 			int length = (int)((ILiteralSymbol)resolve(context, atd.getSize())).getValue().intValue();
 			if (length != symbols.size()) throw new ECompilerException(literalStruct, "Invalid number of elements!");
 			return new ArrayLiteralSymbol(symbols.toArray(new ISymbol[symbols.size()]), td);
 		}
 		
 		if (td instanceof StructTypeDef){
 			StructTypeDef std = (StructTypeDef)td;
 			if (std.getMembers().size() != symbols.size())
 				throw new ECompilerException(literalStruct, "Invalid number of elements!");
 			Map<Variable, ISymbol> members = new HashMap<Variable, ISymbol>();
 			int i = 0;
 			for(Variable m : std.getMembers()){
 				members.put(m, symbols.get(i));
 				i++;
 			}
 			return new StructLiteralSymbol(members, td);
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public OperatedSymbol executeOperator(OperationRole role, EObject context, SequenceStep step,
 			ISymbol... symbols) throws ECompilerException {
 		OperationFinder opfinder = getOpFinder();
 		OperationCompiler oc = opfinder.getOperationCompiler(getCodePlatform(), role, symbols);
 		if (oc == null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("Cannot find ");
 			sb.append(role);
 			sb.append("operator for pattern (");
 			for(ISymbol s : symbols){
 				sb.append(getTypeName(s.getType()));
 				sb.append(", ");
 			}
 			sb.append(")");
 			throw new ECompilerException(context, sb.toString());
 		}
 		return new OperatedSymbol(Collections.singletonList(oc.compile(this, oc.createResultBuffer(step))),oc.getReturns(step, this));
 	}
 	
 	private String getTypeName(TypeDef td){
 		if (td == null) return "<NULLTYPE>";
 		if (td instanceof RefTypeDef){
 			return ((RefTypeDef) td).getType().getName();
 		}
 		return td.toString();
 	}
 	
 	@Override
 	public IVariableSymbol createBuffer(TypeDef type) throws ECompilerException {
 		MemoryAssignmentValueSymbol symbol = this.getVariableManager().allocate(this, type);
 		IVariableSymbol v = VariableSymbol.create(symbol, type);
 		return v;
 	}
 	
 	@Override
 	public TypeDef getResultType(OperationRole role, ISymbol... symbols) throws ECompilerException {
 		OperationFinder opfinder = getOpFinder();
 		if (opfinder != null){
 			Operation op = opfinder.getOperation(role, symbols);
 			if (op != null){
 				if (op.getReturn() != null){
 					return computeType(op.getReturn());
 				}
 				if (op.getReturnvar() != null){
 					return op.getReturnvar().getType();
 				}
 			}
 		}
 		return null;
 	}
 	
 }
