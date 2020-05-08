 package iTests.framework.utils;
 
 import org.openqa.selenium.NoSuchElementException;
 import org.openspaces.admin.machine.Machine;
 import org.utils.wanem.Constants;
 import org.utils.wanem.Constants.DisconnectType;
 import org.utils.wanem.RuleSet;
 import org.utils.wanem.WANEM;
 import org.utils.wanem.WANemPage;
 import org.utils.wanem.modes.AdvancedMode;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /*
 * How to start wanem on 192.168.9.197:
 * reboot the machine (called WANem on xen server), on startup refuse DHCP.
 * on ip prompt enter 192.168.9.197 and skip everything else.
 */
 public class WANemUtils {
 	
 	private static AdvancedMode advMode;
 	private static final int DISCONNECT_TIME = 900;
     private static final String WANEM_IP = "192.168.9.197";
     
     public static final String SSH_USERNAME = "tgrid";
     public static final String SSH_PASSWORD = "tgrid";
     public static final int SSH_TIMEOUT = 60000;
     
     private static WANemPage page;
 
 	public static void init(){
 		page = WANEM.getPage();
 		advMode = page.switchToAdvancesMode();
 	}
 	
 	public static void destroy() {
 		page.close();
 	}
 	
 	private static AdvancedMode getAdvMode(){
 		if (advMode == null)
 				init();
 		return advMode;
 	}
 	
 
 	public static void disconnect(Machine machine, List<Machine> machinesToDisconnectFrom){
 		
 		RuleSet defaultRuleSet = getAdvMode().getDefaultRuleSet();
 		List<String[]> pairs = machinesToPairs(machine, machinesToDisconnectFrom);
 		for (int i = 0 ; i <= pairs.size() - 1 ; i++) {
 			String[] pair = pairs.get(i);
 			if (i == 0) {
 				addDisconnectRule(pair[0], pair[1], defaultRuleSet);
 				getAdvMode().applySettings();
 			}
 			else {
 				addDisconnectRule(pair[0], pair[1], getAdvMode().addRuleSet());
 				getAdvMode().applySettings();
 			}
 		}
 		
 		/* wait for 5 seconds before returning to ensure the disconnection was applied */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void createDelay(long delayTime){		
 		createDelay(delayTime, 0);
 	}
 	
 	public static void createDelay(long delayTime, long jitter){
 		
 		RuleSet defaultRuleSet = getAdvMode().getDefaultRuleSet();
 		addDelayRule(delayTime, jitter, defaultRuleSet);
 		getAdvMode().applySettings();
 
 		/* wait for 5 seconds before returning to ensure the delay was applied */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void createPacketLoss(double packetLoss) {
 		RuleSet defaultRuleSet = getAdvMode().getDefaultRuleSet();
 		addPacketLossRule(packetLoss, defaultRuleSet);
 		getAdvMode().applySettings();
 		
 		/* wait for 5 seconds before returning to ensure the rule was applied */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void createDuplication(double duplication) {
 		RuleSet defaultRuleSet = getAdvMode().getDefaultRuleSet();
 		addDuplicationRule(duplication, defaultRuleSet);
 		getAdvMode().applySettings();
 		
 		/* wait for 5 seconds before returning to ensure the rule was applied */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void disconnectOuterHost(List<Machine> innerMachines, String outerHost){
 		RuleSet defaultRuleSet = getAdvMode().getDefaultRuleSet();
 		
 		for (int i = 0 ; i < innerMachines.size(); i++) {
 			String innerHost = innerMachines.get(i).getHostAddress();
 			if (i == 0) {
 				addDisconnectRule(innerHost, outerHost, defaultRuleSet);
 				getAdvMode().applySettings();
 			}
 			else {
 				addDisconnectRule(innerHost, outerHost, getAdvMode().addRuleSet());
 				getAdvMode().applySettings();
 			}
 		}
 		
 		/* wait for 5 seconds before returning to ensure the disconnection was applied */
 		try {
 			Thread.sleep(5000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void reset(){
 		getAdvMode().resetSetting();
 		
 	}
 	
 	private static List<String[]> machinesToPairs(Machine machine, List<Machine> machines){
 		List<String[]> pairs = new ArrayList<String[]>();
 		for (int i = 0; i < machines.size(); i++) {
 			if(!machine.equals(machines.get(i))){
 				String [] pair = {machine.getHostAddress(), machines.get(i).getHostAddress()};
 				pairs.add(pair);	
 			}
 		}
 		return pairs;
 	}
 	
 	private static void addDisconnectRule(String src, String dst, RuleSet ruleSet) {
 		LogUtils.log("adding a disconnection rule between src : " + src + " and target : " + dst);
 		chooseBandwidth(ruleSet);
         ruleSet.ipSourceAdress(src).ipDestAdress(dst).ipSourceSubnet("32")
         .ipDestSubnet("32")
         .getRandomDisconnect().disconnectType(DisconnectType.icmpHostUnreachable)
 		.mttfLow(1).mttfHigh(2).mttrLow(DISCONNECT_TIME).mttrHigh(DISCONNECT_TIME + 1);
 	}
 	
 	private static void addDelayRule(long delayTime, long jitter, RuleSet ruleSet) {
 		
 		LogUtils.log("adding a delay rule throughout the network");
 		chooseBandwidth(ruleSet);
 		ruleSet.getDelay().time(delayTime).jitter(jitter);
 	}
 	
 	private static void addPacketLossRule(double packetLoss, RuleSet ruleSet) {
 		
 		LogUtils.log("adding a packet loss rule of " + packetLoss + "% throughout the network");
 		chooseBandwidth(ruleSet);
 		ruleSet.getLoss().loss(packetLoss);
 	}
 
 	private static void addDuplicationRule(double duplication, RuleSet ruleSet) {
 		
 		LogUtils.log("adding a duplication rule throughout the network");
 		chooseBandwidth(ruleSet);		
 		ruleSet.getDuplication().duplication(duplication);
 	}
 
 	private static void chooseBandwidth(RuleSet ruleSet) {
 		try{
 			ruleSet.bandwidth(Constants.Bandwidth.fastEthernet);			
 		}
 		catch(NoSuchElementException e){
 			LogUtils.log("element with text '" + Constants.Bandwidth.fastEthernet + "' not found. Trying again with '" + Constants.Bandwidth.fastEthernetOption2 + "'");
 			ruleSet.bandwidth(Constants.Bandwidth.fastEthernetOption2);
 		}
 	}
 	
 	public static void addRoutingTableEntries(String[] ips) {
 		for (String source : ips) {
 			for (String target : ips) {
 				if (!target.equals(source)) addRoute(source, target);
 			}
 		}
 	}
 
     private static void addRoute(String srcIP, String dstIP) {
//        SSHUtils.runCommand(srcIP, 60000, addRouteCommand(dstIP, WANEM_IP), SSH_USERNAME, SSH_PASSWORD);
        SSHUtils.runCommand(srcIP, 60000, "cd /export/tgrid/nirb/; mkdir fgfg", SSH_USERNAME, SSH_PASSWORD);

     }
     
     private static String addRouteCommand(String dstIP, String routeThroughIP) {
         return "sudo /sbin/route add -host " + dstIP + " netmask 0.0.0.0 gw " + routeThroughIP;
     }
 
     public static void removeRoutingTableEntries(String[] ips) {
         for (String source : ips) {
             for (String target : ips) {
                 if (!target.equals(source)) removeRoute(source, target);
             }
         }
     }
 
 	private static void removeRoute(String srcIP, String dstIP) {
         SSHUtils.runCommand(srcIP, 60000, removeRouteCommand(dstIP, WANEM_IP), SSH_USERNAME, SSH_PASSWORD);
 
 	}
 
     private static String removeRouteCommand(String dstIP, String routeThroughIP) {
         return "sudo /sbin/route del -host " + dstIP + " netmask 0.0.0.0 gw " + routeThroughIP;
     }

//    public static void main(String[] args) {
//        String[] ips = {"pc-lab105","pc-lab106","pc-lab107","pc-lab108"};
//        addRoutingTableEntries(ips);
//    }
 }
