 package grafo;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * Clase que manipula un grafo implementando matriz de adyacencia.
  */
 public class Grafo {
 
 	int[][] matriz;
 	int tam;
 	int [] orden;
 	
 	public Grafo(Integer tam) {
 
 		this.matriz = new int[tam][tam];
 		this.tam = tam;
 		this.orden = new int [tam];
 	}
 
 	/*
 	 * Se crea un grafo desde un archivo de input
 	 */
 	public Grafo(String path) {
 
 		File file = null;
 		FileReader fr = null;
 		BufferedReader br = null;
 		String linea = null;
 
 		try {
 			file = new File(path);
 			fr = new FileReader(file);
 			br = new BufferedReader(fr);
 
 			// Se lee la primera linea con la info del grafo
 			linea = br.readLine();
 			String[] spliteado = linea.split("\t");
 			this.tam = Integer.parseInt(spliteado[0]);
 			this.matriz = new int[this.tam][this.tam];
 			this.orden = new int [this.tam];	
 			
 			for (int i=0;i<this.tam; i++)
 				this.orden[i]=i;
 			// Se setean las adyacencias
 			int cantAristas = Integer.parseInt(spliteado[1]);
 			for(int i = 0; i < cantAristas; i++){
 				
 				linea = br.readLine();
 				String[] stringAdyacencia = linea.split("\t");
 				setearAdyacencia(Integer.parseInt(stringAdyacencia[0]),Integer.parseInt(stringAdyacencia[1]));
 			}
 
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		} finally {
 			try {
 				if (null != fr) {
 					fr.close();
 				}
 			} catch (Exception e2) {
 				e2.printStackTrace();
 			}
 		}
 
 	}
 
 	public int getTam() {
 
 		return this.tam;
 	}
 
 	public boolean sonAdyacentes(int i, int j) {
 
 		if (matriz[i][j] == 1) {
 
 			return true;
 		} else {
 
 			return false;
 		}
 	}
 
 	public void setearAdyacencia(int i, int j) {
 
 		if (i < tam && j < tam && i >= 0 && j >= 0) {
 			matriz[i][j] = 1;
 			matriz[j][i] = 1;
 		}
 	}
 
 	public void quitarAdyacencia(int i, int j) {
 
 		if (i < tam && j < tam && i >= 0 && j >= 0) {
 			matriz[i][j] = 0;
			matriz[j][i] = 0;
 		}
 	}
 
 	public void mostrarMatriz() {
 
 		for (int i = 0; i < tam; i++) {
 			for (int j = 0; j < tam; j++) {
 
 				System.out.print(matriz[i][j] + " ");
 			}
 			System.out.print("\n");
 		}
 	}
 	
 	public void mostrarOrden(){
 		for (int i=0; i< tam; i++)
 			System.out.print(this.orden[i]);
 		System.out.println();
 	}
 	
 	private void inviertoOrden(int n1, int n2) {
 		int l1;
 		int l2;
 		l1 = this.orden[n1];
 		l2 = this.orden[n2];
 
 		this.orden[n1] = l2;
 		this.orden[n2] = l1;
 	}
 	
 	
 	public void ordenaAleatorio()
 	{
 		Random generador = new Random();
 		
 		int random = generador.nextInt(1000);
 		
 //		for (int i=0; i< this.tam ; i++)
 //		{
 			for (int j=0; j< random ; j++)
 				inviertoOrden(generador.nextInt(this.tam), generador.nextInt(this.tam));
 		//}	
 		
 		
 	}
 	
 	public int getVertice(int n)
 	{
 		return this.orden[n];
 	}
 	
 	public void ordenaMayorAMenorGrado()
 	{
 		int grado1;
 		int grado2;
 		boolean ordenado = false;
 		
 		while (!ordenado)
 		{
 			ordenado = true;
 			for (int i = 0; i< this.tam-1 ; i++)
 			{
 				grado1 = gradoVertice(this.orden[i]);
 				grado2 = gradoVertice(this.orden[i+1]);
 								
 				if (grado1 < grado2)
 				{
 					inviertoOrden(i, i+1);
 					ordenado = false;					
 				}				
 			}			
 		}
 	}
 	
 	public void ordenaMenorAMayorGrado()
 	{
 		int grado1;
 		int grado2;
 		boolean ordenado = false;
 		
 		while (!ordenado)
 		{
 			ordenado = true;
 			for (int i = 0; i< this.tam-1 ; i++)
 			{
 				grado1 = gradoVertice(this.orden[i]);
 				grado2 = gradoVertice(this.orden[i+1]);
 								
 				if (grado1 > grado2)
 				{
 					inviertoOrden(i, i+1);
 					ordenado = false;					
 				}				
 			}			
 		}	
 	}
 	
 	public int gradoVertice(int vertice)
 	{
 		int retorno = 0;
 		for (int i = 0; i< this.tam; i ++)
 			retorno+=this.matriz[vertice][i];
 		
 		return retorno;
 	}
 
 	public int gradoMax() {
 
 		int retorno = 0;
 
 		for (int i = 0; i < this.tam; i++) {
 
 			int aux;
 			
 			aux = gradoVertice(i);
 
 //			for (int j = 0; j < this.tam; j++) {
 //
 //				if (matriz[i][j] == 1) {
 //					aux++;
 //				}
 //			}
 			if (aux > retorno) {
 				retorno = aux;
 			}
 		}
 
 		return retorno;
 	}
 
 	public int gradoMin() {
 
 		int retorno = this.tam;
 
 		for (int i = 0; i < this.tam; i++) {
 
 			int aux;
 			
 			aux = gradoVertice(i);
 
 //			for (int j = 0; j < this.tam; j++) {
 //
 //				if (matriz[i][j] == 1) {
 //
 //					aux++;
 //				}
 //
 //			}
 
 			if (aux < retorno) {
 
 				retorno = aux;
 			}
 		}
 
 		return retorno;
 	}
 
 	public static void main(String[] args) {
 
 		Grafo g = new Grafo("LoteDePruebas/InputGenerados/grafoLALALA.in");
 		g.mostrarMatriz();
 		g.mostrarOrden();
 		g.ordenaMayorAMenorGrado();
 		System.out.println("Ordenado de mayor a menor:");
 		g.mostrarOrden();
 				
 		g.ordenaMenorAMayorGrado();
 		System.out.println("Ordenado de menor a mayor:");
 		g.mostrarOrden();
 		
 	}
 
 }
