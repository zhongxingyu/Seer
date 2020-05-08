 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.util.LinkedList;
 import java.net.*;
 import java.io.*;
 import java.awt.image.*;
 import java.io.File;
 import java.util.Iterator;
 import javax.imageio.*;
 import javax.imageio.stream.*; 
 
 public class MainPanel extends JPanel {
 
 	public final String DEFAULT_PORT = "21476";
 
 	Dimension drawingSize;
 	File currentFile = null;
 	Drawing drawing;
 	DrawingPanel drawingPanel;
 	ControlPanel controlPanel;
 	ConsolePanel consolePanel;
 	JScrollPane scroller;
 	UserResponder userResponder = new UserResponder();
 	NetController netController = new NetController();
 	Thread connectionThread = null;
 	Thread serverThread = null;
 	FileHandler fileHandler = new FileHandler();
 	Menu menuBar = new Menu();
 
 	public MainPanel(File file, Dimension d, String ip, int port) {
 
 		setLayout(new BorderLayout());
 		drawing = new Drawing();
 		drawing.container = this;
 
 		if (d != null) {
 			drawingSize = d;
 			drawingPanel = new DrawingPanel();
 			drawingPanel.setPreferredSize(drawingSize);
 		} else {
 			drawingSize = new Dimension(1000, 1000);
 			drawingPanel = new DrawingPanel();
 			drawingPanel.setPreferredSize(drawingSize);
 			drawingPanel.setMaximumSize(drawingSize);
 			drawingPanel.setMinimumSize(drawingSize);
 		}
 
 		//Set up a gray-colored background for when the drawing area is smaller than the window...
 		JPanel bgBox = new JPanel();
 		bgBox.setLayout(new BoxLayout(bgBox, BoxLayout.Y_AXIS));
 		bgBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		bgBox.add(Box.createVerticalGlue());
 		bgBox.add(drawingPanel);
 		bgBox.add(Box.createVerticalGlue());
 		bgBox.setBackground(Color.lightGray);
 
 		scroller = new JScrollPane(bgBox);
 		controlPanel = new ControlPanel();
 		consolePanel = new ConsolePanel();
 		add(scroller, BorderLayout.CENTER);
 		add(controlPanel, BorderLayout.SOUTH);
 		add(consolePanel, BorderLayout.EAST);
 		if (file != null) {
 			try {
 				drawing = fileHandler.openFile(file);
 			} catch (FileNotFoundException e) {
 				System.out.println("File not found: " + e);
 			} catch (IOException e) {
 				System.out.println("IOException: " + e);
 			}
 			for (Layer l: drawing.layers) {
 				System.out.println("Layer named: " + l.name);
 			}
 			drawingPanel.setPreferredSize(new Dimension(drawing.panelWidth, drawing.panelHeight));
 			controlPanel.refigureLayers();
 			controlPanel.setCurrentLayer(drawing.layers[0].name);
 			drawingPanel.repaint();
 			currentFile = file;
 		}
 		if (ip != null) {
 			userResponder.beginConnection(ip, port);
 		}
 		drawingPanel.setNewCursor();
 		menuBar.save.setEnabled(false);
 		validate();
 
 	}
 
 	public void resizeDrawingPanel(Dimension d) {
 		scroller.remove(drawingPanel);
 		remove(scroller);
 		drawingPanel = new DrawingPanel();
 		drawingPanel.setPreferredSize(d);
 		JPanel bgBox = new JPanel();
 		bgBox.setLayout(new BoxLayout(bgBox, BoxLayout.Y_AXIS));
 		bgBox.setBackground(Color.lightGray);
 		bgBox.setAlignmentX(JComponent.CENTER_ALIGNMENT);
 		bgBox.add(Box.createVerticalGlue());
 		bgBox.add(drawingPanel);
 		bgBox.add(Box.createVerticalGlue());
 		scroller = new JScrollPane(bgBox);
 		add(scroller);
 		validate();
 	}
 
 	public class ConnectPanel extends JPanel {
 		JTextField ipField = new JTextField("127.0.0.1");
 		JTextField portField = new JTextField(DEFAULT_PORT);
 		public ConnectPanel() {
 			JLabel message = new JLabel("Enter connection details...");
 			JLabel ipLabel = new JLabel("IP:");
 			JLabel portLabel = new JLabel("Port:");
 			JPanel ipPanel = new JPanel();
 			JPanel portPanel = new JPanel();
 			ipPanel.add(ipLabel);
 			ipPanel.add(ipField);
 			portPanel.add(portLabel);
 			portPanel.add(portField);
 			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 			add(message);
 			add(ipPanel);
 			add(portPanel);
 		}
 		public String getIP() {
 			return ipField.getText();
 		}
 		public String getPort() {
 			return portField.getText();
 		}
 	}
 
 	public class UserResponder {
 		ActionListener newHandler;
 		ActionListener openHandler;
 		ActionListener saveHandler;
 		ActionListener saveAsHandler;
 		ActionListener exportHandler;
 		ActionListener connectHandler;
 		ActionListener serverHandler;
 		ActionListener colorHandler;
 		ActionListener addLayerHandler;
 		ActionListener brushHandler;
 		ActionListener eraserHandler;
 		ActionListener colorPickerHandler;
 		ActionListener quitHandler;
 		ActionListener advanceLayerHandler;
 		ActionListener moveLayerBackHandler;
 		ActionListener hideLayerHandler;
 		ActionListener showLayerHandler;
 		ActionListener layerVisibleHandler;
 		ActionListener layerSelectHandler;
 		DocumentListener changeWidthHandler;
 		boolean connectedToServer = false;
 		boolean serverRunning = false;
 
 		public UserResponder () {
 			newHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					DrawingWindow.newWindow();
 				}
 			};
 			exportHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					export();
 				}
 			};
 			saveHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					save();
 				}
 			};
 			saveAsHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					saveAs();
 				}
 			};
 			openHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					DrawingWindow.open();
 				}
 			};
 			connectHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					toggleConnect();
 				}
 			};
 			serverHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					toggleServer();
 				}
 			};
 			colorHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					changeColor(JColorChooser.showDialog(controlPanel, "New Color", drawing.currentColor));
 				}
 			};
 			addLayerHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					addLayer();
 				}
 			};
 			advanceLayerHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					moveLayerForward();
 				}
 			};
 			moveLayerBackHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					moveLayerBack();
 				}
 			};
 			showLayerHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					showCurrentLayer();
 				}
 			};
 			hideLayerHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					hideCurrentLayer();
 				}
 			};
 			layerVisibleHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					Layer currentLayer = drawing.layers[controlPanel.getCurrentLayer()];
 					boolean checkBoxState = menuBar.layerVisible.isSelected();
 					if (currentLayer.visible != checkBoxState) {
 						currentLayer.setVisible(checkBoxState);
 					}
 					drawingPanel.repaint();
 				}
 			};
 			layerSelectHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					reactToLayerSelection();
 				}
 			};
 			brushHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					brushToggle();
 				}
 			};
 			eraserHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					eraseToggle();
 				}
 			};
 			colorPickerHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					colorPickerToggle();
 				}
 			};
 			quitHandler = new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					System.exit(0);
 				}
 			};
 			changeWidthHandler = new DocumentListener() {
 				public void changedUpdate(DocumentEvent e) {}
 				public void removeUpdate(DocumentEvent e) {
 					drawingPanel.setNewCursor();
 				}
 				public void insertUpdate(DocumentEvent e) {
 					drawingPanel.setNewCursor();
 				}
 			};
 		}
 
 		public void setControlsToNoConnection() {
 			controlPanel.serverButton.setEnabled(true);
 			controlPanel.connectButton.setEnabled(true);
 			controlPanel.serverButton.setText("Start server");
 			controlPanel.connectButton.setText("Connect...");
 			serverRunning = false;
 			connectedToServer = false;
 			System.out.println("Set controls to no connection");
 		}
 		public void setControlsToServerRunning() {
 			controlPanel.serverButton.setEnabled(true);
 			controlPanel.connectButton.setEnabled(false);
 			controlPanel.serverButton.setText("Stop server");
 			controlPanel.connectButton.setText("Connect...");
 			serverRunning = true;
 			connectedToServer = false;
 		}
 		public void setControlsToConnectedToServer() {
 			controlPanel.serverButton.setEnabled(false);
 			controlPanel.connectButton.setEnabled(true);
 			controlPanel.serverButton.setText("Start server");
 			controlPanel.connectButton.setText("End connection");
 			serverRunning = false;
 			connectedToServer = true;
 		}
 
 		public void save() {
 			if (currentFile != null) {
 				tryToWriteDrawing(currentFile);
 			} else {
 				saveAs();
 			}
 		}
 
 		public void tryToWriteDrawing(File file) {
 			try {
 				fileHandler.writeDrawingToFile(file, drawing);
 				System.out.println("File saved");
 				menuBar.save.setEnabled(false);
 				currentFile = file;
 				JFrame parentWindow = (JFrame)SwingUtilities.getWindowAncestor(drawingPanel);
 				parentWindow.setTitle(file.getName());
 			} catch (IOException e) {
 				System.out.println("IOException: " + e);
 			}
 		}
 
 		public void saveAs() {
 			JFileChooser fc = new JFileChooser();
 			if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 				if (file.exists()) {
 					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 						consolePanel.tellUser("Overwriting file...");
 						tryToWriteDrawing(file);
 					} else {
 						consolePanel.tellUser("Cancelled...");
 					}
 				} else {
 					tryToWriteDrawing(file);
 				}
 			}
 		}
 
 		public void export() {
 			JFileChooser fc = new JFileChooser();
 			if (fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 				if (file.exists()) {
 					if (JOptionPane.showConfirmDialog(null, "Overwrite existing file?", "Overwrite?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 						consolePanel.tellUser("Overwriting file...");
 						fileHandler.exportFile(file, drawing);
 					} else {
 						consolePanel.tellUser("Cancelled...");
 					}
 				} else {
 					fileHandler.exportFile(file, drawing);
 				}
 			}
 		}
 
 		public void colorPickerToggle() {
 			menuBar.brush.setSelected(false);
 			menuBar.eraser.setSelected(false);
 			if (!menuBar.colorPicker.isSelected()) {
 				menuBar.colorPicker.setSelected(true);
 			}
 			drawingPanel.setNewCursor();
 		}
 
 		public void eraseToggle() {
 			menuBar.brush.setSelected(false);
 			menuBar.colorPicker.setSelected(false);
 			if (!menuBar.eraser.isSelected()) {
 				menuBar.eraser.setSelected(true);
 			}
 			drawingPanel.setNewCursor();
 		}
 
 		public void brushToggle() {
 			menuBar.eraser.setSelected(false);
 			menuBar.colorPicker.setSelected(false);
 			if (!menuBar.brush.isSelected()) {
 				menuBar.brush.setSelected(true);
 			}
 			drawingPanel.setNewCursor();
 		}
 
 		public void changeColor(Color c) {
 			drawing.currentColor = c;
 			setBorder(BorderFactory.createLineBorder(c, 2));
 			consolePanel.setBorder(BorderFactory.createLineBorder(c, 2));
 			drawingPanel.setNewCursor();
 		}
 
 		public void addLayer() {
 			int numLayers = controlPanel.layerSelect.getItemCount();
 			String defaultName = "Layer " + numLayers;
 			String name = (String)JOptionPane.showInputDialog(
 					SwingUtilities.getWindowAncestor(controlPanel),
 					"Enter layer name: ",
 					"New Layer",
 					JOptionPane.PLAIN_MESSAGE,
 					null,
 					null,
 					defaultName);
 			if (name != null) {
 				if (drawing.layerExists(name)) {
 					JOptionPane.showMessageDialog(null, "Name already in use! Please choose another.", "Cannot create layer", JOptionPane.ERROR_MESSAGE);
 					addLayer();
 				} else {
 					drawing.addLayer(drawing.panelWidth, drawing.panelHeight, name);
 					netController.addLayer(-1, drawing.panelWidth, drawing.panelHeight, name);
 					menuBar.save.setEnabled(true);
 					controlPanel.refigureLayers();
 					controlPanel.layerSelect.setSelectedIndex(0);
 				}
 			}
 		}
 
 		public void hideCurrentLayer() {
 			int currentLayer = controlPanel.getCurrentLayer();
 			drawing.layers[currentLayer].visible = false;
 			drawingPanel.repaint();
 		}
 
 		public void showCurrentLayer() {
 			int currentLayer = controlPanel.getCurrentLayer();
 			drawing.layers[currentLayer].visible = true;
 			drawingPanel.repaint();
 		}
 
 		public void reactToLayerSelection() {
 			Layer currentLayer = drawing.layers[controlPanel.getCurrentLayer()];
 			if (menuBar.layerVisible.isSelected() != currentLayer.visible) {
 				menuBar.layerVisible.setSelected(currentLayer.visible);
 			}
 			controlPanel.refigureLayerButtons(); 
 		}
 
 		public void moveLayerForward() {
 			Object currentItem = controlPanel.layerSelect.getSelectedItem();
 			int currentLayer = controlPanel.getCurrentLayer();
 			int maxLayer = controlPanel.getMaxLayer();
 			if (currentLayer < maxLayer) {
 				drawing.swapLayers(currentLayer, currentLayer+1);
 				menuBar.save.setEnabled(true);
 				controlPanel.refigureLayers();
 				controlPanel.layerSelect.setSelectedItem(currentItem);
 				drawingPanel.repaint();
 				netController.swapLayers(-1, currentLayer, currentLayer+1);
 			} else {
 				consolePanel.tellUser("Layer is already at front!");
 			}
 		}
 
 		public void moveLayerBack() {
 			Object currentItem = controlPanel.layerSelect.getSelectedItem();
 			int currentLayer = controlPanel.getCurrentLayer();
 			if (currentLayer > 0) {
 				drawing.swapLayers(currentLayer, currentLayer-1);
 				controlPanel.refigureLayers();
 				controlPanel.layerSelect.setSelectedItem(currentItem);
 				drawingPanel.repaint();
 				netController.swapLayers(-1, currentLayer, currentLayer-1);
 			} else {
 				consolePanel.tellUser("Layer is already at back!");
 			}
 		}
 
 		public void showConnectDialog() {
 			ConnectPanel connectPanel = new ConnectPanel();
 			int result = JOptionPane.showConfirmDialog(null, connectPanel, "Enter connection details...", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
 			if (result == JOptionPane.OK_OPTION) {
 				String ip = connectPanel.getIP();
 				int port = Integer.parseInt(connectPanel.getPort());
 				beginConnection(ip, port);
 			}
 		}
 
 		public void beginConnection(final String ip, final int port) {
 
 			connectionThread = new Thread() {
 				public void run() {
 					netController.connectToServer(ip, port);
 				}
 			};
 			connectionThread.start();
 			connectedToServer = true;
 			menuBar.connect.setText("Disconnect...");
 			menuBar.server.setEnabled(false);
 			controlPanel.connectButton.setText("End Connection");
 			controlPanel.serverButton.setEnabled(false);
 		}
 
 		public void toggleConnect() {
 			if (!connectedToServer) {
 				showConnectDialog();
 			} else {
 				netController.disconnectFromServer();
 				connectedToServer = false;
 				menuBar.connect.setText("Connect...");
 				menuBar.server.setEnabled(true);
 				controlPanel.connectButton.setText("Connect...");
 				controlPanel.serverButton.setEnabled(true);
 			}
 		}
 
 		public void toggleServer() {
 			if (!serverRunning) {
 				final String portInput = (String)JOptionPane.showInputDialog(
 						SwingUtilities.getWindowAncestor(controlPanel),
 						"Enter port:",
 						"Start Server",
 						JOptionPane.PLAIN_MESSAGE,
 						null,
 						null,
 						DEFAULT_PORT); 
 				if (portInput != null) {
 					final int port = Integer.parseInt(portInput);
 					menuBar.server.setText("Stop Server");
 					menuBar.connect.setEnabled(false);
 					userResponder.setControlsToServerRunning();
 					netController.startServer(port);
 				}
 			} else {
 				netController.stopServer();
 				menuBar.server.setText("Start Server...");
 				menuBar.connect.setEnabled(true);
 				controlPanel.serverButton.setText("Start Server...");
 				controlPanel.connectButton.setEnabled(true);
 				serverRunning = false;
 			}
 		}
 	} 
 
 	public class FileHandler {
 
 		private String generateLayerName(String init, Drawing newDrawing) {
 			int num = 0;
 			String name = init;
 			while (newDrawing.layerExists(name)) {
 				num = num + 1;
 				name = init + "_" + num;
 			}
 			return name;
 		}
 
 		private void insertImage(BufferedImage img, String name, Drawing newDrawing) {
 			int rgb;
 			int w;
 			int h;
 			Layer newLayer;
 			name = generateLayerName(name, newDrawing);
 			w = img.getWidth();
 			h = img.getHeight();
 			newLayer = newDrawing.addLayer(w, h, name);
 			for (Layer l: newDrawing.layers) {
 				System.out.println("Layer in newDrawing1 named: " + l.name);
 			}
 			newLayer.i = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
 			for (int x = 0; x < w; x++) {
 				for (int y = 0; y < h; y++) {
 					rgb = img.getRGB(x, y);
 					newLayer.i.setRGB(x, y, rgb);
 				}
 			}
 			menuBar.save.setEnabled(true);
 		}
 
 		public Drawing openFile (File file) throws FileNotFoundException, IOException {
 			String currentLayerName = "";
 			String chunk;
 			FileReader fr;
 			String[] chunkSplit;
 			BufferedReader br;
 			BufferedImage img;
 			String imageText = "";
 			Drawing newDrawing = new Drawing();
 			newDrawing.layers = new Layer[0];
 			newDrawing.firstLayerAdded = true; //Necessary so we don't overwrite our new layer the first time we repaint drawingPanel 
 			fr = new FileReader(file);
 			br = new BufferedReader(fr);
 			while ((chunk = getChunk(br)) != null) {
 				if (chunk.startsWith("NAMEIS")) {
 					if (!imageText.equals("!")) {
 						InputStream is = new ByteArrayInputStream(imageText.getBytes());
 						img = ImageIO.read(is);
 						insertImage(img, currentLayerName, newDrawing);
 					} 
 					imageText = "";
 					chunkSplit = chunk.split(":");
 					int len = chunkSplit[1].length();
 					currentLayerName = chunkSplit[1].substring(0, len-1);
 
 				} else {
 					imageText = imageText + chunk;
 				}
 			}
 			if (currentLayerName.equals("")) {
 				currentLayerName = "Imported Image";
 			}
 			InputStream is = new ByteArrayInputStream(imageText.getBytes());
 			img = ImageIO.read(is);
 			insertImage(img, currentLayerName, newDrawing);
 
 			newDrawing.panelWidth = img.getWidth();
 			newDrawing.panelHeight = img.getHeight();
 
 			return newDrawing;
 		}
 
 		private String getChunk(BufferedReader br) {
 			//Customized function for getting the next "chunk" of text from a BufferedReader. 
 			//Similar to readLine(), but uses '!' as a delimiter between chunks. readLine has 
 			//multiple delimiters (linefeeds, carriage returns, linefeeds+carriage returns), 
 			//and there doesn't seem to be any way to tell which one ended a particular line. 
 			//Using readLine would thus lead to data loss if used to read image files.
 			int i = -1;
 			try {
 				i = br.read();
 				if (i == -1) {
 					System.out.println("getChunk returning null");
 					return null;
 				}
 			} catch (Exception e) {
 				System.out.println("Error reading file 1: " + e);
 			}
 			char c = (char)i;
 			String s = c + "";
 			while (i != -1 && c != '!') {
 				try {
 					br.mark(1);
 					i = br.read();
 				} catch (Exception e) {
 					System.out.println("Error reading file 2: " + e);
 				}
 				c = (char)i;
 				s = s + c;
 			}
 			if (i == -1) {
 				try {
 					br.reset();
 				} catch (Exception e) {
 					System.out.println("Error reading file 3: " + e);
 				}
 				return s;
 			} else return (s);
 		}
 
 
 		public void writeDrawingToFile(File file, Drawing drawingToSave) throws IOException {
 			FileWriter fw;
 			PrintWriter pw;
 			Iterator<ImageWriter> iter;
 			ImageWriter writer;
 			ImageWriteParam iwp;
 			FileImageOutputStream output;
 			IIOImage img;
 			iter = ImageIO.getImageWritersByFormatName("png");
 			writer = (ImageWriter)iter.next();
 			iwp = writer.getDefaultWriteParam();
 			output = new FileImageOutputStream(file);
 			writer.setOutput(output);
 			fw = new FileWriter(file);
 			pw = new PrintWriter(fw);
 			synchronized(file) {
 				for (Layer layer: drawingToSave.layers) {
 					pw.print("!NAMEIS:" + layer.name + "!");
 					pw.close();
 					output.seek(file.length());
 					writer.setOutput(output);
 					img = new IIOImage(layer.i, null, null);
 					writer.write(null, img, iwp);
 					fw = new FileWriter(file, true);
 					pw = new PrintWriter(fw);
 				}
 			}
 			pw.close();
 			writer.dispose();
 
 		}
 
 		public void exportFile(File file, Drawing d) {
 			BufferedImage toSave = new BufferedImage(d.panelWidth, d.panelHeight, BufferedImage.TYPE_INT_RGB);
 			for (int x = 0; x < d.panelWidth; x++) {
 				for (int y = 0; y < d.panelHeight; y++) {
 					toSave.setRGB(x, y, d.getColorAt(x, y).getRGB());
 				}
 			}
 			try {
 				ImageIO.write(toSave, "png", file);
 				System.out.println("File " + file.getName() + " has been successfully written.");
 			} catch (Exception e) {
 				System.out.println("Error writing file: " + e);
 			}
 		}
 
 	}
 
 	public class Menu extends JMenuBar {
 
 		JMenu fileMenu = new JMenu("File");
 		JMenu drawingMenu = new JMenu("Drawing");
 		JMenu networkMenu = new JMenu("Network");
 		JMenuItem quit = new JMenuItem("Quit");
 		JMenuItem newDoc = new JMenuItem("New...");
 		JMenuItem save = new JMenuItem("Save");
 		JMenuItem saveAs = new JMenuItem("Save as...");
 		JMenuItem open = new JMenuItem("Open...");
 		JMenuItem export = new JMenuItem("Export as PNG...");
 		JCheckBoxMenuItem brush = new JCheckBoxMenuItem("Brush");
 		JCheckBoxMenuItem eraser = new JCheckBoxMenuItem("Eraser");
 		JCheckBoxMenuItem colorPicker = new JCheckBoxMenuItem("Color picker");
 		JMenuItem changeColor = new JMenuItem("Change color...");
 		JMenuItem addLayer = new JMenuItem("Add layer...");
 		JMenuItem moveLayerForward = new JMenuItem("Move layer forward");
 		JMenuItem moveLayerBack = new JMenuItem("Move layer back");
 		JMenuItem layerVisible = new JCheckBoxMenuItem("Layer visible");
 		JMenuItem connect = new JMenuItem("Connect...");
 		JMenuItem server = new JMenuItem("Start server...");
 		public Menu() {
 			newDoc.addActionListener(userResponder.newHandler);
 			open.addActionListener(userResponder.openHandler);
 			save.addActionListener(userResponder.saveHandler);
 			saveAs.addActionListener(userResponder.saveAsHandler);
 			export.addActionListener(userResponder.exportHandler);
 			server.addActionListener(userResponder.serverHandler);
 			connect.addActionListener(userResponder.connectHandler);
 			quit.addActionListener(userResponder.quitHandler);
 			changeColor.addActionListener(userResponder.colorHandler);
 			brush.setSelected(true);
 			brush.addActionListener(userResponder.brushHandler);
 			eraser.addActionListener(userResponder.eraserHandler);
 			colorPicker.addActionListener(userResponder.colorPickerHandler);
 			addLayer.addActionListener(userResponder.addLayerHandler);
 			moveLayerForward.addActionListener(userResponder.advanceLayerHandler);
 			moveLayerBack.addActionListener(userResponder.moveLayerBackHandler);
 			layerVisible.setSelected(true);
 			layerVisible.addActionListener(userResponder.layerVisibleHandler);
 			fileMenu.add(newDoc);
 			fileMenu.add(open);
 			fileMenu.add(save);
 			fileMenu.add(saveAs);
 			fileMenu.add(export);
 			fileMenu.add(quit);
 			drawingMenu.add(brush);
 			drawingMenu.add(eraser);
 			drawingMenu.add(colorPicker);
 			drawingMenu.addSeparator();
 			drawingMenu.add(changeColor);
 			drawingMenu.addSeparator();
 			drawingMenu.add(addLayer);
 			drawingMenu.add(moveLayerForward);
 			drawingMenu.add(moveLayerBack);
 			drawingMenu.add(layerVisible);
 			networkMenu.add(connect);
 			networkMenu.add(server);
 			add(fileMenu);
 			add(drawingMenu);
 			add(networkMenu);
 		}
 	}
 
 	public class ConsolePanel extends JPanel {   
 		JTextArea console;
 		synchronized public void tellUser(String s) {
 			console.append(s+"\n\n");
 			console.setCaretPosition(console.getText().length());
 		}
 		public ConsolePanel() {
 			setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
 			setLayout(new BorderLayout());
 			console = new JTextArea(5, 15);
 			JScrollPane consoleScroller = new JScrollPane(console);
 			console.setEditable(false);
 			console.setLineWrap(true);
 			add(consoleScroller, BorderLayout.CENTER);
 			tellUser("*************");
 			tellUser("  Welcome!  ");
 			tellUser("*************");
 		}
 	}
 
 	public class ControlPanel extends JPanel {
 		public JTextField lineWidthField;
 		boolean serverRunning = false;
 		boolean connectedToServer = false;
 		JButton serverButton = new JButton("Start server");
 		JButton connectButton = new JButton("Connect...");
 		JButton colorButton = new JButton("Color...");
 		JComboBox layerSelect = new JComboBox();
 		JButton moveLayerBackButton = new JButton("Move layer back");
 		JButton layerAddButton = new JButton("Add Layer");
 		JButton moveLayerForwardButton = new JButton("Move layer forward");
 
 		public int getCurrentLayer() {
 			return (layerSelect.getItemCount()-1)-layerSelect.getSelectedIndex();
 		}
 		public int getMaxLayer() {
 			return layerSelect.getItemCount()-1;
 		}
 		public Object getCurrentLayerName() {
 			return layerSelect.getSelectedItem();
 		}
 		public void setCurrentLayer(Object layerName) {
 			layerSelect.setSelectedItem(layerName);
 		}
 		public void setCurrentLayerSelectIndex(int i) {
 			layerSelect.setSelectedIndex(i);
 		}
 		public int getCurrentLayerSelectIndex() {
 			return layerSelect.getSelectedIndex();
 		}
 		public void refigureLayers() {
 			Object oldItem = layerSelect.getSelectedItem();
 			layerSelect.removeAllItems();
 			for (Layer layer: drawing.layers) {
 				layerSelect.insertItemAt(layer.name, 0);
 			}
 			if (oldItem == null) {
 				layerSelect.setSelectedIndex(0);
 			} else {
 				layerSelect.setSelectedItem(oldItem);
 			}
 			refigureLayerButtons();
 		}
 		public void refigureLayerButtons() {
 			if (getCurrentLayer() >= getMaxLayer()) {
 				moveLayerForwardButton.setEnabled(false);
 				menuBar.moveLayerForward.setEnabled(false);
 			} else {
 				moveLayerForwardButton.setEnabled(true);
 				menuBar.moveLayerForward.setEnabled(true);
 			}
 			if (getCurrentLayer() <= 0) {
 				moveLayerBackButton.setEnabled(false);
 				menuBar.moveLayerBack.setEnabled(false);
 			} else {
 				moveLayerBackButton.setEnabled(true);
 				menuBar.moveLayerBack.setEnabled(true);
 			}
 		}
 		public int getLineWidth() {
 			try { 
 				return Integer.parseInt(lineWidthField.getText());
 			} catch (NumberFormatException e) {
 				return 1;
 			}
 		}
 
 		public ControlPanel() {
 
 			setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
 			setLayout(new FlowLayout());
 			setBackground(Color.WHITE);
 
 			lineWidthField = new JTextField("1", 3);
 			JLabel lineWidthLabel = new JLabel("Width:");
 			layerSelect.addItem("Layer 0");
 			refigureLayers();
 
 
 			layerAddButton.addActionListener(userResponder.addLayerHandler);
 			moveLayerForwardButton.addActionListener(userResponder.advanceLayerHandler);
 			moveLayerBackButton.addActionListener(userResponder.moveLayerBackHandler);
 			serverButton.addActionListener(userResponder.serverHandler);
 			connectButton.addActionListener(userResponder.connectHandler);
 			colorButton.addActionListener(userResponder.colorHandler);
 			layerSelect.addActionListener(userResponder.layerSelectHandler);
 			lineWidthField.getDocument().addDocumentListener(userResponder.changeWidthHandler);
 			add(moveLayerBackButton);
 			add(moveLayerForwardButton);
 			add(layerSelect);
 			add(layerAddButton);
 			add(lineWidthLabel);
 			add(lineWidthField);
 			add(colorButton);
 			add(serverButton);
 			add(connectButton);
 			requestFocusInWindow();
 		}
 
 	}
 
 	public class NetController {
 		LinkedList<Connection> connections = new LinkedList<Connection>();
 		boolean serverRunning = false;
 		boolean connected = false;
 		int numConnections = 0;
 		int myID = 0;
 		ConnectionWaiter connectionWaiter;
 
 		public void addLayer(int ident, int width, int height, String name) {
 			for (Connection connection: connections) {
 				if (ident != connection.otherID) {
 					connection.addLayer(ident, width, height, name);
 				}
 			}
 		}
 
 		public void addToCurve(int ident, int x, int y) {
 			for (Connection connection: connections) {
 				if (ident != connection.otherID) {
 					connection.addToCurve(ident, x, y);
 				}
 			}
 		}
 
 		public void sendNewCurve(int ident, int startX, int startY, int lineWidth, Color color, boolean erase, int layer) {
 			for (Connection connection: connections) {
 				if (ident != connection.otherID) {
 					connection.sendNewCurve(ident, startX, startY, lineWidth, color, erase, layer);
 				}
 			}
 		}
 
 		public void swapLayers(int ident, int l1, int l2) {
 			for (Connection connection: connections) {
 				if (ident != connection.otherID) {
 					connection.swapLayers(ident, l1, l2);
 				}
 			}
 		}
 
 		public void handleRemoteDisconnect(Connection c) {
 			try {
 				c.connectionSocket.close();
 			} catch (Exception e) {
 				consolePanel.tellUser("Error in handling remote disconnection: " + e);
 			}
 			c.active = false;
 			if (serverRunning) {
 				consolePanel.tellUser("User number " + c.otherID + " disconnected.");
 				connections.remove(c);
 			} else {
 				consolePanel.tellUser("Server ended connection.");
 				controlPanel.connectButton.setText("Connect to server...");
 				controlPanel.serverButton.setEnabled(true);
 			}
 		}
 
 		public void disconnectFromServer() {
 			connected = false;
 			connections.peek().end();
 			numConnections = 0;
 			connections = new LinkedList<Connection>();
 		}
 
 		public void stopServer() {
 			serverRunning = false;
 			connectionWaiter.stopWaiting();
 			for (Connection connection: connections) {
 				connection.end();
 			}
 			numConnections = 0;
 			connected = false;
 			connections = new LinkedList<Connection>();
 		}
 
 		public void startServer(int port) {
 			try {
 				connectionWaiter = new ConnectionWaiter(port);
 				serverRunning = true;
 				connected = true;
 				connectionWaiter.start();
 			} catch (Exception e) {
 				consolePanel.tellUser("A connection error occurred: " + e);
 				userResponder.setControlsToNoConnection();
 			}
 		}
 
 		public BufferedImage string2Image(String s) {
 			InputStream is = new ByteArrayInputStream(s.getBytes());
 			BufferedImage img = null;
 			try {
 				img = ImageIO.read(is);
 			} catch (Exception e) {
 				System.out.println("Error reading image");
 			}
 			return img;
 		}
 
 		public String getImageStringFromReader(BufferedReader br) {
 			StringBuffer imageString = new StringBuffer("");
 			String in;
 			System.out.println("Trying to return image from a reader");
 			while (!(in = getChunk(br, '\n')).equals("ENDIMAGE\n")) {
 				imageString.append(in);
 			}
 			System.out.println("Returning an image from a reader");
 			return imageString.toString();
 		}
 
 		public BufferedImage getImageFromReader(BufferedReader br) {
 			return string2Image(getImageStringFromReader(br));
 		}
 
 		public String getChunk(BufferedReader br, char delim) {
 			int i = -1;
 			try {
 				i = br.read();
 				if (i == -1) {
 					return null;
 				}
 			} catch (Exception e) {
 				System.out.println("Error reading file 1: " + e);
 			}
 			char c = (char)i;
 			String s = c + "";
 			while (i != -1 && c != delim) {
 				try {
 					br.mark(1);
 					i = br.read();
 				} catch (Exception e) {
 					System.out.println("Error reading file 2: " + e);
 				}
 				c = (char)i;
 				s = s + c;
 			}
 			if (i == -1) {
 				try {
 					br.reset();
 				} catch (Exception e) {
 					System.out.println("Error reading file 3: " + e);
 				}
 				return s;
 			} else return (s);
 		}
 
 		public void correctDimensions(Connection connection) {
 			int myWidth = drawing.panelWidth;
 			int myHeight = drawing.panelHeight;
 			connection.outStream.println(myWidth);
 			connection.outStream.println(myHeight);
 			connection.outStream.flush();
 			int otherWidth = 0;
 			int otherHeight = 0;
 			try {
 				otherWidth = Integer.parseInt(connection.inStream.readLine());
 				otherHeight = Integer.parseInt(connection.inStream.readLine());
 			} catch(Exception e) {
 				consolePanel.tellUser("correctDimensions");
 				consolePanel.tellUser("A connection error occurred.");
 				consolePanel.tellUser("Error: " + e);
 			}
 			int newWidth = myWidth;
 			int newHeight = myHeight;
 			if (otherWidth > myWidth) {
 				newWidth = otherWidth;
 			}
 			if (otherHeight > myHeight) {
 				newHeight = otherHeight;
 			}
 			consolePanel.tellUser("Correcting dimensions within correctDimensions");
 			if (newHeight != myHeight || newWidth != myWidth) {
 				Dimension newDim = new Dimension(newWidth, newHeight);
 				drawing.resizeAllLayers(newDim);
 				consolePanel.tellUser("Layers resized within correctDimensions");
 				drawing.panelWidth = newDim.width;
 				drawing.panelHeight = newDim.height;
 				consolePanel.tellUser("Resizing drawing Panel");
 				System.out.println("drawingData.container: " + drawing.container);
 				consolePanel.tellUser("drawingData.container exists");
 				drawing.container.resizeDrawingPanel(newDim);
 
 			}
 		}
 
 		public void sendLayersToServer(Connection connection) {
 			for (Layer l: drawing.layers) {
 				consolePanel.tellUser("Sending layer to server: " + l.name);
 				connection.addLayerWithImage(myID, l.name, l.i);
 			}
 			connection.outStream.println("DONE");
 			connection.outStream.flush();
 		}
 
 		public void correctLayerNames(Connection connection) {
 			String in = "";
 			try {
 				in = connection.inStream.readLine();
 			} catch(Exception e) {
 				consolePanel.tellUser("Connection error while correcting layer names");
 				consolePanel.tellUser("Error: " + e);
 			}
 			while (in.startsWith("RL")) {
 				String[] processedIn = in.split(";");
 				String oldName = processedIn[2];
 				String newName = processedIn[3];
 				consolePanel.tellUser("Renaming " + oldName + " to " + newName + ".");
 				int oldLayerIndex = controlPanel.getCurrentLayerSelectIndex();
 				for (Layer l: drawing.layers) {
 					if (l.name.equals(oldName)) {
 						l.name = newName;
 						controlPanel.refigureLayers();
 						controlPanel.setCurrentLayerSelectIndex(oldLayerIndex);
 					}
 				}
 				try {
 					in = connection.inStream.readLine();
 				} catch(Exception e) {
 					consolePanel.tellUser("Connection error while correcting layer names");
 					consolePanel.tellUser("Error: " + e);
 				}
 			}
 		}
 
 		public void connectToServer(String ip, int port) {
 			try {
 				Socket socket = new Socket(ip, port);
 				connected = true;
 				Connection connection = new Connection(socket);
 				consolePanel.tellUser("Connected to server at " + connection.remoteAddress);
 				connections.add(connection);
 				myID = Integer.parseInt(connection.inStream.readLine());
 				connection.myID = myID;
 				connection.otherID = myID;
 				correctDimensions(connection);
 				sendLayersToServer(connection);
 				correctLayerNames(connection);
 				connection.start();
 			} catch (Exception e) {
 				consolePanel.tellUser("connectToServer");
 				consolePanel.tellUser("A connection error occurred.");
 				consolePanel.tellUser("Error: " + e);
 			}
 		}
 
 		public class ConnectionWaiter extends Thread {
 			ServerSocket listener;
 			Connection newConnection;
 			int port;
 			public ConnectionWaiter(int p) {
 				port = p;
 			}
 			public void stopWaiting() {
 				try {
 					listener.close();
 				} catch (Exception e) {
 					consolePanel.tellUser("Server ending error? " + e);
 				}
 			}
 			public void run() {
 				try {
 					listener = new ServerSocket(port);
 				} catch (Exception e) {
 					consolePanel.tellUser("Server problem 1 in method run: " + e);
 					serverRunning = false;
 					userResponder.setControlsToNoConnection();
 				}
 				while(serverRunning) {
 					try {
 						consolePanel.tellUser("Awaiting connections on port " + port + "...");
 						newConnection = new Connection(listener.accept());
 						consolePanel.tellUser("Connection established with " + newConnection.remoteAddress);
 						connections.add(newConnection);
 						numConnections += 1;
 						newConnection.outStream.println(numConnections);
 						newConnection.outStream.flush();
 						newConnection.otherID = numConnections;
 						LinkedList<Layer> newLayers = new LinkedList<Layer>();
 						String in;
 						String[] processedIn;
 						String newLayerName;
 						String oldLayerName;
 						Layer newLayer;
 						int insertionPoint = 0;
 						correctDimensions(newConnection);
 						while ((in = newConnection.inStream.readLine()).startsWith("LI;")) {
 							System.out.println("Receiving layer from connector");
 							processedIn = in.split(";");
 							oldLayerName = processedIn[4];
 							newLayerName = oldLayerName + "(" + processedIn[1] + ")";
 							int width = Integer.parseInt(processedIn[2]);
 							int height = Integer.parseInt(processedIn[3]);
 							newLayer = drawing.insertLayer(width, height, newLayerName, insertionPoint);
 							menuBar.save.setEnabled(true);
 							try {
 								newLayer.i = getImageFromReader(newConnection.inStream);
 							} catch (Exception e) {
 								consolePanel.tellUser("Error receiving " + oldLayerName + " from connector");
 							}
 							newLayers.add(newLayer);
 							insertionPoint++;
 							controlPanel.refigureLayers();
 							drawingPanel.repaint();
 							newConnection.renameLayer(0, oldLayerName, newLayerName);
 						}
 						newConnection.outStream.println("DONE");
 						newConnection.outStream.flush();
 						System.out.println("Just sent DONE from server");
 						for (Layer l: drawing.layers) {
 							if (!newLayers.contains(l)) {
 								newConnection.addLayerWithImage(0, l.name, l.i);
 								System.out.println("Sending layer " + l.name + "from server.");
 							} 
 						}
 						/*for (Layer l: drawingPanel.layers) {
 							consolePanel.tellUser("Sending layer and image for " + l.name);
 							newConnection.addLayerWithImage(0, l.name, l.i);
 						}*/
 						/*for (Curve c: drawingPanel.curves) {
 							consolePanel.tellUser("Sending a curve from server...");
 							newConnection.sendCurve(c);
 						}*/
 						newConnection.start();
 					} catch (Exception e) {
 						consolePanel.tellUser("Server problem 2 in method run: " + e);
 						serverRunning = false;
 						userResponder.setControlsToNoConnection();
 					}
 				}
 			}
 		}
 
 		public class Connection extends Thread {
 			PrintWriter outStream;
 			OutputStream socketOutStream;
 			InputStream socketInStream;
 			BufferedReader inStream;
 			Curve currentRemoteCurve;
 			Curve[] activeRemoteCurves = new Curve[1];
 			Socket connectionSocket;
 			int remoteLineWidth;
 			int otherID = 0;
 			int myID = 0;
 			String remoteAddress;
 			Color remoteColor;
 			volatile boolean active;
 
 			public void end() {
 				active = false;
 				outStream.println("END");
 				outStream.flush();
 				try {
 					connectionSocket.close();
 				} catch (Exception e) {
 					consolePanel.tellUser("Error: " + e);
 				}
 			}
 
 			public void run() {
 				while(active) {
 					handleRemoteAction();
 				}
 			}
 
 			public void swapLayers(int ident, int l1, int l2) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				outStream.println("SL"+";"+ident+";"+l1+";"+l2);
 				outStream.flush();
 			}
 
 			public void sendNewCurve(int ident, int startX, int startY, int lineWidth, Color color, boolean erase, int layer) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				outStream.println("NEW"+";"+ident+";"+lineWidth+";"+String.valueOf(color.getRGB())+";"+startX+";"+startY+";"+erase+";"+layer);
 				outStream.flush();
 			}
 
 			public void addToCurve(int ident, int x, int y) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				outStream.println("PT"+";"+ident+";"+x+";"+y);
 				outStream.flush();
 			}
 
 			public void sendCurve(Curve c) {
 				outStream.println("NEW"+";"+otherID+";"+c.lineWidth+";"+String.valueOf(c.color.getRGB())+";"+c.coords[0].x+";"+c.coords[0].y+";"+c.erase+";"+c.layer);
 				for (Coord coord: c.coords) {
 					outStream.println("PT"+";"+otherID+";"+coord.x+";"+coord.y);
 				}
 				outStream.flush();
 			}
 
 			public void renameLayer(int ident, String oldName, String newName) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				outStream.println("RL"+";"+ident+";"+oldName+";"+newName);
 				outStream.flush();
 			}
 
 			public void addLayer(int ident, int width, int height, String name) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				outStream.println("L"+";"+ident+";"+width+";"+height+";"+name);
 				outStream.flush();
 			}
 
 			public void addLayerWithImage(int ident, String name, BufferedImage img) {
 				if (ident == -1) {
 					ident = myID;
 				}
 				int width = img.getWidth();
 				int height = img.getHeight();
 				outStream.println("LI"+";"+ident+";"+width+";"+height+";"+name);
 				outStream.flush();
 				try {
 					ImageIO.write(img, "png", socketOutStream);
 				} catch (Exception e) {
 					consolePanel.tellUser("Could not send the image for layer named " + name + ".");
 				}
 				outStream.println();
 				outStream.println("ENDIMAGE");
 				outStream.flush();
 			}
 
 			public void handleRemoteAction() {
 				int x;
 				int y;
 				int layer;
 				String in;
 				String[] processedIn;
 				int receivedID;
 				boolean erase;
 				try {
 					in = inStream.readLine();
 				} catch(Exception e) {
 					System.out.println("Connection possibly interrupted: " + e);
 					in = "";
 				}
 				processedIn = in.split(";"); 
 				if (processedIn[0].equals("NEW")) {
 					/*The start of a new curve */
 					consolePanel.tellUser("Received 'NEW'");
 					receivedID = Integer.parseInt(processedIn[1]);
 					remoteLineWidth = Integer.parseInt(processedIn[2]);
 					remoteColor = new Color(Integer.parseInt(processedIn[3]));
 					x = Integer.parseInt(processedIn[4]);
 					y = Integer.parseInt(processedIn[5]);
 					erase = new Boolean(processedIn[6]);
 					layer = Integer.parseInt(processedIn[7]);
 					if (receivedID > activeRemoteCurves.length-1) {
 						Curve[] temp = new Curve[receivedID+1];
 						System.arraycopy(activeRemoteCurves, 0, temp, 0, activeRemoteCurves.length);
 						activeRemoteCurves = new Curve[receivedID+1];
 						System.arraycopy(temp, 0, activeRemoteCurves, 0, activeRemoteCurves.length);
 					}
 					activeRemoteCurves[receivedID] = drawing.startNewCurve(x, y, remoteLineWidth, remoteColor, erase, layer);
 					menuBar.save.setEnabled(true);
 					if (serverRunning) {
 						netController.sendNewCurve(receivedID, x, y, remoteLineWidth, remoteColor, erase, layer);
 					}
 				}
 				else if (processedIn[0].equals("PT")) {
 					/*A new point is being added to the connector's current curve*/
 					receivedID = Integer.parseInt(processedIn[1]);
 					x = Integer.parseInt(processedIn[2]);
 					y = Integer.parseInt(processedIn[3]);
 					drawing.addToCurve(x, y, activeRemoteCurves[receivedID]);
 					drawingPanel.repaint();
 					menuBar.save.setEnabled(true);
 					if (serverRunning) {
 						netController.addToCurve(receivedID, x, y);
 					}
 				}
 				else if (processedIn[0].equals("L")) {
 					/*Connector added a new blank layer*/
 					receivedID = Integer.parseInt(processedIn[1]);
 					int width = Integer.parseInt(processedIn[2]);
 					int height = Integer.parseInt(processedIn[3]);
 					String name = processedIn[4];
 					Layer newLayer = drawing.addLayer(width, height, name);
 					menuBar.save.setEnabled(true);
 					System.out.println("Receiving layer " + name);
 					newLayer.id = receivedID;
 					controlPanel.refigureLayers();
 				}
 				else if (processedIn[0].equals("RL")) {
 					/*Connector wants to rename layer*/
 					receivedID = Integer.parseInt(processedIn[1]);
 					String oldName = processedIn[2];
 					String newName = processedIn[3];
 					for (Layer l: drawing.layers) {
 						if (l.name == oldName) {
 							l.name = newName;
 						}
 					}
 					controlPanel.refigureLayers();
 				}
 				else if (processedIn[0].equals("LI")) {
 					/*Connector is sending a layer, along with image data to put in the layer*/
 					receivedID = Integer.parseInt(processedIn[1]);
 					int width = Integer.parseInt(processedIn[2]);
 					int height = Integer.parseInt(processedIn[3]);
 					String name = processedIn[4];
 					Layer newLayer = drawing.addLayer(width, height, name);
 					menuBar.save.setEnabled(true);
 					System.out.println("Receiving layer with image " + name);
 					try {
 						newLayer.i = getImageFromReader(inStream);
 					} catch (Exception e) {
 						consolePanel.tellUser("Error reading layer image for layer named " + newLayer.name + ".");
 					}
 					controlPanel.refigureLayers();
 					drawingPanel.repaint();
 				}
 				else if (processedIn[0].equals("SL")) {
 					/*Connector is swapping layers*/
 					receivedID = Integer.parseInt(processedIn[1]);
 					int l1 = Integer.parseInt(processedIn[2]);
 					int l2 = Integer.parseInt(processedIn[3]);
 					menuBar.save.setEnabled(true);
 					drawing.swapLayers(l1, l2);
 					controlPanel.refigureLayers();
 					drawingPanel.repaint();
 				}
 				else {
 					if (processedIn[0].equals("END")) {
 						netController.handleRemoteDisconnect(this);
 					}
 				}
 			}
 
 
 			public Connection(Socket s) {
 				connectionSocket = s;
 				active = true;
 				try {
 					socketOutStream = connectionSocket.getOutputStream();
 					socketInStream = connectionSocket.getInputStream();
 					inStream = new BufferedReader(new InputStreamReader(socketInStream));
 					outStream = new PrintWriter(socketOutStream);
 					remoteAddress = connectionSocket.getInetAddress().getHostName();
 				} catch (Exception e) {
 					System.out.println(e);
 				}
 			}
 		}
 	}
 
 	public class DPCursor {
 		/* Describes the cursor we'll use for the DrawingPanel. 
 		 * Contains a method to convert itself to a Java "Cursor"
 		 * object.
 		 */
 
 		DrawingPanel dp;
 
 		public static final int DEFAULTSHAPE = 0;
 		public static final int CROSSHAIR = 1;
 		public static final int CIRCLE = 2;
 		public static final int SQUARE = 3;
 
 		final int COLORCHANGETHRESHOLD = 400; 
 		/*How bright/dim the area under the cursor has to be to justify changing 
 		 * the cursor's color to make it more visible. The value of 400 was chosen to
 		 * ensure that, e.g., the cursor becomes white when most of the underlying area
 		 * is black. */
 
 		final int CURSORIMAGEPADDING = 2;
 		/*When we draw a circle-shaped cursor, the image needs to be a bit wider than the diameter of the
 		 * circle in order to contain the whole circle
 		 */
 
 		final int CROSSHAIRTHRESHOLD = 3;
 		/*When the cursor is small, we want to change it to a crosshair, rather than a circle the width
 		 * of the brush size. This gives the cursor width (in pixels) at which we make the change.*/  
 
 		Color color;
 		int shape;
 		int width;
 
 		public DPCursor(DrawingPanel dp, Color c, int s, int w) {
 			this.dp = dp;
 			shape = s;
 			color = c;
 			width = w;
 		}
 
 		public Boolean equals(DPCursor c) {
 			if (this.width == c.width && this.shape == c.shape && this.color.equals(c.color)) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		public Cursor convertToCursor() {
 			Cursor cursor;
 			int w = width;
 			if (shape == DEFAULTSHAPE) {
 				cursor = new Cursor(Cursor.DEFAULT_CURSOR);
 			} else {
 				BufferedImage img; //The buffered image we'll draw the new cursor image into, in order to pass to createCustomCursor
 				int cursorImageWidth; //The width and height of that buffered image. Now just set it wide enough...
 
 				if (shape == CROSSHAIR) { 
 					cursorImageWidth = 11; 
 				} else {
 					cursorImageWidth = w+CURSORIMAGEPADDING; 
 				}
 
 				img = new BufferedImage(cursorImageWidth, cursorImageWidth, BufferedImage.TYPE_INT_ARGB);
 				Graphics g = img.getGraphics();
 				g.setColor(color);
 				if (shape == CROSSHAIR) {
 					g.drawLine(cursorImageWidth/2-5, cursorImageWidth/2, cursorImageWidth/2+5, cursorImageWidth/2);
 					g.drawLine(cursorImageWidth/2, cursorImageWidth/2-5, cursorImageWidth/2, cursorImageWidth/2+5);
 				}
 				if (shape == CIRCLE) {
 					g.drawOval(cursorImageWidth/2-w/2, cursorImageWidth/2-w/2, w, w);
 				}
 				if (shape == SQUARE) {
 					g.drawRect(cursorImageWidth/2-w/2, cursorImageWidth/2-w/2, w, w);
 				}
 				g.dispose();
 				Point hotspot = new Point(cursorImageWidth/2, cursorImageWidth/2);
 				Toolkit tk = Toolkit.getDefaultToolkit();
 				cursor = tk.createCustomCursor(img, hotspot, "cursor");
 			}
 			return cursor;
 		}
 
 		private int clipCursorSize(int preferredSize) {
 			/* We want our cursor to take up an area that's preferredSize x preferredSize, 
 			 * but the system we're on might not allow cursors that size. Return the closest 
 			 * width/height we can actually use, assuming a square-shaped cursor.
 			 */
 			Toolkit tk = Toolkit.getDefaultToolkit();
 			Dimension closestAllowedDimension = 
 					tk.getBestCursorSize(preferredSize + CURSORIMAGEPADDING, preferredSize + CURSORIMAGEPADDING);
 			int closestW = closestAllowedDimension.width - CURSORIMAGEPADDING;
 			int closestH = closestAllowedDimension.height - CURSORIMAGEPADDING;
 			if (closestW != preferredSize) {
 				System.out.println("W: " + closestW);
 				System.out.println("H: " + closestH);
 			}
 			return Math.min(closestW, closestH);
 		}
 
 		public void makeCursorVisibleOn(Color c) {
 			//Change our cursor color in order to be visible on a background of the given color.
 			if (dp.getBrightness(c) > COLORCHANGETHRESHOLD) {
 				System.out.println("changing to black");
 				color = Color.black;
 			} else {
 				System.out.println("changing to white");
 				color = Color.white;
 			}
 		}
 
 		public DPCursor getNew(int x, int y) {
 			/* When passed the current mouse coordinates, return a cursor appropriate to the mouse's
 			 * current location: white if it's on a dark background, black if on a light background. */
 			int newWidth;
 			int newShape;
 			Color newColor;
 			newWidth = clipCursorSize(controlPanel.getLineWidth());
 
 			System.out.println(dp.fractionOfPixelsBrighterThan(getPointsCircle(x, y, newWidth), 0.5));
 
 			if (dp.averagePixelBrightness(getPointsCircle(x, y, newWidth)) > COLORCHANGETHRESHOLD) { 
 				newColor = Color.BLACK;
 			} else {
 				newColor = Color.WHITE;
 			}
 			if (menuBar.colorPicker.isSelected()) {
 				newShape = DEFAULTSHAPE;
 			} else if (menuBar.eraser.isSelected()) {
 				newShape = SQUARE;
 			} else {
 				if (newWidth < CROSSHAIRTHRESHOLD) {
 					newShape = CROSSHAIR;
 				} else {
 					newShape = CIRCLE;
 				}
 			}
 			return (new DPCursor(dp, newColor, newShape, newWidth));
 		}
 
 		public LinkedList<Coord> getPointsCircle(int x, int y, int w) {
 			int r = w/2; //Make the radius of the "circle" we're testing equal to the width/2
 			int pointX; //The x coordinate of the pixel currently being counted
 			int pointY; //The y coordinate of the pixel currently being counted
 
 			LinkedList<Coord> points = new LinkedList<Coord>();
 			for (int xi = - r; xi <= r; xi++) {
 				pointX = xi + x;
 				pointY = y - (int)Math.sqrt(r*r - xi*xi);
 				points.add(new Coord(pointX, pointY));
 				pointY = y + (int)Math.sqrt(r*r - xi*xi);
 				points.add(new Coord(pointX, pointY));
 			}
 			return points;
 		}
 	}
 
 	public class DPMouseMotionListener extends MouseMotionAdapter {
 		private int timesMouseMoved = 0;
 		public void mouseDragged(MouseEvent evt) {
 			handleMouseDragged(evt);
 		}
 		public void mouseMoved(MouseEvent evt) {
 			if (timesMouseMoved < 15) {
 				timesMouseMoved++;
 			} else {
 				timesMouseMoved = 0;
				DPCursor oldCursor = drawingPanel.dpCursor;
				DPCursor newCursor = oldCursor.getNew(evt.getX(), evt.getY());
				if (!newCursor.equals(oldCursor)) {
					drawingPanel.dpCursor = newCursor;
					drawingPanel.setCursor(drawingPanel.dpCursor.convertToCursor());
 				}
 			}
 		}
 		public void handleMouseDragged(MouseEvent evt) {
 			int x = evt.getX();
 			int y = evt.getY();
 			if (drawing.layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
 				drawing.currentCurve.addPoint(x, y);
 				menuBar.save.setEnabled(true);
 				if (netController != null && netController.connected == true) {
 					netController.addToCurve(-1, x, y);
 				}
 				drawingPanel.repaint();
 			} else if (menuBar.colorPicker.isSelected()) {
 				if (x<drawingPanel.getWidth() && x>0 && y<drawingPanel.getHeight() && y>0)
 					userResponder.changeColor(drawing.getColorAt(x, y));
 			}
 		}
 	}
 	
 	public class DPMouseListener extends MouseAdapter {
 		boolean mouseDown = false;
 		int lastX;
 		int lastY;
 		int lineWidth;
 
 		public void mousePressed(MouseEvent evt) {
 			drawingPanel.validate();
 			handleMouseDown(evt);
 			//When we start drawing, the cursor will have the current brush color behind it.
 			//Change the cursor color so that it stays visible against that background.
 			if (drawingPanel.dpCursor.shape == DPCursor.CIRCLE) {
 				drawingPanel.dpCursor.makeCursorVisibleOn(drawing.currentColor);
 				drawingPanel.makeCursorCurrentDPCursor();
 			}
 		}
 		public void mouseReleased(MouseEvent evt) {
 			handleMouseUp();
 		}
 
 		public void handleMouseDown(MouseEvent evt) {
 			int x = evt.getX();
 			int y = evt.getY();
 			if (!drawing.layers[controlPanel.getCurrentLayer()].visible && !menuBar.colorPicker.isSelected()) {
 				JOptionPane.showMessageDialog(null, "The current layer is invisible.", "Can't draw there", JOptionPane.ERROR_MESSAGE);
 			} else if (menuBar.colorPicker.isSelected()) {
 				if (x<drawingPanel.getWidth() && x>0 && y<drawingPanel.getHeight() && y>0)
 					userResponder.changeColor(drawing.getColorAt(x, y));
 			} else {
 				lineWidth = controlPanel.getLineWidth();
 				lastX = x;
 				lastY = y;
 				mouseDown = true;
 				boolean erase = menuBar.eraser.isSelected();
 				drawing.currentCurve = new Curve(lastX, lastY, lineWidth, drawing.currentColor, controlPanel.getCurrentLayer(), erase);
 				synchronized(drawing.curves) {
 					drawing.curves.add(drawing.currentCurve);
 				}
 				//newCurves.add(currentCurve);
 				if (netController!=null && netController.connected == true) {
 					netController.sendNewCurve(-1, lastX, lastY, lineWidth, drawing.currentColor, erase, controlPanel.getCurrentLayer());
 				}
 				menuBar.save.setEnabled(true);
 				drawingPanel.repaint();
 			}
 		}
 		public void handleMouseUp() {
 			mouseDown = false;
 			drawing.currentCurve.done = true;
 		}
 	}
 
 	public class DrawingPanel extends JPanel {
 
 		DPCursor dpCursor = new DPCursor(this, Color.black, DPCursor.DEFAULTSHAPE, 1);
 
 		public Dimension getMinimumSize() {
 			return getPreferredSize();
 		}
 
 		public Dimension getMaximumSize() {
 			return getPreferredSize();
 		}
 
 		public DrawingPanel() {
 			setBackground(Color.WHITE);
 			addMouseListener(new DPMouseListener());
 			addMouseMotionListener(new DPMouseMotionListener());
 		}
 
 
 		public void paintComponent(Graphics g) {
 			if (!drawing.firstLayerAdded) {
 				drawing.panelWidth = getWidth();
 				drawing.panelHeight = getHeight();
 				drawingSize = new Dimension(drawing.panelWidth, drawing.panelHeight);
 				drawing.layers[0] = new Layer(drawing.panelWidth, drawing.panelHeight, "Layer 0");
 				drawing.layers[0].id = drawing.nextLayerID;
 				drawing.nextLayerID++;
 				drawing.firstLayerAdded = true;
 				controlPanel.refigureLayers();
 			}
 			super.paintComponent(g);
 			Graphics2D layerG;
 
 			synchronized(drawing.curves) {
 				for (Curve c: drawing.curves) {
 					if (!c.done) {
 						layerG = (Graphics2D)drawing.layers[c.layer].i.getGraphics();
 						layerG.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 								RenderingHints.VALUE_ANTIALIAS_ON);
 						layerG.setColor(c.color);
 						drawing.drawCurve(c, layerG);
 					}
 				}
 			}
 			for (Layer layer: drawing.layers) {
 				if (layer.visible) g.drawImage(layer.i, 0, 0, this);
 			}
 		}
 
 		public float getBrightness2(Color c) {
 			float[] hsb;
 			hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
 			return hsb[2];
 		}
 
 		public int getBrightness(Color c) {
 			return c.getRed() + c.getGreen() + c.getBlue();
 		}
 
 		public float fractionOfPixelsBrighterThan(LinkedList<Coord> pixels, double threshold) {
 			float fraction = 0;
 			float brightness;
 			int numPixelsCounted = 0;
 			int numPixelsBrighter = 0;
 			float totalBrightness = 0;
 			for (Coord c: pixels) {
 				try {
 					brightness = getBrightness(drawing.getColorAt(c.x, c.y));
 					totalBrightness += brightness;
 					if (brightness > threshold) {
 						numPixelsBrighter++;
 					}
 					numPixelsCounted++;
 				} catch (ArrayIndexOutOfBoundsException e) {}
 			}
 			if (numPixelsCounted > 0) { //Avoid division by zero.
 				fraction = (float)numPixelsBrighter / (float)numPixelsCounted;
 			}
 			return fraction;
 		}
 
 		public int averagePixelBrightness(LinkedList<Coord> pixels) {
 			/* takes a list of Coords and gives the average "brightness" of the pixels
 			 * that they represent. "Brightness" means the sum of the R, G, and B */
 			int totalPixelBrightness = 0;
 			Color pixelColor; //The color of the pixel currently being counted
 			int n = 0;
 			for (Coord c: pixels) {
 				try {
 					pixelColor = drawing.getColorAt(c.x, c.y);
 					totalPixelBrightness += getBrightness(pixelColor);
 					n++;
 				} catch (Exception e) {}
 			}
 			if (n == 0) return 0;
 			else {
 				return totalPixelBrightness / n;
 			}
 		}
 
 
 		public void makeCursorCurrentDPCursor() {
 			setCursor(dpCursor.convertToCursor());
 		}
 
 		public void setNewCursor() {
 			//Function to be called when we need to change the cursor, but it isn't 
 			//necessarily in the drawingPanel, so we can't test the pixels that it's over.
 			//Just set to black.
 			DPCursor newDPCursor = dpCursor.getNew(0, 0);
 			newDPCursor.color = Color.black;
 			dpCursor = newDPCursor;
 			setCursor(newDPCursor.convertToCursor());
 		}
 	}
 }
