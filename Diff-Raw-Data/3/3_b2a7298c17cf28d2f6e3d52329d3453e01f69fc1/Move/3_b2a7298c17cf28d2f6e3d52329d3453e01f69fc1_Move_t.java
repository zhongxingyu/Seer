 package gestures.filters;
 
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 
 import entities.ships.Player;
 /**
  * The Backoff is a back movement, which accept a trace with gradient between 260 and 280 degrees
  * 
  * @author Quentin Bernard et Ludovic Feltz
  */
 
 /* <This program is an Shoot Them up space game, called Escape-IR, made by IR students.>
  *  Copyright (C) <2012>  <BERNARD Quentin & FELTZ Ludovic>
 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 
 public class Move implements Filter {
 	
 	double angle = 0;
 	double vec_x = 0;
 	double vec_y = 0;
 	
 	
 	@Override
 	public boolean check(List<Vec2> trace){
 		if(trace.size() < 2)
 			return true;
		angle = Filters.getAngle(trace);
 		Vec2 beg = trace.get(0);
 		Vec2 end = trace.get(trace.size()-1);
 		vec_x = Math.abs(end.x - beg.x);
 		vec_y = Math.abs(end.y - beg.y);
 		return true;
 	}
 
 	@Override
 	public void apply(Player ship) {
 		
 		float norm = (float) Math.sqrt(vec_x*vec_x + vec_y*vec_y);
 		ship.move(angle, (int)norm);
 	}
 	
 }
