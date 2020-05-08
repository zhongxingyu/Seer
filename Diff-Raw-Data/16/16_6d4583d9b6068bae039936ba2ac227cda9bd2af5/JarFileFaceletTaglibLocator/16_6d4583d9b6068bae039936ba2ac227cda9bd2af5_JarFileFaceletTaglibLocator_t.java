 /*******************************************************************************
  * Copyright (c) 2008 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Cameron Bateman - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facelet.core.internal.registry.taglib;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jst.jsf.common.internal.finder.AbstractMatcher.AlwaysMatcher;
 import org.eclipse.jst.jsf.common.internal.finder.AbstractMatcher.IMatcher;
 import org.eclipse.jst.jsf.common.internal.finder.VisitorMatcher;
 import org.eclipse.jst.jsf.common.internal.finder.acceptor.JarEntryMatchingAcceptor;
 import org.eclipse.jst.jsf.common.internal.finder.matcher.TaglibJarEntryFinder;
 import org.eclipse.jst.jsf.common.internal.resource.ClasspathJarFile;
 import org.eclipse.jst.jsf.common.internal.resource.DefaultJarLocator;
 import org.eclipse.jst.jsf.common.internal.resource.IJarLocator;
 import org.eclipse.jst.jsf.common.internal.resource.IJarLocator.JarChangeEvent;
 import org.eclipse.jst.jsf.common.internal.resource.IJarLocator.JarChangeListener;
 import org.eclipse.jst.jsf.common.internal.resource.JavaCoreMediator;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCorePlugin;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.IFaceletTagRecord.JarTagRecordDescriptor;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.Listener.TaglibChangedEvent;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.Listener.TaglibChangedEvent.CHANGE_TYPE;
 import org.eclipse.jst.jsf.facelet.core.internal.registry.taglib.faceletTaglib.FaceletTaglib;
 
 /**
  * A locator that finds Facelet taglibs in jars on the classpath
  * 
  * @author cbateman
  * 
  */
 public class JarFileFaceletTaglibLocator extends AbstractFaceletTaglibLocator
 {
     /**
      * Default taglib finder that looks in meta-inf
      */
     public static final TaglibJarEntryFinder _taglibMetaInfFinder = new TaglibJarEntryFinder(
             Pattern.compile("META-INF/.*\\.taglib\\.xml")); //$NON-NLS-1$
     /**
      * Default finder that looks in the glassfish package.
      */
     public static final TaglibJarEntryFinder _taglibGlassfishFinder = new TaglibJarEntryFinder(
             Pattern.compile("com/sun/faces/metadata/taglib/.*\\.taglib\\.xml")); //$NON-NLS-1$
     private static final List<IMatcher> MATCHERS;
     static
     {
         final List<IMatcher> matchers = new ArrayList<IMatcher>();
         matchers.add(_taglibGlassfishFinder);
         matchers.add(_taglibMetaInfFinder);
         MATCHERS = Collections.unmodifiableList(matchers);
     }
     private static final String DISPLAYNAME = Messages.JarFileFaceletTaglibLocator_0;
     private static final String ID = JarFileFaceletTaglibLocator.class
             .getCanonicalName();
     private final TagRecordFactory _factory;
     private final Map<String, IFaceletTagRecord> _records;
     private final IJarLocator _locator;
    private final List<IMatcher> _jarEntryMatchers;
 
     /**
      * @param factory
      */
     public JarFileFaceletTaglibLocator(final TagRecordFactory factory)
     {
         this(factory, new DefaultJarLocator(
                 Collections.singletonList(new AlwaysMatcher()),
                 new JavaCoreMediator()));
     }
 
     /**
      * @param factory
      * @param jarProvider
      */
     public JarFileFaceletTaglibLocator(final TagRecordFactory factory,
             final IJarLocator jarProvider)
     {
         this(factory, jarProvider, MATCHERS);
     }
 
     /**
      * @param factory
      * @param jarProvider
      * @param jarEntryMatchers
      */
     public JarFileFaceletTaglibLocator(final TagRecordFactory factory,
            final IJarLocator jarProvider, final List<IMatcher>  jarEntryMatchers)
    {    
         super(ID, DISPLAYNAME);
         _factory = factory;
         _records = new HashMap<String, IFaceletTagRecord>();
         _locator = jarProvider;
         _jarEntryMatchers = jarEntryMatchers;
     }
 
     @Override
     public void start(final IProject project)
     {
         _locator.start(project);
         final List<LibJarEntry> tagLibsFound = new ArrayList<LibJarEntry>();
         final Collection<? extends ClasspathJarFile> jars = _locator
                 .getJars(project);
         for (final ClasspathJarFile cpJarFile : jars)
         {
             final JarFile jarFile = cpJarFile.getJarFile();
             if (jarFile != null)
             {
                 tagLibsFound.addAll(processJar(cpJarFile, _jarEntryMatchers));
             }
         }
         for (final LibJarEntry jarEntry : tagLibsFound)
         {
             final IFaceletTagRecord record = _factory.createRecords(jarEntry
                     .getTaglib(), new JarTagRecordDescriptor(
                     jarEntry.getPath(), jarEntry.getEntryName()));
             if (record != null)
             {
                 _records.put(record.getURI(), record);
             }
         }
         _locator.addListener(new JarChangeListener()
         {
             @Override
             public void changed(final JarChangeEvent event)
             {
                 switch (event.getType())
                 {
                     case JAR_ADDED:
                     {
                         final ClasspathJarFile jar = event.getJar();
                         final List<LibJarEntry> foundLibs = processJar(jar, _jarEntryMatchers);
                         for (final LibJarEntry lib : foundLibs)
                         {
                            final IFaceletTagRecord newRecord = _factory.createRecords(
                                     lib.getTaglib(),
                                     new JarTagRecordDescriptor(lib
                                             .getPath(), lib
                                             .getEntryName()));
                             _records.put(newRecord.getURI(), newRecord);
                             fireChangeEvent(new TaglibChangedEvent(
                                     JarFileFaceletTaglibLocator.this, null,
                                     newRecord,
                                     CHANGE_TYPE.ADDED));
                         }
                     }
                     break;
                     case JAR_REMOVED:
                     {
                         final ClasspathJarFile jar = event.getJar();
                         final List<IFaceletTagRecord>  removeRecords = 
                             new ArrayList<IFaceletTagRecord>();
                         for (final Map.Entry<String, IFaceletTagRecord> entry : _records
                                 .entrySet())
                         {
                             if (entry.getValue().getDescriptor()
                                     .getPath().equals(jar.getPath()))
                             {
                                 removeRecords.add(entry.getValue());
                             }
                         }
                         
                         for (final IFaceletTagRecord removeMe : removeRecords)
                         {
                             _records.remove(removeMe);
                             fireChangeEvent(new TaglibChangedEvent(
                                     JarFileFaceletTaglibLocator.this,
                                     removeMe, null,
                                     CHANGE_TYPE.REMOVED));
                         }
                     }
                     break;
                 }
             }
         });
         super.start(project);
     }
 
     @Override
     public void stop()
     {
         _locator.stop();
         super.stop();
     }
 
     @Override
     public Map<String, ? extends IFaceletTagRecord> doLocate(
             final IProject project)
     {
         return Collections.unmodifiableMap(_records);
     }
 
     /**
      * @param entry
      * @param defaultDtdStream
      * @throws Exception
      */
     private static List<LibJarEntry> processJar(final ClasspathJarFile cpJarFile,
            final List<IMatcher> jarEntryMatchers)
     {
         final List<LibJarEntry> tagLibsFound = new ArrayList<LibJarEntry>();
         final JarFile jarFile = cpJarFile.getJarFile();
         try
         {
             if (jarFile != null)
             {
                 final JarEntryMatchingAcceptor acceptor = new JarEntryMatchingAcceptor();
                 final VisitorMatcher<JarFile, JarEntry, String> matcher = new VisitorMatcher<JarFile, JarEntry, String>(
                        "", "", acceptor, jarEntryMatchers); //$NON-NLS-1$//$NON-NLS-2$
                 final Collection<? extends JarEntry> matchingEntries = matcher
                         .find(jarFile);
                 for (final JarEntry jarEntry : matchingEntries)
                 {
                     InputStream is = null;
                     try
                     {
                         is = jarFile.getInputStream(jarEntry);
                         final String name = jarEntry.getName();
                         final TagModelLoader loader = new TagModelLoader(name);
                         loader.loadFromInputStream(is);
                         final FaceletTaglib tagLib = loader.getTaglib();
                         if (tagLib != null)
                         {
                             tagLibsFound.add(new LibJarEntry(tagLib, cpJarFile
                                     .getPath(), name));
                         }
                     } catch (final Exception e)
                     {
                         FaceletCorePlugin.log(
                                 "Error initializing facelet registry entry", //$NON-NLS-1$
                                 e);
                     } finally
                     {
                         if (is != null)
                         {
                             // is.close();
                         }
                     }
                 }
             }
         } catch (final Exception e)
         {
             JSFCorePlugin.log(e,
                     "While locating jar based facelet tag libraries"); //$NON-NLS-1$
         } finally
         {
             if (jarFile != null)
             {
                 try
                 {
                     jarFile.close();
                 } catch (final IOException ioe)
                 {
                     FaceletCorePlugin.log("Error closing jar file", ioe); //$NON-NLS-1$
                 }
             }
         }
         return tagLibsFound;
     }
 
     private static class LibJarEntry
     {
         private final FaceletTaglib _taglib;
         private final String _entryName;
         private final IPath _iPath;
 
         public LibJarEntry(final FaceletTaglib taglib, final IPath iPath,
                 final String entryName)
         {
             super();
             _taglib = taglib;
             _iPath = iPath;
             _entryName = entryName;
         }
 
         public FaceletTaglib getTaglib()
         {
             return _taglib;
         }
 
         public String getEntryName()
         {
             return _entryName;
         }
 
         public IPath getPath()
         {
             return _iPath;
         }
     }
 }
