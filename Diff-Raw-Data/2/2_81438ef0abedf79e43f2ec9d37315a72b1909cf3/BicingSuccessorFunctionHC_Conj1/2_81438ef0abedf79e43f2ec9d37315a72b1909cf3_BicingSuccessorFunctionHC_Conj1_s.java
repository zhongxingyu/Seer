 package bicing.aima;
 
 import java.util.List;
 import java.util.Vector;
 
 import aima.search.framework.Successor;
 import aima.search.framework.SuccessorFunction;
 import bicing.Ciudad;
 
 public class BicingSuccessorFunctionHC_Conj1 implements SuccessorFunction {
 
 	public List<Successor> getSuccessors(Object aState) {
 		Vector<Successor> result = new Vector<Successor>();
 		// Cast del objecto aState a ciudad
 		Ciudad estCiudad = (Ciudad) aState;
 
 		// Conjunto addTransporte, modTransporte, delTransporte
 
 		// -----------------------------------------------------------------------
 		// Aplicamos operador addTransporte
 		// -----------------------------------------------------------------------
 
 		// Por cada furgoneta disponible
 		for (int t = 0; t < Ciudad.getNumFurgonetas() - estCiudad.transportes.size(); t++) {
 			// Por cada origen
 			for (int origen = 0; origen < Ciudad.estaciones.getNumStations(); origen++) {
 				// Si no furgoneta y dnm > 0 y nh - ns > 0
 				if (!estCiudad.hayFurgonetaEnEstacion(origen) && (Ciudad.estaciones.getStationDoNotMove(origen) > 0) && ((Ciudad.estaciones.getDemandNextHour(origen) - Ciudad.estaciones.getStationNextState(origen)) > 0)) {
 					// Por cada parada 1
 					for (int paradaUno = 0; paradaUno < Ciudad.estaciones.getNumStations(); paradaUno++) {
 						// Si no origen
 						if (paradaUno != origen) {
 							// Por cada parada 2
 							for (int paradaDos = -1; paradaDos < Ciudad.estaciones.getNumStations(); paradaDos++) {
 								// Si paradaDos != de origen y paradaUno
 								if ((paradaDos != origen) && (paradaDos != paradaUno)) {
 
 									// Repartimos bicicletas
 									int bcOrigen = Ciudad.estaciones.getStationDoNotMove(origen);
 									int bcParadaUno = bcOrigen;
 									int bcParadaDos;
 
 									if (paradaDos != -1) {
 										// Miramos si son pares
 										if (bcOrigen % 2 == 0) {
 											bcParadaUno = bcOrigen / 2;
 											bcParadaDos = bcOrigen / 2;
 										} else {
 											bcParadaDos = bcOrigen / 2;
 											bcParadaUno = bcOrigen - paradaDos;
 										}
 									} else {
 										bcParadaDos = -1;
 									}
 
 									Ciudad nuevaCiudad = new Ciudad(estCiudad);
 									nuevaCiudad.addTransporte(origen, bcOrigen, paradaUno, bcParadaUno, paradaDos, bcParadaDos);
 									result.add(new Successor("", nuevaCiudad));
 
 								}
 							}
 						}
 					}
 				}
 			}
 
 		}
 
 		// -----------------------------------------------------------------------
 		// Aplicamos el operador modificarTransporte
 		// -----------------------------------------------------------------------
 
 		int origen, paradaUno, paradaDos;
 
 		// Por cada transporte
 		for (int t = 0; t < estCiudad.transportes.size(); t++) {
 			origen = estCiudad.transportes.get(t).getOrigen();
 			paradaUno = estCiudad.transportes.get(t).getParadaUno();
			paradaDos = estCiudad.transportes.get(t).getBcParadaDos();
 			int bcParadaUno;
 
 			// Por cada bicicleta en el origen
 			for (int bcOrigen = 1; bcOrigen <= Ciudad.estaciones.getStationDoNotMove(origen); bcOrigen++) {
 				// Capacidad furgoneta
 				if (bcOrigen <= 30) {
 
 					// Hay paradaDos
 					if (paradaDos != -1) {
 						// Por cada bcParadaUno
 						for (bcParadaUno = 1; bcParadaUno < bcOrigen; bcParadaUno++) {
 
 							Ciudad nuevaCiudad = new Ciudad(estCiudad);
 							nuevaCiudad.modTransporte(t, origen, bcOrigen, paradaUno, bcParadaUno, paradaDos, (bcOrigen - bcParadaUno));
 							result.add(new Successor("", nuevaCiudad));
 						}
 
 					} else {
 						bcParadaUno = bcOrigen;
 
 						Ciudad nuevaCiudad = new Ciudad(estCiudad);
 						nuevaCiudad.modTransporte(t, origen, bcOrigen, paradaUno, bcParadaUno, paradaDos, -1);
 						result.add(new Successor("", nuevaCiudad));
 					}
 
 				}
 			}
 
 		}
 
 		// -----------------------------------------------------------------------
 		// Aplicamos operador delTransporte
 		// -----------------------------------------------------------------------
 
 		// Por cada transporte
 		for (int t = 0; t < estCiudad.transportes.size(); t++) {
 			Ciudad nuevaCiudad = new Ciudad(estCiudad);
 			nuevaCiudad.delTransporte(t);
 			result.add(new Successor("", nuevaCiudad));
 		}
 
 		return result;
 	}
 }
