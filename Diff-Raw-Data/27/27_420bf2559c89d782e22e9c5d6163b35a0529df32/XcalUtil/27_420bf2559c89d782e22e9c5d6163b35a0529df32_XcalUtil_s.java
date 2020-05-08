 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package edu.rpi.cmt.calendar;
 
 import edu.rpi.sss.util.xml.tagdefs.XcalTags;
 
 import net.fortuna.ical4j.model.DateTime;
 import net.fortuna.ical4j.model.TimeZone;
 
 import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
 import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
 import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
 import ietf.params.xml.ns.icalendar_2.BaseComponentType;
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DaylightType;
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
 import ietf.params.xml.ns.icalendar_2.ObjectFactory;
 import ietf.params.xml.ns.icalendar_2.StandardType;
 import ietf.params.xml.ns.icalendar_2.TzidParamType;
 import ietf.params.xml.ns.icalendar_2.UntilRecurType;
 import ietf.params.xml.ns.icalendar_2.ValarmType;
 import ietf.params.xml.ns.icalendar_2.VcalendarType;
 import ietf.params.xml.ns.icalendar_2.VeventType;
 import ietf.params.xml.ns.icalendar_2.VfreebusyType;
 import ietf.params.xml.ns.icalendar_2.VjournalType;
 import ietf.params.xml.ns.icalendar_2.VtimezoneType;
 import ietf.params.xml.ns.icalendar_2.VtodoType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.Duration;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 
 /**
  * @author douglm
  *
  */
 public class XcalUtil {
   private static final ObjectFactory icalOf = new ObjectFactory();
   static Map<Class, QName> compNames = new HashMap<Class, QName>();
 
   public static final Integer OuterKind = 0;
   public static final Integer RecurringKind = 1;
   public static final Integer UidKind = 2;
   public static final Integer AlarmKind = 3;
   public static final Integer TzKind = 4;
 
   static Map<QName, Integer> compKinds = new HashMap<QName, Integer>();
 
   static {
     // Outer container
     addInfo(XcalTags.vcalendar,
             OuterKind,
             VcalendarType.class);
 
     // Recurring style uid + optional recurrence id
     addInfo(XcalTags.vtodo,
             RecurringKind,
             VtodoType.class);
     addInfo(XcalTags.vjournal,
             RecurringKind,
             VjournalType.class);
     addInfo(XcalTags.vevent,
             RecurringKind,
             VeventType.class);
 
     // Uid only
     addInfo(XcalTags.vfreebusy,
             UidKind,
             VfreebusyType.class);
 
     addInfo(XcalTags.valarm,
             AlarmKind,
             ValarmType.class);
 
     // Timezones
     addInfo(XcalTags.standard,
             TzKind,
             StandardType.class);
     addInfo(XcalTags.vtimezone,
             TzKind,
             VtimezoneType.class);
     addInfo(XcalTags.daylight,
             TzKind,
             DaylightType.class);
   }
 
   /** Initialize the DateDatetimeProperty
    * @param dt
    * @param dtval
    * @param tzid
    * @throws Throwable
    */
   public static void initDt(final DateDatetimePropertyType dt,
                             final String dtval,
                             final String tzid) throws Throwable {
     XMLGregorianCalendar xgc = fromDtval(dtval);
 
     if (dtval.length() == 8) {
       dt.setDate(xgc);
       return;
     }
 
     dt.setDateTime(xgc);
 
     if (dtval.endsWith("Z") || (tzid == null)) {
       return;
     }
 
     TzidParamType tz = new TzidParamType();
     tz.setText(tzid);
 
     ArrayOfParameters aop = dt.getParameters();
 
     if (aop == null) {
       aop = new ArrayOfParameters();
       dt.setParameters(aop);
     }
 
     aop.getBaseParameter().add(icalOf.createTzid(tz));
 
     dt.setParameters(aop);
   }
 
   /** Initialize the recur property
    * @param dt
    * @param dtval
    * @throws Throwable
    */
   public static void initUntilRecur(final UntilRecurType dt,
                             final String dtval) throws Throwable {
     XMLGregorianCalendar xgc = fromDtval(dtval);
 
     if (dtval.length() == 8) {
       dt.setDate(xgc);
       return;
     }
 
     dt.setDateTime(xgc);
   }
 
   /**
    * @param dtval
    * @return XMLGregorianCalendar
    * @throws Throwable
    */
   public static XMLGregorianCalendar fromDtval(final String dtval) throws Throwable {
     DatatypeFactory dtf = DatatypeFactory.newInstance();
 
     return dtf.newXMLGregorianCalendar(getXmlFormatDateTime(dtval));
   }
 
   /**
    * @param dur
    * @return Duration
    * @throws Throwable
    */
   public static Duration makeXmlDuration(final String dur) throws Throwable {
     DatatypeFactory dtf = DatatypeFactory.newInstance();
 
     return dtf.newDuration(dur);
   }
 
   /**
    * @param dtval
    * @return utc
    * @throws Throwable
    */
   public static XMLGregorianCalendar getXMlUTCCal(final String dtval) throws Throwable {
     DatatypeFactory dtf = DatatypeFactory.newInstance();
 
     return dtf.newXMLGregorianCalendar(getXmlFormatDateTime(dtval));
   }
 
   /** */
   public static class DtTzid {
     /** yyyymmdd or yyyymmddThhmmss or yyyymmddThhmmssZ */
     public String dt;
 
     /** true if dt represents a date */
     public boolean dateOnly;
 
     /** null or tzid from param */
     public String tzid;
   }
 
   public abstract static class TzGetter {
     /**
      * @param id
      * @return A timezone or null if non found
      * @throws Throwable
      */
     public abstract TimeZone getTz(final String id) throws Throwable;
   }
 
   /** For date only values and floating convert to local UTC. For UTC just return
    * the value. For non-floating convert.
    *
    * @param dt
    * @param tzs
    * @return string UTC value
    * @throws Throwable
    */
   public static String getUTC(final DateDatetimePropertyType dt,
                               final TzGetter tzs) throws Throwable {
     DtTzid dtz = getDtTzid(dt);
 
     if ((dtz.dt.length() == 18) && (dtz.dt.charAt(17) == 'Z')) {
       return dtz.dt;
     }
 
     TimeZone tz = null;
     if (dtz.tzid != null) {
       tz = tzs.getTz(dtz.tzid);
     }
 
     DateTime dtim = new DateTime(dtz.dt, tz);
 
     dtim.setUtc(true);
 
     return dtim.toString();
   }
 
   /**
    * @param dt
    * @return DtTzid filled in
    */
   public static DtTzid getDtTzid(final DateDatetimePropertyType dt) {
     DtTzid res = new DtTzid();
 
     ArrayOfParameters aop = dt.getParameters();
 
     if (aop != null) {
       for (JAXBElement<? extends BaseParameterType> e: aop.getBaseParameter()) {
         if (e.getName().equals(XcalTags.tzid)) {
           res.tzid = ((TzidParamType)e.getValue()).getText();
           break;
         }
       }
     }
 
     res.dateOnly = dt.getDate() != null;
     if (res.dateOnly) {
       res.dt = getIcalFormatDateTime(dt.getDate().toString());
     } else {
       res.dt = getIcalFormatDateTime(dt.getDateTime().toString());
     }
 
     return res;
   }
 
   /**
    * @param val ical format or xml format date or datetime
    * @return XML formatted
    */
   public static String getXmlFormatDateTime(final String val) {
     if (val.charAt(4) == '-') {
       // XML format
       return val;
     }
 
     StringBuilder sb = new StringBuilder();
 
     sb.append(val.substring(0, 4));
     sb.append("-");
     sb.append(val.substring(4, 6));
     sb.append("-");
     sb.append(val.substring(6, 8));
 
     if (val.length() > 8) {
       sb.append("T");
       sb.append(val.substring(9, 11));
       sb.append(":");
       sb.append(val.substring(11, 13));
       sb.append(":");
       sb.append(val.substring(13));
     }
 
     return sb.toString();
   }
 
   /**
    * @param dt
    * @return rfc5545 date or date/time
    */
   public static String getIcalFormatDateTime(final XMLGregorianCalendar dt) {
     if (dt == null) {
       return null;
     }
 
     return getIcalFormatDateTime(dt.toXMLFormat());
   }
 
   /**
    * @param dt
    * @return rfc5545 date or date/time
    */
   public static String getIcalFormatDateTime(final String dt) {
     if (dt == null) {
       return null;
     }
 
     if (dt.charAt(4) != '-') {
       // Already Ical format
       return dt;
     }
 
     StringBuilder sb = new StringBuilder();
 
     sb.append(dt.substring(0, 4));
     sb.append(dt.substring(5, 7));
     sb.append(dt.substring(8, 10));
 
     if (dt.length() > 10) {
       sb.append("T");
       sb.append(dt.substring(11, 13));
       sb.append(dt.substring(14, 16));
       sb.append(dt.substring(17, 19));
 
       if (dt.endsWith("Z")) {
         sb.append("Z");
       }
     }
 
     return sb.toString();
   }
 
   /**
    * @param ical
    * @param name
    * @return null or first matching component
    */
   public static BaseComponentType findComponent(final IcalendarType ical,
                                                 final QName name) {
     for (VcalendarType v: ical.getVcalendar()) {
       if (name.equals(XcalTags.vcalendar)) {
         return v;
       }
 
       BaseComponentType bc = findComponent(v, name);
       if (bc != null) {
         return bc;
       }
     }
 
     return null;
   }
 
   /** Get enclosed components for the supplied component.
    *
    * @param c
    * @return list of components or null for none or unrecognized class.
    */
   public static List<JAXBElement<? extends BaseComponentType>> getComponents(final BaseComponentType c) {
     if (c.getComponents() == null) {
       return null;
     }
 
     return new ArrayList<JAXBElement<? extends BaseComponentType>>(
         c.getComponents().getBaseComponent());
   }
 
   /**
    * @param bcPar
    * @param name
    * @return null or first matching component
    */
   public static BaseComponentType findComponent(final BaseComponentType bcPar,
                                                 final QName name) {
     List<JAXBElement<? extends BaseComponentType>> cs = getComponents(bcPar);
     if (cs == null) {
       return null;
     }
 
     for (JAXBElement<? extends BaseComponentType> bcel: cs) {
       if (bcel.getName().equals(name)) {
         return bcel.getValue();
       }
 
       BaseComponentType bc = findComponent(bcel.getValue(), name);
       if (bc != null) {
         return bc;
       }
     }
 
     return null;
   }
 
   /**
    * @param ical
    * @return null or first contained entity
    */
   public static BaseComponentType findEntity(final IcalendarType ical) {
     if (ical == null) {
       return null;
     }
 
     for (VcalendarType v: ical.getVcalendar()) {
       ArrayOfComponents cs = v.getComponents();
       if (cs == null) {
         continue;
       }
 
       for (JAXBElement<? extends BaseComponentType> bcel: cs.getBaseComponent()) {
         return bcel.getValue();
       }
     }
 
     return null;
   }
 
   /** Searches this entity for the named property. Does not recurse down.
    *
    * @param bcPar
    * @param name
    * @return null or first matching property
    */
   public static BasePropertyType findProperty(final BaseComponentType bcPar,
                                               final QName name) {
     if (bcPar == null) {
       return null;
     }
 
     ArrayOfProperties ps = bcPar.getProperties();
     if (ps == null) {
       return null;
     }
 
     for (JAXBElement<? extends BasePropertyType> bpel: ps.getBasePropertyOrTzid()) {
       if (bpel.getName().equals(name)) {
         return bpel.getValue();
       }
     }
 
     return null;
   }
 
   /** Searches the property for the named parameter.
    *
    * @param prop
    * @param name
    * @return null or first matching property
    */
   public static BaseParameterType findParam(final BasePropertyType prop,
                                             final QName name) {
     if (prop == null) {
       return null;
     }
 
     ArrayOfParameters ps = prop.getParameters();
     if (ps == null) {
       return null;
     }
 
     for (JAXBElement<? extends BaseParameterType> bpel: ps.getBaseParameter()) {
       if (bpel.getName().equals(name)) {
         return bpel.getValue();
       }
     }
 
     return null;
   }
 
   /**
    * @param cl
    * @return QName for component
    */
   public static QName getCompName(final Class cl) {
     return compNames.get(cl);
   }
 
   /**
    * @param name
    * @return component kind
    */
   public static int getCompKind(final QName name) {
     return compKinds.get(name);
   }
 
   private static void addInfo(final QName nm, final Integer kind, final Class cl) {
     compNames.put(cl, nm);
     compKinds.put(nm, kind);
   }
 }
