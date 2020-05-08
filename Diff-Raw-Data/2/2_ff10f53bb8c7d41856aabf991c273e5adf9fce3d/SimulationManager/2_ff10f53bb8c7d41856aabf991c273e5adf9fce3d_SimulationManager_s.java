 /**
  * @(#)SimulationManager.java
  */
 
 package aurora.service;
 
 import java.io.*;
 
 import javax.xml.parsers.*;
 import org.xml.sax.InputSource;
 import org.w3c.dom.*;
 import aurora.hwc.*;
 
 
 /**
  * Process manager for simulation. 
  * @author Alex Kurzhanskiy
  */
 public class SimulationManager implements ProcessManager {
 	
 	/**
 	 * Run simulation with progress reports at required frequency.
 	 * @param input_files [0] contains buffered XML configuration; [1] contains buffered XML time range specification.
 	 * @param output_files [0] contains name of the output file; [1] (optional) place holder for configuration XML dumped on exit. 
 	 * @param updater 
 	 * @param period
 	 * @return <code>Done!</code> if successful, otherwise throw exception.
 	 */
 	public String run_application(String[] input_files, String[] output_files, Updatable updater, int period) throws Exception {
 		double initial_time, max_time;
 		if ((input_files == null) || (input_files.length < 1))
 			throw new Exception("Error: No input files!");
 		if ((output_files == null) || (output_files.length < 1))
 			throw new Exception("Error: No output files specified!");
 		ContainerHWC mySystem = new ContainerHWC();
 		mySystem.batchMode();
 		try {
 	        InputSource is = new InputSource();
 	        is.setCharacterStream(new StringReader(input_files[0]));
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
 			mySystem.initFromDOM(doc.getChildNodes().item(0));
 			mySystem.validate();
 			initial_time = mySystem.getMySettings().getTimeInitial();
 			max_time = mySystem.getMySettings().getTimeMax();
 			if (input_files.length > 1) {
 				double initial_time0 = initial_time;
 				double max_time0 = max_time;
 				is.setCharacterStream(new StringReader(input_files[1]));
 				Node trp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getChildNodes().item(0);
 				Node bt_attr = trp.getAttributes().getNamedItem("begin_time");
 				if (bt_attr != null)
 					initial_time0 = Double.parseDouble(bt_attr.getNodeValue())/3600.0;
 				Node dur_attr = trp.getAttributes().getNamedItem("duration");
 				if (dur_attr != null)
 					max_time0 = initial_time0 + (Double.parseDouble(dur_attr.getNodeValue()) / 3600.0);
 				initial_time0 = Math.max(initial_time0, initial_time);
				max_time0 = Math.min(max_time0, max_time);
 				if (initial_time0 < max_time0) {
 					initial_time = initial_time0;
 					max_time = max_time0;
 				}
 			}
 		}
 		catch(Exception e) {
 			throw new Exception("Error: Failed to parse xml: " + e.getMessage());
 		}
 		File data = null;
 		try {
 			data = new File(output_files[0]);
 			if ((!mySystem.getMySettings().setTmpDataFile(data)) || (!mySystem.getMySettings().createDataHeader())) {
 				throw new Exception("Error: Failed to open data output file!");
 			}
 		}
 		catch(Exception e) {
 			throw new Exception("Error: Failed to open data output file: " + e.getMessage());
 		}
 		if (output_files.length > 1) { // warm-up run
 			mySystem.getMySettings().setTimeMax(initial_time);
 		}
 		else { // normal run
 			mySystem.getMySettings().setTimeMax(max_time);
 			mySystem.getMySettings().setTimeInitial(initial_time);
 		}
 		try {
 			mySystem.initialize();
 		}
 		catch(Exception e) {
 			mySystem.getMySettings().getTmpDataOutput().close();
 			throw new Exception("Error: Failed to initialize: " + e.getMessage());
 		}
 		boolean res = true;
 		mySystem.getMyStatus().setStopped(false);
 		mySystem.getMyStatus().setSaved(false);
 		int ts = mySystem.getMyNetwork().getTS();
 		int start_ts = ts;
 		double total_sim_time = Math.max(0, mySystem.getMySettings().getTimeMax() - mySystem.getMyNetwork().getSimTime());
 		long curr_sys_time = System.currentTimeMillis();
 		long lst_updt_time = curr_sys_time;
 		while ((!mySystem.getMyStatus().isStopped()) && res) {
 			try {
 				res = mySystem.dataUpdate(++ts);
 			}
 			catch(Exception e) {
 				mySystem.getMySettings().getTmpDataOutput().close();
 				throw new Exception("Simulation failed on time step " + ts + ": " + e.getMessage());
 			}
 			curr_sys_time = System.currentTimeMillis();
 			if ((updater != null) && (((curr_sys_time-lst_updt_time)/1000) >= period)) {
 				updater.notify_update((int)Math.round(100*(((ts - start_ts)*mySystem.getMyNetwork().getTP()) / total_sim_time)));
 				lst_updt_time = curr_sys_time;
 			}
 		}
 		if (!res)
 			throw new Exception("Simulation failed on time step " + ts);
 		mySystem.getMySettings().getTmpDataOutput().close();
 		if ((output_files != null) && (output_files.length > 1)) {
 			PrintStream ps = null;
 			try {
 				if ((output_files[1] != null) && (!output_files[1].isEmpty())) {
 					File o_file = new File(output_files[1]);
 					ps = new PrintStream(new FileOutputStream(o_file.getAbsolutePath()));
 					mySystem.xmlDump(ps);
 				}
 				else {
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					ps = new PrintStream(baos);
 					mySystem.xmlDump(ps);
 					output_files[1] = baos.toString();
 				}
 			}
 			catch(Exception e) {
 				if (ps != null)
 					ps.close();
 				throw new Exception("Error: Failed to generate configuration file");
 			}
 			ps.close();
 		}
 		return("Done!");
 	}
 
 	/**
 	 * My process is Simulation.
 	 */
 	public String whatIsMyProcess() {
 		return "Simulation";
 	}
 
 }
