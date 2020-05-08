 package cc.warlock.core.stormfront.internal;
 
 import java.util.HashMap;
 import java.util.Stack;
 
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.WarlockStringMarker;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.stormfront.IStormFrontProtocolHandler;
 import cc.warlock.core.stormfront.IStormFrontTagHandler;
 import cc.warlock.core.stormfront.xml.StormFrontAttributeList;
 
 public abstract class StyledSubTagHandler extends DefaultTagHandler {
 
 	private HashMap<String, IStormFrontTagHandler> tagHandlers =
 		new HashMap<String, IStormFrontTagHandler>();
 	protected WarlockString buffer;
 	private Stack<WarlockStringMarker> styleStack = new Stack<WarlockStringMarker>();
 	
 	public StyledSubTagHandler(IStormFrontProtocolHandler handler) {
 		super(handler);
 		
 		tagHandlers.put("b", new StyleBTagHandler());
 		tagHandlers.put("preset", new StylePresetTagHandler());
 	}
 	
 	@Override
 	public void handleStart(StormFrontAttributeList attributes, String rawXML) {
 		buffer = new WarlockString();
 	}
 	
 	@Override
 	public boolean handleCharacters(String characters) {
 		buffer.append(characters);
 		
 		return true;
 	}
 	
 	@Override
 	public void handleEnd(String rawXML) {
 		while(!styleStack.isEmpty()) {
 			WarlockStringMarker marker = styleStack.pop();
 			marker.setEnd(buffer.length());
 		}
 	}
 	
 	@Override
 	public IStormFrontTagHandler getTagHandler(String tagName) {
 		return tagHandlers.get(tagName);
 	}
 	
 	private void addStyle(WarlockStringMarker marker) {
 		if(buffer == null)
 			buffer = new WarlockString();
 		
 		if(styleStack.isEmpty()) {
 			buffer.addMarker(marker);
 			styleStack.push(marker);
 		} else {
 			styleStack.peek().addMarker(marker);
 			styleStack.push(marker);
 		}
 	}
 	
 	private void removeStyle(WarlockStringMarker marker) {
 		if(styleStack.isEmpty() || styleStack.peek() != marker)
 			return;
 		
 		styleStack.pop();
 		
 		marker.setEnd(buffer.length());
 	}
 	
 	private class StyleBTagHandler extends BaseTagHandler {
 
 		private WarlockStringMarker marker = null;
 
 		@Override
 		public String[] getTagNames() {
 			return new String[] { "b" };
 		}
 		
 		@Override
 		public void handleStart(StormFrontAttributeList attributes, String rawXML) {
 			IWarlockStyle style = handler.getClient().getClientSettings().getNamedStyle("bold");
 			marker = new WarlockStringMarker(style, buffer.length(), buffer.length());
 			addStyle(marker);
 		}
 		
 		@Override
 		public void handleEnd(String rawXML) {
 			removeStyle(marker);
 			marker = null;
 		}
 		
 		@Override
 		public boolean ignoreNewlines() {
 			return false;
 		}
 	}
 	
 	public class StylePresetTagHandler extends BaseTagHandler {
 		
 		private WarlockStringMarker marker = null;
 		
 		@Override
 		public String[] getTagNames() {
 			return new String[] { "preset" };
 		}
 		
 		@Override
 		public void handleStart(StormFrontAttributeList attributes, String rawXML) {
 			String id = attributes.getValue("id");
 			IWarlockStyle style = handler.getClient().getClientSettings().getNamedStyle(id);
 			if (style == null)
 				style = new WarlockStyle();
 			
 			marker = new WarlockStringMarker(style, buffer.length(), buffer.length());
 			addStyle(marker);
 		}
 		
 		@Override
 		public void handleEnd(String rawXML) {
 			removeStyle(marker);
 			marker = null;
 		}
 		
 		@Override
 		public  boolean ignoreNewlines() {
 			return false;
 		}
 
 	}
 
 }
