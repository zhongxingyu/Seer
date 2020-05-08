 package org.geogit.web.api;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.annotation.Nullable;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.jettison.AbstractXMLStreamWriter;
 import org.geogit.api.CommandLocator;
 import org.geogit.api.FeatureBuilder;
 import org.geogit.api.FeatureInfo;
 import org.geogit.api.GeogitSimpleFeature;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
 import org.geogit.api.Ref;
 import org.geogit.api.Remote;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevFeature;
 import org.geogit.api.RevFeatureBuilder;
 import org.geogit.api.RevFeatureType;
 import org.geogit.api.RevObject;
 import org.geogit.api.RevPerson;
 import org.geogit.api.RevTag;
 import org.geogit.api.RevTree;
 import org.geogit.api.SymRef;
 import org.geogit.api.plumbing.DiffIndex;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.FindTreeChild;
 import org.geogit.api.plumbing.RevObjectParse;
 import org.geogit.api.plumbing.diff.AttributeDiff;
 import org.geogit.api.plumbing.diff.AttributeDiff.TYPE;
 import org.geogit.api.plumbing.diff.DiffEntry;
 import org.geogit.api.plumbing.diff.DiffEntry.ChangeType;
 import org.geogit.api.plumbing.merge.Conflict;
 import org.geogit.api.plumbing.merge.MergeScenarioReport;
 import org.geogit.api.porcelain.FetchResult;
 import org.geogit.api.porcelain.FetchResult.ChangedRef;
 import org.geogit.api.porcelain.PullResult;
