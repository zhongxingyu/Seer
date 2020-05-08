 package com.vloxlands.scene;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.Display;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.TextureImpl;
 
 import com.vloxlands.game.Game;
 import com.vloxlands.game.world.Map;
 import com.vloxlands.gen.MapGenerator;
 import com.vloxlands.gen.MapGenerator.MapSize;
 import com.vloxlands.net.Player;
 import com.vloxlands.net.Server;
 import com.vloxlands.net.packet.Packet;
 import com.vloxlands.net.packet.Packet.PacketTypes;
 import com.vloxlands.net.packet.Packet0Connect;
 import com.vloxlands.net.packet.Packet3ChatMessage;
 import com.vloxlands.net.packet.Packet4ServerInfo;
 import com.vloxlands.net.packet.Packet6Player;
 import com.vloxlands.net.packet.Packet7Settings;
 import com.vloxlands.net.packet.Packet8Attribute;
 import com.vloxlands.settings.Tr;
 import com.vloxlands.ui.ArrowButton;
 import com.vloxlands.ui.ChatContainer;
 import com.vloxlands.ui.ColorLabel;
 import com.vloxlands.ui.Container;
 import com.vloxlands.ui.GuiRotation;
 import com.vloxlands.ui.IGuiElement;
 import com.vloxlands.ui.IGuiEvent;
 import com.vloxlands.ui.InputField;
 import com.vloxlands.ui.Label;
 import com.vloxlands.ui.LobbySlot;
 import com.vloxlands.ui.ProgressBar;
 import com.vloxlands.ui.Spinner;
 import com.vloxlands.ui.TextButton;
 import com.vloxlands.util.Assistant;
 import com.vloxlands.util.RenderAssistant;
 
 public class SceneNewGame extends Scene
 {
 	ProgressBar progressBar;
 	TextButton start, back, disco;
 	Spinner mapsize;
 	
 	static Container lobby;
 	static ChatContainer chat;
 	static Container otherColors;
 	static float progress;
 	static String progressString;
 	static boolean wasRejoining = false;
 	
 	@Override
 	public void init()
 	{
 		if (Game.server == null && !Game.client.isConnected())
 		{
 			Game.server = new Server(Game.IP); // host
 			chat = null;
 			lobby = null;
 			progress = 0;
 			progressString = "";
 			wasRejoining = false;
 		}
 		
 		Game.currentMap = new Map();
 		
 		setBackground();
 		
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
 		
 		if (chat == null)
 		{
 			chat = new ChatContainer(0, 115 + Display.getHeight() - 220 - TextButton.HEIGHT - 150, Display.getWidth() - TextButton.WIDTH - 90, TextButton.HEIGHT + 150);
 		}
 		chat.setX(0);
 		chat.setY(115 + Display.getHeight() - 220 - TextButton.HEIGHT - 150);
 		chat.setWidth(Display.getWidth() - TextButton.WIDTH - 90);
 		chat.setHeight(TextButton.HEIGHT + 150);
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
 						Game.client.sendPacket(new Packet3ChatMessage(Game.client.getUsername(), msg));
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
 		
 		progressBar = new ProgressBar(Display.getWidth() / 2, Display.getHeight() / 2 - ProgressBar.HEIGHT / 2, 400, 0, false);
 		progressBar.setVisible(false);
 		content.add(progressBar);
 		
 		disco = new TextButton(Display.getWidth() / 2 - TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("disconnect"));
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
 		
 		back = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH / 2, Display.getHeight() - TextButton.HEIGHT, Tr._("back"));
 		back.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.currentGame.setScene(new SceneMainMenu());
 			}
 		});
 		content.add(back);
 		
 		start = new TextButton(Display.getWidth() / 2 + TextButton.WIDTH, Display.getHeight() - TextButton.HEIGHT, Tr._("start"));
 		start.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				Game.server.setMapGenerator(new MapGenerator(Game.server.getConnectedClientCount(), MapSize.values()[mapsize.getValue()]));
 			}
 		});
 		
 		content.add(new Label(Display.getWidth() - TextButton.WIDTH - 70, 130, (TextButton.WIDTH + 70) / 2, 25, Tr._("mapsize") + ":", false));
 		mapsize = new Spinner(Display.getWidth() - TextButton.WIDTH - 70, 155, TextButton.WIDTH + 55, 0, MapSize.values().length, 1, 1, GuiRotation.HORIZONTAL);
 		mapsize.setArrowButtonTypes(ArrowButton.ARROW_L_HOR, ArrowButton.ARROW_R_HOR);
 		mapsize.setClickEvent(new IGuiEvent()
 		{
 			@Override
 			public void trigger()
 			{
 				try
 				{
 					Game.client.sendPacket(new Packet7Settings("mapsize", mapsize.getValue() + ""));
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 			}
 		});
 		String[] titles = new String[MapSize.values().length];
 		for (int i = 0; i < titles.length; i++)
 		{
 			MapSize ms = MapSize.values()[i];
 			titles[i] = Tr._("mapsize." + ms.name().toLowerCase()) + " " + ms.getSize() + "x" + ms.getSize();
 		}
 		mapsize.setTitles(titles);
 		content.add(mapsize);
 		
 		int size = 35;
 		int spacing = 5;
 		int inRow = 3;
 		// width - 400, height - 20 - 6
 		otherColors = new Container(0, 0, 30 + inRow * (size + spacing), 30 + (int) Math.ceil(Player.COLORS.length / (float) inRow) * (size + spacing), true, true);
 		otherColors.setZIndex(10);
 		otherColors.setVisible(false);
 		
 		for (int i = 0; i < Player.COLORS.length; i++)
 		{
 			final ColorLabel cl = new ColorLabel(15 + (size + spacing) * (i % inRow), 15 + (size + spacing) * (i / inRow), size, Player.COLORS[i]);
 			cl.parent = otherColors;
 			cl.setZIndex(11);
 			cl.setClickEvent(new IGuiEvent()
 			{
 				@Override
 				public void trigger()
 				{
 					otherColors.setVisible(false);
 					otherColors.components.get(Assistant.asList(Player.COLORS).indexOf(Game.client.getPlayer().getColor())).setEnabled(true);
 					Game.client.getPlayer().setColor(cl.getColor());
 					cl.setEnabled(false);
 					try
 					{
 						Game.client.sendPacket(new Packet6Player(Game.client.getPlayer()));
 					}
 					catch (IOException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			});
 			otherColors.add(cl);
 		}
 		content.add(otherColors);
 		
 		if (!Game.client.isConnected()) Game.client.connectToServer(Game.IP);
		if (Game.client.isConnectedToLocalhost())
		{
			start.setEnabled(true);
			back.setX(Display.getWidth() / 2 - TextButton.WIDTH / 2);
			disco.setX((int) (Display.getWidth() / 2 - TextButton.WIDTH * 1.5f));
			mapsize.setEnabled(Game.client.isConnectedToLocalhost());
			content.add(start);
		}
 		try
 		{
 			Game.client.sendPacket(new Packet4ServerInfo());
 			
 			if (Game.client.isRejoined())
 			{
 				lockScene();
 				Game.client.sendPacket(new Packet8Attribute("net_rejoin", "_"));
 				wasRejoining = true;
 				Game.client.resetRejoined();
 			}
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateLobby(Player[] pl)
 	{
 		ArrayList<Player> players = Assistant.asList(pl);
 		
 		@SuppressWarnings("unchecked")
 		ArrayList<IGuiElement> lobbyCopy = (ArrayList<IGuiElement>) lobby.components.clone();
 		lobby.components.clear();
 		
 		int index = 0;
 		for (IGuiElement iG : lobbyCopy)
 		{
 			if (players.indexOf(((LobbySlot) iG).getPlayer()) > -1)
 			{
 				iG.setY(15 + index * LobbySlot.HEIGHT2);
 				iG.setWidth(lobby.getWidth() - 20);
 				iG.setX(10);
 				lobby.add(iG);
 				players.remove(((LobbySlot) iG).getPlayer());
 				index++;
 			}
 		}
 		for (int i = 0; i < players.size(); i++)
 		{
 			LobbySlot slot = new LobbySlot(players.get(i));
 			slot.parent = lobby;
 			slot.setX(10);
 			slot.setY(15 + index * LobbySlot.HEIGHT2 + i * LobbySlot.HEIGHT2);
 			slot.setWidth(lobby.getWidth() - 20);
 			lobby.add(slot);
 		}
 		
 		for (IGuiElement iG : lobby.components)
 		{
 			final LobbySlot slot = (LobbySlot) iG;
 			
 			slot.initButtons();
 			boolean bool = slot.getPlayer().getUsername().equals(Game.client.getPlayer().getUsername());
 			
 			slot.components.get(0).setEnabled(bool); // rename
 			slot.components.get(1).setEnabled(Game.client.isConnectedToLocalhost() && !bool); // kick
 			slot.components.get(2).setEnabled(bool); // ready
 			ColorLabel cl = (ColorLabel) slot.components.get(3); // colorLabel
 			cl.setEnabled(bool);
 			
 			ArrayList<Color> colors = Assistant.asList(Player.COLORS);
 			for (IGuiElement g : otherColors.components)
 				g.setEnabled(true);
 			
 			for (Player player : pl)
 				otherColors.components.get(colors.indexOf(player.getColor())).setEnabled(false);
 			
 			cl.setClickEvent(new IGuiEvent()
 			{
 				@Override
 				public void trigger()
 				{
 					otherColors.setVisible(!otherColors.isVisible());
 					otherColors.setX(slot.getX() + slot.getWidth() - 400 + lobby.getX());
 					otherColors.setY(slot.getY() + slot.getHeight() - 26 + lobby.getY());
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void render()
 	{
 		if (!wasRejoining) super.render();
 		if (!chat.isEnabled())
 		{
 			TextureImpl.bindNone();
 			glEnable(GL_BLEND);
 			glColor4f(IGuiElement.gray.x, IGuiElement.gray.y, IGuiElement.gray.z, IGuiElement.gray.w);
 			
 			RenderAssistant.renderRect(0, 0, Display.getWidth(), Display.getHeight());
 			glColor4f(1, 1, 1, 1);
 			progressBar.setValue(progress);
 			progressBar.title = progressString;
 			progressBar.render();
 			glDisable(GL_BLEND);
 		}
 		
 		if (Game.currentMap.islands.size() == MapSize.values()[mapsize.getValue()].getSizeSQ()) progress = 0.5f + (Game.currentMap.initializedIslands / (float) Game.currentMap.islands.size()) / 2;
 		
 		if (Game.currentMap.initialized) Game.currentGame.setScene(new SceneGame());
 	}
 	
 	@Override
 	public void onTick()
 	{
 		super.onTick();
 		
 		if (Game.currentMap.islands.size() == MapSize.values()[mapsize.getValue()].getSizeSQ())
 		{
 			if (!Game.currentMap.initialized)
 			{
 				progressString = Tr._("renderchunks");
 				Game.currentMap.initMap();
 			}
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
 	public void onClientReveivedData(byte[] data)
 	{
 		PacketTypes type = Packet.lookupPacket(data[0]);
 		switch (type)
 		{
 			case CONNECT:
 			{
 				Packet0Connect p = new Packet0Connect(data);
 				if (p.getUsername().equals(Game.client.getUsername()))
 				{
 					if (Game.client.isConnectedToLocalhost())
 					{
 						start.setEnabled(true);
 						back.setX(Display.getWidth() / 2 - TextButton.WIDTH / 2);
 						disco.setX((int) (Display.getWidth() / 2 - TextButton.WIDTH * 1.5f));
 						mapsize.setEnabled(Game.client.isConnectedToLocalhost());
 						content.add(start);
 					}
 				}
 			}
 			
 			case RENAME:
 			{
 				try
 				{
 					Game.client.sendPacket(new Packet4ServerInfo());
 				}
 				catch (IOException e)
 				{
 					e.printStackTrace();
 				}
 				break;
 			}
 			case SERVERINFO:
 			{
 				updateLobby(new Packet4ServerInfo(data).getPlayers());
 				break;
 			}
 			case PLAYER:
 			{
 				Packet6Player p = new Packet6Player(data);
 				Player player = p.getPlayer();
 				for (IGuiElement iG : lobby.components)
 				{
 					LobbySlot slot = (LobbySlot) iG;
 					if (slot.getPlayer().getUsername().equals(player.getUsername()))
 					{
 						TextButton tb = (TextButton) slot.components.get(2); // ready
 						tb.setActive(player.isReady());
 						if (player.isReady()) tb.textColor = new Vector3f(124 / 256f, 222 / 256f, 106 / 256f);
 						else tb.textColor = new Vector3f(1, 1, 1);
 						ColorLabel cl = (ColorLabel) slot.components.get(3); // colorLabel
 						cl.setColor(player.getColor());
 						
 						if (Game.client.isConnectedToLocalhost()) start.setEnabled(Game.server.areAllClientsReady());
 						break;
 					}
 				}
 				break;
 			}
 			case SETTINGS:
 			{
 				Packet7Settings p = new Packet7Settings(data);
 				switch (p.getKey())
 				{
 					case "mapsize":
 					{
 						mapsize.setValue(Integer.parseInt(p.getValue()));
 						break;
 					}
 				}
 				break;
 			}
 			case ATTRIBUTE:
 			{
 				Packet8Attribute p = new Packet8Attribute(data);
 				if (p.getKey().equals("mapeditor_progress_float"))
 				{
 					progress = Float.parseFloat(p.getValue()) / 2;
 					lockScene();
 				}
 				if (p.getKey().equals("mapeditor_progress_string"))
 				{
 					progressString = Tr._(p.getValue());
 					lockScene();
 				}
 				break;
 			}
 			default:
 				break;
 		}
 	}
 }
