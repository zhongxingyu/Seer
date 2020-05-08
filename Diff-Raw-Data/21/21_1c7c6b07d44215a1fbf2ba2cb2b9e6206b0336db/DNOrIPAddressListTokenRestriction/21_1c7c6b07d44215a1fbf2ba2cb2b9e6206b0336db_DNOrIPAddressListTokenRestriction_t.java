 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2005 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: DNOrIPAddressListTokenRestriction.java,v 1.5 2008-08-28 20:25:31 manish_rustagi Exp $
  *
  */
 
 package com.iplanet.dpro.session;
 
 import com.iplanet.am.util.Misc;
 import com.iplanet.dpro.session.service.SessionService;
 import com.iplanet.sso.SSOToken;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 /**
  * <code>DNOrIPAddressListTokenRestriction</code> implements
  * <code>TokenRestriction</code> interface and handles the restriction of
  * the <code>DN</code> or <code>IPAddress</code>
  */
 public class DNOrIPAddressListTokenRestriction implements TokenRestriction {
 
     private String dn;
 
     private Set addressList = new HashSet();
 
     private String asString;
 
    /**
     * Constructs <code>DNOrIPAddressListTokenRestriction</code> object based on
     * the <code>DN</code> and list of host names to be restricted.
     * @param dn the <code>DN</code> of the user
     * @param hostNames list of host names.
     * @exception Exception if finding IP Address of host to be restricted or
     *            if something goes wrong.
     */
    public DNOrIPAddressListTokenRestriction(String dn, List hostNames)
         throws Exception
     {
        String val = ""; 
        boolean hostmatch = false;    
         this.dn = Misc.canonicalize(dn);
         for (Iterator i = hostNames.iterator(); i.hasNext();) {
            try { 
                val = (String)i.next();  
                addressList.add(InetAddress.getByName(val)); 
                hostmatch = true; 
            } catch (java.net.UnknownHostException e) { 
                if (SessionService.sessionDebug.warningEnabled()) { 
                    SessionService.sessionDebug.warning(
                    "DNOrIPAddressListTokenRestriction.constructor: " +
                    "failure resolving host " + val); 
                } 
                if (!i.hasNext() && !hostmatch) {
                    throw new java.net.UnknownHostException (val); 
                }
            } 
         }
 
         StringBuffer buf = new StringBuffer();
         buf.append(dn).append("\n");
         Object[] sortedAddressList = addressList.toArray();
         Arrays.sort(sortedAddressList, addressComparator);
         for (int i = 0; i < sortedAddressList.length; i++) {
             buf.append(((InetAddress) sortedAddressList[i]).getHostAddress());
             buf.append("\n");
         }
         asString = buf.toString();
     }
 
     private static Comparator addressComparator = new Comparator() {
         public int compare(Object o1, Object o2) {
             return ((InetAddress) o1).getHostAddress().compareTo(
                     ((InetAddress) o2).getHostAddress());
         }
     };
 
     /**
      * This method returns the restriction as a string.
      * 
      * @return A concatenated string of DN and/or Host Name/IP Address.
      */
     public String toString() {
         return asString;
     }
 
     /**
      * Returns a hash code for this object.
      * 
      * @return a hash code value for this object.
      */
     public int hashCode() {
         return toString().hashCode();
     }
 
     /**
      * Returns a true if the restriction matches the context for which it was
      * set.
      * 
      * @param context The context from which the restriction needs to be
      *        checked. The context can be any from the following - the Single
      *        Sign on token of the Application against which the restriction
      *        is being compared - the IP Address/Host Name of the Application
      *        against which the restriction is being compared
      * @return true if the restriction is satisfied.
      * @throws Exception is thrown if the there was an error.
      */
     public boolean isSatisfied(Object context) throws Exception {
         if (context == null) {
             return false;
         } else if (context instanceof SSOToken) {
             SSOToken usedBy = (SSOToken) context;
             return Misc.canonicalize(usedBy.getPrincipal().getName())
                     .equals(dn);
         } else if (context instanceof InetAddress) {
             return addressList.contains(context);
         } else {
             if (SessionService.sessionDebug.warningEnabled()) {
                 SessionService.sessionDebug.warning("Unknown context type:"
                         + context);
             }
             return false;
         }
     }
 
     /**
      * Returns true of <code>other</code> meets these criteria.
      * <ol type="1">
      * <li>it is not null;
      * <li>it is an instance of <code>DNOrIPAddressListTokenRestriction</code>;
      * <li>it has the same distinguished name as this object; and
      * <li>it has the same set of IP addresses as this object.
      * </ol>
      * 
      * @param other the object to be used for comparison.
      * @return true if <code>other</code> meets the above criteria.
      */
     public boolean equals(Object other) {
         return other != null
                 && (other instanceof DNOrIPAddressListTokenRestriction)
                 && other.toString().equals(this.toString());
     }
 }
