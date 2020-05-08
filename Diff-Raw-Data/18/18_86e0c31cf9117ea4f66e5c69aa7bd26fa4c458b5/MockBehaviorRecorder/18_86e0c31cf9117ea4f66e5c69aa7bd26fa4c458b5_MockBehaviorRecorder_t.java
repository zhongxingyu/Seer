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
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.easymock.internal.IProxyFactory;
 
 import com.advancedpwr.record.BeanRecorder;
 import com.advancedpwr.record.InstanceTree;
 import com.advancedpwr.record.ObjectRecorder;
 import com.advancedpwr.record.methods.MethodBuilderFactory;
 
 /**
  * An {@link ObjectRecorder} that records the <i>behavior</i> of an object tree as a Java class file.
  * The resulting factory class uses <a href="http://easymock.org/">EasyMock</a> mock objects to mock the expected behavior of methods
  * called on objects throughout the object tree.
  * <p>
  * Recording example as a JUnit test:
  * </p>
  *<p><blockquote><pre>
 	public void testRecordJavadocExample()
 	{
 		MockBehaviorRecorder recorder = new MockBehaviorRecorder();
 		recorder.setDestination( "generated" );
 		
 		Person person = new Person();
 		
 		Person dad = new Person();
 		dad.setName( "dad" );
 		person.setDad( dad );
 		
 		person = recorder.record( person );
 		
 		person.setName( "Joe" );
 		assertEquals( "Joe", person.getName() );
 		assertEquals( "dad", person.getDad().getName() );
 		assertEquals( "dad", person.getDad().getName() );
 		
 		Person mom = new Person();
 		mom.setName( "mom" );
 		person.setMom( mom );
 		
 		recorder.endRecording();
 		
 	}
  * </pre></blockquote><p>
  * 
  * Note that the "person" instance is swapped with an instrumented instance of the "person" object on the line
  * 
  * <p><blockquote><pre>
  * person = recorder.record( person );
  * </pre></blockquote><p>
  * 
  * The above example will record the <i>behavior</i> of the "person" instance as a tree of mock objects in the Java class:
  * 
  * <p><blockquote><pre>
 	public class PersonFactory extends MockFactory
 	{
 	
 		protected Person person;
 	
 		public Person buildPerson()
 		{
 			if ( person != null ) 
 			{
 				return person;
 			}
 			person = createStrictMock( Person.class );
 			person.setName( "Joe" );
 			expect( person.getName() ).andReturn( "Joe" );
 			expect( person.getDad() ).andReturn( buildPerson_3_1() );
 			expect( person.getDad() ).andReturn( buildPerson_3_1() );
 			person.setMom( buildPerson_5_1() );
 			replay( person );
 			return person;
 		}
 	
 		protected Person person_3_1;
 	
 		protected Person buildPerson_3_1()
 		{
 			if ( person_3_1 != null ) 
 			{
 				return person_3_1;
 			}
 			person_3_1 = createStrictMock( Person.class );
 			expect( person_3_1.getName() ).andReturn( "dad" );
 			expect( person_3_1.getName() ).andReturn( "dad" );
 			replay( person_3_1 );
 			return person_3_1;
 		}
 	
 		protected Person person_5_1;
 	
 		protected Person buildPerson_5_1()
 		{
 			if ( person_5_1 != null ) 
 			{
 				return person_5_1;
 			}
 			person_5_1 = createStrictMock( Person.class );
 			replay( person_5_1 );
 			return person_5_1;
 		}
 	}
  * </pre></blockquote><p>
  * 
  * To reconstruct the instance of "person" in a unit test and assert that all method calls are identical to the recorded behavior:
  * 
  * <p><blockquote><pre>
   	public void testPlaybackJavadocExample()
 	{
 		Person person = new PersonFactory().buildPerson();
 		
 		person.setName( "Joe" );
 		assertEquals( "Joe", person.getName() );
 		assertEquals( "dad", person.getDad().getName() );
 		assertEquals( "dad", person.getDad().getName() );
 		
 		Person mom = new Person();
 		mom.setName( "mom" );
 		person.setMom( mom );
 	}
  * </pre></blockquote><p>
  * 
  * Note that the above unit test will <b>fail</b> because the mock objects are strictly performing checks on all objects they
  * are interacting with in the object tree.  In the playback test case, the instance of "mom" is not the same instance as the
  * recorded test case, so the mock objects will complain.  In order to make this test useful, it is necessary to edit the recorded
  * PersonFactory class and change the line
  * 
  * <p><blockquote><pre>
  * person.setMom( buildPerson_5_1() );
  * </pre></blockquote><p>
  * 
  * to
  * 
  * <p><blockquote><pre>
  * person.setMom( anyObject( Person.class ) );
  * </pre></blockquote><p>
  * 
  * This may be a bug.  If you disagree with this behavior, please open an issue on the <a href="http://jtor.sourceforge.net/">Java Test Object Recorder</a> site. 
  * 
  * The <code>MockBehaviorRecorder</code> is "instance aware" and supports {@link Collection} and {@link Map} objects.  This class
  * requires the EasyMock 3.0 jar and the cglib 2.2 jar.
  *  
  * @author Matthew Avery, mavery@advancedpwr.com on Jun 22, 2010
  *
  */
 
 public class MockBehaviorRecorder extends BeanRecorder
 {
 	protected Set<Class> fieldPreferredInterfaces;
 	
 	public MockBehaviorRecorder()
 	{
 		addPreferredInterface( java.sql.Connection.class );
 		addPreferredInterface( java.sql.PreparedStatement.class );
 		addPreferredInterface( java.sql.Statement.class );
 		addPreferredInterface( java.sql.ResultSet.class );
 		
 	}
 	protected Set<Class> getPreferredInterfaces()
 	{
 		if ( fieldPreferredInterfaces == null )
 		{
 			fieldPreferredInterfaces = new LinkedHashSet<Class>();
 		}
 		return fieldPreferredInterfaces;
 	}
 	
 	public void addPreferredInterface( Class inClass )
 	{
 		if( inClass.isInterface() )
 		{
 			getPreferredInterfaces().add( inClass );
 		}
 	}
 	
 	protected Set<Class> classes()
 	{
 		Set<Class> sourceClasses = getInstanceTree().getFactory().classes();
 		Set<Class> classes = new LinkedHashSet();
 		for ( Iterator iterator = sourceClasses.iterator(); iterator.hasNext(); )
 		{
 			Class sourceClass = (Class) iterator.next();
 			if ( hasPreferedInterface( sourceClass ) )
 			{
 				classes.add( preferredInterface( sourceClass ) );
 			}
 			else
 			{
 				classes.add(  sourceClass );
 			}
 		}
 		classes.add( MockFactory.class );
 		return classes;
 	}
 
 	public <T> T record( T inObject )
 	{
 		return instrument( inObject );
 	}
 	
 	protected void debug( Object inObject )
 	{
 		System.out.println( inObject );
 	}
 
 	protected <T> T instrument( final T inObject )
 	{
 		if ( !canInstrument( inObject ) )
 		{
 			debug( "Not able to instrument object " + inObject );
 			return inObject;
 		}
 		debug( "Instrumenting " + inObject );
 		initializeInstanceTree( inObject );
 		
 		Class objectClass = inObject.getClass();
 		if ( objectClass.isArray() )
 		{
 			return (T)instrumentedArray( (Object[])inObject );
 		}
 		final BehaviorInstanceTree tree = newInstanceTree( inObject );
 
 		IProxyFactory<T> factory = createProxyFactory( objectClass );
 		return factory.createProxy( (Class<T>) objectClass, new MockRecordingInvocationHandler( this, tree ) );
 
 	}
 	
 	protected <T> IProxyFactory<T> createProxyFactory( Class<T> inClass )
 	{
 		if ( hasPreferedInterface( inClass ) )
 		{
 			Class preferedInterace = preferredInterface( inClass );
 			return new InterfaceProxyFactory<T>( preferedInterace );
 		}
 		else
 		{
 			return new ClassProxyFactory<T>();
 		}
 	}
 	
 	protected boolean hasPreferedInterface( Class inClass )
 	{
 		return preferredInterface( inClass ) != null;
 	}
 
 	protected Class preferredInterface( Class inClass )
 	{
 		Set interfaces = new Interfaces().findInterfaces( inClass );
 		for ( Iterator iterator = interfaces.iterator(); iterator.hasNext(); )
 		{
 			Class currentInterface = (Class) iterator.next();
 			if ( getPreferredInterfaces().contains( currentInterface ) )
 			{
 				return currentInterface;
 			}
 		}
 		return null;
 	}
 	
 	protected boolean isEnhanced( Class objectClass )
 	{
 		return objectClass.getSimpleName().contains( "CGLIB" );
 	}
 
 	protected boolean canInstrument( Object result )
 	{
 		return canInstrumentClass( result ) || canInstrumentArray( result ) && !isEnhanced( result.getClass() );
 	}
 
 	protected boolean canInstrumentClass( Object result )
 	{
 		return result != null && isPublic( result ) && !isPrimitive( result ) && !isFinal( result ) && !isEnhanced( result.getClass() );
 	}
 
 	protected boolean isFinal( Object result )
 	{
 		return Modifier.isFinal( result.getClass().getModifiers() );
 	}
 
 	protected boolean isPrimitive( Object result )
 	{
 		return result.getClass().isPrimitive();
 	}
 
 	protected boolean isPublic( Object result )
 	{
 		return Modifier.isPublic( result.getClass().getModifiers() ) || hasPreferedInterface( result.getClass() );
 	}
 	
 	protected boolean canInstrumentArray( Object result )
 	{
		return result != null && result.getClass().isArray() && !result.getClass().getComponentType().isPrimitive();
 	}
 	
 	protected <T> T[] instrumentedArray( T[] inArray )
 	{
 		if ( inArray == null )
 		{
 			return (T[]) new Object[0];
 		}
 		T[] array = Arrays.copyOf( inArray, inArray.length );
 		for ( int i = 0; i < inArray.length; i++ )
 		{
 			if ( canInstrument( inArray[i] ) )
 			{
 				array[i] = instrument( inArray[i] );
 			}
 		}
 		return array;
 	}
 
 	protected BehaviorInstanceTree newInstanceTree( final Object inObject )
 	{
 		BehaviorInstanceTree tree = (BehaviorInstanceTree)getInstanceTree().getFactory().createInstanceTree( inObject, getInstanceTree() );
 		tree.setPreferredInterface( preferredInterface( inObject.getClass() ) );
 		return tree;
 	}
 
 	protected void initializeInstanceTree( Object inObject )
 	{
 		if ( fieldInstanceTree == null )
 		{
 			fieldInstanceTree = createInstanceTree( inObject );
 		}
 	}
 
 	public Object endRecording()
 	{
 		writePackage();
 		writeImports();
 		writeClassDeclaration();
 		openBrace();
 		writeObjectBuilderMethod();
 		closeBrace();
 		closeFile();
 		System.out.println( "Finished recording " + getDestination().getAbsolutePath() + File.separator + getDescriptor().toString() );
 		return getObject();
 	}
 
 	protected MethodBuilderFactory createMethodBuilderFactory()
 	{
 		MethodBuilderFactory factory = new MethodBuilderFactory();
 		factory.setDefaultFactory( new MockMethodBuilderFactory() );
 		return factory;
 	}
 
 	protected void writeClassDeclaration()
 	{
 		write( PUBLIC + CLASS + getDescriptor().getClassName() + " extends MockFactory" );
 	}
 
 	protected InstanceTree createInstanceTree( Object object )
 	{
 		return createBehaviorInstanceTree( object );
 	}
 
 	protected BehaviorInstanceTree createBehaviorInstanceTree( Object object )
 	{
 		BehaviorInstanceTree tree = new BehaviorInstanceTree( object );
 		tree.setPreferredInterface( preferredInterface( object.getClass() ) );
 		return tree;
 	}
 
 }
