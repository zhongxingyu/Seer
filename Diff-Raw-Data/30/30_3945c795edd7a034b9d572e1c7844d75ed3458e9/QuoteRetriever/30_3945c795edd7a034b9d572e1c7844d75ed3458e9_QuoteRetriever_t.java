 package x1.stomp.service;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import javax.inject.Inject;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.client.fluent.Request;
 import org.slf4j.Logger;
 
 import x1.stomp.model.Quote;
 import x1.stomp.model.Share;
 
 public class QuoteRetriever {
   private static final String URL = "http://de.finance.yahoo.com/d/quotes.csv?s={0}&f=sna";
   private static final String CURRENCY = "EUR";
 
   @Inject
   private Logger log;
 
   public Quote retrieveQuote(Share share) {
     try {
       String content = retrieveInternal(share.getKey());
       return createQuote(content, share);
     } catch (IOException e) {
       log.warn(null, e);
       return null;
     }
   }
 
   public List<Quote> retrieveQuotes(List<Share> shares) {
     StringBuffer buffer = new StringBuffer();
     for (Share share : shares) {
       if (buffer.length() > 0) {
         buffer.append('+');
       }
       buffer.append(share.getKey());
     }
 
     List<Quote> result = new ArrayList<>();
     try {
       String content = retrieveInternal(buffer.toString());
       StringTokenizer tokenizer = new StringTokenizer(content, "\n\r");
       while (tokenizer.hasMoreTokens()) {
         String line = tokenizer.nextToken();
         Quote quote = createQuote(line, shares);
         if (quote != null) {
           result.add(quote);
         }
       }
     } catch (IOException e) {
       log.warn(null, e);
     }
     return result;
   }
 
   private String retrieveInternal(String keys) throws IOException {
    log.debug("Retrieve quotes for " + keys);
     String targetUrl = MessageFormat.format(URL, keys.toUpperCase());
     String content = Request.Get(targetUrl).execute().returnContent().asString();
     log.debug("Received content size:" + content.length());
     return content;
   }
 
   private Quote createQuote(String line, List<Share> shares) {
     String[] parts = StringUtils.split(line, ',');
     if (parts.length != 3) {
       return null;
     }
     String key = StringUtils.remove(parts[0], "\"").trim();
     String name = StringUtils.remove(parts[1], "\"").trim();
     String price = parts[2].trim();
     if (price.equalsIgnoreCase("N/A")) {
       return null;
     }
     for (Share share : shares) {
       if (share.getKey().equalsIgnoreCase(key)) {
         share.setKey(key);
         share.setName(name);
         Quote quote = new Quote(share);
         quote.setPrice(Float.valueOf(price));
         quote.setCurrency(CURRENCY);
         return quote;
       }
     }
     return null;
   }
 
   private Quote createQuote(String line, Share share) {
     return createQuote(line, Arrays.asList(share));
   };
 }
