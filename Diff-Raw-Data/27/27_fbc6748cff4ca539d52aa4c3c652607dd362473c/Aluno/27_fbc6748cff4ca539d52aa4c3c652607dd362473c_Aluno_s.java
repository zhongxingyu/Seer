 package modelo.entidade;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 @Entity
 public class Aluno {
 	
 	@Id
 	@GeneratedValue
 	private Long id;
 	private String nome;
 	private Date dataNascimento;
	@ManyToMany(cascade={CascadeType.PERSIST,CascadeType.MERGE})
 	private List<Responsavel> listaResponsavel;
 	
 	public Aluno(){
 		this.listaResponsavel = new ArrayList<Responsavel>();
 	}
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getNome() {
 		return nome;
 	}
 	public void setNome(String nome) {
 		this.nome = nome;
 	}
 	public Date getDataNascimento() {
 		return dataNascimento;
 	}
 	public void setDataNascimento(Date dataNascimento) {
 		this.dataNascimento = dataNascimento;
 	}
 	public List<Responsavel> getListaResponsavel() {
 		return Collections.unmodifiableList(listaResponsavel);
 	}
 	public void adicionaVinculo(Responsavel responsavel) {
 		this.listaResponsavel.add(responsavel);
 	}
 	
 	public void setListaResponsavel(List<Responsavel> listaResponsavel) {
 		this.listaResponsavel = listaResponsavel;
 	}
 	
 }
