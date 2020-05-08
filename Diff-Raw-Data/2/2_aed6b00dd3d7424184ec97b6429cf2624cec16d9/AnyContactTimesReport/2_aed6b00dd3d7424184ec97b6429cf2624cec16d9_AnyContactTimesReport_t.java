 /*******************************************************************************
  * This file is part of DITL.                                                  *
  *                                                                             *
  * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
  *                                                                             *
  * DITL is free software: you can redistribute it and/or modify                *
  * it under the terms of the GNU General Public License as published by        *
  * the Free Software Foundation, either version 3 of the License, or           *
  * (at your option) any later version.                                         *
  *                                                                             *
  * DITL is distributed in the hope that it will be useful,                     *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  * GNU General Public License for more details.                                *
  *                                                                             *
  * You should have received a copy of the GNU General Public License           *
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
  *******************************************************************************/
 package ditl.graphs;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ditl.Listener;
 import ditl.Report;
 import ditl.ReportFactory;
 import ditl.StatefulListener;
 
 public final class AnyContactTimesReport extends Report implements EdgeTrace.Handler {
 
     private final boolean _contacts;
     private final Map<Integer, Integer> edge_count = new HashMap<Integer, Integer>();
     private final Map<Integer, Long> active_nodes = new HashMap<Integer, Long>();
 
     public AnyContactTimesReport(OutputStream out, boolean contacts) throws IOException {
         super(out);
         _contacts = contacts;
        appendComment("id | begin | end | duration");
     }
 
     public static final class Factory implements ReportFactory<AnyContactTimesReport> {
         private final boolean _contacts;
 
         public Factory(boolean contacts) {
             _contacts = contacts;
         }
 
         @Override
         public AnyContactTimesReport getNew(OutputStream out) throws IOException {
             return new AnyContactTimesReport(out, _contacts);
         }
     }
 
     @Override
     public Listener<EdgeEvent> edgeEventListener() {
         return new Listener<EdgeEvent>() {
             @Override
             public void handle(long time, Collection<EdgeEvent> events) throws IOException {
                 for (final EdgeEvent event : events) {
                     final Edge e = event.edge();
                     if (event.isUp()) {
                         incr(time, e.id1, 1);
                         incr(time, e.id2, 1);
                     } else {
                         incr(time, e.id1, -1);
                         incr(time, e.id2, -1);
                     }
                 }
             }
         };
     }
 
     @Override
     public Listener<Edge> edgeListener() {
         return new StatefulListener<Edge>() {
             @Override
             public void handle(long time, Collection<Edge> events) throws IOException {
                 if (_contacts)
                     for (final Edge e : events) {
                         incr(time, e.id1, 1);
                         incr(time, e.id2, 1);
                     }
             }
 
             @Override
             public void reset() {
                 active_nodes.clear();
                 edge_count.clear();
             }
         };
     }
 
     private void incr(long time, Integer id, int diff) throws IOException {
         if (!edge_count.containsKey(id)) {
             edge_count.put(id, diff);
             if (_contacts)
                 active_nodes.put(id, time);
             else {
                 final Long t = active_nodes.remove(id);
                 if (t != null)
                     append(id + " " + t + " " + time + " " + (time - t));
             }
 
         } else {
             Integer c = edge_count.get(id);
             c += diff;
             if (c.equals(0)) {
                 edge_count.remove(id);
                 if (_contacts) {
                     final Long t = active_nodes.remove(id);
                     append(id + " " + t + " " + time + " " + (time - t));
                 } else
                     active_nodes.put(id, time);
             } else
                 edge_count.put(id, c);
         }
     }
 }
