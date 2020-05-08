 package com.barchart.feed.ddf.instrument.provider;
 
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.BASE_CODE_DDF;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.EXCHANGE_DDF;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.GUID;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.LOOKUP;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.PRICE_POINT_VALUE;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.PRICE_TICK_INCREMENT;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.STATUS;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.SYMBOL_CODE_CFI;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.SYMBOL_COMMENT;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.SYMBOL_EXPIRE;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.SYMBOL_REALTIME;
 import static com.barchart.feed.ddf.instrument.provider.XmlTagExtras.TIME_ZONE_DDF;
 import static com.barchart.feed.ddf.util.HelperXML.XML_PASS;
 import static com.barchart.feed.ddf.util.HelperXML.XML_STOP;
 import static com.barchart.feed.ddf.util.HelperXML.xmlByteDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlDecimalDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlStringDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlTimeDecode;
 import static com.barchart.feed.inst.InstrumentField.BOOK_DEPTH;
 import static com.barchart.feed.inst.InstrumentField.BOOK_LIQUIDITY;
 import static com.barchart.feed.inst.InstrumentField.BOOK_STRUCTURE;
 import static com.barchart.feed.inst.InstrumentField.CFI_CODE;
 import static com.barchart.feed.inst.InstrumentField.COMPONENT_LEGS;
 import static com.barchart.feed.inst.InstrumentField.CURRENCY_CODE;
 import static com.barchart.feed.inst.InstrumentField.DESCRIPTION;
 import static com.barchart.feed.inst.InstrumentField.DISPLAY_FRACTION;
 import static com.barchart.feed.inst.InstrumentField.EXCHANGE_CODE;
 import static com.barchart.feed.inst.InstrumentField.LIFETIME;
 import static com.barchart.feed.inst.InstrumentField.MARKET_GUID;
 import static com.barchart.feed.inst.InstrumentField.MARKET_HOURS;
 import static com.barchart.feed.inst.InstrumentField.POINT_VALUE;
 import static com.barchart.feed.inst.InstrumentField.SECURITY_TYPE;
 import static com.barchart.feed.inst.InstrumentField.SYMBOL;
 import static com.barchart.feed.inst.InstrumentField.TICK_SIZE;
 import static com.barchart.feed.inst.InstrumentField.TIME_ZONE_NAME;
 import static com.barchart.feed.inst.InstrumentField.TIME_ZONE_OFFSET;
 import static com.barchart.feed.inst.InstrumentField.VENDOR;
 import static com.barchart.util.values.provider.ValueBuilder.newPrice;
 import static com.barchart.util.values.provider.ValueBuilder.newSize;
 import static com.barchart.util.values.provider.ValueBuilder.newText;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Element;
 import org.xml.sax.Attributes;
 
 import com.barchart.feed.api.data.Instrument;
 import com.barchart.feed.api.enums.BookLiquidityType;
 import com.barchart.feed.api.enums.BookStructureType;
 import com.barchart.feed.api.enums.MarketCurrency;
 import com.barchart.feed.api.enums.SecurityType;
 import com.barchart.feed.api.inst.GuidList;
 import com.barchart.feed.api.inst.InstrumentGUID;
 import com.barchart.feed.ddf.symbol.enums.DDF_Exchange;
 import com.barchart.feed.ddf.symbol.enums.DDF_TimeZone;
 import com.barchart.feed.ddf.util.enums.DDF_Fraction;
 import com.barchart.feed.inst.InstrumentField;
 import com.barchart.missive.api.Tag;
 import com.barchart.missive.core.ObjectMapFactory;
 import com.barchart.proto.buf.inst.BookLiquidity;
 import com.barchart.proto.buf.inst.BookStructure;
 import com.barchart.proto.buf.inst.Calendar;
 import com.barchart.proto.buf.inst.Decimal;
 import com.barchart.proto.buf.inst.InstrumentDefinition;
 import com.barchart.proto.buf.inst.InstrumentType;
 import com.barchart.proto.buf.inst.Interval;
 import com.barchart.util.value.api.Factory;
 import com.barchart.util.value.api.FactoryLoader;
 import com.barchart.util.value.api.Time;
 import com.barchart.util.value.api.TimeInterval;
