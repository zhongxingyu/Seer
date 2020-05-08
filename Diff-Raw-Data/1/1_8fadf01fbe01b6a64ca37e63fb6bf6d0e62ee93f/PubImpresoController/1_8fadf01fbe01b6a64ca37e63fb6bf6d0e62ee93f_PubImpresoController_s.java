 package com.vi.clasificados.publicacion.controller;
 
 import com.vi.clasificados.dominio.Clasificado; 
 import com.vi.clasificados.dominio.DetallePrecioClasificado;
 import com.vi.clasificados.dominio.Pedido;
 import com.vi.clasificados.dominio.TipoClasificado;
 import com.vi.clasificados.services.PedidoService;
 import com.vi.clasificados.services.PublicacionService;
 import com.vi.clasificados.services.TipoClasificadoService;
 import com.vi.comun.util.FechaUtils;
 import com.vi.comun.util.Log;
 import com.vi.locator.ComboLocator;
 import com.vi.util.FacesUtil;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 /**
  * @author Jerson Viveros
  */
 @ManagedBean(name="pubImpresoController")
 @SessionScoped
 public class PubImpresoController {
     //Objetos para procesarImpreso la información del clasificado
     private List<DetallePrecioClasificado> detallePrecio;
     private Clasificado clasificado;
     private TipoClasificado tipoClasificado;
     private Pedido pedido;
     
     //Objetos para guardar información de listas desplegables
     private List<SelectItem> tipos;
     private List<SelectItem> subtipos1;
     private List<SelectItem> subtipos2;
     private List<SelectItem> subtipos3;
     private List<SelectItem> subtipos4;
     private List<SelectItem> subtipos5;
     private List<SelectItem> entidades;
     private List<SelectItem> monedas;
     private List<SelectItem> subtiposPublicacion;
     
     //Objetos para los titulos de los subtipos de cada tipo
     private String nsubtipo1;
     private String nsubtipo2;
     private String nsubtipo3;
     private String nsubtipo4;
     private String nsubtipo5;
     
     //Variables de control permiten hacer un despliegue dinámico y precargar en memoria todos los subtipos, para que sean rapidamente accedidos
     //<TIPO,<SUBTIPO,List<Subtipos>>>, por cada tipo hay varios subtipos
     Map<Integer, Map<Integer, List<TipoClasificado>>> mapaSubtipos;
     Map<Integer, TipoClasificado> mapaTipos;
     private boolean modoEdicion = false;
     private Date minDate;
     
     //Servicios
     @EJB
     TipoClasificadoService tipoService;
     @EJB
     PublicacionService publicacionService;
     @EJB
     PedidoService pedidoService;
     
     //Otros objetos necesarios
     ComboLocator comboLocator;
     
     @PostConstruct
     public void init(){
         comboLocator = ComboLocator.getInstance();
         clasificado = new Clasificado();
         mapaTipos = tipoService.getTipos();
         mapaSubtipos = tipoService.getSubtipos();
         seleccionarSubtipos(clasificado.getTipo().getId());
         tipos = FacesUtil.getSelectsItem(mapaTipos);
         subtiposPublicacion = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.COMB_ID_TIPOPUBIMP));
         pedido = new Pedido(FacesUtil.getUsuario());
         minDate = FechaUtils.getFechaMasPeriodo(new Date(), 2, Calendar.DATE);
         clasificado.setFechaIni(minDate);
         entidades = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.COMB_ID_ENTIDAD));
         monedas = FacesUtil.getSelectsItem(comboLocator.getDataForCombo(ComboLocator.COMB_ID_MONEDAS));
     }
     
     public void iniciarPedido(){
         pedido = new Pedido(FacesUtil.getUsuario());
         pedido.setTipoPedido("CLASIFICADOS IMPRESOS");
         iniciarClasificado();
     }
     
     public void iniciarClasificado(){
         modoEdicion=false;
         int idTipo = clasificado.getTipo().getId();
         clasificado = new Clasificado();
         clasificado.setTipo(new TipoClasificado(idTipo));
         seleccionarSubtipos(idTipo);
     }
     
     //Eventos desde la página pubimpreso.xhtml
     public void cambiarTipo(ValueChangeEvent event) {
         Integer idTipo = (Integer) event.getNewValue();
         seleccionarSubtipos(idTipo);
     }
     
     /*
     * Descripción: Lógica de la vista, para que el usuario  cambie la interfaz 
     *              de ingreso del clasificado de acuerdo al tipo que seleccione
     *              ( VIVIENDA, EMPLEO, VEHICULOS o VARIOS )
     */
     public void seleccionarSubtipos(int idTipo) {
         tipoClasificado = mapaTipos.get(idTipo);
         Map<Integer, List<TipoClasificado>> subtipos = mapaSubtipos.get(idTipo); 
         Set<Integer> nSubs = subtipos.keySet();
         clasificado.resetSubtipos();
         for(Integer subtipo : nSubs){
             switch(subtipo){
                 case 1:
                     nsubtipo1 = subtipos.get(subtipo).get(0).getNombre();
                     subtipos1 = FacesUtil.getSelectsItem(subtipos.get(subtipo));
                     clasificado.setSubtipo1(new TipoClasificado());
                     break;
                 case 2:
                     nsubtipo2 = subtipos.get(subtipo).get(0).getNombre();
                     subtipos2 = FacesUtil.getSelectsItem(subtipos.get(subtipo));
                     clasificado.setSubtipo2(new TipoClasificado());
                     break;
                 case 3:
                     nsubtipo3 = subtipos.get(subtipo).get(0).getNombre();
                     subtipos3 = FacesUtil.getSelectsItem(subtipos.get(subtipo));
                     clasificado.setSubtipo3(new TipoClasificado());
                     break;
                 case 4:
                     nsubtipo4 = subtipos.get(subtipo).get(0).getNombre();
                     subtipos4 = FacesUtil.getSelectsItem(subtipos.get(subtipo));
                     clasificado.setSubtipo4(new TipoClasificado());
                     break;
                 case 5:
                     nsubtipo5 = subtipos.get(subtipo).get(0).getNombre();
                     subtipos5 = FacesUtil.getSelectsItem(subtipos.get(subtipo));
                     clasificado.setSubtipo5(new TipoClasificado());
                     break;
             }
         }
     }
     
     public String procesar(){
         try {
             publicacionService.procesarImpreso(clasificado);
             if(!modoEdicion){
                 pedido.getClasificados().add(clasificado);
             }
             pedido.setValorTotal(publicacionService.calcularTotalPedido(pedido.getClasificados()));
             iniciarClasificado();
         }catch (Exception e) {
             FacesUtil.addMessage(FacesUtil.ERROR, "Error al procesar el clasificado");
             Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
         }
         return "/publicacion/pedidoimpreso.xhtml";
     }
     
     //Eventos de la página pedidoimpreso.xhtml
     public String verDetalle(Clasificado clasificado){
         this.detallePrecio = clasificado.getDetallePrecio();
         return "/publicacion/detalleprecio.xhtml";
     }
     
     public String verPedido(){
         return "/publicacion/pedidoimpreso.xhtml";
     }
     
     public String editar(Clasificado clasificado){
         seleccionarSubtipos(clasificado.getTipo().getId());
         this.clasificado = clasificado;
         modoEdicion = true;
         return "/publicacion/pubimpreso.xhtml";
     }
     
     public String crearNuevo(){
         iniciarClasificado();
         return "/publicacion/pubimpreso.xhtml";
     }
     
     public String borrar(Clasificado clasificado){
         for(int i = 0; i < pedido.getClasificados().size() ; i++){
             Clasificado c = pedido.getClasificados().get(i);
             if(c.getSubtipoPublicacion().equals(clasificado.getSubtipoPublicacion()) 
                     && c.getClasificado().equals(clasificado.getClasificado()) 
                     && c.getFechaIni().equals(clasificado.getFechaIni())
                     && c.getFechaFin().equals(clasificado.getFechaFin())
                     && c.getTipo().equals(clasificado.getTipo())){
                 //Es preferible hacer esto a guardar en base de datos antes de que el usuario este seguro de enviar todo el pedido
                 pedido.getClasificados().remove(i);
                 break;
             }
         }
         pedido.setValorTotal(publicacionService.calcularTotalPedido(pedido.getClasificados()));
         return null;
     }
     
     public String guardarPedido(){
          try {
             pedido = pedidoService.guardarPedido(pedido);
             FacesUtil.addMessage(FacesUtil.INFO, pedido.getMensajePago());
             iniciarPedido();
         }catch (Exception e) {
             FacesUtil.addMessage(FacesUtil.ERROR, "Error al procesar el clasificado");
             Log.getLogger().log(Level.SEVERE, e.getMessage(), e);
         }
         return "/publicacion/informacion.xhtml";
     }
     
     //Eventos desde la página mis_clasificados.xhtml
     public String agregarClasificadoVencidoAPedido(Clasificado clasificado) {
         clasificado.setPedido(pedido);
         clasificado.setFechaIni(FechaUtils.getFechaMasPeriodo(new Date(), 2, Calendar.DATE));
         if(clasificado.getFechaFin().before(clasificado.getFechaIni())){
            clasificado.setFechaFin(clasificado.getFechaIni()); 
         }
         this.clasificado = clasificado;
         procesar();
         return "/publicacion/pedidoimpreso.xhtml";
     }
     
     //Eventos desde la página tipoCla.xhtml
     public String redirectPubImpreso(){
         return "/publicacion/pubimpreso.xhtml";
     }
     
     
     //------------------------------------------------ATRIBUTOS-----------------------------------------------------------------------------------
     public Clasificado getClasificado() {
         return clasificado;
     }
 
     public void setClasificado(Clasificado clasificado) {
         this.clasificado = clasificado;
     }
 
     public List<SelectItem> getTipos() {
         return tipos;
     }
 
     public List<SelectItem> getSubtipos1() {
         return subtipos1;
     }
 
     public List<SelectItem> getSubtipos2() {
         return subtipos2;
     }
 
     public List<SelectItem> getSubtipos3() {
         return subtipos3;
     }
 
     public List<SelectItem> getSubtipos4() {
         return subtipos4;
     }
 
     public List<SelectItem> getSubtipos5() {
         return subtipos5;
     }
 
     public TipoClasificado getTipoClasificado() {
         return tipoClasificado;
     }
 
     public String getNsubtipo1() {
         return nsubtipo1;
     }
 
     public String getNsubtipo2() {
         return nsubtipo2;
     }
 
     public String getNsubtipo3() {
         return nsubtipo3;
     }
 
     public String getNsubtipo4() {
         return nsubtipo4;
     }
 
     public String getNsubtipo5() {
         return nsubtipo5;
     }
 
     public List<SelectItem> getSubtiposPublicacion() {
         return subtiposPublicacion;
     }
 
     public Pedido getPedido() {
         return pedido;
     }
 
     public void setPedido(Pedido clasificadosPedido) {
         this.pedido = clasificadosPedido;
     }
 
     public Date getMinDate() {
         return minDate;
     }
 
     public boolean isModoEdicion() {
         return modoEdicion;
     }
 
     public List<SelectItem> getEntidades() {
         return entidades;
     }
 
     public List<SelectItem> getMonedas() {
         return monedas;
     }
 
     public List<DetallePrecioClasificado> getDetallePrecio() {
         return detallePrecio;
     }  
 }
