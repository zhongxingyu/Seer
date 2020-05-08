 package controller;
 
 import java.util.List;
 
import modelo.entidade.Aluno;
 import modelo.entidade.Curso;
 import br.com.caelum.vraptor.Delete;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Put;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import dao.CursoDao;
 
 @Resource
 public class CursoController {
 	
 	private final CursoDao dao;
 	private final Result result;
 
 	public CursoController(CursoDao dao, Result result){
 		this.dao = dao;
 		this.result = result;
 	}
 	
 	@Get @Path("/curso/adicionar")
 	public void adicionar() {
 	}
 	
 	@Post @Path("/curso/adicionar")
 	public void adicionar(Curso curso) {		
 		dao.salva(curso);
 		result.redirectTo(this).listar();
 	}
 	
 	@Get @Path("/curso/{id}")
 	public Curso editar(Long id){
 		return dao.carrega(id);
 	}
 	
 	@Put @Path("/curso/{curso.id}")
 	public void alterar(Curso curso){
 		dao.atualiza(curso);
 		result.redirectTo(this).listar();
 	}
 	
 	@Get @Path("/curso/listar")
 	public List<Curso> listar(){
 		return dao.listaTudo();
 	}
 	
 	@Delete @Path("/curso/{id}")
 	public void deletar(Long id){
 		dao.deleta(id);
 		result.redirectTo(this).listar();
 	}
 	
 	
 }
