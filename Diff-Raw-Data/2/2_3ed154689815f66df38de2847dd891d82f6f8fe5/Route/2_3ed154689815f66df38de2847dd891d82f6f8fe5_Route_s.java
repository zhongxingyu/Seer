 /*
  * Copyright (C) 2012 TomyLobo
  *
  * This file is part of Routes.
  *
  * Routes is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package eu.tomylobo.routes.infrastructure;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Multimap;
 
 import eu.tomylobo.abstraction.World;
 import eu.tomylobo.abstraction.entity.Player;
 import eu.tomylobo.math.Location;
 import eu.tomylobo.math.Vector;
 import eu.tomylobo.routes.Routes;
 import eu.tomylobo.routes.commands.system.PermissionDeniedException;
 import eu.tomylobo.routes.infrastructure.editor.VisualizedRoute;
 import eu.tomylobo.routes.infrastructure.interpolation.Interpolation;
 import eu.tomylobo.routes.infrastructure.interpolation.KochanekBartelsInterpolation;
 import eu.tomylobo.routes.infrastructure.interpolation.ReparametrisingInterpolation;
 import eu.tomylobo.routes.util.Ini;
 import eu.tomylobo.routes.util.ScheduledTask;
 
 /**
  * Represents a route from one point to another, consisting of multiple nodes.
  *
  * @author TomyLobo
  *
  */
 public final class Route {
 	private final String name;
 	private World world;
 
 	private final List<Node> nodes = new ArrayList<Node>();
 	private boolean nodesDirty = false;
 
 	//private Interpolation interpolation = new LinearInterpolation();
 	private Interpolation interpolation = new ReparametrisingInterpolation(new KochanekBartelsInterpolation());
 
 	private ScheduledTask task = new ScheduledTask(Routes.getInstance()) {
 		@Override
 		public void run() {
 			clearVisualization();
 		}
 	};
 
 	private VisualizedRoute visualizedRoute;
 
 	public Route(String name) {
 		super();
 		this.name = name;
 	}
 
 	public List<Node> getNodes() {
 		return nodes;
 	}
 
 	public void addNodes(Location... locations) {
 		addNodes(-1, locations);
 	}
 
 	public void addNodes(int index, Location... locations) {
 		Node[] nodes = new Node[locations.length];
 		for (int i = 0; i < locations.length; ++i) {
 			Location location = locations[i];
 			World world = location.getWorld();
 			if (this.world == null) {
 				this.world = world;
 			}
 			else if (!this.world.equals(world)) {
 				throw new IllegalArgumentException("New node must be in the same world.");
 			}
 
 			nodes[i] = new Node(location.getPosition());
 		}
 		addNodes(index, nodes);
 	}
 
 	public void addNodes(Node... nodes) {
 		addNodes(-1, nodes);
 	}
 
 	public void addNodes(int index, Node... nodes) {
 		if (index == -1) {
 			index = this.nodes.size();
 		}
 		this.nodes.addAll(index, Arrays.asList(nodes));
 		for (Node node : nodes) {
 			node.setRoute(this);
 		}
 		nodesDirty = true;
 	}
 
 	public Location getLocation(double position) {
 		ensureClean();
 
 		final Vector vec = interpolation.getPosition(position);
 		if (vec == null)
 			return null;
 
 		return Location.fromEye(world, vec, interpolation.get1stDerivative(position));
 	}
 
 	private void ensureClean() {
 		if (nodesDirty) {
 			interpolation.setNodes(nodes);
 			nodesDirty = false;
 			Routes.getInstance().transportSystem.save();
 		}
 	}
 
 	public Vector getVelocity(double position) {
 		ensureClean();
 
 		return interpolation.get1stDerivative(position);
 	}
 
 	public void visualize(double pointsPerMeter, long ticks) {
 		clearVisualization();
 
 		visualizedRoute = new VisualizedRoute(this, pointsPerMeter);
 
 		task.scheduleSyncDelayed(ticks);
 	}
 
 	public void save(Multimap<String, Multimap<String, String>> sections) {
 		final String routeSectionName = "route "+name;
 		final Multimap<String, String> routeSection = LinkedListMultimap.create();
 
 		Ini.saveWorld(routeSection, "%s", world);
 		routeSection.put("nodes", String.valueOf(nodes.size()));
 
 		sections.put(routeSectionName, routeSection);
 
 		for (int i = 0; i < nodes.size(); ++i) {
 			final Node node = nodes.get(i);
 
 			final String nodeName = name + "-" + i;
 			node.save(sections, nodeName);
 		}
 	}
 
 	public void load(Multimap<String, Multimap<String, String>> sections) {
 		final String routeSectionName = "route "+name;
 		final Multimap<String, String> routeSection = Ini.getOnlyValue(sections.get(routeSectionName));
 
 		world = Ini.loadWorld(routeSection, "%s");
 		int nNodes = Ini.getOnlyInt(routeSection.get("nodes"));
 
 		nodes.clear();
 		((ArrayList<Node>) nodes).ensureCapacity(nNodes);
 		for (int i = 0; i < nNodes; ++i) {
 			final String nodeName = name + "-" + i;
 
			nodes.add(new Node(sections, nodeName));
 		}
 		nodesDirty = true;
 	}
 
 	public double length() {
 		ensureClean();
 
 		return interpolation.arcLength(0, 1);
 	}
 
 	private void clearVisualization() {
 		task.cancel();
 		if (visualizedRoute != null) {
 			visualizedRoute.removeEntities();
 		}
 	}
 
 	void setDirty() {
 		nodesDirty = true;
 	}
 
 	public double getArcLength(double positionA, double positionB) {
 		return interpolation.arcLength(positionA, positionB);
 	}
 
 	public World getWorld() {
 		return world;
 	}
 
 	public int getSegment(double position) {
 		return interpolation.getSegment(position);
 	}
 
 	public void checkPermission(Player player, String command) {
 		if (!hasPermission(player, command))
 			throw new PermissionDeniedException();
 	}
 
 	public boolean hasPermission(Player player, String command) {
 		return player.hasPermission("routes."+command+"."+name);
 	}
 
 	public String getName() {
 		return name;
 	}
 }
