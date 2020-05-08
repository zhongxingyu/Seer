 package ar.com.agtech.syros.esphora.conector.elements;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 /**
  * @author Jorge Morando
  *
  */
 
 public class Importe {
 
	private BigDecimal total;
	private BigDecimal neto;
	private BigDecimal iva;
 	private TipoIva tipoIva;
 	private Boolean discriminarIva;
 	
 	/**
 	 * Construye un importe teniendo el neto y calculando el iva dependiendo del tipo de<br>
 	 * IVA que se suministre como par&aacute;metro.<br>
 	 * Por defecto el IVA ser&aacute; discriminado yse calcular&aacute; de acuerdo al tipo de IVA suministrado
 	 * @param total
 	 * @param tipoIva
 	 */
 //	public Importe(BigDecimal importeNeto, TipoIva tipoIva){
 //		this.neto = importeNeto;
 //		this.tipoIva = tipoIva;
 //		this.discriminarIva = Boolean.TRUE;
 //		calcularImporte();
 //	}
 	
 	/**
 	 * Construye un importe desde el importe base y calculando el iva dependiendo del tipo de<br>
 	 * IVA que se suministre como par&aacute;metro. Concluyendo en 2 situaciones:<br>
 	 * <ol>
 	 * <li><p>Si discriminarIva es Boolean.TRUE entonces se calcular&aacute; el IVA de acuerdo al tipo suministrado<br>
 	 * y se manejar&aacute; por separado, convirti&eacute;ndose as&iacute; el importe suministrado en NETO.</p>
 	 * </p><p>
 	 * Total = Neto(#) * TipoIva(#) / 100 
 	 * </p></li>
 	 * <li><p>Si discriminarIva es Boolean.FALSE entonces se calcular&aacute; el IVA de acuerdo al tipo suministrado<br>
 	 * sin ser discriminado calculando así el neto sobre el importe suministrado BRUTO
 	 * </p><p>
 	 * Importe(#) = Neto * TipoIva(#) / 100 
 	 * </p></li>
 	 * </ol> 
 	 * <p>(#) - Es el par&aacute;metro "importe"</p>
 	 * @param importe Valor neto o bruto (dependiendo de discriminarIva = TRUE/FALSE)
 	 * @param tipoIva Tipo de iva a Calcular sobre el importe
 	 * @param discriminarIva Flag que indica si el valor de importe es el total o bruto
 	 */
 	public Importe(BigDecimal importe, TipoIva tipoIva, boolean discriminarIva){
 		if(discriminarIva){
 			this.neto = importe;
 		}else{
 			this.total = importe;
 		}
 		this.tipoIva = tipoIva;
 		this.discriminarIva = discriminarIva;
 		calcularImporte();
 	}
 	
 	public BigDecimal getTotal() {
 		return total;
 	}
 
 	public BigDecimal getNeto() {
 		return neto;
 	}
 
 	public BigDecimal getIva() {
 		return iva;
 	}
 
 	public TipoIva getTipoIva() {
 		return tipoIva;
 	}
 
 	/* PRIVATE METHODS*/
 	private void calcularImporte(){
 		BigDecimal cent = new BigDecimal("100");
 		BigDecimal uni = new BigDecimal("1");
 		
 		BigDecimal porcIva = traducirIVA(); //obtenemos el porcentaje de iva
 		BigDecimal intPorcIva = porcIva.divide(cent);  //calculamos el operando del iva
 		BigDecimal multiplicand = intPorcIva.add(uni); //calculamos el multiplicando para el iva
 		
		porcIva.setScale(2, RoundingMode.HALF_UP);
 		if(discriminarIva){
 			iva = neto.multiply(intPorcIva);
 			total = neto.multiply(multiplicand);
 		}else{
			neto = total.divide(multiplicand);
 			iva = total.subtract(neto);
 		}
 	}
 	
 	private BigDecimal traducirIVA(){
 		BigDecimal porcIva;
 		switch (tipoIva) {
 		case CERO:
 			porcIva = new BigDecimal("0");
 			break;
 		case DIEZ_COMA_CINCO:
 			porcIva = new BigDecimal("10.5");
 			break;
 		case VINTISIETE:
 			porcIva = new BigDecimal("27");
 			break;
 		default:
 			porcIva = new BigDecimal("21");
 			break;
 		}
 		return porcIva;
 	}
 	
 }
