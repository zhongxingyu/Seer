 package com.fhx.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 import org.marketcetera.marketdata.interactivebrokers.LatestMarketData;
 import org.rosuda.REngine.REXPDouble;
 import org.rosuda.REngine.REXPInteger;
 import org.rosuda.REngine.REXPString;
 import org.rosuda.REngine.RList;
 import org.rosuda.REngine.Rserve.RConnection;
 
 public class StatStreamUtil {
 	
 	private static Logger log = Logger.getLogger(StatStreamUtil.class);
 	
 	protected static Properties config;
 	public static boolean launchRserve(String cmd) {
 		boolean debug = Boolean.parseBoolean(config.getProperty("R_DEBUG","false"));
 		
 		return launchRserve(cmd, "--no-save --slave","--no-save --slave",debug); 
 	}
 	
 	/* checks whether Rserve is running and if that's not the case it attempts to start it using the defaults for the platform where it is run on. 
 	 * This method is meant to be set-and-forget and cover most default setups. 
 	 * For special setups you may get more control over R with <<code>launchRserve</code> instead. 
 	 */
 	public static boolean checkLocalRserve() {
 		if (isRserveRunning()) return true;
 		String osname = System.getProperty("os.name");
 		if (osname != null && osname.length() >= 7 && osname.substring(0,7).equals("Windows")) {
 			log.info("Windows: query registry to find where R is installed ...");
 			String installPath = null;
 			try {
 				Process rp = Runtime.getRuntime().exec("reg query HKLM\\Software\\R-core\\R");
 				StreamHog regHog = new StreamHog(rp.getInputStream(), true);
 				rp.waitFor();
 				regHog.join();
 				installPath = regHog.getInstallPath();
 			} catch (Exception rge) {
 				log.info("ERROR: unable to run REG to find the location of R: "+rge);
 				return false;
 			}
 			if (installPath == null) {
 				log.info("ERROR: canot find path to R. Make sure reg is available and R was installed with registry settings.");
 				//return false;
 			}
 			
 			log.info("Setting R installPath to: (C:\\Program Files (x86)\\R\\R-2.10.1) George's tactical fix ");
 			installPath = "C:\\Program Files (x86)\\R\\R-2.10.1";
 			
 			return launchRserve(installPath+"\\bin\\R.exe");
 		}
 		return (launchRserve("R") || /* try some common unix locations of R */
 			((new File("/Library/Frameworks/R.framework/Resources/bin/R")).exists() && launchRserve("/Library/Frameworks/R.framework/Resources/bin/R")) ||
 			((new File("/usr/local/lib/R/bin/R")).exists() && launchRserve("/usr/local/lib/R/bin/R")) ||
 			((new File("/usr/lib/R/bin/R")).exists() && launchRserve("/usr/lib/R/bin/R")) ||
 			((new File("/usr/local/bin/R")).exists() && launchRserve("/usr/local/bin/R")) ||
 			((new File("/sw/bin/R")).exists() && launchRserve("/sw/bin/R")) ||
 			((new File("/usr/common/bin/R")).exists() && launchRserve("/usr/common/bin/R")) ||
 			((new File("/opt/bin/R")).exists() && launchRserve("/opt/bin/R"))
 			);
 	}
 	
 	/** attempt to start Rserve. Note: parameters are <b>not</b> quoted, so avoid using any quotes in arguments
 	 @param cmd command necessary to start R
 	 @param rargs arguments are are to be passed to R
 	 @param rsrvargs arguments to be passed to Rserve
 	 @return <code>true</code> if Rserve is running or was successfully started, <code>false</code> otherwise.
 	 */
 	public static boolean launchRserve(String cmd, String rargs, String rsrvargs, boolean debug) {
 		try {			
 			log.info(String.format("cmd=%s, rargs=%s, rsrvargs=%s, debug=%s", cmd,rargs,rsrvargs,debug));
 			
 			Process p;
 			String[] cmdStr=null;
 			boolean isWindows = false;
 			String osname = System.getProperty("os.name");
 			if (osname != null && osname.length() >= 7 && osname.substring(0,7).equals("Windows")) {
 				isWindows = true; /* Windows startup */
 				cmdStr = new String[] { "\""+cmd+"\" -e \"library(Rserve);Rserve("+(debug?"TRUE":"FALSE")+",args='"+rsrvargs+"')\" "+rargs };
 				log.info("Starting R: " + Arrays.toString(cmdStr));
 				p = Runtime.getRuntime().exec(cmdStr);
 			} else { /* unix startup */
 				cmdStr = new String[] {
 					      "/bin/sh", "-c",
 					      "echo 'library(Rserve);Rserve("+(debug?"TRUE":"FALSE")+",args=\""+rsrvargs+"\")'|"+cmd+" "+rargs
 					      };
 				log.info("Starting R: " + Arrays.toString(cmdStr));
 				p = Runtime.getRuntime().exec(cmdStr);
 			}
 			log.info("waiting for Rserve to start ... ("+Arrays.toString(cmdStr)+")");
 			// we need to fetch the output - some platforms will die if you don't ...
 			StreamHog errorHog = new StreamHog(p.getErrorStream(), false);
 			StreamHog outputHog = new StreamHog(p.getInputStream(), false);
 			if (!isWindows) /* on Windows the process will never return, so we cannot wait */
 				p.waitFor();
 			log.info("call terminated, let us try to connect ...");
 		} catch (Exception x) {
 			log.error("ERROR: failed to start Rserve process, error: "+x.getMessage());
 			return false;
 		}
 		
 		int attempts = 5; /* try up to 5 times before giving up. We can be conservative here, because at this point the process execution itself was successful and the start up is usually asynchronous */
 		while (attempts > 0) {
 			try {
 				RConnection c = new RConnection();
 				log.info("Rserve is running.");
 				c.close();
 				return true;
 			} catch (Exception e2) {
 				log.info("Try failed with: "+e2.getMessage());
 			}
 			/* a safety sleep just in case the start up is delayed or asynchronous */
 			try { Thread.sleep(500); } catch (InterruptedException ix) { };
 			attempts--;
 		}
 		return false;
 	}
 	
 	/** check whether Rserve is currently running (on local machine and default port).
 	 @return <code>true</code> if local Rserve instance is running, <code>false</code> otherwise
 	 */
 	public static boolean isRserveRunning() {
 		try {
 			RConnection c = new RConnection();
 			log.info("Rserve is running.");
 			c.close();
 			return true;
 		} catch (Exception e) {
 			log.info("First connect try failed with: "+e.getMessage());
 		}
 		return false;
 	}
 	
 	public static RList getBasicWindowRList(Map<String, List<LatestMarketData>> tickCache, List<String> symbols, int bwNum, int basicWindowSize) {
 		String symbol;
 		List<LatestMarketData> tickStream;
 		List<String> timeStamp = new ArrayList<String>();
 		List<Integer> winNum = new ArrayList<Integer>();
 		LatestMarketData md;
 		List<Double> val;
 		List<List<Double>> midPxNew = new ArrayList<List<Double>>(symbols.size());
 		boolean addOnce = false;
 		
 		final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");	
 		RList bwList = new RList();
 		
 		try {	
 			log.info("Creating basic window (basicWindowSize="+basicWindowSize+") and pass it to R...bwNum="+bwNum);
 			
 			for(int j=0; j<symbols.size(); j++) {
 				symbol = symbols.get(j);
 				tickStream = tickCache.get(symbol);
 				
 				List<Double> value = null;
 				if(midPxNew.size() <= j) {
 					value = new ArrayList<Double>();
 					midPxNew.add(j, value);
 				}
 				
 				//for(int i=basicWindowSize*bwNum; i<basicWindowSize*(bwNum+1); i++) {
 				for(int i=0; i<basicWindowSize; i++) {
 					md =tickStream.get(i);
 					value.add(md.getLatestBid().getPrice().add(md.getLatestOffer().getPrice()).divide(new BigDecimal(2)).doubleValue());
 					
//					log.info("haha: md=" + md);
//					log.info("haha: md.getTime=" + md.getTime());
//					log.info("haha: SDF=" + SDF);
//					log.info("haha: winNum=" + winNum);
 					if(!addOnce) {
						if (Boolean.parseBoolean(config.getProperty("SIMULATION","false"))) {
							timeStamp.add(SDF.format(new Date()));
						}
						else {
							timeStamp.add(SDF.format(md.getTime()));
						}
 						winNum.add(bwNum);
 					}
 				}
 				addOnce = true;
 			}
 			
 			bwList.put("timestamp", new REXPString(timeStamp.toArray(new String[timeStamp.size()])));
 			bwList.put("winNum", new REXPInteger(ArrayUtils.toPrimitive(winNum.toArray(new Integer[winNum.size()]))));
 			
 			for(int i=0; i<midPxNew.size(); i++) {
 				symbol = symbols.get(i);
 				val = midPxNew.get(i);
 		
 				bwList.put(symbol, new REXPDouble(ArrayUtils.toPrimitive(val.toArray(new Double[val.size()]))));
 			}			
 			
 			for(int i=0; i<timeStamp.size(); i++) {
 				StringBuffer sb = new StringBuffer();			
 				
 				sb.append("["+i+"] "+timeStamp.get(i)+"|"+winNum.get(i)+"|");
 				for(int j=0; j<midPxNew.size(); j++) {
 					val = midPxNew.get(j);
 					sb.append(val.get(i)+"|");
 				}
 				log.info(sb.toString());
 			}	
 		
 		} catch (Exception e) {
 			log.error("Whoops error creating data for basic window");
 			log.error(e.getMessage());
 	        e.printStackTrace();
 	    }
 		
 		return bwList;
 	}
 	
 	public static List<String> getAllSymbols(Properties inConfig) {
 		config = inConfig;
 		String index = config.getProperty("BENCHMARK_INDEX");
 		if (index == null) {
 			log.error("No benchmark index defined for the run");
 			System.exit(1);
 		}
 		
 		String fileName = config.getProperty("SYMBOL_FILE");
 		if (fileName == null) {
 			log.error("No symbol file defined");
 			System.exit(1);
 		}
 		
 		List<String> symbols = new ArrayList<String>();
 		
 		if(index != null)
 			symbols.add(index);
 		
 		if (fileName == null) {
 			fileName = "~/dev/FHX/fhx_java/conf/dia.us.csv";
 		}
 
 		FileReader fileReader;
 		try {
 			fileReader = new FileReader(fileName);
 
 			BufferedReader bufferedReader = new BufferedReader(fileReader);
 
 			// skip header
 			bufferedReader.readLine();
 
 			String line = null;
 			while ((line = bufferedReader.readLine()) != null) {
 				symbols.add(line.trim());
 			}
 
 			log.info("Gathered " + symbols.size() + " symbols");
 			bufferedReader.close();
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		return symbols;
 	}
 }
