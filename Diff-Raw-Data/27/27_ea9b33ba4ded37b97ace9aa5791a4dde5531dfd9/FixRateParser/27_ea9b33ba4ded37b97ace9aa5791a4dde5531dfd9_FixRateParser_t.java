 package hk.hku.cs.c7802.driver;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import hk.hku.cs.c7802.base.cash.CashFlow;
 import hk.hku.cs.c7802.base.conv.DayBase;
 import hk.hku.cs.c7802.base.time.TimePoint;
 import hk.hku.cs.c7802.base.time.TimeSpan;
 import hk.hku.cs.c7802.inst.CashInstrument;
 import hk.hku.cs.c7802.inst.CashInstrument.CashInstrumentBuilder;
 import hk.hku.cs.c7802.inst.FRAInstrument;
 import hk.hku.cs.c7802.inst.FRAInstrument.FRABuilder;
 import hk.hku.cs.c7802.inst.InterestRateInstrument;
 import hk.hku.cs.c7802.inst.InterestRateInstrument.InterestRateInstrumentBuilder;
 import hk.hku.cs.c7802.inst.SwapInstrument;
 import hk.hku.cs.c7802.inst.SwapInstrument.SwapBuilder;
 import hk.hku.cs.c7802.market.MarketData;
 import hk.hku.cs.c7802.rate.CompoundRate;
 
 public class FixRateParser {
 	private static final TimeSpan DEFAULT_SWAP_COUPON_INTERVAL = new TimeSpan(0, 3, 0);
 	private TimePoint ref;
 	
 	public FixRateParser(TimePoint ref) {
 		this.ref = ref;
 	}
 
 	private static Pattern re1 = Pattern.compile("(\\d+)([WMY])");
 	private static Pattern re2 = Pattern.compile("(\\d+)x(\\d+)");
 	
 	public static TimeSpan parseTimeSpan(String subType) {
 		if(subType.equals("ON")) {
 			return new TimeSpan(0, 0, 1);
 		}
 		if(subType.equals("TN")) {
			throw new RuntimeException("Should not call parseTimeSpan for TN, treat me as FRA");
 		}
 		Matcher matcher = re1.matcher(subType);
 		if(matcher.matches()) {
 			int len = Integer.parseInt(matcher.group(1));
 			String type = matcher.group(2);
 			if(type.equals("W")) {
 				return new TimeSpan(0, 0, 7 * len);
 			}
 			else if(type.equals("M")) {
 				return new TimeSpan(0, len, 0);
 			}
 			else if(type.equals("Y")) {
 				return new TimeSpan(len, 0, 0);
 			}
 			else {
 				// Impossible getting here, exception that the regular expression re1 is changed.
 				throw new RuntimeException("assertion failed, check the program, this is a bug");
 			}
 		}
 		throw new RuntimeException("Unknown subType " + subType);
 	}
 	
 	public static TimeSpan[] parseTimeSpans(String subType) {
 		Matcher matcher = re2.matcher(subType);
 		if(matcher.matches()) {
 			int len1 = Integer.parseInt(matcher.group(1));
 			int len2 = Integer.parseInt(matcher.group(2));
 			return new TimeSpan[]{new TimeSpan(0, len1, 0), new TimeSpan(0, len2, 0)};
 		}
 		throw new RuntimeException("Unknown subType " + subType);
 	}
 	
 	public InterestRateInstrumentBuilder getInstrumentBuilder(String type, String subType) {
		if(type.equals("CASH") && subType.equals("TN")) {
			FRABuilder ib = FRAInstrument.create();
			ib.effectiveAfter(new TimeSpan(0, 0, 1));
			ib.terminatingAfter(new TimeSpan(0, 0, 2));
			return ib;
		}
		else if(type.equals("CASH")) {
 			CashInstrumentBuilder ib = CashInstrument.create();
 			TimeSpan ts = parseTimeSpan(subType);
 			ib.maturingAfter(ts);
 			return ib;
 		}
 		else if(type.equals("FRA")) {
 			FRABuilder ib = FRAInstrument.create();
 			TimeSpan[] ts = parseTimeSpans(subType);	// 1x4 returns tuple (1, 4)
 			ib.effectiveAfter(ts[0]);
 			ib.terminatingAfter(ts[1]);
 			return ib;
 		}
 		else if(type.equals("SWAP")) {
 			SwapBuilder ib = SwapInstrument.create();
 			TimeSpan ts = parseTimeSpan(subType);
 			ib.matruingAfter(ts);
 			ib.withCouponInterval(DEFAULT_SWAP_COUPON_INTERVAL);
 			return ib;
 		}
 		else {
 			throw new RuntimeException("Unknown type " + type);
 		}
 	}
 	
 	public TimePoint getDefaultTime() {
 		return ref;
 	}
 	
 	public MarketData parse(String type, String subType, double rate) {
 		// Fill cash/fra/swap specified options
 		InterestRateInstrumentBuilder ib = getInstrumentBuilder(type, subType);		
 		
 		// Fill InterestRateInstrument options now:
 		ib.usingRateType(new CompoundRate(DayBase.ACT365, 4))
 			.rate(rate / 100);
 		
 		// Fill Instrument options now:
 		ib.withName(type + " - " + subType)
 			.withTimestamp(getDefaultTime());	// FIXME: use now() !
 			// just ignore valideThru, meaning forever
 			// .withValidthru(??)		
 		
 		InterestRateInstrument i = ib.build();		
 		
 		CashFlow price = null;
		if(type.equals("CASH") && subType.equals("TN")) {
			price = CashFlow.create(0.0);
		}
		else if(type.equals("CASH") || type.equals("SWAP")) {
 			price = CashFlow.create(1.0);
 		}
 		else if(type.equals("FRA")){
 			price = CashFlow.create(0.0);
 		}
 		else {
 			throw new RuntimeException("Unknown type " + type);
 		}
 		
 		return new MarketData(i, price);
 	}
 	
 }
