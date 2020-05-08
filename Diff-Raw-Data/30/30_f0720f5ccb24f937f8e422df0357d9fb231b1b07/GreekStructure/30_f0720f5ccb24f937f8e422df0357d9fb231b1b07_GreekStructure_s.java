 package com.censoredsoftware.demigods.greek.structure;
 
 import com.censoredsoftware.demigods.engine.structure.Structure;
 import com.censoredsoftware.demigods.engine.structure.StructureData;
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import org.bukkit.Location;
 import org.bukkit.event.Listener;
 
 import java.util.Collection;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 public enum GreekStructure implements Structure
 {
 	/**
 	 * General
 	 */
 	// Altar
 	ALTAR(Altar.name, Altar.AltarDesign.values(), Altar.getDesign, Altar.createNew, Altar.flags, Altar.listener, Altar.radius, Altar.life),
 
 	// Obelisk
 	OBELISK(Obelisk.name, Obelisk.ObeliskDesign.values(), Obelisk.getDesign, Obelisk.createNew, Obelisk.flags, Obelisk.listener, Obelisk.radius, Obelisk.life),
 
 	// Shrine
 	SHRINE(Shrine.name, Shrine.ShrineDesign.values(), Shrine.getDesign, Shrine.createNew, Shrine.flags, Shrine.listener, Shrine.radius, Shrine.life);
 
 	private String name;
 	private Design[] designs;
 	private Function<Location, Design> getDesign;
 	private Function<Design, StructureData> createNew;
 	private Set<Structure.Flag> flags;
 	private Listener listener;
 	private int radius;
 	private float life;
 
 	private GreekStructure(String name, Design[] designs, Function<Location, Design> getDesign, Function<Design, StructureData> createNew, Set<Structure.Flag> flags, Listener listener, int radius, float life)
 	{
 		this.name = name;
 		this.designs = designs;
 		this.getDesign = getDesign;
 		this.createNew = createNew;
 		this.flags = flags;
 		this.listener = listener;
 		this.radius = radius;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public Design getDesign(final String name)
 	{
 		try
 		{
 			return Iterables.find(Sets.newHashSet(designs), new Predicate<Design>()
 			{
 				@Override
 				public boolean apply(Design design)
 				{
 					return design.getName().equalsIgnoreCase(name);
 				}
 			});
 		}
 		catch(NoSuchElementException ignored)
 		{}
 		return null;
 	}
 
 	public Set<Structure.Flag> getFlags()
 	{
 		return flags;
 	}
 
 	public Listener getUniqueListener()
 	{
 		return listener;
 	}
 
 	public int getRadius()
 	{
 		return radius;
 	}
 
 	public float getLife()
 	{
 		return life;
 	}
 
 	public Collection<StructureData> getAll()
 	{
 		return Collections2.filter(StructureData.Util.loadAll(), new Predicate<StructureData>()
 		{
 			@Override
 			public boolean apply(StructureData save)
 			{
 				return save.getTypeName().equals(getName());
 			}
 		});
 	}
 
 	public StructureData createNew(Location reference, boolean generate)
 	{
 		Design design = getDesign.apply(reference);
 		StructureData save = createNew.apply(design);
 
 		// All structures need these
 		save.generateId();
 		save.setReferenceLocation(reference);
 		save.setType(getName());
 		save.setDesign(design.getName());
 		save.addFlags(getFlags());
 		save.setActive(true);
 		save.save();
 
 		if(generate) save.generate();
 		return save;
 	}
 }
