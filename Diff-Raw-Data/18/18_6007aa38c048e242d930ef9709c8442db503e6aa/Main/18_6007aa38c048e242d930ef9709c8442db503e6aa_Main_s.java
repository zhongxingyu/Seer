 /**
  * @author Jeff Standen <jeff@webgroupmedia.com>
  */
 
 package com.portsensor.main;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.math.BigInteger;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.TreeMap;
 
 import org.hyperic.sigar.CpuPerc;
 import org.hyperic.sigar.FileSystemUsage;
 import org.hyperic.sigar.ProcCpu;
 import org.hyperic.sigar.ProcCredName;
 import org.hyperic.sigar.ProcMem;
 import org.hyperic.sigar.ProcState;
 import org.hyperic.sigar.ProcTime;
 import org.hyperic.sigar.Sigar;
 import org.hyperic.sigar.SigarException;
 import org.hyperic.sigar.Who;
 import org.hyperic.sigar.cmd.Ps;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import com.portsensor.command.CommandFactory;
 import com.portsensor.sensor.ConsoleSensor;
 import com.portsensor.sensor.SensorCheck;
 import com.portsensor.testers.PortTester;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		if(1 == args.length && args[0].equalsIgnoreCase("--config")) {
 			ConfigurationBuilder.autoConfigure();
 			
 		} else if(0 == args.length || 1 == args.length && args[0].equalsIgnoreCase("--help")) {
 			Main.printHelp();
 			
 		} else {
 			Configuration cfg = Configuration.getInstance();
 			if(!cfg.load(args[0])) {
 				System.exit(1);
 			}
 
 //			if(2 == args.length && args[1].equalsIgnoreCase("--list-devices")) {
 //				cfg.printDevices();
 //			} else if(3 == args.length && args[1].equalsIgnoreCase("--list-sensors")) {
 //				cfg.printSensors(args[0], args[2]);
 //			} else if(6 == args.length && args[1].equalsIgnoreCase("--add-sensor")) {
 //				cfg.addPort(args[0], args[2], args[3], args[4], args[5]);
 //			} else if(4 == args.length && args[1].equalsIgnoreCase("--remove-sensor")) {
 //				cfg.removeSensor(args[0], args[2], args[3]);
 			if(2 == args.length && args[1].equalsIgnoreCase("--test")) {
 				String xml = Main.getXML();
 				System.out.println(xml);
 			} else if(1==args.length) {
 				String xml = Main.getXML();
 				Main.postXML(xml);
 			} else {
 				Main.printHelp();
 			}
 			
 		}
 		
 		System.exit(0);
 	}
 	
 	private static void printHelp() {
 		System.out.println("Syntax:");
 		System.out.println("<config file>");
 		System.out.println("<config file> --test");
 //		System.out.println("<config file> --list-devices");
 //		System.out.println("<config file> --list-sensors <device>");
 //		System.out.println("<config file> --add-sensor <device> <host> <service> <port>");
 //		System.out.println("<config file> --remove-sensor <device> <sensor>");
 //		System.out.println("--config");
 		System.out.println("--help");
 	}
 	
 	private static String getXML() {
 		Configuration cfg = Configuration.getInstance();
 		Sigar sigar = new Sigar();
 		
 		// Formatters
 		NumberFormat loadFormatter = DecimalFormat.getNumberInstance();
 		loadFormatter.setMaximumFractionDigits(2);
 		
 		SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd HH:mm");
 		
 		// Output XML
 		Element eRoot = new Element("sensors");
 		Document doc = new Document(eRoot);
 		
 		Iterator<ConsoleSensor> iCommands = cfg.getSensors().iterator();
 		while(iCommands.hasNext()) {
 			ConsoleSensor sensor = iCommands.next();
 			String cmd = sensor.getCommand();
 			char cStatus = SensorCheck.STATUS_OK;
 
 			String sSensorOut = "";
 			String sOut = "";
 			
 			// [TODO] Move commands into their own classes
 			
 			// Built-in commands
 			if(cmd.startsWith("#PORT")) {
 				String[] parts = cmd.split(" ");
 				String sHost = parts[1];
 				Integer iPort = Integer.parseInt(parts[2]);
 				sSensorOut = (PortTester.testPort(sHost, iPort)) ? "UP" : "DOWN";
 				
 			} else if(cmd.startsWith("#WHO")) {
 				StringBuilder str = new StringBuilder();
 				try {
 					Who[] whos = sigar.getWhoList();
 					for (Who who : whos) {
 						String host = who.getHost();
 						str.append(String.format("%s\t%s\t%s%s\n", 
 								new Object[] {
 									who.getUser(), 
 									who.getDevice(), 
 									dateFormatter.format(new Date(who.getTime()*1000)),
 									((0 == host.length()) ? "" : String.format("\t(%s)", host))
 								}));
 					}
 					sSensorOut = str.toString();
 				} catch (SigarException e) {
 					e.printStackTrace();
 				}
 
 			} else if(cmd.startsWith("#DF")) {
 				String[] parts = cmd.split(" ");
 				String sFileSystem = parts[1];
 				try {
 					FileSystemUsage usage = sigar.getFileSystemUsage(sFileSystem);
 					sSensorOut = Double.toString(usage.getUsePercent());
 				} catch (SigarException e) {
 					e.printStackTrace();
 				}
 					
 			} else if(cmd.startsWith("#LOAD")) {
 				try {
 					double loads[] = sigar.getLoadAverage();
 					sSensorOut = String.valueOf(loadFormatter.format(loads[0])); // 1 min avg
 				} catch (SigarException e) {
 					e.printStackTrace();
 				}
 				
 			} else if(cmd.startsWith("#PS")) {
 				String[] parts = cmd.split(" ");
 				String sSortBy = (2==parts.length) ? parts[1] : "mem";
 				
 				try {
 					StringBuilder str = new StringBuilder();
 					String sProcUser = "";
 					
 					// Sort by the desired criteria
 					TreeMap<Long, String> pidTree = new TreeMap<Long, String>(Collections.reverseOrder()); 
 					long[] pids = sigar.getProcList();
 					
 					// Do we need to prime the CPU measurements?
 					if(sSortBy.equals("cpu")) {
 						for(long pid : pids) {
 							try {
 								ProcCpu procCpu = sigar.getProcCpu(pid);
 								procCpu.getPercent();
 							} catch(Exception e) {
 								// ignore things we can't measure
 							}
 						}
 					}
 					
 					try {
 						Thread.sleep(1000L);
 					} catch(Exception e) {}
 					
 					// [TODO] Ignore our own pid?
 					
 					for(long pid : pids) {
 						try {
 							ProcCredName procUser = sigar.getProcCredName(pid);
 							sProcUser = procUser.getUser();
 							ProcState procState = sigar.getProcState(pid);
 							
 							if(sSortBy.equals("cpu")) { // [TODO] Format CPU time
 								ProcCpu procCpu = sigar.getProcCpu(pid);
 								Double perc = procCpu.getPercent();
 								
 								String out = String.format("%s\t[%d]\t%s\t(%s)\n", new Object[] {
 										CpuPerc.format(perc),
 										pid,
 										//ProcUtil.getDescription(sigar, pid),
 										procState.getName(),
 										sProcUser
 									});
 								
 								Long hash = new Double(perc * 100 * 1000000).longValue();
 								
 								while(pidTree.containsKey(hash)) {
 									hash++;
 								}
 								
 								pidTree.put(hash, out);
 								
 							} else if(sSortBy.equals("time")) { // [TODO] Format run time
 								ProcTime procTime = sigar.getProcTime(pid);
 								String out = String.format("%s\t[%d]\t%s\t(%s)\n", new Object[] {
 										Ps.getCpuTime(procTime.getTotal()),
 										pid,
 										procState.getName(),
 										//ProcUtil.getDescription(sigar, pid),
 										sProcUser
 									});
 								pidTree.put(procTime.getTotal(), out);
 								
 							} else { // "mem"
 								ProcMem procMem = sigar.getProcMem(pid);
 								String out = String.format("%s\t[%d]\t%s\t(%s)\n", new Object[] {
 										Sigar.formatSize(procMem.getResident()),
 										pid,
 										procState.getName(),
 										//ProcUtil.getDescription(sigar, pid),
 										sProcUser
 									});
 								pidTree.put(procMem.getResident(), out);
 								
 							}
 						} catch(Exception e) { /* ignore partial processes */ }
 					}
 					
 					// Make sure we have 5 max items
 					while(pidTree.size() > 5) {
 						pidTree.remove(pidTree.lastKey());
 					}
 					
 					// Lazy load the remaining items
 					for(String process : pidTree.values()) {
 						str.append(process);
 					}
 					
 					sSensorOut = str.toString();
 					
 				} catch (SigarException e) {
 					e.printStackTrace();
 				}
 				
 			} else { // command wrapper
 				sSensorOut = CommandFactory.quickRun(cmd).trim();
 			}
 			
 			try {
 				// Typecast as requested
 				if(sensor.getType().equals(ConsoleSensor.TYPE_NUMBER)) {
 					sSensorOut = String.valueOf(DecimalFormat.getNumberInstance().parse(sSensorOut).longValue());
 				} else if(sensor.getType().equals(ConsoleSensor.TYPE_PERCENT)) {
 					if(sSensorOut.endsWith("%")) {
 					} else {
 						double percent = DecimalFormat.getNumberInstance().parse(sSensorOut).doubleValue();
 						sSensorOut = String.valueOf(Math.round(100*percent)) + "%";
 					}
 					
 				} else if (sensor.getType().equals(ConsoleSensor.TYPE_DECIMAL)) {
 					sSensorOut = String.valueOf(DecimalFormat.getNumberInstance().parse(sSensorOut).doubleValue());
 				} else if (sensor.getType().equals(ConsoleSensor.TYPE_UPDOWN)) {
 					sSensorOut = String.valueOf(sSensorOut);
 				}
 
 				if(sOut.equals(""))
 					sOut = sSensorOut;
 				
 				// Rule check
 				Iterator<SensorCheck> i = sensor.getChecks().iterator();
 				while(i.hasNext()) {
 					SensorCheck rule = i.next();
 					if(rule.check(sSensorOut)) {
 						cStatus = rule.getStatus();
 						sOut = rule.getMessage() + ": " + sSensorOut;
 					}
 				}
 
 			} catch(ParseException pe) {
 				pe.printStackTrace();
 				cStatus = SensorCheck.STATUS_CRITICAL;
 				sOut = "Sensor result was not a valid "+sensor.getType();
 			}
 			
			if(0 != sSensorOut.length()) {
				Element e = new Element("sensor");
				e.setAttribute("id", sensor.getId());
				e.addContent(new Element("name").setText(sensor.getName()));
				e.addContent(new Element("status").setText(String.valueOf(cStatus)));
				e.addContent(new Element("metric").setText(sSensorOut));
				e.addContent(new Element("metric_type").setText(sensor.getType()));
				e.addContent(new Element("output").setText(sOut));
				eRoot.addContent(e);
			}
 		}
 
 	    XMLOutputter outputter = new XMLOutputter();
 	    outputter.setFormat(Format.getPrettyFormat());
     	return outputter.outputString(doc);       
 	}
 	
 	private static void postXML(String xml) {
 		Configuration cfg = Configuration.getInstance();
         URL url = null;
     	MessageDigest mdMD5 = null;
         
     	String username = cfg.getSetting(Configuration.SETTING_USERNAME, "");
     	String secret_key = cfg.getSetting(Configuration.SETTING_SECRET_KEY, "");
     	String post_url = cfg.getSetting(Configuration.SETTING_POST_URL, "");
 
     	try {
 			mdMD5 = MessageDigest.getInstance("MD5");
 			url = new URL(post_url);
         } catch(Exception e) {
         	System.out.println("Error: " + e.getMessage());
         	System.exit(1);
         }
 
 		String httpDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z").format(new Date()).toString();
 		String url_query = (null==url.getQuery()) ? "" : url.getQuery();
 		
 		System.out.println("Posting to " + post_url);
 
 		try {
 			// Password MD5
 			mdMD5.update(secret_key.getBytes("iso-8859-1"));
 			String password_hash = new BigInteger(1,mdMD5.digest()).toString(16);
 			while(password_hash.length() < 32) // pad
 				password_hash = "0"+password_hash;
 			
 	        // Construct data
 	        String stringToSign = "POST\n"+httpDate+"\n"+url.getPath()+"\n"+url_query+"\n"+xml+"\n"+password_hash+"\n";
 	        mdMD5.update(stringToSign.getBytes("iso-8859-1"));
 	    	String signedString = String.valueOf(new BigInteger(1,mdMD5.digest()).toString(16));
 	    	
 	        // Send data
 	        URLConnection conn = url.openConnection();
 	        conn.setDoOutput(true);
 	        conn.setDoInput(true);
 	        conn.addRequestProperty("Cerb5-Auth", username+":"+signedString);
 	        conn.addRequestProperty("Date", httpDate);
 	        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 	        wr.write(xml);
 	        wr.flush();
 	    
 	        // Get the response
 	        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 	        String line;
 	        while ((line = rd.readLine()) != null) {
 	            // Process line...
 	        	System.out.println(line);
 	        }
 	        wr.close();
 	        rd.close();
 	        
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }		
 	}
 
 }
