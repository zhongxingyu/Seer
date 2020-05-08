 package controllers.websockets;
 
import models.*;
 import org.json.JSONObject;
 import play.Logger;
 import play.db.jpa.JPA;
 import play.mvc.Http;
 import play.mvc.WebSocketController;
 
 import javax.persistence.EntityTransaction;
 import java.math.BigDecimal;
 import java.sql.Date;
 
 /**
  * Proyecto Omoikane: SmartPOS 2.0
  * User: octavioruizcastillo
  * Date: 13/05/12
  * Time: 16:33
  */
 public class ClientController {
     private Http.Outbound outbound;
 
     public ClientController(Http.Outbound outbound) {
         this.outbound = outbound;
     }
 
     public boolean mensajeEntrante(JSONObject message) {
 
             Logger.info("Mensaje recibido");
             EntityTransaction et = VentaPorDia.em().getTransaction();
 
             try {
                 ClientMessage msg = new ClientMessage(message);
                 if( msg.command == ClientMessage.Command.setVentas ) {
                     setVentas(et, msg);
                 }
                 if( msg.command == ClientMessage.Command.setProductos) {
                     setProductos(et, msg);
                 }
                 if( msg.command == ClientMessage.Command.ping ) {
                     JSONObject pong = new JSONObject();
                     pong.put("command", "pong");
                     outbound.send(pong.toString());
                    Sucursal actual = ((Sucursal)Sucursal.findById(msg.idSucursal));
                    actual.estado = EstadoSucursal.ONLINE;
                    actual.save();
                 }
 
             } catch (Exception e) {
                 Logger.error("Error extrayendo información de JSON", e);
                 e.printStackTrace();
             }
 
             // Continue Listening?
             return true;
         }
 
         private synchronized void setProductos(EntityTransaction et, ClientMessage msg) {
             if(!JPA.em().getTransaction().isActive()) { JPA.em().getTransaction().begin(); }
             Sucursal sucursal = Sucursal.findById(msg.idSucursal);
 
             for (Producto producto : msg.getProductos()) {
                 ProductoId productoId = new ProductoId();
                 productoId.id = producto.id;
                 productoId.sucursalId = producto.sucursalId;
 
                 Producto productoToSave = Producto.findById(productoId);
                 if(productoToSave != null) {
                     productoToSave.precio      = producto.precio;
                     productoToSave.costo       = producto.costo;
                     productoToSave.codigo      = producto.codigo;
                     productoToSave.existencias = producto.existencias;
                     productoToSave.nombre      = producto.nombre;
                     productoToSave.utilidad    = producto.utilidad;
 
                 } else {
                     productoToSave = producto;
                 }
                 producto.sucursal = sucursal;
                 System.out.println("Guardando producto: " + productoToSave + ",idsuc:" + productoToSave.sucursalId);
                 productoToSave.save();
 
             }
             JPA.em().flush();
             JPA.em().getTransaction().commit();
         }
 
         private void setVentas(EntityTransaction et, ClientMessage msg) {
             if(!et.isActive()) { et.begin(); }
 
             BigDecimal importe    = msg.getVentaSucursalImporte();
             Date fecha            = msg.getVentaSucursalFecha();
             Sucursal suc          = Sucursal.findById(msg.idSucursal);
 
             VentaPorDia vpd       = VentaPorDia.find("byFechaAndSucursal_id", fecha, suc.id).first();
 
             if(vpd == null)  {
                vpd = new VentaPorDia(fecha, importe);
                vpd.sucursal = suc;
             } else {
               vpd.venta = importe;
             }
             vpd.save();
             et.commit();
             Logger.info("Ventas solicitadas fueron recibidas. S: %s, $: %s", suc.nombre, importe.toString());
         }
 }
