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
 
 class j_remote_lat
 {
      public static void main (String [] args)
      {
          if (args.length != 5) {
             System.out.println ("usage: java RemoteLat <hostname> " +
                  "<in interface> <out interface> <message size> " +
                  "<message count>");
              return;
          }
 
          //  Parse the command line arguments.
          String hostname = args [0];
          String inInterface = args [1];
          String outInterface = args [2];
          int messageSize = Integer.parseInt (args [3]);
          int messageCount = Integer.parseInt (args [4]);
 
          //  Initialise 0MQ runtime.
          Jzmq obj = new Jzmq (hostname);
 
          //  Create the wiring.
          int eid = obj.createExchange ("EG", Jzmq.SCOPE_GLOBAL, outInterface);
          obj.createQueue ("QG", Jzmq.SCOPE_GLOBAL, inInterface);
 
          //  Bounce the messages back to LocalLat
          for (int i = 0; i != messageCount; i ++) {
              byte [] data = obj.receive ();
              assert (data.length == messageSize);
              obj.send (eid, data);
          }
 
          //  Wait a while before exiting.
          try {
              Thread.sleep (2000);
          }
          catch (InterruptedException e) {
              e.printStackTrace ();
          }
      }
 }
