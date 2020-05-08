 /*
  *  Copyright (C) 2011 jruiz
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
 
 package de.cismet.verdis;
 
 import de.cismet.verdis.constants.VerdisMetaClassConstants;
 import Sirius.navigator.connection.SessionManager;
 import de.cismet.cids.custom.util.CidsBeanSupport;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.verdis.gui.Main;
 import de.cismet.verdis.constants.RegenFlaechenPropertyConstants;
 import de.cismet.verdis.gui.RegenFlaechenTabellenPanel;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.sql.Date;
 import java.util.Collection;
 import java.util.List;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author jruiz
  */
 public class FlaechenClipboard implements RegenFlaechenPropertyConstants {
     
     private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FlaechenClipboard.class);
     private static RegenFlaechenTabellenPanel flaechenTable = Main.THIS.getRegenFlaechenTabellenPanel();
 
     private Collection<CidsBean> clipboardFlaecheBeans = new ArrayList<CidsBean>();
     private List<FlaechenClipboardListener> listeners = new ArrayList<FlaechenClipboardListener>();
     private boolean isCutted = false;
 
     public boolean addListener(FlaechenClipboardListener listener) {
         return listeners.add(listener);
     }
 
     public boolean removeListener(FlaechenClipboardListener listener) {
         return listeners.remove(listener);
     }
 
     private void fireClipboardChanged() {
         for (FlaechenClipboardListener listener : listeners) {
             listener.clipboardChanged();
         }
     }
 
     public void paste() {
         if (isPastable()) {
             try {
                 int notPastableCounter = 0;
                 final int numOfClipBoardItems = clipboardFlaecheBeans.size();
                 for (final CidsBean clipboardFlaecheBean : clipboardFlaecheBeans) {
                     if (isPastable(clipboardFlaecheBean)) {
                         CidsBean pasteBean = createPastedBean(clipboardFlaecheBean);
                         flaechenTable.addBean(pasteBean);
                     } else {
                         notPastableCounter++;
                     }
                 }
                 if (notPastableCounter < numOfClipBoardItems) {
                     fireClipboardChanged();
                 }
                 if (notPastableCounter > 0) {
                     LOG.info(notPastableCounter + " flaecheBean(s) not pasted because the flaecheinfoBean of this bean(s) was still assigned to a flaecheBean of the current kassenzeichen");
                 }
             } catch (Exception ex) {
                 LOG.error("error while pasting bean", ex);
             }
         }
     }
 
     private CidsBean createPastedBean(CidsBean clipboardBean) throws Exception {
         CidsBean pasteBean = CidsBeanSupport.cloneCidsBean(clipboardBean);
 
         final int id = flaechenTable.getTableHelper().getNextNewBeanId();
         pasteBean.setProperty(PROP__ID, id);
         pasteBean.getMetaObject().setID(id);
 
         if (clipboardBean.getProperty(PROP__FLAECHENINFO) != null) {
             int flaecheninfoId = (Integer) clipboardBean.getProperty(PROP__FLAECHENINFO__ID);
             CidsBean flaecheninfoBean = SessionManager.getProxy().getMetaObject(flaecheninfoId, CidsAppBackend.getInstance().getVerdisMetaClass(VerdisMetaClassConstants.MC_FLAECHENINFO).getId(), Main.DOMAIN).getBean();
             pasteBean.setProperty(PROP__FLAECHENINFO, flaecheninfoBean);
         }
 
         pasteBean.setProperty(PROP__BEMERKUNG, null);
         pasteBean.setProperty(PROP__FLAECHENBEZEICHNUNG, flaechenTable.getValidFlaechenname((Integer) clipboardBean.getProperty(PROP__FLAECHENINFO__FLAECHENART__ID)));
         final Calendar cal = Calendar.getInstance();
         pasteBean.setProperty(PROP__DATUM_ERFASSUNG, new Date(cal.getTime().getTime()));
         cal.add(Calendar.MONTH, 1);
         final SimpleDateFormat vDat = new SimpleDateFormat("yy/MM");
         pasteBean.setProperty(PROP__DATUM_VERANLAGUNG, vDat.format(cal.getTime()));
 
         return pasteBean;
     }
 
     private boolean isPastable(final CidsBean clipboardFlaecheBean) {
         if (clipboardFlaecheBean == null) {
             return false;
         }
 
         for (final CidsBean flaecheBean : flaechenTable.getAllBeans()) {
             final int id = (Integer) flaecheBean.getProperty(PROP__FLAECHENINFO__ID);
             final int ownId = (Integer) clipboardFlaecheBean.getProperty(PROP__FLAECHENINFO__ID);
             if (id == ownId) {
                 return false;
             }
         }
 
         return true;
     }
 
     public boolean isPastable() {
         return !clipboardFlaecheBeans.isEmpty();
     }
 
     public boolean isCopyable() {
         return !isSelectionEmpty();
     }
 
     public boolean isCutable() {
         return !isSelectionEmpty();
     }
 
     private boolean isSelectionEmpty() {
         return flaechenTable.getSelectedBeans().isEmpty();
 
     }
 
     private boolean cutOrCopy(final Collection<CidsBean> flaecheBeans) {
         if (flaecheBeans != null && !flaecheBeans.isEmpty()) {
             if (!checkNotPasted()) {
                 return false;
             }
             try {
                 clipboardFlaecheBeans.clear();
                 for (final CidsBean flaecheBean : flaecheBeans) {
                     this.clipboardFlaecheBeans.add(CidsBeanSupport.cloneCidsBean(flaecheBean));
                 }
                 fireClipboardChanged();
                 return true;
             } catch (Exception ex) {
                 LOG.error("error while copying or cutting cidsbean", ex);
                 clipboardFlaecheBeans.clear();
                 return false;
             }
         } else {
             return false;
         }
     }
 
     public void copy() {
         if (isCopyable()) {
             final Collection<CidsBean> selectedBeans = getSelectedFlaechenBean();
             cutOrCopy(selectedBeans);
         }
     }
 
     public void cut() {
         if (isCutable()) {
             final Collection<CidsBean> selectedBeans = getSelectedFlaechenBean();
             isCutted = cutOrCopy(selectedBeans);
             if (isCutted) {
                 for (final CidsBean selectedBean : selectedBeans) {
                     flaechenTable.removeBean(selectedBean);
                 }
             }
         }
     }
 
     private Collection<CidsBean> getSelectedFlaechenBean() {
         return flaechenTable.getSelectedBeans();
     }
 
     private boolean checkNotPasted() {
         int answer = JOptionPane.YES_OPTION;
         if (isCutted && clipboardFlaecheBeans != null) {
             answer = JOptionPane.showConfirmDialog(
                     Main.THIS,
                     "In der Verdis-Zwischenablage befinden sich noch Daten die\nausgeschnitten und noch nicht wieder eingef\u00FCgt wurden.\nMöchten Sie diese Daten jetzt verwerfen ?",
                     "Ausschneiden",
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.QUESTION_MESSAGE);
         }
         return answer == JOptionPane.YES_OPTION;
     }
 
     public void storeToFile() {
 //        CismetThreadPool.execute(new Runnable() {
 //
 //            @Override
 //            public void run() {
 //                try {
 //                    final XStream x = new XStream(new Dom4JDriver());
 //                    final FileWriter f = new FileWriter(Main.verdisDirectory + Main.fs + "flaechenClipboardBackup.xml");
 //                    x.toXML(clipboardBeans, f);
 //                    f.close();
 //                } catch (Exception ex) {
 //                    LOG.error("Beim Sichern des Clipboards ist etwas schiefgegangen", ex);
 //                }
 //            }
 //        });
     }
 
     public void loadFromFile() {
 //        CismetThreadPool.execute(new Runnable() {
 //
 //            @Override
 //            public void run() {
 //                try {
 //                    final XStream x = new XStream(new Dom4JDriver());
 //                    clipboardBeans = (List<CidsBean>) x.fromXML(new FileReader(Main.verdisDirectory + Main.fs + "flaechenClipboardBackup.xml"));
 //                    clipboardModus = Modus.COPY;
 //                } catch (Exception exception) {
 //                    LOG.error("Beim Laden des Flaechen-ClipboardBackups ist etwas schiefgegangen", exception);
 //                }
 //            }
 //        });
     }
 
     public void deleteStoreFile() {
         
     }
 }
