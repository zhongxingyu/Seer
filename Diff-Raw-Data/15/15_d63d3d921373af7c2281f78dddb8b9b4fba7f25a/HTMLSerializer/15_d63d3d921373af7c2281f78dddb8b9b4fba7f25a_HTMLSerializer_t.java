 package uk.ac.ox.oucs.humfrey.serializers;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
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
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 class HTMLSerializer extends AbstractSerializer {
 	
 	Templater templater;
 	Map<String,AbstractSerializer> serializers;
 	String homeURIRegex;
 	String[][] groups = {
 			{"Naming", "rdfs_label", "skos_prefLabel", "skos_altLabel", "dc_title"},
 			{"Overview", "dct_publisher", "dct_issued", "dct_license", "foaf_homepage"},
 			{"Location", "v_adr", "geo_lat", "geo_long"},
 			{"Contact", "v_tel", "foaf_mbox", "foaf_phone"},
 	};
 	
 	public HTMLSerializer(Templater templater, Map<String,AbstractSerializer> serializers, String homeURIRegex) {
 		this.templater = templater;
 		this.serializers = serializers;
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
 	public void serializeModel(Model model, Model fullModel, Query query,
 			HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		VelocityContext context = new VelocityContext();
 		Resource resource = fullModel.getResource(query.getURI());
 		
 		context.put("resource", VelocityResource.create(resource, homeURIRegex, fullModel));
 		context.put("query", query);
 		context.put("serializers", serializers);
 		context.put("model", fullModel);
 		
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
 		resp.setContentType(getContentType());
 		templater.render(resp, "sparql.vm", context);
 	}
 	
 	@Override
 	public void serializeResources(Model model, Model fullModel, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
 		List<VelocityResource> resources = new LinkedList<VelocityResource>();
 		
 		ResIterator subjects = model.listSubjects();
 		while (subjects.hasNext())
 			resources.add(VelocityResource.create(subjects.next(), homeURIRegex));
 		
 		VelocityContext context = new VelocityContext();
 		resp.setContentType(getContentType());
 		context.put("model", model);
 		context.put("resources", resources);
 		context.put("query", req.getParameter("query"));
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
		templater.render(resp, "sparql.vm", context);

	}

 }
