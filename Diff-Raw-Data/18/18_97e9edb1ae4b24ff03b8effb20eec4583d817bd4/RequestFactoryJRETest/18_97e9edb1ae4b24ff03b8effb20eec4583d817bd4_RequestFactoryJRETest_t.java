 package com.clouway.jobex.client.applyingforjob;
 
 
 import com.clouway.jobex.client.security.UsernameProvider;
 import com.clouway.jobex.server.applyingforjob.JobApplication;
 import com.clouway.jobex.server.applyingforjob.JobApplicationService;
 import com.clouway.jobex.shared.JobApplicationProxy;
 import com.clouway.jobex.shared.JobExRequestFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.isA;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 /**
  * @author Adelin Ghanayem adelin.ghanaem@clouway.com
  */
 public class RequestFactoryJRETest {
 
   private JobExRequestFactory factory;
 
   private JobApplicationService service;
 
   private JobApplicationPresenter presenter;
 
   private JobExRequestFactory.JobApplicationRequestContext context;
 
   @Mock
   JobApplicationView view;
 
   @Mock
   UsernameProvider provider;
 
   @Before
   public void setUp() throws Exception {
     initMocks(this);
     factory = RequestFactoryHelper.create(JobExRequestFactory.class);
     service = RequestFactoryHelper.getService(JobApplicationService.class);
     context = factory.jobApplicationContext();
    presenter = new JobApplicationPresenter(factory, view, provider);
   }
 
   @Test
   public void appliesForAJob() {
 
     JobApplicationProxy proxy = context.create(JobApplicationProxy.class);
 
     proxy.setJobId(1l);
 
     proxy.setCvId(2l);
 
     doThrow(new RuntimeException()).when(service).applyForJob(isA(JobApplication.class));
 
    presenter.applyForJob(1l, 2l);
 
     ArgumentCaptor<JobApplication> jobApplicationArgumentCaptor = ArgumentCaptor.forClass(JobApplication.class);
 
     verify(service).applyForJob(jobApplicationArgumentCaptor.capture());
 
     JobApplication jobApplication = jobApplicationArgumentCaptor.getValue();
 
     assertThat(jobApplication.getCvId(), is(equalTo(2l)));
 
     assertThat(jobApplication.getJobId(), is(equalTo(1l)));
 
     verify(view).notifyUserOfCommunicationError();
 
 
   }
 
 
 }
