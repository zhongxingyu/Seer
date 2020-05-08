 
 package com.zygon.trade.agent;
 
 import com.zygon.trade.market.data.Interpreter;
 import com.zygon.trade.market.model.indication.Identifier;
 import java.util.Collection;
 
 /**
  *
  * @author zygon
  */
 public class AgentBuilder<T> {
 
     // These are Agent specific
     private String name;
     private Collection<Interpreter<T>> interpreters;
     
     // These are Strategy specific
     private Collection<Identifier> supportedIndicators;
     private SignalGenerator signalGenerator;
     private SignalHandler signalHandler;
     
    public Agent build() {
         Strategy strategy = new Strategy(this.name+"_strategy", this.supportedIndicators, this.signalGenerator, this.signalHandler);
        return new Agent(this.name, this.interpreters, strategy);
     }
 
     public void setInterpreters(Collection<Interpreter<T>> interpreters) {
         this.interpreters = interpreters;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setSignalGenerator(SignalGenerator signalGenerator) {
         this.signalGenerator = signalGenerator;
     }
 
     public void setSignalHandler(SignalHandler signalHandler) {
         this.signalHandler = signalHandler;
     }
 
     public void setSupportedIndicators(Collection<Identifier> supportedIndicators) {
         this.supportedIndicators = supportedIndicators;
     }
 }
