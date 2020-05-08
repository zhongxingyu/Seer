 package no.runsafe.framework.server.entity;
 
 import org.bukkit.Art;
 import org.bukkit.entity.Painting;
 
 public class RunsafePainting extends RunsafeEntity
 {
 	public RunsafePainting(Painting toWrap)
 	{
 		super(toWrap);
 		painting = toWrap;
 	}
 
 	public void NextArt()
 	{
 		int oldId = painting.getArt().getId();
		int newId = (oldId + 1) % Art.values().length;
 		while (!painting.setArt(Art.getById(newId)) && newId != oldId)
 			newId = (newId + 1) % Art.values().length;
 	}
 
 	public void PrevArt()
 	{
 		int oldId = painting.getArt().getId();
 		int newId = oldId - 1;
 		if (newId < 1)
 			newId = Art.values().length;
 		while (!painting.setArt(Art.getById(newId)) && newId != oldId)
 		{
 			newId = (newId - 1);
 			if (newId < 1)
 				newId = Art.values().length;
 		}
 	}
 
 	public String getArt()
 	{
 		return painting.getArt().name();
 	}
 
 	public void setArt(String name)
 	{
 		painting.setArt(Art.getByName(name));
 	}
 
 	public void setArt(int id)
 	{
 		painting.setArt(Art.getById(id));
 	}
 
 	private final Painting painting;
 }
