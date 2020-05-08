 package controller;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 import model.*;
 import view.*;
 
 
 public class MouseClicker implements MouseListener {
 
 	private GameController gamec;
 	private Game g;
 	
 	public MouseClicker(GameController gc){
 		gamec=gc;
 		g = gc.get_game();
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		// TODO Auto-generated method stub
 		DrawField df = (DrawField) e.getSource();
 		DrawBoardAbsolute dba = (DrawBoardAbsolute) df.getParent();
 		Field f = df.get_field();
 		
 		Position pos = f.get_position();
 		System.out.println("Clicked position is: " + pos);
 		System.out.println("Clicked field is: " + f);
 		//creation for sets for the different objects
 		HashSet<Position> posset = new HashSet<Position>(); 
 		HashSet<Field> fieldset = new HashSet<Field>();
 		HashSet<DrawField> drawset = new HashSet<DrawField>();
 		
 		// this part concerns selecting positions
 		if (g.check_position(pos)) {
 			System.out.println("Az sym!");
 			posset.addAll(g.add_position(pos));
 			for (Position position : posset){
 				fieldset.add(g.get_board().get_fields().get(position));
 			}
 			df.select();
 			drawset.addAll(dba.get_drawfields(fieldset));
 			for (DrawField dff : drawset){
 				dff.deselect();
 			}
 			drawset.add(df);
 		
 		}
 		// this part concerns moves
 		
		if (g.check_neighbour_position(pos)){
 			System.out.println("MESTYA");
 			Move move = g.gen_move(pos);
 			if (g.check_position_for_move(pos, move)){
 				System.out.println("USPYAVAM MESTYA");
 				g.move(move);
 				
 			//deselect the selected pieces
 				posset.addAll(move.get_own_positions().keySet());
 				for (Position position : posset){
 					fieldset.add(g.get_board().get_fields().get(position));
 				}
 				drawset.addAll(dba.get_drawfields(fieldset));
 				for (DrawField dff : drawset){
 					dff.deselect();
 				}
 				posset.addAll(move.get_affected_positions());
 				for (Position position : posset){
 					fieldset.add(g.get_board().get_fields().get(position));
 				}
 				drawset.addAll(dba.get_drawfields(fieldset));
 //				System.out.println(move.get_affected_positions());
 //				posset.addAll(move.get_affected_positions());
 			}
 		}
 //		if (!posset.isEmpty()){
 //		for (Position position : posset){
 //			fieldset.add(g.get_board().get_fields().get(position));
 //
 //		}
 //		df.select();
 //		drawset.addAll(dba.get_drawfields(fieldset));
 //		for (DrawField dff : drawset){
 //			dff.deselect();
 //		}
 //		drawset.add(df);
 //		}
 		//update of the fields that need to be redrawn
 		System.out.println(drawset);
 		gamec.get_board().update_fields(drawset);
 
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
