 package net.premereur.reflection.util;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 public class ReflectionUtilTest {
 
 	static interface MyInterface<A, B> {
 	}
 
 	static class MyClass implements MyInterface<Integer, String> {
 	}
 
 	@Test
 	public void shouldGetImplementedInterfaceGenericTypes() {
 		Type[] interfaceTypes = ReflectionUtil.getImplementedInterfaceGenericTypes(MyClass.class, MyInterface.class);
 		assertEquals(Integer.class, interfaceTypes[0]);
 		assertEquals(String.class, interfaceTypes[1]);
 	}
 
 	static class MyNonImplementingClass {
 		
 	}
 	
 	static class MyImplementingNonGenericClass implements Cloneable {
 		
 	}
 	
 	@Test
 	public void shouldReturnEmptyArrayWhenImplementedInterfaceHasNoGenericTypes() {
 		Type[] interfaceTypes = ReflectionUtil.getImplementedInterfaceGenericTypes(MyNonImplementingClass.class, MyInterface.class);
 		assertEquals(0, interfaceTypes.length);
 		interfaceTypes = ReflectionUtil.getImplementedInterfaceGenericTypes(MyImplementingNonGenericClass.class, MyInterface.class);
 		assertEquals(0, interfaceTypes.length);
 	}
 	
	static class MyDerivedClass extends MyClass {
		
	}
	
	@Test
	public void shouldGetImplementedInterfaceGenericTypesEvenIfDerived() {
		Type[] interfaceTypes = ReflectionUtil.getImplementedInterfaceGenericTypes(MyDerivedClass.class, MyInterface.class);
		assertEquals(Integer.class, interfaceTypes[0]);
		assertEquals(String.class, interfaceTypes[1]);
	}	
	
 	@Test
 	public void shouldReturnNewInstanceWithoutCheckedExceptions() {
 		ReflectionUtil.uncheckedNewInstance(ArrayList.class);
 	}
 	
 	@Test(expected=RuntimeException.class)
 	public void shouldThrowUncheckedExceptionInCaseOfInstantiationProblems() {
 		ReflectionUtil.uncheckedNewInstance(Boolean.class);
 	}
 	
 	static class MySingleton {
 		private MySingleton() { }
 	}
 
 	@Test(expected=RuntimeException.class)
 	public void shouldThrowUncheckedExceptionInCaseOfIllegalAccessProblems() {
 		ReflectionUtil.uncheckedNewInstance(MySingleton.class);
 	}
 
 
 }
