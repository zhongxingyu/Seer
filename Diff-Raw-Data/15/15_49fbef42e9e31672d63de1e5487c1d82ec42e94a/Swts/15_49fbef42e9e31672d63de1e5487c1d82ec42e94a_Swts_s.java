 /* This file is part of SoftwareFm
 /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 /* This file is part of SoftwareFm
  /* SoftwareFm is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
 /* SoftwareFm is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
 /* You should have received a copy of the GNU General Public License along with SoftwareFm. If not, see <http://www.gnu.org/licenses/> */
 
 package org.softwareFm.display.swt;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Callable;
 
 import junit.framework.Assert;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowData;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.softwareFm.display.composites.IHasComposite;
 import org.softwareFm.display.composites.IHasControl;
 import org.softwareFm.display.constants.DisplayConstants;
 import org.softwareFm.utilities.arrays.ArrayHelper;
 import org.softwareFm.utilities.callbacks.ICallback;
 import org.softwareFm.utilities.collections.Files;
 import org.softwareFm.utilities.collections.Iterables;
 import org.softwareFm.utilities.collections.Lists;
 import org.softwareFm.utilities.constants.UtilityConstants;
 import org.softwareFm.utilities.constants.UtilityMessages;
 import org.softwareFm.utilities.exceptions.WrappedException;
 import org.softwareFm.utilities.functions.Functions;
 import org.softwareFm.utilities.functions.IFunction1;
 import org.softwareFm.utilities.indent.Indent;
 import org.softwareFm.utilities.resources.IResourceGetter;
 import org.softwareFm.utilities.strings.Strings;
 
 public class Swts {
 
 	public static class Actions {
 		public static Action Action(IResourceGetter resourceGetter, String key, Class<?> imageAnchor, String imageKey, final Runnable run) {
 			String text = IResourceGetter.Utils.getOrException(resourceGetter, key);
 			Action action = new Action(text, IAction.AS_RADIO_BUTTON) {
 				@Override
 				public void run() {
 					if (isChecked())
 						run.run();
 				}
 			};
 			ImageDescriptor imageDescriptor = getImageDescriptor(imageAnchor, imageKey);
 			action.setImageDescriptor(imageDescriptor);
 			return action;
 		}
 
 		private static ImageDescriptor getImageDescriptor(Class<?> imageAnchor, String imageKey) {
 			try {
 				return ImageDescriptor.createFromFile(imageAnchor, imageKey);
 			} catch (Exception e) {
 				return ImageDescriptor.getMissingImageDescriptor();
 			}
 		}
 	}
 
 	public static class Size {
 
 		public static Listener resizeMeToParentsSize(final Control control) {
 			Size.setSizeFromClientArea(control);
 
 			Listener listener = new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					Size.setSizeFromClientArea(control);
 				}
 			};
 			control.getParent().addListener(SWT.Resize, listener);
 			Swts.redrawAllChildren(control);
 			return listener;
 		}
 
 		static int globalId = 0;
 
 		public static Listener resizeMeToParentsSizeWithLayout(final IHasComposite hasComposite) {
 			final Composite composite = hasComposite.getComposite();
 			Size.setSizeFromClientArea(composite);
 
 			Listener listener = new Listener() {
 				// int id = globalId++;
 
 				@Override
 				public void handleEvent(Event event) {
 					// System.out.println("SWT/resizeMeToParentsSizeWithLayout " + id + boundsUpToShell(composite));
 					Size.setSizeFromClientArea(composite);
 					composite.layout();
 					Swts.redrawAllChildren(composite);
 					// System.out.println("   end SWT/resizeMeToParentsSizeWithLayout " + id + boundsUpToShell(composite));
 				}
 			};
 			composite.getParent().addListener(SWT.Resize, listener);
 			return listener;
 		}
 
 		public static Listener resizeMeToParentsSizeWithTopMargin(final Control control, final int topMargin) {
 			Size.setSizeAndLocationFromParentsSizeWithTopMargin(control, topMargin);
 
 			Listener listener = new Listener() {
 				@Override
 				public void handleEvent(Event event) {
 					Size.setSizeAndLocationFromParentsSizeWithTopMargin(control, topMargin);
 				}
 
 			};
 			control.getParent().addListener(SWT.Resize, listener);
 			return listener;
 		}
 
 		private static void setSizeAndLocationFromParentsSizeWithTopMargin(final Control control, final int topMargin) {
 			Rectangle clientArea = control.getParent().getClientArea();
 			control.setLocation(clientArea.x, clientArea.y + topMargin);
 			control.setSize(clientArea.width, clientArea.height - topMargin);
 		}
 
 		public static void removeOldResizeListener(final Control control, Listener listener) {
 			if (listener != null)
 				control.removeListener(SWT.Resize, listener);
 		}
 
 		public static void setSizeToComputedAndLayout(IHasControl left, int wHint, int hHint) {
 			Control control = left.getControl();
 			Size.setSizeToComputedSize(control, wHint, hHint);
 			if (control instanceof Composite)
 				((Composite) control).layout();
 		}
 
 		public static void setSizeToComputedAndLayout(IHasControl left, Rectangle rectangle) {
 			Control control = left.getControl();
 			Point computedSize = control.computeSize(rectangle.width, rectangle.height);
 			control.setSize(computedSize);
 			if (control instanceof Composite)
 				((Composite) control).layout();
 		}
 
 		public static void setSizeFromClientArea(IHasComposite parent, IHasControl child) {
 			Rectangle clientArea = parent.getComposite().getClientArea();
 			child.getControl().setSize(clientArea.width, clientArea.height);
 		}
 
 		public static Rectangle setSizeFromClientArea(Control child) {
 			Rectangle clientArea = child.getParent().getClientArea();
 			child.setSize(clientArea.width, clientArea.height);
 			if (child instanceof Composite)
 				((Composite) child).layout();
 			return clientArea;
 			// child.redraw();
 
 		}
 
 		public static void setSizeToComputedSize(Control c, int wHint, int hHint) {
 			Point size = c.computeSize(wHint, hHint);
 			c.setSize(size);
 		}
 
 	}
 
 	public static class Show {
 
 		public static void displayNoLayout(String title, IFunction1<Composite, Composite> builder) {
 			try {
 				Display display = new Display();
 				Shell shell = new Shell(display);
 				shell.setSize(600, 400);
 				shell.setText(title);
 				builder.apply(shell);
 				shell.open();
 				while (!shell.isDisposed()) {
 					if (!display.readAndDispatch())
 						display.sleep();
 				}
 				display.dispose();
 			} catch (Exception e) {
 				throw WrappedException.wrap(e);
 			}
 		}
 
 		public static void display(String title, IFunction1<Composite, Composite> builder) {
 			try {
 				Display display = new Display();
 				Shell shell = new Shell(display);
 				shell.setSize(1300, 500);
 				shell.setText(title);
 				shell.setLayout(new FormLayout());
 				Composite composite = builder.apply(shell);
 				FormData fd = new FormData();
 				fd.bottom = new FormAttachment(100, 0);
 				fd.right = new FormAttachment(100, 0);
 				fd.top = new FormAttachment(0, 0);
 				fd.left = new FormAttachment(0, 0);
 				composite.setLayoutData(fd);
 				shell.open();
 				Swts.layoutDump(shell);
 				while (!shell.isDisposed()) {
 					if (!display.readAndDispatch())
 						display.sleep();
 				}
 				display.dispose();
 			} catch (Exception e) {
 				throw WrappedException.wrap(e);
 			}
 		}
 
 		public static <T extends IHasControl> void xUnit(String title, final File root, final String extension, final ISituationListAndBuilder<T> builder) {
 			Swts.Show.display(title, new IFunction1<Composite, Composite>() {
 				@Override
 				public Composite apply(Composite from) throws Exception {
 					Callable<? extends Iterable<String>> situations = new Callable<Iterable<String>>() {
 						@Override
 						public Iterable<String> call() throws Exception {
 							return Iterables.map(Files.walkChildrenOf(root, Files.extensionFilter(extension)), Files.toFileName());
 						}
 					};
 					final SituationListAnd<T> result = new SituationListAnd<T>(from, situations, builder);
 					result.addListener(new ISituationListListener<T>() {
 						@Override
 						public void selected(T hasControl, String selectedItem) throws Exception {
 							File file = new File(root, selectedItem);
 							String value = Files.getText(file);
 							builder.selected(hasControl, selectedItem, value);
 							result.setText(value);
 						}
 					});
 
 					result.selectFirst();
 					return result.getComposite();
 				}
 			});
 		}
 
 		public static <T extends IHasControl> void xUnit(String title, final ISituationListAndBuilder<T> builder, final Map<String, Object> situationMap) {
 			Swts.Show.display(title, new IFunction1<Composite, Composite>() {
 				@Override
 				public Composite apply(Composite from) throws Exception {
 					final Callable<? extends Iterable<String>> situationsCallable = new Callable<Iterable<String>>() {
 						@Override
 						public Iterable<String> call() throws Exception {
 							return situationMap.keySet();
 						}
 					};
 					final SituationListAnd<T> result = new SituationListAnd<T>(from, situationsCallable, builder);
 					result.addListener(new ISituationListListener<T>() {
 						@Override
 						public void selected(T hasControl, String selectedItem) throws Exception {
 							Object value = situationMap.get(selectedItem);
 							builder.selected(hasControl, selectedItem, value);
 							result.setText(value.toString());
 						}
 					});
 
 					result.selectFirst();
 					return result.getComposite();
 				}
 			});
 		}
 
 	}
 
 	public static class Buttons {
 
 		public static void makeButtonFromMainMethod(Composite composite, final Class<?> classWithMain) {
 			org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(composite, SWT.PUSH);
 			button.setText(classWithMain.getSimpleName());
 			button.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					new Thread() {
 						@Override
 						public void run() {
 							try {
 								Method method = classWithMain.getMethod("main", String[].class);
 								method.invoke(null, new Object[] { new String[0] });
 							} catch (Exception e1) {
 								throw WrappedException.wrap(e1);
 							}
 						}
 					}.start();
 				}
 			});
 		}
 
 		public static org.eclipse.swt.widgets.Button makePushButton(Composite parent, IResourceGetter resourceGetter, String titleKey, final Runnable runnable) {
 			return Buttons.makePushButton(parent, resourceGetter, titleKey, true, runnable);
 		}
 
 		public static org.eclipse.swt.widgets.Button makePushButton(Composite parent, IResourceGetter resourceGetter, String titleOrKey, boolean titleIsKey, final Runnable runnable) {
 			return Buttons.makePushButton(parent, SWT.PUSH, resourceGetter, titleOrKey, titleIsKey, runnable);
 		}
 
 		public static org.eclipse.swt.widgets.Button makePushButton(Composite parent, int style, IResourceGetter resourceGetter, String titleOrKey, boolean titleIsKey, final Runnable runnable) {
 			org.eclipse.swt.widgets.Button button = new org.eclipse.swt.widgets.Button(parent, style);
 			String title = titleIsKey ? IResourceGetter.Utils.getOrException(resourceGetter, titleOrKey) : titleOrKey;
 			button.setText(title);
 			button.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					runnable.run();
 				}
 			});
 			return button;
 		}
 
 		public static Label makeImageButton(Composite parent, Image image, final Runnable runnable) {
 			Label label = new Label(parent, SWT.NULL);
 			label.setImage(image);
 			label.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseUp(MouseEvent e) {
 					runnable.run();
 				}
 			});
 			return label;
 		}
 
 	}
 
 	public static class Grid {
 
 		public static void addGrabHorizontalAndFillGridDataToAllChildrenWithMargins(Composite composite, int margin) {
 			GridLayout layout = new GridLayout();
 			layout.marginWidth = margin;
 			layout.marginHeight = margin;
 			layout.verticalSpacing = 5;
 			composite.setLayout(layout);
 			for (Control control : composite.getChildren()) {
 				GridData data = Grid.makeGrabHorizonalAndFillGridData();
 				control.setLayoutData(data);
 			}
 		}
 
 		public static void addGrabHorizontalAndFillGridDataToAllChildren(Composite composite) {
 			Swts.Grid.addGrabHorizontalAndFillGridDataToAllChildrenWithMargins(composite, 0);
 		}
 
 		public static void addGrabHorizontalAndFillGridDataToAllChildrenWithHeightHint(Composite composite, int heightHint) {
 			GridLayout layout = new GridLayout();
 			layout.marginWidth = 0;
 			layout.verticalSpacing = 0;
 			layout.marginHeight = 0;
 			composite.setLayout(layout);
 			for (Control control : composite.getChildren()) {
 				GridData data = Grid.makeGrabHorizonalAndFillGridDataWithHeight(heightHint);
 				control.setLayoutData(data);
 			}
 		}
 
 		public static void addGrabHorizontalAndFillGridDataToAllChildrenWithHeightWidthHint(Composite composite, int heightHint, int widthHint) {
 			GridLayout layout = new GridLayout();
 			layout.marginWidth = 0;
 			layout.verticalSpacing = 0;
 			layout.marginHeight = 0;
 			composite.setLayout(layout);
 			for (Control control : composite.getChildren()) {
 				GridData data = Grid.makeGrabHorizonalAndFillGridDataWithHeightWidth(heightHint, widthHint);
 				control.setLayoutData(data);
 			}
 		}
 
 		private static GridData makeGrabHorizonalAndFillGridDataWithHeightWidth(int heightHint, int widthHint) {
 			GridData result = Grid.makeGrabHorizonalAndFillGridData();
 			result.heightHint = heightHint;
 			result.widthHint = widthHint;
 			return result;
 
 		}
 
 		public static GridData makeGrabHorizonalVerticalAndFillGridData() {
 			GridData data = new GridData();
 			data.horizontalAlignment = GridData.FILL;
 			data.verticalAlignment = GridData.FILL;
 			data.grabExcessHorizontalSpace = true;
 			data.grabExcessVerticalSpace = true;
 			return data;
 		}
 
 		public static GridData makeGrabHorizonalAndFillGridData() {
 			GridData data = new GridData();
 			data.horizontalAlignment = GridData.FILL;
 			data.grabExcessHorizontalSpace = true;
 			return data;
 		}
 
 		public static Layout getGridLayoutWithoutMargins(int columns) {
 			GridLayout gridLayout = new GridLayout(columns, false);
 			gridLayout.marginWidth = 0;
 			gridLayout.marginHeight = 0;
 			gridLayout.verticalSpacing = 0;
 			gridLayout.horizontalSpacing = 0;
 			return gridLayout;
 		}
 
 		public static GridData makeGrabHorizonalAndFillGridDataWithHeight(int heightHint) {
 			GridData result = Swts.Grid.makeGrabHorizonalAndFillGridData();
 			result.heightHint = heightHint;
 			return result;
 
 		}
 
 	}
 
 	public static class Row {
 
 		public static Layout getHorizonalMarginRowLayout(int margin) {
 			RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
 			rowLayout.marginWidth = 0;
 			rowLayout.marginHeight = 0;
 			rowLayout.marginTop = margin;
 			rowLayout.marginBottom = margin;
 			rowLayout.marginLeft = margin;
 			rowLayout.marginRight = margin;
 			rowLayout.fill = true;
 			rowLayout.justify = false;
 			rowLayout.spacing = 0;
 			return rowLayout;
 		}
 
 		public static Layout getHorizonalNoMarginRowLayout() {
 			return Swts.Row.getHorizonalMarginRowLayout(0);
 		}
 
 		public static <C extends Control> void setRowDataFor(int width, int height, C... controls) {
 			for (Control control : controls)
 				control.setLayoutData(new RowData(width, height));
 
 		}
 
 	}
 
 	public static ScrolledComposite newScrolledComposite(Composite parent, int style, final String description) {
 		return new ScrolledComposite(parent, style) {
 			@Override
 			public String toString() {
 				return description + "." + super.toString();
 			}
 		};
 	}
 
 	public static Composite newComposite(Composite parent, int style, final String description) {
 		return new Composite(parent, style) {
 			@Override
 			public String toString() {
 				return description + "." + super.toString();
 			}
 		};
 	}
 
 	public static SashForm newSashForm(Composite parent, int style, final String description) {
 		return new SashForm(parent, style) {
 			@Override
 			public String toString() {
 				return description + "." + super.toString();
 			}
 		};
 	}
 
 	public static void redrawAllChildren(Control control) {
 		walkChildren(control, new ICallback<Control>() {
 			@Override
 			public void process(Control t) throws Exception {
 				t.redraw();
 			}
 		});
 	}
 
 	public static void walkChildren(Control control, ICallback<Control> callback) {
 		if (control instanceof Composite)
 			for (Control child : ((Composite) control).getChildren()) {
 				ICallback.Utils.call(callback, child);
 				walkChildren(child, callback);
 			}
 	}
 
 	public static <T extends Control> List<T> findChildrenWithClass(Control control, final Class<T> clazz) {
 		final List<T> result = Lists.newList();
 		walkChildren(control, new ICallback<Control>() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void process(Control t) throws Exception {
 				if (clazz.isAssignableFrom(t.getClass()))
 					result.add((T) t);
 			}
 		});
 		return result;
 	}
 
 	public static Group newGroup(Composite parent, int style, final String description) {
 		return new Group(parent, style) {
 			@Override
 			public String toString() {
 				return description + "." + super.toString();
 			}
 
 			@Override
 			protected void checkSubclass() {
 			}
 		};
 	}
 
 	public static StyledText makeHelpDisplayer(Composite parent) {
 		StyledText text = new StyledText(parent, SWT.WRAP | SWT.BORDER);
 		text.setEditable(false);
 		text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
 		return text;
 
 	}
 
 	public static void setHelpText(StyledText help, IResourceGetter resourceGetter, String helpKey) {
 		help.setText(helpKey == null ? "" : Strings.nullSafeToString(resourceGetter.getStringOrNull(helpKey)));
 		help.getParent().layout();
 	}
 
 	public static void setFontStyle(Label label, int fontStyle) {
 		Font font = label.getFont();
 		FontData fontData = font.getFontData()[0];
 		Font newFont = new Font(label.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), fontStyle));
 		label.setFont(newFont);
 	}
 
 	public static void setFontStyle(StyledText text, int fontStyle) {
 		Font font = text.getFont();
 		FontData fontData = font.getFontData()[0];
 		Font newFont = new Font(text.getDisplay(), new FontData(fontData.getName(), fontData.getHeight(), fontStyle));
 		text.setFont(newFont);
 	}
 
 	public static Control setAfter(List<Control> controls, Control firstControl) {
 		for (Control control : controls) {
 			control.moveBelow(firstControl);
 			firstControl = control;
 		}
 		return firstControl;
 	}
 
 	public static <K, V extends Composite> void sortVisibilityForComposites(Map<K, V> map, IFunction1<K, Boolean> acceptor) {
 		sortVisibility(map, Functions.<V, Control> toSingletonList(), acceptor);
 	}
 
 	public static <K> void sortVisibilityForHasControlList(Map<K, List<IHasControl>> map, IFunction1<K, Boolean> acceptor) {
 		sortVisibility(map, IHasControl.Utils.toListOfControls(), acceptor);
 	}
 
 	public static <K, V> void sortVisibility(Map<K, V> map, IFunction1<V, List<Control>> convertor, IFunction1<K, Boolean> acceptor) {
 		try {
 			final List<Control> visibleControls = Lists.newList();
 			final List<Control> invisibleControls = Lists.newList();
 			for (Entry<K, V> entry : map.entrySet()) {
 				V value = entry.getValue();
 				for (Control control : convertor.apply(value)) {
 					boolean isVisible = acceptor.apply(entry.getKey());
 					control.setVisible(isVisible);
 					if (isVisible)
 						visibleControls.add(control);
 					else
 						invisibleControls.add(control);
 				}
 			}
 			Control lastVisible = Swts.setAfter(visibleControls, null);
 			for (Control control : invisibleControls)
 				control.moveBelow(lastVisible);
 		} catch (Exception e) {
 			throw WrappedException.wrap(e);
 		}
 	}
 
 	public static void makeOnlyOneVisible(Composite content, Control control) {
 		final List<Control> visibleControls = Lists.newList();
 		final List<Control> invisibleControls = Lists.newList();
 		for (Control child : content.getChildren()) {
 			boolean visible = child == control;
 			child.setVisible(visible);
 			if (visible)
 				visibleControls.add(child);
 			else
 				invisibleControls.add(child);
 		}
 		Control lastVisible = Swts.setAfter(visibleControls, null);
 		for (Control invisibleControl : invisibleControls)
 			invisibleControl.moveBelow(lastVisible);
 	}
 
 	public static TableEditor addEditor(Table table, int row, int col, Control control) {
 		TableItem[] items = table.getItems();
 		TableEditor editor = new TableEditor(table);
 		editor.grabHorizontal = true;
 		editor.setEditor(control, items[row], col);
 		return editor;
 	}
 
 	public static String layoutAsString(Control control) {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append(control);
 		Point location = control.getLocation();
 		Point size = control.getSize();
 		buffer.append(" Loc: " + location.x + ", " + location.y);
 		buffer.append(" Size: " + size.x + ", " + size.y);
 		if (control instanceof Composite)
 			buffer.append(" Layout: " + ((Composite) control).getLayout());
 		buffer.append(" LayoutData: " + control.getLayoutData());
 		return buffer.toString();
 	}
 
 	public static void layoutDump(Control control) {
 		layoutDump(control, new Indent());
 	}
 
 	public static void layoutDump(Control control, Indent indent) {
 		System.out.println(indent + control.getClass().getSimpleName() + "(Visible: " + control.isVisible() + ": " + layoutAsString(control));
 		if (control.getLayoutData() instanceof GridData)
 			if (!(control.getParent().getLayout() instanceof GridLayout))
 				System.err.println("Layout issues: child has GridData parent not GridLayout");
 		if (control.getLayoutData() instanceof RowData)
 			if (!(control.getParent().getLayout() instanceof RowLayout))
 				System.err.println("Layout issues: child has RowData parent not RowLayout");
 		if (control instanceof Composite) {
 			Composite composite = (Composite) control;
 			for (Control nested : composite.getChildren()) {
 				Indent indented = indent.indent();
 				layoutDump(nested, indented);
 			}
 		}
 	}
 
 	public static void removeAllChildren(Composite composite) {
 		for (Control control : composite.getChildren())
 			control.dispose();
 
 	}
 
 	public static List<Control> children(Composite composite) {
 		return Arrays.asList(composite.getChildren());
 	}
 
 	public static List<Control> allChildren(Composite composite) {
 		List<Control> result = Lists.newList();
 		for (Control c : composite.getChildren()) {
 			result.add(c);
 			if (c instanceof Composite)
 				result.addAll(allChildren((Composite) c));
 		}
 		return result;
 	}
 
 	public static void assertHasChildrenInOrder(Composite composite, Control... controls) {
 		List<Control> children = children(composite);
 		int lastIndex = -1;
 		for (Control c : controls) {
 			int index = children.indexOf(c);
 			if (index == -1)
 				Assert.fail("Could not find " + c + " in " + children);
 			if (index < lastIndex)
 				Assert.fail("Child " + index + "/" + c + " is in wrong order\nExpected " + controls + "\naActual: " + children);
 
 		}
 
 	}
 
 	public static IFunction1<String, Image> imageFn(final ImageRegistry imageRegistry) {
 		return new IFunction1<String, Image>() {
 			@Override
 			public Image apply(String from) throws Exception {
 				return imageRegistry.get(from);
 			}
 		};
 	}
 
 	public static void asyncExec(IHasControl hasControl, Runnable runnable) {
 		Control control = hasControl.getControl();
 		if (!control.isDisposed())
 			control.getDisplay().asyncExec(runnable);
 	}
 
 	public static void asyncExec(Control control, Runnable runnable) {
 		if (!control.isDisposed())
 			control.getDisplay().asyncExec(runnable);
 
 	}
 
 	public static void syncExec(IHasControl hasControl, Runnable runnable) {
 		Control control = hasControl.getControl();
 		if (!control.isDisposed())
 			control.getDisplay().syncExec(runnable);
 	}
 
 	public static void dispatchUntilQueueEmpty(Display display) {
 		while (display.readAndDispatch())
 			doNothing();
 	}
 
 	private static void doNothing() {
 	}
 
 	public static void removeChildrenAfter(Composite composite, Control control) {
 		Control[] children = composite.getChildren();
 		int index = ArrayHelper.findIndexOf(children, control);
 		if (index != -1)
 			for (int i = index + 1; i < children.length; i++)
 				children[i].dispose();
 	}
 
 	public static Color makeColor(Device device, IResourceGetter resourceGetter, String colorKey) {
 		String raw = IResourceGetter.Utils.getOrException(resourceGetter, colorKey);
 		List<String> strings = Strings.splitIgnoreBlanks(raw, ",");
 		if (strings.size() != 3)
 			throw new IllegalStateException(MessageFormat.format(DisplayConstants.illegalColorString, colorKey, raw));
 		int r = Integer.parseInt(strings.get(0));
 		int g = Integer.parseInt(strings.get(1));
 		int b = Integer.parseInt(strings.get(2));
 		return new Color(device, r, g, b);
 	}
 
 	public static IFunction1<Composite, IHasControl> labelFn(final String text) {
 		return new IFunction1<Composite, IHasControl>() {
 
 			@Override
 			public IHasControl apply(Composite from) throws Exception {
 				Label label = new Label(from, SWT.NULL);
 				label.setText(text);
 				return IHasControl.Utils.toHasControl(label);
 			}
 		};
 	}
 
 	public static IFunction1<Composite, IHasControl> styledTextFn(final String text, final int style) {
 		return new IFunction1<Composite, IHasControl>() {
 
 			@Override
 			public IHasControl apply(Composite from) throws Exception {
 				StyledText styledText = new StyledText(from, style);
 				styledText.setText(text);
 				return IHasControl.Utils.toHasControl(styledText);
 			}
 		};
 	}
 
 	public static void layoutIfComposite(Control control) {
 		if (control instanceof Composite)
 			((Composite) control).layout();
 
 	}
 
 	public static String boundsUpToShell(Control control) {
 		StringBuffer buffer = new StringBuffer();
 		boundsUpToShell(buffer, control);
 		return buffer.toString();
 	}
 
 	private static void boundsUpToShell(StringBuffer buffer, Control control) {
 		if (buffer.length() > 0)
 			buffer.append(" / ");
 		buffer.append(control.getClass().getSimpleName() + ": " + control.getBounds());
 		if (control.getParent() == null)
 			return;
 		else {
 			Composite parent = control.getParent();
 			boundsUpToShell(buffer, parent);
 		}
 
 	}
 
 	public static String clientAreasUpToShell(Control control) {
 		StringBuffer buffer = new StringBuffer();
 		clientAreasUpToShell(buffer, control);
 		return buffer.toString();
 	}
 
 	private static void clientAreasUpToShell(StringBuffer buffer, Control control) {
 		if (buffer.length() > 0)
 			buffer.append(" / ");
 		String value = control instanceof Composite ? ((Composite) control).getClientArea().toString() : "na";
 		buffer.append(control.getClass().getSimpleName() + ": " + value);
 		if (control.getParent() == null)
 			return;
 		else {
 			Composite parent = control.getParent();
 			clientAreasUpToShell(buffer, parent);
 		}
 
 	}
 
 	public static Button findButtonWithText(List<Button> buttons, String string) {
 		for (Button button : buttons)
 			if (string.equals(button.getText()))
 				return button;
 		throw new IllegalArgumentException(MessageFormat.format(UtilityConstants.cannotFindButtonWithText, string, buttons));
 	}
 
 	public static Layout titleAndContentLayout(final int titleHeight) {
 		return new Layout() {
 
 			@Override
 			protected void layout(Composite composite, boolean flushCache) {
 				Control[] children = composite.getChildren();
 				assert children.length == 2 : Arrays.asList(children);
 				Control title = children[0];
 				Control content = children[1];
 				Rectangle ca = composite.getClientArea();
 				title.setBounds(ca.x, ca.y, ca.width, titleHeight);
 				content.setBounds(ca.x, ca.y + titleHeight, ca.width, ca.height - titleHeight);
 			}
 
 			@Override
 			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
 				Control[] children = composite.getChildren();
 				assert children.length == 2;
 				Control content = children[1];
 				Point size = content.computeSize(wHint, hHint);
 				return new Point(size.x, size.y + titleHeight);
 			}
 		};
 
 	}
 
 	public static Layout titleAndRhsLayout(final int rightMargin) {
 		return new Layout() {
 
 			@Override
 			protected void layout(Composite composite, boolean flushCache) {
 				Control[] children = composite.getChildren();
 				assert children.length == 2 : Arrays.asList(children);
 				Control lhs = children[0];
 				Control rhs = children[1];
 				int rhsWidth = children[1].computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
 				Rectangle ca = composite.getClientArea();
 				lhs.setBounds(ca.x, ca.y, ca.width - rhsWidth - rightMargin, ca.height);
 				rhs.setBounds(ca.x + ca.width - rhsWidth - rightMargin, ca.y, rhsWidth, ca.height);
 			}
 
 			@Override
 			protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
 				Control[] children = composite.getChildren();
 				assert children.length == 2;
 				Control lhs = children[0];
 				Control rhs = children[1];
 				Point lhSize = lhs.computeSize(wHint, hHint);
 				Point rhSize = rhs.computeSize(wHint, hHint);
 				return new Point(lhSize.x + rhSize.x + rightMargin, Math.max(lhSize.y, rhSize.y));
 			}
 		};
 	}
 
 	public static void addMenuTo(IHasControl hasControl, IFunction1<Control, Menu> builder) {
 		Control control = hasControl.getControl();
 		Menu menu = Functions.call(builder, control);
 		control.setMenu(menu);
 	}
 
 	public static void addMenu(Menu menu, IResourceGetter resourceGetter, String key, final Runnable runnable) {
 		String text = IResourceGetter.Utils.getOrException(resourceGetter, key);
 		MenuItem item = new MenuItem(menu, SWT.NULL);
 		item.setText(text);
 		item.addListener(SWT.Selection, new Listener() {
 			@Override
 			public void handleEvent(Event event) {
 				runnable.run();
 			}
 		});
 	}
 
 	public static Point computeSizeForVerticallyStackedComposites(int wHint, int hHint, Control... controls) {
 		int sumY = 0;
 		int maxX = 0;
 		for (Control control : controls) {
 			Point size = control.computeSize(wHint, hHint);
 			maxX = Math.max(maxX, size.x);
 			sumY += size.y;
 		}
 		return new Point(maxX, sumY);
 	}
 
 	public static Control getDescendant(Control control, int... childIndicies) {
 		return getDescendant(control, 0, childIndicies);
 
 	}
 
 	private static Control getDescendant(Control control, int index, int[] childIndicies) {
 		if (index >= childIndicies.length)
 			return control;
 		if (control instanceof Composite)
 			return getDescendant(((Composite) control).getChildren()[childIndicies[index]], index + 1, childIndicies);
 		throw new IllegalArgumentException(MessageFormat.format(UtilityMessages.cannotGetDescendant, index, Arrays.asList(childIndicies)));
 	}
 
 }
