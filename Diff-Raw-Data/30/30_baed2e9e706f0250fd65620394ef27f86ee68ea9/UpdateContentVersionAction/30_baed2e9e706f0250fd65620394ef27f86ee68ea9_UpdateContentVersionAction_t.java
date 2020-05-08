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
 
 package org.infoglue.cms.applications.contenttool.actions;
 
 import org.infoglue.cms.controllers.kernel.impl.simple.*;
 import org.infoglue.cms.entities.content.ContentVersionVO;
import org.infoglue.cms.exception.ConstraintException;
 import org.infoglue.cms.util.ConstraintExceptionBuffer;
 
 /**
   * This is the action-class for UpdateContentVersionVersion
   * 
   * @author Mattias Bogeblad
   */
 
 public class UpdateContentVersionAction extends ViewContentVersionAction 
 {
 	private ContentVersionVO contentVersionVO;
 	private Integer contentId;
 	private Integer languageId;
 	private Integer contentVersionId;
 	private Integer currentEditorId;
 	private String attributeName;
	
 	private ConstraintExceptionBuffer ceb;
 	
 	public UpdateContentVersionAction()
 	{
 		this(new ContentVersionVO());
 	}
 	
 	public UpdateContentVersionAction(ContentVersionVO contentVersionVO)
 	{
 	    this.contentVersionVO = contentVersionVO;
 		this.ceb = new ConstraintExceptionBuffer();	
 	}
 	
 	public String doExecute() throws Exception
 	{
 		super.initialize(this.contentVersionId, this.contentId, this.languageId);
 		ceb.throwIfNotEmpty();
 
 		this.contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
		
		try
		{
		    this.contentVersionVO = ContentVersionControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), this.contentId, this.languageId, this.contentVersionVO);
		}
		catch(ConstraintException ce)
		{
		    super.contentVersionVO = this.contentVersionVO;
		    throw ce;
		}
 		
 		return "success";
 	}
 
 	public String doStandalone() throws Exception
 	{
 		super.initialize(this.contentVersionId, this.contentId, this.languageId);
 		ceb.throwIfNotEmpty();
 		
 		this.contentVersionVO.setVersionModifier(this.getInfoGluePrincipal().getName());
 		this.contentVersionVO = ContentVersionControllerProxy.getController().acUpdate(this.getInfoGluePrincipal(), this.contentId, this.languageId, this.contentVersionVO);
 		
 		return "standalone";
 	}
 
 	public String doSaveAndExit() throws Exception
     {
 		doExecute();
 						 
 		return "saveAndExit";
 	}
 
 	public String doSaveAndExitStandalone() throws Exception
 	{
 		doExecute();
 						 
 		return "saveAndExitStandalone";
 	}
 
 	public String doBackground() throws Exception
 	{
 		doExecute();
 						 
 		return "background";
 	}
 	
 	public void setContentVersionId(Integer contentVersionId)
 	{
 	    this.contentVersionVO.setContentVersionId(contentVersionId);	
 	}
 
     public java.lang.Integer getContentVersionId()
     {
         return this.contentVersionVO.getContentVersionId();
     }
 
 	public void setStateId(Integer stateId)
 	{
 		this.contentVersionVO.setStateId(stateId);	
 	}
 
     public java.lang.Integer getStateId()
     {
         return this.contentVersionVO.getStateId();
     }
 
 	public void setContentId(Integer contentId)
 	{
 		this.contentId = contentId;	
 	}
 
     public java.lang.Integer getContentId()
     {
         return this.contentId;
     }
 
 	public void setLanguageId(Integer languageId)
 	{
 		this.languageId = languageId;
 	}
 
     public java.lang.Integer getLanguageId()
     {
         return this.languageId;
     }
         
     public java.lang.String getVersionValue()
     {
         return this.contentVersionVO.getVersionValue();
     }
         
     public void setVersionValue(java.lang.String versionValue)
     {
     	this.contentVersionVO.setVersionValue(versionValue);
     }
     
 	public Integer getCurrentEditorId() 
 	{
 		return currentEditorId;
 	}
 
 	public void setCurrentEditorId(Integer integer) 
 	{
 		currentEditorId = integer;
 	}
 
 	public String getAttributeName()
 	{
 		return this.attributeName;
 	}
 
 	public String getVersionComment()
 	{
 		return this.contentVersionVO.getVersionComment();
 	}
 	
 	public void setAttributeName(String attributeName)
 	{
 		this.attributeName = attributeName;
 	}
 
 }
