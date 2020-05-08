 package com.marcindziedzic.bdd;
 
 import org.jbehave.core.steps.CandidateSteps;
 import org.jbehave.core.steps.InstanceStepsFactory;
 import org.jbehave.web.runner.wicket.WebRunnerApplication;
 
 import java.util.List;
 
 public class JBehaveWebRunnerApplication extends WebRunnerApplication {
 
     @Override
     protected List<CandidateSteps> candidateSteps() {
        return new InstanceStepsFactory(configuration(), new TradeIsNotAlertedBelowThreshold()).createCandidateSteps();
     }
 
 }
