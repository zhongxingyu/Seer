 package client.gui.panels;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.text.SimpleDateFormat;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.ListCellRenderer;
 
 import client.model.NotificationModel;
 import client.model.NotificationType;
 
 public class NotificationListCellRenderer implements ListCellRenderer {
 	
 	public final static char aa = 229, oe = 248;
 
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
 		NotificationModel model = (NotificationModel)value;
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM HH:mm");
 		Color textColor = model.isRead() ? Color.LIGHT_GRAY : Color.BLACK;
 		Color bgColor = isSelected ? new Color(100, 100, 255) : Color.WHITE;
 
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		panel.setBackground(bgColor);
 
 		Font f = new Font("Sans serif", Font.PLAIN, 10);
 
 		JLabel timeStamp = new JLabel(sdf.format(model.getTime().getTime()) + "  ");
 		timeStamp.setFont(f);
 		timeStamp.setForeground(textColor);
 		timeStamp.setBackground(bgColor);
 		timeStamp.setAlignmentX(Component.LEFT_ALIGNMENT);
 		timeStamp.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel.add(timeStamp);
 
 		panel.add(Box.createHorizontalGlue());
 
 		JTextArea text = new JTextArea();
 		text.setFont(f);
 		text.setBackground(bgColor);
 		text.setForeground(textColor);
 		text.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		text.setAlignmentY(Component.TOP_ALIGNMENT);
 		text.setLineWrap(true);
 		text.setWrapStyleWord(true);
		text.setSize(new Dimension(100,1));
 //		text.setText("testing tekst som er veldig veldig veldig veldig veldig veldig veldig veldig lang");
 		text.setText(getNotificationText(model));
 		panel.add(text);
 
 		return panel;
 	}
 	
 	private String getNotificationText(NotificationModel model) {
 		String text = "";
 		SimpleDateFormat time = new SimpleDateFormat("HH:mm");
 		SimpleDateFormat date = new SimpleDateFormat("dd/MM");
 		if (model.getType() == NotificationType.A_CANCELED) {
 			text = model.getRegardsUser().getFullName() +
 					" har avlyst m" + oe + "tet";
 			if (model.getRegardsMeeting().getName() != null)
 				text += " med navn \'" + model.getRegardsMeeting().getName() + "\'";
 			text += " kl " + time.format(model.getRegardsMeeting().getTimeFrom().getTime());
 			text += " den " + date.format(model.getRegardsMeeting().getTimeFrom().getTime());
 		} else if (model.getType() == NotificationType.A_EDITED) {
 			text = model.getRegardsUser().getFullName() +
 					" har redigert detaljer i";
 			if (model.getRegardsMeeting().getName() != null)
 				text += " m" + oe + "tet med navn \'" + model.getRegardsMeeting().getName() + "\'";
 			else
 				text += " et m" + oe + "te dere har ";
 			text += " kl " + time.format(model.getRegardsMeeting().getTimeFrom().getTime());
 			text += " den " + date.format(model.getRegardsMeeting().getTimeFrom().getTime());
 			text += ". Dobbeltklikk for " + aa + " se detaljer.";
 		} else if (model.getType() == NotificationType.A_INVITATION) {
 			text = model.getRegardsUser().getFullName() + 
 					" har invitert deg til et m" + oe + "te";
 			if (model.getRegardsMeeting().getName() != null)
 				text += " med navn \'" + model.getRegardsMeeting().getName() + "\'";
 			text +=	" fra kl " + time.format(model.getRegardsMeeting().getTimeFrom().getTime()) +
 					" til kl " + time.format(model.getRegardsMeeting().getTimeTo().getTime()) +
 					" den " + date.format(model.getRegardsMeeting().getTimeFrom().getTime()) +
 					". Dobbeltklikk for " + aa + " se detaljer.";
 		} else if (model.getType() == NotificationType.A_USER_DENIED) {
 			text += model.getRegardsUser().getFullName() +
 					" har avsl" + aa + "tt invitasjon til";
 			if (model.getRegardsMeeting().getName() != null)
 				text += " m" + oe + "tet med navn \'" + model.getRegardsMeeting().getName() + "\'";
 			else
 				text += " et m" + oe +  "te dere har ";
 			text += " kl " + time.format(model.getRegardsMeeting().getTimeFrom().getTime());
 			text += " den " + date.format(model.getRegardsMeeting().getTimeFrom().getTime());
 			text += ". Dobbeltklikk for " + aa + " redigere m" + oe + "tedetaljer.";
 		}
 		
 		return text;
 	}
 
 }
