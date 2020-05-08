 package sisloc.modelo;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.OneToMany;
 
 @Entity
 public class Produto implements Serializable{
 	
 	private static final long serialVersionUID = -2257876564571767612L;
 	
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	private Long id;
 	private String codigo;
 	private String nome;
 	private String descricao;
 	private Integer quantidade;
 	private Double valor;
 	private String obs;
 	
 	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
 	@JoinColumn
 	private List<Preco> precos;
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getCodigo() {
 		return codigo;
 	}
 
 	public void setCodigo(String codigo) {
 		this.codigo = codigo.toUpperCase();
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome.toUpperCase();
 	}
 
 	public String getDescricao() {
 		return descricao;
 	}
 
 	public void setDescricao(String descricao) {
 		this.descricao = descricao.toUpperCase();
 	}
 
 	public Integer getQuantidade() {
 		return quantidade;
 	}
 
 	public void setQuantidade(Integer quantidade) {
 		this.quantidade = quantidade;
 	}
 
 	public List<Preco> getPrecos() {
 		return precos;
 	}
 
 	public void setPrecos(List<Preco> precos) {
 		this.precos = precos;
 	}
 
 	public Double getValor() {
 		return valor;
 	}
 
 	public void setValor(Double valor) {
 		this.valor = valor;
 	}
 	
 	public Double getPreco(Long dias){
 		Collections.sort(precos);
 		Double preco = 0.0;
 		for(Preco p : precos){
 			preco = p.getPreco();
 			if(p.getDias() <= dias){
 				return preco;
 			}
 		}
 		
 		return preco;
 		
 	}
 
 	public String getObs() {
 		return obs;
 	}
 
 	public void setObs(String obs) {
 		this.obs = obs;
 	}
 
 }
