 package uk.ac.ox.cs.sokobanexam.ui;
 
 import uk.ac.ox.cs.sokobanexam.util.Point;
 
 /**
  * Listener used to inform when the selected or hover point of model has changed.
  * The previous points are sent along, so you may update a smaller part of the screen.
  */
 public interface SelectionChangeListener {
 	public void onSelectionChanged(MazeModel model, Point from, Point to);
 	public void onHighlightChanged(MazeModel mazeModel, Point from, Point to);
	public void onSelectionVisibilityChanged(MazeModel mazeModel);
 }
