 package com.censoredsoftware.Demigods.Engine.Ability;
 
 import java.util.Set;
 
 import redis.clients.johm.*;
 
 @Model
 public class Devotion
 {
 	@Id
 	private Long Id;
	@Reference
 	@Indexed
 	private String type;
 	@Attribute
 	private Integer exp;
 	@Attribute
 	private Integer level;
 
 	public enum Type
 	{
 		OFFENSE, DEFENSE, STEALTH, SUPPORT, PASSIVE, ULTIMATE
 	}
 
 	void setType(Type type)
 	{
 		this.type = type.toString();
 	}
 
 	void setExp(Integer exp)
 	{
 		this.exp = exp;
 	}
 
 	void setLevel(Integer level)
 	{
 		this.level = level;
 	}
 
 	public Type getType()
 	{
 		return Type.valueOf(this.type);
 	}
 
 	public Integer getLevel()
 	{
 		return this.level;
 	}
 
 	public Integer getExp()
 	{
 		return this.exp;
 	}
 
 	public Integer getExpGoal()
 	{
 		return (int) Math.ceil(500 * Math.pow(this.level + 1, 2.02)); // TODO: Will need to be tweaked and will possibly be different for each Type.
 	}
 
 	public static void save(Devotion devotion)
 	{
 		JOhm.save(devotion);
 	}
 
 	public static Devotion load(long id)
 	{
 		return JOhm.get(Devotion.class, id);
 	}
 
 	public static Set<Devotion> loadAll()
 	{
 		return JOhm.getAll(Devotion.class);
 	}
 
 	@Override
 	public Object clone() throws CloneNotSupportedException
 	{
 		throw new CloneNotSupportedException();
 	}
 }
