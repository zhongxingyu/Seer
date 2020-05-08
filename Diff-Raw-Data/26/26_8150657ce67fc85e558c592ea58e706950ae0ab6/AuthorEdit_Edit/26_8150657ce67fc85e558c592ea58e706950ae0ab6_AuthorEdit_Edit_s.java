 /*
  *	Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * 	Evan DeGraff
  *
  * 	Permission is hereby granted, free of charge, to any person obtaining a copy of
  * 	this software and associated documentation files (the "Software"), to deal in
  * 	the Software without restriction, including without limitation the rights to
  * 	use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * 	the Software, and to permit persons to whom the Software is furnished to do so,
  * 	subject to the following conditions:
  *
  * 	The above copyright notice and this permission notice shall be included in all
  * 	copies or substantial portions of the Software.
  *
  * 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * 	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * 	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * 	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * 	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * 	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.view;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Choice;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.view.treeView.TreeView;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * Fragment controlled by AuthorEdit view
  * 
  * Contains editing fields for a custom FragmentView
  * 
  * @author James Finlay
  *
  */
 public class AuthorEdit_Edit extends Fragment implements OnClickListener, IFragmentSelected, TextWatcher
 {
 	private static final String TAG = "AuthorEdit_Edit";
 
 	private Activity m_activity = null;
 	private ChoiceAdapter m_adapter = null;
 	
 	private View _rootView;
 	private StoryFragment _fragment;
 
 	private Button _addChoiceBtn;
 	private EditText _content;
 	private ListView _choiceList = null;
 
 	private TreeView m_treeview = null;
 	private ActionBar m_actionBar = null;
 
 	// data used for editing and creating choices
 	private boolean m_isEditingChoice = false;
 	private Choice m_selectedChoice = null;
 	private String m_choiceTxt = null;
 	private IFragmentSelected m_oldListener = null;
 
 	public void SetTreeView(TreeView treeView)
 	{
 		m_treeview = treeView;
 	}
 
 	public void SetActionBar(ActionBar actionBar)
 	{
 		m_actionBar = actionBar;
 	}
 
 	public void setFragment(StoryFragment sf) 
 	{
 		_fragment = sf;
 		setUpView();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, 
 			ViewGroup container, Bundle savedInstanceState) {
 
 		_rootView = inflater.inflate(R.layout.fragment_edit, container, false);
 		setUpView();
 		return _rootView;
 	}
 	
 	public void CancelPendingActions()
 	{
 		if(m_isEditingChoice)
 		{
 			RestoreTreeView();
 			m_selectedChoice = null;
 			m_choiceTxt = null;
 		}
 	}
 
 	private void setUpView() 
 	{
 		if (_rootView == null) return;
 		if (_fragment == null) return;
		if (m_activity == null) return;
 
 		/** Layout items **/
 		_addChoiceBtn = (Button) _rootView.findViewById(R.id.addChoice_btn);
 		_choiceList = (ListView) _rootView.findViewById(R.id.choicesList);
 		_content = (EditText) _rootView.findViewById(R.id.content);
 
 		_content.setText(_fragment.getStoryText());
 		_content.addTextChangedListener(this);
 
 		_addChoiceBtn.setOnClickListener(this);
 		
 		// TODO::JT fix James' stuffz
 		/*
 		_media.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(getActivity(), FullScreen_Image.class);
 				intent.putExtra(FullScreen_Image.TAG_AUTHOR, true);
 				startActivity(intent);	
 			}
 		});
 		*/
 
 		_choiceList.setClickable(false);
 		m_adapter = new ChoiceAdapter(	m_activity, 
 										R.layout.choice_item, 
 										_fragment.getChoices());
 		_choiceList.setAdapter(m_adapter);
 	}
 	
 	@Override
 	public void onAttach(Activity activity)
 	{
 		super.onAttach(activity);
 		
 		m_activity = activity;
 		setUpView();
 	}
 
 	public void onClick(View v)
 	{
 		AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());
 
 		alert.setTitle("Title");
 		alert.setMessage("Message");
 
 		// Set an EditText view to get user input 
 		final EditText input = new EditText(this.getActivity());
 		alert.setView(input);
 
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() 
 		{
 			public void onClick(DialogInterface dialog, int whichButton) 
 			{
 				m_choiceTxt = input.getText().toString();
 				if(m_choiceTxt != null && m_choiceTxt.length() > 0)
 				{
 					m_isEditingChoice = true;
 					LaunchTreeView();
 				}
 			}
 		});
 
 		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
 		{
 			public void onClick(DialogInterface dialog, int whichButton) 
 			{
 				m_isEditingChoice = false;
 			}
 		});
 
 		alert.show();
 	}
 
 	private void LaunchTreeView()
 	{
 		m_oldListener = m_treeview.GetFragmentCallback();
 		m_treeview.SetFragmentCallback(this);
 		m_actionBar.setSelectedNavigationItem(AuthorEdit.OVERVIEW_INDEX);
 		
 		Toast.makeText(m_activity, "Select a fragment to connect the choice to", Toast.LENGTH_LONG).show();
 	}
 
 	private void RestoreTreeView()
 	{
 		m_treeview.SetFragmentCallback(m_oldListener);
 		m_oldListener = null;
 		m_actionBar.setSelectedNavigationItem(AuthorEdit.EDIT_INDEX);
 	}
 	
 	private void RefreshTreeView()
 	{
 		m_treeview.RefreshView();
 	}
 
 	public void OnFragmentSelected(StoryFragment selectedFragment) 
 	{
 		if(m_isEditingChoice)
 		{
 			m_isEditingChoice = false;
 
 			if(m_selectedChoice == null)
 			{
 				// then we are making a new choice
 				Choice newChoice = new Choice(m_choiceTxt, selectedFragment.getFragmentID());
 				_fragment.addChoice(newChoice);
 				m_treeview.AddChoice(_fragment, newChoice);
 			}
 			else
 			{
 				m_selectedChoice.setTarget(selectedFragment.getFragmentID());
 			}
 
 			RefreshTreeView();
 			m_adapter.notifyDataSetChanged();
 		}
 
 		// we should get this callback exactly once and no more
 		RestoreTreeView();
 		
 		m_selectedChoice = null;
 		m_choiceTxt = null;
 	}
 	
 	public void afterTextChanged(Editable s) 
 	{
 		_fragment.setStoryText(s.toString());
 		m_treeview.RefreshView();
 	}
 
 	@Override
 	public void beforeTextChanged(CharSequence s, int start, int count,
 			int after) 
 	{	} // Dont' care
 
 	@Override
 	public void onTextChanged(CharSequence s, int start, int before, int count) 
 	{	} // Don't care
 
 	private class ChoiceAdapter extends ArrayAdapter<Choice>
 	{
 		private Context m_context;
 		private List<Choice> m_values;
 		private List<ChoiceButtonCallback> m_callbacks = new ArrayList<ChoiceButtonCallback>();
 
 		public ChoiceAdapter(Context context, int resource, List<Choice> choices) 
 		{
 			super(context, resource, choices);
 
 			m_context = context;
 			m_values = choices;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			LayoutInflater inflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 			View rowView = inflater.inflate(R.layout.choice_item, parent, false);
 
 			Choice item = m_values.get(position);
 
 			/** Layout Items **/
 			ImageButton deleteBtn = (ImageButton)rowView.findViewById(R.id.choice_remove_btn);
 			ImageButton choiceBtn = (ImageButton)rowView.findViewById(R.id.choices_edit_btn);
 			EditText txt = (EditText)rowView.findViewById(R.id.choice_txt);
 
 			ChoiceButtonCallback callback = new ChoiceButtonCallback(_fragment, item);	
 			m_callbacks.add(callback);
 
 			txt.setText(item.getText());
 			txt.addTextChangedListener(callback);
 			deleteBtn.setOnClickListener(callback);
 			choiceBtn.setOnClickListener(callback);
 
 			return rowView;
 		}
 
 		private class ChoiceButtonCallback implements OnClickListener, TextWatcher
 		{
 			private StoryFragment m_origin = null;
 			private Choice m_choice = null;
 
 			public ChoiceButtonCallback(StoryFragment frag, Choice choice)
 			{
 				m_origin = frag;
 				m_choice = choice;
 			}
 
 			public void onClick(View btn) 
 			{
 				if(btn.getId() == R.id.choice_remove_btn)
 				{
 					m_origin.removeChoice(m_choice);
 					RefreshTreeView();
 					m_adapter.notifyDataSetChanged();
 					m_treeview.RemoveChoice(m_origin, m_choice);
 				}
 				else if(btn.getId() == R.id.choices_edit_btn)
 				{
 					m_isEditingChoice = true;
 					m_selectedChoice = m_choice;
 					LaunchTreeView();
 				}
 				else
 				{
 					Log.e(TAG, "Somehow clicked on a button that shouldn't exist...?");
 				}
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) 
 			{
 				m_choice.setText(s.toString());
 			}
 
 			@Override
 			public void beforeTextChanged(	CharSequence s, 
 											int start, 
 											int count,
 											int after) 
 			{
 				// Do nothing, don't care
 			}
 
 			@Override
 			public void onTextChanged(	CharSequence s, 
 										int start, 
 										int before,
 										int count) 
 			{
 				// Do nothing, don't care
 			}
 		}
 	}
 }
