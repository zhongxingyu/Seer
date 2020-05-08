 package hci.polygon;
 
 import hci.util.Point;
 
 import java.util.*;
 
 public class PolygonManager {
 	
 	private ArrayList<TaggedPolygon> polygons = new ArrayList<TaggedPolygon>();
 	
 	public void loadPolygons(int hash)
 	{
 		
 		
 		
 	}
 	
 	public void savePolygons()
 	{
 		
 	}
 	
 	public void highlightPoint(Point p)
 	{
 		
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			if (polygons.get(i).isEditing())
 			{
 				polygons.get(i).selectPoint(p);
 			}
 		}
 		
 	}
 	
 	public void resetPointHighlight()
 	{
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			polygons.get(i).clearSelectedPoint();
 		}
 	}
 	
 	public boolean updatePoint(Point p)
 	{
 		
 		// if highlighted point, update it
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			if (polygons.get(i).getSelectedPointIdx() != -1)
 			{
 				polygons.get(i).updateSelectedPoint(p);
 				return true;
 			}
 		}
 		
 		return false;
 		
 	}
 	
 	public void finishPolygon()
 	{
 		
 		if (polygons.size() == 0)
 			return;
 		
 		TaggedPolygon p = polygons.get(polygons.size() - 1);
 		
 		if (p.size() < 3)
 		{
 			polygons.remove(polygons.size() - 1);
 			return;
 		}
 		
		// finish all editing
		for (int i = 0; i < polygons.size(); i++)
		{
			polygons.get(i).complete();
		}
 		
 	}
 	
 	public void resetHighlights()
 	{
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			polygons.get(i).resetHighlight();
 		}
 	}
 	
 	public boolean updateHighlights(Point p)
 	{
 		
 		boolean highlightedPolygon = false,
 				repaint = false;
 		
 		for (int i = polygons.size()-1; i >= 0; i--)
 		{
 			
 			if (!highlightedPolygon)
 			{
 				if (polygons.get(i).updateHighlight(p))
 				{
 					repaint = true;
 				}
 				
 				if (polygons.get(i).isHighlighted())
 				{
 					highlightedPolygon = true;
 				}
 			}
 			else
 			{
 				if (polygons.get(i).resetHighlight())
 				{
 					repaint = true;
 				}
 			}
 			
 		}
 		
 		return repaint;
 		
 	}
 	
 	public boolean removeHighlights()
 	{
 		
 		boolean repaint = false;
 
 		for (int i = polygons.size()-1; i >= 0; i--)
 		{
 			
 			if (polygons.get(i).resetHighlight())
 			{
 				repaint = true;
 			}
 			
 		}
 		
 		return repaint;
 		
 	}
 	
 	public boolean editHighlighted()
 	{
 		
 		boolean repaint = false;
 		
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			
 			if (polygons.get(i).isHighlighted())
 			{
 				polygons.get(i).edit();
 				repaint = true;
 				break;
 			}
 			
 		}
 		
 		return repaint;
 		
 	}
 	
 	public boolean removeHighlighted()
 	{
 		boolean repaint = false;
 		Iterator<TaggedPolygon> i = polygons.iterator();
 		while (i.hasNext())
 		{
 			TaggedPolygon p = i.next();
 			
 			if (p.isHighlighted())
 			{
 				i.remove();
 				repaint = true;
 			}
 		}
 		
 		return repaint;
 	}
 	
 	public boolean openPolygon()
 	{
 		if (polygons.size() == 0)
 		{
 			return false;
 		}
 		
 		return !polygons.get(polygons.size() - 1).isComplete() || polygons.get(polygons.size() - 1).isEditing();
 	}
 	
 	public boolean isEditing()
 	{
 		if (polygons.size() == 0)
 		{
 			return false;
 		}
 		
		for (int i = 0; i < polygons.size(); i++)
		{
			if (polygons.get(i).isEditing())
			{
				return true;
			}
		}
		
		return false;
 	}
 	
 	public void addNewPoint(Point p)
 	{
 		
 		// if last polygon is complete, start new one
 		TaggedPolygon polygon;
 		if (polygons.size() > 0)
 		{
 			polygon = polygons.get(polygons.size()-1);
 		}
 		else
 		{
 			polygon = new TaggedPolygon();
 			polygons.add(polygon);
 		}
 		
 		if (polygon.isComplete() && !polygon.isEditing())
 		{
 			polygon = new TaggedPolygon();
 			polygons.add(polygon);
 		}
 		
 		polygon.addPoint(p);
 		
 	}
 	
 	public TaggedPolygon[] getPolygons()
 	{
 		TaggedPolygon[] pArr = new TaggedPolygon[polygons.size()];
 		
 		for (int i = 0; i < polygons.size(); i++)
 		{
 			pArr[i] = polygons.get(i);
 		}
 		
 		return pArr;
 	}
 	
 	public boolean removeLastPoint()
 	{
 		
 		if (polygons.size() == 0)
 			return false;
 		
 		if (openPolygon())
 		{
 			
 			if (polygons.get(polygons.size()-1).removeLastPoint())
 			{
 				if (polygons.get(polygons.size() - 1).size() == 0)
 				{
 					polygons.remove(polygons.size() - 1);
 				}
 				return true;
 			}
 			
 		}
 		
 		return false;
 		
 	}
 	
 	public String toString()
 	{
 		String s = "[PolygonManager";
 		
 		if (polygons.size() == 0)
 		{
 			return s + "]";
 		}
 		
 		s = s + "\n" + polygons.get(0).toString();
 		
 		for (int i = 1; i < polygons.size(); i++)
 		{
 			s = s + ", " + polygons.get(i).toString();
 		}
 		
 		return s + "]";
 	}
 	
 }
