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
 
 import org.oasis_open.docs.ns.wscal.calws_soap.PropertiesSelectionType;
 import org.oasis_open.docs.ns.wscal.calws_soap.PropertyReferenceType;
 import org.oasis_open.docs.ns.wscal.calws_soap.PropertySelectionType;
 
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 /** This class wraps an array of properties. Properties are ordered by the
  * comparator so we can step through them and try to produce a set of changes for
  * the target - when reading this is the source that is the target.
  *
  * @author Mike Douglass
  */
 class PropsWrapper extends BaseSetWrapper<PropWrapper, CompWrapper,
                                           JAXBElement<? extends BasePropertyType>>
                    implements Comparable<PropsWrapper> {
   /* Set of properties we skip during comparison.
    */
   private static Map<QName, QName> skipped = new HashMap<QName, QName>();
 
   static {
     addSkipped(new QName(icalendarNs, "prodid"));
     addSkipped(new QName(icalendarNs, "version"));
 
     addSkipped(new QName(icalendarNs, "created"));
     addSkipped(new QName(icalendarNs, "dtstamp"));
     addSkipped(new QName(icalendarNs, "last-modified"));
   }
 
   /* Use this to find a candidate for matching when we get a mismatch
    * We're trying to deal with the situation where we have missing
    * properties: e.g.
    * this:      that:
    * aaa        aaa
    * bbb
    * ccc        ccc
    *
    * When we see "bbb" we want to know if it turns up later in that or whether
    * it really is a new one. It's probably quicker just to scan the list
    */
   //private int[] hashCodes;
 
   PropsWrapper(final CompWrapper parent,
                final List<JAXBElement<? extends BasePropertyType>> propsList) {
     super(parent, new QName(icalendarNs, "properties"), propsList);
   }
 
   @Override
   PropWrapper[] getTarray(final int len) {
     return new PropWrapper[len];
   }
 
   @SuppressWarnings("unchecked")
   @Override
   Set<PropWrapper> getWrapped(final JAXBElement<? extends BasePropertyType> el) {
     QName nm = el.getName();
 
     /* We skip certain properties as they only appear on one side
      * If this is one we skip return null.
      */
     if (skipped.containsKey(nm)) {
       return null;
     }
 
     Set<PropWrapper> res = new TreeSet<PropWrapper>();
     List<BasePropertyType> normed = getMatcher().getNormalized(el.getValue());
 
     for (BasePropertyType bp: normed) {
       res.add(new PropWrapper(this, nm, bp));
     }
 
     return res;
   }
 
   /** Return a list of differences between this (the new object) and that (the
    * old object)
    *
    * @param that - the old form.
    * @return changes
    */
   public PropertiesSelectionType diff(final PropsWrapper that) {
     PropertiesSelectionType sel = null;
 
     int thatI = 0;
     int thisI = 0;
 
     while ((that != null) && (thisI < size()) && (thatI < that.size())) {
       PropWrapper thisOne = getTarray()[thisI];
       PropWrapper thatOne = that.getTarray()[thatI];
 
       if (thisOne.equals(thatOne)) {
         thisI++;
         thatI++;
         continue;
       }
 
       int ncmp = thisOne.compareNames(thatOne);
 
       if (ncmp == 0) {
         /* names match - scan down that side to see if we can find a matching
          * property. All the intermediate ones would then be marked for
          * deletion.
          *
          * If that doesn't find a match, scan down this side to see if we can
          * match thatOne. If we find a match the intermediate ones would then be
          * marked for addition.
          *
          * We only need to scan so far. The items are all ordered so we should be
          * able to do a simple comparison as an initial test.
          *
          * To complicate issues - we should diff the non-matching properties to
          * see if it's a parameter change or a value change.
          *
          * A value change is an update to the property, a parameter change might
          * be an add, update or mod.
          */
 
         if (((thisI + 1) == size()) &&
             ((thatI + 1) == that.size())) {
           // No more on this side and that side - call it an update
           sel = select(sel, thisOne.diff(thatOne));
           thisI++;
           thatI++;
           continue;
         }
 
         /* More on one or both sides. This allows the possibility that an
          * extra multivalued value has been inserted or one deleted.
          *
          * We should check further down both sides.
          */
 
         // For the moment just create a diff
 
         sel = select(sel, thisOne.diff(thatOne));
         thisI++;
         thatI++;
       } else if (ncmp < 0) {
         // in this but not that - addition
         sel = add(sel, thisOne.makeRef());
         thisI++;
       } else {
         // in that but not this - deletion
         sel = remove(sel, thatOne.makeRef());
         thatI++;
       }
     }
 
     while (thisI < size()) {
       // Extra ones in the source
 
       PropWrapper thisOne = getTarray()[thisI];
       sel = add(sel, thisOne.makeRef());
       thisI++;
     }
 
     while ((that != null) && (thatI < that.size())) {
       // Extra ones in the target
 
       PropWrapper thatOne = that.getTarray()[thatI];
       sel = remove(sel, thatOne.makeRef());
       thatI++;
     }
 
     return sel;
   }
 
   PropertiesSelectionType getSelect(final PropertiesSelectionType val) {
     if (val != null) {
       return val;
     }
 
     PropertiesSelectionType sel = new PropertiesSelectionType();
 
     return sel;
   }
 
   PropertiesSelectionType add(final PropertiesSelectionType sel,
                               final PropertyReferenceType val) {
     PropertiesSelectionType csel = getSelect(sel);
 
     csel.getAdd().add(val);
 
     return csel;
   }
 
   PropertiesSelectionType remove(final PropertiesSelectionType sel,
                                  final PropertyReferenceType val) {
     PropertiesSelectionType csel = getSelect(sel);
 
     csel.getRemove().add(val);
 
     return csel;
   }
 
   PropertiesSelectionType select(final PropertiesSelectionType sel,
                                  final PropertySelectionType val) {
     PropertiesSelectionType csel = getSelect(sel);
 
     csel.getProperty().add(val);
 
     return csel;
   }
 
   public int compareTo(final PropsWrapper that) {
     if (size() < that.size()) {
       return -1;
     }
 
     if (size() > that.size()) {
       return 1;
     }
 
     Iterator<PropWrapper> it = that.getEls().iterator();
 
     for (PropWrapper p: getEls()) {
       PropWrapper thatP = it.next();
 
       int res = p.compareTo(thatP);
 
       if (res != 0) {
         return res;
       }
     }
 
     return 0;
   }
 
   @Override
   public int hashCode() {
     int hc = size() + 1;
 
     for (PropWrapper p: getEls()) {
       hc += p.hashCode();
     }
 
     return hc;
   }
 
   @Override
   public boolean equals(final Object o) {
     return compareTo((PropsWrapper)o) == 0;
   }
 
   /* * We've checked the one at offset start. See if there is a matching one
    * later on and return it's index.
    *
    * @param thisOne
    * @param thatOne
    * @param start
    * @return
    * /
   private int present(final PropWrapper thisOne,
                       final PropsWrapper that,
                       final int start) {
     int i = start + 1;
 
     if (i >= size()) {
       return -1;
     }
 
     int hc = thisOne.hashCode();
 
     for (PropWrapper p: props) {
       if ((that.hashCodes[i] == hc) &&
           that.equals(p)) {
         return i;
       }
 
       i++;
     }
 
     return -1;
   }*/
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder("PropsWrapper{");
 
     super.toStringSegment(sb);
     sb.append("}");
 
     return sb.toString();
   }
 
   private static void addSkipped(final QName nm) {
     skipped.put(nm, nm);
   }
 }
