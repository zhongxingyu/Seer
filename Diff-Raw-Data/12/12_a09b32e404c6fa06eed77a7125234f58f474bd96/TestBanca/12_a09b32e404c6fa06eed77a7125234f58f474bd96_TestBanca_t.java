 package poo.esempi;
 
 import poo.banca.*;
 
 public class TestBanca {
 	public static void main(String[]args) {
 		Banca carime = new Banca(50); // Banca rurale :D
 		ContoBancario cb1 = new ContoBancario("51/4832/23", 1500);
 		ContoBancario cb2 = new ContoBancario("51/8329/49", 2000);
 		ContoConFido cf1 = new ContoConFido("52/4498/47", 20000, 5000);
 		carime.addConto(cb1); carime.addConto(cb2); carime.addConto(cf1);
 		System.out.println("Lista clienti: ");
 		carime.report();
 		cf1.setFido(10000);
 		if (cb1.preleva(500)) System.out.println("51/4832/23 Corri a pagare l'ENEL!");
 		else System.out.println("51/4832/23 Piangi in silenzio...");
 		carime.regalo();
		if (carime.removeConto(cb2))
 			System.out.println("Addio cliente 51/8329/49");
 		ContoBancario cb3 = carime.getConto("52/4498/47"); // ContoConFido è un ContoBancario! :P
 		System.out.println(cb3); // È un ContoBancario, ma in memoria c'è un ContoConFido, verrà usato il toString completo!
 	} // main
 } // TestBanca
