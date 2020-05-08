 package br.com.athat.web.controller.pop;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import br.com.athat.core.entity.produto.Produto;
 import br.com.athat.core.manager.produto.ProdutoManager;
 import br.com.athat.utils.validators.ValidatorUtils;
 import br.com.athat.web.utils.AbstractController;
 
 public class PopProdutoController extends AbstractController {
 
 	private static final long serialVersionUID = 1L;
 
 	private Long id;
 	private String nome;
 	private Produto produto;
 	private List<Produto> produtos;
 	private boolean validaEstoque;
 
 	@Autowired
 	private ProdutoManager produtoManager;
 
 	public PopProdutoController() {
 		limpar();
 		produtos = new ArrayList<Produto>();
 	}
 
 	public void buscarProdutos() {
 		Produto p = new Produto();
 		if (id != null) {
 			p.setId(id);
 		} else if (ValidatorUtils.isNotEmptyAndNotNull(nome)) {
 			p.setNome(nome);
 		}
 		
 		if(validaEstoque) {
 			produtos = produtoManager.buscarComEstoque(p);
 		} else {
 			produtos = produtoManager.buscar(p);
 		}
 	}
 
 	public void limpar() {
 		id = null;
 		nome = "";
 		produto = new Produto();
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
 
 	public Produto getProduto() {
 		return produto;
 	}
 
 	public void setProduto(Produto produto) {
 		this.produto = produto;
 	}
 
 	public List<Produto> getProdutos() {
 		return produtos;
 	}
 
 	public void setProdutos(List<Produto> produtos) {
 		this.produtos = produtos;
 	}
 
 	public ProdutoManager getProdutoManager() {
 		return produtoManager;
 	}
 
 	public void setProdutoManager(ProdutoManager produtoManager) {
 		this.produtoManager = produtoManager;
 	}
 	
 	public boolean isValidaEstoque() {
 		return validaEstoque;
 	}
 	
 	public void setValidaEstoque(boolean validaEstoque) {
 		this.validaEstoque = validaEstoque;
 	}
 }
