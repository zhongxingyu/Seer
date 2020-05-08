 package frontend.panels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import backend.blocks.Countable;
 import frontend.swing.CurrentConstants;
 import frontend.swing.ScrollPane;
 
 public class StepSolution extends JPanel {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1780266591415441861L;
 	private Saved _savePanel;
 	private Solution _display, _answer, _comp;
 	private List<String> _solList, _compList;
 	private int _stepNumber = 0;
 	private ScrollPane _scroll;
 	private JButton _forwardButton, _backButton;
 	private Countable _ans;
 	
 	public StepSolution(){
 		super();
 		this.setLayout(new BorderLayout());
 		this.setBackground(CurrentConstants.STEPSOLUTION_BG);
 		this.setBorder(CurrentConstants.STEPSOLUTION_BORDER);
 		this.setPreferredSize(new Dimension(100,100));
 
 		_solList = new ArrayList<>();
 		
 		//main display
 		_display = new Solution("\\text{Solutions will be displayed here}");
 //		_display.addMouseListener(new Listener(this));
 		_scroll = new ScrollPane(_display);
 		_scroll.getVerticalScrollBar().setUnitIncrement(20);
 		_scroll.setBorder(CurrentConstants.STEPSOLUTION_SCROLL_BORDER);
 		_scroll.setBackground(CurrentConstants.STEPSOLUTION_SCROLL_BG);
 		
 		//bottom bar
 		JPanel bottomBar = new JPanel(new BorderLayout());
 		bottomBar.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBAR_BORDER);
 		bottomBar.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBAR_BG);
 		
 		//bottom bar left
 		JPanel bottomBarLeft = new JPanel(new BorderLayout());
 		bottomBarLeft.setBackground(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFT_BG);
 		bottomBarLeft.setBorder(CurrentConstants.STEPSOLUTION_BOTTOMBARLEFT_BORDER);
 		_comp = new Solution("");
 		_comp.setBorder(CurrentConstants.STEPSOLUTION_COMP_BORDER);
 		_comp.setBackground(CurrentConstants.STEPSOLUTION_COMP_BG);
 		ScrollPane compScroll = new ScrollPane(_comp);
 		compScroll.setBackground(CurrentConstants.STEPSOLUTION_COMPSCROLL_BG);
 		compScroll.setBorder(CurrentConstants.STEPSOLUTION_COMPSCROLL_BORDER);
 		bottomBarLeft.add(compScroll, BorderLayout.CENTER);
 		_forwardButton = new JButton(">");
 		_backButton = new JButton("<");
 		_forwardButton.addActionListener(new ForwardBack(this, true));
 		_backButton.addActionListener(new ForwardBack(this, false));
 		bottomBarLeft.add(_forwardButton, BorderLayout.EAST);
 		bottomBarLeft.add(_backButton, BorderLayout.WEST);
 		
 		_answer = new Solution(100,100);
 		_answer.setMargins(0, 0);
 		_answer.addMouseListener(new AnswerListener());
 		_answer.setToolTipText("Double click to save answer");
 		_answer.setBorder(CurrentConstants.STEPSOLUTION_ANSWER_BORDER);
 		_answer.setBackground(CurrentConstants.STEPSOLUTION_ANSWER_BG);
 		_answer.setPreferredSize(new Dimension(0,90));
 		
 		bottomBar.add(bottomBarLeft, BorderLayout.CENTER);
 		bottomBar.add(_answer, BorderLayout.EAST);
 		
 		this.add(_scroll, BorderLayout.CENTER);
 		this.add(bottomBar, BorderLayout.SOUTH);
 		
 		checkButtons();
 		
 		this.revalidate();
 	}
 	
 	
 	public void setSavedPanel(Saved savePanel){
 		_savePanel = savePanel;
 	}
 	
 	public void clear(){
 		_display.setTex("");
 		_comp.setTex("");
 		_answer.setTex("");
 		_stepNumber = 0;
 		_solList.clear();
 		_ans = null;
 		checkButtons();
 	}
 	
 	public void setSolution(List<List<String>> steps, Countable answer){
 		_solList = steps.get(1);
 		_compList = steps.get(0);
 		_stepNumber = 0;
 		_display.setTex("\\text{Step " + (_stepNumber + 1) + "}\\\\" + _solList.get(0));
 		_comp.setTex(_compList.get(0));
 		_answer.setTex("\\text{Answer:}\\\\" + answer.toLatex());
 		_ans = answer;
 		checkButtons();
 		this.revalidate();
 		this.repaint();
 	}
 	
	public void setError(String error){
		clear();
		_display.setTex(error);
	}
	
 	public void next(){
 		if(_stepNumber < _solList.size() - 1){
 			_stepNumber++;
 		}
 		checkButtons();
 		_display.setTex("\\text{Step " + (_stepNumber + 1) + "}\\\\" + _solList.get(_stepNumber));
 		_comp.setTex(_compList.get(_stepNumber));
 		resetScroll();
 		this.repaint();
 	}
 	
 	public void prev(){
 		if(_stepNumber > 0){
 			_stepNumber--;
 		}
 		checkButtons();
 		_display.setTex("\\text{Step " + (_stepNumber + 1) + "}\\\\" + _solList.get(_stepNumber));
 		_comp.setTex(_compList.get(_stepNumber));
 		resetScroll();
 		this.repaint();
 	}
 	
 	public void checkButtons(){
 		_backButton.setEnabled(true);
 		_forwardButton.setEnabled(true);
 		if(_stepNumber == 0){
 			_backButton.setEnabled(false);
 		}
 		if(_stepNumber == _solList.size()-1 || _solList.size() == 0){
 			_forwardButton.setEnabled(false);
 		}
 	}
 	
 	public void resetScroll(){
 		_scroll.getVerticalScrollBar().setValue(0);
 		_scroll.getHorizontalScrollBar().setValue(0);
 	}
 	
 	private class AnswerListener extends MouseAdapter{
 		public void mouseClicked(MouseEvent e){
 			if(_ans != null){
 				if(e.getClickCount() == 2){
 					_savePanel.addCountable(_ans);
 				}
 			}
 		}
 	}
 	
 	private class ForwardBack implements ActionListener{
 		StepSolution _ss;
 		boolean _forward;
 		
 		public ForwardBack(StepSolution ss, Boolean forward){
 			_ss = ss;
 			_forward = forward; 
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if(_forward){
 				_ss.next();
 			} else {
 				_ss.prev();
 			}
 		}
 	}
 	
 }
