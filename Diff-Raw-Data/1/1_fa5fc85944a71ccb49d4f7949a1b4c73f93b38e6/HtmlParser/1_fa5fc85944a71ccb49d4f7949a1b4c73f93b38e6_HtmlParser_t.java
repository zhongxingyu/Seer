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
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.pool.ObjectPool;
 import org.apache.commons.pool.PoolableObjectFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.lexer.InputStreamSource;
 import org.htmlparser.lexer.Lexer;
 import org.htmlparser.lexer.Source;
 import org.htmlparser.scanners.ScriptScanner;
 import org.htmlparser.util.NodeIterator;
 import org.htmlparser.visitors.NodeVisitor;
 import org.microformats.hCard.HCard;
 import org.microformats.hCard.HCardParser.HCardVisitor;
 import org.osgi.service.component.ComponentContext;
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.parser.IParserContext;
 import org.paxle.parser.ISubParser;
 import org.paxle.parser.ParserContext;
 import org.paxle.parser.ParserException;
 import org.paxle.parser.html.IHtmlParser;
 
 /**
  * Parses (X)HTML-pages using the html parser from
  * <a href="http://htmlparser.sf.net">http://htmlparser.sf.net</a>.
  * <p>
  *  It uses a kind of iterator with callback to walk through the node-tree of
  *  the HTML page, extracting information whereever supported and putting it
  *  into the {@link IParserDocument}.
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
 public class HtmlParser implements IHtmlParser, ISubParser, PoolableObjectFactory {
 	
 	private final Log logger = LogFactory.getLog(HtmlParser.class);
 	
 	/*
 	public static void main(String[] args) {
 		final File dir = new File(args[0]);
 		final String[] files = dir.list();
 		for (int i=0; i<files.length; i++) try {
 			// IParserDocument doc = new CachedParserDocument(null);
 			final String url = "http://www.example.com/";
 			FixedPage page = new FixedPage(new InputStreamSource(new FileInputStream(new File(dir, files[i]))),
 					new ParserLogger(LogFactory.getLog(HtmlParser.class), URI.create(url)));
 			page.setUrl(url);
 			final Lexer lexer = new Lexer(page);
 			lexer.setNodeFactory(NodeCollector.NODE_FACTORY);
 			Parser parser = new Parser(lexer);
 			System.out.println("PARSING: " + parser.getURL());
 			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
 			System.out.println(files[i]);
 			NodeCollector nc = new NodeCollector(page.logger, page);
 			final IParserDocument pdoc = new CachedParserDocument(null);
 			nc.init(pdoc, null, false, false);
 			parser.visitAllNodesWith(nc);
 			page.close();
 			System.out.println("-------------------------------------------------------------------------------------------");
 			System.out.println();
 			System.out.println(pdoc.toString());
 		} catch (final Exception e) { e.printStackTrace(); }
 	}*/
 	
 	/**
 	 * An object pool containing {@link HtmlParserRequisites}
 	 */
 	private ObjectPool pool = null;
 	
 	protected void activate(@SuppressWarnings("unused") ComponentContext context) {
 		this.pool = new GenericObjectPool(this);
 		ScriptScanner.STRICT = false;
 	}
 
 	protected void deactivate(@SuppressWarnings("unused") ComponentContext context) throws Exception {
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
 			final IParserContext context = ParserContext.getCurrentContext();
 			
 			boolean obeyRobotsNoindex = true, obeyRobotsNofollow = true;
 			boolean useHcards = true;
 			final ICommandProfile cmdProfile = context.getCommandProfile();
 			if (cmdProfile != null) {
 				Serializable v;
 				if ((v = cmdProfile.getProperty(PROP_VALIDATE_META_ROBOTS_NOINDEX)) != null)
 					obeyRobotsNoindex = ((Boolean)v).booleanValue();
 				if ((v = cmdProfile.getProperty(PROP_VALIDATE_META_ROBOTS_NOFOLLOW)) != null)
 					obeyRobotsNofollow = ((Boolean)v).booleanValue();
 				if ((v = cmdProfile.getProperty(PROP_HCARD_ENABLE)) != null)
 					useHcards = ((Boolean)v).booleanValue();
 			}
 			
 			final IParserDocument doc = context.createDocument();	
 			final InputStreamSource iss = new InputStreamSource(is, charset);
 			
 			try {
 				req.parse(
 						location,
 						doc,
 						context.getReferenceNormalizer(),
 						context,
 						iss,
 						obeyRobotsNoindex, obeyRobotsNofollow,
 						useHcards);
 			} finally {
 				iss.destroy();			// performs the same operation as page.close()
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
 		HCardVisitor hc = null;
 		
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
 			// no need to reset the nc & hc, this is done prior to each parsing process by the parse()-method
 		}
 		
 		public void parse(
 				final URI location,
 				final IParserDocument doc,
 				final IReferenceNormalizer refNorm,
 				final IParserContext context,
 				final Source source,
 				final boolean obeyRobotsNoindex, final boolean obeyRobotsNofollow,
 				final boolean useHcards) throws org.htmlparser.util.ParserException, IOException {
 			logger.setLocation(location);
 			
 			page.init(source);
 			page.setUrl(location.toASCIIString());
 			
 			nc.init(doc, refNorm, obeyRobotsNoindex, obeyRobotsNofollow);
 			
 			if (useHcards) {
 				if (hc == null)
 					hc = new HCardVisitor();
 				hc.init(location);
 				
 				// we cannot reposition the source-stream to the beginning, so we have to run the visitors simultanously
 				visitAllNodesWithAll(nc, hc);
 				
 				// extract hcard-information into pdoc
 				extractHcardInfos(doc, context);
 			} else {
 				parser.visitAllNodesWith(nc);
 			}
 		}
 		
 		private void extractHcardInfos(final IParserDocument doc, final IParserContext context) throws IOException {
 			/* TODO:
 			 *  - junit test case */
 			final List<HCard> parsedHCards = hc.parsedHCards();
 			final String fmt = "#%0" + Integer.toString(parsedHCards.size()).length() + "d: %s";
 			final Iterator<HCard> it = parsedHCards.iterator();
 			int i = 0;
 			while (it.hasNext()) {
 				final HCard hcard = it.next();
 				final IParserDocument subdoc = context.createDocument();
				subdoc.setMimeType("text/x-vcard");
 				
 				/* Since the HCard and all of it's content is immutable, we need to seperate the
 				 * information ourselves here instead of simply clearing the lists containing the URLs. */
 				
 				// add all extracted links
 				for (final URI agentUri : hcard.agents) subdoc.addReference(agentUri, hcard.fn, "HCardParser");
 				for (final URI logoUri  : hcard.logos)  subdoc.addReferenceImage(logoUri, hcard.fn);
 				for (final URI photoUri : hcard.photos) subdoc.addReferenceImage(photoUri, hcard.fn);
 				for (final URI soundUri : hcard.sounds) subdoc.addReference(soundUri, hcard.fn, "HCardParser");
 				for (final URI uri      : hcard.urls)   subdoc.addReference(uri, hcard.fn, "HCardParser");
 				
 				// append all other information as text (copied from HCard.toString() and omitted the URL-fields)
 				if ( !hcard.n.isEmpty() )          subdoc.append("Name: ").append(hcard.n.toString()).append('\n');
 				if ( hcard.nicknames.size() > 0 )  subdoc.append("Nickname: ").append(printCommaList(hcard.nicknames)).append('\n');
 				if ( hcard.bday != null )          subdoc.append("Birthday: ").append(printDate(hcard.bday.longValue())).append('\n');
 				if ( hcard.tels.size() > 0 )       subdoc.append("TelNr: ").append(printCommaList(hcard.tels)).append('\n');
 				if ( hcard.emails.size() > 0 )     subdoc.append("Email: ").append(printCommaList(hcard.emails)).append('\n');
 				if ( hcard.geo != null )           subdoc.append("Geolocation: ").append(hcard.geo.toString()).append('\n');
 				if ( hcard.tz != null )            subdoc.append("Timezone: ").append(hcard.tz.toString()).append('\n');
 				if ( hcard.adrs.size() > 0 )       subdoc.append("Address:\n").append(printBlockList(hcard.adrs));
 				if ( hcard.labels.size() > 0 )     subdoc.append("Label:\n").append(printBlockList(hcard.labels));
 				if ( hcard.mailers.size() > 0 )    subdoc.append("Mailer:").append(printLineList(hcard.mailers)).append('\n');
 				if ( hcard.titles.size() > 0 )     subdoc.append("Title:").append(printCommaList(hcard.titles)).append('\n');
 				if ( hcard.orgs.size() > 0 )       subdoc.append("Organization: ").append(printLineList(hcard.orgs)).append('\n');
 				if ( hcard.roles.size() > 0 )      subdoc.append("Roles: ").append(printCommaList(hcard.roles)).append('\n');
 				if ( hcard.categories.size() > 0 ) subdoc.append("Category: ").append(printCommaList(hcard.categories)).append('\n');
 				if ( hcard.keys.size() > 0 )       subdoc.append("Key: ").append(printBlockList(hcard.keys));
 				if ( hcard.notes.size() > 0 )      subdoc.append("Note: ").append(printBlockList(hcard.notes));
 				if ( hcard.rev != null )           subdoc.append("Rev: ").append(printDate(hcard.rev.longValue())).append('\n');
 				if ( hcard.sortString != null )    subdoc.append("SortString: ").append(hcard.sortString).append('\n');
 				if ( hcard.uid != null )           subdoc.append("UID: " ).append(hcard.uid).append('\n');
 				
 				subdoc.setAuthor(hcard.fn);
 				subdoc.setTitle("hCard for " + hcard.fn);
 				doc.addSubDocument(String.format(fmt, Integer.valueOf(i++), hcard.fn), subdoc);
 				
 				it.remove();
 			}
 		}
 		
 		private void visitAllNodesWithAll(final NodeVisitor... visitors) throws org.htmlparser.util.ParserException {
 	        Node node;
 	        for (final NodeVisitor visitor : visitors)
 	        	visitor.beginParsing();
 	        for (NodeIterator e = parser.elements(); e.hasMoreNodes(); )
 	        {
 	            node = e.nextNode();
 	            for (final NodeVisitor visitor : visitors)
 	            	node.accept(visitor);
 	        }
 	        for (final NodeVisitor visitor : visitors)
 		        visitor.finishedParsing();
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
 	
 	/* The following 4 print*()-methods are copied from org.microformats.hcard.HCard */
 	
 	private static String printDate(long millis) {
 		return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH).format(new Date(millis));
 	}
 	
 	private static String printLineList(List<?> list) {
 		if ( list.size() == 1 ) return list.get(0).toString();
 		
 		StringBuilder sb = new StringBuilder();
 		for ( Object o : list ) sb.append("\n    ").append(o);
 		return sb.toString();
 	}
 	
 	private static String printBlockList(List<?> list) {
 		boolean first = true;
 		StringBuilder sb = new StringBuilder();
 		for ( Object o : list ) {
 			if ( first ) first = false;
 			else sb.append(" ---- \n");
 			String item = o.toString();
 			if ( item.endsWith("\n") ) item = item.substring(0, item.length() -1);
 			sb.append("    ");
 			sb.append(item.replaceAll("\n", "\n    "));
 			sb.append('\n');
 		}
 		
 		return sb.toString();
 	}
 	
 	private static String printCommaList(List<?> list) {
 		boolean first = true;
 		StringBuilder sb = new StringBuilder();
 		for ( Object o : list ) {
 			if ( first ) first = false;
 			else sb.append(", ");
 			sb.append(o);
 		}
 		
 		return sb.toString();
 	}
 }
