 package org.decomposer.math.vector.hashmap;
 
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Map.Entry;
 
 import org.decomposer.math.vector.DiskBufferedDoubleMatrix;
 import org.decomposer.math.vector.DoubleMatrix;
 import org.decomposer.math.vector.HashMapDoubleMatrix;
 import org.decomposer.math.vector.ImmutableSparseDoubleMatrix;
 import org.decomposer.math.vector.IntDoublePair;
 import org.decomposer.math.vector.MapVector;
 import org.decomposer.math.vector.hashmap.HashMapVector;
 import org.decomposer.math.vector.hashmap.HashMapVectorFactory;
 
 
 import junit.framework.TestCase;
 
 public class TestDoubleMatrix extends TestCase
 {
 
  public static File getTmpFile() throws Exception
  {
    File sysTemp = new File(System.getProperty("java.io.tmpdir"));
    File tmpDir = new File(sysTemp, TestDoubleMatrix.class.getName());  
    return tmpDir;
  }
  
  public static void delete(File file) throws Exception
  {
    if(file.isDirectory())
    {
      for(File f : file.listFiles())
      {
        delete(f);
      }
    }
    file.delete();
  }
  
   public TestDoubleMatrix(String name)
   {
     super(name);
   }
 
   protected void setUp() throws Exception
   {
     super.setUp();
   }
 
   protected void tearDown() throws Exception
   {
     super.tearDown();
    delete(getTmpFile());
   }
 
   public void testTranspose() throws Exception
   {
     int numRows = 100;
     int numCols = 200;
     DoubleMatrix matrix = randomHashMapDoubleMatrix(numRows, 50, numCols, 150, 0.5);
     matrix = randomImmutableSparseDoubleMatrix(numRows, 50, numCols, 150, 0.5);
   }
   
   public void testTransposeMultiply() throws Exception
   {
     int numRows = 1000;
     int numCols = 200;
     DoubleMatrix matrix = randomHashMapDoubleMatrix(numRows, numRows, numCols, 150, 0.5);
     MapVector v = matrix.get(32);
     MapVector w = matrix.timesSquared(v);
     MapVector w2 = timesSquared(matrix, v);
     assertTrue(Math.abs(w2.norm() - w.norm()) < 10e-12);
     
   }
   
   private MapVector timesSquared(DoubleMatrix matrix, MapVector v)
   {
     MapVector w = matrix.times(v);
     MapVector w2 = new HashMapVector();
     for(Entry<Integer, MapVector> entry : matrix)
     {
       MapVector d_i = entry.getValue();
       int i = entry.getKey();
       for(IntDoublePair pair : d_i)
       {
         int alpha = pair.getInt();
         double d_i_alpha = pair.getDouble();
         w2.add(alpha, w.get(i) * d_i_alpha);
       }
     }
     return w2;
   }
   
   public void testMatrixSerialization() throws Exception
   {
     DoubleMatrix m = randomImmutableSparseDoubleMatrix(100, 95, 50, 20, 1.0);
     ByteArrayOutputStream os = new ByteArrayOutputStream();
     ObjectOutputStream oos = new ObjectOutputStream(os);
     oos.writeObject(m);
     byte[] objectBytes = os.toByteArray();
     ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(objectBytes));
     DoubleMatrix mOut = (DoubleMatrix)ois.readObject();
     assertTrue(m.equals(mOut));
   }
   
   public void testDiskBufferedDoubleMatrix() throws Exception
   {    
    File tmpDir = getTmpFile();
     if(tmpDir.isDirectory())
     {
       for(File f : tmpDir.listFiles())
         f.delete();
     }
     if(tmpDir.canWrite() && !tmpDir.delete()) throw new Exception(tmpDir + " exists, but I can't delete it");
     if(!tmpDir.exists() && !tmpDir.mkdir()) throw new Exception("Can't make directory");
     DoubleMatrix inMem = new HashMapDoubleMatrix(new HashMapVectorFactory());
     for(int i=0; i<10; i++)
     {
       DoubleMatrix m = randomImmutableSparseDoubleMatrix(100, 90, 50, 20, 1.0, i * 100);
       for(Entry<Integer, MapVector> entry : m)
         inMem.set(entry.getKey(), entry.getValue());
       DiskBufferedDoubleMatrix.persistChunk(tmpDir, m, true);
     }
     DoubleMatrix fromDisk = new DiskBufferedDoubleMatrix(tmpDir, 10);
     boolean equals = (fromDisk).equals(inMem);
     assertTrue(equals);
     
     MapVector v = null;
     while(v == null || v.norm() == 0)
       v = inMem.get((int)(Math.random() * inMem.numRows()));
     
     MapVector w = inMem.times(v);
     MapVector w2 = fromDisk.times(v);
     assertTrue(w.equals(w2));
     
     w = fromDisk.timesSquared(v);
    w2 = inMem.timesSquared(v);
     assertTrue(w.equals(w2));
   }
   
   /**
    * 
    * @param numRows
    * @param numNonZeroRows
    * @param numCols
    * @param numNonZeroEntries
    * @param absMeanValue
    * @return
    */
   public static DoubleMatrix randomHashMapDoubleMatrix(int numRows, int numNonZeroRows, int numCols, int numNonZeroEntries, double absMeanValue)
   {
     DoubleMatrix matrix = new HashMapDoubleMatrix(new HashMapVectorFactory());
     for(int i=0; i<numNonZeroRows; i++)
     {
       MapVector vector = new HashMapVector();
       for(int j=0; j<numNonZeroEntries; j++)
       {
         vector.set((int)(Math.random()*numCols), (float)(2*absMeanValue*Math.random()));
       }
       matrix.set((int)(Math.random()*numRows), vector);
     }
     return matrix;
   }
   
   /**
    * 
    * @param numRows
    * @param numNonZeroRows
    * @param numCols
    * @param numNonZeroEntries
    * @param absMeanValue
    * @return
    */
   public static DoubleMatrix randomImmutableSparseDoubleMatrix(int numRows, int numNonZeroRows, int numCols, int numNonZeroEntries, double absMeanValue)
   {
     return new ImmutableSparseDoubleMatrix(randomHashMapDoubleMatrix(numRows, numNonZeroRows, numCols, numNonZeroEntries, absMeanValue));
   }
   
   public static DoubleMatrix randomImmutableSparseDoubleMatrix(int numRows, int numNonZeroRows, int numCols, int numNonZeroEntries, double absMeanValue, int rowOffset)
   {
     DoubleMatrix m = randomHashMapDoubleMatrix(numRows, numNonZeroRows, numCols, numNonZeroEntries, absMeanValue);
     DoubleMatrix mOffset = new HashMapDoubleMatrix(new HashMapVectorFactory());
     for(Entry<Integer, MapVector> row : m)
     {
       mOffset.set(row.getKey() + rowOffset, row.getValue());
     }
     return new ImmutableSparseDoubleMatrix(mOffset);
   }
   
 }
