 /*
  *  You may not change or alter any portion of this comment or credits
  * of supporting developers from this source code or any supporting source code
  * which is considered copyrighted (c) material of the original comment or credit authors.
  * This program is distributed WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  */
 
 package cmdGA;
 
 import java.util.List;
 import java.util.Vector;
 
 import cmdGA.exceptions.IncorrectParameterTypeException;
 import cmdGA.parameterType.ParameterType;
 
 
 /**
  * Represents an Option that accepts one o more arguments.
  * 
  * @author Javier Iserte <jiserte@unq.edu.ar>
  *
  */
 public class MultipleOption extends Option {
 	protected char separatingChar;  
 	protected Object[] values=null;
 	
 	// CONSTRUCTORS
 	/**
 	 * Create a new MultipleOption parser.
 	 * 
 	 * @param parser An instance of Parser that is linked to the option.
 	 * @param defaultValue The default value returned 
 	 * @param name The string that is used to call this option in the command line.
 	 * @param alias An alias for the name.
 	 * @param type An instance of ParameterType that represents the type of the value returned.
 	 * @param separatingChar Is the char that used to separate the arguments.
 	 */
 	public MultipleOption(Parser parser, Object defaultValue, String name, String alias, char separatingChar, ParameterType type) {
 		super(parser, defaultValue, name, alias,  type);
 		this.setSeparatingChar(separatingChar);
 	}
 
 	/**
 	 * Create a new MultipleOption parser.
 	 * 
 	 * @param parser An instance of Parser that is linked to the option.
 	 * @param defaultValue The default value returned 
 	 * @param name The string that is used to call this option in the command line.
 	 * @param type An instance of ParameterType that represents the type of the value returned.
 	 * @param separatingChar Is the char that used to separate the arguments.
 	 */	
 	public MultipleOption(Parser parser, Object defaultValue, String name, char separatingChar, ParameterType type) {
 		super(parser, defaultValue, name,  type);
 		this.setSeparatingChar(separatingChar);		
 	}
 	
 	// PUBLIC METHODS
 	/**
 	 * Parses that string that represents the arguments and set its value to the option.
 	 * 
 	 * @param string Is a string that represents the arguments for an option.
 	 */
 	@Override
 	public void setValue(String value) throws IncorrectParameterTypeException {
 		if (value.trim().isEmpty()) { throw new IncorrectParameterTypeException ("Option " + this.getName() + " needs at least one argument, but none were found"); }
 		String[] p = value.split(((Character)separatingChar).toString());
 		List<Object> l = new Vector<Object>();
 		for (String string : p) {
 			l.add(this.type.parseParameter(string));
 		} 
 		this.values = l.toArray();
 	}
 	/**
 	 * Count the number of arguments after the method setValue is used.
 	 * 
 	 * @return the number of arguments 
 	 */
 	public int count() {
 		if (this.values==null) return 0;
 		return this.values.length;
 	}
 	/**
 	 * Returns one of the arguments.
 	 * @param position Is the index (starting at 0) of the argument required. 
 	 * @return an Object of type represented by ParameterType
 	 */
 	public Object getValue(int position) {
 		if (this.values==null) {
			return ((Object[])this.defaultValue)[position];
 		}
 		return this.values[position];
 	}
 	/**
 	 * Returns the collection of arguments. 
 	 * @return an array of Object of type represented by ParameterType
 	 */
 	public Object[] getValues() {
 		if (this.values==null) {
 			List<Object> r = new Vector<Object>();
 			r.add(this.defaultValue);
 			return r.toArray();
 		}
 		return this.values;
 	}
 	
 	// GETTERS & SETTERS
 	public char getSeparatingChar() {
 		return separatingChar;
 	}
 	protected void setSeparatingChar(char separatingChar) {
 		this.separatingChar = separatingChar;
 	}
 
 
 	
 
 	
 
 }
