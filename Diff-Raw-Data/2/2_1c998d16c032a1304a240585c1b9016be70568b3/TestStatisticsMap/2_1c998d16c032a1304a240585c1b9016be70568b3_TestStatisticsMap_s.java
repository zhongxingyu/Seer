 // Copyright (C) 2000, 2001, 2002, 2003, 2004, 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.statistics;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.grinder.common.AbstractTestSemantics;
 import net.grinder.common.GrinderProperties;
 import net.grinder.common.Test;
 
 
 /**
  * A map of test numbers to {@link TestStatistics}s.
  *
  * <p>Test statistics synchronisation occurs at the granularity of the
  * contained {@link RawStatistics} instances. The map is synchronised
  * on the <code>TestStatisticsMap</code> itself.</p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestStatisticsMap implements java.io.Externalizable {
 
   // The serialVersionUID should be incremented whenever the default
   // statistic indicies are changed in StatisticsIndexMap.
   private static final long serialVersionUID = 2L;
 
   /**
    * Use a TreeMap so we store in test number order. Synchronise on
    * this TestStatisticsMap before accessing.
    */
   private final Map m_data = new TreeMap();
 
   /**
    * Creates a new <code>TestStatisticsMap</code> instance.
    */
   public TestStatisticsMap() {
   }
 
   /**
    * Put a new {test, statistics} pair in the map.
    *
    * @param test A test.
    * @param statistics The test's statistics.
    */
   public final void put(Test test, TestStatistics statistics) {
     if (!(statistics instanceof TestStatisticsImplementation)) {
       throw new RuntimeException(
         "TestStatistics implementation not supported");
     }
 
     synchronized (this) {
       m_data.put(test, statistics);
     }
   }
 
   /**
    * Return the number of entries in the
    * <code>TestStatisticsMap</code>. For use by the unit tests.
    *
    * @return an <code>int</code> value
    */
   final int size() {
     return m_data.size();
   }
 
   /**
    * Add the values in another <code>TestStatisticsMap</code> to this
    * <code>TestStatisticsMap</code>.
    *
    * @param other The other <code>TestStatisticsMap</code>.
    */
   public final void add(TestStatisticsMap other) {
     final TestStatisticsFactory testStatisticsFactory =
       TestStatisticsFactory.getInstance();
 
     synchronized (other) {
       final Iterator otherIterator = other.new Iterator();
 
       while (otherIterator.hasNext()) {
         final Pair othersPair = otherIterator.next();
 
         final TestStatistics statistics;
 
         synchronized (this) {
           final TestStatistics existingStatistics =
             (TestStatistics)m_data.get(othersPair.getTest());
 
           if (existingStatistics == null) {
             statistics = testStatisticsFactory.create();
             put(othersPair.getTest(), statistics);
           }
           else {
             statistics = existingStatistics;
           }
         }
 
         statistics.add(othersPair.getStatistics());
       }
     }
   }
 
   /**
    * Reset all our statistics and return a snapshot.
   * 
    * @return The snapshot.
    */
   public TestStatisticsMap reset() {
     final TestStatisticsMap result = new TestStatisticsMap();
 
     final TestStatisticsFactory testStatisticsFactory =
       TestStatisticsFactory.getInstance();
 
     synchronized (this) {
       final Iterator iterator = new Iterator();
 
       while (iterator.hasNext()) {
         final Pair pair = iterator.next();
 
         final TestStatistics statistics = pair.getStatistics();
         final TestStatistics snapshot;
 
         synchronized (statistics) {
           snapshot = (TestStatistics)statistics.snapshot();
           statistics.reset();
         }
 
         result.put(pair.getTest(), snapshot);
       }
     }
 
     return result;
   }
 
   /**
    * Implement value based equality. Used by unit tests, so we don't
    * bother with synchronisation.
    *
    * @param o <code>Object</code> to compare to.
    * @return <code>true</code> if and only if the two objects are equal.
    */
   public final boolean equals(Object o) {
     if (o == this) {
       return true;
     }
 
     if (!(o instanceof TestStatisticsMap)) {
       return false;
     }
 
     final TestStatisticsMap otherMap = (TestStatisticsMap)o;
 
     if (m_data.size() != otherMap.m_data.size()) {
       return false;
     }
 
     final Iterator iterator = new Iterator();
     final Iterator otherIterator = otherMap.new Iterator();
 
     while (iterator.hasNext()) {
       final Pair pair = iterator.next();
       final Pair otherPair = otherIterator.next();
 
       if (!pair.getTest().equals(otherPair.getTest()) ||
           !pair.getStatistics().equals(otherPair.getStatistics())) {
         return false;
       }
     }
 
     return true;
   }
 
   /**
    * Defer to <code>Object.hashCode().</code>
    *
    * <p>We define <code>hashCode</code> to keep Checkstyle happy, but
    * we don't use it.
    *
    * @return The hash code.
    */
   public final int hashCode() {
     return super.hashCode();
   }
 
   /**
    * Return a <code>String</code> representation of this
    * <code>TestStatisticsMap</code>.
    *
    * @return The <code>String</code>
    */
   public String toString() {
     final StringBuffer result = new StringBuffer();
 
     result.append("TestStatisticsMap = {");
 
     synchronized (this) {
       final Iterator iterator = new Iterator();
 
       while (iterator.hasNext()) {
         final Pair pair = iterator.next();
 
         result.append("(");
         result.append(pair.getTest());
         result.append(", ");
         result.append(pair.getStatistics());
         result.append(")");
       }
     }
 
     result.append("}");
 
     return result.toString();
   }
 
   /**
    * Efficient externalisation method.
    *
    * @param out Handle to the output stream.
    * @exception IOException If an I/O error occurs.
    */
   public void writeExternal(ObjectOutput out) throws IOException {
 
     final TestStatisticsFactory testStatisticsFactory =
       TestStatisticsFactory.getInstance();
 
     synchronized (this) {
       out.writeInt(m_data.size());
 
       final Iterator iterator = new Iterator();
 
       while (iterator.hasNext()) {
         final Pair pair = iterator.next();
 
         out.writeInt(pair.getTest().getNumber());
 
         // Its a class invariant that our TestStatistics are all
         // TestStatisticsImplementations.
         testStatisticsFactory.writeStatisticsExternal(
           out, (TestStatisticsImplementation)pair.getStatistics());
       }
     }
   }
 
   /**
    * Efficient externalisation method. No synchronisation, assume that
    * we're being read into a new instance.
    *
    * @param in Handle to the input stream.
    * @exception IOException If an I/O error occurs.
    */
   public void readExternal(ObjectInput in) throws IOException {
 
     final int n = in.readInt();
 
     final TestStatisticsFactory testStatisticsFactory =
       TestStatisticsFactory.getInstance();
 
     m_data.clear();
 
     for (int i = 0; i < n; i++) {
       m_data.put(new LightweightTest(in.readInt()),
                  testStatisticsFactory.readStatisticsExternal(in));
     }
   }
 
   /**
    * Light weight test implementation that the console uses.
    */
   private static final class LightweightTest extends AbstractTestSemantics {
     private final int m_number;
 
     public LightweightTest(int number) {
       m_number = number;
     }
 
     public int getNumber() {
       return m_number;
     }
 
     public String getDescription() {
       return "";
     }
 
     public GrinderProperties getParameters() {
       throw new UnsupportedOperationException(
         getClass().getName() + ".LightweightTest.getParameters()");
     }
   }
 
   /**
    * A type safe iterator. Should synchronize on the
    * <code>TestStatisticsMap</code> around use.
    */
   public final class Iterator {
     private final java.util.Iterator m_iterator;
 
     /**
      * Creates a new <code>Iterator</code> instance.
      */
     public Iterator() {
       m_iterator = m_data.entrySet().iterator();
     }
 
     /**
      * Check whether we are at the end of the {@link
      * TestStatisticsMap}.
      *
      * @return <code>true</code> if there is a next {@link
      * TestStatisticsMap.Pair}.
      */
     public boolean hasNext() {
       return m_iterator.hasNext();
     }
 
     /**
      * Get the next {@link TestStatisticsMap.Pair} from the {@link
      * TestStatisticsMap}.
      *
      * @return The next {@link TestStatisticsMap.Pair}.
      * @throws java.util.NoSuchElementException If there is no next element.
      */
     public Pair next() {
       final Map.Entry entry = (Map.Entry)m_iterator.next();
       final Test test = (Test)entry.getKey();
       final TestStatistics statistics = (TestStatistics)entry.getValue();
 
       return new Pair(test, statistics);
     }
   }
 
   /**
    * A type safe pair of a {@link net.grinder.common.Test} and a
    * {@link TestStatistics}.
    */
   public static final class Pair {
     private final Test m_test;
     private final TestStatistics m_statistics;
 
     private Pair(Test test, TestStatistics statistics) {
       m_test = test;
       m_statistics = statistics;
     }
 
     /**
      * Get the {@link net.grinder.common.Test}.
      *
      * @return  The {@link net.grinder.common.Test}.
      */
     public Test getTest() {
       return m_test;
     }
 
     /**
      * Get the {@link TestStatistics}.
      *
      * @return The {@link TestStatistics}.
      */
     public TestStatistics getStatistics() {
       return m_statistics;
     }
   }
 }
