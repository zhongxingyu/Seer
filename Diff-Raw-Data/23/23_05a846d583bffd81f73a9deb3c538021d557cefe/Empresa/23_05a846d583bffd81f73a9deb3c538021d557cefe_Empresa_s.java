 package ar.edu.utn.tadp.empresa;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.Hours;
 import org.joda.time.Interval;
 
 import ar.edu.utn.tadp.recurso.Persona;
 import ar.edu.utn.tadp.recurso.Recurso;
 import ar.edu.utn.tadp.requerimiento.Requerimiento;
 import ar.edu.utn.tadp.reunion.Reunion;
 
 
 /**
  * Representa a una Empresa. Contiene todos los recursos.
  */
 public class Empresa {
 
 	private List<Recurso> recursos = new ArrayList<Recurso>();
 
 	public Reunion createReunion(Persona anfitrion, List<Requerimiento> criterios, Hours horas, DateTime vencimiento) {
 		ArrayList<ArrayList<Recurso>> candidatos = new ArrayList<ArrayList<Recurso>>();
 
 		this.satisfaceRequerimientos(criterios);
 		candidatos = seleccionarCandidatos(criterios, horas, vencimiento);
 
 		ArrayList<Recurso> asistentes = new ArrayList<Recurso>();
 
 		try {
 			asistentes = this.seleccionarCandidatos(candidatos);
 		} catch (NoHayAsistentesDisponiblesException e){
 			throw new CantMakeReunionException(e);
 		}
 
 		/*
 		 * Se supone que ocuparAsistente no puede fallar, por eso no va
 		 * con try/catch si falla estamos al horno, porque estan dadas las
 		 * condiciones para que no falle nunca.
 		 */
 		Interval intervalo = ocuparAsistentes(horas, asistentes);
 		
 		return new Reunion(anfitrion, asistentes, intervalo);
 	}
 
 	private Interval ocuparAsistentes(Hours horas, ArrayList<Recurso> asistentes) {
 		Interval intervalo = new Interval(0, 0);
 
 		for (Recurso recurso : asistentes) {
 			intervalo = recurso.intervaloDisponibleDe(horas.toStandardDuration());
 			intervalo = intervalo.withEnd(intervalo.getStart().plus(horas.toStandardDuration()));
 
 			if (todosLosAsistentesTienenDisponibleElIntervalo(asistentes, intervalo)) {
 				// si lo tienen, ocupo a todos los recursos.
 				recurso.getAgenda().ocupateDurante(intervalo);
 			}
 
 		}

 		
 		for (Recurso recurso : asistentes) {
 			recurso.ocupateDurante(intervalo);
 		}
 		return intervalo;
		}
 		
		for (Recurso recurso : asistentes) {
			recurso.ocupateDurante(intervalo);
		}
		return intervalo;
 	}
 
 	private ArrayList<ArrayList<Recurso>> seleccionarCandidatos(
 			List<Requerimiento> criterios, Hours horas, DateTime vencimiento) {
 		ArrayList<ArrayList<Recurso>> candidatos = new ArrayList<ArrayList<Recurso>>();
 		
 		for (Requerimiento requerimiento : criterios) {
 			candidatos.add(requerimiento.teSatisfacenDurante(horas, vencimiento));
 		}
 		
 		return candidatos;
 	}
 
 	private void satisfaceRequerimientos(List<Requerimiento> criterios) {
 		for (Requerimiento requerimiento : criterios) {
 			requerimiento.buscaLosQueTeSatisfacen(recursos);
 		}
 	}
 
 	private ArrayList<Recurso> seleccionarCandidatos(
 			ArrayList<ArrayList<Recurso>> candidatos) {
 		ArrayList<Recurso> asistentes = new ArrayList<Recurso>();
 		for (ArrayList<Recurso> recursos : candidatos) {
 			asistentes.add(recursos.get(0));
 		}
 		if (asistentes.isEmpty())
 			throw new NoHayAsistentesDisponiblesException();
 		return asistentes;
 	}
 
 	public void addRecurso(Recurso recurso) {
 		this.recursos.add(recurso);
 	}
 
 	public void removeRecurso(Recurso recurso) {
 		this.recursos.remove(recurso);
 	}
 
 	public void removeAllRecurso() {
 		this.recursos.removeAll(this.recursos);
 	}
 }
