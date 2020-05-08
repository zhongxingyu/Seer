 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.objectmodel;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.github.a2g.core.interfaces.ConstantsForAPI;
 
 
 public class VerbCollection {
 	private List<Verb> verbs;
	//VerbCollectionCallbackAPI api;
	VerbCollection(/*VerbCollectionCallbackAPI api*/)
 	{
 		//this.api = api;
 		verbs = new ArrayList<Verb>();
 
 		verbs.add(new Verb("Walk", "Walk to AAA", ConstantsForAPI.WALK));
 		verbs.add(new Verb("Talk", "Talk to AAA", ConstantsForAPI.TALK));
 		verbs.add(new Verb("Examine", "Examine AAA", ConstantsForAPI.EXAMINE));
 		verbs.add(new Verb("Grab", "Grab AAA", ConstantsForAPI.GRAB));
 		verbs.add(new Verb("Cut", "Cut AAA|Cut AAA with BBB", ConstantsForAPI.CUT));
		verbs.add(new Verb("Swing", "Swing AAA", ConstantsForAPI.SWING));
 		verbs.add(new Verb("Turn on","Turn on AAA", ConstantsForAPI.TURN_ON));
 		verbs.add(new Verb("Use","Use AAA|Use AAA with BBB", ConstantsForAPI.USE));
 		verbs.add(new Verb("Push", "Push AAA", ConstantsForAPI.PUSH));
 		verbs.add(new Verb("Pull", "Pull AAA", ConstantsForAPI.PULL));
 		verbs.add(new Verb("Throw", "Throw AAA|Throw AAA at BBB", ConstantsForAPI.THROW));
 		verbs.add(new Verb("Eat", "Eat AAA", ConstantsForAPI.EAT));
 	}
 
 	public Verb get(int i) {
 		return verbs.get(i);
 	}
 
 	public void removeByCode(int verbCode)
 	{
 		for(int i = 0 ; i<verbs.size();i++)
 		{
 			if(verbs.get(i).getCode() == verbCode)
 			{
 				verbs.remove(i);
 				//api.update();
 				break;
 			}
 		}
 	}
 
 	public int getNumberOfRows() {
 		Double d= Math.sqrt(verbs.size()*1.0)+.9;
 		int numRows = d.intValue();
 		return numRows;
 	}
 
 	public int getNumberOfColumns() {
 		int numRows = getNumberOfRows();
 		Double e = (verbs.size()/(numRows*1.0))+.90;
 
 		int numCols = e.intValue();
 		return numCols;
 	}
 
 	public int size()
 	{
 		int size = verbs.size();
 		return size;
 	}
 }
