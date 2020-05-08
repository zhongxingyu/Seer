 package org.jhotdraw.samples.svg;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.DelayQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 
 import org.jhotdraw.draw.DefaultDrawingView;
 import org.jhotdraw.samples.svg.figures.Delete;
 import org.jhotdraw.util.PicsGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lorent.whiteboard.client.Client;
 import com.lorent.whiteboard.command.ClientRunnable;
 import com.lorent.whiteboard.command.DelayedMeetingCommandAdaptor;
 import com.lorent.whiteboard.command.ResultCommand;
 import com.lorent.whiteboard.command.impl.BroadcastCloseWhiteboardCommand;
 import com.lorent.whiteboard.command.impl.BroadcastCommand;
 import com.lorent.whiteboard.command.impl.BroadcastStateCommand;
 import com.lorent.whiteboard.context.AppContext;
 import com.lorent.whiteboard.model.CommandsManager;
 import com.lorent.whiteboard.model.Sendable;
 import com.lorent.whiteboard.model.Updater;
 import com.lorent.whiteboard.model.View;
 
 public class SVGPanels implements CommandsManager{
 	private static final Logger logger = LoggerFactory.getLogger(SVGPanels.class);
 	
 	private final ConcurrentHashMap<String,SVGPanel> panels=new ConcurrentHashMap<String,SVGPanel>();
 	
 	private final ConcurrentHashMap<String,PicsGenerator> picsGenerator=new ConcurrentHashMap<String,PicsGenerator>();
 	
 	private final AtomicInteger viewId =new AtomicInteger();
 	
 	private final DelayQueue<BroadcastCommand> resendCommandQueue=new DelayQueue<BroadcastCommand>();
 	
 	private volatile Client client;
 	
 	private volatile JTabbedPane parent;
 	
 	private volatile String ip;
 	
 	private volatile boolean writable;
 	
 	private static volatile SVGPanels instance;
 	
 	private ConcurrentHashMap<Object,Object> attributes=new ConcurrentHashMap<Object, Object>();
 	
 	private SVGPanels(){}
 	
 	public static SVGPanels getInstance(){
 		if(instance==null){
 			synchronized(SVGPanels.class){
 				if(instance!=null){
 					return instance;
 				}
 				try {
 					instance.wait();
 				} catch (InterruptedException e) {
 					throw new IllegalStateException("instance",e);
 				}
 			}
 		}
 		return instance;
 	}
 	
 	public synchronized static SVGPanels newInstance(String ip,String meetingId,JTabbedPane parent,boolean writable){
 		if(parent==null)
 			throw new IllegalArgumentException("parent == null!");
 		if(instance==null){
 			instance=new SVGPanels();
 		}else{
 			instance.client.close(true);
 			instance=new SVGPanels();
 		}
 		instance.parent=parent;
 		instance.ip=ip;
 		instance.writable=writable;
 		try{
 			instance.client = Client.newInstance(ip,instance);
 			AppContext.put(Sendable.class, instance.client);
 			instance.client.attend(meetingId);
 		}catch(RuntimeException e){
 			logger.warn("Please check the WhiteBoard Server is Serving!",e);
 		}
 		SVGPanels.class.notifyAll();
 		return instance;
 	}
 	
 	public SVGPanel createPanel(){
 		synchronized(panels){
 			SVGPanel svgPanel = new SVGPanel(writable);
 			SVGPanel repeat;
 			do{
 				String vid = String.valueOf(viewId.incrementAndGet());
 				svgPanel.getView().setId(vid);
 				repeat = panels.putIfAbsent(vid,svgPanel);
 			}while(repeat!=null);
 			addToParent(svgPanel);
 			return svgPanel;
 		}
 	}
 	
 	public SVGPanel createPanel(String vid){
 		synchronized(panels){
 			if (vid == null)
 				throw new IllegalArgumentException("vid == null!");
 			SVGPanel svgPanel;
 			if(vid.indexOf('.')!=-1){
 				svgPanel = new SVGPanel(true,writable);
 			}else{
 				svgPanel = new SVGPanel(writable);
 			}
 			svgPanel.getView().setId(vid);
 			SVGPanel repeat = panels.putIfAbsent(vid,svgPanel);
 			if(repeat!=null){
 				return repeat;
 			}
 			addToParent(svgPanel);
 			return svgPanel;
 		}
 	}
 	
 	public SVGPanel createPanel(String vid,long commandId){
 		synchronized(panels){
 			if (vid == null)
 				throw new IllegalArgumentException("vid == null!");
 			SVGPanel svgPanel;
 			if(vid.indexOf('.')!=-1){
 				svgPanel = new SVGPanel(true,writable);
 			}else{
 				svgPanel = new SVGPanel(writable);
 			}
 			DefaultDrawingView view = svgPanel.getView();
 			view.setId(vid);
 			view.setCommandId(commandId);
 			SVGPanel repeat = panels.putIfAbsent(vid,svgPanel);
 			if(repeat!=null){
 				return repeat;
 			}
 			addToParent(svgPanel);
 			return svgPanel;
 		}
 	}
 	
 	private void addToParent(final SVGPanel panel){
 		if (parent != null) {
 			SwingUtilities.invokeLater(new Runnable() {
 
 				@Override
 				public void run() {
 					DefaultDrawingView view = panel.getView();
 					if(view.isDoc())
 						parent.addTab("画板:" + getSimpleFileName(view.getId()), panel);
 					else
 						parent.addTab("画板:" + view.getId(), panel);
 					int i = parent.indexOfComponent(panel);
 					if(writable)
 						new CloseTabButton(parent, i);
 				}
 			});
 		}
 	}
 	
 	public static String getSimpleFileName(String fileName){
 		int index=fileName.indexOf('_');
 		if(index!=-1)
 			return new String(fileName.substring(index+1));
 		return fileName;
 	}
 	
 	@Override
 	public void execute(BroadcastCommand command) {
 		View view = getView(command.getWhiteboardId());
 		boolean result = view.execute(command);
 		if(result){
 			instance.client.resetRemaingingTime();
 		}
 	}
 	
 	@Override
 	public void execute(ClientRunnable message) {
 		((ClientRunnable) message).run(this);
 	}
 
 	@Override
 	public void execute(ResultCommand message) {
 		((ResultCommand) message).subsequentlyRun(this);
 	}
 
 	@Override
 	public View getView(String id){
 		SVGPanel panel = panels.get(id);
 		if(panel==null){
 			SVGPanel opanel = createPanel(id);
 			if(opanel!=null){
 				panel=opanel;
 			}
 		}
 		return panel.getView();
 	}
 	
 	@Override
 	public void acceptState(BroadcastStateCommand command) {
 		View view = getView(command.getWhiteboardId());
 		boolean result = view.acceptState(command);
 		if(result){
 			instance.client.resetRemaingingTime();
 		}
 	}
 	
 	public String getIp() {
 		return ip;
 	}
 
 	@Override
 	public boolean acceptCommandId(String viewId,long commandId) {
 		boolean result;
 		do{
 			View view = getView(viewId);
 			result = view.acceptCommandId(commandId);
 			if(!result){
 				long futureCommandId = view.getFutureCommandId();
 				if(futureCommandId>=commandId){
 					logger.warn("{} drop {},futureCommandId is {}",new Object[]{viewId,commandId,futureCommandId});
 					break;
 				}
 				logger.warn("{} dont accept {},futureCommandId is {}",new Object[]{viewId,commandId,futureCommandId});
 			}else{
 				logger.debug("{} accepted {}",viewId,commandId);
 				instance.client.resetRemaingingTime();
 				return true;
 			}
 		}while(!result);
 		return false;
 	}
 
 	@Override
 	public long getFutureCommandId(String viewId) {
 		return getView(viewId).getFutureCommandId();
 	}
 	
 	@Override
 	public HashMap<String, Long> getFutureCommandIds() {
 		HashMap<String, Long> ids = new HashMap<String,Long>();
 		for(Entry<String, SVGPanel> entry:panels.entrySet()){
 			SVGPanel p = entry.getValue();
 			if(p!=null){
 				long futureCommandId = p.getView().getFutureCommandId();
 				if(futureCommandId==1)
 					continue;
 				ids.put(entry.getKey(),futureCommandId);
 			}
 		}
 		return ids;
 	}
 	
 	@Override
 	public Map<String, Map<Integer, Long>> getStateCommandIds() {
 		Map<String, Map<Integer, Long>> ids = new HashMap<String,Map<Integer,Long>>();
 		for(Entry<String, SVGPanel> entry:panels.entrySet()){
 			SVGPanel p = entry.getValue();
 			if(p!=null){
 				Map<Integer, Long> stateCommandIds = p.getView().getStateCommandIds();
 				if(stateCommandIds.isEmpty())
 					continue;
 				ids.put(entry.getKey(),stateCommandIds);
 			}
 		}
 		return ids;
 	}
 
 	@Override
 	public BroadcastCommand getBroadcastCommand(String whiteboardId,long id){
 		SVGPanel svgPanel = panels.get(whiteboardId);
 		if(svgPanel==null)
 			return null;
 		return svgPanel.getCommand(id);
 	}
 	
 	@Override
 	public BroadcastCommand broadcastSuccessed(String whiteboardId,long id){
 		SVGPanel svgPanel = panels.get(whiteboardId);
 		if(svgPanel==null)
 			return null;
 		return svgPanel.removeCommand(id);
 	}
 
 	@Override
 	public BroadcastCommand getMissingCommand() throws InterruptedException{
 		while(true){
 			BroadcastCommand missing = resendCommandQueue.take();
 			SVGPanel svgPanel = panels.get(missing.getWhiteboardId());
 			if(svgPanel==null)
 				continue;
 			if(svgPanel.containsCommand(missing)){
 				missing.addToQueue(resendCommandQueue);
 				return missing;
 			}
 		}
 	}
 	
 
 	@Override
 	public void clearMissingCommands() {
 		resendCommandQueue.clear();
 	}
 
 	@Override
 	public BroadcastCommand create(String meetingId, Updater<?> updater,View view) {
 		if(view==null){
 			throw new IllegalArgumentException("view == null!");
 		}
 		long cid = view.getFutureCommandId();
 		BroadcastCommand command = new BroadcastCommand(meetingId, updater, cid,view.getId(),view.getPage());
 		
 		save(cid, command);
 		
 		addToResendQueue(command);
 		
 		if(command.getUpdater().getClass().isAssignableFrom(Delete.class)){
 			view.addCommandToPage(view.getPage(), command);
 		}
 		return command;
 	}
 	
 	@Override
 	public BroadcastStateCommand createState(String meetingId,
 			Updater<?> updater, View view,Integer stateType) {
 		if(view==null){
 			throw new IllegalArgumentException("view == null!");
 		}
 		long cid = view.getFutureCommandId();
 		BroadcastStateCommand command = new BroadcastStateCommand(meetingId,view.getId(),cid,stateType,updater);
 		
 		return command;
 	}
 	
 	private void save(long cid,BroadcastCommand command){
 		ConcurrentHashMap<Long, BroadcastCommand> commands = panels.get(command.getWhiteboardId()).getCommands();
 		while(commands.putIfAbsent(cid, command)!=null){
 			cid++;
 		}
 		command.setCommandId(cid);
 	}
 	
 	@Override
 	public BroadcastCloseWhiteboardCommand createCloseWhiteboardCommand(
 			String meetingId, String whiteboardId) {
 		long cid = getView(whiteboardId).getFutureCommandId();
 		BroadcastCloseWhiteboardCommand command = new BroadcastCloseWhiteboardCommand(meetingId, whiteboardId);
 		
 		save(cid, command);
 		
 		addToResendQueue(command);
 		
 		final SVGPanel svgPanel = panels.get(whiteboardId);
 		
 		SwingUtilities.invokeLater(new Runnable(){
 			@Override
 			public void run() {
 				parent.remove(svgPanel);
 				logger.debug("removed whiteboard {}",svgPanel.getView().getId());
 			}
 		});
 		return command;
 	}
 	
 	private void addToResendQueue(DelayedMeetingCommandAdaptor c){
 		c.addToQueue(resendCommandQueue);
 	}
 	
 	public void synced(){
 		client.hideGlassPanel();
 	}
 
 	public Client getSendable() {
 		return client;
 	}
 	
 	public PicsGenerator registerPicsGenerator(String id,PicsGenerator picsGenerator){
 		return this.picsGenerator.putIfAbsent(id, picsGenerator);
 	}
 	
 	public PicsGenerator getPicsGenerator(String id){
 		return picsGenerator.get(id);
 	}
 	
 	public void removePicsGenerator(String id){
 		picsGenerator.remove(id);
 	}
 	
 	public void close(){
 		removeAll();
 		instance.client.close(true);
 		instance=null;
 	}
 	
 	@Override
 	public void remove(final String vid){
 		synchronized(panels){
 			final SVGPanel svgPanel = panels.remove(vid);
 			if(svgPanel==null)
 				return;
 			svgPanel.getView().dispose();
 			logger.debug("removing whiteboard {}",vid);
 			SwingUtilities.invokeLater(new Runnable(){
 				@Override
 				public void run() {
 					parent.remove(svgPanel);
 					logger.debug("removed whiteboard {}",vid);
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void removeAll(){
 		synchronized(panels){
 			for(SVGPanel panel:panels.values()){
 				panel.getView().dispose();
 			}
 			panels.clear();
 			logger.debug("removing all whiteboards");
 			SwingUtilities.invokeLater(new Runnable(){
 				@Override
 				public void run() {
 					parent.removeAll();
 					logger.debug("removed all whiteboards");
 				}
 			});
 		}
 	}
 	
 	public boolean isEmpty(){
 		return panels.isEmpty();
 	}
 	
 	public boolean isWritable() {
		return writable;
 	}
 
 	@Override
 	public JTabbedPane getContainer() {
 		return parent;
 	}
 	
 	@Override
 	public Object putAttribute(Object key, Object value) {
 		return attributes.put(key, value);
 	}
 	
 	@Override
 	public Object putAttributeIfAbsent(Object key, Object value) {
 		return attributes.putIfAbsent(key, value);
 	}
 
 	@Override
 	public Object getAttribute(Object key) {
 		return attributes.get(key);
 	}
 
 	@Override
 	public Object removeAttribute(Object key) {
 		return attributes.remove(key);
 	}
 	
 
 	public void setWritable(boolean writable) {
 		synchronized(panels){
 			this.writable = writable;
 			if(writable){
 				for (SVGPanel p : panels.values()) {
 					p.initToolbar();
 					int i = parent.indexOfComponent(p);
 					new CloseTabButton(parent, i);
 				}
 			}else{
 				for (SVGPanel p : panels.values()) {
 					p.removeToolbar();
 					int i = parent.indexOfComponent(p);
 					parent.setTabComponentAt(i, null);
 				}
 			}
 		}
 	}
 
 	class CloseTabButton extends JPanel implements ActionListener {
 		private static final long serialVersionUID = 1L;
 		private JTabbedPane pane;
 
 		public CloseTabButton(JTabbedPane pane, int index) {
 			this.pane = pane;
 			setOpaque(false);
 			add(new JLabel(pane.getTitleAt(index), pane.getIconAt(index),
 					JLabel.LEFT));
 //			Icon closeIcon = new CloseIcon();
 			JButton btClose = new JButton();
 			btClose.setOpaque(false);
 			btClose.setContentAreaFilled(false);
 			btClose.setBorderPainted(false);
 			ImageIcon imageIcon = new ImageIcon(SVGPanels.class.getResource("/org/jhotdraw/app/action/images/closer_rollover.gif"));
 			btClose.setRolloverIcon(imageIcon);
 			btClose.setPressedIcon(new ImageIcon(SVGPanels.class.getResource("/org/jhotdraw/app/action/images/closer_pressed.gif")));
 			btClose.setIcon(new ImageIcon(SVGPanels.class.getResource("/org/jhotdraw/app/action/images/closer.gif")));
 			btClose.setPreferredSize(new Dimension(imageIcon.getIconWidth(),
 					imageIcon.getIconHeight()));
 			add(btClose);
 			btClose.addActionListener(this);
 			pane.setTabComponentAt(index, this);
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			int i = pane.indexOfTabComponent(this);
 			if (i != -1) {
 				SVGPanel sp = (SVGPanel) pane.getComponentAt(i);
 				String id = sp.getView().getId();
 				Client.getInstance().broadcastCloseWhiteboard(id);
 			}
 		}
 	}
     
 	class CloseIcon implements Icon {
 		public void paintIcon(Component c, Graphics g, int x, int y) {
 			g.setColor(Color.RED);
 			g.drawLine(6, 6, getIconWidth() - 7, getIconHeight() - 7);
 			g.drawLine(getIconWidth() - 7, 6, 6, getIconHeight() - 7);
 		}
 
 		public int getIconWidth() {
 			return 17;
 		}
 
 		public int getIconHeight() {
 			return 17;
 		}
 	}
 }
