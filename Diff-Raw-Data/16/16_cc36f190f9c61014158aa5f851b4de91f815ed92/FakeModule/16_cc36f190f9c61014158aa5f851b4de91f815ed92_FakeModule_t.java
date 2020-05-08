 /**
 * Copyright © 2013 Joseph Walton-Rivers <webpigeon@unitycoders.co.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx.commandprocessor;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  * Fake module used for unit testing.
  * 
  * @author webpigeon
  * 
  */
 public class FakeModule
 {
 
 	/**
 	 * Default command
 	 * 
 	 * @param ex
 	 */
 	@Command
 	public void doStuff(MessageEvent<PircBotX> ex)
 	{
 	}
 
 	/**
 	 * Single command
 	 * 
 	 * @param ex
 	 */
 	@Command("hello")
 	public void doStuffOnce(MessageEvent<PircBotX> ex)
 	{
 	}
 
 	/**
 	 * Double command
 	 * 
 	 * @param ex
 	 */
 	@Command({ "goodbye", "bye" })
 	public void doStuffTwice(MessageEvent<PircBotX> ex)
 	{
 	}
 }
