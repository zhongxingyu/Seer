 /*
  *                    BioJava development code
  *
  * This code may be freely distributed and modified under the
  * terms of the GNU Lesser General Public Licence.  This should
  * be distributed with the code.  If you do not have a copy,
  * see:
  *
  *      http://www.gnu.org/copyleft/lesser.html
  *
  * Copyright for this code is held jointly by the individual
  * authors.  These should be listed in @author doc comments.
  *
  * For more information on the BioJava project and its aims,
  * or to join the biojava-l mailing list, visit the home page
  * at:
  *
  *      http://www.biojava.org/
  *
  */
 
 package org.biojavax.bio.seq;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioException;
 import org.biojava.bio.seq.Feature;
 import org.biojava.bio.seq.FeatureFilter;
 import org.biojava.bio.seq.FeatureHolder;
 import org.biojava.bio.seq.FilterUtils;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SimpleFeatureHolder;
 import org.biojava.bio.seq.StrandedFeature;
 import org.biojava.bio.seq.StrandedFeature.Strand;
 import org.biojava.bio.symbol.Location;
 import org.biojava.bio.symbol.SymbolList;
 import org.biojava.ontology.InvalidTermException;
 import org.biojava.ontology.Term;
 import org.biojava.utils.AbstractChangeable;
 import org.biojava.utils.ChangeEvent;
 import org.biojava.utils.ChangeSupport;
 import org.biojava.utils.ChangeVetoException;
 import org.biojavax.RankedCrossRef;
 import org.biojavax.RichAnnotation;
 import org.biojavax.SimpleRichAnnotation;
 import org.biojavax.RichObjectFactory;
 import org.biojavax.ontology.ComparableTerm;
 
 /**
  * A simple implementation of RichFeature.
  * @author Richard Holland
  * @author Mark Schreiber
  */
 public class SimpleRichFeature extends AbstractChangeable implements RichFeature {
     
     private RichAnnotation notes = new SimpleRichAnnotation();
     private ComparableTerm typeTerm;
     private ComparableTerm sourceTerm;
     private FeatureHolder parent;
     private RichLocation location = RichLocation.EMPTY_LOCATION;
     private Set crossrefs = new TreeSet();
     private Set relations = new TreeSet();
     private String name;
     private int rank;
     
     /**
      * Creates a new instance of SimpleRichFeature based on a template.
      * @param parent The parent feature holder.
      * @param templ The template to construct the feature from.
      * @throws ChangeVetoException if we don't want to be like the template.
      * @throws InvalidTermException if any of the template terms are bad.
      */
     public SimpleRichFeature(FeatureHolder parent, Feature.Template templ) throws ChangeVetoException, InvalidTermException {
         if (parent==null) throw new IllegalArgumentException("Parent cannot be null");
         if (templ==null) throw new IllegalArgumentException("Template cannot be null");
         if (templ.type==null && templ.typeTerm==null) throw new IllegalArgumentException("Template type cannot be null");
         if (templ.source==null && templ.sourceTerm==null) throw new IllegalArgumentException("Template source cannot be null");
         if (templ.location==null) throw new IllegalArgumentException("Template location cannot be null");
         
         this.setParent(parent);
         this.setLocation(templ.location);
         
         if (templ.typeTerm!=null) this.setTypeTerm(templ.typeTerm);
         else this.setType(templ.type);
         if (templ.sourceTerm!=null) this.setSourceTerm(templ.sourceTerm);
         else this.setSource(templ.source);
         
         if (templ.annotation instanceof RichAnnotation) {
             this.notes.setNoteSet(((RichAnnotation)templ.annotation).getNoteSet());
         } else {
             this.notes = new SimpleRichAnnotation();
             for (Iterator i = templ.annotation.keys().iterator(); i.hasNext(); ) {
                 Object key = i.next();
                 this.notes.setProperty(i.next(), templ.annotation.getProperty(key));
             }
         }
         
         if (templ instanceof RichFeature.Template) {
             this.setRankedCrossRefs(((RichFeature.Template)templ).rankedCrossRefs);
             this.setFeatureRelationshipSet(((RichFeature.Template)templ).featureRelationshipSet);
         }
     }
     
     // Hibernate requirement - not for public use.
     protected SimpleRichFeature() {}
     
     /**
      * {@inheritDoc}
      */
     public Feature.Template makeTemplate() {
         RichFeature.Template templ = new RichFeature.Template();
         templ.annotation = this.notes;
         templ.featureRelationshipSet = this.relations;
         templ.rankedCrossRefs = this.crossrefs;
         templ.location = this.location;
         templ.sourceTerm = this.sourceTerm;
         templ.source = this.sourceTerm.getName();
         templ.typeTerm = this.typeTerm;
         templ.type = this.typeTerm.getName();
         return templ;
     }
     
     /**
      * {@inheritDoc}
      */
     public Annotation getAnnotation() { return this.notes; }
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public Set getNoteSet() { return this.notes.getNoteSet(); }
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public void setNoteSet(Set notes) throws ChangeVetoException { this.notes.setNoteSet(notes); }
     
     // Hibernate use only
     private Set getLocationSet() {
         // Convert the location into a set of BioSQL-compatible simple locations
         Collection newlocs = RichLocation.Tools.flatten(this.location);
         this.locsSet.retainAll(newlocs); // clear out forgotten ones
         this.locsSet.addAll(newlocs); // add in new ones
         return this.locsSet; // original for Hibernate purposes
     }
     
     // Hibernate use only
     private void setLocationSet(Set locs) throws ChangeVetoException {
         this.locsSet = locs; // original kept for Hibernate purposes
         // Construct a nice BioJavaX location from the set of BioSQL-compatible simple ones
         this.location = RichLocation.Tools.construct(RichLocation.Tools.merge(locs));
     }
     private Set locsSet = new TreeSet();
     
     /**
      * {@inheritDoc}
      */
     public void setName(String name) throws ChangeVetoException {
         if(!this.hasListeners(RichFeature.NAME)) {
             this.name = name;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.NAME,
                     name,
                     null
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.NAME);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.name = name;
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public String getName() { return this.name; }
     
     /**
      * {@inheritDoc}
      */
     public void setRank(int rank) throws ChangeVetoException {
         if(!this.hasListeners(RichFeature.RANK)) {
             this.rank = rank;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.RANK,
                     new Integer(rank),
                     new Integer(this.rank)
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.RANK);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.rank = rank;
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public int getRank() { return this.rank; }
     
     /**
      * {@inheritDoc}
      */
     public Sequence getSequence() {
         FeatureHolder p = this.parent;
         while (p instanceof Feature) p = ((Feature)p).getParent();
         return (Sequence)p;
     }
     
     /**
      * {@inheritDoc}
      */
     public String getSource() { return this.sourceTerm.getName(); }
     
     /**
      * {@inheritDoc}
      */
     public void setSource(String source) throws ChangeVetoException {
         try {
             this.setSourceTerm(RichObjectFactory.getDefaultOntology().getOrCreateTerm(source));
         } catch (InvalidTermException e) {
             throw new ChangeVetoException("Source term was rejected by the default ontology",e);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public Term getSourceTerm() { return this.sourceTerm; }
     
     /**
      * {@inheritDoc}
      */
     public void setSourceTerm(Term t) throws ChangeVetoException, InvalidTermException {
         if (t==null) throw new IllegalArgumentException("Term cannot be null");
         if (!(t instanceof ComparableTerm)) t = RichObjectFactory.getDefaultOntology().getOrImportTerm(t);
         if(!this.hasListeners(RichFeature.SOURCETERM)) {
             this.sourceTerm = (ComparableTerm)t;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.SOURCETERM,
                     t,
                     this.sourceTerm
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.SOURCETERM);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.sourceTerm = (ComparableTerm)t;
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public String getType() { return this.typeTerm.getName(); }
     
     /**
      * {@inheritDoc}
      */
     public void setType(String type) throws ChangeVetoException {
         try {
             this.setTypeTerm(RichObjectFactory.getDefaultOntology().getOrCreateTerm(type));
         } catch (InvalidTermException e) {
             throw new ChangeVetoException("Type term was rejected by the default ontology",e);
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public Term getTypeTerm() { return this.typeTerm; }
     
     /**
      * {@inheritDoc}
      */
     public void setTypeTerm(Term t) throws ChangeVetoException, InvalidTermException {
         if (t==null) throw new IllegalArgumentException("Term cannot be null");
         if (!(t instanceof ComparableTerm)) t = RichObjectFactory.getDefaultOntology().getOrImportTerm(t);
         if(!this.hasListeners(RichFeature.TYPETERM)) {
             this.typeTerm = (ComparableTerm)t;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.TYPETERM,
                     t,
                     this.typeTerm
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.TYPETERM);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.typeTerm = (ComparableTerm)t;
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public SymbolList getSymbols() { return this.location.symbols(this.getSequence()); }
     
     /**
      * {@inheritDoc}
      */
     public Location getLocation() { return this.location; }
     
     /**
      * {@inheritDoc}
      */
     public void setLocation(Location loc) throws ChangeVetoException {
         if (loc==null) throw new IllegalArgumentException("Location cannot be null");
        if (!(loc instanceof RichLocation)) loc = RichLocation.Tools.enrich(loc);
         if(!this.hasListeners(RichFeature.LOCATION)) {
             this.location = (RichLocation)loc;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.LOCATION,
                     loc,
                     this.location
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.LOCATION);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.location = (RichLocation)loc;
                 cs.firePostChangeEvent(ce);
             }
         }
         this.location.setFeature(this);
     }
     
     /**
      * {@inheritDoc}
      */
     public FeatureHolder getParent() { return this.parent; }
     
     /**
      * {@inheritDoc}
      */
     public void setParent(FeatureHolder parent) throws ChangeVetoException {
         if (parent==null) throw new IllegalArgumentException("Parent cannot be null");
         if(!this.hasListeners(RichFeature.PARENT)) {
             this.parent = parent;
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.PARENT,
                     parent,
                     this.parent
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.PARENT);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.parent = parent;
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public Set getRankedCrossRefs() { return this.crossrefs; }
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public void setRankedCrossRefs(Set crossrefs) throws ChangeVetoException {
         this.crossrefs = crossrefs; // original for Hibernate
     }
     
     /**
      * {@inheritDoc}
      */
     public void addRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
         if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
         if(!this.hasListeners(RichFeature.CROSSREF)) {
             this.crossrefs.add(crossref);
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.CROSSREF,
                     crossref,
                     null
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.CROSSREF);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.crossrefs.add(crossref);
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeRankedCrossRef(RankedCrossRef crossref) throws ChangeVetoException {
         if (crossref==null) throw new IllegalArgumentException("Crossref cannot be null");
         if(!this.hasListeners(RichFeature.CROSSREF)) {
             this.crossrefs.remove(crossref);
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.CROSSREF,
                     null,
                     crossref
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.CROSSREF);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.crossrefs.remove(crossref);
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public Set getFeatureRelationshipSet() { return this.relations; } // must be original for Hibernate
     
     /**
      * {@inheritDoc} 
      * <b>Warning</b> this method gives access to the original 
      * Collection not a copy. This is required by Hibernate. If you
      * modify the object directly the behaviour may be unpredictable.
      */
     public void setFeatureRelationshipSet(Set relationships) throws ChangeVetoException {
         this.relations = relationships;  // must be original for Hibernate
     }
     
     /**
      * {@inheritDoc}
      */
     public void addFeatureRelationship(RichFeatureRelationship relationship) throws ChangeVetoException {
         if (relationship==null) throw new IllegalArgumentException("Relationship cannot be null");
         if(!this.hasListeners(RichFeature.RELATION)) {
             this.relations.add(relationship);
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.RELATION,
                     relationship,
                     null
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.RELATION);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.relations.add(relationship);
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeFeatureRelationship(RichFeatureRelationship relationship) throws ChangeVetoException {
         if (relationship==null) throw new IllegalArgumentException("Relationship cannot be null");
         if(!this.hasListeners(RichFeature.RELATION)) {
             this.relations.remove(relationship);
         } else {
             ChangeEvent ce = new ChangeEvent(
                     this,
                     RichFeature.RELATION,
                     null,
                     relationship
                     );
             ChangeSupport cs = this.getChangeSupport(RichFeature.RELATION);
             synchronized(cs) {
                 cs.firePreChangeEvent(ce);
                 this.relations.remove(relationship);
                 cs.firePostChangeEvent(ce);
             }
         }
     }
     
     // Converts relations into a set of child feature objects
     private Set relationsToFeatureSet() {
         Set features = new TreeSet();
         for (Iterator i = this.relations.iterator(); i.hasNext(); ) {
             RichFeatureRelationship r = (RichFeatureRelationship)i.next();
             features.add(r.getSubject());
         }
         return features;
     }
     
     /**
      * {@inheritDoc}
      */
     public boolean containsFeature(Feature f) { return this.relationsToFeatureSet().contains(f); }
     
     /**
      * {@inheritDoc}
      */
     public int countFeatures() { return this.relationsToFeatureSet().size(); }
     
     /**
      * {@inheritDoc}
      */
     public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
         if (ft==null) throw new IllegalArgumentException("Template cannot be null");
         RichFeature f;
         try {
             f = new SimpleRichFeature(this.parent, ft);
         } catch (InvalidTermException e) {
             throw new ChangeVetoException("Term was not accepted",e);
         }
         this.addFeatureRelationship(
                 new SimpleRichFeatureRelationship(this, f, SimpleRichFeatureRelationship.getContainsTerm(), 0)
                 );
         return f;
     }
     
     /**
      * {@inheritDoc}
      */
     public Iterator features() { return this.relationsToFeatureSet().iterator(); }
     
     /**
      * {@inheritDoc}
      */
     public FeatureHolder filter(FeatureFilter filter) {
         boolean recurse = !FilterUtils.areProperSubset(filter, FeatureFilter.top_level);
         return this.filter(filter, recurse);
     }
     
     /**
      * {@inheritDoc}
      */
     public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
         SimpleFeatureHolder fh = new SimpleFeatureHolder();
         for (Iterator i = this.features(); i.hasNext(); ) {
             Feature f = (RichFeature)i.next();
             try {
                 if (fc.accept(f)) fh.addFeature(f);
             } catch (ChangeVetoException e) {
                 throw new RuntimeException("Aaargh! Our feature was rejected!",e);
             }
         }
         return fh;
     }
     
     /**
      * {@inheritDoc}
      */
     public FeatureFilter getSchema() { return FeatureFilter.all; }
     
     /**
      * {@inheritDoc}
      */
     public void removeFeature(Feature f) throws ChangeVetoException, BioException {
         Set features = new TreeSet();
         for (Iterator i = this.relations.iterator(); i.hasNext(); ) {
             RichFeatureRelationship r = (RichFeatureRelationship)i.next();
             if (r.getSubject().equals(f)) i.remove();
         }
     }
     
     /**
      * {@inheritDoc}
      * NOT IMPLEMENTED.
      */
     public void setStrand(StrandedFeature.Strand strand) throws ChangeVetoException {
         throw new ChangeVetoException("The strand is immutable on RichFeature objects.");
     }
     
     /**
      * {@inheritDoc}
      */
     public StrandedFeature.Strand getStrand() {
         RichLocation.Strand s = this.location.getStrand();
         if (s.equals(RichLocation.Strand.NEGATIVE_STRAND)) return StrandedFeature.NEGATIVE;
         if (s.equals(RichLocation.Strand.POSITIVE_STRAND)) return StrandedFeature.POSITIVE;
         else return StrandedFeature.UNKNOWN;
     }
     
     /**
      * {@inheritDoc}
      */
     public int hashCode() {
         int code = 17;
         // Hibernate comparison - we haven't been populated yet
         if (this.parent==null) return code;
         // Normal comparison
         code = 31*code + this.parent.hashCode();
         code = 31*code + this.sourceTerm.hashCode();
         code = 31*code + this.typeTerm.hashCode();
         code = 31*code + this.rank;
         return code;
     }
     
     /**
      * {@inheritDoc}
      * Features are equal when they have the same parent, type, source
      * and rank.
      */
     public boolean equals(Object o) {
         if (! (o instanceof Feature)) return false;
         // Hibernate comparison - we haven't been populated yet
         if (this.parent==null) return false;
         // Normal comparison
         Feature fo = (Feature) o;
         if (! this.parent.equals(fo.getParent())) return false;
         if (! this.typeTerm.equals(fo.getTypeTerm())) return false;
         if (! this.sourceTerm.equals(fo.getSourceTerm())) return false;
         if (fo instanceof RichFeature) {
             RichFeature rfo = (RichFeature)fo;
             return rfo.getRank()==this.getRank();
         }
         return false;
     }
     
     /**
      * {@inheritDoc}
      * Features are sorted by rank only. However, equal ranks are
      * acceptable, so -1 is returned if the ranks are equal.
      */
     public int compareTo(Object o) {
         // Hibernate comparison - we haven't been populated yet
         if (this.parent==null) return -1;
         // Normal comparison
         Feature them = (Feature)o;
         if (them instanceof RichFeature) {
             RichFeature rfo = (RichFeature)them;
             if (this.rank!=rfo.getRank()) return this.rank-rfo.getRank();
         }
         return -1; // because multiple equal items are allowed
     }
     
     /**
      * {@inheritDoc}
      * Form: "(#rank) parent:type,source(location)"
      */
     public String toString() {
         return "(#"+this.rank+") "+this.parent+":"+this.getType()+","+this.getSource()+"("+this.location+")";
     }
     
     // Hibernate requirement - not for public use.
     private Integer id;
     
     // Hibernate requirement - not for public use.
     private Integer getId() { return this.id; }
     
     // Hibernate requirement - not for public use.
     private void setId(Integer id) { this.id = id; }
 
 }
 
