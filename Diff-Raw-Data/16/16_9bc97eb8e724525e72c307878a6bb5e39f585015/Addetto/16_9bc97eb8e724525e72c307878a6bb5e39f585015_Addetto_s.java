 package it.ivncr.erp.model.personale;
 
 import it.ivncr.erp.model.generale.Azienda;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.Lob;
 import javax.persistence.ManyToOne;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.builder.ToStringBuilder;
 import org.apache.commons.lang3.builder.ToStringStyle;
 
 @Entity
 @Table(name = "per_addetto")
 public class Addetto {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY, generator = "per_addetto_id_seq")
 	@SequenceGenerator(name = "per_addetto_id_seq", sequenceName = "per_addetto_id_seq")
 	@Column(name="id")
 	private Integer id;
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="azienda_id")
 	private Azienda azienda;
 
 	@Column(name="matricola")
 	private String matricola;
 
 	@Column(name="nome")
 	private String nome;
 
 	@Column(name="cognome")
 	private String cognome;
 
 	@Column(name="data_nascita")
 	private Date dataNascita;
 
 	@Column(name="luogo_nascita")
 	private String luogoNascita;
 
 	@Column(name="codice_fiscale")
 	private String codiceFiscale;
 
 	@Column(name="sesso")
 	private String sesso;
 
 	@Lob
 	@Column(name="foto")
 	private byte[] foto;
 
 	@Column(name="note")
 	private String note;
 
 	@Column(name="fittizio")
 	private Boolean fittizio;
 
 	@Column(name="attivo")
 	private Boolean attivo;
 
 	@Column(name="data_giuramento")
 	private Date dataGiuramento;
 
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name="stato_civile_id")
 	private StatoCivile statoCivile;
 
 	@Column(name="creazione_ts")
 	private Date creazione;
 
 	@Column(name="ultima_modifica_ts")
 	private Date ultimaModifica;
 
 
 	@Override
 	public String toString() {
 
 		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
 			.append("id", id)
 			.append("azienda", azienda, false)
 			.append("matricola", matricola)
 			.append("nome", nome)
 			.append("cognome", cognome)
 			.append("dataNascita", dataNascita)
 			.append("luogoNascita", luogoNascita)
 			.append("codiceFiscale", codiceFiscale)
 			.append("sesso", sesso)
			.append("foto", foto)
 			.append("note", note)
 			.append("fittizio", fittizio)
 			.append("attivo", attivo)
 			.append("dataGiuramento", dataGiuramento)
 			.append("statoCivile", statoCivile, false)
 			.append("creazione", creazione)
 			.append("ultimaModifica", ultimaModifica)
 			.toString();
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Azienda getAzienda() {
 		return azienda;
 	}
 
 	public void setAzienda(Azienda azienda) {
 		this.azienda = azienda;
 	}
 
 	public String getMatricola() {
 		return matricola;
 	}
 
 	public void setMatricola(String matricola) {
 		this.matricola = matricola;
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public String getCognome() {
 		return cognome;
 	}
 
 	public void setCognome(String cognome) {
 		this.cognome = cognome;
 	}
 
 	public Date getDataNascita() {
 		return dataNascita;
 	}
 
 	public void setDataNascita(Date dataNascita) {
 		this.dataNascita = dataNascita;
 	}
 
 	public String getLuogoNascita() {
 		return luogoNascita;
 	}
 
 	public void setLuogoNascita(String luogoNascita) {
 		this.luogoNascita = luogoNascita;
 	}
 
 	public String getCodiceFiscale() {
 		return codiceFiscale;
 	}
 
 	public void setCodiceFiscale(String codiceFiscale) {
 		this.codiceFiscale = codiceFiscale;
 	}
 
 	public String getSesso() {
 		return sesso;
 	}
 
 	public void setSesso(String sesso) {
 		this.sesso = sesso;
 	}
 
 	public byte[] getFoto() {
 		return foto;
 	}
 
 	public void setFoto(byte[] foto) {
 		this.foto = foto;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 
 	public Boolean getFittizio() {
 		return fittizio;
 	}
 
 	public void setFittizio(Boolean fittizio) {
 		this.fittizio = fittizio;
 	}
 
 	public Boolean getAttivo() {
 		return attivo;
 	}
 
 	public void setAttivo(Boolean attivo) {
 		this.attivo = attivo;
 	}
 
 	public Date getDataGiuramento() {
 		return dataGiuramento;
 	}
 
 	public void setDataGiuramento(Date dataGiuramento) {
 		this.dataGiuramento = dataGiuramento;
 	}
 
 	public StatoCivile getStatoCivile() {
 		return statoCivile;
 	}
 
 	public void setStatoCivile(StatoCivile statoCivile) {
 		this.statoCivile = statoCivile;
 	}
 
 	public Date getCreazione() {
 		return creazione;
 	}
 
 	public void setCreazione(Date creazione) {
 		this.creazione = creazione;
 	}
 
 	public Date getUltimaModifica() {
 		return ultimaModifica;
 	}
 
 	public void setUltimaModifica(Date ultimaModifica) {
 		this.ultimaModifica = ultimaModifica;
 	}
 }
