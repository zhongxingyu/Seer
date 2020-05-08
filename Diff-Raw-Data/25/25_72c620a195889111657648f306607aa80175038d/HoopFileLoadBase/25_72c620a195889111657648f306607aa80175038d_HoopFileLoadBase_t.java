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
 
 package edu.cmu.cs.in.hoop.hoops.base;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import edu.cmu.cs.in.base.HoopDataType;
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.io.HoopFileTools;
 import edu.cmu.cs.in.base.kv.HoopKVString;
import edu.cmu.cs.in.hoop.properties.types.HoopEnumSerializable;
 import edu.cmu.cs.in.hoop.properties.types.HoopIntegerSerializable;
 import edu.cmu.cs.in.hoop.properties.types.HoopURISerializable;
 
 /**
 * 
 */
 public class HoopFileLoadBase extends HoopLoadBase implements HoopInterface
 {    				
 	private static final long serialVersionUID = -3882301282248283204L;
 	
 	protected HoopKVString fileKV=null;		
 	protected HoopURISerializable URI=null;
 	protected HoopIntegerSerializable maxFiles=null;
 	protected HoopIntegerSerializable batchSize=null;
	protected HoopEnumSerializable mode=null; // LINEAR,SAMPLE
 	
 	private ArrayList <String> files=null;
 	
 	protected Integer fileIndex=0;
 	protected Integer actualBatchSize=1;
 	protected Integer maxEntries=1;
 	
 	/**
 	 *
 	 */
     public HoopFileLoadBase () 
     {
 		setClassName ("HoopFileLoadBase");
 		debug ("HoopFileLoadBase ()");
 		
 		setHoopDescription ("Load Text File(s)");
 					
 		URI=new HoopURISerializable (this,"URI","");
 		maxFiles=new HoopIntegerSerializable (this,"maxFiles",1);
 		batchSize=new HoopIntegerSerializable (this,"batchSize",1);
		mode=new HoopEnumSerializable (this,"mode","LINEAR,SAMPLE");
     }
 	/**
 	 * 
 	 */
 	public String getFileExtension() 
 	{
 		return URI.getFileExtension();
 	}
 	/**
 	 * 
 	 */	
 	public void setFileExtension(String fileExtension) 
 	{
 		this.setFileExtension(fileExtension);
 	}    
 	/**
 	 *
 	 */
 	public void setContent(String content) 
 	{
 		fileKV.setValue(content);
 	}
 	/**
 	 *
 	 */
 	public String getContent() 
 	{
 		return fileKV.getValue();
 	}
 	/**
 	 *
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 		
 		this.resetData();
 		
 		setKVType (0,HoopDataType.LONG,"Timestamp"); // KEY !!!!!!!!
 		setKVType (1,HoopDataType.STRING,"Content");
 		setKVType (2,HoopDataType.STRING,"URL");
 		setKVType (3,HoopDataType.STRING,"File Name");
 		setKVType (4,HoopDataType.STRING,"Created");
 		setKVType (5,HoopDataType.STRING,"Modified");
 		setKVType (6,HoopDataType.STRING,"Author");
 		
 		if (URI.getValue().indexOf("<PROJECTPATH>")!=-1)
 		{
 			if (HoopLink.project==null)
 			{
 				this.setErrorString ("You need a project first for this hoop to be useful");
 				return (false);
 			}
 		
 			if (HoopLink.project.getVirginFile()==true)
 			{
 				this.setErrorString ("You to save your project first for this hoop to be useful");
 				return (false);
 			}
 		}	
 				
 		if (URI.getDirsOnly()==false)
 		{				
 			debug ("Processing single file ...");
 			
 			if (processSingleFile (HoopLink.relativeToAbsolute(URI.getValue()))==false)
 				return (false);
 			else
 			{
 				getVisualizer ().setExecutionInfo (" R: 1 out of 1");				
 				return (true);
 			}
 		}
 		
 		if (URI.getDirsOnly()==true)
 		{
 			debug ("Processing file set ...");
 			
 			if (files==null)
 			{
 				fileIndex=0;
 				
 				maxEntries=maxFiles.getPropValue();
 				
 				files=new ArrayList<String> ();
 				
 				ArrayList <String> tempList=HoopLink.fManager.listFiles(HoopLink.relativeToAbsolute(URI.getValue()));
 																
 				for (int i=0;i<tempList.size();i++)
 				{
 					String testEntry=tempList.get(i);
 					
 					// Filter unwanted entries ...
 					
 					if ((testEntry.equals(".")==false) && (testEntry.equals("..")==false))
 					{
 						File finalTest=new File (testEntry);
 						
 						if (finalTest.isDirectory()==false)
 						{
 							files.add(testEntry);
 						}
 					}
 				}
 				
 				actualBatchSize=1;
 												
 				debug ("files.size (): " + files.size() + " < + batchSize.getPropValue(): " + batchSize.getPropValue());
 				
 				if (files.size()<batchSize.getPropValue())
 				{										
 					actualBatchSize=files.size();
 				}	
 				else
 				{
 					actualBatchSize=batchSize.getPropValue();
 				}
 				
 				debug ("Using batch size: " + actualBatchSize);
 				
 				if (maxEntries<1)
 					maxEntries=files.size();
 				else
 				{
 					if (files.size ()<maxEntries)
 						maxEntries=files.size();
 				}
 			}
 
 			for (int w=0;w<(fileIndex+actualBatchSize);w++)
 			{			
 				String nextFile=files.get(w);
 			
 				if (processSingleFile (HoopLink.relativeToAbsolute(URI.getValue())+"/"+nextFile)==false)
 				{
 					return (false);
 				}
 			}	
 			
 			StringBuffer aStatus=new StringBuffer ();
 			
 			Integer runCount=fileIndex+actualBatchSize;
 			
 			aStatus.append (" R: ");
 			aStatus.append (runCount.toString());
 			aStatus.append (" out of ");
 			aStatus.append (String.format("%d",files.size()));
 			
 			debug (aStatus.toString ());
 			
 			getVisualizer ().setExecutionInfo (aStatus.toString ());
 			
 			fileIndex+=actualBatchSize;
 			
 			debug ("fileIndex: " + fileIndex + ", actualBatchSize: " + actualBatchSize + ", files.size (): " + files.size());
 			
 			if (fileIndex<maxEntries)
 			{
 				this.setDone(false);
 			}
 		}
 		
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	private Boolean processSingleFile (String aPath)
 	{
 		debug ("processSingleFile ("+aPath+")");
 		
 		String contents=HoopLink.fManager.loadContents(aPath);
 		
 		if (contents==null)
 		{
 			this.setErrorString(HoopLink.fManager.getErrorString());
 			return (false);
 		}
 		
 		fileKV=new HoopKVString ();
 	
 		Long stringStamp=HoopLink.fManager.getFileTime(aPath);
 		File namer=new File (aPath);
 		
 		HoopFileTools fTools=new HoopFileTools ();
 		
 		fileKV.setKey (fTools.getFileTimeString(stringStamp));
 		fileKV.setValue (contents);
 		fileKV.add (HoopLink.fManager.getURI());	
 		fileKV.add (namer.getName());
 		fileKV.add (fTools.getFileTimeString(stringStamp)); // Created
 		fileKV.add (fTools.getFileTimeString(stringStamp)); // Modified
 		fileKV.add ("unknown"); // Owner
 		
 		/*
 		UserPrincipal owner = Files.getOwner(path);
 		String username = owner.getName();
 		*/
 							
 		addKV (fileKV);
 		
 		return (true);
 	}
 	/**
 	 *
 	 */	
 	public void setInputStreamPath(String inputStreamPath) 
 	{
 		URI.setValue(inputStreamPath);
 	}
 	/**
 	 *
 	 */	
 	public String getInputStreamPath() 
 	{
 		return URI.getValue();
 	}	
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopFileLoadBase ());
 	}		
 	/**
 	 * 
 	 */
 	public void showFiles ()
 	{
 		debug ("showFiles ()");
 		
 		for (int i=0;i<files.size();i++)
 		{
 			String aFile=files.get(i);
 			
 			debug ("File: " + aFile);
 		}
 	}
 }
