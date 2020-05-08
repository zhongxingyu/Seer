 package uk.ac.bbk.REx.utils;
 
 import uk.ac.ebi.mdk.domain.annotation.InChI;
 import uk.ac.ebi.mdk.domain.annotation.rex.RExCompound;
 import uk.ac.ebi.mdk.domain.entity.Metabolite;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicParticipant;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 
 import java.util.*;
 
 public class Evaluator
 {
     public static Results evaluateResults(
             Collection<MetabolicReaction> reactions,
             Collection<MetabolicReaction> expectedReactions,
             Set<String> currencyMols)
     {
         Map<String, Set<MetabolicReaction>> substrateIndex = new HashMap<String, Set<MetabolicReaction>>();
         Map<String, Set<MetabolicReaction>> productIndex = new HashMap<String, Set<MetabolicReaction>>();
 
         Map<String, Set<MetabolicReaction>> substrateCutOffIndex = new HashMap<String, Set<MetabolicReaction>>();
         Map<String, Set<MetabolicReaction>> productCutOffIndex = new HashMap<String, Set<MetabolicReaction>>();
         Set<MetabolicReaction> cutOffReactions = new HashSet<MetabolicReaction>();
 
         int totalReactionsAboveCutoff = 0;
 
         for(MetabolicReaction r : reactions)
         {
             Map<String, Double> substrateExtractionScores = new HashMap<String, Double>();
             Map<String, Double> productExtractionScores = new HashMap<String, Double>();
             Map<String, Double> substrateRelevanceScores = new HashMap<String, Double>();
             Map<String, Double> productRelevanceScores = new HashMap<String, Double>();
 
             for(RExCompound compound : r.getAnnotations(RExCompound.class))
             {
                 if(compound.getType() == RExCompound.Type.SUBSTRATE)
                 {
                     substrateExtractionScores.put(compound.getID(), compound.getExtraction());
                     substrateRelevanceScores.put(compound.getID(), compound.getRelevance());
                 }
                 else if(compound.getType() == RExCompound.Type.PRODUCT)
                 {
                     productExtractionScores.put(compound.getID(), compound.getExtraction());
                     productRelevanceScores.put(compound.getID(), compound.getRelevance());
                 }
             }
 
             boolean isAboveCutOff = false;
 
             for(MetabolicParticipant substrate : r.getReactants())
             {
                 Metabolite m = substrate.getMolecule();
                 String id = m.getIdentifier().toString();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!substrateIndex.containsKey(inchi.toInChI()))
                     {
                         substrateIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                     }
                     substrateIndex.get(inchi.toInChI()).add(r);
 
                     if(substrateExtractionScores.get(id) >= 5.0
                             && substrateRelevanceScores.get(id) > 0.0)
                     {
                         isAboveCutOff = true;
                         if(!substrateCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             substrateCutOffIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                         }
                         substrateCutOffIndex.get(inchi.toInChI()).add(r);
                         cutOffReactions.add(r);
                     }
                 }
             }
 
             for(MetabolicParticipant product : r.getProducts())
             {
                 Metabolite m = product.getMolecule();
                 String id = m.getIdentifier().toString();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!productIndex.containsKey(inchi.toInChI()))
                     {
                         productIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                     }
                     productIndex.get(inchi.toInChI()).add(r);
 
                     if(productExtractionScores.get(id) >= 5.0
                             && productRelevanceScores.get(id) > 0.0)
                     {
                         isAboveCutOff = true;
                         if(!productCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             productCutOffIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                         }
                         productCutOffIndex.get(inchi.toInChI()).add(r);
                         cutOffReactions.add(r);
                     }
                 }
             }
 
             if(isAboveCutOff)
             {
                 totalReactionsAboveCutoff++;
             }
         }
 
         Map<String, Set<MetabolicReaction>> expectedSubstrateIndex = new HashMap<String, Set<MetabolicReaction>>();
         Map<String, Set<MetabolicReaction>> expectedProductIndex = new HashMap<String, Set<MetabolicReaction>>();
         for(MetabolicReaction expectedReaction : expectedReactions)
         {
             for(MetabolicParticipant substrate : expectedReaction.getReactants())
             {
                 Metabolite m = substrate.getMolecule();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!expectedSubstrateIndex.containsKey(inchi.toInChI()))
                     {
                         expectedSubstrateIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                     }
 
                     expectedSubstrateIndex.get(inchi.toInChI()).add(expectedReaction);
                 }
             }
 
             for(MetabolicParticipant product : expectedReaction.getProducts())
             {
                 Metabolite m = product.getMolecule();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!expectedProductIndex.containsKey(inchi.toInChI()))
                     {
                         expectedProductIndex.put(inchi.toInChI(), new HashSet<MetabolicReaction>());
                     }
 
                     expectedProductIndex.get(inchi.toInChI()).add(expectedReaction);
                 }
             }
         }
 
         int foundReactions = 0;
         List<MetabolicReaction> expectedReactionsFound = new ArrayList<MetabolicReaction>();
         List<MetabolicReaction> foundReactionsExpected = new ArrayList<MetabolicReaction>();
         for(MetabolicReaction expectedReaction : expectedReactions)
         {
             //Determine if this is a terminal reaction
             List<MetabolicParticipant> substrates = expectedReaction.getReactants();
             List<MetabolicParticipant> products = expectedReaction.getProducts();
 
             int substratesNotInOtherReactions = 0;
             for(MetabolicParticipant substrate : expectedReaction.getReactants())
             {
                 Metabolite m = substrate.getMolecule();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!expectedProductIndex.containsKey(inchi.toInChI()))
                     {
                         substratesNotInOtherReactions++;
                         break;
                     }
                 }
             }
 
             int productsNotInOtherReactions = 0;
             for(MetabolicParticipant product : expectedReaction.getProducts())
             {
                 Metabolite m = product.getMolecule();
                 for(InChI inchi : m.getAnnotations(InChI.class))
                 {
                     if(!expectedSubstrateIndex.containsKey(inchi.toInChI()))
                     {
                         productsNotInOtherReactions++;
                         break;
                     }
                 }
             }
 
             boolean startTerminus = false;
             if(substrates.size()-substratesNotInOtherReactions == 0)
             {
                 startTerminus = true;
             }
 
             boolean endTerminus = false;
             if(products.size()-productsNotInOtherReactions == 0)
             {
                 endTerminus = true;
             }
 
             if(startTerminus && endTerminus)
             {
                START:
                 for(MetabolicParticipant substrate : expectedReaction.getReactants())
                 {
                     Metabolite sm = substrate.getMolecule();
                     for(InChI sinchi : sm.getAnnotations(InChI.class))
                     {
                         if(!currencyMols.contains(sinchi.toInChI())
                                 && substrateCutOffIndex.containsKey(sinchi.toInChI()))
                         {
                             for(MetabolicParticipant product : expectedReaction.getProducts())
                             {
                                 Metabolite pm = product.getMolecule();
                                 for(InChI pinchi : pm.getAnnotations(InChI.class))
                                 {
                                     if(!currencyMols.contains(pinchi.toInChI())
                                             && productCutOffIndex.containsKey(pinchi.toInChI()))
                                     {
                                         Set<MetabolicReaction> commonReactions = substrateCutOffIndex.get(sinchi.toInChI());
                                         commonReactions.retainAll(productCutOffIndex.get(pinchi.toInChI()));
 
                                         if(!commonReactions.isEmpty())
                                         {
                                             foundReactions++;
                                             foundReactionsExpected.addAll(commonReactions);
                                             expectedReactionsFound.add(expectedReaction);
                                             break START;
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             else if(startTerminus && !endTerminus)
             {
                 for(MetabolicParticipant substrate : expectedReaction.getReactants())
                 {
                     Metabolite sm = substrate.getMolecule();
                     Set<MetabolicReaction> commonReactions = new HashSet<MetabolicReaction>();
                     for(InChI inchi : sm.getAnnotations(InChI.class))
                     {
                         if(!currencyMols.contains(inchi.toInChI())
                                 && substrateCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             commonReactions.addAll(substrateCutOffIndex.get(inchi.toInChI()));
                         }
                     }
 
                     for(MetabolicParticipant product : expectedReaction.getProducts())
                     {
                         Metabolite pm = product.getMolecule();
                         for(InChI inchi : pm.getAnnotations(InChI.class))
                         {
                             if(!currencyMols.contains(inchi.toInChI())
                                     && expectedSubstrateIndex.containsKey(inchi.toInChI())
                                     && productCutOffIndex.containsKey(inchi.toInChI()))
                             {
                                 Set<MetabolicReaction> productReactions = productCutOffIndex.get(inchi.toInChI());
                                 commonReactions.retainAll(productReactions);
                             }
                         }
                     }
 
                     if(!commonReactions.isEmpty())
                     {
                         foundReactions++;
                         foundReactionsExpected.addAll(commonReactions);
                         expectedReactionsFound.add(expectedReaction);
                        break;
                     }
                 }
             }
             else if(!startTerminus && endTerminus)
             {
                 for(MetabolicParticipant product : expectedReaction.getProducts())
                 {
                     Metabolite pm = product.getMolecule();
                     Set<MetabolicReaction> commonReactions = new HashSet<MetabolicReaction>();
                     for(InChI inchi : pm.getAnnotations(InChI.class))
                     {
                         if(!currencyMols.contains(inchi.toInChI())
                                 && productCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             commonReactions.addAll(productCutOffIndex.get(inchi.toInChI()));
                         }
                     }
 
                     for(MetabolicParticipant substrate : expectedReaction.getReactants())
                     {
                         Metabolite sm = substrate.getMolecule();
                         for(InChI inchi : sm.getAnnotations(InChI.class))
                         {
                             if(!currencyMols.contains(inchi.toInChI())
                                     && expectedProductIndex.containsKey(inchi.toInChI())
                                     && substrateCutOffIndex.containsKey(inchi.toInChI()))
                             {
                                 Set<MetabolicReaction> substrateReactions = substrateCutOffIndex.get(inchi.toInChI());
                                 commonReactions.retainAll(substrateReactions);
                             }
                         }
                     }
 
                     if(!commonReactions.isEmpty())
                     {
                         foundReactions++;
                         foundReactionsExpected.addAll(commonReactions);
                         expectedReactionsFound.add(expectedReaction);
                        break;
                     }
                 }
             }
             else
             {
                 Set<MetabolicReaction> commonReactions = new HashSet<MetabolicReaction>();
                 boolean firstRun = true;
                 for(MetabolicParticipant substrate : expectedReaction.getReactants())
                 {
                     Metabolite m = substrate.getMolecule();
                     for(InChI inchi : m.getAnnotations(InChI.class))
                     {
                         if(!currencyMols.contains(inchi.toInChI())
                                 && expectedProductIndex.containsKey(inchi.toInChI())
                                 && substrateCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             Set<MetabolicReaction> substrateReactions = substrateCutOffIndex.get(inchi.toInChI());
 
                             if(firstRun)
                             {
                                 commonReactions.addAll(substrateReactions);
                                 firstRun = false;
                             }
                             else
                             {
                                 commonReactions.retainAll(substrateReactions);
                             }
                         }
                     }
                 }
 
                 for(MetabolicParticipant product : expectedReaction.getProducts())
                 {
                     Metabolite m = product.getMolecule();
                     for(InChI inchi : m.getAnnotations(InChI.class))
                     {
                         if(!currencyMols.contains(inchi.toInChI())
                                 && expectedSubstrateIndex.containsKey(inchi.toInChI())
                                 && productCutOffIndex.containsKey(inchi.toInChI()))
                         {
                             Set<MetabolicReaction> productReactions = productCutOffIndex.get(inchi.toInChI());
                             commonReactions.retainAll(productReactions);
                         }
                     }
                 }
 
                 if(!commonReactions.isEmpty())
                 {
                     foundReactions++;
                     foundReactionsExpected.addAll(commonReactions);
                     expectedReactionsFound.add(expectedReaction);
                 }
             }
         }
 
         Set<MetabolicReaction> reactionsExpectedButNotFound = new HashSet<MetabolicReaction>(expectedReactions);
         reactionsExpectedButNotFound.removeAll(expectedReactionsFound);
 
         Set<MetabolicReaction> reactionsFoundButNotExpected = new HashSet<MetabolicReaction>(cutOffReactions);
         reactionsFoundButNotExpected.removeAll(foundReactionsExpected);
 
         double recall = (double)foundReactions/(double)expectedReactions.size();
         double precision = (double)foundReactions/(double)totalReactionsAboveCutoff;
         return new Results(
                 recall,
                 precision,
                 foundReactionsExpected,
                 new ArrayList<MetabolicReaction>(reactionsExpectedButNotFound),
                 new ArrayList<MetabolicReaction>(reactionsFoundButNotExpected));
     }
 }
