 package ca.cmput301f13t03.adventure_datetime.view.treeView;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.StoryFragment;
 import ca.cmput301f13t03.adventure_datetime.view.treeView.Camera;
 import ca.cmput301f13t03.adventure_datetime.view.treeView.Region;
 
 /**
  * Represents a story fragment on the tree view
  * @author Jesse
  *
  */
 class FragmentNode extends Region
 {
 	private static final int MAX_TXT_FOR_FIRST_LINE = 20;
 	private static final int MAX_TXT_FOR_SECOND_LINE = 15;
 	
 	private Bitmap m_backgroundImage = null;
 	private StoryFragment m_fragment = null;
 	private String m_lineOne = null;
 	private String m_lineTwo = null;
 	
 	private static Paint s_backgroundPaint = null;
 	private static Paint s_textPaint = null;
 	
 	// I love this feature!
 	static
 	{
 		s_backgroundPaint = new Paint();
		s_backgroundPaint.setAlpha(225);
 		
 		s_textPaint = new Paint();
 		s_textPaint.setColor(Color.BLACK);
 		s_textPaint.setTextAlign(Align.CENTER);
 		s_textPaint.setTextSize(40);
 	}
 	
 	public FragmentNode(StoryFragment fragment, Resources res)
 	{
 		super(0, 0, 0, 0); // width and height will be updated momentarily...
 		this.m_fragment = fragment;
 		m_backgroundImage = BitmapFactory.decodeResource(res, R.drawable.bk_fragment_node);
 		this.width = m_backgroundImage.getWidth();
 		this.height = m_backgroundImage.getHeight();
 		
 		String storyTxt = m_fragment.getStoryText();
 		
 		if(storyTxt.length() > MAX_TXT_FOR_FIRST_LINE + MAX_TXT_FOR_SECOND_LINE)
 		{
 			m_lineOne = ChopString(storyTxt, MAX_TXT_FOR_FIRST_LINE, 0);
 			m_lineTwo = ChopString(storyTxt, m_lineOne.length(), MAX_TXT_FOR_FIRST_LINE) + "...";
 		}
 		else if(storyTxt.length() > MAX_TXT_FOR_FIRST_LINE)
 		{
 			m_lineOne = ChopString(storyTxt, MAX_TXT_FOR_FIRST_LINE, 0);
 			m_lineTwo = storyTxt.substring(m_lineOne.length(), storyTxt.length());
 		}
 		else
 		{
 			m_lineOne = storyTxt;
 			m_lineTwo = "";
 		}
 	}
 	
 	/**
 	 * Helper function that finds an appropriate spot to cut the string
 	 * as close to the end as possible without chopping a word up
 	 */
 	private String ChopString(String str, int length, int start)
 	{
 		String result = str.substring(start, length + start);
 		int lastIndex = result.lastIndexOf(' ');
 		
 		if(lastIndex > 0)
 		{
 			return result.substring(0, lastIndex);
 		}
 		else
 		{
 			return result;
 		}
 	}
 	
 	public StoryFragment GetFragment()
 	{
 		return m_fragment;
 	}
 	
 	public void Draw(Canvas surface, Camera camera)
 	{
 		camera.DrawLocal(surface, s_backgroundPaint, m_backgroundImage, this.x, this.y);
 		camera.DrawLocal(surface, s_textPaint, m_lineOne, this.x + this.width / 2, this.y + this.height * 1 / 3);
 		camera.DrawLocal(surface, s_textPaint, m_lineTwo, this.x + this.width / 2, this.y + this.height * 2 / 3);
 	}
 }
