 package uk.ac.ox.oucs.humfrey.serializers;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 import uk.ac.ox.oucs.humfrey.Query;
 import uk.ac.ox.oucs.humfrey.Templater;
 
 /**
  * An interface to all available serializers.
  * 
  * @author Alexander Dutton <alexander.dutton@oucs.ox.ac.uk>
  */
 public class Serializer {
 	private Map<String,AbstractSerializer> serializers = new HashMap<String,AbstractSerializer>();
 	Model fullModel;
 	
 	public Serializer(Model fullModel, Templater templater, String homeURIRegex) {
 		this.fullModel = fullModel;
 		
 		serializers.put("rdf", new RDFXMLSerializer(this, homeURIRegex));
 		serializers.put("n3", new Notation3Serializer(this, homeURIRegex));
 		serializers.put("nt", new NTripleSerializer(this, homeURIRegex));
 		serializers.put("ttl", new TurtleSerializer(this, homeURIRegex));
 		serializers.put("js", new JSONSerializer(true));
 		serializers.put("json", new JSONSerializer(false));
 		serializers.put("srx", new SparqlXMLSerializer());
 		serializers.put("html", new HTMLSerializer(templater, this, homeURIRegex));
 	}
 	
 	public AbstractSerializer get(String format) {
 		return serializers.get(format);
 	}
 	
 	public boolean hasFormat(String format) {
 		return serializers.containsKey(format);
 	}
 	
 	public Collection<String> getFormats() {
 		return serializers.keySet();
 	}
 	
 	public Map<String,AbstractSerializer> getSerializers() {
 		return serializers;
 	}
 	
 	public void serializeModel(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeModel(model, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeResultSet(resultset, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeSparqlError(String message, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeSparqlError(message, query, req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeResource(Resource resource, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeResource(resource, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeResourceList(List<Resource> resources, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeResourceList(resources, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeResourceList(Model model, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeResourceList(model, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public void serializeBoolean(boolean value, Query query, HttpServletRequest req, HttpServletResponse resp) {
 		AbstractSerializer serializer = serializers.get(query.getAccept());
 		try {
 			serializer.serializeBoolean(value, query, req, resp);
 			resp.setStatus(HttpServletResponse.SC_OK);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (NotImplementedException e) {
 			resp.setStatus(query.negotiatedAccept() ? HttpServletResponse.SC_NOT_ACCEPTABLE : HttpServletResponse.SC_NOT_FOUND);
 		}
 	}
 	
 	public static Map<Property,Set<RDFNode>> getPropertyMap(Resource resource) {
 		StmtIterator stmts = resource.listProperties();
 		Map<Property,Set<RDFNode>> properties = new HashMap<Property,Set<RDFNode>>();
 		while (stmts.hasNext()) {
 			Statement stmt = stmts.next();
 			if (!properties.containsKey(stmt.getPredicate()))
 				properties.put(stmt.getPredicate(), new HashSet<RDFNode>());
 			properties.get(stmt.getPredicate()).add(stmt.getObject());
 		}
 		return properties;
 	}
 	
 	public Map<String,AbstractSerializer> getSerializers(AbstractSerializer.SerializationType serializationType) {
 		Map<String,AbstractSerializer> matches = new HashMap<String,AbstractSerializer>();
 		for (String format : serializers.keySet()) {
 			AbstractSerializer serializer = serializers.get(format);
 			if (serializer.canSerialize(serializationType))
 				matches.put(format, serializer);
 		}
 		return matches;
 	}
 	
 	
 }
