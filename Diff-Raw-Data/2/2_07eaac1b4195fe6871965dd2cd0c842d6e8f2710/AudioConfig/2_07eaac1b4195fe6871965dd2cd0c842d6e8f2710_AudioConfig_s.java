 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.model.audio;
 
 import se.chalmers.dat255.sleepfighter.model.IdProvider;
 
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * AudioConfig models data per Alarm such as volume, etc.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 27, 2013
  */
 @DatabaseTable(tableName = "audio_config")
 public class AudioConfig implements IdProvider {
 	public static final String ID_COLUMN = "id";
 
	@DatabaseField(generatedId = true)
 	private int id;
 
 	// TODO: REMOVE when real fields are added, NEEDED 'cause SQLite crashes otherwise.
 	@DatabaseField
 	private String temp;
 
 	/**
 	 * Constructs an AudioConfig, for DB purposes only.
 	 */
 	public AudioConfig() {
 	}
 
 	/**
 	 * Returns the id of the AudioConfig (in DB).
 	 *
 	 * @return the id.
 	 */
 	public int getId() {
 		return this.id;
 	}
 }
