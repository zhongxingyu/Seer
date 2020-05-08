 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.historical.provider;
 
 import static com.barchart.feed.ddf.historical.provider.CodecHelper.*;
 import static com.barchart.feed.ddf.instrument.enums.DDF_InstrumentField.*;
 
 import org.joda.time.DateTime;
 
 import com.barchart.feed.ddf.historical.api.DDF_EntryBar;
 import com.barchart.feed.ddf.instrument.api.DDF_Instrument;
 import com.barchart.feed.ddf.instrument.enums.DDF_InstrumentField;
 import com.barchart.feed.ddf.message.enums.DDF_TradeDay;
 import com.barchart.util.ascii.ASCII;
 
 // TODO: Auto-generated Javadoc
 abstract class EntryBar extends Entry implements DDF_EntryBar {
 
 	/**
 	 * Instantiates a new entry bar.
 	 *
 	 * @param instrument the instrument
 	 */
 	public EntryBar(final DDF_Instrument instrument) {
 		super(instrument);
 	}
 
 	// ///////////////////////////
 
 	//
 
 	protected long priceOpenMantissa;
 	protected long priceHighMantissa;
 	protected long priceLowMantissa;
 	protected long priceCloseMantissa;
 
 	protected long sizeVolume;
 
 	// ///////////////////////////
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.api.DDF_EntryBar#priceCloseMantissa()
 	 */
 	@Override
 	public long priceCloseMantissa() {
 		return priceCloseMantissa;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.api.DDF_EntryBar#priceHighMantissa()
 	 */
 	@Override
 	public long priceHighMantissa() {
 		return priceHighMantissa;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.api.DDF_EntryBar#priceLowMantissa()
 	 */
 	@Override
 	public long priceLowMantissa() {
 		return priceLowMantissa;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.api.DDF_EntryBar#priceOpenMantissa()
 	 */
 	@Override
 	public long priceOpenMantissa() {
 		return priceOpenMantissa;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.api.DDF_EntryBar#sizeVolume()
 	 */
 	@Override
 	public long sizeVolume() {
 		return sizeVolume;
 	}
 
 	//
 
 	/**
 	 * YYYY­MM­DD HH:MM,TRADING_DAY,
 	 * 
 	 * OPEN,HIGH,LOW,CLOSE,VOLUME.
 	 *
 	 * @param inputLine the input line
 	 */
 	@Override
 	public void decode(final String inputLine) {
 
 		if (inputLine == null) {
 			return;
 		}
 
 		final String[] inputArray = splitCSV(inputLine);
 
		millisUTC = decodeMinsTime(inputArray[0], instrument);
 
 		ordTradeDay = DDF_TradeDay.fromDay(decodeInt(inputArray[1])).ord;
 
 		priceOpenMantissa = decodeMantissa(inputArray[2], priceExponent());
 		priceHighMantissa = decodeMantissa(inputArray[3], priceExponent());
 		priceLowMantissa = decodeMantissa(inputArray[4], priceExponent());
 		priceCloseMantissa = decodeMantissa(inputArray[5], priceExponent());
 
 		sizeVolume = decodeLong(inputArray[6]);
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.provider.Entry#encode()
 	 */
 	@Override
 	public String encode() {
 
 		final StringBuilder text = new StringBuilder(128);
 
 		text.append(encodeMinsTime(millisUTC, instrument));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append((char) getTradeDay().code);
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeMantissa(priceOpenMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 		text.append(encodeMantissa(priceHighMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 		text.append(encodeMantissa(priceLowMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 		text.append(encodeMantissa(priceCloseMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeLong(sizeVolume));
 
 		return text.toString();
 
 	}
 
 	//
 
 	static final String HEADER = "INDEX,SYMBOL,MILLIS_UTC,DATE_TIME_ISO,TRADE_DAY,"
 			+ "PRICE_OPEN,PRICE_HIGH,PRICE_LOW,PRICE_CLOSE," + "SIZE_VOLUME";
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.provider.Entry#csvHeader()
 	 */
 	@Override
 	public String csvHeader() {
 		return HEADER;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.barchart.feed.ddf.historical.provider.Entry#csvEntry()
 	 */
 	@Override
 	public String csvEntry() {
 
 		final StringBuilder text = new StringBuilder(128);
 
 		text.append(index);
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(instrument.get(DDF_SYMBOL_UNIVERSAL));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(millisUTC);
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(new DateTime(millisUTC, instrument
 				.get(DDF_InstrumentField.DDF_ZONE).zone));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(getTradeDay());
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeMantissa(priceOpenMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeMantissa(priceHighMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeMantissa(priceLowMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeMantissa(priceCloseMantissa, priceExponent()));
 		text.append(ASCII.STRING_COMMA);
 
 		text.append(encodeLong(sizeVolume));
 
 		return text.toString();
 
 	}
 }
