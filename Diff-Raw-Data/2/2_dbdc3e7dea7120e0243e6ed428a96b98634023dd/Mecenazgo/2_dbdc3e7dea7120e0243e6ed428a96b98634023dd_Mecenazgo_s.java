 /**
  * Clase Mecenazgo: Representacin de un periodo de mecenazgo entre un pintor y un mecenas.
  * 
  * @author Jerson Chinchilla
  * @author Matthew Bertelsen
  * @author Mauricio Fernandez
  * 	@version 18/08/2013		 
  */
 
 package logica;
 
 import java.io.Serializable;
 import java.sql.Date;
 
 import javax.persistence.*;
 
 @Entity
@Table(name="TMecenazgo")
 
 public class Mecenazgo implements Serializable {
 	
 private static final long serialVersionUID = -436540065081698326L;
 	
 	//Format = yyyy-mm-dd
 	@Column(name="fecha_inicio")
 	private Date fechaInicio;
 	
 	//Format = yyyy-mm-dd
 	@Column(name="fecha_fin")
 	private Date fechaFin;
 	
 	@Id
 	@ManyToOne
 	@JoinColumn(name="id_pintor")
 	private Pintor pintor;
 	
 	@Id
 	@ManyToOne
 	@JoinColumn(name="id_mecenas")
 	private Mecenas	mecenas;
 	
 	/**
 	 * Constructor
 	 */
 	private Mecenazgo(){
 		
 	}
 	
 	/**
 	 * Constructor
 	 * @param pfechaInicio: Fecha de inicio del mecenazgo.
 	 * @param pfechaFin: Fecha de finalizacin del mecenazgo.
 	 * @param pidPintor: Identificador del pintor al que se le di el mecenazgo.
 	 * @param pidMecenas: Identificador del mecenas que brind el mecenazgo.
 	 */	
 	public Mecenazgo(String pfechaInicio, String pfechaFin, Pintor pPintor, Mecenas pMecenas){
 		setFechaInicio(pfechaInicio);
 		setFechaFin(pfechaFin);
 		setPintor(pPintor);
 		setMecenas(pMecenas);
 	}
 	
 	/**
 	 * Getter
 	 * @return fechaInicio
 	 */
 	public String getFechaInicio() {
 		return fechaInicio.toString();
 	}
 	
 	/**
 	 * Setter
 	 *@param pfechaInicio: Fecha de inicio del mecenazgo.
 	 */
 	public void setFechaInicio(String pfechaInicio) {
 		fechaInicio = Date.valueOf(pfechaInicio);
 	}
 	
 	/**
 	 * Setter
 	 *@param pfechaInicio: Fecha de inicio del mecenazgo.
 	 */
 	public void setFechaInicio(Date pfechaInicio) {
 		fechaInicio = pfechaInicio;
 	}
 	
 	/**
 	 * Getter
 	 * @return fechaFin
 	 */
 	public String getFechaFin() {
 		return fechaFin.toString();
 	}
 	
 	/**
 	 * Setter
 	 * @param pfechaFin: Fecha de finalizacin del mecenazgo.
 	 */
 	public void setFechaFin(String pfechaFin) {
 		fechaFin = Date.valueOf(pfechaFin);
 	}
 	
 	/**
 	 * Setter
 	 * @param pfechaFin: Fecha de finalizacin del mecenazgo.
 	 */
 	public void setFechaFin(Date pfechaFin) {
 		fechaFin = pfechaFin;
 	}
 	
 	public Pintor getPintor() {
 		return pintor;
 	}
 
 	public void setPintor(Pintor ppintor){
 		pintor = ppintor;
 	}
 	
 	public Mecenas getMecenas() {
 		return mecenas;
 	}
 
 	public void setMecenas(Mecenas pmecenas){
 		mecenas = pmecenas;
 	}
 
 }
