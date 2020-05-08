 package uk.ac.kcl.inf.provoking.builder;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.xml.bind.DatatypeConverter;
 import uk.ac.kcl.inf.provoking.model.*;
 import uk.ac.kcl.inf.provoking.model.util.AttributeHolder;
 import uk.ac.kcl.inf.provoking.model.util.IDGenerator;
 import uk.ac.kcl.inf.provoking.model.util.Identified;
 import uk.ac.kcl.inf.provoking.model.util.Influenceable;
 import uk.ac.kcl.inf.provoking.model.util.Term;
 import uk.ac.kcl.inf.provoking.model.util.UniqueIDGenerator;
 import uk.ac.kcl.inf.provoking.model.util.UniqueURIGenerator;
 import uk.ac.kcl.inf.provoking.serialise.ProvConstructer;
 import uk.ac.kcl.inf.provoking.serialise.SerialisationHint;
 import static uk.ac.kcl.inf.provoking.serialise.SerialisationHintType.*;
 
 /**
  * A builder for iteratively and succinctly creating PROV documents.
  * 
  * @author Simon Miles
  */
 public class ProvBuilder {
     private Document _document;
     private Description _current, _prior;
     private List<AbbreviationResolver> _resolvers;
     private Map<String, Description> _bookmarks;
     private IDGenerator _idgen;
     private boolean _bookmarksAsLabels;
     private boolean _nameRelations;
 
     public ProvBuilder (Document addToExisting, IDGenerator idgen) {
         _document = addToExisting;
         _current = null;
         _prior = null;
         _resolvers = new LinkedList<> ();
         _bookmarks = new HashMap<> ();
         _idgen = idgen;
         _bookmarksAsLabels = false;
         _nameRelations = false;
         setPrefix ("prov:", Term.PROV_NS);
         recogniseURIScheme ("http");
         recogniseURIScheme ("mailto");
         recogniseURIScheme ("urn");
     }
 
     public ProvBuilder (IDGenerator idgen) {
         this (new Document (), idgen);
     }
 
     public ProvBuilder (Document addToExisting, String idStart) {
         this (addToExisting, new UniqueIDGenerator (idStart));
     }
 
     public ProvBuilder (String idStart) {
         this (new UniqueIDGenerator (idStart));
     }
 
     public ProvBuilder (String prefix, String idURIBase) {
         this (new UniqueURIGenerator (idURIBase));
         setPrefix (prefix, idURIBase);
     }
 
     public ProvBuilder (Document addToExisting) {
         this (addToExisting, "");
     }
 
     public ProvBuilder () {
         this ("");
     }
 
     public ProvBuilder actedOnBehalfOf (String... attributes) throws ProvBuildException {
         return store (new ActedOnBehalfOf (idGenRel ("ActedOnBehalfOf", attributes), getAgent ("actedOnBehalfOf"), null), true, attributes);
     }
 
     public ProvBuilder activity (Date started, Date ended, String... attributes) throws ProvBuildException {
         return addActivity (new Activity (idGen ("Activity", attributes), started, ended), true, attributes);
     }
 
     public ProvBuilder activity (String... attributes) throws ProvBuildException {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Activity)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not an activity.");
                 }
                 return addActivity ((Activity) bookmarked, false, attributes);
             }
         }
         return activity ((Date) null, (Date) null, attributes);
     }
 
     public ProvBuilder activityRef (Object identifier) throws ProvBuildException {
         return addActivity (Activity.reference (identifier), false);
     }
 
     private ProvBuilder addActivity (Activity activity, boolean isNew, String... attributes) throws ProvBuildException {
         store (activity, isNew, attributes);
         if (_prior != null) {
             ProvConstructer.setActivity (_prior, activity);
         }
         return this;
     }
 
     private ProvBuilder addAgent (Agent agent, boolean isNew, String... attributes) throws ProvBuildException {
         store (agent, isNew, attributes);
         if (_prior != null) {
             ProvConstructer.setAgent (_prior, agent);
         }
         return this;
     }
 
     private void addAttributes (AttributeHolder addTo, boolean isNew, String... attributes) throws ProvBuildException {
         int equals;
         String keyText, valueText;
         Object key, value;
 
         for (String attribute : attributes) {
             equals = attribute.indexOf ('=');
             if (equals < 0) {
                 if (isNew) {
                     _bookmarks.put (attribute, (Description) addTo);
                     if (_bookmarksAsLabels) {
                         addTo.addAttribute (Term.label.uri (), attribute);
                     }
                 }
                 continue;
             }
             valueText = attribute.substring (equals + 1).trim ();
             value = resolve (valueText);
             keyText = attribute.substring (0, equals).trim ();
             if (keyText.trim ().equals ("")) {
                 id (value);
             } else {
                 key = resolve (keyText);
                 addTo.addAttribute (key, value);
             }
         }
     }
 
     private ProvBuilder addEntity (Entity entity, boolean isNew, String... attributes) throws ProvBuildException {
         store (entity, isNew, attributes);
         if (_prior != null) {
             ProvConstructer.setEntity (_prior, entity);
         }
         return this;
     }
 
     private ProvBuilder addLocation (Location location, boolean isNew, String... attributes) throws ProvBuildException {
         store (location, isNew, attributes);
         if (_prior != null) {
             ProvConstructer.setLocation (_prior, location);
         }
         return this;
     }
 
     public ProvBuilder addResolver (AbbreviationResolver resolver) {
         _resolvers.add (resolver);
         return this;
     }
 
     private ProvBuilder addRole (Role role, boolean isNew, String... attributes) throws ProvBuildException {
         store (role, isNew, attributes);
         if (_prior != null) {
             ProvConstructer.setRole (_prior, role);
         }
         return this;
     }
 
     public ProvBuilder agent (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Agent)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not an agent.");
                 }
                 return addAgent ((Agent) bookmarked, false, attributes);
             }
         }
         return addAgent (new Agent (idGen ("Agent", attributes)), true, attributes);
     }
 
     public ProvBuilder agentRef (Object identifier) {
         return addAgent (Agent.reference (identifier), false);
     }
 
     public ProvBuilder alternateOf () throws ProvBuildException {
         return store (new AlternateOf (getEntity ("alternateOf"), null), true);
     }
 
     public ProvBuilder and () {
         _prior = _current;
         _current = null;
         return this;
     }
 
     public Document build () {
         return _document;
     }
 
     public Bundle buildBundle (Object identifier) {
         return new Bundle (identifier, _document);
     }
     
     public ProvBuilder bundle (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Bundle)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not a bundle.");
                 }
                 return addEntity ((Bundle) bookmarked, false, attributes);
             }
         }
         return addEntity (new Bundle (idGen ("Bundle", attributes)), true, attributes);
     }
 
     public ProvBuilder collection (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Collection)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not a collection.");
                 }
                 return addEntity ((Collection) bookmarked, false, attributes);
             }
         }
         return addEntity (new Collection (idGen ("Collection", attributes)), true, attributes);
     }
         
     public ProvBuilder emptyCollection (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof EmptyCollection)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not an empty collection.");
                 }
                 return addEntity ((EmptyCollection) bookmarked, false, attributes);
             }
         }
         return addEntity (new EmptyCollection (idGen ("EmptyCollection", attributes)), true, attributes);
     }
 
     public ProvBuilder entity (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Entity)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not an entity.");
                 }
                 return addEntity ((Entity) bookmarked, false, attributes);
             }
         }
         return addEntity (new Entity (idGen ("Entity", attributes)), true, attributes);
     }
 
     private Activity getActivity (String edgeType) throws ProvBuildException {
         if (_current == null || !(_current instanceof Activity)) {
             throw new ProvBuildException (edgeType + " only applies to activities");
         }
         return (Activity) _current;
     }
 
     private Agent getAgent (String edgeType) throws ProvBuildException {
         if (_current == null || !(_current instanceof Agent)) {
             throw new ProvBuildException (edgeType + " only applies to agents");
         }
         return (Agent) _current;
     }
 
     private Entity getEntity (String edgeType) throws ProvBuildException {
         if (_current == null || !(_current instanceof Entity)) {
             throw new ProvBuildException (edgeType + " only applies to entities");
         }
         return (Entity) _current;
     }
 
     private Influenceable getInfluenceable (String edgeType) throws ProvBuildException {
         if (_current == null || !(_current instanceof Influenceable)) {
             throw new ProvBuildException (edgeType + " only applies to activites, agents or entities");
         }
         return (Influenceable) _current;
     }
 
     public ProvBuilder hadMember () throws ProvBuildException {
         return store (new HadMember (getEntity ("hadMember"), null), true);
     }
 
     public ProvBuilder hadPrimarySource (String... attributes) throws ProvBuildException {
         return store (new HadPrimarySource (idGenRel ("HadPrimarySource", attributes), getEntity ("hadPrimarySource"), null), true);
     }
 
     public ProvBuilder id (Object identifier) throws ProvBuildException {
         if (_current == null || !(_current instanceof Identified)) {
             throw new ProvBuildException ("IDs can only be applied to identifiable descriptions: " + identifier);
         }
         ((Identified) _current).setIdentifier (identifier);
         _document.addSerialisationHint (new SerialisationHint (explicitlyIdentified, _current));
         return this;
     }
 
     public ProvBuilder idAbbr (String identifierAbbreviation) throws ProvBuildException {
         return id (resolve (identifierAbbreviation));
     }
     
     private Object idGen (String descriptive, String... attributes) {
         for (String attribute : attributes) {
             if (attribute.indexOf ('=') < 0 && !attribute.startsWith ("*")) {
                 return _idgen.generateID (attribute);
             }
         }
         if (descriptive == null) {
             return null;
         } else {
             return _idgen.generateID (descriptive);
         }
     }
     
     private Object idGenRel (String descriptive, String... attributes) {
         if (_nameRelations) {
             return idGen (descriptive, attributes);
         } else {
             return null;
         }
     }
 
    /**
     * Set the location of the activity, agent, entity or event just built.
     * 
     * @param id The location string (possibly an abbreviation such as a resolveable qualified name)
     * @param attributes The attributes to annotate to the location
     * @return This builder
     */
     public ProvBuilder location (String id, String... attributes) {
         Description bookmarked;
         Location newLocation;
 
         bookmarked = _bookmarks.get (id);
         if (bookmarked != null) {
            if (!(bookmarked instanceof Location)) {
                throw new ProvBuildException ("Reference " + id + " is not a location.");
             }
             return addLocation ((Location) bookmarked, false, attributes);
         }
         newLocation = new Location (resolve (id));
         _bookmarks.put (id, newLocation);

         return addLocation (newLocation, true, attributes);
     }
 
     public ProvBuilder organization (String... attributes) {
         Description bookmarked;
         
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Organization)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not an organization.");
                 }
                 return addAgent ((Organization) bookmarked, false, attributes);
             }
         }
         return addAgent (new Organization (idGen ("Organization", attributes)), true, attributes);
     }
 
     public ProvBuilder person (String... attributes) {
         Description bookmarked;
         
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Person)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not a person.");
                 }
                 return addAgent ((Person) bookmarked, false, attributes);
             }
         }
         return addAgent (new Person (idGen ("Person", attributes)), true, attributes);
     }
 
     public ProvBuilder plan (String... attributes) {
         Description bookmarked;
 
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof Plan)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not a plan.");
                 }
                 return addEntity ((Plan) bookmarked, false, attributes);
             }
         }
         return addEntity (new Plan (idGen ("Plan", attributes)), true, attributes);
     }
     
     public void recogniseURIScheme (String scheme) {
         _resolvers.add (new URISchemeResolver (scheme));
     }
 
     private Object resolve (String text) {
         for (AbbreviationResolver resolver : _resolvers) {
             if (resolver.appliesTo (text)) {
                 return resolver.resolve (text);
             }
         }
         return text;
     }
 
     public ProvBuilder role (String id, String... attributes) {
         Description bookmarked;
         Role newRole;
 
         bookmarked = _bookmarks.get (id);
         if (bookmarked != null) {
             if (!(bookmarked instanceof Role)) {
                 throw new ProvBuildException ("Reference " + id + " is not a role.");
             }
             return addRole ((Role) bookmarked, false, attributes);
         }
         newRole = new Role (resolve (id));
         _bookmarks.put (id, newRole);
 
         return addRole (newRole, true, attributes);
     }
 
     public ProvBuilder setBookmarksAsLabels (boolean bookmarksAsLabels) {
         _bookmarksAsLabels = bookmarksAsLabels;
         return this;
     }
 
     public ProvBuilder setNameRelations (boolean nameRelations) {
         _nameRelations = nameRelations;
         return this;
     }
     
     public ProvBuilder setPrefix (String prefix, String vocabularyURI) {
         addResolver (new URIPrefixResolver (prefix, vocabularyURI));
         _document.addSerialisationHint (new SerialisationHint (namespacePrefix, prefix, vocabularyURI));
         return this;
     }
 
     public ProvBuilder softwareAgent (String... attributes) {
         Description bookmarked;
         
         if (attributes.length >= 1) {
             bookmarked = _bookmarks.get (attributes[0]);
             if (bookmarked != null) {
                 if (!(bookmarked instanceof SoftwareAgent)) {
                     throw new ProvBuildException ("Bookmark reference " + attributes[0] + " is not a software agent.");
                 }
                 return addAgent ((SoftwareAgent) bookmarked, false, attributes);
             }
         }
         return addAgent (new SoftwareAgent (idGen ("SoftwareAgent", attributes)), true, attributes);
     }
 
     public ProvBuilder specializationOf () throws ProvBuildException {
         return store (new SpecializationOf (getEntity ("specializationOf"), null), true);
     }
 
     public ProvBuilder startNew () {
         _document = new Document ();
         _bookmarks.clear ();
         return this;
     }
 
     private ProvBuilder store (Description item, boolean isNew, String... attributes) throws ProvBuildException {
         if (_document == null) {
             throw new ProvBuildException ("Must start a document (or bundle) before adding to it");
         }
         if (item instanceof AttributeHolder) {
             addAttributes ((AttributeHolder) item, isNew, attributes);
         }
         if (isNew) {
             _document.add (item);
         }
         _prior = _current;
         _current = item;
         return this;
     }
 
     public ProvBuilder used (Date time, String... attributes) throws ProvBuildException {
         store (new Used (idGenRel ("Used", attributes), getActivity ("used"), null, time), true, attributes);
         if (_prior != null) {
             if (_prior instanceof WasDerivedFrom) {
                 ((WasDerivedFrom) _prior).setUsage ((Used) _current);
             }
         }
 
         return this;
     }
 
     public ProvBuilder used (String... attributes) throws ProvBuildException {
         return used ((Date) null, attributes);
     }
 
     public ProvBuilder wasAssociatedWith (String... attributes) throws ProvBuildException {
         return store (new WasAssociatedWith (idGenRel ("WasAssociatedWith", attributes), getActivity ("wasAssociatedWith"), null), true, attributes);
     }
 
     public ProvBuilder wasAttributedTo (String... attributes) throws ProvBuildException {
         return store (new WasAttributedTo (idGenRel ("WasAttributedTo", attributes), getEntity ("wasAttributedTo"), null), true, attributes);
     }
 
     public ProvBuilder wasDerivedFrom (String... attributes) throws ProvBuildException {
         return store (new WasDerivedFrom (idGenRel ("WasDerivedFrom", attributes), getEntity ("wasDerivedFrom"), null), true, attributes);
     }
 
     public ProvBuilder wasEndedBy (Date time, String... attributes) throws ProvBuildException {
         return store (new WasEndedBy (idGenRel ("WasEndedBy", attributes), getActivity ("wasEndedBy"), (Entity) null, (Activity) null, time), true, attributes);
     }
 
     public ProvBuilder wasEndedBy (String... attributes) throws ProvBuildException {
         return wasEndedBy ((Date) null, attributes);
     }
 
     public ProvBuilder wasGeneratedBy (Date time, String... attributes) throws ProvBuildException {
         store (new WasGeneratedBy (idGenRel ("WasGeneratedBy", attributes), getEntity ("wasGeneratedBy"), null, time), true, attributes);
         if (_prior != null) {
             if (_prior instanceof WasDerivedFrom) {
                 ((WasDerivedFrom) _prior).setGeneration ((WasGeneratedBy) _current);
             }
         }
 
         return this;
     }
 
     public ProvBuilder wasGeneratedBy (String... attributes) throws ProvBuildException {
         return wasGeneratedBy ((Date) null, attributes);
     }
 
     public ProvBuilder wasInfluencedBy (String... attributes) throws ProvBuildException {
         return store (new WasInfluencedBy (idGenRel ("WasInfluencedBy", attributes), getInfluenceable ("wasInfluencedBy"), null), true, attributes);
     }
 
     public ProvBuilder wasInformedBy (String... attributes) throws ProvBuildException {
         return store (new WasInformedBy (idGenRel ("WasInformedBy", attributes), getActivity ("wasInformedBy"), null), true, attributes);
     }
 
     public ProvBuilder wasInvalidatedBy (Date time, String... attributes) throws ProvBuildException {
         return store (new WasInvalidatedBy (idGenRel ("WasInvalidatedBy", attributes), getEntity ("wasInvalidatedBy"), (Activity) null, time), true, attributes);
     }
 
     public ProvBuilder wasInvalidatedBy (String... attributes) throws ProvBuildException {
         return wasInvalidatedBy ((Date) null, attributes);
     }
 
     public ProvBuilder wasQuotedFrom (String... attributes) throws ProvBuildException {
         return store (new WasQuotedFrom (getEntity ("wasQuotationFrom"), null), true, attributes);
     }
 
     public ProvBuilder wasRevisionOf (String... attributes) throws ProvBuildException {
         return store (new WasRevisionOf (idGenRel ("WasRevisionOf", attributes), getEntity ("wasRevisionOf"), null), true, attributes);
     }
 
     public ProvBuilder wasStartedBy (Date time, String... attributes) throws ProvBuildException {
         return store (new WasStartedBy (idGenRel ("WasStartedBy", attributes), getActivity ("wasStartedBy"), (Entity) null, (Activity) null, time), true, attributes);
     }
 
     public ProvBuilder wasStartedBy (String... attributes) throws ProvBuildException {
         return wasStartedBy ((Date) null, attributes);
     }
 
     public ProvBuilder where (String bookmark) {
         Description bookmarked;
 
         bookmarked = _bookmarks.get (bookmark);
         if (bookmarked == null) {
             throw new ProvBuildException ("Bookmark reference not recognised: " + bookmark);
         }
         _prior = _current;
         _current = bookmarked;
 
         return this;
     }
 
     public Date xsdDate (String xsdDateTimeString) {
         return DatatypeConverter.parseDateTime (xsdDateTimeString).getTime ();
     }
 }
