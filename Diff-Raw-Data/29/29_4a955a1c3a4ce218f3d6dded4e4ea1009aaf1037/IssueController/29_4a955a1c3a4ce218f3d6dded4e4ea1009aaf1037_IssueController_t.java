 package br.com.triadworks.issuetracker.controller;
 
import java.util.Date;
 import java.util.List;
 
 import br.com.caelum.vraptor.Delete;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Put;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
 import br.com.triadworks.issuetracker.dao.IssueDao;
 import br.com.triadworks.issuetracker.dao.ProjetoDao;
 import br.com.triadworks.issuetracker.dao.UsuarioDao;
 import br.com.triadworks.issuetracker.model.Issue;
 
 @Resource
 public class IssueController {
 	
 	private final IssueDao dao;
 	private final Result result;
 	private final ProjetoDao projetoDao;
 	private final UsuarioDao usuarioDao;
	private final Validator validator;
	private final UsuarioWeb usuarioWeb;
 
 	public IssueController(IssueDao dao, ProjetoDao projetoDao, 
			UsuarioDao usuarioDao, UsuarioWeb usuarioWeb,
			Result result, Validator validator) {
 		this.dao = dao;
 		this.projetoDao = projetoDao;
 		this.usuarioDao = usuarioDao;
		this.usuarioWeb = usuarioWeb;
 		this.result = result;
		this.validator = validator;
 	}
 	
 	@Get
 	@Path("/issues")
 	public List<Issue> lista() {
 		List<Issue> issues = dao.listaTudo();
 		return issues;
 	}
 	
 	@Get("/issues/novo")
 	public void novo() {
		
		Issue issue = new Issue();
		issue.setReportadoEm(new Date());
		issue.setReportadoPor(usuarioWeb.getUsuario());
		
		result.include("issue", issue);
 		result.include("projetos", projetoDao.listaTudo());
 		result.include("usuarios", usuarioDao.listaTudo());
 	}
 	
 	@Post("/issues")
 	public void adiciona(Issue issue) {
		
		validator.onErrorForwardTo(this).novo();
		
 		dao.salva(issue);
 		result.include("notice", "Issue adicionada com sucesso!");
 		result.redirectTo(this).lista();
 	}
 
 	@Get("/issues/{id}")
 	public Issue edita(Long id) {
 		result.include("usuarios", usuarioDao.listaTudo());
 		Issue issue = dao.carrega(id);
 		return issue;
 	}
 	
 	@Put("/issues/{issue.id}")
 	public void altera(Issue issue) {
		
		validator.onErrorForwardTo(this).edita(issue.getId());
		
 		dao.atualiza(issue);
 		result.include("notice", "Issue atualizada com sucesso!");
 		result.redirectTo(this).lista();
 	}
 	
 	@Delete("/issues/{id}")
 	public void remove(Long id) {
 		Issue issue = dao.carrega(id);
 		dao.remove(issue);
 		result.nothing();
 	}
 	
 }
