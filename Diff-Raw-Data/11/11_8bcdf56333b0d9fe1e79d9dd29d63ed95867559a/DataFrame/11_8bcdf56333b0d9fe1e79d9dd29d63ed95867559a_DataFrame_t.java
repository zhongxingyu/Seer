 import javax.swing.JFrame;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.BorderLayout;
 
 public class DataFrame extends JFrame {
 
    volatile boolean stopped;
     FFT fft;
     GetAudio audioinput;
     Histogram hist;
    private Thread histthread;
 
     public DataFrame() {
         setTitle("Histogram");
         stopped = true; 
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         JButton start = new JButton("Start");
         JButton stop = new JButton("Stop");
         hist = new Histogram(this);	
         
        //starts recording, if recording has not already been started.
 
         start.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(stopped == true) {
                     stopped = false;
                     audioinput = new GetAudio(DataFrame.this);
                     audioinput.setup(); //method to initialize the audio listener. If it can't set up a TargetDataLine, it will set stopped to true, preventing the rest of this method from executing.
                     if(stopped == false) {
                         fft = new FFT(DataFrame.this);
                         fft.start(); //starting fft before audioinput to ensure that there is a queue for audioinput to stick chunks of data in
                         histthread = new Thread(hist,"histthread");
                         audioinput.start();
                         histthread.start();
                     }
                 }
             }
         });
     
         //this one does the exact opposite: stops recording if it's currently running.
 
         stop.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(stopped == false) {
                     stopped = true;
                     try { 
                         fft.interrupt(); //Interrupts fft thread if it was waiting on more data from the queue.
                     }
                     catch(NullPointerException ex) {
                         //these should only print if you set up audio, but it can't start.
                         System.out.println("fft does not exist");
                     }
                     try {
                         histthread.interrupt();
                     }
                     catch(NullPointerException ex) {
                         System.out.println("histthread does not exist");
                     }
                 }
             }
         }
         );
 
         //buttons are, for now, in BorderLayout. Could do a different layout, but this was easy.
 
         add(start, BorderLayout.NORTH);
         add(stop, BorderLayout.SOUTH);
         add(hist, BorderLayout.CENTER);
         pack();
         setVisible(true);
     }
 
     public void setStopped(boolean val) {
         stopped = val;
     }
 
     public boolean isStopped() {
         return(stopped);
     }
 
     public static void main(String[] args) {
         DataFrame foo = new DataFrame();
     }
 }
