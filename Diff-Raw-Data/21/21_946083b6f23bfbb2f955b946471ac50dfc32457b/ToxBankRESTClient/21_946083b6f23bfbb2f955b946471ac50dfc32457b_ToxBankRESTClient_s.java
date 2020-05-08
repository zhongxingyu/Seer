 package net.toxbank.isa.creator.plugin;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.toxbank.client.TBClient;
 import net.toxbank.client.resource.IToxBankResource;
 import net.toxbank.client.resource.Organisation;
 import net.toxbank.client.resource.Project;
 import net.toxbank.client.resource.Protocol;
 import net.toxbank.client.resource.ProtocolClient;
 import net.toxbank.client.resource.User;
 import net.toxbank.isa.creator.plugin.resource.ResourceDescription;
 import net.toxbank.isa.creator.plugin.xml.KeywordsRDFHandler;
 import net.toxbank.isa.creator.plugin.xml.ResourceXMLHandler;
 
 import org.isatools.isacreator.configuration.RecommendedOntology;
 import org.isatools.isacreator.managers.ApplicationManager;
 import org.isatools.isacreator.ontologymanager.OntologySourceRefObject;
 import org.isatools.isacreator.ontologymanager.common.OntologyTerm;
 import org.isatools.isacreator.plugins.host.service.PluginOntologyCVSearch;
 import org.isatools.isacreator.plugins.registries.OntologySearchPluginRegistry;
 import org.opentox.rest.RestException;
 
 
 /**
  * Created by the Toxbank
  *
  * @author Nina Jeliazkova (jeliazkova.nina@gmail.com)
  *         <p/>
  *         Date: 27/04/2012
  *         Time: 13:28
  * 
  * Based on https://github.com/ISA-tools/NovartisMetastore by       
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 12/09/2011
  *         Time: 16:51
  */
 public class ToxBankRESTClient implements PluginOntologyCVSearch {
 	
 	public static List<ResourceDescription> resourceInformation;
     public static String queryURL;
     
     static {
         ResourceXMLHandler xmlHandler = new ResourceXMLHandler();
         resourceInformation = xmlHandler.parseXML();
     }
 
     public Map<OntologySourceRefObject, List<OntologyTerm>> searchRepository(String term) {
         return searchRepository(term, new HashMap<String, RecommendedOntology>(), false);
 
     }
     
     enum TBRESOURCE {
     	TBU,
     	TBP,
     	TBC,
     	TBO,
     	TBK
     }
     
     private Map<OntologySourceRefObject, List<OntologyTerm>> performQuery(
     							Map<OntologySourceRefObject, List<OntologyTerm>> results,
     							TBClient tbclient,
     							String term, ResourceDescription resourceDescription) {
     	final String prefix = "SEURAT-Protocol-";
         try {
         	TBRESOURCE tbresource = TBRESOURCE.valueOf(resourceDescription.getResourceAbbreviation());
 			OntologySourceRefObject source = new OntologySourceRefObject(
 										resourceDescription.getResourceAbbreviation(),  
 										resourceDescription.getQueryURL(),
 										"",
 										resourceDescription.getResourceName());
         	switch (tbresource) {
         	case TBU: {
         		List<User> items = tbclient.getUserClient().searchRDF_XML(new URL(resourceDescription.getQueryURL()),term);
         		if (items!=null && items.size()>0) 
         			convertResourceResult(items,source,results);
         		
         		break;
         	}
         	case TBC: {
         		List<Project> items = tbclient.getProjectClient().searchRDF_XML(new URL(resourceDescription.getQueryURL()),term);
         		if (items!=null && items.size()>0) 
         			convertResourceResult(items,source,results);
         		break;
         	}
         	case TBP: {
         		List<Protocol> items  = null;
         		ProtocolClient pcli = tbclient.getProtocolClient();
         		//if protocol identifier, go directly by the URL
         		if (term.startsWith(prefix)) 
         			items = pcli.getRDF_XML(new URL(String.format("%s/%s",resourceDescription.getQueryURL(),term)));
         		//otherwise
         		if (items==null || items.size()==0) {
 	        		if (resourceDescription.getSearchServiceURL()!=null) 
 	        		try {
 	        			//try the search service
 	        			List<URL> urls = tbclient.getSearchClient().searchURI(new URL(String.format("%ssearch?resourceType=protocol",resourceDescription.getSearchServiceURL())),term);
 	        			if (urls!=null) {
 	        				items = new ArrayList<Protocol>();
 	        				for (URL url : urls) try {
 	        					Logger.getLogger(this.getClass().getName()).log(Level.FINE,url.toExternalForm());
 	        					items.addAll(pcli.get(url));
 	        				} catch (Exception x) {
 	        					//we can get the URL, but not have rights to retrieve the metadata
 	        					Logger.getLogger(this.getClass().getName()).log(Level.WARNING,url.toExternalForm(),x);
 	        					Protocol protocol = new Protocol(url);
 	        					items.add(protocol);
 	        				}
 	        			}
 	        			
 	        		} catch (Exception x) { Logger.getLogger(this.getClass().getName()).log(Level.WARNING,resourceDescription.getSearchServiceURL(),x);}
 	        		//if no resutls, go to the protocol service directly
 					if (items==null || items.size()==0) 
 		        		items = pcli.searchRDF_XML(new URL(resourceDescription.getQueryURL()),term);
         		}
         		if (items!=null && items.size()>0) 
         			convertResourceResult(items,source,results);
         		
         		
         		break;
         	}
         	case TBO: {
         		List<Organisation> items = tbclient.getOrganisationClient().searchRDF_XML(new URL(resourceDescription.getQueryURL()),term);
         		if (items!=null && items.size()>0) 
         			convertResourceResult(items,source,results);
         		break;
         	}
         	case TBK: {
         		List<OntologyTerm> terms = searchKeywords(term,resourceDescription);
         		if ((terms!=null) && terms.size()>0)  results.put(KeywordsRDFHandler.source,terms);
         		break;
         	}
         	}
     		
             return results;
         } catch (RestException x) {
             String msg = String.format("[%s] %s Error connecting to %s",x.getStatus(),x.getMessage(), resourceDescription.getQueryURL());
             Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,msg,x);
         } catch (MalformedURLException x) {
         	String msg = "Wrong URL ...";
         	Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,msg,x);
         } catch (Exception x) {
         	String msg = "No file found. Assuming connection is down...";
         	Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,msg,x);
         } finally {
         }
         return new HashMap<OntologySourceRefObject, List<OntologyTerm>>();
     }
     
     public Map<OntologySourceRefObject, List<OntologyTerm>> searchRepository(String term, 
     			Map<String, RecommendedOntology> recommendedOntologies, boolean searchAll) {
 		 Map<OntologySourceRefObject, List<OntologyTerm>> results = new HashMap<OntologySourceRefObject, List<OntologyTerm>>();
 		 System.out.println(recommendedOntologies);
     	TBClient tbclient = new TBClient();
     	 try {
     		 boolean loggedin = false;
 		        for (ResourceDescription resourceDescription : resourceInformation) {
 
 		        	if (!loggedin) {
 		        		loggedin = tbclient.login(resourceDescription.getUsername(),resourceDescription.getPassword());
 		        	}
 		            String fieldDetails = ApplicationManager.getCurrentlySelectedFieldName();
 		            // only do the search if the field matches one expected by the system
 		            if (searchAll) {
 		                results.putAll(performQuery(results,tbclient,term, resourceDescription));
 		            } else {
 		                if (checkIfResourceHasField(resourceDescription, fieldDetails) || checkIfResourceIsRecommended(resourceDescription, recommendedOntologies)) {
 		                    System.out.println("Querying on " + resourceDescription.getResourceName() + " for " + term + " on " + fieldDetails);
 		                    results.putAll(performQuery(results,tbclient, term, resourceDescription));
 		                }
 		            }
 		        }
 		     
          } catch (MalformedURLException e) {
              System.out.println("Wrong URL ...");
              e.printStackTrace();
          } catch (Exception e) {
              System.out.println("No file found. Assuming connection is down...");
              e.printStackTrace();
          } finally {
         		try { tbclient.logout(); } catch (Exception x) {}
         		try { tbclient.close(); } catch (Exception x) {}
          }	        
 
          return results;
     }
     
     /**
      * We can check against current assay and the field
      *
      * @param resourceDescription - resource to check
      * @param fieldDetails        - field to look for
      * @return true or false. True if the resource should be searched on for this field.
      */
     public boolean checkIfResourceHasField(ResourceDescription resourceDescription, String fieldDetails) {
     	return false;
     	/*
         if (fieldDetails == null) {
             return false;
         } else {
             String fieldName = fieldDetails.substring(fieldDetails.lastIndexOf(":>") + 2).trim();
 
             if (resourceDescription.getResourceFields().containsKey(fieldName)) {
                 String assayMeasurement, assayTechnology = "";
                 if (fieldDetails.contains("using")) {
                     String[] fields = fieldDetails.split("using");
                     assayMeasurement = fields[0].trim();
                     assayTechnology = fields[1].substring(0, fields[1].lastIndexOf(":>")).trim();
                 } else {
                     assayMeasurement = fieldDetails.substring(0, fieldDetails.lastIndexOf(":>"));
                 }
 
                 for (ResourceField resourceField : resourceDescription.getResourceFields().get(fieldName)) {
                     if (resourceField.getAssayMeasurement().isEmpty()) {
                         return true;
                     } else if (assayMeasurement.equalsIgnoreCase(resourceField.getAssayMeasurement())
                             && assayTechnology.equalsIgnoreCase(resourceField.getAssayTechnology())) {
                         return true;
                     }
                 }
             }
             return false;
         }
         */
     }
 
     
     private boolean checkIfResourceIsRecommended(ResourceDescription resourceDescription, Map<String, RecommendedOntology> recommendedOntologies) {
         for (String ontology : recommendedOntologies.keySet()) {
             if (recommendedOntologies.get(ontology).getOntology().getOntologyAbbreviation().equals(resourceDescription.getResourceAbbreviation())) {
                 return true;
             }
         }
         return false;
     }
     protected List<OntologyTerm> searchKeywords(String term, ResourceDescription resourceDescription)  {
     	KeywordsRDFHandler keywordsHandler = new KeywordsRDFHandler();
     	String keywordsPath ;
         try {
         	URL url = new URL(resourceDescription.getQueryURL());
         	File file = new File(url.getFile());
         	if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());
         	keywordsPath = file.getAbsolutePath();
         } catch (MalformedURLException e) {
         	//consider relative path within ISACreator
         	keywordsPath = resourceDescription.getQueryURL();
         } catch (FileNotFoundException e) {
             System.out.println("No file found. Assuming connection is down...");
             e.printStackTrace();
             return null;
         }
         
         try {
         	  return keywordsHandler.parse(keywordsPath,term);
         } catch (Exception x) {
         	x.printStackTrace();
         	System.out.println("Invalid file path "+resourceDescription.getQueryURL());
         	return null;
         }
     }
 
     public void registerSearch() {
         OntologySearchPluginRegistry.registerPlugin(this);
     }
 
     public void deregisterSearch() {
         OntologySearchPluginRegistry.deregisterPlugin(this);
     }
 
   
     protected void convertResourceResult(List<? extends IToxBankResource> resources,OntologySourceRefObject source,Map<OntologySourceRefObject, List<OntologyTerm>> results) {
 
     	ArrayList<OntologyTerm> terms = new ArrayList<OntologyTerm>();
     	 for(IToxBankResource resource:resources) {
              OntologyTerm ontologyTerm = new OntologyTerm(resource.getTitle(),null,"", source);
             ontologyTerm.setOntologyPurl(source.getSourceFile()+"/");
              ontologyTerm.setOntologyTermAccession(resource.getResourceURL().toExternalForm());
             // ontologyTerm.addToComments("Organisation", user.getOrganisations().toString());
             ontologyTerm.setOntologyTermName(ontologyTerm.getOntologyTermAccession());
              if (resource instanceof User) {
             	 User user = (User) resource;
             	 String person = String.format("%s %s %s",
             			 user.getTitle()==null?"":user.getTitle(),
             			 user.getFirstname()==null?"":user.getFirstname(),
             			 user.getLastname()==null?"":user.getLastname());
             	 ontologyTerm.setOntologyTermAccession(person);
             	 ontologyTerm.addToComments("Name",person);
             	 if (user.getOrganisations()!=null) {
             		 StringBuilder b = new StringBuilder();
             		 String d = "";
             		 for (Organisation org : user.getOrganisations()) { b.append(d);b.append(org.getTitle());d=","; } 
             		 ontologyTerm.addToComments("Affiliation",b.toString());
             	 }
             	 if (user.getProjects()!=null) {
             		 StringBuilder b = new StringBuilder();
             		 String d = "";
             		 for (Project project : user.getProjects()) { b.append(d);b.append(project.getTitle());d=","; } 
             		 ontologyTerm.addToComments("Consortium",b.toString());
             	 }
             	 if (user.getHomepage()!=null) {
             		 String url = user.getHomepage().toExternalForm();
             		 if (url.startsWith("http")) url = String.format("<a href='%s'>%s</a>", url,url);
             		 ontologyTerm.addToComments("WWW",url);
             	 }
             	 if (user.getEmail()!=null) ontologyTerm.addToComments("e-mail",user.getEmail());
             	 if (user.getWeblog()!=null) ontologyTerm.addToComments("Blog",user.getWeblog().toExternalForm());
              } else if (resource instanceof Protocol) {
             	 Protocol protocol = (Protocol) resource;
            	 ontologyTerm.setOntologyTermAccession(resource.getTitle());
             	 if (protocol.getIdentifier()!=null) ontologyTerm.addToComments("Protocol identifier",protocol.getIdentifier());
             	 if (protocol.getTitle()!=null) ontologyTerm.addToComments("Name",protocol.getTitle());
             	 if (protocol.getAbstract()!=null) ontologyTerm.addToComments("Abstract",protocol.getAbstract());
             	 if (protocol.getOrganisation()!=null) ontologyTerm.addToComments("Organisation",protocol.getOrganisation().getTitle());
             	 if (protocol.getOwner()!=null) ontologyTerm.addToComments("Owner",protocol.getOwner().toString());
             	 if (protocol.getProject()!=null) ontologyTerm.addToComments("Consortium",protocol.getProject().getTitle());
             	 //if (protocol.getSubmissionDate()!=null) ontologyTerm.addToComments("Submitted",new Date(protocol.getSubmissionDate())));
             	 if (protocol.getStatus()!=null) ontologyTerm.addToComments("Status",protocol.getStatus().toString());
             	 if (protocol.getResourceURL()!=null) {
             		 String url = protocol.getResourceURL().toExternalForm();
             		 if (url.startsWith("http")) url = String.format("<a href='%s' title='%s'>%s</a>", url,url,url);
             		 ontologyTerm.addToComments("WWW",url);
             	 }
             	 
              } else if (resource instanceof Project) {
             	 Project project = (Project) resource;
            	 ontologyTerm.setOntologyTermAccession(resource.getTitle());
             	 ontologyTerm.addToComments("Consortium name",project.getTitle());
              } else if (resource instanceof Organisation) {
            	 ontologyTerm.setOntologyTermAccession(resource.getTitle());
             	 ontologyTerm.addToComments("Organisation name",resource.getTitle());
              }
              terms.add(ontologyTerm);
              
          }
     	 if (terms!=null && (terms.size()>0)) {
     		 /**
     		  * The results don't get sorted in the plugin panel, so not much sense here
     		 Collections.sort(terms,new Comparator<OntologyTerm>() {
 				public int compare(OntologyTerm o1, OntologyTerm o2) {
 					return o1.getComments().get("category").compareTo(o2.getComments().get("category"));
 				}
 			});
 			**/
     		 results.put(source, terms);
     	 }
     }
 
 	public Set<String> getAvailableResourceAbbreviations() {
 		 Set<String> abbreviations = new TreeSet<String>();
 		 for (ResourceDescription resourceDescription : resourceInformation)
 			 abbreviations.add(resourceDescription.getResourceAbbreviation());
 		 return abbreviations;
 	}
 
 	public boolean hasPreferredResourceForCurrentField(
 			Map<String, RecommendedOntology> arg0) {
 		System.out.println("hasPreferredResourceForCurrentField");
 		System.out.println(arg0);
 		return false;
 	}
 }
