 package gui;
 
 import java.awt.Graphics;
 
 import instructions.ModeInstruction;
 import instructions.ModeInstructionType;
 import instructions.GenerateInstruction;
 import instructions.GenerateInstructionType;
 
 public class ModeToolbar extends Toolbar {
 	final static int BUTTONS = 3;
	
 	ToolbarButton _activeButton;
	
 	public ModeToolbar(DockController dockControl) {
 		super(dockControl, Orientation.VERTICAL);
		
 		createButtons();
 		dockControl.dockToLeft(this);
 	}
	
 	protected void createButtons(){
 		// add mode buttons (note, selection, zoom)
 		ToolbarButton modeNote = new ToolbarButton("images/mode/mode_note.png");
 		ToolbarButton modeSelection = new ToolbarButton("images/mode/mode_chord.png");
 		ToolbarButton modeZoom = new ToolbarButton("images/mode/mode_zoom.png");
		ToolbarButton genChords = new ToolbarButton("images/mode/gen_chord.png");
		ToolbarButton genVoices = new ToolbarButton("images/mode/gen_zoom.png");
		
 		modeNote.setInstruction(new ModeInstruction(this, ModeInstructionType.MODE, EditMode.NOTE));
 		modeSelection.setInstruction(new ModeInstruction(this, ModeInstructionType.MODE, EditMode.SELECTION));
 		modeZoom.setInstruction(new ModeInstruction(this, ModeInstructionType.MODE, EditMode.ZOOM));
 		genChords.setInstruction(new GenerateInstruction(this, GenerateInstructionType.CHORDS));
 		genVoices.setInstruction(new GenerateInstruction(this, GenerateInstructionType.VOICES));
		
 		_buttons = new ToolbarButton[]{modeNote, modeSelection, modeZoom};
 	}
 }
