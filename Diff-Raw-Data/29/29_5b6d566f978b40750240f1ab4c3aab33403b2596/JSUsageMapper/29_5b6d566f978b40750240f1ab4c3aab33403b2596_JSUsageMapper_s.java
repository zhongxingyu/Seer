 package edu.utwente.mbd;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 
 import edu.utwente.mbd.scriptparse.ScriptInformation;
 import edu.utwente.mbd.scriptparse.ScriptInformation.Type;
 import edu.utwente.mbd.scriptparse.ScriptTagExtractor;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.log4j.Logger;
 import org.commoncrawl.hadoop.mapred.ArcRecord;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
 import com.google.common.base.Joiner;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 
 public class JSUsageMapper extends Mapper<Text, ArcRecord, Text, LongWritable> {
 	private final ListeningExecutorService executor = MoreExecutors
 			.listeningDecorator(MoreExecutors.sameThreadExecutor());
 
 	private static final Logger LOG = Logger.getLogger(JSUsage.class);
 	
 	public final static String INLINE_PREFIX = "il:";
 	public final static String COUNT_PREFIX = "cn";
 	public final static String COOCCURRENCE_PREFIX = "co";
 	
 	public final static String SEP = "\t";
 	public final static String COMMA = ",";
 	public final static String NULL = "NULL";
 	
 	
 	private final static Joiner join = Joiner.on(SEP).useForNull(NULL);
 	private final static Joiner joinComma = Joiner.on(COMMA).useForNull(NULL);
 
 	private Document doc;
 	private LongWritable outVal = new LongWritable(1);
 
 	public static enum MAPPERCOUNTER {
		NOT_RECOGNIZED_AS_HTML, HTML_PARSE_FAILURE, HTML_PAGE_TOO_LARGE, EXCEPTIONS, OUT_OF_MEMORY, TIMEOUT
 	}
 
 	@Override
 	protected void map(final Text key, final ArcRecord value,
 			final Context context) throws IOException, InterruptedException {
 		try {
 			final String url = key.toString();
 			// check <html> tag
 			if (!value.getContentType().contains("html")) {
 				context.getCounter(MAPPERCOUNTER.NOT_RECOGNIZED_AS_HTML)
 						.increment(1);
 				return;
 			}
 
 			// ensure page size is reasonable
 			if (value.getContentLength() > (8 * 1024 * 1024)) {
 				context.getCounter(MAPPERCOUNTER.HTML_PAGE_TOO_LARGE)
 						.increment(1);
 				return;
 			}
 
 			// parse document, in Future so it is cancellable
 			ListenableFuture<Iterable<ScriptInformation>> scripts = executor
 					.submit(new Callable<Iterable<ScriptInformation>>() {
 						@Override
 						public Iterable<ScriptInformation> call()
 								throws Exception {
 							Document doc = value.getParsedHTML();
 
 							if (doc == null) { // parsing failed? return empty
 												// list.
 								context.getCounter(
 										MAPPERCOUNTER.HTML_PARSE_FAILURE)
 										.increment(1);
 								return Lists.newArrayList();
 							}
 							return ScriptTagExtractor.getScriptTags(url, doc);
 						}
 					});
 
 			// Handle all the script tags:
 			handleScriptTags(scripts.get(2, TimeUnit.MINUTES), context);
 		} catch (InterruptedException e) { // timeout on Future.get()
 			context.getCounter(MAPPERCOUNTER.TIMEOUT).increment(1);
 		} catch (Throwable e) {
 			if (e instanceof OutOfMemoryError) { // occassionally Jsoup parser
 													// runs out of memory ...
 				System.gc(); // first GC then increment
 				context.getCounter(MAPPERCOUNTER.OUT_OF_MEMORY).increment(1);
 			}
 
 			LOG.error("Caught Exception", e);
 			context.getCounter(MAPPERCOUNTER.EXCEPTIONS).increment(1);
 		}
 	}
 	
 	private void handleScriptTags (Iterable<ScriptInformation> scripts, Context context) throws IOException, InterruptedException{
 		List<String> libs = Lists.newArrayList();
 
 		for (ScriptInformation inf : scripts) { // handle all scripts
 			if (inf == null) // malformed URL or smth
 				continue;
 			
 			// prefix inline keys
 			String key = inf.type == Type.INLINE ? INLINE_PREFIX+inf.fileName : inf.fileName;
			// emit [filename, hostname of file], add to list.			
			context.write(new Text(join.join(COUNT_PREFIX, key, inf.pageAddr)), outVal);
 			
 			libs.add(inf.fileName);
			// when it is remote js: add complete url to list for co-occurence as well
			if (inf.type == Type.REMOTE)
				libs.add(inf.pageAddr);
 		}
 		// sort libs
 		List<String> sortedLibs = Ordering.natural().sortedCopy(libs);
 		
 		// emit libs
 		context.write(new Text(join.join(COOCCURRENCE_PREFIX, joinComma.join(sortedLibs)).toString()), outVal);
 	}
 
 }
