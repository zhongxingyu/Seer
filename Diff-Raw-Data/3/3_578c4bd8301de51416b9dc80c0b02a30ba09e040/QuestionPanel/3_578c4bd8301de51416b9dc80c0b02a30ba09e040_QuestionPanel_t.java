 package rsqbexpt;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collections;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ButtonModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
 
 public class QuestionPanel extends ExptPanel {
 	private Question _question;
 	int _index;
 	private Vector<JRadioButton> _options;
 
 	public QuestionPanel(Dimension d, ExptFrame ef,int qstindx) {
 		super(d,ef);
 		_question = _frame.getUser().getQuestionAtIndex(qstindx);
 		_index = qstindx;
 
 
 		//do whatever is necessary to set up a status quo
 		
 		
 		//get the number of options for question at index
 		int numOps = _question._lotteries.size();
 
 		//create a vector for the radio buttons
 		_options = new Vector<JRadioButton>(); 
 		
 		//set the _c for the SQ
 		_c.gridwidth=1;
 		_c.gridx=0;
 		_c.gridy=3;
 		
 		//create path to one to select so that we can select it after making the buttongroup
 		JRadioButton selected = null;
 		for (int i=0; i<numOps;i++){
 			int lotteryNumber = _frame.getScribe().getLotteryNumber(_question.number, i);
 			String htmlstring = _frame.getScribe().getHTMLforLotID(_question._lotteries.get(i));
 			JRadioButton tempbutton = new JRadioButton("<html>"+htmlstring+"</html>");
 			if (lotteryNumber == _question._sq){// we found the status quo!
 				switch(_question._sqtype){
 				case 0:
 					//do nothing
 					break;
 				case 1:
 					//this will just have a suggested option on the top pane
 					_northpanel.add(new JLabel("<html>"+htmlstring+"</html>"),_c);
 					break;
 				case 2:
 					//this will just pre-select the status quo option
 					selected=tempbutton;
 					_question._response = _question._sq;
 
 					break;
 				case 3:
 					//this will put the SQ above AND pre-select
 					selected=tempbutton;
 					_northpanel.add(new JLabel("<html>"+htmlstring+"</html"),_c);
 					_question._response = _question._sq;
 					break;
 					
 				}
 			}
 			
 
 			tempbutton.addActionListener(new lradioAL(lotteryNumber));
 			_options.add(i,tempbutton);
 			
 		}
 		
 		//shuffle the options
 		Collections.shuffle(_options);
 		
 		JPanel optionspanel = new JPanel();
 		optionspanel.setLayout(new GridBagLayout());
 		optionspanel.setBorder(new javax.swing.border.TitledBorder("Question "+(_index+1)+" of "+_frame._totalquestions));
 
 		
 		//find the number of columns/rows
 		int numops = _options.size();
 		int max = (int)java.lang.Math.ceil(java.lang.Math.sqrt(numops));
 		//add the labels to a button group and to the panel
 		ButtonGroup bgroup = new ButtonGroup();
 		_c.insets = new Insets(3,20,20, 3);
 		for (int i=0;i<numOps;i++){
 			int myrow = i;
 			int mycol = 0;
			if(max>=3){
 			while (myrow >=max){
 				myrow = myrow-(max);
 				mycol = mycol+1;
 			}
			}
 			bgroup.add(_options.get(i));
 			_c.gridx=mycol;
 			_c.gridy=myrow;
 			optionspanel.add(_options.get(i),_c);
 		}
 		
 		//select if there is an SQ
 		if(selected!=null)
 			selected.setSelected(true);
 		
 		_middlepanel.add(optionspanel);
 		
 		//add the "next" button with the right text
 		JButton next=null;
 		if(_frame.testFinalQuestion(qstindx))
 			next = new JButton("submit and review all selections");
 		else
 			next = new JButton("submit and continue");
 		
 		next.addActionListener(new nextQuestionAL());
 		JPanel wrap = new JPanel();
 		_c.gridx=0;
 		_c.gridy=30;
 		_c.gridwidth=5;
 		wrap.add(next);
 		_middlepanel.add(wrap,_c);
 	
 	
 	
 	}
 		
 	private class lradioAL implements ActionListener{
 		private int id;
 		public lradioAL(int i){
 			id=i;
 			
 		}
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			_question._response = id;
 		}
 
 	}
 
 
 	private class nextQuestionAL implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if(_question._response!=0){				
 				//check to see if we are on the final question, if not, move on to the next question
 				if (_frame.testFinalQuestion(_index))
 					_frame.switchToReviewPage();	
 				else
 					_frame.switchToNextQuestion(_index+1);
 			}
 		}
 
 	}
 
 }
