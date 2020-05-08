 package de.shop.artikelverwaltung.controller;
 
 import static de.shop.util.Constants.JSF_INDEX;
 import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
 import static javax.ejb.TransactionAttributeType.REQUIRED;
 import static javax.ejb.TransactionAttributeType.SUPPORTS;
 
 import java.io.Serializable;
 import java.lang.invoke.MethodHandles;
 import java.util.Collections;
 import java.util.List;
 import java.util.Locale;
 
 import javax.enterprise.event.Event;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.ejb.Stateful;
 import javax.ejb.TransactionAttribute;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.context.Flash;
 import javax.faces.event.ValueChangeEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.servlet.http.HttpSession;
 
 import org.jboss.logging.Logger;
 import org.richfaces.cdi.push.Push;
 
 import de.shop.artikelverwaltung.domain.Artikel;
 import de.shop.artikelverwaltung.domain.Kategorie;
 import de.shop.artikelverwaltung.service.ArtikelService;
 import de.shop.auth.controller.AuthController;
 import de.shop.util.Client;
 import de.shop.util.Log;
 import de.shop.util.Transactional;
 
 
 
 /**
  * Dialogsteuerung fuer die ArtikelService
  */
 @Named("ac")
 @SessionScoped
 @Stateful
 @TransactionAttribute(SUPPORTS)
 @Log
 public class ArtikelController implements Serializable {
 	private static final long serialVersionUID = 1564024850446471639L;
 
 	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
 	
 	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
 	private static final String FLASH_ARTIKEL = "artikel";
 	private static final String JSF_ARTIKELVERWALTUNG = "/artikelverwaltung/";
 	private static final String JSF_VIEW_ARTIKEL = JSF_ARTIKELVERWALTUNG + "viewArtikel";
 
 	private static final String JSF_UPDATE_ARTIKEL = JSF_ARTIKELVERWALTUNG + "updateArtikel";
 	//private static final int ANZAHL_LADENHUETER = 5;
 	
 	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
 	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";
 
 	private Long artikel_id;	
 	private List<Artikel> artikel = Collections.emptyList();
 	private String name;
 	private Long id;
	private List<Artikel> ladenhueter;
 	private Artikel neuerArtikel;
 	private Kategorie neueKategorie;
 	private Artikel updateArtikel;
 	private boolean geaendertArtikel;
 	
 	private String selectedArtId;
 	private String selectedKatId;
 
 	@Inject
 	private ArtikelService as;
 	
 	@Inject
 	@Client
 	private Locale locale;
 	
 	@Inject
 	private Flash flash;
 	
 	@Inject
 	private transient HttpSession session;
 	
 	@Inject
 	private AuthController auth;
 	
 	@Inject
 	@Push(topic = "updateArtikel")
 	private transient Event<String> updateArtikelEvent;
 	
 
 	
 	@PostConstruct
 	private void postConstruct() {
 		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
 	}
 
 	@PreDestroy
 	private void preDestroy() {
 		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
 	}
 	
 	@Override
 	public String toString() {
 		return "ArtikelController [name=" + name + "]";
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}


	public List<Artikel> getLadenhueter() {
		return ladenhueter;
	}
 	
 	@Transactional
 	public String findArtikelByName() {
 		artikel = as.findArtikelByNamen(name, locale);
 		flash.put(FLASH_ARTIKEL, artikel);
 
 		return JSF_LIST_ARTIKEL;
 	}
 	
 	@Transactional
 	public void findArtikelById() {
 		updateArtikel = as.findArtikelById(Long.valueOf(selectedArtId), locale);
 	}
 	
 	@TransactionAttribute(REQUIRED)
 	public void createArtikel() {
 		
 			neuerArtikel = (Artikel) as.createArtikel(neuerArtikel, locale);
 		
 	}
 	@Transactional
 	public void findArtikelByKategorie() {
 		artikel = as.findArtikelByKategorie(selectedKatId, locale);
 		//flash.put(FLASH_ARTIKEL, artikel);
 
 		//return JSF_LIST_ARTIKEL_BY_KATEGORIE;
 	}
 	public void createEmptyArtikel() {
 		if (neuerArtikel != null) {
 			return;
 		}
 		neuerArtikel = new Artikel();
 		neueKategorie = new Kategorie();
 		neuerArtikel.setKategorie(neueKategorie);
 		
 	}
//TODO 
//	@Transactional
//	public void loadLadenhueter() {
//		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
//	}
 	
 	@Transactional
 	public String selectArtikel() {
 		if (session.getAttribute(SESSION_VERFUEGBARE_ARTIKEL) != null) {
 			return JSF_SELECT_ARTIKEL;
 		}
 		
 		final List<Artikel> alleArtikel = as.findAllArtikel();
 		session.setAttribute(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
 		return JSF_SELECT_ARTIKEL;
 	}
 
 	public Long getArtikel_id() {
 		return artikel_id;
 	}
 
 	public void setArtikel_id(Long artikel_id) {
 		this.artikel_id = artikel_id;
 	}
 
 	public List<Artikel> getArtikel() {
 		return artikel;
 	}
 
 	public void setArtikel(List<Artikel> artikel) {
 		this.artikel = artikel;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Artikel getNeuerArtikel() {
 		return neuerArtikel;
 	}
 
 	public void setNeuerArtikel(Artikel neuerArtikel) {
 		this.neuerArtikel = neuerArtikel;
 	}
 //	public void getKategorieParam(){
 //		
 //		FacesContext fc = FacesContext.getCurrentInstance();
 //		Map<String,String> params = fc.getExternalContext().getRequestParameterMap();
 //		selectedKatId = Long.valueOf(params.get("id"));
 //	}
 
 	public String getSelectedKatId() {
 		return selectedKatId;
 	}
 
 	public void setSelectedKatId(String selectedKatId) {
 		this.selectedKatId = selectedKatId;
 	}
 	
 	@TransactionAttribute(REQUIRED)
 	public String update() {
 		auth.preserveLogin();
 		
 		if (!geaendertArtikel || updateArtikel == null) {
 			return JSF_INDEX;
 		}
 		
 		updateArtikel = as.updateArtikel(updateArtikel, locale);
 	
 
 		// Push-Event fuer Webbrowser
 		updateArtikelEvent.fire(String.valueOf(updateArtikel.getArtikelId()));
 		
 		// ValueChangeListener zuruecksetzen
 		geaendertArtikel = false;
 		
 		// Aufbereitung fuer viewArtikel.xhtml
 		artikel_id = updateArtikel.getArtikelId();
 		
 		return JSF_VIEW_ARTIKEL + JSF_REDIRECT_SUFFIX;
 	}
 	
 	public String selectForUpdate(Artikel ausgewaehlterArtikel) {
 		if (ausgewaehlterArtikel == null) {
 			return null;
 		}
 		
 		updateArtikel = ausgewaehlterArtikel;
 		
 		return JSF_UPDATE_ARTIKEL;
 			   
 	}
 	public void geaendert(ValueChangeEvent e) {
 		if (geaendertArtikel) {
 			return;
 		}
 		
 		if (e.getOldValue() == null) {
 			if (e.getNewValue() != null) {
 				geaendertArtikel = true;
 			}
 			return;
 		}
 
 		if (!e.getOldValue().equals(e.getNewValue())) {
 			geaendertArtikel = true;				
 		}
 	}
 
 	public Artikel getUpdateArtikel() {
 		return updateArtikel;
 	}
 
 	public void setUpdateArtikel(Artikel updateArtikel) {
 		this.updateArtikel = updateArtikel;
 	}
 
 	public boolean isGeaendertArtikel() {
 		return geaendertArtikel;
 	}
 
 	public void setGeaendertArtikel(boolean geaendertArtikel) {
 		this.geaendertArtikel = geaendertArtikel;
 	}
 
 	public String getSelectedArtId() {
 		return selectedArtId;
 	}
 
 	public void setSelectedArtId(String selectedArtId) {
 		this.selectedArtId = selectedArtId;
 	}
 }
