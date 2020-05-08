 /**
  * Represents a day block in the small calendar. 
  */
 
 package gui.main;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
import java.util.Date;
 
 public class TinyDayBlock extends JPanel implements FocusListener {
 
 	private JTextArea textArea = new JTextArea();
 	private Date date;
 	private DatePicker dp;
 	
 	public TinyDayBlock(DatePicker dp, Date date, boolean today) {
 		this(dp, date, Color.WHITE, today);
 	}
 	
 	public TinyDayBlock(DatePicker dp, Date date, Color color, boolean today) {
 		this.date = date;
 		this.dp = dp;
 		
 		
 		textArea = new JTextArea();
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		textArea.setEditable(false);
 		textArea.setFont(new Font("Tahoma",Font.PLAIN,11));
 		textArea.setOpaque(false);
 		textArea.setHighlighter(null);
 		
 		textArea.setText(date.getDay() + "");
 		
 		setLayout(new BorderLayout());
 		textArea.setFocusable(true);
 		textArea.addFocusListener(this);
 		setBackground(color);
 		setPreferredSize(new Dimension(20,20));
 		add(textArea, BorderLayout.CENTER);
 		if (today) {
 			dp.registerCurrentDay(this);
 			setBorder(BorderFactory.createMatteBorder(1,1,1,1,Color.RED));
 			grabFocus();
 		}
 	}
 
 	public void select() {
 		setBackground(new Color(215, 215, 255));
 	}
 	
 	public void deselect() {
 		setBackground(Color.WHITE);
 	}
 	
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date d) {
 		date=d;
 	}
 	
 	@Override
 	public void focusGained(FocusEvent arg0) {
 		
 		dp.reportFocusGained(this);
 		
 	}
 
 	@Override
 	public void focusLost(FocusEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 }
