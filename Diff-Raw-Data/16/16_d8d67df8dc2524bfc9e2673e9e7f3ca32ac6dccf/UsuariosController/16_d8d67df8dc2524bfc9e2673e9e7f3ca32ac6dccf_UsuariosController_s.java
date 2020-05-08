 package br.caelum.vraptor.controller;
 
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Time;
 import java.util.Date;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tomcat.util.http.fileupload.IOUtils;
 
 import com.sun.jmx.snmp.Timestamp;
 
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.blank.IndexController;
 import br.com.caelum.vraptor.dao.UsuarioDao;
 import br.com.caelum.vraptor.dao.UsuarioWeb;
 import br.com.caelum.vraptor.infra.Arquivo;
 import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
 import br.com.caelum.vraptor.models.Playlist;
 import br.com.caelum.vraptor.models.Usuario;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 
 @Resource
 public class UsuariosController {
 
 	private final UsuarioDao dao;
 	private final Result result;
 	private final ServletContext context;
 	
 	private final Validator validator;
 	private final HttpServletRequest request;
 	private final HttpServletResponse response;
 
 	private final UsuarioWeb usuarioWeb;
 
     public UsuariosController(UsuarioDao dao, Result result, final ServletContext context, HttpServletRequest request, HttpServletResponse response, Validator validator, UsuarioWeb usuarioWeb) {
         this.dao = dao;
         this.result = result;
         this.context = context;
         this.request = request;
         this.response = response;
         this.validator = validator;
         this.usuarioWeb = usuarioWeb;
     }
 
     public void adiciona(Usuario usuario, String confirmacao) {
 		//Timestamp timestampObj = new Timestamp();
 		//long timeStamp = timestampObj.getDateTime();
 		
 		//Arquivo fotoUsuarioUsuario = new Arquivo(fotoUsuario, "usuarios", timeStamp);
     	//fotoUsuarioUsuario.salvaArquivo();
     	
     	//Arquivo capaUsuarioUsuario = new Arquivo(capaUsuario, "usuarios", timeStamp);
     	//capaUsuarioUsuario.salvaArquivo();
     	
     	//Usuario.setFoto(timeStamp + fotoUsuario.getFileName());
     	//Usuario.setCapa(timeStamp + capaUsuario.getFileName());
     	
     	
    	
     	if (dao.existeUsuario(usuario)) {
     	      validator.add(new ValidationMessage("Nome de usuário já existe", 
     	          "usuario.usuario"));
         }
 

    	if (usuario.getSenha() != confirmacao) {
    		validator.add(new ValidationMessage("As senhas informadas não são iguais", 
      	          ""));
    	}

    	validator.onErrorUsePageOf(IndexController.class).login();
     	
     	
         dao.salva(usuario);
         
         result.include("notice", "Usuario adicionado com sucesso!");
         //result.redirectTo(this).lista();
         result.redirectTo(IndexController.class).index();
     }
     
     
     @Post("/login_validar")
     public void login_validar(Usuario usuario) {
       Usuario carregado = dao.carrega(usuario);
       
       if (carregado == null) {
         validator.add(
             new ValidationMessage("Usuário e/ou senha inválidos",
                 "usuario.usuario"));
       }
       
       System.out.println("Errors: -----------------------");
       
       System.out.println(validator.getErrors());
       
       validator.onErrorUsePageOf(IndexController.class).login();
 
       usuarioWeb.login(carregado);
 
       System.out.println("Deveria redirecionar...");
       
       result.redirectTo(IndexController.class).index();
     }
  
     
     @Path("/logout")
     public void logout() {
       usuarioWeb.logout();
       result.redirectTo(IndexController.class).login();
     }
     
 	/* public List<Playlist> lista() {*/
 		/*return dao.listaTudo();*/
 /*	}*/
 }
