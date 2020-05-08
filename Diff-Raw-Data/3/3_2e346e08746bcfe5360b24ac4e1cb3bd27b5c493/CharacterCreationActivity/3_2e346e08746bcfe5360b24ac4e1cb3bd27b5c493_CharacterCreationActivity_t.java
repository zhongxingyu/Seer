 package edu.upenn.cis350.Trace2Learn;
 
 import java.util.List;
 
 import edu.upenn.cis350.Trace2Learn.R.id;
 import edu.upenn.cis350.Trace2Learn.Characters.LessonCharacter;
 import edu.upenn.cis350.Trace2Learn.Characters.LessonItem;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Paint;
 import android.os.Bundle;
 import android.text.Layout;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class CharacterCreationActivity extends Activity {
 	
 	private LinearLayout _characterViewSlot;
 	private CharacterCreationPane _creationPane;
 	private CharacterPlaybackPane _playbackPane;
 	private Button _contextButton;
 	
 	private TextView _tagText;
 	
 	private DbAdapter _dbHelper;
 	
 	private Mode _currentMode = Mode.INVALID;
 	
 	private enum Mode
 	{
 		CREATION,
 		DISPLAY,
 		ANIMATE,
 		SAVE,
 		INVALID;
 	}
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		_paint = new Paint();
 		setContentView(R.layout.test_char_display);
 
 		_characterViewSlot = (LinearLayout)this.findViewById(id.character_view_slot);
 		_contextButton = (Button)this.findViewById(id.context_button);
 		_creationPane = new CharacterCreationPane(this, _paint);
 		_playbackPane = new CharacterPlaybackPane(this, _paint, false, 2);
 
 		_tagText = (TextView)this.findViewById(id.tag_list);
 		
 		_paint.setAntiAlias(true);
 		_paint.setDither(true);
 		_paint.setColor(0xFFFF0000);
 		_paint.setStyle(Paint.Style.STROKE);
 		_paint.setStrokeJoin(Paint.Join.ROUND);
 		_paint.setStrokeCap(Paint.Cap.ROUND);
 		_paint.setStrokeWidth(12);
 		
 		_dbHelper = new DbAdapter(this);
         _dbHelper.open();
 		
 		setCharacterCreationPane();
 
 	}
 	
 	private synchronized void setCharacterCreationPane()
 	{
 		if(_currentMode != Mode.CREATION)
 		{
 			_currentMode = Mode.CREATION;
 			_characterViewSlot.removeAllViews();
 			_characterViewSlot.addView(_creationPane);	
 			_contextButton.setText("Clear");
 		}
 	}
 	
 	private synchronized void setCharacterDisplayPane()
 	{
 		_playbackPane.setAnimated(false);
 		if(_currentMode != Mode.DISPLAY)
 		{
 			LessonCharacter curChar = _creationPane.getCharacter();
 			_currentMode = Mode.DISPLAY;
 			_playbackPane.setCharacter(curChar);
 			_characterViewSlot.removeAllViews();
 			_characterViewSlot.addView(_playbackPane);
 			_contextButton.setText("Animate");
 		}
 	}
 	
 	private synchronized void setCharacterSavePane()
 	{
 		if(_currentMode != Mode.SAVE)
 		{
 			_currentMode = Mode.SAVE;
 			_characterViewSlot.removeAllViews();
 			//_characterViewSlot.addView(_savePane);	
 			_contextButton.setText("Commit");
 		}
 	}
 
 	public void setContentView(View view)
 	{
         super.setContentView(view);
     }
 	
 	private Paint _paint;
 
 	public void colorChanged(int color) {
 		_paint.setColor(color);
 	}
 	
 	
 	public void onContextButtonClick(View view)
 	{
 		switch(_currentMode)
 		{
 		case CREATION:
 			_creationPane.clearPane();
 			break;
 		case DISPLAY:
 			_currentMode = Mode.ANIMATE;
 			_playbackPane.setAnimated(true);
 			_contextButton.setText("Stop");
 			break;
 		case ANIMATE:
 			_currentMode = Mode.DISPLAY;
 			_playbackPane.setAnimated(false);
 			_contextButton.setText("Animate");
 			break;
 		case SAVE:
 			break;
 		}
 	}
 
 	
 	private String tagsToString(List<String> tags)
 	{
 		StringBuffer buf = new StringBuffer();
 		for(String str : tags)
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
 		_dbHelper.addCharacter(_creationPane.getCharacter());
 	}
 	
 	public void onTagButtonClick(View view)
 	{
 		LessonCharacter character = _creationPane.getCharacter();
 		character.addTag("Char");
 		
 		Intent i = new Intent(this, TagActivity.class);
 		i.putExtra("ID", character.getId());
		i.putExtra("TYPE", character.getItemType().toString());
		
 		startActivity(i);
 		
 		_dbHelper.addCharacter(character);
 		String tags = tagsToString(_dbHelper.getTags(character.getId()));
 		Log.i("TAGS", tags);
 		_tagText.setText(tags);
 	}
 	
 	public void onDisplayButtonClick(View view)
 	{
 		Log.i("CLICK", "DISPLAY");
 		setCharacterDisplayPane();
 	}
 
 }
