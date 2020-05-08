 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lunabeat.pooper.commands;
 
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceStateChange;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.TerminateInstancesResult;
 import com.lunabeat.dooper.ClusterConfig;
 import com.lunabeat.dooper.ClusterInstance;
 import com.lunabeat.dooper.ClusterList;
 import com.lunabeat.dooper.HadoopCluster;
 import com.lunabeat.dooper.MasterTimeoutException;
 import com.lunabeat.dooper.SCPException;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *
  * @author cory
  */
 public class AppCommand {
 
 	private static final Log LOG = LogFactory.getLog(AppCommand.class.getName());
 	private static final Map<String, CommandInfo> COMMANDS = initCommands();
 	private ClusterConfig _config;
 
 	public AppCommand(ClusterConfig config) {
 		_config = config;
 	}
 
 	private static Map<String, CommandInfo> initCommands() {
 		Map<String, CommandInfo> tmpMap = new HashMap<String, CommandInfo>();
 		tmpMap.put("list-clusters", new CommandInfo(0, null, "List all ec2-hadoop cluster names."));
 		tmpMap.put("delete-cluster", new CommandInfo(1, new String[]{"name:string"}, "Delete groups for ec2-cluster."));
 		tmpMap.put("launch-cluster", new CommandInfo(2, new String[]{"name:string", "size:instanceSize", "nodes:int"}, "Launch a new Hadoop cluster."));
 		tmpMap.put("launch-master", new CommandInfo(3, new String[]{"name:string", "size:instanceSize"}, "Launch a new Hadoop master."));
 		tmpMap.put("launch-slaves", new CommandInfo(4, new String[]{"name:string", "size:instanceSize", "nodes:int"}, "Launch slaves for existing cluster."));
 		tmpMap.put("terminate-cluster", new CommandInfo(5, new String[]{"name:string"}, "Terminate all instances in a  Hadoop cluster."));
 		tmpMap.put("terminate-slaves", new CommandInfo(6, new String[]{"name:string", "nodes:int"}, "Terminate slaves in a cluster."));
 		tmpMap.put("describe-cluster", new CommandInfo(7, new String[]{"name:string"}, "Get instance info for a cluster."));
 		tmpMap.put("push-file", new CommandInfo(8, new String[]{"clusterName:string", "instances:instances", "srcPath:string", "destPath:string"}, "Copy file to cluster machines."));
 		tmpMap.put("create-groups", new CommandInfo(9, new String[]{"clusterName:string"}, "Create security groups for cluster"));
 		tmpMap.put("get-login-command", new CommandInfo(10, new String[]{"target:string"}, "Get remote login command for master or specified instance id. This command is for external scripts."));
 		tmpMap.put("login", new CommandInfo(11, new String[]{"target:string"}, "Launch remote login for master or specific instance."));
 
 		return Collections.unmodifiableMap(tmpMap);
 	}
 
 	public static Set<String> commandNames() {
 		return COMMANDS.keySet();
 	}
 
 	public static CommandInfo commandInfo(String name) {
 		return COMMANDS.get(name);
 	}
 
 	public void runCommand(String commandName, String[] args) {
 
 		CommandInfo cInfo = COMMANDS.get(commandName);
 		if (cInfo == null) {
 			System.out.println("INVALID COMMAND: '" + commandName + "'.");
 			System.exit(1);
 		}
 		try {
 			checkCommandArgs(commandName, cInfo, args);
 		} catch (ArgumentException e) {
 			System.out.println(e.getProblem());
 			System.exit(1);
 		}
 		System.out.println("Running " + commandName);
 		switch (cInfo.getIndex()) {
 			case 0:
 				listClusters();
 				break;
 			case 1:
 				deleteCluster(args[0]);
 				break;
 			case 2:
 				launchCluster(args);
 				break;
 			case 3:
 				launchMaster(args);
 				break;
 			case 4:
 				launchSlaves(args);
 				break;
 			case 5:
 				terminateCluster(args[0]);
 				break;
 			case 6:
 				terminateSlaves(args);
 				break;
 			case 7:
 				describeCluster(args[0]);
 				break;
 			case 8:
 				pushFile(args);
 				break;
 			case 9:
 				createGroups(args[0]);
 				break;
 			case 10:
 				getLoginCommand(args[0]);
 				break;
 			case 11:
 				LOG.fatal("Direct call to login.");
 				System.out.println("COMMAND 'login' needs to be handled by external script for now.\nUse 'get-login-command' to retrieve command and exec in shell.");
 				break;
 			default:
 				throw new RuntimeException("Bad AppInfo index for '" + commandName + "' :" + cInfo.getIndex());
 		}
 	}
 
 	private void checkCommandArgs(String commandName, CommandInfo cInfo, String[] args) throws ArgumentException {
 
 		String[] requiredArgNames = cInfo.getRequiredArgNames();
 		String[] requiredArgTypes = cInfo.getRequiredArgTypes();
 
 		if (requiredArgNames == null) {
 			return;
 		}
 		if (args.length < requiredArgNames.length) {
 			StringBuilder sb = new StringBuilder("Command '");
 			sb.append(commandName).append("' requires ").
 					append(requiredArgNames.length).
 					append(" arguments ").
 					append(args.length).
 					append(" found.\nargs:");
 			for (int x = 0; x < requiredArgNames.length; x++) {
 				sb.append(" ").
 						append(requiredArgNames[x]).
 						append("(").
 						append(requiredArgTypes[x]).
 						append(")");
 			}
 			throw new ArgumentException(sb.toString());
 		}
 
 		for (int x = 0; x < requiredArgNames.length; x++) {
 			String argName = requiredArgNames[x];
 			String type = requiredArgTypes[x];
 			if ("string".contentEquals(type)) {
 				continue;
 			}
 			if ("int".contentEquals(type)) {
 				try {
 					Integer.parseInt(args[x]);
 				} catch (NumberFormatException e) {
 					throw new ArgumentException("Command '" + commandName + "' requires integer value for " + argName + ".");
 				}
 			} else if ("instanceSize".contentEquals(type)) {
 				if (!ClusterConfig.INSTANCE_TYPES.contains(args[x])) {
 					StringBuilder sb = new StringBuilder("Invalid instance size '").append(args[x]).append("'.\n").append("\tvalid sizes are:\n");
 					for (String s : ClusterConfig.INSTANCE_TYPES) {
 						sb.append("\t\t").append(s).append("\n");
 					}
 					throw new ArgumentException("Command '" + commandName + "'\n" + sb.toString());
 				}
 			} else if ("instances".contentEquals(type)) {
 
 				if (!ClusterConfig.INSTANCE_GROUP_TYPES.contains(args[x])) {
 					StringBuilder sb =
 							new StringBuilder("Invalid instance '").append(args[x]).
 							append("'.\n").append("\tvalid instances are:\n");
 					for (String s : ClusterConfig.INSTANCE_GROUP_TYPES) {
 						sb.append("\t\t").append(s).append("\n");
 					}
 					throw new ArgumentException("Command '" + commandName + "'\n" + sb.toString());
 				}
 			}
 
 
 		}
 	}
 
 	private void listClusters() {
 		Map<String, Map<String, List<Instance>>> out = ClusterList.getClusterMap(_config);
 		System.out.println("found running clusters:");
 		HashSet<String> emptyClusters = new HashSet<String>();
 		for (String clustername : out.keySet()) {
 			//System.out.println(clustername);
 			boolean empty = true;
 			for (String group : out.get(clustername).keySet()) {
 				if (out.get(clustername).get(group).size() < 1) {
 					emptyClusters.add(group.replace(HadoopCluster.MASTER_SUFFIX, ""));
 				} else {
 					System.out.println(group + " (" + out.get(clustername).get(group).size() + ")");
 					empty = false;
 				}
 				for (Instance i : out.get(clustername).get(group)) {
 					StringBuilder sb = new StringBuilder("\t");
 					sb.append(i.getInstanceId());
 					sb.append("\t");
 					sb.append(i.getInstanceType());
 					sb.append("\t");
 					sb.append(i.getState().getName());
 					//sb.append("\t");
 					//sb.append(i.getPublicDnsName());
 					System.out.println(sb.toString());
 				}
 				if (!empty) {
 					System.out.println();
 				}
 			}
 			if (!empty) {
 				System.out.println("-----------");
 			}
 		}
 		System.out.println("Found empty cluster security groups:");
 		for (String s : emptyClusters) {
 			System.out.println("\t" + s);
 		}
 	}
 
 	private void deleteCluster(String clusterName) {
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		if (!cluster.groupsExist()) {
 			System.out.println("Cluster '" + clusterName + "' does not exist.");
 			return;
 		}
 
 		if (cluster.removeSecurityGroups()) {
 			System.out.println("Deleted cluster '" + clusterName + "'.");
 		} else {
 			System.out.println("'" + clusterName + "' has instances and was not deleted.");
 		}
 
 	}
 
 	private void launchCluster(String[] args) {
 		String clusterName = args[0];
 		String instanceSize = args[1];
 		int nodes = Integer.parseInt(args[2]);
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		try {
 			System.out.println("launching master.");
 			RunInstancesResult mr = cluster.launchMaster(instanceSize);
 			if (mr == null) {
 				System.out.println("Launch master failed!");
 				System.exit(100);
 			}
 			Reservation r = mr.getReservation();
 			Instance i = r.getInstances().get(0);
 			if (i == null) {
 				System.out.println("Launch master failed! reservation id: " + r.getReservationId());
 				System.exit(100);
 			}
 			System.out.println("reservation id: " + r.getReservationId());
 			System.out.println("\tinstance: " + i.getInstanceId() + "\t" + i.getState().getName());
 
 			System.out.println("launching slaves (" + nodes + ").");
 			System.out.println("waiting for master to get address.");
 			RunInstancesResult sr = cluster.launchSlaves(nodes, instanceSize);
 			if (sr == null) {
 				System.out.println("Launch slaves failed!");
 				System.exit(100);
 			}
 			Reservation sres = sr.getReservation();
 			List<Instance> sis = sres.getInstances();
 			if (sis == null || sis.isEmpty()) {
 				System.out.println("Launch slaves failed! reservation id: " + sres.getReservationId());
 				System.exit(100);
 			}
 			System.out.println("reservation id: " + sres.getReservationId());
 			for (Instance si : sis) {
 				System.out.println("\tinstance: " + si.getInstanceId() + "\t" + si.getState().getName());
 			}
 			System.out.println("Success.");
 		} catch (IOException e) {
 			System.out.println("IOException during userdata file encoding.");
 			System.out.println(e.getMessage());
 			System.exit(100);
 		} catch (MasterTimeoutException e) {
 			System.out.println("Timed out waiting for master to start.\nDon't panic.\nTry: 'pooper launch-slaves "
 					+ e.group() + " " + e.size() + " " + e.howMany() + "' after master is running.\n" + "master id: " + e.masterId());
 			System.exit(0);
 		}
 	}
 
 	private void launchMaster(String[] args) {
 		String clusterName = args[0];
 		String instanceSize = args[1];
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		try {
 			System.out.println("launching master.");
 			RunInstancesResult mr = cluster.launchMaster(instanceSize);
 			if (mr == null) {
 				System.out.println("Launch master failed!");
 				System.exit(100);
 			}
 			Reservation r = mr.getReservation();
 			Instance i = r.getInstances().get(0);
 			if (i == null) {
 				System.out.println("Launch master failed! reservation id: " + r.getReservationId());
 				System.exit(100);
 			}
 			System.out.println("reservation id: " + r.getReservationId());
 			System.out.println("\tinstance: " + i.getInstanceId() + "\t" + i.getState().getName());
 			System.out.println("Success.");
 		} catch (IOException e) {
 			System.out.println("IOException during userdata file encoding.");
 			System.out.println(e.getMessage());
 			System.exit(100);
 		}
 	}
 
 	private void launchSlaves(String[] args) {
 		String clusterName = args[0];
 		String instanceSize = args[1];
 		int nodes = Integer.parseInt(args[2]);
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		try {
 			System.out.println("launching slaves (" + nodes + ").");
 			RunInstancesResult sr = cluster.launchSlaves(nodes, instanceSize);
 			if (sr == null) {
 				System.out.println("Launch slaves failed!");
 				System.exit(100);
 			}
 			Reservation sres = sr.getReservation();
 			List<Instance> sis = sres.getInstances();
 			if (sis == null || sis.isEmpty()) {
 				System.out.println("Launch slaves failed! reservation id: " + sres.getReservationId());
 				System.exit(100);
 			}
 			System.out.println("reservation id: " + sres.getReservationId());
 			for (Instance si : sis) {
 				System.out.println("\tinstance: " + si.getInstanceId() + "\t" + si.getState().getName());
 			}
 			System.out.println("Success.");
 		} catch (IOException e) {
 			System.out.println("IOException during userdata file encoding.");
 			System.out.println(e.getMessage());
 			System.exit(100);
 		} catch (MasterTimeoutException e) {
 			System.out.println("Timed out waiting for master to start.\nDon't panic.\nTry: 'pooper launch-slaves "
 					+ e.group() + " " + e.size() + " " + e.howMany() + "' after master is running.\n" + "master id: " + e.masterId());
 			System.exit(0);
 		}
 	}
 
 	private void terminateCluster(String clusterName) {
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		TerminateInstancesResult tr = cluster.terminateCluster();
 		if (tr == null || tr.getTerminatingInstances().isEmpty()) {
 			System.out.println("no instances terminated.");
 			System.exit(0);
 		}
 		System.out.println("Terminating " + tr.getTerminatingInstances().size() + " instances.");
 		for (InstanceStateChange i : tr.getTerminatingInstances()) {
 			System.out.println("\t" + i.getInstanceId() + " "
 					+ i.getPreviousState().getName()
 					+ " -> " + i.getCurrentState().getName());
 		}
 		System.out.println("Success.");
 	}
 
 	private void terminateSlaves(String[] args) {
 		String clusterName = args[0];
 		int nodes = Integer.parseInt(args[1]);
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		TerminateInstancesResult tr = cluster.terminateSlaves(nodes);
 		if (tr == null || tr.getTerminatingInstances().isEmpty()) {
 			System.out.println("no instances terminated.");
 			System.exit(0);
 		}
 		System.out.println("Terminating " + tr.getTerminatingInstances().size() + " instances.");
 		for (InstanceStateChange i : tr.getTerminatingInstances()) {
 			System.out.println("\t" + i.getInstanceId() + " "
 					+ i.getPreviousState().getName()
 					+ "-> " + i.getCurrentState().getName());
 		}
 		System.out.println("Success.");
 	}
 
 	private void describeCluster(String name) {
 		HadoopCluster cluster = new HadoopCluster(name, _config);
 		if (!cluster.groupsExist()) {
 			System.out.println("Cluster " + name + " not found.");
 			System.exit(0);
 		}
 		System.out.println(name);
 		System.out.println("---------------");
 		System.out.println("Master:");
 		System.out.println("-------");
 		ClusterInstance master = cluster.getMaster();
 		if (master == null) {
 			System.out.println("no master found.");
 		} else {
 			System.out.println("id:\t\t" + master.getInstance().getInstanceId());
 			System.out.println("state:\t\t" + master.getInstance().getState().getName());
 			System.out.println("size:\t\t" + master.getInstance().getInstanceType());
 			System.out.println("ami:\t\t" + master.getInstance().getImageId());
 			System.out.println("public:\t\t" + master.getInstance().getPublicDnsName());
 			System.out.println("privte:\t\t" + master.getInstance().getPrivateDnsName());
 			System.out.println("launch:\t\t" + master.getInstance().getLaunchTime().toString());
 			System.out.println("reserv:\t\t" + master.getReservationId());
 			System.out.println("groups:\t\t" + master.getSecurityGroups());
 		}
 		System.out.println();
 		System.out.println("Slaves:");
 		System.out.println("-------");
 		List<ClusterInstance> slaves = cluster.getSlaves();
 		if (master == null) {
 			System.out.println("no slaves found.");
 		} else {
 			for (ClusterInstance slave : slaves) {
 				System.out.println("id:\t\t" + slave.getInstance().getInstanceId());
 				System.out.println("state:\t\t" + slave.getInstance().getState().getName());
 				System.out.println("size:\t\t" + slave.getInstance().getInstanceType());
 				System.out.println("ami id:\t\t" + slave.getInstance().getImageId());
 				System.out.println("public:\t\t" + slave.getInstance().getPublicDnsName());
 				System.out.println("privat:\t\t" + slave.getInstance().getPrivateDnsName());
 				System.out.println("launch:\t\t" + slave.getInstance().getLaunchTime().toString());
 				System.out.println("reserv:\t\t" + slave.getReservationId());
 				System.out.println("groups:\t\t" + slave.getSecurityGroups());
 				System.out.println("---");
 			}
 		}
 
 	}
 
 	private void pushFile(String[] args) {
 		//clusterName:string","instances:instances","srcPath:string","destPath:string"
 		String clusterName = args[0];
 		String target = args[1];
 		String src = args[2];
 		String dest = args[3];
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		try {
 			if ("master".contentEquals(target) || "cluster".contentEquals(target)) {
 				System.out.println("Pushing to master.");
 				if (cluster.getMaster() == null) {
 					System.out.println("No master found.");
 				}
 				cluster.putFile(cluster.getMaster(), src, dest);
 				System.out.println("Copied to " + cluster.getMaster().getInstance().getInstanceId() + ".");
 			}
 			if ("slaves".contentEquals(target) || "cluster".contentEquals(target)) {
 				System.out.println("Pushing to slaves.");
 				List<ClusterInstance> slaves = cluster.getSlaves();
 				if (slaves == null) {
 					System.out.println("No slaves found.");
 				}
 				System.out.println("Copying to " + slaves.size() + " slaves.");
 				for (ClusterInstance slave : slaves) {
 					cluster.putFile(slave, src, dest);
 					System.out.println("Copied to " + slave.getInstance().getInstanceId() + ".");
 				}
 			}
 
 			System.out.println("Copied " + src + " to " + dest + " on " + target + ".");
 		} catch (SCPException scpe) {
 			boolean isMaster = scpe.getInstance().getInstance().getInstanceId().contentEquals(cluster.getMaster().getInstance().getInstanceId());
 			System.out.println("Error pushing to " + (isMaster ? "master" : "slave"));
 			System.out.println("InstanceId: " + scpe.getInstance().getInstance().getInstanceId());
 			System.out.println("message: " + scpe.getMessage());
 			if (scpe.getCause() != null) {
 				System.out.println("cause: " + scpe.getCause().getMessage());
 			}
 			System.exit(1);
 		}
 
 	}
 
 	private void createGroups(String clusterName) {
 		HadoopCluster cluster = new HadoopCluster(clusterName, _config);
 		cluster.createSecurityGroups();
 		System.out.println("Created groups for " + clusterName + ".");
 	}
 
 	private void getLoginCommand(String target) {
 		String host = null;
 		HadoopCluster cluster = new HadoopCluster(target, _config);
		if(cluster.groupsExist())
 			if(cluster.getMaster() != null)
 				host = cluster.getMaster().getInstance().getPublicDnsName();
		else if(target.startsWith(ClusterConfig.EC2_INSTANCE_PREFIX)){
 			DescribeInstancesResult ir = cluster.getInstanceForId(target);
			if(ir.getReservations().size() > 0)
				if(ir.getReservations().get(0).getInstances().size() > 0)
 					host = ir.getReservations().get(0).getInstances().get(0).getPublicDnsName();
 		}
 		if(host == null){
 			System.out.println("'" + target + "' is not a valid cluster name or instance id.\nexiting.");
 			System.exit(0);
 		}
 		StringBuilder sb = new StringBuilder("ssh -i")
 				.append(_config.get(ClusterConfig.KEYPAIR_FILE_KEY))
 				.append(" ")
 				.append(_config.get(ClusterConfig.USERNAME_KEY))
 				.append("@")
 				.append(host);
 		System.out.println(sb.toString());
 	}
 
 	public static class CommandInfo {
 
 		private String[] _requiredArgNames;
 		private String[] _requiredArgTypes;
 		private String _description;
 		private int _index = -1;
 
 		/**
 		 *
 		 * @param requiredArgs
 		 * @param description
 		 */
 		public CommandInfo(int index, String[] requiredArgs, String description) {
 			if (requiredArgs == null) {
 				_requiredArgNames = null;
 				_requiredArgTypes = null;
 			} else {
 				_requiredArgNames = new String[requiredArgs.length];
 				_requiredArgTypes = new String[requiredArgs.length];
 				for (int x = 0; x < requiredArgs.length; x++) {
 					String[] parts = requiredArgs[x].split(":");
 					if (parts.length != 2) {
 						throw new RuntimeException("Bad required arg given to CommandInfo - '" + requiredArgs[x] + "'.");
 					}
 					_requiredArgNames[x] = parts[0];
 					_requiredArgTypes[x] = parts[1];
 				}
 			}
 
 			_description = description;
 			_index = index;
 		}
 
 		/**
 		 * @return the description
 		 */
 		public String getDescription() {
 			return _description;
 		}
 
 		/**
 		 * @return the index
 		 */
 		public int getIndex() {
 			return _index;
 		}
 
 		/**
 		 *
 		 * @return array of required arg names.
 		 */
 		private String[] getRequiredArgNames() {
 			return _requiredArgNames;
 		}
 
 		/**
 		 * 
 		 * @return array of required arg types.
 		 */
 		private String[] getRequiredArgTypes() {
 			return _requiredArgTypes;
 		}
 	}
 }
