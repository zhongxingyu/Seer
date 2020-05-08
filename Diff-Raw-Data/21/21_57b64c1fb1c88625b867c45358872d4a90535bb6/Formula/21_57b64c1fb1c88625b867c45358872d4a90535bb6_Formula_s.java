 /*
  *  ___  ___   _      ___                 _    
  * |   \/ __| /_\    / __|___ _ _  ___ __(_)___
  * | |) \__ \/ _ \  | (_ / -_) ' \/ -_|_-< (_-<
  * |___/|___/_/ \_\  \___\___|_||_\___/__/_/__/
  *
  * -----------------------------------------------------------------------------
  * @author: Herbert Veitengruber 
  * @version: 1.0.0
  * -----------------------------------------------------------------------------
  *
  * Copyright (c) 2013 Herbert Veitengruber 
  *
  * Licensed under the MIT license:
  * http://www.opensource.org/licenses/mit-license.php
  */
 package dsagenesis.core.util.logic;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Vector;
 
 import javax.script.ScriptException;
 
 import dsagenesis.core.sqlite.TableHelper;
 
 import jhv.util.debug.logger.ApplicationLogger;
 import jhv.util.script.JXJavaScriptMethod;
 import jhv.util.script.JXScriptArgument;
 import jhv.util.script.JXScriptFactory;
 
 /**
  * Formula
  * 
  * A class for converting, parsing formula's from/to DB string
  * and execute them with javax.script API
  * 
  * Formula is commonly used 
  * for basic calculations and modifiers
  *  
  * @see jhv.util.script.*
  */
 public class Formula 
 		implements ISyntax
 {
 	// ============================================================================
 	//  Constants
 	// ============================================================================
 	
 	/**
 	 * version of the formula format.
 	 * can be used to update formula strings.
 	 */
 	public static final String VERSION = "1.0";
 	
 	/**
 	 * prefix argument block
 	 * each argument is an array:
 	 * 		[0] = database id of the argument
 	 * 		[1] = value type of the argument
 	 * 		[2] = column name of the arguments table used as label
 	 */
 	public static final String PREFIX_ARGUMENTS = "_args_[";
 	
 	/**
 	 * prefix return type
 	 * e.g. java.lang.Integer, java.lang.String ...
 	 */
 	public static final String PREFIX_RETURN_TYPE = "_rtype_[";
 	
 	/**
 	 * prefix code block 
 	 * contains javascript code
 	 */
 	public static final String PREFIX_CODE = "_f(x)_[";
 	
 	
 	// ============================================================================
 	//  Variables
 	// ============================================================================
 	
 	/**
 	 * Argument names are used inside the script code.
 	 * they are stored as a Vector Vector.
 	 * 		[0] = database id of the argument
 	 * 		[1] = class of the argument
 	 * 		[2] = column name of the arguments table used as label
 	 * 		[3] = (optional) contains the label string
 	 */
 	private Vector<Vector<String>> arguments;
 	
 	/**
 	 * return type (class) of the formula
 	 */
 	private Class<?> returnType;
 
 	/**
 	 * the script code
 	 */
 	private String scriptcode;
 		
 	/**
 	 * to reduce db access and query only once
 	 */
 	private boolean queryWasDone = false;
 	
 	// ============================================================================
 	//  Constructors
 	// ============================================================================
 	
 	/**
 	 * Empty Constructor.
 	 * Used by cell dialog
 	 */
 	public Formula() 
 	{
 		this.arguments = new Vector<Vector<String>>();
 		try
 		{
 			this.returnType = Class.forName("java.lang.Integer");
 		} catch( ClassNotFoundException e ) {
 			// nothing to do 
 		}
 		this.scriptcode = "";
 	}
 
 	/**
 	 * Constructor 2.
 	 * Used by table cells
 	 * 
 	 * @param dbString
 	 */
 	public Formula(String dbString)
 	{
 		this();
 		
 		if( dbString != null )
 			parseStringFromDB(dbString);
 	}
 	
 	
 	// ============================================================================
 	//  Functions
 	// ============================================================================
 
 	/**
 	 * parseStringFromDB
 	 * 
 	 * string format: 
 	 * 		"@[...]_args_[[a,b,c],[...]...,n]_rtype_[long class name]_f(x)_[the script code]"
 	 * 
 	 * @param dbString
 	 */
 	@Override
 	public void parseStringFromDB(String dbString)
 	{
 		if( !dbString.startsWith(PREFIX_LOGIC+PREFIX_VERSION) )
 		{
 			ApplicationLogger.logError("Not a Formula:\n"+dbString);
 			return;
 		}
 		
 		int start = PREFIX_LOGIC.length()
 				+ PREFIX_VERSION.length();
 		int end = dbString.indexOf(PARENTHESIS_CLOSE,start);
 		String version = dbString.substring(start,end);
 		
 		if( !version.equals(VERSION) )
 		{
 			ApplicationLogger.logWarning(
 					"Formula has Version "
 						+ version 
 						+ " but needs "
 						+ VERSION
 					);
 // TODO implement update
 			ApplicationLogger.logWarning(
 					"TODO implement update!"
 				);
 		}
 		
 		// extract arguments
 		start = parseArguments(dbString,start);
 		
 		// extract return type
 		start = dbString.indexOf(PREFIX_RETURN_TYPE,start)
 				+ PREFIX_RETURN_TYPE.length();
 		end = dbString.indexOf(PARENTHESIS_CLOSE,start);
 		try 
 		{
 			returnType = Class.forName(dbString.substring(start,end));
 		} catch( ClassNotFoundException e ) {
 			// nothing to do
 		}
 		
 		// extract code
 		start = dbString.indexOf(PREFIX_CODE,end) 
 				+ PREFIX_CODE.length();
 		end = dbString.lastIndexOf(PARENTHESIS_CLOSE);
 		String subScript = dbString.substring(start,end);
 		
 		subScript = subScript.replaceAll(MASK_NEWLINE,"\n");
 		this.scriptcode = subScript.replaceAll(MASK_TAB,"\t");
 	}
 	
 	private int parseArguments(String dbString, int pos)
 	{
 		boolean isParenthesisOpen = false;
 		boolean done = false;
 		String fragment = "";
 		Vector<String> arg = new Vector<String>();
 		
 		pos = dbString.indexOf(PREFIX_ARGUMENTS, pos) 
 				+ PREFIX_ARGUMENTS.length();
 		
 		while( !done )
 		{
 			char c = dbString.charAt(pos);
 			
 			if( c == PARENTHESIS_OPEN )
 			{
 				isParenthesisOpen = true;
 			
 			} else if( c == PARENTHESIS_CLOSE ) {
 				if( !isParenthesisOpen )
 				{
 					done = true;
 				} else {
 					isParenthesisOpen = false;
 					
 					arg.add(fragment);
 					arg.add("");
 					arguments.add(arg);
 					fragment = "";
 					arg = new Vector<String>();
 				}
 			} else if( c == ARRAY_SEPARATOR ) {
 				arg.add(fragment);
 				fragment = "";
 				
 			} else {
 				fragment += c;
 			}
 			pos++;
 		}
 		return pos;
 	}
 	
 	
 	@Override
 	public void queryLabels() 
 			throws SQLException
 	{
 		if( queryWasDone )
 			return;
 		
 		for( int i=0; i< arguments.size(); i++ )
 		{
 			String id = arguments.get(i).get(0);
 			String column = arguments.get(i).get(2);
 			arguments.get(i).set(
 					3,
 					(String)TableHelper.getColumnEntryFor(id, column)
 				);
 		}
 		queryWasDone = true;
 	}
 	
 	@Override
 	public String renderStringForDB()
 	{	
 		if( isEmpty() ) 
 			return null;		
 
 		String args = "";
 		
 		for( int i=0; i<arguments.size(); i++ )
 		{
 			args += PARENTHESIS_OPEN
 					+ arguments.get(i).get(0)
 					+ ARRAY_SEPARATOR
 					+ arguments.get(i).get(1)
 					+ ARRAY_SEPARATOR
 					+ arguments.get(i).get(2)
 					+ PARENTHESIS_CLOSE;
 		}
 		
 		String str = PREFIX_LOGIC
 				+ PREFIX_VERSION
 				+ VERSION
 				+ PARENTHESIS_CLOSE
 				+ PREFIX_ARGUMENTS
 				+ args
 				+ PARENTHESIS_CLOSE
 				+ PREFIX_RETURN_TYPE
 				+ this.returnType.getName()
 				+ PARENTHESIS_CLOSE
 				+ PREFIX_CODE;
 		
 		String code = scriptcode.replaceAll("\n", MASK_NEWLINE);
 		str += code.replaceAll("\t", MASK_TAB) 
 				+ PARENTHESIS_CLOSE;
 		
 		return str;
 	}
 	
 	@Override
 	public String renderStringForCell()
 	{
 		if( isEmpty() )
 			return "f(x):{null}";
 		
 		try 
 		{
 			queryLabels();
 		} catch (SQLException e) {
 			// nothing to do comes from db and should be correct.
 		}
 		
 		String code = scriptcode.replaceAll("\n", " ");
 		code = code.replaceAll("\t", " "); 
 		for( int i=0; i<arguments.size(); i++ )
			code = code.replaceAll(
					arguments.get(i).get(0),
					arguments.get(i).get(3)
				);
		
 		return "f(x):{"+ code + "}";
 	}
 	
 	@Override
 	public String renderStringForCellTooltip()
 	{
 		if( isEmpty() )
 			return null;
 		
 		try 
 		{
 			queryLabels();
 		} catch (SQLException e) {
 			// nothing to do comes from db and should be correct.
 		}
 		
 		String code = scriptcode.replaceAll("\n", "<br>");
 		code = code.replaceAll("\t", "&nbsp; &nbsp; ");
 		
 		// replace ids with labels
 		for( int i=0; i<arguments.size(); i++ )
			code = code.replaceAll(
					arguments.get(i).get(0),
					arguments.get(i).get(3)
				);
 		
 		return "<html>" + code + "</html>";
 	}
 	
 	@Override
 	public boolean isEmpty()
 	{
 		if( arguments.size() == 0 && scriptcode.equals("") ) 
 			return true;		
 
 		return false;
 	}
 	
 	/**
 	 * calculate
 	 * 
 	 * @param values
 	 * @return
 	 * @throws ScriptException
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Object calculate(
 			Vector<Object> values
 		) throws ScriptException
 	{
 		ArrayList<JXScriptArgument<?>> args = new ArrayList<JXScriptArgument<?>>();
 		
 		for( int i=0; i<arguments.size(); i++ )
 		{
 			JXScriptArgument<?> arg = new JXScriptArgument<Object>(
 					arguments.get(i).get(0),
 					values.get(i)
 				);
 			args.add(arg);
 		}
 		
 		JXJavaScriptMethod method = new JXJavaScriptMethod(
 				new JXScriptFactory(), 
 				returnType
 			);
 		
 		return method.invoke(scriptcode,args);
 	}
 	
 	/**
 	 * getArguments
 	 * 
 	 * @return Vector Vector of arguments
 	 * 		Inner Vector:
 	 * 		[0] = database id of the argument
 	 * 		[1] = value type of the argument
 	 * 		[2] = column name of the arguments table used as label
 	 * 		[3] = (optional) contains the label string
 	 * 
 	 */
 	public Vector<Vector<String>> getArguments()
 	{
 		return this.arguments;
 	}
 	
 	/**
 	 * getArgument
 	 * 
 	 * @param id
 	 */
 	public Vector<String> getArgument(String id)
 	{
 		for( int i=0; i<this.arguments.size(); i ++ )
 		{
 			if( this.arguments.get(i).get(0).equals(id) )
 			{
 				return this.arguments.get(i);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * setArguments
 	 * 
 	 * @param arguments
 	 * 		Inner Vector:
 	 * 		[0] = database id of the argument
 	 * 		[1] = value type of the argument
 	 * 		[2] = column name of the arguments table used as label
 	 * 		[3] = (optional) contains the label string
 	 */
 	public void setArguments(Vector<Vector<String>> arguments) 
 	{
 		this.arguments = arguments;
 	}
 	
 	/**
 	 * addArgument
 	 * 
 	 * @param id 
 	 * 		database id of the argument
 	 * @param type 
 	 * 		class name
 	 * @param column 
 	 * 		column name of the arguments table used as label
 	 * @param label (optional)
 	 */
 	public void addArgument(
 			String id, 
 			String type,
 			String column,
 			String label
 		)
 	{
 		Vector<String> arg = new Vector<String>();
 		arg.add(id);
 		arg.add(type);
 		arg.add(column);
 		arg.add(label);
 		
 		this.arguments.add(arg);
 	}
 
 	/**
 	 * removeArgument
 	 * 
 	 * @param id
 	 */
 	public void removeArgument(String id)
 	{
 		for( int i=0; i<this.arguments.size(); i ++ )
 		{
 			if( this.arguments.get(i).get(0).equals(id) )
 			{
 				this.arguments.remove(i);
 				return;
 			}
 		}
 	}
 	
 	/**
 	 * getScriptcode
 	 * 
 	 * @return
 	 */
 	public String getScriptcode() 
 	{
 		return this.scriptcode;
 	}
 
 	/**
 	 * setScriptcode
 	 * 
 	 * @param scriptcode
 	 */
 	public void setScriptcode(String scriptcode) 
 	{
 		this.scriptcode = scriptcode;
 	}
 	
 	/**
 	 * getReturnType
 	 * 
 	 * @return
 	 */
 	public Class<?> getReturnType() 
 	{
 		return returnType;
 	}
 
 	/**
 	 * setReturnType
 	 * 
 	 * @param returnType
 	 */
 	public void setReturnType(Class<?> returnType) 
 	{
 		this.returnType = returnType;
 	}
 	
 	/**
 	 * toString
 	 */
 	public String toString()
 	{
 		return this.renderStringForCell();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public Object clone()
 	{
 		Formula f = new Formula();
 		
 		f.arguments = (Vector<Vector<String>>) this.arguments.clone();
 		f.returnType = this.returnType;
 		f.scriptcode = new String(this.scriptcode);
 		
 		return f;
 	}
 }
