 package org.eclipse.jem.internal.proxy.vm.remote;
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ConnectionHandler.java,v $
 *  $Revision: 1.1 $  $Date: 2003/10/27 17:22:23 $ 
  */
 
 
 import java.io.*;
 import java.net.Socket;
 import java.net.SocketException;
 
 import org.eclipse.jem.internal.proxy.common.CommandException;
 import org.eclipse.jem.internal.proxy.common.remote.Commands;
 import org.eclipse.jem.internal.proxy.common.remote.UnexpectedExceptionCommandException;
 import org.eclipse.jem.internal.proxy.initParser.*;
 /**
  * This handles one connection.
  */
 
 public class ConnectionHandler {	
 		
 	protected Socket socket;
 	final protected RemoteVMServerThread server;
 	final protected Thread connectionThread;
 	protected DataInputStream in;
 	protected DataOutputStream out;
 	
 	// Kludge: Bug in Linux 1.3.xxx of JVM. Closing a socket while the socket is being read/accept will not interrupt the
 	// wait. Need to timeout to the socket read/accept before the socket close will be noticed. This has been fixed
 	// in Linux 1.4. So on Linux 1.3 need to put timeouts in on those sockets that can be separately closed while reading/accepting.
 	static boolean LINUX_1_3 = "linux".equalsIgnoreCase(System.getProperty("os.name")) && System.getProperty("java.version","").startsWith("1.3");	 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 	
 	/**
 	 * This may be running as a callback, in that case there is no connectionThread.
 	 */
 	public ConnectionHandler(Socket sc, RemoteVMServerThread svr, Thread connectionThread) {
 		socket = sc;
 		server = svr;
 		this.connectionThread = connectionThread;
 		
 		Integer bufSize = Integer.getInteger("proxyvm.bufsize"); //$NON-NLS-1$
 		if (bufSize == null)
 			bufSize = new Integer(5000);
 			
 		try {
 			out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), bufSize.intValue()));
 			in = new DataInputStream(new BufferedInputStream(socket.getInputStream(), bufSize.intValue()));
 		} catch (IOException e) {
 		}
 			
 	}
 	
 	/**
 	 * Did this construct correctly.
 	 * This is needed because an exception could be thrown in the ctor and
 	 * that's not a good thing to do.
 	 */
 	public boolean isConnected() {
 		return socket != null;
 	}	
 	
 	/**
 	 * Request the thread to close down.
 	 */
 	public void close() {
		try {
			Socket s = socket;
			socket = null;
			s.close();
		} catch (Exception e) {
 		}
 	}
 	
 	/**
 	 * Process and loop until told to stop. A callback_done will stop
 	 * the loop and will return a result. Otherwise null is returned.
 	 */
 	public Object run() throws CommandException {
 		Object result = null;		
 		boolean shutdown = false;
 		boolean closeWhenDone = true;
 		Commands.ValueObject valueObject = new Commands.ValueObject();	// Working value object so not continually recreated.
 		InvokableValueSender valueSender = new InvokableValueSender();	// Working valuesender so not continually recreated. 
 		try {
 			boolean doLoop = true;
 
 			/**
 			 * Note: In the cases below you will see a lot of finally clauses that null variables out.
 			 * This is because this is a long running loop, and variables declared within blocks are not
 			 * garbage collected until the method is terminated, so these variables once set would never
 			 * be GC'd. The nulling at the end of the case makes sure that any of those objects set are
 			 * now available for garbage collection when necessary.
 			 */		
 			while(doLoop && isConnected()) {
 				byte cmd = 0;
 				try {
 					if (LINUX_1_3)
 						socket.setSoTimeout(1000);	// Linux 1.3 bug, see comment on LINUX_1_3
 					cmd = in.readByte();
 					if (LINUX_1_3 && isConnected())
 						socket.setSoTimeout(0);	// Linux 1.3 bug, see comment on LINUX_1_3
 				} catch (InterruptedIOException e) {
 					continue;	// Timeout, try again
 				}
 				switch (cmd) {
 					case Commands.QUIT_CONNECTION:				
 						doLoop = false;	
 						break;	// Close this connection
 						
 					case Commands.TERMINATE_SERVER:
 						doLoop = false;		
 						shutdown = true;	// Shutdown everything
 						break;
 						
 					case Commands.GET_CLASS:
 						String className = in.readUTF();
 						Class aClass = null;
 						Class superClass = null;
 						String superClassName = null;
 						String componentTypeName = null;
 						int [] dimList = null;
 						boolean added = false;
 						try {
 							if (className.charAt(0) != '[') 
 								aClass = Class.forName(className);
 							else {
 								// It is an array. Strip off the type and try to find the class for it.
 								int dimEnd = className.lastIndexOf('[');
 								componentTypeName = className.substring(dimEnd+1);
 								if (componentTypeName.charAt(0) == 'L') {
 									// The type is a class
 									componentTypeName = componentTypeName.substring(1,componentTypeName.length()-1);	// Strip off begining 'L' and trailing ';'
 									aClass = Class.forName(componentTypeName);									
 								} else {
 									aClass = (Class) Commands.MAP_SHORTSIG_TO_TYPE.get(componentTypeName.substring(0,1));	// It is a primitive, get the mapping name
 									if (aClass == null)
 										throw new ClassNotFoundException();	// Type couldn't be found in the mapping table.
 									
 								}
 								
 								// Now comes the real kludge. We can't just find a class of type array. You need to have
 								// an array first and get its class.
 								dimList = new int[dimEnd+1];	// Each entry will be initialized to zero by default.
 								aClass = java.lang.reflect.Array.newInstance(aClass, dimList).getClass();
 							}
 								
 							added = server.getIdentityID(aClass, valueObject);
 							boolean isInterface = aClass.isInterface();
 							boolean isAbstract = java.lang.reflect.Modifier.isAbstract(aClass.getModifiers());
 							superClass = aClass.getSuperclass();
 							superClassName = (superClass != null) ? superClass.getName() : ""; //$NON-NLS-1$
 							out.writeByte(Commands.GET_CLASS_RETURN);
 							out.writeInt(valueObject.objectID);
 							out.writeBoolean(isInterface);
 							out.writeBoolean(isAbstract);
 							out.writeUTF(superClassName);
 							out.flush();
 						} catch (ClassNotFoundException e) {
 							valueObject.set();
 							Commands.sendErrorCommand(out, Commands.GET_CLASS_NOT_FOUND, valueObject);						
 						} catch (ExceptionInInitializerError e) {
 							sendException(e.getException(), valueObject, out);								
 						} catch (LinkageError e) {
 							sendException(e, valueObject, out);								
 						} catch (Throwable e) {
 							// Something bad, did we add a class? If we did remove it from the table.
 							if (added)
 								server.removeObject(server.getObject(valueObject.objectID));
 							throw e;
 						} finally {
 							// clear out for GC to work.
 							className = null;
 							aClass = null;
 							superClass = null;
 							superClassName = null;
 							valueObject.set();
 							componentTypeName = null;
 							dimList = null;
 						}
 						break;
 						
 					case Commands.GET_CLASS_FROM_ID:
 						int classID = in.readInt();
 						try {
 							aClass = (Class) server.getObject(classID);
 							boolean isInterface = aClass.isInterface();
 							boolean isAbstract = java.lang.reflect.Modifier.isAbstract(aClass.getModifiers());							
 							superClass = aClass.getSuperclass();
 							superClassName = (superClass != null) ? superClass.getName() : ""; //$NON-NLS-1$
 							out.writeByte(Commands.GET_CLASS_ID_RETURN);
 							out.writeUTF(aClass.getName());
 							out.writeBoolean(isInterface);
 							out.writeBoolean(isAbstract);
 							out.writeUTF(superClassName);
 							out.flush();
 						} catch (ClassCastException e) {
 							valueObject.set();
 							Commands.sendErrorCommand(out, Commands.CLASS_CAST_EXCEPTION, valueObject);
 						} finally {	
 							// clear out for GC to work.
 							aClass = null;
 							superClass = null;
 							superClassName = null;
 							valueObject.set();							
 						}
 						break;
 
 					case Commands.GET_OBJECT_DATA:
 						int objectID = in.readInt();
 						Object anObject = null;
 						try {
 							anObject = server.getObject(objectID);
 							valueObject.setObjectID(objectID, server.getIdentityID(anObject.getClass()));
 							Commands.writeValue(out, valueObject, true);
 						} catch (ClassCastException e) {
 							valueObject.set();
 							Commands.sendErrorCommand(out, Commands.CLASS_CAST_EXCEPTION, valueObject);
 						} finally {
 							anObject = null;	// Clear out for GC to work
 							valueObject.set();							
 						}
 						break;												
 						
 					case Commands.RELEASE_OBJECT:
 						int id = in.readInt();
 						server.removeObject(server.getObject(id));
 						break;						
 
 					case Commands.NEW_INIT_STRING:
 						classID = in.readInt();	// ID Of class to do new upon.
 						String initString = in.readUTF();	// The init string.
 						Object newValue = null;
 						Class theClass = null;
 						try {
 							theClass = (Class) server.getObject(classID);
 							if (theClass == null) {
 								// The class wasn't found. So imply ClassNotFound exception.
 								throw new ClassNotFoundException();
 							}
 							
 							InitializationStringParser parser = null;
 							try {
 								parser = InitializationStringParser.createParser(initString);
 								newValue = parser.evaluate();								
 								boolean primitive = parser.isPrimitive();
 								if (primitive != theClass.isPrimitive()) {
 									valueObject.set();
 									Commands.sendErrorCommand(out, Commands.CLASS_CAST_EXCEPTION, valueObject);
 									continue;	// Goto next command.
 								} 
 								if (primitive) {
 									try {
 										// Need to do special tests for compatibility and assignment.
 										sendObject(newValue, classID, valueObject, out, true);	// This will make sure it goes out as the correct primitive type
 									} catch (ClassCastException e) {
 										// The returned type is not of the correct type for what is expected.
 										valueObject.set();
 										Commands.sendErrorCommand(out, Commands.CLASS_CAST_EXCEPTION, valueObject);
 										continue;	// Goto next command
 									}									
 								} else {
 									if (newValue != null) {
 										// Test to see if they are compatible. (Null can be assigned to any object,
 										// so that is why it was tested out above).
 										if (!theClass.isInstance(newValue)) {
 											// The returned type is not of the correct type for what is expected.
 											valueObject.set();
 											Commands.sendErrorCommand(out, Commands.CLASS_CAST_EXCEPTION, valueObject);
 											continue;	// Goto next command
 										}
 									}
 									sendObject(newValue, NOT_A_PRIMITIVE, valueObject, out, true);	// Send out as an object.
 								}
 							} catch (InitializationStringEvaluationException e) {
 								if (e instanceof EvaluationException) {
 									// Want to return the real exception.
 									sendException(e.getOriginalException(), valueObject, out);
 								} else {
 									// Couldn't be evaluated, return an error for this.
 									setExceptionIntoValue(e.getOriginalException(), valueObject);
 									Commands.sendErrorCommand(out, Commands.CANNOT_EVALUATE_STRING, valueObject);
 								}
 							} finally {
 								parser = null;	// Clear out for GC to work
 							}									
 						} catch (Throwable e) {
 							sendException(e, valueObject, out);
 						} finally {
 							// Clear out for GC to work
 							initString = null;
 							theClass = null;
 							newValue = null;
 							valueObject.set();							
 						}	
 						break;						
 												
 					case Commands.INVOKE:
 						Object target = null;
 						Object[] parms = null;						
 						Class returnType = null;
 						java.lang.reflect.Method aMethod = null;						
 						try {					
 							int methodID = in.readInt();	// ID of method to invoke
 							aMethod = (java.lang.reflect.Method) server.getObject(methodID);	// Method to invoke
 							Commands.readValue(in, valueObject);
 							target = getInvokableObject(valueObject);	
 							Commands.readValue(in, valueObject);
 							if (valueObject.type == Commands.ARRAY_IDS) {
 								// It is an array containing IDs, as it normally would be.
 								valueSender.initialize(valueObject);
 								Commands.readArray(in, valueObject.anInt, valueSender, valueObject);
 								parms = (Object[]) valueSender.getArray();
 							} else {
 								// It is all objects or null, so it should be an Object[] or null. If not, then this is an error.
 								parms = (Object[]) valueObject.anObject;
 							}
 							
 							if (!aMethod.isAccessible())
 								aMethod.setAccessible(true);	// We will allow all to occur. Let access control be handled by IDE and compiler.
 							newValue = aMethod.invoke(target, parms);
 							returnType = aMethod.getReturnType();
 							if (returnType.isPrimitive()) {
 								int returnTypeID = server.getIdentityID(returnType);
 								// Need to tell sendObject the correct primitive type.
 								sendObject(newValue, returnTypeID, valueObject, out, true);
 				
 							} else {
 								sendObject(newValue, NOT_A_PRIMITIVE, valueObject, out, true);	// Just send the object back. sendObject knows how to iterpret the type
 							}									
 						} catch (CommandException e) {
 							throw e;	// Throw it again. These we don't want to come up as an exception proxy. These should end the thread.
 						} catch (java.lang.reflect.InvocationTargetException e) {
 							// This is a wrappered exception. Return the wrappered one so it looks like
 							// it was the real one. (Sometimes the method being invoked is on a java.lang.reflect.Constructor.newInstance,
 							// which in turn is an InvocationTargetException, so we will go until we don't have an InvocationTargetException.
 							Throwable t = e;
 							do {
 								t = ((java.lang.reflect.InvocationTargetException) t).getTargetException();
 							} while (t instanceof java.lang.reflect.InvocationTargetException);
 							sendException(t, valueObject, out);
 						} catch (Throwable e) {
 							sendException(e, valueObject, out);	// Turn it into a exception proxy on the client.
 						} finally {
 							// Clear out for GC to work
 							valueObject.set();
 							parms = null;
 							target = null;
 							aMethod = null;
 							returnType = null;
 							newValue = null;
 							valueSender.clear();
 						}					
 						break;
 						
 					case Commands.CALLBACK_DONE:
 						try {
 							if (connectionThread != null) {
 								valueObject.set();
 								Commands.sendErrorCommand(out, Commands.UNKNOWN_COMMAND_SENT, valueObject);
 							} else {
 								Commands.readValue(in, valueObject);
 								if (valueObject.type == Commands.ARRAY_IDS) {
 									// It is an array containing IDs, as it normally would be.
 									valueSender.initialize(valueObject);
 									Commands.readArray(in, valueObject.anInt, valueSender, valueObject);
 									result = (Object[]) valueSender.getArray();
 								} else {
 									result = getInvokableObject(valueObject);
 								}
 								doLoop = false;	// We need to terminate and return result
 								closeWhenDone = false;	// Don't close, we will continue.
 							}
 						} finally {
 							valueObject.set();
 							valueSender.clear();
 						}
 						break;
 						
 					case Commands.ERROR:
 						try {
 							// Got an error command. Don't know what to do but read the
 							// valud and simply print it out.
 							Commands.readValue(in, valueObject);
 							result = getInvokableObject(valueObject);
 							System.out.println("Error sent to server: Result=" + result); //$NON-NLS-1$
 						} finally {
 							valueObject.set();
 						}
 						break;
 							
 					default:
 						// Unknown command. We don't know how long it is, so we need to shut the connection down.
 						System.out.println("Error: Invalid cmd send to server: Cmd=" + cmd); //$NON-NLS-1$
 						doLoop = false;
 						closeWhenDone = true;
 						break;
 				}
 			}
 		} catch (EOFException e) {
 			// This is ok. It means that the connection on the other side was terminated.
 			// So just accept this and go down.
 		} catch (CommandException e) {
 			throw e;
 		} catch (SocketException e) {
 			if (socket != null)
 				throw new UnexpectedExceptionCommandException(false, e);	// socket null means a valid close request
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			if (closeWhenDone) {
 				if (in != null)
 					try {
 						in.close();
 					} catch (Exception e) {
 					}
 				if (out != null)
 					try {
 						out.close();
 					} catch (Exception e) {
 					}
 				close();
 			}
 		}
 		
 		if (closeWhenDone && connectionThread != null)
 			server.removeConnectionThread(connectionThread);
 		if (shutdown)
 			server.requestShutdown();
 			
 		return result;
 	}
 	
 protected static final int NOT_A_PRIMITIVE = Commands.NOT_AN_ID;
 protected static final int SEND_AS_IS = -2;	// This means sends as an object not as an id.
 	
 public static final int CLASS_ADDED = 1;
 public static final int OBJECT_ADDED = 2;
 public int fillInValue(Object object, int primitiveTypeID, Commands.ValueObject valueObject) {
 	int added = 0;
 	if (object == null) {
 		valueObject.set();
 	} else {
 		int classID = 0;
 		if (primitiveTypeID != NOT_A_PRIMITIVE && primitiveTypeID != SEND_AS_IS) {
 			classID = primitiveTypeID;	// The object is really supposed to be a primitive of this type
 			switch (classID) {
 				case Commands.BOOLEAN_TYPE:
 					valueObject.set(((Boolean) object).booleanValue());
 					break;
 				case Commands.INTEGER_TYPE:
 					if (object instanceof Character)
 						valueObject.set((int) ((Character) object).charValue());	// Because char can be widened to an int
 					else
 						valueObject.set(((Number) object).intValue());					
 					break;
 				case Commands.BYTE_TYPE:
 					valueObject.set(((Number) object).byteValue());
 					break;
 				case Commands.CHARACTER_TYPE:
 					valueObject.set(((Character) object).charValue());
 					break;
 				case Commands.DOUBLE_TYPE:
 					if (object instanceof Character)
 						valueObject.set((double) ((Character) object).charValue());	// Because char can be widened to a double
 					else
 						valueObject.set(((Number) object).doubleValue());					
 					break;
 				case Commands.FLOAT_TYPE:
 					if (object instanceof Character)
 						valueObject.set((float) ((Character) object).charValue());	// Because char can be widened to a float
 					else
 						valueObject.set(((Number) object).floatValue());					
 					break;
 				case Commands.SHORT_TYPE:
 					valueObject.set(((Number) object).shortValue());
 					break;
 				case Commands.LONG_TYPE:
 					if (object instanceof Character)
 						valueObject.set((long) ((Character) object).charValue());	// Because char can be widened to a long
 					else
 						valueObject.set(((Number) object).longValue());					
 					break;
 			}
 		} else {
 			// It's not a primitive.
 			boolean addObject = false, addClass = false;			
 			Class objClass = object.getClass();
 			classID = server.getIdentityID(objClass);
 			if (classID == RemoteVMServerThread.ID_NOT_FOUND) {
 				Commands.ValueObject classValue = new Commands.ValueObject();
 				addClass = server.getIdentityID(objClass, classValue);
 				if (addClass)
 					added |= CLASS_ADDED;
 				classID = classValue.objectID;
 			}
 		
 			switch (classID) {
 				case Commands.BYTE_CLASS:
 					valueObject.set((Byte) object);
 					break;
 				case Commands.CHARACTER_CLASS:
 					valueObject.set((Character) object);
 					break;
 				case Commands.DOUBLE_CLASS:
 					valueObject.set((Double) object);
 					break;
 				case Commands.FLOAT_CLASS:
 					valueObject.set((Float) object);
 					break;
 				case Commands.INTEGER_CLASS:
 					valueObject.set((Integer) object);
 					break;
 				case Commands.LONG_CLASS:
 					valueObject.set((Long) object);
 					break;
 				case Commands.SHORT_CLASS:
 					valueObject.set((Short) object);
 					break;
 				case Commands.BOOLEAN_CLASS:
 					valueObject.set((Boolean) object);
 					break;
 				case Commands.STRING_CLASS:
 					valueObject.set((String) object);
 					break;
 				case Commands.BIG_DECIMAL_CLASS:
 					valueObject.set(object, Commands.BIG_DECIMAL_CLASS);
 					break;
 				case Commands.BIG_INTEGER_CLASS:
 					valueObject.set(object, Commands.BIG_INTEGER_CLASS);
 					break;
 				default:
 					if (primitiveTypeID != SEND_AS_IS) {
 						addObject = server.getIdentityID(object, valueObject);
 						if (addObject) {
 							added |= OBJECT_ADDED;			
 							valueObject.setObjectID(valueObject.objectID, classID);
 						} else
 							valueObject.setObjectID(valueObject.objectID);
 					} else
 						valueObject.set(object, classID);
 					break;
 			}
 		}
 	}
 	
 	return added;
 }
 
 public void sendObject(Object object, int primitiveTypeID, Commands.ValueObject valueObject, DataOutputStream out, boolean writeAsCommand) throws CommandException {			
 	int added = fillInValue(object, primitiveTypeID, valueObject);
 	boolean sent = false;
 	try {
 		Commands.writeValue(out, valueObject, writeAsCommand);	// Write it back as a value command.
 		sent = true;
 	} finally {
 		if (!sent) {
 			// Ending due to some problem, so clean up any added objects from the id table.
 			if ((added & OBJECT_ADDED) != 0)
 				server.removeObject(valueObject.objectID);
 			if ((added & CLASS_ADDED) != 0)
 				server.removeObject(valueObject.classID);
 		}
 	}
 }
 		
 	
 	public void sendException(Throwable e, Commands.ValueObject value, DataOutputStream out) throws CommandException {
 		// Exception ocurred, return a value command with the exception within it.
 		setExceptionIntoValue(e, value);
 		Commands.sendErrorCommand(out, Commands.THROWABLE_SENT, value);		
 	}
 
 	public void setExceptionIntoValue(Throwable e, Commands.ValueObject value) {		
 		server.getIdentityID(e, value);	// It should always create it so we don't need to know.
 		int eID = value.objectID;
 		Class eClass = e.getClass();							
 		server.getIdentityID(eClass, value);
 		int eClassID = value.objectID;
 		value.setException(eID, eClassID);
 	}
 	
 	/**
 	 * From the valueObject, get an Object that is invokable (i.e. can be the target of an invoke, or one of its parms).
 	 * If it is an array type, the a ValueSender is returned. The invoker needs to then cast this
 	 * to a ValueSender and call the readArray routine.
 	 */
 	public Object getInvokableObject(final Commands.ValueObject value) {
 		switch (value.type) {
 			case Commands.NEW_OBJECT_ID:
 			case Commands.OBJECT_ID:
 				// These need to have access to the server to get the real object
 				return server.getObject(value.objectID);						
 			default:
 				// These have all the info needed within the value object itself, so ask it.
 				return value.getAsObject();
 		}
 	}
 	
 	// Helper class for getting an array.
 	private class InvokableValueSender implements Commands.ValueSender {
 		int index = 0;
 		Object array;
 
 		public InvokableValueSender() {
 		}
 				
 		public InvokableValueSender(Commands.ValueObject arrayHeader) {
 			initialize(arrayHeader);
 		}
 		
 		public void initialize(Commands.ValueObject arrayHeader) {
 			index = 0;
 			Class arrayType = (Class) server.getObject(arrayHeader.classID);
 			array = java.lang.reflect.Array.newInstance(arrayType, arrayHeader.anInt);
 		}
 		
 		public void clear() {
 			array = null;
 			index = 0;
 		}
 		
 		public Object getArray() {
 			return array;
 		}
 					
 		// A new value is being sent to the array
 		public void sendValue(Commands.ValueObject value) {
 			java.lang.reflect.Array.set(array, index++, getInvokableObject(value));	// add it to the array
 		}
 					
 		// The next entry is an array too!
 		public Commands.ValueSender nestedArray(Commands.ValueObject arrayHeader) {
 			InvokableValueSender sender = new InvokableValueSender(arrayHeader);
 			// Take the newly created array and put it into the current array.
 			java.lang.reflect.Array.set(array, index++, sender.getArray());
 			return sender;
 		}
 	}
 
 }
 
 
