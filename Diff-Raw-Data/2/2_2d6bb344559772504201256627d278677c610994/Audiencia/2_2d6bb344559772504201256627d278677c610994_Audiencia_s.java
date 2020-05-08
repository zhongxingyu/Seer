 package br.com.cenajur.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Fetch;
 import org.hibernate.annotations.FetchMode;
 import org.hibernate.annotations.LazyCollection;
 import org.hibernate.annotations.LazyCollectionOption;
 
 import br.com.cenajur.util.CenajurUtil;
 import br.com.topsys.database.hibernate.TSActiveRecordAb;
 import br.com.topsys.util.TSUtil;
 
 @Entity
 @Table(name = "audiencias")
 public class Audiencia extends TSActiveRecordAb<Audiencia>{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5140592857414734323L;
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator="audiencias_id")
 	@SequenceGenerator(name="audiencias_id", sequenceName="audiencias_id_seq")
 	private Long id;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name = "processo_numero_id")
 	private ProcessoNumero processoNumero;
 	
 	@Column(name = "data_audiencia")
 	private Date dataAudiencia;
 	
 	@Column(name = "data_cadastro")
 	private Date dataCadastro;
 	
	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name = "situacao_audiencia_id")
 	private SituacaoAudiencia situacaoAudiencia;
 	
 	@ManyToOne(fetch=FetchType.EAGER)
 	private Vara vara;
 	
 	@OneToMany(mappedBy = "audiencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
 	@Fetch(value = FetchMode.SUBSELECT)
 	private List<AudienciaAdvogado> audienciasAdvogados;
 	
 	private String descricao;
 	
 	@Column(name = "data_atualizacao")
 	private Date dataAtualizacao;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	@JoinColumn(name = "colaborador_atualizacao_id")
 	private Colaborador colaboradorAtualizacao;
 	
 	@OneToMany(mappedBy = "audiencia", cascade = CascadeType.ALL, orphanRemoval = true)
 	@LazyCollection(LazyCollectionOption.FALSE)
 	private List<DocumentoAudiencia> documentos;
 	
 	@Column(name = "flag_cliente_ciente")
 	private Boolean flagClienteCiente;
 	
 	@Transient
 	private Colaborador advogado;
 	
 	@ManyToOne(fetch=FetchType.LAZY)
 	private Agenda agenda;
 	
 	@Transient
 	private Date dataInicial;
 	
 	@Transient
 	private Date dataFinal;
 	
 	public Audiencia() {
 	}
 
 	public Audiencia(Long id, String descricao, String descricaoVara, Date dataAudiencia) {
 		this.id = id;
 		this.descricao = descricao;
 		this.vara = new Vara();
 		this.vara.setDescricao(descricaoVara);
 		this.dataAudiencia = dataAudiencia;
 	}
 
 	public Long getId() {
 		return TSUtil.tratarLong(id);
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public ProcessoNumero getProcessoNumero() {
 		return processoNumero;
 	}
 
 	public void setProcessoNumero(ProcessoNumero processoNumero) {
 		this.processoNumero = processoNumero;
 	}
 
 	public Date getDataAudiencia() {
 		return dataAudiencia;
 	}
 
 	public void setDataAudiencia(Date dataAudiencia) {
 		this.dataAudiencia = dataAudiencia;
 	}
 
 	public Date getDataCadastro() {
 		return dataCadastro;
 	}
 
 	public void setDataCadastro(Date dataCadastro) {
 		this.dataCadastro = dataCadastro;
 	}
 
 	public SituacaoAudiencia getSituacaoAudiencia() {
 		return situacaoAudiencia;
 	}
 
 	public void setSituacaoAudiencia(SituacaoAudiencia situacaoAudiencia) {
 		this.situacaoAudiencia = situacaoAudiencia;
 	}
 
 	public Vara getVara() {
 		return vara;
 	}
 
 	public void setVara(Vara vara) {
 		this.vara = vara;
 	}
 
 	public List<AudienciaAdvogado> getAudienciasAdvogados() {
 		return audienciasAdvogados;
 	}
 
 	public void setAudienciasAdvogados(List<AudienciaAdvogado> audienciasAdvogados) {
 		this.audienciasAdvogados = audienciasAdvogados;
 	}
 
 	public String getDescricao() {
 		return descricao;
 	}
 	
 	public String getResumoDescricao() {
 		return CenajurUtil.obterResumoGrid(descricao, 35);
 	}
 
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 	public Date getDataAtualizacao() {
 		return dataAtualizacao;
 	}
 
 	public void setDataAtualizacao(Date dataAtualizacao) {
 		this.dataAtualizacao = dataAtualizacao;
 	}
 
 	public Colaborador getColaboradorAtualizacao() {
 		return colaboradorAtualizacao;
 	}
 
 	public void setColaboradorAtualizacao(Colaborador colaboradorAtualizacao) {
 		this.colaboradorAtualizacao = colaboradorAtualizacao;
 	}
 
 	public List<DocumentoAudiencia> getDocumentos() {
 		return documentos;
 	}
 
 	public void setDocumentos(List<DocumentoAudiencia> documentos) {
 		this.documentos = documentos;
 	}
 
 	public Boolean getFlagClienteCiente() {
 		return flagClienteCiente;
 	}
 
 	public void setFlagClienteCiente(Boolean flagClienteCiente) {
 		this.flagClienteCiente = flagClienteCiente;
 	}
 
 	public Colaborador getAdvogado() {
 		return advogado;
 	}
 
 	public void setAdvogado(Colaborador advogado) {
 		this.advogado = advogado;
 	}
 
 	public Agenda getAgenda() {
 		return agenda;
 	}
 
 	public void setAgenda(Agenda agenda) {
 		this.agenda = agenda;
 	}
 
 	public Date getDataInicial() {
 		return dataInicial;
 	}
 
 	public void setDataInicial(Date dataInicial) {
 		this.dataInicial = dataInicial;
 	}
 
 	public Date getDataFinal() {
 		return dataFinal;
 	}
 
 	public void setDataFinal(Date dataFinal) {
 		this.dataFinal = dataFinal;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Audiencia other = (Audiencia) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 	
 	public List<Audiencia> findByProcesso(Processo processo){
 		return super.find("from Audiencia a where a.processoNumero.processo.id = ? ", "a.dataAudiencia desc",processo.getId());
 	}
 	
 	@Override
 	public List<Audiencia> findByModel(String... fieldsOrderBy) {
 		
 		StringBuilder query = new StringBuilder();
 		
 		query.append(" select distinct a from Audiencia a inner join a.audienciasAdvogados aa where 1 = 1 ");
 		
 		if(!TSUtil.isEmpty(processoNumero) && !TSUtil.isEmpty(processoNumero.getNumero())){
 			query.append(CenajurUtil.getParamSemAcento("a.processoNumero.numero"));
 		}
 		
 		if(!TSUtil.isEmpty(dataInicial) && !TSUtil.isEmpty(dataFinal)){
 			query.append("and date(a.dataAudiencia) between date(?) and date(?) ");
 		}
 		
 		if(!TSUtil.isEmpty(situacaoAudiencia) && !TSUtil.isEmpty(situacaoAudiencia.getId())){
 			query.append("and a.situacaoAudiencia.id = ? ");
 		}
 		
 		if(!TSUtil.isEmpty(advogado) && !TSUtil.isEmpty(advogado.getId())){
 			query.append("and aa.advogado.id = ? ");
 		}
 		
 		if(!TSUtil.isEmpty(vara) && !TSUtil.isEmpty(vara.getId())){
 			query.append("and a.vara.id = ? ");
 		}
 		
 		if(!TSUtil.isEmpty(descricao)){
 			query.append("and ").append(CenajurUtil.semAcento("a.descricao")).append(" like ").append(CenajurUtil.semAcento("?")).append(" ");
 		}
 		
 		List<Object> params = new ArrayList<Object>();
 		
 		if(!TSUtil.isEmpty(processoNumero) && !TSUtil.isEmpty(processoNumero.getNumero())){
 			params.add(CenajurUtil.tratarString(processoNumero.getNumero()));
 		}
 		
 		if(!TSUtil.isEmpty(dataInicial) && !TSUtil.isEmpty(dataFinal)){
 			params.add(dataInicial);
 			params.add(dataFinal);
 		}
 		
 		if(!TSUtil.isEmpty(situacaoAudiencia) && !TSUtil.isEmpty(situacaoAudiencia.getId())){
 			params.add(situacaoAudiencia.getId());
 		}
 		
 		if(!TSUtil.isEmpty(advogado) && !TSUtil.isEmpty(advogado.getId())){
 			params.add(advogado.getId());
 		}
 		
 		if(!TSUtil.isEmpty(vara) && !TSUtil.isEmpty(vara.getId())){
 			params.add(vara.getId());
 		}
 		
 		if(!TSUtil.isEmpty(descricao)){
 			params.add(CenajurUtil.tratarString(descricao));
 		}
 		
 		return super.find(query.toString(), "a.dataAudiencia", params.toArray());
 	}
 	
 	public Audiencia obterPorAgenda(Agenda agenda){
 		return super.get(" select a from Audiencia a left outer join fetch a.documentos d where a.agenda.id = ? ", agenda.getId());
 	}
 	
 	public List<Audiencia> pesquisarAudienciasProximas(int qtdDias){
 		return super.find("select a from Audiencia a where a.dataAudiencia between ? and ? ", null, new Date(), CenajurUtil.getDataMaisDias(qtdDias));
 	}
 }
