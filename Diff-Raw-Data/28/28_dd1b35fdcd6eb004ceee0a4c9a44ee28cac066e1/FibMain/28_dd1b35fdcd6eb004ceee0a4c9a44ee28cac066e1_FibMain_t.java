 package fr.imag.adele.apam.fibonacci;
 
 import fr.imag.adele.apam.ApamComponent;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.util.Util;
 
 public class FibMain implements Runnable, ApamComponent{
 
 	public static int nbInst = 0 ;
 	Fib fib ;
 
 	int nb = 12 ;
 
 	public void run() {
		long deb ;
		int fibResult ;
		long fin ;
		long duree ;
		
 		System.out.println("Starting  fibonacci " + nb );
//		long deb = System.currentTimeMillis() ;
////		int fibResult = fib.compute (nb) ;
//		int fibResult = fib.computeSmart (nb) ;
//		long fin = System.currentTimeMillis() ;
//		long duree = fin - deb ;
//		System.out.println("initialization de " + nbInst + " instances. Duree: " + duree + " milis.  Result=" + fibResult);
////		System.out.println("initialization: " + duree + " milis");
 
 		for (int i = 0 ; i < 20; i++) {
 			deb = System.nanoTime() ;
 			fibResult = fib.computeSmart (nb) ;
 			fin = System.nanoTime() ;
 			duree = fin - deb ;
 			System.out.println("execution de " + nbInst + " appels. Duree: " + duree + " nano");
 		}
 	//	System.out.println("Resultat pour Fibonacci " + nb + " : " + fibResult);
 	}
 
 
 	public void apamInit(Instance apamInstance ) {
 		String param = apamInstance.getProperty("param") ;
 		if (param != null) {
 			try {
 				String[] params = Util.split(param) ;
 				int n = Integer.parseInt(params[0]);
 				nb = n ;
 			}
 			catch (Exception e) {
 				System.out.println("parameter invalid; not an integer: param=" + param );
 			}
 		}
 		System.out.println("Starting new fibonacci " + nb );
 		new Thread(this, "APAM perf test").start();
 	}
 
 	public void apamRemove() {
 	}
 
 	public void wiredFor(String resource) {
 	}
 
 	public void unWiredFor(String resource) {
 	}
 
 
 }
