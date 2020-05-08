 package br.com.unifacs.model;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import java.util.Set;
 
 
 /**
  * The persistent class for the usuario_projeto database table.
  * 
  */
 @Entity
 @Table(name="usuario_projeto")
 public class UsuarioProjeto implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private int id;
 
 	private String block;
 
 	//bi-directional many-to-one association to Lancamento
 	@OneToMany(mappedBy="usuarioProjeto")
 	private Set<Lancamento> lancamentos;
 
 	//bi-directional many-to-one association to Orcamento
 	@OneToMany(mappedBy="usuarioProjeto")
 	private Set<Orcamento> orcamentos;
 
 	//bi-directional many-to-one association to Usuario
	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="id_Usuario")
 	private Usuario usuario;
 
 	//bi-directional many-to-one association to Projeto
	@ManyToOne(fetch=FetchType.EAGER)
 	@JoinColumn(name="id_Projeto")
 	private Projeto projeto;
 
     public UsuarioProjeto() {
     }
 
 	public int getId() {
 		return this.id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public String getBlock() {
 		return this.block;
 	}
 
 	public void setBlock(String block) {
 		this.block = block;
 	}
 
 	public Set<Lancamento> getLancamentos() {
 		return this.lancamentos;
 	}
 
 	public void setLancamentos(Set<Lancamento> lancamentos) {
 		this.lancamentos = lancamentos;
 	}
 	
 	public Set<Orcamento> getOrcamentos() {
 		return this.orcamentos;
 	}
 
 	public void setOrcamentos(Set<Orcamento> orcamentos) {
 		this.orcamentos = orcamentos;
 	}
 	
 	public Usuario getUsuario() {
 		return this.usuario;
 	}
 
 	public void setUsuario(Usuario usuario) {
 		this.usuario = usuario;
 	}
 	
 	public Projeto getProjeto() {
 		return this.projeto;
 	}
 
 	public void setProjeto(Projeto projeto) {
 		this.projeto = projeto;
 	}
 	
 }
