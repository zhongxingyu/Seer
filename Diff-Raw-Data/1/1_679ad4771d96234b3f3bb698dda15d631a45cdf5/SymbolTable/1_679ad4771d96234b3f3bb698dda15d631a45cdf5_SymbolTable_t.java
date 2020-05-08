 package symboltable;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.ListIterator;
 import exceptions.AnalyzeException;
 
 //TODO: do we really need the getConst() method?
 
 public class SymbolTable{
 
 	private HashMap<String, Program> program_map; /**Map that contains programs*/
 	private HashSet<String> const_ID_set; /**Set that contains constants*/
 	private static SymbolTable st_instance; /**Used for the instance of the singleton SymbolTable*/
 
 	
 //Constructors///////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Constructor of the Singleton SymbolTable
 	 */
 	private SymbolTable(){
 		this.program_map = new HashMap<String, Program>();
 		this.const_ID_set = new HashSet<String>();
 		
 	}
 
 	/**
 	 * Used to instanciate the Singleton Symbol Table if it doesn't exists and to return it
 	 * @return: The singleton Symbol Table
 	 */
 	public static synchronized SymbolTable getInstance(){
 		
 		if(st_instance == null)
 			st_instance= new SymbolTable();
 		
 		return st_instance;
 	}
 
 	
 	
 //Constants////////////////////////////////////////////////////////////////////////////////////////
 	
 	/**
 	 * Adding constants in the Set if it doesn't exist, throw an exception if it's inside
 	 * @param ID: the Constant to add in the set
 	 */
 	public void addConst(String ID){
 		
 		try {
 			
 			if(!const_ID_set.contains(ID))
 				this.const_ID_set.add(ID);
 			else
 				throw new AnalyzeException("AnalyzeException: The table already contains this symbol: "+ID);
 		
 		} catch (AnalyzeException e) {
 			e.printStackTrace();
 		}	
 	}
 
 	/**
 	 * Used to verify if a constant exists in the set
 	 * @param ID: ID of the constant that we are looking for
 	 * @return: true if it exists in the set, false if it isn't
 	 */
 	public boolean containsConst(String ID){
 		
 		Iterator<String> it= const_ID_set.iterator();
 		
 		while(it.hasNext()){
 			
 			String const_ID_elt= it.next();
 			if(ID.equals(const_ID_elt))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	
 //Programs////////////////////////////////////////////////////////////////////////////////////////	
 	
 	/**
 	 * Adding a program in the map
 	 * @param ID: ID of the program
 	 * @param var_in: length of the input variables
 	 * @param var_out: length of the output variables
 	 * @param var_loc: length of the local variables
 	 */
 	public void addProgram(String ID, int var_in, int var_out){
 		
 		try{
 			
 			if(!program_map.containsKey(ID)){
 				
 				Program program1= new Program(var_in, var_out);
 				this.program_map.put(ID, program1);
 				System.out.println("Programme added successfully!");
 			}
 			else
 				throw new AnalyzeException("AnalyzeException: The table already contains this symbol: "+ID);
 				
 		} catch (AnalyzeException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Used to verify if a program exists in the map
 	 * @param ID: ID of the program that we are looking for
 	 * @return: true if it exists in the map, false if it isn't
 	 */
 	public boolean containsProgram(Program ID){
 		
 		if(program_map.containsKey(ID))	
 			return true;
 		
 		return false;
 	}
 	
 	/**
 	 * Returns a program from the map
 	 * @param ID: ID of the program to return
 	 * @return: A program from the list
 	 */
 	public Program getProgram(String ID){
 		
 		return program_map.get(ID);
 	}
 	
 	public String codeC() {
 		StringBuilder str = new StringBuilder();
 		str.append("wh::SymbolTable::getInstance()->initTable(" + this.const_ID_set.size() + ");");
 		Iterator<String> it = const_ID_set.iterator();
 		int i=0;
 		while (it.hasNext())
 		{
 			str.append("wh::SymbolTable::getInstance()->addSymbol(" + i + ", \"" + it.next() + "\");");
			i++;
 		}
 		return str.toString();
 	}
 	
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		str.append("SYMBOLTABLE\r\n");
 		
 		str.append("  Constants\r\n");
 		for(String constant : this.const_ID_set)
 			str.append("	" + constant + "\r\n");
 
 		str.append("\r\n  Programs\r\n");
 		Iterator<String> itr = this.program_map.keySet().iterator();
 		while(itr.hasNext()) {
 			String tmp = itr.next();
 			str.append("	" + tmp + ":\r\n" + this.program_map.get(tmp).toString() + "\r\n");
 		}
 		return str.toString();
 	}
 }
