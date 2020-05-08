 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 
 import main.Settings;
 import util.ArrayUtil;
 import util.SwingUtil;
 import cluster.Clustering;
 import data.ClusteringData;
 
 public class MenuBar extends JMenuBar
 {
 	static interface MyMenuItem
 	{
 		public boolean isMenu();
 
 		public Action getAction();
 	}
 
 	static class DefaultMyMenuItem implements MyMenuItem
 	{
 		Action action;
 
 		public DefaultMyMenuItem(Action action)
 		{
 			this.action = action;
 		}
 
 		public DefaultMyMenuItem(Action action, boolean checkbox)
 		{
 			this.action = action;
 		}
 
 		@Override
 		public boolean isMenu()
 		{
 			return false;
 		}
 
 		@Override
 		public Action getAction()
 		{
 			return action;
 		}
 	}
 
 	static class MyMenu implements MyMenuItem
 	{
 		String name;
 		Vector<MyMenuItem> items = new Vector<MyMenuItem>();
 
 		public MyMenu(String name, Action... actions)
 		{
 			this.name = name;
 			for (Action action : actions)
 				items.add(new DefaultMyMenuItem(action));
 
 		}
 
 		public MyMenu(String name, MyMenuItem... items)
 		{
 			this.name = name;
 			for (MyMenuItem item : items)
 				this.items.add(item);
 		}
 
 		@Override
 		public boolean isMenu()
 		{
 			return true;
 		}
 
 		@Override
 		public Action getAction()
 		{
 			return null;
 		}
 	}
 
 	static class MyMenuBar
 	{
 		Vector<MyMenu> menus = new Vector<MyMenu>();
 		List<Action> actions = new ArrayList<Action>();
 
 		public MyMenuBar(MyMenu... menus)
 		{
 			for (MyMenu m : menus)
 				this.menus.add(m);
 
 			for (MyMenu m : menus)
 			{
 				for (MyMenuItem i : m.items)
 				{
 					if (!i.isMenu())
 						actions.add(i.getAction());
 					else
 						for (MyMenuItem ii : ((MyMenu) i).items)
 							actions.add(ii.getAction());
 				}
 			}
 		}
 	}
 
 	GUIControler guiControler;
 	ViewControler viewControler;
 	Clustering clustering;
 
 	MyMenuBar menuBar;
 
 	//file
 	Action fActionNew;
 	//edit
 	Action eActionRemoveCurrent;
 	Action eActionRemoveClusters;
 	Action eActionRemoveModels;
 	Action eActionExportCurrent;
 	Action eActionExportClusters;
 	Action eActionExportModels;
 	//view
 	Action vActionFullScreen;
 	Action vActionDrawHydrogens;
 	Action vActionHideUnselectedCompounds;
 	Action vActionSpin;
 	Action vActionBlackWhite;
 
 	//help
 
 	public MenuBar(GUIControler guiControler, ViewControler viewControler, Clustering clustering)
 	{
 		this.guiControler = guiControler;
 		this.viewControler = viewControler;
 		this.clustering = clustering;
 		buildActions();
 		buildMenu();
 		installListeners();
 		update();
 	}
 
 	private void buildMenu()
 	{
 		for (MyMenu m : menuBar.menus)
 		{
 			JMenu menu = new JMenu(m.name);
 			for (MyMenuItem i : m.items)
 			{
 				if (!i.isMenu())
 				{
 					if (i.getAction().getValue(Action.SELECTED_KEY) != null)
 					{
 						JCheckBoxMenuItem c = new JCheckBoxMenuItem(i.getAction());
 						menu.add(c);
 					}
 					else
 						menu.add(i.getAction());
 				}
 				else if (i instanceof MyMenu)
 				{
 					JMenu mm = new JMenu(((MyMenu) i).name);
 					for (MyMenuItem ii : ((MyMenu) i).items)
 						mm.add(((DefaultMyMenuItem) ii).action);
 					menu.add(mm);
 				}
 			}
 			add(menu);
 		}
 	}
 
 	public JPopupMenu getPopup()
 	{
 		JPopupMenu p = new JPopupMenu();
 		boolean first = true;
 		for (MyMenu m : menuBar.menus)
 		{
 			if (!first)
 				p.addSeparator();
 			else
 				first = false;
 			for (MyMenuItem i : m.items)
 			{
 				if (!i.isMenu())
 				{
 					if (i.getAction().getValue(Action.SELECTED_KEY) != null)
 					{
 						JCheckBoxMenuItem c = new JCheckBoxMenuItem(i.getAction());
 						p.add(c);
 					}
 					else
 						p.add(i.getAction());
 				}
 				else
 				{
 					JMenu mm = new JMenu(((MyMenu) i).name);
 					for (MyMenuItem ii : ((MyMenu) i).items)
 						mm.add(((DefaultMyMenuItem) ii).action);
 					p.add(mm);
 				}
 			}
 		}
 		return p;
 	}
 
 	private void buildActions()
 	{
 		fActionNew = new AbstractAction("New dataset / mapping")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				newClustering(0);
 			}
 		};
 		((AbstractAction) fActionNew).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK));
 		Action fActionExit = new AbstractAction("Exit")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				System.exit(0);
 			}
 		};
 		((AbstractAction) fActionExit).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
 		MyMenu fileMenu = new MyMenu("File", fActionNew, fActionExit);
 
 		eActionRemoveCurrent = new AbstractAction("Remove Selected Cluster/Compound")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int[] m = (int[]) ((AbstractAction) eActionRemoveCurrent).getValue("Model");
 				Integer c = (Integer) ((AbstractAction) eActionRemoveCurrent).getValue("Cluster");
 				View.instance.suspendAnimation("remove selected");
 				if (m.length > 0)
 					clustering.removeModels(m);
 				else if (c != null)
 					clustering.removeCluster(c);
 				View.instance.proceedAnimation("remove selected");
 			}
 		};
 		eActionRemoveCurrent.setEnabled(false);
 		((AbstractAction) eActionRemoveCurrent).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, ActionEvent.ALT_MASK));
 		eActionRemoveClusters = new AbstractAction("Remove Cluster/s")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				View.instance.suspendAnimation("remove clusters");
 				clustering.chooseClustersToRemove();
 				View.instance.proceedAnimation("remove clusters");
 			}
 		};
 		eActionRemoveModels = new AbstractAction("Remove Compound/s")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				View.instance.suspendAnimation("remove compounds");
 				clustering.chooseModelsToRemove();
 				View.instance.proceedAnimation("remove compounds");
 			}
 		};
 		MyMenu removeMenu = new MyMenu("Remove", eActionRemoveCurrent, eActionRemoveClusters, eActionRemoveModels);
 
 		eActionExportCurrent = new AbstractAction("Export Selected Cluster/Compound")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int[] m = (int[]) ((AbstractAction) eActionRemoveCurrent).getValue("Model");
 				Integer c = (Integer) ((AbstractAction) eActionRemoveCurrent).getValue("Cluster");
 				if (m.length > 0)
 					clustering.exportModels(m);
 				else if (c != null)
 					clustering.exportClusters(new int[] { c });
 			}
 		};
 		eActionExportClusters = new AbstractAction("Export Cluster/s")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				clustering.chooseClustersToExport();
 			}
 		};
 		eActionExportModels = new AbstractAction("Export Compound/s")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				clustering.chooseModelsToExport();
 			}
 		};
 		MyMenu exportMenu = new MyMenu("Export", eActionExportCurrent, eActionExportClusters, eActionExportModels);
 		MyMenu editMenu = new MyMenu("Edit", removeMenu, exportMenu);
 
 		vActionFullScreen = new AbstractAction("Fullscreen mode enabled")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				guiControler.setFullScreen(!guiControler.isFullScreen());
 			}
 		};
 		vActionFullScreen.putValue(Action.SELECTED_KEY, guiControler.isFullScreen());
 		((AbstractAction) vActionFullScreen).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.ALT_MASK));
 		guiControler.addPropertyChangeListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(GUIControler.PROPERTY_FULLSCREEN_CHANGED))
 					vActionFullScreen.putValue(Action.SELECTED_KEY, guiControler.isFullScreen());
 			}
 		});
 		vActionDrawHydrogens = new AbstractAction("Draw hydrogens (if available)")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				viewControler.setHideHydrogens(!viewControler.isHideHydrogens());
 			}
 		};
 		((AbstractAction) vActionDrawHydrogens).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
 		vActionDrawHydrogens.putValue(Action.SELECTED_KEY, !viewControler.isHideHydrogens());
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_SHOW_HYDROGENS))
 					vActionDrawHydrogens.putValue(Action.SELECTED_KEY, !viewControler.isHideHydrogens());
 			}
 		});
 
 		vActionHideUnselectedCompounds = new AbstractAction("Hide unselected compounds")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				viewControler.setHideUnselected(!viewControler.isHideUnselected());
 			}
 		};
 		((AbstractAction) vActionHideUnselectedCompounds).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK));
 		vActionHideUnselectedCompounds.putValue(Action.SELECTED_KEY, viewControler.isHideUnselected());
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_HIDE_UNSELECT_CHANGED))
 					vActionHideUnselectedCompounds.putValue(Action.SELECTED_KEY, viewControler.isHideUnselected());
 			}
 		});
 
 		vActionSpin = new AbstractAction("Spin enabled")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				viewControler.setSpinEnabled(!viewControler.isSpinEnabled());
 			}
 		};
 		((AbstractAction) vActionSpin).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
 		vActionSpin.putValue(Action.SELECTED_KEY, viewControler.isSpinEnabled());
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_SPIN_CHANGED))
 					vActionSpin.putValue(Action.SELECTED_KEY, viewControler.isSpinEnabled());
 			}
 		});
 
 		vActionBlackWhite = new AbstractAction("Background color black")
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				viewControler.setBlackgroundBlack(!viewControler.isBlackgroundBlack());
 			}
 		};
 		((AbstractAction) vActionBlackWhite).putValue(Action.ACCELERATOR_KEY,
 				KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
 		vActionBlackWhite.putValue(Action.SELECTED_KEY, viewControler.isBlackgroundBlack());
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_BACKGROUND_CHANGED))
 					vActionBlackWhite.putValue(Action.SELECTED_KEY, viewControler.isBlackgroundBlack());
 			}
 		});
 
 		MyMenu viewMenu = new MyMenu("View", vActionFullScreen, vActionDrawHydrogens, vActionHideUnselectedCompounds,
 				vActionSpin, vActionBlackWhite);
 
 		Action hActionAbout = new AbstractAction("About " + Settings.TITLE)
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showAboutDialog();
 			}
 		};
 		MyMenu helpMenu = new MyMenu("Help", hActionAbout);
 
 		menuBar = new MyMenuBar(fileMenu, editMenu, viewMenu, helpMenu);
 	}
 
 	public static void showAboutDialog()
 	{
 		TextPanel p = new TextPanel();
 		p.addHeading(Settings.TITLE);
 		p.addTable(new String[][] { { "Version:", Settings.VERSION_STRING }, { "Homepage:", Settings.HOMEPAGE },
 				{ "Contact:", "Martin Gütlein (martin.guetlein@gmail.com)" } });
 		JOptionPane.showMessageDialog(Settings.TOP_LEVEL_COMPONENT, p, "About " + Settings.TITLE,
 				JOptionPane.INFORMATION_MESSAGE, Settings.CHES_MAPPER_IMAGE);
 	}
 
 	private void update()
 	{
 
 		int m[] = new int[0];
 		Integer c = null;
 
 		if (clustering.isClusterActive())
 		{
 			if (clustering.isModelActive())
 				m = ArrayUtil.concat(m, clustering.getModelActive().getSelectedIndices());
 			if (clustering.isModelWatched() && ArrayUtil.indexOf(m, clustering.getModelWatched().getSelected()) == -1)
 				m = ArrayUtil.concat(m, new int[] { clustering.getModelWatched().getSelected() });
 		}
 		else if (clustering.isClusterWatched())
 			c = clustering.getClusterWatched().getSelected();
 
 		eActionRemoveCurrent.putValue("Cluster", c);
 		eActionRemoveCurrent.putValue("Model", m);
 
 		if (m.length > 0 || c != null)
 		{
 			if (m.length == 1)
 			{
 				((AbstractAction) eActionRemoveCurrent).putValue(Action.NAME,
 						"Remove " + clustering.getModelWithModelIndex(m[0]));
 				((AbstractAction) eActionExportCurrent).putValue(Action.NAME,
 						"Export " + clustering.getModelWithModelIndex(m[0]));
 			}
 			else if (m.length > 1)
 			{
 				((AbstractAction) eActionRemoveCurrent).putValue(Action.NAME, "Remove " + m.length + " Compounds");
 				((AbstractAction) eActionExportCurrent).putValue(Action.NAME, "Export " + m.length + " Compounds");
 			}
 			else if (c != -1)
 			{
 				((AbstractAction) eActionRemoveCurrent).putValue(Action.NAME, "Remove "
 						+ clustering.getCluster(c).getName());
 				((AbstractAction) eActionExportCurrent).putValue(Action.NAME, "Export "
 						+ clustering.getCluster(c).getName());
 			}
 			eActionRemoveCurrent.setEnabled(true);
 			eActionExportCurrent.setEnabled(true);
 		}
 		else
 		{
 			((AbstractAction) eActionRemoveCurrent).putValue(Action.NAME, "Remove Selected Cluster/Compound");
 			eActionRemoveCurrent.setEnabled(false);
 
 			((AbstractAction) eActionExportCurrent).putValue(Action.NAME, "Export Selected Cluster/Compound");
 			eActionExportCurrent.setEnabled(false);
 
 		}
 	}
 
 	private void installListeners()
 	{
 		clustering.getClusterActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update();
 			}
 		});
 		clustering.getClusterWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update();
 			}
 		});
 		clustering.getModelWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update();
 			}
 		});
 		clustering.getModelActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update();
 			}
 		});
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update();
 			}
 		});
 
 	}
 
 	private void newClustering(final int startPanel)
 	{
 		guiControler.block("new clustering");
 		Thread noAWTThread = new Thread(new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					final CheSMapperWizard wwd = new CheSMapperWizard((JFrame) SwingUtilities.getRoot(MenuBar.this),
 							startPanel);
 					Settings.TOP_LEVEL_COMPONENT = MenuBar.this.getTopLevelAncestor();
 					SwingUtil.waitWhileVisible(wwd);
 					if (wwd.isWorkflowSelected())
 					{
 						View.instance.suspendAnimation("remap");
 						clustering.clear();
 						ClusteringData d = CheSViewer.doMapping(wwd);
 						if (d != null)
 						{
 							clustering.newClustering(d);
 							CheSViewer.finalizeTask();
 						}
 						View.instance.proceedAnimation("remap");
 					}
 				}
 				finally
 				{
 					guiControler.unblock("new clustering");
 				}
 			}
 		});
 		noAWTThread.start();
 	}
 
 	/**
 	 * somehow the accelerate key registration does not work reliably, do that manually
 	 * 
 	 * @param e
 	 */
 	public void handleKeyEvent(KeyEvent e)
 	{
 		//		System.err.println("handle key event " + KeyEvent.getKeyText(e.getKeyCode()) + " "
 		//				+ KeyEvent.getKeyModifiersText(e.getModifiers()) + " " + e.getKeyCode() + " " + e.getModifiers());
 		for (Action action : menuBar.actions)
 		{
 			if (((AbstractAction) action).isEnabled())
 			{
 				KeyStroke k = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
 				if (k != null)
 				{
 					if (e.getKeyCode() == k.getKeyCode() && ((k.getModifiers() & e.getModifiers()) != 0))
 					{
 						//							System.err.println("perform " + action.toString());
 						action.actionPerformed(new ActionEvent(this, -1, ""));
 					}
 					else
 					{
 						//							System.err.println("no match: " + KeyEvent.getKeyText(k.getKeyCode()) + " "
 						//									+ KeyEvent.getKeyModifiersText(k.getModifiers()) + " " + k.getKeyCode() + " "
 						//									+ k.getModifiers());
 					}
 				}
 			}
 		}
 	}
 
 	public static void main(String args[])
 	{
 		showAboutDialog();
 	}
 
 }
