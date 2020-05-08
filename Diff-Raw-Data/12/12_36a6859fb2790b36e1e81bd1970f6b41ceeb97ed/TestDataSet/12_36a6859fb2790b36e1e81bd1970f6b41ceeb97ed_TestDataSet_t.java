 package hk.hku.cs.c7802.test.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import hk.hku.cs.c7802.base.cash.CashFlow;
 import hk.hku.cs.c7802.base.conv.DayBase;
 import hk.hku.cs.c7802.base.time.TimePoint;
 import hk.hku.cs.c7802.base.time.TimePointFormatException;
 import hk.hku.cs.c7802.base.time.TimeSpan;
 import hk.hku.cs.c7802.inst.CashInstrument;
 import hk.hku.cs.c7802.inst.FRAInstrument;
 import hk.hku.cs.c7802.inst.SwapInstrument;
 import hk.hku.cs.c7802.market.MarketData;
 import hk.hku.cs.c7802.market.MarketDataPool;
 import hk.hku.cs.c7802.rate.CompoundRate;
 
 public class TestDataSet {
 
 	public TestDataSet() {
 		this(getDefaultTime());		
 	}
 	
 	public TestDataSet(TimePoint time) {
 		this.time = time;
 		cash_on = buildCash("Cash-O/N", new TimeSpan(0, 0, 1), 3.20);
 		cash_tn = buildCash("Cash-T/N", new TimeSpan(0, 0, 2), 3.25);
 		cash_1w = buildCash("Cash-1W", new TimeSpan(0, 0, 7), 3.40);
 		cash_2w = buildCash("Cash-2W", new TimeSpan(0, 0, 14), 3.45);
 		cash_1m = buildCash("Cash-1M", new TimeSpan(0, 1, 0), 3.50);
 		cash_2m = buildCash("Cash-2M", new TimeSpan(0, 2, 0), 3.60);
 		cash_3m = buildCash("Cash-3M", new TimeSpan(0, 3, 0), 3.65);
 		fra_1x4 = buildFRA("FRA-1x4", new TimeSpan(0, 1, 0), new TimeSpan(0, 4, 0), 3.85);
 		fra_2x5 = buildFRA("FRA-2x5", new TimeSpan(0, 2, 0), new TimeSpan(0, 5, 0), 4.00);
 		fra_3x6 = buildFRA("FRA-3x6", new TimeSpan(0, 3, 0), new TimeSpan(0, 6, 0), 4.15);
 		fra_6x9 = buildFRA("FRA-6x9", new TimeSpan(0, 6, 0), new TimeSpan(0, 9, 0), 4.40);
 		swap_1y = buildSwap("Swap-1Y", new TimeSpan(0, 3, 0), new TimeSpan(1, 0, 0), 4.40);
 		swap_2y = buildSwap("Swap-2Y", new TimeSpan(0, 3, 0), new TimeSpan(2, 0, 0), 4.65);
 		swap_3y = buildSwap("Swap-3Y", new TimeSpan(0, 3, 0), new TimeSpan(3, 0, 0), 4.90);
		// pool.debug();
 	}
 	
 	public MarketDataPool pool = new MarketDataPool();
 	
 	// CASH INSTRUMENTS
 	public Collection<CashInstrument> cashes = new ArrayList<CashInstrument>();
 	//	CASH	ON
 	public CashInstrument cash_on;
 	//	CASH	TN
 	public CashInstrument cash_tn;
 	//	CASH	1W
 	public CashInstrument cash_1w;
 	//	CASH	2W
 	public CashInstrument cash_2w;
 	//	CASH	1M
 	public CashInstrument cash_1m;
 	//	CASH	2M
 	public CashInstrument cash_2m;
 	//	CASH	3M
 	public CashInstrument cash_3m;
 	
 	// FRA INSTRUMENTS
 	public Collection<FRAInstrument> fras = new ArrayList<FRAInstrument>();
 	//	FRA	1x4
 	public FRAInstrument fra_1x4;
 	//	FRA	2x5
 	public FRAInstrument fra_2x5;
 	//	FRA	3x6
 	public FRAInstrument fra_3x6;
 	//	FRA	6x9
 	public FRAInstrument fra_6x9;
 	
 	// SWAP INSTRUMENTS
 	public Collection<SwapInstrument> swaps = new ArrayList<SwapInstrument>();
 	//	SWAP	1Y
 	public SwapInstrument swap_1y;
 	//	SWAP	2Y
 	public SwapInstrument swap_2y;
 	//	SWAP	3Y
 	public SwapInstrument swap_3y;
 	
 	private CashInstrument buildCash(String name, TimeSpan span, double rate) {
 		CashInstrument ret = (CashInstrument) CashInstrument.create()
 								.maturingAfter(span)
 								.usingRateType(new CompoundRate(DayBase.ACT365, 4))
 								.rate(rate / 100)
 								.withTimestamp(time)
 								.withName(name)
 								.build();
 		pool.addEntry(new MarketData(ret, CashFlow.create(1.0)));
 		cashes.add(ret);
 		return ret;
 	}
 	
 	private FRAInstrument buildFRA(String name, TimeSpan span1, TimeSpan span2, double rate) {
 		FRAInstrument ret = (FRAInstrument) FRAInstrument.create()
 								.effectiveAfter(span1)
 								.terminatingAfter(span2)
 								.usingRateType(new CompoundRate(DayBase.ACT365, 4))
 								.rate(rate / 100)
 								.withTimestamp(time)
 								.withName(name)
 								.build();
 		pool.addEntry(new MarketData(ret, CashFlow.create(0.0)));
 		fras.add(ret);
 		return ret;
 	}
 	
 	private SwapInstrument buildSwap(String name, TimeSpan intv, TimeSpan span, double rate) {
 		SwapInstrument ret = (SwapInstrument) SwapInstrument.create()
 								.withCouponInterval(intv)
 								.matruingAfter(span)
 								.usingRateType(new CompoundRate(DayBase.ACT365, 4))
 								.rate(rate / 100)
 								.withTimestamp(time)
 								.withName(name)
 								.build();
 		pool.addEntry(new MarketData(ret, CashFlow.create(1.0)));
 		swaps.add(ret);
 		return ret;
 	}
 	
 	
 	private TimePoint time;
 	
 	public static TimePoint getDefaultTime() {
 		try {
 			return TimePoint.parse("2011-04-05 12:00:00 GMT+8");
 		} catch (TimePointFormatException e) {
 			// This should never happen
 			throw new RuntimeException("This is impossible!");
 		}
 	}
 }
