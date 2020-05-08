 package com.hapiware.jmx;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 import sun.jvmstat.monitor.HostIdentifier;
 import sun.jvmstat.monitor.MonitorException;
 import sun.jvmstat.monitor.MonitoredHost;
 import sun.jvmstat.monitor.MonitoredVm;
 import sun.jvmstat.monitor.MonitoredVmUtil;
 import sun.jvmstat.monitor.VmIdentifier;
 import sun.management.ConnectorAddressLink;
 
 public class LogLevel
 {
 	private static final String LOGGING_NAME = "java.util.logging:type=Logging";
 	
 
 	public static void main(String[] args)
 	{
 		if(args.length == 0 || args.length > 4)
 			usage();
 		
 		if((args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) && args.length != 1)
 			usage();
 		
 		if((args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("print")) && args.length != 3)
 			usage();
 		
 		if((args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("set")) && args.length != 4)
 			usage();
 		
 		if(
 			args[0].equalsIgnoreCase("-?") ||
 			args[0].equalsIgnoreCase("-h") ||
 			args[0].equalsIgnoreCase("-help") ||
 			args[0].equalsIgnoreCase("--help")
 		)
 			usage();
 		
 		if(args[0].equalsIgnoreCase("l") || args[0].equalsIgnoreCase("list")) {
 			listJvms();
 		} else
 			if(args[0].equalsIgnoreCase("p") || args[0].equalsIgnoreCase("print")) {
 				try {
 					Integer pid = Integer.valueOf(args[1]);
 					printLevels(pid, args[2]);
 				}
 				catch(NumberFormatException ex) {
 					System.out.println(args[1] + " was not recognized as PID.");
 					usage();
 				}
 			} else
 				if(args[0].equalsIgnoreCase("s") || args[0].equalsIgnoreCase("set")) {
 					try {
 						Integer pid = Integer.valueOf(args[1]);
 						setLevels(pid, args[2], args[3]);
 					}
 					catch(NumberFormatException ex) {
 						System.out.println(args[1] + " was not recognized as PID.");
 						usage();
 					}
 				} else {
 					System.out.println("Command (" + args[0] + ") not recognized.");
 					usage();
 				}
 	}
 	
 	private static void usage()
 	{
 		final String logLevel = "java -jar loglevel-1.0.0.jar";
 		System.out.println("Usage: " + logLevel + " [-? | -h | -help | --help]");
 		System.out.println("       " + logLevel + " CMD");
 		System.out.println();
 		System.out.println("       CMD:");
 		System.out.println("          l");
 		System.out.println("          list");
 		System.out.println("              Lists PIDs and names of connectable JVMs.");
 		System.out.println("              PID starting with asterix (*) means that JMX agent is not runnig for that JVM.");
 		System.out.println();
 		System.out.println("          p PID PATTERN");
 		System.out.println("          print PID PATTERN");
 		System.out.println("              Prints current logging levels for all loggers matching PATTERN");
 		System.out.println("              (Java RE) for a JVM process PID.");
 		System.out.println();
 		System.out.println("          s PID PATTERN LEVEL");
 		System.out.println("          set PID PATTERN LEVEL");
 		System.out.println("              Sets a new logging level LEVEL for all loggers matching PATTERN");
 		System.out.println("              (Java RE) for a JVM process PID.");
 		System.out.println();
 		System.out.println("Examples:");
 		System.out.println("    " + logLevel + " -?");
 		System.out.println("    " + logLevel + " list");
 		System.out.println("    " + logLevel + " p 50001 ^.+");
 		System.out.println("    " + logLevel + " set 50001 ^com\\.hapiware\\.Test.* INFO");
 		System.out.println();
 		System.exit(0);
 	}
 	
 	private static void listJvms()
 	{
 		try {
 			MonitoredHost mh = MonitoredHost.getMonitoredHost(new HostIdentifier((String)null)); 
 			Set<?> vms = mh.activeVms();
 			for(Iterator<?> it = vms.iterator(); it.hasNext(); /* empty */) {
 				Integer pid = (Integer)it.next();
 				MonitoredVm vm = mh.getMonitoredVm(new VmIdentifier(pid.toString()));
 				String commandLine = MonitoredVmUtil.commandLine(vm);
 				if(commandLine.toLowerCase().contains("loglevel"))
 					continue;
 				
 				String serviceUrl = ConnectorAddressLink.importFrom(pid);
 				System.out.println((serviceUrl == null ? "*" : "")  + pid + " " + commandLine);
 				vm.detach();
 			}
 		}
 		catch(URISyntaxException e) {
 			e.printStackTrace();
 		}
 		catch(MonitorException e) {
 			e.printStackTrace();
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		catch(NullPointerException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static MBeanServerConnection getConnection(Integer pid)
 	{
 		MBeanServerConnection connection = null;
 		try {
 			String serviceUrl = ConnectorAddressLink.importFrom(pid);
 			if(serviceUrl == null)
 				return null;
 			
 			JMXServiceURL jmxUrl = new JMXServiceURL(serviceUrl);
 			JMXConnector connector = JMXConnectorFactory.connect(jmxUrl);
 			connection = connector.getMBeanServerConnection();
 		}
 		catch(IOException e) {
			System.out.println("Process " + pid + " not found.");
			System.exit(-1);
 		}
 		return connection;
 	}
 	
 	private static String getLoggerLevel(
 		MBeanServerConnection connection,
 		ObjectName name,
 		String loggerName
 	)
 		throws 
 			InstanceNotFoundException,
 			MBeanException,
 			ReflectionException,
 			IOException
 	{
 		String level = 
 			(String)connection.invoke(
 				name,
 				"getLoggerLevel",
 				new String[] { loggerName },
 				new String [] { "java.lang.String" }
 			);
 		if(level.trim().length() == 0)
 			level = "Not defined";
 		return level;
 	}
 	
 	private static void printLevels(Integer pid, String loggerNamePattern)
 	{
 		try {
 			MBeanServerConnection connection = getConnection(pid);
 			if(connection == null) {
 				System.out.println("Cannot connect to " + pid + ". JMX agent is not running.");
 				System.out.println(
 					"Start target JVM with -Dcom.sun.management.jmxremote or" 
 					+ " if you are running JVM 1.6 or later use startJmx service."
 				);
 				System.exit(-1);
 			}
 			Pattern pattern = Pattern.compile(loggerNamePattern);
 			ObjectName name = new ObjectName(LOGGING_NAME);
 			String[] loggerNames = (String[])connection.getAttribute(name, "LoggerNames");
 			for(String ln : loggerNames) {
 				if(pattern.matcher(ln).matches())
 					System.out.println(ln + " : " + getLoggerLevel(connection, name, ln));
 			}
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		catch(MalformedObjectNameException e) {
 			e.printStackTrace();
 		}
 		catch(NullPointerException e) {
 			e.printStackTrace();
 		}
 		catch(InstanceNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch(ReflectionException e) {
 			e.printStackTrace();
 		}
 		catch(AttributeNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch(MBeanException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void setLevels(Integer pid, String loggerNamePattern, String level)
 	{
 		try {
 			MBeanServerConnection connection = getConnection(pid);
 			if(connection == null) {
 				System.out.println("Cannot connect to " + pid);
 				System.exit(-1);
 			}
 			Pattern pattern = Pattern.compile(loggerNamePattern);
 			ObjectName name = new ObjectName(LOGGING_NAME);
 			String[] loggerNames = (String[])connection.getAttribute(name, "LoggerNames");
 			for(String ln : loggerNames) {
 				if(pattern.matcher(ln).matches()) {
 					String oldLevel = getLoggerLevel(connection, name, ln);
 					connection.invoke(
 						name,
 						"setLoggerLevel",
 						new String[] { ln, level },
 						new String [] { "java.lang.String", "java.lang.String" }
 					);
 					System.out.println(ln + " : " + oldLevel + " -> " + level);
 				}
 			}
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		catch(MalformedObjectNameException e) {
 			e.printStackTrace();
 		}
 		catch(NullPointerException e) {
 			e.printStackTrace();
 		}
 		catch(InstanceNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch(ReflectionException e) {
 			e.printStackTrace();
 		}
 		catch(AttributeNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch(MBeanException e) {
 			e.printStackTrace();
 		}
 	}
 }
