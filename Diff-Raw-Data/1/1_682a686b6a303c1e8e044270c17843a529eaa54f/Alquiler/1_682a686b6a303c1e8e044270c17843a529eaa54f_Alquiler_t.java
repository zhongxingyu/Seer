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
 import org.apache.isis.applib.annotation.RegEx;
 import org.apache.isis.applib.annotation.Where;
 import org.apache.isis.applib.annotation.MemberGroups;
 
 
 import adicional.Adicional;
 import adicional.AdicionalServicio;
 
 import com.google.common.collect.Lists;
 
 import cliente.Cliente;
 import disponibles.AutoPorFecha;
 import disponibles.DisponibleServicio;
 
 
 
 @javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.APPLICATION)
 @javax.jdo.annotations.Version(strategy=VersionStrategy.VERSION_NUMBER, column="VERSION")
 @MemberGroups({"Estado","Cliente","Datos del Alquiler","Autos","Datos de Factura"})
 @javax.jdo.annotations.Query(name="traerAlquileres", language="JDOQL",value="SELECT FROM alquiler.Alquiler order by numero asc")
 @AutoComplete(repository=AlquilerServicio.class, action="autoComplete")
 @Audited 
 @ObjectType("ALQUILER")
 public class Alquiler {
 	
 	public String iconName(){
 		
 		if(getEstado() == EstadoAlquiler.CERRAR){
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
 	public static enum EstadoAlquiler{
 		RESERVADO, EN_PROCESO, FINALIZADO, CERRAR;
 	}	
 	public static enum TipoPago{
 		EFECTIVO, CHEQUE, TARJETA_CREDITO, TARJETA_DEBITO;
 	}
 	//{{Titulo
     public String title(){
             return getEstado().toString();        
     }
     // }}
     //  {{Numero de la reserva
     @PrimaryKey
     @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
     private Long numero;
     @Disabled
     @Named("Nro Alquiler")
     @MemberOrder(name="Datos del Alquiler",sequence="1")
     public Long getNumero() {
             return numero;
     }    
     public void setNumero(final Long numero) {
             this.numero = numero;
     }
     // }}
     // {{Estado
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
     @Hidden
     public EstadoAlquiler getEstado(){
             return estado;
     }
     public void setEstado(final EstadoAlquiler estado){
             this.estado = estado;
     }
     // }}
     // {{Fecha en la que se realiza el alquiler
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
     @Hidden
     public Date getFecha() {
             return fecha;
     }
     public void setFecha(final Date fecha) {
             this.fecha = fecha;
     }
     //}}
     // {{ Precio 
 	private String  precio;
     @Named("Precio")
     @Disabled
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos del Alquiler",sequence="3")
 	public String getPrecioAlquiler(){
 		return precio;
 	}
 	public void setPrecioAlquiler(final String precio){
 		this.precio=precio;
 	}	
 	@Hidden
 	//{{ Calculo para el sub-Total
 	public Alquiler calculoSubTotal(){
 		List<AutoPorFecha> listaAutos=Lists.newArrayList(getAutos());		
 		
 		BigDecimal sum=new BigDecimal("0");		
 		sum.setScale(5, BigDecimal.ROUND_HALF_UP);
 		for (AutoPorFecha auto:listaAutos){
 			BigDecimal aux=new BigDecimal(auto.getCategoria().getPrecio());
 			aux.setScale(5, BigDecimal.ROUND_HALF_UP);
 			sum=sum.add(aux);
 			//suma=suma+auto.getCategoria().getPrecio();
 		}		
 		setPrecioAlquiler(sum.toString());
 		return this;
 	}	
 	// }}
 	// {{ Tipo de Pago
 	private TipoPago tipoPago;
     @Named("Tipo de Pago")
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="1")
 	public TipoPago getTipoPago() {
 		return tipoPago;
 	}
 	public void setTipoPago(final TipoPago tipoPago) {
 		this.tipoPago = tipoPago;
 	}
 	public String disableTipoPago(){
 		if (getEstado()==EstadoAlquiler.RESERVADO||getEstado()==EstadoAlquiler.EN_PROCESO){
 			return "El Alquiler debe estar FINALIZADO";
 		}
 		else {
 			return getEstado()==EstadoAlquiler.FINALIZADO? null:"El Alquiler esta CERRADO no se puede editar";
 		}
 	}
 	// }}	
 	// {{ Numero de Factura
 	private int factura;
     @Named("Nro Factura")
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="2")
 	public int getNumeroFactura() {
 		return factura;
 	}
 	public void setNumeroFactura(final int factura) {
 		this.factura = factura;
 	}
 	public String disableNumeroFactura(){
 		if (getEstado()==EstadoAlquiler.RESERVADO||getEstado()==EstadoAlquiler.EN_PROCESO){
 			return "El Alquiler debe estar FINALIZADO";
 		}
 		else {
 			return getEstado()==EstadoAlquiler.FINALIZADO? null:"El Alquiler esta CERRADO no se puede editar";
 		}
 	}	
 	// }}
 	// {{ Precio 
 	private String precioTot;
     @Named("Precio Total")
     @Disabled
     @Hidden(where=Where.ALL_TABLES)
     @MemberOrder(name="Datos de Factura",sequence="3")
 	public String getPrecioTotal(){
 		return precioTot;
 	}
 	public void setPrecioTotal(final String precioTot){
 		this.precioTot=precioTot;
 	}	
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
 	// }}	
 	// {{ Lista de Autos 	
 	@Persistent(mappedBy="alquiler")		
 	private List<AutoPorFecha> autos = new ArrayList<AutoPorFecha>();
     public List<AutoPorFecha> getAutos() {
         return autos;
     }
     public void setAutos(List<AutoPorFecha> listaAutos) {
     	this.autos = listaAutos;
     }
     @Named("Borrar")
     @MemberOrder(name="Autos",sequence="1")
     public Alquiler removeFromAutos(final AutoPorFecha auto) {
             autos.remove(auto);
             container.removeIfNotAlready(auto);
             calculoSubTotal();
             calculoTotal();
             return this;            
     }
     public String disableRemoveFromAutos(AutoPorFecha auto){
         if(getEstado() == EstadoAlquiler.RESERVADO || getEstado() == EstadoAlquiler.EN_PROCESO) {
         	return autos.size()>0? null: "No existe Autos para este Alquiler";
         }
         else return getEstado()==EstadoAlquiler.FINALIZADO? "El Alquiler esta FINALIZADO no se puede editar":"El Alquiler esta CERRADO no se puede editar";          			          
     }
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
     public List<AutoPorFecha> choices0RemoveFromAutos() {
         return Lists.newArrayList(getAutos());
     }        
     // }}
     // {{ Lista de Adicionales
  	@Persistent(mappedBy="alquiler")	
  	private List<Adicional> adicionales= new ArrayList<Adicional>();
     public List<Adicional> getAdicionales() {
          return adicionales;
     }
     public void setAdicionales(List<Adicional> listaAdicionales) {
      	this.adicionales = listaAdicionales;
     }
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
     @Hidden
     public void addToAdicionales(Adicional adic) {
     	if(adic == null || adicionales.contains(adic)) {
              return;
     	}    	
     	adic.setAlquiler(this);
     	adicionales.add(adic);
     	calculoTotal();
     } 
     @Named("Borrar")
     @MemberOrder(name="Adicionales",sequence="2")
     public Alquiler removeFromAdicionales(final Adicional adic) {
     		adicionales.remove(adic);            
             calculoTotal();
             return this;
     }
     
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
     public List<Adicional> choices0RemoveFromAdicionales() {
         return Lists.newArrayList(getAdicionales());
     }        
     //}}
     // {{ Cliente		
 	private Cliente clienteId;
 	@DescribedAs("Numero de CUIL/CUIT")
 	@Disabled
 	@Named("CUIL/CUIT")
 	@MemberOrder(name="Cliente",sequence="1")	
 	public Cliente getClienteId() {
 		return clienteId;
 	}	
 	public void setClienteId(final Cliente clienteId)	{		
 		this.clienteId=clienteId;
 	}	
 	// }}
 	// {{ Nombre Cliente
 	private String nombre;
 	@Disabled
 	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
 	@Named("Nombre")
 	@MemberOrder(name="Cliente",sequence="2")	
 	public String getNombreCliente() {
 		return nombre;
 	}
 	public void setNombreCliente(String nombre) {
 		this.nombre = nombre;
 	}
 	// }}
 
 	// {{ Apellido Cliente
 	private String apellido;
 	@Disabled
 	@RegEx(validation = "\\w[@&:\\-\\,\\.\\+ \\w]*")
 	@Named("Apellido")
 	@MemberOrder(name="Cliente",sequence="3")	
 	public String getApellidoCliente() {
 		return apellido;
 	}
 	public void setApellidoCliente(String apellido) {
 		this.apellido = apellido;
 	}
 	// {{
 	//{{
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
 	// }}
     	
 	// {{ {{ OwnedBy (property)	
 	private String ownedBy;
 	@Hidden 
 	public String getOwnedBy() {
 	    return ownedBy;	
 	}
 	public void setOwnedBy(final String ownedBy){
 	    this.ownedBy = ownedBy;	
 	}	
 	// }}
 	@MemberOrder(name="Estado",sequence="2")
 	public Alquiler enProceso(){
 		setEstado(EstadoAlquiler.EN_PROCESO);		   	
 		
 		return this;
 	}
     public String disableEnProceso() {
         if(getEstado() == EstadoAlquiler.RESERVADO) {
                 return null;
         }
         else return getEstado() == EstadoAlquiler.EN_PROCESO? "El Alquiler ya se encuentra EN PROCESO":"El Alquiler debe estar RESERVADO para pasar a EN PROCESO";               
     }
     @MemberOrder(name="Estado",sequence="3")
 	public Alquiler finalizado(){
 		setEstado(EstadoAlquiler.FINALIZADO);
 		return this;
 	}
     public String disableFinalizado() {
         if(getEstado() == EstadoAlquiler.EN_PROCESO) {
                 return null;
         }
         else return getEstado() == EstadoAlquiler.FINALIZADO? "El Alquiler ya se encuentra FINALIZADO":"El Alquiler debe estar EN PROCESO para FINALIZARLO";
     }
     
 	@MemberOrder(name="Estado",sequence="4")
 	public Alquiler cerrar(){
 		setEstado(EstadoAlquiler.CERRAR);
 		return this;
 	}
     //{{Deshabilitacion de estado cerrar
     public String disableCerrar() {
         return (getEstado() == EstadoAlquiler.EN_PROCESO||getEstado()==EstadoAlquiler.RESERVADO)?"El Alquiler debe estar FINALIZADO para poder CERRARLO":null;   
     }
     //}}
     // {{ injected: DomainObjectContainer
     private DomainObjectContainer container;
     public void setDomainObjectContainer(final DomainObjectContainer container) {
         this.container = container;
     }
     // }}    
     @SuppressWarnings("unused")
 	private AdicionalServicio adicServ;
     @Hidden
     public void inyectarAdicionalServicio(AdicionalServicio adicServ){
     	this.adicServ=adicServ;
     }
     @SuppressWarnings("unused")
 	private DisponibleServicio dispServ;
     @Hidden
     public void inyectarAdicionalServicio(DisponibleServicio dispServ){
     	this.dispServ=dispServ;
     }
 }
