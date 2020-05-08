 package com.jwxicc.cricket.records;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import javax.ejb.EJB;
 import java.util.List;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.persistence.Query;
 
 import com.jwxicc.cricket.entity.Batting;
 import com.jwxicc.cricket.entity.Player;
 import com.jwxicc.cricket.interfaces.PlayerManager;
 import com.jwxicc.cricket.util.JwxiccUtils;
 
 @Stateless(name = "battingRecordsManager")
 @LocalBean
 public class BattingRecordsManager extends
 		RecordsManager<Batting, BattingRecord> {
 
 	@EJB
 	PlayerManager playerManager;
 
 	private static final String CAREER_BATTING_BASE_SQL = "select playerid, count(b.battingid) as matches, "
 			+ "count(if(b.howoutid not in (0, 16, 17),1,null)) as innings, "
 			+ "count(if(b.howoutid in (1, 7, 13),1,null)) as notouts, "
 			+ "max(score) as highest, sum(score) as total, sum(balls) as ballsfaced, "
 			+ "count(if(b.score >= 50,1,null)) as 50s, "
 			+ "count(if(b.score >= 100,1,null)) as 100s, "
 			+ "sum(score)/count(if(b.howoutid not in (0,1,7,13,16,17),1,null)) as avg, "
 			+ "sum(if(b.balls > 0,b.score,0))/sum(balls) as strikerate "
 			+ "from BATTING b natural join PLAYER p where teamid = :jwxi ";
 
 	/*
 	 * mysql> select b.battingpos, b.playerid, p.scorecardname, score from
 	 * (select battingpos, max(score) as highscore, playerid from batting b
 	 * natural join player p where teamid = 2 group by battingpos) as t inner
 	 * join batting b natural join player p on t.battingpos = b.battingpos and
 	 * t.highscore = score where teamid = 2 order by battingpos;
 	 */
 
 	@Override
 	public List<Batting> getInningsBest() {
 		// sql to get the lowest score in the top 10 scores for jwxi, to allow
 		// for duplicates
 		String sql = "select min(score) from (select b.score from BATTING b natural join PLAYER p where p.teamId = :jwxi order by b.score desc limit 0,:limit) as T";
 		Query sqlQuery = em.createNativeQuery(sql);
 		sqlQuery.setParameter("jwxi", JwxiccUtils.JWXICC_TEAM_ID);
 		sqlQuery.setParameter("limit", JwxiccUtils.RECORDS_TO_SHOW);
 		sqlQuery.setMaxResults(JwxiccUtils.RECORDS_TO_SHOW);
 		Integer score = (Integer) sqlQuery.getSingleResult();
 		System.out.println("min score for top 10: " + score);
 
 		// now get all scores for jwxi greater than or equal to this score
 		String queryString = "from Batting b left join fetch b.player p left join fetch b.inning.game g left join fetch g.ground where b.score >= :score and p.team.teamId = :jwxi order by b.score desc, g.date desc";
 		Query query = em.createQuery(queryString);
 		query.setParameter("jwxi", JwxiccUtils.JWXICC_TEAM_ID);
 		query.setParameter("score", score);
 		List<Batting> resultList = query.getResultList();
 
 		return resultList;
 	}
 
 	public List<Batting> getInningsMostBallsFaced() {
 		String sql = "";
 
 		return null;
 	}
 
 	@Override
 	public List<BattingRecord> getByAggregate() {
 		// Get the base_sql data ordered by most runs
 		String sql = CAREER_BATTING_BASE_SQL
 				+ "group by playerid order by total desc, avg desc, strikerate desc, ballsfaced asc";
 
 		return this.getBattingRecords(sql);
 	}
 
 	@Override
 	public List<BattingRecord> getByAverage() {
 		// Get the base sql data ordered by average
 		String sql = CAREER_BATTING_BASE_SQL
 				+ "group by playerid order by avg desc, total desc, strikerate desc, ballsfaced asc";
 
 		return this.getBattingRecords(sql);
 	}
 
 	private List<BattingRecord> getBattingRecords(String sqlQuery) {
 		Query query = em.createNativeQuery(sqlQuery);
 		query.setParameter("jwxi", JwxiccUtils.JWXICC_TEAM_ID);
 		query.setMaxResults(JwxiccUtils.RECORDS_TO_SHOW);
 		List<Object[]> results = query.getResultList();
 
 		List<BattingRecord> battingRecords = new ArrayList<BattingRecord>(10);
 
 		for (Object[] rs : results) {
 			battingRecords.add(this.getRecordFromResult(rs));
 		}
 
 		return battingRecords;
 	}
 
 	private BattingRecord getRecordFromResult(Object[] rs) {
 		BattingRecord battingRecord = new BattingRecord();
 		// 0: playerid
 		Player player = playerManager.findLazy(objToInt(rs[0]));
 		battingRecord.setPlayer(player);
 		// 1: matches
 		battingRecord.setMatchesPlayed(objToInt(rs[1]));
 		// 2: innings
 		battingRecord.setInnings(objToInt(rs[2]));
 		// 3: not outs
 		battingRecord.setNotOuts(objToInt(rs[3]));
 		// 4: highest
 		battingRecord.setHighestScore(objToInt(rs[4]));
 		// 5: total
 		battingRecord.setAggregate(objToInt(rs[5]));
 		// 6: balls faced
 		battingRecord.setBallsFaced(objToInt(rs[6]));
 		// 7: 50s
 		battingRecord.setFifties(objToInt(rs[7]));
 		// 8: 100s
 		battingRecord.setHundreds(objToInt(rs[8]));
 		// 9: average
		battingRecord.setAverage(BigDecimal.valueOf(Double.valueOf(rs[9]
				.toString())));
 		// 10: strike rate
		battingRecord.setStrikeRate(BigDecimal.valueOf(
				Double.valueOf(rs[10].toString())).scaleByPowerOfTen(2));
 
 		return battingRecord;
 	}
 
 	@Override
 	public BattingRecord getPlayerCareerRecord(int playerId) {
		String sqlQuery = CAREER_BATTING_BASE_SQL + "and p.playerId = :pid group by playerid";
		
 		Query query = em.createNativeQuery(sqlQuery);
 		query.setParameter("jwxi", JwxiccUtils.JWXICC_TEAM_ID);
 		query.setParameter("pid", playerId);
		
 		// only returns 1 object[]
 		List<Object[]> results = query.getResultList();
		
 		return getRecordFromResult(results.get(0));
 	}
 }
