 /*
  *     This file is part of Metabolic Network Builder
  *
  *     Metabolic Network Builder is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Foobar is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.resource;
 
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectOutput;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.core.IdentifierSet;
 import uk.ac.ebi.interfaces.identifiers.Identifier;
 import uk.ac.ebi.interfaces.identifiers.ProteinIdentifier;
 import uk.ac.ebi.interfaces.identifiers.SequenceIdentifier;
 import uk.ac.ebi.metabolomes.identifier.AbstractIdentifier;
 import uk.ac.ebi.metabolomes.identifier.InChI;
 import uk.ac.ebi.metabolomes.identifier.MIRIAMEntry;
 import uk.ac.ebi.metabolomes.resource.Resource;
 import uk.ac.ebi.resource.chemical.BasicChemicalIdentifier;
 import uk.ac.ebi.resource.chemical.ChEBIIdentifier;
 import uk.ac.ebi.resource.chemical.DrugBankIdentifier;
 import uk.ac.ebi.resource.chemical.HMDBIdentifier;
 import uk.ac.ebi.resource.chemical.KEGGCompoundIdentifier;
 import uk.ac.ebi.resource.classification.ECNumber;
 import uk.ac.ebi.resource.classification.GeneOntologyAnnotation;
 import uk.ac.ebi.resource.classification.InterPro;
 import uk.ac.ebi.resource.gene.BasicGeneIdentifier;
 import uk.ac.ebi.resource.gene.ChromosomeIdentifier;
 import uk.ac.ebi.resource.organism.Taxonomy;
 import uk.ac.ebi.resource.protein.BasicProteinIdentifier;
 import uk.ac.ebi.resource.protein.SwissProtIdentifier;
 import uk.ac.ebi.resource.protein.TrEMBLIdentifier;
 import uk.ac.ebi.resource.reaction.BasicReactionIdentifier;
 import uk.ac.ebi.resource.rna.BasicRNAIdentifier;
 import uk.ac.ebi.resource.structure.HSSPIdentifier;
 import uk.ac.ebi.resource.structure.PDBIdentifier;
 
 /**
  * IdentifierFactory.java
  * Factory for identifiers
  *
  * @author johnmay
  * @date May 6, 2011
  */
 public class IdentifierFactory {
 
     private static final Logger logger = Logger.getLogger(IdentifierFactory.class);
     private static final String IDENTIFIER_MAPPING_FILE = "IdentifierResourceMapping.properties";
     private static final Identifier[] identifiers = new Identifier[Byte.MAX_VALUE];
     private List<Identifier> supportedIdentifiers = new ArrayList<Identifier>(Arrays.asList(
             new ChEBIIdentifier(),
             new KEGGCompoundIdentifier(),
             new TrEMBLIdentifier(),
             new SwissProtIdentifier(),
             new Taxonomy(),
             new ECNumber(),
             new BasicChemicalIdentifier(),
             new BasicReactionIdentifier(),
             new BasicGeneIdentifier(),
             new BasicRNAIdentifier(),
             new BasicProteinIdentifier(),
             new ReconstructionIdentifier(),
             new ChromosomeIdentifier(),
             new TaskIdentifier(),
             new DrugBankIdentifier(),
             new HMDBIdentifier(),
             new InterPro(),
             new GeneOntologyAnnotation(),
             new HSSPIdentifier(),
             new PDBIdentifier(),
             new InChI()));
     private Map<String, Identifier> synonyms = new HashMap();
     private List<SequenceIdentifier> proteinIdentifiers = new ArrayList(Arrays.asList(new BasicProteinIdentifier(),
                                                                                       new TrEMBLIdentifier(),
                                                                                       new SwissProtIdentifier()));
     private Map<String, SequenceIdentifier> proteinIdMap = new HashMap();
     private List<String> synonymExclusions = Arrays.asList("uniprotkb");
 
     public List<Identifier> getSupportedIdentifiers() {
         return supportedIdentifiers;
     }
 
     private IdentifierFactory() {
 
         for (Identifier identifier : supportedIdentifiers) {
             identifiers[identifier.getIndex()] = identifier;
         }
 
         for (Identifier identifier : supportedIdentifiers) {
 
             synonyms.put(identifier.getShortDescription().toLowerCase(Locale.ENGLISH), identifier);

             for (String synonym : identifier.getDatabaseSynonyms()) {
 
                 String key = synonym.toLowerCase(Locale.ENGLISH);
 
                 if (synonymExclusions.contains(key) == Boolean.FALSE) {
 
                     if (synonyms.containsKey(key)) {
                         logger.warn("Clashing synonym names in map: " + key + " appears more then once");
                     }
 
                     synonyms.put(key, identifier);
 
                 }
 
             }
         }
 
 
         for (SequenceIdentifier id : proteinIdentifiers) {
             proteinIdMap.put(id.getHeaderCode(), id);
         }
 
     }
 
     public static class IdentifierFactoryHolder {
 
         public static IdentifierFactory INSTANCE = new IdentifierFactory();
     }
 
     public static IdentifierFactory getInstance() {
         return IdentifierFactoryHolder.INSTANCE;
     }
 
     /**
      * Resolves a sequence header e.g. sp|Q38483|EKFF_EKH to one of our identifiers
      */
     public IdentifierSet resolveSequenceHeader(String header) {
 
         IdentifierSet idSet = new IdentifierSet();
         LinkedList<String> tokens = new LinkedList(Arrays.asList(header.split("\\|")));
 
         while (tokens.size() > 0) {
             String key = tokens.get(0);
             if (proteinIdMap.containsKey(key)) {
                 ProteinIdentifier id = (ProteinIdentifier) proteinIdMap.get(key).newInstance();
                 tokens = id.resolve(tokens);
                 idSet.add(id);
             } else {
                 tokens.removeFirst();
             }
         }
 
         return idSet;
 
     }
 
     /**
      * Builds an identifier given the accession
      * Uses the identifier parse method to validate ids (slower)
      * @param resource
      * @param accession
      * @deprecated do not use
      */
     @Deprecated
     public static AbstractIdentifier getIdentifier(Resource resource, String accession) {
 
         Constructor constructor = resource.getIdentifierConstructor();
         if (constructor != null) {
             try {
                 return (AbstractIdentifier) constructor.newInstance(accession, true);
             } catch (InstantiationException ex) {
                 ex.printStackTrace();
             } catch (IllegalAccessException ex) {
                 ex.printStackTrace();
             } catch (IllegalArgumentException ex) {
                 ex.printStackTrace();
             } catch (InvocationTargetException ex) {
                 ex.printStackTrace();
             }
         }
         return new BasicProteinIdentifier(accession);
     }
 
     /*     
      * @param resource
      * @param accession
      * @return
      * @deprecated do not use
      */
     @Deprecated
     public static AbstractIdentifier getUncheckedIdentifier(Resource resource, String accession) {
 
         Constructor constructor = resource.getIdentifierConstructor();
         if (constructor != null) {
             try {
                 return (AbstractIdentifier) constructor.newInstance(accession, false);
             } catch (InstantiationException ex) {
                 ex.printStackTrace();
             } catch (IllegalAccessException ex) {
                 ex.printStackTrace();
             } catch (IllegalArgumentException ex) {
                 ex.printStackTrace();
             } catch (InvocationTargetException ex) {
                 ex.printStackTrace();
             }
         }
         return new BasicProteinIdentifier(accession);
     }
 
     /**
      * Builds a list of identifiers from a string that may
      * or maynot contain multiple identifiers
      * atm: handle gi|39327|sp|398339 etc..
      * Use resolveSequenceHeader
      * @param idsString
      * @return
      */
     @Deprecated
     public static List<AbstractIdentifier> getIdentifiers(String idsString) {
 
         List<AbstractIdentifier> hitIdentifiers = new ArrayList<AbstractIdentifier>();
 
         if (idsString.contains(ID_SEPERATOR)) {
 
             ListIterator<String> it = Arrays.asList(idsString.split(ID_ESCAPED_SEPERATOR)).
                     listIterator();
 
             // db identifiers , gi,sp,tr etc..
             while (it.hasNext()) {
 
                 String dbid = it.next();
 
                 if (dbid.length() <= DBID_MAX_LENGTH) {
                     Resource r = Resource.getResource(dbid);
 
                     if (r != Resource.UNKNOWN) {
                         hitIdentifiers.add(IdentifierFactory.getIdentifier(r, it.next()));
                     } else if (it.hasNext()) {
                         dbid = it.next();
                         r = Resource.getResource(dbid);
                         if (r != Resource.UNIPROT) {
                             hitIdentifiers.add(IdentifierFactory.getIdentifier(r, it.next()));
                         } else {
                             it.previous();
                         }
                     }
 
                 } else {
 
                     hitIdentifiers.add(new BasicProteinIdentifier(dbid));
                 }
             }
         } else {
             hitIdentifiers.add(new BasicProteinIdentifier(idsString));
         }
 
         return hitIdentifiers;
     }
 
     /**
      *
      * Returns and identifier
      *
      * @param <T>
      * @param type
      * @return
      */
     public Identifier ofClass(Class type) {
         return ofIndex(IdentifierLoader.getInstance().getIndex(type));
     }
 
     /**
      * Main factory method. this returns a new identifier of the given index. The indicies are specified in the
      * IdentifierDescription.propertiers file (see. src/main/resources)
      * @param index
      * @return
      */
     public Identifier ofIndex(Byte index) {
         return identifiers[index].newInstance();
     }
 
     /**
      * Create an identifier of the given synonym. for example "EC" for ECNumber.
      * The synonyms are loaded from the MIRIAM registry with custom synonyms
      * specified in the IdentifierDescription properites resource file.
      * @param synonym
      * @return
      */
     public Identifier ofSynonym(String synonym) {
 
         String key = synonym.toLowerCase(Locale.ENGLISH);
 
         if (synonyms.containsKey(key)) {
             return synonyms.get(key).newInstance();
         }
 
         throw new InvalidParameterException("No matching identifier synonym found for: " + synonym);
     }
 
     /**
      *
      * Utility method for reading an identifier to an ObjectOutput
      *
      */
     public Identifier read(ObjectInput in) throws IOException, ClassNotFoundException {
         Identifier identifier = ofIndex(in.readByte());
         identifier.readExternal(in);
         return identifier;
     }
 
     /**
      *
      * Utility method for writing an identifier to an ObjectOutput
      *
      */
     public void write(ObjectOutput out, Identifier identifier) throws IOException {
         out.writeByte(identifier.getIndex());
         identifier.writeExternal(out);
     }
     private static final String ID_SEPERATOR = "|";
     private static final String ID_ESCAPED_SEPERATOR = "\\|";
     private static final int DBID_MAX_LENGTH = 3;
 }
