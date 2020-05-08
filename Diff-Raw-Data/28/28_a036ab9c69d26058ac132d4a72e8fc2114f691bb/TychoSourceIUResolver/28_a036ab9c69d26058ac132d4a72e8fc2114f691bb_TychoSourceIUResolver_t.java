 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.tpmp.resolver.tycho;
 
 import static org.sourcepit.common.utils.lang.Exceptions.pipe;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.logging.Logger;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.sisu.equinox.EquinoxServiceFactory;
 import org.eclipse.tycho.artifacts.TargetPlatform;
 import org.eclipse.tycho.osgi.adapters.MavenLoggerAdapter;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolutionResult;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolutionResult.Entry;
 import org.eclipse.tycho.p2.resolver.facade.P2Resolver;
 import org.eclipse.tycho.p2.resolver.facade.P2ResolverFactory;
 import org.sourcepit.common.manifest.osgi.BundleManifest;
 import org.sourcepit.common.manifest.osgi.BundleSymbolicName;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.common.manifest.osgi.resource.BundleManifestResourceImpl;
 import org.sourcepit.tpmp.resolver.TargetPlatformResolutionHandler;
 
 import com.google.common.base.Optional;
 
 @Named
 public class TychoSourceIUResolver
 {
    @Inject
    private MavenProjectFacade projectFacade;
 
    @Inject
    private EquinoxServiceFactory equinox;
 
    @Inject
    private Logger logger;
 
    public void resolveSources(MavenSession session, final TargetPlatform targetPlatform,
       Collection<String> sourceTargetBundles, TargetPlatformResolutionHandler handler)
    {
       final Set<String> sourceTargets = new HashSet<String>(sourceTargetBundles);
       if (sourceTargets.isEmpty())
       {
          return;
       }
 
       final Map<String, MavenProject> projectsMap = projectFacade.createVidToProjectMap(session);
 
       final P2Resolver resolver = createResolver();
 
       final ClassLoader classLoader = targetPlatform.getClass().getClassLoader();
 
       final P2TargetPlatformDAO tpDAO = new P2TargetPlatformDAO(classLoader);
       final InstallableUnitDAO iuDAO = tpDAO.getInstallableUnitDAO();
 
       for (final Object unit : tpDAO.getInstallableUnits(targetPlatform))
       {
          if (sourceTargets.isEmpty())
          {
             return;
          }
 
          if (hasSourceCapability(iuDAO, unit))
          {
             final String symbolicName = iuDAO.getId(unit);
             final String version = iuDAO.getVersion(unit).toString();
 
             final BundleManifest manifest = getManifest(iuDAO, unit);
 
             String[] targetIdAndVersion = getTargetIdAndVersion(manifest);
             if (targetIdAndVersion == null)
             {
                targetIdAndVersion = getTargetIdAndVersion(symbolicName, version);
             }
 
             if (targetIdAndVersion != null)
             {
                final String targetKey = targetIdAndVersion[0] + "_" + targetIdAndVersion[1];
                if (sourceTargets.remove(targetKey))
                {
                   final P2ResolutionResult result = resolver.resolveInstallableUnit(targetPlatform, symbolicName,
                      version);
 
                   for (Entry entry : result.getArtifacts())
                   {
                     final Optional<MavenProject> mavenProject = projectFacade.getMavenProject(projectsMap,
                        targetIdAndVersion[0], targetIdAndVersion[1]);
                      final File location = projectFacade.getLocation(entry, mavenProject);
                      if (location != null && location.exists())
                      {
                         handler.handlePlugin(entry.getId(), entry.getVersion(), location, false, mavenProject.orNull());
                      }
                   }
                }
             }
          }
       }
    }
 
    private P2Resolver createResolver()
    {
       final P2ResolverFactory factory = equinox.getService(P2ResolverFactory.class);
       final P2Resolver resolver = factory.createResolver(new MavenLoggerAdapter(logger, false));
       return resolver;
    }
 
    private String[] getTargetIdAndVersion(String symbolicName, String version)
    {
       String targetId = null;
       if (symbolicName.endsWith(".source"))
       {
          targetId = symbolicName.substring(0, symbolicName.length() - ".source".length());
       }
       if (targetId == null || version == null)
       {
          return null;
       }
       return new String[] { targetId, version };
    }
 
    private String[] getTargetIdAndVersion(BundleManifest manifest)
    {
       String targetId = null;
       String version = null;
 
       final BundleSymbolicName bundleSymbolicName = manifest.getBundleSymbolicName();
       final Version bundleVersion = manifest.getBundleVersion();
       if (bundleSymbolicName != null && bundleVersion != null)
       {
          String symbolicName = bundleSymbolicName.getSymbolicName();
          String value = manifest.getHeaderValue("Eclipse-SourceBundle");
          if (value == null)
          {
             if (symbolicName.endsWith(".source"))
             {
                targetId = symbolicName.substring(0, symbolicName.length() - ".source".length());
             }
             version = bundleVersion.toFullString();
          }
          else
          {
             String[] segments = value.split(";");
             targetId = segments[0];
             for (int i = 1; i < segments.length; i++)
             {
                String segment = segments[i];
                if (segment.startsWith("version="))
                {
                   segment = segment.substring("version=".length());
                   if (segment.startsWith("\""))
                   {
                      segment = segment.substring(1);
                   }
                   if (segment.endsWith("\""))
                   {
                      segment = segment.substring(0, segment.length() - 1);
                   }
                   version = segment;
                   break;
                }
             }
          }
       }
 
       if (targetId == null || version == null)
       {
          return null;
       }
       return new String[] { targetId, version };
    }
 
    private static BundleManifest getManifest(InstallableUnitDAO iuDao, Object unit)
    {
       final TouchpointDataDAO tdDAO = iuDao.getTouchpointDataDAO();
       final TouchpointInstructionDAO tiDAO = tdDAO.getTouchpointInstructionDAO();
       for (Object point : iuDao.getTouchpointData(unit))
       {
          Object instruction = tdDAO.getInstruction(point, "manifest");
          if (instruction != null)
          {
             String manifest = tiDAO.getBody(instruction);
 
             Resource resource = new BundleManifestResourceImpl();
             try
             {
                resource.load(new ByteArrayInputStream(manifest.getBytes("UTF-8")), null);
             }
             catch (IOException e)
             {
                throw pipe(e);
             }
             return (BundleManifest) resource.getContents().get(0);
          }
       }
       return null;
    }
 
    private static boolean hasSourceCapability(InstallableUnitDAO iuDao, Object unit)
    {
       for (Object capabilty : iuDao.getProvidedCapabilities(unit))
       {
          if (capabilty.toString().startsWith("org.eclipse.equinox.p2.eclipse.type/source/"))
          {
             return true;
          }
       }
       return false;
    }
 
    private abstract static class AbstractDAO
    {
       private final String className;
 
       protected final ClassLoader classLoader;
 
       private Class<?> clazz;
 
       protected AbstractDAO(ClassLoader classLoader, String className)
       {
          this.classLoader = classLoader;
          this.className = className;
       }
 
       protected Class<?> getClazz()
       {
          if (clazz == null)
          {
             try
             {
                clazz = classLoader.loadClass(className);
             }
             catch (ClassNotFoundException e)
             {
                throw new IllegalStateException(e);
             }
          }
          return clazz;
       }
 
       protected Method getMethod(String methodName, Class<?>... argTypes)
       {
          return getMethod(getClazz(), methodName, argTypes);
       }
 
       private static Method getMethod(Class<?> clazz, String methodName, Class<?>... argTypes)
       {
          try
          {
             return clazz.getDeclaredMethod(methodName, argTypes);
          }
          catch (NoSuchMethodException e)
          {
             for (Class<?> interfaze : clazz.getInterfaces())
             {
                final Method method = getMethod(interfaze, methodName, argTypes);
                if (method != null)
                {
                   return method;
                }
             }
             final Class<?> superclass = clazz.getSuperclass();
             if (superclass != null)
             {
                final Method method = getMethod(superclass, methodName, argTypes);
                if (method != null)
                {
                   return method;
                }
             }
             return null;
          }
       }
 
       @SuppressWarnings("unchecked")
       protected static <T> T invoke(Method method, Object target, Object... args)
       {
          try
          {
             return (T) method.invoke(target, args);
          }
          catch (IllegalAccessException e)
          {
             throw pipe(e);
          }
          catch (InvocationTargetException e)
          {
             final Throwable t = e.getTargetException();
             if (t instanceof RuntimeException)
             {
                throw (RuntimeException) t;
             }
             if (t instanceof Error)
             {
                throw (Error) t;
             }
             if (t instanceof Exception)
             {
                throw pipe((Exception) t);
             }
             throw new IllegalStateException(t);
          }
       }
    }
 
    private static class P2TargetPlatformDAO extends AbstractDAO
    {
       private InstallableUnitDAO iuDAO;
 
       private Method getInstallableUnits;
 
       public P2TargetPlatformDAO(ClassLoader classLoader)
       {
          super(classLoader, "org.eclipse.tycho.artifacts.p2.P2TargetPlatform");
       }
 
       public InstallableUnitDAO getInstallableUnitDAO()
       {
          if (iuDAO == null)
          {
             iuDAO = new InstallableUnitDAO(classLoader);
          }
          return iuDAO;
       }
 
       public Collection<?> getInstallableUnits(Object targetPlatform)
       {
          if (getInstallableUnits == null)
          {
             getInstallableUnits = getMethod("getInstallableUnits");
          }
          return invoke(getInstallableUnits, targetPlatform);
       }
    }
 
    private static class InstallableUnitDAO extends AbstractDAO
    {
       private TouchpointDataDAO tdDAO;
 
       private Method getId;
       private Method getVersion;
       private Method getProvidedCapabilities;
       private Method getTouchpointData;
 
       public InstallableUnitDAO(ClassLoader classLoader)
       {
          super(classLoader, "org.eclipse.equinox.p2.metadata.IInstallableUnit");
       }
 
       public TouchpointDataDAO getTouchpointDataDAO()
       {
          if (tdDAO == null)
          {
             tdDAO = new TouchpointDataDAO(classLoader);
          }
          return tdDAO;
       }
 
       public String getId(Object unit)
       {
          if (getId == null)
          {
             getId = getMethod("getId");
          }
          return invoke(getId, unit);
       }
 
       public Object getVersion(Object unit)
       {
          if (getVersion == null)
          {
             getVersion = getMethod("getVersion");
          }
          return invoke(getVersion, unit);
       }
 
       public Collection<?> getProvidedCapabilities(Object installableUnit)
       {
          if (getProvidedCapabilities == null)
          {
             getProvidedCapabilities = getMethod("getProvidedCapabilities");
          }
          return invoke(getProvidedCapabilities, installableUnit);
       }
 
       public Collection<?> getTouchpointData(Object unit)
       {
          if (getTouchpointData == null)
          {
             getTouchpointData = getMethod("getTouchpointData");
          }
          return invoke(getTouchpointData, unit);
       }
    }
 
    private static class TouchpointDataDAO extends AbstractDAO
    {
       private TouchpointInstructionDAO tiDAO;
       private Method getInstruction;
 
       protected TouchpointDataDAO(ClassLoader classLoader)
       {
          super(classLoader, "org.eclipse.equinox.p2.metadata.ITouchpointData");
       }
 
       public TouchpointInstructionDAO getTouchpointInstructionDAO()
       {
          if (tiDAO == null)
          {
             tiDAO = new TouchpointInstructionDAO(classLoader);
          }
          return tiDAO;
       }
 
       public Object getInstruction(Object touchpointData, String key)
       {
          if (getInstruction == null)
          {
             getInstruction = getMethod("getInstruction", String.class);
          }
          return invoke(getInstruction, touchpointData, key);
       }
    }
 
    private static class TouchpointInstructionDAO extends AbstractDAO
    {
       private Method getBody;
 
       protected TouchpointInstructionDAO(ClassLoader classLoader)
       {
          super(classLoader, "org.eclipse.equinox.p2.metadata.ITouchpointInstruction");
       }
 
       public String getBody(Object instruction)
       {
          if (getBody == null)
          {
             getBody = getMethod("getBody");
          }
          return invoke(getBody, instruction);
       }
    }
 }
