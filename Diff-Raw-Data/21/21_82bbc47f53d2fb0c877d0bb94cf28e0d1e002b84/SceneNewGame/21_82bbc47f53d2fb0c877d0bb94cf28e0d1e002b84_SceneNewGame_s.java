 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.gen.MapGenerator;
 import com.vloxlands.net.Server;
 import com.vloxlands.net.packet.Packet;
 import com.vloxlands.net.packet.Packet01Disconnect;
 import com.vloxlands.net.packet.Packet03ChatMessage;
 import com.vloxlands.net.packet.Packet04ServerInfo;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.Action;
 import com.vloxlands.ui.ChatContainer;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.Dialog;
 import com.vloxlands.ui.GuiRotation;
 import com.vloxlands.ui.IGuiElement;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.InputField;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.LobbySlot;
 import com.vloxlands.ui.ProgressBar;
 import com.vloxlands.ui.Spinner;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.RenderAssistant;
 
 
 public class SceneNewGame extends Scene
 {
 	ProgressBar progress;
 	Spinner xSize, zSize, radius;
 	static Container lobby;
 	static ChatContainer chat;
 	
 	@Override
 	public void init()
 	{
 		if (Game.server == null && !Game.client.isConnected())
 		{
 			Game.server = new Server(Game.IP);
 		}
 		if (!Game.client.isConnected()) Game.client.connectToServer(Game.IP);
 		
 		try
 		{
 			Game.client.sendPacket(new Packet04ServerInfo());
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 		
 		setBackground();
 		// setUserZone();
 		
 		setTitle(Tr._("newGame"));
 		
 		if (lobby == null)
 		{
 			lobby = new Container(0, 115, Display.getWidth() - TextButton.WIDTH - 90, Display.getHeight() - 220);
 		}
 		lobby.setX(0);
 		lobby.setY(115);
 		lobby.setWidth(Display.getWidth() - TextButton.WIDTH - 90);
 		lobby.setHeight(Display.getHeight() - 220);
 		content.add(lobby);
 		
		chat = new ChatContainer(0, 115 + Display.getHeight() - 220 - TextButton.HEIGHT - 150, Display.getWidth() - TextButton.WIDTH - 90, TextButton.HEIGHT + 150);
 		content.add(chat);
 		
 		final InputField chatInput = new InputField(5, Display.getHeight() - 115 - InputField.HEIGHT - 15, Display.getWidth() - TextButton.WIDTH - 120, "", "");
 		chatInput.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				String msg = chatInput.getText();
 				if (msg.length() > 0)
 				{
 					try
 					{
 						Game.client.sendPacket(new Packet03ChatMessage(Game.client.getUsername(), msg));
 						chatInput.setText("");
 					}
 					catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 		content.add(chatInput);
 		
 		content.add(new Container(Display.getWidth() - TextButton.WIDTH - 90, 115, TextButton.WIDTH + 90, Display.getHeight() - 220));
 		
 		progress = new ProgressBar(Display.getWidth() / 2, Display.getHeight() / 2 - ProgressBar.HEIGHT / 2, 400, 0, true);
 		progress.setVisible(false);
 		content.add(progress);
 		
 		TextButton disco = new TextButton((int) (Display.getWidth() / 2 - TextButton.WIDTH * 1.5f), Display.getHeight() - TextButton.HEIGHT, Tr._("disconnect"));
 		disco.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.client.disconnect();
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		content.add(disco);
 		
 		TextButton back = new TextButton(Display.getWidth() / 2 - TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("back"));
 		back.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		content.add(back);
 		
 		TextButton skip = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("start"));
 		skip.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.mapGenerator = new MapGenerator(xSize.getValue(), zSize.getValue(), 20, 24);
 				Game.mapGenerator.start();
 				lockScene();
 				progress.setVisible(true);
 			}
 		});
 		content.add(skip);
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 130, (TextButton.WIDTH + 70) / 2, 25, "X-" + Tr._("islands") + ":", false));
 		xSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 125, (TextButton.WIDTH + 70) / 2, 1, 4, 1, 1, GuiRotation.HORIZONTAL);
 		content.add(xSize);
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 175, (TextButton.WIDTH + 70) / 2, 25, "Z-" + Tr._("islands") + ":", false));
 		zSize = new Spinner(Display.getWidth() - TextButton.WIDTH - 80 + (TextButton.WIDTH + 70) / 2, 170, (TextButton.WIDTH + 70) / 2, 1, 4, 1, 1, GuiRotation.HORIZONTAL);
 		content.add(zSize);
 	}
 	
 	private void updateLobby(String[] pl)
 	{
 		ArrayList<String> players = new ArrayList<>();
 		for (String p : pl)
 			players.add(p);
 		
 		@SuppressWarnings("unchecked")
 		ArrayList<IGuiElement> lobbyCopy = (ArrayList<IGuiElement>) lobby.components.clone();
 		lobby.components.clear();
 		
 		int index = 0;
 		for (IGuiElement iG : lobbyCopy)
 		{
 			if (players.indexOf(((LobbySlot) iG).getUsername()) > -1)
 			{
 				iG.setY(15 + index * LobbySlot.HEIGHT);
 				lobby.add(iG);
 				players.remove(((LobbySlot) iG).getUsername());
 				index++;
 			}
 		}
 		for (int i = 0; i < players.size(); i++)
 		{
 			LobbySlot slot = new LobbySlot(players.get(i));
 			slot.setX(15);
 			slot.setY(15 + index * LobbySlot.HEIGHT + i * LobbySlot.HEIGHT);
 			slot.setWidth(lobby.getWidth() - 30);
 			lobby.add(slot);
 		}
 	}
 	
 	@Override
 	public void onTick()
 	{
 		super.onTick();
 		
 		if (Game.currentMap != null) Game.currentGame.setScene(new SceneGame());
 	}
 	
 	@Override
 	public void render()
 	{
 		super.render();
 		
 		if (Game.mapGenerator != null)
 		{
 			glEnable(GL_BLEND);
 			glColor4f(IGuiElement.gray.x, IGuiElement.gray.y, IGuiElement.gray.z, IGuiElement.gray.w);
 			glBindTexture(GL_TEXTURE_2D, 0);
 			RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 			glColor4f(1, 1, 1, 1);
 			progress.setValue(Game.mapGenerator.progress);
 			progress.render();
 			glDisable(GL_BLEND);
 		}
 	}
 	
 	@Override
 	public void onClientMessage(String message)
 	{
 		String type = message.substring(0, message.indexOf("$"));
 		Vector3f color = new Vector3f(1, 1, 1);
 		switch (type)
 		{
 			case "INFO":
 			{
 				color = new Vector3f(0.9f, 0.9f, 0);
 				break;
 			}
 		}
 		chat.addMessage(message.substring(message.indexOf("$") + 1), color);
 	}
 	
 	@Override
 	public void onClientReveivedPacket(Packet packet)
 	{
 		switch (packet.getType())
 		{
 			case DISCONNECT:
 			{
 				Packet01Disconnect p = (Packet01Disconnect) packet;
 				if (p.getUsername().equals(Game.client.getUsername()))
 				{
 					if (!p.getReason().equals("mp.disconnect"))
 					{
 						Game.currentGame.addScene(new Dialog(Tr._("info"), Tr._(p.getReason()), new Action(Tr._("close"), new IGuiEvent()
 						{
 							@Override
 							public void trigger()
 							{
 								Game.currentGame.setScene(new SceneMainMenu());
 							}
 						})));
 					}
 				}
 				try
 				{
 					Game.client.sendPacket(new Packet04ServerInfo());
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				break;
 			}
 			case SERVERINFO:
 			{
 				updateLobby(((Packet04ServerInfo) packet).getPlayers());
 				break;
 			}
 			default:
 				break;
 		}
 	}
 }
