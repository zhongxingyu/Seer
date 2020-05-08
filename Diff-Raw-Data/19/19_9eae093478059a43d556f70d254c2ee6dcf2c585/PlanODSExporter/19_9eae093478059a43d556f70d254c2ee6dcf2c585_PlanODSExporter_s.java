 package net.alpha01.jwtest.exports;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Iterator;
 
 import net.alpha01.jwtest.beans.Plan;
 import net.alpha01.jwtest.beans.Step;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.dao.PlanMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.StepMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.exceptions.JWTestException;
 
 import org.apache.log4j.Logger;
 
 import com.sun.star.comp.helper.BootstrapException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.sheet.XSpreadsheet;
 import com.sun.star.uno.Exception;
 
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class PlanODSExporter {
 	
 	/**
 	 * 
 	 * @param idPlan
 	 * @return
 	 * @throws BootstrapException
 	 * @throws Exception
 	 * @throws JWTestException
 	 */
 	public static XComponent exportToXComponent(int idPlan) throws BootstrapException, Exception, JWTestException{
 		SqlSessionMapper<PlanMapper> sesMapper = SqlConnection.getSessionMapper(PlanMapper.class);
 		TestCaseMapper testMapper = sesMapper.getSqlSession().getMapper(TestCaseMapper.class);
 		StepMapper stepMapper = sesMapper.getSqlSession().getMapper(StepMapper.class);
 		Plan plan = sesMapper.getMapper().get(BigInteger.valueOf(idPlan));
 		XComponent xComponent=TestCaseODSExporter.exportToXComponent();
 		XSpreadsheet xSpreadsheet=RequirementODSExporter.getSpreadSheet(xComponent, "Piano");
 		int y=10;
 		
 		Iterator<TestCase> itt = testMapper.getAllByPlan(plan.getId()).iterator();
 		while (itt.hasNext()) {
 			TestCase tCase = itt.next();
 			String description = tCase.getDescription();
 			String expectedResult = tCase.getExpected_result();
 			Iterator<Step> its = stepMapper.getAll(tCase.getId().intValue()).iterator();
 			int i = 0;
 			while (its.hasNext()) {
 				i++;
 				Step step = its.next();
 				description += i + ") " + step.getDescription();
 				expectedResult += i + ") " + step.getExpected_result();
 			}
 			xSpreadsheet.getCellByPosition(1, y).setFormula(tCase.getRequirement().getType().toString());
 			xSpreadsheet.getCellByPosition(2, y).setFormula(tCase.getRequirement().getNum().toString());
			xSpreadsheet.getCellByPosition(4, y).setFormula(tCase.getRequirement().getId().toString());
 			xSpreadsheet.getCellByPosition(5, y).setFormula(tCase.getName());
 			xSpreadsheet.getCellByPosition(7, y).setFormula(description);
 			xSpreadsheet.getCellByPosition(8, y++).setFormula(expectedResult);
 		}
 		sesMapper.close();
 		return xComponent;
 	}
 	/**
 	 * 
 	 * @param idPlan
 	 * @return
 	 * @throws JWTestException
 	 * @throws BootstrapException
 	 * @throws Exception
 	 */
 	public static File exportToODS(int idPlan) throws JWTestException {
 		File tmpFile;
 		try {
			tmpFile = File.createTempFile("jwtest_export_plan", ".");
 			Logger.getLogger(PlanODSExporter.class).debug("Exporting Plan on tmpFile:" + tmpFile.getAbsolutePath());
 			tmpFile.deleteOnExit();
 			
 			XComponent xComponent=exportToXComponent(idPlan);
 			
 			RequirementODSExporter.storeDocComponent(xComponent, "file:///"+tmpFile.getAbsolutePath().replace('\\', '/'));
 			RequirementODSExporter.closeDocComponent(xComponent);
 			
 			return tmpFile;
 		} catch (IOException e) {
 			throw new JWTestException(PlanODSExporter.class, e);
 		} catch (BootstrapException e) {
 			throw new JWTestException(PlanODSExporter.class, e);
 		} catch (Exception e) {
 			throw new JWTestException(PlanODSExporter.class, e);
 		} catch (java.lang.Exception e) {
 			throw new JWTestException(PlanODSExporter.class, e);
 		}
 	}
 }