import org.geogit.storage.FieldType;
 import org.geogit.web.api.commands.BranchWebOp;
 import org.geogit.web.api.commands.LsTree;
 import org.geogit.web.api.commands.RefParseWeb;
 import org.geogit.web.api.commands.RemoteWebOp;
 import org.geogit.web.api.commands.TagWebOp;
 import org.geogit.web.api.commands.UpdateRefWeb;
 import org.geotools.referencing.CRS;
 import org.opengis.feature.type.GeometryType;
 import org.opengis.feature.type.PropertyDescriptor;
 import org.opengis.feature.type.PropertyType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterators;
 import com.vividsolutions.jts.geom.Geometry;
 
 /**
  * Provides a wrapper for writing common GeoGit objects to a provided {@link XMLStreamWriter}.
  */
 public class ResponseWriter {
 
     protected final XMLStreamWriter out;
 
     /**
      * Constructs a new {code ResponseWriter} with the given {@link XMLStreamWriter}.
      * 
      * @param out the output stream to write to
      */
     public ResponseWriter(XMLStreamWriter out) {
         this.out = out;
         if (out instanceof AbstractXMLStreamWriter) {
             configureJSONOutput((AbstractXMLStreamWriter) out);
         }
     }
 
     private void configureJSONOutput(AbstractXMLStreamWriter out) {
     }
 
     /**
      * Ends the document stream.
      * 
      * @throws XMLStreamException
      */
     public void finish() throws XMLStreamException {
         out.writeEndElement(); // results
         out.writeEndDocument();
     }
 
     /**
      * Begins the document stream.
      * 
      * @throws XMLStreamException
      */
     public void start() throws XMLStreamException {
         start(true);
     }
 
     /**
      * Begins the document stream with the provided success flag.
      * 
      * @param success whether or not the operation was successful
      * @throws XMLStreamException
      */
     public void start(boolean success) throws XMLStreamException {
         out.writeStartDocument();
         out.writeStartElement("response");
         writeElement("success", Boolean.toString(success));
     }
 
     /**
      * Writes the given header elements to the stream. The array should be organized into key/value
      * pairs. For example {@code [key, value, key, value]}.
      * 
      * @param els the elements to write
      * @throws XMLStreamException
      */
     public void writeHeaderElements(String... els) throws XMLStreamException {
         out.writeStartElement("header");
         for (int i = 0; i < els.length; i += 2) {
             writeElement(els[i], els[i + 1]);
         }
         out.writeEndElement();
     }
 
     /**
      * Writes the given error elements to the stream. The array should be organized into key/value
      * pairs. For example {@code [key, value, key, value]}.
      * 
      * @param errors the errors to write
      * @throws XMLStreamException
      */
     public void writeErrors(String... errors) throws XMLStreamException {
         out.writeStartElement("errors");
         for (int i = 0; i < errors.length; i += 2) {
             writeElement(errors[i], errors[i + 1]);
         }
         out.writeEndElement();
     }
 
     /**
      * @return the {@link XMLStreamWriter} for this instance
      */
     public XMLStreamWriter getWriter() {
         return out;
     }
 
     /**
      * Writes the given element to the stream.
      * 
      * @param element the element name
      * @param content the element content
      * @throws XMLStreamException
      */
     public void writeElement(String element, @Nullable String content) throws XMLStreamException {
         out.writeStartElement(element);
         if (content != null) {
             out.writeCharacters(content);
         }
         out.writeEndElement();
     }
 
     /**
      * Writes staged changes to the stream.
      * 
      * @param setFilter the configured {@link DiffIndex} command
      * @param start the change number to start writing from
      * @param length the number of changes to write
      * @throws XMLStreamException
      */
     public void writeStaged(DiffIndex setFilter, int start, int length) throws XMLStreamException {
         writeDiffEntries("staged", start, length, setFilter.call());
     }
 
     /**
      * Writes unstaged changes to the stream.
      * 
      * @param setFilter the configured {@link DiffWorkTree} command
      * @param start the change number to start writing from
      * @param length the number of changes to write
      * @throws XMLStreamException
      */
     public void writeUnstaged(DiffWorkTree setFilter, int start, int length)
             throws XMLStreamException {
         writeDiffEntries("unstaged", start, length, setFilter.call());
     }
 
     public void writeUnmerged(List<Conflict> conflicts, int start, int length)
             throws XMLStreamException {
         Iterator<Conflict> entries = conflicts.iterator();
 
         advance(entries, start);
         if (length < 0) {
             length = Integer.MAX_VALUE;
         }
         for (int i = 0; i < length && entries.hasNext(); i++) {
             Conflict entry = entries.next();
             out.writeStartElement("unmerged");
             writeElement("changeType", "CONFLICT");
             writeElement("path", entry.getPath());
             writeElement("ours", entry.getOurs().toString());
             writeElement("theirs", entry.getTheirs().toString());
             writeElement("ancestor", entry.getAncestor().toString());
             out.writeEndElement();
         }
     }
 
     @SuppressWarnings("rawtypes")
     private void advance(Iterator it, int cnt) {
         for (int i = 0; i < cnt && it.hasNext(); i++) {
             it.next();
         }
     }
 
     /**
      * Writes a set of {@link DiffEntry}s to the stream.
      * 
      * @param name the element name
      * @param start the change number to start writing from
      * @param length the number of changes to write
      * @param entries an iterator for the DiffEntries to write
      * @throws XMLStreamException
      */
     public void writeDiffEntries(String name, int start, int length, Iterator<DiffEntry> entries)
             throws XMLStreamException {
         advance(entries, start);
         if (length < 0) {
             length = Integer.MAX_VALUE;
         }
         int counter = 0;
         while (entries.hasNext() && counter < length) {
             DiffEntry entry = entries.next();
             out.writeStartElement(name);
             writeElement("changeType", entry.changeType().toString());
             NodeRef oldObject = entry.getOldObject();
             NodeRef newObject = entry.getNewObject();
             if (oldObject == null) {
                 writeElement("newPath", newObject.path());
                 writeElement("newObjectId", newObject.objectId().toString());
                 writeElement("path", "");
                 writeElement("oldObjectId", ObjectId.NULL.toString());
             } else if (newObject == null) {
                 writeElement("newPath", "");
                 writeElement("newObjectId", ObjectId.NULL.toString());
                 writeElement("path", oldObject.path());
                 writeElement("oldObjectId", oldObject.objectId().toString());
             } else {
                 writeElement("newPath", newObject.path());
                 writeElement("newObjectId", newObject.objectId().toString());
                 writeElement("path", oldObject.path());
                 writeElement("oldObjectId", oldObject.objectId().toString());
             }
             out.writeEndElement();
             counter++;
         }
         if (entries.hasNext()) {
             writeElement("nextPage", "true");
         }
     }
 
     /**
      * Writes a set of {@link RevCommit}s to the stream.
      * 
      * @param entries an iterator for the RevCommits to write
      * @param page the page number to write
      * @param elementsPerPage the number of commits per page
      * @throws XMLStreamException
      */
     public void writeCommits(Iterator<RevCommit> entries, int page, int elementsPerPage)
             throws XMLStreamException {
         advance(entries, page * elementsPerPage);
         int counter = 0;
         while (entries.hasNext() && counter < elementsPerPage) {
             RevCommit entry = entries.next();
             out.writeStartElement("commit");
             writeElement("id", entry.getId().toString());
             writeElement("tree", entry.getTreeId().toString());
 
             ImmutableList<ObjectId> parentIds = entry.getParentIds();
             out.writeStartElement("parents");
             for (ObjectId parentId : parentIds) {
                 writeElement("id", parentId.toString());
             }
             out.writeEndElement();
 
             writePerson("author", entry.getAuthor());
             writePerson("committer", entry.getCommitter());
 
             out.writeStartElement("message");
             if (entry.getMessage() != null) {
                 out.writeCData(entry.getMessage());
             }
             out.writeEndElement();
 
             out.writeEndElement();
             counter++;
         }
         if (entries.hasNext()) {
             writeElement("nextPage", "true");
         }
     }
 
     /**
      * Writes a {@link RevPerson} to the stream.
      * 
      * @param enclosingElement the element name
      * @param p the RevPerson to writes
      * @throws XMLStreamException
      */
     public void writePerson(String enclosingElement, RevPerson p) throws XMLStreamException {
         out.writeStartElement(enclosingElement);
         writeElement("name", p.getName().orNull());
         writeElement("email", p.getEmail().orNull());
         writeElement("timestamp", Long.toString(p.getTimestamp()));
         writeElement("timeZoneOffset", Long.toString(p.getTimeZoneOffset()));
         out.writeEndElement();
     }
 
     /**
      * Writes the response for the {@link Commit} command to the stream.
      * 
      * @param diff the changes returned from the command
      * @throws XMLStreamException
      */
     public void writeCommitResponse(Iterator<DiffEntry> diff) throws XMLStreamException {
         int adds = 0, deletes = 0, changes = 0;
         DiffEntry diffEntry;
         while (diff.hasNext()) {
             diffEntry = diff.next();
             switch (diffEntry.changeType()) {
             case ADDED:
                 ++adds;
                 break;
             case REMOVED:
                 ++deletes;
                 break;
             case MODIFIED:
                 ++changes;
                 break;
             }
         }
         writeElement("added", Integer.toString(adds));
         writeElement("changed", Integer.toString(changes));
         writeElement("deleted", Integer.toString(deletes));
     }
 
     /**
      * Writes the response for the {@link LsTree} command to the stream.
      * 
      * @param iter the iterator of {@link NodeRefs}
      * @param verbose if true, more detailed information about each node will be provided
      * @throws XMLStreamException
      */
     public void writeLsTreeResponse(Iterator<NodeRef> iter, boolean verbose)
             throws XMLStreamException {
 
         while (iter.hasNext()) {
             NodeRef node = iter.next();
             out.writeStartElement("node");
             writeElement("path", node.path());
             if (verbose) {
                 writeElement("metadataId", node.getMetadataId().toString());
                 writeElement("type", node.getType().toString().toLowerCase());
                 writeElement("objectId", node.objectId().toString());
             }
             out.writeEndElement();
         }
 
     }
 
     /**
      * Writes the response for the {@link UpdateRefWeb} command to the stream.
      * 
      * @param ref the ref returned from the command
      * @throws XMLStreamException
      */
     public void writeUpdateRefResponse(Ref ref) throws XMLStreamException {
         out.writeStartElement("ChangedRef");
         writeElement("name", ref.getName());
         writeElement("objectId", ref.getObjectId().toString());
         if (ref instanceof SymRef) {
             writeElement("target", ((SymRef) ref).getTarget());
         }
         out.writeEndElement();
     }
 
     /**
      * Writes the response for the {@link RefParseWeb} command to the stream.
      * 
      * @param ref the ref returned from the command
      * @throws XMLStreamException
      */
     public void writeRefParseResponse(Ref ref) throws XMLStreamException {
         out.writeStartElement("Ref");
         writeElement("name", ref.getName());
         writeElement("objectId", ref.getObjectId().toString());
         if (ref instanceof SymRef) {
             writeElement("target", ((SymRef) ref).getTarget());
         }
         out.writeEndElement();
     }
 
     /**
      * Writes an empty ref response for when a {@link Ref} was not found.
      * 
      * @throws XMLStreamException
      */
     public void writeEmptyRefResponse() throws XMLStreamException {
         out.writeStartElement("RefNotFound");
         out.writeEndElement();
     }
 
     /**
      * Writes the response for the {@link BranchWebOp} command to the stream.
      * 
      * @param localBranches the local branches of the repository
      * @param remoteBranches the remote branches of the repository
      * @throws XMLStreamException
      */
     public void writeBranchListResponse(List<Ref> localBranches, List<Ref> remoteBranches)
             throws XMLStreamException {
 
         out.writeStartElement("Local");
         for (Ref branch : localBranches) {
             out.writeStartElement("Branch");
             writeElement("name", branch.localName());
             out.writeEndElement();
         }
         out.writeEndElement();
 
         out.writeStartElement("Remote");
         for (Ref branch : remoteBranches) {
             if (!(branch instanceof SymRef)) {
                 out.writeStartElement("Branch");
                 writeElement("remoteName", branch.namespace().replace(Ref.REMOTES_PREFIX + "/", ""));
                 writeElement("name", branch.localName());
                 out.writeEndElement();
             }
         }
         out.writeEndElement();
 
     }
 
     /**
      * Writes the response for the {@link RemoteWebOp} command to the stream.
      * 
      * @param remotes the list of the {@link Remote}s of this repository
      * @throws XMLStreamException
      */
     public void writeRemoteListResponse(List<Remote> remotes) throws XMLStreamException {
         for (Remote remote : remotes) {
             out.writeStartElement("Remote");
             writeElement("name", remote.getName());
             out.writeEndElement();
         }
     }
 
     /**
      * Writes the response for the {@link TagWebOp} command to the stream.
      * 
      * @param tags the list of {@link RevTag}s of this repository
      * @throws XMLStreamException
      */
     public void writeTagListResponse(List<RevTag> tags) throws XMLStreamException {
         for (RevTag tag : tags) {
             out.writeStartElement("Tag");
             writeElement("name", tag.getName());
             out.writeEndElement();
         }
     }
 
     public void writeFetchResponse(FetchResult result) throws XMLStreamException {
         out.writeStartElement("Fetch");
         if (result.getChangedRefs().entrySet().size() > 0) {
             for (Entry<String, List<ChangedRef>> entry : result.getChangedRefs().entrySet()) {
                 out.writeStartElement("Remote");
                 writeElement("remoteName", entry.getKey());
                 for (ChangedRef ref : entry.getValue()) {
                     out.writeStartElement("Branch");
 
                     writeElement("changeType", ref.getType().toString());
                     if (ref.getOldRef() != null) {
                         writeElement("name", ref.getOldRef().localName());
                         writeElement("oldValue", ref.getOldRef().getObjectId().toString());
                     }
                     if (ref.getNewRef() != null) {
                         if (ref.getOldRef() == null) {
                             writeElement("name", ref.getNewRef().localName());
                         }
                         writeElement("newValue", ref.getNewRef().getObjectId().toString());
                     }
                     out.writeEndElement();
                 }
                 out.writeEndElement();
             }
         }
         out.writeEndElement();
     }
 
     public void writePullResponse(PullResult result, Iterator<DiffEntry> iter, CommandLocator geogit)
             throws XMLStreamException {
         out.writeStartElement("Pull");
         writeFetchResponse(result.getFetchResult());
         if (iter != null) {
             writeElement("Remote", result.getRemoteName());
             writeElement("Ref", result.getNewRef().localName());
             int added = 0;
             int removed = 0;
             int modified = 0;
             while (iter.hasNext()) {
                 DiffEntry entry = iter.next();
                 if (entry.changeType() == ChangeType.ADDED) {
                     added++;
                 } else if (entry.changeType() == ChangeType.MODIFIED) {
                     modified++;
                 } else if (entry.changeType() == ChangeType.REMOVED) {
                     removed++;
                 }
             }
             writeElement("Added", Integer.toString(added));
             writeElement("Modified", Integer.toString(modified));
             writeElement("Removed", Integer.toString(removed));
         }
         if (result.getMergeReport().isPresent()
                 && result.getMergeReport().get().getReport().isPresent()) {
             writeMergeResponse(result.getMergeReport().get().getReport().get(), geogit, result
                     .getMergeReport().get().getOurs(), result.getMergeReport().get().getPairs()
                     .get(0).getTheirs(), result.getMergeReport().get().getPairs().get(0)
                     .getAncestor());
         }
         out.writeEndElement();
     }
 
     /**
      * Writes a set of feature diffs to the stream.
      * 
      * @param diffs a map of {@link PropertyDescriptor} to {@link AttributeDiffs} that specify the
      *        difference between two features
      * @throws XMLStreamException
      */
     public void writeFeatureDiffResponse(Map<PropertyDescriptor, AttributeDiff> diffs)
             throws XMLStreamException {
         Set<Entry<PropertyDescriptor, AttributeDiff>> entries = diffs.entrySet();
         Iterator<Entry<PropertyDescriptor, AttributeDiff>> iter = entries.iterator();
         while (iter.hasNext()) {
             Entry<PropertyDescriptor, AttributeDiff> entry = iter.next();
             out.writeStartElement("diff");
             writeElement("attributename", entry.getKey().getName().toString());
             writeElement("changetype", entry.getValue().getType().toString());
             if (entry.getValue().getOldValue() != null
                     && entry.getValue().getOldValue().isPresent()) {
                 writeElement("oldvalue", entry.getValue().getOldValue().get().toString());
             }
             if (entry.getValue().getNewValue() != null
                     && entry.getValue().getNewValue().isPresent()
                     && !entry.getValue().getType().equals(TYPE.NO_CHANGE)) {
                 writeElement("newvalue", entry.getValue().getNewValue().get().toString());
             }
             out.writeEndElement();
         }
     }
 
     /**
      * Writes the response for a set of diffs while also supplying the geometry.
      * 
      * @param geogit - a CommandLocator to call commands from
      * @param diff - a DiffEntry iterator to build the response from
      * @throws XMLStreamException
      */
    public void writeGeometryChanges(final CommandLocator geogit, Iterator<DiffEntry> diff, int page,
            int elementsPerPage) throws XMLStreamException {
 
         advance(diff, page * elementsPerPage);
         int counter = 0;
 
         Iterator<GeometryChange> changeIterator = Iterators.transform(diff,
                 new Function<DiffEntry, GeometryChange>() {
                     @Override
                     public GeometryChange apply(DiffEntry input) {
                         Optional<RevObject> feature = Optional.absent();
                         Optional<RevObject> type = Optional.absent();
                         String path = null;
                         String crsCode = null;
                         GeometryChange change = null;
                         if (input.changeType() == ChangeType.ADDED
                                 || input.changeType() == ChangeType.MODIFIED) {
                             feature = geogit.command(RevObjectParse.class)
                                     .setObjectId(input.newObjectId()).call();
                             type = geogit.command(RevObjectParse.class)
                                     .setObjectId(input.getNewObject().getMetadataId()).call();
                             path = input.getNewObject().path();
 
                         } else if (input.changeType() == ChangeType.REMOVED) {
                             feature = geogit.command(RevObjectParse.class)
                                     .setObjectId(input.oldObjectId()).call();
                             type = geogit.command(RevObjectParse.class)
                                     .setObjectId(input.getOldObject().getMetadataId()).call();
                             path = input.getOldObject().path();
                         }
                         if (feature.isPresent() && feature.get() instanceof RevFeature
                                 && type.isPresent() && type.get() instanceof RevFeatureType) {
                             RevFeatureType featureType = (RevFeatureType) type.get();
                             Collection<PropertyDescriptor> attribs = featureType.type()
                                     .getDescriptors();
 
                             for (PropertyDescriptor attrib : attribs) {
                                 PropertyType attrType = attrib.getType();
                                FieldType entityType = FieldType.forBinding(attrType.getBinding());
                                if (entityType.isGeometry() && attrType instanceof GeometryType) {
                                     GeometryType gt = (GeometryType) attrType;
                                     CoordinateReferenceSystem crs = gt
                                             .getCoordinateReferenceSystem();
                                     if (crs != null) {
                                         crsCode = CRS.toSRS(crs);
                                     }
                                     break;
                                 }
                             }
 
                             RevFeature revFeature = (RevFeature) feature.get();
                             FeatureBuilder builder = new FeatureBuilder(featureType);
                             GeogitSimpleFeature simpleFeature = (GeogitSimpleFeature) builder
                                     .build(revFeature.getId().toString(), revFeature);
                             change = new GeometryChange(simpleFeature, input.changeType(), path,
                                     crsCode);
                         }
                         return change;
                     }
                 });
 
         while (changeIterator.hasNext() && (elementsPerPage == 0 || counter < elementsPerPage)) {
             GeometryChange next = changeIterator.next();
             if (next != null) {
                 GeogitSimpleFeature feature = next.getFeature();
                 ChangeType change = next.getChangeType();
                 out.writeStartElement("Feature");
                 writeElement("change", change.toString());
                 writeElement("id", next.getPath());
                 List<Object> attributes = feature.getAttributes();
                 for (Object attribute : attributes) {
                     if (attribute instanceof Geometry) {
                         writeElement("geometry", ((Geometry) attribute).toText());
                         break;
                     }
                 }
                 if (next.getCRS() != null) {
                     writeElement("crs", next.getCRS());
                 }
                 out.writeEndElement();
                 counter++;
             }
         }
         if (changeIterator.hasNext()) {
             writeElement("nextPage", "true");
         }
     }
 
     /**
      * Writes the response for a set of conflicts while also supplying the geometry.
      * 
      * @param geogit - a CommandLocator to call commands from
      * @param conflicts - a Conflict iterator to build the response from
      * @throws XMLStreamException
      */
     public void writeConflicts(final CommandLocator geogit, Iterator<Conflict> conflicts,
             final ObjectId ours, final ObjectId theirs) throws XMLStreamException {
         Iterator<GeometryConflict> conflictIterator = Iterators.transform(conflicts,
                 new Function<Conflict, GeometryConflict>() {
                     @Override
                     public GeometryConflict apply(Conflict input) {
                         ObjectId commitId = ours;
                         if (input.getOurs().equals(ObjectId.NULL)) {
                             commitId = theirs;
                         }
                         Optional<RevObject> object = geogit.command(RevObjectParse.class)
                                 .setObjectId(commitId).call();
                         RevCommit commit = null;
                         if (object.isPresent() && object.get() instanceof RevCommit) {
                             commit = (RevCommit) object.get();
                         } else {
                             throw new CommandSpecException("Couldn't resolve id: "
                                     + commitId.toString() + " to a commit");
                         }
 
                         object = geogit.command(RevObjectParse.class)
                                 .setObjectId(commit.getTreeId()).call();
                         Optional<NodeRef> node = Optional.absent();
                         if (object.isPresent()) {
                             RevTree tree = (RevTree) object.get();
                             node = geogit.command(FindTreeChild.class).setParent(tree)
                                     .setChildPath(input.getPath()).call();
                         } else {
                             throw new CommandSpecException("Couldn't resolve commit's treeId");
                         }
 
                         RevFeatureType type = null;
                         RevFeature feature = null;
 
                         if (node.isPresent()) {
                             object = geogit.command(RevObjectParse.class)
                                     .setObjectId(node.get().getMetadataId()).call();
                             if (object.isPresent() && object.get() instanceof RevFeatureType) {
                                 type = (RevFeatureType) object.get();
                             } else {
                                 throw new CommandSpecException(
                                         "Couldn't resolve newCommit's featureType");
                             }
                             object = geogit.command(RevObjectParse.class)
                                     .setObjectId(node.get().objectId()).call();
                             if (object.isPresent() && object.get() instanceof RevFeature) {
                                 feature = (RevFeature) object.get();
                             } else {
                                 throw new CommandSpecException(
                                         "Couldn't resolve newCommit's feature");
                             }
                         }
 
                         GeometryConflict conflict = null;
 
                         if (feature != null && type != null) {
                             String crsCode = null;
                             Collection<PropertyDescriptor> attribs = type.type().getDescriptors();
 
                             for (PropertyDescriptor attrib : attribs) {
                                 PropertyType attrType = attrib.getType();
                                FieldTypeType entityType = FieldTypeType.forBinding(attrType
                                        .getBinding());
                                if (entityType.isGeometry() && attrType instanceof GeometryType) {
                                     GeometryType gt = (GeometryType) attrType;
                                     CoordinateReferenceSystem crs = gt
                                             .getCoordinateReferenceSystem();
 
                                     if (crs != null) {
                                         crsCode = CRS.toSRS(crs);
                                     }
                                     break;
                                 }
                             }
 
                             FeatureBuilder builder = new FeatureBuilder(type);
                             GeogitSimpleFeature simpleFeature = (GeogitSimpleFeature) builder
                                     .build(feature.getId().toString(), feature);
                             Geometry geom = null;
                             List<Object> attributes = simpleFeature.getAttributes();
                             for (Object attribute : attributes) {
                                 if (attribute instanceof Geometry) {
                                     geom = (Geometry) attribute;
                                     break;
                                 }
                             }
                             conflict = new GeometryConflict(input, geom, crsCode);
                         }
                         return conflict;
                     }
                 });
 
         while (conflictIterator.hasNext()) {
             GeometryConflict next = conflictIterator.next();
             if (next != null) {
                 out.writeStartElement("Feature");
                 writeElement("change", "CONFLICT");
                 writeElement("id", next.getConflict().getPath());
                 writeElement("ourvalue", next.getConflict().getOurs().toString());
                 writeElement("theirvalue", next.getConflict().getTheirs().toString());
                 writeElement("geometry", next.getGeometry().toText());
                 if (next.getCRS() != null) {
                     writeElement("crs", next.getCRS());
                 }
                 out.writeEndElement();
             }
         }
     }
 
     /**
      * Writes the response for a set of merged features while also supplying the geometry.
      * 
      * @param geogit - a CommandLocator to call commands from
      * @param features - a FeatureInfo iterator to build the response from
      * @throws XMLStreamException
      */
     public void writeMerged(final CommandLocator geogit, Iterator<FeatureInfo> features)
             throws XMLStreamException {
         Iterator<GeometryChange> changeIterator = Iterators.transform(features,
                 new Function<FeatureInfo, GeometryChange>() {
                     @Override
                     public GeometryChange apply(FeatureInfo input) {
                         GeometryChange change = null;
                         RevFeatureBuilder revBuilder = new RevFeatureBuilder();
                         RevFeature revFeature = revBuilder.build(input.getFeature());
                         RevFeatureType featureType = input.getFeatureType();
                         Collection<PropertyDescriptor> attribs = featureType.type()
                                 .getDescriptors();
                         String crsCode = null;
 
                         for (PropertyDescriptor attrib : attribs) {
                             PropertyType attrType = attrib.getType();
                            FieldTypeType entityType = FieldTypeType.forBinding(attrType
                                    .getBinding());
                            if (entityType.isGeometry() && attrType instanceof GeometryType) {
                                 GeometryType gt = (GeometryType) attrType;
                                 CoordinateReferenceSystem crs = gt.getCoordinateReferenceSystem();
                                 if (crs != null) {
                                     crsCode = CRS.toSRS(crs);
                                 }
                                 break;
                             }
                         }
 
                         FeatureBuilder builder = new FeatureBuilder(featureType);
                         GeogitSimpleFeature simpleFeature = (GeogitSimpleFeature) builder.build(
                                 revFeature.getId().toString(), revFeature);
                         change = new GeometryChange(simpleFeature, ChangeType.MODIFIED, input
                                 .getPath(), crsCode);
                         return change;
                     }
                 });
 
         while (changeIterator.hasNext()) {
             GeometryChange next = changeIterator.next();
             if (next != null) {
                 GeogitSimpleFeature feature = next.getFeature();
                 out.writeStartElement("Feature");
                 writeElement("change", "MERGED");
                 writeElement("id", next.getPath());
                 List<Object> attributes = feature.getAttributes();
                 for (Object attribute : attributes) {
                     if (attribute instanceof Geometry) {
                         writeElement("geometry", ((Geometry) attribute).toText());
                         break;
                     }
                 }
                 if (next.getCRS() != null) {
                     writeElement("crs", next.getCRS());
                 }
                 out.writeEndElement();
             }
         }
     }
 
     /**
      * Writes the response for a merge dry-run, contains unconflicted, conflicted and merged
      * features.
      * 
      * @param report - the MergeScenarioReport containing all the merge results
      * @param transaction - a CommandLocator to call commands from
      * @throws XMLStreamException
      */
     public void writeMergeResponse(MergeScenarioReport report, CommandLocator transaction,
             ObjectId ours, ObjectId theirs, ObjectId ancestor) throws XMLStreamException {
         out.writeStartElement("Merge");
         writeElement("ours", ours.toString());
         writeElement("theirs", theirs.toString());
         writeElement("ancestor", ancestor.toString());
         if (report.getConflicts().size() > 0) {
             writeElement("conflicts", Integer.toString(report.getConflicts().size()));
         }
         writeGeometryChanges(transaction, report.getUnconflicted().iterator(), 0, 0);
         writeConflicts(transaction, report.getConflicts().iterator(), ours, theirs);
         writeMerged(transaction, report.getMerged().iterator());
         out.writeEndElement();
     }
 
     /**
      * Writes the id of the transaction created or nothing if it was ended successfully.
      * 
      * @param transactionId - the id of the transaction or null if the transaction was closed
      *        successfully
      * @throws XMLStreamException
      */
     public void writeTransactionId(UUID transactionId) throws XMLStreamException {
         out.writeStartElement("Transaction");
         if (transactionId != null) {
             writeElement("ID", transactionId.toString());
         }
         out.writeEndElement();
     }
 
     private class GeometryChange {
         private GeogitSimpleFeature feature;
 
         private ChangeType changeType;
 
         private String path;
 
         private String crs;
 
         public GeometryChange(GeogitSimpleFeature feature, ChangeType changeType, String path,
                 String crs) {
             this.feature = feature;
             this.changeType = changeType;
             this.path = path;
             this.crs = crs;
         }
 
         public GeogitSimpleFeature getFeature() {
             return feature;
         }
 
         public ChangeType getChangeType() {
             return changeType;
         }
 
         public String getPath() {
             return path;
         }
 
         public String getCRS() {
             return crs;
         }
     }
 
     private class GeometryConflict {
         private Conflict conflict;
 
         private Geometry geom;
 
         private String crs;
 
         public GeometryConflict(Conflict conflict, Geometry geom, String crs) {
             this.conflict = conflict;
             this.geom = geom;
             this.crs = crs;
         }
 
         public Conflict getConflict() {
             return conflict;
         }
 
         public Geometry getGeometry() {
             return geom;
         }
 
         public String getCRS() {
             return crs;
         }
     }
 }
