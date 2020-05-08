 package it.ivncr.erp.jsf.managedbean.commerciale.gestioneclienti;
 
 import it.ivncr.erp.jsf.managedbean.accesso.session.LoginInfo;
 import it.ivncr.erp.model.commerciale.Cliente;
 import it.ivncr.erp.model.commerciale.Contatto;
 import it.ivncr.erp.model.commerciale.Divisa;
 import it.ivncr.erp.model.commerciale.GruppoCliente;
 import it.ivncr.erp.model.commerciale.TipoBusinessPartner;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.cliente.ClienteService;
 import it.ivncr.erp.service.contatto.ContattoService;
 import it.ivncr.erp.service.lut.LUTService;
 import it.ivncr.erp.util.ValidationUtil;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import org.apache.commons.lang3.ObjectUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.primefaces.context.RequestContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 @ManagedBean
 @ViewScoped
 public class DettaglioClienteGenerale implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(DettaglioClienteGenerale.class);
 
 	private static final long serialVersionUID = 1L;
 
 	@ManagedProperty("#{loginInfo}")
     private LoginInfo loginInfo;
 
 	@ManagedProperty("#{gestioneClienti.edited.id}")
 	private Integer id;
 
 	private String codice;
 	private String ragioneSociale;
 	private String partitaIva;
 	private String codiceFiscale;
 	private Integer codiceGruppoCliente;
 	private Integer codiceDivisa;
 	private Integer codiceTipoBusinessPartner;
 
 	private String contattoPrincipaleNome;
 	private String contattoPrincipaleTelefoni;
 	private String contattoPrincipaleFax;
 	private String contattoPrincipaleEmail;
 
 	private BigDecimal saldoContabile;
 
 	private String telefono1;
 	private String telefono2;
 	private String cellulare;
 	private String fax;
 	private String email;
 
 	private Boolean attivo;
 	private Date attivoDal;
 	private Date attivoAl;
 	private String attivoNote;
 
 	private Boolean bloccato;
 	private Date bloccatoDal;
 	private Date bloccatoAl;
 	private String bloccatoNote;
 
 	private String firstCodice;
 	private String lastCodice;
 
 	private List<GruppoCliente> listGruppoCliente;
 	private List<Divisa> listDivisa;
 	private List<TipoBusinessPartner> listTipoBusinessPartner;
 
 	private List<Cliente> conflicting;
 
 	private String note;
 
 	@PostConstruct
 	public void init() {
 
 		LUTService lutService = ServiceFactory.createService("LUT");
 
 		// Load gruppo cliente LUT.
 		//
 		listGruppoCliente = lutService.listItems("GruppoCliente");
 
 		// Load divisa LUT.
 		//
 		listDivisa = lutService.listItems("Divisa");
 
 		// Load tipo business partner LUT.
 		//
 		listTipoBusinessPartner = lutService.listItems("TipoBusinessPartner");
 
 		// If we are editing an existing record, it is time to fetch
 		// it from the database and fill in the bean fields.
 		//
 		if(id != null) {
 
 			ClienteService cs = ServiceFactory.createService("Cliente");
 			Cliente cliente = cs.retrieveDeep(id);
 
 			codice = cliente.getCodice();
 			ragioneSociale = cliente.getRagioneSociale();
 			partitaIva = cliente.getPartitaIva();
 			codiceFiscale = cliente.getCodiceFiscale();
 			codiceGruppoCliente = cliente.getGruppoCliente() != null ? cliente.getGruppoCliente().getId() : null;
 			codiceDivisa = cliente.getDivisa() != null ? cliente.getDivisa().getId() : null;
 			codiceTipoBusinessPartner = cliente.getTipoBusinessPartner() != null ? cliente.getTipoBusinessPartner().getId() : null;
 
 			telefono1 = cliente.getTelefono1();
 			telefono2 = cliente.getTelefono2();
 			cellulare = cliente.getCellulare();
 			fax = cliente.getFax();
 			email = cliente.getEmail();
 
 			attivo = cliente.getAttivo();
 			attivoDal = cliente.getAttivoDal();
 			attivoAl = cliente.getAttivoAl();
 			attivoNote = cliente.getAttivoNote();
 
 			bloccato = cliente.getBloccato();
 			bloccatoDal = cliente.getBloccatoDal();
 			bloccatoAl = cliente.getBloccatoAl();
 			bloccatoNote = cliente.getBloccatoNote();
 
			saldoContabile = cliente.getSaldoContabile();

 			// Load contact summary data.
 			//
 			loadRiepilogoContatto();
 		}
 	}
 
 	public void loadRiepilogoContatto() {
 
 		ContattoService cs = ServiceFactory.createService("Contatto");
 		Contatto contatto = cs.getDefault(id, true);
 
 		if(contatto == null) {
 			contattoPrincipaleNome = null;
 			contattoPrincipaleTelefoni = null;
 			contattoPrincipaleFax = null;
 			contattoPrincipaleEmail = null;
 		} else {
 			contattoPrincipaleNome = contatto.getNome();
 			contattoPrincipaleTelefoni = contatto.getAggregatoTelefoni();
 			contattoPrincipaleFax = contatto.getFax();
 			contattoPrincipaleEmail = contatto.getEmail();
 		}
 	}
 
 
 	public void generateCodice() {
 
 		// Create cliente service that will be used to query for codice.
 		//
 		ClienteService cs = ServiceFactory.createService("Cliente");
 		String[] codici = cs.retrieveNextCodice(loginInfo.getCodiceAzienda());
 		logger.debug(String.format("Found a pair of codes: [%s, %s]", codici[0], codici[1]));
 
 		lastCodice = codici[0];
 		firstCodice = codici[1];
 	}
 
 
 	public void doSelectFirstCodice() {
 
 		logger.debug(String.format("First codice chosen: %s", firstCodice));
 		codice = firstCodice;
 	}
 
 
 	public void doSelectLastCodice() {
 
 		logger.debug(String.format("Last codice chosen: %s", lastCodice));
 		codice = lastCodice;
 	}
 
 
 	public void doSaveForce() {
 
 		// Apply form-level validations (should already have been checked,
 		// but just in case).
 		//
 		if(!formValidations())
 			return;
 
 		// Persist data.
 		//
 		persistClienteData();
 	}
 
 
 	public void doSave() {
 
 		// Apply form-level validations.
 		//
 		if(!formValidations())
 			return;
 
 		// Before saving it is necessary to check if there is a conflict
 		// with a different record in terms of cf or piva.
 		//
 		if(checkPivaCfConflicts()) {
 
 			logger.debug("Conflicts detected.");
 
 			// Signal to open modal dialog.
 			//
 			RequestContext.getCurrentInstance().addCallbackParam("conflict", true);
 			return;
 		}
 
 		// Persist data.
 		//
 		persistClienteData();
 		logger.debug("Record successfully persisted.");
 
 		// Signal to caller that everything went fine.
 		//
 		RequestContext.getCurrentInstance().addCallbackParam("conflict", false);
 	}
 
 
 	public void prepareActivation() {
 
 		logger.debug("Inside prepareActivation method.");
 		note = null;
 	}
 
 
 	public void doToggleActivation() {
 
 		// Retrieve cliente service.
 		//
 		ClienteService cs = ServiceFactory.createService("Cliente");
 
 		// Toggle activation.
 		//
 		Cliente cliente = null;
 		if(attivo) {
 			cliente = cs.deactivate(id, note);
 		} else {
 			cliente = cs.activate(id, note);
 		}
 
 		// Update form fields.
 		//
 		attivo = cliente.getAttivo();
 		attivoDal = cliente.getAttivoDal();
 		attivoAl = cliente.getAttivoAl();
 		attivoNote = cliente.getAttivoNote();
 		bloccato = cliente.getBloccato();
 		bloccatoDal = cliente.getBloccatoDal();
 		bloccatoAl = cliente.getBloccatoAl();
 		bloccatoNote = cliente.getBloccatoNote();
 
 		// Signal to caller that everything went fine.
 		//
 		RequestContext.getCurrentInstance().addCallbackParam("ok", true);
 	}
 
 
 	private boolean checkPivaCfConflicts() {
 
 		// Create cliente service to check for conflicts.
 		//
 		ClienteService cs = ServiceFactory.createService("Cliente");
 
 		// If the record is being updated, and piva and cf have not been
 		// modified, this means that those values have already been forced and
 		// another confirmation is not required.
 		//
 		if(id != null) {
 
 			Cliente cliente = cs.retrieve(id);
 
 			if(
 					ObjectUtils.equals(cliente.getCodiceFiscale(), codiceFiscale) &&
 					ObjectUtils.equals(cliente.getPartitaIva(), partitaIva)) {
 
 				conflicting = new ArrayList<Cliente>();
 				return false;
 			}
 		}
 
 
 		// Retrieve the list of records having the same cf or the same piva
 		// in the currently selected azienda. If the record is being updated
 		// the check must be performed excluding the current one.
 		//
 		conflicting = cs.listConflicts(
 					loginInfo.getCodiceAzienda(),
 					id,
 					partitaIva,
 					codiceFiscale);
 		logger.debug(String.format("Found %d conflicting records.", conflicting.size()));
 
 		return conflicting.size() > 0;
 	}
 
 
 	private void persistClienteData() {
 
 		// Create cliente service to persist data.
 		//
 		ClienteService cs = ServiceFactory.createService("Cliente");
 
 		try {
 
 			Cliente cliente = null;
 
 			// If the record already exists, just update it.
 			//
 			if(id != null) {
 
 				cliente = cs.update(
 						id,
 						ragioneSociale,
 						partitaIva,
 						codiceFiscale,
 						codiceGruppoCliente,
 						codiceDivisa,
 						codiceTipoBusinessPartner,
 						telefono1,
 						telefono2,
 						cellulare,
 						fax,
 						email);
 
 				logger.debug("Entity successfully updated.");
 			}
 
 			// Otherwise create a new record.
 			//
 			else {
 
 				cliente = cs.create(
 						loginInfo.getCodiceAzienda(),
 						codice,
 						ragioneSociale,
 						partitaIva,
 						codiceFiscale,
 						codiceGruppoCliente,
 						codiceDivisa,
 						codiceTipoBusinessPartner,
 						telefono1,
 						telefono2,
 						cellulare,
 						fax,
 						email);
 				id = cliente.getId();
 
 				logger.debug("Entity successfully created.");
 
 			}
 
 			// Update form fields.
 			//
 			attivo = cliente.getAttivo();
 			attivoDal = cliente.getAttivoDal();
 			attivoAl = cliente.getAttivoAl();
 			attivoNote = cliente.getAttivoNote();
 
 			bloccato = cliente.getBloccato();
 			bloccatoDal = cliente.getBloccatoDal();
 			bloccatoAl = cliente.getBloccatoAl();
 			bloccatoNote = cliente.getBloccatoNote();
 
 			// Everything went fine.
 			//
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_INFO,
 					"Successo",
 					"Il salvataggio dei dati si è concluso con successo.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 
 		} catch(Exception e) {
 
 			logger.warn("Exception caught while saving entity.", e);
 
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Errore di sistema",
 					"Si è verificato un errore in fase di salvataggio del record.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 		}
 	}
 
 
 	private boolean formValidations() {
 
 		// Normalize fields.
 		//
 		codiceFiscale = codiceFiscale != null ? codiceFiscale.toUpperCase() : null;
 
 		// What goes into codice fiscale and partita IVA depends on the selection made
 		// on tipo business partner field.
 		//
 		if(codiceTipoBusinessPartner == TipoBusinessPartner.SOCIETA) {
 
 			// In this case Partita IVA is mandatory.
 			//
 			if(StringUtils.isEmpty(partitaIva)) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per una società la partita IVA è un campo obbligatorio.");
 				FacesContext.getCurrentInstance().addMessage("partitaIva", message);
 				return false;
 			}
 
 		} else if (codiceTipoBusinessPartner == TipoBusinessPartner.PRIVATO) {
 
 			// Codice fiscale is mandatory.
 			//
 			if(StringUtils.isEmpty(codiceFiscale)) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per un privato il campo codice fiscale è un campo obbligatorio.");
 				FacesContext.getCurrentInstance().addMessage("codiceFiscale", message);
 				return false;
 			}
 
 			// Codice cannot be a partita IVA code.
 			//
 			if(!ValidationUtil.isCodiceFiscale(codiceFiscale)) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per un privato è necessario introdurre il codice fiscale personale (alfanumerico).");
 				FacesContext.getCurrentInstance().addMessage("codiceFiscale", message);
 				return false;
 			}
 
 		} else if (codiceTipoBusinessPartner == TipoBusinessPartner.ENTE) {
 
 			// Codice fiscale is mandatory.
 			//
 			if(StringUtils.isEmpty(codiceFiscale)) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per un ente il campo codice fiscale è un campo obbligatorio.");
 				FacesContext.getCurrentInstance().addMessage("codiceFiscale", message);
 				return false;
 			}
 
 			// Codice fiscale must be represented by a partita IVA code.
 			//
 			if(!ValidationUtil.isPartitaIva(codiceFiscale)) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per un ente non è possibile specificare il codice fiscale personale (alfanumerico).");
 				FacesContext.getCurrentInstance().addMessage("codiceFiscale", message);
 				return false;
 			}
 
 			// The code must start with 8 or 9.
 			//
 			if(!codiceFiscale.startsWith("8") && !codiceFiscale.startsWith("9")) {
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_ERROR,
 						"Errore",
 						"Per un ente il codice fiscale deve iniziare per 8 o per 9.");
 				FacesContext.getCurrentInstance().addMessage("codiceFiscale", message);
 				return false;
 			}
 
 		} else {
 
 			String msg = String.format("Unexpected value detected for codiceTipoBusinessPartner: %d", codiceTipoBusinessPartner);
 			logger.error(msg);
 			throw new RuntimeException(msg);
 		}
 
 		return true;
 	}
 
 	public LoginInfo getLoginInfo() {
 		return loginInfo;
 	}
 
 	public void setLoginInfo(LoginInfo loginInfo) {
 		this.loginInfo = loginInfo;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getCodice() {
 		return codice;
 	}
 
 	public void setCodice(String codice) {
 		this.codice = codice;
 	}
 
 	public String getRagioneSociale() {
 		return ragioneSociale;
 	}
 
 	public void setRagioneSociale(String ragioneSociale) {
 		this.ragioneSociale = ragioneSociale;
 	}
 
 	public String getPartitaIva() {
 		return partitaIva;
 	}
 
 	public void setPartitaIva(String partitaIva) {
 		this.partitaIva = partitaIva;
 	}
 
 	public String getCodiceFiscale() {
 		return codiceFiscale;
 	}
 
 	public void setCodiceFiscale(String codiceFiscale) {
 		this.codiceFiscale = codiceFiscale;
 	}
 
 	public Integer getCodiceGruppoCliente() {
 		return codiceGruppoCliente;
 	}
 
 	public void setCodiceGruppoCliente(Integer codiceGruppoCliente) {
 		this.codiceGruppoCliente = codiceGruppoCliente;
 	}
 
 	public Integer getCodiceDivisa() {
 		return codiceDivisa;
 	}
 
 	public void setCodiceDivisa(Integer codiceDivisa) {
 		this.codiceDivisa = codiceDivisa;
 	}
 
 	public Integer getCodiceTipoBusinessPartner() {
 		return codiceTipoBusinessPartner;
 	}
 
 	public void setCodiceTipoBusinessPartner(Integer codiceTipoBusinessPartner) {
 		this.codiceTipoBusinessPartner = codiceTipoBusinessPartner;
 	}
 
 	public String getContattoPrincipaleNome() {
 		return contattoPrincipaleNome;
 	}
 
 	public void setContattoPrincipaleNome(String contattoPrincipaleNome) {
 		this.contattoPrincipaleNome = contattoPrincipaleNome;
 	}
 
 	public String getContattoPrincipaleTelefoni() {
 		return contattoPrincipaleTelefoni;
 	}
 
 	public void setContattoPrincipaleTelefoni(String contattoPrincipaleTelefoni) {
 		this.contattoPrincipaleTelefoni = contattoPrincipaleTelefoni;
 	}
 
 	public String getContattoPrincipaleFax() {
 		return contattoPrincipaleFax;
 	}
 
 	public void setContattoPrincipaleFax(String contattoPrincipaleFax) {
 		this.contattoPrincipaleFax = contattoPrincipaleFax;
 	}
 
 	public String getContattoPrincipaleEmail() {
 		return contattoPrincipaleEmail;
 	}
 
 	public void setContattoPrincipaleEmail(String contattoPrincipaleEmail) {
 		this.contattoPrincipaleEmail = contattoPrincipaleEmail;
 	}
 
 	public BigDecimal getSaldoContabile() {
 		return saldoContabile;
 	}
 
 	public void setSaldoContabile(BigDecimal saldoContabile) {
 		this.saldoContabile = saldoContabile;
 	}
 
 	public String getTelefono1() {
 		return telefono1;
 	}
 
 	public void setTelefono1(String telefono1) {
 		this.telefono1 = telefono1;
 	}
 
 	public String getTelefono2() {
 		return telefono2;
 	}
 
 	public void setTelefono2(String telefono2) {
 		this.telefono2 = telefono2;
 	}
 
 	public String getCellulare() {
 		return cellulare;
 	}
 
 	public void setCellulare(String cellulare) {
 		this.cellulare = cellulare;
 	}
 
 	public String getFax() {
 		return fax;
 	}
 
 	public void setFax(String fax) {
 		this.fax = fax;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Boolean getAttivo() {
 		return attivo;
 	}
 
 	public void setAttivo(Boolean attivo) {
 		this.attivo = attivo;
 	}
 
 	public Date getAttivoDal() {
 		return attivoDal;
 	}
 
 	public void setAttivoDal(Date attivoDal) {
 		this.attivoDal = attivoDal;
 	}
 
 	public Date getAttivoAl() {
 		return attivoAl;
 	}
 
 	public void setAttivoAl(Date attivoAl) {
 		this.attivoAl = attivoAl;
 	}
 
 	public String getAttivoNote() {
 		return attivoNote;
 	}
 
 	public void setAttivoNote(String attivoNote) {
 		this.attivoNote = attivoNote;
 	}
 
 	public Boolean getBloccato() {
 		return bloccato;
 	}
 
 	public void setBloccato(Boolean bloccato) {
 		this.bloccato = bloccato;
 	}
 
 	public Date getBloccatoDal() {
 		return bloccatoDal;
 	}
 
 	public void setBloccatoDal(Date bloccatoDal) {
 		this.bloccatoDal = bloccatoDal;
 	}
 
 	public Date getBloccatoAl() {
 		return bloccatoAl;
 	}
 
 	public void setBloccatoAl(Date bloccatoAl) {
 		this.bloccatoAl = bloccatoAl;
 	}
 
 	public String getBloccatoNote() {
 		return bloccatoNote;
 	}
 
 	public void setBloccatoNote(String bloccatoNote) {
 		this.bloccatoNote = bloccatoNote;
 	}
 
 	public List<GruppoCliente> getListGruppoCliente() {
 		return listGruppoCliente;
 	}
 
 	public void setListGruppoCliente(List<GruppoCliente> listGruppoCliente) {
 		this.listGruppoCliente = listGruppoCliente;
 	}
 
 	public List<Divisa> getListDivisa() {
 		return listDivisa;
 	}
 
 	public void setListDivisa(List<Divisa> listDivisa) {
 		this.listDivisa = listDivisa;
 	}
 
 	public List<TipoBusinessPartner> getListTipoBusinessPartner() {
 		return listTipoBusinessPartner;
 	}
 
 	public void setListTipoBusinessPartner(
 			List<TipoBusinessPartner> listTipoBusinessPartner) {
 		this.listTipoBusinessPartner = listTipoBusinessPartner;
 	}
 
 	public String getFirstCodice() {
 		return firstCodice;
 	}
 
 	public void setFirstCodice(String firstCodice) {
 		this.firstCodice = firstCodice;
 	}
 
 	public String getLastCodice() {
 		return lastCodice;
 	}
 
 	public void setLastCodice(String lastCodice) {
 		this.lastCodice = lastCodice;
 	}
 
 	public List<Cliente> getConflicting() {
 		return conflicting;
 	}
 
 	public void setConflicting(List<Cliente> conflicting) {
 		this.conflicting = conflicting;
 	}
 
 	public String getNote() {
 		return note;
 	}
 
 	public void setNote(String note) {
 		this.note = note;
 	}
 }
