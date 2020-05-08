 package net.alpha01.jwtest.exports;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigInteger;
 import java.util.Iterator;
 
 import com.sun.star.comp.helper.BootstrapException;
 import com.sun.star.lang.XComponent;
 import com.sun.star.sheet.XSpreadsheet;
 import com.sun.star.uno.Exception;
 
 import net.alpha01.jwtest.beans.Requirement;
 import net.alpha01.jwtest.beans.Result;
 import net.alpha01.jwtest.beans.Session;
 import net.alpha01.jwtest.beans.Step;
 import net.alpha01.jwtest.beans.TestCase;
 import net.alpha01.jwtest.dao.RequirementMapper;
 import net.alpha01.jwtest.dao.ResultMapper;
 import net.alpha01.jwtest.dao.ResultMapper.ResultSort;
 import net.alpha01.jwtest.dao.SessionMapper;
 import net.alpha01.jwtest.dao.SqlConnection;
 import net.alpha01.jwtest.dao.SqlSessionMapper;
 import net.alpha01.jwtest.dao.StepMapper;
 import net.alpha01.jwtest.dao.TestCaseMapper;
 import net.alpha01.jwtest.exceptions.JWTestException;
 import au.com.bytecode.opencsv.CSVWriter;
 
 public class ResultODSExporter {
 	public static File exportToODS(int idSession) throws JWTestException{
 		File tmpFile;
 		try {
			tmpFile = File.createTempFile("jwtest_export_results", ".ods");
 			tmpFile.deleteOnExit();
 			
 
 			SqlSessionMapper<ResultMapper> sesMapper = SqlConnection.getSessionMapper(ResultMapper.class);
 			SessionMapper sessionMapper = sesMapper.getSqlSession().getMapper(SessionMapper.class);
 			TestCaseMapper testMapper = sesMapper.getSqlSession().getMapper(TestCaseMapper.class);
 			RequirementMapper reqMapper = sesMapper.getSqlSession().getMapper(RequirementMapper.class);
 			StepMapper stepMapper = sesMapper.getSqlSession().getMapper(StepMapper.class);
 			Session sess=sessionMapper.get(BigInteger.valueOf(idSession));
 			
 			XComponent xComponent=PlanODSExporter.exportToXComponent(sess.getId_plan().intValue());
 			XSpreadsheet xSpreadsheet=RequirementODSExporter.getSpreadSheet(xComponent, "Esecuzione");
 			
 			Iterator<Result> itr = sesMapper.getMapper().getAll(new ResultSort(idSession)).iterator();
 			
 			int y=10;
 			while (itr.hasNext()){
 				Result res = itr.next();
 				TestCase test = testMapper.get(res.getId_testcase());
 				Requirement req= reqMapper.get(test.getId_requirement());
 				String description=test.getDescription();
 				String expectedResult=test.getExpected_result();
 				
 				Iterator<Step> its = stepMapper.getAll(test.getId().intValue()).iterator();
 				int i=0;
 				while (its.hasNext()){
 					i++;
 					Step step = its.next();
 					description+=i+") "+step.getDescription();
 					expectedResult+=i+") "+step.getExpected_result();
 				}
 				String result;
 				if (res.getSuccess()){
 					result="OK";
 				}else{
 					result="C";
 				}
 				
 				xSpreadsheet.getCellByPosition(1, y).setFormula(req.getType()+"-"+req.getNum());
 				xSpreadsheet.getCellByPosition(2, y).setFormula(test.getId().toString());
 				xSpreadsheet.getCellByPosition(3, y).setFormula(test.getName());
 				xSpreadsheet.getCellByPosition(5, y).setFormula(description);
 				xSpreadsheet.getCellByPosition(6, y).setFormula(expectedResult);
 				xSpreadsheet.getCellByPosition(7, y).setFormula(result);
 				xSpreadsheet.getCellByPosition(8, y++).setFormula(res.getNote());
 				
 			}
 		
 			sesMapper.close();
 			
 
 			RequirementODSExporter.storeDocComponent(xComponent, "file:///"+tmpFile.getAbsolutePath().replace('\\', '/'));
 			RequirementODSExporter.closeDocComponent(xComponent);
 			
 			return tmpFile;
 		} catch (IOException e) {
 			throw new JWTestException(ResultODSExporter.class,e);
 		} catch (BootstrapException e) {
 			throw new JWTestException(ResultODSExporter.class,e);
 		} catch (Exception e) {
 			throw new JWTestException(ResultODSExporter.class,e);
 		} catch (java.lang.Exception e) {
 			throw new JWTestException(ResultODSExporter.class,e);
 		}
 	}
 }
