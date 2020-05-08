 package mereditor.modelo;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import mereditor.modelo.Validacion.EstadoValidacion;
 import mereditor.modelo.base.Componente;
 import mereditor.modelo.base.ComponenteAtributos;
 import mereditor.modelo.base.ComponenteNombre;
 import mereditor.modelo.validacion.Observacion;
 import mereditor.modelo.validacion.ValidarAcoplamiento;
 import mereditor.modelo.validacion.ValidarClaridadAtributos;
 import mereditor.modelo.validacion.ValidarClaridadComponentes;
 import mereditor.modelo.validacion.ValidarSuperposicion;
 
 public class DiagramaDER extends Diagrama {
 
 	
 	
 	protected Validacion validacion = new Validacion();
 
	
	
	
 	public DiagramaDER(Proyecto proyecto) {
		super(proyecto);
 		this.proyecto = proyecto;
 	}
 
 	/**
 	 * Devuelve los diagramas hijos de este diagrama.
 	 * @return
 	 */
 	public Set<DiagramaDER> getDiagramasDER() {
 		Set<DiagramaDER> ders=new HashSet<DiagramaDER>();
 		for(Diagrama diagrama:diagramas){
 			if(DiagramaDER.class.isInstance(diagrama))
 				ders.add((DiagramaDER)diagrama);
 		}
 		return Collections.unmodifiableSet(ders);
 	}
 	public void setDiagramasDER(Set<DiagramaDER> ders){
 		diagramas.clear();
 		for(Diagrama diagrama:ders)
 			diagramas.add(diagrama);
 	}
 
 	
 
 	/**
 	 * Obtiene las entidades de este diagrama y sus ancestros.
 	 * 
 	 * @param incluirAncestros
 	 *            Indica si se deben incluir las entidades de los ancestros.
 	 * @return
 	 */
 	public Set<Entidad> getEntidades(boolean incluirAncestros) {
 		Set<Entidad> entidades = Componente.filtrarComponentes(Entidad.class, this.componentes);
 
 		if (incluirAncestros && this.getPadre() != null) {
 			DiagramaDER diagrama = (DiagramaDER) this.getPadre();
 			entidades.addAll(diagrama.getEntidades(incluirAncestros));
 		}
 
 		return entidades;
 	}
 
 	/**
 	 * Obtiene una colección con todos los atributos de los componentes de este
 	 * diagrama.
 	 * 
 	 * @param incluirAncestros
 	 * @return
 	 */
 	public Collection<Atributo> getAtributos(boolean incluirAncestros) {
 		Set<ComponenteAtributos> componentes = Componente.filtrarComponentes(
 				ComponenteAtributos.class, this.componentes);
 
 		Set<Atributo> atributos = new HashSet<>();
 
 		for (ComponenteAtributos componente : componentes)
 			atributos.addAll(componente.getAtributos());
 
 		if (incluirAncestros && this.getPadre() != null) {
 			DiagramaDER diagrama = (DiagramaDER) this.getPadre();
 			atributos.addAll(diagrama.getAtributos(incluirAncestros));
 		}
 
 		return atributos;
 	}
 
 	/**
 	 * Obtiene una colección con todas las Relaciones de este diagrama.
 	 * 
 	 * @param incluirAncestros
 	 * @return
 	 */
 	public Set<Relacion> getRelaciones(boolean incluirAncestros) {
 		Set<Relacion> relaciones = Componente.filtrarComponentes(Relacion.class, this.componentes);
 
 		if (incluirAncestros && this.getPadre() != null) {
 			DiagramaDER diagrama = (DiagramaDER) this.getPadre();
 			relaciones.addAll(diagrama.getRelaciones(incluirAncestros));
 		}
 
 		return relaciones;
 	}
 
 	/**
 	 * Obtiene una colección con todas las Jerarquias de este diagrama.
 	 * 
 	 * @param incluirAncestros
 	 * @return
 	 */
 	public Set<Jerarquia> getJerarquias(boolean incluirAncestros) {
 		Set<Jerarquia> jerarquias = Componente
 				.filtrarComponentes(Jerarquia.class, this.componentes);
 
 		if (incluirAncestros && this.getPadre() != null) {
 			DiagramaDER diagrama = (DiagramaDER) this.getPadre();
 			jerarquias.addAll(diagrama.getJerarquias(incluirAncestros));
 		}
 
 		return jerarquias;
 	}
 
 	/**
 	 * Devuelve la información de validacion del diagrama.
 	 * 
 	 * @return
 	 */
 	public Validacion getValidacion() {
 		return this.validacion;
 	}
 
 	@Override
 	public void addValidaciones() {
 		this.validaciones.add(new ValidarAcoplamiento());
 		this.validaciones.add(new ValidarClaridadComponentes());
 		this.validaciones.add(new ValidarClaridadAtributos());
 		this.validaciones.add(new ValidarSuperposicion());
 	}
 
 	@Override
 	public Observacion validar() {
 		Observacion observacion = super.validar();
 
 		for (Componente componente : this.componentes)
 			observacion.addObservacion(componente.validar());
 
 		if (!observacion.isEmpty()) {
 			this.validacion.setObservaciones(observacion.toString());
 			this.validacion.setEstado(EstadoValidacion.VALIDADO_CON_OBSERVACIONES);
 		} else
 			this.validacion.setEstado(EstadoValidacion.VALIDADO);
 
 		return observacion;
 	}
 
 	
 }
