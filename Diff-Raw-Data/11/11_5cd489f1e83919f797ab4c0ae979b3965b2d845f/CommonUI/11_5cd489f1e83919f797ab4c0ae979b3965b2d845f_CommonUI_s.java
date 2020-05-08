 package org.aesthete.swingobjects.view;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GraphicsConfiguration;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.Border;
 import javax.swing.text.MaskFormatter;
 
 import org.aesthete.swingobjects.ActionProcessor.CLIENT_PROPS;
 import org.aesthete.swingobjects.SwingObjProps;
 import org.aesthete.swingobjects.exceptions.ErrorSeverity;
 import org.aesthete.swingobjects.exceptions.SwingObjectRunException;
 import org.aesthete.swingobjects.exceptions.SwingObjectsExceptions;
 import org.aesthete.swingobjects.util.FieldCallback;
 import org.aesthete.swingobjects.util.ReflectionUtils;
 import org.apache.commons.beanutils.MethodUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.jdesktop.swingx.painter.ImagePainter;
 
 public class CommonUI {
 
 	public static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
 	public static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
 
 	private static Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
 
 	public static void initComponent(Object prop) {
 		JComponent component = (JComponent)prop;
 		Border savedBorder=(Border)component.getClientProperty(CLIENT_PROPS.BORDER);
 		if(savedBorder==null){
 		    component.putClientProperty(CLIENT_PROPS.BORDER,
 		    		component.getBorder()==null?BorderFactory.createEmptyBorder():component.getBorder());
 		}else{
 		    component.setBorder(savedBorder);
 		}
 
 		Boolean isEnabled=(Boolean)component.getClientProperty(CLIENT_PROPS.ENABLED);
 		if(isEnabled==null) {
 			component.putClientProperty(CLIENT_PROPS.ENABLED, component.isEnabled());
 		}
 
 		component.setEnabled(false);
 
 		component.setCursor(WAIT_CURSOR);
 
 		String tooltip=(String)component.getClientProperty(CLIENT_PROPS.TOOLTIP);
 		if(tooltip==null){
 			component.putClientProperty(CLIENT_PROPS.TOOLTIP, component.getToolTipText()==null ? "" : component.getToolTipText());
 		}else{
 			component.setToolTipText(tooltip);
 		}
 	}
 
 	public static void restoreComponentsToInitialState(List<JComponent> comps) {
 		for(JComponent comp : comps) {
 			Boolean isEnabled=(Boolean)comp.getClientProperty(CLIENT_PROPS.ENABLED);
 			comp.setEnabled(isEnabled);
 			comp.setCursor(DEFAULT_CURSOR);
 		}
 	}
 
 	public static void nameComponents(Class<?> clz, Object obj) {
 		try {
             Field[] fields = clz.getDeclaredFields();
 			for (Field field : fields) {
 				if (CommonUI.isNamedComponent(field.getType())) {
 					field.setAccessible(true);
 					if (field.get(obj) != null) {
 						String name = (String) MethodUtils.invokeMethod(field.get(obj), "getName", null);
						if (StringUtils.isNotEmpty(name)) {
 							MethodUtils.invokeMethod(field.get(obj), "setName", field.getName());
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			throw new SwingObjectRunException(e, ErrorSeverity.SEVERE, FrameFactory.class);
 		}
 	}
 
 	public static boolean isNamedComponent(Class<?> type) {
 		if (!JComponent.class.isAssignableFrom(type)) {
 			return false;
 		}
		if (JPanel.class.isAssignableFrom(type)) {
			return false;
		} else if (JDialog.class.isAssignableFrom(type)) {
			return false;
		} else if (JFrame.class.isAssignableFrom(type)) {
			return false;
		}

 		return true;
 	}
 
 	public static void showErrorDialogForComponent(SwingObjectsExceptions e) {
 		JXErrorPane errorPane = new JXErrorPane();
 		ImageIcon imageIcon = null;
 		switch (e.getErrorSeverity()) {
 		case SEVERE:
 			imageIcon = new javax.swing.ImageIcon(CommonUI.class.getResource("/images/error.png"));
 			errorPane.setErrorReporter(new InfoGatherErrorReporterFrame());
 			break;
 		case ERROR:
 			imageIcon = new javax.swing.ImageIcon(CommonUI.class.getResource("/images/error.png"));
 			break;
 		case WARNING:
 			imageIcon = new javax.swing.ImageIcon(CommonUI.class.getResource("/images/warning.png"));
 			break;
 		case INFO:
 			imageIcon = new javax.swing.ImageIcon(CommonUI.class.getResource("/images/info.png"));
 			break;
 		}
 		String detailedMessage = e.getDetailedMessage(false);
 		errorPane.setIcon(imageIcon);
 		errorPane.setErrorInfo(new ErrorInfo(e.getErrorSeverity().toString(), e.getDetailedMessage(true), detailedMessage, null, (Throwable) e, e
 				.getErrorSeverity().getJavaLoggingLevel(), null));
 		errorPane.setPreferredSize(new Dimension(CommonUI.getFractionedWidth(35), CommonUI.getFractionedHeight(22)));
 		JXErrorPane.showFrame(null, errorPane);
 	}
 
 
 	public static int getFractionedWidth(double widthPercent){
 		return (int)(screenSize.getWidth()*widthPercent/100);
 	}
 
 	public static int getFractionedHeight(double heightPercent){
 		return (int)(screenSize.getHeight()*heightPercent/100);
 	}
 
 	public static int getScreenWidth(){
 		return (int)screenSize.getWidth();
 	}
 	public static int getScreenHeight(){
 		return (int)screenSize.getHeight();
 	}
 
 
 	public static void showOnScreen(final JFrame frame) {
 		runInEDT(new Runnable() {
 			@Override
 			public void run() {
 				frame.pack();
 				locateOnOpticalScreenCenter(frame);
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	public static void showOnScreen(final JDialog frame) {
 		runInEDT(new Runnable() {
 			public void run() {
 				frame.pack();
 				locateOnOpticalScreenCenter(frame);
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	 /**
      * Locates the given component on the screen's center.
      *
      * @param component   the component to be centered
      */
     public static void locateOnOpticalScreenCenter(Component component) {
     	GraphicsConfiguration gc = component.getGraphicsConfiguration();
     	Rectangle bounds = gc.getBounds();
     	int x = (int) ( bounds.getWidth() - component.getWidth() ) /2 ;
     	int y = (int) ( bounds.getHeight() - component.getHeight() ) /2;
     	component.setLocation( x, y );
     }
 
     public static void setIconImageForContainer(Window window){
     	String iconimg = SwingObjProps.getSwingObjProperty("application-icon");
 		if(StringUtils.isNotEmpty(iconimg)){
 			window.setIconImage(new ImageIcon(CommonUI.class.getResource(iconimg)).getImage());
 
     	}
     }
 
     public static ImageIcon getScaledImage(int width, int height, String imageLoc){
 		ImageIcon icon=new ImageIcon(imageLoc);
 		Image scaledImage=icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
 		icon=new ImageIcon(scaledImage);
 		return icon;
 	}
 
     public static void setErrorBorderAndTooltip(JComponent jcomponent,String errortooltip) {
     	String tooltip=jcomponent.getToolTipText();
 		if(StringUtils.isEmpty(tooltip)){
 			tooltip="<html>"+errortooltip+"</html>";
 		}else{
 			tooltip=tooltip.replace(Pattern.quote("<html>"),
 					Matcher.quoteReplacement("<html>"+errortooltip+"<br/>"));
 		}
 		jcomponent.setToolTipText(tooltip);
 		jcomponent.setBorder(BorderFactory.createLineBorder(Color.red));
     }
 
     public static void runInEDT(final Runnable run) {
     	if(SwingUtilities.isEventDispatchThread()) {
     		run.run();
     	}else {
     		SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					run.run();
 				}
 			});
     	}
     }
 
     public static ImagePainter getImagePainter(String imageLocation) {
 		ImagePainter imgPainter=null;
 		try {
 			imgPainter = new ImagePainter(ImageIO.read(new File(imageLocation)));
 		} catch (IOException e) {
 			throw new SwingObjectRunException(e, ErrorSeverity.SEVERE, CommonUI.class);
 		}
 		return imgPainter;
 	}
 
     public static JTextField getMaskFormattedTextField(String format) {
     	try {
 			return new JFormattedTextField(new MaskFormatter(format));
 		} catch (ParseException e) {
 			throw new SwingObjectRunException(e, CommonUI.class);
 		}
     }
 }
