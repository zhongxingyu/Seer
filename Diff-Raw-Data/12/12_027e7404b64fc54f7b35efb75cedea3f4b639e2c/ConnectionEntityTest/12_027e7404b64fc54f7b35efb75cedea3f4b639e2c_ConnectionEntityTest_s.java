 /*
  * Copyright (C) 2014 Project-Phoenix
  * 
  * This file is part of library.
  * 
  * library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with library.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.phoenix.rs;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 import org.junit.Test;
 
 import de.phoenix.rs.entity.PhoenixDetails;
 import de.phoenix.rs.entity.PhoenixLectureGroup;
 import de.phoenix.rs.entity.PhoenixTask;
 import de.phoenix.rs.key.ConnectionEntity;
 import de.phoenix.rs.key.SelectEntity;
 
 public class ConnectionEntityTest {
 
     @Test
     public void testManual() {
         ConnectionEntity ce = new ConnectionEntity();
         SelectEntity<PhoenixTask> ts = new SelectEntity<PhoenixTask>().addKey("title", "TestTask");
         SelectEntity<PhoenixLectureGroup> gs = new SelectEntity<PhoenixLectureGroup>().addKey("name", "TestGroup");
 
         ce.addSelectEntity(PhoenixTask.class, ts).addSelectEntity(PhoenixLectureGroup.class, gs);
 
         List<SelectEntity<PhoenixTask>> selectEntities = ce.getSelectEntities(PhoenixTask.class);
         assertEquals("TestTask", selectEntities.get(0).get("title"));
 
         List<SelectEntity<PhoenixLectureGroup>> groupSelects = ce.getSelectEntities(PhoenixLectureGroup.class);
         assertEquals("TestGroup", groupSelects.get(0).get("name"));
     }
 
     @Test
     public void testAutomatic() {
         ConnectionEntity ce = new ConnectionEntity();
 
         PhoenixDetails details = new PhoenixDetails("testRoom", DateTimeConstants.MONDAY, new LocalTime(12, 30), new LocalTime(14, 00), new Period(), new LocalDate(1991, 3, 3), new LocalDate(1990, 3, 3));
         ce.addEntity(details);
 
         SelectEntity<PhoenixDetails> detailsSelector = ce.getFirstSelectEntity(PhoenixDetails.class);
 
         assertEquals("testRoom", detailsSelector.get("room"));
     }
 
     @Test
     public void testAutomaticList() {
         ConnectionEntity ce = new ConnectionEntity();
 
         PhoenixDetails detail = new PhoenixDetails("testRoom", DateTimeConstants.MONDAY, new LocalTime(12, 30), new LocalTime(14, 00), new Period(), new LocalDate(1991, 3, 3), new LocalDate(1990, 3, 3));
 
         List<PhoenixDetails> detailList = Arrays.asList(detail, detail);
 
         ce.addEntities(detailList);
 
         List<SelectEntity<PhoenixDetails>> selectEntities = ce.getSelectEntities(PhoenixDetails.class);
         assertEquals(2, selectEntities.size());
         for (SelectEntity<PhoenixDetails> selectEntity : selectEntities) {
             assertEquals("testRoom", selectEntity.get("room"));
         }
 
     }
 
     @Test
     public void testAttributes() {
         ConnectionEntity ce = new ConnectionEntity();
         DateTime now = DateTime.now();
 
         ce.addAttribute("title", "TestBlatt").addAttribute("date", now);
 
         assertEquals("TestBlatt", ce.getAttribute("title"));
         assertEquals(now, ce.getAttribute("date"));
     }
 }
