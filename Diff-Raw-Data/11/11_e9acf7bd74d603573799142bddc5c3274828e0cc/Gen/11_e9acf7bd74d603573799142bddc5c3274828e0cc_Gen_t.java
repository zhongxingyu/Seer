 package tbc.supercheck;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Random;
 
 /**
  * Gen is a provider of arbitrary primitive values, and of methods that ease 
  * the production of arbitrary objects. When defining an arbitrary() method for
  * a class that will be the random data of a property, its first and only 
  * argument should be a Gen object:
  * 
  * <pre>public static Alignment arbitrary(Gen gen) {
  *     return gen.oneOf(Alignment.LEFT, Alignment.CENTRE, Alignment.RIGHT);
  * }</pre>
  * 
  * @author Karl Jonathan Ward <karl.j.ward@googlemail.com>
  */
 public class Gen {
 
     private long randomSeed = System.currentTimeMillis();
     
     private Random random   = new Random(randomSeed);
 
     /** 
      * Answers a random object, selected from the objects in the possibles 
      * array. Each entry in the possibles array has an equal chance of being 
      * selected.
      * <p>
      * An example:
      * <pre>oneOf(new Shape[] { new Circle(), new Square(), new Triangle() })
      * </pre>
      * 
      * @return a random object from possibles
      */
     public <A> A oneOf(A... possibles) {
         return possibles[random.nextInt(possibles.length)];
     }
     
     /** 
      * Choose from objects with given probabilities. This is equivalent to a 
      * call to {@link #select(float[])} in the form:
      * <pre>possibles[select(probabilities)]</pre>
      */
     public <A> A oneOf(float[] probabilities, A... possibles) {
         return possibles[select(probabilities)];
     }
 
     /** 
      * Answers a random value from `from` to `to`, inclusive both  sides. Each 
      * integer in the range has an equal chance of being chosen.
      * <p>
      * An example:
      * <pre>switch (choose(1, 4)) {
      *     case 1:
      *         return new Square.createWithin(Circle.arbitrary());
      *     case 2:
      *         return new Circle(gen.arbInt(), gen.arbInt(), gen.arbInt());
      *     case 3:
      *         return new Triangle.createWithin(Circle.arbitrary());
      *     case 4: default:
      *         return new Line(gen.arbInt(), gen.arbInt());
      * }</pre>
      * 
      * @return an int in the range [from, to]
      */
     public int choose(int from, int to) {
         if (from == to) return from;
         
         return random.nextInt(to - from + 1) + from;
     }
     
     /** 
      * Answers an index into the probability array. The index is selected 
      * randomly, where the random selection takes the probability schematic 
      * provided by the probability array. Probabilities will be normalised 
      * if they do not sum to 1.0f.
      * <p>
      * An example:
      * <pre>switch (select(0.05, 0.95)) {
      *     case 0: // 0.05
      *         return MyObject.SPECIAL_VALUE_NOT_OFTEN_ENCOUNTERED;
      *     case 1: default: // 0.95
      *         return new MyObject(...bunch of random data...);
      * }</pre>
      * 
      * @return an index of the probabilities array
      */
     public int select(float... probabilities) {
         probabilities = probabilities.clone(); // avoid modifying input
         
         float sum = 0.0f;
         for (float f : probabilities) {
             sum += f;
         }
         
         if (sum != 1.0f) {
             for (int idx = 0; idx < probabilities.length; idx++) {
                 probabilities[idx] = probabilities[idx] / sum;
             }
         }
         
         float ran = random.nextFloat(), totalThusFar = 0.0f;
         for (int idx=0; idx<probabilities.length; idx++) {
             totalThusFar += probabilities[idx];
             if (ran <= totalThusFar) {
                 return idx;
             }
         }
         return probabilities.length - 1; // because our normalisation
                                          // may suffer rounding errors
     }
     
     /**
      * Answers a float within the integer range [from, to]. In this example a 
      * loose quad tree is created with a k value (real number) somewhere between 
      * 1 and 3:
      * 
      * <pre>LooseQuadTree.createWithK(within(1, 3))</pre>
      * 
      * The integer boundary values have a 20% probability of being returned.
      * 
      * @return a float within the range [from, to]
      */
     public float within(int from, int to) {
         if (from == to) return from;
         
         switch (select(0.1f, 0.1f, 0.8f)) {
         case 0:
             return from;
         case 1:
             return to;
         case 2: default:
             return random.nextFloat() + choose(from, to - 1);
         }
     }
     
     /**
      * Creates an arbitrary array, whose contents are arbitrary objects. There
      * is a 10% chance that a zero-length array will be returned. Otherwise,
     * the array with have a length between 1 and 10, inclusive. The arbitraryT
      * type must implement an arbitrary() method.
      * <p>
      * An example, where Point2D alsoimplements arbitrary():
      * <pre>public static PointSet arbitrary(Gen gen) {
      *     return new PointSet.fromArray(arbArray(Point2D.class))
      * }</pre>
      * 
      * Be careful of circular dependency. If an arbitrary() definition in class
      * A calls this method, passing class A, then infinite recursion will occur.
      * 
      * @return an arbitrary object array
      */
     @SuppressWarnings("unchecked")
     public <A> A[] arbArray(Class<A> arbitraryT) {
         switch (select(0.1f, 0.9f)) {
         case 0:
             return (A[]) Array.newInstance(arbitraryT, 0);
         case 1: default:
            A[] ary = (A[]) Array.newInstance(arbitraryT, choose(1, 10));
             for (int idx=0; idx<ary.length; idx++) {
                 ary[idx] = (A) createArbitraryFor(arbitraryT);
             }
             return ary;
         }
     }
     
     /**
      * Answers an arbitrary byte.  Like with {@link #arbInt()}, the values 
      * MAX_VALUE, MIN_VALUE, 0, -1 and 1 have a hightened chance of selection.
      * 
      * @return an arbitrary byte
      */
     public byte arbByte() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.75f)) {
         case 0:
             return 0;
         case 1:
             return Byte.MAX_VALUE;
         case 2:
             return Byte.MIN_VALUE;
         case 3:
             return -1;
         case 4:
             return 1;
         case 5: default:
             return (byte) random.nextInt();
         }
     }
     
     /**
      * Answers an arbitrary short. Like with {@link #arbInt()}, the values 
      * MAX_VALUE, MIN_VALUE, 0, -1 and 1 have a hightened chance of selection.
      * 
      * @return an arbitrary short
      */
     public short arbShort() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.75f)) {
         case 0:
             return 0;
         case 1:
             return Short.MAX_VALUE;
         case 2:
             return Short.MIN_VALUE;
         case 3:
             return -1;
         case 4:
             return 1;
         case 5: default:
             return (short) random.nextInt();
         }
     }
     
     /** 
      * Answers an arbitrary integer, with an inflated probability of returing 
      * the values MAX_VALUE, MIN_VALUE, 0, -1 and 1. That is, the probability of 
      * receiving these values is higher than with a call to Random.nextInt(), but 
      * is still low in comparison to the probability of receiving an integer that 
      * is not of these values. This is intended to improve the testing of corner 
      * cases.
      * 
      * @return an arbitrary int
      */
     public int arbInt() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.75f)) {
         case 0:
             return 0;
         case 1:
             return Integer.MAX_VALUE;
         case 2:
             return Integer.MIN_VALUE;
         case 3:
             return -1;
         case 4:
             return 1;
         case 5: default:
             return random.nextInt();
         }
     }
     
     /** 
      * Answers an arbitrary long. Like with {@link #arbInt()}, the values 
      * MAX_VALUE, MIN_VALUE, 0, -1 and 1 have a hightened chance of selection.
      * 
      * @return an arbitrary long
      */
     public long arbLong() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.75f)) {
         case 0:
             return 0l;
         case 1:
             return Long.MAX_VALUE;
         case 2:
             return Long.MIN_VALUE;
         case 3:
             return -1l;
         case 4:
             return 1l;
         case 5: default:
             return random.nextLong();
         }
     }
     
     /** 
      * Answers an arbitrary boolean.
      * 
      * @return an arbitrary boolean
      */
     public boolean arbBoolean() {
         return random.nextBoolean();
     }
     
     /** 
      * Answers an arbitrary float. Like with {@link #arbInt()}, the  values NaN, 
      * POSITIVE_INFINITY, NEGATIVE_INFINITY, MAX_VALUE, MIN_VALUE and 0.0f (though 
      * not -1 and 1) have a hightened chance of selection.
      * 
      * @return an arbitrary float
      */
     public float arbNastyFloat() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.7f)) {
         case 0:
             return Float.NaN;
         case 1:
             return Float.POSITIVE_INFINITY;
         case 2:
             return Float.NEGATIVE_INFINITY;
         case 3:
             return Float.MAX_VALUE;
         case 4:
             return Float.MIN_VALUE;
         case 5:
             return 0.0f;
         case 6: default:
             return random.nextFloat() + random.nextInt();
         }
     }
     
     /** 
      * Answers an arbitrary float, but never NaN, POSITIVE_INFINITY or
      * NEGATIVE_INFINITY.
      * 
      * @return an arbitrary float
      */
     public float arbFloat() {
         switch (select(0.05f, 0.05f, 0.05f, 0.85f)) {
         case 0:
             return Float.MAX_VALUE;
         case 1:
             return Float.MIN_VALUE;
         case 2:
             return 0.0f;
         case 3: default:
             return random.nextFloat() + random.nextInt();
         }
     }
     
     /** 
      * Answers an arbitrary double. Like with {@link #arbInt()}, the  values 
      * NaN, POSITIVE_INFINITY, NEGATIVE_INFINITY, MAX_VALUE, MIN_VALUE and 0.0d 
      * (though not -1 and 1) have a hightened chance of selection.
      * 
      * @return an arbitrary double
      */
     public double arbNastyDouble() {
         switch (select(0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.05f, 0.7f)) {
         case 0:
             return Double.NaN;
         case 1:
             return Double.POSITIVE_INFINITY;
         case 2:
             return Double.NEGATIVE_INFINITY;
         case 3:
             return Double.MAX_VALUE;
         case 4:
             return Double.MIN_VALUE;
         case 5:
             return 0.0d;
         case 6: default:
             return random.nextDouble() + random.nextInt();
         }
     }
     
     /** 
      * Answers an arbitrary double, but never NaN, POSITIVE_INFINITY or 
      * NEGATIVE_INFINITY.
      * 
      * @return an arbitrary double
      */
     public double arbDouble() {
         switch (select(0.05f, 0.05f, 0.05f, 0.85f)) {
         case 0:
             return Double.MAX_VALUE;
         case 1:
             return Double.MIN_VALUE;
         case 2:
             return 0.0d;
         case 3: default:
             return random.nextDouble() + random.nextInt();
         }
     }
     
     /**
      * Answers an arbitrary character on the latin-1 page.
      * 
      * @return an arbitrary char
      */
     public char arbChar() {
         return (char) random.nextInt(256);
     }
     
     /**
      * Answers an arbitrary string, with an inflated probability of returning
      * an empty string. Generated strings have a maximum length of 1024 characters.
      * Characters within the string will all be on the latin-1 page.
      * 
      * @return an arbitrary string
      */
     public String arbString() {
         switch (select(0.2f, 0.8f)) {
         case 0:
             return "";
         case 1: default:
             char[] cs = new char[choose(1, 1024)];
             for (int idx=0; idx<cs.length; idx++) {
                 cs[idx] = (char) random.nextInt(256); // inline of arbChar()
             }
             return new String(cs);
         }
     }
     
     <T> Object createArbitraryFor(Class<T> arbitraryT) throws TestException {
         if (arbitraryT.isEnum()) {
             T[] enumCs = arbitraryT.getEnumConstants();
            return enumCs[choose(0, enumCs.length - 1)];
         } else if (arbitraryT.isArray()) {
             return arbArray(arbitraryT.getComponentType());
         } else if (arbitraryT.isPrimitive()) {
             return createPrimitiveFor(arbitraryT);
        } else if (arbitraryT == String.class) {
            return arbString();
         }
         
         /* otherwise we want an arbitrary arbitraryT */
         try {
             Method maker = arbitraryT.getDeclaredMethod("arbitrary", 
                     new Class[] { Gen.class });
             return maker.invoke(null, new Object[] { this });
         } catch (NoSuchMethodException e) {
             throw new TestException("Missing arbitrary() definition on: "
                     + arbitraryT.getName());
         } catch (InvocationTargetException e) {
             throw new TestException(arbitraryT.getName() + ".arbitrary() threw " +
                     e.getCause());
         } catch (IllegalAccessException e) {
             throw new TestException(e.toString());
         }
     }
     
     Object createPrimitiveFor(Class<?> primitiveT) {
         if (primitiveT == Boolean.TYPE) {
             return arbBoolean();
         } else if (primitiveT == Character.TYPE) {
             return arbChar();
         } else if (primitiveT == Byte.TYPE) {
             return arbByte();
         } else if (primitiveT == Short.TYPE) {
             return arbShort();
         } else if (primitiveT == Integer.TYPE) {
             return arbInt();
         } else if (primitiveT == Long.TYPE) {
             return arbLong();
         } else if (primitiveT == Float.TYPE) {
             return arbFloat();
         } else if (primitiveT == Double.TYPE) {
             return arbDouble();
         } else { /* Void.TYPE */
             return null;
         }
     }
     
     long getSeed() {
         return randomSeed;
     }
     
     void setSeed(long randomSeed) {
         random.setSeed(this.randomSeed = randomSeed);
     }
 }
