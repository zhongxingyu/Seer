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
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.alfresco.model.ContentModel;
 import org.alfresco.repo.model.Repository;
 import org.alfresco.repo.template.TemplateNode;
 import org.alfresco.repo.web.scripts.RepositoryImageResolver;
 import org.alfresco.service.ServiceRegistry;
 import org.alfresco.service.cmr.model.FileFolderService;
 import org.alfresco.service.cmr.model.FileInfo;
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
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.optaros.alfresco.docasu.wcs.helper.NodeRefWrapper;
 import com.optaros.alfresco.docasu.wcs.helper.NodeRefWrapperComparator;
 
 /**
  * @author Jean-Luc Geering
  */
 public class AbstractDocumentWebScript extends DeclarativeWebScript {
 
 	private static final Log log = LogFactory.getLog(AbstractDocumentWebScript.class);
 
 	private static final String EDITABLE_EXTENSION_REGEX = "txt|html?";
 
 	protected static final String PARAM_NODE_ID = "nodeId";
 	protected static final String PARAM_CATEGORY_ID = "categoryId";
 	protected static final String PARAM_START = "start";
 	protected static final String PARAM_LIMIT = "limit";
 	protected static final String PARAM_SORT = "sort";
 	protected static final String PARAM_DIR = "dir";
 
 	protected static final String KEYWORD_ROWS = "rows";
 
 	protected CustomFileFolderService customFileFolderService;
 	protected FileFolderService fileFolderService;
 	protected NodeService nodeService;
 	protected PermissionService permissionService;
 	protected VersionService versionService;
 
 	// the new beans for Alfresco 3
 	protected ServiceRegistry serviceRegistry;
 	protected RepositoryImageResolver repositoryImageResolver;
 	protected Repository repository;
 
 	protected TemplateImageResolver imageResolver;
 
 	protected StoreRef storeRef = new StoreRef("workspace://SpacesStore");
 
 	public void setCustomFileFolderService(CustomFileFolderService customFileFolderService) {
 		this.customFileFolderService = customFileFolderService;
 	}
 
 	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
 		this.serviceRegistry = serviceRegistry;
 	}
 
 	public void setRepositoryImageResolver(RepositoryImageResolver repositoryImageResolver) {
 		this.repositoryImageResolver = repositoryImageResolver;
 	}
 
 	public void setRepository(Repository repository) {
 		this.repository = repository;
 	}
 
 	protected void initServices() {
 		fileFolderService = serviceRegistry.getFileFolderService();
 		nodeService = serviceRegistry.getNodeService();
 		permissionService = serviceRegistry.getPermissionService();
 		versionService = serviceRegistry.getVersionService();
 		imageResolver = repositoryImageResolver.getImageResolver();
 	}
 
 	protected void readParam(Map<String, String> params, String key, String value) {
 		if (value != null && value.length() > 0) {
 			params.put(key, value);
 		}
 	}
 
 	protected Map<String, String> readParams(WebScriptRequest req) {
 		Map<String, String> params = new HashMap<String, String>();
 
 		readParam(params, PARAM_NODE_ID, req.getParameter(PARAM_NODE_ID));
 		readParam(params, PARAM_CATEGORY_ID, req.getParameter(PARAM_CATEGORY_ID));
 		readParam(params, PARAM_START, req.getParameter(PARAM_START));
 		readParam(params, PARAM_LIMIT, req.getParameter(PARAM_LIMIT));
 		readParam(params, PARAM_SORT, req.getParameter(PARAM_SORT));
 		readParam(params, PARAM_DIR, req.getParameter(PARAM_DIR));
 
 		if (log.isDebugEnabled()) {
 			log.debug("PARAM nodeId = " + params.get(PARAM_NODE_ID));
 			log.debug("PARAM categoryId = " + params.get(PARAM_CATEGORY_ID));
 			log.debug("PARAM start = " + params.get(PARAM_START));
 			log.debug("PARAM limit = " + params.get(PARAM_LIMIT));
 			log.debug("PARAM sort = " + params.get(PARAM_SORT));
 			log.debug("PARAM dir = " + params.get(PARAM_DIR));
 		}
 
 		// set default paging values.
 		setPagingValues(params);
 
 		return params;
 	}
 
 	private void setPagingValues(Map<String, String> params) {
 		if (!params.containsKey(PARAM_START)) {
 			// TODO
 			log.warn("Setting start to 0 TODO refactor ui");
 			params.put(PARAM_START, "0");
 		}
 		if (!params.containsKey(PARAM_LIMIT)) {
 			// TODO
 			log.warn("Setting limit to 50 TODO refactor ui");
 			params.put(PARAM_LIMIT, "50");
 		}
 	}
 
 	protected List<FileInfo> toFileInfo(List<NodeRef> nodes) {
 		List<FileInfo> result = new ArrayList<FileInfo>();
 		for (NodeRef node : nodes) {
 			if (permissionService.hasPermission(node, PermissionService.READ) == AccessStatus.ALLOWED) {
 				result.add(fileFolderService.getFileInfo(node));
 			} else {
 				// ignore node
 				log.debug("User does not have permission to access node: " + node.getId() + ". Will be removed from result set!");
 			}
 		}
 		return result;
 	}
 
 	private List<NodeRefWrapper> sort(List<NodeRefWrapper> nodes, QName property, boolean ascending) {
 		NodeRefWrapperComparator comparator = new NodeRefWrapperComparator(property, ascending);
 		Collections.sort(nodes, comparator);
 		return nodes;
 	}
 
 	protected List<NodeRef> sort(List<NodeRef> nodeRefList, Map<String, String> params) {
 		if (params.get(PARAM_SORT) != null && !"".equals(params.get(PARAM_SORT))) {
 			QName property = getSortProperty(params);
 			boolean ascending = !"DESC".equals(params.get(PARAM_DIR));
 			List<NodeRefWrapper> nodeRefWrapperList = NodeRefWrapper.createNodeRefWrappers(nodeRefList, nodeService, property);
 			nodeRefWrapperList = sort(nodeRefWrapperList, property, ascending);
 			return NodeRefWrapper.getNodeRefList(nodeRefWrapperList);
 		}
 		return nodeRefList;
 	}
 
 	protected QName getSortProperty(Map<String, String> params) {
 		String sortParam = params.get(PARAM_SORT);
 		if (sortParam != null && !"".equals(sortParam)) {
 			if (sortParam.equals("name")) {
 				return ContentModel.PROP_NAME;
 			} else if (sortParam.equals("size")) {
 				return ContentModel.PROP_CONTENT;
 			} else if (sortParam.equals("modified")) {
 				return ContentModel.PROP_MODIFIED;
 			} else if (sortParam.equals("created")) {
 				return ContentModel.PROP_CREATED;
 			} else if (sortParam.equals("creator")) {
 				return ContentModel.PROP_CREATOR;
 			}
 		}
 		return null;
 	}
 
 	protected List<NodeRef> doPaging(List<NodeRef> nodes, Map<String, String> params) {
 		int elementCount = nodes.size();
 		if (params.get(PARAM_START) != null && params.get(PARAM_LIMIT) != null) {
 			try {
 				// Read and adapt fromIndex and toIndex
 				int fromIndex = Integer.parseInt(params.get(PARAM_START));
 				if (fromIndex < 0)
 					fromIndex = 0;
 				int count = Integer.parseInt(params.get(PARAM_LIMIT));
 				int toIndex = fromIndex + count;
 				if (toIndex > elementCount)
 					toIndex = elementCount;
 				if (fromIndex > toIndex)
 					fromIndex = toIndex;
 				// Apply
 				nodes = nodes.subList(fromIndex, toIndex);
 			} catch (NumberFormatException nfe) {
 				log.info("invalid start or limit param");
 			}
 		}
 		return nodes;
 	}
 
 	protected Map<String, Object> toModelRow(FileInfo fileInfo) {
 
 		TemplateNode templateNode = new TemplateNode(fileInfo.getNodeRef(), serviceRegistry, imageResolver);
 		Map<String, Object> row = new HashMap<String, Object>();
 		row.put("nodeId", fileInfo.getNodeRef().getId());
 		row.put("name", fileInfo.getName());
 		row.put("title", getProperty(fileInfo, ContentModel.PROP_TITLE, ""));
 		row.put("modified", fileInfo.getModifiedDate());
 		row.put("created", fileInfo.getCreatedDate());
 		row.put("author", getProperty(fileInfo, ContentModel.PROP_AUTHOR, ""));
 		row.put("creator", getProperty(fileInfo, ContentModel.PROP_CREATOR, ""));
 		row.put("description", getProperty(fileInfo, ContentModel.PROP_DESCRIPTION, ""));
 		row.put("modifier", getProperty(fileInfo, ContentModel.PROP_MODIFIER, ""));
 		Version currentVersion = versionService.getCurrentVersion(fileInfo.getNodeRef());
 		if (currentVersion != null) {
 			row.put("versionable", true);
 			row.put("version", currentVersion.getVersionLabel());
 		} else {
 			row.put("versionable", false);
 			row.put("version", "Versioning not enabled");
 		}
 		row.put("writePermission", AccessStatus.ALLOWED == permissionService.hasPermission(fileInfo.getNodeRef(), "Write"));
 		row.put("createPermission", AccessStatus.ALLOWED == permissionService.hasPermission(fileInfo.getNodeRef(), "CreateChildren"));
 		row.put("deletePermission", AccessStatus.ALLOWED == permissionService.hasPermission(fileInfo.getNodeRef(), "Delete"));
 		row.put("locked", templateNode.getIsLocked());
 		row.put("isWorkingCopy", nodeService.hasAspect(fileInfo.getNodeRef(), ContentModel.ASPECT_WORKING_COPY));
 		row.put("url", getUrl(templateNode));
 		row.put("downloadUrl", templateNode.getDownloadUrl());
 		row.put("icon16", templateNode.getIcon16());
 		row.put("icon32", templateNode.getIcon32());
 		row.put("icon64", templateNode.getIcon64());
 		row.put("editable", isEditable(fileInfo));
 		if (fileInfo.isFolder()) {
 			row.put("isFolder", true);
 		} else {
			row.put("size", fileInfo.getContentData().getSize());
			row.put("mimetype", fileInfo.getContentData().getMimetype());
 		}
 		return row;
 	}
 
 	private String getUrl(TemplateNode templateNode) {
 		if (templateNode.getIsDocument()) {
 			return templateNode.getUrl();
 		} else if (templateNode.getIsContainer()) {
 			// if folder, then return proper URL to load DoCASU interface
 			// TODO: move this outside source code
 			return "/wcs/docasu/ui?nodeId=" + templateNode.getId();
 		}
 		return "";
 	}
 
 	/**
 	 * Returns the absolute repository path for nodeRef.
 	 * 
 	 * @param nodeRef
 	 * @return
 	 */
 	protected String generatePath(NodeRef nodeRef) {
 		LinkedList<FileInfo> nodes = new LinkedList<FileInfo>();
 		while (nodeRef != null) {
 			// only nodes that are accessible get in Path
 			if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED) {
 				nodes.add(0, fileFolderService.getFileInfo(nodeRef));
 				nodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
 			} else {
 				log.warn("User does not have permission to access node: " + nodeRef.getId() + ". Repository path will be incomplete!");
 				nodeRef = null;
 			}
 		}
 		StringBuffer path = new StringBuffer();
 		for (FileInfo fileInfo : nodes) {
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
 		String extension = fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).toLowerCase();
 		if (extension.matches(EDITABLE_EXTENSION_REGEX)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	private String getProperty(FileInfo info, QName property, String defaultValue) {
 		if (info.getProperties().containsKey(property)) {
 			Serializable serializable = info.getProperties().get(property);
 			if (serializable != null) {
 				return serializable.toString();
 			} else {
 				return defaultValue;
 			}
 		} else {
 			return defaultValue;
 		}
 	}
 
 }
