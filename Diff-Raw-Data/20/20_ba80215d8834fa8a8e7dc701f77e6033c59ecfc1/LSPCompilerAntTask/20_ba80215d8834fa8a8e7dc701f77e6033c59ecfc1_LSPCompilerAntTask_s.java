 /*
  * Copyright (c) 2003, Mikael Stldal
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * 3. Neither the name of the author nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
  * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  *
  * Note: This is known as "the modified BSD license". It's an approved
  * Open Source and Free Software license, see
  * http://www.opensource.org/licenses/
  * and
  * http://www.gnu.org/philosophy/license-list.html
  */
 
 package nu.staldal.lsp;
 
 import java.io.*;
 import java.util.Properties;
 
 import org.xml.sax.SAXException;
 import org.apache.tools.ant.*;
 import org.apache.tools.ant.util.*;
 import org.apache.tools.ant.types.FileSet;
  
 
 /**
  * Apache Ant interface to LSPCompiler.
  *
  * @see LSPCompiler
  */
 public class LSPCompilerAntTask extends Task
 {
 	private LSPCompilerHelper compiler;
 	
 	private File sourcepath;
 	private File destdir;	
 	private FileSet fileset;
 	private boolean force;
 
 	
 	public LSPCompilerAntTask()
 	{
 		compiler = new LSPCompilerHelper();
 		compiler.err = new PrintWriter(System.out, true);
 	}
 
 
 	public void init()
     	throws BuildException
 	{
 		fileset = null;
 		sourcepath = null;		
 		destdir = null;
 		force = false;
 	}
 
 	
 	// Attribute setter methods
 	
	public void setVerbose(boolean verbose)
	{
		compiler.verbose = verbose;
	}

	public void setFirce(boolean force)
 	{
 		this.force = force;
 	}
 	
 	public void setSourcepath(File sourcepath)
 	{
 		this.sourcepath = sourcepath;
 	}
 		
 	public void setDestdir(File destdir)
 	{
 		this.destdir = destdir;
 	}
 
 	
 	// Handle nested elements
 	
 	public void addConfiguredFileset(FileSet fileset)
 	{
 		this.fileset = fileset;	
 	}
 	
 		
 	public void execute() throws BuildException
 	{
 		if (fileset == null)
 			throw new BuildException("Must have a nested <fileset> element");
 
 		if (destdir == null)
 			throw new BuildException("Must have a destdir attribute");
 
 		compiler.targetDir = destdir;
 		
 		if (sourcepath == null) sourcepath = project.getBaseDir();
 		compiler.sourceDir = sourcepath;
 
 		DirectoryScanner ds = fileset.getDirectoryScanner(project);
 		File fromDir = fileset.getDir(project);
 		compiler.startDir = fromDir;
 
 		String[] srcFiles = ds.getIncludedFiles();
 		
 		for (int i = 0; i<srcFiles.length; i++)
 		{
 			File srcFile = new File(fromDir, srcFiles[i]);
 			File destFile = 
 				new File(destdir, LSPCompilerHelper.targetFilename(srcFiles[i]));
 			if (force || !destFile.isFile() 
 					|| destFile.lastModified() < srcFile.lastModified())
 			{
 				if (!compiler.doCompile(srcFiles[i]))
 					throw new BuildException("LSP compilation failed");
 			}
 		}
 	}
 
 }
 
