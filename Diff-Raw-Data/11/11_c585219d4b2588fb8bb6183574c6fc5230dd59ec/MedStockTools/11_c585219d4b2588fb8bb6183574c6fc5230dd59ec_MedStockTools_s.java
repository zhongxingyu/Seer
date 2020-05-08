 package entity.prescription;
 
 import entity.Station;
 import entity.info.Resident;
 import entity.info.ResidentTools;
 import op.OPDE;
 import op.care.med.inventory.PnlInventory;
 import op.care.med.structure.DlgUPREditor;
 import op.controlling.PnlControlling;
 import op.tools.Pair;
 import op.tools.SYSConst;
 import op.tools.SYSTools;
 import org.apache.commons.collections.Closure;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 
 import javax.persistence.EntityManager;
 import javax.persistence.LockModeType;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.swing.*;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: tloehr
  * Date: 18.11.11
  * Time: 14:28
  * To change this template use File | Settings | File Templates.
  */
 public class MedStockTools {
 //    public static boolean apvNeuberechnung = true;
 
     public static final int STATE_NOTHING = 0;
     public static final int STATE_WILL_BE_CLOSED_SOON = 10;
 
     // These modes work only in the beginning of the lifecyle of a new TradeForm. When the first stock (UPRn) is added, then its not yet clear which UPR to assume
     // so its always a dummy UPR which is replaced by the first calculated effective UPR when closing this first package.
     // This is also needed when fiddling with the UPRs afterwards in order to correct awkward values.
     public static final int ADD_TO_AVERAGES_UPR_WHEN_CLOSING = 0;
     public static final int REPLACE_WITH_EFFECTIVE_UPR_WHEN_CLOSING = 1;
     public static final int DONT_REPLACE_UPR = 2;
 
     public static ListCellRenderer getBestandOnlyIDRenderer() {
         return new ListCellRenderer() {
             @Override
             public java.awt.Component getListCellRendererComponent(JList jList, Object o, int i, boolean isSelected, boolean cellHasFocus) {
                 String text;
                 if (o == null) {
                     text = "<i>Keine Auswahl</i>"; //SYSTools.toHTML("<i>Keine Auswahl</i>");
                 } else if (o instanceof MedStock) {
                     text = ((MedStock) o).getID().toString();
                 } else {
                     text = o.toString();
                 }
                 return new DefaultListCellRenderer().getListCellRendererComponent(jList, text, i, isSelected, cellHasFocus);
             }
         };
     }
 
     /**
      * Sucht aus den den Beständen des Vorrats den angebrochenen heraus.
      *
      * @param inventory
      * @return der angebrochene Bestand. null, wenn es keinen gab.
      */
     public static MedStock getStockInUse(MedInventory inventory) {
         MedStock bestand = null;
         if (inventory != null && inventory.getMedStocks() != null) {
             Iterator<MedStock> itBestand = inventory.getMedStocks().iterator();
             while (itBestand.hasNext()) {
                 MedStock b = itBestand.next();
                 if (b.isOpened() && !b.isClosed()) {
                     bestand = b;
                     break;
                 }
             }
         }
         return bestand;
     }
 
     public static HashMap getStock4Printing(MedStock bestand) {
         OPDE.debug("StockID: " + bestand.getID());
 
         HashMap hm = new HashMap();
         hm.put("medstock.tradeform", TradeFormTools.toPrettyString(bestand.getTradeForm()));
 
         String pzn = bestand.getPackage() == null ? "??" : bestand.getPackage().getPzn();
         hm.put("medstock.package.pzn", pzn);
         hm.put("medstock.id", bestand.getID());
         hm.put("medstock.in", bestand.getIN());
 
         // stock expires
         hm.put("medstock.expires", bestand.getExpires());
 
         // expires after being opened
         if (bestand.getTradeForm().getDaysToExpireAfterOpened() == null) {
             hm.put("medstock.tradeform.expires.after.opened", null);
         } else {
             int days = bestand.getTradeForm().getDaysToExpireAfterOpened();
             int weeks = 0;
             if (days >= 7) {
                 weeks = days / 7;
             }
             String exp = weeks > 0 ? weeks + OPDE.lang.getString("misc.msg.weeks").substring(0, 1).toUpperCase() : days + OPDE.lang.getString("misc.msg.Days").substring(0, 1).toUpperCase();
             hm.put("medstock.tradeform.expires.after.opened", "!!" + exp);
         }
 
         hm.put("medstock.usershort", bestand.getUser().getUID());
         hm.put("medstock.userlong", bestand.getUser().getFullname());
         hm.put("medstock.inventory.resident.name", ResidentTools.getNameAndFirstname(bestand.getInventory().getResident()));
         hm.put("medstock.inventory.resident.dob", bestand.getInventory().getResident().getDOB());
         hm.put("medstock.inventory.resident.id", bestand.getInventory().getResident().getRIDAnonymous());
 
         return hm;
     }
 
     public static BigDecimal getSum(MedStock bestand) {
         BigDecimal result = BigDecimal.ZERO;
         for (MedStockTransaction buchung : bestand.getStockTransaction()) {
             result = result.add(buchung.getAmount());
         }
         return result;
     }
 
     /**
      * gets the sum of doses in the BHP (note: this figure is in usage unit)
      *
      * @param medStock
      * @return
      */
     public static BigDecimal getSumOfDosesInBHP(MedStock medStock) {
         BigDecimal result = BigDecimal.ZERO;
         for (MedStockTransaction tx : medStock.getStockTransaction()) {
             if (!tx.isPartOfCancelPair() && tx.isBHP()) {
                 result = result.add(tx.getBhp().getDose());
             }
         }
         return result;
     }
 
     /**
      * Ermittelt die Menge, die in einer Packung noch enthalten ist.
      *
      * @param stock die entsprechende Packung
      * @return die Summe in der Packungs Einheit.
      */
     public static BigDecimal getSum(EntityManager em, MedStock stock) throws Exception {
         BigDecimal result;
 //        OPDE.debug("BestID: " + stock.getID());
         Query query = em.createQuery(" " +
                 " SELECT SUM(tx.amount) " +
                 " FROM MedStock st " +
                 " JOIN st.stockTransaction tx " +
                 " WHERE st = :stock ");
 
         try {
             query.setParameter("stock", stock);
             result = (BigDecimal) query.getSingleResult();
         } catch (NoResultException nre) {
             result = BigDecimal.ZERO;
         }
 
         return result;
     }
 
     /**
      * This method closes the given MedStock. It forces the current balance to zero by adding a system MedStockTransaction
      * to the existing list of TXs.
      * <p/>
      * If there is a next package to be used, it will be opened.
      * <p/>
      * If there are prescription (valid until the end of this package) there will be closed, too, unless
      * there are still unused packages in this inventory.
      *
      * @param em       persistence context to be used
      * @param medStock to be closed
      * @param text     for the closing TX
      * @param state    of the closing TX
      * @return if there is a nextStock to be opened after this one, it will be returned here. NULL otherwise.
      * @throws Exception
      */
     public static MedStock close(EntityManager em, MedStock medStock, String text, short state) throws Exception {
         MedStock nextStock = null;
         BigDecimal stocksum = getSum(medStock);
         MedStockTransaction finalTX = new MedStockTransaction(medStock, stocksum.negate(), state);
         finalTX.setText(text);
         medStock.getStockTransaction().add(finalTX);
         medStock.setState(MedStockTools.STATE_NOTHING);
         DateTime now = new DateTime();
         medStock.setOut(now.toDate());
 
         BigDecimal effectiveUPR = getEffectiveUPR(medStock);
         if (medStock.getTradeForm().getDosageForm().isUPRn()) {
             if (medStock.getTradeForm().getUPR() != null) {
                 medStock.setUPR(medStock.getTradeForm().getUPR());
                 medStock.setUPRDummyMode(ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
             } else {
                 if (medStock.getUPRDummyMode() == ADD_TO_AVERAGES_UPR_WHEN_CLOSING) {
                     if (effectiveUPR.compareTo(BigDecimal.ZERO) > 0) { // if this stock was never used the effective UPR must be 0. we must handle this case separately
                         // if the deviation was too high (usually more than 20%), then the new UPR is discarded
                         BigDecimal maxDeviation = new BigDecimal(Double.parseDouble(OPDE.getProps().getProperty("apv_korridor")));
                         BigDecimal deviation = medStock.getUPR().divide(effectiveUPR, 4, BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(100)).abs();
                         OPDE.debug("the deviation was: " + deviation + "%");
 
                         // if the deviation is below the limit, then the new UPR will be accepted.
                         // it must also be greater than 0
                         if (deviation.compareTo(maxDeviation) <= 0 && effectiveUPR.compareTo(BigDecimal.ZERO) > 0) {
                             OPDE.debug("acceptable");
                             medStock.setUPR(effectiveUPR);
                         } else {
                             OPDE.debug("discarded");
                         }
                     } else {
                         OPDE.debug("effective UPR is 0 or less. new UPR discarded");
                     }
                 } else if (medStock.getUPRDummyMode() == REPLACE_WITH_EFFECTIVE_UPR_WHEN_CLOSING) {  // REPLACE_WITH_EFFECTIVE_UPR_WHEN_CLOSING
                     medStock.setUPR(effectiveUPR);
                     medStock.setUPRDummyMode(ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
                 } else { // DONT_REPLACE_UPR
                     medStock.setUPRDummyMode(ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
                 }
             }
         }
         medStock.setUPREffective(effectiveUPR);
 
         if (medStock.hasNext2Open()) {
             nextStock = medStock.getNextStock();
             nextStock.setOpened(now.plusSeconds(1).toDate());
             OPDE.debug("NextStock: " + medStock.getNextStock().getID() + " will be opened now");
         } else {
             // Nothing to open next ?
             // Are there still stocks in this inventory ?
             if (MedInventoryTools.getNextToOpen(medStock.getInventory()) == null) {
                 // No ??
                 // Are there any prescriptions that needs to be closed now, because of the empty package ?
                 for (Prescription prescription : PrescriptionTools.getPrescriptionsByInventory(medStock.getInventory())) {
                     if (prescription.isUntilEndOfPackage()) {
                         prescription = em.merge(prescription);
                         em.lock(prescription, LockModeType.OPTIMISTIC);
                         prescription.setTo(new Date());
                         prescription.setUserOFF(em.merge(OPDE.getLogin().getUser()));
                         prescription.setDocOFF(prescription.getDocON());
                         prescription.setHospitalOFF(prescription.getHospitalON());
                     }
                 }
             }
         }
         return nextStock;
     }
 
     public static MedStockTransaction getStartTX(MedStock bestand) {
         MedStockTransaction result = null;
         for (MedStockTransaction buchung : bestand.getStockTransaction()) {
             if (buchung.getState() == MedStockTransactionTools.STATE_CREDIT) {
                 result = buchung;
                 break;
             }
         }
         return result;
     }
 
 
     /**
      * Diese Methode bucht auf einen Bestand immer genau soviel drauf oder runter, damit er auf
      * dem gewünschten soll landet.
      *
      * @param bestand um die es geht.
      * @param soll    gewünschter Endbestand
      * @param em      EntityManager in dessen Kontext das ganze abläuft
      * @param text    für die Buchung
      * @param status  für die Buchung
      * @return die Korrektur Buchung
      * @throws Exception
      */
 
     public static MedStockTransaction setStockTo(EntityManager em, MedStock bestand, BigDecimal soll, String text, short status) throws Exception {
         MedStockTransaction result = null;
         bestand = em.merge(bestand);
 
         BigDecimal bestandSumme = getSum(em, bestand);
 
         if (!bestandSumme.equals(soll)) {
             BigDecimal korrektur;
             if (bestandSumme.compareTo(BigDecimal.ZERO) <= 0) {
                 korrektur = bestandSumme.abs().add(soll);
             } else {
                 korrektur = bestandSumme.negate().add(soll);
             }
 
             // passende Buchung anlegen.
             result = em.merge(new MedStockTransaction(bestand, korrektur, status));
             result.setText(text);
         }
         return result;
     }
 
     public static String getTextASHTML(MedStock bestand) {
         String result = "";
         result += "<font color=\"blue\"><b>" + bestand.getTradeForm().getMedProduct().getText() + " " + bestand.getTradeForm().getSubtext() + ", ";
 
         if (bestand.getPackage() != null && !SYSTools.catchNull(bestand.getPackage().getPzn()).isEmpty()) {
             result += OPDE.lang.getString("misc.msg.PZN") + ": " + bestand.getPackage().getPzn() + ", ";
             result += MedPackageTools.GROESSE[bestand.getPackage().getSize()] + ", " + bestand.getPackage().getContent() + " " + SYSConst.UNITS[bestand.getTradeForm().getDosageForm().getPackUnit()] + " ";
             String zubereitung = SYSTools.catchNull(bestand.getTradeForm().getDosageForm().getPreparation());
             String anwtext = SYSTools.catchNull(bestand.getTradeForm().getDosageForm().getUsageText());
             result += zubereitung.equals("") ? anwtext : (anwtext.equals("") ? zubereitung : zubereitung + ", " + anwtext);
             result += "</b></font>";
         }
 
         return result;
     }
 
     public static String getAsHTML(MedStock stock) {
         String result = "";
 
         String htmlcolor = stock.isClosed() ? "gray" : "red";
 
         result += "<font face =\"" + SYSConst.ARIAL14.getFamily() + "\">";
         result += "<font color=\"" + htmlcolor + "\"><b><u>" + stock.getID() + "</u></b></font>&nbsp; ";
         result += TradeFormTools.toPrettyString(stock.getTradeForm());
 
         if (stock.hasPackage()) {
             result += ", " + MedPackageTools.toPrettyString(stock.getPackage());
         }
 
         if (stock.getTradeForm().getDosageForm().isUPRn()) {
             result += ", APV: ";
             if (stock.getTradeForm().getUPR() != null) {
                 result += stock.getTradeForm().getUPR().setScale(2, RoundingMode.HALF_UP).toString() + " (" + OPDE.lang.getString(DlgUPREditor.internalClassID + ".constant.upr") + ")";
             } else {
                 result += stock.getUPR().setScale(2, RoundingMode.HALF_UP).toString();
             }
             result += " " + (stock.getUPRDummyMode() == REPLACE_WITH_EFFECTIVE_UPR_WHEN_CLOSING && stock.getTradeForm().getUPR() != null ? OPDE.lang.getString(PnlInventory.internalClassID + ".UPRwillBeReplaced") : "");
         }
 
         if (stock.hasNext2Open()) {
             result += ", <b>" + OPDE.lang.getString(PnlInventory.internalClassID + ".nextstock") + ": " + stock.getNextStock().getID() + "</b>";
         } else if (stock.isToBeClosedSoon()) {
             result += ", <b>" + OPDE.lang.getString(PnlInventory.internalClassID + ".stockWillBeClosedSoon") + "</b>";
         }
 
         DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
 
         result += "&nbsp;<font color=\"blue\">" + OPDE.lang.getString("misc.msg.incoming") + ": " + df.format(stock.getIN()) + "</font>";
         if (stock.isOpened()) {
             result += "&nbsp;<font color=\"green\">" + OPDE.lang.getString("misc.msg.opening") + ": " + df.format(stock.getOpened()) + "</font>";
 
             // variable expiry ?
             if (stock.getTradeForm().getDaysToExpireAfterOpened() != null) {
                 String color = stock.isExpired() ? "red" : "black";
                 result += "&nbsp;<font color=\"" + color + "\">" + OPDE.lang.getString("misc.msg.expiresAfterOpened") + ": " + df.format(new DateTime(stock.getOpened()).plusDays(stock.getTradeForm().getDaysToExpireAfterOpened()).toDate()) + "</font>";
             }
 
         }
 
         // fixed expiry ?
         if (stock.getExpires() != null && !stock.isClosed()) {
             String color = stock.isExpired() ? "red" : "black";
 
             DateFormat sdf = df;
             // if expiry isa at the end of a month then it has a different format
             if (new DateMidnight(stock.getExpires()).equals(new DateMidnight(stock.getExpires()).dayOfMonth().withMaximumValue())) {
                 sdf = new SimpleDateFormat("MM/yy");
             }
             result += "&nbsp;<font color=\"" + color + "\">" + OPDE.lang.getString("misc.msg.expires") + ": " + sdf.format(stock.getExpires()) + "</font>";
         }
 
         if (stock.isClosed()) {
             result += SYSConst.html_bold("&nbsp;<font color=\"black\">" + OPDE.lang.getString("misc.msg.outgoing") + ": " + df.format(stock.getOut()) + "</font>");
         }
         result += "</font>";
 
         return result;
 
     }
 
 //    public static String getCompactHTML(MedStock stock) {
 //        String result = "";
 //        result += "<b>" + stock.getID() + "</b>&nbsp;";
 //        result += TradeFormTools.toPrettyString(stock.getTradeForm());
 //
 //        if (stock.hasPackage()) {
 //            result += ", " + MedPackageTools.toPrettyString(stock.getPackage());
 //        }
 //        result += ", APV: " + NumberFormat.getNumberInstance().format(stock.getUPR()) + " " + (stock.isDummyUPR() ? OPDE.lang.getString(PnlInventory.internalClassID + ".UPRwillBeReplaced") : "");
 //        return result;
 //    }
 
     public static void open(EntityManager em, MedStock stock) {
         MedStock myStock = em.merge(stock);
         myStock.setOpened(new Date());
     }
 
     public static MedStock getPreviousStock(MedStock fromThisOne) {
         MedInventory inventory = fromThisOne.getInventory();
         Collections.sort(inventory.getMedStocks());
         int index = inventory.getMedStocks().indexOf(fromThisOne);
         MedStock previous = null;
         if (index > 0) {
             previous = inventory.getMedStocks().get(index - 1);
         }
         return previous;
     }
 
     public static String getListForMedControl(Station station, Closure progress) {
         StringBuilder html = new StringBuilder(1000);
         int p = -1;
         progress.execute(new Pair<Integer, Integer>(p, 100));
         Resident prevResident = null;
 
         EntityManager em = OPDE.createEM();
         String jpql = " " +
                 " SELECT b FROM MedStock b " +
                 " WHERE b.inventory.resident.station = :station AND b.inventory.resident.adminonly <> 2 " +
                 " AND b.opened < :opened AND b.out = :tfn " +
                 " ORDER BY b.inventory.resident.name, b.inventory.resident.firstname, b.inventory.text, b.opened ";
 
         Query query = em.createQuery(jpql);
         query.setParameter("opened", new Date());
         query.setParameter("tfn", SYSConst.DATE_UNTIL_FURTHER_NOTICE);
         query.setParameter("station", station);
         List<MedStock> list = query.getResultList();
         em.close();
 
         StringBuilder table = new StringBuilder(1000);
         DateFormat df = DateFormat.getDateInstance();
 
         html.append(SYSConst.html_h1(PnlControlling.internalClassID + ".drugs.controllist"));
         html.append(SYSConst.html_h2(OPDE.lang.getString("misc.msg.subdivision") + ": " + station.getName()));
         html.append(SYSConst.html_h3("misc.msg.key"));
         html.append(SYSConst.html_ul(
                 SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.1") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.2") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.3") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.4") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.5") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.6") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.7") +
                         SYSConst.html_li(PnlControlling.internalClassID + ".drugs.controllist.key.8")
         ));
 
         table.append(SYSConst.html_table_tr(
                 SYSConst.html_table_th("misc.msg.resident") +
                         SYSConst.html_table_th(PnlInventory.internalClassID + ".search.stockid") +
                         SYSConst.html_table_th("misc.msg.medication") +
                         SYSConst.html_table_th("misc.msg.opened") +
                         SYSConst.html_table_th("#1") +
                         SYSConst.html_table_th("#2") +
                         SYSConst.html_table_th("#3") +
                         SYSConst.html_table_th("#4") +
                         SYSConst.html_table_th("#5") +
                         SYSConst.html_table_th("#6") +
                         SYSConst.html_table_th("#7") +
                         SYSConst.html_table_th("#8")
         ));
 
         p = 0;
         int zebra = 0;
         for (MedStock bestand : list) {
             progress.execute(new Pair<Integer, Integer>(p, list.size()));
             p++;
 
             if (prevResident != bestand.getInventory().getResident()) {
                 zebra++;
                 prevResident = bestand.getInventory().getResident();
             }
 
             table.append(SYSConst.html_table_tr(
                     SYSConst.html_table_td(ResidentTools.getTextCompact(bestand.getInventory().getResident())) +
                             SYSConst.html_table_td(bestand.getID().toString()) +
                             SYSConst.html_table_td(TradeFormTools.toPrettyString(bestand.getTradeForm())) +
                             SYSConst.html_table_td(df.format(bestand.getOpened())) +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;") +
                             SYSConst.html_table_td("&nbsp;")
                     , zebra % 2 == 0));
         }
         html.append(SYSConst.html_table(table.toString(), "1"));
         return html.toString();
     }
 
     /**
      * This method calculates the effective UPR as it transpired during the lifetime of that particular medstock.
      * It is vital, that this calculation is only done, when a package is empty. Otherwise the estimation
      * of the UPR is wrong.
      *
      * @param medstock, für den das Verhältnis neu berechnet werden soll.
      */
     public static BigDecimal getEffectiveUPR(MedStock medstock) {
         if (medstock.getTradeForm().getDosageForm().isUPR1()) {
             return BigDecimal.ONE;
         }
         if (medstock.getTradeForm().getUPR() != null) {
             return medstock.getTradeForm().getUPR();
         }
 
         OPDE.debug("<--- recalculateUPR ");
         OPDE.debug("MedStock ID: " + medstock.getID());
 
         // this is the amount of content, which was in that package before it was opened
         // package unit
         BigDecimal startContent = MedStockTools.getStartTX(medstock).getAmount();
 
         // usage unit
         BigDecimal theoreticalSum = MedStockTools.getSumOfDosesInBHP(medstock);
 
         // Die Gaben aus der BHP sind immer in der Anwendungseinheit. Teilt man diese durch das
         // verwendete APV, erhält man das was rechnerisch in der Packung drin gewesen
         // sein soll. Das kann natürlich von dem realen Inhalt abweichen. Klebt noch was an
         // der Flaschenwand oder wurde was verworfen. Das APV steht ja für Anzahl der Anwendung im
         // Verhaltnis zur Packungseinheit 1. Wurden 100 Tropfen gegeben, bei einem APV von 20(:1)
         // Dann ergibt das einen rechnerischen Flascheninhalt von 5 ml.
 
         // The doses of the applications which have been calculated by the BHPs are always in
         // the unit of the usage.
         // When a package is empty, we know two things for sure:
         // 1. The startContent has been completely used up
         // 2. the sum of all applications (theoreticalSum) is what we've got really out of the bottle
         //
         // hence the effective UPR must have been
         //
         //                          the sum of all applications in the usage unit
         //    effective UPR   =     --------------------------------------------
         //                          the startContent in the package unit
         //
         BigDecimal effectiveUPR = theoreticalSum.divide(startContent, 4, BigDecimal.ROUND_UP);
 
         return effectiveUPR;
     }
 
     /**
      * calculates a starting UPR for a newly opened stock. If there is no UPR yet, it creates a new one and marks it as dummy,
      * so it will be replaced by the first calculated result, when this package is closed.
      * For DosageForms with type STATE_UPR1, there is no calculation at all. Those are always 1 constantly.
      */
     public static BigDecimal getEstimatedUPR(TradeForm tradeForm) {
         OPDE.debug("<--- calcProspectiveUPR");
         BigDecimal upr = null;
         if (tradeForm.getDosageForm().getUPRState() == DosageFormTools.STATE_DONT_CALC) {
             OPDE.debug("STATE_DONT_CALC");
             // no calculation for gel or ointments. they wont work out anyways.
             upr = BigDecimal.ONE;// getEstimatedUPR_BY_RESIDENT(tradeForm, resident);
         } else if (tradeForm.getDosageForm().getUPRState() == DosageFormTools.STATE_UPRn) {
             OPDE.debug("STATE_UPRn");
 
             if (tradeForm.getUPR() != null) {
                 // if there is a constant UPR defined for that tradeform
                 // so there is no estimation necessary
                 upr = tradeForm.getUPR();
                 OPDE.debug("constant UPRn");
             } else {
                 EntityManager em = OPDE.createEM();
                 try {
                     Query query = em.createQuery("SELECT AVG(s.upr) FROM MedStock s WHERE s.tradeform = :tradeform AND s.uprDummyMode = :dummymode ");
                     query.setParameter("dummymode", ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
                     query.setParameter("tradeform", tradeForm);
                     Object result = query.getSingleResult();
 
                     if (result == null) {
                         upr = BigDecimal.ONE;
                         OPDE.debug("calculated UPRn. first of its kind. UPR: 1");
                     } else if (result instanceof Double) {
                         upr = new BigDecimal((Double) result);
                     } else {
                         upr = (BigDecimal) query.getSingleResult();
                     }
                     upr = upr.setScale(2, BigDecimal.ROUND_HALF_UP);
                     OPDE.debug("calculated UPRn. average so far: " + upr.toString());
                 } catch (NoResultException nre) {
                     upr = BigDecimal.ONE;
                 } catch (Exception e) {
                     OPDE.fatal(e);
                 } finally {
                     em.close();
                 }
 
             }
 
         } else {
             OPDE.debug("STATE_UPR1");
             upr = BigDecimal.ONE;
         }
         OPDE.debug("upr: " + upr);
         OPDE.debug("calcProspectiveUPR --->");
         return upr;
     }
 
     public static boolean isNoStockYetForThis(TradeForm tradeForm) {
         Integer count = null;
         EntityManager em = OPDE.createEM();
         try {
             Query query = em.createQuery("SELECT COUNT(s.upr) FROM MedStock s WHERE s.tradeform = :tradeform AND s.uprDummyMode = :dummymode ");
             query.setParameter("dummymode", ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
             query.setParameter("tradeform", tradeForm);
             Object result = query.getSingleResult();
 
             if (result == null) {
                 count = 0;
             } else if (result instanceof Long) {
                 count = ((Long) result).intValue();
             } else {
                 count = (Integer) query.getSingleResult();
             }
         } catch (NoResultException nre) {
             count = 0;
         } catch (Exception e) {
             OPDE.fatal(e);
         } finally {
             em.close();
         }
         return count == 0;
     }
 
 //    private static BigDecimal getEstimatedUPR_BY_TRADEFORM(TradeForm tradeForm) {
 //        BigDecimal upr = null;
 //        EntityManager em = OPDE.createEM();
 //        try {
 //            Query query = em.createQuery("SELECT AVG(s.upr) FROM MedStock s WHERE s.tradeform = :tradeform AND s.uprDummyMode = :dummymode ");
 //            query.setParameter("dummymode", ADD_TO_AVERAGES_UPR_WHEN_CLOSING);
 //            query.setParameter("tradeform", tradeForm);
 //            Object result = query.getSingleResult();
 //
 //            if (result == null) {
 //                upr = BigDecimal.ONE;
 //            } else if (result instanceof Double) {
 //                upr = new BigDecimal((Double) result);
 //            } else {
 //                upr = (BigDecimal) query.getSingleResult();
 //            }
 //            upr = upr.setScale(2, BigDecimal.ROUND_HALF_UP);
 //        } catch (NoResultException nre) {
 //            upr = null;
 //        } catch (Exception e) {
 //            OPDE.fatal(e);
 //        } finally {
 //            em.close();
 //        }
 //        return upr;
 //    }
 
 //    private static BigDecimal getEstimatedUPR_BY_RESIDENT(TradeForm tradeForm, Resident resident) {
 //        BigDecimal upr = null;
 //        EntityManager em = OPDE.createEM();
 //        try {
 //            Query query = em.createQuery("SELECT AVG(s.upr) FROM MedStock s WHERE s.tradeform = :tradeform AND s.inventory.resident = :resident AND s.uprDummy = FALSE ");
 //            query.setParameter("tradeform", tradeForm);
 //            query.setParameter("resident", resident);
 //            Object result = query.getSingleResult();
 //
 //            if (result == null) {
 //                upr = BigDecimal.ONE;
 //            } else if (result instanceof Double) {
 //                upr = new BigDecimal((Double) result);
 //            } else {
 //                upr = (BigDecimal) query.getSingleResult();
 //            }
 //            upr = upr.setScale(2, BigDecimal.ROUND_HALF_UP);
 //        } catch (NoResultException nre) {
 //            upr = null;
 //        } catch (Exception e) {
 //            OPDE.fatal(e);
 //        } finally {
 //            em.close();
 //        }
 //        return upr;
 //    }
 
 //    /**
 //     * sets the upr for all stocks in that list.
 //     *
 //     * @param upr
 //     * @param listStocks
 //     * @throws Exception
 //     */
 //    public static void setUPR(EntityManager em, BigDecimal upr, ArrayList<MedStock> listStocks) throws Exception {
 //        for (MedStock s : listStocks) {
 //            MedStock stock = em.merge(s);
 //            em.lock(stock, LockModeType.OPTIMISTIC);
 //            em.lock(stock.getInventory(), LockModeType.OPTIMISTIC_FORCE_INCREMENT);
 //            stock.setUPR(upr);
 //            stock.setUPRDummyMode(ADD_TO_AVERAGES_UPR_WHEN_CLOSING); // no dummies after this has been set
 //        }
 //    }
 
     public static List<MedStock> getExpiryList(int days) {
         ArrayList<MedStock> list = new ArrayList<MedStock>();
 
         EntityManager em = OPDE.createEM();
         Query query1 = em.createQuery("SELECT s FROM MedStock s WHERE s.expires IS NOT NULL AND s.out = :tfn ");
         query1.setParameter("tfn", SYSConst.DATE_UNTIL_FURTHER_NOTICE);
        ArrayList<MedStock> list1 = new ArrayList<MedStock>(query1.getResultList());
 
         Query query2 = em.createQuery("SELECT s FROM MedStock s JOIN s.tradeform t WHERE t.daysToExpireAfterOpened IS NOT NULL AND s.out = :tfn AND s.opened < :now");
         query2.setParameter("tfn", SYSConst.DATE_UNTIL_FURTHER_NOTICE);
         query2.setParameter("now", new Date());
        list1.addAll(new ArrayList<MedStock>(query2.getResultList()));
 
        for (MedStock stock : list1) {
             if (stock.expiresIn(days)) {
                 list.add(stock);
             }
         }
 
        list1.clear();
 
         em.close();
         return list;
     }
 }
