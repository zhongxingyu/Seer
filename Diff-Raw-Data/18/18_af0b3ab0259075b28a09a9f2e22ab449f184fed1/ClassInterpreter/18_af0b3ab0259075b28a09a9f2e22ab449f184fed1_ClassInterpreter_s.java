 /*******************************************************************************
  *
  *	Copyright (C) 2008 Fujitsu Services Ltd.
  *
  *	Author: Nick Battle
  *
  *	This file is part of VDMJ.
  *
  *	VDMJ is free software: you can redistribute it and/or modify
  *	it under the terms of the GNU General Public License as published by
  *	the Free Software Foundation, either version 3 of the License, or
  *	(at your option) any later version.
  *
  *	VDMJ is distributed in the hope that it will be useful,
  *	but WITHOUT ANY WARRANTY; without even the implied warranty of
  *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *	GNU General Public License for more details.
  *
  *	You should have received a copy of the GNU General Public License
  *	along with VDMJ.  If not, see <http://www.gnu.org/licenses/>.
  *
  ******************************************************************************/
 
 package org.overturetool.vdmj.runtime;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.overturetool.vdmj.debug.DBGPReader;
 import org.overturetool.vdmj.definitions.ClassDefinition;
 import org.overturetool.vdmj.definitions.ClassList;
 import org.overturetool.vdmj.definitions.Definition;
 import org.overturetool.vdmj.definitions.DefinitionSet;
 import org.overturetool.vdmj.definitions.LocalDefinition;
 import org.overturetool.vdmj.expressions.Expression;
 import org.overturetool.vdmj.lex.Dialect;
 import org.overturetool.vdmj.lex.LexLocation;
 import org.overturetool.vdmj.lex.LexNameToken;
 import org.overturetool.vdmj.lex.LexTokenReader;
 import org.overturetool.vdmj.messages.VDMError;
 import org.overturetool.vdmj.messages.VDMErrorsException;
 import org.overturetool.vdmj.pog.ProofObligationList;
 import org.overturetool.vdmj.statements.CallObjectStatement;
 import org.overturetool.vdmj.statements.Statement;
 import org.overturetool.vdmj.syntax.ExpressionReader;
 import org.overturetool.vdmj.syntax.StatementReader;
 import org.overturetool.vdmj.traces.CallSequence;
 import org.overturetool.vdmj.traces.Verdict;
 import org.overturetool.vdmj.typechecker.Environment;
 import org.overturetool.vdmj.typechecker.FlatCheckedEnvironment;
 import org.overturetool.vdmj.typechecker.FlatEnvironment;
 import org.overturetool.vdmj.typechecker.NameScope;
 import org.overturetool.vdmj.typechecker.PrivateClassEnvironment;
 import org.overturetool.vdmj.typechecker.PublicClassEnvironment;
 import org.overturetool.vdmj.typechecker.TypeChecker;
 import org.overturetool.vdmj.types.Type;
 import org.overturetool.vdmj.util.Utils;
 import org.overturetool.vdmj.values.NameValuePair;
 import org.overturetool.vdmj.values.NameValuePairList;
 import org.overturetool.vdmj.values.NameValuePairMap;
 import org.overturetool.vdmj.values.ObjectValue;
 import org.overturetool.vdmj.values.Value;
 
 /**
  * The VDM++ interpreter.
  */
 
 public class ClassInterpreter extends Interpreter
 {
 	private final ClassList classes;
 	private ClassDefinition defaultClass;
 	private NameValuePairMap createdValues;
 	private DefinitionSet createdDefinitions;
 
 	public ClassInterpreter(ClassList classes) throws Exception
 	{
 		this.classes = classes;
 		this.createdValues = new NameValuePairMap();
 		this.createdDefinitions = new DefinitionSet();
 
 		if (classes.isEmpty())
 		{
 			setDefaultName(null);
 		}
 		else
 		{
 			setDefaultName(classes.get(0).name.name);
 		}
 	}
 
 	@Override
 	public void setDefaultName(String cname) throws Exception
 	{
 		if (cname == null)
 		{
 			defaultClass = new ClassDefinition();
 			classes.add(defaultClass);
 		}
 		else
 		{
     		for (ClassDefinition c: classes)
     		{
     			if (c.name.name.equals(cname))
     			{
     				defaultClass = c;
     				return;
     			}
     		}
 
     		throw new Exception("Class " + cname + " not loaded");
 		}
 	}
 
 	@Override
 	public String getDefaultName()
 	{
 		return defaultClass.name.name;
 	}
 
 	@Override
 	public File getDefaultFile()
 	{
 		return defaultClass.name.location.file;
 	}
 
 	@Override
 	public Set<File> getSourceFiles()
 	{
 		return classes.getSourceFiles();
 	}
 
 	public ClassList getClasses()
 	{
 		return classes;
 	}
 
 	@Override
 	public String getInitialContext()
 	{
 		return initialContext.toString() +
 			(createdValues.isEmpty() ? "" :
 				Utils.listToString("", createdValues.asList(), "\n", "\n"));
 	}
 
 	@Override
 	public Environment getGlobalEnvironment()
 	{
 		return new PublicClassEnvironment(classes);
 	}
 
 	@Override
 	public void init(DBGPReader dbgp)
 	{
 		initialContext = classes.initialize(dbgp);
 		createdValues = new NameValuePairMap();
 		createdDefinitions = new DefinitionSet();
 	}
 
 	@Override
 	public Expression parseExpression(String line, String module)
 		throws Exception
 	{
 		LexTokenReader ltr = new LexTokenReader(line, Dialect.VDM_PP);
 		ExpressionReader reader = new ExpressionReader(ltr);
 		reader.setCurrentModule(module);
 		return reader.readExpression();
 	}
 
 	@Override
 	public Statement parseStatement(String line, String module)
 		throws Exception
 	{
 		LexTokenReader ltr = new LexTokenReader(line, Dialect.VDM_PP);
 		StatementReader sr = new StatementReader(ltr);
 		sr.setCurrentModule(module);
 		return sr.readStatement();
 	}
 
 	@Override
 	public Type typeCheck(Expression expr, Environment env)
 		throws Exception
 	{
 		TypeChecker.clearErrors();
 		Type type = expr.typeCheck(env, null, NameScope.NAMESANDSTATE);
 
 		if (TypeChecker.getErrorCount() > 0)
 		{
 			// TypeChecker.printErrors(Console.out);
 			throw new VDMErrorsException(TypeChecker.getErrors());
 		}
 
 		return type;
 	}
 
 	@Override
 	public Type typeCheck(Statement stmt, Environment env)
 		throws Exception
 	{
 		TypeChecker.clearErrors();
 		Type type = stmt.typeCheck(env, NameScope.NAMESANDSTATE);
 
 		if (TypeChecker.getErrorCount() > 0)
 		{
 			// TypeChecker.printErrors(Console.out);
 			throw new VDMErrorsException(TypeChecker.getErrors());
 		}
 
 		return type;
 	}
 
 	private Value execute(Expression expr, DBGPReader dbgp)
 	{
 		mainContext = new StateContext(
 			defaultClass.name.location, "global static scope");
 
 		mainContext.putAll(initialContext);
 		mainContext.putAll(createdValues);
 		mainContext.setThreadState(dbgp);
 		clearBreakpointHits();
 
 		return expr.eval(mainContext);
 	}
 
 	/**
 	 * Parse the line passed, type check it and evaluate it as an expression
 	 * in the initial context.
 	 *
 	 * @param line A VDM expression.
 	 * @return The value of the expression.
 	 * @throws Exception Parser, type checking or runtime errors.
 	 */
 
 	@Override
 	public Value execute(String line, DBGPReader dbgp) throws Exception
 	{
 		Expression expr = parseExpression(line, getDefaultName());
 		Environment env = getGlobalEnvironment();
 		Environment created =
 			new FlatCheckedEnvironment(createdDefinitions.asList(), env);
 
 		typeCheck(expr, created);
 		return execute(expr, dbgp);
 	}
 
 	/**
 	 * Parse the line passed, and evaluate it as an expression in the context
 	 * passed.
 	 *
 	 * @param line A VDM expression.
 	 * @param ctxt The context in which to evaluate the expression.
 	 * @return The value of the expression.
 	 * @throws Exception Parser or runtime errors.
 	 */
 
 	@Override
 	public Value evaluate(String line, Context ctxt) throws Exception
 	{
 		Expression expr = parseExpression(line, getDefaultName());
 		LexNameToken classname =
 			new LexNameToken("CLASS", ctxt.location.module, ctxt.location);
 		ClassDefinition me = (ClassDefinition)classes.findType(classname);
 		PublicClassEnvironment globals = new PublicClassEnvironment(classes);
 		Environment env = new PrivateClassEnvironment(me, globals);
 
 		try
 		{
 			typeCheck(expr, env);
 		}
 		catch (VDMErrorsException e)
 		{
 			// We don't care... we just needed to type check it.
 		}
 
 		ctxt.threadState.init();
 		return expr.eval(ctxt);
 	}
 
 	@Override
 	public ClassDefinition findClass(String classname)
 	{
 		LexNameToken name = new LexNameToken("CLASS", classname, null);
 		return (ClassDefinition)classes.findType(name);
 	}
 
 	@Override
 	public Value findGlobal(LexNameToken name)
 	{
 		// The name will not be type-qualified, so we can't use the usual
 		// findName methods
 
 		for (ClassDefinition c: classes)
 		{
 			for (Definition d: c.definitions)
 			{
 				if (d.isFunctionOrOperation())
 				{
 					NameValuePairList nvpl = d.getNamedValues(initialContext);
 
 					for (NameValuePair n: nvpl)
 					{
 						if (n.name.matches(name))
 						{
 							return n.value;
 						}
 					}
 				}
 			}
 
 			for (Definition d: c.allInheritedDefinitions)
 			{
 				if (d.isFunctionOrOperation())
 				{
 					NameValuePairList nvpl = d.getNamedValues(initialContext);
 
 					for (NameValuePair n: nvpl)
 					{
 						if (n.name.matches(name))
 						{
 							return n.value;
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public Statement findStatement(File file, int lineno)
 	{
 		return classes.findStatement(file, lineno);
 	}
 
 	@Override
 	public Expression findExpression(File file, int lineno)
 	{
 		return classes.findExpression(file, lineno);
 	}
 
 	public void create(String var, String exp) throws Exception
 	{
 		Expression expr = parseExpression(exp, getDefaultName());
 		Environment env = getGlobalEnvironment();
 		Environment created =
 			new FlatCheckedEnvironment(createdDefinitions.asList(), env);
 
 		Type type = typeCheck(expr, created);
 		Value v = execute(exp, null);
 
 		LexLocation location = defaultClass.location;
 		LexNameToken n = new LexNameToken(defaultClass.name.name, var, location);
 
 		createdValues.put(n, v);
 		createdDefinitions.add(new LocalDefinition(location, n, NameScope.LOCAL, type));
 	}
 
 	@Override
 	public ProofObligationList getProofObligations()
 	{
 		return classes.getProofObligations();
 	}
 
 	public List<Object> runtrace(String classname, String... statements)
 	{
 		return runtrace(classname, Arrays.asList(statements));
 	}
 
 	public List<Object> runtrace(String classname, List<String> statements)
 	{
 		List<Object> list = new Vector<Object>();
 		ClassDefinition classdef = findClass(classname);
 
 		if (classdef == null)
 		{
 			list.add("Class " + classname + " not found");
 			return list;
 		}
 
 		Environment env = new FlatEnvironment(
 			classdef.getSelfDefinition(),
 			new PrivateClassEnvironment(classdef, getGlobalEnvironment()));
 
 		ObjectValue object = null;
 
 		try
 		{
 			object = classdef.newInstance(null, null, initialContext);
 		}
 		catch (ValueException e)
 		{
 			list.add(e.getMessage());
 			return list;
 		}
 
 		Context ctxt = new ObjectContext(
 				classdef.name.location, classdef.name.name + "()",
 				initialContext, object);
 
 		ctxt.put(classdef.name.getSelfName(), object);
 
 		for (String statement: statements)
 		{
 			try
 			{
 				Statement s = parseStatement(statement, classname);
 				typeCheck(s, env);
 
 				if (TypeChecker.getErrorCount() != 0)
 				{
 					List<VDMError> errors = TypeChecker.getErrors();
 					list.add(Utils.listToString(errors, " and "));
 					break;
 				}
 				else
 				{
 					list.add(s.eval(ctxt));
 				}
 			}
 			catch (Exception e)
 			{
 				list.add(e.getMessage());
 				break;
 			}
 		}
 
 		return list;
 	}
 
 	public List<Object> runtrace(Environment env, CallSequence statements)
 	{
 		List<Object> list = new Vector<Object>();
 
 		try
 		{
			Context copy = statements.ctxt.copy();
			copy.setThreadState(null);

 			for (CallObjectStatement statement: statements)
 			{
 				try
 				{
 					typeCheck(statement, env);
 				}
 				catch (Exception e)
 				{
 					// "Better to have tried and failed than never to have
 					// tried at all" :-)
 				}
 
 				list.add(statement.eval(copy));
 			}
 
 			list.add(Verdict.PASSED);
 		}
 		catch (ContextException e)
 		{
 			list.add(e.getMessage());
 
 			switch (e.number)
 			{
 				case 4055:	// precondition fails for functions
 				case 4071:	// precondition fails for operations
 				case 4087:	// invalid type conversion
 				case 4060:	// type invariant failure
 				case 4130:	// class invariant failure
					list.add(Verdict.INCONCLUSIVE);
 					break;
 
 				default:
 					list.add(Verdict.FAILED);
 			}
 		}
 		catch (Exception e)
 		{
 			list.add(e.getMessage());
 			list.add(Verdict.FAILED);
 		}
 
 		return list;
 	}
 }
