 package com.chaosdev.textmodloader.util.codeblock;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import com.chaosdev.textmodloader.methods.MethodExecuter;
 import com.chaosdev.textmodloader.util.*;
 import com.chaosdev.textmodloader.util.annotations.Annotation;
 import com.chaosdev.textmodloader.util.annotations.Annotation.AnnotationType;
 import com.chaosdev.textmodloader.util.annotations.IAnnotable;
 import com.chaosdev.textmodloader.util.codeblocktypes.CodeBlockType;
 import com.chaosdev.textmodloader.util.method.Method;
 import com.chaosdev.textmodloader.util.method.PredefinedMethod;
 import com.chaosdev.textmodloader.util.operator.Operator;
 import com.chaosdev.textmodloader.util.types.Type;
 
 /**
  * The Class CodeBlock.
  */
 public class CodeBlock implements IAnnotable, TextModConstants
 {
 	/** The block comment flag. */
 	public boolean					blockComment	= false;
 	
 	/** The super code block. */
 	public CodeBlock				superCodeBlock;
 	
 	/** The code blocks. */
 	public List<CodeBlock>			codeBlocks;
 	
 	/** The lines. */
 	public List<String>				lines;
 	
 	/** The variables. */
 	public Map<String, Variable>	variables		= new HashMap<String, Variable>();
 	
 	/** The parser. */
 	public Parser					parser;
 	
 	/**
 	 * Instantiates a new code block.
 	 * 
 	 * @param superBlock
 	 *            the super block
 	 */
 	public CodeBlock(CodeBlock superBlock)
 	{
 		this(superBlock, new LinkedList<String>());
 	}
 	
 	/**
 	 * Instantiates a new code block.
 	 * 
 	 * @param superBlock
 	 *            the super block
 	 * @param lines
 	 *            the lines
 	 */
 	public CodeBlock(CodeBlock superBlock, List<String> lines)
 	{
 		this.superCodeBlock = superBlock;
 		this.lines = lines;
 		this.codeBlocks = new LinkedList<CodeBlock>();
 		this.parser = new Parser(this);
 	}
 	
 	/**
 	 * Gets the code block super class.
 	 * 
 	 * @return the code block class
 	 */
 	public ClassCodeBlock getCodeBlockClass()
 	{
 		return superCodeBlock != null ? superCodeBlock.getCodeBlockClass() : null;
 	}
 	
 	/**
 	 * Gets the variables.
 	 * 
 	 * @return the variables
 	 */
 	public Map<String, Variable> getVariables()
 	{
 		Map<String, Variable> var = new HashMap<String, Variable>();
 		var.putAll(variables);
 		if (superCodeBlock != null)
 			var.putAll(superCodeBlock.getVariables());
 		return var;
 	}
 	
 	/**
 	 * Checks if the line can start a new code block
 	 * 
 	 * @param line
 	 *            the line
 	 * @return true, if is block start
 	 * @throws ParserException
 	 *             the parser exception
 	 */
 	public boolean isBlockStart(String line) throws ParserException
 	{
 		return !line.isEmpty() && !line.equals("\n") && !isComment(line) && !isMethod(line) && !isVariable(line) && CodeBlockType.getCodeBlockType(this, line) != null;
 	}
 	
 	/**
 	 * Checks if the line can end a new code block
 	 * 
 	 * @param line
 	 *            the line
 	 * @return true, if is block end
 	 */
 	public boolean isBlockEnd(String line)
 	{
 		return line.endsWith("}");
 	}
 	
 	/**
 	 * Checks if the line is a comment
 	 * 
 	 * @param line
 	 *            the line
 	 * @return true, if is comment
 	 */
 	public boolean isComment(String line)
 	{
 		return blockComment || line.startsWith("//");
 	}
 	
 	/**
 	 * Executes the Code Block
 	 * 
 	 * @return the return value of the execution
 	 */
 	public Object execute()
 	{
 		Annotation nextAnnotation;
 		CodeBlock cb = null;
 		for (int i = 0; i < lines.size(); i++)
 		{
 			String line = lines.get(i).trim();
 			
 			if (line.trim().startsWith("/*"))
 				blockComment = true;
 			if (line.trim().endsWith("*/"))
 				blockComment = false;
 			
 			try
 			{
 				if (cb instanceof HeaderCodeBlock && ((HeaderCodeBlock) cb).getCodeBlockType().isBreakable() && line.equals("break;"))
 					cb = null;
 				
 				if (line.startsWith("return "))
 					return parser.directParse(line.replaceFirst(Pattern.quote("return "), ""));
 				
 				if (cb != null && !isBlockStart(line) && !line.startsWith("{") && !isBlockEnd(line))
 					cb.lines.add(line);
 				
 				if (isBlockStart(line))
 					cb = new HeaderCodeBlock(line, this);
 				
 				else if (line.startsWith("{") && cb == null)
 					cb = new CodeBlock(this);
 				
 				else if (isBlockEnd(line))
 				{
 					cb.lines.add(line.replace("}", "").trim());
 					cb.execute();
 					codeBlocks.add(cb);
 					cb = null;
 				}
 				
 				if (cb == null)
 					this.executeLine(line);
 			}
 			catch (ParserException pex)
 			{
 				System.out.println("  Syntax error while executing line " + i + ": ");
 				throw new RuntimeException(pex);
 			}
 			catch (Exception ex)
 			{
 				System.out.println("  Exception while executing line " + i + ": ");
 				throw new RuntimeException(ex);
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Executes a line.
 	 * 
 	 * @param line
 	 *            the line
 	 * @throws ParserException
 	 *             the parser exception
 	 */
 	public void executeLine(String line) throws ParserException
 	{
 		if (TextModHelper.isLineValid(line))
 		{
 			System.out.println("  Reading line: " + line);
 			if (isMethod(line)) // Method invocation
 			{
 				Method method = getMethod(line);
 				executeMethod(method);
 			}
 			else if (isVariable(line)) // Variables
 			{
 				Variable v = getVariable(line);
 				this.variables.put(v.name, v);
 				this.parser.update(this);
 				System.out.println("  Variable \'" + v.name + "\' of type \'" + v.type.toString() + "\' added with value \'" + v.value.toString() + "\'.");
 			}
 			else
 			{
 				throw new ParserException("Invalid line '" + line + "'");
 			}
 		}
 	}
 	
 	/**
 	 * Gets a method from a line.
 	 * 
 	 * @param line
 	 *            the line
 	 * @return the method
 	 * @throws ParserException
 	 *             the parser exception
 	 */
 	public Method getMethod(String line) throws ParserException
 	{
 		PredefinedMethod method = (PredefinedMethod) readMethod(line);
 		if (method.isValid())
 			return method;
 		else
 			return getCustomMethod(method.name);
 	}
 	
 	/**
 	 * Gets the custom method.
 	 * 
 	 * @param name
 	 *            the name
 	 * @return the custom method
 	 */
 	public Method getCustomMethod(String name)
 	{
 		return getCodeBlockClass().getCustomMethod(name);
 	}
 	
 	/**
 	 * Reads a method from a line
 	 * 
 	 * @param line
 	 *            the line
 	 * @return the method (with name and parameters)
 	 * @throws ParserException
 	 *             the parser exception
 	 */
 	public Method readMethod(String line) throws ParserException
 	{
 		// Replaces the method identifier
 		int parametersStart = line.indexOf(METHOD_PARAMETERS_START_CHAR);
 		int parametersEnd = line.lastIndexOf(METHOD_PARAMETERS_END_CHAR);
 		if (parametersStart == -1 || parametersEnd == -1)
 			return null;
 		
 		String methodName = line.substring(0, parametersStart);
 		String parameters = line.substring(parametersStart + 1, parametersEnd);
 		String[] aparameters = TextModHelper.createParameterList(parameters, PARAMETER_SPLIT_CHAR.charAt(0));
 		for (int m = 0; m < aparameters.length; m++)
 		{
 			aparameters[m] = aparameters[m].trim();
 		}
 		Object[] aparameters2 = parser.parse(aparameters);
 		return new PredefinedMethod(methodName, aparameters2);
 	}
 	
 	/**
 	 * Executes a method.
 	 * 
 	 * @param method
 	 *            the method
 	 * @return the object
 	 */
 	public Object executeMethod(Method method)
 	{
 		return method.execute(this, method.parameters);
 	}
 	
 	/**
 	 * Gets the variable from a line.
 	 * 
 	 * @param line
 	 *            the line
 	 * @return the variable
 	 * @throws ParserException
 	 *             the parser exception
 	 */
 	public Variable getVariable(String line) throws ParserException
 	{
 		String[] split = TextModHelper.createParameterList(line.replace(";", ""), ' ');
 		Variable var = null;
 		if (isType(split[0])) // First part is a type declaration
 		{
 			Type type = Type.getTypeFromName(split[0]);
 			String name = split[1];
 			Object value = parser.directParse(split[3]);
 			var = new Variable(type, name, value);
 		}
 		else
 		// First part is an existing variable name
 		{
 			Variable var1 = variables.get(split[0]);
 			String operator = split[1];
 			Object value = parser.directParse(split[2]);
 			var = operate(var1, operator, value);
 		}
 		return var;
 	}
 	
 	/**
 	 * Checks if the line is a variable.
 	 * 
 	 * @param par1
 	 *            the par1
 	 * @return true, if is variable
 	 */
 	public boolean isVariable(String par1)
 	{
 		String first = par1;
 		if (par1.contains(" "))
 			first = par1.substring(0, par1.indexOf(" "));
 		if (variables.get(first) != null) // Already existing Variable
 			return true;
 		else if (isType(first))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Checks if the line is a method.
 	 * 
 	 * @param par1
 	 *            the par1
 	 * @return true, if is method
 	 */
 	public boolean isMethod(String par1)
 	{
 		par1 = par1.replace(";", "");
 		if (par1.contains(TextModConstants.METHOD_PARAMETERS_START_CHAR) && par1.endsWith(TextModConstants.METHOD_PARAMETERS_END_CHAR))
 		{
 			MethodExecuter executer = TextModHelper.getMethodExecuterFromName(par1.substring(0, par1.indexOf(TextModConstants.METHOD_PARAMETERS_START_CHAR)));
 			return executer != null;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if the string is a type.
 	 * 
 	 * @param par1
 	 *            the par1
 	 * @return true, if is type
 	 */
 	public boolean isType(String par1)
 	{
		return Type.getTypeFromName(par1) != Type.VOID;
 	}
 	
 	/**
 	 * Uses a variable, an operator and a value to calculate a new value for the variable
 	 * 
 	 * @param var1
 	 *            the var1
 	 * @param operator
 	 *            the operator
 	 * @param value
 	 *            the value
 	 * @return the variable
 	 */
 	public Variable operate(Variable var1, String operator, Object value)
 	{
 		if (operator.equals("="))
 			var1.value = value;
 		else
 		{
 			Operator op = Operator.fromString(operator);
 			if (op.canOperate(var1.type, Type.getTypeFromClass(value.getClass())))
 				var1.value = op.operate(var1.value, value);
 		}
 		return var1;
 	}
 	
 	/**
 	 * (non-Javadoc)
 	 * @see
 	 * com.chaosdev.textmodloader.util.annotations.IAnnotable#getAnnotationType
 	 * ()
 	 */
 	@Override
 	public AnnotationType getAnnotationType()
 	{
 		return AnnotationType.NOTHING;
 	}
 }
