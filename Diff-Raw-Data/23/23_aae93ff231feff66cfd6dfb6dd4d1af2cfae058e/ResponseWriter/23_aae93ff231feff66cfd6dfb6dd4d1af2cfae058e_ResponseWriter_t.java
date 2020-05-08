 package org.geogit.web.api;
 
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.Nullable;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.codehaus.jettison.AbstractXMLStreamWriter;
 import org.geogit.api.NodeRef;
 import org.geogit.api.ObjectId;
import org.geogit.api.Ref;
import org.geogit.api.SymRef;
 import org.geogit.api.RevCommit;
 import org.geogit.api.RevPerson;
 import org.geogit.api.plumbing.DiffIndex;
 import org.geogit.api.plumbing.DiffWorkTree;
 import org.geogit.api.plumbing.diff.DiffEntry;
 
 /**
  *
  */
 public class ResponseWriter {
 
     protected final XMLStreamWriter out;
 
     public ResponseWriter(XMLStreamWriter out) {
         this.out = out;
         if (out instanceof AbstractXMLStreamWriter) {
             configureJSONOutput((AbstractXMLStreamWriter) out);
         }
     }
 
     private void configureJSONOutput(AbstractXMLStreamWriter out) {
     }
 
     public void finish() throws XMLStreamException {
         out.writeEndElement(); // results
         out.writeEndDocument();
     }
 
     public void start() throws XMLStreamException {
         start(true);
     }
 
     public void start(boolean success) throws XMLStreamException {
         out.writeStartDocument();
         out.writeStartElement("response");
         writeElement("success", Boolean.toString(success));
     }
 
     public void writeHeaderElements(String... els) throws XMLStreamException {
         out.writeStartElement("header");
         for (int i = 0; i < els.length; i += 2) {
             writeElement(els[i], els[i + 1]);
         }
         out.writeEndElement();
     }
 
     public void writeErrors(String... errors) throws XMLStreamException {
         out.writeStartElement("errors");
         for (int i = 0; i < errors.length; i += 2) {
             writeElement(errors[i], errors[i + 1]);
         }
         out.writeEndElement();
     }
 
     public XMLStreamWriter getWriter() {
         return out;
     }
 
     public void writeElement(String element, @Nullable String content) throws XMLStreamException {
         out.writeStartElement(element);
         if (content != null) {
             out.writeCharacters(content);
         }
         out.writeEndElement();
     }
 
     public void writeStaged(DiffIndex setFilter, int start, int length) throws XMLStreamException {
         writeDiffEntries("staged", start, length, setFilter.call());
     }
 
     public void writeUnstaged(DiffWorkTree setFilter, int start, int length)
             throws XMLStreamException {
         writeDiffEntries("unstaged", start, length, setFilter.call());
     }
 
     @SuppressWarnings("rawtypes")
     private void advance(Iterator it, int cnt) {
         for (int i = 0; i < cnt && it.hasNext(); i++) {
             it.next();
         }
     }
 
     public void writeDiffEntries(String name, int start, int length, Iterator<DiffEntry> entries)
             throws XMLStreamException {
         advance(entries, start);
         if (length < 0) {
             length = Integer.MAX_VALUE;
         }
         for (int i = 0; i < length && entries.hasNext(); i++) {
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
         }
     }
 
     public void writeCommits(Iterator<RevCommit> entries) throws XMLStreamException {
         while (entries.hasNext()) {
             RevCommit entry = entries.next();
             out.writeStartElement("commit");
             writeElement("id", entry.getId().toString());
             writeElement("tree", entry.getTreeId().toString());
 
             List<ObjectId> parentIds = entry.getParentIds();
             out.writeStartElement("parents");
             for (ObjectId parentId : parentIds) {
                 writeElement("id", parentId.toString());
             }
             out.writeEndElement();
 
             writePerson("author", entry.getAuthor());
             writePerson("committer", entry.getCommitter());
             writeElement("message", entry.getMessage());
             out.writeEndElement();
         }
     }
 
     public void writePerson(String enclosingElement, RevPerson p) throws XMLStreamException {
         out.writeStartElement(enclosingElement);
         writeElement("name", p.getName().orNull());
         writeElement("email", p.getEmail().orNull());
         writeElement("timestamp", Long.toString(p.getTimestamp()));
         writeElement("timeZoneOffset", Long.toString(p.getTimeZoneOffset()));
         out.writeEndElement();
     }
 
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
 
    public void writeUpdateRefResponse(Ref ref)
             throws XMLStreamException {
         out.writeStartElement("ChangedRef");
        writeElement("name", ref.getName());
        writeElement("objectId", ref.getObjectId().toString());
        if(ref instanceof SymRef) {
            writeElement("target", ((SymRef)ref).getTarget());
        }
         out.writeEndElement();
     }
 }
