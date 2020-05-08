 package com.worktogether.webService;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 import org.jboss.resteasy.spi.HttpResponse;
 
 import com.worktogether.buisiness.DominioStatusRequsicao;
 import com.worktogether.buisiness.GerenciaEvento;
 import com.worktogether.buisiness.GerenciaRanking;
 import com.worktogether.buisiness.GerenciaUsuario;
 import com.worktogether.dto.EventoDTO;
 import com.worktogether.dto.GeolocalizacaoDTO;
 import com.worktogether.dto.HabilidadeDTO;
 import com.worktogether.dto.UsuarioDTO;
 import com.worktogether.entities.DominioTipoPontuacao;
 import com.worktogether.entities.Evento;
 import com.worktogether.entities.Habilidade;
 import com.worktogether.entities.Presenca;
 import com.worktogether.entities.Publicacao;
 import com.worktogether.entities.Usuario;
 
 @Path("/worktogetherresource")
 public class WSWorkTogetherResource {
 	
 	@Context 
 	HttpResponse response;
 	
 	@Inject
 	GerenciaUsuario gru;
 	
 	@Inject
 	GerenciaEvento gre;
 	
 	@Inject
 	GerenciaRanking grk;
 	
 	@POST
 	@Path("/validarEmail")
 	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public WSReturn validarEmail(String email){
 		
 		WSReturn a = gru.isEmailExistente(email);
 		
 		return a;
 			
 	} 
 	
 	@POST
 	@Path("/cadastrarUsuario")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public Long cadastrarUsuario(Usuario usuario){
 		return gru.cadastrarUsuario(usuario);
 	}
 	
 	@POST
 	@Path("/cadastrarEvento")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public Long cadastrarEvento(Evento evento){
 		return gre.cadastrarEvento(evento);
 	} 
 	
 	@POST
 	@Path("/autenticarUsuario")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public String autenticarUsuario(Usuario usuario){
 		return gru.autenticarUsuario(usuario);
 	} 
 	
 	@POST
 	@Path("/buscarEventosSugeridos")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<EventoDTO> buscarEventosSugeridos(Usuario usuario) {
 		return gre.buscarEventos(usuario);
 	}
 	
 	@POST
 	@Path("/buscarEventos")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<EventoDTO> buscarEventos(Usuario usuario){
 		return gre.buscarEventos(usuario);
 	}
 	
 	//TODO MODELAR
 	@POST
 	@Path("/enviarConviteAutomatico")
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public void enviarConviteAutomatico(@QueryParam("usuarioInfo") String usuarioInfo, @QueryParam("localizacao") String localizacao){
 		gre.enviarConviteAutomatico(usuarioInfo, localizacao);
 	} 
 	
 	//TODO MODELAR
 	@POST
 	@Path("/buscarRankingUsuario")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<UsuarioDTO> buscarRankingUsuario(@QueryParam("pontuacao") BigDecimal pontuacao){
 		return grk.buscarRankingUsuario(pontuacao);
 	}
 	
 	//TODO MODELAR
 	@POST
 	@Path("/buscarRankingEvento")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<EventoDTO> buscarRankingEvento(@QueryParam("pontuacao") BigDecimal pontuacao){
 		return grk.buscarRankingEvento(pontuacao);
 	}
 	
 	//TODO MODELAR
 	@POST
 	@Path("/confirmarPresenca")
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public String confirmarPresenca(@QueryParam("idEvento") Long idEvento, @QueryParam("idUsuario") Long idUsuario, @QueryParam("latitude") BigDecimal latitude, @QueryParam("longitude") BigDecimal longitude){
 		String msg = gre.confirmarPresenca(idEvento, idUsuario, latitude, longitude);
 		
 		if(DominioStatusRequsicao.SUCESS.toString().equalsIgnoreCase(msg)){
 			grk.atualizarPontuacaoEvento(idEvento, DominioTipoPontuacao.PRESENCA);
 			grk.atualizarPontuacaoUsuario(idUsuario, DominioTipoPontuacao.PRESENCA);
 		}
 		return msg;
 	}
 	
 	
 	@POST
 	@Path("/indicarPresenca")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public String indicarPresenca(@QueryParam("idEvento") Long idEvento, @QueryParam("idUsuario") Long idUsuario){
 		String msg = gre.indicarPresenca(idEvento, idUsuario);
 		
 		if(DominioStatusRequsicao.SUCESS.toString().equalsIgnoreCase(msg)){
 			grk.atualizarPontuacaoEvento(idEvento, DominioTipoPontuacao.INDICAR_PRESENCA);
 			grk.atualizarPontuacaoUsuario(idUsuario, DominioTipoPontuacao.INDICAR_PRESENCA);
 		}
 		return msg;
 	}
 	
 	@POST
 	@Path("/removerPresenca")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public String removerPresenca(@QueryParam("idEvento") Long idEvento, @QueryParam("idUsuario") Long idUsuario){
 		return gre.removerPresenca(idEvento, idUsuario);
 	}
 	
	
	
	
 	
 	public String publicar(Publicacao publicacao){return null;} 
 	
 	public List<Habilidade> buscarHabilidades(Long inicio, Long fiim){return null;} 
 	public String atualizarListaEventosUsuairo(Usuario usuario, List<Presenca> eventos){return null;} 
 	 
 	
 	@POST
 	@Path("/verificarAtualizacaoEventos")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<EventoDTO> verificarAtualizacaoEventos(Usuario usuario){
 		return gre.buscarEventos(usuario);
 	}
 	
 	@POST
 	@Path("/buscarGeolocalizacaoEvento")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<GeolocalizacaoDTO> buscarGeolocalizacaoEvento(@QueryParam("idEvento") Long idEvento){
 		return gre.buscarGeolocalizacaoEvento(idEvento);
 	}
 	
 	@POST
 	@Path("/buscarHabilidadeEvento")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<HabilidadeDTO> buscarHabilidadeEvento(@QueryParam("idEvento") Long idEvento){
 		return gre.buscarHabilidadeEvento(idEvento);
 	}
 	
 	//TODO MODELAR
 	@POST
 	@Path("/buscarEventosUsuario")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public List<EventoDTO> buscarEventosUsuario(@QueryParam("idUsuario") Long idUsuario){
 		return gre.buscarEventosUsuario(idUsuario);
 	}
 	
 	//TODO MODELAR
 	@POST
 	@Path("/buscarEvento")
 	@Produces({MediaType.APPLICATION_JSON})
 	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
 	public EventoDTO buscarEvento(@QueryParam("idEvento") Long idEvento, @QueryParam("idEvento") Long idUsuario){
 		return gre.buscarEvento(idEvento, idUsuario);
 	}
 
 }
