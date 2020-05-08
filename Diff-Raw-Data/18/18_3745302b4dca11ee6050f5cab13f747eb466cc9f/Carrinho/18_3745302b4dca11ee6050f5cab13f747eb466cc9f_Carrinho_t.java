 package produtos;
 
import java.util.Date;

 public class Carrinho {
 
 	private int idProduto;
 	private String titulo;
 	private String descricao;
 	private double preco;
 	private int qtdNoCarrinho;
 	private int idProdutoCarrinho;
 	private int qtdProdutos;
 	private int idCarrinho;
	private Date dataCadastroCarrinho;
 
 
 
 	public Carrinho() {
 		// TODO Auto-generated constructor stub
 	}
 
 
 
 	public int getIdProduto() {
 		return idProduto;
 	}
 
 
 
 	public void setIdProduto(int idProduto) {
 		this.idProduto = idProduto;
 	}
 
 
 
 	public String getTitulo() {
 		return titulo;
 	}
 
 
 
 	public void setTitulo(String titulo) {
 		this.titulo = titulo;
 	}
 
 
 
 	public String getDescricao() {
 		return descricao;
 	}
 
 
 
 	public void setDescricao(String descricao) {
 		this.descricao = descricao;
 	}
 
 
 
 	public double getPreco() {
 		return preco;
 	}
 
 
 
 	public void setPreco(double preco) {
 		this.preco = preco;
 	}
 
 
 
 	public int getQtdNoCarrinho() {
 		return qtdNoCarrinho;
 	}
 
 
 
 	public void setQtdNoCarrinho(int qtdNoCarrinho) {
 		this.qtdNoCarrinho = qtdNoCarrinho;
 	}
 
 
 
 	public int getIdProdutoCarrinho() {
 		return idProdutoCarrinho;
 	}
 
 
 
 	public void setIdProdutoCarrinho(int idProdutoCarrinho) {
 		this.idProdutoCarrinho = idProdutoCarrinho;
 	}
 
 
 
 	public int getQtdProdutos() {
 		return qtdProdutos;
 	}
 
 
 
 	public void setQtdProdutos(int qtdProdutos) {
 		this.qtdProdutos = qtdProdutos;
 	}
 
 
 
 	public int getIdCarrinho() {
 		return idCarrinho;
 	}
 
 
 
 	public void setIdCarrinho(int idCarrinho) {
 		this.idCarrinho = idCarrinho;
 	}
 
 
 
	public Date getDataCadastroCarrinho() {
 		return dataCadastroCarrinho;
 	}
 
 
 
	public void setDataCadastroCarrinho(Date dataCadastroCarrinho) {
 		this.dataCadastroCarrinho = dataCadastroCarrinho;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Item do carrinho [id=%s, titulo=%s]", idCarrinho, titulo);
 	}
 
 }
