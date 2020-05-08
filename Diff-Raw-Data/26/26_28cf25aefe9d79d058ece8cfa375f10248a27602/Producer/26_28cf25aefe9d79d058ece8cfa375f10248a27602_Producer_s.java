 package ch2.ex3;
 
 public class Producer implements Runnable{
 	
 	private EventStorage storage;
 	public Producer (EventStorage storage){
 		this.storage = storage;
 	}
 	@Override
 	public void run(){
 		for (int i =0; i<100; i++){
 			System.out.println("Consumer:"+i);
 			storage.set();
 		}
 	}
 	
 }
