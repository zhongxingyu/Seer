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
 package org.paxle.tools.dns.impl;
 
 import java.net.URI;
 
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.doc.ICommand;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.LinkInfo;
 import org.paxle.core.doc.impl.BasicCommand;
 import org.paxle.core.doc.impl.BasicParserDocument;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.tools.dns.IAddressTool;
 
 public class DNSFilterOnlineTest extends MockObjectTestCase {
 	private IFilter<ICommand> filter;
 	
 	@Override
 	protected void setUp() throws Exception {	
 		super.setUp();

		// init dns-tool
		final IAddressTool dnsTool = new AddressTool() {{
			this.activate(mock(ComponentContext.class, "dnsToolContext"));
		}};
 		
		// init dns-filter
 		this.filter = new DNSFilter() {{
			this.dns = dnsTool;
			this.activate(mock(ComponentContext.class, "dnsFilterContext"));
 		}};
 	}
 	
 	private ICommand createCommand(URI location) {
 		ICommand cmd = new BasicCommand();
 		cmd.setLocation(location);
 		return cmd;
 	}
 	
 	public void testFilterKnownHost() {
 		final IFilterContext context = mock(IFilterContext.class);
 		final ICommand cmd = this.createCommand(URI.create("http://svn.paxle.net"));
 		
 		this.filter.filter(cmd, context);
 		assertEquals(ICommand.Result.Passed, cmd.getResult());
 	}
 	
 	public void testFilterUnKnownHost() {
 		final IFilterContext context = mock(IFilterContext.class);
 		final ICommand cmd = this.createCommand(URI.create("http://xyz.paxle.net"));
 		
 		this.filter.filter(cmd, context);
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}
 	
 	public void testFilterMultipleHosts() {
 		final IFilterContext context = mock(IFilterContext.class);
 		
 		final URI knownDomain = URI.create("http://paxle.org");
 		final URI unknownDomain = URI.create("http://xxxpaxle.net");
 		
 		final IParserDocument pDoc = new BasicParserDocument(mock(ITempFileManager.class));
 		pDoc.addReference(knownDomain, new LinkInfo());
 		pDoc.addReference(unknownDomain, new LinkInfo());
 		
 		final ICommand cmd = this.createCommand(URI.create("http://svn.paxle.net"));
 		cmd.setParserDocument(pDoc);
 		
 		this.filter.filter(cmd, context);
 		assertEquals(ICommand.Result.Passed, cmd.getResult());
 		assertEquals(LinkInfo.Status.OK, pDoc.getLinks().get(knownDomain).getStatus());
 		assertEquals(LinkInfo.Status.FILTERED, pDoc.getLinks().get(unknownDomain).getStatus());
 	}
 	
 	public void testFilterInvalidHost() {
 		final IFilterContext context = mock(IFilterContext.class);		
 		final URI invalidDomain = URI.create("http://www.xyz.net%20target=/");
 		final ICommand cmd = this.createCommand(invalidDomain);
 		
 		assertEquals(ICommand.Result.Passed, cmd.getResult());
 		this.filter.filter(cmd, context);
 		assertEquals(ICommand.Result.Rejected, cmd.getResult());
 	}
 }
