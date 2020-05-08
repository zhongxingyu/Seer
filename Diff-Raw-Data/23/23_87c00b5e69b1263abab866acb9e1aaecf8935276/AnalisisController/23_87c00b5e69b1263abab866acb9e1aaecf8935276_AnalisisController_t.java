 package ar.edu.utn.frba.proyecto.controller;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.model.SelectableDataModel;
 
 import ar.edu.utn.frba.proyecto.dao.impl.AnalisisDao;
 import ar.edu.utn.frba.proyecto.datamodel.AnalisisDataModel;
 import ar.edu.utn.frba.proyecto.domain.Analisis;
 import ar.edu.utn.frba.proyecto.domain.AuditObject;
 import ar.edu.utn.frba.proyecto.domain.Criterio;
 import ar.edu.utn.frba.proyecto.domain.Paso;
 
 @ManagedBean
 @ViewScoped
 public class AnalisisController extends BaseAbmController<Analisis> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3805173284502287061L;
 	
 	private Criterio[] selectedCriterios;
 
 	private List<Criterio> refreshedCriterios;
 	
 	@ManagedProperty("#{analisisDao}")
 	private AnalisisDao analisisDao;
 
 	@ManagedProperty("#{criterioController}")
 	private CriterioController criterioController;
 
 	@Override
 	protected AnalisisDao getDao() {
 		return this.analisisDao;
 	}
 	
 	private Analisis selectedAnalisis = new Analisis();
 	
 	public List<Analisis> filterProducts(String query){
 		List<Analisis> resultList = new ArrayList<Analisis>();
 		
 		List<Analisis> analisisList = getItems();
 		for ( Analisis analisis : analisisList){
 			if ( analisis.getNombre().startsWith(query)){
 				resultList.add(analisis);
 			}
 		}
 		
 		return resultList;
 	}
 	
 	public void refreshCurrentCriterios(){
 		setRefreshedCriterios(getCriterioController().getCriteriosByAnalisis(getSelectedAnalisis()));
 	}
 	@Override
 	public void addItem(){
 		if ( getSelectedCriterios().length > 0){
 			super.addItem();
 			
 		} else {
 			String confirmMessage = "Debes seleccionar al menos un criterio";
 			FacesContext.getCurrentInstance().addMessage(getAddMessageKey(),
 					new FacesMessage(FacesMessage.SEVERITY_FATAL, confirmMessage, null));
 		}
 	}
 	
//	@Override
//	public void deleteItems(){
//		for ( Analisis analisis : getSelectedItems()){
//			getCriterioController().removeCriteriosFromAnalisis(new Analisis(analisis.getId(), null));
//		}
//		
//		super.deleteItems();
//	}
 
 	@Override
 	protected void extraAddItemProcess() {
 		getCriterioController().addCriteriosToAnalisis(getCurrentItem(), getSelectedCriterios());
 		super.extraAddItemProcess();
 	}
 	
 	@Override
 	protected void extraGetItemProcess(Analisis analisis) {
 		analisis.setCriterios(getCriterioController().getCriteriosByAnalisis(analisis));
 	}
 	
 	@Override
 	protected void extraGetItemsProcess(){
 		for (Analisis analisis : getItems()) {
 			extraGetItemProcess(analisis);
 		}
 	}
 	
 	@Override
 	protected void extraResetCurrentProcess() {
 		setSelectedCriterios(new Criterio[4]);
 	}
 
 	@Override
 	protected Analisis newBaseItem() {
 		return new Analisis(0, "");
 	}
 
 	@Override
 	protected Analisis newBaseItem(Analisis item) {
 		return new Analisis(item);
 	}
 
 	@Override
 	protected boolean isDifferent() {
 		return !getOriginalSelectedItem().getNombre().equals(getSelectedItem().getNombre()) ||
 				hasCriteriosChanged();
 	}
 	
 	@Override
 	protected void extraUpdateItemProcess() {
 		
 		if (hasCriteriosChanged()) {
 			if (getSelectedCriterios().length > 0) {
 				getCriterioController().removeCriteriosFromAnalisis(getSelectedItem());
 				getCriterioController().addCriteriosToAnalisis(getSelectedItem(),getSelectedCriterios());
 			} else {
 				String confirmMessage = "Debes seleccionar al menos un criterio";
 				FacesContext.getCurrentInstance().addMessage(getUpdateMessageKey(),
 						new FacesMessage(FacesMessage.SEVERITY_FATAL,
 								confirmMessage, null));
 				return;
 			}
 		}
 		super.extraUpdateItemProcess();
 	}
 
 	private boolean hasCriteriosChanged() {
 		return !(getOriginalSelectedItem().getCriterios().containsAll(getSelectedItemProfilesAsList()) &&
 				getOriginalSelectedItem().getCriterios().size() == getSelectedCriterios().length);
 	}
 	
 	private List<Criterio> getSelectedItemProfilesAsList() {
 		if ( getSelectedCriterios() != null && getSelectedCriterios().length > 0)
 			return Arrays.asList(getSelectedCriterios());
 		
 		return new ArrayList<Criterio>();
 	}
 
 	public void setAnalisisDao(AnalisisDao analisisDao) {
 		this.analisisDao = analisisDao;
 	}
 
 	@Override
 	protected SelectableDataModel<Analisis> newDataModel(List<Analisis> all) {
 		return new AnalisisDataModel(all);
 	}
 
 	/**
 	 * @return the selectedCriterios
 	 */
 	public Criterio[] getSelectedCriterios() {
 		if ( this.selectedCriterios == null){
 			this.selectedCriterios = new Criterio[getCriterioController().getItems().size()];
 		}
 		return selectedCriterios;
 	}
 	
 	public Criterio[] getAnalisisCriteriosAsArray(){
 		return selectedItem.getCriterios().toArray(new Criterio[50]);
 	}
 
 	public String[] getValuesFromCriteriosAsArray(){
 		List<Criterio> criterios = getSelectedAnalisis().getCriterios();
 		String[] resultArray = new String[criterios.size()];
 		for (int i = 0; i < criterios.size(); i++){
 			resultArray[i] = criterios.get(i).getValorEsperado();
 		}
 		return resultArray;
 	}
 
 	/**
 	 * @param selectedCriterios the selectedCriterios to set
 	 */
 	public void setSelectedCriterios(Criterio[] selectedCriterios) {
 		this.selectedCriterios = selectedCriterios;
 	}
 
 	public CriterioController getCriterioController() {
 		return criterioController;
 	}
 
 	public void setCriterioController(CriterioController criterioController) {
 		this.criterioController = criterioController;
 	}
 
 	/**
 	 * @return the refreshedCriterios
 	 */
 	public List<Criterio> getRefreshedCriterios() {
 		return refreshedCriterios;
 	}
 
 	/**
 	 * @param refreshedCriterios the refreshedCriterios to set
 	 */
 	public void setRefreshedCriterios(List<Criterio> refreshedCriterios) {
 		this.refreshedCriterios = refreshedCriterios;
 	}
 
 	/**
 	 * @return the selectedAnalisis
 	 */
 	public Analisis getSelectedAnalisis() {
 		return selectedAnalisis;
 	}
 
 	/**
 	 * @param selectedAnalisis the selectedAnalisis to set
 	 */
 	public void setSelectedAnalisis(Analisis selectedAnalisis) {
 		this.selectedAnalisis = selectedAnalisis;
 	}
 
 	public List<Analisis> getAnalisisByPaso(Paso paso) {
 		return getDao().getAnalisisByPasoT(paso);
 	}
 	
 }
