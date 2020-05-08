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
 package org.paxle.parser.impl;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.osgi.framework.internal.core.FilterImpl;
 import org.jmock.Expectations;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Filter;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.doc.IDocumentFactory;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.impl.BasicDocumentFactory;
 import org.paxle.core.io.temp.ITempFileManager;
 import org.paxle.parser.IParserContext;
 
 public class ParserContextLocaleTest extends MockObjectTestCase {
 	protected IParserContext parserContext;
 	
 	protected ComponentContext componentContext;
 	protected BundleContext bc;
 	
 	protected ServiceReference parserDocFactoryRef;
 	protected Dictionary<String, Object> parserDocFactoryProps;
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		// creating dummy bundle- and component-contexts
 		this.bc = mock(BundleContext.class);
 		this.componentContext = mock(ComponentContext.class);
 		
 		// creating a dummy service-registration for the doc-factory
 		this.parserDocFactoryRef = mock(ServiceReference.class);		
 		this.parserDocFactoryProps = new Hashtable<String, Object>();
 		this.parserDocFactoryProps.put(IDocumentFactory.DOCUMENT_TYPE, new String[]{IParserDocument.class.getName()});
 		
 		final Filter classFilter = mock(Filter.class);
 		final ITempFileManager tempFileManager = mock(ITempFileManager.class);
 		
 		checking(new Expectations(){{
 			// allowing to fetch the bundle-context
 			allowing(componentContext).getBundleContext();
 			will(returnValue(bc));
 			
 			// allowing to locate the doc-factory
 			allowing(componentContext).locateService("docFactory", parserDocFactoryRef);
 			will(returnValue(new BasicDocumentFactory(tempFileManager)));
 			
 			// allowing to create the filter
 			allowing(bc).createFilter(with(any(String.class)));
 			will(returnValue(classFilter));
 			
 			// mathing the filter against the doc-factory service-registration properties
 			allowing(classFilter).match(parserDocFactoryRef);
 			will(returnValue(
					new FilterImpl(String.format("(%s=%s)",IDocumentFactory.DOCUMENT_TYPE,IParserDocument.class.getName()))
 					.match(parserDocFactoryProps))
 			);
 			
 			allowing(tempFileManager).createTempFile(); 
 		}});
 		
 		// creating the crawler-context
 		this.parserContext = new TestParserContextLocale(this.componentContext, this.parserDocFactoryRef).createContext();		
 	}
 	
 	public void testCreateDocument() throws IOException {
 		IParserDocument cdoc = this.parserContext.createDocument();
 		assertNotNull(cdoc);
 	}
 	
 	public void testCreateDocumentForType() throws InvalidSyntaxException, IOException {
 		IParserDocument cdoc = this.parserContext.createDocument(IParserDocument.class, null);
 		assertNotNull(cdoc);
 	}
 	
 	private static class TestParserContextLocale extends ParserContextLocal {
 		public TestParserContextLocale(ComponentContext ctx, ServiceReference factoryRef) {			
 			this.docFactoryRefs.add(factoryRef);
 			this.ctx = ctx;
 		}
 		
 		public IParserContext createContext() {
 			return this.initialValue();
 		}
 	}
 }
