 package database.control;
 
 import database.entity.Column;
 import database.entity.ColumnType;
 import database.entity.Entity;
 import database.entity.Table;
 import database.model.table.EntityTableModel;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 public class EntityController
         extends AbstractController {
     
     private Table workingTable;
     
     public EntityController ( ControllersHolder holder ) {
         super( holder );
     }
     
     public Table getWorkingTable () {
         return workingTable;
     }
     
     public void setWorkingTable ( Table workingTable ) {
         this.workingTable = workingTable;
     }
     
     /**
      * Creates table model from already exist table entity
      * @return table model
      */
     public EntityTableModel createEntityTableModel () {
         EntityTableModel model = new EntityTableModel( workingTable );
         model.addValues( workingTable.getData() );
         return model;
     }
     
     /**
      * Save data from table model to entity
      * @param entityTableModel
      * @return true - if action was successful. false - otherwise
      */
     public boolean saveData ( EntityTableModel entityTableModel ) {
         boolean result = false;
         List<Entity> data = entityTableModel.getData();
         
         if ( validate( data ) ) {
             workingTable.setData( data );
             result = true;
         }
         return result;
     }
     
     private boolean validate ( List<Entity> data ) {
        boolean result = false;
         
         for ( Entity entity : data ) {
             for ( Column column : workingTable.getColumns() ) {
                result = checkType( column, entity ) & result;
             }
         }
         
        return result;
     }
     
     private boolean checkType ( Column column, Entity entity ) {
         boolean isSimple = column.getSimpleType() != null;
         String value = entity.getProperties().get( column.getName() );
                 
         boolean isValid = false;
         
         if ( isSimple ) {
             isValid = column.getSimpleType().isValid( value );
             
         } else {
             List<String> pkValues = this.holder.getProvider().getPkValues( column.getComplexType() );
             for ( String pkValue : pkValues) {
                 if (pkValue.equals( value ) ) {
                     isValid = true;
                     break;
                 }
             }
         }
         
         if ( !isValid )
             this.holder.getErrors().addError( "Не вiрне значення: " + value );
         
         return isValid;
     }
 }
