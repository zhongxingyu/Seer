 package org.pointrel.pointrel20120623.demos;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 import org.pointrel.pointrel20120623.core.Session;
 import org.pointrel.pointrel20120623.core.TransactionVisitor;
 import org.pointrel.pointrel20120623.core.Utility;
 
 import com.fasterxml.jackson.core.JsonEncoding;
 import com.fasterxml.jackson.core.JsonFactory;
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 
 // TODO: Make sure done writing before close
 // TODO: Background writing and reading as SwingWorker
 // TODO: How to know when reading/writing and how if locks GUI or otherwise user knows what is happening
 
 public class SimpleConceptMapApp {
 	public static String FrameNameBase = "Simple Concept Map";
 	
 	Session session;
 	
 	JPanel appPanel = new JPanel();
 	ConceptMapPanel mapPanel = new ConceptMapPanel();
 	JButton newConceptButton = new JButton("New concept...");
 	
 	private int ReloadFrequency_ms = 3000;
 
 	// TODO: Fix this so it is settable
	public static String DefaultConceptMapUUID = "default_concept_map";
	public String conceptMapUUIDForApp = DefaultConceptMapUUID;
 	
 	public SimpleConceptMapApp(Session session) {
 		this.session = session;
 	}
 
 	public static void main(String[] args) {
 		File archive = new File("./PointrelArchive");
 		Session session = new Session(archive);
 		// Session session = new Session("http://twirlip.com/pointrel/");
 		final JFrame frame = new JFrame(FrameNameBase);
 		final SimpleConceptMapApp app = new SimpleConceptMapApp(session);
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JPanel appPanel = app.openGUI();
 				frame.setSize(600, 600);
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				frame.add(appPanel);
 				frame.setVisible(true);
 			}
 		});
 	}
 	
 	public class ConceptMap {
 		final public static String ContentType = "text/vnd.pointrel.SimpleConceptMap.ConceptMap.json";
 		final public static String Version = "20120623.0.1.0";
 		
 		final String documentUUID;
 		final String timestamp;
 		final String userID;
 		final String title;
 		final String noteBody;
 		
 		public ArrayList<ConceptDrawable> drawables = new ArrayList<ConceptDrawable>();
 		
 		// From: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6352385
 		private String encodeColor(Color c) {
 			char[] buf = new char[7];
 			buf[0] = '#';
 			String s = Integer.toHexString(c.getRed());
 			if (s.length() == 1) {
 				buf[1] = '0';
 				buf[2] = s.charAt(0);
 			} else {
 				buf[1] = s.charAt(0);
 				buf[2] = s.charAt(1);
 			}
 			s = Integer.toHexString(c.getGreen());
 			if (s.length() == 1) {
 				buf[3] = '0';
 				buf[4] = s.charAt(0);
 			} else {
 				buf[3] = s.charAt(0);
 				buf[4] = s.charAt(1);
 			}
 			s = Integer.toHexString(c.getBlue());
 			if (s.length() == 1) {
 				buf[5] = '0';
 				buf[6] = s.charAt(0);
 			} else {
 				buf[5] = s.charAt(0);
 				buf[6] = s.charAt(1);
 			}
 			return String.valueOf(buf);
 		}
 		
 		public ConceptMap(String documentUUID, String timestamp, String userID, String title, String noteBody) {
			if (documentUUID == null) {
				throw new RuntimeException("documentUUID should not be null");
			}
 			this.documentUUID = documentUUID;
 			this.timestamp = timestamp;
 			this.userID = userID;
 			this.title = title;
 			this.noteBody = noteBody;
 		}
 		
 		public ConceptMap(byte[] content) throws IOException {
 			boolean typeChecked = false;
 			boolean versionChecked = false;
 			String documentUUID_Read = null;
 			String timestamp_Read = null;
 			String userID_Read = null;
 			String title_Read = null;
 			String noteBody_Read = null;
 			
 			JsonFactory jsonFactory = new JsonFactory();
 			ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
 			JsonParser jsonParser = jsonFactory.createJsonParser(inputStream);
 
 			if (jsonParser.nextToken() != JsonToken.START_OBJECT) {
 				throw new IOException("Expected data to start with an Object");
 			}
 			
 			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
 				String fieldName = jsonParser.getCurrentName();
 				jsonParser.nextToken();
 
 				if (fieldName.equals("type")) {
 					String type = jsonParser.getText();
 					if (!ContentType.equals(type)) {
 						throw new RuntimeException("Expected type of " + ContentType + "  but got : " + type);
 					}
 					typeChecked = true;
 				} else if (fieldName.equals("version")) {
 					String version = jsonParser.getText();
 					if (!Version.equals(version)) {
 						throw new RuntimeException("Expected version of " + Version + "  but got : " + version);
 					}
 					versionChecked = true;
 				} else if (fieldName.equals("drawables")) {
 					while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
 						String concept_Read = null;
 						String uuid_Read = null;
 						String color_Read = null;
 						String rectangle_Read = null;
 						while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
 							String drawableFieldName = jsonParser.getCurrentName();
 							jsonParser.nextToken();
 							if (drawableFieldName.equals("uuid")) {
 								uuid_Read = jsonParser.getText();
 							} else if (drawableFieldName.equals("concept")) {
 								concept_Read = jsonParser.getText();
 							} else if (drawableFieldName.equals("color")) {
 								color_Read = jsonParser.getText();
 							} else if (drawableFieldName.equals("rectangle")) {
 								rectangle_Read = jsonParser.getText();
 							}
 						}
 						// TODO: Check all four fields were read
 						Color color = Color.decode(color_Read);
 						Rectangle rectangle = decodeRectangle(rectangle_Read);
 						ConceptDrawable drawable = new ConceptDrawable(uuid_Read, concept_Read, color, rectangle);
 						drawables.add(drawable);
 					}
 				} else if (fieldName.equals("documentUUID")) {
 					documentUUID_Read = jsonParser.getText();
 				} else if (fieldName.equals("timestamp")) {
 					timestamp_Read = jsonParser.getText();
 				} else if (fieldName.equals("userID")) {
 					userID_Read = jsonParser.getText();
 				} else if (fieldName.equals("title")) {
 					title_Read = jsonParser.getText();
 				} else if (fieldName.equals("noteBody")) {
 					noteBody_Read = jsonParser.getText();
 				} else {
 					throw new IOException("Unrecognized field '" + fieldName + "'");
 				}
 			}
 			jsonParser.close();
 			
 			if (!typeChecked) {
 				throw new RuntimeException("Expected type of " + ContentType + "  but no type field");
 			}
 			
 			if (!versionChecked) {
 				throw new RuntimeException("Expected version of " + Version + "  but no version field");
 			}
 			
 			// TODO: Maybe different error handling for this condition?
 			if (documentUUID_Read == null) {
				throw new RuntimeException("The field documentUUID read should not be null");
 			}
 			
 			documentUUID = documentUUID_Read;
 			timestamp = timestamp_Read;
 			userID = userID_Read;
 			title = title_Read;
 			noteBody = noteBody_Read;
 
 		}
 
 		private Rectangle decodeRectangle(String stringWithFourIntegers) {
 			String[] parts = stringWithFourIntegers.split(" ");
 			if (parts.length != 4) throw new RuntimeException("Problem reading rectangle");
 			Rectangle rectangle = new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
 			return rectangle;
 		}
 
 		byte[] toJSONBytes() {
 			try {
 				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 				JsonFactory jsonFactory = new JsonFactory();
 				JsonGenerator jsonGenerator = jsonFactory.createJsonGenerator(outputStream, JsonEncoding.UTF8);
 
 				jsonGenerator.useDefaultPrettyPrinter();
 				  
 				jsonGenerator.writeStartObject();
 				jsonGenerator.writeStringField("type", ContentType);
 				jsonGenerator.writeStringField("version", Version);
 				jsonGenerator.writeStringField("documentUUID", documentUUID);
 				jsonGenerator.writeStringField("timestamp", timestamp);
 				jsonGenerator.writeStringField("userID", userID);
 				jsonGenerator.writeStringField("title", title);
 				jsonGenerator.writeStringField("noteBody", noteBody);
 				
 				jsonGenerator.writeArrayFieldStart("drawables");
 				for (ConceptDrawable drawable: drawables) {
 					jsonGenerator.writeStartObject();
 					jsonGenerator.writeStringField("uuid", drawable.uuid);
 					jsonGenerator.writeStringField("concept", drawable.concept);
 					jsonGenerator.writeStringField("color", encodeColor(drawable.color));
 					Rectangle rectangle = drawable.rectangle;
 					jsonGenerator.writeStringField("rectangle", "" + rectangle.x + " " + rectangle.y + " " + rectangle.width + " " + rectangle.height);
 					jsonGenerator.writeEndObject();
 				}
 				
 				jsonGenerator.writeEndArray();
 				jsonGenerator.writeEndObject();
 				jsonGenerator.close();
 				return outputStream.toByteArray();
 			} catch (IOException e) {
 				e.printStackTrace();
 				return null;
 			}
 		}
 		
 	}
 	
 	public class ConceptDrawable {
 		public String uuid;
 		public String concept;
 		public Color color = Color.black;
 		public Rectangle rectangle;
 		
 		public ConceptDrawable() {
 			uuid = Utility.generateUUID("org.pointrel.SimpleConceptMap.ConceptDrawable");
 		}
 		
 		public ConceptDrawable(String uuid, String concept, Color color, Rectangle rectangle) {
 			this.uuid = uuid;
 			this.concept = concept;
 			this.color = color;
 			this.rectangle = rectangle;
 		}
 		
 		boolean inBound(Point point) {
 			return rectangle.contains(point);
 		}
 		
 		void draw(Graphics2D graphics) {
 			graphics.setColor(color);
 			graphics.fillOval((int)rectangle.getX(), (int)rectangle.getY(), (int)rectangle.getWidth(), (int)rectangle.getHeight());
 
 			int xx = (int) rectangle.getWidth();
 			int yy = (int) rectangle.getHeight();
 			int w2 = graphics.getFontMetrics().stringWidth(concept) / 2;
 			int h2 = graphics.getFontMetrics().getDescent();
 			//g2d.fillRect(0, 0, xx, yy);
 			//g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
 			graphics.setPaint(Color.red);
 			graphics.drawString(concept, rectangle.x + xx / 2 - w2, rectangle.y + yy / 2 + h2);
 		}
 		
 		void setLocation(Point point) {
 			rectangle.setLocation(point);
 		}
 		
 //		@SuppressWarnings("unchecked")
 //		JSONObject asJSONObject() {
 //			JSONObject result = new JSONObject();
 //			result.put("uuid", uuid);
 //			result.put("concept", concept);
 //			result.put("rectangle.x", rectangle.x);
 //			result.put("rectangle.y", rectangle.y);
 //			result.put("rectangle.width", rectangle.width);
 //			result.put("rectangle.height", rectangle.height);
 //			result.put("rectangle", rectangle);
 //			result.put("color", color);
 //			return result;
 //		}
 	}
 	
 	@SuppressWarnings("serial") 
 	class ConceptMapPanel extends JPanel implements MouseListener, MouseMotionListener {
 		// TODO: Think about this concept -- is this mutable or not? When are new versions made? Is something changeable with list? Etc.
		ConceptMap conceptMap = new ConceptMap(DefaultConceptMapUUID, Utility.currentTimestamp(), "default_user@example.com", "", "");
 		ConceptDrawable selected = null;
 		Point down = null;
 		Point offset = null;
 		
 		ConceptMapPanel() {
 			this.addMouseListener(this);
 			this.addMouseMotionListener(this);
 			ConceptDrawable drawable1 = new ConceptDrawable();
 			drawable1.rectangle = new Rectangle(20, 20, 100, 100);
 			drawable1.concept = "test one";
 			conceptMap.drawables.add(drawable1);
 			ConceptDrawable drawable2 = new ConceptDrawable();
 			drawable2.rectangle = new Rectangle(50, 50, 100, 100);
 			drawable2.color = Color.yellow;
 			drawable2.concept = "test two";
 			conceptMap.drawables.add(drawable2);
 		}
 		
 	    @Override 
 	    public void paintComponent(Graphics g) {
 	         super.paintComponent(g); 
 	         Graphics2D graphics = (Graphics2D) g;
 	         graphics.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 	         
 	         for (ConceptDrawable drawable: conceptMap.drawables) {
 	        	 	drawable.draw(graphics);
 	         }
 	    }
 
 
 		public void mouseClicked(MouseEvent arg0) {
 			// Do nothing
 		}
 
 		public void mousePressed(MouseEvent e) {
 			down = e.getPoint();
 			for (int i = conceptMap.drawables.size() - 1; i >= 0; i--) {
 				ConceptDrawable drawable = conceptMap.drawables.get(i);
 				if (drawable.inBound(down)) {
 					selected = drawable;
 					offset = new Point((int)drawable.rectangle.getX() - down.x, (int)drawable.rectangle.getY() - down.y);
 					conceptMap.drawables.remove(i);
 					conceptMap.drawables.add(selected);
 					this.repaint();
 					return;
 				}
 			}
 			selected = null;
 			down = null;
 			offset = null;
 		}
 
 		public void mouseReleased(MouseEvent arg0) {
 			if (selected != null) saveConceptMap();
 			selected = null;
 			down = null;
 			offset = null;
 		}
 
 		public void mouseEntered(MouseEvent arg0) {
 			// Do nothing
 		}
 
 		public void mouseExited(MouseEvent arg0) {
 			// Do nothing
 		}
 
 		public void mouseDragged(MouseEvent e) {
 			if (selected == null) return;
 			// TODO: Constrain on screen
 			selected.rectangle.setLocation(e.getX() + offset.x, e.getY() + offset.y);
 			this.repaint();
 		}
 
 		public void mouseMoved(MouseEvent arg0) {
 			// Do nothing			
 		}
 		
 	}
 
 	public JPanel openGUI() {
 		appPanel.setLayout(new BorderLayout());
 		appPanel.add(mapPanel, BorderLayout.CENTER);
 		appPanel.add(newConceptButton, BorderLayout.SOUTH);
 		
 		hookupActions();
 		return appPanel;
 	}
 
 	private void hookupActions() {
 		newConceptButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {newConceptButtonPressed();}});
 		
 		// Update every ten seconds
 		ActionListener runnable = new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				checkForAndLoadUpdatedConceptMap();
 			}
 		};
 		runnable.actionPerformed(null);
 		Timer timer = new Timer(ReloadFrequency_ms , runnable);
 		timer.start();
 	}
 
 	protected void newConceptButtonPressed() {
 		String name = JOptionPane.showInputDialog(appPanel, "Concept name?");
 		if (name == null || name.length() == 0) return;
 		// TODO: Ensure no newlines in name
 		ConceptDrawable drawable = new ConceptDrawable();
 		drawable.rectangle = new Rectangle(250, 250, 100, 100);
 		drawable.color = Color.blue;
 		drawable.concept = name;
 		mapPanel.conceptMap.drawables.add(drawable);
 		mapPanel.repaint();
 		saveConceptMap();
 	}
 
 //	@SuppressWarnings("unchecked")
 //	private void saveConceptMap() {
 //		JSONArray list = new JSONArray();
 //		for (ConceptDrawable drawable: mapPanel.drawables) {
 //			list.add(drawable.asJSONObject());
 //		}
 //		StringWriter out = new StringWriter();
 //		try {
 //			JSONValue.writeJSONString(list, out);
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 //		String jsonText = out.toString();
 //		System.out.println(jsonText);
 //	}
 	
 	private void saveConceptMap() {
 		// Note that Jackson ObjectMapper uses Java Reflection which may not run under Web Start or as an Applet due to security issues; would it help to set CAN_OVERRIDE_ACCESS_MODIFIERS to false?
 //		ObjectMapper mapper = new ObjectMapper();
 //		ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
 //		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 //		try {
 //			writer.writeValue(outputStream, mapPanel.conceptMap);
 //		} catch (JsonGenerationException e) {
 //			e.printStackTrace();
 //		} catch (JsonMappingException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 //		String jsonString = null;
 //		try {
 //			jsonString = outputStream.toString("utf-8");
 //		} catch (UnsupportedEncodingException e) {
 //			e.printStackTrace();
 //		}
 		byte[] jsonBytes = mapPanel.conceptMap.toJSONBytes();
 		System.out.println(new String(jsonBytes));
 		
 		// TODO: Fix user
 		session.setUser("tester1@example.com");
 		String conceptMapVersionURI = session.addContent(jsonBytes, ConceptMap.ContentType);
		System.out.println("Writing concept map ==============================================");
 		session.addSimpleTransactionForVariable(session.getWorkspaceVariable(), conceptMapVersionURI, "new concept map version");
		System.out.println("Just wrote new concept map: " + new String(jsonBytes));
 		//ConceptMap newMap = gson.fromJson(json, ConceptMap.class);
 		//System.out.println(newMap.drawables.size());
 	}
 	
 	private void checkForAndLoadUpdatedConceptMap() {
 		System.out.println("checkForAndLoadUpdatedConceptMap test");
 		if (mapPanel.selected != null) return;
 		System.out.println("checkForAndLoadUpdatedConceptMap proceeding");
 		// TODO: Fix this so it searches better
 		// byte[] jsonBytes = session.getResourceInSimpleTransactionForVariable(session.getWorkspaceVariable());
 		// if (jsonBytes == null || jsonBytes.length == 0) return;
 		
 		// ByteArrayInputStream inputStream = new ByteArrayInputStream(jsonBytes);
 //		// Note that Jackson ObjectMapper uses Java Reflection which may not run under Web Start or as an Applet due to security issues; would it help to set CAN_OVERRIDE_ACCESS_MODIFIERS to false?
 //		ObjectMapper mapper = new ObjectMapper();
 //		ConceptMap newMap = null;
 //		try {
 //			newMap = mapper.readValue(inputStream, ConceptMap.class);
 //		} catch (JsonParseException e) {
 //			e.printStackTrace();
 //		} catch (JsonMappingException e) {
 //			e.printStackTrace();
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 		//ConceptMap newMap = null;
 		//try {
 		//	newMap = new ConceptMap(jsonBytes);
 		//} catch (IOException e) {
 		//	e.printStackTrace();
 		//}
		ConceptMap newMap = loadLatestConceptMapForUUID(conceptMapUUIDForApp);
 		if (newMap != null) {
 			mapPanel.conceptMap = newMap;
 			mapPanel.repaint();
 		}
 	}
 	
 	// TODO: Genericize the code below which is from SimpleNoteTakerApp, so the common patters is common, also to ChatApp
 	// TODO: Should have an index
 	class ConceptMapVersionCollector extends TransactionVisitor {
 		final String encodedContentType = Utility.encodeContentType(ConceptMap.ContentType);
 		ArrayList<ConceptMap> conceptMaps = new ArrayList<ConceptMap>();
 		final int maximumCount;
 		final String documentUUID;
 		
 		ConceptMapVersionCollector(String documentUUID, int maximumCount) {
 			this.documentUUID = documentUUID;
 			this.maximumCount = maximumCount;
 		}
 		
 		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
 		
 		public boolean resourceInserted(String resourceUUID) {
 			if (!resourceUUID.endsWith(encodedContentType)) return false;
 			byte[] conceptMapContent = session.getContentForURI(resourceUUID);
 			ConceptMap conceptMap;
 			try {
 				conceptMap = new ConceptMap(conceptMapContent);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
			System.out.println("conceptMap.documentUUID: " + conceptMap.documentUUID);
 			if (conceptMap.documentUUID.equals(documentUUID)) {
 				conceptMaps.add(conceptMap);
 				if (maximumCount > 0 && conceptMaps.size() >= maximumCount) return true;
 			}
 			return false;
 		}
 	}
 	
 	class ConceptMapUUIDCollector extends TransactionVisitor {
 		final String encodedContentType = Utility.encodeContentType(ConceptMap.ContentType);
 		final HashSet<String> conceptMapUUIDs = new HashSet<String>();
 		final int maximumCount;
 		
 		ConceptMapUUIDCollector(int maximumCount) {
 			this.maximumCount = maximumCount;
 		}
 		
 		// TODO: Maybe should handle removes, too? Tricky as they come before the inserts when recursing
 		
 		public boolean resourceInserted(String resourceUUID) {
 			if (!resourceUUID.endsWith(encodedContentType)) return false;
 			byte[] conceptMapContent = session.getContentForURI(resourceUUID);
 			ConceptMap conceptMap;
 			try {
 				conceptMap = new ConceptMap(conceptMapContent);
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			conceptMapUUIDs.add(conceptMap.documentUUID);
 			if (maximumCount > 0 && conceptMapUUIDs.size() >= maximumCount) return true;
 			return false;
 		}
 	}
 	
 	// Finds most recently added version of concept map
 	ConceptMap loadLatestConceptMapForUUID(String uuid) {
 		ArrayList<ConceptMap> listItems = loadConceptMapVersionsForUUID(uuid, 1);
 		if (listItems == null || listItems.isEmpty()) return null;
 		return listItems.get(0);
 	}
 	
	// Finds all added versions of a concept map up to a maximumCount (use zero for all)
 	ArrayList<ConceptMap> loadConceptMapVersionsForUUID(String uuid, int maximumCount) {
 		// TODO: Should create, maintain, and use an index
 		String transactionURI = session.getVariable(session.getWorkspaceVariable());
 		ConceptMapVersionCollector visitor = new ConceptMapVersionCollector(uuid, maximumCount);
 		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(session, transactionURI, visitor);
 		if (visitor.conceptMaps.isEmpty()) return null;
 		return visitor.conceptMaps;			
 	}
 	
	// Finds all uuids for concept maps up to a maximumCount (use zero for all)
 	Set<String> loadConceptMapUUIDs(int maximumCount) {
 		// TODO: Should create, maintain, and use an index
 		String transactionURI = session.getVariable(session.getWorkspaceVariable());
 		ConceptMapUUIDCollector visitor = new ConceptMapUUIDCollector(maximumCount);
 		TransactionVisitor.visitAllResourcesInATransactionTreeRecursively(session, transactionURI, visitor);
 		if (visitor.conceptMapUUIDs.isEmpty()) return new HashSet<String>();
 		return visitor.conceptMapUUIDs;			
 	}
 }
