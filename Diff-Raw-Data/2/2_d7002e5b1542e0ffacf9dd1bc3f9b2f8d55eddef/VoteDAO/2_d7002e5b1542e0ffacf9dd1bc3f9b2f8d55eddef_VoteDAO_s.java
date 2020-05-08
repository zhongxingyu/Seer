 package org.freecode.irc.votebot.dao;
 
 import org.freecode.irc.votebot.entity.Vote;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deprecated
  * Date: 11/21/13
  * Time: 7:32 PM
  */
 public class VoteDAO extends AbstractDAO {
     private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS votes (pollId integer, voter string NOT NULL, answerIndex integer NOT NULL)";
     private static final String GET_PREVIOUS_VOTE_OF_USER_ON_POLL = "SELECT * FROM votes WHERE voter = ? AND pollId = ? LIMIT 1";
     private static final String UPDATE_VOTE_OF_USER_ON_POLL = "UPDATE votes SET answerIndex = ? WHERE voter = ? AND pollId = ?";
     private static final String ADD_NEW_VOTE = "INSERT INTO votes(pollId,voter,answerIndex) VALUES (?,?,?)";
    private static final String GET_VOTES_ON_POLL = "SELECT answerIndex FROM votes WHERE pollId = ?";
 
     private ResultSet resultSet;
     private PreparedStatement statement;
 
     public void createTable() throws SQLException {
         statement = dbConn.prepareStatement(CREATE_TABLE);
         statement.setQueryTimeout(5);
         statement.execute();
     }
 
     public Vote getUsersVoteOnPoll(final String voter, final int pollId) throws SQLException {
         statement = dbConn.prepareStatement(GET_PREVIOUS_VOTE_OF_USER_ON_POLL);
         statement.setString(1, voter);
         statement.setInt(2, pollId);
         resultSet = statement.executeQuery();
 
         if (resultSet.next()) {
             return new Vote(resultSet);
         }
         return null;
     }
 
     public void updateUsersVote(final Vote vote) throws SQLException {
         statement = dbConn.prepareStatement(UPDATE_VOTE_OF_USER_ON_POLL);
         statement.setInt(1, vote.getAnswerIndex());
         statement.setString(2, vote.getVoter());
         statement.setInt(3, vote.getPollId());
         statement.executeUpdate();
     }
 
     public void addUsersVote(final String voter, final int pollId, final int answerIndex) throws SQLException {
         statement = dbConn.prepareStatement(ADD_NEW_VOTE);
         statement.setInt(1, answerIndex);
         statement.setString(2, voter);
         statement.setInt(3, pollId);
         statement.execute();
     }
 
     public Vote[] getVotesOnPoll(final int pollId) throws SQLException {
         List<Vote> votes = new ArrayList<>();
         statement = dbConn.prepareStatement(GET_VOTES_ON_POLL);
         statement.setInt(1, pollId);
         resultSet = statement.executeQuery();
 
         while (resultSet.next()) {
             votes.add(new Vote(resultSet));
         }
 
         return votes.toArray(new Vote[votes.size()]);
     }
 }
