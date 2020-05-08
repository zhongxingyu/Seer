 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dao;
 
 import Datos.*;
 import Integracion.ConexionSAT.CSD;
 import Negocios.Cifrado.Cifrado;
 import Negocios.ObtenerFiel.Fiel;
 import Negocios.ObtenerFolios.Folio;
 import com.mysql.jdbc.Blob;
 import java.io.*;
 import java.security.NoSuchProviderException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import org.apache.fop.apps.FOPException;
 import org.jdom.Document;
 import subsistemaISFE.EnvioMail;
 
 /**
  * Servlet encargada de la generación de la factura electrónica
  * @author lupe
  */
 public class Factura extends HttpServlet {
 
     /**
      * Processes requests for both HTTP
      * <code>GET</code> and
      * <code>POST</code> methods.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         PrintWriter out = response.getWriter();
         String idFolioPDF = null;
         String idUsuario = null;
         
         if ("Clientes".equals(request.getParameter("Factura"))) {
 
             try {
                 String aux = request.getParameter("idUsuario");
                 aux = Cifrado.decodificarBase64(aux);
                 Sql s = new Sql();
                 String sql = "select idCliente, tipoPersona,nombreCliente,APaternoCliente,AMaternoCliente,razonCliente from cliente where idUsuario= " + aux + ";";
                 ResultSet rs;
                 rs = s.consulta(sql);
 
                 while (rs.next()) {
                     if (rs.getBoolean("tipoPersona") == false) {
                         out.println("<option value=\"" + rs.getInt("idCliente") + "\">" + rs.getString("nombreCliente") + " " + rs.getString("APaternoCliente") + " " + rs.getString("AMaternoCliente") + "</option>");
                     } else {
                         out.println("<option value=\"" + rs.getInt("idCliente") + "\">" + rs.getString("razonCliente") + "</option>");
                     }
 
                 }
 
 
             } catch (InstantiationException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 out.close();
             }
         } else if ("Generar".equals(request.getParameter("Factura"))) {
              try {
                 String aux = request.getParameter("idUsuaio");
                 aux = Cifrado.decodificarBase64(aux);
                 //INSTANCIAS DE LAS CLASES A UTILIZAR DURANTE EL PROCESO DE LA GENERACIÓN DE LA FACTURA
                 Sql s = new Sql();
                 Usuario emisor = new Usuario();
                 Contribuyente receptor = new Contribuyente();
                 CSD csd = new CSD();
                 Fiel fiel = new Fiel();
                 Direccion d = new Direccion();
                 Direccion dReceptor = new Direccion();
                 
                 //DATOS DEL EMISOR (USUARIO)
                 //out.println("a");
                 String sql = "select u.tipoPersona,u.nombre,u.apellidoMaterno,u.apellidoPaterno,u.razonSocial,u.curp,u.rfc,u.mail, f.archivoFiel, c.noCertificado, c.archivoCSD, d.codigoPostal, d.calle ,d.nombreLocalidad,d.nombreMunicipio, d.nombreEstado from usuario u, csd c, fiel f, direccionusuario d where u.idUsuario = " + aux + " and c.idCSD = u.idCSD and f.idFiel = u.idFiel and d.idUsuario = u.idUsuario;";
                 ResultSet rs;
                 rs = s.consulta(sql);
                 //out.println("1");
                 while (rs.next()) {
                     emisor.setTipoPersona(rs.getBoolean("tipoPersona"));
                     if (rs.getBoolean("tipoPersona") == false) {
                         emisor.setNombre(rs.getString("nombre"));
                         emisor.setApMaterno(rs.getString("apellidoMaterno"));
                         emisor.setApPaterno(rs.getString("apellidoPaterno"));
                         emisor.setCurp(rs.getString("curp"));
                     } else {
                         emisor.setRazonSocial(rs.getString("razonSocial"));
                     }
                     emisor.setRFC(rs.getString("rfc"));
                     emisor.setCorreo(rs.getString("mail"));
                     //CSD DEL EMISOR
                     csd.setArchivoCSD(rs.getBytes("archivoCSD"));
                     csd.setNoCertificado(rs.getString("noCertificado"));
                     emisor.setCSD(csd);
                     //FIEL DEL EMISOR
                     fiel.setArchivoFiel(rs.getBytes("archivoFiel"));
                     fiel.setPassword(request.getParameter("passwordFiel"));
                     emisor.setFiel(fiel);
                     //DIRECCION DEL EMISOR
                     d.setCodigoPostal(rs.getString("codigoPostal"));
                     d.setLocalidad(rs.getString("nombreLocalidad"));
                     d.setMunicipio(rs.getString("nombreMunicipio"));
                    d.setEstado(rs.getString("nombreEstado"));
                     d.setCalle(rs.getString("calle"));
                     emisor.setDireccion(d);
                 }
                 
                 //DATOS DEL RECEPTOR (CLIENTE)
                 //out.println("b");
                 String sqlRec = "select c.tipoPersona,c.nombreCliente,c.APaternoCliente,c.AMaternoCliente,c.razonCliente,c.rfc, d.codigoPostal, d.calleCliente,d.nombreLocalidad,d.nombreMunicipio, d.nombreEstado from cliente c, direccioncliente d where c.idUsuario = " + aux + " and d.idCliente = c.idCliente;";
                 ResultSet rsRec;//SQLException after end of result set
                 rsRec = s.consulta(sqlRec);
                 while (rsRec.next()) {
                     receptor.setTipoPersona(rsRec.getBoolean("tipoPersona"));
                     if (rsRec.getBoolean("tipoPersona") == false) {
                         out.println("a");
                         receptor.setNombre(rsRec.getString("nombreCliente"));
                         receptor.setApMaterno(rsRec.getString("APaternoCliente"));
                         receptor.setApPaterno(rsRec.getString("AMaternoCliente"));
                         out.println("b");
                     } else {
                         out.println("c");
                         receptor.setRazonSocial(rsRec.getString("razonCliente"));
                         out.println("d");
                     }
                    out.println(rsRec.getString("rfc"));
                    receptor.setRFC(rsRec.getString("rfc"));out.println("1");
                     //DIRECCION DEL RECEPTOR
                     dReceptor.setCodigoPostal(rsRec.getString("codigoPostal"));out.println("2");
                     dReceptor.setLocalidad(rsRec.getString("nombreLocalidad"));out.println("3");
                     dReceptor.setMunicipio(rsRec.getString("nombreMunicipio"));out.println("4");
                     dReceptor.setEstado(rsRec.getString("nombreEstado"));out.println("5");
                     dReceptor.setCalle(rsRec.getString("calleCliente"));out.println("6");
                     receptor.setDireccion(dReceptor);
                 }
                 
                 //DATOS DE LA FACTURA (CONCEPTOS,SUBTOTAL,IVA,DESCUENTO,TOTAL,EXPEDICIÓN,ETC)
                 Datos.Factura f = new Datos.Factura();
                //int numProductos=Integer.parseInt(request.getParameter("cantcampos"));//numero de productos que se recibiran
                 ArrayList<Datos.Concepto> productos = new ArrayList<Datos.Concepto>();
                 //DATOS DE LOS CONCEPTOS DE LA FACTURA
                 
                 /**for(int i=0;i<numProductos;i++){
                     Datos.Concepto concepto = new Datos.Concepto();
                     concepto.setCantidad(Double.parseDouble(request.getParameter("cantidad"+i)));
                     concepto.setnombreProducto(request.getParameter("nombre"+i));
                     concepto.setValorUnitario(Double.parseDouble(request.getParameter("unitario"+i)));
                     concepto.setImporte(Double.parseDouble(request.getParameter("total"+i)));
                     concepto.setDescripcion(concepto.getnombreProducto() + ": " + request.getParameter("descripcion"+i));
                     productos.add(concepto);
                 }*/
                 
                 //DATOS DEL CONCEPTO PARA LA VERSION DE PRUEBA
                 Datos.Concepto conceptos = new Datos.Concepto();
                 conceptos.setCantidad(Double.parseDouble(request.getParameter("cantidad")));
                 conceptos.setnombreProducto(request.getParameter("nombre"));
                 conceptos.setValorUnitario(Double.parseDouble(request.getParameter("unitario")));
                 conceptos.setImporte(Double.parseDouble(request.getParameter("total")));
                 conceptos.setDescripcion(conceptos.getnombreProducto() + ": " + request.getParameter("descripcion"));
                 productos.add(conceptos);
                 f.setConceptos(productos);
                 
                 //DATOS GENERALES DE LA FACTURA
                 f.setFormaDePago(request.getParameter("formaDePago"));   //CAMPO A AGREGAR EN FORMULARIO
                 f.setSubTotal(Double.parseDouble(request.getParameter("subTotal")));
                 f.setIVA(Double.parseDouble(request.getParameter("iva")));
                 f.setDescuento(Double.parseDouble(request.getParameter("descuento")));
                 f.setTotal(Double.parseDouble(request.getParameter("GranTotal")));
                 f.setTipoDeComprobante(request.getParameter("tipoComprobante"));   //CAMPO A AGREGAR EN FORMULARIO
                 //
                 f.setEmisor(emisor);
                 f.setReceptor(receptor);
                 //FOLIO DE LA FACTURA
                 String sqlFolio = "select idFolio, numFolio from folios where usado=0 limit 1;";
                 ResultSet rsFolio;
                 rsFolio = s.consulta(sqlFolio);
                 rsFolio.next();
                 String idFolio = rsFolio.getString("idfolio");
                 String numFolio = rsFolio.getString("numfolio");
                 //
                 Folio folio = new Folio();
                 folio.setNoFolio(Long.parseLong(numFolio));
                 folio.setUUID(Long.parseLong(idFolio));
                 f.setFolio(folio);
                 //ESTABLECIENDO EL ESTADO DEL FOLIO USADO
                 String actualizaEstadoFolio = "update folios set usado=1 where usado=0 and idFolio = " + idFolio + " limit 1";
                s.ejecuta(actualizaEstadoFolio);
                 //DATOS DEL LUGAR DE EXPEDCIÓN DE LA FACTURA
                 Direccion expedidoEn = new Direccion();
                 expedidoEn.setCalle("JUAN DE DIOS BATIZ");
                 expedidoEn.setCodigoPostal("07738");
                 expedidoEn.setEstado("DISTRIO FEDERAL");
                 expedidoEn.setMunicipio("GUSTAVO A. MADERO");
                 f.setExpedidoEn(expedidoEn);
 
                 //DATOS DE ISFE COMO PAC 
                 XML xml = new XML();
                 Datos.ISFE isfe = new Datos.ISFE();
                 out.println("d");
                 String sqlISFE = "select u.idUsuario,f.archivoFiel,c.noCertificado,c.archivoCSD from fiel f,csd c,usuario u where u.idUsuario=1 and u.idFiel=f.idFiel and u.idCSD=c.idCSD;";
                 ResultSet rsIsfe = s.consulta(sqlISFE);
                 Fiel fielISFE = new Fiel();
                 CSD csdISFE = new CSD();
                 while (rsIsfe.next()) {
                     fielISFE.setArchivoFiel(rsIsfe.getBytes("f.archivoFiel"));
                     csdISFE.setArchivoCSD(rsIsfe.getBytes("c.archivoCSD"));
                     csdISFE.setNoCertificado(rsIsfe.getString("c.noCertificado"));
                 }
                 fielISFE.setPassword("a0123456789");
                 isfe.setFiel(fielISFE);
                 isfe.setCSD(csdISFE);
                 
                 //GENERACION DEL XML 
                Document facturaXML = xml.generarXML(f, isfe, "\\ISFE-20110020\\resources\\xml\\");
                 File fXML = XML.generarArchivoXML(facturaXML, emisor.getRFC() + folio.getNoFolio() + receptor.getRFC() + ".xml");
                 
                 //ENVIO DEL XML AL CORREO DEL USUARIO 
                 EnvioMail mail = new EnvioMail();
                 mail.EnvioMail(emisor.getCorreo(), "Entrega de Factura Electrónica ISFE " + new Date(), "ISFE, hace entrega de la factura electrónica en formato XML.\nHacemos de su conocimiento que en este momento el resguardo de la factura\nes responsabilidad de usted.\n\nGracias por utilizar ISFE.", fXML, emisor.getRFC() + folio.getNoFolio() + receptor.getRFC() + ".xml");
                 
                 //ALMACENAMIENTO DEL XML EN LA BASE DE DATOS DE ISFE
                 String sqlFactura = "instert into factura (facturaXML,formaPago,idUsuario,idFolio,nombreXML) values (" + fXML + ",'" + f.getFormaDePago() + "'," + aux + "," + idFolio + ",'" + emisor.getRFC() + folio.getNoFolio() + receptor.getRFC() + "');";
                 s.consulta(sqlFactura);
                 fXML.delete();
                 out.println("Factura generada exitosamente, y se ha enviado al correo:\n " + emisor.getCorreo());
                 
                 //VARIABLES AUXILIARES PARA CONSULTAS SQL
                 idFolioPDF = idFolio;
                 idUsuario = aux;
 
             } catch (InstantiationException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 out.println(ex);
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SecurityException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (UnsupportedEncodingException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (NoSuchProviderException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (Exception ex){
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         } else if ("Impresa".equals(request.getParameter("Factura"))) {
             try {
                 //OBTENCIÓN DEL XML DESDE LA BASE DE DATOS
                 String sqlPDF = "select nombreXML, facturaXML from factura where idFolio=" + idFolioPDF + " and idUsuario=" + idUsuario + ");";
                 Sql s = new Sql();
                 ResultSet rsPDF = s.consulta(sqlPDF);
                 rsPDF.next();
                 Blob blob = (Blob) rsPDF.getBlob("facturaXML");
                 InputStream is = blob.getBinaryStream();
                 File xml = new File(rsPDF.getString("nombreXML") + ".xml");
                 FileOutputStream fos = new FileOutputStream(xml);
                 int b = 0;
                 while ((b = is.read()) != -1) {
                     fos.write(b);
                 }
                 
                 //GENERACIÓN DEL PDF (FACTURA IMPRESA) PARA VISUALIZARLO EN LA PAGINA WEB
                 File pdf = PDF.generarArchivoPDF(xml, "/ISFE-20110020/resources/xslt/", rsPDF.getString("nombreXML") + ".pdf");
                 PDF.visualizarPDF(pdf, response, request);
             } catch (InstantiationException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (SQLException ex) {
                 out.println(ex);
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (FOPException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (TransformerConfigurationException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             } catch (TransformerException ex) {
                 Logger.getLogger(Factura.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Short description";
     }// </editor-fold>
 }
