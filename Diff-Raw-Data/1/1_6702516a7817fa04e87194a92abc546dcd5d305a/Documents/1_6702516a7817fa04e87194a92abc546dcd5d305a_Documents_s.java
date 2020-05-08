 package com.optaros.alfresco.docasu.wcs;
 
 /*
  *    Copyright (C) 2008 Optaros, Inc. All rights reserved.
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program. If not, see <http://www.gnu.org/licenses/>.
  *    
  */
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.alfresco.model.ContentModel;
 import org.alfresco.repo.template.TemplateNode;
 import org.alfresco.service.cmr.model.FileFolderService;
 import org.alfresco.service.cmr.model.FileInfo;
 import org.alfresco.service.cmr.repository.ContentData;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.cmr.repository.StoreRef;
 import org.alfresco.service.cmr.repository.TemplateImageResolver;
 import org.alfresco.service.cmr.security.AccessStatus;
 import org.alfresco.service.cmr.security.PermissionService;
 import org.alfresco.service.cmr.version.Version;
 import org.alfresco.service.cmr.version.VersionService;
 import org.alfresco.service.namespace.QName;
 import org.alfresco.web.scripts.DeclarativeWebScript;
 import org.alfresco.web.scripts.WebScriptRequest;
 import org.alfresco.web.scripts.WebScriptStatus;
 import org.apache.commons.logging.LogFactory;
 
 public class Documents extends DeclarativeWebScript {
 
 	private static final org.apache.commons.logging.Log log = LogFactory.getLog(Documents.class);
 
 	private static final String EDITABLE_EXTENSION_REGEX = "txt|html?";
 
 	public Map<String, Object> executeImpl(WebScriptRequest req, WebScriptStatus status) {
 		
 		String nodeId = req.getParameter("nodeId");
 		String start = req.getParameter("start");
 		String limit = req.getParameter("limit");
 		String sort = req.getParameter("sort");
 		String dir = req.getParameter("dir");
 		
 		if (log.isDebugEnabled()) {
 			log.debug("nodeId = " + nodeId);
 			log.debug("start = " + start);
 			log.debug("limit = " + limit);
 			log.debug("sort = " + sort);
 			log.debug("dir = " + dir);
 		}
 		
 		if (start == null) {
 			// TODO
 			log.warn("Setting start to 0 TODO refactor ui");
 			start = "0";
 		}
 		if (limit == null) {
 			// TODO
 			log.warn("Setting limit to 50 TODO refactor ui");
 			limit = "50";
 		}
 
 		StoreRef storeRef = new StoreRef("workspace://SpacesStore");
 		NodeRef companyHome = getRepositoryContext().getCompanyHome();
 		NodeRef nodeRef = companyHome;
 
 		if (nodeId != null) {
 			nodeRef = new NodeRef(storeRef, nodeId);
 		}
 		
 		FileFolderService fileFolderService = getServiceRegistry().getFileFolderService();
 		NodeService nodeService = getServiceRegistry().getNodeService();
 		PermissionService permissionService = getServiceRegistry().getPermissionService();
 		VersionService versionService = getServiceRegistry().getVersionService();
 
 		TemplateImageResolver imageResolver = getWebScriptRegistry().getTemplateImageResolver();
 
 		FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
 		String path = generatePath(nodeService, fileFolderService, nodeRef);
 		List<FileInfo> children = fileFolderService.list(nodeRef);
 		
 		if (log.isDebugEnabled()) {
 			log.debug("node is folder = " + fileInfo.isFolder());
 			log.debug("node name = " + fileInfo.getName());
 			log.debug("node path = " + path);
 			log.debug("node children count = " + children.size());
 		}
 
 		// Sorting
 		if (sort!=null) {
 			ColumnComparator comparator = new ColumnComparator(sort, !"DESC".equals(dir));
 			Collections.sort(children, comparator);
 		}
 		// Paging
 		int elementCount = children.size();
 		if (start != null && limit != null) {
 			try {
 				int fromIndex = Integer.parseInt(start);
 				if (fromIndex < 0) fromIndex = 0;
 				int count = Integer.parseInt(limit);
 				int toIndex = fromIndex + count;
 				if (toIndex > elementCount) toIndex = elementCount;
 				if (fromIndex > toIndex) fromIndex = toIndex;
 				children = children.subList(fromIndex, toIndex);
 			}
 			catch (NumberFormatException nfe) {
 				log.info("invalid start or limit param");
 			}
 		}
 		
 		Object[] rows = new Object[children.size()]; 
 		int i = 0;
 		for (FileInfo info : children) {
 			TemplateNode templateNode = new TemplateNode(info.getNodeRef(), getServiceRegistry(), imageResolver);
 			Map<String, Object> row = new HashMap<String, Object>();
 			row.put("nodeId", info.getNodeRef().getId());
 			row.put("name", info.getName());
 			row.put("title", getProperty(info, ContentModel.PROP_TITLE, ""));
 			row.put("modified", info.getModifiedDate());
 			row.put("created", info.getCreatedDate());
 			row.put("author", getProperty(info, ContentModel.PROP_AUTHOR, ""));
 			row.put("creator", getProperty(info, ContentModel.PROP_CREATOR, ""));
 			row.put("description", getProperty(info, ContentModel.PROP_DESCRIPTION, ""));
 			row.put("modifier", getProperty(info, ContentModel.PROP_MODIFIER, ""));
 			Version currentVersion = versionService.getCurrentVersion(info.getNodeRef());
 			if (currentVersion != null) {
 				row.put("versionable", true);
 				row.put("version", currentVersion.getVersionLabel());
 			}
 			else {
 				row.put("versionable", false);
 				row.put("version", "Versioning not enabled");
 			}
 			row.put("writePermission", AccessStatus.ALLOWED == permissionService.hasPermission(info.getNodeRef(), "Write"));
 			row.put("createPermission", AccessStatus.ALLOWED == permissionService.hasPermission(info.getNodeRef(), "CreateChildren"));
 			row.put("deletePermission", AccessStatus.ALLOWED == permissionService.hasPermission(info.getNodeRef(), "Delete"));
 			row.put("locked", templateNode.getIsLocked());
 			row.put("isWorkingCopy", nodeService.hasAspect(info.getNodeRef(), ContentModel.ASPECT_WORKING_COPY));
 			row.put("url", templateNode.getUrl());
 			row.put("downloadUrl", templateNode.getDownloadUrl());
 			row.put("icon16", templateNode.getIcon16());
 			row.put("icon32", templateNode.getIcon32());
 			row.put("icon64", templateNode.getIcon64());
 			row.put("editable", isEditable(info));
 			if (info.isFolder()) {
 				row.put("isFolder", true);
 			}
 			else {
 				row.put("size", info.getContentData().getSize());
 				row.put("mimetype", info.getContentData().getMimetype());
 			}
 			rows[i++] = row;
 		}
 		
 		Map<String, Object> model = new HashMap<String, Object>();
 		model.put("total", elementCount);
 		model.put("path", path);
 		model.put("randomNumber", Math.random());
 		model.put("folderName", fileInfo.getName());
 		model.put("rows", rows);
 		return model;
 	}
 
 	private String generatePath(NodeService nodeService, FileFolderService fileFolderService, NodeRef nodeRef) {
 		LinkedList<NodeRef> nodes = new LinkedList<NodeRef>();
 		while (nodeRef != null) {
 			nodes.add(0, nodeRef);
 			nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
 		}
 		StringBuffer path = new StringBuffer();
 		for (NodeRef pathElement : nodes) {
 			FileInfo fileInfo = fileFolderService.getFileInfo(pathElement);
 			// root has no fileInfo == null !
 			if (fileInfo != null) {
 				path.append("/");
 				path.append(fileInfo.getName());
 			}
 		}
 		return path.toString();
 	}
 
 	private boolean isEditable(FileInfo info) {
 		String fileName = info.getName();
 		String extension = fileName.substring(fileName.lastIndexOf('.')+1, fileName.length()).toLowerCase();
 		if (extension.matches(EDITABLE_EXTENSION_REGEX)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private Serializable getProperty(FileInfo info, QName property, String defaultValue) {
 		if (info.getProperties().containsKey(property)) {
 			return info.getProperties().get(property);
 		}
 		else {
 			return defaultValue;
 		}
 	}
 
 	private class ColumnComparator implements Comparator<FileInfo>{
 
 		private final String column;
 		private final boolean ascending;
 		
 		public ColumnComparator(String column, boolean ascending) {
 			this.column = column;
 			this.ascending = ascending;
 		}
 		
 		public int compare(FileInfo f1, FileInfo f2) {
 			if (column.equals("name")) {
 				String name1 = f1.getName();
 				String name2 = f2.getName();
 				return (ascending?name1.compareTo(name2):name2.compareTo(name1));
 			}
 			else if (column.equals("size")) {
 				ContentData data1 = f1.getContentData();
 				long size1 = (data1!=null?data1.getSize():0);
 				ContentData data2 = f2.getContentData();
 				long size2 = (data2!=null?data2.getSize():0);
 				long diff = (ascending?size1-size2:size2-size1);
 				if (diff > 0) return 1;
 				else if (diff < 0) return -1;
 				else return 0;
 			}
 			else if (column.equals("modified")) {
 				Date date1 = f1.getModifiedDate();
 				Date date2 = f2.getModifiedDate();
 				return (ascending?date1.compareTo(date2):date2.compareTo(date1));
 			}
 			else if (column.equals("created")) {
 				Date date1 = f1.getCreatedDate();
 				Date date2 = f2.getCreatedDate();
 				return (ascending?date1.compareTo(date2):date2.compareTo(date1));
 			}
 			else if (column.equals("creator")) {
 				String creator1 = (String)getProperty(f1, ContentModel.PROP_CREATOR, "");
 				String creator2 = (String)getProperty(f2, ContentModel.PROP_CREATOR, "");
 				return (ascending?creator1.compareTo(creator2):creator2.compareTo(creator1));
 			}
 			else {
 				log.error("Sorting not implemented for column = " + column);
 				return 0;
 			}
 		}
 	}
 }
