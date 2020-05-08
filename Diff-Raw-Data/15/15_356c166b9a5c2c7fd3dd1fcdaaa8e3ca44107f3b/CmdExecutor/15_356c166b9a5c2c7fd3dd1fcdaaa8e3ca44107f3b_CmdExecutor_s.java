 package vcluster.ui;
 
 import java.util.StringTokenizer;
 
 import vcluster.engine.groupexecutor.VClusterExecutor;
 import vcluster.global.Config;
 import vcluster.plugin.PluginManager;
 
 /**
  * @author huangdada
  * 
  */
 public class CmdExecutor {
 
 	/**
 	 * if quit, it checks if monitoring process and vm manager process are still running
 	 */
 	public static void quit()
 	{
 		/* shutdown Manager first */
 		if (Config.monMan != null) Config.monMan.shutDwon();
 		if (Config.vmMan != null) Config.vmMan.shutDwon();
 	}
 
 	public static boolean isQuit(String aCmd)
 	{
 		String cmd = aCmd.trim();
 		if(Command.QUIT.contains(cmd)) {
 
 			/* shutdown Manager first */
 			if (Config.monMan != null) Config.monMan.shutDwon();
 			if (Config.vmMan != null) Config.vmMan.shutDwon();
 			
 			return true;
 		}
 
 		return false;
 	}
 	
 	public static boolean execute(String cmdLine)
 	{
 		StringTokenizer st = new StringTokenizer(cmdLine);
 		
 		String cmd = st.nextToken().trim();
 		
 		Command command = getCommand(cmd);
 		
 		// if (command == Command.NOT_DEFINED) return false;
 		
 		switch (command.getCmdGroup()) {
 		case VCLUSTER: return executeVcluster(command, cmdLine);
 		case CLOUD: return executeCloud(command, cmdLine);
 		case PROXY_SERVER: return executeProxy(command, cmdLine);
 		case NOT_DEFINED: return false;
 		}
 		
 		return false;
 	}
 	
 	private static boolean executeVcluster(Command command, String cmdLine)
 	{
 		
 		switch (command) {
 		case DEBUG_MODE:
 			return VClusterExecutor.debug_mode(cmdLine);
 		case VMMAN:
 			return VClusterExecutor.vmman(cmdLine);
 		case MONITOR:
 			return VClusterExecutor.monitor(cmdLine);
 		case CLOUDMAN:
 			return VClusterExecutor.cloudman(cmdLine);
 		case SHOW:
 			return VClusterExecutor.show(cmdLine);
 		case LOAD:
 			return VClusterExecutor.load(cmdLine);
 		case SET:
 			return VClusterExecutor.set(cmdLine);
 		case ENGMODE:
 			return VClusterExecutor.engmode(cmdLine);
 		case PLUGMAN:
 			return VClusterExecutor.plugman(cmdLine);
 		}
 		
 		return true;
 	}
 	
 
 	private static boolean executeProxy(Command command, String cmdLine)
 	{
 /*
 		try{
 			Config.proxyExecutor = PluginManager.bcPlugins.get(Config.batch_plugin);
 		}catch(NullPointerException ne){
 			
 			System.out.println("\nno proxyExecutor,please register proxyExecutor plugin!\n");
 			System.out.println("          [USAGE] : plugin <register plugin_name | list>\n");
 			return false;
 		}
 		*/
 		switch (command) {
 		case CHECK_POOL: return PluginManager.current_proxyExecutor.check_pool();
 		case CHECK_Q: return PluginManager.current_proxyExecutor.check_q();
		case CONDOR: return PluginManager.current_proxyExecutor.condor(cmdLine);
		case ONEVM: return PluginManager.current_proxyExecutor.onevm(cmdLine);
 		}
 		
 		return true;
 	}
 	
 	
 
 	private static boolean executeCloud(Command command, String cmdLine)
 	{
 
 		/*
 		RUN_INSTANCE (CMD_GROUP.CLOUD, "RunInstances, runinstance, ri, runinst, runins, run"),
 		START_INSTANCE (CMD_GROUP.CLOUD, "StartInstances, startinstance, si, startinst, startins, start"),
 		STOP_INSTANCE (CMD_GROUP.CLOUD, "StopInstances, stopinstance, stop"),
 		DESCRIBE_INSTANCE (CMD_GROUP.CLOUD, "DescribeInstances, describeinstance, din, dins, descinst, descins"),
 		TERMINATE_INSTANCE (CMD_GROUP.CLOUD, "TerminateInstances, terminateinstance, terminate, ti, kill, killins"),
 		DESCRIBE_IMAGE (CMD_GROUP.CLOUD, "DescribeImages, describeimage, dim, dimg, descimg"),
 		*/
 		
 		// command.toPrint();
 		
 		/* first check if a vm can be launched using cloud API
 		 * if so, call registered function (plug-in based).
 		 * if not, call REST API for a specified cloud system,
 		 * which is chosen from cloud system pool based on priority.
 		 */
 		switch (command) {
 		case RUN_INSTANCE: return PluginManager.current_cloudExecutor.run_instance(Config.cloudMan.getCurrentCloud());
 		case DESCRIBE_INSTANCE: return PluginManager.current_cloudExecutor.describe_instance(Config.cloudMan.getCurrentCloud(), cmdLine);
 		case TERMINATE_INSTANCE: return PluginManager.current_cloudExecutor.terminate_instance(Config.cloudMan.getCurrentCloud(), cmdLine);
 
 		}
 		
 		return true;
 	}
 	
 	public static Command getCommand(String aCmdLine) 
 	{
 		StringTokenizer st = new StringTokenizer(aCmdLine);
 		String aCmd = st.nextToken().trim();
 		
         for (Command cmd : Command.values())
         	if (cmd.contains(aCmd)) return cmd;
         
         return Command.NOT_DEFINED;
  	}
 }
