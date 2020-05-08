 package uk.ac.horizon.ug.exploding.engine;
 
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 
 import uk.ac.horizon.ug.exploding.db.Game;
 import uk.ac.horizon.ug.exploding.db.GameTime;
 import uk.ac.horizon.ug.exploding.db.Member;
 import uk.ac.horizon.ug.exploding.db.Message;
 import uk.ac.horizon.ug.exploding.db.Player;
 import uk.ac.horizon.ug.exploding.db.Position;
 import uk.ac.horizon.ug.exploding.db.TimelineEvent;
 import uk.ac.horizon.ug.exploding.db.Zone;
 
 import equip2.core.DataspaceObjectEvent;
 import equip2.core.DataspaceObjectsEvent;
 import equip2.core.IDataspace;
 import equip2.core.IDataspaceObjectsListener;
 import equip2.core.ISession;
 import equip2.core.QueryTemplate;
 import equip2.spring.db.IDAllocator;
 
 public class Engine
 {
 	static Logger logger = Logger.getLogger(Engine.class.getName());
 
 	protected Map<Integer, ZoneCache> zoneCache = new HashMap<Integer, ZoneCache>();
 	
 	protected Random random = new Random();
 	
 	// FIXME - persist this, sysvars
 	protected long lastEventCheckTime;
 	
 	protected IDataspace dataspace;
 
 	public void setDataspace(IDataspace dataspace)
 	{
 		this.dataspace = dataspace;
 	}
 
 	public IDataspace getDataspace()
 	{
 		return this.dataspace;
 	}
 	
 	// milliseconds to gametime ratio
 	protected float timeRatio;
 	
 	public void setTimeRatio(float timeRatio)
 	{
 		this.timeRatio = timeRatio;
 	}
 
 	public float getTimeRatio()
 	{
 		return this.timeRatio;
 	}
 	
 	// new member radius
 	protected double spawnRadius;
 	
 	public void setSpawnRadius(double spawnRadius)
 	{
 		this.spawnRadius = spawnRadius;
 	}
 
 	public double getSpawnRadius()
 	{
 		return this.spawnRadius;
 	}
 	
 	// proximity radius
 	protected double proximityRadius;
 	
 	public void setProximityRadius(double proximityRadius)
 	{
 		this.proximityRadius = proximityRadius;
 	}
 
 	public double getProximityRadius()
 	{
 		return this.proximityRadius;
 	}
 	
 	public Engine()
 	{
 		
 	}
 	
 	public void startup()
 	{
 		logger.info("engine startup");
 		
 		lastEventCheckTime = System.currentTimeMillis();
 		
 		// cache zones
 		ISession session = dataspace.getSession();
 		session.begin(ISession.READ_ONLY);
 		
 		Zone zone = new Zone();
 		
 		Object [] zones = session.match(zone);
 		
 		session.end();
 		
 		for(int i=0;i<zones.length;i++)
 		{
 			cacheZone((Zone)zones[i]);
 		}			
 
 		session = dataspace.getSession();		
 		session.getEventManagement().addIDataspaceObjectsListener(new ZoneListener(), zone);
 
 		// member events
 		Member member = new Member();
 		session = dataspace.getSession();
 		session.getEventManagement().addIDataspaceObjectsListener(new MemberListener(), member);
 	}
 	
 	class ZoneListener implements IDataspaceObjectsListener
 	{
 		ZoneListener(){}
 		
 		public void objectsChanged(DataspaceObjectsEvent dose)
 		{
 			logger.info("Engine::ZoneListener triggered");
 			
 			try
 			{
 				Enumeration changes = dose.getDataspaceObjectEvents();
 				
 				while(changes.hasMoreElements())
 				{
 					DataspaceObjectEvent doe = (DataspaceObjectEvent) changes.nextElement();
 					
 					if(doe.getRealChange() == DataspaceObjectEvent.OBJECT_ADDED
 							|| doe.getRealChange() == DataspaceObjectEvent.OBJECT_MODIFIED)
 					{
 						if(doe.getNewValue()!=null)
 						{
 							cacheZone((Zone)doe.getNewValue());
 						}
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				logger.error("Engine::ZoneListener error: " +e.toString(), new Throwable());
 			}
 		}			
 	}
 	
 	public void cacheZone(Zone zone)
 	{
 		logger.info("Engine::cacheZone triggered");
 		ZoneCache zc = new ZoneCache(zone);
 		zoneCache.put(zone.getOrgId(), zc);
 	}
 	
 	public Integer getZoneID(String contentGroupID, Position p)
 	{		
 		Iterator<Entry<Integer, ZoneCache>> it = zoneCache.entrySet().iterator();
 		
 		while(it.hasNext())
 		{
 			Map.Entry<Integer, ZoneCache> pairs = (Map.Entry<Integer, ZoneCache>)it.next();
 			
 			if(pairs.getValue().zone.getContentGroupID().equals(contentGroupID))
 			{
 				if(pairs.getValue().contains(p.getLatitude(), p.getLongitude()))
 				{
 					return pairs.getKey();
 				}
 			}
 	    }
 
 		return null;
 	}
 	
 	class MemberListener implements IDataspaceObjectsListener
 	{
 		MemberListener(){}
 		
 		public void objectsChanged(DataspaceObjectsEvent dose)
 		{
 			logger.info("Engine::MemberListener triggered");
 			
 			try
 			{
 				Enumeration changes = dose.getDataspaceObjectEvents();
 				
 				while(changes.hasMoreElements())
 				{
 					DataspaceObjectEvent doe = (DataspaceObjectEvent) changes.nextElement();
 					
 					if(doe.getRealChange() == DataspaceObjectEvent.OBJECT_ADDED)
 					{
 						if(doe.getNewValue()!=null)
 						{
 							// placed or carried?
 							Member member = (Member) doe.getNewValue();
 							
 							if(member.getCarried()==false)
 							{
 								// check zones, consequences
 								memberPlaced(member);
 							}
 						}
 					}
 					else if(doe.getRealChange() == DataspaceObjectEvent.OBJECT_MODIFIED)
 					{
 						if(doe.getNewValue()!=null && doe.getOldValue()!=null)
 						{
 							// carried to placed?
 							Member member = (Member) doe.getNewValue();
 							Member oldMember = (Member) doe.getOldValue();
 							
 							if(oldMember.getCarried()==true && member.getCarried()==false)
 							{
 								// check zones, consequences
 								memberPlaced(member);
 							}							
 						}
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				logger.error("Engine::MemberListener error: " +e.toString(), new Throwable());
 			}
 		}			
 	}	
 
 	public void checkEvents()
 	{		
 		// for each active game, check for events since last tick
 		
 		ISession session = dataspace.getSession();
 		session.begin(ISession.READ_WRITE);
 		
 		QueryTemplate gqt = new QueryTemplate(Game.class);
 		gqt.addConstraintEq("state", Game.ACTIVE);
 		
 		Object [] gs = session.match(gqt);
 		
 		long now = System.currentTimeMillis();
 		
 		for(int i=0; i<gs.length; i++)
 		{
 			Game g = (Game) gs[i];
 						
 			if(g.getContentGroupID()!=null && g.getContentGroupID().length()>0)
 			{
 				GameTime gt = (GameTime) session.get(GameTime.class, g.getGameTimeID());
 				
 				if(gt==null)
 				{
 					continue;
 				}
 				
 				// real time elapsed
 				long elapsed = now - this.lastEventCheckTime;
 				float currentGameTime = (elapsed * this.timeRatio) + gt.getGameTime();
 
 			   	QueryTemplate eq = new QueryTemplate(TimelineEvent.class);
 		     	eq.addConstraintGt("startTime", gt.getGameTime());
 		     	eq.addConstraintLe("startTime", currentGameTime);
 		    	eq.addOrder("startTime");
 		    	
 		    	Object [] es = session.match(eq);
 		    	
 		    	for(int j=0; j<es.length; j++)
 		    	{
 		    		TimelineEvent e = (TimelineEvent) es[j];
 		    		
		    		logger.info("found event: " + e.getName());
		    		
 		    		if(e.isSetTrack() && e.getTrack()==0)
 		    		{
 		    			// year event
 		    			g.setYear(e.getName());
 		    			session.update(g);
 		    		}
 		    		else
 		    		{
 		    			handleContentEvent(session, g, e);		    			
 		    		}
 		    	}
 		    	
		    	logger.info(gt.getGameTime() + " " + currentGameTime);
 		    	
 		    	gt.setGameTime(currentGameTime);
 			}
 		}
 
     	this.lastEventCheckTime = now;
 
 		session.end();
 	}
 	
 	
 	public void memberPlaced(Member member)
 	{
 		ISession session = dataspace.getSession();
 		session.begin(ISession.READ_WRITE);
 
 		// FIXME members entering zones with active duration events?
 		// FIXME there appear to be stat modifiers associated with Zones, this is not in the design doc.
 
 		Game game = (Game) session.get(Game.class, member.getGameID());
 		
 		if(game==null)
 		{
 			// FIXME error
 			session.end();
 			return;
 		}		
 		
 		// client is now setting zone
 		//member.setZone(this.getZoneID(game.getContentGroupID(), member.getPosition()));
 		
 	   	QueryTemplate mq = new QueryTemplate(Member.class);
 	   	mq.addConstraintEq("gameID", game.getID());
 	   	mq.addConstraintEq("carried", false);
 	   	
 	   	if(!member.isSetZone() || member.getZone()==0)
 	   	{
 	   		// they're in a "gap"
 	   		//	FIXME - does "assimilation" work across zone boundaries?
 	   		session.end();
 	   		return;
 	   	}
 
    		mq.addConstraintEq("zone", member.getZone());
 
 	   	Object [] ms = session.match(mq);
 	   	
 	   	if(ms.length==0)
 	   	{
 	   		// increase wealth
 	   		// FIXME - does "community member in a new region" mean they've not been here before?
    			int wealth = member.getWealth();
    			wealth += 3;
    			if(wealth>10)
    			{
    				wealth = 10;
    			}
    			else if(wealth<0)
    			{
    				wealth = 0;
    			}
    			member.setWealth(wealth);
 	   	}
 	   	else if(ms.length>=9) // excluding ourselves
 	   	{
 	   		// -2 health, +2 knowledge
 	   		// -2 wealth, +2 participation
 	   		// +2 participation
 	   		// FIXME this presumably *doesn't* mean +4 participation?!
 	   		
 	   		for(int i=0; i<ms.length; i++)
 	   		{
 	   			Member other = (Member) ms[i];
 
 	   			int health = other.getHealth();
 	   			health += -2;
 		   					   			
 		   		if(health>10)
 		   		{
 		   			health = 10;
 		   		}
 		   		else if(health<=0)
 		   		{
 			   		// initial member
 			   		if(other.getParentMemberID()==null || other.getParentMemberID().length()==0)
 			   		{
 			   			health = 1;
 			   		}
 			   		else
 			   		{
 		   				Message msg = new Message();
 		   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 		   				msg.setCreateTime(System.currentTimeMillis());
 		   				msg.setYear(game.getYear());
 		   				msg.setType(Message.MSG_MEMBER_DIED);
 		   				msg.setPlayerID(member.getPlayerID());
 		   				msg.setHandled(false);
 		   				msg.setTitle("Member died");
 		   				msg.setDescription(ContextMessages.MSG_DEATH);
 		   			
 		   				session.add(msg);
 			   			
 			   			session.remove(other);
 			   			continue;
 			   		}
 		   		}
 		   			
 		   		other.setHealth(health);		   			
 	   			
 	   			int wealth = other.getWealth();
 		   		wealth += -2;
 		   		if(wealth>10)
 		   		{
 		   			wealth = 10;
 		   		}
 		   		else if(wealth<0)
 		   		{
 		   			wealth = 0;
 		   		}
 		   		other.setWealth(wealth);
 		   		
 	   			int action = other.getAction();
 	   			action += 2;
 	   			if(action>10)
 	   			{
 	   				action = 10;
 	   			}
 	   			else if(action<0)
 	   			{
 	   				action = 0;
 	   			}
 	   			other.setAction(action);
 	   			
 	   			session.update(other);
 	   		}
 	   	}
 		
 		// proximity to others
 	   	
 	   	for(int i=0; i<ms.length; i++)
 	   	{
 	   		Member other = (Member) ms[i];
 	   		
 	   		if(other.getPlayerID().equals(member.getPlayerID()))
 	   		{
 	   			continue;
 	   		}
 	   		
 	   		if(ZoneCache.distanceBetweenPoints(member.getPosition(), other.getPosition())<proximityRadius)
 	   		{
 	   			if(member.getAction()>other.getAction())
 	   			{
 		   			// we assimilate them
 		   			int action = member.getAction();
 		   			action += 3;
 		   			if(action>10)
 		   			{
 		   				action = 10;
 		   			}
 		   			else if(action<0)
 		   			{
 		   				action = 0;
 		   			}
 		   			member.setAction(action);	
 		   			
 	   				// message them
 	   				Message msg = new Message();
 	   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 	   				msg.setCreateTime(System.currentTimeMillis());
 	   				msg.setYear(game.getYear());
 	   				msg.setType(Message.MSG_MEMBER_ASSIMILATED);
 	   				msg.setPlayerID(other.getPlayerID());
 	   				msg.setHandled(false);
 	   				msg.setTitle("Lost a member");
 	   				msg.setDescription(ContextMessages.MSG_ASSIMILATED);
 
 	   				session.add(msg);
 	   				
 	   				// message us
 	   				msg = new Message();
 	   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 	   				msg.setCreateTime(System.currentTimeMillis());
 	   				msg.setYear(game.getYear());
 	   				msg.setType(Message.MSG_MEMBER_ASSIMILATED_OTHER);
 	   				msg.setPlayerID(member.getPlayerID());
 	   				msg.setHandled(false);
 	   				msg.setTitle("New member");
 	   				msg.setDescription(ContextMessages.MSG_ASSIMILATE);
 	   			
 	   				session.add(msg);
 	   				
 	   				// assimilate
 		   			other.setPlayerID(member.getPlayerID());
 		   			other.setParentMemberID(member.getID());
 	   				session.update(other);
 	   				
 	   			}
 	   			else if(member.getAction()==other.getAction())
 	   			{
 	   				// do nothing, we are equal
 	   				continue;
 	   			}
 	   			else
 	   			{
 	   				// they assimilate us
 		   			int action = other.getAction();
 		   			action += 3;
 		   			if(action>10)
 		   			{
 		   				action = 10;
 		   			}
 		   			else if(action<0)
 		   			{
 		   				action = 0;
 		   			}
 		   			other.setAction(action);
 		   			session.update(other);
 	   				
 		   			// message us
 	   				Message msg = new Message();
 	   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 	   				msg.setCreateTime(System.currentTimeMillis());
 	   				msg.setYear(game.getYear());
 	   				msg.setType(Message.MSG_MEMBER_ASSIMILATED);
 	   				msg.setPlayerID(member.getPlayerID());
 	   				msg.setHandled(false);
 	   				msg.setTitle("Lost community member");
 	   				msg.setDescription(ContextMessages.MSG_ASSIMILATED);
 	   			
 	   				session.add(msg);
 	   				
 	   				// message them
 	   				msg = new Message();
 	   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 	   				msg.setCreateTime(System.currentTimeMillis());
 	   				msg.setYear(game.getYear());
 	   				msg.setType(Message.MSG_MEMBER_ASSIMILATED_OTHER);
 	   				msg.setPlayerID(member.getPlayerID());
 	   				msg.setHandled(false);
 	   				msg.setTitle("Lost community member");
 	   				msg.setDescription(ContextMessages.MSG_ASSIMILATE);
 	   			
 	   				session.add(msg);
 	   				
 	   				// assimilate	   				
 		   			member.setPlayerID(other.getPlayerID());
 		   			member.setParentMemberID(other.getID());
 	   				session.update(member);
 	   			}
 	   		}
 	   	}
 	   	
 	   	// finally, does this mean anyone can author ?
 	   	this.updateAuthorable(session, game);	   		
 		
 		session.end();
 	}
 	
 	// FIXME - I don't understand *Player* participation points yet
 	public void updateAuthorable(ISession session, Game game)
 	{
 	   	// has this changed a player's ability to author events?
 	   	QueryTemplate pqt = new QueryTemplate(Player.class);
 	   	pqt.addConstraintEq("gameID", game.getID());
 	   	
 	   	Object [] ps = session.match(pqt);
 	   	
 	   	for(int i=0; i<ps.length; i++)
 	   	{
 	   		Player p = (Player) ps[i];
 	   		
 	   		QueryTemplate mqt = new QueryTemplate(Member.class);
 	   		mqt.addConstraintEq("playerID", p.getID());
 	   		mqt.addConstraintGe("brains", 8);
 	   		
 	   		int count = session.count(mqt);
 	   		
 	   		// FIXME - notify client of change
 	   		if(count>0 && p.getCanAuthor()==false)
 	   		{
 	   			p.setCanAuthor(true);
 	   			session.update(p);
 	   		}
 	   		else if(count==0 && p.getCanAuthor()==true)
 	   		{
 	   			p.setCanAuthor(false);
 	   			session.update(p);
 	   		}
 	   	}
 	}
 
 	
 	public void handleContentEvent(ISession session, Game game, TimelineEvent event)
 	{		
 	   	QueryTemplate mq = new QueryTemplate(Member.class);
 	   	mq.addConstraintEq("gameID", game.getID());
 	   	mq.addConstraintEq("carried", false);
 	   	
 	   	if(event.getZoneId()!=0)
 	   	{
 	   		mq.addConstraintEq("zone", event.getZoneId());
 	   	}
 	   	
 	   	Object [] ms = session.match(mq);
 	   	
 	   	Set<String> playerIDs = new HashSet<String>();
 	   	
 	   	for(int i=0; i<ms.length; i++)
 	   	{
 	   		Member member = (Member) ms[i];
 	   		
 	   		// this *player* needs to be notified
 	   		playerIDs.add(member.getPlayerID());
 	   		
 	   		if(event.getAbsolute()==1)
 	   		{
 	   			// FIXME absolute events?!   			
 	   		}
 	   		else
 	   		{
 	   			// FIXME min/max ranges?!
 	   			// - there are none in gameState.xml still
 	   			
 	   			if(event.getHealth()!=0)
 	   			{
 		   			int health = member.getHealth();
 		   			health += event.getHealth();
 		   					   			
 		   			if(health>10)
 		   			{
 		   				health = 10;
 		   			}
 		   			else if(health<=0)
 		   			{
 			   			// initial member
 			   			if(member.getParentMemberID()==null || member.getParentMemberID().length()==0)
 			   			{
 			   				health = 1;
 			   			}
 			   			else
 			   			{
 			   				Message msg = new Message();
 			   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 			   				msg.setCreateTime(System.currentTimeMillis());
 			   				msg.setYear(game.getYear());
 			   				msg.setType(Message.MSG_MEMBER_DIED);
 			   				msg.setPlayerID(member.getPlayerID());
 			   				msg.setHandled(false);
 			   				msg.setTitle(event.getName());
 			   				msg.setDescription(ContextMessages.MSG_DEATH);
 			   				
 			   				session.add(msg);
 			   				
 			   				session.remove(member);
 			   				continue;
 			   			}
 		   			}
 		   			
 		   			member.setHealth(health);		   			
 	   			}
 	   			
 	   			if(event.getWealth()!=0)
 	   			{
 		   			int wealth = member.getWealth();
 		   			wealth += event.getWealth();
 		   			if(wealth>10)
 		   			{
 		   				wealth = 10;
 		   			}
 		   			else if(wealth<0)
 		   			{
 		   				wealth = 0;
 		   			}
 		   			member.setWealth(wealth);		   			
 	   			}
 	   			
 	   			if(event.getAction()!=0)
 	   			{
 		   			int action = member.getAction();
 		   			action += event.getAction();
 		   			if(action>10)
 		   			{
 		   				action = 10;
 		   			}
 		   			else if(action<0)
 		   			{
 		   				action = 0;
 		   			}
 		   			member.setAction(action);		   			
 	   			}
 	   			
 	   			if(event.getBrains()!=0)
 	   			{
 		   			int brains = member.getBrains();
 		   			brains += event.getBrains();
 		   			if(brains>10)
 		   			{
 		   				brains = 10;
 		   			}
 		   			else if(brains<0)
 		   			{
 		   				brains = 0;
 		   			}
 		   			member.setBrains(brains);		   			
 	   			}
 	   			
 	   			// create offspring
 	   			if(member.getHealth()>=8)
 	   			{
 	   				if(member.getWealth()>=8)
 	   				{
 	   					// FIXME reduce wealth too?!
 	   					member.setWealth(4);
 		   				member.setHealth(4);
 		   				
 		   				Player p = (Player) session.get(Player.class, member.getPlayerID());
 		   				
 		   				if(p!=null)
 		   				{
 		   					// player notified by Player object change
 		   					p.setNewMemberQuota(p.getNewMemberQuota()+1);
 		   				}
 		   				
 	   					continue;
 	   				}
 	   				else
 	   				{	   			
 		   				member.setHealth(4);
 		   					
 		   				Member offspring = new Member();
 		   				offspring.setID(IDAllocator.getNewID(session, Member.class, "M", null));
 		   				offspring.setCarried(false);
 		   				offspring.setGameID(member.getGameID());
 		   				offspring.setPlayerID(member.getPlayerID());
 		   				offspring.setParentMemberID(member.getID());
 		   				offspring.setHealth(4);
 		   				offspring.setColourRef(member.getColourRef());
 		   				
 		   				// FIXME - server generated offspring names, how are they generated?
 		   				offspring.setName(member.getName() + random.nextInt(1000));
 		   				
 		   				offspring.setWealth(member.getWealth() + ((random.nextInt(2)>0) ? -1 : 1));
 		   				if(offspring.getWealth()>10)
 			   			{
 			   				offspring.setWealth(10);
 			   			}
 			   			else if(offspring.getWealth()<0)
 			   			{
 			   				offspring.setWealth(0);
 			   			}
 		   				
 		   				offspring.setBrains(member.getBrains() + ((random.nextInt(2)>0) ? -1 : 1));
 		   				if(offspring.getBrains()>10)
 			   			{
 			   				offspring.setBrains(10);
 			   			}
 			   			else if(offspring.getBrains()<0)
 			   			{
 			   				offspring.setBrains(0);
 			   			}
 	
 		   				offspring.setAction(member.getAction() + ((random.nextInt(2)>0) ? -1 : 1));
 		   				if(offspring.getAction()>10)
 			   			{
 			   				offspring.setAction(10);
 			   			}
 			   			else if(offspring.getAction()<0)
 			   			{
 			   				offspring.setAction(0);
 			   			}
 
 		   				// FIXME - should be metres rather than degrees
 		   				double xoffset = (random.nextDouble()*spawnRadius*2)-spawnRadius;
 		   				double yoffset = (random.nextDouble()*spawnRadius*2)-spawnRadius;
 		   				
 		   				Position p = new Position();
 		   				p.setElevation(0.0);
 		   				p.setLongitude(member.getPosition().getLongitude()+xoffset);
 		   				p.setLatitude(member.getPosition().getLatitude()+yoffset);
 		   				
 		   				offspring.setPosition(p);
 		   				
 		   				// still need server zones for this?
 		   				offspring.setZone(this.getZoneID(game.getContentGroupID(), p));
 		   				
 		   				session.add(offspring);
 		   				
 		   				Message msg = new Message();
 		   				msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 		   				msg.setCreateTime(System.currentTimeMillis());
 		   				msg.setYear(game.getYear());
 		   				msg.setType(Message.MSG_MEMBER_CREATED);
 		   				msg.setPlayerID(member.getPlayerID());
 		   				msg.setHandled(false);
 		   				msg.setTitle("New member");
 		   				
 		   				if(offspring.getZone()!=0)
 		   				{
 			   				QueryTemplate zqt = new QueryTemplate(Zone.class);
 			   				zqt.addConstraintEq("orgId", offspring.getZone());
 			   				
 			   				Object [] zs = session.match(zqt);
 			   				
 			   				if(zs.length==1)
 			   				{
 			   					Zone z = (Zone) zs[0];
 				   				msg.setDescription(ContextMessages.MSG_BIRTH.replace("<zone>", z.getName()));
 			   				}
 			   				else
 			   				{
 				   				msg.setDescription(ContextMessages.MSG_BIRTH.replace("<zone>", "your community"));	
 			   				}
 		   				}
 		   				else
 		   				{
 			   				msg.setDescription(ContextMessages.MSG_BIRTH.replace("<zone>", "your community"));
 		   				}	   				
 		   			
 		   				session.add(msg);
 	   				}
 	   			}
 	   			
 	   			session.update(member);
 	   		}
 	   	}
 	   	
 	   	Message msg = new Message();
 	   	msg.setCreateTime(System.currentTimeMillis());
 	   	msg.setYear(game.getYear());
 	   	msg.setType(Message.MSG_TIMELINE_CONTENT);
 	   	msg.setTitle(event.getName());
 	   	msg.setDescription(event.getDescription());
 	   	msg.setHandled(false);
 		
 	   	Iterator<String> it = playerIDs.iterator();
 	   	
 	   	while(it.hasNext())
 	   	{
 		   	msg.setID(IDAllocator.getNewID(session, Message.class, "MSG", null));
 		   	msg.setPlayerID(it.next());
 			session.add(msg);
 	   	}
 	   		
 	   	this.updateAuthorable(session, game);
 	}
 }
