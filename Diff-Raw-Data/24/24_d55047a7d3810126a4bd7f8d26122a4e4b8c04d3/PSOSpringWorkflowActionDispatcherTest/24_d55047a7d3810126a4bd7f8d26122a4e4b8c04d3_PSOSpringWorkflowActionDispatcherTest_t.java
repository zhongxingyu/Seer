 /*
  * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
  * All rights reserved. This material contains unpublished, copyrighted
  * work including confidential and proprietary information of Percussion.
  *
  * test.percussion.pso.workflow PSOSpringWorkflowActionDispatcherTest.java
  *
  */
 package test.percussion.pso.workflow;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.percussion.extension.IPSWorkFlowContext;
 import com.percussion.extension.IPSWorkflowAction;
 import com.percussion.pso.workflow.IPSOWFActionService;
 import com.percussion.pso.workflow.PSOSpringWorkflowActionDispatcher;
 import com.percussion.server.IPSRequestContext;
 
 public class PSOSpringWorkflowActionDispatcherTest
 {
    Log log = LogFactory.getLog(PSOSpringWorkflowActionDispatcherTest.class);
    
    Mockery context;
    TestablePSOSpringWorkflowActionDispatcher cut; 
    IPSOWFActionService asvc; 
    
    @Before
    public void setUp() throws Exception
    {
       context = new Mockery();
       cut = new TestablePSOSpringWorkflowActionDispatcher();
       asvc = context.mock(IPSOWFActionService.class);
       cut.setAsvc(asvc);       
    }
    
    @Test
    public final void testPerformAction()
    {
       final IPSRequestContext request = context.mock(IPSRequestContext.class);
       final IPSWorkFlowContext wfContext = context.mock(IPSWorkFlowContext.class);
       final IPSWorkflowAction action = context.mock(IPSWorkflowAction.class);
       final List<IPSWorkflowAction> acts = new ArrayList<IPSWorkflowAction>();
       acts.add(action); 
       try
       {
          context.checking(new Expectations(){{
             one(wfContext).getWorkflowID();
             will(returnValue(1));
             one(wfContext).getTransitionID();
             will(returnValue(2));
             one(asvc).getActions(1, 2);
             will(returnValue(acts));
             one(action).performAction(wfContext, request); 
          }});
          
          cut.performAction(wfContext, request);
          
          context.assertIsSatisfied();
       } catch (Exception ex)
       {
          log.error("Unexpected Exception " + ex,ex);
          fail("Exception"); 
       } 
    }
    
    private class TestablePSOSpringWorkflowActionDispatcher extends PSOSpringWorkflowActionDispatcher
    {
 
       @Override
       public void setAsvc(IPSOWFActionService asvc)
       {
          super.setAsvc(asvc);
       }
       
    }
 }
