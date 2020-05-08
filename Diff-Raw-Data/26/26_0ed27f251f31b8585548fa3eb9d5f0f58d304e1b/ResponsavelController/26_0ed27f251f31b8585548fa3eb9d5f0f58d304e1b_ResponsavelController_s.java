 package controller;
 
 import java.util.List;
 
 import modelo.entidade.Aluno;
 import modelo.entidade.Responsavel;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import dao.AlunoDao;
 import dao.ResponsavelDao;
 
 @Resource
 public class ResponsavelController {
 	
 	private final ResponsavelDao dao;
 	private final AlunoDao alunoDao;
 	private final Result result;
 	private List<Aluno> alunoList;
 
 	public ResponsavelController(ResponsavelDao dao, AlunoDao alunoDao, Result result){
 		this.dao = dao;
 		this.alunoDao = alunoDao;
 		this.result = result;
 	}
 	
 	@Get @Path("/responsavel/adicionar")
 	public void adicionar() {
 		incluirListaNaResult();
 	}
 	
 	@Post @Path("/responsavel/adicionar")
 	public void adicionar(Responsavel responsavel) {
 		dao.salva(responsavel);
		result.redirectTo(this).listar();
 	}
 	
	@Path("/responsavel/{id}/editar")
 	public Responsavel editar(Long id){
 		incluirListaNaResult();
 		return dao.carrega(id);
 	}
 	
	@Path("/responsavel/{responsavel.id}")
 	public void alterar(Responsavel responsavel){
 		dao.atualiza(responsavel);
 		result.redirectTo(this).listar();
 	}
 		
 	
 	@Get @Path("/responsavel/listar")
 	public List<Responsavel> listar(){
 		return dao.listaTudo();
 	}
 	
 	private void incluirListaNaResult(){
 		alunoList = alunoDao.listaTudo();
 		result.include("alunoList", alunoList);
 	}
 	
 }
