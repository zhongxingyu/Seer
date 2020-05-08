 package edu.upenn.cis350.Trace2Learn.Database;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 public class Lesson extends LessonItem {
 	
 	private List<Long> _words;
 	
 	public Lesson(){
 		_type = ItemType.LESSON;
 		_words = new ArrayList<Long>();
 	}
 	
 	public synchronized void addWord(Long word){
 		_words.add(word);
 	}
 	
 	public synchronized List<Long> getWordIds(){
 		return new ArrayList<Long>(_words);
 	}
 	
 	public synchronized long getWordId(int i){
 		return _words.get(i).longValue();
 	}
 	
 	public synchronized List<LessonCharacter> getWords()
 	{
 		ArrayList<LessonCharacter> chars = new ArrayList<LessonCharacter>(_words.size());
 		for(Long id : _words)
 		{
 			if(_db == null) 
 			{
 				chars.add(new LessonCharacter(id));
 			}
 			else
 			{
 				LessonCharacter ch = _db.getCharacterById(id);
 				chars.add(ch);
 			}
 			
 		}
 		return chars;
 	}
 	
 	public int length()
 	{
 		return _words.size();
 	}
 	
 	public synchronized boolean removeWord(Long word){
 		return _words.remove(word);
 	}
 	
 	public synchronized long removeWord(int i){
 		return _words.remove(i).longValue();
 	}
 	
 	public synchronized void clearWords(){
 		_words.clear();
 	}
 	
 	@Override
 	public void draw(Canvas canvas, Paint paint, float left, float top, float width, float height, float time)
 	{
 		// TODO add animation code
 		draw(canvas, paint, left, top, width, height);
 	}
 	
	//@Override
	/*public void draw(Canvas canvas, Paint paint, float left, float top, float width, float height)
 	{
		int i = 0;
 		float charWidth = width/length();
 		for(Long id : _phrases)
 		{
 			LessonWord word;
 			if(_db == null)
 			{
 				word = new LessonWord(id);
 			}
 			else
 			{
 				word = _db.getCharacterById(id);
 			}
 			word.draw(canvas, paint, left + charWidth*i, top, charWidth, height);
 			i++;
		}
	}*/
 	
 	
 }
