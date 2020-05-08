 package org.put.hd.dmarf.jocl;
 
 import org.jocl.samples.JOCLDeviceQuery;
 import org.jocl.samples.JOCLSample;
import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class JOCLSamplesTest {
 
	@Before
	public void set_up(){
		// TODO: those test are problematic in log runing with other test
	}
	
 	@Test
 	public void deviceQueryTest() {
 		JOCLDeviceQuery.runJOCLDeviceQuery();
 	}
 
 	@Test
 	public void OpenCL_1_0_Test() {
 		JOCLSample.runJOCL_1_0_Sample();
 	}
 
 	@Test
 	@Ignore("OpenCL 1.1 not yet supported widely")
 	public void OpenCL_1_1_CPU_Test() {
 		//JOCLSample_1_1.runJOCL_1_1_Sample();
 	}
 }
