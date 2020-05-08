 public class GuitarString {
     private RingBuffer ringBuffer;
     private int count;
     private double volume;
     private double frequency ;
     private int SAMPLING_RATE = 44100 ;
 
     public GuitarString(double f) {
         frequency = f ; 
         count = 0;
         volume = 1.0;
         int capacity = (int)Math.round(SAMPLING_RATE / frequency);
         ringBuffer = new RingBuffer(capacity);
         mute();
     }
     
     // set the buffer to white noise
     void pluck() {
         ringBuffer.clear();
         while (!ringBuffer.isFull())
             ringBuffer.enqueue(Math.random() - 0.5);
     }
 
     // fill the buffer with zeros
     public void mute() {
     	ringBuffer.clear();
         while (!ringBuffer.isFull())
             ringBuffer.enqueue(0.0); // void mute()
     }
 
     // change the length of the buffer according to the number of frets
     void pressFretDown(int fret) {
         double newFrequency = frequency * Math.pow(2.0, fret / 12.0) ;
 		int capacity = (int)Math.round(SAMPLING_RATE / newFrequency);
         ringBuffer = new RingBuffer(capacity) ;
		mute();
     }
 
     // advance the simulation one time step
     void tic() {
         double a = ringBuffer.dequeue();
         double b = ringBuffer.peek();
         a = (a+b) / 2;
         a *= 0.996;
 
         ringBuffer.enqueue(a);
         count++;
     }
     
     // set new volume
     public void setVolume(double vol) {
     	volume = vol;
     }
 
     // return the current sample
     public double sample() {
         return ringBuffer.peek() * volume;
     }
     // return number of tics
     public int time() {
         return count;
     }
 }
