 package ar.com.eoconsulting.utils.documentlibrary;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import ar.com.eoconsulting.utils.permissions.RolePermissionsUtils;
 
 import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
 import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.model.ResourceConstants;
 import com.liferay.portal.security.permission.ActionKeys;
 import com.liferay.portlet.documentlibrary.model.DLFileEntry;
 import com.liferay.portlet.documentlibrary.model.DLFolder;
 import com.liferay.portlet.documentlibrary.service.DLFolderLocalServiceUtil;
 
 
 /**
  * Document Library Folder utils.
  *
  * @author Mariano Ruiz
  */
 public abstract class DLFolderUtils {
 
 	public static final List<String>
 			DLFOLDER_PERMISSIONS = Arrays.asList(
 				ActionKeys.VIEW,
 				ActionKeys.ACCESS,
 				ActionKeys.UPDATE,
 				ActionKeys.DELETE,
 				ActionKeys.PERMISSIONS,
 				ActionKeys.ADD_DOCUMENT,
 				ActionKeys.ADD_SUBFOLDER);
 
 
 	/**
 	 * Return the folder named as <code>name</code> into <code>parentFolder</code>.<br/>
 	 * If <code>parentFolder</code> is <code>null</code>, then the folder should not have
 	 * ancestor (root folder).
 	 */
 	@SuppressWarnings("unchecked")
 	public static DLFolder getByNameAndParent(String name, DLFolder parentFolder)
 			throws SystemException {
 
 		List<DLFolder> result;
 		if(parentFolder != null) {
 			result = DLFolderLocalServiceUtil.dynamicQuery(
 				DynamicQueryFactoryUtil.forClass(DLFolder.class).add(
 				PropertyFactoryUtil.forName("name").eq(name)).add(
 				PropertyFactoryUtil.forName("parentFolderId")
 				.eq(new Long(parentFolder.getPrimaryKey()))));
 		} else {
 			result = DLFolderLocalServiceUtil.dynamicQuery(
 				DynamicQueryFactoryUtil.forClass(DLFolder.class).add(
 				PropertyFactoryUtil.forName("name").eq(name)).add(
 				PropertyFactoryUtil.forName("parentFolderId")
 				.eq(new Long(0L))));
 		}
 
 		if(result.size()==0) {
 			return null;
 		}
 		return result.get(0);
 	}
 
 	/**
 	 * @return <code>true</code> if <code>ancestorFolder</code>
 	 * is ancestor of <code>dlFolder</code>.
 	 */
 	public static boolean isAncestorFolder(
 			DLFolder dlFolder, DLFolder ancestorFolder)
 				throws PortalException, SystemException {
 
 		DLFolder parentFolder = dlFolder.getParentFolder();
 		while(parentFolder != null) {
 			if(parentFolder.equals(ancestorFolder)) {
 				return true;
 			}
 			parentFolder = parentFolder.getParentFolder();
 		}
 		return false;
 	}
 
 	/**
 	 * @return <code>true</code> if the folder
 	 * with <code>ancestorFolderId</code>
 	 * is ancestor of <code>folderId</code>.
 	 */
 	public static boolean isAncestorFolder(
 			long folderId, long ancestorFolderId)
 				throws PortalException, SystemException {
 
 		DLFolder dlFolder = DLFolderLocalServiceUtil.getDLFolder(folderId);
		if(dlFolder!=null && ancestorFolderId==0) {
			return true;
		}
 		DLFolder ancestorFolder = DLFolderLocalServiceUtil.getDLFolder(ancestorFolderId);
 		DLFolder parentFolder = dlFolder.getParentFolder();
 		while(parentFolder != null) {
 			if(parentFolder.equals(ancestorFolder)) {
 				return true;
 			}
 			parentFolder = parentFolder.getParentFolder();
 		}
 		return false;
 	}
 
 	/**
 	 * @return <code>true</code> if <code>parentFolderName</code>
 	 * is ancestor of <code>dlFileEntry</code>.
 	 */
 	public static boolean isAncestorFolder(
 			DLFileEntry dlFileEntry, String parentFolderName)
 				throws PortalException, SystemException {
 
 		DLFolder parentFolder = dlFileEntry.getFolder();
 		while(parentFolder != null) {
 			if(parentFolder.getName().equals(parentFolderName)) {
 				return true;
 			}
 			parentFolder = parentFolder.getParentFolder();
 		}
 		return false;
 	}
 
 	public static Map<Long, String[]> getRoleIdsToActionIds(long companyId, long dlFolderId)
 			throws PortalException, SystemException {
 
 		long[] roleIds = RolePermissionsUtils.getRoleHasPermissions(
 				companyId,
 				DLFolder.class.getName(),
 				ResourceConstants.SCOPE_INDIVIDUAL,
 				String.valueOf(dlFolderId));
 		return RolePermissionsUtils.getRoleIdsToActionIds(
 				companyId,
 				DLFolder.class.getName(),
 				ResourceConstants.SCOPE_INDIVIDUAL,
 				String.valueOf(dlFolderId),
 				roleIds, DLFOLDER_PERMISSIONS);
 	}
 }
