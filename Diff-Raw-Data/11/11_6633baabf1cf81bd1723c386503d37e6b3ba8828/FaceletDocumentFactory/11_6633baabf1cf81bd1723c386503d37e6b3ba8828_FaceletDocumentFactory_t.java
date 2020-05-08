 package org.eclipse.jst.jsf.facelet.core.internal.cm;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.Namespace;
 import org.eclipse.jst.jsf.core.JSFVersion;
 import org.eclipse.jst.jsf.designtime.internal.view.model.ITagRegistry;
 import org.eclipse.jst.jsf.designtime.internal.view.model.TagRegistryFactory.TagRegistryFactoryException;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.FaceletRegistryManager.MyRegistryFactory;
 import org.eclipse.jst.jsf.facelet.core.internal.util.ViewUtil;
 import org.eclipse.jst.jsf.facelet.core.internal.util.ViewUtil.PrefixEntry;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.CMDocumentFactoryTLD;
 import org.eclipse.jst.jsp.core.internal.contentmodel.tld.provisional.TLDDocument;
 import org.eclipse.jst.jsp.core.taglib.ITaglibRecord;
 import org.eclipse.jst.jsp.core.taglib.TaglibIndex;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.w3c.dom.Element;
 
 /**
  * Creates CMDocument framework adaptation for Facelet features.
  * 
  * NOTE: this class currently caches state and is NOT THREADSAFE.  Share 
  * instances of this class between unowned classes at your own risk.
  * 
  * @author cbateman
  *
  */
 public class FaceletDocumentFactory
 {
     private final IProject                        _project;
     private final Map<String, NamespaceCMAdapter> _cmDocuments;
     private final Map<String, TagInfo>    _externalTagInfo;
 
     /**
      * @param project
      */
     public FaceletDocumentFactory(final IProject project)
     {
         _project = project;
         _cmDocuments = new HashMap<String, NamespaceCMAdapter>(8);
         _externalTagInfo = new HashMap<String, TagInfo>(8);
     }
 
     /**
      * @param uri
      * @param prefix
      * @return the CMDocument for the uri where prefix is used as its namespace
      * short-form (usually in the context of an XML document instance).
      */
     public CMDocument createCMDocumentForContext(final String uri,
             final String prefix)
     {
         final NamespaceCMAdapter cmDoc = getOrCreateCMDocument(_project, uri);
 
         if (cmDoc != null)
         {
             return new DocumentNamespaceCMAdapter(cmDoc, prefix);
         }
         return null;
     }
 
     /**
      * @param element
      * @return the CM model data for element or null if none.
      */
     public CMElementDeclaration createCMElementDeclaration(final Element element)
     {
         final String prefix = element.getPrefix();
         final Map<String, PrefixEntry> namespaces = ViewUtil
                 .getDocumentNamespaces(element.getOwnerDocument());
         final PrefixEntry prefixEntry = namespaces.get(prefix);
 
         if (prefixEntry != null)
         {
             final CMDocument cmDoc = createCMDocumentForContext(prefixEntry
                     .getUri(), prefixEntry.getPrefix());
 
             if (cmDoc != null)
             {
                 return (CMElementDeclaration) cmDoc.getElements().getNamedItem(
                         element.getLocalName());
             }
         }
 
         return null;
     }
 
     /**
      * @param ns
      * @return the externa tag info the namespace.  May return a previously
      * cached value. If there is no cached value, then creates it.
      */
     public TagInfo getOrCreateExtraTagInfo(final String ns)
     {
         TagInfo tagInfo = _externalTagInfo.get(ns);
 
         if (tagInfo == null)
         {
             tagInfo = createExternalTagInfo(ns);
             _externalTagInfo.put(ns, tagInfo);
         }
         return tagInfo;
     }
 
     /**
      * @return a new external tag info for this namespace
      */
     private TagInfo createExternalTagInfo(final String uri)
     {
         final JSFVersion jsfVersion = JSFVersion.valueOfProject(_project);
         TagInfo tldTagInfo = null;
         if (jsfVersion != null)
         {
             if (jsfVersion.compareTo(JSFVersion.V2_0) >= 0)
             {
                 tldTagInfo = new FaceletTagInfo(_project, uri);
             } else
             {
                 final ITaglibRecord[] tldrecs = TaglibIndex
                         .getAvailableTaglibRecords(_project.getFullPath());
                 FIND_TLDRECORD: for (final ITaglibRecord rec : tldrecs)
                 {
                     final String matchUri = rec.getDescriptor().getURI();
                     if (uri.equals(matchUri))
                     {
                         final CMDocumentFactoryTLD factory = new CMDocumentFactoryTLD();
                         tldTagInfo = new MetadataTagInfo(_project,
                                 (TLDDocument) factory.createCMDocument(rec));
                         break FIND_TLDRECORD;
                     }
                 }
             }
         }
        //Bug 361005 - NPE in Attributes Tab of Web Page Editor Properties
        if (tldTagInfo == null)
        {
            tldTagInfo = new MetadataTagInfo(_project, uri);
        }
         return tldTagInfo;
     }
 
     private NamespaceCMAdapter getOrCreateCMDocument(final IProject project,
             final String uri)
     {
         NamespaceCMAdapter adapter = _cmDocuments.get(uri);
 
         if (adapter == null)
         {
             final MyRegistryFactory factory = new MyRegistryFactory();
 
             ITagRegistry registry;
             try
             {
                 registry = factory.createTagRegistry(project);
                 if (registry != null)
                 {
                     final Namespace ns = registry.getTagLibrary(uri);
 
                     if (ns != null)
                     {
                         adapter = new NamespaceCMAdapter(ns, project);
                         _cmDocuments.put(uri, adapter);
                     }
                 }
             }
             catch (final TagRegistryFactoryException e)
             {
                 // fall-through
             }
         }
         return adapter;
     }
 
 }
