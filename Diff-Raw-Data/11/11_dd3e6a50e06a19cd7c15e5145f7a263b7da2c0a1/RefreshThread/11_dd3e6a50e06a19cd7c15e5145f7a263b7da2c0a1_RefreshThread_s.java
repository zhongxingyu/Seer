 /**
  * Project_VASE Connect package
  */
 package vase.client.connect;
 
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.SwingUtilities;
 
 import com.vmware.vim25.VirtualMachineSummary;
 import com.vmware.vim25.mo.VirtualMachine;
 
 import vase.client.ThreadExt;
 
 /**
  * Refresh Thread Class
  * <br />
  * Thread is created after the GuiMain is constructed, then started
  * whenever an update is needed. The RefreshWorker class starts an instance of
  * this class at the interval specified on the REFRESH_INTERVAL
  * @author James McNatt & Brenton Kapral
  * @version Project_VASE Connect
  */
 public class RefreshThread extends ThreadExt
 {
 	private GuiMain main;
 	
 	/**
 	 * Main Constructor
 	 * @param main the GuiMain instance containing attributes needed to be refreshed
 	 */
 	public RefreshThread(GuiMain main)
 	{
 		this.main = main;
 	}
 	
 	/**
 	 * Run Method, runs the thread
 	 */
 	public void run()
 	{
 		SwingUtilities.invokeLater(new Runnable() 
 		{
 			public void run()
 			{
 				try
 				{
 					ArrayList<VirtualMachine> vms = main.engine.getVirtualMachines();
 					DefaultListModel model = (DefaultListModel) main.jListVMs.getModel();
 					int selection = main.jListVMs.getSelectionModel().getMinSelectionIndex();
 					
 					model.removeAllElements();
 					
 					for (VirtualMachine vm : vms)
 					{
						Object[] data = new Object[3];
 						VirtualMachineSummary summary = vm.getSummary();
 						try {data[0] = summary.config.name;} catch (Exception e) {data[0] = "Unknown";}
 						try {data[1] = summary.guest.guestFullName;} catch (Exception e) {data[1] = "Unknown";}
 						try {data[2] = summary.runtime.powerState.name();} catch (Exception e) {data[2] = "Unknown";}
 						model.addElement(data);
 					}
 					
 					if (selection <= model.getSize() - 1)
 					{
 						main.jListVMs.getSelectionModel().setSelectionInterval(selection, selection);
 					}
 				}
 				
 				catch (RuntimeException e)
 				{
 					ProjectConstraints.LOG.printStackTrace(e);
 					main.engine.disconnect();
 				}
 				
 				catch (Exception e)
 				{
 					ProjectConstraints.LOG.printStackTrace(e);
 				}
 			}
 		});
 	}
 }
