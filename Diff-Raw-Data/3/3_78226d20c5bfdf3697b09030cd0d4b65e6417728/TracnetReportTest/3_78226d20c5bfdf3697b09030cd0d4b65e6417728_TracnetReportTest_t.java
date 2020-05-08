 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 package org.openmrs.module.tracnetreportingsdmx;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.GlobalProperty;
 import org.openmrs.api.context.Context;
 import org.openmrs.module.reporting.common.DateUtil;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.report.ReportData;
 import org.openmrs.module.reporting.report.ReportDesign;
 import org.openmrs.module.reporting.report.definition.ReportDefinition;
 import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
 import org.openmrs.module.reporting.report.renderer.ReportRenderer;
 import org.openmrs.module.reporting.report.service.ReportService;
 import org.openmrs.module.reporting.report.util.ReportUtil;
 import org.openmrs.test.BaseContextSensitiveTest;
 import org.openmrs.test.BaseModuleContextSensitiveTest;
 import org.openmrs.test.SkipBaseSetup;
 
 /**
  * Tests the production of the TracNet Report Definition to SDMX
  */
 @SkipBaseSetup
 public class TracnetReportTest extends BaseModuleContextSensitiveTest {
 	
 	Log log = LogFactory.getLog(getClass());
 
 	/**
 	 * @see BaseContextSensitiveTest#useInMemoryDatabase()
 	 */
 	@Override
 	public Boolean useInMemoryDatabase() {
 		return false;
 	}
 	
 	@Before
 	public void setup() throws Exception {
 		authenticate();
 		setGlobalProperty("tracnetreportingsdmx.locationDataProviderId", "1234");
 		setGlobalProperty("tracnetreportingsdmx.confirmation_email_address", "mseaton@pih.org");
 		setGlobalProperty("tracnetreportingsdmx.email_from", "mseaton@pih.org");
 		setGlobalProperty("tracnetreportingsdmx.email_to", "mseaton@pih.org");		
 	}
 
 	/**
 	 * Tests the TracnetReport
 	 */
 	@Test
 	public void shouldGenerateTracnetReport() throws Exception { 
 		
 		ReportDefinition reportDefinition = TracnetReport.getTracnetReportDefinition(true);
 		
 		EvaluationContext context = new EvaluationContext();
 		context.addParameterValue("startDate", DateUtil.getDateTime(2011, 1, 1));
 		context.addParameterValue("endDate", DateUtil.getDateTime(2011, 1, 31));
 		ReportDefinitionService rs = Context.getService(ReportDefinitionService.class);
 		ReportData data = rs.evaluate(reportDefinition, context);
 		
 		ReportDesign design = Context.getService(ReportService.class).getReportDesignByUuid(TracnetReport.REPORT_DESIGN_UUID);
 		
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ReportRenderer renderer = design.getRendererType().newInstance();
 		renderer.render(data, design.getUuid(), baos);
 		baos.close();
 		
 		String fileName = renderer.getFilename(reportDefinition, design.getUuid());
		File outFile = File.createTempFile(fileName, null);
		ReportUtil.writeByteArrayToFile(outFile, baos.toByteArray());
 	}
 	
 	private void setGlobalProperty(String name, String value) {
 		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(name);
 		if (gp == null) {
 			gp = new GlobalProperty(name);
 		}
 		gp.setPropertyValue(value);
 		Context.getAdministrationService().saveGlobalProperty(gp);
 	}
 }
