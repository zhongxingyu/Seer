 /*
  * Copyright 2010 syuu, and contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.dhtfox;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.Writer;
 import java.net.InetAddress;
 import java.net.Proxy;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import ow.dht.ByteArray;
 import ow.dht.DHT;
 import ow.dht.DHTFactory;
 import ow.messaging.Signature;
 import ow.messaging.upnp.Mapping;
 import ow.tool.dhtshell.commands.ClearCommand;
 import ow.tool.dhtshell.commands.GetCommand;
 import ow.tool.dhtshell.commands.HaltCommand;
 import ow.tool.dhtshell.commands.HelpCommand;
 import ow.tool.dhtshell.commands.InitCommand;
 import ow.tool.dhtshell.commands.LocaldataCommand;
 import ow.tool.dhtshell.commands.PutCommand;
 import ow.tool.dhtshell.commands.QuitCommand;
 import ow.tool.dhtshell.commands.RemoveCommand;
 import ow.tool.dhtshell.commands.ResumeCommand;
 import ow.tool.dhtshell.commands.SetSecretCommand;
 import ow.tool.dhtshell.commands.SetTTLCommand;
 import ow.tool.dhtshell.commands.StatusCommand;
 import ow.tool.dhtshell.commands.SuspendCommand;
 import ow.tool.emulator.EmulatorControllable;
 import ow.tool.util.shellframework.Command;
 import ow.tool.util.shellframework.Interruptible;
 import ow.tool.util.shellframework.MessagePrinter;
 import ow.tool.util.shellframework.Shell;
 import ow.tool.util.shellframework.ShellServer;
 import ow.tool.util.toolframework.AbstractDHTBasedTool;
 
 @SuppressWarnings("unchecked")
 public class DHTFox extends AbstractDHTBasedTool<String> implements
 		EmulatorControllable, Interruptible {
 	private final static String COMMAND = "dhtfox"; // A shell/batch script
 													// provided as
 													// bin/owdhtshell
 	private final static int SHELL_PORT = -1;
 	private final static int HTTP_PORT = 8080;
 	public final static String ENCODING = "UTF-8";
 	@SuppressWarnings("unchecked")
 	private final static Class/* Command<<DHT<String>>> */[] COMMANDS = {
 			StatusCommand.class, InitCommand.class, GetCommand.class,
 			PutCommand.class, RemoveCommand.class, SetTTLCommand.class,
 			SetSecretCommand.class, LocaldataCommand.class,
 			// SourceCommand.class,
 			HelpCommand.class, QuitCommand.class, HaltCommand.class,
 			ClearCommand.class, SuspendCommand.class, ResumeCommand.class };
 
 	private final static List<Command<DHT<String>>> commandList;
 	private final static Map<String, Command<DHT<String>>> commandTable;
 
 	static {
 		commandList = ShellServer.createCommandList(COMMANDS);
 		commandTable = ShellServer.createCommandTable(commandList);
 		SLF4JBridgeHandler.install();
 	}
 
 	private static final Logger logger = LoggerFactory.getLogger(DHTFox.class);
 	public static final Proxy PROXY_SETTING = Proxy.NO_PROXY;
 	public static final int HTTP_REQUEST_TIMEOUT = 1000;
 	private ByteArray hashedSecret;
 	private DHT<String> dht = null;
 	private HTTPServer http = null;
 	private boolean upnpEnable = true;
 	private Mapping httpMapping;
 	private ExecutorService putExecutor = Executors.newCachedThreadPool();
 	private ScheduledExecutorService maintenanceExecutor = Executors
 			.newScheduledThreadPool(1);
 
 	private Thread mainThread = null;
 	private UPnP upnp;
 
 	protected void usage(String command) {
 		super.usage(command, null);
 	}
 
 	public static void main(String[] args) throws Exception {
 		(new DHTFox()).start(args);
 	}
 
 	protected void start(String[] args) throws Exception {
 		this.init(args, true);
 	}
 
 	public Writer invoke(String[] args, PrintStream out) {
 		try {
 			this.init(args, false);
 		} catch (Exception e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private void init(String[] args, boolean interactive) throws Exception {
 		int shellPort = SHELL_PORT;
 		String secret = null;
 		String logbackxml = "logback.xml";
 		int httpPort = HTTP_PORT;
 
 		this.mainThread = Thread.currentThread();
 		
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				shutdown();
 				interrupt();
 			}
 		});
 		
 		Options opts = new Options();
 		// from super
 		opts.addOption("s", "selfaddress", true, "self IP address");
 		opts.addOption("N", "no-upnp", false,
 				"disable UPnP address port mapping");
 		// from Main
 		opts.addOption("p", "shellport", true, "shell port number");
 		// original
 		opts.addOption("x", "secret", true, "secret");
 		opts.addOption("l", "logbackxml", true, "path of logback.xml");
 		opts.addOption("H", "httport", true, "http port number");
 
 		CommandLineParser parser = new PosixParser();
 		CommandLine cmd = null;
 		try {
 			cmd = parser.parse(opts, args);
 		} catch (ParseException e) {
 			HelpFormatter help = new HelpFormatter();
 			help.printHelp("DHTFox", opts, true);
 			System.exit(1);
 		}
 
 		parser = null;
 		opts = null;
 
 		String optVal;
 		if (cmd.hasOption('N'))
 			this.upnpEnable = false;
 		optVal = cmd.getOptionValue('p');
 		if (optVal != null) {
 			shellPort = Integer.parseInt(optVal);
 		}
 		optVal = cmd.getOptionValue('x');
 		if (optVal != null)
 			secret = optVal;
 		else {
 			System.err.println("secret required");
 			System.exit(1);
 		}
 		optVal = cmd.getOptionValue('l');
 		if (optVal != null)
 			logbackxml = optVal;
 		optVal = cmd.getOptionValue('H');
 		if (optVal != null)
 			httpPort = Integer.parseInt(optVal);
 
 		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
 		JoranConfigurator configurator = new JoranConfigurator();
 		configurator.setContext(lc);
 		lc.reset();
 		configurator.doConfigure(logbackxml);
 
 		this.hashedSecret = new ByteArray(secret.getBytes(ENCODING))
 				.hashWithSHA1();
 		dht = super.initialize(Signature.APPLICATION_ID_DHT_SHELL,
 				(short) 0x10000, DHTFactory.getDefaultConfiguration(), COMMAND,
 				cmd);
 
 		cmd = null;
 
 		dht.setHashedSecretForPut(hashedSecret);
 		
 		LocalResponseCache.installResponseCache();
 		
 		upnp = new UPnP(upnpEnable);
 		InetAddress selfAddress = upnp.getSelfAddress();
 		
 		LocalResponseCache.putAllCaches(dht, httpPort, putExecutor, selfAddress);
 
 		maintenanceExecutor.scheduleAtFixedRate(new LocalDataMaintenanceTask(
 				dht, httpPort, selfAddress), 60, 60, TimeUnit.SECONDS);
 
 		http = new HTTPServer(httpPort, dht, PROXY_SETTING,
 				HTTP_REQUEST_TIMEOUT, putExecutor, selfAddress);
 		http.bind();
 		
 		if (upnpEnable) {
 			httpMapping = new Mapping(httpPort,
 					InetAddress.getLocalHost().getHostAddress(), httpPort,
 					Mapping.Protocol.TCP, "DHTFox httpd");
 			upnp.addMapping(httpMapping);
 		}
 
 		// start a ShellServer
 		ShellServer<DHT<String>> shellServ = new ShellServer<DHT<String>>(
 				commandTable, commandList, new ShowPromptPrinter(),
 				new NoCommandPrinter(), null, dht, shellPort, null);
 		shellServ.addInterruptible(this);
 
 		try {
 			Thread.sleep(Long.MAX_VALUE);
 		} catch (InterruptedException e) {
 			logger.warn(e.getMessage(), e);
 		}
 	}
 
 	public boolean startWithoutException(String secret, boolean upnpEnable,
 			String bootstrapNode, String localip, int dhtPort, int httpPort,
 			int shellPort, String logbackFilePath) {
 		try {
 			start(secret, upnpEnable, bootstrapNode, localip, dhtPort,
 					httpPort, shellPort, logbackFilePath);
 		} catch (Exception e) {
 			logger.warn(e.getMessage(), e);
 			return false;
 		}
 		return true;
 	}
 
 	public void start(String secret, boolean upnpEnable, String bootstrapNode,
 			String localip, int dhtPort, int httpPort, int shellPort,
 			String logbackFilePath) throws Exception {
 
 	}
 
 	public void interrupt() {
 		if (this.mainThread != null
 				&& !this.mainThread.equals(Thread.currentThread()))
 			this.mainThread.interrupt();
 	}
 
 	private static class ShowPromptPrinter implements MessagePrinter {
 		public void execute(PrintStream out, String hint) {
 			out.print("Ready." + Shell.CRLF);
 			out.flush();
 		}
 	}
 
 	private static class NoCommandPrinter implements MessagePrinter {
 		public void execute(PrintStream out, String hint) {
 			out.print("No such command");
 
 			if (hint != null)
 				out.print(": " + hint);
 			else
 				out.print(".");
 			out.print(Shell.CRLF);
 
 			out.flush();
 		}
 	}
 
 	 protected void shutdown() { 
 		 putExecutor.shutdownNow();
 		 logger.info("shutdown putExecutor");
 		 maintenanceExecutor.shutdownNow();
 		 logger.info("shutdown maintenanceExecutor");
 		 http.stop();
 		 logger.info("shutdown httpd");
 		 upnp.deleteMapping(httpMapping);
 		 logger.info("delete upnp mapping");
 	 }
 }
