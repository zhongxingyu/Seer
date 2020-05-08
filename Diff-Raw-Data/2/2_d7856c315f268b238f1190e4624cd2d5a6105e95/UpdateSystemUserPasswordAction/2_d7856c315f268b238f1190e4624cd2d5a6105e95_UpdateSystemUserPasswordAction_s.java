 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.RoleControllerProxy;
 import org.infoglue.cms.controllers.kernel.impl.simple.SystemUserController;
 import org.infoglue.cms.controllers.kernel.impl.simple.UserControllerProxy;
 import org.infoglue.cms.exception.ConstraintException;
 
 import webwork.action.Action;
 import webwork.action.ActionContext;
 
 /**
  * This action makes it possible to change a users password for him/her.
  * 
  * @author Mattias Bogeblad
  */
 
 public class UpdateSystemUserPasswordAction extends InfoGlueAbstractAction
 {
     private String userName;
     private String oldPassword;
     private String newPassword;
     private String verifiedNewPassword;
     private String returnAddress;
     
     public String doInput() throws Exception
     {
     	return Action.INPUT;
     }
     
     public String doInputStandalone() throws Exception
     {
     	return "inputStandalone";
     }
     
 	protected String doExecute() throws Exception 
 	{
 	    if(!newPassword.equals(verifiedNewPassword))
 	        throw new ConstraintException("SystemUser.newPassword", "309");
 	    
 	    UserControllerProxy.getController().updateUserPassword(this.userName, this.oldPassword, this.newPassword);
 		
	    if(this.returnAddress != null)
 	    {
 	        ActionContext.getResponse().sendRedirect(returnAddress);
 	        return Action.NONE;
 	    }
 	    else
 	        return Action.SUCCESS;
 	}
 	
     public String getNewPassword()
     {
         return newPassword;
     }
     
     public void setNewPassword(String newPassword)
     {
         this.newPassword = newPassword;
     }
     
     public String getOldPassword()
     {
         return oldPassword;
     }
     
     public void setOldPassword(String oldPassword)
     {
         this.oldPassword = oldPassword;
     }
     
     public String getUserName()
     {
         return userName;
     }
     
     public void setUserName(String userName)
     {
         this.userName = userName;
     }
     
     public String getVerifiedNewPassword()
     {
         return verifiedNewPassword;
     }
     
     public void setVerifiedNewPassword(String verifiedNewPassword)
     {
         this.verifiedNewPassword = verifiedNewPassword;
     }
     
     public String getReturnAddress()
     {
         return returnAddress;
     }
     
     public void setReturnAddress(String returnAddress)
     {
         this.returnAddress = returnAddress;
     }
 }
