 /*
  * This file is part of d3.
  * 
  * d3 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * d3 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with d3.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Copyright 2010 Guilhelm Savin
  */
 package org.d3.feature;
 
 import java.awt.GraphicsEnvironment;
 import java.util.concurrent.TimeUnit;
 
 import org.d3.Args;
 import org.d3.Console;
 import org.d3.Actor;
 import org.d3.actor.ActorInternalException;
 import org.d3.actor.ActorsEvent;
 import org.d3.actor.Agency;
 import org.d3.actor.Feature;
 import org.d3.actor.LocalActor;
 import org.d3.actor.StepActor;
 import org.d3.annotation.ActorPath;
 import org.d3.events.Bindable;
 import org.d3.events.NonBindableActorException;
 import org.d3.feature.model.MultiThreadProxyPipe;
 import org.d3.feature.model.ResizableView;
 import org.graphstream.algorithm.DynamicAlgorithm;
 import org.graphstream.graph.Edge;
 import org.graphstream.graph.Graph;
 import org.graphstream.graph.Node;
 import org.graphstream.graph.implementations.AdjacencyListGraph;
 import org.graphstream.graph.implementations.Graphs;
 import org.graphstream.stream.thread.ThreadProxyPipe;
 import org.graphstream.ui.layout.Layout;
 import org.graphstream.ui.layout.Layouts;
 import org.graphstream.ui.swingViewer.DefaultView;
 import org.graphstream.ui.swingViewer.Viewer;
 import org.graphstream.ui.swingViewer.Viewer.CloseFramePolicy;
 
 @ActorPath("/features/model")
 public class Model extends Feature implements StepActor, Bindable {
 	protected static long MODEL_ID_GENERATOR = 0;
 
 	protected static String defaultStyleSheet = "graph {" + " padding: 50px;"
 			+ "} " + "node { fill-color: black; } "
 			+ "node.remote { fill-color: red; } "
 			+ "node.entity { size: 10px; } "
 			+ "node.application { size: 20px; }";
 
 	public static enum LoadBalancer {
 		NONE(""), ANTCO2("org.graphstream.algorithm.antco2.AntCo2Algorithm");
 
 		public final String algorithmClassName;
 
 		LoadBalancer(String className) {
 			this.algorithmClassName = className;
 		}
 	}
 
 	protected Graph model;
 	protected Viewer viewer;
 	protected long weightDecreaserPeriod = 500;
 	protected TimeUnit weightDecreaserUnit = TimeUnit.MILLISECONDS;
 	protected boolean justEntities = true;
 	protected DynamicAlgorithm loadBalancer;
 
 	public Model() {
 		super(String.format("model%x", MODEL_ID_GENERATOR++));
 	}
 
 	public void initFeature() {
 		Args args = Agency.getActorArgs(this);
 
 		model = Graphs.synchronizedGraph(new AdjacencyListGraph(getId(), false,
 				true));
 		model.addAttribute("ui.stylesheet", defaultStyleSheet);
 		model.addAttribute("ui.quality");
 		model.addAttribute("ui.antialias");
 
 		if (args.has("display") && Boolean.parseBoolean(args.get("display"))
 				&& isDisplayable())
 			display(true);
 
 		if (args.has("load_balancing")
 				&& Boolean.parseBoolean(args.get("load_balancing"))) {
 
 			LoadBalancer loadBalancer = LoadBalancer.ANTCO2;
 
 			try {
 				if (args.has("load_balancer"))
 					loadBalancer = LoadBalancer.valueOf(args
 							.get("load_balancer"));
 			} catch (Exception e) {
 				Console.warning(e.getMessage());
 			}
 
 			try {
 				@SuppressWarnings("unchecked")
 				Class<? extends DynamicAlgorithm> cls = (Class<? extends DynamicAlgorithm>) Class
 						.forName(loadBalancer.algorithmClassName);
 				this.loadBalancer = cls.newInstance();
 				this.loadBalancer.init(model);
 			} catch (Exception e) {
 				throw new ActorInternalException(e);
 			}
 		}
 
 		try {
 			Agency.getLocalAgency().getActors().getEventDispatcher().bind();
 		} catch (NonBindableActorException e) {
 			Console.exception(e);
 		}
 
 		for (LocalActor actor : Agency.getLocalAgency().getActors())
 			createNode(actor);
 	}
 
 	protected void display(boolean autolayout) {
 		if (isDisplayable()) {
 			if (viewer != null) {
 
 			} else {
 				ThreadProxyPipe pipe = new MultiThreadProxyPipe(model);
 
 				viewer = new Viewer(pipe);
 				viewer.setCloseFramePolicy(CloseFramePolicy.CLOSE_VIEWER);
 
 				DefaultView view = new ResizableView(viewer,
 						Viewer.DEFAULT_VIEW_ID, Viewer.newGraphRenderer());
 
 				viewer.addView(view);
 				view.openInAFrame(true);
 
 				if (autolayout) {
 					Layout layout = Layouts.newLayoutAlgorithm();
 					viewer.enableAutoLayout(layout);
 				}
 			}
 		}
 	}
 
 	protected Node createNode(Actor actor) {
 		String id;
 		id = actor.isRemote() ? actor.getAgencyFullPath() : actor.getFullPath();
 
 		Node n = model.getNode(id);
 
 		if (n == null) {
 			String type = actor.getType().name();
 
 			n = model.addNode(id);
 			n.addAttribute("type", type);
 			n.setAttribute("ui.class", type);
 			n.setAttribute("label", id);
 		}
 
 		return n;
 	}
 
 	protected void deleteNode(Actor actor) {
 		String id;
 		id = actor.isRemote() ? actor.getAgencyFullPath() : actor.getFullPath();
 
 		model.removeNode(id);
 	}
 
 	protected void updateEdge(Actor source, Actor target) {
 		Node sourceNode, targetNode;
 		Edge edge;
 
 		sourceNode = createNode(source);
 		targetNode = createNode(target);
 
 		edge = sourceNode.getEdgeToward(targetNode.getId());
 
 		if (edge == null) {
 			edge = model.addEdge(String.format("%s--%s", sourceNode.getId(),
 					targetNode.getId()), sourceNode.getId(),
 					targetNode.getId(), true);
 			edge.addAttribute("weight", 1);
 		} else {
 			increaseWeight(edge);
 		}
 	}
 
 	protected boolean isDisplayable() {
 		return !GraphicsEnvironment.isHeadless();
 	}
 
 	protected void increaseWeight(Edge e) {
 		double w = e.getNumber("weight");
 		w *= 1.2;
 		e.changeAttribute("weight", w);
 	}
 
 	protected void decreaseWeight(Edge e) {
 		double w = e.getNumber("weight");
 		w *= 0.75;
 
 		if (w < 0.1) {
 			Node src = e.getNode0();
 			Node trg = e.getNode1();
 			model.removeEdge(e.getId());
 
 			if (src.hasAttribute("remote") && src.getDegree() == 0)
 				model.removeNode(src.getId());
 
 			if (trg.hasAttribute("remote") && trg.getDegree() == 0)
 				model.removeNode(trg.getId());
 		} else {
 			e.changeAttribute("weight", w);
 		}
 	}
 
 	public <K extends Enum<K>> void trigger(K event, Object... data) {
 		if (ActorsEvent.class.isAssignableFrom(event.getClass())) {
			ActorsEvent aEvent = ActorsEvent.class.cast(event);
 
 			switch (aEvent) {
 			case ACTOR_REGISTERED: {
 				Actor actor = (Actor) data[0];
 				createNode(actor);
 				break;
 			}
 			case ACTOR_UNREGISTERED: {
 				Actor actor = (Actor) data[0];
 				deleteNode(actor);
 				break;
 			}
 			case CALL: {
 				Actor source = (Actor) data[0];
 				Actor target = (Actor) data[1];
 				updateEdge(source, target);
 				break;
 			}
 			}
 		}
 	}
 
 	public long getStepDelay(TimeUnit unit) {
 		return unit.convert(weightDecreaserPeriod, weightDecreaserUnit);
 	}
 
 	public void step() {
 		for (Edge e : model.getEachEdge())
 			decreaseWeight(e);
 
 		if (Model.this.loadBalancer != null)
 			Model.this.loadBalancer.compute();
 	}
 }
