 package com.cheesymountain.woe;
 
 /*=============================================================
  * Copyright 2012, Cheesy Mountain Production
  * 
  * This file is part of World of Everbies.
  * 
  * World of Everbies is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * World of Everbies is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with World of Everbies.  If not, see <http://www.gnu.org/licenses/>.
  ================================================================*/
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.LinkedList;
 import android.text.format.*;
 
 import com.cheesymountain.woe.food.Food;
 import com.cheesymountain.woe.interact.Interaction;
 import com.cheesymountain.woe.item.Item;
 import com.cheesymountain.woe.training.Training;
 import com.cheesymountain.woe.util.Occupationable;
 import com.cheesymountain.woe.work.Work;
 
 /**
  * Contains and creates a logged string with information about what the Everbie
  * is up to.
  * 
  * @author Cheesy Mountain
  * 
  */
 public class Log {
 
 	private static Log log;
 	LinkedList<String> logList;
 	Time time = new Time();
 
 	private Log() {
 		logList = new LinkedList<String>();
 		time.set(System.currentTimeMillis());
 	}
 
 	/**
 	 * Returns a pointer that references to the Log if one exists otherwise
 	 * creates a new one
 	 * 
 	 * @return a pointer to the log
 	 */
 	public synchronized static Log getLog() {
 		if (log == null) {
 			log = new Log();
 		}
 		return log;
 	}
 
 	/**
 	 * Returns a plain String containing the text that will be displayed
 	 * 
 	 * @return a long text
 	 */
 	public String getLogList() {
 		String logString = "";
 		String[] entry = logList.toArray(new String[logList.size()]);
 		for (int i = 0; i < logList.size(); i++) {
 			logString += entry[i];
 		}
 		return logString;
 
 	}
 
 	/**
 	 * Is called when the Everbie is busy when the owner has tried to use some
 	 * other activity and the Log should be configured correctly.
 	 */
 	public void isBusy() {
 		String time = (Everbie.getEverbie().getOccupiedHours() == 0 ? ""
 				: Everbie.getEverbie().getOccupiedHours() + "h ")
 				+ (Everbie.getEverbie().getOccupiedMinutes() - Everbie
 						.getEverbie().getOccupiedHours() * 60)
 				+ "m "
 				+ (Everbie.getEverbie().getOccupiedSeconds() - Everbie
 						.getEverbie().getOccupiedMinutes() * 60) + "s.";
 		firstRemoval();
 		logList.addLast("\n" + Everbie.getEverbie().getName()
 				+ " is busy at the moment but will be ready in " + time);
 	}
 
 	/**
 	 * Is called when the Everbie is fainted when the owner has tried to use
 	 * some other activity and the Log should be configured correctly.
 	 */
 	public void isFainted() {
 		String time = ((Everbie.getEverbie().getFaintedTime() / 60) == 0 ? ""
 				: Everbie.getEverbie().getFaintedTime() / 60 + "h ")
 				+ (Everbie.getEverbie().getFaintedTime() % 60) + "m ";
 		firstRemoval();
 		logList.addLast("\n" + Everbie.getEverbie().getName()
 				+ " is fainted at the moment but will wake up in maybe " + time);
 	}
 	
 	//Javadoc to come
 	public void isDead(){
 		firstRemoval();
 		logList.addLast("\n" + Everbie.getEverbie().getName() + " has unfortunately passed away.");
 	}
 
 	/**
 	 * Is called when training is started to the Everbie to configure the log
 	 * correctly
 	 * 
 	 * @param train
 	 *            - the type of training started
 	 */
 	public void started(Training train) {
 		firstRemoval();
 		logList.addLast("\n" + getTime() + Everbie.getEverbie().getName()
 				+ " began to workout by starting with " + train.getName());
 	}
 
 	/**
 	 * Is called when work is started by the Everbie to configure the log
 	 * correctly
 	 * 
 	 * @param work
 	 *            - the kind of work started
 	 */
 	public void started(Work work) {
 		firstRemoval();
 		logList.addLast("\n" + getTime() + Everbie.getEverbie().getName()
 				+ " started to work with " + work.getName());
 	}
 
 	/**
 	 * Is called when food is given to the Everbie to configure the String
 	 * correctly
 	 * 
 	 * @param food
 	 *            - the food given
 	 */
 	public void foodGiven(Food food) {
 		firstRemoval();
 		logList.addLast("\n" + getTime()
 				+ Everbie.getEverbie().getName()
 				+ " ate som "
 				+ food.getName()
 				+ " and is now"
 				+ (food.getHappinessModifier() > 0 ? " happier" : (food
 						.getHappinessModifier() < 0 ? " angrier" : " the same"))
 				+ (food.getToxicityModifier() > 0 ? " but became sicker"
 						: (food.getToxicityModifier() < 0 ? " and became healthier"
 								: "")) + " than before and not as hungry");
 	}
 
 	/**
 	 * Is called when work is given to the Everbie to configure the String
 	 * correctly
 	 * 
 	 * @param work
 	 *            - the work to do
 	 */
 	public void doneWith(Work work) {
 		firstRemoval();
 		logList.addLast("\n" + getTime()
 				+ Everbie.getEverbie().getName()
 				+ " worked with "
 				+ work.getName()
 				+ " for "
 				+ work.getTime()
 				+ " minutes and earned "
 				+ work.getSalary() + " Oi");
 	}
 
 	/**
 	 * Is called when training is completed by the Everbie to configure the
 	 * String correctly
 	 * 
 	 * @param train
 	 */
 	public void doneWith(Training train) {
 		firstRemoval();
 
 		logList.addLast("\n" + getTime()
 				+ Everbie.getEverbie().getName()
 				+ " worked out by doing some "
 				+ train.getName()
 				+ " and now "
 				+ Everbie.getEverbie().getName()
 				+ " gained"
 				+ (train.getStrengthModifier() > 0 ? " increased" : train
 						.getStrengthModifier() < 0 ? " decreased" : " no")
 				+ " strength and gained"
 				+ (train.getStaminaModifier() > 0 ? " increased" : train
 						.getStaminaModifier() < 0 ? " decreased" : " no")
 				+ " stamina  and gained"
 				+ (train.getIntelligenceModifier() > 0 ? " increased" : train
 						.getIntelligenceModifier() < 0 ? " decreased" : " no")
 				+ " intelligence and grew hungrier");
 	}
 
 	/**
 	 * Is called when training is completed by the Everbie to configure the
 	 * String correctly
 	 * 
 	 * @param occupation
 	 *            - the occupation that is done
 	 */
 	public void doneWith(Occupationable occupation) {
 		if (occupation instanceof Work) {
 			doneWith((Work) occupation);
 		} else if (occupation instanceof Training) {
 			doneWith((Training) occupation);
 		}
 
 	}
 
 	/**
 	 * Is called when the user interacts with the Everbie to configure the
 	 * String correctly
 	 * 
 	 * @param interact
 	 *            - the interaction choosen
 	 */
 	public void interactionMade(Interaction interact) {
 		firstRemoval();
 
 		logList.addLast("\n" + getTime()
 				+ "You and "
 				+ Everbie.getEverbie().getName()
 				+ " "
 				+ interact.getName()
 				+ " and now "
 				+ Everbie.getEverbie().getName()
 				+ (interact.getCutenessModifier() > 0 ? " became cuter"
 						: interact.getCutenessModifier() < 0 ? " became uglier"
 								: " confused")
 				+ (interact.getCharmModifier() > 0 ? " and whinks at you"
 						: interact.getCharmModifier() < 0 ? " and gestures angrily at you"
 								: " and hugs you")
 				+ ", "
 				+ Everbie.getEverbie().getName()
 				+ " is now"
 				+ (interact.getHappinessModifier() > 0 ? " happier than"
 						: interact.getHappinessModifier() < 0 ? " angrier than"
 								: " the same as") + " before");
 	}
 
 	/**
 	 * Is called when item is given to the Everbie to configure the String
 	 * correctly.
 	 * 
 	 * @param item
 	 *            - the item given
 	 */
 	public void itemUsed(Item item) {
 		firstRemoval();
 		logList.addLast("\n" + getTime()
 				+ Everbie.getEverbie().getName()
 				+ " used a "
 				+ item.getName()
 				+ (item.getStrengthModifier() > 0 ? " and became stronger"
 						: (item.getStrengthModifier() < 0 ? " and became weaker"
 								: ""))
 				+ (item.getStaminaModifier() > 0 ? " and became tougher"
 						: (item.getStaminaModifier() < 0 ? " and became less fit"
 								: ""))
 				+ (item.getIntelligenceModifier() > 0 ? " and became smarter"
 						: (item.getIntelligenceModifier() < 0 ? " and became dumber"
 								: ""))
 				+ (item.getCharmModifier() > 0 ? " and became more charming"
 						: (item.getStrengthModifier() < 0 ? " and became less charming"
 								: ""))
 				+ (item.getCutenessModifier() > 0 ? " and became cuter" : (item
 						.getCutenessModifier() < 0 ? " and became uglier" : ""))
 				+ (item.getHealthModifier() > 0 ? " and regained health"
 						: (item.getHealthModifier() < 0 ? " and lost health"
 								: ""))
 				+ (item.getToxicityModifier() > 0 ? " and got sicker" : (item
 						.getStrengthModifier() < 0 ? " and became healthier"
 						: "")));
 	}
 
 	/**
 	 * Is called when the Everbie has fought an enemy.
 	 * 
 	 * @param log
 	 *            - the custom string to add
 	 */
 	public void combatLog(String log) {
 		firstRemoval();
 		logList.addLast(log);
 	}
 
 	/**
 	 * Removes the oldest addition to the log if needed.
 	 */
 	private void firstRemoval() {
 		if (logList.size() > 19) {
 			logList.removeFirst();
 		}
 	}
 
 	/**
 	 * Returns the current time.
 	 * 
 	 * @return
 	 */
 	private String getTime() {
 		NumberFormat formatter = new DecimalFormat("00");
 		String timeString = "\n" + time.monthDay + "/" + (time.month + 1) % 12
 				+ " " + formatter.format(time.hour) + ":"
 				+ formatter.format(time.minute) + " - ";
 		return timeString;
 	}
 
 	/**
 	 * Prints out that you lack the required strength.
 	 * 
 	 * @param strengthReq
 	 *            - the strength required
 	 */
 	public void tooWeak(int strengthReq) {
 		logList.addLast("\n" + Everbie.getEverbie().getName() + " is too weak, "
 				+ strengthReq + " strength is required");
 	}
 
 	/**
 	 * Prints out that you lack the required strength.
 	 * 
 	 * @param strengthReq
 	 *            - the strength required
 	 */
 	public void tooLazy(int staminaReq) {
 		logList.addLast("\n" + Everbie.getEverbie().getName() + " is too lazy, "
 				+ staminaReq + " stamina is required");
 	}
 
 	/**
 	 * Prints out that you lack the required strength.
 	 * 
 	 * @param strengthReq
 	 *            - the strength required
 	 */
 	public void tooDumb(int intelligenceReq) {
 		logList.addLast("\n" + Everbie.getEverbie().getName() + " is too stupid, "
 				+ intelligenceReq + " intelligence is required");
 	}
 	
 	public void reset(){
		logList.clear();
 	}
 }
