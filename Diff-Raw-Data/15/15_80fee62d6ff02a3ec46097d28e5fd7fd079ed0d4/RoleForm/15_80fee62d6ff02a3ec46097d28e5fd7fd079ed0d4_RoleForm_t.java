 /*
  * Created on Dec 3, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.nih.nci.security.upt.forms;
 
 /**
  *
  *<!-- LICENSE_TEXT_START -->
  *
  *The NCICB Common Security Module's User Provisioning Tool (UPT) Software License,
  *Version 3.0 Copyright 2004-2005 Ekagra Software Technologies Limited ('Ekagra')
  *
  *Copyright Notice.  The software subject to this notice and license includes both
  *human readable source code form and machine readable, binary, object code form
  *(the 'UPT Software').  The UPT Software was developed in conjunction with the
  *National Cancer Institute ('NCI') by NCI employees and employees of Ekagra.  To
  *the extent government employees are authors, any rights in such works shall be
  *subject to Title 17 of the United States Code, section 105.    
  *
  *This UPT Software License (the 'License') is between NCI and You.  'You (or
  *'Your') shall mean a person or an entity, and all other entities that control,
  *are controlled by, or are under common control with the entity.  'Control' for
  *purposes of this definition means (i) the direct or indirect power to cause the
  *direction or management of such entity, whether by contract or otherwise, or
  *(ii) ownership of fifty percent (50%) or more of the outstanding shares, or
  *(iii) beneficial ownership of such entity.  
  *
  *This License is granted provided that You agree to the conditions described
  *below.  NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up,
  *no-charge, irrevocable, transferable and royalty-free right and license in its
  *rights in the UPT Software to (i) use, install, access, operate, execute, copy,
  *modify, translate, market, publicly display, publicly perform, and prepare
  *derivative works of the UPT Software; (ii) distribute and have distributed to
  *and by third parties the UPT Software and any modifications and derivative works
  *thereof; and (iii) sublicense the foregoing rights set out in (i) and (ii) to
  *third parties, including the right to license such rights to further third
  *parties.  For sake of clarity, and not by way of limitation, NCI shall have no
  *right of accounting or right of payment from You or Your sublicensees for the
  *rights granted under this License.  This License is granted at no charge to You.
  *
  *1.	Your redistributions of the source code for the Software must retain the
  *above copyright notice, this list of conditions and the disclaimer and
  *limitation of liability of Article 6 below.  Your redistributions in object code
  *form must reproduce the above copyright notice, this list of conditions and the
  *disclaimer of Article 6 in the documentation and/or other materials provided
  *with the distribution, if any.
  *2.	Your end-user documentation included with the redistribution, if any, must
  *include the following acknowledgment: 'This product includes software developed
  *by Ekagra and the National Cancer Institute.'  If You do not include such
  *end-user documentation, You shall include this acknowledgment in the Software
  *itself, wherever such third-party acknowledgments normally appear.
  *
  *3.	You may not use the names 'The National Cancer Institute', 'NCI' 'Ekagra
  *Software Technologies Limited' and 'Ekagra' to endorse or promote products
  *derived from this Software.  This License does not authorize You to use any
  *trademarks, service marks, trade names, logos or product names of either NCI or
  *Ekagra, except as required to comply with the terms of this License.
  *
  *4.	For sake of clarity, and not by way of limitation, You may incorporate this
  *Software into Your proprietary programs and into any third party proprietary
  *programs.  However, if You incorporate the Software into third party proprietary
  *programs, You agree that You are solely responsible for obtaining any permission
  *from such third parties required to incorporate the Software into such third
  *party proprietary programs and for informing Your sublicensees, including
  *without limitation Your end-users, of their obligation to secure any required
  *permissions from such third parties before incorporating the Software into such
  *third party proprietary software programs.  In the event that You fail to obtain
  *such permissions, You agree to indemnify NCI for any claims against NCI by such
  *third parties, except to the extent prohibited by law, resulting from Your
  *failure to obtain such permissions.
  *
  *5.	For sake of clarity, and not by way of limitation, You may add Your own
  *copyright statement to Your modifications and to the derivative works, and You
  *may provide additional or different license terms and conditions in Your
  *sublicenses of modifications of the Software, or any derivative works of the
  *Software as a whole, provided Your use, reproduction, and distribution of the
  *Work otherwise complies with the conditions stated in this License.
  *
  *6.	THIS SOFTWARE IS PROVIDED 'AS IS,' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
  *(INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY,
  *NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED.  IN NO
  *EVENT SHALL THE NATIONAL CANCER INSTITUTE, EKAGRA, OR THEIR AFFILIATES BE LIABLE
  *FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  *DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  *SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  *CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  *TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  *THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *<!-- LICENSE_TEXT_END -->
  *
  */
 
 
 import gov.nih.nci.security.UserProvisioningManager;
 import gov.nih.nci.security.authorization.domainobjects.Privilege;
 import gov.nih.nci.security.authorization.domainobjects.Role;
 import gov.nih.nci.security.dao.PrivilegeSearchCriteria;
 import gov.nih.nci.security.dao.RoleSearchCriteria;
 import gov.nih.nci.security.dao.SearchCriteria;
 import gov.nih.nci.security.upt.constants.DisplayConstants;
 import gov.nih.nci.security.upt.viewobjects.FormElement;
 import gov.nih.nci.security.upt.viewobjects.SearchResult;
 import gov.nih.nci.security.util.ObjectSetUtil;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.ActionMessages;
 import org.apache.struts.validator.ValidatorForm;
 
 /**
  * @author Kunal Modi (Ekagra Software Technologies Ltd.)
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class RoleForm extends ValidatorForm implements BaseAssociationForm
 {
 	
 	private String roleId;
 	private String roleName;
 	private String roleDescription;
 	private String roleActiveFlag;
 	private String roleUpdateDate;
 	
 	private String[] associatedIds;
 	
 	
 
 	/**
 	 * @return Returns the roleActiveFlag.
 	 */
 	public String getRoleActiveFlag() {
 		return roleActiveFlag;
 	}
 	/**
 	 * @param roleActiveFlag The roleActiveFlag to set.
 	 */
 	public void setRoleActiveFlag(String roleActiveFlag) {
 		this.roleActiveFlag = roleActiveFlag;
 	}
 	/**
 	 * @return Returns the roleDescription.
 	 */
 	public String getRoleDescription() {
 		return roleDescription;
 	}
 	/**
 	 * @param roleDescription The roleDescription to set.
 	 */
 	public void setRoleDescription(String roleDescription) {
 		this.roleDescription = roleDescription;
 	}
 	/**
 	 * @return Returns the roleId.
 	 */
 	public String getRoleId() {
 		return roleId;
 	}
 	/**
 	 * @param roleId The roleId to set.
 	 */
 	public void setRoleId(String roleId) {
 		this.roleId = roleId;
 	}
 	/**
 	 * @return Returns the roleName.
 	 */
 	public String getRoleName() {
 		return roleName;
 	}
 	/**
 	 * @param roleName The roleName to set.
 	 */
 	public void setRoleName(String roleName) {
 		this.roleName = roleName;
 	}
 	
 	/**
 	 * @return Returns the roleUpdateDate.
 	 */
 	public String getRoleUpdateDate() {
 		return roleUpdateDate;
 	}
 	/**
 	 * @param roleUpdateDate The roleUpdateDate to set.
 	 */
 	public void setRoleUpdateDate(String roleUpdateDate) {
 		this.roleUpdateDate = roleUpdateDate;
 	}
 	
 	/**
 	 * @return Returns the associatedIds.
 	 */
 	public String[] getAssociatedIds() {
 		return associatedIds;
 	}
 	/**
 	 * @param associatedIds The associatedIds to set.
 	 */
 	public void setAssociatedIds(String[] associatedIds) {
 		this.associatedIds = associatedIds;
 	}
 	
 	public ArrayList getAddFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 
 		formElementList.add(new FormElement("Role Name", "roleName", getRoleName(), DisplayConstants.INPUT_BOX, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Role Description", "roleDescription", getRoleDescription(), DisplayConstants.INPUT_TEXTAREA, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Role Active Flag", "roleActiveFlag", getRoleActiveFlag(), DisplayConstants.INPUT_RADIO, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 
 		return formElementList;
 	}
 
 	public ArrayList getDisplayFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 
 		formElementList.add(new FormElement("Role Name", "roleName", getRoleName(), DisplayConstants.INPUT_BOX, DisplayConstants.REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Role Description", "roleDescription", getRoleDescription(), DisplayConstants.INPUT_TEXTAREA, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Role Active Flag", "roleActiveFlag", getRoleActiveFlag(), DisplayConstants.INPUT_RADIO, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 		formElementList.add(new FormElement("Role Update Date", "roleUpdateDate", getRoleUpdateDate(), DisplayConstants.INPUT_DATE, DisplayConstants.NOT_REQUIRED, DisplayConstants.DISABLED));
 
 		return formElementList;
 	}
 
 	public ArrayList getSearchFormElements()
 	{
 		ArrayList formElementList = new ArrayList();
 
 		formElementList.add(new FormElement("Role Name", "roleName", getRoleName(), DisplayConstants.INPUT_BOX, DisplayConstants.NOT_REQUIRED, DisplayConstants.NOT_DISABLED));
 
 		return formElementList;
 	}
 
 	public void resetForm()
 	{
 		this.roleId = "";
 		this.roleName = "";
 		this.roleDescription = "";
 		this.roleActiveFlag = DisplayConstants.YES;
 		this.roleUpdateDate = "";
 		this.associatedIds = null;
 	}
 
 	public void reset(ActionMapping mapping, HttpServletRequest request)
 	{
 		this.roleName = "";
 		this.roleDescription = "";
 		this.roleActiveFlag = DisplayConstants.YES;
 		this.associatedIds = null;		
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.forms.BaseDBForm#buildDisplayForm(javax.servlet.http.HttpServletRequest)
 	 */
 	public void buildDisplayForm(HttpServletRequest request) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		Role role = userProvisioningManager.getRoleById(this.roleId);
 
 		this.roleName = role.getName();
 		this.roleDescription = role.getDesc();
		if (role.getActive_flag() == DisplayConstants.ONE) this.roleActiveFlag = DisplayConstants.YES;
 			else this.roleActiveFlag = DisplayConstants.NO;
 		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 		this.roleUpdateDate = simpleDateFormat.format(role.getUpdateDate());
 
 	}
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.forms.BaseDBForm#buildDBObject(javax.servlet.http.HttpServletRequest)
 	 */
 	public void buildDBObject(HttpServletRequest request) throws Exception
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		
 		Role role = null;
 		
 		if ((this.roleId == null) || ((this.roleId).equalsIgnoreCase("")))
 		{
 			role = new Role();
 		}
 		else
 		{
 			role = userProvisioningManager.getRoleById(this.roleId);
 		}
 		
 		role.setName(this.roleName);
 		role.setDesc(this.roleDescription);
 
		if (this.roleActiveFlag.equals(DisplayConstants.YES)) role.setActive_flag(DisplayConstants.ONE);
 			else role.setActive_flag(DisplayConstants.ZERO);
 		
 		if ((this.roleId == null) || ((this.roleId).equalsIgnoreCase("")))
 		{
 			userProvisioningManager.createRole(role);
 			this.roleId = role.getId().toString();
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			this.roleUpdateDate = simpleDateFormat.format(role.getUpdateDate());
 		}
 		else
 		{
 			userProvisioningManager.modifyRole(role);
 			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
 			this.roleUpdateDate = simpleDateFormat.format(role.getUpdateDate());
 		}
 	}
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.forms.BaseDBForm#removeDBObject(javax.servlet.http.HttpServletRequest)
 	 */
 	public void removeDBObject(HttpServletRequest request) throws Exception 
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		userProvisioningManager.removeRole(this.roleId);
 		this.resetForm();
 	}
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.forms.BaseDBForm#searchObjects(javax.servlet.http.HttpServletRequest)
 	 */
 	public SearchResult searchObjects(HttpServletRequest request, ActionErrors errors, ActionMessages messages) throws Exception 
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		Role role = new Role();
 		if (this.roleName != null && !(this.roleName.trim().equalsIgnoreCase("")))
 			role.setName(this.roleName);
 		
 		SearchCriteria searchCriteria = new RoleSearchCriteria(role);
 		List list = userProvisioningManager.getObjects(searchCriteria);
 		SearchResult searchResult = new SearchResult();
 		searchResult.setSearchResultMessage(searchCriteria.getMessage());
 		searchResult.setSearchResultObjects(list);
 		return searchResult;
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.forms.BaseDBForm#getPrimaryId()
 	 */
 	public String getPrimaryId() 
 	{
 		if (this.roleId == null)
 		{
 			return new String("");
 		}
 		else
 		{
 			return roleId;
 		}
 	}
 
 
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.upt.forms.BaseAssociationForm#buildAssociationObject(javax.servlet.http.HttpServletRequest)
 	 */
 	public void buildAssociationObject(HttpServletRequest request) throws Exception 
 	{
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 
 		Collection associatedPrivileges = (Collection)userProvisioningManager.getPrivileges(this.roleId);
 		
 		Privilege privilege = new Privilege();
 		SearchCriteria searchCriteria = new PrivilegeSearchCriteria(privilege);
 		Collection totalPrivileges = (Collection)userProvisioningManager.getObjects(searchCriteria);
 
 		Collection availablePrivileges = ObjectSetUtil.minus(totalPrivileges,associatedPrivileges);
 		
 		request.setAttribute(DisplayConstants.ASSIGNED_SET, associatedPrivileges);
 		request.setAttribute(DisplayConstants.AVAILABLE_SET, availablePrivileges);
 		
 	}
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.upt.forms.BaseAssociationForm#setAssociationObject(javax.servlet.http.HttpServletRequest)
 	 */
 	public void setAssociationObject(HttpServletRequest request) throws Exception {
 
 		UserProvisioningManager userProvisioningManager = (UserProvisioningManager)(request.getSession()).getAttribute(DisplayConstants.USER_PROVISIONING_MANAGER);
 		if (this.associatedIds == null)
 			this.associatedIds = new String[0];
 		userProvisioningManager.assignPrivilegesToRole(this.roleId, this.associatedIds);
 	}
 	/* (non-Javadoc)
 	 * @see gov.nih.nci.security.upt.forms.BaseDBForm#getFormName()
 	 */
 	public String getFormName() {
 		return DisplayConstants.ROLE_ID;
 	}
 
 }
