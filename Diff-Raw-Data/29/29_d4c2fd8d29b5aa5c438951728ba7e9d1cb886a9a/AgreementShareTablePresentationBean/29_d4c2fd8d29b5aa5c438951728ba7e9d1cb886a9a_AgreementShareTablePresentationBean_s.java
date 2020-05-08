 package presentationLayer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import util.InvalidValueException;
 import util.MathUtil;
 import util.Messages;
 import annotations.TransferObj;
 import businessLayer.Agreement;
 import businessLayer.ChiefScientist;
 
 @Named("agrShareTablePB")
 @ConversationScoped
 public class AgreementShareTablePresentationBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject @TransferObj private Agreement agreement;
 	@Inject private AgreementShareTableBuilder builder;
 
 	private PersonnelShare selectedShare;
 	private PersonnelShare newShare;
 
 	private float tempPersonnel;
 	private float tempGoodsAndServicesSubfield;
 
 	public AgreementShareTablePresentationBean() {
 		newShare = new PersonnelShare();
 	}
 
 	@PostConstruct
 	public void init() {
 		updateMainValues();
 		// FIXME non basta metterlo nel postConstruct
 	}
 
 	public List<PersonnelShare> getShares() {
 		List<PersonnelShare> result = new ArrayList<>();
 		Map<ChiefScientist, Float> shares = agreement.getShareTable().getSharePerPersonnel();
 		for (ChiefScientist chief : shares.keySet()) {
 			result.add(new PersonnelShare(chief, shares.get(chief)));
 		}
 		return result;
 	}
 
 	public void addShare() {
 		System.out.println("Adding share " + newShare);
 		agreement.getShareTable().getSharePerPersonnel().put(newShare.chiefScientist, newShare.value);
 		System.out.println("Share added: \n\t" + agreement.getShareTable().getSharePerPersonnel());
 		newShare = new PersonnelShare();
 	}
	
	public void removeShare(PersonnelShare share){
 		agreement.getShareTable().getSharePerPersonnel().remove(share.chiefScientist);
 	}
 
 	public PersonnelShare getSelectedShare() {
 		return selectedShare;
 	}
 
 	public void setSelectedShare(PersonnelShare selectedValue) {
 		this.selectedShare = selectedValue;
 	}
 
 	public PersonnelShare getNewShare() {
 		return newShare;
 	}
 
 	public Set<ChiefScientist> getAddedChiefs() {
 		return agreement.getShareTable().getSharePerPersonnel().keySet();
 	}
 
 	public float getAtheneumCapitalBalance() {
 		return agreement.getShareTable().getAtheneumCapitalBalance();
 	}
 
 	public float getAtheneumCommonBalance() {
 		return agreement.getShareTable().getAtheneumCommonBalance();
 	}
 
 	public float getStructures() {
 		return agreement.getShareTable().getStructures();
 	}
 
 	public float getPersonnel() {
 		return agreement.getShareTable().getPersonnel();
 	}
 
 	public float getGoodsAndServices() {
 		return agreement.getShareTable().getGoodsAndServices();
 	}
 
 	public float getBusinessTrip() {
 		return agreement.getShareTable().getBusinessTrip();
 	}
 
 	public float getConsumerMaterials() {
 		return agreement.getShareTable().getConsumerMaterials();
 	}
 
 	public float getInventoryMaterials() {
 		return agreement.getShareTable().getInventoryMaterials();
 	}
 
 	public float getRentals() {
 		return agreement.getShareTable().getRentals();
 	}
 
 	public float getPersonnelOnContract() {
 		return agreement.getShareTable().getPersonnelOnContract();
 	}
 
 	public float getOtherCost() {
 		return agreement.getShareTable().getOtherCost();
 	}
 
 	void setAtheneumCapitalBalance(float atheneumCapitalBalance) {
 		agreement.getShareTable().setAtheneumCapitalBalance(atheneumCapitalBalance);
 	}
 
 	void setAtheneumCommonBalance(float atheneumCommonBalance) {
 		agreement.getShareTable().setAtheneumCommonBalance(atheneumCommonBalance);
 	}
 
 	void setStructures(float structures) {
 		agreement.getShareTable().setStructures(structures);
 	}
 
 	public void setPersonnel(float personnel) {
 		agreement.getShareTable().setPersonnel(personnel);
 		System.out.println("Quota personale aggiornata: " + personnel);
 	}
 
 	public void setBusinessTrip(float businessTrip) {
 		agreement.getShareTable().setBusinessTrip(businessTrip);
 	}
 
 	public void setConsumerMaterials(float consumerMaterials) {
 		agreement.getShareTable().setConsumerMaterials(consumerMaterials);
 	}
 
 	public void setInventoryMaterials(float inventoryMaterials) {
 		agreement.getShareTable().setInventoryMaterials(inventoryMaterials);
 	}
 
 	public void setRentals(float rentals) {
 		agreement.getShareTable().setRentals(rentals);
 	}
 
 	public void setPersonnelOnContract(float personnelOnContract) {
 		agreement.getShareTable().setPersonnelOnContract(personnelOnContract);
 	}
 
 	public float getTempPersonnel() {
 		return tempPersonnel;
 	}
 
 	private void updateMainValues() {
 		try {
 			// il try-catch-throw è necessario anche qui perché il metodo non
 			// viene chiamato solo in seguito alla validazione: potrebbero
 			// esserci valori scorretti che non derivano dall'input dell'utente
 			// (e.g., il file di configurazione è errato)
 			builder.build();
 		} catch (InvalidValueException e) {
 			throw new IllegalStateException("Incorrect agreement share table values");
 		}
 
 		System.out.println("Campi aggiornati: athCapBal = " + getAtheneumCapitalBalance() + ", athCommonBal = " + getAtheneumCommonBalance()
 				+ ", structures = " + getStructures());
 		try {
 			agreement.getShareTable().updateGoodsAndServices();
 		} catch (InvalidValueException e) {
 			// l'eccezione dovrebbe essere già stata gestita precedentemente. Se
 			// viene lanciata c'è qualcosa che non va
 			throw new RuntimeException("An exception was thrown when it should have already been managed.");
 		}
 	}
 
 	public void validatePersonnel(FacesContext context, UIComponent component, Object value) {
 		if (value instanceof Double) {
 			tempPersonnel = ((Double) value).floatValue();
 		} else if (value instanceof Long) {
 			tempPersonnel = ((Long) value).floatValue();
 		} else {
 			throw new IllegalStateException("Inserted input is not a number");
 		}
 
 		try {
 			updateMainValues();
 		} catch (IllegalStateException e) {
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 
 	}
 
 	private void initTempSubfield(Object value) {
 		if (value instanceof Double) {
 			tempGoodsAndServicesSubfield = ((Double) value).floatValue();
 		} else if (value instanceof Long) {
 			tempGoodsAndServicesSubfield = ((Long) value).floatValue();
 		} else {
 			throw new IllegalStateException("Inserted input is not a number");
 		}
 	}
 
 	public void validateBusinessTrip(FacesContext context, UIComponent component, Object value) {
 		initTempSubfield(value);
 		float tmp = getBusinessTrip();
 		try {
 			setBusinessTrip(tempGoodsAndServicesSubfield);
 			agreement.getShareTable().updateOtherCosts();
 		} catch (InvalidValueException e) {
 			setBusinessTrip(tmp);
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 	}
 
 	public void validateRentals(FacesContext context, UIComponent component, Object value) {
 		initTempSubfield(value);
 		float tmp = getRentals();
 		try {
 			setRentals(tempGoodsAndServicesSubfield);
 			agreement.getShareTable().updateOtherCosts();
 		} catch (InvalidValueException e) {
 			setRentals(tmp);
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 	}
 
 	public void validateInventoryMat(FacesContext context, UIComponent component, Object value) {
 		initTempSubfield(value);
 		float tmp = getInventoryMaterials();
 		try {
 			setInventoryMaterials(tempGoodsAndServicesSubfield);
 			agreement.getShareTable().updateOtherCosts();
 		} catch (InvalidValueException e) {
 			setInventoryMaterials(tmp);
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 	}
 
 	public void validateConsumerMat(FacesContext context, UIComponent component, Object value) {
 		initTempSubfield(value);
 		float tmp = getConsumerMaterials();
 		try {
 			setConsumerMaterials(tempGoodsAndServicesSubfield);
 			agreement.getShareTable().updateOtherCosts();
 		} catch (InvalidValueException e) {
 			setConsumerMaterials(tmp);
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 	}
 
 	public void validatePersonnelOnContract(FacesContext context, UIComponent component, Object value) {
 		initTempSubfield(value);
 		float tmp = getPersonnelOnContract();
 		try {
 			setPersonnelOnContract(tempGoodsAndServicesSubfield);
 			agreement.getShareTable().updateOtherCosts();
 		} catch (InvalidValueException e) {
 			setPersonnelOnContract(tmp);
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTableInvalidInput"));
 		}
 	}
	
 	public void validatePersonnelShares(FacesContext context, UIComponent component, Object value) {
 		Map<ChiefScientist, Float> shares = agreement.getShareTable().getSharePerPersonnel();
 		float sum = 0F;
		for(float f : shares.values()){
 			sum += f;
 		}
		if(!MathUtil.doubleEquals(sum, 100.0)){
 			throw new ValidatorException(Messages.getErrorMessage("err_shareTablePersonnel"));
 		}
 	}
	
//	public void validatePersonnelShareChief(FacesContext context, UIComponent component, Object value) {
//		if(null == value){
//			throw new ValidatorException(new FacesMessage("Chief cannot be null"));
//		}
//	}
 
 	public float getPercentOfMainField(float field) {
 		return agreement.getWholeAmount() * field / 100;
 	}
 
 	public float getPercentOfGoodsField(float field) {
 		return getPercentOfMainField(getGoodsAndServices()) * field / 100;
 	}
 
 	public class PersonnelShare {
 		private ChiefScientist chiefScientist;
 		private float value;
 
 		public PersonnelShare(ChiefScientist chiefScientist, float value) {
 			this.chiefScientist = chiefScientist;
 			this.value = value;
 		}
 
 		public PersonnelShare() {
 			chiefScientist = null;
 			value = 0;
 		}
 
 		public ChiefScientist getChiefScientist() {
 			return chiefScientist;
 		}
 
 		public void setChiefScientist(ChiefScientist chiefScientist) {
 			System.out.println("Setting: " + chiefScientist);
 			this.chiefScientist = chiefScientist;
 		}
 
 		public float getValue() {
 			return value;
 		}
 
 		public void setValue(float value) {
 			System.out.println("Setting: " + value);
 			this.value = value;
 		}
 
 		public float getPercentValue() {
 			System.out.println(">>> Percent value called");
 			return getPercentOfMainField(getPersonnel()) * value / 100;
 		}
 
 		@Override
 		public String toString() {
 			return "PersonnelShare [chiefScientist=" + chiefScientist + ", value=" + value + "]";
 		}
 
 	}
 
 }
