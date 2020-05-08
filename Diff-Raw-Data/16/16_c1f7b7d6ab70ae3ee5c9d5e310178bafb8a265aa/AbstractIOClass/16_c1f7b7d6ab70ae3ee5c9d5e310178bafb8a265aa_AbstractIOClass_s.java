 package net.toxbank.client.io.rdf;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import net.toxbank.client.resource.IToxBankResource;
 
 import com.hp.hpl.jena.rdf.model.*;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public abstract class AbstractIOClass<T extends IToxBankResource> implements IOClass<T> {
   
 	public Model toJena(Model toAddTo, T... resources) throws IllegalArgumentException {
 
 		if (toAddTo == null) toAddTo = ModelFactory.createDefaultModel();
 		if (resources == null) return toAddTo;
 
 		for (T resource : resources) {
 			if (resource.getResourceURL() == null) {
 				throw new IllegalArgumentException(String.format(msg_ResourceWithoutURI, resource.getClass().getName()));
 			}
 			objectToJena(toAddTo,resource);
 		}
 		return toAddTo;
 	}	
 	public abstract Resource objectToJena(Model toAddTo, T object) throws IllegalArgumentException;
 	/**
 	 * To handle {@link listResourcesWithProperty}
 	 * @param source
 	 * @param iter
 	 * @return
 	 */
 	protected List<T> fromJena(Model source, ResIterator iter) {
 		if (source == null) return Collections.emptyList();
 
 		if (!iter.hasNext()) return Collections.emptyList();
 
 		List<T> items = new ArrayList<T>();
 		while (iter.hasNext()) {
 			T item = fromJena(source,iter.next());
 			items.add(item);
 		}
 
 		return items;
 	}
 	
 	/**
 	 * To handle {@link listObjectsOfProperty}
 	 * @param source
 	 * @param iter
 	 * @return
 	 */
 	protected List<T> fromJena(Model source, NodeIterator iter) {
 		if (source == null) return Collections.emptyList();
 
 		if (!iter.hasNext()) return Collections.emptyList();
 
 		List<T> items = new ArrayList<T>();
 		while (iter.hasNext()) {
 			RDFNode node = iter.next();
 			if (node.isResource())  {
 				T item = fromJena(source,(Resource) node);
 				items.add(item);
 			}
 		}
 
 		return items;
 	}	
 	/**
 	 * to avoid "cannot encode (char)  in context XML" exception when writing to rdf/xml
 	 * @return
 	 */
 	protected String cleanLiteral(String m) {
 		for (int i=1; i < 7;i ++) {
 			if (m.indexOf((char)i)>=0) m=m.replace(((char)i),' ');
 		}
 		return m;
 	}
 
 	protected boolean hasType(Resource res, Resource type) {
 	  try {
 	    for (StmtIterator iter = res.listProperties(RDF.type); iter.hasNext(); ) {
 	      Statement stmt = iter.next();
 	      if (stmt.getResource().getURI().equals(type.getURI())) {
 	        return true;
 	      }
 	    }
 	  }
 	  catch (Exception e) {}
 	  return false;
 	}
 	
 	protected void addString(Resource res, Property prop, String value) {
 	  if (value != null) {
 	    res.addLiteral(prop, value);
 	  }
 	}
 	
 	protected void addTimestamp(Resource res, Property prop, Long timestamp) {
 	  addString(res, prop, timestampToDateString(timestamp));
 	}
 	
 	protected Boolean getBoolean(Resource res, Property prop) {
 	  try {
 	    return res.getProperty(prop).getBoolean();
 	  }
 	  catch (Exception e) {
 	    return null;
 	  }
 	}
 	
 	protected String getString(Resource res, Property prop) {
 	  try {
 	    return res.getProperty(prop).getString();
 	  }
 	  catch (Exception e) {
 	    return null;
 	  }
 	}
 	
 	protected String getUri(Resource res, Property prop) {
 	  try {
 	    return res.getProperty(prop).getResource().getURI();
 	  }
 	  catch (Exception e) {
 	    return null;
 	  }
 	}
 	
 	protected Long getTimestamp(Resource res, Property prop) {
 	  try {
	    String dateString = res.getProperty(prop).getString();
	    return dateStringToTimestamp(dateString);
 	  }
 	  catch (Exception e) {
	    return null;
 	  }
 	}
 	
  protected DateFormat dateStringFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss z");
   protected String timestampToDateString(Long timestamp) {
     if (timestamp == null || timestamp == 0) {
       return null;
     }
     Date date = new Date(timestamp);
     return dateStringFormat.format(date);    
   }
   protected Long dateStringToTimestamp(String dateString) throws Exception {
     if (dateString == null) {
       return null;
     }
     Date date = dateStringFormat.parse(dateString);
     return date.getTime();
   }
 }
