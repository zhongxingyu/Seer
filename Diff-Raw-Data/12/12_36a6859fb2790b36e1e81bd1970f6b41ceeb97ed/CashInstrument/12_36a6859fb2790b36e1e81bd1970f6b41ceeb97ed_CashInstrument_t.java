 package hk.hku.cs.c7802.inst;
 
 import hk.hku.cs.c7802.base.cash.CashFlow;
 import hk.hku.cs.c7802.base.cash.CashStream;
 import hk.hku.cs.c7802.base.conv.DateRoller;
 import hk.hku.cs.c7802.base.time.TimePoint;
 import hk.hku.cs.c7802.base.time.TimeSpan;
 
 public class CashInstrument extends InterestRateInstrument {
 
 	private CashInstrument(CashInstrumentBuilder builder) {
 		super(builder);
 		this.maturity = builder.maturity;
 		if (maturity == null)
 			throw new IllegalArgumentException("Missing parameter(s) for Cash Instrument.");
 		buildCashStream();
 	}
 
 	@Override
 	public CashFlow valueWith(InstrumentEvaluator ev) {
 		stream.accept(ev.getCashStreamVisitor());
 		return ev.getValue();
 	}
 
 	@Override
 	public String toString() {
 		return super.toString() + " M" + maturity + ".";
 	}
 	
 	private void buildCashStream() {
 		stream = new CashStream();
 		// suppose we save $1 on the next business day, i.e., the reference day
 		TimePoint ref = DateRoller.NEXT_BUZ_DAY.roll(timestamp);
 
 		// we'll get $1+interest on the pay-out day
 		TimePoint payday = DateRoller.MOD_NEXT_BUZ_DAY.roll(ref.plus(maturity));
		
		// workaround for O/N and T/N
		if (maturity.equals(new TimeSpan(0, 0, 1)) || maturity.equals(new TimeSpan(0, 0, 2)))
			payday = DateRoller.NEXT_BUZ_DAY.roll(ref.plus(maturity));
		
 		stream.add(CashFlow.create(1 / rateType.disFactorAfter(rate, payday.minus(ref))), payday);
 	}
 	
 	private TimeSpan maturity;
 	private CashStream stream;
 	
 	public static class CashInstrumentBuilder extends InterestRateInstrumentBuilder{
 		
 		protected CashInstrumentBuilder(){
 		}
 		
 		public CashInstrumentBuilder maturingAfter(TimeSpan maturity) {
 			this.maturity = maturity;
 			return this;
 		}
 		
 		@Override
 		public CashInstrument build() {
 			return new CashInstrument(this);
 		}
 		
 		private TimeSpan maturity;
 	}
 	
 	public static CashInstrumentBuilder create() {
 		return new CashInstrumentBuilder();
 	}
 }
