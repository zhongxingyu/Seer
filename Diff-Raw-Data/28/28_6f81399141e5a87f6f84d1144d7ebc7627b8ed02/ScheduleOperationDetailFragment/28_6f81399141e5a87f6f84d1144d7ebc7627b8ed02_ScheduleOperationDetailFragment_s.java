 package org.rhq.pocket.operation;
 
 import java.util.Map;
 
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.DeserializationConfig;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 
 import org.rhq.core.domain.rest.Link;
 import org.rhq.core.domain.rest.OperationDefinitionRest;
 import org.rhq.core.domain.rest.OperationRest;
 import org.rhq.core.domain.rest.PropertyType;
 import org.rhq.core.domain.rest.SimplePropDef;
 import org.rhq.pocket.FinishCallback;
 import org.rhq.pocket.R;
 import org.rhq.pocket.TalkToServerTask;
 
 /**
  * Fragment to set up and edit an operation schedule and to send it off
  * @author Heiko W. Rupp
  */
 public class ScheduleOperationDetailFragment extends Fragment implements View.OnClickListener {
 
     private View layout;
     private OperationDefinitionRest definition;
     private int resourceId;
     private Button checkButton;
     private OperationRest operation;
     private String submitLink;
     private String historyLink;
     private ImageView statusView;
 
     @SuppressWarnings("unused")
     public ScheduleOperationDetailFragment() {
         // For Android internal use
     }
 
     public ScheduleOperationDetailFragment(OperationDefinitionRest definition, int resourceId) {
 
         this.definition = definition;
         this.resourceId = resourceId;
     }
 
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         layout = inflater.inflate(R.layout.operation_schedule_detail_fragment, container, false);
         checkButton = (Button) layout.findViewById(R.id.check_button);
         checkButton.setOnClickListener(this);
         statusView = (ImageView) layout.findViewById(R.id.statusView);
         return layout;
     }
 
     @Override
     public void onResume() {
         super.onResume();
         fillFieldsFromDefinition();
 
     }
 
 
     @Override
     public void onClick(View view) {
 
         if (view.equals(checkButton)) {
 
             // User has entered the screen and set params. Now validate
             if (operation==null) {
 
                 boolean valid = validateFields();
                 if (!valid) {
                     statusView.setImageDrawable(getResources().getDrawable(R.drawable.availability_red_24));
                     Toast.makeText(getActivity(),"Not all required fields are set",Toast.LENGTH_SHORT).show();
                     return;
                 }
                 else {
                     statusView.setImageDrawable(getResources().getDrawable(R.drawable.availability_green_24));
                 }
 
                 operation = new OperationRest(resourceId,definition.getId());
 
                 fillOperationParamsFromUI();
 
                 new TalkToServerTask(getActivity(),new FinishCallback() {
                     @Override
                     public void onSuccess(JsonNode result) {
                         handleResponse(result);
                         checkButton.setText("Submit");
                     }
 
                     @Override
                     public void onFailure(Exception e) {
                         // TODO: Customise this generated block
                     }
                 },"/operation/definition/" + definition.getId() + "?resourceId=" + resourceId,"POST").execute();
             }
             else { // operation != null -> we have something to submit
 
                 if (submitLink==null) {
                     Toast.makeText(getActivity(),"Can't schedule yet", Toast.LENGTH_SHORT).show();
                     return;
                 }
                 int i = submitLink.indexOf("rest/1/");
                 if (i>-1) {
                     submitLink = submitLink.substring(i+7);
                 }
 
 
                 new TalkToServerTask(getActivity(),new FinishCallback() {
                     @Override
                     public void onSuccess(JsonNode result) {
                         handleResponse(result);
                     }
 
                     @Override
                     public void onFailure(Exception e) {
                         // TODO: Customise this generated block
                     }
                 },"/" + submitLink,"PUT").execute(operation);
 
             }
 
 
         } else if (view.getId()==0xdeadbeef) {
             // History button
             int i = historyLink.lastIndexOf("/");
             Log.i("bla", historyLink); // TODO
             if (i>-1) {
                 historyLink = historyLink.substring(i+1);
             }
             OperationHistoryDetailFragment fragment = new OperationHistoryDetailFragment();
             fragment.setHistoryId(historyLink);
             FragmentManager fm = getFragmentManager();
             Fragment f = fm.findFragmentById(R.id.detail_container);
             FragmentTransaction ft = fm.beginTransaction();
             ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
             ft.remove(f);
             ft.add(R.id.detail_container,fragment);
             ft.commit();
         }
 
     }
 
     private boolean validateFields() {
         for (SimplePropDef spd : definition.getParams()) {
             if (spd.isRequired() && !(spd.getType()== PropertyType.BOOLEAN)) {
                 String name =spd.getName();
                TextView tv = (TextView) layout.findViewWithTag(name);
                if (tv.getText()==null || tv.getText().length()==0)
                     return false;
             }
         }
         return true;
     }
 
     private void handleResponse(JsonNode result) {
         ObjectMapper objectMapper = new ObjectMapper();
         objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
 
         try {
             operation = objectMapper.readValue(result,new TypeReference<OperationRest>() {});
             fillFieldsFromOperation();
             for (Link link : operation.getLinks()) {
                 if (link.getRel().equals("edit")) {
                     submitLink = link.getHref();
                     checkButton.setEnabled(true);
                 }
                 if (link.getRel().equals("history")) {
                     Toast.makeText(getActivity(),"Scheduled",Toast.LENGTH_SHORT).show();
                     historyLink = link.getHref();
                     addGoToHistoryButton();
                 }
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void fillFieldsFromDefinition() {
         TextView tv = (TextView) layout.findViewById(R.id.operation_name);
         tv.setText(definition.getName());
 
         TableLayout tl = (TableLayout) layout.findViewById(R.id.opsParamsTable);
         for (SimplePropDef spd : definition.getParams()) {
             TableRow row = new TableRow(getActivity());
             tl.addView(row);
 
             TextView nameView = new TextView(getActivity());
             String name = spd.getName();
             if (spd.isRequired())
                name +="*";
 
            nameView.setText(name);
             row.addView(nameView);
 
             if (spd.getType()==PropertyType.BOOLEAN) {
                 CheckBox cb = new CheckBox(getActivity());
                 cb.setTag(name);
                 if (spd.getDefaultValue()!=null)
                     cb.setChecked(Boolean.valueOf(spd.getDefaultValue()));
                 row.addView(cb);
             } else {
                 EditText et = new EditText(getActivity());
                 et.setTag(name);
 
                 if(PropertyType.isNumeric(spd.getType())) {
                     et.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                 }
 
                 if (spd.getDefaultValue()!=null) {
                     et.setText(""+spd.getDefaultValue());
                 }
                 row.addView(et);
             }
 
         }
     }
 
 
     private void addGoToHistoryButton() {
         LinearLayout buttonBar = (LinearLayout) layout.findViewById(R.id.button_bar);
         Button historyButton = new Button(getActivity());
         historyButton.setText("Show history");
         historyButton.setId(0xdeadbeef);
         historyButton.setOnClickListener(this);
         buttonBar.addView(historyButton);
 
         checkButton.setEnabled(false); // The operation has been scheduled
 
     }
 
     private void fillOperationParamsFromUI() {
         Map<String,Object> params = operation.getParams();
         for (SimplePropDef spd:definition.getParams()) {
             String name = spd.getName();
            View v = layout.findViewWithTag(name);
 
             if (v instanceof TextView) {
                 // TODO differentiate numeric types
                 params.put(name, ((TextView) v).getText().toString());
             }
             if (v instanceof CheckBox) {
                 params.put(name, ((CheckBox) v).isChecked());
             }
         }
     }
 
     private void fillFieldsFromOperation() {
         Map<String,Object> params = operation.getParams();
         for (SimplePropDef spd: definition.getParams()) {
             String name = spd.getName();
             View v = layout.findViewWithTag(name);
 
             if (v instanceof TextView) {
                 // TODO differentiate numeric formats
                 ((TextView) v).setText(params.get(name).toString());
             }
             if (v instanceof CheckBox) {
                 ((CheckBox) v).setChecked((Boolean) params.get(name));
             }
         }
     }
 }
