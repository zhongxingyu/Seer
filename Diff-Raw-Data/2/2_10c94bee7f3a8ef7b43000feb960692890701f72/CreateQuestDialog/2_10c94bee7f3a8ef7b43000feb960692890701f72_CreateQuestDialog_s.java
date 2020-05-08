 package net.skycraftmc.SkyQuest.utilitygui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import net.skycraftmc.SkyQuest.Quest;
 import net.skycraftmc.SkyQuest.Stage;
 
 @SuppressWarnings("serial")
 public class CreateQuestDialog extends JDialog implements DocumentListener, ActionListener
 {
 	SkyQuestUtility util;
 	QuestPanel qp;
 	private JTextField name;
 	private JTextField fsname;
 	private JTextField id;
 	private JButton create;
 	private JButton cancel;
 	public CreateQuestDialog(QuestPanel qp)
 	{
 		super(qp.util, "SkyQuest Utility - Create Quest", true);
 		this.qp = qp;
 		util = qp.util;
 		name = new JTextField();
 		fsname = new JTextField("stage1");
 		id = new JTextField();
 		create = new JButton("Create");
 		cancel = new JButton("Cancel");
 		setLayout(new BorderLayout());
 		JPanel p = new JPanel();
 		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
 		JPanel p2 = new JPanel();
 		p2.setLayout(new BorderLayout());
 		p2.add("Center", name);
 		p2.add("West", new JLabel("Quest Name"));
 		JPanel p3 = new JPanel();
 		p3.setLayout(new BorderLayout());
 		p3.add("Center", fsname);
 		p3.add("West", new JLabel("First Stage Name"));
 		JPanel p4 = new JPanel();
 		p4.setLayout(new BorderLayout());
 		p4.add("Center", id);
 		p4.add("West", new JLabel("Quest ID"));
 		p.add(p4);
 		p.add(p2);
 		p.add(p3);
 		add("Center", p);
 		JPanel btn = new JPanel();
 		btn.add(create);
 		btn.add(cancel);
 		add("South", btn);
 		setDefaultCloseOperation(HIDE_ON_CLOSE);
 		refresh();
 		create.addActionListener(this);
 		cancel.addActionListener(this);
 		id.getDocument().addDocumentListener(this);
 		name.getDocument().addDocumentListener(this);
 		fsname.getDocument().addDocumentListener(this);
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setBounds(d.width/4, d.height/4, d.width/2, d.height/2);
 		refresh();
 	}
 	
 	public void display()
 	{
 		setVisible(true);
 		refresh();
 	}
 	
 	private void refresh()
 	{
 		String n = name.getText();
 		String s = fsname.getText();
 		String i = id.getText();
 		if(i.trim().isEmpty() || n.trim().isEmpty() || s.trim().isEmpty())create.setEnabled(false);
		else if(i.contains(" ") || n.contains(" ") || s.contains(" "))create.setEnabled(false);
 		else if(util.qm.getQuest(i) != null)create.setEnabled(false);
 		else create.setEnabled(true);
 	}
 	public void changedUpdate(DocumentEvent arg0) 
 	{
 		refresh();
 	}
 	public void insertUpdate(DocumentEvent arg0)
 	{
 		refresh();
 	}
 	public void removeUpdate(DocumentEvent arg0) 
 	{
 		refresh();
 	}
 	public void actionPerformed(ActionEvent e) 
 	{
 		if(e.getSource() == create)
 		{
 			Quest q = new Quest(id.getText(), name.getText(), new Stage(fsname.getText()));
 			util.qm.addQuest(q);
 			qp.list.refreshList();
 			setVisible(false);
 			util.markFileChanged();
 			qp.delete.setEnabled(true);
 			qp.list.setSelectedValue(q, true);
 		}
 		else if(e.getSource() == cancel)
 		{
 			setVisible(false);
 		}
 	}
 }
