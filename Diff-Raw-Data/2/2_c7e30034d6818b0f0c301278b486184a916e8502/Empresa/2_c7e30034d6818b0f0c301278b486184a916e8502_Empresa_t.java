 package br.org.aappe.erp.bean;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Embedded;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.SequenceGenerator;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  * @author Phelipe Melanias
  */
 //@Entity
 @Table(name="empresa")
 public class Empresa implements Serializable {
     private static final long serialVersionUID = 1L;
 
     @Id
     @SequenceGenerator(name="id_empresa_seq", sequenceName="id_empresa_seq")
    @GeneratedValue(generator="id_empresa_seq", strategy=GenerationType.AUTO)
     private int id;
 
     @Temporal(TemporalType.DATE)
     @Column(name="dt_cadastro", nullable=false, updatable=false)
     private Date data;
 
     @Column(length=18, nullable=false, unique=true)
     private String cnpj;
 
     @Column(length=200, nullable=false, unique=true)
     private String razaoSocial;
 
     @Column(length=200)
     private String nomeFantasia;
 
     @Column(length=200)
     private String site;
 
     @Column(length=100, nullable=false, unique=true)
     private String email;
 
     @Column(length=14, nullable=false)
     private String telefone;
 
     @Column(length=14)
     private String fax;
 
     @Embedded
     private Endereco endereco;
     
     @OneToMany(cascade=CascadeType.ALL, mappedBy="empresa")
     private Filial filiais;
 
     //getters e setters
     public int getId() { return id; }
     public void setId(int id) { this.id = id; }
 
     public Date getData() { return data; }
     public void setData(Date data) { this.data = data; }
 
     public String getCnpj() { return cnpj; }
     public void setCnpj(String cnpj) { this.cnpj = cnpj.trim(); }
 
     public String getRazaoSocial() { return razaoSocial; }
     public void setRazaoSocial(String razaoSocial) { this.razaoSocial = razaoSocial.trim(); }
 
     public String getNomeFantasia() { return nomeFantasia; }
     public void setNomeFantasia(String nomeFantasia) { this.nomeFantasia = nomeFantasia.trim(); }
 
     public String getSite() { return site; }
     public void setSite(String site) { this.site = site.trim(); }
 
     public String getEmail() { return email; }
     public void setEmail(String email) { this.email = email.trim(); }
 
     public String getTelefone() { return telefone; }
     public void setTelefone(String telefone) { this.telefone = telefone.trim(); }
 
     public String getFax() { return fax; }
     public void setFax(String fax) { this.fax = fax.trim(); }
 
     public Endereco getEndereco() { return endereco; }
     public void setEndereco(Endereco endereco) { this.endereco = endereco; }
 
     public Filial getFiliais() { return filiais; }
     public void setFiliais(Filial filiais) { this.filiais = filiais; }
 }
