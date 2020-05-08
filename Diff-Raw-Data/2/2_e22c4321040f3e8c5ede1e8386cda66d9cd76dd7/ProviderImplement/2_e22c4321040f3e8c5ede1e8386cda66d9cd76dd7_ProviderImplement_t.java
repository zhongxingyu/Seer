 package BOB.Cloud.provider;
 
 import java.util.Random;
 import java.util.concurrent.BlockingQueue;
 
 import BOB.Cloud.provider.Provider;
 
 /**
  * @author  syncc
  */
 public class ProviderImplement implements Provider{
 	/*
 	 * 큐가 가진 배열의 길이는 1000개 이고 배열에 들어가는 자료형은 String 입니다.
 	 * 큐에는 절대 Null은 저장하지 마세요 NullPointerException이 발생합니다.
 	 * 사용할 메서드는 put(1)-삽입, take(0)-빼냄 [큐가 가득 차거나 비었을 때 대기하는 메서드]	
 	 */
 	private BlockingQueue<String> queue;
 	/**
 	 * @uml.property  name="logProducer"
 	 * @uml.associationEnd  
 	 */
 	private LogProducer logProducer;
 	
 	/* 로그를 몇개나 만들지,, 생성자에서 넣어줍니다. */
 	private int logNum;
 	
 	/* 로그를 만들 포맷을 지정합니다 */
 	private String logFormat;
 	
 	/*비정상 로그를 출력할 확률을 지정합니다*/
 	private int abnormalRandomValue;
 	
 	public ProviderImplement(int _logNum, BlockingQueue<String> _queue, String _logFormat, int _abnormalRandomValue){
 		this.logNum = _logNum;
 		this.queue 	= _queue;
 		this.logFormat = _logFormat;
 		this.abnormalRandomValue = _abnormalRandomValue; 
 		this.logProducer = new LogProducer();
 	}
 
 	Random randomValue = new Random();
 	boolean randomFlag;
 	
 	/* 스레드를 만들어 줍니다. */
 	public void run() {
 		for(int i =0; i < logNum; i++){
 			try{
 				if(randomValue.nextInt(100) <= abnormalRandomValue){
 					randomFlag = false;
 				}else{
 					randomFlag = true;
 				}
 				//System.out.print(randomFlag);
 				System.out.println(this.logProducer.ModelParser(randomFlag, "random"));
				//queue.put(this.logProducer.ModelParser(false, "random"));
 				//System.out.println(queue.size());
 
 				//System.out.println(queue.take());
 			} catch (Exception e){
 				e.printStackTrace();
 			} 
 		}
 		//System.out.println("ProviderImplement thread End");
 		
 	}	
 	
 	
 }
