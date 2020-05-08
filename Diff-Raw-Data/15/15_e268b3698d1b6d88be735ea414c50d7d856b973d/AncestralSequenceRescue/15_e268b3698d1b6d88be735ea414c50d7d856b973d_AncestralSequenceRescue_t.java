 package org.ithinktree.code.phylo.asr;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.ParallelGroup;
 import javax.swing.GroupLayout.SequentialGroup;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import dr.evolution.io.Importer.ImportException;
 import dr.evolution.io.NexusImporter;
 import dr.evolution.tree.NodeRef;
 import dr.evolution.tree.Tree;
 import dr.evolution.util.Taxon;
 
 public class AncestralSequenceRescue {
 
 	private static final String ASR = "Ancestral Sequence Rescue";
 	private static FileReader fr;
 	private static PrintStream os = System.out;
 	private static Tree t;
 	private static final Set<String> s = new HashSet<String>();
 	
 	/**
 	 * @param args
 	 * @throws ImportException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException, ImportException {
 		
 		final JFrame f;
 		
 		if (args.length < 3) {
 			
 			f = new JFrame();
 			f.setTitle(ASR);
 			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			GroupLayout gl = new GroupLayout(f.getContentPane());
 			f.getContentPane().setLayout(gl);
 			
 			Component[] ca = new Component[4];
 			
 			final JList l = new JList();
 			l.setLayoutOrientation(JList.VERTICAL);
 			JScrollPane sc = new JScrollPane(l);
 			
 			JPanel p = new JPanel();
 			JButton b = new JButton("Choose Annotated Tree File...");
 			final JTextField tf1 = new JTextField("no file selected", 16);
			tf1.setEditable(false);
 			b.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showOpenDialog(f) == JFileChooser.APPROVE_OPTION) {
 						try {
 							fr = new FileReader(fc.getSelectedFile());
 							t = new NexusImporter(fr).importNextTree();
 							fr.close();
 							l.setListData(t.asList().toArray());
 							l.repaint();
 						} catch (Exception e) {
 							JOptionPane.showMessageDialog(f, e.toString(), ASR, JOptionPane.ERROR_MESSAGE);
 							actionPerformed(ae);
 						}
 						tf1.setText(fc.getSelectedFile().getName());
 					}
 					
 				}
 			});
 			p.add(b);
 			p.add(tf1);
 			ca[0] = p;
 	
 			p = new JPanel();
 			b = new JButton("Choose Output Text File...");
 			final JTextField tf2 = new JTextField("no file selected", 16);
			tf2.setEditable(false);
 			b.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent ae) {
 					JFileChooser fc = new JFileChooser();
 					if (fc.showSaveDialog(f) == JFileChooser.APPROVE_OPTION) {
 						try {
 							os = new PrintStream(new FileOutputStream(fc.getSelectedFile() + ".txt"));
 						} catch (FileNotFoundException e) {
 							JOptionPane.showMessageDialog(f, e.toString(), ASR, JOptionPane.ERROR_MESSAGE);
 							actionPerformed(ae);
 						}
 						tf2.setText(fc.getSelectedFile().getName());
 					}
 					
 				}
 			});
 			p.add(b);
 			p.add(tf2);
 			ca[1] = p;
 
 			ca[2] = sc;
 			
 			p = new JPanel();
			b = new JButton("Quit");
 			b.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent ae) {
 					System.exit(0);
 				}
 			});
 			p.add(b);
 			b = new JButton("Rescue Sequences");
 			b.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(ActionEvent ae) {
 					for (Object o : l.getSelectedValues()) {
 						s.add(((Taxon) o).getId());
 					}
					rescueSequences();
					JOptionPane.showMessageDialog(f, "Rescue successful.", ASR, JOptionPane.INFORMATION_MESSAGE);
 				}
 			});
 			p.add(b);
 			ca[3] = p;
 			
 			ParallelGroup pg = gl.createParallelGroup(GroupLayout.Alignment.CENTER);
 			SequentialGroup sg = gl.createSequentialGroup();
 			for (int i = 0; i < ca.length; ++i) {
 				Component c = ca[i];
 				pg.addComponent(c);
 				sg.addComponent(c);
 			}
 			gl.setHorizontalGroup(gl.createSequentialGroup().addGroup(pg));
 			gl.setVerticalGroup(sg);
 			
 			f.pack();
 			f.setVisible(true);
 						
 		} else {
 			fr = new FileReader(args[0]);
 			t = new NexusImporter(fr).importNextTree();
 			fr.close();
 			for (int i = 1; i < args.length; ++i) s.add(args[i]);
 			rescueSequences();
			System.exit(0);
 		}
 		
 	}
 	
 	private static void rescueSequences() {
 		NodeRef n = Tree.Utils.getCommonAncestorNode(t, s);
 		@SuppressWarnings("rawtypes")
 		Iterator i = t.getNodeAttributeNames(n);
 		while (i.hasNext()) {
 			String a = (String) i.next();
 			Object o = t.getNodeAttribute(n, a);
 			String str;
 			if (o.getClass().getName() == "[Ljava.lang.Object;") {
 				str = Arrays.toString((Object[]) o);
 			} else {
 				str = o.toString();
 			}
 			os.println(a + ": " + str);
 		}
 		os.close();
 	}
 	
 }
