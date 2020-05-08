 /*
  *  Copyright (C) 2007 - 2012 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.opensdi.persistence.service.impl;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import it.geosolutions.opensdi.destination.dto.TraceDto;
 import it.geosolutions.opensdi.destination.dto.TraceLineDto;
 import it.geosolutions.opensdi.destination.service.TraceService;
 import it.geosolutions.opensdi.destination.service.impl.TraceServiceImpl;
 import it.geosolutions.opensdi.dto.GeobatchRunInfo;
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * @author adiaz
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration("classpath:applicationContext-test.xml")
 public class TraceServiceTest {
 
 private static Random RANDOM = new Random();
 
 /**
  * We need the instance because we need to insert for this test
  */
 @Autowired
private TraceServiceImpl traceService;
 
 /**
  * Test for {@link TraceService#searchByRun(GeobatchRunInfo)} method
  */
 @Test
 public void testSearchByRunInfo() {
     try {
         int traces = 10;
         TraceDto trace = null;
         String fileName = generateRandomString();
         for (int i = 0; i < traces; i++) {
             trace = getTestTrace(fileName, 10);
             trace.setNr_rec_scartati(new BigInteger(i + ""));
             trace = traceService.saveTrace(trace);
         }
         List<TraceDto> list = traceService.searchByRun(getTestRunInfo(trace));
         assertNotNull(list);
         assertTrue(!list.isEmpty());
         assertTrue(list.size() == traces);
     } catch (Exception e) {
         e.printStackTrace();
         fail();
     }
 }
 
 /**
  * Test for {@link TraceService#cleanByRun(GeobatchRunInfo)} method
  */
 @Test
 public void testCleanRunInfo() {
     try {
         int traces = 10;
         TraceDto trace = null;
         String fileName = generateRandomString();
         for (int i = 0; i < traces; i++) {
             trace = getTestTrace(fileName, 10);
             trace.setNr_rec_scartati(new BigInteger(i + ""));
             trace = traceService.saveTrace(trace);
         }
         GeobatchRunInfo runInfo = getTestRunInfo(trace);
         List<TraceDto> list = traceService.searchByRun(runInfo);
         assertNotNull(list);
         assertTrue(!list.isEmpty());
         assertTrue(list.size() == traces);
         traceService.cleanByRun(runInfo);
         list = traceService.searchByRun(runInfo);
         assertTrue(list == null || list.isEmpty());
     } catch (Exception e) {
         e.printStackTrace();
         fail();
     }
 }
 
 /**
  * @return randomized string
  */
 private String generateRandomString() {
     return "test_" + RANDOM.nextInt();
 }
 
 /**
  * Obtain a trace entity for a fileName
  * 
  * @param fileName
  * @return trace entity from test proposal
  */
 private TraceDto getTestTrace(String fileName, int linesNumber) {
     TraceDto trace = new TraceDto();
     Date today = new Date();
     trace.setNome_file(fileName);
     trace.setData(today);
     trace.setData_elab(today);
     trace.setData_imp_siig(today);
     trace.setData_imp_storage(today);
     trace.setNr_rec_scartati(new BigInteger("0"));
     trace.setNr_rec_shape(new BigInteger("0"));
     trace.setNr_rec_scartati_siig(new BigInteger("0"));
     trace.setNr_rec_storage(new BigInteger("0"));
     List<TraceLineDto> lines = new LinkedList<TraceLineDto>();
     for (int i = 0; i < linesNumber; i++)
         lines.add(getTestTraceLine());
     trace.setLines(lines);
     return trace;
 }
 
 /**
  * @return trace line entity from test proposal
  */
 private TraceLineDto getTestTraceLine() {
     TraceLineDto line = new TraceLineDto();
     line.setCodice_log("test");
     line.setDescr_errore("test");
     return line;
 }
 
 /**
  * Obtain a run information related to a trace element
  * 
  * @param trace
  * @return run information with file name initialized
  */
 private GeobatchRunInfo getTestRunInfo(TraceDto trace) {
     GeobatchRunInfo runInfo = new GeobatchRunInfo();
     String[] compositeId = { "path", trace.getNome_file() + ".test",
             "operation" };
     runInfo.setCompositeId(compositeId);
     return runInfo;
 }
 }
