 import java.awt.*;
 import java.awt.event.*;
 
 public class ControlPanel extends Panel implements ActionListener {
 	private ArduinoInterface ai;

 	private Button ledOn;
 	private Button ledOff;
 	private Button getLight;

 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6969280129122570471L;

 	public ControlPanel(ArduinoInterface ai) {
 		this.ai = ai;

 		setLayout(new GridLayout(10, 1));
 		ledOn = new Button("Led on");
 		ledOn.addActionListener(this);
 		add(ledOn);

 		ledOff = new Button("Led off");
 		ledOff.addActionListener(this);
 		add(ledOff);
 
 		getLight = new Button("Get ligth val");
 		getLight.addActionListener(this);
		add(getLight);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == ledOn)
 			ai.ledOn();

 		if (ae.getSource() == ledOff)
			ai.ledOff();
 	}
 }
