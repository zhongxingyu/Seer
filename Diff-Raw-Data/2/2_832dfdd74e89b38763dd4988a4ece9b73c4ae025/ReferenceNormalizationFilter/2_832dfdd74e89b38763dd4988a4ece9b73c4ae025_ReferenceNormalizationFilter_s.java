 
 package org.paxle.core.filter.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.net.MalformedURLException;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.paxle.core.doc.IParserDocument;
 import org.paxle.core.filter.IFilter;
 import org.paxle.core.filter.IFilterContext;
 import org.paxle.core.queue.ICommand;
 
 public class ReferenceNormalizationFilter implements IFilter<ICommand> {
 	
 	private static final Charset UTF8 = Charset.forName("UTF-8");
 	
 	public static final Hashtable<String,Integer> DEFAULT_PORTS = new Hashtable<String,Integer>();
 	static {
 		DEFAULT_PORTS.put("http", Integer.valueOf(80));		// HTTP is not part of the global url-stream-handlers
 		DEFAULT_PORTS.put("https", Integer.valueOf(443));
 	}
 	
 	private final Log logger = LogFactory.getLog(ReferenceNormalizationFilter.class);
 	private final boolean sortQuery;
 	private final boolean appendSlash;
 	
 	public ReferenceNormalizationFilter() {
 		this(false, false);
 	}
 	
 	/**
 	 * Creates a new normalization filter for references.
 	 * @param sortQuery whether to sort the query parameters lexicographically by their respective keys
 	 * @param appendSlash whether a slash should be appended to the path if the last path element does not contain a dot
 	 * @see <a href="http://dblab.ssu.ac.kr/publication/LeKi05a.pdf">On URL Normalization</a>
 	 */
 	public ReferenceNormalizationFilter(final boolean sortQuery, final boolean appendSlash) {
 		this.sortQuery = sortQuery;
 		this.appendSlash = appendSlash;
 	}
 	
 	public void filter(ICommand command, IFilterContext filterContext) {
 		final IParserDocument pdoc = command.getParserDocument();
 		if (pdoc != null)
 			normalizeParserDoc(pdoc, new OwnURL());
 	}
 	
 	private void normalizeParserDoc(final IParserDocument pdoc, final OwnURL url) {
 		final Map<String,IParserDocument> subdocMap = pdoc.getSubDocs();
 		if (subdocMap != null && subdocMap.size() > 0)
 			for (final IParserDocument subdoc : subdocMap.values())
 				normalizeParserDoc(subdoc, url);
 		
 		final Map<String,String> linkMap = pdoc.getLinks();
 		if (linkMap == null || linkMap.size() == 0)
 			return;
 		final Iterator<Map.Entry<String,String>> it = linkMap.entrySet().iterator();
 		final Map<String,String> normalizedLinks = new HashMap<String,String>();
 		final Charset charset = (pdoc.getCharset() == null) ? UTF8 : pdoc.getCharset();		// UTF-8 is a recommended fallback but not standard yet
 		while (it.hasNext()) {
 			final Map.Entry<String,String> entry = it.next();
 			final String location = entry.getKey();
 			try {
 				final String normalized = url.parseBaseUrlString(location, charset);
 				if (normalized.equals(location))
 					continue;
 				
 				logger.debug("normalized reference " + location + " to " + normalized);
 				normalizedLinks.put(normalized, entry.getValue());
 			} catch (ParseException e) {
 				logger.info("error parsing reference: " + e.getMessage() + ", removing");
 			} catch (MalformedURLException e) {
 				logger.info("removing malformed reference " + location);
 			}
 			it.remove();
 		}
 		linkMap.putAll(normalizedLinks);
 	}
 	
 	private static final Pattern PATH_PATTERN = Pattern.compile("(/[^/]+(?<!/\\.{1,2})/)[.]{2}(?=/|$)|/\\.(?=/)|/(?=/)");
 	
 	/**
 	 * Resolves backpaths
 	 * @param path The path of an URL
 	 * @return The path without backpath directives
 	 */
 	private static String resolveBackpath(String path) {
 		
 		if (path == null || path.length() == 0) return "/";
 		if (path.length() == 0 || path.charAt(0) != '/') { path = "/" + path; }
 		
 		Matcher matcher = PATH_PATTERN.matcher(path);
 		while (matcher.find()) {
 			path = matcher.replaceAll("");
 			matcher.reset(path);
 		}
 		
 		return path.equals("")?"/":path;
 	}
 	
 	private static String urlDecode(final String str, final Charset charset) throws ParseException {
 		int percent = str.indexOf('%');
 		if (percent == -1)
 			return str;
 		
 		final StringBuffer sb = new StringBuffer(str.length());				// buffer to build the converted string
 		final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);	// buffer for conversion of contiguous %-encoded bytes
 		int last = 0;
 		final int len = str.length();
 		do {
 			sb.append(str.substring(last, percent));						// write non-encoded part
 			
 			/* loop to convert sequence of %-encoded tokens into bytes. Contiguous byte-sequences have to be dealt with
 			 * in one block before decoding, because - dependant on the charset - more than one byte may be needed to
 			 * represent a single character. If the conversion to bytes was done sequentially, decoding might fail */
 			do {
 				if (percent + 3 > str.length())
 					throw new ParseException("unexpected end of input", percent + 3);
 				final String token = str.substring(percent + 1, percent + 3);
 				if (!token.matches("[0-9a-fA-F]{2}"))
 					throw new ParseException("illegal url-encoded token '" + token + "'", percent);
 				
 				baos.write(Integer.parseInt(token, 16) & 0xFF);
 				percent += 3;
 			} while (percent < len && str.charAt(percent) == '%');
 			
 			sb.append(charset.decode(ByteBuffer.wrap(baos.toByteArray())));	// here the actual decoding takes place
 			baos.reset();													// reuse the ByteArrayOutputStream in the next run
 			
 			last = percent;													// byte after the token
 			percent = str.indexOf('%', last);								// search for next token, returns -1 if last > len
 		} while (percent != -1);
 		return sb.append(str.substring(last)).toString();
 	}
 	
 	public class OwnURL {
 		
 		/*
 		 * What happens here?
 		 * 
 		 * Every URL has to end with a slash, if it only consists of a scheme and authority.
 		 * 
 		 * This slash is a part of the path. Even a simple URL like "http://example.org/" is a mapping on a directory on the server.
 		 * As directories have to end with a slash, there _must_ be a slash if there is no path given ending with a filename.
 		 * 
 		 * In the next step we will remove default ports from the URL.
 		 * 
 		 * Then we convert the protocol identifier and the (sub-) domain to lowercase.
 		 * Case is not important for these parts, for the path it is.
 		 * 
 		 * Then we resolve backpaths in the path.
 		 * 
 		 * Later the resulting normalized URL is assembled, along this way possible fragments are removed, as they are simply not added
 		 * 
 		 */
 		
 		private String protocol;
 		private String username;
 		private String password;
 		private String host;
 		private int port = -1;
 		private String path = "/";
 		private Map<String,String> query;
 		private String fragment;
 		
 		public OwnURL() {
 		}
 		
 		/**
 		 * This method takes an URL as input and returns it in a normalized form. There should be no change in the functionality of the URL.
 		 * @param location the unnormalized URL
 		 * @param charset the {@link Charset} of the document this link was extracted from. It is needed to transform URL-encoded entities
 		 *        to Java's Unicode representation. If <code>null</code> is passed, the default charset will be used
 		 * @return the normalized URL consisting of protocol, username/password if given, hostname, port if it is not the default port for
 		 *         its protocol, the path and all given query arguments. The fragment part is omitted. It also performs Punycode- and
 		 *         URL-decoding.
 		 * @author Roland Ramthun, Franz Brau&szlig;e
 		 * @throws MalformedURLException if the given URL-String could not be parsed due to inconsistency with the URI-standard
 		 */
 		public String parseBaseUrlString(final String url, final Charset charset) throws MalformedURLException, ParseException {
 			// init
 			protocol = null;
 			username = null;
 			password = null;
 			host = null;
 			port = -1;
 			path = "/";
 			if (query != null)
 				query.clear();
 			fragment = null;
 			
 			// extract the protocol
 			final int colonpos = url.indexOf(':');
 			if (colonpos <= 0)
 				throw new MalformedURLException("No protocol specified in URL " + url);
 			protocol = url.substring(0, colonpos).toLowerCase();
 			
 			final int protocolEnd;
			if (url.charAt(colonpos + 1) == '/' && url.charAt(colonpos + 2) == '/') {
 				protocolEnd = colonpos + 3;
 			} else {
 				throw new MalformedURLException("No valid protocol identifier given in URL " + url);
 			}
 			
 			// extract username / password
 			final int slashAfterHost = url.indexOf('/', protocolEnd);
 			final int at = url.indexOf('@');
 			final int hostStart;
 			final int credSepColon = url.indexOf(':', protocolEnd); //the colon which separates username and password
 			if (at != -1 && at < slashAfterHost) {
 				if (credSepColon > (protocolEnd + 1) && credSepColon < at) {
 					username = url.substring(protocolEnd, credSepColon);
 					password = url.substring(credSepColon + 1, at);
 				} else {
 					username = url.substring(protocolEnd, at);
 				}
 				hostStart = at + 1;
 			} else {
 				hostStart = protocolEnd;
 			}
 			
 			// extract the hostname
 			final int portColon = url.indexOf(':', hostStart);
 			final int hostEnd = (portColon == -1) ? (slashAfterHost == -1) ? url.length() : slashAfterHost : portColon;
 			// TODO: de-punycode
 			// host = IDN.toUnicode(url.substring(hostStart, hostEnd).toLowerCase());		// de-punycode (sub-)domain(s) - java 1.6 code
 			host = url.substring(hostStart, hostEnd).toLowerCase();
 			
 			// extract the port
 			final int portEnd = (slashAfterHost == -1) ? url.length() : slashAfterHost;
 			if (portColon != -1 && portColon < portEnd) {
 				final String portNr = url.substring(portColon + 1, portEnd);
 				if (!portNr.matches("\\d{1,5}"))
 					throw new MalformedURLException("Illegal port-number in URL " + url);
 				port = Integer.parseInt(portNr);
 				if (port < 1 || port > 65535)
 					throw new MalformedURLException("Port-number out of range in URL " + url);
 				final Integer defPort = DEFAULT_PORTS.get(protocol);
 				if (defPort != null && port == defPort.intValue())
 					port = -1;
 			}
 			
 			if (slashAfterHost != -1) {
 				// extract the path
 				final int qmark = url.indexOf('?', slashAfterHost);
 				final int hashmark = url.indexOf('#', slashAfterHost);
 				final int pathEnd = (qmark == -1) ? (hashmark == -1) ? url.length() : hashmark : qmark;
 				path = resolveBackpath(urlDecode(url.substring(slashAfterHost, pathEnd), charset));
 				if (appendSlash &&
 						path.charAt(path.length() - 1) != '/' &&
 						path.indexOf('.', path.lastIndexOf('/')) == -1) {
 					path += '/';
 				}
 				
 				// extract the query
 				if (qmark != -1) {
 					final int queryEnd = (hashmark == -1) ? url.length() : hashmark;
 					if (queryEnd > qmark + 1) {
 						if (query == null)
 							query = (sortQuery) ? new TreeMap<String,String>() : new LinkedHashMap<String,String>();
 						int paramStart = qmark + 1;
 						do {
 							int paramEnd = url.indexOf('&', paramStart);
 							if (paramEnd == -1 || paramEnd > queryEnd)
 								paramEnd = queryEnd;
 							final int eq = url.indexOf('=', paramStart);
 							if (eq == -1 || eq > paramEnd)
 								throw new MalformedURLException("Illegal query parameter " + url.substring(paramStart, paramEnd) + " in URL " + url);
 							query.put(
 									urlDecode(url.substring(paramStart, eq).replace('+', ' '), charset),
 									urlDecode(url.substring(eq + 1, paramEnd).replace('+', ' '), charset));
 							paramStart = paramEnd + 1;
 						} while (paramStart < queryEnd);
 					}
 				}
 				
 				// extract the fragment
 				if (hashmark != -1)
 					fragment = urlDecode(url.substring(hashmark + 1), charset);
 			}
 			
 			// output
 			return toNormalizedString();
 		}
 		
 		private StringBuffer appendQuery(final StringBuffer sb) {
 			for (Map.Entry<String,String> e : query.entrySet())
 				sb.append(e.getKey()).append('=').append(e.getValue()).append('&');
 			sb.deleteCharAt(sb.length() - 1);
 			return sb;
 		}
 		
 		/**
 		 * Assembles the normalized representation of the given URL
 		 * @return the normalized URL
 		 */
 		public String toNormalizedString() {
 			final StringBuffer sb = new StringBuffer(protocol).append("://");
 			if (username != null) {
 				sb.append(username);
 				if (password != null)
 					sb.append(':').append(password);
 				sb.append('@');
 			}
 			sb.append(host);
 			if (port != -1)
 				sb.append(':').append(port);
 			
 			sb.append(path);
 			if (query != null && query.size() > 0)
 				appendQuery(sb.append('?'));
 			return sb.toString();
 		}
 	}
 }
