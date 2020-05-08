 package gov.nih.nci.cagrid.portal.liferay.service;
 
 import com.liferay.portal.PortalException;
 import com.liferay.portal.SystemException;
 import com.liferay.portal.kernel.util.PortalInitable;
 import com.liferay.portal.kernel.util.PortalInitableUtil;
 import com.liferay.portal.model.Role;
 import com.liferay.portal.model.RoleConstants;
 import com.liferay.portal.security.permission.ResourceActionsUtil;
 import com.liferay.portal.service.PermissionServiceUtil;
 import com.liferay.portal.service.RoleServiceUtil;
 import gov.nih.nci.cagrid.portal.liferay.utils.LiferayLoginUtil;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 import java.util.List;
 
 /**
  * Loads a role and associates some Actions with the Role.
  * Class is agnostic to the Role and Action(List). Should be configured
  * with a DI framework
  * <p/>
  * <p/>
  * Ensures that the Portal has initialized before peforming the load
  * <p/>
  * <p/>
  * User: kherm
  *
  * @author kherm manav.kher@semanticbits.com
  */
 public class RolePopulator implements InitializingBean, PortalInitable {
 
     private List<String> actions;
     private String resource;
     private String roleName = "Role Name";
     private String roleDesc = "Role Description";
     private long scopeId;
     private LiferayLoginUtil liferayLoginHelper;
 
     private Log logger = LogFactory.getLog(getClass());
 
     // attach it to a static content as a PortalInitable object
     public void afterPropertiesSet() throws Exception {
         PortalInitableUtil.init(this);
     }
 
     /**
      * On Portal initialization, load the roles
      */
     public void portalInit() {
         try {
             loadRoles();
         } catch (Exception e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /*
     Load roles into Liferay.
      */
     public void loadRoles() throws Exception {
         long companyId = liferayLoginHelper.getCompany().getCompanyId();
         Role addCERole = null;
         try {
             addCERole = RoleServiceUtil.getRole(companyId, roleName);
         } catch (PortalException e) {
             //will create now
             logger.warn("Role for resource " + resource + " does not exist. Will create now.");
         }
 
         if (addCERole == null) {
             try {
                 liferayLoginHelper.masqueradeOmniUser();
             } catch (Exception e) {
                 throw new SystemException("Could not masquerade as Omni user. Will not populate roles", e);
             }
 
             logger.info("Will populate Liferay with roles that do not exist.");
 
             Role role = RoleServiceUtil.addRole(roleName, roleDesc, RoleConstants.TYPE_REGULAR);
 
            ResourceActionsUtil.getModelResourceActions(resource);
 
             for (String action : actions) {
                 PermissionServiceUtil.setRolePermission(
                         role.getRoleId(), scopeId, resource, 1,
                         String.valueOf(role.getCompanyId()), action);
             }
             logger.info("Sucessfully Added roles to Liferay");
         }
     }
 
     public String getRoleName() {
         return roleName;
     }
 
     public void setRoleName(String roleName) {
         this.roleName = roleName;
     }
 
     public String getRoleDesc() {
         return roleDesc;
     }
 
     public void setRoleDesc(String roleDesc) {
         this.roleDesc = roleDesc;
     }
 
     public LiferayLoginUtil getLiferayLoginHelper() {
         return liferayLoginHelper;
     }
 
     public void setLiferayLoginHelper(LiferayLoginUtil liferayLoginHelper) {
         this.liferayLoginHelper = liferayLoginHelper;
     }
 
     public long getScopeId() {
         return scopeId;
     }
 
     public void setScopeId(long scopeId) {
         this.scopeId = scopeId;
     }
 
     public List<String> getActions() {
         return actions;
     }
 
     public void setActions(List<String> actions) {
         this.actions = actions;
     }
 
     public String getResource() {
         return resource;
     }
 
     public void setResource(String resource) {
         this.resource = resource;
     }
 
 
 }
