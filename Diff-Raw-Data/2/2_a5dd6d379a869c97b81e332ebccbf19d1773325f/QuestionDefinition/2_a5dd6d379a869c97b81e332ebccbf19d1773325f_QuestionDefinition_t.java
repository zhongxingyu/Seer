 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
 
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package om.devservlet;
 
 import java.io.*;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import om.*;
 import om.question.Question;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import util.misc.ClosableClassLoader;
 import util.xml.XHTML;
 import util.xml.XML;
 
 /**
  * Represents the information from a question definition file, and actions on
  * that such as building and instantiating the question.
  */
 public class QuestionDefinition
 {
 	/** Question definition file */
 	private File fThis;
 
 	/** Source tree root */
 	private File fSource;
 
 	/** Java package of question */
 	private String sPackage;
 
 	/** Additional package roots to include in jar */
 	private String[] asAdditionalPackageRoots;
 
 	/** Creation time */
 	private long lCreated=System.currentTimeMillis();
 
 	/** Jar file target */
 	private File fJar;
 
 	/** Owner */
 	private QuestionDefinitions qdOwner;
 
 	/**
 	 * Constructs definition from a file.
 	 * @param f Definition file
 	 * @throws OmException If there's anything wrong with the file
 	 */
 	QuestionDefinition(QuestionDefinitions qdOwner,File f) throws OmDeveloperException
 	{
 		this.qdOwner=qdOwner;
 		fThis=f;
 		try
 		{
 
 			// Parse XML & check root
 			Document d=XML.parse(f);
 			if(!d.getDocumentElement().getTagName().equals("questiondefinition"))
 				throw new OmFormatException(
 					"Root must be <questiondefinition> in question definition: "+f);
 
 			fSource=new File(XML.getText(d.getDocumentElement(),"sourcetree"));
 			sPackage=XML.getText(d.getDocumentElement(),"package");
 
 			asAdditionalPackageRoots=XML.getTextFromChildren(
 				d.getDocumentElement(),"includepackage");
 
 			fJar=new File(f.getParentFile(),getID()+".jar");
 		}
 		catch(IOException e) // XML parse failure probably
 		{
 			throw new OmDeveloperException("Error parsing XML or loading question definition: "+f);
 		}
 	}
 
 	/** @return Question definition ID */
 	public String getID() { return QuestionDefinitions.getNameFromFile(fThis); }
 
 	/** @return Source tree root */
 	public File getSourceFolder() { return fSource; }
 
 	/** @return Java package of question */
 	public String getPackage() { return sPackage; }
 
 	/** @return Array of additional package roots to include in jar */
 	public String[] getAdditionalPackageRoots() {  return asAdditionalPackageRoots; }
 
 	/** @return True if this should be reloaded */
 	boolean isOutdated() { return System.currentTimeMillis() - lCreated > QuestionDefinitions.CACHEMILLISECONDS; }
 
 	/**
 	 * Builds this question into the target jar file.
 	 * @param wProgress Writer that will receive progress information in HTML
 	 *   format. This code writes the starting &lt;html&gt;&lt;body&gt; tags,
 	 *   but not the closing ones which should be written after this method
 	 *   returns.
 	 * @return True if build succeeded, false if it failed
 	 */
 	boolean build(Writer wProgress) throws OmException
 	{
 		// Delete jar file if it exists
 		if(fJar.exists())
 		{
 			if(!fJar.delete()) throw new OmException("Failed to delete jar file: "+fJar);
 		}
 
 		// Create Ant build script based on template
 		File fWebapp=fThis.getParentFile().getParentFile();
 		Document dBuildScript;
 		File fAnt=new File(qdOwner.getQuestionsFolder(),"questionbuild.ant");
 		try
 		{
 			dBuildScript=XML.parse(getClass().getResourceAsStream(
 				"questionbuild.xml"));
 
 			File sourceFolder = getSourceFolder();
 			if(!sourceFolder.exists())
 				throw new OmException(
 					"Source folder '"+fSource+"' does not exist.");
 
 			// Fill in standard properties
 			Map<String,String> mReplace=new HashMap<String,String>();
 			mReplace.put("WEBAPP",fWebapp.getAbsolutePath());
 			mReplace.put("JAR",fJar.getAbsolutePath());
 			mReplace.put("SOURCE",sourceFolder.getAbsolutePath());
 			mReplace.put("QUESTIONPACKAGE",getPackage().replace('.','/'));
 			XML.replaceTokens(dBuildScript,mReplace);
 
 			// Add includes for additional package roots
 			Element[] ae=XML.getElementArray(
 				dBuildScript.getElementsByTagName("ADDPACKAGESMARKER"));
 			for(int i=0;i<ae.length;i++)
 			{
 				// Get rid of marker
 				Element eParent=(Element)ae[i].getParentNode();
 				eParent.removeChild(ae[i]);
 
 				// Add child for each extra package
 				for(int iAdditional=0;iAdditional<asAdditionalPackageRoots.length;iAdditional++)
 				{
 					Element eInclude=XML.createChild(eParent,"include");
 					eInclude.setAttribute("name",
 						asAdditionalPackageRoots[iAdditional].replace('.','/')+"/"+
 						(XML.getRequiredAttribute(ae[i],"type").equals("javac") ? "**/*.java" : ""));
 				}
 			}
 
 			// Save script out (don't use .xml or it looks like a question)
 			XML.save(dBuildScript,fAnt);
 		}
 		catch(IOException ioe)
 		{
 			throw new OmUnexpectedException(ioe);
 		}
 
 		// Run external Ant
 		try
 		{
 			Process p=Runtime.getRuntime().exec(
 				qdOwner.getAntCommand(),
 				// Environment
 				new String[] {"JAVA_HOME="+qdOwner.getJDKHome()},
 				// Working directory
 				qdOwner.getQuestionsFolder());
 
 			wProgress.write(
 				"<html>" +
 				"<head>" +
 				"<title>Building "+getID()+" ("+fJar.getAbsolutePath()+")</title>" +
 				"<style type='text/css'>" +
 				"body { font: 10px Andale Mono, Lucida Console, monospace; }" +
 				".out,.err { white-space:pre; }"+
 				".err { color:#900; }"+
 				"</style>"+
 				"</head>" +
 				"<body>");
 
 			ReadThread rtOut=new ReadThread(
 				new BufferedReader(new InputStreamReader(p.getInputStream())),
 				wProgress,"out");
 			new ReadThread(
 				new BufferedReader(new InputStreamReader(p.getErrorStream())),
 				wProgress,"err");
 
 			p.waitFor();
 
 			fAnt.delete();
 
 			return rtOut.isSuccess();
 		}
 		catch(IOException ioe)
 		{
 			throw new OmException("Error running Ant build",ioe);
 		}
 		catch(InterruptedException ie)
 		{
 			throw new OmUnexpectedException(ie);
 		}
 	}
 
 	/** @return True if the jar file has been built */
 	boolean hasJar()
 	{
 		return fJar.exists();
 	}
 
 	/** Crappy class to group the two things I need to return */
 	public class RunReturn
 	{
 		Question q;
 		Document dMeta;
 		ClosableClassLoader ccl;
 	}
 
 	/**
 	 * @return Newly-created Question object after loading jar. Question
 	 *   still needs to be initialised based on the provided metadata documnt.
 	 * @throws OmException If there's any error in loading the jar or in
 	 *   reflection etc.
 	 */
 	RunReturn run() throws OmException
 	{
 		// Return result
 		RunReturn rr=new RunReturn();
 
 		// Get new classloader
 		try
 		{
 			rr.ccl=new ClosableClassLoader(fJar,getClass().getClassLoader());
 		}
 		catch(IOException ioe)
 		{
 			throw new OmException(
 				"Failed to start question classloader for: "+fJar,ioe);
 		}
 
 		boolean bSuccess=false;
 		try
 		{
 			// Get metadata document
 			try
 			{
 				URL uXML=rr.ccl.findResource("question.xml");
 				if(uXML==null)
 					throw new OmDeveloperException("question.xml not present in: "+fJar);
 				InputStream is=uXML.openStream();
 				rr.dMeta=XML.parse(is);
 				is.close();
 			}
 			catch(IOException ioe)
 			{
 				throw new OmDeveloperException(
 					"Failed to load or parse question.xml in: "+fJar,ioe);
 			}
 
 			// Find classname
 			Element eRoot=rr.dMeta.getDocumentElement();
 			if(!eRoot.getTagName().equals("question"))
 				throw new OmDeveloperException(
 					"Expecting <question> as root of question.xml in: "+fJar);
 			if(!eRoot.hasAttribute("class"))
 				throw new OmDeveloperException(
 					"Expecting class= attribute on root of question.xml in: "+fJar);
 			String sClass=eRoot.getAttribute("class");
 
 			// Load class
 			try
 			{
				Class<?> c=rr.ccl.loadClass(sClass);
 				rr.q=(Question)c.newInstance();
 			}
 			catch(ClassNotFoundException cnfe)
 			{
 				throw new OmDeveloperException("Failed to find "+sClass+" in: "+fJar);
 			}
 			catch(ClassCastException cce)
 			{
 				throw new OmDeveloperException(
 					"Class "+sClass+" doesn't implement Question, in: "+fJar);
 			}
 			catch(InstantiationException ie)
 			{
 				throw new OmException(
 					"Error instantiating "+sClass+" in: "+fJar,ie);
 			}
 			catch(IllegalAccessException iae)
 			{
 				throw new OmException(
 					"Error instantiating "+sClass+" (check it's public) in: "+fJar,iae);
 			}
 			catch(Throwable t)
 			{
 				throw new OmException(
 					"Error instantiating "+sClass+" in: "+fJar,t);
 			}
 
 			bSuccess=true;
 			return rr;
 		}
 		finally
 		{
 			if(!bSuccess) rr.ccl.close();
 		}
 	}
 
 	/** Class that passes data from reader to writer one line at a time in XHTML */
 	private static class ReadThread
 	{
 		private boolean bSuccess=false;
 
 		ReadThread(final BufferedReader br,final Writer w,final String sClass)
 		{
 			Thread t=new Thread(new Runnable()
 			{
 				public void run()
 				{
 					try
 					{
 						while(true)
 						{
 							String sLine=br.readLine();
 							if(sLine==null) break;
 							if(sLine.toUpperCase().equals("BUILD SUCCESSFUL")) bSuccess=true;
 							if(sLine.equals(""))
 								w.write("<div class='"+sClass+"'>\n</div>\n");
 							else
 								w.write("<div class='"+sClass+"'>"+XHTML.escape(sLine,XHTML.ESCAPE_TEXT)+"</div>\n");
 							w.flush();
 						}
 					}
 					catch(IOException ioe)
 					{
 					}
 				}
 			});
 			t.start();
 		}
 
 		boolean isSuccess() { return bSuccess; }
 	}
 }
