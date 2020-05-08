 package net.cim.client.customer;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.sql.SQLException;
 
 import net.cim.client.AbstractSubModuleController;
 import net.cim.client.BeanListTableModel;
 import net.cim.client.MainController;
 import net.cim.client.data.DataRepository;
 import net.cim.client.data.model.Customer;
 
 public class CustomerListSubModuleController extends AbstractSubModuleController {
 
 	private final static String[] COLUMNS = new String[] {"ID", "Name", "Vorname"};
 	private final static String[] COLUMNS_PROPERTIES = new String[] {"id", "name", "firstName"};
 	
 	private BeanListTableModel<Customer> tableModel = new BeanListTableModel<Customer>(COLUMNS, Customer.class, COLUMNS_PROPERTIES);
 	private final DataRepository repository = DataRepository.getInstance();
 	
 	public CustomerListSubModuleController(MainController mainController) {
 		super(mainController,new CustomerListSubModuleView());
 		getView().addPropertyChangeListener(CustomerListSubModuleView.PROPERTY_EDIT_CUSTOMER, new PropertyChangeListener() {
 			@Override
 			public void propertyChange(PropertyChangeEvent evt) {
				if (getView().getCustomerTable().getSelectedRowCount() > 0){
				
					tableModel.getBeanList().get( getView().getCustomerTable().getSelectedRow());
					CustomerEditorSubModuleController aChildController = new CustomerEditorSubModuleController(getMainController(), tableModel.getBeanList().get( getView().getCustomerTable().getSelectedRow()));
					add(aChildController);
					getMainController().activate(aChildController);
				}
 			}
 		});
 		getView().getCustomerTable().setModel(tableModel);
 	}
 	
 	public CustomerListSubModuleView getView() {
 		return (CustomerListSubModuleView)super.getView();
 	}
 	
 	public void update() {
 		try {
 			tableModel.setBeanList(repository.getAll(Customer.class));
 		} catch (IllegalArgumentException | IllegalAccessException
 				| InstantiationException | SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public String getTitle() {
 		return "Kundenliste";
 	}
 	
 }
