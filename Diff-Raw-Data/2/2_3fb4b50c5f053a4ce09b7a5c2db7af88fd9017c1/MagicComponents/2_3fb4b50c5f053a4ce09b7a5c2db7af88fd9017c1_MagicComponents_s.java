 package net.abusingjava.swing;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.lang.reflect.Array;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.FutureTask;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JToggleButton;
 import javax.swing.JTree;
 import javax.swing.ListModel;
 import javax.swing.SortOrder;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.tree.TreeModel;
 
 import net.abusingjava.Author;
 import net.abusingjava.Since;
 import net.abusingjava.Version;
 import net.abusingjava.swing.magic.Cards.CardComponent;
 import net.abusingjava.swing.magic.CheckBox;
 import net.abusingjava.swing.magic.Component;
 import net.abusingjava.swing.magic.MultiList;
 import net.abusingjava.swing.magic.MultiList.MultiListTable;
 import net.abusingjava.swing.magic.TextComponent;
 import net.abusingjava.swing.magic.ToggleButton;
 import net.java.balloontip.BalloonTip;
 
 import org.jdesktop.swingx.JXDatePicker;
 import org.jdesktop.swingx.JXTable;
 
 /**
  * A Collection of Components on which you may apply a function as you want.
  */
 @Author("Julian Fleischer")
 @Version("2011-12-20")
 @Since(value = "2011-08-19", version = "1.0")
 public class MagicComponents {
 
 	final ArrayList<Component> $components = new ArrayList<Component>();
 
 	@SuppressWarnings("unused")
 	final MagicPanel $parent;
 
 	public MagicComponents(final MagicPanel $parent, final Component... $components) {
 		this.$parent = $parent;
 		this.$components.ensureCapacity($components.length);
 		for (final Component $c : $components) {
 			this.$components.add($c);
 		}
 	}
 
 	public MagicComponents(final MagicPanel $parent, final Collection<Component> $components) {
 		this.$parent = $parent;
 		this.$components.ensureCapacity($components.size());
 		for (final Component $c : $components) {
 			this.$components.add($c);
 		}
 	}
 
 	public Component get(final int $index) {
 		return $components.get($index);
 	}
 	
 	public MagicComponents setText(final Object $object) {
 		return setText($object.toString());
 	}
 
 	public MagicComponents setText(final String $text) {
 		for (final Component $comp : $components) {
 			if ($comp instanceof TextComponent) {
 				AbusingSwing.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						((TextComponent) $comp).setText($text);
 					}
 				});
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents setModel(final Object $model) {
 		if ($model instanceof TableModel) {
 			for (final Component $comp : $components) {
 				final JComponent $c = $comp.getRealComponent();
 				if ($c instanceof JTable) {
 					((JTable) $c).setModel((TableModel) $model);
 				}
 			}
 		} else if ($model instanceof ListModel) {
 			for (final Component $comp : $components) {
 				final JComponent $c = $comp.getRealComponent();
 				if ($c instanceof JList) {
 					((JList) $c).setModel((ListModel) $model);
 				}
 			}
 		} else if ($model instanceof TreeModel) {
 			for (final Component $comp : $components) {
 				final JComponent $c = $comp.getRealComponent();
 				if ($c instanceof JTree) {
 					((JTree) $c).setModel((TreeModel) $model);
 				}
 			}
 		} else if ($model instanceof ComboBoxModel) {
 			for (final Component $comp : $components) {
 				final JComponent $c = $comp.getRealComponent();
 				if ($c instanceof JComboBox) {
 					((JComboBox) $c).setModel((ComboBoxModel) $model);
 				}
 			}
 		}
 		return this;
 	}
 
 	public boolean isSelected() {
 		for (final Component $comp : $components) {
 			if ($comp instanceof ToggleButton) {
 				final FutureTask<Boolean> $task = new FutureTask<Boolean>(new Callable<Boolean>() {
 					@Override
 					public Boolean call() throws Exception {
 						return ((JToggleButton) $comp.getRealComponent()).isSelected();
 					}
 				});
 				AbusingSwing.invokeLater($task);
 				try {
 					return $task.get();
 				} catch (final InterruptedException $ev) {
 					$ev.printStackTrace();
 				} catch (final ExecutionException $ev) {
 					$ev.printStackTrace();
 				}
 			}
 		}
 		return false;
 	}
 
 	public String getText() {
 		for (final Component $comp : $components) {
 			if ($comp instanceof TextComponent) {
 				final FutureTask<String> $task = new FutureTask<String>(new Callable<String>() {
 					@Override
 					public String call() {
 						return ((TextComponent) $comp).getText();
 					}
 				});
 				AbusingSwing.invokeLater($task);
 				try {
 					return $task.get();
 				} catch (final InterruptedException $exc) {
 					// TODO Auto-generated catch block
 					$exc.printStackTrace();
 				} catch (final ExecutionException $exc) {
 					// TODO Auto-generated catch block
 					$exc.printStackTrace();
 				}
 			} else if ($comp.getRealComponent() instanceof JSpinner) {
 				final FutureTask<String> $task = new FutureTask<String>(new Callable<String>() {
 					@Override
 					public String call() {
 						return ((JSpinner) ($comp.getRealComponent())).getValue().toString();
 					}
 				});
 				AbusingSwing.invokeLater($task);
 				try {
 					return $task.get();
 				} catch (final InterruptedException $exc) {
 					// TODO Auto-generated catch block
 					$exc.printStackTrace();
 				} catch (final ExecutionException $exc) {
 					// TODO Auto-generated catch block
 					$exc.printStackTrace();
 				}
 			}
 		}
 		return "";
 	}
 	
 	public Object getValue() {
 		for (final Component $comp : $components) {
 			if ($comp.getRealComponent() instanceof JSpinner) {
 				final FutureTask<Object> $task = new FutureTask<Object>(new Callable<Object>() {
 					@Override
 					public Object call() {
 						return ((JSpinner) ($comp.getRealComponent())).getValue();
 					}
 				});
 				AbusingSwing.invokeLater($task);
 				try {
 					return $task.get();
 				} catch (final InterruptedException $exc) {
 					$exc.printStackTrace();
 				} catch (final ExecutionException $exc) {
 					$exc.printStackTrace();
 				}
 			} else if ($comp.getRealComponent() instanceof JXDatePicker) {
 				final FutureTask<Object> $task = new FutureTask<Object>(new Callable<Object>() {
 					@Override
 					public Object call() {
 						return ((JXDatePicker) ($comp.getRealComponent())).getDate();
 					}
 				});
 				AbusingSwing.invokeLater($task);
 				try {
 					return $task.get();
 				} catch (final InterruptedException $exc) {
 					$exc.printStackTrace();
 				} catch (final ExecutionException $exc) {
 					$exc.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	public MagicComponents sortBy(final int $columnIndex) {
 		for (final Component $comp : $components) {
 			final JComponent $real = $comp.getRealComponent();
 			if ($real instanceof JXTable) {
 				final JXTable $jxTable = (JXTable) $real;
 				if ($jxTable.getSortedColumn() == null) {
 					$jxTable.setSortOrder($columnIndex, SortOrder.ASCENDING);
 				}
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents clear() {
 		for (final Component $comp : $components) {
 			final JComponent $real = $comp.getRealComponent();
 			if ($real instanceof JTable) {
 				final TableModel $m = ((JTable) $real).getModel();
 				if ($m instanceof DefaultTableModel) {
 					try {
 						AbusingSwing.invokeAndWait(new Runnable() {
 							@Override
 							public void run() {
 								while ($m.getRowCount() > 0) {
 									((DefaultTableModel) $m).removeRow(0);
 								}
 							}
 						});
 					} catch (final Exception $exc) {
 						throw new RuntimeException($exc);
 					}
 				}
 			} else if ($real instanceof JList) {
 				final ListModel $m = ((JList) $real).getModel();
 				if ($m instanceof DefaultListModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							((DefaultListModel) $m).setSize(0);
 						}
 					});
 				}
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents show(final int $index) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c instanceof JTabbedPane) {
 						final JTabbedPane $pane = (JTabbedPane) $c;
 						$pane.setSelectedIndex($index);
 					} else if ($c.getLayout() instanceof CardLayout) {
 						final CardLayout $layout = (CardLayout) $c.getLayout();
 						$layout.first($c);
 						for (int $i = 0; $i < $index; $i++) {
 							$layout.next($c);
 						}
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents showNext() {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c.getLayout() instanceof CardLayout) {
 						((CardLayout) $c.getLayout()).next($c);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents showPrev() {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c.getLayout() instanceof CardLayout) {
 						((CardLayout) $c.getLayout()).previous($c);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents showFirst() {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c.getLayout() instanceof CardLayout) {
 						((CardLayout) $c.getLayout()).first($c);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents showLast() {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c.getLayout() instanceof CardLayout) {
 						((CardLayout) $c.getLayout()).last($c);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	/**
 	 * Returns the underlying components as the specified $class.
 	 * <p>
 	 * This applies to the first component in the list which can be cast to the
 	 * specified list.
 	 * <p>
 	 * <b>Example:</b>
 	 * <code>$(".allComponentsOfThisClass").as(JTable.class)</code> will return
 	 * the underlying JTable of the first component in the class
 	 * “.allComponentsOfThisClass” that is actually realized by a JTable.
 	 * <p>
 	 * The check whether the shoe fits or not is done using
 	 * {@link Class#isAssignableFrom(Class)}, i.e. if the underlying
 	 * {@link JComponent} is (for example) a {@link JXTable} (which is a JTable,
 	 * too) it will match in the above example.
 	 * <p>
 	 * This method is <b>not</b> <i>thread-safe</i>, since it exposes the
 	 * underlying JComponents to any calling thread. Don’t get me wrong dude,
 	 * calling the method itself from any thread causes no harm, however, you
 	 * will be responsible for invoking methods of the returned components on
 	 * the AWT Event Queue.
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends JComponent> T as(final Class<T> $class) {
 		if ($class.isArray()) {
 			final List<T> $list = new LinkedList<T>();
 			for (final Component $comp : $components) {
 				final JComponent $c = $comp.getRealComponent();
 				if ($class.isAssignableFrom($c.getClass())) {
 					$list.add((T) $c);
 				}
 			}
 			return (T) (Object) $list.toArray((Object[]) Array.newInstance($class.getComponentType(), $list.size()));
 		}
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($class.isAssignableFrom($c.getClass())) {
 				return (T) $c;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the underlying components as a {@link List} of the specified 
 	 * $class or, if the result would be an empty list, returns <b>null</b> in 
 	 * that case.
 	 * <p>
 	 * The check whether the shoe fits or not is done using
 	 * {@link Class#isAssignableFrom(Class)}, i.e. if the underlying
 	 * {@link JComponent} is (for example) a {@link JXTable} (which is a JTable,
 	 * too) it will match in the above example.
 	 * <p>
 	 * This method is <b>not</b> <i>thread-safe</i>, since it exposes the
 	 * underlying JComponents to any calling thread. Don’t get me wrong dude,
 	 * calling the method itself from any thread causes no harm, however, you
 	 * will be responsible for invoking methods of the returned components on
 	 * the AWT Event Queue.
 	 */
 	@SuppressWarnings("unchecked")
 	public <T extends JComponent> List<T> asList(final Class<T> $class) {
 		final List<T> $result = new ArrayList<T>($components.size());
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($class.isAssignableFrom($c.getClass())) {
 				$result.add((T) $c);
 			}
 		}
		return $result;
 	}
 	
 	public int count() {
 		return $components.size();
 	}
 
 	/**
 	 * Applies to any component. Shows the components.
 	 * <p>
 	 * This works by calling {@link JComponent#setVisible(boolean)}.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents show() {
 		for (final Component $comp : $components) {
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$comp.getJComponent().setVisible(true);
 				}
 			});
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Hides the components.
 	 * <p>
 	 * This works by calling {@link JComponent#setVisible(boolean)}. The
 	 * components will simply not be shown anymore, however, they are not
 	 * removed from the layout (i.e. they will leave an empty space).
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents hide() {
 		for (final Component $comp : $components) {
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$comp.getJComponent().setVisible(false);
 
 				}
 			});
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Enables the components.
 	 * <p>
 	 * This works by calling {@link JComponent#setEnabled(boolean)}.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents enable() {
 		for (final Component $comp : $components) {
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$comp.getJComponent().setEnabled(true);
 				}
 			});
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Disables the components.
 	 * <p>
 	 * This works by calling {@link JComponent#setEnabled(boolean)}.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents disable() {
 		for (final Component $comp : $components) {
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$comp.getJComponent().setEnabled(false);
 				}
 			});
 		}
 		return this;
 	}
 
 	public MagicComponents setForeground(final Color $color) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					$comp.getRealComponent().setForeground($color);
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents setForeground(final String $hexColor) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					$comp.getRealComponent().setForeground(
 							new net.abusingjava.swing.magix.types.Color($hexColor).getColor());
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents setBackground(final Color $color) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					$comp.getRealComponent().setBackground($color);
 				}
 			}
 		});
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Sets the background color of the particular
 	 * components.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents setBackground(final String $hexColor) {
 		for (final Component $comp : $components) {
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$comp.getRealComponent().setBackground(
 							new net.abusingjava.swing.magix.types.Color($hexColor).getColor());
 				}
 			});
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Sets the font of the particular components.
 	 * <p>
 	 * The font is identified by calling {@link Font#decode(String)}.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents setFont(final String $font) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					$c.setFont(Font.decode($font));
 				}
 			}
 		});
 		return this;
 	}
 
 	/**
 	 * Applies to any component. Sets the font size of the particular component.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents setFontSize(final int $size) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			AbusingSwing.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					$c.setFont($c.getFont().deriveFont((float) $size));
 				}
 			});
 		}
 		return this;
 	}
 
 	public MagicComponents add(final Iterable<?> $values) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($comp instanceof MultiList) {
 				final DefaultTableModel $m = (DefaultTableModel) ((JTable) $c).getModel();
 				for (final Object $value : $values) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							$m.addRow(new Object[]{false, $value});
 						}
 					});
 				}
 			} else if ($c instanceof JComboBox) {
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					for (final Object $value : $values) {
 						AbusingSwing.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								((DefaultComboBoxModel) $m).addElement($value);
 							}
 						});
 					}
 				}
 			} else if ($c instanceof JList) {
 				final ListModel $m = ((JList) $c).getModel();
 				if ($m instanceof DefaultListModel) {
 					for (final Object $value : $values) {
 						AbusingSwing.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								((DefaultListModel) $m).addElement($value);
 							}
 						});
 					}
 				}
 			}
 		}
 		return this;
 	}
 	/**
 	 * Applies to all &lt;table&gt;-Objects. Adds the row, specified as Array.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents addRow(final Object[] $values) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JTable) {
 				final TableModel $m = ((JTable) $c).getModel();
 				if ($m instanceof DefaultTableModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							((DefaultTableModel) $m).addRow($values);
 						}
 					});
 				}
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to all &lt;table&gt;-Objects. Adds multiple rows.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents addRows(final Object[][] $values) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JTable) {
 				final TableModel $m = ((JTable) $c).getModel();
 				if ($m instanceof DefaultTableModel) {
 					for (final Object[] $row : $values) {
 						AbusingSwing.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								((DefaultTableModel) $m).addRow($row);
 							}
 						});
 					}
 				}
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to all &lt;combobx&gt;, and &lt;list&gt;-Objects. Adds an item to
 	 * the underlying model.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents add(final Object[] $values) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof MultiListTable) {
 				for (final Object $value : $values) {
 					add($value);
 				}
 			} else if ($c instanceof JComboBox) {
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					for (final Object $value : $values) {
 						AbusingSwing.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								((DefaultComboBoxModel) $m).addElement($value);
 							}
 						});
 					}
 				}
 			} else if ($c instanceof JList) {
 				final ListModel $m = ((JList) $c).getModel();
 				if ($m instanceof DefaultListModel) {
 					for (final Object $value : $values) {
 						AbusingSwing.invokeLater(new Runnable() {
 							@Override
 							public void run() {
 								((DefaultListModel) $m).addElement($value);
 							}
 						});
 					}
 				}
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Applies to all &lt;combobox&gt;, &lt;list&gt;, and
 	 * &lt;multilist&gt;-Objects. Adds an item to the underlying model.
 	 * <p>
 	 * This method is <i>thread-safe</i>. You can call it from any thread, it’s
 	 * actions will execute in the AWT-Event-Queue.
 	 */
 	public MagicComponents add(final Object $value) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JComboBox) {
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							$comp.setUpdate(true);
 							((DefaultComboBoxModel) $m).addElement($value);
 							$comp.setUpdate(false);
 						}
 					});
 				}
 			} else if ($c instanceof JList) {
 				final ListModel $m = ((JList) $c).getModel();
 				if ($m instanceof DefaultListModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							((DefaultListModel) $m).addElement($value);
 						}
 					});
 				}
 			} else if ($comp instanceof MultiList) {
 				final TableModel $m = ((JTable) $c).getModel();
 				if ($m instanceof DefaultTableModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							((DefaultTableModel) $m).addRow(new Object[]{false, $value});
 						}
 					});
 				}
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents add(final Object $value, final boolean $selected) {
 		for (final Component $comp : $components) {
 			if ($comp instanceof MultiList) {
 				final JComponent $c = $comp.getRealComponent();
 				final TableModel $m = ((JTable) $c).getModel();
 				if ($m instanceof DefaultTableModel) {
 					AbusingSwing.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							((DefaultTableModel) $m).addRow(new Object[]{$selected, $value});
 						}
 					});
 				}
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents setSelectedItem(final Object $item) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JComboBox) {
 				$comp.setUpdate(true);
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					$m.setSelectedItem($item);
 				}
 				$comp.setUpdate(false);
 			} else if ($c instanceof JList) {
 				((JList) $c).setSelectedValue($item, true);
 			}
 		}
 		return this;
 	}
 
 	public MagicComponents setSelectedIndex(final int $index) {
 		AbusingSwing.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					if ($c instanceof JComboBox) {
 						//$comp.setUpdate(true);
 						final ComboBoxModel $m = ((JComboBox) $c).getModel();
 						if ($m instanceof DefaultComboBoxModel) {
 							$m.setSelectedItem($m.getElementAt($index));
 						}
 						//$comp.setUpdate(false);
 					} else if ($c instanceof JList) {
 						((JList) $c).setSelectedIndex($index);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	public MagicComponents setSelected(final boolean $selected) {
 		for (final Component $comp : $components) {
 			if ($comp instanceof CheckBox) {
 				final JCheckBox $checkBox = (JCheckBox) $comp.getRealComponent();
 				AbusingSwing.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						$checkBox.setSelected($selected);
 					}
 				});
 			} else if ($comp instanceof ToggleButton) {
 				final JToggleButton $toggleButton = (JToggleButton) $comp.getRealComponent();
 				AbusingSwing.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						$toggleButton.setSelected($selected);
 					}
 				});
 			}
 		}
 		return this;
 	}
 
 	public List<Object> getSelectedItems() {
 		for (final Component $comp : $components) {
 			if ($comp instanceof MultiList) {
 				final DefaultTableModel $m = (DefaultTableModel) ((JTable) $comp.getRealComponent()).getModel();
 				final List<Object> $list = new LinkedList<Object>();
 				for (int $i = 0; $i < $m.getRowCount(); $i++) {
 					if ((Boolean) $m.getValueAt($i, 0)) {
 						$list.add($m.getValueAt($i, 1));
 					}
 				}
 				return $list;
 			}
 		}
 		return new ArrayList<Object>(0);
 	}
 
 	public MagicComponents clearSelection() {
 		for (final Component $comp : $components) {
 			if ($comp instanceof MultiList) {
 				final DefaultTableModel $m = (DefaultTableModel) ((JTable) $comp.getRealComponent()).getModel();
 				for (int $i = 0; $i < $m.getRowCount(); $i++) {
 					$m.setValueAt(false, $i, 0);
 				}
 			}
 		}
 		return this;
 	}
 
 	public Object getSelectedItem() {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JComboBox) {
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					return $m.getSelectedItem();
 				}
 			} else if ($c instanceof JList) {
 				final ListModel $m = ((JList) $c).getModel();
 				try {
 					return $m.getElementAt(((JList) $c).getSelectedIndex());
 				} catch (final ArrayIndexOutOfBoundsException $exc) {
 					return null;
 				}
 			}
 		}
 		return null;
 	}
 
 	public Object getValueAt(final int $modelRow, final int $modelColumn) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JTable) {
 				final TableModel $m = ((JTable) $c).getModel();
 				if ($m instanceof DefaultTableModel) {
 
 				}
 				return $m.getValueAt($modelRow, $modelColumn);
 			}
 		}
 		return null;
 	}
 
 	public int getSelectedIndex() {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getRealComponent();
 			if ($c instanceof JComboBox) {
 				final ComboBoxModel $m = ((JComboBox) $c).getModel();
 				if ($m instanceof DefaultComboBoxModel) {
 					return ((DefaultComboBoxModel) $m).getIndexOf($m.getSelectedItem());
 				}
 			} else if ($c instanceof JList) {
 				return ((JList) $c).getSelectedIndex();
 			}
 		}
 		return -1;
 	}
 
 	public MagicComponents setValue(final int $value) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					try {
 						Method $m;
 						if (($m = $c.getClass().getMethod("setValue", int.class)) != null) {
 							$m.invoke($c, $value);
 						} else if (($m = $c.getClass().getMethod("setValue", Integer.class)) != null) {
 							$m.invoke($c, $value);
 						} else if (($m = $c.getClass().getMethod("setValue", double.class)) != null) {
 							$m.invoke($c, (double) $value);
 						}
 					} catch (final Exception $exc) {
 						$exc.printStackTrace(System.err);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	/**
 	 * Applies to all Components which have a “setMax” method (such as
 	 * &lt;progressbar&gt;, &lt;numeric&gt;)
 	 */
 	public MagicComponents setMax(final int $value) {
 		AbusingSwing.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				for (final Component $comp : $components) {
 					final JComponent $c = $comp.getRealComponent();
 					try {
 						Method $m;
 						if (($m = $c.getClass().getMethod("setMax", int.class)) != null) {
 							$m.invoke($c, $value);
 						} else if (($m = $c.getClass().getMethod("setMax", Integer.class)) != null) {
 							$m.invoke($c, $value);
 						} else if (($m = $c.getClass().getMethod("setMax", double.class)) != null) {
 							$m.invoke($c, (double) $value);
 						}
 					} catch (final Exception $exc) {
 						$exc.printStackTrace(System.err);
 					}
 				}
 			}
 		});
 		return this;
 	}
 
 	/**
 	 * Applies on &lt;multilist&gt;-Components only: Shows only the selected
 	 * items in the list.
 	 */
 	public void showSelectedOnly(final boolean $selected) {
 		for (final Component $comp : $components) {
 			if ($comp instanceof MultiList) {
 				AbusingSwing.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						((MultiList) $comp).showSelectedOnly($selected);
 					}
 				});
 			}
 		}
 	}
 
 	public void showBubble(final String $message) {
 		for (final Component $comp : $components) {
 			final JComponent $c = $comp.getJComponent();
 			if ($comp.getBalloonTip() != null) {
 				$comp.getBalloonTip().closeBalloon();
 			}
 			final BalloonTip $tip = new BalloonTip($c, $message);
 			$comp.setBalloonTip($tip);
 		}
 	}
 
 	public void clearBubble() {
 		for (final Component $comp : $components) {
 			if ($comp.getBalloonTip() != null) {
 				$comp.getBalloonTip().closeBalloon();
 				$comp.setBalloonTip(null);
 			}
 		}
 	}
 
 	public void goTo() {
 		for (final Component $comp : $components) {
 			if ($comp instanceof CardComponent) {
 				((CardComponent) $comp).goTo();
 			}
 		}
 	}
 }
