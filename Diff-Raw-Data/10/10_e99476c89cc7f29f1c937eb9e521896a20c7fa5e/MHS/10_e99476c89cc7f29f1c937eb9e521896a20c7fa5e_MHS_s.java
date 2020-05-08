 package au.com.miskinhill.rdf.vocabulary;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 
 public final class MHS {
 
 	public static final String NS_URI = "http://miskinhill.com.au/rdfschema/1.0/";
 	
 	public static final Resource Journal = ResourceFactory.createResource(
 			NS_URI + "Journal");
 	public static final Resource Issue = ResourceFactory.createResource(
 			NS_URI + "Issue");
 	public static final Resource IssueContent = ResourceFactory.createResource(
 			NS_URI + "IssueContent");
 	public static final Resource Article = ResourceFactory.createResource(
 			NS_URI + "Article");
 	public static final Resource Book = ResourceFactory.createResource(
 			NS_URI + "Book");
 	public static final Resource Review = ResourceFactory.createResource(
 			NS_URI + "Review");
 	public static final Resource Author = ResourceFactory.createResource(
 			NS_URI + "Author");
 	public static final Resource Institution = ResourceFactory.createResource(
 			NS_URI + "Institution");
 	public static final Resource Obituary = ResourceFactory.createResource(
 	        NS_URI + "Obituary");
 	public static final Resource Citation = ResourceFactory.createResource(
 	        NS_URI + "Citation");
 	
 	public static final Property cites = ResourceFactory.createProperty(
 	        NS_URI, "cites");
 	public static final Property reviews = ResourceFactory.createProperty(
 	        NS_URI, "reviews");
 	public static final Property responsibility = ResourceFactory.createProperty(
 	        NS_URI, "responsibility");
 	public static final Property translator = ResourceFactory.createProperty(
 	        NS_URI, "translator");
 	public static final Property onlinePublicationDate = ResourceFactory.createProperty(
 	        NS_URI, "onlinePublicationDate");
 	
 	///CLOVER:OFF
 	private MHS() {
     }
 	///CLOVER:ON
 	
 }
