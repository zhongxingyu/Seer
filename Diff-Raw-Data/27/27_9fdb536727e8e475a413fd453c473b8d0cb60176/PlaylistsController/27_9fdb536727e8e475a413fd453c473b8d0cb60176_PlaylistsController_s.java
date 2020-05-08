 package br.caelum.vraptor.controller;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.sql.Time;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tomcat.util.http.fileupload.IOUtils;
 
 import com.sun.jmx.snmp.Timestamp;
 
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import br.com.caelum.vraptor.blank.IndexController;
 import br.com.caelum.vraptor.dao.PlaylistDao;
 import br.com.caelum.vraptor.dao.UsuarioWeb;
 import br.com.caelum.vraptor.infra.Arquivo;
 import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
 import br.com.caelum.vraptor.models.Playlist;
 import br.com.caelum.vraptor.models.Usuario;
 
 @Resource
 public class PlaylistsController {
 
 	private final PlaylistDao dao;
 	private final Result result;
 	private final ServletContext context;
 	
 	private final HttpServletRequest request;
 	private final HttpServletResponse response;
 	
 	private final UsuarioWeb usuarioWeb;
 	
 
     public PlaylistsController(PlaylistDao dao, Result result, final ServletContext context, HttpServletRequest request, HttpServletResponse response,  UsuarioWeb usuarioWeb) {
         this.dao = dao;
         this.result = result;
         this.context = context;
         this.request = request;
         this.response = response;
         this.usuarioWeb = usuarioWeb;
     }
 
     public void adiciona(Playlist playlist, UploadedFile foto) {
 		Timestamp timestampObj = new Timestamp();
 		long timeStamp = timestampObj.getDateTime();
 		
     	Arquivo arquivoCapa = new Arquivo(foto, "playlists", timeStamp);
     	arquivoCapa.salvaArquivo();
     	
     	playlist.setImagemCapa(timeStamp + foto.getFileName());
     	
     	Usuario usuario = new Usuario();
     	usuario.setId(this.usuarioWeb.getId());
     	
     	playlist.setUsuario(usuario);
     
         dao.salva(playlist);
         
         result.include("notice", "Playlist criada com sucesso!");
         //result.redirectTo(this).lista();
         result.redirectTo(IndexController.class).index();
     }
 
     public void formulario(){
     	
     }
  
     public Playlist edita(Long id){
     	return dao.carrega(id);
     }
     
     public void altera(Playlist playlist) {
         dao.atualiza(playlist);
         result.include("notice", "Playlist alterada com sucesso!");
         result.redirectTo(this).lista();
     }
     
     public void remove(Long id) {
         Playlist playlist = dao.carrega(id);
         dao.remove(playlist);
         
         result.include("notice", "Playlist removida com sucesso!");
         result.redirectTo(this).lista();
     }
  
 	public List<Playlist> lista() {
 		return dao.listaTudo();
 	}
 }
