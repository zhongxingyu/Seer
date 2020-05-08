 package au.gov.ga.conn4d.test.utils;
 
 import java.io.IOException;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import ucar.nc2.NetcdfFile;
 import ucar.nc2.Variable;
 import au.gov.ga.conn4d.utils.IndexLookup_Cell;
 
 public class IndexLookup_CellTest{
 	
 	NetcdfFile ncFile;
 	IndexLookup_Cell loc;
 	Variable var;
 	
 	@Test
 	public void testLocate() {
 		
 		try {
			ncFile = NetcdfFile.open("C://Temp//xmap.nc");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		var = ncFile.findVariable("Longitude");
 		loc = new IndexLookup_Cell(var);
 		loc.setMinval(-1.005);
 		loc.setMaxval(1.005);
 		Assert.assertEquals(-1,loc.lookup(-8));
 		Assert.assertEquals(-1,loc.lookup(-1.005));
 		Assert.assertEquals(0,loc.lookup(-1.004));
 		Assert.assertEquals(0,loc.lookup(-1.003));
 		Assert.assertEquals(0,loc.lookup(-1.0));
 		Assert.assertEquals(0,loc.lookup(-0.999));
 		Assert.assertEquals(0,loc.lookup(-0.996));
 		Assert.assertEquals(1,loc.lookup(-0.995));
 		Assert.assertEquals(1,loc.lookup(-0.994));
 		Assert.assertEquals(1,loc.lookup(-0.99));
 		Assert.assertEquals(100,loc.lookup(0));
 		Assert.assertEquals(200,loc.lookup(0.999));
 		Assert.assertEquals(200,loc.lookup(1));
 		Assert.assertEquals(201,loc.lookup(1.05));
 		Assert.assertEquals(201,loc.lookup(1.07));
 		Assert.assertEquals(201,loc.lookup(18));
 	}
 }
