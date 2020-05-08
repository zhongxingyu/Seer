 package client;
 
 import gui.Scoreboard;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import model.Picture;
 
 import common.Protocol;
 
 public class ReceiverThread extends Thread {
 	
 	private DataInputStream dis;
 	private PictureWrapper picture;
 	private Scoreboard scoreboard;
 	
 	public ReceiverThread(PictureWrapper p, InputStream is) {
 		picture = p;
 		dis = new DataInputStream(is);
 	}
 	
 	public void run() {
 		try {
 			while (true)  {
 				Command command;
 				byte b = dis.readByte();
 				switch (b) {
 				case Protocol.CMD_SET_COLOR:
 					command = new ColorCommand(picture.getModel());
 					break;
 				case Protocol.CMD_SET_THICKNESS:
 					command = new ThicknessCommand(picture.getModel());
 					break;
 				case Protocol.DRAW_LINE_START:
 					command = new LineCommand(picture.getModel());
 					break;
 				case Protocol.DRAW_COORD_BULK:
 					command = new CoordCommand(picture.getModel());
 					break;
 				case Protocol.CMD_UNDO:
 					command = new UndoCommand(picture.getModel());
 					break;
 				case Protocol.CMD_CLEAR_ALL:
 					command = new ClearAllCommand(picture.getModel());
 					break;
 				case Protocol.CMD_UPDATE_RANKING:
 					command = new UpdateRankingCommand(scoreboard);
 					break;
 				case Protocol.CMD_DISABLE_DRAWING:
 					command = new DisableCommand(picture);
 					break;
				case Protocol.CMD_ENABLE_DRAWING:
 					command = new EnableCommand(picture);
 					break;
 				default:
 					command = new NoCommand();
 					break;
 				}
 				
 				command.perform(dis);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
