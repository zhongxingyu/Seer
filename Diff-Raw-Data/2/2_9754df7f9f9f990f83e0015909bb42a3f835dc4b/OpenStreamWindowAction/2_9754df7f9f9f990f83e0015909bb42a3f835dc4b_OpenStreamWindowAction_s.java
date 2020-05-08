 package cc.warlock.rcp.actions;
 
 import org.eclipse.jface.action.Action;
 
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.views.GameView;
 import cc.warlock.rcp.views.StreamView;
 
 public class OpenStreamWindowAction extends Action {
 
 	private String title, streamName;
 	
 	public OpenStreamWindowAction (String title, String streamName)
 	{
 		super(title, Action.AS_CHECK_BOX);
 		this.setImageDescriptor(WarlockSharedImages.getImageDescriptor(WarlockSharedImages.IMG_WINDOW));
 		
 		this.title = title;
 		this.streamName = streamName;
 	}
 	
 	@Override
 	public void run() {
 		
 		StreamView streamView = StreamView.getViewForStream(streamName);
 		streamView.setViewTitle(title);
		streamView.setAppendNewlines(true);
 		
 		GameView inFocus = GameView.getViewInFocus();
 		if (inFocus != null) {
 			streamView.setClient(inFocus.getStormFrontClient());
 		}
 		
 		setChecked(true);
 	}
 	
 	@Override
 	public String getText() {
  		return title;
 	}
 }
