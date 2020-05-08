 /*
  * $Id: ContextPathResolver.java 794 2008-04-23 22:23:10Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.atom;
 
 import static org.mule.galaxy.util.AbderaUtils.newErrorMessage;
 import static org.mule.galaxy.util.AbderaUtils.throwMalformed;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 import javax.xml.namespace.QName;
 
 import org.acegisecurity.context.SecurityContextHolder;
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.i18n.iri.IRI;
 import org.apache.abdera.i18n.text.UrlEncoding;
 import org.apache.abdera.i18n.text.CharUtils.Profile;
 import org.apache.abdera.model.AtomDate;
 import org.apache.abdera.model.Content;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Element;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.Person;
 import org.apache.abdera.model.Text;
 import org.apache.abdera.model.Text.Type;
 import org.apache.abdera.parser.ParseException;
 import org.apache.abdera.protocol.server.RequestContext;
 import org.apache.abdera.protocol.server.RequestContext.Scope;
 import org.apache.abdera.protocol.server.context.EmptyResponseContext;
 import org.apache.abdera.protocol.server.context.ResponseContextException;
 import org.apache.abdera.protocol.server.context.SimpleResponseContext;
 import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mule.galaxy.Artifact;
 import org.mule.galaxy.ArtifactVersion;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.EntryResult;
 import org.mule.galaxy.EntryVersion;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.extension.AtomExtension;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
 import org.mule.galaxy.policy.ApprovalMessage;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.util.AbderaUtils;
 import org.mule.galaxy.util.SecurityUtils;
 
 public abstract class AbstractEntryCollection 
     extends AbstractEntityCollectionAdapter<Item> {
     public static final String NAMESPACE = "http://galaxy.mule.org/1.0";
     public static final String ID_PREFIX = "urn:galaxy:artifact:";    
     public static final QName VERSION_QNAME = new QName(NAMESPACE, "version");
 
     protected final Log log = LogFactory.getLog(getClass());
 
     protected Factory factory;
     protected Registry registry;
     
     public AbstractEntryCollection(Registry registry) {
         super();
         this.registry = registry;
         factory = Abdera.getInstance().getFactory();
     }
 
     public Content getContent(Item doc, RequestContext request) {
         Content content = request.getAbdera().getFactory().newContent();
 //        content.setContentType(org.apache.abdera.model.Content.Type.XHTML);
         org.mule.galaxy.Entry entry = getEntry(doc);
         if (entry != null) {
             content.setText(entry.getDescription());
         } else {
             content.setText("");
         }
         return content;
     }
     
     protected Artifact getArtifact(Item i) {
         if (i instanceof Artifact) {
             return (Artifact) i;
         }
         
         if (i instanceof ArtifactVersion) {
             return ((ArtifactVersion) i).getParent();
         }
         
         return null;
     }
     
     protected org.mule.galaxy.Entry getEntry(Item i) {
         if (i instanceof org.mule.galaxy.Entry) {
             return (org.mule.galaxy.Entry) i;
         }
         
         if (i instanceof EntryVersion) {
             return ((EntryVersion) i).getParent();
         }
         
         return null;
     }
     
     protected EntryVersion getEntryVersion(Item i) {
         if (i instanceof org.mule.galaxy.Entry) {
             return ((org.mule.galaxy.Entry) i).getDefaultOrLastVersion();
         }
         
         if (i instanceof EntryVersion) {
             return (EntryVersion) i;
         }
         
         return null;
     }
     @Override
     protected String addEntryDetails(RequestContext request, 
                                      Entry atomEntry, 
                                      IRI feedIri, 
                                      Item item)
         throws ResponseContextException {
         String link = super.addEntryDetails(request, atomEntry, feedIri, item);
         
         Element itemInfo;
         Item eOrW = getEntryOrWorkspace(item);
         if (eOrW instanceof Artifact) {
             Artifact artifact = (Artifact) eOrW;
             itemInfo = factory.newElement(new QName(NAMESPACE, "artifact-info"));
             itemInfo.setAttributeValue("mediaType", artifact.getContentType().toString());
             
             if (artifact.getDocumentType() != null) {
                 itemInfo.setAttributeValue("documentType", artifact.getDocumentType().toString());
             }
         } else if (eOrW instanceof org.mule.galaxy.Entry) {
             itemInfo = factory.newElement(new QName(NAMESPACE, "entry-info"));
         } else {
             itemInfo = factory.newElement(new QName(NAMESPACE, "workspace-info"));
         }
 
         atomEntry.addExtension(itemInfo);
         itemInfo.setAttributeValue("created", AtomDate.format(item.getCreated().getTime()));
         itemInfo.setAttributeValue("name", eOrW.getName());
         
         EntryVersion ev = getEntryVersion(item);
         if (ev != null) {
             Element version = factory.newElement(new QName(NAMESPACE, "version"));
             version.setAttributeValue("label", ev.getVersionLabel());
             version.setAttributeValue("enabled", (String) new Boolean(ev.isEnabled()).toString());
             version.setAttributeValue("default",(String) new Boolean(ev.isDefault()).toString());
             version.setAttributeValue("created", AtomDate.format(ev.getCreated().getTime()));
             
             atomEntry.addExtension(version);
         }
 
         if (ev != null) {
 //            addMetadata(entry, atomEntry, request, null);
             addMetadata(ev, atomEntry, request, null);
         } else {
             // workspaces can have metadata too
             addMetadata(item, atomEntry, request, null);
         }
         
         return link;
     }
 
     protected void addMetadata(Item entryObj, Entry atomEntry, RequestContext request, String id) {
         Element metadata = factory.newElement(new QName(NAMESPACE, "metadata"));
         
         if (id != null) {
             
         }
         boolean showHidden = BooleanUtils.toBoolean(request.getParameter("showHiddenProperties"));
        
         for (PropertyInfo p : entryObj.getProperties()) {
             PropertyDescriptor pd = p.getPropertyDescriptor();
             
             if (p.isVisible() || showHidden) {
                 if (pd != null && pd.getExtension() instanceof AtomExtension) {
                     ((AtomExtension) pd.getExtension()).annotateAtomEntry(entryObj, pd, atomEntry, factory);
                 } else {
                     Element prop = factory.newElement(new QName(NAMESPACE, "property"), metadata);
                     prop.setAttributeValue("name", p.getName());
                     prop.setAttributeValue("locked", new Boolean(p.isLocked()).toString());
                     
                     if (p.isVisible()) {
                         prop.setAttributeValue("visible", new Boolean(p.isVisible()).toString());
                     }
                     
                     Object value = p.getValue();
                     if (value == null) {
                         value = "";
                     }
                     
                     if (value instanceof Collection) {
                         for (Object o : ((Collection) value)) {
                             Element valueEl = factory.newElement(new QName(NAMESPACE, "value"), prop);
                             
                             valueEl.setText(o.toString());
                         }
                     } else {
                         prop.setAttributeValue("value", value.toString());
                     }
                 }
             }
         }
         
         atomEntry.addExtension(metadata);
     }
 
     @Override
     public Text getSummary(Item item, RequestContext request) {
         Artifact a = getArtifact(item);
         if (a != null) {
             Text summary = factory.newSummary();
             
             String d = a.getDescription();
             if (d == null) d = "";
             
             summary.setText(d);
             summary.setTextType(Type.XHTML);
             
             return summary;
         }
         return null;
     }
 
     public String getTitle(Item doc) {
         return getEntryOrWorkspace(doc).getName();
     }
     
     public InputStream getMediaStream(Item entry) throws ResponseContextException {
         EntryVersion ev = getEntryVersion(entry);
         if (ev instanceof ArtifactVersion) {
             return ((ArtifactVersion)ev).getStream();
         }
         throw new UnsupportedOperationException();
     }
     
     @Override
     public String getContentType(Item entry) {
         Artifact artifact = getArtifact(entry);
         if (artifact != null) {
             return artifact.getContentType().toString();
         }
         throw new UnsupportedOperationException();
     }
 
     public String getAuthor(RequestContext request) {
         return "Mule Galaxy";
     }
 
     public Date getUpdated(Item doc) {
         return doc.getUpdated().getTime();
     }
 
     @Override
     public EntryVersion postMedia(MimeType mimeType, 
                                   String slug, 
                                   InputStream inputStream,
                                   RequestContext request) throws ResponseContextException {
         try {
             String version = getVersion(request);
             
             User user = getUser();
             
             EntryResult result = postMediaEntry(slug, mimeType, version, inputStream, user, request);
             
             return (EntryVersion) result.getEntryVersion();
         } catch (DuplicateItemException e) {
             throw newErrorMessage("Duplicate artifact.", "An artifact with that name already exists in this workspace.", 409);
         } catch (RegistryException e) {
             log.error("Could not add artifact.", e);
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         } catch (MimeTypeParseException e) {
             throw new ResponseContextException(500, e);
         } catch (PolicyException e) {
             throw AbderaUtils.createArtifactPolicyExceptionResponse(e);
         } catch (AccessException e) {
             throw new ResponseContextException(401, e);
         }
     }
     
     protected User getUser() {
         return SecurityUtils.getCurrentUser();
     }
 
     protected String getVersion(RequestContext request) throws ResponseContextException {
         String version = request.getHeader("X-Artifact-Version");
         
         if (version == null || version.equals("")) {
             EmptyResponseContext ctx = new EmptyResponseContext(500);
            ctx.setStatusText("You must supply an X-Artifact-Version header!");
             
             throw new ResponseContextException(ctx);
         }
         return version;
     }
 
     protected abstract EntryResult postMediaEntry(String slug, MimeType mimeType, String version,
                                                      InputStream inputStream, User user, RequestContext ctx)
         throws RegistryException, PolicyException, IOException, MimeTypeParseException, ResponseContextException, DuplicateItemException, AccessException;
     
     @Override
     public boolean isMediaEntry(Item entry) {
         return entry instanceof Artifact || entry instanceof ArtifactVersion;
     }
 
     @Override
     public List<Person> getAuthors(Item entry, RequestContext request) throws ResponseContextException {
         Person author = request.getAbdera().getFactory().newAuthor();
         author.setName("Galaxy");
         return Arrays.asList(author);
     }
     
     @Override
     public String getMediaName(Item item) {
         return UrlEncoding.encode(getEntry(item).getName());
     }
 
     @Override
     public String getHref(RequestContext request) {
         String href = (String) request.getAttribute(Scope.REQUEST, EntryResolver.COLLECTION_HREF);
         if (href == null) {
             // this is the url we use when pulling down the services document
             href = request.getTargetBasePath() + "/registry";
         }
         return href;
     }
 
     public String getId(Item item) {
         return ID_PREFIX + item.getId();
     }
 
     @Override
     protected String getFeedIriForEntry(Item entryObj, RequestContext request) {
         Item e = getEntryOrWorkspace(entryObj);
         
         Workspace w = (Workspace) e.getParent();
         String path;
         if (w == null) {
             path = "";
         } else {
             path = UrlEncoding.encode(w.getPath(), Profile.PATH.filter());
         }
         return request.getTargetBasePath() + "/registry" + path;
     }
 
     @Override
     public String getName(Item doc) {
         return getNameOfArtifact(doc) + ";atom";
     }
     public String getNameOfArtifact(Item i) {
         return UrlEncoding.encode(getEntryOrWorkspace(i).getName(), Profile.PATH.filter());
     }
     
     protected Item getEntryOrWorkspace(Item i) {
         if (i instanceof Workspace) {
             return (Workspace) i;
         }
         
         return getEntry(i);
     }
     
     public Item getEntry(String name, RequestContext request) throws ResponseContextException {
         return getRegistryItem(request);
     }
 
     protected Item getRegistryItem(RequestContext request) {
         return (Item) request.getAttribute(Scope.REQUEST, EntryResolver.ITEM);
     }
 
     protected String getVersionLabel(Entry atomEntry) throws ResponseContextException {
         Element version = atomEntry.getExtension(VERSION_QNAME);
         
         if (version == null || version.getAttributeValue("label") == null) {
             throw newErrorMessage("Invalid version label", "A version element must be specified with a label attribute.", 500);
         }
         
         return version.getAttributeValue("label");
     }
 
 
     @Override
     public void putEntry(Item item, 
                          String title, 
                          Date updated, 
                          List<Person> authors, 
                          String summary,
                          Content content, 
                          RequestContext request) throws ResponseContextException {
         org.mule.galaxy.Entry entry = getEntry(item);
         EntryVersion ev = getEntryVersion(item);
 //        artifact.setName(title);
         
         try {
             Document<Entry> entryDoc = request.getDocument();
             Entry atomEntry = entryDoc.getRoot();
             
             
             if (entry instanceof Artifact) {
                 entry.setDescription(summary);
                 mapEntryExtensions(ev, atomEntry);
             } else if (entry != null && content != null) {
                 entry.setDescription(content.getText());
                 mapEntryExtensions(ev, atomEntry);
             } else {
                 mapEntryExtensions(item, atomEntry);
             }
         } catch (ParseException e) {
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         } catch (RegistryException e) {
             throw new ResponseContextException(500, e);
         } catch (PolicyException e) {
             throw AbderaUtils.createArtifactPolicyExceptionResponse(e);
         }
     }
 
     protected void mapEntryExtensions(Item av, Entry entry) throws ResponseContextException,
         PolicyException, RegistryException {
         for (Element e : entry.getElements()) {
             QName q = e.getQName();
             
             AtomExtension atomExt = getExtension(q);
             if (atomExt != null) {
                 atomExt.updateItem(av, factory, e);
             } else if (NAMESPACE.equals(q.getNamespaceURI())) {
                 if ("metadata".equals(q.getLocalPart())) {
                     updateMetadata(av, e);
                 } else if ("version".equals(q.getLocalPart())) {
                     updateVersion(getEntryVersion(av), e);
                 } else if ("item-info".equals(q.getLocalPart())) {
                     updateItemInfo(getEntryOrWorkspace(av), e);
                 }
             }
         }
     }
 
     private void updateItemInfo(Item entryOrWorkspace, Element e) {
         String name = e.getAttributeValue("name");
         
         if (name != null) {
             entryOrWorkspace.setName(name);
         }
     }
 
     private AtomExtension getExtension(QName q) {
         for (Extension e : registry.getExtensions()) {
             if (e instanceof AtomExtension && ((AtomExtension) e).getUnderstoodElements().contains(q)) {
                 return (AtomExtension) e;
             }
         }
         return null;
     }
 
     private void updateVersion(EntryVersion av, Element e) 
         throws RegistryException, PolicyException, ResponseContextException {
         String label = e.getAttributeValue("label");
         
         if (label != null && !av.getVersionLabel().equals(label)) {
             av.setVersionLabel(label);
         }
         
         String def = e.getAttributeValue("default");
         if (def != null) {
             boolean defBool = BooleanUtils.toBoolean(def);
             
             if (defBool != av.isDefault()) 
             {
                 if (defBool) {
                     av.setAsDefaultVersion();
                 } else {
                     throwMalformed("You can only set an artifact default version to true!");
                 }
             }
         }
         
         String enabled = e.getAttributeValue("enabled");
         if (enabled != null) {
             boolean enabledBool = BooleanUtils.toBoolean(enabled);
             
             if (enabledBool != av.isEnabled()) {
                 av.setEnabled(enabledBool);
             }
         }
     }
     
     private void updateMetadata(Item av, Element e) throws ResponseContextException, PolicyException {
         for (Element propEl : e.getElements()) {
             String name = propEl.getAttributeValue("name");
             if (name == null)
                 throwMalformed("You must specify name attributes on metadata properties.");
             
             String value = propEl.getAttributeValue("value");
             String visible = propEl.getAttributeValue("visible");
             if (value != null) {
                 try {
                     av.setProperty(name, value);
                 } catch (PropertyException e1) {
                     // Ignore as its probably because its locked
                 }
             } else {
                 List<Element> elements = propEl.getElements();
                 ArrayList<String> values = new ArrayList<String>();
                 for (Element valueEl : elements) {
                     if (valueEl.getQName().getLocalPart().equals("value")) {
                         values.add(valueEl.getText().trim());
                     }
                 }
                 try {
                     av.setProperty(name, values);
                 } catch (PropertyException e1) {
                     // Ignore as its probably because its locked
                 }
             }
             
             if (visible != null) {
                 av.setVisible(name, BooleanUtils.toBoolean(visible));
             }
         }
     }
 
     public void deleteEntry(String name, RequestContext request) throws ResponseContextException {
         Item entry = getRegistryItem(request);
 
         try {
             entry.delete();
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new ResponseContextException(405, e);
         }
     }
 
     public void deleteMedia(String name, RequestContext request) throws ResponseContextException {
         Item entry = getRegistryItem(request);
 
         try {
             entry.delete();
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new ResponseContextException(405, e);
         }
     }
 }
