 package algoritmoGenetico;
 
 import java.util.ArrayList;
 
 import controlador.Parametros;
 
 /**
  * Clase que implementa el algoritmo genetico principal
  * 
  */
 public class AlgoritmoGenetico 
 {
 	private int longitudCromosoma;
 
 	// Informacion para las graficas
 	private Cromosoma[] gokus;
 	private Cromosoma[] mejoresCromosomas;
 	private double[] medias;
 	
 	// Informacion para las tablas
 	private ArrayList<Cromosoma> picos;
 	private ArrayList<Integer> generacionesPicos;
 	
 	/**
 	 * Implementacion del algoritmo genetico.
 	 * Busca: 
 	 *  - el mejor cromosoma obtenido tras todas las genraciones (Goku)
 	 *  - la lista de los mejores cromosomas en cada generacion (mejoresCromosomas)
 	 *  - los picos
 	 *  
 	 * @param parametros del problema
 	 */
 	public void algoritmo_genetico(Parametros parametros) {
 		double porcElite = parametros.getElitismo();
 		// Obtenemos la poblacion inicial 
 		Cromosoma[] pob = poblacion_inicial(parametros); 
 		int pos_mejor = evaluarPoblacion(pob);	
 
 		// Informacion para las graficas
 		mejoresCromosomas = new Cromosoma[parametros.getNumGeneraciones()];
 		gokus = new Cromosoma[parametros.getNumGeneraciones()];
 		medias = new double[parametros.getNumGeneraciones()];
 		Cromosoma mejor = null;
 		
 		Cromosoma[] elite = new Cromosoma[(int) (parametros.getTamPoblacion()*porcElite)];
 		
 		// bucle de evolucion
 		for (int i = 0; i < parametros.getNumGeneraciones(); i++) {
 		// 0) cogemos a la elite
 			elite = separaMejores(pob,porcElite);
 			
 		// 1) seleccion
 			if (parametros.getSeleccion() == 0)
 				pob = seleccionTorneo(pob, parametros);
 			
 			if (parametros.getSeleccion() == 1)
 				pob = seleccionRuleta(pob, parametros);
 			
 		// 2) reproduccion
 			pob = reproduccion(pob, parametros);
 			
 		// 3) mutacion
 			pob = mutacion(pob, parametros);
 			
 		// 4) volvemos a integrar a la elite			
 			incluye(elite,pob);
 			
 		// 5) tratar la nueva solucion
 			pos_mejor = evaluarPoblacion(pob);
 			
 			if(i == 0){
 				// primera generacion: no esta inicializado <mejor> ni <picoAnterior>	
 				mejor = pob[pos_mejor].clone();
 			} else {
 				if (pob[pos_mejor].getAptitud() > mejor.getAptitud()){
 					mejor=pob[pos_mejor].clone();
 				}
 			}
 			
 		// 6) guardar los resultados
 			mejoresCromosomas[i]=pob[pos_mejor].clone();
 			gokus[i]=mejor.clone();
 			medias[i]=calcularMedia(pob);
 		}
 	}
 
 	/**
 	 * Inserta una elite en una poblacion sustituyendo los primeros cromosomas 
 	 * por los mejores extraidos anteriormente.
 	 * @param elite
 	 * @param poblacion 
 	 */
 	private void incluye(Cromosoma[] elite, Cromosoma[] pob) {
 		for (int i=0; i<elite.length; i++){
 			pob[i]=elite[i];
 		}
 	}
 
 	/**
 	 * Dada una poblacion devuelve a los mejores
 	 * @param poblacion inicial
 	 * @param porcentaje de elite a seleccionar
 	 * @return mejores cromosomas
 	 */
 	private Cromosoma[] separaMejores(Cromosoma[] pob, double porcElite) {
 		int tamElite = (int) (pob.length*porcElite);
 		Cromosoma[] elite = new Cromosoma[tamElite];
 		Cromosoma[] pobAux = pob.clone();
 		int posMejor;
 		for (int i=0; i<tamElite; i++){
 			posMejor=dameMejor(pobAux);
 			elite[i]=pobAux[posMejor].clone();
 			pobAux[posMejor]=null;
 		}
 		return elite;
 	}
 	/**
 	 * Dada una poblacion calcula la media de sus aptitudes
 	 * @param poblacion
 	 * @return media
 	 */
 	private double calcularMedia(Cromosoma[] pob) {
 		double media=0;
 		for (int i=0; i<pob.length;i++){
 			media+=pob[i].getAptitud();
 		}
 		return media/pob.length;
 	}
 
 	/** 
 	 * Metodo para obtener una poblacion inicial aleatoria
 	 * 
 	 * @param parametros del problema
 	 * @return poblacion generada
 	 */
 	private Cromosoma[] poblacion_inicial(Parametros param) 
 	{
 		int tam = param.getTamPoblacion();
 		Cromosoma[] pob = new Cromosoma[tam];
 		
 		for (int i = 0; i < tam; i++) {
			pob[i] = new Cromosoma();
 		}		
 		return pob;
 	}
 	
 	/**
 	 * La funcion de seleccion escoge un numero de supervivientes
 	 * igual al tam de la poblacion.
 	 * 
 	 * Metodo de seleccion => Seleccion por ruleta.
 	 * 
 	 * @param poblacion
 	 * @param parametros del problema
 	 * @return Poblacion seleccionada
 	 */
 	private Cromosoma[] seleccionRuleta(Cromosoma[] pob, Parametros parametros) 
 	{
 		Cromosoma[] poblacionSeleccionada = new Cromosoma[parametros.getTamPoblacion()];
 		int posicionSuperviviente;
 		
 		double random;
 		for (int i = 0; i < parametros.getTamPoblacion(); i++) {
 			random = Math.random();
 			posicionSuperviviente = 0;
 			while ( (random > pob[posicionSuperviviente].getPuntuacionAcumulada()) &&
 					(posicionSuperviviente < parametros.getTamPoblacion()) ) {
 				posicionSuperviviente++;
 			}
 			
 			poblacionSeleccionada[i] = (Cromosoma) pob[posicionSuperviviente].clone();
 		}
 		
 		return poblacionSeleccionada;
 	}
 	
 	/**
 	 * La funcion de seleccion escoge un numero de supervivientes
 	 * igual al tam de la poblacion.
 	 * 
 	 * Metodo de seleccion => Seleccion por torneo.
 	 * 
 	 * @param poblacion
 	 * @param parametros del problema
 	 * @return Poblacion seleccionada
 	 */
 	private Cromosoma[] seleccionTorneo(Cromosoma[] pob, Parametros parametros)
 	{
 		Cromosoma[] poblacionSeleccionada = new Cromosoma[parametros.getTamPoblacion()];
 		int tamTorneo=3;
 		Cromosoma mejor=null;
 		int random;
 		for(int i=0; i<parametros.getTamPoblacion();i++) {
 			for(int j=0; j<tamTorneo; j++){
 				random=Operaciones.aleatorioEntre(0,parametros.getTamPoblacion()-1);
 				if (mejor==null || (pob[random].getAptitud() > mejor.getAptitud()))
 					mejor = pob[random];
 			}
 			poblacionSeleccionada[i]=mejor.clone();
 		}
 		return poblacionSeleccionada;
 	}
 
 	/**
 	 * La reproduccion consiste en la seleccion de los individuos a reproducirse
 	 * y en la aplicacion del operador de cruce a cada una de las parejas.
 	 * 
 	 * @param poblacion
 	 * @param parametros del problema
 	 * @return nueva poblacion resultante
 	 */
 	private Cromosoma[] reproduccion(Cromosoma[] pob, Parametros parametros) 
 	{
 		// 1. Seleccionados para reproducir
 		int[] selCruce= new int[parametros.getTamPoblacion()];
 		
 		// 2. Contador seleccionados
 		int numSelCruce = 0;
 		int puntoCruce;
 		double prob;	
 		
 		// 3. Se eligen los individuos a cruzar
 		for (int i=0; i<parametros.getTamPoblacion(); i++){
 			prob=Math.random();
 			if (prob < parametros.getProbCruce()){
 				selCruce[numSelCruce] = i;
 				numSelCruce++;
 			}
 		}
 		
 		// 4. El numero de seleccionados se hace par
 		if ((numSelCruce % 2) == 1){
 			numSelCruce--;
 		}
 		
 		// 5. Se cruzan los individuos elegidos en un punto al azar
 		for (int i=0; i<numSelCruce; i+=2){
 			puntoCruce = Operaciones.aleatorioEntre(0, longitudCromosoma - 1);
 			Cromosoma[] cromos = cruce(pob[selCruce[i]],pob[selCruce[i+1]],puntoCruce,parametros);
 			//los nuevos individuos sustituyen a sus progenitores
 			pob[selCruce[i]]=cromos[0];
 			pob[selCruce[i+1]]=cromos[1];
 		}
 		return pob;
 	}
 
 	/**
 	 * La mutacion cambia ciertos genes de ciertos individuos de la poblacion
 	 * 
 	 * @param poblacion
 	 * @param paramametros del problema
 	 */
 	private Cromosoma[] mutacion(Cromosoma[] pob, Parametros parametros)
 	{
 		Cromosoma[] pobMutante = new Cromosoma[parametros.getTamPoblacion()];
 		
 		double random;
 		for (int i = 0; i < parametros.getTamPoblacion(); i++) {
 			pobMutante[i] = pob[i].clone();
 			for (int j = 0; j < longitudCromosoma; j++) {
 				random = Math.random();
 				if (random < parametros.getProbMutacion()) {
 					pobMutante[i].mutaGen(j);
 					pobMutante[i].evaluarCromosoma();
 				}
 			}
 		}
 		
 		return pobMutante;
 		
 	}
 
 	/**
 	 * El operador de cruce toma dos padres y genera dos cadenas hijas.
 	 * La funcin calcula la aptitud de los nuevos individuos.
  	 *
 	 * @param Cromosoma padre
 	 * @param Cromosoma madre
 	 * @param Cromosoma hijo1 (hijo)
 	 * @param Cromosoma hijo2 (hija)
 	 * @param puntoCruce
 	 */
 	private Cromosoma[] cruce(Cromosoma padre, Cromosoma madre, int puntoCruce, Parametros parametros) 
 	{
 		
 		//Inicializamos los hijos
		Cromosoma hijo=new Cromosoma();
		Cromosoma hija=new Cromosoma();
 		
 		// primera parte del intercambio: 
 		// copiar progenitor desde el principio hasta el punto de cruce
 		
 		for (int i = 0; i < puntoCruce; i++){
 			hijo.setGen(i, padre.getGenes()[i]);
 			hija.setGen(i, madre.getGenes()[i]);
 		}
 		
 		// segunda parte del intercambio:
 		// copiar desde el punto de cruce hasta el final del otro progenitor
 		
 		for (int i = puntoCruce; i < longitudCromosoma; i++){
 			hija.setGen(i, padre.getGenes()[i]);
 			hijo.setGen(i, madre.getGenes()[i]);
 		}
 		
 		// evaluar los nuevos individuos		
 		hijo.evaluarCromosoma();
 		hija.evaluarCromosoma();
 		
 		//devuelve los hijos
 		Cromosoma[] cromos = new Cromosoma[2];
 		cromos[0]=hijo;
 		cromos[1]=hija;
 		return cromos;
 		
 	}
 	
 	/**
 	 * 
 	 * @param poblacion a evaluar
 	 * @return la posicion del mejor individuo
 	 */
 	private int evaluarPoblacion(Cromosoma[] poblacion)
 	{
 		double puntuacionAcumulada = 0;
 		double aptitudMejor = 0;
 		double sumaAptitudes = 0;
 		
 		int pos_mejor = 0;
 		
 		for (int i = 0; i< poblacion.length; i++) {
 			//Calculamos la suma de las aptitudes
 			sumaAptitudes += poblacion[i].getAptitud();
 			//Buscamos la aptitud mejor y su posicion
 			if (poblacion[i].getAptitud() > aptitudMejor) {
 				pos_mejor = i;
 				aptitudMejor = poblacion[i].getAptitud();
 			}
 		}
 		
 		for (int i = 0; i< poblacion.length; i++) {
 			puntuacionAcumulada+=poblacion[i].getAptitud();
 			poblacion[i].setPuntuacionAcumulada((int) (puntuacionAcumulada/sumaAptitudes));
 		}
 		
 		return pos_mejor;
 	}
 	
 	/**
 	 * Dada una poblacion nos devuelve al mejor individuo
 	 * @param poblacion para buscar al mejor
 	 * @return la posicion del mejor individuo
 	 */
 	private int dameMejor(Cromosoma[] poblacion)
 	{
 		double aptitudMejor = 0;
 		
 		int pos_mejor = 0;
 		
 		for (int i = 0; i< poblacion.length; i++) {
 			if (poblacion[i]!=null && poblacion[i].getAptitud() > aptitudMejor) {
 				pos_mejor = i;
 				aptitudMejor = poblacion[i].getAptitud();
 			}
 		}
 		
 		return pos_mejor;
 	}
 	
 
 	
 	/* Getters */
 	public Cromosoma[] getGokus(){ return this.gokus; }
 	public Cromosoma[] getMejoresCromosomas() {return this.mejoresCromosomas; }
 	public double[] getMedias() {return this.medias; }
 	public ArrayList<Cromosoma> getPicos(){ return this.picos; }
 	public ArrayList<Integer> getGeneracionesPicos(){ return this.generacionesPicos; }
 	
 }
