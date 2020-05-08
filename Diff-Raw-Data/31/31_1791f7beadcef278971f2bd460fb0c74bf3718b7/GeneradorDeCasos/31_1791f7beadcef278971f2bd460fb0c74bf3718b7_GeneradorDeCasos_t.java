 package generadorDeCasos;
 
 import grafo.Grafo;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.DecimalFormat;
 import java.util.Random;
 import java.util.Scanner;
 
 
 /**
  * Generador de casos del TP de Coloreo
  */
 public class GeneradorDeCasos {
 
 	public static void main(String[] args) {
 
 		// Definicion de constantes
 		final String path = "LoteDePruebas/InputGenerados/";
 		final String extension = ".in";
 
 		// DECLARACION DE VARIABLES:
 		Scanner console = new Scanner(System.in);
 		String fileName = null;
 		FileWriter salida = null;
 		PrintWriter psalida = null;
 
 		Grafo grafo = null; // Grafo
 		int N = 0; // Cantidad de nodos
 		double p = 0; // probabilidad
 		int A = 0; // Cantidad de aristas
 		double adyacencia = 0; // % de adyacencia
 		int gradoMax = 0;
 		int gradoMin = 0; 
 
 		// Se pide al user los datos para generar el caso
 		do {
 			System.out
 					.println("Ingrese la cantidad de nodos \"N\" a generar: ");
 			N = console.nextInt();
 		} while (N <= 0);
 		do {
 			System.out
 					.println("Ingrese el probabilidad \"p\" de que exista una arista entre nodos: ");
 			p = console.nextDouble();
 		} while (p < 0 || p > 1);
 
 		System.out
				.println("Ingrese el nombre que tendra el archivo de este caso (Sin la extensin): ");
 		fileName = console.next();
 
 		console.close();
 
 		System.out.println("Se va a generar un grafo aleatorio G( N= " + N
 				+ ", p= " + p + " )");
 		System.out.println("El nombre del archivo es: " + path + fileName
 				+ extension);
 
 		// Resolucion del problemagradoMax
 
 		grafo = new Grafo(N);
 		for (int i = 0; i < N; i++) {
 			for (int j = i + 1; j < N; j++) {
 
 				// Se obtiene un valor random entre 0.0 y 1.0
 				Random r = new Random();
 				double random = r.nextDouble();
 				if (random <= p) {
 					grafo.setearAdyacencia(i, j);
 					grafo.setearAdyacencia(j, i);
 					A++;
 				}
 			}
 		}
 
 		int aristasPosibles = Math.round((N * (N - 1)) / 2);
 		if (A != 0) {
 			adyacencia = ((double) A / (double) aristasPosibles);
 		}
 		
 		gradoMax = grafo.gradoMax();
 		gradoMin = grafo.gradoMin();
 
 		grafo.mostrarMatriz();
 		
 		// Escritura en el archivo
 		try {
 			// Apertura del archivo de salida
 			salida = new FileWriter(path + fileName + extension);
 			psalida = new PrintWriter(salida);
 
 			// Se imprime la primera linea con el siguiente formato
 			// N Cantidad_de_Aristas %adyacencia Grado_max Grado_min
 
 			psalida.print(N + "\t" + A + "\t%");
 			DecimalFormat f = new DecimalFormat("##.00");
 			psalida.print(f.format(adyacencia * 100));
			psalida.println("\t" + gradoMax + "\t" + gradoMin);
 
 			// Se imprimen los nodos adyacentes
 			for (int i = 0; i < N; i++) {
 				for (int j = 0; j < N; j++) {
 
 					if (grafo.sonAdyacentes(i, j)) {
 						psalida.println(i + "\t" + j);
 					}
 				}
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		} finally {
 			try {
 				System.out.println("Fin del proceso.");
 				if (null != salida)
 					salida.close();
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 }
