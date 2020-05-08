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
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import ditl.Bus;
 import ditl.IdGenerator;
 import ditl.Listener;
 import ditl.Matcher;
 import ditl.StatefulReader;
 import ditl.StatefulWriter;
 import ditl.Trace;
 import ditl.Units;
 
 public class NS2Movement {
 
     public static void fromNS2(MovementTrace movement,
             InputStream in, Long maxTime, double timeMul, long ticsPerSecond,
             long offset, final boolean fixPauseTimes, IdGenerator idGen) throws IOException {
 
         final StatefulWriter<MovementEvent, Movement> movementWriter = movement.getWriter();
         final Map<Integer, Movement> positions = new HashMap<Integer, Movement>();
         final BufferedReader br = new BufferedReader(new InputStreamReader(in));
         final Bus<MovementEvent> buffer = new Bus<MovementEvent>();
         String line;
         long last_time = Long.MIN_VALUE;
 
         while ((line = br.readLine()) != null)
             if (!line.isEmpty()) {
                 Integer id;
                 String id_str;
                 double s;
                 Movement m;
                 long time;
                 final String[] elems = line.split("\\p{Blank}+");
 
                 if (line.startsWith("$node")) {
                     id_str = elems[0].split("\\(|\\)")[1];
                     id = idGen.getInternalId(id_str);
                     final double c = Double.parseDouble(elems[3]);
 
                     m = positions.get(id);
                     if (m == null) {
                         m = new Movement(id, new Point(0, 0));
                         positions.put(id, m);
                     }
 
                     if (elems[2].equals("X_"))
                         m.x = c;
                     else if (elems[2].equals("Y_"))
                         m.y = c;
 
                 } else if (line.startsWith("$ns")) {
                     time = (long) (Double.parseDouble(elems[2]) * timeMul) + offset;
                     if (time > last_time)
                         last_time = time;
                     id_str = elems[3].split("\\(|\\)")[1];
                     id = idGen.getInternalId(id_str);
                     final Point dest = new Point(Double.parseDouble(elems[5]), Double.parseDouble(elems[6]));
                    s = Double.parseDouble(elems[7].substring(0, elems[7].length() - 1)) / timeMul;
                     buffer.queue(time, new MovementEvent(id, s, dest));
                 }
             }
         br.close();
 
         movementWriter.setInitState(offset, positions.values());
 
         buffer.addListener(new Listener<MovementEvent>() {
             @Override
             public void handle(long time, Collection<MovementEvent> events) throws IOException {
                 for (final MovementEvent mev : events) {
                     final Movement m = positions.get(mev.id); // current
                                                               // movement
                     m.handleEvent(time, mev);
                     movementWriter.removeFromQueueAfterTime(time, new Matcher<MovementEvent>() {
                         @Override
                         public boolean matches(MovementEvent item) {
                             return (item.id == mev.id);
                         }
                     });
                     movementWriter.queue(time, mev);
                     if (fixPauseTimes)
                         movementWriter.queue(m.arrival, new MovementEvent(mev.id, 0, mev.dest)); // queue
                                                                                                  // the
                                                                                                  // waiting
                                                                                                  // time
                                                                                                  // as
                                                                                                  // well
                 }
                 movementWriter.flush(time);
             }
         });
 
         buffer.flush();
 
         last_time = (maxTime != null) ? maxTime : last_time;
         movementWriter.setProperty(Trace.maxTimeKey, last_time);
         movementWriter.setProperty(Trace.timeUnitKey, Units.toTimeUnit(ticsPerSecond));
         idGen.writeTraceInfo(movementWriter);
         movementWriter.close();
     }
 
     public static void toNS2(MovementTrace movement, OutputStream out, double timeMul) throws IOException {
 
         final StatefulReader<MovementEvent, Movement> movementReader = movement.getReader();
         final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
 
         movementReader.seek(movement.minTime());
         for (final Movement mv : movementReader.referenceState())
             bw.write(mv.ns2String());
 
         while (movementReader.hasNext())
             for (final MovementEvent mev : movementReader.next())
                 switch (mev.type) {
                     case NEW_DEST:
                         bw.write(mev.ns2String(movementReader.time(), timeMul));
                         break;
                     default:
                         System.err.println("IN and OUT movement events are not supported by NS2");
                 }
         bw.close();
         movementReader.close();
     }
 }
