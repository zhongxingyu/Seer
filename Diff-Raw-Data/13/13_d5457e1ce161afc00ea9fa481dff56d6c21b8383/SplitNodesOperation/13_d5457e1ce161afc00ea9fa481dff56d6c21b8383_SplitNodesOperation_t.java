 package org.iucn.sis.server.api.io.taxomatic;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.io.TaxomaticIO;
 import org.iucn.sis.server.api.utils.TaxomaticException;
 import org.iucn.sis.shared.api.models.Synonym;
 import org.iucn.sis.shared.api.models.TaxomaticOperation;
 import org.iucn.sis.shared.api.models.Taxon;
 import org.iucn.sis.shared.api.models.TaxonLevel;
 import org.iucn.sis.shared.api.models.TaxonStatus;
 import org.iucn.sis.shared.api.models.User;
 
 /**
  * For split the taxomatic is designed so that you select the node (Old1)
  * that you would like to split. You are then prompted to create new taxa
  * which you would like to split the node into (at least 2, lets say New1,
  * New2). For each child of Old1, you can choose to place it under either
  * New1 or in New2. Old1 is deprecated, and has two new Synonyms linking to
 * New1 and New2. New1 and New2 receive the children that were selected for
  * them, and they both receive a deprecated synonym to Old1. None of the
  * assessments are transferred from Old1.
  * 
  * This operation removes all children of the oldTaxon and places them into 
 * the the new Parent which is stored in the parentToChildren map
  * 
  * @author adam.schwartz
  * @author carl.scott
  */
 public class SplitNodesOperation extends TaxomaticWorker {
 	
 	private final TaxomaticOperation operation;
 	private final Taxon original;
 	private final Map<Taxon, List<Taxon>> parentToChildren;
 	
 	public SplitNodesOperation(Session session, TaxomaticOperation operation) {
 		super(session, TaxomaticIO.SPLIT);
 		this.operation = operation;
 		this.original = loadOriginal(operation.getInstructions());
 		this.parentToChildren = loadParentToChildren(operation.getInstructions());
 	}
 	
 	public SplitNodesOperation(Session session, User user, Taxon original, Map<Taxon, List<Taxon>> parentToChildren) {
 		super(session, TaxomaticIO.SPLIT);
 		TaxomaticOperation operation = new TaxomaticOperation();
 		operation.setDate(new Date());
 		operation.setInstructions(generateInstructions(original, parentToChildren));
 		operation.setDetails(generateDetails(original, parentToChildren));
 		operation.setOperation(TaxomaticIO.SPLIT);
 		operation.setUser(user);
 		
 		this.operation = operation;
 		this.original = original;
 		this.parentToChildren = parentToChildren;
 	}
 	
 	@Override
 	public String getUndoOperationName() {
 		return TaxomaticIO.MERGE;
 	}
 	
 	private String generateDetails(Taxon original,  Map<Taxon, List<Taxon>> parentToChildren) {
 		StringBuilder builder = new StringBuilder();
 		builder.append("Split children of " + original.getFullName() + " into new nodes: ");
 		for (Iterator<Map.Entry<Taxon, List<Taxon>>> iter = parentToChildren.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry<Taxon, List<Taxon>> entry = iter.next();
 			
 			builder.append("New Parent " + entry.getKey().getFullName() + " has children ");
 			
 			for (Iterator<Taxon> iterator = entry.getValue().iterator(); iterator.hasNext(); ) {
 				builder.append(iterator.next().getFullName() + (iterator.hasNext() ? "," : ""));
 			}
 			if (iter.hasNext())
 				builder.append("; ");
 		}
 		return builder.toString();
 	}
 	
 	/*
 	 * original|parent:child,child|parent:child,child
 	 */
 	private String generateInstructions(Taxon original, Map<Taxon, List<Taxon>> parentToChildren) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(original.getId());
 		builder.append('|');
 		for (Iterator<Map.Entry<Taxon, List<Taxon>>> iter = parentToChildren.entrySet().iterator(); iter.hasNext(); ) {
 			Map.Entry<Taxon, List<Taxon>> entry = iter.next();
 			builder.append(entry.getKey().getId());
 			builder.append(":");
 			for (Iterator<Taxon> iterator = entry.getValue().iterator(); iterator.hasNext(); )
 				builder.append(iterator.next().getId() + (iterator.hasNext() ? "," : ""));
 			if (iter.hasNext())
 				builder.append('|');
 		}
 		return builder.toString();
 	}
 	
 	private Taxon loadOriginal(String instructions) {
 		return taxonIO.getTaxon(Integer.valueOf(instructions.split("|")[0]));
 	}
 	
 	private Map<Taxon, List<Taxon>> loadParentToChildren(String instructions) {
 		Map<Taxon, List<Taxon>> map = new HashMap<Taxon, List<Taxon>>();
 		String[] split = instructions.split("|");
 		for (int i = 1; i < split.length; i++) {
 			String[] mapSplit = split[i].split(":");
 			Taxon parent = taxonIO.getTaxon(Integer.valueOf(mapSplit[0]));
 			List<Taxon> children = new ArrayList<Taxon>();
 			for (String id : mapSplit[1].split(","))
 				children.add(taxonIO.getTaxon(Integer.valueOf(id)));
 			
 			map.put(parent, children);
 		}
 		
 		return map;
 	}
 
 	public void run() throws TaxomaticException {
 		Set<Taxon> taxaToSave = new HashSet<Taxon>();
 		taxaToSave.add(original);
 		
 		if (original.getLevel() >= TaxonLevel.SPECIES)
 			original.setStatus(TaxonStatus.STATUS_SYNONYM);
 
 		for (Entry<Taxon, List<Taxon>> entry : parentToChildren.entrySet()) {
 			Taxon parent = entry.getKey();
			
 			Synonym synonym = Taxon.synonymizeTaxon(original);
 			synonym.setStatus(Synonym.SPLIT);
 			if (synonym.getSpeciesAuthor() != null)
 				synonym.setSpeciesAuthor(synonym.getSpeciesAuthor() + " <i>pro parte</i>");
			
			//Create relationships
			synonym.setTaxon(parent);
 			parent.getSynonyms().add(synonym);
			
 			taxaToSave.add(parent);
 
 			// MOVE EACH CHILD AND ADD SYNONYMS
 			for (Taxon child : entry.getValue()) {
 				taxaToSave.addAll(taxomaticIO.moveTaxon(parent, child, operation.getUser(), Synonym.SPLIT, false));
 			}
 		}
 
 		taxomaticIO.updateAndSave(taxaToSave, operation.getUser(), true);
 		
 		recordOperationHistory(taxaToSave, operation);
 	}
 	
 	public void undo() throws TaxomaticException {
 		throw new TaxomaticException("This operation can not be undone.  Please manually perform the appropriate " + "" + " operation.");
 	}
 
 }
