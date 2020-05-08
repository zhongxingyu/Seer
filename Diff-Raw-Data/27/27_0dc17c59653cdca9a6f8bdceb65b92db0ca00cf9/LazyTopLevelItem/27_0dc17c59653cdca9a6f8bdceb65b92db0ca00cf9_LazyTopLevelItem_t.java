 /*******************************************************************************
  *
  * Copyright (c) 2013 Oracle Corporation.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *
  *    Roy Varghese
  *
  *******************************************************************************/
 package hudson.model;
 
 import hudson.PermalinkList;
 import hudson.XmlFile;
 import hudson.search.Search;
 import hudson.search.SearchIndex;
 import hudson.security.ACL;
 import hudson.security.Permission;
 import hudson.tasks.LogRotator;
 import hudson.util.RunList;
 import java.io.File;
 import java.io.IOException;
 import java.lang.ref.WeakReference;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import org.eclipse.hudson.api.model.IJob;
 import org.eclipse.hudson.graph.Graph;
 import org.kohsuke.stapler.StaplerProxy;
 import org.springframework.security.AccessDeniedException;
 
 
 /**
  * A decorator around a top-level job that loads the real job lazily and hangs
  * onto it with a weak reference.
  *
  * @author Roy Varghese
  */
 final class LazyTopLevelItem implements TopLevelItem, IJob, StaplerProxy {
     
     // Parameters to the Item
     class Key {
         final XmlFile configFile;
         final ItemGroup parent;
         final String name;
         Key(XmlFile configFile, ItemGroup parent, String name) {
             this.configFile = configFile;
             this.parent = parent;
             this.name = name;
         }
         
         public boolean equals(Object o) {
             boolean equal = false;
             if ( o.getClass() == Key.class) {
                 Key other = (Key) o;
                 equal = name.equals(other.name) && 
                         configFile.equals(other.configFile) && 
                         parent.equals(other.parent);
                        
             }
             return equal;
                 
         }
         
         @Override
         public int hashCode() {
             return name.hashCode();
         }
     }
     
     private final Key key;
     
     private WeakReference<TopLevelItem> ref;
     
     // The cache reference
     private final TopLevelItemsCache itemsCache;
     
     // Log non-loadable Item only once.
     private boolean loggedError = false;
     
     // Type of the item. This is used to satisfy 'instanceof' checks against
     // LazyTopLevelItem for backward compatibility. Such checks are really intended
     // for the wrapped object, not for the wrapper.
     private Class itemType = null;
 
     LazyTopLevelItem(XmlFile configFile, ItemGroup parent, String name, TopLevelItem item) {
         this.key = new Key(configFile, parent, name);
         
        itemsCache = Hudson.getInstance().itemsCache();
         
         // Add to cache if value is already created/available.
         if ( item != null ) {
             itemsCache.put(key, item);
         }
     }
 
     private Class itemType() {
         if ( itemType == null) {
             Item item = item();
             if ( item != null) {
                 itemType = item.getClass();
             }
         }
         return itemType;
     }
     
     /**
      * Unwrap and return the decorated item from a LazyTopLevelItem.
      * 
      * @return null if item is not LazyTopLevelItem, or if neither the
      * decorated nor the item itself can be cast into requested type.
      */
     static <T> T getIfInstanceOf(Item item, Class<T> clazz) {
         
         if ( item.getClass() == LazyTopLevelItem.class) {
 
             LazyTopLevelItem lazyItem = (LazyTopLevelItem) item;
             TopLevelItem realItem = lazyItem.item();
             if (clazz.isAssignableFrom(realItem.getClass())) {
                 return (T) realItem;
             }
                 
         }
         
         return clazz.isInstance(item)? clazz.cast(item): null;
         
     }
     
     /**
      * Load the item if not already loaded.
      *
      * @return
      */
     private synchronized TopLevelItem item() {
         TopLevelItem item = (ref != null? ref.get(): null);
         
         if ( item  == null ) {
             item = itemsCache.get(key);
             ref = new WeakReference( item );
             
         }
         return item;
     }
 
     private IJob job() {
         final Item item = item();
         assert IJob.class.isAssignableFrom(item.getClass());
         return (IJob) item;
     }
 
     @Override
     public TopLevelItemDescriptor getDescriptor() {
         return item().getDescriptor();
     }
 
     @Override
     public ItemGroup<? extends Item> getParent() {
         return key.parent;
     }
 
     @Override
     public Collection<? extends Job> getAllJobs() {
         return item().getAllJobs();
     }
 
     @Override
     public String getName() {
         return key.name;
     }
 
     @Override
     public String getFullName() {
         return item().getFullName();
     }
 
     @Override
     public String getDisplayName() {
         return item().getDisplayName();
     }
 
     @Override
     public String getFullDisplayName() {
         return item().getFullDisplayName();
     }
 
     @Override
     public String getUrl() {
         return item().getUrl();
     }
 
     @Override
     public String getShortUrl() {
         return item().getShortUrl();
     }
 
     @Override
     public String getAbsoluteUrl() {
         return item().getAbsoluteUrl();
     }
 
     @Override
     public void onLoad(ItemGroup<? extends Item> parent, String name) throws IOException {
         // No-op;
     }
 
     @Override
     public void onCopiedFrom(Item src) {
         item().onCopiedFrom(src);
     }
 
     @Override
     public void onCreatedFromScratch() {
         item().onCreatedFromScratch();
     }
 
     @Override
     public void save() throws IOException {
         item().save();
     }
 
     @Override
     public void delete() throws IOException, InterruptedException {
         item().delete();
     }
 
     @Override
     public File getRootDir() {
         return item().getRootDir();
     }
 
     @Override
     public Search getSearch() {
         return item().getSearch();
     }
 
     @Override
     public String getSearchName() {
         return item().getSearchName();
     }
 
     @Override
     public String getSearchUrl() {
         return item().getSearchUrl();
     }
 
     @Override
     public SearchIndex getSearchIndex() {
         return item().getSearchIndex();
     }
 
     @Override
     public ACL getACL() {
         return item().getACL();
     }
 
     @Override
 
     public void checkPermission(Permission permission) throws AccessDeniedException {
         item().checkPermission(permission);
     }
 
     
     @Override
     public boolean hasPermission(Permission permission) {
         return item().hasPermission(permission);
     }
 
     @Override
     public Object getTarget() {
         return item();
     }
     @Override
     public boolean isNameEditable() {
         return job().isNameEditable();
     }
 
     @Override
     public LogRotator getLogRotator() {
         return job().getLogRotator();
     }
 
     @Override
     public void setLogRotator(LogRotator logRotator) {
         job().setLogRotator(logRotator);
     }
 
     @Override
     public boolean supportsLogRotator() {
         return job().supportsLogRotator();
     }
 
     @Override
     public Map getProperties() {
         return job().getProperties();
     }
 
     @Override
     public List getAllProperties() {
         return job().getAllProperties();
     }
 
     @Override
     public boolean isInQueue() {
         return job().isInQueue();
     }
 
     @Override
     public Queue.Item getQueueItem() {
         return job().getQueueItem();
     }
 
     @Override
     public boolean isBuilding() {
         return job().isBuilding();
     }
 
     @Override
     public boolean isKeepDependencies() {
         return job().isKeepDependencies();
     }
 
     @Override
     public int assignBuildNumber() throws IOException {
         return job().assignBuildNumber();
     }
 
     @Override
     public int getNextBuildNumber() {
         return job().getNextBuildNumber();
     }
 
     @Override
     public void updateNextBuildNumber(int next) throws IOException {
         job().updateNextBuildNumber(next);
     }
 
     @Override
     public void logRotate() throws IOException, InterruptedException {
         job().logRotate();
     }
 
     @Override
     public List getWidgets() {
         return job().getWidgets();
     }
 
     @Override
     public boolean isBuildable() {
         return job().isBuildable();
     }
 
     @Override
     public PermalinkList getPermalinks() {
         return job().getPermalinks();
     }
 
     @Override
     public RunList getBuilds() {
         return job().getBuilds();
     }
 
     @Override
     public List getBuilds(Fingerprint.RangeSet rs) {
         return job().getBuilds(rs);
     }
 
     @Override
     public SortedMap getBuildsAsMap() {
         return job().getBuildsAsMap();
     }
 
     @Override
     public Run getBuildByNumber(int n) {
         return job().getBuildByNumber(n);
     }
 
     @Override
     public Run getNearestBuild(int n) {
         return job().getNearestBuild(n);
     }
 
     @Override
     public Run getNearestOldBuild(int n) {
         return job().getNearestOldBuild(n);
     }
 
     @Override
     public Run getLastBuild() {
         return job().getLastBuild();
     }
 
     @Override
     public Run getFirstBuild() {
         return job().getFirstBuild();
     }
 
     @Override
     public Run getLastSuccessfulBuild() {
         return job().getLastSuccessfulBuild();
     }
 
     @Override
     public Run getLastUnsuccessfulBuild() {
         return job().getLastUnsuccessfulBuild();
     }
 
     @Override
     public Run getLastUnstableBuild() {
         return job().getLastUnstableBuild();
     }
 
     @Override
     public Run getLastStableBuild() {
         return job().getLastStableBuild();
     }
 
     @Override
     public Run getLastFailedBuild() {
         return job().getLastFailedBuild();
     }
 
     @Override
     public Run getLastCompletedBuild() {
         return job().getLastCompletedBuild();
     }
 
     @Override
     public List getLastBuildsOverThreshold(int numberOfBuilds, Result threshold) {
         return job().getLastBuildsOverThreshold(numberOfBuilds, threshold);
     }
 
     @Override
     public String getBuildStatusUrl() {
         return job().getBuildStatusUrl();
     }
 
     @Override
     public BallColor getIconColor() {
         return job().getIconColor();
     }
 
     @Override
     public HealthReport getBuildHealth() {
         return job().getBuildHealth();
     }
 
     @Override
     public List getBuildHealthReports() {
         return job().getBuildHealthReports();
     }
 
     @Override
     public Graph getBuildTimeGraph() {
         return job().getBuildTimeGraph();
     }
 
     @Override
     public BuildTimelineWidget getTimeline() {
         return job().getTimeline();
     }
 
     @Override
     public String getCreatedBy() {
         return job().getCreatedBy();
     }
 
     @Override
     public long getCreationTime() {
         return job().getCreationTime();
     }
 
 }
