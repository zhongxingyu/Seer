 package at.epu.DataAccessLayer;
 
 import at.epu.DataAccessLayer.DataModels.*;
 import at.epu.DataAccessLayer.DataModels.SQLModels.*;
 
 public class DALReal implements DALInterface {
 	@Override
 	public ContactDataModel getContactDataModel() {
 		return new SQLContactDataModel();
 	}
 
 	@Override
 	public CustomerDataModel getCustomerDataModel() {
 		return new SQLCustomerDataModel();
 	}
 
 	@Override
 	public OfferDataModel getOfferDataModel() {
 		return new SQLOfferDataModel();
 	}
 
 	@Override
 	public ProjectDataModel getProjectDataModel() {
 		return new SQLProjectDataModel();
 	}
 
 	@Override
 	public BillDataModel getBillDataModel() {
 		return new SQLBillDataModel();
 	}
 
 	@Override
 	public BankAccountDataModel getBankAccountDataModel() {
 		return new SQLBankAccountDataModel();
 	}
 }
