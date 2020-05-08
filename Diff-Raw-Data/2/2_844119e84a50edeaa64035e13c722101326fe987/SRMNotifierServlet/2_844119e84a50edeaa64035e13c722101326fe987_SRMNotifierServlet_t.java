 package notifier;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import notifier.parser.SRMCalendarParser;
 import twitter4j.TwitterException;
 
 // コミット履歴
 // 2010/3/13 03:09
 // 2010/3/14 00:19
 // 2010/5/5  02:14
 // 2010/5/5  21:09 更新時間の変更（テスト環境）
 // 2010/5/6  05:11 更新方法の更新（テスト環境）＆twitter4jアップグレード
 // 2010/5/12 07:41 本番環境にシフト
 // 2010/5/27 03:00 ついったークラスを別に保持する．変更通知を行う．(テスト環境)
 // 2010/12/22 06:40 次の年のSRM取得ができてないバグを修正
 // 2010/12/22 06:40 twitter4jを2.1.3から2.1.9にアップグレード
 // 2010/12/22 07:10 本番環境にver3でアップロード
 // 2011/03/18 23:20 通知時間を30分前および15分前を追加＋Arenaの短縮URLを表示
 @SuppressWarnings("serial")
 public class SRMNotifierServlet extends HttpServlet {
 	private static final Logger log = Logger.getLogger(SRMNotifierServlet.class
 			.getName());
 	private static final String hash = "#Topcoder #SRM";
 	private static final SimpleDateFormat format = SRMCalendarParser.getDataFormat();
 	private static final String[] msgs = { "開始24時間前です", "開始12時間前です",
 			"登録を開始しました", "開始1時間前です", "開始30分前です", "開始15分前です", "開始5分前です",
 			"Coding Phase を開始しました", "Coding Phase を終了しました",
 			"Challenge Phase を開始しました", "終了しました" };
 	private static final long[] dates = { -toLong(24, 60), -toLong(12, 60),
 			-toLong(3, 60), -toLong(1, 60), -toLong(1, 30), -toLong(1, 15),
 			-toLong(1, 5), 0, toLong(1, 75), toLong(1, 80), toLong(1, 95) };
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		log.info("場所(Locale):" + Locale.getDefault());
 
 		GregorianCalendar cal = new GregorianCalendar();
 		Date now = cal.getTime();
 		log.info("[" + now + ":" + format.format(now) + "]");
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 
 		try {
 			log.info("更新時間（分）：" + cal.get(Calendar.MINUTE) + "　更新時間（時）："
 					+ cal.get(Calendar.HOUR_OF_DAY));
 			SRM srm = getNearestSRM(pm);
 			log.info("srm :" + srm.toString());
 
 			log.info("compeTime :" + format.format(srm.getCompetisionTime()));
 
 			Date target = new Date(srm.getCompetisionTime().getTime()
 					+ dates[srm.getCount()]);
 			log.info("通知判定 [now:" + format.format(now) + "].after[target:"
 					+ format.format(target) + "]==" + now.after(target));
 			log.info("通知判定 " + now.after(target));
 			// if (now.after(target)) { // version2
 			while (now.after(target)) { // version5
 				// 通知判定
 				if (now.before(new Date(target.getTime() + toLong(1, 4)))) { //
 					String notifyDate = "at " + format.format(target);
 					if (srm.getCount() < 3) {
 						notifyDate = "開始時間: " + format.format(srm.getCompetisionTime());
 					}
 					post(msgs[srm.getCount()], srm, notifyDate);
 				}
 				srm.setCount(srm.getCount() + 1);
 				// SRM終了判定
 				if (srm.getCount() >= dates.length) {
 					SRM nextSrm = getSecondNearestSRM(pm);
 					postNextSRM(nextSrm); // 消すついでに次のSRMの時間も告知
 					pm.deletePersistent(srm);
 					log.info(srm.getName() + "のデータを削除");
 					break;
 				}
 				target = new Date(srm.getCompetisionTime().getTime()
 						+ dates[srm.getCount()]);
 			}
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		} finally {
 			pm.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private SRM getNearestSRM(PersistenceManager pm) {
 		Query query = pm.newQuery(SRM.class);
 		query.setRange(0, 1);
 		query.setOrdering("competisionTime");
 		List<SRM> srms = (List<SRM>) query.execute();
 		SRM srm = srms.get(0);
 		log.info("最近傍SRM取得:" + srm);
 		return srm;
 	}
 
 	// SRMが終わったときに次のSRMを通知するため．３・１２金
 	@SuppressWarnings("unchecked")
 	private SRM getSecondNearestSRM(PersistenceManager pm) {
 		Query query = pm.newQuery(SRM.class);
 		query.setRange(1, 2);
 		query.setOrdering("competisionTime");
 		List<SRM> srms = (List<SRM>) query.execute();
 		SRM srm = srms.get(0);
 		log.info("準最近傍SRM取得:" + srm);
 		return srm;
 	}
 
 	private void post(String msg, SRM srm, String date) throws TwitterException {
 		// Twitter twitter;
 		// SRM 463 終了しました at 2010年03月02日（火） 22時35分 #Topcoder #SRM
 		String status = srm.getName() + " " + msg + " " + date;
 		if (2 <= srm.getCount() && srm.getCount() <= 7) {
 			status += " Arena -> http://bit.ly/gloK93";
 		}
 		status += " " + hash;
 		TwitterManager.post(status);
 	}
 
 	// 次のSRMをpostするため
 	private void postNextSRM(SRM srm) throws TwitterException {
 		// Twitter twitter;
 		// 次の SRM000 は 20XX年XX月XX日（Ｘ） XX時XX分 からです #Topcoder #SRM
 		String status = "次の " + srm.getName() + " は "
 				+ format.format(srm.getCompetisionTime()) + " からです " + hash;
 		TwitterManager.post(status);
 	}
 
 	private static long toLong(int hour, int minute) {
 		return hour * minute * 60 * 1000;
 	}
 
 }
