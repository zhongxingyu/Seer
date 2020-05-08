 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.OneToMany;
 
 import notifiers.Mails;
 import play.data.validation.*;
 import play.db.jpa.Model;
 import play.libs.Codec;
 import play.modules.search.Query;
 import play.modules.search.Search;
 
 @Entity
 public class Usuario extends Model {
 
 	@Required(message="Tienes que poner un nombre de usuario")
 	public String username;
 
     @Required(message="La dirección de email tiene que ser válida")
     @Email
     @CheckWith(value=MailUniqueCheck.class, message="Hay un usuario ya registrado con esta dirección de email")
     public String email;
 
 	public String descripcion;
 	public Long creditos;
 
     @Required(message="Tienes que proporcionar un password")
     @MinSize(value=4, message="El password tiene que tener 4 caracteres como mínimo")
     public String password;
 
     public String needConfirmation;
 	
     @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "experto")
     public List<Tema> temas;
     
     @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "interesado")
     public List<Busqueda> intereses;
 
     @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "interesado")
     public List<Encuentro> encuentrosSolicitados;
 
     public Usuario register() {
         String hashPassword = Codec.hexMD5(password);
         password = hashPassword;
 
         needConfirmation = Codec.UUID();
 
         Usuario usuarioGrabado = save();
         Mails.confirm(usuarioGrabado);
 
         return usuarioGrabado;
     }
 
     public static Usuario confirm(String uuid) {
         Usuario confirmado = Usuario.find("needConfirmation", uuid).first();
 
         if (confirmado != null) {
             confirmado.needConfirmation = null;
             confirmado.save();
         }
 
         return confirmado;
     }
 
     public static Usuario connect(String email, String password) {
         String passwordHash = Codec.hexMD5(password);
 
         Usuario found = Usuario.find(
                 "email = ? and password = ? and needConfirmation is null", email, passwordHash).first();
 
         return found;
     }
 
     public Usuario resetPassword() {
         String newPassword = Codec.UUID().substring(0, 5);
         password = Codec.hexMD5(newPassword);
 
         Usuario savedUser = save();
         Mails.send_password(savedUser, newPassword);
 
         return savedUser;
     }
 
     public List<Encuentro> findEncuentrosOfrecidos() {
         return Encuentro.find("from Encuentro where tema.experto.id = ?", id).fetch();
     }
 
     public List<Tema> findTemasDeInteres() {
         List<Tema> temasDeInteres = new ArrayList<Tema>();
 
         for (Busqueda interes : intereses) {
             Query query = Search.search("titulo:(" + interes.texto + ")", Tema.class);
             List<Tema> temasParaInteres = query.fetch();
             temasDeInteres.addAll(temasParaInteres);
         }
 
         return temasDeInteres;
     }
 
     static class MailUniqueCheck extends Check {
         public boolean isSatisfied(Object usuario, Object email) {
             return Usuario.find("email", email).first() == null;
         }
     }
 
 }
