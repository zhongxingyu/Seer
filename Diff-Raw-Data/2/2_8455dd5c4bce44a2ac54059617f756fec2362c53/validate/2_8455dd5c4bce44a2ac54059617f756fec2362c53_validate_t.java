 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dwrScripts;
 
 import dao.SMTPAuthentication;
 import daoImpl.*;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import pojo.Acceso;
 import pojo.AsignacionCapaContra;
 import pojo.Bitacora;
 import pojo.Contrato;
 import pojo.EmailSent;
 import pojo.Empresa;
 import pojo.Operacion;
 import pojo.Usuario;
 
 
 // CIPHER / GENERATORS
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.KeyGenerator;
 
 // KEY SPECIFICATIONS
 import java.security.spec.KeySpec;
 import java.security.spec.AlgorithmParameterSpec;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEParameterSpec;
 
 // EXCEPTIONS
 import java.security.InvalidAlgorithmParameterException;
 import java.security.NoSuchAlgorithmException;
 import java.security.InvalidKeyException;
 import java.security.spec.InvalidKeySpecException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import java.io.UnsupportedEncodingException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import pojo.*;
 /**
  *
  * @author mamg
  */
 public class validate {
     Cipher ecipher;
     Cipher dcipher;
     String phase ="dmkIAZfwCmKt$3$";
   
     public validate() {
     }
     
     public int loguearse(String email, String password){
         int respuesta=0;
                         
         UsuarioDaoImpl usudao = new UsuarioDaoImpl();
         Usuario usuario = new Usuario();
         usuario = usudao.findByEmail(email);
         
         if(usuario==null){
             respuesta = 0;//significa que digito mal el email o no existe
         }else{
             if(usuario.getEstado()==4){
                 respuesta=4;//cuenta fue bloqueda
             }else if(usuario.getEstado()==0){
                 respuesta=3;//esperando que se active la cuenta
             }            
             else{
                String pass = encriptar.md5(password);            
                  if(usuario.getPassword().equals(pass)){
                      respuesta = 1;//es la persona correcta
                  }else{                   
                      respuesta = 2;//digito mal el password                   
                 }   
             }                            
         }
             
         return respuesta;
     }
     
     public void  bloquear(String email){
         try{
             UsuarioDaoImpl usudao = new UsuarioDaoImpl();
             Usuario usuario = new Usuario();
             usuario = usudao.findByEmail(email);
             
             int m=0;
             
             //validar que tipo de usuario es?
             if(usuario.getTipoUsuario()==1){// tipo administrador
                 usuario.setEstado(4);
                 usudao.update(usuario);   
                 
                 //obtener el string aleratorio y guardar la solicitud en la tabla email_sent
                 String stringRandom = getStringRandom();
                 Date fecha = new Date();
                 Timestamp momentoTimestamp = new Timestamp(fecha.getTime());
             
                 EmailSent emailSent = new EmailSent(usuario,momentoTimestamp,stringRandom);
                 EmailSentDaoImpl emailDao = new EmailSentDaoImpl(); 
                 emailDao.create(emailSent);
                 
                 String urlLost;                
                 Properties archivoConf = new Properties();
                 archivoConf.load(this.getClass().getClassLoader().getResourceAsStream("/micelanea.properties"));
                 urlLost = (String) archivoConf.getProperty("seceUrl");
                 urlLost = "<a href='"+ urlLost +"/forgetPassword.jsp?liame=" + usuario.getCorreo() +"&&ogidoc="+ stringRandom +"'><span>http://sece.pml.org.ni/forgetPassword.jsp?liame=" + usuario.getCorreo() +"&&ogidoc="+ stringRandom +"</span></a>";
                              
                 //enviarle un correo de que su cuenta ha sido bloqueada.
                 m = EnviarCorreo("sece@pml.org.ni",usuario.getCorreo(),"Bloqueo de Cuenta","<strong>Estimado "+ usuario.getNombre() +",</strong> <p> Su cuenta ha sido bloqueada por varios intentos fallidos de entrar al sistema y haber introducido la contraseña incorrecta. En este link de acontinuacion podra resetear su contraseña <br>"+ urlLost +".</p> <p> Gracias, SECE TEAM.</strong></p>");
                 //(String remitente,String destinatario,String asunto,String mensaje_cuerpo)
             }else if(usuario.getTipoUsuario()==2){ //tipo capacitador o usuario
                 usuario.setEstado(4);
                 usudao.update(usuario);
                 
                 m = EnviarCorreo("sece@pml.org.ni",usuario.getCorreo(),"Bloqueo de Cuenta","<strong>Estimado "+ usuario.getNombre() +",</strong> <p> Su cuenta ha sido bloqueada por varios intentos fallidos de entrar al sistema y haber introducido la contraseña incorrecta. Dirijase al administrador del sistema que le brindara unos pasos para su posterior activacion.</p> <p> Gracias, SECE TEAM.</strong></p>");
                 
                 String nameCapa = usuario.getNombre();
                 usuario = usudao.findAdministrador();
                 
                 m = EnviarCorreo("sece@pml.org.ni",usuario.getCorreo(),"Bloqueo de Cuenta","<strong>Estimado Administrador"+ usuario.getNombre() +",</strong> <p> La cuenta del capacitador "+ nameCapa+" ha sido bloqueada por varios intentos fallidos de entrar al sistema y haber introducido la contraseña incorrecta. Dirijase al capacitador para confirmar que no ha sido un tercero que intenta hackear su cuenta.</p> <p> Gracias, SECE TEAM.</strong></p>");
             }else{ //si es tipo 4 0 3
                 usuario.setEstado(4);
                 usudao.update(usuario);
                 
                 m = EnviarCorreo("sece@pml.org.ni",usuario.getCorreo(),"Bloqueo de Cuenta","<strong>Estimado "+ usuario.getNombre() +",</strong> <p> Su cuenta ha sido bloqueada por varios intentos fallidos de entrar al sistema y haber introducido la contraseña incorrecta. Dirijase al administrador del sistema que le brindara unos pasos para su posterior activacion.</p> <p> Gracias, SECE TEAM.</strong></p>");
             }
         }catch(Exception e){
             System.out.println("El error es --- " + e.getMessage());
         }
         
     }
     
     public void saveActionBitacora(int id_acceso,int id_operacion, String descripcion,int id_elemento,String anterior,String actual){
         
         OperacionDaoImpl opeDao = new OperacionDaoImpl();
         Operacion operacion = new Operacion();
         
         operacion = opeDao.findById(id_operacion);
         
         AccesoDaoImpl accDao = new AccesoDaoImpl();
         Acceso acceso = new Acceso();
         
         acceso = accDao.findById(id_acceso);
         
         Date fecha = new Date();        
         Timestamp momentoTimestamp = new Timestamp(fecha.getTime());
         
         Bitacora bitacora = new Bitacora(operacion,acceso,descripcion,id_elemento,anterior,actual,momentoTimestamp);
         BitacoraDaoImpl bitaDao = new BitacoraDaoImpl();
         bitaDao.create(bitacora);
            
     }     
     
     public int guardarUsuario(String txtname_empresa,String txtdes,String txttel_empresa,int cState,String txtdirec_empre,String txtname,String txtcargo,String txttel,String txtcorreo,String txtdir,String txtpass, int capacitador){  
         try{
             
         String name, cargo,telefono,correo,direccion,pass,name_empresa,descripcion,telefono_empresa,ciudad,direccion_empresa;
         int comboZone;
         
         name_empresa= txtname_empresa;
         descripcion = txtdes;
         telefono_empresa = txttel_empresa;
         ciudad = "";
         comboZone = cState;
         direccion_empresa = txtdirec_empre;
         
         name= txtname;
         cargo= txtcargo;
         telefono= txttel;
         correo= txtcorreo;
         direccion = txtdir;
         pass = txtpass;
   
         String password = encriptar.md5(pass);
        
        //Aqui se guarda la empresa, 2374 managua
        ZoneDaoImpl zoneDao = new ZoneDaoImpl();
        Empresa empresa = new Empresa(zoneDao.findById_Zone(comboZone),name_empresa,descripcion,telefono_empresa,ciudad,direccion_empresa,null);
        EmpresaDaoImpl empresaDao = new EmpresaDaoImpl();
        empresaDao.create(empresa);
           
        //Aqui se guarda el usuario
        //tipo 3 contacto, y estado del usuario 0 porque todavia no ha sido dado de alta.
        Usuario usuario = new Usuario(empresa,name,cargo,telefono,correo,direccion,3,password,0,null,null,null,null,null);
        UsuarioDaoImpl UsuDao= new UsuarioDaoImpl();
        UsuDao.create(usuario);            
        
        Date fecha = new Date();
        Timestamp momentoTimestamp = new Timestamp(fecha.getTime());
        
        //el estado del contrato es 0, porque todavia no ha sido dado de alta
        Contrato contrato = new Contrato(usuario,0,momentoTimestamp,momentoTimestamp,null,null,null);
        ContratoDaoImpl contratoDao = new ContratoDaoImpl();
        contratoDao.create(contrato);
        
        //para asignar al usuario capacitador a un contrato
        
        Usuario usuarioCapa = new Usuario();//un usuario de tipo capacitador
        if (capacitador == -1){        
         usuarioCapa = UsuDao.findById(balanceoCargaCapacitador().getIdUsuario());//UsuDao.findById(2);
         int m = EnviarCorreo("sece@pml.org.ni",usuarioCapa.getCorreo(),"Asignacion Usuario","<strong>Estimado capacitador "+ usuarioCapa.getNombre() +",</strong> <p> una nueva empresa se ha registrado "+ txtname_empresa +" y el sistema de evaluación de competitividad empresarial (SECE) te ha asignado a ella. Entra en tu panel de administración para ver detalle de esa empresa y darle de alta si crees conveniente o denegarle acceso al sistema. </p> <p> Gracias, SECE TEAM.</strong></p>");
        }else{
         usuarioCapa = UsuDao.findById(capacitador);
         
         //cambio el estado a 1 porque su cuenta esta activada
         usuario.setEstado(1);
         UsuDao.update(usuario);
         //igual con el contrato, estaactivado
         contrato.setEstado(1);
         contratoDao.update(contrato);
         
         //guardar asignacion de indicadores delegado como el avance
         List<Indicador> listIndi = new ArrayList<Indicador>();
         IndicadorDaoImpl daoIndicador = new IndicadorDaoImpl();
         listIndi = daoIndicador.findAllByActive();
        
         AvanceDaoImpl daoAvance = new AvanceDaoImpl();
         DelegacionIndiUsuDaoImpl deledao = new DelegacionIndiUsuDaoImpl();
          for(int i=0;i<listIndi.size();i++){
                
            DelegacionIndiUsu dele = new DelegacionIndiUsu(usuario,listIndi.get(i),contrato);
            deledao.create(dele);
            
            Avance avance = new Avance(contrato,listIndi.get(i),0,0,0,0);   
            daoAvance.create(avance);        
           }            
        }
        
        //asigno el nuevo contrato al capacitador
        AsignacionCapaContra as = new AsignacionCapaContra(usuarioCapa,contrato);
        AsignacionCapaContraDaoImpl asDao = new AsignacionCapaContraDaoImpl();
        asDao.create(as);   
      
             return 1;
         }catch(Exception e){
             System.out.println(e.getMessage());
             return 0;
         }
     }
     
     public Usuario balanceoCargaCapacitador(){
         int cantidadContratos = 0;
         
         List<Usuario> listCapa = new ArrayList<Usuario>();
         UsuarioDaoImpl daoUsuario = new UsuarioDaoImpl();
         listCapa = daoUsuario.capacitadoresActivos();
         
         Iterator<Usuario> iterUsu = listCapa.iterator();
         Usuario usuario = new Usuario();
         
         List<AsignacionCapaContra> listAsignacion = new ArrayList<AsignacionCapaContra>();
         AsignacionCapaContraDaoImpl daoAsignacion = new AsignacionCapaContraDaoImpl();
         
         Contrato contrato = new Contrato();
         ContratoDaoImpl daoContra = new ContratoDaoImpl();
         
         String array[][] = new String[listCapa.size()][2];
         int m=0;
         //recorriendo usuario capacitador uno por uno.
         while(iterUsu.hasNext()){
             usuario = iterUsu.next();
             listAsignacion = daoAsignacion.findAllByIdUsuarioCapacitador(usuario);
             array[m][0] = String.valueOf(usuario.getIdUsuario());
             
             Iterator<AsignacionCapaContra> iterAsig  = listAsignacion.iterator();
             
             if(listAsignacion.size()==0){
                 return usuario;// a este usuario le vamos asignar porque no poseia contratos asignados
             }else{
                 // sacamos los contratos realizandose
                 while(iterAsig.hasNext()){
                     contrato = daoContra.findById(iterAsig.next().getContrato().getIdContrato());
                     if(contrato.getEstado()==1){
                        cantidadContratos++; 
                     }
                 }
             }
             
             array[m][1] = String.valueOf(cantidadContratos);
             cantidadContratos = 0;//inicializo en cero para comenzar de nuevo
             m++;
         }
         
         //voy ordenar el array por el metodo de la burbuja
         String aux[][] = new String[1][2];//array auxiliar
         
         //Vamos a recorrer la matriz
         for(int i = 0; i < listCapa.size(); i++){
             for (int j = i + 1; j < listCapa.size(); j++){
                 if(Integer.parseInt(array[j][1]) < Integer.parseInt(array[i][1])){
                     aux[0][0] = array[i][0]; 
                     aux[0][1] = array[i][1]; 
                     
                     array[i][0] = array[j][0];
                     array[i][1] = array[j][1];
                     
                     array[j][0] = aux[0][0];                    
                     array[j][1] = aux[0][1];                    
                 }//fin del if
             }//fin del segundo for j           
         }//fin del primer for i
         
         //aqui busco el primer usuario del array que significaria el que posee mas
         usuario = daoUsuario.findById(Integer.parseInt(array[0][0]));
         
         
         return usuario;
     }
     
     public String getStringRandom(){
         String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
 	int string_length = 9;
 	String randomstring = "";
         
 	for (int i=0; i<string_length; i++) {
 		double rnum = Math.floor(Math.random() * chars.length());
                 
 		randomstring += chars.substring( (int) rnum, (int) rnum+1);                
                 //chars.substring(i, i)
 	}
         return randomstring;
     }
             
     public int passwordOlvidado(String destinatario){
         try{
         
         String stringRandom = getStringRandom();
         
         Usuario usuario = new Usuario();
         UsuarioDaoImpl usuDao = new UsuarioDaoImpl();
         usuario = usuDao.findByEmail(destinatario);
         
         if(usuario==null){
             return 0;//usuario no existe.
         }else{
             
             EmailSentDaoImpl emailDao = new EmailSentDaoImpl();
             EmailSent emailSent = new EmailSent();
             
             emailSent = emailDao.findByUsuario(usuario);
             
             if(emailSent == null){
                 String seceURL;
                 Properties archivoConf = new Properties();
                 archivoConf.load(this.getClass().getClassLoader().getResourceAsStream("/micelanea.properties"));
                 seceURL = (String) archivoConf.getProperty("seceUrl");
         
                 //String url="<a href='http://localhost:8080/sece/forgetPassword.jsp?liame=" + destinatario +"&&ogidoc="+ randomstring +"'><span>http://sece.pml.org.ni/forgetPassword.jsp?liame=" +destinatario+"&&ogidoc="+randomstring +"</span></a>";
                 String url="<a href='"+ seceURL +"/forgetPassword.jsp?liame=" + destinatario +"&&ogidoc="+ stringRandom +"'><span>http://sece.pml.org.ni/forgetPassword.jsp?liame=" +destinatario+"&&ogidoc="+ stringRandom +"</span></a>";
             
                 Date fecha = new Date();
                 Timestamp momentoTimestamp = new Timestamp(fecha.getTime());
             
                 emailSent = new EmailSent(usuario,momentoTimestamp,stringRandom);
              
                 emailDao.create(emailSent);
                     
                 int m = EnviarCorreo("sece@pml.org.ni",destinatario,"Restablecer Contraseña","<strong>Estimado "+ usuario.getNombre() +",</strong> <p> click en el link de abajo para resetear tu contraseña en SECE y eliga una nueva<br>"+ url +"</p> <p> Gracias, SECE TEAM.</strong></p>");
                 return 1; // se guardo correctamente todo.                                
             }else{                
                 return 2;// el usuario posee una solicitud de cambio de contraseña
             }
             
             
         }
         
         }catch(Exception e){
             System.out.println(e.getMessage());            
         }
         return 4;
         
     }
     
     public int existeCorreo(String email){
         Usuario usu = new Usuario();
         UsuarioDaoImpl usuaD = new UsuarioDaoImpl();
         usu = usuaD.findByEmail(email);
         
        if(usu==null){
             return 0;// que el correo no existe 
         }else{
             return 1;// que existe el correo
         }
     }
     
     public String cambiarPasswordByID(int idUsuario,String newContra){
         Usuario usuario = new Usuario();
         try{
         UsuarioDaoImpl usuDao = new UsuarioDaoImpl();
         usuario = usuDao.findById(idUsuario);
         
         String password = encriptar.md5(newContra);
         usuario.setPassword(password);
         usuDao.update(usuario);
         
         EmailSent emailSent = new EmailSent();
         EmailSentDaoImpl emailDao = new EmailSentDaoImpl(); 
         emailSent = emailDao.findByUsuario(usuario);
         emailDao.delete(emailSent);
         
         }catch(Exception e){System.out.println(e.getMessage());}
         return usuario.getCorreo();
     }
     
     public int EnviarCorreo(String remitente,String destinatario,String asunto,String mensaje_cuerpo){
         try{
             String host ="mail.pml.org.ni";
             String from = remitente;//"sece@pml.org.ni";
             String to = destinatario;//"mamg.sept30@gmail.com";
             String subject= asunto;//"Hello";
             String body = mensaje_cuerpo;//"ja aj";
 
             System.out.println ("Prueba para enviar un mail..." + new java.util.Date());
         
             Properties prop = new Properties();
         
             prop.put("mail.smtp.host", host);
             /*Esta línea es la que indica al API que debe autenticarse*/
             prop.put("mail.smtp.auth", "true");
             prop.put("mail.smtp.port", "26");
         
             //*Añadir esta linea si queremos ver una salida detallada del programa*/
             //prop.put("mail.debug", "true");
                            
             SMTPAuthentication auth = new SMTPAuthentication();            
             
             // Get session
             Session session = Session.getInstance(prop, auth);
             
             // Define message
             MimeMessage message = new MimeMessage(session);
             message.setFrom(new InternetAddress(from));
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
             message.setSubject(subject);
             //message.setText(body);
             message.setContent(body,"text/html");
             System.out.println ("Enviando ..." );
             
             Transport.send(message);
             
             System.out.println ("Mensaje enviado!");
             return 1;
         }catch(Exception e){
             System.out.println(e.getMessage());
             return 0;
         }
     } 
     
     
    //encriptar     
    
     
      public void cargar(String passPhrase) {
 
         // 8-bytes Salt
         byte[] salt = {
             (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
             (byte)0x56, (byte)0x34, (byte)0xE3, (byte)0x03
         };
 
         // Iteration count
         int iterationCount = 19;
 
         try {
 
             KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
             SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
 
             ecipher = Cipher.getInstance(key.getAlgorithm());
             dcipher = Cipher.getInstance(key.getAlgorithm());
 
             // Prepare the parameters to the cipthers
             AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
 
             ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
             dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
 
         } catch (InvalidAlgorithmParameterException e) {
             System.out.println("EXCEPTION: InvalidAlgorithmParameterException");
         } catch (InvalidKeySpecException e) {
             System.out.println("EXCEPTION: InvalidKeySpecException");
         } catch (NoSuchPaddingException e) {
             System.out.println("EXCEPTION: NoSuchPaddingException");
         } catch (NoSuchAlgorithmException e) {
             System.out.println("EXCEPTION: NoSuchAlgorithmException");
         } catch (InvalidKeyException e) {
             System.out.println("EXCEPTION: InvalidKeyException");
         }
     }
      
     public String encrypt(String str) {
         try {
             cargar(phase);
             // Encode the string into bytes using utf-8
             byte[] utf8 = str.getBytes("UTF8");
 
             // Encrypt
             byte[] enc = ecipher.doFinal(utf8);
 
             // Encode bytes to base64 to get a string
             return new sun.misc.BASE64Encoder().encode(enc);
 
         } catch (BadPaddingException e) {
         } catch (IllegalBlockSizeException e) {
         } catch (UnsupportedEncodingException e) {
         } catch (IOException e) {
         }
         return null;
     }
       
     public String decrypt(String str) {
 
         try {
              cargar(phase);
             // Decode base64 to get bytes
             byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
 
             // Decrypt
             byte[] utf8 = dcipher.doFinal(dec);
 
             // Decode using utf-8
             return new String(utf8, "UTF8");
 
         } catch (BadPaddingException e) {
         } catch (IllegalBlockSizeException e) {
         } catch (UnsupportedEncodingException e) {
         } catch (IOException e) {
         }
         return null;
     }
     
 }
