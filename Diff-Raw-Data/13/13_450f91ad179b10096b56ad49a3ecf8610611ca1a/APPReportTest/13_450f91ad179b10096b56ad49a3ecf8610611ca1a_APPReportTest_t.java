 package com.iappsam.reporting;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.Date;
 
 import net.sf.jasperreports.engine.JRException;
 
 import org.junit.Test;
 
 import com.iappsam.entities.forms.AnnualProcurementPlan;
 
 public class APPReportTest {
 
 	@Test
 	public void exportAPPtoPDF() throws JRException {
 
 		AnnualProcurementPlan.Id id = new AnnualProcurementPlan.Id(2010, 1);
 		AnnualProcurementPlan app = new AnnualProcurementPlan(id, "CTR-001", new Date(), 0, 0);
 
 		APPReport report = new APPReport(app);
 		File pdf = report.toPDF();
 
 		assertTrue(pdf.exists());

		pdf.delete();
 	}
 }
