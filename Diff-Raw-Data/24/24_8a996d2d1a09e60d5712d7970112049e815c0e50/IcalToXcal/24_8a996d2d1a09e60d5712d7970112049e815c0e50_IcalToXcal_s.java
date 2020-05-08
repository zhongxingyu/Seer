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
 
 import edu.rpi.cmt.calendar.PropertyIndex.ParameterInfoIndex;
 import edu.rpi.cmt.calendar.PropertyIndex.PropertyInfoIndex;
 import edu.rpi.sss.util.Util;
 
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.CategoryList;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.ComponentList;
 import net.fortuna.ical4j.model.Date;
 import net.fortuna.ical4j.model.NumberList;
 import net.fortuna.ical4j.model.Parameter;
 import net.fortuna.ical4j.model.ParameterList;
 import net.fortuna.ical4j.model.Period;
 import net.fortuna.ical4j.model.PeriodList;
 import net.fortuna.ical4j.model.Property;
 import net.fortuna.ical4j.model.PropertyList;
 import net.fortuna.ical4j.model.Recur;
 import net.fortuna.ical4j.model.ResourceList;
 import net.fortuna.ical4j.model.WeekDay;
 import net.fortuna.ical4j.model.component.CalendarComponent;
 import net.fortuna.ical4j.model.component.Daylight;
 import net.fortuna.ical4j.model.component.Standard;
 import net.fortuna.ical4j.model.component.VAlarm;
 import net.fortuna.ical4j.model.component.VEvent;
 import net.fortuna.ical4j.model.component.VFreeBusy;
 import net.fortuna.ical4j.model.component.VJournal;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.model.component.VToDo;
 import net.fortuna.ical4j.model.parameter.TzId;
 import net.fortuna.ical4j.model.parameter.Value;
 import net.fortuna.ical4j.model.property.Categories;
 import net.fortuna.ical4j.model.property.FreeBusy;
 import net.fortuna.ical4j.model.property.Geo;
 import net.fortuna.ical4j.model.property.PercentComplete;
 import net.fortuna.ical4j.model.property.Priority;
 import net.fortuna.ical4j.model.property.RRule;
 import net.fortuna.ical4j.model.property.Repeat;
 import net.fortuna.ical4j.model.property.Resources;
 import net.fortuna.ical4j.model.property.Sequence;
 import net.fortuna.ical4j.model.property.XProperty;
 
 import ietf.params.xml.ns.icalendar_2.ActionPropType;
 import ietf.params.xml.ns.icalendar_2.AltrepParamType;
 import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
 import ietf.params.xml.ns.icalendar_2.ArrayOfParameters;
 import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
 import ietf.params.xml.ns.icalendar_2.AttendeePropType;
 import ietf.params.xml.ns.icalendar_2.BaseComponentType;
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.CategoriesPropType;
 import ietf.params.xml.ns.icalendar_2.ClassPropType;
 import ietf.params.xml.ns.icalendar_2.CnParamType;
 import ietf.params.xml.ns.icalendar_2.CommentPropType;
 import ietf.params.xml.ns.icalendar_2.CompletedPropType;
 import ietf.params.xml.ns.icalendar_2.ContactPropType;
 import ietf.params.xml.ns.icalendar_2.CreatedPropType;
 import ietf.params.xml.ns.icalendar_2.CutypeParamType;
 import ietf.params.xml.ns.icalendar_2.DateDatetimePropertyType;
 import ietf.params.xml.ns.icalendar_2.DaylightType;
 import ietf.params.xml.ns.icalendar_2.DelegatedFromParamType;
 import ietf.params.xml.ns.icalendar_2.DelegatedToParamType;
 import ietf.params.xml.ns.icalendar_2.DescriptionPropType;
 import ietf.params.xml.ns.icalendar_2.DirParamType;
 import ietf.params.xml.ns.icalendar_2.DtendPropType;
 import ietf.params.xml.ns.icalendar_2.DtstampPropType;
 import ietf.params.xml.ns.icalendar_2.DtstartPropType;
 import ietf.params.xml.ns.icalendar_2.DuePropType;
 import ietf.params.xml.ns.icalendar_2.DurationPropType;
 import ietf.params.xml.ns.icalendar_2.ExrulePropType;
 import ietf.params.xml.ns.icalendar_2.FbtypeParamType;
 import ietf.params.xml.ns.icalendar_2.FreebusyPropType;
 import ietf.params.xml.ns.icalendar_2.FreqRecurType;
 import ietf.params.xml.ns.icalendar_2.GeoPropType;
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
 import ietf.params.xml.ns.icalendar_2.LanguageParamType;
 import ietf.params.xml.ns.icalendar_2.LastModifiedPropType;
 import ietf.params.xml.ns.icalendar_2.LocationPropType;
 import ietf.params.xml.ns.icalendar_2.MemberParamType;
 import ietf.params.xml.ns.icalendar_2.MethodPropType;
 import ietf.params.xml.ns.icalendar_2.ObjectFactory;
 import ietf.params.xml.ns.icalendar_2.OrganizerPropType;
 import ietf.params.xml.ns.icalendar_2.PartstatParamType;
 import ietf.params.xml.ns.icalendar_2.PercentCompletePropType;
 import ietf.params.xml.ns.icalendar_2.PeriodType;
 import ietf.params.xml.ns.icalendar_2.PriorityPropType;
 import ietf.params.xml.ns.icalendar_2.ProdidPropType;
 import ietf.params.xml.ns.icalendar_2.RecurType;
 import ietf.params.xml.ns.icalendar_2.RecurrenceIdPropType;
 import ietf.params.xml.ns.icalendar_2.RelatedParamType;
 import ietf.params.xml.ns.icalendar_2.RelatedToPropType;
 import ietf.params.xml.ns.icalendar_2.ReltypeParamType;
 import ietf.params.xml.ns.icalendar_2.RepeatPropType;
 import ietf.params.xml.ns.icalendar_2.ResourcesPropType;
 import ietf.params.xml.ns.icalendar_2.RoleParamType;
 import ietf.params.xml.ns.icalendar_2.RrulePropType;
 import ietf.params.xml.ns.icalendar_2.ScheduleStatusParamType;
 import ietf.params.xml.ns.icalendar_2.SentByParamType;
 import ietf.params.xml.ns.icalendar_2.SequencePropType;
 import ietf.params.xml.ns.icalendar_2.StandardType;
 import ietf.params.xml.ns.icalendar_2.StatusPropType;
 import ietf.params.xml.ns.icalendar_2.SummaryPropType;
 import ietf.params.xml.ns.icalendar_2.TranspPropType;
 import ietf.params.xml.ns.icalendar_2.TriggerPropType;
 import ietf.params.xml.ns.icalendar_2.TzidParamType;
 import ietf.params.xml.ns.icalendar_2.UidPropType;
 import ietf.params.xml.ns.icalendar_2.UntilRecurType;
 import ietf.params.xml.ns.icalendar_2.UrlPropType;
 import ietf.params.xml.ns.icalendar_2.ValarmType;
 import ietf.params.xml.ns.icalendar_2.VcalendarType;
 import ietf.params.xml.ns.icalendar_2.VersionPropType;
 import ietf.params.xml.ns.icalendar_2.VeventType;
 import ietf.params.xml.ns.icalendar_2.VfreebusyType;
 import ietf.params.xml.ns.icalendar_2.VjournalType;
 import ietf.params.xml.ns.icalendar_2.VtimezoneType;
 import ietf.params.xml.ns.icalendar_2.VtodoType;
 import ietf.params.xml.ns.icalendar_2.XBedeworkCostPropType;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 
 /** Conversion to XML from ical4j
  * @author douglm
  */
 public class IcalToXcal {
   static ObjectFactory of = new ObjectFactory();
 
   /**
    * @param cal
    * @param pattern - allows specification of a subset to be returned.
    * @return icalendar in XML
    * @throws Throwable
    */
   public static IcalendarType fromIcal(final Calendar cal,
                                        final BaseComponentType pattern) throws Throwable {
     return fromIcal(cal, pattern, false);
   }
 
   @SuppressWarnings("unchecked")
   public static IcalendarType fromIcal(final Calendar cal,
                                        final BaseComponentType pattern,
                                        final boolean doTimezones) throws Throwable {
     IcalendarType ical = new IcalendarType();
     VcalendarType vcal = new VcalendarType();
 
     ical.getVcalendar().add(vcal);
 
     processProperties(cal.getProperties(), vcal, pattern);
 
     ComponentList icComps = cal.getComponents();
 
     if (icComps == null) {
       return ical;
     }
 
     ArrayOfComponents aoc = new ArrayOfComponents();
     vcal.setComponents(aoc);
 
     for (Object o: icComps) {
       if (!doTimezones && (o instanceof VTimeZone)) {
         // Skip these
         continue;
       }
       aoc.getBaseComponent().add(toComponent((CalendarComponent)o,
                                                            pattern));
     }
 
     return ical;
   }
 
   /** Make a BaseComponentType component from an ical4j object. This may produce a
    * VEvent, VTodo or VJournal.
    *
    * @param val
    * @param pattern - if non-null limit returned components and values to those
    *                  supplied in the pattern.
    * @return JAXBElement<? extends BaseComponentType>
    * @throws Throwable
    */
   public static JAXBElement
                     toComponent(final Component val,
                                 final BaseComponentType pattern) throws Throwable {
     if (val == null) {
       return null;
     }
 
     PropertyList icprops = val.getProperties();
     ComponentList icComps = null;
 
     if (icprops == null) {
       // Empty component
       return null;
     }
 
     JAXBElement el;
 
     if (val instanceof VEvent) {
       el = of.createVevent(new VeventType());
       icComps = ((VEvent)val).getAlarms();
     } else if (val instanceof VToDo) {
       el = of.createVtodo(new VtodoType());
       icComps = ((VToDo)val).getAlarms();
     } else if (val instanceof VJournal) {
       el = of.createVjournal(new VjournalType());
     } else if (val instanceof VFreeBusy) {
       el = of.createVfreebusy(new VfreebusyType());
     } else if (val instanceof VAlarm) {
       el = of.createValarm(new ValarmType());
     } else if (val instanceof VTimeZone) {
       el = of.createVtimezone(new VtimezoneType());
       icComps = ((VTimeZone)val).getObservances();
     } else if (val instanceof Daylight) {
       el = of.createDaylight(new DaylightType());
     } else if (val instanceof Standard) {
       el = of.createStandard(new StandardType());
     } else {
       throw new Exception("org.bedework.invalid.entity.type" +
           val.getClass().getName());
     }
 
     BaseComponentType comp = (BaseComponentType)el.getValue();
 
     processProperties(val.getProperties(), comp, pattern);
 
     if (Util.isEmpty(icComps)) {
       return el;
     }
 
     /* Process any sub-components */
     ArrayOfComponents aoc = new ArrayOfComponents();
     comp.setComponents(aoc);
 
     for (Object o: icComps) {
       @SuppressWarnings("unchecked")
       JAXBElement<? extends BaseComponentType> subel = toComponent((Component)o,
                                       pattern);
       aoc.getBaseComponent().add(subel);
     }
 
     return el;
   }
 
   /**
    * @param icprops
    * @param comp
    * @param pattern
    * @throws Throwable
    */
   public static void processProperties(final PropertyList icprops,
                                        final BaseComponentType comp,
                                        final BaseComponentType pattern) throws Throwable {
     if ((icprops == null) || icprops.isEmpty()) {
       return;
     }
 
     comp.setProperties(new ArrayOfProperties());
     List<JAXBElement<? extends BasePropertyType>> pl = comp.getProperties().getBasePropertyOrTzid();
 
     Iterator it = icprops.iterator();
 
     while (it.hasNext()) {
       Property prop = (Property)it.next();
 
       PropertyInfoIndex pii = PropertyInfoIndex.lookupPname(prop.getName());
 
       if (pii == null) {
         continue;
       }
 
       if (!emit(pattern, comp.getClass(), pii.getXmlClass())) {
         continue;
       }
 
       JAXBElement<? extends BasePropertyType> xmlprop =
           doProperty(prop, pii);
 
       if (xmlprop != null) {
         processParameters(prop.getParameters(), xmlprop.getValue());
         pl.add(xmlprop);
       }
     }
   }
 
   static JAXBElement<? extends BasePropertyType> doProperty(final Property prop,
                                                      final PropertyInfoIndex pii) throws Throwable {
     switch (pii) {
       case ACTION:
         /* ------------------- Action: Alarm -------------------- */
 
         ActionPropType a = new ActionPropType();
         a.setText(prop.getValue());
         return of.createAction(a);
 
       case ATTACH:
         /* ------------------- Attachments -------------------- */
         //          pl.add(setAttachment(att));
         return null;
 
       case ATTENDEE:
         /* ------------------- Attendees -------------------- */
 
         AttendeePropType att = new AttendeePropType();
         att.setCalAddress(prop.getValue());
         return of.createAttendee(att);
 
       case BUSYTYPE:
         return null;
 
       case CATEGORIES:
         /* ------------------- Categories -------------------- */
 
         // LANG - filter on language - group language in one cat list?
 
         CategoriesPropType c = new CategoriesPropType();
         CategoryList cats = ((Categories)prop).getCategories();
 
         Iterator pit = cats.iterator();
         while (pit.hasNext()) {
           c.getText().add((String)pit.next());
         }
 
         return of.createCategories(c);
 
       case CLASS:
         /* ------------------- Class -------------------- */
 
         ClassPropType cl = new ClassPropType();
         cl.setText(prop.getValue());
         return of.createClass(cl);
 
       case COMMENT:
         /* ------------------- Comments -------------------- */
 
         CommentPropType cm = new CommentPropType();
         cm.setText(prop.getValue());
         return of.createComment(cm);
 
       case COMPLETED:
         /* ------------------- Completed -------------------- */
 
         CompletedPropType cmp = new CompletedPropType();
         cmp.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
         return of.createCompleted(cmp);
 
       case CONTACT:
         /* ------------------- Contact -------------------- */
 
         // LANG
         ContactPropType ct = new ContactPropType();
         ct.setText(prop.getValue());
 
         return of.createContact(ct);
 
       case CREATED:
         /* ------------------- Created -------------------- */
 
         CreatedPropType created = new CreatedPropType();
         created.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
         return of.createCreated(created);
 
       case DESCRIPTION:
         /* ------------------- Description -------------------- */
 
         DescriptionPropType desc = new DescriptionPropType();
         desc.setText(prop.getValue());
         return of.createDescription(desc);
 
       case DTEND:
         /* ------------------- DtEnd -------------------- */
 
         DtendPropType dtend = (DtendPropType)makeDateDatetime(new DtendPropType(),
                                                               prop);
         return of.createDtend(dtend);
 
       case DTSTAMP:
         /* ------------------- DtStamp -------------------- */
 
         DtstampPropType dtstamp = new DtstampPropType();
         dtstamp.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
         return of.createDtstamp(dtstamp);
 
       case DTSTART:
         /* ------------------- DtStart -------------------- */
 
         DtstartPropType dtstart = (DtstartPropType)makeDateDatetime(new DtstartPropType(),
                                                                     prop);
         return of.createDtstart(dtstart);
 
       case DUE:
         /* ------------------- Due -------------------- */
 
         DuePropType due = (DuePropType)makeDateDatetime(new DuePropType(),
                                                         prop);
         return of.createDue(due);
 
       case DURATION:
         /* ------------------- Duration -------------------- */
 
         DurationPropType dur = new DurationPropType();
 
         dur.setDuration(prop.getValue());
         return of.createDuration(dur);
 
       case EXDATE:
         /* ------------------- ExDate --below------------ */
         return null;
 
       case EXRULE:
         /* ------------------- ExRule --below------------- */
 
         ExrulePropType er = new ExrulePropType();
         er.setRecur(doRecur(((RRule)prop).getRecur()));
 
         return of.createExrule(er);
 
       case FREEBUSY:
         /* ------------------- freebusy -------------------- */
 
         FreeBusy icfb = (FreeBusy)prop;
         PeriodList fbps = icfb.getPeriods();
 
         if (Util.isEmpty(fbps)) {
           return null;
         }
 
         FreebusyPropType fb = new FreebusyPropType();
 
         String fbtype = paramVal(prop, Parameter.FBTYPE);
 
         if (fbtype != null) {
           ArrayOfParameters pars = getAop(fb);
 
           FbtypeParamType f = new FbtypeParamType();
 
           f.setText(fbtype);
           JAXBElement<FbtypeParamType> param = of.createFbtype(f);
           pars.getBaseParameter().add(param);
         }
 
         List<PeriodType> pdl =  fb.getPeriod();
 
         for (Object o: fbps) {
           Period p = (Period)o;
           PeriodType np = new PeriodType();
 
           np.setStart(XcalUtil.getXMlUTCCal(p.getStart().toString()));
           np.setEnd(XcalUtil.getXMlUTCCal(p.getEnd().toString()));
           pdl.add(np);
         }
 
         return of.createFreebusy(fb);
 
       case GEO:
         /* ------------------- Geo -------------------- */
 
         Geo geo = (Geo)prop;
         GeoPropType g = new GeoPropType();
 
         g.setLatitude(geo.getLatitude().floatValue());
         g.setLatitude(geo.getLongitude().floatValue());
         return of.createGeo(g);
 
       case LAST_MODIFIED:
         /* ------------------- LastModified -------------------- */
 
         LastModifiedPropType lm = new LastModifiedPropType();
         lm.setUtcDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
         return of.createLastModified(lm);
 
       case LOCATION:
         /* ------------------- Location -------------------- */
 
         LocationPropType l = new LocationPropType();
         l .setText(prop.getValue());
 
         return of.createLocation(l);
 
       case METHOD:
         /* ------------------- Method -------------------- */
 
         MethodPropType m = new MethodPropType();
 
         m.setText(prop.getValue());
         return of.createMethod(m);
 
       case ORGANIZER:
         /* ------------------- Organizer -------------------- */
 
         OrganizerPropType org = new OrganizerPropType();
         org.setCalAddress(prop.getValue());
         return of.createOrganizer(org);
 
       case PERCENT_COMPLETE:
         /* ------------------- PercentComplete -------------------- */
 
         PercentCompletePropType p = new PercentCompletePropType();
         p.setInteger(BigInteger.valueOf(((PercentComplete)prop).getPercentage()));
 
         return of.createPercentComplete(p);
 
       case PRIORITY:
         /* ------------------- Priority -------------------- */
 
         PriorityPropType pr = new PriorityPropType();
         pr.setInteger(BigInteger.valueOf(((Priority)prop).getLevel()));
 
         return of.createPriority(pr);
 
       case PRODID:
         /* ------------------- Prodid -------------------- */
         ProdidPropType prod = new ProdidPropType();
         prod.setText(prop.getValue());
         return of.createProdid(prod);
 
       case RDATE:
         /* ------------------- RDate ------------------- */
         // XXX Todo
         return null;
 
       case RECURRENCE_ID:
         /* ------------------- RecurrenceID -------------------- */
 
         RecurrenceIdPropType ri = new RecurrenceIdPropType();
         String strval = prop.getValue();
 
         if (dateOnly(prop)) {
           // RECUR - fix all day recurrences sometime
           if (strval.length() > 8) {
             // Try to fix up bad all day recurrence ids. - assume a local timezone
             strval = strval.substring(0, 8);
           }
 
           ri.setDate(XcalUtil.fromDtval(strval));
         } else {
           XcalUtil.initDt(ri, strval, getTzid(prop));
         }
 
         return of.createRecurrenceId(ri);
 
       case RELATED_TO:
         /* ------------------- RelatedTo -------------------- */
 
         RelatedToPropType rt = new RelatedToPropType();
 
         String relType = paramVal(prop, Parameter.RELTYPE);
         String value = paramVal(prop, Parameter.VALUE);
 
         if ((value == null) || "uid".equalsIgnoreCase(value)) {
           rt.setUid(prop.getValue());
         } else if ("uri".equalsIgnoreCase(value)) {
           rt.setUri(prop.getValue());
         } else {
           rt.setText(prop.getValue());
         }
 
         if (relType != null) {
           ArrayOfParameters pars = getAop(rt);
 
           ReltypeParamType r = new ReltypeParamType();
           r.setText(relType);
           JAXBElement<ReltypeParamType> param = of.createReltype(r);
           pars.getBaseParameter().add(param);
         }
 
         return of.createRelatedTo(rt);
 
       case REPEAT:
         /* ------------------- Repeat Alarm -------------------- */
         Repeat rept = (Repeat)prop;
         RepeatPropType rep = new RepeatPropType();
         rep.setInteger(BigInteger.valueOf(rept.getCount()));
 
         return of.createRepeat(rep);
 
       case REQUEST_STATUS:
         /* ------------------- RequestStatus -------------------- */
 
         // XXX Later
         return null;
 
       case RESOURCES:
         /* ------------------- Resources -------------------- */
 
         ResourcesPropType r = new ResourcesPropType();
 
         List<String> rl = r.getText();
         ResourceList rlist = ((Resources)prop).getResources();
 
         Iterator rlit = rlist.iterator();
         while (rlit.hasNext()) {
           rl.add((String)rlit.next());
         }
 
         return of.createResources(r);
 
       case RRULE:
         /* ------------------- RRule ------------------- */
 
         RrulePropType rrp = new RrulePropType();
         rrp.setRecur(doRecur(((RRule)prop).getRecur()));
 
         return of.createRrule(rrp);
 
       case SEQUENCE:
         /* ------------------- Sequence -------------------- */
 
         SequencePropType s = new SequencePropType();
         s.setInteger(BigInteger.valueOf(((Sequence)prop).getSequenceNo()));
 
         return of.createSequence(s);
 
       case STATUS:
         /* ------------------- Status -------------------- */
 
         StatusPropType st = new StatusPropType();
 
         st.setText(prop.getValue());
         return of.createStatus(st);
 
       case SUMMARY:
         /* ------------------- Summary -------------------- */
 
         SummaryPropType sum = new SummaryPropType();
         sum.setText(prop.getValue());
         return of.createSummary(sum);
 
       case TRIGGER:
         /* ------------------- Trigger - alarm -------------------- */
         TriggerPropType trig = new TriggerPropType();
 
         String valType = paramVal(prop, Parameter.VALUE);
 
         if ((valType == null) ||
             (valType.equalsIgnoreCase(Value.DURATION.getValue()))) {
           trig.setDuration(prop.getValue());
           String rel = paramVal(prop, Parameter.RELATED);
           if (rel != null) {
             ArrayOfParameters pars = getAop(trig);
 
             RelatedParamType rpar = new RelatedParamType();
             rpar.setText(IcalDefs.alarmTriggerRelatedEnd);
             JAXBElement<RelatedParamType> param = of.createRelated(rpar);
             pars.getBaseParameter().add(param);
           }
         } else if (valType.equalsIgnoreCase(Value.DATE_TIME.getValue())) {
           //t.setDateTime(val.getTrigger());
           trig.setDateTime(XcalUtil.getXMlUTCCal(prop.getValue()));
         }
 
         return of.createTrigger(trig);
 
       case TRANSP:
         /* ------------------- Transp -------------------- */
 
         TranspPropType t = new TranspPropType();
         t.setText(prop.getValue());
         return of.createTransp(t);
 
       case TZID:
       case TZNAME:
       case TZOFFSETFROM:
       case TZOFFSETTO:
       case TZURL:
         return null;
 
       case UID:
         /* ------------------- Uid -------------------- */
 
         UidPropType uid = new UidPropType();
         uid.setText(prop.getValue());
         return of.createUid(uid);
 
       case URL:
         /* ------------------- Url -------------------- */
 
         UrlPropType u = new UrlPropType();
 
         u.setUri(prop.getValue());
         return of.createUrl(u);
 
       case VERSION:
         /* ------------------- Version - vcal only -------------------- */
 
         VersionPropType vers = new VersionPropType();
         vers.setText(prop.getValue());
         return of.createVersion(vers);
 
       case XBEDEWORK_COST:
         /* ------------------- Cost -------------------- */
 
         XBedeworkCostPropType cst = new XBedeworkCostPropType();
 
         cst.setText(prop.getValue());
         return of.createXBedeworkCost(cst);
 
       default:
         if (prop instanceof XProperty) {
           /* ------------------------- x-property --------------------------- */
 
           String name = prop.getName();
 
           PropertyInfoIndex xpii = PropertyInfoIndex.lookupPname(prop.getName());
 
           if (pii == null) {
             return null;
           }
 
           return null;
         }
 
     } // switch (pii)
 
     return null;
   }
 
   static void processParameters(final ParameterList icparams,
                                 final BasePropertyType prop) throws Throwable {
     if ((icparams == null) || icparams.isEmpty()) {
       return;
     }
 
    prop.setParameters(new ArrayOfParameters());
    List<JAXBElement<? extends BaseParameterType>> pl = prop.getParameters().getBaseParameter();

     Iterator it = icparams.iterator();
 
     while (it.hasNext()) {
       Parameter param = (Parameter)it.next();
 
       ParameterInfoIndex pii = ParameterInfoIndex.lookupPname(param.getName());
 
       if (pii == null) {
         continue;
       }
 
       JAXBElement<? extends BaseParameterType> xmlprop =
           doParameter(param, pii);
 
       if (xmlprop != null) {
        pl.add(xmlprop);
       }
     }
   }
 
   static JAXBElement<? extends BaseParameterType> doParameter(final Parameter param,
                                                      final ParameterInfoIndex pii) throws Throwable {
     switch (pii) {
       case ALTREP:
         AltrepParamType ar = new AltrepParamType();
         ar.setUri(param.getValue());
         return of.createAltrep(ar);
 
       case CN:
         CnParamType cn = new CnParamType();
         cn.setText(param.getValue());
         return of.createCn(cn);
 
       case CUTYPE:
         CutypeParamType c = new CutypeParamType();
         c.setText(param.getValue());
         return of.createCutype(c);
 
       case DELEGATED_FROM:
         DelegatedFromParamType df = new DelegatedFromParamType();
         df.getCalAddress().add(param.getValue());
         return of.createDelegatedFrom(df);
 
       case DELEGATED_TO:
         DelegatedToParamType dt = new DelegatedToParamType();
         dt.getCalAddress().add(param.getValue());
         return of.createDelegatedTo(dt);
 
       case DIR:
         DirParamType d = new DirParamType();
         d.setUri(param.getValue());
         return of.createDir(d);
 
       case ENCODING:
         return null;
 
       case FMTTYPE:
         return null;
 
       case FBTYPE:
         return null;
 
       case LANGUAGE:
         LanguageParamType l = new LanguageParamType();
         l.setText(param.getValue());
         return of.createLanguage(l);
 
       case MEMBER:
         MemberParamType m = new MemberParamType();
         m.getCalAddress().add(param.getValue());
         return of.createMember(m);
 
       case PARTSTAT:
         PartstatParamType partstat = new PartstatParamType();
         partstat.setText(param.getValue());
         return of.createPartstat(partstat);
 
       case RANGE:
         return null;
 
       case RELATED:
         return null;
 
       case RELTYPE:
         return null;
 
       case ROLE:
         RoleParamType r = new RoleParamType();
         r.setText(param.getValue());
         return of.createRole(r);
 
       case RSVP:
         return null;
 
       case SCHEDULE_AGENT:
         return null;
 
       case SCHEDULE_STATUS:
         ScheduleStatusParamType ss = new ScheduleStatusParamType();
         ss.setText(param.getValue());
         return of.createScheduleStatus(ss);
 
       case SENT_BY:
         SentByParamType sb = new SentByParamType();
         sb.setCalAddress(param.getValue());
         return of.createSentBy(sb);
 
       case TYPE:
         return null;
 
       case TZID:
         TzidParamType tzid = new TzidParamType();
         tzid.setText(param.getValue());
         return of.createTzid(tzid);
 
       case VALUE:
         return null;
 
       default:
 
     } // switch (pii)
 
     return null;
   }
 
   /** Build recurring properties from ical recurrence.
    *
    * @param r
    * @return RecurTyp filled in
    * @throws Throwable
    */
   public static RecurType doRecur(final Recur r) throws Throwable {
     RecurType rt = new RecurType();
 
     rt.setFreq(FreqRecurType.fromValue(r.getFrequency()));
     if (r.getCount() > 0) {
       rt.setCount(BigInteger.valueOf(r.getCount()));
     }
 
     Date until = r.getUntil();
     if (until != null) {
       UntilRecurType u = new UntilRecurType();
       XcalUtil.initUntilRecur(u, until.toString());
     }
 
     if (r.getInterval() > 0) {
       rt.setInterval(String.valueOf(r.getInterval()));
     }
 
     listFromNumberList(rt.getBysecond(),
                        r.getSecondList());
 
     listFromNumberList(rt.getByminute(),
                        r.getMinuteList());
 
     listFromNumberList(rt.getByhour(),
                        r.getHourList());
 
     if (r.getDayList() != null) {
       List<String> l = rt.getByday();
 
       for (Object o: r.getDayList()) {
         l.add(((WeekDay)o).getDay());
       }
     }
 
     listFromNumberList(rt.getByyearday(),
                        r.getYearDayList());
 
     intlistFromNumberList(rt.getBymonthday(),
                           r.getMonthDayList());
 
     listFromNumberList(rt.getByweekno(),
                        r.getWeekNoList());
 
     intlistFromNumberList(rt.getBymonth(),
                           r.getMonthList());
 
     bigintlistFromNumberList(rt.getBysetpos(),
                              r.getSetPosList());
 
     return rt;
   }
 
   private static void listFromNumberList(final List<String> l,
                                         final NumberList nl) {
     if (nl == null) {
       return;
     }
 
     for (Object o: nl) {
       l.add(String.valueOf(o));
     }
   }
 
   private static void intlistFromNumberList(final List<Integer> l,
                                             final NumberList nl) {
     if (nl == null) {
       return;
     }
 
     for (Object o: nl) {
       l.add((Integer)o);
     }
   }
 
   private static void bigintlistFromNumberList(final List<BigInteger> l,
                                             final NumberList nl) {
     if (nl == null) {
       return;
     }
 
     for (Object o: nl) {
       l.add(BigInteger.valueOf((Integer)o));
     }
   }
 
   private static String getTzid(final Property p) {
     TzId tzidParam = (TzId)p.getParameter(Parameter.TZID);
 
     if (tzidParam == null) {
       return null;
     }
 
     return tzidParam.getValue();
   }
 
   private static boolean dateOnly(final Property p) {
     Value valParam = (Value)p.getParameter(Parameter.VALUE);
 
     if ((valParam == null) || (valParam.getValue() == null)) {
       return false;
     }
 
     return valParam.getValue().toUpperCase().equals(Value.DATE);
   }
 
   private static String paramVal(final Property p,
                                  final String paramName) {
     Parameter param = p.getParameter(paramName);
 
     if ((param == null) || (param.getValue() == null)) {
       return null;
     }
 
     return param.getValue();
   }
 
   private static ArrayOfParameters getAop(final BasePropertyType prop) {
     ArrayOfParameters pars = prop.getParameters();
 
     if (pars == null) {
       pars = new ArrayOfParameters();
       prop.setParameters(pars);
     }
 
     return pars;
   }
 
   private static DateDatetimePropertyType makeDateDatetime(final DateDatetimePropertyType p,
                                                            final Property prop) throws Throwable {
     XcalUtil.initDt(p, prop.getValue(), getTzid(prop));
 
     return p;
   }
 
   private static boolean emit(final BaseComponentType pattern,
                               final Class compCl,
                               final Class... cl) {
     if (pattern == null) {
       return true;
     }
 
     if (!compCl.getName().equals(pattern.getClass().getName())) {
       return false;
     }
 
     if ((cl == null) | (cl.length == 0)) {
       // Any property
       return true;
     }
 
     String className = cl[0].getName();
 
     if (BasePropertyType.class.isAssignableFrom(cl[0])) {
       if (pattern.getProperties() == null) {
         return false;
       }
 
       List<JAXBElement<? extends BasePropertyType>> patternProps =
          pattern.getProperties().getBasePropertyOrTzid();
 
       for (JAXBElement<? extends BasePropertyType> jp: patternProps) {
         if (jp.getValue().getClass().getName().equals(className)) {
           return true;
         }
       }
 
       return false;
     }
 
     List<JAXBElement<? extends BaseComponentType>> patternComps =
       XcalUtil.getComponents(pattern);
 
     if (patternComps == null) {
       return false;
     }
 
     // Check for component
 
     for (JAXBElement<? extends BaseComponentType> jp: patternComps) {
       if (jp.getValue().getClass().getName().equals(className)) {
         return emit(pattern, cl[0], Arrays.copyOfRange(cl, 1, cl.length - 1));
       }
     }
 
     return false;
   }
 }
