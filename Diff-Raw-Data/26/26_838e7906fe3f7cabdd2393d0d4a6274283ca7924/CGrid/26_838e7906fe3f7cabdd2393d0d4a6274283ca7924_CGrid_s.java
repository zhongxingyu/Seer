 package org.percepta.mgrankvi.floorplanner.gwt.client.floorgrid;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.percepta.mgrankvi.floorplanner.gwt.client.CommandObject;
 import org.percepta.mgrankvi.floorplanner.gwt.client.VisualItem;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.GeometryUtil;
 import org.percepta.mgrankvi.floorplanner.gwt.client.geometry.Point;
 import org.percepta.mgrankvi.floorplanner.gwt.client.paint.GridButton;
 import org.percepta.mgrankvi.floorplanner.gwt.client.paint.GridUtils;
 import org.percepta.mgrankvi.floorplanner.gwt.client.room.CRoom;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ContextMenuEvent;
 import com.google.gwt.event.dom.client.ContextMenuHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.MenuBar;
 import com.google.gwt.user.client.ui.MenuItem;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.vaadin.client.ui.VNotification;
 
 public class CGrid extends Composite implements ClickHandler, MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseOutHandler, ContextMenuHandler,
 		ChangeHandler, KeyUpHandler {
 
 	private static final String CLASSNAME = "c-floorgrid";
 	private static final int GRID_SIZE = 50;
 
 	protected final Canvas canvas;
 
 	private final TextBox typeAndEdit = new TextBox();
 
 	protected PopupPanel contextMenu;
 
 	private int gridSize = 50;
 	private int offsetX = 0;
 	private int offsetY = 0;
 	private final Point origo = new Point(0, 0);
 
 	private int orgX, orgY;
 	private Point orgOrigo = null;
 
 	final int BUTTON_SIZE = 25;
 	final String DEFAULT_COLOR = "LAVENDER";
 	final String DEFAULT_SELECTED_COLOR = "SILVER";
 
 	final GridButton up;
 	final GridButton down;
 	final GridButton plus;
 	final GridButton minus;
 
 	private final List<GridButton> buttons;
 
 	protected boolean animating = false;
 	private boolean mouseDown = false;
 	private boolean mouseMoved = true;
 	private boolean hasFloorAbove = false;
 	private boolean hasFloorBelow = false;
 
 	private int downX = 0;
 	private int downY = 0;
 	int zoom = 0;
 
 	boolean isEditable = false;
 	private final FlowPanel content;
 
 	CFloor selectedFloor;
 	LinkedList<CFloor> floors = new LinkedList<CFloor>();
 	List<VisualItem> items = new LinkedList<VisualItem>();
 
 	public CGrid() {
 		content = new FlowPanel();
 		content.setSize("100%", "100%");
 
 		initWidget(content);
 
 		setStyleName(CLASSNAME);
 
 		addDomHandler(this, MouseDownEvent.getType());
 		addDomHandler(this, MouseMoveEvent.getType());
 		addDomHandler(this, MouseUpEvent.getType());
 		addDomHandler(this, ClickEvent.getType());
 		addDomHandler(this, ContextMenuEvent.getType());
 		addDomHandler(this, ChangeEvent.getType());
 		addDomHandler(this, MouseOutEvent.getType());
 		addDomHandler(this, KeyUpEvent.getType());
 
 		canvas = Canvas.createIfSupported();
 		if (canvas != null) {
 			content.add(canvas);
 			clearCanvas();
 			// paint();
 		} else {
 			getElement().setInnerHTML("Canvas not supported");
 		}
 
 		content.add(typeAndEdit);
 
 		final Style editStyle = typeAndEdit.getElement().getStyle();
 		typeAndEdit.addChangeHandler(this);
 		typeAndEdit.addKeyUpHandler(this);
 		editStyle.setPosition(Position.RELATIVE);
 		editStyle.setLeft(0.0, Style.Unit.PX);
 		editStyle.setProperty("margin", "0");
 		typeAndEdit.setWidth(Window.getClientWidth() + "px");
 
 		plus = new GridButton(new Point(Window.getClientWidth() - 50, 25), BUTTON_SIZE, DEFAULT_COLOR);
 		minus = new GridButton(new Point(Window.getClientWidth() - 50, 51), BUTTON_SIZE, DEFAULT_COLOR);
 		up = new GridButton(new Point(Window.getClientWidth() - 50, 78), BUTTON_SIZE, DEFAULT_COLOR);
 		down = new GridButton(new Point(Window.getClientWidth() - 50, 105), BUTTON_SIZE, DEFAULT_COLOR);
 		buttons = Arrays.asList(up, down, plus, minus);
 	}
 
 	private void clearCanvas() {
 		canvas.setCoordinateSpaceWidth(Window.getClientWidth());
 		canvas.setCoordinateSpaceHeight(Window.getClientHeight() - typeAndEdit.getOffsetHeight() - 4);
 	}
 
 	SearchBar searchBar = new SearchBar(this);
 	ButtonBar buttonBar = new ButtonBar(this);
 
 	public void setFloor(final CFloor floor) {
 		selectedFloor = floor;
 		floor.grid = this;
 		hasFloorAbove = hasFloorAbove();
 		hasFloorBelow = hasFloorBelow();
 	}
 
 	private void paint() {
 		final Context2d context = canvas.getContext2d();
 
 		GridUtils.paintGrid(context, new Point(offsetX, offsetY), gridSize, origo);
 		GridUtils.paintZoomInButton(context, plus.getPosition(), plus.getSize(), plus.getColor());
 		GridUtils.paintZoomOutButton(context, minus.getPosition(), minus.getSize(), minus.getColor());
 
 		if (floors.size() > 1) {
 			if (hasFloorAbove) {
 				GridUtils.paintFloorUpButton(context, up.getPosition(), up.getSize(), up.getColor());
 			}
 			if (hasFloorBelow) {
 				GridUtils.paintFloorDownButton(context, down.getPosition(), down.getSize(), down.getColor());
 			}
 		}
 
 		if (selectedFloor != null) {
 			selectedFloor.paint();
 		}
 
 		for (final VisualItem item : items) {
 			item.paint(context);
 		}
 
 		searchBar.paint(context);
 		buttonBar.paint(context);
 	}
 
 	private boolean hasFloorAbove() {
 		final int selectedFloorIndex = floors.indexOf(selectedFloor);
		return selectedFloorIndex <= floors.size() + 1;
 	}
 
 	private boolean hasFloorBelow() {
 		return !selectedFloor.equals(floors.getFirst());
 	}
 
 	protected void repaint() {
 		clearCanvas();
 		paint();
 	}
 
 	@Override
 	public void onClick(final ClickEvent event) {
 		if (animating) {
 			return;
 		}
 		if (typeAndEdit.getElement().equals(event.getNativeEvent().getEventTarget())) {
 			return;
 		}
 		if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
 		} else {
 			if (mouseMoved) {
 				mouseMoved = false;
 				return;
 			}
 			final int clientX = event.getClientX();
 			final int clientY = event.getClientY();
 			if (clientX > Window.getClientWidth() - 50 && clientX < Window.getClientWidth() - 25) {
 				if (clientY > plus.getPosition().getY() && clientY < plus.getPosition().getY() + BUTTON_SIZE) {
 					// sx = sy = 2
 					// x2 = sx*x1
 					// y2 = sy*y1
 					zoom++;
 					if (zoom == 0) {
 						reset();
 					} else {
 						scale(2);
 					}
 					repaint();
 					return;
 				} else if (clientY > minus.getPosition().getY() && clientY < minus.getPosition().getY() + BUTTON_SIZE) {
 					// sx = sy = 0.5
 					// x2 = sx*x1
 					// y2 = sy*y1
 					zoom--;
 					if (zoom == 0) {
 						reset();
 					} else {
 						scale(0.5);
 					}
 					repaint();
 					return;
 				} else if (hasFloorAbove && clientY > up.getPosition().getY() && clientY < up.getPosition().getY() + BUTTON_SIZE) {
 					final int selectedFloorIndex = floors.indexOf(selectedFloor);
 					setFloor(floors.get(selectedFloorIndex + 1));
 					repaint();
 					return;
 				} else if (hasFloorBelow && clientY > down.getPosition().getY() && clientY < down.getPosition().getY() + BUTTON_SIZE) {
 					final int selectedFloorIndex = floors.indexOf(selectedFloor);
 					setFloor(floors.get(selectedFloorIndex - 1));
 					repaint();
 					return;
 				}
 			}
 
 			if (selectedFloor != null) {
 				selectedFloor.click(clientX, clientY);
 				if (isEditable) {
 					selectedFloor.clickEditable(clientX, clientY);
 				} else {
 					selectedFloor.clickForRoomSelect(downX, downY);
 				}
 			}
 			buttonBar.click(clientX, clientY);
 			repaint();
 		}
 	}
 
 	private void scale(final double scale) {
 		if (orgOrigo == null) {
 			orgOrigo = new Point(origo.getX(), origo.getY());
 			orgX = offsetX;
 			orgY = offsetY;
 		}
		if (selectedFloor != null) {
			selectedFloor.scale(scale);
 		}
 		offsetX = (int) Math.ceil(offsetX * scale);
 		offsetY = (int) Math.ceil(offsetY * scale);
 		gridSize = (int) Math.ceil(gridSize * scale);
 		origo.setX((int) Math.ceil(origo.getX() * scale));
 		origo.setY((int) Math.ceil(origo.getY() * scale));
 	}
 
 	private void reset() {
 		origo.setX(orgOrigo.getX());
 		origo.setY(orgOrigo.getY());
 		gridSize = GRID_SIZE;
 		offsetX = orgX;
 		offsetY = orgY;
 		orgOrigo = null;
 
		if (selectedFloor != null) {
			selectedFloor.reset();
 		}
 	}
 
 	@Override
 	public void onMouseDown(final MouseDownEvent event) {
 		if (animating) {
 			return;
 		}
 		downX = event.getClientX();
 		downY = event.getClientY();
 		mouseDown = true;
 		if (contextMenu != null) {
 			contextMenu.hide();
 			contextMenu = null;
 		}
 		if (isEditable && selectedFloor != null) {
 			selectedFloor.mouseDownEditable(downX, downY);
 		}
 	}
 
 	@Override
 	public void onMouseUp(final MouseUpEvent event) {
 		mouseDown = false;
 		if (selectedFloor != null) {
 			selectedFloor.mouseUp();
 		}
 	}
 
 	@Override
 	public void onMouseMove(final MouseMoveEvent event) {
 		if (animating) {
 			return;
 		}
 		final int clientX = event.getClientX();
 		final int clientY = event.getClientY();
 		if (mouseDown) {
 			mouseMoved = true;
 
 			if (selectedFloor != null) {
 				if (selectedFloor.selected == null) {
 					pan(event);
 				} else {
 					selectedFloor.moveRoom(selectedFloor.selected, event, clientX, clientY);
 				}
 			}
 
 			downX = clientX;
 			downY = clientY;
 
 			repaint();
 		} else if (selectedFloor != null) {
 			selectedFloor.checkHover(clientX, clientY);
 		}
 
 		// Change coloring of + and - buttons if hovered over.
 		if (clientX > Window.getClientWidth() - 50 && clientX < Window.getClientWidth() - 25) {
 			if (clientY > plus.getPosition().getY() && clientY < plus.getPosition().getY() + BUTTON_SIZE) {
 				plus.setColor(DEFAULT_SELECTED_COLOR);
 				clearColors(plus);
 			} else if (clientY > minus.getPosition().getY() && clientY < minus.getPosition().getY() + BUTTON_SIZE) {
 				minus.setColor(DEFAULT_SELECTED_COLOR);
 				clearColors(minus);
 			} else if (hasFloorAbove && clientY > up.getPosition().getY() && clientY < up.getPosition().getY() + BUTTON_SIZE) {
 				up.setColor(DEFAULT_SELECTED_COLOR);
 				clearColors(up);
 			} else if (hasFloorBelow && clientY > down.getPosition().getY() && clientY < down.getPosition().getY() + BUTTON_SIZE) {
 				down.setColor(DEFAULT_SELECTED_COLOR);
 				clearColors(down);
 			} else {
 				clearColors();
 			}
 		} else {
 			clearColors();
 		}
 		if (searchBar.mouseOver(clientX, clientY)) {
 			if (!searchBar.isVisible()) {
 				searchBar.setAnimate(true);
 				searchBar.setVisible(true);
 			}
 		} else if (searchBar.isVisible()) {
 			searchBar.setAnimate(true);
 			searchBar.setVisible(false);
 		}
 		if (buttonBar.mouseOver(clientX, clientY)) {
 			if (!buttonBar.isVisible()) {
 				buttonBar.setAnimate(true);
 				buttonBar.setVisible(true);
 			}
 		} else if (buttonBar.isVisible()) {
 			buttonBar.setAnimate(true);
 			buttonBar.setVisible(false);
 		}
 		repaint();
 	}
 
 	protected void clearColors(final GridButton... not) {
 		final List<GridButton> notList = Arrays.asList(not);
 		for (final GridButton button : buttons) {
 			if (!notList.contains(button)) {
 				button.setColor(DEFAULT_COLOR);
 			}
 		}
 	}
 
 	protected void pan(final MouseMoveEvent event) {
 		pan(event.getClientX() - downX, event.getClientY() - downY);
 	}
 
 	protected void pan(final int amountx, final int amounty) {
 		offsetX += amountx;
 		offsetY += amounty;
 
 		offsetX = offsetX % 50;
 		offsetY = offsetY % 50;
 
 		origo.move(amountx, amounty);
 		if (orgOrigo != null) {
 			orgOrigo.move(amountx, amounty);
 		}
		if (selectedFloor != null) {
			selectedFloor.panRooms(amountx, amounty);
 		}
 
 		for (final VisualItem item : items) {
 			item.movePosition(amountx, amounty);
 		}
 	}
 
 	boolean noChangeEvent = false;
 
 	@Override
 	public void onKeyUp(final KeyUpEvent event) {
 		if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 			noChangeEvent = true;
 			handleTextFieldValue(null);
 		}
 	}
 
 	@Override
 	public void onChange(final ChangeEvent event) {
 		if (noChangeEvent) {
 			noChangeEvent = false;
 		} else {
 			handleTextFieldValue(null);
 		}
 	}
 
 	protected void setAnimating(final boolean animating) {
 		this.animating = animating;
 	}
 
 	protected void handleTextFieldValue(String value) {
 		if (value == null) {
 			value = typeAndEdit.getValue();
 		}
 
 		if (value != null && !value.isEmpty()) {
 			final CommandObject cmd = new CommandObject(value);
 			if (selectedFloor != null) {
 				for (final CRoom room : selectedFloor.getRooms()) {
 					if (room.isSelected()) {
 						switch (cmd.getCommand()) {
 						case MOVE_TO:
 							room.setPosition(GeometryUtil.combine(origo, cmd.getPosition()));
 							typeAndEdit.setValue("");
 							break;
 						case MOVE_BY:
 							room.movePosition(cmd.getX(), cmd.getY());
 							typeAndEdit.setValue("");
 							break;
 						case SAVE:
 							fireEvent(new MenuEvent(MenuEvent.MenuEventType.UPDATE_ROOMS));
 							typeAndEdit.setValue("");
 							break;
 						case INVALID_STRING:
 							Window.alert("Command String was invalid");
 							break;
 						case PARSE_FAILED:
 							Window.alert("Parsing coordinates failed");
 							break;
 						}
 						repaint();
 						break;
 					}
 				}
 			}
 			switch (cmd.getCommand()) {
 			case PAN:
 				pan(cmd.getX(), cmd.getY());
 				typeAndEdit.setValue("");
 				repaint();
 				break;
 			case FIND:
 				if (selectedFloor != null) {
 					if (selectedFloor.getNames().contains(cmd.getValue())) {
 						typeAndEdit.setValue("");
 
 						selectedFloor.markTableOfSelectedPerson(cmd.getValue());
 					} else if (selectedFloor.namesContain(cmd)) {
 						typeAndEdit.setValue("");
 						final LinkedList<String> possible = selectedFloor.possibilities(cmd);
 						if (possible.size() == 1) {
 							selectedFloor.markTableOfSelectedPerson(possible.getFirst());
 						} else if (possible.size() > 1) {
 							new NameSelectPopup(possible, selectedFloor);
 						}
 					} else {
 						final VNotification notification = new VNotification();
 						final Style style = notification.getElement().getStyle();
 						style.setBackgroundColor("#c8ccd0");
 						style.setPadding(15.0, Unit.PX);
 						style.setProperty("border-radius", "4px");
 						style.setProperty("-moz-border-radius", "4px");
 						style.setProperty("-webkit-border-radius", "4px");
 
 						notification.show("No user found for [" + cmd.getValue() + "]", VNotification.CENTERED, null);
 					}
 				}
 				break;
 			}
 		}
 	}
 
 	@Override
 	public void onContextMenu(final ContextMenuEvent event) {
 		if (!isEditable || selectedFloor == null) {
 			return;
 		}
 		event.preventDefault();
 		mouseDown = false;
 		selectedFloor.selected = null;
 
 		final int x = event.getNativeEvent().getClientX();
 		final int y = event.getNativeEvent().getClientY();
 
 		final MenuBar rootMenu = new MenuBar(true);
 		final MenuBar roomMenu = new MenuBar(true);
 
 		final MenuItem triangle = new MenuItem("3 corners", selectedFloor.addCommand(3, x, y));
 		final MenuItem square = new MenuItem("4 corners", selectedFloor.addCommand(4, x, y));
 		final MenuItem fivePoints = new MenuItem("5 corners", selectedFloor.addCommand(5, x, y));
 		final MenuItem LShape = new MenuItem("L shaped 6 corners", selectedFloor.addCommand(6, x, y));
 
 		roomMenu.addItem(triangle);
 		roomMenu.addItem(square);
 		roomMenu.addItem(fivePoints);
 		roomMenu.addItem(LShape);
 
 		final MenuItem newRoom = new MenuItem("Add new room", roomMenu);
 		rootMenu.addItem(newRoom);
 
 		rootMenu.setVisible(true);
 		contextMenu = new PopupPanel();
 		contextMenu.add(rootMenu);
 
 		Style style = rootMenu.getElement().getStyle();
 		style.setProperty("backgroundColor", "gray");
 		style.setProperty("borderColor", "gray");
 		style.setProperty("borderWidth", "1px 3px 3px 1px");
 		style.setProperty("borderStyle", "solid");
 
 		style = roomMenu.getElement().getStyle();
 		style.setProperty("backgroundColor", "gray");
 		style.setProperty("borderColor", "gray");
 		style.setProperty("borderWidth", "1px 3px 3px 1px");
 		style.setProperty("borderStyle", "solid");
 
 		selectedFloor.checkForRemoveAndAddItem(rootMenu);
 
 		rootMenu.addItem(new MenuItem("Push states to server", new Command() {
 			@Override
 			public void execute() {
 				fireEvent(new MenuEvent(MenuEvent.MenuEventType.UPDATE_ROOMS));
 				contextMenu.hide();
 				contextMenu = null;
 			}
 		}));
 
 		contextMenu.setPopupPosition(x, y);
 		contextMenu.show();
 	}
 
 	@Override
 	public void onMouseOut(final MouseOutEvent event) {
 		mouseDown = false;
 		if (selectedFloor != null) {
 			selectedFloor.selected = null;
 		}
 	}
 
 	public void setEditable(final boolean editable) {
 		isEditable = editable;
 	}
 
 	public void clear() {
 		floors.clear();
 		items.clear();
 	}
 
 	public void add(final Widget widget) {
 		if (widget instanceof CFloor) {
 			floors.add((CFloor) widget);
 			Collections.sort(floors);
 		} else if (widget instanceof VisualItem) {
 			items.add((VisualItem) widget);
 		}
 	}
 
 	public void showNames() {
 		if (selectedFloor != null) {
 			selectedFloor.showNames();
 		}
 	}
 
 }
