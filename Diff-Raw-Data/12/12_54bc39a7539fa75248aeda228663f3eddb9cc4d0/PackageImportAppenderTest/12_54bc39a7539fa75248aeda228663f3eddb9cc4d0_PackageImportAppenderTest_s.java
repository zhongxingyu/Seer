 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.osgify.core.bundle;
 
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.hamcrest.core.IsNull.nullValue;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.sourcepit.common.manifest.osgi.BundleHeaderName.IMPORT_PACKAGE;
 import static org.sourcepit.osgify.core.bundle.PackageImportAppender.determineImportVersionRange;
 import static org.sourcepit.osgify.core.bundle.PackageImportAppender.DemanderRole.CONSUMER;
 import static org.sourcepit.osgify.core.bundle.PackageImportAppender.DemanderRole.FRIEND;
 import static org.sourcepit.osgify.core.bundle.PackageImportAppender.DemanderRole.PROVIDER;
 import static org.sourcepit.osgify.core.bundle.TestContextHelper.appendPackageExport;
 import static org.sourcepit.osgify.core.bundle.TestContextHelper.appendTypeWithReferences;
 import static org.sourcepit.osgify.core.bundle.TestContextHelper.newBundleCandidate;
 import static org.sourcepit.osgify.core.bundle.TestContextHelper.newPackageExport;
 
 import javax.inject.Inject;
 
 import org.hamcrest.core.IsEqual;
 import org.junit.Test;
 import org.sonatype.guice.bean.containers.InjectedTest;
 import org.sourcepit.common.manifest.osgi.Version;
 import org.sourcepit.common.manifest.osgi.VersionRange;
 import org.sourcepit.osgify.core.model.context.BundleCandidate;
 import org.sourcepit.osgify.core.model.context.BundleReference;
 import org.sourcepit.osgify.core.model.context.ContextModelFactory;
 import org.sourcepit.osgify.core.model.java.JavaArchive;
 import org.sourcepit.osgify.core.model.java.JavaModelFactory;
 
 /**
  * @author Bernd Vogt <bernd.vogt@sourcepit.org>
  */
 public class PackageImportAppenderTest extends InjectedTest
 {
    @Inject
    private PackageImportAppender importAppender;
 
    @Test
    public void testSortAlphabetically()
    {
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       appendPackageExport(ref, newPackageExport("a3", null));
 
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "z.Bar", "a3.Bar");
       appendTypeWithReferences(jArchive, "z.Bar", 47, "a2.Bar");
       appendTypeWithReferences(jArchive, "a2.Bar", 47, "a.Bar");
 
       BundleCandidate bundle = newBundleCandidate("1.0.1", "OSGi/Minimum-1.0", jArchive);
       bundle.getDependencies().add(ref);
       appendPackageExport(bundle, newPackageExport("z", null));
       appendPackageExport(bundle, newPackageExport("a2", null));
       appendPackageExport(bundle, newPackageExport("a", null));
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("a,a2,a3,z"));
    }
 
    @Test
    public void testImportReferencedPackages()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "org.sourcepit.Foo", 47, "foo.Bar");
       BundleCandidate bundle = newBundleCandidate("1.0.1", "OSGi/Minimum-1.0", jArchive);
 
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       appendPackageExport(ref, newPackageExport("foo", "1.2.3"));
 
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo;version=\"[1.2.3,2)\""));
    }
 
    @Test
    public void testFilterPlatformPackages()
    {
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       appendPackageExport(ref, newPackageExport("b", null));
 
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "javax.microedition.io.Foo", "b.Foo");
 
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("b"));
    }
 
    @Test
    public void testIgnoreUnresolveablePackages()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "b.Foo");
 
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, nullValue());
    }
 
    @Test
    public void testCompatibeVersionsForPublicSelfImports()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "b.Foo");
 
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
       appendPackageExport(bundle, newPackageExport("b", "2"));
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("b;version=\"[2,3)\""));
    }
 
    @Test
    public void testDefaultVersionIfPackageIsExportedWithDefaultVersion()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "b.Foo", "foo.Foo");
 
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
       appendPackageExport(bundle, newPackageExport("b", null));
 
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       appendPackageExport(ref, newPackageExport("foo", null));
 
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
 
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("b,foo"));
    }
 
    @Test
    public void testIgnorePlatformFamilyPackages()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "javax.xml.stream.Foo", "sun.misc.Foo");
       BundleCandidate bundle = newBundleCandidate("1.0.1", "JavaSE-1.6", jArchive);
       importAppender.append(bundle);
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertNull(packageImports);
    }
 
    @Test
    public void testUseVersionRangeOfReference()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "foo.Foo");
 
       // test package version fits into version range of reference
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("[1,2)"));
       appendPackageExport(ref, newPackageExport("foo", "1.1"));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo;version=\"[1,2)\""));
 
       // test package version doesn't fit into version range of reference
       bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("[1,2)"));
       appendPackageExport(ref, newPackageExport("foo", "3"));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo;version=\"[3,4)\""));
 
       // test package version is default version
       bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("[1,2)"));
       appendPackageExport(ref, newPackageExport("foo", "0.0.0"));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo"));
 
       // test package version is default version (null)
       bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("[1,2)"));
       appendPackageExport(ref, newPackageExport("foo", null));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo"));
 
       // test ref version isn't a "real" range
       bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("1"));
       appendPackageExport(ref, newPackageExport("foo", "2"));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo;version=\"[1,3)\"")); // versions are merged, see merge ref test
    }
 
    @Test
    public void testOptionalReference()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "foo.Foo");
 
       // test package version fits into version range of reference
       BundleCandidate bundle = newBundleCandidate("1.0.1", "CDC-1.0/Foundation-1.0", jArchive);
 
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setOptional(true);
       appendPackageExport(ref, newPackageExport("foo", null));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
      assertThat(packageImports, IsEqual.equalTo("foo;optional:=true"));
    }
 
    @Test
    public void testMergeReferenceRangeWithTargetBundleVersion()
    {
       JavaArchive jArchive = JavaModelFactory.eINSTANCE.createJavaArchive();
       appendTypeWithReferences(jArchive, "a.Bar", 47, "foo.Foo");
 
       BundleCandidate bundle = newBundleCandidate("999", "CDC-1.0/Foundation-1.0", jArchive);
 
       BundleReference ref = ContextModelFactory.eINSTANCE.createBundleReference();
       ref.setVersionRange(VersionRange.parse("1"));
       appendPackageExport(ref, newPackageExport("foo", "2"));
       bundle.getDependencies().add(ref);
 
       importAppender.append(bundle);
       String packageImports = bundle.getManifest().getHeaderValue(IMPORT_PACKAGE);
       assertThat(packageImports, IsEqual.equalTo("foo;version=\"[1,3)\""));
    }
 
    @Test
    public void testWiden()
    {
       VersionRange r;
       Version v;
 
       r = VersionRange.parse("0");
       v = Version.parse("1");
       assertThat(determineImportVersionRange(r, v, CONSUMER).toString(), IsEqual.equalTo("[0,2)"));
       assertThat(determineImportVersionRange(r, v, FRIEND).toString(), IsEqual.equalTo("[0,1.1)"));
       assertThat(determineImportVersionRange(r, v, PROVIDER).toString(), IsEqual.equalTo("[0,1.1)"));
 
       // java.xmal jar=1.4.2 package=1.4 (apply equivalent)
       r = VersionRange.parse("1.4.2");
       v = Version.parse("1.4");
       assertThat(determineImportVersionRange(r, v, CONSUMER).toString(), IsEqual.equalTo("[1.4,2)"));
       assertThat(determineImportVersionRange(r, v, FRIEND).toString(), IsEqual.equalTo("[1.4,1.5)"));
       assertThat(determineImportVersionRange(r, v, PROVIDER).toString(), IsEqual.equalTo("[1.4,1.5)"));
 
       r = VersionRange.parse("1.4.2");
       v = Version.parse("1.3");
       assertThat(determineImportVersionRange(r, v, CONSUMER).toString(), IsEqual.equalTo("[1.3,2)"));
       assertThat(determineImportVersionRange(r, v, FRIEND).toString(), IsEqual.equalTo("[1.3,1.4)"));
       assertThat(determineImportVersionRange(r, v, PROVIDER).toString(), IsEqual.equalTo("[1.3,1.4)"));
 
       r = VersionRange.parse("[2,4]");
       v = Version.parse("3");
       assertThat(determineImportVersionRange(r, v, CONSUMER).toString(), IsEqual.equalTo("[2,4]"));
       assertThat(determineImportVersionRange(r, v, FRIEND).toString(), IsEqual.equalTo("[2,4]"));
       assertThat(determineImportVersionRange(r, v, PROVIDER).toString(), IsEqual.equalTo("[2,4]"));
 
       r = VersionRange.parse("(2,4]");
       v = Version.parse("2");
       assertThat(determineImportVersionRange(r, v, CONSUMER).toString(), IsEqual.equalTo("[2,3)"));
       assertThat(determineImportVersionRange(r, v, FRIEND).toString(), IsEqual.equalTo("[2,2.1)"));
       assertThat(determineImportVersionRange(r, v, PROVIDER).toString(), IsEqual.equalTo("[2,2.1)"));
    }
 
    @Test
    public void testTrimQualifierFromRange() throws Exception
    {
       VersionRange range;
       
       range = VersionRange.parse("0.0.0");
       assertThat(PackageImportAppender.trimQualifiers(range).toString(), equalTo("0.0.0"));
       
       range = VersionRange.parse("[0,1.0.0.rc4)");
       assertThat(PackageImportAppender.trimQualifiers(range).toString(), equalTo("[0,1)"));
       
       range = VersionRange.parse("[0.0.0.murks,1.0.0.rc4)");
       assertThat(PackageImportAppender.trimQualifiers(range).toString(), equalTo("[0,1)"));
       
       range = VersionRange.parse("[0.0.1.murks,0.0.1.rc4)");
       assertThat(PackageImportAppender.trimQualifiers(range).toString(), equalTo("[0.0.1,0.0.1)"));
    }
 
 }
