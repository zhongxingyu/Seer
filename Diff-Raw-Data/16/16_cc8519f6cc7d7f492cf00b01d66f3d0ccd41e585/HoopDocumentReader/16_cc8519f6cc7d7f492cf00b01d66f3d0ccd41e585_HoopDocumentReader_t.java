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
 
 package edu.cmu.cs.in.hoop.hoops.load;
 
 import java.util.Iterator;
 
 import com.sleepycat.collections.StoredMap;
 
 import edu.cmu.cs.in.base.HoopLink;
 import edu.cmu.cs.in.base.kv.HoopKVDocument;
 import edu.cmu.cs.in.base.kv.HoopKVString;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopLoadBase;
 import edu.cmu.cs.in.hoop.properties.types.HoopEnumSerializable;
 //import edu.cmu.cs.in.hoop.properties.types.HoopIntegerSerializable;
 import edu.cmu.cs.in.search.HoopDataSet;
 
 public class HoopDocumentReader extends HoopLoadBase
 {
 	private static final long serialVersionUID = 3069547626921137451L;
 	
 	private HoopEnumSerializable selectedField=null;
 	private StoredMap<Long, HoopKVDocument> inp=null;
 	private Iterator<HoopKVDocument> dbIterator = null;
 	
 	private boolean newRun=true;
 		
 	/**
 	 *
 	 */ 
 	public HoopDocumentReader () 
 	{		
 		setClassName ("HoopDocumentReader");
 		debug ("HoopDocumentReader ()");
 		
 		setHoopDescription ("Load KVs from Document DB");
 				
 		removeInPort ("KV");
 		enableProperty ("URI",false);
 		
 		selectedField=new HoopEnumSerializable (this,"selectedField","title,author,abstr,text,createDate,modifiedDate,keywords,url,description,tokens");
 	}
 	/**
 	 * 
 	 */
 	public void reset ()
 	{
 		newRun=true;
 	}
 	/**
 	 *
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 					
 		debug ("Mapping project path ("+getProjectPath ()+") to db dir ...");
 				
 		if (HoopLink.dataSet==null)
 		{
 			HoopLink.dataSet=new HoopDataSet ();
 			HoopLink.dataSet.checkDB ();
 		}
 		else
 			HoopLink.dataSet.checkDB ();
 
 		if (newRun==true)
 		{
 			inp=HoopLink.dataSet.getData();
 		
 			dbIterator = inp.values().iterator();
 		}	
 		
 		if (inp==null)
 		{
 			this.setErrorString("Unable to obtain handle to document database");
 			return (false);
 		}
 		
 		int actualSize=inp.size();
 		
 		calculateIndexingSizes (actualSize);
 				
 		if (mode.getValue().equals("LINEAR")==true)
 			processLinear ();
 		else
 			processSample ();
 						
 		return (true);
 	}
 	/**
 	 * 
 	 */
 	private void processLinear ()
 	{
 		debug ("processLinear ()");
 				
 		bCount=0;
 		
 		while (checkLoopDone ()==false)		
 		{			
 			if (dbIterator.hasNext()==true)
 			{			
 				if (loadDataObject (loadIndex)==false)
 					return;
 			}	
 			
 			loadIndex++; // Update total index
 			bCount++; // Update batch count
 		}		
 		
 		if (checkDone ()==false)
 			this.setDone(false);
 	}
 	/**
 	 * 
 	 */
 	private void processSample ()
 	{
 		debug ("processSample ()");
 		
 		bCount=0;
 		
 		while (checkLoopDone ()==false)		
 		{			
 			if (loadDataObject (getSample (originalSize))==false)
 				return;
 			
 			loadIndex++; // Update total index
 			bCount++; // Update batch count
 		}		
 		
 		if (checkDone ()==false)
 			this.setDone(false);
 	}	
 	/**
 	 * 
 	 */
 	protected boolean loadDataObject (int anIndex)
 	{
 		debug ("loadDataObject ("+anIndex+")");
 		
 		HoopKVDocument aDoc=(HoopKVDocument) dbIterator.next();
 		
 		//StoredMap<Long, HoopKVDocument> inp=HoopLink.dataSet.getData();
 				
 		//HoopKVDocument aDocument=inp.get(anIndex);
 		
 		processDocument (aDoc);		
 		
 		return (true);
 	}	
 	/**
 	 * 
 	 */
 	private void processDocument (HoopKVDocument aDocument)
 	{
 		debug ("processDocument ()");
 		
 		if (selectedField.getValue().equalsIgnoreCase("title")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.title.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("author")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.author.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("description")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.description.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("createDate")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.createDate.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("modifiedDate")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.modifiedDate.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("text")==true)
 		{
			debug ("Creating string KV out of document ("+aDocument.getKeyString()+"), with text: " + aDocument.getValue());
			
 			HoopKVString textContent=new HoopKVString (aDocument.getKeyString(),aDocument.getValue());
 			
 			// Copy all other versions of the text into the new document (if available)
 			
 			for (int j=0;j<aDocument.getValuesRaw().size();j++)
 			{
 				if (j>0)
 				{
 					textContent.setValue(aDocument.getValue(j),j);
 				}
 			}
 			
 			this.addKV(textContent);
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("abstr")==true)
 		{
 			HoopKVString textContent=new HoopKVString (aDocument.abstr.getKeyString(),aDocument.getValue());
 			
 			// Copy all other versions of the abstract into the new document (if available)
 			
 			for (int j=0;j<aDocument.abstr.getValuesRaw().size();j++)
 			{
 				if (j>0)
 				{
 					textContent.setValue(aDocument.abstr.getValue(j),j);
 				}
 			}
 			
 			this.addKV(textContent);
 		}	
 		
 		if (selectedField.getValue().equalsIgnoreCase("keywords")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.keywords.getValue()));
 		}
 		
 		if (selectedField.getValue().equalsIgnoreCase("url")==true)
 		{
 			this.addKV(new HoopKVString (aDocument.getKeyString(),aDocument.url.getValue()));
 		}				
 	}
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopDocumentReader ());
 	}	
 }
