 package net.toxbank.client.io.rdf;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.toxbank.client.resource.Organisation;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class OrganisationIO implements IOClass<Organisation> {
 
 	public Model toJena(Model toAddTo, Organisation... resources) {
 		if (toAddTo == null) toAddTo = ModelFactory.createDefaultModel();
 		if (resources == null) return toAddTo;
 
 		for (Organisation org : resources) {
 			if (org.getResourceURL() == null) {
				throw new IllegalArgumentException("All organisations must have resource URIs.");
 			}
 			Resource res = toAddTo.createResource(org.getResourceURL().toString());
 			toAddTo.add(res, RDF.type, TOXBANK.ORGANIZATION);
 		}
 		return toAddTo;
 	}
 
 	public List<Organisation> fromJena(Model source) {
 		if (source == null) return Collections.emptyList();
 
 		ResIterator iter = source.listResourcesWithProperty(RDF.type, TOXBANK.ORGANIZATION);
 		if (!iter.hasNext()) return Collections.emptyList();
 
 		List<Organisation> organisations = new ArrayList<Organisation>();
 		while (iter.hasNext()) {
 			Organisation org = new Organisation();
 			Resource res = iter.next();
 			System.out.println(res);
 			try {
 				org.setResourceURL(
 					new URL(res.getURI())
 				);
 			} catch (MalformedURLException e) {
 				throw new IllegalArgumentException(
 					"Found resource with an invalid URI:" + res.getURI()
 				);
 			}
 			organisations.add(org);
 		}
 
 		return organisations;
 	}
 
 }
