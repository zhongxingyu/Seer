 package lector.client.admin.langedit;
 
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 
 public class Texto extends Composite {
 
 	private Label lblNewLabel;
 	private TextBox textBox;
 
 	/**
 	 * @wbp.parser.constructor
 	 */
 	public Texto() {
 		
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setSpacing(5);
 		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		initWidget(horizontalPanel);
		horizontalPanel.setSize("100%", "100%");
 		
 		lblNewLabel = new Label("New label");
 		horizontalPanel.add(lblNewLabel);
 		
 		textBox = new TextBox();
 		textBox.setVisibleLength(30);
 		horizontalPanel.add(textBox);
		textBox.setWidth("100%");
 	}
 
 	public Texto(String lblNewLabelt) {
 		
 		HorizontalPanel horizontalPanel = new HorizontalPanel();
 		horizontalPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
 		horizontalPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		initWidget(horizontalPanel);
 		horizontalPanel.setSize("100%", "100%");
 		
 		lblNewLabel = new Label(lblNewLabelt);
 		horizontalPanel.add(lblNewLabel);
 		
 		textBox = new TextBox();
 		textBox.setVisibleLength(30);
 		horizontalPanel.add(textBox);
 		textBox.setWidth("100%");
 	}
 	
 	public Label getLblNewLabel() {
 		return lblNewLabel;
 	}
 	
 	public TextBox getTextBox() {
 		return textBox;
 	}
 	
 	public void setLblNewLabel(String lblNewLabel) {
 		this.lblNewLabel.setText(lblNewLabel);
 	}
 	
 	public void setTextBox(String textBox) {
 		this.textBox.setText(textBox);
 	}
 	
 	public boolean isEquals(String texto)
 	{
 		return lblNewLabel.getText().equals(texto);
 	}
 	
 	public String getText() {
 		return textBox.getText();
 	}
 	
 	public String getLabel()
 	{
 		return lblNewLabel.getText();
 	}
 }
