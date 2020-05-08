 package simumatch.match;
 
 public class Turno {
 	String comentarista = "Turno ";
 	int numeroDeTurno;
 	public int estado;
 	String local, visitante;
 	Partido partido;
 	Turno(int estado, Partido p){
 		this.estado = estado;
 		this.partido = p;
 		local= p.local.nombre;
 		visitante= p.visitante.nombre;
 		numeroDeTurno = partido.turnoActual;
 
 		if(partido.turnoActual==partido.duracion/2)comentarista = "Descanso("+numeroDeTurno+") ";
 		else comentarista += numeroDeTurno+": ";
 		
 		switch(estado){
 		case  6 : com("�Gol de "+ local+"!");break;
 		case  5 : com(local+" esta dominado el juego, es cuestion de tiempo que marque");break;
 		case  1 : com(local+" tiene oportunidad de tomar la iniciativa");break;
 		case  0 : com("El partido esta muy igualado");break;
 		case -1 : com(visitante+" tiene oportunidad de tomar la iniciativa");break;
 		case -5 : com(visitante+" esta dominado el juego, es cuestion de tiempo que marque");break;
 		case -6 : com("�Gol de "+ visitante+"!");break;
 		default:
 			if(estado >6 || estado <-6){
 				System.out.println("Se ha llegado un estado incoherente: "+estado);
 				com("Parece que unos aliens han desembarcado en medio del campo...Yo me voy a mi casa!!!");
 				return;
 			}
 			if(estado>0) com(local+" lleva la iniciativa");
 			else 	 com(visitante+" lleva la iniciativa");
 		}
 		
 		if(partido.animoL>2*partido.animoV)com("Parece que "+local+" esta mucho mas animado que "+visitante);
 		else if(partido.animoV>2*partido.animoL)com("Parece que "+visitante+" esta mucho mas animado que "+local);
 		
 		if(estado>0){
 			if(partido.tacL==2)com("Parece que "+local+" no esta aprovechando su ventaja, si yo fuera el entrenador me enfadaria mucho");
 			if(partido.tacV==1)com("Parece que "+visitante+" no esta defendiendo debidamente, es una oportunidad magnifica para marcar");
 		}else if(estado<0){
 			if(partido.tacV==2)com("Parece que "+visitante+" no esta aprovechando su ventaja, si yo fuera el entrenador me enfadaria mucho");
 			if(partido.tacL==1)com("Parece que "+local+" no esta defendiendo debidamente, es una oportunidad magnifica para marcar");
 		}
 		
 		if(numeroDeTurno==partido.duracion){
 			com("�Final del partido!");
 			if(partido.goles()==0)com("EMPATE");
 			else if(partido.goles()> 0)com("Ganador: el equipo local: "+local);
 			else com("Ganador: el equipo visitante: "+visitante);
 		}
 		
 		comentarista+="\n["+partido.marL+"|"+partido.marV+"]\n\n";
 	}
 	public String toString(){
 		return comentarista;
 	}
 	void com(String comentario){
 		comentarista+="\n"+comentario+".";
 	}
	double[] getAbanico(){
 		if(numeroDeTurno!=partido.turnoActual)return null;
 		return partido.lastAbanico;
 	}
 }
