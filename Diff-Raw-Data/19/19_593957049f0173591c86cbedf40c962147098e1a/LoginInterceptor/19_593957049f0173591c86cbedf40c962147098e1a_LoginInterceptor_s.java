 package br.com.caelum.vraptor.interceptor;
 
 
 
 import br.com.caelum.vraptor.InterceptionException;
 import br.com.caelum.vraptor.Intercepts;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.component.UsuarioLogado;
 import br.com.caelum.vraptor.controller.LoginController;
 import br.com.caelum.vraptor.core.InterceptorStack;
 import br.com.caelum.vraptor.resource.ResourceClass;
 import br.com.caelum.vraptor.resource.ResourceMethod;
 
 @Intercepts
 public class LoginInterceptor implements Interceptor {
 
 	private UsuarioLogado usuarioLogado;
 	private Result result;
 	
 	public LoginInterceptor(UsuarioLogado usuarioLogado, Result result){
 		this.usuarioLogado = usuarioLogado;
 		this.result = result;
 	}
 	
 	@Override
 	public boolean accepts(ResourceMethod method) {
 		ResourceClass resource = method.getResource();
 		
 		return !resource.getType().isAssignableFrom(LoginController.class);
 	}
 
 	@Override
 	public void intercept(InterceptorStack stack, ResourceMethod method,
 			Object instance) throws InterceptionException {
		if (usuarioLogado.getUsuarioLogado() != null){
 			stack.next(method, instance);
 		}else{
 			result.redirectTo(LoginController.class).formulario();
 		}
 		
 	}
	
 }
