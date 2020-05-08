 package presentationLayer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.validator.ValidatorException;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import util.Messages;
 import annotations.TransferObj;
 import businessLayer.AbstractShareTable;
 import businessLayer.Agreement;
 import businessLayer.Installment;
 
 @Named("instShareTablePB")
 @ConversationScoped
 public class InstallmentShareTablePresentationBean extends ShareTablePresentationObj implements Serializable {
 	private static final long serialVersionUID = 1L;
 
 	@Inject
 	@TransferObj
 	private Installment installment;
 
 	public InstallmentShareTablePresentationBean() {
 		super();
 	}
 
 	@Override
 	protected AbstractShareTable getTransferObjShareTable() {
 		return installment.getShareTable();
 	}
 
 	@Override
 	protected float getTransfetObjWholeAmount() {
 		return installment.getWholeAmount();
 	}
 
 	public void validateWithOtherInstallments(FacesContext context, UIComponent component, Object value) {
		//TODO eliminare try-catch
 		try {
 			Agreement agr = installment.getAgreement();
 			List<Installment> installments = agr.getInstallments();
 			installments.add(installment);
 			List<List<Float>> instShareTablesMainAttributes = new ArrayList<>();
 			List<List<Float>> instShareTablesSubAttributes = new ArrayList<>();
 
 			System.out.println(1);
 
 			for (Installment i : installments) {
 				System.out.print("Rata" + i + " \n\t ");
 				instShareTablesMainAttributes.add(getMainAttributeList(i.getShareTable(), i.getWholeAmount()));
 				System.out.print("\t");
 				instShareTablesSubAttributes.add(getSubAttributeList(i.getShareTable(),
 						getPercentOf(i.getWholeAmount(), i.getShareTable().getGoodsAndServices())));
 			}
 
 			System.out.println(2);
 
 			System.out.print("Convenzione: ");
 			List<Float> l = getMainAttributeList(agr.getShareTable(), agr.getWholeAmount());
 			System.out.println(3);
 			System.out.print("\t");
 			List<Float> p = getSubAttributeList(agr.getShareTable(), getPercentOf(agr.getShareTable().getGoodsAndServices(), agr.getWholeAmount()));
 			System.out.println(4);
 
 			//devono essere nello stesso ordine in cui si aggiungono sotto! L'oscenità fatta codice
 			String[] mainAttr = { Messages.getString("shareTableCapitalBalance"), Messages.getString("shareTableCommonBalance"),
 					Messages.getString("shareTableStructures"), Messages.getString("shareTablePersonnel"), Messages.getString("shareTableGoods") };
 			String[] subAttr = { Messages.getString("shareTableTrip"), Messages.getString("shareTableInvMaterials"),
 					Messages.getString("shareTableConsMaterials"), Messages.getString("shareTableRentals"),
 					Messages.getString("shareTableContrPersonnel"), Messages.getString("shareTableOtherCosts") };
 
 			validateFields(l, instShareTablesMainAttributes, mainAttr);
 			System.out.println(5);
 			validateFields(p, instShareTablesSubAttributes, subAttr);
 			System.out.println(6);
 		} catch (Exception | Error e) {
 			System.out.println("Blbl " + e);
 			throw e;
 		}
 	}
 
 	private void validateFields(List<Float> agrAttributes, List<List<Float>> instAttrs, String[] attrNames) {
 		// XXX questo metodo e tutti quelli ad esso correlati sono osceni. Se si
 		// trova un meccanismo per non scrivere simili blasfemie è meglio
 
 		for (int i = 0; i < agrAttributes.size(); i++) {
 			float sum = 0F;
 			for (List<Float> l : instAttrs) {
 				sum += l.get(i);
 			}
 			if (sum > agrAttributes.get(i)) {
 				System.err.println("Errore sull'attributo '" + attrNames[i] + "': convenzione=" + agrAttributes.get(i) + ", rate=" + sum);
 				Object[] params = { attrNames[i] };
 				throw new ValidatorException(Messages.getErrorMessage("err_installmentShareTable", params));
 			}
 		}
 
 	}
 
 	private List<Float> getMainAttributeList(AbstractShareTable t, float wholeAmount) {
 		List<Float> attributes = new ArrayList<>();
 
 		attributes.add(getPercentOf(t.getAtheneumCapitalBalance(), wholeAmount));
 		attributes.add(getPercentOf(t.getAtheneumCommonBalance(), wholeAmount));
 		attributes.add(getPercentOf(t.getStructures(), wholeAmount));
 		attributes.add(getPercentOf(t.getPersonnel(), wholeAmount));
 		attributes.add(getPercentOf(t.getGoodsAndServices(), wholeAmount));
 
 		System.out.println(attributes);
 
 		return attributes;
 	}
 
 	private List<Float> getSubAttributeList(AbstractShareTable t, float wholeAmount) {
 		List<Float> attributes = new ArrayList<>();
 
 		attributes.add(getPercentOf(t.getBusinessTrip(), wholeAmount));
 		attributes.add(getPercentOf(t.getInventoryMaterials(), wholeAmount));
 		attributes.add(getPercentOf(t.getConsumerMaterials(), wholeAmount));
 		attributes.add(getPercentOf(t.getRentals(), wholeAmount));
 		attributes.add(getPercentOf(t.getPersonnelOnContract(), wholeAmount));
 		attributes.add(getPercentOf(t.getOtherCost(), wholeAmount));
 
 		System.out.println(attributes);
 
 		return attributes;
 	}
 }
