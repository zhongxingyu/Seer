 package org.min.packkmean;
 
 import java.io.IOException;
 import java.util.Scanner;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		//Cargamos el fichero Properties
 		LoadProperties loadP = new LoadProperties(args[0]);
 		loadP.load();
 		
 		//Cargamos los datos del fichero .arff
 		LoadData loadD = new LoadData(loadP.getData());
 		ListaEntidades lista = loadD.CargarDatos(loadP.getK());
 		
 		//Llamamos a k-means
 		boolean menu1 = false;
 		while(!menu1) {
 			System.out.println("Determine la inicializacion deseada");
 			System.out.println("1.-Aleatoria");
 			System.out.println("2.-Por division de espacio");
 			System.out.println("===================================");
 			Scanner sc = new Scanner(System.in);
 			int psc = sc.nextInt();
 			switch (psc){
 				case 1:
 					menu1 = true;
 					boolean menu2a = false;
 					while(!menu2a) {
 						System.out.println("Determine la iterabilidad");
 						System.out.println("1.-Definido");
 						System.out.println("2.-Automatico");
 						System.out.println("3.-Volver");
 						System.out.println("===================================");
 						Scanner read2 = new Scanner(System.in);
 						int pRead2 = sc.nextInt();
 						switch (pRead2){
 							case 1: 
 								System.out.println("Inserte el numero de ciclos");
 								Scanner read = new Scanner(System.in);
 								int pRead = sc.nextInt();
								Kmeans pKMeans = new KMeans(lista, loadP.getM(), lista.randomSelect(Integer.parseInt(loadP.getK())), pRead);
 								break;
 							case 2: 
 								
 								break;
 							case 3:
 								
 								break;
 							default: 
 								System.out.println("Opcion no valida.");
 								
 						}
 					}
 					break;
 	
 				case 2:
 					menu1 = true;
 					boolean menu2b = false;
 					while(!menu2b) {
 						System.out.println("Determine la iterabilidad");
 						System.out.println("1.-Definido");
 						System.out.println("2.-Automatico");
 						System.out.println("===================================");
 						Scanner read3 = new Scanner(System.in);
 						int pRead3 = sc.nextInt();
 						switch (pRead3){
 							case 1: 
 								menu2b = true;
 								System.out.println("Inserte el numero de ciclos");
 								Scanner read = new Scanner(System.in);
 								int pRead = sc.nextInt();
 								
 								break;
 							case 2:
 								menu2b = true;
 								break;
 							default: 
 								System.out.println("Opcion no valida.");
 								break;
 					}
 					
 					}
 					break;
 					
 				default: 
 					System.out.println("Opcion no valida.");
 					
 					
 			}
 		}
 		ListaEntidades random = lista.randomSelect(Integer.parseInt(loadP.getK()));
 		KMeans pKMeans = new KMeans(lista, loadP.getM(), random);
 	}
 }
