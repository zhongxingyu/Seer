 package de.unisiegen.gtitool.ui.model;
 
 
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableColumn;
 
 import de.unisiegen.gtitool.core.entities.Alphabet;
 
 
 /**
  * The <code>ConsoleColumnModel</code> for the error and warning tables
  * 
  * @author Benjamin Mies
  * @version $Id$
  */
 public class MachineColumnModel extends DefaultTableColumnModel
 {
 
   /**
    * The serial version uid.
    */
   private static final long serialVersionUID = 8539044321404059407L;
 
 
   /**
    * Allocates a new <code>ConsoleColumnModel</code>.
    */
   public MachineColumnModel ( Alphabet alphabet )
   {
     TableColumn column;
     column = new TableColumn ( 0 );
    column.setHeaderValue ( "" ); 
     this.addColumn ( column );
 
     for ( int i = 0 ; i < alphabet.symbolSize () ; i++ )
     {
       column = new TableColumn ( i + 1 );
       column.setHeaderValue ( alphabet.getSymbol ( i ).toString () ); 
       this.addColumn ( column );
     }
   }
 }
