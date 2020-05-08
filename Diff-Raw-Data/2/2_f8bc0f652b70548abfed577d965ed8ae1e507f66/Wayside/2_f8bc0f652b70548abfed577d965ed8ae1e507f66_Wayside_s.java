 package wayside;
 
 import java.util.*;
 import java.util.logging.*;
 import global.*;
 import trackmodel.*;
 import trainmodel.*;
 
 public abstract class Wayside implements WaysideInterface, Runnable
 {
 	final static Logger log = Logger.getLogger(Wayside.class.getName());
 	
 	protected List<Track> track;
 	protected ID id;
 	
 	protected boolean direction;
 	
 	protected Wayside nextL;
 	protected Wayside nextR;
 	protected Wayside prevL;
 	protected Wayside prevR;
 	
 	public Wayside(ID waysideID)
 	{
 		id = waysideID;
 		track = new ArrayList<Track>();
 		direction = true;
 		log.config(logPrefix() + "Created");
 	}
 	
 	public void run()
 	{
 		if(hasTrain())
 		{
 			runLogic();
 		}
 	}
 	
 	/*
 	 * Runs the logic for its track section which is assumed to have a train.
 	 */
 	abstract void runLogic();
 	
 	public void setAuthority(ID trackID, int auth)
 	{
 		/* Possible performance improvement if tracks are also hashed by ID */
 		Track t;
 		if (auth < 0)
 		{
 			log.warning(logPrefix() + "Invalid authority " + auth);
 			return;
 		}
 		if ((t = findTrack(trackID)) == null)
 		{
 			return;
 		}
 		t.setAuthority(auth);
 	}
 	
 	public void setDispatchLimit(ID trackID, int speed)
 	{
 		/* Possible performance improvement if tracks are also hashed by ID */
 		Track t;
 		if (speed < 0)
 		{
 			log.warning(logPrefix() + "Invalid speed " + speed);
 			return;
 		}
 		if ((t = findTrack(trackID)) == null)
 		{
 			return;
 		}
 		int limit = (t.getInherentSpeedLimit() < speed) ? t.getInherentSpeedLimit() : speed;
 		t.setDispatchLimit(limit);
 	}
 	
 	public Track findTrack(ID trackID)
 	{
 		for (Track t : track)
 		{
 			if (t.getID().equals(trackID))
 			{
 				return t;
 			}
 		}
 		log.warning(logPrefix() + "Cannot find track " + trackID);
 		return null;
 	}
 
 	public boolean hasTrain()
 	{
 		for (Track t : track)
 		{
 			if (t.isOccupied())
 			{
 				boolean dir = t.getDirection();
 				if (direction != dir)
 				{
 					direction = dir;
 					log.fine(logPrefix() + "Direction changed to " + direction);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean clearToReceiveFrom(Wayside w)
 	{
 		if (!hasTrain())
 		{
 			return true;
 		}
 		if (nextLeft().equals(w) || nextRight().equals(w))
 		{
 			return false;
 		}
 		if (!track.get(trackStart()).isOccupied())
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	protected void spreadAuthority(int auth)
 	{
		int dir = direction ? -1 : 1;
 		for (int i = trackStart(); i < trackEnd(); i += dir)
 		{
 			track.get(i).setAuthority(auth);
 			auth++;
 			/* Make sure a possible second train only has
 			 * authority up to the first train */
 			if (track.get(i).isOccupied())
 			{
 				auth = -1;
 			}
 		}
 	}
 	
 	protected Wayside nextLeft()
 	{
 		return (direction ? nextL : prevL);
 	}
 	
 	protected Wayside nextRight()
 	{
 		return (direction ? nextR : prevR);
 	}
 	
 	protected int trackStart()
 	{
 		return direction ? 0 : track.size();
 	}
 	
 	protected int trackEnd()
 	{
 		return direction ? track.size() : 0;
 	}
 	
 	public void addTrack(Track t)
 	{
 		track.add(t);
 		log.config(logPrefix() + "Track " + t.getID() + " added");
 	}
         
 	public ArrayList <Track> getTrackBlocks()
 	{
 		return (ArrayList)track;
 	}
 	
 	public void setWaysideNextLeft(Wayside w)
 	{
 		nextL = w;
 	}
 	
 	public void setWaysideNextRight(Wayside w)
 	{
 		nextR = w;
 	}
 	
 	public void setWaysidePrevLeft(Wayside w)
 	{
 		prevL = w;
 	}
 	
 	public void setWaysidePrevRight(Wayside w)
 	{
 		prevR = w;
 	}
 	
 	public ID getID()
 	{
 		return id;
 	}
 
 	public boolean equals(Wayside w)
 	{
 		return id.equals(w.getID());
 	}
 
 	public String toString()
 	{
 		return id.toString();
 	
 	}
 	
 	protected String logPrefix()
 	{
 		return "Wayside " + id.toString() + ": ";
 	}
 }
