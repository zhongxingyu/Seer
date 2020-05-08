 package com.knownstylenolife.hadoop.workshop.wordcount.tool;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
 import org.apache.hadoop.mrunit.types.Pair;
 import org.apache.hadoop.util.ToolRunner;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Resources;
 import com.knownstylenolife.hadoop.workshop.unit.tool.MapReduceLocalTestCaseBase;
 import com.knownstylenolife.hadoop.workshop.unit.util.DfsTestUtil;
 import com.knownstylenolife.hadoop.workshop.unit.util.PairUtil;
 import com.knownstylenolife.hadoop.workshop.wordcount.mapreduce.WordCountMapper;
 import com.knownstylenolife.hadoop.workshop.wordcount.mapreduce.WordCountSumReducer;
 
 public class WordCountSimpleMainToolTest extends MapReduceLocalTestCaseBase {
 	
 	private static Log LOG = LogFactory.getLog(WordCountSimpleMainToolTest.class);
 	
 	private static final String MR_LOG_LEVEL = org.apache.log4j.Level.WARN.toString();
 
 	private MapReduceDriver<LongWritable, Text, Text, LongWritable, Text, LongWritable> mapreduceDriver;
 	private WordCountSimpleToolMain tool;
 	
 	private String inputFilename = "hadoop-wikipedia.txt";
 	private URL inputURL;
 	
 	private String expectedOutputFilePath = "WordCountSimpleMainToolTest/testRun_expected/part-r-00000";
 	private URL expectedOutputFileURL;
 	
 	private Matcher matcher;
 	
 	@Before
 	public void setUp() throws Exception {
 		mapreduceDriver = new MapReduceDriver<LongWritable, Text, Text, LongWritable, Text, LongWritable>(new WordCountMapper(), new WordCountSumReducer());
 		tool = new WordCountSimpleToolMain();
 		tool.setConf(getConfiguration());
 		inputURL = Resources.getResource(getClass(), inputFilename);
 		expectedOutputFileURL = Resources.getResource(getClass(), expectedOutputFilePath);
 		matcher = Pattern.compile(WordCountMapper.WORDS_REGEX).matcher("");
 	}
 	
 	/**
 	 * Test mapreduce with MapReduceDriver.
 	 * 
 	 * @throws IOException
 	 */
 	@Test
 	public void testMapReduce() throws IOException {
 		mapreduceDriver.addInput(
 			new LongWritable(0), 
 			new Text(Resources.toString(inputURL, Charsets.UTF_8)));
 		
 		List<Pair<Text, LongWritable>> reduceOutputs = mapreduceDriver.run();
 		List<String> actualLineResultList = PairUtil.toStrings(reduceOutputs);
 		
 		List<String> expectedLineResultList = Resources.readLines(expectedOutputFileURL, Charsets.UTF_8);
 		assertEquals(expectedLineResultList.size(), actualLineResultList.size());
 		int expectedLineResultListSize = expectedLineResultList.size();
 	    for(int i = 0; i < expectedLineResultListSize; i++) {
 	    	assertTrue("Invalid match key is found. pair = " + reduceOutputs.get(i).toString(), 
 	    		matcher.reset(reduceOutputs.get(i).getFirst().toString()).matches());
 			assertEquals("Does not match line!! line = " + (i + 1) + ", expected = " + expectedLineResultList.get(i) + ", actual = " + actualLineResultList.get(i), 
 				expectedLineResultList.get(i), actualLineResultList.get(i));
 		}
 	}
 	
 	/**
 	 * test with standalone mode
 	 * @throws Exception
 	 */
 	@Test
 	public void testRun() throws Exception {
 		prepareJob(new File(inputURL.toURI()));
 		assertEquals(0, 
 			ToolRunner.run(
 				tool, 
 				new String[] { 
 					getInputDir().toString(), 
 					getOutputDir().toString(), 
 					MR_LOG_LEVEL
 		}));
 
 		Path[] actualOutputFiles = DfsTestUtil.getOutputFiles(getOutputDir(), getFileSystem());
 		assertEquals(1, actualOutputFiles.length);
 		assertOutputFile(actualOutputFiles[0]);
 	}
 	
 	private void assertOutputFile(Path actualOutputFile) throws IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(getFileSystem().open(actualOutputFile)));
 
 		List<String> expectedLineResultList = Resources.readLines(expectedOutputFileURL, Charsets.UTF_8);
 		int expectedLineResultListSize = expectedLineResultList.size();
 		String actualLine;
 		for(int i = 0; i < expectedLineResultListSize; i++) {
 			assertNotNull("Actual file is ended", actualLine = br.readLine());
 			assertEquals("Does not match line!! line = " + (i + 1) + ", expected = " + expectedLineResultList.get(i) + ", actual = " + actualLine, 
 				expectedLineResultList.get(i), actualLine);
 		}
		assertNull("actual file is not ended yet", br.readLine());
 		br.close();
 	}
 }
