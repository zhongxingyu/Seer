import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Scanner;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
class Pair{
 	Integer id;
 	Integer score;
 	public Pair(Integer id, Integer score){
 		this.id = id;
 		this.score = score;
 	}
 	public Integer getScore(){return this.score;}
 	public Integer getID(){return this.id;}
 }
 
 public class Q4 {
 	static AtomicInteger municoes[];
 	static Pair acertos[];
 
	static class Atirador implements Runnable{
 		private int ID;
 		Ajudante ajudante;
 		public Atirador(int ID){
 			this.ID = ID;
 			ajudante = new Ajudante(ID);
 		}
 		public void carrega(){
 			municoes[ID].getAndDecrement();
 		}
 
 		public boolean atira(){
 			if (Math.random() < 0.5)
 				return true;
 			return false;
 		}
 
 		@Override
 		public void run() {
 			ajudante.run();
 			while(true){
 				int ammo = municoes[ID].get();
 				if (ammo > 0){
 					this.carrega();
 					if (this.atira()){
 						Integer score = acertos[ID].getScore();
 						score++;
 					}
 					try {
 						Thread.sleep(350);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if (municoes[ID].get() == 4){
 						municoes[ID].notify();
 					}
 				}
 				else
 				{
 					try {
 						municoes[ID].wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 	}
 
	static class Ajudante implements Runnable{
 		private int ID;
 		public Ajudante(int ID){
 			this.ID = ID;
 		}
 		public void produz(){
 			municoes[ID].getAndIncrement();
 		}
 		@Override
 		public void run() {
 			while(true){
 				int ammo = municoes[ID].get();
 				int sleepTime = ((int) Math.random() * 601) + 100;
 				if (ammo < 5){
 					this.produz();
 					try {
 						Thread.sleep(sleepTime);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 					if (municoes[ID].get() == 1){
 						municoes[ID].notify();
 					}
 				}
 				else{
 					try {
 						municoes[ID].wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 	}
 
 
 	public static void main(String[] args) {
 		Scanner input = new Scanner(System.in);
 		int X = input.nextInt();
 		int K = input.nextInt();
 		municoes = new AtomicInteger[X];
 		acertos = new Pair[X];
 		for (int i = 0; i < X; i++){
 			municoes[i] = new AtomicInteger(2);
 			acertos[i] = new Pair(i, 0);
 		}
 		ExecutorService executor = Executors.newCachedThreadPool();
 		for (int i = 0; i < X; i++) {
 			executor.execute(new Atirador(i));
 		}
 		executor.shutdown();
 		try {
 			executor.awaitTermination(K, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 		}
		Arrays.sort(acertos, new Comparator<Pair>(){
			@Override
			public int compare(Pair o1, Pair o2) {
				return o2.getScore() - o1.getScore();
			}
		});
		System.out.println("ID \t SCORE");
		for (int i = 0; i < X; i++) {
			System.out.println(acertos[i].getID() + " \t " + acertos[i].getScore());
			
		}
 	}
 
 }
