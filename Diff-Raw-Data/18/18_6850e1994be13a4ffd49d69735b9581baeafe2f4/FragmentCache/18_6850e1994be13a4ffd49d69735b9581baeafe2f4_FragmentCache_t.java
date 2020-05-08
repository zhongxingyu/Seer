 /*
  * FragmentCache.java
  *
  * Created on April 2nd, 2009
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.j2free.jsp.tags.cache;
 
 import java.io.IOException;
 
 
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.BodyContent;
 import javax.servlet.jsp.tagext.BodyTagSupport;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  *  Implementation of a fragment cache as a custom tag.  This fragment cache
  *  guarantees that only a single thread may trigger an update
  * 
  * @author  Ryan Wilson
  */
 public class FragmentCache extends BodyTagSupport {
     
     private static final Log log = LogFactory.getLog(FragmentCache.class);
 
     public static final AtomicLong WARNING_COMPUTE_DURATION = new AtomicLong(10000);
 
    public static final AtomicBoolean enabled = new AtomicBoolean(false);
 
     // This is the max amount of time a thread will wait() on another thread
     // that is currently updating the Fragment.  If the updating thread does
     // not complete the update within REQUEST_TIMEOUT, the waiting thread
     // will print a message to refresh the page.
     public static final AtomicLong REQUEST_TIMEOUT = new AtomicLong(20);
 
     private static final String ATTRIBUTE_DISABLE_ONCE = "nocache";
 
     // The backing ConcurrentMap
     private static final ConcurrentMap<String,Fragment> cache =
             new ConcurrentHashMap<String,Fragment>(50000,0.8f,50);
 
     // The cleaner instance
     private static final FragmentCleaner cleaner = new FragmentCleaner(cache);
 
     // A single-threaded executor to run the cleaner task
     private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
 
     // A ScheduledFuture representing the cleaner task
     private static volatile ScheduledFuture cleanerFuture = null;
 
     // Cache Timeout
     private long timeout;
     
     // Cache Key
     private String key;
     
     // Cache Condition, used to specify a value to monitor for a change
     private String condition;
 
     // If true, the cache will be ignored for this request
     private boolean disable;
     
     public FragmentCache() {
         super();
         disable = false;
     }
     
     public void setKey(String key) {
         this.key = key;
     }
     
     public void setCondition(String condition) {
         this.condition = condition;
     }
     
     public void setTimeout(long timeout) {
         this.timeout = timeout;
     }
 
     public void setDisable(boolean disable) {
         this.disable = disable;
     }
 
     private long start;
 
     /**
      *  To evaluate the BODY and have control passed to doAfterBody, return EVAL_BODY_BUFFERED
      *  To use the cached content, writed the fragment content to the page, then return SKIP_BODY
      *
      *  Rules:
      *
      *    - Conditionts under which the body should be evaluated:
      *      - Caching is disabled
      *      - Caching is enabled, but no cached fragment exists
      *      - Caching is enabled, but the cache has expired and no other thread is currently refreshing the cache
      *      - Caching is enabled, but the condition has changed, and no other thread is currently refreshing the cache
      *
      *    - Conditions under which the body should NOT be evaluated:
      *      - Caching is enabled and a cached version exists
      *      - Caching is enabled, the cache has expired, but another thread is refreshing the cache
      *      - Caching is enabled, the condition has changed, but another thread is refreshing the cache
      *
      */
     @Override
     public int doStartTag() throws JspException {
 
         start = System.currentTimeMillis();
 
        // If the cache isn't enabled, make sure disable is set
        disable |= !enabled.get();
 
         // If the tag isn't set to disable, check for other things that might disable this ...
         if (!disable) {
 
             // Check for the request attribute
             if (pageContext.getAttribute(ATTRIBUTE_DISABLE_ONCE) != null)
                 disable = true;
             
         }
 
         // If disable is set, either by the tag, the request attribute, or the global flag, then ignore the cache
         if (disable) {
             if (log.isTraceEnabled()) log.trace("Cache disabled for " + key);
             return EVAL_BODY_BUFFERED;
         }
 
         // See if there is a cached Fragment already
         Fragment cached = cache.get(key);
 
         // If not ...
         if (cached == null) {
 
             // If the fragment didn't exist, create a Fragment
             if (log.isTraceEnabled()) log.trace("cached == null [key: " + key + "]");
             Fragment fragment = new Fragment(condition, timeout);
 
             // Necessary to use putIfAbset, because the map could have changed
             // since calling cache.get(key) above
             cached = cache.putIfAbsent(key, fragment);
 
             // If putIfAbsent(key,fragment) returned null, then there was not
             // a Fragment for this key in the map, even by this point.
             if (cached == null) {
                 cached = fragment;
             }
             
         } else if (cached != null && cached.isExpiredAndAbandoned()) {
             // Or if it exists but has expired and is abandoned (meaning its' lock-for-update
             // has been held past its' lock wait timeout.
             
             // If we're here, it probably means that a thread hit an exception while updating the old fragment and was never
             // able to unlock the fragment.  So, remove the old fragment, then create a new one starting with the old content
             log.warn("Found unmodifiable Fragment, replacing [key: " + key + "], with a duplicate (but modifiable) Fragment");
             Fragment fragment = new Fragment(cached,condition,timeout);
             
             if (cache.replace(key, cached, fragment)) {
                 cached = fragment;
             }
         }
 
         // Try to acquire the lock for update.  If successful, then
         // the Fragment needs to be updated and this Thread has taken
         // the responsibility to do so.
         if (cached.tryAcquireForUpdate(condition)) {
             if (log.isTraceEnabled()) log.trace("successfully acquired lock-for-update [key: " + key + "]");
             return EVAL_BODY_BUFFERED;
         }
 
         if (log.isTraceEnabled()) log.trace("denied lock-for-update [key: " + key + "]");
 
         // Try to get the content, cached.get() will block if
         // the content of the fragment is not yet set, so catch an
         // InterruptedException
         String response = null;
         try {
             if (log.isTraceEnabled()) log.trace("calling cached.getContent() [key: " + key + "]");
             response = cached.get(REQUEST_TIMEOUT.get(), TimeUnit.SECONDS);
             if (log.isTraceEnabled()) log.trace("cached.getContent() returned [key: " + key + "]");
         } catch (InterruptedException e) {
             log.error("Error writing Fragment.getContent() to page",e);
             response = "Sorry, fetching from the cache took longer than we expected.  Please refresh the page.";
         }
 
         // Write the response
         try {
             if (log.isTraceEnabled()) log.trace("writing output [key: " + key + "]");
             pageContext.getOut().write(response);
         } catch (IOException e) {
             log.error("Error writing Fragment.getContent() to page",e);
         }
         
         long duration = System.currentTimeMillis() - start;
 
         if (duration > WARNING_COMPUTE_DURATION.get()) {
             log.warn("Warning: slow FragmentCache, " + duration + "ms [key: " + key + "]");
         } else if (log.isTraceEnabled()) {
             log.trace("FragmentCache completed in " + duration + "ms [key: " + key + "]");
         }
         
         return SKIP_BODY;
     }
 
     /**
      *  Write the content to the apge, try to acquire lock and update, release lock,
      *  then return SKIP_BODY when complete
      */
     @Override
     public int doAfterBody() throws JspException {
 
         // Get the BodyContent, the result of the processing of the body of the tag
         BodyContent body = getBodyContent();
         String content   = body.getString();
 
         Fragment cached = cache.get(key);
         
         // Update the Fragment, doing this before writing to the page will release
         // waiting threads sooner.
         if (!disable) {
             if (log.isTraceEnabled()) log.trace("doAfterBody Getting Fragment [key: " + key + "]");
 
             // Get a reference to the Fragment, then try to update it.
             if (cached == null) {
                 log.warn("Fragment for key[" + key + "] became null between doStartTag and doAfterBody, probably removed by cleaner");
             } else {
                 if (!cached.tryUpdateAndRelease(content, condition)) {
                     log.warn("Failed to update Fragment[key=" + key + "], unable to obtain lock.  Fragment must have changed since doStartTag.");
                 } else if (log.isTraceEnabled()) {
                     log.trace("Cache updated and lock released [key: " + key + "]");
                 }
             }
             
         } else if (log.isTraceEnabled()) {
             log.trace("Disable set, not caching [key: " + key + "]");
         }
         
         // Attempt to write the body to the page
         try {
             if (log.isTraceEnabled()) log.trace("Writing to page [key: " + key + "]");
             body.getEnclosingWriter().write(content);
             if (log.isTraceEnabled()) log.trace("BodyContent rendered [key: " + key + "]");
         } catch (IOException e) {
             log.error("Error writing bodyContent to page",e);
         }
 
         return SKIP_BODY;
     }
 
     @Override
     public int doEndTag() throws JspException {
         Fragment fragment = cache.get(key);
         if (fragment != null) {
             fragment.tryRelease();
         }
         return EVAL_PAGE;
     }
 
     /**
      * Non-blocking method, so two threads could theoretically schedule the cleaner
      * at the same time and not interfere with each other.
      *
      * @param interval The time interval
      * @param unit The time unit the interval is in
      */
     public static void scheduleCleaner(long interval, TimeUnit unit, boolean runNow) {
 
         if (cleanerFuture != null)
             cleanerFuture.cancel(false);
 
         if (runNow)
             cleaner.run();
 
         cleanerFuture = executor.scheduleAtFixedRate(cleaner, interval, interval, unit);
     }
 }
