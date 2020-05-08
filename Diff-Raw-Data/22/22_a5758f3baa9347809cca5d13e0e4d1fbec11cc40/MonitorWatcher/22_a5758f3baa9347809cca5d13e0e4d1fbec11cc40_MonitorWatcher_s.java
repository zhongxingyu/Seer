 /*
  * jPOS Project [http://jpos.org]
  * Copyright (C) 2000-2010 Alejandro P. Revilla
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.core.monitor;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /** A Watcher is a monitor instance of a monitor that keep the state of
  * the watching process so it can return the lines considered as "new" since
  * the last lines the caller obtained.
  * 
  * @author jpaoletti jeronimo.paoletti@gmail.com
  * @see http://github.com/jpaoletti/jPOS-Presentation-Manager
  * 
  * */
 public class MonitorWatcher {
 	private Monitor monitor;
 	private Object actual = null;
 	
 	public MonitorWatcher(Monitor monitor) {
 		super();
 		this.setMonitor(monitor);
 	}
 
 	public String startWatching() throws Exception{
 		MonitorLine line = getMonitor().getSource().getLastLine();
 		actual = (line!=null)?line.getId():null;
 		return getMonitor().getFormatter().format(line);
 	}
 	
 	public List<String> getNewLines() throws Exception{
 		List<MonitorLine> lines = getMonitor().getSource().getLinesFrom(actual);
 		List<String> result = new ArrayList<String>();
 		for (MonitorLine line : lines) {
 			result.add(getMonitor().getFormatter().format(line));
 		}
		actual = (lines.size()==0)?null:lines.get(lines.size()-1).getId();
 		return result;
 	}
 
 	public void setMonitor(Monitor monitor) {
 		this.monitor = monitor;
 	}
 
 	public Monitor getMonitor() {
 		return monitor;
 	}
 
 }
