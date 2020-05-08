 package extgui;
 
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTextField;
 
 @SuppressWarnings("serial")
 public class LineEditor extends JTextField
 {
 	private List<String> histories = new ArrayList<String>();
 	private List<String> working = new ArrayList<String>();
 	private int limit;
 	private int pos;
 
 	public LineEditor()
 	{
 		this(32);
 	}
 
 	public LineEditor(int limit)
 	{
 		this.limit = limit;
 		addKeyListener(new KeyAdapter()
 		{
 			public void keyPressed(KeyEvent e)
 			{
 				if (e.getKeyCode() == KeyEvent.VK_UP)
 				{
 					up();
 				}
 				else if (e.getKeyCode() == KeyEvent.VK_DOWN)
 				{
 					down();
 				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					lineFeed();
				}
 			}
 		});
 		initWorking();
 	}
 
	private void lineFeed()
 	{
		histories.add(0, getText());
		if (histories.size() >= limit)
 		{
			histories.remove(histories.size() - 1);
 		}
 		initWorking();
 	}
 
 	private void initWorking()
 	{
 		working = new ArrayList<String>(histories);
 		working.add(0, "");
 		pos = 0;
 	}
 
 	private void up()
 	{
 		if (pos + 1 < working.size())
 		{
 			saveText();
 			pos++;
 			setText(working.get(pos));
 		}
 	}
 
 	private void down()
 	{
 		if (0 <= pos - 1)
 		{
 			saveText();
 			pos--;
 			setText(working.get(pos));
 		}
 	}
 
 	private void saveText()
 	{
 		working.set(pos, getText());
 	}
 }
