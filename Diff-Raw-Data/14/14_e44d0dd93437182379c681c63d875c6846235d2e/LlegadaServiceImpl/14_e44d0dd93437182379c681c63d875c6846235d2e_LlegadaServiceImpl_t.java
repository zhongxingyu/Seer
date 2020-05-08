 package com.opendata.trenconretraso.service.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlTable;
 import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
 import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
 import com.opendata.trenconretraso.bom.Estacion;
 import com.opendata.trenconretraso.bom.Llegada;
 import com.opendata.trenconretraso.dao.LlegadaDao;
 import com.opendata.trenconretraso.service.EstacionService;
 import com.opendata.trenconretraso.service.LlegadaService;
 
 import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_3_6;
 
 
 /**
  * 
  * @author Alberto Gomez Toribio
  *
  */
 @Service
 public class LlegadaServiceImpl implements LlegadaService {
 
 	Logger log = Logger.getLogger(this.getClass());
 	
 	@Autowired
 	EstacionService estacionService;
 	@Autowired
 	LlegadaDao llegadaDao;
 	
 	public LlegadaServiceImpl(){}
 	
 	public LlegadaServiceImpl(EstacionService estacionService, LlegadaDao llegadaDao){
 		this.estacionService = estacionService;
 		this.llegadaDao = llegadaDao;
 	}
 	
 	public void recolectarLlegadasDeEstacion(Estacion estacion) throws Exception {
 		
 		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 		
 		WebClient webClient = new WebClient(FIREFOX_3_6);
 		webClient.setJavaScriptEnabled(false);
 		webClient.setCssEnabled(false);
 		webClient.setAppletEnabled(false);
 		webClient.setThrowExceptionOnFailingStatusCode(false);
 		webClient.setThrowExceptionOnScriptError(false);
 		webClient.setPopupBlockerEnabled(true);
 		
 		HtmlPage page = webClient.getPage(estacion.getURL());
 		
 		HtmlTable tabla = (HtmlTable)page.getBody().getElementsByTagName("table").get(0);
 		
 		HtmlTableBody body = tabla.getBodies().get(0);
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 		
 		for(HtmlTableRow row : body.getRows()){
 			
 			Llegada llegada = new Llegada();
 			
 			llegada.setIdEstacion(estacion.getId());
 			llegada.setProcedencia(row.getCell(1).getTextContent().trim());
 			llegada.setNumeroTren(Long.parseLong(row.getCell(4).getTextContent().trim()));
 			
 			//Recojo la hora
 			Date hLlegadaDate = sdf.parse(row.getCell(0).getTextContent());
 			Calendar hLlegadaAux = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 			hLlegadaAux.setTime(hLlegadaDate);
 			
 			//Creo un calendar con la hora que voy a insertar
 			Calendar hLlegada = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 			hLlegada.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
 			hLlegada.set(Calendar.MONTH, now.get(Calendar.MONTH));
 			hLlegada.set(Calendar.YEAR, now.get(Calendar.YEAR));
 			hLlegada.set(Calendar.SECOND, 0);
 			hLlegada.set(Calendar.MILLISECOND, 0);
 			hLlegada.set(Calendar.HOUR_OF_DAY,hLlegadaAux.get(Calendar.HOUR_OF_DAY));
 			hLlegada.set(Calendar.MINUTE,hLlegadaAux.get(Calendar.MINUTE));
 			
			/**
			 * Si cuando se ejecuta esta lógica son las 23:30 y en la página de ADIF estoy 
			 * leyendo una llegada próxima con hora de llegada a las 00:30, esa llegada es 
			 * del día siguiente, por lo que debo de sumarle 1 al día
			 * 
			 * Estoy suponiendo que en la pagina de ADIF no se muestran llegadas cuya hora
			 * oficial de llegada es 1 hora atrás de la hora actual.
			 * Es decir, estoy suponiendo que las llegadas se borran conforme llegan los trenes,
			 * o al menos en la siguiente hora a la llegada del tren.
			 */
			if(hLlegada.get(Calendar.HOUR_OF_DAY) < now.get(Calendar.HOUR_OF_DAY)){
				hLlegada.add(Calendar.DATE, 1);
			}
			
 			llegada.sethLlegada(hLlegada.getTime());
 			
 			//recojo la hora
 			Date hPrevistaDate = sdf.parse(row.getCell(2).getTextContent());
 			Calendar hPrevistaAux = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 			hPrevistaAux.setTime(hPrevistaDate);
 			
 			Calendar hPrevista = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 			hPrevista.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH));
 			hPrevista.set(Calendar.MONTH, now.get(Calendar.MONTH));
 			hPrevista.set(Calendar.YEAR, now.get(Calendar.YEAR));
 			hPrevista.set(Calendar.SECOND, 0);
 			hPrevista.set(Calendar.MILLISECOND, 0);
 			hPrevista.set(Calendar.HOUR_OF_DAY,hPrevistaAux.get(Calendar.HOUR_OF_DAY));
 			hPrevista.set(Calendar.MINUTE,hPrevistaAux.get(Calendar.MINUTE));
 			
 			llegada.sethPrevista(hPrevista.getTime());
 			
 			//Buscamos la ultima llegada
 			Llegada llegadaLast = this.findLastByTren(estacion.getId(), llegada.getNumeroTren());
 			
 			Calendar llegadaLastHLlegada = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 			if(llegadaLast!= null){
 				llegadaLastHLlegada.setTime(llegadaLast.gethLlegada());
 			}
 			
 			//El numero de tren se repite cada dia, si es el del mismo dia actualizo,
 			//si es el de otro dia, lo creo
 			if(llegadaLast != null &&
 					llegadaLastHLlegada.get(Calendar.DAY_OF_MONTH) ==
 					hLlegada.get(Calendar.DAY_OF_MONTH)){
 				llegadaLast.sethPrevista(llegada.gethPrevista());
 				
 				llegadaLast.sethPrevista(llegada.gethPrevista());
 				this.update(llegadaLast);
 			}
 			else{
 				this.create(llegada);
 			}
 		}
 		
 	}
 
 	public Llegada findById(Long id) {
 		return llegadaDao.findById(id);
 	}
 
 	public List<Llegada> findByEstacion(Long idEstacion, Date dia) {
 		Calendar fechac = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 		fechac.setTime(dia);
 		
 		Calendar desdec = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 		desdec.set(Calendar.DAY_OF_MONTH, fechac.get(Calendar.DAY_OF_MONTH));
 		desdec.set(Calendar.MONTH, fechac.get(Calendar.MONTH));
 		desdec.set(Calendar.YEAR, fechac.get(Calendar.YEAR));
 		desdec.set(Calendar.HOUR_OF_DAY,0);
 		desdec.set(Calendar.MINUTE,0);
 		desdec.set(Calendar.SECOND,0);
 		desdec.set(Calendar.MILLISECOND,0);
 		
 		Calendar hastac = Calendar.getInstance(TimeZone.getTimeZone("Europe/Madrid"));
 		hastac.set(Calendar.DAY_OF_MONTH, fechac.get(Calendar.DAY_OF_MONTH));
 		hastac.set(Calendar.MONTH, fechac.get(Calendar.MONTH));
 		hastac.set(Calendar.YEAR, fechac.get(Calendar.YEAR));
 		hastac.set(Calendar.HOUR_OF_DAY,23);
 		hastac.set(Calendar.MINUTE,59);
 		hastac.set(Calendar.SECOND,59);
 		hastac.set(Calendar.MILLISECOND,0);
 		
 		return this.findByEstacion(idEstacion,desdec.getTime(),hastac.getTime());
 	}
 
 	@Override
 	public Llegada create(Llegada llegada) {
 		return llegadaDao.create(llegada);
 	}
 
 	@Override
 	public List<Llegada> findAll() {
 		return llegadaDao.findAll();
 	}
 
 	@Override
 	public Llegada update(Llegada llegada) {
 		return llegadaDao.update(llegada);
 	}
 
 	@Override
 	public Llegada findLastByTren(Long idEstacion, Long numeroTren) {
 		return llegadaDao.findLastByTren(idEstacion, numeroTren);
 	}
 
 	@Override
 	public List<Llegada> findByEstacion(Long idEstacion, Date desde, Date hasta) {
 		return llegadaDao.findByEstacion(idEstacion, desde, hasta);
 	}
 
 }
