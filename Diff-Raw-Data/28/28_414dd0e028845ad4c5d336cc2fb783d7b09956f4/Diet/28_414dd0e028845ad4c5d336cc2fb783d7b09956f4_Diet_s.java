 package com.moulis.eleonore.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import com.moulis.eleonore.exceptions.InternalErrorException;
 import com.moulis.eleonore.xml.EleonoreXmlTags;
 
 /**
  *   Copyright (C) 2012  MOULIS Marius <moulis.marius@gmail.com>
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class Diet implements Serializable {
 	
 	private static final long serialVersionUID = 5280934763008571794L;
 	
 	private DietObjective objective;	
 	private List<DietStep> diet;
 	
 	public Diet(DietObjective objective)
 	{
 		this.objective = objective;
 		diet = new ArrayList<DietStep>();
 	}
 	
 	public Diet(double objective)
 	{
 		this.objective = new DietObjective(objective);
 		diet = new ArrayList<DietStep>();
 	}
 	
 	public double getObjective()
 	{
 		return objective.getObjective();
 	}
 	
 	public void addStep(DietStep step)
 	{
 		diet.add(step);
 	}
 	
 	public void addStep(double weight, Date date)
 	{
 		int stepId = diet.size();
 		diet.add(new DietStep(weight, date, stepId));
 	}
 	
 	public DietStep getLastDietStep() throws InternalErrorException
 	{
 		DietStep lastDietStep = null;
 		try 
 		{
 			lastDietStep = diet.get(diet.size() - 1);
 		}
 		catch (IndexOutOfBoundsException e) 
 		{
 			throw new InternalErrorException();
 		}
 		catch (NullPointerException e) 
 		{
 			throw new InternalErrorException();
 		}
 		
 		return lastDietStep;
 
 	}
 	
 	public DietStep getFirstDietStep() throws InternalErrorException
 	{
 		DietStep firstDietStep = null;
 		try 
 		{
 			firstDietStep = diet.get(0);
 		}
 		catch (IndexOutOfBoundsException e) 
 		{
 			throw new InternalErrorException();
 		}
 		catch (NullPointerException e) 
 		{
 			throw new InternalErrorException();
 		}
 		
 		return firstDietStep;		
 	}
 	
 	public double getTotalWeightLost()
 	{
 		double total = 0.0;
 		int numberOfSteps = diet.size();
 		if (numberOfSteps > 1)
 		{
 			for (int i = 0 ; i < numberOfSteps-1; i++)
 			{
				double delta = diet.get(i).getWeight() + diet.get(i+1).getWeight();
				total += delta;
 			}
 		}
 			
 		return total;
 	}
 	
 	/**
 	 * Return the {@link Diet} duration in terms of days
 	 * @throws InternalErrorException 
 	 * */
 	public long getDietDuration() throws InternalErrorException
 	{
 		long firstDate = getFirstDietStep().getDate().getTime();
 		long lastDate = getLastDietStep().getDate().getTime();
 		
 		long durationMilliSeconds = firstDate - lastDate;
 		long durationSeconds = durationMilliSeconds / 1000;
 		long durationHour = durationSeconds / 3600;
		long durationDay = durationHour / 24;
 		
		if (durationDay < 1)
 			durationDay = 1;
 		
 		return durationDay;
 	}
 	
 	public List<DietStep> getDiet()
 	{
 		return diet;
 	}
 	
 	public String toXml()
 	{
 		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n\n";
 		xml += "<!-- Do not modify this file! -->\n\n";
 		
 		xml += EleonoreXmlTags.ROOT_TAG_COMPLETE_OPEN;
 		xml += objective.toXml();
 		
 		for (DietStep step : diet)
 		{
 			xml += step.toXml();
 		}
 		
 		
 		xml += "</diet>\n";
 		
 		
 		return xml;
 	}
 
 }
