 package mmu;
 
 import java.text.DecimalFormat;
 import java.util.*;
 
 import mmu.policy.IPageReplacementPolicy;
 
 public class MemoryManager implements IMemoryManager {
 
 	protected IPageReplacementPolicy policy;
 	private int size;
 	protected MemoryPage[] memory;
	protected int memoryPointer = 0;
 	private HashMap<Integer, MemoryPage> disk = new HashMap<Integer, MemoryPage>();
 	
 	public MemoryManager(IPageReplacementPolicy policy, int size) {
 		this.policy = policy;
 		this.size = size;
 		memory = new MemoryPage[size];
 	}
 	
 	protected MemoryPage getFromDisk(int address) {
 		MemoryPage p = this.disk.get(address);
 		if (p == null) {
 			this.disk.put(address, p = new MemoryPage(address));
 		}
 		
 		//TODO is this needed?
 		try {
 			Thread.sleep(0);
 		} catch (InterruptedException e) {
 		}
 		
 		return p;
 	}
 	
 	public void access(int address) {
 		
 		MemoryPage page = getFromMemory(address);
 		
 		//If not in memory, get if from disk
 		if (page == null) {
 			
 			int posToInsert;
 			
 			//If memory is full, evict a page
 			if (memoryPointer >= memory.length) {
 				page = this.policy.findPageToEvict(memory);
 				page.updateEvictStats();
 				posToInsert = evict(page);
 			}
 			//If its not full, get a pointer to next free memory
 			else{
 				posToInsert = memoryPointer;
 				memoryPointer++;
 			}
 			
 			
 			page = this.getFromDisk(address);
 			page.updateLoadStats();
 			
 			//TODO should this be here?
 			page.updateRefStats();
 			
 			memory[posToInsert] = page;
 			
 		} else {
 			page.updateRefStats();
 		}
 	}
 	
 	protected boolean isFull() {
		return this.memoryPointer >= this.memory.length;
 	}
 	
 	protected MemoryPage getFromMemory(int address){
 		for(MemoryPage p: memory){
 			if(p != null)
 				if(p.address == address)
 					return p;
 		}
 		return null;
 	}
 	
	protected void addToMemory(int position, MemoryPage p) {	
		memory[position] = p;
	}
	
 	protected int evict(MemoryPage page){
 		for(int i = 0; i < memory.length; i++){
 			if(memory[i] != null){
 				if(memory[i].address == page.address)
 					return i;
 			}
 		}
 		return -1;
 	}
 	
 	public void love(int page, int l) {
 		this.policy.love(getFromMemory(page), l);
 	}
 	
 	public void love(int page, int l, boolean pin) {
 		this.policy.love(getFromMemory(page), l, pin);
 	}
 	
 	public void hate(int page, int l) {
 		try {
 			this.policy.hate(getFromMemory(page), l); 
 		} catch (NullPointerException e) {
 			for (int i = 0; i < this.memory.length; i++) {
 				System.err.println(i + ": " + this.memory[i] + "");
 			}
 			
 			System.err.println("looking for: " + page);
 			throw e;
 		}
 	}
 	
 	public int capacity() {
 		return this.size;
 	}
 	
 	public void summary() {
 		
 		long hit = 0;
 		long miss = 0;
 		long evicted = 0;
 		
 		for (MemoryPage page : this.disk.values()) {
 			hit += page.hits();
 			miss += page.misses();
 			evicted += page.evictions();
 		}
 		
 		DecimalFormat df = new DecimalFormat("0.00");
 		
 		System.out.println("page statistics:[hit-count=" + hit + ";miss-count=" +
 				miss + ";evicted-count=" + evicted + "; miss-ratio=" + 
 				df.format((double)miss/hit) + "]");
 		
 	}
 	
 	public double getMissRatio(){
 		long hit = 0;
 		long miss = 0;
 		
 		for (MemoryPage page : this.disk.values()) {
 			hit += page.hits();
 			miss += page.misses();
 		}
 		
 		return (double)miss/hit;
 	}
 	
 	public void stats() {
 		Integer [] addresses = this.disk.keySet().toArray(new Integer[0]);
 		
 		Arrays.sort(addresses);
 		for (int i : addresses) {
 			MemoryPage p = this.disk.get(i);
 			System.out.println(p.stats());
 		}
 	}
 	
 	public void profile() {
 		for (MemoryPage block : memory) {
 			System.out.print(block.toString() + ';');
 		}
 	}
 }
