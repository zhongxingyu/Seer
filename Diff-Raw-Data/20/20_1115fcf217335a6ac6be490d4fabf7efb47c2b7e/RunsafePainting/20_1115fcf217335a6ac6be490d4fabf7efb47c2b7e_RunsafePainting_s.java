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
		painting.setArt(Art.getById((painting.getArt().getId() + 1) % Art.values().length));
 	}
 
 	public void PrevArt()
 	{
		int id = painting.getArt().getId() - 1;
		if (id < 1)
			id = Art.values().length;
		painting.setArt(Art.getById(id));
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