import com.barchart.util.values.api.Fraction;
 import com.barchart.util.values.api.PriceValue;
 import com.barchart.util.values.api.TextValue;
 import com.barchart.util.values.provider.ValueBuilder;
 import com.barchart.util.values.provider.ValueConst;
 
 public final class InstrumentXML {
 	
 	private static final Factory factory = FactoryLoader.load();
 	
 	@SuppressWarnings("unused")
 	private static final Logger log = LoggerFactory
 			.getLogger(InstrumentXML.class);
 	
 	private InstrumentXML() {
 		
 	}
 	
 	public static Instrument decodeXML(final Element tag) throws Exception {
 		
 		// lookup status
 
 		final String statusCode = xmlStringDecode(tag, STATUS, XML_STOP);
 		final StatusXML status = StatusXML.fromCode(statusCode);
 
 		if (!status.isFound()) {
 			final String lookup = xmlStringDecode(tag, LOOKUP, XML_STOP);
 			throw new SymbolNotFoundException(lookup);
 		}
 
 		// decode DOM
 		TextValue guid;
 		try {
 			guid = ValueBuilder.newText(xmlStringDecode(tag, GUID, XML_STOP));
 		} catch (Exception e) {
 			return Instrument.NULL_INSTRUMENT;
 		}
 		
 		final TextValue symbolReal = ValueBuilder.newText(xmlStringDecode(tag, SYMBOL_REALTIME, XML_STOP));
 		final byte exchCode = xmlByteDecode(tag, EXCHANGE_DDF, XML_PASS); 
 		final byte baseCode = xmlByteDecode(tag, BASE_CODE_DDF, XML_STOP);
 		final String codeCFI = xmlStringDecode(tag, SYMBOL_CODE_CFI, XML_PASS);
 		final String zoneCode = xmlStringDecode(tag, TIME_ZONE_DDF, XML_STOP);
 		final String symbolComment = xmlStringDecode(tag, SYMBOL_COMMENT,
 				XML_PASS);
 		
 		final Time expire = xmlTimeDecode(tag, SYMBOL_EXPIRE, XML_PASS);
 
 		// month code for exp of futures contract
 		final DDF_TimeZone zone = DDF_TimeZone.fromCode(zoneCode);
 		final DDF_Exchange exchange = DDF_Exchange.fromCode(exchCode);
 		final DDF_Fraction frac = DDF_Fraction.fromBaseCode(baseCode);
 		final long priceStepMantissa = xmlDecimalDecode(frac, tag,
 				PRICE_TICK_INCREMENT, XML_STOP);
 		final String pricePointString = xmlStringDecode(tag, PRICE_POINT_VALUE,
 				XML_PASS);
 
 		PriceValue pricePoint = ValueBuilder.newPrice(0);
 		if (pricePointString != null) {
 			try {
 				pricePoint = ValueBuilder.newPrice(Double
 						.valueOf(pricePointString));
 			} catch (Exception e) {
 			}
 
 		}
 
 		final PriceValue priceStep = newPrice(priceStepMantissa,
 				frac.decimalExponent);
 		
 		/* Build Lifetime, currently only have last month/year of instrument from ddf.extras */
 		TimeInterval lifetime; 
 		if(expire == null) { // Was isNull()
 			lifetime = com.barchart.util.value.impl.ValueConst.NULL_TIME_INTERVAL;
 		} else {
 			lifetime = factory.newTimeInterval(0, expire.millisecond());
 		}
 		
 		return build(guid, symbolReal, symbolComment, codeCFI, 
				exchange, priceStep, pricePoint, frac.fraction, lifetime, zone);
 		
 	}
 	
 	public static Instrument decodeSAX(final Attributes ats) throws Exception {
 		
 		// lookup status
 		final String statusCode = xmlStringDecode(ats, STATUS, XML_STOP);
 		final StatusXML status = StatusXML.fromCode(statusCode);
 		if (!status.isFound()) {
 			final String lookup = xmlStringDecode(ats, LOOKUP, XML_STOP);
 			throw new SymbolNotFoundException(lookup);
 		}
 		
 		// decode SAX
 		final TextValue guid = ValueBuilder.newText(xmlStringDecode(ats, GUID, XML_STOP));
 		final TextValue symbolReal = ValueBuilder.newText(
 				xmlStringDecode(ats, SYMBOL_REALTIME, XML_STOP));
 		final byte exchCode = xmlByteDecode(ats, EXCHANGE_DDF, XML_PASS); 
 		final byte baseCode = xmlByteDecode(ats, BASE_CODE_DDF, XML_STOP);
 		final String codeCFI = xmlStringDecode(ats, SYMBOL_CODE_CFI, XML_PASS);
 		final String zoneCode = xmlStringDecode(ats, TIME_ZONE_DDF, XML_STOP);
 		final String symbolComment = xmlStringDecode(ats, SYMBOL_COMMENT,
 				XML_PASS);
 		final Time expire = xmlTimeDecode(ats, SYMBOL_EXPIRE, XML_PASS);
 
 		//
 
 		final DDF_TimeZone zone = DDF_TimeZone.fromCode(zoneCode);
 		final DDF_Exchange exchange = DDF_Exchange.fromCode(exchCode);
 		final DDF_Fraction frac = DDF_Fraction.fromBaseCode(baseCode);
 		final long priceStepMantissa = xmlDecimalDecode(frac, ats,
 				PRICE_TICK_INCREMENT, XML_STOP);
 		final String pricePointString = xmlStringDecode(ats, PRICE_POINT_VALUE,
 				XML_PASS);
 
 		PriceValue pricePoint = ValueBuilder.newPrice(0);
 		if (pricePointString != null) {
 			try {
 				pricePoint = ValueBuilder.newPrice(Double
 						.valueOf(pricePointString));
 			} catch (Exception e) {
 			}
 		}
 
 		final PriceValue priceStep = newPrice(priceStepMantissa,
 				frac.decimalExponent);
 		
 		/* Build Lifetime, currently only have last month/year of instrument from ddf.extras */
 		TimeInterval lifetime; 
 		if(expire == null) { // Was isNull()
 			lifetime = com.barchart.util.value.impl.ValueConst.NULL_TIME_INTERVAL;
 		} else {
 			lifetime = factory.newTimeInterval(0, expire.millisecond());
 		}
 		
 		return build(guid, symbolReal, symbolComment, codeCFI, 
				exchange, priceStep, pricePoint, frac.fraction, lifetime, zone);
 		
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private static final Instrument build(final TextValue guid,
 			final TextValue symbolReal, final String symbolComment,
 			final String codeCFI, final DDF_Exchange exchange,
 			final PriceValue priceStep, final PriceValue pricePoint,
 			final Fraction fraction, final TimeInterval lifetime,
 			final DDF_TimeZone zone) {
 		
 		final Map<Tag, Object> map = new HashMap<Tag, Object>();
 		
 		map.put(InstrumentField.GUID, new InstrumentGUID(guid));
 		map.put(MARKET_GUID, guid);
 		map.put(SECURITY_TYPE, SecurityType.NULL_TYPE);
 		map.put(BOOK_LIQUIDITY, BookLiquidityType.NONE);
 		map.put(BOOK_STRUCTURE, BookStructureType.NONE);
 		map.put(BOOK_DEPTH, ValueConst.NULL_SIZE);
 		map.put(VENDOR, newText("Barchart"));
 		map.put(SYMBOL, symbolReal);
 		map.put(DESCRIPTION, newText(symbolComment));
 		map.put(CFI_CODE, newText(codeCFI));
 		map.put(CURRENCY_CODE, MarketCurrency.USD);
 		map.put(EXCHANGE_CODE, newText(new byte[]{(exchange.code)}));
 		map.put(TICK_SIZE, priceStep);
 		map.put(POINT_VALUE, pricePoint);
 		map.put(DISPLAY_FRACTION, fraction);
 		map.put(LIFETIME, lifetime);
 		map.put(MARKET_HOURS, factory.newSchedule(new TimeInterval[0]));
 		map.put(TIME_ZONE_OFFSET, newSize(zone.getUTCOffset()));
 		map.put(TIME_ZONE_NAME, newText(zone.name()));
 		map.put(COMPONENT_LEGS, new GuidList());
 
 		return ObjectMapFactory.build(InstrumentDDF.class, map);
 		
 	}
 	
 	public static InstrumentDefinition decodeSAXProto(final Attributes ats) throws Exception {
 
 		InstrumentDefinition.Builder builder = InstrumentDefinition.newBuilder();
 		
 		// lookup status
 		final String statusCode = xmlStringDecode(ats, STATUS, XML_STOP);
 		final StatusXML status = StatusXML.fromCode(statusCode);
 		if (!status.isFound()) {
 			final String lookup = xmlStringDecode(ats, LOOKUP, XML_STOP);
 			throw new SymbolNotFoundException(lookup);
 		}
 		
 		/* market identifier; must be globally unique; */
 		try {
 			builder.setMarketId(Long.parseLong(xmlStringDecode(ats, GUID, XML_STOP)));
 		} catch(Exception e) {
 			return InstrumentDefinition.getDefaultInstance();
 		}
 		/* type of security, Forex, Equity, etc. */
 		builder.setInstrumentType(InstrumentType.NO_TYPE_INST);
 		
 		/* liquidy type, default / implied / combined */
 		builder.setBookLiquidity(BookLiquidity.NO_BOOK_LIQUIDITY);
 		
 		/* structure of book */
 		builder.setBookStructure(BookStructure.NO_BOOK_STRUCTURE);
 		
 		/* book depth */
 		builder.setBookDepth(0);
 		
 		/* vendor */
 		builder.setVendorId("Barchart");
 		
 		/* market symbol; can be non unique; */
 		builder.setSymbol(xmlStringDecode(ats, SYMBOL_REALTIME, XML_STOP));
 		
 		/* market free style description; can be used in full text search */
 		builder.setDescription(String.valueOf(xmlStringDecode(ats, SYMBOL_COMMENT,
 				XML_PASS)));
 		
 		/* stock vs future vs etc. */
 		builder.setCfiCode(xmlStringDecode(ats, SYMBOL_CODE_CFI, XML_PASS));
 		
 		/* price currency */
 		builder.setCurrencyCode("USD");
 		
 		/* market originating exchange identifier */
 		final DDF_Exchange exchange = DDF_Exchange.fromCode( 
 				xmlByteDecode(ats, EXCHANGE_DDF, XML_PASS));
 		builder.setExchangeCode(exchange.name());
 		
 		final DDF_Fraction frac = DDF_Fraction.fromBaseCode(
 				xmlByteDecode(ats, BASE_CODE_DDF, XML_STOP));
 		
 		/* price step / increment size / tick size */
 		final long priceStepMantissa = xmlDecimalDecode(frac, ats,
 				PRICE_TICK_INCREMENT, XML_STOP);
 		builder.setMinimumPriceIncrement(buildDecimal(priceStepMantissa, frac.decimalExponent));
 		
 		/* value of a future contract / stock share */
 		final String pricePointString = xmlStringDecode(ats, PRICE_POINT_VALUE,	XML_PASS);
 		if(pricePointString == null) {
 			builder.setContractPointValue(buildDecimal(0,0));
 		} else {
 			PriceValue pricePoint = ValueBuilder.newPrice(Double
 					.valueOf(pricePointString));
 			builder.setContractPointValue(buildDecimal(
 					pricePoint.mantissa(), pricePoint.exponent()));
 		}
 		
 		/* display fraction base : decimal(10) vs binary(2), etc. */
 		builder.setDisplayBase((int)frac.fraction.base());
 		builder.setDisplayExponent(frac.fraction.exponent());
 		
 		/* Calendar */
 		final Time expire = xmlTimeDecode(ats, SYMBOL_EXPIRE, XML_PASS);
 		final Calendar.Builder calBuilder = Calendar.newBuilder();
 		final Interval.Builder intBuilder = Interval.newBuilder();
 		
 		intBuilder.setTimeStart(0);
 		if(expire == null) {
 			intBuilder.setTimeFinish(0);
 		} else {
 			intBuilder.setTimeFinish(expire.millisecond());
 		}
 		
 		calBuilder.setLifeTime(intBuilder.build());
 		builder.setCalendar(calBuilder.build());
 
 		//
 		final DDF_TimeZone zone = DDF_TimeZone.fromCode(xmlStringDecode(
 				ats, TIME_ZONE_DDF, XML_STOP));
 		
 		/* timezone represented as offset in minutes from utc */
 		builder.setTimeZoneOffset(zone.getUTCOffset());
 		
 		/* time zone name as text */
 		builder.setTimeZoneName(zone.name());
 		
 		return builder.build();
 		
 	}
 	
 	public static Decimal buildDecimal(final long mantissa, final int exponent) {
 		
 		final Decimal.Builder builder = Decimal.newBuilder();
 		builder.setMantissa(mantissa);
 		builder.setExponent(exponent);
 		return builder.build();
 		
 	}
 	
 	//OLD
 //	final String symbolHist = xmlStringDecode(ats, SYMBOL_HIST, XML_STOP);
 //	final String symbolDDFReal = xmlStringDecode(ats, SYMBOL_DDF_REAL, XML_STOP);
 //	final String exchangeComment = xmlStringDecode(ats, EXCHANGE_COMMENT, XML_PASS);
 //	final String ddf_expire_month = xmlStringDecode(ats, SYMBOL_DDF_EXPIRE_MONTH, XML_PASS);
 //	final String ddf_expire_year = xmlStringDecode(ats, SYMBOL_DDF_EXPIRE_YEAR, XML_PASS);
 	
 	/*<instruments status="200" count="1">
 		<instrument lookup="IBM" 
 		status="200" 
 		guid="IBM" 
 		id="1298146" 
 		symbol_realtime="IBM" 
 		symbol_ddf="IBM" 
 		symbol_historical="IBM" 
 		symbol_description="International Business Machines Corp." 
 		symbol_cfi="EXXXXX" 
 		exchange="XNYS" 
 		exchange_channel="NYSE" 
 		exchange_description="New York Stock Exchange" 
 		exchange_ddf="N" 
 		time_zone_ddf="America/New_York" 
 		tick_increment="1" 
 		unit_code="2" 
 		base_code="A" 
 		point_value="1"/>
 	</instruments>*/
 	
 	/*<instruments status="200" count="1">
 		<instrument lookup="ESM3" 
 		status="200" 
 		guid="ESM2013" 
 		id="94112573" 
 		symbol_realtime="ESM2013" 
 		symbol_ddf="ESM3" 
 		symbol_historical="ESM13" 
 		symbol_description="E-Mini S&P 500" 
 		symbol_expire="2013-06-21T23:59:59-05:00" 
 		symbol_ddf_expire_month="M" 
 		symbol_ddf_expire_year="3" 
 		symbol_cfi="FXXXXX" 
 		exchange="XCME" 
 		exchange_channel="GBLX" 
 		exchange_description="CMEGroup CME (Globex Mini)" 
 		exchange_ddf="M" 
 		time_zone_ddf="America/Chicago" 
 		tick_increment="25" 
 		base_code="A" 
 		unit_code="2" 
 		point_value="50"/>
 	</instruments>*/
 	
 	/*<instruments status="200" count="1">
 		<instrument lookup="_S_FX_A6H2_A6Z1" 
 		status="200" 
 		guid="_S_FX_A6H2_A6Z1" 
 		id="1819728" 
 		symbol_realtime="_S_FX_A6H2_A6Z1" 
 		symbol_ddf="_S_FX_A6H2_A6Z1" 
 		symbol_historical="_S_FX_A6H2_A6Z1" 
 		symbol_description="Australian Dollar Futures Foreign Exchange Spread" 
 		symbol_cfi="FMXXXX" 
 		exchange="XIMM" 
 		exchange_channel="IMM" 
 		exchange_description="CMEGroup CME" 
 		exchange_ddf="M" 
 		time_zone_ddf="America/New_York" 
 		tick_increment="1" 
 		unit_code="5" 
 		base_code="D" 
 		point_value="1"/>
 	</instruments>*/
 
 }
