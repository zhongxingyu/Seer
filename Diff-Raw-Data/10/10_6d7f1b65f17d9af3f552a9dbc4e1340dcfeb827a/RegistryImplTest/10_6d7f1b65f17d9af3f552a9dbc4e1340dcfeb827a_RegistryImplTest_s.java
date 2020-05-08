 package jmonkey.export;
 
 import static org.junit.Assert.*;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.junit.Test;
 
 public class RegistryImplTest {
 
 	@Test
 	public void test_RegistryImpl1_Null() {
 		RegistryImpl reg = new RegistryImpl(null);
 	}
 
 	@Test
 	public void test_RegistryImpl1_valid() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		assertEquals(version,reg.getVersion());
 	}
 
 	@Test
 	public void test_RegistryImpl2_emptyFile() throws IOException {
 		int[] version = {1};
 		File file = new File("");
 		try {
 			RegistryImpl reg = new RegistryImpl(file,version);
 		} catch (java.io.IOException ioe) {
 			return;
 		}
 		fail("Expected IOException in test_RegistryImpl2_null");
 	}
 
 	@Test
 	public void test_RegistryImpl2_invalidVersion() throws IOException {
 		int[] version = {Integer.MIN_VALUE};
 		File file = new File("test");
 		try {
 			RegistryImpl reg = new RegistryImpl(file,version);
 		} catch (java.io.IOException ioe) {
 			fail("unexpected IOException in test_RegistryImpl2_null");
 		}
 	}
 	
 	@Test
 	public void test_toString() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		String toString = reg.toString();
 		assertTrue(toString.startsWith("Registry dump: "));
 	}
 
 	@Test
 	public void test_commit() throws IOException {
 		int[] version = {Integer.MIN_VALUE};
 		File file = new File("test");
 		try {
 			RegistryImpl reg = new RegistryImpl(file,version);
 			reg.commit();
 		} catch (java.io.IOException ioe) {
 			fail("unexpected IOException in test_RegistryImpl2_null");
 		}		
 	}
 	
 	@Test
 	public void test_revert() throws IOException {
 		int[] version = {Integer.MIN_VALUE};
 		File file = new File("test");
 		try {
 			RegistryImpl reg = new RegistryImpl(file,version);
 			reg.revert();
 		} catch (java.io.IOException ioe) {
 			fail("unexpected IOException in test_RegistryImpl2_null");
 		}		
 	}
 	
 	@Test
 	public void test_stringToVersion_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("stringToVersion",String.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new String("12345"));
 			assertArrayEquals(new int[]{12345}, (int[])returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_stringToVersion_null(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("stringToVersion",String.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, null);
 			assertArrayEquals(null, (int[])returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_versionToString_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("versionToString",int[].class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new int[]{12345});
 			assertEquals("12345", (String)returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_versionToString");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_versionToString");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_versionToString");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_versionToString");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_versionToString");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_versionToString");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_checkVersion_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("checkVersion",int[].class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{12345});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new int[]{12345});
 		} catch (InvocationTargetException e) {
 			fail("RegistryFormatException: incorrect registry version no");
 			e.printStackTrace();
 		}catch (SecurityException e) {
 			fail("unexpected SecurityException in test_checkVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_checkVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_checkVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_checkVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_checkVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_checkVersion_invalid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("checkVersion",int[].class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{12345});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new int[]{1234});
 		} catch (InvocationTargetException e) {
 			//RegistryFormatException: incorrect registry version no is expected
 			return;
 		}catch (SecurityException e) {
 			fail("unexpected SecurityException in test_checkVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_checkVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_checkVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_checkVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_checkVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_setBasicProperty_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 	        Class cls2[] = new Class[]{ String.class, String.class, String.class, int.class };
 			registryImplMethod = registryImpl.getDeclaredMethod("setBasicProperty",cls2);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{12345});
 	        Object obj[] = new Object[]{"group","key","value",1};
 			Object returnValue = registryImplMethod.invoke(registryImplObject,obj);
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_setBasicProperty");
 			e.printStackTrace();
 		}catch (SecurityException e) {
 			fail("unexpected SecurityException in test_setBasicProperty");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_setBasicProperty");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_setBasicProperty");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_setBasicProperty");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_setBasicProperty");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_setBasicProperty_invalidValue(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 	        Class cls2[] = new Class[]{ String.class, String.class, String.class, int.class };
 			registryImplMethod = registryImpl.getDeclaredMethod("setBasicProperty",cls2);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{12345});
 			String nul = null;
 	        Object obj[] = new Object[]{"group","key",nul,1};
 			Object returnValue = registryImplMethod.invoke(registryImplObject,obj);
 		}catch (InvocationTargetException e) {
 			assertEquals("Cannot set a property to null.",e.getTargetException().getMessage());
 			//expected InvocationTargetException in test_setBasicProperty
 		}catch (SecurityException e) {
 			fail("unexpected SecurityException in test_setBasicProperty");
 			e.printStackTrace();
 		}catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_setBasicProperty");
 			e.printStackTrace();
 		}catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_setBasicProperty");
 			e.printStackTrace();
 		}catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_setBasicProperty");
 			e.printStackTrace();
 		}catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_setBasicProperty");
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void test_getBasicProperty_missingKey(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 	        Class cls2[] = new Class[]{ String.class, String.class, int.class };
 			registryImplMethod = registryImpl.getDeclaredMethod("getBasicProperty",cls2);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{12345});
 	        Object obj[] = new Object[]{"group","key",1};
 			Object returnValue = registryImplMethod.invoke(registryImplObject,obj);
 		}catch (InvocationTargetException e) {
 			assertEquals("group: group = key, key = missing registry group",e.getCause().getMessage());
 		}catch (SecurityException e) {
 			fail("unexpected SecurityException in test_getBasicProperty");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_getBasicProperty");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_getBasicProperty");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_getBasicProperty");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_getBasicProperty");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_isArrayType() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			Boolean returnVal = reg.isArrayType("group","key");
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing reg values
 		}
 	}
 	
 	@Test
 	public void test_isArrayType_nullGroup() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			Boolean returnVal = reg.isArrayType(null,"key");
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing parameter value
 		}
 	}
 	
 	@Test
 	public void test_isArrayType_nullKey() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			Boolean returnVal = reg.isArrayType("group",null);
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing parameter value
 		}
 	}
 	
 	@Test
 	public void test_getType() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			int returnVal = reg.getType("group","key");
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing reg values
 		}
 	}
 	
 	@Test
 	public void test_getType_nullGroup() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			int returnVal = reg.getType(null,"key");
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing parameter value
 		}
 	}
 	
 	@Test
 	public void test_getType_nullKey() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			int returnVal = reg.getType("group",null);
 		}catch(NullPointerException e){
 			//expected nullpointerexception on missing parameter value
 		}
 	}
 	
 	@Test
 	public void test_typeToMarker_valid() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		String returnVal = reg.typeToMarker(Registry.TYPE_STRING_SINGLE);
 		assertEquals(RegistryImpl.ID_STR,returnVal);
 	}
 	
 	@Test
 	public void test_typeToMarker_invalid() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			String returnVal = reg.typeToMarker(Integer.MAX_VALUE);
 		}catch(RegistryException e){
 			assertEquals("unknown type (2147483647)",e.getMessage());
 		}		
 	}
 	
 	@Test
 	public void test_markerToType_valid() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		int returnVal = reg.markerToType(RegistryImpl.ID_STR);
 		assertEquals(Registry.TYPE_STRING_SINGLE,returnVal);
 	}
 	
 	@Test
 	public void test_markerToType_invalid() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
 			int returnVal = reg.markerToType("test");
 		}catch(RegistryException e){
 			assertEquals("unrecognized type string (test)",e.getMessage());
 		}		
 	}
 
 	@Test
 	public void test_markerToType_null() {
 		int[] version = {1};
 		RegistryImpl reg = new RegistryImpl(version);
 		try{
			int returnVal = reg.markerToType(null);
 		}catch(RegistryException e){
 			assertEquals("unrecognized type string ()",e.getMessage());
 		}		
 	}
 	
 	@Test
 	public void test_encode_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("encode",Object.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new String("12345"));
 			assertEquals("-84|-19|0|5|116|0|5|49|50|51|52|53|", returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_encode_null(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("encode",Object.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new Object[]{null});
 			assertEquals("-84|-19|0|5|112|", returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_decode_valid(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("decode",String.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new String("-84|-19|0|5|116|0|5|49|50|51|52|53|"));
 			assertEquals("12345", returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 	
 	@Test
 	public void test_decode_null(){
 		try {
 			Class<RegistryImpl> registryImpl = RegistryImpl.class;
 	        Class cls[] = new Class[] { int[].class };
 	        Constructor<RegistryImpl> constructor = registryImpl.getDeclaredConstructor(cls);
 			Method registryImplMethod;
 			registryImplMethod = registryImpl.getDeclaredMethod("decode",String.class);			
 			registryImplMethod.setAccessible(true);
 			Object registryImplObject = constructor.newInstance(new int[]{1});
 			Object returnValue = registryImplMethod.invoke(registryImplObject, new Object[]{"-84|-19|0|5|112|"});
 			assertEquals(null, returnValue);
 		} catch (SecurityException e) {
 			fail("unexpected SecurityException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (NoSuchMethodException e) {
 			fail("unexpected NoSuchMethodException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			fail("unexpected InstantiationException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			fail("unexpected IllegalAccessException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			fail("unexpected IllegalArgumentException in test_stringToVersion");
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			fail("unexpected InvocationTargetException in test_stringToVersion");
 			e.printStackTrace();
 		}
 	}
 }
