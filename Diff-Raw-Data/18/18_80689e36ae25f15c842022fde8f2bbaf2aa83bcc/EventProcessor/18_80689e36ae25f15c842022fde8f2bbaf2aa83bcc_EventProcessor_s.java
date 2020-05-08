 package eu.scape_project.simulator.engine.processor;
 
 import java.util.List;
 
 import com.google.inject.Inject;
 
 import eu.scape_project.pw.simulator.engine.container.IEventContainer;
 import eu.scape_project.pw.simulator.engine.container.IEventContainerFactory;
 import eu.scape_project.pw.simulator.engine.container.IEventObserverContainer;
 import eu.scape_project.pw.simulator.engine.container.IEventObserverContainerFactory;
 import eu.scape_project.pw.simulator.engine.model.IEvent;
 import eu.scape_project.pw.simulator.engine.model.IEventObserver;
 import eu.scape_project.pw.simulator.engine.model.ISimulationProperties;
 import eu.scape_project.pw.simulator.engine.model.state.ISimulationState;
 import eu.scape_project.pw.simulator.engine.model.state.ISimulationStateFactory;
 import eu.scape_project.pw.simulator.engine.recorder.IRecorder;
 
 public class EventProcessor implements IEventProcessor{
 
 	private final IEventContainerFactory eventContainerFactory;
 
 	private final IEventObserverContainerFactory eOFactory;
 
 	private final IRecorder recorder;
 
 	private final ISimulationProperties properties;
 
 	private final ISimulationStateFactory simulationStateFactory;
 
 	@Inject
 	public EventProcessor(IEventContainerFactory eventContainerFactory,
 			IEventObserverContainerFactory eOFactory, IRecorder recorder,
 			ISimulationProperties properties,
 			ISimulationStateFactory simulationStateFactory) {
 		this.eventContainerFactory = eventContainerFactory;
 		this.eOFactory = eOFactory;
 		this.recorder = recorder;
 		this.properties = properties;
 		this.simulationStateFactory = simulationStateFactory;
 	}
 
 	@Override
 	public void startSimulation() {
 		recorder.startSimulation(properties);
 		for (int simulationRun = 0; simulationRun < properties
 				.getNumberOfRuns(); simulationRun++) {
			System.out.println("Runnning " + simulationRun + " run");
 			IEventObserverContainer eOContainer = eOFactory
 					.getEventObserverContainer();
 			IEventContainer eventContainer = eventContainerFactory
 					.getEventContainer();
 			ISimulationState state = simulationStateFactory
 					.getSimulationState();
 			recorder.startRun(state, simulationRun);
 			while (!eventContainer.isEmpty()) {
 				IEvent event = eventContainer.getNextEvent();
 				if (state.getTime() != event.getScheduleTime()) {
 					recorder.record(state);
 				}
 				state.setTime(event.getScheduleTime());
 				event.execute(state);
 				List<IEventObserver> tmp = eOContainer.get(event.getName());
 				if (tmp != null) {
 					for (IEventObserver observer : tmp) {
 						eventContainer.addEvent(observer.schedules(state));
 					}
 				}
 			}
 			recorder.stopRun(state, simulationRun);
 		}
 		recorder.stopSimulation(properties);
 	}
	/*
	 * public IEventContainerFactory getEventContainer() { return
	 * eventContainerFactory; }
	 * 
	 * public IEventObserverContainer getEOContainer() { return eOContainer; }
	 * 
	 * public ISimulationState getSimulationState() { return state; }
	 */
 
 }
