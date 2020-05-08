 package notifier;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.jdo.Extent;
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import notifier.parser.SRMCalendarParser;
 
 @SuppressWarnings("serial")
 public class SRMUpdaterServlet extends HttpServlet {
 	private static final Logger log = Logger.getLogger(SRMNotifierServlet.class
 			.getName());
 	private static final int updateScheduleHour = 15;
 	private static final String[] months = { "jan", "feb", "mar", "apr", "may",
 			"jun", "jul", "aug", "sep", "oct", "nov", "dec" };
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		GregorianCalendar cal = new GregorianCalendar();
 		Date now = cal.getTime();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			log.info("[" + now + ":"
 					+ SRMCalendarParser.getDataFormat().format(now) + "]");
 			updateSRM(pm, now, "thisMonth");
 			// 次の月のSRM更新を一日に一回
 				cal.add(Calendar.MONTH, 1);
 				int nextMonth = cal.get(Calendar.MONTH);
 				int year = cal.get(Calendar.YEAR) % 100;
 				updateSRM(pm, now, months[nextMonth] + "_" + year);
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		} finally {
 			pm.close();
 		}
 	}
 
 	private void updateSRM(PersistenceManager pm, Date now, String month) {
 		SRMCalendarParser parser = new SRMCalendarParser(
 				"http://community.topcoder.com/tc?module=Static&d1=calendar&d2="
 						+ month);
 		List<SRM> updates = parser.getSRMs();
 		Extent<SRM> extent = pm.getExtent(SRM.class, true);
 		for (SRM srm : extent) {
 			log.info("格納済みデータ" + srm);
 			// check update
 			for (SRM update : new ArrayList<SRM>(updates)) {
 				if ((srm.getName().equals(update.getName()))
 						&& !srm.equals(update)) {
 					srm.update(update);
 					updates.remove(update);
 					log.info(srm.getName() + " のデータを更新 to " + srm);
 				} else if (srm.equals(update)) {
 					updates.remove(update);
 				}
 			}
 		}
 		for (SRM update : updates) {
 			log.info("追加する？ :" + "[update=" + update + "]" + "[now=" + now
 					+ "] u.isafter(n) " + update.getRegisterTime().after(now));
 			if (update.getRegisterTime().after(now)) {
 				try {
 					pm.makePersistent(update);
 					log.info(update.getName() + " のデータを追加 " + update);
 				} catch (Exception e) {
 					log.warning(e.getMessage());
 				}
 			}
 		}
 	}
 
 }
