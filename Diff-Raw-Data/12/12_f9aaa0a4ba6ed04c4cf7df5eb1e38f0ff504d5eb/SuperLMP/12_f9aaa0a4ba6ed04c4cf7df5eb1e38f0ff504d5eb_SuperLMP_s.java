 /*
  * Like LMP, but super
  */
 package lmp;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  *
  * @author Adrian
  */
 public class SuperLMP extends JPanel {
 
     public static Double result;
     public static final int SLOT_LIMIT = 20000; // Run until this many time slots have passed
     public static int AVG_PACKET_SIZE = 512; // Average packet size
     public static final double CONVERSION = 0.015625; // The conversion factor to go from bytes -> time slots
     public static int NUM_OF_STATIONS = 20;
     public static double MICROSECONDS = 51.2;
     public static double lambda = 20.0;
 
     public static void main(String[] args) {
         String s1 = JOptionPane.showInputDialog(null, "Enter the number of stations");
         NUM_OF_STATIONS = Integer.parseInt(s1);
         String s2 = JOptionPane.showInputDialog(null, "Enter the Packet Size");
         AVG_PACKET_SIZE = Integer.parseInt(s2);
         String s3 = JOptionPane.showInputDialog(null, "Enter lambda");
         lambda = Double.parseDouble(s3);
         JFrame frame = new JFrame();
         JPanel boardPanel = new JPanel();
         int throughAverage = 0; // Used for calculating average throughput
        double trafficAverage = 0; // Used for calculating average traffic load
         for (int i = 0; i <= 50; i++) {
             Simulator mario = new Simulator(NUM_OF_STATIONS, lambda);
             double sum = 0; // The summation of sums!
             while ((mario.getPacketsSent() * AVG_PACKET_SIZE * CONVERSION) <= SLOT_LIMIT) {
                 result = mario.send(); // Do an initial send. If it fails, tryAgain(), else, hooray!
                 if (result == -1) {
                     System.out.println("Hooray! You sent at time: " + mario.tryAgain()); // Method tryAgain() internally loops
                 } else {
                     System.out.println("Hooray! You sent at time: " + result);
                 }
 
                 sum = +mario.getSentTime(); // Add the sent times together
                 System.out.println("Packets sent successfully at time " + mario.getSuccessfulPacketsSent());
            }
            // Print out the calculated traffic load
            System.out.println("Calculated traffic load: " + ((NUM_OF_STATIONS * AVG_PACKET_SIZE * 8)/(lambda * MICROSECONDS)));
            // Calculate the average traffic load
            trafficAverage+=(NUM_OF_STATIONS * AVG_PACKET_SIZE * 8)/(lambda * MICROSECONDS);
            
             // Print out the calculated throughput
             System.out.println("Calculated throughput is: " + (mario.getSuccessfulPacketsSent() * AVG_PACKET_SIZE * 512) / (SLOT_LIMIT * 51.2 * Math.pow(10, -6))); //Throughput!
             // Calculate the average throuput
             throughAverage += (mario.getSuccessfulPacketsSent() * 8 * AVG_PACKET_SIZE) / (SLOT_LIMIT * 51.2 * Math.pow(10, -6));
         }
         // I think this is obvious
         System.out.println("Average throughput is: " + throughAverage / 50);
         // So is this
        System.out.println("Average traffic load is: " + trafficAverage/50);
     }
 }
