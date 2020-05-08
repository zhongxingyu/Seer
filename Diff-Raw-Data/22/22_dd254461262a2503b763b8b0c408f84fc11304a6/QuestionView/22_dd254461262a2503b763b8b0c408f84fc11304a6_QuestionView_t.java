 package com.heralli.ekonsehan;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 public class QuestionView extends JPanel implements MouseListener, ActionListener{
 	
 	BackgroundPanel blankPanel;
 	TypePanel typePanel = new TypePanel();
 	QuestionPanel questionPanel = new QuestionPanel();
 	TimesUpPanel timesUpPanel = new TimesUpPanel();
 	AnswerPanel answerPanel = new AnswerPanel();
 	TimesUpPanel timesUpPanel1 = new TimesUpPanel();
 	
 	
 	
 	Question q;
 	
 	int state=0; 
 		
 	QuestionManager qm;
 	
 	CardLayout cl = new CardLayout();
 	
 	
 	public QuestionView(QuestionManager qm)
 	{
 		this.qm = qm;
 		setCurrentQuestion(qm.getNextQuestion());
 		addMouseListener(this);
 		start();
 	}
 	
 	public void setCurrentQuestion(Question q)
 	{
 		this.q = q;	
 	}
 	
 	
 	public void start()
 	{
 		ImageIcon im;
 		this.setLayout(cl);
 		this.setBackground(new Color(31,73,125));
 		
 		im = new ImageIcon(getClass().getResource("/resources/teaser.jpg"));
 		blankPanel = new BackgroundPanel(im.getImage(),BackgroundPanel.SCALED);
 		
 		this.add(blankPanel,"0");
 		
 		typePanel.setQuestion(q);
 		this.add(typePanel,"1");
 		
 		this.add(timesUpPanel,"2");
 		
 		questionPanel.setQuestion(q);
 		this.add(questionPanel,"3");
 		
 		this.add(timesUpPanel1,"4");
 		
 		answerPanel.setQuestion(q);		
 		this.add(answerPanel,"5");		
 		
 
 		
 		
 	}
 	
 	
 	// Sets the minimum width for column c to be w pixels wide
 	public void setColumnMinWidth(GridBagLayout gbl, int c, int w) {
 	    int[] ws = gbl.columnWidths;
 	    if (ws == null) {
 	        ws = new int[c+1];
 	    } else if (ws.length < c+1) {
 	        ws = new int[c+1];
 	        System.arraycopy(gbl.columnWidths, 0, ws, 0, gbl.columnWidths.length);
 	    }
 	    ws[c] = w;
 	    gbl.columnWidths = ws;
 	}
 
 	// Sets the minimum height for row r to be h pixels high
 	public void setRowMinHeight(GridBagLayout gbl, int r, int h) {
 	    int[] hs = gbl.rowHeights;
 	    if (hs == null) {
 	        hs = new int[r+1];
 	    } else if (hs.length < r+1) {
 	        hs = new int[r+1];
 	        System.arraycopy(gbl.rowHeights, 0, hs, 0, gbl.rowHeights.length);
 	    }
 	    hs[r] = h;
 	    gbl.rowHeights = hs;
 	}
 	public void mousePressed(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseEntered(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseExited(MouseEvent e) {
 	       
 	    }
 
 	    public void mouseClicked(MouseEvent e) {
 	       if (state > 5){
 	    	   state = 0;
 		    	setCurrentQuestion(qm.getNextQuestion());
 		    	   start();
 		   }
 	       CardLayout cl = (CardLayout)(this.getLayout());
 	       cl.show(this, state+"");
 	       System.out.println(state);
 	       state++;
 	       
 	    }
 	    
 	    public void actionPerformed(ActionEvent e){
 	    	mouseClicked(null);
 	    }
 	    
 	    
 	    
 	    class AnswerPanel extends JPanel 
 	    {
 	    	JLabel lblAnswer;
 	    	
 	    	public AnswerPanel()
 	    	{
 	    		JPanel topB = new JPanel();
 	    		topB.setBackground(new Color(31,73,125));
 	    		
 	    		JPanel bottomB = new JPanel();
 	    		bottomB.setBackground(new Color(31,73,125));
 	    				
	    		JPanel centerB = new JPanel(new GridLayout(2,1));
 	    		centerB.setBackground(new Color(255,255,255));
 	    		
	    		JLabel lbl = new JLabel("Correct Answer: ");
	    		lbl.setHorizontalAlignment(SwingConstants.CENTER);
	    		lbl.setFont(new Font("Arial", Font.BOLD, 70));
	    		centerB.add(lbl);
	    			    		
 	    		lblAnswer = new JLabel();
 	    		lblAnswer.setHorizontalAlignment(SwingConstants.CENTER);
 	    		lblAnswer.setFont(new Font("Arial", Font.BOLD, 100));
 	    		centerB.add(lblAnswer);
 	    		
 	    		setLayout(new GridLayout(3,1));
 	    		add(topB);
 	    		add(centerB);
 	    		add(bottomB);
 	    	}
 	    	
 	    	public void setQuestion(Question q)
 	    	{
	    		lblAnswer.setText(q.getAnswer());
 	    		
 	    	}
 	    	
 	    }
 	    
 	    
 	    
 	    
 	    class TimesUpPanel extends JPanel
 	    {
 	    	public TimesUpPanel()
 	    	{
 	    		JPanel topA = new JPanel();
 	    		topA.setBackground(new Color(31,73,125));
 	    		
 	    		JPanel bottomA = new JPanel();
 	    		bottomA.setBackground(new Color(31,73,125));
 	    				
 	    		JPanel centerA = new JPanel(new GridLayout(1,1));
 	    		centerA.setBackground(new Color(255,255,255));
 	    				
 	    		JLabel lblLevelA = new JLabel("TIME'S UP!");
 	    		lblLevelA.setHorizontalAlignment(SwingConstants.CENTER);
 	    		lblLevelA.setFont(new Font("Times New Roman", Font.BOLD, 100));
 	    		centerA.add(lblLevelA);
 	    			
 	    		setLayout(new GridLayout(3,1));
 	    		add(topA);
 	    		add(centerA);
 	    		add(bottomA);
 	    		
 	    	}
 	    	
 	    }
 	    
 	    
 	    class QuestionPanel extends JPanel implements ActionListener
 	    {
 	    	QuizTimer qTimer;
 	    	
 	    	JTextArea displayQuestion;
 	    	JLabel displayPoints;
 	    	JLabel displayType;
 	    	
 	    	
 	    	public QuestionPanel()
 	    	{
 	    		GridBagLayout layout = new GridBagLayout();
 	    		GridBagConstraints c = new GridBagConstraints();
 	    		c.fill = GridBagConstraints.BOTH;
 	    		setLayout(layout);
 	    		
 	    		JPanel leftTop = new JPanel(new GridLayout(2,1));
 	    		leftTop.setBackground(new Color(31,73,125));
 	    		c.gridx =0;
 	    		c.gridy =0;
 	    		layout.setConstraints(leftTop,c);
 	    		qTimer = new QuizTimer();
 	    		qTimer.setTimesUpListener(this);
 	    		displayPoints = new JLabel();
 	    		displayType = new JLabel();
 	    		
 	    		displayPoints.setForeground(Color.YELLOW);
 	    		displayType.setForeground(Color.YELLOW);
 	    		displayType.setFont(new Font("Times New Roman", Font.BOLD, 50));
 	    		displayPoints.setFont(new Font("Times New Roman", Font.BOLD, 40));
 	    		displayPoints.setHorizontalAlignment(SwingConstants.CENTER);
 	    		displayType.setHorizontalAlignment(SwingConstants.CENTER);
 	    		displayType.setVerticalAlignment(SwingConstants.BOTTOM);
 	    		displayPoints.setVerticalAlignment(SwingConstants.TOP);
 	    		
 	    		leftTop.add(displayType);
 	    		leftTop.add(displayPoints);
 	    		
 	    		
 	    		
 
 	    		
 	    		JPanel rightTop = new JPanel(new BorderLayout());
 	    		rightTop.setBackground(new Color(255,255,255));		
 	    		c.gridx =1;
 	    		c.gridy =0;
 	    		layout.setConstraints(rightTop,c);
 	    		displayQuestion = new JTextArea();
 	    		displayQuestion.setFont(new Font("Arial", Font.PLAIN, 70));
 	    		displayQuestion.setLineWrap(true);
 	    		displayQuestion.setWrapStyleWord(true);
 	    		rightTop.add(new JScrollPane(displayQuestion));
 	    		
 	    		JPanel leftCenter = new JPanel();
 	    		leftCenter.setBackground(new Color(31,73,125));
 	    		c.gridx =0;
 	    		c.gridy =1;
 	    		layout.setConstraints(leftCenter,c);
 	    				
 	    		JPanel rightCenter = new JPanel(new BorderLayout());
 	    		rightCenter.setBackground(new Color(255,255,255));		
 	    		c.gridx =1;
 	    		c.gridy =1;
 	    		layout.setConstraints(rightCenter,c);
 	    				
 	    		JPanel leftBottom = new JPanel();
 	    		leftBottom.setBackground(new Color(31,73,125));
 	    		c.gridx =0;
 	    		c.gridy =2;
 	    		layout.setConstraints(leftBottom,c);
 	    		leftBottom.add(qTimer);
 	    		
 	    		JPanel rightBottom = new JPanel(new GridLayout(2,2));
 	    		rightBottom.setBackground(new Color(255,255,255));		
 	    		c.gridx =1;
 	    		c.gridy =2;
 	    		layout.setConstraints(rightBottom,c);
 	    		
 	    		/*
 	    		if (q.getType().equals(Question.MULTIPLE_CHOICE))
 	    		{
 	    			MultipleChoiceQuestion q2=(MultipleChoiceQuestion)q;
 	    			
 	    			JLabel choiceA = new JLabel(q2.getChoice(0));
 	    			choiceA.setFont(new Font("Arial", Font.PLAIN, 70));
 	    			JLabel choiceB = new JLabel(q2.getChoice(1));
 	    			choiceB.setFont(new Font("Arial", Font.PLAIN, 70));
 	    			JLabel choiceC = new JLabel(q2.getChoice(2));
 	    			choiceC.setFont(new Font("Arial", Font.PLAIN, 70));
 	    			JLabel choiceD = new JLabel(q2.getChoice(3));
 	    			choiceD.setFont(new Font("Arial", Font.PLAIN, 70));
 	    						
 	    			rightBottom.add(choiceA);
 	    			rightBottom.add(choiceB);
 	    			rightBottom.add(choiceC);
 	    			rightBottom.add(choiceD);
 	    		}
 	    		
 	    		*/
 	    		
 	    		add(leftTop);
 	    		add(rightTop);
 	    		add(leftCenter);
 	    		add(rightCenter);
 	    		add(leftBottom);
 	    		add(rightBottom);
 	    		
 	    		layout.columnWeights = new double[]{0.0f,4.0f};
 	    		layout.rowWeights = new double[]{2.0f,2.0f};
 	    		
 	    		
 	    		setColumnMinWidth(layout, 0, 300);
 	    		setRowMinHeight(layout, 0, 550);
 	    		setRowMinHeight(layout, 1, 300);
 	    		setRowMinHeight(layout, 2, 300);
 	    		
 	    	}
 	    	
 	    	public void setQuestion(Question q)
 	    	{
 	    		displayQuestion.setText(q.getQuestion());
 	    		displayPoints.setText("(" +q.getLevel()+" - " + q.getPoints() + " points )");
 	    		displayType.setText(q.getType());
 	    		qTimer.setMax(q.getTimeAllowed());
 	    	}
 	    	
 	    	public void actionPerformed(ActionEvent e){
 	    		QuestionView.this.mouseClicked(null);
 		    }
 	    }
 	    
 	    
 	    class TypePanel extends JPanel implements ActionListener
 	    {
 	    	QuizTimer betTimer = new QuizTimer(10);
 	    	JLabel lblType;
 	    	JLabel lblLevel;
 	    	
 	    	public TypePanel(){
 	    		
 	    		JPanel top = new JPanel();
 	    		top.setBackground(new Color(31,73,125));
 	    		
 	    		JPanel bottom = new JPanel();
 	    		bottom.setBackground(new Color(31,73,125));
 	    		
 	    		betTimer.setTimesUpListener(this);
 	    		bottom.add(betTimer);
 	    		
 	    		JPanel center = new JPanel(new GridLayout(2,1));
 	    		center.setBackground(new Color(255,255,255));
 	    		
 	    		lblType = new JLabel();
 	    		lblType.setHorizontalAlignment(SwingConstants.CENTER);
 	    		lblType.setFont(new Font("Times New Roman", Font.BOLD, 80));
 	    		
 	    		lblLevel = new JLabel();
 	    		lblLevel.setHorizontalAlignment(SwingConstants.CENTER);
 	    		lblLevel.setFont(new Font("Times New Roman", Font.BOLD, 70));
 	    		lblLevel.setForeground(new Color(100,100,100));
 	    		center.add(lblType);
 	    		center.add(lblLevel);
 	    		
 	    		setLayout(new GridLayout(3,1));
 	    		add(top);
 	    		add(center);
 	    		add(bottom);
 	    		
 	    	}
 	    	
 	    	public void setQuestion(Question q)
 	    	{
 	    		lblType.setText(q.getType());
 	    		lblLevel.setText("(" +q.getLevel()+" - " + q.getPoints() + " points )");
 	    		betTimer.setMax(10);
 	    	}
 	    	
 	    	public void actionPerformed(ActionEvent e){
 		    	QuestionView.this.mouseClicked(null);
 		    }
 	    	
 	    }
 	
 }
