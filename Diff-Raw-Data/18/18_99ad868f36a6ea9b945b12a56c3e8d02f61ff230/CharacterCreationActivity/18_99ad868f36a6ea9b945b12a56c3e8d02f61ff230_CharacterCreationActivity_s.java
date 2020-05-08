 package edu.upenn.cis350.Trace2Learn;
 
 import java.util.List;
 
 import edu.upenn.cis350.Trace2Learn.R.id;
 import edu.upenn.cis350.Trace2Learn.Database.DbAdapter;
 import edu.upenn.cis350.Trace2Learn.Database.LessonCharacter;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 public class CharacterCreationActivity extends Activity {
 
 	private LinearLayout _characterViewSlot;
 	private CharacterCreationPane _creationPane;
 	private CharacterPlaybackPane _playbackPane;	
 	private CharacterTracePane _tracePane;
 	
 	private TextView _tagText;
 
 	private DbAdapter _dbHelper;
 
 	private Mode _currentMode = Mode.INVALID;
 
 	private long id_to_pass = -1;
 
 	private enum Mode {
 		CREATION, DISPLAY, ANIMATE, SAVE, INVALID, TRACE;
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.test_char_display);
 
 		_characterViewSlot =(LinearLayout)findViewById(id.character_view_slot);
 		_creationPane = new CharacterCreationPane(this);
 		_playbackPane = new CharacterPlaybackPane(this, false, 2);
 		_tracePane = new CharacterTracePane(this);
 
 		setCharacter(new LessonCharacter());
 
 		_tagText = (TextView) this.findViewById(id.tag_list);
 
 		_dbHelper = new DbAdapter(this);
 		_dbHelper.open();
 
 		initializeMode();
 
 	}
 
 	/**
 	 * Initialize the display mode, if the activity was started with intent to
 	 * display a character, that character should be displayed
 	 */
 	private void initializeMode() 
 	{
 		Bundle bun = getIntent().getExtras();
 		if (bun != null && bun.containsKey("mode")) 
 		{
 			String mode = bun.getString("mode");
 			if (mode.equals("display")) 
 			{
 				setCharacter(_dbHelper.getCharacterById(bun.getLong("charId")));
 				setCharacterDisplayPane();
 				id_to_pass = bun.getLong("charId");
 				updateTags();
 			}
 		} else 
 		{
 			setCharacterCreationPane();
 		}
 	}
 
 	/**
 	 * Switches the display mode to creation
 	 */
 	private synchronized void setCharacterCreationPane() 
 	{
 		if (_currentMode != Mode.CREATION) 
 		{
 			_currentMode = Mode.CREATION;
 			_characterViewSlot.removeAllViews();
 			_characterViewSlot.addView(_creationPane);
 		}
 	}
 
 	/**
 	 * Switches the display mode to display
 	 */
 	private synchronized void setCharacterDisplayPane()
 	{
 		_playbackPane.setAnimated(true);
 		if (_currentMode != Mode.DISPLAY) 
 		{
 			LessonCharacter curChar = _creationPane.getCharacter();
 			setCharacter(curChar);
 			_currentMode = Mode.DISPLAY;
 			_characterViewSlot.removeAllViews();
 			_characterViewSlot.addView(_playbackPane);
 		}
 	}
 
 	/**
 	 * Switches the display mode to display
 	 */
 	private synchronized void setCharacterTracePane()
 	{
 		_tracePane.clearPane();
 		if (_currentMode != Mode.TRACE) 
 		{
 			LessonCharacter curChar = _creationPane.getCharacter();
 			setCharacter(curChar);
 			_currentMode = Mode.TRACE;
 			_characterViewSlot.removeAllViews();
 			_characterViewSlot.addView(_tracePane);
 		}
 	}
 	
 	public void setContentView(View view)
 	{
 		super.setContentView(view);
 	}
 
 	private void setCharacter(LessonCharacter character)
 	{
 		_creationPane.setCharacter(character);
 		_playbackPane.setCharacter(character);
 		_tracePane.setTemplate(character);
 	}
 
 	private void updateTags()
 	{
 		if (id_to_pass >= 0)
 		{
 			List<String> tags = _dbHelper.getTags(id_to_pass);
 			this._tagText.setText(tagsToString(tags));
 			setCharacter(_dbHelper.getCharacterById(id_to_pass));
 		}
 	}
 
 	public void onClearButtonClick(View view)
 	{
 		_creationPane.clearPane();
 		_tracePane.clearPane();
 		_playbackPane.clearPane();
 	}
 	
 	public void onTraceButtonClick(View view)
 	{
 		setCharacterTracePane();
 	}
 	
 	@Override
 	public void onRestart()
 	{
 		super.onRestart();
 		updateTags();
 	}
 
 	private String tagsToString(List<String> tags)
 	{
 		StringBuffer buf = new StringBuffer();
 		for (String str : tags)
 		{
 			buf.append(str + ", ");
 		}
 
 		return buf.toString();
 	}
 
 	public void onCreateButtonClick(View view)
 	{
 		setCharacterCreationPane();
 	}
 
 	public void onSaveButtonClick(View view)
 	{
 		LessonCharacter character = _creationPane.getCharacter();
 		long id = character.getId();
 		_dbHelper.addCharacter(character);
 		Log.e("Adding to DB", Long.toString(character.getId()));
 		id_to_pass = character.getId();
 		//if(id >= 0)
 		//{
 			onTagButtonClick(view);
 		//}
 		updateTags();
 	}
 
 	public void onTagButtonClick(View view) 
 	{
 		LessonCharacter character = _creationPane.getCharacter();
 		if (id_to_pass >= 0) 
 		{
 			Log.e("Passing this CharID", Long.toString(id_to_pass));
 			Intent i = new Intent(this, TagActivity.class);
 
 			i.putExtra("ID", id_to_pass);
 			i.putExtra("TYPE", character.getItemType().toString());
 
 			startActivity(i);
 		} else
 		{
 			_tagText.setText("Error: Save the character before adding tags");
 		}
 
 	}
 
 	public void onAnimateButtonClick(View view) 
 	{
 		Log.i("CLICK", "DISPLAY");
 		setCharacterDisplayPane();
 	}
 
 }
