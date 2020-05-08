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
 
 import org.infoglue.cms.applications.common.actions.WebworkAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.*;
 
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.exception.*;
 
 /**
  * This action removes a repository from the system.
  * 
  * @author Mattias Bogeblad
  */
 
 public class DeleteRepositoryAction extends WebworkAbstractAction
 {
 	private RepositoryVO repositoryVO;
 	private Integer repositoryId;
 	
 	public DeleteRepositoryAction()
 	{
 		this(new RepositoryVO());
 	}
 
 	public DeleteRepositoryAction(RepositoryVO repositoryVO) 
 	{
 		this.repositoryVO = repositoryVO;
 	}
 	
	protected String doExecute() throws Exception 
 	{
 	    this.repositoryVO.setRepositoryId(this.getRepositoryId());
 		RepositoryController.getController().delete(this.repositoryVO, this.getInfoGluePrincipal().getName());
 		return "success";
 	}
 	
 	public void setRepositoryId(Integer repositoryId) throws SystemException
 	{
 		this.repositoryVO.setRepositoryId(repositoryId);	
 	}
 
     public java.lang.Integer getRepositoryId()
     {
         return this.repositoryVO.getRepositoryId();
     }
         
 	
 }
