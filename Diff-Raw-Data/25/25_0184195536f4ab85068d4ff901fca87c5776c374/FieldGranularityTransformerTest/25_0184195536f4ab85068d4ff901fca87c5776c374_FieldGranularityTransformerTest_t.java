 package org.multiverse.stms.alpha.instrumentation.fieldgranularity;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.multiverse.annotations.FieldGranularity;
 import org.multiverse.annotations.TransactionalObject;
 import org.multiverse.stms.alpha.AlphaStm;
 import org.multiverse.stms.alpha.AlphaTransactionalObject;
 import org.multiverse.transactional.DefaultTransactionalReference;
 import org.multiverse.transactional.primitives.*;
 
 import static java.util.Arrays.asList;
 import static org.junit.Assert.*;
 import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
 import static org.multiverse.stms.alpha.instrumentation.AlphaReflectionUtils.assertHasField;
 
 public class FieldGranularityTransformerTest {
     private AlphaStm stm;
 
     @Before
     public void setUp() {
         stm = (AlphaStm) getGlobalStmInstance();
        }
 
 
     //test with different forms of references.
 
     //test with array
 
    //test that annotation on non transactional object doesn't cause problems.
 
     //test that annotation on final field is ignored.
 
     //test that annotation on volatile field is ignored
 
     //test that annotationo on excluded field is ignored
 
 
     // ================ access modifiers =================
 
     @Test
     public void privateField() {
         Class clazz = PrivateField.class;
         PrivateField field = new PrivateField(10);
 
         assertHasField(field.getClass(), "value", TransactionalInteger.class);
 
         assertFalse(field instanceof AlphaTransactionalObject);
         long version = stm.getVersion();
 
         field.setValue(field.getValue() + 1);
 
         assertEquals(version + 1, stm.getVersion());
         assertEquals(11, field.getValue());
     }
 
     @TransactionalObject
     public static class PrivateField {
         @FieldGranularity
         private int value;
 
         public PrivateField(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     @Test
     public void protectedField() {
         ProtectedField field = new ProtectedField(10);
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalInteger.class);
 
         long version = stm.getVersion();
         field.setValue(field.getValue() + 1);
 
         assertEquals(version + 1, stm.getVersion());
         assertEquals(11, field.getValue());
     }
 
 
     @TransactionalObject
     public static class ProtectedField {
         @FieldGranularity
         protected int value;
 
         public ProtectedField(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     @Test
     public void publicField() {
         PublicField field = new PublicField(10);
         assertHasField(field.getClass(), "value", TransactionalInteger.class);
         assertFalse(field instanceof AlphaTransactionalObject);
         long version = stm.getVersion();
 
         field.setValue(field.getValue() + 1);
 
         assertEquals(version + 1, stm.getVersion());
         assertEquals(11, field.getValue());
     }
 
     @TransactionalObject
     public static class PublicField {
         @FieldGranularity
         protected int value;
 
         public PublicField(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     @Test
     public void packageFriendlyField() {
         PackageFriendlyField field = new PackageFriendlyField(10);
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalInteger.class);
 
         long version = stm.getVersion();
         field.setValue(field.getValue() + 1);
 
         assertEquals(version + 1, stm.getVersion());
         assertEquals(11, field.getValue());
     }
 
     @TransactionalObject
     public static class PackageFriendlyField {
         @FieldGranularity
         int value;
 
         public PackageFriendlyField(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     // ================ different types =================
 
     @Test
     public void intField_structure() {
         IntField field = new IntField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalInteger.class);
     }
 
     @Test
     public void intField_Usage() {
         IntField field = new IntField();
         assertEquals(0, field.getValue());
         field.setValue(100);
         assertEquals(100, field.getValue());
 
         field = new IntField(200);
         assertEquals(200, field.getValue());
     }
 
     @TransactionalObject
     private static class IntField {
 
         @FieldGranularity
         private int value;
 
         public IntField() {
         }
 
         private IntField(int value) {
             this.value = value;
         }
 
         public int getValue() {
             return value;
         }
 
         public void setValue(int value) {
             this.value = value;
         }
     }
 
     @Test
     public void floatField_structure() {
         FloatField field = new FloatField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalFloat.class);
     }
 
     @Test
     public void floatField_Usage() {
         FloatField field = new FloatField();
         assertTrue(new Float(0).equals(field.getValue()));
         field.setValue(100);
         assertTrue(new Float(100).equals(field.getValue()));
 
         field = new FloatField(200);
         assertTrue(new Float(200).equals(field.getValue()));
     }
 
     @TransactionalObject
     private static class FloatField {
 
         @FieldGranularity
         private float value;
 
         public FloatField() {
         }
 
         public FloatField(float value) {
             this.value = value;
         }
 
         public float getValue() {
             return value;
         }
 
         public void setValue(float value) {
             this.value = value;
         }
     }
 
     @Test
     public void booleanField_structure() {
         BooleanField field = new BooleanField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalBoolean.class);
 
     }
 
     @Test
     public void booleanField_usage() {
         BooleanField field = new BooleanField();
         assertFalse(field.getValue());
         field.setValue(true);
         assertTrue(field.getValue());
 
         field = new BooleanField(true);
         assertTrue(field.getValue());
     }
 
     @TransactionalObject
     private static class BooleanField {
 
         @FieldGranularity
         private boolean value;
 
         public BooleanField() {
         }
 
         public BooleanField(boolean value) {
             this.value = value;
         }
 
         public boolean getValue() {
             return value;
         }
 
         public void setValue(boolean value) {
             this.value = value;
         }
     }
 
     @Test
     public void byteField_structure() {
         ByteField field = new ByteField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalByte.class);
     }
 
     @Test
     public void byteField_Usage() {
         ByteField field = new ByteField();
         assertEquals(0, field.getValue());
 
         field.setValue((byte) 10);
         assertEquals((byte) 10, field.getValue());
 
         field = new ByteField((byte) 50);
         assertEquals((byte) 50, field.getValue());
     }
 
     @TransactionalObject
     private static class ByteField {
 
         @FieldGranularity
         private byte value;
 
         public ByteField() {
         }
 
         public ByteField(byte value) {
             this.value = value;
         }
 
         public byte getValue() {
             return value;
         }
 
         public void setValue(byte value) {
             this.value = value;
         }
     }
 
     @Test
     public void charField_structure() {
         CharField field = new CharField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalCharacter.class);
     }
 
     @Test
     public void charField_Usage() {
         CharField field = new CharField();
         assertEquals(0, field.getValue());
         field.setValue('a');
         assertEquals('a', field.getValue());
 
         field = new CharField('b');
         assertEquals('b', field.getValue());
     }
 
     @TransactionalObject
     private static class CharField {
 
         @FieldGranularity
         private char value;
 
         public CharField() {
         }
 
         public CharField(char value) {
             this.value = value;
         }
 
         public char getValue() {
             return value;
         }
 
         public void setValue(char value) {
             this.value = value;
         }
     }
 
     @Test
     public void longField_structure() {
         LongField field = new LongField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalLong.class);
     }
 
     @Test
     public void longField_Usage() {
         LongField field = new LongField();
         assertEquals(0, field.getValue());
         field.setValue(100);
         assertEquals(100, field.getValue());
 
         field = new LongField(200);
         assertEquals(200, field.getValue());
     }
 
     @TransactionalObject
     private static class LongField {
 
         @FieldGranularity
         private long value;
 
         public LongField() {
         }
 
         public LongField(long value) {
             this.value = value;
         }
 
         public long getValue() {
             return value;
         }
 
         public void setValue(long value) {
             this.value = value;
         }
     }
 
     @Test
     public void doubleField_structure() {
         DoubleField field = new DoubleField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalDouble.class);
     }
 
     @Test
     public void doubleField_Usage() {
         DoubleField field = new DoubleField();
         assertTrue(new Double(0).equals(field.getValue()));
         field.setValue(100);
         assertTrue(new Double(100).equals(field.getValue()));
 
         field = new DoubleField(200);
         assertTrue(new Double(200).equals(field.getValue()));
     }
 
 
     @TransactionalObject
     private static class DoubleField {
 
         @FieldGranularity
         private double value;
 
         public DoubleField() {
         }
 
         public DoubleField(int value) {
             this.value = value;
         }
 
         public double getValue() {
             return value;
         }
 
         public void setValue(double value) {
             this.value = value;
         }
     }
 
     @Test
     public void shortField_structure() {
         ShortField field = new ShortField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", TransactionalShort.class);
     }
 
     @Test
     public void shortField_Usage() {
         ShortField field = new ShortField();
         assertEquals(0, field.getValue());
         field.setValue((short) 100);
         assertEquals((short) 100, field.getValue());
 
         field = new ShortField((short) 30);
         assertEquals((short) 30, field.getValue());
     }
 
 
     @TransactionalObject
     private static class ShortField {
 
         @FieldGranularity
         private short value;
 
         public ShortField() {
         }
 
         public ShortField(short value) {
             this.value = value;
         }
 
         public short getValue() {
             return value;
         }
 
         public void setValue(short value) {
             this.value = value;
         }
     }
 
     @Test
     public void objectField_structure() {
         ObjectField field = new ObjectField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", DefaultTransactionalReference.class);
     }
 
     @Test
     public void objectField_Usage() {
         ObjectField field = new ObjectField();
         assertNull(field.getValue());
         field.setValue("foo");
         assertEquals("foo", field.getValue());
 
         field = new ObjectField("bar");
         assertEquals("bar", field.getValue());
     }
 
     @TransactionalObject
     private static class ObjectField {
 
         @FieldGranularity
         private Object value;
 
         public ObjectField() {
         }
 
         public ObjectField(Object value) {
             this.value = value;
         }
 
         public Object getValue() {
             return value;
         }
 
         public void setValue(Object value) {
             this.value = value;
         }
     }
 
     @Test
     public void typedField_structure() {
         TypedField field = new TypedField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", DefaultTransactionalReference.class);
     }
 
     @Test
     public void typedField_Usage() {
         TypedField field = new TypedField();
         assertNull(field.getValue());
         field.setValue("foo");
         assertEquals("foo", field.getValue());
 
         field = new TypedField("bar");
         assertEquals("bar", field.getValue());
     }
 
     @TransactionalObject
     private static class TypedField {
 
         @FieldGranularity
         private String value;
 
         public TypedField() {
         }
 
         public TypedField(String value) {
             this.value = value;
         }
 
         public String getValue() {
             return value;
         }
 
         public void setValue(String value) {
             this.value = value;
         }
     }
 
     @Test
     public void arrayField_structure() {
         ArrayField field = new ArrayField();
         assertFalse(field instanceof AlphaTransactionalObject);
         assertHasField(field.getClass(), "value", DefaultTransactionalReference.class);
     }
 
     @Test
     public void arrayField_Usage() {
         ArrayField field = new ArrayField();
         assertNull(field.getValue());
         field.setValue(new String[]{"foo"});
         assertTrue(asList(field.getValue()).equals(asList("foo")));
 
         field = new ArrayField(new String[]{"bar"});
         assertTrue(asList(field.getValue()).equals(asList("bar")));
     }
 
     @TransactionalObject
     private static class ArrayField {
 
         @FieldGranularity
         private String[] value;
 
         public ArrayField() {
         }
 
         public ArrayField(String[] value) {
             this.value = value;
         }
 
         public String[] getValue() {
             return value;
         }
 
         public void setValue(String[] value) {
             this.value = value;
         }
     }
 }
