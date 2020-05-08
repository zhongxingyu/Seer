 package calico.plugins.historyrecorder.reader;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import calico.controllers.CCanvasController;
 import calico.networking.netstuff.CalicoPacket;
 import calico.plugins.historyrecorder.HistoryRecorderPlugin;
 
 public class CanvasHistoryEventProcessor {
 
 	/* Takes a packet and creates a file, returning the file name. */
 	public String processCanvasState(CalicoPacket p, long time, String clientName, long cuid) {
 
 			if (!(new File("processed_history_logs/")).exists())
 				(new File("processed_history_logs/")).mkdir();
 			
 			p.rewind();
 			//int comm = p.getInt();
 			
 //			if (comm != NetworkCommand.PRESENCE_LEAVE_CANVAS)
 //				return "";
 			
 //			if(clientName.compareTo("David") != 0){
 //				System.out.println("clientname = '"+clientName+"'");
 //				return "";
 //			}
 			
 			// For Dastyni: This is how you create an image
			System.out.println("Processing history event " + HistoryRecorderPlugin.count++);
 			BufferedImage bi = new BufferedImage(1200, 900, BufferedImage.TYPE_INT_ARGB);
 			Graphics2D ig2 = bi.createGraphics();
 			ig2.setColor(Color.white);
 			ig2.fillRect(0, 0, 1200, 900);
 			CCanvasController.canvases.get(cuid).render(ig2);
 
 			String imgName = "processed_history_logs/" + clientName + "_image_" + HistoryRecorderPlugin.count++;
 			try {
 				CalicoHistoryReader.save_to_disk(imgName, bi);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return imgName;
 		}
 }
