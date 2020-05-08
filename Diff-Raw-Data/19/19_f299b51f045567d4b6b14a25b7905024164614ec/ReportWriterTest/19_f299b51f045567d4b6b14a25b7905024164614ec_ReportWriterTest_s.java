 package analysis;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import analysis.data.Execution;
 import analysis.data.HadoopInformation;
 import analysis.data.HadoopMachineUsage;
 import analysis.data.MachineUsage;
 
 import commons.util.StringUtil;
 
 public class ReportWriterTest {
 
 	private static final String TEST_FILE_NAME = "result_test.csv";
 	private static final Long START_TIME_1 = 0L;
 	private static final Long END_TIME_1 = 0L;
 	private static final boolean DISCOMFORT_1 = false;
 	private ReportWriter writer;
 	private RandomAccessFile file;
 	
 	private static final String BENCHMARK_1 = "mr";
 	
 	private static final Double WRITE_ATTEMPTS_1 = 500.0;
 	private static final Double WRITE_ATTEMPTS_2 = WRITE_ATTEMPTS_1 + 100;
 	private static final Double WRITE_ATTEMPTS_3 = WRITE_ATTEMPTS_2 + 100;
 	private static final Double WRITE_ATTEMPTS_4 = WRITE_ATTEMPTS_3 + 100;
 	private static final Double WRITE_ATTEMPTS_5 = WRITE_ATTEMPTS_4 + 100;
 	
 	private static final Double WRITE_NUM_1 = 300.0;
 	private static final Double WRITE_NUM_2 = WRITE_NUM_1 + 100L;
 	private static final Double WRITE_NUM_3 = WRITE_NUM_2 + 100L;
 	private static final Double WRITE_NUM_4 = WRITE_NUM_3 + 100L;
 	private static final Double WRITE_NUM_5 = WRITE_NUM_4 + 100L;
 	
 	private static final Double READ_NUM_1 = 900.0;
 	private static final Double READ_NUM_2 = READ_NUM_1 + 100;
 	private static final Double READ_NUM_3 = READ_NUM_2 + 100;
 	private static final Double READ_NUM_4 = READ_NUM_3 + 100;
 	private static final Double READ_NUM_5 = READ_NUM_4 + 100;
 	
 	private static final Double READ_SECTORS_1 = 900.0;
 	private static final Double READ_SECTORS_2 = READ_SECTORS_1 + 100;
 	private static final Double READ_SECTORS_3 = READ_SECTORS_2 + 100;
 	private static final Double READ_SECTORS_4 = READ_SECTORS_3 + 100;
 	private static final Double READ_SECTORS_5 = READ_SECTORS_4 + 100;
 	
 	private static final Double MEMORY_USAGE_1 = 1300.0;
 	private static final Double MEMORY_USAGE_2 = MEMORY_USAGE_1 + 100;
 	private static final Double MEMORY_USAGE_3 = MEMORY_USAGE_2 + 100;
 	private static final Double MEMORY_USAGE_4 = MEMORY_USAGE_3 + 100;
 	private static final Double MEMORY_USAGE_5 = MEMORY_USAGE_4 + 100;
 	
 	private static final Double IDLE_CPU_1 = 0.0;
 	private static final Double IDLE_CPU_2 = IDLE_CPU_1 + 20;
 	private static final Double IDLE_CPU_3 = IDLE_CPU_2 + 20;
 	private static final Double IDLE_CPU_4 = IDLE_CPU_3 + 20;
 	private static final Double IDLE_CPU_5 = IDLE_CPU_4 + 20;
 	
 	private static final Double USER_CPU_1 = 0.0;
 	private static final Double USER_CPU_2 = USER_CPU_1 + 20;
 	private static final Double USER_CPU_3 = USER_CPU_2 + 20;
 	private static final Double USER_CPU_4 = USER_CPU_3 + 20;
 	private static final Double USER_CPU_5 = USER_CPU_4 + 20;
 	
 	
 	private static final Double HADOOP_CPU_1 = 0.0;
 	private static final Double HADOOP_CPU_2 = HADOOP_CPU_1 + 20;
 	private static final Double HADOOP_CPU_3 = HADOOP_CPU_2 + 20;
 	private static final Double HADOOP_CPU_4 = HADOOP_CPU_3 + 20;
 	private static final Double HADOOP_CPU_5 = HADOOP_CPU_4 + 20;
 	
 	private static final Double HADOOP_MEMORY_1 = 23.0;
 	private static final Double HADOOP_MEMORY_2 = HADOOP_MEMORY_1 + 20;
 	private static final Double HADOOP_MEMORY_3 = HADOOP_MEMORY_2 + 20;
 	private static final Double HADOOP_MEMORY_4 = HADOOP_MEMORY_3 + 20;
 	private static final Double HADOOP_MEMORY_5 = HADOOP_MEMORY_4 + 20;
 	
 	private static long time1 = 1000;
 	private static long time2 = time1 + 1;
 	private static long time3 = time2 + 1;
 	private static long time4 = time3 + 1;
 	private static long time5 = time4 + 1;
 	
 	@Before
 	public void setUp() throws Exception {
 		File testFile = new File(TEST_FILE_NAME);
 		testFile.createNewFile();
 		
 		writer = new ReportWriter(TEST_FILE_NAME);
 		
 		file = new RandomAccessFile(TEST_FILE_NAME, "r");
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		new File(TEST_FILE_NAME).delete();
 	}
 	
 	@Test
 	public void testWrite() throws IOException {
 		Execution execution1 = new Execution(START_TIME_1, END_TIME_1);
 		Map<Long, Double> idleCPU = new HashMap<Long, Double>();
 		idleCPU.put(time1, IDLE_CPU_1);
 		idleCPU.put(time2, IDLE_CPU_2);
 		idleCPU.put(time3, IDLE_CPU_3);
 		idleCPU.put(time4, IDLE_CPU_4);
 		idleCPU.put(time5, IDLE_CPU_5);
 		
 		Map<Long, Double> userCPU = new HashMap<Long, Double>();
 		userCPU.put(time1, USER_CPU_1);
 		userCPU.put(time2, USER_CPU_2);
 		userCPU.put(time3, USER_CPU_3);
 		userCPU.put(time4, USER_CPU_4);
 		userCPU.put(time5, USER_CPU_5);
 		
 		Map<Long, Double> memoryUsage = new HashMap<Long, Double>();
 		memoryUsage.put(time1, MEMORY_USAGE_1);
 		memoryUsage.put(time2, MEMORY_USAGE_2);
 		memoryUsage.put(time3, MEMORY_USAGE_3);
 		memoryUsage.put(time4, MEMORY_USAGE_4);
 		memoryUsage.put(time5, MEMORY_USAGE_5);
 		
 		Map<Long, Double> readNumber = new HashMap<Long, Double>();
 		readNumber.put(time1, READ_NUM_1);
 		readNumber.put(time2, READ_NUM_2);
 		readNumber.put(time3, READ_NUM_3);
 		readNumber.put(time4, READ_NUM_4);
 		readNumber.put(time5, READ_NUM_5);
 		
 		Map<Long, Double> readSectors = new HashMap<Long, Double>();
 		readSectors.put(time1, READ_SECTORS_1);
 		readSectors.put(time2, READ_SECTORS_2);
 		readSectors.put(time3, READ_SECTORS_3);
 		readSectors.put(time4, READ_SECTORS_4);
 		readSectors.put(time5, READ_SECTORS_5);
 		
 		Map<Long, Double> writeNumber = new HashMap<Long, Double>();
 		writeNumber.put(time1, WRITE_NUM_1);
 		writeNumber.put(time2, WRITE_NUM_2);
 		writeNumber.put(time3, WRITE_NUM_3);
 		writeNumber.put(time4, WRITE_NUM_4);
 		writeNumber.put(time5, WRITE_NUM_5);
 		
 		Map<Long, Double> writeAttemptNumber = new HashMap<Long, Double>();
 		writeAttemptNumber.put(time1, WRITE_ATTEMPTS_1);
 		writeAttemptNumber.put(time2, WRITE_ATTEMPTS_2);
 		writeAttemptNumber.put(time3, WRITE_ATTEMPTS_3);
 		writeAttemptNumber.put(time4, WRITE_ATTEMPTS_4);
 		writeAttemptNumber.put(time5, WRITE_ATTEMPTS_5);
 		
 		MachineUsage machineUsage = new MachineUsage(idleCPU, userCPU, memoryUsage, readNumber, readSectors, writeNumber, writeAttemptNumber);
 		
 		Map<Long, Double> hadoopCPUUsage = new HashMap<Long, Double>();
 		hadoopCPUUsage.put(time1, HADOOP_CPU_1);
 		hadoopCPUUsage.put(time2, HADOOP_CPU_2);
 		hadoopCPUUsage.put(time3, HADOOP_CPU_3);
 		hadoopCPUUsage.put(time4, HADOOP_CPU_4);
 		hadoopCPUUsage.put(time5, HADOOP_CPU_5);
 		
 		Map<Long, Double> hadoopMemoryUsage = new HashMap<Long, Double>();
 		hadoopMemoryUsage.put(time1, HADOOP_MEMORY_1);
 		hadoopMemoryUsage.put(time2, HADOOP_MEMORY_2);
 		hadoopMemoryUsage.put(time3, HADOOP_MEMORY_3);
 		hadoopMemoryUsage.put(time4, HADOOP_MEMORY_4);
 		hadoopMemoryUsage.put(time5, HADOOP_MEMORY_5);
 		
 		HadoopMachineUsage hadoopMachineUsage = new HadoopMachineUsage(hadoopCPUUsage, hadoopMemoryUsage);
 		
 		Map<Long, String> benchmarks = new HashMap<Long, String>();
 		benchmarks.put(time1, BENCHMARK_1);
 		
 		HadoopInformation hadoopInfo = new HadoopInformation(benchmarks);
 		writer.write(execution1, DISCOMFORT_1, machineUsage, hadoopMachineUsage , hadoopInfo);
 		
 		rewindFile(TEST_FILE_NAME);
 		getLineFromFile(TEST_FILE_NAME);
 		String line = getLineFromFile(TEST_FILE_NAME);
 		
		assertEquals(START_TIME_1, new Long(line.split(",")[0].trim()));
		assertEquals(END_TIME_1, new Long(line.split(",")[1].trim()));
		assertEquals(DISCOMFORT_1, new Boolean(line.split(",")[2].trim()));
		assertEquals(BENCHMARK_1, line.split(",")[3].trim());
		assertEquals(StringUtil.concat("-", new ArrayList<Object>(hadoopCPUUsage.values())), 
									line.split(",")[4].trim());
		assertEquals(StringUtil.concat("-", new ArrayList<Object>(hadoopMemoryUsage.values())), 
									line.split(",")[5].trim());
 	}
 
 	private void rewindFile(String testFileName) throws IOException {
 		file.seek(0);
 	}
 	
 	private String getLineFromFile(String testFileName) throws IOException {
 		String line = file.readLine();
 		return line;
 	}
 }
