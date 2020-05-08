 package sim.storage.cli;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import sim.Request;
 
 @RunWith(JUnit4.class)
 public class WorkloadReaderTest {
 
 	@Test
 	public void getInitialDataTest() {
 		String filePath = "test/sim/storage/cli/workload";
 		WorkloadReader wReader = new WorkloadReader(filePath);
 		InitialDataInfo initInfo = wReader.getInitialDataInfo();
 		assertThat(initInfo.getNumberOfFiles(), is(10));
 		assertThat(initInfo.getLowerBound(), is(1));
 		assertThat(initInfo.getUpperBound(), is(5));
 	}
 
 	@Test
 	public void workloadReadTest() {
 		String filePath = "test/sim/storage/cli/workload";
 		WorkloadReader wReader = new WorkloadReader(filePath);
 		wReader.getInitialDataInfo();
 
		Request[] requests = wReader.getRequests(1);
		assertThat(requests.length, is(1));

		requests = wReader.getRequests(0);
		assertThat(requests.length, is(0));

		requests = wReader.getRequests(5);
		assertThat(requests.length, is(2));
 	}
 
 }
