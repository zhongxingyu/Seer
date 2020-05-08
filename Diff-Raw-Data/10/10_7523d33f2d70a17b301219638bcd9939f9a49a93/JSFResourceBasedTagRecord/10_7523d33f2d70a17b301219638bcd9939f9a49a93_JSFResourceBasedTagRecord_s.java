 package org.eclipse.jst.jsf.facelet.core.internal.registry.taglib;
 
 import java.io.File;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.jsf.common.internal.util.JarUtilities;
 import org.eclipse.jst.jsf.designtime.internal.resources.IJSFResource;
 import org.eclipse.jst.jsf.designtime.internal.resources.IJSFResourceContainer;
 import org.eclipse.jst.jsf.designtime.internal.resources.IJSFResourceFragment;
 import org.eclipse.jst.jsf.designtime.internal.resources.IJarBasedJSFResource;
 import org.eclipse.jst.jsf.designtime.internal.resources.IWorkspaceJSFResourceFragment;
 import org.eclipse.jst.jsf.designtime.internal.resources.WorkspaceJSFResourceContainer;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.Listener.TaglibChangedEvent;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.Listener.TaglibChangedEvent.CHANGE_TYPE;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglibFactory;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglibTag;
 
 /**
  * A facelet tag record is derived from a JSF locatable resource (ezcomp).
  * 
  * @author cbateman
  * 
  */
 public class JSFResourceBasedTagRecord extends FaceletTagRecord
 {
     private final List<FaceletTaglibTag> _tags;
     private final String _uri;
 
     /**
      * @param uri
      * @param tags
      * @param descriptor
      */
     public JSFResourceBasedTagRecord(final String uri,
             final List<FaceletTaglibTag> tags,
             final TagRecordDescriptor descriptor)
     {
         super(descriptor);
         _uri = uri;
         _tags = tags;
     }
 
     /**
      * 
      */
     private static final long serialVersionUID = 5944923828112777373L;
 
     public int getNumTags()
     {
         return _tags.size();
     }
 
     @Override
     public String getURI()
     {
         return _uri;
     }
 
     @Override
     public FaceletTaglibTag getTag(final String name)
     {
         for (final FaceletTaglibTag tag : _tags)
         {
             if (tag.getTagName().equals(name))
             {
                 return tag;
             }
         }
         return null;
     }
 
     @Override
     public Collection<? extends FaceletTaglibTag> getTags()
     {
         return Collections.unmodifiableCollection(_tags);
     }
 
     /**
      * Merge my tags with 'withThese'. Tags in my list will be replaced with
      * those in 'withThese' if their names match.
      * 
      * @param withThese
      */
     /* package */JSFResourceBasedTagRecord mergeTags(
             final List<FaceletTaglibTag> withThese)
     {
         final List<FaceletTaglibTag> tags = new ArrayList<FaceletTaglibTag>(
                 _tags);
         for (final FaceletTaglibTag mergeTag : withThese)
         {
             final FaceletTaglibTag tag = getTag(mergeTag.getTagName());
             if (tag != null)
             {
                 tags.remove(tag);
             }
             tags.add(mergeTag);
         }
         return new JSFResourceBasedTagRecord(_uri, tags, getDescriptor());
     }
 
     /* package */JSFResourceBasedTagRecord removeTags(
             final List<FaceletTaglibTag> removeThese)
     {
         final List<FaceletTaglibTag> tags = new ArrayList<FaceletTaglibTag>(
                 _tags);
         for (final FaceletTaglibTag mergeTag : removeThese)
         {
             final FaceletTaglibTag tag = getTag(mergeTag.getTagName());
             if (tag != null)
             {
                 tags.remove(tag);
             }
         }
         return new JSFResourceBasedTagRecord(_uri, tags, getDescriptor());
     }
 
     /**
      * A builder for tag record.
      * 
      * @author cbateman
      * 
      */
     public static class Builder
     {
         private static final LibEntry WHOLE_LIBRARY = new LibEntry(null);
         private static final JSFResourceBasedTagRecord WHOLE_LIB_RECORD = new JSFResourceBasedTagRecord(null, Collections.EMPTY_LIST, null);
         private final Map<String, LibEntry> _tags = new HashMap<String, LibEntry>();
         private static final String FACELET_FILE_CONTENT_TYPE = "org.eclipse.wst.html.core.htmlsource"; //$NON-NLS-1$
 
         /**
          * @param jsfResource
          * @param changeType
          */
         public void addTag(final IJSFResource jsfResource,
                 final CHANGE_TYPE changeType)
         {
             final String libraryName = jsfResource.getId().getLibraryName();
             if (libraryName == null)
             {
                 return;
             }
             final String uri = String.format(
                     "http://java.sun.com/jsf/composite/%s", libraryName); //$NON-NLS-1$
             LibEntry tags = _tags.get(uri);
             if (tags == null)
             {
                 tags = new LibEntry(createDescriptor(jsfResource));
                 _tags.put(uri, tags);
             }
             final String resourceName = jsfResource.getId().getResourceName();
             final IPath resourceNamePath = new Path(resourceName)
                     .removeFileExtension();
             final FaceletTaglibTag tag = FaceletTaglibFactory.eINSTANCE
                     .createFaceletTaglibTag();
             tag.setTagName(resourceNamePath.toString());
             switch (changeType)
             {
                 case ADDED:
                 case CHANGED:
                     // only add to the list on a add/change if the resource
                     // exists and is the right type
                    if (jsfResource.isAccessible()
                            && jsfResource
                                    .isContentType(FACELET_FILE_CONTENT_TYPE))
                     {
                         tags.addTag(tag);
                     }
                 break;
                 case REMOVED:
                     // add all comers to the remove list. There will only be
                     // removal
                     // on merge if ADDED/CHANGED path decided they should be
                     // there.
                     tags.addTag(tag);
                 break;
             }
         }
 
         /**
          * @param jsfResource
          * @param changeType
          */
         public void addLibrary(final IJSFResourceContainer jsfResource,
                 final CHANGE_TYPE changeType)
         {
             final String libraryName = jsfResource.getId().getLibraryName();
             if (libraryName == null || libraryName.trim().length() == 0)
             {
                 return;
             }
             final String uri = String.format(
                     "http://java.sun.com/jsf/composite/%s", libraryName); //$NON-NLS-1$
             if (changeType == CHANGE_TYPE.REMOVED)
             {
                 _tags.put(uri, WHOLE_LIBRARY);
             } else
             {
                 LibEntry tags = _tags.get(uri);
                 if (tags == null)
                 {
                     tags = new LibEntry(createDescriptor(jsfResource));
                     _tags.put(uri, tags);
                 }
             }
         }
 
         /**
          * @return the built list of tag records.
          */
         public Map<String, JSFResourceBasedTagRecord> build()
         {
             final Map<String, JSFResourceBasedTagRecord> records = new HashMap<String, JSFResourceBasedTagRecord>();
             for (final Map.Entry<String, LibEntry> entry : _tags
                     .entrySet())
             {
                 if (entry.getValue() == WHOLE_LIBRARY)
                 {
                     records.put(entry.getKey(), WHOLE_LIB_RECORD);
                 }
                 else
                 {
                     final String uri = entry.getKey();
                     final List<FaceletTaglibTag> tags = new ArrayList<FaceletTaglibTag>(entry.getValue().getTags());
                     final TagRecordDescriptor descriptor = entry.getValue().getDescriptor();
                     final JSFResourceBasedTagRecord newRecord = new JSFResourceBasedTagRecord(
                             uri, tags, descriptor);
                     records.put(entry.getKey(), newRecord);
                 }
             }
             return records;
         }
 
         /**
          * @param locator
          * @param records
          * @return a list of taglib change events that reflect what will happen
          *         when my tags are merged into the map 'records'.
          */
         public List<TaglibChangedEvent> createMerge(
                 final AbstractFaceletTaglibLocator locator,
                 final Map<String, JSFResourceBasedTagRecord> records)
         {
             final Map<String, JSFResourceBasedTagRecord> newRecords = build();
             final List<TaglibChangedEvent> mergeEvents = new ArrayList<TaglibChangedEvent>();
             for (final Map.Entry<String, JSFResourceBasedTagRecord> entry : newRecords
                     .entrySet())
             {
                 TaglibChangedEvent event = null;
                 if (!records.containsKey(entry.getKey()))
                 {
                     event = new TaglibChangedEvent(locator, null,
                             entry.getValue(), CHANGE_TYPE.ADDED);
                 } else
                 {
                     final JSFResourceBasedTagRecord oldRecord = records
                             .get(entry.getKey());
                     final JSFResourceBasedTagRecord newRecord = oldRecord
                             .mergeTags(entry.getValue()._tags);
                     event = new TaglibChangedEvent(locator, oldRecord,
                             newRecord, CHANGE_TYPE.CHANGED);
                 }
                 mergeEvents.add(event);
             }
             return mergeEvents;
         }
 
         /**
          * @param locator
          * @param records
          * @return a list of change events that will result from removing my
          *         _tags from records.
          */
         public List<TaglibChangedEvent> createRemove(
                 final AbstractFaceletTaglibLocator locator,
                 final Map<String, JSFResourceBasedTagRecord> records)
         {
             final Map<String, JSFResourceBasedTagRecord> newRecords = build();
             final List<TaglibChangedEvent> mergeEvents = new ArrayList<TaglibChangedEvent>();
             for (final Map.Entry<String, JSFResourceBasedTagRecord> entry : newRecords
                     .entrySet())
             {
                 TaglibChangedEvent event = null;
                 final JSFResourceBasedTagRecord oldRecord = records.get(entry
                         .getKey());
                 if (oldRecord != null)
                 {
                     final JSFResourceBasedTagRecord record = entry.getValue();
                     if (record == WHOLE_LIB_RECORD)
                     {
                         event = new TaglibChangedEvent(locator, oldRecord,
                                 null, CHANGE_TYPE.REMOVED);
                     } else
                     {
                         final JSFResourceBasedTagRecord newRecord = oldRecord
                                 .removeTags(entry.getValue()._tags);
                         event = new TaglibChangedEvent(locator, oldRecord,
                                 newRecord, CHANGE_TYPE.CHANGED);
                     }
                 }
                 if (event != null)
                 {
                     mergeEvents.add(event);
                 }
             }
             return mergeEvents;
         }
 
         /**
          * @param events
          * @param withThese
          * @return a new merged map that contains withThese plus everything
          *         here.
          */
         public Map<String, JSFResourceBasedTagRecord> merge(
                 final List<TaglibChangedEvent> events,
                 final Map<String, JSFResourceBasedTagRecord> withThese)
         {
             final Map<String, JSFResourceBasedTagRecord> newMap = new HashMap<String, JSFResourceBasedTagRecord>(
                     withThese);
             for (final TaglibChangedEvent event : events)
             {
                 switch (event.getChangeType())
                 {
                     case ADDED:
                     case CHANGED:
                     {
                         final IFaceletTagRecord newRecord = event.getNewValue();
                         // doubles as null check
                         if (newRecord instanceof JSFResourceBasedTagRecord)
                         {
                             newMap.put(newRecord.getURI(),
                                     (JSFResourceBasedTagRecord) newRecord);
                         }
                     }
                     break;
                     case REMOVED:
                     {
                         final IFaceletTagRecord oldRecord = event.getOldValue();
                         if (oldRecord != null)
                         {
                             newMap.remove(oldRecord.getURI());
                         }
                     }
                     break;
                 }
             }
             return newMap;
         }
 
         private TagRecordDescriptor createDescriptor(final IJSFResourceFragment resource)
         {
             if (resource instanceof IWorkspaceJSFResourceFragment)
             {
                 if (resource instanceof WorkspaceJSFResourceContainer)
                 {
                     final IResource res = ((WorkspaceJSFResourceContainer)resource).getResource();
                     return new WorkspaceTagRecordDescriptor((IFolder) res);
                 }
                 final IResource res = ((IWorkspaceJSFResourceFragment) resource)
                         .getResource();
                 return new WorkspaceTagRecordDescriptor((IFile) res);
             } else if (resource instanceof IJarBasedJSFResource)
             {
                 final URL jarURL = ((IJarBasedJSFResource) resource)
                         .getJarURL();
                 final File file = JarUtilities.INSTANCE.getFile(jarURL);
                 if (file != null)
                 {
                     final String absolutePath = file.getAbsolutePath();
                     final String jarEntryName = ((IJarBasedJSFResource) resource)
                             .getJarEntryName();
                     return new JarTagRecordDescriptor(new Path(absolutePath),
                             jarEntryName);
                 }
             }
             return null;
         }
 
         private static class LibEntry
         {
             private final TagRecordDescriptor _descriptor;
             private final List<FaceletTaglibTag> _tags;
 
             /**
              * @param descriptor
              */
             public LibEntry(final TagRecordDescriptor descriptor)
             {
                 super();
                 _descriptor = descriptor;
                 _tags = new ArrayList<FaceletTaglibTag>();
             }
 
             public TagRecordDescriptor getDescriptor()
             {
                 return _descriptor;
             }
 
             public void addTag(final FaceletTaglibTag tag)
             {
                 _tags.add(tag);
             }
 
             public List<FaceletTaglibTag> getTags()
             {
                 return Collections.unmodifiableList(_tags);
             }
         }
     }
 }
