 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.legacy;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 import de.cosmocode.palava.bridge.ConnectionLostException;
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.MimeType;
 import de.cosmocode.palava.bridge.Server;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.call.DataCall;
 import de.cosmocode.palava.bridge.call.TextCall;
 import de.cosmocode.palava.bridge.command.Job;
 import de.cosmocode.palava.bridge.command.Response;
 import de.cosmocode.palava.bridge.content.JsonContent;
 import de.cosmocode.palava.bridge.content.PhpContent;
 import de.cosmocode.palava.bridge.content.TextContent;
 import de.cosmocode.palava.bridge.session.HttpSession;
 
 /**
  * 
  * @deprecated use Cache annotation
  *
  * @author Willi Schoenborn
  */
 @Deprecated
 public abstract class CachableJob extends UtilityJobImpl implements Job {
     
     //  methods implemented from UtilityJob
 
     @Override
     public String getMandatory(String key) throws Exception {
         throw new UnsupportedOperationException();
     }
     @Override
     public String getMandatory(String key, String argumentType) throws Exception {
         throw new UnsupportedOperationException();
     }
     @Override
     public String getOptional(String key) {
         throw new UnsupportedOperationException();
     }
     @Override
     public String getOptional(String key, String defaultValue) {
         throw new UnsupportedOperationException();
     }
     @Override
     public boolean hasArgument(String key) {
         throw new UnsupportedOperationException();
     }
     
     // ----------------------
     // Static items used for caching
     // ----------------------
 
    private static final Logger logger = LoggerFactory.getLogger(CachableJob.class);
     private static final Map<CacheItem, Content> cache = new ConcurrentHashMap<CacheItem, Content>();
     private static final Queue<CacheItem> cacheQueue = new ConcurrentLinkedQueue<CacheItem>();
     
     /**  100MB maximum Cache size. TODO: make it configurable  */
     private static final long maxCacheSize = 104857600l;
     private static final AtomicLong currentUsedCacheSize = new AtomicLong(0l);
 
     
     /**
      * @return the cache, unmodifiable
      */
     public static Map<CacheItem, Content> getCache() {
         return Collections.unmodifiableMap(cache);
     }
 
     /**
      * @return the current used cache size (in byte)
      */
     public static long getCurrentUsedCacheSize() {
         return currentUsedCacheSize.get();
     }
     /**
      * @return the maximum available cache size (in byte)
      */
     public static long getMaxCacheSize() {
         return maxCacheSize;
     }
     
     /**
      * @return the number of items cached (CacheItem->Content)
      */
     public static int getItemCount() {
         return cacheQueue.size();
     }
     
     /**
      * Clears the cache and removes all mappings from the cache. Returns the number of items removed.
      * @return the number of items removed (i.e. the number of items previously in the cache).
      */
     public static int clearCache() {
         final int previousCacheSize = cacheQueue.size();
         synchronized (cache) {
             cache.clear();
             cacheQueue.clear();
             currentUsedCacheSize.set(0);
         }
         return previousCacheSize;
     }
     
     
     // ----------------------
     // Abstract methods that must be implemented ...
     // ----------------------
     
     /**
      * Process the job as it normally would be.
      * ATTENTION: parameters server and session are flipped in the signature
      */
     public abstract void process(Call request,
             Response response, 
             Server server, 
             HttpSession session, 
             Map<String, Object> caddy)
     throws ConnectionLostException, Exception;
     
     // ----------------------
     // Overridable methods
     // ----------------------
     
     
     // ----------------------
     // Caching implementation
     
     /**
      * getArguments returns the arguments of a given request.
      * the arguments should be a unique identifier for the combination of job and request,
      * for example getArgs() on a DataRequest or getText() on a JSONRequest.
      * @return the unique arguments of this job for the given request
      * @throws Exception: in case something went wrong
      * @throws UncachableException: if the given request isn't supported
      */
     protected Object getArguments(Call request) throws Exception, UncachableException {
         if (request instanceof DataCall) {
             return ((DataCall) request).getStringedArguments();
         } else if (request instanceof TextCall) {
             return ((TextCall)request).getText();
         } else {
             throw new UncachableException("unsupported request of type: " + request.getClass().getCanonicalName());
         }
     }
     
     /**
      * Returns the CacheItem for the job-call or null if caching is not supported.
      */
     protected CacheItem getCacheItem(Call request, HttpSession session, Server server, Map<String, Object> caddy) throws Exception {
         final String language = session == null ? null : (String) session.get("lang");
         
         // we need a language for caching
         if (language == null)  return null;
         
         try {
             // normal behaviour
             return new CacheItem(this, getArguments(request), language, request.getHeader().getContentLength());
         } catch (UncachableException e) {
             // no caching for this job
             logger.warn("getArguments not supported by job " + this.getClass().getCanonicalName(), e);
             return null;
         }
     }
     
     /**
      * Returns true if the Job-Call represented by the given CacheItem is cached, false otherwise.
      * @param cacheItem a wrapper for the cacheItem
      * @return true if this job is cached, false otherwise
      */
     protected boolean isCached(CacheItem cacheItem) {
         return cacheItem != null && cache.containsKey(cacheItem);
     }
     
     /**
      * Puts the mapping for the given cache item to the content into the cache.
      * This method is called each time after the child Job calls (and returns from) the "process"-method
      * and isCached(cacheItem) returns false.
      * @param cacheItem the cache item that represents the unique identifier for this request
      * @param content the response-content
      * @throws UncachableException thrown to indicate that we cannot cache; should not be thrown excessively, as it is catched by "process"
      */
     protected void putIntoCache(final CacheItem cacheItem, final Content content) throws UncachableException {
         if (cacheItem != null && content != null) {
             // just an approximation ...
             final long tmpCalculated = 2*(
                 cacheItem.getArgumentsLength()
                 + cacheItem.getJobName().length()
                 + cacheItem.getLanguage().length()
                 + content.getLength()
                 )+38*4;
             
             cache.put(cacheItem, content);
             cacheQueue.add(cacheItem);
             
             // add calculated cache size to current used cache
             if (currentUsedCacheSize.addAndGet(tmpCalculated) > maxCacheSize) {
                 // We have exceeded our cache-limit:
                 // we must delete items from the cache, oldest first
                 int removedItems = 0;
                 while (currentUsedCacheSize.get() > maxCacheSize && cache.size() > 0 && cacheQueue.size() > 0) {
                     final CacheItem rmCacheItem = cacheQueue.poll();
                     removeFromCache(rmCacheItem);
                     removedItems++;
                 }
                 if (logger.isInfoEnabled() && removedItems > 0) logger.info ("removed " + removedItems + " items from cache to free ram");
             }
 
             // logging
             if (logger.isDebugEnabled()) {
                 logger.debug ("putIntoCache: " + cacheItem.getJobName() + ", " + cacheItem.getJobArguments() + ", " + cacheItem.getLanguage());
                 logger.debug ("    ~total mem usage: " + currentUsedCacheSize); //ObjectSizeCalculator.sizeOf(cache));
             }
         }
     }
     
     protected boolean removeFromCache (final CacheItem cacheItem) {
         if (cacheItem != null && cache.containsKey(cacheItem)) {
             final Content content = cache.remove(cacheItem);
             
             if (content == null) return false;  // nothing removed
 
             final long tmpCalculated = 2*(
                 cacheItem.getArgumentsLength()
                 + cacheItem.getJobName().length()
                 + cacheItem.getLanguage().length()
                 + content.getLength()
                 )+38*4;
             
             currentUsedCacheSize.addAndGet(-tmpCalculated);
             
             return true;
         } else {
             return false;
         }
     }
     
     // ----------------------
     // Implementation of "process": Caching occurs here
     // ----------------------
 
     @Override
     public final void process(Call request,
             Response response,
             HttpSession session,
             Server server,
             Map<String, Object> caddy)
     throws ConnectionLostException, Exception {
         
         // lookup cache item
         final CacheItem cacheItem = getCacheItem(request, session, server, caddy);
         
         // try to find the response-content for this job-call in the cache
         // and put it into the response and end execution.
         if (isCached(cacheItem)) {
             final Content content = cache.get(cacheItem);
             response.setContent(content);
             
             // logging ...
             logger.info  ("CachableJob: Retrieved data from cache");
             if (logger.isDebugEnabled() && cacheItem != null) {
                 logger.debug ("Content-Length: " + content.getLength());
                 logger.debug ("Arguments: " + cacheItem.getJobArguments());
                 logger.debug ("Cache-Content: " + cacheItem.getJobName());
                 logger.debug ("Language: " + cacheItem.getLanguage());
             }
             if (logger.isTraceEnabled()) {
                 if (content instanceof TextContent ||
                     content instanceof PhpContent ||
                     content instanceof JsonContent) {
                     final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                     content.write(stream);
                    logger.trace("{}", stream);
                 }
             }
         } else {
             // the response could not be found in the cache:
             // let the job do its processing and put the response-content into the cache afterwards
             try {
                 process (request, response, server, session, caddy);
                 putIntoCache(cacheItem, response.getContent());
             } catch (UncachableException e) {
                 // do nothing; process threw an UncachableException, so putIntoCache does nothing
                 // NOTE: only UncachableException is catched, all other exceptions are ignored
             }
         }
     }
     
     // TODO move to own file
     public static class ImmutableContent implements Content {
         
         private final Content wrapped;
         
         public ImmutableContent(final Content content) {
             this.wrapped = content;
         }
 
         @Override
         public long getLength() {
             return wrapped.getLength();
         }
 
         @Override
         public MimeType getMimeType() {
             return wrapped.getMimeType();
         }
 
 
         @Override
         public void write(OutputStream out) throws IOException {
             wrapped.write(out);
         }
         
     }
     
     /**
      * A helper class that helps identify the called job.
      * It uses the job name (fully canonical java-class-name),
      * the arguments and the language in the session to identify it.
      */
     public static class CacheItem {
         private final String jobName;
         private final Object jobArguments;
         private final String language;
         
         private long argumentsLength;
         
         
         public String getJobName() {
             return jobName;
         }
         public Object getJobArguments() {
             return jobArguments;
         }
         public String getLanguage() {
             return language;
         }
         public void setArgumentsLength(long argumentsLength) {
             this.argumentsLength = argumentsLength;
         }
         public long getArgumentsLength() {
             return argumentsLength;
         }
         
         public CacheItem (CachableJob job, Object jobArguments, String language, long argumentsLength) {
             this(job, jobArguments, language);
             this.argumentsLength = argumentsLength;
         }
         
         /**
          * Takes the canonical jobName, the jobArguments and the language.
          * This is the minimal constructor for JobCache that ensures 
          * that equals(Object o) and hashCode() work as they should.
          * @param job
          * @param jobArguments
          * @param language
          */
         public CacheItem (CachableJob job, Object jobArguments, String language) {
             this (job.getClass().getCanonicalName(), jobArguments, language);
         }
         
         /**
          * Takes the canonical jobName, the jobArguments and the language.
          * This is the minimal constructor for JobCache that ensures 
          * that equals(Object o) and hashCode() work as they should.
          * @param job
          * @param jobArguments
          * @param language
          */
         public CacheItem (String jobName, Object jobArguments, String language) {
             this.jobName = jobName;
             this.jobArguments = jobArguments;
             this.language = language;
         }
 
         @Override
         public int hashCode() {
             final int PRIME = 31;
             int result = 1;
             result = PRIME * result + ((jobArguments == null) ? 0 : jobArguments.hashCode());
             result = PRIME * result + ((jobName == null) ? 0 : jobName.hashCode());
             result = PRIME * result + ((language == null) ? 0 : language.hashCode());
             return result;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (this == obj)
                 return true;
             if (obj == null)
                 return false;
             if (getClass() != obj.getClass())
                 return false;
             final CacheItem other = (CacheItem) obj;
             if (jobArguments == null) {
                 if (other.jobArguments != null)
                     return false;
             } else if (!jobArguments.equals(other.jobArguments))
                 return false;
             if (jobName == null) {
                 if (other.jobName != null)
                     return false;
             } else if (!jobName.equals(other.jobName))
                 return false;
             if (language == null) {
                 if (other.language != null)
                     return false;
             } else if (!language.equals(other.language))
                 return false;
             return true;
         }
         
     }
 
 }
