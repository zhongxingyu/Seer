 package ar.edu.utn.tadp.requerimiento;
 
 import java.util.Collection;
 import java.util.Set;
 
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
		// si, le puse self :P y la tengo que declarar final porque sino se
		// queja ¬¬
 		final Requerimiento self = this;
 		Predicate<Recurso> p = new Predicate<Recurso>() {
 			public boolean apply(Recurso unRecurso) {
				return self.cumpleCondicion(unRecurso);
 			}
 		};
 		return Sets.newHashSet(Iterables.filter(recursos, p));
 	}
 
 	/**
 	 * @return verdadero si todas las condiciones estan incluidas entre las
 	 *         propiedades del recurso, que tendra otras mas.
 	 */
	// FIXME no cumple con el enunciado!
	public boolean cumpleCondicion(Recurso unRecurso) {
		boolean cumple = false;
		for (Propiedad propiedad : this.condiciones) {
			// FIXME se esta pisando el valor de "cumple"
			cumple = Iterables.contains(unRecurso.getPropiedades(), propiedad);
		}
		return cumple;
 	}
 
 	public void agregarCondiciones(Set<Propiedad> unasCondiciones) {
 		this.condiciones = unasCondiciones;
 	}
 }
