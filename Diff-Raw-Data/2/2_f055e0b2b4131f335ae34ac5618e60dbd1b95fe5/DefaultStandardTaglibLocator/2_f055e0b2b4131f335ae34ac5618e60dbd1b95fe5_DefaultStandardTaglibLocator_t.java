 package org.eclipse.jst.jsf.facelet.core.internal.registry.taglib;
 
 import java.io.File;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCorePlugin;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.IFaceletTagRecord.TagRecordDescriptor;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglib;
 
 /**
  * @author cbateman
  * 
  */
 public class DefaultStandardTaglibLocator extends AbstractFaceletTaglibLocator
 {
     private static final Set<String> taglibLocations;
     static
     {
         final Set<String> set = new HashSet<String>();
         set.add("/std-taglibs/html_basic.taglib.xml"); //$NON-NLS-1$
         set.add("/std-taglibs/composite.taglib.xml"); //$NON-NLS-1$
         set.add("/std-taglibs/facelets_jsf_core.taglib.xml"); //$NON-NLS-1$
         set.add("/std-taglibs/jstl-core.taglib.xml"); //$NON-NLS-1$
         set.add("/std-taglibs/jstl-fn.taglib.xml"); //$NON-NLS-1$
         set.add("/std-taglibs/ui.taglib.xml"); //$NON-NLS-1$
         taglibLocations = Collections.unmodifiableSet(set);
     }
     private static final Set<MyTagRecordDescriptor> DEFAULT_TAGLIBS;
     static
     {
         Set<MyTagRecordDescriptor>  taglibs = new HashSet<MyTagRecordDescriptor>();
         for (final String location : taglibLocations)
         {
             try
             {
                 final URL url = FaceletCorePlugin.getDefault().getBundle()
                         .getEntry(location);
                 final URL fileURL = FileLocator.toFileURL(url);
                File file = new File(fileURL.getPath());
                 final InputStream openStream = fileURL.openStream();
                 final TagModelLoader loader = new TagModelLoader(
                         file.getAbsolutePath());
                 loader.loadFromInputStream(openStream);
                 final FaceletTaglib taglib = loader.getTaglib();
                 MyTagRecordDescriptor desc = new MyTagRecordDescriptor(new Path(fileURL.toString()), taglib);
                 taglibs.add(desc);
             } catch (final Exception e)
             {
                 FaceletCorePlugin.log(
                         "Trying to load default taglib for: " + location, e); //$NON-NLS-1$
             }
         }
         DEFAULT_TAGLIBS = Collections.unmodifiableSet(taglibs);
     }
     private HashMap<String, IFaceletTagRecord> _defaultRecords;
 
     /**
      * 
      */
     public DefaultStandardTaglibLocator()
     {
         super("", ""); //$NON-NLS-1$//$NON-NLS-2$
         _defaultRecords = new HashMap<String, IFaceletTagRecord>();
     }
 
     @Override
     public void start(IProject project)
     {
         final TagRecordFactory factory = new TagRecordFactory(project,
                 false);
         
         for (final MyTagRecordDescriptor desc : DEFAULT_TAGLIBS)
         {
             final IFaceletTagRecord record = factory.createRecords(desc.getTaglib(),
                     desc);
             if (record != null)
             {
                 _defaultRecords.put(record.getURI(), record);
             }
         }
         super.start(project);
     }
 
     @Override
     protected Map<String, ? extends IFaceletTagRecord> doLocate(IProject context)
     {
         return Collections.unmodifiableMap(_defaultRecords);
     }
 
     private static class MyTagRecordDescriptor extends TagRecordDescriptor
     {
         private final IPath _path;
         private final FaceletTaglib  _taglib;
         
         public MyTagRecordDescriptor(final IPath path, final FaceletTaglib taglib)
         {
             super(Source.JAR);
             _path = path;
             _taglib = taglib;
         }
 
         @Override
         public IResource getResource()
         {
             return null;
         }
 
         @Override
         public IPath getPath()
         {
             return _path;
         }
 
         public FaceletTaglib getTaglib()
         {
             return _taglib;
         }
     }
 }
