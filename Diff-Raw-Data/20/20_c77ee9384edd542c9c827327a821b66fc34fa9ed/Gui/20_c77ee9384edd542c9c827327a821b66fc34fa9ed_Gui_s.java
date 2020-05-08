 package client.gui;
 
 import javax.swing.JPanel;
 
 public class Gui extends JPanel
 {	
 	private int w = 0;
 	private int h = 0;
 	
 	private GuiChat chat = new GuiChat();
 	
 	public Gui(int width, int height)
 	{
 		width = w;
 		height = h;
 	}
 	
 	public void guiCreate(int id)
 	{
 		switch(id) 
 		{
 			case 1: chat.guiChatCreate(w, h);
 		}
 	}
 	
 	public void guiDelete(int id)
 	{
 		switch(id)
 		{
 			case 1: chat.guiChatDestroy();
 		}
 	}
 }
