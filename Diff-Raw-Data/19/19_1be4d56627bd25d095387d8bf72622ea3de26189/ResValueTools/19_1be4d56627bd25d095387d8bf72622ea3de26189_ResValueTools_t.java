 package entity.values;
 
 
 import entity.Station;
 import entity.StationTools;
 import entity.info.Resident;
 import entity.info.ResidentTools;
 import entity.nursingprocess.DFNTools;
 import op.OPDE;
 import op.care.values.PnlValues;
 import op.controlling.PnlControlling;
 import op.tools.Pair;
 import op.tools.SYSCalendar;
 import op.tools.SYSConst;
 import op.tools.SYSTools;
 import org.apache.commons.collections.Closure;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 import javax.persistence.TemporalType;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: tloehr
  * Date: 28.10.11
  * Time: 16:41
  * To change this template use File | Settings | File Templates.
  */
 public class ResValueTools {
 
     // weight adjustment percentages for amputation
     public static final BigDecimal FOOT = new BigDecimal(1.8);
     public static final BigDecimal BELOW_KNEE = new BigDecimal(6);
     public static final BigDecimal ABOVE_KNEE = new BigDecimal(15);
     public static final BigDecimal ENTIRE_LOWER_EXTREMITY = new BigDecimal(18.5);
     public static final BigDecimal HAND = new BigDecimal(1);
     public static final BigDecimal BELOW_ELLBOW = new BigDecimal(3);
     public static final BigDecimal ABOVE_ELLBOW = new BigDecimal(5);
     public static final BigDecimal ENTIRE_UPPER_EXTREMITY = new BigDecimal(6.5);
 
 
 //    public static final String[] VALUES = new String[]{"UNKNOWN", "RR", "PULSE", "TEMP", "GLUCOSE", "WEIGHT", "HEIGHT", "BREATHING", "QUICK", "STOOL", "VOMIT", "LIQUIDBALANCE"};
 
     //    public static final String[] UNITS = new String[]{"", "mmHg", "s/m", "°C", "mg/dl", "kg", "m", "A/m", "%", "", "", "ml"};
     public static final String RRSYS = "systolisch";
     public static final String RRDIA = "diatolisch";
 
     /**
      * Rendert eine HTML Darstellung des Datums und des Benutzers eines bestimmten Bewohner Wertes
      *
      * @param bwert
      * @param showids
      * @param colorize
      * @return
      */
     public static String getPITasHTML(ResValue bwert, boolean showids, boolean colorize) {
         SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd.MM.yyyy HH:mm");
         String color = "";
         if (colorize) {
             if (bwert.isReplaced() || bwert.isDeleted()) {
                 color = SYSConst.html_lightslategrey;
             } else {
                 color = OPDE.getProps().getProperty(DFNTools.SHIFT_KEY_TEXT[SYSCalendar.whatShiftIs(bwert.getPit())] + "_FGBHP");
             }
         }
         String result = sdf.format(bwert.getPit()) + "; " + bwert.getUser().getFullname();
         if (showids) {
             result += "<br/><i>[" + bwert.getID() + "]</i>";
         }
         return (colorize ? "<font " + color + " " + SYSConst.html_arial14 + ">" + result + "</font>" : result);
     }
 
 
     public static ArrayList<Integer> getYearsWithValues(Resident resident, ResValueTypes type) {
         EntityManager em = OPDE.createEM();
         Query query = em.createNativeQuery("SELECT DISTINCT(YEAR(PIT)) j FROM resvalue WHERE BWKennung = ? AND TYPE = ? ORDER BY j DESC");
         query.setParameter(1, resident.getRID());
         query.setParameter(2, type.getValType());
         ArrayList<Integer> result = new ArrayList<Integer>(query.getResultList());
         em.close();
         return result;
     }
 
     public static ArrayList<Date> getDaysWithValues(Resident resident, ResValueTypes type, int year) {
         DateTime from = new LocalDate(year, 1, 1).dayOfYear().withMinimumValue().toDateTimeAtStartOfDay();
         DateTime to = new LocalDate(year, 1, 1).dayOfYear().withMaximumValue().toDateTimeAtStartOfDay().secondOfDay().withMaximumValue();
 
         EntityManager em = OPDE.createEM();
         Query query = em.createNativeQuery(" " +
                 " SELECT DISTINCT DATE(r.pit) d" +
                 " FROM resvalue r " +
                 " INNER JOIN resvaluetypes t ON r.type = t.id " +
                 " WHERE DATE(r.pit) >= ? AND DATE(r.pit) <= ? AND r.bwkennung = ? AND t.valtype = ? " +
                 " GROUP BY DATE(r.pit)" +
                 " ORDER BY d DESC");
 
         query.setParameter(1, from.toDate());
         query.setParameter(2, to.toDate());
         query.setParameter(3, resident.getRID());
         query.setParameter(4, type.getValType());
         ArrayList<Date> result = new ArrayList<Date>(query.getResultList());
         em.close();
         return result;
     }
 
     /**
      * Ermittelt den ersten bisher eingetragenen Wert für einen Bewohner.
      *
      * @param bewohner
      * @return
      */
     public static ResValue getFirst(Resident bewohner) {
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("SELECT b FROM ResValue b WHERE b.resident = :bewohner ORDER BY b.pit ");
         query.setParameter("bewohner", bewohner);
         query.setMaxResults(1);
         ResValue p = (ResValue) query.getSingleResult();
         em.close();
         return p;
     }
 
     /**
      * Ermittelt den jeweils zuletzt eingetragenen Wert.
      *
      * @param resident um den es geht
      * @param type     des gesuchten Wertes
      * @return der Wert. <code>null</code>, wenn es keinen gibt.
      */
     public static ResValue getLast(Resident resident, short type) {
 
         ResValue result;
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery(" " +
                 " SELECT b FROM ResValue b WHERE b.resident = :bewohner AND b.replacedBy IS NULL AND b.vtype.valType = :type " +
                 " ORDER BY b.pit DESC ");
         query.setMaxResults(1);
         query.setParameter("bewohner", resident);
         query.setParameter("type", type);
 
         try {
             result = (ResValue) query.getSingleResult();
         } catch (Exception e) {
             result = null;
         } finally {
             em.close();
         }
 
         return result;
     }
 
     public static String getInfoAsHTML(ResValue resValue) {
         String result = "<div id=\"fonttext\">";
 
         DateFormat df = DateFormat.getDateTimeInstance();
         if (resValue.isDeleted()) {
             result += OPDE.lang.getString("misc.msg.thisentryhasbeendeleted") + " <br/>" + OPDE.lang.getString("misc.msg.atchrono") + " " + df.format(resValue.getEditDate()) + " <br/>" + OPDE.lang.getString("misc.msg.Bywhom") + " " + resValue.getEditedBy().getFullname() + "<br/>";
         }
         if (resValue.isReplacement() && !resValue.isReplaced()) {
             result += OPDE.lang.getString("misc.msg.thisEntryIsAReplacement") + " <br/>" + OPDE.lang.getString("misc.msg.atchrono") + " " + df.format(resValue.getCreateDate()) + "<br/>" + OPDE.lang.getString("misc.msg.originalentry") + ": " + resValue.getReplacementFor().getID() + "<br/>";
         }
         if (resValue.isReplaced()) {
             result += OPDE.lang.getString("misc.msg.thisentryhasbeenedited") + " <br/>" + OPDE.lang.getString("misc.msg.atchrono") + " " + df.format(resValue.getCreateDate()) + "<br/>" + OPDE.lang.getString("misc.msg.Bywhom") + " " + resValue.getEditedBy().getFullname();
             result += "<br/>" + OPDE.lang.getString("misc.msg.replaceentry") + ": " + resValue.getReplacedBy().getID() + "</i><br/>";
         }
         if (!resValue.getText().trim().isEmpty()) {
             result += "<br/>" + SYSConst.html_bold("misc.msg.comment") + ":";
             result += SYSConst.html_paragraph(resValue.getText().trim());
         }
 
         return result + "</div>";
     }
 
     //
 //
 //    public static String getTitle(ResValue param) {
 //        String result = "";
 //        if (param.getType() == RR) {
 //            result += "<b>" + param.getValue1() + "/" + param.getValue2() + " " + UNITS[RR] + " " + VALUES[PULSE] + ": " + param.getValue3() + " " + UNITS[PULSE] + "</b>";
 //        } else if (param.getType() == STOOL || param.getType() == VOMIT) {
 //            result += "<i>" + OPDE.lang.getString("misc.msg.novalue") + "</i>";
 //        } else {
 //            result += "<b>" + param.getValue1() + " " + UNITS[param.getType()] + "</b>";
 //        }
 //        return result;
 //    }
     public static String getTextAsHTML(ResValue wert, boolean colorize) {
         String result = "";
         if (!SYSTools.catchNull(wert.getText()).isEmpty()) {
             String color = "";
             if (colorize) {
                 if (wert.isReplaced() || wert.isDeleted()) {
                     color = SYSConst.html_lightslategrey;
                 } else {
                     color = OPDE.getProps().getProperty(DFNTools.SHIFT_KEY_TEXT[SYSCalendar.whatShiftIs(wert.getPit())] + "_FGBHP");
                 }
             }
             result = "<font " + color + " " + SYSConst.html_arial14 + ">" + "<b>" + OPDE.lang.getString("misc.msg.comment") + ":</b> " + wert.getText() + "</font>";
         }
         return result;
     }
 
 
     public static String getAsHTML(List<ResValue> resValues, ResValueTypes vtype) {
 
         if (resValues.isEmpty()) {
             return SYSConst.html_italic("misc.msg.noentryyet");
         }
 
         String html = "";
 
         html += SYSConst.html_h1(vtype.getText());
         html += SYSConst.html_h2(ResidentTools.getLabelText(resValues.get(0).getResident()));
 
         html += "<table  id=\"fonttext\" border=\"1\" cellspacing=\"0\"><tr>" +
                 "<th style=\"width:20%\">" + OPDE.lang.getString(PnlValues.internalClassID + ".tabheader1") +
                 "</th><th style=\"width:40%\">" + OPDE.lang.getString(PnlValues.internalClassID + ".tabheader2") + "</th>" +
                 "</th><th style=\"width:40%\">" + OPDE.lang.getString(PnlValues.internalClassID + ".tabheader3") + "</th></tr>\n";
 
         for (ResValue resValue : resValues) {
             html += "<tr>";
             html += "<td>";
             html += resValue.isReplaced() ? SYSConst.html_22x22_Eraser + "&nbsp;" : "";
             html += resValue.isReplacement() ? SYSConst.html_22x22_Edited + "&nbsp;" : "";
             html += getPITasHTML(resValue, true, false);
             html += "</td>";
             html += "<td>" + getAsHTML(resValue) + "</td>";
             html += "<td>" + getTextAsHTML(resValue, false) + "</td>";
             html += "</tr>\n";
         }
 
         html += "</table>\n";
 
         return html;
     }
 
     public static String getAsHTML(ResValue rv) {
         String result = "";
 
         result += getInfoAsHTML(rv);
 
         result += getValueAsHTML(rv);
 
 //             result += " (" + VALUES[rv.getType()] + ")";
 
         return "<div = \"fonttext\">" + result + "</div>";
     }
 
     public static boolean hatEinfuhren(Resident bewohner) {
         boolean result = false;
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("SELECT SUM(b.val1) FROM ResValue b WHERE b.val1 > 0 AND b.replacedBy IS NULL AND b.resident = :bewohner AND b.vtype.valType = :type AND b.pit >= :pit ");
         query.setParameter("bewohner", bewohner);
         query.setParameter("type", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("pit", new DateTime().minusWeeks(1).toDateMidnight().toDate());
 
         BigDecimal sumwert = (BigDecimal) query.getSingleResult();
         result = sumwert != null && sumwert.abs().compareTo(BigDecimal.ZERO) > 0;
 
         return result;
     }
 
     public static boolean hatAusfuhren(Resident bewohner) {
         boolean result = false;
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("SELECT SUM(b.val1) FROM ResValue b WHERE b.val1 < 0 AND b.replacedBy IS NULL AND b.resident = :bewohner AND b.vtype.valType = :type AND b.pit >= :pit ");
         query.setParameter("bewohner", bewohner);
         query.setParameter("type", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("pit", new DateTime().minusWeeks(1).toDateMidnight().toDate());
 
         BigDecimal sumwert = (BigDecimal) query.getSingleResult();
         result = sumwert != null && sumwert.abs().compareTo(BigDecimal.ZERO) > 0;
 
         return result;
     }
 
     /**
      * retrieves the PITs of the first and the last entry in the ResValue table.
      *
      * @param resident
      * @return
      */
     public static Pair<DateTime, DateTime> getMinMax(Resident resident, ResValueTypes vtype) {
         Pair<DateTime, DateTime> result = null;
 
         EntityManager em = OPDE.createEM();
         Query queryMin = em.createQuery("SELECT rv FROM ResValue rv WHERE rv.resident = :resident AND rv.vtype = :vtype ORDER BY rv.pit ASC ");
         queryMin.setParameter("resident", resident);
         queryMin.setParameter("vtype", vtype);
         queryMin.setMaxResults(1);
 
         Query queryMax = em.createQuery("SELECT rv FROM ResValue rv WHERE rv.resident = :resident AND rv.vtype = :vtype ORDER BY rv.pit DESC ");
         queryMax.setParameter("resident", resident);
         queryMax.setParameter("vtype", vtype);
         queryMax.setMaxResults(1);
 
         try {
             ArrayList<ResValue> min = new ArrayList<ResValue>(queryMin.getResultList());
             ArrayList<ResValue> max = new ArrayList<ResValue>(queryMax.getResultList());
             if (min.isEmpty()) {
                 result = null;
             } else {
                 result = new Pair<DateTime, DateTime>(new DateTime(min.get(0).getPit()), new DateTime(max.get(0).getPit()));
             }
 
         } catch (Exception e) {
             OPDE.fatal(e);
         }
         return result;
     }
 
     public static ArrayList<ResValue> getResValues(Resident resident, ResValueTypes vtype) {
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.vtype = :vtype" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("vtype", vtype);
         ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         return list;
     }
 
     public static ArrayList<ResValue> getResValues(Resident resident, short type, DateMidnight f, DateMidnight t) {
 
         //        DateTime theYear = new DateTime(year, 1, 1, 0, 0, 0);
         DateTime from = f.toDateTime().secondOfDay().withMinimumValue();
         DateTime to = t.toDateTime().secondOfDay().withMaximumValue();
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.pit >= :from" +
                 " AND rv.pit <= :to" +
                 " AND rv.vtype.valType = :type" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("type", type);
         query.setParameter("from", from.toDate());
         query.setParameter("to", to.toDate());
         ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         return list;
     }
 
     public static ArrayList<ResValue> getResValues(Resident resident, ResValueTypes vtype, LocalDate day) {
 
 //        //        DateTime theYear = new DateTime(year, 1, 1, 0, 0, 0);
 //        DateTime from = day.toDateTime().secondOfDay().withMinimumValue();
 //        DateTime to = day.toDateTime().secondOfDay().withMaximumValue();
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.vtype = :vtype" +
                 " AND rv.pit >= :from" +
                 " AND rv.pit <= :to" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("vtype", vtype);
         query.setParameter("from", day.toDateTimeAtStartOfDay().toDate());
         query.setParameter("to", SYSCalendar.eod(day).toDate());
         ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         return list;
     }
 
     public static ArrayList<ResValue> getResValues(Resident resident, ResValueTypes vtype, int year) {
 
//        DateTime theYear = new DateTime(year, 1, 1, 0, 0, 0);
        DateTime from = SYSCalendar.boy(year);
        DateTime to = SYSCalendar.eoy(year);
 
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.vtype = :vtype" +
                 " AND rv.pit >= :from" +
                 " AND rv.pit <= :to" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("vtype", vtype);
         query.setParameter("from", from.toDate());
         query.setParameter("to", to.toDate());
         ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         return list;
     }
 
     public static String getValueAsHTML(ResValue rv) {
         String result = (rv.isDeleted() || rv.isReplaced() ? "<s>" : "");
 //        NumberFormat dcf = DecimalFormat.getNumberInstance();
 
         if (rv.getType().getValType() == ResValueTypesTools.RR) {
             DecimalFormat dcf1 = new DecimalFormat(rv.getType().getFormat1());
             DecimalFormat dcf2 = new DecimalFormat(rv.getType().getFormat2());
             DecimalFormat dcf3 = new DecimalFormat(rv.getType().getFormat3());
             result += "<b>" + dcf1.format(rv.getVal1()) + "/" + dcf2.format(rv.getVal2()) + " " + rv.getType().getUnit1() + " " + rv.getType().getLabel3() + ": " + dcf3.format(rv.getVal3()) + " " + rv.getType().getUnit3() + "</b>";
         } else if (rv.getType().getValType() == ResValueTypesTools.STOOL || rv.getType().getValType() == ResValueTypesTools.VOMIT || rv.getType().getValType() == ResValueTypesTools.ASPIRATION) {
             result += "<i>" + SYSTools.catchNull(rv.getText(), "--") + "</i>";
         } else {
             DecimalFormat dcf = new DecimalFormat(rv.getType().getFormat1());
             result += "<b>" + dcf.format(rv.getVal1()) + " " + rv.getType().getUnit1() + "</b>";
         }
         result += (rv.isDeleted() || rv.isReplaced() ? "</s>" : "");
         return result;
     }
 
     public static String getLiquidBalance(DateMidnight month, Closure progress) {
         ArrayList<Resident> listResidents = ResidentTools.getAllActive(month);
         Format monthFormmatter = new SimpleDateFormat("MMMM yyyy");
         DateTime from = month.dayOfMonth().withMinimumValue().toDateTime();
         DateTime to = month.dayOfMonth().withMaximumValue().plusDays(1).toDateTime().minusSeconds(1);
 
         int p = -1;
         progress.execute(new Pair<Integer, Integer>(p, listResidents.size()));
 
         StringBuilder html = new StringBuilder(1000);
 
         html.append(SYSConst.html_h1(OPDE.lang.getString(PnlControlling.internalClassID + ".nutrition.liquidbalance")));
         html.append(SYSConst.html_h2(monthFormmatter.format(month.toDate())));
 
         p = 0;
         for (Resident resident : listResidents) {
             p++;
             progress.execute(new Pair<Integer, Integer>(p, listResidents.size()));
             Properties controlling = resident.getControlling();
             if (controlling.containsKey(ResidentTools.KEY_BALANCE) && controlling.getProperty(ResidentTools.KEY_BALANCE).equals("on")) {
                 BigDecimal targetIn = SYSTools.parseBigDecimal(controlling.getProperty(ResidentTools.KEY_TARGETIN));
 
                 html.append(SYSConst.html_h3(ResidentTools.getTextCompact(resident)));
                 html.append(SYSConst.html_div(SYSConst.html_bold(OPDE.lang.getString("misc.msg.targetDrink")) + ": " + targetIn.setScale(2, RoundingMode.HALF_UP).toString() + " ml"));
 
                 HashMap<DateMidnight, Pair<BigDecimal, BigDecimal>> balanceMap = getLiquidBalancePerDay(resident, from.toDateMidnight(), to.toDateMidnight());
 
                 ArrayList<DateMidnight> listDays = new ArrayList<DateMidnight>(balanceMap.keySet());
                 Collections.sort(listDays);
 
                 StringBuilder table = new StringBuilder(1000);
                 table.append(SYSConst.html_table_tr(
                         SYSConst.html_table_th(OPDE.lang.getString("misc.msg.Date")) +
                                 SYSConst.html_table_th(OPDE.lang.getString("misc.msg.ingestion")) +
                                 SYSConst.html_table_th(OPDE.lang.getString("misc.msg.egestion")) +
                                 SYSConst.html_table_th(OPDE.lang.getString("misc.msg.balance"))
                 ));
                 for (DateMidnight day : listDays) {
                     BigDecimal linesum = balanceMap.get(day).getFirst().add(balanceMap.get(day).getSecond());
                     table.append(SYSConst.html_table_tr(
                             SYSConst.html_table_td(DateFormat.getDateInstance().format(day.toDate()), null) +
                                     SYSConst.html_table_td(balanceMap.get(day).getFirst().setScale(2, RoundingMode.HALF_UP).toString(), "right") +
                                     SYSConst.html_table_td(balanceMap.get(day).getSecond().setScale(2, RoundingMode.HALF_UP).toString(), "right") +
                                     SYSConst.html_table_td(linesum.setScale(2, RoundingMode.HALF_UP).toString(), "right")
                     ));
 
                 }
 
                 html.append(SYSConst.html_table(table.toString(), "1"));
             }
         }
 
         return html.toString();
     }
 
     /**
      * creates a combined weight and bmi statistics for the given period in html. bmi is only calculated if there is a height available for the particular resident.
      *
      * @param monthsback number of months from now on. starts always at the beginning of that month.
      * @param progress
      * @return
      */
     public static String getWeightStats(int monthsback, Closure progress) {
         StringBuffer html = new StringBuffer(1000);
         int p = -1;
         progress.execute(new Pair<Integer, Integer>(p, 100));
 
         DateMidnight from = new DateMidnight().minusMonths(monthsback).dayOfMonth().withMinimumValue();
         EntityManager em = OPDE.createEM();
         DateFormat df = DateFormat.getDateInstance();
 
         String jpql = " " +
                 " SELECT rv " +
                 " FROM ResValue rv " +
                 " WHERE rv.vtype.valType = :valType " +
                 " AND rv.resident.adminonly <> 2 " +
                 " AND rv.pit >= :from " +
                 " ORDER BY rv.resident.name, rv.resident.firstname, rv.pit ";
 
 
         Query query = em.createQuery(jpql);
         query.setParameter("valType", ResValueTypesTools.WEIGHT);
         query.setParameter("from", from.toDate());
         ArrayList<ResValue> listVal = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         HashMap<Resident, ArrayList<ResValue>> listData = new HashMap<Resident, ArrayList<ResValue>>();
         for (ResValue val : listVal) {
             if (!listData.containsKey(val.getResident())) {
                 listData.put(val.getResident(), new ArrayList<ResValue>());
             }
             listData.get(val.getResident()).add(val);
         }
 
         ArrayList<Resident> listResidents = new ArrayList<Resident>(listData.keySet());
         Collections.sort(listResidents);
 
         html.append(SYSConst.html_h1(OPDE.lang.getString(PnlControlling.internalClassID + ".nutrition.weightstats")));
         html.append(SYSConst.html_h2(OPDE.lang.getString("misc.msg.analysis") + ": " + df.format(from.toDate()) + " &raquo;&raquo; " + df.format(new Date())));
 
         ResValueTypes heightType = ResValueTypesTools.getType(ResValueTypesTools.HEIGHT);
         ResValueTypes weightType = ResValueTypesTools.getType(ResValueTypesTools.WEIGHT);
         p = 0;
 
         for (Resident resident : listResidents) {
             progress.execute(new Pair<Integer, Integer>(p, listResidents.size()));
             p++;
 
             html.append(SYSConst.html_h3(ResidentTools.getTextCompact(resident)));
 
             ResValue height = getLast(resident, ResValueTypesTools.HEIGHT);
 
             html.append(
                     SYSConst.html_div(
                             heightType.getText() + ": " + (height == null ? OPDE.lang.getString("misc.msg.noentryyet") : height.getVal1().setScale(2, RoundingMode.HALF_UP).toString() + " " + heightType.getUnit1())
                     )
             );
 
 
             BigDecimal prevWeight = null;
             BigDecimal prevBMI = null;
 
             StringBuffer table = new StringBuffer(1000);
 
             table.append(SYSConst.html_table_tr(
                     SYSConst.html_table_th("misc.msg.Date") +
                             SYSConst.html_table_th(weightType.getText()) +
                             SYSConst.html_table_th("misc.msg.comment") +
                             SYSConst.html_table_th("+-") +
                             SYSConst.html_table_th("BMI") +
                             SYSConst.html_table_th("+-")
             ));
 
             for (ResValue weight : listData.get(resident)) {
 
                 BigDecimal bmi = height == null ? null : weight.getVal1().divide(height.getVal1().pow(2), 2, RoundingMode.HALF_UP);
 
                 BigDecimal divWeight = prevWeight == null ? null : weight.getVal1().subtract(prevWeight);
                 BigDecimal divBMI = prevBMI == null ? null : bmi.subtract(prevBMI);
 
                 table.append(SYSConst.html_table_tr(
                         SYSConst.html_table_td(df.format(weight.getPit())) +
                                 SYSConst.html_table_td(weight.getVal1().setScale(2, RoundingMode.HALF_UP).toString(), "right") +
                                 SYSConst.html_table_td(SYSTools.catchNull(weight.getText(), "--"), weight.getText() == null ? "center" : "left") +
                                 SYSConst.html_table_td(SYSTools.catchNull(divWeight, "--"), divWeight == null ? "center" : "right") +
                                 SYSConst.html_table_td(SYSTools.catchNull(bmi, "--"), bmi == null ? "center" : "right") +
                                 SYSConst.html_table_td(SYSTools.catchNull(divBMI, "--"), divBMI == null ? "center" : "right")
                 ));
 
                 prevBMI = bmi;
                 prevWeight = weight.getVal1();
 
             }
 
             // Stats over the whole analysis period
             ResValue firstValue = listData.get(resident).get(0);
             BigDecimal firstBMI = height == null ? null : firstValue.getVal1().divide(height.getVal1().pow(2), 2, RoundingMode.HALF_UP);
             BigDecimal divWeight = prevWeight.subtract(firstValue.getVal1());
             BigDecimal divBMI = firstBMI == null || prevBMI == null ? null : prevBMI.subtract(firstBMI);
 
 
             table.append(SYSConst.html_table_tr(
                     SYSConst.html_table_th("misc.msg.completePeriod") +
                             SYSConst.html_table_th("--", "center") +
                             SYSConst.html_table_th("--", "center") +
                             SYSConst.html_table_th(SYSTools.catchNull(divWeight, "--"), divWeight == null ? "center" : "right") +
                             SYSConst.html_table_th("--", "center") +
                             SYSConst.html_table_th(SYSTools.catchNull(divBMI, "--"), divBMI == null ? "center" : "right")
             ));
 
             html.append(SYSConst.html_table(table.toString(), "1"));
 
         }
 
         return html.toString();
     }
 
 
     public static HashMap<DateMidnight, Pair<BigDecimal, BigDecimal>> getLiquidBalancePerDay(Resident resident, DateMidnight from, DateMidnight to) {
         // First BD is for the influx, second for the outflow
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.replacedBy IS NULL " +
                 " AND rv.editedBy IS NULL " +
                 " AND rv.vtype.valType = :valType" +
                 " AND rv.pit >= :from" +
                 " AND rv.pit <= :to" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("from", from.toDate());
         query.setParameter("to", to.plusDays(1).toDateTime().minusSeconds(1).toDate());
         ArrayList<ResValue> list = null;
         list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         // init with dates. so that there are now "empty" days
         HashMap<DateMidnight, Pair<BigDecimal, BigDecimal>> hm = new HashMap<DateMidnight, Pair<BigDecimal, BigDecimal>>();
         for (DateMidnight day = from; day.compareTo(to) <= 0; day = day.plusDays(1)) {
             hm.put(day, new Pair<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO));
         }
 
         for (ResValue val : list) {
             Pair<BigDecimal, BigDecimal> pair = hm.get(new DateMidnight(val.getPit()));
             BigDecimal ingestion = pair.getFirst();
             BigDecimal egestion = pair.getSecond();
             if (val.getVal1().compareTo(BigDecimal.ZERO) < 0) {
                 egestion = egestion.add(val.getVal1().abs());
             } else {
                 ingestion = ingestion.add(val.getVal1());
             }
             hm.put(new DateMidnight(val.getPit()), new Pair<BigDecimal, BigDecimal>(ingestion, egestion));
         }
 
         return hm;
     }
 
 
     public static BigDecimal getIngestion(Resident resident, LocalDate day) {
         // First BD is for the influx, second for the outflow
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT SUM(rv.val1) FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.replacedBy IS NULL " +
                 " AND rv.editedBy IS NULL " +
                 " AND rv.val1 > 0 " +
                 " AND rv.vtype.valType = :valType " +
                 " AND rv.pit >= :from " +
                 " AND rv.pit <= :to ");
 
         query.setParameter("resident", resident);
         query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("from", day.toDateTimeAtStartOfDay().toDate());
         query.setParameter("to", SYSCalendar.eod(day).toDate());
         BigDecimal sum = (BigDecimal) query.getSingleResult();
         em.close();
 
         return sum == null ? BigDecimal.ZERO : sum;
     }
 
     public static BigDecimal getEgestion(Resident resident, LocalDate day) {
         // First BD is for the influx, second for the outflow
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT SUM(rv.val1) FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.replacedBy IS NULL " +
                 " AND rv.editedBy IS NULL " +
                 " AND rv.val1 < 0 " +
                 " AND rv.vtype.valType = :valType " +
                 " AND rv.pit >= :from " +
                 " AND rv.pit <= :to ");
 
         query.setParameter("resident", resident);
         query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("from", day.toDateTimeAtStartOfDay().toDate());
         query.setParameter("to", SYSCalendar.eod(day).toDate());
         BigDecimal sum = (BigDecimal) query.getSingleResult();
         em.close();
 
         return sum == null ? BigDecimal.ZERO : sum;
     }
 
 //    public static BigDecimal getAvgIn(Resident resident, DateMidnight month) {
 //        DateTime from = month.dayOfMonth().withMinimumValue().toDateTime();
 //        DateTime to = month.dayOfMonth().withMaximumValue().plusDays(1).toDateTime().minusSeconds(1);
 //
 //        // First BD is for the influx, second for the outflow
 //        EntityManager em = OPDE.createEM();
 //        Query query = em.createQuery("" +
 //                " SELECT AVG(rv.val1) FROM ResValue rv " +
 //                " WHERE rv.resident = :resident " +
 //                " AND rv.replacedBy IS NULL " +
 //                " AND rv.vtype.valType = :valType" +
 //                " AND rv.val1 >= 0 " +
 //                " AND rv.pit >= :from" +
 //                " AND rv.pit <= :to" +
 //                " ORDER BY rv.pit DESC ");
 //        query.setParameter("resident", resident);
 //        query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
 //        query.setParameter("from", from.toDate());
 //        query.setParameter("from", to.plusDays(1).toDateTime().minusSeconds(1).toDate());
 //        BigDecimal avg = (BigDecimal) query.getSingleResult();
 //        em.close();
 //
 //        return avg == null ? BigDecimal.ZERO : avg;
 //    }
 
     public static HashMap<LocalDate, BigDecimal> getLiquidIn(Resident resident, LocalDate from) {
         EntityManager em = OPDE.createEM();
         Query query = em.createQuery("" +
                 " SELECT rv FROM ResValue rv " +
                 " WHERE rv.resident = :resident " +
                 " AND rv.replacedBy IS NULL " +
                 " AND rv.vtype.valType = :valType " +
                 " AND rv.val1 > 0 " +
                 " AND rv.pit >= :from" +
                 " ORDER BY rv.pit DESC ");
         query.setParameter("resident", resident);
         query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
         query.setParameter("from", from.toDate());
         ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
         em.close();
 
         HashMap<LocalDate, BigDecimal> hm = new HashMap<LocalDate, BigDecimal>();
         for (LocalDate day = from; day.compareTo(new LocalDate()) <= 0; day = day.plusDays(1)) {
             hm.put(day, BigDecimal.ZERO);
         }
 
         for (ResValue val : list) {
             BigDecimal bd = hm.get(new LocalDate(val.getPit()));
             hm.put(new LocalDate(val.getPit()), bd.add(val.getVal1()));
         }
 
         return hm;
     }
 
 //    public static HashMap<DateMidnight, BigDecimal> getLiquidOut(Resident resident, DateMidnight from) {
 //        EntityManager em = OPDE.createEM();
 //        Query query = em.createQuery("" +
 //                " SELECT rv FROM ResValue rv " +
 //                " WHERE rv.resident = :resident " +
 //                " AND rv.replacedBy IS NULL " +
 //                " AND rv.vtype.valType = :valType " +
 //                " AND rv.val1 < 0 " +
 //                " AND rv.pit >= :from" +
 //                " ORDER BY rv.pit DESC ");
 //        query.setParameter("resident", resident);
 //        query.setParameter("valType", ResValueTypesTools.LIQUIDBALANCE);
 //        query.setParameter("from", from.toDate());
 //        ArrayList<ResValue> list = new ArrayList<ResValue>(query.getResultList());
 //        em.close();
 //
 //        HashMap<DateMidnight, BigDecimal> hm = new HashMap<DateMidnight, BigDecimal>();
 //        for (DateMidnight day = from; day.compareTo(new DateMidnight()) <= 0; day = day.plusDays(1)) {
 //            hm.put(day, BigDecimal.ZERO);
 //        }
 //
 //        for (ResValue val : list) {
 //            BigDecimal bd = hm.get(new DateMidnight(val.getPit()));
 //            hm.put(new DateMidnight(val.getPit()), bd.add(val.getVal1()));
 //        }
 //
 //        return hm;
 //    }
 
 
     /**
      * returns a list of triples of {Resident, ResValue, int} for residents which havent had a stool in the last int days.
      * The ResValue denotes the last stool, if there was any. NULL otherwise.
      *
      * @return
      */
     public static ArrayList<Object[]> getNoStool() {
 
         ArrayList<Object[]> result = new ArrayList<Object[]>();
         Station currentStation = StationTools.getStationForThisHost();
         ArrayList<Resident> listResident = ResidentTools.getAllActive(currentStation.getHome());
 
 //        DateTime now = new DateTime();
 
         for (Resident resident : listResident) {
             Properties controlling = resident.getControlling();
             if (controlling.containsKey(ResidentTools.KEY_STOOLDAYS) && !controlling.getProperty(ResidentTools.KEY_STOOLDAYS).equals("off")) {
                 int days = Integer.parseInt(controlling.getProperty(ResidentTools.KEY_STOOLDAYS));
                 ResValue lastStool = getLast(resident, ResValueTypesTools.STOOL);
 
                 if (lastStool == null || new DateTime(lastStool.getPit()).toLocalDate().isBefore(new LocalDate().minusDays(days))) {
                     result.add(new Object[]{resident, lastStool, days});
                 }
             }
         }
 
         listResident.clear();
         return result;
     }
 
     public static ArrayList<Object[]> getHighLowIn() {
 
         ArrayList<Object[]> result = new ArrayList<Object[]>();
         Station currentStation = StationTools.getStationForThisHost();
         ArrayList<Resident> listResident = ResidentTools.getAllActive(currentStation.getHome());
 
         LocalDate now = new LocalDate();
         try {
             for (Resident resident : listResident) {
 
                 ArrayList<Pair<LocalDate, BigDecimal>> violatingValues = new ArrayList<Pair<LocalDate, BigDecimal>>();
                 Properties controlling = resident.getControlling();
 
                 if (controlling.containsKey(ResidentTools.KEY_LOWIN) && !controlling.getProperty(ResidentTools.KEY_LOWIN).equals("off")) {
                     int days = Integer.parseInt(controlling.getProperty(ResidentTools.KEY_DAYSDRINK));
                     HashMap<LocalDate, BigDecimal> in = getLiquidIn(resident, now.minusDays(days));
                     if (!in.isEmpty()) {
                         for (LocalDate day = now.minusDays(days); day.compareTo(new LocalDate()) <= 0; day = day.plusDays(1)) {
 
                             if (in.get(day).compareTo(new BigDecimal(controlling.getProperty(ResidentTools.KEY_LOWIN))) < 0) {
                                 violatingValues.add(new Pair<LocalDate, BigDecimal>(day, in.get(day)));
                             }
                         }
                     }
                 }
 
                 if (controlling.containsKey(ResidentTools.KEY_HIGHIN) && !controlling.getProperty(ResidentTools.KEY_HIGHIN).equals("off")) {
                     int days = Integer.parseInt(controlling.getProperty(ResidentTools.KEY_DAYSDRINK));
                     HashMap<LocalDate, BigDecimal> in = getLiquidIn(resident, now.minusDays(days));
                     if (!in.isEmpty()) {
                         for (LocalDate day = now.minusDays(days); day.compareTo(new LocalDate()) <= 0; day = day.plusDays(1)) {
 
                             if (in.get(day).compareTo(new BigDecimal(controlling.getProperty(ResidentTools.KEY_HIGHIN))) > 0) {
                                 violatingValues.add(new Pair<LocalDate, BigDecimal>(day, in.get(day)));
                             }
                         }
                     }
                 }
 
                 if (!violatingValues.isEmpty()) {
                     result.add(new Object[]{resident, violatingValues});
                 }
             }
         } catch (Exception e) {
             OPDE.fatal(e);
         }
         listResident.clear();
 
 
         return result;
 
     }
 
     /**
      * @param weight in kg
      * @return Required liquid in ml
      */
     public static BigDecimal getRequiredLiquid(BigDecimal weight) {
         if (weight == null) {
             return null;
         }
         BigDecimal rl = new BigDecimal(1500); // for the first 20 kg of weight
         return rl.add(new BigDecimal(15).multiply(weight.subtract(new BigDecimal(20))));
 
     }
 
     /**
      * Harris-Benedict-Formula
      *
      * @param weight in kg
      * @param height in m
      * @param age    in years
      * @return Basal Metabolic Rate in kcal / 24h
      */
     public static BigDecimal getBasalMetabolicRate(BigDecimal weight, BigDecimal height, int age, int gender) {
 
         if (weight == null || height == null) {
             return null;
         }
 
 
         height = height.multiply(new BigDecimal(100));
 
         BigDecimal bmr;
 
         if (gender == ResidentTools.MALE) {
             bmr = new BigDecimal(66.5).add(new BigDecimal(13.7).multiply(weight)).add(new BigDecimal(5).multiply(height)).subtract(new BigDecimal(6.8).multiply(new BigDecimal(age)));
         } else {
             bmr = new BigDecimal(655).add(new BigDecimal(9.6).multiply(weight)).add(new BigDecimal(1.8).multiply(height)).subtract(new BigDecimal(4.7).multiply(new BigDecimal(age)));
         }
 
         return bmr;
     }
 
 
     /**
      * Ideal Body Weight (Hamwi Method)
      *
      * @param height in m
      * @param gender
      * @return
      */
     public static BigDecimal getIBW(BigDecimal height, int gender) {
 
         if (height == null) {
             return null;
         }
 
         height = height.multiply(new BigDecimal(100));
 
         BigDecimal HEIGHT_BARRIER = new BigDecimal(152.4);
         BigDecimal WEIGHT_PER_CM = gender == ResidentTools.MALE ? new BigDecimal(1.1) : new BigDecimal(0.9);
         BigDecimal BASE_WEIGHT = gender == ResidentTools.MALE ? new BigDecimal(48) : new BigDecimal(45);
 
         BigDecimal heightAboveBarrier = height.subtract(HEIGHT_BARRIER);
         if (heightAboveBarrier.compareTo(BigDecimal.ZERO) < 0) {
             heightAboveBarrier = BigDecimal.ZERO;
         }
 
 
         return BASE_WEIGHT.add(heightAboveBarrier.multiply(WEIGHT_PER_CM));
 
     }
 
     /**
      * Body Mass Index
      *
      * @param weight in kg
      * @param height in meter
      * @return
      */
     public static BigDecimal getBMI(BigDecimal weight, BigDecimal height) {
         BigDecimal bmi = null;
 
         if (weight != null && height != null) {
             bmi = weight.divide(height.pow(2), 2, BigDecimal.ROUND_HALF_UP);
         }
 
         return bmi;
 
     }
 
 }
