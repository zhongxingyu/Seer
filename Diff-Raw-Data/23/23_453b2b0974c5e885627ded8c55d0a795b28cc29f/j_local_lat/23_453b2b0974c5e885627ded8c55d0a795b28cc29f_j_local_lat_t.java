 /*
     Copyright (c) 2007-2008 FastMQ Inc.
 
     This file is part of 0MQ.
 
     0MQ is free software; you can redistribute it and/or modify it under
     the terms of the Lesser GNU General Public License as published by
     the Free Software Foundation; either version 3 of the License, or
     (at your option) any later version.
 
     0MQ is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     Lesser GNU General Public License for more details.
 
     You should have received a copy of the Lesser GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 class j_local_lat
 {
      public static void main (String [] args)
      {
          if (args.length != 3) {
             System.out.println ("usage: java j_local_lat <hostname> " +
                  "<message size> <message count>");
              return;
          }
 
          //  Parse the command line arguments.
          String hostname = args [0];
          int messageSize = Integer.parseInt (args [1]);
          int messageCount = Integer.parseInt (args [2]);
 
          //  Print out the test parameters.
          System.out.println ("message size: " + messageSize + " [B]");
          System.out.println ("roundtrip count: " + messageCount);
 
          //  Initialise 0MQ runtime.
          Jzmq obj = new Jzmq (hostname);
 
          //  Create the wiring.
          int eid = obj.createExchange ("EL", Jzmq.SCOPE_LOCAL, null);
          obj.createQueue ("QL", Jzmq.SCOPE_LOCAL, null);
          obj.bind ("EL", "QG");
          obj.bind ("EG", "QL");
 
          //  Get the inital timestamp.
          long start = System.currentTimeMillis ();
 
          for (int i = 0; i != messageCount; i ++) {
              byte data [] = new byte [messageSize];
              obj.send (eid, data);
              byte [] data2 = obj.receive ();
              assert (data == data2);
          }
 
          //  Get the final timestamp.
          long end = System.currentTimeMillis ();
 
          //  Compute and print out the latency.
          double latency = (double) ((end - start) * 1000) / messageCount / 2;
          System.out.println ("Your average latency is " + latency + " [us]");
      }
 }
