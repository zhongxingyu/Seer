 import java.util.ArrayDeque;
 import java.util.Collections;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicInteger;
 
 
 public class MainClass {
 
 	//first,end,prof
 	//static Deque<Integer> result = new ArrayDeque<>();
 	static List<Integer> result = new LinkedList<>();
 	
 	static CountDownLatch latch ;
 	static CountDownLatch latch2 ;
 	
 	public static void main(String[] args) throws InterruptedException {
 		
 		final int plotsProfitability [] = new int[args.length-1];
 		for(int i = 1; i < args.length; i++){
 		 plotsProfitability[i-1]= Integer.valueOf(args[i]);
 		}
 		
 		for(Integer i : plotsProfitability)
 			System.out.println(i);
 		
 		int nrOfThreads = Runtime.getRuntime().availableProcessors();
 		int plotsForSale = Integer.parseInt(args[0]);
 		latch=new CountDownLatch(nrOfThreads);
 		
 		boolean oddInterval = false;
		if(plotsProfitability.length%2!=0) 
 			oddInterval=true; 
 		
 		final int step = plotsProfitability.length/nrOfThreads; 
 		System.out.println(step);
 		
 		for(int i = 0 ; i < nrOfThreads; i++ ){
 			final int i_final = i;
			final boolean oddIntervar_final=oddInterval;
 			new Thread(new Runnable() {
 				
 				@Override
 				public void run() {
 					int start = i_final*step;
					int end;
					if(oddIntervar_final)
						end = (i_final+1)*step+1;
					else
						end = (i_final+1)*step;
 					
 					for(int i = 0; i < plotsProfitability.length; i++ ){
 						for(int j = start ; j < end; j++){	
 							
 							if(i<=j){
 								int profitability = countProfitability(i,j);
 							
 								synchronized (result) {
 									result.add(i);
 									result.add(j);
 									result.add(profitability);
 								}
 							}
 						//System.out.println(i +""+ Thread.currentThread().getName()+ " -> " 
 							//				+ plotsProfitability[i]);
 						}
 					}
 					latch.countDown();
 				}
 
 				private int countProfitability(int i, int j) {
 					
 					System.out.println("count profitability from : " + i + " to : " + j  );
 					int sum=0;
 					for(;i<=j;i++)
 						sum+=plotsProfitability[i];
 					System.out.println(" = "+ sum );
 					return sum;
 				}
 			}).start();
 		}
 		
 		latch.await();
 		System.out.println(result);
 		
 		
 		final List<Integer> bestProfitable = new LinkedList<>();
 		latch2=new CountDownLatch(1);
 		//search for most profitable plots
 		
 	
 		
 		new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				
 				Integer maxProfit = Collections.max(result);
 				
 				int sizeOfList = result.size();
 				for(int i = 2 ; i < sizeOfList; i+=3){
 					if(result.get(i).equals(maxProfit))
 						bestProfitable.add(i);
 				}
 				latch2.countDown();
 				
 			}
 		}).start();
 		
 		//show most profitable
 		int currentSpan=plotsForSale;
 		int indexFinal=0;;
 		latch2.await();
 		for(Integer i : bestProfitable){
 			int span = result.get(i-1) - result.get(i-2);
 			if(currentSpan>span)
 				indexFinal=i;
 				
 		}
 		//change for human numbering
 		int startOfResult = result.get(indexFinal-2) +1;
 		int endOfResult = result.get(indexFinal-1) + 1;
 		System.out.println(startOfResult + " " + endOfResult + " " 
 							+ result.get(indexFinal)); 
 		
 		
 		
 	}
 	
 	
 
 }
