 package twitz.ui.renderers;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.util.ArrayList;
 import java.util.regex.*;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.ListCellRenderer;
 import org.jdesktop.application.ResourceMap;
 import twitter4j.Status;
 import twitter4j.User;
 import twitz.testing.*;
 import org.pushingpixels.substance.api.*;
 import org.pushingpixels.substance.api.renderers.*;
 import twitz.TwitzApp;
 import twitz.TwitzMainView;
 
 public class ContactsRenderer extends SubstanceDefaultListCellRenderer {
 	
 	public ContactsRenderer()
 	{
 		super();
 		this.putClientProperty(SubstanceLookAndFeel.COLORIZATION_FACTOR, 1.0);
 	}
 	
 	@Override
 	public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus)
 	{
 		super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
 		User u = (User)value;
 		java.net.URL imgURI = u.getProfileImageURL();
 		javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgURI);
 		int status = icon.getImageLoadStatus();
 		Image img = null;
 		if(status == MediaTracker.ERRORED) {
 			ResourceMap res = TwitzApp.getContext().getResourceMap(TwitzMainView.class);
 			icon = res.getImageIcon("icon.user_gray");
 			img = icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
 		}
 		else {
 			img = icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
 		}
 		//Image img = icon.getImage().getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH);
 		icon = new ImageIcon(img);
 		Status s = u.getStatus();
 		setText("<html><b>"+u.getScreenName()+"</b>");
 		setIcon(icon);
 		if(list instanceof twitz.ui.ContactsList)
 			setToolTipText("<html>"+tableWrap(u, s, 250));
 		else
 			setToolTipText(u.getScreenName());
 		setVerticalAlignment(TOP);
 		setFont(new Font("Arial", Font.BOLD, 10));
 		return this;
 	}
 
 	private String tableWrap(User u, Status s, int width) //{{{
 	{
 		StringBuffer table = new StringBuffer("<table border=0 width=");
 		table.append(width+"");
 		table.append("><tr align='center' ><td><b>");
 		table.append(u.getScreenName());
		table.append("</b></td></tr><tr><td>");
		table.append(pretify(s.getText()));
		table.append("</td></tr>");
 		if(u.getLocation() != null)
 		{
 			table.append("<tr><td>From: "+u.getLocation()+"</td></tr>");
 		}
 		table.append("</table>");
 		return table.toString();
 	} //}}}
 
 	private String pretify(String text)//{{{
 	{
 		ArrayList<String> list = new ArrayList<String>();
 		String rv = "";
 		String left = "<strong><font color=\"blue\">";
 		String right = "</font></strong>";
 		Pattern p = Pattern.compile("([#@]\\w+?)\\s");
 		Matcher m = p.matcher(text);
 		while(m.find())
 		{
 			list.add(m.group());
 		}
 		String t = text;
 		for(String s:list)
 		{
 			t = t.replaceAll(s, left+s+right);
 		}
 		rv = t;
 		return rv;
 	} //}}}
 }
