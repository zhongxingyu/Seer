 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth.controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 
 import taberystwyth.db.TabServer;
 import taberystwyth.view.TeamInsertionFrame;
 
 /**
  * @author Roberto Sarrionandia [r@sarrionandia.com]
  * @author Cal Paterson The listener for the TeamInsertionFrame
  * 
  */
 public class TeamInsertionListener implements ActionListener {
 
 	private static final Logger LOG = Logger
 			.getLogger(TeamInsertionListener.class);
 
 	TeamInsertionFrame frame;
 
 	/**
 	 * Constructor
 	 */
 	public TeamInsertionListener() {
 		this.frame = TeamInsertionFrame.getInstance();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Clear")) {
 			frame.getTeamName().setText("");
 			frame.getSwing().setSelected(false);
 
 			frame.getSpeaker1Name().setText("");
 			frame.getSpeaker1Institution().setText("");
 			frame.getSpeaker1ESL().setSelected(false);
 			frame.getSpeaker1Novice().setSelected(false);
 
 			frame.getSpeaker2Name().setText("");
 			frame.getSpeaker2Institution().setText("");
 			frame.getSpeaker2ESL().setSelected(false);
 			frame.getSpeaker2Novice().setSelected(false);
 		}
 
 		if (e.getActionCommand().equals("Save")) {
 			Connection sql;
 			try {
 				sql = TabServer.getConnectionPool().getConnection();
 			
				String speaker1 = "insert into speakers " +
						"(\"name\", " +
						"\"institution\", " +
						"\"esl\", " +
						"\"novice\") " +
						"values (?,?,?,?);";
 				PreparedStatement p1 = sql.prepareStatement(speaker1);
 				p1.setString(1, frame.getSpeaker1Name().getText());
 				p1.setString(2, frame.getSpeaker1Institution().getText());
 				p1.setBoolean(3, frame.getSpeaker1ESL().isSelected());
 				p1.setBoolean(4, frame.getSpeaker1Novice().isSelected());
 				LOG.debug("s1name = " + frame.getSpeaker1Name().getText());
 				p1.execute();
 				p1.close();
 
				String speaker2 = "insert into speakers " +
						"(\"name\", " +
						"\"institution\", " +
						"\"esl\", " +
						"\"novice\") " +
						"values (?,?,?,?);";
 				PreparedStatement p2 = sql.prepareStatement(speaker2);
 				p2.setString(1, frame.getSpeaker2Name().getText());
 				p2.setString(2, frame.getSpeaker2Institution().getText());
 				p2.setBoolean(3, frame.getSpeaker2ESL().isSelected());
 				p2.setBoolean(4, frame.getSpeaker2Novice().isSelected());
 				LOG.debug("s1name = " + frame.getSpeaker2Name().getText());
 				p2.execute();
 				p2.close();
 
				String team = "insert into teams " +
						"(\"speaker1\", " +
						"\"speaker2\", " +
						"\"name\") " +
						"values(?,?,?)";
 				PreparedStatement t = sql.prepareStatement(team);
 				t.setString(1, frame.getSpeaker1Name().getText());
 				t.setString(2, frame.getSpeaker2Name().getText());
 				t.setString(3, frame.getTeamName().getText());
 				t.execute();
 				t.close();
 
 				sql.commit();
 			} catch (SQLException e1) {
 				LOG.error("Unable to insert a team", e1);
 				JOptionPane.showMessageDialog(frame,
 						"Unable to insert that team into the database",
 						"Database Error", JOptionPane.ERROR_MESSAGE);
 			}
 			frame.getSpeaker1Name().setText("");
 			frame.getSpeaker1Institution().setText("");
 			frame.getSpeaker1ESL().setSelected(false);
 			frame.getSpeaker1Novice().setSelected(false);
 			frame.getSpeaker2Name().setText("");
 			frame.getSpeaker2Institution().setText("");
 			frame.getSpeaker2ESL().setSelected(false);
 			frame.getSpeaker2Novice().setSelected(false);
 			frame.getTeamName().setText("");
 		}
 		
 	}
 
 }
