 package gt.editor.gui;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
 import org.getspout.spoutapi.gui.GenericButton;
 
 public abstract class ToggleButton extends GenericButton {
 
 	enum Side{A, B};
 	
 	private Side side;
 	
	public ToggleButton() {
		updateSide();
	}
	
	
 	public void updateSide() {
 		side = determineSide();
 		
 		switch (side) {
 		case A:
 			setText(getASideText());
 			break;
 		case B:
 			setText(getBSideText());
 			break;
 		}
 	}
 
 	@Override
 	public void onButtonClick(final ButtonClickEvent event) {			
 
 		switch (side) {
 		case A:
 			onASideClick(event);
 			break;
 		case B:
 			onBSideClick(event);
 			break;
 		}
 		
 		
 	};	
 	
 	
 	protected abstract String getASideText();
 	protected abstract String getBSideText();	
 	
 	protected abstract void onASideClick(ButtonClickEvent event);
 	protected abstract void onBSideClick(ButtonClickEvent event);
 	
 	protected abstract Side determineSide();
 }
