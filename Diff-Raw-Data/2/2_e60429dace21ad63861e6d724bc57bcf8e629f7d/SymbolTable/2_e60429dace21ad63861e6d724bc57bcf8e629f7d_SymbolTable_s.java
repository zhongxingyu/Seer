 
 package scalr.variable;
 
 import java.util.HashMap;
 
 import scalr.Exceptions.FunctionExistsError;
 import scalr.Exceptions.TypeError;
 import scalr.Exceptions.FunctionDoesNotExistError;
 import scalr.expression.*;
 
 /**
  * This utility class contains a mapping of all IDs ({@linkplain String}) to their
  * {@linkplain Variable} instance.
  * @author mark
  */
 public final class SymbolTable
 {
 	public static String currentFunctionScope="";
 	public static final HashMap<String, Function> functionReferences= new HashMap<String, Function>();
 	public static final HashMap<String, HashMap<String, Expression>>	reference	=
 	                                                                                  new HashMap<String, HashMap<String, Expression>>();
 	
 	private SymbolTable() throws AssertionError
 	{
 		throw new AssertionError();
 	}
 	
 	public static void addFunc(String func) throws FunctionExistsError
 	{
 		if (reference.containsKey(func))
 			throw new FunctionExistsError(func);
 		HashMap<String, Expression> funcTable = new HashMap<String, Expression>();
 		reference.put(func, funcTable);
 	}
 	public static void addFuncRef(Function f){
 		functionReferences.put(f.getName(), f);
 	}
	public Function getFuncRef(String id) throws FunctionDoesNotExistError{
 		if(!functionReferences.containsKey(id))
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
 		if (!locRef.containsKey(id)) {
 			locRef.put(id, var);
 			return true;
 		}
 		else {
 			Expression existingVar = locRef.get(id);
 			if (existingVar.getType() != var.getType())
 				throw new TypeError(id);
 			locRef.remove(id);
 			locRef.put(id, var);
 			return false;
 		}
 	}
 	public static Expression getMember(String func, String id){
 		HashMap<String, Expression> selfie= reference.get(func);
 
 		return (Expression) selfie.get(id);
 
 	}
 	public static boolean memberExists(String func, String id){
 		boolean out=false;
 		if(reference.containsKey(func)){
 			HashMap<String, Expression> temp = reference.get(func);
 			if(temp.containsKey(id)){
 				out=true;
 			}
 		}
 		return out;
 	}
 }
