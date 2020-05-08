 /*
  * Dog - A project for making highly scalable non-clustered game and simulation environments.
  * Copyright (C) 2009-2010 BlinzProject <gtalent2@gmail.com>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License version 2 as
  * published by the Free Software Foundation.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.blinz.dog.world;
 
 /**
  * An interface to be implemented by sprites that need to be constantly updated
  * every cycle.
  * @author Blinz
  */
 public interface UpdatingSprite {
 
     /**
     * Method invoked regardless of Zone status as client or server in Zone
     * execution once per Zone cycle.
      */
     public void update();
 
     /**
      * Method for client specific Zones that is invoked in Zone execution once
      * per Zone cycle in client Zones.
      */
     public void clientUpdate();
 
     /**
      * Method for server specific Zones that is invoked in Zone execution once
      * per Zone cycle in server Zones.
      */
     public void serverUpdate();
 }
