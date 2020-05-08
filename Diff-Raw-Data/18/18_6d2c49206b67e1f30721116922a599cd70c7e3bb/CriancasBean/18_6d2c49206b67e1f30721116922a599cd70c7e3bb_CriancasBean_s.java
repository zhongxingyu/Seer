 package br.app.sisau.beans;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.List;
 import javax.persistence.*;
import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 
 /**
  *
  * @author jr
  */
 @Entity
 @Table(name = "criancas", catalog = "sisau", schema = "public")
 @XmlRootElement
 @NamedQueries({
     @NamedQuery(name = "CriancasBean.findAll", query = "SELECT c FROM CriancasBean c"),
     @NamedQuery(name = "CriancasBean.findByPkCriancas", query = "SELECT c FROM CriancasBean c WHERE c.pkCriancas = :pkCriancas"),
     @NamedQuery(name = "CriancasBean.findByRecemNascido", query = "SELECT c FROM CriancasBean c WHERE c.recemNascido = :recemNascido"),
     @NamedQuery(name = "CriancasBean.findByDataNascimento", query = "SELECT c FROM CriancasBean c WHERE c.dataNascimento = :dataNascimento"),
     @NamedQuery(name = "CriancasBean.findByDuracaoGestao", query = "SELECT c FROM CriancasBean c WHERE c.duracaoGestao = :duracaoGestao"),
     @NamedQuery(name = "CriancasBean.findByTipoParto", query = "SELECT c FROM CriancasBean c WHERE c.tipoParto = :tipoParto"),
     @NamedQuery(name = "CriancasBean.findByTamnhoNascer", query = "SELECT c FROM CriancasBean c WHERE c.tamnhoNascer = :tamnhoNascer"),
     @NamedQuery(name = "CriancasBean.findByUnidadeFrequencia", query = "SELECT c FROM CriancasBean c WHERE c.unidadeFrequencia = :unidadeFrequencia"),
     @NamedQuery(name = "CriancasBean.findByNUbs", query = "SELECT c FROM CriancasBean c WHERE c.nUbs = :nUbs"),
     @NamedQuery(name = "CriancasBean.findByNDeclaracaoNascidoVivo", query = "SELECT c FROM CriancasBean c WHERE c.nDeclaracaoNascidoVivo = :nDeclaracaoNascidoVivo"),
     @NamedQuery(name = "CriancasBean.findByNRegistroCivil", query = "SELECT c FROM CriancasBean c WHERE c.nRegistroCivil = :nRegistroCivil"),
     @NamedQuery(name = "CriancasBean.findByNCartaoSus", query = "SELECT c FROM CriancasBean c WHERE c.nCartaoSus = :nCartaoSus"),
     @NamedQuery(name = "CriancasBean.findByPesoNascer", query = "SELECT c FROM CriancasBean c WHERE c.pesoNascer = :pesoNascer"),
     @NamedQuery(name = "CriancasBean.findByComprimentoNascer", query = "SELECT c FROM CriancasBean c WHERE c.comprimentoNascer = :comprimentoNascer"),
     @NamedQuery(name = "CriancasBean.findByTipagemSanguinea", query = "SELECT c FROM CriancasBean c WHERE c.tipagemSanguinea = :tipagemSanguinea"),
     @NamedQuery(name = "CriancasBean.findByTipagemSanguineaMae", query = "SELECT c FROM CriancasBean c WHERE c.tipagemSanguineaMae = :tipagemSanguineaMae"),
     @NamedQuery(name = "CriancasBean.findByTestePesinho", query = "SELECT c FROM CriancasBean c WHERE c.testePesinho = :testePesinho")})
 public class CriancasBean implements Serializable {
     private static final long serialVersionUID = 1L;
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
     @Basic(optional = false)
    @NotNull
     @Column(name = "pk_criancas", nullable = false)
     private Long pkCriancas;
     @Column(name = "recem_nascido")
     private Boolean recemNascido;
     @Column(name = "data_nascimento")
     @Temporal(TemporalType.TIMESTAMP)
     private Date dataNascimento;
     @Column(name = "duracao_gestao")
     private BigInteger duracaoGestao;
     @Size(max = 50)
     @Column(name = "tipo_parto", length = 50)
     private String tipoParto;
     // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
     @Column(name = "tamnho_nascer", precision = 17, scale = 17)
     private Double tamnhoNascer;
     @Size(max = 50)
     @Column(name = "unidade_frequencia", length = 50)
     private String unidadeFrequencia;
     @Column(name = "n_ubs")
     private BigInteger nUbs;
     @Column(name = "n_declaracao_nascido_vivo")
     private BigInteger nDeclaracaoNascidoVivo;
     @Column(name = "n_registro_civil")
     private BigInteger nRegistroCivil;
     @Column(name = "n_cartao_sus")
     private BigInteger nCartaoSus;
     @Column(name = "peso_nascer", precision = 17, scale = 17)
     private Double pesoNascer;
     @Column(name = "comprimento_nascer", precision = 17, scale = 17)
     private Double comprimentoNascer;
     @Size(max = 50)
     @Column(name = "tipagem_sanguinea", length = 50)
     private String tipagemSanguinea;
     @Size(max = 50)
     @Column(name = "tipagem_sanguinea_mae", length = 50)
     private String tipagemSanguineaMae;
     @Size(max = 50)
     @Column(name = "teste_pesinho", length = 50)
     private String testePesinho;
     @OneToMany(mappedBy = "fkCrianca")
     private List<DesenCriancasBean> desenCriancasList;
     @JoinColumn(name = "fk_familia", referencedColumnName = "pk_familia")
     @ManyToOne
     private FamiliasBean fkFamilia;
     @OneToMany(mappedBy = "fkCriancas")
     private List<PessoaTipoBean> pessoaTipoList;
     @OneToMany(mappedBy = "fkCriancas")
     private List<CriancasDesenCriancasBean> criancasDesenCriancasList;
     @OneToMany(mappedBy = "fkCriancas")
     private List<CriancasDoencasBean> criancasDoencasList;
     @OneToMany(mappedBy = "fkCriancas")
     private List<CriancasVacinasBean> criancasVascinasList;
     @OneToMany(mappedBy = "fkCrianca")
     private List<CriancasVitaminasBean> criancasVitaminasList;
 
     public CriancasBean() {
     }
 
     public CriancasBean(Long pkCriancas) {
         this.pkCriancas = pkCriancas;
     }
 
     public Long getPkCriancas() {
         return pkCriancas;
     }
 
     public void setPkCriancas(Long pkCriancas) {
         this.pkCriancas = pkCriancas;
     }
 
     public Boolean getRecemNascido() {
         return recemNascido;
     }
 
     public void setRecemNascido(Boolean recemNascido) {
         this.recemNascido = recemNascido;
     }
 
     public Date getDataNascimento() {
         return dataNascimento;
     }
 
     public void setDataNascimento(Date dataNascimento) {
         this.dataNascimento = dataNascimento;
     }
 
     public BigInteger getDuracaoGestao() {
         return duracaoGestao;
     }
 
     public void setDuracaoGestao(BigInteger duracaoGestao) {
         this.duracaoGestao = duracaoGestao;
     }
 
     public String getTipoParto() {
         return tipoParto;
     }
 
     public void setTipoParto(String tipoParto) {
         this.tipoParto = tipoParto;
     }
 
     public Double getTamnhoNascer() {
         return tamnhoNascer;
     }
 
     public void setTamnhoNascer(Double tamnhoNascer) {
         this.tamnhoNascer = tamnhoNascer;
     }
 
     public String getUnidadeFrequencia() {
         return unidadeFrequencia;
     }
 
     public void setUnidadeFrequencia(String unidadeFrequencia) {
         this.unidadeFrequencia = unidadeFrequencia;
     }
 
     public BigInteger getNUbs() {
         return nUbs;
     }
 
     public void setNUbs(BigInteger nUbs) {
         this.nUbs = nUbs;
     }
 
     public BigInteger getNDeclaracaoNascidoVivo() {
         return nDeclaracaoNascidoVivo;
     }
 
     public void setNDeclaracaoNascidoVivo(BigInteger nDeclaracaoNascidoVivo) {
         this.nDeclaracaoNascidoVivo = nDeclaracaoNascidoVivo;
     }
 
     public BigInteger getNRegistroCivil() {
         return nRegistroCivil;
     }
 
     public void setNRegistroCivil(BigInteger nRegistroCivil) {
         this.nRegistroCivil = nRegistroCivil;
     }
 
     public BigInteger getNCartaoSus() {
         return nCartaoSus;
     }
 
     public void setNCartaoSus(BigInteger nCartaoSus) {
         this.nCartaoSus = nCartaoSus;
     }
 
     public Double getPesoNascer() {
         return pesoNascer;
     }
 
     public void setPesoNascer(Double pesoNascer) {
         this.pesoNascer = pesoNascer;
     }
 
     public Double getComprimentoNascer() {
         return comprimentoNascer;
     }
 
     public void setComprimentoNascer(Double comprimentoNascer) {
         this.comprimentoNascer = comprimentoNascer;
     }
 
     public String getTipagemSanguinea() {
         return tipagemSanguinea;
     }
 
     public void setTipagemSanguinea(String tipagemSanguinea) {
         this.tipagemSanguinea = tipagemSanguinea;
     }
 
     public String getTipagemSanguineaMae() {
         return tipagemSanguineaMae;
     }
 
     public void setTipagemSanguineaMae(String tipagemSanguineaMae) {
         this.tipagemSanguineaMae = tipagemSanguineaMae;
     }
 
     public String getTestePesinho() {
         return testePesinho;
     }
 
     public void setTestePesinho(String testePesinho) {
         this.testePesinho = testePesinho;
     }
 
     @XmlTransient
     public List<DesenCriancasBean> getDesenCriancasList() {
         return desenCriancasList;
     }
 
     public void setDesenCriancasList(List<DesenCriancasBean> desenCriancasList) {
         this.desenCriancasList = desenCriancasList;
     }
 
     public FamiliasBean getFkFamilia() {
         return fkFamilia;
     }
 
     public void setFkFamilia(FamiliasBean fkFamilia) {
         this.fkFamilia = fkFamilia;
     }
 
     @XmlTransient
     public List<PessoaTipoBean> getPessoaTipoList() {
         return pessoaTipoList;
     }
 
     public void setPessoaTipoList(List<PessoaTipoBean> pessoaTipoList) {
         this.pessoaTipoList = pessoaTipoList;
     }
 
     @XmlTransient
     public List<CriancasDesenCriancasBean> getCriancasDesenCriancasList() {
         return criancasDesenCriancasList;
     }
 
     public void setCriancasDesenCriancasList(List<CriancasDesenCriancasBean> criancasDesenCriancasList) {
         this.criancasDesenCriancasList = criancasDesenCriancasList;
     }
 
     @XmlTransient
     public List<CriancasDoencasBean> getCriancasDoencasList() {
         return criancasDoencasList;
     }
 
     public void setCriancasDoencasList(List<CriancasDoencasBean> criancasDoencasList) {
         this.criancasDoencasList = criancasDoencasList;
     }
 
     @XmlTransient
     public List<CriancasVacinasBean> getCriancasVascinasList() {
         return criancasVascinasList;
     }
 
     public void setCriancasVascinasList(List<CriancasVacinasBean> criancasVascinasList) {
         this.criancasVascinasList = criancasVascinasList;
     }
 
     @XmlTransient
     public List<CriancasVitaminasBean> getCriancasVitaminasList() {
         return criancasVitaminasList;
     }
 
     public void setCriancasVitaminasList(List<CriancasVitaminasBean> criancasVitaminasList) {
         this.criancasVitaminasList = criancasVitaminasList;
     }
 
     @Override
     public int hashCode() {
         int hash = 0;
         hash += (pkCriancas != null ? pkCriancas.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object object) {
         // TODO: Warning - this method won't work in the case the id fields are not set
         if (!(object instanceof CriancasBean)) {
             return false;
         }
         CriancasBean other = (CriancasBean) object;
         if ((this.pkCriancas == null && other.pkCriancas != null) || (this.pkCriancas != null && !this.pkCriancas.equals(other.pkCriancas))) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toString() {
         return "Código.:" + pkCriancas;
     }
     
}
