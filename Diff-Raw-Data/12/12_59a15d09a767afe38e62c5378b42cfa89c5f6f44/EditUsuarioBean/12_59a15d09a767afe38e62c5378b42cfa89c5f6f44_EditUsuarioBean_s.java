 package managedBeans;
 
 import hibernate.EditUsuarioHibernate;
 
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import model.Usuario;
 
 public class EditUsuarioBean {
 	private Usuario editUsuario;
 	private List<Usuario> colaboradoresList;
 	private String filtroPermissao;
 	private String msgCadastro;
 	private int id_usuario_editado;
 	private String senhaAntiga;
 	private HttpSession session = new CriaHttpSession().getSession();
 	
 	public void updateUsuario(){
 		if (editUsuario.getSenha().equals("")){
 			editUsuario.setSenha(senhaAntiga);
 		}
 		
 		new EditUsuarioHibernate().updateUsuario(editUsuario);
 		msgCadastro = "Usurio "+editUsuario.getLogin()+" alterado com sucesso";
 	}
 	
 	public void populaColaboradores(){
 		Usuario u = (Usuario) session.getAttribute("usuario");
		u.setPermissao(filtroPermissao);
		colaboradoresList = new EditUsuarioHibernate().buscarColaboradores(u);
 	}
 	
 	public void setaUsuarioEdicao(){
 		for (Usuario u : colaboradoresList){
			if (u.getId_usuario() == id_usuario_editado){
				editUsuario = u;
 				senhaAntiga = editUsuario.getSenha();
 			}
 		}
 		msgCadastro = null;
 	}
 	
 	public Usuario getEditUsuario() {
 		return editUsuario;
 	}
 
 	public void setEditUsuario(Usuario editUsuario) {
 		this.editUsuario = editUsuario;
 	}
 
 	public List<Usuario> getColaboradoresList() {
 		return colaboradoresList;
 	}
 
 	public void setColaboradoresList(List<Usuario> colaboradoresList) {
 		this.colaboradoresList = colaboradoresList;
 	}
 
 	public String getFiltroPermissao() {
 		return filtroPermissao;
 	}
 
 	public void setFiltroPermissao(String filtroPermissao) {
 		this.filtroPermissao = filtroPermissao;
 	}
 
 	public String getMsgCadastro() {
 		return msgCadastro;
 	}
 
 	public void setMsgCadastro(String msgCadastro) {
 		this.msgCadastro = msgCadastro;
 	}
 
 	public int getId_usuario_editado() {
 		return id_usuario_editado;
 	}
 
 	public void setId_usuario_editado(int id_usuario_editado) {
 		this.id_usuario_editado = id_usuario_editado;
 	}
 	
 }
