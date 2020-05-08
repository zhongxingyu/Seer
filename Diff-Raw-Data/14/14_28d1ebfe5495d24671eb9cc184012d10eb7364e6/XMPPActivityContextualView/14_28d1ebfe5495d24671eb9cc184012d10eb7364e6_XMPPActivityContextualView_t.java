 /*******************************************************************************
  * Copyright (C) 2007 The University of Manchester   
  *               2009 Egon Willighagen <egon.willighagen@gmail.com>
  * 
  *  Modifications to the initial code base are copyright of their
  *  respective authors, or their employers as appropriate.
  * 
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2.1 of
  *  the License, or (at your option) any later version.
  *    
  *  This program is distributed in the hope that it will be useful, but
  *  WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *    
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  ******************************************************************************/
 package net.sf.taverna.t2.activities.xmpp.views;
 
 import java.awt.Frame;
 
 import javax.swing.Action;
 
 import net.sf.taverna.t2.activities.xmpp.XMPPActivity;
 import net.sf.taverna.t2.activities.xmpp.XMPPConfigurationBean;
 import net.sf.taverna.t2.activities.xmpp.actions.XMPPActivityConfigurationAction;
 import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
 import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
 
 public class XMPPActivityContextualView extends HTMLBasedActivityContextualView<XMPPConfigurationBean> {
 
 	private static final long serialVersionUID = -553974533001808511L;
 
 	public XMPPActivityContextualView(Activity<?> activity) {
 		super(activity);
 	}
 
 	@Override
 	protected String getViewTitle() {
 		return "XMPP Activity";
 	}
 
 	@Override
 	protected String getRawTableRowsHtml() {
		String html = "<tr><td>Client JID</td><td>"+getConfigBean().getClientJID()+"</td></tr>" +
		  "<tr><td>Client JID password</td><td>"+getConfigBean().getPassword()+"</td></tr>" +
		  "<tr><td>Jabber host</td><td>"+getConfigBean().getHost()+"</td></tr>" +
		  "<tr><td>Jabber host port</td><td>"+getConfigBean().getPort()+"</td></tr>" +
		  "<tr><td>Service JID</td><td>"+getConfigBean().getServiceJID()+"</td></tr>" +;
 		return html;
 	}
 
 	@Override
 	public Action getConfigureAction(Frame owner) {
 		return new XMPPActivityConfigurationAction((XMPPActivity)getActivity(),owner);
 	}
 
 }
 
 
