 package br.com.caelum.notasfiscais.mb;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 
 import br.com.caelum.notasfiscais.dao.ProdutoDao;
 import br.com.caelum.notasfiscais.modelo.Produto;
 
 
 @ManagedBean
 public class ProdutoBean {
 	
 	private Produto produto = new Produto();
 	private List<Produto> produtos;
 	
 	public Produto getProduto(){
 		return this.produto;
 		
 	}
 	
 	public void grava(){
 		ProdutoDao dao = new ProdutoDao();
 		dao.adiciona(produto);
 		this.produto = new Produto();
 		
 	}
 	public List<Produto> getProdutos(){
 		if(produtos == null){
 			System.out.println("Carregando produtos...");
 			produtos = new ProdutoDao().listaTodos();
 		}
 		
 		return produtos;
 		
 	}
 }
