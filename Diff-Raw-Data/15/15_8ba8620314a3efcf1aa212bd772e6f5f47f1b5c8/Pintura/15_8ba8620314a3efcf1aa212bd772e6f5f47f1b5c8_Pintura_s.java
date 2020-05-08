 /**
  * Clase Pintura: Representacin de una pintura.
  * 
  * @author Jerson Chinchilla
  * @author Matthew Bertelsen
  * @author Mauricio Fernandez
  * 	@version 18/08/2013		 
  */
 
 
 package logica;
 
 import java.sql.Date;
 import java.util.Set;
 import java.io.Serializable;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name="TPinturas")
 
 public class Pintura implements Serializable{
 	
 private static final long serialVersionUID = -436540065081698326L;
 	
 	@Id
 	@Basic(optional=false)
 	@Column(name="codigo")
 	private String codigo;
 	
 	@Column(name="nombre")
 	private String	nombre;
 	
 	@Column(name="dimensiones")
 	private String dimensiones;
 	
 	//Format = yyyy-mm-dd
 	@Column(name="fecha_creacion")
 	private Date fechaCreacion;
 	
 	@Column(name="tiempo_tardado")
 	private String tiempoTardado;
 	
 	@Column(name="tecnica")
 	private String tecnica;
 	
 	@Column(name="condicion_actual")
 	private int condicionActual;
 	
 	@Column(name="es_famosa")
 	private int esFamosa;
 	
 	@ManyToOne
 	@JoinColumn(name="id_pintor")
 	private Pintor pintor;
 	
 	@OneToOne
 	@JoinColumn(name="id_pintura")
 	private Set<Adquisicion> adquisiciones;
 	
	/*private Exposicion exposicion;*/
	
 	/**
 	 * Constructor
 	 */
 	public Pintura(){
 		
 	}
 	
 	/**
 	 * Constructor
 	 * @param pcodigo: Cdigo de la pintura.
 	 * @param pnombre: Nombre de la pintura.
 	 * @param pdimensiones: Dimensiones de la pintura.
 	 * @param pfechaCreacion: Fecha de creacin de la pintura.
 	 * @param ptiempoTardado: Tiempo de elaboracin de la pintura.
 	 * @param ptecnica: Tcnica en la que la pintura fue elaborada.
 	 * @param pcondicionActual: Condicin actual de la pintura (excelente, buena, regular o mala).
 	 * @param pesFamosa: Indicador si es una pintura muy famosa.
 	 * @param pPintor:Pintor que elabor la pintura.
 	 */	
 	public Pintura(String pcodigo, String pnombre, String pdimensiones, String pfechaCreacion, String ptiempoTardado,
 			String ptecnica, int pcondicionActual, int pesFamosa, Pintor pPintor) {
 		setCodigo(pcodigo);
 		setNombre(pnombre);
 		setDimensiones(pdimensiones);
 		setFechaCreacion(pfechaCreacion);
 		setTiempoTardado(ptiempoTardado);
 		setTecnica(ptecnica);
 		setCondicionActual(pcondicionActual);
 		setEsFamosa(pesFamosa);
 		setPintor(pPintor);
 	}
 	
 	/**
 	 * Getter
 	 * @return codigo
 	 */
 	public String getCodigo() {
 		return codigo;
 	}
 	
 	/**
 	 * Setter
 	 * @param pcodigo: Cdigo de la pintura.
 	 */
 	public void setCodigo(String pcodigo) {
 		codigo = pcodigo;
 	}
 	
 	/**
 	 * Getter
 	 * @return nombre
 	 */
 	public String getNombre() {
 		return nombre;
 	}
 	
 	/**
 	 * Setter
 	 * @param pnombre: Nombre de la pintura.
 	 */
 	public void setNombre(String pnombre) {
 		nombre = pnombre;
 	}
 	
 	/**
 	 * Getter
 	 * @return dimensiones
 	 */
 	public String getDimensiones() {
 		return dimensiones;
 	}
 	
 	/**
 	 * Setter
 	 * @param pdimensiones: Dimensiones de la pintura.
 	 */
 	public void setDimensiones(String pdimensiones) {
 		dimensiones = pdimensiones;
 	}
 	
 	/**
 	 * Getter
 	 * @return fechaCreacion
 	 */
 	public String getFechaCreacion() {
 		return fechaCreacion.toString();
 	}
 	
 	/**
 	 * Setter
 	 * @param pfechaCreacion: Fecha de creacin de la pintura.
 	 */
 	public void setFechaCreacion(String pfechaCreacion) {
 		fechaCreacion = Date.valueOf(pfechaCreacion);
 	}
 	
 	/**
 	 * Setter
 	 * @param pfechaCreacion: Fecha de creacin de la pintura.
 	 */
 	public void setFechaCreacion(Date pfechaCreacion) {
 		fechaCreacion = pfechaCreacion;
 	}
 	
 	/**
 	 * Getter
 	 * @return fechaCreacion
 	 */
 	public String getTiempoTardado() {
 		return tiempoTardado;
 	}
 	
 	/**
 	 * Setter
 	 * @param ptiempoTardado: Tiempo de elaboracin de la pintura.
 	 */
 	public void setTiempoTardado(String ptiempoTardado) {
 		tiempoTardado = ptiempoTardado;
 	}
 	
 	/**
 	 * Getter
 	 * @return tecnica
 	 */
 	public String getTecnica() {
 		return tecnica;
 	}
 	
 	/**
 	 * Setter
 	 * @param ptecnica: Tcnica en la que la pintura fue elaborada.
 	 */
 	public void setTecnica(String ptecnica) {
 		tecnica = ptecnica;
 	}
 	
 	/**
 	 * Getter
 	 * @return condicionActual
 	 */
 	public int getCondicionActual() {
 		return condicionActual;
 	}
 	
 	/**
 	 * Setter
 	 * @param pcondicionActual: Condicin actual de la pintura (excelente, buena, regular o mala).
 	 */
 	public void setCondicionActual(int pcondicionActual) {
 		condicionActual = pcondicionActual;
 	}
 	
 	/**
 	 * Getter
 	 * @return esFamosa
 	 */
 	public int getEsFamosa() {
 		return esFamosa;
 	}
 	
 	/**
 	 * Setter
 	 * @param pesFamosa: Indicador si es una pintura muy famosa.
 	 */
 	public void setEsFamosa(int pesFamosa) {
 		esFamosa = pesFamosa;
 	}
 	
 	/**
 	 * Getter
 	 * @return pintor
 	 */
 	public Pintor getPintor() {
 		return pintor;
 	}
 	
 	/**
 	 * Setter
 	 * pPintor:Pintor que elabor la pintura.
 	 */
 	public void setPintor(Pintor pPintor) {
 		pintor = pPintor;
 	}
 	
 	public Set<Adquisicion> getAdquisiciones() {
 		return adquisiciones;
 	}
 
 	public void setAdquisiciones(Set<Adquisicion> pAdquisiciones){
 		adquisiciones = pAdquisiciones;
 	}
	
	/*
	public Exposicion getExposicion() throws Exception {
		if (exposicion == null) {
			setExposicion((new MultiExposicion()).buscarExposicion(idPintor));
		}
		return exposicion;
	}

	public void setExposicion(Exposicion pexposicion){
		exposicion = pexposicion;
	}*/
 
 }
