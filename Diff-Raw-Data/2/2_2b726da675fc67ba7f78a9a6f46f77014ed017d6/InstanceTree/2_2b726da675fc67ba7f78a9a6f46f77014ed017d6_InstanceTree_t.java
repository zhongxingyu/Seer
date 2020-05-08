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
 package com.advancedpwr.record;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class InstanceTree
 {
 	protected Object fieldObject;
 	protected Method fieldCurrentMethod;
 	protected List<AccessPath> fieldAccessPaths;
 	protected int depth;
 	protected InstanceTree fieldParent;
 	protected InstanceTreeFactory fieldFactory;
 	
 	protected int fieldIndex;
 	
 	public InstanceTree getParent()
 	{
 		if ( fieldParent == null )
 		{
 			fieldParent = this;
 		}
 		return fieldParent;
 	}
 	
 	public int getDepth()
 	{
 		return depth;
 	}
 
 	public void setDepth( int depth )
 	{
 		this.depth = depth;
 	}
 
 	public boolean isTop()
 	{
 		return getParent().equals( this );
 	}
 	
 	public void setParent( InstanceTree parent )
 	{
 		fieldParent = parent;
 	}
 
 	protected Method getCurrentMethod()
 	{
 		return fieldCurrentMethod;
 	}
 
 	public void setCurrentMethod( Method currentMethod )
 	{
 		fieldCurrentMethod = currentMethod;
 	}
 
 	public InstanceTree( Object object )
 	{
 		this( object, null );
 		getFactory().getTrees().put( object, this );
 		setIndex( getFactory().count++ );
 		inspectObject();
 	}
 	
 	public InstanceTree( Object object, InstanceTree inTree )
 	{
 		setParent( inTree );
 		if ( inTree != null )
 		{
 			setFactory( inTree.getFactory() );
 		}
 		setObject( object );
 	}
 	
 	protected void inspectObject()
 	{
 		List<Method> methods = sortedMethods();
 		for ( Method method : methods )
 		{
 			setCurrentMethod( method );
 			addMethodAccessPath();
 		}
 		addCollectionAccessPaths();
 		addMapAccessPaths();
 		addArrayAccessPaths();
 		
 	}
 
 	protected List<Method> sortedMethods()
 	{
 		Method[] methods = objectClass().getMethods();
 		List list = Arrays.asList( methods );
 		Collections.sort( list, new MethodNameComparator() );
 		Collections.sort( list, new CollectionMethodComparator() );
 		Collections.sort( list, new MapMethodComparator() );
 		Collections.sort( list, new ArrayMethodComparator() );
 		return list;
 	}
 
 	protected void addMapAccessPaths()
 	{
 		if ( Map.class.isAssignableFrom( objectClass() ) )
 		{
 			Map map = (Map) getObject();
 			for ( Iterator iterator = map.entrySet().iterator(); iterator.hasNext(); )
 			{
 				Map.Entry entry = (Map.Entry) iterator.next();
 				
 				MapPutPath path = new MapPutPath();
 				path.setKeyTree( createInstanceTree( entry.getKey() ) );
 				path.setValueTree( createInstanceTree( entry.getValue() ) );
 				path.setInstanceName( currentInstanceName() );
 				getAccessPaths().add( path );
 			}
 		}
 	}
 
 	protected String currentInstanceName()
 	{
 		if ( getParent().equals( this ) )
 		{
 			return objectClass().getSimpleName();
 		}
 		return getParent().getCurrentMethod().getName().replaceFirst( "set", "" ) + "_" + depth;
 	}
 
 	protected void addCollectionAccessPaths()
 	{
 		if ( Collection.class.isAssignableFrom( objectClass() ) )
 		{
 			Collection collection = (Collection) getObject();
 			for ( Iterator iterator = collection.iterator(); iterator.hasNext(); )
 			{
 				Object member = iterator.next();
 				MultiPath path = new MultiPath();
 				path.setTree( createInstanceTree( member ) );
 				path.setInstanceName( currentInstanceName() );
 				getAccessPaths().add( path );
 			}
 		}
 	}
 	
 	protected void addArrayAccessPaths()
 	{
		if ( objectClass().isArray() && !objectClass().getComponentType().isPrimitive() )
 		{
 			Object[] array = (Object[]) getObject();
 		
 			for ( int i = 0; i < array.length; i++ )
 			{
 				Object member = array[i];
 				MultiPath path = new MultiPath();
 				path.setTree( createInstanceTree( member ) );
 				path.setInstanceName( objectClass().getComponentType().getSimpleName() );
 				getAccessPaths().add( path );
 			}
 		}
 	}
 
 	public Class<? extends Object> objectClass()
 	{
 		return getObject().getClass();
 	}
 
 	protected void addMethodAccessPath()
 	{
 		if ( isSetter() && hasGetterMethod() )
 		{
 			Method getter = getterMethod();
 			Object result = invoke( getter );
 			if ( result != null )
 			{
 				addAccessPathForResult( result );
 			}
 		}
 	}
 	
 	protected void addAccessPathForResult( Object result )
 	{
 		AccessPath accessor = createAccessorMethodPath( result );
 		getAccessPaths().add( accessor );
 	}
 	
 	protected AccessPath createAccessorMethodPath( Object result )
 	{
 		AccessorMethodPath accessor = new AccessorMethodPath();
 		accessor.setSetter( getCurrentMethod() );
 		InstanceTree tree = createInstanceTree( result );
 		accessor.setTree( tree );
 		debug( "created accessor " + accessor + " for result " + result );
 		return accessor;
 	}
 	
 	protected InstanceTree createInstanceTree( Object result )
 	{
 		return getFactory().createInstanceTree( result, this );
 	}
 
 	public InstanceTree createTree( Object result )
 	{
 		return new InstanceTree( result, this );
 	}
 	
 	protected void debug( String inString )
 	{
 //		System.out.println( inString );
 	}
 	
 	public List<AccessPath> getAccessPaths()
 	{
 		if ( fieldAccessPaths == null )
 		{
 			fieldAccessPaths = new ArrayList<AccessPath>();
 		}
 		return fieldAccessPaths;
 	}
 
 	protected Object invoke( Method getter )
 	{
 		try
 		{
 			 return getter.invoke( getObject() );
 		}
 		catch ( Exception e )
 		{
 			throw new RecorderException( e );
 		}
 		
 	}
 
 	protected boolean isSetter()
 	{
 		Method method = getCurrentMethod();
 		return Modifier.isPublic( method.getModifiers() )  && method.getName().startsWith( "set" ) && method.getParameterTypes().length == 1;
 	}
 	
 	protected String getterName()
 	{
 		if( boolean.class.equals( getCurrentMethod().getParameterTypes()[0] ) )
 		{
 			return getCurrentMethod().getName().replaceFirst( "set", "is" );
 		}
 		return getCurrentMethod().getName().replaceFirst( "set", "get" );
 	}
 	
 	protected Method getterMethod()
 	{
 		Method[] methods = objectClass().getMethods();
 		for ( int i = 0; i < methods.length; i++ )
 		{
 			Method method = methods[i];
 			if ( isGetter( method ) )
 			{
 				return method;
 			}
 		}
 		return null;
 	}
 
 	protected boolean isGetter( Method method )
 	{
 		return method.getName().equals( getterName() ) && method.getParameterTypes().length == 0;
 	}
 	
 	protected boolean hasGetterMethod()
 	{
 		return getterMethod() != null;
 	}
 	
 	
 	public Object getObject()
 	{
 		return fieldObject;
 	}
 
 	public void setObject( Object object )
 	{
 		if ( object == null )
 		{
 			object = new Null();
 		}
 		fieldObject = object;
 	}
 
 	protected Set<Class> classes()
 	{
 		Set<Class> classes = new LinkedHashSet<Class>();
 		if ( !objectClass().isArray() )
 		{
 			addClass( classes, objectClass() );
 		}
 		for ( AccessPath path : getAccessPaths() )
 		{
 			addClass( classes, path.getParameterClass() );
 			addClass( classes, path.getResultClass() );
 			for ( Class aClass : path.getExceptions() )
 			{
 				addClass( classes, aClass );
 			}
 		}
 		return classes;
 	}
 	
 	protected void addClass( Set<Class> classes, Class inClass )
 	{
 		if ( ignoredClass( inClass ) )
 		{
 			return;
 		}
 		if ( inClass.isArray() )
 		{
 			inClass = inClass.getComponentType();
 		}
 		classes.add( inClass );
 	}
 
 	public boolean ignoredClass( Class param )
 	{
 		return param == null
 			|| short.class.isAssignableFrom( param ) 
 		    || int.class.isAssignableFrom( param )
 		    || long.class.isAssignableFrom( param )
 		    || float.class.isAssignableFrom( param )
 		    || double.class.isAssignableFrom( param )
 		    || boolean.class.isAssignableFrom( param )
 		    || byte.class.isAssignableFrom( param )
 		    || char.class.isAssignableFrom( param )
 		    || void.class.isAssignableFrom( param )
 		    || Null.class.isAssignableFrom( param )
 		    || param.getName().startsWith( "java.lang." );
 		
 	}
 
 	public int getIndex()
 	{
 		return fieldIndex;
 	}
 
 	public void setIndex( int index )
 	{
 		fieldIndex = index;
 	}
 
 	public InstanceTreeFactory getFactory()
 	{
 		if ( fieldFactory == null )
 		{
 			fieldFactory = createFactory();
 		}
 		return fieldFactory;
 	}
 
 	protected InstanceTreeFactory createFactory()
 	{
 		return new InstanceTreeFactory();
 	}
 
 	public void setFactory( InstanceTreeFactory factory )
 	{
 		fieldFactory = factory;
 	}
 
 	public Set<Class> getExceptions()
 	{
 		List cache = new ArrayList();
 		cache.add( this );
 		Set<Class> exceptions = getExceptions( cache );
 		return exceptions;
 	}
 
 	protected Set<Class> getExceptions( List cache )
 	{
 		Set<Class> exceptions = new LinkedHashSet<Class>();
 		for ( AccessPath path : getAccessPaths() )
 		{
 			if ( !cache.contains( path.getInstanceTree() ) )
 			{
 				cache.add( path.getInstanceTree() );
 				exceptions.addAll( path.getExceptions() );
 				exceptions.addAll( path.getInstanceTree().getExceptions( cache ) );
 				
 			}
 		}
 		return exceptions;
 	}
 	
 }
