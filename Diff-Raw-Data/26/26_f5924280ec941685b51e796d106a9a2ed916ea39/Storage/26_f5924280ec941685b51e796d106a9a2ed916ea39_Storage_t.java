 /*
  * Copyright 2012 zhongl
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package com.github.zhongl.ipage;
 
 import com.github.zhongl.util.Entry;
 import com.github.zhongl.util.Nils;
 import com.google.common.base.Function;
import com.google.common.base.Predicate;
 import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
 import com.google.common.io.Files;
 import com.google.common.util.concurrent.FutureCallback;
 import org.softee.management.annotation.*;
 
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static com.google.common.base.Preconditions.checkState;
 
 /** @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a> */
 @MBean
 public class Storage<V> implements Iterable<V> {
 
     private static final int DEFRAG_RATIO = Integer.getInteger("ipage.snapshot.defrag.ratio", 7);
 
     private final File head;
     private final File pages;
     private final Codec<V> codec;
 
     private final AtomicInteger total;
     private final AtomicInteger removed;
     private final AtomicInteger defragRatio;
 
     private volatile long lastMergeElapseMillis;
     private volatile Snapshot<V> snapshot;
 
 
     public Storage(File dir, Codec<V> codec) throws IOException {
         this.codec = codec;
         this.head = new File(dir, "HEAD");
         this.pages = tryMkdir(new File(dir, "pages"));
 
         this.snapshot = loadHead();
         this.total = new AtomicInteger(0);
         this.removed = new AtomicInteger(0);
         this.defragRatio = new AtomicInteger(DEFRAG_RATIO);
 
         snapshot.foreachIndexEntry(new Function<Boolean, Void>() {
             @Override
             public Void apply(@Nullable Boolean alived) {
                 total.incrementAndGet();
                 if (!alived) removed.incrementAndGet();
                 return Nils.VOID;
             }
         });
     }
 
     @ManagedAttribute
     public int getSize() { return snapshot.size(); }
 
     @ManagedAttribute
     public long getLastMergeElapseMillis() { return lastMergeElapseMillis; }
 
     @ManagedAttribute
     public int getAlives() { return total.get() - removed.get(); }
 
     @ManagedOperation
     @Description("positive delta for up, negative delta for down, then return the final ratio which must be in [1,9].")
     public int defragRatio(@Parameter("delta") int delta) {
         if (delta == 0) return defragRatio.get();
         int current = defragRatio.get();
         int next = Math.min(9, Math.max(1, current + delta));
         while (!defragRatio.compareAndSet(current, next)) {}
         return next;
     }
 
    public void merge(Collection<Entry<Key, V>> appendings, Collection<Key> removings, FutureCallback<Void> flushedCallback) {
         if (appendings.isEmpty() && removings.isEmpty()) {
             flushedCallback.onSuccess(Nils.VOID);
             return;
         }
 
        removings = Collections2.filter(removings, new Predicate<Key>() {
            @Override
            public boolean apply(@Nullable Key key) {
                return get(key) != null;
            }
        });

         int cTotal = total.addAndGet(appendings.size());
         int cRemoved = removed.addAndGet(removings.size());
 
         boolean needDefrag = cRemoved * 1.0 / cTotal >= defragRatio.get() * 0.1;
 
         // TODO log merge starting
         try {
             Stopwatch stopwatch = new Stopwatch().start();
             String snapshotName;
             if (needDefrag) {
                 int alive = cTotal - cRemoved;
                 snapshotName = snapshot.defrag(appendings, removings, alive);
                 total.set(alive);
                 removed.set(0);
             } else {
                 snapshotName = snapshot.append(appendings, removings);
             }
             lastMergeElapseMillis = stopwatch.elapsedMillis();
 
             setHead(snapshotName);
             snapshot = new Snapshot<V>(pages, snapshotName, codec);
             flushedCallback.onSuccess(Nils.VOID);
             // TODO log merge ending
         } catch (Throwable t) {
             // TODO log merge error
             flushedCallback.onFailure(t);
         } finally {
             cleanUp();
         }
     }
 
     public V get(Key key) { return snapshot.get(key); }
 
     @Override
     public Iterator<V> iterator() { return snapshot.iterator(); }
 
     private File tryMkdir(File dir) {
         if (!dir.exists()) checkState(dir.mkdirs());
         return dir;
     }
 
     protected void setHead(String snapshotName) throws IOException {
         Files.write(snapshotName + "\n", head, Charset.defaultCharset());
     }
 
     protected Snapshot<V> loadHead() throws IOException {
         if (!head.exists()) return new Snapshot<V>(pages, "null.s", codec);
         String name = Files.readFirstLine(head, Charset.defaultCharset());
         return new Snapshot<V>(pages, name, codec);
     }
 
     protected void cleanUp() {
         File[] files = pages.listFiles();
         for (File file : files) {
             if (!snapshot.isLinkTo(file)) checkState(file.delete());
         }
     }
 
 
 }
