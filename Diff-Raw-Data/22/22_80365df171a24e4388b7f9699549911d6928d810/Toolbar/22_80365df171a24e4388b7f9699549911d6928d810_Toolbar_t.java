 package pipes.view.tools;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import pipes.editing.TuneEditController;
 import pipes.editing.TuneEditListener;
 import pipes.editing.actions.EditAction;
 import pipes.model.BeatDivision;
 import pipes.model.embellishment.EmbellishmentFamily;
 import pipes.view.TuneView;
 
 public class Toolbar extends JPanel implements TuneEditListener {
 	private static final long serialVersionUID = 1L;
 	private static final String ICON_PATH = "images/";
 
 	public void tuneEdited(EditAction action) {
 		undoButton.setEnabled(controller.canUndo());
 		redoButton.setEnabled(controller.canRedo());
 	}
 	
 	public Toolbar(TuneView view, final TuneEditController controller) {
 		this.controller = controller;
 		controller.addEditListener(this);
 		
		add(new PlaybackControls(controller));
 		
 		undoButton = new JButton("undo");
 		undoButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.undo();
 			}
 		});
 		undoButton.setEnabled(controller.canUndo());
 		
 		redoButton = new JButton("redo");
 		redoButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.redo();
 			}
 		});
 		redoButton.setEnabled(controller.canRedo());
 		
 		add(undoButton);
 		add(redoButton);
 		
 		ButtonGroup toolGroup = new ButtonGroup();
 		
 		JToggleButton pitchButton = new ToolButton(new ChangePitchTool(view, controller), "adjust_pitch_icon");
 		add(pitchButton);
 		toolGroup.add(pitchButton);
 
 		JToggleButton deleteButton = new ToolButton(new DeleteTool(view, controller), "delete_note_icon");
 		add(deleteButton);
 		toolGroup.add(deleteButton);
 
 		JToggleButton dotButton = new ToolButton(new ToggleDotTool(view, controller), "toggle_dot_icon");
 		add(dotButton);
 		toolGroup.add(dotButton);
 
 		JToggleButton addThirty = new ToolButton(new AddNoteTool(BeatDivision.THIRTY_SECOND, view, controller), "thirty_note_icon");
 		add(addThirty);
 		toolGroup.add(addThirty);
 
 		JToggleButton addSixteen = new ToolButton(new AddNoteTool(BeatDivision.SIXTEENTH, view, controller), "sixteenth_note_icon");
 		add(addSixteen);
 		toolGroup.add(addSixteen);
 
 		JToggleButton addEighth = new ToolButton(new AddNoteTool(BeatDivision.EIGHTH, view, controller), "eighth_note_icon");
 		add(addEighth);
 		toolGroup.add(addEighth);
 
 		JToggleButton addQuarter = new ToolButton(new AddNoteTool(BeatDivision.QUARTER, view, controller), "quarter_note_icon");
 		add(addQuarter);
 		toolGroup.add(addQuarter);
 
 		JToggleButton addHalf = new ToolButton(new AddNoteTool(BeatDivision.HALF, view, controller), "half_note_icon");
 		add(addHalf);
 		toolGroup.add(addHalf);
 
 		JToggleButton addWhole = new ToolButton(new AddNoteTool(BeatDivision.WHOLE, view, controller), "whole_note_icon");
 		add(addWhole);
 		toolGroup.add(addWhole);
 		
 		for (EmbellishmentFamily f : EmbellishmentFamily.EMBELLISHMENTS) {
 			JToggleButton eButton = new ToolButton(new SetEmbellishmentFamilyTool(view, controller, f), f.getShortName()+"_icon");
 			add(eButton);
 			toolGroup.add(eButton);
 		}
 	}
 	
 	private class ToolButton extends JToggleButton {
 		private static final long serialVersionUID = 1L;
 		
 		ToolButton(final EditTool tool, String iconName) {
 			setIcon(new ImageIcon(ICON_PATH + iconName + ".png"));
 			setToolTipText(tool.getName());
 			
 			setSize(new Dimension(40, 40));
 			setPreferredSize(new Dimension(40, 40));
 			
 			addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					controller.setCurrentTool(tool);
 				}
 			});
 		}
 	}
 
 	private TuneEditController controller;
 	
 	private JButton undoButton;
 	private JButton redoButton;
 }
