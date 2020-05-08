 package net.bioclipse.xws4j.business;
 import org.w3c.dom.Element;
 
 import net.bioclipse.xws.JavaDOMTools;
 import net.bioclipse.xws.client.Client;
 import net.bioclipse.xws.client.IXmppItem;
 import net.bioclipse.xws.client.adhoc.IFunction;
 import net.bioclipse.xws.client.adhoc.IService;
 import net.bioclipse.xws.client.adhoc.IoSchemata;
 import net.bioclipse.xws.exceptions.XmppException;
 import net.bioclipse.xws4j.Activator;
 import net.bioclipse.xws4j.exceptions.Xws4jException;
 import net.bioclipse.xws.binding.BindingManager;
 import net.bioclipse.xws.binding.IIoFactory;
 import net.bioclipse.xws.binding.exceptions.XwsBindingException;
 /**
  * 
  * This file is part of the Bioclipse xws4j Plug-in.
  * 
  * Copyright (C) 2008 Johannes Wagener
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * @author Johannes Wagener, Ola Spjuth
  */
 public class XwsManager implements IXwsManager {
        public String getManagerName() {
                 return "xmpp";
         }
     public Client getDefaultClient() throws Xws4jException {
     	return Activator.getDefaultClientCurator().getDefaultClient();
     }
     public String getStatus() {
     	Client client;
     	try {
     		client = Activator.getDefaultClientCurator().getDefaultClient();
     	} catch (Exception e) {
     		return e.toString();
     	}
     	return client.toString();
     }
     public void connect() throws Xws4jException, XmppException {
     	Client client;
    		client = Activator.getDefaultClientCurator().getDefaultClient();
    		client.connect();
     }
     public void disconnect() throws Xws4jException, XmppException {
     	Client client;
    		client = Activator.getDefaultClientCurator().getDefaultClient();
    		client.disconnect();
     }
     public boolean isConnected() throws Xws4jException {
     	Client client;
    		client = Activator.getDefaultClientCurator().getDefaultClient();
    		return client.isConnected();
     }
     public IXmppItem getXmppItem(String jid, String node) throws Xws4jException {
     	Client client;
    		client = Activator.getDefaultClientCurator().getDefaultClient();
    		return client.getXmppItem(jid, node);
     }
     public IService getService(String service_jid) throws Xws4jException {
     	Client client;
                 client = Activator.getDefaultClientCurator().getDefaultClient();
                 return client.getService(service_jid);
     }
     public IFunction getFunction(String service_jid, String function_name) throws Xws4jException {
     	Client client;
                 client = Activator.getDefaultClientCurator().getDefaultClient();
                 return client.getFunction(service_jid, function_name);
     }
     public IIoFactory getIoFactory(IoSchemata ioschemata) throws XwsBindingException {
     	IIoFactory iofactory =
     		BindingManager.getIoFactory(ioschemata, Activator.getDefaultBindingDefinitions());
     	Activator.updateProjectExplorer();
     	return iofactory;
     }
     
     public IIoFactory getIoFactory(IFunction function) throws XwsBindingException {
     	return BindingManager.getIoFactory(function, Activator.getDefaultBindingDefinitions());
     }
 
     /**
      * Checks if IoFactory exists for a function
      * @param function
      * @return true if exists, false otherwise
      */
     public boolean existsIoFactory(IFunction function){
     	try {
 			IIoFactory fac = BindingManager.getIoFactory(function, Activator.getDefaultBindingDefinitions());
 			if (fac==null) return false;
 		} catch (XwsBindingException e) {
 			return false;
 		}
     	return true;
     }
 
     public String toString(Element element) {
         return JavaDOMTools.w3cElementToString(element);
     }
 
 }
