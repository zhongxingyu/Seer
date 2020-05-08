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
 package org.paxle.parser.html.impl;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.htmlparser.Parser;
 import org.htmlparser.lexer.InputStreamSource;
 import org.htmlparser.lexer.Lexer;
 import org.htmlparser.lexer.Source;
import org.htmlparser.scanners.ScriptScanner;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.parser.CachedParserDocument;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 
 /**
  * Parses (X)HTML-pages using the html parser from
  * <a href="http://htmlparser.sf.net">http://htmlparser.sf.net</a>.
  * <p>
  *  It uses a kind of iterator with callback to walk through the node-tree of
  *  the HTML page, extracting information whereever supported and putting it
  *  into the {@link CachedParserDocument}.
  * </p>
  * @see org.htmlparser.Parser#visitAllNodesWith(org.htmlparser.visitors.NodeVisitor) for the iterator
  * @see org.paxle.parser.html.impl.NodeCollector for the callback
  * 
  * @scr.component
  * @scr.service interface="org.paxle.parser.ISubParser"
  * @scr.property name="MimeTypes" private="true" 
  * 				 values.1="text/html"
  * 				 values.2="application/xhtml+xml"
  * 			     values.3="application/xml"
  * 				 values.4="text/xml"
  * 				 values.4="text/sgml"
  */
 public class HtmlParser implements ISubParser, PoolableObjectFactory {
 	
 	private static final String PROP_VALIDATE_META_ROBOTS_NOINDEX = HtmlParser.class.getName() + ".validateMetaRobots.noindex";
 	private static final String PROP_VALIDATE_META_ROBOTS_NOFOLLOW = HtmlParser.class.getName() + ".validateMetaRobots.nofollow";
 	
 	private final Log logger = LogFactory.getLog(HtmlParser.class);
 	
 	/*
 	public static void main(String[] args) {
 		final File dir = new File(args[0]);
 		final String[] files = dir.list();
 		for (int i=0; i<files.length; i++) try {
 			ParserDocument doc = new ParserDocument();
 			Page page = new PPage(new InputStreamSource(new FileInputStream(new File(dir, files[i]))));
 			page.setUrl("http://www.example.com/");
 			Parser parser = new Parser(new Lexer(page));
 			System.out.println("PARSING: " + parser.getURL());
 			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
 			NodeCollector nc = new NodeCollector(doc);
 			System.out.println(files[i]);
 			parser.visitAllNodesWith(nc);
 			page.close();
 			System.out.println("-------------------------------------------------------------------------------------------");
 			System.out.println();
 			System.out.println(doc.toString());
 		} catch (final Exception e) { e.printStackTrace(); }
 	}*/
 	
 	/**
 	 * An object pool containing {@link HtmlParserRequisites}
 	 */
 	private ObjectPool pool = null;
 	
 	protected void activate(ComponentContext context) {
 		this.pool = new GenericObjectPool(this);
		ScriptScanner.STRICT = false;
 	}
 
 	protected void deactivate(ComponentContext context) throws Exception {
 		this.pool.close();
 		this.pool = null;
 	}
 	
 	/**
 	 * @see PoolableObjectFactory#activateObject(Object)
 	 */
 	public void activateObject(Object arg0) throws Exception {
 		((HtmlParserRequisites)arg0).reset();
 	}
 	
 	/**
 	 * @see PoolableObjectFactory#destroyObject(Object)
 	 */
 	public void destroyObject(Object arg0) throws Exception {
 		// nothing to do
 	}
 	
 	/**
 	 * @see PoolableObjectFactory#makeObject()
 	 */
 	public Object makeObject() throws Exception {
 		return new HtmlParserRequisites();
 	}
 	
 	/**
 	 * @see PoolableObjectFactory#passivateObject(Object)
 	 */
 	public void passivateObject(Object arg0) throws Exception {
 		// nothing to do
 	}
 	
 	/**
 	 * @see PoolableObjectFactory#validateObject(Object)
 	 */
 	public boolean validateObject(Object arg0) {
 		// don't know how
 		return true;
 	}
 
 	/**
 	 * @see ISubParser#parse(URI, String, InputStream)
 	 */
 	public IParserDocument parse(URI location, String charset, InputStream is)
 			throws ParserException, UnsupportedEncodingException, IOException 
 	{
 		HtmlParserRequisites req = null;
 		try {
 			// testing if we support the charset. if not we try to use UTF-8
 			boolean unsupportedCharset = false;
 			try {
 				if (charset != null && !Charset.isSupported(charset)) {
 					unsupportedCharset = true;
 				}
 			} catch (IllegalCharsetNameException e) {
 				unsupportedCharset = true;
 			}
 			
 			if (unsupportedCharset) {
 				this.logger.warn(String.format(
 						"The resource '%s' has an unsupported charset '%s'. We try to use UTF-8 instead.", 
 						location,
 						charset
 				));
 				charset = "UTF-8";
 			}
 			
 			// getting parser-requisites from object pool 
 			req = (HtmlParserRequisites) this.pool.borrowObject();
 			
 			// parsing content
 			final ParserContext context = ParserContext.getCurrentContext();
 			final IParserDocument doc = new CachedParserDocument(context.getTempFileManager());			
 			final InputStreamSource iss = new InputStreamSource(is, charset);
 			
 			boolean obeyRobotsNoindex = true, obeyRobotsNofollow = true;
 			final ICommandProfile cmdProfile = context.getCommandProfile();
 			if (cmdProfile != null) {
 				final Serializable robNoindex = cmdProfile.getProperty(PROP_VALIDATE_META_ROBOTS_NOINDEX);
 				final Serializable robNofollow = cmdProfile.getProperty(PROP_VALIDATE_META_ROBOTS_NOFOLLOW);
 				if (robNoindex != null)
 					obeyRobotsNoindex = ((Boolean)robNoindex).booleanValue();
 				if (robNofollow != null)
 					obeyRobotsNofollow = ((Boolean)robNofollow).booleanValue();
 			}
 			
 			try {
 				req.parse(location, doc, context.getReferenceNormalizer(), iss, obeyRobotsNoindex, obeyRobotsNofollow);
 				/*
 				final FixedPage page = new FixedPage(iss);
 				final ParserLogger pl = new ParserLogger(logger, location);
 				final Parser parser = new Parser(new Lexer(page), pl);
 				
 				final NodeCollector nc = new NodeCollector(doc, pl, page, context.getReferenceNormalizer());
 				parser.visitAllNodesWith(nc);
 				page.close();
 				*/
 			} finally {
 				iss.destroy();
 			}
 			
 			// return parser-requisites into pool
 			this.pool.returnObject(req);
 			req = null;
 			
 			// set document charset if actual charset is null (i.e. not set yet)
 			if (charset != null && doc.getCharset() == null)
 				doc.setCharset(Charset.forName(charset));
 			
 			// set document status
 			doc.setStatus(IParserDocument.Status.OK);
 			return doc;
 		} catch (Throwable e) {
 			if (req != null) {
 				try {
 					this.pool.invalidateObject(req);
 				} catch (Exception e1) {
 					this.logger.error(String.format(
 							"Unexpected '%s' while trying to invalidate parser-requisites",
 							e1.getClass().getName()
 					),e1);
 				}
 			}
 			
 			if (e instanceof org.htmlparser.util.ParserException) {
 				throw new ParserException("error parsing HTML nodes-tree", e);
 			}
 			throw new ParserException("internal error: " + e.getMessage(), e);
 		}
 	}
 	
 	private final class HtmlParserRequisites {
 		final ParserLogger logger = new ParserLogger(HtmlParser.this.logger);
 		final FixedPage page = new FixedPage(logger);
 		final Lexer lexer = new Lexer(page);
 		final Parser parser = new Parser(lexer, logger);
 		final NodeCollector nc = new NodeCollector(logger, page);
 		
 		public HtmlParserRequisites() {
 	        lexer.setNodeFactory(NodeCollector.NODE_FACTORY);
 		}
 		
 		public void reset() {
 			// no need to reset the logger
 			lexer.getCursor().setPosition(0);
 			/* the lexer does not have to be reset, it would only reset
 			 *  - the page, which is done prior to each parsing process by the parse()-method
 			 *  - the cursor, which is done above
 			 */
 			// no need to reset the parser, it would only reset the lexer - see above
 			// no need to reset the nc, this is done prior to each parsing process by the parse()-method
 		}
 		
 		public void parse(
 				final URI location,
 				final IParserDocument doc,
 				final IReferenceNormalizer refNorm,
 				final Source source,
 				boolean obeyRobotsNoindex,
 				boolean obeyRobotsNofollow) throws org.htmlparser.util.ParserException {
 			logger.setLocation(location);
 			page.init(source);
 			page.setUrl(location.toASCIIString());
 			nc.init(doc, refNorm, obeyRobotsNoindex, obeyRobotsNofollow);
 			parser.visitAllNodesWith(nc);
 		}
 	}
 	
 	/**
 	 * TODO: the html parser does not seem to extract the keywords
 	 */
 	public IParserDocument parse(URI location, String charset, File content) throws ParserException,
 			UnsupportedEncodingException, IOException {
 		final FileInputStream fis = new FileInputStream(content);
 		try {
 			return parse(location, charset, fis);
 		} finally {
 			fis.close();
 		}
 	}
 }
