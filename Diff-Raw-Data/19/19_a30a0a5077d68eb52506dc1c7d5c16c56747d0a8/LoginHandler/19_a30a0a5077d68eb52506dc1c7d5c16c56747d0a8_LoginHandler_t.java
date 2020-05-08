 package exemplo.jsf.richfaces4.managedbean;
 
import java.io.Serializable;

public class LoginHandler implements Serializable {

	private static final long serialVersionUID = -8085460699907450479L;
 
 	private String login;
 	
 	private String senha;
 
 	private String msg;
 	
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		System.out.println("mudando o login para: " + login);
 		this.login = login;
 	}
 
 	public String getSenha() {
 		return senha;
 	}
 
 	public void setSenha(String senha) {
 		this.senha = senha;
 	}
 	
 	public String getMsg() {
 		return msg;
 	}
 
 	public void setMsg(String msg) {
 		this.msg = msg;
 	}
 
 	public String logar() {
 		if(login.equals("admin") && senha.equals("123")) {
 			System.out.println("login com sucesso");
 			return "success";
 		} else {
 			System.out.println("falha no login");
 			setMsg("Usuario ou Senha invalidos");
 			return "failure";
 		}
 	}
 }
