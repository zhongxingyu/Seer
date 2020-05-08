 package gui;
 import javax.swing.JDialog;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import cal_master.Communicator;
 import calendar.CalendarResponses;
 import calendar.Event;
 
 public class SliderPane{
 
 	private JSlider _slider;
 	private JLabel _label;
 	private JPanel _contentPane;
 	private JFrame _parent;
 	private JOptionPane _optionPane;
 	private int _duration = 60;
 	private JDialog _dialog;
 	private CalendarGui _gui;
 
 	public SliderPane(int numHours, CalendarGui gui){
 
 		_gui = gui;
 		_dialog = new JDialog();
 		_optionPane = new JOptionPane();
 		_slider = new JSlider();
 		_label = new JLabel("Hello");
 		_parent = new JFrame();
 		_contentPane = new JPanel();
 		_contentPane.add(_label);
 		_slider.addChangeListener(new SlideListener());
 		_slider.setMinorTickSpacing(15);
 		//		_slider.setMajorTickSpacing(15);
 		_slider.setValue(60);
 		_slider.setMinimum(15);
 		_slider.setMaximum(60*numHours);
 		_slider.setPaintTicks(false);
 		_slider.setPaintLabels(false);
 		_slider.setSnapToTicks(true);
 		//		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 
 		_optionPane.setMessage(new Object[] { "How long is the event you are planning? ", _slider , _label});
 		_optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
		_optionPane.setOptionType(JOptionPane.OK_OPTION);
 
 		//		JOptionPane.showInput
 
 		//		_optionPane.setVisible(true);
 		_dialog = _optionPane.createDialog(_parent, "Event Length");
 		_dialog.pack();
 		_dialog.setVisible(true);
 		_dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		_dialog.setResizable(true);
 		//		_optionPane.show
 
 	}
 
 
 	private class SlideListener implements ChangeListener{
 		@Override
 
 		public void stateChanged(ChangeEvent changeEvent) {
 			JSlider source = (JSlider) changeEvent.getSource();
 //			if (!source.getValueIsAdjusting()) {
 				_duration = (int) source.getValue();
 				_label.setText(Integer.toString (((int) source.getValue())/60)
 						+ " hrs " + Integer.toString(((int) source.getValue())%60) + " min");
 				_gui.setBestTimes(_duration);
 //			}
 		}
 
 	}
 
 
 
 	//	public static void main(final String[] args) {
 	//		SliderPane s = new SliderPane(5);
 	//	}
 
 
 
 }
