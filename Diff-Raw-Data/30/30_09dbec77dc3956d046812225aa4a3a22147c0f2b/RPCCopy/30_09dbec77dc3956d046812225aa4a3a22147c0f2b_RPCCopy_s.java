 /**
  * 
  */
 package com.google.gwt.user.server.rpc;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 /**
  * Encapsulation of both RPCCopy for GWT 1.4 and GWT 1.5
  * (damned invokeAndEncodeResponse !!!!!)
  * 
  * @author bruno.marchesson
  */
 public class RPCCopy
 {
 	//----
 	// Enumeration
 	//----
 	/**
 	 * GWT Version
 	 */
 	enum Version
 	{
 		GWT14,
 		GWT15
 	}
 	
 	//----
 	// Attributes
 	//----
 	/**
 	 * The current GWT version
 	 */
 	protected Version _version;
 	
 	//----
 	// Singleton
 	//----
 	/**
 	 * The unique instance of the singleton
 	 */
 	private static RPCCopy _instance = null;
 
 	/**
 	 * @return the unique instance of the singleton
 	 */
 	public static RPCCopy getInstance()
 	{
 		if (_instance == null)
 		{
 			_instance = new RPCCopy();
 		}
 		return _instance;
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Constructor
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Private constructor of the singleton
 	 */
 	private RPCCopy()
 	{
 		_version = Version.GWT15;
 		
 	//	GWT version detection, based on RPC method parsing
 	//	(findInterfaceMethod is present in GWT 1.4 and not 1.5)
 	//
 		try
 		{
 			Method[] methods = RPC.class.getDeclaredMethods();
 			for (int index = 0; index < methods.length; index++)
 			{
 				if ("findInterfaceMethod".equals(methods[index].getName()))
 				{
 					_version = Version.GWT14;
 					break;
 				}
 			}
 		} 
 		catch (SecurityException e)
 		{
 			e.printStackTrace();
 		}
 		
 		System.out.println(_version.toString());
 	}
 
 	
 	//-------------------------------------------------------------------------
 	//
 	// Encapsulated methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Decode request method
 	 */
 	public RPCRequest decodeRequest(String encodedRequest, Class type,
 		      SerializationPolicyProvider serializationPolicyProvider)
 	{
 		if (_version == Version.GWT14)
 		{
 			return RPCCopy_GWT14.decodeRequest(encodedRequest, type, 
 											   serializationPolicyProvider);
 		}
 		else
 		{
 			return RPCCopy_GWT15.decodeRequest(encodedRequest, type, 
 											   serializationPolicyProvider);
 		}
 	}
 
 	/**
 	 * Invoke method
 	 * @throws InvocationTargetException 
 	 * @throws SerializationException 
 	 */
 	public Object invoke(Object target,
 		      			 Method serviceMethod, Object[] args,
 		      			 SerializationPolicy serializationPolicy) 
 						 throws SerializationException, InvocationTargetException
 	{
 		if (_version == Version.GWT14)
 		{
 			return RPCCopy_GWT14.invoke(target, serviceMethod, 
 										args, serializationPolicy);
 		}
 		else
 		{
 			return RPCCopy_GWT15.invoke(target, serviceMethod, 
 					args, serializationPolicy);
 		}
 	}
 	
 	/**
 	 * Encode successful response method.
 	 * @throws SerializationException 
 	 */
 	public String encodeResponseForSuccess(Method serviceMethod, Object object, 
 										   SerializationPolicy serializationPolicy)
 										   throws SerializationException
 	{
 		if (_version == Version.GWT14)
 		{
 			return RPCCopy_GWT14.encodeResponseForSuccess(serviceMethod, object, 
 														  serializationPolicy);
 		}
 		else
 		{
 			return RPCCopy_GWT15.encodeResponseForSuccess(serviceMethod, object, 
 					  									  serializationPolicy);
 		}
 	}
 
 	/**
 	 * Encode failure response method.
 	 * @throws SerializationException 
 	 */
 	public String encodeResponseForFailure(Method serviceMethod, Throwable cause,
 										   SerializationPolicy serializationPolicy)
 										   throws SerializationException
 	{
 		if (_version == Version.GWT14)
 		{
 			return RPCCopy_GWT14.encodeResponseForFailure(serviceMethod, cause, serializationPolicy);
 		}
 		else
 		{
 			return RPCCopy_GWT15.encodeResponseForFailure(serviceMethod, cause, serializationPolicy);
 		}
 	}
 	
 	
 }
