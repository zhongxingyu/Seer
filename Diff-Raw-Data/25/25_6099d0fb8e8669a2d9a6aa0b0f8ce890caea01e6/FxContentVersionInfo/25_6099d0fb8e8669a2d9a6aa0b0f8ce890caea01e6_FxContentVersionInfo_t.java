 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.content;
 
import com.flexive.core.LifeCycleInfoImpl;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.security.LifeCycleInfo;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Arrays;
 
 /**
  * Information about a content's existing versions
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxContentVersionInfo implements Serializable {
 
     private static final long serialVersionUID = -2928402301816290488L;
     private long id;
     private int minVersion;
     private int maxVersion;
     private int liveVersion;
     private int lastModifiedVersion;
     private Map<Integer, VersionData> versions;
     private VersionSelector versionSelector;
     private Integer[] vers;
 
     public static final class VersionData implements Serializable {
         private LifeCycleInfo lifeCycleInfo;
         private long step;
 
         public VersionData(LifeCycleInfo lifeCycleInfo, long step) {
             this.lifeCycleInfo = lifeCycleInfo;
             this.step = step;
         }
 
         public LifeCycleInfo getLifeCycleInfo() {
             return lifeCycleInfo;
         }
 
         public long getStep() {
             return step;
         }
 
     }
 
     /**
      * Selector for versions (helps with EL)
      */
     public static final class VersionSelector extends Hashtable<Integer, VersionData> {
         private static final long serialVersionUID = -1240028837766282868L;
         FxContentVersionInfo versionInfo;
 
         /**
          * Ctor
          *
          * @param versionInfo versionInfo to use
          */
         VersionSelector(FxContentVersionInfo versionInfo) {
             this.versionInfo = versionInfo;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public VersionData get(Object key) {
             return versionInfo.versions.get(key);
         }
     }
 
     /**
      * Ctor
      *
      * @param id                  instance id
      * @param minVersion          minimum existing version
      * @param maxVersion          maximum existing version
      * @param liveVersion         live version (if not exists: -1)
      * @param lastModifiedVersion version that has the latest modification date
      * @param versions            map of version,LifeCycleInfo entries
      */
     public FxContentVersionInfo(long id, int minVersion, int maxVersion, int liveVersion, int lastModifiedVersion, Map<Integer, VersionData> versions) {
         this.id = id;
         this.minVersion = minVersion;
         this.maxVersion = maxVersion;
         this.liveVersion = liveVersion;
         this.lastModifiedVersion = lastModifiedVersion;
         this.versions = versions;
         this.versionSelector = new VersionSelector(this);
         this.vers = versions.keySet().toArray(new Integer[versions.keySet().size()]);
         Arrays.sort(vers);
     }
 
     /**
     * Ctor for a new content
     */
    public FxContentVersionInfo() {
        this.id = -1;
        this.minVersion = 1;
        this.maxVersion = 1;
        this.liveVersion = 1;
        this.lastModifiedVersion = 1;
        this.versions = new HashMap<Integer, VersionData>(1);
        this.versions.put(1, new VersionData(LifeCycleInfoImpl.createNew(FxContext.getUserTicket()), 0));
        this.versionSelector = new VersionSelector(this);
        this.vers = versions.keySet().toArray(new Integer[versions.keySet().size()]);
        Arrays.sort(vers);
    }

    /**
      * Get the id of the instance
      *
      * @return id of the instance
      */
     public long getId() {
         return id;
     }
 
     /**
      * Get the minimum existing version
      *
      * @return minimum existing version
      */
     public int getMinVersion() {
         return minVersion;
     }
 
     /**
      * Get the maximum existing version
      *
      * @return maximum existing version
      */
     public int getMaxVersion() {
         return maxVersion;
     }
 
     /**
      * Get the live version. if no live version exists then <code>-1</code> is returned
      *
      * @return live version or <code>-1</code>
      */
     public int getLiveVersion() {
         return liveVersion;
     }
 
     /**
      * Does a live version exist for this content instance
      *
      * @return if a live version exists for this content instance
      */
     public boolean hasLiveVersion() {
         return liveVersion > 0;
     }
 
     /**
      * Does a live version exist for this content instance
      * (EL compatible variant)
      *
      * @return if a live version exists for this content instance
      * @see #hasLiveVersion()
      */
     public boolean isHasLiveVersion() {
         return liveVersion > 0;
     }
 
     /**
      * Get the version that was changed most recently
      *
      * @return version that was changed most recently
      */
     public int getLastModifiedVersion() {
         return lastModifiedVersion;
     }
 
     /**
      * Get an array for all available versions (sorted)
      *
      * @return array for all available versions (sorted)
      */
     public Integer[] getVersions() {
         return vers.clone();
     }
 
     /**
      * How many versions exist?
      *
      * @return number of versions
      */
     public int getVersionCount() {
         return versions.size();
     }
 
     /**
      * Get the version selector
      *
      * @return VersionSelector
      */
     public VersionSelector getVersionSelector() {
         return versionSelector;
     }
 
     /**
      * Get the VersionData for a requested version or <code>null</code> if the
      * requested version does not exist
      *
      * @param version the requested version
      * @return VersionData for a requested version or <code>null</code> if the
      *         requested version does not exist
      */
     public VersionData getVersionData(int version) {
         return versions.get(version);
     }
 
     /**
      * Get the LifeCycleInfo for a requested version or <code>null</code> if the
      * requested version does not exist
      *
      * @param version the requested version
      * @return LifeCycleInfo for a requested version or <code>null</code> if the
      *         requested version does not exist
      */
     public LifeCycleInfo getLifeCycleInfo(int version) {
         return getVersionData(version).getLifeCycleInfo();
     }
 
     /**
      * Get the step id for a requested version or <code>null</code> if the
      * requested version does not exist
      *
      * @param version the requested version
      * @return step id for a requested version or <code>null</code> if the
      *         requested version does not exist
      */
     public long getStep(int version) {
         return getVersionData(version).getStep();
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(200);
         sb.append("ContentVersionInfo:\nGeneral information: {id=").append(this.id).append(",minVer=").append(this.minVersion).
                 append(",maxVer=").append(this.maxVersion).append(",liveVer=").append(this.liveVersion).
                 append(",lastModVer=").append(this.lastModifiedVersion).append("}\n");
         for (int v : versions.keySet())
             sb.append("Version ").append(v).append(":").append(versions.get(v).toString()).append("\n");
 
         return sb.toString();
     }
 
     /**
      * Check if the requested Pk's version exists
      *
      * @param pk primary key
      * @return if the requested Pk's version exists
      */
     public boolean containsVersion(FxPK pk) {
         if (!pk.isDistinctVersion()) {
             return pk.getVersion() == FxPK.MAX || pk.getVersion() == FxPK.LIVE && liveVersion > 0;
         }
         return versions.containsKey(pk.getVersion());
     }
 
     /**
      * Get the correct maximum or live version if <code>version</code> is version constant from FxPK
      *
      * @param version the requested version
      * @return distinct version
      */
     public int getDistinctVersion(int version) {
         if (version == FxPK.MAX)
             return maxVersion;
         if (version == FxPK.LIVE)
             return liveVersion;
         return version;
     }
 
     /**
      * Temp. class to create LifeCycleInfo's for new instances
      */
     public static class NewLifeCycleInfoImpl implements LifeCycleInfo {
 
         private long userId;
         private long time;
 
         /**
          * Ctor using the current user
          * 
          * @since 3.1
          */
         public NewLifeCycleInfoImpl() {
             this.userId = FxContext.getUserTicket().getUserId();
             this.time = System.currentTimeMillis();
         }
 
         /**
          * Ctor
          *
          * @param userId user id to use
          */
         public NewLifeCycleInfoImpl(long userId) {
             this.userId = userId;
             this.time = System.currentTimeMillis();
         }
 
         /**
          * {@inheritDoc}
          */
         public long getCreatorId() {
             return userId;
         }
 
         /**
          * {@inheritDoc}
          */
         public long getCreationTime() {
             return time;
         }
 
         /**
          * {@inheritDoc}
          */
         public long getModificatorId() {
             return userId;
         }
 
         /**
          * {@inheritDoc}
          */
         public long getModificationTime() {
             return time;
         }
     }
 
     /**
      * Create an empty version info for new FxContent instances using the calling user as creator
      *
      * @return an empty version info for new FxContent instances
      */
     public static FxContentVersionInfo createEmpty() {
         Map<Integer, VersionData> versions = new HashMap<Integer, VersionData>(1);
         versions.put(1, new VersionData(new NewLifeCycleInfoImpl(FxContext.getUserTicket().getUserId()), 1));
         return new FxContentVersionInfo(FxPK.NEW_ID, 1, 1, -1, 1, versions);
     }
 }
