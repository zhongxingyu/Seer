  /******* BEGIN LICENSE BLOCK *****
  * Versión: GPL 2.0/CDDL 1.0/EPL 1.0
  *
  * Los contenidos de este fichero están sujetos a la Licencia
  * Pública General de GNU versión 2.0 (la "Licencia"); no podrá
  * usar este fichero, excepto bajo las condiciones que otorga dicha 
  * Licencia y siempre de acuerdo con el contenido de la presente. 
  * Una copia completa de las condiciones de de dicha licencia,
  * traducida en castellano, deberá estar incluida con el presente
  * programa.
  * 
  * Adicionalmente, puede obtener una copia de la licencia en
  * http://www.gnu.org/licenses/gpl-2.0.html
  *
  * Este fichero es parte del programa opensiXen.
  *
  * OpensiXen es software libre: se puede usar, redistribuir, o
  * modificar; pero siempre bajo los términos de la Licencia 
  * Pública General de GNU, tal y como es publicada por la Free 
  * Software Foundation en su versión 2.0, o a su elección, en 
  * cualquier versión posterior.
  *
  * Este programa se distribuye con la esperanza de que sea útil,
  * pero SIN GARANTÍA ALGUNA; ni siquiera la garantía implícita 
  * MERCANTIL o de APTITUD PARA UN PROPÓSITO DETERMINADO. Consulte 
  * los detalles de la Licencia Pública General GNU para obtener una
  * información más detallada. 
  *
  * TODO EL CÓDIGO PUBLICADO JUNTO CON ESTE FICHERO FORMA PARTE DEL 
  * PROYECTO OPENSIXEN, PUDIENDO O NO ESTAR GOBERNADO POR ESTE MISMO
  * TIPO DE LICENCIA O UNA VARIANTE DE LA MISMA.
  *
  * El desarrollador/es inicial/es del código es
  *  FUNDESLE (Fundación para el desarrollo del Software Libre Empresarial).
  *  Indeos Consultoria S.L. - http://www.indeos.es
  *
  * Contribuyente(s):
  *  Eloy Gómez García <eloy@opensixen.org> 
  *
  * Alternativamente, y a elección del usuario, los contenidos de este
  * fichero podrán ser usados bajo los términos de la Licencia Común del
  * Desarrollo y la Distribución (CDDL) versión 1.0 o posterior; o bajo
  * los términos de la Licencia Pública Eclipse (EPL) versión 1.0. Una 
  * copia completa de las condiciones de dichas licencias, traducida en 
  * castellano, deberán de estar incluidas con el presente programa.
  * Adicionalmente, es posible obtener una copia original de dichas 
  * licencias en su versión original en
  *  http://www.opensource.org/licenses/cddl1.php  y en  
  *  http://www.opensource.org/licenses/eclipse-1.0.php
  *
  * Si el usuario desea el uso de SU versión modificada de este fichero 
  * sólo bajo los términos de una o más de las licencias, y no bajo los 
  * de las otra/s, puede indicar su decisión borrando las menciones a la/s
  * licencia/s sobrantes o no utilizadas por SU versión modificada.
  *
  * Si la presente licencia triple se mantiene íntegra, cualquier usuario 
  * puede utilizar este fichero bajo cualquiera de las tres licencias que 
  * lo gobiernan,  GPL 2.0/CDDL 1.0/EPL 1.0.
  *
  * ***** END LICENSE BLOCK ***** */
 package org.opensixen.process;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.logging.Level;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.compiere.model.MConversionRate;
 import org.compiere.model.MConversionType;
 import org.compiere.model.MCurrency;
 import org.compiere.process.ProcessInfoParameter;
 import org.compiere.process.SvrProcess;
 import org.compiere.util.DB;
 import org.compiere.util.Env;
 import org.compiere.util.Trx;
 import org.opensixen.osgi.interfaces.ICommand;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * UpdateChangeRate 
  *
  * @author Eloy Gomez
  * Indeos Consultoria http://www.indeos.es
  */
 public class UpdateChangeRate extends SvrProcess implements ICommand {
 
 	private RateDataModel dataModel;
 	private String sourceURL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
 	private String[] enabledCurrency;
 	private String baseISOCode = "EUR";
 	private MCurrency baseCurrency;
 	private int C_ConversionType_ID;
 	private String trxName;
 	private Properties ctx;
 	
 	/* (non-Javadoc)
 	 * @see org.compiere.process.SvrProcess#prepare()
 	 */
 	@Override
 	public void prepare() {
 		ctx = Env.getCtx();
 		for (ProcessInfoParameter param :getParameter())	{
 			if (param.getParameterName().equals("Currencies"))	{
 				String currencies = param.getParameter().toString();
 				enabledCurrency = currencies.split(",");
 			}
 		}
 		baseCurrency = MCurrency.get(ctx, baseISOCode);
 		C_ConversionType_ID = MConversionType.getDefault(Env.getAD_Client_ID(ctx));
 		if (baseCurrency == null || C_ConversionType_ID == 0)	{
 			throw new RuntimeException("Can't find base currency: " + baseISOCode + "or conversionType: " + C_ConversionType_ID);
 		}
 		trxName = Trx.createTrxName();
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.compiere.process.SvrProcess#doIt()
 	 */
 	@Override
 	public String doIt() throws Exception {
 		parse();
 		for (String currency:enabledCurrency)	{
 			addRate(currency.trim());
 		}
 		
 		DB.commit(true, trxName);
 		return null;		
 	}
 
 	
 	public void parse()	{
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setNamespaceAware(true); // never forget this!
 		DocumentBuilder builder;
 		dataModel = new RateDataModel();
 		try {
 			builder = factory.newDocumentBuilder();
 			Document doc = builder.parse(sourceURL);
 			
 			NodeList cubes = doc.getElementsByTagName("Cube");
 			
 			for (int i=0; i < cubes.getLength(); i++)	{
 				Node node = cubes.item(i);
 				NamedNodeMap attributes = node.getAttributes();
 				
 				if (attributes.getLength() > 0)	{
 					Node timeNode = attributes.getNamedItem("time");
 					if (timeNode != null)	{
 						// Establecemos la fecha de inicio y fin
 						// La de inicio es el dia del cambio + 1;
 						// la de fin el mismo dia.
 						SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
 						Date parsedDate = dateFormat.parse(timeNode.getNodeValue());
 						GregorianCalendar calendar = new GregorianCalendar();
 						calendar.setTime(parsedDate);				
 						calendar.add(Calendar.HOUR, 24);
 						dataModel.setDateFrom(new Timestamp(calendar.getTimeInMillis()));
 						// Si no es sabado, la fecha fin es la misma
						if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)	{
 							dataModel.setDateTo(new Timestamp(calendar.getTimeInMillis()));
 						}
 						
 						// Los sabados la fecha de fin es 3 dias despues
 						else {
 							calendar.add(Calendar.HOUR, 48);
 							dataModel.setDateTo(new Timestamp(calendar.getTimeInMillis()));
 						}
 						log.info("Time: " + timeNode.getNodeValue());
 					}
 					else {
 						Node currencyNode = attributes.getNamedItem("currency");
 						Node rateNode = attributes.getNamedItem("rate");
 						if (currencyNode != null && rateNode != null)	{
 							String currency = currencyNode.getNodeValue();
 							BigDecimal rate = new BigDecimal(rateNode.getNodeValue());
 							dataModel.addRate(currency, rate);
 						}
 					}
 					
 				}
 			}
 		    
 		} catch (Exception e) {
 			log.log(Level.SEVERE, "No se puede actualizar la informacion de las tasas.", e);
 		} 		
 	}
 	
 	/**
 	 * Añade la tasa de cambio para la moneda.
 	 * @param currency
 	 */
 	private void addRate(String ISOCode)	{
 		BigDecimal rate = dataModel.getRate(ISOCode);
 		MCurrency currency = MCurrency.get(ctx, ISOCode);
 		// Primero, añadimos la tasa base -> currency
 		MConversionRate base2currency = new MConversionRate(baseCurrency, C_ConversionType_ID, baseCurrency.getC_Currency_ID()	, currency.getC_Currency_ID(), rate, dataModel.getDateFrom());
 		log.info(base2currency.toString());
 		base2currency.setValidTo(dataModel.getDateTo());
 		base2currency.save(trxName);
 		
 		// Ahora la tasa inversa
 		double dd = 1 / rate.doubleValue();
 		BigDecimal divideRate = new BigDecimal(dd);
 		MConversionRate currency2base = new MConversionRate(currency, C_ConversionType_ID, currency.getC_Currency_ID()	, baseCurrency.getC_Currency_ID(), divideRate, dataModel.getDateFrom());
 		
 		// Sobreescribimos el divideRate puesto que sabemos exactamente el que es
 		currency2base.setDivideRate(rate);
 
 		log.info(currency2base.toString());
 		currency2base.setValidTo(dataModel.getDateTo());
 		currency2base.save(trxName);				
 	}
 	
 }
 
 class RateDataModel	{
 	private Timestamp dateFrom;
 	private Timestamp dateTo;
 	
 	private HashMap<String, BigDecimal> rates = new HashMap<String, BigDecimal>();
 	
 	
 	/**
 	 * @return the dateFrom
 	 */
 	public Timestamp getDateFrom() {
 		return dateFrom;
 	}
 
 	/**
 	 * @param dateFrom the dateFrom to set
 	 */
 	public void setDateFrom(Timestamp dateFrom) {
 		this.dateFrom = dateFrom;
 	}
 
 	/**
 	 * @return the dateTo
 	 */
 	public Timestamp getDateTo() {
 		return dateTo;
 	}
 
 	/**
 	 * @param dateTo the dateTo to set
 	 */
 	public void setDateTo(Timestamp dateTo) {
 		this.dateTo = dateTo;
 	}
 
 	public void addRate(String currency, BigDecimal rate)	{
 		rates.put(currency, rate);
 	}
 	
 	public BigDecimal getRate(String currency)	{
 		return rates.get(currency);
 	}
 }
