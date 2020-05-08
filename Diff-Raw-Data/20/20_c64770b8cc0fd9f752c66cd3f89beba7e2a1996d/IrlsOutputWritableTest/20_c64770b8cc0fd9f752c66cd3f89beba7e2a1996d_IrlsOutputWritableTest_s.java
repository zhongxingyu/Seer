 package uk.ac.ebi.fgpt.hadoopUtils.microarray.data;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Writable;
 import org.apache.mahout.math.DenseVector;
 import org.apache.mahout.math.Vector;
 import org.junit.Before;
 import org.junit.Test;
 

public final class IrlsOutputWritableTest extends org.apache.mahout.math.MahoutTestCase {
   private IrlsOutputWritable irlsOutputWritable;
   private IrlsOutput mockIrlsOutput;
   
   @Before
   public void setUp() throws Exception {
     
     mockIrlsOutput = new IrlsOutput();
     
     Vector vector0 = new DenseVector(3);
     vector0.set(0, 1);
     vector0.set(1, 2);
     vector0.set(2, 3);
     Vector vector1 = new DenseVector(3);
     vector1.set(0, 7);
     vector1.set(1, 8);
     vector1.set(2, 9);
     
     Vector[] mockarray = new Vector[2];
     mockarray[0] = vector0;
     mockarray[1] = vector1;
     
     mockIrlsOutput.setArrayOfWeightVectors(mockarray);
     mockIrlsOutput.setVectorOfEstimates(vector0);
     mockIrlsOutput.setProbesetName("mock_name");
     
     irlsOutputWritable = new IrlsOutputWritable(mockIrlsOutput);
   }
   
   @Test
   public void testIrlsOutputWritable() {
     assertNotNull(irlsOutputWritable);
   }
   
   @Test
   public void testGet() {
     assertEquals("Probeset Object Changed?", mockIrlsOutput, irlsOutputWritable.get());
   }
   
   @Test
   public void testReadAndWrite() throws IOException {
     IrlsOutputWritable irlsOutputWritable2 = new IrlsOutputWritable();
     writeAndRead(irlsOutputWritable, irlsOutputWritable2);
     IrlsOutput irlsOutput2 = irlsOutputWritable2.get();
     
     assertEquals(mockIrlsOutput.getProbesetName(), irlsOutput2.getProbesetName());
     
   }
   
   @Test
   public void testWriteToHDFS() throws IOException {
     Configuration conf = new Configuration();
     Path probePath = new Path(getTestTempDir().toString(), mockIrlsOutput.getProbesetName());
     IrlsOutputWritable.writeToPath(conf, probePath, mockIrlsOutput);
     assertTrue(FileSystem.get(conf).exists(probePath));
     
   }
   
   private static void writeAndRead(Writable toWrite, Writable toRead) throws IOException {
     ByteArrayOutputStream baos = new ByteArrayOutputStream();
     DataOutputStream dos = new DataOutputStream(baos);
     try {
       toWrite.write(dos);
     } finally {
       dos.close();
     }
     
     ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
     DataInputStream dis = new DataInputStream(bais);
     try {
       toRead.readFields(dis);
     } finally {
       dis.close();
     }
   }
 }
