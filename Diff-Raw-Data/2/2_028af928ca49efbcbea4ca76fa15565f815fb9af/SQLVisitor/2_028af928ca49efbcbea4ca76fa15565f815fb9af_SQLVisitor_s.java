 package visitors;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import jsonObjects.PBPJson;
 
 import nba.ContextInfo;
 import nba.Game;
 import nba.Period;
 import nba.PlayRole;
 import nba.Player;
 import nba.Possession;
 import nba.play.*;
 import nba.playType.PlayType;
 import nba.playType.block.Block;
 import nba.playType.ejection.Ejection;
 import nba.playType.foul.*;
 import nba.playType.freeThrow.FreeThrow;
 import nba.playType.jumpBall.JumpBall;
 import nba.playType.rebound.Rebound;
 import nba.playType.review.Review;
 import nba.playType.shot.*;
 import nba.playType.steal.Steal;
 import nba.playType.substitution.Substitution;
 import nba.playType.technical.*;
 import nba.playType.timeout.Timeout;
 import nba.playType.turnover.Turnover;
 import nba.playType.violation.Violation;
 import visitor.Visitor;
 
 public class SQLVisitor implements Visitor {
 
 	private Connection conn;
 	private PreparedStatement stmt;
 	private ResultSet rs;
 	private ArrayList<PBPJson> pbp;
 	private int gameID;
 	private int currentPeriodID, currentPossessionID, 
 					currentShotID, currentFoulID, currentTechnicalID,
 					currentStealID, currentTurnoverID, currentPlayerID,
 					currentFreeThrowID, homeID, awayID;
 	private boolean missed;
 	private ContextInfo currentContext;
 	private Possession currentPossession;
 	
 	public SQLVisitor(String path, String userName, String password, ArrayList<PBPJson> pbp,
 			int homeID, int awayID, int gameID)
 	{
 		this.pbp = pbp;
 		this.homeID = homeID;
 		this.awayID = awayID;
 		this.gameID = gameID;
 		Collections.sort(this.pbp, PBPJson.COMPARE_BY_PLAY_ID);
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(path,userName,password);
 		}
 		catch (ClassNotFoundException e) 
 		{
 			e.printStackTrace();
 		}
 		catch (SQLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void visit(ContextInfo contextInfo) {}
 
 	@Override
 	public void visit(Game game) 
 	{
 		for (Period p : game.getPeriods())
 		{
 			p.accept(this);
 		}
 	}
 
 	@Override
 	public void visit(Period period) 
 	{
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`period` (`period_identifier`)" +
 					"VALUES (?);");
 			stmt.setInt(1, period.getPeriod());
 		    stmt.executeUpdate();
 		    
 		    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.currentPeriodID = rs.getInt(1);
 		    } 
 		    else 
 		    {
 		        //TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`game_periods` (`game_id`,`period_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.gameID);
 			stmt.setInt(2, this.currentPeriodID);
 		    stmt.executeUpdate();
 		    
 		    for(Possession p : period.getPossessions())
 		    {
 		    	currentShotID = -1;
 		    	currentFoulID = -1;
 		    	currentTechnicalID = -1;
 		    	currentStealID = -1;
 		    	currentTurnoverID = -1;
 		    	currentPlayerID = -1;
 		    	currentFreeThrowID = -1;
 		    	p.accept(this);
 		    }
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Player player) {}
 
 	@Override
 	public void visit(Play play) 
 	{
 		currentContext = play.getContextInfo();
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(PlayerPlay play) 
 	{
 		currentContext = play.getContextInfo();
 		this.currentPlayerID = play.getPlayer().getPlayerID();
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(MissedPlay play) 
 	{
 		currentContext = play.getContextInfo();
 		this.missed = true;
 		this.currentPlayerID = play.getPlayer().getPlayerID();
 		play.getPlayType().accept(this);
 	}
 
 	@Override
 	public void visit(PlayType playType) {}
 
 	@Override
 	public void visit(Block block) 
 	{
 		int blockID = -1;
 		
 		if(this.currentShotID != -1)
 		{
 			try 
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`block` VALUES (DEFAULT);");
 				stmt.executeUpdate();
 				
 				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 			    if (rs.next()) 
 			    {
 			    	blockID = rs.getInt(1);
 			    }
 			    else 
 			    {
 			    	//TODO throw an exception from here
 			    }
 				
 			    stmt = conn.prepareStatement("INSERT INTO `nba2`.`block_player` (`block_id`,`player_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, blockID);
 				stmt.setInt(2, this.currentPlayerID);
 				stmt.executeUpdate();
 				
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`block_shot` (`shot_id`,`block_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, this.currentShotID);
 				stmt.setInt(2, blockID);
 				stmt.executeUpdate();
 			} 
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println("Unable to find shot associated with block");
 			//TODO error on shot not being found
 		}
 	}
 
 	@Override
 	public void visit(Ejection ejection) 
 	{
 		int ejectionID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`ejection` VALUES (DEFAULT);");
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	ejectionID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`ejection_player` (`ejection_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, ejectionID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`ejection_possession` (`ejection_id`,`possession_id`," +
 					"`time_of_ejection`) VALUES (?,?,?);");
 			stmt.setInt(1, ejectionID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Foul foul) 
 	{
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul` (`foul_type`,`team_foul`," +
 					"`personal_foul`) VALUES (?,?,?);");
 			stmt.setString(1, foul.getFoulType().getFoulType());
 			stmt.setBoolean(2, foul.getFoulType().teamFoul());
 			stmt.setBoolean(3, foul.getFoulType().personalFoul());
 			stmt.executeUpdate();
 		    
 		    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.currentFoulID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul_player` (`foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.currentFoulID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 		    
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul_possession` (`foul_id`," +
 					"`possession_id`, `time_of_foul`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentFoulID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(DoublePersonalFoul foul) 
 	{
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul` (`foul_type`,`team_foul`," +
 					"`personal_foul`) VALUES (?,?,?);");
 			stmt.setString(1, foul.getFoulType().getFoulType());
 			stmt.setBoolean(2, foul.getFoulType().teamFoul());
 			stmt.setBoolean(3, foul.getFoulType().personalFoul());
 			stmt.executeUpdate();
 		    
 		    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.currentFoulID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul_player` (`foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.currentFoulID);
 			stmt.setInt(2, foul.getPlayer1().getPlayerID());
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul_player` (`foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.currentFoulID);
 			stmt.setInt(2, foul.getPlayer2().getPlayerID());
 			stmt.executeUpdate();
 		    
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`foul_possession` (`foul_id`," +
 					"`possession_id`, `time_of_foul`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentFoulID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(FreeThrow freeThrow) 
 	{
 		try
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`free_throw` " +
 					"(`made`, `current_num`, `out_of`) " +
 					"VALUES (?,?,?);");
 			stmt.setBoolean(1, freeThrow.madeFT());
 			stmt.setInt(2, freeThrow.currentFTNumber());
 			stmt.setInt(3, freeThrow.outOfFTNumber());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	this.currentFreeThrowID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`free_throw_player` (`free_throw_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.currentFreeThrowID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`free_throw_possession` (`free_throw_id`,`possession_id`," +
 					"`time_of_free_throw`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentFreeThrowID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		}
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		if (freeThrow.getFreeThrowType().equals("Technical"))
 		{
 			if (this.currentTechnicalID != -1)
 			{
 				try 
 				{
 					stmt = conn.prepareStatement("INSERT INTO `nba2`.`free_throw_technical` (`technical_id`,`free_throw_id`)" +
 							"VALUES (?,?);");
 					stmt.setInt(1, this.currentTechnicalID);
 					stmt.setInt(2, this.currentFreeThrowID);
 					stmt.executeUpdate();
 				}
 				catch (SQLException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				//TODO error, couldn't find technical
 			}
 		}
 		else
 		{
 			if (this.currentFoulID != -1)
 			{
 				try
 				{
 					stmt = conn.prepareStatement("INSERT INTO `nba2`.`free_throw_foul` (`foul_id`,`free_throw_id`)" +
 							"VALUES (?,?);");
 					stmt.setInt(1, this.currentFoulID);
 					stmt.setInt(2, this.currentFreeThrowID);
 					stmt.executeUpdate();
 				}
 				catch (SQLException e) 
 				{
 					e.printStackTrace();
 				}
 			}
 			else
 			{
 				//TODO error, couldn't find foul
 			}
 		}
 	}
 
 	@Override
 	public void visit(JumpBall jumpBall) 
 	{
 		int jumpBallID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`jump_ball` VALUES (DEFAULT);");
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	jumpBallID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    if (jumpBall.getEnding().getTippedTo() != null)
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`jump_ball_players` (`jump_ball_id`," +
 		    			"`player_1_id`, `player_2_id`, `tipped_to_id`) VALUES (?,?,?,?);");
 				stmt.setInt(1, jumpBallID);
 				stmt.setInt(2, jumpBall.getPlayer1().getPlayerID());
 				stmt.setInt(3, jumpBall.getPlayer2().getPlayerID());
 				stmt.setInt(4, jumpBall.getEnding().getTippedTo().getPlayerID());
 				stmt.executeUpdate();
 		    }
 		    else
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`jump_ball_players` (`jump_ball_id`," +
 		    			"`player_1_id`, `player_2_id`) VALUES (?,?,?);");
 				stmt.setInt(1, jumpBallID);
 				stmt.setInt(2, jumpBall.getPlayer1().getPlayerID());
 				stmt.setInt(3, jumpBall.getPlayer2().getPlayerID());
 				stmt.executeUpdate();
 		    }
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`jump_ball_possession` (`jump_ball_id`,`possession_id`," +
 					"`time_of_jump`) VALUES (?,?,?);");
 			stmt.setInt(1, jumpBallID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Rebound rebound) 
 	{
 		int reboundID = -1;
 		boolean isDefensive = true;
 		
 		if (this.currentContext.getPlayRole().equals(PlayRole.HOME))
 		{
 			isDefensive = (this.homeID == this.currentPossession.getDefenseID());
 		}
 		else
 		{
 			isDefensive = (this.awayID == this.currentPossession.getDefenseID());
 		}
 		
 		if (this.currentFreeThrowID != -1)
 		{
 			try 
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound` (`defensive_rebound`) " +
 						"VALUES (?);");
 				stmt.setBoolean(1, isDefensive);
 				stmt.executeUpdate();
 				
 				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 			    if (rs.next()) 
 			    {
 			    	reboundID = rs.getInt(1);
 			    }
 			    else 
 			    {
 			    	//TODO throw an exception from here
 			    }
 				
 			    stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound_player` (`rebound_id`,`player_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, reboundID);
 				stmt.setInt(2, this.currentPlayerID);
 				stmt.executeUpdate();
 				
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound_free_throw` (`rebound_id`,`free_throw_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, reboundID);
 				stmt.setInt(2, this.currentFreeThrowID);
 				stmt.executeUpdate();
 			}
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else if (this.currentShotID != -1)
 		{
 			try 
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound` (`defensive_rebound`) " +
 						"VALUES (?);");
 				stmt.setBoolean(1, isDefensive);
 				stmt.executeUpdate();
 				
 				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 			    if (rs.next()) 
 			    {
 			    	reboundID = rs.getInt(1);
 			    }
 			    else 
 			    {
 			    	//TODO throw an exception from here
 			    }
 				
 			    stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound_player` (`rebound_id`,`player_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, reboundID);
 				stmt.setInt(2, this.currentPlayerID);
 				stmt.executeUpdate();
 				
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`rebound_shot` (`rebound_id`,`shot_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, reboundID);
 				stmt.setInt(2, this.currentShotID);
 				stmt.executeUpdate();
 			}
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			//TODO error, because there is no matching shot or FT
 		}
 	}
 
 	@Override
 	public void visit(Review review) {}
 
 	@Override
 	public void visit(Shot shot) 
 	{
 		boolean threePointShot = shot.getShotType() instanceof ThreePointShot;
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`shot` (`x`,`y`,`shot_made`," +
 					"`three_pointer`, `shot_type`) VALUES (?,?,?,?,?);");
 			stmt.setInt(1, shot.getX());
 			stmt.setInt(2, shot.getY());
 			stmt.setBoolean(3, !missed);
 			stmt.setBoolean(4, threePointShot);
 			stmt.setString(5, shot.getShotType().getDescription());
 			stmt.executeUpdate();
 		    
 		    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.currentShotID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`shot_player` (`shot_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, this.currentShotID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 		    
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`shot_possession` (`shot_id`," +
 					"`possession_id`, `time_of_shot`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentShotID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
		if (shot.getShotEnding().getAssist() != null)
 			shot.getShotEnding().getAssist().accept(this);
 	}
 
 	@Override
 	public void visit(Assist assist) 
 	{
 		int assistID = -1;
 		
 		if(this.currentShotID != -1)
 		{
 			try 
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`assist` VALUES (DEFAULT);");
 				stmt.executeUpdate();
 				
 				rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 			    if (rs.next()) 
 			    {
 			    	assistID = rs.getInt(1);
 			    }
 			    else 
 			    {
 			    	//TODO throw an exception from here
 			    }
 				
 			    stmt = conn.prepareStatement("INSERT INTO `nba2`.`assist_player` (`assist_id`,`player_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, assistID);
 				stmt.setInt(2, assist.getPlayer().getPlayerID());
 				stmt.executeUpdate();
 				
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`assist_shot` (`shot_id`,`assist_id`)" +
 						"VALUES (?,?);");
 				stmt.setInt(1, this.currentShotID);
 				stmt.setInt(2, assistID);
 				stmt.executeUpdate();
 			} 
 			catch (SQLException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			System.out.println("Unable to find shot associated with assist");
 			//TODO error on shot not being found
 		}
 	}
 
 	@Override
 	public void visit(Steal steal) 
 	{
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`steal` VALUES (DEFAULT) ;");
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	this.currentStealID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`steal_player` (`steal_id`,`player_id`)" +
 		    		"VALUES (?,?);");
 		    stmt.setInt(1, this.currentStealID);
 		    stmt.setInt(2, this.currentPlayerID);
 		    stmt.executeUpdate();
 		    
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`steal_possession` (`steal_id`,`possession_id`," +
 					"`time_of_steal`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentStealID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 			
 			if(this.currentTurnoverID != -1)
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`steal_player` (`steal_id`,`turnover_id`)" +
 		    			"VALUES (?,?);");
 		    	stmt.setInt(1, this.currentStealID);
 		    	stmt.setInt(2, this.currentTurnoverID);
 		    	stmt.executeUpdate();
 			}
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Substitution sub) 
 	{
 		int subID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`substitution` VALUES (DEFAULT);");
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	subID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`substitution_players` (`jump_ball_id`," +
 		    			"`player_in_id`, `player_out_id`) VALUES (?,?,?);");
 			stmt.setInt(1, subID);
 			stmt.setInt(2, sub.getIn().getPlayerID());
 			stmt.setInt(3, sub.getOut().getPlayerID());
 			stmt.executeUpdate();
 		   
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`substitution_possession` (`substitution_id`,`possession_id`," +
 					"`time_of_sub`) VALUES (?,?,?);");
 			stmt.setInt(1, subID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Technical technical) 
 	{
 		int technicalID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul` (technical_foul_type) VALUES (?);");
 			stmt.setString(1, technical.technicalType());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	technicalID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_player` (`technical_foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_possession` (`technical_foul_id`,`possession_id`," +
 					"`time_of_technical`) VALUES (?,?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(DoubleTechnical technical) 
 	{
 		int technicalID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul` (technical_foul_type) VALUES (?);");
 			stmt.setString(1, technical.technicalType());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	technicalID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_player` (`technical_foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, technical.getPlayer1().getPlayerID());
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_player` (`technical_foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, technical.getPlayer2().getPlayerID());
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_possession` (`technical_foul_id`,`possession_id`," +
 					"`time_of_technical`) VALUES (?,?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void visit(TauntingTechnical technical) 
 	{
 		int technicalID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul` (technical_foul_type) VALUES (?);");
 			stmt.setString(1, technical.technicalType());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	technicalID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_player` (`technical_foul_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`technical_foul_possession` (`technical_foul_id`,`possession_id`," +
 					"`time_of_technical`) VALUES (?,?,?);");
 			stmt.setInt(1, technicalID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Timeout timeout) 
 	{
 		int timeoutID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`timeout` (`timeout_type`) " +
 					" VALUES (?);");
 			stmt.setString(1, timeout.getTimeoutType());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	timeoutID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    if (currentContext.getPlayRole().equals(PlayRole.HOME))
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`timeout_team` (`timeout_id`," +
 		    			"`team_id`) VALUES (?,?);");
 		    	stmt.setInt(1, timeoutID);
 		    	stmt.setInt(2, this.homeID);
 		    	stmt.executeUpdate();
 		    }
 		    else if (currentContext.getPlayRole().equals(PlayRole.AWAY))
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`timeout_team` (`timeout_id`," +
 		    			"`team_id`) VALUES (?,?);");
 		    	stmt.setInt(1, timeoutID);
 		    	stmt.setInt(2, this.awayID);
 		    	stmt.executeUpdate();
 		    }
 		    
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`timeout_possession` (`timeout_id`,`possession_id`," +
 					"`time_of_timeout`) VALUES (?,?,?);");
 			stmt.setInt(1, timeoutID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Turnover turnover) 
 	{
 		if (turnover.getTurnoverType().toString().equals("No Turnover"))
 		{
 			return;
 		}
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`turnover` (turnover_type) VALUES (?);");
 			stmt.setString(1, turnover.getTurnoverType().toString());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	this.currentTurnoverID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    if(turnover.playerTurnover())
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`turnover_player` (`turnover_id`,`player_id`)" +
 		    			"VALUES (?,?);");
 		    	stmt.setInt(1, this.currentTurnoverID);
 		    	stmt.setInt(2, this.currentPlayerID);
 		    	stmt.executeUpdate();
 		    }
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`turnover_possession` (`turnover_id`,`possession_id`," +
 					"`time_of_turnover`) VALUES (?,?,?);");
 			stmt.setInt(1, this.currentTurnoverID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 			
 			if(this.currentStealID != -1)
 			{
 				stmt = conn.prepareStatement("INSERT INTO `nba2`.`steal_turnover` (`steal_id`,`turnover_id`)" +
 		    			"VALUES (?,?);");
 		    	stmt.setInt(1, this.currentStealID);
 		    	stmt.setInt(2, this.currentTurnoverID);
 		    	stmt.executeUpdate();
 			}
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Violation violation) 
 	{
 		int violationID = -1;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`violation` (violation_type) VALUES (?);");
 			stmt.setString(1, violation.getViolationType().toString());
 			stmt.executeUpdate();
 			
 			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		    	violationID = rs.getInt(1);
 		    }
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 			
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`violation_player` (`violation_id`,`player_id`)" +
 					"VALUES (?,?);");
 			stmt.setInt(1, violationID);
 			stmt.setInt(2, this.currentPlayerID);
 			stmt.executeUpdate();
 			
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`violation_possession` (`violation_id`,`possession_id`," +
 					"`time_of_violation`) VALUES (?,?,?);");
 			stmt.setInt(1, violationID);
 			stmt.setInt(2, this.currentPossessionID);
 			stmt.setInt(3, getConvertedPlayTime(currentContext.getPlayID()));
 			stmt.executeUpdate();
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void visit(Possession possession) 
 	{
 		ArrayList<Player> players = possession.getAwayPlayers();
 		players.addAll(possession.getHomePlayers());
 		this.currentPossession = possession;
 		
 		try 
 		{
 			stmt = conn.prepareStatement("INSERT INTO `nba2`.`possession` VALUES (DEFAULT);");
 			stmt.executeUpdate();
 		    
 		    rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 
 		    if (rs.next()) 
 		    {
 		        this.currentPossessionID = rs.getInt(1);
 		    } 
 		    else 
 		    {
 		    	//TODO throw an exception from here
 		    }
 		    
 		    stmt = conn.prepareStatement("INSERT INTO `nba2`.`period_possessions`" +
 		    " (`period_id`,`possession_id`) VALUES (?,?);");
 			stmt.setInt(1, this.currentPeriodID);
 			stmt.setInt(2, this.currentPossessionID);
 		    stmt.executeUpdate();
 		    
 		    for (Player player : players)
 		    {
 		    	stmt = conn.prepareStatement("INSERT INTO `nba2`.`possession_players`" +
 		    		    " (`possession_id`,`player_id`) VALUES (?,?);");
 		    			stmt.setInt(1, this.currentPossessionID);
 		    			stmt.setInt(2, player.getPlayerID());
 		    		    stmt.executeUpdate();
 		    }
 		} 
 		catch (SQLException e) 
 		{
 			e.printStackTrace();
 		}
 		
 		for(Play play : possession.getPossessionPlays())
 		{
 			this.missed = false;
 			currentPlayerID = -1;
 			play.accept(this);
 		}
 	}
 	
 	private int convertStringTime(String time)
 	{
 		String[] timeParts = time.split(":");
 		String min = timeParts[0];
 		String tens = timeParts[1].substring(0,1);
 		String singles = timeParts[1].substring(1, 2);
 		return ((Integer.parseInt(min) * 60) + (Integer.parseInt(tens) * 10) +
 				Integer.parseInt(singles)) * 10;
 	}
 	
 	private String getPlayTime(int playID)
 	{
 		PBPJson relevantPlay = new PBPJson();
 		relevantPlay.setEventNum(playID);
 		int index = Collections.binarySearch(this.pbp, relevantPlay, 
 				PBPJson.COMPARE_BY_PLAY_ID);
 		
 		if (index == -1)
 		{
 			System.out.println("Game: " + this.gameID + " " +
 					"Play: " + playID + " Play not found.");
 			System.exit(-1);
 		}
 		else
 		{
 			relevantPlay = this.pbp.get(index);
 		}
 		
 		return relevantPlay.getActualTime();
 	}
 	
 	private int getConvertedPlayTime(int playID)
 	{
 		return convertStringTime(getPlayTime(playID));
 	}
 
 }
