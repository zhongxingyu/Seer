 package edu.sjtu.simpl.validate;
 
 import java.lang.reflect.InvocationTargetException;
 
 import backup.CompileTimeValidatorOld;
 import edu.sjtu.simpl.syntax.AnonymousFunction;
 import edu.sjtu.simpl.syntax.Application;
 import edu.sjtu.simpl.syntax.Assignment;
 import edu.sjtu.simpl.syntax.BinaryOperation;
 import edu.sjtu.simpl.syntax.BoolValue;
 import edu.sjtu.simpl.syntax.Bracket;
 import edu.sjtu.simpl.syntax.Expression;
 import edu.sjtu.simpl.syntax.First;
 import edu.sjtu.simpl.syntax.Head;
 import edu.sjtu.simpl.syntax.IfThenElse;
 import edu.sjtu.simpl.syntax.IntValue;
 import edu.sjtu.simpl.syntax.LetInEnd;
 import edu.sjtu.simpl.syntax.List;
 import edu.sjtu.simpl.syntax.Nil;
 import edu.sjtu.simpl.syntax.Nop;
 import edu.sjtu.simpl.syntax.Pair;
 import edu.sjtu.simpl.syntax.Second;
 import edu.sjtu.simpl.syntax.Sequence;
 import edu.sjtu.simpl.syntax.Tail;
 import edu.sjtu.simpl.syntax.UnaryOperation;
 import edu.sjtu.simpl.syntax.Value;
 import edu.sjtu.simpl.syntax.Variable;
 import edu.sjtu.simpl.syntax.WhileDoEnd;
 import edu.sjtu.simpl.type.BoolType;
 import edu.sjtu.simpl.type.FunctionType;
 import edu.sjtu.simpl.type.IntType;
 import edu.sjtu.simpl.type.ListType;
 import edu.sjtu.simpl.type.PairType;
 import edu.sjtu.simpl.type.Type;
 import edu.sjtu.simpl.type.UnitType;
 import edu.sjtu.simpl.type.UnknownType;
 import edu.sjtu.simpl.util.Log;
 
 public class ComplilerValidator implements IComplieTimeValidator{
 
 	public Type validate(Expression e, TypeMap tm)
 	{
 		Class[] cargs = new Class[2];
 		cargs[0] = e.getClass();
 		cargs[1] = tm.getClass();
 		
 		Object[] args = new Object[2];
 		args[0] = e;
 		args[1] = tm;
 		try {
 			return (Type) ComplilerValidator.class.getMethod("V", cargs).invoke(this, args);
 		} catch (IllegalAccessException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IllegalArgumentException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InvocationTargetException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (NoSuchMethodException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (SecurityException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private Type check_or_set(Expression e, TypeMap tm, Type t)
 	{
 		if(e instanceof Variable)
 		{
 			return check_or_set((Variable)e,tm,t);
 		}
 		else
 		{
 			return V(e,tm);
 		}
 	}
 	
 	@Override
 	public Type V(Expression e, TypeMap tm) {
 		Log.debug("validate Expression called...");
 		return validate(e,tm);
 	}
 
 	@Override
 	public Type V(AnonymousFunction fun, TypeMap tm) {
 		Log.debug("validate AnonymousFunction called...");
 		FunctionType ft = new FunctionType();
 		
 		TypeMap newtm = new TypeMap();
 		newtm.put(fun.arg.name, Type.UNKNOWN);
 		
 		newtm = tm.onion(newtm);
 		
 		//lazy ensure arg type and body type
 		Type bType = V(fun.body,newtm);
 		
 		ft.argType = newtm.get(fun.arg.name);
 		newtm.print();
		Log.debug("retrive argType for "+fun.toString()+"-"+ft.argType.toString()+" got");
 		ft.bodyType = bType;
 		
 		if(ft.argType == null)
 		{
 			return null;
 		}
 		if(ft.bodyType == null)
 		{
 			return null;
 		}
 		
 		return ft;
 	}
 
 	@Override
 	public Type V(Application app, TypeMap tm) {
 		Log.debug("validate Application called...");
 		Type tfun = V(app.func,tm);
 		
 		Type argType = null;
 		Type bodyType = null;
 		
 		if(tfun == null )
 		{
 			return null;
 		}
 		else if(!(tfun instanceof FunctionType))
 		{
 			Log.error("Type Error in "+app.toString()+", FunctionType expexcted..");
 			return null;
 		}
 		else if(tfun instanceof FunctionType)
 		{	
 			argType = ((FunctionType) tfun).argType;
 			bodyType = ((FunctionType) tfun).bodyType;
 			
 			Type paraType = check_or_set(app.param,tm,argType);
 			Log.debug("after checking para...");
 			tm.print();
 			if(paraType == null)
 			{
 				return null;
 			}
 			else if(!(paraType.equals(argType))&&!(argType instanceof UnknownType))
 			{
 				Log.error("Type Error in "+app.param.toString()+",arg type error,"+argType.toString()+" expected..");
 				return null;
 			}
 		}
 		return bodyType;
 	}
 
 	@Override
 	public Type V(BinaryOperation bop, TypeMap tm) {
 		Log.debug("validate BinaryOperation called...");
 		if (bop.getOperator().equals("+") 
 				|| bop.getOperator().equals("-")
 				|| bop.getOperator().equals("*")
 				|| bop.getOperator().equals("/")
 				)
 		{
 			Type t1 = check_or_set(bop.e1, tm, Type.INT);
 			Type t2 = check_or_set(bop.e2, tm, Type.INT);
 			if ( (Type.INT.equals(t1)||Type.UNKNOWN.equals(t1))
 					&&( Type.INT.equals(t2)||Type.UNKNOWN.equals(t2)))
 			{
				Log.debug("validate +-*/ ok..");
 				return Type.INT;
 			}
 			else if(t1 != null && t2 != null)
 			{
 				Log.debug("Type Error:"+bop.e1.toString()+","+bop.e2.toString()+" both need to be of type 'INT'");
 				return null;
 			}
 			return null;
 		}
 		else if(bop.getOperator().equals(">")
 				|| bop.getOperator().equals("<")
 				|| bop.getOperator().equals("=")) 
 		{
 			Type t1 = check_or_set(bop.e1, tm, Type.INT);
 			Type t2 = check_or_set(bop.e2, tm, Type.INT);
 			if ( (Type.INT.equals(t1)||Type.UNKNOWN.equals(t1))
 					&&( Type.INT.equals(t2)||Type.UNKNOWN.equals(t2)))
 			{
				Log.debug("validate >< ok..");
 				return Type.BOOL;
 			}
 			else if(t1 != null && t2 != null)
 			{
 				Log.debug("Type Error...:"+bop.e1.toString()+","+bop.e2.toString()+" both need to be of type 'INT'");
 				return null;
 			}
 			return null;
 		}
 		else if (bop.getOperator().equals("and")
 				|| bop.getOperator().equals("or")) 
 		{
 			Type t1 = check_or_set(bop.e1, tm, Type.BOOL);
 			Type t2 = check_or_set(bop.e2, tm, Type.BOOL);
 			if ( (Type.BOOL.equals(t1)||Type.UNKNOWN.equals(t1))
 					&&( Type.BOOL.equals(t2)||Type.UNKNOWN.equals(t2)))
 			{
				Log.debug("validate and or ok..");
 				return Type.BOOL;
 			}
 			else if(t1 != null && t2 != null)
 			{
 				Log.debug("Type Error in "+bop.toString()+":both '"+bop.e1.toString()+"' and '"+bop.e2.toString()+"' need to be of type 'BOOL'");
 				return null;
 			}
 			return null;
 		}
 
 		return null;
 	}
 
 	@Override
 	public Type V(BoolValue bv, TypeMap tm) {
 		Log.debug("validate BoolValue called...");
 		return new BoolType();
 	}
 
 	@Override
 	public Type V(Bracket brket, TypeMap tm) {
 		Log.debug("validate Bracket called...");
 		return V(brket.e,tm);
 	}
 
 	@Override
 	public Type V(First fst, TypeMap tm) {
 		Log.debug("validate First called...");
 		Type et = V(fst.e, tm);
 		if(et == null)
 		{
 			return null;
 		}
 		if(!(et instanceof PairType))
 		{
 			Log.error("Type Error in"+fst.toString());
 			return null;
 		}
 		else
 			return ((PairType)et).t1;
 	}
 	
 	@Override
 	public Type V(Second scd, TypeMap tm) {
 		Log.debug("validate Second called...");
 		Type et = V(scd.e, tm);
 		if(et == null)
 		{
 			return null;
 		}
 		
 		if(!(et instanceof PairType))
 		{
 			Log.error("Type Error in"+scd.toString());
 			return null;
 		}
 		else
 			return ((PairType)et).t2;
 	}
 
 	@Override
 	public Type V(Head head, TypeMap tm) {
 		Log.debug("validate Head called...");
 		Type t = V(head.e,tm);
 		if(t == null)
 		{
 			return null;
 		}
 		
 		if(t instanceof ListType)
 		{
 			return ((ListType) t).itemType;
 		}
 		else
 		{
 			Log.error("Type Error in "+head.toString()+","+head.e.toString()+" need to be a list");
 			return null;
 		}
 	}
 
 	@Override
 	public Type V(IfThenElse ite, TypeMap tm) {
 		Log.debug("validate IfThenElse called...");
 		//check condition
 		tm.print();
 		Type t1 = V(ite.condition,tm);
 		tm.print();
 		if(t1 == null)
 		{
 			return null;
 		}
 		else if(!(t1 instanceof BoolType))
 		{
 			Log.error("Type Error in "+ite.condition.toString()+", BOOL expected..");
 			return null;
 		}
 		
 		Type t3 = V(ite.thenClause,tm);
 		if(t3 == null)
 		{
 			return null;
 		}
 		
 		//check then clause and elseClause
 		Type t2 = V(ite.elseClause,tm);
 		if(t2 == null)
 		{
 			return null;
 		}
 		
 		if(t2.equals(t3))
 		{
 			return t2;
 		}
 		else if(t2 instanceof UnknownType)
 		{
 			return t3;
 		}
 		else if(t3 instanceof UnknownType)
 		{
 			return t2;
 		}
 		else
 		{
 			Log.error("Type Error in "+ite.toString()+",type is different between thenClause:"+ t3.toString() +" and elseClause:"+t2.toString());
 			return null;
 		}
 	}
 
 	@Override
 	public Type V(IntValue intValue, TypeMap tm) {
 		Log.debug("validate IntValue called...");
 		return new IntType();
 	}
 
 	@Override
 	public Type V(LetInEnd letin, TypeMap tm) {
 		Log.debug("validate LetInEnd called...");
 		
 		TypeMap newTm = new TypeMap();
 		Type liklyType = Type.UNKNOWN;
 		if(letin.definition instanceof AnonymousFunction)
 		{
 			FunctionType ft = new FunctionType();
 			ft.argType = Type.UNKNOWN;
 			ft.bodyType = Type.UNKNOWN;
 			liklyType = ft;
 		}
 		newTm.put(letin.x.name, liklyType);
 		newTm = tm.onion(newTm);
 		
 		Type defType = V(letin.definition,newTm);
 		if(defType != null)
 		{
 			newTm.set(letin.x.name, defType);
 		}
 		else
 		{
			Log.debug("letin.definition failed..");
 			return null;
 		}
 		Log.debug("after definition let..");
 		newTm.print();
 		
 		Type bodyType = V(letin.body,newTm);
 		return bodyType;
 	}
 
 	@Override
 	public Type V(List list, TypeMap tm) {
 		Log.debug("validate List called...");
 		Type t1 = V(list.head,tm);
 		Type t2 = V(list.tail,tm);
 		
 		if(t1 == null || t2 == null)
 		{
 			return null;
 		}
 		
 		if(t2 instanceof ListType)
 		{
 			t2 = ((ListType)t2).itemType;
 		}
 		
 		if(t1.equals(t2))
 		{
 			ListType t = new ListType();
 			t.itemType = t1;
 			return t;
 		}
 		else
 		{
 			Log.error("Type Error in "+list.toString());
 			return null;
 		}
 	}
 
 	@Override
 	public Type V(Nil nil, TypeMap tm) {
 		Log.debug("validate Nil called...");
 		return null;
 	}
 
 	@Override
 	public Type V(Nop nop, TypeMap tm) {
 		Log.debug("validate Nop called...");
 		return null;
 	}
 
 	@Override
 	public Type V(Pair pair, TypeMap tm) {
 		Log.debug("validate Pair called...");
 		Type t1 = V(pair.e1,tm);
 		Type t2 = V(pair.e2,tm);
 		
 		if(t1!=null&&t2!=null)
 		{
 			PairType pairType = new PairType();
 			pairType.t1 = t1;
 			pairType.t2 = t2;
 			return pairType;
 		}
 		return null;
 	}
 
 	@Override
 	public Type V(Sequence seq, TypeMap tm) {
 		Log.debug("validate Sequence called...");
 		return null;
 	}
 
 	@Override
 	public Type V(Tail tail, TypeMap tm) {
 		Log.debug("validate Tail called...");
 		Type et = V(tail.e, tm);
 		if(et == null)
 		{
 			return null;
 		}
 		
 		if(!(et instanceof ListType))
 		{
 			Log.error("Type Error in"+tail.toString());
 			return null;
 		}
 		else
 			return et;
 	}
 
 	@Override
 	public Type V(UnaryOperation uop, TypeMap tm) {
 		Log.debug("validate UnaryOperation called...");
 		String op = uop.getOperator();
 		if(op.equals("~"))
 		{
 			Type t = V(uop.e,tm);
 			if(t == null)
 			{
 				return null;
 			}
 			else if(t instanceof IntType)
 			{
 				return t;
 			}
 			else
 			{
 				Log.error("Type Error in "+uop.toString()+","+uop.e.toString()+" expected to be a INT");
 				return null;
 			}
 		}
 		else if(op.equals("not"))
 		{
 			Type t = V(uop.e,tm);
 			if(t==null)
 			{
 				return null;
 			}
 			else if(t instanceof BoolType)
 			{
 				return t;
 			}
 			else
 			{
 				Log.error("Type Error in "+uop.toString()+","+uop.e.toString()+" expected to be a BOOL");
 				return null;
 			}
 		}
 		Log.error("should  not reach here...");
 		return null;
 	}
 
 	@Override
 	public Type V(Value v, TypeMap tm) {
 		Log.debug("validate Value called...");
 		return null;
 	}
 
 	@Override
 	public Type V(Variable var, TypeMap tm) {
 		Log.debug("validate Variable called...");
 		if(!tm.contains(var.name))
 		{
 			Log.debug("variable '"+var.name+"' not declared..");
 			return null;
 		}
 		return tm.get(var.name);
 	}
 	
 	private Type check_or_set(Variable var, TypeMap tm, Type t)
 	{
 		Log.debug("check_or_set Variable "+var.name+" called...");
 		if(!tm.contains(var.name))
 		{
 			Log.debug("variable '"+var.name+"' not declared..");
 			return null;
 		}
 		//check type
 		Type tvar = tm.get(var.name);
 		if(tvar instanceof UnknownType)
 		{
 			Log.debug("lazy set type of '"+var.name+"' to "+t.toString());
 			//lazy set
 			tm.set(var.name, t);
 		}
 		
 		return tm.get(var.name);
 	}
 
 	@Override
 	public Type V(WhileDoEnd wde, TypeMap tm) {
 		Log.debug("validate WhileDoEnd called...");
 		Type t1 = V(wde.condition,tm);
 		Type t2 = V(wde.body,tm);
 		
 		if(!(t1 instanceof BoolType))
 		{
 			Log.error("Type Error in While,'"+wde.condition.toString()+"' need to be of type BOOL");
 			return null;
 		}
 		
 		if(!(t2 instanceof UnitType))
 		{
 			Log.error("Type Error in While,'"+wde.body.toString()+"' need to be of type UNIT");
 			return null;
 		}
 		
 		return new UnitType();
 	}
 
 	@Override
 	public Type V(Assignment assign, TypeMap tm) {
 		Log.debug("validate Assignment called...");
 		Type t1 = V(assign.var,tm);
 		Type t2 = V(assign.val,tm);
 		if(t1 == null || t2 == null)
 		{
 			return null;
 		}
 		
 		if(t1.equals(t2))
 		{
 			return Type.UNIT;
 		}
 		else
 		{
 			Log.error("Type Error in "+assign.toString());
 			return null;
 		}
 	}
 
 }
