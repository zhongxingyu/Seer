 package ru.spbstu.telematics.flowgen;
 
 
 import ru.spbstu.telematics.flowgen.openflow.OneIfaceFirewallRule;
 
 import java.util.Set;
 import java.util.TreeSet;
 
 public class FlowGenMain {
 
 	public static void main(String[] args) {
 
 		Set<Integer> ports = new TreeSet<Integer>();
 		ports.add(2);
 		ports.add(3);
 		ports.add(5);
 		ports.add(4);
 
 		Object[] portsArr = ports.toArray();
		for (Object aPortsArr : portsArr) {
			System.out.println(aPortsArr.toString());
 		}
 
 		OneIfaceFirewallRule rule = new OneIfaceFirewallRule("00:00:82:39:d8:7f:90:4c", 1, ports);
 
 		System.out.println(rule.getOutPortsString());
 
 	}
 }
