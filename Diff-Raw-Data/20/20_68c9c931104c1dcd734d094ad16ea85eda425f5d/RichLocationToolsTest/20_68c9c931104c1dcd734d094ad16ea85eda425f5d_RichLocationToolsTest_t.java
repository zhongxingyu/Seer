 package org.biojavax.bio.seq;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import junit.framework.TestCase;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.RangeLocation;
 
 /**
  *
  * @author George Waldon
  */
 public class RichLocationToolsTest extends TestCase {
 
     public RichLocationToolsTest(String testName) {
         super(testName);
     }
 
     @Override
     protected void setUp() throws Exception {
 
     }
 
     @Override
     protected void tearDown() throws Exception {
 
     }
 
     /** Test RichLocation.Tools.contruct //partial
      *
      */
     public void testconstruct() {
         //case members.size() == 1
         Collection<Location> members = new ArrayList<Location>();
         members.add(new RangeLocation(1,10));
         assertTrue(RichLocation.Tools.construct(members)instanceof RichLocation);
     }
 
     /** Test RichLocation.Tools.merge
      *
      */
     public void testmerge() {
         Collection<Location> members = new ArrayList<Location>();
         Location loc;
 
         // Bipartite
         members.add(new RangeLocation(1,10));
         members.add(new RangeLocation(10,20));
         Collection<Location> result = RichLocation.Tools.merge(members);
         assert (result.size()==1);
         loc =  result.toArray(new Location[0])[0];
         assert (loc.isContiguous());
         assert (loc.getMin()==1);
         assert (loc.getMax()==20);
 
         members.clear();
         members.add(new SimpleRichLocation(new SimplePosition(1),new SimplePosition(10),1));
         members.add(new SimpleRichLocation(new SimplePosition(10),new SimplePosition(20),1));
         result = RichLocation.Tools.merge(members);
         assert (result.size()==1);
         loc =  result.toArray(new Location[0])[0];
         assert (loc.isContiguous());
         assert (loc.getMin()==1);
         assert (loc.getMax()==20);
 
         // Tripartite
         members.clear();
         members.add(new RangeLocation(1,10));
         members.add(new RangeLocation(20,30));
         members.add(new RangeLocation(1,30));
         result = RichLocation.Tools.merge(members);
         assert (result.size()==1);
         loc =  result.toArray(new Location[0])[0];
         assert (loc.isContiguous());
         assert (loc.getMin()==1);
         assert (loc.getMax()==30);
 
         members.clear();
         members.add(new SimpleRichLocation(new SimplePosition(1),new SimplePosition(10),1));
         members.add(new SimpleRichLocation(new SimplePosition(20),new SimplePosition(30),1));
         members.add(new SimpleRichLocation(new SimplePosition(1),new SimplePosition(30),1));
         result = RichLocation.Tools.merge(members);
         assert (result.size()==1);
         loc =  result.toArray(new Location[0])[0];
         assert (loc.isContiguous());
         assert (loc.getMin()==1);
         assert (loc.getMax()==30);
     }

    /** Test modulateCircularLocation at junction: method must return valid
     * sequence position >0.
     */
    public void testmodulatecircularLocation() {
        int seqlen = 10000; //Dummy
        int start = seqlen;
        int end = 1000; // Dummy

        int[] res = RichLocation.Tools.modulateCircularLocation(start,end,seqlen);
        assert (res[0]==seqlen);
        assert (res[1]==seqlen+end);
    }
 }
