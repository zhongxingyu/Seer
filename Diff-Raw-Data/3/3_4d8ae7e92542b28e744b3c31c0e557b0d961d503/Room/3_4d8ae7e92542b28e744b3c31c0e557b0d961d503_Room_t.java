 package ui;
 
 import java.util.Arrays;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.MovementEvent;
 import org.eclipse.swt.custom.MovementListener;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.grouplayout.GroupLayout;
 import org.eclipse.swt.layout.grouplayout.GroupLayout.ParallelGroup;
 import org.eclipse.swt.layout.grouplayout.GroupLayout.SequentialGroup;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import connection.Connection;
 
 public class Room extends Composite{
 
 	public static final int WHO=1, TOPIC=2, IO=4;
 
 	private Connection serverConnection;
 	
 	private Channel channel;
 	
 	//make clickable links by changing the style and the data of the individual messages
 	//http://eclipse.org/articles/StyledText%201/article1.html
 	private StyledText output, input, topicBox;
 	
 	private Tree who;
 	
 	private String topic;
 	
 	private int layout;
 	
 	public Room(Composite parent, int style, int layout){
 		super(parent, style);
 		this.layout = layout;
 	}
 	
 	public void instantiate(){
 		//get the parent composite ready to roll
 		Composite composite = new Composite(getServerConnection().getChanList(), SWT.NONE);
 		channel.getTabRef().setControl(composite);
 		
 		if((layout & TOPIC)!=0){
 			topicBox = new StyledText(composite, SWT.BORDER | SWT.WRAP);
 			topicBox.setEditable(false);
 		}
 		if((layout & IO)!=0){
 			//set up the output window
 			output = new StyledText(composite, SWT.BORDER | SWT.V_SCROLL | SWT.WRAP | SWT.MULTI);
 			output.setEditable(false);
 			output.addWordMovementListener(new MovementListener(){
 
 				@Override
 				public void getNextOffset(MovementEvent arg0) {
 					String[] message = arg0.lineText.split(" ");
 					for(String s : message){
 						if(arg0.lineText.indexOf(s)+s.length() < arg0.offset) continue;
 						if(s.contains("://") && arg0.lineText.indexOf(s) + s.length() > arg0.offset) {
 							Program.launch(s);
 						}
 					}
 				}
 
 				@Override
 				public void getPreviousOffset(MovementEvent arg0) {
 					
 				}});
 			
 			//set up the input box and it's enter-key listener
 			input = new StyledText(composite, SWT.BORDER);
 			input.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyPressed(KeyEvent e) {
 					//CR == Carriage Return == Enter
 					if(e.character == SWT.CR){
 						if(input.getText().startsWith("/")){
 							serverConnection.doCommand(input.getText().substring(1));
 						} else {
 							serverConnection.sendMessage(channel.getChannelName(), input.getText().replaceAll("\r\n", ""));
 						}
 						input.setText("");
 					}
 				}
 			});
 			
 		}
 
 		if((layout & WHO)!=0){
 			who = new Tree(composite, SWT.BORDER);
 			who.addListener(SWT.MouseDown, new Listener () {
 				public void handleEvent (Event event) {
 					Point point = new Point (event.x, event.y);
 					final TreeItem item = who.getItem (point);
 
 					if (item != null && item.getData()!=null && item.getData().equals(true)) {
 						Menu m = new Menu(item.getParent().getShell(), SWT.POP_UP);
 						MenuItem mitem = new MenuItem(m, SWT.PUSH);
 						mitem.setText("Query");
 						mitem.addSelectionListener(new SelectionListener() {
 
 							@Override
 							public void widgetDefaultSelected(
 									SelectionEvent arg0) {
 							}
 
 							@Override
 							public void widgetSelected(SelectionEvent arg0) {
 								serverConnection.createRoom(item.getText(), IO);	
 
 							}
 						});
 						item.getParent().setMenu(m);
 					}
 				}
 			});			
 			who.addListener(SWT.MouseDoubleClick, new Listener() {
 
 				@Override
 				public void handleEvent(Event event) {
 					Point point = new Point (event.x, event.y);
 					TreeItem item = who.getItem (point);
 					if(item!=null){
 						serverConnection.createRoom(item.getText(), IO);	
 					}	
 				}});
 
 		}
 		
 		//generate the anchors for the windows
 		GroupLayout gl_composite = new GroupLayout(composite);
 		
 		SequentialGroup ss = gl_composite.createSequentialGroup();
 		ParallelGroup p2 = gl_composite.createParallelGroup(GroupLayout.LEADING);
 
 		if((layout & TOPIC)!=0){
 			p2.add(topicBox, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		if((layout & IO)!=0){
 			p2.add(output, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 			p2.add(input, GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE);
 		}
 		ss.add(p2);
 		if((layout & WHO)!=0){
 			ss.add(who, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE);
 		}
         gl_composite.setHorizontalGroup(
                 gl_composite.createParallelGroup(GroupLayout.LEADING)
                         .add(ss)
         );
         
 		ParallelGroup p = gl_composite.createParallelGroup(GroupLayout.LEADING);
 		SequentialGroup s = gl_composite.createSequentialGroup();
 		
 		if((layout & TOPIC)!=0){
 			s.add(topicBox,GroupLayout.DEFAULT_SIZE, 15, Short.MAX_VALUE);
 		}
 		if((layout & IO)!=0){
 			if((layout & TOPIC)==0){
 				s.add(output, GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE);
 			} else {
 				s.add(output, GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE);
 			}
 			s.add(input);
 		}
 		p.add(s);
 		if((layout & WHO)!=0){
 			p.add(who, GroupLayout.DEFAULT_SIZE, 344, Short.MAX_VALUE);
 		}
 		gl_composite.setVerticalGroup(p);
 		
 		composite.setLayout(gl_composite);
 	}
 	
 	/**
 	 * @return the layout
 	 */
 	public int getIntLayout() {
 		return layout;
 	}
 
 	public void setOutput(StyledText output) {
 		this.output = output;
 	}
 
 	public StyledText getOutput() {
 		return output;
 	}
 
 	public void setInput(StyledText input) {
 		this.input = input;
 	}
 
 	public StyledText getInput() {
 		return input;
 	}
 
 	public void setWho(Tree who) {
 		this.who = who;
 	}
 
 	public Tree getWho() {
 		return who;
 	}
 
 	public void setChannelName(String channelName) {
 		this.channel.setChannelName(channelName);
 	}
 
 	public Channel getChannel() {
 		return channel;
 	}
 	
 	public void setChannel(Channel c){
 		this.channel = c;
 	}
 
 	public void setServerConnection(Connection serverConnection) {
 		this.serverConnection = serverConnection;
 	}
 
 	public Connection getServerConnection() {
 		return serverConnection;
 	}
 	
 	public void setTopic(String topic) {
 		this.topic = topic;
 		topicBox.setText(topic);
 		topicBox.setToolTipText(topic);
 	}
 	
 	public String getTopic() {
 		return topic;
 	}
 	
 }
