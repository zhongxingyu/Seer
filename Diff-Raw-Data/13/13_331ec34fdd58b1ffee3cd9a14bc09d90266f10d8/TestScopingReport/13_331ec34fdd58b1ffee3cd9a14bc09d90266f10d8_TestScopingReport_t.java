 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.steps.scopingreport;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.FileCompare;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.filters.html.HtmlFilter;
 import net.sf.okapi.filters.plaintext.PlainTextFilter;
 import net.sf.okapi.lib.extra.pipelinebuilder.XBatch;
 import net.sf.okapi.lib.extra.pipelinebuilder.XBatchItem;
 import net.sf.okapi.lib.extra.pipelinebuilder.XParameter;
 import net.sf.okapi.lib.extra.pipelinebuilder.XPipeline;
 import net.sf.okapi.lib.extra.pipelinebuilder.XPipelineStep;
 import net.sf.okapi.lib.extra.steps.EventLogger;
 import net.sf.okapi.lib.extra.steps.TextUnitLogger;
 import net.sf.okapi.steps.common.RawDocumentToFilterEventsStep;
 import net.sf.okapi.steps.leveraging.LeveragingStep;
 import net.sf.okapi.steps.repetitionanalysis.RepetitionAnalysisStep;
 import net.sf.okapi.steps.segmentation.SegmentationStep;
 import net.sf.okapi.steps.wordcount.SimpleWordCountStep;
 import net.sf.okapi.steps.wordcount.WordCountStep;
 import net.sf.okapi.steps.wordcount.categorized.okapi.ExactLocalContextMatchWordCountStep;
 import net.sf.okapi.steps.wordcount.common.GMX;
 import net.sf.okapi.steps.wordcount.common.Metrics;
 import net.sf.okapi.steps.wordcount.common.MetricsAnnotation;
 
 import org.junit.Test;
 
 public class TestScopingReport {
 
 	private static final LocaleId EN = new LocaleId("en-us");
 	private static final LocaleId ES = new LocaleId("es-es");
 	private static final LocaleId DE = new LocaleId("de-de");
 	
 	
 	public static void testPath(String path) {
 		Logger logger = Logger.getLogger(TestScopingReport.class.getName()); // loggers are cached
 		logger.setLevel(Level.FINE);
 		logger.fine(new File(path).getAbsolutePath());
 	}
 	
 	@Test
 	public void testDefaultTemplate() throws MalformedURLException {
 		ScopingReportStep srs = new ScopingReportStep();
 		Parameters params = (Parameters) srs.getParameters();
 		assertEquals("My Project", params.getProjectName());
 		assertEquals("", params.getCustomTemplateURI());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		new XPipeline(
 				"HTML report test",
 				new XBatch(
 						new XBatchItem(
 								new URL("file", null, pathBase + "aa324.html"),
 								"UTF-8",
 								EN,
 								ES),								
 						new XBatchItem(
 								new URL("file", null, pathBase + "form.html"),
 								"UTF-8",
 								EN,
 								DE)
 						),
 				new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new EventLogger(),
 				new WordCountStep(),
 				new XPipelineStep(
 						srs,
 						new XParameter("outputPath", pathBase + "out/test_custom_template_report.html")
 						)
 		).execute();
 		testPath(pathBase + "out");
 		assertEquals("My Project", params.getProjectName());
 		assertEquals("", params.getCustomTemplateURI());
 	}
 	
 	@Test
 	public void testDefaultTemplateSWCS() throws MalformedURLException {
 		ScopingReportStep srs = new ScopingReportStep();
 		Parameters params = (Parameters) srs.getParameters();
 		assertEquals("My Project", params.getProjectName());
 		assertEquals("", params.getCustomTemplateURI());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		new XPipeline(
 				"HTML report test",
 				new XBatch(
 						new XBatchItem(
 								new URL("file", null, pathBase + "aa324.html"),
 								"UTF-8",
 								EN,
 								ES),								
 						new XBatchItem(
 								new URL("file", null, pathBase + "form.html"),
 								"UTF-8",
 								EN,
 								ES)
 						),
 				new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new EventLogger(),
 				new SimpleWordCountStep(),
 				new XPipelineStep(
 						srs,
 						new XParameter("outputPath", pathBase + "out/test_custom_template_report.html")
 						)
 		).execute();
 		testPath(pathBase + "out");
 		assertEquals("My Project", params.getProjectName());
 		assertEquals("", params.getCustomTemplateURI());
 	}
 	
 	@Test
 	public void testCustomTemplateSWCS() throws MalformedURLException {
 		String path = this.getClass().getResource("totals_report_template.html").getPath();
 		ScopingReportStep srs = new ScopingReportStep();
 		Parameters params = (Parameters) srs.getParameters();
 		params.setCustomTemplateURI(path);
 		assertEquals("My Project", params.getProjectName());
 		assertEquals(path, params.getCustomTemplateURI());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		new XPipeline(
 				"HTML report test",
 				new XBatch(
 						new XBatchItem(
 								new URL("file", null, pathBase + "aa324.html"),
 								"UTF-8",
 								EN,
 								ES),								
 						new XBatchItem(
 								new URL("file", null, pathBase + "form.html"),
 								"UTF-8",
 								EN,
 								DE)
 						),
 				new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new EventLogger(),
 				new SimpleWordCountStep(),
 				new XPipelineStep(
 						srs,
 						new XParameter("outputPath", pathBase + "out/totals_report.html")
 						)
 		).execute();
 		testPath(pathBase + "out");
 		assertEquals("My Project", params.getProjectName());
 		assertEquals(path, params.getCustomTemplateURI());
 	}
 	
 	@Test
 	public void testCustomTemplate() throws MalformedURLException {
 		ScopingReportStep srs = new ScopingReportStep();
 		Parameters params = (Parameters) srs.getParameters();
 		assertEquals("", params.getCustomTemplateURI());
 		params.setProjectName("Test scoping report");
 		String path = this.getClass().getResource("test_scoping_report.html").getPath();
 		assertNotNull(path);
 		params.setCustomTemplateURI(path);
 		assertEquals("test_scoping_report.html", Util.getFilename(path, true));
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		XPipeline pipeline = new XPipeline(
 				"HTML report test",
 				new XBatch(
 						new XBatchItem(
 								new URL("file", null, pathBase + "aa324.html"),
 								"UTF-8",
 								EN,
 								ES),								
 						new XBatchItem(
 								new URL("file", null, pathBase + "form.html"),
 								"UTF-8",
 								EN,
 								ES)
 						),
 				new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new EventLogger(),
 				new XPipelineStep(
 						srs,
 						new XParameter("outputPath", pathBase + "out/test_custom_template_report.html")
 						)
 		);
 		assertEquals("Test scoping report", params.getProjectName());
 		assertEquals("test_scoping_report.html", Util.getFilename(params.getCustomTemplateURI(), true));
 		pipeline.execute();
 		testPath(pathBase + "out");
 		assertEquals("Test scoping report", params.getProjectName());
 		assertEquals("test_scoping_report.html", Util.getFilename(params.getCustomTemplateURI(), true));
 	}
 		
 	@Test
 	public void htmlReportTest() throws MalformedURLException {
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		
 		new XPipeline(
 				"HTML report test",
 				new XBatch(
 						new XBatchItem(
 								new URL("file", null, pathBase + "aa324.html"),
 								"UTF-8",
 								EN,
 								ES),								
 						new XBatchItem(
 								new URL("file", null, pathBase + "form.html"),
 								"UTF-8",
 								EN,
 								ES)
 						),
 				new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new EventLogger(),
 				new XPipelineStep(
 						new ScopingReportStep(),
 						//new XParameter("projectName", "Test Scoping Report"),
 						//new XParameter("outputURI", this.getClass().getResource("").toString() + "out/test_scoping_report.html")
 						new XParameter("outputPath", pathBase + "out/test_scoping_report.html")
 						)
 		).execute();
 		
 		testPath(pathBase + "out");
 	}
 	
 	@Test
 	public void htmlReportTest2() throws URISyntaxException {
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		
 		StartDocument sd1 = new StartDocument("sd1");
 		sd1.setName(new File(this.getClass().getResource("aa324.html").toURI()).getAbsolutePath());
 		StartDocument sd2 = new StartDocument("sd2");
 		sd2.setName(new File(this.getClass().getResource("form.html").toURI()).getAbsolutePath());
 		
 		ScopingReportStep srs = new ScopingReportStep();
 		Parameters params = (Parameters) srs.getParameters();
 		params.setProjectName("Test scoping report");
 		params.setOutputPath(pathBase + "out/test_scoping_report2.html"); 
 		
 		srs.setSourceLocale(EN); 
 		srs.setTargetLocale(ES);
 		
 		srs.handleEvent(new Event(EventType.START_BATCH));
 		srs.handleEvent(new Event(EventType.START_DOCUMENT, sd1));
 		srs.handleEvent(new Event(EventType.START_DOCUMENT, sd2));
 		
 		Ending res = new Ending("end_batch");
 		MetricsAnnotation ma = new MetricsAnnotation();
 		res.setAnnotation(ma);
 		Metrics m = ma.getMetrics();		
 		
 		m.setMetric(GMX.TotalWordCount, 1273);
 		m.setMetric(ExactLocalContextMatchWordCountStep.METRIC, 72);
 		m.setMetric(GMX.ExactMatchedWordCount, 120);
 		m.setMetric(GMX.LeveragedMatchedWordCount, 132);
 		m.setMetric(GMX.FuzzyMatchedWordCount, 781);
 		m.setMetric(GMX.RepetitionMatchedWordCount, 112);
 		
 		srs.handleEvent(new Event(EventType.END_BATCH, res));
 		testPath(pathBase + "out/test_scoping_report2.html");
 	}
 	
 	@Test
 	public void testLeveraging() throws MalformedURLException {
 
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("aa324.html").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters params = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		params.setDbDirectory(pathBase + "testtm");
 		
 		new XPipeline(
 				"HTML report test",
 				new XBatch(
 //						new XBatchItem(
//								new URL("file", null, pathBase + "test2.txt"),
 //								"UTF-8",
 //								EN,
 //								ES),								
 						new XBatchItem(
 								//new URL("file", null, pathBase + "form.html"),
 								new URL("file", null, pathBase + "test.txt"),
 								"UTF-8",
 								EN,
 								ES)
 						),
 				//new RawDocumentToFilterEventsStep(new HtmlFilter()),
 				new RawDocumentToFilterEventsStep(new PlainTextFilter()),
 				new EventLogger(),
 				new XPipelineStep(
 						new SegmentationStep(),
 						//new Parameter("sourceSrxPath", pathBase + "test.srx")
 						new XParameter("sourceSrxPath", pathBase + "default.srx"),
 						//new Parameter("sourceSrxPath", pathBase + "myRules.srx")
 						new XParameter("trimSrcLeadingWS", net.sf.okapi.steps.segmentation.Parameters.TRIM_YES),
 						new XParameter("trimSrcTrailingWS", net.sf.okapi.steps.segmentation.Parameters.TRIM_YES)
 				),
 				new XPipelineStep(new LeveragingStep(), 
 						//new XParameter("resourceClassName", net.sf.okapi.connectors.opentran.OpenTranTMConnector.class.getName()),
 						new XParameter("resourceClassName", net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName()),
 						new XParameter("resourceParameters", params.toString(), true),
 						new XParameter("threshold", 60),
 						new XParameter("fillTarget", true)
 				),
 				new WordCountStep(),
 				new TextUnitLogger(),
 				new XPipelineStep(
 						new ScopingReportStep(),
 						new XParameter("projectName", "Test Scoping Report"),
 						//new XParameter("outputURI", this.getClass().getResource("").toString() + "out/test_scoping_report.html")
 						new XParameter("outputPath", pathBase + "out/test_scoping_report3.html")
 						)
 		).execute();
 		
 //	No golden file comparison is possible as the output report contains a localized date 
 //		FileCompare fc = new FileCompare();
 //		String outputFilePath = pathBase + "out/test_scoping_report3.html";
 //		String goldFilePath = pathBase + "gold/test_scoping_report3.html";
 //		assertTrue(fc.filesExactlyTheSame(outputFilePath, goldFilePath));
 		
 		testPath(pathBase + "out");
 	}
 
 	@Test
 	public void test_a_word_is_counted_only_once() throws MalformedURLException {
 		ScopingReportStep srs;
 		WordCountStep wcs;
 		StartDocument sd;
 		Ending ed;
 		Event sdEvent, edEvent;
 		Event sbEvent, ebEvent;
 		ITextUnit tu1, tu2, tu3, tu4;
 		Event tuEvent1, tuEvent2, tuEvent3, tuEvent4;
 		Logger logger = Logger.getLogger(getClass().getName());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		rparams.setDbDirectory(pathBase + "testtm");
 		
 		String outputFilePath = pathBase + "out/test_scoping_report4.txt";
 		String goldFilePath = pathBase + "gold/test_scoping_report4.txt";
 		
 		sbEvent = new Event(EventType.START_BATCH);
 		ebEvent = new Event(EventType.END_BATCH);
 		Event siEvent = new Event(EventType.START_BATCH_ITEM);
 		Event eiEvent = new Event(EventType.END_BATCH_ITEM);
 		
 		sd = new StartDocument("sd");		
 		sd.setLocale(LocaleId.ENGLISH);
 		sdEvent = new Event(EventType.START_DOCUMENT, sd);
 		
 		ed = new Ending("ed");
 		edEvent = new Event(EventType.END_DOCUMENT, ed);
 		
 		tu1 = new TextUnit("tu1");
 		tu1.setSource(new TextContainer("Elephants cannot fly."));
 		tuEvent1 = new Event(EventType.TEXT_UNIT, tu1);
 		
 		tu2 = new TextUnit("tu2");
 		tu2.setSource(new TextContainer("Elephants can't fly."));
 		tuEvent2 = new Event(EventType.TEXT_UNIT, tu2);
 		
 		tu3 = new TextUnit("tu3");
 		tu3.setSource(new TextContainer("Elephants can fly."));
 		tuEvent3 = new Event(EventType.TEXT_UNIT, tu3);
 		
 		tu4 = new TextUnit("tu4");
 		tu4.setSource(new TextContainer("Airplanes can fly."));
 		tuEvent4 = new Event(EventType.TEXT_UNIT, tu4);
 		
 		LeveragingStep ls = new LeveragingStep();
 		ls.setSourceLocale(LocaleId.ENGLISH);
 		ls.setTargetLocale(LocaleId.FRENCH);
 		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 		params.setResourceParameters(rparams.toString());
 		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 		params.setThreshold(60);
 		params.setFillTarget(true);
 		
 		wcs = new WordCountStep();				
 		srs = new ScopingReportStep();
 		srs.setSourceLocale(LocaleId.ENGLISH);
 		srs.setTargetLocale(LocaleId.FRENCH);
 		Parameters params2 = (Parameters) srs.getParameters();
 		params2.setOutputPath(outputFilePath);
 		params2.setCustomTemplateURI(this.getClass().getResource("golden_file_template.txt").getPath());
 		sd.setName(params2.getCustomTemplateURI());
 				
 		wcs.handleEvent(sbEvent);
 		wcs.handleEvent(siEvent);
 		wcs.handleEvent(sdEvent);
 		wcs.handleEvent(tuEvent1);
 		wcs.handleEvent(tuEvent2);
 		wcs.handleEvent(tuEvent3);
 		wcs.handleEvent(tuEvent4);
 		wcs.handleEvent(edEvent);
 		wcs.handleEvent(eiEvent);
 		wcs.handleEvent(ebEvent);
 		
 		ls.handleEvent(sbEvent);
 		ls.handleEvent(siEvent);
 		ls.handleEvent(sdEvent);
 		ls.handleEvent(tuEvent1);
 		ls.handleEvent(tuEvent2);
 		ls.handleEvent(tuEvent3);
 		ls.handleEvent(tuEvent4);
 		ls.handleEvent(edEvent);
 		ls.handleEvent(eiEvent);
 		ls.handleEvent(ebEvent);
 		
 		srs.handleEvent(sbEvent);
 		srs.handleEvent(siEvent);
 		srs.handleEvent(sdEvent);
 		srs.handleEvent(tuEvent1);
 		srs.handleEvent(tuEvent2);
 		srs.handleEvent(tuEvent3);
 		srs.handleEvent(tuEvent4);
 		srs.handleEvent(edEvent);
 		srs.handleEvent(eiEvent);
 		srs.handleEvent(ebEvent);
 				
 		logger.fine(TextUnitLogger.getTuInfo(tu1, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu2, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu3, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu4, LocaleId.ENGLISH));
 		
 		testPath(outputFilePath);
 		
 		FileCompare fc = new FileCompare();		
 		assertTrue(fc.filesExactlyTheSame(outputFilePath, goldFilePath));
 		
 	}	
 	
 	@Test
 	public void test_a_word_is_counted_only_once2() throws MalformedURLException {
 		ScopingReportStep srs;
 		WordCountStep wcs;
 		StartDocument sd;
 		Ending ed;
 		Event sdEvent, edEvent;
 		Event sbEvent, ebEvent;
 		ITextUnit tu1, tu2, tu3, tu4;
 		Event tuEvent1, tuEvent2, tuEvent3, tuEvent4;
 		Logger logger = Logger.getLogger(getClass().getName());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 			new net.sf.okapi.connectors.pensieve.Parameters();
 		rparams.setDbDirectory(pathBase + "testtm");
 		
 		String outputFilePath = pathBase + "out/test_scoping_report5.txt";
 		String goldFilePath = pathBase + "gold/test_scoping_report5.txt";
 		
 		sbEvent = new Event(EventType.START_BATCH);
 		ebEvent = new Event(EventType.END_BATCH);
 		Event siEvent = new Event(EventType.START_BATCH_ITEM);
 		Event eiEvent = new Event(EventType.END_BATCH_ITEM);
 		
 		sd = new StartDocument("sd");		
 		sd.setLocale(LocaleId.ENGLISH);
 		sdEvent = new Event(EventType.START_DOCUMENT, sd);
 		
 		ed = new Ending("ed");
 		edEvent = new Event(EventType.END_DOCUMENT, ed);
 		
 		tu1 = new TextUnit("tu1");
 		tu1.setSource(new TextContainer("Elephants cannot fly."));
 		tuEvent1 = new Event(EventType.TEXT_UNIT, tu1);
 		
 		tu2 = new TextUnit("tu2");
 		tu2.setSource(new TextContainer("Elephants can't fly."));
 		tuEvent2 = new Event(EventType.TEXT_UNIT, tu2);
 		
 		tu3 = new TextUnit("tu3");
 		tu3.setSource(new TextContainer("Elephants can fly."));
 		tuEvent3 = new Event(EventType.TEXT_UNIT, tu3);
 		
 		tu4 = new TextUnit("tu4");
 		tu4.setSource(new TextContainer("Airplanes can fly."));
 		tuEvent4 = new Event(EventType.TEXT_UNIT, tu4);
 		
 		LeveragingStep ls = new LeveragingStep();
 		ls.setSourceLocale(LocaleId.ENGLISH);
 		ls.setTargetLocale(LocaleId.FRENCH);
 		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 		params.setResourceParameters(rparams.toString());
 		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 		params.setThreshold(60);
 		params.setFillTarget(true);
 		
 		wcs = new WordCountStep();				
 		srs = new ScopingReportStep();
 		srs.setSourceLocale(LocaleId.ENGLISH);
 		srs.setTargetLocale(LocaleId.FRENCH);
 		Parameters params2 = (Parameters) srs.getParameters();
 		params2.setOutputPath(outputFilePath);
 		params2.setCustomTemplateURI(this.getClass().getResource("golden_file_template.txt").getPath());
 		params2.setCountAsNonTranslatable_ExactMatch(true);
 		params2.setCountAsNonTranslatable_GMXFuzzyMatch(true);
 		sd.setName(params2.getCustomTemplateURI());
 				
 		wcs.handleEvent(sbEvent);
 		wcs.handleEvent(siEvent);
 		wcs.handleEvent(sdEvent);
 		wcs.handleEvent(tuEvent1);
 		wcs.handleEvent(tuEvent2);
 		wcs.handleEvent(tuEvent3);
 		wcs.handleEvent(tuEvent4);
 		wcs.handleEvent(edEvent);
 		wcs.handleEvent(eiEvent);
 		wcs.handleEvent(ebEvent);
 		
 		ls.handleEvent(sbEvent);
 		ls.handleEvent(siEvent);
 		ls.handleEvent(sdEvent);
 		ls.handleEvent(tuEvent1);
 		ls.handleEvent(tuEvent2);
 		ls.handleEvent(tuEvent3);
 		ls.handleEvent(tuEvent4);
 		ls.handleEvent(edEvent);
 		ls.handleEvent(eiEvent);
 		ls.handleEvent(ebEvent);
 		
 		srs.handleEvent(sbEvent);
 		srs.handleEvent(siEvent);
 		srs.handleEvent(sdEvent);
 		srs.handleEvent(tuEvent1);
 		srs.handleEvent(tuEvent2);
 		srs.handleEvent(tuEvent3);
 		srs.handleEvent(tuEvent4);
 		srs.handleEvent(edEvent);
 		srs.handleEvent(eiEvent);
 		srs.handleEvent(ebEvent);
 				
 		logger.fine(TextUnitLogger.getTuInfo(tu1, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu2, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu3, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu4, LocaleId.ENGLISH));
 		
 		testPath(outputFilePath);
 		
 		FileCompare fc = new FileCompare();		
 		assertTrue(fc.filesExactlyTheSame(outputFilePath, goldFilePath));		
 	}
 	
 	@Test
 	public void test_repetitions() throws MalformedURLException {
 		ScopingReportStep srs;
 		WordCountStep wcs;
 		StartDocument sd;
 		RepetitionAnalysisStep ras;
 		Ending ed;
 		Event sdEvent, edEvent;
 		Event sbEvent, ebEvent;
 		ITextUnit tu1, tu2, tu3, tu4;
 		Event tuEvent1, tuEvent2, tuEvent3, tuEvent4;
 		Logger logger = Logger.getLogger(getClass().getName());
 		
 		String pathBase = Util.getDirectoryName(this.getClass().getResource("").getPath()) + "/";
 //		net.sf.okapi.connectors.pensieve.Parameters rparams = 
 //			new net.sf.okapi.connectors.pensieve.Parameters();
 //		rparams.setDbDirectory(pathBase + "testtm");
 		
 		String outputFilePath = pathBase + "out/test_scoping_report6.txt";
 		String goldFilePath = pathBase + "gold/test_scoping_report6.txt";
 		
 		sbEvent = new Event(EventType.START_BATCH);
 		ebEvent = new Event(EventType.END_BATCH);
 		Event siEvent = new Event(EventType.START_BATCH_ITEM);
 		Event eiEvent = new Event(EventType.END_BATCH_ITEM);
 		
 		sd = new StartDocument("sd");		
 		sd.setLocale(LocaleId.ENGLISH);
 		sdEvent = new Event(EventType.START_DOCUMENT, sd);
 		
 		ed = new Ending("ed");
 		edEvent = new Event(EventType.END_DOCUMENT, ed);
 		
 		tu1 = new TextUnit("tu1");
 		tu1.setSource(new TextContainer("Elephants cannot fly."));
 		tuEvent1 = new Event(EventType.TEXT_UNIT, tu1);
 		
 		tu2 = new TextUnit("tu2");
 		//tu2.setSource(new TextContainer("Elephants can't fly."));
 		tu2.setSource(new TextContainer("Elephants cannot fly."));
 		tuEvent2 = new Event(EventType.TEXT_UNIT, tu2);
 		
 		tu3 = new TextUnit("tu3");
 		tu3.setSource(new TextContainer("Elephants can fly."));
 		tuEvent3 = new Event(EventType.TEXT_UNIT, tu3);
 		
 		tu4 = new TextUnit("tu4");
 		tu4.setSource(new TextContainer("Airplanes can fly."));
 		tuEvent4 = new Event(EventType.TEXT_UNIT, tu4);
 		
 		//LeveragingStep ls = new LeveragingStep();
 //		ls.setSourceLocale(LocaleId.ENGLISH);
 //		ls.setTargetLocale(LocaleId.FRENCH);
 //		net.sf.okapi.steps.leveraging.Parameters params = (net.sf.okapi.steps.leveraging.Parameters) ls.getParameters();
 //		params.setResourceParameters(rparams.toString());
 //		params.setResourceClassName(net.sf.okapi.connectors.pensieve.PensieveTMConnector.class.getName());
 //		params.setThreshold(60);
 //		params.setFillTarget(true);
 		
 		ras = new RepetitionAnalysisStep();
 		net.sf.okapi.steps.repetitionanalysis.Parameters params = 
 			(net.sf.okapi.steps.repetitionanalysis.Parameters) ras.getParameters();
 		params.setFuzzyThreshold(10);
 		
 		ras.setSourceLocale(LocaleId.ENGLISH);
 		ras.setTargetLocale(LocaleId.FRENCH);
 		wcs = new WordCountStep();				
 		srs = new ScopingReportStep();
 		srs.setSourceLocale(LocaleId.ENGLISH);
 		srs.setTargetLocale(LocaleId.FRENCH);
 		Parameters params2 = (Parameters) srs.getParameters();
 		params2.setOutputPath(outputFilePath);
 		params2.setCustomTemplateURI(this.getClass().getResource("golden_file_template2.txt").getPath());
 		sd.setName(params2.getCustomTemplateURI());
 				
 		ras.handleEvent(sbEvent);
 		ras.handleEvent(siEvent);
 		ras.handleEvent(sdEvent);
 		ras.handleEvent(tuEvent1);
 		ras.handleEvent(tuEvent2);
 		ras.handleEvent(tuEvent3);
 		ras.handleEvent(tuEvent4);
 		ras.handleEvent(edEvent);
 		ras.handleEvent(eiEvent);
 		ras.handleEvent(ebEvent);
 		
 		wcs.handleEvent(sbEvent);
 		wcs.handleEvent(siEvent);
 		wcs.handleEvent(sdEvent);
 		wcs.handleEvent(tuEvent1);
 		wcs.handleEvent(tuEvent2);
 		wcs.handleEvent(tuEvent3);
 		wcs.handleEvent(tuEvent4);
 		wcs.handleEvent(edEvent);
 		wcs.handleEvent(eiEvent);
 		wcs.handleEvent(ebEvent);
 		
 //		ls.handleEvent(sbEvent);
 //		ls.handleEvent(siEvent);
 //		ls.handleEvent(sdEvent);
 //		ls.handleEvent(tuEvent1);
 //		ls.handleEvent(tuEvent2);
 //		ls.handleEvent(tuEvent3);
 //		ls.handleEvent(tuEvent4);
 //		ls.handleEvent(edEvent);
 //		ls.handleEvent(eiEvent);
 //		ls.handleEvent(ebEvent);
 		
 		srs.handleEvent(sbEvent);
 		srs.handleEvent(siEvent);
 		srs.handleEvent(sdEvent);
 		srs.handleEvent(tuEvent1);
 		srs.handleEvent(tuEvent2);
 		srs.handleEvent(tuEvent3);
 		srs.handleEvent(tuEvent4);
 		srs.handleEvent(edEvent);
 		srs.handleEvent(eiEvent);
 		srs.handleEvent(ebEvent);
 				
 		logger.fine(TextUnitLogger.getTuInfo(tu1, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu2, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu3, LocaleId.ENGLISH));
 		logger.fine(TextUnitLogger.getTuInfo(tu4, LocaleId.ENGLISH));
 		
 		testPath(outputFilePath);
 		
 		FileCompare fc = new FileCompare();		
 		assertTrue(fc.filesExactlyTheSame(outputFilePath, goldFilePath));		
 	}
 }
 
