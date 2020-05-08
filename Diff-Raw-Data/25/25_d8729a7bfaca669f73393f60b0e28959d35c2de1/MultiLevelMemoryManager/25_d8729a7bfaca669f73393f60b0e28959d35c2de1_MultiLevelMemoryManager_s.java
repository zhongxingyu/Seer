 package mmu;
 
 import java.util.HashMap;
 
 import mmu.policy.ChainedLeastRecentlyUsedReplacementPolicy;
 import mmu.policy.ChainedLeastRecentlyUsedReplacementPolicy.Chain;
 
 public class MultiLevelMemoryManager extends MemoryManager {
 
 	private Chain default_chain = null;
 	
 	public MultiLevelMemoryManager(int capacity) {
 		super(new ChainedLeastRecentlyUsedReplacementPolicy(), capacity);
 		this.policy = (ChainedLeastRecentlyUsedReplacementPolicy)super.policy;
 		this.default_chain = this.policy.love();
 		
 		this.chains.put(this.default_chain.id(), this.default_chain);
 	}
 	
 	private Chain evict_page() {
 		Chain c = null;
 		long minimum = System.currentTimeMillis() + 1000;
 		
 		this.sample++;
 		for (Chain chain : this.chains.values()) {
 			if (chain.timestamp(this.sample) < minimum && chain.size() > 0) {
 				minimum = chain.timestamp(this.sample);
 				c = chain;
 			}
 		}
 		
 		return c;
 	}
 	
 	@Override
 	public void access(int address) {
 		MemoryPage page = super.getFromMemory(address); // this.memory.get(address);
 		Chain chain = null;
 		
 		int position = 0;
 		
 		if (page == null) {
 			if (super.isFull()) { // (this.memory.size() >= this.size) {
 				chain = this.evict_page();
				page = chain.evict();
 				position = super.evict(page); //this.memory.remove(page.address);
 			}
 			
 			page = super.getFromDisk(address); // this.get(address);
 			page.updateLoadStats();
 			page.updateRefStats();
 			
			super.memory[position] = page; //  this.memory.put(address, page);
 			this.policy.love(page, this.default_chain.id());
 		} else {
 			page.updateRefStats();
 		}
 	}
 	
 	public Chain chain() {
 		Chain c = this.policy.love();
 		this.chains.put(c.id(), c);
 		return c;
 	}
 
 	public void love(int page, int l) {
 		MemoryPage p = super.getFromMemory(page); //this.memory.get(page);
 		
 		if (p.link != -1) {
 			this.policy.hate(p, p.link);
 		}
 		
 		this.policy.love(p, l);
 	}
 	
 	public void love(int page, int l, boolean pin) {
 		MemoryPage p = super.getFromMemory(page); // this.memory.get(page);
 		if (p.link != -1) {
 			this.policy.hate(p, p.link);
 		}
 		
 		this.policy.love(p, l, pin);
 	}
 	
 	public void hate(int page, int l) {
 		MemoryPage p = super.getFromMemory(page); //this.memory.get(page);
 		if (p != null && p.link != this.default_chain.id()) {
 			this.policy.hate(p);
 			this.policy.love(p, this.default_chain.id());
 		}
 	}
 	
 	private int sample = 1;
 	private HashMap<Integer, Chain> chains = new HashMap<Integer, Chain>();
 	private ChainedLeastRecentlyUsedReplacementPolicy policy = null;
 	
 
 }
