 import javax.swing.*;
 import javax.swing.event.*;
 
 import java.awt.*;
 import java.awt.color.ColorSpace;
 import java.awt.event.*;
 import java.util.*;
 
 public class GradingPanel extends JPanel {
 	String ID;
 	ArrayList<Item> items;
 	JScrollPane content;
 	JPanel main;
 
 	private class GradingButton implements ActionListener, DocumentListener{
 		JButton button;
 		Item item;
 		JTextField score;
 		JLabel total;
 		JButton comment;
 		String com;
 
 		public GradingButton(Item i) {
 			item = i;
 			button = new JButton("<html><center>"+item.getDescription()+"</center></html>");
 			button.setForeground(Color.black);
 			button.addActionListener(this);
 			score = new JTextField(2);
 			score.setText(""+item.getPointCur());
 			score.setEditable(false);
 			score.getDocument().addDocumentListener(this);
 			total = new JLabel("/" + i.getPointMax());
 
 			com = "";
 			comment = new JButton("Comment");
 			comment.setForeground(Color.gray);
 			comment.addActionListener(this);
 		}
 
 		public void actionPerformed(ActionEvent arg0) {
 			if (arg0.getSource().equals(button)) {
 				if (button.getForeground().equals(Color.red)) {
 					item.setPointCur(0 > item.getPointMax() ? 0 : item.getPointMax());
 					button.setForeground(Color.black);
 					score.setText(""+item.getPointCur());
 					score.setEditable(false);
 				}
 				else {
 					item.setPointCur(0 < item.getPointMax() ? 0 : item.getPointMax());
 					button.setForeground(Color.red);
 					score.setText(""+item.getPointCur());
 					score.setEditable(true);
 				}
 			}
 			else if (arg0.getSource().equals(comment)) {
 				String temp = JOptionPane.showInputDialog("Comment?:", com);
 				if (temp != null)
 					com = temp;
 				if (com != null && !com.isEmpty()) {
 					comment.setForeground(Color.black);
 					comment.setToolTipText(com);
 					item.setComment(com);
 				}
 				else {
 					comment.setForeground(Color.gray);
 					comment.setToolTipText("");
 					item.setComment("");
 				}
 			}
 		}
 
 		public void changedUpdate(DocumentEvent arg0) {
 
 		}
 
 		public int getScore() {
 			int sc = 0;
 			try {
 				sc = Integer.parseInt(score.getText());
 			}
 			catch (Exception e) {
 				return 0;
 			}
 			if (item.getPointMax() < 0) {
 				if (sc > 0)
 					sc = 0;
 				if (sc < item.getPointMax())
 					sc = item.getPointMax();
 				return sc;
 			}
 			if (sc < 0)
 				sc = 0;
 			if (sc > item.getPointMax())
 				sc = item.getPointMax();
 			return sc;
 		}
 
 		public void insertUpdate(DocumentEvent arg0) {
 			item.setPointCur(getScore());
 		}
 
 		public void removeUpdate(DocumentEvent arg0) {
 
 		}
 	}
 
 	GridBagConstraints c;
 
 	public GradingPanel(String I) {
 		ID = I;
 		main = new JPanel();
 		main.setLayout(new GridBagLayout());
 		c = new GridBagConstraints();
 		c.gridy = 1;
 		content = new JScrollPane(main);
 		add(content);
 
 		items = new ArrayList<Item>();
 
 		//		content.setPreferredSize(new Dimension(200, 200));
 	}
 
 	public void addItem(Item i) {
 		Item item = new Item(i.getPointMax(), i.getDescription(), i.getMilestone());
 		GradingButton button = new GradingButton(item);
 		items.add(item);
 		c.gridx = 1;
 		c.weightx = .85;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		main.add(button.button, c);
 		c.gridx = 2;
 		c.weightx = .05;
 		c.fill = GridBagConstraints.NONE;
 		main.add(button.score, c);
 		c.gridx = 3;
 		c.weightx = .05;
 		main.add(button.total, c);
 		c.gridx = 4;
 		c.weightx = .05;
 		main.add(button.comment, c);
 		c.gridy++;
 	}
 
 	//	public void update() {
 	//		//remove(content);
 	//
 	//		main.revalidate();
 	//		main.repaint();
 	//		//add(content);
 	//	}
 
 	public int addItems() {
 		int total = 0;
 		for (int i = 0; i < items.size(); i++)
 			total += items.get(i).getPointCur();
 		return total;
 	}
 
 	public String getErrors() {
 		String errors = "";
 		for (Item i:items) {
 			if (i.getPointMax() > 0 && i.getPointCur() < i.getPointMax()) {
 				if (i.getComment() != null && !i.getComment().isEmpty())
 					errors += ("  + (-" + (i.getPointMax()-i.getPointCur()) + ") - " + i.getDescription() + " - " + i.getComment()) + "\n";
 				else
 					errors += ("  + (-" + (i.getPointMax()-i.getPointCur()) + ") - " + i.getDescription()) + "\n";
 			}
 			else if (i.getPointMax() < 0 && i.getPointCur() < 0) {
 				if (i.getComment() != null && !i.getComment().isEmpty())
 					errors += ("  + (" + i.getPointCur() + " points) - "  + i.getDescription() + " - " + i.getComment() + "\n");
 				else
 					errors += ("  + (" + i.getPointCur() + " points) - " + i.getDescription() + "\n");
 			}
 			else if (i.getComment() != null && !i.getComment().isEmpty())
 				errors += "  + " + i.getDescription() + " - " + i.getComment() + "\n";
 		}
 		return errors;
 	}
 
 	public ArrayList<Item> getItems() {
 		return items;
 	}
 
 	public Item get(int index) {
 		return items.get(index);
 	}
 }
