 package com.censoredsoftware.Demigods.Engine.Ability;
 
 import java.util.Set;
 
 import javax.persistence.Id;
 
 import redis.clients.johm.Attribute;
 import redis.clients.johm.Indexed;
 import redis.clients.johm.JOhm;
 import redis.clients.johm.Model;
 
 @Model
 public class Devotion
 {
 	@Id
	private Long id;
 	@Attribute
 	@Indexed
 	private Type type;
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
 		this.type = type;
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
 		return this.type;
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
