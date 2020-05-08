 package de.tud.kitchen.apps.eventinspector;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.util.HashSet;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTree;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import de.tud.kitchen.api.event.Event;
 import de.tud.kitchen.api.event.EventConsumer;
 import de.tud.kitchen.apps.eventinspector.DynamicEventFilter.DynamicEventFilterDelegate;
 
 public class EventWindow {
 
 	private JFrame frame;
 	private EventConsumer consumer;
 	private JTextArea eventTextPane;
 	private JTree tree;
 	private ClassTreeNode rootTreeNode;
 	private DynamicEventFilter dynamicEventFilter;
 	
 	/**
 	 * Create the application.
 	 */
 	public EventWindow() {
 		consumer = new DebugEventConsumer();
 		initialize();
 	}
 	
 	public JFrame getFrame() {
 		return frame;
 	}
 	
 	public EventConsumer getEventConsumer() {
 		return consumer;
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 634, 503);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane = new JScrollPane();
 		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
 		
 		eventTextPane = new JTextArea();
 		eventTextPane.setEditable(false);
 		scrollPane.setViewportView(eventTextPane);
 		
 		tree = new JTree();
 		tree.setPreferredSize(new Dimension(160, 76));
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
 		dynamicEventFilter = new DynamicEventFilter(new DynamicEventFilterDelegate() {
 			@Override
 			public void handleEvent(final Event event) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
						eventTextPane.append(event.toString().concat("\n"));
 					}
 				});
 			}
 		});
 		tree.getSelectionModel().addTreeSelectionListener(dynamicEventFilter);
 		rootTreeNode = new ClassTreeNode(Event.class);
 		tree.setModel(new DefaultTreeModel(rootTreeNode));
 		for (int i = 0; i < tree.getRowCount(); i++) {
 		         tree.expandRow(i);
 		}
 		
 		frame.getContentPane().add(tree, BorderLayout.EAST);
 	}
 	
 	public class DebugEventConsumer extends EventConsumer {
 		
 		private HashSet<Class<?>> seenClasses = new HashSet<Class<?>>();
 		
 		public void handleEvent(final Event event) {
 			if (seenClasses.add(event.getClass())) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						ClassTreeNode newTreeNode = new ClassTreeNode(event.getClass());
 						rootTreeNode.add(newTreeNode);
						if (newTreeNode.getParent()!= null)
							((DefaultTreeModel) tree.getModel()).nodeStructureChanged(newTreeNode.getParent());
						for (int i = 0; i < tree.getRowCount(); i++) {
					         tree.expandRow(i);
						}
 					}
 				});
 			}
 			dynamicEventFilter.handleEvent(event);
 				
 		}
 	}
 
 }
