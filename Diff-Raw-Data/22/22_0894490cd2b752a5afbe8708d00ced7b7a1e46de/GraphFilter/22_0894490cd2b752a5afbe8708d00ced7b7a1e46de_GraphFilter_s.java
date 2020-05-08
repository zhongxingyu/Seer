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
 
 
 package org.paxle.filter.webgraph.impl;
 
 import java.net.URI;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.collections.map.LRUMap;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.filter.FilterQueuePosition;
 import org.paxle.core.filter.FilterTarget;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 
 /**
  * This helper class determines the language of a document and inserts its finding into a parser-doc and all of its subdocs 
  * 
  * @scr.component name="org.paxle.filter.webgraph.impl.GraphFilter"
  * @scr.service interface="org.paxle.core.filter.IFilter"
  * @scr.property name="org.paxle.metadata" value="true" value="true" type="Boolean"
  * @scr.property name="org.paxle.metadata.localization" value="/OSGI-INF/l10n/GraphFilter"
  */
 @FilterTarget(@FilterQueuePosition(
 		queue = "org.paxle.parser.out", 
 		position = 0,
 		enabled = false
 ))
 public class GraphFilter implements IFilter<ICommand> {
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	private LRUMap domainRelations=new LRUMap(5000);
 	
 	public void filter(ICommand command, IFilterContext context) {
 		if (command == null) throw new NullPointerException("The command object is null.");
 		if (command.getResult() != ICommand.Result.Passed) return;
 		try {
 			// getting the domain name of the location
 			String domain1=command.getLocation().getHost();
 			if(domain1.startsWith("www.")) domain1=domain1.substring(4);
 			
 			// loop through all extracted links
 			Map<URI, LinkInfo> links=command.getParserDocument().getLinks();
 			if (links == null || links.size() == 0) return;
 			
 			// getting the domainmap for the current domain
 			Set<String> domains = this.getRelationsMap(domain1);
 			
 			Iterator<URI> it = links.keySet().iterator();			
 			while(it.hasNext()){
				String domain2 = it.next().getHost();
 				if(domain2.startsWith("www.")) domain2=domain2.substring(4);
 				if (!domain1.equalsIgnoreCase(domain2)) {
 					domains.add(domain2);
 				}
 			}			
 		} catch (Exception e) {
 			this.logger.error(String.format(
 					"Unexpected %s while processing command with URI '%s'.",
 					e.getClass().getName(),
 					command.getLocation().toASCIIString()
 			),e);
 		}			
 	}
 	
 	public LRUMap getRelations(){
 		return domainRelations;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public synchronized Set<String> getRelationsMap(String domainName) {
 		if (this.domainRelations.containsKey(domainName)) {
 			return (Set<String>) this.domainRelations.get(domainName);
 		} else {
 			// creating an new map
 			Set<String> domains=new HashSet<String>();
 			domains = (Set<String>) Collections.synchronizedSet(domains);
 			this.domainRelations.put(domainName, domains);
 			return domains;
 		}
 	}
 }
