 package mmu;
 
 
 public class LoveHateReplacementPolicy implements IPageReplacementPolicy {	
 	
 	private static int PIN_LEVEL = 255;
 	
 	public String name() {
 		return "LHR";
 	}
 	
 	public MemoryPage evict(MemoryPage [] pages) {
 		
 		long minTime = System.currentTimeMillis() + 1000;
 		int minLove = Integer.MAX_VALUE;
 		
		MemoryPage b = null;
 		
 		for (MemoryPage block : pages) {
 			
 			//Remove the least recently used page of those pages with the lowest level of love
			
 			if(block.love < minLove || block.love == minLove && block.time < minTime){
 				minTime = block.time;
 				minLove = block.love;
				b = block;
 			}
 			
 		}
	
		//Decrease the love of the evicted page
		if(b.love != PIN_LEVEL){
			b.love--;
 		}
		return b;
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
