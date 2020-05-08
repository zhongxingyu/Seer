 package multiworld.data;
 
 import java.util.EnumMap;
 import multiworld.api.flag.FlagName;
 import multiworld.flags.FlagValue;
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.generator.ChunkGenerator;
 
 /**
  * The class that represents worlds
  *
  * @author Fernando
  */
 public class InternalWorld
 {
 	private String worldName;
 	private long worldSeed;
 	private World.Environment worldType = World.Environment.NORMAL;
 	private ChunkGenerator worldGen;
 	private String options;
 	private EnumMap<FlagName, FlagValue> flags;
 	private String madeBy;
 	private String portalLink;
 	private String endLink;
 	private int difficulty = 2;
 
 	public InternalWorld()
 	{
 	}
 
 	InternalWorld(String name, long seed, World.Environment env, ChunkGenerator gen, String options, EnumMap<FlagName, FlagValue> map, String by, int difficulty)
 	{
 		this.worldName = name;
 		this.worldSeed = seed;
 		this.worldType = env;
 		this.worldGen = gen;
 		this.options = options;
 		this.flags = map;
 		this.madeBy = by;
 		this.portalLink = "";
 		this.endLink = "";
 		this.difficulty = difficulty;
 	}
 
 	InternalWorld(String name, long seed, World.Environment env, ChunkGenerator gen, String options, EnumMap<FlagName, FlagValue> map, String by, String link, String endLink, int difficulty)
 	{
 		this.worldName = name;
 		this.worldSeed = seed;
 		this.worldType = env;
 		this.worldGen = gen;
 		this.options = options;
 		this.flags = map;
 		this.madeBy = by;
 		this.portalLink = link;
 		this.endLink = endLink;
 		this.difficulty = difficulty;
 	}
 
 	public World getWorld()
 	{
 		return Bukkit.getWorld(worldName);
 
 	}
 
 	public String getName()
 	{
 		return this.worldName;
 	}
 
 	public World.Environment getEnv()
 	{
 		return this.worldType;
 	}
 
 	public long getSeed()
 	{
 		return this.worldSeed;
 	}
 
 	public String getOptions()
 	{
 		return this.options;
 	}
 
 	public String getPortalWorld()
 	{
 		return this.portalLink;
 	}
 
 	EnumMap<FlagName, FlagValue> getFlags()
 	{
 		return flags;
 	}
 
 	public ChunkGenerator getGen()
 	{
 		return this.worldGen;
 	}
 
 	public String getWorldType()
 	{
 		if (this.worldGen != null)
 		{
 			if (this.getMainGen().equals("NULLGEN"))
 			{
 				if (this.worldType == World.Environment.NORMAL)
 				{
 					return "Normal world with unknown external generator";
 				}
 				else if (this.worldType == World.Environment.NETHER)
 				{
 					return "Nether world with unknown external generator";
 				}
 				else if (this.worldType == World.Environment.THE_END)
 				{
 					return "End world with unknown external generator";
 				}
 			}
 			else if (this.getMainGen().startsWith("PLUGIN"))
 			{
 				if (this.worldType == World.Environment.NORMAL)
 				{
 					return "Normal world with external generator: " + this.getOptions();
 				}
 				else if (this.worldType == World.Environment.NETHER)
 				{
 					return "Nether world with external generator: " + this.getOptions();
 				}
 				else if (this.worldType == World.Environment.THE_END)
 				{
 					return "End world with external generator: " + this.getOptions();
 				}
 			}
 			else
 			{
 				if (this.worldType == World.Environment.NORMAL)
 				{
 					return "Normal world with internal generator: " + this.getMainGen() + (this.getOptions().isEmpty() ? "" : ": "+this.getOptions());
 				}
 				else if (this.worldType == World.Environment.NETHER)
 				{
 					return "Nether world with internal generator: " + this.getMainGen() + (this.getOptions().isEmpty() ? "" : ": "+this.getOptions());
 				}
 				else if (this.worldType == World.Environment.THE_END)
 				{
 					return "End world with internal generator: " + this.getMainGen() +  (this.getOptions().isEmpty() ? "" : ": "+this.getOptions());
 				}
 			}
 		}
 		else
 		{
 			if (this.worldType == World.Environment.NORMAL)
 			{
 				return "Normal world";
 			}
 			else if (this.worldType == World.Environment.NETHER)
 			{
 				return "Nether world";
 			}
 			else if (this.worldType == World.Environment.THE_END)
 			{
 				return "End world";
 			}
 		}
 
		return "Unknwon world";
 	}
 
 	public String getMainGen()
 	{
 		return this.madeBy;
 	}
 
 	public String getEndPortalWorld()
 	{
 		return this.endLink;
 	}
 
 	/**
 	 * @return the difficulty
 	 */
 	public int getDifficulty()
 	{
 		return difficulty;
 	}
 
 	@Override
 	public boolean equals(Object obj)
 	{
 		if (obj == null)
 		{
 			return false;
 		}
 		if (getClass() != obj.getClass())
 		{
 			return false;
 		}
 		final InternalWorld other = (InternalWorld) obj;
 		if ((this.worldName == null) ? (other.worldName != null) : !this.worldName.equals(other.worldName))
 		{
 			return false;
 		}
 		if (this.worldSeed != other.worldSeed)
 		{
 			return false;
 		}
 		if (this.worldType != other.worldType)
 		{
 			return false;
 		}
 		if (this.worldGen != other.worldGen && (this.worldGen == null || !this.worldGen.equals(other.worldGen)))
 		{
 			return false;
 		}
 		if ((this.options == null) ? (other.options != null) : !this.options.equals(other.options))
 		{
 			return false;
 		}
 		if (this.flags != other.flags && (this.flags == null || !this.flags.equals(other.flags)))
 		{
 			return false;
 		}
 		if ((this.madeBy == null) ? (other.madeBy != null) : !this.madeBy.equals(other.madeBy))
 		{
 			return false;
 		}
 		if ((this.portalLink == null) ? (other.portalLink != null) : !this.portalLink.equals(other.portalLink))
 		{
 			return false;
 		}
 		if ((this.endLink == null) ? (other.endLink != null) : !this.endLink.equals(other.endLink))
 		{
 			return false;
 		}
 		if (this.difficulty != other.difficulty)
 		{
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public int hashCode()
 	{
 		int hash = 0x82746283;
 		hash ^= (this.worldName != null ? this.worldName.hashCode() : 0);
 		hash ^= (int) (this.worldSeed ^ (this.worldSeed >>> 32));
 		hash ^= (this.worldType != null ? this.worldType.hashCode() : 0);
 		hash ^= (this.worldGen != null ? this.worldGen.hashCode() : 0);
 		hash ^= (this.options != null ? this.options.hashCode() : 0);
 		hash ^= (this.flags != null ? this.flags.hashCode() : 0);
 		hash ^= (this.madeBy != null ? this.madeBy.hashCode() : 0);
 		hash ^= (this.portalLink != null ? this.portalLink.hashCode() : 0);
 		hash ^= (this.endLink != null ? this.endLink.hashCode() : 0);
 		hash ^= this.difficulty *123456;
 		return hash;
 	}
 
 	@Override
 	public String toString()
 	{
 		return "InternalWorld{"
 			+ "worldName=" + worldName
 			+ ", worldSeed=" + worldSeed
 			+ ", worldType=" + worldType
 			+ ", worldGen=" + worldGen
 			+ ", options=" + options
 			+ ", flags=" + flags
 			+ ", madeBy=" + madeBy
 			+ ", portalLink=" + portalLink
 			+ ", endLink=" + endLink
 			+ ", difficulty=" + difficulty
 			+ '}';
 	}
 
 	void setWorldName(String worldName)
 	{
 		this.worldName = worldName;
 	}
 
 	void setWorldSeed(long worldSeed)
 	{
 		this.worldSeed = worldSeed;
 	}
 
 	public void setWorldType(World.Environment worldType)
 	{
 		this.worldType = worldType;
 	}
 
 	public void setWorldGen(ChunkGenerator worldGen)
 	{
 		this.worldGen = worldGen;
 	}
 
 	void setOptions(String options)
 	{
 		this.options = options;
 	}
 
 	void setFlags(EnumMap<FlagName, FlagValue> flags)
 	{
 		this.flags = flags;
 	}
 
 	void setMadeBy(String madeBy)
 	{
 		this.madeBy = madeBy;
 	}
 
 	void setPortalLink(String portalLink)
 	{
 		this.portalLink = portalLink;
 	}
 
 	void setEndLink(String endLink)
 	{
 		this.endLink = endLink;
 	}
 
 	void setDifficulty(int difficulty)
 	{
 		this.difficulty = difficulty;
 	}
 }
