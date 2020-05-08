 /*-
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawnsci.intro.content;
 
 import java.io.PrintWriter;
 
 import org.dawnsci.plotting.jreality.util.JOGLChecker;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.intro.config.IIntroContentProviderSite;
 import org.eclipse.ui.intro.config.IIntroXHTMLContentProvider;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import uk.ac.diamond.scisoft.system.info.SystemInformation;
 
 /**
  * Content provider for the welcome intro page: system information checking
  * 
  * @author Baha El Kassaby
  * 
  */
 public class SystemInfoContentProvider implements IIntroXHTMLContentProvider{
 
 	@Override
 	public void init(IIntroContentProviderSite site) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void createContent(String id, PrintWriter out) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void createContent(String id, Composite parent, FormToolkit toolkit) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private boolean hasJOGL = false;
 	private boolean hasJOGLshaders = false;
 
 	@Override
 	public void createContent(String id, Element parent) {
 		SystemInformation.initialize();
 		Composite comp = PlatformUI.getWorkbench().getDisplay().getActiveShell();
 
 		hasJOGL = JOGLChecker.canUseJOGL_OpenGL(null, comp);
 		// re-check a second time if false
 		if(!hasJOGL)
 			hasJOGL = JOGLChecker.canUseJOGL_OpenGL(null, comp);
 		SystemInformation.setOpenGLSupport(hasJOGL);
 		SystemInformation.setOpenGLMaxTex(JOGLChecker.getMaxTextureWidth());
 		SystemInformation.setOpenGLVendor(JOGLChecker.getVendorName());
 		//hasJOGLshaders = logoComp.hasShaders();
 		// check if there is an overwrite
 //		IntroLogo logoComp;
 //		logoComp = new IntroLogo(comp,SWT.DOUBLE_BUFFERED,hasJOGL);
 		String propString = System.getProperty("uk.ac.diamond.analysis.rcp.plotting.useGL13");
 		if (propString != null && propString.toLowerCase().equals("true")) {
 			hasJOGLshaders = false;
 		}
 		SystemInformation.setGLSLSupport(hasJOGLshaders);
 		
 		//system check content provider
 		Document dom = parent.getOwnerDocument();
 		parent.setAttribute("class","grid_12");
 		parent.setAttribute("style", "color:#FFFFFF;background-color:#0F0F0F;height:100px;float:center;");
 		Element br = dom.createElement("br");
 		Element br0 = dom.createElement("br");
 		Element br1 = dom.createElement("br");
 		//Element br2 = dom.createElement("br");
 		Element br3 = dom.createElement("br");
 		Element br4 = dom.createElement("br");
 		Element br5 = dom.createElement("br");
 		Element b = dom.createElement("b");
 		Element spanOpenGL14 = dom.createElement("span");
 		//Element spanOpenGL21 = dom.createElement("span");
 		Element spanJavaMemory = dom.createElement("span");
 		Element spanNumberCPU = dom.createElement("span");
 		Element spanJavaVersion = dom.createElement("span");
 		parent.appendChild(br);
 		parent.appendChild(dom.createTextNode("\t"));
 		b.appendChild(dom.createTextNode("..System check..."));
 		b.appendChild(br0);
 		parent.appendChild(b);
 
 		String color = SystemInformation.writeHTMLInfo(SystemInformation.SUPPORTOPENGL)[0];
 		String content = SystemInformation.writeHTMLInfo(SystemInformation.SUPPORTOPENGL)[1];
		spanOpenGL14.setAttribute("style", "color:"+color);
		spanOpenGL14.appendChild(dom.createTextNode(content));
		parent.appendChild(dom.createTextNode("...Support OpenGL feature level 1.4........................"));
		spanOpenGL14.appendChild(br1);
		parent.appendChild(spanOpenGL14);
 
 //		color = SystemInformation.writeHTMLInfo(SystemInformation.SUPPORTGLSL)[0];
 //		content = SystemInformation.writeHTMLInfo(SystemInformation.SUPPORTGLSL)[1];
 //		spanOpenGL21.setAttribute("style", "color:"+color);
 //		spanOpenGL21.appendChild(dom.createTextNode(content));
 //		parent.appendChild(dom.createTextNode("...Support OpenGL feature level 2.1 (GLSL).........."));
 //		spanOpenGL21.appendChild(br2);
 //		parent.appendChild(spanOpenGL21);
 
 		color = SystemInformation.writeHTMLInfo(SystemInformation.TOTALMEMORY)[0];
 		content = SystemInformation.writeHTMLInfo(SystemInformation.TOTALMEMORY)[1];
 		spanJavaMemory.setAttribute("style", "color:"+color);
 		spanJavaMemory.appendChild(dom.createTextNode(content));
 		parent.appendChild(dom.createTextNode("...System memory available to Java......................"));
 		spanJavaMemory.appendChild(br3);
 		parent.appendChild(spanJavaMemory);
 
 		color = SystemInformation.writeHTMLInfo(SystemInformation.NUMCPUS)[0];
 		content = SystemInformation.writeHTMLInfo(SystemInformation.NUMCPUS)[1];
 		spanNumberCPU.setAttribute("style", "color:"+color);
 		spanNumberCPU.appendChild(dom.createTextNode(content));
 		parent.appendChild(dom.createTextNode("...Number of CPUs......................................................."));
 		spanNumberCPU.appendChild(br4);
 		parent.appendChild(spanNumberCPU);
 
 		color = SystemInformation.writeHTMLInfo(SystemInformation.JAVAVERSION)[0];
 		content = SystemInformation.writeHTMLInfo(SystemInformation.JAVAVERSION)[1];
 		spanJavaVersion.setAttribute("style", "color:"+color);
 		spanJavaVersion.appendChild(dom.createTextNode(content));
 		parent.appendChild(dom.createTextNode("...Java version.............................................................."));
 		spanJavaVersion.appendChild(br5);
 		parent.appendChild(spanJavaVersion);
 	}
 }
