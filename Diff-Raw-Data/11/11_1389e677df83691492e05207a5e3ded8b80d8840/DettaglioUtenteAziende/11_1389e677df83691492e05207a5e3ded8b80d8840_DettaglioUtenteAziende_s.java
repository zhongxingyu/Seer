 package it.ivncr.erp.jsf.managedbean.accesso.utente;
 
 import it.ivncr.erp.model.generale.Azienda;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.lut.LUTService;
 import it.ivncr.erp.service.utente.UtenteService;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.event.TabChangeEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean
 @ViewScoped
 public class DettaglioUtenteAziende implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(DettaglioUtenteAziende.class);
 
 	private static final long serialVersionUID = 1L;
 
 	@ManagedProperty("#{gestioneUtenti.edited.id}")
 	private Integer id;
 
 	private List<Azienda> aziende;
 	private List<Azienda> filtered;
 	private Azienda[] selected;
 	private boolean[] disabled;
 	private Integer codiceAziendaPreferita;
 
 
 	public void onTabChange(TabChangeEvent event) {
 
 		// If aziende tab has been selected it is necessary to initialize
 		// the aziende list in the bean.
 		//
 		if(event.getTab().getId().equals("aziendeTab")) {
 
 			logger.debug("Selected tab changed to aziende.");
 
 			// Id should never be null, otherwise we are in for some kind
 			// of flow-of-pages or DI problem...
 			//
 			if(id == null) {
 				String msg = "Unexpected null id detected.";
 				logger.error(msg);
 				throw new RuntimeException(msg);
 			}
 
 			logger.debug("Loading aziende list.");
 			LUTService ls = ServiceFactory.createService("LUT");
 			aziende = ls.listItems("Azienda", "codice");
 
 			UtenteService us = ServiceFactory.createService("Utente");
 			List<Azienda> list = us.listAziende(id);
 			selected = list.toArray(new Azienda[0]);
 
 			Azienda preferita = us.retrieveDefaultAzienda(id, false);
 			codiceAziendaPreferita = preferita != null ? preferita.getId() : null;
 
 			updateDisabledForDefaultSelection();
 		}
 	}
 
 
 	public void onRowSelectCheckbox() {
 
 		updateDisabledForDefaultSelection();
 	}
 
 
 	public void doResetDefault() {
 
 		codiceAziendaPreferita = null;
 	}
 
 
 	private void updateDisabledForDefaultSelection() {
 
 		disabled = new boolean[aziende.size()];
 		for(int i = 0; i < disabled.length; ++i) {
 
 			disabled[i] = true;
 			Azienda a1 = aziende.get(i);
 
 			for(int j = 0; j < selected.length; ++j) {
 				Azienda a2 = selected[j];
 
 				if(a1.getId().equals(a2.getId())) {
 					disabled[i] = false;
 					break;
 				}
 			}
 		}
 
 		// Check if the current default is on a disabled radio.
 		//
 		for(int i = 0; i < aziende.size(); ++i) {
 
 			Azienda azienda = aziende.get(i);
 
 			if(disabled[i] && azienda.getId().equals(codiceAziendaPreferita)) {
 				codiceAziendaPreferita = null;
 				break;
 			}
 		}
 	}
 
 
 	public void doUpdateAziende() {
 
 		logger.debug("Entering doUpdateAziende() method.");
 
 		// Get selected ids.
 		//
 		Integer[] ids = new Integer[selected.length];
 		for(int i = 0; i < selected.length; ++i) {
 			ids[i] = selected[i].getId();
 		}
 
 		// Set the aziende.
 		//
 		try {
 			UtenteService us = ServiceFactory.createService("Utente");
 			us.setAziende(
 				id,
 				ids,
 				codiceAziendaPreferita);
 
 			// Everything went fine.
 			//
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_INFO,
 					"Successo",
 					"Il salvataggio dei dati si è concluso con successo.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 
 		} catch(Exception e) {
 
 			logger.warn("Exception caught while updating entity.", e);
 
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Errore di sistema",
 					"Si è verificato un errore in fase di aggiornamento dei record.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 		}
 	}
 
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public List<Azienda> getAziende() {
 		return aziende;
 	}
 
 	public void setAziende(List<Azienda> aziende) {
 		this.aziende = aziende;
 	}
 
 	public List<Azienda> getFiltered() {
 		return filtered;
 	}
 
 	public void setFiltered(List<Azienda> filtered) {
 		this.filtered = filtered;
 	}
 
 	public Azienda[] getSelected() {
 		return selected;
 	}
 
 	public void setSelected(Azienda[] selected) {
 		this.selected = selected;
 	}
 
 	public Integer getCodiceAziendaPreferita() {
 		return codiceAziendaPreferita;
 	}
 
 	public void setCodiceAziendaPreferita(Integer codiceAziendaPreferita) {
 		this.codiceAziendaPreferita = codiceAziendaPreferita;
 	}
 
 	public boolean[] getDisabled() {
 		return disabled;
 	}
 
 	public void setDisabled(boolean[] disabled) {
 		this.disabled = disabled;
 	}
 }
