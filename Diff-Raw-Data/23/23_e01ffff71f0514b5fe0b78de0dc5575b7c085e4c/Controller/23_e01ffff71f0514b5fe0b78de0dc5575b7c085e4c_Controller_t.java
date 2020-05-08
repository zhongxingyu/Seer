 package pl.edu.pw.elka.pszt.inteligraph.controller;
 
 import javax.swing.SwingUtilities;
 
 import pl.edu.pw.elka.pszt.inteligraph.Constans;
 import pl.edu.pw.elka.pszt.inteligraph.events.Event;
 import pl.edu.pw.elka.pszt.inteligraph.events.EventHandler;
 import pl.edu.pw.elka.pszt.inteligraph.events.EventName;
 import pl.edu.pw.elka.pszt.inteligraph.events.EventsBlockingQueue;
 import pl.edu.pw.elka.pszt.inteligraph.events.EventsHandlersMap;
 import pl.edu.pw.elka.pszt.inteligraph.model.Model;
 import pl.edu.pw.elka.pszt.inteligraph.view.View;
 
 public class Controller {
 
 	EventsBlockingQueue blockingQueue;
 	Model model;
 	View view;
 	EventsHandlersMap eventHandlers = new EventsHandlersMap();
 	
 	public Controller(Model model, View view, EventsBlockingQueue blockingQueue) {
 		this.model = model;
 		this.view = view;
 		this.blockingQueue = blockingQueue;
 		
 		this.loadHandlers();
 		
 		try {
 			blockingQueue.put(new Event(EventName.SHOW_EMPTY_WINDOW));
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Ładuje handlery do obsługi zdarzeń
 	 */
 	private void loadHandlers() {
 		eventHandlers.put(EventName.SHOW_EMPTY_WINDOW, new EventHandler() {
 			
 			@Override
 			public void execute() {
 				view.showWindow();
 				view.getGraphParametersPanel().setStopButtonActive(false);
 				view.getGraphParametersPanel().setStepsButtonActive(false);
 				view.getStatusBar().setAppState("...");
 			}
 		});
 		
 		eventHandlers.put(EventName.DRAW_GRAPH_N, new EventHandler() {
 
 		    @Override
 		    public void execute() {
 			if (model.getGraph() != null) {
         			SwingUtilities.invokeLater(new Runnable() {
         			    @Override
         			    public void run() {
         				view.getGraphParametersPanel().setStopButtonActive(true);
         				view.getGraphParametersPanel().setStepsButtonActive(false);
                 			model.calculateVerticesPositions(view.getMi(), view.getLambda(), view.getSteps());
                 			view.getStatusBar().setAppState("Liczę dla " + view.getSteps() + " kroków...");
         			    }
         			});
 
 			}
 		    }
 		});
 		
 		eventHandlers.put(EventName.DRAW_GRAPH_INF, new EventHandler() {
 		    
 		    @Override
 		    public void execute() {
 			if (model.getGraph() != null) {
         			SwingUtilities.invokeLater(new Runnable() {
         			    @Override
         			    public void run() {
         				view.getGraphParametersPanel().setStopButtonActive(true);
         				view.getGraphParametersPanel().setStepsButtonActive(false);
                 			model.calculateVerticesPositions(view.getMi(), view.getLambda());	
                 			view.getStatusBar().setAppState("Liczę dla ∞ kroków...");
         			    }
         			});
 			}
 		    }
 		});
 		
 		eventHandlers.put(EventName.DRAW_GRAPH_STOP, new EventHandler() {
 		    
 		    @Override
 		    public void execute() {
 			model.stopCalculations();
 			model.getEvolutionSteps();
 			
 			SwingUtilities.invokeLater(new Runnable() {
 			    @Override
 			    public void run() {
 				view.getGraphParametersPanel().setStopButtonActive(false);
 				view.getGraphParametersPanel().setStepsButtonActive(true);
 				view.getStatusBar().setAppState(Constans.STATE_COMPUTING_ENDED);
 				view.getStatusBar().setEvolutionSteps(model.getEvolutionSteps());
 			    }
 			});
 		    }
 		});
 		eventHandlers.put(EventName.CHOOSE_FILE, new EventHandler() {
 		    
 		    @Override
 		    public void execute() {
 			model.buildGraph(view.getGraphFile());
 
 			SwingUtilities.invokeLater(new Runnable() {
 			    @Override
 			    public void run() {
 				view.getGraphParametersPanel().setStopButtonActive(false);
 				view.getGraphParametersPanel().setStepsButtonActive(true);
 				view.getStatusBar().setAppState("Otworzono: " + view.getGraphFile().getName());
 				view.getStatusBar().setGraphParams(model.getGraph().getVertexCount(), model.getGraph().getEdgeCount());
 			    }
 			});
 			
			model.calculateVerticesPositions(1, 10);
 			try
 			{
 				//model.getBestArrangement();
 				view.setGraphView(model.getGraph(), model.getBestArrangement());
 			} catch (Exception e)
 			{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		    }
 		});
 	}
 
 	/**
 	 * Główna pętla programu obsługująca kolejkę zdarzeń
 	 */
 	public void programStart() {
 		
 		Event event;
 		EventHandler eventHandler;
 		
 		while(true)
 		{
 			try {
 				event = this.blockingQueue.take();
 				eventHandler = eventHandlers.get(event.getEventName());
 				eventHandler.execute();
 				
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 	}
 
 }
