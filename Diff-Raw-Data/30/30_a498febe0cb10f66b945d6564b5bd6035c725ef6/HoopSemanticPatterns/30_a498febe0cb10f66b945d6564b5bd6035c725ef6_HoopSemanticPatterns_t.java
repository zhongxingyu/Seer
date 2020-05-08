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
 
 package edu.cmu.cs.in.hoop.hoops.analyze;
 
 import java.util.ArrayList;
 
 import edu.cmu.cs.in.base.kv.HoopKV;
 import edu.cmu.cs.in.base.kv.HoopKVInteger;
 import edu.cmu.cs.in.base.kv.HoopKVString;
 import edu.cmu.cs.in.hoop.hoops.base.HoopAnalyze;
 import edu.cmu.cs.in.hoop.hoops.base.HoopBase;
 import edu.cmu.cs.in.hoop.hoops.base.HoopInterface;
 import edu.cmu.cs.in.hoop.properties.types.HoopIntegerSerializable;
 
 /**
 * 
 */
 public class HoopSemanticPatterns extends HoopAnalyze implements HoopInterface
 {    				
 	private static final long serialVersionUID = -1639663485291926304L;
 	
 	protected HoopIntegerSerializable maxGap=null;
 	
 	/**
 	 *
 	 */
     public HoopSemanticPatterns () 
     {
 		setClassName ("HoopSemanticPatterns");
 		debug ("HoopSemanticPatterns ()");
 				
 		//removeOutPort ("KV");
 		
 		setHoopDescription ("Create features based on semantic patterns");
 		
 		maxGap=new HoopIntegerSerializable (this,"maxGap",5);
     }
 	/**
 	 * We assume here a stream of tokens or terms with a sliding window that
 	 * gets reset when an end of sentence marker is found. End of sentence
 	 * markers are either newlines, periods, question marks or exclamation
 	 * marks. Discovered patterns are stored per sentence and then further
 	 * refined to meet the semantic criteria encoded in this hoop or any
 	 * derived hoops.
	 * 
	 * Based on:
	 * 
	 * Philip Gianfortoni, David Adamson, and Carolyn P. Ros. 2011. Modeling 
	 * of stylistic variation in social media with stretchy patterns. In 
	 * Proceedings of the First Workshop on Algorithms and Resources for 
	 * Modelling of Dialects and Language Varieties (DIALECTS '11). Association 
	 * for Computational Linguistics, Stroudsburg, PA, USA, 49-59. 
	 * 
	 * Jacob Eisenstein and Regina Barzilay. 2008. Bayesian unsupervised topic 
	 * segmentation. In Proceedings of the Conference on Empirical Methods in 
	 * Natural Language Processing (EMNLP '08). Association for Computational 
	 * Linguistics, Stroudsburg, PA, USA, 334-343.
	 * 
 	 */
 	public Boolean runHoop (HoopBase inHoop)
 	{		
 		debug ("runHoop ()");
 			
 		ArrayList <HoopKV> inData=inHoop.getData();
 		
 		if (inData!=null)
 		{
 			HoopKVString overview=new HoopKVString ();
 			overview.setKey("N");
 			overview.setValue(String.format("%d",inData.size()));

 			for (int t=0;t<inData.size();t++)
 			{
 				HoopKVInteger aKV=(HoopKVInteger) inData.get(t);
 												
 				ArrayList<Object> vals=aKV.getValuesRaw();

 				for (int i=0;i<vals.size();i++)
 				{
 
				}
 			}			
 		}	
 						
 		return (true);				
 	}	 
 	/**
 	 * 
 	 */
 	public HoopBase copy ()
 	{
 		return (new HoopSemanticPatterns ());
 	}	
 }
