 /**
  * Copyright (c) 2012 Sourcepit.org contributors and others. All rights reserved. This program and the accompanying
  * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  */
 
 package org.sourcepit.maven.bootstrap.it;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 
 import java.io.File;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Test;
 import org.sourcepit.common.maven.testing.ExternalMavenTest;
 import org.sourcepit.common.testing.Environment;
import org.sourcepit.maven.bootstrap.it.Report;
import org.sourcepit.maven.bootstrap.it.TestBootstrapParticipant;
import org.sourcepit.maven.bootstrap.it.TestBootstrapper;
 
 /**
  * @author Bernd Vogt <bernd.vogt@sourcepit.org>
  */
 public class BuildBootstrapperIT extends ExternalMavenTest
 {
    @Override
    protected boolean isDebug()
    {
       return false;
    }
 
    @Override
    protected Environment newEnvironment()
    {
       return Environment.get("env-it.properties");
    }
 
    @Test
    public void testSimpleProject() throws Exception
    {
       final File projectDir = getResource("simple-project");
 
       final int error = build(projectDir, "-e", "-B", "compile");
       assertThat(error, is(0));
 
       Report bootstrapperReport = new Report(new File(projectDir, TestBootstrapper.class.getName() + ".txt"));
       List<String> lines = bootstrapperReport.readLines();
       assertThat(lines.size(), is(4));
 
       Iterator<String> it = lines.iterator();
       assertThat(it.next(), equalTo("getModuleDescriptors"));
       assertThat(it.next(), equalTo("pom.xml"));
       assertThat(it.next(), equalTo("beforeBootstrapProjects"));
       assertThat(it.next(), equalTo("afterWrapperProjectsInitialized"));
 

       Report participantReport = new Report(new File(projectDir, TestBootstrapParticipant.class.getName() + ".txt"));
       lines = participantReport.readLines();
       assertThat(lines.size(), is(2));
 
       it = lines.iterator();
       assertThat(it.next(), equalTo("beforeBuild,org.sourcepit.it,simple-project"));
       assertThat(it.next(), equalTo("afterBuild,org.sourcepit.it,simple-project"));
    }
 
    @Test
    public void testReactorProject() throws Exception
    {
       final File projectDir = getResource("reactor-project");
 
       final int error = build(projectDir, "-e", "-B", "compile");
       assertThat(error, is(0));
 
       Report bootstrapperReport = new Report(new File(projectDir, TestBootstrapper.class.getName() + ".txt"));
       List<String> lines = bootstrapperReport.readLines();
       assertThat(lines.size(), is(4));
 
       Iterator<String> it = lines.iterator();
       assertThat(it.next(), equalTo("getModuleDescriptors"));
      assertThat(it.next(), equalTo("pom.xml,module-project-a/pom.xml,module-project-b/pom.xml"));
       assertThat(it.next(), equalTo("beforeBootstrapProjects"));
       assertThat(it.next(), equalTo("afterWrapperProjectsInitialized"));
 
 
       Report participantReport = new Report(new File(projectDir, TestBootstrapParticipant.class.getName() + ".txt"));
       lines = participantReport.readLines();
       assertThat(lines.size(), is(4));
 
       it = lines.iterator();
       assertThat(it.next(), equalTo("beforeBuild,org.sourcepit.it,module-project-b"));
       assertThat(it.next(), equalTo("beforeBuild,org.sourcepit.it,module-project-a"));
       assertThat(it.next(), equalTo("afterBuild,org.sourcepit.it,module-project-b"));
       assertThat(it.next(), equalTo("afterBuild,org.sourcepit.it,module-project-a"));
    }
 }
