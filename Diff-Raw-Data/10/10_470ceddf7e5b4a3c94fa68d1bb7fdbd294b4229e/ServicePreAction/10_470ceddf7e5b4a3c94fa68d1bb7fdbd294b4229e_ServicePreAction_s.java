 package nl.proteon.liferay.surfnet.security.opensocial.servlet.events.pre;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.portlet.PortletPreferences;
 import javax.portlet.ReadOnlyException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nl.proteon.liferay.surfnet.security.opensocial.OpenSocialGroupLocalServiceUtil;
 import nl.proteon.liferay.surfnet.security.opensocial.exceptions.OpenSocialException;
 import nl.proteon.liferay.surfnet.security.opensocial.model.OpenSocialGroup;
 import com.liferay.counter.service.CounterLocalServiceUtil;
 import com.liferay.portal.kernel.events.Action;
 import com.liferay.portal.kernel.events.ActionException;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.model.Group;
 import com.liferay.portal.model.Layout;
 import com.liferay.portal.model.LayoutConstants;
 import com.liferay.portal.model.LayoutTypePortlet;
 import com.liferay.portal.model.Role;
 import com.liferay.portal.model.User;
 import com.liferay.portal.service.GroupLocalServiceUtil;
 import com.liferay.portal.service.LayoutLocalServiceUtil;
 import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
 import com.liferay.portal.service.RoleLocalServiceUtil;
 import com.liferay.portal.service.ServiceContext;
 import com.liferay.portal.service.UserGroupRoleLocalServiceUtil;
 import com.liferay.portal.util.PortalUtil;
 import com.liferay.portal.util.PortletKeys;
 
 public class ServicePreAction extends Action {
 
 	@Override
 	public void run(HttpServletRequest request, HttpServletResponse response)
 			throws ActionException {
 
 		try {
 			
 			long companyId = PortalUtil.getCompanyId(request);
 			User user = PortalUtil.getUser(request);
 			
 			if(user!=null) {
 				List<OpenSocialGroup> openSocialGroups = OpenSocialGroupLocalServiceUtil.getOpenSocialGroups(user.getUserId());
 		
 				for(OpenSocialGroup openSocialGroup : openSocialGroups) {
 					Group group = null;
 				
 					group = getGroup(companyId, openSocialGroup.getTitle());
 				
 					if(group==null) {
 						
 						_log.info("Group " + openSocialGroup.getId() + " is new");
 						
 						group = addGroup(
 								user.getUserId(),
 								companyId, 
 								openSocialGroup.getTitle(),
 								openSocialGroup.getDescription(),
								"/" + openSocialGroup.getId().substring(30)
 								);
 						
 						_log.info("Group " + openSocialGroup.getId() + " added as " + group.getName());
 						
 						Layout layout = LayoutLocalServiceUtil.addLayout(user.getUserId(), group.getGroupId(), true, 
 								-1, "our_page", "our_title", "", LayoutConstants.TYPE_PORTLET, false, 
 								"", new ServiceContext());
 					
 						LayoutTypePortlet layoutTypePortlet = (LayoutTypePortlet) layout.getLayoutType();
 						layoutTypePortlet.setLayoutTemplateId(user.getUserId(), "1_column");
 						String documentLibraryPortletId = layoutTypePortlet.addPortletId(user.getUserId(), PortletKeys.DOCUMENT_LIBRARY, "column-1", -1, false);
 				    
 						long ownerId = PortletKeys.PREFS_OWNER_ID_DEFAULT;
 						int ownerType = PortletKeys.PREFS_OWNER_TYPE_LAYOUT;
 				    
 						PortletPreferences prefs = PortletPreferencesLocalServiceUtil.getPreferences(companyId, 
 								ownerId, ownerType, layout.getPlid(), documentLibraryPortletId);
 				    
 						prefs.setValue("portletSetupShowBorders", "false");
 				    
 						PortletPreferencesLocalServiceUtil.updatePreferences(ownerId, ownerType, layout
 								.getPlid(), documentLibraryPortletId, prefs);
 				    
 						layout = LayoutLocalServiceUtil.updateLayout(layout.getGroupId(),
 								layout.isPrivateLayout(), layout.getLayoutId(),
 								layout.getTypeSettings());
 					
 					} else {
 						group = updateGroup(companyId, group.getGroupId(), openSocialGroup.getDescription());
 					}
 					Role role = RoleLocalServiceUtil.getRole(companyId, "Site Member");
 				
 					UserGroupRoleLocalServiceUtil.addUserGroupRoles(
 							user.getUserId(), 
 							group.getGroupId(), 
 							new long[] { role.getRoleId() });
 					
 				}
 				
 				Map<String, OpenSocialGroup> openSocialGroupsMap = new HashMap<String, OpenSocialGroup>();
 				for(OpenSocialGroup openSocialGroup : openSocialGroups){
 					openSocialGroupsMap.put(openSocialGroup.getTitle(), openSocialGroup);
 				}
 				
 				List<Group> groups = GroupLocalServiceUtil.getUserGroups(user.getUserId(), true);
 				
 				for(Group group : groups) {
 					if(openSocialGroupsMap.get(group.getName()) == null){
 						GroupLocalServiceUtil.unsetUserGroups(user.getUserId(), new long[] { group.getGroupId() });
 						_log.info("User " + user.getOpenId() + " removed from " + group.getName());
 					}
 				}
 			}
 
 		} catch (PortalException e) {
 			_log.debug(e,e);
 		} catch (SystemException e) {
 			_log.debug(e,e);
 		} catch (OpenSocialException e) {
 			_log.debug(e,e);
 		} catch (ReadOnlyException e) {
 			_log.debug(e,e);
 		}
 	}
 	
 	public Group getGroup(long companyId, String name) {
 		Group group = null;
 		
 		try {
 			group = GroupLocalServiceUtil.getGroup(companyId, name);
 		} catch (Exception e) {
 		}
 		return group;
 	}
 	
 	public Group addGroup(long userId, long companyId, String name, String description, String friendlyURL) {
 		Group group = null;
 		
 		try {
 			String className = Group.class.getName();
 			long classPK = CounterLocalServiceUtil.increment(className);
 			int type = 3;
 			boolean site = true;
 			boolean active = true;
 		
 			ServiceContext serviceContext = new ServiceContext();		
 
 			group = GroupLocalServiceUtil.addGroup(userId, className, classPK, name, 
 					description, type, friendlyURL, site, active, serviceContext);
 		} catch (PortalException e) {
 			_log.error(e,e);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		}
 		return group;
 	}
 	
 	public Group updateGroup(long companyId, long groupId, String description) {
 		Group group = null;
 		
 		try {
 			group = GroupLocalServiceUtil.getGroup(groupId);
 
 			group.setDescription(description);
 			
 			group = GroupLocalServiceUtil.updateGroup(group);
 		} catch (SystemException e) {
 			_log.error(e,e);
 		} catch (PortalException e) {
 			_log.error(e,e);
 		}
 		return group;
 	}
 	
 	private static Log _log = LogFactoryUtil.getLog(ServicePreAction.class);
 
 }
