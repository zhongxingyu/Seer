 package it.ivncr.erp.jsf.managedbean.commerciale.gestioneclienti;
 
 
 import it.ivncr.erp.jsf.RobustLazyDataModel;
 import it.ivncr.erp.model.commerciale.Contatto;
 import it.ivncr.erp.model.commerciale.TipoContatto;
 import it.ivncr.erp.service.QueryResult;
 import it.ivncr.erp.service.ServiceFactory;
 import it.ivncr.erp.service.SortDirection;
 import it.ivncr.erp.service.contatto.ContattoService;
 import it.ivncr.erp.service.lut.LUTService;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
 
 import org.primefaces.context.RequestContext;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 @ManagedBean
 @ViewScoped
 public class DettaglioClienteContatti implements Serializable {
 
 	private static final Logger logger = LoggerFactory.getLogger(DettaglioClienteContatti.class);
 
 	private static final long serialVersionUID = 1L;
 
 	@ManagedProperty("#{dettaglioClienteGenerale}")
 	private DettaglioClienteGenerale dettaglioClienteGenerale;
 
 	private LazyDataModel<Contatto> model;
 	private Contatto selected;
 
 	private Integer id;
 	private Integer codiceTipoContatto;
 	private String titolo;
 	private String nome;
 	private String telefono1;
 	private String telefono2;
 	private String cellulare;
 	private String fax;
 	private String email;
 
 	private List<TipoContatto> listTipoContatto;
 	private List<String> listTitoli;
 
 
 	// Used for default selection.
 	//
 	private List<Contatto> currentListContent;
 	private Integer codiceContattoPreferito;
 
 
 	public DettaglioClienteContatti() {
 
 		model = new RobustLazyDataModel<Contatto>() {
 
 			private static final long serialVersionUID = 1L;
 			@Override
 			public List<Contatto> load(
 					int first,
 					int pageSize,
 					String sortField,
 					SortOrder sortOrder,
 					Map<String, String> filters) {
 
 				logger.debug("Fetching data model.");
 
 				if(dettaglioClienteGenerale.getId() == null) {
 					return null;
 				}
 
 				// Inject codice cliente argument in applied filters map.
 				//
 				filters.put("codiceCliente", Integer.toString(dettaglioClienteGenerale.getId()));
 
 				ContattoService cs = ServiceFactory.createService("Contatto");
 				QueryResult<Contatto> result = cs.list(
 						first,
 						pageSize,
 						sortField,
 						SortDirection.fromSortOrder(sortOrder),
 						filters);
 
 				this.setRowCount(result.getRecords());
 
 				currentListContent = result.getResults();
 				return result.getResults();
 			}
 
 			@Override
 			public Object getRowKey(Contatto contatto) {
 
 				return contatto == null ? null : contatto.getId();
 			}
 
 			@Override
 			public Contatto getRowData(String rowKey) {
 
 				ContattoService cs = ServiceFactory.createService("Contatto");
 				Contatto contatto = cs.retrieveDeep(Integer.decode(rowKey));
 				return contatto;
 			}
 		};
 	}
 
 	@PostConstruct
 	public void init() {
 
 		LUTService lutService = ServiceFactory.createService("LUT");
 
 		// Load tipo contatto LUT.
 		//
 		listTipoContatto = lutService.listItems("TipoContatto");
 
 
 		ContattoService cs = ServiceFactory.createService("Contatto");
 		listTitoli = cs.listDistinctTitolo();
 
 		logger.debug("Initialization performed.");
 	}
 
 	private void clean() {
 
 		logger.debug("Cleaning form state.");
 
 		id = null;
 		codiceTipoContatto = null;
 		titolo = null;
 		nome = null;
 		telefono1 = null;
 		telefono2 = null;
 		cellulare = null;
 		fax = null;
 		email = null;
 	}
 
 	public void startCreate() {
 
 		logger.debug("Entering startCreate method.");
 
 		clean();
 	}
 
 	public void startUpdate() {
 
 		logger.debug("Entering startUpdate method.");
 
 		if(selected == null) {
 			String msg = "Unexpected null value detected for selected row.";
 			logger.error(msg);
 			throw new RuntimeException(msg);
 		}
 
 		// Reloading the entity is required to be sure that the value has not changed since it was
 		// read in the data table list of values.
 		//
 		ContattoService cs = ServiceFactory.createService("Contatto");
 		selected = cs.retrieveDeep(selected.getId());
 
 		id = selected.getId();
 		codiceTipoContatto = selected.getTipoContatto() != null ? selected.getTipoContatto().getId() : null;
 		titolo = selected.getTitolo();
 		nome = selected.getNome();
 		telefono1 = selected.getTelefono1();
 		telefono2 = selected.getTelefono2();
 		cellulare = selected.getCellulare();
 		fax = selected.getFax();
 		email = selected.getEmail();
 	}
	
	public void onContattoPreferitoChanged(ActionEvent e) {
		
        String param = (String) FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("clicked");
        if(param != null) {
        	codiceContattoPreferito = Integer.parseInt(param);
        }
	}
 
 	public void doSave() {
 
 		// Save the entity.
 		//
 		try {
 			ContattoService cs = ServiceFactory.createService("Contatto");
 
 			if(id == null) {
 				cs.create(
 						dettaglioClienteGenerale.getId(),
 						codiceTipoContatto,
 						titolo,
 						nome,
 						telefono1,
 						telefono2,
 						cellulare,
 						fax,
 						email);
 				logger.debug("Entity successfully created.");
 
 				// Add a message.
 				//
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_INFO,
 						"Record creato",
 						"La creazione del nuovo contatto si è conclusa con successo.");
 				FacesContext.getCurrentInstance().addMessage(null, message);
 
 			} else {
 
 				cs.update(
 						id,
 						codiceTipoContatto,
 						titolo,
 						nome,
 						telefono1,
 						telefono2,
 						cellulare,
 						fax,
 						email);
 				logger.debug("Entity successfully updated.");
 
 				// Add a message.
 				//
 				FacesMessage message = new FacesMessage(
 						FacesMessage.SEVERITY_INFO,
 						"Record aggiornato",
 						"La modifica del contatto si è conclusa con successo.");
 				FacesContext.getCurrentInstance().addMessage(null, message);
 			}
 
 			// Signal to modal dialog that everything went fine.
 			//
 			RequestContext.getCurrentInstance().addCallbackParam("ok", true);
 
 		} catch(Exception e) {
 
 			logger.warn("Exception caught while saving entity.", e);
 
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Errore di sistema",
 					"Si è verificato un errore in fase di salvataggio del record.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 		}
 	}
 
 
 	public void doDelete() {
 
 		if(selected == null || selected.getId() == null) {
 			String msg = "Unexpected null id detected.";
 			logger.error(msg);
 			throw new RuntimeException(msg);
 		}
 
 		// Delete the entity.
 		//
 		try {
 			ContattoService cs = ServiceFactory.createService("Contatto");
 			cs.delete(selected.getId());
 
 			logger.debug("Entity successfully deleted.");
 
 			// Add a message.
 			//
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_INFO,
 					"Record eliminato",
 					"L'eliminazione del contatto si è conclusa con successo.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 
 			// Signal to modal dialog that everything went fine.
 			//
 			RequestContext.getCurrentInstance().addCallbackParam("ok", true);
 
 		} catch(Exception e) {
 
 			logger.warn("Exception caught while deleting entity.", e);
 
 			FacesMessage message = new FacesMessage(
 					FacesMessage.SEVERITY_ERROR,
 					"Errore di sistema",
 					"Si è verificato un errore in fase di eliminazione del record.");
 			FacesContext.getCurrentInstance().addMessage(null, message);
 		}
 	}
 
 	public List<String> completeTitolo(String query) {
 
 		String q = query.toUpperCase();
 
 		List<String> result = new ArrayList<String>();
 
 		for(String s : listTitoli) {
 			if(s == null) {
 				continue;
 			}
 
 			if(s.toUpperCase().contains(q)) {
 				result.add(s);
 			}
 		}
 
 		return result;
 	}
 
 	public DettaglioClienteGenerale getDettaglioClienteGenerale() {
 		return dettaglioClienteGenerale;
 	}
 
 	public void setDettaglioClienteGenerale(
 			DettaglioClienteGenerale dettaglioClienteGenerale) {
 		this.dettaglioClienteGenerale = dettaglioClienteGenerale;
 	}
 
 	public LazyDataModel<Contatto> getModel() {
 		return model;
 	}
 
 	public void setModel(LazyDataModel<Contatto> model) {
 		this.model = model;
 	}
 
 	public Contatto getSelected() {
 		return selected;
 	}
 
 	public void setSelected(Contatto selected) {
 		this.selected = selected;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public Integer getCodiceTipoContatto() {
 		return codiceTipoContatto;
 	}
 
 	public void setCodiceTipoContatto(Integer codiceTipoContatto) {
 		this.codiceTipoContatto = codiceTipoContatto;
 	}
 
 	public String getTitolo() {
 		return titolo;
 	}
 
 	public void setTitolo(String titolo) {
 		this.titolo = titolo;
 	}
 
 	public String getNome() {
 		return nome;
 	}
 
 	public void setNome(String nome) {
 		this.nome = nome;
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
 
 	public List<TipoContatto> getListTipoContatto() {
 		return listTipoContatto;
 	}
 
 	public void setListTipoContatto(List<TipoContatto> listTipoContatto) {
 		this.listTipoContatto = listTipoContatto;
 	}
 
 	public List<Contatto> getCurrentListContent() {
 		return currentListContent;
 	}
 
 	public void setCurrentListContent(List<Contatto> currentListContent) {
 		this.currentListContent = currentListContent;
 	}
 
 	public Integer getCodiceContattoPreferito() {
 		return codiceContattoPreferito;
 	}
 
 	public void setCodiceContattoPreferito(Integer codiceContattoPreferito) {
 		this.codiceContattoPreferito = codiceContattoPreferito;
 	}
 }
