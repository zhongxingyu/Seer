 package controller;
 
 import java.awt.event.ActionEvent;
 import java.util.HashSet;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 
 import model.Field;
 import model.Game;
 import model.Player;
 import model.Position;
 import model.Ring;
 import view.DrawBoardAbsolute;
 import view.DrawField;
 import view.DrawMain;
 import view.RadioButtonStartGame;
 //import controller.*;
 import model.*;
 
 public class UndoListener extends AbstractAction{
 //	DrawMain dm;
 //	JDialog jd;
 //	String buttonchosen;
 	Game game;
 	GameController gamec;
 	public UndoListener(GameController gc){		
 		super();
 //		dm=drawm;
 //		jd= dialog;
 //		buttonchosen=s;
 		gamec=gc;
 		game=gc.get_game();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
			System.out.println(game.get_moves());
		    if (!game.get_moves().isEmpty()){
 			Move m = game.undo_move();
			JButton df = (JButton) e.getSource();
 			DrawBoardAbsolute dba = (DrawBoardAbsolute) df.getParent();
//			Field f = df.get_field();
 			HashSet<Position> posset = new HashSet<Position>(); 
 			HashSet<Field> fieldset = new HashSet<Field>();
 			HashSet<DrawField> drawset = new HashSet<DrawField>();
 			
 		    posset.addAll(m.get_own_positions().keySet());
 			for (Position position : posset){
 				fieldset.add(game.get_board().get_fields().get(position));
 			}
 			drawset.addAll(dba.get_drawfields(fieldset));
 			for (DrawField dff : drawset){
 				dff.deselect();
 			}
 			posset.addAll(m.get_affected_positions());
 			for (Position position : posset){
 				fieldset.add(game.get_board().get_fields().get(position));
 			}
 			drawset.addAll(dba.get_drawfields(fieldset));
 			gamec.get_board().update_fields(drawset);
		    }
 	}
 //		String choice = bg.getSelection().getActionCommand();
 //		JButton button = (JButton)e.getSource();
 //		RadioButtonStartGame rbsg = (RadioButtonStartGame)button.getParent();
 //		
 //		System.out.println(rbsg.group.getSelection().getActionCommand() +"in action game");
 //		jd.setVisible(false);
 //		Player p1 = new Player(1,"test1");
 //		Player p2 = new Player(1,"test2");
 //		Ring<Player> lp = new Ring<Player>();
 //		lp.add(p1);
 //		lp.add(p2);
 //		GameController gc = new GameController(lp, dm);
 //		}
 //		Game g = new Game(lp);
 		
 //		DrawBoardAbsolute db = new DrawBoardAbsolute(g.get_board());
 //		dm.getContentPane().add(db);
 //		dm.setVisible(true);
 		
 }
