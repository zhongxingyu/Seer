 package br.com.puc.sispol.controller;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import br.com.puc.sispol.dao.AreaDeConhecimentoDAO;
 import br.com.puc.sispol.modelo.AreaDeConhecimento;
import br.com.puc.sispol.modelo.Tarefa;
 
 @Controller
 public class ManterAreaDeConhecimentoController {
 	private AreaDeConhecimentoDAO dao;
 
 	public ManterAreaDeConhecimentoController() {
 		this.dao = new AreaDeConhecimentoDAO();
 	}
 
 	@RequestMapping("novaAreaDeConhecimento")
 	public String form() {
 		return "manter_area_de_conhecimento/incluir_area_de_conhecimento";
 	}
 
 	@RequestMapping("adicionaAreaDeConhecimento")
 	public ModelAndView adiciona(@Valid AreaDeConhecimento areaDeConhecimento,
 			BindingResult result) {
 
 		ModelAndView mv = new ModelAndView(
 				"manter_area_de_conhecimento/incluir_area_de_conhecimento");
 		// mv.addObject("tarefas", tarefas);
 
 		if (result.hasFieldErrors()) {
 			return mv;
 		}
 
 		dao.adiciona(areaDeConhecimento);
 		mv.addObject("sucesso", 1);
 		return mv;
 	}
 
 	@RequestMapping("consultarAreaDeConhecimento")
 	public ModelAndView consulta(HttpServletRequest request) {
 
 		ModelAndView mv = new ModelAndView(
 				"manter_area_de_conhecimento/consultar_area_de_conhecimento");
 
 		if (request.getParameter("titulo") != null) {
 			List<AreaDeConhecimento> areasDeConhecimento = dao
 					.buscaPorTitulo(request.getParameter("titulo"));
 			mv.addObject("areasDeConhecimento", areasDeConhecimento);
 		}
 		return mv;
 	}
 
 	@RequestMapping("excluirAreaDeConhecimento")
 	public void exclui(HttpServletRequest request, HttpServletResponse response) {
 
 		System.out.println("call excluirAreaDeConhecimento()");
 		if (request.getParameter("codigo") != null) {
 			dao.remove(Long.parseLong(request.getParameter("codigo"), 10));
 		}
 		System.out.println("Redireciana para a Home");
 		try {
 			response.sendRedirect("consultarAreaDeConhecimento");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@RequestMapping("alterarAreaDeConhecimento")
 	public String alterar(HttpServletRequest request, Model model) {
 		System.out.println("call alteraAreaDeConhecimento()");
 		if (request.getParameter("codigo") != null) {
 			model.addAttribute("areaDeConhecimento", dao.busca((Long.parseLong(
 					request.getParameter("codigo"), 10))));
 		}
 
 		return "manter_area_de_conhecimento/alterar_area_de_conhecimento";
 	}
 
 	@RequestMapping("alteraAreaDeConhecimento")
 	public ModelAndView altera(@Valid AreaDeConhecimento areaDeConhecimento,
 			BindingResult result) {
 		ModelAndView mv = new ModelAndView(
 				"manter_area_de_conhecimento/alterar_area_de_conhecimento");
 		
 		if (result.hasFieldErrors()) {
 			return mv;
 		}
 
 		dao.altera(areaDeConhecimento);
 
 		mv.addObject("sucesso", 1);
 		return mv;
 	}
 
 }
