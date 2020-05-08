 package uk.ac.kcl.inf.provoking.serialise.rdf;
 
 import java.net.URI;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import uk.ac.kcl.inf.provoking.model.ActedOnBehalfOf;
 import uk.ac.kcl.inf.provoking.model.Activity;
 import uk.ac.kcl.inf.provoking.model.Agent;
 import uk.ac.kcl.inf.provoking.model.AlternateOf;
 import uk.ac.kcl.inf.provoking.model.Attribute;
 import uk.ac.kcl.inf.provoking.model.Description;
 import uk.ac.kcl.inf.provoking.model.Document;
 import uk.ac.kcl.inf.provoking.model.Entity;
 import uk.ac.kcl.inf.provoking.model.HadMember;
 import uk.ac.kcl.inf.provoking.model.HadPrimarySource;
 import uk.ac.kcl.inf.provoking.model.InstantaneousEvent;
 import uk.ac.kcl.inf.provoking.model.SpecializationOf;
 import uk.ac.kcl.inf.provoking.model.Used;
 import uk.ac.kcl.inf.provoking.model.WasAssociatedWith;
 import uk.ac.kcl.inf.provoking.model.WasAttributedTo;
 import uk.ac.kcl.inf.provoking.model.WasDerivedFrom;
 import uk.ac.kcl.inf.provoking.model.WasEndedBy;
 import uk.ac.kcl.inf.provoking.model.WasGeneratedBy;
 import uk.ac.kcl.inf.provoking.model.WasInfluencedBy;
 import uk.ac.kcl.inf.provoking.model.WasInformedBy;
 import uk.ac.kcl.inf.provoking.model.WasInvalidatedBy;
 import uk.ac.kcl.inf.provoking.model.WasQuotedFrom;
 import uk.ac.kcl.inf.provoking.model.WasRevisionOf;
 import uk.ac.kcl.inf.provoking.model.WasStartedBy;
 import uk.ac.kcl.inf.provoking.model.util.AttributeHolder;
 import uk.ac.kcl.inf.provoking.model.util.Identified;
 import uk.ac.kcl.inf.provoking.model.util.Term;
 import uk.ac.kcl.inf.provoking.serialise.SerialisationHint;
 
 /**
  * Serialises a PROV Document into a sequence of RDF triples, sent to registered
  * triple listeners for recording in the desired storage. Triple listeners are
  * registered on construction of the RDFSerialiser.
  * 
  * @author Simon Miles
  */
 public class RDFSerialiser {
     private static int _lastBlank = 0;
     private List<TriplesListener> _listeners;
     private Map<Description, String> _blankIDs;
 
     /**
      * Create a serialiser returning triples to the given listeners.
      * 
      * @param listeners Listeners receiving the triples produced by serialising documents.
      */
     public RDFSerialiser (TriplesListener... listeners) {
         _listeners = new LinkedList<> (Arrays.asList (listeners));
         _blankIDs = new HashMap<> ();
     }
 
     /**
      * Creates a blank node identifier for the given description, or returns the
      * previously created identifier if one exists.
      * 
      * @param description The description for which a blank node identifier is required
      * @return The blank node identifier string
      */
     private String blank (Description description) {
         String blank = _blankIDs.get (description);
 
         if (blank == null) {
             _lastBlank += 1;
             blank = "_:b" + _lastBlank;
             _blankIDs.put (description, blank);
         }
 
         return blank;
     }
 
     /**
      * Report a triple with a URI subject and literal object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (URI subject, URI predicate, Literal object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, object);
         }
     }
 
     /**
      * Report a triple with a URI subject and a URI object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (URI subject, URI predicate, URI object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, object);
         }
     }
 
     /**
      * Report a triple with a URI subject and a blank node object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (URI subject, URI predicate, String blankObject) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, blankObject);
         }
     }
 
     /**
      * Report a triple with a blank node subject and a literal object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (String blankSubject, URI predicate, Literal object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, object);
         }
     }
 
     /**
      * Report a triple with a blank node subject and a URI object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (String blankSubject, URI predicate, URI object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, object);
         }
     }
 
     /**
      * Report a triple with a blank node subject and a blank node object to the registered listeners.
      * 
      * @param subject The triple subject
      * @param predicate The triple predicate
      * @param object The triple object
      */
     private void fire (String blankSubject, URI predicate, String blankObject) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, blankObject);
         }
     }
 
     /**
      * Returns true if the given description can be expressed by a binary, rather than qualified, relation.
      * A description will fit this criteria if it has no attributes, no timestamp, no
      * role, no location, no other optional arguments (e.g. the plan for wasAssociatedWith relations),
      * and there is no serialisation hint in the document stating that the description should
      * be explicitly identified.
      * 
      * @param description The description that may be expressible as a single PROV relation triple
      * @param document The document in which the description is contained
      * @param optionalArguments The optional arguments of the description
      */
     private boolean isMinimal (Description description, Document document, Object... optionalArguments) {
         if (description instanceof AttributeHolder && ((AttributeHolder) description).hasAttributes ()) {
             return false;
         }
         if (description instanceof InstantaneousEvent) {
             if (((InstantaneousEvent) description).getTime () != null) {
                 return false;
             }
             if (((InstantaneousEvent) description).getRole () != null) {
                 return false;
             }
             if (((InstantaneousEvent) description).getLocation () != null) {
                 return false;
             }
         }
         for (Object argument : optionalArguments) {
             if (argument != null) {
                 return false;
             }
         }
         if (SerialisationHint.wasExplicitlyIdentified (description, document)) {
             return false;
         }
         return true;
     }
 
     /**
      * Returns the PROV-O equivalents for PROV-O vocabulary, i.e. prov:type becomes
      * rdf:type and prov:label becomes rdf:label. In all other cases, the URI
      * is returned unchanged.
      * 
      * @param predicate The original URI (possibly PROV-N specific)
      * @return The translated PROV-O URI
      */
     private URI provnToProvo (URI predicate) {
         if (predicate.equals (Term.type.uri ())) {
             return RDF.typeURI ();
         }
         if (predicate.equals (Term.label.uri ())) {
             return RDF.labelURI ();
         }
         return predicate;
     }
 
     /**
      * Serialises the given PROV document, causing triples to be sent to 
      * the registered triple listeners.
      * 
      * @param document The document to serialise
      */
     public void serialise (Document document) {
         List<URI> rolesAndLocations = new LinkedList<> ();
         
         for (Description description : document) {
             serialise (description, document, rolesAndLocations);
         }
     }
     
     /**
      * Serialises a PROV description that is part of a document.
      * 
      * @param description The description to serialise
      * @param document The document from which the description is drawn
      * @param rolesAndLocations A record of which role and location URIs have already been given type declaration triples in the serialisation
      */
     private void serialise (Description description, Document document, List<URI> rolesAndLocations) {
         Term qualifiedRelation = Term.qualifiedDerivation;
         
         // References do not have serialisations in this document
         if (description instanceof Identified && ((Identified) description).isReference ()) {
             return;
         }
         // Some relations are always binary
         if (description instanceof SpecializationOf) {
             serialise (((SpecializationOf) description).getSpecificEntity (), Term.specializationOf, ((SpecializationOf) description).getGeneralEntity ());
             return;
         }
         if (description instanceof AlternateOf) {
             serialise (((AlternateOf) description).getAlternateA (), Term.alternateOf, ((AlternateOf) description).getAlternateB ());
             return;
         }
         if (description instanceof HadMember) {
             serialise (((HadMember) description).getCollection (), Term.hadMember, ((HadMember) description).getMember ());
             return;
         }
         // Activities, agents and entities may have times and/or locations
         if (description instanceof Activity) {
             serialiseLiteral (description, Term.startedAtTime, ((Activity) description).getStartedAt ());
             serialiseLiteral (description, Term.endedAtTime, ((Activity) description).getEndedAt ());
             serialise (description, Term.atLocation, ((Activity) description).getLocation ());
         }
         if (description instanceof Entity) {
             serialise (description, Term.atLocation, ((Entity) description).getLocation ());
         }
         if (description instanceof Agent) {
             serialise (description, Term.atLocation, ((Agent) description).getLocation ());
         }
         // Relations that are subtypes of other relations
         if (description instanceof HadPrimarySource) {
             serialise (((HadPrimarySource) description).getDerived (), Term.hadPrimarySource, ((HadPrimarySource) description).getDerivedFrom ());
             qualifiedRelation = Term.qualifiedPrimarySource;
         }
         if (description instanceof WasQuotedFrom) {
             serialise (((WasQuotedFrom) description).getDerived (), Term.wasQuotedFrom, ((WasQuotedFrom) description).getDerivedFrom ());
             qualifiedRelation = Term.qualifiedQuotation;
         }
         if (description instanceof WasRevisionOf) {
             serialise (((WasRevisionOf) description).getDerived (), Term.wasRevisionOf, ((WasRevisionOf) description).getDerivedFrom ());
             qualifiedRelation = Term.qualifiedRevision;
         }
         // Potentially non-binary relations
         if (description instanceof ActedOnBehalfOf) {
             serialise (((ActedOnBehalfOf) description).getActer (), Term.actedOnBehalfOf, ((ActedOnBehalfOf) description).getOnBehalfOf ());
             if (isMinimal (description, document, ((ActedOnBehalfOf) description).getActivity ())) {
                 return;
             }
             serialise (((ActedOnBehalfOf) description).getActer (), Term.qualifiedDelegation, description);
             serialise (description, Term.agent, ((ActedOnBehalfOf) description).getOnBehalfOf ());
             serialise (description, Term.activity, ((ActedOnBehalfOf) description).getActivity ());
         }
         if (description instanceof Used) {
             serialise (((Used) description).getUser (), Term.used, ((Used) description).getUsed ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise (((Used) description).getUser (), Term.qualifiedUsage, description);
             serialise (description, Term.entity, ((Used) description).getUsed ());
         }
         if (description instanceof WasAssociatedWith) {
             serialise (((WasAssociatedWith) description).getResponsibleFor (), Term.wasAssociatedWith, ((WasAssociatedWith) description).getResponsible ());
            if (isMinimal (description, document, ((WasAssociatedWith) description).getPlan (), ((WasAssociatedWith) description).getRole ())) {
                 return;
             }
             serialise (((WasAssociatedWith) description).getResponsibleFor (), Term.qualifiedAssociation, description);
             serialise (description, Term.agent, ((WasAssociatedWith) description).getResponsible ());
             serialise (description, Term.hadPlan, ((WasAssociatedWith) description).getPlan ());
             serialise (description, Term.hadRole, ((WasAssociatedWith) description).getRole ());
         }
         if (description instanceof WasAttributedTo) {
             serialise (((WasAttributedTo) description).getAttributed (), Term.wasAttributedTo, ((WasAttributedTo) description).getAttributedTo ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise (((WasAttributedTo) description).getAttributed (), Term.qualifiedAttribution, description);
             serialise (description, Term.agent, ((WasAttributedTo) description).getAttributedTo ());
         }
         if (description instanceof WasDerivedFrom) {
             // If a more specialised term has not been already recorded, then record the binary derived relationx
             if (qualifiedRelation == Term.qualifiedDerivation) {
                 serialise (((WasDerivedFrom) description).getDerived (), Term.wasDerivedFrom, ((WasDerivedFrom) description).getDerivedFrom ());
             }
             if (isMinimal (description, document, ((WasDerivedFrom) description).getDeriver (),
                            ((WasDerivedFrom) description).getGeneration (), ((WasDerivedFrom) description).getUsage ())) {
                 return;
             }
             serialise (((WasDerivedFrom) description).getDerived (), qualifiedRelation, description);
             serialise (description, Term.entity, ((WasDerivedFrom) description).getDerivedFrom ());
             serialise (description, Term.activity, ((WasDerivedFrom) description).getDeriver ());
             serialise (description, Term.hadGeneration, ((WasDerivedFrom) description).getGeneration ());
             serialise (description, Term.hadUsage, ((WasDerivedFrom) description).getUsage ());
         }
         if (description instanceof WasEndedBy) {
             serialise (((WasEndedBy) description).getEnded (), Term.wasEndedBy, ((WasEndedBy) description).getTrigger ());
             if (isMinimal (description, document, ((WasEndedBy) description).getEnder ())) {
                 return;
             }
             serialise (((WasEndedBy) description).getEnded (), Term.qualifiedEnd, description);
             serialise (description, Term.entity, ((WasEndedBy) description).getTrigger ());
             serialise (description, Term.activity, ((WasEndedBy) description).getEnder ());
         }
         if (description instanceof WasGeneratedBy) {
             serialise (((WasGeneratedBy) description).getGenerated (), Term.wasGeneratedBy, ((WasGeneratedBy) description).getGenerater ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise (((WasGeneratedBy) description).getGenerated (), Term.qualifiedGeneration, description);
             serialise (description, Term.activity, ((WasGeneratedBy) description).getGenerater ());
         }
         if (description instanceof WasInfluencedBy) {
             serialise ((Description) ((WasInfluencedBy) description).getInfluenced (), Term.wasInfluencedBy, (Description) ((WasInfluencedBy) description).getInfluencer ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise ((Description) ((WasInfluencedBy) description).getInfluenced (), Term.qualifiedInfluence, description);
             serialise (description, Term.influencer, (Description) ((WasInfluencedBy) description).getInfluencer ());
         }
         if (description instanceof WasInformedBy) {
             serialise (((WasInformedBy) description).getInformed (), Term.wasInformedBy, ((WasInformedBy) description).getInformer ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise (((WasInformedBy) description).getInformed (), Term.qualifiedCommunication, description);
             serialise (description, Term.activity, ((WasInformedBy) description).getInformer ());
         }
         if (description instanceof WasInvalidatedBy) {
             serialise (((WasInvalidatedBy) description).getInvalidated (), Term.wasInvalidatedBy, ((WasInvalidatedBy) description).getInvalidater ());
             if (isMinimal (description, document)) {
                 return;
             }
             serialise (((WasInvalidatedBy) description).getInvalidated (), Term.qualifiedInvalidation, description);
             serialise (description, Term.activity, ((WasInvalidatedBy) description).getInvalidater ());
         }
         if (description instanceof WasStartedBy) {
             serialise (((WasStartedBy) description).getStarted (), Term.wasStartedBy, ((WasStartedBy) description).getTrigger ());
             if (isMinimal (description, document, ((WasStartedBy) description).getStarter ())) {
                 return;
             }
             serialise (((WasStartedBy) description).getStarted (), Term.qualifiedStart, description);
             serialise (description, Term.entity, ((WasStartedBy) description).getTrigger ());
             serialise (description, Term.activity, ((WasStartedBy) description).getStarter ());
         }
         // Record event timestamps, roles and locations
         if (description instanceof InstantaneousEvent) {
             serialiseLiteral (description, Term.atTime, ((InstantaneousEvent) description).getTime ());
             serialise (description, Term.atLocation, ((InstantaneousEvent) description).getLocation ());
             serialise (description, Term.hadRole, ((InstantaneousEvent) description).getRole ());
         }
         // Record type information
         if (description instanceof Identified) {
             for (Term type : ((Identified) description).getClassTerms ()) {
                 type (description, type);
             }
         }
         // Record attributes
         if (description instanceof AttributeHolder) {
             for (Attribute attribute : ((AttributeHolder) description).getAttributes ()) {
                 if (attribute.getKey () instanceof URI) {
                     if (attribute.getValue () instanceof URI) {
                         if (attribute.getValue ().equals (Term.role.uri ()) || attribute.getValue ().equals (Term.hadRole.uri ())) {
                             if (!rolesAndLocations.contains ((URI) attribute.getValue ())) {
                                 fire ((URI) attribute.getValue (), RDF.typeURI (), Term.Role.uri ());
                                 rolesAndLocations.add ((URI) attribute.getValue ());
                             }
                             serialise (description, Term.hadRole.uri (), (URI) attribute.getValue ());
                             continue;
                         }
                         if (attribute.getValue ().equals (Term.location.uri ()) || attribute.getValue ().equals (Term.atLocation.uri ())) {
                             if (!rolesAndLocations.contains ((URI) attribute.getValue ())) {
                                 fire ((URI) attribute.getValue (), RDF.typeURI (), Term.Location.uri ());
                                 rolesAndLocations.add ((URI) attribute.getValue ());
                             }
                             serialise (description, Term.atLocation.uri (), (URI) attribute.getValue ());
                             continue;
                         }
                         serialise (description, (URI) attribute.getKey (), (URI) attribute.getValue ());
                     } else {
                         serialiseLiteral (description, (URI) attribute.getKey (), attribute.getValue ());
                     }
                 }
             }
         }
     }
 
     /**
      * Serialises a relation between two descriptions
      * 
      * @param subject The subject description
      * @param predicate The relation
      * @param object The object description
      */
     private void serialise (Description subject, Term predicate, Description object) {
         URI objectID;
 
         if (subject == null || object == null) {
             return;
         }
         objectID = uriID (object);
         if (objectID == null) {
             serialise (subject, predicate.uri (), blank (object));
         } else {
             serialise (subject, predicate.uri (), objectID);
         }
     }
 
     /**
      * Serialises a relation between a descriptions and a URI resource.
      * 
      * @param subject The subject description
      * @param predicate The relation
      * @param objectID The object URI
      */
     private void serialise (Description subject, URI predicate, URI objectID) {
         URI subjectID;
 
         if (subject == null || objectID == null) {
             return;
         }
         subjectID = uriID (subject);
         if (subjectID == null) {
             fire (blank (subject), predicate, objectID);
         } else {
             fire (subjectID, predicate, objectID);
         }
     }
 
     /**
      * Serialises a relation between a descriptions and a blank node resource.
      * 
      * @param subject The subject description
      * @param predicate The relation
      * @param blankObject The blank node object resource
      */
     private void serialise (Description subject, URI predicate, String blankObject) {
         URI subjectID;
 
         if (subject == null || blankObject == null) {
             return;
         }
         subjectID = uriID (subject);
         if (subjectID == null) {
             fire (blank (subject), predicate, blankObject);
         } else {
             fire (subjectID, predicate, blankObject);
         }
     }
 
     /**
      * Serialises a relation between a descriptions and a literal resource.
      * 
      * @param subject The subject description
      * @param predicate The relation, identified as a Term
      * @param blankObject The blank node object resource
      */
     private void serialiseLiteral (Description subject, Term predicate, Object object) {
         serialiseLiteral (subject, predicate.uri (), object);
     }
 
     /**
      * Serialises a relation between a descriptions and a literal resource.
      * 
      * @param subject The subject description
      * @param predicate The relation, identified as a URI
      * @param blankObject The blank node object resource
      */
     private void serialiseLiteral (Description subject, URI predicate, Object object) {
         URI subjectID;
 
         if (subject == null || object == null) {
             return;
         }
         subjectID = uriID (subject);
         if (subjectID == null) {
             fire (blank (subject), predicate, new Literal (object));
         } else {
             fire (subjectID, predicate, new Literal (object));
         }
     }
 
     /**
      * Records a triple recording the type of a given description.
      * 
      * @param subject The description whose type to assert
      * @param term The type, as a Term
      */
     private void type (Description subject, Term term) {
         URI id = uriID (subject);
 
         if (id == null) {
             fire (blank (subject), RDF.typeURI (), term.uri ());
         } else {
             fire (id, RDF.typeURI (), term.uri ());
         }
     }
 
     /**
      * Records a triple recording the type of a given blank node resource.
      * 
      * @param blank The blank node resource whose type to assert
      * @param term The type, as a Term
      */
     private void type (String blank, Term term) {
         fire (blank, RDF.typeURI (), term.uri ());
     }
 
     /**
      * Returns the URI identifier of the given description, if one has been set,
      * or else null.
      * 
      * @param identified The description with the identifier
      * @return The description's URI identifier or else null
      */
     private URI uriID (Description identified) {
         Object id;
 
         if (identified instanceof Identified) {
             id = ((Identified) identified).getIdentifier ();
             if (id instanceof URI) {
                 return (URI) id;
             }
         }
         return null;
     }
 }
