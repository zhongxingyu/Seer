 /*
  * Copyright 2011 Matthew Avery, mavery@advancedpwr.com
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.advancedpwr.record.mock;
 
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 
 public class MockRecordingInvocationHandler implements InvocationHandler
 {
 	
 	protected BehaviorInstanceTree fieldTree;
 	protected MockBehaviorRecorder fieldRecorder;
 	
 	public MockRecordingInvocationHandler( MockBehaviorRecorder inRecorder, BehaviorInstanceTree inTree )
 	{
 		fieldRecorder = inRecorder;
 		fieldTree = inTree;
 	}
 
 	protected void debug( Object inObject )
 	{
 		System.out.println( inObject );
 	}
 	
 	public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable
 	{
 		debug( "Invoking method " + getSource().getClass().getSimpleName() + "." + method.getName() );
 		getTree().addDeclaredExceptions( method.getExceptionTypes() );
 		Object[] instrumentedArgs = instrumentArguments( method, args );
 		Object result;
 		try
 		{
 			method.setAccessible( true );
 			result = method.invoke( getSource(), instrumentedArgs );
 		}
 		catch ( InvocationTargetException ite )
 		{
 			getTree().setCurrentMethod( method );
 			getTree().addAccessPaths( instrumentedArgs, ite.getTargetException() );
 			throw ite.getTargetException();
 		}
 		if ( isObjectMethod( method ) )
 		{
 			return result;
 		}
 		getTree().setCurrentMethod( method );
 		getTree().addAccessPaths( instrumentedArgs, result );
 		return instrument( result );
 	}
 
 	protected Object[] instrumentArguments( Method method, Object[] args )
 	{
 		Object[] instrumentedArgs;
 		if ( isObjectMethod( method ) )
 		{
 			instrumentedArgs = args;
 		}
 		else
 		{
 			instrumentedArgs = instrumentedArray( args );
 		}
 		return instrumentedArgs;
 	}
 
 	protected <T> T[] instrumentedArray( T[] inArray )
 	{
 		return getRecorder().instrumentedArray( inArray );
 	}
 
 	protected Object getSource()
 	{
 		return getTree().getObject();
 	}
 	
 	protected BehaviorInstanceTree getTree()
 	{
 		return fieldTree;
 	}
 	
 	protected boolean isObjectMethod( Method method )
 	{
 		return "hashCode".equals( method.getName() ) || "equals".equals( method.getName() )  || "toString".equals( method.getName() ) ;
 	}
 
 	protected MockBehaviorRecorder getRecorder()
 	{
 		return fieldRecorder;
 	}
 	
 	protected <T> T instrument( final T inObject )
 	{
 		return getRecorder().instrument( inObject );
 	}
 
 }
