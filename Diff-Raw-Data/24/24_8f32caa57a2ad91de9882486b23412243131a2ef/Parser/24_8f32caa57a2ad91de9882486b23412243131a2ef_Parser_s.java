 package com.chaosdev.textmodloader.util;
 
 import java.lang.reflect.Array;
 import java.util.regex.Pattern;
 
 import clashsoft.clashsoftapi.util.CSUtil;
 
 import com.chaosdev.textmodloader.util.codeblock.CodeBlock;
 import com.chaosdev.textmodloader.util.operator.Operator;
 import com.chaosdev.textmodloader.util.types.Type;
 
 import net.minecraft.item.ItemStack;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class Parser.
  */
 public class Parser implements TextModConstants
 {
 	
 	/** The codeblock. */
 	private CodeBlock	codeblock;
 	
 	/**
 	 * Instantiates a new parser.
 	 *
 	 * @param codeblock the codeblock
 	 */
 	public Parser(CodeBlock codeblock)
 	{
 		this.codeblock = codeblock;
 	}
 	
 	/**
 	 * Helper method to update the sync the codeblock variables.
 	 *
 	 * @param codeblock the codeblock
 	 */
 	public void update(CodeBlock codeblock)
 	{
 		this.codeblock = codeblock;
 	}
 	
 	/**
 	 * Parses a list of parsable objects.
 	 *
 	 * @param par the par
 	 * @return the object[]
 	 * @throws ParserException the parser exception
 	 */
 	public Object[] parse(String... par) throws ParserException
 	{
 		Object[] obj = new Object[par.length];
 		for (int i = 0; i < par.length; i++)
 		{
 			obj[i] = parse(par[i]);
 		}
 		return obj;
 	}
 	
 	/**
 	 * Parses a String, even if it has operators in it
 	 * <p>
 	 * Example:
 	 * <p>
 	 * <code>
 	 * Object o = parse("\"Hello \" + 1")
 	 * </code>
 	 * <p>
 	 * would be the string
 	 * <p>
 	 * <code>
 	 * "Hello"1
 	 * </code>.
 	 *
 	 * @param par1 the par1
 	 * @return the object
 	 * @throws ParserException the parser exception
 	 */
 	public Object parse(String par1) throws ParserException
 	{
 		String[] split = TextModHelper.createParameterList(par1, ' ');
 		
 		Object value = null;
 		for (int i = 0; i < split.length; i++)
 		{
 			String s = split[i];
 			Operator op = Operator.fromString(s);
			Object o = parse(s);
 			if (op == null)
 				value = o;
 			else if (value != null && op.canOperate(Type.getTypeFromClass(value.getClass()), Type.getTypeFromClass(o.getClass())))
 				value = op.operate(value, o);
 			else
 				throw new ParserException("Invalid operator '" + s + "'");
 		}
 		return value;
 	}
 	
 	/**
 	 * Directly parses a string, ignores operators.
 	 * <p>
 	 * Example:
 	 * <p>
 	 * <code>
 	 * Object o = parser.directParse("\"Hello \"" + 1")
 	 * </code>
 	 * <p>
 	 * would cause a ParserException, because operators are not supported by
 	 * this method.
 	 * <p>
 	 * Use parse(String) instead.
 	 *
 	 * @param par1 String to parse
 	 * @return Parsed object
 	 * @throws ParserException the parser exception
 	 */
 	public Object directParse(String par1) throws ParserException
 	{
 		par1 = par1.trim();
 		String normalCase = par1;
 		String lowerCase = par1.toLowerCase();
 		
 		if (par1.startsWith("new ")) // New-Instance-Directives
 			return parseInstance(par1);
 		
 		else if (codeblock.getVariables().get(normalCase) != null) // Indicates
 																	// a
 																	// variable
 			return codeblock.getVariables().get(normalCase).value;
 		
 		else if (codeblock.isMethod(normalCase)) // Indicates a method
 			return codeblock.executeMethod(codeblock.getMethod(par1));
 		
 		else if (lowerCase.equals("true") || lowerCase.equals("false")) // Boolean
 			return (boolean) (lowerCase.equals("true") ? true : false);
 		
 		else if (par1.startsWith(STRING_START_CHAR) && par1.endsWith(STRING_END_CHAR)) // String
 			return par1.substring(1, par1.length() - 1);
 		
 		else if (par1.startsWith(CHAR_START_CHAR) && par1.endsWith(CHAR_END_CHAR) && par1.length() <= 3) // Character
 			return (char) par1.substring(1, par1.length() - 1).charAt(0);
 		
 		else if (lowerCase.matches("-?\\d+(\\.\\d+)?")) // Integer
 			return (int) parseNumber(par1);
 		
 		else if (lowerCase.endsWith(FLOAT_CHAR) && lowerCase.matches("-?\\d+(\\.\\d+)?")) // Float
 			return (float) parseNumber(par1);
 		
 		else if (lowerCase.endsWith(DOUBLE_CHAR) && lowerCase.matches("-?\\d+(\\.\\d+)?")) // Double
 			return (double) parseNumber(par1);
 		
 		else if (par1.startsWith("new ") && par1.contains(ARRAY_START_CHAR) && par1.endsWith(ARRAY_END_CHAR)) // Arrays
 			return parseArray(par1);
 		
 		throw new ParserException("Unable to parse: " + par1);
 	}
 	
 	/**
 	 * Parses a number, using JavaScript for operators.
 	 *
 	 * @param par1 the par1
 	 * @return the double
 	 * @throws ParserException the parser exception
 	 */
 	public double parseNumber(String par1) throws ParserException
 	{
 		return CSUtil.calculateFromString(normalize(par1));
 	}
 	
 	/**
 	 * Parses a boolean, using JavaScript for operators.
 	 *
 	 * @param par1 the par1
 	 * @return true, if successful
 	 * @throws ParserException the parser exception
 	 */
 	public boolean parseBoolean(String par1) throws ParserException
 	{
 		return CSUtil.createBoolean(normalize(par1));
 	}
 	
 	/**
 	 * Normalizes a string for use in JavaScript evaluation.
 	 *
 	 * @param par1 the par1
 	 * @return the string
 	 * @throws ParserException the parser exception
 	 */
 	public String normalize(String par1) throws ParserException
 	{
 		String[] split = TextModHelper.createParameterList(par1, ' ');
 		for (int i = 0; i < split.length; i++)
 		{
			split[i] = split[i].replace(INTEGER_CHAR, "").replace(FLOAT_CHAR, "").replace(DOUBLE_CHAR, "").trim(); // Replaces
																													// indicator
																													// chars
			if (codeblock.isMethod(split[i]) || codeblock.isVariable(split[i])) // Replaced
																				// methods
																				// and
																				// variables
																				// with
																				// their
																				// values
				split[i] = parse(split[i]).toString();
 		}
 		StringBuilder sb = new StringBuilder();
 		for (String s : split)
 			sb.append(s);
 		return sb.toString().trim();
 	}
 	
 	/**
 	 * This will return an array of the specified type instead of an Object[]
 	 * that needs to be converted.
 	 *
 	 * @param par1 the par1
 	 * @return the object
 	 * @throws ParserException the parser exception
 	 */
 	public Object parseArray(String par1) throws ParserException
 	{
 		par1.replaceFirst(Pattern.quote("new "), "");
 		int brace1Pos = par1.indexOf("{");
 		int brace2Pos = par1.indexOf("}");
 		if (brace1Pos == -1 || brace2Pos == -1)
 			return null;
 		String type = par1.substring(0, brace1Pos).trim();
 		String parameters = par1.substring(brace1Pos + 1, brace2Pos).trim();
 		String[] aparameters = TextModHelper.createParameterList(parameters, TextModConstants.ARRAY_SPLIT_CHAR.charAt(0));
 		Object[] aparameters2 = parse(aparameters);
 		return arrayWithType(type, aparameters2);
 	}
 	
 	/**
 	 * Creates an array of the type.
 	 *
 	 * @param type the type
 	 * @param values the values
 	 * @return the object
 	 */
 	public Object arrayWithType(String type, Object... values)
 	{
 		type = type.trim().toLowerCase();
 		Type type1 = Type.getTypeFromName(type);
 		
 		Object[] array = (Object[]) Array.newInstance(type1.getClass(), values.length);
 		System.arraycopy(values, 0, array, 0, values.length);
 		
 		return array;
 	}
 	
 	/**
 	 * Parses a new-instance-directive.
 	 *
 	 * @param par1 the par1
 	 * @return the object
 	 * @throws ParserException the parser exception
 	 */
 	public Object parseInstance(String par1) throws ParserException
 	{
 		String nonew = par1.trim().replaceFirst("new ", "");
 		int brace1Pos = nonew.indexOf(TextModConstants.NEW_INSTANCE_START_CHAR);
 		int brace2Pos = nonew.indexOf(TextModConstants.NEW_INSTANCE_END_CHAR);
 		String type = nonew.substring(0, brace1Pos);
 		String par = nonew.substring(brace1Pos + 1, brace2Pos);
 		String[] par2 = TextModHelper.createParameterList(par, TextModConstants.PARAMETER_SPLIT_CHAR.charAt(0));
 		return createInstance(type, parse(par2));
 	}
 	
 	/**
 	 * Creates a new Object of type <i> type </i> using the parameters <i>
 	 * parameters </i>.
 	 *
 	 * @param type the type
 	 * @param parameters the parameters
 	 * @return the object
 	 */
 	public Object createInstance(String type, Object... parameters)
 	{
 		Type type1 = Type.getTypeFromName(type);
 		if (type1.type.equals(ItemStack.class))
 		{
 			int id = (Integer) parameters[0];
 			int amount = 1;
 			int damage = 0;
 			if (parameters.length >= 2 && parameters[1] instanceof Integer)
 			{
 				amount = (Integer) parameters[1];
 			}
 			if (parameters.length >= 3 && parameters[2] instanceof Integer)
 			{
 				damage = (Integer) parameters[2];
 			}
 			return new ItemStack(id, amount, damage);
 		}
 		return null;
 	}
 }
