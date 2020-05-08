 package org.kalibro.service;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.junit.Test;
 import org.kalibro.ProcessState;
 import org.kalibro.Processing;
 import org.kalibro.Repository;
 import org.kalibro.client.EndpointTest;
 import org.kalibro.dao.ProcessingDao;
 import org.powermock.reflect.Whitebox;
 
 public class ProcessingEndpointTest extends EndpointTest<Processing, ProcessingDao, ProcessingEndpoint> {
 
 	private static final Long ID = Math.abs(new Random().nextLong());
	private static final Date DATE = new Date();
 
 	@Override
 	protected Processing loadFixture() {
 		Processing processing = new Processing(new Repository());
 		Whitebox.setInternalState(processing, "id", ID);
		processing.setDate(DATE);
 		return processing;
 	}
 
 	@Override
 	protected List<String> fieldsThatShouldBeProxy() {
 		return asList("repository", "resultsRoot");
 	}
 
 	@Test
 	public void shouldAnswerIfHasProcessing() {
 		when(dao.hasProcessing(ID)).thenReturn(true);
 		assertFalse(port.hasProcessing(-1L));
 		assertTrue(port.hasProcessing(ID));
 	}
 
 	@Test
 	public void shouldAnswerIfHasReadyProcessing() {
 		when(dao.hasReadyProcessing(ID)).thenReturn(true);
 		assertFalse(port.hasReadyProcessing(-1L));
 		assertTrue(port.hasReadyProcessing(ID));
 	}
 
 	@Test
 	public void shouldAnswerIfHasProcessingAfterDate() {
 		when(dao.hasProcessingAfter(DATE, ID)).thenReturn(true);
 		assertFalse(port.hasProcessingAfter(DATE, -1L));
 		assertTrue(port.hasProcessingAfter(DATE, ID));
 	}
 
 	@Test
 	public void shouldAnswerIfHasProcessingBeforeDate() {
 		when(dao.hasProcessingBefore(DATE, ID)).thenReturn(true);
 		assertFalse(port.hasProcessingBefore(DATE, -1L));
 		assertTrue(port.hasProcessingBefore(DATE, ID));
 	}
 
 	@Test
 	public void shouldGetLastProcessingState() {
 		ProcessState[] states = ProcessState.values();
 		ProcessState state = states[new Random().nextInt(states.length)];
 		when(dao.lastProcessingState(ID)).thenReturn(state);
 		assertSame(state, port.lastProcessingState(ID));
 	}
 
 	@Test
 	public void shouldGetLastReadyProcessing() {
 		when(dao.lastReadyProcessing(ID)).thenReturn(entity);
 		assertDeepDtoEquals(entity, port.lastReadyProcessing(ID));
 	}
 
 	@Test
 	public void shouldGetFirstProcessing() {
 		when(dao.firstProcessing(ID)).thenReturn(entity);
 		assertDeepDtoEquals(entity, port.firstProcessing(ID));
 	}
 
 	@Test
 	public void shouldGetLastProcessing() {
 		when(dao.lastProcessing(ID)).thenReturn(entity);
 		assertDeepDtoEquals(entity, port.lastProcessing(ID));
 	}
 
 	@Test
 	public void shouldGetFirstProcessingAfterDate() {
 		when(dao.firstProcessingAfter(DATE, ID)).thenReturn(entity);
 		assertDeepDtoEquals(entity, port.firstProcessingAfter(DATE, ID));
 	}
 
 	@Test
 	public void shouldGetLastProcessingBeforeDate() {
 		when(dao.lastProcessingBefore(DATE, ID)).thenReturn(entity);
 		assertDeepDtoEquals(entity, port.lastProcessingBefore(DATE, ID));
 	}
 }
