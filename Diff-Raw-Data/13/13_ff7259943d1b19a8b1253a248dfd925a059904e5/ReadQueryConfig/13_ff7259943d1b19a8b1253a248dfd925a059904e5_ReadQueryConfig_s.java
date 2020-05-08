 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // ReadQueryConfig.java
 // Since: May 26, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.bio;
 
 import java.io.Serializable;
 
 /**
  * Read query configuration
  * 
  * @author leo
  * 
  */
 public class ReadQueryConfig implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public static enum Layout {
 		PILEUP, COVERAGE
 	}
 
 	public boolean hasCanvasSupport;
 	public Layout layout;
 	public int pixelWidth;
 	public int maxmumNumberOfReadsToDisplay = 500;
 	/**
 	 * Path to the WIG db file of the read depth
 	 */
 	public String wigPath;
 	public GraphWindow window = GraphWindow.MAX;
 
 	public ReadQueryConfig() {
 	}
 
 	public ReadQueryConfig(int pixelWidth, boolean hasCanvasSupport, Layout layout, int maxmumNumberOfReadsToDisplay, String wigPath) {
 		this(pixelWidth, hasCanvasSupport, layout);
 		this.maxmumNumberOfReadsToDisplay = maxmumNumberOfReadsToDisplay;
		this.wigPath = wigPath;
 	}
 
 	public ReadQueryConfig(int pixelWidth, boolean hasCanvasSupport, Layout layout) {
 		this.pixelWidth = pixelWidth;
 		this.layout = layout;
 		this.hasCanvasSupport = hasCanvasSupport;
 	}
 
 }
