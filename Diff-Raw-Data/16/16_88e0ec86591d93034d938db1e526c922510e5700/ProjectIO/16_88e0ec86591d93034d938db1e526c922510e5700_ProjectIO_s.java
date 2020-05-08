 package net.toxbank.client.io.rdf;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import net.toxbank.client.resource.Project;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 public class ProjectIO implements IOClass<Project> {
 
 	public Model toJena(Model toAddTo, Project... projects) {
 		if (toAddTo == null) toAddTo = ModelFactory.createDefaultModel();
 		if (projects == null) return toAddTo;
 
 		for (Project project : projects) {
 			if (project.getResourceURL() == null) {
				throw new IllegalArgumentException("All protocols must have resource URIs.");
 			}
 			Resource res = toAddTo.createResource(project.getResourceURL().toString());
 			toAddTo.add(res, RDF.type, TOXBANK.PROJECT);
 		}
 		return toAddTo;
 	}
 
 	public List<Project> fromJena(Model source) {
 		if (source == null) return Collections.emptyList();
 
 		ResIterator iter = source.listResourcesWithProperty(RDF.type, TOXBANK.PROJECT);
 		if (!iter.hasNext()) return Collections.emptyList();
 
 		List<Project> projects = new ArrayList<Project>();
 		while (iter.hasNext()) {
 			Project project = new Project();
 			Resource res = iter.next();
 			System.out.println(res);
 			try {
 				project.setResourceURL(
 					new URL(res.getURI())
 				);
 			} catch (MalformedURLException e) {
 				throw new IllegalArgumentException(
 					"Found resource with an invalid URI:" + res.getURI()
 				);
 			}
 			projects.add(project);
 		}
 
 		return projects;
 	}
 
 }
