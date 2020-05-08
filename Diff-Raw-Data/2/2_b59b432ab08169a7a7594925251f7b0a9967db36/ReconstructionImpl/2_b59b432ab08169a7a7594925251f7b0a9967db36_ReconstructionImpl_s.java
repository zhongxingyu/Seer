 /*
  * Copyright (C) 2012  John May and Pablo Moreno
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package uk.ac.ebi.mdk.domain.entity;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import uk.ac.ebi.mdk.domain.identifier.basic.ReconstructionIdentifier;
 import uk.ac.ebi.mdk.domain.entity.collection.*;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicParticipant;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 import uk.ac.ebi.mdk.domain.identifier.Identifier;
 import uk.ac.ebi.mdk.domain.matrix.StoichiometricMatrix;
 import uk.ac.ebi.mdk.domain.identifier.Taxonomy;
 
 import java.io.*;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 
 /**
  * ReconstructionImpl.java
  * Object to represent a complete reconstruction with genes, reactions and metabolites
  *
  * @author johnmay
  * @date Apr 13, 2011
  */
 public class ReconstructionImpl
         extends AbstractAnnotatedEntity
         implements Externalizable, Reconstruction {
 
     private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
             ReconstructionImpl.class);
 
     public static final String PROJECT_FILE_EXTENSION = ".mnb";
 
     private static final String DATA_FOLDER_NAME = "data";
 
     private static final String TMP_FOLDER_NAME = "mnb-tmp";
 
     private static final String GENE_PRODUCTS_FILE_NAME = "serialized-gene-projects.java-bin";
 
     public static final String BASE_TYPE = "Reconstruction";
     // main container for the project on the file system
 
     private File container;
 
     private Taxonomy taxonomy; // could be under a generic ReconstructionContents class but this is already used as an enum
     // component collections
 
     private Genome genome;
 
     private ProductCollection products;
 
     private Reactome reactions;
 
     private Metabolome metabolome;
 
     private Collection<EntityCollection> subsets;
 
     // s matrix
     private StoichiometricMatrix matrix;
 
 
     /**
      * Constructor mainly used for creating a new ReconstructionImpl
      *
      * @param id  The identifier of the project
      * @param org The organism identifier
      */
     public ReconstructionImpl(ReconstructionIdentifier id,
                               Taxonomy org) {
        super(id, org.getCommonName(), org.getCode());
         taxonomy = org;
         reactions = new ReactionList();
         metabolome = new MetabolomeImpl();
         products = new ProductCollection();
         genome = new GenomeImplementation();
         subsets = new ArrayList<EntityCollection>();
     }
 
 
     public ReconstructionImpl(Identifier identifier, String abbreviation, String name) {
         super(identifier, abbreviation, name);
         reactions = new ReactionList();
         metabolome = new MetabolomeImpl();
         products = new ProductCollection();
         genome = new GenomeImplementation();
         subsets = new ArrayList<EntityCollection>();
     }
 
 
     /*
     * Default constructor
     */
     public ReconstructionImpl() {
         metabolome = new MetabolomeImpl();
         reactions = new ReactionList();
         genome = new GenomeImplementation();
         products = new ProductCollection();
         subsets = new ArrayList<EntityCollection>();
     }
 
 
     public ReconstructionImpl newInstance() {
         return new ReconstructionImpl();
     }
 
 
     /**
      * Access the taxonmy of this reconstruction
      *
      * @return
      */
     public Taxonomy getTaxonomy() {
         return taxonomy;
     }
 
 
     @Override
     public String getAccession() {
         String accession = super.getAccession();
         if (accession.contains("%m")) {
             accession = accession.replaceAll("%m", Integer.toString(metabolome.size()));
         }
         if (accession.contains("%n")) {
             accession = accession.replaceAll("%n", Integer.toString(reactions.size()));
         }
         return accession;
     }
 
 
     /**
      * Access the genome of the reconstruction. The genome
      * provides methods for adding chromosomes and genes.
      *
      * @return The genome associated with the reconstruction
      */
     public Genome getGenome() {
         return genome;
     }
 
 
     public void setGenome(Genome genome) {
         this.genome = genome;
     }
 
     /**
      * Access a collection of all the genes in the
      * reconstruction. Adding genes to this collection
      * will not add them to the reconstruction. See
      * {@see Chromosome} and {@se Genome} for how
      * to add genes.
      *
      * @return All genes currently in the reconstruction
      */
     public Collection<Gene> getGenes() {
         return genome.getGenes();
     }
 
 
     /**
      * Access to the gene products associated with the
      * reconstruction as {@see ProductCollection}. The
      * gene product collection contains a mix of Protein,
      * Ribosomal RNA and Transfer RNA products
      *
      * @return
      */
     public ProductCollection getProducts() {
         return products;
     }
 
     /**
      * Add a product to the reconstruction
      * @param product
      */
     public void addProduct(GeneProduct product) {
         products.add(product);
     }
 
 
     /**
      * Access to the reactions associated with the
      * reconstruction as {@see ReactionList}. The
      * reaction order is maintained in List to ease
      * read/write operations
      *
      * @return
      */
     public Reactome getReactions() {
         return reactions;
     }
 
     public Reactome getReactome() {
         return reactions;
     }
 
 
     public Proteome getProteome(){
         return products;
     }
 
     /**
      * Access the collection of metabolites for this
      * reconstruction
      *
      * @return
      */
     public Metabolome getMetabolome() {
         return metabolome;
     }
 
 
     /**
      * Add a new metabolic reaction to the
      * reconstruction. Note this method does not
      * check for duplications.
      *
      * @param reaction a new reaction
      */
     public void addReaction(MetabolicReaction reaction) {
         reactions.add(reaction);
 
         for (MetabolicParticipant p : reaction.getReactants()) {
             if (metabolome.contains(p.getMolecule()) == false) {
                 addMetabolite(p.getMolecule());
             }
         }
         for (MetabolicParticipant p : reaction.getProducts()) {
             if (metabolome.contains(p.getMolecule()) == false) {
                 addMetabolite(p.getMolecule());
             }
         }
 
     }
 
 
     /**
      * Add a new metabolite to the reconstruction.
      * Note this method does not check for duplicates
      *
      * @param metabolite a new metabolite
      */
     public void addMetabolite(Metabolite metabolite) {
         metabolome.add(metabolite);
     }
 
 
     /**
      * Add a new subset to the reconstruction. The subset should
      * define entities already in the reconstruction.
      */
     public boolean addSubset(EntityCollection subset) {
         return subsets.add(subset);
     }
 
 
     public Collection<EntityCollection> getSubsets() {
         return subsets;
     }
 
 
     /**
      * Removes a subset from the reconstruction. The subset should
      * define entities already in the reconstruction. Note removing
      * the subset will not remove the entities
      */
     public boolean removeSubset(EntityCollection subset) {
         return subsets.remove(subset);
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public ReconstructionIdentifier getIdentifier() {
         return (ReconstructionIdentifier) super.getIdentifier();
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String toString() {
         return getAccession();
     }
 
 
     /**
      * Holding methods (likely to change) *
      */
     public void setMatrix(StoichiometricMatrix matrix) {
         this.matrix = matrix;
     }
 
     public StoichiometricMatrix getMatrix() {
         return matrix;
     }
 
 
     public boolean hasMatrix() {
         return matrix != null;
 
 
     }
     // TODO (jwmay) MOVE all methods below this comment
 
 
     public void setContainer(File container) {
         this.container = container;
     }
 
 
     public File getContainer() {
         if (container == null) {
             container = new File(System.getProperty("user.home") + File.separator + getAccession() + PROJECT_FILE_EXTENSION);
         }
         return container;
     }
 
 
 
     /**
      * Loads a reconstruction from a given container
      */
     //    public static ReconstructionImpl load(File container) throws IOException, ClassNotFoundException {
     //
     //        File file = new File(container, "recon.extern.gzip");
     //        ObjectInput in = new ObjectInputStream(new GZIPInputStream(new FileInputStream(file),
     //                                                                   1024 * 8)); // 8 mb
     //        ReconstructionImpl reconstruction = new ReconstructionImpl();
     //        reconstruction.readExternal(in);
     //
     //        return reconstruction;
     //
     //    }
     //    /**
     //     * Saves the project and it's data
     //     * @return if the project was saved
     //     */
     //    public boolean save() throws IOException {
     //        if (container != null) {
     //
     //            ObjectOutput out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(
     //                    new File(container, "recon.extern.gzip"))));
     //            this.writeExternal(out);
     //            out.close();
     //            return true;
     //
     //        }
     //        return false;
     //    }
     //
     //
     //    public void saveAsProject(File projectRoot) throws IOException {
     //
     //        if (!projectRoot.getPath().endsWith("mnb")) {
     //            projectRoot = new File(projectRoot.getPath() + ".mnb");
     //        }
     //
     //        // create folder
     //        if (!projectRoot.exists()) {
     //            logger.info("Saving project as " + projectRoot);
     //            setContainer(projectRoot);
     //            container.mkdir();
     //            getDataDirectory().mkdir();
     //            save();
     //            //  setTmpDir();
     //        } else if (projectRoot.equals(container)) {
     //            save();
     //        } else {
     //            JOptionPane.showMessageDialog(null,
     //                                          "Cannot overwrite a different project");
     //        }
     //    }
     public void writeExternal(ObjectOutput out) throws IOException {
         //        super.writeExternal(out);
         //
         //        out.writeUTF(container.getAbsolutePath());
         //
         //        taxonomy.writeExternal(out);
         //
         //
         //        // genome
         //        genome.write(out);
         //
         //        // products
         //        products.writeExternal(out, genome);
         //
         //        // metabolites
         //        out.writeInt(metabolites.size());
         //        for (Metabolite metabolite : metabolites) {
         //            metabolite.writeExternal(out);
         //        }
         //
         //        // reactions
         //        out.writeInt(reactions.size());
         //        for (MetabolicReaction reaction : reactions) {
         //            reaction.writeExternal(out, metabolites, products);
         //            // already writen so don't need to write
         //        }
     }
 
 
     public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
         //        super.readExternal(in);
         //
         //        container = new File(in.readUTF());
         //
         //        // ids
         //        taxonomy = new Taxonomy();
         //        taxonomy.readExternal(in);
         //
         //        // genome
         //        genome.read(in);
         //
         //        // products
         //        products = new ProductCollection();
         //        products.readExternal(in, genome);
         //
         //
         //
         //        // metabolites
         //        metabolites = new MetaboliteCollection();
         //        int nMets = in.readInt();
         //        for (int i = 0; i < nMets; i++) {
         //            Metabolite m = DefaultEntityFactory.getInstance().newInstance(Metabolite.class);
         //            m.readExternal(in);
         //            metabolites.add(m);
         //        }
         //
         //        // reactions
         //        reactions = new ReactionList();
         //
         //        long start = System.currentTimeMillis();
         //        int nRxns = in.readInt();
         //        for (int i = 0; i < nRxns; i++) {
         //            MetabolicReaction r = new MetabolicReaction();
         //            r.readExternal(in, metabolites, products);
         //            reactions.add(r);
         //        }
         //        long end = System.currentTimeMillis();
         //        logger.info("Loaded reaction into collection " + (end - start) + " ms");
     }
 
     @Override
     public void setTaxonomy(Identifier taxonomy) {
         if (taxonomy instanceof Taxonomy) {
             setTaxonomy((Taxonomy) taxonomy);
         }
         else {
             throw new InvalidParameterException("Not taxonomic identifier!");
         }
     }
 
 
     public void setTaxonomy(Taxonomy taxonomy) {
         this.taxonomy = taxonomy;
     }
 }
