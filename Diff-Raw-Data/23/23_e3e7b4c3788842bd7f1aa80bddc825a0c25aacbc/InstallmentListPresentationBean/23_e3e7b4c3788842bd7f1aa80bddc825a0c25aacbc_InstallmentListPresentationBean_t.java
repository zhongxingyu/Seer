 package presentationLayer;
 
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.joda.money.Money;
 
 import util.Config;
 import util.Messages;
 import annotations.TransferObj;
 import businessLayer.Contract;
 import businessLayer.Installment;
 import java.io.Serializable;
 
 @Named("installmentListPB")
 @ConversationScoped
 public class InstallmentListPresentationBean implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1942889592688495534L;
 	@Inject
 	@TransferObj
 	private Contract contract;
 
 	public InstallmentListPresentationBean() {
 	}
 	
 	
 	public void validateInstallments(FacesContext context, UIComponent component, Object value) {
 		System.out.println("Validating...");
 
 		
 		Money sum = Money.zero(Config.currency);
 		for(Installment i : contract.getInstallments()){
 			
 			sum = sum.plus(i.getWholeTaxableAmount());
 		}
 		
		if (sum.compareTo(contract.getWholeTaxableAmount()) != 0) {
			System.out.println("Validazione Completata con fallimento: sum = " + sum + ", contract = " + contract.getWholeTaxableAmount());
 			throw new ValidatorException(Messages.getErrorMessage("err_instInvalidInstallmentSum"));
 		}
 		else{
 			System.out.println("Validazione Completata con sucesso");
 		}
 	}
 
 }
