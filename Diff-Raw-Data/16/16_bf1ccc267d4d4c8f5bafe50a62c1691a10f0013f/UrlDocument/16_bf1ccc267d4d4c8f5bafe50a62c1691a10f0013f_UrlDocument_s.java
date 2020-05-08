 package com.softeoz.bookmarktree.domain;
 
 import org.primefaces.model.TreeNode;
 
 public class UrlDocument {
 
 	private String name;
 
 	private String url;
 
 	private String fullPath;
 
 	private String parentPath;
 
 	private TreeNode parentNode;
 
 	public UrlDocument() {
 	}
 
 	public UrlDocument(String name, String url, TreeNode parent) {
 
 		setName(name);
 		setUrl(url);
 		setParentNode(parent);
 		pathUpdate();
 
 	}
 
 	public void pathUpdate() {
 		Path fullPathProxy = new Path();
		// Path parentPath = new Path();
 		if (isTop()) {
 			// setParentPath(null);
 			// setFullPath(null);
			// parentPath.setValue(null);
 			fullPathProxy.setValue(null);
 		} else {
 			if (isRoot()) {
 				// setParentPath("");
 				// setFullPath(name);
				// parentPath.setValue("");
 				fullPathProxy.setValue(name);
 			} else {
 				// setParentPath(((UrlDocument) getParentNode().getData())
 				// .getFullPath());
 				// setFullPath(parentPath + "/" + name);
				// parentPath.setValue(((UrlDocument) getParentNode().getData())
				// .getFullPath());
 				fullPathProxy.add(name);
 			}
 		}
 
 		fullPath = fullPathProxy.getValue();
 	}
 
 	public String getFullPath() {
 		return fullPath;
 	}
 
 	public void setFullPath(String path) {
 		this.fullPath = path;
 	}
 
 	public boolean isRoot() {
 		return getParentNode().getParent() == null;
 	}
 
 	public boolean isTop() {
 		return getParentNode() == null;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 		pathUpdate();
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public TreeNode getParentNode() {
 		return parentNode;
 	}
 
 	public void setParentNode(TreeNode node) {
 		this.parentNode = node;
 	}
 
 	public String getParentPath() {
 		return parentPath;
 	}
 
 	public void setParentPath(String parentPath) {
 		this.parentPath = parentPath;
 	}
 
 	@Override
 	public String toString() {
 		return fullPath;
 	}
 
 }
