 package au.org.intersect.samifier.domain;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 
 public class GffOutputterUnitTest
 {
     public GffOutputterUnitTest()
     {
     }
 
     @Test
     public void testGffOutputWithGenomeFileWithoutExtension()
     {
         ProteinLocation proteinLocation = new ProteinLocation("a",1,2,"+", "1");
         GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test");
         String lineFeed = System.getProperty("line.separator");
         String firstLine = gffOutputter.getOutput().split(lineFeed)[0];
         String firstToken = firstLine.split("\t")[0];
         assertEquals("test", firstToken);
     }
 
     @Test
     public void testGffOutputWithGenomeFileWithExtension()
     {
         ProteinLocation proteinLocation = new ProteinLocation("a",1,2,"+", "1");
         GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test.txt");
         String lineFeed = System.getProperty("line.separator");
         String firstLine = gffOutputter.getOutput().split(lineFeed)[0];
         String firstToken = firstLine.split("\t")[0];
         assertEquals("test", firstToken);
     }
 
     @Test
     public void testGffWholeOutput()
     {
         ProteinLocation proteinLocation = new ProteinLocation("glimmer_name",1,2,"+", "1");
         GffOutputter gffOutputter = new GffOutputter(proteinLocation,"test");
         String lineFeed = System.getProperty("line.separator");
 
         StringBuffer expectedOutput = new StringBuffer();
        expectedOutput.append("test\tGlimmer\tgene\t1\t2\t0\t+\t0\tName=glimmer_name;ID=glimmer_name;");
         expectedOutput.append(lineFeed);
        expectedOutput.append("test\tGlimmer\tCDS\t1\t2\t0\t+\t0\tName=glimmer_name;Parent=glimmer_name;");
         expectedOutput.append(lineFeed);
         assertEquals(expectedOutput.toString(), gffOutputter.getOutput());
     }
 
 
 }
