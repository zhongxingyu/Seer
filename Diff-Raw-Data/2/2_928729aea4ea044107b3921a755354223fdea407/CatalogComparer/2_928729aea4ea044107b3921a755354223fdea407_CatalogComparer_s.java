 package com.johnpickup.backup;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class CatalogComparer {
 	private FileCatalog from;
 	private FileCatalog to;
 	private Set<String> added = new HashSet<String>();
 	private Set<String> removed = new HashSet<String>();
 	private Set<String> changed = new HashSet<String>();
 	private long totalBytesToCopy = 0;
 
 	public CatalogComparer(FileCatalog from, FileCatalog to) {
 		this.from = from;
 		this.to = to;
 		compare();
 	}
 	
 	private void compare() {
 		for (String name : from) {
 			if (to.contains(name)) {
 				if (!from.getCharacteristics(name).equals(to.getCharacteristics(name))) {
 					changed.add(name);
 					totalBytesToCopy += from.getCharacteristics(name).getSize();
 				}
 			}
 			else {
 				removed.add(name);
 			}
 		}
 		for (String name : to) {
 			if (!from.contains(name)) {
 				added.add(name);
				totalBytesToCopy += from.getCharacteristics(name).getSize();
 			}
 		}
 	}
 
 	public FileCatalog getFrom() {
 		return from;
 	}
 
 	public FileCatalog getTo() {
 		return to;
 	}
 
 	public Set<String> getAdded() {
 		return added;
 	}
 
 	public Set<String> getRemoved() {
 		return removed;
 	}
 
 	public Set<String> getChanged() {
 		return changed;
 	}
 
 	public long getTotalFilesToCopy() {
 		return added.size() + changed.size();
 	}
 
 	public long getTotalFilesToDelete() {
 		return removed.size();
 	}
 
 	public long getTotalBytesToCopy() {
 		return totalBytesToCopy ;
 	}
 
 }
