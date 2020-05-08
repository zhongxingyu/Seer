 // Copyright (C) 2 003 Uwe Alex, Ulmenweg 22,68167 Mannheim, Germany
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 import java.util.*;
 import java.math.*;
 import java.io.*;
 
 /**
  * This Class is designed to work with huge permutations.
  * The webpages <a href="http://www.merriampark.com/perm.htm">http://www.merriampark.com/perm.htm</a> and
  <a href="http://www-sfb288.math.tu-berlin.de/~jtem/">http://www-sfb288.math.tu-berlin.de/~jtem/</a>
  * did inspire me to some changes and improvements.
  * You may download the SourceCode here: <a href="http://www.uwe-alex.de/Permutation/Permutation.java"> http://www.uwe-alex.de/Permutation/Permutation.java </a>
  * <p>
  * The Class Permutation behave some like a not resizable ArrayList of int's.
  * The <tt>size</tt> and <tt>get</tt> operations run in constant time, <tt>indexOf</tt>
  * and <tt>toArray<\tt> run in linear time. The results are the integers of the current permutation
  * not other permutations.
  * <p>
  * The class Permutation behave some like a ListTterator of a "List" of permutations.
  * The methods <tt>next</tt>(O(2)),<tt>hasNext</tt>(O(N)),<tt>previous</tt>(O(2)),
  * <tt>hasPrevious</tt>(O(N))
  * allows the programmer to traverse the (not realy existing) "list" of permutations in either
  * direction, and obtain the permutation's current position in that "list". Other than
  * a ListIterator this class does have a current element, its cursor position
  * always lies at the element between the element that would be returned by a call to previous()
  * and the element that would be returned by a call to next(). Special are the methods
  * <tt>next(i)</tt>( O(N*(lg(N) ) and <tt>setPermutationNumber()</tt> and <tt>getPermutationNumber</tt>
  * witch let you work with permutations like an ArrayList.
 
  * The "list" of permutations with N elements does have 1*2* .. *N elements.
  * This is usally more than any computers memory capacity. So these permutations are
  * calculateted from the present permutation. The methods <tt>next</tt> and <tt>previous</tt>
  * are running in constant average time.
 <p>
  * This Class implements the Intefaces Comparator and Comparable (see the Java Collections Framework). The Permutations
  * are compared like "Strings". A smaller Permutation does have a lower PermutationNumber.
 <p>
 <pre>
 
 toString  PermNum get(0) get(1) get(2) indexOf(1) hasNext
 [0,1,2]   0       0      1      2      1          true
 [0,2,1]   1       0      2      1      2          true
 [1,0,2]   2       1      0      2      0          true
 [1,2,0]   3       1      2      0      0          true
 [2,0,1]   4       2      0      1      2          true
 [2,1,0]   5       2      1      0      1          false
 
 </pre>
 
 <pre>
 
 <b>Example</b>:
 
     public static void main(String[] args)
     {
       System.out.println("The Permutations of  Alex,Otto,Uwe :");
       System.out.println();
       String[] s={"Alex","Otto","Uwe"};
       Permutation p=new Permutation(s.length);
       for (int i=0;i<=6;i++)
       {
          for(int j=0;j<3;j++)
              System.out.print(s[p.get(j)]+",");
          System.out.println(" PermutationNumber: "+p.getPermutationNumber());
          p.next();
       }
 
       System.out.println();
       System.out.println("Select PermutationNumber 4");
       p.setPermutationNumber(4);
 
       for(int j=0;j<3;j++)
              System.out.print(s[p.get(j)]+",");
       System.out.println(" Number: "+p.getPermutationNumber());
 
       System.out.println();
       System.out.println("Select PermutationNumber 2");
       p.setPermutationNumber(2);
 
       for(int j=0;j<3;j++)
              System.out.print(s[p.get(j)]+",");
       System.out.println(" Number: "+p.getPermutationNumber());
 
       System.out.println();
       System.out.println("next()");
       p.next();
 
       for(int j=0;j<3;j++)
              System.out.print(s[p.get(j)]+",");
       System.out.println(" Number: "+p.getPermutationNumber());
 
     }
 
 <b>Output</b>:
 
 The Permutations of  Alex,Otto,Uwe :
 
 Alex,Otto,Uwe, PermutationNumber: 0
 Alex,Uwe,Otto, PermutationNumber: 1
 Otto,Alex,Uwe, PermutationNumber: 2
 Otto,Uwe,Alex, PermutationNumber: 3
 Uwe,Alex,Otto, PermutationNumber: 4
 Uwe,Otto,Alex, PermutationNumber: 5
 Alex,Otto,Uwe, PermutationNumber: 0
 
 Select PermutationNumber 4
 Uwe,Alex,Otto, Number: 4
 
 Select PermutationNumber 2
 Otto,Alex,Uwe, Number: 2
 
 next()
 Otto,Uwe,Alex, Number: 3
 
 
 <b>Example</b>: Magic Square 4 x 4
 
 
   public static void main(String[] args)
   {
       // Demonstrates the use of the Permutation Class!
 
       Permutation p=new Permutation(16);
       Permutation q;
       do
       {
               q=(Permutation) p.clone();
               // Check the Lines
               while (p.get(0)+p.get(1)+p.get(2)+p.get(3)!=30) p.next(3);
               while (p.get(4)+p.get(5)+p.get(6)+p.get(7)!=30) p.next(7);
               while (p.get(8)+p.get(9)+p.get(10)+p.get(11)!=30) p.next(11);
               //if  (p.get(12)+p.get(13)+p.get(14)+p.get(15)!=30) p.next(15);
 
               // Check the Colums
               if   (p.get(0)+p.get(4)+p.get(8)+p.get(12)!=30) p.next(12);
               if   (p.get(1)+p.get(5)+p.get(9)+p.get(13)!=30) p.next(13);
               if   (p.get(2)+p.get(6)+p.get(10)+p.get(14)!=30) p.next(14);
               //if (p.get(3)+p.get(7)+p.get(11)+p.get(15)!=30) p.next(15);
 
               // Check the Diagonals
               if (p.get(3)+p.get(6)+p.get(9)+p.get(12)!=30) p.next(12);
               if (p.get(0)+p.get(5)+p.get(10)+p.get(15)!=30) p.next(15);
 
       }while(!p.equals(q)); // No errors found by checking all
 
       // Print the Square
 
       for(int i=0;i<16;i++)
       {
               int w=p.get(i)+1;
               if (w<10) System.out.print(" ");
               System.out.print(" "+w);
               if ((i+1)%4==0) System.out.println();
       }
   }
 
 
 
 <b>Output</b>:
 
   1  2 15 16
  12 14  3  5
  13  7 10  4
   8 11  6  9
 
 <b>Example</b> Magic Square 5 x 5
 
 
   public static void main(String[] args)
   {
 
       int N=25;
       Permutation p=new Permutation(N);
       Permutation q;
 	  for (int sn=1;sn<4;sn++)
 	  {
 	      do
           {
                 q=(Permutation) p.clone();
                 // Check the lines
                 for(int i=20;i>=0;i-=5)
                 {
                    if (p.nextGE(i+2,13-p.get(i)-p.get(i+1))<0)p.next(i+1);
                    if (p.nextGE(i+3,36-p.get(i)-p.get(i+1)-p.get(i+2))<0)p.next(i+2);
                    if (!p.nextEQ(i+4,60-p.get(i)-p.get(i+1)-p.get(i+2)-p.get(i+3),i+3))p.next(i+3);
                 }
                // Check the Colums
                 for(int i=4;i>=0;i--)
                 {
                     if (p.nextGE(i+10,13-p.get(i)-p.get(i+5))<0)p.next(i+9);
                     if (p.nextGE(i+15,36-p.get(i)-p.get(i+5)-p.get(i+10))<0)p.next(i+14);
                     if (!p.nextEQ(i+20,60-p.get(i)-p.get(i+5)-p.get(i+10)-p.get(i+15),i+15))p.next(i+15);
                 }
                 // Check the Diagonals
                 if (p.nextGE(12,13-p.get(4)-p.get(8))<0)p.next(11);
                 if (p.nextGE(16,36-p.get(4)-p.get(8)-p.get(12))<0)p.next(15);
                 if (!p.nextEQ(20,60-p.get(4)-p.get(8)-p.get(12)-p.get(16),16))p.next(16);
 
                 if (p.nextGE(12,13-p.get(0)-p.get(6))<0)p.next(11);
                 if (p.nextGE(18,36-p.get(0)-p.get(6)-p.get(12))<0)p.next(17);
                 if (!p.nextEQ(24,60-p.get(0)-p.get(6)-p.get(12)-p.get(18),18))p.next(18);
           }while(!p.equals(q)); // No errors found by checking all
        // Print the Square
 
 
           System.out.println();System.out.println("Square No:"+sn);
           System.out.println("Pemutation Number: "+p.getPermutationNumber());
           System.out.println();
 
           for(int i=0;i<25;i++)
           {
                   int w=p.get(i)+1;
                   if (w<10) System.out.print(" ");
                   System.out.print(" "+w);
                   if ((i+1)%5==0) System.out.println();
           }
        }
   }
 
 
 
 <b>Output</b>:
 
 
 Square No:1
 Pemutation Number: 12310598036244579013654
 
   1  2 13 24 25
   3 22 19  6 15
  23 16 10 11  5
  21  7  9 20  8
  17 18 14  4 12
 
 Square No:2
 Pemutation Number: 12310603413326743456918
 
   1  2 13 24 25
   3 23 16  8 15
  21 19 10  6  9
  22  4 14 20  5
  18 17 12  7 11
 
 Square No:3
 Pemutation Number: 12310604397993244628398
 
   1  2 13 24 25
   3 23 19  4 16
  21 15 10 12  7
  22  8  9 20  6
  18 17 14  5 11
 
 
 
 
 
 </pre>
  * @author  Uwe Alex, Ulmenweg 22, 68167 Mannheim, Germany <a href="http://www.uwe-alex.de">http://www.uwe-alex.de</a>
 */
 
