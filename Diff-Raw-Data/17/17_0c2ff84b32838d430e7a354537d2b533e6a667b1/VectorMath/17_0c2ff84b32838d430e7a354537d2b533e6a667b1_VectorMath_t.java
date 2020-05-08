 package ar.edu.unlam.math;
 
 import java.util.Arrays;
 
import ar.edu.unlam.math.exception.VectorDimensionException;
 
 public class VectorMath {
 	// Atributos ~
 	// -------------------------------------------------------
 	private double[] valores;
 
 	// Constructores ~
 	// -------------------------------------------------------
 	public VectorMath() {
 		this(0);
 	}
 
 	public VectorMath(double... v) {
 		int cantidad = v.length;
 		valores = new double[cantidad];
 		for (int i = 0; i < cantidad; i++)
 			valores[i] = v[i];
 	}
 
 	// Getters & Setters ~
 	// -------------------------------------------------------
 	public double[] getValores() {
 		return valores;
 	}
 
 	public void setValores(double[] vector) {
 		this.valores = vector;
 	}
 
 	public int getDimension() {
 		return getValores().length;
 	}
 
 	// Métodos ~
 	// -------------------------------------------------------
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("(");
 
 		for (double valor : valores)
 			sb.append(valor + ", ");
 		sb.replace(sb.length() - 2, sb.length(), ")");
 		return sb.toString();
 	}
 
 	/***
 	 * Realiza la suma vectorial entre dos vectores de la misma dimension
 	 * 
 	 * @param v2
 	 *            Vector a sumar
 	 * @return Vector resultado de la suma
 	 * @throws VectorDimensionException
 	 *             En caso de que la dimesion de los vectores sea distinta
 	 */
 	public VectorMath sumar(VectorMath v2) throws VectorDimensionException {
 		int dimension = v2.getDimension();
 		if (this.getDimension() != dimension)
 			throw new VectorDimensionException(
 					"Para sumar es necesario que la dimensión de los vectores sean iguales");
 
 		double[] vector = v2.getValores();
 		double[] resultado = new double[dimension];
 		for (int i = 0; i < dimension; i++)
 			resultado[i] = valores[i] + vector[i];
 
 		return new VectorMath(resultado);
 	}
 
 	/***
 	 * Realiza la resta vectorial entre dos vectores de la misma dimension
 	 * 
 	 * @param v2
 	 *            Vector a restar
 	 * @return Vector resultado de la resta
 	 * @throws VectorDimensionException
 	 *             En caso de que la dimesion de los vectores sea distinta
 	 */
 	public VectorMath restar(VectorMath v2) throws VectorDimensionException {
 		int dimension = v2.getDimension();
 		if (this.getDimension() != dimension)
 			throw new VectorDimensionException(
 					"Para restar es necesario que la dimensión de los vectores sean iguales");
 
 		double[] vector = v2.getValores();
 		double[] resultado = new double[dimension];
 		for (int i = 0; i < dimension; i++)
 			resultado[i] = valores[i] - vector[i];
 
 		return new VectorMath(resultado);
 	}
 
 	/***
 	 * Realiza el producto vectorial (o producto cruz) entre dos vectores de dimension 3
 	 * 
 	 * @param v2
 	 *            Vector con el que se realizara el producto (debe ser de dimension 3)
 	 * @return Vector resultado del producto
 	 * @throws VectorDimensionException
 	 *             En caso de que la dimesion de los vectores sea distinta de 3
 	 */
 	public VectorMath productoVectorial(VectorMath v2) throws VectorDimensionException {
 		if (this.getDimension() != 3 || v2.getDimension() != 3)
 			throw new VectorDimensionException(
 					"El producto vectorial sólo puede realizarse en vectores de dimension 3");
 
 		double[] vector = v2.getValores();
 		double[] resultado = new double[3];
 		resultado[0] = valores[1] * vector[2] - vector[1] * valores[2];
 		resultado[1] = -(valores[0] * vector[2] - vector[0] * valores[2]);
 		resultado[2] = valores[0] * vector[1] - vector[0] * valores[1];
 
 		return new VectorMath(resultado);
 	}
 
 	/***
 	 * Realiza el producto interno entre dos vectores de la
 	 * misma dimension
 	 * 
 	 * @param v2
 	 *            Vector con el que se realizara el producto
 	 * @return Vector resultado del producto
 	 * @throws VectorDimensionException
 	 *             En caso de que la dimesion de los vectores sea distinta
 	 */
 	public double producto(VectorMath v2) throws VectorDimensionException {
 		int dimension = v2.getDimension();
 		if (this.getDimension() != dimension)
 			throw new VectorDimensionException(
 					"Para realizar el producto interno es necesario que la dimensión de los vectores sean iguales");
 
 		double[] vector = v2.getValores();
 		double resultado = 0;
 		for (int i = 0; i < dimension; i++)
 			resultado += valores[i] * vector[i];
 
 		return resultado;
 	}
 
 	/***
 	 * Realiza el producto escalar entre el vector y un número real
 	 * 
 	 * @param n
 	 *            Numero real con el que se hará el producto
 	 * @return Vector resultado del producto
 	 */
 	public VectorMath producto(Double n) {
 		int dimension = getDimension();
 		double[] resultado = new double[dimension];
 		for (int i = 0; i < dimension; i++)
 			resultado[i] = valores[i] * n;
 		return new VectorMath(resultado);
 	}
 
 	/***
 	 * Realiza el producto escalar entre el vector y un número entero
 	 * 
 	 * @param n
 	 *            Numero entero con el que se hará el producto
 	 * @return Vector resultado del producto
 	 */
 	public VectorMath producto(Integer n) {
 		return producto(new Double(n));
 	}
 
 	/***
 	 * Realiza la norma uno del vector
 	 * 
 	 * @return Resultado de aplicar la norma uno del vector
 	 */
 	public double normaUno() {
 		double resultado = 0;
 		for (double valor : valores)
 			resultado += Math.abs(valor);
 		return resultado;
 	}
 
 	/***
 	 * Realiza la norma dos del vector
 	 * 
 	 * @return Resultado de aplicar la norma dos del vector
 	 */
 	public double normaDos() {
 		double resultado = 0;
 		for (double valor : valores)
 			resultado += valor * valor;
 		return Math.sqrt(resultado);
 	}
 
 	/***
 	 * Realiza la norma infinito del vector
 	 * 
 	 * @return Resultado de aplicar la norma infinito del vector
 	 */
 	public double normaInfinito() {
 		double maximo = 0;
 		for (double valor : valores) {
 			valor = Math.abs(valor);
 			if (valor > maximo)
 				maximo = valor;
 		}
 		return maximo;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + Arrays.hashCode(valores);
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		VectorMath other = (VectorMath) obj;
 		if (!Arrays.equals(valores, other.valores))
 			return false;
 		return true;
 	}
 
 	@Override
 	public VectorMath clone() {
 		double[] resultado = new double[getDimension()];
 		for (int i = 0; i < resultado.length; i++)
 			resultado[i] = valores[i];
 		return new VectorMath(resultado);
 	}
 
 	// Test ~
 	// -------------------------------------------------------
 	public static void main(String[] args) {
 		VectorMath v0 = new VectorMath();
 		VectorMath v1 = new VectorMath(3, 2, 5);
 		VectorMath v2 = new VectorMath(10, 2, 3);
 		// MatrizMath m1 = new MatrizMath();
 
 		System.out.println("v0: " + v0);
 		System.out.println("v1: " + v1);
 		System.out.println("v2: " + v2);
 
 		// Test suma ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("SUMA");
 			System.out.println("v1 + v2: " + v1.sumar(v2) + " / ESPERADO (13,4,8)");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		try {
 			System.out.println("v0 + v1: ");
 			System.out.println(v1.sumar(v0) + " / ESPERADO VectorDimensionException");
 		} catch (VectorDimensionException e) {
 			System.out.println("VectorDimensionException");
 		}
 
 		// Test restar ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("RESTA");
 			System.out.println("v1-v2: " + v1.restar(v2) + " / ESPERADO (-7,0,2)");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			System.out.println("v0 - v1: ");
 			System.out.println(v1.sumar(v0) + " / ESPERADO VectorDimensionException");
 		} catch (VectorDimensionException e) {
 			System.out.println("VectorDimensionException");
 		}
 
 		// Test producto interno ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Producto interno");
 			System.out.println("v1 · v2: " + v1.producto(v2) + " / ESPERADO " + (30 + 4 + 15));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			System.out.println("v1 · v0: ");
 			System.out.println(v1.producto(v0) + " / ESPERADO VectorDimensionException");
 		} catch (VectorDimensionException e) {
 			System.out.println("VectorDimensionException");
 		}
 
 		// Test producto vectorial ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Producto vectorial");
 			System.out.println("v1 x v2: " + v1.productoVectorial(v2) + " / ESPERADO (-4,41,26)");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		try {
 			System.out.println("v1 x v0: ");
 			System.out.println(v1.producto(v0) + " / ESPERADO VectorDimensionException");
 		} catch (VectorDimensionException e) {
 			System.out.println("VectorDimensionException");
 		}
 
 		// Test producto matriz ~
 		// -----------------------------------------------------------------------------------
 		// System.out.println("PRODUCTO MATRIZ - " + v1.producto(m1));
 
 		// Test producto escalar ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Producto escalar");
 			System.out.println("v1·20: " + v1.producto(20) + " / ESPERADO (60,40,100)");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Test producto norma uno ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Norma uno");
 			System.out.println("v1: " + v1.normaUno() + "/ ESPERADO 10");
 			System.out.println("v1-v2: " + v1.restar(v2).normaUno() + "/ ESPERADO 9");
 			System.out.println("v0: " + v0.normaUno() + "/ ESPERADO 0");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Test producto norma dos ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Norma dos");
 			System.out.println("v1: " + v1.normaDos() + "/ ESPERADO " + Math.sqrt(38));
 			System.out.println("v2: " + v2.normaDos() + "/ ESPERADO " + Math.sqrt(113));
 			System.out.println("v0: " + v0.normaDos() + "/ ESPERADO 0");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Test producto norma infinito ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("Norma infinito");
 			System.out.println("v1: " + v1.normaInfinito() + "/ ESPERADO 5");
 			System.out.println("v2: " + v2.normaInfinito() + "/ ESPERADO 10");
 			System.out.println("v1-v2: " + v1.restar(v2).normaInfinito() + "/ ESPERADO 7");
 			System.out.println("v0: " + v0.normaInfinito() + "/ ESPERADO 0");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		// Test producto equals ~
 		// -----------------------------------------------------------------------------------
 		try {
 			System.out.println();
 			System.out.println("EQUALS:");
 			System.out.println("\tv1 == v2: " + v1.equals(v2) + " / ESPERADO false");
 			VectorMath v4 = v1.clone();
 			System.out.println("\tv4 = v1.clone()");
 			System.out.println("\tv1 == v4: " + v1.equals(v4) + " / ESPERADO true");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 }
