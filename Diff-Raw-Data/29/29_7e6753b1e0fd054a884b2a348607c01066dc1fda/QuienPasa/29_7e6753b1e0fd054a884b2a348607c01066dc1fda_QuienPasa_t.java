 /**
  * 
  */
 package reglas;
 
 import models.*;
 
 /**
  * @author lopez
 * Esta Regla sirve para cacular el Final de los equipo basado en la Abreviacion de Equipo
  * 
  */
 public class QuienPasa extends ReglaBase {
 
	enum enumTipo {
		Grupo,
		PartidoGanador,
		PartidoPerdido
	}
	class DependeDe {
		enumTipo Tipo;
		Long Dato;
	}
	
 	public QuienPasa(String Parametro) {
 		super(Parametro);
 	}
 
 	@Override
 	public long cacular(Pronostico Pronostico) {
 		return -1;
 	}
	public DependeDe DependeDe(){
		return null;
		
	}
 	@Override
 	public long cacular(Torneo Torneo) {
		for(Equipo Equipo: Torneo.getEquipos()){
			if(Equipo.caculable) {
				if(Equipo.Abreviatura.matches("")) {
					
				}
			}
		}
		return 0;
 	}
 
 	@Override
 	public long Generar(Pronostico Pronostico) {
 		return -1;
 	}
 
 	@Override
 	public String IncluirJS() {
 		return null;
 	}
 
 
 }
