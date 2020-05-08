 /*******************************************************************************
  * Copyright (c) 2013 - Alexandre Jorge Fonseca (alexjf.net)
  * License: GPL v3 (http://www.gnu.org/licenses/gpl-3.0.txt)
  ******************************************************************************/
 package net.alexjf.tmm.fragments;
 
 import java.math.BigDecimal;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import net.alexjf.tmm.R;
 import net.alexjf.tmm.domain.DatabaseHelper;
 import net.alexjf.tmm.domain.MoneyNode;
 import net.alexjf.tmm.exceptions.DatabaseException;
 import net.alexjf.tmm.fragments.DrawablePickerFragment.OnDrawablePickedListener;
 import net.alexjf.tmm.utils.DrawableResolver;
 import net.alexjf.tmm.utils.PreferenceManager;
 import net.alexjf.tmm.views.SelectorButton;
 
 import android.app.Activity;
 import android.app.DatePickerDialog.OnDateSetListener;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.text.TextUtils;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class MoneyNodeEditorFragment extends Fragment 
     implements OnDateSetListener, OnDrawablePickedListener {
     private static final String KEY_CURRENTNODE = "currentNode";
     private static final String KEY_SELECTEDICON = "selectedIcon";
 
     private static final String PREFKEY_DEFAULTCURRENCY = "pref_key_default_currency";
 
     private static final String TAG_DATEPICKER = "datePicker";
     private static final String TAG_DRAWABLEPICKER = "drawablePicker";
 
     private OnMoneyNodeEditListener listener;
 
     private MoneyNode node;
     private String selectedDrawableName;
 
     private DatePickerFragment datePicker;
     private DrawablePickerFragment drawablePicker;
 
     private EditText nameText;
     private EditText descriptionText;
     private SelectorButton iconSelectorButton;
     private Button creationDateButton;
     private EditText initialBalanceText;
     private Spinner currencySpinner;
     private Button addButton;
     private SimpleDateFormat dateFormat;
 
     public interface OnMoneyNodeEditListener {
         public void onMoneyNodeCreated(MoneyNode node);
         public void onMoneyNodeEdited(MoneyNode node);
     }
 
     public MoneyNodeEditorFragment() {
         dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy");
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_moneynode_editor, container, false);
 
         nameText = (EditText) v.findViewById(R.id.name_text);
         descriptionText = (EditText) v.findViewById(R.id.description_text);
         iconSelectorButton = (SelectorButton) v.findViewById(R.id.icon_selector);
         creationDateButton = (Button) v.findViewById(R.id.creationDate_button);
         initialBalanceText = (EditText) v.findViewById(R.id.initialBalance_text);
         currencySpinner = (Spinner) v.findViewById(R.id.currency_spinner);
         addButton = (Button) v.findViewById(R.id.add_button);
 
         FragmentManager fm = getFragmentManager();
         datePicker = (DatePickerFragment) fm.findFragmentByTag(TAG_DATEPICKER);
         drawablePicker = (DrawablePickerFragment) 
             fm.findFragmentByTag(TAG_DRAWABLEPICKER);
 
         if (datePicker == null) {
             datePicker = new DatePickerFragment();
         }
 
         if (drawablePicker == null) {
             drawablePicker = new DrawablePickerFragment();
             drawablePicker.setFilter("glyphish_");
         }
 
         datePicker.setListener(this);
         drawablePicker.setListener(this);
 
         creationDateButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 try {
                    datePicker.setDate(dateFormat.parse(creationDateButton.getText().toString()));
                 } catch (ParseException e) {
                 }
                 datePicker.show(getFragmentManager(), TAG_DATEPICKER);
             }
         });
 
         iconSelectorButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 drawablePicker.show(getFragmentManager(), TAG_DRAWABLEPICKER);
             }
         });
 
         addButton.setOnClickListener(new OnClickListener() {
             public void onClick(View view) {
                 if (!validateInputFields()) {
                     return;
                 }
 
                 String name = nameText.getText().toString().trim();
                 String description = descriptionText.getText().toString().trim();
                 Date creationDate;
                 try {
                     creationDate = dateFormat.parse(
                         creationDateButton.getText().toString());
                 } catch (ParseException e) {
                     creationDate = new Date();
                 }
 
                 BigDecimal initialBalance;
                 try {
                     initialBalance = new BigDecimal(
                         initialBalanceText.getText().toString());
                 } catch (NumberFormatException e) {
                     initialBalance = new BigDecimal(0);
                 }
 
                 String currency = currencySpinner.getSelectedItem().toString().trim();
 
                 if (node == null) {
                     MoneyNode newNode = new MoneyNode(name, description, selectedDrawableName,
                         creationDate, initialBalance, currency);
                     listener.onMoneyNodeCreated(newNode);
                 } else {
                     node.setName(name);
                     node.setIcon(selectedDrawableName);
                     node.setDescription(description);
                     node.setCreationDate(creationDate);
                     node.setInitialBalance(initialBalance);
                     node.setCurrency(currency);
                     listener.onMoneyNodeEdited(node);
                 }
             }
         });
 
         if (savedInstanceState != null) {
             node = savedInstanceState.getParcelable(KEY_CURRENTNODE);
         } else {
         }
         
         updateNodeFields();
 
         if (savedInstanceState != null) {
             selectedDrawableName = savedInstanceState.getString(KEY_SELECTEDICON);
             int iconId = DrawableResolver.getInstance().getDrawableId(selectedDrawableName);
             iconSelectorButton.setDrawableId(iconId);
         }
 
         return v;
     }
 
     public void onDateSet(DatePicker view, int year, int month, int day) {
         GregorianCalendar calendar = new GregorianCalendar(year, month, day);
         creationDateButton.setText(dateFormat.format(calendar.getTime()));
     }
 
     public void onDrawablePicked(int drawableId, String drawableName) {
         iconSelectorButton.setDrawableId(drawableId);
         selectedDrawableName = drawableName;
         iconSelectorButton.setError(false);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         outState.putString(KEY_SELECTEDICON, selectedDrawableName);
         outState.putParcelable(KEY_CURRENTNODE, node);
         super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             listener = (OnMoneyNodeEditListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + 
                     " must implement OnMoneyNodeEditListener");
         }
     }
 
     /**
      * @return the node
      */
     public MoneyNode getNode() {
         return node;
     }
 
     /**
      * @param node the node to set
      */
     public void setNode(MoneyNode node) {
         MoneyNode prevNode = this.node;
         this.node = node;
 
         if (prevNode != node) {
             updateNodeFields();
         }
     }
 
     private void updateNodeFields() {
         // If we are adding a new node, reset all fields
         if (node == null) {
             nameText.setText("");
             descriptionText.setText("");
             iconSelectorButton.setDrawableId(0);
             creationDateButton.setText(dateFormat.format(new Date()));
             initialBalanceText.setText("");
 
             PreferenceManager prefManager = PreferenceManager.getInstance();
             String prefCurrency = prefManager.readUserStringPreference(
                     PREFKEY_DEFAULTCURRENCY, null);
 
             int positionInSpinner = 0;
 
             if (prefCurrency != null) {
                 @SuppressWarnings("unchecked")
                 ArrayAdapter<String> adapter = (ArrayAdapter<String>) 
                     currencySpinner.getAdapter();
                 positionInSpinner = adapter.getPosition(prefCurrency);
             }
 
             currencySpinner.setSelection(positionInSpinner);
             addButton.setText(R.string.add);
         // If we are editing a node, fill fields with current information
         } else {
             nameText.setText(node.getName());
             descriptionText.setText(node.getDescription());
             selectedDrawableName = node.getIcon();
             int iconId = DrawableResolver.getInstance().getDrawableId(selectedDrawableName);
             iconSelectorButton.setDrawableId(iconId);
             creationDateButton.setText(dateFormat.format(node.getCreationDate()));
             initialBalanceText.setText(node.getInitialBalance().toString());
             @SuppressWarnings("unchecked")
             ArrayAdapter<String> adapter = (ArrayAdapter<String>) currencySpinner.getAdapter();
             int positionInSpinner = adapter.getPosition(node.getCurrency());
             currencySpinner.setSelection(positionInSpinner);
             addButton.setText(R.string.edit);
         }
     }
 
     private boolean validateInputFields() {
         boolean error = false;
 
         Drawable errorDrawable = 
             getResources().getDrawable(R.drawable.indicator_input_error);
         errorDrawable.setBounds(0, 0, 
                 errorDrawable.getIntrinsicWidth(), 
                 errorDrawable.getIntrinsicHeight());
         String name = nameText.getText().toString();
 
         // TODO move error strings to resources
         String nameError = null;
         if (TextUtils.isEmpty(name)) {
             nameError = "Name cannot be empty.";
         }
         else {
             try {
                 // If we are adding a new node and name already exists
                 if (node == null && 
                     DatabaseHelper.getInstance().hasMoneyNodeWithName(name)) {
                     nameError = "A money node with that name already exists.";
                 }
             } catch (DatabaseException e) {
                 nameError = "Unable to determine if node already exists.";
             }
         }
 
         if (nameError != null) {
             nameText.setError(nameError, errorDrawable);
             error = true;
         }
 
         if (TextUtils.isEmpty(selectedDrawableName)) {
             iconSelectorButton.setError(true);
             error = true;
         }
 
         return !error;
     }
 }
 
