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
 package edu.rpi.cmt.calendar.diff;
 
 import edu.rpi.cmt.calendar.XcalUtil;
 import edu.rpi.sss.util.Util;
 import edu.rpi.sss.util.xml.NsContext;
 import edu.rpi.sss.util.xml.tagdefs.XcalTags;
 
 import ietf.params.xml.ns.icalendar_2.ActionPropType;
 import ietf.params.xml.ns.icalendar_2.AttachPropType;
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.CalAddressPropertyType;
 import ietf.params.xml.ns.icalendar_2.CalscalePropType;
 import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DurationPropType;
 import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
 import ietf.params.xml.ns.icalendar_2.GeoPropType;
 import ietf.params.xml.ns.icalendar_2.IntegerPropertyType;
 import ietf.params.xml.ns.icalendar_2.RecurPropertyType;
 import ietf.params.xml.ns.icalendar_2.RecurType;
 import ietf.params.xml.ns.icalendar_2.RequestStatusPropType;
 import ietf.params.xml.ns.icalendar_2.StatusPropType;
 import ietf.params.xml.ns.icalendar_2.TextListPropertyType;
 import ietf.params.xml.ns.icalendar_2.TextPropertyType;
 import ietf.params.xml.ns.icalendar_2.TranspPropType;
 import ietf.params.xml.ns.icalendar_2.TriggerPropType;
 import ietf.params.xml.ns.icalendar_2.UriPropertyType;
 import ietf.params.xml.ns.icalendar_2.UtcDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.UtcOffsetPropertyType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 /** This class wraps a property.
  *
  * @author Mike Douglass
  */
 class PropWrapper extends BaseEntityWrapper<PropWrapper,
                                             PropsWrapper,
                                             BasePropertyType>
             implements Comparable<PropWrapper> {
   private ParamsWrapper params;
 
   ValueType vt;
 
   /* Key is the real name of the property - result is null for no mapping or a
    * QName we treat it as for comparison.
    */
   private static Map<QName, QName> mappedNames = new HashMap<QName, QName>();
 
   static {
     final String ns = "urn:ietf:params:xml:ns:icalendar-2.0";
 
     mappedNames.put(new QName(ns, "x-bedework-exsynch-organizer"),
                     new QName(ns, "organizer"));
   }
 
   PropWrapper(final PropsWrapper parent,
               final QName name,
               final BasePropertyType p) {
     super(parent, name, p);
 
     List<JAXBElement<? extends BaseParameterType>> plist = null;
 
     if (p.getParameters() != null) {
       plist = p.getParameters().getBaseParameter();
     }
     params = new ParamsWrapper(this, plist);
   }
 
   @Override
   QName getMappedName(final QName name) {
     return mappedNames.get(name);
   }
 
   @Override
   ValueType getUpdateValue() {
     return getValue();
   }
 
   @Override
   boolean sameEntity(final BaseEntityWrapper val) {
     int res = super.compareNameClass(val);
     if (res != 0) {
       return false;
     }
 
     return true;
   }
 
   @Override
   public void appendXpathElement(final StringBuilder sb,
                                  final NsContext nsContext) {
     appendNsName(sb, nsContext);
 
     // Append value element.
 
     ValueType vt = getValue();
     sb.append("[");
 
     // XXX This is wrong - just using first element for the moment
     ValueTypeEntry vte = vt.vtes.get(0);
     nsContext.appendNsName(sb, vte.typeElement);
 
     sb.append("/text()='");
     sb.append(vte.value);
     sb.append("']");
   }
 
   /** Creates a diff value if the values differ. Sets that.diffVal
    *
    * @param that
    * @return List<BaseEntityWrapper>
    */
   public List<BaseEntityWrapper> diff(final PropWrapper that) {
     List<BaseEntityWrapper> u = new ArrayList<BaseEntityWrapper>();
 
     if (!getValue().equals(that.getValue())) {
       that.setDiffVal(this);
       u.add(that);
       return u;
     }
 
     that.setDiffVal(null);
 
     u.addAll(params.diff(that.params));
     return u;
   }
 
   ValueType getValue() {
     if (vt != null) {
       return vt;
     }
 
     BasePropertyType p = getEntity();
 
     if (p instanceof ActionPropType) {
       vt = new ValueType(XcalTags.textVal,
                          ((ActionPropType)p).getText().toString());
       return vt;
     }
 
     if (p instanceof FreebusyPropType) {
       vt = new ValueType(XcalTags.periodVal,
                          ((FreebusyPropType)p).getPeriod().toString());
       return vt;
     }
 
     if (p instanceof RequestStatusPropType) {
       RequestStatusPropType rs = (RequestStatusPropType)p;
 
       vt = new ValueType(XcalTags.codeVal, rs.getCode());
 
       if (rs.getDescription() != null) {
         vt.vtes.add(new ValueTypeEntry(XcalTags.descriptionVal,
                                        rs.getDescription()));
       }
 
       if (rs.getExtdata() != null) {
         vt.vtes.add(new ValueTypeEntry(XcalTags.extdataVal,
                                        rs.getExtdata()));
       }
 
       return vt;
     }
 
     if (p instanceof GeoPropType) {
       GeoPropType gp = (GeoPropType)p;
 
       vt = new ValueType(XcalTags.latitudeVal,
                          String.valueOf(gp.getLatitude()));
       vt.vtes.add(new ValueTypeEntry(XcalTags.longitudeVal,
                                      String.valueOf(gp.getLongitude())));
 
       return vt;
     }
 
     if (p instanceof StatusPropType) {
       vt = new ValueType(XcalTags.textVal,
                          ((StatusPropType)p).getText().toString());
       return vt;
     }
 
     if (p instanceof TranspPropType) {
       vt = new ValueType(XcalTags.textVal,
                          ((TranspPropType)p).getText().toString());
       return vt;
     }
 
     if (p instanceof CalscalePropType) {
       vt= new ValueType(XcalTags.textVal,
                         ((CalscalePropType)p).getText().toString());
       return vt;
     }
 
     if (p instanceof TriggerPropType) {
       TriggerPropType tp = (TriggerPropType)p;
       if (tp.getDuration() != null) {
         vt = new ValueType(XcalTags.durationVal,
                            tp.getDuration().toString());
       } else {
         vt = new ValueType(XcalTags.dateTimeVal,
                            tp.getDateTime().toString());
       }
 
       return vt;
     }
 
     if (p instanceof DurationPropType) {
       vt = new ValueType(XcalTags.durationVal,
                          ((DurationPropType)p).getDuration().toString());
       return vt;
     }
 
     if (p instanceof AttachPropType) {
       AttachPropType ap = (AttachPropType)p;
 
       if (ap.getBinary() !=  null) {
         vt = new ValueType(XcalTags.binaryVal,
                            ap.getBinary());
       } else {
         vt = new ValueType(XcalTags.uriVal,
                            ap.getUri());
       }
 
       return vt;
     }
 
     if (p instanceof DateDatetimePropertyType) {
       XcalUtil.DtTzid dtTzid = XcalUtil.getDtTzid((DateDatetimePropertyType)p);
 
       if (dtTzid.dateOnly) {
         vt = new ValueType(XcalTags.dateVal,
                            dtTzid.dt);
       } else {
         vt = new ValueType(XcalTags.dateTimeVal,
                            dtTzid.dt);
       }
 
       return vt;
     }
 
     if (p instanceof DatetimePropertyType) {
       vt = new ValueType(XcalTags.dateTimeVal,
                         XcalUtil.xmlDtToIcalDt(((DatetimePropertyType)p).getDateTime().toString()));
       return vt;
     }
 
     if (p instanceof UtcDatetimePropertyType) {
       vt = new ValueType(XcalTags.utcDateTimeVal,
                         XcalUtil.xmlDtToIcalDt(((UtcDatetimePropertyType)p).getUtcDateTime().toString()));
       return vt;
     }
 
     if (p instanceof CalAddressPropertyType) {
       vt = new ValueType(XcalTags.calAddressVal,
                          ((CalAddressPropertyType)p).getCalAddress());
       return vt;
     }
 
     if (p instanceof UtcOffsetPropertyType) {
       vt = new ValueType(XcalTags.utcOffsetVal,
                          ((UtcOffsetPropertyType)p).getUtcOffset());
       return vt;
     }
 
     if (p instanceof TextListPropertyType) {
       List<String> ss = ((TextListPropertyType)p).getText();
 
       vt = new ValueType();
 
       for (String s: ss) {
         vt.vtes.add(new ValueTypeEntry(XcalTags.textVal, s));
       }
 
       return vt;
     }
 
     if (p instanceof TextPropertyType) {
       vt = new ValueType(XcalTags.textVal,
                          ((TextPropertyType)p).getText());
       return vt;
     }
 
     if (p instanceof RecurPropertyType) {
       RecurType r = ((RecurPropertyType)p).getRecur();
 
       vt = new ValueType();
 
       append(vt, XcalTags.freq, r.getFreq().toString());
       append(vt, XcalTags.count, r.getCount());
       append(vt, XcalTags.until, r.getUntil());
       append(vt, XcalTags.interval, r.getInterval());
       append(vt, XcalTags.bysecond, r.getBysecond());
       append(vt, XcalTags.byminute, r.getByminute());
       append(vt, XcalTags.byhour, r.getByhour());
       append(vt, XcalTags.byday, r.getByday());
       append(vt, XcalTags.byyearday, r.getByyearday());
       append(vt, XcalTags.bymonthday, r.getBymonthday());
       append(vt, XcalTags.byweekno, r.getByweekno());
       append(vt, XcalTags.bymonth, r.getBymonth());
       append(vt, XcalTags.bysetpos, r.getBysetpos());
       append(vt, XcalTags.wkst, r.getWkst().toString());
 
       return vt;
     }
 
     if (p instanceof IntegerPropertyType) {
       vt = new ValueType(XcalTags.integerVal,
                          String.valueOf(((IntegerPropertyType)p).getInteger()));
       return vt;
     }
 
     if (p instanceof UriPropertyType) {
       vt = new ValueType(XcalTags.uriVal,
                          ((UriPropertyType)p).getUri());
       return vt;
     }
 
     return null;
   }
 
   private void append(final ValueType vt,
                       final QName nm,
                       final List val) {
     if (val == null) {
       return;
     }
 
     for (Object o: val) {
       append(vt, nm, o);
     }
   }
 
   private void append(final ValueType vt,
                       final QName nm,
                       final Object val) {
     if (val == null) {
       return;
     }
 
     vt.vtes.add(new ValueTypeEntry(nm, String.valueOf(val)));
   }
 
   public int compareTo(final PropWrapper o) {
     int res = super.compareNameClass(o);
     if (res != 0) {
       return res;
     }
 
     res = Util.cmpObjval(getValue(), o.getValue());
 
     if (res != 0) {
       return res;
     }
 
     // Try params
 
     return params.compareTo(o.params);
   }
 
   @Override
   public int hashCode() {
     return getName().hashCode() * getValue().hashCode();
   }
 
   @Override
   public boolean equals(final Object o) {
     return compareTo((PropWrapper)o) == 0;
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder("PropWrapper{");
 
     super.toStringSegment(sb);
 
     if (params.size() > 0) {
       sb.append(", params=");
       sb.append(params);
     }
 
     sb.append(", value=\"");
     sb.append(getValue());
     sb.append("\"");
 
     sb.append("}");
 
     return sb.toString();
   }
 }
