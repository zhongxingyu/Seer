 /**
  *
  */
 package icd3;
 
 /**
  * A date representation that repeats in a regular modular cycle
  */
 public abstract class CyclicDate<T extends CyclicDate<T>> implements MayanDate<T>
 {
     /**
      * The integer representation of this date
      */
     private int m_value;
 
     /**
      * Instantiates a new cyclic date.
      *
      * @param value The integer value corresponding to this cyclic date's equivalence class
      */
     public CyclicDate(int value)
     {
         int cycle = this.cycle();
 
         // Ensure that value is within the positive equivalence class (mod cycle)
         m_value = (value % cycle + cycle) % cycle;
     }
 
     /**
      * Give the number of date representations possible in this date type.
      *
      * @return The number of equivalence classes represented by this cyclic date.
      */
     public abstract int cycle();
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#minus(java.lang.Object)
      */
     @Override
     public int minus(T other)
     {
         if (null == other)
         {
            throw new NullPointerException("Cannot subtract a null Haab Date");
         }
 
         // Subtract the integer representations
         int difference = this.toInt() - other.toInt();
         int cycle = this.cycle();
 
         // Ensure that difference is within the positive equivalence class (mod cycle)
         return (difference % cycle + cycle) % cycle;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see icd3.MayanDate#toInt()
      */
     @Override
     public int toInt()
     {
         return m_value;
     }
 
     /*
      * (non-Javadoc)
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object o)
     {
         // Must be non-null, also a HaabDate, and have the same integer representation
         return o != null && this.getClass().equals(o.getClass()) && this.toInt() == ((CyclicDate<?>) o).toInt();
     }
 }
