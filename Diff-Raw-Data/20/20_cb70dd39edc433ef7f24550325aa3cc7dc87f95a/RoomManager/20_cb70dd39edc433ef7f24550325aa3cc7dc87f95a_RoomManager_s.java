 package shared;
 
 import java.util.List;
 
 import lombok.Getter;
 
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
 
 	@Getter
 	private static MainWindow main;
 
 	public static Customs colorset;
 
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
 					String strippedLine = Colors.removeFormattingAndColors(m.getContent());
 			
 					if (r.getOutput() != null) {
 						r.getOutput().append("<" + m.getSender() + "> " + strippedLine);
 						r.getOutput().setSelection(r.getOutput().getText().length()); // scroll the output down
 					}
 					
 					List<StyleRange> styleRanges = ControlCodeParser.parseControlCodes(m.getContent(),
 							r.getOutput().getText().length() - strippedLine.length() );
 					
 					for(StyleRange styleRange : styleRanges.toArray(new StyleRange[styleRanges.size()]))
 						r.getOutput().setStyleRange(styleRange);
 			
					for (String s : m.getContent().split(" ")) {
 						if (s.contains("://")) {
 							// TODO: make this conform to the global list, and make the
 							// global list work
 							Color blue = new Color(r.getOutput().getDisplay(), 0, 0, 255);
 							StyleRange styleRange = new StyleRange();
 							styleRange.start = r.getOutput().getCharCount()
									- m.getContent().length() + m.getContent().indexOf(s);
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
