 package mmu;
 
 
 public class LoveHateReplacementPolicy implements IPageReplacementPolicy {	
 	
 	private static int PIN_LEVEL = 255;
 	
 	public String name() {
 		return "LHR";
 	}
 	
 	public MemoryPage evict(MemoryPage [] pages) {
 		
 		long minTime = System.currentTimeMillis() + 1000;
		long lruMinTime = minTime;
 		int minLove = Integer.MAX_VALUE;
 		
		MemoryPage evitedBlock = null;
		MemoryPage lruEvictedBlock = null;
 		
 		for (MemoryPage block : pages) {
 			
 			//Remove the least recently used page of those pages with the lowest level of love
 			if(block.love < minLove || block.love == minLove && block.time < minTime){
 				minTime = block.time;
 				minLove = block.love;
				evitedBlock = block;
			}
			
			//Keep track of the actually least recently used, ignoring love
			if(block.time < lruMinTime){
				lruMinTime = block.time;
				lruEvictedBlock = block;
 			}
 			
 		}
		
		//If the evicted block is more recently used than some other block,
		//the other block was protected by love, and its love should be decreased
		if(lruEvictedBlock.time < evitedBlock.time){
			lruEvictedBlock.love--;
 		}
	
		return evitedBlock;
 	}
 	
 	public void love(MemoryPage page, int level) {
 		if (page != null) {
 			page.love = Math.min(level, PIN_LEVEL);
 		}
 	}
 	
 	public void love(MemoryPage page, int level, boolean pin) {
 		if (page != null) {
 			page.love = pin ? PIN_LEVEL : Math.min(level, PIN_LEVEL - 1);
 		}
 	}
 	
 	public void hate(MemoryPage page, int level) {
 		if (page != null) {
 			page.love = Math.min(level, page.love);
 		}
 	}
 	
 	public void hate(MemoryPage page) {
 		if (page != null) {
 			page.love = 0;
 			page.time = 0;
 		}
 	}
 }
