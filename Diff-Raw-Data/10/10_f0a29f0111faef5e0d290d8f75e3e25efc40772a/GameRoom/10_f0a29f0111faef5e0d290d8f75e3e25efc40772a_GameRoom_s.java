 package ru.narod.vn91.pointsop.gui;
 
 import ru.narod.vn91.pointsop.data.GameInfo;
 import ru.narod.vn91.pointsop.data.Player;
 import ru.narod.vn91.pointsop.data.Sgf;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface.MoveInfoAbstract;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface.MoveResult;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface.MoveType;
 import ru.narod.vn91.pointsop.php.PhpBackupServer;
 import ru.narod.vn91.pointsop.server.ServerInterface;
 import ru.narod.vn91.pointsop.sounds.Sounds;
 import ru.narod.vn91.pointsop.utils.Memory;
 import ru.narod.vn91.pointsop.utils.ObjectKeeper;
 import ru.narod.vn91.pointsop.utils.TimedAction;
 
 //import javax.jnlp.FileContents;
 //import javax.jnlp.FileOpenService;
 import javax.jnlp.FileSaveService;
 import javax.jnlp.ServiceManager;
 import javax.swing.*;
 import java.awt.event.MouseEvent;
 import java.io.ByteArrayInputStream;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class GameRoom extends javax.swing.JPanel implements RoomInterface {
 
 	GuiForServerInterface centralGuiController;
 	GameInfo gameInfo;
 	boolean amIPlaying, amIRed, redStopped = false, blueStopped = false;
 	ArrayList<MoveInfoAbstract> moveList = new ArrayList<MoveInfoAbstract>();
 	int mousePosX, mousePosY, redScore, blueScore;
 	boolean isRedTurnNow = true;
 	Sgf.GameResult gameResult = Sgf.GameResult.UNFINISHED;
 	Paper paper;
 	TimerLabel timerLabel_Red;
 	TimerLabel timerLabel_Blue;
 	Object synchronizationMakeMoveWhereMouse = new Object();
 	final ObjectKeeper<TimedAction> timedAction = new ObjectKeeper<TimedAction>();
 //	long timeLastTurnHappend;
 
 	public JPanel getMainJPanel() {
 		return this;
 	}
 
 	public RoomPart_Chat getRoomPart_Chat() {
 		return roomPart_Chat;
 	}
 
 	public RoomPart_Userlist getRoomPart_UserList() {
 		return roomPart_UserList;
 	}
 
 	public RoomPart_GameList getRoomPart_GameList() {
 		return null;
 	}
 
 	public Paper getRoomPart_Paper() {
 		return (Paper)jPanel_Paper;
 	}
 
 	public PrivateChat getPrivateChat() {
 		return null;
 	}
 
 	public ServerInterface getServer() {
 		return gameInfo.server;
 	}
 
 	public String getRoomNameOnServer() {
 		return gameInfo.id;
 	}
 
 	public boolean userAsksClose() {
 		if (gameInfo.server != null) {
 			gameInfo.server.unsubscribeRoom(gameInfo.id);
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public void makeMove(
 			boolean silent,
 			int x,
 			int y,
 			boolean isRed,
 			boolean isNowRed,
 			int remainingTimeRed,
 			int remainingTimeBlue) {
 		timedAction.o = null;
 		MoveResult moveResult = paper.makeMove(silent, x, y, isRed);
 		if (moveResult != MoveResult.ERROR) {
 			MoveInfoAbstract moveInfoAbstract = new MoveInfoAbstract();
 			moveInfoAbstract.coordX = x;
 			moveInfoAbstract.coordY = y;
 			moveInfoAbstract.moveType = (isRed) ? MoveType.RED : MoveType.BLUE;
 			moveList.add(moveInfoAbstract);
 			if (silent == false) {
 				// add a corresponding chat message
 				String eatInformation = "";
 				switch (moveResult) {
 					case BAD:
 						eatInformation = " + "
 								+ "игрок попался в ловушку и был сожран:) "
 								+ "ОСТОРОЖНЕЙ, если это произошло "
 								+ "после нажатия кнопки СТОП, "
 								+ "то из-за различных правил игры в pointsxt и pointsOp "
 								+ "поле может выглядеть по-разному!";
 						break;
 					case GOOD:
 						eatInformation = "+ :)";
 						break;
 					default:
 						break;
 				}
 				String colorName = isRed ? "кр" : "сн";
 				roomPart_Chat.addServerNotice(
 						"#" + moveList.size() + " " + colorName + " " + x + ":" + y + " " + eatInformation);
 
 				showScoreAndCursor();
 				new Sounds().playMakeMove(amIPlaying);
 				timerLabel_Red.setRemainingTime(remainingTimeRed, isNowRed);
 				timerLabel_Blue.setRemainingTime(remainingTimeBlue, !isNowRed);
 
 				// make move to current mouse position
 //				System.out.println("maked move. #" + moveList.size());
 				if (isOnlineGame()
 						&& Memory.isDebug() == true
 						&& amIPlaying == true
 						&& isNowRed == amIRed) {
 					final int moveListSize = moveList.size();
 					timedAction.o = new TimedAction() {
 
 						@Override
 						public boolean isAlive() {
 							return timedAction.o == this;
 						}
 
 						@Override
 						public void run() {
 							if (paper.getEngine().getDotType(mousePosX, mousePosY).isEmpty()
 									&&moveListSize == moveList.size()) {
 //								System.out.println("moveListSize = " + moveListSize);
 //								System.out.println("moveList.size() = " + moveList.size());
 								gameInfo.server.makeMove(gameInfo.id, mousePosX, mousePosY);
 							}
 						}
 
 					};
 					int secondsRemaining = amIRed ? remainingTimeRed : remainingTimeBlue;
 					timedAction.o.executeWhen(new Date().getTime()
 							+ secondsRemaining * 1000L - 200);
 				}
 			}
 		}
 	}
 
 	public void stopGame(boolean isRedPlayer) {
 		String whoPressedStop = (isRedPlayer) ? gameInfo.first.guiName : gameInfo.second.guiName;
 		if (isRedPlayer) {
 			redStopped = true;
 		} else {
 			blueStopped = true;
 		}
 		if (redStopped && blueStopped) {
 			if (redScore > blueScore) {
 				gameResult = Sgf.GameResult.RED_WON_END_OF_GAME;
 			} else if (redScore > blueScore) {
 				gameResult = Sgf.GameResult.BLUE_WON_END_OF_GAME;
 			} else if (redScore == blueScore) {
 				gameResult = Sgf.GameResult.DRAW_END_OF_GAME;
 			}
 			roomPart_Chat.addServerNotice(
 					"Оба игрока нажали кнопку СТОП. Игра закончена...");
 		} else {
 			roomPart_Chat.addServerNotice(whoPressedStop + " нажал кнопку СТОП");
 		}
 	}
 
 	public void gameLost(final boolean isRedLooser,
 			final boolean wantToSave) {
 		gameResult = (isRedLooser) ? Sgf.GameResult.BLUE_WON_BY_RESIGN : Sgf.GameResult.RED_WON_BY_RESIGN;
 		final String whoWon = (isRedLooser) ? gameInfo.second.guiName : gameInfo.first.guiName;
 		String whoLost = (isRedLooser) ? gameInfo.first.guiName : gameInfo.second.guiName;
 		roomPart_Chat.addServerNotice(
 				"" + whoLost + " сдался... Победитель - " + whoWon + ".");
 		new Thread() {
 
 			@Override
 			public void run() {
 				String eidokropkiLink = "";
 				if ((gameInfo.isRated == true) && (wantToSave == true)) {
 					eidokropkiLink = PhpBackupServer.sendToEidokropki(
 							gameInfo.first.guiName, gameInfo.second.guiName, gameInfo.first.rating, gameInfo.second.rating, 39, 32,
 							gameInfo.getTimeAsString(), gameResult, moveList);
 					getServer().sendChat(gameInfo.server.getMainRoom(),
 							"Закончена игра " + gameInfo.first.guiName + "-" + gameInfo.second.guiName
 							+ " ( " + eidokropkiLink + " ), победитель - " + whoWon + ". Поздравляем!");
 				}
 //				PhpBackupServer.sendToPointsgt(gameInfo.first.guiName, gameInfo.second.guiName,
 //						gameInfo.isRated, gameInfo.getTimeAsString(), isRedLooser, moveList,
 //						eidokropkiLink);
 			}
 		}.start();
 
 	}
 
 	public void paperClick(
 			int x,
 			int y,
 			MouseEvent evt) {
 		if (amIPlaying && evt.getButton() == MouseEvent.BUTTON1) {
 			if (isOnlineGame()) {
 				gameInfo.server.makeMove(gameInfo.id, x, y);
 			} else {
 				makeMove(false, x, y, isRedTurnNow, !isRedTurnNow, 1800, 1800);
 				isRedTurnNow = !isRedTurnNow;
 			}
 		}
 	}
 
 	public void paperMouseMove(int x,
 			int y,
 			MouseEvent evt) {
 		mousePosX = x;
 		mousePosY = y;
 		showScoreAndCursor();
 	}
 
 	private boolean isOnlineGame() {
 		return gameInfo.server!=null;
 	}
 
 	private void showScoreAndCursor() {
 		String result = "<html>";
 		if (mousePosX != -1) {
 			result += String.format("<b>[%02d:%02d] </b>"
 					+ "<font size=-1>(pxt%02d:%02d)</font>, ",
 					mousePosX, mousePosY, mousePosX - 1, 32 - mousePosY);
 		}
 		result += String.format("счёт <b><font color=red>%s</font>"
 				+ "-<font color=blue>%s</font></b>", paper.getRedScore(),
 				paper.getBlueScore());
 		result += ", ход " + (moveList.size() + 1);
 		result += "</html>";
 		jLabel_MouseCoords.setText(result);
 
 	}
 
 	/** Creates new form ContainerRoom_Game */
 	public GameRoom(
 			GameInfo gameInfo,
 			GuiController centralGuiController,
 			boolean chatReadOnly,
 			boolean amIPlaying,
 			boolean amIRed) {
 		this.amIPlaying = amIPlaying;
 		this.amIRed = amIRed;
 		this.centralGuiController = centralGuiController;
 		this.gameInfo = gameInfo;
 		paper = new Paper() {
 
 			@Override
 			public void paperClick(int x,
 					int y,
 					MouseEvent evt) {
 				GameRoom.this.paperClick(x, y, evt);
 			}
 
 			@Override
 			public void paperMouseMove(int x,
 					int y,
 					MouseEvent evt) {
 				GameRoom.this.paperMouseMove(x, y, evt);
 			}
 		};
 		paper.initPaper(39, 32);
 		timerLabel_Red = new TimerLabel();
 		timerLabel_Blue = new TimerLabel();
 
 //		if (amIPlaying) {
 //			стартовая позиция скрест
 //			paper.makeMove(19, 16, false);
 //			paper.makeMove(19, 17, true);
 //			paper.makeMove(20, 16, true);
 //			paper.makeMove(20, 17, false);
 //		}
 
 		initComponents();
 		jPanel_Tree.setVisible(false);
 
 		roomPart_Chat.setReadOnly(chatReadOnly);
		roomPart_Chat.initChat(this, gameInfo.first.guiName, gameInfo.second.guiName);
 //		if (amIPlaying == false) {
 //		jButton_AdditionalActions.setVisible(false);
 		jButton_EndGame.setVisible(false);
 		jButton_Pass.setVisible(false);
 		jButton_Stop.setVisible(false);
 //		}
 		{
 			// netbeans debug
 			GameRoom this_copy = this;
 			roomPart_UserList.initRoomPart(this_copy, centralGuiController);
 		}
 	}
 
 	/** This method is called from within the constructor to
 	 * initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is
 	 * always regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
   // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
   private void initComponents() {
 
     jPopupMenu_AdditionalActions = new javax.swing.JPopupMenu();
     jMenuItem_SaveGame = new javax.swing.JMenuItem();
     jPanel_ForChat = new javax.swing.JPanel();
     roomPart_UserList = new ru.narod.vn91.pointsop.gui.RoomPart_Userlist();
     roomPart_Chat = new ru.narod.vn91.pointsop.gui.RoomPart_Chat();
     jButton_AdditionalActions = new javax.swing.JButton();
     jPanel_ForGame = new javax.swing.JPanel();
     jPanel_Paper = paper;
     jLabel_MouseCoords = new javax.swing.JLabel();
     jPanel1 = new javax.swing.JPanel();
     jToggleButton_ShowTree = new javax.swing.JToggleButton();
     jButton_Pass = new javax.swing.JButton();
     jButton_Stop = new javax.swing.JButton();
     jButton_EndGame = new javax.swing.JButton();
     jPanel2 = new javax.swing.JPanel();
     jLabel1 = timerLabel_Red;
     jButton1 = new javax.swing.JButton();
     jLabel2 = timerLabel_Blue;
     jPanel_Tree = new javax.swing.JPanel();
 
     jMenuItem_SaveGame.setText("jMenuItem1");
     jMenuItem_SaveGame.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         jMenuItem_SaveGameActionPerformed(evt);
       }
     });
     jPopupMenu_AdditionalActions.add(jMenuItem_SaveGame);
 
     jButton_AdditionalActions.setText("сохранить игру");
     jButton_AdditionalActions.setComponentPopupMenu(jPopupMenu_AdditionalActions);
     jButton_AdditionalActions.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         jButton_AdditionalActionsActionPerformed(evt);
       }
     });
 
     javax.swing.GroupLayout jPanel_ForChatLayout = new javax.swing.GroupLayout(jPanel_ForChat);
     jPanel_ForChat.setLayout(jPanel_ForChatLayout);
     jPanel_ForChatLayout.setHorizontalGroup(
       jPanel_ForChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addComponent(roomPart_UserList, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
       .addComponent(roomPart_Chat, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
       .addComponent(jButton_AdditionalActions, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
     );
     jPanel_ForChatLayout.setVerticalGroup(
       jPanel_ForChatLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel_ForChatLayout.createSequentialGroup()
         .addComponent(jButton_AdditionalActions)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(roomPart_UserList, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
         .addGap(18, 18, 18)
         .addComponent(roomPart_Chat, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout jPanel_PaperLayout = new javax.swing.GroupLayout(jPanel_Paper);
     jPanel_Paper.setLayout(jPanel_PaperLayout);
     jPanel_PaperLayout.setHorizontalGroup(
       jPanel_PaperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGap(0, 547, Short.MAX_VALUE)
     );
     jPanel_PaperLayout.setVerticalGroup(
       jPanel_PaperLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGap(0, 411, Short.MAX_VALUE)
     );
 
     jLabel_MouseCoords.setFont(jLabel_MouseCoords.getFont().deriveFont(jLabel_MouseCoords.getFont().getSize()+3f));
     jLabel_MouseCoords.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
     jLabel_MouseCoords.setText("  .  ");
 
     jToggleButton_ShowTree.setText(">");
     jToggleButton_ShowTree.addChangeListener(new javax.swing.event.ChangeListener() {
       public void stateChanged(javax.swing.event.ChangeEvent evt) {
         jToggleButton_ShowTreeStateChanged(evt);
       }
     });
     jToggleButton_ShowTree.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         jToggleButton_ShowTreeActionPerformed(evt);
       }
     });
 
     jButton_Pass.setText("пасс");
     jButton_Pass.setEnabled(false);
 
     jButton_Stop.setText("стоп");
     jButton_Stop.setEnabled(false);
 
     jButton_EndGame.setText("конец игры...");
     jButton_EndGame.setEnabled(false);
 
     jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
     jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getSize()+2f));
     jLabel1.setText("00:00");
 
     jButton1.setText("<Время>");
     jButton1.addActionListener(new java.awt.event.ActionListener() {
       public void actionPerformed(java.awt.event.ActionEvent evt) {
         jButton1ActionPerformed(evt);
       }
     });
 
     jLabel2.setFont(jLabel2.getFont().deriveFont(jLabel2.getFont().getSize()+2f));
     jLabel2.setText("00:00");
 
     javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
     jPanel2.setLayout(jPanel2Layout);
     jPanel2Layout.setHorizontalGroup(
       jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel2Layout.createSequentialGroup()
         .addComponent(jLabel2)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jButton1)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jLabel1))
     );
     jPanel2Layout.setVerticalGroup(
       jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
         .addComponent(jButton1)
         .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
     jPanel1.setLayout(jPanel1Layout);
     jPanel1Layout.setHorizontalGroup(
       jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel1Layout.createSequentialGroup()
         .addComponent(jToggleButton_ShowTree)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 92, Short.MAX_VALUE)
         .addComponent(jButton_Pass)
         .addGap(18, 18, 18)
         .addComponent(jButton_Stop)
         .addGap(18, 18, 18)
         .addComponent(jButton_EndGame)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
         .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
     );
     jPanel1Layout.setVerticalGroup(
       jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
         .addComponent(jToggleButton_ShowTree)
         .addComponent(jButton_EndGame)
         .addComponent(jButton_Stop)
         .addComponent(jButton_Pass))
       .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
     );
 
     jPanel_Tree.setBorder(javax.swing.BorderFactory.createTitledBorder("дерево ходов (пока не рабочее...)"));
 
     javax.swing.GroupLayout jPanel_TreeLayout = new javax.swing.GroupLayout(jPanel_Tree);
     jPanel_Tree.setLayout(jPanel_TreeLayout);
     jPanel_TreeLayout.setHorizontalGroup(
       jPanel_TreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGap(0, 535, Short.MAX_VALUE)
     );
     jPanel_TreeLayout.setVerticalGroup(
       jPanel_TreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGap(0, 0, Short.MAX_VALUE)
     );
 
     javax.swing.GroupLayout jPanel_ForGameLayout = new javax.swing.GroupLayout(jPanel_ForGame);
     jPanel_ForGame.setLayout(jPanel_ForGameLayout);
     jPanel_ForGameLayout.setHorizontalGroup(
       jPanel_ForGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addComponent(jLabel_MouseCoords, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
       .addComponent(jPanel_Tree, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
       .addComponent(jPanel_Paper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
       .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     );
     jPanel_ForGameLayout.setVerticalGroup(
       jPanel_ForGameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(jPanel_ForGameLayout.createSequentialGroup()
         .addComponent(jLabel_MouseCoords)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
         .addComponent(jPanel_Paper, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jPanel_Tree, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
     );
 
     javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
     this.setLayout(layout);
     layout.setHorizontalGroup(
       layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
         .addComponent(jPanel_ForGame, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
         .addComponent(jPanel_ForChat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
     );
     layout.setVerticalGroup(
       layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
       .addComponent(jPanel_ForChat, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
       .addComponent(jPanel_ForGame, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     );
   }// </editor-fold>//GEN-END:initComponents
 
 	private void jMenuItem_SaveGameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem_SaveGameActionPerformed
 
 	}//GEN-LAST:event_jMenuItem_SaveGameActionPerformed
 
 	private void jToggleButton_ShowTreeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jToggleButton_ShowTreeStateChanged
 //		System.out.println(timerLabel);
 //		timerLabel.setRemainingTime(5,false);
 		if (jToggleButton_ShowTree.isSelected()) {
 			jToggleButton_ShowTree.setText("v");
 			jPanel_Tree.setVisible(true);
 		} else {
 			jToggleButton_ShowTree.setText(">");
 			jPanel_Tree.setVisible(false);
 		}
 	}//GEN-LAST:event_jToggleButton_ShowTreeStateChanged
 
 	private void jToggleButton_ShowTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton_ShowTreeActionPerformed
 	}//GEN-LAST:event_jToggleButton_ShowTreeActionPerformed
 
 	private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 		JOptionPane.showMessageDialog(this, "Дополнительные действия с временем пока недоступны.");
 
 	}//GEN-LAST:event_jButton1ActionPerformed
 
 	private void jButton_AdditionalActionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_AdditionalActionsActionPerformed
 		String[] extensions = {".sgf", ".sgftochki"};
 		String sgfData = Sgf.constructSgf(
 				gameInfo.first.guiName, gameInfo.second.guiName,
 				gameInfo.first.rating, gameInfo.second.rating, 39, 32,
 				gameInfo.getTimeAsString(), gameResult, 0,
 				moveList, true);
 
 		try {
 			ByteArrayInputStream is = new ByteArrayInputStream(
 					sgfData.getBytes());
 //			FileOpenService fos = (FileOpenService) ServiceManager.lookup(
 //					"javax.jnlp.FileOpenService"
 //			);
 			FileSaveService fss = (FileSaveService) ServiceManager.lookup("javax.jnlp.FileSaveService");
 
 			//					userFirst + "-" + userSecond + " "
 			//					+ GlobalGuiSettings.myDateFormat + " "
 			//					+ GlobalGuiSettings.myTimeFormat,
 			//FileContents fileContents =
 			fss.saveFileDialog(
 					"Vasya - Frosya 2011-04-01", extensions,
 					is, "Vasya - Frosya 2011-04-01");
 		} catch (Exception e) {
 			System.err.println("Error: " + e.getMessage());
 		}
 	}//GEN-LAST:event_jButton_AdditionalActionsActionPerformed
 
   // Variables declaration - do not modify//GEN-BEGIN:variables
   private javax.swing.JButton jButton1;
   private javax.swing.JButton jButton_AdditionalActions;
   private javax.swing.JButton jButton_EndGame;
   private javax.swing.JButton jButton_Pass;
   private javax.swing.JButton jButton_Stop;
   private javax.swing.JLabel jLabel1;
   private javax.swing.JLabel jLabel2;
   private javax.swing.JLabel jLabel_MouseCoords;
   private javax.swing.JMenuItem jMenuItem_SaveGame;
   private javax.swing.JPanel jPanel1;
   private javax.swing.JPanel jPanel2;
   private javax.swing.JPanel jPanel_ForChat;
   private javax.swing.JPanel jPanel_ForGame;
   private javax.swing.JPanel jPanel_Paper;
   private javax.swing.JPanel jPanel_Tree;
   private javax.swing.JPopupMenu jPopupMenu_AdditionalActions;
   private javax.swing.JToggleButton jToggleButton_ShowTree;
   private ru.narod.vn91.pointsop.gui.RoomPart_Chat roomPart_Chat;
   private ru.narod.vn91.pointsop.gui.RoomPart_Userlist roomPart_UserList;
   // End of variables declaration//GEN-END:variables
 }
