 package com.tomazkovacic.boilerpipe.thrift;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.apache.thrift.protocol.TBinaryProtocol;
 import org.apache.thrift.protocol.TCompactProtocol;
 import org.apache.thrift.protocol.TProtocolFactory;
 import org.apache.thrift.transport.TFramedTransport;
 import org.apache.thrift.transport.TNonblockingServerSocket;
 import org.apache.thrift.transport.TServerSocket;
 import org.apache.thrift.transport.TServerTransport;
 import org.apache.thrift.transport.TTransportException;
 import org.apache.thrift.transport.TTransportFactory;
 
 
 /**
  * A helper class with methods for parsing the command line arguments and 
  * constructing valid building blocks for a java thrift server. (Using best
  * practices from the hadoop hbase thrift server)
  * */
 public class ServerSettings{
 	
 	private static Log LOG = LogFactory.getLog(ServerSettings.class);
 	
 	/**
 	 * Default listening port
 	 * */
 	public static final int DEFAULT_PORT = 9999;
 	
 	public CommandLine cmd;
 	private Options options;
 	
 	/**
 	 * Parse command line arguments and return an instance of ServerSettings
 	 * utility
 	 * 
 	 * @param args
 	 * 		command line arguments from main()
 	 * @throws ParseException
 	 * */
 	public static ServerSettings parseArgs(String[] args) throws ParseException{
 		Options options = new Options();
         //TODO: address binding
         //options.addOption("b","bind",true,"Address to bind the Thrift server to. Not supported by the Nonblocking and HsHa server [default: 0.0.0.0]");
        options.addOption("p", "port", true, "Port to bind to [default: 9999]");
         options.addOption("f", "framed", false, "Use framed transport");
         options.addOption("c", "compact", false, "Use the compact protocol");
         options.addOption("h", "help", false, "Print help information");
 
         OptionGroup servers = new OptionGroup();
         servers.addOption(new Option("nonblocking", false,"Use the TNonblockingServer. This implies the framed transport."));
         servers.addOption(new Option("hsha", false,"Use the THsHaServer. This implies the framed transport."));
         servers.addOption(new Option("threadpool", false,"Use the TThreadPoolServer. This is the default."));
         options.addOptionGroup(servers);
 
         CommandLineParser parser = new PosixParser();
         CommandLine cmd = parser.parse(options, args);
 		
         return new ServerSettings(cmd, options);
 	}
 	
 	/**
 	 * Default constructor
 	 * 
 	 * @param cmd
 	 * 		parsed command line arguments
 	 * @param options
 	 * 		cli options
 	 * */
 	public ServerSettings(CommandLine cmd, Options options){
 		this.cmd  = cmd;
 		this.options = options;
 	}
 	
 	/**
 	 * Print help desctiptor to the stdout
 	 * */
 	public void printHelp(){
 		HelpFormatter formatter = new HelpFormatter();
 	    formatter.printHelp("Thrift", options);	
 	}
 	
 	/**
 	 * Get a valid server listening port. 
 	 * 
 	 * @return 
 	 * 		Returns default port if the argument can't be parsed.
 	 * */
 	public int getListeningPort(){
 		int port;
		if(cmd.hasOption("port")){
			
			try {
				port =  Integer.parseInt(cmd.getOptionValue("port"));
			} catch (NumberFormatException e) {
				LOG.warn("Could not parse the value provided for the port option," +
				" listening to the default port value instead ");
				port = DEFAULT_PORT;
			}
		}
		else{
			port = DEFAULT_PORT;
		}
 		
         LOG.info("listening to port:" + Integer.toString(port) );
 		return port;
 	}
 	
 	/**
 	 * Get protocol factory. 
 	 * 
 	 * @return
 	 * 		Use binary protocol if the "--compact" flag was not set
 	 * */
 	public TProtocolFactory getProtocolFactory(){
 		if(cmd.hasOption("compact")){
 			LOG.info("Using compact protocol");
 			return new TCompactProtocol.Factory();
 		}
 		else{
             LOG.info("Using binary protocol");
             return new TBinaryProtocol.Factory();
 		}
 	}
 	
 	
 	/**
 	 * Get transport factory
 	 * 
 	 * @return
 	 * 		Return framed transport for HsHa and non-blocking server. Otherwise
 	 * 		return the default TTransportFactory instance.
 	 * */
 	public TTransportFactory getTransportFactory(){
 		if (cmd.hasOption("nonblocking") || cmd.hasOption("hsha") || cmd.hasOption("framed")){
 			LOG.info("Using framed transport");
 			return new TFramedTransport.Factory();
 		}
 		else {
 			LOG.info("Using server dependant transport");
 			return new TTransportFactory();
 		}
 	}
 	
 	/**
 	 * Get server transport
 	 * 
 	 * @return
 	 * 		Nonblocking server socket for hsha and nonblocking server.
 	 * @throws TTransportException 
 	 * */
 	public TServerTransport getServerTransport() throws TTransportException{
 		int port = getListeningPort();
 		if (cmd.hasOption("nonblocking") || cmd.hasOption("hsha")){
 			LOG.info("Using non blocking server socket on port "+ Integer.toString(port));
 			return new TNonblockingServerSocket(port);
 		}
 		else{
 			LOG.info("Using server socket on port "+ Integer.toString(port));
 			return new TServerSocket(port);
 		}		
 	}
 	
 
 }
