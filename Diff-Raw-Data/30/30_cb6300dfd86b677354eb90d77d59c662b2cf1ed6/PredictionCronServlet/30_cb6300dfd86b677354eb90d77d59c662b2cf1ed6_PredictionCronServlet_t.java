 package fr.free.chiquichi.server.cron;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import fr.free.chiquichi.server.datastore.PMF;
 import fr.free.chiquichi.server.datastore.model.StockDataDb;
 
 /**
  * Download data from stock
  * 
  * @author sylvain
  * 
  */
 @SuppressWarnings("serial")
 public class PredictionCronServlet extends HttpServlet {
 	private static final Logger _logger = Logger
 			.getLogger(PredictionCronServlet.class.getName());
 
 	@Override
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 
 		Double min = Double.valueOf(1000000);
 		Double max = Double.valueOf(0);
 		;
 
 		StockDataDb oldStock = new StockDataDb();
 		Calendar calend = Calendar.getInstance();
 		calend.set(Calendar.YEAR, 3000);
 		oldStock.setDate(calend.getTime());
 
 		StockDataDb lastStock = new StockDataDb();
 		calend = Calendar.getInstance();
 		calend.set(Calendar.YEAR, 2000);
 		lastStock.setDate(calend.getTime());
 
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(StockDataDb.class);
 
 		query.setFilter("date>= dateParam");
 		query.declareParameters("Date dateParam");
 
 		query.setFilter("date>= dateParam");
 		query.setOrdering("date desc");
 		query.declareParameters("Date dateParam");
 
 		calend = Calendar.getInstance();
 		calend.add(Calendar.HOUR, -1 * 24 * 5);
 		Date date = calend.getTime();
 		query.declareImports("import java.util.Date");
 
		_logger.info("Requete du " + date.toString() + " a aujourd'hui");
 
 		@SuppressWarnings("unchecked")
 		List<StockDataDb> results = (List<StockDataDb>) query.execute(date);
 		Iterator<StockDataDb> iter = results.iterator();
 		while (iter.hasNext()) {
 			StockDataDb stockData = iter.next();
 			if (stockData.getDate().getTime() > lastStock.getDate().getTime()) {
 				lastStock = stockData;
 			}
 
 			if (stockData.getDate().getTime() < oldStock.getDate().getTime()) {
 				oldStock = stockData;
 			}
 
 			if (stockData.getClose() > max) {
 				max = stockData.getClose();
 			}
 
 			if (stockData.getClose() < min) {
 				min = stockData.getClose();
 			}
 
 		}
 
 		if (oldStock.getClose() == min) {
 			sendEmail(oldStock, max, true);
 		}
 
 		if (oldStock.getClose() == max) {
 			sendEmail(oldStock, max, false);
 		}
 
 	}
 
 	@Override
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 	private void sendEmail(StockDataDb stockData, Double max, Boolean buy) {
 
		String emailFrom = "xxxxx@gmail.com";
		String emailAdd = "yyyyyyyyy@gmail.com";

 		if (stockData.getClose() != 0) {
 			Properties props = new Properties();
 			Session session = Session.getDefaultInstance(props, null);
 			DateFormat dateFormatProgress = new SimpleDateFormat("yyyy-MM-dd");
 
 			String title = "Cours du petrole "
 					+ dateFormatProgress.format(Calendar.getInstance()
 							.getTime());
 			String msgBody = "";
 			if (buy) {
				msgBody = "C'est le moment de faire le plein. \n Il y a 5 jours le cours etait a "
 						+ stockData.getClose()
						+ " et il atteint apres "
 						+ max
						+ "";
 			} else {
				msgBody = "IL FAUT SURTOUT PAS FAIRE LE PLEIN. \n Il y a 5 jours le cours etait a "
 						+ stockData.getClose()
						+ " et il atteint apres "
 						+ max
						+ "";
 			}
 
 			try {
				_logger.info("Envoi d'un message" + msgBody);
 				Message msg = new MimeMessage(session);
 
				msg.setFrom(new InternetAddress(emailFrom));
 
 				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
 						emailAdd, "Mr. Developer"));
 				msg.setSubject(title);
 				msg.setText(msgBody);
 				Transport.send(msg);
 
 			} catch (AddressException e) {
 				_logger.severe(e.getMessage());
 			} catch (MessagingException e) {
 				_logger.severe(e.getMessage());
 			} catch (UnsupportedEncodingException e) {
 				_logger.severe(e.getMessage());
 			}
 		}
 	}
 
 }
