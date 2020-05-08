 package gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Enumeration;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractButton;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import tools.ClientToolProperties;
 
 public class ToolPalette extends JPanel {
 	private ButtonGroup btnGroup;
 	private ClientToolProperties tp;
 
 	// Command sent when corresponding button is pressed: PEN, BRUSH, BUCKET,
 	// ERASER, TEXT, SHAPE_LINE, SHAPE_RECTANGLE, SHAPE_ELLIPSE
 	public ToolPalette(ClientToolProperties tp) {
 		btnGroup = new ButtonGroup();
 		this.tp = tp;
 		this.setOpaque(false);
 		BtnListener btnListener = new BtnListener();
 		this.setLayout(new GridLayout(4, 2, 5, 5));
 
 		try {
 			// pen button
 			JButton penButton = new JButton();
 			Image img = ImageIO.read(getClass().getResource("/res/icons/pen.png"));
 			penButton.setIcon(new ImageIcon(img));
 			penButton.setActionCommand(ClientToolProperties.PEN_TOOL + "");
 			btnGroup.add(penButton);
 
 			// brush button
 			JButton brushButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/paintbrush.png")); 
 			brushButton.setIcon(new ImageIcon(img));
 			brushButton.setActionCommand(ClientToolProperties.BRUSH_TOOL + "");
 			btnGroup.add(brushButton);
 
 			// colorPicker button
 			JButton colorPicker = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/pipette.png"));
 			colorPicker.setIcon(new ImageIcon(img)); 
 			colorPicker.setActionCommand(ClientToolProperties.COLORPICKER_TOOL + "");
 			btnGroup.add(colorPicker);
 
 			// eraser button
 			JButton eraserButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/erase.png"));
 			eraserButton.setIcon(new ImageIcon(img));
 			eraserButton.setActionCommand(ClientToolProperties.ERASER_TOOL + "");
 			btnGroup.add(eraserButton);
 
 			// text button
 			JButton textButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/text.png"));
 			textButton.setIcon(new ImageIcon(img));
 			textButton.setActionCommand(ClientToolProperties.TEXT_TOOL + "");
 			btnGroup.add(textButton);
 
 			// line button
 			JButton lineButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/line.png"));
 			lineButton.setIcon(new ImageIcon(img));
 			lineButton.setActionCommand(ClientToolProperties.LINE_TOOL + "");
 			btnGroup.add(lineButton);
 
 			// rectangle button
 			JButton rectangleButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/rectangle.png"));
 			rectangleButton.setIcon(new ImageIcon(img));
 			rectangleButton.setActionCommand(ClientToolProperties.RECTANGLE_TOOL + "");
 			btnGroup.add(rectangleButton);
 
 			// ellipse button
 			JButton ellipseButton = new JButton();
 			img = ImageIO.read(getClass().getResource("/res/icons/ellipse.png"));
 			ellipseButton.setIcon(new ImageIcon(img));
 			ellipseButton.setActionCommand(ClientToolProperties.ELLIPSE_TOOL + "");
 			btnGroup.add(ellipseButton);
 
 			Enumeration<AbstractButton> buttons = btnGroup.getElements();
 
 			while (buttons.hasMoreElements()) {
 				JButton b = (JButton) buttons.nextElement();
 				b.setPreferredSize(new Dimension(30, 30));
 				b.addActionListener(btnListener);
 				b.setFocusPainted(false);
 				this.add(b);
 			}
 		} catch (Exception ex) {
 			System.out.println("Exception: " + ex.getMessage());
 		}
 	}
 
 	private class BtnListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Enumeration<AbstractButton> buttons = btnGroup.getElements();
 			while (buttons.hasMoreElements()) {
 				JButton b = (JButton) buttons.nextElement();
 				b.setBackground(new Color(255, 255, 255));
 			}
 			
 			JButton pressedButton = ((JButton) e.getSource());
 			pressedButton.setBackground(new Color(200, 200, 200));
 
 			int tool = Integer.parseInt(e.getActionCommand());
 			tp.changeTool(tool);
 		}
 	}
 }
