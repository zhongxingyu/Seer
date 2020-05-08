 /**
  * 
  */
 package org.iplantc.core.uidiskresource.client.util;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.iplantc.core.jsonutil.JsonUtil;
 import org.iplantc.core.uicommons.client.models.CommonModelAutoBeanFactory;
 import org.iplantc.core.uicommons.client.models.HasId;
 import org.iplantc.core.uicommons.client.models.UserInfo;
 import org.iplantc.core.uidiskresource.client.models.DiskResource;
 import org.iplantc.core.uidiskresource.client.models.File;
 import org.iplantc.core.uidiskresource.client.models.Folder;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.gwt.json.client.JSONArray;
 import com.google.web.bindery.autobean.shared.AutoBean;
 import com.google.web.bindery.autobean.shared.AutoBeanCodex;
 import com.google.web.bindery.autobean.shared.Splittable;
 import com.google.web.bindery.autobean.shared.impl.StringQuoter;
 
 /**
  * @author sriram
  *
  */
 public class DiskResourceUtil {
 
     /**
      * Parse the parent folder from a path.
      * 
      * @param path the path to parse.
      * @return the parent folder.
      */
     public static String parseParent(String path) {
         LinkedList<String> split = Lists.newLinkedList(Splitter.on("/").trimResults().omitEmptyStrings().split(path));
         split.removeLast();
         return "/".concat(Joiner.on("/").join(split));
     }
 
     /**
      * Parse the display name from a path.
      * 
      * @param path the path to parse.
      * @return the display name.
      */
     public static String parseNameFromPath(String path) {
         LinkedList<String> split = Lists.newLinkedList(Splitter.on("/").trimResults().omitEmptyStrings().split(path));
 
         return split.removeLast();
     }
     
     public static List<String> parseNamesFromIdList(Iterable<String> idList) {
         List<String> nameList = Lists.newArrayList();
         for (String s : idList) {
             nameList.add(parseNameFromPath(s));
         }
         return nameList;
     }
 
     public static String asCommaSeperatedNameList(Iterable<String> idList) {
         return Joiner.on(",").join(parseNamesFromIdList(idList));
     }
 
     public static boolean isOwner(DiskResource resource) {
         return resource.getPermissions().isOwner();
     }
 
     public static boolean isOwner(Iterable<DiskResource> resources) {
         if (resources == null) {
             return false;
         }
 
         // Use predicate to determine if user is owner of all disk resources
         for (DiskResource dr : resources) {
             if (!dr.getPermissions().isOwner()) {
                 return false;
             }
         }
 
         return true;
     }
 
     /**
      * Determines if the user is an owner of one item in the given resources.
      * 
      * @param resources
      * @return
      */
     public static boolean hasOwner(Iterable<DiskResource> resources) {
         if (resources == null) {
             return false;
         }
 
         for (DiskResource dr : resources) {
             if (dr.getPermissions().isOwner()) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Determines if the given <code>DiskResource</code> is a direct child of the given parent <code>Folder</code>.
      * @param parent
      * @param resource
      * @return
      */
     public static boolean isChildOfFolder(Folder parent, DiskResource resource) {
         return parseParent(resource.getId()).equals(parent.getId());
     }
 
     /**
      * Determines if the given folder is a descendant of the given ancestor folder.
      * This is done by verifying that the given folder's path starts with the ancestor's path.
      *  
      * @param ancestor the ancestor folder.
      * @param folder the folder whose ancestry is verified.
      * @return true if the folder is a descendant of the given ancestor, false otherwise.
      */
     public static boolean isDescendantOfFolder(Folder ancestor, Folder folder) {
         return folder.getId().startsWith(ancestor.getId());
     }
 
     public static boolean isMovable(Iterable<DiskResource> dropData) {
         return isOwner(dropData);
     }
 
     public static boolean canUploadTo(DiskResource resource) {
         return isOwner(resource) && (resource instanceof Folder) && !inTrash(resource);
     }
 
     public static boolean inTrash(DiskResource resource) {
         return resource != null && resource.getId().startsWith(UserInfo.getInstance().getTrashPath());
     }
 
     public static <R extends DiskResource> boolean containsFolder(Iterable<R> selection) {
         for(DiskResource resource : selection){
             if(resource instanceof Folder){
                 return true;
             }
         }
         return false;
     }
 
     public static <R extends DiskResource> Iterable<File> extractFiles(Iterable<R> diskresources) {
         List<File> files = Lists.newArrayList();
         for (DiskResource dr : diskresources) {
             if (dr instanceof File) {
                 files.add((File)dr);
             }
         }
         return files;
     }
 
     public static <R extends DiskResource> Iterable<Folder> extractFolders(Iterable<R> diskresources) {
         List<Folder> folders = Lists.newArrayList();
         for (DiskResource dr : diskresources) {
             if (dr instanceof Folder) {
                 folders.add((Folder)dr);
             }
         }
         return folders;
     }
 
     public static <R extends HasId> List<String> asStringIdList(Iterable<R> diskResourceList) {
         List<String> ids = Lists.newArrayList();
         for (R dr : diskResourceList) {
             ids.add(dr.getId());
         }
 
         return ids;
     }
 
     public static <R extends HasId> Splittable createStringIdListSplittable(Iterable<R> hasIdList) {
         JSONArray jArr = JsonUtil.buildArrayFromStrings(asStringIdList(hasIdList));
 
         return StringQuoter.split(jArr.toString());
     }
 
     public static Splittable createSplittableFromStringList(List<String> strings) {
         return StringQuoter.split(JsonUtil.buildArrayFromStrings(strings).toString());
     }
 
     public static HasId getFolderIdFromFile(CommonModelAutoBeanFactory cFactory, File file) {
         AutoBean<HasId> hAb = AutoBeanCodex.decode(cFactory, HasId.class, "{\"id\": \"" + parseParent(file.getId()) + "\"}");
         return hAb.as();
     }
 
 }
