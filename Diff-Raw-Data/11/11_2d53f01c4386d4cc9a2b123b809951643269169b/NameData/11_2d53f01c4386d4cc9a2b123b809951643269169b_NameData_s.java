 /**
  * @author dgeorge
  * 
 * $Id: NameData.java,v 1.1 2005-09-28 13:58:45 georgeda Exp $
  * 
  * $Log: not supported by cvs2svn $
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
 }
