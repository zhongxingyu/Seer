 /**
  * @author dgeorge
  * 
 * $Id: NameData.java,v 1.2 2007-11-01 13:43:08 pandyas Exp $
  * 
  * $Log: not supported by cvs2svn $
 * Revision 1.1  2005/09/28 13:58:45  georgeda
 * More work on manager interface for CI
 *
  * Revision 1.1  2005/09/27 19:17:08  georgeda
  * Refactor of CI managers
  *
  *
  */
 package gov.nih.nci.camod.webapp.form.cibase;
 
 
 /**
  Interface for most NameData screens
  */
 public interface NameData  {
 
     public String getName();
 
     public void setName(String name);
 
     public String getOtherName();
 
     public void setOtherName(String otherName);
    
	public String getComments();
	
	public void setComments(String comments);    
 }
