 
 package edu.wustl.security.beans;
 
 
 /**
  * A bean object to store role and group details.
  * @author deepti_shelar
  *
  */
 public class RoleGroupDetailsBean
 {
 	/**
 	 * roleName.
 	 */
 	private String roleName;
 	/**
 	 * roleType.
 	 */
 	private String roleType;
 	/**
 	 * groupType.
 	 */
 	private String groupType;
 	/**
 	 * groupName.
 	 */
 	private String groupName;
 	/**
 	 * roleId.
 	 */
 	private String roleId;
 	/**
 	 * groupId.
 	 */
 	private String groupId;
 
 	/**
 	 * @return the roleName
 	 */
 	public String getRoleName()
 	{
 		return roleName;
 	}
 
 	/**
 	 * @param roleName the roleName to set
 	 */
 	public void setRoleName(final String roleName)
 	{
 		this.roleName = roleName;
 	}
 
 	/**
 	 * @return the roleType
 	 */
 	public String getRoleType()
 	{
 		return roleType;
 	}
 
 	/**
 	 * @param roleType the roleType to set
 	 */
 	public void setRoleType(final String roleType)
 	{
 		this.roleType = roleType;
 	}
 
 	/**
 	 * @return the groupType
 	 */
 	public String getGroupType()
 	{
 		return groupType;
 	}
 
 	/**
 	 * @param groupType the groupType to set
 	 */
 	public void setGroupType(final String groupType)
 	{
 		this.groupType = groupType;
 	}
 
 	/**
 	 * @return the groupName
 	 */
 	public String getGroupName()
 	{
 		return groupName;
 	}
 
 	/**
 	 * @param groupName the groupName to set
 	 */
 	public void setGroupName(final String groupName)
 	{
 		this.groupName = groupName;
 	}
 
 	/**
 	 * @return the roleId
 	 */
 	public String getRoleId()
 	{
 		return roleId;
 	}
 
 	/**
 	 * @param roleId the roleId to set
 	 */
 	public void setRoleId(final String roleId)
 	{
 		this.roleId = roleId;
 	}
 
 	/**
 	 * @return the groupId
 	 */
 	public String getGroupId()
 	{
 		return groupId;
 	}
 
 	/**
 	 * @param groupId the groupId to set
 	 */
 	public void setGroupId(final String groupId)
 	{
 		this.groupId = groupId;
 	}
 
 	/**
 	 * @param object the object to be compared.
 	 * @return true if any of the following attributes of both object matches:
 	 * 			- roleId
 	 *          - roleName
 	 *          - groupName
 	 *          - groupId
 	 *          - roleType
 	 *          - groupType
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
	public boolean equals(final Object object) // NOPMD by deepti_shelar on 11/17/08 5:35 PM
 	{
 		boolean equals = false;
 		if ((object != null) && object instanceof RoleGroupDetailsBean)
 		{
 			RoleGroupDetailsBean bean = (RoleGroupDetailsBean) object;
 			equals = ( isObjectEqual(bean.getGroupId(), this.getGroupId())
 					|| isObjectEqual(bean.getGroupName(), this.getGroupName())
 					|| isObjectEqual(bean.getGroupType(), this.getGroupType())
 					|| isObjectEqual(bean.getRoleId(), this.getRoleId())
 					|| isObjectEqual(bean.getRoleName(), this.getRoleName())
 					|| isObjectEqual(bean.getRoleType(), this.getRoleType()) );
 		}
 		return equals;
 	}
 	/**
 	 * Checks for equality.
 	 * @param src obj
 	 * @param target obj to be compared
 	 * @return boolean isequal
 	 */
 	private boolean isObjectEqual(final Object src, final Object target)
 	{
 		boolean isEqual = false;
 		if(src != null)
 		{
 			isEqual = src.equals(target);
 		}
 		return isEqual;
 	}
 
 	/**
 	 * @return int
 	 */
 	public int hashCode()
 	{
 		return edu.wustl.security.global.Constants.HASH_CODE;
 	}
 
 	/**
 	 * @return String.
 	 */
 	public String toString()
 	{
 		return "groupId=" + this.getGroupId() + ":" + "groupName=" + this.getGroupName() + ":"
 				+ "groupType=" + this.getGroupType() + ":" + "roleId=" + this.getRoleId() + ":"
 				+ "roleName=" + this.getRoleName() + ":" + "roleType=" + this.getRoleType();
 	}
 }
