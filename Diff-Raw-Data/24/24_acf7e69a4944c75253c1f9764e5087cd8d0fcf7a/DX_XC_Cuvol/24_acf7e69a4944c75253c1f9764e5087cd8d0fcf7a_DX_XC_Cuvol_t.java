 /**
  * Copyright (C) 2011-2012 Barchart, Inc. <http://www.barchart.com/>
  *
  * All rights reserved. Licensed under the OSI BSD License.
  *
  * http://www.opensource.org/licenses/bsd-license.php
  */
 package com.barchart.feed.ddf.message.provider;
 
 import static com.barchart.feed.ddf.message.provider.CodecHelper.DDF_NO_SIZES;
 import static com.barchart.feed.ddf.message.provider.CodecHelper.xmlDecSymbol;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.ENTRY_ARRAY;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.ENTRY_COUNT;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.FRACTION_DDF;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.PRICE_LAST;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.PRICE_TICK_INCREMENT;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.SIZE_LAST;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.SIZE_LAST_CUVOL;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.SYMBOL;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.TAG;
 import static com.barchart.feed.ddf.message.provider.XmlTagCuvol.TIME_LAST;
 import static com.barchart.feed.ddf.util.HelperXML.XML_PASS;
 import static com.barchart.feed.ddf.util.HelperXML.XML_STOP;
 import static com.barchart.feed.ddf.util.HelperXML.xmlAsciiEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlByteDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlByteEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlCheckTagName;
 import static com.barchart.feed.ddf.util.HelperXML.xmlDecimalDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlDecimalEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlIntegerDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlIntegerEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlLongDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlLongEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlStringDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlTextEncode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlTimeDecode;
 import static com.barchart.feed.ddf.util.HelperXML.xmlTimeEncode;
 import static com.barchart.util.ascii.ASCII.ASCII_CHARSET;
 import static com.barchart.util.ascii.ASCII.COMMA;
 import static com.barchart.util.ascii.ASCII.NUL;
 import static com.barchart.util.ascii.ASCII.STRING_COLON;
 
 import java.nio.ByteBuffer;
 
 import org.w3c.dom.Element;
 
 import com.barchart.feed.api.fields.InstrumentField;
 import com.barchart.feed.api.inst.Instrument;
 import com.barchart.feed.base.cuvol.api.MarketDoCuvolEntry;
 import com.barchart.feed.base.provider.DefCuvolEntry;
 import com.barchart.feed.ddf.message.api.DDF_MarketCuvol;
 import com.barchart.feed.ddf.message.api.DDF_MessageVisitor;
 import com.barchart.feed.ddf.message.enums.DDF_MessageType;
 import com.barchart.feed.ddf.symbol.enums.DDF_Exchange;
 import com.barchart.feed.ddf.util.HelperDDF;
 import com.barchart.feed.ddf.util.HelperXML;
 import com.barchart.feed.ddf.util.enums.DDF_Fraction;
 import com.barchart.util.ascii.ASCII;
 import com.barchart.util.math.MathExtra;
 import com.barchart.util.values.api.PriceValue;
 import com.barchart.util.values.api.SizeValue;
 import com.barchart.util.values.provider.ValueBuilder;
 
 // TODO: Auto-generated Javadoc
 /**
  * 
  * 15:08:28.909 [# ddf-messages] DEBUG c.d.f.f.example.LoggingHandler - message
  * : <CV basecode="A" count="150" data=
  * "113350,664:113375,1784:113400,2260:113425,2455:113450,2971:113475,2521:113500,3602:113525,3850:113550,3301:113575,6200:113600,9926:113625,11987:113650,10718:113675,12768:113700,19141:113725,9085:113750,12569:113775,11917:113800,16510:113825,17500:113850,18828:113875,15183:113900,11732:113925,9956:113950,10469:113975,9986:114000,13726:114025,11633:114050,12445:114075,15600:114100,15509:114125,11482:114150,13138:114175,12326:114200,11433:114225,9741:114250,9281:114275,8991:114300,9858:114325,9006:114350,7075:114375,8531:114400,10078:114425,11080:114450,10982:114475,8871:114500,15686:114525,16466:114550,15120:114575,9329:114600,13970:114625,12386:114650,15059:114675,9239:114700,8835:114725,7411:114750,8499:114775,11624:114800,11503:114825,7803:114850,8718:114875,14246:114900,10017:114925,7836:114950,8894:114975,10852:115000,11507:115025,12432:115050,11361:115075,15226:115100,16731:115125,16413:115150,18737:115175,18128:115200,21706:115225,19439:115250,17879:115275,20670:115300,23730:115325,18947:115350,26949:115375,26184:115400,31994:115425,46110:115450,43204:115475,36104:115500,34297:115525,27653:115550,23282:115575,24310:115600,28410:115625,34470:115650,42204:115675,36114:115700,30239:115725,33025:115750,26037:115775,25204:115800,31169:115825,19394:115850,21554:115875,20145:115900,23318:115925,19194:115950,19505:115975,16858:116000,15294:116025,15088:116050,13045:116075,9340:116100,16278:116125,18340:116150,19902:116175,14463:116200,14656:116225,13090:116250,12232:116275,14549:116300,11699:116325,12123:116350,13824:116375,15418:116400,15835:116425,14881:116450,16529:116475,24168:116500,22962:116525,18997:116550,18203:116575,15935:116600,18054:116625,14109:116650,13339:116675,17379:116700,15692:116725,14704:116750,10558:116775,7665:116800,5683:116825,4804:116850,6638:116875,4734:116900,4580:116925,3009:116950,4387:116975,3623:117000,3904:117025,2666:117050,910:117075,4"
  * date="20120129120000" last="115825" lastcvol="37" lastsize="3" symbol="ESZ1"
  * tickincrement="25"/>
  * 
  * */
 
 class DX_XC_Cuvol extends BaseMarket implements DDF_MarketCuvol {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.barchart.feed.ddf.message.provider.Base#accept(com.barchart.feed.
 	 * ddf.message.api.DDF_MessageVisitor, java.lang.Object)
 	 */
 	@Override
 	public <Result, Param> Result accept(
 			final DDF_MessageVisitor<Result, Param> visitor, final Param param) {
 		return visitor.visit(this, param);
 	}
 
 	DX_XC_Cuvol() {
 		super(DDF_MessageType.CUVOL_SNAP_XML);
 	}
 
 	DX_XC_Cuvol(final DDF_MessageType messageType) {
 		super(messageType);
 	}
 
 	// //////////////////////////////////////
 
 	// cuvol descriptor
 	protected long priceFirst = HelperDDF.DDF_EMPTY;
 	protected long priceStep = HelperDDF.DDF_EMPTY;
 	protected long[] sizeArray = DDF_NO_SIZES;
 
 	// last trade
 	protected long priceTrade = HelperDDF.DDF_EMPTY;
 	protected long sizeTrade = HelperDDF.DDF_EMPTY;
 	protected long sizeTradeCuvol = HelperDDF.DDF_EMPTY;
 
 	// //////////////////////////////////////
 
 	/**
 	 * Gets the price first.
 	 * 
 	 * @return the price first
 	 */
 	public final PriceValue getPriceFirst() {
 		return HelperDDF.newPriceDDF(priceFirst, getFraction());
 	}
 
 	protected final int entryCount() {
 		final long[] array = sizeArray;
 		final int length = array.length;
 		int entryCount = 0;
 		for (int index = 0; index < length; index++) {
 			final long sizeValue = array[index];
 			if (sizeValue > 0) {
 				entryCount++;
 			}
 		}
 		return entryCount;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.api.DDF_MarketCuvol#entries()
 	 */
 	@Override
 	public final MarketDoCuvolEntry[] entries() {
 
 		final long[] array = sizeArray;
 		final int length = array.length;
 
 		final int entryCount = entryCount();
 
 		final int exponent = getFraction().decimalExponent;
 
 		final MarketDoCuvolEntry[] entries = new MarketDoCuvolEntry[entryCount];
 		int entryIndex = 0;
 
 		for (int index = 0; index < length; index++) {
 			final long sizeValue = array[index];
 			if (sizeValue > 0) {
 				final int place = entryIndex + 1;
 				final long mantissa = priceFirst + index * priceStep;
 				final PriceValue price = ValueBuilder.newPrice(mantissa,
 						exponent);
 				final SizeValue size = ValueBuilder.newSize(sizeValue);
 				final MarketDoCuvolEntry entry = new DefCuvolEntry(place,
 						price, size);
 				entries[entryIndex++] = entry;
 			}
 		}
 
 		assert entryIndex == entryCount;
 
 		return entries;
 
 	}
 
 	/**
 	 * Entries.
 	 * 
 	 * @param array
 	 *            the array
 	 */
 	public final void entries(final long[] array) {
 		sizeArray = array;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.api.DDF_MarketCuvol#getSizeLastCuvol()
 	 */
 	@Override
 	public SizeValue getSizeLastCuvol() {
 		return HelperDDF.newSizeDDF(sizeTradeCuvol);
 	}
 
 	/**
 	 * Sets the last size cuvol.
 	 * 
 	 * @param size
 	 *            the new last size cuvol
 	 */
 	public void setLastSizeCuvol(final long size) {
 		sizeTradeCuvol = size;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.api.DDF_MarketCuvol#getPriceLast()
 	 */
 	@Override
 	public PriceValue getPriceLast() {
 		return HelperDDF.newPriceDDF(priceTrade, getFraction());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.api.DDF_MarketCuvol#getSizeLast()
 	 */
 	@Override
 	public SizeValue getSizeLast() {
 		return HelperDDF.newSizeDDF(sizeTrade);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.api.DDF_MarketCuvol#getPriceStep()
 	 */
 	@Override
 	public PriceValue getPriceStep() {
 		return HelperDDF.newPriceDDF(priceStep, getFraction());
 	}
 
 	//
 
 	@Override
 	protected final String xmlTagName() {
 		return TAG;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.barchart.feed.ddf.message.provider.Base#decodeXML(org.w3c.dom.Element
 	 * )
 	 */
 	@Override
 	public final void decodeXML(final Element tag) {
 
 		xmlCheckTagName(tag, TAG);
 
 		symbolArray = xmlDecSymbol(tag, SYMBOL, XML_STOP);
 
 		updateSpread();
 
 		final byte baseCode = xmlByteDecode(tag, FRACTION_DDF, XML_STOP);
 		final DDF_Fraction frac = DDF_Fraction.fromBaseCode(baseCode);
 		setFraction(frac);
 
 		priceStep = xmlDecimalDecode(frac, tag, PRICE_TICK_INCREMENT, XML_STOP);
 
 		priceTrade = xmlDecimalDecode(frac, tag, PRICE_LAST, XML_STOP);
 		sizeTrade = xmlLongDecode(tag, SIZE_LAST, XML_STOP);
 		sizeTradeCuvol = xmlLongDecode(tag, SIZE_LAST_CUVOL, XML_STOP);
 
 		//
 
 		final int entryCount = xmlIntegerDecode(tag, ENTRY_COUNT, XML_STOP);
 
 		if (entryCount > 0) {
 
 			final String stringEntries = xmlStringDecode(tag, ENTRY_ARRAY,
 					XML_STOP);
 
 			final String[] stringArray = stringEntries.split(STRING_COLON);
 
 			long priceMin = Long.MAX_VALUE;
 			long priceMax = Long.MIN_VALUE;
 
 			final long[] prices = new long[entryCount];
 			final int[] sizes = new int[entryCount];
 
 			int index = 0;
 
 			for (final String stringEntry : stringArray) {
 				assert ValueBuilder.isPureAscii(stringEntry);
 				final ByteBuffer buffer = ByteBuffer.wrap(stringEntry
 						.getBytes(ASCII_CHARSET));
 				final long price = HelperDDF.decimalDecode(frac, buffer, COMMA);
 				final long size = HelperDDF.longDecode(buffer, NUL);
 				priceMin = Math.min(priceMin, price);
 				priceMax = Math.max(priceMax, price);
 				prices[index] = price;
 				sizes[index] = MathExtra.castLongToInt(size);
 				index++;
 			}
 
 			assert index == entryCount;
 
 			priceFirst = priceMin;
 
 			final long range = priceMax - priceMin + priceStep;
 			final int length = MathExtra.castLongToInt(range / priceStep);
 			sizeArray = new long[length];
 
 			for (index = 0; index < entryCount; index++) {
 				final long price = prices[index];
 				final int size = sizes[index];
 				final int offset = (int) ((price - priceFirst) / priceStep);
 				sizeArray[offset] = size;
 			}
 
 			assert index == entryCount;
 
 		}
 
 		final Instrument instrument = getInstrument();
		setExchange(DDF_Exchange.fromCode(instrument.get(
				InstrumentField.EXCHANGE_CODE).toString().getBytes()[0]));
 
 		final long millisUTC = xmlTimeDecode(getExchange().kind.time.zone, tag,
 				TIME_LAST, XML_PASS);
 		setDecodeDefaults(millisUTC);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * com.barchart.feed.ddf.message.provider.Base#encodeXML(org.w3c.dom.Element
 	 * )
 	 */
 	@Override
 	public final void encodeXML(final Element tag) {
 
 		xmlCheckTagName(tag, TAG);
 
 		xmlAsciiEncode(getSymbolFull(), tag, SYMBOL);
 
 		final DDF_Fraction frac = getFraction();
 		xmlByteEncode(frac.baseCode, tag, FRACTION_DDF);
 
 		xmlDecimalEncode(priceStep, frac, tag, PRICE_TICK_INCREMENT);
 
 		xmlDecimalEncode(priceTrade, frac, tag, PRICE_LAST);
 		xmlLongEncode(sizeTrade, tag, SIZE_LAST);
 		xmlLongEncode(sizeTradeCuvol, tag, SIZE_LAST_CUVOL);
 
 		//
 
 		final int entryCount = entryCount();
 		xmlIntegerEncode(entryCount, tag, ENTRY_COUNT);
 
 		final StringBuilder text = new StringBuilder(512);
 
 		final ByteBuffer buffer = ByteBuffer.allocate(32);
 
 		if (entryCount > 0) {
 
 			final long[] array = sizeArray;
 			final int length = array.length;
 
 			int entryIndex = 0;
 
 			for (int index = 0; index < length; index++) {
 				final long size = array[index];
 				if (size > 0) {
 					if (entryIndex > 0) {
 						text.append(STRING_COLON);
 					}
 					final long price = priceFirst + index * priceStep;
 					buffer.clear();
 					HelperDDF.decimalEncode(price, frac, buffer, COMMA);
 					HelperDDF.longEncode(size, buffer, NUL);
 					final String stringEntry = new String(buffer.array(), 0,
 							buffer.position(), ASCII_CHARSET);
 					text.append(stringEntry);
 					entryIndex++;
 				}
 			}
 
 			assert entryCount == entryIndex;
 
 		}
 
 		xmlTextEncode(text, tag, ENTRY_ARRAY);
 
 		// see decodeXML() above
 
 		xmlTimeEncode(millisUTC, getExchange().kind.time.zone, tag, TIME_LAST);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.barchart.feed.ddf.message.provider.Base#toString()
 	 */
 	@Override
 	public String toString() {
 		final Element tag = HelperXML.xmlNewDocument(xmlTagName());
 		encodeXML(tag);
 		final byte[] array = HelperXML.xmlDocumentEncode(tag, true);
 		return new String(array, ASCII.ASCII_CHARSET);
 	}
 
 	@Override
 	protected void appedFields(final StringBuilder text) {
 
 		super.appedFields(text);
 
 		// TODO
 		text.append("TODO : ");
 
 	}
 
 }
