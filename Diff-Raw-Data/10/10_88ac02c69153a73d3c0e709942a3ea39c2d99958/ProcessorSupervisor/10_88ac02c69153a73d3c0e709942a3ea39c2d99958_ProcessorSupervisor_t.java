 package infoprivacy.simulator;
 
 import infoprivacy.simulator.processors.DummyGrapher;
 import infoprivacy.simulator.processors.DummyProcessor;
 import infoprivacy.simulator.processors.Over80MPHProcessor;
 import infoprivacy.simulator.processors.ReportedPointCounter;
 import infoprivacy.simulator.processors.TotalMileage;
 import infoprivacy.simulator.processors.TripTime;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Supervises a collection of data processors that make up the analytics
  * portion of the software.
  * 
  * @author Joseph Lewis <joehms22@gmail.com>
  *
  */
 public class ProcessorSupervisor 
 {
 	private static ProcessorSupervisor INSTANCE; // singleton design pattern
 	
 	private final LinkedList<Processor> m_processors = new LinkedList<>();
 	private final HashSet<Processor> m_processorsEnabled = new HashSet<>(); // the processors that are enabled
 	
 	/**
 	 * Creates or returns the single ProcessorSupervisor for this program.
 	 * 
 	 * @return - an instance of ProcessorSupervisor.
 	 */
 	public static ProcessorSupervisor getInstance()
 	{
 		if(INSTANCE == null)
 		{
 			INSTANCE = new ProcessorSupervisor();
 		}
 		
 		return INSTANCE;
 	}
 	
 	private ProcessorSupervisor()
 	{
 		populateProcessors();
 		m_processorsEnabled.addAll(m_processors);
 	}
 
 	
 	/**
 	 * Adds all of the known processors to the supervisor.
 	 */
 	private void populateProcessors() 
 	{
 		
 		// TODO Jake, add your processors here!
 		m_processors.add(new DummyProcessor());
 		m_processors.add(new DummyGrapher());
 		m_processors.add(new Over80MPHProcessor());
 		m_processors.add(new TotalMileage());
 		m_processors.add(new TripTime());
 		
 		// This is the last one
 		m_processors.addLast(new ReportedPointCounter());
 	}
 	
 	/**
 	 * Gets a list of all known processors.
 	 * @return a new list of processors.
 	 */
 	public List<Processor> getProcessors()
 	{
 		return new LinkedList<>(m_processors);
 	}
 	
 	/**
 	 * Checks to see if a processor is going to be run when new data
 	 * is received.
 	 * @param proc - the processor to check
 	 * @return true if the processor is to be run, false if it is not.
 	 */
 	public boolean isEnabled(Processor proc)
 	{
 		return m_processorsEnabled.contains(proc);
 	}
 	
 	/**
 	 * Enables or disables a processor from being run with new data.
 	 * @param proc - the processor to enable/disable.
 	 * @param enable - true to enable the processor false to disable it.
 	 */
 	public void setEnabled(Processor proc, boolean enable)
 	{
 		if(enable)
 		{
 			m_processorsEnabled.add(proc);
 		}
 		else
 		{
 			m_processorsEnabled.remove(proc);
 		}
 	}
 	
 	/**
 	 * Processes a point of data in the stream by handing it off to each 
 	 * processor. 
 	 * 
 	 * @param time - the time the data occurs at.
 	 * @param speedMPH - the speed the vehicle was going at the time.
 	 */
 	public void process(Date time, double speedMPH)
 	{
		for(Processor proc : m_processors)
 		{
			// we do this because m_processors needs to be sorted, and a HashMap
			// doesn't mean this property holds.
			if(m_processorsEnabled.contains(proc))
			{
				proc.process(time, speedMPH);
			}
 		}
 	}
 }
