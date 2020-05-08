 /*
  * LazyCache.java -
  * Copyright (C) 2003 Klaus Rennecke, all rights reserved.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense, and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package net.sourceforge.fraglets.zeig.cache;
 
 
 /**
  * @author marion@users.sourceforge.net
 * @version $Revision: 1.5 $
  */
 public class LazyCache implements SimpleCache {
     protected CacheEntry entries[];
     
     private int initialSize;
     
    private int fill;
    private int drop;
     
     public LazyCache() {
         init(16);
     }
     
     private void init(int size) {
         this.initialSize = size;
         this.entries = new CacheEntry[size];
         this.fill = 0;
         this.drop = 0;
     }
     
     private final int index(int hash, int length) {
         return hash & (length - 1);
     }
     
     private final CacheEntry get(int hash) {
         // avoid synchronisation
         CacheEntry entries[] = this.entries;
         return entries[index(hash, entries.length)];
     }
     
     public CacheEntry get(int hash, Object key) {
        // avoid synchronisation
         CacheEntry candidate = get(hash);
         if (candidate != null && candidate.equals(key)) {
             return candidate;
         } else {
             return null;
         }
     }
     
     public CacheEntry get(int hash, int key) {
        // avoid synchronisation
         CacheEntry candidate = get(hash);
         if (candidate != null && candidate.equals(key)) {
             return candidate;
         } else {
             return null;
         }
     }
     
     public final void put(CacheEntry entry) {
         // avoid synchronisation
         CacheEntry entries[] = this.entries;
         int index = index(entry.hashCode(), entries.length);
         if (entries[index] == null) {
             fill++;
        } else if (fill + fill / 2 >= entries.length) {
            entries = grow();
         } else {
             drop++;
         }
         entries[index] = entry;
     }
     
     private synchronized CacheEntry[] grow() {
         int count = 0;
         CacheEntry less[] = entries;
         CacheEntry more[] = new CacheEntry[less.length << 1];
         for (int i = 0; i < less.length; i++) {
             CacheEntry entry = less[i];
             if (entry != null) {
                 int index = index(entry.hashCode(), more.length);
                 if (more[index] == null) {
                     count++;
                 }
                 more[index] = entry;
             }
         }
         
         this.drop += this.fill - count; 
         this.fill = count;
        this.entries = more;
        
        return more;
     }
     
     /**
      * @see net.sourceforge.fraglets.zeig.cache.SimpleCache#clear()
      */
     public void clear() {
         init(initialSize);
     }
 
     /**
      * @see net.sourceforge.fraglets.zeig.cache.SimpleCache#geSize()
      */
     public int getSize() {
         return entries.length;
     }
 
     /**
      * @see net.sourceforge.fraglets.zeig.cache.SimpleCache#getFill()
      */
     public int getFill() {
         return this.fill;
     }
 
     /* (non-Javadoc)
      * @see net.sourceforge.fraglets.zeig.cache.SimpleCache#getDrop()
      */
     public int getDrop() {
         return this.drop;
     }
 
 }
