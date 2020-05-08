 package gui.comps;
 
 import java.awt.*;
 import javax.swing.*;
 
 /**
  * ȡʱ
  * 
  * @author lqy
  * 
  */
 public class TimePanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * ʱСʱ
 	 */
 	protected JTextField hourText;
 	/**
 	 * ʱķӲ
 	 */
 	protected JTextField minuteText;
 
 	public TimePanel() {
 		GridBagLayout layout = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		this.setLayout(layout);
 
 		// Сʱ
 		hourText = new JTextField("0", 2);
 		c.gridy = 0;
 		c.gridheight = GridBagConstraints.REMAINDER;
 		c.weightx = 0.5;
 		c.fill = GridBagConstraints.BOTH;
 		layout.setConstraints(hourText, c);
 		this.add(hourText);
 
 		JLabel label = new JLabel("Сʱ");
 		c.weightx = 0.0;
 		layout.setConstraints(label, c);
 		this.add(label);
 
 		// ӷ
 		minuteText = new JTextField("3", 2);
 		c.weightx = 0.5;
 		layout.setConstraints(minuteText, c);
 		this.add(minuteText);
 
 		label = new JLabel("");
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.weightx = 0.0;
 		layout.setConstraints(label, c);
 		this.add(label);
 	}
 
 	/**
 	 *  Сʱ\
 	 * 
 	 * @param time
 	 *            ʱ:
 	 */
 	public void setTime(long time) {
 		time = time / (1000 * 60);
 		int minute = (int) (time % 60);
 		int hour = (int) (time / 60);
 		hourText.setText(Integer.toString(hour));// ԭСʱ
 		minuteText.setText(Integer.toString(minute));// ԭķ
 	}
 
 	/**
 	 * Сʱ\ּʱ
 	 * 
 	 * @return ʱ:
 	 */
 	public long getTime() {
		return Integer.parseInt(hourText.getText()) * (3600 * 1000L)
				+ Integer.parseInt(minuteText.getText()) * (60 * 1000L);
 	}
 
 }
