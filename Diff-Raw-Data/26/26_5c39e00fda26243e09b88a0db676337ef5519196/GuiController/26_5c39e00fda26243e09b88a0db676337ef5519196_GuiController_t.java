 package ru.narod.vn91.pointsop.gui;
 
 
 import java.awt.Component;
 import java.awt.Image;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import ru.narod.vn91.pointsop.data.GameInfoListener;
 import ru.narod.vn91.pointsop.data.GameOuterInfo;
 import ru.narod.vn91.pointsop.data.GamePool;
 import ru.narod.vn91.pointsop.data.Player;
 import ru.narod.vn91.pointsop.data.PlayerPool;
 import ru.narod.vn91.pointsop.data.GameOuterInfo.GameState;
 import ru.narod.vn91.pointsop.server.AiVirtualServer;
 import ru.narod.vn91.pointsop.server.ServerInterface;
 import ru.narod.vn91.pointsop.sounds.Sounds;
 import ru.narod.vn91.pointsop.utils.Functor;
 import ru.narod.vn91.pointsop.utils.Memory;
 
 public class GuiController implements GuiForServerInterface {
 
   JTabbedPaneMod tabbedPane;
   JTextPane serverOutput;
   ServerInterface pointsxt_tochkiorg_server;
   ServerInterface pointsxt_ircworldru_server;
   ServerInterface pointsxt_vn91_server;
   ServerInterface zagramTwo_server;
   ServerInterface zagram_server;
   ServerInterface pointsopServer;
   HashMap<ServerRoom, RoomInterface> roomInterfaces = new HashMap<ServerRoom, RoomInterface>();
   HashMap<ServerRoom, GameRoom> gameRooms = new HashMap<ServerRoom, GameRoom>();
   HashMap<ServerRoom, LangRoom> langRooms = new HashMap<ServerRoom, LangRoom>();
   HashMap<Player, PrivateChat> privateChatList = new HashMap<Player, PrivateChat>();
   PlayerPool playerPool = new PlayerPool();
   GamePool gamePool = new GamePool();
 
   GuiController(final JTabbedPaneMod tabbedPane) {
     this.tabbedPane = tabbedPane;
   }
 
   public synchronized void serverClosed(ServerInterface server) {
     if (server == pointsxt_ircworldru_server) {
       pointsxt_ircworldru_server.disconnectServer();
       pointsxt_ircworldru_server = null;
     } else if (server == pointsxt_tochkiorg_server) {
       pointsxt_tochkiorg_server.disconnectServer();
       pointsxt_tochkiorg_server = null;
     } else if (server == pointsopServer) {
       pointsopServer.disconnectServer();
       pointsopServer = null;
     }
     if (!(server instanceof AiVirtualServer)) {
       rawError(
           server,
           "Отключился от сервера... \n"
               + "К сожалению, переподключиться в \"тихом\" режиме "
               + "пока-что невозможно. \n"
               + "Чтобы подключиться, закройте приложение и откройте его заново.");
     }
   }
 
   @Override
   public void updateUserInfo(
       ServerInterface server, String id,
       String guiName, Image image,
       Integer rating, Integer winCount, Integer lossCount, Integer drawCount,
       String status) {
     Player player = playerPool.get(server, id);
     Player updateInstance = new Player(server, id, guiName, rating, winCount,
           lossCount, drawCount, image, status);
     player.updateFrom(updateInstance);
   }
 
   @Override
   public void updateGameInfo(
 			ServerInterface server, String id, String masterRoomId,
 			String firstId, String secondId, Integer sizeX, Integer sizeY,
 			Boolean yAxisInverted, Boolean isRedFirst, Boolean isRated,
 			Integer handicapRed, Integer instantWin, Boolean manualEnclosings,
 			Boolean stopEnabled, Boolean isEmptyScored, GameState state,
 			Integer freeTemporalTime, Integer additionalAccumulatingTime,
 			Integer startingTime, Integer periodLength
       ) {
   GameOuterInfo gameOuterInfo = gamePool.get(server, id);
   Player first = playerPool.get(server, firstId);
   Player second = playerPool.get(server, secondId);
 
     GameOuterInfo updateInstance = new GameOuterInfo(
         server, secondId, masterRoomId, first, second,
         sizeX, sizeY, yAxisInverted,
         isRedFirst, isRated, handicapRed, instantWin,
         manualEnclosings, stopEnabled, isEmptyScored, state, freeTemporalTime,
         additionalAccumulatingTime, startingTime, periodLength);
   gameOuterInfo.updateFrom(updateInstance);
   }
 
   @Override
   public void userJoinedRoom(ServerInterface server, String room, String id,
       boolean isStartup) {
     Player player = playerPool.get(server, id);
     LangRoom langRoom = langRooms.get(new ServerRoom(room, server));
     GameRoom gameRoom = gameRooms.get(new ServerRoom(room, server));
     if (langRoom != null) {
       RoomPart_Userlist users = langRoom.getRoomPart_UserList();
       if (users != null) {
         users.userJoined(player);
       }
       RoomPart_Chat chat = langRoom.getRoomPart_Chat();
       if ((chat != null) && (isStartup == false)) {
         chat.addUserJoinedNotice(player);
       }
     } else if (gameRoom != null) {
       RoomPart_Userlist users = gameRoom.getRoomPart_UserList();
       if (users != null) {
         users.userJoined(player);
       }
       RoomPart_Chat chat = gameRoom.getRoomPart_Chat();
       if ((chat != null) && (isStartup == false)) {
         chat.addUserJoinedNotice(player);
       }
     }
   }
 
   @Override
   public synchronized void userLeftRoom(
       ServerInterface server,
       String roomId,
       String userId,
       String reason) {
     Player player = playerPool.get(server, userId);
     RoomInterface roomInterface = roomInterfaces.get(
         new ServerRoom(
             roomId,
             server
         )
         );
     if (roomInterface == null) {
     } else {
       RoomPart_Userlist users = roomInterface.getRoomPart_UserList();
       if (users != null) {
         roomInterface.getRoomPart_UserList().userLeave(player);
       }
       RoomPart_Chat chat = roomInterface.getRoomPart_Chat();
       if (chat != null) {
         chat.addUserLeftNotice(player.guiName, reason);
       }
     }
   }
 
   public synchronized void userDisconnected(
       ServerInterface server,
       String id,
       String additionalMessage) {
     Player player = playerPool.get(server, id);
     PrivateChat privateChat = privateChatList.get(player);
     if ((privateChat != null) &&
         (tabbedPane.contains(privateChat))) {
 			if (additionalMessage != null && additionalMessage.equals("") == false) {
 				privateChat.addChat("server",
 					player.guiName + " вышел из игры: " + additionalMessage,
 					true);
 			} else {
 				privateChat.addChat("server", player.guiName + " вышел из игры...", true);
 			}
     }
     playerPool.remove(server, id);
   }
 
   public synchronized void subscribedLangRoom(
       String roomNameOnServer,
       ServerInterface serverInterface,
       String guiRoomName,
       boolean isServersMainRoom) {
     LangRoom langRoom = langRooms.get(
         new ServerRoom(
             roomNameOnServer,
             serverInterface
         )
         );
 
     if (langRoom != null) {
       // nothing
     } else {
       langRoom = new LangRoom(serverInterface, roomNameOnServer, this);
       langRooms.put(
           new ServerRoom(roomNameOnServer, serverInterface),
           langRoom
           );
       roomInterfaces.put(
           new ServerRoom(roomNameOnServer, serverInterface),
           langRoom
           );
       tabbedPane.addTab(guiRoomName, langRoom, false);
       tabbedPane.makeBold(langRoom);
       if (isServersMainRoom) {
         tabbedPane.setSelectedComponent(langRoom);
       }
     }
   }
 
   @Override
   public synchronized void subscribedGame(
       ServerInterface server, String roomId) {
     GameOuterInfo game = gamePool.get(server, roomId);
     if (roomInterfaces.containsKey(new ServerRoom(game.id, server))) {
       return;
     }
     final GameRoom containerRoom_Game = new GameRoom(
         game,
         this);
     gameRooms.put(
         new ServerRoom(game.id, server),
         containerRoom_Game
         );
     roomInterfaces.put(
         new ServerRoom(game.id, server),
         containerRoom_Game
         );
 
 		final Functor<GameOuterInfo, String> game2html = new Functor<GameOuterInfo, String>() {
 			public String call(GameOuterInfo input) {
 				return String.format("<html><font color=%s>%s</font>" +
 						"<font color=black> - </font>" +
 						"<font color=%s>%s</font></html>",
						GlobalGuiSettings.getHtmlColor(input.player1Color()),
 						input.first.guiName,
						GlobalGuiSettings.getHtmlColor(input.player2Color()),
 						input.second.guiName
 						);
 			}
 		};
 		tabbedPane.addTab(game2html.call(game), containerRoom_Game, true);
 		tabbedPane.setCloseListener_FalseIfStopClosing(
 			containerRoom_Game,
 			new Functor<Void, Boolean>() {
 				@Override
 				public Boolean call(Void input) {
 					return containerRoom_Game.userAsksClose();
 				}
 			});
 		tabbedPane.setSelectedComponent(containerRoom_Game);
 		game.addChangeListener(
 			new GameInfoListener() {
 
 				@Override
 				public void onChange(GameOuterInfo gameOuterInfo) {
 					synchronized (GuiController.this) {
 						tabbedPane.updateTabText(containerRoom_Game, game2html.call(gameOuterInfo));
 					}
 				}
 			}
 		);
 
   };
 
   public synchronized void unsubscribedRoom(
       ServerInterface server,
       String room) {
     RoomInterface roomInterface = roomInterfaces.get(
         new ServerRoom(
             room,
             server
         )
         );
     if (roomInterface != null) {
       tabbedPane.remove((Component) roomInterface);
     }
     roomInterfaces.remove(new ServerRoom(room, server));
   }
 
   public synchronized void chatReceived(
       ServerInterface server,
       String room,
       String user,
       String message,
       Long time) {
     RoomInterface roomInterface = roomInterfaces.get(
         new ServerRoom(
             room,
             server
         )
         );
     if (roomInterface == null) {
     } else {
       RoomPart_Chat chat = roomInterface.getRoomPart_Chat();
       if (chat != null) {
         roomInterface.getRoomPart_Chat().addChat(user, message, time);
         tabbedPane.makeBold(roomInterface.getMainJPanel());
       }
     }
   }
 
   public synchronized void privateMessageReceived(
       ServerInterface server,
       String userId,
       String message) {
 
     Player player = playerPool.get(server, userId);
     PrivateChat privateChat = privateChatList.get(player);
     if (privateChat == null) {
       // creating new panel
       privateChat = new PrivateChat(player);
       privateChatList.put(player, privateChat);
       tabbedPane.addTab(player.guiName, privateChat, true);
     } else {
       if (tabbedPane.contains(privateChat) == false) {
         tabbedPane.addTab(
             player.guiName, privateChat, true
             ); // resurrecting a closed panel
       } else {
         // updating an old and not closed panel
       }
     }
     tabbedPane.makeBold(privateChat);
     privateChat.addChat(player.guiName, message, false);
   }
 
   @Override
   public void soundReceived(ServerInterface server, String user) {
     privateMessageReceived(server, user, "послан звуковой сигнал");
     Sounds.playAlarmSignal();
   }
 
   public synchronized void createPrivateChatWindow(
       Player player
       ) {
     PrivateChat privateChat = privateChatList.get(player);
     if (privateChat == null) {
       privateChat = new PrivateChat(player);
       privateChatList.put(player, privateChat);
       tabbedPane.addTab(player.guiName, privateChat, true);
       tabbedPane.setSelectedComponent(privateChat);
     } else {
 			if (tabbedPane.contains(privateChat) == false) {
         tabbedPane.addTab(player.guiName, privateChat, true);
       }
       tabbedPane.setSelectedComponent(privateChat);
     }
   }
 
   public synchronized void serverNoticeReceived(
       ServerInterface server,
       String room,
       String message) {
     RoomInterface roomInterface = roomInterfaces.get(
         new ServerRoom(
             room,
             server
         )
         );
     if (roomInterface == null) {
     } else {
       RoomPart_Chat chat = roomInterface.getRoomPart_Chat();
       if (chat != null) {
         roomInterface.getRoomPart_Chat().addServerNotice(message);
       }
     }
   }
 
   @Override
   public void gameRowCreated(
       ServerInterface server,
       String masterRoom,
       String newRoom) {
     GameOuterInfo gameOuterInfo = gamePool.get(server, newRoom);
     RoomInterface roomInterface = roomInterfaces.get(
         new ServerRoom(masterRoom, server));
     if (roomInterface == null) {
       this.raw(server, "creating a game with an incorrect MasterRoom");
     } else {
       roomInterface.getRoomPart_GameList().gameCreated(gameOuterInfo);
     }
 
   }
 
   @Override
   public void gameRowDestroyed(
       ServerInterface server,
       String oldRoom) {
     GameOuterInfo gameOuterInfo = gamePool.get(server, oldRoom);
     if (gameOuterInfo != null) {
       LangRoom room = langRooms.get(
           new ServerRoom(gameOuterInfo.masterRoomId, server));
       if (room != null) {
         room.getRoomPart_GameList().gameDestroyed(gameOuterInfo);
       }
     }
   }
 //public synchronized void gameDestroyed(
 //ServerInterface server,
 //String masterRoom,
 //String oldRoom) {
 //GameInfo gameInfo = gamePool.get(server, oldRoom, masterRoom);
 //LangRoom room = langRooms.get(new ServerRoom(masterRoom, server));
 //if (room == null) {
 //} else {
 //room.getRoomPart_GameList().gameDestroyed(gameInfo);
 //}
 //}
 
 
 //public synchronized void unsubsribedGame(
 //		ServerInterface server,
 //		String room) {
 //	RoomInterface roomInterface = roomInterfaces.get(
 //			new ServerRoom(
 //					room,
 //					server
 //			)
 //			);
 //	if (roomInterface != null) {
 //		tabbedPane.remove((Component) roomInterface);
 //	}
 //	roomInterfaces.remove(new ServerRoom(room, server));
 //}
 
   @Override
   public void gameInviteReceived(
       ServerInterface server,
       String room,
       String possibleOpponent) {
     Sounds.playAlarmSignal();
     server.acceptOpponent(room, possibleOpponent);
   }
 
 	public synchronized void makedMove(
 			ServerInterface server,
 			String room,
 			boolean silent,
 			int x,
 			int y,
 			boolean isRed,
 			boolean nowPlays,
 			int timeLeftRed, int timeLeftBlue) {
 		GameRoom gameRoom = gameRooms.get(new ServerRoom(room, server));
 		if (gameRoom != null) {
 			gameRoom.makeMove(silent, x, y, isRed, nowPlays, timeLeftRed,
 					timeLeftBlue);
 			tabbedPane.makeBold(gameRoom);
 		}
 	}
 
   public synchronized void gameStop(
       ServerInterface server,
       String room,
       boolean isRedPlayer) {
     GameRoom room_Game = gameRooms.get(new ServerRoom(room, server));
     if (room_Game != null) {
       gameRooms.get(new ServerRoom(room, server)).stopGame(isRedPlayer);
     }
   }
 
   public synchronized void gameLost(
       ServerInterface server,
       String room,
       boolean isRedLooser,
       boolean wantToSave) {
     GameRoom room_Game = gameRooms.get(new ServerRoom(room, server));
     if (room_Game != null) {
       gameRooms.get(new ServerRoom(room, server)).gameLost(
           isRedLooser,
           wantToSave
           );
     }
   }
 
 	@Override
 	public synchronized void raw(ServerInterface server, String info) {
 		String add = server.getServerName() + ": " + info + "\n";
 		if (Memory.isDebug() == true) {
 			System.out.print(add);
 		} else {
 			this.privateMessageReceived(server, server.getServerName(), info);
 //			String oldText = serverOutput.getText();
 //			serverOutput.setText(oldText + add);
 		}
 	}
 
   @Override
   public void rawError(ServerInterface server, String info) {
     this.raw(server, info);
     JOptionPane.showMessageDialog(
         null,
         info,
         "Error: " + info,
         JOptionPane.ERROR_MESSAGE
         );
   }
 
   public synchronized void activateGameRoom(
       ServerInterface server,
       String roomName) {
     try {
       tabbedPane.setSelectedComponent(
           gameRooms.get(
               new ServerRoom(
                   roomName, server
               )
               )
           );
     } catch (Exception ignored) {
     }
   }
 }
 
 class ServerRoom {
 
   String roomName;
   ServerInterface serverInterface;
 
   public ServerRoom(
       String roomName,
       ServerInterface serverInterface) {
     this.roomName = roomName;
     this.serverInterface = serverInterface;
   }
 
   @Override
   public boolean equals(Object compareToObject) {
     if (compareToObject instanceof ServerRoom == false) {
       return false;
       // avoid this stupid NetBeans hint...
     }
     if ((compareToObject == null)
         || (getClass().isInstance(compareToObject) == false)) {
       return false;
     }
     ServerRoom compareTo = getClass().cast(compareToObject);
     boolean roomNameEquals = (roomName == null)
         ? compareTo.roomName == null
         : roomName.equals(compareTo.roomName);
     boolean serverEquals = (serverInterface == null)
         ? compareTo.serverInterface == null
         : serverInterface.equals(compareTo.serverInterface);
     return roomNameEquals && serverEquals;
   }
 
   @Override
   public int hashCode() {
     int hash = 3;
     hash = 29 * hash + (this.roomName != null ? this.roomName.hashCode() : 0);
     hash = 29 * hash
         + (this.serverInterface != null ? this.serverInterface.hashCode() : 0);
     return hash;
   }
 }
 
 
 class JTabbedPaneMod {
 
 	JTabbedPane tabbedPane = new JTabbedPane();
 
 	Map<Component, Functor<Void, Boolean>> closeListeners =
 			new HashMap<Component, Functor<Void, Boolean>>();
 
 	public Component getComponent() {
 		return Component.class.cast(tabbedPane);
 	}
 
 	public void addTab(
 			String title,
 			final Component component,
 			boolean isCloseable) {
 		tabbedPane.addTab("", component);
 		TabCloseable tabCloseable = new TabCloseable(title, isCloseable);
 		tabCloseable.addCloseListener(new Functor<TabCloseable, Void>() {
 			@Override
 			public Void call(TabCloseable input) {
 				Functor<Void, Boolean> closeListener = closeListeners.get(component);
 				if (closeListener != null &&
 						closeListener.call(null) == Boolean.FALSE) {
 					// do nothing. Calling listeners is enough.
 				} else {
 					remove(component);
 				}
 				return null;
 			}
 		});
 		tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, tabCloseable);
 	}
 
 	public void setCloseListener_FalseIfStopClosing(
 			Component component,
 			Functor<Void, Boolean> closeListener) {
 		closeListeners.put(component, closeListener);
 	}
 
 	public boolean contains(Component component) {
 		return tabbedPane.indexOfComponent(component) >= 0;
 	}
 
 	public boolean isSelected(Component component) {
 		return tabbedPane.getSelectedComponent().equals(component);
 	}
 
 	public void setSelectedComponent(Component component) {
 		tabbedPane.setSelectedComponent(component);
 	}
 
 	public void remove(Component component) {
 		closeListeners.remove(component);
 		tabbedPane.remove(component);
 	}
 
 	private String getNotBoldNotHtml(String s) {
 		s = s.
 				replaceAll("<b>", "").replaceAll("</b>", "").
 				replaceAll("<html>", "").replaceAll("</html>", "").
 				replaceAll("\\*\\*\\*", "");
 		return s;
 	}
 
 	private String getNotBold(String s) {
 		return "<html>" + getNotBoldNotHtml(s) + "</html>";
 	}
 
 	private String getBold(String s) {
 		return "<html>***" + getNotBoldNotHtml(s) + "</html>";
 	}
 
 	public void makeBold(Component component) {
 		int tabIndex = tabbedPane.indexOfComponent(component);
 		if (tabIndex >= 0 &&
 				tabIndex != tabbedPane.getSelectedIndex()) {
 			Component panel = tabbedPane.getTabComponentAt(tabIndex);
 			// try {
 			TabCloseable tab = TabCloseable.class.cast(panel);
 			String newTitle = getBold(tab.getText());
 			if (newTitle.equals(tab.getText()) == false) {
 				tab.setText(newTitle);
 			}
 			// } catch (ClassCastException e) {
 			// }
 		}
 	}
 
 	public void updateTabText(Component component, String newTitle) {
 		int tabIndex = tabbedPane.indexOfComponent(component);
 		if (tabIndex >= 0) {
 			Component tabPanel = tabbedPane.getTabComponentAt(tabIndex);
 			// try {
 			TabCloseable tab = TabCloseable.class.cast(tabPanel);
 			tab.setText(newTitle);
 			// } catch (ClassCastException e) {
 			// }
 		}
 	}
 
 	public JTabbedPaneMod() {
 		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		tabbedPane.setFocusable(false);
 		tabbedPane.addChangeListener(new ChangeListener() {
 
 			public void stateChanged(ChangeEvent e) {
 				int selectedIndex = tabbedPane.getSelectedIndex();
 				Component panel = tabbedPane.getTabComponentAt(selectedIndex);
 				try {
 					TabCloseable tab = TabCloseable.class.cast(panel);
 					String s = tab.getText();
 					s = getNotBold(s);
 					tab.setText(s);
 				} catch (ClassCastException ex) {
 				} catch (NullPointerException ex) {
 				}
 			}
 		});
 	}
 }
