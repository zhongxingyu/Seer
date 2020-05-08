 package controllerLayer;
 
 import java.io.Serializable;
 import java.util.Date;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ConversationScoped;
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.inject.Produces;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import annotations.Current;
 import annotations.TransferObj;
 import businessLayer.AgreementInstallment;
 import businessLayer.Contract;
 import businessLayer.Installment;
 
 @Named("installmentManager")
 @ConversationScoped
 public class InstallmentManagerBean implements Serializable {
 
 	protected static final long serialVersionUID = 1L;
 
 	@Inject
 	@TransferObj
 	protected Contract contract;
 
 	@Inject
 	@Current
 	private ContractHelper helper;
 
 	// TODO spostare return indirizzo pagina
 	protected Installment selectedInstallment;
 	protected Installment transferObjInstallment;
 	protected Installment installment;
 	
 	@PostConstruct
 	public void init(){
 		addInstallment();
 	}
 
 
 	public Installment getSelectedInstallment() {
 		return selectedInstallment;
 	}
 
 
 	public void setSelectedInstallment(Installment selectedInstallment) {
 		System.out.println("weofiwoifwfwef setSelInst");
 		this.selectedInstallment = selectedInstallment;
 	}
 
 
 	public void cancel() {
		System.out.println("Cancelling");
 		close();
 	}
 
 
 	public void save() {
 
 		// installment.copy(transferObjInstallment);
 
 		if (selectedInstallment == null) {
 			System.out.println("Saving new installment");
 			installment.copy(transferObjInstallment);
 			// agreement.getInstallments().add(installment);
 			contract.addInstallment(installment);
 		} else {
 			System.out.println("Saving installment with ID: " + selectedInstallment);
 			selectedInstallment.copy(transferObjInstallment);
 		}
 
 		close();
 	}
 
 
 	public void close() {
 
 		selectedInstallment = null;
 	}
 
 
 	@Produces
 	@TransferObj
 	@RequestScoped
 	public Installment getTransferObjInstallment() {
 
 		return transferObjInstallment;
 	}
 
 
 	public Installment getInstallment() {
 		return transferObjInstallment;
 	}
 
 
 	protected void initInstallment() {
 
 		installment = helper.getNewInstallment();
 		transferObjInstallment = helper.getNewInstallment();
 		transferObjInstallment.copy(selectedInstallment);
 
 	}
 
 
 	// TODO riunire?
 	public void viewInstallment(Installment inst) {
 		setSelectedInstallment(inst);
 		initInstallment();
 	}
 
 
 	public void editInstallment(Installment inst) {
 		System.out.println("Edit inst with ID: " + inst.getId());
 		setSelectedInstallment(inst);
 		initInstallment();
 	}
 
 
 	public void deleteInstallment() {
 
 		// agreement.getInstallments().remove(selectedInstallment);
 		contract.removeInstallment(selectedInstallment);
 		selectedInstallment.setContract(null);
 		close();
 	}
 
 
 	protected void insertRandomValues(AgreementInstallment inst) {
 		// TODO eliminare
 
 		inst.setDate(new Date());
 		inst.setInvoiceDate(new Date());
 		inst.setVoucherDate(new Date());
 
 	}
 
 
 	public String addInstallment() {
 		installment = helper.getNewInstallment();
 		transferObjInstallment = helper.getNewInstallment();
 		transferObjInstallment.setContract(contract);
 		transferObjInstallment.initShareTableFromContract(contract);
 		
 		// insertRandomValues(transferObjInstallment); //TODO eliminare
 		return "/installmentWiz.xhtml";
 	}
 
 }
