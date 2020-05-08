 package ejercicios_ut3;
 
 import java.util.Scanner;
 
 public class Ej_condicionales7 {
 
 	public static void main(String[] args) throws InterruptedException {
 		/*
 		 * El juego de piedra/papel/tijera.
 		 *  Haz un programa para jugar a piedra/papel/tijera con el ordenador.
 		 *   Solo una ronda por ejecucin.
 		 *   Para obtener un nmero aleatorio en Java entre 1 y 3
 		 *   resultado = (int )(Math.random() * 3 + 1);
 		 */
 		Scanner teclado = new Scanner(System.in);
 		final int mijugada =(int)(Math.random()*3+1);
 		int tujugada=0;
 		int sumajugadas=0; // para guardar la suma de las jugadas
 		String mimano="";
 	    String tumano="";
 	    String ganador="EMPATE"; // guardar GANO YO o GANAS T O EMPATE
 	    String verbo=""; // guardar el VERBO de accion (MACHACA, ENVUELVE, CORTA )
 	    String parte1=" TU " ;//guardar MI o TU... segun el caso"
 	    String parte2=" MI " ; //guardar MI o TU... segun el caso
 	    
 	    
 	    
 
 		System.out.println("PIEDRA...");
 		Thread.sleep(200);
 		System.out.println("PAPEL...");
 		Thread.sleep(200);
 		System.out.println("O TIJERA...");
 		Thread.sleep(200);
 		System.out.println("1...2...3...?");
 		
 		tujugada = teclado.nextInt();
 		teclado.close();
 		
		if (mijugada >3 || tujugada <1){
 			System.out.println("Recuerda slo puedes usar 1, 2 o 3 ");
 		} else 
 		{
 			// asigno la palabra de la "mano del ordenador" y "la del  jugador"
 			switch (mijugada) {
 			case 1:
 					mimano=" PIEDRA ";
 				break;
 			case 2:
 					mimano=" PAPEL";
 				break;
 			case 3:
 					mimano=" TIJERA ";
 				break;
 			default:
 			break;
 			} // fin del primer switch
 			switch (tujugada) {
 			case 1:
 					tumano=" PIEDRA ";
 				break;
 			case 2:
 					tumano=" PAPEL ";
 				break;
 			case 3:
 					tumano=" TIJERA ";
 				break;
 			default:
 			break;
 			} // fin del segundo switch
 			// mostramos las manos
 			System.out.println("Yo he sacado:"+mimano+" tu has sadado"+tumano+" asi que...");
 			
 			
 			 if (mijugada==tujugada) {
 				 ganador=" EMPATAN ";
 				 System.out.println("\n"+parte1+" "+tumano+" y "+parte2+" "+tumano+" ... "+ganador);
 				 //determinacin del empate
 				 }
 			 else { // vamos con el resto de casos (1+2 2+1 piedra y papel o viceversa ...  (2+3 3+2 papel-tijera o viceversa.. 
 				    // ( 1+3 3+1 piedra tijera o viceversa...
 				 sumajugadas=mijugada+tujugada;
 				 switch (sumajugadas) {
 				 case 3: //1+2 o 2+1  --- piedra y papel, o papel y piedra
 					 		verbo=" ENVUELVE ";
 					 	if (mijugada<tujugada) { //YO PIEDRA,TU PAPEL
 					 		ganador=" GANAS TU";
 					 		parte1=" TU "+tumano+verbo;
 					 		parte2=" MI "+mimano;
 					 	}else{
 					 		ganador=" GANO YO";
 							parte1=" MI "+mimano+verbo;
 							parte2=" TU "+tumano;
 					 	}
 					 break;
 				case 4: //1+3 o 3+1  -- piedra y tijera, o tijera y piedra
 					verbo=" MACHACA ";
 				 	if (mijugada>tujugada) { //YO PIEDRA,TU PAPEL
 				 		ganador=" GANAS TU";
 				 		parte1=" TU "+tumano+verbo;
 				 		parte2=" MI "+mimano;
 				 	}else{
 				 		ganador=" GANO YO";
 						parte1=" MI "+mimano+verbo;
 						parte2=" TU "+tumano;
 				 	}
 					 break;
 					 
 				case 5: //2+3 o 3+2  -- papel y tijera, o tijera y papel
 					verbo=" CORTA ";
 				 	if (mijugada<tujugada) { //YO PIEDRA,TU PAPEL
 				 		ganador=" GANAS TU";
 				 		parte1=" TU "+tumano+verbo;
 				 		parte2=" MI "+mimano;
 				 	}else{
 				 		ganador=" GANO YO";
 				 		parte1=" MI "+mimano+verbo;
 						parte2=" TU "+tumano;
 					break;
 				 }
 				 }
 				 	 System.out.println("\n"+parte1+parte2+" ... "+ganador);
 		
 				 } // cierre del  el del else
 			 
 			
 		}// cierre del IF (primero)
 	
 	
 		
 	}
 
 }
