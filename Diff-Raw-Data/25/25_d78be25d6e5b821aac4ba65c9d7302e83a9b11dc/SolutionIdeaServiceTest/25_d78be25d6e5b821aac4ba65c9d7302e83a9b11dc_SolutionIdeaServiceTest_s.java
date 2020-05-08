 package com.tdm.server.logic.service.problem;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.anyLong;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.Collection;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 

 import com.tdm.common.dto.GdmProblemId;
 import com.tdm.common.dto.SolutionIdea;
 import com.tdm.server.datastore.dao.ProblemDao;
 import com.tdm.server.datastore.dao.SolutionIdeaDao;
 import com.tdm.server.logic.problem.service.DefaultSolutionIdeaService;
 import com.tdm.server.logic.service.SolutionIdeaService;
 
 public class SolutionIdeaServiceTest {
 
 	SolutionIdeaService ideasService;
 	private ProblemDao problemDaoMock = mock(ProblemDao.class);
 	private SolutionIdeaDao solutionIdeaDao = mock(SolutionIdeaDao.class);
 
 	@Before
 	public void setUp() {
 		ideasService = new DefaultSolutionIdeaService(problemDaoMock, solutionIdeaDao);
 		when(solutionIdeaDao.create(anyLong(), anyString())).thenReturn(
				new SolutionIdea(1, 1, "New idea"));
 	}
 
 	@Test
 	public void shouldAddNewIdeaToProblem() {
 		// Given
 		GdmProblemId problemId = GdmProblemId.create(1);
 		String ideaName = "New idea";
 
 		// When
 		SolutionIdea solutionIdea = ideasService.createAndAddSolutionIdea(
 				problemId, ideaName);
 
 		// Then
 		Assert.assertEquals(problemId, solutionIdea.getProblemId());
 		Assert.assertEquals(ideaName, solutionIdea.getName());
 		Assert.assertNotNull(solutionIdea.getId());
 	}
 
 	@Test
 	public void shouldRetrieveAllIdeasForProblem() {
 		GdmProblemId problemId = GdmProblemId.create(0);
 		Collection<SolutionIdea> ideas = ideasService
 				.retrieveSolutionIdeasForProblem(problemId);
 
 		assertEquals(0, ideas.size());
 	}
 
 }
