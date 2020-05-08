 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.console.impl;
 
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
 import org.eclipse.osgi.framework.console.CommandInterpreter;
 import org.eclipse.osgi.framework.console.CommandProvider;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.IMWComponent;
 
@Component
@Service(CommandProvider.class)
 public class PaxleCommandProvider implements CommandProvider {
 
 	private static enum COMPONENT {
 		Crawler("org.paxle.crawler"),
 		Parser("org.paxle.parser"),
 		Indexer("org.paxle.indexer");
 		
 		public String id;
 		private COMPONENT(String id) {
 			this.id = id;
 		};
 	}
 	
 	private BundleContext context;
 	
 	protected void activate(ComponentContext context) {
 		this.context = context.getBundleContext();
 	}
 
 	private IMWComponent getMWComponent(COMPONENT component) throws InvalidSyntaxException {
 		ServiceReference[] crawlerRef = this.context.getServiceReferences(
 				IMWComponent.class.getName(), 
 				String.format("(%s=%s)", IMWComponent.COMPONENT_ID,component.id)
 		);
 		if (crawlerRef == null) return null;
 		else if (crawlerRef.length > 1) {
 			throw new IllegalStateException(String.format("More than one '%s' component found.", component.toString()));
 		}
 				
 		return (IMWComponent) this.context.getService(crawlerRef[0]);
 	}
 	
 	public void _crawler(CommandInterpreter ci) throws InvalidSyntaxException {		
 		this.mwComponentCommands(COMPONENT.Crawler, ci, ci.nextArgument());
 	}
 		
 	public void _parser(CommandInterpreter ci) throws InvalidSyntaxException {		
 		this.mwComponentCommands(COMPONENT.Parser, ci, ci.nextArgument());
 	}	
 	
 	public void _indexer(CommandInterpreter ci) throws InvalidSyntaxException {		
 		this.mwComponentCommands(COMPONENT.Indexer, ci, ci.nextArgument());
 	}
 	
 	public void _components(CommandInterpreter ci) throws InvalidSyntaxException {		
 		this.mwComponentCommands(COMPONENT.values(), ci, ci.nextArgument());
 	}
 	
 	private void mwComponentCommands(COMPONENT componentName, CommandInterpreter ci, String arg) throws InvalidSyntaxException {
 		this.mwComponentCommands(new COMPONENT[]{componentName}, ci, arg);
 	}
 	
 	private void mwComponentCommands(COMPONENT[] componentNames, CommandInterpreter ci, String arg) throws InvalidSyntaxException {
 		if (arg == null) {
 			ci.println("No argument found");
 			return;
 		}
 		
 		StringBuilder buf = new StringBuilder();
 		if (arg.equals("overview")) {
 			buf.append("Component: | Active: | Enqueued:\r\n");
 		}
 		
 		for (COMPONENT componentName : componentNames) {
 			IMWComponent<?> component = this.getMWComponent(componentName);
 			if (component == null) {
 				ci.println(String.format("No %s found", componentName.name()));
 				return;
 			}
 
 			if (arg.equals("status")) {
 				buf.append(String.format("%s status:\r\n", componentName.name()))
 				   .append(String.format(" Activity = %s\r\n",(component.isPaused())?"paused":"running"))
 				   .append(String.format(" PPM = %d\r\n",component.getPPM()))
 				   .append(String.format(" Active Jobs = %d\r\n",component.getActiveJobCount()))
 				   .append(String.format(" Enqueued Jobs = %d\r\n",component.getEnqueuedJobCount()))
 				   .append("\r\n");				
 			} else if (arg.equals("pause")) {
 				component.pause();
 				buf.append(String.format("%s paused.\r\n", componentName.name()));
 			} else if (arg.equals("resume")) {
 				component.resume();
 				buf.append(String.format("%s resumed.\r\n", componentName.name()));
 			} else if (arg.equals("overview")) {
 				buf.append(String.format(
 						   "%10s | %7d | %7d\r\n",
 						   componentName.name(),
 						   component.getActiveJobCount(),
 						   component.getEnqueuedJobCount()
 				   ));
 			}
 		}
 		
 		ci.println(buf.toString());
 	}
 	
 	public String getHelp() {
 		StringBuilder buf = new StringBuilder();
 		
 		buf.append("Paxle commands:\r\n")
 		   .append("---Controlling the Crawler/Parser/Indexer---\r\n")
 		   .append("\tcrawler|parser|indexer|components - Paxle component related commands\r\n")
 		   .append("\tstatus - print current status\r\n")
 		   .append("\toverview - display the amount of active and enqueued jobs\r\n")
 		   .append("\tpause - pause component\r\n")
 		   .append("\tresume - resume component\r\n");
 		
 		return buf.toString();
 	}
 
 }
