 /**
  * Copyright (c) 2013 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.maven;
 
 import static org.sourcepit.common.utils.io.IO.buffOut;
 import static org.sourcepit.common.utils.io.IO.fileOut;
 import static org.sourcepit.common.utils.io.IO.write;
 import static org.sourcepit.common.utils.lang.Exceptions.pipe;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.DefaultModelWriter;
 import org.apache.maven.plugin.LegacySupport;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.artifact.Artifact;
 import org.sonatype.aether.installation.InstallRequest;
 import org.sonatype.aether.installation.InstallationException;
 import org.sourcepit.common.manifest.osgi.BundleManifest;
 import org.sourcepit.common.manifest.osgi.resource.BundleManifestResourceImpl;
 import org.sourcepit.common.maven.aether.ArtifactFactory;
 import org.sourcepit.common.maven.model.ArtifactKey;
 import org.sourcepit.common.maven.model.ArtifactKeyBuilder;
 import org.sourcepit.common.maven.model.MavenArtifact;
 import org.sourcepit.common.maven.model.MavenDependency;
 import org.sourcepit.common.utils.io.Write.ToStream;
 import org.sourcepit.common.utils.props.LinkedPropertiesMap;
 import org.sourcepit.common.utils.props.PropertiesMap;
 import org.sourcepit.maven.dependency.model.DependencyModel;
 import org.sourcepit.maven.dependency.model.DependencyNode;
 import org.sourcepit.maven.dependency.model.DependencyTree;
 import org.sourcepit.osgify.core.model.context.BundleCandidate;
 import org.sourcepit.osgify.core.model.context.OsgifyContext;
 import org.sourcepit.osgify.core.packaging.Repackager;
 import org.sourcepit.osgify.maven.OsgifyModelBuilder.Result;
 
 /**
  * @goal osgify-dependencies
  * @requiresProject true
  * @requiresDependencyResolution compile
  * @author Bernd Vogt <bernd.vogt@sourcepit.org>
  */
 public class OsgifyProjectDependenciesMojo extends AbstractGuplexedMojo
 {
    /** @parameter default-value="${project.build.directory}" */
    private File targetDir;
 
    /** @parameter default-value="${project}" */
    private MavenProject project;
 
    /** @parameter default-value="**.internal.**, **.internal, **.impl.**, **.impl" */
    private String internalPackages;
 
    @Inject
    private OsgifyModelBuilder modelBuilder;
 
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException
    {
       PropertiesMap properties = new LinkedPropertiesMap();
       properties.put("osgifier.internalPackages", internalPackages);
 
       final ManifestGeneratorFilter generatorFilter = new ManifestGeneratorFilter();
 
       final Result result = modelBuilder.build(generatorFilter, properties, project.getDependencies());
       final DependencyModel dependencyModel = result.dependencyModel;
       final OsgifyContext bundleModel = result.osgifyModel;
 
       writeModel(new File(targetDir, "dependencyModel.xml"), dependencyModel);
       writeModel(new File(targetDir, "osgifyModel.xml"), bundleModel);
 
 
       EList<BundleCandidate> bundles = bundleModel.getBundles();
 
       final Map<ArtifactKey, BundleCandidate> keyToBundle = new HashMap<ArtifactKey, BundleCandidate>();
       final Map<ArtifactKey, Model> keyToPom = new HashMap<ArtifactKey, Model>();
       final Map<ArtifactKey, ArtifactKey> keyToNewKey = new HashMap<ArtifactKey, ArtifactKey>();
       for (BundleCandidate bundle : bundles)
       {
          final MavenArtifact artifact = bundle.getExtension(MavenArtifact.class);
          final ArtifactKey key = artifact.getArtifactKey();
          keyToBundle.put(key, bundle);
 
          final Model pom = getPom(bundle);
          keyToPom.put(key, pom);
 
          if (generatorFilter.isGenerateManifest(bundle))
          {
             final ArtifactKeyBuilder newKey = new ArtifactKeyBuilder();
             newKey.setGroupId(pom.getGroupId());
             newKey.setArtifactId(pom.getArtifactId());
             newKey.setType(key.getType());
             newKey.setClassifier(key.getClassifier());
             newKey.setVersion(pom.getVersion());
             keyToNewKey.put(key, newKey.toArtifactKey());
          }
          else
          {
             keyToNewKey.put(key, key);
          }
       }
 
       for (BundleCandidate bundle : bundles)
       {
          final ArtifactKey key = getArtifactKey(bundle);
          final Model pom = keyToPom.get(key);
 
          final DependencyTree dependencyTree = dependencyModel.getDependencyTree(key);
          if (dependencyTree != null)
          {
             for (DependencyNode dependencyNode : dependencyTree.getDependencyNodes())
             {
                if (dependencyNode.isSelected())
                {
                   pom.addDependency(toDependency(keyToNewKey.get(dependencyNode.getArtifact().getArtifactKey()),
                      dependencyNode));
                }
             }
          }
       }
 
       for (BundleCandidate bundle : bundles)
       {
          final ArtifactKey key = getArtifactKey(bundle);
 
          final File dir = getWorkDir(bundle);
 
          writeModel(new File(dir, "MANIFEST.MF"), EcoreUtil.copy(bundle.getManifest()));
 
          final File pomFile = new File(dir, "pom.xml");
          try
          {
             new DefaultModelWriter().write(pomFile, null, keyToPom.get(key));
          }
          catch (IOException e)
          {
             throw pipe(e);
          }
 
          if (generatorFilter.isGenerateManifest(bundle) && !generatorFilter.isSourceBundle(bundle))
          {
            final File srcJarFile = bundle.getLocation();
            final File destJarFile = new File(dir, getBundleId(bundle) + ".jar");
            repackager.copyJarAndInjectManifest(srcJarFile, destJarFile, bundle.getManifest());
 
             InstallRequest request = new InstallRequest();
 
             Artifact artifact = artifactFactory.createArtifact(keyToNewKey.get(key));
             Artifact pomArtifact = artifactFactory.createArtifact(artifact, null, "pom");
             request.addArtifact(pomArtifact.setFile(pomFile));
             request.addArtifact(artifact.setFile(destJarFile));
 
             BundleCandidate sourceBundle = bundle.getSourceBundle();
             if (sourceBundle != null && sourceBundle.getLocation() != null)
             {
               ArtifactKey srcKey = getArtifactKey(sourceBundle);
               final Artifact sourceArtifact = artifactFactory.createArtifact(artifact, srcKey.getClassifier(),
                  srcKey.getType());
               request.addArtifact(sourceArtifact.setFile(sourceBundle.getLocation()));
             }
 
             try
             {
                repositorySystem.install(buildContext.getRepositorySession(), request);
             }
             catch (InstallationException e)
             {
                throw pipe(e);
             }
 
          }
       }
 
    }
 
    private File getWorkDir(BundleCandidate bundle)
    {
       final String bundleDir = (bundle.isNativeBundle() ? "native/" : "osgified/") + getBundleId(bundle);
 
       File dir = new File(targetDir, bundleDir);
       return dir;
    }
 
    private String getBundleId(BundleCandidate bundle)
    {
       return bundle.getSymbolicName() + "_" + bundle.getVersion();
    }
 
    @Inject
    private RepositorySystem repositorySystem;
 
    @Inject
    private Repackager repackager;
 
    @Inject
    private ArtifactFactory artifactFactory;
 
    @Inject
    private LegacySupport buildContext;
 
 
    private Dependency toDependency(ArtifactKey newKey, DependencyNode dependencyNode)
    {
       final MavenDependency declaredDependency = dependencyNode.getDeclaredDependency();
       final Dependency dependency = new Dependency();
       dependency.setGroupId(newKey.getGroupId());
       dependency.setArtifactId(newKey.getArtifactId());
       dependency.setVersion(declaredDependency.getVersionConstraint());
       dependency.setClassifier(newKey.getClassifier());
       final String type = newKey.getType();
       if (!"jar".equals(type))
       {
          dependency.setType(type);
       }
       final boolean optional = declaredDependency.isOptional();
       if (optional)
       {
          dependency.setOptional(optional);
       }
       final String scope = declaredDependency.getScope().toString();
       if (!"compile".equals(scope))
       {
          dependency.setScope(scope);
       }
       return dependency;
    }
 
 
    private ArtifactKey getArtifactKey(BundleCandidate bundle)
    {
       return bundle.getExtension(MavenArtifact.class).getArtifactKey();
    }
 
 
    private Model getPom(BundleCandidate bundle)
    {
       Model model = new Model();
       model.setModelVersion("4.0.0");
       // TODO
       model.setGroupId("srcpit.bundles");
       model.setArtifactId(bundle.getSymbolicName());
       // TODO
       // model.setVersion(bundle.getVersion().toMinimalString());
       model.setVersion(getArtifactKey(bundle).getVersion());
 
       return model;
    }
 
    private void writeModel(File file, BundleManifest model)
    {
       final ToStream<EObject> toStream = new ToStream<EObject>()
       {
          @Override
          public void write(OutputStream out, EObject model) throws Exception
          {
             final BundleManifestResourceImpl resource = new BundleManifestResourceImpl();
             resource.getContents().add(model);
             resource.save(out, null);
          }
       };
       write(toStream, buffOut(fileOut(file, true)), model);
    }
 
    private void writeModel(File file, EObject model)
    {
       final ToStream<EObject> toStream = new ToStream<EObject>()
       {
          @Override
          public void write(OutputStream out, EObject model) throws Exception
          {
             final XMLResourceImpl resource = new XMLResourceImpl();
             resource.getContents().add(model);
             resource.save(out, null);
          }
       };
       write(toStream, buffOut(fileOut(file, true)), model);
    }
 }
