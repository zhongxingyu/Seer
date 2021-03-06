 package is.idega.idegaweb.member.business;
 
 /**
  * Title:        idegaWeb User Subsystem
  * Description:  idegaWeb User Subsystem is the base system for Users and Group management
  * Copyright:    Copyright (c) 2002
  * Company:      idega
  * @author <a href="mailto:tryggvi@idega.is">Tryggvi Larusson</a>
  * @version 1.0
  */
 
 public class NoSiblingFound extends javax.ejb.FinderException {
 
   public NoSiblingFound(String UserName) {
      super("No children found for user "+UserName);
   }
 }
