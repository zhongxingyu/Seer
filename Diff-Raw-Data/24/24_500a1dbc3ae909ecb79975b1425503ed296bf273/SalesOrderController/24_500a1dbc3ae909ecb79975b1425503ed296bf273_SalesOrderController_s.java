 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.Controller;
 
 import java.sql.SQLException;
 import java.util.Date;
 import restaurante.Beans.Products;
 import restaurante.Beans.Saleable;
 import restaurante.Beans.SalesOrder;
 import restaurante.Beans.SalesOrderItems;
 import restaurante.DaoPersistence.BranchesDao;
 import restaurante.DaoPersistence.CustomersDao;
 import restaurante.DaoPersistence.EmployeesDao;
 import restaurante.DaoPersistence.SalesOrderDao;
 import restaurante.DaoPersistence.TablesDao;
 import restaurante.Utilities.ContentValues;
 import restaurante.Utilities.Functions;
 
 /**
  *
  * @author Alan
  */
 public class SalesOrderController extends AbstractController<SalesOrder> implements JsonConvertible {
     private double currentTotalValue = 0;
     private double currentTotalPerPerson = 0;
     
     public SalesOrderController() {
         this.setNewRecord(true);
     }
 
     @Override
     public void setNewRecord(boolean newRecord) {
         super.setNewRecord(newRecord);
         if(newRecord) {
             setCurrentTotalValue(0);
             setCurrentTotalPerPerson(0);
         }
     }
     
     @Override
     protected void validate(SalesOrder register) throws BeanValidationException {
         String errorMsg = "";
         boolean hasError = false;
         if(register.getCustomer() == null) {
             register.getCustomer().setId(-1);
         } else if(register.getCustomer().getId() <= 0) {
             errorMsg = "Selecione um cliente válido! \n";
             hasError = true;
         }
         if(register.getEmployee() == null || register.getEmployee().getId() <= 0) {
             errorMsg = "Selecione um funcionário válido! \n";
             hasError = true;
         }
         if(register.getBranch() == null || register.getBranch().getId() <= 0) {
             errorMsg = "Filial não selecionada.";
             hasError = true;
         }
         if(register.getTable() == null  || register.getTable().getTableNumber() <= 0) {
             errorMsg = "Selecione uma mesa.";
             hasError = true;
         }
         if(register.getDate() == null || register.getDate().getTime() == 0) {
             errorMsg = "Digite uma data válida.";
             hasError = true;
         }
         if(register.getTotalValue() <= 0) {
             errorMsg = "Valor inválido.";
             hasError = true;
         }
         if(register.getPaymentMethod() == null) {
             errorMsg = "Selecione uma forma de pagamento.";
             hasError = true;
         }
         for(SalesOrderItems item : register.getItems()) {
             if(item.getSaleable().getType() == Saleable.Type.PRODUCT &&
                     !((Products)item.getSaleable()).isFinal()) {
                 errorMsg = "O Produto " + item.getSaleable().getId() + " - " + 
                         ((Products)item.getSaleable()).getName() + " não é"
                         + " um produto final.";
                 hasError = true;    
             }
         }
         if(hasError) {
             throw new BeanValidationException(errorMsg);
         }
     }
     
     /*
      * Save the current bean in database
      */
     public int saveRecord(Integer id, int customer, int employee, int table, 
             int branch, Date date, double totalValue, int totalPersons,
             int paymentMethod, int status, SalesOrderItems[] items) throws SQLException, BeanValidationException, ClassNotFoundException, Exception {
 
         SalesOrder register = new SalesOrder();
                        
         register.setCustomer(new CustomersDao().findById(customer));
         register.setEmployee(new EmployeesDao().findById(employee));
         register.setBranch(new BranchesDao().findById(branch));
         register.setTable(new TablesDao().findById(table));
         
         register.setDate(date);
         register.setTotalValue(totalValue);
         register.setNumberOfPersons(totalPersons);
         register.setValueForPerson(totalValue / totalPersons);
         register.setPaymentMethod(SalesOrder.PaymentMethod.values()[paymentMethod]);
         register.setStatus(SalesOrder.Status.values()[status]);
         register.setItems(items);
         validate(register);
         
         if(isNewRecord()) {
             id = new SalesOrderDao().insert(register);
             setNewRecord(false);
             return id;
         } else {
             register.setId(id);
             new SalesOrderDao().update(register);
             return id;
         }
     }
     
     public int saveRecord(SalesOrder register) throws BeanValidationException, ClassNotFoundException, SQLException, Exception {
         validate(register);
         if(register.getId() == 0) {
             int id = new SalesOrderDao().insert(register);
             setNewRecord(false);
             return id;
         } else {
             new SalesOrderDao().update(register);
             return register.getId();
         }
     }
     
     /*
      * List all Costumer registered
      */
     @Override
     public SalesOrder[] list() throws Exception {
         SalesOrderDao dao = new SalesOrderDao();
         return dao.listAll();
     }
     
     /*
      * Find for a costumer using id
      */
     @Override
     public SalesOrder findById(int id) throws Exception {
         SalesOrderDao dao = new SalesOrderDao();
         setNewRecord(false);
         return dao.findById(id);
     }
 
     /*
      * Find for a costumer using SQL LIKE
      */
     @Override
     public SalesOrder[] findFor(Object findFor) throws Exception {
         SalesOrderDao dao = new SalesOrderDao();
         return dao.listAll(findFor.toString(), SalesOrderDao.FIELDS[0], SalesOrderDao.FIELDS[1]);
     }
     
     /*
      * delete the id
      */
     @Override
     public void delete(int id) throws ClassNotFoundException, SQLException {
         setNewRecord(true);
         new SalesOrderDao().delete(id);
     }
     
     @Override
     public String listToJson(String token) { 
         try {
             SalesOrder[] list = this.list();
             return toJson(token, list);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
     
     @Override
     public String findToJson(String findFor, String token) { 
         try {
             SalesOrder[] list = this.findFor(findFor);
             return toJson(token, list);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
     
     @Override
     public String beanToJson(int id, String token) { 
         try {
             SalesOrder register = this.findById(id);
             return toJson(token, register);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
 
     public SalesOrder[] findByDate(Date date) throws Exception {
         String dateSql = Functions.dateToDateStringSql(date).substring(0, 10);
         String where = SalesOrderDao.FIELDS[4] + " LIKE ?";
         SalesOrderDao dao = new SalesOrderDao();
         ContentValues[] values = dao.select(where, dateSql);
         return dao.arrayOfContentValuesToArrayOfBeans(values);
     }
     
     public SalesOrder[] findBetweenDates(Date dateBefore, Date dateAfter) throws Exception {
         String dateBeforeSql = Functions.dateToDateStringSql(dateBefore);
         String dateAfterSql = Functions.dateToDateStringSql(dateAfter);
         String where = SalesOrderDao.FIELDS[4] + " > ? AND " + SalesOrderDao.FIELDS[4] + " < ?";
         SalesOrderDao dao = new SalesOrderDao();
         ContentValues[] values = dao.select(where, dateBeforeSql, dateAfterSql);
         return dao.arrayOfContentValuesToArrayOfBeans(values);
     }
 
     /**
      * @return the currentTotalValue
      */
     public double getCurrentTotalValue() {
         return currentTotalValue;
     }
 
     /**
      * @param currentTotalValue the currentTotalValue to set
      */
     public void setCurrentTotalValue(double currentTotalValue) {
         this.currentTotalValue = currentTotalValue;
     }
 
     /**
      * @return the currentNumberOfPersons
      */
     public double getCurrentTotalPerPerson() {
         return currentTotalPerPerson;
     }
 
     /**
      * @param currentNumberOfPersons the currentNumberOfPersons to set
      */
     public void setCurrentTotalPerPerson(double currentTotalPerPerson) {
         this.currentTotalPerPerson = currentTotalPerPerson;
     }
 }
