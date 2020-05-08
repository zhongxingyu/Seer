 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hd.cl.haas.distributedcrawl.Indexer;
 
 import de.hd.cl.haas.distributedcrawl.App;
 import de.hd.cl.haas.distributedcrawl.common.*;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.Source;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.SequenceFile;
 import org.apache.hadoop.mapreduce.Mapper;
 
 /**
  *
  *
  * @author Michael Haas
  */
 public class IndexerMap extends Mapper<URLText, WebDBURLList, Term, Posting> {
 
     /**
      * How many seconds we wait before hitting a server again.
      */
     private static final int CRAWL_DELAY = 3;
     /**
      * How many seconds we wait before crawling a website again. This means that
      * WebDB timestamp for an URL must be older than this value.
      */
     private static final int MIN_AGE = 300;
     private SequenceFile.Writer writer;
 
     @Override
     protected void setup(Context context) throws IOException, InterruptedException {
         super.setup(context);
         if (this.writer == null) {
             // TODO: update with correct class?
             FileSystem fs = FileSystem.get(context.getConfiguration());
             int id = context.getJobID().getId();
             Path outPath = new Path(context.getWorkingDirectory(), App.FRESHURLS_DIR);
             if (!fs.exists(outPath)) {
                 fs.mkdirs(outPath);
             }
             Path p = new Path(outPath, "shard-" + id + ".dat");
 
 
             // TODO: wrap writer in its own class to hide serialized class details?
             this.writer = SequenceFile.createWriter(fs, context.getConfiguration(), p, URLText.class, WebDBURLList.class);
             System.err.println("Writer initialized.");
         }
     }
 
     /**
      *
      *
      *
      * @param domain Host of current URL we're crawling
      * @param source
      * @throws IOException
      */
     private void processLinks(URLText domain, Source source) throws IOException {
         List<Element> anchorElements = source.getAllElements("a");
         for (Element anchorElement : anchorElements) {
             String target = anchorElement.getAttributeValue("href");
             System.err.println("Anchor target is: " + target);
             if (target == null) {
                 continue;
             }
             // is relative domain?
             if (target.startsWith("/")) {
                 target = domain.toString() + target;
                 // TODO: oops, domain does not have scheme.
                 target = "http://" + target;
             }
             if (!target.startsWith("http")) {
                 System.out.println("URL with unsupported scheme.");
                 continue;
             }
            // if target contains anchor inside page, remove part afterwards
             // we only crawl whole pages and the IndexMerger will remove
             // duplicates
             int offset;
             if ((offset = target.indexOf("#")) > -1) {
                 target = target.substring(0, offset);
             }
             // we use the URL of the current document as key, but I don't plan
             // on using this in the DB
             // We use date 0 for new URLs.
             // If we already have seen this URL in the database, we
             // will find it during the WebDBMerger stage and eliminate
             // the duplicate
             // We also write out the currently crawled URL with the current
             // date to have an up-to-date entry in the database
             WebDBURL u = new WebDBURL(new URLText(target), 0);
             // we use WebURLList format even for a single URL to have an uniform
             // file format for use with @WebDBMerger.
             // The WebDB-ish format we write out here however has duplicate keys
             // and even duplicate URLs
             // etc, so it's not usable as input for @Indexer.. or is it?
 
             // Duplicate keys (i.e. domains) are bad because it might cause too much
             // traffic and unnecessary crawls
 
             // TODO: what is the invariant on keys in SequenceFile or SequenceFileInputFormat
 
             this.writeDBURL(u);
         }
     }
 
     private void writeDBURL(WebDBURL url) throws MalformedURLException, IOException {
         String domainOfNewURL = url.getURL().getHost();
         this.writeDBURL(new URLText(domainOfNewURL), url);
     }
 
     private void writeDBURL(URLText host, WebDBURL url) throws IOException {
         ArrayList<WebDBURL> temp = new ArrayList<WebDBURL>();
         temp.add(url);
         WebDBURLList l = new WebDBURLList();
         l.fromCollection(temp);
         this.writer.append(host, l);
 
     }
 
     private Map<String, Integer> countTokens(String[] tokens) {
         // count absolute frequencies for terms
         HashMap<String, Integer> counts = new HashMap<String, Integer>();
         for (int jj = 0; jj < tokens.length; jj++) {
             String term = tokens[jj];
             // clean up string
             term = term.replace(",", "");
             term = term.replace(".", "");
             if (!counts.containsKey(term)) {
                 counts.put(term, 0);
             }
             counts.put(term, counts.get(term) + 1);
         }
         return counts;
     }
 
     @Override
     protected void map(URLText key, WebDBURLList value, Context context) throws IOException, InterruptedException {
 
         System.out.println("key is " + key.toString());
 
         // Write current URL to WebDB with crawl date.
         // TODO: this deprecates the need to merge two
         // separate index directories as the new index
         // will have all entries from the old index.. unless
         // we only write out URLs we actually crawl, as some URLs
         // will need to be skipped because they're not old enough.
 
         // So, to sum up: we still need to merge old and new index
         // as the new index (fresh-urls) only contains a subset of
         // old-index that was fresh enough to be crawled.
 
         WebDBURL[] urls = value.toArray();
         for (int ii = 0; ii < urls.length; ii++) {
             Thread.sleep(CRAWL_DELAY * 1000);
             WebDBURL dbURL = urls[ii];
             Date d = dbURL.getDate();
             Date now = new Date();
            if ((now.getTime() - d.getTime()) < 300 * 1000) {
                 System.err.println("URL " + dbURL.getText() + " is not old enough, not crawling");
                 continue;
             }
            WebDBURL newEntry = new WebDBURL(dbURL.getURLText(), new Date());
            this.writeDBURL(key, newEntry);
 
             URL url = dbURL.getURL();
             InputStream stream;
             try {
                 url.openConnection();
                
                 stream = url.openStream();
                
            } catch (java.io.FileNotFoundException e) {
                System.err.println("Caught FileNotFoundException when opening URL " + dbURL);
                e.printStackTrace();
                continue;
                
            } catch (java.net.UnknownHostException e) {
                System.err.println("Caught UnknownHostException when opening URL " + dbURL);
                 e.printStackTrace();
                 continue;
             }
             Source source = new Source(stream);
             source.fullSequentialParse();
             this.processLinks(key, source);
 
             String completeContent = source.getTextExtractor().toString();
             System.out.println("CompleteContent: ");
             System.out.println(completeContent);
             // poor man's tokenizer
             String[] tokens = completeContent.split(" ");
             Map<String, Integer> counts = this.countTokens(tokens);
 
             Term tTerm = new Term();
             LongWritable freq = new LongWritable();
             for (String term : counts.keySet()) {
                 // Emit(term t, posting <n,H{t}>)      
                 freq.set(counts.get(term));
                 Posting p = new Posting(dbURL.getURLText(), freq);
                 tTerm.set(term);
                 context.write(tTerm, p);
             }
         }
     }
 }
