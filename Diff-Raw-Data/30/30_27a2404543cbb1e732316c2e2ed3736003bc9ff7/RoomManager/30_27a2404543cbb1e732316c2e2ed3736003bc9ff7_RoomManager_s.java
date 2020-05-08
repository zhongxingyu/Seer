 package shared;
 
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tray;
 import org.eclipse.swt.widgets.TrayItem;
 import org.eclipse.swt.widgets.Tree;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 
 import ui.composites.MainWindow;
 import ui.room.Room;
 import connection.Connection;
 import connection.KEllyBot;
 import connection.Settings;
 
 public class RoomManager {
 
 	public static MainWindow getMain() {return main;}
 	
 	private static MainWindow main;
 
 	public static Customs colorset = new Customs();
 
 	public static void enQueue(Message mes) {
 		filterMessage(mes);
 	}
 
 	public static void createRoom(final Composite c, final Tree tree,
 			final int style, final String channelstr,
 			final Connection newConnection, final int layout,
 			final Channel channel) {
 		if (!main.getDisplay().isDisposed()) {
 			if (canAddRoom(newConnection, channelstr)) {
 				main.getDisplay().asyncExec(new Runnable() {
 					public void run() {
 						Room r = new Room(c, style, layout, tree, channelstr,
 								newConnection, channel);
 						newConnection.addRoom(r);
 					}
 				});
 			}
 		}
 	}
 
 	public static void setMain(MainWindow w) {
 		main = w;
 		
 		initTray();
 	}
 
 	private static void initTray() {
 		Tray sysTray = main.getDisplay().getSystemTray();
 		if(sysTray != null) {
 			Image image = new Image(main.getDisplay(), "icon.png");
 			TrayItem item = new TrayItem(sysTray, SWT.NONE);
 			item.setToolTipText(KEllyBot.VERSION);
 			item.setImage(image);
 		}
 	}
 
 	public static boolean canAddRoom(Connection newConnection, String s) {
 		return newConnection.canAddRoom(s);
 	}
 
 	private static void filterMessage(final Message m) {
 		if (!main.getDisplay().isDisposed()) {
 			main.getDisplay().asyncExec(new Runnable() {
 				public void run(){
 					Room r = m.getBot().getConnection().findRoom(m.getChannel());
 					if (r == null)
 						return;
 					
 					if(Settings.getSettings().getNicksIgnored().contains(m.getSender()))
 							return;
 					
 					//use this so the nick tags <MyNick> get colored too. 
 					String msgWithNick;
 					if(m.getType() == Message.ACTION)
 						msgWithNick = "*** " + m.getSender() + " " + m.getContent();
 					else
 						msgWithNick = "<" + m.getSender() + "> " + m.getContent();
 					
 					//apply color for each type of message, except MSG because that's already default
 					if(m.getType() != Message.MSG)
 					{
 						String colorStr = Settings.getSettings().getOutputColors().get(m.getType());
 						msgWithNick = colorset.ircColorsStr.get(colorStr) + msgWithNick;
 					}
 					String strippedLine = Colors.removeFormattingAndColors(msgWithNick);
 			
 					if (r.getOutput() != null) {
 						int scrollPos = r.getOutput().getTopPixel();
 						int ySize = r.getOutput().getBounds().height;
 						boolean scrollDown = (scrollPos > (r.getOutput().getVerticalBar().getMaximum() - ySize));
 						switch(m.getType())
 						{
 						//TODO: Make PM and NOTICE and CONSOLE types do what they're supposed to do.
 						case Message.MSG:
 							r.updateLastMessage(strippedLine);
 							r.getOutput().append(strippedLine); 
 							if(strippedLine.toLowerCase().contains(m.getBot().getNick().toLowerCase()))
 								r.changeStatus(Room.NAME_CALLED);
 							else
 								r.changeStatus(Room.NEW_MESSAGE);
 							break;
 						case Message.PM:
 							r.updateLastMessage(strippedLine);
 							r.getOutput().append(strippedLine);
 							if(strippedLine.toLowerCase().contains(m.getBot().getNick().toLowerCase()))
 								r.changeStatus(Room.NAME_CALLED);
 							else
 								r.changeStatus(Room.NEW_MESSAGE);
 							break;
 						case Message.NOTICE: 
 							r.updateLastMessage(strippedLine);
 							r.getOutput().append(strippedLine);
 							if(strippedLine.toLowerCase().contains(m.getBot().getNick().toLowerCase()))
 								r.changeStatus(Room.NAME_CALLED);
 							else
 								r.changeStatus(Room.NEW_MESSAGE);
 							break;
 						case Message.CONSOLE: 
 							r.getOutput().append(strippedLine);
 							r.changeStatus(Room.NEW_IRC_EVENT);
 							break;
 						case Message.ACTION:
 							r.updateLastMessage(strippedLine);
 							r.getOutput().append(strippedLine);
 							if(strippedLine.toLowerCase().contains(m.getBot().getNick().toLowerCase()))
 								r.changeStatus(Room.NAME_CALLED);
 							else
 								r.changeStatus(Room.NEW_MESSAGE);
 							break;
 						
 						}
 						
 						if(scrollDown) r.getOutput().setSelection(r.getOutput().getText().length()); // scroll the output down
 					}
 					
 					List<StyleRange> styleRanges = ControlCodeParser.parseControlCodes(msgWithNick,
							r.getOutput().getText().length() - strippedLine.length() );
 					
 					for(StyleRange styleRange : styleRanges.toArray(new StyleRange[styleRanges.size()]))
 						r.getOutput().setStyleRange(styleRange);
 			
 					for (String s : strippedLine.split(" ")) {
 						if (s.contains("://")) {
 							// TODO: make this conform to the global list, and make the
 							// global list work
 							Color blue = new Color(r.getOutput().getDisplay(), 0, 0, 255);
 							StyleRange styleRange = new StyleRange();
 							styleRange.start = r.getOutput().getCharCount()
 									- strippedLine.length() + strippedLine.indexOf(s);
 							styleRange.length = s.length();
 							styleRange.foreground = blue;
 							styleRange.data = s;
 							styleRange.underline = true;
 							styleRange.underlineStyle = SWT.UNDERLINE_LINK;
 							r.getOutput().setStyleRange(styleRange);
 						}
 					}
 				}
 			});
 		}
 	}
 	
 }
