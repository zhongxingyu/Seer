 package com.moulis.eleonore.controllers;
 
 import android.content.Intent;
 
 import com.moulis.eleonore.EleonoreConfig;
 import com.moulis.eleonore.activities.EleonoreHistoryActivity;
 import com.moulis.eleonore.activities.EleonoreSeeStatisticsActivity;
 import com.moulis.eleonore.exceptions.InternalErrorException;
 import com.moulis.eleonore.model.Diet;
 import com.moulis.eleonore.units.EleonoreCurrentUnit;
 import com.moulis.eleonore.units.EleonoreUnit;
 import com.moulis.eleonore.units.EleonoreUnitsConverter;
 
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
 
 public class EleonoreStatisticsController extends AbstractEleonoreController {
 	
 	private final Diet diet;
 	private final EleonoreSeeStatisticsActivity activity;
 	private final EleonoreUnit currentUnit = EleonoreCurrentUnit.getCurrentUnit();
 	private final EleonoreUnitsConverter converter = EleonoreUnitsConverter.INSTANCE;
 	
 	public EleonoreStatisticsController(Diet diet, EleonoreSeeStatisticsActivity activity)
 	{
 		this.diet = diet;
 		this.activity = activity;
 	}
 	
 	public void backToMainActivity()
 	{
 		activity.finish();
 	}
 	
 	public Diet getDiet()
 	{
 		return diet;
 	}
 	
 	public EleonoreSeeStatisticsActivity getActivity()
 	{
 		return activity;
 	}
 	
 	public String getObjectiveAsText()
 	{
 		double dietObjective = diet.getObjective();
 		double convertedDietObjective = converter.convertFromKilograms(currentUnit, dietObjective);
 		return converter.formatValue(convertedDietObjective) + " " +  currentUnit;
 	}
 	
 	public String getActualWeightAsText()
 	{
 		String actualWeightAsAString = "";
 		
 		try 
 		{
 			double actualWeight = getActualWeight();
 			double convertedActuelWeight = converter.convertFromKilograms(currentUnit, actualWeight);
 			actualWeightAsAString = converter.formatValue(convertedActuelWeight) + " " + currentUnit;
 		} 
 		catch (InternalErrorException e) 
 		{
 			actualWeightAsAString = EleonoreConfig.ELEONORE_NA_STRING;
 		}
 
 		return actualWeightAsAString;
 	}
 	
 	public String getLostWeightAsAText()
 	{
 		String lostWeightAsAString = "";
 		
 		try 
 		{
 			double firstWeight = diet.getFirstDietStep().getWeight();
 			double actualWeight = getActualWeight();
 			double lostWeight = firstWeight - actualWeight;
 			double convertedLostWeight = converter.convertFromKilograms(currentUnit, lostWeight);
 			lostWeightAsAString = converter.formatValue(convertedLostWeight) + " " + currentUnit;
 			if (lostWeight < 0 )
 			{
 				lostWeightAsAString = lostWeightAsAString.replace("-", "+");
 			}			
 		} 
 		catch (InternalErrorException e) 
 		{
 			lostWeightAsAString = EleonoreConfig.ELEONORE_NA_STRING;
 		}
  
 		
 		return lostWeightAsAString;
 	}
 	
 	public String getAverageLostWeightAsAText()
 	{
 		String averageAsAString = "";
 
 		try 
 		{
 			double totalLostWeight = diet.getTotalWeightLost();
 			double numberOfDays = diet.getDietDuration();
			double convertedTotalLostWeight = converter.convertFromKilograms(currentUnit, totalLostWeight);
			averageAsAString = converter.formatValue(convertedTotalLostWeight / numberOfDays) + " " + currentUnit;
 		}
 		catch (InternalErrorException e)
 		{
 			averageAsAString = EleonoreConfig.ELEONORE_NA_STRING;
 		}
 		
 		return averageAsAString;
 	}
 	
 	public String getObjectiveRemaining()
 	{
 		
 		String objectiveRemainingAsAString = "";
 		
 		try 
 		{
 			double actualWeight = getActualWeight();
 			double objective = diet.getObjective();
 			double objectiveRemaining = actualWeight - objective;
 			double convertedObjectiveRemaining = converter.convertFromKilograms(currentUnit, objectiveRemaining);
			objectiveRemainingAsAString = converter.formatValue(convertedObjectiveRemaining) + " " + currentUnit;
 		} 
 		catch (InternalErrorException e) 
 		{
 			objectiveRemainingAsAString = EleonoreConfig.ELEONORE_NA_STRING;
 		}
 		
 		return objectiveRemainingAsAString;
 
 	}
 	
 	public String getObjectiveDoneRateAsAText()
 	{
 		String objectiveDoneRateAsAString = "";
 		try
 		{
 			double objectiveDoneRate = getObjectiveDoneRate();
 			objectiveDoneRateAsAString = converter.formatValue(objectiveDoneRate) + " %";
 		}
 		catch (InternalErrorException e) 
 		{
 			objectiveDoneRateAsAString = EleonoreConfig.ELEONORE_NA_STRING;
 		}
 		
 		return objectiveDoneRateAsAString;
 	}
 	
 	public double getObjectiveDoneRate() throws InternalErrorException
 	{
 		double actualWeight = getActualWeight();
		double firstWeight = diet.getLastDietStep().getWeight();
 		double objective = diet.getObjective();
 		double objectiveDoneRate = (firstWeight - actualWeight)/(firstWeight - objective) * 100;
 		return objectiveDoneRate;
 	}
 	
 	private double getActualWeight() throws InternalErrorException
 	{
 		return diet.getLastDietStep().getWeight();
 	}
 	
 	public void launchHistoryActivity()
 	{
 		Intent intent = new Intent();
 		intent.putExtra(EleonoreMainController.DIET_STATISTICS_DATA, diet);
 		intent.setClass(activity, EleonoreHistoryActivity.class);
 		activity.startActivity(intent);
 	}
 
 }
