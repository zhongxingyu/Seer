 /**
  * 
  */
 package com.isesalud.controller.persistence;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.isesalud.ejb.persistence.PatientPersistenceEjb;
 import com.isesalud.ejb.persistence.PreviaEnfermedadPersistenceEjb;
 import com.isesalud.model.CancerOtrasPartes;
 import com.isesalud.model.Paciente;
 import com.isesalud.model.ParienteCancerMama;
 import com.isesalud.model.PreviaEnfermedad;
 import com.isesalud.model.SintomaCancerMama;
 import com.isesalud.support.components.BaseManagedCrudController;
 import com.isesalud.support.components.EditModeEnum;
 import com.isesalud.support.exceptions.BaseException;
 
 /**
  * @author Jesus Espinoza Hernandez
  *
  */
 @Named
 @ConversationScoped
 public class PatientPersistence extends BaseManagedCrudController<Paciente, PatientPersistenceEjb> {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8376685298021320691L;
 	
 	@EJB
 	private PatientPersistenceEjb manager;
 	
 	@EJB
 	private PreviaEnfermedadPersistenceEjb previaEnfermedadPersistenceEjb;
 	
 	@Inject
 	private Conversation conversation;
 	
 	/**************CHILD LIST*******************************************/
 	private PreviaEnfermedad previaEnfermedad;
 	private CancerOtrasPartes cancerOtrasPartes;
 	private ParienteCancerMama parienteCancerMama;
 	private SintomaCancerMama sintomaCancerMama;
 	
 	private List<PreviaEnfermedad> prevEnfList = new ArrayList<PreviaEnfermedad>();
 	private List<CancerOtrasPartes> cancerList = new ArrayList<CancerOtrasPartes>();
 	private List<ParienteCancerMama> parienteList = new ArrayList<ParienteCancerMama>();
 	private List<SintomaCancerMama> sintomaList = new ArrayList<SintomaCancerMama>();
 	
 	public PreviaEnfermedad getPreviaEnfermedad() {
 		return previaEnfermedad;
 	}
 	
 	public void setPreviaEnfermedad(PreviaEnfermedad previaEnfermedad) {
 		this.previaEnfermedad = previaEnfermedad;
 	}
 	
 	public CancerOtrasPartes getCancerOtrasPartes() {
 		return cancerOtrasPartes;
 	}
 	
 	public void setCancerOtrasPartes(CancerOtrasPartes cancerOtrasPartes) {
 		this.cancerOtrasPartes = cancerOtrasPartes;
 	}
 	
 	public ParienteCancerMama getParienteCancerMama() {
 		return parienteCancerMama;
 	}
 	
 	public void setParienteCancerMama(ParienteCancerMama parienteCancerMama) {
 		this.parienteCancerMama = parienteCancerMama;
 	}
 	
 	public List<PreviaEnfermedad> getPrevEnfList() {
 		return prevEnfList;
 	}
 	
 	public SintomaCancerMama getSintomaCancerMama() {
 		return sintomaCancerMama;
 	}
 	
 	public void setSintomaCancerMama(SintomaCancerMama sintomaCancerMama) {
 		this.sintomaCancerMama = sintomaCancerMama;
 	}
 	
 	public void setPrevEnfList(List<PreviaEnfermedad> prevEnfList) {
 		this.prevEnfList = prevEnfList;
 	}
 	
 	public List<CancerOtrasPartes> getCancerList() {
 		return cancerList;
 	}
 	
 	public void setCancerList(List<CancerOtrasPartes> cancerList) {
 		this.cancerList = cancerList;
 	}
 	
 	public List<ParienteCancerMama> getParienteList() {
 		return parienteList;
 	}
 	
 	public void setParienteList(List<ParienteCancerMama> parienteList) {
 		this.parienteList = parienteList;
 	}
 	
 	public List<SintomaCancerMama> getSintomaList() {
 		return sintomaList;
 	}
 	
 	public void setSintomaList(List<SintomaCancerMama> sintomaList) {
 		this.sintomaList = sintomaList;
 	}
 	/*************END CHILD LIST*****************************************/
 	
 	
 	@PostConstruct
 	public void init(){
 		if(conversation != null && conversation.isTransient()){
 			this.parienteCancerMama = new ParienteCancerMama();
 			this.cancerOtrasPartes = new CancerOtrasPartes();
 			this.previaEnfermedad = new PreviaEnfermedad();
 			this.sintomaCancerMama = new SintomaCancerMama();
 		}
 	}
 	
 	@Override
 	protected void doBeforeSave() throws BaseException {
 		super.doBeforeSave();
 		getModel().setParientescancermama(parienteList);
 		getModel().setCanceresOtraPartes(cancerList);
 		getModel().setPreviasenfermedades(prevEnfList);
 		getModel().setSintomasCancerMama(sintomaList);
 	}
 	
 	@Override
 	protected void doAfterAdd() throws BaseException {
 		if(conversation.isTransient())
 			conversation.begin();
 		super.doAfterAdd();
 	}
 	
 	@Override
 	protected void doAfterEdit() throws BaseException {
 		super.doAfterEdit();
 		if(conversation.isTransient())
 			conversation.begin();
 		
 		this.cancerList = getModel().getCanceresOtraPartes();
 		this.sintomaList = getModel().getSintomasCancerMama();
 		this.parienteList = getModel().getParientescancermama();
 		this.prevEnfList = getModel().getPreviasenfermedades();
 	}
 	
 	@Override
 	protected void doAfterSave() throws BaseException {
 		super.doAfterSave();
 		if(!conversation.isTransient())
 			conversation.end();
 	}
 	
 	@Override
 	protected void doAfterCancel() throws BaseException {
 		super.doAfterCancel();
 		if(!conversation.isTransient())
 			conversation.end();
 	}
 	
 	public void addCancerOtrasPartes(ActionEvent e){
 		if(this.cancerOtrasPartes.getParteCuerpo() != null && !this.cancerOtrasPartes.getParteCuerpo().isEmpty()){
 			this.cancerOtrasPartes.setPaciente(getModel());
 			this.cancerList.add(cancerOtrasPartes);
 			this.cancerOtrasPartes = new CancerOtrasPartes();
 		}
 	}
 	
 	public void addPreviaEnfermedad(ActionEvent e){
 		if(this.previaEnfermedad.getIllness() != null && !this.previaEnfermedad.getIllness().isEmpty()){
 			this.previaEnfermedad.setPaciente(getModel());
 			this.prevEnfList.add(previaEnfermedad);
 			this.previaEnfermedad = new PreviaEnfermedad();
 		}
 	}
 	
 	public void addParienteCancerMama(ActionEvent e){
 		if(this.parienteCancerMama.getRelative() != null && !this.parienteCancerMama.getRelative().isEmpty()){
 			this.parienteCancerMama.setPaciente(getModel());
 			this.parienteList.add(parienteCancerMama);
 			this.parienteCancerMama = new ParienteCancerMama();
 		}
 	}
 	
 	public void addSintomaCancerMama(ActionEvent e){
 		if(this.sintomaCancerMama.getSintomaNombre() != null && !this.sintomaCancerMama.getSintomaNombre().isEmpty()){
 			this.sintomaCancerMama.setPaciente(getModel());
 			this.sintomaList.add(sintomaCancerMama);
 			this.sintomaCancerMama = new SintomaCancerMama();
 		}
 	}
 	
 	@Override
 	public void edit(ActionEvent actionEvent) {
 		Paciente p = (Paciente) actionEvent.getComponent().getAttributes().get("selected");
 		setModel(p);
 		super.edit(actionEvent);
 	}
 	
 	public void erasePreviasEnfermedades(ActionEvent e){
 		PreviaEnfermedad p = (PreviaEnfermedad) e.getComponent().getAttributes().get("selectIllnes");
 		Iterator<PreviaEnfermedad> iter = this.prevEnfList.iterator();
 		while(iter.hasNext()){
 			PreviaEnfermedad i = iter.next();
 			if(p.equals(i)){
 				if(getEditMode() == EditModeEnum.EDITING){
					this.previaEnfermedadPersistenceEjb.delete(i.getId());
 					iter.remove();
 				} else {
 					iter.remove();
 				}
 			}
 		}
 	}
 	
 	public String navigate(){
 		return "home";
 	}
 
 	@Override
 	protected PatientPersistenceEjb getCrudManager() {
 		return this.manager;
 	}
 
 	@Override
 	protected Paciente getNewModel() {
 		return new Paciente();
 	}
 
 	@Override
 	protected String getAddRole() {
 		return null;
 	}
 
 	@Override
 	protected String getEditRole() {
 		return null;
 	}
 
 	@Override
 	protected String getDeleteRole() {
 		return null;
 	}
 
 	@Override
 	protected String getViewRole() {
 		return null;
 	}
 
 }
