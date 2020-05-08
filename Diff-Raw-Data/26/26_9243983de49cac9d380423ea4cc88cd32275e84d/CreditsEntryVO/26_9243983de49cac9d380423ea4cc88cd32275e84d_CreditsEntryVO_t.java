 package org.dpi.creditsEntry;
 
 
 public class CreditsEntryVO{
 	
 
 	CreditsEntry creditsEntry =  new CreditsEntryImpl();
 	
 	String currentCategory;
 
 	String proposedCategory;
 	
 	String occupationalGroup;
 
 	String parentOccupationalGroup;
 	
 		
 	boolean canAccountBorrarMovimiento = false;
	boolean canAccountChangeCreditsEntryStatus = false;
 	
 	public CreditsEntryVO(){
 		super();
 	}
 	
 	public CreditsEntry getCreditsEntry() {
 		return creditsEntry;
 	}
 
 	public void setCreditsEntry(CreditsEntry creditsEntry) {
 		this.creditsEntry = creditsEntry;
 	}
 
 	public String getCurrentCategory() {
 		return currentCategory;
 	}
 
 	public void setCurrentCategory(String currentCategory) {
 		this.currentCategory = currentCategory;
 	}
 
 	public String getProposedCategory() {
 		return proposedCategory;
 	}
 
 	public void setProposedCategory(String proposedCategory) {
 		this.proposedCategory = proposedCategory;
 	}
 	
 	public boolean isCanAccountBorrarMovimiento() {
 		return canAccountBorrarMovimiento;
 	}
 
 	public void setCanAccountBorrarMovimiento(boolean canAccountBorrarMovimiento) {
 		this.canAccountBorrarMovimiento = canAccountBorrarMovimiento;
 	}
 
	public boolean isCanAccountChangeCreditsEntryStatus() {
		return canAccountChangeCreditsEntryStatus;
 	}
 
 	public void setCanAccountChangeCreditsEntryStatus(
			boolean canAccountChangeCreditsEntryStatus) {
		this.canAccountChangeCreditsEntryStatus = canAccountChangeCreditsEntryStatus;
 	}
 
 	public String getOccupationalGroup() {
 		return occupationalGroup;
 	}
 
 	public void setOccupationalGroup(String occupationalGroup) {
 		this.occupationalGroup = occupationalGroup;
 	}
 
 	public String getParentOccupationalGroup() {
 		return parentOccupationalGroup;
 	}
 
 	public void setParentOccupationalGroup(String parentOccupationalGroup) {
 		this.parentOccupationalGroup = parentOccupationalGroup;
 	}
 
 }
