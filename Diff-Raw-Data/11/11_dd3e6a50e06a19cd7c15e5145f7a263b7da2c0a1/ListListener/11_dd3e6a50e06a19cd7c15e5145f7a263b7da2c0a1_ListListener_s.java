 /**
  * Project_VASE Connect package
  */
 package vase.client.connect;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.SwingUtilities;
 
 import com.vmware.vim25.mo.VirtualMachine;
 
 import vase.client.List;
 
 /**
  * Custom List MouseAdapter and ListSelectionListener class for List class
  * <br />
  * Bring up the Right-Click menu calls double click action
  * @author James McNatt & Brenton Kapral
  * @version Project_VASE Connect
  */
 public class ListListener extends MouseAdapter
 {
 	private CommandEngine engine;
 	private List list;
 	
 	/**
 	 * Main constructor
 	 * @param jlFolders the folders list
 	 * @param jlTemplates the templates list
 	 * @param jlVMs the vms list
 	 * @param engine the command engine for deployment commands
 	 */
 	public ListListener(List list, CommandEngine engine)
 	{
 		this.engine = engine;
 		this.list = list;
 	}
 	
 	/**
 	 * Displays the right-click menu if the mouse event was a right-click
 	 * @param event the mouse event
 	 */
 	public void mouseClicked(MouseEvent event)
 	{		
 		if (SwingUtilities.isRightMouseButton(event))
 		{
 			event.consume();
 			
 			try
 			{
 				list.setSelectedIndex(list.locationToIndex(event.getPoint()));
 				if (list.getSelectedIndex() != -1)
 				{
 					Object[] data = (Object[]) list.getSelectedValue();
 					PopupMenuVM menu = new PopupMenuVM(engine, (VirtualMachine) data[3]);
 					menu.show(event.getComponent(), event.getX(), event.getY());
 				}
 			}
 			
 			catch (Exception e)
 			{
 				ProjectConstraints.LOG.printStackTrace(e);
 			}
 		}
 		
 		else
 		{
 			if (event.getClickCount() == 2 && !event.isConsumed() && SwingUtilities.isLeftMouseButton(event))
 			{
 				event.consume();
 				Object vm = null;
				if (list.getSelectedIndex() != -1)
 				{
 					Object[] data = (Object[]) list.getModel().getElementAt(list.getSelectedIndex());
 					vm = data[3];
 				}
 				
 				if (vm != null)
 				{
 					engine.launchConsole((VirtualMachine) vm);
 				}
 			}
 		}
 	}
 }
