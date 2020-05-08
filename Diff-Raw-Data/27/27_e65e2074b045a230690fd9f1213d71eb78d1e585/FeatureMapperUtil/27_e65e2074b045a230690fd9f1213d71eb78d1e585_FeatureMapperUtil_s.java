 package org.genedb.crawl.modelling;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.biojava.bio.BioException;
 import org.biojava.bio.proteomics.IsoelectricPointCalc;
 import org.biojava.bio.proteomics.MassCalc;
 import org.biojava.bio.seq.ProteinTools;
 import org.biojava.bio.seq.io.SymbolTokenization;
 import org.biojava.bio.symbol.IllegalSymbolException;
 import org.biojava.bio.symbol.SimpleSymbolList;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojava.bio.symbol.SymbolPropertyTable;
 import org.genedb.crawl.mappers.FeatureMapper;
 import org.genedb.crawl.mappers.OrganismsMapper;
 import org.genedb.crawl.mappers.RegionsMapper;
 import org.genedb.crawl.model.Coordinates;
 import org.genedb.crawl.model.Cvterm;
 import org.genedb.crawl.model.Feature;
 import org.genedb.crawl.model.LocatedFeature;
 import org.genedb.crawl.model.Organism;
 import org.genedb.crawl.model.Property;
 import org.genedb.util.TranslationException;
 import org.genedb.util.Translator;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import uk.ac.sanger.artemis.sequence.Bases;
 
 /**
  * A class that does useful things with mappers, centering around features.
  * 
  * @author gv1
  * 
  */
 public class FeatureMapperUtil {
 
     private static Logger   logger = Logger.getLogger(FeatureMapperUtil.class);
 
     @Autowired
     private OrganismsMapper organismsMapper;
 
     @Autowired
     private RegionsMapper   regionsMapper;
 
     @Autowired
     public FeatureMapper    featureMapper;
     
     public Feature getFeature(String uniqueName, String name, String organism) {
         Integer organism_id = null;
 
         if (organism != null) {
             Organism o = this.getOrganism(organism);
             if (o != null)
                 organism_id = o.ID;
         }
 
         Feature resultFeature = featureMapper.get(uniqueName, name, organism_id);
 
         return resultFeature;
     }
 
     public Organism getOrganism(String organism) {
         Organism mappedOrganism = null;
 
         if (organism.contains(":")) {
             String[] split = organism.split(":");
 
             if (split.length == 2) {
 
                 String prefix = split[0];
                 String orgDescriptor = split[1];
 
                 if (prefix.equals("com")) {
                     mappedOrganism = organismsMapper.getByCommonName(orgDescriptor);
                 } else if (prefix.equals("tax")) {
                     mappedOrganism = organismsMapper.getByTaxonID(orgDescriptor);
                 } else if (prefix.equals("org")) {
                     mappedOrganism = organismsMapper.getByID(Integer.parseInt(orgDescriptor));
                 }
 
             }
 
         } else {
 
             mappedOrganism = organismsMapper.getByCommonName(organism);
 
         }
 
         return mappedOrganism;
     }
 
     private void summarise(Feature feature) {
 
         feature.coordinates = featureMapper.coordinates(feature);
 
         // TODO - this might need to be fixed to work with non-LocatedFeature
         // instances
         if (feature instanceof LocatedFeature && feature.coordinates != null && feature.coordinates.size() > 0) {
             LocatedFeature locatedFeature = (LocatedFeature) feature;
             Coordinates c = locatedFeature.coordinates.get(0);
             locatedFeature.fmin = c.fmin;
             locatedFeature.fmax = c.fmax;
             locatedFeature.region = c.region;
             locatedFeature.phase = c.phase;
             locatedFeature.strand = c.strand;
 
         }
 
         feature.properties = featureMapper.properties(feature);
         feature.terms = featureMapper.terms(feature);
         feature.synonyms = featureMapper.synonyms(feature);
         feature.pubs = featureMapper.pubs(feature);
         feature.dbxrefs = featureMapper.dbxrefs(feature);
         feature.domains = featureMapper.domains(feature);
         feature.orthologues = featureMapper.orthologues(feature);
 
     }
 
     public Feature getAncestorGene(Feature currentFeature, List<Cvterm> ofType) {
 
         if (currentFeature.type.name.equals("gene") || currentFeature.type.name.equals("pseudogene"))
             return currentFeature;
 
         List<Feature> parents = featureMapper.parents(currentFeature, ofType);
 
         for (Feature parent : parents) {
             // parents are objects
             Feature root = getAncestorGene(parent, ofType);
 
             if (root != null) {
                 return root;
             }
 
         }
 
         return null;
     }
 
     public void getDescendants(Feature feature, List<Cvterm> ofType, boolean includeSummaries) {
 
         feature.children = featureMapper.children(feature, ofType);
         if (includeSummaries)
             summarise(feature);
 
         if (feature.children == null)
             return;
 
         for (Feature child : feature.children) {
             // children are subjects
             getDescendants(child, ofType, includeSummaries);
         }
 
     }
 
     public static Feature getTranscript(Feature requested, Feature hierarchyFeature) {
 
         Feature firstTranscript = null;
 
         for (Feature child : hierarchyFeature.children) {
             if (child.type.name.equals("mRNA")) {
                 if (requested.uniqueName.equals(hierarchyFeature.uniqueName) || requested.uniqueName.equals(child.uniqueName)) {
                     return child;
                 }
 
                 if (firstTranscript == null)
                     firstTranscript = child;
 
                 for (Feature grandChild : child.children) {
                     String grandChildType = grandChild.type.name;
                     if (requested.type.name.equals(grandChildType)) {
                         if (grandChildType.equals("polypeptide")) {
                             return child;
                         } else if (grandChildType.equals("exon")) {
                             return child;
                         }
                     }
                 }
             }
         }
 
         return firstTranscript;
     }
 
     public List<Feature> getExons(Feature transcript) {
         List<Feature> exons = new ArrayList<Feature>();
         for (Feature potential_exon : transcript.children) {
             if (potential_exon.type.name.equals("exon")) {
                 logger.info("exon " + potential_exon.uniqueName);
                 potential_exon.coordinates = featureMapper.coordinates(potential_exon);
                 exons.add(potential_exon);
             }
         }
         return exons;
     }
 
     public String getExonSequence(List<Feature> exons) {
         StringBuffer sequence = new StringBuffer();
         for (Feature exon : exons) {
             Coordinates coordinates = exon.coordinates.get(0);
             String dnaString = regionsMapper.sequenceTrimmed(coordinates.region, coordinates.fmin, coordinates.fmax).dna;
             
             // if (coordinates.strand < 0)
             //                 dnaString = Bases.reverseComplement(dnaString);
             
             logger.info(String.format(">%s (%s) %d-%d \n%s", exon.uniqueName, coordinates.region, coordinates.fmin, coordinates.fmax, dnaString));
             
             sequence.append(dnaString);
         }
         return sequence.toString();
     }

     public List<Property> getPolypeptideProperties(Feature feature, Feature hierarchyFeature) throws NumberFormatException, BioException, TranslationException {
         Feature transcript = getTranscript(feature, hierarchyFeature);
         if (transcript == null)
             throw new RuntimeException("Could not find transcript");
 
         List<Feature> exons = getExons(transcript);
         
         Feature firstExon = exons.get(0);
         if (firstExon == null)
             throw new RuntimeException("Could not find exon");
         
         String exonSequence = getExonSequence(exons);
         logger.info(String.format("transcript : %s, exonSequence : %s", transcript.uniqueName, exonSequence));
         
         Coordinates firstExonCoordinate = firstExon.coordinates.get(0);
         
         boolean isFwd = (firstExonCoordinate.strand > 0) ? true : false;
         if (! isFwd) {
             logger.info("reversing");
             exonSequence = Bases.reverseComplement(exonSequence);
             logger.info(String.format("reversed transcript : %s, exonSequence : %s", transcript.uniqueName, exonSequence));
         }
             
         String phase = firstExonCoordinate.phase;
         if (phase == null)
             phase = "0";
         
         Organism o = organismsMapper.getByID(feature.organism_id);
 
         Property translationTableProp = organismsMapper.getOrganismProp(o, "genedb_misc", "translationTable");
         int translationTable = Integer.parseInt(translationTableProp.value);
 
         return getPolypeptideProperties(exonSequence, Integer.parseInt(phase), translationTable);
     }
 
     public static List<Property> getPolypeptideProperties(String dnaString, int phase, int translationTable) throws BioException, TranslationException {
 
         List<Property> properties = new ArrayList<Property>();
 
         // Coordinates coordinates = feature.coordinates.get(0);
         // int phase = (coordinates.phase != null) ?
         // Integer.parseInt(coordinates.phase) : 0;
 
         // String dnaString = regionsMapper.sequenceTrimmed(coordinates.region,
         // coordinates.fmin, coordinates.fmax).dna;
 
         String residuesString = translate(translationTable, dnaString, phase, false);
         
         logger.info(residuesString);
 
         SymbolTokenization proteinTokenization = ProteinTools.getTAlphabet().getTokenization("token");
         SymbolList residuesSymbolList = null;
         residuesSymbolList = new SimpleSymbolList(proteinTokenization, residuesString);
         
         logger.info(residuesSymbolList);
 
         if (residuesSymbolList.length() == 0) {
             throw new RuntimeException(String.format("Polypeptide feature has zero-length residues"));
             // return properties;
         }
 
         // if the sequence ends with a termination symbol (*), we need to
         // remove it
         if (residuesSymbolList.symbolAt(residuesSymbolList.length()) == ProteinTools.ter()) {
             if (residuesSymbolList.length() == 1) {
                 throw new RuntimeException(String.format("Polypeptide feature only has termination symbol"));
                 // return properties;
             }
             residuesSymbolList = residuesSymbolList.subList(1, residuesSymbolList.length() - 1);
         }
 
         Property aminoProp = new Property();
         aminoProp.name = "Amino Acids";
         aminoProp.value = String.valueOf(residuesSymbolList.length());
         properties.add(aminoProp);
 
         double isoElectricPoint = new IsoelectricPointCalc().getPI(residuesSymbolList, false, false);
 
         Property isoProp = new Property();
         isoProp.name = "Isoelectric Point";
         isoProp.value = "pH " + String.valueOf(isoElectricPoint);
         properties.add(isoProp);
 
         double mass2 = calculateMass(residuesSymbolList);
 
         Property massProp = new Property();
         massProp.name = "Mass";
         massProp.value = String.valueOf(mass2 / 1000) + " kDa";
         properties.add(massProp);
 
         double charge = calculateCharge(residuesString);
 
         Property chargeProp = new Property();
         chargeProp.name = "Charge";
         chargeProp.value = String.valueOf(charge);
         properties.add(chargeProp);
 
         return properties;
     }
 
     public static String translate(int translationTableId, String dnaSequence, int phase, boolean stopCodonTranslatedAsSelenocysteine) throws TranslationException {
         return Translator.getTranslator(translationTableId).translate(dnaSequence, phase, stopCodonTranslatedAsSelenocysteine);
     }
 
     public static double calculateMass(SymbolList residuesSymbolList) throws IllegalSymbolException {
 
         double massInDaltons = MassCalc.getMass(residuesSymbolList, SymbolPropertyTable.AVG_MASS, true);
         return massInDaltons;
 
     }
 
     /**
      * Calculate the charge of a polypeptide.
      * 
      * @param residues
      *            a string representing the polypeptide residues, using the
      *            single-character code
      * @return the charge of this polypeptide (in what units?)
      */
     public static double calculateCharge(String residues) {
         double charge = 0.0;
         for (char aminoAcid : residues.toCharArray()) {
             switch (aminoAcid) {
                 case 'B':
                 case 'Z':
                     charge += -0.5;
                     break;
                 case 'D':
                 case 'E':
                     charge += -1.0;
                     break;
                 case 'H':
                     charge += 0.5;
                     break;
                 case 'K':
                 case 'R':
                     charge += 1.0;
                     break;
                 /*
                  * EMBOSS seems to think that 'O' (presumably Pyrrolysine) also
                  * contributes +1 to the charge. According to Wikipedia, this
                  * obscure amino acid is found only in methanogenic archaea, so
                  * it's unlikely to trouble us soon. Still, it can't hurt:
                  */
                 case 'O':
                     charge += 1.0;
                     break;
             }
         }
         return charge;
     }
 
 }
