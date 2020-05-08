 package sk.openhouse.automation.pipelineui.service.impl;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.UniformInterfaceException;
 
 import sk.openhouse.automation.pipelineclient.BuildClient;
 import sk.openhouse.automation.pipelineclient.PhaseClient;
 import sk.openhouse.automation.pipelineclient.ProjectClient;
 import sk.openhouse.automation.pipelineclient.VersionClient;
 import sk.openhouse.automation.pipelinedomain.domain.PhaseState;
 import sk.openhouse.automation.pipelinedomain.domain.response.BuildPhaseResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.BuildPhasesResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.BuildResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.BuildsResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.PhaseResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.PhasesResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.ProjectResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.ProjectsResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.StateResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.StatesResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.VersionResponse;
 import sk.openhouse.automation.pipelinedomain.domain.response.VersionsResponse;
 import sk.openhouse.automation.pipelineui.model.Build;
 import sk.openhouse.automation.pipelineui.service.PipelineException;
 import sk.openhouse.automation.pipelineui.service.impl.PipelineServiceImpl;
 
 public class PipelineServiceImplTest {
 
     @Mock
     private ProjectClient projectClient;
 
     @Mock
     private VersionClient versionClient;
 
     @Mock
     private BuildClient buildClient;
 
     @Mock
     private PhaseClient phaseClient;
 
     private PipelineServiceImpl pipelineServiceImpl;
 
     @BeforeMethod
     public void beforeMethod() {
 
         MockitoAnnotations.initMocks(this);
         pipelineServiceImpl = new PipelineServiceImpl(projectClient, versionClient, buildClient, phaseClient);
     }
 
     @Test
     public void testGetProjectNames() {
 
         List<ProjectResponse> projects = new ArrayList<ProjectResponse>();
         ProjectResponse project = new ProjectResponse();
         project.setName("test project");
         projects.add(project);
 
         ProjectsResponse projectsResponse = new ProjectsResponse();
         projectsResponse.setProjects(projects);
 
         Mockito.when(projectClient.getProjects()).thenReturn(projectsResponse);
 
         List<String> projectNames = pipelineServiceImpl.getProjectNames();
         Assert.assertEquals(projectNames.size(), 1);
         Assert.assertEquals(projectNames.get(0), "test project");
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetProjectNamesUniformInterfaceException() {
 
         Mockito.when(projectClient.getProjects()).thenThrow(UniformInterfaceException.class);
         pipelineServiceImpl.getProjectNames();
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetProjectNamesClientHandlerException() {
 
         Mockito.when(projectClient.getProjects()).thenThrow(ClientHandlerException.class);
         pipelineServiceImpl.getProjectNames();
     }
 
     @Test
     public void testGetVersionNumbers() {
 
         List<VersionResponse> versions = new ArrayList<VersionResponse>();
         VersionResponse version = new VersionResponse();
         version.setVersionNumber("0.7");
         versions.add(version);
 
         VersionsResponse versionsResponse = new VersionsResponse();
         versionsResponse.setVersions(versions);
 
         Mockito.when(versionClient.getVersions("test")).thenReturn(versionsResponse);
 
         List<String> versionNumbers = pipelineServiceImpl.getVersionNumbers("test");
         Assert.assertEquals(versionNumbers.size(), 1);
         Assert.assertEquals(versionNumbers.get(0), "0.7");
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetVersionNumbersUniformInterfaceException() {
 
         Mockito.when(versionClient.getVersions(Mockito.anyString())).thenThrow(UniformInterfaceException.class);
         pipelineServiceImpl.getVersionNumbers("test");
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetVersionNumbersClientHandlerException() {
 
         Mockito.when(versionClient.getVersions(Mockito.anyString())).thenThrow(ClientHandlerException.class);
         pipelineServiceImpl.getVersionNumbers("test");
     }
 
     @Test
     public void testGetPhaseNames() {
 
         List<PhaseResponse> phases = new ArrayList<PhaseResponse>();
         PhaseResponse phase = new PhaseResponse();
         phase.setName("QA");
         phases.add(phase);
 
         PhasesResponse phasesResponse = new PhasesResponse();
         phasesResponse.setPhases(phases);
 
         Mockito.when(phaseClient.getPhases("test", "0.3")).thenReturn(phasesResponse);
 
         List<String> phaseNames = pipelineServiceImpl.getPhaseNames("test", "0.3");
         Assert.assertEquals(phaseNames.size(), 1);
         Assert.assertEquals(phaseNames.get(0), "QA");
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetPhaseNamesUniformInterfaceException() {
 
         Mockito.when(phaseClient.getPhases(Mockito.anyString(), Mockito.anyString()))
                 .thenThrow(UniformInterfaceException.class);
         pipelineServiceImpl.getPhaseNames("test", "0.1");
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetPhaseNamesClientHandlerException() {
 
         Mockito.when(phaseClient.getPhases(Mockito.anyString(), Mockito.anyString()))
                 .thenThrow(ClientHandlerException.class);
         pipelineServiceImpl.getPhaseNames("test", "0.1");
     }
 
     @Test
     public void testGetBuildsNoPhases() {
 
         List<BuildResponse> builds = new ArrayList<BuildResponse>();
         BuildResponse build = new BuildResponse();
         build.setNumber(7);
         builds.add(build);
 
         BuildsResponse buildsResponse = new BuildsResponse();
         buildsResponse.setBuilds(builds);
 
        Mockito.when(buildClient.getBuilds("test", "0.3", 10)).thenReturn(buildsResponse);
 
         List<Build> buildModels = pipelineServiceImpl.getBuilds("test", "0.3", 10);
         Assert.assertEquals(buildModels.size(), 1);
         Assert.assertEquals(buildModels.get(0).getNumber(), 7);
         Assert.assertEquals(buildModels.get(0).getStates().size(), 0);
     }
 
     @Test
     public void testGetBuilds() {
 
         List<StateResponse> stateResponses = new ArrayList<StateResponse>();
         StateResponse state1 = new StateResponse();
         state1.setDate(new Date());
         state1.setName(PhaseState.FAIL);
         stateResponses.add(state1);
 
         StateResponse state2 = new StateResponse();
         state2.setDate(new Date());
         state2.setName(PhaseState.SUCCESS);
         stateResponses.add(state2);
 
         StatesResponse states = new StatesResponse();
         states.setStates(stateResponses);
 
         List<BuildPhaseResponse> phases = new ArrayList<BuildPhaseResponse>();
         BuildPhaseResponse phase = new BuildPhaseResponse();
         phase.setName("QA");
         phase.setStates(states);
         phases.add(phase);
 
         BuildPhasesResponse buildPhases = new BuildPhasesResponse();
         buildPhases.setBuildPhases(phases);
 
         List<BuildResponse> builds = new ArrayList<BuildResponse>();
         BuildResponse build = new BuildResponse();
         build.setNumber(7);
         build.setBuildPhases(buildPhases);
         builds.add(build);
 
         BuildsResponse buildsResponse = new BuildsResponse();
         buildsResponse.setBuilds(builds);
 
        Mockito.when(buildClient.getBuilds("test", "0.3", 10)).thenReturn(buildsResponse);
 
         List<Build> buildModels = pipelineServiceImpl.getBuilds("test", "0.3", 10);
         Assert.assertEquals(buildModels.size(), 1);
         Assert.assertEquals(buildModels.get(0).getNumber(), 7);
         Assert.assertEquals(buildModels.get(0).getStates().size(), 1);
         Assert.assertEquals(buildModels.get(0).getStates().get("QA"), PhaseState.SUCCESS);
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetBuildsUniformInterfaceException() {
 
        Mockito.when(buildClient.getBuilds(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                 .thenThrow(UniformInterfaceException.class);
         pipelineServiceImpl.getBuilds("test", "0.1", 10);
     }
 
     @SuppressWarnings("unchecked")
     @Test(expectedExceptions = PipelineException.class)
     public void testGetBuildsClientHandlerException() {
 
        Mockito.when(buildClient.getBuilds(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt()))
                 .thenThrow(ClientHandlerException.class);
         pipelineServiceImpl.getBuilds("test", "0.1", 10);
     }
 }
