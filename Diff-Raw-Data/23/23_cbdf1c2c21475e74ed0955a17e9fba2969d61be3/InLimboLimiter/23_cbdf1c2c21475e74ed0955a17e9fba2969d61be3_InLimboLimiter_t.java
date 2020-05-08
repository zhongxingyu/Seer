 package uk.co.acuminous.julez.scenario.limiter;
 
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import uk.co.acuminous.julez.event.Event;
 import uk.co.acuminous.julez.event.handler.EventHandler;
 import uk.co.acuminous.julez.scenario.Scenario;
 import uk.co.acuminous.julez.scenario.ScenarioEvent;
 import uk.co.acuminous.julez.scenario.ScenarioSource;
 import uk.co.acuminous.julez.util.ConcurrencyUtils;
 import uk.co.acuminous.julez.util.JulezSugar;
 
 public class InLimboLimiter implements ScenarioSource, EventHandler {
     
     private ScenarioSource scenarios;    
    private Integer upperLimit = Integer.MAX_VALUE;
    private Integer lowerLimit;
     
     private AtomicInteger counter = new AtomicInteger();
     private long pause = 100;
 
     public InLimboLimiter() {        
     }
     
     public InLimboLimiter(ScenarioSource scenarios, int limit) {
         this(scenarios, limit, limit);
     }
     
     public InLimboLimiter(ScenarioSource scenarios, int upperLimit, int lowerLimit) {
         this.scenarios = scenarios;
         this.upperLimit = upperLimit;
         this.lowerLimit = lowerLimit;
     }            
     
     public InLimboLimiter to(ScenarioSource scenarios) {
         this.scenarios = scenarios;
         return this;
     }
     
     public InLimboLimiter applyLimitOf(int limit, JulezSugar units) {
         this.upperLimit = limit;
        this.lowerLimit = lowerLimit == null ? limit : lowerLimit;
         return this;
     }    
     
     public InLimboLimiter liftLimitAt(int limit, JulezSugar units) {
         this.lowerLimit = limit;
         return this;
     }    
 
     @Override
     public Scenario next() {
         if (counter.get() > upperLimit) {
             while (counter.get() > lowerLimit) {
                 ConcurrencyUtils.sleep(pause, MILLISECONDS);
             }
         }
         counter.incrementAndGet();
         return scenarios.next();
     }
 
     @Override
     public void onEvent(Event event) {
         if (ScenarioEvent.END.equals(event.getType())) {
             counter.decrementAndGet();
         }        
     }
         
     public void setPause(long value, TimeUnit units) {
         pause = MILLISECONDS.convert(value, units);
     }    
 
 }
