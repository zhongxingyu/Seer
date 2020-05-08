 /*
   File: AbstractGraphReader.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.data.readers;
 
 import giny.view.GraphView;
 
 import java.io.File;
 import java.io.IOException;
import java.util.regex.Pattern;
 
 import cytoscape.CyNetwork;
 import cytoscape.layout.CyLayoutAlgorithm;
 import cytoscape.layout.CyLayouts;
 import cytoscape.task.TaskMonitor;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.view.CyNetworkView;
 
 
 /**
  *
  */
 public abstract class AbstractGraphReader implements GraphReader {
 	protected String fileName;
 
 	/**
 	 * Creates a new AbstractGraphReader object.
 	 *
 	 * @param fileName  DOCUMENT ME!
 	 */
 	public AbstractGraphReader(String fileName) {
 		this.fileName = fileName;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @throws IOException DOCUMENT ME!
 	 */
 	public abstract void read() throws IOException;
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public int[] getNodeIndicesArray() {
 		return null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public int[] getEdgeIndicesArray() {
 		return null;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getNetworkName() {
 		String t = "";
 
 		if (fileName != null) {
 			File tempFile = new File(fileName);
 			t = tempFile.getName();

			// Remove the file extension '.sif' or '.gml' as network title
			if (Pattern.matches(".+\\.SIF$", t.toUpperCase()) ||
					Pattern.matches(".+\\.GML$", t.toUpperCase()))
			{
				t = t.substring(0, t.length()-4);
			}			
 		}
 
 		return CyNetworkNaming.getSuggestedNetworkTitle(t);
 	}
 
 	/**
 	 * Executes post-processing:  no-op.
 	*/
 	public void doPostProcessing(CyNetwork network) {
 	}
 
 	/**
 	 * Return the CyLayoutAlgorithm used to layout the graph
 	 */
 	public CyLayoutAlgorithm getLayoutAlgorithm() {
 		return CyLayouts.getDefaultLayout();
 	}
 
 	/**
  	 * Set the task monitor to use for this reader
  	 *
  	 * @param monitor the TaskMonitor to use
  	 */
 	public void setTaskMonitor(TaskMonitor monitor) {
 	}
 
 	/**
 	 * @deprecated Use getLayoutAlgorithm().doLayout(view) instead. Will be removed 5/2008.
 	 */
 	public void layout(GraphView view) {
 		getLayoutAlgorithm().doLayout((CyNetworkView)view);
 	}
 }
