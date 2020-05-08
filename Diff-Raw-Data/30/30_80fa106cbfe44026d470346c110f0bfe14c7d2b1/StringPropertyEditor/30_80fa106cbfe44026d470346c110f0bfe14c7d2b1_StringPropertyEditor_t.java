 package zz.utils.ui.propertyeditors;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.JTextField;
 
 import zz.utils.properties.IProperty;
 import zz.utils.properties.IPropertyListener;
 import zz.utils.properties.IRWProperty;
 
 public class StringPropertyEditor
 {
 	@SuppressWarnings("serial")
 	public static class TextField extends SimplePropertyEditor<String>
 	implements ActionListener, FocusListener
 	{
 		private final JTextField itsTextField = new JTextField();
 		
 		public TextField(IRWProperty<String> aProperty)
 		{
 			super(aProperty);
 			setLayout(new BorderLayout());
 			add(itsTextField, BorderLayout.CENTER);
 			itsTextField.addActionListener(this);
 			itsTextField.addFocusListener(this);
 		}
 		
 		public void focusGained(FocusEvent aE)
 		{
 		}
 	
 		public void focusLost(FocusEvent aE)
 		{
 			uiToProperty();
 		}
 	
 		public void actionPerformed(ActionEvent aE)
 		{
 			uiToProperty();
 		}
 		
 		@Override
		protected void propertyToUi(String aValue)
		{
			itsTextField.setText(aValue);
		}
		
		@Override
 		protected void uiToProperty()
 		{
 			getProperty().set(itsTextField.getText());
 		}
 	}
 }
