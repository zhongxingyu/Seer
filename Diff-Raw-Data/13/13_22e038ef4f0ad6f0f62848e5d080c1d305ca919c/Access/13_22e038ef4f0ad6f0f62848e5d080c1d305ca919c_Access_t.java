 package org.b3mn.poem;
 
 import java.io.Serializable;
 
 public class Access implements Serializable {
 	
 	private static final long serialVersionUID = 2216329517850461299L;
 
     private long context_id;
     private String context_name;
     private long subject_id;
     private String subject_name;
     private long object_id;
     private String object_name;
     private String access_scheme;	
     private String access_term;
     private long access_id;
     private String plugin_relation;
     private String scheme;
     private String term;
 
 	public String getAccess_scheme() {
 		return access_scheme;
 	}
 	public String getAccess_term() {
 		return access_term;
 	}
 	public long getContext_id() {
 		return context_id;
 	}
 	public String getContext_name() {
 		return context_name;
 	}
 	public long getObject_id() {
 		return object_id;
 	}
 	public String getObject_name() {
 		return object_name;
 	}
 	public String getPlugin_relation() {
 		return plugin_relation;
 	}
 	public String getScheme() {
 		return scheme;
 	}
 	public long getSubject_id() {
 		return subject_id;
 	}
 	public long getAccess_id() {
 		return access_id;
 	}
 	public String getSubject_name() {
 		return subject_name;
 	}
 	public String getTerm() {
 		return term;
 	}
 	public void setSubject_id(long subject_id) {
 		this.subject_id = subject_id;
 	}
 	public void setSubject_name(String subject_name) {
 		this.subject_name = subject_name;
 	}
 	public void setObject_id(long object_id) {
 		this.object_id = object_id;
 	}
 	public void setObject_name(String object_name) {
 		this.object_name = object_name;
 	}
 	public void setContext_id(long context_id) {
 		this.context_id = context_id;
 	}
 	public void setContext_name(String context_name) {
 		this.context_name = context_name;
 	}
 	public void setAccess_scheme(String access_scheme) {
 		this.access_scheme = access_scheme;
 	}
 	public void setAccess_term(String access_term) {
 		this.access_term = access_term;
 	}
 	public void setPlugin_relation(String plugin_relation) {
 		this.plugin_relation = plugin_relation;
 	}
 	public void setScheme(String scheme) {
 		this.scheme = scheme;
 	}
 	public void setTerm(String term) {
 		this.term = term;
 	}
 	public void setAccess_id(long access_id) {
 		this.access_id = access_id;
 	}
 	public String getUrl() {
		return this.getObject_name() + "/access?id=" +  this.getAccess_id();
 	}
 	public String getSubject() {
 		return this.getContext_name();
 	}
 	public String getPredicate() {
 		return this.getAccess_term();
 	}
 	
 	public int hashCode() {
 		int result = 0;
 		result+= access_id;
 		result+= context_id;
 		result+= context_name.hashCode();
 		result+= subject_id;
 		result+= subject_name.hashCode();
 		result+= object_id;
 		result+= object_name.hashCode();
 		result+= access_scheme.hashCode();	
 		result+= access_term.hashCode();
 		result+= plugin_relation.hashCode();
 		result+= scheme.hashCode();
 		result+= term.hashCode();
         return result;
 	}
 	
 	public boolean equals(Object other) {
 		return false;
 	}
 }
