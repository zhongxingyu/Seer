 package bioUtils;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
 import java.text.DecimalFormat;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
 
 public class DNAUtil {
 
 //	public static String singleAnalyze(String dnaFile, int startIndex,
 //			int stopIndex, int winShift, int winSize) {
 //
 //		Map<Integer, Result> results = new TreeMap<Integer, Result>();
 //		File file = new File(dnaFile);
 //
 //		// Check and initialize all variables
 //		int curIndex = startIndex < 0 ? 1 : startIndex;
 //		int lastIndex = stopIndex < 0 ? -1 : stopIndex;
 //		int size = winSize < 0 ? 1000 : winSize;
 //		int shift = winShift < 0 ? size : winShift;
 //
 //		BufferedReader reader = null;
 //		try {
 //			FileReader input = new FileReader(file);
 //			reader = new BufferedReader(input);
 //
 //			// Skip the first line
 //			reader.readLine();
 //			// Get to the start index
 //			for (int i = 1; i < curIndex;) {
 //				int res = reader.read();
 //				if (res > 40) {
 //					i++;
 //				}
 //				if (res == -1) {
 //					input.close();
 //					return "";
 //				}
 //			}
 //
 //			boolean end = false;
 //			boolean first = true;
 //			int runningIndex = curIndex;
 //
 //			// While we still have characters to read
 //			while (!end) {
 //
 //				// reset the reader
 //				if (!first) {
 //					try {
 //						reader.reset();
 //					} catch (Exception e) {
 //						// Don't care - ignore this
 //					}
 //
 //					// seek - not on the first iteration
 //					for (int i = 0; i < shift;) {
 //						int res = reader.read();
 //						if (res > 40) {
 //							i++;
 //						}
 //						if (res == -1) {
 //							end = true;
 //							break;
 //						}
 //					}
 //				}
 //
 //				first = false;
 //
 //				// mark
 //				StringBuilder sequence = new StringBuilder();
 //				reader.mark(size + 1);
 //
 //				int i;
 //				// read the next winsize sequence
 //				for (i = 0; i < size && runningIndex + i < lastIndex;) {
 //					int res = reader.read();
 //					if (res > 40) {
 //						sequence.append((char) res);
 //						i++;
 //					}
 //					if (res == -1) {
 //						end = true;
 //						break;
 //					}
 //				}
 //
 //				if (runningIndex + shift <= lastIndex) {
 //					Result result = DNAUtil.findGcContentForSegment(sequence
 //							.toString());
 //
 //					results.put(runningIndex, result);
 //				}
 //
 //				runningIndex += shift;
 //
 //				if (runningIndex >= lastIndex) {
 //					end = true;
 //				}
 //			}
 //
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //
 //		DecimalFormat df = new DecimalFormat("#.##");
 //
 //		// And build the result string
 //		String rtn = "";
 //
 //		if (winShift > 0 || winSize > 0) {
 //			rtn = "Range" + " ,\t" + "min" + " ,\t" + "max\n";
 //
 //			// Sort the results for better display
 //			Map<Integer, Result> sortedRes = new TreeMap<Integer, Result>(
 //					results);
 //
 //			for (Integer a : sortedRes.keySet()) {
 //				rtn += a + " to " + (a + size - 1) + " ,\t"
 //						+ df.format(results.get(a).min) + " ,\t"
 //						+ df.format(results.get(a).max) + "\n";
 //			}
 //		} else {
 //			double minTot = 0;
 //			double maxTot = 0;
 //
 //			for (Integer a : results.keySet()) {
 //				minTot += results.get(a).min;
 //				maxTot += results.get(a).max;
 //			}
 //			rtn = "Total min ,\tTotal max\n";
 //			rtn += df.format(minTot / results.keySet().size()) + " ,\t"
 //					+ df.format(maxTot / results.keySet().size());
 //		}
 //
 //		return rtn;
 //	}
 
 	public static String analyze(String dnaFile, int startIndex, int stopIndex,
 			int winShift, int winSize) throws Exception {
 
 		//final ExecutorService service;
 
 		Map<Integer, Result> results = new ConcurrentHashMap<Integer, Result>();
 		//File file = new File(dnaFile);
 
 		// Create the thread pool
 		//service = Executors.newFixedThreadPool(100);
 		
 		String dnaSequence = DNAFileReader.readFASTA(dnaFile);
 
 		// Check and initialize all variables
 		int curIndex = startIndex < 0 ? 1 : startIndex;
 		int lastIndex = stopIndex < 0 ? dnaSequence.length() : stopIndex;
 		int size = winSize < 0 ? 1000 : winSize;
 		int shift = winShift < 0 ? size : winShift;
 		int endPos = 0;
 		
 		while(curIndex <= lastIndex)
 		{
 			
 			endPos = (curIndex + size <= lastIndex) ? (curIndex+size-1) : lastIndex;
 			
 			Result res = DNAUtil.findGcContentForSegment(dnaSequence.substring(curIndex-1, endPos));
 			res.start = curIndex;
 			res.stop = endPos;
 			if(!res.max.isNaN() && !res.min.isNaN()) {
 				results.put(curIndex, res);
 			}
 			curIndex += shift;
 		}
 
 		
 		// Kick off the threads
 		/*
 		for (int i = 0; i < size / shift; i++) {
 			service.execute(new CalcThread(file, curIndex, lastIndex, size,
 					results));
 			curIndex += shift;
 		}
 		*/
 
 		// Wait until all threads terminate (max wait of 1 minute - may need to
 		// make this longer)
 		//service.shutdown();
 		//service.awaitTermination(1, TimeUnit.MINUTES);
 
 		DecimalFormat df = new DecimalFormat("#.##");
 
 		// And build the result string
 		String rtn = "";
 
 		if (winShift > 0 || winSize > 0) {
 			rtn = "Range" + " ,\t" + "min" + " ,\t" + "max\n";
 
 			// Sort the results for better display
 			Map<Integer, Result> sortedRes = new TreeMap<Integer, Result>(
 					results);
 
 			for (Integer a : sortedRes.keySet()) {
 				rtn += results.get(a).start + " to " + results.get(a).stop + " ,\t" //a + " to " + (a + size - 1) + " ,\t"
 						+ df.format(results.get(a).min) + " ,\t"
 						+ df.format(results.get(a).max) + "\n";
 			}
 		} else {
 			double minTot = 0;
 			double maxTot = 0;
 
 			for (Integer a : results.keySet()) {
 				minTot += results.get(a).min;
 				maxTot += results.get(a).max;
 			}
 			rtn = "Total min ,\tTotal max\n";
 			rtn += df.format(minTot / results.keySet().size()) + " ,\t"
 					+ df.format(maxTot / results.keySet().size());
 		}
 
 		return rtn;
 	}
 
 	public static Result findGcContentForSegment(String segment) {
 
 		double total = 0;
 		int gc = 0;
 		int n = 0;
 
 		for (int i = 0; i < segment.length(); i++) {
 			total++;
 			if (segment.charAt(i) == 'G' || segment.charAt(i) == 'C') {
 				gc++;
 			} else if (segment.charAt(i) == 'N') {
 				n++;
 			}
 		}
 		
 		System.out.println("Total: " + total);
 
 		Result rtn = new Result();
 		rtn.max = 100 * (double) (gc + n) / total;
 		rtn.min = 100 * (double) gc / total;
 
 		return rtn;
 	}
 }
