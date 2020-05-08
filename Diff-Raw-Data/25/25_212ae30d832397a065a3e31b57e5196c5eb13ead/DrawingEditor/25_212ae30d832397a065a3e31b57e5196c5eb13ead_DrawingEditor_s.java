 package homework3.android;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.KeyEvent;
 
public class DrawingEditor extends TestInteractiveWindowGroup {
 
 	private Behavior newRect;
 	private Behavior newLine;
 	private Behavior moveBehavior;
 	private Behavior choiceBehavior;
 	private Text status;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	protected void setup() {
 		println("Press 'l' to draw lines");
 		println("Press 'r' to draw rectangles");
 		println("Press 's' to enter selection mode");
 		println("Press 'm' to enter move mode");
 		println("Currently drawing lines");
 		
 		newRect = new NewRectBehavior(Color.RED, 5, this);
 		newLine = new NewLineBehavior(Color.BLUE, 7, this);
 		moveBehavior = new MoveBehavior(this);
 		choiceBehavior = new ChoiceBehavior(this);
 		
 		m_behaviors.add(newLine);
 	}
 
 	private SelectionHandles makeSelectableObject(GraphicalObject child)
 	{
 		SelectionHandles result = new SelectionHandles(0, 0, 100, 100);
 		result.addChild(child);
 		return result;
 	}
 	
 	private void addSelectionHandles()
 	{
 		List<GraphicalObject> childrenCopy = new ArrayList<GraphicalObject>(getChildren());
 		for (GraphicalObject child : childrenCopy) {
 			removeChild(child);
 			addChild(makeSelectableObject(child));
 		}
 	}
 	
 	private void removeSelectionHandles()
 	{
 		List<GraphicalObject> childrenCopy = new ArrayList<GraphicalObject>(getChildren());
 		for (GraphicalObject child : childrenCopy) {
 			if(child instanceof SelectionHandles)
 			{
 				SelectionHandles sh = (SelectionHandles)child;
 				removeChild(child);
 				for (GraphicalObject child2 : sh.getChildren()) {
 					addChild(child2);
 				}
 			}
 		}
 	}
 	
 	
 	
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		m_behaviors.clear();
 		removeSelectionHandles();
 		// TODO Auto-generated method stub
 		if(keyCode == KeyEvent.KEYCODE_R)
 		{
 			m_behaviors.add(newRect);
 			println("currently drawing rectangles");
 		} else if (keyCode == KeyEvent.KEYCODE_S)
 		{
 			// todo: make all the items selectable
 			m_behaviors.add(choiceBehavior);
 			addSelectionHandles();
 			println("currently selecting items");
 		}else if (keyCode == KeyEvent.KEYCODE_L)
 		{
 			m_behaviors.add(newLine);
 			println("currently drawing lines");
 		}else if (keyCode == KeyEvent.KEYCODE_M)
 		{
 			m_behaviors.add(moveBehavior);
 			println("currently moving items");
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 
 }
