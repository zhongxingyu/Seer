 package eu.trentorise.opendata.nlprise.identifiers.parser;
 
 import org.parboiled.BaseParser;
 import org.parboiled.Rule;
 
 /**
  * Parboiled parser for some Italian date and time format
  * @author Alberto Zanella <a.zanella@trentorise.eu>
  * Last modified by azanella On 08/lug/2013
  */
 public class ItalianDateParser extends BaseParser<Object> {
 
 	public Rule DateTimeParser() {
 		return Sequence(DateParser(), Optional(FirstOf(Ch(','), Ch(';'))),
 				FirstOf(Sequence(Ch(' '), Time()), EOI));
 	}
 
 	Rule DateParser() {
 		return FirstOf(ExtendedDate(), ExtendedDayAndNumber(), BasicSlashDate(), BasicDashDate(), LongDashDate(),
 				MonthandYearOnly(), MonthDashYear(), CkanDateAndTime());
 	}
 
 	// DD/MM/YYYY or DD/MM/YY
 	Rule BasicSlashDate() {
 		return Sequence(DayOfMonth(), Slash(), MonthInNum(), Slash(), Year());
 	}
 
 	// Extended italian rep. [dayinwords[,]] dd
 	Rule ExtendedDayAndNumber() {
 		return Sequence(
 				OneDayinWords(), Optional(Ch(',')), Ch(' '),
 				DayOfMonth());
 	}
 
 	// Extended italian rep. [dayinwords[,]] dd monthinwords[,] [yyyy]
 	Rule ExtendedDate() {
 		return Sequence(
 				Optional(OneDayinWords(), Optional(Ch(',')), Ch(' ')),
 				DayOfMonth(),
 				Ch(' '),
 				OneMonthInWords(),
 				FirstOf(TestTime(),Sequence(Optional(Ch(',')), Ch(' '), Year())),EOI);
 	}
 
 	Rule TestTime() {
 		return Test(Sequence(Optional(FirstOf(Ch(','), Ch(';'))), Ch(' ')),
 				Time());
 	}
 
 	// Dash separated date dd-mm-yy[yy]
 	Rule BasicDashDate() {
 		return Sequence(DayOfMonth(), Dash(), MonthInNum(), Dash(), Year());
 	}
 	
 	Rule LongDashDate() {
 		return Sequence(DayOfMonth(), Dash(), OneMonthInWords(), Dash(), Year());
 	}
 
 	// Month and year only monthinwords yy or yyyy
 	Rule MonthandYearOnly() {
 		return Sequence(OneMonthInWords(), Optional(Ch(',')), Ch(' '), Year());
 	}
 
 	// Monthinwords-yy giugno-99 gu-99
 	Rule MonthDashYear() {
 		return Sequence(OneMonthInWords(), Dash(), Year());
 	}
 
 	// Specific CKAN Date Format "YYYY-MM-DDTHH:MM:SS.MMMMM"
 	Rule CkanDateAndTime() {
 		return Sequence(CharRange('1', '2'), Digit(), Digit(), Digit(), Dash(),
 				MonthInNum(), Dash(), DayOfMonth(), Ch('T'), Digit(), Digit(),
 				Ch(':'), Digit(), Digit(), Ch(':'), Digit(), Digit(), Ch('.'),
 				Digit(), Digit(), Digit(), Digit(), Digit(), Digit());
 	}
 
 	Rule Dash() {
 		return Sequence(Optional(Ch(' ')), Ch('-'), Optional(Ch(' ')));
 	}
 
 	Rule Slash() {
 		return Sequence(Optional(Ch(' ')), Ch('/'), Optional(Ch(' ')));
 	}
 
 	Rule OneDayinWords() {
 		return FirstOf(
 				DoubleNamesRule(GiorniSettimana.LUNEDI.getAbbr(),
 						GiorniSettimana.LUNEDI.getComplete()),
 				DoubleNamesRule(GiorniSettimana.MARTEDI.getAbbr(),
 						GiorniSettimana.MARTEDI.getComplete()),
 				DoubleNamesRule(GiorniSettimana.MERCOLEDI.getAbbr(),
 						GiorniSettimana.MERCOLEDI.getComplete()),
 				DoubleNamesRule(GiorniSettimana.GIOVEDI.getAbbr(),
 						GiorniSettimana.GIOVEDI.getComplete()),
 				DoubleNamesRule(GiorniSettimana.VENERDI.getAbbr(),
 						GiorniSettimana.VENERDI.getComplete()),
 				DoubleNamesRule(GiorniSettimana.SABATO.getAbbr(),
 						GiorniSettimana.SABATO.getComplete()),
 				DoubleNamesRule(GiorniSettimana.DOMENICA.getAbbr(),
 						GiorniSettimana.DOMENICA.getComplete()));
 	}
 
 	Rule OneMonthInWords() {
 		return FirstOf(
 				DoubleNamesRule(MesiAnno.GENNAIO.getAbbr(),
 						MesiAnno.GENNAIO.getComplete()),
 				DoubleNamesRule(MesiAnno.FEBBRAIO.getAbbr(),
 						MesiAnno.FEBBRAIO.getComplete()),
 				DoubleNamesRule(MesiAnno.MARZO.getAbbr(),
 						MesiAnno.MARZO.getComplete()),
 				DoubleNamesRule(MesiAnno.APRILE.getAbbr(),
 						MesiAnno.APRILE.getComplete()),
 				DoubleNamesRule(MesiAnno.MAGGIO.getAbbr(),
 						MesiAnno.MAGGIO.getComplete()),
 				DoubleNamesRule(MesiAnno.GIUGNO.getAbbr(),
 						MesiAnno.GIUGNO.getComplete()),
 				DoubleNamesRule(MesiAnno.LUGLIO.getAbbr(),
 						MesiAnno.LUGLIO.getComplete()),
 				DoubleNamesRule(MesiAnno.AGOSTO.getAbbr(),
 						MesiAnno.AGOSTO.getComplete()),
 				DoubleNamesRule(MesiAnno.SETTEMBRE.getAbbr(),
 						MesiAnno.SETTEMBRE.getComplete()),
 				DoubleNamesRule(MesiAnno.OTTOBRE.getAbbr(),
 						MesiAnno.OTTOBRE.getComplete()),
 				DoubleNamesRule(MesiAnno.NOVEMBRE.getAbbr(),
 						MesiAnno.NOVEMBRE.getComplete()),
 				DoubleNamesRule(MesiAnno.DICEMBRE.getAbbr(),
 						MesiAnno.DICEMBRE.getComplete()));
 	}
 
 	// The extended year validates only 1### or 2###
 	Rule Year() {
 		return FirstOf(
 				Sequence(CharRange('1', '2'), Digit(), Digit(), Digit()),
 				Sequence(Optional(FirstOf(Ch('.'), Ch('\''))), Digit(), Digit()));
 	}
 
 	Rule DayOfMonth() {
 		return Sequence(Sequence(Digit(), Optional(Digit())),
 				isLimited(matchOrDefault("0"), 31));
 	}
 
 	Rule MonthInNum() {
 		return Sequence(Sequence(Digit(), Optional(Digit())),
 				isLimited(matchOrDefault("0"), 12));
 	}
 
 	Rule Digit() {
 		return CharRange('0', '9');
 	}
 
 	boolean isLimited(String s, int limit) {
 		if (Integer.parseInt(s) <= limit) {
			// push(new Integer(s));
 			return true;
 		}
 		return false;
 	}
 
 	Rule DoubleNamesRule(String abbr, String complete) {
 		return FirstOf(IgnoreCase(complete),
 				Sequence(IgnoreCase(abbr), Optional(Ch('.'))));
 	}
 
 	/**
 	 * Enumerates all italian months with conventional abbreviation
 	 * 
 	 * @author Alberto Zanella <a.zanella@trentorise.eu> Last modified by azanella On Jul 3, 2013
 	 */
 	private enum MesiAnno {
 
 		GENNAIO("gennaio", "gen"), FEBBRAIO("febbraio", "feb"), MARZO("marzo",
 				"mar"), APRILE("aprile", "apr"), MAGGIO("maggio", "mag"), GIUGNO(
 				"giugno", "giu"), LUGLIO("luglio", "lug"), AGOSTO("agosto",
 				"ago"), SETTEMBRE("settembre", "set"), OTTOBRE("ottobre", "ott"), NOVEMBRE(
 				"novembre", "nov"), DICEMBRE("dicembre", "dic");
 
 		private String abbr;
 		private String complete;
 
 		private MesiAnno(String complete, String abbr) {
 			this.complete = complete;
 			this.abbr = abbr;
 		}
 
 		public String getAbbr() {
 			return abbr;
 		}
 
 		public String getComplete() {
 			return complete;
 		}
 	}
 
 	/**
 	 * Enumerates all italian days of week with conventional abbreviations
 	 * 
 	 * @author Alberto Zanella <a.zanella@trentorise.eu> Last modified by azanella On Jul 3, 2013
 	 */
 	public enum GiorniSettimana {
 
 		LUNEDI("luned�", "lun"), MARTEDI("marted�", "mar"), MERCOLEDI(
 				"mercoled�", "mer"), GIOVEDI("gioved�", "gio"), VENERDI(
 				"venerd�", "venerd�"), SABATO("sabato", "sab"), DOMENICA(
 				"domenica", "dom");
 
 		private String abbr;
 		private String complete;
 
 		private GiorniSettimana(String complete, String abbr) {
 			this.complete = complete;
 			this.abbr = abbr;
 		}
 
 		public String getAbbr() {
 			return abbr;
 		}
 
 		public String getComplete() {
 			return complete;
 		}
 	}
 
 	// Time parsing
 	Rule Time() {
 		return FirstOf(Time_Col_HH_MM_SS(), Time_Dot_HH_MM_SS());
 	}
 
 	// h(h)?:mm(:ss)?
 	Rule Time_Col_HH_MM_SS() {
 		return Sequence(OneOrTwoDigits(), ':', TwoDigits(),
 				FirstOf(Sequence(':', TwoDigits()), push(0)), EOI);
 	}
 
 	// h(h)?.mm(.ss)?
 	Rule Time_Dot_HH_MM_SS() {
 		return Sequence(OneOrTwoDigits(), '.', TwoDigits(),
 				FirstOf(Sequence('.', TwoDigits()), push(0)), EOI);
 	}
 
 	Rule OneOrTwoDigits() {
 		return FirstOf(TwoDigits(), OneDigit());
 	}
 
 	Rule OneDigit() {
 		return Sequence(Digit(), push(Integer.parseInt(matchOrDefault("0"))));
 	}
 
 	Rule TwoDigits() {
 		return Sequence(Sequence(Digit(), Digit()),
 				push(Integer.parseInt(matchOrDefault("0"))));
 	}
 
 }
