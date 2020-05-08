 class GuitarString {
 
     /**
      *   void pluck()                  // set the buffer to white noise
      void mute()                          // fill the buffer with zeros
      void pressFretDown(int fret)         // change the length of the buffer according to the number of frets
      void tic()                           // advance the simulation one time step
      double sample()                        // return the current sample
      int time()                          // return number of tics
     */
 	
     private double SamplingRate =  44100.; //Sammpling Rate einer Audio-CD 
     public RingBuffer buffer;
     public int time;
     private double frequenz = 0;
     private double Energiefaktor = 0.996;
 	
 	
     /**
      * Der Konstruktor erschafft einen RingBuffer buffer der Laenge (int)(frequenz/sampling rate)
      * @param frequency
      */
     GuitarString(double frequency) {
 	int bufferLaenge = (int) (SamplingRate/frequency);       
 	this.buffer = new RingBuffer(bufferLaenge);
 	mute();                                        
 	this.time = 0;
 	this.frequenz = frequency;
     }
 
     /**
      * set the buffer to white noise
      * @return
      */
     public void pluck()    {              
 	while (!this.buffer.isEmpty()){
 	    this.buffer.dequeue();           //erstes Element wird gelÃ¶scht (solange, bis alle gelÃ¶scht sind)
 	}
 	while (!this.buffer.isFull()){
 	    this.buffer.enqueue(Math.random() - 0.5);
 	}	
     }	
 	
     /**
      * fill the buffer with zeros
      * @return
      */
     public void mute()    {                   
 	while (!this.buffer.isEmpty()){
 	    this.buffer.dequeue();           //erstes Element wird gelÃ¶scht (solange, bis alle gelÃ¶scht sind)
 	}
 	while (!this.buffer.isFull()){
 	    this.buffer.enqueue(0.);
 	}			
     }	
 	
 	
     /**
      * change the length of the buffer according to the number of frets
      * @param fret KP was das ist
      */
     public void pressFretDown(int fret)  {       // 
	double[] hbuffer = new double[this.buffer.size()];
 	int i = 0;
 	while (!this.buffer.isEmpty()){
 	    hbuffer[i]=this.buffer.dequeue();
 	}
 	this.frequenz = this.frequenz * Math.pow(2.0, fret / 12.0);
 	int bufferLaenge= (int) (SamplingRate/this.frequenz);
 	this.buffer = new RingBuffer(bufferLaenge)	;
 	mute();
     }	
 	
     /**
      * advance the simulation one time step
      * @return
      */
     public void tic()  {                         
 	if (this.buffer.size >1){
 	    double average = (this.buffer.dequeue()+this.buffer.peek())/2.;
 	    this.buffer.enqueue(average* this.Energiefaktor);
 	} else System.out.print("tic: buffer ist leer");
 	this.time++;
 		
     }	
 
     /**
      * return the current sample
      * @return
      */
     public double sample()   {                    
 	return this.buffer.peek();
     }	
 	
 }
