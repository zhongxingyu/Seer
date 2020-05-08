 package com.thoughtworks.qdox.xml;
 
 import com.thoughtworks.qdox.ant.AbstractQdoxTask;
 import com.thoughtworks.qdox.model.JavaSource;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.io.Writer;
 import org.apache.tools.ant.BuildException;
 
 public class JavaDocXmlTask extends AbstractQdoxTask {
 
 	//---( Constants )---
 
 	static final String XML_VERSION_PREAMBLE = 
 		"<?xml version=\"1.0\"?>";
 	static final String QD0X_DTD_PREAMBLE = 
 		"<!DOCTYPE qdox PUBLIC"
 		+ " \"-//codehaus.org//QDox 1.0//EN\""
 		+ " \"http://qdox.codehaus.org/dtd/qdox-1.0.dtd\">";
 	
 	//---( Arguments )---
 
 	private File dest;
 	private boolean writeDtd;
 
 	/**
 	 * Set output file name
 	 */
 	public void setDest(File dest) {
 		this.dest = dest;
 	}
 
 	/**
 	 * Set the "writeDtd" attribute.  If true, the QDox DTD will be
	 * included in the output.  Defaults to true.
 	 */
 	public void setWriteDtd(boolean writeDtd) {
 		this.writeDtd = writeDtd;
 	}
 
 	//---( Execution )---
 
 	protected void validateAttributes() {
 		super.validateAttributes();
 		if (dest == null) {
 			throw new BuildException("no \"dest\" specified");
 		}
 	}
 
 	protected void processSources(JavaSource[] sources) {
 		try {
 			Writer out = new FileWriter(dest);
 			writePreamble(out);
 			JavaDocXmlGenerator xmlGenerator =
 				new JavaDocXmlGenerator(new TextXmlHandler(out, "  "));
 			xmlGenerator.write(sources);
 		} catch (IOException e) {
 			throw new BuildException(e);
 		}
 	}
 
 	protected void writePreamble(Writer writer) throws IOException {
 		PrintWriter out = new PrintWriter(writer);
 		out.println(XML_VERSION_PREAMBLE);
 		if (writeDtd) {
 			out.println(QD0X_DTD_PREAMBLE);
 		}
 		out.flush();
 	}
 	
 }
