 package com.barchart.feed.ddf.instrument.provider;
 
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.LOOKUP;
 import static com.barchart.feed.ddf.util.HelperXML.XML_STOP;
 import static com.barchart.feed.ddf.util.HelperXML.xmlFirstChild;
 import static com.barchart.feed.ddf.util.HelperXML.xmlStringDecode;
 
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.zip.GZIPInputStream;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.openfeed.proto.inst.InstrumentDefinition;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import rx.Observable;
 import rx.subjects.ReplaySubject;
 
 import com.barchart.feed.api.consumer.MetadataService.Result;
 import com.barchart.feed.api.consumer.MetadataService.SearchContext;
 import com.barchart.feed.api.model.meta.Instrument;
 import com.barchart.feed.api.model.meta.id.VendorID;
 import com.barchart.feed.base.provider.Symbology;
 import com.barchart.feed.ddf.instrument.provider.DDF_InstrumentProvider.RemoteRunner;
 import com.barchart.feed.ddf.util.HelperXML;
 import com.barchart.feed.inst.participant.InstrumentState;
 import com.barchart.feed.inst.provider.InstrumentFactory;
 
 public class DDF_RxInstrumentProvider {
 
 	private static final Logger log = LoggerFactory.getLogger(
 			DDF_RxInstrumentProvider.class);
 	
 	private static final int MAX_URL_LEN = 7500;
 			
 	static final ConcurrentMap<String, List<InstrumentState>> symbolMap =
 			new ConcurrentHashMap<String, List<InstrumentState>>();
 	
 	public static VendorID CQG_VENDOR_ID = new VendorID("CQG");
 	
 	/* ***** ***** ***** Begin Executor ***** ***** ***** */
 	
 	/**
 	 * Default executor service with dameon threads
 	 */
 	private volatile static ExecutorService executor = Executors.newCachedThreadPool( 
 			
 			new ThreadFactory() {
 
 		final AtomicLong counter = new AtomicLong(0);
 		
 		@Override
 		public Thread newThread(final Runnable r) {
 			
 			final Thread t = new Thread(r, "Feed thread " + 
 					counter.getAndIncrement()); 
 			
 			t.setDaemon(true);
 			
 			return t;
 		}
 		
 	});
 	
 	/**
 	 * Bind framework executor.
 	 * @param e
 	 */
 	public synchronized static void bindExecutorService(final ExecutorService e) {
 		
 		log.debug("Binding new executor service");
 		
 		executor.shutdownNow();
 		executor = e;
 		executor.submit(new RemoteRunner());
 		
 	}
 	
 	/* ***** ***** ***** End Executor ***** ***** ***** */
 	
 	public static Observable<Result<Instrument>> fromString(final String... symbols) {
 		return fromString(SearchContext.NULL, symbols);
 	}
 	
 	public static Observable<Result<Instrument>> fromString(final SearchContext ctx, 
 			final String... symbols) {
 		
 		ReplaySubject<Result<Instrument>> sub = ReplaySubject.create();
 		executor.submit(runnableFromString(sub, ctx, symbols));
 		
 		return sub;
 	}
 	
 	public static Runnable runnableFromString(
 			final ReplaySubject<Result<Instrument>> sub,
 			final SearchContext ctx, final String... symbols) {
 		
 		return new Runnable() {
 
 			@Override
 			public void run() {
 				
 				final Map<String, List<InstrumentState>> res = 
 						new HashMap<String, List<InstrumentState>>();
 				
 				final List<String> toBatch = new ArrayList<String>();
 				
 				/* Filter out cached symbols */
 				for(String symbol : symbols) {
 					
 					symbol = Symbology.formatSymbol(symbol);
 					
 					if(symbolMap.containsKey(symbol)) {
 						res.put(symbol, symbolMap.get(symbol));
 					} else {
 						toBatch.add(symbol);
 					}
 					
 				}
 				
 				try {
 					
 					final List<String> queries = buildQueries(toBatch);
 					
 					for(final String query : queries) {
 						
 						final Map<String, List<InstrumentState>> lookup = remoteLookup(query);
 						
 						/* Store instruments returned from lookup */
 						for(final Entry<String, List<InstrumentState>> e : lookup.entrySet()) {
 							symbolMap.put(e.getKey(), e.getValue());
 							
 							/* Add alternate options symbol */
 							final InstrumentState inst = e.getValue().get(0);
 							if(inst != null) {
 								
 								if(inst.symbol().contains("|")) {
									symbolMap.put(inst.vendorSymbols().get(
											VendorID.BARCHART), e.getValue());
 								}
 								
 							}
 							
 						}
 						
 						res.putAll(lookup);
 					}
 					
 					sub.onNext(result(res));
 					sub.onCompleted();
 				} catch (Exception e1) {
 					sub.onError(e1);
 				}
 			}
 			
 		};
 		
 	}
 	
 	private static Map<String, List<InstrumentState>> remoteLookup(final String query) {
 		
 		try {
 		
 			final Map<String, List<InstrumentState>> result = 
 					new HashMap<String, List<InstrumentState>>();
 			
 			log.debug("remote batch on {}", urlInstrumentLookup(query));
 			
 			final URL url = new URL(urlInstrumentLookup(query));
 			
 			final HttpURLConnection connection = (HttpURLConnection) url
 					.openConnection();
 			
 			connection.setRequestProperty("Accept-Encoding", "gzip");
 			
 			connection.connect();
 			
 			InputStream input = connection.getInputStream();
 	
 			if (connection.getContentEncoding().equals("gzip")) {
 				input = new GZIPInputStream(input);
 			}
 			
 			final BufferedInputStream stream =
 					new BufferedInputStream(input);
 	
 			final SAXParserFactory factory =
 					SAXParserFactory.newInstance();
 			final SAXParser parser = factory.newSAXParser();
 			
 			final DefaultHandler handler = new DefaultHandler() {
 				
 				@Override
 				public void startElement(final String uri,
 						final String localName, final String qName,
 						final Attributes ats) throws SAXException {
 					
 					if (qName != null && qName.equals("instrument")) {
 
 						try {
 							
 							final String lookup = xmlStringDecode(ats, LOOKUP, XML_STOP);
 							final InstrumentDefinition def = InstrumentXML.decodeSAX(ats);
 							
 							InstrumentState inst;
 							
 							if (def != InstrumentDefinition.getDefaultInstance()) {
 								inst = InstrumentFactory.instrumentState(def);
 							} else {
 								inst = InstrumentState.NULL;
 							}
 							
 							final List<InstrumentState> insts = new ArrayList<InstrumentState>();
 							insts.add(inst);
 							result.put(lookup, insts);
 							
 						} catch (final SymbolNotFoundException se) {
 							throw new RuntimeException(se); // would be nice to add to map
 						} catch (final Exception e) {
 							throw new RuntimeException(e);
 						}
 						
 					}
 					
 				}
 				
 			};
 			
 			parser.parse(stream, handler);
 			
 			return result;
 		
 		} catch (final Exception e) {
 			throw new RuntimeException(e);
 		}
 		
 	}
 	
 	/* ***** ***** ***** CQG ***** ***** ***** */
 	
 	private static final ConcurrentMap<String, String> cqgSymMap =
 			new ConcurrentHashMap<String, String>();
 	
 	public static Observable<String> fromCQGString(final String symbol) {
 		try {
 			return Observable.from(executor.submit(
 					callableFromCQGString(SearchContext.NULL, symbol)));
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public static Callable<String> callableFromCQGString(
 			final SearchContext ctx, final String symbol) {
 		
 		return new Callable<String>() {
 
 			@Override
 			public String call() throws Exception {
 				
 				/* Filter out cached symbols */
 				if(cqgSymMap.containsKey(symbol)) {
 					return cqgSymMap.get(symbol);
 				}
 				
 				final String query = cqgInstLoopURL(symbol);
 				
 				final Element root = HelperXML.xmlDocumentDecode(query);
 
 		        final Element tag = xmlFirstChild(root, "symbol", XML_STOP);
 		        
 		        final String result = Symbology.formatSymbol(tag.getTextContent());
 		        
 		        if(result != null) {
 		        	cqgSymMap.put(symbol, result);
 		        }
 		        
 		        return result;
 				
 			}
 		};
 		
 	}
 	
 	private static Result<Instrument> result(final Map<String, List<InstrumentState>> res) {
 		
 		return new Result<Instrument>() {
 
 			@Override
 			public SearchContext context() {
 				return SearchContext.NULL;
 			}
 
 			@Override
 			public Map<String, List<Instrument>> results() {
 				Map<String, List<Instrument>> result = new HashMap<String, List<Instrument>>();
 				for(final Entry<String, List<InstrumentState>> e : res.entrySet()) {
 					final List<Instrument> list = new ArrayList<Instrument>();
 					list.add(e.getValue().get(0));
 					result.put(e.getKey(), list);
 				}
 				return result;
 			}
 
 			@Override
 			public boolean isNull() {
 				return false;
 			}
 		};
 		
 	}
 	
 	/* ***** ***** ***** Begin Remote Lookup ***** ***** ***** */
 	
 	private static final String SERVER_EXTRAS = "extras.ddfplus.com";
 	
 	private static final String CQG_SYMBOL = "&symbology=CQG";
 
 	private static final String urlInstrumentLookup(final CharSequence lookup) {
 		return "http://" + SERVER_EXTRAS + "/instruments/?lookup=" + lookup + CQG_SYMBOL;
 	}
 	
 	private static final String cqgInstLoopURL(final CharSequence lookup) {
 		return "http://" + SERVER_EXTRAS + "/symbology/?symbol=" + lookup +
                  "&provider=CQG";
 	}
 	
 	static List<String> buildQueries(final List<String> symbols) throws Exception {
 		
 		final List<String> queries = new ArrayList<String>();
 		
 		while(!symbols.isEmpty()) {
 		
 			final StringBuilder sb = new StringBuilder();
 			int len = 0;
 			int symCount = 0;
 			
 			while(len < MAX_URL_LEN && symCount < 400 && !symbols.isEmpty()) {
 			
 				final String s = symbols.remove(0);
 			
 				log.debug("Pulled {} from remote queue", s);
 			
 				symCount++;
 				len += s.length() + 1;
 				sb.append(s).append(",");
 			
 			}
 		
 			/* Remove trailing comma */
 			sb.deleteCharAt(sb.length() - 1);
 			
 			queries.add(sb.toString());
 			
 			log.debug("Sending {} to remote lookup", sb.toString());
 		
 		}
 		
 		return queries;
 		
 	}
 	
 }
