 package de.hub.clickwatch.apps.performance.compoundhandler;
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.PrintStream;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 
 import com.google.inject.Guice;
 
 import de.hub.clickwatch.ClickWatchModule;
 import de.hub.clickwatch.ClickWatchStandaloneSetup;
 import de.hub.clickwatch.analysis.data.Plot;
 import de.hub.clickwatch.apps.performance.compoundhandler.PerformanceGauge.RetrievalType;
 import de.hub.clickwatch.connection.INodeConnection;
 import de.hub.clickwatch.connection.INodeConnectionProvider;
 import de.hub.clickwatch.connection.adapter.IHandlerAdapter;
 import de.hub.clickwatch.connection.adapter.IValueAdapter;
 import de.hub.clickwatch.recoder.cwdatabase.ExperimentStatistics;
 import de.hub.clickwatch.recorder.database.CWRecorderStandaloneSetup;
 import de.hub.clickwatch.specificmodels.brn.BrnValueAdapter;
 import de.hub.clickwatch.specificmodels.brn.sys_info_systeminfo.Systeminfo;
import de.hub.clickwatch.util.Throwables;
 
 public class PerformanceExperiment implements IApplication {
 	
 	private static int remoteLocalUpdateIntervalFactor = 10;
 	private static int updateInterval = 200;
 	private static int duration = 30000;
 
 	public void runExperiment(String args[]) {
 		PrintStream out = System.out;
		File file = null;
 		String[] nodeIds = null;
 		
 		Options options = new Options();
 		options.addOption("u", true, "update interval");
 		options.addOption("d", true, "duration");
 		options.addOption("r", true, "remote local update interval factor");
 		options.addOption("o", true, "output file");
 		
 		try {
 			CommandLine cl = new PosixParser().parse(options, args);
 		
 			if (cl.hasOption("u")) {
 				updateInterval = Integer.parseInt(cl.getOptionValue("u"));
 			} 
 			if (cl.hasOption("d")) {
 				duration = Integer.parseInt(cl.getOptionValue("d"));
 			}
 			if (cl.hasOption("r")) {
 				remoteLocalUpdateIntervalFactor = Integer.parseInt(cl.getOptionValue("r"));
 			}
 			if (cl.hasOption("o")) {
				file = new File(cl.getOptionValue("o"));
				out = new PrintStream(file); 
 			}
 		
 			nodeIds = cl.getArgs();
 		} catch (Exception e) {
 			new HelpFormatter().printHelp(e.getLocalizedMessage(), options);
 			return;	
 		}
 		
 		PerformanceGauge gauge = new PerformanceGauge(remoteLocalUpdateIntervalFactor);
 		Plot plot = new Plot();
 		
 		for(int run = 1; run <= 23; run++) {
 			for(int handler: new int[] { 50, 100, 150, 200 }) {
 				for(RetrievalType retrievalType: new RetrievalType[] {
 						RetrievalType.BASE_LINE, 
 						RetrievalType.ON_DEMAND, 
 						RetrievalType.COMPOUND_HANDLER, 
 						RetrievalType.DELTA_COMPOUND_HANDLER }) {
 					for(String nodeId: nodeIds) {
 						double baseCpuLoad = getCpuLoad(nodeId);
 						
 						ExperimentStatistics statistics = gauge.measure(retrievalType, duration, updateInterval, handler, nodeId);
 						
 						int remoteLocalUpdateIntervalInfluence = 1;
 						if (retrievalType == RetrievalType.COMPOUND_HANDLER || retrievalType == RetrievalType.DELTA_COMPOUND_HANDLER) {
 							remoteLocalUpdateIntervalInfluence = remoteLocalUpdateIntervalFactor;
 						}
 						plot.addEntry(
 								run, 
 								Integer.parseInt(nodeId.replace(".", "")), 
 								handler, 
 								retrievalType.ordinal(),
 								(statistics.getTimeRequestS().getMean()/1000000)/remoteLocalUpdateIntervalInfluence,
 								statistics.getBytesRequestS().getMean()/remoteLocalUpdateIntervalInfluence,
 								statistics.getCpuLoadS().getMean(),
 								statistics.getSamplesN().getSum()*remoteLocalUpdateIntervalInfluence,
 								baseCpuLoad);
								
						out.close();
						try {
							out = new PrintStream(file);
						} catch (FileNotFoundException e) {
							Throwables.propagate(e);
						} 
 						plot.printCSV(out);
 						out.flush();
 					}
 				}
 			}
 		}	
		out.close();
 	}
 	
 	private double getCpuLoad(String nodeId) {
 		INodeConnectionProvider ncp = Guice.createInjector(new ClickWatchModule() {
 			@Override
 			protected void bindValueAdapter() {
 				bind(IValueAdapter.class).to(BrnValueAdapter.class);
 			}
 		}).getInstance(INodeConnectionProvider.class);
 		INodeConnection nc = ncp.createConnection(nodeId, "7777");
 		nc.connect();
 		Systeminfo systemInfo = (Systeminfo)nc.getAdapter(IHandlerAdapter.class).getHandler("sys_info/systeminfo");
 		double result = systemInfo.getSystem().getCpu_usage().getReal();
 		nc.disconnect();
 		EcoreUtil.delete(systemInfo);
 		return result;
 		
 	}
 	
 	@Override
 	public Object start(IApplicationContext context) throws Exception {
 		String[] args = (String[]) context.getArguments().get("application.args");
 		runExperiment(args);
 	    return EXIT_OK;
 	}
 
 	@Override
 	public void stop() {
 		System.out.println("forced to stop ... rude by OSGI!");
 	}
 	
 	public static final void main(String[] args) {
 		ClickWatchStandaloneSetup.doSetup();
 		CWRecorderStandaloneSetup.doSetup();
 		new PerformanceExperiment().runExperiment(args);
 	}
 }
