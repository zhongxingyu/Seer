 package com.examples;
 
 import static com.examples.Results.*;
 import static org.facile.ContextParser.*;
 import static org.facile.ContextParser.Input.IN;
 import static org.facile.Facile.*;
 import java.io.File;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.List;
 import org.facile.Facile.my;
 import org.facile.IO.FileObject;
 import org.facile.ProcessIO.ProcessInOut;
 import org.facile.ProcessIO.ProcessOut;
 import org.facile.Sys;
 
 public class AutoBench {
 
 	// --clients xen.caucho.com:4600,lancre.caucho.com:4600
 	static String[] clients;
 	// --uri1 /file_$5.html
 	static String uri1;
 	// --host1 ch_resin
 	static String host1;
 	// --port1 8080
 	static int port1;
 	// --uri2 /file_0k.html
 	static String uri2;
 	// --host2 ch_nginx
 	static String host2;
 	// --port2 80
 	static int port2;
 	// --num_conn $1
 	static int numConn;
 	// --num_call 10
 	static int numCall;
 	// --low_rate $2
 	static int lowRate;
 	// --high_rate $3
 	static int highRate;
 	// --rate_step $4
 	static int rateStep;
 	// --timeout 3
 	static int timeout;
 	// --file out_con$1_start$2_end$3_step$4_$5.tsv
 	static File file;
 	static File homeDir;
 	static boolean dryRun;
 	static boolean verbose;
 	static boolean echoCommandLine = true;
 
 	static boolean slave = false;
 	static boolean master = false;
 
 	static int processTimeout = 0;
 
 	static List<File> path;
 
 	static File outputFolder;
 	static int slaveId;
 	static boolean remote;
 
 	static boolean shortForm;
 	
 	static int slavePort = 4600;
 	private static ServerSocket slaveServerSocket;
 	private static Socket socketFromSlaveToMaster;
 
 	public static void runIt() throws Exception {
 
 		if (slave) {
 			runSlave();
 		}
 
 		if (homeDir == null) {
 			homeDir = cwf();
 			if (verbose)
 				print("Home directory was not passed setting to: ", homeDir);
 		}
 
 		if (verbose)
 			print("Command line arguments", cmdLine());
 
 		outputFolder = file(homeDir, "/output");
 
 		path = Sys.path();
 
 		if (Sys.os().startsWith("Mac OS X")) {
 			if (!isIn(file("/usr/bin"), path)) {
 				path.add(file("/usr/bin"));
 			}
 			if (!isIn(file("/usr/local/bin/"), path)) {
 				path.add(file("/usr/local/bin/"));
 			}
 		}
 
 		@SuppressWarnings("unchecked")
 		String runString1 = joinByString(
 				"_",
 				array("run1", "host", host1, "port", port1, "uri", uri1,
 						"numConn", numConn, "numCall", numCall, "rates",
 						lowRate, highRate, rateStep));
 		@SuppressWarnings("unchecked")
 		String runString2 = joinByString(
 				"_",
 				array("run2", "host", host2, "port", port2, "uri", uri2,
 						"numConn", numConn, "numCall", numCall, "rates",
 						lowRate, highRate, rateStep));
 
 		runString1 = runString1.replace('.', '_').replace('/', '_')
 				.replace(' ', '_').replace(':', '_').replace(',', '_');
 		runString2 = runString2.replace('.', '_').replace('/', '_')
 				.replace(' ', '_').replace(':', '_').replace(',', '_');
 
 		// Build an output file dir that uniquely defines this run
 		File out1Dir = file(outputFolder, runString1);
 		File out2Dir = file(outputFolder, runString2);
 
 		out1Dir.mkdirs();
 		out2Dir.mkdirs();
 
 		outputHeader();
 
 		for (int rate = lowRate, index = 0; rate < (highRate + rateStep); rate += rateStep, index++) {
 
 			print("Running benchmark run number ", index);
 			my myout1 = null;
 			my myout2 = null;
 
 			if (master) {
 				myout1 = runSlavesServer(1, rate, out1Dir, index);
 				myout2 = runSlavesServer(2, rate, out2Dir, index);
 			} else {
 				myout1 = runServer(1, rate, out1Dir, index);
 				myout2 = runServer(2, rate, out2Dir, index);
 			}
 			if (!dryRun) outputResult(rate, myout1, myout2);
 
 		}
 
 	}
 
 
 	
 	private static String httperfString(int rate, String host, String uri,
 			int port) {
 		String httperf1 = sprint("httperf", "--server", host, "--uri", uri,
 				"--num-con", numConn, "--num-call", numCall, "--timeout",
 				timeout, "--rate", rate, "--port", port);
 		return httperf1;
 	}
 
 	private static String httperfString(int rate, String host, String uri,
 			int port, int nconn, int ncall, int to) {
 		String httperf1 = sprint(
 				"httperf", 
 				"--server", host, 
 				"--uri", uri,
 				"--num-con", nconn, 
 				"--num-call", ncall, 
 				"--timeout", to, 
 				"--rate", rate, 
 				"--port", port);
 		return httperf1;
 	}
 
 	
 	private static void runSlave() throws Exception {
 		if (remote) {
 			initSlave();
 		}
 		
 		String command = null;
 		ProcessOut run = null;
 
 		FileObject reader;
 		FileObject writer;
 		if (!remote) {
 			reader = open(System.in);
 			writer = open(System.out);
 			writer.autoFlush();
 
 		} else {
 			reader = open(socketFromSlaveToMaster.getInputStream());
 			writer = open(socketFromSlaveToMaster.getOutputStream());
 			writer.autoFlush();
 		}
		
		writer.print("Slave started ok " + slaveId);

 		String line = reader.readLine();
 
 		expect("", "ack", line);
 
 		while(line!=null && !line.trim().equals("DIE NOW")) {
 			if (line.trim().equals("command")) {
 				writer.print("ack command", slaveId);
 				command = reader.readLine();
 				writer.print ("running ...... command", command);
 				run = run(processTimeout * 60, path, verbose, command);
 				writer.print ("done ...... running command", command);
 				writer.print("EXIT CODE for SLAVE ", slaveId, ": EXIT=", run.exit);
 				writer.print(run);
 				writer.print ("***done***");
 				if (run.exit != 0) {
 					System.exit(run.exit);
 				}
 			}
 			line = reader.readLine();
 		}
 		if (run==null) {
 			System.exit(-666);
 		} else {
 			System.exit(run.exit);
 		}
 	}
 
 
 	private static void initSlave() throws Exception {
 		   slaveServerSocket = new ServerSocket(slavePort);
 		   print("Waiting for socket connection on port " + slavePort);
 		   socketFromSlaveToMaster = slaveServerSocket.accept();
 	}
 
 
 
 	private static my runSlavesServer(int serverNum, int rate, File outDir, int runNumber) throws Exception {
 		initProcess();
 		expectFromChildren("Slave started ok");		
 		sendToChildren("ack");
 		
 		String cmdLine;
 
 		//Fill this with the real command.
 		if (serverNum == 1) {
 			cmdLine = httperfString(rate/2, host1, uri1, port1, numConn/2, numCall/2, timeout);
 		} else {
 			cmdLine = httperfString(rate/2, host2, uri2, port2, numConn/2, numCall/2, timeout);
 		}
 		sendToChildren("command");
 		expectFromChildren("ack command");
 		sendToChildren(cmdLine);
 		List<String> list = readFromChildren("***done***");
 		rest(1000);
 		my out1 = parseOutput(list.get(0));
 		my out2 = parseOutput(list.get(1));
 		rest(1000);
 		sendToChildren("DIE NOW");
 		
 		int times=0;
 		for (ProcessInOut p : processes) {
 			while (!p.isDone()) {
 				rest(1000);
 				times++;
 				if (times > 10) {
 					p.kill();
 				}
 			}
 		}
 		
 		if (inout1.processOut().exit == 0 && inout2.processOut().exit != 0) {
 			return out1;
 		} else if (inout1.processOut().exit != 0 && inout2.processOut().exit == 0) {
 			return out2;
 		} else if (inout1.processOut().exit != 0 && inout2.processOut().exit != 0) {
 			return null;
 		} else {
 		
 			Double v = get(dbl, out1, replies);
 			
 			if (v== null) {
 				return null;
 			}
 			if (v!=null) {
 				v+= get(dbl, out2, replies);
 				out1.i(replies, str(v));
 			}
 			
 			v = get(dbl, out1, error_total);
 			v+= get(dbl, out2, error_total);
 			out1.i(error_total, str(v));
 			
 			v = get(dbl, out1, conn_rate);
 			v+= get(dbl, out2, conn_rate);
 			out1.i(conn_rate, str(v));
 			
 			v = get(dbl, out1, req_rate);
 			v+= get(dbl, out2, req_rate);
 			out1.i(req_rate, str(v));
 			
 			v = get(dbl, out1, rep_rate_min);
 			v+= get(dbl, out2, rep_rate_min);
 			out1.i(rep_rate_min, str(v));
 	
 			v = get(dbl, out1, rep_rate_avg);
 			v+= get(dbl, out2, rep_rate_avg);
 			out1.i(rep_rate_avg, str(v));
 	
 			v = get(dbl, out1, rep_rate_max);
 			v+= get(dbl, out2, rep_rate_max);
 			out1.i(rep_rate_max, str(v));
 	
 			v = get(dbl, out1, rep_rate_stdv);
 			v+= get(dbl, out2, rep_rate_stdv);
 			out1.i(rep_rate_stdv, str(v));
 	
 			v = get(dbl, out1, status_100);
 			v+= get(dbl, out2, status_100);
 			out1.i(status_100, str(v));
 	
 			v = get(dbl, out1, status_200);
 			v+= get(dbl, out2, status_200);
 			out1.i(status_200, str(v));
 	
 			v = get(dbl, out1, status_300);
 			v+= get(dbl, out2, status_300);
 			out1.i(status_300, str(v));
 	
 			v = get(dbl, out1, status_400);
 			v+= get(dbl, out2, status_400);
 			out1.i(status_400, str(v));
 	
 			v = get(dbl, out1, status_500);
 			v+= get(dbl, out2, status_500);
 			out1.i(status_500, str(v));
 	
 			v = get(dbl, out1, net_io);
 			v+= get(dbl, out2, net_io);
 			out1.i(net_io, str(v));
 			
 			return out1;
 		}
 
 	}
 
 	static ProcessInOut inout1;
 	static ProcessInOut inout2;
 	static List<ProcessInOut> processes = ls(inout1, inout2);
 
 	
 	private static void sendToChildren(String value) {
 		for (FileObject toProcess : toSlaves) {
 			toProcess.println(value);
 		}
 	}
 	private static void expectFromChildren(String value) {
 		String line = null;
 		int index = 1;
 		for (FileObject out : fromSlaves) {
 			if (verbose) print ("expect " + value);
 			line = out.readLine();
 			if (verbose) print("FROM CHILD", index, line);
 			notNull(line);
 			expect("", value + " " + index, line.trim());
 			index++;
 		}
 		
 	}
 	
 	private static List<String> readFromChildren(String delim) {
 		List<String> list = ls(string);
 		String line = null;
 		int index = 1;
 
 		for (FileObject out : fromSlaves) {
 			Appendable buf = buf();
 			line = out.readLine();
 			while (!(line==null) && !line.trim().equals(delim.trim())){
 				print("FROM CHILD READ", index, line);
 				add(buf, line + "\n");
 				line = out.readLine();
 			}
 			list.add(str(buf)+"\n");
 			index++;
 		}
 		return list;
 		
 	}
 
 	static List<Socket> slaveSockets = ls(Socket.class);
 	static List<FileObject> toSlaves = ls(FileObject.class);
 	static List<FileObject> fromSlaves = ls(FileObject.class); 
 	private static void initProcess() throws Exception {
 		
 		if (!remote) {
 			List<File> classPath = Sys.classPath();
 	
 			String classpath = calculateClasspath(classPath);
 	
 			String args = join(' ', Sys.java().toString(), "-cp", classpath,
 				AutoBench.class.getName(), "--slave", "yes");
 
 			inout1 = runAsync(0, path, true, args + " --slave-id 1");
 			inout2 = runAsync(0, path, true, args + " --slave-id 2");
 			
 			processes = ls(inout1, inout2);
 			
 
 			fromSlaves = ls(inout1.getStdOut(),inout2.getStdOut());
 			toSlaves = ls(inout1.getStdIn(), inout2.getStdIn());
 		} else {
 			
 			if (clients==null) {
 				print ("The argument --clients is required if --remote is set");
 			}
 			for (String client : clients) {
 				String[] hostPort = split(client, ":");
 				String host = hostPort[0];
 				int port = toInt(hostPort[1]);
 				Socket socket = new Socket(host, port);
 				slaveSockets.add(socket);
 				toSlaves.add(open(socket.getOutputStream()));
 				fromSlaves.add(open(socket.getInputStream()));
 			}
 
 		}
 	}
 
 	private static String calculateClasspath(List<File> classPath) {
 		File abPath = null;
 		File facilePath = null;
 		for (File p : classPath) {
 			List<File> files = files(p, ".*com/examples/AutoBench.*");
 			if (files.size() > 0) {
 				abPath = p;
 			}
 			files = files(p, ".*org/facile/Facile.*");
 			if (files.size() > 0) {
 				facilePath = p;
 			}
 		}
 
 		String classpath = null;
 		if (abPath.equals(facilePath)) {
 			classpath = abPath.toString();
 		} else {
 			classpath = join(':', abPath, facilePath);
 		}
 		return classpath;
 	}
 
 	public static my runServer(int serverNum, int rate, File outDir, int runNum) {
 
 		String cmdLine;
 
 		if (serverNum == 1) {
 			cmdLine = httperfString(rate, host1, uri1, port1);
 		} else {
 			cmdLine = httperfString(rate, host2, uri2, port2);
 		}
 
 		if (dryRun) {
 			print("Dry run");
 			print("SERVER " + serverNum, mul(50, "*"));
 			print(cmdLine);
 			return null;
 		} else {
 
 			print("SERVER", serverNum, ":", mul(50, "*"));
 			if (echoCommandLine || verbose)
 				print("RUNNING:", cmdLine);
 			ProcessOut run = run(processTimeout * 60, path, verbose, cmdLine);
 			print("EXIT CODE for SERVER ", serverNum, ":", run.exit);
 			if (verbose)
 				print(run.stdout);
 
 			File outputFile = file(outDir, "run_" + runNum + "_out");
 			writeAll(outputFile, lines(cmdLine, run.toString()));
 
 			if (run.exit != 0) {
 				fprint(System.err, "NOT ABLE TO RUN PROGRAM EXIT CODE : ",
 						run.exit);
 				File errorFile = file(outDir, "run_" + runNum + "_err_"
 						+ run.exit);
 				writeAll(errorFile, lines(cmdLine, run.stderr));
 				rest(1000);
 			}
 
 			return parseOutput(run.stdout);
 		}
 
 	}
 
 	public static void parseTest() {
 		File file = file(cwf(), "src/com/examples/sample.txt");
 
 		String sample = readAll(file);
 
 		if (file != null) {
 			outputHeader();
 		}
 
 		outputResult(500, parseOutput(sample), parseOutput(sample));
 	}
 
 	private static void outputHeader() {
 		String header = null;
 
 		if (!shortForm) {
 			header = join('\t', "dem_req_rate", "req_rate_" + host1,
 					"con_rate_ch_" + host1, "min_rep_rate_" + host1,
 					"avg_rep_rate_" + host1, "max_rep_rate_ch_" + host1,
 					"stddev_rep_rate_" + host1, "resp_time_" + host1, "net_io_"
 							+ host1, "errors_" + host1, "status_100_" + host1,
 					"status_200_" + host1, "status_300_" + host1, "status_400_"
 							+ host1, "status_500_" + host1,
 					"req_rate_" + host2, "con_rate_ch_" + host2,
 					"min_rep_rate_" + host2, "avg_rep_rate_" + host2,
 					"max_rep_rate_ch_" + host2, "stddev_rep_rate_" + host2,
 					"resp_time_" + host2, "net_io_" + host2, "errors_" + host2,
 					"status_100_" + host2, "status_200_" + host2, "status_300_"
 							+ host2, "status_400_" + host2, "status_500_"
 							+ host2);
 		} else {
 			header = join('\t', "rate", "rate_" + host1, "io_" + host1, "errs_"
 					+ host1, "resp_time_" + host1, "rate_" + host2, "io_"
 					+ host2, "errs_" + host2, "resp_time_" + host2);
 
 		}
 
 		if (verbose)
 			print(header);
 
 		appendWriteLine(outputFile(), header);
 	}
 
 	private static void outputResult(int rate, my server1, my server2) {
 		
 		if (master) {
 			rate = rate / 2;
 		}
 
 		String result1 = createResultForServer(server1);
 		String result2 = createResultForServer(server2);
 
 		String result = join('\t', rate * numCall, result1, result2);
 		if (verbose)
 			print(result);
 
 		appendWriteLine(outputFile(), result);
 
 	}
 
 	private static File outputFile() {
 
 		File outFile = null;
 
 		if (file == null) {
 			outFile = file(outputFolder, "out.tsv");
 
 		} else {
 			if (!file.isAbsolute()) {
 				outFile = file(outputFolder, file.toString());
 			} else {
 				outFile = file;
 			}
 		}
 
 		return outFile;
 
 	}
 
 	private static String createResultForServer(my server) {
 		if (server == null) {
 			return mul(14, "**\t");
 		}
 
 		if (shortForm) {
 			
 			return join('\t', 
 					server.safe(req_rate), server.safe(net_io),
 					server.safe(error_total), server.safe(rep_time));
 
 			// server1.i(key, value)
 			// dem_req_rate req_rate_ch_resin con_rate_ch_resin
 			// min_rep_rate_ch_resin avg_rep_rate_ch_resin
 			// X req_rate conn_rate rep_rate_min rep_rate_avg
 
 		} else {
 			return join(
 					'\t',
 					server.safe(req_rate),
 					server.safe(conn_rate),
 					server.safe(rep_rate_min),
 					server.safe(rep_rate_avg),
 
 					// max_rep_rate_ch_resin stddev_rep_rate_ch_resin
 					// resp_time_ch_resin net_io_ch_resin errors_ch_resin
 					// rep_rate_max rep_rate_stdv rep_time net_io error_total
 					server.safe(rep_rate_max),
 					server.safe(rep_rate_stdv),
 					server.safe(rep_time),
 					server.safe(net_io),
 					server.safe(error_total),
 
 					// status_100_ch_resin status_200_ch_resin
 					// status_300_ch_resin status_400_ch_resin
 					// status_500_ch_resin
 					// status_100 status_200 status_300 status_400 status_500
 					server.safe(status_100), 
 					server.safe(status_200),
 					server.safe(status_300), 
 					server.safe(status_400),
 					server.safe(status_500)
 					
 					);
 		}
 	}
 
 	public static my parseOutput(String output) {
 		my $results = my(Results.class, all);
 
 		openString(IN, output);
 
 		while (IN()) {
 
 			if (ok("^Total: .*replies (\\d+)")) {
 				$results.i(replies, $1());
 			}
 			if (ok("^Errors:.*total (\\d+)")) {
 				$results.i(error_total, $1());
 			}
 			if (ok("/{start line}Connection rate: ({digit}+/.{digit})/")) {
 				$results.i(conn_rate, $1());
 			}
 			if (ok("^Request rate: (\\d+\\.\\d)")) {
 				$results.i(req_rate, $1());
 			}
 			if (ok("/^Reply rate .*min (/d+/./d) avg (/d+/./d) max (/d+/./d) stddev (/d+/./d)/")) {
 				$results.i(rep_rate_min, $1());
 				$results.i(rep_rate_avg, $2());
 				$results.i(rep_rate_max, $3());
 				$results.i(rep_rate_stdv, $4());
 			}
 			if (ok("/^Reply time {any} response ({digit}+/.{digit})/")) {
 				$results.i(rep_time, $1());
 			}
 			if (ok("/^Reply status{any}1xx=({digit}+) 2xx=({digit}+) 3xx=({digit}+) 4xx=({digit}+) 5xx=({digit}+){any}/")) {
 				$results.i(status_100, $1());
 				$results.i(status_200, $2());
 				$results.i(status_300, $3());
 				$results.i(status_400, $4());
 				$results.i(status_500, $5());
 			}
 
 			if (ok("/^Net I{fw}O: ({digit}+/.{digit})/")) {
 				$results.i(net_io, $1());
 			}
 
 		}		
 		return $results;
 
 	}
 
 	public static void main(String[] args) {
 		context(AutoBench.class, args, new Runnable() {
 			@Override
 			public void run() {
 				try {
 					runIt();
 				} catch (Exception ex) {
					print (ex.getMessage());
 					ex.printStackTrace();
 					System.exit(-777);
 				}
 			}
 		});
 	}
 }
