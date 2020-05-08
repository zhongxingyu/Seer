 package com.pehrs.spring.api.doc.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 /**
  * Holds the info about the Spring MVS REST Controller
  * 
  * @author matti
  *
  */
 public class ControllerDesc implements Comparable<ControllerDesc>{
 
 	String name;
 	String pkgName;
 	String urlRoot = "";
 	String comment = "";
 	
 	List<MethodDesc> methods = new ArrayList<MethodDesc>();
 	
 	SortedSet<PathInfo> pathInfos = new TreeSet<PathInfo>();
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getUrlRoot() {
 		if(urlRoot==null) return "";
 		return urlRoot;
 	}
 
 	public void setUrlRoot(String urlRoot) {
 		this.urlRoot = urlRoot;
 	}
 
 	public void addMethod(MethodDesc method) {
 		methods.add(method);
 	}
 
 	public void removeMethod(MethodDesc method) {
 		methods.remove(method);
 	}
 	
 	public Iterator<MethodDesc> getMethodIterator() {
 		return methods.iterator();
 	}
 
 	public List<MethodDesc> getMethods() {
 		return methods;
 	}
 
 	public void setMethods(List<MethodDesc> methods) {
 		this.methods = methods;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		if(comment==null) this.comment = "";
 		else this.comment = comment;
 	}
 
 	public String getCommentTextFirstSentence() {
 		if(comment==null) {
 			return "";
 		}
 		int index = comment.indexOf('@');
 		if(index!=-1) {
			return comment.substring(0, index + 1);
 		}
		return comment;
 	}
 
 	public String getPkgName() {
 		return pkgName;
 	}
 
 	public String getPkgNameAsPath() {
 		if(pkgName==null) return "";
 		return pkgName.replace('.', '/');
 	}
 
 	public void setPkgName(String pkgName) {
 		this.pkgName = pkgName;
 	}
 
 	public Iterator<PathInfo> getPathInfoIterator() {
 		return pathInfos.iterator();
 	}
 	
 	public SortedSet<PathInfo> getPathInfos() {
 		return pathInfos;
 	}
 
 	public void setPathInfos(SortedSet<PathInfo> pathInfos) {
 		this.pathInfos = pathInfos;
 	}
 	
 	public String getPathInfoRoot() {
 		if(urlRoot!=null && urlRoot.length()>0) {
 			return urlRoot;
 		}
 		String res = null;
 		for(PathInfo info: pathInfos) {
 			if(res == null || info.getRequestPath().length() < res.length()) {
 				res = info.getRequestPath();
 			}
 		}
 		return (res==null?"":res);
 	}
 
 	@Override
 	public String toString() {
 		return "ClassDesc [name=" + name + ", pkgName=" + pkgName
 				+ ", urlRoot=" + urlRoot + ", comment=" + comment
 				+ ", methods=" + methods + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
 		result = prime * result + ((methods == null) ? 0 : methods.hashCode());
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((pkgName == null) ? 0 : pkgName.hashCode());
 		result = prime * result + ((urlRoot == null) ? 0 : urlRoot.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ControllerDesc other = (ControllerDesc) obj;
 		if (comment == null) {
 			if (other.comment != null)
 				return false;
 		} else if (!comment.equals(other.comment))
 			return false;
 		if (methods == null) {
 			if (other.methods != null)
 				return false;
 		} else if (!methods.equals(other.methods))
 			return false;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (pkgName == null) {
 			if (other.pkgName != null)
 				return false;
 		} else if (!pkgName.equals(other.pkgName))
 			return false;
 		if (urlRoot == null) {
 			if (other.urlRoot != null)
 				return false;
 		} else if (!urlRoot.equals(other.urlRoot))
 			return false;
 		return true;
 	}
 
 	@Override
 	public int compareTo(ControllerDesc other) {
 		String cmp = name;
 		if(pkgName!=null) {
 			cmp = pkgName+name;
 		}
 		String ocmp = other.name;
 		if(other.pkgName!=null) {
 			ocmp = other.pkgName + other.name;
 		}
 		return cmp.compareTo(ocmp);
 	}
 
 	public String getJSId() {
 		return getPkgName().replace('.', '_') +"_"+getName();
 	}
 	
 	
 }
