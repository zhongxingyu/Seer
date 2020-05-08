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
 package org.paxle.se.index.lucene.impl;
 
 import java.io.Reader;
 import java.io.StringReader;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URI;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.util.tracker.ServiceTracker;
 import org.paxle.core.IMWComponent;
 import org.paxle.core.doc.IIndexerDocument;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.doc.IParserDocument.Status;
 import org.paxle.core.io.IOTools;
 import org.paxle.core.queue.Command;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommand.Result;
 
 public class SnippetFetcher {
 	/**
 	 * Thread pool service
 	 */
 	private ExecutorService execService;
 	
 	/**
 	 * For logging
 	 */
 	protected Log logger = LogFactory.getLog(this.getClass());
 	
 	/**
 	 * @scr.reference target="(component.ID=org.paxle.crawler)" 
 	 */
 	protected ServiceTracker crawler;
 	
 	/**
 	 * @scr.reference target="(component.ID=org.paxle.parser)"
 	 */
 	protected ServiceTracker parser;
 	
 	protected PaxleAnalyzer analyzer;
 	
 	public SnippetFetcher(BundleContext ctx, PaxleAnalyzer analyzer) throws InvalidSyntaxException {
 		this.crawler = new ServiceTracker(ctx, ctx.createFilter("(&(objectClass=org.paxle.core.IMWComponent)(component.ID=org.paxle.crawler))"),null);
 		this.crawler.open();
 		this.parser = new ServiceTracker(ctx, ctx.createFilter("(&(objectClass=org.paxle.core.IMWComponent)(component.ID=org.paxle.parser))"),null);
 		this.parser.open();		
 		this.analyzer = analyzer;
 		this.execService = Executors.newCachedThreadPool();
 	}
 	
 	public void close() {
 		this.crawler.close();
 		this.parser.close();
 	}
 		
 	public String getSnippet(Query query, String locationStr) {
 		try {
 			// creating a dummy command
 			URI locationURI = URI.create(locationStr);
 			ICommand cmd = Command.createCommand(locationURI);
 			
 			// getting the crawler
 			@SuppressWarnings("unchecked")
 			IMWComponent<ICommand> crawlerComp = (IMWComponent<ICommand>) crawler.getService();
 			if (crawlerComp == null) return null;
 			
 			// getting the parser
 			@SuppressWarnings("unchecked")
 			IMWComponent<ICommand> parserComp = (IMWComponent<ICommand>) parser.getService();
 			if (parserComp == null) return null;
 		
 			// crawling the resource
 			crawlerComp.process(cmd);
 			if (cmd.getResult() != Result.Passed) return null;
 			
 			// parsing the resource
 			parserComp.process(cmd);
 			if (cmd.getResult() != Result.Passed) return null;
 			
 			// trying to get the parsed content
 			IParserDocument pdoc = cmd.getParserDocument();
 			if (pdoc == null) return null;
 			else if (pdoc.getStatus() != Status.OK) return null;
 			
 			// getting the document content
 			Reader content = pdoc.getTextAsReader();
 			if (content == null) return null;			
 			
 			// reading some text
 			StringBuilder text = new StringBuilder();
 			IOTools.copy(content, text, 10240);
 			
 	        final Highlighter highlighter = new Highlighter(new QueryScorer(query));
 			final TokenStream tokenStream = this.analyzer.tokenStream("content", new StringReader(text.toString()));
 			final String result = highlighter.getBestFragments(tokenStream, text.toString(), 3, "...");
 						
 			return result;
 		} catch (Throwable e) {
			this.logger.error(e.getMessage(), e);
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Method to generate a dynamic proxy around an {@link IIndexerDocument}
 	 * @param idoc the {@link IIndexerDocument} to wrap
 	 * @param query the query as entered by the user
 	 * @param deadline a point in time when the snippet generation should have finished
 	 * @return a wrapped {@link IIndexerDocument}
 	 */
 	public IIndexerDocument createProxy(IIndexerDocument idoc, Query query,long deadline) {
 		IIndexerDocument idocProxy = (IIndexerDocument) Proxy.newProxyInstance(
 				IIndexerDocument.class.getClassLoader(),
                 new Class[] { IIndexerDocument.class },
                 new SnippetFetchingWrapper(idoc, query, deadline)
 		);
 		return idocProxy;
 	}
 	
 	/**
 	 * This class is a dynamic wrapper around an {@link IIndexerDocument}, intercepts
 	 * method calls to {@link IIndexerDocument#get(org.paxle.core.doc.Field)} and 
 	 * injects snippets fetched asynchronous by the {@link ExecutorService}
 	 */
 	private class SnippetFetchingWrapper implements InvocationHandler, Callable<String> {
 		/**
 		 * The {@link IIndexerDocument indexer-document} we need to generate a snippet for
 		 */
 		private IIndexerDocument idoc;
 		
 		/**
 		 * The query as entered by the user
 		 */
 		private Query query;
 		
 		/**
 		 * An object to determine the snippet-generation status
 		 */
 		private Future<String> pendingTask;
 		
 		/**
 		 * The time when snippet-generation should have been finished
 		 */
 		private long deadline;
 		
 		/**
 		 * A flag specifying if the caller has already tried to get the snippet via a function
 		 * call to {@link IIndexerDocument#get(org.paxle.core.doc.Field)}.
 		 */
 		private boolean fetched;
 		
 		public SnippetFetchingWrapper(IIndexerDocument idoc, Query query, long deadline) {
 			this.idoc = idoc;
 			this.query = query;
 			this.deadline = deadline;
 			
 			// starting an async task for snippet fetching
 			this.pendingTask = execService.submit(this);
 		}
 		
 		/**
 		 * This method is used to intercept function calls to {@link IIndexerDocument#get(org.paxle.core.doc.Field)} if 
 		 * {@link IIndexerDocument#SNIPPET} is used as argument, and to take a look if the asnychronous snippet-fetching-task
 		 * has finished and returned a result.
 		 */
 		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 			if (method.getName().equals("get") && args != null && args.length > 0 && IIndexerDocument.SNIPPET.equals(args[0])) {
 				this.fetched = true;
 				if (this.idoc.get(IIndexerDocument.SNIPPET) == null) {
 					/* 
 					 * if the task has not finished yet we'll wait some time to receive the result
 					 */
 					if (!this.pendingTask.isDone()) {
 						try {
 							long timeToWait = this.deadline - System.currentTimeMillis(); 
 							if (timeToWait > 100) {
 								String result = this.pendingTask.get(timeToWait, TimeUnit.MILLISECONDS);
 								if (result != null) return result;
 							}
 						} catch (Exception e) {
 							// ignore this
 						}
 					}
 				}
 			}
 			
 			return method.invoke(this.idoc, args);
 		}
 
 		/**
 		 * This method is called asynchronous by an {@link ExecutorService} to generate
 		 * a snippet for a found {@link IIndexerDocument} 
 		 */
 		public String call() throws Exception {
 			// getting the URI of the document
 			String locationStr = this.idoc.get(IIndexerDocument.LOCATION);
 			
 			// generating the snippet
 			String snippet = getSnippet(query, locationStr);
 			
 			// if a snippet was generated successfully we store it into the
 			// IIndexerDocument now
 			if (snippet != null) {
 				this.idoc.set(IIndexerDocument.SNIPPET, snippet);
 				if (this.fetched) {
 					/* The caller already has tried to fetch the snippet.
 					 * 
 					 * TODO: we could insert the generated snippet into a cache here 
 					 * so that it can be fetched asynchronous, e.g. by an ajax task
 					 */					
 				}
 			}
 			return snippet;
 		}
 		
 	}
 }
