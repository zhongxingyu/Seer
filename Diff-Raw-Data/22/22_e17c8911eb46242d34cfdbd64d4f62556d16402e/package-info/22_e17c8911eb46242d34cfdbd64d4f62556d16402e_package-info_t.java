 //
 // Copyright 2009 Jean-Francois Richard
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 //
 /**
  *
  * Code to make WAR files directly executable.
  *
  * <p>
  * This package contains code that will launch a WAR file in an embedded
  * servlet container.  This is useful for quick deployment of WAR files without
  * requiring the installation of a full-blown servlet container
  * (Tomcat, Glassfish, etc.), while still being able to use a container for
  * deployement.  JNLP deployment should also be possible (though untested for
  * now).
  *
  * <p>
 * The current implementation uses Winstone
  * <a href="http://winstone.sf.net/">http://winstone.sf.net</a>
  * as the servlet container.  It is of course possible to use a similar
  * scheme with Jetty or other servlet containers, but we believe Winstone,
  * with its very small footprint and lack of external dependencies, is a good
  * candidate to serve as an container in this context.  It doesn't grow the
  * WAR file too big and still provide most of the functionality one expects from
  * a servlet container.
  *
  * <p>
  * This code has been inspired by Hudson
  * (<a href="http://hudson-ci.org">http://hudson-ci.org</a>) and
  * Google Gerrit
  * (<a href="http://code.google.com/p/gerrit/">http://code.google.com/p/gerrit/</a>).
  *
  * <p>
  * To use, one must do some Maven trickery on the project that is meant to be
  * built as an executable WAR.
  *
  * <p>
  * A sample <code>pom.xml</code> magic section is presented next.
  * If you wish to manually set the project final name in your <code>pom.xml</code>,
  * you need to change the copy task near the bottom to take that into account).
  *
  * <pre>
  * &lt;project ...&gt;
  *   &lt;properties&gt;
  *       &lt;winstoneFlavour&gt;winstone-lite&lt;/winstoneFlavour&gt;
  *       &lt;winstoneVersion&gt;0.9.10&lt;/winstoneVersion&gt;
  *   &lt;/properties&gt;
  *   &lt;dependencies&gt;
  *       &lt;dependency&gt;
  *           &lt;groupId&gt;org.inuua&lt;/groupId&gt;
  *           &lt;artifactId&gt;executable-war&lt;/artifactId&gt;
  *           &lt;version&gt;1.0-SNAPSHOT&lt;/version&gt;
  *           &lt;scope&gt;provided&lt;/scope&gt;
  *       &lt;/dependency&gt;
  *        &lt;dependency&gt;
  *           &lt;groupId&gt;net.sourceforge.winstone&lt;/groupId&gt;
  *           &lt;artifactId&gt;${winstoneFlavour}&lt;/artifactId&gt;
  *           &lt;version&gt;${winstoneVersion}&lt;/version&gt;
  *       &lt;/dependency&gt;
  *   &lt;/dependencies&gt;
  *   &lt;build&gt;
  *       &lt;plugins&gt;
  *           &lt;plugin&gt;
  *               &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
  *               &lt;artifactId&gt;maven-war-plugin&lt;/artifactId&gt;
  *               &lt;configuration&gt;
  *                   &lt;archive&gt;
  *                       &lt;manifest&gt;
  *                           &lt;mainClass&gt;org.inuua.executable_war.App&lt;/mainClass&gt;
  *                       &lt;/manifest&gt;
  *                       &lt;manifestEntries&gt;
  *                           &lt;X-Winstone-Jar&gt;${winstoneFlavour}-${winstoneVersion}.jar&lt;/X-Winstone-Jar&gt;
  *                           &lt;!-- Set some extra parameters to pass Winstone --&gt;
  *                           &lt;!-- DO NOT SET webroot AND warfile --&gt;
  *                           &lt;X-Extra-Winstone-Parameters&gt;--useJNDI=false&lt;/X-Extra-Winstone-Parameters&gt;
  *                       &lt;/manifestEntries&gt;
  *                   &lt;/archive&gt;
  *               &lt;/configuration&gt;
  *           &lt;/plugin&gt;
  *           &lt;plugin&gt;
  *               &lt;groupId&gt;org.apache.maven.plugins&lt;/groupId&gt;
  *               &lt;artifactId&gt;maven-dependency-plugin&lt;/artifactId&gt;
  *               &lt;executions&gt;
  *                   &lt;execution&gt;
  *                       &lt;id&gt;make-executable&lt;/id&gt;
  *                       &lt;phase&gt;generate-resources&lt;/phase&gt;
  *                       &lt;goals&gt;
  *                           &lt;goal&gt;unpack&lt;/goal&gt;
  *                       &lt;/goals&gt;
  *                       &lt;configuration&gt;
  *                           &lt;artifactItems&gt;
  *                               &lt;artifactItem&gt;
  *                                   &lt;groupId&gt;org.inuua&lt;/groupId&gt;
  *                                   &lt;artifactId&gt;executable-war&lt;/artifactId&gt;
  *                                   &lt;overWrite&gt;true&lt;/overWrite&gt;
  *                                   &lt;outputDirectory&gt;${project.build.directory}/executable-war&lt;/outputDirectory&gt;
  *                                   &lt;includes&gt;*&#42;/*.class&lt;/includes&gt;
  *                               &lt;/artifactItem&gt;
  *                           &lt;/artifactItems&gt;
  *                       &lt;/configuration&gt;
  *                   &lt;/execution&gt;
  *               &lt;/executions&gt;
  *           &lt;/plugin&gt;
  *           &lt;plugin&gt;
  *               &lt;artifactId&gt;maven-antrun-plugin&lt;/artifactId&gt;
  *               &lt;executions&gt;
  *                   &lt;execution&gt;
  *                       &lt;id&gt;fix-output&lt;/id&gt;
  *                       &lt;phase&gt;process-classes&lt;/phase&gt;
  *                       &lt;configuration&gt;
  *                           &lt;tasks&gt;
  *                               &lt;!-- Change this if you set the final name yourself: --&gt;
  *                               &lt;property name="d" location="${basedir}/target/${project.artifactId}-${project.version}"/&gt;
  *                               &lt;copy todir="${d}"&gt;
  *                                   &lt;fileset dir="${basedir}/target/executable-war" includes="*&#42;/*"/&gt;
  *                               &lt;/copy&gt;
  *                           &lt;/tasks&gt;
  *                       &lt;/configuration&gt;
  *                       &lt;goals&gt;
  *                           &lt;goal&gt;run&lt;/goal&gt;
  *                       &lt;/goals&gt;
  *                   &lt;/execution&gt;
  *               &lt;/executions&gt;
  *           &lt;/plugin&gt;
  *       &lt;/plugins&gt;
  *   &lt;/build&gt;
  * &lt;/project&gt;
  * </pre>
  *
  */
 package org.inuua.executable_war;
