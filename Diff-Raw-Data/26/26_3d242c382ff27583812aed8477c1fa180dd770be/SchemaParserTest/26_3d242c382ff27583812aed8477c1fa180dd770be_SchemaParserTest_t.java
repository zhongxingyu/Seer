 package togos.schemaschema.parser;
 
 import java.util.Map;
 
 import junit.framework.TestCase;
 import togos.lang.InterpretError;
 import togos.lang.ScriptError;
 import togos.schemaschema.ComplexType;
 import togos.schemaschema.EnumType;
 import togos.schemaschema.FieldSpec;
 import togos.schemaschema.IndexSpec;
 import togos.schemaschema.Predicate;
 import togos.schemaschema.Predicates;
 import togos.schemaschema.PropertyUtil;
 import togos.schemaschema.SchemaObject;
 import togos.schemaschema.Type;
 import togos.schemaschema.Types;
 
 public class SchemaParserTest extends TestCase
 {
 	SchemaParser sp;
 	
 	public void setUp() throws Exception {
 		sp = new SchemaParser();
 		sp.defineFieldModifier("key", SchemaParser.FieldIndexModifierSpec.INSTANCE);
 		sp.defineFieldModifier("index", SchemaParser.FieldIndexModifierSpec.INSTANCE);
 		sp.defineType(Types.INTEGER);
 		sp.defineType(Types.STRING);
 		sp.defineClassPredicate( Predicates.EXTENDS );
 	}
 	
 	protected void assertFieldsNamedProperly( Map<String,FieldSpec> fieldMap ) {
 		for( Map.Entry<String,FieldSpec> e : fieldMap.entrySet() ) {
 			assertEquals( e.getKey(), e.getValue().name );
 		}
 	}
 	
 	protected void assertPropertyValue( Object expectedValue, SchemaObject obj, Predicate prop ) {
 		if( expectedValue == null && obj.getProperties().get(prop) == null ) {
 		} else {
 			assertNotNull( prop.name+" should not be null", obj.getProperties().get(prop) );
 			assertEquals( 1, obj.getProperties().get(prop).size() );
 			for( Object v : obj.getProperties().get(prop) ) {
 				assertEquals( expectedValue, v );
 			}
 		}
 	}
 	
 	protected ComplexType parseClass( String source, String className ) throws ScriptError {
 		sp.parse(source, "(test source)");
 		SchemaObject so = sp.types.get(className); 
 		assertTrue("Parsed object expected to be a complex type", so instanceof ComplexType);
 		// assertEquals( source, classes.get("some object").toString() );
 		return (ComplexType)so;
 	}
 	
 	public void testSimpleClass() throws ScriptError {
 		String source =
 			"class 'some object' {\n" +
 			"\tint field : integer\n" +
 			"\tstr field : string\n" +
 			"}";
 		
 		ComplexType ot = parseClass( source, "some object" );
 		assertEquals( source, ot.toString() );
 		assertEquals( 2, ot.fieldsByName.size() );
 		assertFieldsNamedProperly( ot.fieldsByName );
 		
 		{
 			FieldSpec intFieldSpec = ot.fieldsByName.get("int field");
 			assertNotNull( intFieldSpec );
 			assertEquals( "int field", intFieldSpec.name );
 			assertPropertyValue( null, intFieldSpec, Predicates.IS_NULLABLE );
 			assertPropertyValue( Types.INTEGER, intFieldSpec, Predicates.OBJECTS_ARE_MEMBERS_OF );
 		}
 		
 		{
 			FieldSpec strFieldSpec = ot.fieldsByName.get("str field");
 			assertNotNull( strFieldSpec );
 			assertEquals( "str field", strFieldSpec.name );
 			assertFalse( PropertyUtil.isTrue(strFieldSpec.getProperties(), Predicates.IS_NULLABLE) );
 			assertPropertyValue( Types.STRING, strFieldSpec, Predicates.OBJECTS_ARE_MEMBERS_OF );
 		}
 	}
 	
 	public void testExtendedClass() throws ScriptError {
 		String source =
 			"class 'some object' : extends(integer) {\n" +
 			"\tnumber of bits : integer\n" +
 			"}";
 			
 		ComplexType ot = parseClass( source, "some object" );
 		assertEquals( source, ot.toString() );
 	}
 	
 	public void testClassWithPrimaryKey() throws ScriptError {
 		String source =
 			"class 'some object' {\n" +
 			"\tint field : integer : key(primary)\n" +
 			"\tstr field : string : key(primary)\n" +
 			"}";
 		
 		ComplexType ot = parseClass( source, "some object" );
 		
 		assertEquals( 1, ot.indexesByName.size() );
 		assertTrue( ot.indexesByName.containsKey("primary") );
 		IndexSpec primaryIndex = ot.indexesByName.get("primary");
 		assertEquals( "primary", primaryIndex.name );
 		assertEquals( 2, primaryIndex.fields.size() );
 	}
 	
 	public void testParseEnum() throws ScriptError {
 		String source =
 			"enum colorful color {\n" +
 			"\tyellow\n" +
 			"\tgreen\n" +
 			"\tred\n" +
 			"}";
 		
 		ComplexType ot = parseClass( source, "colorful color" );
 		assertTrue( ot instanceof EnumType );
 		EnumType et = (EnumType)ot;
 		assertEquals( 3, et.validValues.size() );
 		for( SchemaObject v : et.validValues ) {
 			assertTrue( PropertyUtil.isMemberOf( v, et ) );
 			assertTrue( "red".equals(v.getName()) || "green".equals(v.getName()) || "yellow".equals(v.getName()) );
 		}
 	}
 	
 	public void testEvaluateEnumValue() throws ScriptError {
 		String source =
 			"enum X {\n"+
 			"  foo\n" +
 			"  bar\n" +
 			"}\n" +
 			"\n" +
 			"class property X : X\n" +
 			"\n" +
			"class Y : X @ foo\n";
 		
 		sp.parse(source, "(test script)");
 		
 		EnumType enumX = (EnumType)sp.types.get("X");
 		assertNotNull(enumX);
 		Predicate predX = sp.predicates.get("X");
 		assertNotNull(predX);
 		
		ComplexType yClass = parseClass( source, "Y" );
		assertNotNull( yClass );
		assertEquals( 1, PropertyUtil.getAll(yClass.getProperties(), predX).size() );
		for( Object v : PropertyUtil.getAll(yClass.getProperties(), predX) ) {
 			assertTrue( PropertyUtil.isMemberOf((SchemaObject)v, enumX) );
 		}
 	}
 	
 	public void testEvaluateInvalidEnumValue() throws ScriptError {
 		String source =
 			"enum X {\n"+
 			"  foo\n" +
 			"  bar\n" +
 			"}\n" +
 			"\n" +
 			"class property X : X\n";
 		
 		sp.parse(source, "(test script)");
 		
 		EnumType enumX = (EnumType)sp.types.get("X");
 		assertNotNull(enumX);
 		Predicate predX = sp.predicates.get("X");
 		assertNotNull(predX);
 				
 		try {
 			sp.parse("class Y : X @ barrrr\n", "(more test script)");
 			fail("Inalid enum value should have thrown InterpretError");
 		} catch( InterpretError e ) { }
 	}
 	
 	public void testFieldModifier() throws ScriptError {
 		String source =
 			"field modifier 'le mod' = integer\n" +
 			"class X {\n" +
 			"  field Y : le mod\n" +
 			"}";
 		
 		ComplexType classX = parseClass(source, "X");
 		assertEquals( 1, classX.fieldsByName.size() );
 		for( FieldSpec f : classX.fieldsByName.values() ) {
 			assertEquals( 1, f.getObjectTypes().size() );
 			for( Type t : f.getObjectTypes() ) {
 				assertEquals( "integer", t.getName() );
 			}
 		}
 	}
 }
