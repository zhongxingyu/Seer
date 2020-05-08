 package financeiro.web;
 
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ValueChangeEvent;
 
 import financeiro.conta.Conta;
 import financeiro.conta.ContaBO;
 import financeiro.usuario.Usuario;
 import financeiro.usuario.UsuarioBO;
 
 @ManagedBean(name = "contextoBean")
 @SessionScoped
 public class ContextoBean {
 	
 	private Usuario usuarioLogado = null;
 	
 	private Conta contaAtiva = null;
 	
	private List<Conta> contas = null;
	
 	public Usuario getUsuarioLogado() {
 		FacesContext context = FacesContext.getCurrentInstance();
 		ExternalContext external = context.getExternalContext();
 		String login = external.getRemoteUser();
 		
 		if (usuarioLogado == null || !usuarioLogado.getLogin().equals(login)) {
 			if (login != null) {
 				UsuarioBO usuarioBO = new UsuarioBO();
 				usuarioLogado = usuarioBO.buscarPorLogin(login);
 				contaAtiva = null;
 			}
 		}
 		
 		return usuarioLogado;
 	}
 	
 	public void setUsuarioLogado(Usuario usuario) {
 		usuarioLogado = usuario;
 	}
 	
 	public Conta getContaAtiva() {
 		if (contaAtiva == null) {
 			Usuario usuario = getUsuarioLogado();
 			ContaBO contaBO = new ContaBO();
 			contaAtiva = contaBO.buscarFavorita(usuario);
 			if (contaAtiva == null) {
 				List<Conta> contas = contaBO.listar(usuario);
 				if (contas != null) {
 					for (Conta conta : contas) {
 						contaAtiva = conta;
 						break;
 					}
 				}
 			}
 		}
 		return contaAtiva;
 	}
 	
 	public void setContaAtiva(ValueChangeEvent event) {
		Integer conta = Integer.valueOf(event.getNewValue().toString());
 		ContaBO contaBO = new ContaBO();
 		contaAtiva = contaBO.carregar(conta);
 	}
	
	public List<Conta> getContas() {
		if (contas == null) {
			ContaBO contaBO = new ContaBO();
			contas = contaBO.listar(getUsuarioLogado());
		}
		return contas;
	}
 }
