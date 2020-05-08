 //package IA.probTreballadors;
 
 import java.util.*;
 import java.util.Queue;
 
 public class probTreballadorsCiutat {
 
 	public class Treballador{
 		boolean conductor;
 		Posicio origen, desti;
 		int cotxe;
 
 		int ie,is;
 
 		public Treballador(Posicio origen, Posicio desti){
 			conductor = false;
 			this.origen = origen;
 			this.desti = desti;
 		}
 
 		public void setConductor(boolean cond){
 			conductor = cond;
 		}
 
 		public Posicio getOrigen() {
 			return this.origen;
 		}
 
 		public Posicio getDesti() {
 			return this.desti;
 		}
 	}
 
 	public class Posicio{
 
 		int x;
 		int y;
 
 		public Posicio(int x, int y){
 			this.x = x;
 			this.y = y;
 		}
 
 		//Getters i setters
 		public void setX (int valor){
 			x = valor;
 		}
 
 		public int getX(){
 			return(x);
 		}
 
 		public void setY (int valor){
 			y = valor;
 		}
 
 		public int getY(){
 			return(y);
 		}
 
 	}
 
 	public class Cotxe{
 		Treballador conductor;
 		int ordre[];
 
 		public Cotxe (Treballador c){
 			conductor = c;
 		}
 
 		/*x es l'id del treballador al array de treballadors*/
 		public void afegirAcompanyant(int x){
 
 		}
 		public void prioritzarEntrada(int x){
 			/*Realment necessitem una cua? Amb les cues de java natives no podem avançar elements.*/
 		}
 		public void prioritzarSortida(int x){
 			/*Realment necessitem una cua? Amb les cues de java natives no podem avançar elements.*/
 		}
 	}
 
 	private int N; //Nombre de treballadors
 	private int M; //Nombre de treballadors conductors.
 
 	private Cotxe [] cotxes;
 	private Treballador [] treballadors;
 	private int [] distanciaRecorrida;
 
 	private static int maxTrabajadoresCoche = 2;
 	private static int maximaDistanciaConductor = 300;
 	
 
 	public probTreballadorsCiutat(){
 		N = 10;
 		M = 8;
 
 		Date date = new Date();
 		long time = date.getTime();
 
 		Random rnd = new Random(time);
 
 		int n_conductors = 0;
 
 		treballadors = new Treballador[N];
 		cotxes = new Cotxe[N-M];
 		distanciaRecorrida = new int[N-M];
 
 		for (int i=0; i<N; i++){ 
 			int x_ori = rnd.nextInt(100);
 			int y_ori = rnd.nextInt(100);
 			Posicio origen = new Posicio (x_ori,y_ori);
 
 			int x_desti = rnd.nextInt(100);
 			int y_desti = rnd.nextInt(100);
 			Posicio desti = new Posicio (x_desti,y_desti);
 
 			Treballador t = new Treballador(origen,desti);
 
 			treballadors[i] = t;
 
 			if (n_conductors < N-M && rnd.nextBoolean()){
 				treballadors[i].conductor = true;
 				n_conductors++;
 			}
 
 			/* Prints dels resultats aleatoris */
 			System.out.println("=========Treballador " + i+ " ========");
 			System.out.println("origen amb x: " + x_ori + " y: " + y_ori);
 			System.out.println("desti amb x: " + x_desti + " y: " + y_desti);
 
 			if (treballadors[i].conductor){
 				System.out.println("SOC CONDUCTOR FUCK YEAHH!");
 			}
 
 			System.out.println("======================================");
 
 			System.out.println("===========Distancia camí=============");
 
 			System.out.println(distancia_recorregut(treballadors[i]));
 		}
 
 	}
 
 
 	/*
 	 Funcions utils que ens faran falta
 	 */
 
 	int abs (int x){
 		if (x<0) return -x;
 		else return x;
 	}
 
 	int distancia_recorregut(Treballador t){
 		return abs(t.desti.x - t.origen.x) + abs(t.desti.y - t.origen.y);
 	}
 
 	int distancia_dos_punts(Posicio a, Posicio b){
 		return abs(a.getX()-b.getX()) + abs(a.getY()-b.getY());
 	}
	int distancia_recorregut_cotxe(Cotxe c){
 
 		int distanciaPrimer = 0;
 		int distanciaAcompanyants = 0;
 		int distanciaUltim = 0;
 
 		/*Depenem de com montem l'estructura dels acompanyants*/
 
 		distanciaPrimer = distancia_dos_punts(c.conductor.origen,treballadors[c.ordre[0]].origen);
 		distanciaUltim = distancia_dos_punts(treballadors[c.ordre[2*M-1]].desti,c.conductor.desti);
 
 		int a=0;
 		int b=0;
 		boolean sortida = false;
 
 		Posicio anterior = treballadors[c.ordre[0]].origen;
 
 		for (int i=1;i<2*M;i++){
 
 			if (c.ordre[i] == a){
 				a = 0;
 				sortida = true;
 			}
 			else if	(c.ordre[i] == b){
 				b = 0;
 				sortida = true;
 			}
 			else if (a == 0){
 				a = i;
 				sortida = false;
 			}
 			else if (b == 0){
 				b = i;
 				sortida = false;
 			}
 
 			if (sortida){
				distanciaAcompanyants += distancia_dos_punts(c.ordre[i].desti,anterior); 
				anterior = c.ordre[i].desti;
 			}
 			else{
				distanciaAcompanyants += distancia_dos_punts(c.ordre[i].origen,anterior); 
				anterior = c.ordre[i].origen;
 			}
 		}
 		
 		return distanciaPrimer + distanciaAcompanyants + distanciaUltim;
 	}
 	void recalcularDistanciesCotxes(){
 		for (int i=0;i<N-M ;i++) distanciaRecorrida[i] = distanciaRecorregutCotxe(cotxes[i]);
 	}	
 
 
 	
 }
 
