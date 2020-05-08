 package edss.gui;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.beans.PropertyVetoException;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import edss.canvas.StateConstant;
 import edss.interf.GuiMediator;
 // modificare variabila modificat!!!
 
 public class NewInternalFrame extends JInternalFrame {
 	
 	JSlider zoomSlider;
 	String fileName;
 	private JPanel internalPanel;
 	private int zoomFactor = 100;
 	private GuiImpl gui;
 	private int state;
	private boolean modified = false;
 //	private JSVGCanvas canvas;
 	
 	GuiMediator mediator; // = new Mediator(); 
 
 	public NewInternalFrame(String s, int i, String file)
 	{
 		super(s + " " + i, true, true, true, true);
 		setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
 		state = StateConstant.PIECESTATE;
 		fileName = file;
 		this.mediator =   new edss.med.Mediator();		
 		mediator.init();
 		setBounds(i*10, i*10, 300, 300);
 		// setDefaultCloseOperation(HIDE_ON_CLOSE);
 		internalPanel = new JPanel(new BorderLayout());
 		zoomSlider = new JSlider(1, 5);
 		zoomSlider.setValue(3);
 		zoomSlider.addMouseListener(new MouseListener()
 		{
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				int auxZoom = zoomSlider.getValue();
 //					if(auxZoom < 37)
 //						zoomFactor = 25;
 //					if(auxZoom >= 37 && auxZoom < 76)
 //						zoomFactor = 50;
 //					if(auxZoom >= 76 && auxZoom < 150)
 //						zoomFactor = 100;
 //					if(auxZoom >= 150 && auxZoom < 300)
 //						zoomFactor = 200;
 //					if(auxZoom >= 300)
 //						zoomFactor = 400;
 				
 				zoomFactor = (int) ((Math.pow(2, (auxZoom - 3)) * 100));
 				System.out.println("Zoom = " + zoomFactor);
 				// zoomSlider.setValue(zoomFactor);
 				mediator.scale(zoomFactor); 
 			}
 
 			
 		});
 		setLayout(new BorderLayout());
 		add(internalPanel, BorderLayout.CENTER);
 		add(zoomSlider, BorderLayout.SOUTH);
 		setVisible(true);
 		this.addInternalFrameListener(new InternalFrameListener()
 		{
 
 			@Override
 			public void internalFrameActivated(InternalFrameEvent e) {
 				// System.out.println(getTitle() + " <<<<");
 				if(gui != null)
 				{
 					modified = true;
 					gui.mediator = mediator;
 					if(gui.M.isSelected())
 					{
 						state = StateConstant.MOUSESTATE;
 						gui.mediator.enterState(StateConstant.MOUSESTATE);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 					}
 					if(gui.P.isSelected())
 					{
 						state = StateConstant.PIECESTATE;
 						gui.mediator.enterState(StateConstant.PIECESTATE);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 					}
 					if(gui.D.isSelected())
 					{
 						state = StateConstant.DELETESTATE;
 						gui.mediator.enterState(StateConstant.DELETESTATE);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 					}
 					if(gui.G.isSelected())
 					{
 						state = StateConstant.DRAGSTATE;
 						gui.mediator.enterState(StateConstant.DRAGSTATE);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 					}
 					if(gui.N.isSelected())
 					{
 						state = StateConstant.WIRESTATE;
 						gui.mediator.enterState(StateConstant.WIRESTATE);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 					}
 					if(gui.J.isSelected())
 					{
 						state = StateConstant.PIECESTATE;
 						gui.mediator.enterState(StateConstant.PIECESTATE);
 						gui.mediator.setPiece("node", "node", "node");
 					}
 					else
 					{
 						gui.mediator.enterState(state);
 						if(gui.getSelectedPiece() != null)
 							gui.mediator.setPiece(gui.getSelectedPiece().getName(), gui.getSelectedPiece().getCategory(), gui.getSelectedPiece().getSubCategory());
 						else 
 						{
 							gui.M.setSelected(true);
 							gui.mediator.enterState(state);
 						}
 						if(state == StateConstant.PIECESTATE)
 							gui.P.setSelected(true);
 						if(state == StateConstant.DRAGSTATE)
 							gui.G.setSelected(true);
 						if(state == StateConstant.WIRESTATE)
 							gui.N.setSelected(true);	
 						if(state == StateConstant.DELETESTATE)
 							gui.D.setSelected(true);	
 						if(state == StateConstant.MOUSESTATE)
 							gui.M.setSelected(true);	
 					}
 				}
 				
 			}
 
 			@Override
 			public void internalFrameClosed(InternalFrameEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void internalFrameClosing(InternalFrameEvent e) {
 				if(modified == true)
 				{
 					final JDialog saveDialog = new JDialog();
 					saveDialog.setModal(true);
 					saveDialog.setLocationRelativeTo(getThis());
 					saveDialog.setLayout(new BorderLayout());
 					saveDialog.add(new JLabel("Project has been modified, save first?"), BorderLayout.CENTER);
 					JButton bs = new JButton("Save");
 						bs.addActionListener(new ActionListener()
 						{
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								if(fileName != null)
 								{
 									String aux = fileName.substring(0, fileName.lastIndexOf('.'));
 									mediator.save(aux);
 								}
 								else
 								{
 									JFileChooser chSave = new JFileChooser();
 									chSave.setDialogType(JFileChooser.SAVE_DIALOG);
 									FileFilter myFilter = new FileNameExtensionFilter("*.svg", "svg");
 									chSave.addChoosableFileFilter(myFilter);
 									chSave.showSaveDialog(getThis());
 								
 									if(chSave.getSelectedFile() != null)
 									{
 										System.out.println(chSave.getSelectedFile().getAbsolutePath());
 										if(chSave.getSelectedFile().getAbsolutePath().contains(".svg"))
 										{
 											fileName = chSave.getSelectedFile().getAbsolutePath();
 										}
 										else
 										{
 											fileName = chSave.getSelectedFile().getAbsolutePath() + ".svg";
 										}
 										mediator.save(chSave.getSelectedFile().getAbsolutePath());
 										setTitle(fileName);
 										
 									}
 								}
 								modified = false;
 								saveDialog.dispose();
 								dispose();
 								gui.mediator = null;
 							}	
 						});
 					JButton bq = new JButton("Quit");
 						bq.addActionListener(new ActionListener()
 						{
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								modified = false;
 								getThis().dispose();
 								saveDialog.dispose();
 								if(gui != null)
 									gui.mediator = null;
 							}
 						});
 					JButton bc = new JButton("Cancel");
 						bc.addActionListener(new ActionListener()
 						{
 							@Override
 							public void actionPerformed(ActionEvent e) {
 								saveDialog.dispose();						
 							}
 						});
 					JPanel px = new JPanel(new GridLayout(1,0));
 					px.add(bs);
 					px.add(bq);
 					px.add(bc);
 					saveDialog.add(px, BorderLayout.SOUTH);
 					saveDialog.setSize(250, 80);
 					saveDialog.setVisible(true);
 					// saveDialog.setLocationRelativeTo(null);
 					saveDialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				}
 				else { getThis().dispose(); modified = false;}
 				
 			}
 
 			@Override
 			public void internalFrameDeactivated(InternalFrameEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void internalFrameDeiconified(InternalFrameEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void internalFrameIconified(InternalFrameEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void internalFrameOpened(InternalFrameEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 		});
 	}
 	public void registerGui(GuiImpl g)
 	{
 		gui = g;
 	}
 	
 	public int getZoomFactor()
 	{
 		return zoomFactor;
 	}
 	public void setZoomFactor(int z)
 	{
 		zoomFactor = z;
 	}
 	
 	public JPanel getPanel()
 	{
 		return internalPanel;
 	}
 	
 	public GuiMediator getMediator()
 	{
 		return mediator;
 	}
 	
 	public boolean wasModified()
 	{
 		return modified;
 	}
 	
 	public void modify(boolean b)
 	{
 		modified = b;
 	}
 	public NewInternalFrame getThis()
 	{
 		return this;
 	}
 	
 }
