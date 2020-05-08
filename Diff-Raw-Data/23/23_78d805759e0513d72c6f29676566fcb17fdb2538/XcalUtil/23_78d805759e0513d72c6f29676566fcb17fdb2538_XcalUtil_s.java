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
 
 import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
 import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
 import ietf.params.xml.ns.icalendar_2.ArrayOfVcalendarContainedComponents;
 import ietf.params.xml.ns.icalendar_2.AvailableType;
 import ietf.params.xml.ns.icalendar_2.BaseComponentType;
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DaylightType;
 import ietf.params.xml.ns.icalendar_2.EventTodoComponentType;
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
 import ietf.params.xml.ns.icalendar_2.ObjectFactory;
 import ietf.params.xml.ns.icalendar_2.StandardType;
 import ietf.params.xml.ns.icalendar_2.TzidParamType;
 import ietf.params.xml.ns.icalendar_2.UntilRecurType;
 import ietf.params.xml.ns.icalendar_2.ValarmType;
 import ietf.params.xml.ns.icalendar_2.VavailabilityType;
 import ietf.params.xml.ns.icalendar_2.VcalendarType;
 import ietf.params.xml.ns.icalendar_2.VeventType;
 import ietf.params.xml.ns.icalendar_2.VtimezoneType;
 import ietf.params.xml.ns.icalendar_2.VtodoType;
 
 import java.util.ArrayList;
 import java.util.List;
 
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
      res.dt = xmlDtToIcalDt(dt.getDate().toString());
     } else {
      res.dt = xmlDtToIcalDt(dt.getDateTime().toString());
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
    * @return DtTzid filled in
    */
  public static String xmlDtToIcalDt(final String dt) {
     StringBuilder sb = new StringBuilder();
 
     sb.append(dt.substring(0, 4));
     sb.append(dt.substring(5, 7));
     sb.append(dt.substring(8, 10));
 
     if (dt.length() > 8) {
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
     if (c instanceof VcalendarType) {
       VcalendarType vc = (VcalendarType)c;
 
       if (vc.getComponents() == null) {
         return null;
       }
 
       return new ArrayList<JAXBElement<? extends BaseComponentType>>(
         vc.getComponents().getVcalendarContainedComponent());
     }
 
     if ((c instanceof VeventType) ||
         (c instanceof VtodoType)) {
       EventTodoComponentType etc = (EventTodoComponentType)c;
 
       if ((etc.getComponents() == null) ||
           (etc.getComponents().getValarm().size() == 0)) {
         return null;
       }
 
       ArrayList<JAXBElement<? extends BaseComponentType>> res =
         new ArrayList<JAXBElement<? extends BaseComponentType>>();
 
       for (ValarmType va: etc.getComponents().getValarm()) {
         res.add(new JAXBElement<ValarmType>(XcalTags.valarm,
             ValarmType.class, va));
       }
 
       return res;
     }
 
     if (c instanceof VavailabilityType) {
       VavailabilityType vav = (VavailabilityType)c;
 
       if ((vav.getComponents() == null) ||
           (vav.getComponents().getAvailable().size() == 0)) {
         return null;
       }
 
       ArrayList<JAXBElement<? extends BaseComponentType>> res =
         new ArrayList<JAXBElement<? extends BaseComponentType>>();
 
       for (AvailableType a: vav.getComponents().getAvailable()) {
         res.add(new JAXBElement<AvailableType>(XcalTags.available,
             AvailableType.class, a));
       }
 
       return res;
     }
 
     if (c instanceof VtimezoneType) {
       VtimezoneType etc = (VtimezoneType)c;
 
       if ((etc.getComponents() == null) ||
           (etc.getComponents().getStandardOrDaylight().size() == 0)) {
         return null;
       }
 
       ArrayList<JAXBElement<? extends BaseComponentType>> res =
         new ArrayList<JAXBElement<? extends BaseComponentType>>();
 
       for (BaseComponentType bc: etc.getComponents().getStandardOrDaylight()) {
         QName nm;
         if (bc instanceof DaylightType) {
           nm = XcalTags.daylight;
         } else if (bc instanceof StandardType) {
           nm = XcalTags.standard;
         } else {
           continue;
         }
 
         res.add(new JAXBElement<BaseComponentType>(nm,
             (Class<BaseComponentType>)bc.getClass(), bc));
       }
 
       return res;
     }
 
     return null;
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
       ArrayOfVcalendarContainedComponents cs = v.getComponents();
       if (cs == null) {
         continue;
       }
 
       for (JAXBElement<? extends BaseComponentType> bcel: cs.getVcalendarContainedComponent()) {
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
 
     for (JAXBElement<? extends BasePropertyType> bpel: ps.getBaseProperty()) {
       if (bpel.getName().equals(name)) {
         return bpel.getValue();
       }
     }
 
     return null;
   }
 
 }
