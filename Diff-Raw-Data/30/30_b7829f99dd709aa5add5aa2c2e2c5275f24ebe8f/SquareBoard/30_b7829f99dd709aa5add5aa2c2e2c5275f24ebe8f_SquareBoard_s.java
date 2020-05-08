 package IA;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import aima.search.framework.Successor;
 
 public class SquareBoard
 {
   // Globals and misc
   // Global N value
   public static int N = 20;
   public static double KH21 = 0.5;
 	public static int K;
 	public static int P;
 
   // Data
 
 	public Ruta[] rutes;
 	public Parada[] parades;
 	int restaddicional = 1; //Minim de parades per ruta
 
 	public SquareBoard(int k, int p, boolean rest)
 	{
 		if(rest) restaddicional = (Integer)(p / (2 * k));
 		int i;
 		this.K = k;
 		this.P = p;
 		rutes = new Ruta[K];
 		parades = new Parada[P];
 		crearParades();
 		for (i = 0; i < K; i++)
 			rutes[i] = new Ruta(i);
 	}
 
   public SquareBoard(int k, int p)
   {
 		int i;
 		this.K = k;
 		this.P = p;
 		rutes = new Ruta[K];
 		parades = new Parada[P];
 		crearParades();
 		for (i = 0; i < K; i++)
 			rutes[i] = new Ruta(i);
 
     // solIni1();
    // Now this is done in the Main class
   }
 
   // Copy operator
   public SquareBoard(SquareBoard original)
   {
     int i;
 
     N = original.N;
     K = original.K;
     P = original.P;
     parades = new Parada[P];
     rutes = new Ruta[K];
 
     for(i = 0; i < P; i++)
       parades[i] = new Parada(original.parades[i]);
     for(i = 0; i < K; i++)
       rutes[i] = new Ruta(original.rutes[i]);
 
   }
 
 	private void crearParades()
 	{
 		int x,y, i;
 		for (i=0; i<P; i++)
 			parades[i] = new Parada(-1,-1,-1);
 
 		for (i=0; i<P; i++)
 		{
 			x = aleatori(N);
 			y = aleatori(N);
 			while (paradaOcupada(x,y))
 			{
 				x = aleatori(N);
 				y = aleatori(N);
 			}
 			parades[i] = new Parada(x,y, i);
 		}
 	}
 	
 	private void crearParadesFixades() 
 	{
 			int x,y, i;
 			int j = 0;
 			for (i=0; i<P; i++)
 				parades[i] = new Parada(-1,-1,-1);
 
 			for (i=0; i<P; i++)
 			{
 				x = (Integer)(i * 4025 / 345) % N;
 				y = (Integer)(i * 6043 / 245) % N;
 				while (paradaOcupada(x,y))
 				{
 					j = j + i + 1;
 					x = (Integer)(j * 4025 / 345) % N;
 					y = (Integer)(j * 6043 / 245) % N;
 				}
 				parades[i] = new Parada(x,y, i);
 				j = 0;
 			}
 	}
 	
 	public void delRutes()
 	{
 	  for(int i = 0; i < K; i++)
 	    rutes[i] = new Ruta(i);
 
     for(int i = 0; i < P; i++)
 	    parades[i].ruta = -1;
 	}
 
 	// Solució Inicial Bona
 	public String solIni1()
 	{
 		int pi, ki;
 		int[] paradaActual = new int[K]; // Parada Actual per cada ruta
 		boolean[] assignades = new boolean[P]; // Vector de parades assignades
 		int numassignades = 0;
 		int currentdist = 0;
 		int mindist = Integer.MAX_VALUE;
 
     //MOLT IMPORTANT: Esborra qualsevol ruta que hi hagi pogut haver... 
 		delRutes();
 
 		for (pi = 0; pi < P; pi++) assignades[pi] = false; // Inicialitzem vector d'assignades
 		for (ki = 0; ki < K; ki++) paradaActual[ki] = -1; // Inicialitzem vector de parades actuals..
 
 
 		for(ki = 0; numassignades < P; numassignades++, ki = (ki + 1) % K) // Afegeixo la resta de parades
 		{	
 			for (pi = 0; pi < P; pi++)
 			{
 				if(!assignades[pi] && pi != paradaActual[ki])
 				{
           // Aixo nomes ho fem si ja estem per la segona o més rondes de rutes... en la primera no ho fem perque no hi ha paradaActual
 					if (ki >= K)
 					{
 					  currentdist = parades[paradaActual[ki]].distParadaFisica(new Parada(19, 19, -1)); // Distància amb parada fi
             currentdist += parades[paradaActual[ki]].distParadaFisica(parades[pi]); // + Distància amb nova parada qualsevol
 					}
 					else
 					{
 					  currentdist = (new Parada(0, 0, -1)).distParadaFisica(parades[pi]);	
 					}
 
 					if(currentdist < mindist) // En cas que sigui mínim l'assignem a la ruta
 					{
 						mindist = currentdist;
 						paradaActual[ki] = pi;
 					}
 				}
 			}
 			mindist = Integer.MAX_VALUE;
 			rutes[ki].afegirParada(paradaActual[ki]);
 			assignades[paradaActual[ki]] = true;
 		}		
 
 		String out;
 			out = "############Solució Inicial 1#############\n";
 			out += "Heuristic 1: " + getHeuristic1() + "\n";
 			out += "Heuristic 2: " + getHeuristic2() + "\n";
 			out += "##########################################";
 		return out;
 	}
 
 	// Solució Inicial Ràpida
 	public String solIni2()
 	{
 		int pi, ki;
 		ki = 0;
 
 		//MOLT IMPORTANT: Esborra qualsevol ruta que hi hagi pogut haver... 
     delRutes();
 
 		for (pi = 0; pi < P; pi++)
 		{
 			rutes[ki].afegirParada(pi);
 			ki = (ki + 1) % K;
 		}
 
 		String out;
 			out = "############Solució Inicial 2#############\n";
 			out += "Heuristic 1: " + getHeuristic1() + "\n";
 			out += "Heuristic 2: " + getHeuristic2() + "\n";
 			out += "##########################################";
 		return out;
 	}
 	
 	// Solució inicial completament aleatòria
 	public String solIni3()
 	{
 		int ki = 0;
 		int pi = 0;
 		int numassignades = 0;
 		boolean[] assignades = new boolean[P];
 		
 		for (pi = 0; pi < P; pi++) assignades[pi] = false;
 		
 		//MOLT IMPORTANT: Esborra qualsevol ruta que hi hagi pogut haver... 
     delRutes();
 
 		while (numassignades < P)
 		{
 			pi = aleatori(P);
 			if(!assignades[pi])
 			{
 				rutes[ki].afegirParada(pi);
 				numassignades++;
 				assignades[pi] = true;
 				if (numassignades < K) ki = (ki + 1) % K;
 			} 
 			if (numassignades >= K) ki = (ki + 1) % K;
 		}
 
 		String out;
 			out = "############Solució Inicial 3#############\n";
 			out += "Heuristic 1: " + getHeuristic1() + "\n";
 			out += "Heuristic 2: " + getHeuristic2() + "\n";
 			out += "##########################################";
 		return out;
 	}
 
 	private int aleatori(int max)
 	{
 		return new Double(Math.random() * max).intValue();
 	}
 
 	private boolean paradaOcupada(int x, int y)
 	{
 		if ((x == 0 && y == 0) || (x == 19 && y == 19)) return true;
 		for (int i = 0; i < P; i++)
 		{
 			if (parades[i].x == x && parades[i].y == y) return true;
 		}
 
 
 		return false;
 	}
 
 	private int idParada(int x, int y)
 	{
 		if (x == 0 && y == 0) return -10; // 0,0
 		else if (x == 19 && y == 19) return -20; // 19,19
 		for (int i = 0; i < P; i++)
 		{
 			if (parades[i].x == x && parades[i].y == y) return parades[i].id;
 		}
 		return -1; // No hi ha parada
 	}
 
   public String toString()
   {
     String out = "";
     int i,j;
 
     out += "########################################\n";
 
     for(i = 0; i < N; i++)
     {
       for(j = 0; j < N; j++)
       {
         if (paradaOcupada(i,j)) out += " " + idParada(i,j) + " ";
            else out += " - ";
       }
       out +="\n";
     }
     out += "########################################\n";
 
     out += "\nSuma total rutes: "+getHeuristic1()+"\n";
     out += "StDev dist entre totes les parades: "+getHeuristic2()+"\n\n";
 
 		out += "Rutes:\n";
 		for (i = 0; i < K; i++)
 		{
 			out += "Ruta " + i + " ("+rutes[i].numparades+") :";
 			for(j = 0; j < rutes[i].numparades; j++)
 			{
 				out += rutes[i].paradesRuta[j] + " ";
 			}
 			out += " dist: " + rutes[i].dist + "\n";
 		}

		// Mostra distancies entre tots els parells de parades
    // for(i = 0; i < P; i++)
    // {
    //   for(j = i+1; j < P; j++)
    //   {
    //           out += "Dist entre "+i+" i "+j+" :"+ parades[i].distParada(parades[j]) + "\n";
    //   }
    // }

     return out;
   }
 
   public String info()
   {
     String out = "";
     out += "Suma total rutes: "+getHeuristic1()+"\n";
     out += "StDev dist entre totes les parades: "+getHeuristic2();
     return out;
   }
 
 	public boolean canviarRuta(int paradaOrigen, int rutaDesti)
 	{
 		if (rutes[parades[paradaOrigen].ruta].numparades > restaddicional)
 		{
 			if (rutes[parades[paradaOrigen].ruta].treureParada(paradaOrigen))
 			{
 				rutes[rutaDesti].afegirParada(paradaOrigen);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	//Mou sempre cap a la dreta
 	public boolean moureParada(int parada)
 	{
 		return rutes[parades[parada].ruta].moureParada(parada);
 	}
 
 	public List<Successor> getSuccessors()
 	{
 	  int i,j;
 	  List<Successor> list = new ArrayList<Successor>();
 	  SquareBoard newboard;
 
 	  // Fem tots els moviments de parada possibles:
 	  for(i = 0; i < K; i++) // Per cada parada
 	  {
 	    for(j = 0; j < rutes[i].numparades; j++) // per cada posicio de la ruta a la que pertany
 	    {
 	      newboard = new SquareBoard(this);
         newboard.moureParada(rutes[i].paradesRuta[j]);
        // System.out.println(newboard);
         list.add(new Successor("P " +rutes[i].paradesRuta[j]+ " move in route " + i + " ", newboard));
 	    }
 	  }
 
 	  // Fem tots els intercanvis de parada possibles:
 	  for(i = 0; i < P; i++) // per cada parada
 	  {
 	    for(j = 0; j < K; j++) // per cada ruta
 	    {
 	      if (j != parades[i].ruta) // Nomes la movem a rutes diferents...
 	      {
 	        newboard = new SquareBoard(this);
 	        newboard.canviarRuta(i, j);
          // System.out.println(newboard);
           list.add(new Successor("P " +i+ " move to route " + j + " ", newboard));
 	      }
 	    }
 	  }
     return list;
 	}
 
 	public double getHeuristic1()
 	{
 	  double h;
 	  int d = 0;
     for (int i = 0; i < K; i++)
       d += rutes[i].dist;
 	  h = (double) d;
    // System.out.println("Heuristic: " + h);
 	  return h;
 	}
 
 	public double getHeuristic2()
 	{
 	  double h = 0;
 	  int i,j,c = 0;
 	  int total = 0;
 	  double avg = 0;
 	  double stdev = 0;
 
     for(i = 0; i < P; i++)
       for(j = i+1; j < P; j++, c++)
             total += parades[i].distParada(parades[j]);
 
     avg = total / c;
 
     for(i = 0; i < P; i++)
       for(j = i+1; j < P; j++)
             stdev += Math.abs(parades[i].distParada(parades[j]) - avg);
 
     h = stdev;
    // System.out.println("Heuristic: " + h);
 	  return h;
 	}
 
   public double getHeuristic3()
 	{
 	  return getHeuristic1() * (1.0-KH21) + KH21 * getHeuristic2();
 	}
 
 	class Parada
 	{
 		public int id;
 		public int x;
 		public int y;
 		public int ruta;
 
 		public Parada(int x_, int y_, int id_)
 		{
 			id = id_;
 			x = x_;
 			y = y_;
 			ruta = -1;
 		}
 
 		// Operador de copia
 		public Parada(Parada original)
 		{
 		  id = original.id;
 		  x = original.x;
 		  y = original.y;
 		  ruta = original.ruta;
 		}
 
		// Retorna la distanfia fisica entre dues parades, independent de les rutes
 		public int distParadaFisica(Parada desti)
 		{
       return Math.abs(x - desti.x) + Math.abs(y - desti.y);
 		}
 
 		// Retorna la distancia entre dues parades, fent transbord si cal
 		public int distParada(Parada desti)
 		{
 		  int d;
       if (this.ruta == desti.ruta)
       { // Estan a la mateixa ruta, calcular distancia directa
         // Fem un petit truco: dist entre dos parades d'una ruta és la resta de la dist entre origen i parada1 i origen i parada2...
         d = Math.abs(rutes[this.ruta].distOrigen(this) - rutes[this.ruta].distOrigen(desti));
       }
       else
       {
         // Cal calcular quina es la versio mes rapida, passant per origen o per final
         int dorig, dfin;
         dorig = rutes[this.ruta].distOrigen(this) + rutes[desti.ruta].distOrigen(desti);
         dfin = rutes[this.ruta].distFinal(this) + rutes[desti.ruta].distFinal(desti);
         d = Math.min(dorig, dfin);
       }
       return d;
 		}
 	}
 
 	class Ruta
 	{
 		int id;
 		int[] paradesRuta;
 		int numparades;
 	// Suma de les distancies entre totes les parades seguint l'ordre de la ruta
 		int dist;
 
 		public Ruta(int id_)
 		{
 			numparades = 0;
 			dist = new Parada(0,0,-1).distParadaFisica(new Parada(19, 19, -1));
 			paradesRuta = new int[P];
 			this.id = id_;
 		}
 
 		// Operador de copia
 		public Ruta(Ruta original)
 		{
 		  this.id = original.id;
 		  this.numparades = original.numparades;
 		  this.dist = original.dist;
       this.paradesRuta = new int[P];
       System.arraycopy(original.paradesRuta, 0, this.paradesRuta, 0, original.paradesRuta.length);
 		}
 
 		public void afegirParada(int id_)
 		{
 			// Comprovem que no sigui ja la parada a la ruta...
 			for(int i = 0; i < numparades; i++)
 			  if (paradesRuta[i] == id_) return;
 
 			paradesRuta[numparades] = id_;
 			parades[id_].ruta = this.id;
 			numparades++;
 			// Recalcular dist ruta
       dist = this.calcularDist();
 		}
 
 		public boolean treureParada(int id_)
 		{
 			int i;
 			if (numparades <= 1) return false;
 			for (i = 0; i < numparades && paradesRuta[i] != id_; i++);
 			for(; i < numparades-1; i++)
 			{
 				paradesRuta[i] = paradesRuta[i+1];
 			}
 			paradesRuta[numparades] = -1;
 			numparades--;
 			calcularDist(); // Recalculem distancia de la ruta
 			return true;
 		}
 
 		public boolean moureParada(int id_)
 		{
 			int i, aux;
 			for (i = 0; i < numparades && paradesRuta[i] != id_; i++);
 			aux = paradesRuta[i];
 			paradesRuta[i] = paradesRuta[(i+1) % numparades];
 			paradesRuta[(i+1) % numparades] = aux;
 			dist = calcularDist(); // Recalculem distancia de la ruta
 			return true;
 		}
 		
 		// Retorna la distancia total de la ruta
 		public int calcularDist()
 		{
 		  int d;
 		  if (numparades == 0) // Si no hi ha cap parada, distancia entre origen i final
         d = new Parada(0,0,-1).distParadaFisica(new Parada(19, 19, -1));
       else
       {
         // Si hi ha parades, calculem la dist entre origen i la primera parada...
         d = new Parada(0,0,-1).distParadaFisica(parades[paradesRuta[0]]);
         for (int i = 1; i < numparades; i++)
         { // Per cada parada sumem la distancia entre ella i l'anterior a ella
           d += parades[paradesRuta[i]].distParadaFisica(parades[paradesRuta[i-1]]);
         }
         // Finalment sumem la distancia entre la darrera parada i el final de ruta
         d += new Parada(19,19,-1).distParadaFisica(parades[paradesRuta[numparades-1]]);
       }
       return d;
 		}
 
 		// Assumim que parada forma part de la ruta, es una precondicio
 		public int distOrigen(Parada parada)
 		{
 		  int i, d;
 		  d = new Parada(0,0,-1).distParadaFisica(parades[paradesRuta[0]]); // Dist entre origen i primera parada
 
      // Si ja hem trobat la que buscave, finito
       if (paradesRuta[0] == parada.id) return d;
 
 		  // Comencem des d'1 perque assumim que aquesta ruta com a minim te una parada
 		  for(i = 1; i < numparades; i++)
 		  {
         d += parades[paradesRuta[i]].distParadaFisica(parades[paradesRuta[i-1]]);
         if(paradesRuta[i] == parada.id) break;
 		  }
 		  return d;
 		}
 
 		// Assumim que parada forma part de la ruta, es una precondicio
 		public int distFinal(Parada parada)
 		{
 		  int i, d;
 		  d = new Parada(19,19,-1).distParadaFisica(parades[paradesRuta[numparades-1]]); // Dist entre final i ultima parada
 
		  // Ja hem trobat la que buscavem, finito
       if (paradesRuta[numparades-1] == parada.id) return d;
 		  // Comencem des d'1 perque assumim que aquesta ruta com a minim te una parada
 		  for(i = numparades - 2; i >= 0; i--)
 		  {
         d += parades[paradesRuta[i]].distParadaFisica(parades[paradesRuta[i+1]]);
         if(paradesRuta[i] == parada.id) break;
 		  }
 
 		  return d;
 		}
 	}
 }
 
