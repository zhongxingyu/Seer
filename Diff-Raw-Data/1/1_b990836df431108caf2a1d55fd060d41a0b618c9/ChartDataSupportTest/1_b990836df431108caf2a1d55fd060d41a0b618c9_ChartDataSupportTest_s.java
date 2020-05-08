 /**
  *    OpenTrainingCenter
  *
  *    Copyright (C) 2014 Sascha Iseli sascha.iseli(at)gmx.ch
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package ch.opentrainingcenter.charts.bar.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import ch.opentrainingcenter.charts.ng.SimpleTrainingChart;
 import ch.opentrainingcenter.charts.single.XAxisChart;
 import ch.opentrainingcenter.core.helper.RunType;
 import ch.opentrainingcenter.model.ModelFactory;
 import ch.opentrainingcenter.model.training.ISimpleTraining;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 @SuppressWarnings("nls")
 public class ChartDataSupportTest {
 
     @Test(expected = IllegalArgumentException.class)
     public void testConvertAndSortNull() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         support.convertAndSort(null);
     }
 
     @Test
     public void testConvertAndSortEmpty() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         final List<ChartDataWrapper> result = support.convertAndSort(new ArrayList<ISimpleTraining>());
 
         assertNotNull(result);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testConvertAndSortOneTraining() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         final ArrayList<ISimpleTraining> data = new ArrayList<ISimpleTraining>();
         final DateTime dt = new DateTime(2014, 8, 29, 12, 0);
         data.add(ModelFactory.createSimpleTraining(1000, 10, dt.toDate(), 142, 159, 3, RunType.LONG_JOG, ""));
 
         final List<ChartDataWrapper> result = support.convertAndSort(data);
 
         assertFalse(result.isEmpty());
 
         assertEquals("Distanz", 1.0, result.get(0).getValue(SimpleTrainingChart.DISTANZ), 0.0001);
         assertEquals("Distanz", 142, result.get(0).getValue(SimpleTrainingChart.HERZ), 0.0001);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreatePastDataNull_empty() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         support.createPastData(null, new ArrayList<ChartDataWrapper>());
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testCreatePastData_empty_null() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         support.createPastData(new ArrayList<ISimpleTraining>(), null);
     }
 
     @Test
     public void testCreatePastData_empty_empty() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         final List<ChartDataWrapper> result = support.createPastData(new ArrayList<ISimpleTraining>(), new ArrayList<ChartDataWrapper>());
 
         assertNotNull(result);
         assertTrue(result.isEmpty());
     }
 
     @Test
     public void testCreatePastData_empty_now() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         final ArrayList<ChartDataWrapper> now = new ArrayList<ChartDataWrapper>();
         final DateTime datum = new DateTime(2013, 8, 29, 12, 00);
         final String category = "a";
         now.add(new ChartDataWrapper(1000, 142, category, datum.toDate()));
 
         // execute
         final List<ChartDataWrapper> result = support.createPastData(new ArrayList<ISimpleTraining>(), now);
 
         final ChartDataWrapper cdw = result.get(0);
         assertEquals(category, cdw.getCategory());
         assertEquals(0, cdw.getValue(SimpleTrainingChart.DISTANZ), 0.001);
         assertEquals(0, cdw.getValue(SimpleTrainingChart.HERZ), 0.001);
         final DateTime date = new DateTime(cdw.getDate().getTime());
         assertEquals("Wird mit der Vorjahresperiode verglichen", 2012, date.getYear());
     }
 
     @Test
     public void testCreatePastData_merge() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         // NOW
         final ArrayList<ChartDataWrapper> now = new ArrayList<ChartDataWrapper>();
         final DateTime datum = new DateTime(2013, 8, 29, 12, 00);
         final String category = "08";
         now.add(new ChartDataWrapper(1000, 142, category, datum.toDate()));
 
         // PAST
         final ArrayList<ISimpleTraining> dataPast = new ArrayList<ISimpleTraining>();
         final DateTime datePast = new DateTime(2012, 8, 29, 12, 00);
 
         dataPast.add(ModelFactory.createSimpleTraining(2000, 2d, datePast.toDate(), 132, 155, 4d, RunType.EXT_INTERVALL, "note"));
 
         // execute
         final List<ChartDataWrapper> result = support.createPastData(dataPast, now);
 
         assertEquals("Gibt nur eine Kategorie", 1, result.size());
         final ChartDataWrapper cdw = result.get(0);
         assertEquals(category, cdw.getCategory());
         assertEquals(2, cdw.getValue(SimpleTrainingChart.DISTANZ), 0.001);
         assertEquals(132, cdw.getValue(SimpleTrainingChart.HERZ), 0.001);
         final DateTime date = new DateTime(cdw.getDate().getTime());
         assertEquals("Wird mit der Vorjahresperiode verglichen", 2012, date.getYear());
     }
 
     @Test
     public void testCreatePastData_2_categories() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.MONTH);
 
         // NOW
         final ArrayList<ChartDataWrapper> now = new ArrayList<ChartDataWrapper>();
         final DateTime datum = new DateTime(2013, 8, 29, 12, 00);
         final String category = "08";
         now.add(new ChartDataWrapper(1000, 142, category, datum.toDate()));
 
         // PAST
         final ArrayList<ISimpleTraining> dataPast = new ArrayList<ISimpleTraining>();
         final DateTime datePast = new DateTime(2012, 9, 29, 12, 00);
 
         dataPast.add(ModelFactory.createSimpleTraining(2000, 2d, datePast.toDate(), 132, 155, 4d, RunType.EXT_INTERVALL, "note"));
 
         // execute
         final List<ChartDataWrapper> result = support.createPastData(dataPast, now);
 
         assertEquals("Gibt zwei Kategorien", 2, result.size());
         final ChartDataWrapper cdw1 = result.get(0);
         assertEquals(category, cdw1.getCategory());
         assertEquals(0, cdw1.getValue(SimpleTrainingChart.DISTANZ), 0.001);
         assertEquals(0, cdw1.getValue(SimpleTrainingChart.HERZ), 0.001);
         final DateTime date1 = new DateTime(cdw1.getDate().getTime());
         assertEquals("Wird mit der Vorjahresperiode verglichen", 2012, date1.getYear());
 
         final ChartDataWrapper cdw2 = result.get(1);
         assertEquals("09", cdw2.getCategory());
         assertEquals(2, cdw2.getValue(SimpleTrainingChart.DISTANZ), 0.001);
         assertEquals(132, cdw2.getValue(SimpleTrainingChart.HERZ), 0.001);
         final DateTime date2 = new DateTime(cdw2.getDate().getTime());
         assertEquals("Wird mit der Vorjahresperiode verglichen", 2012, date2.getYear());
     }
 
     @Test
     public void testCreatePastData_1_categories_for_2_YEAR() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.YEAR);
 
         // NOW
         final ArrayList<ChartDataWrapper> now = new ArrayList<ChartDataWrapper>();
         final DateTime datum = new DateTime(2013, 8, 29, 12, 00);
         final String category = "2013";
         now.add(new ChartDataWrapper(1000, 142, category, datum.toDate()));
 
         // PAST
         final ArrayList<ISimpleTraining> dataPast = new ArrayList<ISimpleTraining>();
         final DateTime datePast = new DateTime(2012, 8, 29, 12, 00);
 
         dataPast.add(ModelFactory.createSimpleTraining(2000, 2d, datePast.toDate(), 132, 155, 4d, RunType.EXT_INTERVALL, "note"));
 
         // execute
         final List<ChartDataWrapper> result = support.createPastData(dataPast, now);
 
         assertEquals("Es gibt nur eine Past Kategorie", 1, result.size());
         final ChartDataWrapper cdw1 = result.get(0);
         assertEquals("2012", cdw1.getCategory());
         assertEquals(2, cdw1.getValue(SimpleTrainingChart.DISTANZ), 0.001);
         assertEquals(132, cdw1.getValue(SimpleTrainingChart.HERZ), 0.001);
         final DateTime date1 = new DateTime(cdw1.getDate().getTime());
         assertEquals("Wird mit der Vorjahresperiode verglichen", 2012, date1.getYear());
     }
 
     @Test
     public void testCreatePast_day_not_supported() {
         final ChartDataSupport support = new ChartDataSupport(XAxisChart.DAY);
 
         // NOW
         final ArrayList<ChartDataWrapper> now = new ArrayList<ChartDataWrapper>();
         final DateTime datum = new DateTime(2013, 8, 29, 12, 00);
         final String category = "08";
         now.add(new ChartDataWrapper(1000, 142, category, datum.toDate()));
 
         // PAST
         final ArrayList<ISimpleTraining> dataPast = new ArrayList<ISimpleTraining>();
         final DateTime datePast = new DateTime(2012, 8, 29, 12, 00);
 
         dataPast.add(ModelFactory.createSimpleTraining(2000, 2d, datePast.toDate(), 132, 155, 4d, RunType.EXT_INTERVALL, "note"));
 
         // execute
         final List<ChartDataWrapper> result = support.createPastData(dataPast, now);
 
         assertEquals("Day wird nicht supported", 0, result.size());
     }
 }
