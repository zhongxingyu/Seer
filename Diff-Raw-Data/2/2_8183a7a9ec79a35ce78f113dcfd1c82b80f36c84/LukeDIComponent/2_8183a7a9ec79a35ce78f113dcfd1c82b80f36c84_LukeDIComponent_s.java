 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2008 the original author or authors.
  * 
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  * 
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 
 package org.paxle.desktop.addon.luke.impl;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Toolkit;
 
 import org.getopt.luke.Luke;
 import org.paxle.desktop.DIComponent;
 
 public class LukeDIComponent implements DIComponent {
 	
 	private static final Dimension DEFAULT_DIM = new Dimension(700, 500);
	private static final String TITLE = "Luke - Lucene Index Toolbox, v 0.8.1 (2008-02-13)";
 	
 	private final Luke luke = new Luke();
 	
 	public void close() {
 		luke.actionClose();
 	}
 	
 	public Container getContainer() {
 		return luke;
 	}
 	
 	public String getTitle() {
 		return "Luke Toolbox";
 	}
 	
 	public Dimension getWindowSize() {
 		return DEFAULT_DIM;
 	}
 	
 	public void setFrame(Frame f) {
 		f.setTitle(TITLE);
 		f.setPreferredSize(DEFAULT_DIM);
 	    f.setIconImage(Toolkit.getDefaultToolkit().createImage(Luke.class.getResource("/img/luke.gif")));
 	}
 }
