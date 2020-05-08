 package uk.ac.ox.oucs.humfrey;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.hp.hpl.jena.datatypes.BaseDatatype;
 import com.hp.hpl.jena.datatypes.RDFDatatype;
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 final public class Namespaces {
 	public static Namespace aiiso = new Namespace("aiiso", "http://purl.org/vocab/aiiso/schema#");
 	public static Namespace cc = new Namespace("cc", "http://web.resource.org/cc/");
 	public static Namespace cs = new Namespace("cs", "http://purl.org/vocab/changeset/schema#");
 	public static Namespace owl = new Namespace("owl", "http://www.w3.org/2002/07/owl#");
 	public static Namespace dc = new Namespace("dc", "http://purl.org/dc/elements/1.1/");
 	public static Namespace dcat = new Namespace("dcat", "http://vocab.deri.ie/dcat#");
	public static Namespace dct = new Namespace("dct", "http://purl.org/dc/terms/");
 	public static Namespace dctype = new Namespace("dctype", "http://purl.org/dc/dcmitype/");
 	public static Namespace event = new Namespace("event", "http://purl.org/NET/c4dm/event.owl#");
 	public static Namespace foaf = new Namespace("foaf", "http://xmlns.com/foaf/0.1/");
 	public static Namespace geo = new Namespace("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
 	public static Namespace sioc = new Namespace("sioc", "http://rdfs.org/sioc/ns#");
 	public static Namespace skos = new Namespace("skos", "http://www.w3.org/2004/02/skos/core#");
 	public static Namespace rdf = new Namespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
 	public static Namespace rdfs = new Namespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
 	public static Namespace v = new Namespace("v", "http://www.w3.org/2006/vcard/ns#");
 	public static Namespace vann = new Namespace("vann", "http://purl.org/vocab/vann/");
 	public static Namespace vs = new Namespace("vs", "http://www.w3.org/2003/06/sw-vocab-status/ns#");
 	public static Namespace xsd = new Namespace("xsd", "http://www.w3.org/2001/XMLSchema#");
 	
 	public static Map<String,String> getPrefixMapping() {
 		return Namespace.register;
 	}
 	public static String getURI(String prefix) {
 		return Namespace.register.get(prefix);
 	}
 	public static String getPrefix(String ns) {
 		return Namespace.uriRegister.get(ns);
 	}
 	
 	public static Property p(Model model, String s) {
 		String[] parts = s.split("[:_]");
 		return model.createProperty(Namespace.register.get(parts[0]), parts[1]);
 	}
 	public static Resource r(Model model, String s) {
 		String[] parts = s.split("[:_]");
 		return model.createResource(Namespace.register.get(parts[0]) + parts[1]);
 	}
 	public static String abbreviate(Resource resource) {
 		String namespace = resource.getNameSpace();
 		if (Namespace.uriRegister.containsKey(namespace))
 			return getPrefix(namespace) + ":" + resource.getLocalName();
 		else
 			return resource.getURI();
 	}
 	
 	public static class Namespace {
 		String prefix, ns;
 		static Map<String,String> register = new HashMap<String,String>();
 		static Map<String,String> uriRegister = new HashMap<String,String>();
 		static Model ontModel = ModelFactory.createOntologyModel();
 		
 		public Namespace(String prefix, String ns) {
 			this.prefix = prefix;
 			this.ns = ns;
 			register.put(prefix, ns);
 			uriRegister.put(ns, prefix);
 		}
 		public Node _(String local) {
 			return Node.createURI(ns + local);
 		}
 		public Property p(String local) {
 			return ontModel.createProperty(ns, local);
 		}
 		public Resource r(String local) {
 			return ontModel.createResource(ns+local);
 		}
 		public RDFDatatype d(String local) {
 			return new BaseDatatype(ns + local);
 		}
 		public String getPrefixString() {
 			return "PREFIX "+prefix+": <"+ns+">\n";
 		}
 	}
 }
