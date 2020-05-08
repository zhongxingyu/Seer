 package com.mais.leantasks;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import android.content.Context;
 import android.text.Editable;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 
 import com.mais.leantasks.model.Task;
 import com.mais.leantasks.sql.Table;
 
 public class TaskArrayAdapter extends ArrayAdapter<Task> {
 
 	private int resource;
 	private Logger log = Logger.getLogger("TaskArrayAdapter");
 	
 	public TaskArrayAdapter(Context context, int textViewResourceId, List<Task> objects) {
 		super(context, textViewResourceId, objects);
 		this.resource = textViewResourceId;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, final ViewGroup parent) {
 		
 	    LinearLayout taskView;
 	    final Task task = getItem(position);
 	    
 	    final String taskText = task.getText();
 	    boolean taskChecked = task.isChecked();
 	    boolean taskArchived = task.isArchived();
 	    if(convertView == null) {
 	        taskView = new LinearLayout(getContext());
 	        LayoutInflater inflater = 
 	            (LayoutInflater)getContext()
 	            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 	        inflater.inflate(resource, taskView, true);
 	    } else {
 	        taskView = (LinearLayout)convertView;
 	    }
 	    
 	    CheckBox taskCheckBox = (CheckBox)taskView.findViewById(R.id.task_check_box);
 	    
 	    taskCheckBox.setChecked(taskChecked);
 	    taskCheckBox.setOnClickListener(new OnTaskCheckListener(this, task));
 	    
 	    final EditText editText = (EditText)taskView.findViewById(R.id.task_edit_text);
 	    editText.setText(formatTaskText(taskText));
 	    final ImageButton taskButton = (ImageButton) taskView.findViewById(R.id.task_button);
 	    taskButton.setTag(R.drawable.ic_action_remove);
	    taskButton.setOnClickListener(new OnTaskDeleteListener(this, task, editText));
 	    
 	    editText.setOnFocusChangeListener(new OnFocusChangeListener() {
 			
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 				if(hasFocus){
 					EditText focusedEditText = (EditText) v;
 					Editable htmlText = focusedEditText.getText();
 					String newText = Html.fromHtml(htmlText.toString()).toString();
 					focusedEditText.setText(newText);
 					taskButton.setImageResource(R.drawable.ic_action_confirm);
 					taskButton.setTag(R.drawable.ic_action_confirm);
 		            log.info("remove removed");
 				} else {
 					log.info("confirm removed");
 					taskButton.setImageResource(R.drawable.ic_action_remove);
 					taskButton.setTag(R.drawable.ic_action_remove);
 				}
 			}
 		});
 	    
 //	    editText.addTextChangedListener(new TextWatcher(){
 //	        public void afterTextChanged(Editable s) {
 //	        	if(editText.isFocused()){
 //		            
 //	        	}
 //	        }
 //	        public void beforeTextChanged(CharSequence s, int start, int count, int after){ }
 //	        public void onTextChanged(CharSequence s, int start, int before, int count){}
 //	    }); 
 	    
 //	    editText.setOnEditorActionListener(new OnEditorActionListener() {
 //			
 //			@Override
 //			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 //				if (actionId == EditorInfo.IME_ACTION_SEARCH ||
 //			            actionId == EditorInfo.IME_ACTION_DONE ||
 //			            event.getAction() == KeyEvent.ACTION_DOWN &&
 //			            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 //					
 //		        	v.setVisibility(View.GONE);
 //					ViewGroup row = (ViewGroup) v.getParent();
 //					for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
 //					    View view = row.getChildAt(itemPos);
 //					    log.info("focusChanged: "+itemPos);
 //					    if (view instanceof TextView) {
 //					         view.setVisibility(View.VISIBLE);
 //					    }
 //					}
 //					
 //					return true; // consume.
 //			    }
 //			    return false; // pass on to other listeners. 
 //			}
 //		});
 //	    
 //	    TextView showText = (TextView) taskView.findViewById(R.id.task_show_text);
 //	    showText.setText(taskText);
 //	    showText.setOnClickListener(new OnClickListener() {
 //			
 //			@Override
 //			public void onClick(View v) {
 //				v.setVisibility(View.GONE);
 //				ViewGroup row = (ViewGroup) v.getParent();
 //				for (int itemPos = 0; itemPos < row.getChildCount(); itemPos++) {
 //					log.info("clicked: "+itemPos);
 //				    View view = row.getChildAt(itemPos);
 //				    log.info(view.getClass().toString());
 //				    if (view instanceof EditText) {
 //				    	log.info("LAL");
 //				        view.setVisibility(View.VISIBLE);
 //				        view.requestFocus();
 //				    }
 //				}
 //			}
 //		});
 	    
 	    return taskView;
 	}
 	
 	private Spanned formatTaskText(String taskText){
 		String taskTextArray[] = taskText.split(" ", 2);
 	    String firstWord;
 	    String theRest;
 	    
 	    if(taskTextArray.length == 0){
 	    	firstWord = "";
 	    	theRest = "";
 	    } else if(taskTextArray.length == 1){
 	    	firstWord = taskTextArray[0];
 	    	theRest = "";
 	    } else {
 	    	firstWord = taskTextArray[0];
 		    theRest = taskTextArray[1];
 	    }
 	    
 	    return Html.fromHtml("<b>"+firstWord+"</b> "+theRest);
 	    
 	}
 	
 	private class OnTaskCheckListener implements OnClickListener {
 
 		private TaskArrayAdapter adapter;
 		private Task task;
 		
 		public OnTaskCheckListener(TaskArrayAdapter adapter, Task task){
 			this.task = task;
 			this.adapter = adapter;
 		}
 		
 		@Override
 		public void onClick(View v) {
 			CheckBox checkBox = (CheckBox)v;
 			task.setChecked(checkBox.isChecked());
 			Table.getInstance(v.getContext()).tasks.update(task);
 		}
 		
 	}
 	
 	private class OnTaskDeleteListener implements OnClickListener {
 
 		private TaskArrayAdapter adapter;
 		private Task task;
 		private EditText editText;
 		
 		public OnTaskDeleteListener(TaskArrayAdapter adapter, Task task, EditText editText){
 			this.task = task;
 			this.adapter = adapter;
 			this.editText = editText;
 		}
 		
 		@Override
 		public void onClick(View v) {
 			ImageButton button = (ImageButton) v;
 			if( ((Integer) button.getTag()) == R.drawable.ic_action_remove)
 			{				
 				Table.getInstance(v.getContext()).tasks.delete(task);
 				adapter.remove(task);
 				adapter.notifyDataSetChanged();
 			}
 			else if(((Integer) button.getTag()) == R.drawable.ic_action_confirm)
 			{
 				task.setText(editText.getText().toString());
 				adapter.notifyDataSetChanged();
 				Table.getInstance(v.getContext()).tasks.update(task);
 			}
 		}
 		
 	}
 		
 }
