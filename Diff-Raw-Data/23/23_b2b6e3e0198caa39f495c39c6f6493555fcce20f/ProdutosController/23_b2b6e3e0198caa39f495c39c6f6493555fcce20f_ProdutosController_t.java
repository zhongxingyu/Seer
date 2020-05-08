 package sisloc.controller;
 
 import java.util.List;
 
 import sisloc.dao.ProdutoDao;
 import sisloc.modelo.Produto;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 
 @Resource
 public class ProdutosController {
 	
 	private ProdutoDao dao;
 	private Result result;
 	
 	public ProdutosController(ProdutoDao dao, Result result){
 		this.dao = dao;
 		this.result = result;
 	}
 	
 	@Path("/produtos/cadastrar")
 	public void cadastrar(){
 	}
 	
 	@Post
 	@Path("/produtos/salvar")
 	public void salvar(Produto produto){
 		try {
 		     if(produto != null) {
		    	 produto.setStatus("A");
 		    	 if(produto.getId() == null) {
 		    		 dao.salvar(produto);
 		    	 } else {
 		    		 dao.atualizar(produto);
 	             }
 	          }
 		     result.include("produto", produto);
 		     result.redirectTo(this.getClass()).cadastrar();
 		} catch (Exception e) {
 			result.include("msg", e.getMessage());
 		}
 	}
 	
 	@Path("/produtos/editar/{produto.id}")
 	public void editar(Produto produto){
 		produto = dao.selectById(produto);
 	    if(produto != null) {
 	         result.include("produto", produto);
 	    }
 	    result.redirectTo(this.getClass()).cadastrar();
 	}
 	
 	@Path("/produtos/visualizar/{produto.id}")
 	public void visualizar(Produto produto){
 		produto = dao.selectById(produto);
 	    if(produto != null) {
 	         result.include("produto", produto);
 	    }
 	    //result.redirectTo(this.getClass()).cadastrar();
 	}
 	
 	@Path("/produtos/excluir/{produto.id}")
 	public void excluir(Produto produto){
 		produto = dao.selectById(produto);
 	    dao.excluir(produto);
 	    //result.redirectTo(this.getClass()).consultar();	
 	}
 	
 	@Path("/produtos/consultar")
 	public List<Produto> consultar(){
 		List<Produto> t = dao.listaTodos();
 		return t;
 	}
 	
 	@Path("/produtos/consultanome")
 	public List<Produto> consultanome(Produto produto){
 		List<Produto> c = dao.consultaNome(produto);
 		return c;
 	}
 
 }
