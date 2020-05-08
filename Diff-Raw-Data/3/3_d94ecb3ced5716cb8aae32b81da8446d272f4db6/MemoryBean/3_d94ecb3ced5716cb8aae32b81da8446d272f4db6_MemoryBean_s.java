 package li.rudin.rt.examples.simple;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
import javax.inject.Named;
 
 import li.rudin.rt.api.observable.Observable;
 
 @ApplicationScoped
@Named
 public class MemoryBean
 {
 	
 	@Inject Observable<Long> freeMemory, totalMemory, usedMemory;
 
 	public long getUsedMemory() {
 		return usedMemory.get();
 	}
 
 	public void setUsedMemory(long usedMemory) {
 		this.usedMemory.set(usedMemory);
 	}
 
 	public long getTotalMemory() {
 		return totalMemory.get();
 	}
 
 	public void setTotalMemory(long totalMemory) {
 		this.totalMemory.set(totalMemory);
 	}
 
 	public long getFreeMemory() {
 		return freeMemory.get();
 	}
 
 	public void setFreeMemory(long freeMemory) {
 		this.freeMemory.set(freeMemory);
 	}
 
 }