public class Permutation implements Cloneable,Serializable,Iterator,Comparator,Comparable
 {
     //-----------------------------Fields---------------------------------------------
 
    /** Containing the permutation. The Numbers 0,1,2,...N-1 */
     private int[] Al;
     /** The size of the int[] Array Al containing the permutation.*/
     private int N;
     private BigInteger[] fak=null;
     //-----------------------------Constructors---------------------------------------
 
     /**
     *   Constructs a Permutation with a size n.
     *   Initialisize Permutation with the Numbers: 0,1,2, ... , N-2, N-1<br>
     *   <pre><b>Example</b>:
     *
     *       Permutation p=new Permutation(4); // 0,1,2,3</pre>
     *
     *            @param  n the length of the array.
     *            @throws IllegalArgumentException if n is lower then 1.
     *
     */
     public Permutation(int n)
     {
         if (n < 1)     throw new IllegalArgumentException ("Wrong n(<1): new Permutation(int n)");
         N=n;
         Al=new int[N];
         first();
     }
     /**
             Creates a clone of p
     */
     public Permutation(Permutation p)
     {
         N=p.N;
         Al=new int[N];
         System.arraycopy(p.Al,0,Al,0,N);
     }
     /**
                 Creates a Permutation with a copy of A as Permutation.
                 The Values of A have to contain all values from 0 to A.length-1
     */
     public Permutation(int[] A)
     {
         N=A.length;
         boolean[] hb=new boolean[N]; // All false
         Al=new int[N];
         for (int i=0;i<N;i++)
         {
             Al[i]=A[i];
             System.out.println(A[i]);
             if (hb[A[i]]) throw new IllegalArgumentException ("Wrong int[]A: new Permutation(int[] A)");
             hb[A[i]]=true;
         }
      }
 
     //-----------------------------Methods--------------------------------------------
 
      /**
      * Returns a copy of this <tt>Permutation</tt> instance.  (The
      * elements themselves are copied to.)
      *
      * @return  a clone of this <tt>Permutation</tt> instance.
      */
 
     public Object clone()
     {
         Permutation p=new Permutation(this);
         return p;
     }
 
      /**
      * Searches for the first occurence of the given argument.
      *
      *
      * @param   elem   an int.
      * @return  the index of the first occurrence of the argument in this
      *          permutation; returns <tt>-1</tt> if the object is not found.
      * @see     Object#equals(Object)
      */
 
     public int indexOf(int elem)
     {
         int i;
         for (i=0;i<N;i++) if (Al[i]==elem) return i;
         return -1;
     }
 
 
     /**
      * Returns the int at the specified position in this permutation.
      *
      * @param  index index of int to return.
      * @return the position of the specified value in this permutation or -1 if not found.
      * @throws    IndexOutOfBoundsException if index is out of range <tt>(index
      *           &lt; 0 || index &gt;= size())</tt>.
      */
     public int get(int index)
     {
         return Al[index];
     }
 
 
      /**
      * Swaps the elements at the specified positions in this permutation.
      * (If the specified positions are equal, invoking this method leaves
      * the permutation unchanged.)
      *
      * @param i the index of one element to be swapped.
      * @param j the index of the other element to be swapped.
      * @throws IndexOutOfBoundsException if either <tt>i</tt> or <tt>j</tt>
      *         is out of range (i &lt; 0 || i &gt;= permutation.size()
      *         || j &lt; 0 || j &gt;= permutation.size()).
      */
 
     public void swap(int i,int j)
     {
         if (i!=j)
         {
             int h=Al[i];Al[i]=Al[j];Al[j]=h;
         }
     }
 
     /**
      * Randomly permutes the array of this permutation using a default source of
      * randomness.  All permutations occur with approximately equal
      * likelihood.<p>
      *
      * The hedge "approximately" is used in the foregoing description because
      * default source of randomenss is only approximately an unbiased source
      * of independently chosen bits. If it were a perfect source of randomly
      * chosen bits, then the algorithm would choose permutations with perfect
      * uniformity.<p>
      *
      * This implementation traverses the permutation array forward, from the first element
      * up to the second last, repeatedly swapping a randomly selected element into
      * the "current position".  Elements are randomly selected from the
      * portion of the permutation array that runs from the current position to the
      * last element, inclusive.<p>
      *
      * This method runs in linear time.
      */
     public void shuffle()
     {
         Random r=new Random();
         for (int i=0;i<N-1;i++) swap(i,i+r.nextInt(N-i));
     }
 
     /**
     Returns the number of integers in this permutation.
     <pre>
             <b>Example</b>:
 
                 Permutation p=new Permutation(4);
                 int a=p.size();  //a=4
     </pre>
     * @return  the number of elements in this permutation.
     */
     public int size()
     {
         return N;
     }
 
     /**
      * Returns <tt>true</tt> if this permutation is not the last
      * traversing the "list" of permutations in the forward direction.
      * (In other words, returns <tt>true>/tt> if this permutation is
      * not [N-1,N-2,...,1,0]
      *
      *
      * @return <tt>true</tt> if next has more elements when
      *        traversing the "list" of permutations in the forward direction.
      */
 
     public boolean hasNext()
     {
             return !isLast();
     }
 
     /**
      * Returns <tt>true</tt> if this permutation is the last
      * traversing the "list" of permutations in the forward direction.
      * (In other words, returns <tt>true>/tt> if this permutation is
      * [N-1,N-2,...,1,0]
      *
      *
      * @return <tt>true</tt> if next has no more elements when
      *        traversing the "list" of permutations in the forward direction.
      */
     public boolean isLast()
     {
         for(int i=0;i<N;i++)
             if(Al[i]!=N-1-i) return false;
         return true;
     }
 
      /**
      * Returns <tt>true</tt> if this permutation is not the first
      * traversing the "list" of permutations in the backward direction.
      * (In other words, returns <tt>true>/tt> if this permutation is
      * not [0,1,2,...,N-2,N,1]
      *
      *
      * @return <tt>true</tt> if next has more elements when
      *        traversing the "list" of permutations in the backward direction.
      */
     public boolean hasPrevious()
     {
         return !isFirst();
     }
     /**
         Returns true if this equals 0,1,2,...N-2,N-1, false if not
 
         @return true if this equals 0,1,2,...N-2,N-1, false if not
     */
 
     public boolean isFirst()
     {
         for(int i=0;i<N;i++)
             if(Al[i]!=i) return false;
         return true;
     }
 
     /**
 
         Returns true if <i>this</i> is a even permutation.
         A even permutation can be build in a even number of transpositions starting with the identity.
 
         @return true if <i>this</i> is a even permutation, false if not.
     */
 
     public boolean isEven()
     {
         int[] h=new int[N];
         int i;
         boolean result=true;
         for(i=0;i<N;i++)h[i]=Al[i];
         for (i=0;i<N;i++)
             while(h[i]!=i)
             {
                 int j=h[i];
                 h[i]=h[j];
                 h[j]=j;// (i==2,h[i]=5)  x x 5 x x y x x x .. swap(2,5) -> x x y x x 5 x x also einer mehr an richtiger position
                 result=!result;
             }
         return result;
     }
 
     /**
 
         Returns true if <i>this</i> is a odd permutation.
         A even permutation can be build in a odd number of transpositions starting with the identity.
         The result is calculated by isOdd is !isEven.
 
         @return true if <i>this</i> is a odd permutation, false if not.
     */
     public boolean isOdd()
     {
         return !isEven();
     }
 
 
      /**
      * Each permutation can be written as a product of transpositions. Each
      * cycle in the  decomposition gives rise to an independant
      * product of transposition. The oermutation [2,3,...,n,1} can be written as
      * the composition of n-1 transpositions: (1,n)(1,n-1)...(1,2). This
      * method returns the minimal total number of required transpositions.
      *
      * @return an <code>int</code> value
      */
     public int numTranspos()
     {
         int result = 0;
         int[] h=new int[N];
         int i;
         for(i=0;i<N;i++)h[i]=Al[i];
         for (i=0;i<N;i++)
             while(h[i]!=i)
             {
                 int j=h[i];
                 h[i]=h[j];
                 h[j]=j;// (i==2,h[i]=5)  x x 5 x x y x x x .. swap(2,5) -> x x y x x 5 x x also einer mehr an richtiger position
                 result++;
             }
         return result;
     }
 
     /**
      * Returns whether <i>this</i> is a valid derangement
      * (permutation without fixed point) of the indices
      * <code>0..N-1</code>.     *
     *
      * @return true if <i>this</i> is a valid derangement, false if not.
      */
     public boolean isDerangement()
     {
         for (int i=0;i<N;i++)
                 if(Al[i]==i) return false;
         return true;
     }
 
 
     public int[] toArray()
     {
         int[] al=new int[N];
         System.arraycopy(al,0,Al,0,N);
         return al;
     }
 
     /**
      * Returns a string representation of this permutation.  The string
      * representation consists of a list of the permutations elements
      * enclosed in square brackets
      * (<tt>"[]"</tt>). <p>
      *
      * This implementation creates an empty string buffer, appends a left
      * square bracket, and iterates over the permutation appending the string
      * representation of each element in turn.  After appending each element
      * , the string <tt>", "</tt> is appended.  Finally the last element and a right
      * bracket is appended.  A string is obtained from the string buffer, and
      * returned.
      *
      * @return a string representation of this collection.
      */
 
     public String toString()
     {
     	StringBuffer buf = new StringBuffer();
         buf.append("[");
 
         for(int i=0;i<N-1;i++)buf.append(Al[i]+",");
         buf.append(Al[N-1]+"]");
         return buf.toString();
     }
     /**
      * Compares its two arguments for order.  Returns a negative integer,
      * zero, or a positive integer as the first argument is less than, equal
      * to, or greater than the second.<p>
      *
      * The implementor does ensure that <tt>sgn(compare(x, y)) ==
      * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
      * implies that <tt>compare(x, y)</tt> must throw an exception if and only
      * if <tt>compare(y, x)</tt> throws an exception.)<p>
      *
      * The implementor does ensure that the relation is transitive:
      * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
      * <tt>compare(x, z)&gt;0</tt>.<p>
      *
      * Finally, the implementer does ensure that <tt>compare(x, y)==0</tt>
      * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
      * <tt>z</tt>.<p>
      *
 
      * <tt>(compare(x, y)==0) == (x.equals(y))</tt>. "Note: this comparator
      * is consistent with equals."
      *
      * @param o1 the first object to be compared.
      * @param o2 the second object to be compared.
      * @return a negative integer, zero, or a positive integer as the
      *            first argument is less than, equal to, or greater than the
      *           second.
      * @throws ClassCastException if the arguments' types prevent them from
      *         being compared by this Comparator.
      */
 
     public int compare(Object o1, Object o2)
     {
           if ((!(o1 instanceof Permutation))||(!(o2 instanceof Permutation)))
              throw new ClassCastException();
           Permutation p1=(Permutation)o1,p2=(Permutation)o2;
           int maxIndex;
           maxIndex=(p1.N<p2.N)?p1.N:p2.N;
           for(int i=0;i<maxIndex;i++)
               if (p1.Al[i]!=p2.Al[i])if (p1.Al[i]<p2.Al[i]) return -1;else return 1;
           if (p1.N<p2.N) return -1;
           if (p1.N>p2.N) return 1;
           return 0;
     }
 
 
     /**
      * Compares this object with the specified object for order.  Returns a
      * negative integer, zero, or a positive integer as this object is less
      * than, equal to, or greater than the specified object.<p>
      *
      *
      * The implementor does ensure <tt>sgn(x.compareTo(y)) ==
      * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
      * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
      * <tt>y.compareTo(x)</tt> throws an exception.)<p>
      *
      * The implementor does also ensure that the relation is transitive:
      * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
      * <tt>x.compareTo(z)&gt;0</tt>.<p>
      *
      * Finally, the implementer does ensure that <tt>x.compareTo(y)==0</tt>
      * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
      * all <tt>z</tt>.<p>
      *
      *
      * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. "Note: this class has a natural
      ordering that is consistent with equals."
      *
      * @param   o the Object to be compared.
      * @return  a negative integer, zero, or a positive integer as this object
      *        is less than, equal to, or greater than the specified object.
      *
      * @throws ClassCastException if the specified object's type prevents it
      *         from being compared to this Object.
      */
     public int compareTo(Object o)
     {
          if (!(o instanceof Permutation))
              throw new ClassCastException();
           Permutation p=(Permutation)o;
           int maxIndex;
           maxIndex=(this.N<p.N)?this.N:p.N;
           for(int i=0;i<maxIndex;i++)
               if (this.Al[i]<p.Al[i]) return -1;
           if (this.N<p.N) return -1;
           if (this.N>p.N) return 1;
           return 0;
     }
 
 
     /**
      * Compares the specified object with this permutation for equality.  Returns
      * <tt>true</tt> if and only if the specified object is also a permutation, both
      * permutations have the same size, and all corresponding pairs of elements in
      * the two permutations are <i>equal</i>.  In other words, two permutations
      * are defined to be
      * equal if they contain the same elements in the same order.<p>
      *
      * This implementation first checks if the specified object is this
      * list. If so, it returns <tt>true</tt>; if not, it checks if the
      * specified object is a permutation. If not, it returns <tt>false</tt>; if so,
      * the integer arrays are compared
      * @param o the object to be compared for equality with this permutation.
      *
      * @return <tt>true</tt> if the specified object is equal to this permutation.
      */
     public boolean equals(Object o)
     {
         if (o == this)  return true;
         if (!(o instanceof Permutation)) return false;
         int[] al=((Permutation)o).Al;
          for (int i=0; i<N; i++)
             if (Al[i] != al[i])
                 return false;
 
         return true;
     }
 
 
     /**
      * Returns the inverse permutation of <i>this</i>.
      *
      * @return the inverse permutation of <i>this</i>.
      */
 
     public Permutation inverse()
     {
         int [] h=new int[N];
         for(int i=0;i<N;i++)
             h[Al[i]]=i;
         return new Permutation(h);
     }
 
      /** Change <i>this</i> to the first (lowest) possible permutation .
      <pre>
              <b>Example</b>s:
 
              before            after first()
              [0,3,1,2]         [0,1,2,3]
              [0,1]             [0,1]
              [2,1,0]           [0,1,2]
 
      </pre>
 
      The numbers from 0 to N-1 are storted in ascending numerical order.
 
      */
 
 
 
     public void first()
     {
             for (int i=0;i<N;i++)Al[i]=i;
     }
 
 
     /**
      * Change <i>this</i> to the last (highest) possible permutation .
      <pre>
              <b>Example</b>s:
 
              before            after
              [0,3,1,2]         [3,2,1,0]
              [0,1]             [1,0]
              [2,1,0]           [2,1,0]
 
      </pre>
 
      The numbers from 0 to N-1 are stored in descending numerical order in the
      permutation array.
 
      */
 
     public void last()
     {
            for (int i=0;i<N;i++)Al[i]=N-i-1; // i=1(1)N !
     }
 
 
     /**
      * Change the the permutation to the next permutation in sorting order.
      *  This method may be called
      * repeatedly to iterate through the permutatons, or intermixed with calls to
      * <tt>previous</tt> to go back and forth.  (Note that alternating calls
      * to <tt>next</tt> and <tt>previous</tt> will return the same element
      * repeatedly.) if isLast()==false then next will increment the PermutationNumber;
      * if isLast()==true then next() will perform like first().
      <pre>
 
 <b>Example</b>:
 
      public static void main(String[] args)
      {
          Permutation p=new Permutation(7);
          p.shuffle();
          for (int i=0;i<12;i++)
          {
              System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
              p.next();
          }
              System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
      }
 
 <b>Output</b>:
 
 [5,1,2,6,0,4,3] = PermutationNumber : 3763
 [5,1,2,6,3,0,4] = PermutationNumber : 3764
 [5,1,2,6,3,4,0] = PermutationNumber : 3765
 [5,1,2,6,4,0,3] = PermutationNumber : 3766
 [5,1,2,6,4,3,0] = PermutationNumber : 3767
 [5,1,3,0,2,4,6] = PermutationNumber : 3768
 [5,1,3,0,2,6,4] = PermutationNumber : 3769
 [5,1,3,0,4,2,6] = PermutationNumber : 3770
 [5,1,3,0,4,6,2] = PermutationNumber : 3771
 [5,1,3,0,6,2,4] = PermutationNumber : 3772
 [5,1,3,0,6,4,2] = PermutationNumber : 3773
 [5,1,3,2,0,4,6] = PermutationNumber : 3774
 [5,1,3,2,0,6,4] = PermutationNumber : 3775
 
      </pre>
      *
      */
 
     public Object next()
     {
         int Nm1=N-1;
         int p=Nm1,low,high,s,m;
         while ((p>0) && (Al[p]<Al[p-1])) p--;
         // Al ist von Index Nm1 bis p absteigend Sortiert
         // see  Arrays.binarySearch()
         if (p>0) //if p==0 then it's the last one
         {
             s=Al[p-1];
             if (Al[Nm1]>s) low=Nm1;
             else
             {
                 high=Nm1;// Position Kleinster
                low=p;// Position Grter
                while (high>low+1)// Suche nchts greren
                 {
                     m=(high+low) >> 1;
                     if (Al[m]<s) high=m;else low=m;
                 }
             }
             Al[p-1]=Al[low];
             Al[low]=s;
         }
         // else "wrap arround"
         high=Nm1;
         while (high>p)
         {
             m=Al[high];
             Al[high]=Al[p];
             Al[p]=m;
             p++;
             high--;
         }
         return this;
     }
 
         /**
      * Change the the permutation to the previous permutation in sorting order.
      *  This method may be called
      * repeatedly to iterate through the permutatons, or intermixed with calls to
      * <tt>next</tt> to go forth and back.  (Note that alternating calls
      * to <tt>next</tt> and <tt>previous</tt> will return the same element
      * repeatedly.) if isFirst()==false then previous will decrement the PermutationNumber;
      * if IsFirst()==true then previous will perform like last().
      <pre>
 
 <b>Example</b>:
 
      public static void main(String[] args)
      {
          Permutation p=new Permutation(5);
          p.setPermutationNumber(10);
          for (int i=10;i>-3;i--)
          {
              System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
              p.previous();
          }
          System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
      }
 
 <b>Output</b>:
 
 [0,2,4,1,3] = PermutationNumber : 10
 [0,2,3,4,1] = PermutationNumber : 9
 [0,2,3,1,4] = PermutationNumber : 8
 [0,2,1,4,3] = PermutationNumber : 7
 [0,2,1,3,4] = PermutationNumber : 6
 [0,1,4,3,2] = PermutationNumber : 5
 [0,1,4,2,3] = PermutationNumber : 4
 [0,1,3,4,2] = PermutationNumber : 3
 [0,1,3,2,4] = PermutationNumber : 2
 [0,1,2,4,3] = PermutationNumber : 1
 [0,1,2,3,4] = PermutationNumber : 0
 [4,3,2,1,0] = PermutationNumber : 119
 [4,3,2,0,1] = PermutationNumber : 118
 [4,3,1,2,0] = PermutationNumber : 117
 
  </pre>
      *
      */
 
     public Object previous()
     {
         int Nm1=N-1;
         int p=Nm1,low,high,s,m;
         while ((p>0) && (Al[p]>Al[p-1])) p--;
         if (p>0)
         {
             s=Al[p-1];
             if (Al[Nm1]<s) low=Nm1;
             else
             {
                 high=Nm1;// Position Kleinster
                low=p;// Position Grter
                while (high>low+1)// Suche nchts greren
                 {
                     m=(high+low) >> 1;
                     if (Al[m]>s) high=m;else low=m;
                 }
             }
             Al[p-1]=Al[low];
             Al[low]=s;
         }
         high=Nm1;
         while (high>p)
         {
             m=Al[high];
             Al[high]=Al[p];
             Al[p]=m;
             p++;
             high--;
         }
         return this;
     }
 
 
     /**
     Change to the smallest permutation bigger than <i>this</i> with a change at position <i>index</i>.
     <pre>
 
 <b>Example</b>:
 
      public static void main(String[] args)
      {
          Permutation p=new Permutation(9);
          p.shuffle();
          for (int i=0;i<12;i++)
          {
              System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
              p.next(5);
          }
              System.out.println(p+" = PermutationNumber : "+p.getPermutationNumber());
      }
 
 <b>Output</b>:
 
 [8,0,3,4,5,6,7,2,1] = PermutationNumber : 324305
 [8,0,3,4,5,7,1,2,6] = PermutationNumber : 324306
 [8,0,3,4,6,1,2,5,7] = PermutationNumber : 324312
 [8,0,3,4,6,2,1,5,7] = PermutationNumber : 324318
 [8,0,3,4,6,5,1,2,7] = PermutationNumber : 324324
 [8,0,3,4,6,7,1,2,5] = PermutationNumber : 324330
 [8,0,3,4,7,1,2,5,6] = PermutationNumber : 324336
 [8,0,3,4,7,2,1,5,6] = PermutationNumber : 324342
 [8,0,3,4,7,5,1,2,6] = PermutationNumber : 324348
 [8,0,3,4,7,6,1,2,5] = PermutationNumber : 324354
 [8,0,3,5,1,2,4,6,7] = PermutationNumber : 324360
 [8,0,3,5,1,4,2,6,7] = PermutationNumber : 324366
 [8,0,3,5,1,6,2,4,7] = PermutationNumber : 324372
 
     </pre>
     */
     public Object next(int index)
     {
         if (index>=N-2) next();
         else
         {
             int m=Al[index];
             sort(index+1,N-1);
             if (m>Al[N-1])
             {
               if (index>0) return next(index-1);
               first();
             }
             else
             {    int o;
                 for (o=N-2;Al[o]>m;o--);
                 Al[index]=Al[o+1];Al[o+1]=m;
             }
         }
         return this;
     }
 
 
     /**
     Change the value in <i>index</i> to the <i>value</i> or higher without changes at positions lower than <i>index</i>.
     Work as if as many next() are made as necessary to change the value at
     position index to <i>value</i>. If the value at position index is already
     greater-equal <i>value</i> nothing is changed.
 
     <pre>
 <b>Example</b>:
 
    public static void main(String[] args)
    {
      // Demonstrates the use of the Permutation Class!
       Permutation p=new Permutation(10);
       for(int i=0;i<10;i++)
       {
               p.shuffle();
               System.out.println(p+" Number : "+p.getPermutationNumber());
               p.next(6);
               System.out.println(p+" Number : "+p.getPermutationNumber());
               System.out.println();
       }
   }
 
 <b>Output</b>:
 
 [4,5,9,8,7,2,0,1,3,6] Number : 1653048
 [4,5,9,8,7,2,1,0,3,6] Number : 1653054
 
 [5,8,6,9,3,7,0,4,2,1] Number : 2126621
 [5,8,6,9,3,7,1,0,2,4] Number : 2126622
 
 [9,7,0,5,2,4,8,3,1,6] Number : 3551228
 [9,7,0,5,2,6,1,3,4,8] Number : 3551232
 
 [7,9,5,0,4,8,3,2,1,6] Number : 2888390
 [7,9,5,0,4,8,6,1,2,3] Number : 2888394
 
 [6,7,1,8,2,4,3,5,9,0] Number : 2428017
 [6,7,1,8,2,4,5,0,3,9] Number : 2428020
 
 [5,3,7,9,0,2,1,4,8,6] Number : 1964905
 [5,3,7,9,0,2,4,1,6,8] Number : 1964910
 </pre>
 
 
      @return 1 if the value at position is now <i>value</i>,
       0 if the value at position is now higher as <i>value</i>,
       -1 if the value at position is still lower as <i>value</i>
     */
     public int nextGE(int index,int value)
     {
 
         if (Al[index]==value) return 1; //nothing to do
         if (Al[index]>value) return 0; //nothing to do
         int pos=N,compare=N;
 
         for(int i=index+1;i<N;i++)
             if ((Al[i]>=value)&&(Al[i]<compare))
             {
                 compare=Al[i];
                 pos=i;
             }
         if (pos<N)
         {
             Al[pos]=Al[index];
             Al[index]=compare;
             sort(index+1,N-1);
             if (compare==value) return 1;
             else return 0;
         }
         return -1;
     }
 
 
     public boolean nextEQ(int index,int value,int save)
     {
         int pos;
         do
         {
         	if (Al[index]==value) return true; //nothing to do
         	for (pos=save+1;((pos<N)&&(Al[pos]!=value));pos++);//search value
         	if (pos>=N) return false;//value not found
         	if(pos<index)next(pos);
         }while (pos<index);// solange gesuchtes vorne
       	if (value>Al[index])
     	{
                 Al[pos]=Al[index];
                 Al[index]=value;
                 sort(index+1,N-1);
                 return true;
          }
          else
          {
             	if (save==index-1) return false;
             	next(index-1);
 				return nextEQ(index,value,save);
          }
     }
 
 
 
 
     /**
     Not supported.
     @exception UnsupportedOperationException
     */
     public void remove()
     {
             throw new UnsupportedOperationException ("");
      }
 
     /**
 
             Returns the number of the permutation. The first permutation
             [0,1,2,...N-2,N-1] has the number 0, the last permutation [N-1,N-2,...,0]
             has the number N!-1 .
     <pre>
         <b>Example</b>
 
             Permutation   PermutationNumber
               0,1,2          0
               0,2,1          1
               1,0,2          2
               ...            ...
 
               0,1,2,3        0
               0,1,3,2        1
               0,2,1,3        2
               0,2,3,1        3
               ...            ...
 
     </pre>
     * @return the index of <i>this</i> in the "list" of permutations.
 
     */
     // get a coffee
     public BigInteger getPermutationNumber()
     {
         if (fak==null)
         {
                fak=new BigInteger[N];
                fak[N-1]=BigInteger.ONE;
                for(int i=N-2;i>=0;i--)
                        fak[i]=fak[i+1].multiply(new BigInteger(N-i-1+""));
         }
         int[] h=new int[N];
         for (int i=0;i<N;i++)h[i]=i;
         BigInteger PermNumber=BigInteger.ZERO;
         for (int i=0;i<N-1;i++)
         {
             int j=0;
             int m=Al[i];
 /*            CRT.println(":"+i+","+m);
             for(int oo=0;oo<25;oo++)CRT.print(" "+oo+":"+h[oo]);
             CRT.ln();*/
             while (h[i+j]!=m)j++;
             PermNumber=PermNumber.add(fak[i].multiply(new BigInteger(j+"")));
             for(;j>0;j--)h[i+j]=h[i+j-1];
         }
         return PermNumber;
     }
 
 
     /**
 
         Creates the permutation with number BigInteger(s).
         Realised as setPermutationNumber(new BigInteger(s))
 
         <pre>
 <b>Example</b>:
 
    public static void main(String[] args)
    {
      // Demonstrates the use of the Permutation Class!
       Permutation p=new Permutation(20);
       p.setPermutationNumber("1234567890123456789");
       System.out.println(p+" Number : "+p.getPermutationNumber());
       p.previous();
       System.out.println(p+" Number : "+p.getPermutationNumber());
   }
 
 <b>Output</b>:
 
 [10,2,16,18,17,5,3,12,13,9,1,8,6,15,14,7,19,4,11,0] Number : 1234567890123456789
 [10,2,16,18,17,5,3,12,13,9,1,8,6,15,14,7,19,4,0,11] Number : 1234567890123456788
 
        </pre>
 
     */
     public void setPermutationNumber(String s)
     {
         setPermutationNumber(new BigInteger(s));
     }
     /**
 
         Creates the permutation with number BigInteger(l.toString()).
         See getPermutationNumber.
         Realised as setPermutationNumber(new BigInteger(l.toString()))
 
     */
 
     public void setPermutationNumber(long l)
     {
         setPermutationNumber(new BigInteger(l+""));
     }
     /**
         Creates the permutation with PermutationNumber <i>Number</i>.
         See getPermutationNumber.
     <pre>
 
               PermutationNumber   Permutation
               0                   0,1,2
               1                   0,2,1
               2                   1,0,2
               ...                 ...
 
               0                   0,1,2,3
               1                   0,1,3,2
               2                   0,2,1,3
               3                   0,2,3,1
               ...                 ...
 
         setPermutationNumber("0")                   is the same as
         setPermutationNumber(0)                     is the same as
         setPermutationNumber(BigInteger.ZERO)       is the same as
         setPermutationNumber(new BigInteger("0"))   is the same as
         first()
 
 
     </pre>
     */
     public void setPermutationNumber(BigInteger Number)
     {
         if (fak==null)
         {
                fak=new BigInteger[N];
                fak[N-1]=BigInteger.ONE;
                for(int i=N-2;i>=0;i--)
                        fak[i]=fak[i+1].multiply(new BigInteger(N-i-1+""));
         }
         Number=Number.mod(fakultaet(N));
         for(int i=0;i<N;i++)Al[i]=i;
         if (Number.equals(BigInteger.ZERO)) return;
         for (int i=0;i<N-1;i++)
         {
             BigInteger[] bi=Number.divideAndRemainder(fak[i]);
             int j=bi[0].intValue();
             if (j!=0)
             {
                 int m=Al[i+j];
                 for (;j>0;j--)Al[i+j]=Al[i+j-1];
                 Al[i]=m;
                 Number=bi[1];
             }
         }
     }
 
     private void sort(int first, int last)
     {
        int v,h;// DataTyp like the Array
        if (last-first<7)
        {
             for (int i=first; i<=last; i++)
             {
                 int j;
                 h=Al[i];
                 for (j=i; j>first && (v=Al[j-1])>h; j--)Al[j]=v;
                 Al[j]=h;
             }
             return;
         }
         int a = first, c = last;
         int m = (first + last) >> 1;
         if (Al[a]>Al[m]) {h=Al[a];Al[a]=Al[m];Al[m]=h;}
         if (Al[m]>Al[c])
         {
            h=Al[c];Al[c]=Al[m];Al[m]=h;
            if (Al[a]>h) {Al[m]=Al[a];Al[a]=h;}
         }
         v=Al[m];
         a++;c--;
         while(true)
         {
             while (Al[a] < v)  a++;
             while (Al[c] > v)  c--;
             if (a > c) break;
             h=Al[a];Al[a]=Al[c];Al[c]=h;
             a++;c--;
         }
         if (c>first) sort(first,c);
         if (a<last) sort(a,last);
     }
 
 /**
            Returns 1*2*3*4* ... *n as BigInteger
 */
     private static BigInteger fakultaet (int n)
     {
         if (n<0)      throw new IllegalArgumentException ("Wrong n(<1): fakultaet(int n)");
         BigInteger p = BigInteger.ONE;
         for (int i=2;i<=n;i++) p=p.multiply (new BigInteger(i+""));
         return p;
       }
 
 }
 
