 package symboltable;
 import java.util.HashSet;
 import java.util.Iterator;
 
 //TODO: do we really need the getVariable method?
 
 public class Program{
 
 	private HashSet<String> var_in;  /**Input list*/
 	private HashSet<String> var_out; /**Ouput list*/
 	private HashSet<String> var_loc; /**Local list*/
 	
 	/**
 	 * Programs constructor
 	 * @param lng_in: Input list length
 	 * @param lng_out: Output list length
 	 */
 	public Program(int lng_in, int lng_out){
 			
 		//Creating input list
 		this.var_in= new HashSet<String>(lng_in);
 		
 		//Creating output list
 		this.var_out= new HashSet<String>(lng_out);
 
 		//Creating local variables list
 		this.var_loc= new HashSet<String>();
 	}
 	
 	/**
 	 * Adding a variable to a program
 	 * @param ID: content of the variable
 	 * @param var_cat: category of the variable
 	 * @throws MissingException 
 	 * @pre: the value of var_cat must be: "Param" || "Return" || "Local"
 	 */
 	public void addVariable(String ID, VarType var_cat) {
 		switch (var_cat){
 		case INPUT:
 				this.var_in.add(ID);
 			break;
 		case OUTPUT:
 				this.var_out.add(ID);
 			break;
 		case LOCAL:
 				this.var_loc.add(ID);
 			break;
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Verify if a variable is contained in the program
 	 * @param ID: the ID of the variable that we are looking for
 	 * @return: true if the variable is in the program, false if it isn't
 	 */
 	public boolean containsVariable(String ID){
 		
 		Iterator<String> var_in_it= var_in.iterator();
 		while(var_in_it.hasNext()){
 			
 			String var_in_elt= var_in_it.next();
 			if(ID.equals(var_in_elt))
 				return true;
 		}
 		
 		Iterator<String> var_loc_it= var_loc.iterator();
 		while(var_loc_it.hasNext()){
 			
 			String var_loc_elt= var_loc_it.next();
 			if(ID.equals(var_loc_elt))
 				return true;
 		}
 
 		return false;		
 	}
 	
 	public String codeC() {
 
 		StringBuilder str = new StringBuilder();
 		Iterator<String> it = this.var_in.iterator();
 		while (it.hasNext()){
 			str.append("ptrNode " + it.next() + ";");
 		}
 		
		it = this.var_out.iterator();
		while (it.hasNext()){
			str.append("ptrNode " + it.next() + ";");
		}

 		it = this.var_loc.iterator();
 		while (it.hasNext()){
			str.append("ptrNode " + it.next() + ";");
 		}
 		
 		return str.toString();
 		
 		
 	}
 	
 	public String toString() {
 		StringBuilder str = new StringBuilder();
 		str.append("	  IN\r\n");
 		for(String var : this.var_in)
 			str.append("    	" + var + "\r\n");
 		
 		str.append("	  OUT\r\n");
 		for(String var : this.var_out)
 			str.append("    	" + var + "\r\n");
 		
 		str.append("	  LOCAL\r\n");
 		for(String var : this.var_loc)
 			str.append("    	" + var + "\r\n");
 		
 		return str.toString();
 	}
 
 }
