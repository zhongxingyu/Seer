 package com.github.easydoc.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.github.easydoc.model.criteria.DocSearchCriteria;
 
 public class Model implements Serializable {
	private static final long serialVersionUID = 2L;
 		
 	private DocTree doctree = new DocTree();
 	private transient DocTree rawDocs = new DocTree();
 	
 	public List<Doc> findDocs(DocSearchCriteria criteria) {
 		List<Doc> ret = new ArrayList<Doc>();
 		ret.addAll(rawDocs.find(criteria));
 		ret.addAll(doctree.find(criteria));
 		return ret;
 	}
 
 	public void addRawDocs(List<Doc> docs) {
 		rawDocs.addRoots(docs);
 	}
 	
 	public Collection<Doc> getRawDocs() {
 		return rawDocs.getRoots();
 	}
 	
 	public Map<String, Object> toFreemarkerModel() {
 		Map<String, Object> ret = new HashMap<String, Object>();
 		ret.put("doctree", doctree.getRoots());
 		return ret;
 	}
 
 	public DocTree getDocTree() {
 		return doctree;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Model [doctree=%s, rawDocs=%s]", doctree, rawDocs);
 	}
 	
 	public boolean isEmpty() {
 		return rawDocs.isEmpty() && doctree.isEmpty();
 	}
 
 }
