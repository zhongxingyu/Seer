 package br.com.puc.sispol.controller;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import br.com.puc.sispol.dao.SimuladoDAO;
 
 @Controller
 public class EfetuarInscricaoSimuladoController {
 	private SimuladoDAO daoSimulado;
 
 	public EfetuarInscricaoSimuladoController() {
 		this.daoSimulado = new SimuladoDAO();
 	}
 
 	@RequestMapping("listaSimuladosAguardandoInscricao")
 	public String listaSimuladosAguardandoInscricao(Model model) {
 		model.addAttribute("simulados", daoSimulado.listaAguardandoInscricao());
 		return "efetuar_inscricao_simulado/listagem_inscricao_simulado";
 	}
 
 	@RequestMapping("efetuaInscricaoSimulado")
	public void inscreve(Long codSimulado, Long codUsuario, HttpServletResponse response) {
 		daoSimulado.efetuaInscricao(codSimulado, codUsuario);
		response.setStatus(200);
 	}
 }
