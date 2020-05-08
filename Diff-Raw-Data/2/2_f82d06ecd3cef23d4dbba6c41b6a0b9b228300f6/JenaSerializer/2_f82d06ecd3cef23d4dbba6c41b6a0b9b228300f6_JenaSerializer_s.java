 package uk.ac.ox.oucs.humfrey.serializers;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import uk.ac.ox.oucs.humfrey.Namespaces;
 import uk.ac.ox.oucs.humfrey.Query;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 public abstract class JenaSerializer extends AbstractSerializer {
 	public abstract String getSerialization();
 
 	protected Serializer serializer;
 
 	public JenaSerializer(Serializer serializer, String homeURIRegex) {
 		this.serializer = serializer;
 	}
 
 	@Override
 	public void serializeResource(Resource resource, Query query, HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		Model model = buildModelForResource(resource);
 		addFormatInformation(model, query, serializer);
		serializeModel(buildModelForResource(resource), query, req, resp);
 	}
 	
 	@Override
 	public void serializeResourceList(List<Resource> resources, Query query, HttpServletRequest req, HttpServletResponse resp) throws IOException {
 		Model model = ModelFactory.createDefaultModel();
 		for (Resource resource : resources)
 			buildModelForResource(resource, model);
 		serializeModel(model, query, req, resp);
 	}
 	
 	@Override
 	public void serializeModel(Model model, Query query, HttpServletRequest req,
 			HttpServletResponse resp) throws IOException {
 		resp.setContentType(getContentType());
 		Writer writer = resp.getWriter();
 		model.setNsPrefixes(Namespaces.getPrefixMapping());
 		model.write(writer, getSerialization());
 	}
 
 	@Override
 	public boolean canSerialize(SerializationType serializationType) {
 		switch (serializationType) {
 		case ST_MODEL:
 		case ST_RESOURCE:
 			return true;
 		default:
 			return false;
 		}
 	}
 }
