 package elfville.client.views.subcomponents;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import elfville.client.views.Board;
 
 
 public abstract class CreatePanel extends JPanel implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	protected final JTextArea textArea = new JTextArea();
 	protected final JTextField textField = new JTextField();
 	protected final String buttonLabel;
 	protected final String textFieldLabel;
 	protected final String textAreaLabel;
 	protected final Board board;
 
 	public CreatePanel(Board board, String textFieldLabel,
 			String textAreaLabel, String buttonLabel) {
 		super();
 		this.board = board;
 		this.textAreaLabel = textAreaLabel;
 		this.textFieldLabel = textFieldLabel;
 		this.buttonLabel = buttonLabel;
 		makeThePanel();
 	}
 
 	private void makeThePanel() {
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder("Create Clan"),
 				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
 		
 		JButton button = new JButton(buttonLabel);
 		button.addActionListener(this);
 		
		add(new JLabel(textFieldLabel));
 		//name.setMinimumSize(new Dimension(300, 20));
 		//name.setPreferredSize(new Dimension(300, 20));
 		add(textField);
		add(new JLabel(textAreaLabel));
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		JScrollPane scrollabletextArea = new JScrollPane(textArea);
 		scrollabletextArea.setPreferredSize(new Dimension(300,80));
 		scrollabletextArea.setMinimumSize(new Dimension(300, 40));
 		add(scrollabletextArea);
 		add(button);
 		//setMaximumSize(new Dimension(400, 470));
 		//setMinimumSize(new Dimension(400, 470));
 		//TODO: fix the code duplication between create clan panel and create post panel
 	}
 
 	@Override
 	public abstract void actionPerformed(ActionEvent e);
 
 }
