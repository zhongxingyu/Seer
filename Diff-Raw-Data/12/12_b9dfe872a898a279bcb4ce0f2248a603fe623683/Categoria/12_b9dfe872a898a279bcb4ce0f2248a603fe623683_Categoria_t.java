 package br.com.ln.financeiro.entity;
 
 import javax.persistence.Entity;
 import javax.persistence.Enumerated;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
import br.com.ln.financeiro.enums.TipoOperacaoEnum;
 import br.com.ln.orm.GenericHibernateDAO;
 
 import lombok.Data;
 import lombok.EqualsAndHashCode;
 
 @Entity
 @EqualsAndHashCode(callSuper=true)
 public @Data class Categoria extends GenericHibernateDAO<Categoria, Long> {
 	
 	@Id
 	@GeneratedValue(strategy=GenerationType.AUTO)
 	private Long id;
 	
 	private String descricao;
 	
 	@OneToOne
 	private Categoria categoriaPai;
 
 	@Enumerated
 	private TipoOperacaoEnum tipoOperacao;
 	
 	@ManyToOne
 	private Entidade entidade;
 	
 	public boolean validate() {
 		return true;
 	}
 
 }
