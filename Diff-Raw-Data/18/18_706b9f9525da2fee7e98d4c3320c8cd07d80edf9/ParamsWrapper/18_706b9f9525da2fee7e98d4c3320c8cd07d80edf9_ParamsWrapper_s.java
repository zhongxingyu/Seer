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
 
 import org.oasis_open.docs.ns.wscal.calws_soap.ParameterReferenceType;
 import org.oasis_open.docs.ns.wscal.calws_soap.ParameterSelectionType;
 import org.oasis_open.docs.ns.wscal.calws_soap.ParametersSelectionType;
 
 import ietf.params.xml.ns.icalendar_2.BaseParameterType;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 /** This class wraps an array of parameters.
  *
  * @author Mike Douglass
  */
 class ParamsWrapper extends BaseSetWrapper<ParamWrapper, PropWrapper,
                                           JAXBElement<? extends BaseParameterType>>
                     implements Comparable<ParamsWrapper> {
   ParamsWrapper(final PropWrapper parent,
                 final List<JAXBElement<? extends BaseParameterType>> plist) {
     super(parent, new QName(icalendarNs, "parameters"), plist);
   }
 
   @Override
   ParamWrapper[] getTarray(final int len) {
     return new ParamWrapper[len];
   }
 
   @Override
   Set<ParamWrapper> getWrapped(final JAXBElement<? extends BaseParameterType> el) {
     Set<ParamWrapper> res = new TreeSet<ParamWrapper>();
 
     res.add(new ParamWrapper(this, el.getName(), el.getValue()));
 
     return res;
   }
 
   /** Return a list of differences between this (the new object) and that (the
    * old object)
    *
    * @param that - the old form.
    * @return changes
    */
   public ParametersSelectionType diff(final ParamsWrapper that) {
     ParametersSelectionType sel = null;
 
     int thatI = 0;
     int thisI = 0;
 
     while ((thisI < size()) && (thatI < that.size())) {
       ParamWrapper thisOne = getTarray()[thisI];
       ParamWrapper thatOne = that.getTarray()[thatI];
 
       if (thisOne.equals(thatOne)) {
         thisI++;
         thatI++;
         continue;
       }
 
       /* I think it's true that we can only have a single occurence of a
        * parameter name. We can make use of that here
        */
 
       int ncmp = thisOne.compareNames(thatOne);
 
       if (ncmp == 0) {
         // Names match - it's a modify
         sel = select(sel, thisOne.diff(thatOne));
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
 
       ParamWrapper thisOne = getTarray()[thisI];
       sel = add(sel, thisOne.makeRef());
       thisI++;
     }
 
     while (thatI < that.size()) {
       // Extra ones in the target
 
       ParamWrapper thatOne = that.getTarray()[thatI];
       sel = remove(sel, thatOne.makeRef());
       thatI++;
     }
 
     return sel;
   }
 
   ParametersSelectionType getSelect(final ParametersSelectionType val) {
     if (val != null) {
       return val;
     }
 
     ParametersSelectionType sel = new ParametersSelectionType();
 
     return sel;
   }
 
   ParametersSelectionType add(final ParametersSelectionType sel,
                               final ParameterReferenceType val) {
     ParametersSelectionType csel = getSelect(sel);
 
     csel.getAdd().add(val);
 
     return csel;
   }
 
   ParametersSelectionType remove(final ParametersSelectionType sel,
                                  final ParameterReferenceType val) {
     ParametersSelectionType csel = getSelect(sel);
 
     csel.getRemove().add(val);
 
     return csel;
   }
 
   ParametersSelectionType select(final ParametersSelectionType sel,
                                  final ParameterSelectionType val) {
     ParametersSelectionType csel = getSelect(sel);
 
     csel.getParameter().add(val);
 
     return csel;
   }
 
   public int compareTo(final ParamsWrapper that) {
     if (size() < that.size()) {
       return -1;
     }
 
     if (size() > that.size()) {
       return 1;
     }
 
     Iterator<ParamWrapper> it = that.getEls().iterator();
 
     for (ParamWrapper p: getEls()) {
       ParamWrapper thatP = it.next();
 
       int res = p.compareTo(thatP);
 
       if (res != 0) {
         return res;
       }
     }
 
     return 0;
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder("ParamsWrapper{");
 
     super.toStringSegment(sb);
     sb.append("}");
 
     return sb.toString();
   }
 }
