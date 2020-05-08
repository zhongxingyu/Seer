 /********************************************************************************
  *                                                                              *
  *  This file is part of Recraft.                                               *
  *                                                                              *
  *  Recraft is free software: you can redistribute it and/or modify             *
  *  it under the terms of the GNU General Public License as published by        *
  *  the Free Software Foundation, either version 3 of the License, or           *
  *  (at your option) any later version.                                         *
  *                                                                              *
  *  Recraft is distributed in the hope that it will be useful,                  *
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of              *
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
  *  GNU General Public License for more details.                                *
  *                                                                              *
  *  You should have received a copy of the GNU General Public License           *
  *  along with Recraft.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                              *
  *  Copyright 2012 Chris Foster.                                                *
  *                                                                              *
  ********************************************************************************/
 
 package recraft.packet;
 
 import recraft.core.Packet;
 import recraft.util.IntVector3;
 
 public class TestPacket extends Packet
 {
 	public static int id = 1;
 
 	private IntVector3 vector;
 
 	public TestPacket(int x, int y, int z)
 	{
 		this.vector = new IntVector3(x, y, z);
 	}
 
 	@Override
 	public Object open()
 	{
 		return this.vector;
 	}
 }
