 /*
  * Copyright (C) 2005-2008 Michael Keith, Australia Telescope National Facility, CSIRO
  * 
  * email: mkeith@pulsarastronomy.net
  * www  : www.pulsarastronomy.net
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
 package bookkeepr.xmlable;
 
 import bookkeepr.xml.IdAble;
 import bookkeepr.xml.StringConvertable;
 import bookkeepr.xml.XMLAble;
 import coordlib.Coordinate;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.w3c.util.DateParser;
 import org.w3c.util.InvalidDateException;
 
 /**
  *
  * @author kei041
  */
 public class CandidateListStub implements IdAble, XMLAble {
 
     private long id;
     private String name;
     private long psrxmlId;
     private long processingId;
     private int ncands;
     private Date completedDate = null;
     private Date observedDate = null;
     private Coordinate coordinate = null;
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public Date getDate() {
         return this.completedDate;
     }
 
     public String getCompletedDate() {
         if (completedDate == null) {
             return null;
         }
         return DateParser.getIsoDateNoMillis(completedDate);
     }
 
     public Date getObservedDateObj() {
         return observedDate;
     }
 
     public String getObservedDate() {
         if (observedDate == null) {
             return null;
         }
        return DateParser.getIsoDateNoMillis(observedDate);
     }
 
     public Date getCompletedDateAlt() {
         return completedDate;
     }
 
     public void setCompletedDate(Date completedDate) {
         this.completedDate = completedDate;
     }
 
     public void setCompletedDate(String completedDate) {
         try {
             this.completedDate = DateParser.parse(completedDate);
         } catch (InvalidDateException ex) {
             Logger.getLogger(Psrxml.class.getName()).log(Level.WARNING, "Bad date/time specified in psrxml file. MUST be in ISO8601 format.", ex);
             this.completedDate = null;
         }
     }
 
     public void setObservedDate(String completedDate) {
         try {
             this.observedDate = DateParser.parse(completedDate);
         } catch (InvalidDateException ex) {
             Logger.getLogger(Psrxml.class.getName()).log(Level.WARNING, "Bad date/time specified in psrxml file. MUST be in ISO8601 format.", ex);
             this.observedDate = null;
         }
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public String toString() {
         return this.getName();
     }
 
     public int getNcands() {
         return ncands;
     }
 
     public void setNcands(int ncands) {
         this.ncands = ncands;
     }
 
     public long getProcessingId() {
         return processingId;
     }
 
     public void setProcessingId(long processingId) {
         this.processingId = processingId;
     }
 
     public long getPsrxmlId() {
         return psrxmlId;
     }
 
     public void setPsrxmlId(long psrxmlId) {
         this.psrxmlId = psrxmlId;
     }
 
     public Coordinate getCoordinate() {
         return coordinate;
     }
 
     public void setCoordinate(Coordinate coordinate) {
         this.coordinate = coordinate;
     }
 
     public String getClassName() {
         return this.getClass().getSimpleName();
     }
 
     public HashMap<String, StringConvertable> getXmlParameters() {
         return xmlParameters;
     }
 
     public List<String> getXmlSubObjects() {
         return xmlSubObjects;
     }
     private static HashMap<String, StringConvertable> xmlParameters = new HashMap<String, StringConvertable>();
     private static ArrayList<String> xmlSubObjects = new ArrayList<String>();
     
 
     static {
         xmlParameters.put("Id", StringConvertable.ID);
         xmlParameters.put("PsrxmlId", StringConvertable.ID);
         xmlParameters.put("ProcessingId", StringConvertable.ID);
         xmlParameters.put("CompletedDate", StringConvertable.STRING);
         xmlParameters.put("ObservedDate", StringConvertable.STRING);
         xmlParameters.put("Name", StringConvertable.STRING);
         xmlParameters.put("Ncands", StringConvertable.INT);
         xmlParameters.put("Coordinate", StringConvertable.COORDINATE);
 
 
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof CandidateListStub) {
             return ((CandidateListStub) obj).getId() == this.getId();
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return new Long(this.getId()).hashCode();
     }
 }
