 package net.randomhacks.wave.voting.approval.client;
 
 import java.util.ArrayList;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyPressEvent;
 import com.google.gwt.event.dom.client.KeyPressHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.gadgets.client.DynamicHeightFeature;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.TextBox;
 
 public class ChoicesTable extends FlexTable implements ChoicesModel.Listener {
 	private TextBox inputBox = new TextBox();
 	private Button addButton = new Button("Add");
 	
 	private ChoicesModel model;
 	private ArrayList<String> knownChoices = new ArrayList<String>();
 	private DynamicHeightFeature dynamicHeightFeature;
 
 	ChoicesTable(ChoicesModel model) {
 		super();
 		this.model = model;
 		
 		getRowFormatter().addStyleName(0, "choicesHeader");
 		// We'll call setHeaderText later.
 		
 		HorizontalPanel hbox = new HorizontalPanel();
 		hbox.add(inputBox);
 		hbox.add(addButton);
 		setWidget(1, 0, hbox);
 
 		addEventHandlers();
 		model.addListener(this);
 	}
 	
 	private void addEventHandlers() {
 		addButton.addClickHandler(new ClickHandler() {
 			public void onClick(ClickEvent event) {
 				addChoice();
 			}
 		});
 		inputBox.addKeyPressHandler(new KeyPressHandler() {
 			public void onKeyPress(KeyPressEvent event) {
 				if (event.getCharCode() == KeyCodes.KEY_ENTER)
 					addChoice();
 			}
 		});
 	}
 
 	private void addChoice() {
 		String input = inputBox.getText().trim();
 		if (input.length() > 0) {
 			inputBox.setText("");
 			model.addChoice(input);
 		}
 	}
 
 	public void notifyChoicesChanged(ArrayList<Choice> choicesModel,
 			boolean isWritable) {
 		updateChoicesTableHeader(choicesModel);
 		updateChoicesTableBody(choicesModel, isWritable);
 		updateChoicesTableFooter(isWritable);
 		if (dynamicHeightFeature != null)
 			dynamicHeightFeature.adjustHeight();
 	}
 
 	private void updateChoicesTableHeader(ArrayList<Choice> choicesModel) {
 		if (choicesModel.size() < 2)
 			setHeaderText("Add Choices");
 		else
 			setHeaderText("Pick Several");
 	}
 
 	private void setHeaderText(String headerText) {
 		setText(0, 0, headerText);
 	}
 
 	private int rowForChoiceIndex(int i) {
 		return i + 1;
 	}
 
 	private void updateChoicesTableBody(ArrayList<Choice> choicesModel,
 			boolean isWritable) {
 		// TODO - Move this code out into a presenter layer where we
 		// can actually test and debug it properly.
 		Log.debug("Known before: " + knownChoices.toString());
 		Log.debug("Model: " + choicesModel.toString());
 		int i = 0;
 		for (Choice choice : choicesModel) {
 			// While 'choice.key' is alphabetically greater than the current
 			// key, then we need to remove the current row. Normally, this can
 			// only happen when rewinding a wave in playback mode.
 			while (i < knownChoices.size() &&
 					choice.key.compareTo(knownChoices.get(i)) > 0)
 				removeChoiceRow(i);
 
 			// If 'choice.key' is alphabetically less than the current
 			// key, then we need to insert a new row.
 			if (i >= knownChoices.size()
 					|| choice.key.compareTo(knownChoices.get(i)) < 0)
 				insertChoiceRow(i, choice);
 
 			// Make sure the current row matches the model.
			updateChoiceRow(rowForChoiceIndex(i), choice, isWritable);
 			++i;
 		}
 		
 		// If we have any trailing rows left over after we've taken care
 		// of all the choices in our model, we'll need to remove them.
 		while (i < knownChoices.size()) {
 			Log.debug("Trailing choice: " + knownChoices.get(i));
 			removeChoiceRow(i);
 		}
 		Log.debug("Known after: " + knownChoices.toString());
 	}
 
 	private void removeChoiceRow(int i) {
		Log.debug("Removing row: " + knownChoices.get(i));
 		knownChoices.remove(i);
 		removeRow(rowForChoiceIndex(i));
 	}
 
 	private void insertChoiceRow(int i, final Choice choice) {
		Log.debug("Inserting row: " + choice.key);
 		knownChoices.add(i, choice.key);
 		int row = rowForChoiceIndex(i);
 		insertRow(row);
 		final CheckBox checkBox = new CheckBox(choice.name);
 		setWidget(row, 0, checkBox);
 		checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
 			public void onValueChange(ValueChangeEvent<Boolean> event) {
 				model.setChosen(choice.name, event.getValue());
 			}
 		});
 	}
 
 	private void updateChoiceRow(int i, Choice choice, boolean isWritable) {
		Log.debug("Updating row: " + choice.key);
 		CheckBox checkBox = (CheckBox) getWidget(rowForChoiceIndex(i), 0);
 		checkBox.setValue(choice.wasChosenByMe);
 		String label = choice.name;
 		if (choice.votes > 0)
 			label += " (" + Integer.toString(choice.votes) + ")";
 		checkBox.setText(label);
 		if (choice.isWinner)
 			checkBox.addStyleName("winningChoice");
 		else
 			checkBox.removeStyleName("winningChoice");
 		checkBox.setEnabled(isWritable);
 	}
 
 	private void updateChoicesTableFooter(boolean isWritable) {
 		inputBox.setEnabled(isWritable);
 		addButton.setEnabled(isWritable);
 	}
 
 	public void setDynamicHeightFeature(DynamicHeightFeature feature) {
 		dynamicHeightFeature = feature;
 		dynamicHeightFeature.adjustHeight();
 	}
 }
