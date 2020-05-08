 package co.com.elramireza.pn.model;
 
 import javax.persistence.*;
 import java.sql.Timestamp;
 
 /**
  * Created by IntelliJ IDEA.
  * User: usuariox
  * Date: 6/02/13
  * Time: 11:01 AM
  */
@javax.persistence.Table(name = "pn_retroalimentacion", schema = "", catalog = "pn")
 @Entity
 public class PnRetroalimentacion {
 	private int idRetro;
 
 	@Column(name = "id_retro")
 	@Id
 	public int getIdRetro() {
 		return idRetro;
 	}
 
 	public void setIdRetro(int idRetro) {
 		this.idRetro = idRetro;
 	}
 
 	private String fortalezas;
 
 	@Column(name = "fortalezas")
 	@Basic
 	public String getFortalezas() {
 		return fortalezas;
 	}
 
 	public void setFortalezas(String fortalezas) {
 		this.fortalezas = fortalezas;
 	}
 
 	private String oportunidades;
 
 	@Column(name = "oportunidades")
 	@Basic
 	public String getOportunidades() {
 		return oportunidades;
 	}
 
 	public void setOportunidades(String oportunidades) {
 		this.oportunidades = oportunidades;
 	}
 
 	private Timestamp fechaCreacion;
 
 	@Column(name = "fecha_creacion")
 	@Basic
 	public Timestamp getFechaCreacion() {
 		return fechaCreacion;
 	}
 
 	public void setFechaCreacion(Timestamp fechaCreacion) {
 		this.fechaCreacion = fechaCreacion;
 	}
 
 	private Participante participanteByIdParticipante;
 
 	@ManyToOne
 	@JoinColumn(name = "id_participante", referencedColumnName = "id", nullable = false)
 	public Participante getParticipanteByIdParticipante() {
 		return participanteByIdParticipante;
 	}
 
 	public void setParticipanteByIdParticipante(Participante participanteByIdParticipante) {
 		this.participanteByIdParticipante = participanteByIdParticipante;
 	}
 
 	private PnCapitulo pnCapituloByIdPnCapitulo;
 
 	@ManyToOne
 	@JoinColumn(name = "id_pn_capitulo")
 	public PnCapitulo getPnCapituloByIdPnCapitulo() {
 		return pnCapituloByIdPnCapitulo;
 	}
 
 	public void setPnCapituloByIdPnCapitulo(PnCapitulo pnCapituloByIdPnCapitulo) {
 		this.pnCapituloByIdPnCapitulo = pnCapituloByIdPnCapitulo;
 	}
 }
