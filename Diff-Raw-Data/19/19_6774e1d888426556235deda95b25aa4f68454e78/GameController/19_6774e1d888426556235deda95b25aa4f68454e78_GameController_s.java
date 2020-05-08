 package org.frustra.tetrus;
 
 import java.io.IOException;
 import java.net.SocketException;
 
 public class GameController extends Thread {
 	{ this.setDaemon(true); }
 	
 	private TetrUs v;
 	
 	public GameController(TetrUs v) {
 		this.v = v;
 		this.start();
 	}
 	
 	public void run() {
 		long timer = System.currentTimeMillis();
 		long nextPlace = 0;
 		while (true) {
 			if (v.modeSelected && (v.singlePlayer || (v.loggedIn && !v.inLobby))) {
 				int tmpFallSpeed = v.keys[3] ? v.fallSpeed / 10 : v.fallSpeed;
 				if (System.currentTimeMillis() - timer > tmpFallSpeed) {
 					if (v.collideBlock(v.currPiece, v.currX, v.currY + 1)) {
 						if (nextPlace > 0 && System.currentTimeMillis() - nextPlace > (v.keys[3] ? v.placeWaitTime / 10 : v.placeWaitTime)) {
 							v.placeBlock(v.currPiece, v.currBlock, v.currX, v.currY);
 							if (!v.singlePlayer) {
 								if (!v.socket.isServerSocket()) {
 									synchronized (v.currPiece) {
 										try {
 											byte[] data = new byte[6 + (v.currPiece.length * v.currPiece[0].length)];
 											data[0] = (byte) v.currBlock;
 											data[1] = (byte) v.currX;
 											data[2] = (byte) v.currY;
 											data[3] = (byte) v.currPiece.length;
 											data[4] = (byte) v.currPiece[0].length;
 											int i = 5;
 											for (int x = 0; x < v.currPiece.length; x++) {
 												for (int y = 0; y < v.currPiece[x].length; y++) {
 													data[i++] = (byte) (v.currPiece[x][y] ? 1 : 0);
 												}
 											}
 											data[i] = v.lastRow;
 											v.socket.send(TetrUs.PACKET_PLACE, (byte) 0x00, data);
 										} catch (IOException e) {
 											if (!(e instanceof SocketException) || !e.getMessage().equals("Socket is closed")) e.printStackTrace();
											return;
 										}
 									}
 								} else {
 									synchronized (v.blocks) {
 										try {
 											byte[] data = new byte[1 + (v.blocks.length * v.blocks[0].length)];
 											int i = 0;
 											for (int x = 0; x < v.blocks.length; x++) {
 												for (int y = 0; y < v.blocks[x].length; y++) {
 													data[i++] = (byte) (v.blocks[x][y]);
 												}
 											}
 											v.socket.send(TetrUs.PACKET_BOARD, (byte) 0x00, data);
 										} catch (IOException e) {
 											if (!(e instanceof SocketException) || !e.getMessage().equals("Socket is closed")) e.printStackTrace();
											return;
 										}
 									}
 								}
 							}
 							v.getNewBlock();
 							nextPlace = 0;
 						} else if (nextPlace <= 0) nextPlace = System.currentTimeMillis();
 					} else {
 						nextPlace = 0;
 						v.drawTrigger = true;
 						v.currY++;
 					}
 					timer = System.currentTimeMillis();
 				}
 				if (System.currentTimeMillis() - v.keyTimer > 75 && (v.keys[0] || v.keys[1])) {
 					if (v.keys[0]) {
 						if (!v.collideBlock(v.currPiece, v.currX - 1, v.currY)) {
 							v.drawTrigger = true;
 							v.currX--;
 						}
 					} else if (v.keys[1]) {
 						if (!v.collideBlock(v.currPiece, v.currX + 1, v.currY)) {
 							v.drawTrigger = true;
 							v.currX++;
 						}
 					}
 					v.keyTimer = System.currentTimeMillis();
 				}
 			}
			
			try {
				Thread.sleep(v.logicSpeed);
			} catch (InterruptedException e) {
				return;
			}
 		}
 	}
 }
