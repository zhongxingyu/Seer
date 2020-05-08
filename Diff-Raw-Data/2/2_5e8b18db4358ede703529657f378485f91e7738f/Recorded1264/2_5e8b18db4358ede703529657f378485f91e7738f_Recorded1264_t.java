 /*
  * Copyright 2011-2012 Gregory P. Moyer
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
 package org.syphr.mythtv.db.schema.impl._0_24;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.syphr.mythtv.db.schema.Recorded;
 import org.syphr.mythtv.db.schema.RecordedId;
 
 @Entity
 @Table(name = "recorded")
 public class Recorded1264 implements Recorded
 {
     /**
      * Serialization ID
      */
     private static final long serialVersionUID = 1L;
 
     @EmbeddedId
     private RecordedId1264 id;
 
     @Column(nullable = false, length = 19)
     @Temporal(TemporalType.TIMESTAMP)
     private Date endtime;
 
     @Column(nullable = false, length = 128)
     private String title;
 
     @Column(nullable = false, length = 128)
     private String subtitle;
 
     @Column(nullable = false, length = 16000)
     private String description;
 
     @Column(nullable = false, length = 64)
     private String category;
 
     @Column(nullable = false, length = 64)
     private String hostname;
 
     @Column(nullable = false)
     private boolean bookmark;
 
     @Column(nullable = false)
     private int editing;
 
     @Column(nullable = false)
     private boolean cutlist;
 
     @Column(nullable = false)
     private int autoexpire;
 
     @Column(nullable = false)
     private int commflagged;
 
     @Column(nullable = false, length = 32)
     private String recgroup;
 
     private Integer recordid;
 
     @Column(nullable = false, length = 40)
     private String seriesid;
 
     @Column(nullable = false, length = 40)
     private String programid;
 
     @Column(nullable = false, length = 19)
     @Temporal(TemporalType.TIMESTAMP)
     private Date lastmodified;
 
     @Column(nullable = false)
     private long filesize;
 
     @Column(nullable = false, precision = 12, scale = 0)
     private float stars;
 
     private Boolean previouslyshown;
 
     @Column(nullable = false, length = 10)
     @Temporal(TemporalType.DATE)
     private Date originalairdate;
 
     @Column(nullable = false)
     private boolean preserve;
 
     @Column(nullable = false)
     private int findid;
 
     @Column(nullable = false)
     private boolean deletepending;
 
     @Column(nullable = false)
     private int transcoder;
 
     @Column(nullable = false, precision = 12, scale = 0)
     private float timestretch;
 
     @Column(nullable = false)
     private int recpriority;
 
     @Column(nullable = false)
     private String basename;
 
     @Column(nullable = false, length = 19)
     @Temporal(TemporalType.TIMESTAMP)
     private Date progstart;
 
     @Column(nullable = false, length = 19)
     @Temporal(TemporalType.TIMESTAMP)
     private Date progend;
 
     @Column(nullable = false, length = 32)
     private String playgroup;
 
     @Column(nullable = false, length = 32)
     private String profile;
 
     @Column(nullable = false)
     private boolean duplicate;
 
     @Column(nullable = false)
     private boolean transcoded;
 
     @Column(nullable = false)
     private byte watched;
 
     @Column(nullable = false, length = 32)
     private String storagegroup;
 
     @Column(nullable = false, length = 19)
     @Temporal(TemporalType.TIMESTAMP)
     private Date bookmarkupdate;
 
     @Override
     public RecordedId1264 getId()
     {
         return this.id;
     }
 
     @Override
     public void setId(RecordedId id)
     {
        if (id != null && !(id instanceof RecordedId1264))
         {
             throw new IllegalArgumentException("Invalid ID type: " + id.getClass().getName());
         }
 
         this.id = (RecordedId1264)id;
     }
 
     @Override
     public Date getEndtime()
     {
         return this.endtime;
     }
 
     @Override
     public void setEndtime(Date endtime)
     {
         this.endtime = endtime;
     }
 
     @Override
     public String getTitle()
     {
         return this.title;
     }
 
     @Override
     public void setTitle(String title)
     {
         this.title = title;
     }
 
     @Override
     public String getSubtitle()
     {
         return this.subtitle;
     }
 
     @Override
     public void setSubtitle(String subtitle)
     {
         this.subtitle = subtitle;
     }
 
     @Override
     public String getDescription()
     {
         return this.description;
     }
 
     @Override
     public void setDescription(String description)
     {
         this.description = description;
     }
 
     @Override
     public String getCategory()
     {
         return this.category;
     }
 
     @Override
     public void setCategory(String category)
     {
         this.category = category;
     }
 
     @Override
     public String getHostname()
     {
         return this.hostname;
     }
 
     @Override
     public void setHostname(String hostname)
     {
         this.hostname = hostname;
     }
 
     @Override
     public boolean isBookmark()
     {
         return this.bookmark;
     }
 
     @Override
     public void setBookmark(boolean bookmark)
     {
         this.bookmark = bookmark;
     }
 
     @Override
     public int getEditing()
     {
         return this.editing;
     }
 
     @Override
     public void setEditing(int editing)
     {
         this.editing = editing;
     }
 
     @Override
     public boolean isCutlist()
     {
         return this.cutlist;
     }
 
     @Override
     public void setCutlist(boolean cutlist)
     {
         this.cutlist = cutlist;
     }
 
     @Override
     public int getAutoexpire()
     {
         return this.autoexpire;
     }
 
     @Override
     public void setAutoexpire(int autoexpire)
     {
         this.autoexpire = autoexpire;
     }
 
     @Override
     public int getCommflagged()
     {
         return this.commflagged;
     }
 
     @Override
     public void setCommflagged(int commflagged)
     {
         this.commflagged = commflagged;
     }
 
     @Override
     public String getRecgroup()
     {
         return this.recgroup;
     }
 
     @Override
     public void setRecgroup(String recgroup)
     {
         this.recgroup = recgroup;
     }
 
     @Override
     public Integer getRecordid()
     {
         return this.recordid;
     }
 
     @Override
     public void setRecordid(Integer recordid)
     {
         this.recordid = recordid;
     }
 
     @Override
     public String getSeriesid()
     {
         return this.seriesid;
     }
 
     @Override
     public void setSeriesid(String seriesid)
     {
         this.seriesid = seriesid;
     }
 
     @Override
     public String getProgramid()
     {
         return this.programid;
     }
 
     @Override
     public void setProgramid(String programid)
     {
         this.programid = programid;
     }
 
     @Override
     public Date getLastmodified()
     {
         return this.lastmodified;
     }
 
     @Override
     public void setLastmodified(Date lastmodified)
     {
         this.lastmodified = lastmodified;
     }
 
     @Override
     public long getFilesize()
     {
         return this.filesize;
     }
 
     @Override
     public void setFilesize(long filesize)
     {
         this.filesize = filesize;
     }
 
     @Override
     public float getStars()
     {
         return this.stars;
     }
 
     @Override
     public void setStars(float stars)
     {
         this.stars = stars;
     }
 
     @Override
     public Boolean getPreviouslyshown()
     {
         return this.previouslyshown;
     }
 
     @Override
     public void setPreviouslyshown(Boolean previouslyshown)
     {
         this.previouslyshown = previouslyshown;
     }
 
     @Override
     public Date getOriginalairdate()
     {
         return this.originalairdate;
     }
 
     @Override
     public void setOriginalairdate(Date originalairdate)
     {
         this.originalairdate = originalairdate;
     }
 
     @Override
     public boolean isPreserve()
     {
         return this.preserve;
     }
 
     @Override
     public void setPreserve(boolean preserve)
     {
         this.preserve = preserve;
     }
 
     @Override
     public int getFindid()
     {
         return this.findid;
     }
 
     @Override
     public void setFindid(int findid)
     {
         this.findid = findid;
     }
 
     @Override
     public boolean isDeletepending()
     {
         return this.deletepending;
     }
 
     @Override
     public void setDeletepending(boolean deletepending)
     {
         this.deletepending = deletepending;
     }
 
     @Override
     public int getTranscoder()
     {
         return this.transcoder;
     }
 
     @Override
     public void setTranscoder(int transcoder)
     {
         this.transcoder = transcoder;
     }
 
     @Override
     public float getTimestretch()
     {
         return this.timestretch;
     }
 
     @Override
     public void setTimestretch(float timestretch)
     {
         this.timestretch = timestretch;
     }
 
     @Override
     public int getRecpriority()
     {
         return this.recpriority;
     }
 
     @Override
     public void setRecpriority(int recpriority)
     {
         this.recpriority = recpriority;
     }
 
     @Override
     public String getBasename()
     {
         return this.basename;
     }
 
     @Override
     public void setBasename(String basename)
     {
         this.basename = basename;
     }
 
     @Override
     public Date getProgstart()
     {
         return this.progstart;
     }
 
     @Override
     public void setProgstart(Date progstart)
     {
         this.progstart = progstart;
     }
 
     @Override
     public Date getProgend()
     {
         return this.progend;
     }
 
     @Override
     public void setProgend(Date progend)
     {
         this.progend = progend;
     }
 
     @Override
     public String getPlaygroup()
     {
         return this.playgroup;
     }
 
     @Override
     public void setPlaygroup(String playgroup)
     {
         this.playgroup = playgroup;
     }
 
     @Override
     public String getProfile()
     {
         return this.profile;
     }
 
     @Override
     public void setProfile(String profile)
     {
         this.profile = profile;
     }
 
     @Override
     public boolean isDuplicate()
     {
         return this.duplicate;
     }
 
     @Override
     public void setDuplicate(boolean duplicate)
     {
         this.duplicate = duplicate;
     }
 
     @Override
     public boolean isTranscoded()
     {
         return this.transcoded;
     }
 
     @Override
     public void setTranscoded(boolean transcoded)
     {
         this.transcoded = transcoded;
     }
 
     @Override
     public byte getWatched()
     {
         return this.watched;
     }
 
     @Override
     public void setWatched(byte watched)
     {
         this.watched = watched;
     }
 
     @Override
     public String getStoragegroup()
     {
         return this.storagegroup;
     }
 
     @Override
     public void setStoragegroup(String storagegroup)
     {
         this.storagegroup = storagegroup;
     }
 
     @Override
     public Date getBookmarkupdate()
     {
         return this.bookmarkupdate;
     }
 
     @Override
     public void setBookmarkupdate(Date bookmarkupdate)
     {
         this.bookmarkupdate = bookmarkupdate;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
         builder.append("Recorded1264 [id=");
         builder.append(id);
         builder.append(", endtime=");
         builder.append(endtime);
         builder.append(", title=");
         builder.append(title);
         builder.append(", subtitle=");
         builder.append(subtitle);
         builder.append(", description=");
         builder.append(description);
         builder.append(", category=");
         builder.append(category);
         builder.append(", hostname=");
         builder.append(hostname);
         builder.append(", bookmark=");
         builder.append(bookmark);
         builder.append(", editing=");
         builder.append(editing);
         builder.append(", cutlist=");
         builder.append(cutlist);
         builder.append(", autoexpire=");
         builder.append(autoexpire);
         builder.append(", commflagged=");
         builder.append(commflagged);
         builder.append(", recgroup=");
         builder.append(recgroup);
         builder.append(", recordid=");
         builder.append(recordid);
         builder.append(", seriesid=");
         builder.append(seriesid);
         builder.append(", programid=");
         builder.append(programid);
         builder.append(", lastmodified=");
         builder.append(lastmodified);
         builder.append(", filesize=");
         builder.append(filesize);
         builder.append(", stars=");
         builder.append(stars);
         builder.append(", previouslyshown=");
         builder.append(previouslyshown);
         builder.append(", originalairdate=");
         builder.append(originalairdate);
         builder.append(", preserve=");
         builder.append(preserve);
         builder.append(", findid=");
         builder.append(findid);
         builder.append(", deletepending=");
         builder.append(deletepending);
         builder.append(", transcoder=");
         builder.append(transcoder);
         builder.append(", timestretch=");
         builder.append(timestretch);
         builder.append(", recpriority=");
         builder.append(recpriority);
         builder.append(", basename=");
         builder.append(basename);
         builder.append(", progstart=");
         builder.append(progstart);
         builder.append(", progend=");
         builder.append(progend);
         builder.append(", playgroup=");
         builder.append(playgroup);
         builder.append(", profile=");
         builder.append(profile);
         builder.append(", duplicate=");
         builder.append(duplicate);
         builder.append(", transcoded=");
         builder.append(transcoded);
         builder.append(", watched=");
         builder.append(watched);
         builder.append(", storagegroup=");
         builder.append(storagegroup);
         builder.append(", bookmarkupdate=");
         builder.append(bookmarkupdate);
         builder.append("]");
         return builder.toString();
     }
 
     @Override
     public int getSeason()
     {
         // not supported in this schema version
         return 0;
     }
 
     @Override
     public void setSeason(int season)
     {
         // not supported in this schema version
     }
 
     @Override
     public int getEpisode()
     {
         // not supported in this schema version
         return 0;
     }
 
     @Override
     public void setEpisode(int episode)
     {
         // not supported in this schema version
     }
 
     @Override
     public String getInetref()
     {
         // not supported in this schema version
         return null;
     }
 
     @Override
     public void setInetref(String inetref)
     {
         // not supported in this schema version
     }
 }
