 package org.jdryad.persistence.file.test;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jdryad.dag.FunctionInput;
 import org.jdryad.dag.FunctionOutput;
 import org.jdryad.dag.IOFactory;
 import org.jdryad.dag.IOKey;
 import org.jdryad.dag.Record;
 import org.jdryad.dag.IOKey.SourceType;
 import org.jdryad.persistence.file.FileIOFactory;
 import org.testng.AssertJUnit;
 import org.testng.annotations.Test;
 
 /**
  * A simple test for testing the persistence to the files.
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class FilePersistenceTest
 {
     private static final int TOTAL_RECORDS = 10;
 
     private List<TestFileRecord> myRecords;
 
     /** A simple record for testing the way we write to files */
     private static class TestFileRecord implements Record
     {
         private int myRecordID;
 
         /** CTOR */
         public TestFileRecord()
         {
             this(-1);
         }
 
         /** CTOR */
         public TestFileRecord(int recordID)
         {
             myRecordID = recordID;
         }
 
         /**
          * Returns the value of recordID
          */
         public int getRecordID()
         {
             return myRecordID;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public int hashCode()
         {
             final int prime = 31;
             int result = 1;
             result = prime * result + myRecordID;
             return result;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public boolean equals(Object obj)
         {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             TestFileRecord other = (TestFileRecord) obj;
             if (myRecordID != other.myRecordID)
                 return false;
             return true;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void readExternal(ObjectInput in) throws IOException,
             ClassNotFoundException
         {
             myRecordID = in.readInt();
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void writeExternal(ObjectOutput out) throws IOException
         {
             out.writeInt(myRecordID);
         }
     }
 
     /**
      * CTOR
      */
     public FilePersistenceTest()
     {
         myRecords = new ArrayList<TestFileRecord>();
         for (int i = 0; i < TOTAL_RECORDS; i++) {
             myRecords.add(new TestFileRecord(i));
         }
     }
 
     /** Factory method for making a key to the file */
     private IOKey makeKey()
     {
         return new IOKey(SourceType.FILE, "C:\\temp\\test1.txt");
     }
 
     /** Factory method for making a IOFactory for reading data */
     public IOFactory makeIOFactory()
     {
         return new FileIOFactory();
     }
 
     @Test(testName="writeTest", groups={"persistence"})
     public void testWrite()
     {
         FunctionOutput output = makeIOFactory().makeOutput(makeKey());
         for (Record r : myRecords) {
             output.write(r);
         }
         output.done();
     }
 
     @Test(testName="readTest",
           dependsOnMethods={"testWrite"},
           groups={"persistence"})
     public void testRead()
     {
         FunctionInput in = makeIOFactory().makeInput(makeKey());
         Iterator<Record> itr = in.getIterator();
         ArrayList<Record> readData = new ArrayList<Record>(TOTAL_RECORDS);
         while (itr.hasNext()) {
             readData.add(itr.next());
         }
        System.out.println(readData);
         AssertJUnit.assertEquals(TOTAL_RECORDS, readData.size());
         AssertJUnit.assertEquals(readData, myRecords);
     }
 }
