 package gui;
 
 import java.awt.BorderLayout;
import java.awt.Dimension;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.border.EmptyBorder;
 
 import util.ArrayUtil;
 
 import com.jgoodies.forms.factories.ButtonBarFactory;
 
 public class CheckBoxSelectDialog extends JDialog
 {
 	protected boolean okPressed = false;
 
 	MouseOverCheckBoxList list;
 	DefaultListModel listModel;
 	MouseOverCheckBoxListComponent listPanel;
 
 	private CheckBoxSelectDialog(Window owner, String title, String description, Object[] values, boolean allSelected)
 	{
 		super(owner, title);
 		boolean selection[] = new boolean[values.length];
 		Arrays.fill(selection, allSelected);
 		init(owner, description, values, selection);
 	}
 
 	private CheckBoxSelectDialog(Window owner, String title, String description, Object[] values, boolean selection[])
 	{
 		super(owner, title);
 		init(owner, description, values, selection);
 	}
 
 	private void init(Window owner, String description, Object[] values, boolean selection[])
 	{
 		setModal(true);
 		buildLayout(description);
 
 		for (Object o : values)
 			listModel.addElement(o);
 
 		List<Integer> selected = new ArrayList<Integer>();
 		for (int i = 0; i < selection.length; i++)
 			if (selection[i])
 				selected.add(i);
 		list.getCheckBoxSelection().setSelectedIndices(ArrayUtil.toPrimitiveIntArray(selected));
 		//System.out.println("selected: " + ArrayUtil.toString(list.getCheckBoxSelection().getSelectedIndices()));
 
		pack();
 		setLocationRelativeTo(owner);
 		setVisible(true);
 	}
 
 	private void buildLayout(String description)
 	{
 		JTextArea d = new JTextArea(description);
 		d.setEditable(false);
 		d.setOpaque(false);
 		d.setBorder(null);
 		d.setWrapStyleWord(true);
 		d.setLineWrap(true);
 
		//text-area hack to prevent pref-size to be sth like 0,2500 
		d.setPreferredSize(null);
		d.setSize(new Dimension(200, Integer.MAX_VALUE));

 		listModel = new DefaultListModel();
 		list = new MouseOverCheckBoxList(listModel);
 		list.setClearOnExit(false);
 		listPanel = new MouseOverCheckBoxListComponent(list);
 
 		final JButton ok = new JButton("OK");
 		JButton cancel = new JButton("Cancel");
 		ActionListener l = new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				okPressed = (e.getSource() == ok);
 				CheckBoxSelectDialog.this.setVisible(false);
 			}
 		};
 		ok.addActionListener(l);
 		cancel.addActionListener(l);
 		JPanel buttons = ButtonBarFactory.buildOKCancelBar(ok, cancel);
 
 		JPanel p = new JPanel(new BorderLayout(0, 10));
 		p.add(d, BorderLayout.NORTH);
 		p.add(listPanel);
 		p.add(buttons, BorderLayout.SOUTH);
 		p.setBorder(new EmptyBorder(10, 10, 10, 10));
 		getContentPane().add(p);
 	}
 
 	public static int[] select(Window owner, String title, String info, Object[] values, boolean allSelected)
 	{
 		CheckBoxSelectDialog d = new CheckBoxSelectDialog(owner, title, info, values, allSelected);
 		if (d.okPressed)
 			return d.list.getCheckBoxSelection().getSelectedIndices();
 		else
 			return null;
 	}
 
 	public static int[] select(Window owner, String title, String info, Object[] values, boolean selected[])
 	{
 		CheckBoxSelectDialog d = new CheckBoxSelectDialog(owner, title, info, values, selected);
 		if (d.okPressed)
 			return d.list.getCheckBoxSelection().getSelectedIndices();
 		else
 			return null;
 	}
 
 	public static void main(String args[])
 	{
 		String s[] = { "ene", "mene", "miste" };
 		int ss[] = CheckBoxSelectDialog.select(null, "test-select-dialog", "description text", s, true);
 		if (ss != null)
 			System.out.println(ArrayUtil.toString(ss));
 	}
 }
