 package hudson.plugins.bitkeeper;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogSet;
 
 public class BitKeeperChangeSetList extends ChangeLogSet<BitKeeperChangeset> {
 	private final List<BitKeeperChangeset> changeSets;
 	
	public BitKeeperChangeSetList(AbstractBuild build, List<BitKeeperChangeset> logs) {
         super(build);
         //Collections.reverse(logs);  // put new things first
         this.changeSets = Collections.unmodifiableList(logs);
         for (BitKeeperChangeset log : logs)
             log.setParent(this);
 	}
 	
 	@Override
 	public boolean isEmptySet() {
 		return changeSets.isEmpty();
 	}
 
	@Override
 	public Iterator<BitKeeperChangeset> iterator() {
 		return changeSets.iterator();
 	}
 
 	public List<BitKeeperChangeset> getLogs() {
         return changeSets;
     }
 }
