 package br.com.triadworks.issuetracker.controller;
 
 import java.util.List;
 
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
 import br.com.triadworks.issuetracker.dao.IssueDao;
 import br.com.triadworks.issuetracker.model.Issue;
 
 @Resource
 public class DashboardController {
 
 	private final IssueDao dao;
 	private final UsuarioWeb usuarioWeb;
	private final Result result;
 
	public DashboardController(IssueDao dao, UsuarioWeb usuarioWeb, Result result) {
 		this.dao = dao;
 		this.usuarioWeb = usuarioWeb;
		this.result = result;
 	}
 	
 	@Get
 	@Path("/dashboard")
 	public List<Issue> dashboard() {
		result.include("todasAsIssues", dao.listaTudo());
 		Long id = usuarioWeb.getUsuario().getId();
 		return dao.getIssuesDoUsuario(id);
 	}
 	
 	@Get
 	@Path("/dashboard/issues/{id}")
 	public Issue detalhe(Long id) {
 		Issue issue = dao.carrega(id);
 		return issue;
 	}
 	
 }
