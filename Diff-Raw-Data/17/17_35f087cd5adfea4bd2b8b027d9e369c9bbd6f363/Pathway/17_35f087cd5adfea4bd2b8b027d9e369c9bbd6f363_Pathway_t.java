 package uk.ac.bbk.REx.internalTypes;
 
 import java.io.FileNotFoundException;
 import java.util.*;
 
 import uk.ac.bbk.REx.exception.CHEBIException;
 import uk.ac.bbk.REx.utils.QueryBuilder;
 import uk.ac.ebi.mdk.domain.annotation.InChI;
 import uk.ac.ebi.mdk.domain.entity.Metabolite;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicParticipant;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class Pathway
 {
 	List<MetabolicReaction> reactions;
 	Set<String> chemicalNames;
     Map<String, Set<MetabolicReaction>> substrateIndex;
     Map<String, Set<MetabolicReaction>> productIndex;
 	Map<String, Set<MetabolicReaction>> substrateInchiIndex;
 	Map<String, Set<MetabolicReaction>> productInchiIndex;
     Set<String> inchis;
 
 	public Pathway(Collection<MetabolicReaction> someReactions)
 	{
 		reactions = new ArrayList<MetabolicReaction>(someReactions);
 		chemicalNames = new HashSet<String>();
         substrateIndex = new HashMap<String, Set<MetabolicReaction>>();
         productIndex = new HashMap<String, Set<MetabolicReaction>>();
 		substrateInchiIndex = new HashMap<String, Set<MetabolicReaction>>();
 		productInchiIndex = new HashMap<String, Set<MetabolicReaction>>();
         inchis = new HashSet<String>();
 		
 		Set<Metabolite> metabolites = new HashSet<Metabolite>();
 		for(MetabolicReaction r : reactions)
 		{
             for(MetabolicParticipant participant : r.getParticipants())
             {
 			    metabolites.add(participant.getMolecule());
             }
 			
 			for(MetabolicParticipant substrate : r.getReactants())
 			{
                 Metabolite metabolite = substrate.getMolecule();
 				chemicalNames.add(metabolite.getName());
 
                 String inchi = null;
                 if(metabolite.hasAnnotation(InChI.class))
                 {
                     for(InChI inchiAnnotation : metabolite.getAnnotations(InChI.class))
                     {
                         inchi = inchiAnnotation.toInChI();
                     }
                 }
 
                if(!substrateIndex.containsKey(metabolite.getIdentifier().toString()))
                 {
                    substrateIndex.put(metabolite.getIdentifier().toString(), new HashSet<MetabolicReaction>());
                 }
                substrateIndex.get(metabolite.getIdentifier().toString()).add(r);
 
                 if(!substrateInchiIndex.containsKey(inchi))
                 {
                     substrateInchiIndex.put(inchi, new HashSet<MetabolicReaction>());
                 }
                 substrateInchiIndex.get(inchi).add(r);
                 inchis.add(inchi);
 			}
 
             for(MetabolicParticipant product : r.getProducts())
             {
                 Metabolite metabolite = product.getMolecule();
                 chemicalNames.add(metabolite.getName());
 
                 String inchi = null;
                 if(metabolite.hasAnnotation(InChI.class))
                 {
                     for(InChI inchiAnnotation : metabolite.getAnnotations(InChI.class))
                     {
                         inchi = inchiAnnotation.toInChI();
                     }
                 }
 
                if(!productIndex.containsKey(metabolite.getIdentifier().toString()))
                 {
                    productIndex.put(metabolite.getIdentifier().toString(), new HashSet<MetabolicReaction>());
                 }
                productIndex.get(metabolite.getIdentifier().toString()).add(r);
 
                 if(!productInchiIndex.containsKey(inchi))
                 {
                     productInchiIndex.put(inchi, new HashSet<MetabolicReaction>());
                 }
                 productInchiIndex.get(inchi).add(r);
                 inchis.add(inchi);
             }
 		}
 	}
 	
 	public List<MetabolicReaction> getReactions()
 	{
 		return reactions;
 	}
 	
 	public Set<String> getChemicalNames()
 	{
 		return chemicalNames;
 	}
 
     public boolean containsMolecule(String inchi)
     {
         return inchis.contains(inchi);
     }
 
     public Set<MetabolicReaction> getReactionsContainingMolecule(String molID)
     {
         Set<MetabolicReaction> output = new HashSet<MetabolicReaction>();
         output.addAll(substrateIndex.get(molID));
         output.addAll(productIndex.get(molID));
         return output;
     }
 
     public List<Metabolite> getMetabolites(Set<String> currencyMolecules)
     {
         Set<Metabolite> metabolitesSet = new HashSet<Metabolite>();
 
         for(MetabolicReaction reaction : reactions)
         {
             for(MetabolicParticipant participant : reaction.getParticipants())
             {
                 Metabolite metabolite = participant.getMolecule();
                 boolean isCurrencyMolecule = false;
                 for(InChI inchi : metabolite.getAnnotations(InChI.class))
                 {
                     if(currencyMolecules.contains(inchi.toInChI()))
                     {
                         isCurrencyMolecule = true;
                     }
                 }
 
                 if(!isCurrencyMolecule)
                 {
                     metabolitesSet.add(metabolite);
                 }
             }
         }
 
         return new ArrayList<Metabolite>(metabolitesSet);
     }
 
     public Set<MetabolicReaction> reactionsContainingSubstrate(String inchi)
     {
         return substrateInchiIndex.get(inchi);
     }
 
     public Set<MetabolicReaction> reactionsContainingProduct(String inchi)
     {
         return productInchiIndex.get(inchi);
     }
 
     public Set<MetabolicReaction> reactionsContainingMolecule(String inchi)
     {
         Set<MetabolicReaction> output = new HashSet<MetabolicReaction>();
         output.addAll(reactionsContainingSubstrate(inchi));
         output.addAll(reactionsContainingProduct(inchi));
         return output;
     }
 	
 	public Set<String> constructQueries(String organismID) throws CHEBIException, FileNotFoundException
     {
 		QueryBuilder qb = new QueryBuilder();
 		return qb.build(organismID, inchis);
 	}
 	
 	public boolean containsPair(String substrateInchi, String productInchi)
 	{
 		if(!substrateInchiIndex.containsKey(substrateInchi)
 				|| !productInchiIndex.containsKey(productInchi))
 		{
 			return false;
 		}
 		Set<MetabolicReaction> substrateReactions = substrateInchiIndex.get(substrateInchi);
 		Set<MetabolicReaction> productReactions = productInchiIndex.get(productInchi);
 		
 		Set<MetabolicReaction> commonReactions = new HashSet<MetabolicReaction>(substrateReactions);
 		commonReactions.retainAll(productReactions);
 		
 		if(commonReactions.size() > 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
     public boolean containsAnyMolecule(MetabolicReaction reaction)
     {
         boolean found = false;
 
         for(MetabolicParticipant participant : reaction.getParticipants())
         {
             Metabolite m = participant.getMolecule();
             for(InChI inchi : m.getAnnotations(InChI.class))
             {
                 if(inchis.contains(inchi.toInChI()))
                 {
                     found = true;
                 }
             }
         }
 
         return found;
     }
 	
 	public boolean containsReaction(MetabolicReaction input)
 	{
 		Set<MetabolicReaction> substratesWith = new HashSet<MetabolicReaction>();
 		for(MetabolicParticipant substrate : input.getReactants())
 		{
             Metabolite metabolite = substrate.getMolecule();
             String inchi = null;
             if(metabolite.hasAnnotation(InChI.class))
             {
                 for(InChI inchiAnnotation : metabolite.getAnnotations(InChI.class))
                 {
                     inchi = inchiAnnotation.toInChI();
                 }
             }
 
 			if(substrateInchiIndex.containsKey(inchi))
 			{
 				substratesWith.addAll(substrateInchiIndex.get(inchi));
 			}
 		}
 
         Set<MetabolicReaction> productsWith = new HashSet<MetabolicReaction>();
         for(MetabolicParticipant product : input.getProducts())
         {
             Metabolite metabolite = product.getMolecule();
             String inchi = null;
             if(metabolite.hasAnnotation(InChI.class))
             {
                 for(InChI inchiAnnotation : metabolite.getAnnotations(InChI.class))
                 {
                     inchi = inchiAnnotation.toInChI();
                 }
             }
 
             if(productInchiIndex.containsKey(inchi))
             {
                 productsWith.addAll(productInchiIndex.get(inchi));
             }
         }
 		
 		substratesWith.retainAll(productsWith);
 		if(substratesWith.size() > 0)
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 }
