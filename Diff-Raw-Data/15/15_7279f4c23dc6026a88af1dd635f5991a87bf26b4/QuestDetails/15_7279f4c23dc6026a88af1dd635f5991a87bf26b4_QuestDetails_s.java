 /*
  * This file is part of MineQuest-API, version 3, Specifications for the
  * MineQuest system.
  * MineQuest-API, version 3 is licensed under GNU Lesser General Public License
  * v3.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.theminequest.api.quest;
 
 import java.io.Serializable;
 
 /**
  * Defines the structure of the quest.
  * 
  * @author The MineQuest Team
  * @since 2.0.0
  * @version 3.0.0
  * @see com.theminequest.api.quest.Quest
  */
 public interface QuestDetails extends Serializable, Comparable<QuestDetails> {
 	
 	/**
 	 * Associated file with this QuestDetails.<br>
 	 * Returns a {@link java.io.File}.
 	 */
 	static final String QUEST_FILE = "mq.file";
 	/**
 	 * The property that contains the Display Name,
 	 * a human readable version of the Quest Name.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_DISPLAYNAME = "mq.displayname";
 	/**
 	 * The property that contains the Quest Description.<br>
 	 * If none is explicitly specified it uses the default
 	 * specified by localization and user-defined configuration.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_DESCRIPTION = "mq.description";
 	/**
 	 * The property that contains the acceptance message.<br>
 	 * If none is explicitly specified it uses the default
 	 * specified by localization and user-defined configuration.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_ACCEPT = "mq.accept";
 	/**
 	 * The property that contains the aborted message.<br>
 	 * If none is explicitly specified it uses the default
 	 * specified by localization and user-defined configuration.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_ABORT = "mq.abort";
 	/**
 	 * The property that contains the completed message.<br>
 	 * If none is explicitly specified it uses the default
 	 * specified by localization and user-defined configuration.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_COMPLETE = "mq.complete";
 	/**
 	 * The property that contains the failed message.<br>
 	 * If none is explicitly specified it uses the default
 	 * specified by localization and user-defined configuration.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_FAIL = "mq.fail";
 	/**
 	 * The property that contains the tasks this quest has.<br>
	 * Returns a {@link java.util.LinkedHashMap} containing
 	 * keys of {@link java.lang.Integer} and values of {@link java.lang.String}
 	 * arrays.
 	 */
 	static final String QUEST_TASKS = "mq.tasks";
 	/**
 	 * The property that contains the event specifications this
 	 * quest has.<br>
	 * Returns a {@link java.util.LinkedHashMap} containing keys of
 	 * {@link java.lang.Integer} and values of {@link java.lang.String}.
 	 */
 	static final String QUEST_EVENTS = "mq.events";
 	/**
 	 * The property that determines whether players participating
 	 * in the quest are sent back to their original locations
 	 * after the quest has been completed or aborted.<br>
 	 * Returns a {@link java.lang.Boolean}.
 	 */
 	static final String QUEST_SPAWNRESET = "mq.spawnreset";
 	/**
 	 * The property that determines the quest spawn point in
 	 * the world.<br>
 	 * Returns an array of <code>double</code> (3 length).
 	 */
 	static final String QUEST_SPAWNPOINT = "mq.spawnpoint";
 	/**
 	 * The property that contains the message sent to players
 	 * when they attempt to edit a part of the quest that is
 	 * not editable.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_EDITMESSAGE = "mq.editmessage";
 	/**
 	 * The property that contains the world name the quest
 	 * is on. If the quest is instanced, it contains the world
 	 * name that an instance will be created off of.<br>
 	 * Returns a {@link java.lang.String}.
 	 */
 	static final String QUEST_WORLD = "mq.world";
 	/**
 	 * The property that determines whether the quest should
 	 * be isolated in an instanced world or not (aka dungeons).<br>
 	 * Returns a {@link java.lang.Boolean}.
 	 */
 	static final String QUEST_LOADWORLD = "mq.loadworld";
 	/**
 	 * The property that provides world flags.<br>
 	 * Returns a {@link java.lang.Integer}.
 	 */
 	static final String QUEST_WORLDFLAGS = "mq.worldflags";
 	/**
 	 * The property that represents what requirements this quest
 	 * contains.<br>
	 * Returns a {@link java.util.LinkedHashMap} containing keys of
 	 * {@link java.lang.Integer} and values of
 	 * {@link com.theminequest.api.requirement.QuestRequirement}.
 	 */
 	static final String QUEST_REQUIREMENTDETAILS = "mq.requirementdetails";
 	/**
 	 * The property that represents what requirements should be checked
 	 * upon receiving the quest.<br>
 	 * Returns a {@link java.util.List} of {@link Integer}s.
 	 */
 	static final String QUEST_GETREQUIREMENTS = "mq.getrequirements";
 	/**
 	 * The property that represents what requirements should be checked
 	 * upon starting the quest.<br>
 	 * This does <b>NOT</b> apply to non-instanced
 	 * quests, as they start immediately upon getting.<br>
 	 * Returns a {@link java.util.List} of {@link Integer}s.
 	 */
 	static final String QUEST_STARTREQUIREMENTS = "mq.startrequirements";
 	/**
 	 * The property that represents what targets are available to the quest.
 	 * <br>Returns a {@link java.util.Map} containing keys of
 	 * {@link java.lang.Integer} and values of
 	 * {@link com.theminequest.api.targeted.QuestTarget}s.
 	 * @since 3.0.0-Milestone2
 	 */
 	static final String QUEST_TARGET = "mq.questtargets";
 	
 	// ----------------------------------------------------------------------
 	
 	/**
 	 * Retrieve the <b>unique</b> name of this quest detail object.
 	 * 
 	 * @since 3.0.0
 	 * @return Name of the Quest
 	 */
 	public String getName();
 	
 	/**
 	 * Retrieve the property associated with the key.
 	 * 
 	 * @param <E> returned type
 	 * @param key Key
 	 * @return Associated Property, or <code>null</code> if there is none.
 	 *         Note that <code>null</code> does not necessarily mean that there
 	 *         is
 	 *         no such property, and that the property may actually be
 	 *         <code>null</code>.
 	 */
 	public <E> E getProperty(String key);
 	
 	/**
 	 * Set the property associated with the key.
 	 * 
 	 * @param <E> returned type
 	 * @param key Key to set
 	 * @param property Property to set
 	 * @return Associated Property (if one was there previously).
 	 */
 	public <E> E setProperty(String key, Serializable property);
 	
 	/**
 	 * Check if this contains a property.
 	 * 
 	 * @param key Key to check
 	 * @return true if there is an associated property
 	 */
 	public boolean containsProperty(String key);
 	
 	/**
 	 * Remove a property
 	 * 
 	 * @param <E> returned type
 	 * @param key Key to remove
 	 * @return Property removed
 	 */
 	public <E> E removeProperty(String key);
 	
 	/**
 	 * Generate a quest from this quest detail.
 	 * 
 	 * @param questId Quest ID.
 	 * @param questOwner Owner of this quest.
 	 * @return the generated quest.
 	 */
 	public Quest generateQuest(long questId, String questOwner);
 	
 }
