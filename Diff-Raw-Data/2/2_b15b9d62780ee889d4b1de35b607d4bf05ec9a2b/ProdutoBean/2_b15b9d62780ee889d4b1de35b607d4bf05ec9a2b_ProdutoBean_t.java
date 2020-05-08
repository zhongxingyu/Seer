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
 		if(produto.getId() == null){
 			dao.adiciona(produto);
 		} else{
 			dao.atualiza(produto);
 		}
 		this.produto = new Produto();
 		this.produtos = dao.listaTodos();
 		
 	}
	public void remove(Produto produto){
 		ProdutoDao dao = new ProdutoDao();
 		dao.remove(produto);
 		this.produtos = dao.listaTodos();
 	}
 	public List<Produto> getProdutos(){
 		if(produtos == null){
 			System.out.println("Carregando produtos...");
 			produtos = new ProdutoDao().listaTodos();
 		}
 		
 		return produtos;
 		
 	}
 	public double getTotal(){
 		double total = 0.0;
 		for (Produto p : produtos) {
 			total += p.getPreco();
 		}
 		return total;
 	}
 	
 	public void setProduto(Produto produto){
 		this.produto = produto;
 	}
 
 }
