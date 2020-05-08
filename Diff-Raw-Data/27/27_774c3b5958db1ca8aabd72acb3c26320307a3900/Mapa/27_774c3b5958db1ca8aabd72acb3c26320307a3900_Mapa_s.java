 import java.io.*;
 import java.util.*;
 import java.lang.*;
 
 
 /* Referencia usada para visualização da numeração
 do mapa: 
 
 http://www.codeproject.com/Articles/14948/Hexagonal-grid-for-games-and-other-projects-Part-1
 
 */
 
 
 public class Mapa
 {
 	int altura, largura;
 	Terreno[][] matriz;
 	ArrayList<Time> times;
 
 	
 
 	/****** Construtor ******/
 
 
 	public Mapa(String arquivo)
 	{
 		String linTemp;
 		int[][] mapaInt = null;
 		this.times = new ArrayList<Time>();
 
 		 try
 		 {
 
 		 	 /* Abertura de arquivo. Fonte: http://www.roseindia.net/java/beginners/java-read-file-line-by-line.shtml */
 			 // Open the file that is the first 
 			 // command line parameter
 			 FileInputStream fstream = new FileInputStream(arquivo);
 			 // Get the object of DataInputStream
 			 DataInputStream in = new DataInputStream(fstream);
 			 BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 			 /* ************* */
 
 
 			 linTemp = br.readLine(); // Primeira linha tem as dimensões do mapa
 		     String[] dimS = linTemp.split(" "); 
 		     if(dimS.length < 2)  throw new IllegalArgumentException("Arquivo do mapa no formato errado"); // Arquivo no formato errado.
 
 		     // Construção da matriz que vai definir o mapa
 
 		     this.altura = Integer.parseInt(dimS[0]);
 		     this.largura = Integer.parseInt(dimS[1]);
 
 		     mapaInt = new int[altura][];
 
 		     for(int i = 0; i < altura; i++)
 		     	mapaInt[i] = traduzLinha(br.readLine(), largura);
 
 
 		 		//Close the input stream
 				in.close();
 			}
 			catch (Exception e)
 			{//Catch exception if any
 				System.err.println("Error: " + e.getMessage());
 				System.exit(-1);
 			}
 
 
 		     // Construção do mapa em si
 
 		     this.matriz = new Terreno[altura][largura];
 
 		     for(int i = 0; i < altura; i++)
 		     	for(int j = 0; j < largura; j++)
 		     	{
 		     		Posicao pos = new Posicao(i,j);
 		     		switch(mapaInt[i][j])
 		     		{
 		     			case 0: this.matriz[i][j] = new Liso(pos); break;
 		     			case 1: this.matriz[i][j] = new Rugoso(pos); break;
 		     			case 2: this.matriz[i][j] = new Agua(pos); break;
 		     			case 3: this.matriz[i][j] = new Deposito(pos); 
 		     					this.matriz[i][j].toDeposito().putCristal(new Cristal(i,j));
 		     					break;
 		     			default: //Base
 		     				if(mapaInt[i][j] < 0)
 		     				{
 		     					int timeId = Math.abs(mapaInt[i][j]);
 		     					System.out.println(timeId);
 		     					if(existeTime(timeId)) throw new IllegalArgumentException();
 		     					this.matriz[i][j] = new Base(pos, timeId);
 		     					times.add(new Time(timeId, this.matriz[i][j].toBase()));
 						 	}
 						 	else 
 						 	{ 
 								this.matriz[i][j] = new Liso(pos);
 							}
 
 		     		}
 		     	}	
 
 
 
 	}
 
 	public boolean existeTime(int timeId)
 	{
 	  for(ListIterator<Time> it = times.listIterator(); it.hasNext();)
 	  {
 	    Time timeTemp = it.next();
 	    if(timeTemp.getId() == timeId) return true;
 	  }
 	  return false;
 	}
 
 	private int[] traduzLinha(String linha, int tam)
 	{
 		String[] nums = linha.split(" ");
 		int[] resp = new int[tam];
 		for(int i = 0; i < tam; i++)
 				resp[i] = Integer.parseInt(nums[i]);
 
 		return resp;
 	}
 
 	public ArrayList<Time> getTimes()
 	{
 		return this.times;
 	}
 
 	public Time getTime(int id)
 	{
 		Time timeTemp;
 		for(ListIterator<Time> it = times.listIterator(); it.hasNext();)
 		{
 			timeTemp = it.next();
 			if(timeTemp.getId() == id) return timeTemp;
 		}
 		return null;
 	}
 
 	//Construtor de mapa predefinido
 	
 	public int altura()
 	{
 		return this.altura;
 	}
 
 	public int largura()
 	{
 		return this.largura;
 	}
 
 
 /* ************************************
 	Funções que retornam o terreno de uma dada posição (i,j) e
 	dos hexagonos adjacentes.
 
 	*********************** */
 
 	public boolean existeTerreno(int i, int j)
 	{
 		return (i >= 0 && i < altura && j >= 0 && j < largura);
 	}
 
 	/* Retorna o terreno na posição i,j */
 	public Terreno getTerreno(int i, int j)
 	{
 		if(!existeTerreno(i,j))
 		 throw new ArrayIndexOutOfBoundsException();
 		return matriz[i][j];
 	}
 
 	public Terreno getTerreno(Posicao pos)
 	{
 		if(!existeTerreno(pos.getX(),pos.getY())) throw new ArrayIndexOutOfBoundsException();
 		return matriz[pos.getX()][pos.getY()];
 	}
 
 	/* Região superior esquerda. */
 	public Terreno getUpLeft(int i, int j)
 	{
 		if(j%2 == 1)// j é impar
 		{
 			if(!existeTerreno(i,j-1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i][j-1]; 
 		} 
 		else // j é par	
 		{
 			if(!existeTerreno(i-1,j-1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i-1][j-1]; 
 		}
 	}
 
 	/* Região superior direita. */
 	public Terreno getUpRight(int i, int j)
 	{
 
 		if(j%2 == 1) 
 		{
 			if(!existeTerreno(i,j+1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i][j+1];
 		} 
 		else
 		{
 			if(!existeTerreno(i-1,j+1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i-1][j+1];
 		} 
 	}
 	
 	/* Região inferior esquerda. */
 	public Terreno getDownLeft(int i, int j)
 	{
 		if(j%2 == 0) 
 		{
 			if(!existeTerreno(i,j-1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i][j-1];
 		}
 		else
 		{ 
 
 			if(!existeTerreno(i+1,j-1))throw new ArrayIndexOutOfBoundsException();
 			return matriz[i+1][j-1]; // j é par
 		}
 	}
 
 	/* Região inferior direita. */
 	public Terreno getDownRight(int i, int j)
 	{
 		if(j%2 == 0) 
 		{
 			if(!existeTerreno(i,j+1)) throw new ArrayIndexOutOfBoundsException();
 			return matriz[i][j+1]; // j é impar
 		}
 		else 
 		{
 			if(!existeTerreno(i+1,j+1)) throw new ArrayIndexOutOfBoundsException();
 			 return matriz[i+1][j+1]; // j é par
 		}
 	}
 	
 	/* Região imediatamente inferior. */
 	public Terreno getDown(int i, int j)
 	{
 		if(!existeTerreno(i+1,j))throw new ArrayIndexOutOfBoundsException();
 		return matriz[i+1][j];
 	}
 
 	/* Região imediatamente superior. */
 	public Terreno getUp(int i, int j)
 	{
 		if(!existeTerreno(i-1,j))throw new ArrayIndexOutOfBoundsException();
 		return matriz[i-1][j];
 
 	}
 
 
 	/****** Funções ******/
 
 
 	/* Função que recepe um robô e o insere no mapa. */
 	public boolean putRobo(Robo roboTemp)
 	{
 		/* Encontra as posições X e Y do robô. */
 		int x = roboTemp.getX();
 		int y = roboTemp.getY();
 		
 		/* Caso seja possível (a posição do robô não esteja ocupada e nem se encontre
 		   fora dos limites do mapa, inserimos o robô no mapa e retornamos TRUE. É  
 		   retornado o valor FALSE caso contrário.                                    */
		if(x < largura && y < altura && y >= 0 && x >= 0)
 		{
 			if(!matriz[x][y].temRobo())
 			{
 				matriz[x][y].putRobo(roboTemp);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/* Função que receberá uma posição e removerá o robô localizado nesta posição. */
 	public void removeRobo(int x, int y)
 	{
		if(x < largura && y < altura && y > 0 && x > 0)
 			matriz[x][y].removeRobo();
 	}
 }
