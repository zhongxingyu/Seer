 package sel;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import vectorMath.VectorMath;
 import matrizMath.MatrizMath;
 
 public class Sel {
 
 	private int dim;
 
 	private MatrizMath m;
 
 	private VectorMath x, b;
 
 	private Double error;
 
 	final static public double EPSILON = Math.pow(10, -12);
 
 	private String path;
 
 	public Sel(MatrizMath m, VectorMath x) {
 		this.dim = m.getColumnas();
 		this.m = m;
 		this.b = x;
 		path = "prueba";
 	}
 
 	public Sel(String path) {
 
 		this.path = path.substring(path.indexOf('/')+1);
 
 		
 		File archivo = null;
 		FileReader fr = null;
 		BufferedReader br = null;
 
 		try {
 			// Apertura del fichero y creacion de BufferedReader para poder
 			// hacer una lectura comoda (disponer del metodo readLine()).
 			archivo = new File("src/sel/" + path + ".in");
 			fr = new FileReader(archivo);
 			br = new BufferedReader(fr);
 
 			String linea;
 
 			// Leo dimension del sistema
 			if ((linea = br.readLine()) != null) {
 
 				dim = Integer.parseInt(linea);
 
 				// Se crea el vector de resultados "b"
 				this.b = new VectorMath(dim);
 
 				// Leo valores y los asigno al vector
 				int cont = 0;
 				while (cont < dim && (linea = br.readLine()) != null) {
 					b.agregarValor(cont, Double.parseDouble(linea));
 					cont++;
 				}
 
 				// Se crea la Matriz M
 				this.m = new MatrizMath(dim, dim);
 
 				// Se llena la matriz con los valores del archivo de entrada
 
 				while ((linea = br.readLine()) != null) {
 
 					String[] valores = linea.split(" ");
 
 					m.setValor(Integer.parseInt(valores[0]),
 							Integer.parseInt(valores[1]),
 							Double.parseDouble(valores[2]));
 
 				}
 
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			// En el finally cerramos el fichero, para asegurarnos
 			// que se cierra tanto si todo va bien como si salta
 			// una excepcion.
 			try {
 				if (null != fr) {
 					fr.close();
 				}
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 
 	public String toString() {
 		String retorno;
 		retorno = "Matriz:\n" + this.m;
 		retorno += "Vector B:\n" + this.b + "\n";
 		retorno += "Vector X:\n" + this.x + "\n";
 		retorno += "Error: " + this.error
 				+ (this.error < 1 ? " Correcto" : " Incorrecto");
 		// System.out.println("Matriz:");
 		// System.out.println(this.m);
 		return retorno;
 	}
 
 	public void resolver() {
 
 		//this.error = EPSILON;
 
 		// calculo de la inversa de M
 		MatrizMath Minversa = this.m.inversa();
 		if (Minversa != null) {
 
 			// Se calcula la matriz identidad I'
 			MatrizMath Iprima = this.m.producto(Minversa);
 
 			MatrizMath Prima = this.m.identidad();
 
 			this.error = calcularErrorSolucion(Prima, Iprima).doubleValue();
 			if (this.error >= EPSILON) {
 				return;
 			}
 
 			// Se calcula el vector X'
 			VectorMath Xprima = Minversa.producto(b);
 
 			// Se calcula el vector B'
 			VectorMath Bprima = m.producto(Xprima);
 
 			// Se calcula el error || B - B' ||2
 			this.error = calcularErrorSolucion(this.b, Bprima).doubleValue();
 
 			// El resultado X se le asigna al calculado X'
 			this.x = Xprima;
 		}
 
 	}
 
 	// Metodo para calcular el error entre dos matrices
 	private Double calcularErrorSolucion(MatrizMath m1, MatrizMath m2) {
 		MatrizMath restaMatrices = m1.resta(m2);
 		return restaMatrices.normaDos();
 
 	}
 
 	// Metodo para calcular el error entre dos vectores
 	private Double calcularErrorSolucion(VectorMath v1, VectorMath v2) {
 
 		VectorMath restaVectores = v1.resta(v2);
 		return restaVectores.normaDos();
 
 	}
 
 	// Metodo que muestra el resultado
 	private void mostrarResultado() {
 		if (x == null)
 			System.out.println("No se encontro una solucion");
 		else {
 			Double[] resultado = x.getVector();
 			System.out.println("Vector de Resultados:");
 			for (int i = 0; i < dim; i++) {
 				System.out.printf("%.20f\n", resultado[i]);
 			}
 		}
 	}
 
 	/*
 	 * Testea si el error es muy grande
 	 */
 	public int test() {
 			
 
 		if (this.error== null || this.error >= 1) {
 
 			generarOut(false);			
 			return this.error == null ?2:0;
 		} else {
 
 			generarOut(true);
 			this.mostrarResultado();
 			return 1;
 		}
 	}
 	
 	public void generaInput()
 	{
 
 		File archivo = null;
 		FileWriter fw = null;
 		PrintWriter pw = null;
 
 		try {
 			archivo = new File("src/sel/InputGenerado/" + "Input" + ".in");
 			fw = new FileWriter(archivo);
 			pw = new PrintWriter(fw);
 			
 			pw.println(this.dim);
 			for (int v = 0; v<this.dim; v++)
 				pw.println (this.b.getVector()[v]);
 			
 			
 			
 			for (int f = 0; f< dim; f++)
 				for (int c = 0; c < dim ; c++)
					pw.println(f+" "+c+" "+this.m.getValor(f, c));
 			
 			
 		} catch (Exception e) {
 
 			e.printStackTrace();
 		}
 
 		finally {
 			// En el finally cerramos el fichero, para asegurarnos
 			// que se cierra tanto si todo va bien como si salta
 			// una excepcion.
 			try {
 				if (null != fw) {
 					fw.close();
 				}
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 			
 	}
 
 	private void generarOut(boolean resultado) {
 
 		File archivo = null;
 		FileWriter fw = null;
 		PrintWriter pw = null;
 
 		try {
 			archivo = new File("src/sel/OuputGenerado/" + this.path + ".out");
 			fw = new FileWriter(archivo);
 			pw = new PrintWriter(fw);
 
 			// Si no hubo resultados
 			if (!resultado) {
 
 				pw.println("NO");
 			} else {
 
 				// Si hay un resultado para mostrar
 				Integer dim = this.b.getVector().length;
				pw.println(dim);
 				for (int i = 0; i < dim; i++) {
					pw.println(this.x.getVector()[i]);
 
 				}
 				pw.println(error);
 			}
 
 		} catch (Exception e) {
 
 			e.printStackTrace();
 		}
 
 		finally {
 			// En el finally cerramos el fichero, para asegurarnos
 			// que se cierra tanto si todo va bien como si salta
 			// una excepcion.
 			try {
 				if (null != fw) {
 					fw.close();
 				}
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 
 		Sel s = null;
 		
 //		for (int i = 1 ; i<8 ; i++){
 //			s = new Sel("Input/SEL"+i);
 //		System.out.println("SEL"+i);
 		s = new Sel("InputGenerado/Input");
 		s.resolver();
 		int resultado = s.test();
 		
 		switch (resultado){
 		case 1:
 			System.out.println("Resultado Correcto");
 			break;
 		case 0:
 			System.out.println("Resultado Incorrecto");
 			break;
 		case 2:
 			System.out.println("No es un sistema compatible determinado");
 			break;
 		}
 		
 					
 //		}
 
 	}
 }
