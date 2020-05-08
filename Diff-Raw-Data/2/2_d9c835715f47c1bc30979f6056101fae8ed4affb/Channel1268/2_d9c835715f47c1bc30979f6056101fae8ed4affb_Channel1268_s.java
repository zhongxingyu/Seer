 /*
  * Copyright 2011 Gregory P. Moyer
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
 package org.syphr.mythtv.db.schema.impl;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.hibernate.annotations.GenericGenerator;
 import org.syphr.mythtv.db.schema.Channel;
 
 @Entity
 @Table(name = "channel")
 public class Channel1268 implements Channel
 {
     @Id
     @GeneratedValue(generator = "assigned")
     @GenericGenerator(name = "assigned", strategy = "assigned")
     private int chanid;
 
     @Column(nullable = false, length = 10)
     private String channum;
 
     @Column(length = 10)
     private String freqid;
 
     private Integer sourceid;
 
     @Column(nullable = false, length = 20)
     private String callsign;
 
     @Column(nullable = false, length = 64)
     private String name;
 
     @Column(nullable = false)
     private String icon;
 
     private Integer finetune;
 
     @Column(nullable = false)
     private String videofilters;
 
     @Column(nullable = false, length = 255)
     private String xmltvid;
 
     @Column(nullable = false)
     private int recpriority;
 
     private Integer contrast;
 
     private Integer brightness;
 
     private Integer colour;
 
     private Integer hue;
 
     @Column(nullable = false, length = 10)
     private String tvformat;
 
     @Column(nullable = false)
     private boolean visible;
 
     @Column(nullable = false)
     private String outputfilters;
 
     private Boolean useonairguide;
 
     private Short mplexid;
 
     private Integer serviceid;
 
     @Column(nullable = false)
     private int tmoffset;
 
     @Column(name = "atsc_major_chan", nullable = false)
     private int atscMajorChan;
 
     @Column(name = "atsc_minor_chan", nullable = false)
     private int atscMinorChan;
 
     @Column(name = "last_record", nullable = false, length = 19)
     private Date lastRecord;
 
     @Column(name = "default_authority", nullable = false, length = 32)
     private String defaultAuthority;
 
     @Column(nullable = false)
     private int commmethod;
 
     public Channel1268()
     {
         super();
     }
 
     public Channel1268(int chanid,
                        String channum,
                        String callsign,
                        String name,
                        String icon,
                        String videofilters,
                        String xmltvid,
                        int recpriority,
                        String tvformat,
                        boolean visible,
                        String outputfilters,
                        int tmoffset,
                        int atscMajorChan,
                        int atscMinorChan,
                        Date lastRecord,
                        String defaultAuthority,
                        int commmethod)
     {
         this.chanid = chanid;
         this.channum = channum;
         this.callsign = callsign;
         this.name = name;
         this.icon = icon;
         this.videofilters = videofilters;
         this.xmltvid = xmltvid;
         this.recpriority = recpriority;
         this.tvformat = tvformat;
         this.visible = visible;
         this.outputfilters = outputfilters;
         this.tmoffset = tmoffset;
         this.atscMajorChan = atscMajorChan;
         this.atscMinorChan = atscMinorChan;
         this.lastRecord = lastRecord;
         this.defaultAuthority = defaultAuthority;
         this.commmethod = commmethod;
     }
 
     public Channel1268(int chanid,
                        String channum,
                        String freqid,
                        Integer sourceid,
                        String callsign,
                        String name,
                        String icon,
                        Integer finetune,
                        String videofilters,
                        String xmltvid,
                        int recpriority,
                        Integer contrast,
                        Integer brightness,
                        Integer colour,
                        Integer hue,
                        String tvformat,
                        boolean visible,
                        String outputfilters,
                        Boolean useonairguide,
                        Short mplexid,
                        Integer serviceid,
                        int tmoffset,
                        int atscMajorChan,
                        int atscMinorChan,
                        Date lastRecord,
                        String defaultAuthority,
                        int commmethod)
     {
         this.chanid = chanid;
         this.channum = channum;
         this.freqid = freqid;
         this.sourceid = sourceid;
         this.callsign = callsign;
         this.name = name;
         this.icon = icon;
         this.finetune = finetune;
         this.videofilters = videofilters;
         this.xmltvid = xmltvid;
         this.recpriority = recpriority;
         this.contrast = contrast;
         this.brightness = brightness;
         this.colour = colour;
         this.hue = hue;
         this.tvformat = tvformat;
         this.visible = visible;
         this.outputfilters = outputfilters;
         this.useonairguide = useonairguide;
         this.mplexid = mplexid;
         this.serviceid = serviceid;
         this.tmoffset = tmoffset;
         this.atscMajorChan = atscMajorChan;
         this.atscMinorChan = atscMinorChan;
         this.lastRecord = lastRecord;
         this.defaultAuthority = defaultAuthority;
         this.commmethod = commmethod;
     }
 
     @Override
     public int getChanid()
     {
         return this.chanid;
     }
 
     @Override
     public void setChanid(int chanid)
     {
         this.chanid = chanid;
     }
 
     @Override
     public String getChannum()
     {
         return this.channum;
     }
 
     @Override
     public void setChannum(String channum)
     {
         this.channum = channum;
     }
 
     @Override
     public String getFreqid()
     {
         return this.freqid;
     }
 
     @Override
     public void setFreqid(String freqid)
     {
         this.freqid = freqid;
     }
 
     @Override
     public Integer getSourceid()
     {
         return this.sourceid;
     }
 
     @Override
     public void setSourceid(Integer sourceid)
     {
         this.sourceid = sourceid;
     }
 
     @Override
     public String getCallsign()
     {
         return this.callsign;
     }
 
     @Override
     public void setCallsign(String callsign)
     {
         this.callsign = callsign;
     }
 
     @Override
     public String getName()
     {
         return this.name;
     }
 
     @Override
     public void setName(String name)
     {
         this.name = name;
     }
 
     @Override
     public String getIcon()
     {
         return this.icon;
     }
 
     @Override
     public void setIcon(String icon)
     {
         this.icon = icon;
     }
 
     @Override
     public Integer getFinetune()
     {
         return this.finetune;
     }
 
     @Override
     public void setFinetune(Integer finetune)
     {
         this.finetune = finetune;
     }
 
     @Override
     public String getVideofilters()
     {
         return this.videofilters;
     }
 
     @Override
     public void setVideofilters(String videofilters)
     {
         this.videofilters = videofilters;
     }
 
     @Override
     public String getXmltvid()
     {
         return this.xmltvid;
     }
 
     @Override
     public void setXmltvid(String xmltvid)
     {
         this.xmltvid = xmltvid;
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
     public Integer getContrast()
     {
         return this.contrast;
     }
 
     @Override
     public void setContrast(Integer contrast)
     {
         this.contrast = contrast;
     }
 
     @Override
     public Integer getBrightness()
     {
         return this.brightness;
     }
 
     @Override
     public void setBrightness(Integer brightness)
     {
         this.brightness = brightness;
     }
 
     @Override
     public Integer getColour()
     {
         return this.colour;
     }
 
     @Override
     public void setColour(Integer colour)
     {
         this.colour = colour;
     }
 
     @Override
     public Integer getHue()
     {
         return this.hue;
     }
 
     @Override
     public void setHue(Integer hue)
     {
         this.hue = hue;
     }
 
     @Override
     public String getTvformat()
     {
         return this.tvformat;
     }
 
     @Override
     public void setTvformat(String tvformat)
     {
         this.tvformat = tvformat;
     }
 
     @Override
     public boolean isVisible()
     {
         return this.visible;
     }
 
     @Override
     public void setVisible(boolean visible)
     {
         this.visible = visible;
     }
 
     @Override
     public String getOutputfilters()
     {
         return this.outputfilters;
     }
 
     @Override
     public void setOutputfilters(String outputfilters)
     {
         this.outputfilters = outputfilters;
     }
 
     @Override
     public Boolean getUseonairguide()
     {
         return this.useonairguide;
     }
 
     @Override
     public void setUseonairguide(Boolean useonairguide)
     {
         this.useonairguide = useonairguide;
     }
 
     @Override
     public Short getMplexid()
     {
         return this.mplexid;
     }
 
     @Override
     public void setMplexid(Short mplexid)
     {
         this.mplexid = mplexid;
     }
 
     @Override
     public Integer getServiceid()
     {
         return this.serviceid;
     }
 
     @Override
     public void setServiceid(Integer serviceid)
     {
         this.serviceid = serviceid;
     }
 
     @Override
     public int getTmoffset()
     {
         return this.tmoffset;
     }
 
     @Override
     public void setTmoffset(int tmoffset)
     {
         this.tmoffset = tmoffset;
     }
 
     @Override
     public int getAtscMajorChan()
     {
         return this.atscMajorChan;
     }
 
     @Override
     public void setAtscMajorChan(int atscMajorChan)
     {
         this.atscMajorChan = atscMajorChan;
     }
 
     @Override
     public int getAtscMinorChan()
     {
         return this.atscMinorChan;
     }
 
     @Override
     public void setAtscMinorChan(int atscMinorChan)
     {
         this.atscMinorChan = atscMinorChan;
     }
 
     @Override
     public Date getLastRecord()
     {
         return this.lastRecord;
     }
 
     @Override
     public void setLastRecord(Date lastRecord)
     {
         this.lastRecord = lastRecord;
     }
 
     @Override
     public String getDefaultAuthority()
     {
         return this.defaultAuthority;
     }
 
     @Override
     public void setDefaultAuthority(String defaultAuthority)
     {
         this.defaultAuthority = defaultAuthority;
     }
 
     @Override
     public int getCommmethod()
     {
         return this.commmethod;
     }
 
     @Override
     public void setCommmethod(int commmethod)
     {
         this.commmethod = commmethod;
     }
 
     @Override
     public String toString()
     {
         StringBuilder builder = new StringBuilder();
        builder.append("Channel1264 [chanid=");
         builder.append(chanid);
         builder.append(", channum=");
         builder.append(channum);
         builder.append(", freqid=");
         builder.append(freqid);
         builder.append(", sourceid=");
         builder.append(sourceid);
         builder.append(", callsign=");
         builder.append(callsign);
         builder.append(", name=");
         builder.append(name);
         builder.append(", icon=");
         builder.append(icon);
         builder.append(", finetune=");
         builder.append(finetune);
         builder.append(", videofilters=");
         builder.append(videofilters);
         builder.append(", xmltvid=");
         builder.append(xmltvid);
         builder.append(", recpriority=");
         builder.append(recpriority);
         builder.append(", contrast=");
         builder.append(contrast);
         builder.append(", brightness=");
         builder.append(brightness);
         builder.append(", colour=");
         builder.append(colour);
         builder.append(", hue=");
         builder.append(hue);
         builder.append(", tvformat=");
         builder.append(tvformat);
         builder.append(", visible=");
         builder.append(visible);
         builder.append(", outputfilters=");
         builder.append(outputfilters);
         builder.append(", useonairguide=");
         builder.append(useonairguide);
         builder.append(", mplexid=");
         builder.append(mplexid);
         builder.append(", serviceid=");
         builder.append(serviceid);
         builder.append(", tmoffset=");
         builder.append(tmoffset);
         builder.append(", atscMajorChan=");
         builder.append(atscMajorChan);
         builder.append(", atscMinorChan=");
         builder.append(atscMinorChan);
         builder.append(", lastRecord=");
         builder.append(lastRecord);
         builder.append(", defaultAuthority=");
         builder.append(defaultAuthority);
         builder.append(", commmethod=");
         builder.append(commmethod);
         builder.append("]");
         return builder.toString();
     }
 }
