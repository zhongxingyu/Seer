 package by.muna.types.test;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import by.muna.types.Constructor;
 import by.muna.types.ConstructorArgs;
 import by.muna.types.DataHole;
 import by.muna.types.Type;
 import by.muna.types.TypeHole;
 import by.muna.types.VectorHole;
 
public class Tests {
 
     public static final Type INT_TYPE = new Type("Int");
     public static final Constructor INT = new Constructor(
         "int", Tests.INT_TYPE, new ConstructorArgs(DataHole.HOLE)
     );
     
     public static final Type LONG_TYPE = new Type("Long");
     public static final Constructor LONG = new Constructor(
         "long", Tests.LONG_TYPE, new ConstructorArgs(DataHole.HOLE)
     );
     
     public static final Type STRING_TYPE = new Type("String");
     public static final Constructor STRING = new Constructor(
         "string", Tests.STRING_TYPE, new ConstructorArgs(DataHole.HOLE)
     );
     
     public static final Type VECTOR_TYPE = new Type("Vector", 1);
     public static final Constructor VECTOR = new Constructor(
         "vector", Tests.VECTOR_TYPE,
         new ConstructorArgs(new VectorHole(new TypeHole(0)))
     );
     
     public static final Type EITHER_TYPE = new Type("Either", 2);
     
     public static final Constructor EITHER_LEFT = new Constructor(
         "left", Tests.EITHER_TYPE, new ConstructorArgs(new TypeHole(0))
     );
     public static final Constructor EITHER_RIGHT = new Constructor(
         "right", Tests.EITHER_TYPE, new ConstructorArgs(new TypeHole(1))
     );
 
     @Test
     public void simple() {
         Type simpleType = new Type("Simple");
         
         Constructor simpleConstructor = new Constructor("simple", simpleType);
         
         Assert.assertEquals("Simple", simpleType.toString());
         Assert.assertEquals("simple = Simple", simpleConstructor.toString());
     }
     
     @Test
     public void dataHole() {
         Type intType = new Type("Int");
         
         Constructor intConstructor = new Constructor(
             "int", intType,
             new ConstructorArgs(DataHole.HOLE)
         );
         
         Assert.assertEquals("int ? = Int", intConstructor.toString());
     }
     
     @Test
     public void vectorHoleAndPolymorphicType() {
         Type vectorType = new Type("Vector", 1);
         
         Constructor vector = new Constructor(
             "vector", vectorType,
             new ConstructorArgs(new VectorHole(new TypeHole(0)))
         );
         
         Constructor intVector = vector.applyType(Tests.INT);
         Constructor typeIntVector = vector.applyType(Tests.INT_TYPE);
         
         Assert.assertEquals("vector # [ @0 ] = Vector @0", vector.toString());
         Assert.assertEquals("vector # [ int ] = Vector int", intVector.toString());
         Assert.assertEquals("vector # [ Int ] = Vector Int", typeIntVector.toString());
         
         Assert.assertEquals("Vector int", intVector.getType().toString());
         Assert.assertEquals("Vector Int", typeIntVector.getType().toString());
     }
     
     @Test
     public void either() {
         Type eitherType = new Type("Either", 2);
         
         Constructor left = new Constructor(
             "left", eitherType,
             new ConstructorArgs(new TypeHole(0))
         );
         Constructor right = new Constructor(
             "right", eitherType,
             new ConstructorArgs(new TypeHole(1))
         );
         
         Constructor leftIntTypeInt = left.applyType(Tests.INT).applyType(Tests.INT_TYPE);
         Constructor rightIntTypeInt = right.applyType(Tests.INT).applyType(Tests.INT_TYPE);
         
         Assert.assertEquals("left int = Either int Int", leftIntTypeInt.toString());
         Assert.assertEquals("right Int = Either int Int", rightIntTypeInt.toString());
     }
     
     @Test
     public void vectorInArgs() {
         Type userType = new Type("User");
         
         Constructor user = new Constructor(
             "user", userType,
             new ConstructorArgs(
                 "name", Tests.STRING,
                 "numbers", Tests.VECTOR_TYPE.applyType(Tests.INT),
                 "bazinga", Tests.VECTOR.applyType(
                     Tests.VECTOR_TYPE.applyType(
                         Tests.EITHER_LEFT.applyType(Tests.STRING).applyType(Tests.LONG_TYPE)
                     )
                 )
             )
         );
         
         Assert.assertEquals(
             "user name:string numbers:Vector int bazinga:vector Vector left string Long = User",
             user.toString()
         );
     }
 
 }
