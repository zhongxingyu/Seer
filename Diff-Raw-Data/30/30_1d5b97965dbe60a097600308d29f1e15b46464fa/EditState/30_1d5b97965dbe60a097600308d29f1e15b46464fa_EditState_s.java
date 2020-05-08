 package uk.ac.ox.cs.sokobanexam.ui;
 
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import uk.ac.ox.cs.sokobanexam.model.Maze;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Floor;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Nothing;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Room;
 import uk.ac.ox.cs.sokobanexam.model.sprites.Sprite;
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 /**
  * The state of the MazeController in which new objects can be edited, deleted or moved.
  */
 public class EditState implements ControllerState, MouseListener,
 		MouseMotionListener, KeyListener, ActionListener,
 		SelectionChangeListener, SpriteChangeListener {
 	
 	private MazeView mView;
 	private MazeModel mModel;
 	
 	private Toolbar mToolbar;
 	private JComponent mConfigurationPanel;
 	private JButton mDeleteButton;
 	
 	public EditState(Toolbar toolbar) {
 		mToolbar = toolbar;		
 		mDeleteButton = new JButton("Delete");
 		mDeleteButton.addActionListener(this);
 	}
 	
 	@Override
 	public void attach(MazeController controller, MazeModel model, MazeView view) {
 		mView = view;
 		mModel = model;
 		mView.addMouseListener(this);
 		mView.addMouseMotionListener(this);
 		mView.addKeyListener(this);
 		mView.requestFocusInWindow();
 		mModel.addSelectionChangeListener(this);
 		mModel.setSelectionVisible(true);
 		mToolbar.add(mDeleteButton);
 		onSelectionChanged(mModel, null, null);
 	}
 	
 	@Override
 	public void detach() {
 		mView.removeMouseListener(this);
 		mView.removeMouseMotionListener(this);
 		mView.removeKeyListener(this);
 		mModel.removeSelectionChangeListener(this);
 		mModel.setSelectionVisible(false);
 		if (mConfigurationPanel != null)
 			mToolbar.remove(mConfigurationPanel);
 		mToolbar.remove(mDeleteButton);
 		forceDoLayout();
 	}
 	
 	@Override
 	public void onSelectionChanged(MazeModel model, Point from, Point to) {
 		// Clear any old, custom configuration
 		if (mConfigurationPanel != null)
 			mToolbar.remove(mConfigurationPanel);
 		// If nothing is selected, we must disable the delete button
 		if (model.getSelected() == null) {
 			mDeleteButton.setEnabled(false);
 			mConfigurationPanel = null;
 		}
 		// Otherwise we generate a new component with the selected sprite
 		else {
 			mDeleteButton.setEnabled(true);
 			Room room = model.getMaze().getRoom(model.getSelected());
 			Sprite sprite = MazeController.isEditableType(room.inner()) ? room.inner() : room;
 			SpriteConfigurationCreator confCreator = new SpriteConfigurationCreator(this);
 			sprite.accept(confCreator);
 			mConfigurationPanel = confCreator.getResult();
 			mToolbar.add(mConfigurationPanel);
 		}
 		
 		forceDoLayout();
 	}
 
 	private void forceDoLayout() {
 		// Hack to get relayouting to work.
 		mToolbar.doLayout();
 		mToolbar.revalidate();
 		mToolbar.repaint();
 		// This part is necessary for small maps
 		if (mToolbar.getParent() != null) {
 			Container parent = mToolbar.getParent();
 			while (!(parent instanceof JFrame))
 				parent = parent.getParent();
 			((JFrame)parent).pack();
 		}
 	}
 	
 	@Override
 	public void onSpriteChanged(Sprite oldSprite, Sprite newSprite) {
 		Maze maze = mModel.getMaze();
 		if (newSprite instanceof Room)
 			maze.putRoom((Room)newSprite);
 		else maze.putRoom(maze.getRoom(oldSprite.point()).withInner(newSprite));
 		mModel.setMaze(maze);
 		mModel.setSelected(mModel.getSelected());
 	}
 	
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		mModel.setHighlighted(mView.pos2Point(e.getX(), e.getY()));
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		mouseMoved(e);
 	}
 	
 	@Override
 	public void mouseExited(MouseEvent e) {
 		mModel.setHighlighted(null);
 	}
 	
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Point point = mView.pos2Point(e.getX(), e.getY());
 		if (point == null) {
 			mModel.setSelected(null);
 			return;
 		}
 		Room room = mModel.getMaze().getRoom(point);
 		if (MazeController.isEditableType(room.inner())
 				|| MazeController.isEditableType(room)) {
 			mModel.setSelected(point);
 			// We request focus, so the delete keyboard shortcut works.
 			mView.requestFocusInWindow();
 		}
 		else
 			mModel.setSelected(null);
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		Point point = mView.pos2Point(e.getX(), e.getY());
 		if (mModel.getSelected() == null || point == null)
 			return;
 		
 		Room from = mModel.getMaze().getRoom(mModel.getSelected());
 		Room to = mModel.getMaze().getRoom(point);
 		if (from.point().equals(to.point()))
 			return;
 		
 		// See if we are moving an inner
 		if (MazeController.isEditableType(from.inner())) {
 			// We can't move it to somewhere with an inner
 			if (MazeController.isEditableType(to.inner())) {
 				JOptionPane.showMessageDialog(mView, "You can't drag on top of other things.");
 				return;
 			}
 			// We can move it somewhere else
 			else {
 				// Create new
 				mModel.getMaze().putRoom(to.withInner(from.inner().move(to.point())));
 				// Remove old
 				mModel.getMaze().putRoom(from.withInner(new Nothing(from.point())));
 			}
 		}
 		// If we are moving a room
 		else {
 			assert MazeController.isEditableType(from);
 			// If there is already a room
 			if (MazeController.isEditableType(to)) {
 				JOptionPane.showMessageDialog(mView, "You can't drag on top of other things.");
 				return;
 			}
 			// Move the room
 			else {
 				// Create new
 				mModel.getMaze().putRoom(from.withInner(to.inner()));
 				// Remove old
 				mModel.getMaze().putRoom(new Floor(from.point()));
 			}
 		}
 		
 		// Check validation
 		if (!mModel.isMazeLegal()) {
 			// Restore maze
 			mModel.getMaze().putRoom(to);
 			mModel.getMaze().putRoom(from);
 			JOptionPane.showMessageDialog(mView, "Dragging here is against the rules.");
 		}
 		else mModel.setSelected(to.point());
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_DELETE)
 			deleteSelected();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		assert e.getSource() == mDeleteButton;
 		deleteSelected();
 	}
 	
 	private void deleteSelected() {
 		Point point = mModel.getSelected();
 		if (point == null)
 			// Nothing selected
 			return;
 		Maze maze = mModel.getMaze();
 		Room room = maze.getRoom(point);
 		if (MazeController.isEditableType(room.inner()))
 			maze.putRoom(room.withInner(new Nothing(room.point())));
 		else maze.putRoom(new Floor(room.inner()));
 		mModel.setMaze(maze);
 		mModel.setSelected(null);
 	}
 	
 	@Override public void mouseClicked(MouseEvent e) {}
 	@Override public void mouseEntered(MouseEvent e) {}
 	@Override public void keyTyped(KeyEvent e) {}
 	@Override public void keyReleased(KeyEvent e) {}
 	@Override public void onHighlightChanged(MazeModel mazeModel, Point from, Point to) {}
 }
