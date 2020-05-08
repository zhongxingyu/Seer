 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sbolstandard.libSBOLj;
 
 import com.clarkparsia.empire.SupportsRdfId;
 import com.clarkparsia.empire.annotation.Namespaces;
 import com.clarkparsia.empire.annotation.RdfId;
 import com.clarkparsia.empire.annotation.RdfProperty;
 import com.clarkparsia.empire.annotation.RdfsClass;
 import com.clarkparsia.empire.annotation.SupportsRdfIdImpl;
 import gnu.crypto.hash.HashFactory;
 import gnu.crypto.hash.IMessageDigest;
 import java.util.Collection;
 import java.util.HashSet;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.OneToMany;
 import org.apache.commons.codec.binary.Hex;
 import org.sbolstandard.libSBOLj.SBOLutil.SkipInJson;
 
 /**
  * The SBOL data model's SequenceAnnotation for RDF and Json.
  *
  * Sequence Annotations give the location and direction of SequenceFeatures of
  * a DnaComponent. The location is specified by the start and stop positions of
  * the Sequence Feature along the DNA sequence. We are using the convention that
  * the first base pair of a DNA sequence is position 1. This convention is
  * established by the broader Molecular biology community, in literature.
  * The direction of the feature is specified by the strand [+/-]. Sequences used
  * are by convention assumed 5' to 3', therefore the <code>+</code> strand is
  * 5' to 3' and the <code>-</code> strand is 3' to 5'.
  *
  * @author mgaldzic
  * @since  0.1, 02/08/2011
  */
 @Namespaces({"sbol", "http://sbols.org/sbol.owl#"})
 @RdfsClass("sbol:SequenceAnnotation")
 @Entity
 public class SequenceAnnotation implements SupportsRdfId {
 
     @SkipInJson
     private SupportsRdfId mIdSupport = new SupportsRdfIdImpl();
     @RdfId(namespace = "http://sbols.org/sbol.owl#")
     private String id;
     @RdfProperty("sbol:start")
     private Integer start;
     @RdfProperty("sbol:stop")
     private Integer stop;
     @RdfProperty("sbol:strand")
     private String strand;
     @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     @RdfProperty("sbol:feature")
     private Collection<SequenceFeature> feature = new HashSet<SequenceFeature>();
 
 
     /**
      * Place a SequenceFeature at this.start, .stop, .strand location.
      *
      * Feature describes this position so it should include the information
      * the users want to be able to get when examining this position.
      *
      * @param feature to describe what is located at this annotation.
      */
     public void addFeature(SequenceFeature feature) {
         if (!getFeatures().contains(feature)) {
             getFeatures().add(feature);
             //this.feature.add(feature);
         }
     }
 
     /**
      * Describe the segments of DNA sequence from start to stop on the strand.
      *
      * The feature has its own displayID, name, description, type, and sequence.
      * Most commonly it is used to describe the sub-sequences of a DnaComponent.
      * Features can be re-used, whenever the SequenceFeature.equals
      * NOTE: the feature could be used as a DnaComponent itself, but this is not
      * implemented in libSBOLj, yet. SbolService does not yet check if an .equals
      * feature exists already.
      * @return collection of any features at this location
      */
     public Collection<SequenceFeature> getFeatures() {
         return feature;
     }
 
     /**
      * The ID portion of the URI which identifies this object
      * @return the RDF id for the object
      */
     public String getId() {
         return id;
     }
 
     /**
      * Creates an ID based on a hash of the start,stop,strand,and parent DnaCompoment.
      *
      * Uses sha-256 to hash the information elements. The parentDnaComponent is
      * used to make sure the hash is unique for any given Sequenceannotation of
      * a DnaComponent. This also enforces the use of one SequenceAnnotation for
      * a single position with strand. 
      * 
      * NOTE: The implication is that there should be a one-to-many relationship
      * for SequenceFeatures. That way multiple features can be placed in one
      * position and orientation. IDENTIFY THE USE CASE THAT NECESSITATES THIS.
      *
      * @param parentDnaComp the DnaComponent that this annotation is for
      */
     public void setId(DnaComponent parentDnaComp) {
         String newId;
         if (this.id == null) {
             String idString = start + stop + strand + parentDnaComp.hashCode();
             IMessageDigest md = HashFactory.getInstance("sha-256");
             byte[] input = idString.getBytes();
             md.update(input, 0, input.length);
             newId = new String(Hex.encodeHex(md.digest()));
         } else {
             newId = this.id;
         }
         this.id = newId;
     }
 
     /**
      * First position of the Sequence Feature being annotated.
      *
      * Start coordinate is in terms of the DnaSequence of the DnaComponent
      * annotated.
      *
      * @return positive integer coordinate of first base of the SequenceFeature.
      */
     public int getStart() {
         return start;
     }
 
     /**
      * First position of the Sequence Feature on the DnaComponent.
      * 
      * Start coordinate in terms of the DnaSequence of the DnaComponent annotated.
      *
      * @param start positive integer coordinate of first base of the SequenceFeature
      */
     public void setStart(int start) {
         this.start = start;
     }
 
     /**
      * Last position of the Sequence Feature on the DnaComponent.
      *
      * Stop coordinate is in terms of the DnaSequence of the DnaComponent annotated.
      *
      * @return positive integer coordinate of last base of the SequenceFeature
      */
     public Integer getStop() {
         return stop;
     }
 
     /**
      * Last position of the Sequence Feature on the DnaComponent.
      *
      * @param stop positive integer coordinate of last base of the SequenceFeature
      */
     public void setStop(Integer stop) {
         this.stop = stop;
     }
 
     /**
      * Orientation of SequenceFeature is the + or - strand.
      * 
      * Sequences used are by convention assumed 5' to 3', therefore the 
      * <code>+</code> strand is 5' to 3' and the <code>-</code> strand 
      * is 3' to 5'.
      *
      * @return <code>+</code> if feature aligns in same direction as DnaComponent,
      *         <code>-</code> if feature aligns in opposite direction as DnaComponent.
      */
     public String getStrand() {
         return strand;
     }
 
     /**
      * Orientation + or - of the SequenceFeature relative to the DNA sequence of
      * the DnaComponent. 
      *
      * DnaSequence used is by convention assumed 5' to 3', therefore the
      * <code>+</code> strand is 5' to 3' and the <code>-</code> strand
      * is 3' to 5'.
      *
      * @param strand <code>+</code> if feature aligns in same direction as DnaComponent,
      *               <code>-</code> if feature aligns in opposite direction as DnaComponent.
      */
     public void setStrand(String strand) {
         this.strand = strand;
     }
 
     /**
      * 
      * @return
      * @inheritDoc
      */
     public RdfKey getRdfId() {
         return mIdSupport.getRdfId();
     }
 
     /**
      * @param id 
      * @inheritDoc
      */
     public void setRdfId(final RdfKey id) {
         mIdSupport.setRdfId(id);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final SequenceAnnotation other = (SequenceAnnotation) obj;
         if (getRdfId() != null) {
             return getRdfId().equals(other.getRdfId());
         } else {
             if (this.start != other.start) {
                 return false;
             }
             if ((this.stop == null) ? (other.stop != null) : !this.stop.equals(other.stop)) {
                 return false;
             }
             if ((this.strand == null) ? (other.strand != null) : !this.strand.equals(other.strand)) {
                 return false;
             }
             if (this.feature != other.feature && (this.feature == null || !this.feature.equals(other.feature))) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         return getRdfId() == null ? 0 : getRdfId().value().hashCode();
     }
 }
