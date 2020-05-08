 package ch.cern.atlas.apvs.client.ui;
 
 import ch.cern.atlas.apvs.client.ClientFactory;
 import ch.cern.atlas.apvs.client.event.SelectTabEvent;
 
 import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
 import com.google.web.bindery.event.shared.EventBus;
 
 public class Tab implements Module {
 
 	public static native void setVisible(Element elem, boolean visible) /*-{
 																		elem.style.display = visible ? '' : 'none';
 																		}-*/;
 
 	private String tab;
 
 	public Tab() {
 	}
 
 	@Override
 	public boolean configure(final Element element, ClientFactory clientFactory, Arguments args) {
 		
 		EventBus eventBus = clientFactory.getEventBus(args.getArg(0));
 		
 		this.tab = args.getArg(1);
 		
 		setVisible(element, false);
 		
 		SelectTabEvent.subscribe(eventBus, new SelectTabEvent.Handler() {
 			
 			@Override
 			public void onTabSelected(SelectTabEvent event) {
 				setVisible(element, event.getTab().equals(tab));
 			}
 		});
 		
 		return false;
 	}
 	
 	@Override
 	public boolean update() {
 		return false;
 	}
 }
