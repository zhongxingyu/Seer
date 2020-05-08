 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 
 public class GraphicUI implements ActionListener {
 	private final JFrame window;
 	private final Container globalPane, menuPane, gamePane;
 	private final RoundButton green, blue, red, yellow, black, white, none;
 	private final JButton newGame, resetLine, valid, quit, resetPawn;
 	private final int numberOfPawnPerRow = 4, numberOfRow = 12;
 	private final ArrayList<RoundButton[]> rows = new ArrayList<>();
 	
 	private int currentRow = 0, currentPawn = 0;
 
 	public GraphicUI() throws Exception {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 
 		this.quit = new JButton("Quitter");
 		this.newGame = new JButton("Nouvelle partie");
 		this.resetLine = new JButton("Effacer la ligne");
 		this.valid = new JButton("Valider");
 		this.resetPawn = new JButton("Effacer le dernier pion");
 
 		this.none = new RoundButton(null, 10);
 		this.red = new RoundButton(Color.RED, 10);
 		this.blue = new RoundButton(Color.BLUE, 10);
 		this.green = new RoundButton(Color.GREEN, 10);
 		this.black = new RoundButton(Color.BLACK, 10);
 		this.white = new RoundButton(Color.WHITE, 10);
 		this.yellow = new RoundButton(Color.YELLOW, 10);
 
 		this.red.addActionListener(this);
 		this.blue.addActionListener(this);
 		this.green.addActionListener(this);
 		this.black.addActionListener(this);
 		this.white.addActionListener(this);
 		this.yellow.addActionListener(this);
 
 		this.resetPawn.addActionListener(this);
 		this.resetLine.addActionListener(this);
 		this.newGame.addActionListener(this);
 		this.valid.addActionListener(this);
 		this.quit.addActionListener(this);
 
 		this.window = new JFrame("Mastermind");
 		this.window.setVisible(true);
 		this.window.setResizable(false);
 		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		this.globalPane = this.window.getContentPane();
 		this.globalPane.setLayout(new BorderLayout());
 
 		this.menuPane = new Container();
 		this.menuPane.setPreferredSize(new Dimension(200, 400));
 		this.menuPane.setLayout(new BoxLayout(this.menuPane, BoxLayout.Y_AXIS));
 
 		this.gamePane = new Container();
 		this.gamePane.setLayout(new BoxLayout(this.gamePane, BoxLayout.Y_AXIS));
 		
 		this.globalPane.add(gamePane, BorderLayout.WEST);
 		this.globalPane.add(menuPane, BorderLayout.EAST);
 
 		initDisplay();
 		createMenu();
 
 
 		this.window.pack();
 	}
 	
 	private void createMenu() {
 		addButton(this.red, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.blue, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.green, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.yellow, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.black, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.white, this.menuPane, 0, 50, Component.CENTER_ALIGNMENT);
 		
 		addButton(this.valid, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.resetPawn, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.resetLine, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.newGame, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 		addButton(this.quit, this.menuPane, 0, 10, Component.CENTER_ALIGNMENT);
 
 		JLabel scores = new JLabel("SCORES");
 		JLabel CPU = new JLabel("CPU : 0");
 		JLabel player = new JLabel("Joueur : 0");
 		
 		scores.setFont(new Font(scores.getFont().getName(), Font.BOLD, 30));
 		CPU.setFont(new Font(CPU.getFont().getName(), Font.BOLD, 18));
 		player.setFont(new Font(player.getFont().getName(), Font.BOLD, 18));
 
 		scores.setAlignmentX(Component.CENTER_ALIGNMENT);
 		CPU.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		player.setAlignmentX(Component.RIGHT_ALIGNMENT);
 
 		this.menuPane.add(Box.createRigidArea(new Dimension(0, 100)));
 		this.menuPane.add(scores);
 		this.menuPane.add(Box.createRigidArea(new Dimension(0, 20)));
 		this.menuPane.add(CPU);
 		this.menuPane.add(player);
 		this.menuPane.setPreferredSize(new Dimension(300, this.menuPane.getPreferredSize().height));
 		
 	}
 
 	private void displayGame() {
 		this.gamePane.removeAll();
 		for (int i = 0; i < this.numberOfRow; ++i) {
 			Container c = new Container();
 			c.setLayout(new FlowLayout());
 			c.setPreferredSize(new Dimension(300, 50));
 
 			for (int j = 0; j < this.numberOfPawnPerRow; ++j)
 				addButton(this.rows.get(i)[j], c, 1, 20, 0);
 
 			JSeparator x = new JSeparator(SwingConstants.HORIZONTAL);
 			x.setPreferredSize(new Dimension(100, 3));
 
 			this.gamePane.add(x);
 			c.add(displayPawnSolution(this.numberOfPawnPerRow));
 			this.gamePane.add(c);
 		}
 		
 		Container c = new Container();
 		c.setLayout(new FlowLayout());
 		
 		JLabel solutionLabel = new JLabel("Solution :");
 		solutionLabel.setFont(new Font(solutionLabel.getFont().getName(), Font.BOLD, solutionLabel.getFont().getSize()));
 
 		JSeparator x = new JSeparator(SwingConstants.HORIZONTAL);
 		x.setPreferredSize(new Dimension(100, 3));
 
 		c.add(solutionLabel);
 		c.add(Box.createRigidArea(new Dimension(20, 20)));
 		
 		for (int j = 0; j < this.numberOfPawnPerRow; ++j)
 			addButton(new RoundButton(Color.GRAY, 10), c, 1, 20, 0);
 		
 		this.gamePane.add(x);
 		this.gamePane.add(c);
 	}
 
 	private void initDisplay() {
 		this.rows.clear();
 		for (int i = 0; i < this.numberOfRow; ++i) {
 			RoundButton[] tmp = new RoundButton[this.numberOfPawnPerRow];
 			for (int j = 0; j < this.numberOfPawnPerRow; ++j)
 				tmp[j] = new RoundButton(Color.GRAY, 10);
 			this.rows.add(tmp);
 		}
 		displayGame();
 	}
 
 	private Container displayPawnSolution(int pawn) {
 		Container c = new Container();
 		c.setLayout(new GridLayout(pawn / 2, 2, 5, 5));
 		c.setPreferredSize(new Dimension(pawn * 5, pawn * 5));
 
 		for (int i = 0; i < pawn; ++i)
 			c.add(new RoundButton(Color.black, 5));
 		return c;
 	}
 
 	private void addButton(JButton button, Container container, int vh, int space, float alignment) {
 		if (alignment > 0)
 			button.setAlignmentX(alignment);
 
 		container.add(button);
 
 		if (space > 0)
 			if (vh == 0)
 				container.add(Box.createRigidArea(new Dimension(0, space)));
 			else
 				container.add(Box.createRigidArea(new Dimension(space, 0)));
 	}
 
 	private static void createAndShowGUI() {
 		try {
 			GraphicUI gui = new GraphicUI();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				createAndShowGUI();
 			}
 		});
 	}
 
 	private void updateRow(Color c) {
 		if (this.currentPawn < 0)
 			this.currentPawn = 0;
 		if (this.currentPawn < this.numberOfPawnPerRow) {
 			this.rows.get(this.currentRow)[this.currentPawn].setBackground(c);
 			this.rows.get(this.currentRow)[this.currentPawn].setSize(20, 20);
 			this.currentPawn++;
 		}
 	}
 
 	private void resetLastPawn() {
		if (this.currentPawn > 0) {
 			this.currentPawn--;
 			this.rows.get(this.currentRow)[this.currentPawn].setBackground(Color.GRAY);
 			this.rows.get(this.currentRow)[this.currentPawn].setSize(10, 10);
 		}
 	}
 
 	private void resetLine() {
 		for (int i = 0; i < this.numberOfPawnPerRow; ++i)
 			resetLastPawn();
 	}
 
 	private void validation() {
 		if (this.currentRow < this.numberOfRow) {
 			this.currentRow++;
 			this.currentPawn = 0;
 		}
 	}
 	
 	private void resetAll() {
 		for(int i = 0; i < this.numberOfRow; ++i) {
 			for(int j = 0; j < this.numberOfPawnPerRow; ++j) {
 				this.rows.get(i)[j].setBackground(Color.GRAY);
 				this.rows.get(i)[j].setSize(10, 10);
 			}
 		}
 		this.currentPawn = 0;
 		this.currentRow = 0;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		Object o = ae.getSource();
 		if (o == this.resetPawn)
 			resetLastPawn();
 		else if (o == this.resetLine)
 			resetLine();
 		else if (o == this.quit)
 			System.exit(0);
 		else if (o == this.valid)
 			validation();
 		else if(o == this.newGame)
 			resetAll();
 		else {
 			RoundButton tmp = (RoundButton) ae.getSource();
 			updateRow(tmp.getBackground());
 		}
 	}
 }
