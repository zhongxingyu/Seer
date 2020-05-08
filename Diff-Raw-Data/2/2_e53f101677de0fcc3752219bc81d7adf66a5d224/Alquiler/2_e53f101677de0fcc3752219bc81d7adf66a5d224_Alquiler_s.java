 package alquiler;
 
 import java.math.BigDecimal;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.jdo.annotations.IdGeneratorStrategy;
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 import javax.jdo.annotations.VersionStrategy;
 import org.apache.isis.applib.DomainObjectContainer;
 import org.apache.isis.applib.annotation.Audited;
 import org.apache.isis.applib.annotation.AutoComplete;
 import org.apache.isis.applib.annotation.DescribedAs;
 import org.apache.isis.applib.annotation.Disabled;
 import org.apache.isis.applib.annotation.Hidden;
 import org.apache.isis.applib.annotation.MemberOrder;
 import org.apache.isis.applib.annotation.Named;
 import org.apache.isis.applib.annotation.NotPersisted;
 import org.apache.isis.applib.annotation.ObjectType;
 import org.apache.isis.applib.annotation.Where;
 import org.apache.isis.applib.annotation.MemberGroups;
 import adicional.Adicional;
 import com.google.common.collect.Lists;
 import cliente.Cliente;
 import disponibles.AutoPorFecha;
 
 /**
  * Entidad que representa el Alquiler de Autos
  * Es la clase principal de nuestro Sistema, mediante el cual se puede
  * realizar el alquiler de un auto seleccionado, entre una fecha determinada.
  * 
  * @see adicional.Adicional
  * @see cliente.Cliente
  * @see disponibles.AutoPorFecha
  */
 @javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.APPLICATION)
 @javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
 @MemberGroups({"Estado","Cliente","Datos del Alquiler","Autos","Datos de Factura"})
 @javax.jdo.annotations.Query(name="traerAlquileres", language="JDOQL",value="SELECT FROM alquiler.Alquiler order by numero asc")
 @AutoComplete(repository=AlquilerServicio.class, action="autoComplete")
 @Audited 
 @ObjectType("ALQUILER")
 public class Alquiler {
 	/**
 	 * Identificacion del nombre del icono que aparecera en la UI
 	 * 
 	 * El icono a mostrarse depende del estado de la reserva: 
 	 * CERRADO, EN PROCESO, FINALIZADO o RESERVADO.
 	 * 
 	 */
 	public String iconName(){
 		
 		if(getEstado() == EstadoAlquiler.CERRADO){
 			return "cerrado";
 		}else {
 			if(getEstado() == EstadoAlquiler.EN_PROCESO){
 				return "enproceso";
 			}else{
 				if(getEstado() == EstadoAlquiler.FINALIZADO){
 					return "finalizado";
 				}else
 				{
 					if(getEstado() == EstadoAlquiler.RESERVADO){
 						return "reservado";
 					}else{
 						return "alquiler";
 					}
 				}
 			}
 		} 
 	}
 	/**
 	 * Enumeracion que determina los posibles estados en los cuales pasa un Alquiler.
 	 */
 	public static enum EstadoAlquiler{
 		RESERVADO, EN_PROCESO, FINALIZADO, CERRADO;
 	}	
 	/**
 	 * Enumeracion que determina los posibles medios de pago.
 	 */
 	public static enum TipoPago{
 		EFECTIVO, CHEQUE, TARJETA_CREDITO, TARJETA_DEBITO;
 	}
 	/**
 	 * Titulo que se mostrar&aacute; en la UI. 
 	 * Se mostrar&aacute; el nombre de los Estados.
 	 * @return String
 	 */
     public String title(){
             return getEstado().toString();        
     }
 
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Long numero;
     /**
      * Se retorna el n&uacute;mero del Alquiler.
      * 
      * @return Long 
      */
     @Disabled
     @Named("Nro Alquiler")
     @MemberOrder(name="Datos del Alquiler",sequence="1")
     public Long getNumero() {
             return numero;
     }
     /**
      * Se setea el n&uacute;mero del Alquiler.
      * @param numero
      */
     public void setNumero(final Long numero) {
             this.numero = numero;
     }
    
     private String nombreEstado;
     @Named("Estado")
     @NotPersisted
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Estado",sequence="1")
     public String getNombreEstado() {
             nombreEstado = getEstado().toString();
             return nombreEstado;
     }    
     private EstadoAlquiler estado;    
     
     /**
      * Se retorna el Estado del alquiler 
      * @return EstadoAlquiler
      */
     @Hidden
     public EstadoAlquiler getEstado(){
             return estado;
     }
     /**
      * Se setea el Estado del Alquiler.
      * @param estado
      */
     public void setEstado(final EstadoAlquiler estado){
             this.estado = estado;
     }
     
     /**
      * Fecha en la cual se realiza el alquiler.
      * @return String
      */
     @Named("Fecha")
     @MemberOrder(name="Datos del Alquiler",sequence="2")
     public String getFechaString() {
             if(getFecha() != null) {
                     SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                     return formato.format(getFecha());
             }
             return null;
     }    
     private Date fecha;
     /**
      * Se retorna la fecha del alquiler
      * @return Date  
      */
     @Hidden
     public Date getFecha() {
             return fecha;
     }
     /**
      * Se setea la fecha del Alquiler
      * @param fecha  
      */
     public void setFecha(final Date fecha) {
             this.fecha = fecha;
     }
  
 	private String  precio;
 	/**
 	 * Retorna el precio del Alquiler en Neto y solamente correspondiente al valor
 	 * de la categoria del Auto.
 	 * 
 	 * @return String
 	 */
     @Named("Precio")
     @Disabled
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos del Alquiler",sequence="3")
 	public String getPrecioAlquiler(){
 		return precio;
 	}
     /**
      *  Se setea el precio del Alquiler en Neto.
      *  @param precio
      */
 	public void setPrecioAlquiler(final String precio){
 		this.precio=precio;
 	}	
 	
 	/**
 	 * M&eacute;todo que calcula el subtotal del Alquiler.
 	 * Se calcula el precio por la categoria de cada auto 
 	 * y la cantidad de d&iacute;as. 
 	 * Se realiza una sumatoria de lo antes mencionado.
 	 * 
 	 * @return alquiler.Alquiler
 	 */
 	@Hidden
 	public Alquiler calculoSubTotal(){
 		List<AutoPorFecha> listaAutos=Lists.newArrayList(getAutos());		
 		
 		BigDecimal sum=new BigDecimal("0");		
 		sum.setScale(5, BigDecimal.ROUND_HALF_UP);
 		for (AutoPorFecha auto:listaAutos){
 			BigDecimal aux=new BigDecimal(auto.getCategoria().getPrecio());
 			aux.setScale(5, BigDecimal.ROUND_HALF_UP);
 			sum=sum.add(aux);
 		}		
 		setPrecioAlquiler(sum.toString());
 		return this;
 	}	
 	
 	private TipoPago tipoPago;
 	/**
 	 * Retorna el tipo de pago
 	 * @return TipoPago
 	 */
     @Named("Tipo de Pago")
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="1")
 	public TipoPago getTipoPago() {
 		return tipoPago;
 	}
     /**
 	 * Setea el tipo de pago
 	 * @param tipoPago
 	 */
 	public void setTipoPago(final TipoPago tipoPago) {
 		this.tipoPago = tipoPago;
 	}
 	/**
 	 * M&eacute;todo provisto por el framework que deshabilita 
 	 * la opcion de poder editar el tipo de pago.
 	 * 
 	 * @return String
 	 */
 	public String disableTipoPago(){
 		if (getEstado()==EstadoAlquiler.RESERVADO||getEstado()==EstadoAlquiler.EN_PROCESO){
 			return "El Alquiler debe estar FINALIZADO";
 		}
 		else {
 			return getEstado()==EstadoAlquiler.FINALIZADO? null:"El Alquiler esta CERRADO no se puede editar";
 		}
 	}
 	
 	
 	private int factura;
 	/**
 	 * Retorna el n&uacute;mero de factura
 	 * 
 	 * @return int
 	 */
     @Named("Nro Factura")
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="2")
 	public int getNumeroFactura() {
 		return factura;
 	}
     /**
 	 * Setea el n&uacute;mero de factura
 	 * 
 	 * @param factura
 	 */
 	public void setNumeroFactura(final int factura) {
 		this.factura = factura;
 	}
 	/**
 	 * M&eacute;todo provisto por el framework que deshabilita 
 	 * la opcion de poder editar el numero de factura
 	 * 
 	 * @return String
 	 */
 	public String disableNumeroFactura(){
 		if (getEstado()==EstadoAlquiler.RESERVADO||getEstado()==EstadoAlquiler.EN_PROCESO){
 			return "El Alquiler debe estar FINALIZADO";
 		}
 		else {
 			return getEstado()==EstadoAlquiler.FINALIZADO? null:"El Alquiler esta CERRADO no se puede editar";
 		}
 	}	
  
 	private String precioTot;
 	/**
 	 * Se retorna la sumatoria del precio Neto
 	 *  m&aacute;s los adicionales.
 	 *  
 	 * @see alquiler.Alquiler#getPrecioAlquiler()
 	 * @see alquiler.Alquiler#calculoSubTotal()
 	 * 
 	 * @return String
 	 */
     @Named("Precio Total")
     @Disabled
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="3")
 	public String getPrecioTotal(){
 		return precioTot;
 	}
     /**
      * Se setea el precio total calculado.
      * @param precioTot
      */
 	public void setPrecioTotal(final String precioTot){
 		this.precioTot=precioTot;
 	}	
 	/**
 	 * Se calcula el precio total, que es la sumatoria de los precios de las categorias de los autos,
 	 * mas los adicionales, por la cantidad de dias.
 	 * @return Alquiler
 	 */
 	@Hidden
 	public Alquiler calculoTotal(){
 		List<AutoPorFecha> listaAutos=Lists.newArrayList(getAutos());
 		List<Adicional> listaAdicionales=Lists.newArrayList(getAdicionales());
 		
 		BigDecimal suma=new BigDecimal("0");		
 		suma.setScale(5, BigDecimal.ROUND_HALF_UP);
 		
 		BigDecimal aux=new BigDecimal("0");		
 		aux.setScale(5, BigDecimal.ROUND_HALF_UP);
 		
 		BigDecimal lista=new BigDecimal(listaAutos.size());		
 		lista.setScale(5, BigDecimal.ROUND_HALF_UP);
 		
 		for (Adicional adic:listaAdicionales){
 			BigDecimal pAdic=new BigDecimal(adic.getPrecio());		
 			pAdic.setScale(5, BigDecimal.ROUND_HALF_UP);			
 			
 			aux= pAdic.multiply(lista);			
 			suma=suma.add(aux);
 		}
 		BigDecimal total=new BigDecimal(listaAutos.size());		
 		total.setScale(5, BigDecimal.ROUND_HALF_UP);
 		
 		BigDecimal precioAlq=new BigDecimal(getPrecioAlquiler());		
 		precioAlq.setScale(5, BigDecimal.ROUND_HALF_UP);
 		
 		total= precioAlq.add(suma);
 		setPrecioTotal(total.toString());
 		
 		return this;		
 	}	
 	/**
 	 * 
 	 * Relacion bidireccional entre Autos y Alquiler.
 	 * Se genera una lista de Autos, entre fechas determinadas.
 	 * 
 	 * @see autos.Auto
 	 * 
 	 * @return List<disponibles.AutoPorFecha>
 	 */
 	@Persistent(mappedBy="alquiler")		
 	private List<AutoPorFecha> autos = new ArrayList<AutoPorFecha>();
     public List<AutoPorFecha> getAutos() {
         return autos;
     }
     /**
      * Se Setea una lista de autos, entre una fecha determinada.
      */
     public void setAutos(List<AutoPorFecha> listaAutos) {
     	this.autos = listaAutos;
     }
     /**
      * Accion que borra el Alquiler especificado.
      * Provisto por el Framework.
      * @param auto
      */
     @Named("Borrar")
     @MemberOrder(name="Autos",sequence="1")
     public Alquiler removeFromAutos(final AutoPorFecha auto) {
             autos.remove(auto);
             container.removeIfNotAlready(auto);
             calculoSubTotal();
             calculoTotal();
             return this;            
     }
     /**
      * Accion provista por el Framework.
      * Deshabilita la opcion de borrar un elemento de la lista de Autos.
      * 
      * @param auto
      * @return String
      */
     public String disableRemoveFromAutos(AutoPorFecha auto){
         if(getEstado() == EstadoAlquiler.RESERVADO || getEstado() == EstadoAlquiler.EN_PROCESO) {
        	return autos.size()>0? null: "No existe Autos para este Alquiler";
         }
         else return getEstado()==EstadoAlquiler.FINALIZADO? "El Alquiler esta FINALIZADO no se puede editar":"El Alquiler esta CERRADO no se puede editar";          			          
     }
     /**
      * Accion que agrega los autos a la lista.
      * @param auto
      */
     @Hidden
     public void addToAutos(AutoPorFecha auto) {
     	if(auto == null || autos.contains(auto)) {
              return;
     	}
     	auto.setAlquiler(this);
     	autos.add(auto);
     	calculoSubTotal();
     	calculoTotal();
     }
     /**
      * Choices provisto por el Framework, 
      * que habilita una serie de opciones para una Propiedad/Accion.
      * Choices para la accion {@link alquiler.Alquiler#removeFromAutos(AutoPorFecha)}
      * 
      * @see alquiler.Alquiler#removeFromAutos(AutoPorFecha)
      * 
      * @return List<disponibles.AutoPorFecha>
      * 
      */
     public List<AutoPorFecha> choices0RemoveFromAutos() {
         return Lists.newArrayList(getAutos());
     }        
 
     
  	@Persistent(mappedBy="alquiler")	
  	private List<Adicional> adicionales= new ArrayList<Adicional>();
  	/**
      * Retorno de Lista de Adicionales con relacion bidireccional con el Alquiler.
      * @return List<adicional.Adicional>
      * 
      */
     public List<Adicional> getAdicionales() {
          return adicionales;
     }
     /**
      * Seteo de Lista de Adicionales.
      * @param listaAdicionales
      * 
      */
     public void setAdicionales(List<Adicional> listaAdicionales) {
      	this.adicionales = listaAdicionales;
     }
     /**
      * Accion que agrega un Adicional a la lista de Adicionales.
      * @param adicional
      * @return Alquiler
      */
     @Named("Añadir")
     @MemberOrder(name="Adicionales",sequence="1")
     public Alquiler agregar(
     		@Named("Adicional") Adicional adicional){
     	Adicional adic=container.newTransientInstance(Adicional.class);
     	adic.setNombre(adicional.getNombre());
     	adic.setDescripcion(adicional.getDescripcion());
     	adic.setPrecio(adicional.getPrecio());
     
     	addToAdicionales(adic);    	
     	return this;
     }
     /**
      * Accion provista por el Framework que deshabilita la opcion
      * de agregar elementos a la lista Adicionales.
      * 
      * @param adicional
      * @return String
      */
     public String disableAgregar(Adicional adicional){
         if(getEstado() == EstadoAlquiler.EN_PROCESO) {
             return null;
         }
         else {           
                if( getEstado() == EstadoAlquiler.RESERVADO){
             	   return "El Alquiler debe estar EN PROCESO";
                }else return getEstado()==EstadoAlquiler.FINALIZADO? "El Alquiler esta FINALIZADO no se puede editar":"El Alquiler esta CERRADO no se puede editar";
         }       
     }
     /**
      * Accion provista por el Framework que permite
      * agregar elementos a la lista Adicionales a un Alquiler.
      * Si es que esta lista no está vacia.
      * @param adic
      */
     @Hidden
     public void addToAdicionales(Adicional adic) {
     	if(adic == null || adicionales.contains(adic)) {
              return;
     	}    	
     	adic.setAlquiler(this);
     	adicionales.add(adic);
     	calculoTotal();
     } 
     /**
      * Accion provista por el Framework que permite
      * eliminar elementos a la lista Adicionales.
      * @param adic
      * @return Alquiler
      */
     @Named("Borrar")
     @MemberOrder(name="Adicionales",sequence="2")
     public Alquiler removeFromAdicionales(final Adicional adic) {
     		adicionales.remove(adic);            
             calculoTotal();
             return this;
     }
     /**
      * Accion provista por el Framework que deshabilita
      * la opcion de remover elementos a la lista Adicionales.
      * @param adicional
      * @return String
      */
     public String disableRemoveFromAdicionales(Adicional adicional){
         if(getEstado() == EstadoAlquiler.EN_PROCESO) {
         	return adicionales.size()>0? null: "No existe Adicionales para este Alquiler";
         }
         else {           
         	if (getEstado() == EstadoAlquiler.RESERVADO){
         		return "No existe Adicionales para este Alquiler";
         	}else return getEstado()==EstadoAlquiler.FINALIZADO? "El Alquiler esta FINALIZADO no se puede editar":"El Alquiler esta CERRADO no se puede editar";
         }			          
     }
     /**
     * Choices provisto por el Framework, 
     * que habilita una serie de opciones para una Propiedad/Accion.
     * 
     * Choices para la accion {@link alquiler.Alquiler#removeFromAdicionales(Adicional)}
     * 
     * @return List<Adicional>
     */ 
     public List<Adicional> choices0RemoveFromAdicionales() {
         return Lists.newArrayList(getAdicionales());
     }        
     	
 	private Cliente clienteId;
 	/**
 	 * Retorna el Nro de Cuil/Cuit del Cliente que realiza el Alquiler
 	 * @return Cliente
 	 */
 	@DescribedAs("Numero de CUIL/CUIT")
 	@Disabled
 	@Named("CUIL/CUIT")
 	@MemberOrder(name="Cliente",sequence="1")	
 	public Cliente getClienteId() {
 		return clienteId;
 	}
 	/**
 	 * Setea el Nro de Cuil/Cuit del Cliente que realiza el Alquiler
 	 * @param clienteId
 	 */
 	public void setClienteId(final Cliente clienteId)	{		
 		this.clienteId=clienteId;
 	}	
 	
 	private String nombre;
 	/**
 	 * Retorna el nombre del Cliente que realiza el Alquiler.
 	 * @return String
 	 */
 	@Disabled
 	@Named("Nombre")
 	@MemberOrder(name="Cliente",sequence="2")	
 	public String getNombreCliente() {
 		return nombre;
 	}
 	/**
 	 * Setea el nombre del Cliente que realiza el Alquiler.
 	 * @param nombre
 	 */
 	public void setNombreCliente(String nombre) {
 		this.nombre = nombre;
 	}
 
 	private String apellido;
 	/**
 	 * Retorna el apellido del Cliente que realiza el Alquiler
 	 * @return String
 	 */
 	@Disabled
 	@Named("Apellido")
 	@MemberOrder(name="Cliente",sequence="3")	
 	public String getApellidoCliente() {
 		return apellido;
 	}
 	/**
 	 * Setea el apellido del Cliente que realiza el Alquiler
 	 * @param apellido
 	 */
 	public void setApellidoCliente(String apellido) {
 		this.apellido = apellido;
 	}
 	
 	
 	/**
 	 * Accion que permite el borrado del Alquiler
 	 */
 	@Named("Borrar")
 	public void borrarAlquiler(){		
 		for (Adicional adic:getAdicionales()){
 			container.removeIfNotAlready(adic);
 		}
 		for (AutoPorFecha auto:getAutos()){
 			container.removeIfNotAlready(auto);
 		}
 		container.informUser("Alquiler número: "+getNumero()+" a sido borrado");
         container.removeIfNotAlready(this);		
 	}
 	/**
 	 * Accion provista por el Framework que permite deshabilitar la accion de borrar el Alquiler.
 	 * @see Alquiler#borrarAlquiler()
 	 * @return String
 	 */
 	public String disableBorrarAlquiler(){
 		if (getEstado()==EstadoAlquiler.RESERVADO||getEstado()==EstadoAlquiler.EN_PROCESO){
 			return null;
 		}
 		else {
 			return getEstado()==EstadoAlquiler.FINALIZADO? "El Alquiler esta FINALIZADO no se puede borrar":"El Alquiler esta CERRADO no se puede borrar";
 		}
 	}
 	
 	private String ownedBy;
 	/**
 	 * Propiedad que retorna el usuario.
 	 * @return String
 	 */
 	@Hidden 
 	public String getOwnedBy() {
 	    return ownedBy;	
 	}
 	/**
 	 * Propiedad que setea el usuario.
 	 * @param ownedBy
 	 */
 	public void setOwnedBy(final String ownedBy){
 	    this.ownedBy = ownedBy;	
 	}	
 	/**
 	 * Accion que pasa el estado del Alquiler a: EN PROCESO
 	 * @return Alquiler
 	 */
 	@MemberOrder(name="Estado",sequence="2")
 	public Alquiler enProceso(){
 		setEstado(EstadoAlquiler.EN_PROCESO);		   	
 		return this;
 	}
 	/**
 	 * Accion provista por el Framework que deshabilita
 	 * la transicion del estado del Alquiler a: EN PROCESO.
 	 * 
 	 * @return String
 	 */
     public String disableEnProceso() {
         if(getEstado() == EstadoAlquiler.RESERVADO) {
                 return null;
         }
         else return getEstado() == EstadoAlquiler.EN_PROCESO? "El Alquiler ya se encuentra EN PROCESO":"El Alquiler debe estar RESERVADO para pasar a EN PROCESO";               
     }
     
     /**
 	 * Accion que pasa el estado del Alquiler a: FINALIZADO
 	 * @return Alquiler
 	 */    
     @MemberOrder(name="Estado",sequence="3")
 	public Alquiler finalizado(){
 		setEstado(EstadoAlquiler.FINALIZADO);
 		return this;
 	}
     /**
 	 * Accion provista por el Framework que deshabilita
 	 * la transicion del estado del Alquiler a: FINALIZADO.
 	 * 
 	 * @return String
 	 */
     public String disableFinalizado() {
         if(getEstado() == EstadoAlquiler.EN_PROCESO) {
                 return null;
         }
         else return getEstado() == EstadoAlquiler.FINALIZADO? "El Alquiler ya se encuentra FINALIZADO":"El Alquiler debe estar EN PROCESO para FINALIZARLO";
     }
     
     /**
    	 * Accion que pasa el estado del Alquiler a: CERRADO
    	 * @return Alquiler
    	 */   
 	@MemberOrder(name="Estado",sequence="4")
 	public Alquiler cerrado(){
 		setEstado(EstadoAlquiler.CERRADO);
 		return this;
 	}
 	/**
 	 * Accion provista por el Framework que deshabilita
 	 * la transicion del estado del Alquiler a: CERRADO.
 	 * 
 	 * @return String
 	 */
     public String disableCerrado() {
         return (getEstado() == EstadoAlquiler.EN_PROCESO||getEstado()==EstadoAlquiler.RESERVADO)?"El Alquiler debe estar FINALIZADO para poder CERRARLO":null;   
     }
     
     private DomainObjectContainer container;
     /**
      * injected: DomainObjectContainer
      */ 
     public void setDomainObjectContainer(final DomainObjectContainer container) {
         this.container = container;
     }
 }
