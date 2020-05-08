 import javax.swing.JSplitPane;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import java.awt.BorderLayout;
 import javax.swing.ListModel;
 import javax.swing.AbstractListModel;
 import javax.swing.event.ListDataListener;
 import javax.swing.JTextField;
 
 import client.*;
 
 public class ChannelWindow extends ChatWindowAbstract {
 	
 	private client.Channel channel;
 	private ChannelListModel list;
 	private JTextField topic;
 
 	public ChannelWindow(String channel_name, client.SyncManager sync) {
 		super(channel_name, ChannelWindow.Type.CHANNEL);
 
 		channel = sync.getChannel(getName());
 
 		list = new ChannelListModel();	
 
 		channel.addChannelListener( channelListener );
 
 
 
 		setLayout(new BorderLayout());
 
 		//a channel window is a split pane...
 		JSplitPane chan = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT ) ;
 
 		//put the console on the left...
 		chan.setLeftComponent( console );
 
 		//the user list on the right.
		//
		 
		chan.setRightComponent( new JScrollPane( new JList(list) ) );
 		
 
 		//favor the left side???
 		chan.setResizeWeight(1.0);
 
 
 		add(chan,BorderLayout.CENTER);
 
 		//@TODO topic
 		add(topic = new JTextField(channel.getTopic()), BorderLayout.NORTH);
 	}
 
 	private class ChannelListModel extends AbstractListModel {
 		public int getSize() {
 			return channel.numUsers();
 		}
 		public Object getElementAt(int idx) {
 			return channel.getUser(idx);
 		}
 		private void update() {
 			fireContentsChanged( this, 0, channel.numUsers() - 1);
 
 		}
 	};
 
 	public ChannelListener channelListener = new ChannelAdapter() {
 		public void topicChanged(Channel c) {
 			topic.setText(c.getTopic());
 		}
 
 		public void usersChanged(Channel c) {
 			list.update();
 
 		}
 	};
 }
