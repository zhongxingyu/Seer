 package br.com.puc.sispol.interceptor;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 public class AutorizadorInterceptor extends HandlerInterceptorAdapter {
 
 	@Override
 	public boolean preHandle(HttpServletRequest request,
 			HttpServletResponse response, Object controller) throws Exception {
 		String uri = request.getRequestURI();
 		if (uri.endsWith("loginForm") || uri.endsWith("efetuaLogin")
 				|| uri.contains("resources") || uri.contains("mostraHome")
				|| uri.contains("novoUsuario") || uri.contains("apura") || uri.contains("lassificacaoSimulado")
 
 		) {
 			System.out.println("Carrega como usuário não logado");
 			return true;
 		}
 		if ((request.getSession().getAttribute("usuarioLogado") != null)) {
 			System.out.println("Carrega como usuário logado");
 			return true;
 		}
 		
 		System.out.println("Redireciana para a Home");
 		response.sendRedirect("mostraHome");
 		return false;
 	}
 	
 }
