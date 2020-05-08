 package com.tehbeard.BeardStat.containers;
 
 import me.tehbeard.utils.expressions.InFixExpression;
 
 /**
  * Dynamic player stats generated from composites of other player stats.
  * A Dynamic player stat is only stored if expressly set to be stored.
  * it is computed at runtime using an expression bound to that stat.
  * @author James
  *
  */
 public class DynamicStat implements IStat {
 
 
 	private String domain;
 	private String world;
    private String category;
    private String statistic;
     private EntityStatBlob owner;
     private InFixExpression expression;
     String expr;
     
     private boolean archive = false;
     
     public DynamicStat(String cat,String stat,String expr){
         this(cat,stat,expr,false);
         
 
     }
     
     public DynamicStat(String cat,String stat,String expr,boolean archive){
        this.category = cat;
        this.statistic = stat;
         this.expression = new InFixExpression(expr);
         this.archive = archive;
         this.expr = expr;
 
     }
 
 
     public int getValue() {
         return expression.getValue(owner);
     }
 
 
 
     public void setValue(int value) {}
 
 
 
     public String getStatistic() {
        return statistic;
     }
 
 
 
     public void incrementStat(int i) {}
 
 
 
     public void decrementStat(int i) {}
 
 
 
     public String getCategory() {
        return category;
     }
 
 
 
     public void clearArchive() {}
 
 
 
     public boolean isArchive() {
         return archive;
     }
 
 
 
     public EntityStatBlob getOwner() {
         return owner;
     }
 
 
 
     public void setOwner(EntityStatBlob playerStatBlob) {
         owner = playerStatBlob;
     }
 
     public void archive() {
     }
     
     
     @Override
 	public void setDomain(String domain) {
 		this.domain = domain;
 	}
 
 
 
 	@Override
 	public String getDomain() {
 		// TODO Auto-generated method stub
 		return domain;
 	}
 
 
 
 	@Override
 	public void setWorld(String world) {
 		this.world = world;
 	}
 
 
 
 	@Override
 	public String getWorld() {
 		return world;
 	}
 	
 	public IStat clone(){
			return new StaticStat(domain,world,category,statistic,getValue());
 		
 	}
 }
