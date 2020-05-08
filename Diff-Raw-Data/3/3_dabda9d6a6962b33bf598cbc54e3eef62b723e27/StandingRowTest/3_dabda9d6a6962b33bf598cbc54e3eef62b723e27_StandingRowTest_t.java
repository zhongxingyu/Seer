 package dbs.project.service.group;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import javax.swing.table.TableModel;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
import dbs.project.dao.TournamentGroupDao;
 import dbs.project.entity.GroupMatch;
 import dbs.project.entity.Team;
 import dbs.project.entity.TournamentGroup;
 import dbs.project.helper.TestHelper;
 
 public class StandingRowTest {
 	List<Team> teams;
 	List<GroupMatch> matches;
 	TournamentGroup group;
 
 	@Before
 	public void setUp() throws Exception {
 		group = TestHelper.playedGroupStage().getGroups().get(0);
 		teams = group.getTeams();
 		matches = group.getMatches();
		TournamentGroupDao.save(group);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		group = null;
 		teams = null;
 		matches = null;
 	}
 
 	@Test
 	public void testGetRows() {
 		List<StandingRow> standings = StandingRow.getRows(this.teams,
 				this.matches);
 		assertEquals(4, standings.size());
 	}
 
 	@Test
 	public void testGetModel() {
 		TableModel model = StandingRow.getModel(group);
 		assertEquals(4, model.getRowCount());
 	}
 
 }
