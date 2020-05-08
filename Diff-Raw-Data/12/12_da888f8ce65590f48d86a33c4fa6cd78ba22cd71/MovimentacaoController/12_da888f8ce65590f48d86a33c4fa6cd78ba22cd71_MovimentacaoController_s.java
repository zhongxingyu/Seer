 package rafaelcds.controller;
 
 import javax.ejb.Stateless;
 import javax.inject.Inject;
 import javax.ws.rs.ApplicationPath;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Application;
 import javax.ws.rs.core.MediaType;
 
 import rafaelcds.model.Movimentacao;
 import rafaelcds.service.MovimentacaoService;
 
@Stateless
 @ApplicationPath("/rest")
 @Path("/movimentacao")
 public class MovimentacaoController extends Application {
 
	public static final String TYPE_JSON = "application/json";

 	@Inject
 	private MovimentacaoService movimentacaoService;
 
 	@POST
 	@Path("/salvar")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	public void salvar(Movimentacao movimentacao) {
 		movimentacaoService.salvar(movimentacao);
 	}
 
 	@GET
 	@Path("/listar")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Movimentacao[] listar() {
 		return movimentacaoService.obterTodos().toArray(new Movimentacao[0]);
 	}
 	
 	@GET
 	@Path("/carregar")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Movimentacao carregar(@QueryParam("id") Long id) {
 		return movimentacaoService.obterPorId(id);
 	}
 	
 	@GET
 	@Path("/excluir")
 	public void excluir(@QueryParam("id") Long id) {
 		movimentacaoService.excluir(id);
 	}
 }
