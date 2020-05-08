 package ui;
 
 import java.awt.Component;
 import java.awt.Font;
 
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.LayoutStyle;
 import javax.swing.ListCellRenderer;
 
 import twitter4j.Status;
 import twitter4j.User;
 
 public class StatusCellRenderer implements ListCellRenderer
 {
 	private static final long	serialVersionUID	= 1L;
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFoucs)
 	{
 		JPanel panel = new JPanel();
 		final Status tweet = (Status) value;
 		final User user = tweet.getUser();
 
 		JLabel icon = new JLabel();
 		JLabel name = new JLabel();
 		JLabel date = new JLabel();
 		JTextArea text = new JTextArea();
 
 		icon.setIcon(new ImageIcon(ImageCache.get(user.getProfileImageURL())));
 		name.setText(user.getName());
 
 		date.setFont(new Font("Lucida Grande", 0, 11)); // NOI18N
 		date.setText(tweet.getCreatedAt().toString());
 
 		text.setColumns(20);
 		text.setLineWrap(true);
 		text.setRows(5);
 		text.setText(tweet.getText());
 		text.setWrapStyleWord(true);
 
 		GroupLayout layout = new GroupLayout(panel);
 		panel.setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
 				layout.createSequentialGroup().addContainerGap().addGroup(
 						layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(text, GroupLayout.DEFAULT_SIZE, 360, Short.MAX_VALUE).addGroup(
 								layout.createSequentialGroup().addComponent(icon).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addGroup(
 										layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(name).addComponent(date)))).addContainerGap()));
 		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(
 				layout.createSequentialGroup().addContainerGap().addGroup(
 						layout.createParallelGroup(GroupLayout.Alignment.BASELINE).addComponent(icon).addComponent(name)).addPreferredGap(
 						LayoutStyle.ComponentPlacement.RELATED).addComponent(text, GroupLayout.PREFERRED_SIZE, 55, GroupLayout.PREFERRED_SIZE).addContainerGap(
 						169, Short.MAX_VALUE)).addGroup(GroupLayout.Alignment.TRAILING,
				layout.createSequentialGroup().addContainerGap(55, Short.MAX_VALUE).addComponent(date)));
 
 		return panel;
 	}
 }
