 /*******************************************************************************
  *
  * Copyright (c) 2004-2013 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Kohsuke Kawaguchi, Tom Huybrechts, Roy Varghese
  *
  *
  *******************************************************************************/
 
 package hudson.model;
 
 import com.google.common.base.Function;
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.thoughtworks.xstream.converters.Converter;
 import com.thoughtworks.xstream.converters.MarshallingContext;
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
 import com.thoughtworks.xstream.io.xml.XppReader;
 import hudson.Extension;
 import hudson.model.listeners.RunListener;
 import hudson.tasks.test.AbstractTestResultAction;
 import hudson.util.AtomicFileWriter;
 import hudson.util.XStream2;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.lang.StringUtils;
 
 /**
  * {@link Map} from build number to {@link Run}.
  *
  * <p> This class is multi-thread safe by using copy-on-write technique, and it
  * also updates the bi-directional links within {@link Run} accordingly.
  *
  * @author Kohsuke Kawaguchi
  */
 public final class RunMap<J extends Job<J, R>, R extends Run<J, R>> 
     extends AbstractMap<Integer, R> 
     implements SortedMap<Integer, R>, BuildHistory<J,R> {
 
     final public XStream2 xstream = new XStream2();
     
     final private Function<RunValue<J,R>, Record<J,R>> RUNVALUE_TO_RECORD_TRANSFORMER = 
         new Function<RunValue<J,R>, Record<J,R>>() {
 
             @Override
             public Record<J, R> apply(RunValue<J, R> input) {
                 return input;
             }
 
         };
 
     @Override
     public Iterator<Record<J, R>> iterator() {
         return Iterators.transform(builds.values().iterator(), RUNVALUE_TO_RECORD_TRANSFORMER);
     }
     
     @Override
     public List<Record<J,R>> allRecords() {
         return ImmutableList.copyOf(iterator());
     }
     
     private enum BuildMarker {
         LAST_COMPLETED,
         LAST_SUCCESSFUL,
         LAST_UNSUCCESSFUL,
         LAST_FAILED,
         LAST_STABLE,
         LAST_UNSTABLE,
     }
     
     private transient HashMap<BuildMarker, RunValue<J,R>> 
             buildMarkersCache = new HashMap<BuildMarker, RunValue<J,R>>();
     
     // copy-on-write map
 
     private SortedMap<Integer, RunValue<J,R>> builds;
     
     
     // A cache of build objects
     private final transient LazyRunValueCache runValueCache = new LazyRunValueCache();
     
     private volatile Map<Integer, Long> buildsTimeMap = new HashMap<Integer, Long>();
     
     
     private transient File persistenceFile;
     
     // Marker to indicate if this objects need to be saved to disk
     private transient volatile boolean dirty;
     
     // Reference to parent job.
     final private J parent;
 
     public RunMap(J parent) {
         this.parent = parent;
         
         builds = new TreeMap<Integer, RunValue<J,R>>(BUILD_TIME_COMPARATOR);
         
         // Initialize xstream
         xstream.alias("buildHistory", RunMap.class);
         xstream.alias("builds", SortedMap.class);
         xstream.alias("build", LazyRunValue.class);
         xstream.alias("build", EagerRunValue.class);
         
     }
 
     public Set<Entry<Integer, R>> entrySet() {
         return Maps.transformValues(builds, RUNVALUE_TO_RUN_TRANSFORMER).entrySet();
     }
 
     public synchronized R put(R value) {
         return put(value.getNumber(), value);
     }
 
     @Override
     public synchronized R put(Integer key, R value) {
         // copy-on-write update
         TreeMap<Integer, RunValue<J,R>> m = new TreeMap<Integer, RunValue<J,R>>(builds);
 
         buildsTimeMap.put(key, value.getTimeInMillis());
         final EagerRunValue erv = new EagerRunValue(this, value);
         RunValue<J,R> r = update(m, key, erv);
         
         // Save the build, so that we can reload it later.
         // For now, the way to figure out if its saved is to check if the config file
         // exists.
         
         if ( !buildXmlExists(erv.buildDir())) {
             try {
                 value.save();
             } catch (IOException ex) {
                 LOGGER.warning("Unable to save build.xml to " + erv.buildDir().getPath());
                 // Not fatal, unless build object reference is released by
                 // Hudson, in which case it won't be loaded again unless
                 // it has actually started running.
             }
         }
 
         setBuilds(Collections.unmodifiableSortedMap(m));
         
         return r!= null? r.getBuild(): null;
     }
 
     private static boolean buildXmlExists(File buildDir) {
         return new File(buildDir, "build.xml").exists();
     }
 
     @Override
     public synchronized void putAll(Map<? extends Integer, ? extends R> rhs) {
         throw new UnsupportedOperationException("Not implemented");
     }
     
     private synchronized void setBuilds(SortedMap<Integer, RunValue<J,R>> map) {
         this.builds = map;
         
         recalcMarkers();
         
         saveToRunMapXml();
     }
     
     private synchronized void recalcMarkers() {
         recalcLastSuccessful();
         recalcLastUnsuccessful();
         recalcLastCompleted();
         recalcLastFailed();
         recalcLastStable();
         recalcLastUnstable();
     }
     
     private synchronized void putAllRunValues(SortedMap<Integer, RunValue<J,R>> lhs,
                                               SortedMap<Integer, RunValue<J,R>> rhs) {
         TreeMap<Integer, RunValue<J,R>> m = new TreeMap<Integer, RunValue<J,R>>(lhs);
         buildsTimeMap.clear();
         
         for (Map.Entry<Integer, RunValue<J,R>> e : rhs.entrySet()) {
             RunValue<J,R> runValue = e.getValue();
             
             buildsTimeMap.put(e.getKey(), runValue.timeInMillis);
             update(m, e.getKey(), runValue);
         }
         
         setBuilds(Collections.unmodifiableSortedMap(m));
         
     }
 
     private RunValue<J,R> update(TreeMap<Integer, RunValue<J,R>> m, Integer key, RunValue<J,R> value) {
         assert value != null;
         // things are bit tricky because this map is order so that the newest one comes first,
         // yet 'nextBuild' refers to the newer build.
         RunValue<J,R> first = m.isEmpty() ? null : m.get(m.firstKey());
         RunValue<J,R> runValue = m.put(key, value);
 //        R r = runValue != null? runValue.get(): null;
         SortedMap<Integer, RunValue<J,R>> head = m.headMap(key);
         if (!head.isEmpty()) {
             if(m.containsKey(head.lastKey())) {
                 RunValue<J,R> prev = m.get(head.lastKey());
                 value.setPrevious(prev.getPrevious());
                 value.setNext(prev);
                 if (m.containsValue(value.getPrevious())) {
                         value.getPrevious().setNext(value);
                 }
                 prev.setPrevious( value);
             }
         } else {
             value.setPrevious( first);
             value.setNext(null);
             if (m.containsValue(first)) {
                 first.setNext( value);
             }
         }
         return runValue;
     }
 
     public synchronized boolean remove(R run) {
         
         Integer buildNumber = run.getNumber();
         RunValue<J,R> runValue = builds.get(buildNumber);
         if ( runValue == null ) {
             return false;
         }
         
         final RunValue<J,R> next = runValue.getNext();
         if ( next != null) {
             next.setPrevious( runValue.getPrevious());
         }
         
         final RunValue<J,R> prev = runValue.getPrevious();
         if ( prev != null) {
             prev.setNext( runValue.getNext());
         }
 
         // copy-on-write update
         // This block is not thread safe
         TreeMap<Integer, RunValue<J,R>> m = new TreeMap<Integer, RunValue<J,R>>(builds);
         buildsTimeMap.remove(buildNumber);
         
         RunValue<J,R> r = m.remove(buildNumber);
 
         if ( r instanceof BuildNavigable) {
             ((BuildNavigable)run).setBuildNavigator(null);
         }
         
         setBuilds(Collections.unmodifiableSortedMap(m));
         
         return r != null;
     }
 
     public synchronized void reset(TreeMap<Integer, RunValue<J,R>> map) {
         putAllRunValues(builds, map);
     }
 
     
     /**
      * Gets the read-only view of this map.
      */
     public SortedMap<Integer, R> getView() {
         return Maps.transformValues(builds, RUNVALUE_TO_RUN_TRANSFORMER);
     }
 
 //
 // SortedMap delegation
 //
     public Comparator<? super Integer> comparator() {
         return builds.comparator();
     }
 
     public SortedMap<Integer, R> subMap(Integer fromKey, Integer toKey) {
         return Maps.transformValues(builds.subMap(fromKey, toKey), RUNVALUE_TO_RUN_TRANSFORMER);
 //        return new ReadonlySortedMap<J,R>(builds.subMap(fromKey, toKey));
     }
 
     public SortedMap<Integer, R> headMap(Integer toKey) {
         return Maps.transformValues(builds.headMap(toKey), RUNVALUE_TO_RUN_TRANSFORMER);
 //        return new ReadonlySortedMap<J,R>((builds.headMap(toKey)));
     }
 
     public SortedMap<Integer, R> tailMap(Integer fromKey) {
         return Maps.transformValues(builds.tailMap(fromKey), RUNVALUE_TO_RUN_TRANSFORMER);
 //        return new ReadonlySortedMap<J,R>(builds.tailMap(fromKey));
     }
     
     /**
      * Callers must first check that map is not empty.
      * @return 
      * @throws NoSuchElementException if map is empty
      */
     public Integer firstKey() {
         return builds.firstKey();
     }
 
     /**
      * Callers must first check that map is not empty.
      * @return 
      * @throws NoSuchElementException if map is empty
      */
     public Integer lastKey() {
         return builds.lastKey();
     }
     
     public static final Comparator<Comparable> COMPARATOR = new Comparator<Comparable>() {
         public int compare(Comparable o1, Comparable o2) {
             return -o1.compareTo(o2);
         }
     };
     /**
      * Compare Build by timestamp
      */
     private Comparator<Integer> BUILD_TIME_COMPARATOR = new Comparator<Integer>() {
         public int compare(Integer i1, Integer i2) {
             Long date1 = buildsTimeMap.get(i1);
             Long date2 = buildsTimeMap.get(i2);
             if (null == date1 || null == date2) {
                 return COMPARATOR.compare(i1, i2);
             }
             return -date1.compareTo(date2);
         }
     };
 
     
     @Override
     public RunValue<J,R> getFirst() {
         if (builds.isEmpty()) {
             return null;
         }
         return builds.get(builds.lastKey());
     }
         
     @Override
     public RunValue<J,R> getLast() {
         if (builds.isEmpty()) {
             return null;
         }
         return builds.get(builds.firstKey());
     }
     
     @Override
     public R getLastBuild() {
         final RunValue<J,R> runValue = getLast();
         return runValue != null? runValue.getBuild(): null;
     }
 
     @Override
     public R getFirstBuild() {
         final RunValue<J,R> runValue = getFirst();
         return runValue != null? runValue.getBuild(): null;
     }
     
     private void recalcLastSuccessful() {
         RunValue<J,R> r = getLast();
         while (r != null && 
                 (r.isBuilding() || 
                  r.getResult() == null || 
                  r.getResult().isWorseThan(Result.UNSTABLE))) {
             r = r.getPrevious();
         }
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_SUCCESSFUL, r);
         if ( r != old) {
             markDirty(true);
         }
     }
     
     @Override
     public RunValue<J,R> getLastSuccessful() {
         return buildMarkersCache.get(BuildMarker.LAST_SUCCESSFUL);
     }
 
 
     @Override
     public R getLastSuccessfulBuild() {
         RunValue<J,R> r = getLastSuccessful();
         return r !=null? r.getBuild(): null;
         
         
     }
 
     private void recalcLastUnsuccessful() {
         RunValue<J,R> r = getLast();
         while (r != null
                 && (r.isBuilding() || r.getResult() == Result.SUCCESS)) {
             r = r.getPrevious();
         }
 
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_UNSUCCESSFUL, r);
         if ( old != r) {
             markDirty(true);
         }
     }
     
     @Override
     public RunValue<J,R> getLastUnsuccessful() {
         return buildMarkersCache.get(BuildMarker.LAST_UNSUCCESSFUL);
     }
     
     @Override
     public R getLastUnsuccessfulBuild() {
         RunValue<J,R> r = getLastUnsuccessful();
         return r!= null? r.getBuild(): null;
     }
 
     private void recalcLastUnstable() {
         RunValue<J,R> r = getLast();
         while (r != null
                 && (r.isBuilding() || r.getResult() != Result.UNSTABLE)) {
             r = r.getPrevious();
         }
 
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_UNSTABLE,r);
         if ( old != r ) {
             markDirty(true);
         }
     }
     
     @Override
     public RunValue<J,R> getLastUnstable() {
         return buildMarkersCache.get(BuildMarker.LAST_UNSTABLE);
     }
 
     @Override
     public R getLastUnstableBuild() {
         RunValue<J,R> r = getLastUnstable();
         return r != null? r.getBuild(): null;
     }
 
     private void recalcLastStable() {
         RunValue<J,R> r = getLast();
         while (r != null && 
                 (r.isBuilding() || 
                  r.getResult().isWorseThan(Result.SUCCESS))) {
             r = r.getPrevious();
         }
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_STABLE,r);
         if ( old != r ) {
             markDirty(true);
         }
     }
     
     @Override
     public RunValue<J,R> getLastStable() {
 
         return buildMarkersCache.get(BuildMarker.LAST_STABLE);
     }
     
     @Override
     public R getLastStableBuild() {
         RunValue<J,R> r = getLastStable();
         return r != null? r.getBuild(): null;
     }
 
     private void recalcLastFailed() {
         RunValue<J,R> r = getLast();
         while (r != null && (r.isBuilding() || r.getResult() != Result.FAILURE)) {
             r = r.getPrevious();
         }
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_FAILED, r);
         if (old != r) {
             markDirty(true);
         }
     }
     
     @Override
     public RunValue<J,R> getLastFailed() {
         return buildMarkersCache.get(BuildMarker.LAST_FAILED);
     }
     
     @Override
     public R getLastFailedBuild() {
         RunValue<J,R> r = getLastFailed();
         return r != null? r.getBuild(): null;
     }
     
     private void recalcLastCompleted() {
         RunValue<J,R> r = getLast();
         while (r != null && r.isBuilding()) {
             r = r.getPrevious();
         }
 
         RunValue<J,R> old = buildMarkersCache.put(BuildMarker.LAST_COMPLETED, r);
         if (old != r) {
             markDirty(true);
         }        
     }
 
     @Override
     public RunValue<J,R> getLastCompleted() {
         return buildMarkersCache.get(BuildMarker.LAST_COMPLETED);
     }
     
     @Override
     public R getLastCompletedBuild() {
         RunValue<J,R> r = getLastCompleted();
         return r != null? r.getBuild(): null;
     }
 
     @Override
     public List<R> getLastBuildsOverThreshold(int numberOfBuilds, Result threshold) {
         final List<Record<J,R>> records = getLastRecordsOverThreshold(numberOfBuilds, threshold);
         
         return Lists.transform(records, new Function<Record<J,R>, R>() {
 
             @Override
             public R apply(Record<J, R> input) {
                 return input.getBuild();
             }
             
         });
         
     }
 
     
     @Override
     public List<Record<J, R>> getLastRecordsOverThreshold(int numberOfRecords, Result threshold) {
         List<Record<J,R>> result = new ArrayList<Record<J,R>>(numberOfRecords);
 
         RunValue<J,R> r = getLast();
         while (r != null && result.size() < numberOfRecords) {
 
             if (!r.isBuilding() && 
                 (r.getResult() != null && 
                  r.getResult().isBetterOrEqualTo(threshold))) {
 
                 result.add(r);
             }
             r = r.getPrevious();
         }
 
         return result; 
     }
 
     /**
      * {@link Run} factory.
      */
     public interface Constructor<R extends Run<?, R>> {
 
         R create(File dir) throws IOException;
     }
 
     /**
      * Fills in {@link RunMap} by loading build records from the file system.
      *
      * @param job Job that owns this map.
      * @param cons Used to create new instance of {@link Run}.
      */
     public synchronized void load(J job, Constructor<R> cons) {
         // If saved Runmap exists, load from that.
         File buildDir = job.getBuildDir();
         persistenceFile = new java.io.File(buildDir, "_runmap.xml");
         
         if ( !loadFromRunMapXml(job, cons)) {
         
             final SimpleDateFormat formatter = Run.ID_FORMATTER.get();
 
             TreeMap<Integer, RunValue<J,R>> m = new TreeMap<Integer, RunValue<J,R>>(BUILD_TIME_COMPARATOR);
             
             buildDir.mkdirs();
             String[] buildDirs = buildDir.list(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     // HUDSON-1461 sometimes create bogus data directories with impossible dates, such as year 0, April 31st,
                     // or August 0th. Date object doesn't roundtrip those, so we eventually fail to load this data.
                     // Don't even bother trying.
                     if (!isCorrectDate(name)) {
                         LOGGER.fine("Skipping " + new File(dir, name));
                         return false;
                     }
                     return !name.startsWith("0000") && new File(dir, name).isDirectory();
                 }
 
                 private boolean isCorrectDate(String name) {
                     try {
                         if (formatter.format(formatter.parse(name)).equals(name)) {
                             return true;
                         }
                     } catch (ParseException e) {
                         // fall through
                     }
                     return false;
                 }
             });
 
             for (String build : buildDirs) {
                 
                if (buildXmlExists(new File(buildDir, build))) {
                     // if the build result file isn't in the directory, ignore it.
                     try {
                         RunValue<J,R> lzRunValue = new LazyRunValue<J,R>(this, buildDir, build, cons);
                         
                         R b = lzRunValue.getBuild();
                         long timeInMillis = b.getTimeInMillis();
                         buildsTimeMap.put(b.getNumber(), timeInMillis);
                         lzRunValue.timeInMillis = timeInMillis;
                         m.put(b.getNumber(), lzRunValue);
                     } catch (InstantiationError e) {
                         e.printStackTrace();
                     }
                 }
             }
             
             reset(m);
         }
 
     }
     
     private synchronized void markDirty(boolean value) {
         this.dirty = value;
         if ( !dirty ) {
             // mark down
             for (RunValue rv: builds.values()) {
                 rv.markDirty(false);
             }
         }
     }
     
     private boolean isDirty() {
         return dirty;
     }
     
     private synchronized void saveToRunMapXml() {
         if (!isDirty() || persistenceFile == null) {
             return;
         }
         
         AtomicFileWriter w = null;
         try {
             w = new AtomicFileWriter(persistenceFile);
             w.write("<?xml version='1.0' encoding='UTF-8'?>\n");
             xstream.toXML(this, w);
             w.commit();
             markDirty(false);
         } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, "Cannot write RunMap.xml", ex);
         } finally {
             if ( w != null) {
                 try { w.abort();} catch (IOException ex) {};
             }
         }
         
     }
     
     private synchronized boolean loadFromRunMapXml(J job, Constructor<R> cons) {
         
         assert persistenceFile != null;
         
         Reader r = null;
         if ( persistenceFile.exists()) {
             try {
                  r = new BufferedReader(new InputStreamReader(new FileInputStream(persistenceFile), "UTF-8"));
 
                 xstream.unmarshal(new XppReader(r), this);
                 
                 // Fix up all the parent and constructor references
                 File buildDir = persistenceFile.getParentFile();
                 
                 boolean wasBuilding = false;
                 for (RunValue<J,R> rv: builds.values()) {
                     assert rv instanceof LazyRunValue;
                     LazyRunValue<J,R> lrv = (LazyRunValue<J,R>) rv;
                     lrv.key.ctor = cons;
                     if ( lrv.isBuilding()) {
                         lrv.sync();
                         wasBuilding = true;
                     }
                 }
                 
                 // If any builds were still building when file was last persisted
                 // update runMap with new status and save the file again.
                 if ( wasBuilding ) {
                     recalcMarkers();
                     saveToRunMapXml();
                 }
                 
                 return true;
             } catch (FileNotFoundException ex) {
                 LOGGER.log(Level.SEVERE, "Cannot read _runmap.xml", ex);
                 
             } catch (UnsupportedEncodingException ex) {
                 LOGGER.log(Level.SEVERE, "Cannot read _runmap.xml", ex);
                 persistenceFile.delete();
             }
             finally {
                 if ( r != null ) {
                     try { r.close(); } catch (Exception e) {}
                 }
             }
             
         }
         
         return false;
     }
     
     private LazyRunValueCache runValueCache() {
         return this.runValueCache;
     }
     
     private static class LazyRunValueCache {
         
         final private LoadingCache<LazyRunValue.Key, Run> cache;
         final static int EVICT_BUILD_IN_SECONDS = 60;
         final static int MAX_ENTRIES = 10000;
         
         private LazyRunValueCache() {
             
             cache = CacheBuilder.newBuilder()
                         .expireAfterAccess(EVICT_BUILD_IN_SECONDS, TimeUnit.SECONDS)
                         .initialCapacity(1024)
                         .maximumSize(MAX_ENTRIES)
                         .softValues()
                         .build( new CacheLoader<LazyRunValue.Key, Run>() {
 
                             @Override
                             public Run load(LazyRunValue.Key key) throws Exception {
 
                                 LazyRunValue.Key k = (LazyRunValue.Key)key;
                                 Run r = k.ctor.create(k.referenced.buildDir());
                                 if ( r instanceof BuildNavigable) {
                                     ((BuildNavigable)r).setBuildNavigator(k.referenced);
                                 }
                                 // Cannot call onLoad() here, it will try
                                 // to query a mutating cache. So just mark it
                                 // for refresh.
                                 k.refreshed = true;
                                 return r;
                                 
                             }
                             
                         });
         }
         
         private Run get(LazyRunValue.Key key) {
             try {
                 return cache.get(key);
             } catch (ExecutionException ex) {
                 LOGGER.log(Level.SEVERE,"Unable to load build", ex);
                 return null;
             }
 
         }
     }
     
     static abstract class RunValue<J extends Job<J,R>, R extends Run<J,R>> 
         implements BuildHistory.Record<J,R> {
         
         private transient RunMap<J,R> runMap;
         
         long timeInMillis;
         long duration;
         String fullDisplayName;
         String displayName;
         String url;
         String builtOnStr;
         
         private RunValue<J,R> previous;
         private RunValue<J,R> next;
         boolean isBuilding;
         boolean isLogUpdated;
         Result result;
         Run.State state;
         private transient boolean dirty;
         int buildNumber;
         
         RunValue() {
         }
         
         protected void sync() {
             R build = getBuild();
             if ( build == null ) {
                 return;
             }
             setBuildNumber( build.getNumber());
             setResult( build.getResult());
             setState( build.getState());
             setBuilding( build.isBuilding());
             setLogUpdated( build.isLogUpdated());
             setTimeInMillis( build.getTimeInMillis());
             setDisplayName( build.getDisplayName());
             setDuration( build.getDuration());
             
             if ( build instanceof AbstractBuild) {
                 setBuiltOnNodeName(((AbstractBuild)build).getBuiltOnStr());
                 setFullDisplayName( build.getFullDisplayName());
                 setUrl( build.getUrl());
             }
             
         }
         
         abstract File buildDir();
         
         
         String relativeBuildDir(File buildsDir) {
             return buildsDir.toURI().relativize(buildDir().toURI()).getPath();
         }
         
         public void setBuildNumber(int number) {
             this.buildNumber = number;
             
         }
         
         
         public void setRunMap(RunMap runMap) {
             this.runMap = runMap;
         }
         
         protected RunMap runMap() {
             return runMap;
         }
         
         void setTimeInMillis(long millis) {
             if ( this.timeInMillis == millis) {
                 return;
             }
             this.timeInMillis = millis;
             markDirty(true);
         }
         
         void setDuration(long duration) {
             if ( this.duration == duration) {
                 return;
             }
             this.duration = duration;
             markDirty(true);
         }
         
         void setDisplayName(String name) {
             if ( StringUtils.equals(this.displayName, name)) {
                 return;
             }
             this.displayName = name;
             markDirty(true);
         }
         
         void setFullDisplayName(String name) {
             if ( StringUtils.equals(this.fullDisplayName, name)) {
                 return;
             }
             this.fullDisplayName = name;
             markDirty(true);
         }
         
         void setUrl(String url) {
             if ( StringUtils.equals(this.url, url)) {
                 return;
             }
             this.url = url;
             markDirty(true);
         }
         
         void setBuiltOnNodeName(String builtOn) {
             if ( StringUtils.equals(this.builtOnStr, builtOn)) {
                 return;
             }
             
             this.builtOnStr = builtOn;
             markDirty(true);
         }
         
         private void markDirty(boolean dirty) {
             this.dirty = dirty;
             if ( dirty ) {
                 // Dirty up
                 runMap.markDirty(true);
             }
         }
         
         private boolean isDirty() {
             return dirty;
         }
         
         void setResult(Result result) {
             if ( result == this.result) {
                 return;
             }
             this.result = result;
             markDirty(true);
         }
         
         void setState(Run.State state) {
             if ( state == this.state) {
                 return;
             }
             this.state = state;
             markDirty(true);
         }
         
         void setLogUpdated(boolean value) {
             if (this.isLogUpdated == value) {
                 return;
             }
             this.isLogUpdated = value;
             markDirty(true);
         }
         
         void setBuilding(boolean value) {
             if (this.isBuilding == value) {
                 return;
             }
             this.isBuilding = value;
             markDirty(true);
         }
         
         
         void setPrevious(RunValue<J,R> previousRunValue) {
             if (this.previous == previousRunValue) {
                 return;
             }
             this.previous = previousRunValue;
             markDirty(true);
         }
         
         void setNext(RunValue<J,R> nextRunvalue) {
             if (this.next == nextRunvalue) {
                 return;
             }
             this.next = nextRunvalue;
             markDirty(true);
         }
         
         @Override
         public int getNumber() {
             return buildNumber;
         }
         
         @Override
         public String getDisplayName() {
             return displayName;
         }
         
         @Override
         public long getTimeInMillis() {
             return timeInMillis;
         }
         
         @Override
         public Calendar getTimestamp() {
             GregorianCalendar c = new GregorianCalendar();
             c.setTimeInMillis(getTimeInMillis());
             return c;
         }
         
         @Override
         public Date getTime() {
             return new Date(getTimeInMillis());
         }
         
         @Override
         public long getDuration() {
             return duration;
         }
         
         @Override
         public String getUrl() {
             return url;
         }
         
         @Override
         public String getFullDisplayName() {
             return fullDisplayName;
         }
         
         @Override 
         public String getBuiltOnNodeName() {
             return builtOnStr;
         }
             
         @Override
         public RunValue<J,R> getPrevious() {
             return previous;
         }
         
         @Override
         public RunValue<J,R> getNext() {
             return next;
         }
         
         @Override
         public Result getResult() {
             return result;
         }
 
         @Override
         public J getParent() {
             return runMap.parent;
         }
          
         @Override
         public R getPreviousBuild() {
             RunValue<J,R> v = getPrevious();
             return v != null? v.getBuild(): null;
         }
 
         @Override
         public R getNextBuild() {
             RunValue<J,R> v = getNext();
             return v != null? v.getBuild(): null;
         }
 
                         
         public boolean isBuilding() {
             return isBuilding;
         }
         
         public boolean isLogUpdated() {
             return isLogUpdated;
         }
         
         @Override
         public R getPreviousCompletedBuild() {
             RunValue<J,R> v = getPreviousCompleted();
             return v != null? v.getBuild(): null;
         }
         
         @Override
         public RunValue<J,R> getPreviousCompleted() {
             RunValue<J,R> v = getPrevious();
             while (v != null && v.isBuilding()) {
                 v = v.getPrevious();
             }
             return v;
         }
         
         @Override
         public R getPreviousBuildInProgress() {
             RunValue<J,R> v = getPreviousInProgress();
             return v != null? v.getBuild(): null;
         }
         
         @Override
         public RunValue<J,R> getPreviousInProgress() {
             RunValue<J,R> v = getPrevious();
             
             while ( v != null && !v.isBuilding()) {
                 v = v.getPrevious();
             }
             
             return v;
         }
 
         @Override
         public R getPreviousBuiltBuild() {
             RunValue<J,R> v = getPreviousBuilt();
             return v != null? v.getBuild(): null;
         }
         
         @Override
         public RunValue<J,R> getPreviousBuilt() {
             RunValue<J,R> v = getPrevious();
             // in certain situations (aborted m2 builds) v.getResult() can still be null, although it should theoretically never happen
             while (v != null && (v.getResult() == null || v.getResult() == Result.NOT_BUILT)) {
                 v = v.getPrevious();
             }
             return v;
         }
 
         @Override
         public R getPreviousNotFailedBuild() {
             RunValue<J,R> v = getPreviousNotFailed();
             return v!= null? v.getBuild(): null;
         }
         
         @Override
         public RunValue<J,R> getPreviousNotFailed() {
             RunValue<J,R> v = getPrevious();
             while (v != null && v.getResult() == Result.FAILURE) {
                 v = v.getPrevious();
             }
             return v;
         }
 
         @Override
         public R getPreviousFailedBuild() {
             RunValue<J,R> v = getPreviousFailed();
             return v != null? v.getBuild(): null;
 
         }
 
         @Override
         public RunValue<J,R> getPreviousFailed() {
             RunValue<J,R> v = getPrevious();
             while (v != null && v.getResult() != Result.FAILURE) {
                 v = v.getPrevious();
             }
             return v;
 
         }
                
         @Override
         public R getPreviousSuccessfulBuild() {
             RunValue<J,R> v = getPreviousSuccessful();
             return v != null? v.getBuild(): null;
         }
         
         @Override
         public RunValue<J,R> getPreviousSuccessful() {
             RunValue<J,R> v = getPrevious();
             while (v != null && v.getResult() != Result.SUCCESS) {
                 v = v.getPrevious();
             }
             return v;
         }
 
         @Override
         public List<BuildHistory.Record<J,R>> getPreviousOverThreshold(int numberOfBuilds, Result threshold) {
             List<BuildHistory.Record<J,R>> builds = new ArrayList<BuildHistory.Record<J,R>>(numberOfBuilds);
 
             RunValue<J,R> r = getPrevious();
             while (r != null && builds.size() < numberOfBuilds) {
                 if (!r.isBuilding()
                         && (r.getResult() != null && r.getResult().isBetterOrEqualTo(threshold))) {
                     builds.add(r);
                 }
                 r = r.getPrevious();
             }
             return builds;
         }
         
         @Override
         public List<R> getPreviousBuildsOverThreshold(int numberOfBuilds, Result threshold) {
             
             return Lists.transform(getPreviousOverThreshold(numberOfBuilds, threshold),
                     new Function<BuildHistory.Record<J,R>, R>() {
 
                         @Override
                         public R apply(BuildHistory.Record<J,R> f) {
                             return f != null? f.getBuild(): null;
                         }
 
                     });
 
         }
 
 
         @Override
         public Run.State getState() {
             return state;
         }
         
         @Override
         public BallColor getIconColor() {
             if (!isBuilding()) {
                 // already built
                 return getResult().color;
             }
 
             // a new build is in progress
             BallColor baseColor;
             if (getPrevious() == null) {
                 baseColor = BallColor.GREY;
             } else {
                 baseColor = getPrevious().getIconColor();
             }
 
             return baseColor.anime();
         }
         
         @Override
         public String getBuildStatusUrl() {
             return getIconColor().getImage();
         }
         
         @Override
         public Run.Summary getBuildStatusSummary() {
             Record<J,R> prev = getPrevious();
 
             if (getResult() == Result.SUCCESS) {
                 if (prev == null || prev.getResult() == Result.SUCCESS) {
                     return new Run.Summary(false, Messages.Run_Summary_Stable());
                 } else {
                     return new Run.Summary(false, Messages.Run_Summary_BackToNormal());
                 }
             }
 
             if (getResult() == Result.FAILURE) {
                 Record<J,R> since = getPreviousNotFailed();
                 if (since == null) {
                     return new Run.Summary(false, Messages.Run_Summary_BrokenForALongTime());
                 }
                 if (since == prev) {
                     return new Run.Summary(true, Messages.Run_Summary_BrokenSinceThisBuild());
                 }
                 Record<J,R> failedBuild = since.getNext();
                 return new Run.Summary(false, Messages.Run_Summary_BrokenSince(failedBuild.getBuild().getDisplayName()));
             }
 
             if (getResult() == Result.ABORTED) {
                 return new Run.Summary(false, Messages.Run_Summary_Aborted());
             }
 
             if (getResult() == Result.UNSTABLE) {
                 R run = this.getBuild();
                 AbstractTestResultAction trN = ((AbstractBuild) run).getTestResultAction();
                 AbstractTestResultAction trP = prev == null ? null : ((AbstractBuild) prev.getBuild()).getTestResultAction();
                 if (trP == null) {
                     if (trN != null && trN.getFailCount() > 0) {
                         return new Run.Summary(false, Messages.Run_Summary_TestFailures(trN.getFailCount()));
                     } else // ???
                     {
                         return new Run.Summary(false, Messages.Run_Summary_Unstable());
                     }
                 }
                 if (trP.getFailCount() == 0) {
                     return new Run.Summary(true, Messages.Run_Summary_TestsStartedToFail(trN.getFailCount()));
                 }
                 if (trP.getFailCount() < trN.getFailCount()) {
                     return new Run.Summary(true, Messages.Run_Summary_MoreTestsFailing(trN.getFailCount() - trP.getFailCount(), trN.getFailCount()));
                 }
                 if (trP.getFailCount() > trN.getFailCount()) {
                     return new Run.Summary(false, Messages.Run_Summary_LessTestsFailing(trP.getFailCount() - trN.getFailCount(), trN.getFailCount()));
                 }
 
                 return new Run.Summary(false, Messages.Run_Summary_TestsStillFailing(trN.getFailCount()));
             }
 
             return new Run.Summary(false, Messages.Run_Summary_Unknown());
         }
         
         @Override
         public String toString() {
             return String.format("RunValue(number=%d,displayName=%s,buildDir=%s,state=%s,result=%s)",
                         buildNumber, displayName, buildDir(), state, result);
         }
         
         public static class ConverterImpl implements Converter {
             
             final File buildsDir;
             final RunMap runMap;
             
             ConverterImpl(RunMap runMap) {
                 this.runMap = runMap;
                 this.buildsDir = runMap.persistenceFile.getParentFile();
             }
             
             @Override
             public void marshal(Object o,  HierarchicalStreamWriter writer, MarshallingContext mc) {
                 // TODO  - turn element names sinto constants.
                 
                 RunValue current = (RunValue) o;
                 writer.startNode("build");
                 
                 writer.startNode("number");
                 writer.setValue(String.valueOf(current.buildNumber));
                 writer.endNode();
 
                 if ( current.displayName != null) {
                     writer.startNode("displayName");
                     writer.setValue(current.displayName);
                     writer.endNode();
                 }
                 
                 if ( current.fullDisplayName != null) {
                     writer.startNode("fullDisplayName");
                     writer.setValue(current.fullDisplayName);
                     writer.endNode();
                 }
                 
                 writer.startNode("buildDir");
                 writer.setValue( current.relativeBuildDir(buildsDir));
                 writer.endNode();
                 
                 writer.startNode("state");
                 writer.setValue( current.state.toString());
                 writer.endNode();
                 
                 if ( current.result != null) {
                     writer.startNode("result");
                     writer.setValue( current.result.toString());
                     writer.endNode();
                 }
                 
                 writer.startNode("building");
                 writer.setValue( Boolean.toString( current.isBuilding));
                 writer.endNode();
                 
                 writer.startNode("logUpdated");
                 writer.setValue( Boolean.toString( current.isLogUpdated()));
                 writer.endNode();
                 
                 writer.startNode("timestamp");
                 writer.setValue( String.valueOf( current.timeInMillis));
                 writer.endNode();
                 
                 writer.startNode("duration");
                 writer.setValue( String.valueOf( current.duration));
                 writer.endNode();
                 
                 if ( current.url != null ) {
                     writer.startNode("url");
                     writer.setValue( current.url);
                     writer.endNode();
                 }
 
                 if ( current.builtOnStr != null) {
                     writer.startNode("builtOn");
                     writer.setValue( current.builtOnStr);
                     writer.endNode();
                 }
                 
                 writer.endNode();
             }
 
 
             @Override
             public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
 
                 LazyRunValue rv = new LazyRunValue(runMap);
                 
                 assert "build".equals(reader.getNodeName());
                 
                 while (reader.hasMoreChildren()) {
                     reader.moveDown();
                     
                     String name = reader.getNodeName();
                     if ( "number".equals(name) ) {
                         rv.buildNumber = Integer.parseInt(reader.getValue()); 
                     }
                     else if ( "displayName".equals(name)) {
                         rv.displayName = reader.getValue();
                     }
                     else if ( "fullDisplayName".equals(name)) {
                         rv.fullDisplayName = reader.getValue();
                     }
                     else if ( "buildDir".equals(name)) {
                         rv.key.buildsDir = this.buildsDir;
                         rv.key.buildDir = reader.getValue();
                     }
                     else if ( "state".equals(name)) {
                         rv.state =  Run.State.valueOf(reader.getValue());
                     }
                     else if ( "result".equals(name)) {
                         String resultValue = reader.getValue();
                         rv.result = resultValue.length() > 0? Result.fromString( resultValue ): null;
                     }
                     else if ( "building".equals(name)) {
                         rv.isBuilding =  Boolean.parseBoolean(reader.getValue());
                     }
                     else if ( "logUpdated".equals(name)) {
                         rv.isLogUpdated = Boolean.parseBoolean(reader.getValue());
                     }
                     else if ( "timestamp".equals(name)) {
                         rv.timeInMillis = Long.parseLong(reader.getValue());
                     }
                     else if ( "duration".equals(name)) {
                         rv.duration = Long.parseLong(reader.getValue());
                     }
                     else if ("url".equals(name)) {
                         rv.url = reader.getValue();
                         if ( rv.url.length() == 0) {
                             rv = null;
                         }
                     }
                     else if ("builtOn".equals(name)) {
                         rv.builtOnStr = reader.getValue();
                         if ( rv.builtOnStr.length() == 0) {
                             rv.builtOnStr = null;
                         }
                     }
                     reader.moveUp();
                 }
                 return rv;
             }
             
 
             @Override
             public boolean canConvert(Class type) {
                 return RunValue.class.isAssignableFrom(type);
             }
             
         }
 
     }
     
     
     /**
      * Hold onto the Constructor and {@literal config} directory and re-instantiate on
      * demand.
      */
      static class LazyRunValue<J extends Job<J,R>, R extends Run<J,R>> 
         extends RunValue<J,R> {
 
 
         private static class Key {
             private String buildDir;
             private File buildsDir;
             private transient RunMap.Constructor ctor;
             private final LazyRunValue referenced;
             private volatile boolean refreshed;
             
             Key(File buildsDir, String buildDir, RunMap.Constructor ctor, LazyRunValue ref) {
                 this.buildsDir = buildsDir;
                 this.buildDir = buildDir;
                 this.ctor = ctor;
                 this.referenced = ref;
             }
             
             @Override
             public boolean equals(Object o) {
                 boolean equal = false;
                 if ( o instanceof Key) {
                     Key other = (Key)o;
                     equal = buildDir.equals(other.buildDir) &&
                             buildsDir.getPath().equals(other.buildsDir.getPath()) &&
                             ctor.equals(other.ctor);
                 }
                 return equal;
             }
             @Override
             public int hashCode() {
                 return buildDir.hashCode();
             }
         }
         
         private final Key key;
         
         private LazyRunValue(RunMap runMap) {
             // Used when loaded from file
             this.key = new Key(null, null, null, this);
             setRunMap(runMap);
         } 
         
         private LazyRunValue( RunMap runMap, File buildsDir, String buildDir, RunMap.Constructor ctor) {
             this.key = new Key(buildsDir, buildDir, ctor, this);
             setRunMap(runMap);
             sync();
         }
         
         @Override
         File buildDir() {
             return new File(key.buildsDir, key.buildDir);
         }
         
         @Override
         public R getBuild() {
             R v=  (R) runMap().runValueCache().get(key);
             if ( key.refreshed ) {
                 // key.refreshed is true if item has been loaded from disk
                 // for the first time by the cache.
                 key.refreshed = false;
                 
                 v.onLoad();
             }
             return v;
         }
 
         
         
     }
     
     /**
      * No Lazy stuff here, just hold onto the instance since we do not 
      * know how to reconstruct it.
      */
     private static class EagerRunValue<J extends Job<J,R>, R extends Run<J,R>> extends RunValue<J,R> {
         private R referenced;
         
         EagerRunValue(RunMap runMap, R r) {
             setRunMap(runMap);
             this.referenced = r;
             if ( r instanceof BuildNavigable) {
                 ((BuildNavigable)r).setBuildNavigator(this);
             }
             sync();
         }
 
         @Override
         public R getBuild() {
             return this.referenced;
         }
         
         @Override
         File buildDir() {
             return getBuild().getRootDir();
         }
 
     }
     
     private static class RunEntry<J extends Job<J,R>, R extends Run<J,R> & BuildNavigable> implements Map.Entry<Integer, R> {
         private Integer key;
         private RunValue<J,R> value;
         
         private RunEntry(Integer key, RunValue<J,R> value) {
             this.key = key;
             this.value = value;
         }
 
         @Override
         public Integer getKey() {
             return key;
         }
 
         @Override
         public R getValue() {
             return value.getBuild();
         }
 
         @Override
         public R setValue(R value) {
             throw new UnsupportedOperationException("Not implemented");
         }
 
     }
     
     /**
      * This is a global listener that is called for all types of builds, so 
      * does not have any type-arguments, but it only operates on AbstractProjects.
      */
     @Extension
     public static class RunValueUpdater extends RunListener<Run> {
         
         
         private void update(Run run) {
             // Updates a RunValue with the latest information from Run
             final Object job = run.getParent();
             if (job instanceof AbstractProject) {
                 final AbstractProject p = (AbstractProject) job;
                 RunValue rv = (RunValue) p.builds.builds.get(run.getNumber());
                 rv.sync();
                 p.builds.recalcMarkers();
                 p.builds.saveToRunMapXml();
 
             }
         }
 
         @Override
         public void onCompleted(Run r, TaskListener listener) {
             update(r);
         }
 
         @Override
         public void onFinalized(Run r) {
             update(r);
         }
 
         @Override
         public void onStarted(Run r, TaskListener listener) {
             update(r);
         }
 
         @Override
         public void onDeleted(Run r) {
             update(r);
         }
         
         
         
     }
     
     
     public static class ConverterImpl implements Converter {
 
         @Override
         public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext mc) {
             final RunMap runMap = (RunMap) source;
             
             writer.startNode("builds");
             RunValue rv = runMap.getFirst();
             while ( rv != null) {
                 mc.convertAnother(rv, new RunValue.ConverterImpl(runMap));
                 rv = rv.getNext();
             }
             writer.endNode();
             
             writer.startNode("markers");
             for (Object bm: runMap.buildMarkersCache.keySet()) {
                 RunValue mbrv = (RunValue) runMap.buildMarkersCache.get(bm);
                 if ( mbrv != null) {
                     writer.startNode(((BuildMarker)bm).name());
                     writer.setValue(String.valueOf(mbrv.buildNumber));
                     writer.endNode();
                 }
             }
             writer.endNode();
         }
 
         @Override
         public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
             final RunMap runMap = (RunMap)uc.currentObject();
             runMap.builds.clear();
             runMap.buildsTimeMap.clear();
             runMap.buildMarkersCache.clear();
             
             while (reader.hasMoreChildren()) {
                 reader.moveDown();
                 if ( "builds".equals(reader.getNodeName())) {
                     RunValue prev = null;
                     while (reader.hasMoreChildren()) {
                         reader.moveDown();
                         RunValue rv = (RunValue) uc.convertAnother(runMap, RunValue.class, 
                                                         new RunValue.ConverterImpl(runMap));
                         rv.setPrevious(prev);
                         runMap.builds.put(rv.getNumber(), rv);
                         runMap.buildsTimeMap.put(rv.getNumber(), rv.timeInMillis);
                         if ( prev != null ) {
                             prev.setNext(rv);
                         }
                         prev = rv;
                         reader.moveUp();
                     }
                 }
                 else if ("markers".equals(reader.getNodeName())) {
                     while (reader.hasMoreChildren()) {
                         reader.moveDown();
                         BuildMarker bm = BuildMarker.valueOf(reader.getNodeName());
                         Integer buildNumber = Integer.parseInt(reader.getValue());
                         RunValue bmrv = (RunValue) runMap.builds.get(buildNumber);
                         runMap.buildMarkersCache.put(bm, bmrv);
                         reader.moveUp();
                     }
                 }
                 
                 reader.moveUp();
             }
             
             return runMap;
         }
 
         @Override
         public boolean canConvert(Class type) {
             return type == RunMap.class;
 
         }
         
     }
     
     private final Function<RunValue<J,R>, R> RUNVALUE_TO_RUN_TRANSFORMER =
         new Function<RunValue<J,R>,R>() {
 
         @Override
         public R apply(RunValue<J,R> input) {
             final R build = input.getBuild();
             return build;
         }
 
      };
     
     private static final Logger LOGGER = Logger.getLogger(RunMap.class.getName());
     
 }
