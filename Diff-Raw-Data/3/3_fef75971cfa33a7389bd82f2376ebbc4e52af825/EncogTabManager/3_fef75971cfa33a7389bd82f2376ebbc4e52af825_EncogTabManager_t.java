 /*
  * Encog(tm) Workbench v2.6 
  * http://www.heatonresearch.com/encog/
  * http://code.google.com/p/encog-java/
  
  * Copyright 2008-2010 Heaton Research, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *   
  * For more information on Heaton Research copyrights, licenses 
  * and trademarks visit:
  * http://www.heatonresearch.com/copyright
  */
 package org.encog.workbench.tabs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.encog.neural.data.NeuralDataSet;
 import org.encog.neural.networks.BasicNetwork;
 import org.encog.workbench.EncogWorkBench;
 import org.encog.workbench.frames.EncogCommonFrame;
 import org.encog.workbench.frames.document.EncogDocumentFrame;
 import org.encog.workbench.frames.document.tree.ProjectFile;
 import org.encog.workbench.tabs.files.BasicFileTab;
 
 public class EncogTabManager {
 	private final List<EncogCommonTab> tabs = new ArrayList<EncogCommonTab>();
 	private final EncogDocumentFrame owner;
 
 	public EncogTabManager(final EncogDocumentFrame owner) {
 		this.owner = owner;
 	}
 
 	public void add(final EncogCommonTab tab) {
 		this.tabs.add(tab);
 		tab.setParent(this.owner);
 	}
 
 	public boolean contains(EncogCommonTab tab) {
 		return this.tabs.contains(tab);
 	}
 
 	/**
 	 * @return the frames
 	 */
 	public List<EncogCommonTab> getTabs() {
 		return this.tabs;
 	}
 
 	/**
 	 * @return the owner
 	 */
 	public EncogCommonFrame getOwner() {
 		return this.owner;
 	}
 
 	public void remove(final EncogCommonTab frame) {
 		this.tabs.remove(frame);
 	}
 	
 	public boolean isTrainingOrNetworkOpen()
 	{
 
 		return false;
 	}
 	
 	public void closeTrainingOrNetwork()
 	{
 		Object[] list = this.tabs.toArray();
 		for(int i=0;i<list.length;i++) {
 			EncogCommonTab tab = (EncogCommonTab)list[i];
 			
 			/*if( tab.getEncogObject() instanceof BasicNetwork 
 					|| tab.getEncogObject() instanceof NeuralDataSet )
 			{				
 				tab.dispose();
 			}*/
 		}
 	}
 	
 	public boolean checkTrainingOrNetworkOpen()
 	{
 		if( isTrainingOrNetworkOpen() )
 		{
 			if( !EncogWorkBench.askQuestion("Windows Open", "There are training and/or network windows open.\nBefore training can begin, these must be closed.  Do you wish to close them now?"))
 			{
 				return false;
 			}
 			closeTrainingOrNetwork();
 		}
 		
 		return true;
 	}
 
 	public boolean alreadyOpen(EncogCommonTab tab) {
 		return this.tabs.contains(tab);
 	}
 
 	public EncogCommonTab find(File file) {
 		for (final EncogCommonTab tab : this.tabs) {
 			ProjectFile pf = (ProjectFile)tab.getEncogObject();
			if( pf==null )
				continue;
			
 			if( file.equals(pf.getFile()))
 				return tab;
 						
 		}
 		return null;
 		
 	}
 
 	public void closeAll() {
 		Object[] list = this.tabs.toArray();
 		for(int i=0;i<list.length;i++) {
 			EncogCommonTab tab = (EncogCommonTab)list[i];
 			tab.dispose();
 		}
 	}
 
 	public void closeAll(File f) {
 		Object[] list = this.tabs.toArray();
 		for(int i=0;i<list.length;i++) {
 			EncogCommonTab tab = (EncogCommonTab)list[i];
 			if( tab.getEncogObject()!=null ) {
 				if( tab.getEncogObject().getFile() !=null ) {
 					if( tab.getEncogObject().getFile().equals(f)) {
 						tab.setDirty(false);
 						tab.dispose();						
 					}
 				}
 			}
 		}
 		
 	}
 }
