 /*
 
 Copyright (C) 2009 Steffen Dienst
 
 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package de.elateportal.editor.components.panels;
 
 import java.io.StringWriter;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.protocol.http.RequestUtils;
 import org.apache.wicket.protocol.http.WebRequest;
 import org.apache.wicket.request.target.basic.RedirectRequestTarget;
 
 import de.elateportal.editor.components.panels.tree.ComplexTaskDefTree;
 import de.elateportal.editor.pages.TaskDefPage;
 import de.elateportal.editor.preview.DummyTaskFactoryImpl;
 import de.elateportal.model.ComplexTaskDef;
 import de.thorstenberger.taskmodel.TaskModelViewDelegate;
 import de.thorstenberger.taskmodel.TaskModelViewDelegateObject;
 import de.thorstenberger.taskmodel.complex.ComplexTaskFactory;
 import de.thorstenberger.taskmodel.complex.ComplexTaskletCorrectorImpl;
 import de.thorstenberger.taskmodel.complex.complextaskdef.impl.ComplexTaskDefDAOImpl;
 import de.thorstenberger.taskmodel.complex.complextaskhandling.impl.ComplexTaskHandlingDAOImpl;
 import de.thorstenberger.taskmodel.complex.impl.ComplexTaskBuilderImpl;
 import de.thorstenberger.taskmodel.complex.impl.ComplexTaskFactoryImpl;
 import de.thorstenberger.taskmodel.impl.TaskManagerImpl;
 import de.thorstenberger.taskmodel.impl.TaskModelViewDelegateObjectImpl;
 import de.thorstenberger.taskmodel.impl.TaskletContainerImpl;
 
 /**
  * @author Steffen Dienst
  * 
  */
 public class PreviewPanel extends Panel {
 	private static AtomicLong tryId = new AtomicLong(0);
 
 	public PreviewPanel(String id, final ComplexTaskDefTree tree) {
 		super(id);
 		setOutputMarkupId(true);
 
 		add(new Link("previewLink") {
 
 			@Override
 			public void onClick() {
 
 				ComplexTaskFactory ctf = new ComplexTaskFactoryImpl(new ComplexTaskletCorrectorImpl());
 				DummyTaskFactoryImpl taskfactory = new DummyTaskFactoryImpl(new ComplexTaskDefDAOImpl(ctf),
 				    new ComplexTaskHandlingDAOImpl(
 				    ctf),
 				    new ComplexTaskBuilderImpl(ctf));
 				// use currently selected taskmodel (in the tree)
 				ComplexTaskDef sampleTaskdef = tree.getCurrentTaskdef();
 
 				try {
 					// marshal to xml
 					JAXBContext context = JAXBContext.newInstance(ComplexTaskDef.class);
 					final Marshaller marshaller = context.createMarshaller();
 					StringWriter sw = new StringWriter();
 
 					marshaller.marshal(sampleTaskdef, sw);
 					// set xml to use
 					taskfactory.setTaskDefXml(sw.toString());
					TaskManagerImpl tm = new TaskManagerImpl(taskfactory, new TaskletContainerImpl(taskfactory));
 
 					TaskModelViewDelegateObject delegateObject = new TaskModelViewDelegateObjectImpl(0,
 					    tm,
 					    "sampleUser", "Max Mustermann",
 					    RequestUtils.toAbsolutePath(urlFor(TaskDefPage.class, null).toString()));
 					TaskModelViewDelegate.storeDelegateObject(getSession().getId(), 0, delegateObject);

 					getRequestCycle().setRequestTarget(
 					    new RedirectRequestTarget(
 					    getContextUrl() + "/taskmodel-core-view/execute.do?id=0&todo=new&try=" + tryId.incrementAndGet()));
 				} catch (JAXBException e) {
 					e.printStackTrace();
 				}
 			}
 
 		});
 	}
 
 	public StringBuffer getContextUrl() {
 		HttpServletRequest req = ((WebRequest) getRequest()).getHttpServletRequest();
 		String protocol = req.isSecure() ? "https://" : "http://";
 		String hostname = req.getServerName();
 		int port = req.getServerPort();
 		StringBuffer url = new StringBuffer(128);
 		url.append(protocol);
 		url.append(hostname);
 		if ((port != 80) && (port != 443)) {
 			url.append(":");
 			url.append(port);
 		}
 		return url;
 	}
 
 }
