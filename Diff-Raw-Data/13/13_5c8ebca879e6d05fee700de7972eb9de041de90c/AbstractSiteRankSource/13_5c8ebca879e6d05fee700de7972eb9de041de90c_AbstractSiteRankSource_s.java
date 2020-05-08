 package org.healthonnet.lucene.siterank.source;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.healthonnet.lucene.siterank.SiteRankInfo;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Throwables;
 
 /**
  * A SiteRankSource is an object that tells us where to get information about the site rank of a given URL.
  * @author nolan
  *
  */
 public abstract class AbstractSiteRankSource implements SiteRankSource {
 
     private static final int DEFAULT_VALUE = -1;
 
     private boolean extractDomainFromUrl;
     
     /**
      * Build up a URI for querying, based on the given input URL string (representing the 
      * site whose rank we want to know.)
      * @param url
      * @return
      */
     protected abstract URI buildURI(String inputUrl);
     
     /**
      * Parse the HTTP result of a URI and return the rank.
      * @param result
      * @param defaultValue
      * @return
      */
     protected abstract int parseRank(String result, int defaultValue);
     
     /**
      * The total number of URLs ranked by this ranking source.
      * @return
      */
     protected abstract int getTotal();
     
     public SiteRankInfo getRankInfo(String inputUrl) {
         
         int rank = DEFAULT_VALUE;
         
         if (extractDomainFromUrl) {
             try {
                 URI uri = new URI(inputUrl);
                inputUrl = Preconditions.checkNotNull(uri.getHost(), 
                        "String %s is not a valid URL. " +
                        "Use extractDomainFromUrl=false if you're not inputting full URLs", inputUrl);
             } catch (URISyntaxException e) {
                throw Throwables.propagate(e);
             }
         }
         
         try {
             rank = getRankInternal(inputUrl);
         } catch (MalformedURLException e) {
             throw Throwables.propagate(e);
         } catch (IOException e) {
             // URL returned with an error; just give no site rank and continue on
             e.printStackTrace();
         }
         
         if (rank == DEFAULT_VALUE) { // not found
             return new SiteRankInfo(0, getTotal(), false);
         }
         return new SiteRankInfo(rank, getTotal(), true);
     }
     
     private int getRankInternal(String inputUrl) throws MalformedURLException, IOException {
         URL url = buildURI(inputUrl).toURL();
         
         BufferedReader bufferedReader = null;
         StringBuilder stringBuilder = new StringBuilder();
         try {
             bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));
             String line = null;
             while ((line = bufferedReader.readLine()) != null) {
                 stringBuilder.append(line);
             }
         } finally {
             if (bufferedReader != null) {
                  bufferedReader.close();
             }
         }
         return parseRank(stringBuilder.toString(), DEFAULT_VALUE);        
     }
     
     public void setExtractDomainFromUrl(boolean extractDomainFromUrl) {
         this.extractDomainFromUrl = extractDomainFromUrl;
     }
     
     @Override
     public String toString() {
         return ToStringBuilder.reflectionToString(this);
     }
 }
