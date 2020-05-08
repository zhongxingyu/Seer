 // See the COPYRIGHT file for copyright and license information
 package org.znerd.logdoc.ant.tasks;
 
 import org.apache.tools.ant.BuildException;
 
 import org.znerd.logdoc.LogDef;
 import org.znerd.logdoc.gen.DocGenerator;
 
 /**
  * An Apache Ant task for generating web pages from Logdoc definitions.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public final class LogdocDocTask extends AbstractLogdocTask {
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>LogdocDocTask</code> object.
     */
    public LogdocDocTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    @Override
    protected void executeImpl(LogDef def) throws Exception {
       DocGenerator generator = new DocGenerator(def, _destDir);
      generator.generateDoc();
    }
 }
