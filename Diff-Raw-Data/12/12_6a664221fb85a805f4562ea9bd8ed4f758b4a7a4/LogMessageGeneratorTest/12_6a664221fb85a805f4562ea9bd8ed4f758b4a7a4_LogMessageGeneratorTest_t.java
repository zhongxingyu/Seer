 /*
  * $Id: LogMessageGeneratorTest.java 1011 2008-06-16 17:57:36Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons.taskdefs;
 
 import java.io.File;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Location;
 import org.apache.tools.ant.Project;
 import org.jcoderz.commons.TestCase;
 
 
 /**
  * JUnit test for the Ant task
  * {@link org.jcoderz.commons.taskdefs.LogMessageGenerator}.
  *
  * @author Michael Griffel
  */
 public class LogMessageGeneratorTest
       extends TestCase
 {
    private LogMessageGenerator mGenerator;
    private File mDestDir;
 
    /** {@inheritDoc} */
    protected void setUp ()
          throws Exception
    {
       super.setUp();
       mDestDir =  mkdir("build/test");
       final File in = new File(getBaseDir(), "src/test/fawkez/log-message.info.xml");
       final File out = new File(mDestDir, "log-message-info.out");
       final LogMessageGenerator g = new LogMessageGenerator();
       g.setApplication("FawkeZ");
       g.setDestdir(mDestDir);
       g.setForce(true);
       g.setIn(in);
       g.setOut(out);
       g.setTaskName("test-message-generator");
       g.setFailonerror(true);
       g.setLocation(new Location("location"));
 
       final Project project = new Project();
       project.setBaseDir(getBaseDir());
       project.setName("JUnit test");
       g.setProject(project);
       mGenerator = g;
    }
 /*
    public void testExecute ()
    {
       mGenerator.execute();
 
       final File testFile = new File(mDestDir,
            "org/jcoderz/commons/TssLogMessage.java");
       assertTrue("Generated Java File " + testFile + " exists",
             testFile.exists());
    }
 
    public void testExecuteBadInFile ()
    {
       mGenerator.setIn(null);
       executeAndExpectBuildException("missing input file.");
       mGenerator.setIn(new File("foo"));
       executeAndExpectBuildException("non existing input file.");
       mGenerator.setIn(new File(getBaseDir(), "build.xml"));
       executeAndExpectBuildException("bogus input file.");
    }
 
    public void testExecuteBadDestDir ()
    {
       mGenerator.setDestdir(null);
       executeAndExpectBuildException("missing destination directory.");
    }
 
    public void testExecuteBadOutFile ()
    {
       mGenerator.setOut(null);
       executeAndExpectBuildException("missing out file.");
    }
 
    public void testExecuteBadApplicationName ()
    {
       mGenerator.setApplication(null);
       executeAndExpectBuildException("missing out file.");
       mGenerator.setApplication("bogus");
       executeAndExpectBuildException("non-existing application name.");
    }
 
    public void testExecuteBadXslFile ()
    {
       mGenerator.setXsl("bogus.xsl"); // -> use default stylesheet 
    }
 */
 
    public void testXyz ()
    {
 	   
    }
    private void executeAndExpectBuildException (String msg)
    {
       try
       {
          mGenerator.execute();
          fail("Expected BuildException for " + msg);
       }
       catch (BuildException expected)
       {
          // expected
           System.err.println("" + expected);
           expected.printStackTrace();
       }
    }
 
    static File mkdir (String relativePath)
    {
       final File dir = new File(getBaseDir(), relativePath);
       if (!dir.exists())
       {
          if (!dir.mkdirs())
          {
             fail("Cannot create destination directory " + dir);
          }
       }
       return dir;
    }
 
 }
