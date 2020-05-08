 /*******************************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ExpressionProcesserController.java,v $
 *  $Revision: 1.2 $  $Date: 2004/05/26 22:02:08 $ 
  */
 package org.eclipse.jem.internal.proxy.vm.remote;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.jem.internal.proxy.common.CommandException;
 import org.eclipse.jem.internal.proxy.common.MapTypes;
 import org.eclipse.jem.internal.proxy.common.remote.Commands;
 import org.eclipse.jem.internal.proxy.common.remote.ExpressionCommands;
 import org.eclipse.jem.internal.proxy.initParser.EvaluationException;
 import org.eclipse.jem.internal.proxy.initParser.MethodHelper;
 import org.eclipse.jem.internal.proxy.initParser.tree.ExpressionProcesser;
 import org.eclipse.jem.internal.proxy.initParser.tree.IInternalExpressionConstants;
 import org.eclipse.jem.internal.proxy.initParser.tree.IExpressionConstants.NoExpressionValueException;
 
  
 /**
  * This processes the commands for expression processing and sends them over
  * to the common expression processer.
  * 
  * This will be instantiated on the start of an expression. And then
  * each expression request from the IDE will be sent into here. The
  * reason this guy doesn't hold onto the connection and process the
  * entire expression is because we need to return to the connection
  * handler to keep the connection live (there is timeouts and stuff
  * in there that we don't want to duplicate here).
  * <p>
  * If there are any expression processing errors (versus hard io errors) we
  * will save up the error but don't do any more processing other than to make
  * sure we read the complete subcommand. This is so that the inputstream is left
  * in a valid state without standed data.
  * <p>
  * The at the sync point (either get value or sync subcommand) we will send back
  * the error.
  *  
  * @since 1.0.0
  */
 public class ExpressionProcesserController {
 
 	protected final RemoteVMServerThread server;
 	protected final ConnectionHandler connHandler;	
 	protected final ExpressionProcesser exp;
 	protected Commands.ValueObject workerValue = new Commands.ValueObject();	// A worker value object so we don't need to keep creating them and releasing them.
 	private ClassLoader classLoader;
 	
 	/**
 	 * An error has occurred. At this point all subcommands will simply make sure they flush the input stream
 	 * correctly, but they do not process it.
 	 * 
 	 * @since 1.0.0
 	 */
 	protected boolean errorOccurred = false;
 	
 	private String novalueMsg = null;	// If NoExpressionValueException then this is the msg. 
 	private Throwable exception = null;	// Was there another kind of exception that was caught.
 	
 	/**
 	 * Create with a default expression processer.
 	 * @param server
 	 * 
 	 * @since 1.0.0
 	 */
 	public ExpressionProcesserController(RemoteVMServerThread server, ConnectionHandler connHandler) {
 		this(server, connHandler, new ExpressionProcesser());
 	}
 	
 	/**
 	 * Create from a subclass with a given expression processer.
 	 * 
 	 * @param server
 	 * @param exp
 	 * 
 	 * @since 1.0.0
 	 */
 	protected ExpressionProcesserController(RemoteVMServerThread server, ConnectionHandler connHandler, ExpressionProcesser exp) {
 		this.server = server;
 		this.connHandler = connHandler;
 		this.exp = exp;
 	}
 	
 	/**
 	 * Set the class loader to use for finding classes. If never set, or if <code>null</code>, then
 	 * <code>Class.forName</code> will be used.
 	 * 
 	 * @param classLoader
 	 * 
 	 * @since 1.0.0
 	 */
 	public void setClassLoader(ClassLoader classLoader) {
 		this.classLoader = classLoader;
 	}
 	
 	/*
 	 * Array of primitive type names. Used to look up primtive types in primitive types array. 
 	 * 
 	 * @since 1.0.0
 	 */
 	private static final List PRIMITIVE_NAMES = Arrays.asList(new String[] {"byte", "char", "short", "int", "long", "float", "double"});
 	private static final Class[] PRIMITIVE_TYPES = new Class[] {Byte.TYPE, Character.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE};
 	/**
 	 * Load the class given the name. If not found, return null.
 	 * 
 	 * @param className
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected Class loadClass(String className) throws ClassNotFoundException {
 		if (className == null)
 			return null;
 		else if (className.endsWith("[]")) {
 			// We have an array request instead. This is trickier.
 			return loadClass(MapTypes.getJNIFormatName(className));
 		} else {
 			int primIndex = PRIMITIVE_NAMES.indexOf(className);
 			if (primIndex >= 0)
 				return PRIMITIVE_TYPES[primIndex];
 			else if (classLoader == null) {
 				return Class.forName(className);
 			} else {
 				return classLoader.loadClass(className);
 			}
 		}
 	}
 
 	/**
 	 * Now process the input stream. If either throws occurs, this is a hard error and we must terminate
 	 * the entire connection. The input stream is in an unknown state.
 	 * 
 	 * @param in The input stream to get the data for the current sub-command.
 	 * 
 	 * @throws CommandException
 	 * @throws IOException
 	 * @since 1.0.0
 	 */
 	public void process(DataInputStream in) throws CommandException, IOException {
 		// In the following subcommand processing, we always read the entire subcommand from the stream.
 		// Then we check if an error has occurred in the past. If it has, we simply break. This is because
 		// once an error occurred we don't want to continue wasting time evaluating, however we need to make
 		// sure that the stream is read completely so that we don't have a corrupted input stream. That way
 		// when all is done we can return the error and still have a valid connection socket.
 		byte subcommand = in.readByte();
 		try {
 			switch (subcommand) {
 				case IInternalExpressionConstants.PUSH_TO_PROXY_EXPRESSION :
 					// Getting a proxy push. The value is sent as valueObject, so use that to read it in.
 					Commands.readValue(in, workerValue);
 					if (errorOccurred)
 						break;
 					Object value = connHandler.getInvokableObject(workerValue);
 					if (value == null)
 						exp.pushExpression(null, MethodHelper.NULL_TYPE);
 					else if (workerValue.isPrimitive())
 						exp.pushExpression(value, workerValue.getPrimitiveType());
 					else
 						exp.pushExpression(value, value.getClass());
 					break;
 
 				case IInternalExpressionConstants.CAST_EXPRESSION :
 					// Get a cast request. The type is sent as valueObject.
 					Commands.readValue(in, workerValue);
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushCast((Class) value);
 					} catch (NoExpressionValueException e) {
 						processException(e);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.INSTANCEOF_EXPRESSION :
 					// Get a instanceof request. The type is sent as valueObject.
 					Commands.readValue(in, workerValue);
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushInstanceof((Class) value);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					} catch (NoExpressionValueException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.INFIX_EXPRESSION :
 					// Get an infix request. The operator and operand type are sent as bytes.
 					byte infix_operator = in.readByte();
 					byte infix_operandType = in.readByte();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushInfix(infix_operator, infix_operandType);
 					} catch (NoExpressionValueException e1) {
 						processException(e1);
 					}
 					break;
 
 				case IInternalExpressionConstants.PREFIX_EXPRESSION :
 					// Get a prefix request. The operator is sent as byte.
 					byte prefix_operandType = in.readByte();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushPrefix(prefix_operandType);
 					} catch (NoExpressionValueException e2) {
 						processException(e2);
 					}
 					break;
 
 				case IInternalExpressionConstants.TYPELITERAL_EXPRESSION :
 					// Get a type literal request. The type string is sent as valueObject.
 					Commands.readValue(in, workerValue);
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						value = loadClass((String) value);
 						exp.pushExpression((Class) value, Class.class);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.ARRAY_ACCESS_EXPRESSION :
 					// Get an array access request. The index cound is sent as int.
 					int arrayAccess_Indexcount = in.readInt();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushArrayAccess(arrayAccess_Indexcount);
 					} catch (NoExpressionValueException e3) {
 						processException(e3);
 					}
 					break;
 
 				case IInternalExpressionConstants.ARRAY_CREATION_EXPRESSION :
 					// Get an array creation request. The type is sent as valueObject, followed by int dimension count.
 					Commands.readValue(in, workerValue);
 					int arrayCreation_dimCount = in.readInt();
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushArrayCreation((Class) value, arrayCreation_dimCount);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					} catch (NoExpressionValueException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.ARRAY_INITIALIZER_EXPRESSION :
 					// Get an array initializer request. The type is sent as valueObject, followed by int expression count.
 					Commands.readValue(in, workerValue);
 					int arrayInitializer_expressionCount = in.readInt();
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushArrayInitializer((Class) value, arrayInitializer_expressionCount);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					} catch (NoExpressionValueException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.CLASS_INSTANCE_CREATION_EXPRESSION :
 					// Get a class instance creation request. The type is sent as valueObject, followed by int argument count.
 					Commands.readValue(in, workerValue);
 					int newInstance_argCount = in.readInt();
 					if (errorOccurred)
 						return;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushClassInstanceCreation((Class) value, newInstance_argCount);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					} catch (EvaluationException e) {
 						processException(e);
 					} catch (NoExpressionValueException e) {
 						processException(e);
 					} catch (InstantiationException e) {
 						processException(e);
 					} catch (IllegalAccessException e) {
 						processException(e);
 					} catch (InvocationTargetException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.TYPERECEIVER_EXPRESSION :
 					// Get a type receiver request. The type is sent as valueObject.
 					Commands.readValue(in, workerValue);
 					if (errorOccurred)
 						break;
 					value = connHandler.getInvokableObject(workerValue);
 					try {
 						if (value instanceof String)
 							value = loadClass((String) value);
 						exp.pushExpression(value, (Class) value);
 					} catch (ClassNotFoundException e) {
 						processException(e);
 					}
 					break;
 
 				case IInternalExpressionConstants.FIELD_ACCESS_EXPRESSION :
 					// Get a field access request. The field name sent as string, followed by hasReceiver as boolean.
 					String fieldAccess_name = in.readUTF();
 					boolean fieldAccess_receiver = in.readBoolean();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushFieldAccess(fieldAccess_name, fieldAccess_receiver);
 					} catch (NoExpressionValueException e4) {
 						processException(e4);
 					} catch (NoSuchFieldException e4) {
 						processException(e4);
 					} catch (IllegalAccessException e4) {
 						processException(e4);
 					}
 					break;
 
 				case IInternalExpressionConstants.METHOD_EXPRESSION :
 					// Get a method invocation request. The method name sent as string, followed by hasReceiver as boolean, and argCount as int.
 					String method_name = in.readUTF();
 					boolean method_receiver = in.readBoolean();
 					int method_argCount = in.readInt();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushMethodInvocation(method_name, method_receiver, method_argCount);
 					} catch (EvaluationException e5) {
 						processException(e5);
 					} catch (NoExpressionValueException e5) {
 						processException(e5);
 					} catch (IllegalAccessException e5) {
 						processException(e5);
 					} catch (InvocationTargetException e5) {
 						processException(e5);
 					}
 					break;
 
 				case IInternalExpressionConstants.CONDITIONAL_EXPRESSION :
 					// Get a conditional expression request. The expression type (ie. condition/true/false) is sent as a byte
 					byte conditional_type = in.readByte();
 					if (errorOccurred)
 						break;
 					try {
 						exp.pushConditional(conditional_type);
 					} catch (NoExpressionValueException e6) {
 						processException(e6);
 					}
 					break;
 			}
 			
 		} catch (RuntimeException e) {
 			processException(e);
 		}
 		
 		workerValue.set();	// Clear it out so nothing being held onto.
 	}
 	
 
 	/**
 	 * Pull the value. If an error occurred during this, then <code>null</code> is returned, and
 	 * caller should do normal error processing.
 	 * 
 	 * @return r[0] is the value, r[1] is the type of the value. <code>null</code> if there was an error.
 	 * 
 	 * @since 1.0.0
 	 */
 	public Object[] pullValue() {
 		Object[] result = new Object[2];
 		try {
 			exp.pullValue(result);
 			return result;
 		} catch (NoExpressionValueException e) {
 			processException(e);
 		}
 		return null;
 	}
 	
 	/**
 	 * Process all other exceptions then the NoExpressionValueException, InvocationTargetException, and EvaluationException. This is
 	 * for exceptions that are not related to the input stream and shouldn't
 	 * cause the input stream to be closed.
 	 * 
 	 * @param e
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void processException(Exception e) {
 		// Process all other exceptions.
 		errorOccurred = true;
 		exception = e;
 	}
 	
 	/**
 	 * Process InvocationTargetException to retrieve the actual target.
 	 * @param e
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void processException(InvocationTargetException e) {
 		errorOccurred = true;
 		exception = e.getTargetException();
 	}
 	
 	/**
 	 * Process EvaluationException to retrieve the actual target.
 	 * @param e
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void processException(EvaluationException e) {
 		errorOccurred = true;
 		exception = e.getOriginalException();
 	}	
 
 	/**
 	 * Special case for NoExpressionValueException caught.
 	 * @param e
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final void processException(NoExpressionValueException e) {
 		errorOccurred = true;
 		novalueMsg = e.getLocalizedMessage();
 		if (novalueMsg == null)
 			novalueMsg = "";	// It was null, so just default of empty string so we know it had occurred.
 	}
 
 	/**
 	 * Return whether there are any errors.
 	 * 
 	 * @return <code>true</code> if no errors.
 	 * 
 	 * @since 1.0.0
 	 */
 	public boolean noErrors() {
 		return !errorOccurred;
 	}
 	
 	/**
 	 * Return the error code from ExpressionCommands if non-exception error occurred.
 	 * Return zero if no error.
 	 * 
 	 * @return Zero if no error code to return, else error code from ExpressionCommands.
 	 * 
 	 * @see org.eclipse.jem.internal.proxy.common.remote.ExpressionCommands#ExpressionNoExpressionValueException
 	 * 
 	 * @since 1.0.0
 	 */
 	public int getErrorCode() {
 		if (novalueMsg != null)
 			return ExpressionCommands.ExpressionNoExpressionValueException;
 		else
 			return 0;
 	}
 	
 	/**
 	 * Return the error message, should only be called if <code>getErrorCode()</code> returned non-zero.
 	 * 
 	 * @return The error message if any. <code>null</code> otherwise.
 	 * 
 	 * @since 1.0.0
 	 */
 	public String getErrorMsg() {
 		if (novalueMsg != null && novalueMsg.length() > 0)
 			return novalueMsg;
 		else
 			return null;
 	}
 	
 	/**
 	 * Return the throwable if a Throwable was caught.
 	 * 
 	 * @return The throwable, or <code>null</code> if not set.
 	 * 
 	 * @since 1.0.0
 	 */
 	public Throwable getErrorThrowable() {
 		return exception;
 	}
 	
 	/**
 	 * Close out things.
 	 * 
 	 * @since 1.0.0
 	 */
 	public void close() {
 		exp.close();
 	}
 
 }
