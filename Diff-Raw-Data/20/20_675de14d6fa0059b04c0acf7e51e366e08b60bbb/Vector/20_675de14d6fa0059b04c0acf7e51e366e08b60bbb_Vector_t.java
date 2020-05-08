 package de.engine.math;
 
 import java.text.MessageFormat;
 
 public class Vector implements Cloneable
 {
     public static final Integer ZEILENSUMMENNORM = 10; 
     public static final Integer EUKLIDISCHENORM  = 11;
     
     private boolean calcLength;
     private double length;
     
     private double x;
     private double y;
     
     private Double value[] = new Double[2];
   
     public Vector()
     {
         x = 0.0;
         y = 0.0;
         
         this.value[0] = 0d;
         this.value[1] = 0d;
         
         calcLength = true;
         length = 0.0;
     }
     
     public Vector(double x, double y)
     {
         this.x = x;
         this.y = y;
         
         this.value[0] = x;
         this.value[1] = y;
         
         calcLength = false;
     }
     
     public Vector(Vector vector)
     {
         this(vector.getX(), vector.getY());
     }
     
     public double getX()
     {
         return x;
     }
     
     public double getY()
     {
         return y;
     }
     
     public void setX(double x)
     {
         this.x = x;
         calcLength = false;
         
     }
     
     public void setY(double y)
     {
         this.y = y;
         calcLength = false;
     }
     
     public void setPoint(double x, double y)
     {
         this.x = x;
         this.y = y;
         calcLength = false;
     }
     
     public double getLength()
     {
         if (!calcLength)
         {
             length = Util.distanceToOrigin(this);
             calcLength = true;
         }
         return length;
     }
     
     public Vector add(Vector v)
     {
         this.x += v.x;
         this.y += v.y;
         calcLength = false;
         return this;
     }
     
     public Vector add(double x, double y)
     {
         this.x += x;
         this.y += y;
         calcLength = false;
         return this;
     }
     
     public Vector minus(Vector v)
     {
         this.x -= v.x;
         this.y -= v.y;
         calcLength = false;
         return this;
     }
     
     public Vector minus(double x, double y)
     {
         this.x -= x;
         this.y -= y;
         calcLength = false;
         return this;
     }
     
     public Vector scale(double s)
     {
         x *= s;
         y *= s;
         return this;
     }
     
     public Vector getNormalVector()
     {
         return new Vector(-1 * y, x);
     }
     
     public Vector getUnitVector()
     {
         double scale = 1 / getLength();
         return Util.scale(this, scale);
     }
     
     
     
     
     // An other form calculating with vectors -- maybe rewrite the old variant?
     // import from numeric project is major essential for jakobiMatrix calculation
     public Double get(int index)
     {
         return value[index];
     }
     
     public void set(int index, Double value)
     {
         this.value[index] = value;
     }
     
     public Double norm( Integer n ) throws RuntimeException 
     {
         if( n==10 ) return zsnorm();
         if( n==11 ) return eunorm();
         
         throw new RuntimeException( MessageFormat.format("'MathLib.getNorm()' liefert den Wert {0}, für welche es keine Normimplementierung für Vektoren gibt.", n ));
     }
     
     private Double zsnorm() 
     {
         Double sum = 0d;
         Double max = 0d;
 
         for(int i=0; i < 2; i++) 
         {
             sum = Math.abs( get(i) );
             if ( max.compareTo( sum ) == -1 ) max = sum;
         }
         return max;
     }
     
     
     private Double eunorm() 
     {
         Double euklidNorm = 0d;
         Double        sum = 0d;
         
         for(int i=0; i < 2; i++) sum = sum + get(i)*get(i);
         
         euklidNorm = Math.sqrt( sum );
         
         return euklidNorm;
     }
     
     
     public Vector setUnitVector(Vector vector) 
     {
         for(int i=0; i < 2; i++) 
         {
             vector.set(i, 1d);
         }
         return vector;
     }
     
     
     @Override
     public Vector clone()
     {
         Vector copy = new Vector();
         
         for (int i = 0; i < 2; i++)
         {
             copy.set(i, get(i));
         }
        
        copy.x = this.x;
        copy.y = this.y;
        
         return copy;
     }
     
     
     public Double[] toDoubleArray() 
     {
         Double x[] = new Double[2];
         for(int i=0; i < 2; i++) x[i] = get(i).doubleValue();
         
         return x;
     }
     
     
     public Vector subtract(Vector vector)
     {
         return addit( vector.multi( -1d ));
     }
     
     
     public Vector multi( Double value)
     {
         Vector v = new Vector();
         for (int i = 0; i < 2; i++)
         {
             v.set(i, this.value[i] * value);
         }
         return v;
     }
     
     public Vector addit(Vector vector)
     {
         Vector v = new Vector();
         for (int i = 0; i < 2; i++)
         {
             v.set(i, this.value[i] + vector.get(i));
         }
         return v;
     }
     
     /**
      * Tauscht die Reihen eines Vektors durch zwei Indizes
      * @param rowIndex1 erster Index
      * @param rowIndex2 zweiter Index
      */
     public Vector swapRows(int rowIndex1, int rowIndex2)
     {
         Vector tempVector = clone();
         
         Double tempBigDecimal = get(rowIndex1);
         tempVector.set(rowIndex1, get(rowIndex2));
         tempVector.set(rowIndex2, tempBigDecimal);
         
         return tempVector;
     }
 }
