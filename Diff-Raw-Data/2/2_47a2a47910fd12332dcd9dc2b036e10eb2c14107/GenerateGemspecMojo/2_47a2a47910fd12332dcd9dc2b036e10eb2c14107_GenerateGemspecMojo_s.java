 /**
  * Copyright 2009. Joe Khoobyar.  All Rights Reserved.
  */
 package com.ankhcraft.maven.plugin.gem;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Developer;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.codehaus.plexus.util.DirectoryScanner;
 
 /**
  * Generates a gemspec from the project model.
  * 
  * @author Joe Khoobyar
  * 
  * @goal gemspec
  * @requiresDependencyResolution runtime
  */
 public class GenerateGemspecMojo
 	extends AbstractRubyGemMojo
 {
 	/**
 	 * GEM platform.
 	 * 
 	 * @parameter alias="gemPlatform" expression="${gem.platform}" default-value="ruby"
 	 * @required
 	 */
 	private String platform;
 
 	/**
 	 * GEM authors.
 	 * 
 	 * @parameter alias="gemAuthors" expression="${gem.authors}"
 	 */
 	private String authors[];
 
 	/**
 	 * GEM email address.
 	 * 
 	 * @parameter alias="gemEmail" expression="${gem.email}"
 	 */
 	private String email;
 
 	/**
 	 * Extra RDoc files.
 	 * 
 	 * @parameter
 	 */
 	private String extraRdocFiles[];
 
 	/**
 	 * Rubyforge Project Identifier (UNIX name)
 	 * 
 	 * @parameter expression="${gem.rubyforgeProject}
 	 */
 	private String rubyforgeProject;
 
 	/**
 	 * RubyGems version
 	 * 
 	 * @parameter expression="${gem.rubygemsVersion}" default-value="1.3.4"
 	 * @required
 	 */
 	private String rubygemsVersion;
 
 	/**
 	 * Required RubyGems version
 	 * 
 	 * @parameter expression="${gem.requiredRubygemsVersion}"
 	 */
 	private String requiredRubygemsVersion;
 
 	/**
 	 * Required ruby version
 	 * 
 	 * @parameter expression="${gem.requiredRubyVersion}"
 	 */
 	private String requiredRubyVersion;
 
 	/** Generates the gemspec. */
 	public void execute () throws MojoExecutionException {
 		StringBuilder sb = new StringBuilder ("--- !ruby/object:Gem::Specification\n");
 		sb.append ("name: ").append (baseName).append ("\n");
 		sb.append ("version: !ruby/object:Gem::Version\n  version: ").append (version).append ("\n");
 		sb.append ("platform: ").append (platform).append ("\n");
 
 		String authors[] = getAuthors ();
 		writeStringArray (sb, null, "authors", authors);
 		String binDir = getBinDir ();
 		if (binDir != null)
			sb.append ("bindir: bin\n");
 		sb.append ("cert_chain: []\n\n");
 		String date = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss Z").format (new Date ());
 		int n = date.length () - 2;
 		sb.append ("date: ").append (date.substring (0, n)).append (":").append (date.substring (n)).append ("\n");
 		sb.append ("default_executable: ").append (defaultExecutable == null ? "" : defaultExecutable).append ("\n");
 		sb.append ("dependencies: ");
 
 		List<Artifact> deps = getGemDependencies ();
 		if (deps != null && deps.size () > 0) {
 			sb.append ("\n");
 			for (Artifact dep : deps) {
 				sb.append ("- !ruby/object:Gem::Dependency\n");
 				sb.append ("  name:").append (dep.getArtifactId ()).append ("\n");
 				sb.append ("  type: :runtime\n");
 				sb.append ("  version_requirement:\n");
 				sb.append ("  version_requirements: !ruby/object:Gem::Requirement\n");
 				sb.append ("    requirements:\n");
 				sb.append ("    - - \">=\"\n");
 				sb.append ("    - - !ruby/object:Gem::Version\n");
 				sb.append ("    - - version: \"").append (dep.getVersion ()).append ("\n");
 				sb.append ("    version:\n");
 				dep.getVersion ();
 			}
 		} else
 			sb.append ("[]\n\n");
 		String desc = getProject ().getDescription ();
 		if (desc != null && (desc = desc.trim ()).length () > 0)
 			sb.append ("description: |\n  ").append (desc).append ("\n\n");
 		sb.append ("email: ").append (getEmail ()).append ("\n");
 		writeStringArray (sb, null, "executables", getExecutables ());
 		writeStringArray (sb, null, "extensions", getExtensions ());
 
 		writeStringArray (sb, null, "extra_rdoc_files", extraRdocFiles);
 		writeStringList (sb, null, "files", getGemFiles ());
 		sb.append ("has_rdoc: true\n");
 		sb.append ("licenses: []\n");
 		sb.append ("rdoc_options:\n- --charset=UTF-8\nrequire_paths:\n- lib\n");
 		if (rubyforgeProject != null)
 			sb.append ("rubyforge_project: ").append (rubyforgeProject).append ("\n");
 		sb.append ("required_rubygems_version: ");
 		sb.append (requiredRubygemsVersion == null ? "" : requiredRubygemsVersion).append ("\n");
 		sb.append ("required_ruby_version: ");
 		sb.append (requiredRubyVersion == null ? "" : requiredRubyVersion).append ("\n");
 		sb.append ("rubygems_version: ").append (rubygemsVersion).append ("\n");
 		sb.append ("signing_key:\n");
 		sb.append ("specification_version: 3\n");
 		sb.append ("summary: ").append (getProject ().getName ()).append ("\n");
 		writeStringList (sb, null, "test_files", getGemTestFiles ());
 
 		File gemspec = getGemFile (outputDirectory, finalName, null, "gemspec");
 		if (gemspec.exists () && !gemspec.delete ())
 			throw new MojoExecutionException ("Failed to delete previous gemspec: " + gemspec);
 		BufferedWriter wr = null;
 		try {
 			gemspec.getParentFile ().mkdir ();
 			gemspec.createNewFile ();
 			wr = new BufferedWriter (new FileWriter (gemspec), 4096);
 			wr.write (sb.toString ());
 			wr.flush ();
 		} catch (IOException e) {
 			throw new MojoExecutionException ("IO error while writing gemspec (" + gemspec.getAbsolutePath () + ")", e);
 		} finally {
 			try {
 				if (wr != null)
 					wr.close ();
 			} catch (IOException ignore) {}
 		}
 	}
 
 	private void writeStringList (StringBuilder sb, String indent, String key, List<String> list) {
 		if (indent == null)
 			indent = "";
 		sb.append (indent).append (key).append (": ");
 		if (list != null && !list.isEmpty ()) {
 			sb.append ("\n");
 			for (String item : list)
 				sb.append (indent).append ("- ").append (item).append ("\n");
 		} else
 			sb.append ("[]\n\n");
 	}
 
 	private void writeStringArray (StringBuilder sb, String indent, String key, String list[]) {
 		if (indent == null)
 			indent = "";
 		sb.append (indent).append (key).append (": ");
 		if (list != null && list.length > 0) {
 			sb.append ("\n");
 			for (String item : list)
 				sb.append (indent).append ("- ").append (item).append ("\n");
 		} else
 			sb.append ("[]\n\n");
 	}
 
 	private String[] sourceLibFiles;
 	private List<String> targetLibFiles;
 
 	private List<String> getGemLibFiles () throws MojoExecutionException {
 		if (targetLibFiles == null) {
 			targetLibFiles = new LinkedList<String> ();
 			if (sourceLibFiles == null)
 				sourceLibFiles = scanDirectory (libSourceDirectory, libIncludes, libExcludes);
 			for (String file : sourceLibFiles)
 				targetLibFiles.add ("lib/" + file.replace ('\\', '/'));
 		}
 		return targetLibFiles;
 	}
 
 	private String[] sourceBinFiles;
 	private List<String> targetBinFiles;
 
 	private List<String> getGemBinFiles () throws MojoExecutionException {
 		if (targetBinFiles == null) {
 			targetBinFiles = new LinkedList<String> ();
 			if (sourceBinFiles == null)
 				sourceBinFiles = scanDirectory (binSourceDirectory, binIncludes, binExcludes);
 			for (String file : sourceBinFiles)
 				targetBinFiles.add ("bin/" + file.replace ('\\', '/'));
 		}
 		return targetBinFiles;
 	}
 
 	private String[] sourceTestFiles;
 	private List<String> targetTestFiles;
 
 	private List<String> getGemTestFiles () throws MojoExecutionException {
 		if (targetTestFiles == null) {
 			targetTestFiles = new LinkedList<String> ();
 			if (sourceTestFiles == null)
 				sourceTestFiles = scanDirectory (testSourceDirectory, testIncludes, testExcludes);
 			for (String file : sourceTestFiles)
 				targetTestFiles.add ("test/" + file.replace ('\\', '/'));
 		}
 		return targetTestFiles;
 	}
 
 	private List<String> getGemFiles () throws MojoExecutionException {
 		List<String> files = new LinkedList<String> ();
 		if (extraBaseDirectories != null)
 			for (String baseDir : extraBaseDirectories)
 				for (String file : scanDirectory (baseDir, extraIncludes, extraExcludes))
 					files.add (file.replace ('\\', '/'));
 		files.addAll (getGemLibFiles ());
 		files.addAll (getGemBinFiles ());
 		files.addAll (getGemTestFiles ());
 		return files;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String[] getAuthors () {
 		if (authors != null && authors.length > 0)
 			return authors;
 
 		List<Developer> devs = getProject ().getDevelopers ();
 		authors = new String[devs == null ? 0 : devs.size ()];
 		int i = 0;
 		for (Developer dev : devs)
 			authors[i++] = dev.getName ();
 		return authors;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getEmail () {
 		String email = this.email;
 
 		if (email != null && (email = email.trim ()).length () > 0)
 			return email;
 
 		for (Developer dev : (List<Developer>) getProject ().getDevelopers ())
 			if ((email = dev.getEmail ()) != null && (email = email.trim ()).length () > 0)
 				return email;
 		return "";
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Artifact> getGemDependencies () {
 		List<Artifact> deps = new LinkedList<Artifact> ();
 		for (Artifact dep : (Set<Artifact>) getProject ().getDependencyArtifacts ())
 			if ("rubygem".equals (dep.getType ()) && "runtime".equals (dep.getScope ()))
 				deps.add (dep);
 		return deps;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String getBinDir () throws MojoExecutionException {
 		if (sourceBinFiles == null)
 			getGemBinFiles ();
 		return (sourceBinFiles != null && sourceBinFiles.length > 0) ? "bin" : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String[] getExecutables () throws MojoExecutionException {
 		if (sourceBinFiles == null)
 			getGemBinFiles ();
 		return sourceBinFiles;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String[] getExtensions () {
 		return null;
 	}
 }
