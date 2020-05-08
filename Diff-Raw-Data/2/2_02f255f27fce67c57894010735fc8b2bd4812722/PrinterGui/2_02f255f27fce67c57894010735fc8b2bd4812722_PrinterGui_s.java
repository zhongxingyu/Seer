 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.LayoutManager;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.awt.print.PageFormat;
 import java.awt.print.Printable;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Authenticator;
 import java.net.MalformedURLException;
 import java.net.PasswordAuthentication;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.imageio.IIOImage;
 import javax.imageio.ImageIO;
 import javax.imageio.ImageWriteParam;
 import javax.imageio.ImageWriter;
 import javax.imageio.stream.ImageOutputStream;
 import javax.print.DocFlavor;
 import javax.print.StreamPrintServiceFactory;
 import javax.print.DocFlavor.CHAR_ARRAY;
 import javax.print.attribute.HashPrintRequestAttributeSet;
 import javax.print.attribute.PrintRequestAttributeSet;
 import javax.print.attribute.standard.Copies;
 import javax.print.attribute.standard.MediaSize;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 public class PrinterGui extends JFrame implements Printable {
     private JTextField ticketField; 
     private JButton submitButton;
     private JComboBox combo;
 	
     private Properties prop = new Properties(); //container for properties
 	
     private Map<String, String> data;
 	
     private String field_header;
     private String field_description;
     private String field_footer;
 	
 	private boolean loadingDone = false;
 	
 	private BufferedImage ticketCard;
 	
 	private int width;
 	private int height;
 	
 	private static String user;
 	private static String pass;
 	
     static class TracAuthenticator extends Authenticator {
         public PasswordAuthentication getPasswordAuthentication() {
             return (new PasswordAuthentication(user, pass.toCharArray()));
         }
     }	
 	
 	/**
 	 * Set up Swing GUI
 	 */
 	public PrinterGui() {
 		loadProperties();
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		Container panel = getContentPane();
 		arrangeElements(panel);
 
 		this.setTitle("TracPrinter");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		pack();
 		setVisible(true);
 	}
 	
 	public PrinterGui(Container panel, String propertiesUrl) {
 		loadPropertiesFromUrl(propertiesUrl);
 		arrangeElements(panel);
 	}
 	
 	private void arrangeElements(Container panel) {
 		LayoutManager lm = new GridBagLayout();
 		GridBagConstraints c = new GridBagConstraints();
 		panel.setLayout(lm);
 		
 		JLabel label = new JLabel("Enter Ticket ID");
 		ticketField = new JTextField();
 		submitButton = new JButton("Print Ticket");
 		
 		String[] projects = prop.getProperty("trac_project").split("\\,");
 		combo = new JComboBox(projects);
 		JLabel projectLabel = new JLabel("Select Project");
 		
 		ActionListener al = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				getTicketContents();
 			}
 		};
 		
 		KeyListener kl = new KeyListener() {
 			
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}
 			
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
 					getTicketContents();
 				}
 			}
 			
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 			}
 		};
 		
 		submitButton.addActionListener(al);
 		ticketField.addKeyListener(kl);
 		ticketField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
 		
 		int row = 0;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = row;
 		c.gridwidth = 1;
 		c.weightx = 0.75;
 		panel.add(projectLabel, c);
 		
 		c.gridx = 1;
 		c.gridy = row;
 		c.gridwidth = 2;
 		c.weightx = 0.25;
 		panel.add(combo, c);
 		
 		row++;
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = row;
 		c.weighty = 1;
 		panel.add(label, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = row;
 		c.weightx = 0.75;
 		c.weighty = 1;
 		panel.add(ticketField, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 2;
 		c.gridy = row;
 		c.weightx = 0.25;
 		panel.add(submitButton, c);
 
 		row++;
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = row;
 		c.gridwidth = 3;
 		panel.add(new JLabel("TracPrinter: www.any-where.de"), c);
 
 		ticketField.transferFocus();		
 	}
 	
 	
 	/**
 	 * Load properties
 	 */
 	private void loadProperties() {
 		try {
 			prop.load(new FileInputStream(new File("printer.properties")));
 			setUserAndPass();
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	private void loadPropertiesFromUrl(String propertiesUrl) {
 		try {
 			URL properties = new URL(propertiesUrl);
 			prop.load(properties.openStream());
 			setUserAndPass();
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}		
 	}
 	
 	private void setUserAndPass() {
 	    if(!prop.getProperty("trac_user").equals("")) {
 	        user = prop.getProperty("trac_user");
 	        pass = prop.getProperty("trac_pass");
 	    }
 	}
 	
 	/**
 	 * Try to load ticket info by URL and start printing routine
 	 */
 	public void getTicketContents() {
 		data = new HashMap<String, String>();
 		try {
 		    
 		    if(!user.equals("") && !pass.equals("")) {
 		        Authenticator.setDefault(new TracAuthenticator());
 		    }
 		    
 			URL url = new URL(prop.getProperty("trac_url") + combo.getSelectedItem() + "/ticket/" + ticketField.getText() + "?format=tab");
 			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 			String header = in.readLine();
 			String[] keys = header.split("\t");
 			String complete = "";
 			String line = in.readLine();
 			
 			while(line != null) {
 				complete += (line + " ");
 				line = in.readLine();
 			}
 			
 			String[] values = complete.split("\t");
 			
 			for(int i=0; i<keys.length; i++) {
 				if(i < values.length) {
 				    data.put(keys[i], values[i]);
 				}
 			}
 			
 			field_header = prop.getProperty("field_header");
 			field_description = prop.getProperty("field_description");
 			field_footer = prop.getProperty("field_footer");
 			
 			for (String key : keys) {
 				field_header = field_header.replaceAll("\\{" + key + "\\}", data.get(key));
 				field_description = field_description.replaceAll("\\{" + key + "\\}", data.get(key));
 				field_footer = field_footer.replaceAll("\\{" + key + "\\}", data.get(key));
 			}
 			loadingDone = true;
 		} catch (MalformedURLException e) {
 			//ticketField.setText(e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 			//ticketField.setText(e.getMessage());
 		} catch  (Exception e) {
 		    e.printStackTrace();
 			//ticketField.setText(e.getMessage());
 		}
 		if(loadingDone) {
 			printTicket();			
 		}
 	}
 
 	/**
 	 * Show print dialog
 	 */
 	private void printTicket() {
 		PrinterJob printJob = PrinterJob.getPrinterJob();
 		printJob.setPrintable(this);
 		String suffix = "jpg";
 		
 		DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
 		String psMimeType = DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType();
 		
 		//Store card as file
         width = Integer.parseInt(prop.getProperty("ticket_width"));
         height = Integer.parseInt(prop.getProperty("ticket_height"));		
 		boolean keepFile = Boolean.parseBoolean(prop.getProperty("keep_image_as_file"));
         ticketCard = new BufferedImage(width + 100, height + 100, BufferedImage.TYPE_INT_RGB);
 		Graphics g = getGraphics(ticketCard.createGraphics());
 		
 		try {
             Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix("jpeg");
             if (!writers.hasNext())
                 throw new IllegalStateException("No writers found");
             
             ImageWriter writer = (ImageWriter) writers.next();
             File file = new File(ticketField.getText() + "." + suffix);
             ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(file));
             
             writer.setOutput(ios);
             
             ImageWriteParam param = writer.getDefaultWriteParam();
             
             param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
             param.setCompressionQuality(1.0f);
             
             writer.write(null, new IIOImage(ticketCard, null, null), param);
             
             if(!keepFile) {
                 file.delete();
             }
         } catch (IOException e1) {
             e1.printStackTrace();
         }
 		
 		if(printJob.printDialog()) {
 			try {
 				printJob.print();
 			} catch (PrinterException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public Graphics2D getGraphics(Graphics2D graphics) {
 	    int offset = 10;
 	    
 	    graphics.setColor(Color.WHITE);
 	    graphics.fillRect(0, 0, 10000,10000);
 	    graphics.setColor(Color.BLACK);
 	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	    
         int font_size_header = Integer.parseInt(prop.getProperty("font_size_header"));
         int font_size_footer = Integer.parseInt(prop.getProperty("font_size_footer"));
         int font_size_description = Integer.parseInt(prop.getProperty("font_size_description"));
         
         int max_chars_headline = Integer.parseInt(prop.getProperty("max_chars_headline"));
         int padding_text_left = Integer.parseInt(prop.getProperty("padding_text_left")) + offset;
         int distance_header_top = Integer.parseInt(prop.getProperty("distance_header_top")) + offset;
         int distance_footer_top = Integer.parseInt(prop.getProperty("distance_footer_top")) + offset;
         int distance_description_top = Integer.parseInt(prop.getProperty("distance_description_top")) + offset;
         
         int distance_line_top = Integer.parseInt(prop.getProperty("distance_line_top")) + offset;
         int distance_line_bottom = Integer.parseInt(prop.getProperty("distance_line_bottom")) + offset;
         
         graphics.drawRect(offset, offset, width, height);
         graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font_size_header));
         if(field_header.length() > max_chars_headline) {
             field_header = field_header.substring(0, max_chars_headline - 3);
         }
         
         graphics.drawString(field_header, padding_text_left, distance_header_top);
         Font font = new Font(Font.SANS_SERIF, Font.PLAIN, font_size_description);
         graphics.setFont(font);
         
         String[] words = field_description.split(" ");
         String line = "";
         
         int pos = 0;
         int max_chars_description = Integer.parseInt(prop.getProperty("max_chars_description"));
         
         int line_height_description = Integer.parseInt(prop.getProperty("line_height_description"));
         int line_height_footer = Integer.parseInt(prop.getProperty("line_height_footer"));
         
         int max_lines = Integer.parseInt(prop.getProperty("max_lines"));
         
         int line_counter = 0;
         
         //build lines
         for (String word : words) {
             if(line_counter == 0 && word.startsWith("\"")) {
                 word = word.substring(1);
             }
             if(word.endsWith("\"")) {
                 word = word.substring(0, word.length()-1);
             }
             if(line_counter < max_lines) {
                 if(pos + word.length() > max_chars_description) {
                     graphics.drawString(line, padding_text_left, distance_description_top + line_counter * line_height_description);
                     line_counter++;
                     line = word + " ";
                     pos = line.length();
                 } else {
                     line = line + word + " ";
                     pos = pos + word.length();
                 }
             }
         }
         
         graphics.drawString(line, padding_text_left, distance_description_top + line_counter * line_height_description);
         
         line_counter = 0;
         graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, font_size_footer));
         graphics.drawString(field_footer, padding_text_left, distance_footer_top);
        graphics.drawString(prop.getProperty("trac_url") + prop.getProperty("trac_project") + "/ticket/" + ticketField.getText(), padding_text_left, distance_footer_top + line_height_footer);
         
         graphics.drawLine(offset, distance_line_top, width + offset, distance_line_top);
         graphics.drawLine(offset, distance_line_bottom, width + offset, distance_line_bottom);	    
 	    return null;
 	}
 	
 	@Override
 	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
 			throws PrinterException {
 	    
 	    double scale = 0.45;
 	    int w = (int) (scale * ticketCard.getWidth());
 	    int h = (int) (scale * ticketCard.getHeight());	    
 	    
 		if(pageIndex == 0) {
 			Graphics2D g = (Graphics2D) graphics;
 			g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
 			graphics.drawImage(ticketCard, 0, 50, w, h, null);
 			return PAGE_EXISTS;
 		} else {
 			return NO_SUCH_PAGE;
 		}
 	}
 }
