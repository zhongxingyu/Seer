 package org.bytewerk.namnam.parser.erlangennuernberg;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.bytewerk.namnam.model.Essen.MealToken;
 import org.bytewerk.namnam.model.Mensa;
 import org.bytewerk.namnam.model.Mensaessen;
 import org.bytewerk.namnam.model.Tagesmenue;
 import org.bytewerk.namnam.parser.NamNamParser;
 import org.ccil.cowan.tagsoup.Parser;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 /**
  * common functionality for all menues from the erlangen/nuernberg studentenwerk
  * <p/>
  * if you wonder why this parser is subclassed just for the urls, keep in mind
  * that the format of the html pages can change any time and may be inconsistent
  * between the different locations.
  * 
  * @author fake
  * @author Jan Knieling
  */
 public abstract class NamNamParserErlangenNuernbergBase implements NamNamParser {
 
 	private static Logger logger = Logger
 			.getLogger(NamNamParserErlangenNuernbergBase.class.getName());
 
 	protected String theURL; // this is what children fill in
 	protected SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy");
 	protected String priceRegex = "[\\s]*\\d+[,.]\\d\\d[\\s]*€[\\s]*";
 	protected DecimalFormat df = new DecimalFormat();
 	protected DecimalFormatSymbols decf = new DecimalFormatSymbols(
 			Locale.GERMAN);
 
 	protected NamNamParserErlangenNuernbergBase() {
 		this.df.setDecimalFormatSymbols(this.decf);
 	}
 
 	abstract public String getMensaName();
 
 	public Mensa getCurrentMenues() throws Exception {
 		Mensa mensa = new Mensa(this.getMensaName());
 
 		XPathFactory xpathFac = XPathFactory.newInstance();
 		XPath xpath = xpathFac.newXPath();
 		URL url = new URL(theURL);
 		InputStream input = url.openStream();
 
 		XMLReader reader = new Parser();
 		reader.setFeature(Parser.namespacesFeature, false);
 		Transformer transformer = TransformerFactory.newInstance()
 				.newTransformer();
 
 		DOMResult domResult = new DOMResult();
 		transformer.transform(new SAXSource(reader, new InputSource(input)),
 				domResult);
 
 		// There is only two tables on the pages so we just check for the tr's
 		// in first place
 		String QUERY = "//tr";
 		Node rootNode = domResult.getNode();
 		NodeList qResult = (NodeList) xpath.evaluate(QUERY, rootNode,
 				XPathConstants.NODESET);
 
 		if (qResult.getLength() == 0) {
 			throw new NullPointerException(
 					"xpath query returned no nodes! please check xpath query!");
 		}
 
 		Date lastDate = null;
 		for (int n = 0; n < qResult.getLength(); n++) {
 
 			// The structure of the mensa pages contains two tables and the
 			// first with all
 			// meals. This (the meal-table) table contains a lot of tr's where
 			// every tr with a
 			// meal contains 5 td's where each one contains a div with the
 			// real information. So the first check is if the td has got 7
 			// children and if
 			// Which td contains what:
 			// 0: Date or nothing
 			// 1: Number of meal for that date or nothing (between the days)
 			// 2: Token for the meal. At the moment just one later maybe more
 			// 3: Description for the meal
 			// 4: Price for students
 			// 5: Price for employees
 			// 6: Price for guests
 			final NodeList currentTrs = qResult.item(n).getChildNodes();
 			if (currentTrs.getLength() == 7) {
 				final Node menuNumberTd = currentTrs.item(1);
 				final String menuNumber = menuNumberTd.getFirstChild()
 						.getTextContent();
 				if (menuNumber.trim().isEmpty()) {
 					lastDate = null;
 					continue;
 				}
 			} else {
 				lastDate = null;
 				continue;
 			}
 
 			/*
 			 * Get the date. Every day has between two and four meals and
 			 * depending on the number of meals. In the same row as the first
 			 * meal of the day the date is too. The algorithm restarts if a
 			 * break between two days is recognized (look at all those continues
 			 * ;) ). That's why we set the lastDate to null in those cases
 			 */
 			final Date date;
 			if (lastDate != null) {
 				date = lastDate;
 			} else {
 				final Node mayDateTd = currentTrs.item(0);
 				if (mayDateTd.getFirstChild() == null) {
 					continue;
 				}
 				String dateContent = mayDateTd.getFirstChild().getTextContent();
 
 				if (dateContent.isEmpty()) {
 					dateContent = qResult.item(n + 1).getFirstChild()
 							.getFirstChild().getTextContent();
 				}
 				// In one of the mensa pages they had daily
 				// meals and a string in the td where actually the date should
 				// have been (and where we didn't expect the string), so
 				// verify that it's indeed a date e.g. Mo 01.01.
 				if (!dateContent.matches("\\w\\w\\s\\d\\d\\.\\d\\d\\.")) {
 					continue;
 				}
 				date = getDateFromString(dateContent);
 				lastDate = date;
 			}
 
 			// Get prices if it's no day off
 			final Node descTd = currentTrs.item(3);
 			final String desc = descTd.getFirstChild().getTextContent();
 			if (desc.toLowerCase().contains("feiertag")) {
 				lastDate = null;
 				continue;
 			}
 
 			final Node sPriceTd = descTd.getNextSibling();
 			final Node bPriceTd = sPriceTd.getNextSibling();
 			final String sPrice = sPriceTd.getFirstChild().getTextContent();
 			final String bPrice = bPriceTd.getFirstChild().getTextContent();
 			if (!sPrice.matches(priceRegex) || !bPrice.matches(priceRegex)) {
 				lastDate = null;
 				continue;
 			}
 
 			// At least create the menu and start again
 			Tagesmenue daymeal = mensa.getMenuForDate(date);
 			if (daymeal == null) {
 				daymeal = new Tagesmenue(date);
 				mensa.addDayMenue(daymeal);
 			}
 
 			Integer bPriceInCents = null;
 			Integer sPriceInCents = null;
 			try {
 				bPriceInCents = getPriceInCents(bPrice);
 				if (bPriceInCents == null)
 					throw new Exception("return value was null");
 			} catch (Exception ex) {
 				logger.log(Level.SEVERE, this.getMensaName() + ", " + date
 						+ ": converting bed. price '" + bPrice
 						+ "' to cents failed", ex);
 			}
 			try {
 				sPriceInCents = getPriceInCents(sPrice);
 				if (sPriceInCents == null)
 					throw new Exception("return value was null");
 			} catch (Exception ex) {
 				logger.log(Level.SEVERE, this.getMensaName() + ", " + date
 						+ ": converting student price '" + sPrice
 						+ "' to cents failed", ex);
 			}
 
 			if (bPriceInCents == null || sPriceInCents == null) {
 				continue;
 			}
 
 			final Node tokenTr = currentTrs.item(2);
 			MealToken tokenToSet = null;
 			if (tokenTr.getFirstChild().getNodeName().toLowerCase()
 					.equals("img")) {
 				if (tokenTr.getFirstChild().getAttributes().getNamedItem("src").getNodeValue()
 						.equals(MealToken.VEGAN.getTokenValue())) {
 					tokenToSet = MealToken.VEGAN;
 				}else{
 					logger.log(Level.WARNING, "Unknown token detected. Please check mensa-page for new Tokens or changed token-image-paths!");
 				}
 			} else {
 				final String token = tokenTr.getFirstChild().getTextContent()
 						.toUpperCase().trim();
 
 				if (token != null && !token.isEmpty()) {
 					for (MealToken mt : MealToken.values()) {
 						if (mt.getTokenValue().equals(token)) {
 							tokenToSet = mt;
 							break;
 						}
 					}
 				}
 			}
 
 			Mensaessen me = new Mensaessen(desc, bPriceInCents, sPriceInCents,
 					tokenToSet);
 			daymeal.addMenu(me);
 
 		}
 
 		return mensa;
 	}
 
 	protected Integer getPriceInCents(String s) throws Exception {
 		if (s == null || "".equals(s.trim())) {
 			return null;
 		}
 		Pattern pricePattern = Pattern.compile(priceRegex);
 		Matcher m = pricePattern.matcher(s);
 		if (!m.find()) {
 			logger.log(Level.WARNING, this.getMensaName()
 					+ ": Price format did not match regular expression");
 			return null;
 		}
 		String tmp = m.group();
		return new Double(df.parse(tmp.substring(0, tmp.indexOf(',') + 2))
 				.doubleValue() * 100).intValue();
 	}
 
 	protected Date getDateFromString(String d) throws Exception {
 		if (d == null || "".equals(d.trim()) || d.length() < 8)
 			return null;
 
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.");
 		Date ret = sdf.parse(d.substring(3));
 
         /*
             foo logic: set the closest year for the month
 
             so, if it is the 15th of december, and we get 12th of january, we set the next year.
             if it is the 15th of january, and we get the 12th of december, we set the last year.
             if it is the 15th of february, and we get the 12th of march, we set the current year.
          */
 
         Calendar current = Calendar.getInstance();
 
         Calendar menuCalThisYear = Calendar.getInstance();
         menuCalThisYear.setTime(ret);
         menuCalThisYear.set(Calendar.YEAR, current.get(Calendar.YEAR));
 
         Calendar menuCalNextYear = Calendar.getInstance();
         menuCalNextYear.setTime(ret);
         menuCalNextYear.set(Calendar.YEAR, current.get(Calendar.YEAR)+1);
 
         Calendar menuCalLastYear = Calendar.getInstance();
         menuCalLastYear.setTime(ret);
         menuCalLastYear.set(Calendar.YEAR, current.get(Calendar.YEAR)-1);
 
         long diffThis = Math.abs(menuCalThisYear.getTimeInMillis() - current.getTimeInMillis());
         long diffNext = Math.abs(menuCalNextYear.getTimeInMillis() - current.getTimeInMillis());
         long diffLast = Math.abs(menuCalLastYear.getTimeInMillis() - current.getTimeInMillis());
 
         if(diffThis < diffNext && diffThis < diffLast) {
             return menuCalThisYear.getTime();
         } else if (diffNext < diffThis && diffNext < diffLast) {
             return menuCalNextYear.getTime();
         } else if (diffLast < diffThis && diffLast < diffNext) {
             return menuCalLastYear.getTime();
         } else {
             System.err.println("could not determine year for date. returning for 1970.");
             return ret;
         }
 	}
 }
