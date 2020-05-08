 package org.seamoo.daos.twigImpl.matching;
 
 import java.util.List;
 
 import org.seamoo.daos.matching.MatchDao;
 import org.seamoo.daos.twigImpl.TwigGenericDaoImpl;
 import org.seamoo.entities.matching.Match;
import org.seamoo.entities.matching.MatchCompetitor;
 
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.SortDirection;
 import com.vercer.engine.persist.ObjectDatastore;
 import com.vercer.engine.persist.FindCommand.RootFindCommand;
 
 public class TwigMatchDaoImpl extends TwigGenericDaoImpl<Match, Long> implements MatchDao {
 
 	@Override
 	protected Match findByKeyWithoutCache(Long key) {
 		ObjectDatastore ods = getEagerOds();
 		Match m = ods.load(Match.class, key);
 		return m;
 	}
 
 	@Override
 	public List<Match> getRecentMatchesByLeague(Long leagueAutoId, long from, int count) {
 		RootFindCommand<Match> fc = getOds().find().type(Match.class).addFilter("leagueAutoId", FilterOperator.EQUAL,
 				leagueAutoId).addSort("endedMoment", SortDirection.DESCENDING).startFrom((int) from).fetchResultsBy(count);
 		return getSegmentedList(fc, count);
 	}
 
 	@Override
 	public long countByLeague(Long leagueAutoId) {
 		RootFindCommand<Match> fc = getOds().find().type(Match.class).addFilter("leagueAutoId", FilterOperator.EQUAL,
 				leagueAutoId);
 		return fc.countResultsNow();
 	}
 
 	@Override
 	public long countByMember(Long memberAutoId) {
 		RootFindCommand<Match> fc = getOds().find().type(Match.class).addFilter("memberAutoIds", FilterOperator.EQUAL,
 				memberAutoId);
 		return fc.countResultsNow();
 	}
 
 	@Override
 	public List<Match> getRecentMatchesBymember(Long memberAutoId, long from, int count) {
 		RootFindCommand<Match> fc = getOds().find().type(Match.class).addFilter("memberAutoIds", FilterOperator.EQUAL,
 				memberAutoId).addSort("endedMoment", SortDirection.DESCENDING).startFrom((int) from);
 		return getSegmentedList(fc, count);
 	}
 }
