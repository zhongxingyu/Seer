 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.educautecisystems.core;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import org.educautecisystems.Esquemas.Ingreso;
 import org.educautecisystems.core.config.ChatServerConf;
 import org.educautecisystems.core.config.ChatSessionConf;
 import org.educautecisystems.intefaz.VentanaPrincipal;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.Namespace;
 import org.jdom2.input.SAXBuilder;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 /**
  *
  * @author Shadow2012
  */
 public class Sistema {
     private static EntityManagerFactory emf = null;
 	public static final String NOMBRE_CARPETA_CONFIGURACION = "EducaUteciSystems";
 	public static final String NOMBRE_CARPETA_CONF_CHAT = "Chat";
 	public static final String NOMBRE_CARPETA_CONF_ARCHIVOS_COMPARTIDOS = "Compartido";
 	public static final String CHAT_CONF_XML = "ChatServerConf.xml";
 	
 	/* Configuración de Chat */
 	private static ChatServerConf	chatServerConf;
	private static ChatSessionConf	chatSessionConf;
	
	/* Archivo por defecto */
	private static final String ip_defecto = "localhost";
	private static final String port_defecto = "7586";
	private static final String nickname_defecto = "nLastName";
	private static final String realName_defecto = "Name LastName";
 	
     public static void main( String []args ) {
         String usuario = "root";
         String password = "admin";
         
         inicializarSistema( usuario,password );
         seleccionadoLookAndFeel();
 		cargarCarpeta();
         new VentanaPrincipal().setVisible(true);
     }
     
     private static void inicializarSistema( String usuario, String password ) {
         Map parametros = new HashMap();
         parametros.put("javax.persistence.jdbc.password", password);
         parametros.put("javax.persistence.jdbc.user", usuario);
         
         emf = Persistence.createEntityManagerFactory("EducaUteciSystemsPU", parametros);
 
 		/* No se pudo detectar la base de datos */
 		if ( !emf.isOpen() ) {
 			System.err.println("No se pudo abrir la base de datos.");
 			System.exit(-1);
 		}
     }
     
     public static void cerrarSistema() {
         System.out.println("Cerrando Sistema..");
         emf.close();
         System.exit(0);
     }
 
     /**
      * @return the emf
      */
     public static EntityManagerFactory getEmf() {
         return emf;
     }
 	
 	public static String getMD5( String text ) {
 		try {
 			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
 			byte[] array = md.digest(text.getBytes());
 			StringBuilder sb = new StringBuilder();
 			for (int i = 0; i < array.length; ++i) {
 				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
 			}
 			return sb.toString();
 		} catch (java.security.NoSuchAlgorithmException e) {
 		}
 		return null;
 	}
     
     private static void seleccionadoLookAndFeel() {
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(Ingreso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(Ingreso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(Ingreso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(Ingreso.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
     }
 	
 	private static void cargarCarpeta() {
 		Properties propiedadesSistema = System.getProperties();
 		String carpetaUsuario =			propiedadesSistema.getProperty("user.home");
 		
 		/* Carpetas de configuraciones */
 		File carpetaConfiguracion = new File(carpetaUsuario, NOMBRE_CARPETA_CONFIGURACION);
 		File carpetaConfChat =		new File(carpetaConfiguracion, NOMBRE_CARPETA_CONF_CHAT);
 		File carpetaConfArchivos =	new File(carpetaConfiguracion, NOMBRE_CARPETA_CONF_ARCHIVOS_COMPARTIDOS);
 		
 		/* Crear carpetas si no existen */
 		if ( !carpetaConfiguracion.exists() ) {
 			carpetaConfiguracion.mkdirs();
 		}
 		if ( !carpetaConfChat.exists() ) {
 			carpetaConfChat.mkdirs();
 		}
 		if ( !carpetaConfArchivos.exists() ) {
 			carpetaConfArchivos.mkdirs();
 		}
 		
 		/* Archivos de configuración */
 		File archivoConfChatXML = new File(carpetaConfChat, CHAT_CONF_XML);
 		
 		if ( archivoConfChatXML.exists() && archivoConfChatXML.isFile() ) {
 			cargarChatConf(archivoConfChatXML);
 		} else {
 			generarChatConf(archivoConfChatXML);
 		}
 	}
 	
 	private static void cargarChatConf ( File archivoConfChatXML ) {
 		ChatServerConf	lChatServerConf =	new ChatServerConf();
 		ChatSessionConf lChatSessionConf =	new ChatSessionConf();
 		
 		SAXBuilder builder = new SAXBuilder();
 		Document documento = null;
 		
 		try {
 			documento = builder.build(archivoConfChatXML);
 		} catch ( JDOMException jdome ) {
 			System.err.println("JDOME: "+jdome);
 		} catch ( IOException ioe ) {
 			System.err.println("IOE: "+ioe);
 		}
 		
 		Namespace baseNamespace = Namespace.getNamespace("chat", "http://free.chat.com/");
 		Element root = documento.getRootElement();
 		
 		/* Datos del servidor */
 		Element eServidor = root.getChild("server", baseNamespace);
 		lChatServerConf.setIp(eServidor.getChildText("ip"));
 		lChatServerConf.setPort(eServidor.getChildText("port"));
 		
 		/* Datos de la sesión */
 		Element eSession = root.getChild("session", baseNamespace);
 		lChatSessionConf.setNickname(eSession.getChildText("nickname"));
 		lChatSessionConf.setRealName(eSession.getChildText("real_name"));
 		
 		/* Guardar información */
 		Sistema.chatServerConf = lChatServerConf;
 		Sistema.chatSessionConf = lChatSessionConf;
 	}
 	
 	private static void generarChatConf( File archivoConfChatXML ) {
 		Document document = new Document();
 		
 		Namespace baseNamespace = Namespace.getNamespace("chat", "http://free.chat.com/");
 		Element root = new Element("config", baseNamespace);
 		
 		/* Datos servidor */
 		Element eServidor = new Element("server", baseNamespace);
		eServidor.addContent(new Element("ip").setText(ip_defecto));
		eServidor.addContent(new Element("port").setText(port_defecto));
 		root.addContent(eServidor);
 		
 		/* Datos sesión */
 		Element eSession = new Element("session", baseNamespace);
		eSession.addContent(new Element("nickname").setText(nickname_defecto));
		eSession.addContent(new Element("real_name").setText(realName_defecto));
 		root.addContent(eSession);
 		
 		/* Guardar archivo */
 		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
 		document.setRootElement(root);
 		
 		try {
 			outputter.output(document, new FileOutputStream(archivoConfChatXML));
 		} catch( IOException ioe ) {
 			System.err.println("No se puedo crear archivo de configuración.");
 		}
		
		/* Iniciar información */
		chatServerConf = new ChatServerConf(ip_defecto, port_defecto);
		chatSessionConf = new ChatSessionConf(nickname_defecto, realName_defecto);
 	}
 }
