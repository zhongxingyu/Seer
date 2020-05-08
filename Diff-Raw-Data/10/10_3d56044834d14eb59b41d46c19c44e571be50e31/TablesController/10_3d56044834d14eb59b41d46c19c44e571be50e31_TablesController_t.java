 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package restaurante.Controller;
 
 /**
  *
  * @author Matheus
  */
 
 import restaurante.DaoPersistence.TablesDao;
 import restaurante.Beans.Tables;
 import java.sql.SQLException;
 import java.util.Date;
 import restaurante.DaoPersistence.BranchesDao;
 import restaurante.Utilities.Functions;
 
 public class TablesController extends AbstractController<Tables> implements JsonConvertible {
     
     public TablesController() {
         this.setNewRecord(true);
     }
     
     /*
      * Save the current bean in database
      */
     public int saveRecord(int tablenumber, Integer numberofchairs, 
             Tables.Status status, Date reservation, int id_branch, String reservedTo, String phoneOfReserved) throws ClassNotFoundException, SQLException, BeanValidationException, Exception {
 
         Tables table;
         table = findById(tablenumber);
         if(table == null) {
             table = new Tables();
         }
         table.setNumberOfChairs(numberofchairs);
         table.setStatus(status);
         if(status == Tables.Status.RESERVED) {
             table.setReservation(reservation);
             table.setPhoneOfReservedFor(phoneOfReserved);
             table.setReservedFor(reservedTo);
         } else {
             table.setReservation(new Date(0));
             table.setPhoneOfReservedFor("");
             table.setReservedFor("");
         }
         table.setBranch(new BranchesDao().findById(id_branch));
         validate(table);
         
         if(tablenumber == 0) {
             int id = new TablesDao().insert(table);
             setNewRecord(false);
             return id;
         } else {
             table.setTableNumber(tablenumber);
             new TablesDao().update(table);
             return tablenumber;
         }
     }
     
     /*
      * Save the current bean in database
      */
     public int saveOnlyPosition(int tablenumber, Integer x, Integer y, int id_branch) throws ClassNotFoundException, SQLException, BeanValidationException, Exception {
         Tables tables = null;
         try {
             tables = findById(tablenumber);
        } catch (Exception ex) { }
         if(tables == null) {
             tables = new Tables();
         }
         
         tables.setPositionX(x);
         tables.setPositionY(y);        
         
         if(tablenumber == 0) {
             tables.setNumberOfChairs(4);
             tables.setStatus(Tables.Status.FREE);
             tables.setReservation(Functions.stringSqlToDate("0000-00-00 00:00:00"));
             tables.setBranch(new BranchesDao().findById(id_branch));
             validate(tables);
             int id = new TablesDao().insert(tables);
             setNewRecord(false);
             return id;
         } else {
             validate(tables);
             new TablesDao().update(tables);
             return tablenumber;
         }
     }
     
     public void saveOnlyStatus(int tableNumber, int status) throws Exception {
         Tables table = null;
         try {
             table = findById(tableNumber);
         } catch (Exception ex) {
             throw new Exception("Impossible to find this table.");
         }
         table.setStatus(Tables.Status.values()[status]);
         new TablesDao().update(table);
     }
     
     /*
      * List all Tables registered
      */
     @Override
     public Tables[] list() throws Exception {
         TablesDao dao = new TablesDao();
         return dao.listAll();
     }
     
     /*
      * Find for a Tables using id
      */
     @Override
     public Tables findById(int id) throws Exception {
         TablesDao dao = new TablesDao();
         setNewRecord(false);
         return dao.findById(id);
     }
 
     /*
      * Find for a Tables using SQL LIKE
      */
     @Override
     public Tables[] findFor(Object findFor) throws Exception {
         return new TablesDao().listAll(findFor.toString(), TablesDao.FIELDS[2], TablesDao.FIELDS[1]);
     }
     
     /*
      * delete the id
      */
     @Override
     public void delete(int id) throws ClassNotFoundException, SQLException {
         setNewRecord(true);
         new TablesDao().delete(id);
     }
 
     @Override
     protected void validate(Tables register) throws BeanValidationException {
         String errorMsg = "";
         boolean hasError = false;
         if(register.getPositionX() < 0 || register.getPositionY() < 0) {
             errorMsg = "Posição inválida \n";
             hasError = true;
         }
         if(register.getPositionX() < 0 || register.getPositionY() < 0) {
             errorMsg = "Posição inválida \n";
             hasError = true;
         }
         if(register.getBranch() == null) {
             errorMsg = "Selecione uma filial! \n";
             hasError = true;
         }
         if(!isNewRecord()) {
             if(register.getNumberOfChairs() < 1) {
                 errorMsg = "Selecione uma filial! \n";
                 hasError = true;
             }
             if(register.getStatus() == null) {
                 errorMsg = "Selecione uma situação para a mesa! \n";
                 hasError = true;
             } else if(register.getStatus() == Tables.Status.RESERVED) {
                 if(register.getReservation() == null) {
                     errorMsg = "Digite um horário e data para a reserva! \n";
                     hasError = true;
                 }
                 if(register.getReservedFor() == null || register.getReservedFor().length() < 4) {
                     errorMsg = "Digite o nome de quem marcou a reserva! \n";
                     hasError = true;
                 }
                 if(register.getPhoneOfReservedFor() == null || register.getPhoneOfReservedFor().length() < 12) {
                     errorMsg = "Digite o telefone de quem marcou a reserva! \n";
                     hasError = true;
                 }
             }
         }
         if(hasError) {
             throw new BeanValidationException(errorMsg);
         }
     }
     
     @Override
     public String listToJson(String token) { 
         try {
             Tables[] list = this.list();
             return toJson(token, list);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
     
     @Override
     public String findToJson(String findFor, String token) { 
         try {
             Tables[] list = this.findFor(findFor);
             return toJson(token, list);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
     
     @Override
     public String beanToJson(int id, String token) { 
         try {
             Tables register = this.findById(id);
             return toJson(token, register);
         } catch (Exception ex) {
             return toJson(token, ex.getMessage(), true);
         }
     }
 }
