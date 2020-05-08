 package org.bitducks.spoofing.core;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import jpcap.packet.Packet;
 
 public class Policy {
 	private List<Rule> rules = new LinkedList<Rule>();
 	private boolean strict = false;
 	
 	public Policy() {
 		
 	}
 	
 	public Policy(boolean strict) {
 		this.strict = strict;
 	}
 	
 	public void addRule(Rule r) {
 		this.rules.add(r);
 	}
 	
 	public boolean removeRule(Rule r) {
 		return this.rules.remove(r);
 	}
 	
 	public boolean checkIfPolicyValid(Packet p) {
 		
 		for (Rule rule : this.rules) {
 			
 			boolean valid = rule.checkRule(p);
 			
 			if (valid && !this.strict) {
 				return true;
			} else if(!valid && this.strict) {
 				return false;
 			}
 			
 		}
 		
		return true;
		
 	}
 }
