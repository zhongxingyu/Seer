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
 package org.paxle.filter.robots.impl;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.paxle.core.doc.ICommand;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.doc.LinkInfo.Status;
 import org.paxle.core.filter.FilterQueuePosition;
 import org.paxle.core.filter.FilterTarget;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.filter.robots.IRobotsTxtManager;
 
 @Component(immediate=true, metatype=false)
 @Service(IFilter.class)
 @Properties({
 	@Property(name="org.paxle.metadata",boolValue=true),
 	@Property(name="org.paxle.metadata.localization",value="/OSGI-INF/l10n/RobotsTxtFilter")
 })
 @FilterTarget({
 	@FilterQueuePosition(queue="org.paxle.crawler.in"),
 	@FilterQueuePosition(queue="org.paxle.parser.out",position=70)
 })
 public class RobotsTxtFilter implements IFilter<ICommand> {
 	
 	/**
 	 * Class to count rejected URI
 	 */
 	static class Counter {
 		/**
 		 * Number of rejected {@link URI}.
 		 */
 		public int c = 0;
 		
 		/**
 		 * Total number of checked {@link URI}.
 		 */
 		public int t = 0;
 	}
 	
 	/**
 	 * For logging
 	 */
 	private Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * A component to check URLs against robots.txt files.
 	 */
 	@Reference
 	protected IRobotsTxtManager robotsTxtManager;
 	
 	/**
 	 * @see IFilter#filter(ICommand)
 	 */
 	public void filter(ICommand command, IFilterContext filterContext) {		
 		// getting the location
 		URI location = command.getLocation();
 
 		try {			
 			final long start = System.currentTimeMillis();
 			
 			// test if the url is disallowed by robots.txt
 			if (this.robotsTxtManager.isDisallowed(location)) {
 				command.setResult(ICommand.Result.Rejected, "Access disallowed by robots.txt");
 				return;
 			}
 
 			// getting the parsed document
 			final Counter c = new Counter();
 			final IParserDocument parserDoc = command.getParserDocument();
 			
 			// checking URI against robots.txt
 			this.checkRobotsTxt(parserDoc, c);			
 			if (c.c > 0 || this.logger.isDebugEnabled()) {
 				logger.info(String.format(
 						"Blocking %d out of %d URIs from reference map(s) of '%s' in %d ms",
 						Integer.valueOf(c.c),
 						Integer.valueOf(c.t),
 						command.getLocation(),
 						Long.valueOf(System.currentTimeMillis() - start)
 				)); 
 			}
 		} catch (Exception e) {
 			this.logger.error(String.format(
 					"Unexpected %s while filtering command with location '%s'.",
 					e.getClass().getName(),
 					location
 			),e);
 		}
 	}
 	
 	private void checkRobotsTxt(IParserDocument parserDoc, final Counter c) {
 		if (parserDoc == null) return;
 		
 		// getting the link map
 		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
 		if (linkMap != null) {
 			this.checkRobotsTxt(linkMap, c);
 		}
 		
 		// loop through sub-parser-docs
 		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
 		if (subDocs != null) {
 			for (IParserDocument subDoc : subDocs.values()) {
 				this.checkRobotsTxt(subDoc, c);
 			}
 		}
 	}	
 	
 	void checkRobotsTxt(final Map<URI, LinkInfo> linkMap, final Counter c) {
 		if (linkMap == null || linkMap.size() == 0) return;
 		
 		// check for blocking URIs
 		final Collection<URI> uriToCheck = this.getOkURI(linkMap);
 		if (uriToCheck.size() == 0) return;
 		c.t += uriToCheck.size();
 		
 		final Collection<URI> disallowedURI = this.robotsTxtManager.isDisallowed(uriToCheck);
 		
 		// mark disallowed URI as blocked
 		if (disallowedURI != null && disallowedURI.size() > 0) {
 			StringBuffer debugMsg = new StringBuffer();
 			
 			for (URI location : disallowedURI) {
 				// getting the metadata of the disallowed URI
 				LinkInfo meta = linkMap.get(location);
 				if (!meta.hasStatus(Status.OK)) continue;
 				
 				// mark URI as filtered
 				meta.setStatus(Status.FILTERED, "Access disallowed by robots.txt");
 				c.c++;
 				if (logger.isDebugEnabled()) {
 					debugMsg.append(String.format("\t%s\r\n", location.toASCIIString()));
 				}
 			}
 			
 			if (logger.isDebugEnabled()) {
 				this.logger.debug(String.format(
 						"%d URI blocked:\r\n%s",
 						Integer.valueOf(disallowedURI.size()),
 						debugMsg.toString()
 				));
 			}
 		}	
 	}
 	
 	/**
 	 * This function returns all links with {@link LinkInfo.Status} <code>OK</code> from the delivered link-map.
 	 * 
 	 * @param linkMap the {@link IParserDocument#getLinks() link-map} of a {@link IParserDocument parser-document}
 	 * @return a collection containing all {@link URI} with status <code>OK</code>
 	 */
 	private Collection<URI> getOkURI(final Map<URI, LinkInfo> linkMap) {
 		if (linkMap == null) return Collections.emptyList();
 		
 		final ArrayList<URI> okLinks = new ArrayList<URI>();
 		for (Entry<URI, LinkInfo> link : linkMap.entrySet()) {
 			URI ref = link.getKey();
 			LinkInfo meta = link.getValue();
 			
 			if (meta.hasStatus(Status.OK)) {
 				okLinks.add(ref);
 			}
 		}
 		return okLinks;
 	}
 }
