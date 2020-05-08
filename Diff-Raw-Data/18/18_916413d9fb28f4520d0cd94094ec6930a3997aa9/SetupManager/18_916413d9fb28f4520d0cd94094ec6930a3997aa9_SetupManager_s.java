 package infoprivacy.simulator.gui;
 
 import infoprivacy.simulator.Processor;
 import infoprivacy.simulator.ProcessorSupervisor;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Date;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
  * This class handles the tab for the whole project setup.
  * 
  * @author Joseph Lewis <joehms22@gmail.com>
  *
  */
 public class SetupManager extends JPanel 
 {
 	public SetupManager()
 	{
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		
 		// Add buttons/forms for each input source
 		JButton fakeDataGenerator = new JButton("Generate Bad Random Route Data");
 		add(fakeDataGenerator);
 		fakeDataGenerator.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				double lastValue = 30;
 				
 				for(int i = 0; i < 100; i++)
 				{
 					Date d = new Date(i);
 					if(Math.random() < .33)
 					{
 						lastValue -= 5;
 						ProcessorSupervisor.getInstance().process(d, lastValue);
 						continue;
 					}
 					if(Math.random() > .66)
 					{
 						lastValue += 5;
 						ProcessorSupervisor.getInstance().process(d, lastValue);
 						continue;
 					}
 					ProcessorSupervisor.getInstance().process(d, lastValue);
 				}
 			}
 			
 		});
 		
 		JButton clearData = new JButton("Clear Output");
 		add(clearData);
 		clearData.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				OutputManager.getInstance().clearOutput();
 			}
 		});
 		
 		// Add enable/disable buttons for each processor
 		add(new JLabel("<html><h1>Processor Enable/Disables</h1></html>"));
 		for(Processor proc : ProcessorSupervisor.getInstance().getProcessors())
 		{
 			add(new ProcessorPanel(proc));
 		}
 	}
 }
