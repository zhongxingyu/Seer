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
		return individuals.get(individualName) != null;
 	}
 	
 	public static OWLNamedIndividual getIndividual(URI uri, OntologyToolset bundle){
 		return getIndividual(uri.toASCIIString(), bundle);
 	}
 	
 	public static OWLNamedIndividual getIndividual(Element element, OntologyToolset bundle){	
		return getIndividual(element.getIdentification(), bundle);
 	}
 	
	private static OWLNamedIndividual getIndividual(String id, OntologyToolset bundle){
		String individualName = makeURICompliantFragment(id, bundle);
		
		OWLNamedIndividual individual = individuals.get(individualName);
		IRI individualIRI;
 		
 		if(individual == null){
			individualIRI = IRI.create(bundle.getIndividualIRI(individualName));
			individual = bundle.getDataFactory().getOWLNamedIndividual(individualIRI);
			individuals.put(individualName, individual);
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
