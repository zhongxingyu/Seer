 package bicing;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Random;
 import java.util.Vector;
 
 import IA.Bicing.Bicing;
 
 public class Ciudad {
 
 	public static Bicing estaciones;
 	public static Integer numFurgonetas;
 	public static Random random;
 
 	public Vector<Transporte> transportes;
 	public Vector<Boolean> estacionesOcupadas;
 
 	public Ciudad(int est, int bc, int f, int demanda, int seed, Random pRandom) {
 		// init estaciones
 		estaciones = new Bicing(est, bc, demanda, seed);
 		// init transportes
 		this.transportes = new Vector<Transporte>();
 		// vars
 		this.estacionesOcupadas = new Vector<Boolean>();
 		numFurgonetas = f;
 		random = pRandom;
 
 		// Marcamos las estaciones como libres
 		for (int i = 0; i < estaciones.getNumStations(); i++) {
 			this.estacionesOcupadas.add(false);
 		}
 	}
 
 	// Copia
 	public Ciudad(Ciudad ciudadOriginal) {
 		// Copiamos las variables no estaticas
 		transportes = new Vector<Transporte>();
 		estacionesOcupadas = new Vector<Boolean>();
 
 		// Copiamos los transportes
 		for (int i = 0; i < ciudadOriginal.transportes.size(); i++) {
 			transportes.add(ciudadOriginal.transportes.get(i));
 		}
 
 		// Copiamos las estaciones ocupadas
 		for (int i = 0; i < ciudadOriginal.estacionesOcupadas.size(); i++) {
 			estacionesOcupadas.add(ciudadOriginal.estacionesOcupadas.get(i));
 		}
 
 	}
 
 	public void initEstrategiaSimple() {
 		// Estrategia simple para crear estado inicial
 		// Inicializamos f transportes de forma aleatoria comprobando que no
 		// repetimos furgonetas por estacion y que una furgoneta no sale con mas
 		// bicicletas de las disponibles (las sobrantes)
 		int timeout = 0;
 
 		Vector<Integer> estacionOcupada = new Vector<Integer>();
 		int numEstaciones = estaciones.getNumStations();
 		int aux, st;
 		Transporte transAux;
 
 		for (int i = 0; i < numFurgonetas; i++) {
 
 			// No enviamos furgonetas a estaciones donde no sobraran o no se
 			// pueden llevar ninguna
 			st = (random.nextInt(numEstaciones));
 			timeout = 0;
 			while (estacionOcupada.contains(st) || ((estaciones.getStationNextState(st) <= estaciones.getDemandNextHour(st)) || (estaciones.getStationDoNotMove(st) <= 0))) {
 				st = (random.nextInt(numEstaciones));
 				timeout = timeout + 1;
 				if (timeout > numEstaciones) {
 					break;
 				}
 			}
 
 			if (timeout > numEstaciones) {
 				while (estacionOcupada.contains(st)) {
 					st = (random.nextInt(numEstaciones));
 				}
 			}
 			estacionOcupada.add(st);
 
 			// st = estacion de donde sale la furgoneta
 			transAux = new Transporte();
 
 			transAux.setOrigen(st);
 
 			// Calculamos bicicletas de salida
 			aux = random.nextInt(estaciones.getStationDoNotMove(st));
 			if (aux > 30) {
 				transAux.bcOrigen = 30;
 			} else {
 				transAux.bcOrigen = aux + 1;
 			}
 
 			// Decidimos numero de paradas
 			if (random.nextInt(2) == 0) {
 				// una parada
 				aux = random.nextInt(numEstaciones);
 				while (aux == transAux.getOrigen()) {
 					aux = random.nextInt(numEstaciones);
 				}
 				transAux.setParadaUno(aux);
 				transAux.setBcParadaUno(transAux.bcOrigen);
 
 			} else {
 				// dos paradas
 				aux = random.nextInt(numEstaciones);
 				while (aux == transAux.getOrigen()) {
 					aux = random.nextInt(numEstaciones);
 				}
 				transAux.setParadaUno(aux);
 				transAux.setBcParadaUno(random.nextInt(transAux.bcOrigen));
 
 				aux = random.nextInt(numEstaciones);
 				while (aux == transAux.getParadaUno() || aux == transAux.getOrigen()) {
 					aux = random.nextInt(numEstaciones);
 				}
 
 				transAux.setParadaDos(aux);
 				transAux.setBcParadaDos(transAux.bcOrigen - transAux.getBcParadaUno());
 
 			}
 			transportes.add(transAux);
 		}
 
 	}
 
 	// ordena el vector v en orden descendente
 	public void ordena(Vector<EstacionesCompare> v) {
 		Collections.sort(v, new Comparator<Object>() {
 			public int compare(Object a, Object b) {
 				return (-(new Integer(((EstacionesCompare) a).getSobrantes())).compareTo(new Integer(((EstacionesCompare) b).getSobrantes())));
 			}
 		});
 	}
 
 	public void initEstrategiaElaborada() {
 		// TODO
 		// Estrategia elaborada para crear estado inicial
 		// Inicializamos f(o menos) transportes de forma que haya furgonetas
 		// en las estaciones con mayor numero de bicicletas sobrantes
 		// comprobando que no repetimos furgonetas por estacion y que una
 		// furgoneta no sale con mas bicicletas de las disponibles (las
 		// sobrantes)
 
 		// estAuxSobrantes es un vector ordenado segun las bicicletas sobrantes
 		// estAuxDemanda es un vector ordenado segun las bicicletas que faltaran
 		// para cubrir la demanda
 		Vector<EstacionesCompare> estAuxSobrantes = new Vector<EstacionesCompare>();
 		Vector<EstacionesCompare> estAuxDemanda = new Vector<EstacionesCompare>();
 		for (int i = 0; i < estaciones.getNumStations(); i++) {
 			int notMove = estaciones.getStationDoNotMove(i);
 			int faltan = estaciones.getDemandNextHour(i) - estaciones.getStationNextState(i);
 
 			if (notMove > 0) {
 				// limitamos el numero de bicicletas que cargaremos a 30
 				if (notMove > 30)
 					notMove = 30;
 
 				EstacionesCompare aux = new EstacionesCompare(i, notMove);
 				estAuxSobrantes.add(aux);
 			}
 			if (faltan > 0) {
 				EstacionesCompare aux2 = new EstacionesCompare(i, faltan);
 				estAuxDemanda.add(aux2);
 			}
 		}
 
 		ordena(estAuxSobrantes);
 		ordena(estAuxDemanda);
 
 		System.out.println("Do not move: ");
 		for (int i = 0; i < estAuxSobrantes.size(); i++)
 			System.out.print(estAuxSobrantes.elementAt(i).getSobrantes() + ", ");
 
 		System.out.println("Faltan: ");
 		for (int i = 0; i < estAuxDemanda.size(); i++)
 			System.out.print(estAuxDemanda.elementAt(i).getSobrantes() + ", ");
 
 		System.out.println("Furgonetas: " + numFurgonetas);
 
 		for (int i = 0; (i < numFurgonetas) && (estAuxSobrantes.size() > 0) && (estAuxDemanda.size() > 0); i++) {
 			System.out.print("1 ");
 			Transporte trans = new Transporte();
 			EstacionesCompare estDem0 = estAuxDemanda.elementAt(0);
 			EstacionesCompare estSob0 = estAuxSobrantes.elementAt(0);
 
 			trans.setOrigen(estSob0.getOrigen());
 			trans.setParadaUno(estDem0.getOrigen());
 			int diferencia = estDem0.getSobrantes() - estSob0.getSobrantes();
 
 			// si la estacion necesitaba mas bicicletas de las que se pueden
 			// transportar, solo habra una parada con bcParadaUno = sobrantes
 			// de la parada origen.
 			if (diferencia > 0) {
 				trans.setBcParadaUno(estSob0.getSobrantes());
 				trans.setBcOrigen(estSob0.getSobrantes());
 
 				// actualizamos vector de demandas
 				estDem0.setSobrantes(estDem0.getSobrantes() - estSob0.getSobrantes());
 				if ((estAuxDemanda.size() > 1) && (estDem0.getSobrantes() < estAuxDemanda.elementAt(1).getSobrantes()))
 					ordena(estAuxDemanda);
 			}
 
 			// en caso contrario, habra dos paradas
 			else {
 				trans.setBcParadaUno(estDem0.getSobrantes());
 				estAuxDemanda.remove(0);
				estSob0.setSobrantes(estSob0.getSobrantes() - trans.getBcParadaUno());
 
 				if (estAuxDemanda.size() > 0) {
 					EstacionesCompare estDem1 = estAuxDemanda.elementAt(0);
 					trans.setParadaDos(estDem1.getOrigen());
					if (estDem1.getSobrantes() > estSob0.getSobrantes()) {
						trans.setBcParadaDos(estSob0.getSobrantes());
 						estDem1.setSobrantes(estDem1.getSobrantes() - trans.getBcParadaDos());
 						if ((estAuxDemanda.size() > 1) && (estDem1.getSobrantes() < estAuxDemanda.elementAt(1).getSobrantes()))
 							ordena(estAuxDemanda);
					} else {
						trans.setBcParadaDos(estDem1.getSobrantes());
						estAuxDemanda.remove(0);
 					}
 				}
 				trans.setBcOrigen(trans.getBcParadaUno() + trans.getBcParadaDos());
 			}
 			estAuxSobrantes.remove(0);
 			transportes.add(trans);
 		}
 
 		System.out.println("");
 		printTransportes();
 
 	}
 
 	public double getBeneficios() {
 		// TODO
 		// Devuelve los beneficios obtenidos por llevar bicicletas y acercarse a
 		// la demanda de cada estacion
 
 		// El acuerdo con Bicing incluye que nos pague un euro por cada
 		// bicicleta que transportemos que haga que el numero de bicicletas de
 		// una estacion se acerque a la demanda
 
 		// Por contra nos cobrara un euro por cada bicicleta que transportemos
 		// que aleje a una estacion de su prevision. Es decir, nos descontaran
 		// por las bicicletas que movamos que hagan que una estacion quede por
 		// debajo de la demanda prevista.
 		return 0.0;
 	}
 
 	public double getGastos() {
 		// Devuelve los gastos producidos debido a los transportes de bicicletas
 
 		// suponiendo que nb es el numero de bicicletas que transportamos en una
 		// furgoneta y d(i,j) es la distancia entre dos estaciones, el coste en
 		// euros es: coste(i,j)=((nb div 10)+1) * d(i,j)
 
 		double gastos = 0.0;
 
 		for (int i = 0; i < transportes.size(); i++) {
 			// Gastos origen estacion uno
 			gastos = gastos + (((transportes.get(i).getBcOrigen() / 10) + 1) * estaciones.getStationsDistance(transportes.get(i).getOrigen(), transportes.get(i).getParadaUno()));
 
 			// Gastos estacion uno estacion dos
 			gastos = gastos + ((((transportes.get(i).getBcOrigen() - transportes.get(i).getBcParadaUno()) / 10) + 1) * estaciones.getStationsDistance(transportes.get(i).getParadaUno(), transportes.get(i).getParadaDos()));
 
 		}
 
 		return gastos;
 	}
 
 	public static Integer getNumFurgonetas() {
 		return numFurgonetas;
 	}
 
 	public int getTransporteConEstacion(int st) {
 		// Devuelve el identificador del transporte (indice en el vector
 		// transportes) del transporte con furgoneta en la estacion origen st
 
 		for (int i = 0; i < this.transportes.size(); i++) {
 			if (this.transportes.get(i).getOrigen() == st) {
 				return i;
 			}
 		}
 		return -1;
 
 	}
 
 	public Boolean hayFurgonetaEnEstacion(int st) {
 		// Devuelve cierto si existe un transporte con origen igual a st, falso
 		// en caso contrario
 		return this.estacionesOcupadas.elementAt(st);
 	}
 
 	public void addTransporte(int origen, int bcOrigen, int paradaUno, int bcParadaUno, int paradaDos, int bcParadaDos) {
 		// Creamos un nuevo transporte y lo aadimos al vector de transportes
 		Transporte transAux = new Transporte();
 		transAux.setOrigen(origen);
 		transAux.setBcOrigen(bcOrigen);
 		transAux.setParadaUno(paradaUno);
 		transAux.setBcParadaUno(bcParadaUno);
 		transAux.setParadaDos(paradaDos);
 		transAux.setBcParadaDos(bcParadaDos);
 		this.transportes.add(transAux);
 
 		// Marcamos la estacion como ocupada
 		this.estacionesOcupadas.set(origen, true);
 	}
 
 	public void modificarTransporte(int origen, int nuevoOrigen, int bcOrigen, int paradaUno, int bcParadaUno, int paradaDos, int bcParadaDos) {
 		Transporte transAux = this.transportes.get(getTransporteConEstacion(origen));
 		transAux.setOrigen(nuevoOrigen);
 		transAux.setBcOrigen(bcOrigen);
 		transAux.setParadaUno(paradaUno);
 		transAux.setBcParadaUno(bcParadaUno);
 		transAux.setParadaDos(paradaDos);
 		transAux.setBcParadaDos(bcParadaDos);
 
 		// Marcamos la nueva estacion como ocupada
 		if (origen != nuevoOrigen) {
 			this.estacionesOcupadas.set(origen, false);
 			this.estacionesOcupadas.set(nuevoOrigen, true);
 		}
 	}
 
 	public void printEstaciones() {
 		// print estaciones
 		System.out.println("Numero de estaciones: " + estaciones.getNumStations());
 		for (int i = 0; i < estaciones.getNumStations(); i++) {
 			System.out.print("Estacion " + i);
 			System.out.print(", coord[" + estaciones.getStationCoord(i)[0] + "," + estaciones.getStationCoord(i)[1] + "]");
 			System.out.print(" - prev_demand_next_hr: " + estaciones.getDemandNextHour(i));
 			System.out.print(" - prev_do_not_move: " + estaciones.getStationDoNotMove(i));
 			System.out.print(" - prev_after_user_moves: " + estaciones.getStationNextState(i));
 			System.out.println();
 		}
 	}
 
 	public void printTransportes() {
 		System.out.println("Numero de transportes: " + transportes.size());
 		for (int i = 0; i < transportes.size(); i++) {
 			System.out.print("Transporte " + i);
 			System.out.print(", STorigen: " + transportes.get(i).getOrigen());
 			System.out.print(", BCorigen: " + transportes.get(i).getBcOrigen());
 			System.out.print(", STuno: " + transportes.get(i).getParadaUno());
 			System.out.print(", BCuno: " + transportes.get(i).getBcParadaUno());
 			System.out.print(", STdos: " + transportes.get(i).getParadaDos());
 			System.out.print(", BCdos: " + transportes.get(i).getBcParadaDos());
 			System.out.println();
 		}
 
 	}
 
 }
