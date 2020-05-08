 package net.sf.okapi.steps.wordcount.categorized.gmx;
 
 import static org.junit.Assert.assertEquals;
 import java.util.logging.Logger;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.AltTranslation;
 import net.sf.okapi.common.annotation.AltTranslationsAnnotation;
 import net.sf.okapi.common.query.MatchType;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.lib.extra.steps.TextUnitLogger;
 import net.sf.okapi.steps.leveraging.LeveragingStep;
 import net.sf.okapi.steps.wordcount.common.BaseCountStep;
 import net.sf.okapi.steps.wordcount.common.BaseCounter;
 import net.sf.okapi.steps.wordcount.common.GMX;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestGMXCounts {
 	
 	private BaseCountStep bcs;
 	private StartDocument sd;
 	private Event sdEvent;
 	private ITextUnit tu;
 	private Event tuEvent;
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	@Before
 	public void startup() {
 		sd = new StartDocument("sd");
 		sd.setLocale(LocaleId.ENGLISH);
 		sdEvent = new Event(EventType.START_DOCUMENT, sd);
 		
 		tu = new TextUnit("tu");
 		tu.setSource(new TextContainer("12:00 is 15 minutes after 11:45. You can check at freetime@example.com 8-) for $300"));
 		tuEvent = new Event(EventType.TEXT_UNIT, tu);
 	}
 
 	@Test
 	public void testGMXAlphanumericOnlyTextUnitWordCountStep () {
 		bcs = new GMXAlphanumericOnlyTextUnitWordCountStep();
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		assertEquals(2, BaseCounter.getCount(tu, GMX.AlphanumericOnlyTextUnitWordCount)); // freetime@example.com, 8-)
 	}
 	
 	@Test
 	public void testGMXExactMatchedWordCountStep () {
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		rparams.setDbDirectory(pathBase + "testtm");
 		tu.setSource(new TextContainer("Elephants cannot fly."));
 		
 		LeveragingStep ls = new LeveragingStep();
 		ls.setSourceLocale(LocaleId.ENGLISH);
 		ls.setTargetLocale(LocaleId.FRENCH);
 		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 		params.setResourceParameters(rparams.toString());
 		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 		params.setThreshold(99);
 		params.setFillTarget(true);
 		ls.handleEvent(sdEvent);
 		ls.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		AltTranslationsAnnotation ata = tu.getTarget(LocaleId.FRENCH).getAnnotation(AltTranslationsAnnotation.class);
 		ata.add(new AltTranslation(LocaleId.ENGLISH, LocaleId.FRENCH, tu.getSource().getFirstContent(), null, null, MatchType.EXACT_UNIQUE_ID, 100, null));
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
		bcs = new GMXExactMatchedWordCountStep();
 		bcs.setSourceLocale(LocaleId.ENGLISH);
 		bcs.setTargetLocale(LocaleId.FRENCH);
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
		assertEquals(3, BaseCounter.getCount(tu, GMX.ExactMatchedWordCount)); //
 	}
 
 	@Test
 	public void testGMXLeveragedMatchedWordCountStep () {
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		rparams.setDbDirectory(pathBase + "testtm");
 		tu.setSource(new TextContainer("Elephants cannot fly."));
 		
 		LeveragingStep ls = new LeveragingStep();
 		ls.setSourceLocale(LocaleId.ENGLISH);
 		ls.setTargetLocale(LocaleId.FRENCH);
 		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 		params.setResourceParameters(rparams.toString());
 		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 		params.setThreshold(99);
 		params.setFillTarget(true);
 		ls.handleEvent(sdEvent);
 		ls.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
 		bcs = new GMXLeveragedMatchedWordCountStep();
 		bcs.setSourceLocale(LocaleId.ENGLISH);
 		bcs.setTargetLocale(LocaleId.FRENCH);
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
 		assertEquals(3, BaseCounter.getCount(tu, GMX.LeveragedMatchedWordCount)); 
 		assertEquals(0, BaseCounter.getCount(tu, GMX.FuzzyMatchedWordCount));
 	}
 	
 	@Test
 	public void testGMXFuzzyMatchWordCountStep () {
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		rparams.setDbDirectory(pathBase + "testtm");
 		tu.setSource(new TextContainer("Elephants cannot fly here."));
 		
 		LeveragingStep ls = new LeveragingStep();
 		ls.setSourceLocale(LocaleId.ENGLISH);
 		ls.setTargetLocale(LocaleId.FRENCH);
 		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 		params.setResourceParameters(rparams.toString());
 		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 		params.setThreshold(50);
 		params.setFillTarget(true);
 		ls.handleEvent(sdEvent);
 		ls.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
 		bcs = new GMXFuzzyMatchWordCountStep();
 		bcs.setSourceLocale(LocaleId.ENGLISH);
 		bcs.setTargetLocale(LocaleId.FRENCH);
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		logger.fine(TextUnitLogger.getTuInfo(tu, LocaleId.ENGLISH));
 		
 		assertEquals(4, BaseCounter.getCount(tu, GMX.FuzzyMatchedWordCount));
 		assertEquals(0, BaseCounter.getCount(tu, GMX.LeveragedMatchedWordCount));
 	}
 	
 	@Test
 	public void testGMXMeasurementOnlyTextUnitWordCountStep () {
 		bcs = new GMXMeasurementOnlyTextUnitWordCountStep();
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		assertEquals(3, BaseCounter.getCount(tu, GMX.MeasurementOnlyTextUnitWordCount)); // 11:45, 12:00, $300
 	}
 	
 	@Test
 	public void testGMXNumericOnlyTextUnitWordCountStep () {
 		bcs = new GMXNumericOnlyTextUnitWordCountStep();
 		bcs.handleEvent(sdEvent);
 		bcs.handleEvent(tuEvent);
 		assertEquals(1, BaseCounter.getCount(tu, GMX.NumericOnlyTextUnitWordCount)); // Only 15 is a stand-alone number
 	}
 	
 	@Test
 	public void testGMXProtectedWordCountStep () {
 		bcs = new GMXProtectedWordCountStep();
 		bcs.handleEvent(sdEvent);		
 		
 		tu.setIsTranslatable(false);
 		bcs.handleEvent(tuEvent);
 		assertEquals(11, BaseCounter.getCount(tu, GMX.ProtectedWordCount)); // freetime@example.com, 8-), $300 are not words	
 		
 		tu.setIsTranslatable(true);
 		bcs.handleEvent(tuEvent);
 		assertEquals(0, BaseCounter.getCount(tu, GMX.ProtectedWordCount)); // 0 - not counted in a translatable TU
 	}
 	
 	@Test
 	public void testGMXRepetitionMatchedWordCountStep () {
 		// Not yet implemented
 	}	
 }
 
