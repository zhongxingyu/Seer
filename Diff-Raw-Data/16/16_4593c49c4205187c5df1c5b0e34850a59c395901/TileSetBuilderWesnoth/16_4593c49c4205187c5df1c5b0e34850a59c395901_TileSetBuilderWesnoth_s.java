 /*
  * Copyright (C) 2012-2013 Martin Steiger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package io;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 import terrain.TerrainType;
 import tiles.HexTileSet;
 
 import com.google.common.collect.DiscreteDomains;
 import com.google.common.collect.Ranges;
 import common.OctDirection;
 
 
 /**
  * TODO Type description
  * @author Martin Steiger
  */
 public class TileSetBuilderWesnoth
 {
 	public HexTileSet read()
 	{
 		Map<OctDirection, TerrainType> borders = new HashMap<>();
 
 		HexTileSet ts = new HexTileSet(72, 72, 36);
 		ts.addImage(new File("data/wesnoth/foreground.png"), 0, 0, 0, 0);
 		ts.setCursorTileIndex(0);
 		ts.setInvalidTileIndex(0);
 		
 		ts.addImage(new File("data/wesnoth/grass/green.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green2.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green3.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green4.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green5.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green6.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green7.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/grass/green8.png"), 0, 0, 0, 0);
 		
 		ts.defineTerrain(Ranges.closed(1, 8).asSet(DiscreteDomains.integers()), TerrainType.GRASS, borders);
 
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A01.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A02.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A03.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A04.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A05.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A06.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A07.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A08.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A09.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A10.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A11.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A12.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A13.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A14.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/water/coast-tropical-A15.png"), 0, 0, 0, 0);
 
 		ts.defineTerrain(Ranges.closed(9, 23).asSet(DiscreteDomains.integers()), TerrainType.WATER, borders);
 
		ts.addImage(new File("data/wesnoth/desert/desert.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert2.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert3.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert4.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert5.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert6.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert7.png"), 0, 0, 0, 0);
		ts.addImage(new File("data/wesnoth/desert/desert8.png"), 0, 0, 0, 0);
 		
 		ts.defineTerrain(Ranges.closed(24, 31).asSet(DiscreteDomains.integers()), TerrainType.SAND, borders);
 	
 		ts.addImage(new File("data/wesnoth/forest/forested-deciduous-summer-hills-tile.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/forest/forested-hills-tile.png"), 0, 0, 0, 0);
 
 		ts.defineTerrain(Ranges.closed(32, 33).asSet(DiscreteDomains.integers()), TerrainType.FOREST, borders);
 
 		ts.addImage(new File("data/wesnoth/hills/regular.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/hills/regular2.png"), 0, 0, 0, 0);
 		ts.addImage(new File("data/wesnoth/hills/regular3.png"), 0, 0, 0, 0);
 
 		ts.defineTerrain(Ranges.closed(34, 36).asSet(DiscreteDomains.integers()), TerrainType.MOUNTAIN, borders);
 
 		ts.addImage(new File("data/wesnoth/grid.png"), 0, 0, 0, 0);
 
 		ts.setGridTileIndex(37);
 		
 		return ts;
 	}
 }
