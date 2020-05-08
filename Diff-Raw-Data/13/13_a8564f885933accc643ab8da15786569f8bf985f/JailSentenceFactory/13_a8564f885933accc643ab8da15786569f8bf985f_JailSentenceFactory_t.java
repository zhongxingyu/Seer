 package no.runsafe.runsafejail.handlers;
 
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.framework.timer.TimerFactory;
 import no.runsafe.runsafejail.objects.JailedPlayer;
 
 import java.util.logging.Level;
 
 public class JailSentenceFactory extends TimerFactory<JailedPlayer>
 {
	public JailSentenceFactory(IScheduler scheduler, IOutput console)
 	{
 		super(scheduler);
 		this.console = console;
	}

	public void setJailHandler(JailHandler jailHandler)
	{
 		this.jailHandler = jailHandler;
 	}
 
 	@Override
 	public void OnTimerElapsed(JailedPlayer state)
 	{
 		state.returnFromJail();
 		this.console.outputDebugToConsole(
 				"Jail timer for %s has been killed, triggering sentence removal.",
 				Level.FINE,
 				state.getName()
 		);
 		this.jailHandler.removeJailSentence(state.getName());
 	}
 
 	private IOutput console;
 	private JailHandler jailHandler;
 }
