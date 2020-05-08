 new Thread() {
     public void run() {
         /* We create a DatagramConnection */
         DatagramConnection dgConnection = null;
         Datagram dg = null;
         try {
             dgConnection =
                 (DatagramConnection) Connecor.open("radiogram://broadcast:37");
             /* Ask for a datagram with the maximum size allowed */
             dg = dgConnection.newDatagram(dgConnection.getMaximumLength());
         } catch (IOException ex) {
             System.out.println("Could not open radiogram connection");
             return;
         }
 
         while (true) {
             try {
                 /*
                  * If there is a change in direction, send the direction
                 * in the form of an integer to an other Sunspot.
                  */
                 if (result != result_old) {
                     result_old = result;
                     dg.reset();
                     dg.writeInt(result);
                     dgConnection.send(dg);
                     System.out.println("Broadcast is going through");
                 }
             } catch (IOException ex) { }
         }
     }
 }.start();
