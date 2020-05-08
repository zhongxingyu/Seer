 /* Copyright (c) 2012  Egon Willighagen <egon.willighagen@gmail.com>
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contact: http://www.bioclipse.net/
  */
 package net.bioclipse.icebear.business;
 
 import java.io.ByteArrayInputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.SocketTimeoutException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.bioclipse.business.BioclipsePlatformManager;
 import net.bioclipse.cdk.business.CDKManager;
 import net.bioclipse.cdk.domain.ICDKMolecule;
 import net.bioclipse.core.business.BioclipseException;
 import net.bioclipse.core.domain.IMolecule;
 import net.bioclipse.core.domain.IMolecule.Property;
 import net.bioclipse.core.domain.StringMatrix;
 import net.bioclipse.icebear.extractors.INextURIExtractor;
 import net.bioclipse.icebear.extractors.IPropertyExtractor;
 import net.bioclipse.icebear.extractors.links.DBPediaMinter;
 import net.bioclipse.icebear.extractors.links.OwlEquivalentClassExtractor;
 import net.bioclipse.icebear.extractors.links.OwlSameAsExtractor;
 import net.bioclipse.icebear.extractors.links.SkosExactMatchExtractor;
 import net.bioclipse.icebear.extractors.properties.ChemAxiomExtractor;
 import net.bioclipse.icebear.extractors.properties.ChemInfExtractor;
 import net.bioclipse.icebear.extractors.properties.DBPediaExtractor;
 import net.bioclipse.icebear.extractors.properties.DublinCoreExtractor;
 import net.bioclipse.icebear.extractors.properties.FoafExtractor;
 import net.bioclipse.icebear.extractors.properties.FreebaseExtractor;
 import net.bioclipse.icebear.extractors.properties.OpenMoleculesExtractor;
 import net.bioclipse.icebear.extractors.properties.PubChemRDFExtractor;
 import net.bioclipse.icebear.extractors.properties.RdfsExtractor;
 import net.bioclipse.icebear.extractors.properties.SioExtractor;
 import net.bioclipse.icebear.extractors.properties.SkosExtractor;
 import net.bioclipse.jobs.IReturner;
 import net.bioclipse.managers.business.IBioclipseManager;
 import net.bioclipse.rdf.business.IRDFStore;
 import net.bioclipse.rdf.business.RDFManager;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 
 import com.hp.hpl.jena.sparql.vocabulary.FOAF;
 import com.hp.hpl.jena.vocabulary.DC;
 import com.hp.hpl.jena.vocabulary.DC_10;
 import com.hp.hpl.jena.vocabulary.DC_11;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 public class IcebearManager implements IBioclipseManager {
 
 	private static final String ICON = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAFZJREFUeF59z4EJADEIQ1F36k7u5E7ZKXeUQPACJ3wK7UNokVxVk9kHnQH7bY9hbDyDhNXgjpRLqFlo4M2GgfyJHhjq8V4agfrgPQX3JtJQGbofmCHgA/nAKks+JAjFAAAAAElFTkSuQmCC";
 	
 	private static final Logger logger = Logger.getLogger(IcebearManager.class);
 
 	private CDKManager cdk = new CDKManager();
 	private RDFManager rdf = new RDFManager();
 	private BioclipsePlatformManager bioclipse = new BioclipsePlatformManager();
 
 	private Map<String,String> resourceMap = new HashMap<String, String>() {
 		private static final long serialVersionUID = -7354694153097755405L;
 	{
 		// prepopulate it with things I already know about so that we do not have to look that up
 		put("http://semanticscience.org/resource/CHEMINF_000000", "chemical entity");
 		put("http://bio2rdf.org/ns/chebi#Compound", "compound");
 		put("http://bio2rdf.org/chebi_resource:Compound", "compound");
 		put("http://www.polymerinformatics.com/ChemAxiom/ChemDomain.owl#NamedChemicalSpecies", "named chemical species");
 		put("http://umbel.org/umbel/rc/DrugProduct", "drug product");
 		put("http://umbel.org/umbel/rc/Drug", "drug");
 		put("http://dbpedia.org/ontology/Drug", "drug");
 		put("http://rdf.freebase.com/ns/medicine.medical_treatment", "medical treatment");
 		put("http://rdf.freebase.com/ns/medicine.risk_factor", "risk factor");
 		put("http://rdf.freebase.com/ns/chemistry.chemical_compound", "chemical compound");
 		put("http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/drugs", "drug");
 		put("http://www4.wiwiss.fu-berlin.de/sider/resource/sider/drugs", "drug");
 		put("http://bio2rdf.org/kegg_resource:Drug", "drug");
 		put("http://bio2rdf.org/drugbank_ontology:drugs", "drug");
 		put("http://xmlns.com/foaf/0.1/Document", "document");
 		put("http://bio2rdf.org/drugbank_drugtype:approved", "approved drug");
 		put("http://bio2rdf.org/drugbank_drugtype:smallMolecule", "small molecule");
 		put("http://purl.obolibrary.org/obo/CHEBI_23367", "molecular entity");
 		put("http://www.opentox.org/api/1.1#Compound", "compound");
 
 		// and also ignore often used things we like to ignore
 		ignore("http://bio2rdf.org/obo_resource:term");
 		ignore("http://bio2rdf.org/obo_resource:Term-chebi");
 		ignore("http://www.w3.org/2002/07/owl#Thing");
 		ignore("http://www.w3.org/2002/07/owl#Class");
 		ignore("http://www.w3.org/2002/07/owl#NamedIndividual");
 		ignore("http://umbel.org/umbel/rc/ChemicalCompoundTypeByChemicalSpecies");
 		ignore("http://umbel.org/umbel#RefConcept");
 		ignore("http://www4.wiwiss.fu-berlin.de/drugbank/vocab/resource/class/Offer");
 		ignore("http://www.w3.org/2000/01/rdf-schema#Class");
 		ignore("http://www.w3.org/2000/01/rdf-schema#Resource");
 		ignore("http://www.opentox.org/api/1.1#Dataset");
 	}
 		public void ignore(String resource) {
 			put(resource, null);
 		}
 	};
 
 	private List<IPropertyExtractor> extractors = new ArrayList<IPropertyExtractor>() {
 		private static final long serialVersionUID = 2825983879781792266L; {
 		add(new RdfsExtractor());
 		add(new DublinCoreExtractor());
 		add(new FoafExtractor());
 		add(new SkosExtractor());
 		add(new ChemInfExtractor());
 		add(new ChemAxiomExtractor());
 		add(new SioExtractor());
 		add(new OpenMoleculesExtractor());
 		add(new DBPediaExtractor());
 		add(new FreebaseExtractor());
 		add(new PubChemRDFExtractor());
 	}};
 	private List<INextURIExtractor> spiders = new ArrayList<INextURIExtractor>() {
 		private static final long serialVersionUID = 7089854109617759948L; {
 		add(new OwlSameAsExtractor());
 		add(new OwlEquivalentClassExtractor());
 		add(new SkosExactMatchExtractor());
 		add(new DBPediaMinter());
 //		add(new OpenMoleculesMinter());
 	}};
 	
 	Map<String,String> extraHeaders = new HashMap<String, String>() {
 		private static final long serialVersionUID = 2825983879781792266L;
 	{
 	  put("Content-Type", "application/rdf+xml");
 	  put("Accept", "application/rdf+xml"); // Both Accept and Content-Type are needed for PubChem 
 	}};
 
     public String getManagerName() {
         return "isbjørn";
     }
 
     public void findInfo(IMolecule mol, IReturner<IRDFStore> returner, IProgressMonitor monitor)
     throws BioclipseException {
     	monitor.beginTask("Downloading RDF resources", 100);
     	ICDKMolecule cdkMol = cdk.asCDKMolecule(mol);
 		String inchi = cdkMol.getInChI(Property.USE_CACHED_OR_CALCULATED);
 		IcebearWorkload workload = new IcebearWorkload();
 		workload.addNewURI("http://rdf.openmolecules.net/?" + inchi);
 		inchi = inchi.replace("=1S/", "=1/");
 		workload.addNewURI("http://rdf.openmolecules.net/?" + inchi);
 		while (workload.hasMoreWork() && !monitor.isCanceled()) {
 			findInfoForOneURI(workload, returner, monitor);
 	    	monitor.worked(1);
 		}
     }
     
     public void findInfo(String uri, IReturner<IRDFStore> returner, IProgressMonitor monitor)
     throws BioclipseException {
     	monitor.beginTask("Downloading RDF resources", 100);
 		IcebearWorkload workload = new IcebearWorkload();
 		workload.addNewURI(uri);
 		while (workload.hasMoreWork() && !monitor.isCanceled()) {
 			findInfoForOneURI(workload, returner, monitor);
 	    	monitor.worked(1);
 		}
     }
     
     public List<Entry> getProperties(IRDFStore store) throws BioclipseException, CoreException {
     	String resource = rdf.getForPredicate(store,
     		"http://www.bioclipse.org/PrimaryObject",
 			"http://www.bioclipse.org/hasURI").get(0);
     	
 		List<Entry> props = new ArrayList<Entry>();
 		for (IPropertyExtractor extractor : extractors) {
 			props.addAll(extractor.extractProperties(store, resource));
 		}
 		return props;
     }
 
     private void findInfoForOneURI(IcebearWorkload workload, IReturner<IRDFStore> returner, IProgressMonitor monitor) {
     	IRDFStore store = rdf.createInMemoryStore();
     	URI nextURI = workload.getNextURI();
 		String nextURIString = nextURI.toString();
 		monitor.subTask("Downloading " + nextURIString);
     	try {
 			rdf.addObjectProperty(store,
 				"http://www.bioclipse.org/PrimaryObject", "http://www.bioclipse.org/hasURI",
 				nextURI.toString()
 			);
 			rdf.importURL(store, nextURIString, extraHeaders, monitor);
 			System.out.println(rdf.asTurtle(store));
 			for (INextURIExtractor spider : spiders) {
 				for (String uri : spider.extractURIs(store, nextURI.toString())) {
 					workload.addNewURI(uri);
 				}
 			}
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
     	returner.partialReturn(store);
     }
     
     public IFile findInfo( IMolecule mol, IFile target, IProgressMonitor monitor) throws BioclipseException, CoreException {
     	if (!bioclipse.isOnline())
     		throw new BioclipseException("Searching information on the web requires an active internet connection.");
 
     	if (monitor == null) monitor = new NullProgressMonitor();
     	monitor.beginTask("Downloading RDF resources", 100);
     	monitor.worked(1);
 
     	StringWriter writer = new StringWriter();
     	PrintWriter pWriter = new PrintWriter(writer);
     	
     	pWriter.println("<html>");
     	pWriter.println("  <head>");
     	pWriter.println("  <title>Isbjørn Report</title>");
     	pWriter.println("  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">");
     	pWriter.println("  <style type=\"text/css\">");
     	pWriter.println("    body {");
     	pWriter.println("      font-family: Arial, Verdana, Sans-serif;");
     	pWriter.println("      a:link {color:black;} ");
     	pWriter.println("      a:hover {color:black; text-decoration:underline;} ");
     	pWriter.println("      a:visited {color:black;} ");
     	pWriter.println("    }");
     	pWriter.println("  </style>");
     	pWriter.println("  </head>");
     	pWriter.println("<body>");
     	pWriter.println("<h1>Isbjørn Report</h1>");
     	// now, the next should of course use an extension point, but this will have to do for now...
     	useIcebearPowers(mol, pWriter, null, monitor);
     	pWriter.println("<html>");
     	pWriter.println("</body>");
     	pWriter.println("</html>");
     	pWriter.flush();
 
     	try {
     		if (target.exists()) {
     			target.setContents(
                     new ByteArrayInputStream(writer.toString()
                         .getBytes("UTF-8")),
                         false,
                         true, // overwrite
                         monitor
                 );
             } else {
             	target.create(
             		new ByteArrayInputStream(writer.toString()
             			.getBytes("UTF-8")),
            			false,
  					monitor
             	);
             }
     	} catch (Exception encodingExeption) {
     		throw new BioclipseException("Error encoding problem: " + encodingExeption.getMessage(), encodingExeption);
     	}
     	
     	return target;
     }
 
     public IFile findInfo( String uri, IFile target, IProgressMonitor monitor) throws BioclipseException, CoreException {
     	if (!bioclipse.isOnline())
     		throw new BioclipseException("Searching information on the web requires an active internet connection.");
 
     	if (monitor == null) monitor = new NullProgressMonitor();
     	monitor.beginTask("Downloading RDF resources", 100);
     	monitor.worked(1);
 
     	StringWriter writer = new StringWriter();
     	PrintWriter pWriter = new PrintWriter(writer);
     	
     	pWriter.println("<html>");
     	pWriter.println("  <head>");
     	pWriter.println("  <title>Isbjørn Report</title>");
     	pWriter.println("  <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\">");
     	pWriter.println("  <style type=\"text/css\">");
     	pWriter.println("    body {");
     	pWriter.println("      font-family: Arial, Verdana, Sans-serif;");
     	pWriter.println("      a:link {color:black;} ");
     	pWriter.println("      a:hover {color:black; text-decoration:underline;} ");
     	pWriter.println("      a:visited {color:black;} ");
     	pWriter.println("    }");
     	pWriter.println("  </style>");
     	pWriter.println("  </head>");
     	pWriter.println("<body>");
     	pWriter.println("<h1>Isbjørn Report</h1>");
     	// now, the next should of course use an extension point, but this will have to do for now...
     	useIcebearPowers(uri, pWriter, null, monitor);
     	pWriter.println("<html>");
     	pWriter.println("</body>");
     	pWriter.println("</html>");
     	pWriter.flush();
 
     	try {
     		if (target.exists()) {
     			target.setContents(
                     new ByteArrayInputStream(writer.toString()
                         .getBytes("UTF-8")),
                         false,
                         true, // overwrite
                         monitor
                 );
             } else {
             	target.create(
             		new ByteArrayInputStream(writer.toString()
             			.getBytes("UTF-8")),
            			false,
  					monitor
             	);
             }
     	} catch (Exception encodingExeption) {
     		throw new BioclipseException("Error encoding problem: " + encodingExeption.getMessage(), encodingExeption);
     	}
     	
     	return target;
     }
 
     private void useIcebearPowers(IMolecule mol, PrintWriter pWriter, List<String> alreadyDone, IProgressMonitor monitor)
 	throws BioclipseException, CoreException {
 		if (alreadyDone == null) alreadyDone = new ArrayList<String>();
 		alreadyDone.add("http://bio2rdf.org/chebi:15377"); // blacklist water
 		// so, what are the isbjørn powers then?
 		// 1. use the InChI to get URIs
 		ICDKMolecule cdkMol = cdk.asCDKMolecule(mol);
 		String inchi = cdkMol.getInChI(Property.USE_CACHED_OR_CALCULATED);
 		String uri = "http://rdf.openmolecules.net/?" + inchi;
 		useIcebearPowers(uri, pWriter, alreadyDone, monitor);
 		// 2. also do the non-standard InChI
 		inchi = inchi.replace("=1S/", "=1/");
 		uri = "http://rdf.openmolecules.net/?" + inchi;
 		useIcebearPowers(uri, pWriter, alreadyDone, monitor);
 	}
 
 	private void useIcebearPowers(String uriString, PrintWriter pWriter, List<String> alreadyDone, IProgressMonitor monitor)
 	throws BioclipseException, CoreException {
 		if (alreadyDone == null) alreadyDone = new ArrayList<String>();
 		try {
 			URI ronURI = new URI(uriString);
 			useUniveRsalIcebearPowers(pWriter, ronURI, alreadyDone, monitor);
 		} catch (URISyntaxException exception) {
 			throw new BioclipseException("Something wrong with the URI: " + exception.getMessage(), exception);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void useUniveRsalIcebearPowers(PrintWriter pWriter, URI uri, List<String> alreadyDone, IProgressMonitor monitor) {
 		if (uri.toString().startsWith("http://data.linkedct.org") ||
 		    uri.toString().startsWith("http://bio2rdf.org/linkedct_intervention") || // ignore LinkedCT which is broken
 		    uri.toString().startsWith("http://bio2rdf.org/drugbank_drugs:") // no longer used by Bio2RDF
 		) {
 			return;
 		}
 		
 		alreadyDone.add(uri.toString());
 		monitor.setTaskName("Downloading " + uri.toString());
 		System.out.println("Downloading " + uri.toString());
 		IRDFStore store = rdf.createInMemoryStore();
 		if (uri.getHost() == null) return; // ignore
 		pWriter.println(
 			"<h2>" + uri.getHost() + " <a href=\""+ uri.toString() + "\">" +
 			"<img border=0 src=\"" + ICON + "\" /></a></h2>");
 		pWriter.println("<ul>");
 		try {
 			rdf.importURL(store, uri.toString(), extraHeaders, monitor);
 			String uriString = uri.toString();
 			if (uriString.startsWith("http://rdf.freebase.com/ns/m/")) {
 				uri = new URI(uri.toString().replace("http://rdf.freebase.com/ns/m/", "http://rdf.freebase.com/ns/m."));
 			}
 			System.out.println(rdf.asRDFN3(store)); // so that I can check what is there...
 			printFoundInformation(pWriter, store, uri);
 			if (uriString.startsWith("http://rdf.freebase.com/ns/")) {
 				// OK, don't recurse from here... that explodes, sad enough
 				return;
 			}
 			// and recurse: owl:sameAs
 			List<String> sameResources = rdf.allOwlSameAs(store, uri.toString());
 			for (String sameResource : sameResources) {
 				// OK, work around a bug in rdf.openmolecules.net
 				if (sameResource.startsWith("http://www.chemspider.com/Chemical-Structure.") &&
 					sameResource.endsWith(".rdf")) {
 					sameResource = sameResource + "#Compound";
 				}
 				if (!alreadyDone.contains(sameResource)) {
 					try {
 						URI sameURI = new URI(sameResource);
 						useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);
 						if (false) throw new SocketTimeoutException();
 					} catch (SocketTimeoutException timeOutException) {
 						pWriter.println("<p><i>Timed out</i></p>");
 					} catch (URISyntaxException exception) {
 						// ignore resource
 					}
 				}
 			}
 			// and recurse more: owl:equivalentClass
 			sameResources = rdf.allOwlEquivalentClass(store, uri.toString());
 			for (String sameResource : sameResources) {
 				if (!alreadyDone.contains(sameResource)) {
 					try {
 						URI sameURI = new URI(sameResource);
 						useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 					} catch (URISyntaxException exception) {
 						// ignore resource
 					}
 				}
 			}
 			// and recurse more: skos:exactMatch
 			sameResources = rdf.getForPredicate(store, uri.toString(), "http://www.w3.org/2004/02/skos/core#exactMatch");
 			for (String sameResource : sameResources) {
 				if (!alreadyDone.contains(sameResource)) {
 					try {
 						URI sameURI = new URI(sameResource);
 						useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 					} catch (URISyntaxException exception) {
 						// ignore resource
 					}
 				}
 			}
 			// get identifiers from DBPedia
 			if (uri.toString().startsWith("http://dbpedia.org/resource")) {
 				try {
 					List<String> casNumbers = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/ontology/casNumber");
 					for (String cas : casNumbers) {
 						System.out.println("CAS reg number: " + cas);
 						// recurse
 						try {
 							URI sameURI = new URI("http://bio2rdf.org/cas:" + cas);
 							useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the CAS RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 				try {
 					List<String> drugBankIDs = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/ontology/drugbank");
 					for (String drugbank : drugBankIDs) {
 						System.out.println("Drugbank code: " + drugbank);
 						// recurse
 						try {
 							URI sameURI = new URI("http://bio2rdf.org/drugbank_drugs:" + drugbank);
 							useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the DrugBank RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 				try {
 					List<String> ids = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/ontology/pubchem");
 					for (String id : ids) {
 						System.out.println("PubChem: " + id);
 						// recurse
 						try {
							URI sameURI = new URI("http://rdf.ncbi.nlm.nih.gov/pubchem/compound/CID" + id);
 							useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the PubChem RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 				try {
 					List<String> ids = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/property/chembl");
 					for (String id : ids) {
 						id = stripDataType(id);
 						System.out.println("CHEMBL: " + id);
 						// recurse
 						try {
 							URI sameURI = new URI("http://linkedchemistry.info/chembl/chemblid/CHEMBL" + id);
 							useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the ChEMBL-RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 				try {
 					List<String> ids = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/property/chebi");
 					for (String id : ids) {
 						id = stripDataType(id);
 						System.out.println("ChEBI: " + id);
 						// recurse
 						try {
 							URI sameURI = new URI("http://bio2rdf.org/chebi:" + id);
 							useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the ChEMBL-RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 				try {
 					List<String> ids = rdf.getForPredicate(store, uri.toString(), "http://dbpedia.org/property/kegg");
 					for (String id : ids) {
 						System.out.println("KEGG: " + id);
 						// recurse
 						try {
 							if (id.startsWith("C")) {
 								URI sameURI = new URI("http://bio2rdf.org/cpd:" + id);
 								useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 							} else if (id.startsWith("D")) {
 								URI sameURI = new URI("http://bio2rdf.org/dr:" + id);
 								useUniveRsalIcebearPowers(pWriter, sameURI, alreadyDone, monitor);	
 							}
 						} catch (URISyntaxException exception) {
 							logger.debug("Error while getting the KEGG RDF: " + exception.getMessage(), exception);
 						}
 					}
 				} catch (BioclipseException exeption) {} // just ignore
 			}
 		} catch (Throwable exception) {
 			logger.warn("Something wrong during IO for " + uri.toString() + ": " + exception.getMessage(), exception);
 		}
 		pWriter.println("</ul>");
 		monitor.worked(1);
 	}
 
 	private String stripDataType(String id) {
 		if (id.contains("^^"))
 			return id.substring(0, id.indexOf("^^"));
 		return id;
 	}
 
 	private void printFoundInformation(PrintWriter pWriter, IRDFStore store, URI ronURI) {
 		// get the rdf:type's
 		try {
 			List<String> approvedTypes = new ArrayList<String>();
 			List<String> types = new ArrayList<String>();
 			types.addAll(rdf.getForPredicate(store, ronURI.toString(), RDF.type.toString()));
 			types.addAll(rdf.getForPredicate(store, ronURI.toString(), RDFS.subClassOf.toString()));
 			// only return types for which we have labels
 			if (types.size() > 0) {
 				for (String type : types) {
 					String label = getLabelForResource(store, type, null);
 					if (label != null && label.length() > 0) {
 						System.out.println("Going to output a label for: " + type);
 						approvedTypes.add(type);
 					}
 				}
 			}
 			// now output what we have left
 			if (approvedTypes.size() > 0) {
 				pWriter.append("<p>");
 				pWriter.println("<b>Is a</b> ");
 				StringBuffer buffer = new StringBuffer();
 				for (String type : approvedTypes) {
 					String label = getLabelForResource(store, type, null);
 					buffer.append(label).append(" <a href=\"").append(type)
 						.append("\"><img src=\"").append(ICON ).append("\" /></a>, ");
 				}
 				String bufferStr = buffer.toString();
 				pWriter.println(bufferStr.substring(0,bufferStr.length()-2));
 				pWriter.append("</p>");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for labels for " + ronURI, exception);
 		}
 		// get a description
 		try {
 			List<String> descriptions = new ArrayList<String>();
 			descriptions.addAll(rdf.getForPredicate(store, ronURI.toString(), DC.description.toString()));
 			descriptions.addAll(rdf.getForPredicate(store, ronURI.toString(), DC_10.description.toString()));
 			descriptions.addAll(rdf.getForPredicate(store, ronURI.toString(), DC_11.description.toString()));
 			descriptions.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://www.w3.org/2004/02/skos/core#definition"));
 			descriptions.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:description"));
 			if (descriptions.size() > 0) {
 				pWriter.println("<b>Descriptions</b><br />");
 				for (String desc : descriptions) {
 					pWriter.append("<p>");
 					pWriter.println(desc);
 					pWriter.append("</p>");
 				}
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for descriptions for " + ronURI, exception);
 		}
 		// get visualizations
 		try {
 			List<String> depictions = new ArrayList<String>();
 			depictions.addAll(rdf.getForPredicate(store, ronURI.toString(), FOAF.depiction.toString()));
 			depictions.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:image"));
 			depictions.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:urlImage"));
 			if (depictions.size() > 0) {
 				pWriter.append("<p>");
 				for (String depiction : depictions) {
 					pWriter.println("<img height=\"80\" src=\"" + depiction + "\" />");
 				}
 				pWriter.append("</p>");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for images for " + ronURI, exception);
 		}
 		// get the identifiers
 		try {
 			List<String> identifiers = new ArrayList<String>();
 			identifiers.addAll(rdf.getForPredicate(store, ronURI.toString(), DC.identifier.toString()));
 			identifiers.addAll(rdf.getForPredicate(store, ronURI.toString(), DC_10.identifier.toString()));
 			identifiers.addAll(rdf.getForPredicate(store, ronURI.toString(), DC_11.identifier.toString()));
 			if (identifiers.size() > 0) {
 				pWriter.println("<p>");
 				pWriter.println("<b>Identifiers</b> ");
 				StringBuffer idString = new StringBuffer();
 				List<String> processedIdentifiers = new ArrayList<String>();
 				for (String identifier : identifiers) {
 					if (!processedIdentifiers.contains(identifier)) {
 						idString.append(identifier).append(", ");
 						processedIdentifiers.add(identifier);
 					}
 				}
 				String fullString = idString.toString();
 				pWriter.println(fullString.substring(0, fullString.length()-2));
 				pWriter.println("</p>");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for identifiers for " + ronURI, exception);
 		}
 		// get the labels
 		try {
 			List<String> labels = new ArrayList<String>();
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), RDFS.label.toString()));
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/obo_resource:synonym"));
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:synonym"));
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://www.w3.org/2004/02/skos/core#prefLabel"));
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://www.w3.org/2004/02/skos/core#altLabel"));
 			labels.addAll(rdf.getForPredicate(store, ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:synonym"));
 			if (labels.size() > 0) {
 				pWriter.println("<p>");
 				pWriter.println("<b>Synonyms</b> ");
 				StringBuffer labelString = new StringBuffer();
 				for (String label : labels) {
 					labelString.append(label).append(", ");
 				}
 				String fullString = labelString.toString();
 				pWriter.println(fullString.substring(0, fullString.length()-2));
 				pWriter.println("</p>");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for labels for " + ronURI, exception);
 		}
 		// get the (home)pages
 		try {
 			List<String> homepages = rdf.getForPredicate(store, ronURI.toString(), FOAF.homepage.toString());
 			if (homepages.size() > 0) {
 				pWriter.println("<b><a href=\"" + homepages.get(0) + "\">Homepage</a></b><br />");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for homepages for " + ronURI, exception);
 		}
 		try {
 			List<String> homepages = rdf.getForPredicate(store, ronURI.toString(), FOAF.page.toString());
 			if (homepages.size() > 0) {
 				pWriter.println("<b><a href=\"" + homepages.get(0) + "\">Homepage</a></b><br />");
 			}
 		} catch (Throwable exception) {
 			logger.warn("Error while quering for web pages for " + ronURI, exception);
 		}
 		// get ChemAxiom properties (for a chemical species)
 		if (ronURI.toString().startsWith("http://www.chemspider.com/")) {
 			final String sparql =
 				"PREFIX chemdomain:  <http://www.polymerinformatics.com/ChemAxiom/ChemDomain.owl#>\n" +
 				"SELECT ?type ?value WHERE {" +
 				"  <" + ronURI.toString() + "> chemdomain:hasPart ?part ." +
 				"  ?part chemdomain:hasIdentifier ?ident ." +
 				"  ?ident chemdomain:hasValue ?value ;" +
 				"    a ?type . " +
 				"}";
 			try {
 				StringMatrix results = rdf.sparql(store, sparql);
 				outputTable(pWriter, results, "type", "value");
 			} catch (Throwable exception) {
 				logger.debug("Error while finding ChemAxiom properties: " + exception.getMessage(), exception);
 			}
 		}
 		// get CHEMINF properties
 		String[] SIO_HAS_ATTRIBUTE = {
 			"http://semanticscience.org/resource/CHEMINF_000200",
 			"http://semanticscience.org/resource/has-attribute",
 			"http://semanticscience.org/resource/SIO_000008"
 		};
 		String[] SIO_HAS_VALUE = {
 			"http://semanticscience.org/resource/has-value",
 			"http://semanticscience.org/resource/SIO_000300"
 		};
 
 		try {
 			Map<String,String> props = new HashMap<String, String>(); 
 			for (String hasAttribute : SIO_HAS_ATTRIBUTE) {
 				for (String hasValue : SIO_HAS_VALUE) {
 					// get SIO properties
 					String sparql =
 						"PREFIX sio: <http://semanticscience.org/resource/>\n" +
 						"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
 						"SELECT ?label ?desc ?value WHERE {" +
 						"  <" + ronURI.toString() + "> <" + hasAttribute + "> ?property ." +
 						"  ?property <" + hasValue + "> ?value ." +
 						"  OPTIONAL { ?property a ?desc . } " +
 						"  OPTIONAL { ?property rdfs:label ?label . }" +
 						"}";
 					StringMatrix results = rdf.sparql(store, sparql);
 					for (int i=1; i<=results.getRowCount(); i++) {
 						String type = "NA";
 						if (results.get(i, "label") != null) {
 							type = results.get(i, "label");
 						} else if (results.get(i, "desc") != null) {
 							type = results.get(i, "desc");
 						}
 						props.put(type, results.get(i, "value"));			
 					}
 				}
 			}
 			outputTable(pWriter, props);
 		} catch (Throwable exception) {
 			logger.debug("Error while finding CHEMINF (PubChem style) properties: " + exception.getMessage(), exception);
 		}
 		// get Bio2RDF properties
 		if (ronURI.toString().startsWith("http://bio2rdf.org/")) {
 			Map<String,String> resultMap = new HashMap<String, String>();
 			addPredicateToMap(store, resultMap, "Mass", ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:mass");
 			addPredicateToMap(store, resultMap, "SMILES", ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:smiles");
 			addResourcePredicateToMap(store, resultMap, "Conjugate Base", ronURI.toString(), "http://bio2rdf.org/obo_resource:is_conjugate_base_of");
 			addResourcePredicateToMap(store, resultMap, "Functional Parent", ronURI.toString(), "http://bio2rdf.org/obo_resource:has_functional_parent");
 			addPredicateToMap(store, resultMap, "Charge", ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:charge");
 			addPredicateToMap(store, resultMap, "Formula", ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:formula");
 			addPredicateToMap(store, resultMap, "IUPAC name", ronURI.toString(), "http://bio2rdf.org/bio2rdf_resource:iupacName");
 			addPredicateToMap(store, resultMap, "CACO2 permeability", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:experimentalCaco2Permeability");
 			addPredicateToMap(store, resultMap, "LogP", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:experimentalLogpHydrophobicity");
 			addPredicateToMap(store, resultMap, "Water solubility", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:experimentalWaterSolubility");
 			addPredicateToMap(store, resultMap, "Food interactions", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:foodInteraction");
 			addPredicateToMap(store, resultMap, "Mechanism of action", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:mechanismOfAction");
 			addPredicateToMap(store, resultMap, "Melting point", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:meltingPoint");
 			addPredicateToMap(store, resultMap, "Monoisotopic mass", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:molecularWeightMono");
 			addPredicateToMap(store, resultMap, "Isoelectric point", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:pkaIsoelectricPoint");
 			addPredicateToMap(store, resultMap, "Toxicity", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:toxicity");
 			addPredicateToMap(store, resultMap, "Protein binding", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:primaryAccessionNo");
 			addPredicateToMap(store, resultMap, "Pharmacology", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:pharmacology");
 			addPredicateToMap(store, resultMap, "Biotransformation", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:biotransformation");
 			addResourcePredicateToMap(store, resultMap, "Categories", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:drugCategory");
 			addResourcePredicateToMap(store, resultMap, "Dosage forms", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:dosageForm");
 			addResourcePredicateToMap(
 				store, resultMap, "Drug interactions", ronURI.toString(), "http://bio2rdf.org/drugbank_ontology:dosageForm",
 				"http://bio2rdf.org/drugbank_druginteractions:" // only list drug-drug interactions
 			);
 			outputTable(pWriter, resultMap);
 		}
 		// get DBPedia properties
 		if (ronURI.toString().startsWith("http://dbpedia.org/resource/")) {
 			Map<String,String> resultMap = new HashMap<String, String>();
 			addPredicateToMap(store, resultMap, "Administration", ronURI.toString(), "http://dbpedia.org/property/routesOfAdministration");
 			addPredicateToMap(store, resultMap, "Bioavailability", ronURI.toString(), "http://dbpedia.org/property/bioavailability");
 			addPredicateToMap(store, resultMap, "Boiling point", ronURI.toString(), "http://dbpedia.org/property/boilingPoint");
 			addPredicateToMap(store, resultMap, "Melting point", ronURI.toString(), "http://dbpedia.org/property/meltingPoint");
 			addResourcePredicateToMap(store, resultMap, "Metabolism", ronURI.toString(), "http://dbpedia.org/property/metabolism");
 			addResourcePredicateToMap(
 				store, resultMap, "Excretion", ronURI.toString(), "http://dbpedia.org/property/excretion"
 			);
 			outputTable(pWriter, resultMap);
 		}
 		// get FreeBase properties
 		if (ronURI.toString().startsWith("http://rdf.freebase.com/ns/")) {
 			Map<String,String> resultMap = new HashMap<String, String>();
 			addPredicateToMap(store, resultMap, "Average molar mass", ronURI.toString(), "http://rdf.freebase.com/ns/chemistry.chemical_compound.average_molar_mass");
 			addPredicateToMap(store, resultMap, "Boiling point", ronURI.toString(), "http://rdf.freebase.com/ns/chemistry.chemical_compound.boiling_point");
 			addPredicateToMap(store, resultMap, "Melting point", ronURI.toString(), "http://rdf.freebase.com/ns/chemistry.chemical_compound.melting_point");
 			addPredicateToMap(store, resultMap, "Density", ronURI.toString(), "http://rdf.freebase.com/ns/chemistry.chemical_compound.density");
 			outputTable(pWriter, resultMap);
 		}
 		// get FU Berlin LODD properties
 		if (ronURI.toString().startsWith("http://www4.wiwiss.fu-berlin.de/")) {
 			Map<String,String> resultMap = new HashMap<String, String>();
 			addPredicateToMap(store, resultMap, "Absorption", ronURI.toString(), "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/abdsorption");
 			addResourcePredicateToMap(store, resultMap, "Targets", ronURI.toString(), "http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/target");
 			addResourcePredicateToMap(store, resultMap, "Side effects", ronURI.toString(), "http://www4.wiwiss.fu-berlin.de/sider/resource/sider/sideEffect");
 			outputTable(pWriter, resultMap);
 		}
 	}
 
 	private String getLabelForResource(IRDFStore currentStore, String resource, IProgressMonitor monitor) {
 		if (resourceMap.containsKey(resource)) return resourceMap.get(resource);
 		if (resource.startsWith("http://rdf.freebase.com/ns/")) return null; // ignore all of them which we did not accept specifically
 		if (resource.startsWith("http://sw.opencyc.org")) return null;
 		System.out.println("Needing a label for resource: " + resource);
 
 		// try the current store first
 		String label = getLabelFromStore(resource, currentStore);
 		if (label != null) return label;
 		
 		try {
 			URI uri = new URI(resource);
 			IRDFStore store = rdf.createInMemoryStore();
 			System.out.println("Getting a label online for resource: " + resource);
 			rdf.importURL(store, uri.toString(), extraHeaders, monitor);
 			System.out.println(rdf.asRDFN3(store)); // so that I can check what is there...
 			return getLabelFromStore(resource, store);
 		} catch (Throwable e) {
 			logger.debug("Something went wrong with getting a label: " + e.getMessage(), e);
 			resourceMap.put(resource, null); // I don't want to try again
 			return null;
 		}
 	}
 
 	private String getLabelFromStore(String resource, IRDFStore store) {
 		List<String> labels = new ArrayList<String>();
 		labels.addAll(getPredicate(store, resource, DC.title.toString()));
 		labels.addAll(getPredicate(store, resource, DC_10.title.toString()));
 		labels.addAll(getPredicate(store, resource, DC_11.title.toString()));
 		labels.addAll(getPredicate(store, resource, RDFS.label.toString()));
 		labels.addAll(getPredicate(store, resource, "http://www.w3.org/2004/02/skos/core#prefLabel"));
 		labels.addAll(getPredicate(store, resource, "http://www.w3.org/2004/02/skos/core#altLabel"));
 		
 		if (labels.size() == 0) {
 			resourceMap.put(resource, null); // don't try again
 			return null; // OK, did not find anything suitable
 		}
 		// the first will do fine, but pick the first English one
 		for (String label : labels) {
 			logger.debug("Is this english? -> " + label);
 			if (label.endsWith("@en")) {
 				label = label.substring(0, label.indexOf("@en")); // remove the lang indication
 				resourceMap.put(resource, label); // store it for later use
 				return label;
 			} else if (!label.contains("@")) {
 				resourceMap.put(resource, label); // store it for later use
 				return label;
 			}
 		}
 		logger.debug("Did not find an English label :(");
 		return labels.get(0); // no labels marked @en, so pick the first
 	}
 
 	private List<String> getPredicate(IRDFStore store, String resource, String predicate) {
 		try {
 			return rdf.getForPredicate(store, resource, predicate);
 		} catch (Throwable e) {
 			logger.debug("Error while getting value for " + predicate + ": " + e.getMessage(), e);
 		};
 		return Collections.emptyList();
 	}
 	
 	// only works for one property per predicate
 	private void addPredicateToMap(IRDFStore store, Map<String, String> resultMap, String label, String resource, String predicate) {
 		try {
 			List<String> props = rdf.getForPredicate(store, resource, predicate);
 			if (props.size() > 0) {
 				resultMap.put(label, props.get(0));
 				System.out.println("Adding " + label + " -> " + props.get(0));
 			}
 		} catch (BioclipseException exception) {
 			logger.debug("Error while getting Bio2RDF props: " + exception.getMessage(), exception);
 		}
 	}
 
 	private void addResourcePredicateToMap(IRDFStore store, Map<String, String> resultMap, String label, String resource, String predicate) {
 		addResourcePredicateToMap(store, resultMap, label, resource, predicate, null);
 	}
 	private void addResourcePredicateToMap(IRDFStore store, Map<String, String> resultMap, String label, String resource, String predicate, String uriFilter) {
 		try {
 			List<String> props = rdf.getForPredicate(store, resource, predicate);
 			if (props.size() > 0) {
 				StringBuffer buffer = new StringBuffer();
 				for (String objectURI : props) {
 					if (uriFilter == null || objectURI.contains(uriFilter)) {
 						String objectLabel = getLabelForResource(store, objectURI, null);
 						if (objectLabel != null && objectLabel.length() > 0) {
 							System.out.println("Adding " + label + " -> " + props.get(0));
 							buffer.append(objectLabel).append(" <a href=\"")
 							    .append(objectLabel).append("\"><img src=\"")
 							    .append(ICON).append("\" /></a>, "
 							);
 						} else {
 							logger.debug("Could not find a label for: " + objectURI);
 						}
 					}
 				}
 				String result = buffer.toString();
 				if (result.length() > 2)
 					result = result.substring(0, result.length()-2);
 				resultMap.put(label, result);
 			}
 		} catch (BioclipseException exception) {
 			logger.debug("Error while getting Bio2RDF props: " + exception.getMessage(), exception);
 		}
 	}
 
 	private void outputTable(PrintWriter pWriter, StringMatrix results,
 			String string, String string2) {
 		if (results.getRowCount() > 0) {
 			pWriter.println("<table border='0'>");
 			for (int i=1; i<=results.getRowCount(); i++) {
 				pWriter.println("  <tr>");
 				pWriter.println("    <td valign=\"top\"><b>" + results.get(i, string) + "</b></td>");
 				pWriter.println("    <td valign=\"top\">" + results.get(i, string2) + "</td>");
 				pWriter.println("  </tr>");
 			}
 			pWriter.println("</table>");
 		}
 	}
 
 	private void outputTable(PrintWriter pWriter, Map<String,String> results) {
 		if (results.size() > 0) {
 			pWriter.println("<table border='0'>");
 			for (String key : results.keySet()) {
 				pWriter.println("  <tr>");
 				pWriter.println("    <td valign=\"top\"><b>" + key + "</b></td>");
 				String property = stripDataType(results.get(key));
 				pWriter.println("    <td valign=\"top\">" + property + "</td>");
 				pWriter.println("  </tr>");
 			}
 			pWriter.println("</table>");
 		}
 	}
 
 	class IcebearWorkload {
 		
 		Set<URI> todo = new HashSet<URI>();
 		Set<URI> done = new HashSet<URI>();
 
 		public boolean hasMoreWork() {
 			System.out.println("work left todo: " + todo.size());
 			return todo.size() != 0;
 		}
 
 		public URI getNextURI() {
 			URI nextURI = todo.iterator().next();
 			System.out.println("next URI: " + nextURI);
 			todo.remove(nextURI);
 			done.add(nextURI);
 			return nextURI;
 		}
 
 		/**
 		 * Returns false when the URI was already processed or is already scheduled.
 		 */
 		public boolean addNewURI(String newURI) {
 			System.out.println("Adding URI: " + newURI);
 			try {
 				URI uri = new URI(newURI);
 				if (done.contains(uri) || todo.contains(uri)) {
 					System.out.println("Already got it...");
 					return false;
 				}
 
 				todo.add(uri);
 				return true;
 			} catch (URISyntaxException e) {
 				System.out.println("Failed to add the new URI: " + e.getMessage());
 				return false;
 			}
 		}
 	}
 }
