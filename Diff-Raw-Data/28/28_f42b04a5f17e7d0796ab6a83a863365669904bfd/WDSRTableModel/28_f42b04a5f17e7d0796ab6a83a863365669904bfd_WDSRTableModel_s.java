 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2010 thorsten
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
 package de.cismet.verdis.gui;
 
import de.cismet.cids.custom.util.CidsBeanSupport;

 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.verdis.commons.constants.FrontinfoPropertyConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public class WDSRTableModel extends CidsBeanTableModel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String[] COLUMN_NAMES = {
             "Nummer",
             "Länge",
             "Straßenreinigung",
             "Winterdienst"
         };

     private static final Class[] COLUMN_CLASSES = {
             Integer.class,
             Float.class,
             String.class,
             String.class
         };
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(WDSRTableModel.class);
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new WDSRTableModel object.
      */
     public WDSRTableModel() {
         super(COLUMN_NAMES, COLUMN_CLASSES);
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public boolean isCellEditable(final int i, final int i1) {
         return false;
     }
 
     @Override
     public Object getValueAt(final int row, final int column) {
         final CidsBean frontBean = getCidsBeanByIndex(row);
         if (frontBean == null) {
             return null;
         }
         if (column == 0) {
             try {
                 return (Integer)frontBean.getProperty(FrontinfoPropertyConstants.PROP__NUMMER);
             } catch (Exception e) {
                 LOG.warn("exception in tablemodel", e);
                 return -1;
             }
         } else if (column == 1) {
             try {
                 return (Integer)frontBean.getProperty(FrontinfoPropertyConstants.PROP__LAENGE_KORREKTUR);
             } catch (Exception e) {
                 LOG.warn("exception in tablemodel", e);
                 return -1f;
             }
         } else if (column == 2) {
             try {
                final Object o = frontBean.getProperty(FrontinfoPropertyConstants.PROP__SR_KLASSE_OR__KEY);
                return (o == null) ? "" : o.toString();
             } catch (Exception e) {
                 LOG.warn("exception in tablemodel", e);
                 return "";
             }
         } else {
             try {
                final Object o = frontBean.getProperty(FrontinfoPropertyConstants.PROP__WD_PRIO_OR__KEY);
                return (o == null) ? "" : o.toString();
             } catch (Exception e) {
                 LOG.warn("exception in tablemodel", e);
                 return "";
             }
         }
     }
 
     @Override
     public CidsBean deepcloneBean(final CidsBean cidsBean) throws Exception {
         final CidsBean deepclone = super.deepcloneBean(cidsBean);
         deepclone.setProperty(
             FrontinfoPropertyConstants.PROP__SR_KLASSE_OR,
             cidsBean.getProperty(FrontinfoPropertyConstants.PROP__SR_KLASSE_OR));
         deepclone.setProperty(
             FrontinfoPropertyConstants.PROP__WD_PRIO_OR,
             cidsBean.getProperty(FrontinfoPropertyConstants.PROP__WD_PRIO_OR));
         deepclone.setProperty(
             FrontinfoPropertyConstants.PROP__STRASSE,
             cidsBean.getProperty(FrontinfoPropertyConstants.PROP__STRASSE));
         // deepclone.setProperty(FrontinfoPropertyConstants.PROP__SATZUNG,
         // cidsBean.getProperty(FrontinfoPropertyConstants.PROP__SATZUNG));
         return deepclone;
     }
 }
