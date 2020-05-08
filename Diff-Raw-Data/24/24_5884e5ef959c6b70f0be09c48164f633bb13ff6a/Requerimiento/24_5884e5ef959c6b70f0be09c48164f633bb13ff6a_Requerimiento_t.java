 package ar.edu.utn.tadp.requerimiento;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import org.joda.time.DateTime;
 import org.joda.time.Hours;
 
 import ar.edu.utn.tadp.propiedad.Propiedad;
 import ar.edu.utn.tadp.recurso.Recurso;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 
 /**
  * Representa un objeto que sabe filtrar a los recursos. Y tiene cargadas las
  * condiciones.
  * 
  * @author clari
  */
 public class Requerimiento {
 
 	private Collection<Propiedad> condiciones;
 	private Collection<Recurso> meSatisfacen;
 
 	public Requerimiento(Collection<Propiedad> condiciones) {
 		this.condiciones = condiciones;
 	}
 
 	/**
 	 * @param recursos
 	 *            una coleccion de <code>Recurso</code>
 	 * @return una colección de Iterables con los elementos que cumplen con el
 	 *         criterio
 	 */
 	public Collection<Recurso> filtrarConjunto(Collection<Recurso> recursos) {
 		final Requerimiento self = this;
 		Predicate<Recurso> p = new Predicate<Recurso>() {
 			public boolean apply(Recurso unRecurso) {
				return self.cumpleCondiciones(unRecurso);
 			}
 		};
 		return Sets.newHashSet(Iterables.filter(recursos, p));
 	}
 
 	/**
 	 * @return verdadero si todas las condiciones estan incluidas entre las
 	 *         propiedades del recurso, que tendra otras mas.
 	 */

	public boolean cumpleCondiciones(Recurso unRecurso) {
			final Recurso recursoActual=unRecurso;
		Predicate<Propiedad> p= new Predicate<Propiedad>(){
			public boolean apply(Propiedad propiedad){
				return Iterables.contains(recursoActual.getPropiedades(), propiedad);	
			}
		};
		
		return Iterables.all(this.condiciones, p);
 	}
 
 	public void agregarCondiciones(Set<Propiedad> unasCondiciones) {
 		this.condiciones = unasCondiciones;
 	}
 
 	public void buscaLosQueTeSatisfacen(List<Recurso> recursos) {
 		meSatisfacen = this.filtrarConjunto(recursos);
 		
 	}
 
 	public ArrayList<Recurso> teSatisfacenDurante(Hours horas, DateTime vencimiento) {
 		ArrayList<Recurso> recursos = new ArrayList<Recurso>();
 		for (Recurso recurso : meSatisfacen){
 			if (recurso.tenesDisponibleAntesDe(horas, vencimiento)) recursos.add(recurso);
 		}
 		if (recursos.isEmpty()) throw new RuntimeException();
 		return recursos;
 	}
 }
