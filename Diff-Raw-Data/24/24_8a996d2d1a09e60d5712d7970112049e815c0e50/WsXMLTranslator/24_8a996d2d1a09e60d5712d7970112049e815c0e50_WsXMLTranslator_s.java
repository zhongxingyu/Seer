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
 
 import edu.rpi.cmt.calendar.PropertyIndex.ComponentInfoIndex;
 import edu.rpi.cmt.calendar.PropertyIndex.PropertyInfoIndex;
 import edu.rpi.cmt.calendar.XmlCalendarBuilder.BuildState;
 import edu.rpi.cmt.calendar.XmlCalendarBuilder.ContentHandlerImpl;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.TimeZoneRegistry;
 
 import org.apache.log4j.Logger;
 
 import ietf.params.xml.ns.icalendar_2.ActionPropType;
 import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
 import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
 import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
 import ietf.params.xml.ns.icalendar_2.AttachPropType;
 import ietf.params.xml.ns.icalendar_2.BaseComponentType;
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.CalAddressListParamType;
 import ietf.params.xml.ns.icalendar_2.CalAddressParamType;
 import ietf.params.xml.ns.icalendar_2.CalAddressPropertyType;
 import ietf.params.xml.ns.icalendar_2.CalscalePropType;
 import ietf.params.xml.ns.icalendar_2.ClassPropType;
 import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DurationParameterType;
 import ietf.params.xml.ns.icalendar_2.DurationPropType;
 import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
 import ietf.params.xml.ns.icalendar_2.GeoPropType;
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
 import ietf.params.xml.ns.icalendar_2.IntegerPropertyType;
 import ietf.params.xml.ns.icalendar_2.RangeParamType;
 import ietf.params.xml.ns.icalendar_2.RecurPropertyType;
 import ietf.params.xml.ns.icalendar_2.RecurType;
 import ietf.params.xml.ns.icalendar_2.RequestStatusPropType;
 import ietf.params.xml.ns.icalendar_2.StatusPropType;
 import ietf.params.xml.ns.icalendar_2.TextListPropertyType;
 import ietf.params.xml.ns.icalendar_2.TextParameterType;
 import ietf.params.xml.ns.icalendar_2.TextPropertyType;
 import ietf.params.xml.ns.icalendar_2.TranspPropType;
 import ietf.params.xml.ns.icalendar_2.TriggerPropType;
 import ietf.params.xml.ns.icalendar_2.UntilRecurType;
 import ietf.params.xml.ns.icalendar_2.UriParameterType;
 import ietf.params.xml.ns.icalendar_2.UriPropertyType;
 import ietf.params.xml.ns.icalendar_2.UtcDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.UtcOffsetPropertyType;
 import ietf.params.xml.ns.icalendar_2.VcalendarType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 /** Convert to/from Web Services style XML. On input this has been parsed by the
  * framework. For output we need to build a JAXB compliant structure.
  *
  * <p>For the moment we build an ical4j structure and use that to create events
  *
  * @author douglm
  */
 public class WsXMLTranslator {
   //private Logger log = Logger.getLogger(WsXMLTranslator.class);
 
   private final TimeZoneRegistry tzRegistry;
 
   /**
    * @param tzRegistry
    */
   public WsXMLTranslator(final TimeZoneRegistry tzRegistry) {
     this.tzRegistry = tzRegistry;
   }
 
   /**
    * @param ical
    * @return Calendar object or null for no data
    * @throws Throwable
    */
   public Calendar fromXcal(final IcalendarType ical) throws Throwable {
     BuildState bs = new BuildState(tzRegistry);
 
     bs.setContentHandler(new ContentHandlerImpl(bs));
 
     List<VcalendarType> vcts = ical.getVcalendar();
     if (vcts.size() == 0) {
       return null;
     }
 
     if (vcts.size() > 1) {
       throw new Exception("More than one vcalendar");
     }
 
     processVcalendar(vcts.get(0), bs);
 
     return bs.getCalendar();
   }
 
   /**
    * @param comp
    * @return Calendar object or null for no data
    * @throws Throwable
    */
   public Calendar fromXcomp(final JAXBElement<? extends BaseComponentType> comp) throws Throwable {
     IcalendarType ical = new IcalendarType();
 
     List<VcalendarType> vcts = ical.getVcalendar();
 
     VcalendarType vcal = new VcalendarType();
     vcts.add(vcal);
 
     ArrayOfComponents aop = new ArrayOfComponents();
 
     vcal.setComponents(aop);
     aop.getBaseComponent().add(comp);
 
     return fromXcal(ical);
   }
 
   private void processVcalendar(final VcalendarType vcal,
                                 final BuildState bs) throws Throwable {
     bs.getContentHandler().startCalendar();
 
     processProperties(vcal.getProperties(), bs);
 
     processCalcomps(vcal, bs);
   }
 
   private void processProperties(final ArrayOfProperties aop,
                                  final BuildState bs) throws Throwable {
     if ((aop == null) || (aop.getBasePropertyOrTzid().size() == 0)) {
       return;
     }
 
     for (JAXBElement<?> e: aop.getBasePropertyOrTzid()) {
       processProperty((BasePropertyType)e.getValue(),
                       e.getName(), bs);
     }
   }
 
   /* Process all the sub-components of the supplied component */
   private void processCalcomps(final BaseComponentType c,
                                final BuildState bs) throws Throwable {
     List<JAXBElement<? extends BaseComponentType>> comps =
       XcalUtil.getComponents(c);
 
     if (comps == null) {
       return;
     }
 
     for (JAXBElement<? extends BaseComponentType> el: comps) {
       processComponent(el.getValue(), bs);
     }
   }
 
   private void processComponent(final BaseComponentType comp,
                                 final BuildState bs) throws Throwable {
     ComponentInfoIndex cii = ComponentInfoIndex.fromXmlClass(comp.getClass());
 
     if (cii == null) {
       throw new Exception("Unknown component " + comp.getClass());
     }
 
     String name = cii.getPname();
     bs.getContentHandler().startComponent(name);
 
     processProperties(comp.getProperties(), bs);
 
     processCalcomps(comp, bs);
 
     bs.getContentHandler().endComponent(name);
   }
 
   private void processProperty(final BasePropertyType prop,
                                final QName elname,
                                final BuildState bs) throws Throwable {
     PropertyInfoIndex pii = PropertyInfoIndex.fromXmlClass(prop.getClass());
     String name;
     if (pii == null) {
       name = elname.getLocalPart().toUpperCase();
     } else {
       name = pii.getPname();
     }
 
     bs.getContentHandler().startProperty(name);
 
     ArrayOfParameters aop = prop.getParameters();
 
     if (aop != null) {
       for (JAXBElement<? extends BaseParameterType> e: aop.getBaseParameter()) {
         String parName = e.getName().getLocalPart().toUpperCase();
 
         bs.getContentHandler().parameter(parName, getParValue(e.getValue()));
       }
     }
 
     if (!processValue(prop, bs)) {
       throw new Exception("Bad property " + prop);
     }
 
     bs.getContentHandler().endProperty(name);
   }
 
   public String fromRecurProperty(final RecurPropertyType rp) {
     RecurType r = rp.getRecur();
 
     List<String> rels = new ArrayList<String>();;
 
     /*
     value-recur = element recur {
       type-freq,
       (type-until | type-count)?,
       element interval  { text }?,
       element bysecond  { text }*,
       element byminute  { text }*,
       element byhour    { text }*,
       type-byday*,
       type-bymonthday*,
       type-byyearday*,
       type-byweekno*,
       element bymonth   { text }*,
       type-bysetpos*,
       element wkst { type-weekday }?
     }
 
      */
     addRecurEl(rels, "FREQ", r.getFreq());
 
     if (r.getUntil() != null) {
       UntilRecurType until = r.getUntil();
       if (until.getDate() != null) {
         rels.add("UNTIL=" + until.getDate());
       } else {
         rels.add("UNTIL=" + until.getDateTime());
       }
     }
 
     addRecurEl(rels, "COUNT", r.getCount());
     addRecurEl(rels, "INTERVAL", r.getInterval());
     addRecurEl(rels, "BYSECOND", r.getBysecond());
     addRecurEl(rels, "BYMINUTE", r.getByminute());
     addRecurEl(rels, "BYHOUR", r.getByhour());
     addRecurEl(rels, "BYDAY", r.getByday());
     addRecurEl(rels, "BYMONTHDAY", r.getBymonthday());
     addRecurEl(rels, "BYYEARDAY", r.getByyearday());
     addRecurEl(rels, "BYWEEKNO", r.getByweekno());
     addRecurEl(rels, "BYMONTH", r.getBymonth());
     addRecurEl(rels, "BYSETPOS", r.getBysetpos());
     addRecurEl(rels, "WKST", r.getWkst());
 
     return fromList(rels, false, ";");
   }
 
   private boolean processValue(final BasePropertyType prop,
                                final BuildState bs) throws Throwable {
     if (prop instanceof RecurPropertyType) {
       propVal(bs, fromRecurProperty((RecurPropertyType)prop));
 
       return true;
     }
 
     if (prop instanceof DurationPropType) {
       DurationPropType dp = (DurationPropType)prop;
 
       propVal(bs, dp.getDuration().toString());
 
       return true;
     }
 
     if (prop instanceof TextPropertyType) {
       TextPropertyType tp = (TextPropertyType)prop;
 
       propVal(bs, tp.getText());
 
       return true;
     }
 
     if (prop instanceof TextListPropertyType) {
       TextListPropertyType p = (TextListPropertyType)prop;
 
       propVal(bs, fromList(p.getText(), false));
 
       return true;
     }
 
     if (prop instanceof CalAddressPropertyType) {
       CalAddressPropertyType cap = (CalAddressPropertyType)prop;
 
       propVal(bs, cap.getCalAddress());
 
       return true;
     }
 
     if (prop instanceof IntegerPropertyType) {
       IntegerPropertyType ip = (IntegerPropertyType)prop;
 
       propVal(bs, String.valueOf(ip.getInteger()));
 
       return true;
     }
 
     if (prop instanceof UriPropertyType) {
       UriPropertyType p = (UriPropertyType)prop;
 
       propVal(bs, p.getUri());
 
       return true;
     }
 
     if (prop instanceof UtcOffsetPropertyType) {
       UtcOffsetPropertyType p = (UtcOffsetPropertyType)prop;
 
       propVal(bs, p.getUtcOffset());
 
       return true;
     }
 
     if (prop instanceof UtcDatetimePropertyType) {
       UtcDatetimePropertyType p = (UtcDatetimePropertyType)prop;
 
       propVal(bs, XcalUtil.getIcalFormatDateTime(p.getUtcDateTime().toString()));
 
       return true;
     }
 
     if (prop instanceof DatetimePropertyType) {
       DatetimePropertyType p = (DatetimePropertyType)prop;
 
       propVal(bs, XcalUtil.getIcalFormatDateTime(p.getDateTime().toString()));
 
       return true;
     }
 
     if (prop instanceof DateDatetimePropertyType) {
       XcalUtil.DtTzid dtTzid = XcalUtil.getDtTzid((DateDatetimePropertyType)prop);
 
       propVal(bs, dtTzid.dt);
 
       return true;
     }
 
     if (prop instanceof CalscalePropType) {
       CalscalePropType p = (CalscalePropType)prop;
 
       propVal(bs, p.getText().name());
 
       return true;
     }
 
     if (prop instanceof AttachPropType) {
       AttachPropType p = (AttachPropType)prop;
 
       if (p.getUri() != null) {
         propVal(bs, p.getUri());
       } else {
         propVal(bs, p.getBinary());
       }
 
       return true;
     }
 
     if (prop instanceof ClassPropType) {
       ClassPropType p = (ClassPropType)prop;
 
       propVal(bs, p.getText());
 
       return true;
     }
 
     if (prop instanceof GeoPropType) {
       GeoPropType p = (GeoPropType)prop;
 
       propVal(bs, p.getLatitude() + ";" + p.getLongitude());
 
       return true;
     }
 
     if (prop instanceof StatusPropType) {
       StatusPropType p = (StatusPropType)prop;
 
       propVal(bs, p.getText());
 
       return true;
     }
 
     if (prop instanceof FreebusyPropType) {
       FreebusyPropType p = (FreebusyPropType)prop;
 
       propVal(bs, fromList(p.getPeriod(), false));
 
       return true;
     }
 
     if (prop instanceof TranspPropType) {
       TranspPropType p = (TranspPropType)prop;
 
       propVal(bs, p.getText());
 
       return true;
     }
 
     if (prop instanceof ActionPropType) {
       ActionPropType p = (ActionPropType)prop;
 
       propVal(bs, p.getText());
 
       return true;
     }
 
     if (prop instanceof TriggerPropType) {
       TriggerPropType p = (TriggerPropType)prop;
 
       if (p.getDuration() != null) {
         propVal(bs, p.getDuration().toString());
       } else {
         propVal(bs, XcalUtil.getIcalFormatDateTime(p.getDateTime().toString()));
       }
 
       return true;
     }
 
     if (prop instanceof RequestStatusPropType) {
       RequestStatusPropType p = (RequestStatusPropType)prop;
 
       StringBuilder sb = new StringBuilder();
 
       sb.append(p.getCode());
       if (p.getDescription() != null) {
         sb.append(";");
         sb.append(p.getDescription());
       }
 
       if (p.getExtdata() != null) {
         sb.append(";");
         sb.append(p.getExtdata());
       }
 
       propVal(bs, sb.toString());
 
       return true;
     }
 
     return false;
   }
 
   private void addRecurEl(final List<String> l, final String name, final Object o) {
     if (o == null) {
       return;
     }
 
     String val;
 
     if (o instanceof List) {
       val = fromList((List<?>)o, false);
       if (val == null) {
         return;
       }
     } else {
       val = String.valueOf(o);
     }
 
     l.add(name + "=" + val);
   }
 
   private void propVal(final BuildState bs,
                        final String val) throws Throwable {
     bs.getContentHandler().propertyValue(val);
   }
 
   private String getParValue(final BaseParameterType bpt) throws Throwable {
     if (bpt instanceof TextParameterType) {
       return ((TextParameterType)bpt).getText();
     }
 
     if (bpt instanceof DurationParameterType) {
       return ((DurationParameterType)bpt).getDuration().toString();
     }
 
     if (bpt instanceof RangeParamType) {
       return ((RangeParamType)bpt).getText().value();
     }
 
     if (bpt instanceof CalAddressListParamType) {
       return fromList(((CalAddressListParamType)bpt).getCalAddress(), true);
     }
 
     if (bpt instanceof CalAddressParamType) {
       return ((CalAddressParamType)bpt).getCalAddress();
     }
 
     if (bpt instanceof UriParameterType) {
       return ((UriParameterType)bpt).getUri();
     }
 
     throw new Exception("Unsupported param type");
   }
 
   private String fromList(final List<?> l, final boolean quote) {
     return fromList(l, quote, ",");
   }
 
   private String fromList(final List<?> l,
                           final boolean quote,
                           final String delimChar) {
     if ((l == null) || l.isEmpty()) {
       return null;
     }
 
     StringBuilder sb = new StringBuilder();
     String delim = "";
     String qt = "";
 
     if (quote) {
       qt = "\"";
     }
 
     for (Object o: l) {
       sb.append(delim);
       delim = delimChar;
       sb.append(qt);
       sb.append(o);
       sb.append(qt);
     }
 
     return sb.toString();
   }
 
   /* ====================================================================
                       Private methods
      ==================================================================== */
 
   /**
    * @return Logger
    */
   public static Logger getLog() {
     return Logger.getLogger(WsXMLTranslator.class);
   }
 
   /**
    * @param t
    */
   public static void error(final Throwable t) {
     getLog().error(WsXMLTranslator.class, t);
   }
 
   /**
    * @param msg
    */
   public static void warn(final String msg) {
     getLog().warn(msg);
   }
 }
