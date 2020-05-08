 
 package scalr.variable;
 
 import java.util.HashMap;
 
 import scalr.Exceptions.FunctionDoesNotExistError;
 import scalr.Exceptions.FunctionExistsError;
 import scalr.Exceptions.TypeError;
 import scalr.expression.Expression;
 import scalr.expression.ExpressionType;
 import scalr.expression.Function;
 
 /**
  * This utility class contains a mapping of all IDs ({@linkplain String}) to their
  * {@linkplain Variable} instance.
  * @author mark
  */
 public final class SymbolTable
 {
 	public static String	                                             currentFunctionScope	=
 	                                                                                                  "";
 	public static final HashMap<String, Function>	                     functionReferences	  =
 	                                                                                                  new HashMap<String, Function>();
 	public static final HashMap<String, HashMap<String, Expression>>	 reference	          =
 	                                                                                                  new HashMap<String, HashMap<String, Expression>>();
 	public static final HashMap<String, HashMap<String, ExpressionType>>	referenceType	  =
 	                                                                                                  new HashMap<String, HashMap<String, ExpressionType>>();
 	
 	private SymbolTable() throws AssertionError
 	{
 		throw new AssertionError();
 	}
 	
 	public static void addFunc(String func) throws FunctionExistsError
 	{
 		System.out.println("Adding function " + func);
 		if (reference.containsKey(func))
 			throw new FunctionExistsError(func);
 		HashMap<String, Expression> funcTable = new HashMap<String, Expression>();
 		HashMap<String, ExpressionType> typeTable = new HashMap<String, ExpressionType>();
 		reference.put(func, funcTable);
 		referenceType.put(func, typeTable);
 	}
 	
 	public static void addFuncRef(Function f)
 	{
 		functionReferences.put(f.getName(), f);
 	}
 	
 	public static Function getFuncRef(String id) throws FunctionDoesNotExistError
 	{
 		if (!functionReferences.containsKey(id))
 			throw new FunctionDoesNotExistError(id);
 		
 		return (Function) functionReferences.get(id);
 	}
 	
 	/**
 	 * Adds the given {@linkplain Variable} with the given {@linkplain String} ID to the symbol
 	 * table.
 	 * @param id
 	 * @param var
 	 * @return True if this variable doesn't exist in this table. in the table and the replacement
 	 *         variable is of the same type, false
 	 */
 	public static boolean addReference(String func, String id, Expression var) throws TypeError
 	{
 		HashMap<String, Expression> locRef = reference.get(func);
 		HashMap<String, ExpressionType> locRefType = referenceType.get(func);
 		if (!locRef.containsKey(id)) {
 			locRef.put(id, var);
 			locRefType.put(id, var.getType());
 			return true;
 		}
 		else {
 			Expression existingVar = locRef.get(id);
 			if (existingVar.getType() != var.getType())
 				throw new TypeError(id);
 			locRef.remove(id);
 			locRef.put(id, var);
 			locRefType.put(id, var.getType());
 			return false;
 		}
 	}
 	
 	public static boolean addTypeReference(String func, String id, ExpressionType type)
 	{
 		HashMap<String, ExpressionType> locRefType = referenceType.get(func);
 		locRefType.put(id, type);
 		return true;
 	}
 	
 	public static Expression getMember(String func, String id)
 	{
 		HashMap<String, Expression> selfie = reference.get(func);
 		
 		return (Expression) selfie.get(id);
 		
 	}
 	
 	public static ExpressionType getMemberType(String func, String id)
 	{
 		HashMap<String, ExpressionType> funcTypeRef = referenceType.get(func);
 		return funcTypeRef.get(id);
 	}
 	
 	public static boolean memberExists(String func, String id)
 	{
 		boolean out = false;
 		if (reference.containsKey(func)) {
 			HashMap<String, Expression> temp = reference.get(func);
 			if (temp.containsKey(id)) {
 				out = true;
 			}
 		}
 		return out;
 	}
 }
