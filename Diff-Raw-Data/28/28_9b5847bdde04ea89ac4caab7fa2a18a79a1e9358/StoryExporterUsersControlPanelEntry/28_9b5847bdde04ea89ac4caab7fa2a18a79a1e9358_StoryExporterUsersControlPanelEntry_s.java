 package org.goodreturn.story.exporter;
 
 import javax.portlet.RenderRequest;
 
 import com.liferay.portal.model.Portlet;
 import com.liferay.portal.security.permission.PermissionChecker;
 import com.liferay.portlet.BaseControlPanelEntry;
 
 public class StoryExporterUsersControlPanelEntry extends BaseControlPanelEntry {
 
 	@Override
 	public boolean isVisible(PermissionChecker permissionChecker, Portlet portlet) throws Exception {
 		return portlet.hasRoleWithName("administrator") ||
 				portlet.hasRoleWithName("weal-owner") ||
 				portlet.hasRoleWithName("weal-operations");
 	}
 
 	
 	
 	public static boolean hasExportPermissions(PermissionChecker permissionChecker, RenderRequest request) {
 		return request.isUserInRole("administrator") ||
 				request.isUserInRole("weal-owner") ||
				request.isUserInRole("weal-operations"); //TODO ADD permissionChecker check here for EXPORT_STORY
 	}
 }
