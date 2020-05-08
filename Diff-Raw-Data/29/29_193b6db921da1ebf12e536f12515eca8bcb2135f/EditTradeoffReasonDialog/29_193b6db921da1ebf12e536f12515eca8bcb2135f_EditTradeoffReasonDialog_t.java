 package edu.cmu.square.client.ui.FinalProductSelection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 import edu.cmu.square.client.model.GwtModesType;
 import edu.cmu.square.client.model.GwtTradeoffReason;
 import edu.cmu.square.client.model.GwtTerm;
 
 import edu.cmu.square.client.utils.SquareUtil;
 
 public class EditTradeoffReasonDialog extends DialogBox
 {
 	
 	private final Label tradeoffReasonTextBox = new Label();
 
 	private GwtTradeoffReason current;
 	private FinalProductSelectionPane updateTradeoffReasonCommand;
 	private List<GwtTradeoffReason> listOfTradeoffReasons = new ArrayList<GwtTradeoffReason>();
 	final FinalProductSelectionMessages messages = (FinalProductSelectionMessages) GWT.create(FinalProductSelectionMessages.class);
 	private Button saveButton;
 	
 
 	public EditTradeoffReasonDialog(GwtTradeoffReason currentTradeoffReason, List<GwtTradeoffReason> tradeoffReasons, FinalProductSelectionPane command)
 		{
 			super();
 		
 			current = currentTradeoffReason;
 			this.listOfTradeoffReasons = tradeoffReasons;
 			this.updateTradeoffReasonCommand = command;
 
 			
 			if(command.getCurrentState().getMode() == GwtModesType.ReadWrite){
 				//System.out.println("****my access right is RW"+command.getCurrentState().getMode());
 				this.initializeDialogReadWrite(currentTradeoffReason);
 			}
 			else if (command.getCurrentState().getMode() == GwtModesType.ReadOnly){
 				//System.out.println("****my access right is RO"+command.getCurrentState().getMode());
 				this.initializeDialogReadOnly(currentTradeoffReason);
 			}
 			
 		}
 
 	/**
 	 * Sets up the controls in the dialog
 	 * 
 	 * @param SoftwarePackage
 	 *            The category to be updated in this dialog.
 	 */
 	private void initializeDialogReadWrite(GwtTradeoffReason tradeoffReason)
 	{
 
 		VerticalPanel baseLayout = new VerticalPanel();
 		VerticalPanel nameLayout = new VerticalPanel();
 		VerticalPanel descriptionLayout = new VerticalPanel();
 		HorizontalPanel buttonsLayout = new HorizontalPanel();
		this.setText(messages.editTradeoffReasonDialogBoxTitleReadOnly());
		//nameLayout.add(new Label(messages.editTradeoffReasonDialogBoxName()));
 		nameLayout.add(this.tradeoffReasonTextBox);
 
 		this.tradeoffReasonTextBox.setWidth("500px");
 		this.tradeoffReasonTextBox.setSize("500px", "80px");
 		this.tradeoffReasonTextBox.setText(tradeoffReason.getTradeoffreason());
 
 		// Set up the buttons
 		saveButton = new Button(messages.editTradeoffReasonDialogBoxOkay(), new SaveHandler(this, tradeoffReason));
 		Button cancelButton = new Button(messages.editTradeoffReasonDialogBoxCancel(), new CancelHandler(this));
 		
 		
 		
 		
 		saveButton.setWidth("100px");
 		cancelButton.setWidth("100px");
 		
 		buttonsLayout.setSpacing(10);
 		buttonsLayout.add(saveButton);
 		buttonsLayout.add(cancelButton);
 
 		// set the base layout
 		baseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		baseLayout.add(nameLayout);
 		baseLayout.add(descriptionLayout);
 		baseLayout.add(buttonsLayout);
 		baseLayout.setSpacing(5);
 
 		this.setWidget(baseLayout);
 	}
 	/**
 	 * Sets up the controls in the dialog
 	 * 
 	 * @param SoftwarePackage
 	 *            The category to be updated in this dialog.
 	 */
 	private void initializeDialogReadOnly(GwtTradeoffReason tradeoffReason)
 	{
 
 		VerticalPanel baseLayout = new VerticalPanel();
 		VerticalPanel nameLayout = new VerticalPanel();
 		VerticalPanel descriptionLayout = new VerticalPanel();
 		HorizontalPanel buttonsLayout = new HorizontalPanel();
 		this.setText(messages.editTradeoffReasonDialogBoxTitleReadOnly());
		//nameLayout.add(new Label(messages.editTradeoffReasonDialogBoxName()));
 		nameLayout.add(this.tradeoffReasonTextBox);
 
 		this.tradeoffReasonTextBox.setWidth("500px");
 		this.tradeoffReasonTextBox.setSize("500px", "80px");
 		this.tradeoffReasonTextBox.setText(tradeoffReason.getTradeoffreason());
 
 		// Set up the buttons
 		//saveButton = new Button(messages.editTradeoffReasonDialogBoxSave(), new SaveHandler(this, tradeoffReason));
 		Button okayButton = new Button(messages.editTradeoffReasonDialogBoxOkay(), new CancelHandler(this));
 		
 		//saveButton.setWidth("100px");
 		okayButton.setWidth("100px");
 		
 		buttonsLayout.setSpacing(10);
 		//buttonsLayout.add(saveButton);
 		buttonsLayout.add(okayButton);
 
 		// set the base layout
 		baseLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		baseLayout.add(nameLayout);
 		baseLayout.add(descriptionLayout);
 		baseLayout.add(buttonsLayout);
 		baseLayout.setSpacing(5);
 
 		this.setWidget(baseLayout);
 	}
 	private void configureButton()
 	{
 		if(tradeoffReasonTextBox.getText().trim().equalsIgnoreCase(""))
 		{
 			saveButton.setEnabled(false);
 		}
 		else
 		{
 			saveButton.setEnabled(true);
 		}
 	}
 
 	private class SaveHandler implements ClickHandler
 	{
 		private EditTradeoffReasonDialog dialog = null;
 		private GwtTradeoffReason localTradeoffReason = null;
 		String currentTradeoffReason = "";
 		public SaveHandler(EditTradeoffReasonDialog dialogPointer, GwtTradeoffReason newTradeoffReason)
 			{
 				super();
 				
 				this.dialog = dialogPointer;
 				this.localTradeoffReason = new GwtTradeoffReason();
 				this.localTradeoffReason.setProjectId(newTradeoffReason.getProjectId());
 				this.localTradeoffReason.setPackageId(newTradeoffReason.getPackageId());
 				this.localTradeoffReason.setTradeoffreason(newTradeoffReason.getTradeoffreason());
 				currentTradeoffReason = newTradeoffReason.getTradeoffreason();
 			}
 		public void onClick(ClickEvent event)
 		{
 			this.dialog.hide();
 		}
 	}
 
 	private class CancelHandler implements ClickHandler
 	{
 		private EditTradeoffReasonDialog dialog = null;
 
 		public CancelHandler(EditTradeoffReasonDialog dialog)
		{
			super();
			this.dialog = dialog;
		}
 		public void onClick(ClickEvent event)
 		{
 			this.dialog.hide(true);
 		}
 	}
 }
