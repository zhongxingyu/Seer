 package cryptocast.crypto;
 
import java.lang.ArithmeticException;

 /**
  * Represents a field over values of type T
  * @param <T> The values we work on
  */
 public abstract class Field<T> {
     /**
      * Adds two elements of the field
      * @param a first element
      * @param b second element
      * @return The value $a + b$
      */
     public abstract T add(T a, T b);
     /**
      * Multiplies two elements of the field
      * @param a first element
      * @param b second element
      * @return The value $a \cdot b$
      */
     public abstract T multiply(T a, T b);
     /**
      * @param a An element of the field
      * @return The additive inverse $-a$ of $a$
      */
     public abstract T negate(T a);
     /**
      * @param a An element of the field
      * @return The multiplicative inverse $a^{-1}$ of $a$
     * @throws ArithmeticExpression if a is the zero element
      */
     public abstract T invert(T a) throws ArithmeticException;
     /**
      * @return The zero element of the field
      */
     public abstract T zero();
     /**
      * @return The one element of the field
      */
     public abstract T one();
     /**
      * @return A random element of the field
      */
     public abstract T randomElement();
 
     /**
      * Subtracts two elements of the field
      * @param a first element
      * @param b second element
      * @return The value $a - b$
      */
     public T subtract(T a, T b) {
         return add(a, negate(b));
     }
     /**
      * Divides two elements of the field
      * @param a first element
      * @param b second element
      * @return The value $\frac{a}{b}$
      */
     public T divide(T a, T b) {
         return multiply(a, invert(b));
     }
     /**
      * Raises an element of the field to an integer power
      * @param a The element of the field
      * @param e The exponent
      * @return The value $a^e$
      */
     public T pow(T a, int e) {
         T result = one();
         for (int i = 0; i < e; ++i)
             result = multiply(result, a);
         return result;
     }
 }
