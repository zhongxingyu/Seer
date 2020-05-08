 package edu.utep.cybershare.vlc.ontology;
 
 import java.net.URI;
 import java.util.Hashtable;
 
 import org.semanticweb.owlapi.model.IRI;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 
 import edu.utep.cybershare.vlc.model.Element;
 
 public class Individuals {
 
 	private static Hashtable<String, OWLNamedIndividual> individuals = new Hashtable<String, OWLNamedIndividual>();
 	
 	public static boolean doesIndividualExist(Element element, OntologyToolset bundle){
 		String individualName = makeURICompliantFragment(element.getIdentification(), bundle);
		String individualIRI = bundle.getIndividualIRI(individualName);
		return individuals.get(individualIRI) != null;
 	}
 	
 	public static OWLNamedIndividual getIndividual(URI uri, OntologyToolset bundle){
 		return getIndividual(uri.toASCIIString(), bundle);
 	}
 	
 	public static OWLNamedIndividual getIndividual(Element element, OntologyToolset bundle){	
		String individualName = makeURICompliantFragment(element.getIdentification(), bundle);
		String individualIRI = bundle.getIndividualIRI(individualName);
		
		return getIndividual(individualIRI, bundle);
 	}
 	
	private static OWLNamedIndividual getIndividual(String uri, OntologyToolset bundle){		
		OWLNamedIndividual individual = individuals.get(uri);
 		
 		if(individual == null){
			individual = bundle.getDataFactory().getOWLNamedIndividual(IRI.create(uri));
			individuals.put(uri, individual);
 		}
 		return individual;
 	}
 		
 	private static String makeURICompliantFragment(String candidateIRIFragment, OntologyToolset bundle){		
 		String newIRIFragment = candidateIRIFragment
 				.replaceAll("[^A-Za-z0-9\\s]", "")
 				.replaceAll("\\s", "-");
 		
 		try{
 			new URI(bundle.getIndividualIRI(newIRIFragment));
 			return newIRIFragment;
 		}
 		catch(Exception e){
 			System.err.println("Offending Name: " + candidateIRIFragment);
 			for(int i = 0; i < candidateIRIFragment.length(); i ++)
 				System.err.println("character: " + candidateIRIFragment.charAt(i) + ", value: " + candidateIRIFragment.codePointAt(i));
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
