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
 
 class j_local_thr
 {
      public static void main (String [] args)
      {
          if (args.length != 4) {
             System.out.println ("usage: java LocalThr <hostname> " +
                  "<interface> <message size> <message count>");
              return;
          }
 
          //  Parse the command line arguments.
          String hostname = args [0];
          String inInterface = args [1];
          long messageSize = Integer.parseInt (args [2]);
          long messageCount = Integer.parseInt (args [3]);
 
          //  Print out the test parameters.
          System.out.println ("message size: " + messageSize + " [B]");
          System.out.println ("message count: " + messageCount);
 
          //  Initialise 0MQ runtime.
          Jzmq obj = new Jzmq (hostname);
 
          //  Create the wiring.
          obj.createQueue ("QG", Jzmq.SCOPE_GLOBAL, inInterface);
 
          //  Receive the first message from RemoteThr.
          byte [] data = obj.receive ();
          assert (data.length == messageSize);
 
          //  Get the inital timestamp.
          long start = System.currentTimeMillis ();
 
          //  Receive the remaining messages from RemoteThr.
          for (int i = 1; i != messageCount; i ++) {
              data = obj.receive ();
              assert (data.length == messageSize);
          }
 
          //  Get the final timestamp.
          long end = System.currentTimeMillis ();
 
          //  Compute and print out the throughput.
          long messageThroughput = 1000 * messageCount / (end - start);
          long megabitThroughput = messageThroughput * messageSize * 8 / 1000000;
          System.out.println ("Your average throughput is " + messageThroughput +
              "[msg/s]");
          System.out.println ("Your average throughput is " + megabitThroughput +
              "[Mb/s]");        
      }
 }
