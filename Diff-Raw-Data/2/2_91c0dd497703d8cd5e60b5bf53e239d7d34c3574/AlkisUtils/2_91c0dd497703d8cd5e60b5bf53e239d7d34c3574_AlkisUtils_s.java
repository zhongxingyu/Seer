 /*
  *  Copyright (C) 2011 thorsten
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.custom.objectrenderer.utils.alkis;
 
 import de.cismet.cids.custom.utils.alkis.AlkisProducts;
 import de.cismet.cids.custom.utils.alkis.AlkisConstants;
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import de.aedsicad.aaaweb.service.util.Address;
 import de.aedsicad.aaaweb.service.util.Buchungsblatt;
 import de.aedsicad.aaaweb.service.util.Buchungsstelle;
 import de.aedsicad.aaaweb.service.util.Owner;
 
 
 
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 
 import de.cismet.cids.dynamics.CidsBean;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 /**
  *
  * @author thorsten
  */
 public class AlkisUtils {
     public final static AlkisProducts PRODUCTS  = new AlkisProducts(AlkisConstants.COMMONS.USER, AlkisConstants.COMMONS.PASSWORD, AlkisConstants.COMMONS.SERVICE);;
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlkisUtils.class);
     //~ Methods ----------------------------------------------------------------
     //--
 
     /**
      * DOCUMENT ME!
      *
      * @param   bean         DOCUMENT ME!
      * @param   description  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String generateLinkFromCidsBean(final CidsBean bean, final String description) {
         if (bean != null) {
             final int objectID = bean.getMetaObject().getId();
             final StringBuilder result = new StringBuilder("<a href=\"");
 //            result.append(bean.getMetaObject().getMetaClass().getID()).append(LINK_SEPARATOR_TOKEN).append(objectID);
             result.append(bean.getMetaObject().getMetaClass().getID()).append(AlkisConstants.LINK_SEPARATOR_TOKEN).append(objectID);
             result.append("\">");
             result.append(description);
             result.append("</a>");
             return result.toString();
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblatt      DOCUMENT ME!
      * @param   buchungsblattBean  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String buchungsblattToString(final CidsBean originatingFlurstueck, final Buchungsblatt buchungsblatt, final CidsBean buchungsblattBean) {
         String alkisId=(String)originatingFlurstueck.getProperty("alkis_id");
         String pos="";
         List<CidsBean> alleFSaufBB=buchungsblattBean.getBeanCollectionProperty("landparcels");
         for (CidsBean lp:alleFSaufBB){
             if (lp.getProperty("landparcelcode").equals(alkisId)){
                 pos=String.valueOf(lp.getProperty("lfn"));
             }
         }
         
         final List<Owner> owners = Arrays.asList(buchungsblatt.getOwners());
         if ((owners != null) && (owners.size() > 0)) {
             final StringBuilder infoBuilder = new StringBuilder();
             infoBuilder.append(
                     "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
 //            infoBuilder.append("<tr><td width=\"200\"><b><a href=\"").append(generateBuchungsblattLinkInfo(buchungsblatt)).append("\">").append(buchungsblatt.getBuchungsblattCode()).append("</a></b></td><td>");
            infoBuilder.append("<tr><td width=\"200\">Nr. "+pos+" auf <b>").append(generateLinkFromCidsBean(buchungsblattBean, buchungsblatt.getBuchungsblattCode())).append("</b></td><td>");
             final Iterator<Owner> ownerIterator = owners.iterator();
 //            if (ownerIterator.hasNext()) {
 //                infoBuilder.append(ownerToString(ownerIterator.next(), ""));
 //            }
             infoBuilder.append(
                     "<table cellspacing=\"0\" cellpadding=\"0\" border=\"0\" align=\"left\" valign=\"top\">");
             while (ownerIterator.hasNext()) {
                 infoBuilder.append(ownerToString(ownerIterator.next(), ""));
 //                infoBuilder.append(ownerToString(ownerIterator.next(), "</td><td>"));
             }
             infoBuilder.append("</table>");
             infoBuilder.append("</td></tr>");
             infoBuilder.append("</table>");
 //            infoBuilder.append("</html>");
             return infoBuilder.toString();
 //            lblBuchungsblattEigentuemer.setText(infoBuilder.toString());
         } else {
             return "";
 //            lblBuchungsblattEigentuemer.setText("-");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   address  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String addressToString(final Address address) {
         if (address != null) {
             final StringBuilder addressStringBuilder = new StringBuilder();
             if (address.getStreet() != null) {
                 addressStringBuilder.append(address.getStreet()).append(" ");
             }
             if (address.getHouseNumber() != null) {
                 addressStringBuilder.append(address.getHouseNumber());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(AlkisConstants.NEWLINE);
             }
             if (address.getPostalCode() != null) {
                 addressStringBuilder.append(address.getPostalCode()).append(" ");
             }
             if (address.getCity() != null) {
                 addressStringBuilder.append(address.getCity());
             }
             if (addressStringBuilder.length() > 0) {
                 addressStringBuilder.append(AlkisConstants.NEWLINE);
             }
             return addressStringBuilder.toString();
         } else {
             return "";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   landParcel  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getLandparcelCodeFromParcelBeanObject(final Object landParcel) {
         if (landParcel instanceof CidsBean) {
             final CidsBean cidsBean = (CidsBean) landParcel;
             final Object parcelCodeObj = cidsBean.getProperty("alkis_id");
             if (parcelCodeObj != null) {
                 return parcelCodeObj.toString();
             }
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fullLandparcelCode  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String prettyPrintLandparcelCode(final String fullLandparcelCode) {
         final String[] tiles = fullLandparcelCode.split("-");
         if (tiles.length == 1) {
             final String flurstueck = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck);
         } else if (tiles.length == 2) {
             final String flurstueck = tiles[1];
             final String flur = tiles[0];
             final String result = _prettyPrintLandparcelCode(flurstueck, flur);
             return result;
         } else if (tiles.length == 3) {
             final String flurstueck = tiles[2];
             final String flur = tiles[1];
             final String gemarkung = tiles[0];
             return _prettyPrintLandparcelCode(flurstueck, flur, gemarkung);
         } else {
             return fullLandparcelCode;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   strings    DOCUMENT ME!
      * @param   separator  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String arrayToSeparatedString(final String[] strings, final String separator) {
         if (strings != null) {
             final StringBuilder result = new StringBuilder();
             for (int i = 0; i < strings.length; /**incremented in loop**/
                     ) {
                 result.append(strings[i]);
                 if (++i < strings.length) {
                     result.append(separator);
                 }
             }
             return result.toString();
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   toEscape  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String escapeHtmlSpaces(String toEscape) {
         if (toEscape != null) {
             toEscape = toEscape.replace(" ", "%20");
         }
         return toEscape;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   owner    DOCUMENT ME!
      * @param   spacing  Einrückung
      *
      * @return  DOCUMENT ME!
      */
     public static String ownerToString(final Owner owner, final String spacing) {
         if (owner != null) {
             final StringBuilder ownerStringBuilder = new StringBuilder();
             ownerStringBuilder.append("<tr><td width=\"75\">").append(spacing);
             if (owner.getNameNumber() != null) {
                 ownerStringBuilder.append(normalizeNameNumber(owner.getNameNumber()));
             }
             ownerStringBuilder.append("</td><td>");
             if (owner.getForeName() != null) {
                 ownerStringBuilder.append(owner.getForeName()).append(" ");
             }
             if (owner.getSurName() != null) {
                 ownerStringBuilder.append(owner.getSurName());
             }
             if (owner.getSalutation() != null) {
                 ownerStringBuilder.append(", ").append(owner.getSalutation());
             }
             if (owner.getDateOfBirth() != null) {
                 ownerStringBuilder.append(", *").append(owner.getDateOfBirth());
             }
             if (owner.getNameOfBirth() != null) {
                 ownerStringBuilder.append(AlkisConstants.NEWLINE + "geb. ").append(owner.getNameOfBirth());
             }
             ownerStringBuilder.append(AlkisConstants.NEWLINE).append("</td></tr>");
             final Address[] addresses = owner.getAddresses();
             if (addresses != null) {
                 for (final Address address : addresses) {
                     if (address != null) {
                         ownerStringBuilder.append("<tr><td></td>").append(spacing).append("<td>");
                         ownerStringBuilder.append(addressToString(address)).append(AlkisConstants.NEWLINE);
                         ownerStringBuilder.append("</td></tr>");
                     }
                 }
             }
             return ownerStringBuilder.toString();
         }
         return "";
     }
 
     /**
      * ----------------private.
      *
      * @param   in  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String removeLeadingZeros(final String in) {
         return in.replaceAll("^0*", "");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   nameNumber  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String normalizeNameNumber(final String nameNumber) {
         final String[] tokens = nameNumber.split("\\.");
         final StringBuilder result = new StringBuilder();
         for (String token : tokens) {
             token = removeLeadingZeros(token);
             if (token.length() > 0) {
                 result.append(token).append(".");
             }
         }
         if (result.length() > 0) {
             result.deleteCharAt(result.length() - 1);
             return result.toString();
         } else {
             return "0";
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   buchungsblatt  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String generateBuchungsblattLinkInfo(final Buchungsblatt buchungsblatt) {
         // TODO: Should return metaclassID::objectID instead of metaclassID::BuchungsblattCode...
         return new StringBuilder("ALKIS_BUCHUNGSBLATT").append(AlkisConstants.LINK_SEPARATOR_TOKEN).append(buchungsblatt.getBuchungsblattCode()).toString();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      * @param   gemarkung   DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck,
             final String flur,
             final String gemarkung) {
         return _prettyPrintLandparcelCode(flurstueck, flur) + " - Gemarkung " + gemarkung;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      * @param   flur        DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck, final String flur) {
         return _prettyPrintLandparcelCode(flurstueck) + " - Flur " + removeLeadingZeros(flur);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   flurstueck  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String _prettyPrintLandparcelCode(final String flurstueck) {
         return "Flurstück " + prettyPrintFlurstueck(flurstueck);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fsZahlerNenner  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private static String prettyPrintFlurstueck(final String fsZahlerNenner) {
         final String[] tiles = fsZahlerNenner.split("/");
         if (tiles.length == 2) {
             return removeLeadingZeros(tiles[0]) + "/" + removeLeadingZeros(tiles[1]);
         } else if (tiles.length == 1) {
             return removeLeadingZeros(tiles[0]);
         }
         return fsZahlerNenner;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   blatt  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String getBuchungsartFromBuchungsblatt(final Buchungsblatt blatt) {
         final Buchungsstelle[] buchungsstellen = blatt.getBuchungsstellen();
         if ((buchungsstellen != null) && (buchungsstellen.length > 0)) {
             
             ArrayList<Buchungsstelle> alleStellen=new ArrayList<Buchungsstelle>();
             alleStellen.addAll(Arrays.asList(buchungsstellen));
             Collections.sort(alleStellen, new Comparator<Buchungsstelle>(){
 
                 @Override
                 public int compare(Buchungsstelle t, Buchungsstelle t1) {
                     return t.getBuchungsartCode().compareTo(t1.getBuchungsartCode());
                 }
 
             });
 
 
             final Buchungsstelle ersteBuchungsstelle = alleStellen.get(alleStellen.size()-1);
             if (ersteBuchungsstelle != null) {
                 final StringBuilder result = new StringBuilder();
                 final String prettyFration = prettyPrintFration(ersteBuchungsstelle.getFraction());
                 result.append(prettyFration);
                 if (prettyFration!=null && prettyFration.length() > 0) {
                     result.append(" ");
                 }
                 result.append(ersteBuchungsstelle.getBuchungsart());
                 String number=ersteBuchungsstelle.getNumber();
                 if (!(number==null || number.trim().length()>0)){
                     result.append(", Aufteilungsplan Nr. ").append(number);
                 }
                 return result.toString();
             }
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   fraction  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static String prettyPrintFration(final String fraction) {
         if (fraction != null) {
 //            final String[] elements = fraction.split("/");
 //            if ((elements != null) && (elements.length == 2)) {
 //                String zaehler = elements[0];
 //                String nenner = elements[1];
 //                if (zaehler.lastIndexOf(".") != -1) {
 //                    zaehler = zaehler.substring(0, zaehler.lastIndexOf("."));
 //                }
 //                if (nenner.lastIndexOf(".") != -1) {
 //                    nenner = nenner.substring(0, nenner.lastIndexOf("."));
 //                }
 //                return zaehler + "/" + nenner;
 //            }
             return fraction;
         }
         return "";
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisPrintAccess() {
         try {
             return SessionManager.getConnection().getConfigAttr(SessionManager.getSession().getUser(), "navigator.alkis.print") != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Print Dialog!", ex);
         }
         return false;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public static boolean validateUserHasAlkisProductAccess() {
         try {
             return SessionManager.getConnection().getConfigAttr(SessionManager.getSession().getUser(), "navigator.alkis.products") != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Products!", ex);
         }
         return false;
     }
 
     public static boolean validateUserHasAlkisBuchungsblattAccess() {
         try {
             return SessionManager.getConnection().getConfigAttr(SessionManager.getSession().getUser(), "custom.alkis.buchungsblatt") != null;
         } catch (ConnectionException ex) {
             log.error("Could not validate action tag for Alkis Buchungsblatt!", ex);
         }
         return false;
     }
 }
