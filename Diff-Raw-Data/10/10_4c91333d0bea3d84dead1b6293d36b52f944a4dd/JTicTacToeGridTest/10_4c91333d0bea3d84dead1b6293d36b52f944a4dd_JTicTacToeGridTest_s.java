 package com.crudetech.tictactoe.delivery.swing.grid;
 
 import com.crudetech.event.Event;
 import com.crudetech.event.EventListener;
 import com.crudetech.tictactoe.delivery.gui.widgets.TicTacToeGridModel;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.*;
 
 public class JTicTacToeGridTest {
     @Test
     public void componentUIIsCreatedOnDefaultCtor() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
         assertThat(jgrid.getUI(), is(instanceOf(TicTacToeGridUI.class)));
     }
 
     @Test
     public void modelIsCreatedOnDefaultCtor() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
         assertThat(jgrid.getModel(), is(notNullValue()));
     }
 
     @Test
     public void modelIsSetOnCtor() {
         TicTacToeGridModel model = spy(new TicTacToeGridModel());
         Event<TicTacToeGridModel.CellsChangedEventObject> changedEvent = eventMock();
         when(model.cellsChanged()).thenReturn(changedEvent);
         JTicTacToeGrid jgrid = new JTicTacToeGrid(model);
 
         verify(model).cellsChanged();
         verify(changedEvent).addListener(anyListener());
         assertThat(jgrid.getModel(), is(model));
     }
 
     @Test
     public void uiIsSetOnCtor() {
         TicTacToeGridUI ui = spy(new TicTacToeGridUI());
         JTicTacToeGrid jgrid = new JTicTacToeGrid(ui);
 
         verify(ui).installUI(jgrid);
         assertThat(jgrid.getUI(), is(ui));
     }
 
     @Test
     public void uiAndModelAreSetOnCtor() {
         TicTacToeGridModel model = spy(new TicTacToeGridModel());
         Event<TicTacToeGridModel.CellsChangedEventObject> changedEvent = eventMock();
         when(model.cellsChanged()).thenReturn(changedEvent);
         TicTacToeGridUI ui = spy(new TicTacToeGridUI());
         JTicTacToeGrid jgrid = new JTicTacToeGrid(model, ui);
 
         verify(ui).installUI(jgrid);
         assertThat(jgrid.getUI(), is(ui));
         verify(model).cellsChanged();
         verify(changedEvent).addListener(anyListener());
         assertThat(jgrid.getModel(), is(model));
     }
 
     @SuppressWarnings("unchecked")
     private Event<TicTacToeGridModel.CellsChangedEventObject> eventMock() {
         return mock(Event.class);
     }
 
     @SuppressWarnings("unchecked")
     private EventListener<TicTacToeGridModel.CellsChangedEventObject> anyListener() {
         return any(EventListener.class);
     }
 
     @Test
     public void setUIOverridesInitialUI() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
         TicTacToeGridUI initialGridUI = jgrid.getUI();
         TicTacToeGridUI newGridUI = new TicTacToeGridUI();
 
         jgrid.setUI(newGridUI);
         assertThat(jgrid.getUI(), is(sameInstance(newGridUI)));
         assertThat(jgrid.getUI(), is(not(sameInstance(initialGridUI))));
     }
 
     @Test
     public void setUIInitializesUIWithComponent() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
         TicTacToeGridUI newGridUI = mock(TicTacToeGridUI.class);
 
         jgrid.setUI(newGridUI);
 
         verify(newGridUI).installUI(jgrid);
     }
 
     @Test
     public void setUIUninstallsOldUI() {
         TicTacToeGridUI oldGridUI = mock(TicTacToeGridUI.class);
         JTicTacToeGrid jgrid = new JTicTacToeGrid(oldGridUI);
 
         jgrid.setUI(new TicTacToeGridUI());
 
         verify(oldGridUI).uninstallUI(jgrid);
     }
 
     private static class TicTacToeGridModelEventRegistrationTrackingStub extends TicTacToeGridModel {
         Event<CellsChangedEventObject> cellsChangedEventSpy;
         EventListener<CellsChangedEventObject> cellsAddedHandler;
         EventListener<CellsChangedEventObject> cellsRemovedHandler;
 
         Event<ChangedEventObject> changedEventSpy;
         EventListener<ChangedEventObject> changedAddedHandler;
         EventListener<ChangedEventObject> changedRemovedHandler;
 
         @Override
         public Event<CellsChangedEventObject> cellsChanged() {
             if (cellsChangedEventSpy == null) {
                 cellsChangedEventSpy = createAndPrepareEventSpy();
             }
             return cellsChangedEventSpy;
         }
 
         @Override
         public Event<ChangedEventObject> changed() {
             if (changedEventSpy == null) {
                 changedEventSpy = createAndPrepareChangedEventSpy();
             }
             return changedEventSpy;
         }
 
         @SuppressWarnings("unchecked")
         private Event<ChangedEventObject> createAndPrepareChangedEventSpy() {
             Event<ChangedEventObject> spy = spy(super.changed());
             doAnswer(new Answer<Object>() {
                 @Override
                 public Object answer(InvocationOnMock invocation) throws Throwable {
                     changedAddedHandler = (EventListener<ChangedEventObject>) invocation.getArguments()[0];
                     return invocation.callRealMethod();
                 }
             }).when(spy).addListener(any(EventListener.class));
             doAnswer(new Answer<Object>() {
                 @Override
                 public Object answer(InvocationOnMock invocation) throws Throwable {
                     changedRemovedHandler = (EventListener<ChangedEventObject>) invocation.getArguments()[0];
                     return invocation.callRealMethod();
                 }
             }).when(spy).removeListener(any(EventListener.class));
             return spy;
         }
 
         @SuppressWarnings("unchecked")
         private Event<CellsChangedEventObject> createAndPrepareEventSpy() {
             Event<CellsChangedEventObject> spy = spy(super.cellsChanged());
             doAnswer(new Answer<Object>() {
                 @Override
                 public Object answer(InvocationOnMock invocation) throws Throwable {
                     cellsAddedHandler = (EventListener<CellsChangedEventObject>) invocation.getArguments()[0];
                     return invocation.callRealMethod();
                 }
             }).when(spy).addListener(any(EventListener.class));
             doAnswer(new Answer<Object>() {
                 @Override
                 public Object answer(InvocationOnMock invocation) throws Throwable {
                     cellsRemovedHandler = (EventListener<CellsChangedEventObject>) invocation.getArguments()[0];
                     return invocation.callRealMethod();
                 }
             }).when(spy).removeListener(any(EventListener.class));
             return spy;
         }
     }
 
     @Test
     public void setModelUnhooksOldModelCellChangedEvents() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
 
         TicTacToeGridModelEventRegistrationTrackingStub firstModel = new TicTacToeGridModelEventRegistrationTrackingStub();
         jgrid.setModel(firstModel);
         assertThat(firstModel.cellsAddedHandler, is(notNullValue()));
 
         TicTacToeGridModelEventRegistrationTrackingStub secondModel = new TicTacToeGridModelEventRegistrationTrackingStub();
         jgrid.setModel(secondModel);
         assertThat(firstModel.cellsRemovedHandler, is(firstModel.cellsAddedHandler));
         assertThat(secondModel.cellsAddedHandler, is(firstModel.cellsAddedHandler));
     }
 
     @Test
     public void setModelUnhooksOldModelChangedEvents() {
         JTicTacToeGrid jgrid = new JTicTacToeGrid();
 
         TicTacToeGridModelEventRegistrationTrackingStub firstModel = new TicTacToeGridModelEventRegistrationTrackingStub();
         jgrid.setModel(firstModel);
         assertThat(firstModel.changedAddedHandler, is(notNullValue()));
 
         TicTacToeGridModelEventRegistrationTrackingStub secondModel = new TicTacToeGridModelEventRegistrationTrackingStub();
         jgrid.setModel(secondModel);
         assertThat(firstModel.changedRemovedHandler, is(firstModel.changedAddedHandler));
         assertThat(secondModel.changedAddedHandler, is(firstModel.changedAddedHandler));
     }
 }
