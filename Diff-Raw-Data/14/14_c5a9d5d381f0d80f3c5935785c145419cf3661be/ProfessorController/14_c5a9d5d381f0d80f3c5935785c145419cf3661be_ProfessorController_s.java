 package controller;
 
 import java.util.List;
 
import modelo.entidade.Curso;
import modelo.entidade.HorarioProfessor;
import modelo.entidade.Professor;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import dao.CursoDao;
import dao.ProfessorDao;
 
 @Resource
 public class ProfessorController {
 
 	private final Result result;
 	private final ProfessorDao dao;
 	private final CursoDao cursoDao;
 	
 	public ProfessorController(Result result, ProfessorDao dao, CursoDao cursoDao ) {
 		this.result = result;
 		this.dao = dao;
 		this.cursoDao = cursoDao;
 	}
 	
 	@Get
 	public void adicionar(){
 		incluirListasResult();
 	}
 	
 	@Post
 	public void adicionar(Professor professor){
 		//dao.salva(professor);
 		result.redirectTo(ProfessorController.class).adicionar();
 	}
 	
 	private void incluirListasResult(){
 		List<Curso> listaCurso = cursoDao.listaTudo();
 		result.include("cursoList", listaCurso);
 	}
 }
