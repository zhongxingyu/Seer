 package br.ufrn.cerescaico.bsi.sigest.model;
 
 import java.io.Serializable;
 import javax.persistence.*;
 import java.util.List;
 
 
 /**
  * The persistent class for the estagiario database table.
  * 
  */
 @Entity
 @Table(name="estagiario")
 public class Estagiario implements Serializable, Bean {
 	private static final long serialVersionUID = 1L;
 
 	@Id
	@SequenceGenerator(name="ESTAGIARIO_CODIGO_GENERATOR", sequenceName="ESTAGIARIO_CODIGO_SEQ")
 	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ESTAGIARIO_CODIGO_GENERATOR")
 	@Column(unique=true, nullable=false)
 	private Integer codigo;
 
 	@Column(nullable=false, length=10)
 	private String matricula;
 
 	@Column(nullable=false, length=255)
 	private String nome;
 
 	//bi-directional many-to-one association to Curso
     @ManyToOne
 	@JoinColumn(name="curso", nullable=false)
 	private Curso cursoBean;
 
 	//bi-directional many-to-one association to Estagio
 	@OneToMany(mappedBy="estagiarioBean")
 	private List<Estagio> estagios;
 
     public Estagiario() {
     }
 
 	public Integer getCodigo() {
 		return this.codigo;
 	}
 
 	public void setCodigo(Integer codigo) {
 		this.codigo = codigo;
 	}
 
 	public String getMatricula() {
 		return this.matricula;
 	}
 
 	public void setMatricula(String matricula) {
 		this.matricula = matricula;
 	}
 
 	public String getNome() {
 		return this.nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 
 	public Curso getCursoBean() {
 		return this.cursoBean;
 	}
 
 	public void setCursoBean(Curso cursoBean) {
 		this.cursoBean = cursoBean;
 	}
 	
 	public List<Estagio> getEstagios() {
 		return this.estagios;
 	}
 
 	public void setEstagios(List<Estagio> estagios) {
 		this.estagios = estagios;
 	}
 	
 }
