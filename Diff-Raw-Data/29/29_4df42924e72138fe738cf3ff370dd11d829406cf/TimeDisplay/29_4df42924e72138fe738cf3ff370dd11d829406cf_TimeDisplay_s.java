 package frontend;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JLabel;
 import javax.swing.Timer;
 
 import backend.Minesweeper;
 
 public class TimeDisplay extends JLabel implements ActionListener{
 	
 	private Minesweeper game;
 	
 	public TimeDisplay(Minesweeper m){
 		super();
		super.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 36));
 		super.setHorizontalAlignment(JLabel.CENTER);
 		game = m;
 		m.getTimer().addActionListener(this);
 		refresh();
 	}
 	
 	private void refresh(){
 		int time = game.getTime();
 		String s = "";
 		//Minutes
 		s += time / 600;
 		s += ":";
 		//Seconds
 		if(((time % 600) / 10) < 10)
 			s += "0";
 		s += (time % 600) / 10;
 		s += ".";
 		//Tenths of a Second
 		s += (time % 600) % 10;
 		super.setText(s);
 		repaint();
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		refresh();
 	}
 	
 	
 	
 }
