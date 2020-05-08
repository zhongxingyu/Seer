 /** 
  * Author: Martin van Velsen <vvelsen@cs.cmu.edu>
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as 
  *  published by the Free Software Foundation, either version 3 of the 
  *  License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package edu.cmu.cs.in.hoop.project;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Element;
 
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopConnection;
 import edu.cmu.cs.in.search.HoopDataSet;
 
 /** 
  * 
  */
 public class HoopProject extends HoopProjectFile
 {
 	private ArrayList <HoopFile> files=null;
 	private ArrayList <HoopProjectFile> fileTemplates=null;
 	
 	/**
 	 * 
 	 */
 	public HoopProject ()
 	{
 		setClassName ("HoopProject");
 		debug ("HoopProject ()");
 		
 		files=new ArrayList<HoopFile> ();
 		fileTemplates=new ArrayList<HoopProjectFile> ();
 		
 		addFileTemplate (new HoopGraphFile ());
 		addFileTemplate (new HoopStopWords ());
 		addFileTemplate (new HoopVocabulary ());
 		addFileTemplate (new HoopWrapperFile ());			
 	}
 	/**
 	 * 
 	 */
 	public Boolean isEmpty ()
 	{
 		debug ("isEmpty ()");
 		
 		HoopGraphFile graphFile=(HoopGraphFile) getFileByClass ("HoopGraphFile");
 		
 		if (graphFile!=null)
 		{
 			if (graphFile.getHoops().size()>1)
 			{
 				return (false);
 			}
 		}	
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	public void addFileTemplate (HoopProjectFile aTemplate)
 	{
 		debug ("addFileTemplate ()");
 		
 		fileTemplates.add(aTemplate);
 	}
 	/**
 	 * 
 	 */
 	private HoopProjectFile instantiateFile (String aClassName)
 	{
 		debug ("instantiateFile ("+aClassName+")");
 		
 		for (int i=0;i<fileTemplates.size();i++)
 		{
 			HoopProjectFile aTemplate=fileTemplates.get(i);
 			
 			if (aTemplate.getClassName().equals(aClassName)==true)
 			{
 				return (aTemplate);
 			}
 		}
 		
 		return (null);
 	}	
 	/**
 	 * 
 	 */
 	public void reset ()
 	{
 		debug ("reset ()");
 		
 		files=new ArrayList<HoopFile> ();
 	}
 	/**
 	 * 
 	 */
 	public void addFile (HoopProjectFile aFile)
 	{
 		debug ("addFile ()");
 		
 		files.add(aFile);
 	}
 	/**
 	 * 
 	 */
 	public ArrayList <HoopFile> getFiles ()
 	{
 		return (files);
 	}
 	/**
 	 * 
 	 */
 	public HoopFile getFileByName (String aFileName)
 	{
		debug ("getFileByName ("+aFileName+")");
 		
 		for (int i=0;i<files.size();i++)
 		{
 			HoopFile aFile=files.get(i);
 			
 			if (aFile.getInstanceName().toLowerCase().equals(aFileName.toLowerCase())==true)
 			{
 				return (aFile);
 			}
 		}
 		
 		return (null);		
 	}	
 	/**
 	 * 
 	 */
 	public HoopFile getFileByClass (String aClass)
 	{
 		debug ("getFileByClass ("+aClass+")");
 		
 		for (int i=0;i<files.size();i++)
 		{
 			HoopFile aFile=files.get(i);
 			
 			if (aFile.getClassName().toLowerCase().equals(aClass.toLowerCase())==true)
 			{
 				return (aFile);
 			}
 		}
 		
 		return (null);
 	}	
 	/**
 	 * This is just a shortcut! In no way should the project file revolve around
 	 * hoop graph editing.
 	 */
 	public HoopBase getGraphRoot ()
 	{
 		debug ("getGraphRoot ()");
 		
 		HoopGraphFile graphFile=(HoopGraphFile) getFileByClass (new HoopGraphFile ().getClassName());
 		
 		if (graphFile!=null)
 		{
 			return (graphFile.getGraphRoot ());
 		}
 		
 		return (null);
 	}
 	/**
 	 * This is just a shortcut! In no way should the project file revolve around
 	 * hoop graph editing.
 	 */
 	public ArrayList <HoopConnection> getGraphConnections ()
 	{
 		debug ("getGraphConnections ()");
 		
 		HoopGraphFile graphFile=(HoopGraphFile) getFileByClass (new HoopGraphFile ().getClassName());
 		
 		if (graphFile!=null)
 		{
 			return (graphFile.getHoopConnections ());
 		}
 		
 		return (null);
 	}	
 	/**
 	 * This is just a shortcut! In no way should the project file revolve around
 	 * hoop graph editing.
 	 */
 	public void setGraphRoot (HoopBase aHoop)
 	{
 		debug ("setGraphRoot ()");
 		
 		HoopGraphFile graphFile=(HoopGraphFile) getFileByClass (new HoopGraphFile ().getClassName());
 		
 		if (graphFile!=null)
 		{
 			graphFile.setGraphRoot(aHoop);
 		}		
 	}	
 	/**
 	 * 
 	 */
 	public Boolean newProject ()
 	{
 		debug ("newProject ()");
 		
 		setFileURI (".hprj");
 		
 		HoopGraphFile aGraph=new HoopGraphFile ();
 		aGraph.setFileURI("graph.xml");
 		
 		files.add(aGraph);
 		
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	public Boolean newProject (String aURI)
 	{
 		debug ("newProject ("+aURI+")");
 		
 		if (aURI==null)
 		{
 			return (newProject ());
 		}
 		
 		setFileURI (aURI+"/.hprj");
 		
 		HoopGraphFile aGraph=new HoopGraphFile ();
 		aGraph.setFileURI(aURI+"/graph.xml");
 		
 		files.add(aGraph);
 		
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	public Boolean save ()
 	{
 		debug ("save ()");
 		
 		for (int i=0;i<files.size();i++)
 		{
 			HoopFile tFile=files.get(i);
 			
 			if (tFile instanceof HoopProjectFile)
 			{			
 				HoopProjectFile saver=(HoopProjectFile) tFile;
 				
 				// Could be pre-defined you know
 				if (saver.getFileURI().isEmpty()==true)
 				{
 					saver.setFileURI(this.getBasePath ()+"/"+tFile.getInstanceName());
 				}	
 			
 				saver.save();
 			}	
 		}		
 		
 		return (super.save());
 	}	
 	/**
 	 * 
 	 */
 	public Boolean saveAs (String aURI)
 	{
 		debug ("saveAs ("+aURI+")");
 		
 		return (true);
 	}		
 	/**
 	 * 
 	 */
 	public Boolean load (String aURI)
 	{
 		debug ("load ("+aURI+")");
 		
 		File fLoader=new File (aURI);
 		if (fLoader.isDirectory()==true)
 		{
 			setFileURI (aURI+"/.hprj");
 		}
 		
 		if (HoopLink.fManager.doesFileExist (this.getFileURI())==false)
 		{
 			debug ("Error, file does not exist: " + this.getFileURI());
 			return (false);
 		}
 		
 		reset ();
 		
 		return (super.load(this.getFileURI()));
 	}
 	/**
 	*
 	*/	
 	public Boolean fromXML(Element root) 
 	{
 		debug ("fromXML ()");
 				
 		List <?> fileList = root.getChildren();
 		 
 		if (fileList.isEmpty()==false)
 		{
 			debug ("Parsing files in graph file");
 		
 			for (int i = 0; i < fileList.size(); i++) 
 			{
 				Element node = (Element) fileList.get(i);
 				
 				String aClass=node.getAttributeValue("class");
 				if (aClass!=null)
 				{
 					HoopProjectFile aFile=instantiateFile (aClass);
 					
 					if (aFile!=null)
 					{
 						aFile.reset();
 						
 						debug ("Loading project file: " + node.getAttributeValue("basepath") + "/" + node.getAttributeValue("instance"));
 						
 						if (aFile.load(node.getAttributeValue("basepath") + "/" + node.getAttributeValue("instance"))==true)
 						{
 							files.add(aFile);				
 						}
 						else
 							debug ("Error: unable to load file");
 					}
 					else
 						debug ("Error: unable to instantiate file class: " + aClass);
 				}
 				else
 					debug ("Error: unable to find class attribute in file xml");
 			}
 		}
 		else
 		{
 			debug ("Error: unable to find file list element in project file");
 			return (false);
 		}
 		
 		return (true);
 	}	
 	/**
 	*
 	*/	
 	public Element toXML() 
 	{
 		debug ("toXML ()");
 		
 		Element rootElement=super.toXML();
 		
 		for (int i=0;i<files.size();i++)
 		{
 			HoopFile aFile=files.get(i);
 			
 			if (aFile instanceof HoopProjectFile)
 			{			
 				HoopProjectFile saver=(HoopProjectFile) aFile;
 				
 				Element fileElement=saver.toXMLID();
 			
 				fileElement.setAttribute("basepath",saver.getBasePath());
 			
 				rootElement.addContent(fileElement);
 			}	
 		}
 	
 		return (rootElement);
 	}
 	/** 
 	 * @param aFile
 	 * @param aPath
 	 */
 	private void refreshSubDir (HoopFile aFile,String aPath)
 	{
 		debug ("refreshSubDir ()");
 		
 		ArrayList <String> testList=HoopLink.fManager.listDirectoryEntries(aPath);
 		
 		for (int i=0;i<testList.size();i++)
 		{
 			String aFileString=testList.get(i);
 			
 			if ((aFileString.equals (".")==false) && (aFileString.equals ("..")==false))
 			{
 				File testFile=new File (aPath+"/"+aFileString);
 				
				debug ("Testing file: " + aFileString);
 				
 				if (testFile.isDirectory()==true)
 				{
 					if (this.getFileByName(aFileString)==null)
 					{
 						HoopFile aDir=new HoopFile ();
 						aDir.setInstanceName(aFileString);
 						aDir.setIsDir(true);
 						
 						aFile.addSubEntry(aDir);
 						
 						refreshSubDir (aDir,aPath+"/"+aFileString);
 					}
 				}
 				
 				if (testFile.isFile()==true)
 				{
 					if (this.getFileByName(aFileString)==null)
 					{
 						HoopFile aDirEntry=new HoopFile ();
 						aDirEntry.setInstanceName(aFileString);
 						aDirEntry.setIsDir(false);
 						
 						aFile.addSubEntry(aDirEntry);
 					}					
 				}
 			}		
 		}	
 	}
 	/**
 	 * 
 	 */
 	public Boolean refresh ()
 	{
 		debug ("refresh ()");
 		
 		if (this.getVirginFile()==true)
 		{
 			debug ("Can't refresh a project that hasn't been saved yet");
 			return (false);
 		}
 		
 		// First check and refresh our local database ...
 		
 		if (HoopLink.dataSet==null)
 		{
 			HoopLink.dataSet=new HoopDataSet ();
 			HoopLink.dataSet.checkDB();
 		}
 		else
 			HoopLink.dataSet.checkDB();			
 		
 		// Then scan the project directory for any new files ...
 		
 		ArrayList <String> testList=HoopLink.fManager.listDirectoryEntries(getBasePath ());
 		
 		for (int i=0;i<testList.size();i++)
 		{
 			String aFileString=testList.get(i);
 			
 			if ((aFileString.equals(this.getInstanceName())==false) && (aFileString.equals (".")==false) && (aFileString.equals ("..")==false))
 			{
 				File testFile=new File (getBasePath ()+"/"+aFileString);
 				
 				debug ("Testing file: " + aFileString);
 				
 				if (testFile.isDirectory()==true)
 				{
 					if (this.getFileByName(aFileString)==null)
 					{
 						HoopFile aDir=new HoopFile ();
 						aDir.setInstanceName(aFileString);
 						aDir.setIsDir(true);
 						
 						files.add(aDir);
 						
 						refreshSubDir (aDir,testFile.getPath());
 					}
 				}
 				
 				if (testFile.isFile()==true)
 				{
 					if (this.getFileByName(aFileString)==null)
 					{
 						HoopFile aDir=new HoopFile ();
 						aDir.setInstanceName(aFileString);
 						aDir.setIsDir(false);
 						
 						files.add(aDir);						
 					}					
 				}
 			}
 		}
 		
 		return (true);
 	}
 }
