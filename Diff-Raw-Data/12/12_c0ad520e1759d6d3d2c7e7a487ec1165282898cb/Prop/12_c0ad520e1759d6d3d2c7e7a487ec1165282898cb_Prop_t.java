 package com.evervoid.state.prop;
 
 import com.evervoid.json.Json;
 import com.evervoid.json.Jsonable;
 import com.evervoid.state.EVContainer;
 import com.evervoid.state.geometry.GridLocation;
 import com.evervoid.state.player.Player;
 
 public abstract class Prop implements Jsonable, Comparable<Prop>
 {
 	protected EVContainer<Prop> aContainer = null;
 	private final int aID;
 	protected GridLocation aLocation;
 	protected final Player aPlayer;
 	private final String aPropType;
 
 	protected Prop(final int id, final Player player, final GridLocation location, final String propType)
 	{
 		aPlayer = player;
 		aLocation = location.clone();
 		aID = id;
 		aPropType = propType;
 	}
 
 	protected Prop(final Json j, final Player player, final String propType)
 	{
 		// get the relevant data and pass it to the actual constructor
 		this(j.getIntAttribute("id"), player, new GridLocation(j.getAttribute("location")), propType);
 	}
 
 	@Override
 	public int compareTo(final Prop other)
 	{
 		return getID() - other.getID();
 	}
 
 	/**
 	 * Deregisters this Prop's location to the Container, if any
 	 */
 	public void deregister()
 	{
 		if (aContainer == null) {
 			return;
 		}
 		aContainer.removeElem(this);
		aContainer = null;
 	}
 
 	/**
 	 * Call this when the prop enters a container or is created in a container
 	 * 
 	 * @param container
 	 *            The solar system that the prop is in
 	 */
 	public void enterContainer(final EVContainer<Prop> container)
 	{
 		if (container == aContainer) {
 			return;
 		}
 		deregister();
 		aContainer = container;
 		register();
 	}
 
 	/**
 	 * @return The Container that this Prop is in, or none if unattached
 	 */
 	public EVContainer<Prop> getContainer()
 	{
 		return aContainer;
 	}
 
 	public int getID()
 	{
 		return aID;
 	}
 
 	public GridLocation getLocation()
 	{
 		return aLocation.clone();
 	}
 
 	public Player getPlayer()
 	{
 		return aPlayer;
 	}
 
 	public String getPropType()
 	{
 		return aPropType;
 	}
 
 	/**
 	 * Call this when the prop leaves the container (or gets destroyed, or goes inside a carrier ship...)
 	 */
 	public void leaveContainer()
 	{
		deregister();
 	}
 
 	/**
 	 * Registers this Prop's location to the Solar System, if any
 	 */
 	public void register()
 	{
 		if (aContainer == null) {
 			return;
 		}
 		aContainer.addElem(this);
 	}
 
 	@Override
 	public Json toJson()
 	{
 		return new Json().setStringAttribute("player", aPlayer.getName()).setAttribute("location", aLocation)
 				.setIntAttribute("id", aID).setStringAttribute("proptype", getPropType());
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Prop_" + aPropType + " of " + aPlayer + " at " + aLocation;
 	}
 }
