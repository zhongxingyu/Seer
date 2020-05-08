 package modules;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static org.jibble.pircbot.Colors.*;
 
 import static panacea.Panacea.*;
 
 /**
  * Discordianism
  *
  * @author Michael Mrozek
  *         Created on the 51st day of Confusion, 3178 YOLD
  */
 public class Discordianism extends NoiseModule {
 	private static String[] SEASONS = {"Chaos", "Discord", "Confusion", "Bureaucracy", "the Aftermath"};
 	private static String[] DOTW = {"Sweetmorn", "Boomtime", "Pungenday", "Prickle-Prickle", "Setting Orange"};
 	private static String LEAP_DOTW = "St. Tib's Day";
 	private static String[] HOLYDAYS = {"Mungday", "Chaoflux", "Mojoday", "Discoflux", "Syaday", "Confuflux", "Zaraday", "Bureflux", "Maladay", "Afflux"};
 
 	@Command("\\.ddate")
 	public void ddate(Message message) {
 		final GregorianCalendar now = new GregorianCalendar();
 		int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
 
 		String dotw = null;
 		if(now.isLeapYear(now.get(Calendar.YEAR))) {
 			if(dayOfYear == 60) {
 				dotw = LEAP_DOTW;
 			} else if(dayOfYear > 59) {
 				dayOfYear--;
 			}
 		}
 
 		if(dotw == null) {
 			dotw = DOTW[(dayOfYear - 1) % DOTW.length];
 		}
 
		final int seasonIndex = dayOfYear / (365 / SEASONS.length);
 		final String season = SEASONS[seasonIndex];
		final int dayOfSeason = (dayOfYear % (365 / SEASONS.length));
 		final int year = now.get(Calendar.YEAR) + 1166;
 
 		// It's sad that this is the most complicated part
 		final String daySuffix;
 		if(dayOfSeason >= 10 && dayOfSeason < 20) {
 			daySuffix = "th";
 		} else {
 			switch(dayOfSeason % 10) {
 			case 1:
 				daySuffix = "st";
 				break;
 			case 2:
 				daySuffix = "nd";
 				break;
 			case 3:
 				daySuffix = "rd";
 				break;
 			default:
 				daySuffix = "th";
 				break;
 			}
 		}
 
 		String holyday = "";
 		if(dayOfSeason == 5 || dayOfSeason == 50) {
 			holyday = HOLYDAYS[seasonIndex * 2 + (dayOfSeason == 50 ? 1 : 0)] + ", ";
 		}
 
 		this.bot.sendMessage(String.format("Today is %s%s, the %d%s day of %s in the YOLD %d", holyday, dotw, dayOfSeason, daySuffix, season, year));
 	}
 
 	@Override public String getFriendlyName() {return "Discordianism";}
 	@Override public String getDescription() {return "Displays the current Discordian day";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".ddate"
 		};
 	}
 }
