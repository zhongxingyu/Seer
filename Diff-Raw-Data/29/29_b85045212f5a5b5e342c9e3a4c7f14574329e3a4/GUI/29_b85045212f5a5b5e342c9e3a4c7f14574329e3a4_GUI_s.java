 package View;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import Control.Controller;
 import View.Components.*;
 
 public class GUI extends JFrame
 {
 
 	private static final long serialVersionUID = 1L;
 	PuzzleArea puzzleArea;
 	ToolBar toolbar;
 	WordListArea wordListArea;
 	Controller controller;
 	JTabbedPane tab;
 	CrosswordTab crossTab;
 	WordSearchTab wsTab;
 	int height, width;
 
 	public GUI(Controller control)
 	{
 		controller = control;
 		setTitle("WORD PUZZLES!");
 		setSize(1250, 900);
 		height = getSize().height;
 		width = getSize().width;
 		wsTab = new WordSearchTab();
 		crossTab = new CrosswordTab();
 
 		tab = new JTabbedPane();
 		
 		add(tab);
 		tab.addTab("Word Search", wsTab);
 		tab.addTab("Crossword", crossTab);
 		
 		tab.addChangeListener(new tabChangeListener());
 
 	}
 
 	class tabChangeListener implements ChangeListener
 	{
 
 		@Override
 		public void stateChanged(ChangeEvent arg0)
 		{
 			if (tab.getSelectedIndex() == 1)
 			{
 
 				puzzleArea
 						.setActivePuzzle(controller.getModel().getCrossWord());
 			}
 
 			else
 			{
				puzzleArea.setActivePuzzle(controller.getModel()
 						.getWordSearch());
 
 			}
 
 		}
 
 	}
 
 	class WordSearchTab extends JPanel
 	{
 
 		private static final long serialVersionUID = 1L;
 	
 		
 		public WordSearchTab()
 		{
			 puzzleArea = new PuzzleArea();
 			wordListArea = new WordListArea(height);
 			toolbar = new ToolBar(height);
 
 			add(toolbar, BorderLayout.WEST);
			add(puzzleArea);
 			add(wordListArea, BorderLayout.EAST);
 			revalidate();
 			repaint();
 		}
 
 	}
 
 	class CrosswordTab extends JPanel
 	{
 
 		private static final long serialVersionUID = 1L;
 
 		public CrosswordTab()
 		{
			puzzleArea = new PuzzleArea();
 			wordListArea = new WordListArea(height);
 			toolbar = new ToolBar(height);
 
 			add(toolbar, BorderLayout.WEST);
 			add(puzzleArea);
 			add(wordListArea, BorderLayout.EAST);
 			revalidate();
 			repaint();
 		}
 	}
 
 	public PuzzleArea getPuzzleArea()
 	{
 		return puzzleArea;
 	}
 
 	public void draw()
 	{
 		setVisible(true);
 	}
 
 }
