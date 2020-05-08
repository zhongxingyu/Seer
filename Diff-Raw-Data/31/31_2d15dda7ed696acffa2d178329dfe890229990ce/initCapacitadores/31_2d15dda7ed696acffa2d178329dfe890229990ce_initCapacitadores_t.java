 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dwrScripts;
 
 import daoImpl.*;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.*;
 import pojo.*;
 
 /**
  *
  * @author Dave
  */
 public class initCapacitadores {
 
     validate validar;
 
     public initCapacitadores() {
     }
 
     public String htennws() {
         return "test exitoso";
     }
 
     public String obtenerURLSist() throws IOException {
         String url;
         Properties archivoConf = new Properties();
         archivoConf.load(this.getClass().getClassLoader().getResourceAsStream("/micelanea.properties"));
         url = (String) archivoConf.getProperty("seceUrl");
         return url;
     }
 
     public String insertarVideoChatCapac(int idcontrato, int idusuario) {
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contrato = cdi.findById(idcontrato);
         String idusuario1 = null, idusuario2 = null, idusuario3 = null, idusuario4 = null;
 
 
         DelegacionIndiUsuDaoImpl diud = new DelegacionIndiUsuDaoImpl();
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         Usuario user = udi.findById(idusuario);
         String nombreUsuario = user.getNombre();
         List<Usuario> usuarios = diud.usuariosSNRepetirdelegacionxContrato(contrato, user);
         System.out.println(usuarios.size());
         try {
             idusuario1 = String.valueOf(usuarios.get(0).getIdUsuario());
             idusuario2 = String.valueOf(usuarios.get(1).getIdUsuario());
             idusuario3 = String.valueOf(usuarios.get(2).getIdUsuario());
             idusuario4 = String.valueOf(usuarios.get(3).getIdUsuario());
         } catch (IndexOutOfBoundsException ie) {
             System.out.println(ie.getMessage());
         }
 
 
         String obj = "<object id='Video_Chat' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0' "
                 + "width='990' height='625'> <param name='movie' value='resources/swf/publicador.swf?idcontrato=" + idcontrato + "&iduser=" + idusuario + "&iduser1=" + idusuario1 + "&iduser2=" + idusuario2 + "&iduser3=" + idusuario3 + "&iduser4=" + idusuario4 + "&nomUser=" + nombreUsuario + "'/>"
                 + " <param name='quality' value='high'/> <embed src='resources/swf/publicador.swf?idcontrato=" + idcontrato + "&iduser=" + idusuario + "&iduser1=" + idusuario1 + "&iduser2=" + idusuario2 + "&iduser3=" + idusuario3 + "&iduser4=" + idusuario4 + "&nomUser=" + nombreUsuario + "' "
                 + "quality='high' pluginspage='http://www.macromedia.com/go/getflashplayer' type='application/x-shockwave-flash' width='1000' height='625'></embed> </object>";
         return obj;
     }
 
     public String insertarVideoChat(int idcontrato, int idusuario) {
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contrato = cdi.findById(idcontrato);
         String idusuario1 = null, idusuario2 = null, idusuario3 = null;
         String idcapacitador = null;
 
         AsignacionCapaContraDaoImpl accdi = new AsignacionCapaContraDaoImpl();
         Usuario capacitador = accdi.findUsByContra(contrato);
         idcapacitador = String.valueOf(capacitador.getIdUsuario());
 
         DelegacionIndiUsuDaoImpl diud = new DelegacionIndiUsuDaoImpl();
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         Usuario user = udi.findById(idusuario);
         String nombreUsuario = user.getNombre();
         Empresa empresa = user.getEmpresa();
         List<Usuario> usuarios = diud.usuariosSNRepetirdelegacionxContrato(contrato, user);
         try {
             idusuario1 = String.valueOf(usuarios.get(0).getIdUsuario());
             idusuario2 = String.valueOf(usuarios.get(1).getIdUsuario());
             idusuario3 = String.valueOf(usuarios.get(2).getIdUsuario());
         } catch (IndexOutOfBoundsException ie) {
             System.out.println(ie.getMessage());
         }
 
 
         String obj = "<object id='Video_Chat' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=6,0,29,0' "
                 + "width='990' height='625'> <param name='movie' value='resources/swf/publicador.swf?idcontrato=" + idcontrato + "&iduser=" + idusuario + "&iduser1=" + idcapacitador + "&iduser2=" + idusuario1 + "&iduser3=" + idusuario2 + "&iduser4=" + idusuario3 + "&nomUser=" + nombreUsuario + "'/>"
                 + " <param name='quality' value='high'/> <embed src='resources/swf/publicador.swf?idcontrato=" + idcontrato + "&iduser=" + idusuario + "&iduser1=" + idcapacitador + "&iduser2=" + idusuario1 + "&iduser3=" + idusuario2 + "&iduser4=" + idusuario3 + "&nomUser=" + nombreUsuario + "' "
                 + "quality='high' pluginspage='http://www.macromedia.com/go/getflashplayer' type='application/x-shockwave-flash' width='1000' height='625'></embed> </object>";
         return obj;
     }
 
     public String getDelegadoxContrato(int idcontrato) {
         String resultado = "";
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contra = cdi.findById(idcontrato);
         DelegacionIndiUsuDaoImpl doii = new DelegacionIndiUsuDaoImpl();
         List<DelegacionIndiUsu> delegados = doii.delegacionxContrato(contra);
         DelegacionIndiUsu delegado;
         Iterator<DelegacionIndiUsu> it = delegados.iterator();
         Calendar c = Calendar.getInstance();
         Date tiempoDgracia;
 
         AccesoDaoImpl adi = new AccesoDaoImpl();
         //var para ultimo acceso del usaurio
         Date acces;
         EmailInactividadDaoImpl eidi = new EmailInactividadDaoImpl();
         EmailInactividad emi;
         int intentos = 0;
 
         if (it.hasNext()) {
             resultado = "<table border='0' id='resultTable' width ='100%'><tr> <th>Delegado</th>  <th>Ultimo Acceso</th> <th>Indicador</th> <th>Correo</th> <th>On-Line</th> </tr>";
 
             while (it.hasNext()) {
                 delegado = it.next();
 
                 try {
                     intentos = eidi.intentosEmailInactividadxUsuario(delegado.getUsuario());
                 } //captura si no hay registros de este cliente en la tabla email_inactividad
                 catch (java.lang.NullPointerException ne) {
                     /*si no hay registro de intento de este usuario crea uno con intentos 0*/
                     emi = new EmailInactividad(delegado.getUsuario(), 0);
                     eidi.create(emi);
                 }
                 //metodo que devuelve ultim acceso del usuario tal
                 acces = adi.fechaUltimoAcceso(delegado.getUsuario());
                 //determinar tiempo de inactividad si acceso no es nulo
                 if (acces != null) {
                     c.setTime(acces);
                     c.add(Calendar.DATE, 3);
                     tiempoDgracia = c.getTime();
                    if (new Date().after(tiempoDgracia)){ 
                        resultado += "<tr style='text-align:center;background-color:#FFF6B3;'> <input type='hidden' value='" + delegado.getUsuario().getIdUsuario() + "'/>"                                
                                  + " <td align='left' style='text-align:left;padding-left:3%;'><button class='btnSentEmail'>enviar correo</button><span class='cant_emailEnviados'>" + intentos + "</span>&nbsp;&nbsp;&nbsp;&nbsp;" + delegado.getUsuario().getNombre() + "</td>";                                                     
                        resultado += "<td class='tdFechaAcc'>";  
                        resultado += acces;
                        resultado += "</td>";
                    }else{
                        resultado += "<tr style='text-align:center;'> <td>" + delegado.getUsuario().getNombre() + "</td>";                                                     
                        resultado += "<td class='tdFechaAcc'>";  
                        resultado += acces;
                        resultado += "</td>";
                    }
                }else if(acces == null) {  
                    resultado += "<tr style='text-align:center;'> <td>" + delegado.getUsuario().getNombre() + "</td>";  
                    resultado += "<td class='tdFechaAcc'>";
                     resultado += "(No hay registros de accesos)";
                     resultado += "</td>";
                 }
                 resultado += "<td>" + delegado.getIndicador().getNombre() + "</td>";
 
                 resultado += "<td>" + delegado.getUsuario().getCorreo() + "</td>";
 
                 resultado += "<td>";
                 if (delegado.getUsuario().getEstado() == 1) {
                     resultado += "<img src='images/offline-user-icon.png' title ='Usuario no Conectado' alt='Usuario no Conectado'/>";
                 } else if (delegado.getUsuario().getEstado() == 2) {
                     resultado += "<img src='images/user-online-icon.png' title ='Usuario Conectado' alt='Usuario Conectado'/>";
                 } else if (delegado.getUsuario().getEstado() == 3) {
                     resultado += "<img src='images/video-chat-icon.png' title ='Usuario en Videollamada' alt='Usuario en Videollamada'/>";
                 } else if (delegado.getUsuario().getEstado() == 4) {
                     resultado += "<img src='images/remove-user-icon.png' title ='Usuario dado de baja' alt='Usuario dado de baja'/>";
                 }
                 resultado += "</td></tr>";
 
             }//fin while
             resultado += "</table>";
         }//fin if
 
         return resultado;
     }
 
     public String empresaNoAsignada(int idempresa) {
         String resultado = "";
         EmpresaDaoImpl empi = new EmpresaDaoImpl();
         Empresa emp = empi.findByID(idempresa);
         resultado = "<table border='0' width ='100%'>"
                 + "<tr><th width='505px'>Direcci&oacute;n</th><th width='259px'>Tel&eacute;fono</th><th width='184px'>Acci&oacute;n</th></tr>"
                 + "<tr style='text-align:center;'>"
                 + "<td>" + emp.getDireccion() + "</td>"
                 + "<td>" + emp.getTelefono() + "</td>"
                 + "<td> <button id='btnImgAprobar' class='btnAprobar'>Aprobar</button>  <button id='btnImgEliminar' class='btnAprobar'>Rechazar</button> </td>"
                 + "</tr></table>";
         return resultado;
     }
 
     public String getListaENA(int idCapacitador) {
         String resultado = "";
 
         Iterator<AsignacionCapaContra> it = listaConxCapac(idCapacitador);
 
         AsignacionCapaContra acaco;
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contra;
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         EmpresaDaoImpl edi = new EmpresaDaoImpl();
         Empresa emp;
         Usuario contacto;
 
         while (it.hasNext()) {
             acaco = it.next();
 
             //estado del contrato activo, contrato de esta empresa actualmente trabajandose,                                    
             if (acaco.getContrato().getEstado() == 0) {
                 contra = cdi.findById(acaco.getContrato().getIdContrato());
                 contacto = udi.findById(contra.getUsuario().getIdUsuario());
                 emp = edi.findByID(contacto.getEmpresa().getIdEmpresa());
                 resultado += "<tr height = '35px'>"
                         + " <td id ='tdEmpresa' style='display:none;'>" + emp.getIdEmpresa() + "</td>"
                         + " <td> " + emp.getNombre() + " </td>"
                         + " <td> " + contacto.getNombre() + " </td>"
                         + " <td> " + contra.getUsuario().getCorreo() + " </td>"
                         + "<td>";
                 if (contra.getFechaInicio() == null) {
                     resultado += "(No hay Registros)";
                 } else {
                     resultado += contra.getFechaInicio();
                 }
                 resultado += "</td>"
                         + "<td id ='tdContrato' style='display:none;'>" + acaco.getContrato().getIdContrato() + "</td>"
                         + "<td id ='tdContacto' style='display:none;'>" + contra.getUsuario().getIdUsuario() + "</td>"
                         + "</tr>";
             }//endif
         }//finif finwhile           
         return resultado;
     }
 
     private Iterator<AsignacionCapaContra> listaConxCapac(int idCapacitador) {
         AsignacionCapaContraDaoImpl a = new AsignacionCapaContraDaoImpl();
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         Usuario capacitador = udi.findById(idCapacitador);
         List<AsignacionCapaContra> l = a.findAllByIdUsuarioCapacitador(capacitador);
         Iterator<AsignacionCapaContra> it = l.iterator();
         return it;
     }
 
     public int AvancePorcXcontrato(Contrato contra) {
         ItemDaoImpl idi = new ItemDaoImpl();
         RespItemDaoImpl ridi = new RespItemDaoImpl();
         int respxContrato = ridi.ItemRespondidosxContrato(contra);
         //Numero de Items activos
         int numItems = idi.numItemActivos();
 
         return ((respxContrato * 100) / numItems);
     }
 
     public void quitarConFin(int idCapacitador, int diasGracia) {
         //funcion que revisa la fecha de finalizacion del contrato, despues de los "dias de gracia" ejecuta el cambio de estado
         //---ojo--- inecesario si el estado es cambiajo automaticamente despues de respondida el ultimo item
         Iterator<AsignacionCapaContra> it = listaConxCapac(idCapacitador);
         AsignacionCapaContra aco;
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contrato;
         Date finContrato;
         Date tiempoDgracia;
         Calendar c = Calendar.getInstance();
         while (it.hasNext()) {
             aco = it.next();
             //if que filtra los contratos vigentes
             if (AvancePorcXcontrato(aco.getContrato()) == 100) {
                 contrato = cdi.findById(aco.getContrato().getIdContrato());
                 finContrato = contrato.getFechaTermino();
                 c.setTime(finContrato);
                 c.add(Calendar.DATE, diasGracia);
                 tiempoDgracia = c.getTime();
                 if (new Date().after(tiempoDgracia)) {
                     contrato.setEstado(2);//actualiza el estado del contrato a actualizado            
                     cdi.update(contrato);
                 }//fin if new Date
             }//fin if 
         }//fin while
 
     }//fin metodo
 
     public String getEmpreContratoFin(int idCapacitador) {
         Iterator<AsignacionCapaContra> it = listaConxCapac(idCapacitador);
         AsignacionCapaContra aco;
         EmpresaDaoImpl edi = new EmpresaDaoImpl();
         Empresa emp = new Empresa();
         String resultado = "", nombre = "", fechaFin = "";
         while (it.hasNext()) {
             aco = it.next();
             if (aco.getContrato().getEstado() == 2) {
                 emp = edi.findByID(aco.getContrato().getUsuario().getEmpresa().getIdEmpresa());
                 nombre = emp.getNombre();
                 if (aco.getContrato().getFechaTermino() == null) {
                     fechaFin = "(No hay registros)";
                 } else {
                     fechaFin = String.valueOf(aco.getContrato().getFechaTermino());
                 }
                 resultado += "<tr> <input type='hidden' value=" + aco.getContrato().getIdContrato() + " />  <td height='35'> " + nombre + " </td> <td>" + fechaFin + " </td> </tr>";
             }//fin if estado
         }//fin while
         return resultado;
     }//fin funcion    
 
     public void enviarCorreoUsuarioDormido(int idcontrato, int idusuario, String ultimoAcceso) throws IOException {
 
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contra = cdi.findById(idcontrato);
 
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         Usuario user = udi.findById(idusuario);
         String nombreUsuario = user.getNombre().toUpperCase();
         String correo = user.getCorreo();
         validar = new validate();
 
         String asunto = "Usuario Inactivo";
         String cuerpo = " <p>Estimado " + nombreUsuario + " se ha detectado <strong>inactividad</strong> en su cuenta del Sistema SECE.</br>"
                 + "Su ultimo acceso al sistema fue el " + ultimoAcceso + ".</p>"
                 + "Le invitamos a continuar de respondiendo la evaluaci&oacute;n en la brevedad de lo posible.</p>"
                 + "Por favor no reinvie a este correo.<p><strong>Gracias SECE TEAM.</strong></p>";
 
         validar.EnviarCorreo("sece@pml.org.ni", correo, asunto, cuerpo);
     }//fin idContrato
 
     public String getHistorialContratosFinalizados(int idCapacitador) {
         String resultado = "";
         int numeroContratosFinalizados = 0, idEmpresa = 0;
         AsignacionCapaContra acaco;
         Iterator<AsignacionCapaContra> it = listaConxCapac(idCapacitador);
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contra = new Contrato();
 
         while (it.hasNext()) {
             acaco = it.next();
             numeroContratosFinalizados = cdi.cantidadContratosxUsuarioFinalizados(acaco.getContrato().getUsuario());
             if (numeroContratosFinalizados != 0) {
                 contra = cdi.findById(acaco.getContrato().getIdContrato());
                 idEmpresa = contra.getUsuario().getEmpresa().getIdEmpresa();
                 resultado += "<tr> <input type='hidden' value='" + idEmpresa + "'/>";
                 resultado += "<td height='35px' style='cursor:pointer;'>" + contra.getUsuario().getEmpresa().getNombre() + "</td>";
                 resultado += "<td style='text-align: center;'>" + numeroContratosFinalizados + "</td>";
                 resultado += "</tr>";
             }//fin if
         }//fin while
         return resultado;
     }//fin funcion getHistorialContratosFinalizados
 
     public void actualizarCantCorreosEnviados(int idUsuario, int cant_intentos) {
         EmailInactividadDaoImpl eidi = new EmailInactividadDaoImpl();
         EmailInactividad ei = new EmailInactividad();
         UsuarioDaoImpl udi = new UsuarioDaoImpl();
         Usuario user = udi.findById(idUsuario);
         //busca EmailInactividad por usuario
         ei = eidi.findByUsuario(user);
         //actualiza los intentos
         ei.setIntentos(cant_intentos);
         eidi.update(ei);
     }//fin metodo   
 
     //metodo que resetea los intentos en la tabla email inactividad
     public void reiniciarConteoEmails(Usuario usuario) {
         EmailInactividadDaoImpl eidi = new EmailInactividadDaoImpl();
         try {
             EmailInactividad emi = eidi.findByUsuario(usuario);
             emi.setIntentos(0);
             eidi.update(emi);
         } catch (NullPointerException ne) {
             System.out.println(ne.getMessage());
         }
     }//fin metodo
 
     public String testear(int idcontrato) {
         String resultado = "";
         double vs, v, vce;
 
         AvanceDaoImpl adi = new AvanceDaoImpl();
         Avance avance;
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         Contrato contra = cdi.findById(idcontrato);
         List<Avance> resultAvance = adi.Xcontrato(contra);
         Iterator<Avance> it = resultAvance.iterator();
         try {
             while (it.hasNext()) {
                 avance = it.next();
                 v = avance.getV();
                 vs = avance.getVs();
                 vce = (vs / v) * 100;
                 long vceR = Math.round(vce);
                 resultado += "<h3 style='float: left;'>" + avance.getIndicador().getNombre() + "</h3> <div align='left' class='progress-bar blue small' style='width:30%;height: 35px;'><div style='width:" + vceR + "%" + ";height: 35px;'><span style='top:26%;font-size: 15px;'>" + vceR + "%" + "</span></div> </div>";
                 //System.out.println(vce);
             }
         } catch (Exception ex) {
             ex.getMessage();
         }
 
         return resultado;
     }//fin funcion
 
     public String getVce(int idcontrato) {
         AvanceDaoImpl adi = new AvanceDaoImpl();
         Avance avance = new Avance();
         ContratoDaoImpl cdi = new ContratoDaoImpl();
         List<Avance> resultAvance = new ArrayList<Avance>();
         double vs = 0.0, v = 0.0, vce = 0.0;
         DecimalFormat df = new DecimalFormat("##.##");
         Contrato contra = cdi.findById(idcontrato);
         resultAvance = adi.Xcontrato(contra);
         Iterator<Avance> it = resultAvance.iterator();
         while (it.hasNext()) {
             avance = it.next();
             vs += avance.getVs();
             v += avance.getV();
         }//fin while
         vce = (vs / v) * 100;
         return df.format(vce);
     }
 }
