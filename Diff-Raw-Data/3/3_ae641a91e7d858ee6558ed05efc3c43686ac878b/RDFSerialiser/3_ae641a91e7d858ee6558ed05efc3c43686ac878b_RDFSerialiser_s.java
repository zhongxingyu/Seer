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
 import uk.ac.kcl.inf.provoking.model.WasInformedBy;
 import uk.ac.kcl.inf.provoking.model.WasInvalidatedBy;
 import uk.ac.kcl.inf.provoking.model.WasQuotedFrom;
 import uk.ac.kcl.inf.provoking.model.WasRevisionOf;
 import uk.ac.kcl.inf.provoking.model.WasStartedBy;
 import uk.ac.kcl.inf.provoking.model.util.AttributeHolder;
 import uk.ac.kcl.inf.provoking.model.util.Identified;
 import uk.ac.kcl.inf.provoking.model.util.Term;
 import uk.ac.kcl.inf.provoking.serialise.SerialisationHint;
 
 public class RDFSerialiser {
     private static int _lastBlank = 0;
     private List<TriplesListener> _listeners;
     private Map<Description, String> _blankIDs;
 
     public RDFSerialiser (TriplesListener... listeners) {
         _listeners = new LinkedList<> (Arrays.asList (listeners));
         _blankIDs = new HashMap<> ();
     }
 
     private String blank (Description description) {
         String blank = _blankIDs.get (description);
 
         if (blank == null) {
             _lastBlank += 1;
             blank = "_:b" + _lastBlank;
             _blankIDs.put (description, blank);
         }
 
         return blank;
     }
     
     private void fire (URI subject, URI predicate, Literal object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, object);
         }
     }
 
     private void fire (URI subject, URI predicate, URI object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, object);
         }
     }
 
     private void fire (URI subject, URI predicate, String blankObject) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (subject, predicate, blankObject);
         }
     }
 
     private void fire (String blankSubject, URI predicate, Literal object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, object);
         }
     }
 
     private void fire (String blankSubject, URI predicate, URI object) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, object);
         }
     }
 
     private void fire (String blankSubject, URI predicate, String blankObject) {
         predicate = provnToProvo (predicate);
         for (TriplesListener listener : _listeners) {
             listener.triple (blankSubject, predicate, blankObject);
         }
     }
 
     /**
      * Returns true if the given description can be expressed by a binary, rather than qualified, relation
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
 
     private URI provnToProvo (URI predicate) {
         if (predicate.equals (Term.type.uri ())) {
             return RDF.typeURI ();
         }
        if (predicate.equals (Term.value.uri ())) {
            return RDF.valueURI ();
        }
         if (predicate.equals (Term.label.uri ())) {
             return RDF.labelURI ();
         }
         return predicate;
     }
 
     public void serialise (Document document) {
         List<URI> rolesAndLocations = new LinkedList<> ();
         
         for (Description description : document) {
             serialise (description, document, rolesAndLocations);
         }
     }
 
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
             if (isMinimal (description, document, ((WasAssociatedWith) description).getPlan ())) {
                 return;
             }
             serialise (((WasAssociatedWith) description).getResponsibleFor (), Term.qualifiedAssociation, description);
             serialise (description, Term.agent, ((WasAssociatedWith) description).getResponsible ());
             serialise (description, Term.hadPlan, ((WasAssociatedWith) description).getPlan ());
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
             serialise (((WasDerivedFrom) description).getDerived (), Term.wasDerivedFrom, ((WasDerivedFrom) description).getDerivedFrom ());
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
 
     private void serialiseLiteral (Description subject, Term predicate, Object object) {
         serialiseLiteral (subject, predicate.uri (), object);
     }
 
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
 
     private void type (Description subject, Term term) {
         URI id = uriID (subject);
 
         if (id == null) {
             fire (blank (subject), RDF.typeURI (), term.uri ());
         } else {
             fire (id, RDF.typeURI (), term.uri ());
         }
     }
 
     private void type (String blank, Term term) {
         fire (blank, RDF.typeURI (), term.uri ());
     }
 
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
