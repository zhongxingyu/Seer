 package com.powertac.tourney.services;
 
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.TimerTask;
 
 import javax.faces.context.FacesContext;
 
 import com.powertac.tourney.beans.Broker;
 import com.powertac.tourney.beans.Game;
 import com.powertac.tourney.beans.Machine;
 import com.powertac.tourney.beans.Machines;
 import com.powertac.tourney.beans.Scheduler;
 import com.powertac.tourney.beans.Tournaments;
 
 public class RunGame extends TimerTask {
 
 	private int registerRetry = 8;
 	private int bootstrapRetry = 100;
 	private boolean running = false;
 
 	String logSuffix = "sim-";// boot-game-" + game.getGameId() + "-tourney-"+
 								// game.getCompetitionName();
 	String tourneyUrl = "";// game.getTournamentSchedulerUrl();
 	String serverConfig = "";// game.getServerConfigUrl();
 	String bootstrapUrl = "sim";// This needs to be empty for jenkins to run a
 								// bootstrapgame.getBootstrapUrl();
 	String pomUrl = "";// game.getPomUrl();
 	String gameId = "";// String.valueOf(game.getGameId());
 	String brokers = "";
 	String machineName = "";
 	String destination = "";
 
 	public RunGame(int gameId, String tourneyUrl, String pomUrl,
 			String destination) {
 		this.gameId = String.valueOf(gameId);
 		this.tourneyUrl = tourneyUrl;
 		this.pomUrl = pomUrl;
 		this.destination = destination;
 
 		// Assumes Jenkins and TS live in the same location as per the install
 		this.serverConfig = tourneyUrl + "faces/properties.jsp?gameId="
 				+ this.gameId;
 	}
 
 	/***
 	 * Make sure a bootstrap has been run for the sim
 	 */
 	private void checkBootstrap() {
 		if (!running) {
 			Database db = new Database();
 
 			try {
 				if (db.isGameReady(Integer.parseInt(gameId))) {
 					this.bootstrapUrl = tourneyUrl + "/faces/pom.jsp?location="
 							+ gameId + "-boot.xml";
 					try {
 						db.startTrans();
 						db.updateGameStatusById(Integer.parseInt(gameId),
 								"game-pending");
 						db.commitTrans();
 						db.closeConnection();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				} else {
 					System.out
 							.println("Game: "
 									+ gameId
 									+ " reports that bootstrap is not ready! retring in 15 seconds... retries left: "
 									+ bootstrapRetry);
 
 					if (bootstrapRetry-- > 0) {
 						Thread.sleep(15000);
 						this.run();
 					} else {
 						// Exceed maximum retries kill scheduled task
 						// TODO: Clean up database after this
 						this.cancel();
 						// System.exit(0);
 					}
 				}
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				System.out
 						.println("Bootstrap Database error while scheduling sim!!");
 				this.cancel();
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	/***
 	 * Make sure brokers are registered for the tournament
 	 */
 	private void checkBrokers() {
 		if (!running) {
 			Database db = new Database();
 
 			int gId = Integer.parseInt(gameId);
 
 			try {
 
 				Game g = db.getGame(gId);
 
 				int numRegistered = 0;
 				if ((numRegistered = db.getNumberBrokersRegistered(g
 						.getTourneyId())) < 1) {
 					System.out.println("TourneyId: " + g.getTourneyId());
 					System.out
 							.println("No brokers registered for tournament waiting to start game "
 									+ g.getGameId()
 									+ " 2 minutes... retries left: "
 									+ registerRetry);
 					if (registerRetry-- > 0) {
 						Thread.sleep(120000);
 						this.run();
 					} else {
 						// Exceed maximum retries kill scheduled task
 						// TODO: Clean up database after this
 
 						this.cancel();
 						// System.exit(0);
 					}
 				} else {
 					System.out
 							.println("There are "
 									+ numRegistered
 									+ " brokers registered for tournament... starting sim");
 					this.brokers = "";
 
 					List<Broker> brokerList = db.getBrokersInGame(Integer
 							.parseInt(gameId));
 					for (Broker b : brokerList) {
 						this.brokers += b.getBrokerName() + ",";
 					}
 					int lastIndex = this.brokers.length();
 					this.brokers = this.brokers.substring(0, lastIndex - 1);
 
 					if (brokerList.size() < 1) {
 						System.out
 								.println("Error no brokers listed in database for gameId: "
 										+ gameId);
 						this.cancel();
 						// System.exit(0);
 					}
 
 				}
 
 			} catch (SQLException e) {
 				System.out
 						.println("Broker Database error while scheduling sim!!");
 				// System.exit(0);
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	private void checkMachineAvailable() {
 		Database db = new Database();
 		if (!running) {
 			try {
 				db.startTrans();
 				List<Machine> machines = db.getMachines();
 				List<Machine> available = new ArrayList<Machine>();
 				for (Machine m : machines) {
 					if (m.getStatus().equalsIgnoreCase("idle")
 							&& m.isAvailable()) {
 						available.add(m);
 					}
 				}
 				if (available.size() > 0) {
 
 					db.updateGameJmsUrlById(Integer.parseInt(gameId), "tcp://"
 							+ available.get(0).getUrl() + ":61616");
 					db.updateProperties(Integer.parseInt(gameId), "tcp://" 
 							+ available.get(0).getUrl() + ":61616", available.get(0).getVizQueue());
 					db.updateGameMachine(Integer.parseInt(gameId), available
 							.get(0).getMachineId());
 					db.updateGameViz(Integer.parseInt(gameId), "http://"+available.get(0).getVizUrl());
 					
 					db.setMachineStatus(available.get(0).getMachineId(),
 							"running");
 					this.machineName = available.get(0).getName();
 					System.out.println("Game: " + gameId
 							+ " running on machine: " + this.machineName);
 					db.commitTrans();
 				} else {
 					db.abortTrans();
 					System.out
 							.println("No machines available to run scheduled game: "
 									+ gameId + " ... will retry in 5 minutes");
 					Thread.sleep(300000);
 					this.run();
 				}
 			} catch (NumberFormatException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void run() {
 		// Check if a boot exists
 		checkBootstrap();
 		// Check if brokers are registered
 		checkBrokers();
 		// Check if there is a machine available to run the sim and set it
 		checkMachineAvailable();
 
 		String finalUrl = "http://localhost:8080/jenkins/job/"
 				+ "start-server-instance/buildWithParameters?"
 				+ "token=start-instance" + "&tourneyUrl=" + tourneyUrl
 				+ "&suffix=" + logSuffix + "&propUrl=" + serverConfig
 				+ "&pomUrl=" + pomUrl + "&bootUrl=" + bootstrapUrl
 				+ "&brokers=" + brokers + "&machine=" + machineName
 				+ "&gameId=" + gameId + "&destination=" + destination;
 
 		try {
 			if (!running) {
 				URL url = new URL(finalUrl);
 				URLConnection conn = url.openConnection();
 				// Get the response
 				InputStream input = conn.getInputStream();
 				System.out.println("Jenkins request to start simulation game: "
 						+ this.gameId);
 				System.out.println("Bootstrap url: " + bootstrapUrl);
 				this.running = true;
 			} else {
 				// Should not get here
 				System.out.println("Request already sent, what?");
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.out.println("Jenkins failure to start simulation game: "
 					+ this.gameId);
 		}
 
 	}
 
 }
