 package AA;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 
 public class P102 {
 
 	private kNN knn;
 
 	/**
 	 * Qué va a puntuar: -Va a mirar el docs a la par que el código para
 	 * corroborar lo que hemos hecho -El estará 10 minutos con cada uno pero
 	 * solo 5 podremos exponer lo hecho -Tendremos que almacenar en disco todo
 	 * lo requerido para clasificar una nueva entrada
 	 */
 
 	public static void main(String[] args) throws IOException {
 
 		P102 pr = new P102();
 		// Aquí están diferentes pruebas. Entrad en los métodos y echar un
 		// vistazo. Son muy fáciles.
 
 		BufferedWriter resultado;
 		resultado = new BufferedWriter(
 				new FileWriter(new File("resultado.csv")));
 		// crearArchivosEquilibrados();
 
 		HashMap<Character, Integer> mapacoCercano = pr.vecinoMasCercano(
 				"salida0Mini.txt", "salida1Mini.txt");
 		HashMap<Character, Integer> mapacoKCercanos = pr.kVecinosMasCercanos(
 				"salida0Mini.txt", "salida1Mini.txt");
 
 		resultado.write("Data sets,Algorithm 1,Algorithm 2\n");
 
 		char letra = 'A';
 		for (int i = 0; i < 26; i++) {
 			System.out.println("Para la " + letra + ":");
 			System.out.println("Con el más cercano tengo: "
 					+ mapacoCercano.get(letra));
 			System.out.println("Con el k vecinos: "
 					+ mapacoKCercanos.get(letra));
 			resultado.write(letra + "," + mapacoCercano.get(letra) + ","
					+ mapacoCercano.get(letra) + "\n");
 			letra++;
 		}
 		resultado.close();
 
 		// pr.PruebasDeKVecinos();
 		// PruebasConTodoVecinoMasCercano();
 		// PruebasConTodoKVecinos();
 
 	}
 
 	P102() {
 		knn = new kNN();
 	}
 
 	void ExportTrainingSet(ArrayList<String> T) {
 		try {
 			OutputStream file = new FileOutputStream("trainingSet.ser");
 			OutputStream buffer = new BufferedOutputStream(file);
 			ObjectOutput output = new ObjectOutputStream(buffer);
 
 			output.writeObject(T);
 			output.close();
 		} catch (Exception ex) {
 
 		}
 
 	}
 
 	ArrayList<String> ImportTrainingSet() {
 		try {
 			InputStream file = new FileInputStream("trainingSet.ser");
 			InputStream buffer = new BufferedInputStream(file);
 			ObjectInput input = new ObjectInputStream(buffer);
 			@SuppressWarnings("unchecked")
 			ArrayList<String> tmp = (ArrayList<String>) input.readObject();
 			input.close();
 			return tmp;
 		} catch (Exception ex) {
 
 		}
 		return null;
 	}
 
 	ArrayList<String> Editing(int k, ArrayList<String> T) { // Training set
 		ArrayList<String> S = new ArrayList<String>(T), // Edited set
 		R = new ArrayList<String>(); // Misclassified set
 		for (String p : S) {
 			knn.setTrainingSet(S);
 			if (p.charAt(0) == getContourClass(p)) { // Misclassified example
 				R.add(p);
 				// Remove example
 			}
 		} // for
 		S.removeAll(R);
 		// Remove all misclassified examples
 		return S;
 	}
 
 	ArrayList<String> CNN(int k, ArrayList<String> T) { // Traning set
 		ArrayList<String> S = new ArrayList<String>(); // CNN set
 		boolean updated;
 		Collections.shuffle(T); // Shuffle array elements
 		do {
 			updated = false;
 			for (String p : T) {
 				if (p.charAt(0) == getContourClass(p)) { // Misclassified
 															// example
 					S.add(p);
 					// It ’s needed
 					updated = true;
 				}
 			}
 		} while (S.size() < T.size() && updated);
 
 		return S;
 	}
 
 	public char getContourClass(String example) {
 
 		knn.inicializarKVecinos();
 		for (String item2 : knn.getTrainingSet()) {
 			int distancia = Levenshtein.computeLevenshteinDistance(
 					example.split(" ")[1], item2.split(" ")[1]);
 			Candidato candidato = new Candidato(item2.split(" ")[0], distancia);
 			knn.checkKVecinos(candidato);
 		}
 		return knn.getMejorCandidatoPonderando().charAt(0);
 	}
 
 	public void createFiveFoldCrossValidation() {
 
 		HashMap<Character, ArrayList<String>> mapaco = new HashMap<Character, ArrayList<String>>();
 		Character inicial = 'A';
 		for (int i = 0; i < 26; i++) {
 			ArrayList<String> letra = new ArrayList<>();
 			mapaco.put(inicial, letra);
 			System.out.println(inicial);
 			inicial++;
 		}
 		try {
 			BufferedReader br1 = new BufferedReader(new FileReader(new File(
 					"Training.200.cad")));
 			String line = br1.readLine();
 			while (line != null) {
 				try {
 					System.out.println(line.charAt(0));
 					mapaco.get(line.charAt(0)).add(line);
 				} catch (Exception ex) {
 				}
 				line = br1.readLine();
 			}
 			br1.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Vamos a hacer archivos!!!!!");
 		Character ini = 'A';
 		try {
 			for (int f = 0; f < 5; f++) {
 				BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
 						"salida" + f + ".txt")));
 				ini = 'A';
 				for (int i = 0; i < 26; i++) {
 					for (int j = 0; j < 40; j++) {
 						System.out.println(mapaco.get(ini).get(0));
 						bw.write(mapaco.get(ini).get(0) + "\n");
 						mapaco.get(ini).remove(0);
 					}
 					ini++;
 				}
 				bw.close();
 			}
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 
 	/**
 	 * Calcular las distancias entre el test y el training usando el k-vecino
 	 * más cercano
 	 * 
 	 * @param ficheroTest
 	 * @param ficheroTraining
 	 * @return
 	 */
 	public HashMap<Character, Integer> kVecinosMasCercanos(String ficheroTest,
 			String ficheroTraining) {
 		HashMap<Character, Integer> mapaco = new HashMap<>();
 
 		// Inicializo el vector de vecinos
 
 		try {
 			Character letra = 'A';
 			// Inicializo el map a 0;
 			for (int i = 0; i < 26; i++) {
 				mapaco.put(letra, 0);
 				letra++;
 			}
 
 			BufferedReader brTraining = new BufferedReader(new FileReader(
 					new File(ficheroTraining)));
 			BufferedReader brTest = new BufferedReader(new FileReader(new File(
 					ficheroTest)));
 			ArrayList<String> lTraining = new ArrayList<>();
 			ArrayList<String> lTest = new ArrayList<>();
 			String line = brTraining.readLine();
 			while (line != null) {
 				lTraining.add(line);
 				line = brTraining.readLine();
 			}
 			line = brTest.readLine();
 			while (line != null) {
 				lTest.add(line);
 				line = brTest.readLine();
 			}
 			char mejorEtiqueta;
 			knn.setTrainingSet(lTraining);
 			for (String item : lTest) {
 				mejorEtiqueta = getContourClass(item);
 				System.out.println("Este tiene la etiqueta "
 						+ item.split(" ")[0] + " y la mínima con kNN es "
 						+ mejorEtiqueta);
 				if (item.split(" ")[0].charAt(0) == mejorEtiqueta) {
 					mapaco.put(item.split(" ")[0].charAt(0),
 							mapaco.get(item.split(" ")[0].charAt(0)) + 1);
 				}
 			}
 			brTraining.close();
 			brTest.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return mapaco;
 
 	}
 
 	/**
 	 * Calcular las distancias entre el test y el training usando el vecino más
 	 * cercano
 	 * 
 	 * @param ficheroTest
 	 * @param ficheroTraining
 	 */
 	public HashMap<Character, Integer> vecinoMasCercano(String ficheroTest,
 			String ficheroTraining) {
 		HashMap<Character, Integer> mapaco = new HashMap<>();
 		try {
 			Character letra = 'A';
 			// Inicializo el map a 0;
 			for (int i = 0; i < 26; i++) {
 				mapaco.put(letra, 0);
 				letra++;
 			}
 
 			BufferedReader brTraining = new BufferedReader(new FileReader(
 					new File(ficheroTraining)));
 			BufferedReader brTest = new BufferedReader(new FileReader(new File(
 					ficheroTest)));
 			ArrayList<String> lTraining = new ArrayList<>();
 			ArrayList<String> lTest = new ArrayList<>();
 			String line = brTraining.readLine();
 			while (line != null) {
 				lTraining.add(line);
 				line = brTraining.readLine();
 			}
 			line = brTest.readLine();
 			while (line != null) {
 				lTest.add(line);
 				line = brTest.readLine();
 			}
 			int distanciaMin = Integer.MAX_VALUE;
 			String etiquetaMin = "";
 			for (String item : lTest) {
 				distanciaMin = Integer.MAX_VALUE;
 				for (String item2 : lTraining) {
 					int distancia = Levenshtein.computeLevenshteinDistance(
 							item.split(" ")[1], item2.split(" ")[1]);
 					if (distancia < distanciaMin) {
 						distanciaMin = distancia;
 						etiquetaMin = item2.split(" ")[0];
 					}
 				}
 				System.out.println("Este tiene la etiqueta "
 						+ item.split(" ")[0] + " y la mínima con 1NN es "
 						+ etiquetaMin);
 				if (item.split(" ")[0].equals(etiquetaMin))
 					mapaco.put(item.split(" ")[0].charAt(0),
 							mapaco.get(item.split(" ")[0].charAt(0)) + 1);
 			}
 			brTraining.close();
 			brTest.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return mapaco;
 	}
 
 
 	private void PruebasDeKVecinos() {
 		kNN knn = new kNN();
 		knn.inicializarKVecinos();
 		// Hago una pruebecica
 		knn.checkKVecinos(new Candidato("G", 10));
 		knn.checkKVecinos(new Candidato("Q", 5));
 		knn.checkKVecinos(new Candidato("O", 3));
 		knn.checkKVecinos(new Candidato("C", 44));
 		knn.checkKVecinos(new Candidato("D", 22));
 		knn.checkKVecinos(new Candidato("O", 2));
 		knn.checkKVecinos(new Candidato("D", 23));
 		// knn.printKVecinos();
 		System.out.println(knn.getMejorCandidatoPonderando());
 	}
 
 	private void PruebasConTodoVecinoMasCercano() {
 
 		Character letra = 'A';
 		BufferedWriter bw = null;
 
 		try {
 			bw = new BufferedWriter(new FileWriter(new File("resultados.txt")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		ArrayList<HashMap<Character, Integer>> listaAciertosParciales = new ArrayList<>();
 		// Dibujo la fila de arriba de la tabla e inicializo los aciertos
 		// globales
 		for (int i = 0; i < 26; i++) {
 			if (i < 25) {
 				System.out.print(letra + "\t");
 				try {
 					bw.write(letra + "\t");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				System.out.print(letra + "\n");
 				try {
 					bw.write(letra + "\n");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			letra++;
 		}
 
 		for (int j = 0; j < 5; j++) {
 			// System.out.println("Usando archivo test: salida"+j+".txt");
 			HashMap<Character, Integer> aciertosGlobales = new HashMap<>();
 			// Inicializo los globales
 			letra = 'A';
 			for (int i = 0; i < 26; i++) {
 				aciertosGlobales.put(letra, 0);
 				letra++;
 			}
 			for (int i = 0; i < 5; i++) {
 				if (i != j) {
 					// System.out.println("Procesando fichero: salida"+i+".txt");
 					HashMap<Character, Integer> aciertos = kVecinosMasCercanos(
 							"salida" + i + ".txt", "salida" + j + ".txt");
 					letra = 'A';
 					for (int k = 0; k < 26; k++) {
 						Integer parcial = aciertos.get(letra);
 						aciertosGlobales.put(letra, aciertosGlobales.get(letra)
 								+ parcial);
 						letra++;
 					}
 					listaAciertosParciales.add(aciertos);
 				}
 			}
 			letra = 'A';
 			for (int i = 0; i < 26; i++) {
 				if (i < 25) {
 					System.out.print(aciertosGlobales.get(letra) + "\t");
 					try {
 						bw.write(aciertosGlobales.get(letra) + "\t");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					System.out.print(aciertosGlobales.get(letra) + "\n");
 					try {
 						bw.write(aciertosGlobales.get(letra) + "\n");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				letra++;
 			}
 			try {
 				bw.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void PruebasConTodoKVecinos() {
 		Character letra = 'A';
 		BufferedWriter bw = null;
 
 		try {
 			bw = new BufferedWriter(new FileWriter(new File("resultados.txt")));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		ArrayList<HashMap<Character, Integer>> listaAciertosParciales = new ArrayList<>();
 		// Dibujo la fila de arriba de la tabla e inicializo los aciertos
 		// globales
 		for (int i = 0; i < 26; i++) {
 			if (i < 25) {
 				System.out.print(letra + "\t");
 				try {
 					bw.write(letra + "\t");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				System.out.print(letra + "\n");
 				try {
 					bw.write(letra + "\n");
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			letra++;
 		}
 
 		try {
 			bw.close();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		for (int j = 0; j < 5; j++) {
 			// System.out.println("Usando archivo test: salida"+j+".txt");
 			HashMap<Character, Integer> aciertosGlobales = new HashMap<>();
 			// Inicializo los globales
 			letra = 'A';
 			for (int i = 0; i < 26; i++) {
 				aciertosGlobales.put(letra, 0);
 				letra++;
 			}
 			for (int i = 0; i < 5; i++) {
 				if (i != j) {
 					// System.out.println("Procesando fichero: salida"+i+".txt");
 					HashMap<Character, Integer> aciertos = vecinoMasCercano(
 							"salida" + i + ".txt", "salida" + j + ".txt");
 					letra = 'A';
 					for (int k = 0; k < 26; k++) {
 						Integer parcial = aciertos.get(letra);
 						aciertosGlobales.put(letra, aciertosGlobales.get(letra)
 								+ parcial);
 						letra++;
 					}
 					listaAciertosParciales.add(aciertos);
 				}
 			}
 
 			try {
 				bw = new BufferedWriter(new FileWriter(new File(
 						"resultados.txt")));
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			letra = 'A';
 			for (int i = 0; i < 26; i++) {
 				if (i < 25) {
 					System.out.print(aciertosGlobales.get(letra) + "\t");
 					try {
 						bw.write(aciertosGlobales.get(letra) + "\t");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					System.out.print(aciertosGlobales.get(letra) + "\n");
 					try {
 						bw.write(aciertosGlobales.get(letra) + "\n");
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				letra++;
 			}
 			try {
 				bw.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
