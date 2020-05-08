 package uk.ac.ox.oucs.humfrey.serializers;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.VelocityContext;
 
 import uk.ac.ox.oucs.humfrey.Query;
 import uk.ac.ox.oucs.humfrey.Templater;
 import uk.ac.ox.oucs.humfrey.namespaces.DCAT;
 import uk.ac.ox.oucs.humfrey.resources.VelocityResource;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 class HTMLSerializer extends AbstractSerializer {
 	
 	Templater templater;
 	Serializer serializer;
 	String homeURIRegex;
 	String[][] groups = {
 			{"Naming", "rdfs_label", "skos_prefLabel", "skos_altLabel", "dc_title"},
 			{"Overview", "dct_publisher", "dct_issued", "dct_license", "foaf_homepage"},
 			{"Location", "v_adr", "geo_lat", "geo_long"},
 			{"Contact", "v_tel", "foaf_mbox", "foaf_phone"},
 	};
 	
 	public HTMLSerializer(Templater templater, Serializer serializer, String homeURIRegex) {
 		this.templater = templater;
 		this.serializer = serializer;
 		this.homeURIRegex = homeURIRegex;
 	}
 
 	@Override
 	public String getContentType() {
 		return "text/html";
 	}
 	
 	@Override
 	public String getName() {
 		return "HTML";
 	}
 
 	@Override
 	public void serializeResource(Resource resource, Query query,
 			HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		VelocityContext context = new VelocityContext();
 		
 		context.put("resource", VelocityResource.create(resource, homeURIRegex));
 		context.put("query", query);
 		context.put("serializers", serializer.getSerializers(SerializationType.ST_RESOURCE));
 		
 		resp.setContentType(getContentType());
 		
 		if (resource.hasProperty(RDF.type, DCAT.Dataset))
 			templater.render(resp.getWriter(), "dataset.vm", context);
 		else
 			templater.render(resp.getWriter(), "doc.vm", context);
 	}
 	
 	@Override
 	public void serializeResultSet(ResultSet resultset, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException{
 		List<List<Object>> results = new LinkedList<List<Object>>();
 		List<String> bindings = resultset.getResultVars();
 		
 		while (resultset.hasNext()) {
 			QuerySolution soln = resultset.next();
 			List<Object> result = new LinkedList<Object>();
 			for (String binding : bindings) {
 				RDFNode node = soln.get(binding);
 				if (node == null)
 					result.add(null);
 				else if (node.isResource())
 					result.add(VelocityResource.create((Resource) node, homeURIRegex));
 				else
 					result.add(((Literal) node).getValue());
 			}
 			results.add(result);
 		}
 		
 		VelocityContext context = new VelocityContext();
 		context.put("results", results);
 		context.put("bindings", bindings);
 		context.put("query", req.getParameter("query"));
 		context.put("serializers", serializer.getSerializers(SerializationType.ST_RESULTSET));
 		resp.setContentType(getContentType());
 		templater.render(resp, "sparql.vm", context);
 	}
 	
 	@Override
 	public void serializeResourceList(List<Resource> resources, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		List<VelocityResource> velocityResources = new LinkedList<VelocityResource>();
 		
 		for (Resource resource : resources)
 			velocityResources.add(VelocityResource.create(resource, homeURIRegex));
 		
 		VelocityContext context = new VelocityContext();
 		resp.setContentType(getContentType());
 		context.put("resources", velocityResources);
 		context.put("query", req.getParameter("query"));
 		context.put("serializers", serializer.getSerializers(SerializationType.ST_RESOURCELIST));
 		templater.render(resp, "sparql.vm", context);
 	}
 
 	@Override
 	public void serializeSparqlError(String message, Query query,
 			HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		resp.setContentType(getContentType());
 		
 		VelocityContext context = new VelocityContext();
 		resp.setContentType(getContentType());
 		context.put("error", message);
 		context.put("query", req.getParameter("query"));
 		context.put("serializers", serializer.getSerializers(SerializationType.ST_EXCEPTION));
 		templater.render(resp, "sparql.vm", context);
 
 	}
 	
 	@Override
 	public void serializeBoolean(boolean value, Query query,
 			HttpServletRequest req, HttpServletResponse resp)
 	throws IOException {
 		VelocityContext context = new VelocityContext();
 		resp.setContentType(getContentType());
 		context.put("hasBooleanResult", true);
 		context.put("booleanResult", value);
 		context.put("query", req.getParameter("query"));
		context.put("serializers", serializer.getSerializers(SerializationType.ST_EXCEPTION));
 		templater.render(resp, "sparql.vm", context);
 	}
 	
 	@Override
 	public boolean canSerialize(SerializationType serializationType) {
 		switch (serializationType) {
 		case ST_BOOLEAN:
 		case ST_RESOURCE:
 		case ST_RESULTSET:
 		case ST_EXCEPTION:
 		case ST_RESOURCELIST:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
