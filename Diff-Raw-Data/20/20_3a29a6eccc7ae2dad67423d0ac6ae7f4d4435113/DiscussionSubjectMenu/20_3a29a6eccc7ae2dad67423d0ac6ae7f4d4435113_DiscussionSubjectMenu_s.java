 package client.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.HeadlessException;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.rmi.ConnectException;
 import java.rmi.RemoteException;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import server.objects.interfaces.DiscussionSubjectInterface;
 import client.interfaces.ClientDisplayerInterface;
 
 /**
  * A {@link JPanel} to store all discussions inside
  * @author Grumpy Group
  */
 public class DiscussionSubjectMenu extends JPanel implements ActionListener, AdjustmentListener {
 
 	/**
 	 * Generated serialVersionUID
 	 */
 	private static final long serialVersionUID = 6560966161410462245L;
 	private int nbChannels=0;
 	private JLabel label=new JLabel(this.nbChannels+" Available subjects:");
 	private final JButton createChannel=new JButton("Create a channel");
 	private JButton previous=new JButton((char)9668+"");
 	private JButton next=new JButton((char)9658+"");
 	private JPanel subjectsPanel=new JPanel();
 	private JScrollPane subjectsScroll;
 	private int scrollPos=0;;
 	private int scrollWidth=350;
 
 	/**
 	 * Construct a new instance of a SubjectMenu
 	 */
 	public DiscussionSubjectMenu() {
 		super();
 		this.setMinimumSize(new Dimension(this.scrollWidth+300,50));
 		this.subjectsPanel.setBorder(null);
 		this.subjectsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		this.subjectsScroll=new JScrollPane(this.subjectsPanel,
 				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
 				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		this.subjectsScroll.setPreferredSize(new Dimension(this.scrollWidth,25));
 		this.subjectsScroll.setBorder(null);
 		this.subjectsScroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0,0));
 		this.subjectsScroll.getHorizontalScrollBar().addAdjustmentListener(this);
 		
 		this.createChannel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		this.label.setLayout(new FlowLayout(FlowLayout.LEFT));
 		this.add(this.createChannel);
 		this.add(this.label);
 		this.add(this.previous);
 		this.add(this.subjectsScroll);
 		this.add(this.next);
 		this.next.setToolTipText("Display next channels");
 		this.previous.setToolTipText("Display previous channels");
 
 		this.previous.addActionListener(this);
 		this.previous.setEnabled(false);
 		this.next.addActionListener(this);
 	}
 	
 	@Override
 	public void revalidate() {
 		super.revalidate();
 		this.computeSizes();
 	}
 	
 	public void reset() {
 		this.subjectsPanel.removeAll();
 		this.nbChannels=0;
 		this.label.setText(this.nbChannels+" Available subjects:");
 	}
 
 	public void updatePanel() {
 		Point currentPos=this.subjectsScroll.getViewport().getViewPosition();
 		this.subjectsScroll.getViewport().setViewPosition(new Point(currentPos.x,6));
 		this.subjectsPanel.repaint();
 		currentPos=this.subjectsScroll.getViewport().getViewPosition();
 	}
 	
 	public void setScrollWidth(int width) {
 		this.scrollWidth=width-370;
 		this.subjectsPanel.setMinimumSize(new Dimension(this.scrollWidth+300,50));
 		this.subjectsScroll.setPreferredSize(new Dimension(this.scrollWidth,25));
 		this.revalidate();
 	}
 
 	/**
 	 * Add a new discussion subject to the current menu.
	 * @param subjectName
 	 */
 	public void addDiscussionSubject(final ClientDisplayerInterface client,
 			final DiscussionSubjectInterface subject) {
 		this.nbChannels++;
 		this.label.setText(this.nbChannels+" Available subjects:");
 		JButton dsButton = new JButton();
 		dsButton.setLayout(new FlowLayout());
 		final DiscussionSubjectMenu dsm=this;
 		dsButton.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent event) {
 				boolean error=false;
 				try {
 					boolean connected=subject.isConnected(client);
 					if(connected||client.getServer().subscribe(subject, client)) {
 						if(client.isOpenedDiscussion(subject)) {
 							client.getDiscussionFrame(subject).setVisible(true);
 						}
 						else {
 							client.openDiscussion(new ClientDiscussionFrame(client, subject));	
 						}
 					}
 					else {
 						client.error("You did not success to subscribe to the cannel", true);
 					}
 				} catch (ConnectException ce) {
 					error=true;
 				} catch (HeadlessException e) {
 					e.printStackTrace();
 				} catch (RemoteException e) {
 					error=true;
 				}
 				if(error) {
 					JOptionPane.showMessageDialog(dsm, "Connection seems to be lost",
 							"Connection error! x)",JOptionPane.ERROR_MESSAGE);
 					System.exit(0);
 				}
 			}
 		});
 		try {
 			String title=subject.getTitle();
 			dsButton.setText(title);
 			dsButton.setToolTipText("Subscribe to '"+title+"' channel");
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 		this.subjectsPanel.add(dsButton);
 	}
 	
 	public void removeDiscussionSubject(final ClientDisplayerInterface client,
 			final DiscussionSubjectInterface subject) {
 		for(Component c:this.subjectsPanel.getComponents()) {
 			if(c instanceof JButton) {
 				try {
 					if(((JButton) c).getText().equalsIgnoreCase(subject.getTitle())) {
 						this.subjectsPanel.remove(c);
 						this.nbChannels--;
 						this.label.setText(this.nbChannels+" Available subjects:");
 					}
 				} catch (RemoteException e) {
 					System.err.println("Can not remove the channel button");
 					e.printStackTrace();
 					return;
 				}
 			}
 		}
 	}
 
 	public void addNullListInformation() {
 		this.subjectsPanel.add(new JLabel("No subjects"));
 	}
 
 	/**
 	 * @return the createChannel
 	 */
 	public JButton getCreateChannel() {
 		return this.createChannel;
 	}
 	
 	private void scrollPrevious(int size) {
 		this.scrollTo(-size);
 	}
 	
 	private void scrollNext(int size) {
 		this.scrollTo(size);
 	}
 	
 	private void computeSizes() {
 		try {
 			this.next.setEnabled(false);
 			this.previous.setEnabled(false);
 			if(this.subjectsPanel.getPreferredSize().getWidth()>=this.scrollWidth) {
 				Point currentPos=this.subjectsScroll.getViewport().getViewPosition();
 				int maxWidth=(int) (this.subjectsPanel.getPreferredSize().getWidth()-
 						this.scrollWidth);
 				if(currentPos.x>0) {
 					this.previous.setEnabled(true);
 				}
 				if(currentPos.x<maxWidth) {
 					this.next.setEnabled(true);
 				}
 			}
 		} catch (NullPointerException e) {
 		}
 	}
 	
 	private void scrollTo(int size) {
 		int maxWidth=this.subjectsPanel.getWidth()-this.scrollWidth;
 		if(maxWidth<=0) {
 			this.computeSizes();
 			return;
 		}
 		Point currentPos=this.subjectsScroll.getViewport().getViewPosition();
 		int newPos=currentPos.x+size;
 		newPos=newPos>maxWidth?maxWidth:newPos;
 		newPos=newPos<=0?0:newPos;
 		if(newPos==maxWidth) {
 			this.next.setEnabled(false);
 		}
 		else if(newPos==0) {
 			this.previous.setEnabled(false);
 		}
 		if(size>0) {
 			this.previous.setEnabled(true);
 		}
 		else if(size<0) {
 			this.next.setEnabled(true);
 		}
 		this.scrollPos=newPos;
 		this.subjectsScroll.getViewport().setViewPosition(new Point(newPos,currentPos.y));
 		this.subjectsPanel.revalidate();
 		this.repaint();
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if(event.getSource().equals(this.previous)) {
 			this.scrollPrevious(50);
 		}
 		else if(event.getSource().equals(this.next)) {
 			this.scrollNext(50);
 		}
 	}
 
 	@Override
 	public void adjustmentValueChanged(AdjustmentEvent event) {
 		if(event.getSource().equals(this.subjectsScroll.getHorizontalScrollBar())) {
 			Point currentPos=this.subjectsScroll.getViewport().getViewPosition();
 			if(currentPos.x>this.scrollPos) {
 				this.scrollNext(10);
 			}
 			else if(currentPos.x<this.scrollPos) {
 				this.scrollPrevious(10);
 			}
 		}
 	}
 	
 }
