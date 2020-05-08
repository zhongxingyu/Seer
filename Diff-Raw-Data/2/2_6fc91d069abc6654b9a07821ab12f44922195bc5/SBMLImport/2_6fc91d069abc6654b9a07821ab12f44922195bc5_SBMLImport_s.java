 package de.zrho.bioview.sbml;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.sbml.jsbml.ListOf;
 import org.sbml.jsbml.Model;
 import org.sbml.jsbml.SBMLReader;
 import org.sbml.jsbml.Species;
 import org.sbml.jsbml.SpeciesReference;
 
 import de.zrho.bioview.model.Complex;
 import de.zrho.bioview.model.Network;
 import de.zrho.bioview.model.Reaction;
 import de.zrho.collections.IndexedSet;
 
 public class SBMLImport {
 	
 	public static Network<String, Double> importNetwork(File file) throws Exception {
 		return importNetwork(new SBMLReader().readSBML(file).getModel());
 	}
 
 	public static Network<String, Double> importNetwork(Model model) {
 		// Import the list of species
 		List<String> species = new IndexedSet<>(model.getListOfSpecies().size());
 		for (Species s : model.getListOfSpecies()) species.add(importSpecies(s));
 		
 		// Import the reactions and collect complexes on the run
 		List<Reaction<String, Double>> reactions = new IndexedSet<>(model.getListOfReactions().size());
 		IndexedSet<Complex<String>> complexes = new IndexedSet<>();
 		
 		for (org.sbml.jsbml.Reaction sourceReaction : model.getListOfReactions()) {
 			Complex<String> reactants = importComplex(sourceReaction.getListOfReactants(), complexes);
 			Complex<String> products = importComplex(sourceReaction.getListOfProducts(), complexes);
 
 			// TODO Find rates
 			reactions.add(new Reaction<String, Double>(reactants, products, 1.0));
 			if(sourceReaction.isReversible()) {
 				reactions.add(new Reaction<String, Double>(products, reactants, 1.0));
 			}
 		}
 		
		return new Network(species, complexes, reactions);
 	}
 	
 	private static Complex<String> importComplex(ListOf<SpeciesReference> source, IndexedSet<Complex<String>> target) {
 		Map<String, Double> complex = new HashMap<>();
 		
 		for (SpeciesReference ref : source) {
 			complex.put(ref.getSpecies(), ref.getStoichiometry());
 		}
 		
 		Complex<String> c = new Complex<String>(complex);
 		int index = target.indexOf(c);
 		if(index >= 0) {
 			return target.get(index);
 		} else {
 			target.add(c);
 			return c;
 		}
 			
 	}
 	
 	private static String importSpecies(Species source) {
 		return source.getId();
 	}
 	
 	
 }
