 package org.lttng.studio.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
 import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
 import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
 import org.lttng.studio.reader.AnalysisPhase;
 import org.lttng.studio.reader.AnalyzerThread;
 import org.lttng.studio.reader.handler.IModelKeys;
 import org.lttng.studio.reader.handler.ITraceEventHandler;
 import org.lttng.studio.reader.handler.TraceEventHandlerFactory;
 import org.lttng.studio.reader.handler.TraceEventHandlerSched;
 import org.lttng.studio.reader.handler.WakeupContextHandler;
 
 public class WakeupContext {
 
 	static Options options;
 
 	public class Opts {
 		public File traceDir;
 		public Long pid;
 		public String comm;
 		public boolean followChild;
 	}
 
 	public static void main(String[] args) throws IOException, InterruptedException, ParseException {
 		WakeupContext self = new WakeupContext();
 		Opts opts = self.new Opts();
 		options = new Options();
 		options.addOption("t", true, "trace path");
 		options.addOption("p", true, "filter with this pid");
 		options.addOption("c", true, "filter with this command");
 		options.addOption("f", false, "follow child processes");
 
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		boolean isError = false;
 		try {
 			cmd = parser.parse(options, args);
 		} catch (ParseException e) {
 			System.out.println("Error parsing options: " + e.getMessage());
 			return;
 		}
 
 		if (cmd.hasOption("t")) {
 			String trace = cmd.getOptionValue("t");
 			opts.traceDir = new File(trace);
 		}
 		if (cmd.hasOption("p")) {
 			opts.pid = new Long(cmd.getOptionValue("p"));
 		}
 		if (cmd.hasOption("c")) {
 			opts.comm = cmd.getOptionValue("c");
 		}
 		if (cmd.hasOption("f")) {
 			opts.followChild = true;
 		}
 
 		if (opts.traceDir == null) {
 			usage();
 		}
 
 		AnalyzerThread thread = new AnalyzerThread();
 		CtfTmfTrace ctfTmfTrace = new CtfTmfTrace();
 		try {
 			ctfTmfTrace.initTrace(null, opts.traceDir.getCanonicalPath(), CtfTmfEvent.class);
 		} catch (TmfTraceException e) {
 			System.out.println("Error loading trace " + opts.traceDir.getCanonicalPath());
 			e.printStackTrace();
 		}
 
 		AnalysisFilter filter = thread.getReader().getRegistry().getOrCreateModel(IModelKeys.SHARED, AnalysisFilter.class);
 		filter.addTid(opts.pid);
 		filter.addCommand(opts.comm);
 		filter.setFollowChild(opts.followChild);

 		Collection<ITraceEventHandler> phase1 = TraceEventHandlerFactory.makeStatedump();
 		Collection<ITraceEventHandler> phase2 = new HashSet<ITraceEventHandler>();
		phase2.add(new WakeupContextHandler());
 		phase2.add(new TraceEventHandlerSched());

 		thread.setTrace(ctfTmfTrace);
 		thread.addPhase(new AnalysisPhase(1, "test", phase1));
 		thread.addPhase(new AnalysisPhase(2, "test", phase2));
 		thread.start();
 		thread.join();
 
 		System.out.println("done");
 	}
 
 	private static void usage() {
 		HelpFormatter formatter = new HelpFormatter();
 		formatter.printHelp("wakeup", options);
 		System.exit(1);
 	}
 
 }
