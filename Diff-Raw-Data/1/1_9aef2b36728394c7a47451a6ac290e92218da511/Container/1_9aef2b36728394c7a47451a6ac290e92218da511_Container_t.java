 package ca.wlu.gisql.cytoscape;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTree;
 import javax.swing.tree.TreeSelectionModel;
 
 import ca.wlu.gisql.ast.type.Type;
 import ca.wlu.gisql.environment.UserEnvironment;
 import ca.wlu.gisql.gui.CommandBox;
 import ca.wlu.gisql.gui.SwingThreadBouncer;
 import ca.wlu.gisql.gui.util.EnvironmentTreeView;
 import ca.wlu.gisql.gui.util.InteractomeTreeCellRender;
 import ca.wlu.gisql.interactome.Interactome;
 import ca.wlu.gisql.runner.ExpressionError;
 import ca.wlu.gisql.runner.ExpressionRunListener;
 import ca.wlu.gisql.runner.ThreadedExpressionRunner;
 
 public class Container extends JPanel implements ActionListener,
 		ExpressionRunListener {
 
 	private static final long serialVersionUID = 9200554954420289191L;
 
 	private final CommandBox command;
 
 	private final EnvironmentTreeView environmentTree;
 
 	private final ThreadedExpressionRunner runner;
 
 	private final JTree variablelist;
 
 	private final JScrollPane variablelistPane;
 
 	public Container(UserEnvironment environment) {
 		super();
 		runner = new ThreadedExpressionRunner(environment,
 				new SwingThreadBouncer(this));
		runner.start();
 		environmentTree = new EnvironmentTreeView(environment);
 		variablelist = new JTree(environmentTree);
 		variablelist.setCellRenderer(new InteractomeTreeCellRender());
 		variablelist.getSelectionModel().setSelectionMode(
 				TreeSelectionModel.SINGLE_TREE_SELECTION);
 		variablelistPane = new JScrollPane(variablelist);
 
 		command = new CommandBox();
 		command.setActionListener(this);
 
 		setLayout(new BorderLayout());
 		add(command, BorderLayout.SOUTH);
 		add(variablelistPane, BorderLayout.CENTER);
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == command) {
 			runner.run(command.getText(), null);
 		}
 	}
 
 	@Override
 	public void processInteractome(Interactome value) {
 
 	}
 
 	@Override
 	public void processOther(Type type, Object value) {
 		JOptionPane.showMessageDialog(this, value.toString() + " :: "
 				+ type.toString(), "Result - gisQL",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	@Override
 	public void reportErrors(Collection<ExpressionError> errors) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
