 package de.christian_klisch.software.servercheck.util;
 
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 
 import org.junit.Test;
 
 import junit.framework.TestCase;
 import de.christian_klisch.software.servercontrol.model.CommandView;
 import de.christian_klisch.software.servercontrol.model.InfoTask;
 import de.christian_klisch.software.servercontrol.model.InfoTaskHTML;
 import de.christian_klisch.software.servercontrol.model.SqlView;
 import de.christian_klisch.software.servercontrol.model.Task;
 import de.christian_klisch.software.servercontrol.util.ModelUtil;
 
 /**
  * JUnit Test.
  * 
  * @author Christian Klisch
  * 
  *         License:
  * 
  *         This program is free software; you can redistribute it and/or modify
  *         it under the terms of the GNU General Public License version 2 as
  *         published by the Free Software Foundation.
  * 
  *         This program is distributed in the hope that it will be useful, but
  *         WITHOUT ANY WARRANTY; without even the implied warranty of
  *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  *         General Public License for more details.
  * 
  *         You should have received a copy of the GNU General Public License
  *         along with this program; if not, write to the Free Software
  *         Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 public class ModelUtilTest extends TestCase {
 
     @Test
     public void testSingleTask() {
 	Task t = new CommandView();
 	t.setId("s1");
 	t.setCommand("echo 1");
 	t.setLastExecute(new GregorianCalendar());
 	t.setLastResult("1");
 	t.setRegexOk("0");
 	t.setRegexWarn("1");
 
 	InfoTask i = ModelUtil.convert2InfoTask(t);
 
 	assertTrue(i.getCommandText().equals(t.getCommand()));
 	assertTrue(i.getStatusText().equals("WARNING"));
 	assertTrue(i.getLastResultText().equals(t.getLastResult()));
 	assertNotNull(i.getLastDateText());
 	assertTrue(i.toString().length() > 100);
 
 	InfoTaskHTML h = ModelUtil.convert2InfoTaskHTML(t);
 	assertTrue(h.getLastResultHTML().indexOf(t.getLastResult()) >= 0);
 	assertNotNull(h.getLastDateText());
 	assertTrue(h.getLastDateHTML().indexOf(h.getLastDateText()) >= 0);
 	assertTrue(h.getRequestButtonHTML().indexOf("button") >= 0);
 	assertTrue(h.toString().length() > 100);
     }
 
     @Test
     public void testMapToInfoList() {
 	Task t1 = new CommandView();
 	Task s2 = new SqlView();
 
 	t1.setId("t1");
 	s2.setId("s2");
 	t1.setLastResult("t1res");
 	s2.setLastResult("s2res");

 	HashMap<String, Task> map = new HashMap<String, Task>();
 	map.put(t1.getId(), t1);
 	map.put(s2.getId(), s2);
 
	InfoTask[] list = ModelUtil.convert2InfoTask(map.values().toArray());
 
 	assertTrue(list[0].getIdText().equals("s2"));
 	assertTrue(list[1].getIdText().equals("t1"));
 	assertTrue(list[0].getLastResultText().equals("s2res"));
 	assertTrue(list[1].getLastResultText().equals("t1res"));
 
	InfoTaskHTML[] listHTML = ModelUtil.convert2InfoTaskHTML(map.values().toArray());
 
 	assertTrue(listHTML[0].getIdText().equals("s2"));
 	assertTrue(listHTML[1].getIdText().equals("t1"));
 	assertTrue(listHTML[0].getLastResultHTML().indexOf("s2res") > 0);
 	assertTrue(listHTML[1].getLastResultHTML().indexOf("t1res") > 0);
     }
 }
