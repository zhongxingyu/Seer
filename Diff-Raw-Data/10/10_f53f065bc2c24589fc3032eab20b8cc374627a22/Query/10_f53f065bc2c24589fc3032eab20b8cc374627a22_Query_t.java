 package org.easysoa.registry.matching;
 
 import java.util.ArrayList;
 
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.platform.query.nxql.NXQLQueryBuilder;
 
 public class Query {
 
 	private ArrayList<Object> params = new ArrayList<Object>();
	private boolean firstCriteria = true;
 	private StringBuffer querySbuf = new StringBuffer();
 	
 	public Query(String queryString) {
 		querySbuf.append(queryString);
 	}
 
 	public void addCriteria(String criteria) {
 		if (firstCriteria) {
			firstCriteria = false;
 			querySbuf.append(" WHERE ");
 		} else {
 			querySbuf.append(" AND ");
 		}
 		querySbuf.append(criteria);
 	}
 	public void addCriteria(String xpath, String value) {
 		if (firstCriteria) {
			firstCriteria = false;
 			querySbuf.append(" WHERE ");
 		} else {
 			querySbuf.append(" AND ");
 		}
 		querySbuf.append("(");
 		querySbuf.append(xpath);
 		querySbuf.append(" IS NULL OR ");
 		querySbuf.append(xpath);
 		querySbuf.append("=?");
 		params.add(value);
 		querySbuf.append(")");
 	}
 	public void addOptionalCriteria(String xpath, String value) {
     	if (value != null && !value.isEmpty()) {
     		addCriteria(xpath, value);
     	}
 	}
 	public String build() throws ClientException {
 		return NXQLQueryBuilder.getQuery(querySbuf.toString(), params.toArray(), true, true);
 	}
 	public String toString() {
 		return querySbuf.toString() + " " + params; // TODO  better
 	}
 }
