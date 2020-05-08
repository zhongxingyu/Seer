 package hu.meza;
 
 import hu.meza.exceptions.CircuitBrokenException;
 
 public class CircuitBreaker {
 
 	private CoolDownStrategy coolDownStrategy;
 	private TriggerStrategy triggerStrategy;
 
 	public CircuitBreaker(CoolDownStrategy secondsToCoolDown, TriggerStrategy triggerStrategy) {
 		this.coolDownStrategy = secondsToCoolDown;
 		this.triggerStrategy = triggerStrategy;
 	}
 
 	public Response execute(Command cmd) {
 		return handleCommand(cmd);
 	}
 
 	private Response handleCommand(Command command) {
 		if (coolDownStrategy.isCool() == false) {
 			return new Response(null, false, new CircuitBrokenException());
 		}
 
 		try {
 			return new Response(command.execute(), true);
 		} catch (Throwable e) {
 			handleException(e);
 			return new Response(null, false, e);
 		}
 	}
 
 	private void handleException(Throwable e) {
		if(coolDownStrategy.isCool() == false) {
 			if (triggerStrategy.isBreaker(e)) {
 				coolDownStrategy.makeHot();
 			}
 		}
 	}
 }
