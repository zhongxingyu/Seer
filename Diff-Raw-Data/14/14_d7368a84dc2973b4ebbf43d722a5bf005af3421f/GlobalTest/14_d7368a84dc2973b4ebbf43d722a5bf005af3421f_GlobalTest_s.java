 import akka.actor.ActorSystem;
 import akka.actor.Cancellable;
 import akka.actor.Scheduler;
 import akka.dispatch.Dispatchers;
 import akka.dispatch.MessageDispatcher;
 import jobs.FakeEventJob;
 import jobs.FakeHistoryRecordJob;
 import jobs.RemoveOldEventJob;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 import play.Play;
 import play.libs.Akka;
 import play.test.FakeApplication;
 import scala.concurrent.ExecutionContext;
 import scala.concurrent.ExecutionContextExecutor;
 import scala.concurrent.duration.Duration;
 import scala.concurrent.duration.FiniteDuration;
 import utils.BObjects;
 
 import java.util.List;
 
 import static jobs.Jobs.BET365_JOB;
 import static jobs.Jobs.MARATHON_JOB;
 import static jobs.Jobs.REMOVE_OLD_HISTORY_JOB;
 import static models.store.Organisation.BET365;
 import static models.store.Organisation.MARATHON;
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.ArgumentCaptor.forClass;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.refEq;
 import static org.mockito.Matchers.same;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 import static org.powermock.api.mockito.PowerMockito.verifyStatic;
 import static org.powermock.api.mockito.PowerMockito.when;
 import static play.Play.isProd;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.start;
 import static play.test.Helpers.stop;
 import static tests.BMatchers.reflectionEq;
 import static utils.BObjects.logAndStopExceptions;
 
 // TODO: logic of this class is too complex.
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({GlobalTest.class, Akka.class, ExecutionContext.class, BObjects.class, Play.class})
 public class GlobalTest {
 
   @Mock
   private ActorSystem actorSystemMock;
   @Mock
   private Scheduler schedulerMock;
   @Mock
   private Dispatchers dispatchersMock;
   @Mock
   private ExecutionContextExecutor defaultDispatcherMock;
   @Mock
   private MessageDispatcher bet365FetchingDispatcherMock;
   @Mock
   private MessageDispatcher marathonFetchingDispatcherMock;
   @Mock
   private Cancellable scheduleMock;
   private FakeApplication fakeApplication;
   @Mock
   private Runnable wrappedRunnable;
 
   @Before
   public void before()
     throws Exception {
     mockStatic(Akka.class);
     when(Akka.system()).thenReturn(actorSystemMock);
 
     when(actorSystemMock.scheduler()).thenReturn(schedulerMock);
 
     when(actorSystemMock.dispatchers()).thenReturn(dispatchersMock);
 
     when(actorSystemMock.dispatcher()).thenReturn(defaultDispatcherMock);
    when(dispatchersMock.lookup("fetch-bet365")).thenReturn(bet365FetchingDispatcherMock);
    when(dispatchersMock.lookup("fetch-marathon")).thenReturn(marathonFetchingDispatcherMock);
 
     when(schedulerMock.schedule(any(FiniteDuration.class), any(FiniteDuration.class), any(Runnable.class), any(ExecutionContext.class))).thenReturn(
       scheduleMock);
 
     mockStatic(BObjects.class);
     when(BObjects.logAndStopExceptions(any(Runnable.class))).thenReturn(wrappedRunnable);
 
     fakeApplication = fakeApplication(new Global());
   }
 
   @Test
   public void startAndStopDevMode()
     throws InterruptedException {
     mockStatic(Play.class);
     when(isProd()).thenReturn(false);
 
     start(fakeApplication);
 
     FiniteDuration offset = Duration.create(0, "ms");
     FiniteDuration delay = Duration.create(30, "sec");
     long oldEventAge = Duration.create(5, "min").toMillis();
 
     verifyStatic();
     isProd();
 
     ArgumentCaptor<Runnable> argsCaptor = forClass(Runnable.class);
     verifyStatic(times(4));
     logAndStopExceptions(argsCaptor.capture());
     List args = argsCaptor.getAllValues();
 
     assertThat(args.get(0)).satisfies(reflectionEq(new RemoveOldEventJob(oldEventAge)));
     assertThat(args.get(1)).satisfies(reflectionEq(new FakeEventJob()));
     assertThat(args.get(2)).satisfies(reflectionEq(new FakeHistoryRecordJob(BET365)));
     assertThat(args.get(3)).satisfies(reflectionEq(new FakeHistoryRecordJob(MARATHON)));
 
     verify(schedulerMock, times(4)).schedule(refEq(offset), refEq(delay), same(wrappedRunnable), same(defaultDispatcherMock));
 
     stop(fakeApplication);
 
     verify(scheduleMock, times(4)).cancel();
   }
 
   @Test
   public void startProdMode()
     throws InterruptedException {
     mockStatic(Play.class);
     when(isProd()).thenReturn(true);
 
     start(fakeApplication);
 
     ArgumentCaptor<Runnable> jobArgsCaptor = forClass(Runnable.class);
     verifyStatic(times(4));
     logAndStopExceptions(jobArgsCaptor.capture());
     List jobs = jobArgsCaptor.getAllValues();
 
     assertThat(jobs).hasSize(4);
     assertThat(jobs.get(0)).satisfies(reflectionEq(new RemoveOldEventJob(Duration.create(1, "day").toMillis())));
     assertThat(jobs.get(1)).isSameAs(REMOVE_OLD_HISTORY_JOB);
     assertThat(jobs.get(2)).isSameAs(MARATHON_JOB);
     assertThat(jobs.get(3)).isSameAs(BET365_JOB);
 
     ArgumentCaptor<FiniteDuration> durationArgsCaptor = forClass(FiniteDuration.class);
     ArgumentCaptor<ExecutionContext> executionContextArgsCaptor = forClass(ExecutionContext.class);
 
     verify(schedulerMock, times(4)).schedule(durationArgsCaptor.capture(), durationArgsCaptor.capture(), same(wrappedRunnable),
       executionContextArgsCaptor.capture());
 
     List<FiniteDuration> durations = durationArgsCaptor.getAllValues();
 
     assertThat(durations).hasSize(8);
     assertThat(durations.get(0)).satisfies(reflectionEq(Duration.create(0, "sec")));
     assertThat(durations.get(1)).satisfies(reflectionEq(Duration.create(1, "min")));
 
     assertThat(durations.get(2)).satisfies(reflectionEq(Duration.create(10, "sec")));
     assertThat(durations.get(3)).satisfies(reflectionEq(Duration.create(1, "min")));
 
     assertThat(durations.get(4)).satisfies(reflectionEq(Duration.create(20, "sec")));
     assertThat(durations.get(5)).satisfies(reflectionEq(Duration.create(1, "min")));
 
     assertThat(durations.get(6)).satisfies(reflectionEq(Duration.create(30, "sec")));
     assertThat(durations.get(7)).satisfies(reflectionEq(Duration.create(1, "min")));
 
     List<ExecutionContext> executionContexts = executionContextArgsCaptor.getAllValues();
     assertThat(executionContexts).hasSize(4).containsExactly(defaultDispatcherMock, defaultDispatcherMock, marathonFetchingDispatcherMock,
       bet365FetchingDispatcherMock);
   }
 }
