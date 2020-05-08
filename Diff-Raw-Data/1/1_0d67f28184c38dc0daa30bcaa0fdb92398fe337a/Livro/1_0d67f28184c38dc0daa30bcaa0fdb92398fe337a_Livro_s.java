 
 package br.ugf.alfabeta.modelo.livros;
 
 import br.ugf.alfabeta.modelo.editoras.Editora;
 import br.ugf.alfabeta.modelo.entidades.Entidade;
 import br.ugf.alfabeta.modelo.validacoes.Identidade;
 import br.ugf.alfabeta.modelo.validacoes.Identificacao;
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author Ana
  */
 @Entity
 @Table(name="livro", uniqueConstraints= {
     @UniqueConstraint(columnNames={"id_livro"}), // ID
     @UniqueConstraint(columnNames={"cod_livro", "id_editora"}) // Identidade
 })
 public class Livro implements Serializable, Entidade {
     
     @Id
     @GeneratedValue
     @Column(name="id_livro")
     @NotNull(message="ID não pode ser nulo.", groups=Identificacao.class)
     private Long id;
     
     @Column(name="cod_livro", unique=true, nullable=false, length=30)
     @NotNull(message="Nome não pode ser nulo.", groups=Identidade.class)
     @Size(min=1, max=30, message="Tamanho do código excede os limites.", groups=Identidade.class)
     private String codigo;
     
     @Column(name="nome_livro", length=30)
     @Size(min=1, max=30, message="Tamanho do nome excede os limites.", groups=Identidade.class)
     private String nome;
 
     @ManyToOne
     @JoinColumn(name="id_editora", referencedColumnName="id_editora", nullable=false)
     @NotNull(message="Livro deve pertencer a uma editora.", groups=Identidade.class)
     private Editora editora;
     
     @Column(name="qtdmin_livro") 
     @Min(value=1, message="Quantidade mínima deve estar acima de 0.", groups=Identidade.class)
     private int quantidadeMinima = 10;
     
     @Column(name="qtd_livro")
     @Min(value=0, message="Quantidade deve ser positiva.", groups=Identidade.class)
     private int quantidade = 0;
     
     @Column(name="preco_livro")
     private double preco;
 
     // [ GETTERS / SETTERS ] ===================================================
     
     @Override
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
         this.codigo = codigo;
     }
 
     public String getNome() {
         return nome;
     }
 
     public void setNome(String nome) {
         this.nome = nome;
     }
 
     public Editora getEditora() {
         return editora;
     }
 
     public void setEditora(Editora editora) {
         this.editora = editora;
     }
 
     public int getQuantidadeMinima() {
         return quantidadeMinima;
     }
 
     public void setQuantidadeMinima(int quantidadeMinima) {
         this.quantidadeMinima = quantidadeMinima;
     }
 
     public int getQuantidade() {
         return quantidade;
     }
 
     public void setQuantidade(int quantidade) {
         this.quantidade = quantidade;
     }
     
     public double getPreco() {
         return preco;
     }
     
     public void setPreco(double preco) {
         this.preco = preco;
     }
 
     // [ EQUALS / HASHCODE ] ===================================================
     
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 83 * hash + (this.codigo != null ? this.codigo.hashCode() : 0);
         return hash;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Livro other = (Livro) obj;
         if ((this.codigo == null) ? (other.codigo != null) : !this.codigo.equals(other.codigo)) {
             return false;
         }
         return true;
     }
     
     // [ TOSTRING ] ============================================================
 
     @Override
     public String toString() {
         return "Livro{" 
                 + "id=" + id 
                 + ", codigo=" + codigo 
                 + ", nome=" + nome 
                 + '}';
     }
     
     @Override
     public Livro clone() {
         
         Livro livro = new Livro();
         livro.id = this.id;
         livro.codigo = this.codigo;
         livro.nome = this.nome;
         livro.editora = this.editora;
         livro.quantidadeMinima = this.quantidadeMinima;
         livro.quantidade = this.quantidade;
         return livro;
     }
     
 }
