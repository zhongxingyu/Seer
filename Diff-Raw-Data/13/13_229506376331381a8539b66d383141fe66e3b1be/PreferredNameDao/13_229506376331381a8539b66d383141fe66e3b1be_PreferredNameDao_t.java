 package edu.wisc.portlet.preferred.dao;
 
 import edu.wisc.portlet.preferred.form.PreferredName;
 
 public interface PreferredNameDao {
 	public PreferredName getPreferredName(String pvi);
 	
 	public boolean isPending();
 	
 	public void setPreferredName(PreferredName pn);
 	
 	public void deletePreferredName(String pvi);
 
	public boolean deletePreferredNameAdmin(String pvi);
 }
