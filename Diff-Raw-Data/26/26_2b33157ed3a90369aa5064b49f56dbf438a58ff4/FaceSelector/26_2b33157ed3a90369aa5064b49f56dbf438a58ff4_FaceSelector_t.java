 package nl.cs.uu.faceselector;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map.Entry;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.events.ShellListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.graphics.Transform;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * User interface, data collection and type declarations for annotating facial
  * features.
  * 
  * @author Paul Lammertsma
  */
 public class FaceSelector {
 
 	/**
 	 * Path to photographs
 	 */
 	private final static String PATH = "../Subjects/";
 
 	/**
 	 * Valid file extensions for photographs
 	 */
 	private static final String[] EXTENSIONS = new String[] { ".jpg", ".png" };
 
 	/**
 	 * Show debug information
 	 */
 	private static final boolean DEBUG = false;
 
 	/*
 	 * Various fields; be sure to add them to FIELDS_TOGGLE or FIELDS_COORD
 	 */
 	private static final Field FIELD_HEAD_T = new Field("Head top", "headT",
 			new Style(Style.LINE_ABOVE, "T"));
 	private static final Field FIELD_HEAD_B = new Field("Head bottom", "headB",
 			new Style(Style.LINE_BELOW, "B"));
 	private static final Field FIELD_EYE_L = new Field("Eye left", "eyeL",
 			new Style(Style.CIRCLE, "L"));
 	private static final Field FIELD_EYE_R = new Field("Eye right", "eyeR",
 			new Style(Style.CIRCLE, "R"));
 	private static final Field FIELD_NOSE = new Field("Nose", "nose",
 			new Style(Style.BOX, "N"));
 	private static final Field FIELD_MOUTH = new Field("Mouth", "mouth",
 			new Style(Style.BOX, "M"));
 
 	private static final Field FIELD_CROP_T = new Field("Crop top", "cropT");
 	private static final Field FIELD_CROP_R = new Field("Crop right", "cropR");
 	private static final Field FIELD_CROP_B = new Field("Crop bottom", "cropB");
 	private static final Field FIELD_CROP_L = new Field("Crop left", "cropL");
 
 	/**
 	 * Array of {@link Field}s that take a boolean value.
 	 */
 	private static final Field[] FIELDS_TOGGLE = new Field[] { FIELD_CROP_T,
 			FIELD_CROP_R, FIELD_CROP_B, FIELD_CROP_L };
 	/**
 	 * Array of {@link Field}s that take a {@link Point} value (specified by
 	 * clicking in the photograph).
 	 */
 	private static final Field[] FIELDS_COORD = new Field[] { FIELD_HEAD_T,
 			FIELD_HEAD_B, FIELD_EYE_L, FIELD_EYE_R, FIELD_NOSE, FIELD_MOUTH };
 
 	private static final Line[] LINES = new Line[] {
 			new Line(FIELD_EYE_L, FIELD_EYE_R),
 			new Line(FIELD_HEAD_T, FIELD_HEAD_B),
 		};
 
 	private static Statistic[] statistics = new Statistic[] {
 			new Statistic("Facial features", new Field[] { FIELD_EYE_L,
 					FIELD_EYE_R, FIELD_MOUTH }, null, false),
 			new Statistic("Haar", new Field[] { FIELD_HEAD_T, FIELD_HEAD_B,
 					FIELD_EYE_L, FIELD_EYE_R }, 10.0),
 			new Statistic("Android", new Field[] { FIELD_EYE_L, FIELD_EYE_R,
 					FIELD_MOUTH }, 10.0),
 			};
 
 	protected static final double CIRCLE_SIZE = 5.0;
 	protected static final int FONT_SIZE = (int) (CIRCLE_SIZE * 2);
 
 	private static final Point EXPECTED_IMAGE_SIZE = new Point(176, 144);
 
 	private static final double RADIANS_TO_DEGREES = 180 / Math.PI;
 
 	private static final boolean IMAGE_CONTROL_VERTICAL = false;
 
 	private static final boolean TRANSLATE_TO_ORIGIN = true;
 
 	private static final double GOLDEN_RATIO = (1 + Math.sqrt(5)) / 2;
 
 	private static LinkedList<File> files = new LinkedList<File>();
 	private static AnnotationData curData = new AnnotationData();
 
 	private static Display display;
 	private static Shell shell;
 	private static Composite imgBox;
 
 	private static Image curImg;
 	private static int curFile = -1;
 
 	protected static float scale;
 
 	private static Button buttonImageNumber;
 	private static Button buttonT, buttonR, buttonB, buttonL;
 	private static Label[] label1;
 	private static ProgressLabel[] label2;
 	private static ProgressLabel label3;
 
 	private static int currentLabel;
 
 	private static boolean onlyIncomplete;
 	protected static Color color1, color2, color3, color4;
 	protected static Font font;
 
	private static double frameSize = 0;
 
 	/**
 	 * Creates and displays the {@link FaceSelector} shell.
 	 * 
 	 * @param args
 	 */
 	public static void main(final String[] args) {
 		display = new Display();
 		shell = new Shell(display);
 
 		color1 = new Color(display, 0, 0, 0);
 		color2 = new Color(display, 0, 255, 0);
 		color3 = new Color(display, 0, 128, 160);
 		color4 = new Color(display, 255, 255, 255);
 		font = new Font(display, "Courier New", FONT_SIZE, SWT.NORMAL);
 
 		display.addFilter(SWT.KeyDown, new Listener() {
 			@Override
 			public void handleEvent(final Event e) {
 				Button button = null;
 				switch (e.keyCode) {
 				case SWT.ARROW_RIGHT:
 					if ((e.stateMask & SWT.SHIFT) > 0) {
 						setOffset(1, 0);
 					} else {
 						setFile(curFile + 1, true);
 					}
 					break;
 				case SWT.ARROW_LEFT:
 					if ((e.stateMask & SWT.SHIFT) > 0) {
 						setOffset(-1, 0);
 					} else {
 						setFile(curFile - 1, true);
 					}
 					break;
 				case SWT.ARROW_DOWN:
 					if ((e.stateMask & SWT.SHIFT) > 0) {
 						setOffset(0, 1);
 					} else {
 						setFile(curFile + 1, true);
 					}
 					break;
 				case SWT.ARROW_UP:
 					if ((e.stateMask & SWT.SHIFT) > 0) {
 						setOffset(0, -1);
 					} else {
 						setFile(curFile - 1, true);
 					}
 					break;
 				case SWT.PAGE_DOWN:
 					setFile(curFile + 1, true);
 					break;
 				case SWT.PAGE_UP:
 					setFile(curFile - 1, true);
 					break;
 				case SWT.DEL:
 					File file;
 					try {
 						file = files.get(curFile).getCanonicalFile();
 						final File destFile = new File(file + ".bak");
 						final int result = showMessage(
 								SWT.YES | SWT.NO | SWT.ICON_QUESTION,
 								"This file will be renamed to:\n\n    "
 										+ destFile
 										+ "\n\nAre you sure you want to exclude it?");
 						if (result == SWT.YES) {
 							if (file.renameTo(destFile)) {
 								files.remove(curFile);
 								setFile(curFile, true);
 							} else {
 								showMessage(
 										SWT.ERROR,
 										"Failed to rename the following file:\n\n    "
 												+ file);
 							}
 						}
 					} catch (final IOException e1) {
 						fatal(e1);
 					}
 					break;
 				case SWT.ESC:
 					shell.close();
 					break;
 				case 'z':
 					if ((e.stateMask & SWT.CTRL) == 0) {
 						// Only CTRL+Z resets
 						break;
 					}
 				case 'r':
 					if (isAnnotated()) {
 						final int result = showMessage(SWT.YES | SWT.NO
 								| SWT.ICON_QUESTION, "Revert data from file?");
 						if (result == SWT.YES) {
 							load();
 						}
 					}
 					break;
 				case 'w':
 					// Crop TOP
 					button = buttonT;
 					break;
 				case 'a':
 					// Crop LEFT
 					button = buttonL;
 					break;
 				case 's':
 					// Crop BOTTOM
 					button = buttonB;
 					break;
 				case 'd':
 					// Crop RIGHT
 					button = buttonR;
 					break;
 				case 'c':
 					// Copy data from previous frame
 					try {
 						if (isAnnotated()) {
 							if (showMessage(SWT.ICON_QUESTION | SWT.YES
 									| SWT.NO, "Discard current data?") == SWT.NO) {
 								break;
 							}
 						}
 						curData = getData(curFile - 1);
 						imgBox.redraw();
 					} catch (Exception ex) {
 						showMessage(SWT.ICON_ERROR, ex.getMessage());
 					}
 					break;
 				case '[':
 					rotate(-0.5f / RADIANS_TO_DEGREES);
 					break;
 				case ']':
 					rotate(0.5f / RADIANS_TO_DEGREES);
 					break;
 				case '-':
 				case SWT.KEYPAD_SUBTRACT:
 					scale(1 / 1.01f);
 					break;
 				case '=':
 					if ((e.stateMask & SWT.SHIFT) == 0) {
 						break;
 					}
 				case SWT.KEYPAD_ADD:
 					scale(1.01f);
 					break;
 				}
 				if (button != null) {
 					button.setSelection(!button.getSelection());
 					setData(button);
 				}
 			}
 		});
 
 		final String msg = "Finding unannotated files... ";
 		if (DEBUG) {
 			System.out.println(msg);
 		} else {
 			System.out.print(msg);
 		}
 		final File path = new File(PATH);
 		onlyIncomplete = true;
 		listFiles(path);
 		onlyIncomplete = false;
 		System.out.println(FaceSelector.files.size() + " file(s)");
 
 		if (files.size() > 0) {
 			final int result = showMessage(
 					SWT.YES | SWT.NO | SWT.ICON_QUESTION,
 					"Would you like to display only unannotated files?");
 			if (result == SWT.YES) {
 				onlyIncomplete = true;
 			}
 		}
 
 		if (!onlyIncomplete) {
 			files.clear();
 			curData.clear();
 			System.out.print("Collecting files... ");
 			listFiles(path);
 		}
 		if (files.size() == 0) {
 			fatal("No images found in path:\n\t" + PATH);
 		} else {
 			System.out.println(FaceSelector.files.size() + " file(s)");
 		}
 
 		final SelectionListener croppedListener = new SelectionListener() {
 			@Override
 			public void widgetSelected(final SelectionEvent e) {
 				if (e.getSource() != null && e.getSource() instanceof Button) {
 					final Button button = (Button) e.getSource();
 					setData(button);
 				} else {
 					throw new RuntimeException("Source is not a Button");
 				}
 			}
 
 			@Override
 			public void widgetDefaultSelected(final SelectionEvent e) {
 			}
 		};
 
 		FormData fd1;
 		final Group group1 = new Group(shell, SWT.NORMAL);
 		{
 			group1.setText("Cropped");
 			fd1 = new FormData();
 			fd1.bottom = new FormAttachment(100, -2);
 			fd1.left = new FormAttachment(0, 2);
 			group1.setLayoutData(fd1);
 			final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 			rowLayout.fill = true;
 			rowLayout.justify = true;
 			rowLayout.center = true;
 			rowLayout.pack = false;
 			group1.setLayout(rowLayout);
 			buttonT = new Button(group1, SWT.TOGGLE);
 			setButton(buttonT, FIELD_CROP_T);
 			buttonT.addSelectionListener(croppedListener);
 			buttonR = new Button(group1, SWT.TOGGLE);
 			setButton(buttonR, FIELD_CROP_R);
 			buttonR.addSelectionListener(croppedListener);
 			buttonB = new Button(group1, SWT.TOGGLE);
 			setButton(buttonB, FIELD_CROP_B);
 			buttonB.addSelectionListener(croppedListener);
 			buttonL = new Button(group1, SWT.TOGGLE);
 			setButton(buttonL, FIELD_CROP_L);
 			buttonL.addSelectionListener(croppedListener);
 		}
 
 		final Composite imgControl = new Composite(shell, SWT.NORMAL);
 		{
 			FormData fd = new FormData();
 			final int width = 110;
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd.bottom = new FormAttachment(group1, -2, SWT.TOP);
 				fd.left = new FormAttachment(0, 2);
 				fd.right = new FormAttachment(100, -2);
 				imgControl.setLayout(new FormLayout());
 			} else {
 				fd = new FormData();
 				fd.bottom = new FormAttachment(100, -2);
 				fd.left = new FormAttachment(100, -width - 10);
 				fd.top = new FormAttachment(group1, 0, SWT.TOP);
 				fd.right = new FormAttachment(100, -2);
 				final RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
 				rowLayout.fill = true;
 				rowLayout.justify = true;
 				rowLayout.center = true;
 				rowLayout.pack = false;
 				imgControl.setLayout(rowLayout);
 			}
 			imgControl.setLayoutData(fd);
 			buttonImageNumber = new Button(imgControl, SWT.NORMAL);
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd = new FormData();
 				fd.left = new FormAttachment(0, 0);
 				fd.right = new FormAttachment(20, 0);
 				fd.bottom = new FormAttachment(100, -5);
 				buttonImageNumber.setLayoutData(fd);
 			} else {
 				RowData rd = new RowData();
 				rd.width = width;
 				buttonImageNumber.setLayoutData(rd);
 			}
 			buttonImageNumber.setAlignment(SWT.CENTER);
 			buttonImageNumber.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					// showMessage(0, "Go to image:");
 				}
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 				}
 			});
 			final Button button1 = new Button(imgControl, SWT.PUSH);
 			button1.setText("< Previous");
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd = new FormData();
 				fd.left = new FormAttachment(buttonImageNumber, 0, SWT.RIGHT);
 				fd.right = new FormAttachment(40, -1);
 				button1.setLayoutData(fd);
 			}
 			button1.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(final SelectionEvent arg0) {
 					setFile(curFile - 1, true);
 				}
 
 				@Override
 				public void widgetDefaultSelected(final SelectionEvent arg0) {
 				}
 			});
 			final Button button2 = new Button(imgControl, SWT.PUSH);
 			button2.setText("Next >");
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd = new FormData();
 				fd.left = new FormAttachment(button1, 1, SWT.RIGHT);
 				fd.right = new FormAttachment(60, 0);
 				button2.setLayoutData(fd);
 			}
 			button2.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(final SelectionEvent arg0) {
 					setFile(curFile + 1, true);
 				}
 
 				@Override
 				public void widgetDefaultSelected(final SelectionEvent arg0) {
 				}
 			});
 			final Button button3 = new Button(imgControl, SWT.PUSH);
 			button3.setText("Statistics");
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd = new FormData();
 				fd.left = new FormAttachment(button2, 1, SWT.RIGHT);
 				fd.right = new FormAttachment(80, 0);
 				button3.setLayoutData(fd);
 			}
 			button3.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(final SelectionEvent arg0) {
 					showStatistics();
 				}
 
 				@Override
 				public void widgetDefaultSelected(final SelectionEvent arg0) {
 				}
 			});
 			final Button button4 = new Button(imgControl, SWT.PUSH);
 			button4.setText("Controls");
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd = new FormData();
 				fd.left = new FormAttachment(button3, 1, SWT.RIGHT);
 				fd.right = new FormAttachment(100, 0);
 				button4.setLayoutData(fd);
 			}
 			button4.addSelectionListener(new SelectionListener() {
 				@Override
 				public void widgetSelected(final SelectionEvent arg0) {
 					showControls();
 				}
 
 				@Override
 				public void widgetDefaultSelected(final SelectionEvent arg0) {
 				}
 			});
 		}
 
 		final Group group2 = new Group(shell, SWT.NORMAL);
 		{
 			group2.setText("Features");
 			final FormData fd = new FormData();
 			fd.bottom = new FormAttachment(100, -2);
 			fd.left = new FormAttachment(group1, 4, SWT.RIGHT);
 			if (IMAGE_CONTROL_VERTICAL) {
 				fd.right = new FormAttachment(100, -2);
 			} else {
 				fd.right = new FormAttachment(imgControl, -2, SWT.LEFT);
 			}
 			fd1.top = new FormAttachment(group2, 0, SWT.TOP);
 			group2.setLayoutData(fd);
 			final GridLayout gridLayout = new GridLayout(2, false);
 			gridLayout.horizontalSpacing = 3;
 			group2.setLayout(gridLayout);
 			final MouseListener labelClick = new MouseListener() {
 				@Override
 				public void mouseUp(final MouseEvent e) {
 				}
 
 				@Override
 				public void mouseDown(final MouseEvent e) {
 					if (e.data != null && e.data instanceof Integer) {
 						final int i = (Integer) e.data;
 						final Object data = label2[i].getData("field");
 						if (data != null && data instanceof String) {
 							final String key = (String) data;
 							curData.manual.remove(key);
 						} else {
 							throw new RuntimeException(
 									"Field is not a string: " + data);
 						}
 					} else {
 						throw new RuntimeException("Source not specified");
 					}
 					updateCoords(true);
 				}
 
 				@Override
 				public void mouseDoubleClick(final MouseEvent e) {
 				}
 			};
 			label1 = new Label[FIELDS_COORD.length];
 			label2 = new ProgressLabel[FIELDS_COORD.length];
 			int i = 0;
 			for (final Field f : FIELDS_COORD) {
 				label1[i] = new Label(group2, SWT.NORMAL);
 				label1[i].setText(f.name());
 				label2[i] = new ProgressLabel(group2, true);
 				label2[i].setData("field", f.field());
 				label2[i].addMouseListener(labelClick, i);
 				i++;
 			}
 			final GridData data = new GridData();
 			data.horizontalSpan = 2;
 			label3 = new ProgressLabel(group2, false);
 			label3.setLayoutData(data);
 		}
 
 		imgBox = new Composite(shell, SWT.BORDER | SWT.DOUBLE_BUFFERED);
 		{
 			final FormData fd = new FormData();
 			fd.top = new FormAttachment(0, 2);
 			fd.bottom = new FormAttachment(imgControl, -2, SWT.TOP);
 			fd.left = new FormAttachment(0, 2);
 			fd.right = new FormAttachment(100, -2);
 			imgBox.setLayoutData(fd);
 			imgBox.addMouseListener(new MouseListener() {
 				@Override
 				public void mouseUp(final MouseEvent e) {
 				}
 
 				@Override
 				public void mouseDown(final MouseEvent e) {
 					imgBox.setFocus();
 					switch (e.button) {
 					case 1:
 						setCoord((int) (e.x / scale), (int) (e.y / scale));
 						break;
 					case 3:
 						setCoord(null);
 						break;
 					}
 				}
 
 				@Override
 				public void mouseDoubleClick(final MouseEvent e) {
 				}
 			});
 			imgBox.addPaintListener(new PaintListener() {
 				@Override
 				public void paintControl(final PaintEvent e) {
 					if (curImg != null) {
 						e.gc.setAntialias(SWT.ON);
 						e.gc.setBackground(color1);
 						// Compute areas
 						final int srcWidth = curImg.getBounds().width;
 						final int srcHeight = curImg.getBounds().height;
 						final double srcRatio = (double) srcWidth
 								/ (double) srcHeight;
 						int destWidth = imgBox.getSize().x;
 						int destHeight = imgBox.getSize().y;
 						final double destRatio = (double) destWidth
 								/ (double) destHeight;
 						if (destRatio < srcRatio) {
 							destHeight = (int) (destWidth / srcRatio);
 						} else {
 							destWidth = (int) (destHeight * srcRatio);
 						}
 						// Draw frame
 						e.gc.drawImage(curImg, 0, 0, srcWidth, srcHeight, 0, 0,
 								destWidth, destHeight);
 						// Calculate scale
 						scale = (((float) destWidth / (float) srcWidth) +
 								((float) destHeight / (float) srcHeight)) / 2;
 						Transform tr = new Transform(e.display);
 						tr.scale(scale, scale);
 						e.gc.setTransform(tr);
 						// Gather data
 						final int radius = (int) (CIRCLE_SIZE / 2 * scale);
 						final int size = (int) (CIRCLE_SIZE * scale);
 						final Point p[] = new Point[FIELDS_COORD.length];
 						final Point l[][] = new Point[LINES.length][2];
 						int i = 0;
 						for (final Field field : FIELDS_COORD) {
 							final String value = curData.manual.get(field
 									.field());
 							if (value != null) {
 								p[i] = toPoint(value);
 							}
 							int j = 0;
 							for (final Line line : LINES) {
 								if (line.from().equals(field)) {
 									l[j][0] = p[i];
 								}
 								if (line.to().equals(field)) {
 									l[j][1] = p[i];
 								}
 								j++;
 							}
 							i++;
 						}
 						// Outline auto-annotated face, if applicable
 						Face faceA = makeFace(curData.automatic, false);
 						if (faceA.box != null && faceA.width > 0
 								&& faceA.height > 0) {
 							int x = faceA.box.x + faceA.box.width / 2;
 							int y = faceA.box.y + faceA.box.height / 2;
 							int half = (int) faceA.width / 2;
 							e.gc.drawRectangle(x - half, y - half,
 									(int) faceA.width, (int) faceA.width);
 						}
 						// Outline face, if applicable
 						Face faceM = makeFace(curData.manual, false);
 						if (faceM.rotation != null && faceM.box != null
 								&& faceM.width > 0 && faceM.height > 0) {
 							float degrees = new Float(faceM.rotation);
 							int x = faceM.box.x + faceM.box.width / 2;
 							int y = faceM.box.y + faceM.box.height / 2;
 							int half = (int) faceM.width / 2;
 							e.gc.setLineWidth(2);
 							e.gc.setForeground(color4);
 							e.gc.drawRectangle(x - half, y - half,
 									(int) faceM.width, (int) faceM.width);
 							tr.translate(x, y);
 							tr.rotate(degrees);
 							e.gc.setTransform(tr);
 							e.gc.setLineWidth(3);
 							e.gc.setAlpha(128);
 							e.gc.setForeground(color4);
 							e.gc.drawOval((int) -faceM.width / 2,
 									(int) -faceM.height / 2, (int) faceM.width,
 									(int) faceM.height);
 							e.gc.setLineWidth(2);
 							e.gc.setAlpha(160);
 							e.gc.setForeground(color3);
 							e.gc.drawOval((int) -faceM.width / 2,
 									(int) -faceM.height / 2, (int) faceM.width,
 									(int) faceM.height);
 						}
 						System.out.println("error: "
 								+ faceM.computeError(faceA, frameSize));
 						// Draw lines
 						e.gc.setAlpha(255);
 						tr = new Transform(e.display);
 						tr.scale(scale, scale);
 						e.gc.setTransform(tr);
 						e.gc.setLineWidth(1);
 						for (int j = 0; j < LINES.length; j++) {
 							if (l[j] != null && l[j][0] != null
 										&& l[j][1] != null) {
 								e.gc.drawLine(l[j][0].x, l[j][0].y,
 											l[j][1].x, l[j][1].y);
 							}
 						}
 						// Draw coordinate fields
 						e.gc.setFont(font);
 						e.gc.setForeground(color2);
 						i = 0;
 						for (final Field field : FIELDS_COORD) {
 							final Style style = field.style();
 							if (p[i] != null && style != null) {
 								style.draw(e.gc, p[i], radius, size);
 							}
 							i++;
 						}
 					}
 				}
 			});
 		}
 
 		setFile(0, false);
 
 		shell.addShellListener(new ShellListener() {
 
 			@Override
 			public void shellIconified(final ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellDeiconified(final ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellDeactivated(final ShellEvent arg0) {
 			}
 
 			@Override
 			public void shellClosed(final ShellEvent arg0) {
 				save();
 			}
 
 			@Override
 			public void shellActivated(final ShellEvent arg0) {
 			}
 		});
 
 		shell.setLayout(new FormLayout());
 		shell.setText(FaceSelector.class.getSimpleName());
 		final int width = IMAGE_CONTROL_VERTICAL ? 500 : 580;
 		final int height = 600;
 		shell.setSize(width, height);
 		shell.layout();
 		shell.setMinimumSize(width, height - imgBox.getSize().y
 				+ EXPECTED_IMAGE_SIZE.y);
 
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	protected static void drawPoint(final int type, final GC gc,
 			final Point p, final int radius, final int size) {
 		gc.setAlpha(128);
 		switch (type) {
 		default:
 		case 1:
 			gc.fillOval(p.x - radius - 1, p.y - radius - 1, size + 3, size + 3);
 			break;
 		case 2:
 			gc.fillRectangle(p.x - radius - 1, p.y - radius - 1, size + 3,
 					size + 3);
 			break;
 		}
 		gc.setAlpha(255);
 		switch (type) {
 		default:
 		case 1:
 			gc.drawOval(p.x - radius, p.y - radius, size, size);
 			break;
 		case 2:
 			gc.drawRectangle(p.x - radius, p.y - radius, size, size);
 			break;
 		}
 	}
 
 	private static Point toPoint(final String value) {
 		return toPoint(value, 1.0);
 	}
 
 	private static Point toPoint(final String string, final double scale) {
 		if (string == null) {
 			return null;
 		}
 		final int pos = string.indexOf(',');
 		final Point p = new Point(0, 0);
 		if (pos > 0) {
 			p.x = (int) (Double.parseDouble(string.substring(0, pos)) * scale);
 			p.y = (int) (Double.parseDouble(string.substring(pos + 1)) * scale);
 		}
 		return p;
 	}
 
 	private static Point2D toPoint2D(final String value) {
 		return toPoint2D(value, 1.0);
 	}
 
 	private static Point2D toPoint2D(final String string, final double scale) {
 		if (string == null) {
 			return null;
 		}
 		final int pos = string.indexOf(',');
 		final Point2D p = new Point2D(0, 0);
 		if (pos > 0) {
 			p.x = Double.parseDouble(string.substring(0, pos)) * scale;
 			p.y = Double.parseDouble(string.substring(pos + 1)) * scale;
 		}
 		return p;
 	}
 
 	private static Rectangle toRectangle(final String value) {
 		return toRectangle(value, 1.0);
 	}
 
 	private static Rectangle toRectangle(final String string, final double scale) {
 		if (string == null) {
 			return null;
 		}
 		int pos = 0;
 		int pos2 = string.indexOf(',', pos + 1);
 		final Rectangle p = new Rectangle(0, 0, 0, 0);
 		if (pos2 > 0) {
 			p.x = (int) (Double.parseDouble(string.substring(pos, pos2)) * scale);
 			pos = pos2 + 1;
 			pos2 = string.indexOf(',', pos);
 			p.y = (int) (Double.parseDouble(string.substring(pos, pos2)) * scale);
 			pos = pos2 + 1;
 			pos2 = string.indexOf(',', pos);
 			p.width = (int) (Double.parseDouble(string.substring(pos, pos2)) * scale);
 			pos = pos2 + 1;
 			p.height = (int) (Double.parseDouble(string.substring(pos)) * scale);
 		}
 		return p;
 	}
 
 	protected static void showStatistics() {
 		final int cropped[] = new int[FIELDS_COORD.length];
 		final int rotation[] = new int[181];
 		final int stats[] = new int[statistics.length];
 		double error = 0.0;
 		int count = 0;
 		int i = 0;
 		for (final File file : files) {
 			AnnotationData fileData;
 			try {
 				fileData = loadData(file.getCanonicalPath());
 			} catch (IOException e) {
 				e.printStackTrace();
 				continue;
 			}
 			if (!isAnnotated()) {
 				continue;
 			}
 			count++;
 			Face faceM = makeFace(fileData.manual, false);
 			Face faceA = makeFace(fileData.automatic, false);
 			error += faceM.computeError(faceA, frameSize);
 			int j = 0;
 			for (final Field field : FIELDS_COORD) {
 				if (fileData.manual.containsKey(field.field())) {
 					final String key = fileData.manual.get(field.field());
 					if (key == null) {
 						cropped[j]++;
 					}
 				}
 				j++;
 			}
 			final Face face = makeFace(fileData.manual, true);
 			final Double angle = face.rotation;
 			int k = 0;
 			for (final Statistic statistic : statistics) {
 				boolean valid = false;
 				if (statistic.all()) {
 					valid = true;
 				}
 				j = 0;
 				for (final Field field : statistic.fields()) {
 					final String key = fileData.manual.get(field.field());
 					if (statistic.all() && key == null) {
 						valid = false;
 					} else if (!statistic.all() && key != null) {
 						valid = true;
 					}
 					j++;
 				}
 				if (statistic.maxAngle() != null) {
 					if (angle == null || angle > statistic.maxAngle()) {
 						valid = false;
 					}
 				}
 				if (valid) {
 					stats[k]++;
 				}
 				k++;
 			}
 			if (angle != null) {
 				final int rot = (int) Math.round(angle);
 				rotation[rot]++;
 			}
 			i++;
 		}
 		String msg = "Annotated: " + count + " of " + files.size();
 		msg += "\nAutomatic annotation error: " + (error / count);
 		int j = 0;
 		for (final Field field : FIELDS_COORD) {
 			msg += "\nCropped " + field.field() + ": " + cropped[j] + " of "
 					+ count;
 			j++;
 		}
 		int sum = 0;
 		for (i = rotation.length - 1; i >= 0; i--) {
 			sum += rotation[i];
 			if (rotation[i] > 0) {
 				msg += "\nRotation ≥ " + i + "°: " + sum;
 			}
 		}
 		msg += "\nRotation N/A: " + (files.size() - sum);
 		i = 0;
 		for (final Statistic statistic : statistics) {
 			msg += "\n" + statistic.name() + ": " + stats[i];
 			i++;
 		}
 		System.out.println(msg);
 		showMessage(SWT.ICON_INFORMATION, msg);
 	}
 
 	protected static void showControls() {
 		showMessage(SWT.ICON_INFORMATION,
 				"PgUp, Up, Left\tPrevious image\n" +
 						"PgDn, Down, Right\tNext image\n" +
 						"Shift + Left\tMove points 1px left\n" +
 						"Shift + Up\tMove points 1px up\n" +
 						"Shift + Right\tMove points 1px right\n" +
 						"Shift + Down\tMove points 1px down\n" +
 						"[\tRotate left\n" +
 						"]\tRotate right\n" +
 						"+\tScale up\n" +
 						"-\tScale down");
 	}
 
 	private static Face makeFace(HashMap<String, String> data,
 			final boolean absolute) {
 		final Face face = new Face();
 		face.box = getBoundingBox(data);
 		if (face.box == null) {
 			return face;
 		}
 		face.width = face.box.width;
 		face.height = face.box.height;
 		final Field eyeL = FIELD_EYE_L;
 		final Field eyeR = FIELD_EYE_R;
 		final Point2D p1 = toPoint2D(data.get(eyeL.field()));
 		final Point2D p2 = toPoint2D(data.get(eyeR.field()));
 		if (p1 != null && p2 != null) {
 			face.width = distance(p1, p2) * 2;
 			final Point2D pA = new Point2D(1.0, 0.0);
 			final Point2D pB = norm(new Point2D(p2.x - p1.x, p2.y - p1.y));
 			final double radians = Math.acos(dot(pA, pB));
 			face.rotation = radians * RADIANS_TO_DEGREES;
 			if (absolute) {
 				face.rotation = Math.abs(face.rotation);
 			}
 		}
 		final Field headT = FIELD_HEAD_T;
 		final Field headB = FIELD_HEAD_B;
 		final Point2D p3 = toPoint2D(data.get(headT.field()));
 		final Point2D p4 = toPoint2D(data.get(headB.field()));
 		if (p3 != null && p4 != null) {
 			face.height = distance(p3, p4);
 			if (face.width == 0) {
 				face.width = face.height / GOLDEN_RATIO;
 			}
 			final Point2D pA = new Point2D(0.0, 1.0);
 			final Point2D pB = norm(new Point2D(p3.x - p4.x, p3.y - p4.y));
 			final double radians = Math.acos(dot(pA, pB));
 			face.rotation = radians * RADIANS_TO_DEGREES;
 			if (absolute) {
 				face.rotation = Math.abs(face.rotation);
 			}
 		}
 		return face;
 	}
 
 	private static double dot(final Point2D p1, final Point2D p2) {
 		return p1.x * p2.x + p1.y * p2.y;
 	}
 
 	private static Point2D norm(final Point2D point) {
 		if (point == null) {
 			return null;
 		}
 		final double len = Math.sqrt(Math.pow(point.x, 2)
 				+ Math.pow(point.y, 2));
 		return new Point2D(point.x / len, point.y / len);
 	}
 
 	private static void setButton(final Button button, final Field field) {
 		button.setText(field.name());
 		button.setData("field", field.field());
 	}
 
 	private static void fatal(final Exception e) {
 		fatal(e.getMessage());
 	}
 
 	private static void fatal(final String message) {
 		final RuntimeException e = new RuntimeException(message);
 		e.printStackTrace();
 		showMessage(SWT.ICON_ERROR, message);
 		System.exit(1);
 	}
 
 	private static int showMessage(final int style, final String message) {
 		return showMessage(style, null, message);
 	}
 
 	private static int showMessage(final int style, String title,
 			final String message) {
 		final MessageBox mg = new MessageBox(shell, style);
 		if (title == null) {
 			title = FaceSelector.class.getSimpleName();
 		}
 		mg.setText(title);
 		mg.setMessage(message);
 		return mg.open();
 	}
 
 	protected static void setCoord(final int x, final int y) {
 		final String value = x + "," + y;
 		if (currentLabel >= 0) {
 			setCoord(value);
 		} else {
 			final Point2D point = new Point2D(x, y);
 			Point2D nearestPoint = null;
 			double nearest = -1;
 			int nearestIndex = 0;
 			int i = 0;
 			for (final Field field : FIELDS_COORD) {
 				final String value2 = curData.manual.get(field.field());
 				if (value2 != null) {
 					final Point2D point2 = toPoint2D(value2);
 					final double distance = distance(point, point2);
 					if (distance < 20 && (nearest < 0 || distance < nearest)) {
 						nearestPoint = point2;
 						nearest = distance;
 						nearestIndex = i;
 					}
 				}
 				i++;
 			}
 			if (nearestPoint != null) {
 				currentLabel = nearestIndex;
 				setCoord(value);
 			}
 		}
 	}
 
 	private static double distance(final Point a, final Point b) {
 		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
 	}
 
 	private static double distance(final Point2D a, final Point2D b) {
 		return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
 	}
 
 	private static void setCoord(final String value) {
 		if (currentLabel >= 0) {
 			final Object data = label2[currentLabel].getData("field");
 			if (data != null && data instanceof String) {
 				final String key = (String) data;
 				curData.manual.put(key, value);
 			} else {
 				throw new RuntimeException("Field is not a string: " + data);
 			}
 		}
 		updateCoords(true);
 	}
 
 	private static void setData(final Button button) {
 		final Object data = button.getData("field");
 		if (data != null && data instanceof String) {
 			final String key = (String) data;
 			String value = "false";
 			if (button.getSelection()) {
 				value = "true";
 			}
 			curData.manual.put(key, value);
 		} else {
 			throw new RuntimeException("Button does not contain toggle data");
 		}
 	}
 
 	private static void setOffset(final int x, final int y) {
 		for (final Field f : FIELDS_COORD) {
 			final String field = f.field();
 			String value = curData.manual.get(field);
 			if (value != null) {
 				final Point2D point = toPoint2D(value);
 				point.x += x;
 				point.y += y;
 				value = point.x + "," + point.y;
 				curData.manual.put(field, value);
 			}
 		}
 		imgBox.redraw();
 	}
 
 	private static void rotate(final double radians) {
 		Matrix matrix = Matrix.rotation(radians);
 		matrix(matrix);
 	}
 
 	private static void scale(final double scale) {
 		Matrix matrix = Matrix.scale(scale);
 		matrix(matrix);
 	}
 
 	public static void matrix(Matrix matrix) {
 		int x = 0, y = 0;
 		if (TRANSLATE_TO_ORIGIN) {
 			Rectangle bounds = getBoundingBox(curData.manual);
 			x = bounds.x + bounds.width / 2;
 			y = bounds.y + bounds.height / 2;
 		}
 		if (DEBUG) {
 			System.out.println("Matrix transformation with " + matrix);
 		}
 		for (final Field f : FIELDS_COORD) {
 			final String field = f.field();
 			String value = curData.manual.get(field);
 			if (value != null) {
 				final Point2D point2d = toPoint2D(value);
 				point2d.x -= x;
 				point2d.y -= y;
 				Point2D point = Matrix.cross(point2d, matrix);
 				point.x += x;
 				point.y += y;
 				if (DEBUG) {
 					System.out.println(field + " transformed from " + value
 							+ " to " + point.x
 							+ "," + point.y);
 				}
 				value = point.x + "," + point.y;
 				curData.manual.put(field, value);
 			}
 		}
 		imgBox.redraw();
 	}
 
 	private static Rectangle getBoundingBox(HashMap<String, String> data) {
 		boolean first = true;
 		// FIXME replace with Recangle2D
 		Rectangle bounds = new Rectangle(0, 0, 0, 0);
 		if (data.containsKey("head")) {
 			String value = data.get("head");
 			return toRectangle(value);
 		}
 		for (final Field f : FIELDS_COORD) {
 			final String field = f.field();
 			String value = data.get(field);
 			if (value != null) {
 				Point2D point = toPoint2D(value);
 				if (point.x < bounds.x || first) {
 					bounds.x = (int) point.x;
 				}
 				if (point.y < bounds.y || first) {
 					bounds.y = (int) point.y;
 				}
 				if (point.x > bounds.width || first) {
 					bounds.width = (int) point.x;
 				}
 				if (point.y > bounds.height || first) {
 					bounds.height = (int) point.y;
 				}
 				first = false;
 			}
 		}
 		if (first) {
 			return null;
 		}
 		bounds.width -= bounds.x;
 		bounds.height -= bounds.y;
 		return bounds;
 	}
 
 	private static String getData(final Field field) {
 		final String key = field.field();
 		if (curData.manual.containsKey(key)) {
 			return curData.manual.get(key);
 		}
 		return null;
 	}
 
 	private static void setFile(final int i, final boolean save) {
 		if (save) {
 			save();
 		}
 		curFile = i;
 		if (curFile > files.size() - 1) {
 			curFile = 0;
 		} else if (curFile < 0) {
 			curFile = files.size() - 1;
 		}
 		buttonImageNumber.setText("Image " + (curFile + 1) + " of "
 				+ files.size());
 		load();
 	}
 
 	private static AnnotationData getData(int i) throws Exception {
 		if (i > files.size() - 1) {
 			throw new IndexOutOfBoundsException(
 					"This is beyond the last file in the sequence.");
 		} else if (i < 0) {
 			throw new IndexOutOfBoundsException(
 					"This is the first file in the sequence.");
 		}
 		final String path = files.get(i).getCanonicalPath();
 		return loadData(path);
 	}
 
 	private static void load() {
 		try {
 			final String path = files.get(curFile).getCanonicalPath();
 			curImg = new Image(display, path);
 			if (curImg != null && frameSize == 0) {
 				Rectangle bounds = curImg.getBounds();
				frameSize = Math.sqrt(Math.pow(bounds.width, 2)
						+ Math.pow(bounds.height, 2));
 			}
 			curData = loadData(path);
 			for (final Field field : FIELDS_TOGGLE) {
 				final String value = curData.manual.get(field.field());
 				boolean selection = false;
 				if (value != null && value.equals("true")) {
 					selection = true;
 				}
 				Button button = null;
 				if (field.equals(FIELD_CROP_T)) {
 					button = buttonT;
 				} else if (field.equals(FIELD_CROP_R)) {
 					button = buttonR;
 				} else if (field.equals(FIELD_CROP_B)) {
 					button = buttonB;
 				} else if (field.equals(FIELD_CROP_L)) {
 					button = buttonL;
 				}
 				if (button != null) {
 					button.setSelection(selection);
 				}
 			}
 			imgBox.redraw();
 		} catch (final IOException e) {
 			throw new RuntimeException(e);
 		}
 		updateCoords(false);
 	}
 
 	private static AnnotationData loadData(String path) {
 		AnnotationData data = new AnnotationData();
 		File file = new File(path + ".txt");
 		data.manual = loadData2(file);
 		file = new File(path + ".AUTO.txt");
 		data.automatic = loadData2(file);
 		return data;
 	}
 
 	protected static HashMap<String, String> loadData2(File file) {
 		HashMap<String, String> response = new HashMap<String, String>();
 		if (file.exists()) {
 			try {
 				final FileReader input = new FileReader(file);
 				final BufferedReader bufRead = new BufferedReader(input);
 				String line;
 				final int count = 0;
 				int lineNo = 0;
 				while ((line = bufRead.readLine()) != null) {
 					lineNo++;
 					if (line.startsWith("#")) {
 						continue;
 					}
 					final int pos = line.indexOf('=');
 					if (pos > 0) {
 						final String key = line.substring(0, pos);
 						String value = line.substring(pos + 1);
 						if (value.equals("null")) {
 							value = null;
 						}
 						response.put(key, value);
 					} else {
 						System.err.println("Parse error on line " + count
 								+ " of " + file);
 					}
 				}
 
 				bufRead.close();
 			} catch (final IOException e) {
 				e.printStackTrace();
 			}
 			if (DEBUG) {
 				System.out.println("Read " + response.size()
 						+ " fields for image #" + (curFile + 1));
 			}
 		}
 		return response;
 	}
 
 	private static void updateCoords(final boolean fromUI) {
 		currentLabel = -1;
 		int count = 0;
 		for (final ProgressLabel label : label2) {
 			final Object data = label.getData("field");
 			if (data != null && data instanceof String) {
 				final String field = (String) data;
 				if (curData.manual.containsKey(field)) {
 					String value = curData.manual.get(field);
 					boolean isNull = false;
 					if (value == null) {
 						value = "N/A";
 						isNull = true;
 						label2[count].setMode(ProgressLabel.MODE_SKIP);
 					} else {
 						label2[count].setMode(ProgressLabel.MODE_COMPLETE);
 					}
 					if (fromUI) {
 						Button button = null;
 						if (field.equals(FIELD_EYE_L.field())) {
 							if (getData(FIELD_HEAD_T) != null
 									&& getData(FIELD_HEAD_B) != null) {
 								button = buttonL;
 							}
 						} else if (field.equals(FIELD_EYE_R.field())) {
 							if (getData(FIELD_HEAD_T) != null
 									&& getData(FIELD_HEAD_B) != null) {
 								button = buttonR;
 							}
 						} else if (field.equals(FIELD_HEAD_T.field())) {
 							button = buttonT;
 						} else if (field.equals(FIELD_HEAD_B.field())) {
 							button = buttonB;
 						}
 						if (button != null) {
 							button.setSelection(isNull);
 							setData(button);
 						}
 					}
 					label2[count].setText("(" + value + ")");
 				} else {
 					if (currentLabel < 0) {
 						label2[count]
 								.setText("Left-click: set coordinates; right-click: N/A");
 						label.setMode(ProgressLabel.MODE_BUSY);
 						currentLabel = count;
 					} else {
 						label2[count].setText("Unset");
 						label2[count].setMode(ProgressLabel.MODE_TODO);
 					}
 				}
 			}
 			count++;
 		}
 		if (currentLabel < 0) {
 			label3.setText("Click the image to update the nearest coordinate");
 			label3.setMode(ProgressLabel.MODE_BUSY);
 		} else {
 			label3.setText("Click the image to set the highlighted coordinate");
 			label3.setMode(ProgressLabel.MODE_TODO);
 		}
 		imgBox.redraw();
 	}
 
 	private static void save() {
 		if (curFile >= 0) {
 			File file;
 			if (curData.manual.size() > 0) {
 				try {
 					file = new File(files.get(curFile).getCanonicalPath()
 							+ ".txt");
 					file.createNewFile();
 					if (file.exists()) {
 						final BufferedWriter bufferedWriter = new BufferedWriter(
 								new FileWriter(file));
 						int count = 0;
 						for (final Entry<String, String> set : curData.manual
 								.entrySet()) {
 							bufferedWriter.write(set.getKey() + "="
 									+ set.getValue() + "\n");
 							count++;
 						}
 						bufferedWriter.close();
 						if (DEBUG) {
 							System.out.println("Stored " + count
 									+ " fields for image #" + (curFile + 1));
 						}
 					} else {
 						throw new RuntimeException("Failed to save data:\n\t"
 								+ file);
 					}
 				} catch (final IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 	}
 
 	private static void listFiles(final File directory) {
 		if (!directory.exists()) {
 			String path;
 			try {
 				path = directory.getCanonicalPath();
 			} catch (final IOException e) {
 				path = directory.getAbsolutePath();
 			}
 			fatal("Path does not exist:\n\n    " + path);
 		}
 		final File[] files = directory.listFiles();
 		for (final File file : files) {
 			if (file.isDirectory()) {
 				listFiles(file);
 			} else if (file.isFile()) {
 				boolean ok = false;
 				for (final String ext : EXTENSIONS) {
 					if (file.getName().endsWith(ext)) {
 						ok = true;
 						break;
 					}
 				}
 				if (!ok) {
 					continue;
 				}
 				if (onlyIncomplete) {
 					try {
 						curData = loadData(file.getCanonicalPath());
 					} catch (final IOException e) {
 						throw new RuntimeException(e);
 					}
 					if (isAnnotated()) {
 						continue;
 					}
 				}
 				FaceSelector.files.add(file);
 			}
 		}
 	}
 
 	private static boolean isAnnotated() {
 		boolean complete = true;
 		for (final Field f : FIELDS_COORD) {
 			final String field = f.field();
 			if (!curData.manual.containsKey(field)) {
 				complete = false;
 				break;
 			}
 		}
 		return complete;
 	}
 }
