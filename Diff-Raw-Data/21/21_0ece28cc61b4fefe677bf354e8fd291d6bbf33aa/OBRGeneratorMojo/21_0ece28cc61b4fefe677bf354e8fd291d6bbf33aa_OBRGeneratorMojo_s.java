 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.apamMavenPlugin;
 
 import java.io.File;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.felix.ipojo.metadata.Element;
 import org.apache.felix.ipojo.parser.ManifestMetadataParser;
 import org.apache.felix.ipojo.parser.ParseException;
 import org.apache.felix.ipojo.plugin.ManipulatorMojo;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.declarations.ComponentDeclaration;
 import fr.imag.adele.apam.util.CoreMetadataParser;
 import fr.imag.adele.apam.util.CoreParser;
 import fr.imag.adele.apam.util.CoreParser.ErrorHandler;
//import org.apache.felix.ipojo.plugin.MavenReporter;
 
 /**
  * Packages an OSGi jar "iPOJO bundle" as an "APAM bundle".
  * 
  * @version $Rev$, $Date$
  * @extendsPlugin maven-ipojo-plugin
  * @goal apam-bundle
  * @extendsGoal ipojo-bundle
  * @phase package
  * @requiresrelationResolution runtime
  * @description manipulate an OSGi bundle jar to include the obr.xml file and
  *              build APAM bundle
  * 
  * @author ApAM Team
  */
 public class OBRGeneratorMojo extends ManipulatorMojo implements ErrorHandler {
 
 
 
 
     public static Map<String, VersionRange> versionRange       = new HashMap<String, VersionRange>();
 
 	public static String                    thisBundleVersion;
 
 	Logger                                  logger             = LoggerFactory.getLogger(OBRGeneratorMojo.class);
 
     /**
      * The Maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
 
     /**
      * The project groupID
      * @parameter default-value="${project.groupId}"
      * @required
      * @readonly
      */
 	public static String currentProjectGroupId ;
 
     /**
      * The project artifactID
      * @parameter default-value="${project.artifactId}"
      * @required
      * @readonly
      */
     public static String currentProjectArtifactId ;
 
     /**
      * The project version
      * @parameter default-value="${project.version}"
      * @required
      * @readonly
      */
     public static String currentProjectVersion ;
 
     /**
      * The project file
      * @parameter default-value="${project.artifact}"
      * @required
      * @readonly
      */
     private Artifact artifact;
 
     /**
      * @parameter default-value="${basedir}
      */
     private File baseDirectory;
 
 	private boolean parsingFailed =false;
 
 
     /**
      * Contains all classes and resources
      */
     public static ClasspathDescriptor classpathDescriptor;
 
     /**
 	 * Execute method : this method launches the OBR generation.
 	 * 
 	 * @throws MojoExecutionException
 	 *             : an exception occurs during the OBR generation..
 	 *
 	 */
 	@Override
 	public void execute() throws MojoExecutionException {
 
 		super.execute();
 
 
 		thisBundleVersion = currentProjectVersion.replace('-', '.');
         classpathDescriptor = new ClasspathDescriptor();
 		try {
 
 			getLog().info("Start bundle header manipulation");
 
 			//The jar to compile
 			List<ComponentDeclaration> components = getComponentFromJar(artifact.getFile());
 			
 			if(components.isEmpty()){
 				throw new InvalidApamMetadataException();
 			}
 			
             classpathDescriptor.add(artifact.getFile());
 			/*
 			 * Get the definition of the components needed to compile
 			 */
 			List <ComponentDeclaration> dependencies = new ArrayList <ComponentDeclaration> ();
 
             /*
              *Clear statics
              */
             versionRange.clear();
 
             /*
              * loop  dependencies
              */
 
 			for (Object artifact : project.getArtifacts()) {
 				if (artifact instanceof Artifact) {
 
 					Artifact relation = (Artifact) artifact;
 
 					VersionRange range = relation.getVersionRange();
 					
 					OBRGeneratorMojo.versionRange.put(relation.getArtifactId(),
 							range);
 					
 					List<ComponentDeclaration> subcomponents=getComponentFromJar(((Artifact) artifact).getFile());
 					
 					if(subcomponents!=null) dependencies.addAll(subcomponents);
 					
 					classpathDescriptor.add(relation.getFile());
 				}
 			}
 
             ApamRepoBuilder arb = new ApamRepoBuilder(components, dependencies);
 			StringBuffer obrContent = arb.writeOBRFile();
 			if (CheckObr.getFailedChecking()) {
 				throw new MojoExecutionException("Metadata Apam compilation failed.");
 			}
 			if (parsingFailed) {
 				error(Severity.ERROR, "Invalid xml Apam Metadata syntax");
 			}
 
 			OutputStream obr;
 			String obrFileStr = baseDirectory.getAbsolutePath()
 			+ File.separator + "src"
 			+ File.separator + "main"
 			+ File.separator + "resources"
 			+ File.separator + "obr.xml";
 			File obrFile = new File(obrFileStr);
 
 			// maven ?? copies first in target/classes before to look in src/resources
 			// and copies src/resources/obr.xml to target/classes *after* obr modification
 			// Thus we delete first target/classes/obr.xml to be sure the newly generated obr.xml file will be used
 
 			String oldObrFileStr = baseDirectory.getAbsolutePath()
 			+ File.separator + "target"
 			+ File.separator + "classes"
 			+ File.separator + "obr.xml";
 			File oldObrFile = new File(oldObrFileStr);
 			if (oldObrFile.exists()) {
 				oldObrFile.delete();
 			}
 
 			if (!obrFile.exists()) {
 				obrFile.getParentFile().mkdirs();
 			}
 			obr = new FileOutputStream(obrFile);
 			obr.write(obrContent.toString().getBytes());
 			obr.flush();
 			obr.close();
 
 		} catch (Exception e) {
 			getLog().error(e.getMessage(), e);
 			error(Severity.ERROR, e.getMessage());
 			throw new MojoExecutionException(e.getMessage());
 		}
 		
 		getLog().info(" obr.xml File generation - SUCCESS ");
 	}
 
 	private List<ComponentDeclaration> getComponentFromJar (File jar) throws InvalidApamMetadataException {
 		
 		try {
 			JarFile jarFile = new JarFile(jar);
 			Manifest manifest = jarFile.getManifest();
 			
 			if(manifest==null) return null;
 			
 			// manifest.getAttributes("").
 			Attributes iPOJOmetadata = manifest.getMainAttributes();
 			String ipojoMetadata = iPOJOmetadata.getValue("iPOJO-Components");
 
 			iPOJOmetadata = null;
 			manifest = null;
 			jarFile.close();
 			if (ipojoMetadata == null ) {
 				String message=" No Apam metadata for " + jar ;
 				getLog().error(message);
 				return Collections.emptyList();
 
 			}
 			
 			getLog().info("Parsing Apam metadata for " + jar + " - SUCCESS ");
 			Element root = ManifestMetadataParser
 			.parseHeaderMetadata(ipojoMetadata);
 			
 			CoreParser parser = new CoreMetadataParser(root);
 			List<ComponentDeclaration> ret = parser.getDeclarations(this);
 			
 			String contains = "    contains components: " ;
 			for (ComponentDeclaration comp : ret) {
 				contains += comp.getName() + " ";
 			}
 			getLog().info(contains) ;
 			return ret ;
 			
 		} catch (ParseException e) {
 			String message="Parsing manifest metadata for " + jar + " - FAILED ";
 			getLog().error(message);
 			error(Severity.ERROR, message);
 		} catch (IOException e) {
 			getLog().error(e.getMessage());
 			error(Severity.ERROR, e.getMessage());
 		}
 		
 		return Collections.emptyList(); 
 	}
 	
 	@Override
 	public void error(Severity severity, String message) {
 		logger.error("error parsing component declaration : " + message);
 		parsingFailed  = true;
 	}
 	
 
 }
