 package com.github.croesch.partimana.view.listener;
 
 import com.github.croesch.partimana.PartiManaDefaultGUITestCase;
 import javax.swing.JFrame;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import org.fest.swing.core.MouseButton;
 import org.fest.swing.data.TableCell;
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.JTableFixture;
 import org.junit.Test;
 
 /**
  * Provides test cases for {@link TableSelectOnClickListener}.
  *
  * @author croesch
  * @since Date: Mar 17, 2013
  */
 public class TableSelectOnClickListenerGUITest extends PartiManaDefaultGUITestCase {
 
   private JTableFixture table;
 
   @Override
   protected void before() {
     this.table = GuiActionRunner.execute(new GuiQuery<JTableFixture>() {
       @Override
       protected JTableFixture executeInEDT() throws Throwable {
         final JTable table = new JTable(new Object[][] { new Object[] { "Müller", "Hans" },
                                                          new Object[] { "Müller", "Bernd" },
                                                          new Object[] { "Jansen", "Jörg" }, },
                                         new Object[] { "Name", "Vorname" });
         table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         table.addMouseListener(new TableSelectOnClickListener());
         final JTableFixture tableFixture = new JTableFixture(robot(), table);
         final JFrame f = new JFrame();
         f.add(table);
        f.setBounds(100, 100, 500, 500);
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         f.setVisible(true);
         return tableFixture;
       }
     });
     robot().waitForIdle();
   }
 
   @Test
   public void testSingleSelection() {
     this.table.click(TableCell.row(0).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(0);
     this.table.click(TableCell.row(1).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(1);
     this.table.click(TableCell.row(2).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(2);
     this.table.click(TableCell.row(0).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(0);
     this.table.click(TableCell.row(1).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(1);
     this.table.click(TableCell.row(2).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(2);
   }
 
   @Test
   public void testMultiSelection() {
     this.table.target.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
     this.table.click(TableCell.row(0).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(0);
     this.table.click(TableCell.row(1).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(1);
     this.table.click(TableCell.row(2).column(0), MouseButton.RIGHT_BUTTON).requireSelectedRows(2);
     this.table.click(TableCell.row(0).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(0);
     this.table.click(TableCell.row(1).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(1);
     this.table.click(TableCell.row(2).column(1), MouseButton.RIGHT_BUTTON).requireSelectedRows(2);
   }
 }
